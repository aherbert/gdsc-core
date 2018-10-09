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

import org.apache.commons.math3.util.FastMath;

import java.awt.Rectangle;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ColorProcessor;
import ij.process.FHT;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.util.Tools;

/**
 * Aligns an image stack to a reference image using XY translation to maximise the correlation.
 * Takes in: <ul> <li>The reference image <li>The image/stack to align. <li>Optional Max/Min values
 * for the X and Y translation <li>Window function to reduce edge artifacts in frequency space </ul>
 * <p> The alignment is calculated using the maximum correlation between the images. The correlation
 * is computed using the frequency domain (note that conjugate multiplication in the frequency
 * domain is equivalent to correlation in the space domain). <p> Output new stack with the best
 * alignment with optional sub-pixel accuracy. <p> By default restricts translation so that at least
 * half of the smaller image width/height is within the larger image (half-max translation). This
 * can be altered by providing a translation bounds. Note that when using normalised correlation all
 * scores are set to zero outside the half-max translation due to potential floating-point summation
 * error during normalisation.
 */
public class AlignImagesFFT {
  /**
   * The Enum WindowMethod.
   */
  public enum WindowMethod {
    //@formatter:off
		/** The none method. */
		NONE{ @Override
		public String getName() { return "None"; }},

		/** The hanning method. */
		HANNING{ @Override
		public String getName() { return "Hanning"; }},

		/** The cosine method. */
		COSINE{ @Override
		public String getName() { return "Cosine"; }},

		/** The tukey method. */
		TUKEY{ @Override
		public String getName() { return "Tukey"; }};
		//@formatter:on

    /** {@inheritDoc} */
    @Override
    public String toString() {
      return getName();
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    abstract public String getName();
  }

  /**
   * The Enum SubPixelMethod.
   */
  public enum SubPixelMethod {
    //@formatter:off
		/** The none method. */
		NONE{ @Override
		public String getName() { return "None"; }},

		/** The cubic method. */
		CUBIC{ @Override
		public String getName() { return "Cubic"; }},

		/** The gaussian method. */
		GAUSSIAN{ @Override
		public String getName() { return "Gaussian"; }};
		//@formatter:on

    /** {@inheritDoc} */
    @Override
    public String toString() {
      return getName();
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    abstract public String getName();
  }

  /** The last X offset. */
  private double lastXOffset = 0;

  /** The last Y offset. */
  private double lastYOffset = 0;

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

  /**
   * Aligns all images in the target stack to the current processor in the reference. <p> If no
   * target is provided then all slices are aligned to the current processor in the reference.
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
    final ImageProcessor refIp = refImp.getProcessor();
    if (targetImp == null) {
      targetImp = refImp;
    }

    // Check same size
    if (!isValid(refIp, targetImp)) {
      return null;
    }

    // Fourier transforms use the largest power-two dimension that covers both images
    int maxN = FastMath.max(refIp.getWidth(), refIp.getHeight());
    final int maxM = FastMath.max(targetImp.getWidth(), targetImp.getHeight());
    maxN = FastMath.max(maxN, maxM);

    this.normalisedRefIp = padAndZero(refIp, maxN, windowMethod, refImageBounds);
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
    double[] s = null;
    double[] ss = null;
    if (normalised) {
      s = new double[normalisedRefIp.getPixelCount()];
      ss = new double[s.length];
      calculateRollingSums(normalisedRefIp, s, ss);
    }

    final FHT refFHT = fft(normalisedRefIp, maxN);

    if (bounds == null) {
      bounds = createHalfMaxBounds(refImp.getWidth(), refImp.getHeight(), targetImp.getWidth(),
          targetImp.getHeight());
    }

    // Process each image in the target stack
    final ImageStack stack = targetImp.getStack();
    for (int slice = 1; slice <= stack.getSize(); slice++) {
      final ImageProcessor targetIp = stack.getProcessor(slice);
      outStack.addSlice(null, alignImages(refFHT, s, ss, targetIp, slice, windowMethod, bounds,
          fpCorrelation, fpNormalised, subPixelMethod, interpolationMethod, clipOutput));
      if (showCorrelationImage) {
        correlationStack.addSlice(null, fpCorrelation.duplicate());
      }
      if (showNormalisedImage) {
        normalisedStack.addSlice(null, fpNormalised.duplicate());
      }
      if (Utils.isInterrupted()) {
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

  private ImageProcessor refIp = null;
  private double[] s = null;
  private double[] ss = null;
  private FHT refFHT = null;

  /**
   * Initialises the reference image for batch alignment. All target images should be equal or
   * smaller than the reference.
   *
   * @param refImp the ref imp
   * @param windowMethod the window method
   * @param normalised True if the correlation should be normalised (score of -1 to 1)
   */
  public void init(ImagePlus refImp, WindowMethod windowMethod, boolean normalised) {
    refIp = null;
    s = null;
    ss = null;
    refFHT = null;

    if (refImp == null) {
      return;
    }

    init(refImp.getProcessor(), windowMethod, normalised);
  }

