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
package uk.ac.sussex.gdsc.core.threshold;

import uk.ac.sussex.gdsc.core.utils.TextUtils;

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
// 1.5 2009/Apr/12 Mean method, MinimumErrorIterative method , enahanced Triangle
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
// 1.14 2011/Apr/14 IsoData issues a warning if threhsold not found

/**
 * Provides thresholding methods based on a histogram. <p> The methods have been extracted from the
 * Auto_Threshold ImageJ plugin of G. Landini.
 */
public class AutoThreshold {
  /**
   * The auto-threshold method
   */
  public enum Method {
    //@formatter:off
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
		//@formatter:on

    /** The name. */
    public final String name;

    private Method(String name) {
      this.name = name;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
      return name;
    }
  }

  /** Flag to allow extra logging information to be output from thresholding methods. */
  public static boolean isDebug = false;

  /** Flag to log information from the thresholding methods. */
  public static boolean isLogging = false;

  private final static String[] methods;
  private final static String[] methods2;

  static {
    final Method[] m = Method.values();
    methods = new String[m.length];
    methods2 = new String[m.length - 1];
    for (int i = 0; i < m.length; i++) {
      methods[i] = m[i].name;
      if (i != 0) {
        methods2[i - 1] = m[i].name;
      }
    }
  }

  /**
   * Gets the methods.
   *
   * @return the methods
   */
  public static String[] getMethods() {
    return methods.clone();
  }

  /**
   * Gets the methods.
   *
   * @param ignoreNone Set to true to remove the {@link Method#NONE} option
   * @return the methods
   */
  public static String[] getMethods(boolean ignoreNone) {
    if (ignoreNone) {
      return methods2.clone();
    }
    return methods.clone();
  }

  /**
   * Gets the method from method name.
   *
   * @param name the name
   * @return the method
   */
  public static Method getMethod(String name) {
    for (int i = 0; i < methods.length; i++) {
      if (methods[i].equals(name)) {
        return Method.values()[i];
      }
    }
    return Method.NONE;
  }

  /**
   * Gets the method from the index of the method names.
   *
   * @param i the index
   * @param ignoreNone Set to true to remove the {@link Method#NONE} option
   * @return the method
   */
  public static Method getMethod(int i, boolean ignoreNone) {
    final Method[] m = Method.values();
    if (ignoreNone) {
      i++;
    }
    if (i >= 0 && i < m.length) {
      return m[i];
    }
    return Method.NONE;
  }

  /**
   * Original IJ implementation for compatibility.
   *
   * @param data the data
   * @return the threshold
   */
  public static int IJDefault(int[] data) {
    int level;
    final int maxValue = data.length - 1;
    double result, sum1, sum2, sum3, sum4;

    int min = 0;
    while ((data[min] == 0) && (min < maxValue)) {
      min++;
    }
    int max = maxValue;
    while ((data[max] == 0) && (max > 0)) {
      max--;
    }
    if (min >= max) {
      level = data.length / 2;
      return level;
    }

    int movingIndex = min;
    do {
      sum1 = sum2 = sum3 = sum4 = 0.0;
      for (int i = min; i <= movingIndex; i++) {
        sum1 += i * data[i];
        sum2 += data[i];
      }
      for (int i = (movingIndex + 1); i <= max; i++) {
        sum3 += i * data[i];
        sum4 += data[i];
      }
      result = (sum1 / sum2 + sum3 / sum4) / 2.0;
      movingIndex++;
    } while ((movingIndex + 1) <= result && movingIndex < max - 1);

    level = (int) Math.round(result);
    return level;
  }

