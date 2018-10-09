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
package uk.ac.sussex.gdsc.core.filters;

import uk.ac.sussex.gdsc.core.utils.FixedIntList;

import org.apache.commons.math3.util.FastMath;

/**
 * Computes the local maxima.
 */
public class NonMaximumSuppression implements Cloneable {
  private static final float floatMin = Float.NEGATIVE_INFINITY;
  private static final int intMin = Integer.MIN_VALUE;

  private boolean neighbourCheck = false;
  private boolean dataBuffer = true;

  private float[] newDataFloat = null;
  private int[] newDataInt = null;
  private FixedIntList resultsBuffer = null;
  private boolean[] maximaFlagBuffer = null;

  /**
   * Compute the local-maxima within a 2n+1 block
   *
   * @param data The input data (packed in YX order)
   * @param maxx The width of the data
   * @param maxy The height of the data
   * @param n The block size
   * @return Indices to the local maxima (index = maxx * y + x)
   */
  public int[] maxFind(float[] data, int maxx, int maxy, int n) {
    final FixedIntList results = getResultsBuffer(data.length);
    final boolean[] maximaFlag = getFlagBuffer(data.length);

    // Boundary control
    final int xwidth = FastMath.min(n, maxx - 1);
    final int ywidth = FastMath.min(n, maxy - 1);
    final int xlimit = maxx - xwidth;
    final int ylimit = maxy - ywidth;

    final int[] offset = new int[(2 * xwidth + 1) * (2 * ywidth + 1) - 1];
    final int[] xoffset = new int[offset.length];
    final int[] yoffset = new int[offset.length];
    int d = 0;
    for (int y = -ywidth; y <= ywidth; y++) {
      for (int x = -xwidth; x <= xwidth; x++) {
        if (x != 0 || y != 0) {
          offset[d] = maxx * y + x;
          xoffset[d] = x;
          yoffset[d] = y;
          d++;
        }
      }
    }

    // Compare all points
    int index = 0;
    for (int y = 0; y < maxy; y++) {
      FIND_MAXIMUM: for (int x = 0; x < maxx; x++, index++) {
        final float v = data[index];

        // Flag to indicate this pixels has a complete (2n+1) neighbourhood
        final boolean isInnerXY = (y >= ywidth && y < ylimit) && (x >= xwidth && x < xlimit);

        // Sweep neighbourhood
        if (isInnerXY) {
          for (int i = 0; i < offset.length; i++) {
            if (maximaFlag[index + offset[i]]) {
              continue FIND_MAXIMUM;
            }
            if (data[index + offset[i]] > v) {
              continue FIND_MAXIMUM;
            }
          }
        } else {
          for (d = offset.length; d-- > 0;) {
            // Get the coords and check if it is within the data
            final int yy = y + yoffset[d];
            final int xx = x + xoffset[d];
            final boolean isWithin = (yy >= 0 && yy < maxy) && (xx >= 0 && xx < maxx);

            if (isWithin) {
              if (maximaFlag[index + offset[d]]) {
                continue FIND_MAXIMUM;
              }
              if (data[index + offset[d]] > v) {
                continue FIND_MAXIMUM;
              }
            }
          }
        }

        results.add(index);
        maximaFlag[index] = true;
      } // end FIND_MAXIMA
    }

    return results.toArray();
  }

  /**
   * Compute the local-maxima within a 2n+1 block An inner boundary of N is ignored as potential
   * maxima.
   *
   * @param data The input data (packed in YX order)
   * @param maxx The width of the data
   * @param maxy The height of the data
   * @param n The block size
   * @return Indices to the local maxima (index = maxx * y + x)
   */
  public int[] maxFindInternal(float[] data, int maxx, int maxy, int n) {
    final FixedIntList results = getResultsBuffer(data.length);
    final boolean[] maximaFlag = getFlagBuffer(data.length);

    // Boundary control
    final int xwidth = FastMath.min(n, maxx - 1);
    final int ywidth = FastMath.min(n, maxy - 1);

    final int[] offset = new int[(2 * xwidth + 1) * (2 * ywidth + 1) - 1];
    int d = 0;
    for (int y = -ywidth; y <= ywidth; y++) {
      for (int x = -xwidth; x <= xwidth; x++) {
        if (x != 0 || y != 0) {
          offset[d] = maxx * y + x;
          d++;
        }
      }
    }

    // Compare all points
    for (int y = n; y < maxy - n; y++) {
      int index = y * maxx + n;
      FIND_MAXIMUM: for (int x = n; x < maxx - n; x++, index++) {
        final float v = data[index];

        // Sweep neighbourhood -
        // No check for boundaries as this should be an internal sweep.
        for (int i = 0; i < offset.length; i++) {
          if (maximaFlag[index + offset[i]]) {
            continue FIND_MAXIMUM;
          }
          if (data[index + offset[i]] > v) {
            continue FIND_MAXIMUM;
          }
        }

        results.add(index);
        maximaFlag[index] = true;
      } // end FIND_MAXIMA
    }

    return results.toArray();
  }

  /**
   * Compute the local-maxima within a 2n+1 block An inner boundary is ignored as potential maxima.
   *
   * @param data The input data (packed in YX order)
   * @param maxx The width of the data
   * @param maxy The height of the data
   * @param n The block size
   * @param border The internal border
   * @return Indices to the local maxima (index = maxx * y + x)
   */
  public int[] maxFindInternal(float[] data, int maxx, int maxy, int n, int border) {
    if (n == border) {
      // Faster algorithm as there is no requirement for bounds checking.
      return maxFindInternal(data, maxx, maxy, n);
    }

    final FixedIntList results = getResultsBuffer(data.length);
    final boolean[] maximaFlag = getFlagBuffer(data.length);

    // Boundary control
    final int xwidth = FastMath.min(n, maxx - 1);
    final int ywidth = FastMath.min(n, maxy - 1);
    final int xlimit = maxx - xwidth;
    final int ylimit = maxy - ywidth;

    final int[] offset = new int[(2 * xwidth + 1) * (2 * ywidth + 1) - 1];
    final int[] xoffset = new int[offset.length];
    final int[] yoffset = new int[offset.length];
    int d = 0;
    for (int y = -ywidth; y <= ywidth; y++) {
      for (int x = -xwidth; x <= xwidth; x++) {
        if (x != 0 || y != 0) {
          offset[d] = maxx * y + x;
          xoffset[d] = x;
          yoffset[d] = y;
          d++;
        }
      }
    }

    // All blocks fit within the border
    final boolean inner = (n < border);

    // Compare all points
    for (int y = border; y < maxy - border; y++) {
      int index = y * maxx + border;
      FIND_MAXIMUM: for (int x = border; x < maxx - border; x++, index++) {
        final float v = data[index];

        if (inner) {
          for (int i = 0; i < offset.length; i++) {
            if (maximaFlag[index + offset[i]]) {
              continue FIND_MAXIMUM;
            }
            if (data[index + offset[i]] > v) {
              continue FIND_MAXIMUM;
            }
          }
        } else {
          // Flag to indicate this pixels has a complete (2n+1) neighbourhood
          final boolean isInnerXY = (y >= ywidth && y < ylimit) && (x >= xwidth && x < xlimit);

          // Sweep neighbourhood
          for (d = offset.length; d-- > 0;) {
            boolean isWithin = isInnerXY;
            if (!isWithin) {
              // Get the coords and check if it is within the data
              final int yy = y + yoffset[d];
              final int xx = x + xoffset[d];
              isWithin = (yy >= 0 && yy < maxy) && (xx >= 0 && xx < maxx);
            }

            if (isWithin) {
              if (maximaFlag[index + offset[d]]) {
                continue FIND_MAXIMUM;
              }
              if (data[index + offset[d]] > v) {
                continue FIND_MAXIMUM;
              }
            }
          }
        }

        results.add(index);
        maximaFlag[index] = true;
      } // end FIND_MAXIMA
    }

    return results.toArray();
  }

  /**
   * Compute the local-maxima within a 2n+1 block. <p> Uses the 2D block algorithm of Neubeck and
   * Van Gool (2006).
   *
   * @param data The input data (packed in YX order)
   * @param maxx The width of the data
   * @param maxy The height of the data
   * @param n The block size
   * @return Indices to the local maxima (index = maxx * y + x)
   */
  public int[] blockFind(float[] data, int maxx, int maxy, int n) {
    if (n == 1) {
      // optimised version for the special case
      return blockFind3x3(data, maxx, maxy);
    }

    return blockFindNxN(data, maxx, maxy, n);
  }

  /**
   * Compute the local-maxima within a 2n+1 block. An inner boundary of N is ignored as potential
   * maxima. <p> Uses the 2D block algorithm of Neubeck and Van Gool (2006).
   *
   * @param data The input data (packed in YX order)
   * @param maxx The width of the data
   * @param maxy The height of the data
   * @param n The block size
   * @param border The internal border
   * @return Indices to the local maxima (index = maxx * y + x)
   */
  public int[] blockFindInternal(float[] data, int maxx, int maxy, int n, int border) {
    if (n == 1) {
      // optimised version for the special case
      return blockFind3x3Internal(data, maxx, maxy, border);
    }

    return blockFindNxNInternal(data, maxx, maxy, n, border);
  }

  /**
   * Compute the local-maxima within a 2n+1 block. <p> Uses the 2D block algorithm of Neubeck and
   * Van Gool (2006).
   *
   * @param data The input data (packed in YX order)
   * @param maxx The width of the data
   * @param maxy The height of the data
   * @param n The block size
   * @return Indices to the local maxima (index = maxx * y + x)
   */
  public int[] blockFindNxN(float[] data, int maxx, int maxy, int n) {
    int[] maxima;
    int nMaxima = 0;
    final int n1 = n + 1;

    if (isNeighbourCheck()) {
      // Compute all candidate maxima in the NxN blocks. Then check each candidate
      // is a maxima in the 2N+1 region including a check for existing maxima.
      final int[][] blockMaximaCandidates = findBlockMaximaCandidatesNxN(data, maxx, maxy, n);

      maxima = new int[blockMaximaCandidates.length];
      final boolean[] maximaFlag = getFlagBuffer(data.length);

      for (final int[] blockMaxima : blockMaximaCandidates) {
        FIND_MAXIMUM: for (final int index : blockMaxima) {
          final float v = data[index];

          final int mi = index % maxx;
          final int mj = index / maxx;

          // Compare the maxima to the surroundings. Ignore the block region already processed.
          //
          // .......(mi-n,mj-n)----------------------------------(mi+n,mj-n)
          // ............|aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa|
          // ............|aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa|
          // ............|aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa|
          // ............|aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa|
          // ............|aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa|
          // ............|bbbbbbbb.(i,j)-------------(i+n,j).ccccccccc|
          // ............|bbbbbbbbbbb|..................|ccccccccccccc|
          // ............|bbbbbbbbbbb|..................|ccccccccccccc|
          // ............|bbbbbbbbbbb|.....(mi,mj)......|ccccccccccccc|
          // ............|bbbbbbbbbbb|..................|ccccccccccccc|
          // ............|bbbbbbbbbbb|..................|ccccccccccccc|
          // ............|bbbbbbbbbbb|..................|ccccccccccccc|
          // ............|bbbbbbbbbbb|..................|ccccccccccccc|
          // ............|bbbbbbbb.(i,j+n)-----------(i+n,j+n).ccccccc|
          // ............|dddddddddddddddddddddddddddddddddddddddddddd|
          // ............|dddddddddddddddddddddddddddddddddddddddddddd|
          // ............|dddddddddddddddddddddddddddddddddddddddddddd|
          // ......(mi-n,mj+n)----------------------------------(mi+n,mj+n)
          //
          // This must be done without over-running boundaries
          // int steps = 0;
          final int j = n1 * (mj / n1);
          final int mi_minus_n = FastMath.max(mi - n, 0);
          final int mi_plus_n = FastMath.min(mi + n, maxx - 1);
          final int mj_minus_n = FastMath.max(mj - n, 0);

          // A
          for (int jj = mj_minus_n; jj < j; jj++) {
            int indexStart = jj * maxx + mi_minus_n;
            final int indexEnd = jj * maxx + mi_plus_n;
            for (; indexStart <= indexEnd; indexStart++) {
              // steps++;
              if (v < data[indexStart]) {
                continue FIND_MAXIMUM;
              }
            }
          }
          final int i = n1 * (mi / n1);
          final int i_plus_n = i + n1;
          final int j_plus_n = FastMath.min(j + n, maxy - 1);
          for (int jj = j; jj <= j_plus_n; jj++) {
            // B
            {
              int indexStart = jj * maxx + mi_minus_n;
              final int indexEnd = jj * maxx + i;
              for (; indexStart < indexEnd; indexStart++) {
                // steps++;
                if (v < data[indexStart]) {
                  continue FIND_MAXIMUM;
                }
              }
            }

            // C
            {
              int indexStart = jj * maxx + i_plus_n;
              final int indexEnd = jj * maxx + mi_plus_n;
              for (; indexStart <= indexEnd; indexStart++) {
                // steps++;
                if (v < data[indexStart]) {
                  continue FIND_MAXIMUM;
                }
              }
            }
          }
          // D
          final int mj_plus_n = FastMath.min(mj + n, maxy - 1);
          for (int jj = j_plus_n + 1; jj <= mj_plus_n; jj++) {
            int indexStart = jj * maxx + mi_minus_n;
            final int indexEnd = jj * maxx + mi_plus_n;
            for (; indexStart <= indexEnd; indexStart++) {
              // steps++;
              if (v < data[indexStart]) {
                continue FIND_MAXIMUM;
              }
            }
          }

          // Neighbour check for existing maxima. This is relevant if the same height.
          // However we cannot just check height as the neighbour may not be the selected maxima
          // within its block.
          // Only check A+B since the blocks for C+D have not yet been processed

          // TODO: We only need to check the maxima of the preceding blocks:
          // This requires iteration over the blockMaximaCandidates as a grid.
          // - Find the maxima for the preceding blocks...
          // - Check if within N distance
          // - Check if equal (higher check has already been done)

          // A
          for (int jj = mj_minus_n; jj < j; jj++) {
            int indexStart = jj * maxx + mi_minus_n;
            final int indexEnd = jj * maxx + mi_plus_n;
            for (; indexStart <= indexEnd; indexStart++) {
              // steps++;
              if (maximaFlag[indexStart]) {
                continue FIND_MAXIMUM;
              }
            }
          }
          for (int jj = j; jj <= j_plus_n; jj++)
          // B
          {
            int indexStart = jj * maxx + mi_minus_n;
            final int indexEnd = jj * maxx + i;
            for (; indexStart < indexEnd; indexStart++) {
              // steps++;
              if (maximaFlag[indexStart]) {
                continue FIND_MAXIMUM;
              }
            }
          }

          // System.out.printf("%.2f @ %d,%d. Steps = %d\n", v, x, y, steps);

          maximaFlag[index] = true;
          maxima[nMaxima++] = index;
          break;
        } // end FIND_MAXIMA
      }
    } else {
      final int[] blockMaxima = findBlockMaximaNxN(data, maxx, maxy, n);

      maxima = blockMaxima; // Re-use storage space

      FIND_MAXIMUM: for (final int index : blockMaxima) {
        final float v = data[index];

        final int mi = index % maxx;
        final int mj = index / maxx;

        // Compare the maxima to the surroundings. Ignore the block region already processed.
        //
        // .......(mi-n,mj-n)----------------------------------(mi+n,mj-n)
        // ............|aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa|
        // ............|aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa|
        // ............|aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa|
        // ............|aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa|
        // ............|aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa|
        // ............|bbbbbbbb.(i,j)-------------(i+n,j).ccccccccc|
        // ............|bbbbbbbbbbb|..................|ccccccccccccc|
        // ............|bbbbbbbbbbb|..................|ccccccccccccc|
        // ............|bbbbbbbbbbb|.....(mi,mj)......|ccccccccccccc|
        // ............|bbbbbbbbbbb|..................|ccccccccccccc|
        // ............|bbbbbbbbbbb|..................|ccccccccccccc|
        // ............|bbbbbbbbbbb|..................|ccccccccccccc|
        // ............|bbbbbbbbbbb|..................|ccccccccccccc|
        // ............|bbbbbbbb.(i,j+n)-----------(i+n,j+n).ccccccc|
        // ............|dddddddddddddddddddddddddddddddddddddddddddd|
        // ............|dddddddddddddddddddddddddddddddddddddddddddd|
        // ............|dddddddddddddddddddddddddddddddddddddddddddd|
        // ......(mi-n,mj+n)----------------------------------(mi+n,mj+n)
        //
        // This must be done without over-running boundaries
        // int steps = 0;
        final int j = n1 * (mj / n1);
        final int mi_minus_n = FastMath.max(mi - n, 0);
        final int mi_plus_n = FastMath.min(mi + n, maxx - 1);
        final int mj_minus_n = FastMath.max(mj - n, 0);

        // A
        for (int jj = mj_minus_n; jj < j; jj++) {
          int indexStart = jj * maxx + mi_minus_n;
          final int indexEnd = jj * maxx + mi_plus_n;
          for (; indexStart <= indexEnd; indexStart++) {
            // steps++;
            if (v < data[indexStart]) {
              continue FIND_MAXIMUM;
            }
          }
        }
        final int i = n1 * (mi / n1);
        final int i_plus_n = i + n1;
        final int j_plus_n = FastMath.min(j + n, maxy - 1);
        for (int jj = j; jj <= j_plus_n; jj++) {
          // B
          {
            int indexStart = jj * maxx + mi_minus_n;
            final int indexEnd = jj * maxx + i;
            for (; indexStart < indexEnd; indexStart++) {
              // steps++;
              if (v < data[indexStart]) {
                continue FIND_MAXIMUM;
              }
            }
          }

          // C
          {
            int indexStart = jj * maxx + i_plus_n;
            final int indexEnd = jj * maxx + mi_plus_n;
            for (; indexStart <= indexEnd; indexStart++) {
              // steps++;
              if (v < data[indexStart]) {
                continue FIND_MAXIMUM;
              }
            }
          }
        }
        // D
        final int mj_plus_n = FastMath.min(mj + n, maxy - 1);
        for (int jj = j_plus_n + 1; jj <= mj_plus_n; jj++) {
          int indexStart = jj * maxx + mi_minus_n;
          final int indexEnd = jj * maxx + mi_plus_n;
          for (; indexStart <= indexEnd; indexStart++) {
            // steps++;
            if (v < data[indexStart]) {
              continue FIND_MAXIMUM;
            }
          }
        }
        // System.out.printf("%.2f @ %d,%d. Steps = %d\n", v, x, y, steps);

        maxima[nMaxima++] = index;
      } // end FIND_MAXIMA
    }

    return truncate(maxima, nMaxima);
  }

