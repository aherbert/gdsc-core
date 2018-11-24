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

package uk.ac.sussex.gdsc.core.clustering;

import uk.ac.sussex.gdsc.core.utils.ValidationUtils;

import org.apache.commons.math3.util.FastMath;

import java.awt.Rectangle;

/**
 * Calculate the density of localisations around a given position using a square block of specified
 * width.
 */
public class DensityManager extends CoordinateStore {

  /**
   * Represent a molecule in 2D space.
   */
  private static class Molecule {
    /** The id. */
    int id;
    /** The x. */
    float x;
    /** The y. */
    float y;

    /** Used to construct a single linked list of molecules. */
    Molecule next;

    Molecule(int id, float x, float y, Molecule next) {
      this.id = id;
      this.x = x;
      this.y = y;
      this.next = next;
    }

    float distance2(Molecule other) {
      final float dx = x - other.x;
      final float dy = y - other.y;
      return dx * dx + dy * dy;
    }
  }

  /**
   * Create a new instance.
   *
   * <p>Input arrays are modified.
   *
   * @param xcoord the x coordinates
   * @param ycoord the y coordinates
   * @param bounds the bounds
   * @throws IllegalArgumentException if results are null or empty
   */
  public DensityManager(float[] xcoord, float[] ycoord, Rectangle bounds) {
    super(xcoord, ycoord, bounds);
  }

  /**
   * Calculate the density for the results.
   *
   * <p>A square block is used around each result of the specified radius. The results are assigned
   * to a grid using a cell size of radius / resolution. The totals of each cell are then counted
   * for the range +/- radius around each result.
   *
   * <p>If the block overlaps the border of the image the density will suffer from under-counting.
   * The value can be optionally scaled using the fraction of the overlap area.
   *
   * <p>Note that the score is the number of molecules surrounding the given molecule, so the
   * molecule itself is not counted.
   *
   * @param radius the radius
   * @param resolution the resolution
   * @param adjustForBorder the adjust for border
   * @return the density
   */
  public int[] calculateSquareDensity(float radius, int resolution, boolean adjustForBorder) {
    checkRadius(radius);
    ValidationUtils.checkArgument(resolution > 0, "Resolution must be strictly positive");

    final float cellSize = radius / resolution;

    final int maxx = (int) (maxXCoord / cellSize) + 1;
    final int maxy = (int) (maxYCoord / cellSize) + 1;

    // Allocate counts to the cells
    final int[] data = new int[maxx * maxy];
    for (int i = 0; i < xcoord.length; i++) {
      final int x = (int) (xcoord[i] / cellSize);
      final int y = (int) (ycoord[i] / cellSize);
      data[y * maxx + x]++;
    }

    // Create rolling sum table. Re-use the storage
    // First row
    int columnSum = 0; // Column sum
    for (int i = 0; i < maxx; i++) {
      columnSum += data[i];
      data[i] = columnSum;
    }

    // Remaining rows:
    // sum = rolling sum of row + sum of row above
    for (int y = 1; y < maxy; y++) {
      int index = y * maxx;
      columnSum = 0;

      // Remaining columns
      for (int x = 0; x < maxx; x++, index++) {
        columnSum += data[index];
        data[index] = data[index - maxx] + columnSum;
      }
    }

    // For each localisation, compute the sum of counts within a square box radius
    final float area = (float) (4.0 * resolution * resolution);
    final int[] density = new int[xcoord.length];
    for (int i = 0; i < xcoord.length; i++) {
      final int u = (int) (xcoord[i] / cellSize);
      final int v = (int) (ycoord[i] / cellSize);

      // Note: Subtract 1 to discount the current localisation. Should this be done?
      int sum = -1;

      // Get the bounds
      int minU = u - resolution - 1;
      final int maxU = FastMath.min(u + resolution, maxx - 1);
      int minV = v - resolution - 1;
      final int maxV = FastMath.min(v + resolution, maxy - 1);

      // Compute sum from rolling sum using:
      // sum(u,v) =
      // + s(maxU,maxV)
      // - s(minU,maxV)
      // - s(maxU,minV)
      // + s(minU,minV)
      // Note:
      // s(u,v) = 0 when either u,v < 0
      // s(u,v) = s(umax,v) when u>umax
      // s(u,v) = s(u,vmax) when v>vmax
      // s(u,v) = s(umax,vmax) when u>umax,v>vmax

      // + s(maxU,maxV)
      int index = maxV * maxx + maxU;
      sum += data[index];

      boolean clipped = false;
      if (minU >= 0) {
        // - s(minU,maxV)
        index = maxV * maxx + minU;
        sum -= data[index];
      } else {
        clipped = true;
        minU = -1;
      }
      if (minV >= 0) {
        // - s(maxU,minV)
        index = minV * maxx + maxU;
        sum -= data[index];

        if (minU >= 0) {
          // + s(minU,minV)
          index = minV * maxx + minU;
          sum += data[index];
        }
      } else {
        clipped = true;
        minV = -1;
      }

      // Adjust for area
      if (adjustForBorder && clipped) {
        sum *= area / ((maxU - minU - 1) * (maxV - minV - 1));
      }

      density[i] = sum;
    }

    return density;
  }

