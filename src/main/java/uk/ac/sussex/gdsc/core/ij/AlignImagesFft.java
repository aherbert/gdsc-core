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

package uk.ac.sussex.gdsc.core.ij;

import uk.ac.sussex.gdsc.core.utils.ImageWindow;
import uk.ac.sussex.gdsc.core.utils.ImageWindow.Cosine;
import uk.ac.sussex.gdsc.core.utils.ImageWindow.Hanning;
import uk.ac.sussex.gdsc.core.utils.ImageWindow.Tukey;
import uk.ac.sussex.gdsc.core.utils.ImageWindow.WindowFunction;
import uk.ac.sussex.gdsc.core.utils.ImageWindow.WindowMethod;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ColorProcessor;
import ij.process.FHT;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.util.Tools;

import org.apache.commons.math3.util.FastMath;

import java.awt.Rectangle;

/**
 * Aligns an image stack to a reference image using XY translation to maximise the correlation.
 * Takes in:
 *
 * <ul> <li>The reference image <li>The image/stack to align. <li>Optional Max/Min values for the X
 * and Y translation <li>Window function to reduce edge artifacts in frequency space </ul>
 *
 * <p>The alignment is calculated using the maximum correlation between the images. The correlation
 * is computed using the frequency domain (note that conjugate multiplication in the frequency
 * domain is equivalent to correlation in the space domain).
 *
 * <p>Output new stack with the best alignment with optional sub-pixel accuracy.
 *
 * <p>By default restricts translation so that at least half of the smaller image width/height is
 * within the larger image (half-max translation). This can be altered by providing a translation
 * bounds. Note that when using normalised correlation all scores are set to zero outside the
 * half-max translation due to potential floating-point summation error during normalisation.
 */
public class AlignImagesFft {

  /**
   * The Enum SubPixelMethod.
   */
  public enum SubPixelMethod {
    /** The none method. */
    NONE("None"),

    /** The cubic method. */
    CUBIC("Cubic");

    private final String nameString;

