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

package uk.ac.sussex.gdsc.core.ij;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.util.Tools;
import java.awt.Rectangle;
import java.util.function.Consumer;
import uk.ac.sussex.gdsc.core.data.VisibleForTesting;
import uk.ac.sussex.gdsc.core.ij.process.Fht;
import uk.ac.sussex.gdsc.core.logging.NullTrackProgress;
import uk.ac.sussex.gdsc.core.logging.TrackProgress;
import uk.ac.sussex.gdsc.core.utils.ImageWindow;
import uk.ac.sussex.gdsc.core.utils.ImageWindow.Cosine;
import uk.ac.sussex.gdsc.core.utils.ImageWindow.Hanning;
import uk.ac.sussex.gdsc.core.utils.ImageWindow.NoWindowFunction;
import uk.ac.sussex.gdsc.core.utils.ImageWindow.Tukey;
import uk.ac.sussex.gdsc.core.utils.ImageWindow.WindowFunction;
import uk.ac.sussex.gdsc.core.utils.ImageWindow.WindowMethod;

/**
 * Aligns an image stack to a reference image using XY translation to maximise the correlation.
 * Takes in:
 *
 * <ul>
 *
 * <li>The reference image
 *
 * <li>The image/stack to align.
 *
 * <li>Optional Max/Min values for the X and Y translation
 *
 * <li>Window function to reduce edge artifacts in frequency space
 *
 * </ul>
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
 *
 * <p>The normalisation is based on the assumption that the target image entirely fits within the
 * reference image for all translations of interest. If this is false then normalised correlations
 * can generate values outside the interval {@code [-1, 1]}.
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

    SubPixelMethod(String name) {
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
  private Fht refFht;

  private TrackProgress progress = NullTrackProgress.getInstance();

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
   * @param correlationImageAction the correlation image action
   * @param normalisedReferenceAction the normalised reference action
   * @param normalisedTargetAction the normalised target action
   * @param clipOutput the clip output
   * @return the image plus
   */
  @SuppressWarnings("null")
  public ImagePlus align(ImagePlus refImp, ImagePlus targetImp, WindowMethod windowMethod,
      Rectangle bounds, SubPixelMethod subPixelMethod, int interpolationMethod, boolean normalised,
      Consumer<ImagePlus> correlationImageAction, Consumer<ImagePlus> normalisedReferenceAction,
      Consumer<ImagePlus> normalisedTargetAction, boolean clipOutput) {
    final ImageProcessor referenceIp = refImp.getProcessor();
    final ImagePlus targetOrRefImp = (targetImp == null) ? refImp : targetImp;

    // Check same size
    if (!isValid(referenceIp, targetOrRefImp)) {
      return null;
    }

    // Fourier transforms use the largest power-two dimension that covers both images
    int maxN = Math.max(referenceIp.getWidth(), referenceIp.getHeight());
    final int maxM = Math.max(targetOrRefImp.getWidth(), targetOrRefImp.getHeight());
    maxN = Math.max(maxN, maxM);

    this.normalisedRefIp = padAndZero(referenceIp, maxN, windowMethod, refImageBounds);
    if (normalisedReferenceAction != null) {
      normalisedReferenceAction
          .accept(new ImagePlus(refImp.getTitle() + " Normalised Ref", normalisedRefIp));
    }
    maxN = normalisedRefIp.getWidth(); // Update with the power-two size

    // Set up the output stack
    final ImageStack outStack =
        new ImageStack(targetOrRefImp.getWidth(), targetOrRefImp.getHeight());
    ImageStack correlationStack = null;
    ImageStack normalisedStack = null;
    FloatProcessor fpCorrelation = null;
    FloatProcessor fpNormalised = null;
    if (correlationImageAction != null) {
      correlationStack = new ImageStack(maxN, maxN);
      fpCorrelation = new FloatProcessor(maxN, maxN);
    }
    if (normalisedTargetAction != null) {
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

    final Fht referenceFht = fht(normalisedRefIp);

    final Rectangle localBounds =
        (bounds == null)
            ? createHalfMaxBounds(refImp.getWidth(), refImp.getHeight(), targetOrRefImp.getWidth(),
                targetOrRefImp.getHeight())
            : bounds;

    // Process each image in the target stack
    final ImageStack stack = targetOrRefImp.getStack();
    for (int slice = 1; slice <= stack.getSize(); slice++) {
      final ImageProcessor targetIp = stack.getProcessor(slice);
      outStack.addSlice(null,
          alignImages(referenceFht, sum, sumSq, targetIp, slice, windowMethod, localBounds,
              fpCorrelation, fpNormalised, subPixelMethod, interpolationMethod, clipOutput));
      if (correlationStack != null) {
        correlationStack.addSlice(null, fpCorrelation.duplicate());
      }
      if (normalisedStack != null) {
        normalisedStack.addSlice(null, fpNormalised.duplicate());
      }
      if (ImageJUtils.isInterrupted()) {
        return null;
      }
    }

    if (correlationStack != null) {
      correlationImageAction
          .accept(new ImagePlus(targetOrRefImp.getTitle() + " Correlation", correlationStack));
    }
    if (normalisedStack != null) {
      normalisedTargetAction
          .accept(new ImagePlus(targetOrRefImp.getTitle() + " Normalised Target", normalisedStack));
    }

    return new ImagePlus(targetOrRefImp.getTitle() + " Aligned", outStack);
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

    final Rectangle localBounds =
        (bounds == null)
            ? createHalfMaxBounds(refIp.getWidth(), refIp.getHeight(), targetImp.getWidth(),
                targetImp.getHeight())
            : bounds;

    // Process each image in the target stack
    final ImageStack stack = targetImp.getStack();
    for (int slice = 1; slice <= stack.getSize(); slice++) {
      final ImageProcessor targetIp = stack.getProcessor(slice);
      outStack.addSlice(null, alignImages(refFht, rollingSum, rollingSumSq, targetIp, slice,
          windowMethod, localBounds, null, null, subPixelMethod, interpolationMethod, clipOutput));
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
   * <p>The target is allowed to be an Fht as returned from
   * {@link #transformTarget(ImageProcessor, uk.ac.sussex.gdsc.core.utils.ImageWindow.WindowMethod)
   * transformTarget(ImageProcessor, WindowMethod)}. If the Fht is not the correct size then an
   * exception is thrown.
   *
   * @param targetIp the target ip
   * @param windowMethod the window method
   * @param bounds the bounds
   * @param subPixelMethod the sub pixel method
   * @return [ x_shift, y_shift, score ]
   * @throws IllegalArgumentException if the target is an Fht that is the incorrect size
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

    final Rectangle localBounds =
        (bounds == null)
            ? createHalfMaxBounds(refIp.getWidth(), refIp.getHeight(), targetIp.getWidth(),
                targetIp.getHeight())
            : bounds;

    return alignImages(refFht, rollingSum, rollingSumSq, targetIp, windowMethod, localBounds,
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
    final int maxN = Math.max(refIp.getWidth(), refIp.getHeight());

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
   * <p>Normalisation assumes that the mean of the target is 0 and the sum of squares is 1.
   *
   * @param subCorrMat the sub correlation matrix
   * @param rollingSum the rolling sum
   * @param rollingSumSq the rolling sum of squares
   * @param targetIp the target image processor
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
    // can be errors in the normalisation score due to the assumptions that the target
    // mean=0 and sum-of-squares=1 not being satisfied.
    // To see this set:
    // union = imageBounds
    final Rectangle union = refImageBounds.union(targetImageBounds);

    // This is updated within the loop
    final Rectangle regionBounds = new Rectangle(sizeU, sizeV);

    // Normalise using the denominator
    final float[] data = (float[]) subCorrMat.getPixels();
    final float[] newData = new float[data.length];
    for (int yyy = union.y; yyy < union.y + union.height; yyy++) {
      for (int xxx = union.x; xxx < union.x + union.width; xxx++) {
        double sum = 0;
        double sumSquares = 0;

        final int minU = xxx - halfSizeU - 1;
        final int maxU = Math.min(minU + sizeU, maxx - 1);
        final int minV = yyy - halfSizeV - 1;
        final int maxV = Math.min(minV + sizeV, maxy - 1);

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

        regionBounds.x = xxx - halfSizeU;
        regionBounds.y = yyy - halfSizeV;
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
        // for (int yy = yyy - halfSizeV; yy < yyy - halfSizeV + sizeV; yy++) {
        // for (int xx = xxx - halfSizeU; xx < xxx - halfSizeU + sizeU; xx++) {
        // if (xx >= 0 && xx < maxx && yy >= 0 && yy < maxy) {
        // final float value = normalisedRefIp.getf(xx, yy);
        // sx += value;
        // ssx += value * value;
        // nn++;
        // }
        // }
        // }
        // uk.ac.sussex.gdsc.core.utils.DoubleEquality eq =
        // new uk.ac.sussex.gdsc.core.utils.DoubleEquality(1e-4, 1e-16);
        // if (n != nn) {
        // System.out.printf("Wrong @ %d,%d %d <> %d\n", xxx, yyy, n, nn);
        // residuals = ssx - sx * sx / nn;
        // } else if (!eq.almostEqualRelativeOrAbsolute(sx, sum)
        // || !eq.almostEqualRelativeOrAbsolute(ssx, sumSquares)) {
        // System.out.printf("Wrong @ %d,%d %g <> %g : %g <> %g\n", xxx, yyy, sx, sum, ssx,
        // sumSquares);
        // residuals = ssx - sx * sx / nn;
        // }

        // Pearson correlation:
        // ( Σ xiyi - nx̄ӯ ) / ( (Σ xi^2 - nx̄^2) (Σ yi^2 - nӯ^2) )^0.5
        //
        // Complex conjugate computes:
        // Σ xiyi
        //
        // Assume y has a zero mean and sum of squares is 1:
        // ( Σ xiyi ) / ( (Σ xi^2 - nx̄^2) )^0.5

        if (residuals > 0) {
          // Update the correct part of the correlation
          index = yyy * maxx + xxx;
          newData[index] = (float) (data[index] / Math.sqrt(residuals));
          // Note:
          // The normalisation does not return a value between -1 and 1 when the target image is
          // bigger than half the size of the reference. This is because the assumptions of mean=0
          // and sum-of-squares=1 is false. Previously this was clipped to the range [-1,1] but this
          // can lead to poor alignments as many values can be the same. Thus the data is left
          // and the user should watch out for normalisation errors.
          // newData[index] = MathUtils.clip(-1.1f, 1.1f, newData[index]);
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
    int maxx = Math.max(width1, width2);
    int maxy = Math.max(height1, height2);
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
  @VisibleForTesting
  static boolean isValid(ImageProcessor refIp, ImagePlus targetImp) {
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
   * @param refFht the ref Fht
   * @param rollingSum the rolling sum
   * @param rollingSumSq the rolling sum of squares
   * @param targetIp the target ip
   * @param slice the slice
   * @param windowMethod the window method
   * @param bounds the bounds (not null)
   * @param fpCorrelation the fp correlation
   * @param fpNormalised the fp normalised
   * @param subPixelMethod the sub pixel method
   * @param interpolationMethod the interpolation method
   * @param clipOutput the clip output
   * @return the image processor
   */
  private ImageProcessor alignImages(Fht refFht, double[] rollingSum, double[] rollingSumSq,
      ImageProcessor targetIp, int slice, WindowMethod windowMethod, Rectangle bounds,
      FloatProcessor fpCorrelation, FloatProcessor fpNormalised, SubPixelMethod subPixelMethod,
      int interpolationMethod, boolean clipOutput) {
    lastXOffset = lastYOffset = 0;

    if (noValue(targetIp)) {
      // Zero correlation with empty image
      progress.log("Best Slice %d  x 0  y 0 = 0", slice);
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
    //
    // For more details see the alignImages method below.

    final int maxN = refFht.getWidth();

    final ImageProcessor paddedTargetIp =
        padAndZero(targetIp, maxN, windowMethod, targetImageBounds);
    final FloatProcessor normalisedTargetIp = normaliseImage(paddedTargetIp);
    final Fht targetFht = fht(normalisedTargetIp);

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

    // Restrict bounds to image limits
    intersect = intersect.intersection(
        new Rectangle(originX + bounds.x, originY + bounds.y, bounds.width, bounds.height));

    final int[] centre = findMaximum(subCorrMat, intersect);
    final float scoreMax = subCorrMat.getf(centre[0], centre[1]);
    double[] subPixelCentre;

    String estimatedScore = "";
    if (subPixelMethod == SubPixelMethod.CUBIC) {
      subPixelCentre = performCubicFit(subCorrMat, centre[0], centre[1]);
      final double score =
          subCorrMat.getBicubicInterpolatedPixel(subPixelCentre[0], subPixelCentre[1], subCorrMat);
      estimatedScore = String.format(" (interpolated score %g)", score);
    } else {
      subPixelCentre = new double[] {centre[0], centre[1]};
    }

    // The correlation image is the size of the reference.
    // Offset from centre of reference
    lastXOffset = subPixelCentre[0] - originX;
    lastYOffset = subPixelCentre[1] - originY;

    progress.log("Best Slice %d  x %g  y %g = %g%s", slice, lastXOffset, lastYOffset, scoreMax,
        estimatedScore);

    // Translate the result and crop to the original size
    if (!doTranslation) {
      return targetIp;
    }

    return translate(interpolationMethod, targetIp, lastXOffset, lastYOffset, clipOutput);
  }

  /**
   * Align images.
   *
   * @param refFht the ref Fht
   * @param rollingSum the rolling sum
   * @param rollingSumSq the rolling sum of squares
   * @param targetIp the target ip
   * @param windowMethod the window method
   * @param bounds the bounds (not null)
   * @param subPixelMethod the sub pixel method
   * @return [ x_shift, y_shift, score ]
   */
  private double[] alignImages(Fht refFht, double[] rollingSum, double[] rollingSumSq,
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

    // Pearson correlation:
    // ( Σ xiyi - nx̄ӯ ) / ( (Σ xi^2 - nx̄^2) (Σ yi^2 - nӯ^2) )^0.5
    //
    // Complex conjugate computes:
    // Σ xiyi
    //
    // If x and y have a zero mean then the standard correlation can be normalised
    // by dividing by the sum of squares:
    // ( Σ xiyi ) / ( (Σ xi^2) (Σ yi^2) )^0.5
    //
    // If x and y have a zero mean and are unit length (sum of squares is 1) then the
    // standard correlation is already normalised.
    // Note: This does not account for varying n when images are offset.
    //
    // The transformTarget method will set the mean as zero and normalise to unit length.
    // The reference is initialised by only transforming to have a zero mean.
    // This allows the plain correlation to compute a good alignment for most cases.
    // The normalisation step will attempt to normalise the result using the mean and sum of
    // squares of the reference (x) computed with the rolling sums. The target mean and sum of
    // squares is computed once (should be 0 and 1). This works well if the target is smaller
    // than the reference and always fits inside the reference for the bounds of interest.
    // When larger then the normalisation begins to fail.

    final int maxN = refFht.getWidth();

    // Allow the input target to be a Fht
    Fht targetFht;
    if (targetIp instanceof Fht) {
      if (targetIp.getWidth() != maxN) {
        throw new IllegalArgumentException("Invalid Fht target");
      }
      targetFht = (Fht) targetIp;
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

    // Restrict bounds to image limits
    intersect = intersect.intersection(
        new Rectangle(originX + bounds.x, originY + bounds.y, bounds.width, bounds.height));

    final int[] centre = findMaximum(subCorrMat, intersect);
    double scoreMax = subCorrMat.getf(centre[0], centre[1]);
    double[] subPixelCentre;

    if (subPixelMethod == SubPixelMethod.CUBIC) {
      subPixelCentre = performCubicFit(subCorrMat, centre[0], centre[1]);
      scoreMax =
          subCorrMat.getBicubicInterpolatedPixel(subPixelCentre[0], subPixelCentre[1], subCorrMat);
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
   * Transforms a target image processor for alignment with the initialised reference. The Fht can
   * be passed to the
   * {@link #align(ImageProcessor, ImageWindow.WindowMethod, Rectangle, SubPixelMethod)} method.
   *
   * <p>A window function is applied to the processor. The result is then shifted to have a mean of
   * zero and normalised to unit length (sum of squares is 1).
   *
   * <p>If the {@link #initialiseReference(ImageProcessor, ImageWindow.WindowMethod, boolean)}
   * method has not been called this returns null.
   *
   * @param targetIp the target ip
   * @param windowMethod the window method
   * @return The Fht
   */
  public Fht transformTarget(ImageProcessor targetIp, WindowMethod windowMethod) {
    if (refFht == null || targetIp == null) {
      return null;
    }
    final int maxN = refFht.getWidth();
    final ImageProcessor paddedTargetIp =
        padAndZero(targetIp, maxN, windowMethod, targetImageBounds);
    final FloatProcessor normalisedTargetIp = normaliseImage(paddedTargetIp);
    return fht(normalisedTargetIp);
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

    final int localInterpolationMethod = (xoffset == (int) xoffset && yoffset == (int) yoffset)
        // No interpolation is needed
        ? ImageProcessor.NONE
        : interpolationMethod;

    // Bicubic interpolation can generate values outside the input range.
    // Optionally clip these. This is not applicable for ColorProcessors.
    float max = Float.NEGATIVE_INFINITY;
    if (localInterpolationMethod == ImageProcessor.BICUBIC && clipOutput
        && !(ip instanceof ColorProcessor)) {
      for (int i = ip.getPixelCount(); i-- > 0;) {
        if (max < ip.getf(i)) {
          max = ip.getf(i);
        }
      }
    }

    ip.setInterpolationMethod(localInterpolationMethod);
    ip.translate(xoffset, yoffset);

    if (localInterpolationMethod == ImageProcessor.BICUBIC && clipOutput
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
    final double[] centre = {xindex, yindex};
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
    for (int y = 0; y < rectangle.height; y++) {
      for (int x = 0, i = (y + rectangle.y) * width + rectangle.x; x < rectangle.width; x++, i++) {
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
  private static FloatProcessor correlate(Fht refComplex, Fht targetComplex) {
    final Fht fht = refComplex.conjugateMultiply(targetComplex);
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
  Fht fht(ImageProcessor ip) {
    final Fht fht = new Fht(ip);
    fht.transform();
    fht.initialiseFastMultiply();
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

    // This should shift the image so it smoothly blends with a zero background
    // Ideally this would window the image so that the result has an average of zero with smooth
    // edges transitions.
    // However this involves shifting the image and windowing. The average depends on both
    // and so would have to be solved iteratively.

    final ImageProcessor wip = (windowMethod == WindowMethod.NONE) ? ip
        // Use separable for speed.
        : applyWindowSeparable(ip, windowMethod);

    // Get average
    double sum = 0;
    for (int ii = 0; ii < wip.getPixelCount(); ii++) {
      sum += wip.getf(ii);
    }
    final double av = sum / wip.getPixelCount();

    // Create the result image
    final FloatProcessor ip2 = new FloatProcessor(size, size);
    final float[] data = (float[]) ip2.getPixels();

    padBounds.width = wip.getWidth();
    padBounds.height = wip.getHeight();
    if (pad) {
      // Place into middle of image => Correlation is centre-to-centre alignment
      final int x = getInsert(size, wip.getWidth());
      final int y = getInsert(size, wip.getHeight());

      padBounds.x = x;
      padBounds.y = y;

      int index = 0;
      for (int yy = 0; yy < wip.getHeight(); yy++) {
        int ii = (yy + y) * size + x;
        for (int xx = 0; xx < wip.getWidth(); xx++, index++, ii++) {
          data[ii] = (float) (wip.getf(index) - av);
        }
      }
    } else {
      padBounds.x = 0;
      padBounds.y = 0;

      // Copy pixels
      for (int ii = 0; ii < wip.getPixelCount(); ii++) {
        data[ii] = (float) (wip.getf(ii) - av);
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
    // Note the Fht power spectrum centre is at n/2 of an even sized image.
    // So we must insert the centre at that point. To do this we check for odd/even
    // and offset if necessary.
    final int diff = maxN - width;
    final int odd = diff & 1;
    return (diff + odd) / 2;
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
   * <p>The resulting image has a mean of zero.
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
        wx = ImageWindow.createWindow(WindowMethod.NONE, maxx);
        wy = ImageWindow.createWindow(WindowMethod.NONE, maxy);
    }

    final float[] data = new float[ip.getPixelCount()];

    // Calculate total signal of window function applied to image (t1).
    // Calculate total signal of window function applied to a flat signal of intensity 1 (t2).
    // Divide t1/t2 => Result is the mean shift for image so that the average is zero.

    double sumWindowFunction = 0;
    double sumImage = 0;
    int index = 0;
    for (int y = 0; y < maxy; y++) {
      for (int x = 0; x < maxx; x++, index++) {
        final double w = wx[x] * wy[y];
        sumWindowFunction += w;
        sumImage += ip.getf(index) * w;
      }
    }

    // Shift to zero. Assumes the sum of the window function is non-zero.
    // This should be ensured by the ImageWindow class.
    final double shift = sumImage / sumWindowFunction;
    index = 0;
    for (int y = 0; y < maxy; y++) {
      for (int x = 0; x < maxx; x++, index++) {
        final double value = (ip.getf(index) - shift) * wx[x] * wy[y];
        data[index] = (float) value;
      }
    }

    return new FloatProcessor(maxx, maxy, data, null);
  }

  /**
   * Apply a window function to reduce edge artifacts.
   *
   * <p>Applied as a non-separable form.
   *
   * <p>The resulting image has a mean of zero.
   *
   * @param ip the image
   * @param windowMethod the window method
   * @return the float processor
   */
  public static FloatProcessor applyWindow(ImageProcessor ip, WindowMethod windowMethod) {
    WindowFunction wf = null;
    switch (windowMethod) {
      case HANNING: //
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
        wf = NoWindowFunction.INSTANCE;
    }

    final float[] data = new float[ip.getPixelCount()];

    final int maxx = ip.getWidth();
    final int maxy = ip.getHeight();
    if (maxx <= 2 && maxy <= 2) {
      // Cannot window small images
      wf = NoWindowFunction.INSTANCE;
    }
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
    int index = 0;
    for (int y = 0; y < maxy; y++) {
      final double dy2 = (y - cy) * (y - cy);
      for (int x = 0; x < maxx; x++, index++) {
        final double distance = Math.sqrt(dx2[x] + dy2);
        final double w = wf.weight(0.5 - (distance / maxDistance));
        sumWindowFunction += w;
        sumImage += ip.getf(index) * w;
      }
    }

    // Shift to zero. Assumes the sum of the window function is non-zero.
    // Can happen when dimensions are below 2x2 but not above that as some distances will be less
    // than the max distance.
    final double shift = sumImage / sumWindowFunction;
    index = 0;
    for (int y = 0; y < maxy; y++) {
      final double dy2 = (y - cy) * (y - cy);
      for (int x = 0; x < maxx; x++, index++) {
        final double distance = Math.sqrt(dx2[x] + dy2);
        final double w = wf.weight(0.5 - (distance / maxDistance));
        final double value = (ip.getf(index) - shift) * w;
        data[index] = (float) value;
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

  /**
   * Sets the progress tracker used for logging.
   *
   * @param progress the new progress
   */
  public void setProgress(TrackProgress progress) {
    this.progress = NullTrackProgress.createIfNull(progress);
  }
}
