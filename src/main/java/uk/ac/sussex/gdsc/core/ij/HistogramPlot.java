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
 * Copyright (C) 2011 - 2020 Alex Herbert
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

import ij.gui.PlotWindow;
import java.util.Arrays;
import java.util.Objects;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.util.FastMath;
import uk.ac.sussex.gdsc.core.ij.gui.Plot2;
import uk.ac.sussex.gdsc.core.ij.plugin.WindowOrganiser;
import uk.ac.sussex.gdsc.core.utils.DoubleData;
import uk.ac.sussex.gdsc.core.utils.MathUtils;
import uk.ac.sussex.gdsc.core.utils.SimpleArrayUtils;
import uk.ac.sussex.gdsc.core.utils.Statistics;
import uk.ac.sussex.gdsc.core.utils.StoredDataStatistics;

/**
 * Class for computing and plotting histograms.
 */
public class HistogramPlot {

  /** The plot title. */
  private final String plotTitle;
  /** The data. */
  private final DoubleData data;
  /** The name of the plotted statistic. */
  private final String name;
  /** The minimum bin width to use (e.g. set to 1 for integer values). 0 is auto. */
  private double minBinWidth;
  /**
   * The remove outliers option.
   *
   * <p>1 - 1.5x IQR. 2 - remove top 2%.
   */
  private int removeOutliersOption;
  /** The number of bins to use. 0 is auto. */
  private int numberOfBins;
  /** The plot shape. */
  private int plotShape = Plot2.BAR;
  /** The plot label. */
  private String plotLabel;
  /** The method to select the histogram bins. Used if the input number of bins is zero. */
  private BinMethod binMethod = BinMethod.SCOTT;

  /** The stats derived from the data. */
  private StoredDataStatistics stats;
  /** The lower range of the interquartile range. */
  private double lower;
  /** The upper range of the interquartile range. */
  private double upper = Double.NaN;
  /** The x-values from the last histogram plotted. */
  private double[] plotXValues;
  /** The y-values from the last histogram plotted. */
  private double[] plotYValues;
  /** The min x from the last histogram plotted. */
  private double plotMinX;
  /** The max x from the last histogram plotted. */
  private double plotMaxX;
  /** The max y from the last histogram plotted. */
  private double plotMaxY;
  /** The last histogram plotted. */
  private Plot2 plot;

  /**
   * A builder for {@link HistogramPlot}.
   */
  public static class HistogramPlotBuilder {
    /** The title to prepend to the plot name. */
    private String title;
    /** The data. */
    private DoubleData data;
    /** The name of the plotted statistic. */
    private String name;
    /** The minimum bin width to use (e.g. set to 1 for integer values). 0 is auto. */
    private double minBinWidth;
    /**
     * The remove outliers option.
     *
     * <p>1 - 1.5x IQR. 2 - remove top 2%.
     */
    private int removeOutliersOption;
    /** The number of bins to use. 0 is auto. */
    private int numberOfBins;
    /** The plot shape. */
    private int plotShape = Plot2.BAR;
    /** The plot label. */
    private String plotLabel;
    /** The method to select the histogram bins. Used if the input number of bins is zero. */
    private BinMethod binMethod = BinMethod.SCOTT;

    /**
     * Instantiates a new histogram plot builder.
     *
     * @param title The title to prepend to the plot name
     * @param data The data
     * @param name The name of the plotted statistic
     */
    public HistogramPlotBuilder(String title, DoubleData data, String name) {
      setTitle(title);
      setData(data);
      setName(name);
    }

    /**
     * Instantiates a new histogram plot builder.
     *
     * <p>Note an exception will be raised within {@link #build()} if the required fields are never
     * set.
     *
     * @param title The title to prepend to the plot name
     * @see #setData(DoubleData)
     * @see #setName(String)
     */
    public HistogramPlotBuilder(String title) {
      setTitle(title);
    }

    /**
     * Sets the title.
     *
     * @param title The title to prepend to the plot name
     * @return the histogram plot builder
     */
    public final HistogramPlotBuilder setTitle(String title) {
      this.title = Objects.requireNonNull(title, "Title must not be null");
      return this;
    }

    /**
     * Sets the data.
     *
     * @param data the data
     * @return the histogram plot builder
     */
    public final HistogramPlotBuilder setData(DoubleData data) {
      this.data = Objects.requireNonNull(data, "Data must not be null");
      return this;
    }

