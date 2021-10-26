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

package uk.ac.sussex.gdsc.core.threshold;

import java.util.logging.Level;
import java.util.logging.Logger;
import uk.ac.sussex.gdsc.core.utils.MathUtils;

// History of the Auto_ThresholdImageJ plugin of G. Landini:
// Autothreshold segmentation
// Following the guidelines at http://pacific.mpi-cbg.de/wiki/index.php/PlugIn_Design_Guidelines
// ImageJ plugin by G. Landini at bham. ac. uk
// 1.0 never released
// 1.1 2009/Apr/08 Undo single images, fixed the stack returning to slice 1
// 1.2 2009/Apr/11 global stack threshold, option to avoid displaying, fixed the stack returning to
// slice 1, fixed upper border of montage,
// 1.3 2009/Apr/11 fixed Stack option with 'Try all' method
// 1.4 2009/Apr/11 fixed 'ignore black' and 'ignore white' for stack histograms
// 1.5 2009/Apr/12 Mean method, MinimumErrorIterative method , enhanced Triangle
// 1.6 2009/Apr/14 Reverted IsoData to a copy of IJ's code as the other version does not always
// return the same value as IJ
// 1.7 2009/Apr/14 small fixes, restore histogram in Triangle if reversed
// 1.8 2009/Jun/01 Set the threshold to foreground colour
// 1.9 2009/Oct/30 report both isodata and IJ's default methods
// 1.10 2010/May/25 We are a package!
// 1.10 2011/Jan/31 J. Schindelin added support for 16 bit images and speedup of the Huang method
// 1.11 2011/Mar/31 Alex Herbert submitted a patch to threshold the stack from any slice position
// 1.12 2011/Apr/09 Fixed: Minimum with 16bit images (search data range only), setting threshold
// without applying the mask, Yen and Isodata with 16 bits offset images, histogram bracketing to
// speed up
// 1.13 2011/Apr/13 Revised the way 16bit thresholds are shown
// 1.14 2011/Apr/14 IsoData issues a warning if threshold not found
//
// Changes incorporated after the initial version based on 1.14:
//
// 1.15 2013/Feb/19 Added 'break' in the minimum method for cases where there is a constant
// histogram after the 2nd peak
// 1.16 2016/Jul/13 Fixed temporary array in Minimum method
// 1.17 2017/May/22 Updated Otsu's method to be more overflow safe. This is not updated with
// with Emre Celebi's code (Fourier library) as per Auto_Threshold.
// Fixed a 16 bit overflow in the Mean method computation

/**
 * Provides thresholding methods based on a histogram.
 *
 * <p>The methods have been extracted from the Auto_Threshold ImageJ plugin of G. Landini.
 *
 * @see <a href="https://imagej.net/Auto_Threshold">Auto Threshold</a>
 */
public class AutoThreshold {

  /** The Constant logger. */
  private static final Logger logger = Logger.getLogger(AutoThreshold.class.getName());

  /**
   * The auto-threshold method.
   */
  public enum Method {
    /** The none method. */
    NONE("None"),
    /** The default method. */
    DEFAULT("Default"),
    /** The huang method. */
    HUANG("Huang"),
    /** The intermodes method. */
    INTERMODES("Intermodes"),
    /** The iso data method. */
    ISO_DATA("IsoData"),
    /** The li method. */
    LI("Li"),
    /** The max entropy method. */
    MAX_ENTROPY("MaxEntropy"),
    /** The mean method. */
    MEAN("Mean"),
    /** The mean plus std dev method. */
    MEAN_PLUS_STD_DEV("MeanPlusStdDev"),
    /** The min error i method. */
    MIN_ERROR_I("MinError(I)"),
    /** The minimum method. */
    MINIMUM("Minimum"),
    /** The moments method. */
    MOMENTS("Moments"),
    /** The otsu method. */
    OTSU("Otsu"),
    /** The percentile method. */
    PERCENTILE("Percentile"),
    /** The renyi entropy method. */
    RENYI_ENTROPY("RenyiEntropy"),
    /** The shanbhag method. */
    SHANBHAG("Shanbhag"),
    /** The triangle method. */
    TRIANGLE("Triangle"),
    /** The yen method. */
    YEN("Yen");

    /** The name. */
    private final String nameString;

    Method(String name) {
      this.nameString = name;
    }

    @Override
    public String toString() {
      return nameString;
    }
  }

  private static final Method[] methodValues;
  private static final String[] methodNames;
  private static final String[] methodNamesWithoutNone;

  static {
    methodValues = Method.values();
    methodNames = new String[methodValues.length];
    methodNamesWithoutNone = new String[methodValues.length - 1];
    for (int i = 0; i < methodValues.length; i++) {
      methodNames[i] = methodValues[i].toString();
      if (i != 0) {
        methodNamesWithoutNone[i - 1] = methodValues[i].toString();
      }
    }
  }

  /**
   * Gets the methods.
   *
   * @return the methods
   */
  public static String[] getMethods() {
    return methodNames.clone();
  }

  /**
   * Gets the methods.
   *
   * @param ignoreNone Set to true to remove the {@link Method#NONE} option
   * @return the methods
   */
  public static String[] getMethods(boolean ignoreNone) {
    if (ignoreNone) {
      return methodNamesWithoutNone.clone();
    }
    return methodNames.clone();
  }

  /**
   * Gets the method from method name.
   *
   * <p>Returns {@link Method#NONE} if the name is not recognised.
   *
   * @param name the name
   * @return the method
   */
  public static Method getMethod(String name) {
    for (int i = 0; i < methodNames.length; i++) {
      if (methodNames[i].equals(name)) {
        return methodValues[i];
      }
    }
    return Method.NONE;
  }

  /**
   * Gets the method from the index of the method names.
   *
   * <p>Returns {@link Method#NONE} if the name is not recognised.
   *
   * @param index the index
   * @param ignoreNone Set to true to remove the {@link Method#NONE} option
   * @return the method
   */
  public static Method getMethod(int index, boolean ignoreNone) {
    final int target = (ignoreNone) ? index + 1 : index;
    if (target >= 0 && target < methodValues.length) {
      return methodValues[target];
    }
    return Method.NONE;
  }

  /**
   * Original IJ implementation for compatibility.
   *
   * @param data the data
   * @return the threshold (or -1 if not found)
   */
  public static int ijDefault(int[] data) {
    final int maxValue = data.length - 1;

    int min = 0;
    while ((data[min] == 0) && (min < maxValue)) {
      min++;
    }
    int max = maxValue;
    while ((data[max] == 0) && (max > 0)) {
      max--;
    }
    if (min >= max) {
      return data.length / 2;
    }

    double result;
    double sum1;
    double sum2;
    double sum3;
    double sum4;
    int movingIndex = min;
    do {
      sum1 = sum2 = sum3 = sum4 = 0.0;
      for (int i = min; i <= movingIndex; i++) {
        sum1 += (double) i * data[i];
        sum2 += data[i];
      }
      for (int i = (movingIndex + 1); i <= max; i++) {
        sum3 += (double) i * data[i];
        sum4 += data[i];
      }
      result = (sum1 / sum2 + sum3 / sum4) / 2.0;
      movingIndex++;
    } while ((movingIndex + 1) <= result && movingIndex < max - 1);

    return (int) Math.round(result);
  }

