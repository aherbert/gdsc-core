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

package uk.ac.sussex.gdsc.core.ij.process;

import ij.ImageStack;
import ij.process.FHT;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import uk.ac.sussex.gdsc.core.utils.MathUtils;

/**
 * Copy implementation of {@link ij.process.FHT} to increase the speed where possible.
 *
 * <p>Speed has been increased for: multiply/divide operations by allowing pre-computation of the
 * state for the operation; and computation of the complex transform in a combined algorithm for
 * the real and imaginary parts.
 *
 * @see FHT
 */
public class Fht extends FloatProcessor {
  private boolean isFrequencyDomain;
  private float[] cosTable;
  private float[] sinTable;
  private int[] bitrev;
  private float[] tempArr;
  // Used for fast multiply operations
  private double[] precomputedH2e;
  private double[] precomputedH2o;
  private double[] precomputedMag;
  private int[] precomputedJj;

  /**
   * Constructs a FHT object from an ImageProcessor. Byte, short and RGB images are converted to
   * float. Float images are duplicated.
   *
   * @param ip the image processor
   * @throws IllegalArgumentException If the processor is not square and a power of 2
   */
  public Fht(ImageProcessor ip) {
    this(ip, false);
  }

  /**
   * Constructs a FHT object from an ImageProcessor. Byte, short and RGB images are converted to
   * float. Float images are duplicated.
   *
   * @param ip the image processor
   * @param isFrequencyDomain True if in the frequency domain
   * @throws IllegalArgumentException If the processor is not square and a power of 2
   */
  public Fht(ImageProcessor ip, boolean isFrequencyDomain) {
    super(ip.getWidth(), ip.getHeight(),
        (float[]) ((ip instanceof FloatProcessor) ? ip.duplicate().getPixels()
            : ip.convertToFloat().getPixels()),
        null);
    if (!powerOf2Size()) {
      throw new IllegalArgumentException(
          "Image not power of 2 size or not square: " + width + "x" + height);
    }
    this.isFrequencyDomain = isFrequencyDomain;
    resetRoi();
  }

  /**
   * Constructs a FHT object.
   *
   * @param pixels the pixels (this is not duplicated)
   * @param maxN the max N
   * @param isFrequencyDomain True if in the frequency domain
   * @throws IllegalArgumentException If the processor is not square and a power of 2
   */
  public Fht(float[] pixels, int maxN, boolean isFrequencyDomain) {
    super(maxN, maxN, pixels);
    if (!powerOf2Size()) {
      throw new IllegalArgumentException(
          "Image not power of 2 size or not square: " + width + "x" + height);
    }
    this.isFrequencyDomain = isFrequencyDomain;
  }

  /**
   * Gets the data.
   *
   * @return the data
   */
  public float[] getData() {
    return (float[]) getPixels();
  }

  /**
   * Returns true of this FHT contains a square image with a width that is a power of two.
   *
   * @return true, if successful
   */
  public final boolean powerOf2Size() {
    return width == height && isPowerOf2(width);
  }

  /**
   * Performs an inverse transform, converting this image into the space domain. The image contained
   * in this FHT must be square and its width must be a power of 2.
   */
  public void inverseTransform() {
    transform(true);
  }

  /**
   * Performs a forward transform, converting this image into the frequency domain. The image
   * contained in this FHT must be square and its width must be a power of 2.
   */
  public void transform() {
    transform(false);
  }

  private void transform(boolean inverse) {
    if (sinTable == null) {
      initializeTables(width);
    }
    final float[] fht = (float[]) getPixels();
    rc2Dfht(fht, inverse, width);
    isFrequencyDomain = !inverse;
    resetFastOperations();
  }

  private void initializeTables(int maxN) {
    // Assume that maxN <= 2^30.
    // The constructor checks the input is a power of size 2 and square. Since this is stored
    // in a single array the size must be less than sqrt(2^31 - 1).
    makeSinCosTables(maxN);
    makeBitReverseTable(maxN);
    tempArr = new float[maxN];
  }