  /**
   * Initialises the reference image for batch alignment. All target images should be equal or
   * smaller than the reference.
   *
   * @param refIp the ref ip
   * @param windowMethod the window method
   * @param normalised True if the correlation should be normalised (score of -1 to 1)
   */
  public void init(ImageProcessor refIp, WindowMethod windowMethod, boolean normalised) {
    s = null;
    ss = null;
    refFHT = null;

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
    s = null;
    ss = null;
    if (normalised) {
      s = new double[normalisedRefIp.getPixelCount()];
      ss = new double[s.length];
      calculateRollingSums(normalisedRefIp, s, ss);
    }

    refFHT = fft(normalisedRefIp, maxN);
  }

  /**
   * Aligns all images in the target stack to the pre-initialised reference.
   *
   * @param targetImp the target imp
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
    if (refFHT == null || targetImp == null) {
      return null;
    }

    final int maxN = refFHT.getWidth();

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
      outStack.addSlice(null, alignImages(refFHT, s, ss, targetIp, slice, windowMethod, bounds,
          null, null, subPixelMethod, interpolationMethod, clipOutput));
      if (Utils.isInterrupted()) {
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
    if (refFHT == null || targetIp == null) {
      return null;
    }

    final int maxN = refFHT.getWidth();

    // Check correct size
    if (targetIp.getWidth() > maxN || targetIp.getHeight() > maxN) {
      return null;
    }

    if (bounds == null) {
      bounds = createHalfMaxBounds(refIp.getWidth(), refIp.getHeight(), targetIp.getWidth(),
          targetIp.getHeight());
    }

    return alignImages(refFHT, s, ss, targetIp, windowMethod, bounds, subPixelMethod);
  }

  /**
   * Calculate rolling sums.
   *
   * @param ip the image
   * @param s_ the s
   * @param ss the ss
   */
  private static void calculateRollingSums(FloatProcessor ip, double[] s_, double[] ss) {
    // Compute the rolling sum and sum of squares
    // s(u,v) = f(u,v) + s(u-1,v) + s(u,v-1) - s(u-1,v-1)
    // ss(u,v) = f(u,v) * f(u,v) + ss(u-1,v) + ss(u,v-1) - ss(u-1,v-1)
    // where s(u,v) = ss(u,v) = 0 when either u,v < 0

    final int maxx = ip.getWidth();
    final int maxy = ip.getHeight();
    final float[] originalData = (float[]) ip.getPixels();
    final double[] data = Tools.toDouble(originalData);

    // First row
    double cs_ = 0; // Column sum
    double css = 0; // Column sum-squares
    for (int i = 0; i < maxx; i++) {
      cs_ += data[i];
      css += data[i] * data[i];
      s_[i] = cs_;
      ss[i] = css;
    }

    // Remaining rows:
    // sum = rolling sum of row + sum of row above
    for (int y = 1; y < maxy; y++) {
      int i = y * maxx;
      cs_ = 0;
      css = 0;

      // Remaining columns
      for (int x = 0; x < maxx; x++, i++) {
        cs_ += data[i];
        css += data[i] * data[i];

        s_[i] = s_[i - maxx] + cs_;
        ss[i] = ss[i - maxx] + css;
      }
    }
  }