  /**
   * Calculate the local density for the results using square blocks of the specified radius. The
   * returned array is equal in size to the number of blocks. The score is the number of molecules
   * within the 3x3 region surrounding each block.
   *
   * @param radius the radius
   * @return the block density array
   */
  public int[] calculateBlockDensity(final float radius) {
    checkRadius(radius);

    // Note: We do not subtract min from the value for speed:
    // final int maxx = (int) ((maxXCoord-minXCoord) / radius) + 1
    // minXCoord will be in the range 0-1 after initialisation.

    final int maxx = (int) (maxXCoord / radius) + 1;
    final int maxy = (int) (maxYCoord / radius) + 1;

    // Allocate counts to the cells
    final int[] data = new int[maxx * maxy];
    for (int i = 0; i < xcoord.length; i++) {
      final int x = (int) (xcoord[i] / radius);
      final int y = (int) (ycoord[i] / radius);
      data[y * maxx + x]++;
    }

    // Create rolling sum table. Re-use the storage
    // First row
    int columnSum = 0; // Column sum
    for (int i = 0; i < maxx; i++) {
      columnSum += data[i];
      data[i] = columnSum;
    }

    // Remaining rows:
    // sum = rolling sum of row + sum of row above
    for (int y = 1; y < maxy; y++) {
      int index = y * maxx;
      columnSum = 0;

      // Remaining columns
      for (int x = 0; x < maxx; x++, index++) {
        columnSum += data[index];
        data[index] = data[index - maxx] + columnSum;
      }
    }

    // Pre-compute U bounds
    final int[] minU = new int[maxx];
    final int[] maxU = new int[maxx];
    final boolean[] minUok = new boolean[maxx];
    for (int u = maxx; u-- > 0;) {
      minU[u] = u - 2;
      maxU[u] = FastMath.min(u + 1, maxx - 1);
      minUok[u] = u >= 2;
    }

    // For each block, compute the sum of counts within a 3x3 box radius
    final int[] density = new int[data.length];
    for (int v = maxy; v-- > 0;) {
      final int minV = v - 2;
      final int maxV = FastMath.min(v + 1, maxy - 1);
      final boolean minVok = (minV >= 0);
      final int lowerIndex = minV * maxx;

      for (int u = maxx; u-- > 0;) {
        // Compute sum from rolling sum using:
        // sum(u,v) =
        // + s(maxU,maxV)
        // - s(minU,maxV)
        // - s(maxU,minV)
        // + s(minU,minV)
        // Note:
        // s(u,v) = 0 when either u,v < 0
        // s(u,v) = s(umax,v) when u>umax
        // s(u,v) = s(u,vmax) when v>vmax
        // s(u,v) = s(umax,vmax) when u>umax,v>vmax

        // + s(maxU,maxV)
        final int upperIndex = maxV * maxx;
        int sum = data[upperIndex + maxU[u]];

        if (minUok[u]) {
          // - s(minU,maxV)
          sum -= data[upperIndex + minU[u]];
        }
        if (minVok) {
          // - s(maxU,minV)
          sum -= data[lowerIndex + maxU[u]];

          if (minUok[u]) {
            // + s(minU,minV)
            sum += data[lowerIndex + minU[u]];
          }
        }

        density[v * maxx + u] = sum;
      }
    }

    return density;
  }