  /**
   * Copy the computation tables from the initialised FHT.
   *
   * @param fht the fht
   */
  public void copyTables(Fht fht) {
    if (fht.sinTable != null && fht.width == width) {
      // No need to clone as the tables are only read
      sinTable = fht.sinTable;
      cosTable = fht.cosTable;
      bitrev = fht.bitrev;
      // Initialise the temp array
      tempArr = new float[width];
    }
  }

  private void makeSinCosTables(int maxN) {
    final int nOver4 = maxN / 4;
    cosTable = new float[nOver4];
    sinTable = new float[nOver4];
    double theta = 0.0;
    final double dTheta = 2.0 * Math.PI / maxN;
    for (int i = 0; i < nOver4; i++) {
      cosTable[i] = (float) Math.cos(theta);
      sinTable[i] = (float) Math.sin(theta);
      theta += dTheta;
    }
  }

  private void makeBitReverseTable(int maxN) {
    bitrev = new int[maxN];
    final int nLog2 = log2(maxN);
    for (int i = 0; i < maxN; i++) {
      bitrev[i] = bitRevX(i, nLog2);
    }
  }

  private static int bitRevX(int x, int bitlen) {
    int temp = 0;
    for (int i = 0; i <= bitlen; i++) {
      if ((x & (1 << i)) != 0) {
        temp |= (1 << (bitlen - i - 1));
      }
    }
    return temp;
  }

  // CHECKSTYLE.OFF: VariableDeclarationUsageDistance
  // CHECKSTYLE.OFF: LocalVariableName

  /** Performs a 2D FHT (Fast Hartley Transform). */
  private void rc2Dfht(float[] x, boolean inverse, int maxN) {
    final float[] tmp = new float[maxN];
    for (int row = 0; row < maxN; row++) {
      dfht3(x, row * maxN, inverse, maxN, tmp);
    }
    transposeR(x, maxN);
    for (int row = 0; row < maxN; row++) {
      dfht3(x, row * maxN, inverse, maxN, tmp);
    }
    transposeR(x, maxN);

    for (int row = 0; row <= maxN / 2; row++) {
      for (int col = 0; col <= maxN / 2; col++) {
        final int modRow = (maxN - row) % maxN;
        final int modCol = (maxN - col) % maxN;
        // see Bracewell, 'Fast 2D Hartley Transf.' IEEE Procs. 9/86
        final float a = x[row * maxN + col];
        final float b = x[modRow * maxN + col];
        final float c = x[row * maxN + modCol];
        final float d = x[modRow * maxN + modCol];
        final float e = ((a + d) - (b + c)) / 2;
        x[row * maxN + col] = a - e;
        x[modRow * maxN + col] = b + e;
        x[row * maxN + modCol] = c + e;
        x[modRow * maxN + modCol] = d - e;
      }
    }
  }

  // CHECKSTYLE.ON: LocalVariableName