    /**
     * Sets the name of the plotted statistic.
     *
     * @param name The name of the plotted statistic
     * @return the histogram plot builder
     */
    public final HistogramPlotBuilder setName(String name) {
      this.name = Objects.requireNonNull(name, "Name must not be null");
      return this;
    }

    /**
     * Sets the minimum bin width to use (e.g. set to 1 for integer values). 0 is auto.
     *
     * @param minBinWidth the new minimum bin width
     * @return the histogram plot builder
     */
    public HistogramPlotBuilder setMinBinWidth(double minBinWidth) {
      this.minBinWidth = minBinWidth;
      return this;
    }

    /**
     * Sets the minimum bin width to use 1 for integer values or 0 for auto.
     *
     * @param isInteger Set to true if using integer bin widths
     * @return the histogram plot builder
     */
    public HistogramPlotBuilder setIntegerBins(boolean isInteger) {
      return setMinBinWidth(isInteger ? 1 : 0);
    }

    /**
     * Sets the removes the outliers option.
     *
     * <ol> <li>1.5x IQR</li><li>Remove top 2%</li></ol>
     *
     * @param removeOutliersOption the new removes the outliers option
     * @return the histogram plot builder
     */
    public HistogramPlotBuilder setRemoveOutliersOption(int removeOutliersOption) {
      this.removeOutliersOption = removeOutliersOption;
      return this;
    }

    /**
     * Sets the number of bins to use. 0 is auto.
     *
     * @param numberOfBins the new number of bins
     * @return the histogram plot builder
     */
    public HistogramPlotBuilder setNumberOfBins(int numberOfBins) {
      this.numberOfBins = numberOfBins;
      return this;
    }

    /**
     * Sets the plot shape. Use {@link Plot2#BAR} to draw a bar chart.
     *
     * @param plotShape the new plot shape
     * @return the histogram plot builder
     */
    public HistogramPlotBuilder setPlotShape(int plotShape) {
      this.plotShape = plotShape;
      return this;
    }

    /**
     * Sets the plot label.
     *
     * @param plotLabel the new plot label
     * @return the histogram plot builder
     */
    public HistogramPlotBuilder setPlotLabel(String plotLabel) {
      this.plotLabel = plotLabel;
      return this;
    }

    /**
     * Sets the bin method for computing the number of bins.
     *
     * @param binMethod the new default method
     * @return the histogram plot builder
     */
    public HistogramPlotBuilder setBinMethod(BinMethod binMethod) {
      this.binMethod = binMethod;
      return this;
    }

    /**
     * Builds the {@link HistogramPlot}.
     *
     * @return the histogram plot
     */
    public HistogramPlot build() {
      final HistogramPlot hp = new HistogramPlot(title, data, name);
      hp.setMinBinWidth(minBinWidth);
      hp.setRemoveOutliersOption(removeOutliersOption);
      hp.setNumberOfBins(numberOfBins);
      hp.setPlotShape(plotShape);
      hp.setPlotLabel(plotLabel);
      hp.setBinMethod(binMethod);
      return hp;
    }

    /**
     * Builds the {@link HistogramPlot} and calls {@link HistogramPlot#show()}.
     *
     * @return The histogram plot window (or null if the plot is not possible)
     */
    public PlotWindow show() {
      return show(null);
    }

    /**
     * Builds the {@link HistogramPlot} and calls {@link HistogramPlot#show(WindowOrganiser)}.
     *
     * <p>If the plot is shown in a new window it will be added to the provided organiser (if not
     * null).
     *
     * @param windowOrganiser the window organiser
     * @return The histogram plot window (or null if the plot is not possible)
     */
    public PlotWindow show(WindowOrganiser windowOrganiser) {
      return build().show(windowOrganiser);
    }
  }

  /**
   * The method to select the number of histogram bins.
   */
  public enum BinMethod {
    /**
     * The Scott's rule bin method.
     *
     * @see #getBinWidthScottsRule(double, int)
     */
    SCOTT,
    /**
     * The Freedman-Diaconis rule bin method.
     *
     * @see #getBinWidthFreedmanDiaconisRule(double, double, int)
     */
    FD,
    /**
     * The Sturges' rule bin method.
     *
     * @see #getBinsSturgesRule(int)
     */
    STURGES,
    /**
     * The square root rule bin method.
     *
     * @see #getBinsSqrtRule(int)
     */
    SQRT
  }

