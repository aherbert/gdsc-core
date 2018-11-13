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

import uk.ac.sussex.gdsc.core.utils.IntFixedList;

import org.apache.commons.math3.util.FastMath;

/**
 * Computes the local maxima. Allows filtering of the maxima using simple height and width checks. .
 *
 * <p>Note: The neighbour check within the block find algorithm does not match that in the
 * NonMaximumSuppression class.
 */
public class FilteredNonMaximumSuppression extends NonMaximumSuppression {

  // TODO: Update to use the same block find algorithm as NonMaximumSuppression.
  // There is a lot of code duplication is this class. The parent could be
  // modified to have hooks at the appropriate point for validation. However
  // this class is rarely used and so the parent is left unmodified to ensure it
  // runs fast.

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
      heightThreshold = FastMath.max(heightThreshold, heightThreshold2);
    }
    return heightThreshold;
  }

  /**
   * Float half maximum.
   *
   * @param background the background
   * @param maximum the maximum
   * @return The half-maximum value (accounting for background)
   */
  private static float floatHalfMaximum(float background, float maximum) {
    return (maximum - background) * 0.5f;
  }

  /**
   * Int half maximum.
   *
   * @param background the background
   * @param maximum the maximum
   * @return The half-maximum value (accounting for background)
   */
  private static int intHalfMaximum(int background, int maximum) {
    return (int) (0.5 + (maximum - background) * 0.5);
  }

  /**
   * Create a copy.
   *
   * @return the copy
   */
  @Override
  public FilteredNonMaximumSuppression copy() {
    return new FilteredNonMaximumSuppression(this);
  }

  // ----------------------------------------------------
  // NOTE:
  // Copy from here to replace 'float' with 'int'
  // ----------------------------------------------------

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
  public int[] maxFind(float[] data, int maxx, int maxy, int n) {
    final IntFixedList results = getResultsBuffer(data.length / 4);
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
    final float heightThreshold = getHeightThreshold();
    final float floatBackground = background;

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
          for (int i = 0; i < offset.length; i++) {
            if (maximaFlag[index + offset[i]] || data[index + offset[i]] > v) {
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
        if (minimumWidth > 0) {
          // Get the width at half maximum.
          final float v_half = floatHalfMaximum(floatBackground, v);
          int index2;
          // Scan right
          int x1 = x + 1;
          index2 = index + 1;
          while (x1 < maxx && data[index2] > v_half) {
            x1++;
            index2++;
          }
          // Scan left
          int x2 = x - 1;
          index2 = index - 1;
          while (x2 >= 0 && data[index2] > v_half) {
            x2--;
            index2--;
          }
          if (x1 - x2 < minimumWidth) {
            continue;
          }
          // Scan up
          int y1 = y + 1;
          index2 = index + maxx;
          while (y1 < maxy && data[index2] > v_half) {
            y1++;
            index2 += maxx;
          }
          // Scan down
          int y2 = y - 1;
          index2 = index - 1;
          while (y2 >= 0 && data[index2] > v_half) {
            y2--;
            index2 -= maxx;
          }
          if (y1 - y2 < minimumWidth) {
            continue;
          }
        }

        results.add(index);
        maximaFlag[index] = true;
      } // end FIND_MAXIMA
    }

    return results.toArray();

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
  public int[] maxFindInternal(float[] data, int maxx, int maxy, int n) {
    final IntFixedList results = getResultsBuffer(data.length / 4);
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
    final float heightThreshold = getHeightThreshold();
    final float floatBackground = background;

    for (int y = n; y < maxy - n; y++) {
      int index = y * maxx + n;
      FIND_MAXIMUM: for (int x = n; x < maxx - n; x++, index++) {
        final float v = data[index];
        if (v < heightThreshold) {
          continue;
        }

        // Sweep neighbourhood -
        // No check for boundaries as this should be an internal sweep.
        for (int i = 0; i < offset.length; i++) {
          if (maximaFlag[index + offset[i]] || data[index + offset[i]] > v) {
            continue FIND_MAXIMUM;
          }
        }

        // Check the maximum width
        if (minimumWidth > 0) {
          final float v_half = floatHalfMaximum(floatBackground, v);
          int index2;
          // Scan right
          int x1 = x + 1;
          index2 = index + 1;
          while (x1 < maxx && data[index2] > v_half) {
            x1++;
            index2++;
          }
          // Scan left
          int x2 = x - 1;
          index2 = index - 1;
          while (x2 >= 0 && data[index2] > v_half) {
            x2--;
            index2--;
          }
          if (x1 - x2 < minimumWidth) {
            continue;
          }
          // Scan up
          int y1 = y + 1;
          index2 = index + maxx;
          while (y1 < maxy && data[index2] > v_half) {
            y1++;
            index2 += maxx;
          }
          // Scan down
          int y2 = y - 1;
          index2 = index - 1;
          while (y2 >= 0 && data[index2] > v_half) {
            y2--;
            index2 -= maxx;
          }
          if (y1 - y2 < minimumWidth) {
            continue;
          }
        }

        results.add(index);
        maximaFlag[index] = true;
      } // end FIND_MAXIMA
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
  public int[] maxFindInternal(float[] data, int maxx, int maxy, int n, int border) {
    if (n == border) {
      // Faster algorithm as there is no requirement for bounds checking.
      return maxFindInternal(data, maxx, maxy, n);
    }

    final IntFixedList results = getResultsBuffer(data.length / 4);
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
    final float heightThreshold = getHeightThreshold();
    final float floatBackground = background;

    for (int y = border; y < maxy - border; y++) {
      int index = y * maxx + border;
      FIND_MAXIMUM: for (int x = border; x < maxx - border; x++, index++) {

        final float v = data[index];
        if (v < heightThreshold) {
          continue;
        }

        if (inner) {
          for (int i = 0; i < offset.length; i++) {
            if (maximaFlag[index + offset[i]] || data[index + offset[i]] > v) {
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
        if (minimumWidth > 0) {
          final float v_half = floatHalfMaximum(floatBackground, v);
          int index2;
          // Scan right
          int x1 = x + 1;
          index2 = index + 1;
          while (x1 < maxx && data[index2] > v_half) {
            x1++;
            index2++;
          }
          // Scan left
          int x2 = x - 1;
          index2 = index - 1;
          while (x2 >= 0 && data[index2] > v_half) {
            x2--;
            index2--;
          }
          if (x1 - x2 < minimumWidth) {
            continue;
          }
          // Scan up
          int y1 = y + 1;
          index2 = index + maxx;
          while (y1 < maxy && data[index2] > v_half) {
            y1++;
            index2 += maxx;
          }
          // Scan down
          int y2 = y - 1;
          index2 = index - 1;
          while (y2 >= 0 && data[index2] > v_half) {
            y2--;
            index2 -= maxx;
          }
          if (y1 - y2 < minimumWidth) {
            continue;
          }
        }

        results.add(index);
        maximaFlag[index] = true;
      } // end FIND_MAXIMA
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
  public int[] blockFindNxN(float[] data, int maxx, int maxy, int n) {
    final int[] blockMaxima = findBlockMaximaNxN(data, maxx, maxy, n);
    int maximaCount = 0;
    final float heightThreshold = getHeightThreshold();
    final float floatBackground = background;

    boolean[] maximaFlag = null;
    if (isNeighbourCheck()) {
      maximaFlag = getFlagBuffer(data.length);
    }

    FIND_MAXIMUM: for (final int index : blockMaxima) {

      final float v = data[index];

      if (v < heightThreshold) {
        continue;
      }

      final int x = index % maxx;
      final int y = index / maxx;

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
      final int mi = x;
      final int mj = y;
      final int i = (n + 1) * (mi / (n + 1));
      final int j = (n + 1) * (mj / (n + 1));
      final int i_plus_n = FastMath.min(i + n + 1, maxx - 1);
      final int j_plus_n = FastMath.min(j + n + 1, maxy - 1);
      final int mi_minus_n = FastMath.max(mi - n, 0);
      final int mi_plus_n = FastMath.min(mi + n, maxx - 1);
      final int mj_minus_n = FastMath.max(mj - n, 0);
      final int mj_plus_n = FastMath.min(mj + n, maxy - 1);

      // A
      for (int jj = mj_minus_n; jj < j; jj++) {
        int indexStart = jj * maxx + mi_minus_n;
        final int indexEnd = jj * maxx + mi_plus_n;
        for (; indexStart <= indexEnd; indexStart++) {
          if (v < data[indexStart]) {
            continue FIND_MAXIMUM;
          }
        }
      }
      for (int jj = j; jj < j_plus_n; jj++) {
        // B
        {
          int indexStart = jj * maxx + mi_minus_n;
          final int indexEnd = jj * maxx + i;
          for (; indexStart < indexEnd; indexStart++) {
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
            if (v < data[indexStart]) {
              continue FIND_MAXIMUM;
            }
          }
        }
      }
      // D
      for (int jj = j_plus_n; jj <= mj_plus_n; jj++) {
        int indexStart = jj * maxx + mi_minus_n;
        final int indexEnd = jj * maxx + mi_plus_n;
        for (; indexStart <= indexEnd; indexStart++) {
          if (v < data[indexStart]) {
            continue FIND_MAXIMUM;
          }
        }
      }

      if (maximaFlag != null) {
        // A
        for (int jj = mj_minus_n; jj < j; jj++) {
          int indexStart = jj * maxx + mi_minus_n;
          final int indexEnd = jj * maxx + mi_plus_n;
          for (; indexStart <= indexEnd; indexStart++) {
            if (maximaFlag[indexStart]) {
              continue FIND_MAXIMUM;
            }
          }
        }
        for (int jj = j; jj < j_plus_n; jj++) {
          // B
          {
            int indexStart = jj * maxx + mi_minus_n;
            final int indexEnd = jj * maxx + i;
            for (; indexStart < indexEnd; indexStart++) {
              if (maximaFlag[indexStart]) {
                continue FIND_MAXIMUM;
              }
            }
          }

          // C
          {
            int indexStart = jj * maxx + i_plus_n;
            final int indexEnd = jj * maxx + mi_plus_n;
            for (; indexStart <= indexEnd; indexStart++) {
              if (maximaFlag[indexStart]) {
                continue FIND_MAXIMUM;
              }
            }
          }
        }
        // D
        for (int jj = j_plus_n; jj <= mj_plus_n; jj++) {
          int indexStart = jj * maxx + mi_minus_n;
          final int indexEnd = jj * maxx + mi_plus_n;
          for (; indexStart <= indexEnd; indexStart++) {
            if (maximaFlag[indexStart]) {
              continue FIND_MAXIMUM;
            }
          }
        }
      }

      // Check the maximum width
      if (minimumWidth > 0) {
        final float v_half = floatHalfMaximum(floatBackground, v);
        int index2;
        // Scan right
        int x1 = x + 1;
        index2 = index + 1;
        while (x1 < maxx && data[index2] > v_half) {
          x1++;
          index2++;
        }
        // Scan left
        int x2 = x - 1;
        index2 = index - 1;
        while (x2 >= 0 && data[index2] > v_half) {
          x2--;
          index2--;
        }
        if (x1 - x2 < minimumWidth) {
          continue;
        }
        // Scan up
        int y1 = y + 1;
        index2 = index + maxx;
        while (y1 < maxy && data[index2] > v_half) {
          y1++;
          index2 += maxx;
        }
        // Scan down
        int y2 = y - 1;
        index2 = index - 1;
        while (y2 >= 0 && data[index2] > v_half) {
          y2--;
          index2 -= maxx;
        }
        if (y1 - y2 < minimumWidth) {
          continue;
        }
      }

      // Re-use storage space
      blockMaxima[maximaCount++] = index;
      if (maximaFlag != null) {
        maximaFlag[index] = true;
      }
    } // end FIND_MAXIMA

    return truncate(blockMaxima, maximaCount);
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
  public int[] blockFindNxNInternal(float[] data, int maxx, int maxy, int n, int border) {
    final int[] blockMaxima = findBlockMaximaNxNInternal(data, maxx, maxy, n, border);
    int maximaCount = 0;
    final float heightThreshold = getHeightThreshold();
    final float floatBackground = background;

    boolean[] maximaFlag = null;
    if (isNeighbourCheck()) {
      maximaFlag = getFlagBuffer(data.length);
    }

    FIND_MAXIMUM: for (final int index : blockMaxima) {
      final float v = data[index];

      if (v < heightThreshold) {
        continue;
      }

      final int x = index % maxx;
      final int y = index / maxx;

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
      // No check for over-running boundaries since this is the internal version
      final int mi = x;
      final int mj = y;
      final int i = (n + 1) * ((mi - border) / (n + 1)) + border; // Blocks n+1 wide
      final int j = (n + 1) * ((mj - border) / (n + 1)) + border; // Blocks n+1 wide
      // The block boundaries will have been truncated on the final block. Ensure this is swept
      final int i_plus_n = FastMath.min(i + n + 1, maxx - border - 1);
      final int j_plus_n = FastMath.min(j + n + 1, maxy - border - 1);
      final int mi_minus_n = FastMath.max(mi - n, 0);
      final int mi_plus_n = FastMath.min(mi + n, maxx - 1);
      final int mj_minus_n = FastMath.max(mj - n, 0);
      final int mj_plus_n = FastMath.min(mj + n, maxy - 1);

      // A
      for (int jj = mj_minus_n; jj < j; jj++) {
        int indexStart = jj * maxx + mi_minus_n;
        final int indexEnd = jj * maxx + mi_plus_n;
        for (; indexStart <= indexEnd; indexStart++) {
          if (v < data[indexStart]) {
            continue FIND_MAXIMUM;
          }
        }
      }
      for (int jj = j; jj < j_plus_n; jj++) {
        // B
        {
          int indexStart = jj * maxx + mi_minus_n;
          final int indexEnd = jj * maxx + i;
          for (; indexStart < indexEnd; indexStart++) {
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
            if (v < data[indexStart]) {
              continue FIND_MAXIMUM;
            }
          }
        }
      }
      // D
      for (int jj = j_plus_n; jj <= mj_plus_n; jj++) {
        int indexStart = jj * maxx + mi_minus_n;
        final int indexEnd = jj * maxx + mi_plus_n;
        for (; indexStart <= indexEnd; indexStart++) {
          if (v < data[indexStart]) {
            continue FIND_MAXIMUM;
          }
        }
      }

      if (maximaFlag != null) {
        // A
        for (int jj = mj_minus_n; jj < j; jj++) {
          int indexStart = jj * maxx + mi_minus_n;
          final int indexEnd = jj * maxx + mi_plus_n;
          for (; indexStart <= indexEnd; indexStart++) {
            if (maximaFlag[indexStart]) {
              continue FIND_MAXIMUM;
            }
          }
        }
        for (int jj = j; jj < j_plus_n; jj++) {
          // B
          {
            int indexStart = jj * maxx + mi_minus_n;
            final int indexEnd = jj * maxx + i;
            for (; indexStart < indexEnd; indexStart++) {
              if (maximaFlag[indexStart]) {
                continue FIND_MAXIMUM;
              }
            }
          }

          // C
          {
            int indexStart = jj * maxx + i_plus_n;
            final int indexEnd = jj * maxx + mi_plus_n;
            for (; indexStart <= indexEnd; indexStart++) {
              if (maximaFlag[indexStart]) {
                continue FIND_MAXIMUM;
              }
            }
          }
        }
        // D
        for (int jj = j_plus_n; jj <= mj_plus_n; jj++) {
          int indexStart = jj * maxx + mi_minus_n;
          final int indexEnd = jj * maxx + mi_plus_n;
          for (; indexStart <= indexEnd; indexStart++) {
            if (maximaFlag[indexStart]) {
              continue FIND_MAXIMUM;
            }
          }
        }
      }

      // Check the maximum width
      if (minimumWidth > 0) {
        final float v_half = floatHalfMaximum(floatBackground, v);
        int index2;
        // Scan right
        int x1 = x + 1;
        index2 = index + 1;
        while (x1 < maxx && data[index2] > v_half) {
          x1++;
          index2++;
        }
        // Scan left
        int x2 = x - 1;
        index2 = index - 1;
        while (x2 >= 0 && data[index2] > v_half) {
          x2--;
          index2--;
        }
        if (x1 - x2 < minimumWidth) {
          continue;
        }
        // Scan up
        int y1 = y + 1;
        index2 = index + maxx;
        while (y1 < maxy && data[index2] > v_half) {
          y1++;
          index2 += maxx;
        }
        // Scan down
        int y2 = y - 1;
        index2 = index - 1;
        while (y2 >= 0 && data[index2] > v_half) {
          y2--;
          index2 -= maxx;
        }
        if (y1 - y2 < minimumWidth) {
          continue;
        }
      }

      // Re-use storage space
      blockMaxima[maximaCount++] = index;
      if (maximaFlag != null) {
        maximaFlag[index] = true;
      }
    } // end FIND_MAXIMA

    return truncate(blockMaxima, maximaCount);
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

    final int[] maxima = new int[xblocks * yblocks];

    final float heightThreshold = getHeightThreshold();
    final float floatBackground = background;
    final boolean validations = (heightThreshold > 0 || minimumWidth > 0 || isNeighbourCheck());

    boolean[] maximaFlag = null;
    if (isNeighbourCheck()) {
      maximaFlag = getFlagBuffer(data.length);
    }

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

    int maximaCount = 0;
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

        if (validations) {
          if (data[maxIndex] < heightThreshold) {
            continue;
          }

          if (maximaFlag != null) {
            for (final int offset : scan) {
              if (maximaFlag[maxIndex + offset]) {
                continue FIND_MAXIMUM;
              }
            }
          }

          // Check the maximum width
          if (minimumWidth > 0) {
            final int x0 = maxIndex % maxx;
            final int y0 = maxIndex / maxx;

            // Get the width at half maximum.
            final float v_half = floatHalfMaximum(floatBackground, data[maxIndex]);
            int index2;
            // Scan right
            int x1 = x0 + 1;
            index2 = maxIndex + 1;
            while (x1 < maxx && data[index2] > v_half) {
              x1++;
              index2++;
            }
            // Scan left
            int x2 = x0 - 1;
            index2 = maxIndex - 1;
            while (x2 >= 0 && data[index2] > v_half) {
              x2--;
              index2--;
            }
            if (x1 - x2 < minimumWidth) {
              continue;
            }
            // Scan up
            int y1 = y0 + 1;
            index2 = maxIndex + newx;
            while (y1 < maxy && data[index2] > v_half) {
              y1++;
              index2 += newx;
            }
            // Scan down
            int y2 = y0 - 1;
            index2 = maxIndex - newx;
            while (y2 >= 0 && data[index2] > v_half) {
              y2--;
              index2 -= newx;
            }
            if (y1 - y2 < minimumWidth) {
              continue;
            }
          }

          if (maximaFlag != null) {
            maximaFlag[maxIndex] = true;
          }
        }

        // Remap the maxima
        final int xx = maxIndex % newx;
        final int yy = maxIndex / newx;

        maxima[maximaCount++] = (yy - 1) * maxx + xx - 1;
      } // end FIND_MAXIMA
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
  public int[] blockFind3x3Internal(float[] data, int maxx, int maxy, int border) {
    if (border < 1) {
      return blockFind3x3(data, maxx, maxy);
    }

    // The number of blocks in x and y
    final int xblocks = getBlocks(maxx - border, 2);
    final int yblocks = getBlocks(maxy - border, 2);

    final int[] maxima = new int[xblocks * yblocks];

    final float heightThreshold = getHeightThreshold();
    final float floatBackground = background;
    final boolean validations = (heightThreshold > 0 || minimumWidth > 0 || isNeighbourCheck());

    boolean[] maximaFlag = null;
    if (isNeighbourCheck()) {
      maximaFlag = getFlagBuffer(data.length);
    }

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

    int maximaCount = 0;
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

        if (validations) {
          if (data[maxIndex] < heightThreshold) {
            continue;
          }

          if (maximaFlag != null) {
            for (final int offset : scan) {
              if (maximaFlag[maxIndex + offset]) {
                continue FIND_MAXIMUM;
              }
            }
          }

          // Check the maximum width
          if (minimumWidth > 0) {
            final int x0 = maxIndex % maxx;
            final int y0 = maxIndex / maxx;

            // Get the width at half maximum.
            final float v_half = floatHalfMaximum(floatBackground, data[maxIndex]);
            int index2;
            // Scan right
            int x1 = x0 + 1;
            index2 = maxIndex + 1;
            while (x1 < maxx && data[index2] > v_half) {
              x1++;
              index2++;
            }
            // Scan left
            int x2 = x0 - 1;
            index2 = maxIndex - 1;
            while (x2 >= 0 && data[index2] > v_half) {
              x2--;
              index2--;
            }
            if (x1 - x2 < minimumWidth) {
              continue;
            }
            // Scan up
            int y1 = y0 + 1;
            index2 = maxIndex + maxx;
            while (y1 < maxy && data[index2] > v_half) {
              y1++;
              index2 += maxx;
            }
            // Scan down
            int y2 = y0 - 1;
            index2 = maxIndex - maxx;
            while (y2 >= 0 && data[index2] > v_half) {
              y2--;
              index2 -= maxx;
            }
            if (y1 - y2 < minimumWidth) {
              continue;
            }
          }

          if (maximaFlag != null) {
            maximaFlag[maxIndex] = true;
          }
        }

        maxima[maximaCount++] = maxIndex;
      } // end FIND_MAXIMA
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
    final int heightThreshold = (int) getHeightThreshold();
    final int intBackground = (int) background;

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
          for (int i = 0; i < offset.length; i++) {
            if (maximaFlag[index + offset[i]] || data[index + offset[i]] > v) {
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
        if (minimumWidth > 0) {
          // Get the width at half maximum.
          final int v_half = intHalfMaximum(intBackground, v);
          int index2;
          // Scan right
          int x1 = x + 1;
          index2 = index + 1;
          while (x1 < maxx && data[index2] > v_half) {
            x1++;
            index2++;
          }
          // Scan left
          int x2 = x - 1;
          index2 = index - 1;
          while (x2 >= 0 && data[index2] > v_half) {
            x2--;
            index2--;
          }
          if (x1 - x2 < minimumWidth) {
            continue;
          }
          // Scan up
          int y1 = y + 1;
          index2 = index + maxx;
          while (y1 < maxy && data[index2] > v_half) {
            y1++;
            index2 += maxx;
          }
          // Scan down
          int y2 = y - 1;
          index2 = index - 1;
          while (y2 >= 0 && data[index2] > v_half) {
            y2--;
            index2 -= maxx;
          }
          if (y1 - y2 < minimumWidth) {
            continue;
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
    final int heightThreshold = (int) getHeightThreshold();
    final int intBackground = (int) background;

    for (int y = n; y < maxy - n; y++) {
      int index = y * maxx + n;
      FIND_MAXIMUM: for (int x = n; x < maxx - n; x++, index++) {
        final int v = data[index];
        if (v < heightThreshold) {
          continue;
        }

        // Sweep neighbourhood -
        // No check for boundaries as this should be an internal sweep.
        for (int i = 0; i < offset.length; i++) {
          if (maximaFlag[index + offset[i]] || data[index + offset[i]] > v) {
            continue FIND_MAXIMUM;
          }
        }

        // Check the maximum width
        if (minimumWidth > 0) {
          final int v_half = intHalfMaximum(intBackground, v);
          int index2;
          // Scan right
          int x1 = x + 1;
          index2 = index + 1;
          while (x1 < maxx && data[index2] > v_half) {
            x1++;
            index2++;
          }
          // Scan left
          int x2 = x - 1;
          index2 = index - 1;
          while (x2 >= 0 && data[index2] > v_half) {
            x2--;
            index2--;
          }
          if (x1 - x2 < minimumWidth) {
            continue;
          }
          // Scan up
          int y1 = y + 1;
          index2 = index + maxx;
          while (y1 < maxy && data[index2] > v_half) {
            y1++;
            index2 += maxx;
          }
          // Scan down
          int y2 = y - 1;
          index2 = index - 1;
          while (y2 >= 0 && data[index2] > v_half) {
            y2--;
            index2 -= maxx;
          }
          if (y1 - y2 < minimumWidth) {
            continue;
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
    final int heightThreshold = (int) getHeightThreshold();
    final int intBackground = (int) background;

    for (int y = border; y < maxy - border; y++) {
      int index = y * maxx + border;
      FIND_MAXIMUM: for (int x = border; x < maxx - border; x++, index++) {

        final int v = data[index];
        if (v < heightThreshold) {
          continue;
        }

        if (inner) {
          for (int i = 0; i < offset.length; i++) {
            if (maximaFlag[index + offset[i]] || data[index + offset[i]] > v) {
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
        if (minimumWidth > 0) {
          final int v_half = intHalfMaximum(intBackground, v);
          int index2;
          // Scan right
          int x1 = x + 1;
          index2 = index + 1;
          while (x1 < maxx && data[index2] > v_half) {
            x1++;
            index2++;
          }
          // Scan left
          int x2 = x - 1;
          index2 = index - 1;
          while (x2 >= 0 && data[index2] > v_half) {
            x2--;
            index2--;
          }
          if (x1 - x2 < minimumWidth) {
            continue;
          }
          // Scan up
          int y1 = y + 1;
          index2 = index + maxx;
          while (y1 < maxy && data[index2] > v_half) {
            y1++;
            index2 += maxx;
          }
          // Scan down
          int y2 = y - 1;
          index2 = index - 1;
          while (y2 >= 0 && data[index2] > v_half) {
            y2--;
            index2 -= maxx;
          }
          if (y1 - y2 < minimumWidth) {
            continue;
          }
        }

        results.add(index);
        maximaFlag[index] = true;
      } // end FIND_MAXIMA
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
    final int[] blockMaxima = findBlockMaximaNxN(data, maxx, maxy, n);
    int maximaCount = 0;
    final int heightThreshold = (int) getHeightThreshold();
    final int intBackground = (int) background;

    boolean[] maximaFlag = null;
    if (isNeighbourCheck()) {
      maximaFlag = getFlagBuffer(data.length);
    }

    FIND_MAXIMUM: for (final int index : blockMaxima) {

      final int v = data[index];

      if (v < heightThreshold) {
        continue;
      }

      final int x = index % maxx;
      final int y = index / maxx;

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
      final int mi = x;
      final int mj = y;
      final int i = (n + 1) * (mi / (n + 1));
      final int j = (n + 1) * (mj / (n + 1));
      final int i_plus_n = FastMath.min(i + n + 1, maxx - 1);
      final int j_plus_n = FastMath.min(j + n + 1, maxy - 1);
      final int mi_minus_n = FastMath.max(mi - n, 0);
      final int mi_plus_n = FastMath.min(mi + n, maxx - 1);
      final int mj_minus_n = FastMath.max(mj - n, 0);
      final int mj_plus_n = FastMath.min(mj + n, maxy - 1);

      // A
      for (int jj = mj_minus_n; jj < j; jj++) {
        int indexStart = jj * maxx + mi_minus_n;
        final int indexEnd = jj * maxx + mi_plus_n;
        for (; indexStart <= indexEnd; indexStart++) {
          if (v < data[indexStart]) {
            continue FIND_MAXIMUM;
          }
        }
      }
      for (int jj = j; jj < j_plus_n; jj++) {
        // B
        {
          int indexStart = jj * maxx + mi_minus_n;
          final int indexEnd = jj * maxx + i;
          for (; indexStart < indexEnd; indexStart++) {
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
            if (v < data[indexStart]) {
              continue FIND_MAXIMUM;
            }
          }
        }
      }
      // D
      for (int jj = j_plus_n; jj <= mj_plus_n; jj++) {
        int indexStart = jj * maxx + mi_minus_n;
        final int indexEnd = jj * maxx + mi_plus_n;
        for (; indexStart <= indexEnd; indexStart++) {
          if (v < data[indexStart]) {
            continue FIND_MAXIMUM;
          }
        }
      }

      if (maximaFlag != null) {
        // A
        for (int jj = mj_minus_n; jj < j; jj++) {
          int indexStart = jj * maxx + mi_minus_n;
          final int indexEnd = jj * maxx + mi_plus_n;
          for (; indexStart <= indexEnd; indexStart++) {
            if (maximaFlag[indexStart]) {
              continue FIND_MAXIMUM;
            }
          }
        }
        for (int jj = j; jj < j_plus_n; jj++) {
          // B
          {
            int indexStart = jj * maxx + mi_minus_n;
            final int indexEnd = jj * maxx + i;
            for (; indexStart < indexEnd; indexStart++) {
              if (maximaFlag[indexStart]) {
                continue FIND_MAXIMUM;
              }
            }
          }

          // C
          {
            int indexStart = jj * maxx + i_plus_n;
            final int indexEnd = jj * maxx + mi_plus_n;
            for (; indexStart <= indexEnd; indexStart++) {
              if (maximaFlag[indexStart]) {
                continue FIND_MAXIMUM;
              }
            }
          }
        }
        // D
        for (int jj = j_plus_n; jj <= mj_plus_n; jj++) {
          int indexStart = jj * maxx + mi_minus_n;
          final int indexEnd = jj * maxx + mi_plus_n;
          for (; indexStart <= indexEnd; indexStart++) {
            if (maximaFlag[indexStart]) {
              continue FIND_MAXIMUM;
            }
          }
        }
      }

      // Check the maximum width
      if (minimumWidth > 0) {
        final int v_half = intHalfMaximum(intBackground, v);
        int index2;
        // Scan right
        int x1 = x + 1;
        index2 = index + 1;
        while (x1 < maxx && data[index2] > v_half) {
          x1++;
          index2++;
        }
        // Scan left
        int x2 = x - 1;
        index2 = index - 1;
        while (x2 >= 0 && data[index2] > v_half) {
          x2--;
          index2--;
        }
        if (x1 - x2 < minimumWidth) {
          continue;
        }
        // Scan up
        int y1 = y + 1;
        index2 = index + maxx;
        while (y1 < maxy && data[index2] > v_half) {
          y1++;
          index2 += maxx;
        }
        // Scan down
        int y2 = y - 1;
        index2 = index - 1;
        while (y2 >= 0 && data[index2] > v_half) {
          y2--;
          index2 -= maxx;
        }
        if (y1 - y2 < minimumWidth) {
          continue;
        }
      }

      // Re-use storage space
      blockMaxima[maximaCount++] = index;
      if (maximaFlag != null) {
        maximaFlag[index] = true;
      }
    } // end FIND_MAXIMA

    return truncate(blockMaxima, maximaCount);
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
    final int[] blockMaxima = findBlockMaximaNxNInternal(data, maxx, maxy, n, border);
    int maximaCount = 0;
    final int heightThreshold = (int) getHeightThreshold();
    final int intBackground = (int) background;

    boolean[] maximaFlag = null;
    if (isNeighbourCheck()) {
      maximaFlag = getFlagBuffer(data.length);
    }

    FIND_MAXIMUM: for (final int index : blockMaxima) {
      final int v = data[index];

      if (v < heightThreshold) {
        continue;
      }

      final int x = index % maxx;
      final int y = index / maxx;

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
      // No check for over-running boundaries since this is the internal version
      final int mi = x;
      final int mj = y;
      final int i = (n + 1) * ((mi - border) / (n + 1)) + border; // Blocks n+1 wide
      final int j = (n + 1) * ((mj - border) / (n + 1)) + border; // Blocks n+1 wide
      // The block boundaries will have been truncated on the final block. Ensure this is swept
      final int i_plus_n = FastMath.min(i + n + 1, maxx - border - 1);
      final int j_plus_n = FastMath.min(j + n + 1, maxy - border - 1);
      final int mi_minus_n = FastMath.max(mi - n, 0);
      final int mi_plus_n = FastMath.min(mi + n, maxx - 1);
      final int mj_minus_n = FastMath.max(mj - n, 0);
      final int mj_plus_n = FastMath.min(mj + n, maxy - 1);

      // A
      for (int jj = mj_minus_n; jj < j; jj++) {
        int indexStart = jj * maxx + mi_minus_n;
        final int indexEnd = jj * maxx + mi_plus_n;
        for (; indexStart <= indexEnd; indexStart++) {
          if (v < data[indexStart]) {
            continue FIND_MAXIMUM;
          }
        }
      }
      for (int jj = j; jj < j_plus_n; jj++) {
        // B
        {
          int indexStart = jj * maxx + mi_minus_n;
          final int indexEnd = jj * maxx + i;
          for (; indexStart < indexEnd; indexStart++) {
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
            if (v < data[indexStart]) {
              continue FIND_MAXIMUM;
            }
          }
        }
      }
      // D
      for (int jj = j_plus_n; jj <= mj_plus_n; jj++) {
        int indexStart = jj * maxx + mi_minus_n;
        final int indexEnd = jj * maxx + mi_plus_n;
        for (; indexStart <= indexEnd; indexStart++) {
          if (v < data[indexStart]) {
            continue FIND_MAXIMUM;
          }
        }
      }

      if (maximaFlag != null) {
        // A
        for (int jj = mj_minus_n; jj < j; jj++) {
          int indexStart = jj * maxx + mi_minus_n;
          final int indexEnd = jj * maxx + mi_plus_n;
          for (; indexStart <= indexEnd; indexStart++) {
            if (maximaFlag[indexStart]) {
              continue FIND_MAXIMUM;
            }
          }
        }
        for (int jj = j; jj < j_plus_n; jj++) {
          // B
          {
            int indexStart = jj * maxx + mi_minus_n;
            final int indexEnd = jj * maxx + i;
            for (; indexStart < indexEnd; indexStart++) {
              if (maximaFlag[indexStart]) {
                continue FIND_MAXIMUM;
              }
            }
          }

          // C
          {
            int indexStart = jj * maxx + i_plus_n;
            final int indexEnd = jj * maxx + mi_plus_n;
            for (; indexStart <= indexEnd; indexStart++) {
              if (maximaFlag[indexStart]) {
                continue FIND_MAXIMUM;
              }
            }
          }
        }
        // D
        for (int jj = j_plus_n; jj <= mj_plus_n; jj++) {
          int indexStart = jj * maxx + mi_minus_n;
          final int indexEnd = jj * maxx + mi_plus_n;
          for (; indexStart <= indexEnd; indexStart++) {
            if (maximaFlag[indexStart]) {
              continue FIND_MAXIMUM;
            }
          }
        }
      }

      // Check the maximum width
      if (minimumWidth > 0) {
        final int v_half = intHalfMaximum(intBackground, v);
        int index2;
        // Scan right
        int x1 = x + 1;
        index2 = index + 1;
        while (x1 < maxx && data[index2] > v_half) {
          x1++;
          index2++;
        }
        // Scan left
        int x2 = x - 1;
        index2 = index - 1;
        while (x2 >= 0 && data[index2] > v_half) {
          x2--;
          index2--;
        }
        if (x1 - x2 < minimumWidth) {
          continue;
        }
        // Scan up
        int y1 = y + 1;
        index2 = index + maxx;
        while (y1 < maxy && data[index2] > v_half) {
          y1++;
          index2 += maxx;
        }
        // Scan down
        int y2 = y - 1;
        index2 = index - 1;
        while (y2 >= 0 && data[index2] > v_half) {
          y2--;
          index2 -= maxx;
        }
        if (y1 - y2 < minimumWidth) {
          continue;
        }
      }

      // Re-use storage space
      blockMaxima[maximaCount++] = index;
      if (maximaFlag != null) {
        maximaFlag[index] = true;
      }
    } // end FIND_MAXIMA

    return truncate(blockMaxima, maximaCount);
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
    data = expand(data, maxx, maxy, newx, newy);

    final int[] maxima = new int[xblocks * yblocks];

    final int heightThreshold = (int) getHeightThreshold();
    final int intBackground = (int) background;
    final boolean validations = (heightThreshold > 0 || minimumWidth > 0 || isNeighbourCheck());

    boolean[] maximaFlag = null;
    if (isNeighbourCheck()) {
      maximaFlag = getFlagBuffer(data.length);
    }

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

    int maximaCount = 0;
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

        if (validations) {
          if (data[maxIndex] < heightThreshold) {
            continue;
          }

          if (maximaFlag != null) {
            for (final int offset : scan) {
              if (maximaFlag[maxIndex + offset]) {
                continue FIND_MAXIMUM;
              }
            }
          }

          // Check the maximum width
          if (minimumWidth > 0) {
            final int x0 = maxIndex % maxx;
            final int y0 = maxIndex / maxx;

            // Get the width at half maximum.
            final int v_half = intHalfMaximum(intBackground, data[maxIndex]);
            int index2;
            // Scan right
            int x1 = x0 + 1;
            index2 = maxIndex + 1;
            while (x1 < maxx && data[index2] > v_half) {
              x1++;
              index2++;
            }
            // Scan left
            int x2 = x0 - 1;
            index2 = maxIndex - 1;
            while (x2 >= 0 && data[index2] > v_half) {
              x2--;
              index2--;
            }
            if (x1 - x2 < minimumWidth) {
              continue;
            }
            // Scan up
            int y1 = y0 + 1;
            index2 = maxIndex + newx;
            while (y1 < maxy && data[index2] > v_half) {
              y1++;
              index2 += newx;
            }
            // Scan down
            int y2 = y0 - 1;
            index2 = maxIndex - newx;
            while (y2 >= 0 && data[index2] > v_half) {
              y2--;
              index2 -= newx;
            }
            if (y1 - y2 < minimumWidth) {
              continue;
            }
          }

          if (maximaFlag != null) {
            maximaFlag[maxIndex] = true;
          }
        }

        // Remap the maxima
        final int xx = maxIndex % newx;
        final int yy = maxIndex / newx;

        maxima[maximaCount++] = (yy - 1) * maxx + xx - 1;
      } // end FIND_MAXIMA
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

    final int[] maxima = new int[xblocks * yblocks];

    final int heightThreshold = (int) getHeightThreshold();
    final int intBackground = (int) background;
    final boolean validations = (heightThreshold > 0 || minimumWidth > 0 || isNeighbourCheck());

    boolean[] maximaFlag = null;
    if (isNeighbourCheck()) {
      maximaFlag = getFlagBuffer(data.length);
    }

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

    int maximaCount = 0;
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

        if (validations) {
          if (data[maxIndex] < heightThreshold) {
            continue;
          }

          if (maximaFlag != null) {
            for (final int offset : scan) {
              if (maximaFlag[maxIndex + offset]) {
                continue FIND_MAXIMUM;
              }
            }
          }

          // Check the maximum width
          if (minimumWidth > 0) {
            final int x0 = maxIndex % maxx;
            final int y0 = maxIndex / maxx;

            // Get the width at half maximum.
            final int v_half = intHalfMaximum(intBackground, data[maxIndex]);
            int index2;
            // Scan right
            int x1 = x0 + 1;
            index2 = maxIndex + 1;
            while (x1 < maxx && data[index2] > v_half) {
              x1++;
              index2++;
            }
            // Scan left
            int x2 = x0 - 1;
            index2 = maxIndex - 1;
            while (x2 >= 0 && data[index2] > v_half) {
              x2--;
              index2--;
            }
            if (x1 - x2 < minimumWidth) {
              continue;
            }
            // Scan up
            int y1 = y0 + 1;
            index2 = maxIndex + maxx;
            while (y1 < maxy && data[index2] > v_half) {
              y1++;
              index2 += maxx;
            }
            // Scan down
            int y2 = y0 - 1;
            index2 = maxIndex - maxx;
            while (y2 >= 0 && data[index2] > v_half) {
              y2--;
              index2 -= maxx;
            }
            if (y1 - y2 < minimumWidth) {
              continue;
            }
          }

          if (maximaFlag != null) {
            maximaFlag[maxIndex] = true;
          }
        }

        maxima[maximaCount++] = maxIndex;
      } // end FIND_MAXIMA
    }

    return truncate(maxima, maximaCount);
  }
}