  /**
   * Performs an optimized 1D FHT of an array or part of an array.
   *
   * @param x Input array; will be overwritten by the output in the range given by base and maxN.
   * @param base First index from where data of the input array should be read.
   * @param inverse True for inverse transform.
   * @param maxN Length of data that should be transformed; this must be always the same for a given
   *        FHT object. Note that all amplitudes in the output 'x' are multiplied by maxN.
   * @param x2 the working data buffer
   */
  private void dfht3(float[] x, int base, boolean inverse, int maxN, float[] x2) {

    // Extract data
    System.arraycopy(x, base, x2, 0, maxN);

    final int log2N = log2(maxN);
    bitRevRArr(x2, maxN); // bitReverse the input array
    int gpSize = 2; // first & second stages - do radix 4 butterflies once thru
    int numGps = maxN / 4;
    for (int gpNum = 0; gpNum < numGps; gpNum++) {
      final int ad1 = gpNum * 4;
      final int ad2 = ad1 + 1;
      final int ad3 = ad1 + gpSize;
      final int ad4 = ad2 + gpSize;
      final float rt1 = x2[ad1] + x2[ad2]; // a + b
      final float rt2 = x2[ad1] - x2[ad2]; // a - b
      final float rt3 = x2[ad3] + x2[ad4]; // c + d
      final float rt4 = x2[ad3] - x2[ad4]; // c - d
      x2[ad1] = rt1 + rt3; // a + b + (c + d)
      x2[ad2] = rt2 + rt4; // a - b + (c - d)
      x2[ad3] = rt1 - rt3; // a + b - (c + d)
      x2[ad4] = rt2 - rt4; // a - b - (c - d)
    }

    if (log2N > 2) {
      // third + stages computed here
      gpSize = 4;
      int numBfs = 2;
      numGps = numGps / 2;
      for (int stage = 2; stage < log2N; stage++) {
        for (int gpNum = 0; gpNum < numGps; gpNum++) {
          final int ad0 = gpNum * gpSize * 2;
          int ad1 = ad0; // 1st butterfly is different from others - no mults needed
          int ad2 = ad1 + gpSize;
          int ad3 = ad1 + gpSize / 2;
          int ad4 = ad3 + gpSize;
          float rt1 = x2[ad1];
          x2[ad1] = x2[ad1] + x2[ad2];
          x2[ad2] = rt1 - x2[ad2];
          rt1 = x2[ad3];
          x2[ad3] = x2[ad3] + x2[ad4];
          x2[ad4] = rt1 - x2[ad4];
          for (int bfNum = 1; bfNum < numBfs; bfNum++) {
            // subsequent BF's dealt with together
            ad1 = bfNum + ad0;
            ad2 = ad1 + gpSize;
            ad3 = gpSize - bfNum + ad0;
            ad4 = ad3 + gpSize;

            final int csAd = bfNum * numGps;
            rt1 = x2[ad2] * cosTable[csAd] + x2[ad4] * sinTable[csAd];
            final float rt2 = x2[ad4] * cosTable[csAd] - x2[ad2] * sinTable[csAd];

            x2[ad2] = x2[ad1] - rt1;
            x2[ad1] = x2[ad1] + rt1;
            x2[ad4] = x2[ad3] + rt2;
            x2[ad3] = x2[ad3] - rt2;

          } /* end bfNum loop */
        } /* end gpNum loop */
        gpSize *= 2;
        numBfs *= 2;
        numGps = numGps / 2;
      } /* end for all stages */
    } /* end if log2N > 2 */

    if (inverse) {
      for (int i = 0; i < maxN; i++) {
        x2[i] = x2[i] / maxN;
      }
    }

    // Copy back
    System.arraycopy(x2, 0, x, base, maxN);
  }

  /**
   * Transpose R.
   *
   * @param x the x
   * @param maxN the max N
   */
  private static void transposeR(float[] x, int maxN) {
    for (int r = 0; r < maxN; r++) {
      for (int c = r + 1, i = r * maxN + r + 1, ii = (r + 1) * maxN + r; c < maxN;
          c++, i++, ii += maxN) {
        final float rTemp = x[i];
        x[i] = x[ii];
        x[ii] = rTemp;
      }
    }
  }

  private static int log2(int x) {
    int count = 31;
    while (!btst(x, count)) {
      count--;
    }
    return count;
  }

  private static boolean btst(int x, int bit) {
    return ((x & (1 << bit)) != 0);
  }

  private void bitRevRArr(float[] x, int maxN) {
    for (int i = 0; i < maxN; i++) {
      tempArr[i] = x[bitrev[i]];
    }
    for (int i = 0; i < maxN; i++) {
      x[i] = tempArr[i];
    }
  }