  /**
   * Gets the bins.
   *
   * <p>Based on the MatLab methods.
   *
   * @param data the data
   * @param method the method
   * @return the bins
   * @see "http://uk.mathworks.com/help/matlab/ref/histogram.html"
   */
  public static int getBins(DoubleData data, BinMethod method) {
    double width;
    double[] limits;
    switch (method) {
      case SCOTT:
        final Statistics stats =
            (data instanceof Statistics) ? (Statistics) data : Statistics.create(data.values());
        width = getBinWidthScottsRule(stats.getStandardDeviation(), data.size());
        limits = MathUtils.limits(data.values());
        return (int) Math.ceil((limits[1] - limits[0]) / width);

      case FD:
        final DescriptiveStatistics descriptiveStats =
            (data instanceof StoredDataStatistics) ? ((StoredDataStatistics) data).getStatistics()
                : new DescriptiveStatistics(data.values());
        final double lower = descriptiveStats.getPercentile(25);
        final double upper = descriptiveStats.getPercentile(75);
        width = getBinWidthFreedmanDiaconisRule(upper, lower, data.size());
        limits = MathUtils.limits(data.values());
        return (int) Math.ceil((limits[1] - limits[0]) / width);

      case STURGES:
        return getBinsSturgesRule(data.size());

      case SQRT:
      default:
        return getBinsSqrtRule(data.size());
    }
  }

  /**
   * Gets the bin width using Scott's rule.
   *
   * <pre>
   * 3.5 * sd / cubeRoot(n)
   * </pre>
   *
   * @param sd the sd
   * @param n the n
   * @return the bin width using Scott's rule
   */
  public static double getBinWidthScottsRule(double sd, int n) {
    return 3.5 * sd / FastMath.cbrt(n);
  }

  /**
   * Gets the bin width using the Freedman-Diaconis rule.
   *
   * <pre>
   * 2 * IQR / cubeRoot(n)
   * </pre>
   *
   * @param upper the upper of the Inter-Quartile Range (IQR)
   * @param lower the lower of the Inter-Quartile Range (IQR)
   * @param n the n
   * @return the bin width freedman diaconis rule
   */
  public static double getBinWidthFreedmanDiaconisRule(double upper, double lower, int n) {
    final double iqr = upper - lower;
    return 2 * iqr / FastMath.cbrt(n);
  }

  /**
   * Gets the bins using the Sturges' rule.
   *
   * <pre>
   * ceil(1 + log2(n)
   * </pre>
   *
   * @param n the n
   * @return the number of bins
   */
  public static int getBinsSturgesRule(int n) {
    return (int) Math.ceil(1 + Math.log(n) / 0.69314718);
  }

  /**
   * Gets the bins using the square root rule.
   *
   * <pre>
   * ceil(squareRoot(n))
   * </pre>
   *
   * @param n the n
   * @return the number of bins
   */
  public static int getBinsSqrtRule(int n) {
    return (int) Math.ceil(Math.sqrt(n));
  }

  /**
   * Calculate a histogram given the provided data.
   *
   * @param data the data
   * @param numberOfBins The number of histogram bins between min and max
   * @return The histogram as a pair of arrays: { value[], frequency[] }
   */
  public static float[][] calcHistogram(float[] data, int numberOfBins) {
    final float[] limits = MathUtils.limits(data);
    return calcHistogram(data, limits[0], limits[1], numberOfBins);
  }

  /**
   * Calculate a histogram given the provided data.
   *
   * <p>The histogram will create the specified number of bins to accommodate all data between the
   * minimum and maximum inclusive. The number of bins must be above one so that min and max are in
   * different bins. If min and max are the same then the number of bins is set to 1.
   *
   * @param data the data
   * @param minimum The minimum value to include (inclusive)
   * @param maximum The maximum value to include (inclusive)
   * @param numberOfBins The number of histogram bins between min and max (must be above one)
   * @return The histogram as a pair of arrays: { value[], frequency[] }
   */
  public static float[][] calcHistogram(float[] data, double minimum, double maximum,
      int numberOfBins) {
    // Parameter check
    int numBins = Math.max(2, numberOfBins);
    double min = minimum;
    double max = maximum;
    if (max < min) {
      final double tmp = max;
      max = min;
      min = tmp;
    }
    double binSize;
    if (max == min) {
      numBins = 1;
      binSize = 1;
    } else {
      binSize = (max - min) / (numBins - 1);
    }

    final float[] value = new float[numBins];
    final float[] frequency = new float[numBins];

    for (int i = 0; i < numBins; i++) {
      value[i] = (float) (min + i * binSize);
    }

    for (final double d : data) {
      final int bin = (int) ((d - min) / binSize);
      // Ignore data outside the range
      if (bin >= 0 && bin < numBins) {
        frequency[bin]++;
      }
    }

    return new float[][] {value, frequency};
  }

