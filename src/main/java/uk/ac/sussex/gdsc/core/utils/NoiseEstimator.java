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
 * Copyright (C) 2011 - 2021 Alex Herbert
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

package uk.ac.sussex.gdsc.core.utils;

import java.util.Arrays;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

/**
 * Contains methods to find the noise in the provided image data.
 *
 * <p>Certain noise estimation routines have been copied from the estimation routines of ND-Safir
 * (N-dimensional noise reduction software): <br>
 * http://raweb.inria.fr/rapportsactivite/RA2011/serpico/uid21.html
 */
public class NoiseEstimator {
  /**
   * The noise estimator method.
   */
  public enum Method {
    /**
     * Use all pixels.
     */
    ALL_PIXELS("All pixels"),
    /**
     * Use a range around the lowest pixel in the image.
     */
    LOWEST_PIXELS("Lowest pixels"),
    /**
     * Use the psuedo-residuals and calculate the least median of squares.
     */
    RESIDUALS_LEAST_MEDIAN_OF_SQUARES("Residuals least-median-of-squares"),
    /**
     * Use the psuedo-residuals and calculate the least trimmed of squares.
     */
    RESIDUALS_LEAST_TRIMMED_OF_SQUARES("Residuals least-trimmed-of-squares"),
    /**
     * Use the psuedo-residuals and calculate the least mean of squares.
     */
    RESIDUALS_LEAST_MEAN_OF_SQUARES("Residuals least-mean-of-squares"),
    /**
     * Use the psuedo-residuals ignoring image border and calculate the least median of squares.
     */
    QUICK_RESIDUALS_LEAST_MEDIAN_OF_SQUARES("Quick residuals least-median-of-squares"),
    /**
     * Use the psuedo-residuals ignoring image border and calculate the least trimmed of squares.
     */
    QUICK_RESIDUALS_LEAST_TRIMMED_OF_SQUARES("Quick residuals least-trimmed-of-squares"),
    /**
     * Use the psuedo-residuals ignoring image border and calculate the least mean of squares.
     */
    QUICK_RESIDUALS_LEAST_MEAN_OF_SQUARES("Quick residuals least-mean-of-squares");

    private final String nameString;

    Method(String name) {
      nameString = name;
    }

    @Override
    public String toString() {
      return getName();
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
      return nameString;
    }
  }

  // Package private for access by inner classes

  /** The data. */
  final float[] data;
  /** The residuals. */
  float[] residuals;
  /** The quick residuals. */
  float[] quickResiduals;
  /** The maxx. */
  final int maxx;
  /** The maxy. */
  final int maxy;

  private int range = 6;
  /**
   * Set this to true if multiple calls will be made to {@link #getNoise(Method)} using methods that
   * modify the residuals (LeastMedian or LeastTrimmed). If false these methods destroy the
   * residuals which then have to be recomputed.
   */
  private boolean preserveResiduals;

  /**
   * Instantiates a new noise estimator.
   *
   * @param data the data
   * @param maxx the maxx
   * @param maxy the maxy
   * @throws IllegalArgumentException if {@code data.length < maxx * maxy} or the dimensions are not
   *         strictly positive.
   */
  NoiseEstimator(float[] data, int maxx, int maxy) {
    if (maxx < 1 || maxy < 1) {
      throw new IllegalArgumentException("X/Y dimensions must be larger than 0");
    }
    if (data == null || data.length < maxx * maxy) {
      throw new IllegalArgumentException("Data must be at least as large as the given dimensions");
    }
    this.data = data;
    this.maxx = maxx;
    this.maxy = maxy;
  }

  /**
   * Create a new noise estimator by wrapping the provided data.
   *
   * @param data the data
   * @param maxx the maxx
   * @param maxy the maxy
   * @return the noise estimator
   * @throws IllegalArgumentException if {@code data.length < maxx * maxy} or the dimensions are not
   *         strictly positive.
   */
  public static NoiseEstimator wrap(float[] data, int maxx, int maxy) {
    return new NoiseEstimator(data, maxx, maxy);
  }

  /**
   * Estimates the noise using pixels from the image.
   *
   * @param method the method
   * @return the noise
   */
  public double getNoise(Method method) {
    Estimator ne;

    switch (method) {
      case QUICK_RESIDUALS_LEAST_TRIMMED_OF_SQUARES:
        ne = new ResidualsLeastTrimmedSquareEstimator(true);
        break;

      case QUICK_RESIDUALS_LEAST_MEDIAN_OF_SQUARES:
        ne = new ResidualsLeastMedianSquareEstimator(true);
        break;

      case QUICK_RESIDUALS_LEAST_MEAN_OF_SQUARES:
        ne = new ResidualsLeastMeanSquareEstimator(true);
        break;

      case RESIDUALS_LEAST_TRIMMED_OF_SQUARES:
        ne = new ResidualsLeastTrimmedSquareEstimator(false);
        break;

      case RESIDUALS_LEAST_MEDIAN_OF_SQUARES:
        ne = new ResidualsLeastMedianSquareEstimator(false);
        break;

      case RESIDUALS_LEAST_MEAN_OF_SQUARES:
        ne = new ResidualsLeastMeanSquareEstimator(false);
        break;

      case LOWEST_PIXELS:
        ne = new MinEstimator(range);
        break;

      default:
        ne = new AllEstimator();
        break;
    }

    return ne.getNoise();
  }

