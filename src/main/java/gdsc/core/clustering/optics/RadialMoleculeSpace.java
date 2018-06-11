/*-
 * #%L
 * Genome Damage and Stability Centre ImageJ Core Package
 * 
 * Contains code used by:
 * 
 * GDSC ImageJ Plugins - Microscopy image analysis
 * 
 * GDSC SMLM ImageJ Plugins - Single molecule localisation microscopy (SMLM)
 * %%
 * Copyright (C) 2011 - 2018 Alex Herbert
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
package gdsc.core.clustering.optics;

/**
 * Store molecules in a 2D grid and perform distance computation using cells within the radius from the centre.
 */
class RadialMoleculeSpace extends GridMoleculeSpace
{
	CircularKernelOffset[] offset;
	private final boolean useInternal;

	RadialMoleculeSpace(OPTICSManager opticsManager, float generatingDistanceE)
	{
		this(opticsManager, generatingDistanceE, 0);
	}

	RadialMoleculeSpace(OPTICSManager opticsManager, float generatingDistanceE, int resolution)
	{
		super(opticsManager, generatingDistanceE, resolution);
		useInternal = opticsManager.getOptions().contains(OPTICSManager.Option.INNER_PROCESSING);
	}

	RadialMoleculeSpace(OPTICSManager opticsManager, float generatingDistanceE, int resolution, boolean useInternal)
	{
		super(opticsManager, generatingDistanceE, resolution);
		this.useInternal = useInternal;
	}

	@Override
	public String toString()
	{
		return String.format("%s, e=%f, bw=%f, r=%d, in=%b", this.getClass().getSimpleName(), generatingDistanceE,
				binWidth, resolution, useInternal);
	}

	@Override
	Molecule[] generate()
	{
		// Generate the grid
		Molecule[] m = super.generate();

		offset = CircularKernelOffset.create(resolution);

		return m;
	}

	@Override
	int determineMaximumResolution(float xrange, float yrange)
	{
		int resolution = 0;

		// A reasonable upper bound is that:
		// - resolution should be 2 or above (to get the advantage of scanning the region around a point using cells).
		// However we must ensure that we have the memory to create the grid.

		// Q. What is a good maximum limit for the memory allocation?
		while (getBins(xrange, yrange, generatingDistanceE, resolution + 1) < 4096 * 4096 || resolution < 2)
		{
			resolution++;
		}
		//System.out.printf("d=%.3f  [%d]\n", generatingDistanceE, resolution);
		// We handle a resolution of zero in the calling function
		return resolution;
	}

	@Override
	double getNMoleculesInGeneratingArea(float xrange, float yrange)
	{
		double nMoleculesInPixel = (double) size / (xrange * yrange);
		double nMoleculesInCircle = Math.PI * generatingDistanceE * generatingDistanceE * nMoleculesInPixel;
		return nMoleculesInCircle;
	}

	/**
	 * Hold the point where inner processing starts to use a higher resolution grid.
	 */
	static int N_MOLECULES_FOR_NEXT_RESOLUTION_INNER = 150;
	/**
	 * Hold the point where processing starts to use a higher resolution grid.
	 */
	static int N_MOLECULES_FOR_NEXT_RESOLUTION_OUTER = 150;