  /**
   * Calculate a histogram given the provided data.
   *
   * @param data the data
   * @param numberOfBins The number of histogram bins between min and max
   * @return The histogram as a pair of arrays: { value[], frequency[] }
   */
  public static double[][] calcHistogram(double[] data, int numberOfBins) {
    final double[] limits = MathUtils.limits(data);
    return calcHistogram(data, limits[0], limits[1], numberOfBins);
  }

  /**
   * Calculate a histogram given the provided data.
   *
   * <p>The histogram will create the specified number of bins to accommodate all data between the
   * minimum and maximum inclusive. The number of bins must be above one so that min and max are in
   * different bins. If min and max are the same then the number of bins is set to 1.
   *
   * @param data the data
   * @param minimum The minimum value to include (inclusive)
   * @param maximum The maximum value to include (inclusive)
   * @param numberOfBins The number of histogram bins between min and max (must be above one)
   * @return The histogram as a pair of arrays: { value[], frequency[] }
   */
  public static double[][] calcHistogram(double[] data, double minimum, double maximum,
      int numberOfBins) {
    // Parameter check
    int numBins = Math.max(2, numberOfBins);
    double min = minimum;
    double max = maximum;
    if (max < min) {
      final double tmp = max;
      max = min;
      min = tmp;
    }
    double binSize;
    if (max == min) {
      numBins = 1;
      binSize = 1;
    } else {
      binSize = (max - min) / (numBins - 1);
    }

    final double[] value = new double[numBins];
    final double[] frequency = new double[numBins];

    for (int i = 0; i < numBins; i++) {
      value[i] = (min + i * binSize);
    }

    for (final double d : data) {
      final int bin = (int) ((d - min) / binSize);
      // Ignore data outside the range
      if (bin >= 0 && bin < numBins) {
        frequency[bin]++;
      }
    }

    return new double[][] {value, frequency};
  }

  /**
   * For the provided histogram x-axis bins, produce an x-axis for plotting. This functions doubles
   * up the histogram x-positions to allow plotting a square line profile using the ImageJ plot
   * command.
   *
   * @param histogramX the histogram X
   * @return the x-axis values
   */
  public static float[] createHistogramAxis(float[] histogramX) {
    final float[] axis = new float[histogramX.length * 2 + 2];
    int index = 0;
    for (final float value : histogramX) {
      axis[index++] = value;
      axis[index++] = value;
    }
    if (histogramX.length > 0) {
      final float dx = (histogramX.length == 1) ? 1 : (histogramX[1] - histogramX[0]);
      axis[index++] = histogramX[histogramX.length - 1] + dx;
      axis[index] = histogramX[histogramX.length - 1] + dx;
    }
    return axis;
  }

  /**
   * For the provided histogram x-axis bins, produce an x-axis for plotting. This functions doubles
   * up the histogram x-positions to allow plotting a square line profile using the ImageJ plot
   * command.
   *
   * @param histogramX the histogram X
   * @return the x-axis values
   */
  public static double[] createHistogramAxis(double[] histogramX) {
    final double[] axis = new double[histogramX.length * 2 + 2];
    int index = 0;
    for (final double value : histogramX) {
      axis[index++] = value;
      axis[index++] = value;
    }
    if (histogramX.length > 0) {
      final double dx = (histogramX.length == 1) ? 1 : (histogramX[1] - histogramX[0]);
      axis[index++] = histogramX[histogramX.length - 1] + dx;
      axis[index] = histogramX[histogramX.length - 1] + dx;
    }
    return axis;
  }

  /**
   * For the provided histogram y-axis values, produce a y-axis for plotting. This functions doubles
   * up the histogram values to allow plotting a square line profile using the ImageJ plot command.
   *
   * @param histogramY the histogram Y
   * @return the y-axis values
   */
  public static float[] createHistogramValues(float[] histogramY) {
    final float[] axis = new float[histogramY.length * 2 + 2];

    int index = 1;
    for (final float value : histogramY) {
      axis[index++] = value;
      axis[index++] = value;
    }
    return axis;
  }