  /**
   * Compute the local-maxima within a 2n+1 block. An inner boundary of N is ignored as potential
   * maxima. <p> Uses the 2D block algorithm of Neubeck and Van Gool (2006).
   *
   * @param data The input data (packed in YX order)
   * @param maxx The width of the data
   * @param maxy The height of the data
   * @param n The block size
   * @param border The internal border
   * @return Indices to the local maxima (index = maxx * y + x)
   */
  public int[] blockFindNxNInternal(float[] data, int maxx, int maxy, int n, int border) {
    int[] maxima;
    int nMaxima = 0;
    final int n1 = n + 1;

    if (isNeighbourCheck()) {
      // Compute all candidate maxima in the NxN blocks. Then check each candidate
      // is a maxima in the 2N+1 region including a check for existing maxima.
      final int[][] blockMaximaCandidates =
          findBlockMaximaCandidatesNxNInternal(data, maxx, maxy, n, border);

      maxima = new int[blockMaximaCandidates.length];
      final boolean[] maximaFlag = getFlagBuffer(data.length);

      for (final int[] blockMaxima : blockMaximaCandidates) {
        FIND_MAXIMUM: for (final int index : blockMaxima) {
          final float v = data[index];

          final int mi = index % maxx;
          final int mj = index / maxx;

          // Compare the maxima to the surroundings. Ignore the block region already processed.
          //
          // .......(mi-n,mj-n)----------------------------------(mi+n,mj-n)
          // ............|aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa|
          // ............|aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa|
          // ............|aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa|
          // ............|aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa|
          // ............|aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa|
          // ............|bbbbbbbb.(i,j)-------------(i+n,j).ccccccccc|
          // ............|bbbbbbbbbbb|..................|ccccccccccccc|
          // ............|bbbbbbbbbbb|..................|ccccccccccccc|
          // ............|bbbbbbbbbbb|.....(mi,mj)......|ccccccccccccc|
          // ............|bbbbbbbbbbb|..................|ccccccccccccc|
          // ............|bbbbbbbbbbb|..................|ccccccccccccc|
          // ............|bbbbbbbbbbb|..................|ccccccccccccc|
          // ............|bbbbbbbbbbb|..................|ccccccccccccc|
          // ............|bbbbbbbb.(i,j+n)-----------(i+n,j+n).ccccccc|
          // ............|dddddddddddddddddddddddddddddddddddddddddddd|
          // ............|dddddddddddddddddddddddddddddddddddddddddddd|
          // ............|dddddddddddddddddddddddddddddddddddddddddddd|
          // ......(mi-n,mj+n)----------------------------------(mi+n,mj+n)
          //
          // No check for over-running boundaries since this is the internal version
          // int steps = 0;
          final int j = n1 * ((mj - border) / n1) + border; // Blocks n+1 wide
          // The block boundaries will have been truncated on the final block. Ensure this is swept
          final int mi_minus_n = FastMath.max(mi - n, 0);
          final int mi_plus_n = FastMath.min(mi + n, maxx - 1);
          final int mj_minus_n = FastMath.max(mj - n, 0);

          // System.out.printf("Block [%d,%d] => [%d,%d]\n", x, y, i, j);

          // A
          for (int jj = mj_minus_n; jj < j; jj++) {
            int indexStart = jj * maxx + mi_minus_n;
            final int indexEnd = jj * maxx + mi_plus_n;
            for (; indexStart <= indexEnd; indexStart++) {
              // steps++;
              if (v < data[indexStart]) {
                continue FIND_MAXIMUM;
              }
            }
          }
          final int i = n1 * ((mi - border) / n1) + border; // Blocks n+1 wide
          final int i_plus_n = i + n1;
          final int j_plus_n = FastMath.min(j + n, maxy - border - 1);
          for (int jj = j; jj <= j_plus_n; jj++) {
            // B
            {
              int indexStart = jj * maxx + mi_minus_n;
              final int indexEnd = jj * maxx + i;
              for (; indexStart < indexEnd; indexStart++) {
                // steps++;
                if (v < data[indexStart]) {
                  continue FIND_MAXIMUM;
                }
              }
            }

            // C
            {
              int indexStart = jj * maxx + i_plus_n;
              final int indexEnd = jj * maxx + mi_plus_n;
              for (; indexStart <= indexEnd; indexStart++) {
                // steps++;
                if (v < data[indexStart]) {
                  continue FIND_MAXIMUM;
                }
              }
            }
          }
          // D
          final int mj_plus_n = FastMath.min(mj + n, maxy - 1);
          for (int jj = j_plus_n + 1; jj <= mj_plus_n; jj++) {
            int indexStart = jj * maxx + mi_minus_n;
            final int indexEnd = jj * maxx + mi_plus_n;
            for (; indexStart <= indexEnd; indexStart++) {
              // steps++;
              if (v < data[indexStart]) {
                continue FIND_MAXIMUM;
              }
            }
          }
          // System.out.printf("%.2f @ %d,%d. Steps = %d, max = %b\n", v, x, y, steps, isMax);

          // Neighbour check for existing maxima. This is relevant if the same height.
          // However we cannot just check height as the neighbour may not be the selected maxima
          // within its block.
          // Only check A+B since the blocks for C+D have not yet been processed

          // TODO: We only need to check the maxima of the preceding blocks:
          // This requires iteration over the blockMaximaCandidates as a grid.
          // - Find the maxima for the preceding blocks...
          // - Check if within N distance
          // - Check if equal (higher check has already been done)

          // A
          for (int jj = mj_minus_n; jj < j; jj++) {
            int indexStart = jj * maxx + mi_minus_n;
            final int indexEnd = jj * maxx + mi_plus_n;
            for (; indexStart <= indexEnd; indexStart++) {
              // steps++;
              if (maximaFlag[indexStart]) {
                continue FIND_MAXIMUM;
              }
            }
          }
          for (int jj = j; jj <= j_plus_n; jj++)
          // B
          {
            int indexStart = jj * maxx + mi_minus_n;
            final int indexEnd = jj * maxx + i;
            for (; indexStart < indexEnd; indexStart++) {
              // steps++;
              if (maximaFlag[indexStart]) {
                continue FIND_MAXIMUM;
              }
            }
          }

          // System.out.printf("%.2f @ %d,%d. Steps = %d\n", v, x, y, steps);

          maximaFlag[index] = true;
          maxima[nMaxima++] = index;
          break;
        } // end FIND_MAXIMA
      }
    } else {
      final int[] blockMaxima = findBlockMaximaNxNInternal(data, maxx, maxy, n, border);

      maxima = blockMaxima; // Re-use storage space

      FIND_MAXIMUM: for (final int index : blockMaxima) {
        final float v = data[index];

        final int mi = index % maxx;
        final int mj = index / maxx;

        // Compare the maxima to the surroundings. Ignore the block region already processed.
        //
        // .......(mi-n,mj-n)----------------------------------(mi+n,mj-n)
        // ............|aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa|
        // ............|aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa|
        // ............|aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa|
        // ............|aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa|
        // ............|aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa|
        // ............|bbbbbbbb.(i,j)-------------(i+n,j).ccccccccc|
        // ............|bbbbbbbbbbb|..................|ccccccccccccc|
        // ............|bbbbbbbbbbb|..................|ccccccccccccc|
        // ............|bbbbbbbbbbb|.....(mi,mj)......|ccccccccccccc|
        // ............|bbbbbbbbbbb|..................|ccccccccccccc|
        // ............|bbbbbbbbbbb|..................|ccccccccccccc|
        // ............|bbbbbbbbbbb|..................|ccccccccccccc|
        // ............|bbbbbbbbbbb|..................|ccccccccccccc|
        // ............|bbbbbbbb.(i,j+n)-----------(i+n,j+n).ccccccc|
        // ............|dddddddddddddddddddddddddddddddddddddddddddd|
        // ............|dddddddddddddddddddddddddddddddddddddddddddd|
        // ............|dddddddddddddddddddddddddddddddddddddddddddd|
        // ......(mi-n,mj+n)----------------------------------(mi+n,mj+n)
        //
        // No check for over-running boundaries since this is the internal version
        // int steps = 0;
        final int j = n1 * ((mj - border) / n1) + border; // Blocks n+1 wide
        // The block boundaries will have been truncated on the final block. Ensure this is swept
        final int mi_minus_n = FastMath.max(mi - n, 0);
        final int mi_plus_n = FastMath.min(mi + n, maxx - 1);
        final int mj_minus_n = FastMath.max(mj - n, 0);

        // System.out.printf("Block [%d,%d] => [%d,%d]\n", x, y, i, j);

        // A
        for (int jj = mj_minus_n; jj < j; jj++) {
          int indexStart = jj * maxx + mi_minus_n;
          final int indexEnd = jj * maxx + mi_plus_n;
          for (; indexStart <= indexEnd; indexStart++) {
            // steps++;
            if (v < data[indexStart]) {
              continue FIND_MAXIMUM;
            }
          }
        }
        final int i = n1 * ((mi - border) / n1) + border; // Blocks n+1 wide
        final int i_plus_n = i + n1;
        final int j_plus_n = FastMath.min(j + n, maxy - border - 1);
        for (int jj = j; jj <= j_plus_n; jj++) {
          // B
          {
            int indexStart = jj * maxx + mi_minus_n;
            final int indexEnd = jj * maxx + i;
            for (; indexStart < indexEnd; indexStart++) {
              // steps++;
              if (v < data[indexStart]) {
                continue FIND_MAXIMUM;
              }
            }
          }

          // C
          {
            int indexStart = jj * maxx + i_plus_n;
            final int indexEnd = jj * maxx + mi_plus_n;
            for (; indexStart <= indexEnd; indexStart++) {
              // steps++;
              if (v < data[indexStart]) {
                continue FIND_MAXIMUM;
              }
            }
          }
        }
        // D
        final int mj_plus_n = FastMath.min(mj + n, maxy - 1);
        for (int jj = j_plus_n + 1; jj <= mj_plus_n; jj++) {
          int indexStart = jj * maxx + mi_minus_n;
          final int indexEnd = jj * maxx + mi_plus_n;
          for (; indexStart <= indexEnd; indexStart++) {
            // steps++;
            if (v < data[indexStart]) {
              continue FIND_MAXIMUM;
            }
          }
        }
        // System.out.printf("%.2f @ %d,%d. Steps = %d, max = %b\n", v, x, y, steps, isMax);

        maxima[nMaxima++] = index;
      } // end FIND_MAXIMA
    }

    return truncate(maxima, nMaxima);
  }