  /**
   * Converts this FHT to a complex Fourier transform and returns it as a two slice stack.
   *
   * <p>This has been adapted from the routine {@link #getComplexTransform()} to compute the real
   * and imaginary parts of the transform at the same time.
   *
   * @return the complex transform
   */
  public ImageStack getComplexTransform() {
    if (!isFrequencyDomain) {
      throw new IllegalArgumentException("Frequency domain image required");
    }
    final int maxN = getWidth();
    final float[] fht = getData();
    final float[] re = new float[maxN * maxN];
    final float[] im = new float[maxN * maxN];
    for (int i = 0; i < maxN; i++) {
      fhtBoth(i, maxN, fht, re, im);
    }
    swapQuadrants(new FloatProcessor(maxN, maxN, re, null));
    swapQuadrants(new FloatProcessor(maxN, maxN, im, null));
    final ImageStack stack = new ImageStack(maxN, maxN);
    stack.addSlice("Real", re);
    stack.addSlice("Imaginary", im);
    return stack;
  }

  /**
   * Converts this FHT to a complex Fourier transform and returns it as a two slice stack.
   *
   * <p>This has been adapted from the routine {@link #getComplexTransform()} to compute the real
   * and imaginary parts of the transform at the same time.
   *
   * @return the complex transform real and imaginary processors
   */
  public FloatProcessor[] getComplexTransformProcessors() {
    if (!isFrequencyDomain) {
      throw new IllegalArgumentException("Frequency domain image required");
    }
    final int maxN = getWidth();
    final float[] fht = getData();
    final float[] re = new float[maxN * maxN];
    final float[] im = new float[maxN * maxN];
    for (int i = 0; i < maxN; i++) {
      fhtBoth(i, maxN, fht, re, im);
    }
    final FloatProcessor[] out = new FloatProcessor[] {new FloatProcessor(maxN, maxN, re, null),
        new FloatProcessor(maxN, maxN, im, null)};
    swapQuadrants(out[0]);
    swapQuadrants(out[1]);
    return out;
  }

  /**
   * FFT real & imaginary value of one row from 2D Hartley Transform.
   *
   * @param row the row
   * @param maxN the max N
   * @param fht the fht
   * @param real the real
   * @param imag the imag
   */
  private static void fhtBoth(int row, int maxN, float[] fht, float[] real, float[] imag) {
    final int base = row * maxN;
    final int offs = ((maxN - row) % maxN) * maxN;
    for (int c = 0; c < maxN; c++) {
      final float a = fht[base + c];
      final float b = fht[offs + ((maxN - c) % maxN)];
      real[base + c] = (a + b) * 0.5f;
      imag[base + c] = (-a + b) * 0.5f;
    }
  }

  /**
   * Swap quadrants 1 and 3 and 2 and 4 of image so the power spectrum origin is at the centre of
   * the image.
   *
   * <pre>
   *   2 1
   *   3 4
   * </pre>
   */
  public void swapQuadrants() {
    swapQuadrants(this);
    resetFastOperations();
  }

  /**
   * Swap quadrants 1 and 3 and 2 and 4 of the specified ImageProcessor so the power spectrum origin
   * is at the centre of the image.
   *
   * <pre>
   *   2 1
   *   3 4
   * </pre>
   *
   * @param ip The processor (must be an even rectangle, i.e. width and height are even)
   * @throws IllegalArgumentException If not even dimensions
   */
  public static void swapQuadrants(FloatProcessor ip) {
    // This is a specialised version to allow using a float buffer and
    // optimised for even sized images

    final int ny = ip.getHeight();
    final int nx = ip.getWidth();
    // Bitwise OR to combine bits and then check for odd
    if (((nx | ny) & 1) == 1) {
      throw new IllegalArgumentException("Require even dimensions");
    }

    final int nyOver2 = ny / 2;
    final int nxOver2 = nx / 2;

    final float[] tmp = new float[nx];
    final float[] a = (float[]) ip.getPixels();

    //@formatter:off
    // We swap: 0 <=> nx_2, 0 <=> ny_2
    // 1 <=> 3
    Fht.swap(a, a, nx, nxOver2,    0,       0, nyOver2, nxOver2, nyOver2, tmp);
    // 2 <=> 4
    Fht.swap(a, a, nx,       0,    0, nxOver2, nyOver2, nxOver2, nyOver2, tmp);
    //@formatter:on
  }