    private SubPixelMethod(String name) {
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

  /** The last X offset. */
  private double lastXOffset;

  /** The last Y offset. */
  private double lastYOffset;

  /** The do translation. */
  private boolean doTranslation = true;

  /** The normalised ref ip. This is used for debugging the normalisation */
  private FloatProcessor normalisedRefIp;

  /**
   * The ref image bounds. The location where the reference/target was inserted into the normalised
   * FFT image
   */
  private final Rectangle refImageBounds = new Rectangle();

  /** The target image bounds. */
  private final Rectangle targetImageBounds = new Rectangle();

  private ImageProcessor refIp;
  private double[] rollingSum;
  private double[] rollingSumSq;
  private FHT refFht;

  /**
   * Aligns all images in the target stack to the current processor in the reference.
   *
   * <p>If no target is provided then all slices are aligned to the current processor in the
   * reference.
   *
   * @param refImp the ref imp
   * @param targetImp the target imp
   * @param windowMethod the window method
   * @param bounds the bounds
   * @param subPixelMethod the sub pixel method
   * @param interpolationMethod see {@link ij.process.ImageProcessor#getInterpolationMethods() }
   * @param normalised the normalised
   * @param showCorrelationImage the show correlation image
   * @param showNormalisedImage the show normalised image
   * @param clipOutput the clip output
   * @return the image plus
   */
  @SuppressWarnings("null")
  public ImagePlus align(ImagePlus refImp, ImagePlus targetImp, WindowMethod windowMethod,
      Rectangle bounds, SubPixelMethod subPixelMethod, int interpolationMethod, boolean normalised,
      boolean showCorrelationImage, boolean showNormalisedImage, boolean clipOutput) {
    final ImageProcessor referenceIp = refImp.getProcessor();
    if (targetImp == null) {
      targetImp = refImp;
    }

    // Check same size
    if (!isValid(referenceIp, targetImp)) {
      return null;
    }

    // Fourier transforms use the largest power-two dimension that covers both images
    int maxN = FastMath.max(referenceIp.getWidth(), referenceIp.getHeight());
    final int maxM = FastMath.max(targetImp.getWidth(), targetImp.getHeight());
    maxN = FastMath.max(maxN, maxM);

    this.normalisedRefIp = padAndZero(referenceIp, maxN, windowMethod, refImageBounds);
    if (showNormalisedImage) {
      new ImagePlus(refImp.getTitle() + " Normalised Ref", normalisedRefIp).show();
    }
    maxN = normalisedRefIp.getWidth(); // Update with the power-two result

    // Set up the output stack
    final ImageStack outStack = new ImageStack(targetImp.getWidth(), targetImp.getHeight());
    ImageStack correlationStack = null;
    ImageStack normalisedStack = null;
    FloatProcessor fpCorrelation = null;
    FloatProcessor fpNormalised = null;
    if (showCorrelationImage) {
      correlationStack = new ImageStack(maxN, maxN);
      fpCorrelation = new FloatProcessor(maxN, maxN);
    }
    if (showNormalisedImage) {
      normalisedStack = new ImageStack(maxN, maxN);
      fpNormalised = new FloatProcessor(maxN, maxN);
    }

    // Subtract mean to normalise the numerator of the cross-correlation.
    // ---
    // The effectively normalises the numerator of the correlation but does not address the
    // denominator.
    // The denominator should be calculated using rolling sums for each offset position.
    // See: Fast Normalized Cross-Correlation by J. P. Lewis
    // http://scribblethink.org/Work/nvisionInterface/nip.html
    // Following the computation of the correlation each offset (u,v) position should then be
    // divided
    // by the energy of the reference image under the target image. This equates to:
    // Sum(x,y) [ f(x,y) - f_(u,v) ]^2
    // where f_(u,v) is the mean of the region under the target feature
    // ---

    // Calculate rolling sum of squares
    double[] sum = null;
    double[] sumSq = null;
    if (normalised) {
      sum = new double[normalisedRefIp.getPixelCount()];
      sumSq = new double[sum.length];
      calculateRollingSums(normalisedRefIp, sum, sumSq);
    }

    final FHT referenceFht = fht(normalisedRefIp);

    if (bounds == null) {
      bounds = createHalfMaxBounds(refImp.getWidth(), refImp.getHeight(), targetImp.getWidth(),
          targetImp.getHeight());
    }

    // Process each image in the target stack
    final ImageStack stack = targetImp.getStack();
    for (int slice = 1; slice <= stack.getSize(); slice++) {
      final ImageProcessor targetIp = stack.getProcessor(slice);
      outStack.addSlice(null, alignImages(referenceFht, sum, sumSq, targetIp, slice, windowMethod,
          bounds, fpCorrelation, fpNormalised, subPixelMethod, interpolationMethod, clipOutput));
      if (showCorrelationImage) {
        correlationStack.addSlice(null, fpCorrelation.duplicate());
      }
      if (showNormalisedImage) {
        normalisedStack.addSlice(null, fpNormalised.duplicate());
      }
      if (ImageJUtils.isInterrupted()) {
        return null;
      }
    }

    if (showCorrelationImage) {
      new ImagePlus(targetImp.getTitle() + " Correlation", correlationStack).show();
    }
    if (showNormalisedImage) {
      new ImagePlus(targetImp.getTitle() + " Normalised Target", normalisedStack).show();
    }

    return new ImagePlus(targetImp.getTitle() + " Aligned", outStack);
  }

  /**
   * Aligns all images in the target stack to the pre-initialised reference.
   *
   * @param targetImp the target image
   * @param windowMethod the window method
   * @param bounds the bounds
   * @param subPixelMethod the sub pixel method
   * @param interpolationMethod see {@link ij.process.ImageProcessor#getInterpolationMethods() }
   * @param clipOutput Set to true to ensure the output image has the same max as the input. Applies
   *        to bicubic interpolation
   * @return the image plus
   */
  public ImagePlus align(ImagePlus targetImp, WindowMethod windowMethod, Rectangle bounds,
      SubPixelMethod subPixelMethod, int interpolationMethod, boolean clipOutput) {
    if (refFht == null || targetImp == null) {
      return null;
    }

    final int maxN = refFht.getWidth();

    // Check correct size
    if (targetImp.getWidth() > maxN || targetImp.getHeight() > maxN) {
      return null;
    }

    // Set up the output stack
    final ImageStack outStack = new ImageStack(targetImp.getWidth(), targetImp.getHeight());

    if (bounds == null) {
      bounds = createHalfMaxBounds(refIp.getWidth(), refIp.getHeight(), targetImp.getWidth(),
          targetImp.getHeight());
    }

    // Process each image in the target stack
    final ImageStack stack = targetImp.getStack();
    for (int slice = 1; slice <= stack.getSize(); slice++) {
      final ImageProcessor targetIp = stack.getProcessor(slice);
      outStack.addSlice(null, alignImages(refFht, rollingSum, rollingSumSq, targetIp, slice,
          windowMethod, bounds, null, null, subPixelMethod, interpolationMethod, clipOutput));
      if (ImageJUtils.isInterrupted()) {
        return null;
      }
    }

    return new ImagePlus(targetImp.getTitle() + " Aligned", outStack);
  }

  /**
   * Aligns the target image to the pre-initialised reference and return the shift and score for the
   * alignment.
   *
   * @param targetIp the target ip
   * @param windowMethod the window method
   * @param bounds the bounds
   * @param subPixelMethod the sub pixel method
   * @return [ x_shift, y_shift, score ]
   */
  public double[] align(ImageProcessor targetIp, WindowMethod windowMethod, Rectangle bounds,
      SubPixelMethod subPixelMethod) {
    if (refFht == null || targetIp == null) {
      return null;
    }

    final int maxN = refFht.getWidth();

    // Check correct size
    if (targetIp.getWidth() > maxN || targetIp.getHeight() > maxN) {
      return null;
    }

    if (bounds == null) {
      bounds = createHalfMaxBounds(refIp.getWidth(), refIp.getHeight(), targetIp.getWidth(),
          targetIp.getHeight());
    }

    return alignImages(refFht, rollingSum, rollingSumSq, targetIp, windowMethod, bounds,
        subPixelMethod);
  }

  /**
   * Initialises the reference image for batch alignment. All target images should be equal or
   * smaller than the reference.
   *
   * @param refImp the ref imp
   * @param windowMethod the window method
   * @param normalised True if the correlation should be normalised (score of -1 to 1)
   */
  public void initialiseReference(ImagePlus refImp, WindowMethod windowMethod, boolean normalised) {
    if (refImp == null) {
      refIp = null;
      rollingSum = null;
      rollingSumSq = null;
      refFht = null;
      return;
    }

    initialiseReference(refImp.getProcessor(), windowMethod, normalised);
  }

  /**
   * Initialises the reference image for batch alignment. All target images should be equal or
   * smaller than the reference.
   *
   * @param refIp the reference image processor
   * @param windowMethod the window method
   * @param normalised True if the correlation should be normalised (score of -1 to 1)
   */
  public void initialiseReference(ImageProcessor refIp, WindowMethod windowMethod,
      boolean normalised) {
    this.refIp = refIp;
    rollingSum = null;
    rollingSumSq = null;
    refFht = null;

    if (refIp == null || noValue(refIp)) {
      return;
    }

    // Fourier transforms use the largest power-two dimension that covers both images
    final int maxN = FastMath.max(refIp.getWidth(), refIp.getHeight());

    this.normalisedRefIp = padAndZero(refIp, maxN, windowMethod, refImageBounds);

    // Subtract mean to normalise the numerator of the cross-correlation.
    // ---
    // This effectively normalises the numerator of the correlation but does not address the
    // denominator.
    // The denominator should be calculated using rolling sums for each offset position.
    // See: http://www.idiom.com/~zilla/Papers/nvisionInterface/nip.html
    // Following the computation of the correlation each offset (u,v) position should then be
    // divided
    // by the energy of the reference image under the target image. This equates to:
    // Sum(x,y) [ f(x,y) - f_(u,v) ]^2
    // where f_(u,v) is the mean of the region under the target feature
    // ---

    // Calculate rolling sum of squares
    rollingSum = null;
    rollingSumSq = null;
    if (normalised) {
      rollingSum = new double[normalisedRefIp.getPixelCount()];
      rollingSumSq = new double[rollingSum.length];
      calculateRollingSums(normalisedRefIp, rollingSum, rollingSumSq);
    }

    refFht = fht(normalisedRefIp);
  }

  /**
   * Calculate rolling sums.
   *
   * @param ip the image
   * @param sum the sum
   * @param sumSq the sum of squares
   */
  private static void calculateRollingSums(FloatProcessor ip, double[] sum, double[] sumSq) {
    // Compute the rolling sum and sum of squares
    // s(u,v) = f(u,v) + s(u-1,v) + s(u,v-1) - s(u-1,v-1)
    // ss(u,v) = f(u,v) * f(u,v) + ss(u-1,v) + ss(u,v-1) - ss(u-1,v-1)
    // where s(u,v) = ss(u,v) = 0 when either u,v < 0

    final int maxx = ip.getWidth();
    final int maxy = ip.getHeight();
    final float[] originalData = (float[]) ip.getPixels();
    final double[] data = Tools.toDouble(originalData);

    // First row
    double columnSum = 0; // Column sum
    double columnSumSq = 0; // Column sum-squares
    for (int i = 0; i < maxx; i++) {
      columnSum += data[i];
      columnSumSq += data[i] * data[i];
      sum[i] = columnSum;
      sumSq[i] = columnSumSq;
    }

    // Remaining rows:
    // sum = rolling sum of row + sum of row above
    for (int y = 1; y < maxy; y++) {
      int index = y * maxx;
      columnSum = 0;
      columnSumSq = 0;

      // Remaining columns
      for (int x = 0; x < maxx; x++, index++) {
        columnSum += data[index];
        columnSumSq += data[index] * data[index];

        sum[index] = sum[index - maxx] + columnSum;
        sumSq[index] = sumSq[index - maxx] + columnSumSq;
      }
    }
  }

  /**
   * Normalise the correlation matrix using the standard deviation of the region from the reference
   * that is covered by the target.
   *
   * @param subCorrMat the sub corr mat
   * @param rollingSum the rolling sum
   * @param rollingSumSq the rolling sum of squares
   * @param targetIp the target ip
   */
  private void normaliseCorrelation(FloatProcessor subCorrMat, double[] rollingSum,
      double[] rollingSumSq, ImageProcessor targetIp) {
    final int maxx = subCorrMat.getWidth();
    final int maxy = subCorrMat.getHeight();
    final Rectangle imageBounds = new Rectangle(0, 0, maxx, maxy);

    // This assumes the target was overlaid on the reference image and was the same size or smaller.
    // The target image bounds contains the original overlay location.

    // sizeU,sizeV are <= maxx,maxy
    final int sizeU = targetIp.getWidth();
    final int sizeV = targetIp.getHeight();

    // Locate where the target image was inserted when padding
    final int x = targetImageBounds.x; // (maxx - sizeU) / 2
    final int y = targetImageBounds.y; // (maxy - sizeV) / 2

    // Calculate overlap:
    // Half the image minus the insert origin is the same as the remaining distance from
    // the insert origin to the centre. However maxx will be an even power of 2 (sizeU may
    // not be) and the origin is already known. Doing the computation this way thus avoids
    // rounding problems.
    //
    //@formatter:off
    //              maxx/2
    //                |
    // +------------------------------+ maxx
    // | Reference                    |
    // |                              |
    // |     x,y                      |
    // |      +--------------+ sizeU  |
    // |      | Target       |        |
    // |      |              |        |
    // |      |              |        |
    // |      |      +       |        |
    // |      |    Centre    |        |
    //@formatter:on
    final int halfSizeU = maxx / 2 - x; // sizeU / 2
    final int halfSizeV = maxy / 2 - y; // sizeV / 2

    // Normalise within the bounds of the largest image (i.e. only allow translation
    // up to half of the longest edge from the reference or target).
    // The further the translation from the half-max translation the more likely there
    // can be errors in the normalisation score due to floating point summation errors.
    // This is observed mainly at the very last pixel overlap between images.
    // To see this set:
    // union = imageBounds
    // TODO - More analysis to determine under what conditions this occurs.
    final Rectangle union = refImageBounds.union(targetImageBounds);

    // Normalise using the denominator
    final float[] data = (float[]) subCorrMat.getPixels();
    final float[] newData = new float[data.length];
    for (int yyy = union.y; yyy < union.y + union.height; yyy++) {
      for (int xxx = union.x; xxx < union.x + union.width; xxx++) {
        double sum = 0;
        double sumSquares = 0;

        int minU = xxx - halfSizeU - 1;
        final int maxU = FastMath.min(minU + sizeU, maxx - 1);
        int minV = yyy - halfSizeV - 1;
        final int maxV = FastMath.min(minV + sizeV, maxy - 1);

        // Compute sum from rolling sum using:
        // sum(u,v) =
        // + s(u+N-1,v+N-1)
        // - s(u-1,v+N-1)
        // - s(u+N-1,v-1)
        // + s(u-1,v-1)
        // Note:
        // s(u,v) = 0 when either u,v < 0
        // s(u,v) = s(umax,v) when u>umax
        // s(u,v) = s(u,vmax) when v>vmax
        // s(u,v) = s(umax,vmax) when u>umax,v>vmax
        // Likewise for ss

        // + s(u+N-1,v+N-1)
        int index = maxV * maxx + maxU;
        sum += rollingSum[index];
        sumSquares += rollingSumSq[index];

        if (minU >= 0) {
          // - s(u-1,v+N-1)
          index = maxV * maxx + minU;
          sum -= rollingSum[index];
          sumSquares -= rollingSumSq[index];
        }
        if (minV >= 0) {
          // - s(u+N-1,v-1)
          index = minV * maxx + maxU;
          sum -= rollingSum[index];
          sumSquares -= rollingSumSq[index];

          if (minU >= 0) {
            // + s(u-1,v-1)
            index = minV * maxx + minU;
            sum += rollingSum[index];
            sumSquares += rollingSumSq[index];
          }
        }

        // Use bounds to calculate the number of pixels

        final Rectangle regionBounds =
            new Rectangle(xxx - halfSizeU, yyy - halfSizeV, sizeU, sizeV);
        final Rectangle r = imageBounds.intersection(regionBounds);

        final int n = r.width * r.height;

        if (n < 1) {
          continue;
        }

        // Get the sum of squared differences
        final double residuals = sumSquares - sum * sum / n;

        // // Check using the original data
        // double sx = 0;
        // double ssx = 0;
        // int nn = 0;
        // for (int yy = yyy - halfSizeV; yy < yyy - halfSizeV + sizeV; yy++)
        // for (int xx = xxx - halfSizeU; xx < xxx - halfSizeU + sizeU; xx++)
        // {
        // if (xx >= 0 && xx < maxx && yy >= 0 && yy < maxy)
        // {
        // float value = normalisedRefIp.getf(xx, yy);
        // sx += value;
        // ssx += value * value;
        // nn++;
        // }
        // }
        // gdsc.fitting.utils.DoubleEquality eq = new gdsc.fitting.utils.DoubleEquality(8, 1e-16);
        // if (n != nn)
        // {
        // System.out.printf("Wrong @ %d,%d %d <> %d\n", xxx, yyy, n, nn);
        // residuals = ssx - sx * sx / nn;
        // }
        // else if (!eq.almostEqualComplement(sx, sum) || !eq.almostEqualComplement(ssx,
        // sumSquares))
        // {
        // System.out.printf("Wrong @ %d,%d %g <> %g : %g <> %g\n", xxx, yyy, sx, sum, ssx,
        // sumSquares);
        // residuals = ssx - sx * sx / nn;
        // }

        final double normalisation = (residuals > 0) ? Math.sqrt(residuals) : 0;

        if (normalisation > 0) {
          newData[index] = (float) (data[index] / normalisation);
          // Watch out for normalisation errors which cause problems when displaying the image data.
          if (newData[index] < -1.1f) {
            newData[index] = -1.1f;
          }
          if (newData[index] > 1.1f) {
            newData[index] = 1.1f;
          }
        }
      }
    }
    subCorrMat.setPixels(newData);
  }

  /**
   * Creates the half max bounds.
   *
   * @param width1 the width 1
   * @param height1 the height 1
   * @param width2 the width 2
   * @param height2 the height 2
   * @return the rectangle
   */
  public static Rectangle createHalfMaxBounds(int width1, int height1, int width2, int height2) {
    // Restrict translation so that at least half of the smaller image width/height
    // is within the larger image (half-max translation)
    int maxx = FastMath.max(width1, width2);
    int maxy = FastMath.max(height1, height2);
    maxx /= 2;
    maxy /= 2;
    return new Rectangle(-maxx, -maxy, 2 * maxx, 2 * maxy);
  }

  /**
   * Creates the bounds.
   *
   * @param minXShift the min X shift
   * @param maxXShift the max X shift
   * @param minYShift the min Y shift
   * @param maxYShift the max Y shift
   * @return the rectangle
   */
  public static Rectangle createBounds(int minXShift, int maxXShift, int minYShift, int maxYShift) {
    final int w = maxXShift - minXShift;
    final int h = maxYShift - minYShift;
    return new Rectangle(minXShift, minYShift, w, h);
  }

  /**
   * Checks if is valid.
   *
   * @param refIp the ref ip
   * @param targetImp the target imp
   * @return true, if is valid
   */
  private static boolean isValid(ImageProcessor refIp, ImagePlus targetImp) {
    // Check images have values. No correlation is possible without.
    return refIp != null && targetImp != null && !noValue(refIp);
  }

  /**
   * No value.
   *
   * @param ip the image
   * @return true if the image has no pixels with a value
   */
  private static boolean noValue(ImageProcessor ip) {
    for (int i = 0; i < ip.getPixelCount(); i++) {
      if (ip.getf(i) != 0) {
        return false;
      }
    }
    return true;
  }

  /**
   * Align images.
   *
   * @param refFht the ref FHT
   * @param rollingSum the rolling sum
   * @param rollingSumSq the rolling sum of squares
   * @param targetIp the target ip
   * @param slice the slice
   * @param windowMethod the window method
   * @param bounds the bounds
   * @param fpCorrelation the fp correlation
   * @param fpNormalised the fp normalised
   * @param subPixelMethod the sub pixel method
   * @param interpolationMethod the interpolation method
   * @param clipOutput the clip output
   * @return the image processor
   */
  private ImageProcessor alignImages(FHT refFht, double[] rollingSum, double[] rollingSumSq,
      ImageProcessor targetIp, int slice, WindowMethod windowMethod, Rectangle bounds,
      FloatProcessor fpCorrelation, FloatProcessor fpNormalised, SubPixelMethod subPixelMethod,
      int interpolationMethod, boolean clipOutput) {
    lastXOffset = lastYOffset = 0;

    if (noValue(targetIp)) {
      // Zero correlation with empty image
      IJ.log(String.format("Best Slice %d  x 0  y 0 = 0", slice));
      if (fpCorrelation != null) {
        fpCorrelation.setPixels(new float[refFht.getPixelCount()]);
      }
      if (fpNormalised != null) {
        fpNormalised.setPixels(new float[refFht.getPixelCount()]);
      }
      return targetIp.duplicate();
    }

    // Perform correlation analysis in Fourier space (A and B transform to F and G)
    // using the complex conjugate of G multiplied by F:
    // C(u,v) = F(u,v) G*(u,v)

    final int maxN = refFht.getWidth();

    final ImageProcessor paddedTargetIp =
        padAndZero(targetIp, maxN, windowMethod, targetImageBounds);
    final FloatProcessor normalisedTargetIp = normaliseImage(paddedTargetIp);
    final FHT targetFht = fht(normalisedTargetIp);

    final FloatProcessor subCorrMat = correlate(refFht, targetFht);

    final int originX = (maxN / 2);
    final int originY = (maxN / 2);

    // Normalise using the denominator
    if (rollingSum != null) {
      normaliseCorrelation(subCorrMat, rollingSum, rollingSumSq, targetIp);
    }

    // Copy back result images
    if (fpCorrelation != null) {
      fpCorrelation.setPixels(subCorrMat.getPixels());
    }
    if (fpNormalised != null) {
      fpNormalised.setPixels(normalisedTargetIp.getPixels());
    }

    Rectangle intersect = new Rectangle(0, 0, subCorrMat.getWidth(), subCorrMat.getHeight());

    // Restrict the translation
    if (bounds != null) {
      // Restrict bounds to image limits
      intersect = intersect.intersection(
          new Rectangle(originX + bounds.x, originY + bounds.y, bounds.width, bounds.height));
    }

    final int[] centre = findMaximum(subCorrMat, intersect);
    final float scoreMax = subCorrMat.getf(centre[0], centre[1]);
    final double[] subPixelCentre;

    String estimatedScore = "";
    if (subPixelMethod == SubPixelMethod.CUBIC) {
      subPixelCentre = performCubicFit(subCorrMat, centre[0], centre[1]);

      double score =
          subCorrMat.getBicubicInterpolatedPixel(subPixelCentre[0], subPixelCentre[1], subCorrMat);
      if (score < -1) {
        score = -1;
      }
      if (score > 1) {
        score = 1;
      }
      estimatedScore = String.format(" (interpolated score %g)", score);
    } else {
      subPixelCentre = new double[] {centre[0], centre[1]};

      if (IJ.debugMode) {
        // Used for debugging - Check if interpolation rounds to a different integer
        double[] debugCentre = performCubicFit(subCorrMat, centre[0], centre[1]);
        debugCentre[0] = Math.round(debugCentre[0]);
        debugCentre[1] = Math.round(debugCentre[1]);

        if (centre[0] != debugCentre[0] || centre[1] != debugCentre[1]) {
          IJ.log(String.format("Cubic rounded to different integer: %d,%d => %d,%d", centre[0],
              centre[1], (int) debugCentre[0], (int) debugCentre[1]));
        }
      }
    }

    // The correlation image is the size of the reference.
    // Offset from centre of reference
    lastXOffset = subPixelCentre[0] - originX;
    lastYOffset = subPixelCentre[1] - originY;

    IJ.log(String.format("Best Slice %d  x %g  y %g = %g%s", slice, lastXOffset, lastYOffset,
        scoreMax, estimatedScore));

    // Translate the result and crop to the original size
    if (!doTranslation) {
      return targetIp;
    }

    return translate(interpolationMethod, targetIp, lastXOffset, lastYOffset, clipOutput);
  }

  /**
   * Align images.
   *
   * @param refFht the ref FHT
   * @param rollingSum the rolling sum
   * @param rollingSumSq the rolling sum of squares
   * @param targetIp the target ip
   * @param windowMethod the window method
   * @param bounds the bounds
   * @param subPixelMethod the sub pixel method
   * @return the double[]
   */
  private double[] alignImages(FHT refFht, double[] rollingSum, double[] rollingSumSq,
      ImageProcessor targetIp, WindowMethod windowMethod, Rectangle bounds,
      SubPixelMethod subPixelMethod) {
    lastXOffset = lastYOffset = 0;

    if (noValue(targetIp)) {
      // Zero correlation with empty image
      return new double[] {0, 0, 0};
    }

    // Perform correlation analysis in Fourier space (A and B transform to F and G)
    // using the complex conjugate of G multiplied by F:
    // C(u,v) = F(u,v) G*(u,v)

    final int maxN = refFht.getWidth();

    // Allow the input target to be a FHT
    FHT targetFht;
    if (targetIp instanceof FHT && targetIp.getWidth() == maxN) {
      targetFht = (FHT) targetIp;
    } else {
      targetFht = transformTarget(targetIp, windowMethod);
    }
    final FloatProcessor subCorrMat = correlate(refFht, targetFht);

    final int originX = (maxN / 2);
    final int originY = (maxN / 2);

    // Normalise using the denominator
    if (rollingSum != null) {
      normaliseCorrelation(subCorrMat, rollingSum, rollingSumSq, targetIp);
    }

    Rectangle intersect = new Rectangle(0, 0, subCorrMat.getWidth(), subCorrMat.getHeight());

    // Restrict the translation
    if (bounds != null) {
      // Restrict bounds to image limits
      intersect = intersect.intersection(
          new Rectangle(originX + bounds.x, originY + bounds.y, bounds.width, bounds.height));
    }

    final int[] centre = findMaximum(subCorrMat, intersect);
    double scoreMax = subCorrMat.getf(centre[0], centre[1]);
    final double[] subPixelCentre;

    if (subPixelMethod == SubPixelMethod.CUBIC) {
      subPixelCentre = performCubicFit(subCorrMat, centre[0], centre[1]);

      double score =
          subCorrMat.getBicubicInterpolatedPixel(subPixelCentre[0], subPixelCentre[1], subCorrMat);
      if (score < -1) {
        score = -1;
      }
      if (score > 1) {
        score = 1;
      }
      scoreMax = score;
    } else {
      subPixelCentre = new double[] {centre[0], centre[1]};
    }

    // The correlation image is the size of the reference.
    // Offset from centre of reference
    lastXOffset = subPixelCentre[0] - originX;
    lastYOffset = subPixelCentre[1] - originY;

    return new double[] {lastXOffset, lastYOffset, scoreMax};
  }

  /**
   * Transforms a target image processor for alignment with the initialised reference. The FHT can
   * be passed to the
   * {@link #align(ImageProcessor, ImageWindow.WindowMethod, Rectangle, SubPixelMethod)} method
   *
   * <p>If the {@link #initialiseReference(ImageProcessor, ImageWindow.WindowMethod, boolean)}
   * method has not been called this returns null.
   *
   * @param targetIp the target ip
   * @param windowMethod the window method
   * @return The FHT
   */
  public FHT transformTarget(ImageProcessor targetIp, WindowMethod windowMethod) {
    if (refFht == null || targetIp == null) {
      return null;
    }
    final int maxN = refFht.getWidth();
    FHT targetFht;
    final ImageProcessor paddedTargetIp =
        padAndZero(targetIp, maxN, windowMethod, targetImageBounds);
    final FloatProcessor normalisedTargetIp = normaliseImage(paddedTargetIp);
    targetFht = fht(normalisedTargetIp);
    return targetFht;
  }

  /**
   * Convert to unit length, return a float processor.
   *
   * @param ip the image
   * @return the float processor
   */
  public static FloatProcessor normaliseImage(ImageProcessor ip) {
    final float[] pixels = new float[ip.getPixelCount()];

    // Normalise to unit length and subtract mean
    double sum = 0;
    for (int i = 0; i < pixels.length; i++) {
      sum += ip.getf(i) * ip.getf(i);
    }
    if (sum > 0) {
      final double factor = 1.0 / Math.sqrt(sum);
      for (int i = 0; i < pixels.length; i++) {
        pixels[i] = (float) (ip.getf(i) * factor);
      }
    }

    return new FloatProcessor(ip.getWidth(), ip.getHeight(), pixels, null);
  }

  /**
   * Duplicate and translate the image processor.
   *
   * @param interpolationMethod the interpolation method
   * @param ip the image
   * @param xoffset the x offset
   * @param yoffset the y offset
   * @param clipOutput Set to true to ensure the output image has the same max as the input. Applies
   *        to bicubic interpolation
   * @return New translated processor
   */
  public static ImageProcessor translate(int interpolationMethod, ImageProcessor ip, double xoffset,
      double yoffset, boolean clipOutput) {
    final ImageProcessor newIp = ip.duplicate();
    translateProcessor(interpolationMethod, newIp, xoffset, yoffset, clipOutput);
    return newIp;
  }

  /**
   * Translate the image processor in place.
   *
   * @param interpolationMethod the interpolation method
   * @param ip the image
   * @param xoffset the x offset
   * @param yoffset the y offset
   * @param clipOutput Set to true to ensure the output image has the same max as the input. Applies
   *        to bicubic interpolation
   */
  public static void translateProcessor(int interpolationMethod, ImageProcessor ip, double xoffset,
      double yoffset, boolean clipOutput) {
    // Check if interpolation is needed
    if (xoffset == (int) xoffset && yoffset == (int) yoffset) {
      interpolationMethod = ImageProcessor.NONE;
    }

    // Bicubic interpolation can generate values outside the input range.
    // Optionally clip these. This is not applicable for ColorProcessors.
    float max = Float.NEGATIVE_INFINITY;
    if (interpolationMethod == ImageProcessor.BICUBIC && clipOutput
        && !(ip instanceof ColorProcessor)) {
      for (int i = ip.getPixelCount(); i-- > 0;) {
        if (max < ip.getf(i)) {
          max = ip.getf(i);
        }
      }
    }

    ip.setInterpolationMethod(interpolationMethod);
    ip.translate(xoffset, yoffset);

    if (interpolationMethod == ImageProcessor.BICUBIC && clipOutput
        && !(ip instanceof ColorProcessor)) {
      for (int i = ip.getPixelCount(); i-- > 0;) {
        if (ip.getf(i) > max) {
          ip.setf(i, max);
        }
      }
    }
  }

  /**
   * Iteratively search the cubic spline surface around the given pixel to maximise the value.
   *
   * @param fp Float processor containing a peak surface
   * @param xindex The peak x position
   * @param yindex The peak y position
   * @return The peak location with sub-pixel accuracy
   */
  public static double[] performCubicFit(FloatProcessor fp, int xindex, int yindex) {
    final double[] centre = new double[] {xindex, yindex};
    // Working space
    final double[] xrange = new double[3];
    final double[] yrange = new double[3];
    // This value will be progressively halved.
    // Start with a value that allows the number of iterations to fully cover the region +/- 1 pixel
    // 0.5 will result in an minimum range of 0.5 / 2^9 = 0.000976
    double range = 0.5;
    for (int c = 10; c-- > 0;) {
      performCubicFit(fp, range, centre, xrange, yrange);
      range /= 2;
    }
    return centre;
  }

  /**
   * Perform cubic fit.
   *
   * @param fp the fp
   * @param range the range
   * @param centre the centre
   * @param xrange the xrange
   * @param yrange the yrange
   */
  private static void performCubicFit(FloatProcessor fp, double range, double[] centre,
      double[] xrange, double[] yrange) {
    double peakValue = Double.NEGATIVE_INFINITY;
    xrange[0] = centre[0] - range;
    xrange[1] = centre[0];
    xrange[2] = centre[0] + range;
    yrange[0] = centre[1] - range;
    yrange[1] = centre[1];
    yrange[2] = centre[1] + range;
    for (int i = 0; i < 3; i++) {
      for (int j = 0; j < 3; j++) {
        final double v = fp.getBicubicInterpolatedPixel(xrange[i], yrange[j], fp);
        if (peakValue < v) {
          peakValue = v;
          centre[0] = xrange[i];
          centre[1] = yrange[j];
        }
      }
    }
  }

  /**
   * Find the maximum within the search rectangle.
   *
   * @param image the image
   * @param rectangle the rectangle
   * @return the maximum
   */
  private static int[] findMaximum(FloatProcessor image, Rectangle rectangle) {
    final int width = image.getWidth();
    float max = Float.NEGATIVE_INFINITY;
    int maxi = 0;
    final float[] data = (float[]) image.getPixels();
    for (int y = rectangle.y; y < rectangle.height; y++) {
      for (int x = 0, i = y * width + rectangle.x; x < rectangle.width; x++, i++) {
        if (max < data[i]) {
          max = data[i];
          maxi = i;
        }
      }
    }
    return new int[] {maxi % width, maxi / width};
  }

  /**
   * Correlate.
   *
   * @param refComplex the ref complex
   * @param targetComplex the target complex
   * @return the float processor
   */
  private static FloatProcessor correlate(FHT refComplex, FHT targetComplex) {
    final FHT fht = refComplex.conjugateMultiply(targetComplex);
    fht.setShowProgress(false);
    fht.inverseTransform();
    fht.swapQuadrants();
    fht.resetMinAndMax();
    final ImageProcessor ip = fht;
    return ip.toFloat(0, null);
  }

  /**
   * Convert the image to a Fast Hartley Transform.
   *
   * @param ip the image
   * @return the fht
   */
  FHT fht(ImageProcessor ip) {
    final FHT fht = new FHT(ip);
    fht.setShowProgress(false);
    fht.transform();
    return fht;
  }

  /**
   * Centre image on zero, padding if necessary to next square power-two above the given max
   * dimension.
   *
   * <p>Optionally apply a window function so the image blends smoothly to zero background.
   *
   * @param ip the image
   * @param maxN the max N
   * @param windowMethod the window method
   * @param padBounds the pad bounds
   * @return the float processor
   */
  FloatProcessor padAndZero(ImageProcessor ip, int maxN, WindowMethod windowMethod,
      Rectangle padBounds) {
    boolean pad = true;
    int size = 2;
    while (size < maxN) {
      size *= 2;
    }
    if (size == maxN && ip.getWidth() == maxN && ip.getHeight() == maxN) {
      pad = false;
    }
    maxN = size;

    // This should shift the image so it smoothly blends with a zero background
    // Ideally this would window the image so that the result has an average of zero with smooth
    // edges transitions.
    // However this involves shifting the image and windowing. The average depends on both
    // and so would have to be solved iteratively.

    if (windowMethod != WindowMethod.NONE) {
      // Use separable for speed.
      // ip = applyWindow(ip, windowMethod)
      ip = applyWindowSeparable(ip, windowMethod);
    }

    // Get average
    double sum = 0;
    for (int ii = 0; ii < ip.getPixelCount(); ii++) {
      sum += ip.getf(ii);
    }
    final double av = sum / ip.getPixelCount();

    // Create the result image
    final FloatProcessor ip2 = new FloatProcessor(maxN, maxN);
    final float[] data = (float[]) ip2.getPixels();

    padBounds.width = ip.getWidth();
    padBounds.height = ip.getHeight();
    if (pad) {
      // Place into middle of image => Correlation is centre-to-centre alignment
      final int x = getInsert(maxN, ip.getWidth());
      final int y = getInsert(maxN, ip.getHeight());

      padBounds.x = x;
      padBounds.y = y;

      for (int yy = 0, index = 0; yy < ip.getHeight(); yy++) {
        int ii = (yy + y) * maxN + x;
        for (int xx = 0; xx < ip.getWidth(); xx++, index++, ii++) {
          data[ii] = (float) (ip.getf(index) - av);
        }
      }
    } else {
      padBounds.x = 0;
      padBounds.y = 0;

      // Copy pixels
      for (int ii = 0; ii < ip.getPixelCount(); ii++) {
        data[ii] = (float) (ip.getf(ii) - av);
      }
    }

    return ip2;
  }

  /**
   * Gets the insert.
   *
   * @param maxN the max N
   * @param width the width
   * @return the insert
   */
  private static int getInsert(int maxN, int width) {
    // Note the FHT power spectrum centre is at n/2 of an even sized image.
    // So we must insert the centre at that point. To do this we check for odd/even
    // and offset if necessary.
    final int diff = maxN - width;
    return ((diff & 1) == 1) ? (diff + 1) / 2 : diff / 2;
  }

  /**
   * Gets the last X offset.
   *
   * @return the lastXOffset
   */
  public double getLastXOffset() {
    return lastXOffset;
  }

  /**
   * Gets the last Y offset.
   *
   * @return the lastYOffset
   */
  public double getLastYOffset() {
    return lastYOffset;
  }

  /**
   * Apply a window function to reduce edge artifacts.
   *
   * <p>Applied as two 1-dimensional window functions. Faster than the non-separable form but has
   * direction dependent corners.
   *
   * @param ip the image
   * @param windowMethod the window method
   * @return the float processor
   */
  public static FloatProcessor applyWindowSeparable(ImageProcessor ip, WindowMethod windowMethod) {
    final int maxx = ip.getWidth();
    final int maxy = ip.getHeight();
    double[] wx = null;
    double[] wy = null;

    switch (windowMethod) {
      case HANNING:
        wx = ImageWindow.hanning(maxx);
        wy = ImageWindow.hanning(maxy);
        break;
      case COSINE:
        wx = ImageWindow.cosine(maxx);
        wy = ImageWindow.cosine(maxy);
        break;
      case TUKEY:
        wx = ImageWindow.tukey(maxx);
        wy = ImageWindow.tukey(maxy);
        break;
      case NONE:
      default:
        return ip.toFloat(0, null);
    }

    final float[] data = new float[ip.getPixelCount()];

    // Calculate total signal of window function applied to image (t1).
    // Calculate total signal of window function applied to a flat signal of intensity 1 (t2).
    // Divide t1/t2 => Result is the mean shift for image so that the average is zero.

    double sumWindowFunction = 0;
    double sumImage = 0;
    for (int y = 0, i = 0; y < maxy; y++) {
      for (int x = 0; x < maxx; x++, i++) {
        final double w = wx[x] * wy[y];
        sumWindowFunction += w;
        sumImage += ip.getf(i) * w;
      }
    }

    // Shift to zero
    if (sumWindowFunction != 0) {
      final double shift = sumImage / sumWindowFunction;
      for (int y = 0, i = 0; y < maxy; y++) {
        for (int x = 0; x < maxx; x++, i++) {
          final double value = (ip.getf(i) - shift) * wx[x] * wy[y];
          data[i] = (float) value;
        }
      }
    }

    return new FloatProcessor(maxx, maxy, data, null);
  }

  /**
   * Apply a window function to reduce edge artifacts.
   *
   * <p>Applied as a non-separable form.
   *
   * @param ip the image
   * @param windowMethod the window method
   * @return the float processor
   */
  public static FloatProcessor applyWindow(ImageProcessor ip, WindowMethod windowMethod) {
    final int maxx = ip.getWidth();
    final int maxy = ip.getHeight();

    WindowFunction wf = null;
    switch (windowMethod) {
      case HANNING: //
        wf = new Hanning();
        break;
      case COSINE:
        wf = new Cosine();
        break;
      case TUKEY:
        wf = new Tukey();
        break;
      case NONE:
      default:
        return ip.toFloat(0, null);
    }

    final float[] data = new float[ip.getPixelCount()];

    final double cx = maxx * 0.5;
    final double cy = maxy * 0.5;
    final double maxDistance = Math.sqrt((double) maxx * maxx + maxy * maxy);

    // Pre-compute
    final double[] dx2 = new double[maxx];
    for (int x = 0; x < maxx; x++) {
      dx2[x] = (x - cx) * (x - cx);
    }

    // Calculate total signal of window function applied to image (t1).
    // Calculate total signal of window function applied to a flat signal of intensity 1 (t2).
    // Divide t1/t2 => Result is the mean shift for image so that the average is zero.

    double sumWindowFunction = 0;
    double sumImage = 0;
    for (int y = 0, i = 0; y < maxy; y++) {
      final double dy2 = (y - cy) * (y - cy);
      for (int x = 0; x < maxx; x++, i++) {
        final double distance = Math.sqrt(dx2[x] + dy2);
        final double w = wf.weight(0.5 - (distance / maxDistance));
        sumWindowFunction += w;
        sumImage += ip.getf(i) * w;
      }
    }

    // Shift to zero
    if (sumWindowFunction != 0) {
      final double shift = sumImage / sumWindowFunction;
      for (int y = 0, i = 0; y < maxy; y++) {
        final double dy2 = (y - cy) * (y - cy);
        for (int x = 0; x < maxx; x++, i++) {
          final double distance = Math.sqrt(dx2[x] + dy2);
          final double w = wf.weight(0.5 - (distance / maxDistance));
          final double value = (ip.getf(i) - shift) * w;
          data[i] = (float) value;
        }
      }
    }

    return new FloatProcessor(maxx, maxy, data, null);
  }

  /**
   * Checks if is do translation.
   *
   * @return if false the image will not be translated
   */
  public boolean isDoTranslation() {
    return doTranslation;
  }

  /**
   * Set to false to prevent the image processor from being translated. The translation can be
   * retrieved using the lastOffset properties.
   *
   * @param doTranslation if false the image will not be translated
   */
  public void setDoTranslation(boolean doTranslation) {
    this.doTranslation = doTranslation;
  }
}
