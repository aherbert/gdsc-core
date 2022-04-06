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
 * Copyright (C) 2011 - 2022 Alex Herbert
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

import uk.ac.sussex.gdsc.core.utils.IntFixedList;

/**
 * Computes the local maxima. Allows filtering of the maxima using simple height and width checks.
 *
 * <p>Note: The neighbour check within the block find algorithm does not match that in the
 * NonMaximumSuppression class.
 */
public class FilteredNonMaximumSuppression extends NonMaximumSuppression {

  // There is a lot of code duplication is this class. The parent could be
  // modified to have hooks at the appropriate point for validation. However
  // this class is rarely used and so the parent is left unmodified to ensure it
  // runs optimally.

  /** The background. */
  private float background;
  /** The fraction above background. */
  private float fractionAboveBackground;
  /** The minimum height. */
  private float minimumHeight;
  /** The minimum width. */
  private float minimumWidth;

  /**
   * Instantiates a new filtered non maximum suppression.
   */
  public FilteredNonMaximumSuppression() {
    // Nothing to do
  }

  /**
   * Instantiates a new filtered non maximum suppression.
   *
   * @param source the source
   */
  private FilteredNonMaximumSuppression(FilteredNonMaximumSuppression source) {
    super(source);
    this.background = source.background;
    this.fractionAboveBackground = source.fractionAboveBackground;
    this.minimumHeight = source.minimumHeight;
    this.minimumWidth = source.minimumWidth;
  }

  /**
   * Sets the fraction above background.
   *
   * @param fractionAboveBackground the fraction above background.
   */
  public void setFractionAboveBackground(float fractionAboveBackground) {
    this.fractionAboveBackground = fractionAboveBackground;
  }

  /**
   * Gets the fraction above background.
   *
   * @return the fraction above background.
   */
  public float getFractionAboveBackground() {
    return fractionAboveBackground;
  }

  /**
   * Sets the minimum height.
   *
   * @param minimumHeight the new minimum height
   */
  public void setMinimumHeight(float minimumHeight) {
    this.minimumHeight = minimumHeight;
  }

  /**
   * Gets the minimum height.
   *
   * @return the minimum height
   */
  public float getMinimumHeight() {
    return minimumHeight;
  }

  /**
   * Sets the minimum width.
   *
   * @param minimumWidth the new minimum width
   */
  public void setMinimumWidth(float minimumWidth) {
    this.minimumWidth = minimumWidth;
  }

  /**
   * Gets the minimum width.
   *
   * @return the minimum width
   */
  public float getMinimumWidth() {
    return minimumWidth;
  }

  /**
   * Sets the background.
   *
   * @param background the new background
   */
  public void setBackground(float background) {
    this.background = background;
  }

  /**
   * Gets the background.
   *
   * @return the background.
   */
  public float getBackground() {
    return background;
  }

  /**
   * Get the height threshold for peaks using the current minimum height and fraction above
   * background.
   *
   * @return the height threshold
   */
  public float getHeightThreshold() {
    float heightThreshold = minimumHeight + background;
    if (fractionAboveBackground != 1) {
      final float heightThreshold2 = background / (1 - fractionAboveBackground);
      heightThreshold = Math.max(heightThreshold, heightThreshold2);
    }
    return heightThreshold;
  }

  /**
   * Find the {@code float} value for half the maximum.
   *
   * @param background the background
   * @param maximum the maximum
   * @return The half-maximum value (accounting for background)
   */
  private static float floatHalfMaximum(float background, float maximum) {
    return (maximum - background) * 0.5f;
  }

  /**
   * Find the {@code int} value for half the maximum.
   *
   * @param background the background
   * @param maximum the maximum
   * @return The half-maximum value (accounting for background)
   */
  private static int intHalfMaximum(int background, int maximum) {
    return (int) (0.5 + (maximum - background) * 0.5);
  }

  @Override
  public FilteredNonMaximumSuppression copy() {
    return new FilteredNonMaximumSuppression(this);
  }

  // ----------------------------------------------------
  // NOTE:
  // Copy from here to replace 'float' with 'int'
  // ----------------------------------------------------