  /**
   * Implements Huang's fuzzy thresholding method. Uses Shannon's entropy function (one can also use
   * Yager's entropy function) Huang L.-K. and Wang M.-J.J. (1995) "Image Thresholding by Minimizing
   * the Measures of Fuzziness" Pattern Recognition, 28(1): 41-51. Reimplemented (to handle 16-bit
   * efficiently) by Johannes Schindelin Jan 31, 2011.
   *
   * <p>Note: This method does not match the version built in to ImageJ.
   *
   * @param data the data
   * @return the threshold (or -1 if not found)
   */
  public static int huang(int[] data) {
    // find first and last non-empty bin
    int first;
    int last;
    for (first = 0; first < data.length && data[first] == 0; first++) {
      // do nothing
    }
    for (last = data.length - 1; last > first && data[last] == 0; last--) {
      // do nothing
    }
    if (first >= last) {
      return -1;
    }

    // calculate the cumulative density and the weighted cumulative density
    final double[] cumulDensity = new double[last + 1];
    final double[] weightedCumulDensity = new double[last + 1];
    cumulDensity[0] = data[0];
    for (int i = Math.max(1, first); i <= last; i++) {
      cumulDensity[i] = cumulDensity[i - 1] + data[i];
      weightedCumulDensity[i] = weightedCumulDensity[i - 1] + i * data[i];
    }

    // precalculate the summands of the entropy given the absolute difference x - mu (integral)
    final double rangeC = (double) last - first;
    final double[] sumMu = new double[last + 1 - first];
    for (int i = 1; i < sumMu.length; i++) {
      final double mu = 1 / (1 + Math.abs(i) / rangeC);
      sumMu[i] = -mu * Math.log(mu) - (1 - mu) * Math.log(1 - mu);
    }

    // calculate the threshold
    int bestThreshold = 0;
    double bestEntropy = Double.MAX_VALUE;
    for (int threshold = first; threshold <= last; threshold++) {
      double entropy = 0;
      int mu = (int) Math.round(weightedCumulDensity[threshold] / cumulDensity[threshold]);
      for (int i = first; i <= threshold; i++) {
        entropy += sumMu[Math.abs(i - mu)] * data[i];
      }
      mu = (int) Math.round((weightedCumulDensity[last] - weightedCumulDensity[threshold])
          / (cumulDensity[last] - cumulDensity[threshold]));
      for (int i = threshold + 1; i <= last; i++) {
        entropy += sumMu[Math.abs(i - mu)] * data[i];
      }

      if (bestEntropy > entropy) {
        bestEntropy = entropy;
        bestThreshold = threshold;
      }
    }

    return bestThreshold;
  }

  /**
   * Bimodal test.
   *
   * @param data the data
   * @return true, if successful
   */
  protected static boolean bimodalTest(double[] data) {
    final int len = data.length;
    int modes = 0;

    for (int k = 1; k < len - 1; k++) {
      if (data[k - 1] < data[k] && data[k + 1] < data[k]) {
        modes++;
        if (modes > 2) {
          return false;
        }
      }
    }
    return modes == 2;
  }

  /**
   * J. M. S. Prewitt and M. L. Mendelsohn, "The analysis of cell images," in Annals of the New York
   * Academy of Sciences, vol. 128, pp. 1035-1053, 1966. ported to ImageJ plugin by G.Landini from
   * Antti Niemisto's Matlab code (GPL) Original Matlab code Copyright (C) 2004 Antti Niemisto. See
   * http://www.cs.tut.fi/~ant/histthresh/ for an excellent slide presentation and the original
   * Matlab code.
   *
   * <p>Assumes a bimodal histogram. The histogram needs is smoothed (using a running average of
   * size 3, iteratively) until there are only two local maxima. j and k. Threshold t is (j+k)/2.
   * Images with histograms having extremely unequal peaks or a broad and ﬂat valley are unsuitable
   * for this method.
   *
   * @param data the data
   * @return the threshold (or -1 if not found)
   */
  public static int intermodes(int[] data) {
    final double[] iHisto = new double[data.length];
    int iter = 0;
    int threshold = -1;
    for (int i = 0; i < data.length; i++) {
      iHisto[i] = data[i];
    }

    while (!bimodalTest(iHisto)) {
      // smooth with a 3 point running mean filter
      double previous = 0;
      double current = 0;
      double next = iHisto[0];
      for (int i = 0; i < data.length - 1; i++) {
        previous = current;
        current = next;
        next = iHisto[i + 1];
        iHisto[i] = (previous + current + next) / 3;
      }
      iHisto[data.length - 1] = (current + next) / 3;
      iter++;
      if (iter > 10000) {
        threshold = -1;
        logger.fine("Intermodes Threshold not found after 10000 iterations.");
        return threshold;
      }
    }

    // The threshold is the mean between the two peaks.
    int tt = 0;
    for (int i = 1; i < data.length - 1; i++) {
      if (iHisto[i - 1] < iHisto[i] && iHisto[i + 1] < iHisto[i]) {
        tt += i;
      }
    }
    threshold = (int) Math.floor(tt / 2.0);
    return threshold;
  }

  /**
   * Also called intermeans. Iterative procedure based on the isodata algorithm [T.W. Ridler, S.
   * Calvard, Picture thresholding using an iterative selection method, IEEE Trans. System, Man and
   * Cybernetics, SMC-8 (1978) 630-632.]. The procedure divides the image into objects and
   * background by taking an initial threshold, then the averages of the pixels at or below the
   * threshold and pixels above are computed. The averages of those two values are computed, the
   * threshold is incremented and the process is repeated until the threshold is larger than the
   * composite average. That is, threshold = (average background + average objects)/2. The code in
   * ImageJ that implements this function is the getAutoThreshold() method in the ImageProcessor
   * class.
   *
   * <p>From: Tim Morris (dtm@ap.co.umist.ac.uk) Subject: Re: Thresholding method? posted to
   * sci.image.processing on 1996/06/24 The algorithm implemented in NIH Image sets the threshold as
   * that grey value, G, for which the average of the averages of the grey values below and above G
   * is equal to G. It does this by initialising G to the lowest sensible value and iterating:
   *
   * @param data the data
   * @return the threshold (or -1 if not found)
   */
  public static int isoData(int[] data) {
    // L = the average grey value of pixels with intensities < G
    // H = the average grey value of pixels with intensities > G
    // is G = (L + H)/2?
    // yes => exit
    // no => increment G and repeat
    //
    // There is a discrepancy with IJ because they are slightly different methods
    int threshold = 0;
    for (int i = 0; i < data.length; i++) {
      if (data[i] > 0) {
        threshold = i + 1;
        break;
      }
    }
    for (;;) {
      int low = 0;
      int totLow = 0;
      for (int i = 0; i < threshold; i++) {
        totLow = totLow + data[i];
        low = low + (data[i] * i);
      }
      int high = 0;
      int totHigh = 0;
      for (int i = threshold + 1; i < data.length; i++) {
        totHigh += data[i];
        high += (data[i] * i);
      }
      if (totLow > 0 && totHigh > 0) {
        low /= totLow;
        high /= totHigh;
        if (threshold == (int) Math.round((low + high) / 2.0)) {
          break;
        }
      }
      threshold++;
      if (threshold > data.length - 2) {
        logger.fine("IsoData Threshold not found.");
        return -1;
      }
    }
    return threshold;
  }