  /**
   * For the provided histogram y-axis values, produce a y-axis for plotting. This functions doubles
   * up the histogram values to allow plotting a square line profile using the ImageJ plot command.
   *
   * @param histogramY the histogram Y
   * @return the y-axis values
   */
  public static double[] createHistogramValues(double[] histogramY) {
    final double[] axis = new double[histogramY.length * 2 + 2];

    int index = 1;
    for (final double value : histogramY) {
      axis[index++] = value;
      axis[index++] = value;
    }
    return axis;
  }

  /**
   * Return the histogram statistics.
   *
   * @param x Histogram values
   * @param y Histogram counts
   * @return Array containing: { mean, standard deviation }
   */
  public static double[] getHistogramStatistics(float[] x, float[] y) {
    // Get the average
    double total = 0;
    double sum = 0.0;
    double sumSq = 0.0;
    for (int i = 0; i < x.length; i++) {
      if (y[i] > 0) {
        final float count = y[i];
        final float value = x[i];
        total += count;
        sum += value * count;
        sumSq += (value * value) * count;
      }
    }
    if (total == 0) {
      return new double[2];
    }
    final double av = sum / total;

    // Get the Std.Dev
    double stdDev;
    if (total > 0) {
      final double d = total;
      stdDev = (d * sumSq - sum * sum) / d;
      if (stdDev > 0) {
        stdDev = Math.sqrt(stdDev / (d - 1.0));
      } else {
        stdDev = 0.0;
      }
    } else {
      stdDev = 0.0;
    }

    return new double[] {av, stdDev};
  }

  /**
   * Instantiates a new histogram plot.
   *
   * @param title The title to prepend to the plot name
   * @param data The data
   * @param name The name of the plotted statistic
   */
  public HistogramPlot(String title, DoubleData data, String name) {
    Objects.requireNonNull(title, "Title must not be null");
    this.data = Objects.requireNonNull(data, "Data must not be null");
    this.name = Objects.requireNonNull(name, "Name must not be null");
    plotTitle = title + " " + name;
  }

  /**
   * Show a histogram of the data.
   *
   * @return The histogram plot window (or null if the plot is not possible)
   */
  public PlotWindow show() {
    return show(null);
  }

  /**
   * Show a histogram of the data.
   *
   * <p>If the plot is shown in a new window it will be added to the provided organiser (if not
   * null).
   *
   * @param windowOrganiser the window organiser
   * @return The histogram plot window (or null if the plot is not possible)
   */
  public PlotWindow show(WindowOrganiser windowOrganiser) {
    final double[] values = data.values();
    if (!canPlot(values)) {
      return null;
    }
    final double[] limits = MathUtils.limits(values);

    // The number of bins are computed with all the data before the outliers are removed.
    // Since the number of bins is used to plot the range of the data it does not matter as
    // removal of outliers will just make the range smaller, and the number of bins would have
    // been the same or smaller with a smaller range. So the result is the histogram may have
    // slightly more bins than defined by the rule for the data plotted.
    // Computing first does allow the statistics to be cached.
    int bins = getOrComputeNumberOfBins(limits, values);

    updateLimitsToRemoveOutliers(limits, values);

    bins = updateBinsUsingMinWidth(bins, limits);

    final boolean barChart = (plotShape & Plot2.BAR) == Plot2.BAR;

    createPlotValues(limits, values, bins, barChart);

    createPlot(barChart);

    return ImageJUtils.display(plotTitle, plot, 0, windowOrganiser);
  }

  /**
   * Check the values can be plotted.
   *
   * @param values the values
   * @return true, if successful
   */
  private static boolean canPlot(double[] values) {
    return (values != null && values.length >= 2 && SimpleArrayUtils.isFinite(values));
  }