	@Override
	void adjustResolution(float xrange, float yrange)
	{
		// This has been optimised using a simple JUnit test to increase the number of molecules in the circle region.

		//If the grid is far too small then many of the lists in each cell will be empty.
		//If the grid is too small then many of the lists in each cell will be empty or contain only 1 item. 
		//This leads to setting up a for loop through only 1 item.
		//If the grid is too large then the outer cells may contain many points that are too far from the
		//centre, missing the chance to ignore them.

		double nMoleculesInArea = getNMoleculesInGeneratingArea(xrange, yrange);

		int newResolution;

		if (useInternal)
		{
			// When using internal processing, we use a different look-up table. This is because
			// there are additional loop constructs that must be maintained and there is a time penalty 
			// for this due to complexity.

			if (nMoleculesInArea < N_MOLECULES_FOR_NEXT_RESOLUTION_INNER)
				newResolution = 2;
			else if (nMoleculesInArea < 500)
				newResolution = 3;
			else if (nMoleculesInArea < 1000)
				newResolution = 4;
			else
				// Above this limit the resolution of the circles is good.
				// TODO - Build the inner and outer circle with different resolutions and see how the area
				// converges as resolution increases (number of pixels * area of single pixel). 
				// At a certain point additional resolution will add more pixels
				// but will not better define the circle.
				newResolution = 5;
		}
		else
		{
			if (nMoleculesInArea < N_MOLECULES_FOR_NEXT_RESOLUTION_OUTER)
				newResolution = 2;
			else if (nMoleculesInArea < 300)
				newResolution = 3;
			else if (nMoleculesInArea < 500)
				newResolution = 4;
			else
				// Above this limit the resolution of the circles is good.
				// TODO - Build the inner and outer circle with different resolutions and see how the area
				// converges as resolution increases (number of pixels * area of single pixel). 
				// At a certain point additional resolution will add more pixels
				// but will not better define the circle.
				newResolution = 5;
		}

		resolution = Math.min(newResolution, resolution);
	}