  /**
   * Swap the rectangle pixel values from pixelsA with pixelsB.
   *
   * <p>No bounds checks are performed so use with care!
   *
   * @param pixelsA the pixelsA pixels
   * @param pixelsB the pixelsB pixels (must match pixelsA.length)
   * @param width the width of each set of pixels
   * @param ax the x origin from pixelsA
   * @param ay the y origin from pixelsA
   * @param bx the x origin from pixelsB
   * @param by the y origin from pixelsB
   * @param rectangleWidth the width of the rectangle to swap
   * @param rectangleHeight the height of the rectangle to swap
   * @param tmp the tmp buffer (must be at least width in length)
   */
  public static void swap(float[] pixelsA, float[] pixelsB, int width, int ax, int ay, int bx,
      int by, int rectangleWidth, int rectangleHeight, float[] tmp) {
    for (int ayy = ay + rectangleHeight, byy = by + rectangleHeight - 1; ayy-- > ay; byy--) {
      final int ai = ayy * width + ax;
      final int bi = byy * width + bx;
      System.arraycopy(pixelsA, ai, tmp, 0, rectangleWidth);
      System.arraycopy(pixelsB, bi, pixelsA, ai, rectangleWidth);
      System.arraycopy(tmp, 0, pixelsB, bi, rectangleWidth);
    }
  }

  /**
   * Initialise fast operations for {@link #multiply(Fht)} and {@link #conjugateMultiply(Fht)}. This
   * pre-computes the values needed for the operations.
   *
   * <p>Note: This initialises the FHT object for use as the argument to the operation, for example
   * if a convolution kernel is to be applied to many FHT objects.
   */
  public void initialiseFastMultiply() {
    if (precomputedH2e == null) {
      // Do this on new arrays for thread safety (i.e. concurrent initialisation)
      final float[] h2 = getData();
      final int maxN = getWidth();
      final double[] h2e = new double[h2.length];
      final double[] h2o = new double[h2e.length];
      final int[] jj = new int[h2e.length];
      for (int r = 0, rowMod = 0, i = 0; r < maxN; r++, rowMod = maxN - r) {
        for (int c = 0, colMod = 0; c < maxN; c++, colMod = maxN - c, i++) {
          final int j = rowMod * maxN + colMod;
          h2e[i] = (h2[i] + h2[j]) / 2;
          h2o[i] = (h2[i] - h2[j]) / 2;
          jj[i] = j;
        }
      }
      this.precomputedH2o = h2o;
      this.precomputedJj = jj;
      // Assign at the end for thread safety (i.e. concurrent initialisation)
      this.precomputedH2e = h2e;
    }
  }

  /**
   * Initialise fast operations for {@link #multiply(Fht)}, {@link #conjugateMultiply(Fht)} and
   * {@link #divide(Fht)}. This pre-computes the values needed for the operations.
   *
   * <p>Note: This initialises the FHT object for use as the argument to the operation, for example
   * if a deconvolution kernel is to be applied to many FHT objects.
   */
  public void initialiseFastOperations() {
    initialiseFastMultiply();
    if (precomputedMag == null) {
      // Do this on new arrays for thread safety (i.e. concurrent initialisation)
      final double[] mag = new double[precomputedH2e.length];
      final int[] jj = precomputedJj;
      final float[] h2 = getData();
      for (int i = 0; i < h2.length; i++) {
        // Note that pre-computed h2e and h2o are divided by 2 so we also
        // divide the magnitude by 2 to allow reuse of the pre-computed values
        // in the divide operation (which does not require h2e/2 and h2o/2)
        mag[i] = Math.max(1e-20, h2[i] * h2[i] + h2[jj[i]] * h2[jj[i]]) / 2;
      }
      this.precomputedMag = mag;
    }
  }

  /**
   * Checks if is initialised for fast multiply.
   *
   * @return true, if is fast multiply
   */
  public boolean isFastMultiply() {
    return precomputedH2e != null;
  }