  /**
   * Implements Li's Minimum Cross Entropy thresholding method. This implementation is based on the
   * iterative version (Ref. 2) of the algorithm. 1) Li C.H. and Lee C.K. (1993) "Minimum Cross
   * Entropy Thresholding" Pattern Recognition, 26(4): 617-625 2) Li C.H. and Tam P.K.S. (1998) "An
   * Iterative Algorithm for Minimum Cross Entropy Thresholding"Pattern Recognition Letters, 18(8):
   * 771-776 3) Sezgin M. and Sankur B. (2004) "Survey over Image Thresholding Techniques and
   * Quantitative Performance Evaluation" Journal of Electronic Imaging, 13(1): 146-165. See
   * http://citeseer.ist.psu.edu/sezgin04survey.html.
   *
   * @param data the data
   * @return the threshold (or -1 if not found)
   */
  public static int li(int[] data) {
    // Ported to ImageJ plugin by G.Landini from E Celebi's fourier_0.8 routines.

    int numPixels = 0;
    for (int ih = 0; ih < data.length; ih++) {
      numPixels += data[ih];
    }

    /* Calculate the mean gray-level */
    double mean = 0.0;
    for (int ih = 0; ih < data.length; ih++) {
      // 0 + 1?
      mean += ih * data[ih];
    }
    mean /= numPixels;
    /* Initial estimate */
    double newThresh = mean;

    int threshold;
    double oldThresh;
    do {
      oldThresh = newThresh;
      threshold = (int) (oldThresh + 0.5); /* range */
      /* Calculate the means of background and object pixels */
      /* Background */
      /* sum of the background pixels at a given threshold */
      int sumBack = 0;
      /* number of background pixels at a given threshold */
      int numBack = 0;
      for (int ih = 0; ih <= threshold; ih++) {
        sumBack += ih * data[ih];
        numBack += data[ih];
      }
      /* mean of the background pixels at a given threshold */
      final double meanBack = MathUtils.div0(sumBack, numBack);
      /* Object */
      /* sum of the object pixels at a given threshold */
      int sumObj = 0;
      /* number of object pixels at a given threshold */
      int numObj = 0;
      for (int ih = threshold + 1; ih < data.length; ih++) {
        sumObj += ih * data[ih];
        numObj += data[ih];
      }
      /* mean of the object pixels at a given threshold */
      final double meanObj = MathUtils.div0(sumObj, numObj);

      /* Calculate the new threshold: Equation (7) in Ref. 2 */
      // new_thresh = simple_round ( ( mean_back - mean_obj ) / ( Math.log ( mean_back ) - Math.log
      // ( mean_obj ) ) )
      // simple_round ( double x )
      // return ( int ) ( IS_NEG ( x ) ? x - .5 : x + .5 )
      //
      // #define IS_NEG( x ) ( ( x ) < -DBL_EPSILON )
      // DBL_EPSILON = 2.220446049250313E-16
      final double temp = (meanBack - meanObj) / (Math.log(meanBack) - Math.log(meanObj));

      // For a valid input histogram 'temp' is never negative as all counts are positive:
      // meanObj > meanBack
      // Thus we drop the check for if temp is negative.
      newThresh = (int) (temp + 0.5);
      /*
       * Stop the iterations when the difference between the new and old threshold values is less
       * than the tolerance
       */
      // Use a fixed threshold tolerance of 0.5
    } while (Math.abs(newThresh - oldThresh) > 0.5);
    return threshold;
  }

  /**
   * Implements Kapur-Sahoo-Wong (Maximum Entropy) thresholding method Kapur J.N., Sahoo P.K., and
   * Wong A.K.C. (1985) "A New Method for Gray-Level Picture Thresholding Using the Entropy of the
   * Histogram" Graphical Models and Image Processing, 29(3): 273-285.
   *
   * @param data the data
   * @return the threshold (or -1 if not found)
   */
  public static int maxEntropy(int[] data) {
    // M. Emre Celebi
    // 06.15.2007
    // Ported to ImageJ plugin by G.Landini from E Celebi's fourier_0.8 routines
    final double[] normHisto = new double[data.length]; /* normalized histogram */
    final double[] p1 = new double[data.length]; /* cumulative normalized histogram */
    final double[] p2 = new double[data.length];

    int total = 0;
    for (int ih = 0; ih < data.length; ih++) {
      total += data[ih];
    }

    for (int ih = 0; ih < data.length; ih++) {
      normHisto[ih] = (double) data[ih] / total;
    }

    p1[0] = normHisto[0];
    p2[0] = 1.0 - p1[0];
    for (int ih = 1; ih < data.length; ih++) {
      p1[ih] = p1[ih - 1] + normHisto[ih];
      p2[ih] = 1.0 - p1[ih];
    }

    /* Determine the first non-zero bin */
    int firstBin = 0;
    for (int ih = 0; ih < data.length; ih++) {
      if (Math.abs(p1[ih]) >= 2.220446049250313E-16) {
        firstBin = ih;
        break;
      }
    }

    /* Determine the last non-zero bin */
    int lastBin = data.length - 1;
    for (int ih = data.length - 1; ih >= firstBin; ih--) {
      if (Math.abs(p2[ih]) >= 2.220446049250313E-16) {
        lastBin = ih;
        break;
      }
    }

    // Calculate the total entropy each gray-level
    // and find the threshold that maximizes it
    /* max entropy */
    double maxEnt = Double.MIN_VALUE;
    int threshold = -1;

    for (int it = firstBin; it <= lastBin; it++) {
      /* Entropy of the background pixels */
      double entBack = 0.0;
      for (int ih = 0; ih <= it; ih++) {
        if (data[ih] != 0) {
          entBack -= (normHisto[ih] / p1[it]) * Math.log(normHisto[ih] / p1[it]);
        }
      }

      /* Entropy of the object pixels */
      double entObj = 0.0;
      for (int ih = it + 1; ih < data.length; ih++) {
        if (data[ih] != 0) {
          entObj -= (normHisto[ih] / p2[it]) * Math.log(normHisto[ih] / p2[it]);
        }
      }

      /* Total entropy */
      final double totEnt = entBack + entObj;

      if (maxEnt < totEnt) {
        maxEnt = totEnt;
        threshold = it;
      }
    }
    return threshold;
  }

