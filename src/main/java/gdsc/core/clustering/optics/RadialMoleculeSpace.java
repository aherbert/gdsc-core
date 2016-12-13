package gdsc.core.clustering.optics;

/**
 * Store molecules in a 2D grid and perform distance computation using cells within the radius from the centre. 
 */
class RadialMoleculeSpace extends GridMoleculeSpace
{
	/**
	 * Store start and end x for each strip in the radial mask
	 */
	private class Offset
	{
		final int start;
		final int startInternal;
		final int endInternal;
		final int end;
		final boolean internal;

		Offset(int start, int startInternal, int endInternal, int end)
		{
			this.start = start;
			this.startInternal = startInternal;
			this.endInternal = endInternal;
			this.end = end;
			internal = endInternal > startInternal;
		}
	}

	Offset[] offset;
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

	Molecule[] generate()
	{
		// Generate the grid
		Molecule[] m = super.generate();

		// Build a search space that can use a circular mask to only search over the required 
		// points in the 2D grid
		int size = 2 * resolution + 1;
		offset = new Offset[size];

		// We process the data in horizontal stripes.
		// Find the external and internal start and end for each stripe.
		// External: A pixel that cannot contain a distance within the generating distance
		// Internal: A pixel that could not contain a distance above the generating distance

		// We build this using only a quarter circle from the origin. The rest is filled using symmetry.
		// XXX
		// ...XXX
		// ......XX
		// ........XX
		// ..........X
		// ..........X

		float e = generatingDistanceE * generatingDistanceE;

		// External:
		// The closest distance to the origin is above the generating distance

		// The outer pixel inner corner (0,0) must be compared to the closest point in the origin pixel:
		// 1,0 for the first row
		// 1,1 for the rest
		// 0,1 when the outerX is <= 0
		// As we draw the arc anti-clockwise we update the origin.
		int origX = 1;
		int origY = 0;
		int outerX = resolution;

		// Internal:
		// The farthest distance to the origin is below the generating distance.

		// The outer pixel outer corner (1,1) must be compared to the farthest point in the origin pixel:
		int innerX = resolution;

		for (int y = 0; y <= resolution; y++)
		{
			// Move the outer pixel if it is above the limit and the minimum distance is currently outside.
			//while (outerX > 0 && distance2(origX, origY, outerX, y) > e)
			float target = e - distance2(origY, y);
			while (outerX > 0 && distance2(origX, outerX) > target)
			{
				outerX--;
				// Update origin
				if (outerX == 0)
					origX = 0;
			}

			// Update origin for subsequent rows
			origY = 1;

			// Move the inner pixel if it is above the limit and the maximum distance is not currently inside.
			// This may be at the limit so check before distance calculations
			if (innerX != -1)
			{
				//while (innerX > -1 && distance2(0, 0, innerX + 1, y + 1) > e)
				target = e - distance2(y + 1);
				while (innerX > -1 && distance2(innerX + 1) > target)
				{
					innerX--;
				}
			}

			// Mirror. Add 1 to the end points so we can use i=start; i<end; i++.
			int start = -outerX;
			int end = outerX + 1;
			int startInternal = -innerX;
			int endInternal = innerX + 1;
			offset[resolution - y] = new Offset(start, startInternal, endInternal, end);
		}

		// Initialise and mirror
		for (int i = 0, j = offset.length - 1; i <= resolution; i++, j--)
		{
			offset[j] = offset[i];
		}

		//		// Show an output mask image for debugging purposes of the region and the internal region.
		//		byte[] outer = new byte[getNeighbourBlocks(resolution)];
		//		byte[] inner = new byte[outer.length];
		//		for (int i = 0, k = 0; i < offset.length; i++)
		//		{
		//			for (int j = -resolution; j <= resolution; j++, k++)
		//			{
		//				if (j >= offset[i].start && j < offset[i].end)
		//					outer[k] = (byte) 255;
		//				if (j >= offset[i].startInternal && j < offset[i].endInternal)
		//					inner[k] = (byte) 255;
		//			}
		//		}
		//		int w = getNBlocks(resolution);
		//		// Test for symmetry
		//		outer: for (int y = 0, k = 0; y < w; y++)
		//		{
		//			for (int x = 0; x < w; x++, k++)
		//			{
		//				if (outer[k] != outer[x * w + y])
		//				{
		//					System.out.println("No outer symmetry");
		//					break outer;
		//				}
		//			}
		//		}
		//		outer: for (int y = 0, k = 0; y < w; y++)
		//		{
		//			for (int x = 0; x < w; x++, k++)
		//			{
		//				if (inner[k] != inner[x * w + y])
		//				{
		//					System.out.println("No inner symmetry");
		//					break outer;
		//				}
		//				// Test distance to centre
		//				if (inner[k] != 0 && distance2(x-resolution, y-resolution, 0, 0) > e)
		//				{
		//					System.out.printf("Bad inner: %d,%d\n", x, y);
		//					//break outer;
		//				}				
		//			}
		//		}

		//Utils.display("Outer", new ByteProcessor(w, w, outer));
		//Utils.display("inner", new ByteProcessor(w, w, inner));

		return m;
	}

