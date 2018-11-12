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
 * Store molecules in a 2D grid and perform distance computation using cells within the radius from
 * the centre.
 */
class RadialMoleculeSpace extends GridMoleculeSpace {

  /**
   * Hold the point where inner processing starts to use a higher resolution grid.
   */
  static final int N_MOLECULES_FOR_NEXT_RESOLUTION_INNER = 150;
  /**
   * Hold the point where processing starts to use a higher resolution grid.
   */
  static final int N_MOLECULES_FOR_NEXT_RESOLUTION_OUTER = 150;

  /** The offset. */
  CircularKernelOffset[] offset;
  private final boolean useInternal;

  /**
   * Instantiates a new radial molecule space.
   *
   * @param opticsManager the optics manager
   * @param generatingDistanceE the generating distance (E)
   */
  RadialMoleculeSpace(OpticsManager opticsManager, float generatingDistanceE) {
    this(opticsManager, generatingDistanceE, 0);
  }

  /**
   * Instantiates a new radial molecule space.
   *
   * @param opticsManager the optics manager
   * @param generatingDistanceE the generating distance (E)
   * @param resolution the resolution
   */
  RadialMoleculeSpace(OpticsManager opticsManager, float generatingDistanceE, int resolution) {
    super(opticsManager, generatingDistanceE, resolution);
    useInternal = opticsManager.getOptions().contains(OpticsManager.Option.INNER_PROCESSING);
  }

  /**
   * Instantiates a new radial molecule space.
   *
   * @param opticsManager the optics manager
   * @param generatingDistanceE the generating distance (E)
   * @param resolution the resolution
   * @param useInternal the use internal
   */
  RadialMoleculeSpace(OpticsManager opticsManager, float generatingDistanceE, int resolution,
      boolean useInternal) {
    super(opticsManager, generatingDistanceE, resolution);
    this.useInternal = useInternal;
  }

  @Override
  public String toString() {
    return String.format("%s, e=%f, bw=%f, r=%d, in=%b", this.getClass().getSimpleName(),
        generatingDistanceE, binWidth, resolution, useInternal);
  }

  @Override
  Molecule[] generate() {
    // Generate the grid
    final Molecule[] m = super.generate();

    offset = CircularKernelOffset.create(resolution);

    return m;
  }

  @Override
  int determineMaximumResolution(float xrange, float yrange) {
    int resolution = 0;

    // A reasonable upper bound is that:
    // - resolution should be 2 or above (to get the advantage of scanning the region around a point
    // using cells).
    // However we must ensure that we have the memory to create the grid.

    // Q. What is a good maximum limit for the memory allocation?
    while (getBins(xrange, yrange, generatingDistanceE, resolution + 1) < 4096 * 4096
        || resolution < 2) {
      resolution++;
    }
    // We handle a resolution of zero in the calling function
    return resolution;
  }

  @Override
  double getNMoleculesInGeneratingArea(float xrange, float yrange) {
    final double nMoleculesInPixel = (double) size / (xrange * yrange);
    return Math.PI * generatingDistanceE * generatingDistanceE * nMoleculesInPixel;
  }

  @Override
  void adjustMaximumResolution(float xrange, float yrange) {
    // This has been optimised using a simple test to increase the number of molecules in the
    // circle region.

    // If the grid is far too small then many of the lists in each cell will be empty.
    // If the grid is too small then many of the lists in each cell will be empty or contain only 1
    // item.
    // This leads to setting up a for loop through only 1 item.
    // If the grid is too large then the outer cells may contain many points that are too far from
    // the centre, missing the chance to ignore them.

    final double nMoleculesInArea = getNMoleculesInGeneratingArea(xrange, yrange);

    int newResolution;

    if (useInternal) {
      // When using internal processing, we use a different look-up table. This is because
      // there are additional loop constructs that must be maintained and there is a time penalty
      // for this due to complexity.

      if (nMoleculesInArea < N_MOLECULES_FOR_NEXT_RESOLUTION_INNER) {
        newResolution = 2;
      } else if (nMoleculesInArea < 500) {
        newResolution = 3;
      } else if (nMoleculesInArea < 1000) {
        newResolution = 4;
      } else {
        // Above this limit the resolution of the circles is good.
        newResolution = 5;
      }
    } else if (nMoleculesInArea < N_MOLECULES_FOR_NEXT_RESOLUTION_OUTER) {
      newResolution = 2;
    } else if (nMoleculesInArea < 300) {
      newResolution = 3;
    } else if (nMoleculesInArea < 500) {
      newResolution = 4;
    } else {
      // Above this limit the resolution of the circles is good.
      newResolution = 5;
    }

    resolution = Math.min(newResolution, resolution);
  }