  /**
   * Checks if is initialised for fast operations.
   *
   * @return true, if is fast operations
   */
  public boolean isFastOperations() {
    return precomputedMag != null;
  }

  private void resetFastOperations() {
    precomputedH2e = null;
    precomputedH2o = null;
    precomputedJj = null;
    precomputedMag = null;
  }

  private Fht createFhtResult(float[] tmp, final int maxN) {
    final Fht result = new Fht(tmp, maxN, true);
    // For faster inverse transform copy the tables
    result.copyTables(this);
    return result;
  }

  /**
   * Returns the image resulting from the point by point Hartley multiplication of this image and
   * the specified image. Both images are assumed to be in the frequency domain. Multiplication in
   * the frequency domain is equivalent to convolution in the space domain.
   *
   * @param fht the fht
   * @return the fht2
   */
  public Fht multiply(Fht fht) {
    return multiply(fht, null);
  }

  /**
   * Returns the image resulting from the point by point Hartley multiplication of this image and
   * the specified image. Both images are assumed to be in the frequency domain. Multiplication in
   * the frequency domain is equivalent to convolution in the space domain.
   *
   * @param fht the fht
   * @param tmp the buffer for the result (can be null)
   * @return the fht2
   */
  public Fht multiply(Fht fht, float[] tmp) {
    return (fht.isFastMultiply())
        ? multiply(fht.precomputedH2e, fht.precomputedH2o, fht.precomputedJj, tmp)
        : multiply(fht.getData(), tmp);
  }

  /**
   * Returns the image resulting from the point by point Hartley multiplication of this image and
   * the specified image. Both images are assumed to be in the frequency domain. Multiplication in
   * the frequency domain is equivalent to convolution in the space domain.
   *
   * @param h2 the second FHT
   * @param tmp the buffer for the result (can be null)
   * @return the fht2
   */
  private Fht multiply(float[] h2, float[] tmp) {
    final float[] h1 = getData();
    final int maxN = getWidth();
    final float[] buffer = getBuffer(tmp, h1.length);
    for (int r = 0, rowMod = 0, i = 0; r < maxN; r++, rowMod = maxN - r) {
      // rowMod = (maxN - r) % maxN
      for (int c = 0, colMod = 0; c < maxN; c++, colMod = maxN - c, i++) {
        // colMod = (maxN - c) % maxN
        // h2e = (h2[r * maxN + c] + h2[rowMod * maxN + colMod]) / 2
        // h2o = (h2[r * maxN + c] - h2[rowMod * maxN + colMod]) / 2
        // tmp[r * maxN + c] = (float) (h1[r * maxN + c] * h2e + h1[rowMod * maxN + colMod] * h2o)

        // This is actually doing for 2D data stored as x[rows][columns]
        // x==column, y==row (this is row-major order as per JTransforms notation)
        // https://en.wikipedia.org/wiki/Discrete_Hartley_transform
        // h2e = (h2[r][c] + h2[N-r][N-c]) / 2
        // h2o = (h2[r][c] - h2[N-r][N-c]) / 2
        // tmp[r][c] = (float) (h1[r][c] * h2e + h1[N-r][N-c] * h2o)

        final int j = rowMod * maxN + colMod;
        final double h2e = (h2[i] + h2[j]) / 2;
        final double h2o = (h2[i] - h2[j]) / 2;
        buffer[i] = (float) (h1[i] * h2e + h1[j] * h2o);
      }
    }
    return createFhtResult(buffer, maxN);
  }

  /**
   * Returns the image resulting from the point by point Hartley multiplication of this image and
   * the specified image. Both images are assumed to be in the frequency domain. Multiplication in
   * the frequency domain is equivalent to convolution in the space domain.
   *
   * @param h2e the pre-initialised h2e value
   * @param h2o the pre-initialised h2o value
   * @param jj the pre-initialised j index
   * @param tmp the buffer for the result (can be null)
   * @return the fht2
   */
  private Fht multiply(double[] h2e, double[] h2o, int[] jj, float[] tmp) {
    final float[] h1 = getData();
    final float[] buffer = getBuffer(tmp, h1.length);
    for (int i = 0; i < h1.length; i++) {
      buffer[i] = (float) (h1[i] * h2e[i] + h1[jj[i]] * h2o[i]);
    }
    return createFhtResult(buffer, width);
  }