	@SuppressWarnings("unused")
	private float distance2(int x, int y, int x2, int y2)
	{
		float dx = (x - x2) * binWidth;
		float dy = (y - y2) * binWidth;
		return dx * dx + dy * dy;
	}

	private float distance2(int x, int x2)
	{
		float dx = (x - x2) * binWidth;
		return dx * dx;
	}

	private float distance2(int x)
	{
		float dx = x * binWidth;
		return dx * dx;
	}

	@Override
	int determineMaximumResolution(float xrange, float yrange)
	{
		// TODO - determine a good resolution for the given generating distance

		return super.determineMaximumResolution(xrange, yrange);
	}

	@Override
	void adjustResolution(float xrange, float yrange)
	{
		// This has been optimised using a simple JUnit test to increase the number of molecules in the circle region.

		//If the grid is far too small then many of the lists in each cell will be empty.
		//If the grid is too small then many of the lists in each cell will be empty or contain only 1 item. 
		//This leads to setting up a for loop through only 1 item.
		//If the grid is too large then the outer cells may contain many points that are too far from the
		//centre, missing the chance to ignore them.

		double nMoleculesInPixel = (double) size / (xrange * yrange);
		double nMoleculesInCircle = Math.PI * generatingDistanceE * nMoleculesInPixel;

		int newResolution;

		if (useInternal)
		{
			// When using internal processing, we get an advantage from a higher resolution grid.
			// This only applies to the findNeighbours method used by DBSCAN. However since this is
			// what this class was written for we use a different look-up table.
			if (nMoleculesInCircle < 20)
				newResolution = 2;
			else if (nMoleculesInCircle < 25)
				newResolution = 3;
			else if (nMoleculesInCircle < 30)
				newResolution = 4;
			else if (nMoleculesInCircle < 35)
				newResolution = 5;
			else if (nMoleculesInCircle < 40)
				newResolution = 6;
			else if (nMoleculesInCircle < 45)
				newResolution = 7;
			else if (nMoleculesInCircle < 70)
				newResolution = 8;
			else
				// We continue to get benefit from high resolution as we can better define what is internal/external 
				newResolution = (int) (nMoleculesInCircle / 10);
		}
		else
		{
			if (nMoleculesInCircle < 20)
				newResolution = 2;
			else if (nMoleculesInCircle < 35)
				newResolution = 3;
			else if (nMoleculesInCircle < 40)
				newResolution = 4;
			else
				// When there are a lot more molecules then the speed is limited by the all-vs-all comparison, 
				// not finding the molecules so this is an upper limit.
				newResolution = 5;
		}

		resolution = Math.min(newResolution, resolution);
	}

	void findNeighbours(int minPts, Molecule object, float e)
	{
		//			if (true)
		//			{
		//				super.findNeighbours(minPts, object, e);
		//				return;
		//			}

		final int xBin = object.xBin;
		final int yBin = object.yBin;

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

		super.findNeighboursAndDistances(minPts, object, e);
	}
}