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

import java.util.Objects;

/**
 * Apply a window function to reduce edge artifacts.
 */
public class ImageWindow {

  /**
   * The method used for the window function.
   */
  public enum WindowMethod {
    /** No window function. */
    NONE("None"),
    /** The Hanning window function. */
    HANNING("Hanning"),
    /** The cosine window function. */
    COSINE("Cosine"),
    /** The Tukey window function. */
    TUKEY("Tukey");

    private final String nameString;

    /**
     * Instantiates a new window method.
     *
     * @param name the name
     */
    private WindowMethod(String name) {
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

  // Allow cached window weights
  private double[] wx;
  private double[] wy;
  private WindowMethod windowFunction;

  /**
   * Apply a window function to reduce edge artifacts.
   *
   * <p>Applied as two 1-dimensional window functions. Faster than the non-separable form but has
   * direction dependent corners.
   *
   * <p>Instance method allows caching the weight matrices.
   *
   * @param image the image
   * @param maxx the maxx
   * @param maxy the maxy
   * @param windowFunction the window function
   * @return the image
   */
  public float[] applySeparable(float[] image, final int maxx, final int maxy,
      WindowMethod windowFunction) {
    Objects.requireNonNull(windowFunction, "Window function must not be null");
    if (windowFunction == WindowMethod.NONE) {
      return image;
    }

    // Get cache and update if necessary
    final WindowMethod localWindowFunction = this.windowFunction;
    double[] localWx = this.wx;
    double[] localWy = this.wy;

    if (localWindowFunction != windowFunction || getLength(localWx) != maxx
        || getLength(localWy) != maxy) {
      // Compute
      switch (windowFunction) {
        case HANNING:
          localWx = hanning(maxx);
          localWy = hanning(maxy);
          break;
        case COSINE:
          localWx = cosine(maxx);
          localWy = cosine(maxy);
          break;
        case TUKEY:
        default:
          localWx = tukey(maxx);
          localWy = tukey(maxy);
          break;
      }

      // Update non-synchronised. It is only a cache.
      this.windowFunction = windowFunction;
      this.wx = localWx;
      this.wy = localWy;
    }

    final float[] data = new float[image.length];

    for (int y = 0, i = 0; y < maxy; y++) {
      for (int x = 0; x < maxx; x++, i++) {
        data[i] = (float) (image[i] * localWx[x] * localWy[y]);
      }
    }

    return data;
  }

  /**
   * Apply a window function to reduce edge artifacts.
   *
   * <p>Applied as two 1-dimensional window functions. Faster than the non-separable form but has
   * direction dependent corners.
   *
   * @param image the image
   * @param maxx the maxx
   * @param maxy the maxy
   * @param windowFunction the window function
   * @return the image
   */
  public static float[] applyWindowSeparable(float[] image, final int maxx, final int maxy,
      WindowMethod windowFunction) {
    double[] wx;
    double[] wy;

    switch (windowFunction) {
      case HANNING:
        wx = hanning(maxx);
        wy = hanning(maxy);
        break;
      case COSINE:
        wx = cosine(maxx);
        wy = cosine(maxy);
        break;
      case TUKEY:
        wx = tukey(maxx);
        wy = tukey(maxy);
        break;
      case NONE:
      default:
        return image;
    }

    final float[] data = new float[image.length];

    for (int y = 0, i = 0; y < maxy; y++) {
      for (int x = 0; x < maxx; x++, i++) {
        data[i] = (float) (image[i] * wx[x] * wy[y]);
      }
    }

    return data;
  }

  /**
   * Apply a window function to reduce edge artifacts.
   *
   * <p>Applied as two 1-dimensional window functions. Faster than the nonseparable form but has
   * direction dependent corners.
   *
   * @param image the image
   * @param maxx the maxx
   * @param maxy the maxy
   * @param wx the window for x
   * @param wy the window for y
   * @return the float[]
   */
  public static float[] applyWindowSeparable(float[] image, final int maxx, final int maxy,
      double[] wx, double[] wy) {
    if (getLength(wx) != maxx || getLength(wy) != maxy) {
      throw new IllegalArgumentException("Window function must match image dimensions");
    }

    final float[] data = new float[image.length];

    for (int y = 0, i = 0; y < maxy; y++) {
      for (int x = 0; x < maxx; x++, i++) {
        data[i] = (float) (image[i] * wx[x] * wy[y]);
      }
    }

    return data;
  }

  /**
   * Apply a window function to reduce edge artifacts.
   *
   * <p>Applied as two 1-dimensional window functions. Faster than the nonseparable form but has
   * direction dependent corners.
   *
   * @param image the image
   * @param maxx the maxx
   * @param maxy the maxy
   * @param wx the window for x
   * @param wy the window for y
   */
  public static void applyWindowSeparableInPlace(float[] image, int maxx, int maxy, double[] wx,
      double[] wy) {
    if (getLength(wx) != maxx || getLength(wy) != maxy) {
      throw new IllegalArgumentException("Window function must match image dimensions");
    }

    for (int y = 0, i = 0; y < maxy; y++) {
      for (int x = 0; x < maxx; x++, i++) {
        image[i] *= wx[x] * wy[y];
      }
    }
  }

  /**
   * Apply a window function to reduce edge artifacts.
   *
   * <p>Applied as a non-separable form.
   *
   * @param image the image
   * @param maxx the maxx
   * @param maxy the maxy
   * @param windowFunction the window function
   * @return the image
   */
  public static float[] applyWindow(float[] image, final int maxx, final int maxy,
      WindowMethod windowFunction) {
    WindowFunction wf;
    switch (windowFunction) {
      case HANNING:
        wf = Hanning.INSTANCE;
        break;
      case COSINE:
        wf = Cosine.INSTANCE;
        break;
      case TUKEY:
        wf = Tukey.INSTANCE;
        break;
      case NONE:
      default:
        return image;
    }

    final float[] data = new float[image.length];

    final double cx = maxx * 0.5;
    final double cy = maxy * 0.5;
    final double maxDistance = Math.sqrt((double) maxx * maxx + (double) maxy * maxy);

    // Pre-compute
    final double[] dx2 = new double[maxx];
    for (int x = 0; x < maxx; x++) {
      dx2[x] = (x - cx) * (x - cx);
    }

    for (int y = 0, i = 0; y < maxy; y++) {
      final double dy2 = (y - cy) * (y - cy);
      for (int x = 0; x < maxx; x++, i++) {
        final double distance = Math.sqrt(dx2[x] + dy2);
        final double w = wf.weight(0.5 - (distance / maxDistance));
        data[i] = (float) (image[i] * w);
      }
    }

    return data;
  }

  /**
   * The Interface WindowFunction.
   */
  public interface WindowFunction {
    /**
     * Return the weight for the window at a fraction of the distance from the edge of the window.
     *
     * @param fractionDistance (range 0-1)
     * @return the weight
     */
    double weight(double fractionDistance);
  }

  /**
   * Implement no window function.
   */
  public static final class NoWindowFunction implements WindowFunction {
    /** The instance. */
    public static final NoWindowFunction INSTANCE = new NoWindowFunction();

    private NoWindowFunction() {}

    @Override
    public double weight(double fractionDistance) {
      return 1.0;
    }
  }

  /**
   * Implement a Hanning window function.
   */
  public static final class Hanning implements WindowFunction {
    /** The instance. */
    public static final Hanning INSTANCE = new Hanning();

    private Hanning() {}

    @Override
    public double weight(double fractionDistance) {
      return 0.5 * (1 - Math.cos(Math.PI * 2 * fractionDistance));
    }
  }

  /**
   * Implement a Cosine window function.
   */
  public static final class Cosine implements WindowFunction {
    /** The instance. */
    public static final Cosine INSTANCE = new Cosine();

    private Cosine() {}

    @Override
    public double weight(double fractionDistance) {
      return Math.sin(Math.PI * fractionDistance);
    }
  }

  /**
   * Implement a Tukey (Tapered Cosine) window function.
   */
  public static final class Tukey implements WindowFunction {

    /** An instance for the Tukey window with the alpha set to 0.5. */
    public static final Tukey INSTANCE = new Tukey(0.5);

    /** The alpha. */
    final double alpha;

    /** The a 1. */
    final double a1;

    /** The a 2. */
    final double a2;

    /**
     * Instantiates a new tukey window function.
     *
     * @param alpha the alpha
     * @throws IllegalArgumentException If the alpha is not in the range {@code [0,1]}
     */
    public Tukey(double alpha) {
      if (alpha < 0 || alpha > 1) {
        throw new IllegalArgumentException("Alpha must be in the range 0-1");
      }
      this.alpha = alpha;
      a1 = alpha / 2;
      a2 = 1 - alpha / 2;
    }

    @Override
    public double weight(double fractionDistance) {
      if (fractionDistance < a1) {
        return 0.5 * (1 + Math.cos(Math.PI * (2 * fractionDistance / alpha - 1)));
      }
      if (fractionDistance > a2) {
        return 0.5 * (1 + Math.cos(Math.PI * (2 * fractionDistance / alpha - 2 / alpha + 1)));
      }
      return 1;
    }
  }

  private static double[] createWindow(WindowFunction wf, int size) {
    // Cannot window below size 3
    if (size < 3) {
      return SimpleArrayUtils.newDoubleArray(size, 1);
    }
    final double nMinus1 = size - 1.0;
    final double[] w = new double[size];
    // Assume symmetry
    final int middle = size / 2;
    for (int i = 0, j = size - 1; i <= middle; i++, j--) {
      w[i] = w[j] = wf.weight(i / nMinus1);
    }
    return w;
  }

  /**
   * Create a window function.
   *
   * @param windowFunction the window function
   * @param size the size of the window
   * @return the window weighting
   */
  public static double[] createWindow(WindowMethod windowFunction, int size) {
    switch (windowFunction) {
      case HANNING:
        return hanning(size);
      case COSINE:
        return cosine(size);
      case TUKEY:
        return tukey(size);
      case NONE:
      default:
        return SimpleArrayUtils.newDoubleArray(size, 1);
    }
  }

  /**
   * Create a Hanning window.
   *
   * @param size the size of the window
   * @return the window weighting
   */
  public static double[] hanning(int size) {
    return createWindow(Hanning.INSTANCE, size);
  }

  /**
   * Create a Cosine window.
   *
   * @param size the size of the window
   * @return the window weighting
   */
  public static double[] cosine(int size) {
    return createWindow(Cosine.INSTANCE, size);
  }

  /**
   * Create a Tukey (Tapered Cosine) window.
   *
   * <p>Alpha controls the distance from the edge of the window to the centre to apply the weight. A
   * value of 1 will return a Hanning window, 0 will return a rectangular window.
   *
   * @param size the size of the window
   * @param alpha the alpha parameter
   * @return the window weighting
   * @throws IllegalArgumentException If alpha is not in the range 0-1
   */
  public static double[] tukey(int size, double alpha) {
    return createWindow(new Tukey(alpha), size);
  }

  /**
   * Create a Tukey (Tapered Cosine) window using the default alpha of 0.5.
   *
   * @param size the size of the window
   * @return the window weighting
   */
  public static double[] tukey(int size) {
    return createWindow(Tukey.INSTANCE, size);
  }

  /**
   * Create a Tukey (Tapered Cosine) window using the specified edge.
   *
   * <p>Edge controls the distance from the edge of the window to the centre to apply the weight. A
   * 0 will return a rectangular window.
   *
   * @param size the size of the window
   * @param edge the size of the window edge
   * @return the window weighting
   */
  public static double[] tukeyEdge(int size, int edge) {
    return tukey(size, tukeyAlpha(size, edge));
  }

  /**
   * Create the alpha for the Tukey window using the desired size of the edge window.
   *
   * @param size the size of the window
   * @param edge the size of the window edge
   * @return the alpha (range 0-1)
   */
  public static double tukeyAlpha(int size, int edge) {
    if (edge <= 0 || size < 1) {
      return 0;
    }
    return Math.min((2.0 * edge) / (size - 1), 1.0);
  }

  /**
   * Gets the length of the data. Returns zero for a null array.
   *
   * @param data the data
   * @return the length
   */
  private static int getLength(double[] data) {
    return data == null ? 0 : data.length;
  }
}