  /**
   * {@inheritDoc}
   *
   * <p>Any maxima below the configured fraction above background are ignored. Fraction =
   * (Max-background)/Max within the 2n+1 neighbourhood. Maxima below the minimum height (above
   * background) or the minimum peak-width at half maximum in any dimension are ignored.
   *
   * @param data The input data (packed in YX order)
   * @param maxx The width of the data
   * @param maxy The height of the data
   * @param n The block size
   * @return Indices to the local maxima (index = maxx * y + x)
   */
  @Override
  public int[] maxFind(float[] data, int maxx, int maxy, int n) {
    final IntFixedList results = getResultsBuffer(data.length / 4);
    final boolean[] maximaFlag = getFlagBuffer(data.length);

    // Boundary control
    final int xwidth = Math.min(n, maxx - 1);
    final int ywidth = Math.min(n, maxy - 1);
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

    final float heightThreshold = getHeightThreshold();
    final float floatBackground = background;

    // Compare all points
    int index = 0;
    for (int y = 0; y < maxy; y++) {
      FIND_MAXIMUM: for (int x = 0; x < maxx; x++, index++) {
        final float v = data[index];
        if (v < heightThreshold) {
          continue;
        }

        // Flag to indicate this pixels has a complete (2n+1) neighbourhood
        final boolean isInnerXy = (y >= ywidth && y < ylimit) && (x >= xwidth && x < xlimit);

        // Sweep neighbourhood
        if (isInnerXy) {
          for (final int shift : offset) {
            if (maximaFlag[index + shift] || data[index + shift] > v) {
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

        // Check the maximum width
        if (isAboveMinimumWidth(data, maxx, maxy, index, x, y, floatBackground)) {
          results.add(index);
          maximaFlag[index] = true;
        }
      } // end FIND_MAXIMUM
    }

    return results.toArray();

  }

  /**
   * Checks if the maxima is above the minimum width. Computes the half height of the maxima and
   * then computes the peak width at half maxima (PWHM) in the X and Y dimensions and compares them
   * to the minimum width.
   *
   * @param data the data
   * @param maxx the maxx
   * @param maxy the maxy
   * @param index the index
   * @param x the x
   * @param y the y
   * @param background the background
   * @return true if PWHM is above minimum width
   */
  private boolean isAboveMinimumWidth(float[] data, int maxx, int maxy, int index, int x, int y,
      float background) {
    // Check the maximum width
    if (minimumWidth > 0) {
      final float halfMax = floatHalfMaximum(background, data[index]);

      // Get the width at half maximum in x
      int index2;
      // Scan right
      int x1 = x + 1;
      index2 = index + 1;
      while (x1 < maxx && data[index2] > halfMax) {
        x1++;
        index2++;
      }
      // Scan left
      int x2 = x - 1;
      index2 = index - 1;
      while (x2 >= 0 && data[index2] > halfMax) {
        x2--;
        index2--;
      }
      // Check PWHM in x
      if (x1 - x2 < minimumWidth) {
        return false;
      }

      // Get the width at half maximum in y
      // Scan up
      int y1 = y + 1;
      index2 = index + maxx;
      while (y1 < maxy && data[index2] > halfMax) {
        y1++;
        index2 += maxx;
      }
      // Scan down
      int y2 = y - 1;
      index2 = index - 1;
      while (y2 >= 0 && data[index2] > halfMax) {
        y2--;
        index2 -= maxx;
      }
      // Check PWHM in y
      if (y1 - y2 < minimumWidth) {
        return false;
      }
    }
    return true;
  }

  /**
   * Checks if the maxima is above the minimum width. Computes the half height of the maxima and
   * then computes the peak width at half maxima (PWHM) in the X and Y dimensions and compares them
   * to the minimum width.
   *
   * @param data the data
   * @param maxx the maxx
   * @param maxy the maxy
   * @param index the index
   * @param background the background
   * @return true if PWHM is above minimum width
   */
  private boolean isAboveMinimumWidth(float[] data, int maxx, int maxy, int index,
      float background) {
    if (minimumWidth > 0) {
      final int x = index % maxx;
      final int y = index / maxx;
      return isAboveMinimumWidth(data, maxx, maxy, index, x, y, background);
    }
    return true;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Any maxima below the configured fraction above background are ignored. Fraction =
   * (Max-background)/Max within the 2n+1 neighbourhood. Maxima below the minimum height (above
   * background) or the minimum peak-width at half maximum in any dimension are ignored.
   *
   * @param data The input data (packed in YX order)
   * @param maxx The width of the data
   * @param maxy The height of the data
   * @param n The block size
   * @return Indices to the local maxima (index = maxx * y + x)
   */
  @Override
  public int[] maxFindInternal(float[] data, int maxx, int maxy, int n) {
    final IntFixedList results = getResultsBuffer(data.length / 4);
    final boolean[] maximaFlag = getFlagBuffer(data.length);

    // Boundary control
    final int xwidth = Math.min(n, maxx - 1);
    final int ywidth = Math.min(n, maxy - 1);

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

    final float heightThreshold = getHeightThreshold();
    final float floatBackground = background;

    // Compare all points
    for (int y = n; y < maxy - n; y++) {
      int index = y * maxx + n;
      FIND_MAXIMUM: for (int x = n; x < maxx - n; x++, index++) {
        final float v = data[index];
        if (v < heightThreshold) {
          continue;
        }

        // Sweep neighbourhood -
        // No check for boundaries as this should be an internal sweep.
        for (final int shift : offset) {
          if (maximaFlag[index + shift] || data[index + shift] > v) {
            continue FIND_MAXIMUM;
          }
        }

        // Check the maximum width
        if (isAboveMinimumWidth(data, maxx, maxy, index, x, y, floatBackground)) {
          results.add(index);
          maximaFlag[index] = true;
        }
      } // end FIND_MAXIMUM
    }

    return results.toArray();
  }

  /**
   * {@inheritDoc}
   *
   * <p>Any maxima below the configured fraction above background are ignored. Fraction =
   * (Max-background)/Max within the 2n+1 neighbourhood. Maxima below the minimum height (above
   * background) or the minimum peak-width at half maximum in any dimension are ignored.
   *
   * @param data The input data (packed in YX order)
   * @param maxx The width of the data
   * @param maxy The height of the data
   * @param n The block size
   * @param border The internal border
   * @return Indices to the local maxima (index = maxx * y + x)
   */
  @Override
  public int[] maxFindInternal(float[] data, int maxx, int maxy, int n, int border) {
    if (n == border) {
      // Faster algorithm as there is no requirement for bounds checking.
      return maxFindInternal(data, maxx, maxy, n);
    }

    final IntFixedList results = getResultsBuffer(data.length / 4);
    final boolean[] maximaFlag = getFlagBuffer(data.length);

    // Boundary control
    final int xwidth = Math.min(n, maxx - 1);
    final int ywidth = Math.min(n, maxy - 1);
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

    final float heightThreshold = getHeightThreshold();
    final float floatBackground = background;

    // Compare all points
    for (int y = border; y < maxy - border; y++) {
      int index = y * maxx + border;
      FIND_MAXIMUM: for (int x = border; x < maxx - border; x++, index++) {

        final float v = data[index];
        if (v < heightThreshold) {
          continue;
        }

        if (inner) {
          for (final int shift : offset) {
            if (maximaFlag[index + shift] || data[index + shift] > v) {
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

        // Check the maximum width
        if (isAboveMinimumWidth(data, maxx, maxy, index, x, y, floatBackground)) {
          results.add(index);
          maximaFlag[index] = true;
        }
      } // end FIND_MAXIMUM
    }

    return results.toArray();
  }

  /**
   * {@inheritDoc}
   *
   * <p>Any maxima below the configured fraction above background are ignored. Fraction =
   * (Max-background)/Max within the 2n+1 neighbourhood. Maxima below the minimum height (above
   * background) or the minimum peak-width at half maximum in any dimension are ignored.
   *
   * @param data The input data (packed in YX order)
   * @param maxx The width of the data
   * @param maxy The height of the data
   * @param n The block size
   * @return Indices to the local maxima (index = maxx * y + x)
   */
  @Override
  public int[] blockFindNxN(float[] data, int maxx, int maxy, int n) {
    int[] maxima;
    int maximaCount = 0;
    final int n1 = n + 1;
    final float heightThreshold = getHeightThreshold();
    final float floatBackground = background;

    if (isNeighbourCheck()) {
      // Compute all candidate maxima in the NxN blocks. Then check each candidate
      // is a maxima in the 2N+1 region including a check for existing maxima.
      final int[][] blockMaximaCandidates = findBlockMaximaCandidatesNxN(data, maxx, maxy, n);

      maxima = new int[blockMaximaCandidates.length];
      final boolean[] maximaFlag = getFlagBuffer(data.length);

      for (final int[] blockMaxima : blockMaximaCandidates) {
        for (final int index : blockMaxima) {
          if (data[index] >= heightThreshold
              && isMaximaNxN(data, maximaFlag, maxx, maxy, n, 0, n1, index)
              && isAboveMinimumWidth(data, maxx, maxy, index, floatBackground)) {
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
        if (data[index] >= heightThreshold && isMaximaNxN(data, maxx, maxy, n, 0, n1, index)
            && isAboveMinimumWidth(data, maxx, maxy, index, floatBackground)) {
          maxima[maximaCount++] = index;
        }
      }
    }

    return truncate(maxima, maximaCount);
  }

  /**
   * {@inheritDoc}
   *
   * <p>Any maxima below the configured fraction above background are ignored. Fraction =
   * (Max-background)/Max within the 2n+1 neighbourhood. Maxima below the minimum height (above
   * background) or the minimum peak-width at half maximum in any dimension are ignored.
   *
   * @param data The input data (packed in YX order)
   * @param maxx The width of the data
   * @param maxy The height of the data
   * @param n The block size
   * @param border The internal border
   * @return Indices to the local maxima (index = maxx * y + x)
   */
  @Override
  public int[] blockFindNxNInternal(float[] data, int maxx, int maxy, int n, int border) {
    int[] maxima;
    int maximaCount = 0;
    final int n1 = n + 1;
    final float heightThreshold = getHeightThreshold();
    final float floatBackground = background;

    if (isNeighbourCheck()) {
      // Compute all candidate maxima in the NxN blocks. Then check each candidate
      // is a maxima in the 2N+1 region including a check for existing maxima.
      final int[][] blockMaximaCandidates =
          findBlockMaximaCandidatesNxNInternal(data, maxx, maxy, n, border);

      maxima = new int[blockMaximaCandidates.length];
      final boolean[] maximaFlag = getFlagBuffer(data.length);

      for (final int[] blockMaxima : blockMaximaCandidates) {
        for (final int index : blockMaxima) {
          if (data[index] >= heightThreshold
              && isMaximaNxN(data, maximaFlag, maxx, maxy, n, border, n1, index)
              && isAboveMinimumWidth(data, maxx, maxy, index, floatBackground)) {
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
        if (data[index] >= heightThreshold && isMaximaNxN(data, maxx, maxy, n, border, n1, index)
            && isAboveMinimumWidth(data, maxx, maxy, index, floatBackground)) {
          maxima[maximaCount++] = index;
        }
      }
    }

    return truncate(maxima, maximaCount);
  }

  /**
   * {@inheritDoc}
   *
   * <p>Any maxima below the configured fraction above background are ignored. Fraction =
   * (Max-background)/Max within the 3x3 neighbourhood. Maxima below the minimum height (above
   * background) or the minimum peak-width at half maximum in any dimension are ignored.
   *
   * <p>Note: The height thresholding step is ignored if the height threshold is zero.
   *
   * @param data The input data (packed in YX order)
   * @param maxx The width of the data
   * @param maxy The height of the data
   * @return Indices to the local maxima (index = maxx * y + x)
   */
  @Override
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
    final int[] a = {-newx - 1, -newx, -newx + 1, -1, +newx - 1};
    final int[] b = {-newx - 1, -newx, -newx + 1, +1, +newx + 1};
    final int[] c = {-newx - 1, -1, +newx - 1, +newx, +newx + 1};
    final int[] d = {-newx + 1, +1, +newx - 1, +newx, +newx + 1};

    final int[] maxima = new int[xblocks * yblocks];
    int maximaCount = 0;
    final float heightThreshold = getHeightThreshold();
    final float floatBackground = background;

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

            final boolean isMaxima = newData[maxIndex] >= heightThreshold
                // Only check ABC using the existing maxima flag
                // since the scan blocks for D have not yet been processed
                && ((scan == d) ? isMaxima3x3(newData, maxIndex, scan)
                    : isMaxima3x3(newData, maximaFlag, maxIndex, scan));

            if (isMaxima && isAboveMinimumWidth(newData, newx, newy, maxIndex, floatBackground)) {
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
          if (newData[maxIndex] >= heightThreshold && isMaxima3x3(newData, maxIndex, scan)
              && isAboveMinimumWidth(newData, newx, newy, maxIndex, floatBackground)) {
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
   * {@inheritDoc}
   *
   * <p>Any maxima below the configured fraction above background are ignored. Fraction =
   * (Max-background)/Max within the 3x3 neighbourhood. Maxima below the minimum height (above
   * background) or the minimum peak-width at half maximum in any dimension are ignored.
   *
   * <p>Note: The height thresholding step is ignored if the height threshold is zero.
   *
   * @param data The input data (packed in YX order)
   * @param maxx The width of the data
   * @param maxy The height of the data
   * @param border The internal border
   * @return Indices to the local maxima (index = maxx * y + x)
   */
  @Override
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
    final int[] a = {-maxx - 1, -maxx, -maxx + 1, -1, +maxx - 1};
    final int[] b = {-maxx - 1, -maxx, -maxx + 1, +1, +maxx + 1};
    final int[] c = {-maxx - 1, -1, +maxx - 1, +maxx, +maxx + 1};
    final int[] d = {-maxx + 1, +1, +maxx - 1, +maxx, +maxx + 1};

    final int[] maxima = new int[xblocks * yblocks];
    int maximaCount = 0;
    final float heightThreshold = getHeightThreshold();
    final float floatBackground = background;

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

            final boolean isMaxima = data[maxIndex] >= heightThreshold
                // Only check ABC using the existing maxima flag
                // since the scan blocks for D have not yet been processed
                && ((scan == d) ? isMaxima3x3(data, maxIndex, scan)
                    : isMaxima3x3(data, maximaFlag, maxIndex, scan));

            if (isMaxima && isAboveMinimumWidth(data, maxx, maxy, maxIndex, floatBackground)) {
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
          if (data[maxIndex] >= heightThreshold && isMaxima3x3(data, maxIndex, scan)
              && isAboveMinimumWidth(data, maxx, maxy, maxIndex, floatBackground)) {
            maxima[maximaCount++] = maxIndex;
          }
        } // end FIND_MAXIMUM
      }
    }

    return truncate(maxima, maximaCount);
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
   * <p>Any maxima below the configured fraction above background are ignored. Fraction =
   * (Max-background)/Max within the 2n+1 neighbourhood. Maxima below the minimum height (above
   * background) or the minimum peak-width at half maximum in any dimension are ignored.
   *
   * @param data The input data (packed in YX order)
   * @param maxx The width of the data
   * @param maxy The height of the data
   * @param n The block size
   * @return Indices to the local maxima (index = maxx * y + x)
   */
  @Override
  public int[] maxFind(int[] data, int maxx, int maxy, int n) {
    final IntFixedList results = getResultsBuffer(data.length / 4);
    final boolean[] maximaFlag = getFlagBuffer(data.length);

    // Boundary control
    final int xwidth = Math.min(n, maxx - 1);
    final int ywidth = Math.min(n, maxy - 1);
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

    final int heightThreshold = (int) getHeightThreshold();
    final int intBackground = (int) background;

    // Compare all points
    int index = 0;
    for (int y = 0; y < maxy; y++) {
      FIND_MAXIMUM: for (int x = 0; x < maxx; x++, index++) {
        final int v = data[index];
        if (v < heightThreshold) {
          continue;
        }

        // Flag to indicate this pixels has a complete (2n+1) neighbourhood
        final boolean isInnerXy = (y >= ywidth && y < ylimit) && (x >= xwidth && x < xlimit);

        // Sweep neighbourhood
        if (isInnerXy) {
          for (final int shift : offset) {
            if (maximaFlag[index + shift] || data[index + shift] > v) {
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

        // Check the maximum width
        if (isAboveMinimumWidth(data, maxx, maxy, index, x, y, intBackground)) {
          results.add(index);
          maximaFlag[index] = true;
        }
      } // end FIND_MAXIMUM
    }

    return results.toArray();

  }

  /**
   * Checks if the maxima is above the minimum width. Computes the half height of the maxima and
   * then computes the peak width at half maxima (PWHM) in the X and Y dimensions and compares them
   * to the minimum width.
   *
   * @param data the data
   * @param maxx the maxx
   * @param maxy the maxy
   * @param index the index
   * @param x the x
   * @param y the y
   * @param background the background
   * @return true if PWHM is above minimum width
   */
  private boolean isAboveMinimumWidth(int[] data, int maxx, int maxy, int index, int x, int y,
      int background) {
    // Check the maximum width
    if (minimumWidth > 0) {
      final int halfMax = intHalfMaximum(background, data[index]);

      // Get the width at half maximum in x
      int index2;
      // Scan right
      int x1 = x + 1;
      index2 = index + 1;
      while (x1 < maxx && data[index2] > halfMax) {
        x1++;
        index2++;
      }
      // Scan left
      int x2 = x - 1;
      index2 = index - 1;
      while (x2 >= 0 && data[index2] > halfMax) {
        x2--;
        index2--;
      }
      // Check PWHM in x
      if (x1 - x2 < minimumWidth) {
        return false;
      }

      // Get the width at half maximum in y
      // Scan up
      int y1 = y + 1;
      index2 = index + maxx;
      while (y1 < maxy && data[index2] > halfMax) {
        y1++;
        index2 += maxx;
      }
      // Scan down
      int y2 = y - 1;
      index2 = index - 1;
      while (y2 >= 0 && data[index2] > halfMax) {
        y2--;
        index2 -= maxx;
      }
      // Check PWHM in y
      if (y1 - y2 < minimumWidth) {
        return false;
      }
    }
    return true;
  }

  /**
   * Checks if the maxima is above the minimum width. Computes the half height of the maxima and
   * then computes the peak width at half maxima (PWHM) in the X and Y dimensions and compares them
   * to the minimum width.
   *
   * @param data the data
   * @param maxx the maxx
   * @param maxy the maxy
   * @param index the index
   * @param background the background
   * @return true if PWHM is above minimum width
   */
  private boolean isAboveMinimumWidth(int[] data, int maxx, int maxy, int index, int background) {
    if (minimumWidth > 0) {
      final int x = index % maxx;
      final int y = index / maxx;
      return isAboveMinimumWidth(data, maxx, maxy, index, x, y, background);
    }
    return true;
  }

  /**
   * Compute the local-maxima within a 2n+1 block. An inner boundary of N is ignored as potential
   * maxima.
   *
   * <p>Any maxima below the configured fraction above background are ignored. Fraction =
   * (Max-background)/Max within the 2n+1 neighbourhood. Maxima below the minimum height (above
   * background) or the minimum peak-width at half maximum in any dimension are ignored.
   *
   * @param data The input data (packed in YX order)
   * @param maxx The width of the data
   * @param maxy The height of the data
   * @param n The block size
   * @return Indices to the local maxima (index = maxx * y + x)
   */
  @Override
  public int[] maxFindInternal(int[] data, int maxx, int maxy, int n) {
    final IntFixedList results = getResultsBuffer(data.length / 4);
    final boolean[] maximaFlag = getFlagBuffer(data.length);

    // Boundary control
    final int xwidth = Math.min(n, maxx - 1);
    final int ywidth = Math.min(n, maxy - 1);

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

    final int heightThreshold = (int) getHeightThreshold();
    final int intBackground = (int) background;

    // Compare all points
    for (int y = n; y < maxy - n; y++) {
      int index = y * maxx + n;
      FIND_MAXIMUM: for (int x = n; x < maxx - n; x++, index++) {
        final int v = data[index];
        if (v < heightThreshold) {
          continue;
        }

        // Sweep neighbourhood -
        // No check for boundaries as this should be an internal sweep.
        for (final int shift : offset) {
          if (maximaFlag[index + shift] || data[index + shift] > v) {
            continue FIND_MAXIMUM;
          }
        }

        // Check the maximum width
        if (isAboveMinimumWidth(data, maxx, maxy, index, x, y, intBackground)) {
          results.add(index);
          maximaFlag[index] = true;
        }
      } // end FIND_MAXIMUM
    }

    return results.toArray();
  }

  /**
   * Compute the local-maxima within a 2n+1 block. An inner boundary is ignored as potential maxima.
   *
   * <p>Any maxima below the configured fraction above background are ignored. Fraction =
   * (Max-background)/Max within the 2n+1 neighbourhood. Maxima below the minimum height (above
   * background) or the minimum peak-width at half maximum in any dimension are ignored.
   *
   * @param data The input data (packed in YX order)
   * @param maxx The width of the data
   * @param maxy The height of the data
   * @param n The block size
   * @param border The internal border
   * @return Indices to the local maxima (index = maxx * y + x)
   */
  @Override
  public int[] maxFindInternal(int[] data, int maxx, int maxy, int n, int border) {
    if (n == border) {
      // Faster algorithm as there is no requirement for bounds checking.
      return maxFindInternal(data, maxx, maxy, n);
    }

    final IntFixedList results = getResultsBuffer(data.length / 4);
    final boolean[] maximaFlag = getFlagBuffer(data.length);

    // Boundary control
    final int xwidth = Math.min(n, maxx - 1);
    final int ywidth = Math.min(n, maxy - 1);
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

    final int heightThreshold = (int) getHeightThreshold();
    final int intBackground = (int) background;

    // Compare all points
    for (int y = border; y < maxy - border; y++) {
      int index = y * maxx + border;
      FIND_MAXIMUM: for (int x = border; x < maxx - border; x++, index++) {

        final int v = data[index];
        if (v < heightThreshold) {
          continue;
        }

        if (inner) {
          for (final int shift : offset) {
            if (maximaFlag[index + shift] || data[index + shift] > v) {
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

        // Check the maximum width
        if (isAboveMinimumWidth(data, maxx, maxy, index, x, y, intBackground)) {
          results.add(index);
          maximaFlag[index] = true;
        }
      } // end FIND_MAXIMUM
    }

    return results.toArray();
  }

  /**
   * Compute the local-maxima within a 2n+1 block.
   *
   * <p>Any maxima below the configured fraction above background are ignored. Fraction =
   * (Max-background)/Max within the 2n+1 neighbourhood. Maxima below the minimum height (above
   * background) or the minimum peak-width at half maximum in any dimension are ignored.
   *
   * <p>Uses the 2D block algorithm of Neubeck and Van Gool (2006).
   *
   * @param data The input data (packed in YX order)
   * @param maxx The width of the data
   * @param maxy The height of the data
   * @param n The block size
   * @return Indices to the local maxima (index = maxx * y + x)
   */
  @Override
  public int[] blockFindNxN(int[] data, int maxx, int maxy, int n) {
    int[] maxima;
    int maximaCount = 0;
    final int n1 = n + 1;
    final int heightThreshold = (int) getHeightThreshold();
    final int intBackground = (int) background;

    if (isNeighbourCheck()) {
      // Compute all candidate maxima in the NxN blocks. Then check each candidate
      // is a maxima in the 2N+1 region including a check for existing maxima.
      final int[][] blockMaximaCandidates = findBlockMaximaCandidatesNxN(data, maxx, maxy, n);

      maxima = new int[blockMaximaCandidates.length];
      final boolean[] maximaFlag = getFlagBuffer(data.length);

      for (final int[] blockMaxima : blockMaximaCandidates) {
        for (final int index : blockMaxima) {
          if (data[index] >= heightThreshold
              && isMaximaNxN(data, maximaFlag, maxx, maxy, n, 0, n1, index)
              && isAboveMinimumWidth(data, maxx, maxy, index, intBackground)) {
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
        if (data[index] >= heightThreshold && isMaximaNxN(data, maxx, maxy, n, 0, n1, index)
            && isAboveMinimumWidth(data, maxx, maxy, index, intBackground)) {
          maxima[maximaCount++] = index;
        }
      }
    }

    return truncate(maxima, maximaCount);
  }

  /**
   * Compute the local-maxima within a 2n+1 block. An inner boundary of N is ignored as potential
   * maxima.
   *
   * <p>Any maxima below the configured fraction above background are ignored. Fraction =
   * (Max-background)/Max within the 2n+1 neighbourhood. Maxima below the minimum height (above
   * background) or the minimum peak-width at half maximum in any dimension are ignored.
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
  @Override
  public int[] blockFindNxNInternal(int[] data, int maxx, int maxy, int n, int border) {
    int[] maxima;
    int maximaCount = 0;
    final int n1 = n + 1;
    final int heightThreshold = (int) getHeightThreshold();
    final int intBackground = (int) background;

    if (isNeighbourCheck()) {
      // Compute all candidate maxima in the NxN blocks. Then check each candidate
      // is a maxima in the 2N+1 region including a check for existing maxima.
      final int[][] blockMaximaCandidates =
          findBlockMaximaCandidatesNxNInternal(data, maxx, maxy, n, border);

      maxima = new int[blockMaximaCandidates.length];
      final boolean[] maximaFlag = getFlagBuffer(data.length);

      for (final int[] blockMaxima : blockMaximaCandidates) {
        for (final int index : blockMaxima) {
          if (data[index] >= heightThreshold
              && isMaximaNxN(data, maximaFlag, maxx, maxy, n, border, n1, index)
              && isAboveMinimumWidth(data, maxx, maxy, index, intBackground)) {
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
        if (data[index] >= heightThreshold && isMaximaNxN(data, maxx, maxy, n, border, n1, index)
            && isAboveMinimumWidth(data, maxx, maxy, index, intBackground)) {
          maxima[maximaCount++] = index;
        }
      }
    }

    return truncate(maxima, maximaCount);
  }

  /**
   * Compute the local-maxima within a 3x3 block.
   *
   * <p>Any maxima below the configured fraction above background are ignored. Fraction =
   * (Max-background)/Max within the 3x3 neighbourhood. Maxima below the minimum height (above
   * background) or the minimum peak-width at half maximum in any dimension are ignored.
   *
   * <p>Note: The height thresholding step is ignored if the height threshold is zero.
   *
   * @param data The input data (packed in YX order)
   * @param maxx The width of the data
   * @param maxy The height of the data
   * @return Indices to the local maxima (index = maxx * y + x)
   */
  @Override
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
    final int[] a = {-newx - 1, -newx, -newx + 1, -1, +newx - 1};
    final int[] b = {-newx - 1, -newx, -newx + 1, +1, +newx + 1};
    final int[] c = {-newx - 1, -1, +newx - 1, +newx, +newx + 1};
    final int[] d = {-newx + 1, +1, +newx - 1, +newx, +newx + 1};

    final int[] maxima = new int[xblocks * yblocks];
    int maximaCount = 0;
    final int heightThreshold = (int) getHeightThreshold();
    final int intBackground = (int) background;

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

            final boolean isMaxima = newData[maxIndex] >= heightThreshold
                // Only check ABC using the existing maxima flag
                // since the scan blocks for D have not yet been processed
                && ((scan == d) ? isMaxima3x3(newData, maxIndex, scan)
                    : isMaxima3x3(newData, maximaFlag, maxIndex, scan));

            if (isMaxima && isAboveMinimumWidth(newData, newx, newy, maxIndex, intBackground)) {
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
          if (newData[maxIndex] >= heightThreshold && isMaxima3x3(newData, maxIndex, scan)
              && isAboveMinimumWidth(newData, newx, newy, maxIndex, intBackground)) {
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
   * Compute the local-maxima within a 3x3 block. An inner boundary of 1 is ignored as potential
   * maxima on the top and left, and a boundary of 1 or 2 on the right or bottom (depending if the
   * image is even/odd dimensions).
   *
   * <p>Any maxima below the configured fraction above background are ignored. Fraction =
   * (Max-background)/Max within the 3x3 neighbourhood. Maxima below the minimum height (above
   * background) or the minimum peak-width at half maximum in any dimension are ignored.
   *
   * <p>Note: The height thresholding step is ignored if the height threshold is zero.
   *
   * @param data The input data (packed in YX order)
   * @param maxx The width of the data
   * @param maxy The height of the data
   * @param border The internal border
   * @return Indices to the local maxima (index = maxx * y + x)
   */
  @Override
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
    final int[] a = {-maxx - 1, -maxx, -maxx + 1, -1, +maxx - 1};
    final int[] b = {-maxx - 1, -maxx, -maxx + 1, +1, +maxx + 1};
    final int[] c = {-maxx - 1, -1, +maxx - 1, +maxx, +maxx + 1};
    final int[] d = {-maxx + 1, +1, +maxx - 1, +maxx, +maxx + 1};

    final int[] maxima = new int[xblocks * yblocks];
    int maximaCount = 0;
    final int heightThreshold = (int) getHeightThreshold();
    final int intBackground = (int) background;

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

            final boolean isMaxima = data[maxIndex] >= heightThreshold
                // Only check ABC using the existing maxima flag
                // since the scan blocks for D have not yet been processed
                && ((scan == d) ? isMaxima3x3(data, maxIndex, scan)
                    : isMaxima3x3(data, maximaFlag, maxIndex, scan));

            if (isMaxima && isAboveMinimumWidth(data, maxx, maxy, maxIndex, intBackground)) {
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
          if (data[maxIndex] >= heightThreshold && isMaxima3x3(data, maxIndex, scan)
              && isAboveMinimumWidth(data, maxx, maxy, maxIndex, intBackground)) {
            maxima[maximaCount++] = maxIndex;
          }
        } // end FIND_MAXIMUM
      }
    }

    return truncate(maxima, maximaCount);
  }
}