  /**
   * Provide the base implementation for all noise estimators.
   */
  private interface Estimator {
    /**
     * Gets the noise.
     *
     * @return the noise
     */
    double getNoise();
  }

  /**
   * Estimate the noise using standard deviation of all pixels in an image.
   */
  private class AllEstimator implements Estimator {
    @Override
    public double getNoise() {
      final SummaryStatistics stats = new SummaryStatistics();
      for (int i = maxx * maxy; i-- > 0;) {
        stats.addValue(data[i]);
      }
      return stats.getStandardDeviation();
    }
  }

  /**
   * Estimate noise using region around lowest pixel in image.
   */
  private class MinEstimator implements Estimator {
    final int range;

    MinEstimator(int range) {
      this.range = range;
    }

    @Override
    public double getNoise() {
      // Get the image minimum
      float min = Float.POSITIVE_INFINITY;
      int index = 0;
      for (int i = maxx * maxy; i-- > 0;) {
        if (min > data[i]) {
          min = data[i];
          index = i;
        }
      }

      final int x = index % maxx;
      final int y = index / maxx;
      final int ys = Math.max(y - range, 0);
      final int ye = Math.min(y + range, maxy - 1);
      final int xs = Math.max(x - range, 0);
      final int xe = Math.min(x + range, maxx - 1);

      final SummaryStatistics stats = new SummaryStatistics();
      for (int y2 = ys; y2 <= ye; y2++) {
        for (int x2 = xs, i = ys * maxx + xs; x2 <= xe; x2++, i++) {
          stats.addValue(data[i]);
        }
      }
      return stats.getStandardDeviation();
    }
  }

  /**
   * Estimate noise using the median of the residuals of the 4N connected neighbours of each pixel.
   */
  private class ResidualsLeastMedianSquareEstimator implements Estimator {
    boolean quick;

    ResidualsLeastMedianSquareEstimator(boolean quick) {
      this.quick = quick;
    }

    @Override
    public double getNoise() {
      float[] buf = (quick) ? getQuickPseudoResiduals() : getPseudoResiduals();
      final int n = buf.length;
      if (n <= 1) {
        // No standard deviation
        return 0;
      }
      if (isPreserveResiduals()) {
        buf = Arrays.copyOf(buf, buf.length);
      }
      final int medianIndex = n / 2;
      Arrays.sort(buf);
      final float median = buf[medianIndex];
      for (int j = 0; j < n; j++) {
        buf[j] = Math.abs(buf[j] - median);
      }
      Arrays.sort(buf);
      final double sig = 1.4828 * buf[medianIndex];
      if (!isPreserveResiduals()) {
        // Residuals have been destroyed
        if (quick) {
          quickResiduals = null;
        } else {
          residuals = null;
        }
      }
      return Math.abs(sig);
    }
  }

  /**
   * Estimate noise using the square of the residuals of the 4N connected neighbours of each pixel.
   *
   * <p>This uses an approximation with the bottom 50% of the data to avoid outliers in the
   * brightest pixels.
   */
  private class ResidualsLeastTrimmedSquareEstimator implements Estimator {
    boolean quick;

    ResidualsLeastTrimmedSquareEstimator(boolean quick) {
      this.quick = quick;
    }

    @Override
    public double getNoise() {
      float[] buf = (quick) ? getQuickPseudoResiduals() : getPseudoResiduals();
      final int n = buf.length;
      if (n <= 1) {
        // No standard deviation
        return 0;
      }
      if (isPreserveResiduals()) {
        buf = Arrays.copyOf(buf, buf.length);
      }
      for (int k = 0; k < n; k++) {
        buf[k] = buf[k] * buf[k];
      }
      Arrays.sort(buf);
      double sum = 0;
      final int medianIndex = n / 2;
      for (int j = 0; j < medianIndex; j++) {
        sum += buf[j];
      }
      final double sig = 2.6477 * Math.sqrt(sum / medianIndex);
      if (!isPreserveResiduals()) {
        // Residuals have been destroyed
        if (quick) {
          quickResiduals = null;
        } else {
          residuals = null;
        }
      }
      return Math.abs(sig);
    }
  }

  /**
   * Estimate noise using the square of the residuals of the 4N connected neighbours of each pixel.
   */
  private class ResidualsLeastMeanSquareEstimator implements Estimator {
    boolean quick;

    ResidualsLeastMeanSquareEstimator(boolean quick) {
      this.quick = quick;
    }