  @Override
  void findNeighbours(int minPts, Molecule object, float generatingDistanceE) {

    final int xbin = object.getXBin();
    final int ybin = object.getYBin();

    neighbours.clear();

    // Use a circle mask over the grid to enumerate the correct cells
    // Only compute distances at the edge of the mask

    // Pre-compute range
    final int miny = Math.max(ybin - resolution, 0);
    final int maxy = Math.min(ybin + resolution + 1, ybins);
    final int startRow = Math.max(resolution - ybin, 0);

    if (useInternal) {
      // Internal processing. Any pixel that is internal does not require
      // a distance computation.

      if (xbin + resolution < xbins && xbin - resolution >= 0) {
        // Internal X. Maintain the centre index and use offsets to set the indices
        int centreIndex = getIndex(xbin, miny);

        for (int y = miny, row = startRow; y < maxy; y++, row++, centreIndex += xbins) {
          // Dynamically compute the search strip
          int index = centreIndex + offset[row].start;
          final int endIndex = centreIndex + offset[row].end;

          // Use fast-forward to skip to the next position with data
          if (grid[index] == null) {
            index = fastForward[index];
          }

          if (offset[row].internal) {
            // Speed this up with diffs
            final int startInternal = centreIndex + offset[row].startInternal;
            final int endInternal = centreIndex + offset[row].endInternal;

            while (index < startInternal) {
              final Molecule[] list = grid[index];

              // Build a list of all the neighbours
              // If at the edge then compute distances
              for (int i = list.length; i-- > 0;) {
                if (object.distanceSquared(list[i]) <= generatingDistanceE) {
                  neighbours.add(list[i]);
                }
              }

              index = fastForward[index];
            }
            while (index < endInternal) {
              final Molecule[] list = grid[index];

              // Build a list of all the neighbours
              // If internal just add all the points

              // This uses System.arrayCopy.
              neighbours.add(list);

              index = fastForward[index];
            }
            while (index < endIndex) {
              final Molecule[] list = grid[index];

              // Build a list of all the neighbours
              // If at the edge then compute distances
              for (int i = list.length; i-- > 0;) {
                if (object.distanceSquared(list[i]) <= generatingDistanceE) {
                  neighbours.add(list[i]);
                }
              }

              index = fastForward[index];
            }
          } else {
            while (index < endIndex) {
              final Molecule[] list = grid[index];

              // Build a list of all the neighbours
              // If not internal then compute distances
              for (int i = list.length; i-- > 0;) {
                if (object.distanceSquared(list[i]) <= generatingDistanceE) {
                  neighbours.add(list[i]);
                }
              }

              index = fastForward[index];
            }
          }
        }
      } else {
        // Compute distances
        for (int y = miny, row = startRow; y < maxy; y++, row++) {
          // Dynamically compute the search strip
          int index = getIndex(Math.max(xbin + offset[row].start, 0), y);
          final int endIndex = getIndex(Math.min(xbin + offset[row].end, xbins), y);

          // Use fast-forward to skip to the next position with data
          if (grid[index] == null) {
            index = fastForward[index];
          }

          if (offset[row].internal) {
            // Speed this up with diffs
            final int startInternal = getIndex(xbin + offset[row].startInternal, y);
            final int endInternal = getIndex(Math.min(xbin + offset[row].endInternal, xbins), y);

            while (index < startInternal) {
              final Molecule[] list = grid[index];

              // Build a list of all the neighbours
              // If at the edge then compute distances
              for (int i = list.length; i-- > 0;) {
                if (object.distanceSquared(list[i]) <= generatingDistanceE) {
                  neighbours.add(list[i]);
                }
              }

              index = fastForward[index];
            }
            while (index < endInternal) {
              final Molecule[] list = grid[index];

              // Build a list of all the neighbours
              // If internal just add all the points

              // This uses System.arrayCopy.
              neighbours.add(list);

              index = fastForward[index];
            }
            while (index < endIndex) {
              final Molecule[] list = grid[index];

              // Build a list of all the neighbours
              // If at the edge then compute distances
              for (int i = list.length; i-- > 0;) {
                if (object.distanceSquared(list[i]) <= generatingDistanceE) {
                  neighbours.add(list[i]);
                }
              }

              index = fastForward[index];
            }
          } else {
            while (index < endIndex) {
              final Molecule[] list = grid[index];

              // Build a list of all the neighbours
              // If not internal then compute distances
              for (int i = list.length; i-- > 0;) {
                if (object.distanceSquared(list[i]) <= generatingDistanceE) {
                  neighbours.add(list[i]);
                }
              }

              index = fastForward[index];
            }
          }
        }
      }
    } else if (xbin + resolution < xbins && xbin - resolution >= 0) {
      // Internal X. Maintain the centre index and use offsets to set the indices
      int centreIndex = getIndex(xbin, miny);

      for (int y = miny, row = startRow; y < maxy; y++, row++, centreIndex += xbins) {
        // Dynamically compute the search strip
        int index = centreIndex + offset[row].start;
        final int endIndex = centreIndex + offset[row].end;

        // Use fast-forward to skip to the next position with data
        if (grid[index] == null) {
          index = fastForward[index];
        }

        while (index < endIndex) {
          final Molecule[] list = grid[index];

          // Build a list of all the neighbours
          // If not internal then compute distances
          for (int i = list.length; i-- > 0;) {
            if (object.distanceSquared(list[i]) <= generatingDistanceE) {
              neighbours.add(list[i]);
            }
          }

          index = fastForward[index];
        }
      }
    } else {
      // Compute distances
      for (int y = miny, row = startRow; y < maxy; y++, row++) {
        // Dynamically compute the search strip
        int index = getIndex(Math.max(xbin + offset[row].start, 0), y);
        final int endIndex = getIndex(Math.min(xbin + offset[row].end, xbins), y);

        // Use fast-forward to skip to the next position with data
        if (grid[index] == null) {
          index = fastForward[index];
        }

        while (index < endIndex) {
          final Molecule[] list = grid[index];

          // Build a list of all the neighbours
          // If not internal then compute distances
          for (int i = list.length; i-- > 0;) {
            if (object.distanceSquared(list[i]) <= generatingDistanceE) {
              neighbours.add(list[i]);
            }
          }

          index = fastForward[index];
        }
      }
    }
  }