  /**
   * Calculate the local density for the results using square blocks of the specified radius. The
   * returned array is equal in size to the number of blocks. The score is the number of molecules
   * within the 3x3 region surrounding each block.
   *
   * @param radius the radius
   * @return the block density array
   */
  public int[] calculateBlockDensity2(final float radius) {
    final float maxx = maxXCoord;
    final float maxy = maxYCoord;

    // Assign to a grid
    final float binWidth = radius;
    final int xbins = 1 + (int) ((maxx) / binWidth);
    final int ybins = 1 + (int) ((maxy) / binWidth);
    final int[][] grid = new int[xbins][ybins];
    for (int i = 0; i < xcoord.length; i++) {
      final int xbin = (int) ((xcoord[i]) / binWidth);
      final int ybin = (int) ((ycoord[i]) / binWidth);
      grid[xbin][ybin]++;
    }

    final int[] density = new int[xbins * ybins];
    boolean withinY = false;
    for (int ybin = ybins; ybin-- > 0; withinY = true) {
      boolean withinX = false;
      for (int xbin = xbins; xbin-- > 0; withinX = true) {
        final int i = ybin * xbins + xbin;
        final int iCount = grid[xbin][ybin];
        density[i] += iCount;

        // Compare up to a maximum of 4 neighbours
        //@formatter:off
        //      | 0,0 | 1,0
        // ------------+-----
        // -1,1 | 0,1 | 1,1
        //@formatter:on

        if (withinY) {
          add(density, grid, xbins, i, iCount, xbin, ybin + 1);
          if (xbin > 0) {
            add(density, grid, xbins, i, iCount, xbin - 1, ybin + 1);
          }

          if (withinX) {
            add(density, grid, xbins, i, iCount, xbin + 1, ybin);
            add(density, grid, xbins, i, iCount, xbin + 1, ybin + 1);
          }
        } else if (withinX) {
          add(density, grid, xbins, i, iCount, xbin + 1, ybin);
        }
      }
    }

    return density;
  }

  private static void add(final int[] density, final int[][] grid, final int xbins, final int index,
      final int indexCount, final int xbin, final int ybin) {
    density[index] += grid[xbin][ybin];
    density[ybin * xbins + xbin] += indexCount;
  }

  /**
   * Calculate the local density for the results using square blocks of the specified radius. The
   * returned array is equal in size to the number of blocks. The score is the number of molecules
   * within the 3x3 region surrounding each block.
   *
   * @param radius the radius
   * @return the block density array
   */
  public int[] calculateBlockDensity3(final float radius) {
    final float maxx = maxXCoord;
    final float maxy = maxYCoord;

    // Assign to a grid
    final float binWidth = radius;
    final int xbins = 1 + (int) ((maxx) / binWidth);
    final int ybins = 1 + (int) ((maxy) / binWidth);
    final int[][] grid = new int[xbins][ybins];
    for (int i = 0; i < xcoord.length; i++) {
      final int xbin = (int) ((xcoord[i]) / binWidth);
      final int ybin = (int) ((ycoord[i]) / binWidth);
      grid[xbin][ybin]++;
    }

    // Simple sweep
    final int[] density = new int[xbins * ybins];
    for (int ybin = 0; ybin < ybins; ybin++) {
      for (int xbin = 0; xbin < xbins; xbin++) {
        int sum = 0;
        for (int y = -1; y <= 1; y++) {
          final int yBin2 = ybin + y;
          if (yBin2 < 0 || yBin2 >= ybins) {
            continue;
          }
          for (int x = -1; x <= 1; x++) {
            final int xBin2 = xbin + x;
            if (xBin2 < 0 || xBin2 >= ybins) {
              continue;
            }
            sum += grid[xBin2][yBin2];
          }
        }
        density[ybin * xbins + xbin] = sum;
      }
    }

    return density;
  }

