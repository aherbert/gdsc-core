package gdsc.core.clustering.optics;

import gdsc.core.clustering.optics.OPTICSManager.Option;

/**
 * Store molecules in a 2D grid
 */
class GridMoleculeSpace extends MoleculeSpace
{
	/**
	 * Used for access to the raw coordinates
	 */
	private final OPTICSManager opticsManager;

	int resolution;
	float binWidth;
	int xBins;
	int yBins;
	Molecule[][] grid;
	final int[] fastForward;

	GridMoleculeSpace(OPTICSManager opticsManager, float generatingDistanceE)
	{
		super(opticsManager.getSize(), generatingDistanceE);

		this.opticsManager = opticsManager;
		setOfObjects = generate();

		// Traverse the grid and store the index to the next position that contains data
		int count = 0;
		int index = grid.length;
		fastForward = new int[index];
		for (int i = index; i-- > 0;)
		{
			fastForward[i] = index;
			if (grid[i] != null)
			{
				index = i;
				count += grid[i].length;
			}
		}
		if (count != setOfObjects.length)
			throw new RuntimeException("Grid does not contain all the objects");
	}

	Molecule[] generate()
	{
		final float minXCoord = opticsManager.getMinimumX();
		final float maxXCoord = opticsManager.getMaximumX();
		final float minYCoord = opticsManager.getMinimumY();
		final float maxYCoord = opticsManager.getMaximumY();

		final float xrange = maxXCoord - minXCoord;
		final float yrange = maxYCoord - minYCoord;

		final float[] xcoord = opticsManager.getXData();
		final float[] ycoord = opticsManager.getYData();

		if (xrange == 0 && yrange == 0)
		{
			resolution = 1;
			binWidth = 1;
		}
		else
		{
			// Use a higher resolution grid to avoid too many distance comparisons
			resolution = determineResolution(xrange, yrange);

			if (resolution == 0)
			{
				// Handle a resolution of zero. This will happen when the generating distance is very small.
				// In this instance we can use a resolution of 1 but change the bin width to something larger.
				resolution = 1;
				binWidth = determineBinWidth(xrange, yrange);
			}
			else
			{
				adjustResolution(xrange, yrange);
				binWidth = generatingDistanceE / resolution;

				//System.out.printf("e=%f, bw=%f, r=%d, w=%f\n", generatingDistanceE, binWidth, resolution, binWidth * resolution);
			}
		}

		// Assign to a grid
		xBins = 1 + (int) (xrange / binWidth);
		yBins = 1 + (int) (yrange / binWidth);

		// Use a transpose grid to allow freeing memory (as we later process in the y then x order)
		Molecule[][] linkedListGrid = new Molecule[yBins][];
		for (int yBin = 0; yBin < yBins; yBin++)
			linkedListGrid[yBin] = new Molecule[xBins];

		Molecule[] setOfObjects = new Molecule[xcoord.length];
		for (int i = 0; i < xcoord.length; i++)
		{
			final float x = xcoord[i];
			final float y = ycoord[i];
			final int xBin = (int) ((x - minXCoord) / binWidth);
			final int yBin = (int) ((y - minYCoord) / binWidth);
			// Build a single linked list
			final Molecule m = new Molecule(i, x, y, xBin, yBin, linkedListGrid[yBin][xBin]);
			setOfObjects[i] = m;
			linkedListGrid[yBin][xBin] = m;
		}

		// Convert grid to arrays ...
		grid = new Molecule[xBins * yBins][];
		for (int yBin = 0, index = 0; yBin < yBins; yBin++)
		{
			for (int xBin = 0; xBin < xBins; xBin++, index++)
			{
				if (linkedListGrid[yBin][xBin] == null)
					continue;
				int count = 0;
				for (Molecule m = linkedListGrid[yBin][xBin]; m != null; m = m.next)
					count++;
				final Molecule[] list = new Molecule[count];
				for (Molecule m = linkedListGrid[yBin][xBin]; m != null; m = m.next)
					list[--count] = m;
				grid[index] = list;
			}
			// Free memory
			linkedListGrid[yBin] = null;
		}

		return setOfObjects;
	}

	int determineResolution(float xrange, float yrange)
	{
		int resolution = 0;
		// What is a good maximum limit for the memory allocation?
		while (getBins(xrange, yrange, generatingDistanceE, resolution + 1) < 100000)
		{
			resolution++;
		}
		//System.out.printf("d=%.3f  [%d]\n", generatingDistanceE, resolution);
		// We handle a resolution of zero in the calling function
		return resolution;
	}

