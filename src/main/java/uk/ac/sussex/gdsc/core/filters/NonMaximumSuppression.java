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
 * Copyright (C) 2011 - 2019 Alex Herbert
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

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.util.FastMath;
import uk.ac.sussex.gdsc.core.utils.IntFixedList;

/**
 * Computes the local maxima.
 */
public class NonMaximumSuppression {

  /**
   * The neighbour check flag. Set to true to perform an additional comparison between the local
   * maxima within the block (size n) and the neighbours within the 2n+1 perimeter.
   */
  private boolean neighbourCheck;

  /**
   * Set to true to use data buffers for processing. Set to false should enable thread safety as all
   * buffers are allocated dynamically.
   */
  private boolean dataBuffer = true;

  /** A buffer to store the input float data with a border. */
  private float[] expandedFloatDataBuffer;
  /** A buffer to store the input int data with a border. */
  private int[] expandedIntDataBuffer;
  /** A buffer to store the maxima indices. */
  private IntFixedList resultsBuffer;
  /** A buffer to store true if the index is a maxima. */
  private boolean[] maximaFlagBuffer;

  /**
   * Class to store scans for candidate maxima. The highest maxima is kept, or if tied then all
   * maxima are kept.
   */
  private static class ScanCandidate {
    /** The size (i.e. number of candidates). */
    int size;
    /** The indices. */
    int[] indices = new int[4];
    /** The scans. */
    int[][] scans = new int[4][];

    /**
     * Gets the size.
     *
     * @return the size
     */
    int size() {
      return size;
    }

    /**
     * Gets the max index.
     *
     * @param index the index
     * @return the max index
     */
    int getMaxIndex(int index) {
      return indices[index];
    }

    /**
     * Gets the scan.
     *
     * @param index the index
     * @return the scan
     */
    int[] getScan(int index) {
      return scans[index];
    }
  }

  /**
   * Class to store scans for candidate maxima. The highest maxima is kept, or if tied then all
   * maxima are kept.
   */
  static final class FloatScanCandidate extends ScanCandidate {
    /** The maxima value. */
    float value;

    /**
     * Initialises to a size of 1 with the given candidate.
     *
     * @param values the values
     * @param index the index in the values
     * @param scan the scan around the candidate
     */
    void init(float[] values, int index, int[] scan) {
      init(values[index], index, scan);
    }

    /**
     * Initialises to a size of 1 with the given candidate.
     *
     * @param value the value
     * @param index the index
     * @param scan the scan around the candidate
     */
    void init(float value, int index, int[] scan) {
      this.value = value;
      indices[0] = index;
      scans[0] = scan;
      size = 1;
    }

    /**
     * Adds the given index from the values and scan arrays. If higher then replace the current
     * maxima. If equal then increase the size to store it in addition.
     *
     * @param values the values
     * @param index the index
     * @param scan the scan
     */
    void add(float[] values, int index, int[] scan) {
      final float newValue = values[index];
      if (newValue > value) {
        init(newValue, index, scan);
      } else if (newValue == value) {
        indices[size] = index;
        scans[size] = scan;
        size++;
      }
    }
  }

  /**
   * Class to store scans for candidate maxima. The highest maxima is kept, or if tied then all
   * maxima are kept.
   */
  static final class IntScanCandidate extends ScanCandidate {
    /** The maxima value. */
    int value;

    /**
     * Initialises to a size of 1 with the given candidate.
     *
     * @param values the values
     * @param index the index in the values
     * @param scan the scan around the candidate
     */
    void init(int[] values, int index, int[] scan) {
      init(values[index], index, scan);
    }

    /**
     * Initialises to a size of 1 with the given candidate.
     *
     * @param value the value
     * @param index the index
     * @param scan the scan around the candidate
     */
    void init(int value, int index, int[] scan) {
      this.value = value;
      indices[0] = index;
      scans[0] = scan;
      size = 1;
    }

    /**
     * Adds the given index from the values and scan arrays. If higher then replace the current
     * maxima. If equal then increase the size to store it in addition.
     *
     * @param values the values
     * @param index the index
     * @param scan the scan
     */
    void add(int[] values, int index, int[] scan) {
      final int newValue = values[index];
      if (newValue > value) {
        init(newValue, index, scan);
      } else if (newValue == value) {
        indices[size] = index;
        scans[size] = scan;
        size++;
      }
    }
  }

  /**
   * Instantiates a new non maximum suppression.
   */
  public NonMaximumSuppression() {
    // Nothing to do
  }

  /**
   * Instantiates a new non maximum suppression.
   *
   * @param source the source
   */
  protected NonMaximumSuppression(NonMaximumSuppression source) {
    this.neighbourCheck = source.neighbourCheck;
    this.dataBuffer = source.dataBuffer;
  }

  /**
   * Neighbour checking performs an additional comparison between the local maxima within the block
   * (size n) and the neighbours within the 2n+1 perimeter. If any neighbour is already a maxima
   * then the local maxima within the block is eliminated. This step is only relevant when neighbour
   * data points have equal values since the search for maxima uses the &lt; operator.
   *
   * <p>Applies to the blockFind algorithms.
   *
   * @param neighbourCheck Enable neighbour checking
   */
  public void setNeighbourCheck(boolean neighbourCheck) {
    this.neighbourCheck = neighbourCheck;
  }

  /**
   * Checks if if neighbour checking is enabled.
   *
   * @return True if neighbour checking is enabled.
   */
  public boolean isNeighbourCheck() {
    return neighbourCheck;
  }

  /**
   * Allow the class to keep a data buffer for processing images with the blockFind3x3 algorithm.
   *
   * @param dataBuffer Enable the data buffer
   */
  public void setDataBuffer(boolean dataBuffer) {
    this.dataBuffer = dataBuffer;
    if (!dataBuffer) {
      expandedFloatDataBuffer = null;
      expandedIntDataBuffer = null;
    }
  }

  /**
   * Checks if the data buffer is enabled.
   *
   * @return True if the data buffer is enabled.
   */
  public boolean isBufferData() {
    return dataBuffer;
  }

  /**
   * Expand the image to the new dimensions with a 1-pixel border.
   *
   * @param data the data
   * @param maxx the maxx
   * @param maxy the maxy
   * @param newx the newx
   * @param newy the newy
   * @return the new image
   */
  protected float[] expand(float[] data, int maxx, int maxy, int newx, int newy) {
    final int size = newx * newy;

    if (!dataBuffer || expandedFloatDataBuffer == null || expandedFloatDataBuffer.length < size) {
      expandedFloatDataBuffer = new float[size];
    }

    // Zero first row
    for (int x = 0; x < newx; x++) {
      expandedFloatDataBuffer[x] = Float.NEGATIVE_INFINITY;
    }
    // Zero last rows
    for (int y = maxy + 1; y < newy; y++) {
      int newIndex = y * newx;
      for (int x = 0; x < newx; x++) {
        expandedFloatDataBuffer[newIndex++] = Float.NEGATIVE_INFINITY;
      }
    }

    int index = 0;
    for (int y = 0; y < maxy; y++) {
      int newIndex = (y + 1) * newx;

      // Zero first column
      expandedFloatDataBuffer[newIndex++] = Float.NEGATIVE_INFINITY;

      // Copy data
      for (int x = 0; x < maxx; x++) {
        expandedFloatDataBuffer[newIndex++] = data[index++];
      }

      // Zero remaining columns
      for (int x = maxx + 1; x < newx; x++) {
        expandedFloatDataBuffer[newIndex++] = Float.NEGATIVE_INFINITY;
      }
    }

    return expandedFloatDataBuffer;
  }

  /**
   * Expand the image to the new dimensions with a 1-pixel border.
   *
   * @param data the data
   * @param maxx the maxx
   * @param maxy the maxy
   * @param newx the newx
   * @param newy the newy
   * @return the new image
   */
  protected int[] expand(int[] data, int maxx, int maxy, int newx, int newy) {
    final int size = newx * newy;

    if (!dataBuffer || expandedIntDataBuffer == null || expandedIntDataBuffer.length < size) {
      expandedIntDataBuffer = new int[size];
    }

    // Zero first row
    for (int x = 0; x < newx; x++) {
      expandedIntDataBuffer[x] = Integer.MIN_VALUE;
    }
    // Zero last rows
    for (int y = maxy + 1; y < newy; y++) {
      int newIndex = y * newx;
      for (int x = 0; x < newx; x++) {
        expandedIntDataBuffer[newIndex++] = Integer.MIN_VALUE;
      }
    }

    int index = 0;
    for (int y = 0; y < maxy; y++) {
      int newIndex = (y + 1) * newx;

      // Zero first column
      expandedIntDataBuffer[newIndex++] = Integer.MIN_VALUE;

      // Copy data
      for (int x = 0; x < maxx; x++) {
        expandedIntDataBuffer[newIndex++] = data[index++];
      }

      // Zero remaining columns
      for (int x = maxx + 1; x < newx; x++) {
        expandedIntDataBuffer[newIndex++] = Integer.MIN_VALUE;
      }
    }

    return expandedIntDataBuffer;
  }