  /**
   * Calculate the density for the results.
   *
   * <p>A circle is used around each result of the specified radius and the number of neighbours
   * counted for each result.
   *
   * <p>If the block overlaps the border of the image the density will suffer from under-counting.
   * The value can be optionally scaled using the fraction of the overlap area.
   *
   * <p>Note that the score is the number of molecules surrounding the given molecule, so the
   * molecule itself is not counted.
   *
   * @param radius the radius
   * @param adjustForBorder Set to true to adjust for border
   * @return the density
   */
  public int[] calculateDensity(float radius, boolean adjustForBorder) {
    checkRadius(radius);

    // For each localisation, compute the sum of counts within a circle radius
    // TODO - Determine the optimum parameters to switch to using the grid method.
    final int[] density =
        (xcoord.length < 200) ? calculateDensityTriangle(radius) : calculateDensityGrid(radius);

    // Adjust for area
    if (adjustForBorder) {
      // Boundary
      final float upperX = maxXCoord - radius;
      final float upperY = maxYCoord - radius;

      for (int i = 0; i < xcoord.length; i++) {
        int sum = density[i];
        final float x = xcoord[i];
        final float y = ycoord[i];

        // Calculate the area of the circle that has been missed
        // http://stackoverflow.com/questions/622287/area-of-intersection-between-circle-and-rectangle
        // Assume: Circle centre will be within the rectangle

        //@formatter:off
        //
        //   S1       S2       S3
        //
        //        |        |
        //    A1  |________|   A3      SA
        //        /        \
        //       /|   A2   |\
        // -----/-|--------|-\-----
        //     |  |        |  |
        //     |B1|   B2   |B3|        SB
        //     |  |        |  |
        // -----\-|--------|-/-----
        //       \|   C2   |/   C3     SC
        //   C1   \________/
        //        |        |
        //
        //@formatter:on

        // Note: A1,A3,C1,C3 are inside the circle
        // S1 = Slice 1, SA = Slice A, etc

        // Calculate if the upper/lower boundary of the rectangle slices the circle
        // -- Calculate the slice area using the formula for a segment
        // -- Check if the second boundary is slices the circle (i.e. a vertex is inside the circle)
        // ---- Calculate the corner section area to subtract from the overlapping slices
        // Missed = S1 + S3 + SA + SC - A1 - A3 - C1 - C3
        double s1 = 0;
        // s3 == 0
        double sa = 0;
        double sc = 0;
        double a1 = 0;
        double a3 = 0;
        double c1 = 0;
        double c3 = 0;

        // Note all coords are shifted the origin so simply compare the radius and the
        // max bounds minus the radius

        if (x < radius) {
          s1 = getSegmentArea(radius, radius - x);
          if (y < radius) {
            a1 = getCornerArea(radius, x, y);
          }
          if (y > upperY) {
            c1 = getCornerArea(radius, x, maxYCoord - y);
          }
        }
        if (x > upperX) {
          final float dx = maxXCoord - x;
          s1 = getSegmentArea(radius, radius - dx);
          if (y < radius) {
            a3 = getCornerArea(radius, dx, y);
          }
          if (y > upperY) {
            c3 = getCornerArea(radius, dx, maxYCoord - y);
          }
        }
        if (y < radius) {
          sa = getSegmentArea(radius, radius - y);
        }
        if (y > upperY) {
          final float dy = maxYCoord - y;
          sc = getSegmentArea(radius, radius - dy);
        }

        final double missed = s1 + sa + sc - a1 - a3 - c1 - c3;
        if (missed > 0) {
          final double adjustment = area / (area - missed);
          sum *= adjustment;
        }

        density[i] = sum;
      }
    }

    return density;
  }

  /**
   * Calculate the density for the results using an all-vs-all analysis.
   *
   * <p>A circle is used around each result of the specified radius and the number of neighbours
   * counted for each result.
   *
   * <p>If the block overlaps the border of the image the density will suffer from under-counting.
   *
   * <p>Note that the score is the number of molecules surrounding the given molecule, so the
   * molecule itself is not counted.
   *
   * @param radius the radius
   * @return the density
   */
  public int[] calculateDensity(float radius) {
    final float r2 = radius * radius;
    final int[] density = new int[xcoord.length];
    for (int i = 0; i < xcoord.length; i++) {
      int sum = density[i];
      final float x = xcoord[i];
      final float y = ycoord[i];
      for (int j = 0; j < xcoord.length; j++) {
        if (i == j) {
          continue;
        }
        final float dx = x - xcoord[j];
        final float dy = y - ycoord[j];
        if (dx * dx + dy * dy < r2) {
          sum++;
        }
      }

      density[i] = sum;
    }
    return density;
  }