	void adjustResolution(final float xrange, final float yrange)
	{
		if (opticsManager.options.contains(Option.HIGH_RESOLUTION))
		{
			return;
		}

		// Do not increase the resolution so high we have thousands of blocks
		// and not many expected points.		
		// Determine the number of molecules we would expect in a square block if they are uniform.
		double blockArea = 4 * generatingDistanceE;
		double expected = opticsManager.getSize() * blockArea / (xrange * yrange);

		// It is OK if 25-50% of the blocks are full
		int newResolution = 1;

		double target = expected / 0.25;

		// Closest
		//				double minDelta = Math.abs(getNeighbourBlocks(newResolution) - target);
		//				while (newResolution < resolution)
		//				{
		//					double delta = Math.abs(getNeighbourBlocks(newResolution + 1) - target);
		//					if (delta < minDelta)
		//					{
		//						minDelta = delta;
		//						newResolution++;
		//					}
		//					else
		//						break;
		//				}

		// Next size up
		while (newResolution < resolution)
		{
			if (getNeighbourBlocks(newResolution) < target)
				newResolution++;
			else
				break;
		}

		resolution = newResolution;

		//System.out.printf("Expected %.2f [%d]\n", expected, (2 * resolution + 1) * (2 * resolution + 1));
	}

	private float determineBinWidth(float xrange, float yrange)
	{
		float binWidth = generatingDistanceE;
		while (getBins(xrange, yrange, binWidth, 1) > 100000)
		{
			// Dumb implementation that doubles the bin width. A better solution
			// would be to conduct a search for the value with a number of bins close 
			// to the target.
			binWidth *= 2;
		}
		return binWidth;
	}

	private int getBins(float xrange, float yrange, float distance, int resolution)
	{
		final float binWidth = distance / resolution;
		final int nXBins = 1 + (int) (xrange / binWidth);
		final int nYBins = 1 + (int) (yrange / binWidth);
		final int nBins = nXBins * nYBins;
		//System.out.printf("d=%.3f  %d => %d\n", generatingDistanceE, resolution, nBins);
		return nBins;
	}

	int getNBlocks(int resolution)
	{
		return 2 * resolution + 1;
	}

	int getNeighbourBlocks(int resolution)
	{
		int size = getNBlocks(resolution);
		return size * size;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.core.clustering.optics.OPTICSManager.MoleculeSpace#findNeighbours(int,
	 * gdsc.core.clustering.optics.OPTICSManager.Molecule, float)
	 */
	void findNeighbours(int minPts, Molecule object, float e)
	{
		//boolean noFF = true;

		// Match findNeighboursAndDistances(minPts, object, e);
		// But do not store the distances

		final int xBin = object.xBin;
		final int yBin = object.yBin;

		neighbours.clear();

		// Pre-compute range
		final int minx = Math.max(xBin - resolution, 0);
		final int maxx = Math.min(xBin + resolution + 1, xBins);
		final int miny = Math.max(yBin - resolution, 0);
		final int maxy = Math.min(yBin + resolution + 1, yBins);

		// Count if there are enough neighbours
		int count = minPts;
		counting: for (int y = miny; y < maxy; y++)
		{
			//				if (noFF)
			//				{
			//					for (int x = minx, index = getIndex(minx, y); x < maxx; x++, index++)
			//					{
			//						if (grid[index] != null)
			//						{
			//							count -= grid[index].length;
			//							if (count <= 0)
			//								break counting;
			//						}
			//					}
			//				}
			//				else
			//				{
			// Use fast-forward to skip to the next position with data
			int index = getIndex(minx, y);
			if (grid[index] == null)
				index = fastForward[index];
			int endIndex = getIndex(maxx, y);
			while (index < endIndex)
			{
				count -= grid[index].length;
				if (count <= 0)
					break counting;
				index = fastForward[index];
			}
			//				}
		}

		if (count > 0)
		{
			// Not a core point so do not compute distances
			//System.out.println("Skipping distance computation (not a core point)");
			return;
		}

		// Compute distances
		for (int y = miny; y < maxy; y++)
		{
			//				if (noFF)
			//				{
			//					for (int x = minx, index = getIndex(minx, y); x < maxx; x++, index++)
			//					{
			//						if (grid[index] != null)
			//						{
			//							final Molecule[] list = grid[index];
			//							for (int i = list.length; i-- > 0;)
			//							{
			//								if (object.distance2(list[i]) <= e)
			//								{
			//									// Build a list of all the neighbours
			//									neighbours.add(list[i]);
			//								}
			//							}
			//						}
			//					}
			//				}
			//				else
			//				{
			// Use fast-forward to skip to the next position with data
			int index = getIndex(minx, y);
			if (grid[index] == null)
				index = fastForward[index];
			int endIndex = getIndex(maxx, y);
			while (index < endIndex)
			{
				final Molecule[] list = grid[index];
				for (int i = list.length; i-- > 0;)
				{
					if (object.distance2(list[i]) <= e)
					{
						// Build a list of all the neighbours
						neighbours.add(list[i]);
					}
				}
				index = fastForward[index];
			}
			//				}
		}

		//			// Full debug of the neighbours that were found
		//			final MoleculeList neighbours2 = new MoleculeList(size);
		//			for (int index = grid.length; index-- > 0;)
		//			{
		//				if (grid[index] != null)
		//				{
		//					final Molecule[] list = grid[index];
		//					for (int i = list.length; i-- > 0;)
		//					{
		//						if (object.distance2(list[i]) <= e)
		//						{
		//							// Build a list of all the neighbours
		//							neighbours2.add(list[i]);
		//						}
		//					}
		//				}
		//			}
		//			
		//			if (neighbours2.size != neighbours.size)
		//			{
		//				System.out.printf("Size error %d vs %d @ %d/%d,%d/%d [%d] %f,%f [%d-%d,%d-%d] %f\n", neighbours2.size,
		//						neighbours.size, xBin, xBins, yBin, yBins, resolution, object.x, object.y, minx, maxx, miny,
		//						maxy, Math.sqrt(e));
		//				for (int i = 0; i < neighbours2.size; i++)
		//				{
		//					Molecule o = neighbours2.get(i);
		//					System.out.printf("%d,%d %f,%f = %f\n", o.xBin, o.yBin, o.x, o.y, object.distance(o));
		//				}
		//			}
	}