  /**
   * C. A. Glasbey, "An analysis of histogram-based thresholding algorithms," CVGIP: Graphical
   * Models and Image Processing, vol. 55, pp. 532-537, 1993.
   *
   * <p>The threshold is the mean of the greyscale data.
   *
   * @param data the data
   * @return the threshold (or -1 if not found)
   */
  public static int mean(int[] data) {
    int threshold = -1;
    long tot = 0;
    long sum = 0;
    for (int i = 0; i < data.length; i++) {
      tot += data[i];
      sum += ((long) i * (long) data[i]);
    }
    if (tot == 0) {
      return 0;
    }
    threshold = (int) Math.floor(sum / tot);
    return threshold;
  }

  /**
   * The multiplier used within the MeanPlusStdDev calculation.
   */
  private static double stdDevMultiplier = 3;

  /**
   * Sets the multiplier used within the MeanPlusStdDev calculation.
   *
   * @param stdDevMultiplier the new multiplier
   */
  public static void setStdDevMultiplier(double stdDevMultiplier) {
    AutoThreshold.stdDevMultiplier = stdDevMultiplier;
  }

  /**
   * Gets the multiplier used within the MeanPlusStdDev calculation.
   *
   * @return the multiplier
   */
  public static double getStdDevMultiplier() {
    return stdDevMultiplier;
  }

  /**
   * Get the mean plus a multiplier of the standard deviation.
   *
   * <p>This is not threadsafe as the standard deviation multiplier is static. Use the instance
   * method for thread safety ({@link #meanPlusStdDev(int[], double)}) if you are changing the
   * multiplier in different threads.
   *
   * @param data The histogram data
   * @return The threshold
   */
  public static int meanPlusStdDev(int[] data) {
    final double m = AutoThreshold.stdDevMultiplier;
    return new AutoThreshold().meanPlusStdDev(data, m);
  }

  /**
   * Mean plus std dev.
   *
   * @param data the data
   * @param stdDevMultiplier the std dev multiplier
   * @return the threshold (or -1 if not found)
   */
  public int meanPlusStdDev(int[] data, double stdDevMultiplier) {
    // The threshold is the mean of the greyscale data plus a multiplier of the image standard
    // deviation
    int count;
    double value;
    double tot = 0;
    double sum = 0;
    double sum2 = 0.0;
    for (int i = 0; i < data.length; i++) {
      if (data[i] > 0) {
        count = data[i];
        tot += count;
        value = i;
        sum += value * count;
        sum2 += (value * value) * count;
      }
    }

    if (tot <= 0) {
      return 0;
    }

    final double mean = sum / tot;

    // Get the Std.Dev.
    // Note: total is > 0
    double stdDev;
    final double d = tot;
    stdDev = (d * sum2 - sum * sum) / d;
    if (stdDev > 0.0) {
      stdDev = Math.sqrt(stdDev / (d - 1.0));
    } else {
      stdDev = 0.0;
    }

    int threshold = (int) Math.floor(mean + stdDev * stdDevMultiplier);

    // Do a bounds check
    if (threshold < 0) {
      threshold = 0;
    } else if (threshold > data.length - 1) {
      threshold = data.length - 1;
    }

    return threshold;
  }

  /**
   * Kittler and J. Illingworth, "Minimum error thresholding," Pattern Recognition, vol. 19, pp.
   * 41-47, 1986. C. A. Glasbey, "An analysis of histogram-based thresholding algorithms," CVGIP:
   * Graphical Models and Image Processing, vol. 55, pp. 532-537, 1993. Ported to ImageJ plugin by
   * G.Landini from Antti Niemisto's Matlab code (GPL). Original Matlab code Copyright (C) 2004
   * Antti Niemisto. See http://www.cs.tut.fi/~ant/histthresh/ for an excellent slide presentation
   * and the original Matlab code.
   *
   * @param data the data
   * @return the threshold (or -1 if not found)
   */
  public static int minErrorI(int[] data) {
    // Initial estimate for the threshold is found with the MEAN
    // algorithm.
    int threshold = mean(data);
    int prevThreshold = -2;

    // Pre-compute
    final double[] cumulativeCount = new double[data.length];
    final double[] cumulativeSum = new double[data.length];
    final double[] cumulativeSumSq = new double[data.length];
    double count = 0;
    double sum = 0;
    double sumSq = 0;
    for (int i = 0; i < data.length; i++) {
      count += data[i];
      cumulativeCount[i] = count;
      sum += i * data[i];
      cumulativeSum[i] = sum;
      sumSq += i * i * data[i];
      cumulativeSumSq[i] = sumSq;
    }

    final int end = data.length - 1;
    while (threshold != prevThreshold) {
      // Calculate some statistics.
      final double mu = cumulativeSum[threshold] / cumulativeCount[threshold];
      final double nu = (cumulativeSum[end] - cumulativeSum[threshold])
          / (cumulativeCount[end] - cumulativeCount[threshold]);
      final double pterm = cumulativeCount[threshold] / cumulativeCount[end];
      final double qterm =
          (cumulativeCount[end] - cumulativeCount[threshold]) / cumulativeCount[end];
      final double sigma2 = cumulativeSumSq[threshold] / cumulativeCount[threshold] - (mu * mu);
      final double tau2 = (cumulativeSumSq[end] - cumulativeSumSq[threshold])
          / (cumulativeCount[end] - cumulativeCount[threshold]) - (nu * nu);

      // The terms of the quadratic equation to be solved.
      final double w0 = 1.0 / sigma2 - 1.0 / tau2;
      final double w1 = mu / sigma2 - nu / tau2;
      final double w2 = (mu * mu) / sigma2 - (nu * nu) / tau2
          + Math.log10((sigma2 * (qterm * qterm)) / (tau2 * (pterm * pterm)));

      // If the next threshold would be imaginary, return with the current one.
      final double sqterm = (w1 * w1) - w0 * w2;
      if (sqterm < 0) {
        logger.fine("MinError(I): not converging. Try \'Ignore black/white\' options");
        return threshold;
      }

      // The updated threshold is the integer part of the solution of the quadratic equation.
      prevThreshold = threshold;
      final double temp = (w1 + Math.sqrt(sqterm)) / w0;

      if (Double.isNaN(temp)) {
        logger.fine("MinError(I): NaN, not converging. Try \'Ignore black/white\' options");
        threshold = prevThreshold;
      } else {
        threshold = (int) Math.floor(temp);
      }
    }
    return threshold;
  }