  /**
   * Search the data for the index of the maximum in each block of size n*n. <p> E.g. Max [ (i,i+n)
   * x (i,j+n) ] for (i=0; i&lt;maxx; i+=n) x (j=0, j&lt;maxy; j+=n)
   *
   * @param data The input data (packed in YX order)
   * @param maxx The width of the data
   * @param maxy The height of the data
   * @param n The block size
   * @return The maxima indices
   */
  public int[] findBlockMaximaNxN(float[] data, int maxx, int maxy, int n) {
    // Include the actual pixel in the block
    // This makes the block search inclusive: i=0; i<=n; i++
    n++;

    // The number of blocks in x and y
    final int xblocks = getBlocks(maxx, n);
    final int yblocks = getBlocks(maxy, n);

    // The final index in each dimension (where an incomplete block is found).
    // This equals maxx/maxy if the number of blocks fits exactly.
    final int xfinal = n * (maxx / n);
    final int yfinal = n * (maxy / n);

    final int[] maxima = new int[xblocks * yblocks];

    int block = 0;
    for (int y = 0; y < maxy; y += n) {
      for (int x = 0; x < maxx; x += n) {
        // Find the sweep size in each direction
        final int xsize = (x != xfinal) ? n : maxx - xfinal;
        int ysize = (y != yfinal) ? n : maxy - yfinal;

        int index = y * maxx + x;
        int maxIndex = index;
        float max = data[maxIndex];

        while (ysize-- > 0) {
          for (int x2 = xsize; x2-- > 0;) {
            if (max < data[index]) {
              max = data[index];
              maxIndex = index;
            }
            index++;
          }
          index += maxx - xsize;
        }

        maxima[block++] = maxIndex;
      }
    }

    return maxima;
  }

  /**
   * Search the data for the index of the maximum in each block of size n*n. An inner boundary of N
   * is ignored as potential maxima. <p> E.g. Max [ (i,i+n) x (i,j+n) ] for (i=n; i&lt;maxx-n; i+=n)
   * x (j=n, j&lt;maxy-n; j+=n)
   *
   * @param data The input data (packed in YX order)
   * @param maxx The width of the data
   * @param maxy The height of the data
   * @param n The block size
   * @param border The internal border
   * @return The maxima indices
   */
  public int[] findBlockMaximaNxNInternal(float[] data, int maxx, int maxy, int n, int border) {
    // Include the actual pixel in the block
    // This makes the block search inclusive: i=0; i<=n; i++
    n++;

    // The number of blocks in x and y. Subtract the boundary blocks.
    final int xblocks = getBlocks(maxx - border, n);
    final int yblocks = getBlocks(maxy - border, n);

    if (xblocks < 1 || yblocks < 1) {
      return new int[0];
    }

    // The final index in each dimension (where an incomplete block is found).
    // This equals maxx/maxy if the number of blocks fits exactly.
    final int xfinal = n * ((maxx - 2 * border) / n) + border;
    final int yfinal = n * ((maxy - 2 * border) / n) + border;

    final int[] maxima = new int[xblocks * yblocks];

    int block = 0;
    for (int y = border; y < maxy - border; y += n) {
      for (int x = border; x < maxx - border; x += n) {
        // System.out.printf("Block [%d,%d]\n", x, y);

        // Find the sweep size in each direction
        final int xsize = (x != xfinal) ? n : maxx - xfinal - border;
        int ysize = (y != yfinal) ? n : maxy - yfinal - border;

        int index = y * maxx + x;
        int maxIndex = index;
        float max = data[maxIndex];

        while (ysize-- > 0) {
          for (int x2 = xsize; x2-- > 0;) {
            if (max < data[index]) {
              max = data[index];
              maxIndex = index;
            }
            index++;
          }
          index += maxx - xsize;
        }

        maxima[block++] = maxIndex;
      }
    }

    return truncate(maxima, block);
  }

  /**
   * Search the data for the index of the maximum in each block of size 2*2. <p> E.g. Max [ (i,i+n)
   * x (i,j+n) ] for (i=0; i&lt;maxx; i+=n) x (j=0, j&lt;maxy; j+=n)
   *
   * @param data The input data (packed in YX order)
   * @param maxx The width of the data
   * @param maxy The height of the data
   * @return The maxima indices
   */
  public int[] findBlockMaxima2x2(float[] data, int maxx, int maxy) {
    // Optimised for 2x2 block
    // final int n = 2;

    // The number of blocks in x and y
    final int xblocks = getBlocks(maxx, 2);
    final int yblocks = getBlocks(maxy, 2);

    // The final index in each dimension (where an incomplete block is found).
    // This equals maxx/maxy if the number of blocks fits exactly.
    final int xfinal = 2 * (maxx / 2);
    final int yfinal = 2 * (maxy / 2);

    // TODO - Try this by expanding the data if xfinal != maxx || yfinal != maxy
    // This will allow less management of boundaries.

    final int[] maxima = new int[xblocks * yblocks];

    // Sweep 4 regions:
    // x.............xfinal
    // |.............|..maxx
    // |.............|..|
    // aaaaaaaaaaaaaaabbb-- y
    // aaaaaaaaaaaaaaabbb
    // aaaaaaaaaaaaaaabbb
    // aaaaaaaaaaaaaaabbb
    // aaaaaaaaaaaaaaabbb
    // aaaaaaaaaaaaaaabbb
    // aaaaaaaaaaaaaaabbb
    // aaaaaaaaaaaaaaabbb--yfinal
    // cccccccccccccccddd
    // cccccccccccccccddd--maxy

    int block = 0;
    for (int y = 0; y < yfinal; y += 2) {
      // A
      int xindex = y * maxx;
      for (int x = 0; x < xfinal; x += 2) {
        // Compare 2x2 block
        final int max1 = (data[xindex] > data[xindex + 1]) ? xindex : xindex + 1;
        final int max2 =
            (data[xindex + maxx] > data[xindex + maxx + 1]) ? xindex + maxx : xindex + maxx + 1;

        maxima[block++] = (data[max1] > data[max2]) ? max1 : max2;

        xindex += 2;
      }
      // B
      if (xfinal != maxx) {
        // Compare 1x2 block
        final int index = y * maxx + xfinal;
        maxima[block++] = (data[index] > data[index + maxx]) ? index : index + maxx;
      }
    }
    if (yfinal != maxy) {
      // C
      int xindex = yfinal * maxx;
      for (int x = 0; x < xfinal; x += 2) {
        // Compare 2x1 block
        maxima[block++] = (data[xindex] > data[xindex + 1]) ? xindex : xindex + 1;
        xindex += 2;
      }
      // D
      if (xfinal != maxx) {
        // Compare 1x1 block
        maxima[block++] = yfinal * maxx + xfinal;
      }
    }

    return maxima;
  }

  /**
   * Search the data for the index of the maximum in each block of size n*n. <p> E.g. Max [ (i,i+n)
   * x (i,j+n) ] for (i=0; i&lt;maxx; i+=n) x (j=0, j&lt;maxy; j+=n) <p> If multiple indices within
   * the block have the same value then all are returned.
   *
   * @param data The input data (packed in YX order)
   * @param maxx The width of the data
   * @param maxy The height of the data
   * @param n The block size
   * @return The maxima indices
   */
  public int[][] findBlockMaximaCandidatesNxN(float[] data, int maxx, int maxy, int n) {
    // Include the actual pixel in the block
    // This makes the block search inclusive: i=0; i<=n; i++
    n++;

    // The number of blocks in x and y
    final int xblocks = getBlocks(maxx, n);
    final int yblocks = getBlocks(maxy, n);

    // The final index in each dimension (where an incomplete block is found).
    // This equals maxx/maxy if the number of blocks fits exactly.
    final int xfinal = n * (maxx / n);
    final int yfinal = n * (maxy / n);

    final int[][] maxima = new int[xblocks * yblocks][];
    final FixedIntList list = new FixedIntList(n * n);

    int block = 0;
    for (int y = 0; y < maxy; y += n) {
      for (int x = 0; x < maxx; x += n) {
        // Find the sweep size in each direction
        final int xsize = (x != xfinal) ? n : maxx - xfinal;
        int ysize = (y != yfinal) ? n : maxy - yfinal;

        int index = y * maxx + x;
        float max = floatMin;

        while (ysize-- > 0) {
          for (int x2 = xsize; x2-- > 0;) {
            if (max < data[index]) {
              list.clear();
              list.add(index);
              max = data[index];
            } else if (max == data[index]) {
              list.add(index);
            }
            index++;
          }
          index += maxx - xsize;
        }

        maxima[block++] = list.toArray();
      }
    }

    return maxima;
  }

  /**
   * Search the data for the index of the maximum in each block of size n*n. An inner boundary of N
   * is ignored as potential maxima. <p> E.g. Max [ (i,i+n) x (i,j+n) ] for (i=n; i&lt;maxx-n; i+=n)
   * x (j=n, j&lt;maxy-n; j+=n) <p> If multiple indices within the block have the same value then
   * all are returned.
   *
   * @param data The input data (packed in YX order)
   * @param maxx The width of the data
   * @param maxy The height of the data
   * @param n The block size
   * @param border The internal border
   * @return The maxima indices
   */
  public int[][] findBlockMaximaCandidatesNxNInternal(float[] data, int maxx, int maxy, int n,
      int border) {
    // Include the actual pixel in the block
    // This makes the block search inclusive: i=0; i<=n; i++
    n++;

    // The number of blocks in x and y. Subtract the boundary blocks.
    final int xblocks = getBlocks(maxx - border, n);
    final int yblocks = getBlocks(maxy - border, n);

    if (xblocks < 1 || yblocks < 1) {
      return new int[0][0];
    }

    // The final index in each dimension (where an incomplete block is found).
    // This equals maxx/maxy if the number of blocks fits exactly.
    final int xfinal = n * ((maxx - 2 * border) / n) + border;
    final int yfinal = n * ((maxy - 2 * border) / n) + border;

    final int[][] maxima = new int[xblocks * yblocks][];
    final FixedIntList list = new FixedIntList(n * n);

    int block = 0;
    for (int y = border; y < maxy - border; y += n) {
      for (int x = border; x < maxx - border; x += n) {
        // System.out.printf("Block [%d,%d]\n", x, y);

        // Find the sweep size in each direction
        final int xsize = (x != xfinal) ? n : maxx - xfinal - border;
        int ysize = (y != yfinal) ? n : maxy - yfinal - border;

        int index = y * maxx + x;
        float max = floatMin;

        while (ysize-- > 0) {
          for (int x2 = xsize; x2-- > 0;) {
            if (max < data[index]) {
              list.clear();
              list.add(index);
              max = data[index];
            } else if (max == data[index]) {
              list.add(index);
            }
            index++;
          }
          index += maxx - xsize;
        }

        maxima[block++] = list.toArray();
      }
    }

    return truncate(maxima, block);
  }

  /**
   * Search the data for the index of the maximum in each block of size 2*2. <p> E.g. Max [ (i,i+n)
   * x (i,j+n) ] for (i=0; i&lt;maxx; i+=n) x (j=0, j&lt;maxy; j+=n) <p> If multiple indices within
   * the block have the same value then all are returned.
   *
   * @param data The input data (packed in YX order)
   * @param maxx The width of the data
   * @param maxy The height of the data
   * @return The maxima indices
   */
  public int[][] findBlockMaximaCandidates2x2(float[] data, int maxx, int maxy) {
    // Optimised for 2x2 block
    // final int n = 2;

    // The number of blocks in x and y
    final int xblocks = getBlocks(maxx, 2);
    final int yblocks = getBlocks(maxy, 2);

    // The final index in each dimension (where an incomplete block is found).
    // This equals maxx/maxy if the number of blocks fits exactly.
    final int xfinal = 2 * (maxx / 2);
    final int yfinal = 2 * (maxy / 2);

    // TODO - Try this by expanding the data if xfinal != maxx || yfinal != maxy
    // This will allow less management of boundaries.

    final int[][] maxima = new int[xblocks * yblocks][];

    // Sweep 4 regions:
    // x.............xfinal
    // |.............|..maxx
    // |.............|..|
    // aaaaaaaaaaaaaaabbb-- y
    // aaaaaaaaaaaaaaabbb
    // aaaaaaaaaaaaaaabbb
    // aaaaaaaaaaaaaaabbb
    // aaaaaaaaaaaaaaabbb
    // aaaaaaaaaaaaaaabbb
    // aaaaaaaaaaaaaaabbb
    // aaaaaaaaaaaaaaabbb--yfinal
    // cccccccccccccccddd
    // cccccccccccccccddd--maxy

    int block = 0;
    for (int y = 0; y < yfinal; y += 2) {
      // A
      int xindex = y * maxx;
      for (int x = 0; x < xfinal; x += 2) {
        // Compare 2x2 block
        final int[] i1 = getIndices(data, xindex, xindex + 1);
        final int[] i2 = getIndices(data, xindex + maxx, xindex + maxx + 1);

        if (data[i1[0]] > data[i2[0]]) {
          maxima[block++] = i1;
        } else if (data[i1[0]] == data[i2[0]]) {
          if (i1.length == 1) {
            if (i2.length == 1) {
              maxima[block++] = new int[] {i1[0], i2[0]};
            } else {
              maxima[block++] = new int[] {i1[0], i2[0], i2[1]};
            }
          } else if (i2.length == 1) {
            maxima[block++] = new int[] {i1[0], i1[1], i2[0]};
          } else {
            maxima[block++] = new int[] {i1[0], i1[1], i2[0], i2[1]};
          }
        } else {
          maxima[block++] = i2;
        }
        xindex += 2;
      }
      // B
      if (xfinal != maxx) {
        // Compare 1x2 block
        final int index = y * maxx + xfinal;
        maxima[block++] = getIndices(data, index, index + maxx);
      }
    }
    if (yfinal != maxy) {
      // C
      int xindex = yfinal * maxx;
      for (int x = 0; x < xfinal; x += 2) {
        // Compare 2x1 block
        maxima[block++] = getIndices(data, xindex, xindex + 1);
        xindex += 2;
      }
      // D
      if (xfinal != maxx) {
        // Compare 1x1 block
        maxima[block++] = new int[] {yfinal * maxx + xfinal};
      }
    }

    return maxima;
  }

  private static int[] getIndices(float[] data, int i, int j) {
    if (data[i] > data[j]) {
      return new int[] {i};
    } else if (data[i] == data[j]) {
      return new int[] {i, j};
    }
    return new int[] {j};
  }