  /**
   * Get or compute the number of bins.
   *
   * @param limits the limits of the plot data
   * @param values the values of the plot data
   * @return the number of bins
   */
  private int getOrComputeNumberOfBins(double[] limits, double[] values) {
    int bins = this.numberOfBins;
    if (bins <= 0) {
      // Auto
      switch (binMethod) {
        case SCOTT:
          getStatistics(values);
          bins = (int) Math.ceil((limits[1] - limits[0])
              / getBinWidthScottsRule(stats.getStandardDeviation(), stats.size()));
          break;

        case FD:
          getStatistics(values);
          lower = stats.getStatistics().getPercentile(25);
          upper = stats.getStatistics().getPercentile(75);
          bins = (int) Math.ceil((limits[1] - limits[0])
              / getBinWidthFreedmanDiaconisRule(upper, lower, stats.size()));
          break;

        case STURGES:
          bins = getBinsSturgesRule(data.size());
          break;

        case SQRT:
        default:
          bins = getBinsSqrtRule(data.size());
          break;
      }
      // In case of error (N=0 or Infinity in the data range)
      if (bins == Integer.MAX_VALUE) {
        bins = getBinsSqrtRule(data.size());
      }
    }
    return bins;
  }

  /**
   * Update bins using the min bin width.
   *
   * @param bins the bins
   * @param limits the limits of the plot data
   * @return the bins
   */
  private int updateBinsUsingMinWidth(int bins, double[] limits) {
    final double binWidth = this.minBinWidth;
    if (binWidth > 0) {
      final double binSize = (limits[1] - limits[0]) / ((bins < 2) ? 1 : bins - 1);
      if (binSize < binWidth) {
        return (int) ((limits[1] - limits[0]) / binWidth) + 1;
      }
    }
    return bins;
  }

  /**
   * Gets the statistics.
   *
   * @param values the values
   * @return the statistics
   */
  private StoredDataStatistics getStatistics(double[] values) {
    StoredDataStatistics localStats = stats;
    if (localStats == null) {
      localStats = (data instanceof StoredDataStatistics) ? (StoredDataStatistics) data
          : StoredDataStatistics.create(values);
      stats = localStats;
    }
    return localStats;
  }

  /**
   * Update limits to remove outliers.
   *
   * @param limits the limits
   * @param values the values
   */
  private void updateLimitsToRemoveOutliers(double[] limits, double[] values) {
    switch (removeOutliersOption) {
      case 1:
        computeInterQuartileRange(values);
        final double iqr = 1.5 * (upper - lower);
        limits[0] = FastMath.max(lower - iqr, limits[0]);
        limits[1] = FastMath.min(upper + iqr, limits[1]);
        break;

      case 2:
        // Remove top 2%
        limits[1] = getStatistics(values).getStatistics().getPercentile(98);
        break;

      default:
        // Nothing to do
    }
  }

  /**
   * Compute the Inter-Quartile Range (IQR).
   *
   * @param values the values
   */
  private void computeInterQuartileRange(double[] values) {
    if (Double.isNaN(upper)) {
      final DescriptiveStatistics descriptiveStatistics = getStatistics(values).getStatistics();
      lower = descriptiveStatistics.getPercentile(25);
      upper = descriptiveStatistics.getPercentile(75);
    }
  }

  /**
   * Creates the plot values that will be passed to the Plot class.
   *
   * @param limits the limits
   * @param values the values
   * @param bins the bins
   * @param barChart true if using a bar chart
   */
  private void createPlotValues(double[] limits, double[] values, int bins,
      final boolean barChart) {
    final double[][] hist = calcHistogram(values, limits[0], limits[1], bins);
    if (barChart) {
      // Standard histogram
      plotXValues = hist[0];
      plotYValues = hist[1];
    } else {
      // Line plot of non-zero values
      int size = 0;
      plotXValues = new double[hist[0].length];
      plotYValues = new double[plotXValues.length];
      for (int i = 0; i < plotXValues.length; i++) {
        if (hist[1][i] != 0) {
          plotXValues[size] = hist[0][i];
          plotYValues[size] = hist[1][i];
          size++;
        }
      }
      plotXValues = Arrays.copyOf(plotXValues, size);
      plotYValues = Arrays.copyOf(plotYValues, size);
    }
  }

  /**
   * Creates the plot.
   *
   * @param barChart true if using a bar chart
   */
  private void createPlot(final boolean barChart) {
    plot = new Plot2(plotTitle, name, "Frequency");
    plotMinX = plotMaxX = plotMaxY = 0;
    if (plotXValues.length > 0) {
      double dx = 0;
      if (barChart) {
        dx = (plotXValues.length == 1) ? 1 : (plotXValues[1] - plotXValues[0]);
      }
      plotMaxX = plotXValues[plotXValues.length - 1] + dx;
      final double xPadding = 0.05 * (plotMaxX - plotXValues[0]);
      plotMinX = plotXValues[0] - xPadding;
      plotMaxX += xPadding;
      plotMaxY = MathUtils.max(plotYValues) * 1.05;
      plot.setLimits(plotMinX, plotMaxX, 0, plotMaxY);
    }
    plot.addPoints(plotXValues, plotYValues, plotShape);
    if (plotLabel != null) {
      plot.addLabel(0, 0, plotLabel);
    }
  }