  /**
   * Calculate the density for the results using an all-vs-all analysis in the lower triangle of
   * comparisons.
   *
   * <p>A circle is used around each result of the specified radius and the number of neighbours
   * counted for each result.
   *
   * <p>If the block overlaps the border of the image the density will suffer from under-counting.
   *
   * <p>Note that the score is the number of molecules surrounding the given molecule, so the
   * molecule itself is not counted.
   *
   * @param radius the radius
   * @return the density
   */
  public int[] calculateDensityTriangle(float radius) {
    final float r2 = radius * radius;
    final int[] density = new int[xcoord.length];
    for (int i = 0; i < xcoord.length; i++) {
      int sum = density[i];
      final float x = xcoord[i];
      final float y = ycoord[i];
      for (int j = i + 1; j < xcoord.length; j++) {
        final float dx = x - xcoord[j];
        final float dy = y - ycoord[j];
        if (dx * dx + dy * dy < r2) {
          sum++;
          density[j]++;
        }
      }

      density[i] = sum;
    }
    return density;
  }

  /**
   * Calculate the density for the results using a nearest neighbour cell grid analysis.
   *
   * <p>A circle is used around each result of the specified radius and the number of neighbours
   * counted for each result.
   *
   * <p>If the block overlaps the border of the image the density will suffer from under-counting.
   *
   * <p>Note that the score is the number of molecules surrounding the given molecule, so the
   * molecule itself is not counted.
   *
   * @param radius the radius
   * @return the density
   */
  public int[] calculateDensityGrid(float radius) {
    final int[] density = new int[xcoord.length];

    final float minx = minXCoord;
    final float miny = minYCoord;
    final float maxx = maxXCoord;
    final float maxy = maxYCoord;

    // Assign to a grid
    final float binWidth = radius * 1.01f;
    final int xbins = 1 + (int) ((maxx - minx) / binWidth);
    final int ybins = 1 + (int) ((maxy - miny) / binWidth);
    final Molecule[][] grid = new Molecule[xbins][ybins];
    for (int i = 0; i < xcoord.length; i++) {
      final float x = xcoord[i];
      final float y = ycoord[i];
      final int xbin = (int) ((x - minx) / binWidth);
      final int ybin = (int) ((y - miny) / binWidth);
      // Build a single linked list
      grid[xbin][ybin] = new Molecule(i, x, y, grid[xbin][ybin]);
    }

    final Molecule[] neighbours = new Molecule[5];
    final float radius2 = radius * radius;
    for (int ybin = 0; ybin < ybins; ybin++) {
      for (int xbin = 0; xbin < xbins; xbin++) {
        if (grid[xbin][ybin] == null) {
          continue;
        }

        // Build a list of which cells to compare up to a maximum of 4
        //@formatter:off
        //      | 0,0  |  1,0
        // -----+------+-----
        // -1,1 | 0,1  |  1,1
        //@formatter:on

        int count = 1;

        if (ybin < ybins - 1) {
          neighbours[count++] = grid[xbin][ybin + 1];
          if (xbin > 0) {
            neighbours[count++] = grid[xbin - 1][ybin + 1];
          }
        }
        if (xbin < xbins - 1) {
          neighbours[count++] = grid[xbin + 1][ybin];
          if (ybin < ybins - 1) {
            neighbours[count++] = grid[xbin + 1][ybin + 1];
          }
        }

        for (Molecule m1 = grid[xbin][ybin]; m1 != null; m1 = m1.next) {
          neighbours[0] = m1.next;

          // Compare to neighbours
          for (int c = count; c-- > 0;) {
            for (Molecule m2 = neighbours[c]; m2 != null; m2 = m2.next) {
              if (m1.distance2(m2) < radius2) {
                density[m1.id]++;
                density[m2.id]++;
              }
            }
          }
        }
      }
    }

    return density;
  }