  /**
   * Compute the local-maxima within a 3x3 block
   *
   * @param data The input data (packed in YX order)
   * @param maxx The width of the data
   * @param maxy The height of the data
   * @return Indices to the local maxima (index = maxx * y + x)
   */
  public int[] blockFind3x3(float[] data, int maxx, int maxy) {
    // The number of blocks in x and y
    final int xblocks = getBlocks(maxx, 2);
    final int yblocks = getBlocks(maxy, 2);

    // The final index in each dimension (where an incomplete block is found).
    // This equals maxx/maxy if the number of blocks fits exactly.
    final int xfinal = 2 * (maxx / 2);
    final int yfinal = 2 * (maxy / 2);

    // Expand the canvas
    final int newx = maxx + (maxx - xfinal) + 2;
    final int newy = maxy + (maxy - yfinal) + 2;
    data = expand(data, maxx, maxy, newx, newy);

    // Compare 2x2 block
    // ....
    // .AB.
    // .CD.
    // ....

    // Create the scan arrays to search the remaining 5 locations around the 2x2 block maxima
    // .... aaa. .bbb .... ....
    // .AB. aA.. ..Bb c... ...d
    // .CD. a... ...b cC.. ..Dd
    // .... .... .... ccc. .ddd
    final int[] a = new int[] {-newx - 1, -newx, -newx + 1, -1, +newx - 1};
    final int[] b = new int[] {-newx - 1, -newx, -newx + 1, +1, +newx + 1};
    final int[] c = new int[] {-newx - 1, -1, +newx - 1, +newx, +newx + 1};
    final int[] d = new int[] {-newx + 1, +1, +newx - 1, +newx, +newx + 1};

    final int[] maxima = new int[xblocks * yblocks];
    int nMaxima = 0;

    if (isNeighbourCheck()) {
      final boolean[] maximaFlag = getFlagBuffer(data.length);

      final int[][] scans = new int[4][];
      final int[] maxIndices = new int[4];
      int candidates;

      for (int y = 0; y < maxy; y += 2) {
        int xindex = (y + 1) * newx + 1;
        for (int x = 0; x < maxx; x += 2, xindex += 2) {
          // Compare A and B
          if (data[xindex] < data[xindex + 1]) {
            scans[0] = b;
            maxIndices[0] = xindex + 1;
            candidates = 1;
          } else if (data[xindex] == data[xindex + 1]) {
            scans[0] = a;
            maxIndices[0] = xindex;
            scans[1] = b;
            maxIndices[1] = xindex + 1;
            candidates = 2;
          } else {
            scans[0] = a;
            maxIndices[0] = xindex;
            candidates = 1;
          }

          // Compare to C
          if (data[maxIndices[0]] < data[xindex + newx]) {
            scans[0] = c;
            maxIndices[0] = xindex + newx;
            candidates = 1;
          } else if (data[maxIndices[0]] == data[xindex + newx]) {
            scans[candidates] = c;
            maxIndices[candidates] = xindex + newx;
            candidates++;
          }

          // Compare to D
          if (data[maxIndices[0]] < data[xindex + newx + 1]) {
            scans[0] = d;
            maxIndices[0] = xindex + newx + 1;
            candidates = 1;
          } else if (data[maxIndices[0]] == data[xindex + newx + 1]) {
            scans[candidates] = d;
            maxIndices[candidates] = xindex + newx + 1;
            candidates++;
          }

          // Check the remaining region for each candidate to ensure a true maxima
          FIND_MAXIMUM: for (int candidate = 0; candidate < candidates; candidate++) {
            final int maxIndex = maxIndices[candidate];
            final int[] scan = scans[candidate];

            for (final int offset : scan) {
              if (data[maxIndex] < data[maxIndex + offset]) {
                continue FIND_MAXIMUM;
              }
            }

            // Only check ABC since the scan blocks for D have not yet been processed
            if (scan != d) {
              for (final int offset : scan) {
                if (maximaFlag[maxIndex + offset]) {
                  continue FIND_MAXIMUM;
                }
              }
            }

            // Remap the maxima
            final int xx = maxIndex % newx;
            final int yy = maxIndex / newx;

            // System.out.printf("blockFind3x3 [%d,%d]\n", xx-1, yy-1);
            maximaFlag[maxIndex] = true;
            maxima[nMaxima++] = (yy - 1) * maxx + xx - 1;
            break;
          }
        }
      }
    } else {
      for (int y = 0; y < maxy; y += 2) {
        int xindex = (y + 1) * newx + 1;
        FIND_MAXIMUM: for (int x = 0; x < maxx; x += 2, xindex += 2) {
          int[] scan = a;
          int maxIndex = xindex;
          if (data[maxIndex] < data[xindex + 1]) {
            scan = b;
            maxIndex = xindex + 1;
          }
          if (data[maxIndex] < data[xindex + newx]) {
            scan = c;
            maxIndex = xindex + newx;
          }
          if (data[maxIndex] < data[xindex + newx + 1]) {
            scan = d;
            maxIndex = xindex + newx + 1;
          }

          // Check the remaining region
          for (final int offset : scan) {
            if (data[maxIndex] < data[maxIndex + offset]) {
              continue FIND_MAXIMUM;
            }
          }

          // Remap the maxima
          final int xx = maxIndex % newx;
          final int yy = maxIndex / newx;

          // System.out.printf("blockFind3x3 [%d,%d]\n", xx-1, yy-1);
          maxima[nMaxima++] = (yy - 1) * maxx + xx - 1;
        } // end FIND_MAXIMA
      }
    }

    return truncate(maxima, nMaxima);
  }

  /**
   * Compute the local-maxima within a 3x3 block. An inner boundary of 1 is ignored as potential
   * maxima on the top and left, and a boundary of 1 or 2 on the right or bottom (depending if the
   * image is even/odd dimensions).
   *
   * @param data The input data (packed in YX order)
   * @param maxx The width of the data
   * @param maxy The height of the data
   * @param border The internal border
   * @return Indices to the local maxima (index = maxx * y + x)
   */
  public int[] blockFind3x3Internal(float[] data, int maxx, int maxy, int border) {
    if (border < 1) {
      return blockFind3x3(data, maxx, maxy);
    }

    // The number of blocks in x and y
    final int xblocks = getBlocks(maxx - border, 2);
    final int yblocks = getBlocks(maxy - border, 2);

    // Compare 2x2 block
    // ....
    // .AB.
    // .CD.
    // ....

    // Create the scan arrays to search the remaining 5 locations around the 2x2 block maxima
    // .... aaa. .bbb .... ....
    // .AB. aA.. ..Bb c... ...d
    // .CD. a... ...b cC.. ..Dd
    // .... .... .... ccc. .ddd
    final int[] a = new int[] {-maxx - 1, -maxx, -maxx + 1, -1, +maxx - 1};
    final int[] b = new int[] {-maxx - 1, -maxx, -maxx + 1, +1, +maxx + 1};
    final int[] c = new int[] {-maxx - 1, -1, +maxx - 1, +maxx, +maxx + 1};
    final int[] d = new int[] {-maxx + 1, +1, +maxx - 1, +maxx, +maxx + 1};

    final int[] maxima = new int[xblocks * yblocks];
    int nMaxima = 0;

    if (isNeighbourCheck()) {
      final boolean[] maximaFlag = getFlagBuffer(data.length);

      final int[][] scans = new int[4][];
      final int[] maxIndices = new int[4];
      int candidates;

      for (int y = border; y < maxy - border - 1; y += 2) {
        int xindex = y * maxx + border;
        for (int x = border; x < maxx - border - 1; x += 2, xindex += 2) {
          // Compare A and B
          if (data[xindex] < data[xindex + 1]) {
            scans[0] = b;
            maxIndices[0] = xindex + 1;
            candidates = 1;
          } else if (data[xindex] == data[xindex + 1]) {
            scans[0] = a;
            maxIndices[0] = xindex;
            scans[1] = b;
            maxIndices[1] = xindex + 1;
            candidates = 2;
          } else {
            scans[0] = a;
            maxIndices[0] = xindex;
            candidates = 1;
          }

          // Compare to C
          if (data[maxIndices[0]] < data[xindex + maxx]) {
            scans[0] = c;
            maxIndices[0] = xindex + maxx;
            candidates = 1;
          } else if (data[maxIndices[0]] == data[xindex + maxx]) {
            scans[candidates] = c;
            maxIndices[candidates] = xindex + maxx;
            candidates++;
          }

          // Compare to D
          if (data[maxIndices[0]] < data[xindex + maxx + 1]) {
            scans[0] = d;
            maxIndices[0] = xindex + maxx + 1;
            candidates = 1;
          } else if (data[maxIndices[0]] == data[xindex + maxx + 1]) {
            scans[candidates] = d;
            maxIndices[candidates] = xindex + maxx + 1;
            candidates++;
          }

          // Check the remaining region for each candidate to ensure a true maxima
          FIND_MAXIMUM: for (int candidate = 0; candidate < candidates; candidate++) {
            final int maxIndex = maxIndices[candidate];
            final int[] scan = scans[candidate];

            for (final int offset : scan) {
              if (data[maxIndex] < data[maxIndex + offset]) {
                continue FIND_MAXIMUM;
              }
            }

            // Only check ABC since the scan blocks for D have not yet been processed
            if (scan != d) {
              for (final int offset : scan) {
                if (maximaFlag[maxIndex + offset]) {
                  continue FIND_MAXIMUM;
                }
              }
            }

            maximaFlag[maxIndex] = true;
            maxima[nMaxima++] = maxIndex;
            break;
          } // end FIND_MAXIMA
        }
      }
    } else {
      for (int y = border; y < maxy - border - 1; y += 2) {
        int xindex = y * maxx + border;
        FIND_MAXIMUM: for (int x = border; x < maxx - border - 1; x += 2, xindex += 2) {
          int[] scan = a;
          int maxIndex = xindex;
          if (data[maxIndex] < data[xindex + 1]) {
            scan = b;
            maxIndex = xindex + 1;
          }
          if (data[maxIndex] < data[xindex + maxx]) {
            scan = c;
            maxIndex = xindex + maxx;
          }
          if (data[maxIndex] < data[xindex + maxx + 1]) {
            scan = d;
            maxIndex = xindex + maxx + 1;
          }

          // Check the remaining region
          for (final int offset : scan) {
            if (data[maxIndex] < data[maxIndex + offset]) {
              continue FIND_MAXIMUM;
            }
          }

          maxima[nMaxima++] = maxIndex;
        } // end FIND_MAXIMA
      }
    }

    return truncate(maxima, nMaxima);
  }

  /**
   * Search the data for the index of the maximum in each block of size n*n. <p> E.g. Max [ (i,i+n)
   * x (i,j+n) ] for (i=0; i&lt;maxx; i+=n) x (j=0, j&lt;maxy; j+=n)
   *
   * @param data The input data (packed in YX order)
   * @param maxx The width of the data
   * @param maxy The height of the data
   * @param n the block size
   * @return The maxima indices
   */
  public int[] findBlockMaxima(float[] data, int maxx, int maxy, int n) {
    if (n == 1) {
      return findBlockMaxima2x2(data, maxx, maxy);
    }

    return findBlockMaximaNxN(data, maxx, maxy, n);
  }

  /**
   * Search the data for the index of the maximum in each block of size n*n. <p> E.g. Max [ (i,i+n)
   * x (i,j+n) ] for (i=0; i&lt;maxx; i+=n) x (j=0, j&lt;maxy; j+=n) <p> If multiple indices within
   * the block have the same value then all are returned.
   *
   * @param data The input data (packed in YX order)
   * @param maxx The width of the data
   * @param maxy The height of the data
   * @param n the block size
   * @return The maxima indices
   */
  public int[][] findBlockMaximaCandidates(float[] data, int maxx, int maxy, int n) {
    if (n == 1) {
      return findBlockMaximaCandidates2x2(data, maxx, maxy);
    }

    return findBlockMaximaCandidatesNxN(data, maxx, maxy, n);
  }

  // ----------------------------------------------------
  // NOTE:
  // Copy up to here to replace 'float' with 'int'
  // ----------------------------------------------------

  /**
   * Expand the image to the new dimensions with a 1-pixel border
   */
  private float[] expand(float[] data, int maxx, int maxy, int newx, int newy) {
    final int size = newx * newy;

    if (!dataBuffer || newDataFloat == null || newDataFloat.length < size) {
      newDataFloat = new float[size];
    }

    // Zero first row
    for (int x = 0; x < newx; x++) {
      newDataFloat[x] = Float.NEGATIVE_INFINITY;
    }
    // Zero last rows
    for (int y = maxy + 1; y < newy; y++) {
      int newIndex = y * newx;
      for (int x = 0; x < newx; x++) {
        newDataFloat[newIndex++] = Float.NEGATIVE_INFINITY;
      }
    }

    int index = 0;
    for (int y = 0; y < maxy; y++) {
      int newIndex = (y + 1) * newx;

      // Zero first column
      newDataFloat[newIndex++] = Float.NEGATIVE_INFINITY;

      // Copy data
      for (int x = 0; x < maxx; x++) {
        newDataFloat[newIndex++] = data[index++];
      }

      // Zero remaining columns
      for (int x = maxx + 1; x < newx; x++) {
        newDataFloat[newIndex++] = Float.NEGATIVE_INFINITY;
      }
    }

    return newDataFloat;
  }

  /**
   * Expand the image to the new dimensions with a 1-pixel border
   */
  private int[] expand(int[] data, int maxx, int maxy, int newx, int newy) {
    final int size = newx * newy;

    if (!dataBuffer || newDataInt == null || newDataInt.length < size) {
      newDataInt = new int[size];
    }

    // Zero first row
    for (int x = 0; x < newx; x++) {
      newDataInt[x] = Integer.MIN_VALUE;
    }
    // Zero last rows
    for (int y = maxy + 1; y < newy; y++) {
      int newIndex = y * newx;
      for (int x = 0; x < newx; x++) {
        newDataInt[newIndex++] = Integer.MIN_VALUE;
      }
    }

    int index = 0;
    for (int y = 0; y < maxy; y++) {
      int newIndex = (y + 1) * newx;

      // Zero first column
      newDataInt[newIndex++] = Integer.MIN_VALUE;

      // Copy data
      for (int x = 0; x < maxx; x++) {
        newDataInt[newIndex++] = data[index++];
      }

      // Zero remaining columns
      for (int x = maxx + 1; x < newx; x++) {
        newDataInt[newIndex++] = Integer.MIN_VALUE;
      }
    }

    return newDataInt;
  }