  /**
   * Gets the minimum bin width to use (e.g. set to 1 for integer values). 0 is auto.
   *
   * @return the minimum bin width
   */
  public double getMinBinWidth() {
    return minBinWidth;
  }

  /**
   * Sets the minimum bin width to use (e.g. set to 1 for integer values). 0 is auto.
   *
   * @param minBinWidth the new minimum bin width
   */
  public void setMinBinWidth(double minBinWidth) {
    this.minBinWidth = minBinWidth;
  }

  /**
   * Gets the removes the outliers option.
   *
   * @return the removes the outliers option
   */
  public int getRemoveOutliersOption() {
    return removeOutliersOption;
  }

  /**
   * Sets the removes the outliers option.
   *
   * <ol> <li>1.5x IQR</li><li>Remove top 2%</li></ol>
   *
   * @param removeOutliersOption the new removes the outliers option
   */
  public void setRemoveOutliersOption(int removeOutliersOption) {
    this.removeOutliersOption = removeOutliersOption;
  }

  /**
   * Gets the number of bins to use. 0 is auto.
   *
   * @return the number of bins
   */
  public int getNumberOfBins() {
    return numberOfBins;
  }

  /**
   * Sets the number of bins to use. 0 is auto.
   *
   * @param numberOfBins the new number of bins
   */
  public void setNumberOfBins(int numberOfBins) {
    this.numberOfBins = Math.max(0, numberOfBins);
  }

  /**
   * Gets the plot shape.
   *
   * @return the plot shape
   */
  public int getPlotShape() {
    return plotShape;
  }

  /**
   * Sets the plot shape. Use {@link Plot2#BAR} to draw a bar chart.
   *
   * @param plotShape the new plot shape
   */
  public void setPlotShape(int plotShape) {
    this.plotShape = plotShape;
  }

  /**
   * Gets the plot label.
   *
   * @return the plot label
   */
  public String getPlotLabel() {
    return plotLabel;
  }

  /**
   * Sets the plot label.
   *
   * @param plotLabel the new plot label
   */
  public void setPlotLabel(String plotLabel) {
    this.plotLabel = plotLabel;
  }

  /**
   * Gets the bin method for computing the number of bins.
   *
   * @return the bin method
   */
  public BinMethod getBinMethod() {
    return binMethod;
  }

  /**
   * Sets the bin method for computing the number of bins.
   *
   * @param binMethod the new default method
   */
  public void setBinMethod(BinMethod binMethod) {
    this.binMethod = binMethod;
  }

  /**
   * Gets x-values from the last histogram plotted.
   *
   * @return the x-values
   * @throws NullPointerException If no plot can been created
   */
  public double[] getPlotXValues() {
    return plotXValues.clone();
  }

  /**
   * Gets y-values from the last histogram plotted.
   *
   * @return the y-values
   * @throws NullPointerException If no plot can been created
   */
  public double[] getPlotYValues() {
    return plotYValues.clone();
  }

  /**
   * Gets the min X from the last histogram plotted.
   *
   * @return the min X
   */
  public double getPlotMinX() {
    return plotMinX;
  }

  /**
   * Gets the max X from the last histogram plotted.
   *
   * @return the max X
   */
  public double getPlotMaxX() {
    return plotMaxX;
  }

  /**
   * Gets the max Y from the last histogram plotted.
   *
   * @return the max Y
   */
  public double getPlotMaxY() {
    return plotMaxY;
  }

  /**
   * Gets the plot from the last histogram plotted.
   *
   * @return the plot
   */
  public Plot2 getPlot() {
    return plot;
  }

  /**
   * Gets the plot title.
   *
   * @return the plot title
   */
  public String getPlotTitle() {
    return plotTitle;
  }

  /**
   * Gets the data.
   *
   * @return the data
   */
  public DoubleData getData() {
    return data;
  }

  /**
   * Gets the name of the plotted statistic.
   *
   * @return the name of the plotted statistic
   */
  public String getName() {
    return name;
  }
}