  /**
   * Normalise the correlation matrix using the standard deviation of the region from the reference
   * that is covered by the target.
   *
   * @param subCorrMat the sub corr mat
   * @param s the s
   * @param ss the ss
   * @param targetIp the target ip
   */
  private void normalise(FloatProcessor subCorrMat, double[] s, double[] ss,
      ImageProcessor targetIp) {
    final int maxx = subCorrMat.getWidth();
    final int maxy = subCorrMat.getHeight();
    final Rectangle imageBounds = new Rectangle(0, 0, maxx, maxy); // refImageBounds;

    final int NU = targetIp.getWidth();
    final int NV = targetIp.getHeight();

    // Locate where the target image was inserted when padding
    final int x = targetImageBounds.x; // (maxx - NU) / 2;
    final int y = targetImageBounds.y; // (maxy - NV) / 2;

    // IJ.log(String.format("maxx=%d, maxy=%d, NU=%d, NV=%d, x=%d, y=%d", maxx, maxy, NU, NV, x,
    // y));

    // Calculate overlap:
    // Assume a full size target image relative to the reference and then compensate with the insert
    // location
    final int halfNU = maxx / 2 - x;
    final int halfNV = maxy / 2 - y;

    // Normalise within the bounds of the largest image (i.e. only allow translation
    // up to half of the longest edge from the reference or target).
    // The further the translation from the half-max translation the more likely there
    // can be errors in the normalisation score due to floating point summation errors.
    // This is observed mainly at the very last pixel overlap between images.
    // To see this set:
    // union = imageBounds;
    // TODO - More analysis to determine under what conditions this occurs.
    final Rectangle union = refImageBounds.union(targetImageBounds);

    // Normalise using the denominator
    final float[] data = (float[]) subCorrMat.getPixels();
    final float[] newData = new float[data.length];
    for (int yyy = union.y; yyy < union.y + union.height; yyy++) {
      int i = yyy * maxx + union.x;
      for (int xxx = union.x; xxx < union.x + union.width; xxx++, i++) {
        double sum = 0;
        double sumSquares = 0;

        int minU = xxx - halfNU - 1;
        final int maxU = FastMath.min(minU + NU, maxx - 1);
        int minV = yyy - halfNV - 1;
        final int maxV = FastMath.min(minV + NV, maxy - 1);

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
        sum += s[index];
        sumSquares += ss[index];

        if (minU >= 0) {
          // - s(u-1,v+N-1)
          index = maxV * maxx + minU;
          sum -= s[index];
          sumSquares -= ss[index];
        }
        if (minV >= 0) {
          // - s(u+N-1,v-1)
          index = minV * maxx + maxU;
          sum -= s[index];
          sumSquares -= ss[index];

          if (minU >= 0) {
            // + s(u-1,v-1)
            index = minV * maxx + minU;
            sum += s[index];
            sumSquares += ss[index];
          }
        }

        // Reset to bounds to calculate the number of pixels
        if (minU < 0) {
          minU = 0;
        }
        if (minV < 0) {
          minV = 0;
        }

        final Rectangle regionBounds = new Rectangle(xxx - halfNU, yyy - halfNV, NU, NV);
        final Rectangle r = imageBounds.intersection(regionBounds);

        // int n = (maxU - minU + 1) * (maxV - minV + 1);
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
        // for (int yy = yyy - halfNV; yy < yyy - halfNV + NV; yy++)
        // for (int xx = xxx - halfNU; xx < xxx - halfNU + NU; xx++)
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
          newData[i] = (float) (data[i] / normalisation);
          // Watch out for normalisation errors which cause problems when displaying the image data.
          if (newData[i] < -1.1f) {
            newData[i] = -1.1f;
          }
          if (newData[i] > 1.1f) {
            newData[i] = 1.1f;
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
    final Rectangle bounds = new Rectangle(minXShift, minYShift, w, h);
    return bounds;
  }

  /**
   * Checks if is valid.
   *
   * @param refIp the ref ip
   * @param targetImp the target imp
   * @return true, if is valid
   */
  private static boolean isValid(ImageProcessor refIp, ImagePlus targetImp) {
    if (refIp == null || targetImp == null) {
      return false;
    }

    // Check images have values. No correlation is possible with
    if (noValue(refIp)) {
      return false;
    }

    return true;
  }

  /**
   * No value.
   *
   * @param ip the image
   * @return true if the image has not pixels with a value
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
   * @param refFHT the ref FHT
   * @param s the s
   * @param ss the ss
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
  private ImageProcessor alignImages(FHT refFHT, double[] s, double[] ss, ImageProcessor targetIp,
      int slice, WindowMethod windowMethod, Rectangle bounds, FloatProcessor fpCorrelation,
      FloatProcessor fpNormalised, SubPixelMethod subPixelMethod, int interpolationMethod,
      boolean clipOutput) {
    lastXOffset = lastYOffset = 0;

    if (noValue(targetIp)) {
      // Zero correlation with empty image
      IJ.log(String.format("Best Slice %d  x %g  y %g = %g", slice, 0, 0, 0));
      if (fpCorrelation != null) {
        fpCorrelation.setPixels(new float[refFHT.getPixelCount()]);
      }
      if (fpNormalised != null) {
        fpNormalised.setPixels(new float[refFHT.getPixelCount()]);
      }
      return targetIp.duplicate();
    }

    // Perform correlation analysis in Fourier space (A and B transform to F and G)
    // using the complex conjugate of G multiplied by F:
    // C(u,v) = F(u,v) G*(u,v)

    final int maxN = refFHT.getWidth();

    final ImageProcessor paddedTargetIp =
        padAndZero(targetIp, maxN, windowMethod, targetImageBounds);
    final FloatProcessor normalisedTargetIp = normalise(paddedTargetIp);
    final FHT targetFHT = fft(normalisedTargetIp, maxN);
    final FloatProcessor subCorrMat = correlate(refFHT, targetFHT);

    // new ImagePlus("Unnormalised correlation", subCorrMat.duplicate()).show();

    final int originX = (maxN / 2);
    final int originY = (maxN / 2);

    // Normalise using the denominator
    if (s != null) {
      normalise(subCorrMat, s, ss, targetIp);
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

    final int[] iCoord =
        getPeak(subCorrMat, intersect.x, intersect.y, intersect.width, intersect.height);
    final float scoreMax = subCorrMat.getf(iCoord[0], iCoord[1]);
    final double[] dCoord = new double[] {iCoord[0], iCoord[1]};

    String estimatedScore = "";
    if (subPixelMethod != SubPixelMethod.NONE) {
      double[] centre = null;
      if (subPixelMethod == SubPixelMethod.CUBIC) {
        centre = performCubicFit(subCorrMat, iCoord[0], iCoord[1]);
      } else {
        // Perform sub-peak analysis using the method taken from Jpiv
        centre = performGaussianFit(subCorrMat, iCoord[0], iCoord[1]);
        // Check the centre has not moved too far
        if (!(Math.abs(dCoord[0] - iCoord[0]) < intersect.width / 2
            && Math.abs(dCoord[1] - iCoord[1]) < intersect.height / 2)) {
          centre = null;
        }
      }

      if (centre != null) {
        dCoord[0] = centre[0];
        dCoord[1] = centre[1];

        final double score =
            subCorrMat.getBicubicInterpolatedPixel(centre[0], centre[1], subCorrMat);
        // if (score < -1)
        // score = -1;
        // if (score > 1)
        // score = 1;
        estimatedScore = String.format(" (interpolated score %g)", score);
      }
    } else if (IJ.debugMode) {
      // Used for debugging - Check if interpolation rounds to a different integer
      double[] centre = performCubicFit(subCorrMat, iCoord[0], iCoord[1]);
      if (centre != null) {
        centre[0] = Math.round(centre[0]);
        centre[1] = Math.round(centre[1]);

        if (centre[0] != iCoord[0] || centre[1] != iCoord[1]) {
          IJ.log(String.format("Cubic rounded to different integer: %d,%d => %d,%d", iCoord[0],
              iCoord[1], (int) centre[0], (int) centre[1]));
        }
      }

      centre = performGaussianFit(subCorrMat, iCoord[0], iCoord[1]);
      if (centre != null) {
        centre[0] = Math.round(centre[0]);
        centre[1] = Math.round(centre[1]);

        if (centre[0] != iCoord[0] || centre[1] != iCoord[1]) {
          IJ.log(String.format("Gaussian rounded to different integer: %d,%d => %d,%d", iCoord[0],
              iCoord[1], (int) centre[0], (int) centre[1]));
        }
      }
    }

    // The correlation image is the size of the reference.
    // Offset from centre of reference
    lastXOffset = dCoord[0] - originX;
    lastYOffset = dCoord[1] - originY;

    IJ.log(String.format("Best Slice %d  x %g  y %g = %g%s", slice, lastXOffset, lastYOffset,
        scoreMax, estimatedScore));

    // Translate the result and crop to the original size
    if (!doTranslation) {
      return targetIp;
    }

    final ImageProcessor resultIp =
        translate(interpolationMethod, targetIp, lastXOffset, lastYOffset, clipOutput);
    return resultIp;
  }

  /**
   * Align images.
   *
   * @param refFHT the ref FHT
   * @param s the s
   * @param ss the ss
   * @param targetIp the target ip
   * @param windowMethod the window method
   * @param bounds the bounds
   * @param subPixelMethod the sub pixel method
   * @return the double[]
   */
  private double[] alignImages(FHT refFHT, double[] s, double[] ss, ImageProcessor targetIp,
      WindowMethod windowMethod, Rectangle bounds, SubPixelMethod subPixelMethod) {
    lastXOffset = lastYOffset = 0;

    if (noValue(targetIp)) {
      // Zero correlation with empty image
      return new double[] {0, 0, 0};
    }

    // Perform correlation analysis in Fourier space (A and B transform to F and G)
    // using the complex conjugate of G multiplied by F:
    // C(u,v) = F(u,v) G*(u,v)

    final int maxN = refFHT.getWidth();

    // Allow the input target to be a FHT
    FHT targetFHT;
    if (targetIp instanceof FHT && targetIp.getWidth() == maxN) {
      targetFHT = (FHT) targetIp;
    } else {
      targetFHT = transformTarget(targetIp, windowMethod);
    }
    final FloatProcessor subCorrMat = correlate(refFHT, targetFHT);

    final int originX = (maxN / 2);
    final int originY = (maxN / 2);

    // Normalise using the denominator
    if (s != null) {
      normalise(subCorrMat, s, ss, targetIp);
    }

    Rectangle intersect = new Rectangle(0, 0, subCorrMat.getWidth(), subCorrMat.getHeight());

    // Restrict the translation
    if (bounds != null) {
      // Restrict bounds to image limits
      intersect = intersect.intersection(
          new Rectangle(originX + bounds.x, originY + bounds.y, bounds.width, bounds.height));
    }

    final int[] iCoord =
        getPeak(subCorrMat, intersect.x, intersect.y, intersect.width, intersect.height);
    double scoreMax = subCorrMat.getf(iCoord[0], iCoord[1]);
    final double[] dCoord = new double[] {iCoord[0], iCoord[1]};

    double[] centre = null;
    switch (subPixelMethod) {
      case CUBIC:
        centre = performCubicFit(subCorrMat, iCoord[0], iCoord[1]);
        break;
      case GAUSSIAN:
        // Perform sub-peak analysis using the method taken from Jpiv
        centre = performGaussianFit(subCorrMat, iCoord[0], iCoord[1]);
        // Check the centre has not moved too far
        if (!(Math.abs(dCoord[0] - iCoord[0]) < intersect.width / 2
            && Math.abs(dCoord[1] - iCoord[1]) < intersect.height / 2)) {
          centre = null;
        }
        break;
      default:
        break;
    }

    if (centre != null) {
      dCoord[0] = centre[0];
      dCoord[1] = centre[1];

      double score = subCorrMat.getBicubicInterpolatedPixel(centre[0], centre[1], subCorrMat);
      if (score < -1) {
        score = -1;
      }
      if (score > 1) {
        score = 1;
      }
      scoreMax = score;
    }

    // The correlation image is the size of the reference.
    // Offset from centre of reference
    lastXOffset = dCoord[0] - originX;
    lastYOffset = dCoord[1] - originY;

    return new double[] {lastXOffset, lastYOffset, scoreMax};
  }

  /**
   * Transforms a target image processor for alignment with the initialised reference. The FHT can
   * be passed to the {@link #align(ImageProcessor, WindowMethod, Rectangle, SubPixelMethod)} method
   * <p> If the {@link #init(ImageProcessor, WindowMethod, boolean)} method has not been called this
   * returns null.
   *
   * @param targetIp the target ip
   * @param windowMethod the window method
   * @return The FHT
   */
  public FHT transformTarget(ImageProcessor targetIp, WindowMethod windowMethod) {
    if (refFHT == null || targetIp == null) {
      return null;
    }
    final int maxN = refFHT.getWidth();
    FHT targetFHT;
    final ImageProcessor paddedTargetIp =
        padAndZero(targetIp, maxN, windowMethod, targetImageBounds);
    final FloatProcessor normalisedTargetIp = normalise(paddedTargetIp);
    targetFHT = fft(normalisedTargetIp, maxN);
    return targetFHT;
  }

  /**
   * Convert to unit length, return a float processor.
   *
   * @param ip the image
   * @return the float processor
   */
  public static FloatProcessor normalise(ImageProcessor ip) {
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
   * @param xOffset the x offset
   * @param yOffset the y offset
   * @param clipOutput Set to true to ensure the output image has the same max as the input. Applies
   *        to bicubic interpolation
   * @return New translated processor
   */
  public static ImageProcessor translate(int interpolationMethod, ImageProcessor ip, double xOffset,
      double yOffset, boolean clipOutput) {
    final ImageProcessor newIp = ip.duplicate();
    translateProcessor(interpolationMethod, newIp, xOffset, yOffset, clipOutput);
    return newIp;
  }

  /**
   * Translate the image processor in place.
   *
   * @param interpolationMethod the interpolation method
   * @param ip the image
   * @param xOffset the x offset
   * @param yOffset the y offset
   * @param clipOutput Set to true to ensure the output image has the same max as the input. Applies
   *        to bicubic interpolation
   */
  public static void translateProcessor(int interpolationMethod, ImageProcessor ip, double xOffset,
      double yOffset, boolean clipOutput) {
    // Check if interpolation is needed
    if (xOffset == (int) xOffset && yOffset == (int) yOffset) {
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
    ip.translate(xOffset, yOffset);

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
   * @param i The peak x position
   * @param j The peak y position
   * @return The peak location with sub-pixel accuracy
   */
  public static double[] performCubicFit(FloatProcessor fp, int i, int j) {
    final double[] centre = new double[] {i, j};
    // Working space
    final double[] xrange = new double[3];
    final double[] yrange = new double[3];
    // This value will be progressively halved.
    // Start with a value that allows the number of iterations to fully cover the region +/- 1 pixel
    // TODO - Test if 0.67 is better as this can cover +/- 1 pixel in 2 iterations
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
   * Perform an interpolated Gaussian fit. <p> The following functions for peak finding using
   * Gaussian fitting have been extracted from Jpiv: http://www.jpiv.vennemann-online.de/
   *
   * @param fp Float processor containing a peak surface
   * @param i The peak x position
   * @param j The peak y position
   * @return The peak location with sub-pixel accuracy
   */
  public static double[] performGaussianFit(FloatProcessor fp, int i, int j) {
    // Extract Pixel block
    final float[][] pixelBlock = new float[fp.getWidth()][fp.getHeight()];
    for (int x = pixelBlock.length; x-- > 0;) {
      for (int y = pixelBlock[0].length; y-- > 0;) {
        if (Float.isNaN(fp.getf(x, y))) {
          pixelBlock[x][y] = -1;
        } else {
          pixelBlock[x][y] = fp.getf(x, y);
        }
      }
    }

    // Extracted as per the code in Jpiv2.PivUtils:
    final int x = 0, y = 0, w = fp.getWidth(), h = fp.getHeight();
    int[] iCoord = new int[2];
    double[] dCoord = new double[2];
    // This will weight the function more towards the centre of the correlation pixels.
    // I am not sure if this is necessary.
    // pixelBlock = divideByWeightingFunction(pixelBlock, x, y, w, h);
    iCoord = getPeak(pixelBlock);
    dCoord = gaussianPeakFit(pixelBlock, iCoord[0], iCoord[1]);
    double[] ret = null;
    // more or less acceptable peak fit
    if (Math.abs(dCoord[0] - iCoord[0]) < w / 2 && Math.abs(dCoord[1] - iCoord[1]) < h / 2) {
      dCoord[0] += x;
      dCoord[1] += y;
      // Jpiv block is in [Y,X] format (not [X,Y])
      ret = new double[] {dCoord[1], dCoord[0]};

      // IJ.log(String.format("Fitted x %d -> %g y %d -> %g",
      // i, ret[0],
      // j, ret[1]));
    }
    return (ret);
  }

  /**
   * Divides the correlation matrix by a pyramid weighting function.
   *
   * @param subCorrMat The biased correlation function
   * @param xOffset If this matrix is merely a search area within a larger correlation matrix, this
   *        is the offset of the search area.
   * @param yOffset If this matrix is merely a search area within a larger correlation matrix, this
   *        is the offset of the search area.
   * @param w Width of the original correlation matrix.
   * @param h Height of the original correlation matrix.
   * @return The corrected correlation function
   */
  @SuppressWarnings("unused")
  private static float[][] divideByWeightingFunction(float[][] subCorrMat, int xOffset, int yOffset,
      int w, int h) {
    for (int i = 0; i < subCorrMat.length; i++) {
      for (int j = 0; j < subCorrMat[0].length; j++) {
        subCorrMat[i][j] = subCorrMat[i][j]
            * (Math.abs(j + xOffset - w / 2) / w * 2 + Math.abs(i + yOffset - h / 2) / h * 2 + 1);
      }
    }
    return subCorrMat;
  }

  /**
   * Finds the highest value in a correlation matrix.
   *
   * @param subCorrMat A single correlation matrix.
   * @return The indices of the highest value {i,j} or {y,x}.
   */
  private static int[] getPeak(float[][] subCorrMat) {
    final int[] coord = new int[2];
    float peakValue = 0;
    for (int i = 0; i < subCorrMat.length; ++i) {
      for (int j = 0; j < subCorrMat[0].length; ++j) {
        if (subCorrMat[i][j] > peakValue) {
          peakValue = subCorrMat[i][j];
          coord[0] = j;
          coord[1] = i;
        }
      }
    }
    return (coord);
  }

  /**
   * Gaussian peak fit. See Raffel, Willert, Kompenhans; Particle Image Velocimetry; 3rd printing;
   * S. 131 for details
   *
   * @param subCorrMat some two dimensional data containing a correlation peak
   * @param x the horizontal peak position
   * @param y the vertical peak position
   * @return a double array containing the peak position with sub pixel accuracy
   */
  private static double[] gaussianPeakFit(float[][] subCorrMat, int x, int y) {
    final double[] coord = new double[2];
    // border values
    if (x == 0 || x == subCorrMat[0].length - 1 || y == 0 || y == subCorrMat.length - 1) {
      coord[0] = x;
      coord[1] = y;
    } else {
      coord[0] = x + (Math.log(subCorrMat[y][x - 1]) - Math.log(subCorrMat[y][x + 1]))
          / (2 * Math.log(subCorrMat[y][x - 1]) - 4 * Math.log(subCorrMat[y][x])
              + 2 * Math.log(subCorrMat[y][x + 1]));
      coord[1] = y + (Math.log(subCorrMat[y - 1][x]) - Math.log(subCorrMat[y + 1][x]))
          / (2 * Math.log(subCorrMat[y - 1][x]) - 4 * Math.log(subCorrMat[y][x])
              + 2 * Math.log(subCorrMat[y + 1][x]));
    }
    return (coord);
  }

  /**
   * Gets the peak.
   *
   * @param subCorrMat the sub corr mat
   * @param minX the min X
   * @param minY the min Y
   * @param w the w
   * @param h the h
   * @return the peak
   */
  private static int[] getPeak(FloatProcessor subCorrMat, int minX, int minY, int w, int h) {
    final int width = subCorrMat.getWidth();
    float max = Float.NEGATIVE_INFINITY;
    int maxi = 0;
    final float[] data = (float[]) subCorrMat.getPixels();
    for (int y = minY; y < minY + h; y++) {
      for (int x = 0, i = y * width + minX; x < w; x++, i++) {
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
   * Fft.
   *
   * @param ip the image
   * @param maxN the max N
   * @return the fht
   */
  // The following Fast Fourier Transform routines have been extracted from the ij.plugins.FFT class
  FHT fft(ImageProcessor ip, int maxN) {
    final FHT fht = new FHT(ip);
    fht.setShowProgress(false);
    fht.transform();
    return fht;
  }

  /**
   * Centre image on zero, padding if necessary to next square power-two above the given max
   * dimension. <p> Optionally apply a window function so the image blends smoothly to zero
   * background.
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
    int i = 2;
    while (i < maxN) {
      i *= 2;
    }
    if (i == maxN && ip.getWidth() == maxN && ip.getHeight() == maxN) {
      pad = false;
    }
    maxN = i;

    // This should shift the image so it smoothly blends with a zero background
    // Ideally this would window the image so that the result has an average of zero with smooth
    // edges transitions.
    // However this involves shifting the image and windowing. The average depends on both
    // and so would have to be solved iteratively.

    if (windowMethod != WindowMethod.NONE) {
      // Use separable for speed.
      // ip = applyWindow(ip, windowMethod);
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
   * Apply a window function to reduce edge artifacts. <p> Applied as two 1-dimensional window
   * functions. Faster than the nonseparable form but has direction dependent corners.
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
        wx = hanning(maxx);
        wy = hanning(maxy);
        break;
      case COSINE:
        wx = cosine(maxx);
        wy = cosine(maxy);
        break;
      case TUKEY:
        wx = tukey(maxx, ALPHA);
        wy = tukey(maxy, ALPHA);
        break;
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
    final double shift = sumImage / sumWindowFunction;
    // double sum = 0;
    for (int y = 0, i = 0; y < maxy; y++) {
      for (int x = 0; x < maxx; x++, i++) {
        final double value = (ip.getf(i) - shift) * wx[x] * wy[y];
        // sum += value;
        data[i] = (float) value;
      }
    }

    return new FloatProcessor(maxx, maxy, data, null);
  }

  /**
   * Apply a window function to reduce edge artifacts <p> Applied as a nonseparable form.
   *
   * @param ip the image
   * @param windowMethod the window method
   * @return the float processor
   */
  public static FloatProcessor applyWindow(ImageProcessor ip, WindowMethod windowMethod) {
    // if (true)
    // return applyWindowSeparable(ip, windowMethod, duplicate);

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
        wf = new Tukey(ALPHA);
        break;
      default:
        return ip.toFloat(0, null);
    }

    final float[] data = new float[ip.getPixelCount()];

    final double cx = maxx * 0.5;
    final double cy = maxy * 0.5;
    final double maxDistance = Math.sqrt(maxx * maxx + maxy * maxy);

    // Precompute
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
    final double shift = sumImage / sumWindowFunction;
    // double sum = 0;
    for (int y = 0, i = 0; y < maxy; y++) {
      final double dy2 = (y - cy) * (y - cy);
      for (int x = 0; x < maxx; x++, i++) {
        final double distance = Math.sqrt(dx2[x] + dy2);
        final double w = wf.weight(0.5 - (distance / maxDistance));
        final double value = (ip.getf(i) - shift) * w;
        // sum += value;
        data[i] = (float) value;
      }
    }

    return new FloatProcessor(maxx, maxy, data, null);
  }

  /** The alpha. */
  private static double ALPHA = 0.5;

  /**
   * The Interface WindowFunction.
   */
  interface WindowFunction {

    /**
     * Return the weight for the window at a fraction of the distance from the edge of the window.
     *
     * @param fractionDistance (range 0-1)
     * @return the double
     */
    double weight(double fractionDistance);
  }

  /**
   * The Class Hanning.
   */
  static class Hanning implements WindowFunction {

    /** {@inheritDoc} */
    @Override
    public double weight(double fractionDistance) {
      return 0.5 * (1 - Math.cos(Math.PI * 2 * fractionDistance));
    }
  }

  /**
   * The Class Cosine.
   */
  static class Cosine implements WindowFunction {

    /** {@inheritDoc} */
    @Override
    public double weight(double fractionDistance) {
      return Math.sin(Math.PI * fractionDistance);
    }
  }

  /**
   * The Class Tukey.
   */
  static class Tukey implements WindowFunction {

    /** The alpha. */
    final double alpha;

    /**
     * Instantiates a new tukey.
     *
     * @param alpha the alpha
     */
    public Tukey(double alpha) {
      this.alpha = alpha;
    }

    /** {@inheritDoc} */
    @Override
    public double weight(double fractionDistance) {
      if (fractionDistance < alpha / 2) {
        return 0.5 * (1 + Math.cos(Math.PI * (2 * fractionDistance / alpha - 1)));
      }
      if (fractionDistance > 1 - alpha / 2) {
        return 0.5 * (1 + Math.cos(Math.PI * (2 * fractionDistance / alpha - 2 / alpha + 1)));
      }
      return 1;
    }
  }

  // Should these be replaced with periodic functions as per use in spectral analysis:
  // http://en.wikipedia.org/wiki/Window_function

  /**
   * Window.
   *
   * @param wf the wf
   * @param N the n
   * @return the double[]
   */
  private static double[] window(WindowFunction wf, int N) {
    final double N_1 = N - 1;
    final double[] w = new double[N];
    // Assume symmetry
    final int middle = N / 2;
    for (int i = 0, j = N - 1; i <= middle; i++, j--) {
      w[i] = w[j] = wf.weight(i / N_1);
    }
    return w;
  }

  /**
   * Hanning.
   *
   * @param N the n
   * @return the double[]
   */
  private static double[] hanning(int N) {
    return window(new Hanning(), N);
  }

  /**
   * Cosine.
   *
   * @param N the n
   * @return the double[]
   */
  private static double[] cosine(int N) {
    return window(new Cosine(), N);
  }

  /**
   * Tukey.
   *
   * @param N the n
   * @param alpha the alpha
   * @return the double[]
   */
  private static double[] tukey(int N, double alpha) {
    return window(new Tukey(alpha), N);
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