  /**
   * Get a buffer for storing result indices.
   *
   * @param size the size
   * @return the results buffer
   */
  private FixedIntList getResultsBuffer(int size) {
    if (!dataBuffer) {
      return new FixedIntList(size);
    }

    if (resultsBuffer == null || resultsBuffer.capacity() < size) {
      resultsBuffer = new FixedIntList(size);
    } else {
      resultsBuffer.clear();
    }

    return resultsBuffer;
  }

  /**
   * Get a buffer for flagging maxima.
   *
   * @param size the size
   * @return the flag buffer
   */
  private boolean[] getFlagBuffer(int size) {
    if (!dataBuffer) {
      return new boolean[size];
    }

    if (maximaFlagBuffer == null || maximaFlagBuffer.length < size) {
      maximaFlagBuffer = new boolean[size];
    } else {
      // Reset flags
      for (int x = size; x-- > 0;) {
        maximaFlagBuffer[x] = false;
      }
    }

    return maximaFlagBuffer;
  }

  private static int getBlocks(int max, int n) {
    final int blocks = (int) Math.ceil((1.0 * max) / n);
    return blocks;
  }

  /**
   * Truncate the array to the specified size.
   *
   * @param array the array
   * @param size the size
   * @return The truncated array
   */
  private static int[] truncate(int[] array, int size) {
    if (array.length == size) {
      return array;
    }
    if (size == 0) {
      return new int[0];
    }
    final int[] copy = new int[size];
    System.arraycopy(array, 0, copy, 0, size);
    return copy;
  }

  /**
   * Truncate the array to the specified size.
   *
   * @param array the array
   * @param size the size
   * @return The truncated array
   */
  private static int[][] truncate(int[][] array, int size) {
    if (array.length == size) {
      return array;
    }
    if (size == 0) {
      return new int[0][0];
    }
    final int[][] copy = new int[size][];
    System.arraycopy(array, 0, copy, 0, size);
    return copy;
  }

  /**
   * Neighbour checking performs an additional comparison between the local maxima within the block
   * (size n) and the neighbours within the 2n+1 perimeter. If any neighbour is already a maxima
   * then the local maxima within the block is eliminated. This step is only relevant when neighbour
   * data points have equal values since the search for maxima uses the &lt; operator. <p> Applies
   * to the blockFind algorithms.
   *
   * @param neighbourCheck Enable neighbour checking
   */
  public void setNeighbourCheck(boolean neighbourCheck) {
    this.neighbourCheck = neighbourCheck;
  }

  /**
   * @return True if neighbour checking is enabled
   */
  public boolean isNeighbourCheck() {
    return neighbourCheck;
  }

  /**
   * Allow the class to keep a data buffer for processing images with the blockFind3x3 algorithm
   *
   * @param dataBuffer Enable the data buffer
   */
  public void setDataBuffer(boolean dataBuffer) {
    this.dataBuffer = dataBuffer;
    if (!dataBuffer) {
      newDataFloat = null;
    }
  }

  /**
   * @return True if the data buffer is enabled
   */
  public boolean isBufferData() {
    return dataBuffer;
  }

  /** {@inheritDoc} */
  @Override
  public NonMaximumSuppression clone() {
    try {
      final NonMaximumSuppression o = (NonMaximumSuppression) super.clone();
      o.newDataFloat = null;
      o.newDataInt = null;
      o.maximaFlagBuffer = null;
      return o;
    } catch (final CloneNotSupportedException e) {
      // Ignore
    }
    return null;
  }

  // ----------------------------------------------------
  // NOTE:
  // The following code is copied directly from above.
  // All 'float' have been replaced with 'int'
  // ----------------------------------------------------

  /**
   * Compute the local-maxima within a 2n+1 block
   *
   * @param data The input data (packed in YX order)
   * @param maxx The width of the data
   * @param maxy The height of the data
   * @param n The block size
   * @return Indices to the local maxima (index = maxx * y + x)
   */
  public int[] maxFind(int[] data, int maxx, int maxy, int n) {
    final FixedIntList results = getResultsBuffer(data.length);
    final boolean[] maximaFlag = getFlagBuffer(data.length);

    // Boundary control
    final int xwidth = FastMath.min(n, maxx - 1);
    final int ywidth = FastMath.min(n, maxy - 1);
    final int xlimit = maxx - xwidth;
    final int ylimit = maxy - ywidth;

    final int[] offset = new int[(2 * xwidth + 1) * (2 * ywidth + 1) - 1];
    final int[] xoffset = new int[offset.length];
    final int[] yoffset = new int[offset.length];
    int d = 0;
    for (int y = -ywidth; y <= ywidth; y++) {
      for (int x = -xwidth; x <= xwidth; x++) {
        if (x != 0 || y != 0) {
          offset[d] = maxx * y + x;
          xoffset[d] = x;
          yoffset[d] = y;
          d++;
        }
      }
    }

    // Compare all points
    int index = 0;
    for (int y = 0; y < maxy; y++) {
      FIND_MAXIMUM: for (int x = 0; x < maxx; x++, index++) {
        final int v = data[index];

        // Flag to indicate this pixels has a complete (2n+1) neighbourhood
        final boolean isInnerXY = (y >= ywidth && y < ylimit) && (x >= xwidth && x < xlimit);

        // Sweep neighbourhood
        if (isInnerXY) {
          for (int i = 0; i < offset.length; i++) {
            if (maximaFlag[index + offset[i]]) {
              continue FIND_MAXIMUM;
            }
            if (data[index + offset[i]] > v) {
              continue FIND_MAXIMUM;
            }
          }
        } else {
          for (d = offset.length; d-- > 0;) {
            // Get the coords and check if it is within the data
            final int yy = y + yoffset[d];
            final int xx = x + xoffset[d];
            final boolean isWithin = (yy >= 0 && yy < maxy) && (xx >= 0 && xx < maxx);

            if (isWithin) {
              if (maximaFlag[index + offset[d]]) {
                continue FIND_MAXIMUM;
              }
              if (data[index + offset[d]] > v) {
                continue FIND_MAXIMUM;
              }
            }
          }
        }

        results.add(index);
        maximaFlag[index] = true;
      } // end FIND_MAXIMA
    }

    return results.toArray();
  }

  /**
   * Compute the local-maxima within a 2n+1 block An inner boundary of N is ignored as potential
   * maxima.
   *
   * @param data The input data (packed in YX order)
   * @param maxx The width of the data
   * @param maxy The height of the data
   * @param n The block size
   * @return Indices to the local maxima (index = maxx * y + x)
   */
  public int[] maxFindInternal(int[] data, int maxx, int maxy, int n) {
    final FixedIntList results = getResultsBuffer(data.length);
    final boolean[] maximaFlag = getFlagBuffer(data.length);

    // Boundary control
    final int xwidth = FastMath.min(n, maxx - 1);
    final int ywidth = FastMath.min(n, maxy - 1);

    final int[] offset = new int[(2 * xwidth + 1) * (2 * ywidth + 1) - 1];
    int d = 0;
    for (int y = -ywidth; y <= ywidth; y++) {
      for (int x = -xwidth; x <= xwidth; x++) {
        if (x != 0 || y != 0) {
          offset[d] = maxx * y + x;
          d++;
        }
      }
    }

    // Compare all points
    for (int y = n; y < maxy - n; y++) {
      int index = y * maxx + n;
      FIND_MAXIMUM: for (int x = n; x < maxx - n; x++, index++) {
        final int v = data[index];

        // Sweep neighbourhood -
        // No check for boundaries as this should be an internal sweep.
        for (int i = 0; i < offset.length; i++) {
          if (maximaFlag[index + offset[i]]) {
            continue FIND_MAXIMUM;
          }
          if (data[index + offset[i]] > v) {
            continue FIND_MAXIMUM;
          }
        }

        results.add(index);
        maximaFlag[index] = true;
      } // end FIND_MAXIMA
    }

    return results.toArray();
  }

  /**
   * Compute the local-maxima within a 2n+1 block An inner boundary is ignored as potential maxima.
   *
   * @param data The input data (packed in YX order)
   * @param maxx The width of the data
   * @param maxy The height of the data
   * @param n The block size
   * @param border The internal border
   * @return Indices to the local maxima (index = maxx * y + x)
   */
  public int[] maxFindInternal(int[] data, int maxx, int maxy, int n, int border) {
    if (n == border) {
      // Faster algorithm as there is no requirement for bounds checking.
      return maxFindInternal(data, maxx, maxy, n);
    }

    final FixedIntList results = getResultsBuffer(data.length);
    final boolean[] maximaFlag = getFlagBuffer(data.length);

    // Boundary control
    final int xwidth = FastMath.min(n, maxx - 1);
    final int ywidth = FastMath.min(n, maxy - 1);
    final int xlimit = maxx - xwidth;
    final int ylimit = maxy - ywidth;

    final int[] offset = new int[(2 * xwidth + 1) * (2 * ywidth + 1) - 1];
    final int[] xoffset = new int[offset.length];
    final int[] yoffset = new int[offset.length];
    int d = 0;
    for (int y = -ywidth; y <= ywidth; y++) {
      for (int x = -xwidth; x <= xwidth; x++) {
        if (x != 0 || y != 0) {
          offset[d] = maxx * y + x;
          xoffset[d] = x;
          yoffset[d] = y;
          d++;
        }
      }
    }

    // All blocks fit within the border
    final boolean inner = (n < border);

    // Compare all points
    for (int y = border; y < maxy - border; y++) {
      int index = y * maxx + border;
      FIND_MAXIMUM: for (int x = border; x < maxx - border; x++, index++) {
        final int v = data[index];

        if (inner) {
          for (int i = 0; i < offset.length; i++) {
            if (maximaFlag[index + offset[i]]) {
              continue FIND_MAXIMUM;
            }
            if (data[index + offset[i]] > v) {
              continue FIND_MAXIMUM;
            }
          }
        } else {
          // Flag to indicate this pixels has a complete (2n+1) neighbourhood
          final boolean isInnerXY = (y >= ywidth && y < ylimit) && (x >= xwidth && x < xlimit);

          // Sweep neighbourhood
          for (d = offset.length; d-- > 0;) {
            boolean isWithin = isInnerXY;
            if (!isWithin) {
              // Get the coords and check if it is within the data
              final int yy = y + yoffset[d];
              final int xx = x + xoffset[d];
              isWithin = (yy >= 0 && yy < maxy) && (xx >= 0 && xx < maxx);
            }

            if (isWithin) {
              if (maximaFlag[index + offset[d]]) {
                continue FIND_MAXIMUM;
              }
              if (data[index + offset[d]] > v) {
                continue FIND_MAXIMUM;
              }
            }
          }
        }

        results.add(index);
        maximaFlag[index] = true;
      } // end FIND_MAXIMA
    }

    return results.toArray();
  }

  /**
   * Compute the local-maxima within a 2n+1 block. <p> Uses the 2D block algorithm of Neubeck and
   * Van Gool (2006).
   *
   * @param data The input data (packed in YX order)
   * @param maxx The width of the data
   * @param maxy The height of the data
   * @param n The block size
   * @return Indices to the local maxima (index = maxx * y + x)
   */
  public int[] blockFind(int[] data, int maxx, int maxy, int n) {
    if (n == 1) {
      // optimised version for the special case
      return blockFind3x3(data, maxx, maxy);
    }

    return blockFindNxN(data, maxx, maxy, n);
  }

  /**
   * Compute the local-maxima within a 2n+1 block. An inner boundary of N is ignored as potential
   * maxima. <p> Uses the 2D block algorithm of Neubeck and Van Gool (2006).
   *
   * @param data The input data (packed in YX order)
   * @param maxx The width of the data
   * @param maxy The height of the data
   * @param n The block size
   * @param border The internal border
   * @return Indices to the local maxima (index = maxx * y + x)
   */
  public int[] blockFindInternal(int[] data, int maxx, int maxy, int n, int border) {
    if (n == 1) {
      // optimised version for the special case
      return blockFind3x3Internal(data, maxx, maxy, border);
    }

    return blockFindNxNInternal(data, maxx, maxy, n, border);
  }