	@Override
	void findNeighbours(int minPts, Molecule object, float e)
	{
		//			if (true)
		//			{
		//				super.findNeighbours(minPts, object, e);
		//				return;
		//			}

		final int xBin = object.getXBin();
		final int yBin = object.getYBin();

		neighbours.clear();

		// Use a circle mask over the grid to enumerate the correct cells
		// Only compute distances at the edge of the mask 

		// Pre-compute range
		final int miny = Math.max(yBin - resolution, 0);
		final int maxy = Math.min(yBin + resolution + 1, yBins);
		final int startRow = Math.max(resolution - yBin, 0);

		// TODO - Determine any situation under which this can be made faster than
		// just computing all the distances.

		//		// Count if there are enough neighbours
		//		int count = minPts;
		//		counting: for (int y = miny, row = startRow; y < maxy; y++, row++)
		//		{
		//			// Dynamically compute the search strip 
		//			final int minx = Math.max(xBin + offset[row].start, 0);
		//			final int maxx = Math.min(xBin + offset[row].end, xBins);
		//
		//			// Use fast-forward to skip to the next position with data
		//			int index = getIndex(minx, y);
		//			if (grid[index] == null)
		//				index = fastForward[index];
		//			int endIndex = getIndex(maxx, y);
		//			while (index < endIndex)
		//			{
		//				count -= grid[index].length;
		//				if (count <= 0)
		//					break counting;
		//				index = fastForward[index];
		//			}
		//		}
		//
		//		if (count > 0)
		//		{
		//			// Not a core point so do not compute distances
		//			//System.out.println("Skipping distance computation (not a core point)");
		//			return;
		//		}

		if (useInternal)
		{
			// Internal processing. Any pixel that is internal does not require 
			// a distance computation.

			if (xBin + resolution < xBins && xBin - resolution >= 0)
			{
				// Internal X. Maintain the centre index and use offsets to set the indices
				int centreIndex = getIndex(xBin, miny);

				for (int y = miny, row = startRow; y < maxy; y++, row++, centreIndex += xBins)
				{
					// Dynamically compute the search strip 
					int index = centreIndex + offset[row].start;
					int endIndex = centreIndex + offset[row].end;

					// Use fast-forward to skip to the next position with data
					if (grid[index] == null)
						index = fastForward[index];

					if (offset[row].internal)
					{
						// Speed this up with diffs
						int startInternal = centreIndex + offset[row].startInternal;
						int endInternal = centreIndex + offset[row].endInternal;

						while (index < startInternal)
						{
							final Molecule[] list = grid[index];

							// Build a list of all the neighbours
							// If at the edge then compute distances
							for (int i = list.length; i-- > 0;)
							{
								if (object.distance2(list[i]) <= e)
								{
									neighbours.add(list[i]);
								}
							}

							index = fastForward[index];
						}
						while (index < endInternal)
						{
							final Molecule[] list = grid[index];

							// Build a list of all the neighbours
							// If internal just add all the points

							// This uses System.arrayCopy.
							neighbours.add(list);

							// Simple addition
							//for (int i = list.length; i-- > 0;)
							//	neighbours.add(list[i]);					

							index = fastForward[index];
						}
						while (index < endIndex)
						{
							final Molecule[] list = grid[index];

							// Build a list of all the neighbours
							// If at the edge then compute distances
							for (int i = list.length; i-- > 0;)
							{
								if (object.distance2(list[i]) <= e)
								{
									neighbours.add(list[i]);
								}
							}

							index = fastForward[index];
						}
					}
					else
					{
						while (index < endIndex)
						{
							final Molecule[] list = grid[index];

							// Build a list of all the neighbours
							// If not internal then compute distances
							for (int i = list.length; i-- > 0;)
							{
								if (object.distance2(list[i]) <= e)
								{
									neighbours.add(list[i]);
								}
							}

							index = fastForward[index];
						}
					}
				}
			}
			else
			{
				// This must respect the bounds

				// Compute distances
				for (int y = miny, row = startRow; y < maxy; y++, row++)
				{
					// Dynamically compute the search strip 
					int index = getIndex(Math.max(xBin + offset[row].start, 0), y);
					int endIndex = getIndex(Math.min(xBin + offset[row].end, xBins), y);

					// Use fast-forward to skip to the next position with data
					if (grid[index] == null)
						index = fastForward[index];

					if (offset[row].internal)
					{
						// Speed this up with diffs
						int startInternal = getIndex(xBin + offset[row].startInternal, y);
						int endInternal = getIndex(Math.min(xBin + offset[row].endInternal, xBins), y);

						while (index < startInternal)
						{
							final Molecule[] list = grid[index];

							// Build a list of all the neighbours
							// If at the edge then compute distances
							for (int i = list.length; i-- > 0;)
							{
								if (object.distance2(list[i]) <= e)
								{
									neighbours.add(list[i]);
								}
							}

							index = fastForward[index];
						}
						while (index < endInternal)
						{
							final Molecule[] list = grid[index];

							// Build a list of all the neighbours
							// If internal just add all the points

							// This uses System.arrayCopy.
							neighbours.add(list);

							// Simple addition
							//for (int i = list.length; i-- > 0;)
							//	neighbours.add(list[i]);					

							index = fastForward[index];
						}
						while (index < endIndex)
						{
							final Molecule[] list = grid[index];

							// Build a list of all the neighbours
							// If at the edge then compute distances
							for (int i = list.length; i-- > 0;)
							{
								if (object.distance2(list[i]) <= e)
								{
									neighbours.add(list[i]);
								}
							}

							index = fastForward[index];
						}
					}
					else
					{
						while (index < endIndex)
						{
							final Molecule[] list = grid[index];

							// Build a list of all the neighbours
							// If not internal then compute distances
							for (int i = list.length; i-- > 0;)
							{
								if (object.distance2(list[i]) <= e)
								{
									neighbours.add(list[i]);
								}
							}

							index = fastForward[index];
						}
					}
				}
			}
		}
		else
		{
			// No use of the internal region. Always compute distances. This is fine if the 
			// number of points per strip is low (i.e. not many distances to compute).

			if (xBin + resolution < xBins && xBin - resolution >= 0)
			{
				// Internal X. Maintain the centre index and use offsets to set the indices
				int centreIndex = getIndex(xBin, miny);

				for (int y = miny, row = startRow; y < maxy; y++, row++, centreIndex += xBins)
				{
					// Dynamically compute the search strip 
					int index = centreIndex + offset[row].start;
					int endIndex = centreIndex + offset[row].end;

					// Use fast-forward to skip to the next position with data
					if (grid[index] == null)
						index = fastForward[index];

					while (index < endIndex)
					{
						final Molecule[] list = grid[index];

						// Build a list of all the neighbours
						// If not internal then compute distances
						for (int i = list.length; i-- > 0;)
						{
							if (object.distance2(list[i]) <= e)
							{
								neighbours.add(list[i]);
							}
						}

						index = fastForward[index];
					}
				}
			}
			else
			{
				// This must respect the bounds

				// Compute distances
				for (int y = miny, row = startRow; y < maxy; y++, row++)
				{
					// Dynamically compute the search strip 
					int index = getIndex(Math.max(xBin + offset[row].start, 0), y);
					int endIndex = getIndex(Math.min(xBin + offset[row].end, xBins), y);

					// Use fast-forward to skip to the next position with data
					if (grid[index] == null)
						index = fastForward[index];

					while (index < endIndex)
					{
						final Molecule[] list = grid[index];

						// Build a list of all the neighbours
						// If not internal then compute distances
						for (int i = list.length; i-- > 0;)
						{
							if (object.distance2(list[i]) <= e)
							{
								neighbours.add(list[i]);
							}
						}

						index = fastForward[index];
					}
				}
			}
		}
	}

