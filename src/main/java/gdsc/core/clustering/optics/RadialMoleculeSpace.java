package gdsc.core.clustering.optics;

/**
 * Store molecules in a high resolution 2D grid and perform distance computation
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

		Offset(int start, int startInternal, int endInternal, int end)
		{
			this.start = start;
			this.startInternal = startInternal;
			this.endInternal = endInternal;
			this.end = end;
		}
	}

	Offset[] offset;

	RadialMoleculeSpace(OPTICSManager opticsManager, float generatingDistanceE)
	{
		super(opticsManager, generatingDistanceE);
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
	int determineResolution(float xrange, float yrange)
	{
		// TODO - determine a good resolution for the given generating distance

		return super.determineResolution(xrange, yrange);
	}

	@Override
	void adjustResolution(float xrange, float yrange)
	{
		// TODO - prevent the resolution from being reduced too small 

		super.adjustResolution(xrange, yrange);
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

		// TODO - Use a circle mask over the grid to enumerate the correct cells
		// Only compute distances at the edge of the mask 

		// Pre-compute range
		final int miny = Math.max(yBin - resolution, 0);
		final int maxy = Math.min(yBin + resolution + 1, yBins);
		final int startRow = Math.max(resolution - yBin, 0);

		// Count if there are enough neighbours
		int count = minPts;
		counting: for (int y = miny, row = startRow; y < maxy; y++, row++)
		{
			// Dynamically compute the search strip 
			final int minx = Math.max(xBin + offset[row].start, 0);
			final int maxx = Math.min(xBin + offset[row].end, xBins);

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
		}

		if (count > 0)
		{
			// Not a core point so do not compute distances
			//System.out.println("Skipping distance computation (not a core point)");
			return;
		}

		// Compute distances
		for (int y = miny, row = startRow, columnShift = getIndex(xBin,
				startRow); y < maxy; y++, row++, columnShift += xBins)
		{
			// Dynamically compute the search strip 
			final int minx = Math.max(xBin + offset[row].start, 0);
			final int maxx = Math.min(xBin + offset[row].end, xBins);

			// Use fast-forward to skip to the next position with data
			int index = getIndex(minx, y);
			if (grid[index] == null)
				index = fastForward[index];
			int endIndex = getIndex(maxx, y);
			while (index < endIndex)
			{
				final Molecule[] list = grid[index];

				// Build a list of all the neighbours

				// Find the column in the circular mask: It should range from -resolution to +resolution.
				final int col = index - columnShift;

				// TODO - Can this be made more efficient with an internal flag (i.e. 1 comparison per loop)?

				// If internal just add all the points
				if (col >= offset[row].startInternal && col < offset[row].endInternal)
				{
					neighbours.add(list);
//					// Debug ...
//					for (int i = list.length; i-- > 0;)
//					{
//						double d = object.distance2(list[i]);
//						if (d > e)
//						{
//							float dx = binWidth * (xBin - list[i].xBin);
//							float dy = binWidth * (yBin - list[i].yBin);
//							System.out.printf("%d  %d/%d %d/%d (%f)  %f > %f :  %d %d %f %f  %f\n", resolution, col,
//									xBin, row, yBin, binWidth, Math.sqrt(d), generatingDistanceE, xBin - list[i].xBin,
//									yBin - list[i].yBin, dx, dy, Math.sqrt(dx * dx + dy * dy));
//						}
//					}
				}
				else
				{
					// If at the edge then compute distances
					for (int i = list.length; i-- > 0;)
					{
						if (object.distance2(list[i]) <= e)
						{
							neighbours.add(list[i]);
						}
					}
				}

				index = fastForward[index];
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

		super.findNeighboursAndDistances(minPts, object, e);
	}
}