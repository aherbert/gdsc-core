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

package uk.ac.sussex.gdsc.core.clustering.optics;

/**
 * Store molecules in a 2D grid.
 */
class GridMoleculeSpace extends MoleculeSpace {
  /**
   * Used for access to the raw coordinates.
   */
  protected final OpticsManager opticsManager;

  /** The resolution. */
  int resolution;

  /** The bin width. */
  float binWidth;

  /** The x bins. */
  int xbins;

  /** The y bins. */
  int ybins;

  /** The grid. */
  Molecule[][] grid;

  /** The fast forward indices (the index to the next position that contains data). */
  int[] fastForward;

  /**
   * Instantiates a new grid molecule space.
   *
   * @param opticsManager the optics manager
   * @param generatingDistanceE the generating distance (E)
   */
  GridMoleculeSpace(OpticsManager opticsManager, float generatingDistanceE) {
    this(opticsManager, generatingDistanceE, 0);
  }

  /**
   * Instantiates a new grid molecule space.
   *
   * @param opticsManager the optics manager
   * @param generatingDistanceE the generating distance (E)
   * @param resolution the resolution
   */
  GridMoleculeSpace(OpticsManager opticsManager, float generatingDistanceE, int resolution) {
    super(opticsManager.getSize(), generatingDistanceE);

    this.opticsManager = opticsManager;
    this.resolution = resolution;
  }

  @Override
  public String toString() {
    return String.format("%s, e=%f, bw=%f, r=%d", this.getClass().getSimpleName(),
        generatingDistanceE, binWidth, resolution);
  }

  @Override
  Molecule[] generate() {
    final float minXCoord = opticsManager.getMinimumX();
    final float maxXCoord = opticsManager.getMaximumX();
    final float minYCoord = opticsManager.getMinimumY();
    final float maxYCoord = opticsManager.getMaximumY();

    final float xrange = maxXCoord - minXCoord;
    final float yrange = maxYCoord - minYCoord;

    final float[] xcoord = opticsManager.getXData();
    final float[] ycoord = opticsManager.getYData();

    if (xrange == 0 && yrange == 0) {
      resolution = 1;
      binWidth = 1;
    } else if (resolution > 0) {
      // The resolution was input
      binWidth = generatingDistanceE / resolution;
    } else {
      // Use a higher resolution grid to avoid too many distance comparisons
      resolution = determineMaximumResolution(xrange, yrange);

      if (resolution == 0) {
        // Handle a resolution of zero. This will happen when the generating distance is very small.
        // In this instance we can use a resolution of 1 but change the bin width to something
        // larger.
        resolution = 1;
        binWidth = determineBinWidth(xrange, yrange);
      } else {
        adjustResolution(xrange, yrange);
        binWidth = generatingDistanceE / resolution;
      }
    }

    // Assign to a grid
    xbins = 1 + (int) (xrange / binWidth);
    ybins = 1 + (int) (yrange / binWidth);

    // Use a transpose grid to allow freeing memory (as we later process in the y then x order)
    final GridMolecule[][] linkedListGrid = new GridMolecule[ybins][];
    for (int ybin = 0; ybin < ybins; ybin++) {
      linkedListGrid[ybin] = new GridMolecule[xbins];
    }

    setOfObjects = new Molecule[xcoord.length];
    for (int i = 0; i < xcoord.length; i++) {
      final float x = xcoord[i];
      final float y = ycoord[i];
      final int xbin = (int) ((x - minXCoord) / binWidth);
      final int ybin = (int) ((y - minYCoord) / binWidth);
      // Build a single linked list
      final GridMolecule m = new GridMolecule(i, x, y, xbin, ybin, linkedListGrid[ybin][xbin]);
      setOfObjects[i] = m;
      linkedListGrid[ybin][xbin] = m;
    }

    // Convert grid to arrays ..
    grid = new Molecule[xbins * ybins][];
    for (int ybin = 0, index = 0; ybin < ybins; ybin++) {
      for (int xbin = 0; xbin < xbins; xbin++, index++) {
        if (linkedListGrid[ybin][xbin] == null) {
          continue;
        }
        int count = 0;
        for (Molecule m = linkedListGrid[ybin][xbin]; m != null; m = m.getNext()) {
          count++;
        }
        final Molecule[] list = new Molecule[count];
        for (Molecule m = linkedListGrid[ybin][xbin]; m != null; m = m.getNext()) {
          list[--count] = m;
        }
        grid[index] = list;
      }
      // Free memory
      linkedListGrid[ybin] = null;
    }

    // Traverse the grid and store the index to the next position that contains data
    int count = 0;
    int index = grid.length;
    fastForward = new int[index];
    for (int i = index; i-- > 0;) {
      fastForward[i] = index;
      if (grid[i] != null) {
        index = i;
        count += grid[i].length;
      }
    }
    if (count != setOfObjects.length) {
      throw new RuntimeException("Grid does not contain all the objects");
    }

    return setOfObjects;
  }