  /**
   * J. M. S. Prewitt and M. L. Mendelsohn, "The analysis of cell images," in Annals of the New York
   * Academy of Sciences, vol. 128, pp. 1035-1053, 1966. ported to ImageJ plugin by G.Landini from
   * Antti Niemisto's Matlab code (GPL). Original Matlab code Copyright (C) 2004 Antti Niemisto. See
   * http://www.cs.tut.fi/~ant/histthresh/ for an excellent slide presentation and the original
   * Matlab code.
   *
   * @param data the data
   * @return the threshold (or -1 if not found)
   */
  public static int minimum(int[] data) {
    if (data.length < 3) {
      // Cannot be bimodal with less than 3 points.
      return -1;
    }

    // Assumes a bimodal histogram. The histogram needs is smoothed (using a
    // running average of size 3, iteratively) until there are only two local maxima.
    // Threshold t is such that yt−1 > yt ≤ yt+1.
    // Images with histograms having extremely unequal peaks or a broad and
    // ﬂat valley are unsuitable for this method.
    int iter = 0;
    int max = -1;
    double[] histogram = new double[data.length];

    for (int i = 0; i < data.length; i++) {
      histogram[i] = data[i];
      if (data[i] > 0) {
        max = i;
      }
    }

    // Working space for the smoothed histogram
    double[] smoothedHistogram = new double[data.length];

    while (!bimodalTest(histogram)) {

      // Check for convergence
      iter++;
      if (iter > 10000) {
        logger.fine("Minimum Threshold not found after 10000 iterations.");
        return -1;
      }

      // smooth with a 3 point running mean filter
      for (int i = 1; i < data.length - 1; i++) {
        smoothedHistogram[i] = (histogram[i - 1] + histogram[i] + histogram[i + 1]) / 3;
      }
      // 0 outside the start
      smoothedHistogram[0] = (histogram[0] + histogram[1]) / 3;
      // 0 outside the end
      smoothedHistogram[data.length - 1] =
          (histogram[data.length - 2] + histogram[data.length - 1]) / 3;

      // Swap
      final double[] tmp = histogram;
      histogram = smoothedHistogram;
      smoothedHistogram = tmp;
    }
    // The threshold is the minimum between the two peaks. modified for 16 bits
    for (int i = 1; i < max; i++) {
      if (histogram[i - 1] > histogram[i] && histogram[i + 1] >= histogram[i]) {
        return i;
      }
    }
    return -1;
  }

  /**
   * W. Tsai, "Moment-preserving thresholding: a new approach," Computer Vision, Graphics, and Image
   * Processing, vol. 29, pp. 377-393, 1985. Ported to ImageJ plugin by G.Landini from the the open
   * source project FOURIER 0.8 by M. Emre Celebi , Department of Computer Science, Louisiana State
   * University in Shreveport Shreveport, LA 71115, USA.
   * http://sourceforge.net/projects/fourier-ipal. http://www.lsus.edu/faculty/~ecelebi/fourier.htm.
   *
   * @param data the data
   * @return the threshold (or -1 if not found)
   */
  public static int moments(int[] data) {
    double total = 0;

    for (int i = 0; i < data.length; i++) {
      total += data[i];
    }

    if (total == 0) {
      return 0;
    }

    final double[] histo = new double[data.length];
    for (int i = 0; i < data.length; i++) {
      histo[i] = data[i] / total; // normalised histogram
    }

    /* Calculate the first, second, and third order moments */
    double m1 = 0.0;
    double m2 = 0.0;
    double m3 = 0.0;
    for (int i = 0; i < data.length; i++) {
      m1 += i * histo[i];
      m2 += i * i * histo[i];
      m3 += i * i * i * histo[i];
    }
    /*
     * First 4 moments of the gray-level image should match the first 4 moments of the target binary
     * image. This leads to 4 equalities whose solutions are given in the Appendix of Ref. 1
     */
    final double m0 = 1.0;
    final double cd = m0 * m2 - m1 * m1;
    final double c0 = (-m2 * m2 + m1 * m3) / cd;
    final double c1 = (m0 * -m3 + m2 * m1) / cd;
    final double z0 = 0.5 * (-c1 - Math.sqrt(c1 * c1 - 4.0 * c0));
    final double z1 = 0.5 * (-c1 + Math.sqrt(c1 * c1 - 4.0 * c0));
    final double p0 =
        (z1 - m1) / (z1 - z0); /* Fraction of the object pixels in the target binary image */

    // The threshold is the gray-level closest
    // to the p0-tile of the normalized histogram
    double sum = 0;
    int threshold = -1;
    for (int i = 0; i < data.length; i++) {
      sum += histo[i];
      if (sum > p0) {
        threshold = i;
        break;
      }
    }
    return threshold;
  }

  /**
   * Otsu's threshold algorithm. C++ code by Jordan Bevik &lt;Jordan.Bevic@qtiworld.com&gt; ported
   * to ImageJ plugin by G.Landini.
   *
   * @param data the data
   * @return the threshold (or -1 if not found)
   */
  public static int otsu(int[] data) {
    final int length = data.length;

    // Initialize values:
    // The total intensity of the image
    long sum = 0;
    // np = total number of points
    long np = 0;
    double ssx = 0;
    for (int k = 0; k < length; k++) {
      final long value = data[k];
      ssx += value * k * k;
      sum += value * k; // Total histogram intensity
      np += value; // Total number of data points
    }

    if (np == 0) {
      return -1;
    }

    // k = the current threshold
    // kstar = optimal threshold
    int kstar = -1;
    // The count of the number of optimal thresholds
    int kstarCount = 0;
    // The maximum Between Class Variance
    double bcvMax = 0;
    // The total intensity for all histogram points <=k
    long sumK = 0;
    // n1 = # points with intensity <=k.
    long n1 = 0;

    // Look at each possible threshold value,
    // calculate the between-class variance, and decide if it's a max
    // No need to check endpoint k = L-1 as denom = 0
    for (int k = 0; k < length - 1; k++) {
      final long value = data[k];
      sumK += value * k;
      n1 += value;

      // The float casting here is to avoid compiler warning about loss of precision and
      // will prevent overflow in the case of large saturated images.
      // Maximum value of denom is (N^2)/4 = approx. 3E10
      final double denom = (double) (n1) * (np - n1);

      // The current Between Class Variance and maximum BCV
      double bcv;
      if (denom == 0) {
        bcv = 0;
      } else {
        // Float here is to avoid loss of precision when dividing.
        // Maximum value of num = 255*N = approx 8E7
        final double num = ((double) n1 / np) * sum - sumK;
        bcv = (num * num) / denom;
      }

      // Added for debugging.
      // Gonzalex & Woods, Digital Image Processing, 3rd Ed. pp 746:
      // "If a maximum exists for more than one threshold it is customary to average them"
      if (bcv > bcvMax) { // Assign the best threshold found so far
        bcvMax = bcv;
        kstar = k;
        kstarCount = 1;
      } else if (bcv == bcvMax) {
        // Total the thresholds for averaging
        kstar += k;
        kstarCount++;
      }
    }

    if (kstarCount > 1) {
      logger.finer("Otsu method has multiple optimal thresholds");
      kstar /= kstarCount;
    }

    // Output the measure of separability. Requires BCVmax / BCVglobal
    if (logger.isLoggable(Level.FINER)) {
      // Calculate global variance
      final double sx = sum;
      final double bcv = (ssx - sx * sx / np) / np;

      final int kstarCopy = kstar;
      final double ratio = (bcvMax / bcv);
      logger.finer(() -> String.format("Otsu separability @ %d: %f", kstarCopy, ratio));
    }

    // kstar += 1; // Use QTI convention that intensity -> 1 if intensity >= k
    // (the algorithm was developed for I-> 1 if I <= k.)
    return kstar;
  }