  /**
   * Get a buffer for storing result indices.
   *
   * @param size the size
   * @return the results buffer
   */
  protected IntFixedList getResultsBuffer(int size) {
    if (!dataBuffer) {
      return new IntFixedList(size);
    }

    if (resultsBuffer == null || resultsBuffer.capacity() < size) {
      resultsBuffer = new IntFixedList(size);
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
  protected boolean[] getFlagBuffer(int size) {
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

  /**
   * Gets the blocks.
   *
   * @param max the maximum of the dimension
   * @param n The block size
   * @return the blocks
   */
  protected static int getBlocks(int max, int n) {
    return (int) Math.ceil((double) max / n);
  }

  /**
   * Truncate the array to the specified size.
   *
   * @param array the array
   * @param size the size
   * @return The truncated array
   */
  protected static int[] truncate(int[] array, int size) {
    if (array.length == size) {
      return array;
    }
    if (size == 0) {
      return ArrayUtils.EMPTY_INT_ARRAY;
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
  protected static int[][] truncate(int[][] array, int size) {
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
   * Create a copy.
   *
   * @return the copy
   */
  public NonMaximumSuppression copy() {
    return new NonMaximumSuppression(this);
  }

  /**
   * The minimum value for a float.
   *
   * @return the minimum value
   */
  protected static final float floatMin() {
    return Float.NEGATIVE_INFINITY;
  }

  /**
   * The minimum value for a int.
   *
   * @return the minimum value
   */
  protected static final int intMin() {
    return Integer.MIN_VALUE;
  }

  // ----------------------------------------------------
  // NOTE:
  // Copy from here to replace 'float' with 'int'
  // ----------------------------------------------------

  /**
   * Compute the local-maxima within a 2n+1 block.
   *
   * @param data The input data (packed in YX order)
   * @param maxx The width of the data
   * @param maxy The height of the data
   * @param n The block size
   * @return Indices to the local maxima (index = maxx * y + x)
   */
  public int[] maxFind(float[] data, int maxx, int maxy, int n) {
    final IntFixedList results = getResultsBuffer(data.length);
    final boolean[] maximaFlag = getFlagBuffer(data.length);

    // Boundary control
    final int xwidth = FastMath.min(n, maxx - 1);
    final int ywidth = FastMath.min(n, maxy - 1);
    final int xlimit = maxx - xwidth;
    final int ylimit = maxy - ywidth;

    final int[] offset = new int[(2 * xwidth + 1) * (2 * ywidth + 1) - 1];
    final int[] xoffset = new int[offset.length];
    final int[] yoffset = new int[offset.length];
    int offsetIndex = 0;
    for (int y = -ywidth; y <= ywidth; y++) {
      for (int x = -xwidth; x <= xwidth; x++) {
        if (x != 0 || y != 0) {
          offset[offsetIndex] = maxx * y + x;
          xoffset[offsetIndex] = x;
          yoffset[offsetIndex] = y;
          offsetIndex++;
        }
      }
    }

    // Compare all points
    int index = 0;
    for (int y = 0; y < maxy; y++) {
      FIND_MAXIMUM: for (int x = 0; x < maxx; x++, index++) {
        final float v = data[index];

        // Flag to indicate this pixels has a complete (2n+1) neighbourhood
        final boolean isInnerXy = (y >= ywidth && y < ylimit) && (x >= xwidth && x < xlimit);

        // Sweep neighbourhood
        if (isInnerXy) {
          for (final int off : offset) {
            if (maximaFlag[index + off] || data[index + off] > v) {
              continue FIND_MAXIMUM;
            }
          }
        } else {
          for (int i = offset.length; i-- > 0;) {
            // Get the coords and check if it is within the data
            final int yy = y + yoffset[i];
            final int xx = x + xoffset[i];
            final boolean isWithin = (yy >= 0 && yy < maxy) && (xx >= 0 && xx < maxx);

            if (isWithin && (maximaFlag[index + offset[i]] || data[index + offset[i]] > v)) {
              continue FIND_MAXIMUM;
            }
          }
        }

        results.add(index);
        maximaFlag[index] = true;
      } // end FIND_MAXIMUM
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
    final IntFixedList results = getResultsBuffer(data.length);
    final boolean[] maximaFlag = getFlagBuffer(data.length);

    // Boundary control
    final int xwidth = FastMath.min(n, maxx - 1);
    final int ywidth = FastMath.min(n, maxy - 1);

    final int[] offset = new int[(2 * xwidth + 1) * (2 * ywidth + 1) - 1];
    int offsetIndex = 0;
    for (int y = -ywidth; y <= ywidth; y++) {
      for (int x = -xwidth; x <= xwidth; x++) {
        if (x != 0 || y != 0) {
          offset[offsetIndex] = maxx * y + x;
          offsetIndex++;
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
        for (final int off : offset) {
          if (maximaFlag[index + off] || data[index + off] > v) {
            continue FIND_MAXIMUM;
          }
        }

        results.add(index);
        maximaFlag[index] = true;
      } // end FIND_MAXIMUM
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

    final IntFixedList results = getResultsBuffer(data.length);
    final boolean[] maximaFlag = getFlagBuffer(data.length);

    // Boundary control
    final int xwidth = FastMath.min(n, maxx - 1);
    final int ywidth = FastMath.min(n, maxy - 1);
    final int xlimit = maxx - xwidth;
    final int ylimit = maxy - ywidth;

    final int[] offset = new int[(2 * xwidth + 1) * (2 * ywidth + 1) - 1];
    final int[] xoffset = new int[offset.length];
    final int[] yoffset = new int[offset.length];
    int offsetIndex = 0;
    for (int y = -ywidth; y <= ywidth; y++) {
      for (int x = -xwidth; x <= xwidth; x++) {
        if (x != 0 || y != 0) {
          offset[offsetIndex] = maxx * y + x;
          xoffset[offsetIndex] = x;
          yoffset[offsetIndex] = y;
          offsetIndex++;
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
          for (final int off : offset) {
            if (maximaFlag[index + off] || data[index + off] > v) {
              continue FIND_MAXIMUM;
            }
          }
        } else {
          // Flag to indicate this pixels has a complete (2n+1) neighbourhood
          final boolean isInnerXy = (y >= ywidth && y < ylimit) && (x >= xwidth && x < xlimit);

          // Sweep neighbourhood
          for (int i = offset.length; i-- > 0;) {
            boolean isWithin = isInnerXy;
            if (!isWithin) {
              // Get the coords and check if it is within the data
              final int yy = y + yoffset[i];
              final int xx = x + xoffset[i];
              isWithin = (yy >= 0 && yy < maxy) && (xx >= 0 && xx < maxx);
            }

            if (isWithin && (maximaFlag[index + offset[i]] || data[index + offset[i]] > v)) {
              continue FIND_MAXIMUM;
            }
          }
        }

        results.add(index);
        maximaFlag[index] = true;
      } // end FIND_MAXIMUM
    }

    return results.toArray();
  }

  /**
   * Compute the local-maxima within a 2n+1 block.
   *
   * <p>Uses the 2D block algorithm of Neubeck and Van Gool (2006).
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
   * maxima.
   *
   * <p>Uses the 2D block algorithm of Neubeck and Van Gool (2006).
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
   * Compute the local-maxima within a 2n+1 block.
   *
   * <p>Uses the 2D block algorithm of Neubeck and Van Gool (2006).
   *
   * @param data The input data (packed in YX order)
   * @param maxx The width of the data
   * @param maxy The height of the data
   * @param n The block size
   * @return Indices to the local maxima (index = maxx * y + x)
   */
  public int[] blockFindNxN(float[] data, int maxx, int maxy, int n) {
    int[] maxima;
    int maximaCount = 0;
    final int n1 = n + 1;

    if (isNeighbourCheck()) {
      // Compute all candidate maxima in the NxN blocks. Then check each candidate
      // is a maxima in the 2N+1 region including a check for existing maxima.
      final int[][] blockMaximaCandidates = findBlockMaximaCandidatesNxN(data, maxx, maxy, n);

      maxima = new int[blockMaximaCandidates.length];
      final boolean[] maximaFlag = getFlagBuffer(data.length);

      for (final int[] blockMaxima : blockMaximaCandidates) {
        for (final int index : blockMaxima) {
          if (isMaximaNxN(data, maximaFlag, maxx, maxy, n, 0, n1, index)) {
            maximaFlag[index] = true;
            maxima[maximaCount++] = index;
            break;
          }
        }
      }
    } else {
      final int[] blockMaxima = findBlockMaximaNxN(data, maxx, maxy, n);

      maxima = blockMaxima; // Re-use storage space

      for (final int index : blockMaxima) {
        if (isMaximaNxN(data, maxx, maxy, n, 0, n1, index)) {
          maxima[maximaCount++] = index;
        }
      }
    }

    return truncate(maxima, maximaCount);
  }

  /**
   * Checks if the block maxima is a maxima in the larger region outside the local block, up to +/-
   * n around the index location.
   *
   * @param data The input data (packed in YX order)
   * @param maximaFlag the maxima flag
   * @param maxx The width of the data
   * @param maxy The height of the data
   * @param n The block size
   * @param border the border
   * @param n1 the n+1
   * @param index the index
   * @return true, if is maxima
   */
  protected static boolean isMaximaNxN(float[] data, boolean[] maximaFlag, int maxx, int maxy,
      int n, int border, int n1, int index) {
    final float v = data[index];

    final int mi = index % maxx;
    final int mj = index / maxx;

    // Compare the maxima to the surroundings. Ignore the block region already processed.
    //@formatter:off
    //
    //        (mi-n,mj-n)----------------------------------(mi+n,mj-n)
    //             |aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa|
    //             |aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa|
    //             |aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa|
    //             |aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa|
    //             |aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa|
    //             |bbbbbbbb (i,j)-------------(i+n,j) ccccccccc|
    //             |bbbbbbbbbbb|                  |ccccccccccccc|
    //             |bbbbbbbbbbb|                  |ccccccccccccc|
    //             |bbbbbbbbbbb|     (mi,mj)      |ccccccccccccc|
    //             |bbbbbbbbbbb|                  |ccccccccccccc|
    //             |bbbbbbbbbbb|                  |ccccccccccccc|
    //             |bbbbbbbbbbb|                  |ccccccccccccc|
    //             |bbbbbbbbbbb|                  |ccccccccccccc|
    //             |bbbbbbbb (i,j+n)-----------(i+n,j+n) ccccccc|
    //             |dddddddddddddddddddddddddddddddddddddddddddd|
    //             |dddddddddddddddddddddddddddddddddddddddddddd|
    //             |dddddddddddddddddddddddddddddddddddddddddddd|
    //       (mi-n,mj+n)----------------------------------(mi+n,mj+n)
    //
    //@formatter:on
    // This must be done without over-running boundaries
    final int j = n1 * ((mj - border) / n1) + border; // Blocks n+1 wide
    final int miMinusN = FastMath.max(mi - n, 0);
    final int miPlusN = FastMath.min(mi + n, maxx - 1);
    final int mjMinusN = FastMath.max(mj - n, 0);

    // Neighbour check for existing maxima. This is relevant if the same height.
    // However we cannot just check height as the neighbour may not be the selected maxima
    // within its block.
    // Only check A+B since the blocks for C+D have not yet been processed

    // A
    for (int jj = mjMinusN; jj < j; jj++) {
      final int indexEnd = jj * maxx + miPlusN;
      for (int indexStart = jj * maxx + miMinusN; indexStart <= indexEnd; indexStart++) {
        // A includes neighbour check
        if (v < data[indexStart] || maximaFlag[indexStart]) {
          return false;
        }
      }
    }
    final int i = n1 * ((mi - border) / n1) + border; // Blocks n+1 wide
    final int iPlusN = i + n1;
    final int jPlusN = FastMath.min(j + n, maxy - 1);
    for (int jj = j; jj <= jPlusN; jj++) {
      // B
      final int indexEnd = jj * maxx + i;
      for (int indexStart = jj * maxx + miMinusN; indexStart < indexEnd; indexStart++) {
        // B includes neighbour check
        if (v < data[indexStart] || maximaFlag[indexStart]) {
          return false;
        }
      }

      // C
      final int indexEnd2 = jj * maxx + miPlusN;
      for (int indexStart = jj * maxx + iPlusN; indexStart <= indexEnd2; indexStart++) {
        if (v < data[indexStart]) {
          return false;
        }
      }
    }
    // D
    final int mjPlusN = FastMath.min(mj + n, maxy - 1);
    for (int jj = jPlusN + 1; jj <= mjPlusN; jj++) {
      final int indexEnd = jj * maxx + miPlusN;
      for (int indexStart = jj * maxx + miMinusN; indexStart <= indexEnd; indexStart++) {
        if (v < data[indexStart]) {
          return false;
        }
      }
    }

    return true;
  }

  /**
   * Checks if the block maxima is a maxima in the larger region outside the local block, up to +/-
   * n around the index location.
   *
   * @param data The input data (packed in YX order)
   * @param maxx The width of the data
   * @param maxy The height of the data
   * @param n The block size
   * @param border the border
   * @param n1 the n+1
   * @param index the index
   * @return true, if is maxima
   */
  protected static boolean isMaximaNxN(float[] data, int maxx, int maxy, int n, int border, int n1,
      int index) {
    final float v = data[index];

    final int mi = index % maxx;
    final int mj = index / maxx;

    // Compare the maxima to the surroundings. Ignore the block region already processed.
    //@formatter:off
    //
    //        (mi-n,mj-n)----------------------------------(mi+n,mj-n)
    //             |aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa|
    //             |aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa|
    //             |aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa|
    //             |aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa|
    //             |aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa|
    //             |bbbbbbbb (i,j)-------------(i+n,j) ccccccccc|
    //             |bbbbbbbbbbb|                  |ccccccccccccc|
    //             |bbbbbbbbbbb|                  |ccccccccccccc|
    //             |bbbbbbbbbbb|     (mi,mj)      |ccccccccccccc|
    //             |bbbbbbbbbbb|                  |ccccccccccccc|
    //             |bbbbbbbbbbb|                  |ccccccccccccc|
    //             |bbbbbbbbbbb|                  |ccccccccccccc|
    //             |bbbbbbbbbbb|                  |ccccccccccccc|
    //             |bbbbbbbb (i,j+n)-----------(i+n,j+n) ccccccc|
    //             |dddddddddddddddddddddddddddddddddddddddddddd|
    //             |dddddddddddddddddddddddddddddddddddddddddddd|
    //             |dddddddddddddddddddddddddddddddddddddddddddd|
    //       (mi-n,mj+n)----------------------------------(mi+n,mj+n)
    //
    //@formatter:on
    // This must be done without over-running boundaries
    final int j = n1 * ((mj - border) / n1) + border; // Blocks n+1 wide
    final int miMinusN = FastMath.max(mi - n, 0);
    final int miPlusN = FastMath.min(mi + n, maxx - 1);
    final int mjMinusN = FastMath.max(mj - n, 0);

    // A
    for (int jj = mjMinusN; jj < j; jj++) {
      final int indexEnd = jj * maxx + miPlusN;
      for (int indexStart = jj * maxx + miMinusN; indexStart <= indexEnd; indexStart++) {
        if (v < data[indexStart]) {
          return false;
        }
      }
    }
    final int i = n1 * ((mi - border) / n1) + border; // Blocks n+1 wide
    final int iPlusN = i + n1;
    final int jPlusN = FastMath.min(j + n, maxy - 1);
    for (int jj = j; jj <= jPlusN; jj++) {
      // B
      final int indexEnd = jj * maxx + i;
      for (int indexStart = jj * maxx + miMinusN; indexStart < indexEnd; indexStart++) {
        if (v < data[indexStart]) {
          return false;
        }
      }

      // C
      final int indexEnd2 = jj * maxx + miPlusN;
      for (int indexStart = jj * maxx + iPlusN; indexStart <= indexEnd2; indexStart++) {
        if (v < data[indexStart]) {
          return false;
        }
      }
    }
    // D
    final int mjPlusN = FastMath.min(mj + n, maxy - 1);
    for (int jj = jPlusN + 1; jj <= mjPlusN; jj++) {
      final int indexEnd = jj * maxx + miPlusN;
      for (int indexStart = jj * maxx + miMinusN; indexStart <= indexEnd; indexStart++) {
        if (v < data[indexStart]) {
          return false;
        }
      }
    }

    return true;
  }

  /**
   * Compute the local-maxima within a 2n+1 block. An inner boundary of N is ignored as potential
   * maxima.
   *
   * <p>Uses the 2D block algorithm of Neubeck and Van Gool (2006).
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
    int maximaCount = 0;
    final int n1 = n + 1;

    if (isNeighbourCheck()) {
      // Compute all candidate maxima in the NxN blocks. Then check each candidate
      // is a maxima in the 2N+1 region including a check for existing maxima.
      final int[][] blockMaximaCandidates =
          findBlockMaximaCandidatesNxNInternal(data, maxx, maxy, n, border);

      maxima = new int[blockMaximaCandidates.length];
      final boolean[] maximaFlag = getFlagBuffer(data.length);

      for (final int[] blockMaxima : blockMaximaCandidates) {
        for (final int index : blockMaxima) {
          if (isMaximaNxN(data, maximaFlag, maxx, maxy, n, border, n1, index)) {
            maximaFlag[index] = true;
            maxima[maximaCount++] = index;
            break;
          }
        }
      }
    } else {
      final int[] blockMaxima = findBlockMaximaNxNInternal(data, maxx, maxy, n, border);

      maxima = blockMaxima; // Re-use storage space

      for (final int index : blockMaxima) {
        if (isMaximaNxN(data, maxx, maxy, n, border, n1, index)) {
          maxima[maximaCount++] = index;
        }
      }
    }

    return truncate(maxima, maximaCount);
  }

  /**
   * Search the data for the index of the maximum in each block of size n*n.
   *
   * <p>E.g. Max [ (i,i+n) x (i,j+n) ] for (i=0; i&lt;maxx; i+=n) x (j=0, j&lt;maxy; j+=n)
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
    final int n1 = n + 1;

    // The number of blocks in x and y
    final int xblocks = getBlocks(maxx, n1);
    final int yblocks = getBlocks(maxy, n1);

    // The final index in each dimension (where an incomplete block is found).
    // This equals maxx/maxy if the number of blocks fits exactly.
    final int xfinal = n1 * (maxx / n1);
    final int yfinal = n1 * (maxy / n1);

    final int[] maxima = new int[xblocks * yblocks];

    int block = 0;
    for (int y = 0; y < maxy; y += n1) {
      for (int x = 0; x < maxx; x += n1) {
        // Find the sweep size in each direction
        final int xsize = (x == xfinal) ? maxx - xfinal : n1;
        int ysize = (y == yfinal) ? maxy - yfinal : n1;

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
   * is ignored as potential maxima.
   *
   * <p>E.g. Max [ (i,i+n) x (i,j+n) ] for (i=n; i&lt;maxx-n; i+=n) x (j=n, j&lt;maxy-n; j+=n)
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
    final int n1 = n + 1;

    // The number of blocks in x and y. Subtract the boundary blocks.
    final int xblocks = getBlocks(maxx - border, n1);
    final int yblocks = getBlocks(maxy - border, n1);

    if (xblocks < 1 || yblocks < 1) {
      return ArrayUtils.EMPTY_INT_ARRAY;
    }

    // The final index in each dimension (where an incomplete block is found).
    // This equals maxx/maxy if the number of blocks fits exactly.
    final int xfinal = n1 * ((maxx - 2 * border) / n1) + border;
    final int yfinal = n1 * ((maxy - 2 * border) / n1) + border;

    final int[] maxima = new int[xblocks * yblocks];

    int block = 0;
    for (int y = border; y < maxy - border; y += n1) {
      for (int x = border; x < maxx - border; x += n1) {

        // Find the sweep size in each direction
        final int xsize = (x == xfinal) ? maxx - xfinal - border : n1;
        int ysize = (y == yfinal) ? maxy - yfinal - border : n1;

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
   * Search the data for the index of the maximum in each block of size 2*2.
   *
   * <p>E.g. Max [ (i,i+n) x (i,j+n) ] for (i=0; i&lt;maxx; i+=n) x (j=0, j&lt;maxy; j+=n)
   *
   * @param data The input data (packed in YX order)
   * @param maxx The width of the data
   * @param maxy The height of the data
   * @return The maxima indices
   */
  public int[] findBlockMaxima2x2(float[] data, int maxx, int maxy) {
    // Optimised for 2x2 block

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
    //@formatter:off
    // x             xfinal
    // |             |  maxx
    // |             |  |
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
    //@formatter:on

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
        maxima[block] = yfinal * maxx + xfinal;
      }
    }

    return maxima;
  }

  /**
   * Search the data for the index of the maximum in each block of size n*n.
   *
   * <p>E.g. Max [ (i,i+n) x (i,j+n) ] for (i=0; i&lt;maxx; i+=n) x (j=0, j&lt;maxy; j+=n).
   *
   * <p>If multiple indices within the block have the same value then all are returned.
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
    final int n1 = n + 1;

    // The number of blocks in x and y
    final int xblocks = getBlocks(maxx, n1);
    final int yblocks = getBlocks(maxy, n1);

    // The final index in each dimension (where an incomplete block is found).
    // This equals maxx/maxy if the number of blocks fits exactly.
    final int xfinal = n1 * (maxx / n1);
    final int yfinal = n1 * (maxy / n1);

    final int[][] maxima = new int[xblocks * yblocks][];
    final IntFixedList list = new IntFixedList(n1 * n1);

    int block = 0;
    for (int y = 0; y < maxy; y += n1) {
      for (int x = 0; x < maxx; x += n1) {
        // Find the sweep size in each direction
        final int xsize = (x == xfinal) ? maxx - xfinal : n1;
        int ysize = (y == yfinal) ? maxy - yfinal : n1;

        int index = y * maxx + x;
        float max = floatMin();

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
   * is ignored as potential maxima.
   *
   * <p>E.g. Max [ (i,i+n) x (i,j+n) ] for (i=n; i&lt;maxx-n; i+=n) x (j=n, j&lt;maxy-n; j+=n).
   *
   * <p>If multiple indices within the block have the same value then all are returned.
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
    final int n1 = n + 1;

    // The number of blocks in x and y. Subtract the boundary blocks.
    final int xblocks = getBlocks(maxx - border, n1);
    final int yblocks = getBlocks(maxy - border, n1);

    if (xblocks < 1 || yblocks < 1) {
      return new int[0][0];
    }

    // The final index in each dimension (where an incomplete block is found).
    // This equals maxx/maxy if the number of blocks fits exactly.
    final int xfinal = n1 * ((maxx - 2 * border) / n1) + border;
    final int yfinal = n1 * ((maxy - 2 * border) / n1) + border;

    final int[][] maxima = new int[xblocks * yblocks][];
    final IntFixedList list = new IntFixedList(n1 * n1);

    int block = 0;
    for (int y = border; y < maxy - border; y += n1) {
      for (int x = border; x < maxx - border; x += n1) {
        // Find the sweep size in each direction
        final int xsize = (x == xfinal) ? maxx - xfinal - border : n1;
        int ysize = (y == yfinal) ? maxy - yfinal - border : n1;

        int index = y * maxx + x;
        float max = floatMin();

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
   * Search the data for the index of the maximum in each block of size 2*2.
   *
   * <p>E.g. Max [ (i,i+n) x (i,j+n) ] for (i=0; i&lt;maxx; i+=n) x (j=0, j&lt;maxy; j+=n).
   *
   * <p>If multiple indices within the block have the same value then all are returned.
   *
   * @param data The input data (packed in YX order)
   * @param maxx The width of the data
   * @param maxy The height of the data
   * @return The maxima indices
   */
  public int[][] findBlockMaximaCandidates2x2(float[] data, int maxx, int maxy) {
    // Optimised for 2x2 block

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
    //@formatter:off
    // x             xfinal
    // |             |  maxx
    // |             |  |
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
    //@formatter:on

    // Working storage for indices
    final IntFixedList i1 = new IntFixedList(4);
    final IntFixedList i2 = new IntFixedList(4);

    int block = 0;
    for (int y = 0; y < yfinal; y += 2) {
      // A
      int xindex = y * maxx;
      for (int x = 0; x < xfinal; x += 2) {
        // Compare 2x2 block
        getIndices(i1, data, xindex, xindex + 1);
        getIndices(i2, data, xindex + maxx, xindex + maxx + 1);

        if (data[i1.get(0)] > data[i2.get(0)]) {
          maxima[block++] = i1.toArray();
        } else if (data[i1.get(0)] < data[i2.get(0)]) {
          maxima[block++] = i2.toArray();
        } else {
          // Tied so merge the candidates
          i1.add(i2);
          maxima[block++] = i1.toArray();
        }
        xindex += 2;
      }
      // B
      if (xfinal != maxx) {
        // Compare 1x2 block
        final int index = y * maxx + xfinal;
        getIndices(i1, data, index, index + maxx);
        maxima[block++] = i1.toArray();
      }
    }
    if (yfinal != maxy) {
      // C
      int xindex = yfinal * maxx;
      for (int x = 0; x < xfinal; x += 2) {
        // Compare 2x1 block
        getIndices(i1, data, xindex, xindex + 1);
        maxima[block++] = i1.toArray();
        xindex += 2;
      }
      // D
      if (xfinal != maxx) {
        // Compare 1x1 block
        maxima[block] = new int[] {yfinal * maxx + xfinal};
      }
    }

    return maxima;
  }

  /**
   * Gets the indices of the maxima within the data from the provided indices.
   *
   * <p>Finds 1 index or 2 indices if both have the same value.
   *
   * @param indices the indices
   * @param data the data
   * @param index1 the first index
   * @param index2 the second index
   */
  protected static void getIndices(IntFixedList indices, float[] data, int index1, int index2) {
    indices.clear();
    if (data[index1] > data[index2]) {
      indices.add(index1);
    } else if (data[index1] < data[index2]) {
      indices.add(index2);
    } else {
      // Tied
      indices.add(index1);
      indices.add(index2);
    }
  }

  /**
   * Compute the local-maxima within a 3x3 block.
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
    final float[] newData = expand(data, maxx, maxy, newx, newy);

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
    int maximaCount = 0;

    if (isNeighbourCheck()) {
      final boolean[] maximaFlag = getFlagBuffer(newData.length);

      final FloatScanCandidate candidates = new FloatScanCandidate();

      for (int y = 0; y < maxy; y += 2) {
        int xindex = (y + 1) * newx + 1;
        for (int x = 0; x < maxx; x += 2, xindex += 2) {
          candidates.init(newData, xindex, a);
          candidates.add(newData, xindex + 1, b);
          candidates.add(newData, xindex + newx, c);
          candidates.add(newData, xindex + newx + 1, d);

          // Check the remaining region for each candidate to ensure a true maxima
          for (int i = 0; i < candidates.size(); i++) {
            final int maxIndex = candidates.getMaxIndex(i);
            final int[] scan = candidates.getScan(i);

            // Only check ABC using the existing maxima flag
            // since the scan blocks for D have not yet been processed
            final boolean isMaxima = (scan == d) ? isMaxima3x3(newData, maxIndex, scan)
                : isMaxima3x3(newData, maximaFlag, maxIndex, scan);

            if (isMaxima) {
              // Remap the maxima
              final int xx = maxIndex % newx;
              final int yy = maxIndex / newx;

              maximaFlag[maxIndex] = true;
              maxima[maximaCount++] = (yy - 1) * maxx + xx - 1;
              break;
            }
          }
        }
      }
    } else {
      for (int y = 0; y < maxy; y += 2) {
        int xindex = (y + 1) * newx + 1;
        for (int x = 0; x < maxx; x += 2, xindex += 2) {
          int[] scan = a;
          int maxIndex = xindex;
          if (newData[maxIndex] < newData[xindex + 1]) {
            scan = b;
            maxIndex = xindex + 1;
          }
          if (newData[maxIndex] < newData[xindex + newx]) {
            scan = c;
            maxIndex = xindex + newx;
          }
          if (newData[maxIndex] < newData[xindex + newx + 1]) {
            scan = d;
            maxIndex = xindex + newx + 1;
          }

          // Check the remaining region
          if (isMaxima3x3(newData, maxIndex, scan)) {
            // Remap the maxima
            final int xx = maxIndex % newx;
            final int yy = maxIndex / newx;

            maxima[maximaCount++] = (yy - 1) * maxx + xx - 1;
          }
        }
      }
    }

    return truncate(maxima, maximaCount);
  }


  /**
   * Checks if is a maxima using the offsets to scan the remaining region around the core 2x2 block.
   *
   * @param data the data
   * @param maximaFlag the maxima flag
   * @param index the index
   * @param scan the scan offsets
   * @return true, if is a maxima
   */
  protected static boolean isMaxima3x3(float[] data, boolean[] maximaFlag, int index, int[] scan) {
    for (final int offset : scan) {
      if (data[index] < data[index + offset] || maximaFlag[index + offset]) {
        return false;
      }
    }
    return true;
  }

  /**
   * Checks if is a maxima using the offsets to scan the remaining region around the core 2x2 block.
   *
   * @param data the data
   * @param index the index
   * @param scan the scan offsets
   * @return true, if is a maxima
   */
  protected static boolean isMaxima3x3(float[] data, int index, int[] scan) {
    for (final int offset : scan) {
      if (data[index] < data[index + offset]) {
        return false;
      }
    }
    return true;
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
    int maximaCount = 0;

    if (isNeighbourCheck()) {
      final boolean[] maximaFlag = getFlagBuffer(data.length);

      final FloatScanCandidate candidates = new FloatScanCandidate();

      for (int y = border; y < maxy - border - 1; y += 2) {
        int xindex = y * maxx + border;
        for (int x = border; x < maxx - border - 1; x += 2, xindex += 2) {
          candidates.init(data, xindex, a);
          candidates.add(data, xindex + 1, b);
          candidates.add(data, xindex + maxx, c);
          candidates.add(data, xindex + maxx + 1, d);

          // Check the remaining region for each candidate to ensure a true maxima
          for (int i = 0; i < candidates.size(); i++) {
            final int maxIndex = candidates.getMaxIndex(i);
            final int[] scan = candidates.getScan(i);

            // Only check ABC using the existing maxima flag
            // since the scan blocks for D have not yet been processed
            final boolean isMaxima = (scan == d) ? isMaxima3x3(data, maxIndex, scan)
                : isMaxima3x3(data, maximaFlag, maxIndex, scan);

            if (isMaxima) {
              maximaFlag[maxIndex] = true;
              maxima[maximaCount++] = maxIndex;
              break;
            }
          }
        }
      }
    } else {
      for (int y = border; y < maxy - border - 1; y += 2) {
        int xindex = y * maxx + border;
        for (int x = border; x < maxx - border - 1; x += 2, xindex += 2) {
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
          if (isMaxima3x3(data, maxIndex, scan)) {
            maxima[maximaCount++] = maxIndex;
          }
        } // end FIND_MAXIMUM
      }
    }

    return truncate(maxima, maximaCount);
  }

  /**
   * Search the data for the index of the maximum in each block of size n*n.
   *
   * <p>E.g. Max [ (i,i+n) x (i,j+n) ] for (i=0; i&lt;maxx; i+=n) x (j=0, j&lt;maxy; j+=n)
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
   * Search the data for the index of the maximum in each block of size n*n.
   *
   * <p>E.g. Max [ (i,i+n) x (i,j+n) ] for (i=0; i&lt;maxx; i+=n) x (j=0, j&lt;maxy; j+=n).
   *
   * <p>If multiple indices within the block have the same value then all are returned.
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
  // The following code is copied directly from above.
  // All 'float' have been replaced with 'int'
  // ----------------------------------------------------

  // CHECKSTYLE.OFF: OverloadMethodsDeclarationOrder

  /**
   * Compute the local-maxima within a 2n+1 block.
   *
   * @param data The input data (packed in YX order)
   * @param maxx The width of the data
   * @param maxy The height of the data
   * @param n The block size
   * @return Indices to the local maxima (index = maxx * y + x)
   */
  public int[] maxFind(int[] data, int maxx, int maxy, int n) {
    final IntFixedList results = getResultsBuffer(data.length);
    final boolean[] maximaFlag = getFlagBuffer(data.length);

    // Boundary control
    final int xwidth = FastMath.min(n, maxx - 1);
    final int ywidth = FastMath.min(n, maxy - 1);
    final int xlimit = maxx - xwidth;
    final int ylimit = maxy - ywidth;

    final int[] offset = new int[(2 * xwidth + 1) * (2 * ywidth + 1) - 1];
    final int[] xoffset = new int[offset.length];
    final int[] yoffset = new int[offset.length];
    int offsetIndex = 0;
    for (int y = -ywidth; y <= ywidth; y++) {
      for (int x = -xwidth; x <= xwidth; x++) {
        if (x != 0 || y != 0) {
          offset[offsetIndex] = maxx * y + x;
          xoffset[offsetIndex] = x;
          yoffset[offsetIndex] = y;
          offsetIndex++;
        }
      }
    }

    // Compare all points
    int index = 0;
    for (int y = 0; y < maxy; y++) {
      FIND_MAXIMUM: for (int x = 0; x < maxx; x++, index++) {
        final int v = data[index];

        // Flag to indicate this pixels has a complete (2n+1) neighbourhood
        final boolean isInnerXy = (y >= ywidth && y < ylimit) && (x >= xwidth && x < xlimit);

        // Sweep neighbourhood
        if (isInnerXy) {
          for (final int off : offset) {
            if (maximaFlag[index + off] || data[index + off] > v) {
              continue FIND_MAXIMUM;
            }
          }
        } else {
          for (int i = offset.length; i-- > 0;) {
            // Get the coords and check if it is within the data
            final int yy = y + yoffset[i];
            final int xx = x + xoffset[i];
            final boolean isWithin = (yy >= 0 && yy < maxy) && (xx >= 0 && xx < maxx);

            if (isWithin && (maximaFlag[index + offset[i]] || data[index + offset[i]] > v)) {
              continue FIND_MAXIMUM;
            }
          }
        }

        results.add(index);
        maximaFlag[index] = true;
      } // end FIND_MAXIMUM
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
    final IntFixedList results = getResultsBuffer(data.length);
    final boolean[] maximaFlag = getFlagBuffer(data.length);

    // Boundary control
    final int xwidth = FastMath.min(n, maxx - 1);
    final int ywidth = FastMath.min(n, maxy - 1);

    final int[] offset = new int[(2 * xwidth + 1) * (2 * ywidth + 1) - 1];
    int offsetIndex = 0;
    for (int y = -ywidth; y <= ywidth; y++) {
      for (int x = -xwidth; x <= xwidth; x++) {
        if (x != 0 || y != 0) {
          offset[offsetIndex] = maxx * y + x;
          offsetIndex++;
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
        for (final int off : offset) {
          if (maximaFlag[index + off] || data[index + off] > v) {
            continue FIND_MAXIMUM;
          }
        }

        results.add(index);
        maximaFlag[index] = true;
      } // end FIND_MAXIMUM
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

    final IntFixedList results = getResultsBuffer(data.length);
    final boolean[] maximaFlag = getFlagBuffer(data.length);

    // Boundary control
    final int xwidth = FastMath.min(n, maxx - 1);
    final int ywidth = FastMath.min(n, maxy - 1);
    final int xlimit = maxx - xwidth;
    final int ylimit = maxy - ywidth;

    final int[] offset = new int[(2 * xwidth + 1) * (2 * ywidth + 1) - 1];
    final int[] xoffset = new int[offset.length];
    final int[] yoffset = new int[offset.length];
    int offsetIndex = 0;
    for (int y = -ywidth; y <= ywidth; y++) {
      for (int x = -xwidth; x <= xwidth; x++) {
        if (x != 0 || y != 0) {
          offset[offsetIndex] = maxx * y + x;
          xoffset[offsetIndex] = x;
          yoffset[offsetIndex] = y;
          offsetIndex++;
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
          for (final int off : offset) {
            if (maximaFlag[index + off] || data[index + off] > v) {
              continue FIND_MAXIMUM;
            }
          }
        } else {
          // Flag to indicate this pixels has a complete (2n+1) neighbourhood
          final boolean isInnerXy = (y >= ywidth && y < ylimit) && (x >= xwidth && x < xlimit);

          // Sweep neighbourhood
          for (int i = offset.length; i-- > 0;) {
            boolean isWithin = isInnerXy;
            if (!isWithin) {
              // Get the coords and check if it is within the data
              final int yy = y + yoffset[i];
              final int xx = x + xoffset[i];
              isWithin = (yy >= 0 && yy < maxy) && (xx >= 0 && xx < maxx);
            }

            if (isWithin && (maximaFlag[index + offset[i]] || data[index + offset[i]] > v)) {
              continue FIND_MAXIMUM;
            }
          }
        }

        results.add(index);
        maximaFlag[index] = true;
      } // end FIND_MAXIMUM
    }

    return results.toArray();
  }

  /**
   * Compute the local-maxima within a 2n+1 block.
   *
   * <p>Uses the 2D block algorithm of Neubeck and Van Gool (2006).
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
   * maxima.
   *
   * <p>Uses the 2D block algorithm of Neubeck and Van Gool (2006).
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
   * Compute the local-maxima within a 2n+1 block.
   *
   * <p>Uses the 2D block algorithm of Neubeck and Van Gool (2006).
   *
   * @param data The input data (packed in YX order)
   * @param maxx The width of the data
   * @param maxy The height of the data
   * @param n The block size
   * @return Indices to the local maxima (index = maxx * y + x)
   */
  public int[] blockFindNxN(int[] data, int maxx, int maxy, int n) {
    int[] maxima;
    int maximaCount = 0;
    final int n1 = n + 1;

    if (isNeighbourCheck()) {
      // Compute all candidate maxima in the NxN blocks. Then check each candidate
      // is a maxima in the 2N+1 region including a check for existing maxima.
      final int[][] blockMaximaCandidates = findBlockMaximaCandidatesNxN(data, maxx, maxy, n);

      maxima = new int[blockMaximaCandidates.length];
      final boolean[] maximaFlag = getFlagBuffer(data.length);

      for (final int[] blockMaxima : blockMaximaCandidates) {
        for (final int index : blockMaxima) {
          if (isMaximaNxN(data, maximaFlag, maxx, maxy, n, 0, n1, index)) {
            maximaFlag[index] = true;
            maxima[maximaCount++] = index;
            break;
          }
        }
      }
    } else {
      final int[] blockMaxima = findBlockMaximaNxN(data, maxx, maxy, n);

      maxima = blockMaxima; // Re-use storage space

      for (final int index : blockMaxima) {
        if (isMaximaNxN(data, maxx, maxy, n, 0, n1, index)) {
          maxima[maximaCount++] = index;
        }
      }
    }

    return truncate(maxima, maximaCount);
  }

  /**
   * Checks if the block maxima is a maxima in the larger region outside the local block, up to +/-
   * n around the index location.
   *
   * @param data The input data (packed in YX order)
   * @param maximaFlag the maxima flag
   * @param maxx The width of the data
   * @param maxy The height of the data
   * @param n The block size
   * @param border the border
   * @param n1 the n+1
   * @param index the index
   * @return true, if is maxima
   */
  protected static boolean isMaximaNxN(int[] data, boolean[] maximaFlag, int maxx, int maxy, int n,
      int border, int n1, int index) {
    final int v = data[index];

    final int mi = index % maxx;
    final int mj = index / maxx;

    // Compare the maxima to the surroundings. Ignore the block region already processed.
    //@formatter:off
    //
    //        (mi-n,mj-n)----------------------------------(mi+n,mj-n)
    //             |aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa|
    //             |aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa|
    //             |aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa|
    //             |aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa|
    //             |aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa|
    //             |bbbbbbbb (i,j)-------------(i+n,j) ccccccccc|
    //             |bbbbbbbbbbb|                  |ccccccccccccc|
    //             |bbbbbbbbbbb|                  |ccccccccccccc|
    //             |bbbbbbbbbbb|     (mi,mj)      |ccccccccccccc|
    //             |bbbbbbbbbbb|                  |ccccccccccccc|
    //             |bbbbbbbbbbb|                  |ccccccccccccc|
    //             |bbbbbbbbbbb|                  |ccccccccccccc|
    //             |bbbbbbbbbbb|                  |ccccccccccccc|
    //             |bbbbbbbb (i,j+n)-----------(i+n,j+n) ccccccc|
    //             |dddddddddddddddddddddddddddddddddddddddddddd|
    //             |dddddddddddddddddddddddddddddddddddddddddddd|
    //             |dddddddddddddddddddddddddddddddddddddddddddd|
    //       (mi-n,mj+n)----------------------------------(mi+n,mj+n)
    //
    //@formatter:on
    // This must be done without over-running boundaries
    final int j = n1 * ((mj - border) / n1) + border; // Blocks n+1 wide
    final int miMinusN = FastMath.max(mi - n, 0);
    final int miPlusN = FastMath.min(mi + n, maxx - 1);
    final int mjMinusN = FastMath.max(mj - n, 0);

    // Neighbour check for existing maxima. This is relevant if the same height.
    // However we cannot just check height as the neighbour may not be the selected maxima
    // within its block.
    // Only check A+B since the blocks for C+D have not yet been processed

    // A
    for (int jj = mjMinusN; jj < j; jj++) {
      final int indexEnd = jj * maxx + miPlusN;
      for (int indexStart = jj * maxx + miMinusN; indexStart <= indexEnd; indexStart++) {
        // A includes neighbour check
        if (v < data[indexStart] || maximaFlag[indexStart]) {
          return false;
        }
      }
    }
    final int i = n1 * ((mi - border) / n1) + border; // Blocks n+1 wide
    final int iPlusN = i + n1;
    final int jPlusN = FastMath.min(j + n, maxy - 1);
    for (int jj = j; jj <= jPlusN; jj++) {
      // B
      final int indexEnd = jj * maxx + i;
      for (int indexStart = jj * maxx + miMinusN; indexStart < indexEnd; indexStart++) {
        // B includes neighbour check
        if (v < data[indexStart] || maximaFlag[indexStart]) {
          return false;
        }
      }

      // C
      final int indexEnd2 = jj * maxx + miPlusN;
      for (int indexStart = jj * maxx + iPlusN; indexStart <= indexEnd2; indexStart++) {
        if (v < data[indexStart]) {
          return false;
        }
      }
    }
    // D
    final int mjPlusN = FastMath.min(mj + n, maxy - 1);
    for (int jj = jPlusN + 1; jj <= mjPlusN; jj++) {
      final int indexEnd = jj * maxx + miPlusN;
      for (int indexStart = jj * maxx + miMinusN; indexStart <= indexEnd; indexStart++) {
        if (v < data[indexStart]) {
          return false;
        }
      }
    }

    return true;
  }

  /**
   * Checks if the block maxima is a maxima in the larger region outside the local block, up to +/-
   * n around the index location.
   *
   * @param data The input data (packed in YX order)
   * @param maxx The width of the data
   * @param maxy The height of the data
   * @param n The block size
   * @param border the border
   * @param n1 the n+1
   * @param index the index
   * @return true, if is maxima
   */
  protected static boolean isMaximaNxN(int[] data, int maxx, int maxy, int n, int border, int n1,
      int index) {
    final int v = data[index];

    final int mi = index % maxx;
    final int mj = index / maxx;

    // Compare the maxima to the surroundings. Ignore the block region already processed.
    //@formatter:off
    //
    //        (mi-n,mj-n)----------------------------------(mi+n,mj-n)
    //             |aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa|
    //             |aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa|
    //             |aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa|
    //             |aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa|
    //             |aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa|
    //             |bbbbbbbb (i,j)-------------(i+n,j) ccccccccc|
    //             |bbbbbbbbbbb|                  |ccccccccccccc|
    //             |bbbbbbbbbbb|                  |ccccccccccccc|
    //             |bbbbbbbbbbb|     (mi,mj)      |ccccccccccccc|
    //             |bbbbbbbbbbb|                  |ccccccccccccc|
    //             |bbbbbbbbbbb|                  |ccccccccccccc|
    //             |bbbbbbbbbbb|                  |ccccccccccccc|
    //             |bbbbbbbbbbb|                  |ccccccccccccc|
    //             |bbbbbbbb (i,j+n)-----------(i+n,j+n) ccccccc|
    //             |dddddddddddddddddddddddddddddddddddddddddddd|
    //             |dddddddddddddddddddddddddddddddddddddddddddd|
    //             |dddddddddddddddddddddddddddddddddddddddddddd|
    //       (mi-n,mj+n)----------------------------------(mi+n,mj+n)
    //
    //@formatter:on
    // This must be done without over-running boundaries
    final int j = n1 * ((mj - border) / n1) + border; // Blocks n+1 wide
    final int miMinusN = FastMath.max(mi - n, 0);
    final int miPlusN = FastMath.min(mi + n, maxx - 1);
    final int mjMinusN = FastMath.max(mj - n, 0);

    // A
    for (int jj = mjMinusN; jj < j; jj++) {
      final int indexEnd = jj * maxx + miPlusN;
      for (int indexStart = jj * maxx + miMinusN; indexStart <= indexEnd; indexStart++) {
        if (v < data[indexStart]) {
          return false;
        }
      }
    }
    final int i = n1 * ((mi - border) / n1) + border; // Blocks n+1 wide
    final int iPlusN = i + n1;
    final int jPlusN = FastMath.min(j + n, maxy - 1);
    for (int jj = j; jj <= jPlusN; jj++) {
      // B
      final int indexEnd = jj * maxx + i;
      for (int indexStart = jj * maxx + miMinusN; indexStart < indexEnd; indexStart++) {
        if (v < data[indexStart]) {
          return false;
        }
      }

      // C
      final int indexEnd2 = jj * maxx + miPlusN;
      for (int indexStart = jj * maxx + iPlusN; indexStart <= indexEnd2; indexStart++) {
        if (v < data[indexStart]) {
          return false;
        }
      }
    }
    // D
    final int mjPlusN = FastMath.min(mj + n, maxy - 1);
    for (int jj = jPlusN + 1; jj <= mjPlusN; jj++) {
      final int indexEnd = jj * maxx + miPlusN;
      for (int indexStart = jj * maxx + miMinusN; indexStart <= indexEnd; indexStart++) {
        if (v < data[indexStart]) {
          return false;
        }
      }
    }

    return true;
  }

  /**
   * Compute the local-maxima within a 2n+1 block. An inner boundary of N is ignored as potential
   * maxima.
   *
   * <p>Uses the 2D block algorithm of Neubeck and Van Gool (2006).
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
    int maximaCount = 0;
    final int n1 = n + 1;

    if (isNeighbourCheck()) {
      // Compute all candidate maxima in the NxN blocks. Then check each candidate
      // is a maxima in the 2N+1 region including a check for existing maxima.
      final int[][] blockMaximaCandidates =
          findBlockMaximaCandidatesNxNInternal(data, maxx, maxy, n, border);

      maxima = new int[blockMaximaCandidates.length];
      final boolean[] maximaFlag = getFlagBuffer(data.length);

      for (final int[] blockMaxima : blockMaximaCandidates) {
        for (final int index : blockMaxima) {
          if (isMaximaNxN(data, maximaFlag, maxx, maxy, n, border, n1, index)) {
            maximaFlag[index] = true;
            maxima[maximaCount++] = index;
            break;
          }
        }
      }
    } else {
      final int[] blockMaxima = findBlockMaximaNxNInternal(data, maxx, maxy, n, border);

      maxima = blockMaxima; // Re-use storage space

      for (final int index : blockMaxima) {
        if (isMaximaNxN(data, maxx, maxy, n, border, n1, index)) {
          maxima[maximaCount++] = index;
        }
      }
    }

    return truncate(maxima, maximaCount);
  }


  /**
   * Search the data for the index of the maximum in each block of size n*n.
   *
   * <p>E.g. Max [ (i,i+n) x (i,j+n) ] for (i=0; i&lt;maxx; i+=n) x (j=0, j&lt;maxy; j+=n)
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
    final int n1 = n + 1;

    // The number of blocks in x and y
    final int xblocks = getBlocks(maxx, n1);
    final int yblocks = getBlocks(maxy, n1);

    // The final index in each dimension (where an incomplete block is found).
    // This equals maxx/maxy if the number of blocks fits exactly.
    final int xfinal = n1 * (maxx / n1);
    final int yfinal = n1 * (maxy / n1);

    final int[] maxima = new int[xblocks * yblocks];

    int block = 0;
    for (int y = 0; y < maxy; y += n1) {
      for (int x = 0; x < maxx; x += n1) {
        // Find the sweep size in each direction
        final int xsize = (x == xfinal) ? maxx - xfinal : n1;
        int ysize = (y == yfinal) ? maxy - yfinal : n1;

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
   * is ignored as potential maxima.
   *
   * <p>E.g. Max [ (i,i+n) x (i,j+n) ] for (i=n; i&lt;maxx-n; i+=n) x (j=n, j&lt;maxy-n; j+=n)
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
    final int n1 = n + 1;

    // The number of blocks in x and y. Subtract the boundary blocks.
    final int xblocks = getBlocks(maxx - border, n1);
    final int yblocks = getBlocks(maxy - border, n1);

    if (xblocks < 1 || yblocks < 1) {
      return ArrayUtils.EMPTY_INT_ARRAY;
    }

    // The final index in each dimension (where an incomplete block is found).
    // This equals maxx/maxy if the number of blocks fits exactly.
    final int xfinal = n1 * ((maxx - 2 * border) / n1) + border;
    final int yfinal = n1 * ((maxy - 2 * border) / n1) + border;

    final int[] maxima = new int[xblocks * yblocks];

    int block = 0;
    for (int y = border; y < maxy - border; y += n1) {
      for (int x = border; x < maxx - border; x += n1) {

        // Find the sweep size in each direction
        final int xsize = (x == xfinal) ? maxx - xfinal - border : n1;
        int ysize = (y == yfinal) ? maxy - yfinal - border : n1;

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
   * Search the data for the index of the maximum in each block of size 2*2.
   *
   * <p>E.g. Max [ (i,i+n) x (i,j+n) ] for (i=0; i&lt;maxx; i+=n) x (j=0, j&lt;maxy; j+=n)
   *
   * @param data The input data (packed in YX order)
   * @param maxx The width of the data
   * @param maxy The height of the data
   * @return The maxima indices
   */
  public int[] findBlockMaxima2x2(int[] data, int maxx, int maxy) {
    // Optimised for 2x2 block

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
    //@formatter:off
    // x             xfinal
    // |             |  maxx
    // |             |  |
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
    //@formatter:on

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
        maxima[block] = yfinal * maxx + xfinal;
      }
    }

    return maxima;
  }

  /**
   * Search the data for the index of the maximum in each block of size n*n.
   *
   * <p>E.g. Max [ (i,i+n) x (i,j+n) ] for (i=0; i&lt;maxx; i+=n) x (j=0, j&lt;maxy; j+=n).
   *
   * <p>If multiple indices within the block have the same value then all are returned.
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
    final int n1 = n + 1;

    // The number of blocks in x and y
    final int xblocks = getBlocks(maxx, n1);
    final int yblocks = getBlocks(maxy, n1);

    // The final index in each dimension (where an incomplete block is found).
    // This equals maxx/maxy if the number of blocks fits exactly.
    final int xfinal = n1 * (maxx / n1);
    final int yfinal = n1 * (maxy / n1);

    final int[][] maxima = new int[xblocks * yblocks][];
    final IntFixedList list = new IntFixedList(n1 * n1);

    int block = 0;
    for (int y = 0; y < maxy; y += n1) {
      for (int x = 0; x < maxx; x += n1) {
        // Find the sweep size in each direction
        final int xsize = (x == xfinal) ? maxx - xfinal : n1;
        int ysize = (y == yfinal) ? maxy - yfinal : n1;

        int index = y * maxx + x;
        int max = intMin();

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
   * is ignored as potential maxima.
   *
   * <p>E.g. Max [ (i,i+n) x (i,j+n) ] for (i=n; i&lt;maxx-n; i+=n) x (j=n, j&lt;maxy-n; j+=n).
   *
   * <p>If multiple indices within the block have the same value then all are returned.
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
    final int n1 = n + 1;

    // The number of blocks in x and y. Subtract the boundary blocks.
    final int xblocks = getBlocks(maxx - border, n1);
    final int yblocks = getBlocks(maxy - border, n1);

    if (xblocks < 1 || yblocks < 1) {
      return new int[0][0];
    }

    // The final index in each dimension (where an incomplete block is found).
    // This equals maxx/maxy if the number of blocks fits exactly.
    final int xfinal = n1 * ((maxx - 2 * border) / n1) + border;
    final int yfinal = n1 * ((maxy - 2 * border) / n1) + border;

    final int[][] maxima = new int[xblocks * yblocks][];
    final IntFixedList list = new IntFixedList(n1 * n1);

    int block = 0;
    for (int y = border; y < maxy - border; y += n1) {
      for (int x = border; x < maxx - border; x += n1) {
        // Find the sweep size in each direction
        final int xsize = (x == xfinal) ? maxx - xfinal - border : n1;
        int ysize = (y == yfinal) ? maxy - yfinal - border : n1;

        int index = y * maxx + x;
        int max = intMin();

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
   * Search the data for the index of the maximum in each block of size 2*2.
   *
   * <p>E.g. Max [ (i,i+n) x (i,j+n) ] for (i=0; i&lt;maxx; i+=n) x (j=0, j&lt;maxy; j+=n).
   *
   * <p>If multiple indices within the block have the same value then all are returned.
   *
   * @param data The input data (packed in YX order)
   * @param maxx The width of the data
   * @param maxy The height of the data
   * @return The maxima indices
   */
  public int[][] findBlockMaximaCandidates2x2(int[] data, int maxx, int maxy) {
    // Optimised for 2x2 block

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
    //@formatter:off
    // x             xfinal
    // |             |  maxx
    // |             |  |
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
    //@formatter:on

    // Working storage for indices
    final IntFixedList i1 = new IntFixedList(4);
    final IntFixedList i2 = new IntFixedList(4);

    int block = 0;
    for (int y = 0; y < yfinal; y += 2) {
      // A
      int xindex = y * maxx;
      for (int x = 0; x < xfinal; x += 2) {
        // Compare 2x2 block
        getIndices(i1, data, xindex, xindex + 1);
        getIndices(i2, data, xindex + maxx, xindex + maxx + 1);

        if (data[i1.get(0)] > data[i2.get(0)]) {
          maxima[block++] = i1.toArray();
        } else if (data[i1.get(0)] < data[i2.get(0)]) {
          maxima[block++] = i2.toArray();
        } else {
          // Tied so merge the candidates
          i1.add(i2);
          maxima[block++] = i1.toArray();
        }
        xindex += 2;
      }
      // B
      if (xfinal != maxx) {
        // Compare 1x2 block
        final int index = y * maxx + xfinal;
        getIndices(i1, data, index, index + maxx);
        maxima[block++] = i1.toArray();
      }
    }
    if (yfinal != maxy) {
      // C
      int xindex = yfinal * maxx;
      for (int x = 0; x < xfinal; x += 2) {
        // Compare 2x1 block
        getIndices(i1, data, xindex, xindex + 1);
        maxima[block++] = i1.toArray();
        xindex += 2;
      }
      // D
      if (xfinal != maxx) {
        // Compare 1x1 block
        maxima[block] = new int[] {yfinal * maxx + xfinal};
      }
    }

    return maxima;
  }

  /**
   * Gets the indices of the maxima within the data from the provided indices.
   *
   * <p>Finds 1 index or 2 indices if both have the same value.
   *
   * @param indices the indices
   * @param data the data
   * @param index1 the first index
   * @param index2 the second index
   */
  protected static void getIndices(IntFixedList indices, int[] data, int index1, int index2) {
    indices.clear();
    if (data[index1] > data[index2]) {
      indices.add(index1);
    } else if (data[index1] < data[index2]) {
      indices.add(index2);
    } else {
      // Tied
      indices.add(index1);
      indices.add(index2);
    }
  }

  /**
   * Compute the local-maxima within a 3x3 block.
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
    final int[] newData = expand(data, maxx, maxy, newx, newy);

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
    int maximaCount = 0;

    if (isNeighbourCheck()) {
      final boolean[] maximaFlag = getFlagBuffer(newData.length);

      final IntScanCandidate candidates = new IntScanCandidate();

      for (int y = 0; y < maxy; y += 2) {
        int xindex = (y + 1) * newx + 1;
        for (int x = 0; x < maxx; x += 2, xindex += 2) {
          candidates.init(newData, xindex, a);
          candidates.add(newData, xindex + 1, b);
          candidates.add(newData, xindex + newx, c);
          candidates.add(newData, xindex + newx + 1, d);

          // Check the remaining region for each candidate to ensure a true maxima
          for (int i = 0; i < candidates.size(); i++) {
            final int maxIndex = candidates.getMaxIndex(i);
            final int[] scan = candidates.getScan(i);

            // Only check ABC using the existing maxima flag
            // since the scan blocks for D have not yet been processed
            final boolean isMaxima = (scan == d) ? isMaxima3x3(newData, maxIndex, scan)
                : isMaxima3x3(newData, maximaFlag, maxIndex, scan);

            if (isMaxima) {
              // Remap the maxima
              final int xx = maxIndex % newx;
              final int yy = maxIndex / newx;

              maximaFlag[maxIndex] = true;
              maxima[maximaCount++] = (yy - 1) * maxx + xx - 1;
              break;
            }
          }
        }
      }
    } else {
      for (int y = 0; y < maxy; y += 2) {
        int xindex = (y + 1) * newx + 1;
        for (int x = 0; x < maxx; x += 2, xindex += 2) {
          int[] scan = a;
          int maxIndex = xindex;
          if (newData[maxIndex] < newData[xindex + 1]) {
            scan = b;
            maxIndex = xindex + 1;
          }
          if (newData[maxIndex] < newData[xindex + newx]) {
            scan = c;
            maxIndex = xindex + newx;
          }
          if (newData[maxIndex] < newData[xindex + newx + 1]) {
            scan = d;
            maxIndex = xindex + newx + 1;
          }

          // Check the remaining region
          if (isMaxima3x3(newData, maxIndex, scan)) {
            // Remap the maxima
            final int xx = maxIndex % newx;
            final int yy = maxIndex / newx;

            maxima[maximaCount++] = (yy - 1) * maxx + xx - 1;
          }
        }
      }
    }

    return truncate(maxima, maximaCount);
  }


  /**
   * Checks if is a maxima using the offsets to scan the remaining region around the core 2x2 block.
   *
   * @param data the data
   * @param maximaFlag the maxima flag
   * @param index the index
   * @param scan the scan offsets
   * @return true, if is a maxima
   */
  protected static boolean isMaxima3x3(int[] data, boolean[] maximaFlag, int index, int[] scan) {
    for (final int offset : scan) {
      if (data[index] < data[index + offset] || maximaFlag[index + offset]) {
        return false;
      }
    }
    return true;
  }

  /**
   * Checks if is a maxima using the offsets to scan the remaining region around the core 2x2 block.
   *
   * @param data the data
   * @param index the index
   * @param scan the scan offsets
   * @return true, if is a maxima
   */
  protected static boolean isMaxima3x3(int[] data, int index, int[] scan) {
    for (final int offset : scan) {
      if (data[index] < data[index + offset]) {
        return false;
      }
    }
    return true;
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
    int maximaCount = 0;

    if (isNeighbourCheck()) {
      final boolean[] maximaFlag = getFlagBuffer(data.length);

      final IntScanCandidate candidates = new IntScanCandidate();

      for (int y = border; y < maxy - border - 1; y += 2) {
        int xindex = y * maxx + border;
        for (int x = border; x < maxx - border - 1; x += 2, xindex += 2) {
          candidates.init(data, xindex, a);
          candidates.add(data, xindex + 1, b);
          candidates.add(data, xindex + maxx, c);
          candidates.add(data, xindex + maxx + 1, d);

          // Check the remaining region for each candidate to ensure a true maxima
          for (int i = 0; i < candidates.size(); i++) {
            final int maxIndex = candidates.getMaxIndex(i);
            final int[] scan = candidates.getScan(i);

            // Only check ABC using the existing maxima flag
            // since the scan blocks for D have not yet been processed
            final boolean isMaxima = (scan == d) ? isMaxima3x3(data, maxIndex, scan)
                : isMaxima3x3(data, maximaFlag, maxIndex, scan);

            if (isMaxima) {
              maximaFlag[maxIndex] = true;
              maxima[maximaCount++] = maxIndex;
              break;
            }
          }
        }
      }
    } else {
      for (int y = border; y < maxy - border - 1; y += 2) {
        int xindex = y * maxx + border;
        for (int x = border; x < maxx - border - 1; x += 2, xindex += 2) {
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
          if (isMaxima3x3(data, maxIndex, scan)) {
            maxima[maximaCount++] = maxIndex;
          }
        } // end FIND_MAXIMUM
      }
    }

    return truncate(maxima, maximaCount);
  }

  /**
   * Search the data for the index of the maximum in each block of size n*n.
   *
   * <p>E.g. Max [ (i,i+n) x (i,j+n) ] for (i=0; i&lt;maxx; i+=n) x (j=0, j&lt;maxy; j+=n)
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
   * Search the data for the index of the maximum in each block of size n*n.
   *
   * <p>E.g. Max [ (i,i+n) x (i,j+n) ] for (i=0; i&lt;maxx; i+=n) x (j=0, j&lt;maxy; j+=n).
   *
   * <p>If multiple indices within the block have the same value then all are returned.
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