  /**
   * Calculate the area of circular segment, a portion of a disk whose upper boundary is a
   * (circular) arc and whose lower boundary is a chord making a central angle of theta radians.
   *
   * @param radius the radius of the circle
   * @param height the height of the arced portion
   * @return The area
   * @see <a href="http://mathworld.wolfram.com/CircularSegment.html">Circular Segment</a>
   */
  private static double getSegmentArea(double radius, double height) {
    return radius * radius * Math.acos((radius - height) / radius)
        - (radius - height) * Math.sqrt(2 * radius * height - height * height);
  }

  /**
   * Get the area taken by a corner of a rectangle within a circle of radius radius.
   *
   * @param radius the radius of the circle
   * @param x The corner X position
   * @param y The corner Y position
   * @return The area
   */
  private static double getCornerArea(double radius, double x, double y) {
    // 1 vertex is inside the circle: The sum of the areas of a circular segment and a triangle.

    //@formatter:off
    //
    //                            (x,y)
    //      XXXXX                   XXXXXXXXX p2
    //     X     X       Triangle ->X     _-X
    //    X       X                 X   _-  X
    //    X    +--X--+              X _-   X <- Circular segment
    //     X   | X   |              X-   XX
    //      XXXXX    |              XXXXX
    //         |     |             p1
    //
    //@formatter:on

    // Assume: circle at origin, x & y are positive, x^2 + y^2 < radius^2

    // Get the point p1 & p2
    final double x1 = x;
    final double y1 = otherSide(x, radius);
    final double x2 = otherSide(y, radius);
    final double y2 = y;

    // Calculate half the length of the chord cutting the circle between p1 & p2
    final double dx = x2 - x1;
    final double dy = y2 - y1;
    final double halfChord = Math.sqrt(dx * dx + dy * dy);

    // Calculate the height of the arced portion
    final double height = radius - otherSide(halfChord, radius);

    // Get the area as the circular segment plus the triangle
    return getSegmentArea(radius, height) + 0.5 * dx * dy;
  }

  /**
   * Returns a from a right angle triangle where a^2 + b^2 = c^2.
   *
   * @param lengthB the b
   * @param lengthC the c
   * @return a
   */
  private static double otherSide(double lengthB, double lengthC) {
    return Math.sqrt(lengthC * lengthC - lengthB * lengthB);
  }

  /**
   * Compute Ripley's K-function.
   *
   * @param density The density score for each particle
   * @param radius The radius at which the density was computed
   * @return The K-function score
   * @see #calculateDensity(float, boolean)
   * @see #calculateSquareDensity(float, int, boolean)
   * @see <a
   *      href="http://en.wikipedia.org/wiki/Spatial_descriptive_statistics#Ripley.27s_K_and_L_functions">Ripleys
   *      K and L functions</a>
   */
  public double ripleysKFunction(int[] density, double radius) {
    checkRadius(radius);
    if (density.length != xcoord.length) {
      throw new IllegalArgumentException(
          "Input density array must match the number of coordinates");
    }

    // Count the number of points within the distance
    int sum = 0;
    for (final int d : density) {
      sum += d;
    }

    // Normalise
    final double scale = area / ((double) density.length * (double) density.length);
    return sum * scale;
  }


  /**
   * Compute Ripley's K-function.
   *
   * @param radius The radius
   * @return The K-function score
   * @see <a
   *      href="http://en.wikipedia.org/wiki/Spatial_descriptive_statistics#Ripley.27s_K_and_L_functions">Ripleys
   *      K and L functions</a>
   */
  public double ripleysKFunction(double radius) {
    checkRadius(radius);

    // Count the number of points within the distance
    final int sum = calculateSumGrid((float) radius);

    // Normalise
    final double scale = area / ((double) xcoord.length * (double) xcoord.length);
    return sum * scale;
  }

  /**
   * Calculate the number of pairs within the given radius.
   *
   * <p>The sum is over {@code i<n, j<n, i!=j}.
   *
   * @param radius the radius
   * @return the pairs
   */
  public int calculateSum(float radius) {
    final float r2 = radius * radius;
    int sum = 0;
    for (int i = 0; i < xcoord.length; i++) {
      final float x = xcoord[i];
      final float y = ycoord[i];
      for (int j = i + 1; j < xcoord.length; j++) {
        final float dx = x - xcoord[j];
        final float dy = y - ycoord[j];
        if (dx * dx + dy * dy < r2) {
          sum++;
        }
      }
    }

    // Note that the sum should be computed over:
    // i<n, j<n, i!=j
    // Thus it should be doubled to account for j iterating from zero.
    return sum * 2;
  }