  /**
   * Compute the local-maxima within a 2n+1 block. <p> Uses the 2D block algorithm of Neubeck and
   * Van Gool (2006).
   *
   * @param data The input data (packed in YX order)
   * @param maxx The width of the data
   * @param maxy The height of the data
   * @param n The block size
   * @return Indices to the local maxima (index = maxx * y + x)
   */
  public int[] blockFindNxN(int[] data, int maxx, int maxy, int n) {
    int[] maxima;
    int nMaxima = 0;
    final int n1 = n + 1;

    if (isNeighbourCheck()) {
      // Compute all candidate maxima in the NxN blocks. Then check each candidate
      // is a maxima in the 2N+1 region including a check for existing maxima.
      final int[][] blockMaximaCandidates = findBlockMaximaCandidatesNxN(data, maxx, maxy, n);

      maxima = new int[blockMaximaCandidates.length];
      final boolean[] maximaFlag = getFlagBuffer(data.length);

      for (final int[] blockMaxima : blockMaximaCandidates) {
        FIND_MAXIMUM: for (final int index : blockMaxima) {
          final int v = data[index];

          final int mi = index % maxx;
          final int mj = index / maxx;

          // Compare the maxima to the surroundings. Ignore the block region already processed.
          //
          // .......(mi-n,mj-n)----------------------------------(mi+n,mj-n)
          // ............|aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa|
          // ............|aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa|
          // ............|aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa|
          // ............|aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa|
          // ............|aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa|
          // ............|bbbbbbbb.(i,j)-------------(i+n,j).ccccccccc|
          // ............|bbbbbbbbbbb|..................|ccccccccccccc|
          // ............|bbbbbbbbbbb|..................|ccccccccccccc|
          // ............|bbbbbbbbbbb|.....(mi,mj)......|ccccccccccccc|
          // ............|bbbbbbbbbbb|..................|ccccccccccccc|
          // ............|bbbbbbbbbbb|..................|ccccccccccccc|
          // ............|bbbbbbbbbbb|..................|ccccccccccccc|
          // ............|bbbbbbbbbbb|..................|ccccccccccccc|
          // ............|bbbbbbbb.(i,j+n)-----------(i+n,j+n).ccccccc|
          // ............|dddddddddddddddddddddddddddddddddddddddddddd|
          // ............|dddddddddddddddddddddddddddddddddddddddddddd|
          // ............|dddddddddddddddddddddddddddddddddddddddddddd|
          // ......(mi-n,mj+n)----------------------------------(mi+n,mj+n)
          //
          // This must be done without over-running boundaries
          // int steps = 0;
          final int j = n1 * (mj / n1);
          final int mi_minus_n = FastMath.max(mi - n, 0);
          final int mi_plus_n = FastMath.min(mi + n, maxx - 1);
          final int mj_minus_n = FastMath.max(mj - n, 0);

          // A
          for (int jj = mj_minus_n; jj < j; jj++) {
            int indexStart = jj * maxx + mi_minus_n;
            final int indexEnd = jj * maxx + mi_plus_n;
            for (; indexStart <= indexEnd; indexStart++) {
              // steps++;
              if (v < data[indexStart]) {
                continue FIND_MAXIMUM;
              }
            }
          }
          final int i = n1 * (mi / n1);
          final int i_plus_n = i + n1;
          final int j_plus_n = FastMath.min(j + n, maxy - 1);
          for (int jj = j; jj <= j_plus_n; jj++) {
            // B
            {
              int indexStart = jj * maxx + mi_minus_n;
              final int indexEnd = jj * maxx + i;
              for (; indexStart < indexEnd; indexStart++) {
                // steps++;
                if (v < data[indexStart]) {
                  continue FIND_MAXIMUM;
                }
              }
            }

            // C
            {
              int indexStart = jj * maxx + i_plus_n;
              final int indexEnd = jj * maxx + mi_plus_n;
              for (; indexStart <= indexEnd; indexStart++) {
                // steps++;
                if (v < data[indexStart]) {
                  continue FIND_MAXIMUM;
                }
              }
            }
          }
          // D
          final int mj_plus_n = FastMath.min(mj + n, maxy - 1);
          for (int jj = j_plus_n + 1; jj <= mj_plus_n; jj++) {
            int indexStart = jj * maxx + mi_minus_n;
            final int indexEnd = jj * maxx + mi_plus_n;
            for (; indexStart <= indexEnd; indexStart++) {
              // steps++;
              if (v < data[indexStart]) {
                continue FIND_MAXIMUM;
              }
            }
          }

          // Neighbour check for existing maxima. This is relevant if the same height.
          // However we cannot just check height as the neighbour may not be the selected maxima
          // within its block.
          // Only check A+B since the blocks for C+D have not yet been processed

          // TODO: We only need to check the maxima of the preceding blocks:
          // This requires iteration over the blockMaximaCandidates as a grid.
          // - Find the maxima for the preceding blocks...
          // - Check if within N distance
          // - Check if equal (higher check has already been done)

          // A
          for (int jj = mj_minus_n; jj < j; jj++) {
            int indexStart = jj * maxx + mi_minus_n;
            final int indexEnd = jj * maxx + mi_plus_n;
            for (; indexStart <= indexEnd; indexStart++) {
              // steps++;
              if (maximaFlag[indexStart]) {
                continue FIND_MAXIMUM;
              }
            }
          }
          for (int jj = j; jj <= j_plus_n; jj++)
          // B
          {
            int indexStart = jj * maxx + mi_minus_n;
            final int indexEnd = jj * maxx + i;
            for (; indexStart < indexEnd; indexStart++) {
              // steps++;
              if (maximaFlag[indexStart]) {
                continue FIND_MAXIMUM;
              }
            }
          }

          // System.out.printf("%.2f @ %d,%d. Steps = %d\n", v, x, y, steps);

          maximaFlag[index] = true;
          maxima[nMaxima++] = index;
          break;
        } // end FIND_MAXIMA
      }
    } else {
      final int[] blockMaxima = findBlockMaximaNxN(data, maxx, maxy, n);

      maxima = blockMaxima; // Re-use storage space

      FIND_MAXIMUM: for (final int index : blockMaxima) {
        final int v = data[index];

        final int mi = index % maxx;
        final int mj = index / maxx;

        // Compare the maxima to the surroundings. Ignore the block region already processed.
        //
        // .......(mi-n,mj-n)----------------------------------(mi+n,mj-n)
        // ............|aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa|
        // ............|aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa|
        // ............|aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa|
        // ............|aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa|
        // ............|aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa|
        // ............|bbbbbbbb.(i,j)-------------(i+n,j).ccccccccc|
        // ............|bbbbbbbbbbb|..................|ccccccccccccc|
        // ............|bbbbbbbbbbb|..................|ccccccccccccc|
        // ............|bbbbbbbbbbb|.....(mi,mj)......|ccccccccccccc|
        // ............|bbbbbbbbbbb|..................|ccccccccccccc|
        // ............|bbbbbbbbbbb|..................|ccccccccccccc|
        // ............|bbbbbbbbbbb|..................|ccccccccccccc|
        // ............|bbbbbbbbbbb|..................|ccccccccccccc|
        // ............|bbbbbbbb.(i,j+n)-----------(i+n,j+n).ccccccc|
        // ............|dddddddddddddddddddddddddddddddddddddddddddd|
        // ............|dddddddddddddddddddddddddddddddddddddddddddd|
        // ............|dddddddddddddddddddddddddddddddddddddddddddd|
        // ......(mi-n,mj+n)----------------------------------(mi+n,mj+n)
        //
        // This must be done without over-running boundaries
        // int steps = 0;
        final int j = n1 * (mj / n1);
        final int mi_minus_n = FastMath.max(mi - n, 0);
        final int mi_plus_n = FastMath.min(mi + n, maxx - 1);
        final int mj_minus_n = FastMath.max(mj - n, 0);

        // A
        for (int jj = mj_minus_n; jj < j; jj++) {
          int indexStart = jj * maxx + mi_minus_n;
          final int indexEnd = jj * maxx + mi_plus_n;
          for (; indexStart <= indexEnd; indexStart++) {
            // steps++;
            if (v < data[indexStart]) {
              continue FIND_MAXIMUM;
            }
          }
        }
        final int i = n1 * (mi / n1);
        final int i_plus_n = i + n1;
        final int j_plus_n = FastMath.min(j + n, maxy - 1);
        for (int jj = j; jj <= j_plus_n; jj++) {
          // B
          {
            int indexStart = jj * maxx + mi_minus_n;
            final int indexEnd = jj * maxx + i;
            for (; indexStart < indexEnd; indexStart++) {
              // steps++;
              if (v < data[indexStart]) {
                continue FIND_MAXIMUM;
              }
            }
          }

          // C
          {
            int indexStart = jj * maxx + i_plus_n;
            final int indexEnd = jj * maxx + mi_plus_n;
            for (; indexStart <= indexEnd; indexStart++) {
              // steps++;
              if (v < data[indexStart]) {
                continue FIND_MAXIMUM;
              }
            }
          }
        }
        // D
        final int mj_plus_n = FastMath.min(mj + n, maxy - 1);
        for (int jj = j_plus_n + 1; jj <= mj_plus_n; jj++) {
          int indexStart = jj * maxx + mi_minus_n;
          final int indexEnd = jj * maxx + mi_plus_n;
          for (; indexStart <= indexEnd; indexStart++) {
            // steps++;
            if (v < data[indexStart]) {
              continue FIND_MAXIMUM;
            }
          }
        }
        // System.out.printf("%.2f @ %d,%d. Steps = %d\n", v, x, y, steps);

        maxima[nMaxima++] = index;
      } // end FIND_MAXIMA
    }

    return truncate(maxima, nMaxima);
  }

  /**
   * Compute the local-maxima within a 2n+1 block. An inner boundary of N is ignored as potential
   * maxima. <p> Uses the 2D block algorithm of Neubeck and Van Gool (2006).
   *
   * @param data The input data (packed in YX order)
   * @param maxx The width of the data
   * @param maxy The height of the data
   * @param n The block size
   * @param border The internal border
   * @return Indices to the local maxima (index = maxx * y + x)
   */
  public int[] blockFindNxNInternal(int[] data, int maxx, int maxy, int n, int border) {
    int[] maxima;
    int nMaxima = 0;
    final int n1 = n + 1;

    if (isNeighbourCheck()) {
      // Compute all candidate maxima in the NxN blocks. Then check each candidate
      // is a maxima in the 2N+1 region including a check for existing maxima.
      final int[][] blockMaximaCandidates =
          findBlockMaximaCandidatesNxNInternal(data, maxx, maxy, n, border);

      maxima = new int[blockMaximaCandidates.length];
      final boolean[] maximaFlag = getFlagBuffer(data.length);

      for (final int[] blockMaxima : blockMaximaCandidates) {
        FIND_MAXIMUM: for (final int index : blockMaxima) {
          final int v = data[index];

          final int mi = index % maxx;
          final int mj = index / maxx;

          // Compare the maxima to the surroundings. Ignore the block region already processed.
          //
          // .......(mi-n,mj-n)----------------------------------(mi+n,mj-n)
          // ............|aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa|
          // ............|aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa|
          // ............|aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa|
          // ............|aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa|
          // ............|aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa|
          // ............|bbbbbbbb.(i,j)-------------(i+n,j).ccccccccc|
          // ............|bbbbbbbbbbb|..................|ccccccccccccc|
          // ............|bbbbbbbbbbb|..................|ccccccccccccc|
          // ............|bbbbbbbbbbb|.....(mi,mj)......|ccccccccccccc|
          // ............|bbbbbbbbbbb|..................|ccccccccccccc|
          // ............|bbbbbbbbbbb|..................|ccccccccccccc|
          // ............|bbbbbbbbbbb|..................|ccccccccccccc|
          // ............|bbbbbbbbbbb|..................|ccccccccccccc|
          // ............|bbbbbbbb.(i,j+n)-----------(i+n,j+n).ccccccc|
          // ............|dddddddddddddddddddddddddddddddddddddddddddd|
          // ............|dddddddddddddddddddddddddddddddddddddddddddd|
          // ............|dddddddddddddddddddddddddddddddddddddddddddd|
          // ......(mi-n,mj+n)----------------------------------(mi+n,mj+n)
          //
          // No check for over-running boundaries since this is the internal version
          // int steps = 0;
          final int j = n1 * ((mj - border) / n1) + border; // Blocks n+1 wide
          // The block boundaries will have been truncated on the final block. Ensure this is swept
          final int mi_minus_n = FastMath.max(mi - n, 0);
          final int mi_plus_n = FastMath.min(mi + n, maxx - 1);
          final int mj_minus_n = FastMath.max(mj - n, 0);

          // System.out.printf("Block [%d,%d] => [%d,%d]\n", x, y, i, j);

          // A
          for (int jj = mj_minus_n; jj < j; jj++) {
            int indexStart = jj * maxx + mi_minus_n;
            final int indexEnd = jj * maxx + mi_plus_n;
            for (; indexStart <= indexEnd; indexStart++) {
              // steps++;
              if (v < data[indexStart]) {
                continue FIND_MAXIMUM;
              }
            }
          }
          final int i = n1 * ((mi - border) / n1) + border; // Blocks n+1 wide
          final int i_plus_n = i + n1;
          final int j_plus_n = FastMath.min(j + n, maxy - border - 1);
          for (int jj = j; jj <= j_plus_n; jj++) {
            // B
            {
              int indexStart = jj * maxx + mi_minus_n;
              final int indexEnd = jj * maxx + i;
              for (; indexStart < indexEnd; indexStart++) {
                // steps++;
                if (v < data[indexStart]) {
                  continue FIND_MAXIMUM;
                }
              }
            }

            // C
            {
              int indexStart = jj * maxx + i_plus_n;
              final int indexEnd = jj * maxx + mi_plus_n;
              for (; indexStart <= indexEnd; indexStart++) {
                // steps++;
                if (v < data[indexStart]) {
                  continue FIND_MAXIMUM;
                }
              }
            }
          }
          // D
          final int mj_plus_n = FastMath.min(mj + n, maxy - 1);
          for (int jj = j_plus_n + 1; jj <= mj_plus_n; jj++) {
            int indexStart = jj * maxx + mi_minus_n;
            final int indexEnd = jj * maxx + mi_plus_n;
            for (; indexStart <= indexEnd; indexStart++) {
              // steps++;
              if (v < data[indexStart]) {
                continue FIND_MAXIMUM;
              }
            }
          }
          // System.out.printf("%.2f @ %d,%d. Steps = %d, max = %b\n", v, x, y, steps, isMax);

          // Neighbour check for existing maxima. This is relevant if the same height.
          // However we cannot just check height as the neighbour may not be the selected maxima
          // within its block.
          // Only check A+B since the blocks for C+D have not yet been processed

          // TODO: We only need to check the maxima of the preceding blocks:
          // This requires iteration over the blockMaximaCandidates as a grid.
          // - Find the maxima for the preceding blocks...
          // - Check if within N distance
          // - Check if equal (higher check has already been done)

          // A
          for (int jj = mj_minus_n; jj < j; jj++) {
            int indexStart = jj * maxx + mi_minus_n;
            final int indexEnd = jj * maxx + mi_plus_n;
            for (; indexStart <= indexEnd; indexStart++) {
              // steps++;
              if (maximaFlag[indexStart]) {
                continue FIND_MAXIMUM;
              }
            }
          }
          for (int jj = j; jj <= j_plus_n; jj++)
          // B
          {
            int indexStart = jj * maxx + mi_minus_n;
            final int indexEnd = jj * maxx + i;
            for (; indexStart < indexEnd; indexStart++) {
              // steps++;
              if (maximaFlag[indexStart]) {
                continue FIND_MAXIMUM;
              }
            }
          }

          // System.out.printf("%.2f @ %d,%d. Steps = %d\n", v, x, y, steps);

          maximaFlag[index] = true;
          maxima[nMaxima++] = index;
          break;
        } // end FIND_MAXIMA
      }
    } else {
      final int[] blockMaxima = findBlockMaximaNxNInternal(data, maxx, maxy, n, border);

      maxima = blockMaxima; // Re-use storage space

      FIND_MAXIMUM: for (final int index : blockMaxima) {
        final int v = data[index];

        final int mi = index % maxx;
        final int mj = index / maxx;

        // Compare the maxima to the surroundings. Ignore the block region already processed.
        //
        // .......(mi-n,mj-n)----------------------------------(mi+n,mj-n)
        // ............|aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa|
        // ............|aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa|
        // ............|aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa|
        // ............|aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa|
        // ............|aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa|
        // ............|bbbbbbbb.(i,j)-------------(i+n,j).ccccccccc|
        // ............|bbbbbbbbbbb|..................|ccccccccccccc|
        // ............|bbbbbbbbbbb|..................|ccccccccccccc|
        // ............|bbbbbbbbbbb|.....(mi,mj)......|ccccccccccccc|
        // ............|bbbbbbbbbbb|..................|ccccccccccccc|
        // ............|bbbbbbbbbbb|..................|ccccccccccccc|
        // ............|bbbbbbbbbbb|..................|ccccccccccccc|
        // ............|bbbbbbbbbbb|..................|ccccccccccccc|
        // ............|bbbbbbbb.(i,j+n)-----------(i+n,j+n).ccccccc|
        // ............|dddddddddddddddddddddddddddddddddddddddddddd|
        // ............|dddddddddddddddddddddddddddddddddddddddddddd|
        // ............|dddddddddddddddddddddddddddddddddddddddddddd|
        // ......(mi-n,mj+n)----------------------------------(mi+n,mj+n)
        //
        // No check for over-running boundaries since this is the internal version
        // int steps = 0;
        final int j = n1 * ((mj - border) / n1) + border; // Blocks n+1 wide
        // The block boundaries will have been truncated on the final block. Ensure this is swept
        final int mi_minus_n = FastMath.max(mi - n, 0);
        final int mi_plus_n = FastMath.min(mi + n, maxx - 1);
        final int mj_minus_n = FastMath.max(mj - n, 0);

        // System.out.printf("Block [%d,%d] => [%d,%d]\n", x, y, i, j);

        // A
        for (int jj = mj_minus_n; jj < j; jj++) {
          int indexStart = jj * maxx + mi_minus_n;
          final int indexEnd = jj * maxx + mi_plus_n;
          for (; indexStart <= indexEnd; indexStart++) {
            // steps++;
            if (v < data[indexStart]) {
              continue FIND_MAXIMUM;
            }
          }
        }
        final int i = n1 * ((mi - border) / n1) + border; // Blocks n+1 wide
        final int i_plus_n = i + n1;
        final int j_plus_n = FastMath.min(j + n, maxy - border - 1);
        for (int jj = j; jj <= j_plus_n; jj++) {
          // B
          {
            int indexStart = jj * maxx + mi_minus_n;
            final int indexEnd = jj * maxx + i;
            for (; indexStart < indexEnd; indexStart++) {
              // steps++;
              if (v < data[indexStart]) {
                continue FIND_MAXIMUM;
              }
            }
          }

          // C
          {
            int indexStart = jj * maxx + i_plus_n;
            final int indexEnd = jj * maxx + mi_plus_n;
            for (; indexStart <= indexEnd; indexStart++) {
              // steps++;
              if (v < data[indexStart]) {
                continue FIND_MAXIMUM;
              }
            }
          }
        }
        // D
        final int mj_plus_n = FastMath.min(mj + n, maxy - 1);
        for (int jj = j_plus_n + 1; jj <= mj_plus_n; jj++) {
          int indexStart = jj * maxx + mi_minus_n;
          final int indexEnd = jj * maxx + mi_plus_n;
          for (; indexStart <= indexEnd; indexStart++) {
            // steps++;
            if (v < data[indexStart]) {
              continue FIND_MAXIMUM;
            }
          }
        }
        // System.out.printf("%.2f @ %d,%d. Steps = %d, max = %b\n", v, x, y, steps, isMax);

        maxima[nMaxima++] = index;
      } // end FIND_MAXIMA
    }

    return truncate(maxima, nMaxima);
  }