  /**
   * Returns the image resulting from the point by point Hartley conjugate multiplication of this
   * image and the specified image. Both images are assumed to be in the frequency domain. Conjugate
   * multiplication in the frequency domain is equivalent to correlation in the space domain.
   *
   * @param fht the fht
   * @return the fht2
   */
  public Fht conjugateMultiply(Fht fht) {
    return conjugateMultiply(fht, null);
  }

  /**
   * Returns the image resulting from the point by point Hartley conjugate multiplication of this
   * image and the specified image. Both images are assumed to be in the frequency domain. Conjugate
   * multiplication in the frequency domain is equivalent to correlation in the space domain.
   *
   * @param fht the fht
   * @param tmp the buffer for the result (can be null)
   * @return the fht2
   */
  public Fht conjugateMultiply(Fht fht, float[] tmp) {
    return (fht.isFastMultiply())
        ? conjugateMultiply(fht.precomputedH2e, fht.precomputedH2o, fht.precomputedJj, tmp)
        : conjugateMultiply(fht.getData(), tmp);
  }

  /**
   * Returns the image resulting from the point by point Hartley conjugate multiplication of this
   * image and the specified image. Both images are assumed to be in the frequency domain. Conjugate
   * multiplication in the frequency domain is equivalent to correlation in the space domain.
   *
   * @param h2 the second FHT
   * @param tmp the buffer for the result (can be null)
   * @return the fht2
   */
  private Fht conjugateMultiply(float[] h2, float[] tmp) {
    final float[] h1 = getData();
    final int maxN = getWidth();
    final float[] buffer = getBuffer(tmp, h1.length);
    for (int r = 0, rowMod = 0, i = 0; r < maxN; r++, rowMod = maxN - r) {
      // rowMod = (maxN - r) % maxN
      for (int c = 0, colMod = 0; c < maxN; c++, colMod = maxN - c, i++) {
        // colMod = (maxN - c) % maxN
        // h2e = (h2[r * maxN + c] + h2[rowMod * maxN + colMod]) / 2
        // h2o = (h2[r * maxN + c] - h2[rowMod * maxN + colMod]) / 2
        // buffer[r * maxN + c] = (float) (h1[r * maxN + c] * h2e - h1[rowMod * maxN + colMod] *
        // h2o)
        final int j = rowMod * maxN + colMod;
        final double h2e = (h2[i] + h2[j]) / 2;
        final double h2o = (h2[i] - h2[j]) / 2;
        buffer[i] = (float) (h1[i] * h2e - h1[j] * h2o);
      }
    }
    return createFhtResult(buffer, maxN);
  }

  /**
   * Returns the image resulting from the point by point Hartley conjugate multiplication of this
   * image and the specified image. Both images are assumed to be in the frequency domain. Conjugate
   * multiplication in the frequency domain is equivalent to correlation in the space domain.
   *
   * @param h2e the pre-initialised h2e value
   * @param h2o the pre-initialised h2o value
   * @param jj the pre-initialised j index
   * @param tmp the buffer for the result (can be null)
   * @return the fht2
   */
  private Fht conjugateMultiply(double[] h2e, double[] h2o, int[] jj, float[] tmp) {
    final float[] h1 = getData();
    final float[] buffer = getBuffer(tmp, h1.length);
    for (int i = 0; i < h1.length; i++) {
      buffer[i] = (float) (h1[i] * h2e[i] - h1[jj[i]] * h2o[i]);
    }
    return createFhtResult(buffer, width);
  }

  /**
   * Returns the image resulting from the point by point Hartley division of this image by the
   * specified image. Both images are assumed to be in the frequency domain. Division in the
   * frequency domain is equivalent to deconvolution in the space domain.
   *
   * @param fht the fht
   * @return the fht2
   */
  public Fht divide(Fht fht) {
    return divide(fht, null);
  }