  /**
   * Calculate the number of pairs within the given radius using a nearest neighbour cell grid
   * analysis.
   *
   * <p>The sum is over {@code i<n, j<n, i!=j}.
   *
   * @param radius the radius
   * @return the pairs
   */
  public int calculateSumGrid(float radius) {
    int sum = 0;

    final float minx = minXCoord;
    final float miny = minYCoord;
    final float maxx = maxXCoord;
    final float maxy = maxYCoord;

    // Assign to a grid
    final float binWidth = radius * 1.01f;
    final int xbins = 1 + (int) ((maxx - minx) / binWidth);
    final int ybins = 1 + (int) ((maxy - miny) / binWidth);
    final Molecule[][] grid = new Molecule[xbins][ybins];
    for (int i = 0; i < xcoord.length; i++) {
      final float x = xcoord[i];
      final float y = ycoord[i];
      final int xbin = (int) ((x - minx) / binWidth);
      final int ybin = (int) ((y - miny) / binWidth);
      // Build a single linked list
      grid[xbin][ybin] = new Molecule(i, x, y, grid[xbin][ybin]);
    }

    final Molecule[] neighbours = new Molecule[5];
    final float radius2 = radius * radius;
    for (int ybin = 0; ybin < ybins; ybin++) {
      for (int xbin = 0; xbin < xbins; xbin++) {
        if (grid[xbin][ybin] == null) {
          continue;
        }

        // Build a list of which cells to compare up to a maximum of 4
        //@formatter:off
        //      | 0,0 | 1,0
        // ------------+-----
        // -1,1 | 0,1 | 1,1
        //@formatter:on

        int count = 1;

        if (ybin < ybins - 1) {
          neighbours[count++] = grid[xbin][ybin + 1];
          if (xbin > 0) {
            neighbours[count++] = grid[xbin - 1][ybin + 1];
          }
        }
        if (xbin < xbins - 1) {
          neighbours[count++] = grid[xbin + 1][ybin];
          if (ybin < ybins - 1) {
            neighbours[count++] = grid[xbin + 1][ybin + 1];
          }
        }

        for (Molecule m1 = grid[xbin][ybin]; m1 != null; m1 = m1.next) {
          neighbours[0] = m1.next;

          // Compare to neighbours
          for (int c = count; c-- > 0;) {
            for (Molecule m2 = neighbours[c]; m2 != null; m2 = m2.next) {
              if (m1.distance2(m2) < radius2) {
                sum++;
              }
            }
          }
        }
      }
    }

    return sum * 2;
  }

  /**
   * Compute Ripley's L-function.
   *
   * @param radius The radius
   * @return The L-function score
   * @see <a
   *      href="http://en.wikipedia.org/wiki/Spatial_descriptive_statistics#Ripley.27s_K_and_L_functions">Ripleys
   *      K and L functions</a>
   */
  public double ripleysLFunction(double radius) {
    final double k = ripleysKFunction(radius);
    return Math.sqrt(k / Math.PI);
  }

  /**
   * Compute Ripley's L-function.
   *
   * @param density The density score for each particle
   * @param radius The radius at which the density was computed
   * @return The K-function score
   * @see #calculateDensity(float, boolean)
   * @see #calculateSquareDensity(float, int, boolean)
   * @see <a
   *      href="http://en.wikipedia.org/wiki/Spatial_descriptive_statistics#Ripley.27s_K_and_L_functions">Ripleys
   *      K and L functions</a>
   */
  public double ripleysLFunction(int[] density, double radius) {
    final double k = ripleysKFunction(density, radius);
    return Math.sqrt(k / Math.PI);
  }

  /**
   * Check the radius is strictly positive.
   *
   * @param radius the radius
   */
  private static void checkRadius(double radius) {
    ValidationUtils.checkArgument(radius > 0, "Radius must be strictly positive");
  }
}