	int getIndex(final int x, final int y)
	{
		return y * xBins + x;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.core.clustering.optics.OPTICSManager.MoleculeSpace#findNeighboursAndDistances(int,
	 * gdsc.core.clustering.optics.OPTICSManager.Molecule, float)
	 */
	void findNeighboursAndDistances(int minPts, Molecule object, float e)
	{
		//boolean noFF = true;

		final int xBin = object.xBin;
		final int yBin = object.yBin;

		neighbours.clear();

		// Pre-compute range
		final int minx = Math.max(xBin - resolution, 0);
		final int maxx = Math.min(xBin + resolution + 1, xBins);
		final int miny = Math.max(yBin - resolution, 0);
		final int maxy = Math.min(yBin + resolution + 1, yBins);

		// Count if there are enough neighbours
		int count = minPts;
		counting: for (int y = miny; y < maxy; y++)
		{
			//				if (noFF)
			//				{
			//					for (int x = minx, index = getIndex(minx, y); x < maxx; x++, index++)
			//					{
			//						if (grid[index] != null)
			//						{
			//							count -= grid[index].length;
			//							if (count <= 0)
			//								break counting;
			//						}
			//					}
			//				}
			//				else
			//				{
			// Use fast-forward to skip to the next position with data
			int index = getIndex(minx, y);
			if (grid[index] == null)
				index = fastForward[index];
			int endIndex = getIndex(maxx, y);
			while (index < endIndex)
			{
				count -= grid[index].length;
				if (count <= 0)
					break counting;
				index = fastForward[index];
			}
			//				}
		}

		if (count > 0)
		{
			// Not a core point so do not compute distances
			//System.out.println("Skipping distance computation (not a core point)");
			return;
		}

		// Compute distances
		for (int y = miny; y < maxy; y++)
		{
			//				if (noFF)
			//				{
			//					for (int x = minx, index = getIndex(minx, y); x < maxx; x++, index++)
			//					{
			//						if (grid[index] != null)
			//						{
			//							final Molecule[] list = grid[index];
			//							for (int i = list.length; i-- > 0;)
			//							{
			//								final float d = object.distance2(list[i]);
			//								if (d <= e)
			//								{
			//									// Build a list of all the neighbours and their working distance
			//									final Molecule otherObject = list[i];
			//									otherObject.d = d;
			//									neighbours.add(otherObject);
			//								}
			//							}
			//						}
			//					}
			//				}
			//				else
			//				{
			// Use fast-forward to skip to the next position with data
			int index = getIndex(minx, y);
			if (grid[index] == null)
				index = fastForward[index];
			int endIndex = getIndex(maxx, y);
			while (index < endIndex)
			{
				final Molecule[] list = grid[index];
				for (int i = list.length; i-- > 0;)
				{
					final float d = object.distance2(list[i]);
					if (d <= e)
					{
						// Build a list of all the neighbours and their working distance
						final Molecule otherObject = list[i];
						otherObject.d = d;
						neighbours.add(otherObject);
					}
				}
				index = fastForward[index];
			}
			//				}
		}
	}
}