    @Override
    public double getNoise() {
      final float[] buf = (quick) ? getQuickPseudoResiduals() : getPseudoResiduals();
      if (buf.length <= 1) {
        // No standard deviation
        return 0;
      }
      double sum = 0;
      double sumSq = 0;
      for (final float value : buf) {
        sum += value;
        sumSq += value * value;
      }
      sum /= buf.length;
      sumSq /= buf.length;
      sumSq -= sum * sum;
      return (sumSq > 0) ? Math.sqrt(sumSq) : 0;
    }
  }

  /**
   * Get the pseudo-residuals of the input data.
   *
   * @return The pseudo residuals
   * @see #computePseudoResiduals()
   */
  float[] getPseudoResiduals() {
    if (residuals == null) {
      residuals = computePseudoResiduals();
    }
    return residuals;
  }

  /**
   * Compute the pseudo-residuals of the input data.
   *
   * <p>The pseudo residual {@code R(x,y)} of the image {@code I(x,y)} are defined by:
   *
   * <pre>
   * {@code R(x,y) = 4 * I(x,y) - (I(x+1,y) + I(x-1,y) + I(x,y+1) + I(x,y-1))}
   * </pre>
   *
   * <p>and normalized so that {@code
   * E[R(x,y)^2] = E[I(x,y)^2]} with {@code E} the expected value.
   *
   * @return The pseudo residuals
   */
  float[] computePseudoResiduals() {
    // Cannot use mirroring if the size is less than 2
    if (maxx < 2 || maxy < 2) {
      return new float[0];
    }

    final float[] newResiduals = new float[maxx * maxy];

    int index = 0;
    for (int y = 0; y < maxy; y++) {
      for (int x = 0; x < maxx; x++, index++) {
        //@formatter:off
        // The sum of the 4N connected neighbours.
        // Edges are handled using reflection to the opposite side.
        final double sum4n
            = ((x == 0)        ? data[index + 1]    : data[index - 1]   )
            + ((x == maxx - 1) ? data[index - 1]    : data[index + 1]   )
            + ((y == 0)        ? data[index + maxx] : data[index - maxx])
            + ((y == maxy - 1) ? data[index - maxx] : data[index + maxx]);
        //@formatter:on

        // 0.223606798 = 1 / sqrt(20)
        newResiduals[index] = (float) (0.223606798 * (4. * data[index] - sum4n));
      }
    }

    return newResiduals;
  }

  /**
   * Get the pseudo-residuals of the input data.
   *
   * @return The pseudo residuals
   * @see #computeQuickPseudoResiduals()
   */
  float[] getQuickPseudoResiduals() {
    if (quickResiduals == null) {
      quickResiduals = computeQuickPseudoResiduals();
    }
    return quickResiduals;
  }

  /**
   * Compute the pseudo-residuals of the input data.
   *
   * <p>Ignore the image border so output will be {@code size = (maxx - 2) * (maxy - 2)}. If either
   * dimension is less than 3 then the output will be a zero length array.
   *
   * @return The pseudo residuals
   */
  float[] computeQuickPseudoResiduals() {
    if (maxx < 3 || maxy < 3) {
      return new float[0];
    }

    final float[] newQuickResiduals = new float[(maxx - 2) * (maxy - 2)];

    int outputIndex = 0;
    for (int y = 1; y < maxy - 1; y++) {
      for (int x = 1, index = y * maxx + 1; x < maxx - 1; x++, index++, outputIndex++) {
        // The sum of the 4N connected neighbours.
        final double sum4n =
            data[index - 1] + data[index + 1] + data[index - maxx] + data[index + maxx];
        // 0.223606798 = 1 / sqrt(20)
        newQuickResiduals[outputIndex] = (float) (0.223606798 * (4. * data[index] - sum4n));
      }
    }

    return newQuickResiduals;
  }

  /**
   * Sets the range.
   *
   * @param range the range for the search around the local minimum. Must be at least 1.
   */
  public void setRange(int range) {
    this.range = Math.max(1, range);
  }

  /**
   * Gets the range.
   *
   * @return the range
   */
  public int getRange() {
    return range;
  }

  /**
   * Checks if preserving residuals. This allows multiple calls to the {@link #getNoise(Method)}
   * using methods that modify the residuals (LeastMedian or LeastTrimmed).
   *
   * @return true, if is preserve residuals
   */
  public boolean isPreserveResiduals() {
    return preserveResiduals;
  }

  /**
   * Sets the preserve residuals option.
   *
   * <p>Set this to true if multiple calls will be made to {@link #getNoise(Method)} using methods
   * that modify the residuals (LeastMedian or LeastTrimmed). If false these methods destroy the
   * residuals which then have to be recomputed.
   *
   * @param preserveResiduals the new preserve residuals
   */
  public void setPreserveResiduals(boolean preserveResiduals) {
    this.preserveResiduals = preserveResiduals;
  }
}