  /**
   * W. Doyle, "Operation useful for similarity-invariant pattern recognition," Journal of the
   * Association for Computing Machinery, vol. 9,pp. 259-267, 1962. Ported to ImageJ plugin by
   * G.Landini from Antti Niemisto's Matlab code (GPL). Original Matlab code Copyright (C) 2004
   * Antti Niemisto. See http://www.cs.tut.fi/~ant/histthresh/ for an excellent slide presentation
   * and the original Matlab code.
   *
   * @param data the data
   * @return the threshold (or -1 if not found)
   */
  public static int percentile(int[] data) {
    final double total = partialSum(data, data.length - 1);
    if (total == 0) {
      return 0;
    }
    int threshold = -1;
    final double ptile = 0.5; // default fraction of foreground pixels
    double temp = 1.0;
    double sum = 0;
    for (int i = 0; i < data.length; i++) {

      sum += data[i];
      // Fraction of the data
      final double f = sum / total;

      // Get closest fraction to the target fraction
      if (f < ptile) {
        final double distance = ptile - f;
        // Always closer to 50% assuming data[i] >= 0
        temp = distance;
        threshold = i;
      } else {
        final double distance = f - ptile;
        if (distance < temp) {
          threshold = i;
        }
        // No point continuing as this is just moving away from the target
        break;
      }
    }
    return threshold;
  }

  /**
   * Partial sum.
   *
   * @param data the data
   * @param size the size
   * @return the double
   */
  protected static double partialSum(int[] data, int size) {
    double sum = 0;
    for (int i = 0; i <= size; i++) {
      sum += data[i];
    }
    return sum;
  }

  /**
   * Kapur J.N., Sahoo P.K., and Wong A.K.C. (1985) "A New Method for Gray-Level Picture
   * Thresholding Using the Entropy of the Histogram" Graphical Models and Image Processing, 29(3):
   * 273-285. Ported to ImageJ plugin by G.Landini from E Celebi's fourier_0.8 routines.
   *
   * @param data the data
   * @return the threshold (or -1 if not found)
   */
  public static int renyiEntropy(int[] data) {

    int total = 0;
    for (int ih = 0; ih < data.length; ih++) {
      total += data[ih];
    }

    if (total == 0) {
      return 0;
    }

    /* normalized histogram */
    final double[] normHisto = new double[data.length];
    for (int ih = 0; ih < data.length; ih++) {
      normHisto[ih] = (double) data[ih] / total;
    }

    /* cumulative normalized histogram */
    final double[] p1 = new double[data.length];
    final double[] p2 = new double[data.length];
    p1[0] = normHisto[0];
    p2[0] = 1.0 - p1[0];
    for (int ih = 1; ih < data.length; ih++) {
      p1[ih] = p1[ih - 1] + normHisto[ih];
      p2[ih] = 1.0 - p1[ih];
    }

    /* Determine the first non-zero bin */
    int firstBin = 0;
    for (int ih = 0; ih < data.length; ih++) {
      if (Math.abs(p1[ih]) >= 2.220446049250313E-16) {
        firstBin = ih;
        break;
      }
    }

    /* Determine the last non-zero bin */
    int lastBin = data.length - 1;
    for (int ih = data.length - 1; ih >= firstBin; ih--) {
      if (Math.abs(p2[ih]) >= 2.220446049250313E-16) {
        lastBin = ih;
        break;
      }
    }

    /* Maximum Entropy Thresholding - BEGIN */
    /* ALPHA = 1.0 */
    /*
     * Calculate the total entropy each gray-level and find the threshold that maximizes it
     */
    // was MIN_INT in original code, but if an empty image is processed it gives an
    // error later on.
    int threshold = 0;
    /* max entropy */
    double maxEnt = 0.0;

    for (int it = firstBin; it <= lastBin; it++) {
      /* Entropy of the background pixels */
      double entBack = 0.0;
      for (int ih = 0; ih <= it; ih++) {
        if (data[ih] != 0) {
          entBack -= (normHisto[ih] / p1[it]) * Math.log(normHisto[ih] / p1[it]);
        }
      }

      /* Entropy of the object pixels */
      double entObj = 0.0;
      for (int ih = it + 1; ih < data.length; ih++) {
        if (data[ih] != 0) {
          entObj -= (normHisto[ih] / p2[it]) * Math.log(normHisto[ih] / p2[it]);
        }
      }

      /* Total entropy */
      final double totEnt = entBack + entObj;

      if (maxEnt < totEnt) {
        maxEnt = totEnt;
        threshold = it;
      }
    }
    final int fixedTstar2 = threshold;

    /* Maximum Entropy Thresholding - END */
    // was MIN_INT in original code, but if an empty image is processed it gives an
    // error later on.
    threshold = 0;
    maxEnt = 0.0;
    /* alpha parameter of the method */
    double alpha = 0.5;
    double term = 1.0 / (1.0 - alpha);
    for (int it = firstBin; it <= lastBin; it++) {
      /* Entropy of the background pixels */
      double entBack = 0.0;
      for (int ih = 0; ih <= it; ih++) {
        entBack += Math.sqrt(normHisto[ih] / p1[it]);
      }

      /* Entropy of the object pixels */
      double entObj = 0.0;
      for (int ih = it + 1; ih < data.length; ih++) {
        entObj += Math.sqrt(normHisto[ih] / p2[it]);
      }

      /* Total entropy */
      final double totEnt = term * ((entBack * entObj) > 0.0 ? Math.log(entBack * entObj) : 0.0);

      if (totEnt > maxEnt) {
        maxEnt = totEnt;
        threshold = it;
      }
    }

    final int fixedTstar1 = threshold;

    // was MIN_INT in original code, but if an empty image is processed it gives an
    // error later on.
    threshold = 0;
    maxEnt = 0.0;
    alpha = 2.0;
    term = 1.0 / (1.0 - alpha);
    for (int it = firstBin; it <= lastBin; it++) {
      /* Entropy of the background pixels */
      double entBack = 0.0;
      for (int ih = 0; ih <= it; ih++) {
        entBack += (normHisto[ih] * normHisto[ih]) / (p1[it] * p1[it]);
      }

      /* Entropy of the object pixels */
      double entObj = 0.0;
      for (int ih = it + 1; ih < data.length; ih++) {
        entObj += (normHisto[ih] * normHisto[ih]) / (p2[it] * p2[it]);
      }

      /* Total entropy */
      final double totEnt = term * ((entBack * entObj) > 0.0 ? Math.log(entBack * entObj) : 0.0);

      if (totEnt > maxEnt) {
        maxEnt = totEnt;
        threshold = it;
      }
    }

    int tstar1 = fixedTstar1;
    int tstar2 = fixedTstar2;
    int tstar3 = threshold;

    /* Sort t_star values */
    if (tstar2 < tstar1) {
      final int tmpVar = tstar1;
      tstar1 = tstar2;
      tstar2 = tmpVar;
    }
    if (tstar3 < tstar2) {
      final int tmpVar = tstar2;
      tstar2 = tstar3;
      tstar3 = tmpVar;
    }
    if (tstar2 < tstar1) {
      final int tmpVar = tstar1;
      tstar1 = tstar2;
      tstar2 = tmpVar;
    }

    /* Adjust beta values */
    int beta1;
    int beta2;
    int beta3;
    if (Math.abs(tstar1 - tstar2) <= 5) {
      if (Math.abs(tstar2 - tstar3) <= 5) {
        beta1 = 1;
        beta2 = 2;
        beta3 = 1;
      } else {
        beta1 = 0;
        beta2 = 1;
        beta3 = 3;
      }
    } else if (Math.abs(tstar2 - tstar3) <= 5) {
      beta1 = 3;
      beta2 = 1;
      beta3 = 0;
    } else {
      beta1 = 1;
      beta2 = 2;
      beta3 = 1;
    }
    /* Determine the optimal threshold value */
    final double omega = p1[tstar3] - p1[tstar1];
    return (int) (tstar1 * (p1[tstar1] + 0.25 * omega * beta1) + 0.25 * tstar2 * omega * beta2
        + tstar3 * (p2[tstar3] + 0.25 * omega * beta3));
  }