  /**
   * Search the data for the index of the maximum in each block of size n*n. <p> E.g. Max [ (i,i+n)
   * x (i,j+n) ] for (i=0; i&lt;maxx; i+=n) x (j=0, j&lt;maxy; j+=n)
   *
   * @param data The input data (packed in YX order)
   * @param maxx The width of the data
   * @param maxy The height of the data
   * @param n The block size
   * @return The maxima indices
   */
  public int[] findBlockMaximaNxN(int[] data, int maxx, int maxy, int n) {
    // Include the actual pixel in the block
    // This makes the block search inclusive: i=0; i<=n; i++
    n++;

    // The number of blocks in x and y
    final int xblocks = getBlocks(maxx, n);
    final int yblocks = getBlocks(maxy, n);

    // The final index in each dimension (where an incomplete block is found).
    // This equals maxx/maxy if the number of blocks fits exactly.
    final int xfinal = n * (maxx / n);
    final int yfinal = n * (maxy / n);

    final int[] maxima = new int[xblocks * yblocks];

    int block = 0;
    for (int y = 0; y < maxy; y += n) {
      for (int x = 0; x < maxx; x += n) {
        // Find the sweep size in each direction
        final int xsize = (x != xfinal) ? n : maxx - xfinal;
        int ysize = (y != yfinal) ? n : maxy - yfinal;

        int index = y * maxx + x;
        int maxIndex = index;
        int max = data[maxIndex];

        while (ysize-- > 0) {
          for (int x2 = xsize; x2-- > 0;) {
            if (max < data[index]) {
              max = data[index];
              maxIndex = index;
            }
            index++;
          }
          index += maxx - xsize;
        }

        maxima[block++] = maxIndex;
      }
    }

    return maxima;
  }

  /**
   * Search the data for the index of the maximum in each block of size n*n. An inner boundary of N
   * is ignored as potential maxima. <p> E.g. Max [ (i,i+n) x (i,j+n) ] for (i=n; i&lt;maxx-n; i+=n)
   * x (j=n, j&lt;maxy-n; j+=n)
   *
   * @param data The input data (packed in YX order)
   * @param maxx The width of the data
   * @param maxy The height of the data
   * @param n The block size
   * @param border The internal border
   * @return The maxima indices
   */
  public int[] findBlockMaximaNxNInternal(int[] data, int maxx, int maxy, int n, int border) {
    // Include the actual pixel in the block
    // This makes the block search inclusive: i=0; i<=n; i++
    n++;

    // The number of blocks in x and y. Subtract the boundary blocks.
    final int xblocks = getBlocks(maxx - border, n);
    final int yblocks = getBlocks(maxy - border, n);

    if (xblocks < 1 || yblocks < 1) {
      return new int[0];
    }

    // The final index in each dimension (where an incomplete block is found).
    // This equals maxx/maxy if the number of blocks fits exactly.
    final int xfinal = n * ((maxx - 2 * border) / n) + border;
    final int yfinal = n * ((maxy - 2 * border) / n) + border;

    final int[] maxima = new int[xblocks * yblocks];

    int block = 0;
    for (int y = border; y < maxy - border; y += n) {
      for (int x = border; x < maxx - border; x += n) {
        // System.out.printf("Block [%d,%d]\n", x, y);

        // Find the sweep size in each direction
        final int xsize = (x != xfinal) ? n : maxx - xfinal - border;
        int ysize = (y != yfinal) ? n : maxy - yfinal - border;

        int index = y * maxx + x;
        int maxIndex = index;
        int max = data[maxIndex];

        while (ysize-- > 0) {
          for (int x2 = xsize; x2-- > 0;) {
            if (max < data[index]) {
              max = data[index];
              maxIndex = index;
            }
            index++;
          }
          index += maxx - xsize;
        }

        maxima[block++] = maxIndex;
      }
    }

    return truncate(maxima, block);
  }

  /**
   * Search the data for the index of the maximum in each block of size 2*2. <p> E.g. Max [ (i,i+n)
   * x (i,j+n) ] for (i=0; i&lt;maxx; i+=n) x (j=0, j&lt;maxy; j+=n)
   *
   * @param data The input data (packed in YX order)
   * @param maxx The width of the data
   * @param maxy The height of the data
   * @return The maxima indices
   */
  public int[] findBlockMaxima2x2(int[] data, int maxx, int maxy) {
    // Optimised for 2x2 block
    // final int n = 2;

    // The number of blocks in x and y
    final int xblocks = getBlocks(maxx, 2);
    final int yblocks = getBlocks(maxy, 2);

    // The final index in each dimension (where an incomplete block is found).
    // This equals maxx/maxy if the number of blocks fits exactly.
    final int xfinal = 2 * (maxx / 2);
    final int yfinal = 2 * (maxy / 2);

    // TODO - Try this by expanding the data if xfinal != maxx || yfinal != maxy
    // This will allow less management of boundaries.

    final int[] maxima = new int[xblocks * yblocks];

    // Sweep 4 regions:
    // x.............xfinal
    // |.............|..maxx
    // |.............|..|
    // aaaaaaaaaaaaaaabbb-- y
    // aaaaaaaaaaaaaaabbb
    // aaaaaaaaaaaaaaabbb
    // aaaaaaaaaaaaaaabbb
    // aaaaaaaaaaaaaaabbb
    // aaaaaaaaaaaaaaabbb
    // aaaaaaaaaaaaaaabbb
    // aaaaaaaaaaaaaaabbb--yfinal
    // cccccccccccccccddd
    // cccccccccccccccddd--maxy

    int block = 0;
    for (int y = 0; y < yfinal; y += 2) {
      // A
      int xindex = y * maxx;
      for (int x = 0; x < xfinal; x += 2) {
        // Compare 2x2 block
        final int max1 = (data[xindex] > data[xindex + 1]) ? xindex : xindex + 1;
        final int max2 =
            (data[xindex + maxx] > data[xindex + maxx + 1]) ? xindex + maxx : xindex + maxx + 1;

        maxima[block++] = (data[max1] > data[max2]) ? max1 : max2;

        xindex += 2;
      }
      // B
      if (xfinal != maxx) {
        // Compare 1x2 block
        final int index = y * maxx + xfinal;
        maxima[block++] = (data[index] > data[index + maxx]) ? index : index + maxx;
      }
    }
    if (yfinal != maxy) {
      // C
      int xindex = yfinal * maxx;
      for (int x = 0; x < xfinal; x += 2) {
        // Compare 2x1 block
        maxima[block++] = (data[xindex] > data[xindex + 1]) ? xindex : xindex + 1;
        xindex += 2;
      }
      // D
      if (xfinal != maxx) {
        // Compare 1x1 block
        maxima[block++] = yfinal * maxx + xfinal;
      }
    }

    return maxima;
  }

  /**
   * Search the data for the index of the maximum in each block of size n*n. <p> E.g. Max [ (i,i+n)
   * x (i,j+n) ] for (i=0; i&lt;maxx; i+=n) x (j=0, j&lt;maxy; j+=n) <p> If multiple indices within
   * the block have the same value then all are returned.
   *
   * @param data The input data (packed in YX order)
   * @param maxx The width of the data
   * @param maxy The height of the data
   * @param n The block size
   * @return The maxima indices
   */
  public int[][] findBlockMaximaCandidatesNxN(int[] data, int maxx, int maxy, int n) {
    // Include the actual pixel in the block
    // This makes the block search inclusive: i=0; i<=n; i++
    n++;

    // The number of blocks in x and y
    final int xblocks = getBlocks(maxx, n);
    final int yblocks = getBlocks(maxy, n);

    // The final index in each dimension (where an incomplete block is found).
    // This equals maxx/maxy if the number of blocks fits exactly.
    final int xfinal = n * (maxx / n);
    final int yfinal = n * (maxy / n);

    final int[][] maxima = new int[xblocks * yblocks][];
    final FixedIntList list = new FixedIntList(n * n);

    int block = 0;
    for (int y = 0; y < maxy; y += n) {
      for (int x = 0; x < maxx; x += n) {
        // Find the sweep size in each direction
        final int xsize = (x != xfinal) ? n : maxx - xfinal;
        int ysize = (y != yfinal) ? n : maxy - yfinal;

        int index = y * maxx + x;
        int max = intMin;

        while (ysize-- > 0) {
          for (int x2 = xsize; x2-- > 0;) {
            if (max < data[index]) {
              list.clear();
              list.add(index);
              max = data[index];
            } else if (max == data[index]) {
              list.add(index);
            }
            index++;
          }
          index += maxx - xsize;
        }

        maxima[block++] = list.toArray();
      }
    }

    return maxima;
  }

  /**
   * Search the data for the index of the maximum in each block of size n*n. An inner boundary of N
   * is ignored as potential maxima. <p> E.g. Max [ (i,i+n) x (i,j+n) ] for (i=n; i&lt;maxx-n; i+=n)
   * x (j=n, j&lt;maxy-n; j+=n) <p> If multiple indices within the block have the same value then
   * all are returned.
   *
   * @param data The input data (packed in YX order)
   * @param maxx The width of the data
   * @param maxy The height of the data
   * @param n The block size
   * @param border The internal border
   * @return The maxima indices
   */
  public int[][] findBlockMaximaCandidatesNxNInternal(int[] data, int maxx, int maxy, int n,
      int border) {
    // Include the actual pixel in the block
    // This makes the block search inclusive: i=0; i<=n; i++
    n++;

    // The number of blocks in x and y. Subtract the boundary blocks.
    final int xblocks = getBlocks(maxx - border, n);
    final int yblocks = getBlocks(maxy - border, n);

    if (xblocks < 1 || yblocks < 1) {
      return new int[0][0];
    }

    // The final index in each dimension (where an incomplete block is found).
    // This equals maxx/maxy if the number of blocks fits exactly.
    final int xfinal = n * ((maxx - 2 * border) / n) + border;
    final int yfinal = n * ((maxy - 2 * border) / n) + border;

    final int[][] maxima = new int[xblocks * yblocks][];
    final FixedIntList list = new FixedIntList(n * n);

    int block = 0;
    for (int y = border; y < maxy - border; y += n) {
      for (int x = border; x < maxx - border; x += n) {
        // System.out.printf("Block [%d,%d]\n", x, y);

        // Find the sweep size in each direction
        final int xsize = (x != xfinal) ? n : maxx - xfinal - border;
        int ysize = (y != yfinal) ? n : maxy - yfinal - border;

        int index = y * maxx + x;
        int max = intMin;

        while (ysize-- > 0) {
          for (int x2 = xsize; x2-- > 0;) {
            if (max < data[index]) {
              list.clear();
              list.add(index);
              max = data[index];
            } else if (max == data[index]) {
              list.add(index);
            }
            index++;
          }
          index += maxx - xsize;
        }

        maxima[block++] = list.toArray();
      }
    }

    return truncate(maxima, block);
  }