  /**
   * Returns the image resulting from the point by point Hartley division of this image by the
   * specified image. Both images are assumed to be in the frequency domain. Division in the
   * frequency domain is equivalent to deconvolution in the space domain.
   *
   * @param fht the fht
   * @param tmp the buffer for the result (can be null)
   * @return the fht2
   */
  public Fht divide(Fht fht, float[] tmp) {
    return (fht.isFastOperations())
        ? divide(fht.precomputedH2e, fht.precomputedH2o, fht.precomputedJj, fht.precomputedMag, tmp)
        : divide(fht.getData(), tmp);
  }

  /**
   * Returns the image resulting from the point by point Hartley division of this image by the
   * specified image. Both images are assumed to be in the frequency domain. Division in the
   * frequency domain is equivalent to deconvolution in the space domain.
   *
   * @param h2 the second FHT
   * @param tmp the buffer for the result (can be null)
   */
  private Fht divide(float[] h2, float[] tmp) {
    final float[] h1 = getData();
    final int maxN = getWidth();
    final float[] buffer = getBuffer(tmp, h1.length);
    for (int r = 0, rowMod = 0, i = 0; r < maxN; r++, rowMod = maxN - r) {
      // rowMod = (maxN - r) % maxN
      for (int c = 0, colMod = 0; c < maxN; c++, colMod = maxN - c, i++) {
        final int j = rowMod * maxN + colMod;
        final double h2i = h2[i];
        final double h2j = h2[j];
        double magnitude = h2i * h2i + h2j * h2j;
        if (magnitude < 1e-20) {
          magnitude = 1e-20;
        }
        final double h2e = (h2i + h2j);
        final double h2o = (h2i - h2j);
        buffer[i] = (float) ((h1[i] * h2e - h1[j] * h2o) / magnitude);
      }
    }
    return createFhtResult(buffer, maxN);
  }

  /**
   * Returns the image resulting from the point by point Hartley division of this image by the
   * specified image. Both images are assumed to be in the frequency domain. Division in the
   * frequency domain is equivalent to deconvolution in the space domain.
   *
   * @param h2e the pre-initialised h2e value
   * @param h2o the pre-initialised h2o value
   * @param jj the pre-initialised j index
   * @param mag the pre-initialised magnitude value
   * @param tmp the buffer for the result (can be null)
   */
  private Fht divide(double[] h2e, double[] h2o, int[] jj, double[] mag, float[] tmp) {
    final float[] h1 = getData();
    final float[] buffer = getBuffer(tmp, h1.length);
    for (int i = 0; i < h1.length; i++) {
      buffer[i] = (float) ((h1[i] * h2e[i] - h1[jj[i]] * h2o[i]) / mag[i]);
    }
    return createFhtResult(buffer, width);
  }

  /**
   * Returns a clone of this FHT.
   *
   * @return the copy
   */
  public Fht getCopy() {
    final Fht fht = new Fht(getData().clone(), width, isFrequencyDomain);
    fht.copyTables(this);
    return fht;
  }

  /**
   * Gets a buffer of the specified length reusing the given buffer if possible.
   *
   * @param buffer the input buffer
   * @param length the length
   * @return the buffer
   */
  private static float[] getBuffer(float[] buffer, int length) {
    if (buffer == null || buffer.length != length) {
      return new float[length];
    }
    return buffer;
  }

  /**
   * Checks if is power of 2 above zero (i.e. not 2^0 == 1).
   *
   * @param n the n
   * @return true, if is power of 2 above zero
   */
  public static boolean isPowerOf2(int n) {
    // Avoid 2^0 returning true
    return n > 1 && MathUtils.isPow2(n);
  }

  /** Returns a string containing information about this FHT. */
  @Override
  public String toString() {
    return "Fht, " + getWidth() + "x" + getHeight() + ", fd=" + isFrequencyDomain;
  }
}