  /**
   * Shanhbag A.G. (1994) "Utilization of Information Measure as a Means of Image Thresholding"
   * Graphical Models and Image Processing, 56(5): 414-419. Ported to ImageJ plugin by G.Landini
   * from E Celebi's fourier_0.8 routines.
   *
   * @param data the data
   * @return the threshold (or -1 if not found)
   */
  public static int shanbhag(int[] data) {

    int total = 0;
    for (int ih = 0; ih < data.length; ih++) {
      total += data[ih];
    }
    if (total == 0) {
      return 0;
    }

    /* normalized histogram */
    final double[] normHisto = new double[data.length];
    for (int ih = 0; ih < data.length; ih++) {
      normHisto[ih] = (double) data[ih] / total;
    }

    /* cumulative normalized histogram */
    final double[] p1 = new double[data.length];
    final double[] p2 = new double[data.length];
    p1[0] = normHisto[0];
    p2[0] = 1.0 - p1[0];
    for (int ih = 1; ih < data.length; ih++) {
      p1[ih] = p1[ih - 1] + normHisto[ih];
      p2[ih] = 1.0 - p1[ih];
    }

    /* Determine the first non-zero bin */
    int firstBin = 0;
    for (int ih = 0; ih < data.length; ih++) {
      if (Math.abs(p1[ih]) >= 2.220446049250313E-16) {
        firstBin = ih;
        break;
      }
    }

    /* Determine the last non-zero bin */
    int lastBin = data.length - 1;
    for (int ih = data.length - 1; ih >= firstBin; ih--) {
      if (Math.abs(p2[ih]) >= 2.220446049250313E-16) {
        lastBin = ih;
        break;
      }
    }

    // Calculate the total entropy each gray-level
    // and find the threshold that maximizes it
    int threshold = -1;
    /* min entropy */
    double minEnt = Double.MAX_VALUE;

    for (int it = firstBin; it <= lastBin; it++) {
      /* Entropy of the background pixels */
      double entBack = 0.0;
      double term = 0.5 / p1[it];
      for (int ih = 1; ih <= it; ih++) {
        entBack -= normHisto[ih] * Math.log(1.0 - term * p1[ih - 1]);
      }
      entBack *= term;

      /* Entropy of the object pixels */
      double entObj = 0.0;
      term = 0.5 / p2[it];
      for (int ih = it + 1; ih < data.length; ih++) {
        entObj -= normHisto[ih] * Math.log(1.0 - term * p2[ih]);
      }
      entObj *= term;

      /* Total entropy */
      final double totEnt = Math.abs(entBack - entObj);

      if (totEnt < minEnt) {
        minEnt = totEnt;
        threshold = it;
      }
    }
    return threshold;
  }

  /**
   * Zack, G. W., Rogers, W. E. and Latt, S. A., 1977, Automatic Measurement of Sister Chromatid
   * Exchange Frequency, Journal of Histochemistry and Cytochemistry 25 (7), pp. 741-753.
   *
   * <p>Modified from Johannes Schindelin plugin.
   *
   * @param data the data
   * @return the threshold (or -1 if not found)
   */
  public static int triangle(int[] data) {
    // find min and max
    int min = 0;
    for (int i = 0; i < data.length; i++) {
      if (data[i] > 0) {
        min = i;
        break;
      }
    }
    if (min > 0) {
      // line to the (p==0) point, not to data[min]
      min--;
    }

    // The Triangle algorithm cannot tell whether the data is skewed to one side or another.
    // This causes a problem as there are 2 possible thresholds between the max and the 2 extremes
    // of the histogram.
    // Here I propose to find out to which side of the max point the data is furthest, and use that
    // as
    // the other extreme.
    int min2 = 0;
    for (int i = data.length - 1; i > 0; i--) {
      if (data[i] > 0) {
        min2 = i;
        break;
      }
    }
    if (min2 < data.length - 1) {
      min2++; // line to the (p==0) point, not to data[min]
    }

    int dmax = 0;
    int max = 0;
    for (int i = 0; i < data.length; i++) {
      if (data[i] > dmax) {
        max = i;
        dmax = data[i];
      }
    }
    // find which is the furthest side
    boolean inverted = false;
    if ((max - min) < (min2 - max)) {
      // reverse the histogram
      inverted = true;
      int left = 0; // index of leftmost element
      int right = data.length - 1; // index of rightmost element
      while (left < right) {
        // exchange the left and right elements
        final int temp = data[left];
        data[left] = data[right];
        data[right] = temp;
        // move the bounds toward the center
        left++;
        right--;
      }
      min = data.length - 1 - min2;
      max = data.length - 1 - max;
    }

    if (min == max) {
      return min;
    }

    // describe line by nx * x + ny * y - d = 0
    // nx is just the max frequency as the other point has freq=0
    double nx = data[max];
    double ny = (double) min - max;
    double distance = Math.sqrt(nx * nx + ny * ny);
    nx /= distance;
    ny /= distance;
    distance = nx * min + ny * data[min];

    // find split point
    int split = min;
    double splitDistance = 0;
    for (int i = min + 1; i <= max; i++) {
      final double newDistance = nx * i + ny * data[i] - distance;
      if (newDistance > splitDistance) {
        split = i;
        splitDistance = newDistance;
      }
    }
    split--;

    if (inverted) {
      // The histogram might be used for something else, so let's reverse it back
      int left = 0;
      int right = data.length - 1;
      while (left < right) {
        final int temp = data[left];
        data[left] = data[right];
        data[right] = temp;
        left++;
        right--;
      }
      return (data.length - 1 - split);
    }
    return split;
  }

