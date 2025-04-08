/*-
 * #%L
 * Genome Damage and Stability Centre Core ImageJ Package
 *
 * Contains core utilities for image analysis in ImageJ and is used by:
 *
 * GDSC ImageJ Plugins - Microscopy image analysis
 *
 * GDSC SMLM ImageJ Plugins - Single molecule localisation microscopy (SMLM)
 * %%
 * Copyright (C) 2011 - 2025 Alex Herbert
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

import ij.gui.Plot;
import java.util.function.DoubleConsumer;
import org.apache.commons.statistics.descriptive.Quantile;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uk.ac.sussex.gdsc.core.ij.HistogramPlot.BinMethod;
import uk.ac.sussex.gdsc.core.ij.HistogramPlot.HistogramPlotBuilder;
import uk.ac.sussex.gdsc.core.utils.DoubleData;
import uk.ac.sussex.gdsc.core.utils.MathUtils;
import uk.ac.sussex.gdsc.core.utils.SimpleArrayUtils;
import uk.ac.sussex.gdsc.core.utils.Statistics;
import uk.ac.sussex.gdsc.core.utils.StoredData;
import uk.ac.sussex.gdsc.core.utils.StoredDataStatistics;

@SuppressWarnings({"javadoc"})
class HistogramPlotTest {
  @Test
  void testBuilder() {
    final String title = "Test";
    final HistogramPlotBuilder builder = new HistogramPlotBuilder(title);
    Assertions.assertThrows(NullPointerException.class, () -> builder.setTitle(null));
    Assertions.assertThrows(NullPointerException.class, () -> builder.setName(null));
    Assertions.assertThrows(NullPointerException.class, () -> builder.setData(null));
    final StoredData data = StoredData.create(new double[] {1, 2, 3});
    builder.setData(data);
    final String name = "Data";
    builder.setName(name);
    HistogramPlot plot;
    plot = builder.build();
    Assertions.assertEquals(title + " " + name, plot.getPlotTitle());
    Assertions.assertEquals(name, plot.getName());
    Assertions.assertSame(data, plot.getData());
    Assertions.assertEquals(0, plot.getMinBinWidth());
    Assertions.assertEquals(0, plot.getRemoveOutliersOption());
    Assertions.assertEquals(0, plot.getNumberOfBins());
    Assertions.assertNull(plot.getLimits());
    Assertions.assertEquals(Plot.BAR, plot.getPlotShape());
    Assertions.assertNull(plot.getPlotLabel());
    Assertions.assertEquals(BinMethod.SCOTT, plot.getBinMethod());
    final double minBinWidth = 3.5;
    final int removeOutliersOption = 1;
    final int numberOfBins = 42;
    final double[] limits = {1, 3};
    final int plotShape = Plot.LINE;
    final String plotLabel = "my label";
    final BinMethod binMethod = BinMethod.STURGES;
    plot = builder.setMinBinWidth(minBinWidth).setRemoveOutliersOption(removeOutliersOption)
        .setNumberOfBins(numberOfBins).setLimits(limits).setPlotShape(plotShape)
        .setPlotLabel(plotLabel).setBinMethod(binMethod).build();
    Assertions.assertEquals(minBinWidth, plot.getMinBinWidth());
    Assertions.assertEquals(removeOutliersOption, plot.getRemoveOutliersOption());
    Assertions.assertEquals(numberOfBins, plot.getNumberOfBins());
    Assertions.assertArrayEquals(limits, plot.getLimits());
    Assertions.assertEquals(plotShape, plot.getPlotShape());
    Assertions.assertEquals(plotLabel, plot.getPlotLabel());
    Assertions.assertEquals(binMethod, plot.getBinMethod());
    builder.setIntegerBins(false);
    Assertions.assertEquals(0, builder.build().getMinBinWidth());
    builder.setIntegerBins(true);
    Assertions.assertEquals(1, builder.build().getMinBinWidth());
  }

  @Test
  void testConstuctorThrows() {
    final String title = "Test";
    final StoredData data = StoredData.create(new double[] {1, 2, 3});
    final String name = "Data";
    Assertions.assertThrows(NullPointerException.class, () -> new HistogramPlot(null, data, name));
    Assertions.assertThrows(NullPointerException.class, () -> new HistogramPlot(title, null, name));
    Assertions.assertThrows(NullPointerException.class, () -> new HistogramPlot(title, data, null));
  }

  @Test
  void testShowWithNoValues() {
    final String title = "Test";
    final String name = "Data";
    Assertions
        .assertNull(new HistogramPlot(title, StoredData.create(new double[] {1}), name).show());
    Assertions.assertNull(new HistogramPlot(title,
        StoredData.create(new double[] {1, Double.POSITIVE_INFINITY}), name).show());
    Assertions.assertNull(new HistogramPlot(title, new DoubleData() {
      @Override
      public double[] values() {
        return null;
      }

      @Override
      public int size() {
        return 0;
      }

      @Override
      public void forEach(DoubleConsumer action) {}
    }, name).show());
  }

  @Test
  void testGetBinsScott() {
    final StoredData data = StoredData.create(SimpleArrayUtils.newArray(10, 1.0, 1.0));
    final StoredDataStatistics stats = StoredDataStatistics.create(data.getValues());
    final double sd = stats.getStandardDeviation();
    final double width = HistogramPlot.getBinWidthScottsRule(sd, data.size());
    final int expected = (int) Math.ceil((10 - 1) / width);
    Assertions.assertEquals(expected, HistogramPlot.getBins(data, BinMethod.SCOTT));
    Assertions.assertEquals(expected, HistogramPlot.getBins(stats, BinMethod.SCOTT));
  }

  @Test
  void testGetBinsFreedmanDiaconis() {
    final StoredData data = StoredData.create(SimpleArrayUtils.newArray(10, 1.0, 1.0));
    final StoredDataStatistics stats = StoredDataStatistics.create(data.getValues());
    final double upper = 8.5;
    final double lower = 2.5;
    final double width = HistogramPlot.getBinWidthFreedmanDiaconisRule(upper, lower, data.size());
    final int expected = (int) Math.ceil((10 - 1) / width);
    Assertions.assertEquals(expected, HistogramPlot.getBins(data, BinMethod.FD));
    Assertions.assertEquals(expected, HistogramPlot.getBins(stats, BinMethod.FD));
  }

  @Test
  void testGetBinsSturges() {
    final StoredData data = StoredData.create(SimpleArrayUtils.newArray(10, 1.0, 1.0));
    final int expected = (int) Math.ceil(1 + Math.log(10) / Math.log(2));
    Assertions.assertEquals(expected, HistogramPlot.getBins(data, BinMethod.STURGES));
  }

  @Test
  void testGetBinsSqrt() {
    final StoredData data = StoredData.create(SimpleArrayUtils.newArray(10, 1.0, 1.0));
    final int expected = (int) Math.ceil(Math.sqrt(10));
    Assertions.assertEquals(expected, HistogramPlot.getBins(data, BinMethod.SQRT));
  }

  @Test
  void testGetBinWidthScottsRule() {
    final int n = 10;
    final double sd = 3.21;
    final double expected = 3.5 * sd / Math.cbrt(n);
    Assertions.assertEquals(expected, HistogramPlot.getBinWidthScottsRule(sd, n), 1e-10);
  }

  @Test
  void testGetBinWidthFreedmanDiaconisRule() {
    final double upper = 10.78;
    final double lower = 3.45;
    final int n = 10;
    final double expected = 2 * (upper - lower) / Math.cbrt(n);
    Assertions.assertEquals(expected,
        HistogramPlot.getBinWidthFreedmanDiaconisRule(upper, lower, n), 1e-10);
  }

  @Test
  void testGetBinsSturgesRule() {
    for (final int n : new int[] {3, 5, 10}) {
      final double expected = (int) Math.ceil(1 + Math.log(n) / Math.log(2.0));
      Assertions.assertEquals(expected, HistogramPlot.getBinsSturgesRule(n), 1e-10);
    }
  }

  @Test
  void testGetBinsSqrtRule() {
    for (final int n : new int[] {3, 5, 10}) {
      final double expected = (int) Math.ceil(Math.sqrt(n));
      Assertions.assertEquals(expected, HistogramPlot.getBinsSqrtRule(n), 1e-10);
    }
  }

  @Test
  void testCalcHistogramFloat() {
    final float[] data = {1, 2, 3, 4, 5, 3, 2, 2, 1};
    //@formatter:off
    Assertions.assertArrayEquals(new float[][] {
      {1, 2, 3, 4, 5},
      {2, 3, 2, 1, 1},
    }, HistogramPlot.calcHistogram(data, 5));
    // With min/max reversed
    Assertions.assertArrayEquals(new float[][] {
      {2, 4, 6},
      {5, 3, 1},
    }, HistogramPlot.calcHistogram(data, 5, 1, 3));
    // With no bins: the number of bins are set to 2
    Assertions.assertArrayEquals(new float[][] {
      {3, 7},
      {8, 1},
    }, HistogramPlot.calcHistogram(data, 0));
    // With no data range the number of bins are set to 1
    Assertions.assertArrayEquals(new float[][] {
      {3},
      {4},
    }, HistogramPlot.calcHistogram(new float[] {3, 3, 3, 3}, 0));
    // Ignore data outside the range
    Assertions.assertArrayEquals(new float[][] {
      {2, 3, 4},
      {3, 2, 1},
    }, HistogramPlot.calcHistogram(data, 2, 4, 3));
    // No data
    Assertions.assertArrayEquals(new float[0][0], HistogramPlot.calcHistogram(new float[0], 0));
    // Single data point
    Assertions.assertArrayEquals(new float[][] {
      {3},
      {1},
    }, HistogramPlot.calcHistogram(new float[] {3}, 0));
    // Test bin is in middle of bin width.
    // This is better for plotting with ImageJ Plot.BAR.
    // However this may not be desirable for analysis so could change in the future.
    final float[] data2 = {0.25f, 0.5f, 0.5f, 0.75f, 1.0f};
    Assertions.assertArrayEquals(new float[][] {
      {0.4375f, 0.8125f, 1.1875f},
      {3, 1, 1},
    }, HistogramPlot.calcHistogram(data2, 3));
    Assertions.assertArrayEquals(new float[][] {
      {0.375f, 0.625f, 0.875f, 1.125f},
      {1, 2, 1, 1},
    }, HistogramPlot.calcHistogram(data2, 4));
    final float[] data3 = {0.5f, 0.5f, 1.5f, 2.5f, 3.5f};
    Assertions.assertArrayEquals(new float[][] {
      {1, 2, 3, 4},
      {2, 1, 1, 1},
    }, HistogramPlot.calcHistogram(data3, 4));
    //@formatter:on
  }

  @Test
  void testCalcHistogramDouble() {
    final double[] data = {1, 2, 3, 4, 5, 3, 2, 2, 1};
    //@formatter:off
    Assertions.assertArrayEquals(new double[][] {
      {1, 2, 3, 4, 5},
      {2, 3, 2, 1, 1},
    }, HistogramPlot.calcHistogram(data, 5));
    // With min/max reversed
    Assertions.assertArrayEquals(new double[][] {
      {2, 4, 6},
      {5, 3, 1},
    }, HistogramPlot.calcHistogram(data, 5, 1, 3));
    // With no bins: the number of bins are set to 2
    Assertions.assertArrayEquals(new double[][] {
      {3, 7},
      {8, 1},
    }, HistogramPlot.calcHistogram(data, 0));
    // With no data range the number of bins are set to 1
    Assertions.assertArrayEquals(new double[][] {
      {3},
      {4},
    }, HistogramPlot.calcHistogram(new double[] {3, 3, 3, 3}, 0));
    // Ignore data outside the range
    Assertions.assertArrayEquals(new double[][] {
      {2, 3, 4},
      {3, 2, 1},
    }, HistogramPlot.calcHistogram(data, 2, 4, 3));
    // No data
    Assertions.assertArrayEquals(new double[0][0], HistogramPlot.calcHistogram(new double[0], 0));
    // Single data point
    Assertions.assertArrayEquals(new double[][] {
      {3},
      {1},
    }, HistogramPlot.calcHistogram(new double[] {3}, 0));
    // Test bin is in middle of bin width.
    // This is better for plotting with ImageJ Plot.BAR.
    // However this may not be desirable for analysis so could change in the future.
    final double[] data2 = {0.25, 0.5, 0.5, 0.75, 1.0};
    Assertions.assertArrayEquals(new double[][] {
      {0.4375, 0.8125, 1.1875},
      {3, 1, 1},
    }, HistogramPlot.calcHistogram(data2, 3));
    Assertions.assertArrayEquals(new double[][] {
      {0.375, 0.625, 0.875, 1.125},
      {1, 2, 1, 1},
    }, HistogramPlot.calcHistogram(data2, 4));
    final double[] data3 = {0.5, 0.5, 1.5, 2.5, 3.5};
    Assertions.assertArrayEquals(new double[][] {
      {1, 2, 3, 4},
      {2, 1, 1, 1},
    }, HistogramPlot.calcHistogram(data3, 4));
    //@formatter:on
  }

  @Test
  void testGetHistogramStatistics() {
    final float[] x = {1, 2, 3, 4, 5};
    final float[] y = {2, 3, 2, 1, 1};
    final Statistics stats = Statistics.create(new float[] {1, 2, 3, 4, 5, 3, 2, 2, 1});
    double[] result = HistogramPlot.getHistogramStatistics(x, y);
    Assertions.assertEquals(stats.getMean(), result[0]);
    Assertions.assertEquals(stats.getStandardDeviation(), result[1]);
    // Empty
    stats.reset();
    final float[] empty = {};
    result = HistogramPlot.getHistogramStatistics(empty, empty);
    Assertions.assertEquals(stats.getMean(), result[0]);
    Assertions.assertEquals(stats.getStandardDeviation(), result[1]);
    result = HistogramPlot.getHistogramStatistics(new float[] {3, 5, 7}, new float[] {0, 0, 0});
    Assertions.assertEquals(stats.getMean(), result[0]);
    Assertions.assertEquals(stats.getStandardDeviation(), result[1]);
    // Single item
    stats.add(3);
    final float[] single = {3};
    result = HistogramPlot.getHistogramStatistics(single, new float[] {1});
    Assertions.assertEquals(stats.getMean(), result[0]);
    Assertions.assertEquals(stats.getStandardDeviation(), result[1]);
    // zero counts
    result = HistogramPlot.getHistogramStatistics(new float[] {3, 5, 7}, new float[] {1, 0, 0});
    Assertions.assertEquals(stats.getMean(), result[0]);
    Assertions.assertEquals(stats.getStandardDeviation(), result[1]);
  }

  @Test
  void testCanPlot() {
    Assertions.assertFalse(HistogramPlot.canPlot(null));
    Assertions.assertFalse(HistogramPlot.canPlot(new double[0]));
    Assertions.assertFalse(HistogramPlot.canPlot(new double[1]));
    Assertions.assertFalse(HistogramPlot.canPlot(new double[] {Double.NaN, 0}));
    Assertions.assertFalse(HistogramPlot.canPlot(new double[] {Double.POSITIVE_INFINITY, 0}));
    Assertions.assertTrue(HistogramPlot.canPlot(new double[] {1, 2}));
  }

  @Test
  void testGetOrComputeNumberOfBins() {
    final double[] values = {1, 2, 3, 4, 5, 3, 2, 2, 1};
    final double[] limits = MathUtils.limits(values);
    final StoredData data = StoredData.create(values);
    final HistogramPlotBuilder builder =
        new HistogramPlot.HistogramPlotBuilder("title", data, "name");
    // Pre-configured
    builder.setNumberOfBins(13);
    HistogramPlot plot = builder.build();
    Assertions.assertEquals(13, plot.getOrComputeNumberOfBins(limits, values));
    // test bin methods
    builder.setNumberOfBins(0);
    for (final BinMethod binMethod : BinMethod.values()) {
      builder.setBinMethod(binMethod);
      plot = builder.build();
      Assertions.assertEquals(HistogramPlot.getBins(data, binMethod),
          plot.getOrComputeNumberOfBins(limits, values));
    }
    // Test with infinity in the data range. It should default to sqrt rule
    data.add(Double.POSITIVE_INFINITY);
    builder.setBinMethod(BinMethod.SCOTT);
    plot = builder.build();
    Assertions.assertEquals(HistogramPlot.getBins(data, BinMethod.SQRT),
        plot.getOrComputeNumberOfBins(limits, data.getValues()));
  }

  @Test
  void testUpdateBinsUsingMinWidth() {
    final double[] limits = {1, 7};
    for (final int bins : new int[] {3, 5, 13}) {
      Assertions.assertEquals(bins, HistogramPlot.updateBinsUsingMinWidth(bins, limits, 0));
    }
    Assertions.assertEquals(6, HistogramPlot.updateBinsUsingMinWidth(6, limits, 0.1));
    Assertions.assertEquals(6, HistogramPlot.updateBinsUsingMinWidth(6, limits, 1));
    Assertions.assertEquals(4, HistogramPlot.updateBinsUsingMinWidth(6, limits, 2));
    Assertions.assertEquals(3, HistogramPlot.updateBinsUsingMinWidth(6, limits, 3));
    Assertions.assertEquals(1, HistogramPlot.updateBinsUsingMinWidth(1, limits, 2));
    Assertions.assertEquals(1, HistogramPlot.updateBinsUsingMinWidth(1, limits, 10));
  }

  @Test
  void testUpdateLimitsToRemoveOutliers() {
    double[] values = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 100};
    double[] limits = MathUtils.limits(values);

    final StoredData data = StoredData.create(values);
    final HistogramPlotBuilder builder =
        new HistogramPlot.HistogramPlotBuilder("title", data, "name");

    builder.setRemoveOutliersOption(0);
    HistogramPlot plot = builder.build();
    double[] limits2 = limits.clone();
    plot.updateLimitsToRemoveOutliers(limits2, values);
    Assertions.assertArrayEquals(limits, limits2);

    // Inter-quartile range: 3 to 9 = 6
    builder.setRemoveOutliersOption(1);
    plot = builder.build();
    plot.updateLimitsToRemoveOutliers(limits2, values);
    // lower limit is the same, upper limit is clipped
    Assertions.assertEquals(limits[0], limits2[0]);
    Assertions.assertTrue(limits[1] > limits2[1]);

    values[0] = -100;
    values[values.length - 1] = values.length + 1;
    limits = MathUtils.limits(values);
    limits2 = limits.clone();
    plot.updateLimitsToRemoveOutliers(limits2, values);
    // limit is clipped
    Assertions.assertEquals(limits[1], limits2[1]);
    Assertions.assertTrue(limits[0] < limits2[0]);

    // Percentile
    builder.setRemoveOutliersOption(2);
    plot = builder.build();

    limits2 = limits.clone();
    plot.updateLimitsToRemoveOutliers(limits2, values);
    Assertions.assertArrayEquals(
        new double[] {limits[0], Quantile.withDefaults().evaluate(values, 0.98)}, limits2);

    // Create larger data
    values = SimpleArrayUtils.newArray(100, 0, 1.0);
    builder.setData(StoredData.create(values));
    plot = builder.build();
    limits2 = new double[] {0, 99};
    plot.updateLimitsToRemoveOutliers(limits2, values);
    Assertions.assertArrayEquals(new double[] {0, Quantile.withDefaults().evaluate(values, 0.98)},
        limits2);
  }

  @Test
  void testCreatePlotValues() {
    final StoredData dummy = StoredData.create(new double[] {});
    final HistogramPlot plot = new HistogramPlot("title", dummy, "name");
    final double[] values = {1, 2, 2, 2, 3, 4, 5, 5, 10};
    final double[] limits = MathUtils.limits(values);
    final int bins = 10;
    final double[][] hist = HistogramPlot.calcHistogram(values, limits[0], limits[1], bins);

    plot.createPlotValues(limits, values, bins);
    Assertions.assertArrayEquals(hist[0], plot.getPlotXValues());
    Assertions.assertArrayEquals(hist[1], plot.getPlotYValues());
  }

  @Test
  void testIsIntegerBins() {
    Assertions.assertFalse(HistogramPlot.isIntegerBins(new double[] {}));
    Assertions.assertFalse(HistogramPlot.isIntegerBins(new double[] {0.5}));
    Assertions.assertTrue(HistogramPlot.isIntegerBins(new double[] {1.0}));
    Assertions.assertFalse(HistogramPlot.isIntegerBins(new double[] {0.5, 1.5}));
    Assertions.assertTrue(HistogramPlot.isIntegerBins(new double[] {1, 2}));
    Assertions.assertFalse(HistogramPlot.isIntegerBins(new double[] {1, 3}));
  }

  @Test
  void testSetLimits() {
    final StoredData dummy = StoredData.create(new double[] {});
    final HistogramPlot plot = new HistogramPlot("title", dummy, "name");
    Assertions.assertNull(plot.getLimits());
    Assertions.assertThrows(IllegalArgumentException.class, () -> plot.setLimits(new double[0]));
    Assertions.assertThrows(IllegalArgumentException.class, () -> plot.setLimits(new double[1]));
    Assertions.assertThrows(IllegalArgumentException.class, () -> plot.setLimits(new double[3]));
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> plot.setLimits(new double[] {1, 0}));
    for (final double bad : new double[] {Double.NaN, Double.NEGATIVE_INFINITY,
        Double.POSITIVE_INFINITY}) {
      Assertions.assertThrows(IllegalArgumentException.class,
          () -> plot.setLimits(new double[] {0, bad}));
      Assertions.assertThrows(IllegalArgumentException.class,
          () -> plot.setLimits(new double[] {bad, 0}));
    }
    final double[] limits = {0, 1};
    plot.setLimits(limits);
    Assertions.assertArrayEquals(limits, plot.getLimits());
  }

  @Test
  void testCreatePlot() {
    final StoredData dummy = StoredData.create(new double[] {});
    final HistogramPlot plot = new HistogramPlot("title", dummy, "name");
    double[] values = {1, 2, 2, 2, 3, 4, 5, 5};
    double[] limits = MathUtils.limits(values);

    // Integer bins
    int bins = (int) (limits[1] - limits[0] + 1);
    plot.setMinBinWidth(1);
    plot.createPlotValues(limits, values, bins);
    plot.createPlot();
    // Test for padding
    double[][] hist = HistogramPlot.calcHistogram(values, limits[0], limits[1], bins);
    double[] x = hist[0];
    double[] y = hist[1];
    Assertions.assertTrue(x[x.length - 1] <= plot.getPlotMaxX());
    Assertions.assertTrue(x[0] >= plot.getPlotMinX());
    Assertions.assertTrue(MathUtils.max(y) <= plot.getPlotMaxY());
    Assertions.assertNotNull(plot.getPlot());

    // Non-integer bins
    bins = 0;
    plot.setMinBinWidth(2);
    plot.createPlotValues(limits, values, bins);
    plot.createPlot();
    // Test for padding
    hist = HistogramPlot.calcHistogram(values, limits[0], limits[1], bins);
    x = hist[0];
    y = hist[1];
    Assertions.assertTrue(x[x.length - 1] <= plot.getPlotMaxX());
    Assertions.assertTrue(x[0] >= plot.getPlotMinX());
    Assertions.assertTrue(MathUtils.max(y) <= plot.getPlotMaxY());
    Assertions.assertNotNull(plot.getPlot());

    // Single bar with label
    values = new double[] {values[0]};
    limits = MathUtils.limits(values);

    plot.setPlotLabel("my label");
    plot.createPlotValues(limits, values, bins);
    plot.createPlot();
    // Test for padding
    hist = HistogramPlot.calcHistogram(values, limits[0], limits[1], bins);
    x = hist[0];
    y = hist[1];
    Assertions.assertTrue(x[x.length - 1] <= plot.getPlotMaxX());
    Assertions.assertTrue(x[0] >= plot.getPlotMinX());
    Assertions.assertTrue(MathUtils.max(y) <= plot.getPlotMaxY());
    Assertions.assertNotNull(plot.getPlot());
  }

  @Test
  void testDrawWithNoData() {
    final StoredData dummy = StoredData.create(new double[] {});
    final HistogramPlot plot = new HistogramPlot("title", dummy, "name");
    Assertions.assertNull(plot.draw());
  }

  @Test
  void testDraw() {
    final double[] values = {1, 2, 2, 2, 3, 4, 5, 5, 10};
    final double[] limits = MathUtils.limits(values);
    final int bins = 10;
    final double[][] hist = HistogramPlot.calcHistogram(values, limits[0], limits[1], bins);

    final HistogramPlot plot = new HistogramPlot("title", StoredData.create(values), "name");
    plot.setNumberOfBins(bins);
    Assertions.assertNull(plot.getPlot());
    Assertions.assertNotNull(plot.draw());

    Assertions.assertArrayEquals(hist[0], plot.getPlotXValues());
    Assertions.assertArrayEquals(hist[1], plot.getPlotYValues());
    Assertions.assertArrayEquals(limits, plot.getLimits());
    Assertions.assertEquals("title name", plot.getPlot().getTitle());
  }

  @Test
  void testDrawWithLimits() {
    final double[] values = {1, 2, 2, 2, 3, 4, 5, 5, 10};
    final double[] limits = {2, 5};
    final int bins = 4;
    final double[][] hist = HistogramPlot.calcHistogram(values, limits[0], limits[1], bins);

    final HistogramPlot plot = new HistogramPlot("title", StoredData.create(values), "name");
    plot.setNumberOfBins(bins);
    plot.setLimits(limits);
    Assertions.assertNull(plot.getPlot());
    Assertions.assertNotNull(plot.draw());

    Assertions.assertArrayEquals(hist[0], plot.getPlotXValues());
    Assertions.assertArrayEquals(hist[1], plot.getPlotYValues());
    Assertions.assertArrayEquals(limits, plot.getLimits());
    Assertions.assertEquals("title name", plot.getPlot().getTitle());
  }

  @ParameterizedTest
  @CsvSource(value = {
      "123, 123",
      "a^b, a^b",
      "a^^b, a^^b",
      // superscript is replaced by a single '^'
      "a^^b^^, a^b",
      "a^^b^^c, a^bc",
      // subscript has no replacement character
      "a!b, a!b",
      "a!!b, a!!b",
      "a!!b!!, ab",
      "a!!b!!c, abc",
      // both
      "a^^b^^!!c!!, a^bc",
  })
  void testStripFormatting(String in, String out) {
    Assertions.assertEquals(out, HistogramPlot.stripFormatting(in));
  }
}