	@Override
	void findNeighboursAndDistances(int minPts, Molecule object, float e)
	{
		// TODO - could this be implemented to use concentric rings around the current pixel
		// We would need to pre-compute all the bounds for each concentric ring.
		// then process from the central point outward. When the min points is achieved 
		// we then compute the core distance using the molecules in the most recent ring. 
		// For all remaining points outside the core distance
		// we only need to compute the reachability distance if it is currently UNDEFINED
		// or it is greater than the core distance.

		//Sweep grid in concentric squares. This is much easier then concentric circles as 
		//we can ensure the bounds are checked only once.
		//
		//If we use circles then we could do this if the quarter circle is within bounds 
		//by storing a list of index offsets. If the quarter circle intersects the edge of the grid then each position must be checked it is inside the bounds. This means storing the xy offset as well as the direct index. It is a lot of bounds comparisons.
		//
		//To sweep a concentric square ring you do upper and lower edges first. Then column 
		//edges with an an index 1 inside. This avoids counting corners twice. This probably 
		//needs 4 loops as each must be checked if it is inside.
		//
		//We can avoid checks if the max square is inside the grid. If not then we can avoid 
		//checks up to the first intersect ring.

		final int xBin = object.getXBin();
		final int yBin = object.getYBin();

		neighbours.clear();

		// Use a circle mask over the grid to enumerate the correct cells
		// Only compute distances at the edge of the mask 

		// Pre-compute range
		final int miny = Math.max(yBin - resolution, 0);
		final int maxy = Math.min(yBin + resolution + 1, yBins);
		final int startRow = Math.max(resolution - yBin, 0);

		if (xBin + resolution < xBins && xBin - resolution >= 0)
		{
			// Internal X. Maintain the centre index and use offsets to set the indices
			int centreIndex = getIndex(xBin, miny);

			for (int y = miny, row = startRow; y < maxy; y++, row++, centreIndex += xBins)
			{
				// Dynamically compute the search strip 
				int index = centreIndex + offset[row].start;
				int endIndex = centreIndex + offset[row].end;

				// Use fast-forward to skip to the next position with data
				if (grid[index] == null)
					index = fastForward[index];

				while (index < endIndex)
				{
					final Molecule[] list = grid[index];

					// Build a list of all the neighbours
					// If not internal then compute distances
					for (int i = list.length; i-- > 0;)
					{
						final float d = object.distance2(list[i]);
						if (d <= e)
						{
							// Build a list of all the neighbours and their working distance
							final Molecule otherObject = list[i];
							otherObject.setD(d);
							neighbours.add(otherObject);
						}
					}

					index = fastForward[index];
				}
			}
		}
		else
		{
			// This must respect the bounds

			// Compute distances
			for (int y = miny, row = startRow; y < maxy; y++, row++)
			{
				// Dynamically compute the search strip 
				int index = getIndex(Math.max(xBin + offset[row].start, 0), y);
				int endIndex = getIndex(Math.min(xBin + offset[row].end, xBins), y);

				// Use fast-forward to skip to the next position with data
				if (grid[index] == null)
					index = fastForward[index];

				while (index < endIndex)
				{
					final Molecule[] list = grid[index];

					// Build a list of all the neighbours
					// If not internal then compute distances
					for (int i = list.length; i-- > 0;)
					{
						final float d = object.distance2(list[i]);
						if (d <= e)
						{
							// Build a list of all the neighbours and their working distance
							final Molecule otherObject = list[i];
							otherObject.setD(d);
							neighbours.add(otherObject);
						}
					}

					index = fastForward[index];
				}
			}
		}
	}
}