  /**
   * Implements Huang's fuzzy thresholding method. Uses Shannon's entropy function (one can also use
   * Yager's entropy function) Huang L.-K. and Wang M.-J.J. (1995) "Image Thresholding by Minimizing
   * the Measures of Fuzziness" Pattern Recognition, 28(1): 41-51. Reimplemented (to handle 16-bit
   * efficiently) by Johannes Schindelin Jan 31, 2011
   *
   * @param data the data
   * @return the threshold
   */
  public static int Huang(int[] data) {
    // find first and last non-empty bin
    int first, last;
    for (first = 0; first < data.length && data[first] == 0; first++) {
      // do nothing
    }
    for (last = data.length - 1; last > first && data[last] == 0; last--) {
      // do nothing
    }
    if (first == last) {
      return 0;
    }

    // calculate the cumulative density and the weighted cumulative density
    final double[] S = new double[last + 1], W = new double[last + 1];
    S[0] = data[0];
    for (int i = Math.max(1, first); i <= last; i++) {
      S[i] = S[i - 1] + data[i];
      W[i] = W[i - 1] + i * data[i];
    }

    // precalculate the summands of the entropy given the absolute difference x - mu (integral)
    final double C = last - first;
    final double[] Smu = new double[last + 1 - first];
    for (int i = 1; i < Smu.length; i++) {
      final double mu = 1 / (1 + Math.abs(i) / C);
      Smu[i] = -mu * Math.log(mu) - (1 - mu) * Math.log(1 - mu);
    }

    // calculate the threshold
    int bestThreshold = 0;
    double bestEntropy = Double.MAX_VALUE;
    for (int threshold = first; threshold <= last; threshold++) {
      double entropy = 0;
      int mu = (int) Math.round(W[threshold] / S[threshold]);
      for (int i = first; i <= threshold; i++) {
        entropy += Smu[Math.abs(i - mu)] * data[i];
      }
      mu = (int) Math.round((W[last] - W[threshold]) / (S[last] - S[threshold]));
      for (int i = threshold + 1; i <= last; i++) {
        entropy += Smu[Math.abs(i - mu)] * data[i];
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
   * @param y the y
   * @return true, if successful
   */
  protected static boolean bimodalTest(double[] y) {
    final int len = y.length;
    boolean b = false;
    int modes = 0;

    for (int k = 1; k < len - 1; k++) {
      if (y[k - 1] < y[k] && y[k + 1] < y[k]) {
        modes++;
        if (modes > 2) {
          return false;
        }
      }
    }
    if (modes == 2) {
      b = true;
    }
    return b;
  }

  /**
   * J. M. S. Prewitt and M. L. Mendelsohn, "The analysis of cell images," in Annals of the New York
   * Academy of Sciences, vol. 128, pp. 1035-1053, 1966. ported to ImageJ plugin by G.Landini from
   * Antti Niemisto's Matlab code (GPL) Original Matlab code Copyright (C) 2004 Antti Niemisto. See
   * http://www.cs.tut.fi/~ant/histthresh/ for an excellent slide presentation and the original
   * Matlab code.
   *
   * Assumes a bimodal histogram. The histogram needs is smoothed (using a running average of size
   * 3, iteratively) until there are only two local maxima. j and k. Threshold t is (j+k)/2. Images
   * with histograms having extremely unequal peaks or a broad and ﬂat valley are unsuitable for
   * this method.
   *
   * @param data the data
   * @return the threshold
   */
  public static int Intermodes(int[] data) {
    final double[] iHisto = new double[data.length];
    int iter = 0;
    int threshold = -1;
    for (int i = 0; i < data.length; i++) {
      iHisto[i] = data[i];
    }

    while (!bimodalTest(iHisto)) {
      // smooth with a 3 point running mean filter
      double previous = 0, current = 0, next = iHisto[0];
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
        log("Intermodes Threshold not found after 10000 iterations.");
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
    // log("mode:" +i);
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
   * From: Tim Morris (dtm@ap.co.umist.ac.uk) Subject: Re: Thresholding method? posted to
   * sci.image.processing on 1996/06/24 The algorithm implemented in NIH Image sets the threshold as
   * that grey value, G, for which the average of the averages of the grey values below and above G
   * is equal to G. It does this by initialising G to the lowest sensible value and iterating:
   *
   * @param data the data
   * @return the threshold
   */
  public static int IsoData(int[] data) {
    // L = the average grey value of pixels with intensities < G
    // H = the average grey value of pixels with intensities > G
    // is G = (L + H)/2?
    // yes => exit
    // no => increment G and repeat
    //
    // There is a discrepancy with IJ because they are slightly different methods
    int i, l, toth, totl, h, g = 0;
    for (i = 1; i < data.length; i++) {
      if (data[i] > 0) {
        g = i + 1;
        break;
      }
    }
    while (true) {
      l = 0;
      totl = 0;
      for (i = 0; i < g; i++) {
        totl = totl + data[i];
        l = l + (data[i] * i);
      }
      h = 0;
      toth = 0;
      for (i = g + 1; i < data.length; i++) {
        toth += data[i];
        h += (data[i] * i);
      }
      if (totl > 0 && toth > 0) {
        l /= totl;
        h /= toth;
        if (g == (int) Math.round((l + h) / 2.0)) {
          break;
        }
      }
      g++;
      if (g > data.length - 2) {
        log("IsoData Threshold not found.");
        return -1;
      }
    }
    return g;
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
   * @return the threshold
   */
  public static int Li(int[] data) {
    // Ported to ImageJ plugin by G.Landini from E Celebi's fourier_0.8 routines.
    int threshold;
    int ih;
    int num_pixels;
    int sum_back; /* sum of the background pixels at a given threshold */
    int sum_obj; /* sum of the object pixels at a given threshold */
    int num_back; /* number of background pixels at a given threshold */
    int num_obj; /* number of object pixels at a given threshold */
    double old_thresh;
    double new_thresh;
    double mean_back; /* mean of the background pixels at a given threshold */
    double mean_obj; /* mean of the object pixels at a given threshold */
    double mean; /* mean gray-level in the image */
    double tolerance; /* threshold tolerance */
    double temp;

    tolerance = 0.5;
    num_pixels = 0;
    for (ih = 0; ih < data.length; ih++) {
      num_pixels += data[ih];
    }

    /* Calculate the mean gray-level */
    mean = 0.0;
    for (ih = 0; ih < data.length; ih++) {
      // 0 + 1?
      mean += ih * data[ih];
    }
    mean /= num_pixels;
    /* Initial estimate */
    new_thresh = mean;

    do {
      old_thresh = new_thresh;
      threshold = (int) (old_thresh + 0.5); /* range */
      /* Calculate the means of background and object pixels */
      /* Background */
      sum_back = 0;
      num_back = 0;
      for (ih = 0; ih <= threshold; ih++) {
        sum_back += ih * data[ih];
        num_back += data[ih];
      }
      mean_back = (num_back == 0 ? 0.0 : (sum_back / (double) num_back));
      /* Object */
      sum_obj = 0;
      num_obj = 0;
      for (ih = threshold + 1; ih < data.length; ih++) {
        sum_obj += ih * data[ih];
        num_obj += data[ih];
      }
      mean_obj = (num_obj == 0 ? 0.0 : (sum_obj / (double) num_obj));

      /* Calculate the new threshold: Equation (7) in Ref. 2 */
      // new_thresh = simple_round ( ( mean_back - mean_obj ) / ( Math.log ( mean_back ) - Math.log
      // ( mean_obj ) ) );
      // simple_round ( double x ) {
      // return ( int ) ( IS_NEG ( x ) ? x - .5 : x + .5 );
      // }
      //
      // #define IS_NEG( x ) ( ( x ) < -DBL_EPSILON )
      // DBL_EPSILON = 2.220446049250313E-16
      temp = (mean_back - mean_obj) / (Math.log(mean_back) - Math.log(mean_obj));

      if (temp < -2.220446049250313E-16) {
        new_thresh = (int) (temp - 0.5);
      } else {
        new_thresh = (int) (temp + 0.5);
        /*
         * Stop the iterations when the difference between the new and old threshold values is less
         * than the tolerance
         */
      }
    } while (Math.abs(new_thresh - old_thresh) > tolerance);
    return threshold;
  }

  /**
   * Implements Kapur-Sahoo-Wong (Maximum Entropy) thresholding method Kapur J.N., Sahoo P.K., and
   * Wong A.K.C. (1985) "A New Method for Gray-Level Picture Thresholding Using the Entropy of the
   * Histogram" Graphical Models and Image Processing, 29(3): 273-285.
   *
   * @param data the data
   * @return the threshold
   */
  public static int MaxEntropy(int[] data) {
    // M. Emre Celebi
    // 06.15.2007
    // Ported to ImageJ plugin by G.Landini from E Celebi's fourier_0.8 routines
    int threshold = -1;
    int ih, it;
    int first_bin;
    int last_bin;
    double tot_ent; /* total entropy */
    double max_ent; /* max entropy */
    double ent_back; /* entropy of the background pixels at a given threshold */
    double ent_obj; /* entropy of the object pixels at a given threshold */
    final double[] norm_histo = new double[data.length]; /* normalized histogram */
    final double[] P1 = new double[data.length]; /* cumulative normalized histogram */
    final double[] P2 = new double[data.length];

    int total = 0;
    for (ih = 0; ih < data.length; ih++) {
      total += data[ih];
    }

    for (ih = 0; ih < data.length; ih++) {
      norm_histo[ih] = (double) data[ih] / total;
    }

    P1[0] = norm_histo[0];
    P2[0] = 1.0 - P1[0];
    for (ih = 1; ih < data.length; ih++) {
      P1[ih] = P1[ih - 1] + norm_histo[ih];
      P2[ih] = 1.0 - P1[ih];
    }

    /* Determine the first non-zero bin */
    first_bin = 0;
    for (ih = 0; ih < data.length; ih++) {
      if (!(Math.abs(P1[ih]) < 2.220446049250313E-16)) {
        first_bin = ih;
        break;
      }
    }

    /* Determine the last non-zero bin */
    last_bin = data.length - 1;
    for (ih = data.length - 1; ih >= first_bin; ih--) {
      if (!(Math.abs(P2[ih]) < 2.220446049250313E-16)) {
        last_bin = ih;
        break;
      }
    }

    // Calculate the total entropy each gray-level
    // and find the threshold that maximizes it
    max_ent = Double.MIN_VALUE;

    for (it = first_bin; it <= last_bin; it++) {
      /* Entropy of the background pixels */
      ent_back = 0.0;
      for (ih = 0; ih <= it; ih++) {
        if (data[ih] != 0) {
          ent_back -= (norm_histo[ih] / P1[it]) * Math.log(norm_histo[ih] / P1[it]);
        }
      }

      /* Entropy of the object pixels */
      ent_obj = 0.0;
      for (ih = it + 1; ih < data.length; ih++) {
        if (data[ih] != 0) {
          ent_obj -= (norm_histo[ih] / P2[it]) * Math.log(norm_histo[ih] / P2[it]);
        }
      }

      /* Total entropy */
      tot_ent = ent_back + ent_obj;

      // log(""+max_ent+" "+tot_ent);
      if (max_ent < tot_ent) {
        max_ent = tot_ent;
        threshold = it;
      }
    }
    return threshold;
  }

  /**
   * C. A. Glasbey, "An analysis of histogram-based thresholding algorithms," CVGIP: Graphical
   * Models and Image Processing, vol. 55, pp. 532-537, 1993.
   *
   * The threshold is the mean of the greyscale data.
   *
   * @param data the data
   * @return the threshold
   */
  public static int Mean(int[] data) {
    int threshold = -1;
    double tot = 0, sum = 0;
    for (int i = 0; i < data.length; i++) {
      tot += data[i];
      sum += (i * data[i]);
    }
    threshold = (int) Math.floor(sum / tot);
    return threshold;
  }

  /**
   * The multiplier used within the MeanPlusStdDev calculation
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
   * Get the mean plus a multiplier of the standard deviation <p> This is not threadsafe as the
   * standard deviation multiplier is static. Use the instance method for thread safety
   * ({@link #MeanPlusStdDev(int[], double)}) if you are changing the multiplier in different
   * threads.
   *
   * @param data The histogram data
   * @return The threshold
   */
  public static int MeanPlusStdDev(int[] data) {
    final double m = AutoThreshold.stdDevMultiplier;
    return new AutoThreshold().MeanPlusStdDev(data, m);
  }

  /**
   * Mean plus std dev.
   *
   * @param data the data
   * @param stdDevMultiplier the std dev multiplier
   * @return the threshold
   */
  public int MeanPlusStdDev(int[] data, double stdDevMultiplier) {
    // The threshold is the mean of the greyscale data plus a multiplier of the image standard
    // deviation
    int threshold = -1;
    int count;
    double value;
    double tot = 0, sum = 0;
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

    final double mean = sum / tot;

    // Get the Std.Dev
    double stdDev;
    if (tot > 0) {
      final double d = tot;
      stdDev = (d * sum2 - sum * sum) / d;
      if (stdDev > 0.0) {
        stdDev = Math.sqrt(stdDev / (d - 1.0));
      } else {
        stdDev = 0.0;
      }
    } else {
      stdDev = 0.0;
    }

    threshold = (int) Math.floor(mean + stdDev * stdDevMultiplier);

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
   * @return the threshold
   */
  public static int MinErrorI(int[] data) {
    int threshold = Mean(data); // Initial estimate for the threshold is found with the MEAN
                                // algorithm.
    int Tprev = -2;
    double mu, nu, p, q, sigma2, tau2, w0, w1, w2, sqterm, temp;

    // Pre-compute
    final double[] A = new double[data.length];
    final double[] B = new double[data.length];
    final double[] C = new double[data.length];
    double a = 0, b = 0, c = 0;
    for (int i = 0; i < data.length; i++) {
      a += data[i];
      A[i] = a;
      b += i * data[i];
      B[i] = b;
      c += i * i * data[i];
      C[i] = c;
    }

    // int counter=1;
    final int end = data.length - 1;
    while (threshold != Tprev) {
      // Calculate some statistics.
      // mu = B(data, threshold) / A(data, threshold);
      // nu = (B(data, end) - B(data, threshold)) / (A(data, end) - A(data, threshold));
      // p = A(data, threshold) / A(data, end);
      // q = (A(data, end) - A(data, threshold)) / A(data, end);
      // sigma2 = C(data, threshold) / A(data, threshold) - (mu * mu);
      // tau2 = (C(data, end) - C(data, threshold)) / (A(data, end) - A(data, threshold)) -
      // (nu * nu);

      // With precomputed values
      mu = B[threshold] / A[threshold];
      nu = (B[end] - B[threshold]) / (A[end] - A[threshold]);
      p = A[threshold] / A[end];
      q = (A[end] - A[threshold]) / A[end];
      sigma2 = C[threshold] / A[threshold] - (mu * mu);
      tau2 = (C[end] - C[threshold]) / (A[end] - A[threshold]) - (nu * nu);

      // The terms of the quadratic equation to be solved.
      w0 = 1.0 / sigma2 - 1.0 / tau2;
      w1 = mu / sigma2 - nu / tau2;
      w2 = (mu * mu) / sigma2 - (nu * nu) / tau2
          + Math.log10((sigma2 * (q * q)) / (tau2 * (p * p)));

      // If the next threshold would be imaginary, return with the current one.
      sqterm = (w1 * w1) - w0 * w2;
      if (sqterm < 0) {
        log("MinError(I): not converging. Try \'Ignore black/white\' options");
        return threshold;
      }

      // The updated threshold is the integer part of the solution of the quadratic equation.
      Tprev = threshold;
      temp = (w1 + Math.sqrt(sqterm)) / w0;

      if (Double.isNaN(temp)) {
        log("MinError(I): NaN, not converging. Try \'Ignore black/white\' options");
        threshold = Tprev;
      } else {
        threshold = (int) Math.floor(temp);
        // log("Iter: "+ counter+++" t:"+threshold);
      }
    }
    return threshold;
  }

  /**
   * A.
   *
   * @param y the y
   * @param j the j
   * @return the double
   */
  protected static double A(int[] y, int j) {
    double x = 0;
    for (int i = 0; i <= j; i++) {
      x += y[i];
    }
    return x;
  }

  /**
   * B.
   *
   * @param y the y
   * @param j the j
   * @return the double
   */
  protected static double B(int[] y, int j) {
    double x = 0;
    for (int i = 0; i <= j; i++) {
      x += i * y[i];
    }
    return x;
  }

  /**
   * C.
   *
   * @param y the y
   * @param j the j
   * @return the double
   */
  protected static double C(int[] y, int j) {
    double x = 0;
    for (int i = 0; i <= j; i++) {
      x += i * i * y[i];
    }
    return x;
  }

  /**
   * J. M. S. Prewitt and M. L. Mendelsohn, "The analysis of cell images," in Annals of the New York
   * Academy of Sciences, vol. 128, pp. 1035-1053, 1966. ported to ImageJ plugin by G.Landini from
   * Antti Niemisto's Matlab code (GPL). Original Matlab code Copyright (C) 2004 Antti Niemisto. See
   * http://www.cs.tut.fi/~ant/histthresh/ for an excellent slide presentation and the original
   * Matlab code.
   *
   * @param data the data
   * @return the threshold
   */
  public static int Minimum(int[] data) {
    // Assumes a bimodal histogram. The histogram needs is smoothed (using a
    // running average of size 3, iteratively) until there are only two local maxima.
    // Threshold t is such that yt−1 > yt ≤ yt+1.
    // Images with histograms having extremely unequal peaks or a broad and
    // ﬂat valley are unsuitable for this method.
    int iter = 0;
    int threshold = -1;
    int max = -1;
    double[] iHisto = new double[data.length];

    for (int i = 0; i < data.length; i++) {
      iHisto[i] = data[i];
      if (data[i] > 0) {
        max = i;
      }
    }
    final double[] tHisto = iHisto;

    while (!bimodalTest(iHisto)) {
      // smooth with a 3 point running mean filter
      for (int i = 1; i < data.length - 1; i++) {
        tHisto[i] = (iHisto[i - 1] + iHisto[i] + iHisto[i + 1]) / 3;
      }
      tHisto[0] = (iHisto[0] + iHisto[1]) / 3; // 0 outside
      tHisto[data.length - 1] = (iHisto[data.length - 2] + iHisto[data.length - 1]) / 3; // 0
                                                                                         // outside
      iHisto = tHisto;
      iter++;
      if (iter > 10000) {
        threshold = -1;
        log("Minimum Threshold not found after 10000 iterations.");
        return threshold;
      }
    }
    // The threshold is the minimum between the two peaks. modified for 16 bits
    for (int i = 1; i < max; i++) {
      // log(" "+i+" "+iHisto[i]);
      if (iHisto[i - 1] > iHisto[i] && iHisto[i + 1] >= iHisto[i]) {
        threshold = i;
      }
    }
    return threshold;
  }

  /**
   * W. Tsai, "Moment-preserving thresholding: a new approach," Computer Vision, Graphics, and Image
   * Processing, vol. 29, pp. 377-393, 1985. Ported to ImageJ plugin by G.Landini from the the open
   * source project FOURIER 0.8 by M. Emre Celebi , Department of Computer Science, Louisiana State
   * University in Shreveport Shreveport, LA 71115, USA.
   * http://sourceforge.net/projects/fourier-ipal. http://www.lsus.edu/faculty/~ecelebi/fourier.htm.
   *
   * @param data the data
   * @return the threshold
   */
  public static int Moments(int[] data) {
    double total = 0;
    final double m0 = 1.0;
    double m1 = 0.0, m2 = 0.0, m3 = 0.0, sum = 0.0, p0 = 0.0;
    double cd, c0, c1, z0, z1; /* auxiliary variables */
    int threshold = -1;

    final double[] histo = new double[data.length];

    for (int i = 0; i < data.length; i++) {
      total += data[i];
    }

    for (int i = 0; i < data.length; i++) {
      histo[i] = data[i] / total; // normalised histogram
    }

    /* Calculate the first, second, and third order moments */
    for (int i = 0; i < data.length; i++) {
      m1 += i * histo[i];
      m2 += i * i * histo[i];
      m3 += i * i * i * histo[i];
    }
    /*
     * First 4 moments of the gray-level image should match the first 4 moments of the target binary
     * image. This leads to 4 equalities whose solutions are given in the Appendix of Ref. 1
     */
    cd = m0 * m2 - m1 * m1;
    c0 = (-m2 * m2 + m1 * m3) / cd;
    c1 = (m0 * -m3 + m2 * m1) / cd;
    z0 = 0.5 * (-c1 - Math.sqrt(c1 * c1 - 4.0 * c0));
    z1 = 0.5 * (-c1 + Math.sqrt(c1 * c1 - 4.0 * c0));
    p0 = (z1 - m1) / (z1 - z0); /* Fraction of the object pixels in the target binary image */

    // The threshold is the gray-level closest
    // to the p0-tile of the normalized histogram
    sum = 0;
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
   * @return the threshold
   */
  public static int Otsu(int[] data) {
    int k, kStar; // k = the current threshold; kStar = optimal threshold
    int N1, N; // N1 = # points with intensity <=k; N = total number of points
    double BCV, BCVmax; // The current Between Class Variance and maximum BCV
    double num, denom; // temporary bookeeping
    int Sk; // The total intensity for all histogram points <=k
    int S; // The total intensity of the image
    final int L = data.length;

    // Initialize values:
    S = N = 0;
    double ssx = 0;
    for (k = 0; k < L; k++) {
      ssx += k * k * data[k];
      S += k * data[k]; // Total histogram intensity
      N += data[k]; // Total number of data points
    }

    Sk = 0;
    N1 = data[0]; // The entry for zero intensity
    BCV = 0;
    BCVmax = 0;
    kStar = 0;
    int kStar_count = 0;

    // Look at each possible threshold value,
    // calculate the between-class variance, and decide if it's a max
    for (k = 1; k < L - 1; k++) { // No need to check endpoints k = 0 or k = L-1
      Sk += k * data[k];
      N1 += data[k];

      // The float casting here is to avoid compiler warning about loss of precision and
      // will prevent overflow in the case of large saturated images
      denom = (double) (N1) * (N - N1); // Maximum value of denom is (N^2)/4 = approx. 3E10

      if (denom != 0) {
        // Float here is to avoid loss of precision when dividing
        num = ((double) N1 / N) * S - Sk; // Maximum value of num = 255*N = approx 8E7
        BCV = (num * num) / denom;

      } else {
        BCV = 0;
      }

      // if (BCV >= BCVmax)
      // { // Assign the best threshold found so far
      // BCVmax = BCV;
      // kStar = k;
      // }
      // Added for debugging.
      // Gonzalex & Woods, Digital Image Processing, 3rd Ed. pp 746:
      // "If a maximum exists for more than one threshold it is customary to average them"
      if (BCV > BCVmax) { // Assign the best threshold found so far
        BCVmax = BCV;
        kStar = k;
        kStar_count = 1;
      } else if (BCV == BCVmax) {
        // Total the thresholds for averaging
        kStar += k;
        kStar_count++;
      }
    }

    if (kStar_count > 1) {
      if (isDebug) {
        log("Otsu method has multiple optimal thresholds");
      }
      kStar /= kStar_count;
    }

    // Output the measure of separability. Requires BCVmax / BCVglobal
    if (isDebug && N > 0) {
      // Calculate global variance
      final double sx = S;
      ssx = 0;
      for (k = 1; k < L; k++) {
        ssx += k * k * data[k];
      }
      BCV = (ssx - sx * sx / N) / N;

      // Removed use of minbin to allow thread safe execution
      // log(String.format("Otsu separability @ %d: %f", kStar + minbin, (BCVmax / BCV)));
      log(String.format("Otsu separability @ %d: %f", kStar, (BCVmax / BCV)));
    }

    // kStar += 1; // Use QTI convention that intensity -> 1 if intensity >= k
    // (the algorithm was developed for I-> 1 if I <= k.)
    return kStar;
  }

  /**
   * W. Doyle, "Operation useful for similarity-invariant pattern recognition," Journal of the
   * Association for Computing Machinery, vol. 9,pp. 259-267, 1962. Ported to ImageJ plugin by
   * G.Landini from Antti Niemisto's Matlab code (GPL). Original Matlab code Copyright (C) 2004
   * Antti Niemisto. See http://www.cs.tut.fi/~ant/histthresh/ for an excellent slide presentation
   * and the original Matlab code.
   *
   * @param data the data
   * @return the threshold
   */
  public static int Percentile(int[] data) {
    int threshold = -1;
    final double ptile = 0.5; // default fraction of foreground pixels

    // double[] avec = new double[data.length];
    // for (int i = 0; i < data.length; i++)
    // avec[i] = 0.0;

    final double total = partialSum(data, data.length - 1);
    double temp = 1.0;
    double sum = 0;
    for (int i = 0; i < data.length; i++) {
      // avec[i] = Math.abs((partialSum(data, i) / total) - ptile);
      // avec[i] = Math.abs((partialSum(data, i) / total) - ptile);
      // log("Ptile[" + i + "]:" + avec[i]);
      //
      // if (avec[i] < temp)
      // {
      // temp = avec[i];
      // threshold = i;
      // }

      sum += data[i];
      // Fraction of the data
      final double f = sum / total;

      // Get closest fraction to the target fraction
      if (f < ptile) {
        final double distance = ptile - f;
        if (distance < temp) {
          temp = distance;
          threshold = i;
        }
      } else {
        final double distance = f - ptile;
        if (distance < temp) {
          temp = distance;
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
   * @param y the y
   * @param j the j
   * @return the double
   */
  protected static double partialSum(int[] y, int j) {
    double x = 0;
    for (int i = 0; i <= j; i++) {
      x += y[i];
    }
    return x;
  }

  /**
   * Kapur J.N., Sahoo P.K., and Wong A.K.C. (1985) "A New Method for Gray-Level Picture
   * Thresholding Using the Entropy of the Histogram" Graphical Models and Image Processing, 29(3):
   * 273-285. Ported to ImageJ plugin by G.Landini from E Celebi's fourier_0.8 routines.
   *
   * @param data the data
   * @return the threshold
   */
  public static int RenyiEntropy(int[] data) {
    int threshold;
    int opt_threshold;

    int ih, it;
    int first_bin;
    int last_bin;
    int tmp_var;
    int t_star1, t_star2, t_star3;
    int beta1, beta2, beta3;
    double alpha;/* alpha parameter of the method */
    double term;
    double tot_ent; /* total entropy */
    double max_ent; /* max entropy */
    double ent_back; /* entropy of the background pixels at a given threshold */
    double ent_obj; /* entropy of the object pixels at a given threshold */
    double omega;
    final double[] norm_histo = new double[data.length]; /* normalized histogram */
    final double[] P1 = new double[data.length]; /* cumulative normalized histogram */
    final double[] P2 = new double[data.length];

    int total = 0;
    for (ih = 0; ih < data.length; ih++) {
      total += data[ih];
    }

    for (ih = 0; ih < data.length; ih++) {
      norm_histo[ih] = (double) data[ih] / total;
    }

    P1[0] = norm_histo[0];
    P2[0] = 1.0 - P1[0];
    for (ih = 1; ih < data.length; ih++) {
      P1[ih] = P1[ih - 1] + norm_histo[ih];
      P2[ih] = 1.0 - P1[ih];
    }

    /* Determine the first non-zero bin */
    first_bin = 0;
    for (ih = 0; ih < data.length; ih++) {
      if (!(Math.abs(P1[ih]) < 2.220446049250313E-16)) {
        first_bin = ih;
        break;
      }
    }

    /* Determine the last non-zero bin */
    last_bin = data.length - 1;
    for (ih = data.length - 1; ih >= first_bin; ih--) {
      if (!(Math.abs(P2[ih]) < 2.220446049250313E-16)) {
        last_bin = ih;
        break;
      }
    }

    /* Maximum Entropy Thresholding - BEGIN */
    /* ALPHA = 1.0 */
    /*
     * Calculate the total entropy each gray-level and find the threshold that maximizes it
     */
    threshold = 0; // was MIN_INT in original code, but if an empty image is processed it gives an
                   // error later on.
    max_ent = 0.0;

    for (it = first_bin; it <= last_bin; it++) {
      /* Entropy of the background pixels */
      ent_back = 0.0;
      for (ih = 0; ih <= it; ih++) {
        if (data[ih] != 0) {
          ent_back -= (norm_histo[ih] / P1[it]) * Math.log(norm_histo[ih] / P1[it]);
        }
      }

      /* Entropy of the object pixels */
      ent_obj = 0.0;
      for (ih = it + 1; ih < data.length; ih++) {
        if (data[ih] != 0) {
          ent_obj -= (norm_histo[ih] / P2[it]) * Math.log(norm_histo[ih] / P2[it]);
        }
      }

      /* Total entropy */
      tot_ent = ent_back + ent_obj;

      // log(""+max_ent+" "+tot_ent);

      if (max_ent < tot_ent) {
        max_ent = tot_ent;
        threshold = it;
      }
    }
    t_star2 = threshold;

    /* Maximum Entropy Thresholding - END */
    threshold = 0; // was MIN_INT in original code, but if an empty image is processed it gives an
                   // error later on.
    max_ent = 0.0;
    alpha = 0.5;
    term = 1.0 / (1.0 - alpha);
    for (it = first_bin; it <= last_bin; it++) {
      /* Entropy of the background pixels */
      ent_back = 0.0;
      for (ih = 0; ih <= it; ih++) {
        ent_back += Math.sqrt(norm_histo[ih] / P1[it]);
      }

      /* Entropy of the object pixels */
      ent_obj = 0.0;
      for (ih = it + 1; ih < data.length; ih++) {
        ent_obj += Math.sqrt(norm_histo[ih] / P2[it]);
      }

      /* Total entropy */
      tot_ent = term * ((ent_back * ent_obj) > 0.0 ? Math.log(ent_back * ent_obj) : 0.0);

      if (tot_ent > max_ent) {
        max_ent = tot_ent;
        threshold = it;
      }
    }

    t_star1 = threshold;

    threshold = 0; // was MIN_INT in original code, but if an empty image is processed it gives an
                   // error later on.
    max_ent = 0.0;
    alpha = 2.0;
    term = 1.0 / (1.0 - alpha);
    for (it = first_bin; it <= last_bin; it++) {
      /* Entropy of the background pixels */
      ent_back = 0.0;
      for (ih = 0; ih <= it; ih++) {
        ent_back += (norm_histo[ih] * norm_histo[ih]) / (P1[it] * P1[it]);
      }

      /* Entropy of the object pixels */
      ent_obj = 0.0;
      for (ih = it + 1; ih < data.length; ih++) {
        ent_obj += (norm_histo[ih] * norm_histo[ih]) / (P2[it] * P2[it]);
      }

      /* Total entropy */
      tot_ent = term * ((ent_back * ent_obj) > 0.0 ? Math.log(ent_back * ent_obj) : 0.0);

      if (tot_ent > max_ent) {
        max_ent = tot_ent;
        threshold = it;
      }
    }

    t_star3 = threshold;

    /* Sort t_star values */
    if (t_star2 < t_star1) {
      tmp_var = t_star1;
      t_star1 = t_star2;
      t_star2 = tmp_var;
    }
    if (t_star3 < t_star2) {
      tmp_var = t_star2;
      t_star2 = t_star3;
      t_star3 = tmp_var;
    }
    if (t_star2 < t_star1) {
      tmp_var = t_star1;
      t_star1 = t_star2;
      t_star2 = tmp_var;
    }

    /* Adjust beta values */
    if (Math.abs(t_star1 - t_star2) <= 5) {
      if (Math.abs(t_star2 - t_star3) <= 5) {
        beta1 = 1;
        beta2 = 2;
        beta3 = 1;
      } else {
        beta1 = 0;
        beta2 = 1;
        beta3 = 3;
      }
    } else if (Math.abs(t_star2 - t_star3) <= 5) {
      beta1 = 3;
      beta2 = 1;
      beta3 = 0;
    } else {
      beta1 = 1;
      beta2 = 2;
      beta3 = 1;
    }
    // log(""+t_star1+" "+t_star2+" "+t_star3);
    /* Determine the optimal threshold value */
    omega = P1[t_star3] - P1[t_star1];
    opt_threshold = (int) (t_star1 * (P1[t_star1] + 0.25 * omega * beta1)
        + 0.25 * t_star2 * omega * beta2 + t_star3 * (P2[t_star3] + 0.25 * omega * beta3));

    return opt_threshold;
  }

  /**
   * Shanhbag A.G. (1994) "Utilization of Information Measure as a Means of Image Thresholding"
   * Graphical Models and Image Processing, 56(5): 414-419. Ported to ImageJ plugin by G.Landini
   * from E Celebi's fourier_0.8 routines.
   *
   * @param data the data
   * @return the threshold
   */
  public static int Shanbhag(int[] data) {
    int threshold;
    int ih, it;
    int first_bin;
    int last_bin;
    double term;
    double tot_ent; /* total entropy */
    double min_ent; /* max entropy */
    double ent_back; /* entropy of the background pixels at a given threshold */
    double ent_obj; /* entropy of the object pixels at a given threshold */
    final double[] norm_histo = new double[data.length]; /* normalized histogram */
    final double[] P1 = new double[data.length]; /* cumulative normalized histogram */
    final double[] P2 = new double[data.length];

    int total = 0;
    for (ih = 0; ih < data.length; ih++) {
      total += data[ih];
    }

    for (ih = 0; ih < data.length; ih++) {
      norm_histo[ih] = (double) data[ih] / total;
    }

    P1[0] = norm_histo[0];
    P2[0] = 1.0 - P1[0];
    for (ih = 1; ih < data.length; ih++) {
      P1[ih] = P1[ih - 1] + norm_histo[ih];
      P2[ih] = 1.0 - P1[ih];
    }

    /* Determine the first non-zero bin */
    first_bin = 0;
    for (ih = 0; ih < data.length; ih++) {
      if (!(Math.abs(P1[ih]) < 2.220446049250313E-16)) {
        first_bin = ih;
        break;
      }
    }

    /* Determine the last non-zero bin */
    last_bin = data.length - 1;
    for (ih = data.length - 1; ih >= first_bin; ih--) {
      if (!(Math.abs(P2[ih]) < 2.220446049250313E-16)) {
        last_bin = ih;
        break;
      }
    }

    // Calculate the total entropy each gray-level
    // and find the threshold that maximizes it
    threshold = -1;
    min_ent = Double.MAX_VALUE;

    for (it = first_bin; it <= last_bin; it++) {
      /* Entropy of the background pixels */
      ent_back = 0.0;
      term = 0.5 / P1[it];
      for (ih = 1; ih <= it; ih++) {
        ent_back -= norm_histo[ih] * Math.log(1.0 - term * P1[ih - 1]);
      }
      ent_back *= term;

      /* Entropy of the object pixels */
      ent_obj = 0.0;
      term = 0.5 / P2[it];
      for (ih = it + 1; ih < data.length; ih++) {
        ent_obj -= norm_histo[ih] * Math.log(1.0 - term * P2[ih]);
      }
      ent_obj *= term;

      /* Total entropy */
      tot_ent = Math.abs(ent_back - ent_obj);

      if (tot_ent < min_ent) {
        min_ent = tot_ent;
        threshold = it;
      }
    }
    return threshold;
  }

  /**
   * Zack, G. W., Rogers, W. E. and Latt, S. A., 1977, Automatic Measurement of Sister Chromatid
   * Exchange Frequency, Journal of Histochemistry and Cytochemistry 25 (7), pp. 741-753.
   *
   * Modified from Johannes Schindelin plugin.
   *
   * @param data the data
   * @return the threshold
   */
  public static int Triangle(int[] data) {
    // find min and max
    int min = 0, dmax = 0, max = 0, min2 = 0;
    for (int i = 0; i < data.length; i++) {
      if (data[i] > 0) {
        min = i;
        break;
      }
    }
    if (min > 0) {
      min--; // line to the (p==0) point, not to data[min]
    }

    // The Triangle algorithm cannot tell whether the data is skewed to one side or another.
    // This causes a problem as there are 2 possible thresholds between the max and the 2 extremes
    // of the histogram.
    // Here I propose to find out to which side of the max point the data is furthest, and use that
    // as
    // the other extreme.
    for (int i = data.length - 1; i > 0; i--) {
      if (data[i] > 0) {
        min2 = i;
        break;
      }
    }
    if (min2 < data.length - 1) {
      min2++; // line to the (p==0) point, not to data[min]
    }

    for (int i = 0; i < data.length; i++) {
      if (data[i] > dmax) {
        max = i;
        dmax = data[i];
      }
    }
    // find which is the furthest side
    // log(""+min+" "+max+" "+min2);
    boolean inverted = false;
    if ((max - min) < (min2 - max)) {
      // reverse the histogram
      // log("Reversing histogram.");
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
      // log("Triangle: min == max.");
      return min;
    }

    // describe line by nx * x + ny * y - d = 0
    double nx, ny, d;
    // nx is just the max frequency as the other point has freq=0
    nx = data[max]; // -min; // data[min]; // lowest value bmin = (p=0)% in the image
    ny = min - max;
    d = Math.sqrt(nx * nx + ny * ny);
    nx /= d;
    ny /= d;
    d = nx * min + ny * data[min];

    // find split point
    int split = min;
    double splitDistance = 0;
    for (int i = min + 1; i <= max; i++) {
      final double newDistance = nx * i + ny * data[i] - d;
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
   * Ported to ImageJ plugin by G.Landini from E Celebi's fourier_0.8 routines.
   *
   * @param data the data
   * @return the threshold
   */
  public static int Yen(int[] data) {
    int threshold;
    int ih, it;
    double crit;
    double max_crit;
    final double[] norm_histo = new double[data.length]; /* normalized histogram */
    final double[] P1 = new double[data.length]; /* cumulative normalized histogram */
    final double[] P1_sq = new double[data.length];
    final double[] P2_sq = new double[data.length];

    int total = 0;
    for (ih = 0; ih < data.length; ih++) {
      total += data[ih];
    }

    for (ih = 0; ih < data.length; ih++) {
      norm_histo[ih] = (double) data[ih] / total;
    }

    P1[0] = norm_histo[0];
    for (ih = 1; ih < data.length; ih++) {
      P1[ih] = P1[ih - 1] + norm_histo[ih];
    }

    P1_sq[0] = norm_histo[0] * norm_histo[0];
    for (ih = 1; ih < data.length; ih++) {
      P1_sq[ih] = P1_sq[ih - 1] + norm_histo[ih] * norm_histo[ih];
    }

    P2_sq[data.length - 1] = 0.0;
    for (ih = data.length - 2; ih >= 0; ih--) {
      P2_sq[ih] = P2_sq[ih + 1] + norm_histo[ih + 1] * norm_histo[ih + 1];
    }

    /* Find the threshold that maximizes the criterion */
    threshold = -1;
    max_crit = Double.MIN_VALUE;
    for (it = 0; it < data.length; it++) {
      crit = -1.0 * ((P1_sq[it] * P2_sq[it]) > 0.0 ? Math.log(P1_sq[it] * P2_sq[it]) : 0.0)
          + 2 * ((P1[it] * (1.0 - P1[it])) > 0.0 ? Math.log(P1[it] * (1.0 - P1[it])) : 0.0);
      if (crit > max_crit) {
        max_crit = crit;
        threshold = it;
      }
    }
    return threshold;
  }

  /**
   * Gets the threshold. <p> This method is faster than calling the required static thresholding
   * method as the histogram is cropped to the min-max range before processing.
   *
   * @param method the method
   * @param data the data
   * @return the threshold
   */
  public static int getThreshold(int method, int[] data) {
    if (method < 0 || method >= methods.length) {
      return 0;
    }
    return getThreshold(Method.values()[method], data);
  }

  /**
   * Gets the threshold. <p> This method is faster than calling the required static thresholding
   * method as the histogram is cropped to the min-max range before processing.
   *
   * @param method the method
   * @param data the data
   * @return the threshold
   */
  public static int getThreshold(String method, int[] data) {
    if (TextUtils.isNullOrEmpty(method)) {
      return 0;
    }
    for (final Method m : Method.values()) {
      if (m.name.equals(method)) {
        return getThreshold(m, data);
      }
    }
    return 0;
  }

  /**
   * Gets the threshold. <p> This method is faster than calling the required static thresholding
   * method as the histogram is cropped to the min-max range before processing.
   *
   * @param method the method
   * @param data the data
   * @return the threshold
   */
  public static int getThreshold(Method method, int[] data) {
    if (method == Method.NONE || data == null) {
      return 0;
    }

    // bracket the histogram to the range that holds data to make it quicker
    int minbin = 0;
    int maxbin = data.length - 1;
    // for (int i = 0; i < data.length; i++)
    // {
    // if (data[i] > 0)
    // maxbin = i;
    // }
    // for (int i = data.length - 1; i >= 0; i--)
    // {
    // if (data[i] > 0)
    // minbin = i;
    // }
    while ((data[minbin] == 0) && (minbin < maxbin)) {
      minbin++;
    }
    while ((data[maxbin] == 0) && (maxbin > minbin)) {
      maxbin--;
    }

    final int size = (maxbin - minbin) + 1;
    final int[] data2;
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
    // Should not be needed
    // if (data == null)
    // return 0;

    switch (method) {
      //@formatter:off
			case OTSU: return Otsu(data);
			case DEFAULT: return IJDefault(data);
			case HUANG: return Huang(data);
			case INTERMODES: return Intermodes(data);
			case ISO_DATA: return IsoData(data);
			case LI: return Li(data);
			case MAX_ENTROPY: return MaxEntropy(data);
			case MEAN: return Mean(data);
			case MEAN_PLUS_STD_DEV: return MeanPlusStdDev(data);
			case MIN_ERROR_I: return MinErrorI(data);
			case MINIMUM: return Minimum(data);
			case MOMENTS: return Moments(data);
			case PERCENTILE: return Percentile(data);
			case RENYI_ENTROPY: return RenyiEntropy(data);
			case SHANBHAG: return Shanbhag(data);
			case TRIANGLE: return Triangle(data);
			case YEN: return Yen(data);
			case NONE:
			default: return -1;
			//@formatter:on
    }
  }

  private static void log(String string) {
    if (isLogging) {
      // This could be changed to output somewhere else, e.g. a utils.logging.Logger instance
      System.out.println(string);
    }
  }
}