  /**
   * Determine maximum resolution.
   *
   * @param xrange the xrange
   * @param yrange the yrange
   * @return the resolution
   */
  int determineMaximumResolution(float xrange, float yrange) {
    int newResolution = 0;

    // A reasonable upper bound is that:
    // - resolution should be 2 or above (to get the advantage of scanning the region around a point
    // using cells).
    // - there should be at least one molecule per stripe
    // However we must ensure that we have the memory to create the grid.
    final double nMoleculesInArea = getNMoleculesInGeneratingArea(xrange, yrange);

    // Q. What is a good maximum limit for the memory allocation?
    while (getBins(xrange, yrange, generatingDistanceE, newResolution + 1) < 4096 * 4096
        && (newResolution < 2 || nMoleculesInArea / getNBlocks(newResolution) > 1)) {
      newResolution++;
    }
    // We handle a resolution of zero in the calling function
    return newResolution;
  }

  /**
   * Gets the number of molecules in the generating area.
   *
   * @param xrange the xrange
   * @param yrange the yrange
   * @return the number of molecules
   */
  double getNMoleculesInGeneratingArea(float xrange, float yrange) {
    // We can easily compute the expected number of molecules in a pixel and from that
    // the expected number in a square block of the max distance:
    final double nMoleculesInPixel = (double) size / (xrange * yrange);
    return 4 * generatingDistanceE * generatingDistanceE * nMoleculesInPixel;
  }

  @SuppressWarnings("unused")
  /**
   * Adjust resolution.
   *
   * @param xrange the xrange
   * @param yrange the yrange
   */
  void adjustResolution(final float xrange, final float yrange) {
    // This has been optimised using a simple JUnit test to increase the number of molecules in the
    // square region.

    // If the grid is far too small then many of the lists in each cell will be empty.
    // If the grid is too small then many of the lists in each cell will be empty or contain only 1
    // item.
    // This leads to setting up a for loop through only 1 item.
    // If the grid is too large then the outer cells may contain many points that are too far from
    // the centre, missing the chance to ignore them.

    final int newResolution = 2;

    // We can set the resolution using a simple look-up table.
    // A JUnit test shows there does not appear to be much benefit from higher resolution as the
    // number of distance comparisons is the limiting factor.
    // double nMoleculesInArea = getNMoleculesInGeneratingArea(xrange, yrange);
    // if (nMoleculesInArea < 20)
    // newResolution = 2;
    // else if (nMoleculesInArea < 25)
    // newResolution = 3;
    // else if (nMoleculesInArea < 35)
    // newResolution = 4;
    // else
    // // When there are a lot more molecules then the speed is limited by the all-vs-all
    // comparison, not finding the molecules so this is an upper limit.
    // newResolution = 5;

    resolution = Math.min(newResolution, resolution);
  }

  /**
   * Get the number of distance comparisons.
   *
   * @param molecules the molecules
   * @return the number of distance comparisons
   */
  public static double comparisons(double molecules) {
    if (molecules < 1) {
      return 0;
    }
    return molecules * (molecules - 1) / 2;
  }