  /**
   * Search the data for the index of the maximum in each block of size 2*2. <p> E.g. Max [ (i,i+n)
   * x (i,j+n) ] for (i=0; i&lt;maxx; i+=n) x (j=0, j&lt;maxy; j+=n) <p> If multiple indices within
   * the block have the same value then all are returned.
   *
   * @param data The input data (packed in YX order)
   * @param maxx The width of the data
   * @param maxy The height of the data
   * @return The maxima indices
   */
  public int[][] findBlockMaximaCandidates2x2(int[] data, int maxx, int maxy) {
    // Optimised for 2x2 block
    // final int n = 2;

    // The number of blocks in x and y
    final int xblocks = getBlocks(maxx, 2);
    final int yblocks = getBlocks(maxy, 2);

    // The final index in each dimension (where an incomplete block is found).
    // This equals maxx/maxy if the number of blocks fits exactly.
    final int xfinal = 2 * (maxx / 2);
    final int yfinal = 2 * (maxy / 2);

    // TODO - Try this by expanding the data if xfinal != maxx || yfinal != maxy
    // This will allow less management of boundaries.

    final int[][] maxima = new int[xblocks * yblocks][];

    // Sweep 4 regions:
    // x.............xfinal
    // |.............|..maxx
    // |.............|..|
    // aaaaaaaaaaaaaaabbb-- y
    // aaaaaaaaaaaaaaabbb
    // aaaaaaaaaaaaaaabbb
    // aaaaaaaaaaaaaaabbb
    // aaaaaaaaaaaaaaabbb
    // aaaaaaaaaaaaaaabbb
    // aaaaaaaaaaaaaaabbb
    // aaaaaaaaaaaaaaabbb--yfinal
    // cccccccccccccccddd
    // cccccccccccccccddd--maxy

    int block = 0;
    for (int y = 0; y < yfinal; y += 2) {
      // A
      int xindex = y * maxx;
      for (int x = 0; x < xfinal; x += 2) {
        // Compare 2x2 block
        final int[] i1 = getIndices(data, xindex, xindex + 1);
        final int[] i2 = getIndices(data, xindex + maxx, xindex + maxx + 1);

        if (data[i1[0]] > data[i2[0]]) {
          maxima[block++] = i1;
        } else if (data[i1[0]] == data[i2[0]]) {
          if (i1.length == 1) {
            if (i2.length == 1) {
              maxima[block++] = new int[] {i1[0], i2[0]};
            } else {
              maxima[block++] = new int[] {i1[0], i2[0], i2[1]};
            }
          } else if (i2.length == 1) {
            maxima[block++] = new int[] {i1[0], i1[1], i2[0]};
          } else {
            maxima[block++] = new int[] {i1[0], i1[1], i2[0], i2[1]};
          }
        } else {
          maxima[block++] = i2;
        }
        xindex += 2;
      }
      // B
      if (xfinal != maxx) {
        // Compare 1x2 block
        final int index = y * maxx + xfinal;
        maxima[block++] = getIndices(data, index, index + maxx);
      }
    }
    if (yfinal != maxy) {
      // C
      int xindex = yfinal * maxx;
      for (int x = 0; x < xfinal; x += 2) {
        // Compare 2x1 block
        maxima[block++] = getIndices(data, xindex, xindex + 1);
        xindex += 2;
      }
      // D
      if (xfinal != maxx) {
        // Compare 1x1 block
        maxima[block++] = new int[] {yfinal * maxx + xfinal};
      }
    }

    return maxima;
  }

  private static int[] getIndices(int[] data, int i, int j) {
    if (data[i] > data[j]) {
      return new int[] {i};
    } else if (data[i] == data[j]) {
      return new int[] {i, j};
    }
    return new int[] {j};
  }

  /**
   * Compute the local-maxima within a 3x3 block
   *
   * @param data The input data (packed in YX order)
   * @param maxx The width of the data
   * @param maxy The height of the data
   * @return Indices to the local maxima (index = maxx * y + x)
   */
  public int[] blockFind3x3(int[] data, int maxx, int maxy) {
    // The number of blocks in x and y
    final int xblocks = getBlocks(maxx, 2);
    final int yblocks = getBlocks(maxy, 2);

    // The final index in each dimension (where an incomplete block is found).
    // This equals maxx/maxy if the number of blocks fits exactly.
    final int xfinal = 2 * (maxx / 2);
    final int yfinal = 2 * (maxy / 2);

    // Expand the canvas
    final int newx = maxx + (maxx - xfinal) + 2;
    final int newy = maxy + (maxy - yfinal) + 2;
    data = expand(data, maxx, maxy, newx, newy);

    // Compare 2x2 block
    // ....
    // .AB.
    // .CD.
    // ....

    // Create the scan arrays to search the remaining 5 locations around the 2x2 block maxima
    // .... aaa. .bbb .... ....
    // .AB. aA.. ..Bb c... ...d
    // .CD. a... ...b cC.. ..Dd
    // .... .... .... ccc. .ddd
    final int[] a = new int[] {-newx - 1, -newx, -newx + 1, -1, +newx - 1};
    final int[] b = new int[] {-newx - 1, -newx, -newx + 1, +1, +newx + 1};
    final int[] c = new int[] {-newx - 1, -1, +newx - 1, +newx, +newx + 1};
    final int[] d = new int[] {-newx + 1, +1, +newx - 1, +newx, +newx + 1};

    final int[] maxima = new int[xblocks * yblocks];
    int nMaxima = 0;

    if (isNeighbourCheck()) {
      final boolean[] maximaFlag = getFlagBuffer(data.length);

      final int[][] scans = new int[4][];
      final int[] maxIndices = new int[4];
      int candidates;

      for (int y = 0; y < maxy; y += 2) {
        int xindex = (y + 1) * newx + 1;
        for (int x = 0; x < maxx; x += 2, xindex += 2) {
          // Compare A and B
          if (data[xindex] < data[xindex + 1]) {
            scans[0] = b;
            maxIndices[0] = xindex + 1;
            candidates = 1;
          } else if (data[xindex] == data[xindex + 1]) {
            scans[0] = a;
            maxIndices[0] = xindex;
            scans[1] = b;
            maxIndices[1] = xindex + 1;
            candidates = 2;
          } else {
            scans[0] = a;
            maxIndices[0] = xindex;
            candidates = 1;
          }

          // Compare to C
          if (data[maxIndices[0]] < data[xindex + newx]) {
            scans[0] = c;
            maxIndices[0] = xindex + newx;
            candidates = 1;
          } else if (data[maxIndices[0]] == data[xindex + newx]) {
            scans[candidates] = c;
            maxIndices[candidates] = xindex + newx;
            candidates++;
          }

          // Compare to D
          if (data[maxIndices[0]] < data[xindex + newx + 1]) {
            scans[0] = d;
            maxIndices[0] = xindex + newx + 1;
            candidates = 1;
          } else if (data[maxIndices[0]] == data[xindex + newx + 1]) {
            scans[candidates] = d;
            maxIndices[candidates] = xindex + newx + 1;
            candidates++;
          }

          // Check the remaining region for each candidate to ensure a true maxima
          FIND_MAXIMUM: for (int candidate = 0; candidate < candidates; candidate++) {
            final int maxIndex = maxIndices[candidate];
            final int[] scan = scans[candidate];

            for (final int offset : scan) {
              if (data[maxIndex] < data[maxIndex + offset]) {
                continue FIND_MAXIMUM;
              }
            }

            // Only check ABC since the scan blocks for D have not yet been processed
            if (scan != d) {
              for (final int offset : scan) {
                if (maximaFlag[maxIndex + offset]) {
                  continue FIND_MAXIMUM;
                }
              }
            }

            // Remap the maxima
            final int xx = maxIndex % newx;
            final int yy = maxIndex / newx;

            // System.out.printf("blockFind3x3 [%d,%d]\n", xx-1, yy-1);
            maximaFlag[maxIndex] = true;
            maxima[nMaxima++] = (yy - 1) * maxx + xx - 1;
            break;
          }
        }
      }
    } else {
      for (int y = 0; y < maxy; y += 2) {
        int xindex = (y + 1) * newx + 1;
        FIND_MAXIMUM: for (int x = 0; x < maxx; x += 2, xindex += 2) {
          int[] scan = a;
          int maxIndex = xindex;
          if (data[maxIndex] < data[xindex + 1]) {
            scan = b;
            maxIndex = xindex + 1;
          }
          if (data[maxIndex] < data[xindex + newx]) {
            scan = c;
            maxIndex = xindex + newx;
          }
          if (data[maxIndex] < data[xindex + newx + 1]) {
            scan = d;
            maxIndex = xindex + newx + 1;
          }

          // Check the remaining region
          for (final int offset : scan) {
            if (data[maxIndex] < data[maxIndex + offset]) {
              continue FIND_MAXIMUM;
            }
          }

          // Remap the maxima
          final int xx = maxIndex % newx;
          final int yy = maxIndex / newx;

          // System.out.printf("blockFind3x3 [%d,%d]\n", xx-1, yy-1);
          maxima[nMaxima++] = (yy - 1) * maxx + xx - 1;
        } // end FIND_MAXIMA
      }
    }

    return truncate(maxima, nMaxima);
  }

  /**
   * Compute the local-maxima within a 3x3 block. An inner boundary of 1 is ignored as potential
   * maxima on the top and left, and a boundary of 1 or 2 on the right or bottom (depending if the
   * image is even/odd dimensions).
   *
   * @param data The input data (packed in YX order)
   * @param maxx The width of the data
   * @param maxy The height of the data
   * @param border The internal border
   * @return Indices to the local maxima (index = maxx * y + x)
   */
  public int[] blockFind3x3Internal(int[] data, int maxx, int maxy, int border) {
    if (border < 1) {
      return blockFind3x3(data, maxx, maxy);
    }

    // The number of blocks in x and y
    final int xblocks = getBlocks(maxx - border, 2);
    final int yblocks = getBlocks(maxy - border, 2);

    // Compare 2x2 block
    // ....
    // .AB.
    // .CD.
    // ....

    // Create the scan arrays to search the remaining 5 locations around the 2x2 block maxima
    // .... aaa. .bbb .... ....
    // .AB. aA.. ..Bb c... ...d
    // .CD. a... ...b cC.. ..Dd
    // .... .... .... ccc. .ddd
    final int[] a = new int[] {-maxx - 1, -maxx, -maxx + 1, -1, +maxx - 1};
    final int[] b = new int[] {-maxx - 1, -maxx, -maxx + 1, +1, +maxx + 1};
    final int[] c = new int[] {-maxx - 1, -1, +maxx - 1, +maxx, +maxx + 1};
    final int[] d = new int[] {-maxx + 1, +1, +maxx - 1, +maxx, +maxx + 1};

    final int[] maxima = new int[xblocks * yblocks];
    int nMaxima = 0;

    if (isNeighbourCheck()) {
      final boolean[] maximaFlag = getFlagBuffer(data.length);

      final int[][] scans = new int[4][];
      final int[] maxIndices = new int[4];
      int candidates;

      for (int y = border; y < maxy - border - 1; y += 2) {
        int xindex = y * maxx + border;
        for (int x = border; x < maxx - border - 1; x += 2, xindex += 2) {
          // Compare A and B
          if (data[xindex] < data[xindex + 1]) {
            scans[0] = b;
            maxIndices[0] = xindex + 1;
            candidates = 1;
          } else if (data[xindex] == data[xindex + 1]) {
            scans[0] = a;
            maxIndices[0] = xindex;
            scans[1] = b;
            maxIndices[1] = xindex + 1;
            candidates = 2;
          } else {
            scans[0] = a;
            maxIndices[0] = xindex;
            candidates = 1;
          }

          // Compare to C
          if (data[maxIndices[0]] < data[xindex + maxx]) {
            scans[0] = c;
            maxIndices[0] = xindex + maxx;
            candidates = 1;
          } else if (data[maxIndices[0]] == data[xindex + maxx]) {
            scans[candidates] = c;
            maxIndices[candidates] = xindex + maxx;
            candidates++;
          }

          // Compare to D
          if (data[maxIndices[0]] < data[xindex + maxx + 1]) {
            scans[0] = d;
            maxIndices[0] = xindex + maxx + 1;
            candidates = 1;
          } else if (data[maxIndices[0]] == data[xindex + maxx + 1]) {
            scans[candidates] = d;
            maxIndices[candidates] = xindex + maxx + 1;
            candidates++;
          }

          // Check the remaining region for each candidate to ensure a true maxima
          FIND_MAXIMUM: for (int candidate = 0; candidate < candidates; candidate++) {
            final int maxIndex = maxIndices[candidate];
            final int[] scan = scans[candidate];

            for (final int offset : scan) {
              if (data[maxIndex] < data[maxIndex + offset]) {
                continue FIND_MAXIMUM;
              }
            }

            // Only check ABC since the scan blocks for D have not yet been processed
            if (scan != d) {
              for (final int offset : scan) {
                if (maximaFlag[maxIndex + offset]) {
                  continue FIND_MAXIMUM;
                }
              }
            }

            maximaFlag[maxIndex] = true;
            maxima[nMaxima++] = maxIndex;
            break;
          } // end FIND_MAXIMA
        }
      }
    } else {
      for (int y = border; y < maxy - border - 1; y += 2) {
        int xindex = y * maxx + border;
        FIND_MAXIMUM: for (int x = border; x < maxx - border - 1; x += 2, xindex += 2) {
          int[] scan = a;
          int maxIndex = xindex;
          if (data[maxIndex] < data[xindex + 1]) {
            scan = b;
            maxIndex = xindex + 1;
          }
          if (data[maxIndex] < data[xindex + maxx]) {
            scan = c;
            maxIndex = xindex + maxx;
          }
          if (data[maxIndex] < data[xindex + maxx + 1]) {
            scan = d;
            maxIndex = xindex + maxx + 1;
          }

          // Check the remaining region
          for (final int offset : scan) {
            if (data[maxIndex] < data[maxIndex + offset]) {
              continue FIND_MAXIMUM;
            }
          }

          maxima[nMaxima++] = maxIndex;
        } // end FIND_MAXIMA
      }
    }

    return truncate(maxima, nMaxima);
  }

  /**
   * Search the data for the index of the maximum in each block of size n*n. <p> E.g. Max [ (i,i+n)
   * x (i,j+n) ] for (i=0; i&lt;maxx; i+=n) x (j=0, j&lt;maxy; j+=n)
   *
   * @param data The input data (packed in YX order)
   * @param maxx The width of the data
   * @param maxy The height of the data
   * @param n the block size
   * @return The maxima indices
   */
  public int[] findBlockMaxima(int[] data, int maxx, int maxy, int n) {
    if (n == 1) {
      return findBlockMaxima2x2(data, maxx, maxy);
    }

    return findBlockMaximaNxN(data, maxx, maxy, n);
  }

  /**
   * Search the data for the index of the maximum in each block of size n*n. <p> E.g. Max [ (i,i+n)
   * x (i,j+n) ] for (i=0; i&lt;maxx; i+=n) x (j=0, j&lt;maxy; j+=n) <p> If multiple indices within
   * the block have the same value then all are returned.
   *
   * @param data The input data (packed in YX order)
   * @param maxx The width of the data
   * @param maxy The height of the data
   * @param n the block size
   * @return The maxima indices
   */
  public int[][] findBlockMaximaCandidates(int[] data, int maxx, int maxy, int n) {
    if (n == 1) {
      return findBlockMaximaCandidates2x2(data, maxx, maxy);
    }

    return findBlockMaximaCandidatesNxN(data, maxx, maxy, n);
  }
}