  /**
   * Implements Yen thresholding method. 1) Yen J.C., Chang F.J., and Chang S. (1995) "A New
   * Criterion for Automatic Multilevel Thresholding" IEEE Trans. on Image Processing, 4(3):
   * 370-378. 2) Sezgin M. and Sankur B. (2004) "Survey over Image Thresholding Techniques and
   * Quantitative Performance Evaluation" Journal of Electronic Imaging, 13(1): 146-165.
   * http://citeseer.ist.psu.edu/sezgin04survey.html
   *
   * <p>Ported to ImageJ plugin by G.Landini from E Celebi's fourier_0.8 routines.
   *
   * @param data the data
   * @return the threshold (or -1 if not found)
   */
  public static int yen(int[] data) {
    final double[] normHisto = new double[data.length]; /* normalized histogram */
    final double[] p1 = new double[data.length]; /* cumulative normalized histogram */
    final double[] p1Sq = new double[data.length];
    final double[] p2Sq = new double[data.length];

    int total = 0;
    for (int ih = 0; ih < data.length; ih++) {
      total += data[ih];
    }

    for (int ih = 0; ih < data.length; ih++) {
      normHisto[ih] = (double) data[ih] / total;
    }

    p1[0] = normHisto[0];
    for (int ih = 1; ih < data.length; ih++) {
      p1[ih] = p1[ih - 1] + normHisto[ih];
    }

    p1Sq[0] = normHisto[0] * normHisto[0];
    for (int ih = 1; ih < data.length; ih++) {
      p1Sq[ih] = p1Sq[ih - 1] + normHisto[ih] * normHisto[ih];
    }

    p2Sq[data.length - 1] = 0.0;
    for (int ih = data.length - 2; ih >= 0; ih--) {
      p2Sq[ih] = p2Sq[ih + 1] + normHisto[ih + 1] * normHisto[ih + 1];
    }

    /* Find the threshold that maximizes the criterion */
    int threshold = -1;
    double maxCrit = Double.MIN_VALUE;
    for (int it = 0; it < data.length; it++) {
      final double crit = 2 * logOrZero(p1[it] * (1.0 - p1[it])) - logOrZero(p1Sq[it] * p2Sq[it]);
      if (crit > maxCrit) {
        maxCrit = crit;
        threshold = it;
      }
    }
    return threshold;
  }

  /**
   * Return the log of the value or zero if it is not positive.
   *
   * @param value the value
   * @return log(value) or zero
   */
  private static double logOrZero(double value) {
    return value > 0.0 ? Math.log(value) : 0.0;
  }

  /**
   * Gets the threshold.
   *
   * <p>This method is faster than calling the required static thresholding method as the histogram
   * is cropped to the min-max range before processing.
   *
   * <p>Note: Unlike the individual threshold methods this method will not return -1 if not found.
   * This ensures the index is within the range of the histogram.
   *
   * @param methodName the method name
   * @param data the data
   * @return the threshold (may be 0 if not found)
   * @see #getThreshold(Method, int[])
   */
  public static int getThreshold(String methodName, int[] data) {
    return getThreshold(getMethod(methodName), data);
  }

  /**
   * Gets the threshold.
   *
   * <p>This method is faster than calling the required static thresholding method as the histogram
   * is cropped to the min-max range before processing.
   *
   * <p>Note: Unlike the individual threshold methods this method will not return -1 if not found.
   * This ensures the index is within the range of the histogram.
   *
   * @param method the method
   * @param data the data
   * @return the threshold (may be 0 if not found)
   */
  public static int getThreshold(Method method, int[] data) {
    if (method == Method.NONE || data == null || data.length == 0) {
      return 0;
    }

    // bracket the histogram to the range that holds data to make it quicker
    int minbin = 0;
    int maxbin = data.length - 1;
    while (minbin <= maxbin && data[minbin] == 0) {
      minbin++;
    }
    if (minbin > maxbin) {
      // No data
      return 0;
    }
    while (data[maxbin] == 0) {
      maxbin--;
    }

    final int size = (maxbin - minbin) + 1;
    int[] data2;
    if (size < data.length) {
      data2 = new int[size];
      for (int i = minbin, j = 0; i <= maxbin; i++, j++) {
        data2[j] = data[i];
      }
    } else {
      data2 = data;
    }

    // Apply the selected algorithm
    int threshold = run(method, data2);

    // Check if any thresholding was performed
    if (threshold < 0) {
      return 0;
    }

    threshold += minbin; // add the offset of the histogram

    return threshold;
  }

  private static int run(Method method, int[] data) {
    switch (method) {
      //@formatter:off
      case OTSU: return otsu(data);
      case DEFAULT: return ijDefault(data);
      case HUANG: return huang(data);
      case INTERMODES: return intermodes(data);
      case ISO_DATA: return isoData(data);
      case LI: return li(data);
      case MAX_ENTROPY: return maxEntropy(data);
      case MEAN: return mean(data);
      case MEAN_PLUS_STD_DEV: return meanPlusStdDev(data);
      case MIN_ERROR_I: return minErrorI(data);
      case MINIMUM: return minimum(data);
      case MOMENTS: return moments(data);
      case PERCENTILE: return percentile(data);
      case RENYI_ENTROPY: return renyiEntropy(data);
      case SHANBHAG: return shanbhag(data);
      case TRIANGLE: return triangle(data);
      case YEN: return yen(data);
      case NONE:
      default: return -1;
      //@formatter:on
    }
  }
}