  private float determineBinWidth(float xrange, float yrange) {
    float newBinWidth = generatingDistanceE;
    while (getBins(xrange, yrange, newBinWidth, 1) > 100000) {
      // Dumb implementation that doubles the bin width. A better solution
      // would be to conduct a search for the value with a number of bins close
      // to the target.
      newBinWidth *= 2;
    }
    return newBinWidth;
  }

  /**
   * Gets the number of bins.
   *
   * @param xrange the xrange
   * @param yrange the yrange
   * @param distance the distance
   * @param resolution the resolution
   * @return the number of bins
   */
  int getBins(float xrange, float yrange, float distance, int resolution) {
    final float newBinWidth = distance / resolution;
    final int nXBins = 1 + (int) (xrange / newBinWidth);
    final int nYBins = 1 + (int) (yrange / newBinWidth);
    return nXBins * nYBins;
  }

  /**
   * Gets the number of blocks.
   *
   * @param resolution the resolution
   * @return the number of blocks
   */
  int getNBlocks(int resolution) {
    return 2 * resolution + 1;
  }

  /**
   * Gets the number of neighbour blocks.
   *
   * @param resolution the resolution
   * @return the number of neighbour blocks
   */
  int getNeighbourBlocks(int resolution) {
    final int size = getNBlocks(resolution);
    return size * size;
  }

  @Override
  void findNeighbours(int minPts, Molecule object, float generatingDistance) {

    // Match findNeighboursAndDistances(minPts, object, generatingDistance)
    // but do not store the distances

    final int xbin = object.getXBin();
    final int ybin = object.getYBin();

    neighbours.clear();

    // Pre-compute range
    final int minx = Math.max(xbin - resolution, 0);
    final int maxx = Math.min(xbin + resolution + 1, xbins);
    final int diff = maxx - minx;
    final int miny = Math.max(ybin - resolution, 0);
    final int maxy = Math.min(ybin + resolution + 1, ybins);

    // Compute distances
    for (int y = miny; y < maxy; y++) {
      int index = getIndex(minx, y);
      final int endIndex = index + diff;
      if (grid[index] == null) {
        index = fastForward[index];
      }
      while (index < endIndex) {
        final Molecule[] list = grid[index];
        for (int i = list.length; i-- > 0;) {
          if (object.distanceSquared(list[i]) <= generatingDistance) {
            // Build a list of all the neighbours
            neighbours.add(list[i]);
          }
        }
        index = fastForward[index];
      }
    }
  }

  /**
   * Gets the index.
   *
   * @param x the x
   * @param y the y
   * @return the index
   */
  int getIndex(final int x, final int y) {
    return y * xbins + x;
  }

  @Override
  void findNeighboursAndDistances(int minPts, Molecule object, float generatingDistance) {
    final int xbin = object.getXBin();
    final int ybin = object.getYBin();

    neighbours.clear();

    // Pre-compute range
    final int minx = Math.max(xbin - resolution, 0);
    final int maxx = Math.min(xbin + resolution + 1, xbins);
    final int diff = maxx - minx;
    final int miny = Math.max(ybin - resolution, 0);
    final int maxy = Math.min(ybin + resolution + 1, ybins);

    // Count if there are enough neighbours
    int count = minPts;
    counting: for (int y = miny; y < maxy; y++) {
      // Use fast-forward to skip to the next position with data
      int index = getIndex(minx, y);
      final int endIndex = index + diff;
      if (grid[index] == null) {
        index = fastForward[index];
      }
      while (index < endIndex) {
        count -= grid[index].length;
        if (count <= 0) {
          break counting;
        }
        index = fastForward[index];
      }
    }

    if (count > 0) {
      // Not a core point so do not compute distances
      return;
    }

    // Compute distances
    for (int y = miny; y < maxy; y++) {
      // Use fast-forward to skip to the next position with data
      int index = getIndex(minx, y);
      final int endIndex = index + diff;
      if (grid[index] == null) {
        index = fastForward[index];
      }
      while (index < endIndex) {
        final Molecule[] list = grid[index];
        for (int i = list.length; i-- > 0;) {
          final float d = object.distanceSquared(list[i]);
          if (d <= generatingDistance) {
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