  @Override
  void findNeighboursAndDistances(int minPts, Molecule object, float generatingDistanceE) {

    // Sweep grid in concentric squares. This is much easier then concentric circles as
    // we can ensure the bounds are checked only once.
    //
    // If we use circles then we could do this if the quarter circle is within bounds
    // by storing a list of index offsets. If the quarter circle intersects the edge of the grid
    // then each position must be checked it is inside the bounds. This means storing the xy offset
    // as well as the direct index. It is a lot of bounds comparisons.
    //
    // To sweep a concentric square ring you do upper and lower edges first. Then column
    // edges with an index 1 inside. This avoids counting corners twice. This probably
    // needs 4 loops as each must be checked if it is inside.
    //
    // We can avoid checks if the max square is inside the grid. If not then we can avoid
    // checks up to the first intersect ring.

    final int xbin = object.getXBin();
    final int ybin = object.getYBin();

    neighbours.clear();

    // Use a circle mask over the grid to enumerate the correct cells
    // Only compute distances at the edge of the mask

    // Pre-compute range
    final int miny = Math.max(ybin - resolution, 0);
    final int maxy = Math.min(ybin + resolution + 1, ybins);
    final int startRow = Math.max(resolution - ybin, 0);

    if (xbin + resolution < xbins && xbin - resolution >= 0) {
      // Internal X. Maintain the centre index and use offsets to set the indices
      int centreIndex = getIndex(xbin, miny);

      for (int y = miny, row = startRow; y < maxy; y++, row++, centreIndex += xbins) {
        // Dynamically compute the search strip
        int index = centreIndex + offset[row].start;
        final int endIndex = centreIndex + offset[row].end;

        // Use fast-forward to skip to the next position with data
        if (grid[index] == null) {
          index = fastForward[index];
        }

        while (index < endIndex) {
          final Molecule[] list = grid[index];

          // Build a list of all the neighbours
          // If not internal then compute distances
          for (int i = list.length; i-- > 0;) {
            final float d = object.distanceSquared(list[i]);
            if (d <= generatingDistanceE) {
              // Build a list of all the neighbours and their working distance
              final Molecule otherObject = list[i];
              otherObject.setD(d);
              neighbours.add(otherObject);
            }
          }

          index = fastForward[index];
        }
      }
    } else {
      // Compute distances
      for (int y = miny, row = startRow; y < maxy; y++, row++) {
        // Dynamically compute the search strip
        int index = getIndex(Math.max(xbin + offset[row].start, 0), y);
        final int endIndex = getIndex(Math.min(xbin + offset[row].end, xbins), y);

        // Use fast-forward to skip to the next position with data
        if (grid[index] == null) {
          index = fastForward[index];
        }

        while (index < endIndex) {
          final Molecule[] list = grid[index];

          // Build a list of all the neighbours
          // If not internal then compute distances
          for (int i = list.length; i-- > 0;) {
            final float d = object.distanceSquared(list[i]);
            if (d <= generatingDistanceE) {
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
