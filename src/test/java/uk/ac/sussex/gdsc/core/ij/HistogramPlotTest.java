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

import ij.gui.Plot;
import java.util.function.DoubleConsumer;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.core.ij.HistogramPlot.BinMethod;
import uk.ac.sussex.gdsc.core.ij.HistogramPlot.HistogramPlotBuilder;
import uk.ac.sussex.gdsc.core.ij.gui.Plot2;
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
    Assertions.assertEquals(Plot2.BAR, plot.getPlotShape());
    Assertions.assertEquals(null, plot.getPlotLabel());
    Assertions.assertEquals(BinMethod.SCOTT, plot.getBinMethod());
    final double minBinWidth = 3.5;
    final int removeOutliersOption = 1;
    final int numberOfBins = 42;
    final int plotShape = Plot.LINE;
    final String plotLabel = "my label";
    final BinMethod binMethod = BinMethod.STURGES;
    plot = builder.setMinBinWidth(minBinWidth).setRemoveOutliersOption(removeOutliersOption)
        .setNumberOfBins(numberOfBins).setPlotShape(plotShape).setPlotLabel(plotLabel)
        .setBinMethod(binMethod).build();
    Assertions.assertEquals(minBinWidth, plot.getMinBinWidth());
    Assertions.assertEquals(removeOutliersOption, plot.getRemoveOutliersOption());
    Assertions.assertEquals(numberOfBins, plot.getNumberOfBins());
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
      {1, 3, 5},
      {5, 3, 1},
    }, HistogramPlot.calcHistogram(data, 5, 1, 3));
    // With no bins: the number of bins are set to 2
    Assertions.assertArrayEquals(new float[][] {
      {1, 5},
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
      {1, 3, 5},
      {5, 3, 1},
    }, HistogramPlot.calcHistogram(data, 5, 1, 3));
    // With no bins: the number of bins are set to 2
    Assertions.assertArrayEquals(new double[][] {
      {1, 5},
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
    //@formatter:on
  }

  @Test
  void testCreateHistogramAxisFloat() {
    Assertions.assertArrayEquals(new float[] {1, 1, 3, 3, 5, 5},
        HistogramPlot.createHistogramAxis(new float[] {1, 3}));
    Assertions.assertArrayEquals(new float[] {3, 3, 4, 4},
        HistogramPlot.createHistogramAxis(new float[] {3}));
    Assertions.assertArrayEquals(new float[] {0, 0},
        HistogramPlot.createHistogramAxis(new float[] {}));
  }

  @Test
  void testCreateHistogramAxisDouble() {
    Assertions.assertArrayEquals(new double[] {1, 1, 3, 3, 5, 5},
        HistogramPlot.createHistogramAxis(new double[] {1, 3}));
    Assertions.assertArrayEquals(new double[] {3, 3, 4, 4},
        HistogramPlot.createHistogramAxis(new double[] {3}));
    Assertions.assertArrayEquals(new double[] {0, 0},
        HistogramPlot.createHistogramAxis(new double[] {}));
  }

  @Test
  void testCreateHistogramValuesFloat() {
    Assertions.assertArrayEquals(new float[] {0, 1, 1, 3, 3, 0},
        HistogramPlot.createHistogramValues(new float[] {1, 3}));
    Assertions.assertArrayEquals(new float[] {0, 3, 3, 0},
        HistogramPlot.createHistogramValues(new float[] {3}));
    Assertions.assertArrayEquals(new float[] {0, 0},
        HistogramPlot.createHistogramValues(new float[] {}));
  }

  @Test
  void testCreateHistogramValuesDouble() {
    Assertions.assertArrayEquals(new double[] {0, 1, 1, 3, 3, 0},
        HistogramPlot.createHistogramValues(new double[] {1, 3}));
    Assertions.assertArrayEquals(new double[] {0, 3, 3, 0},
        HistogramPlot.createHistogramValues(new double[] {3}));
    Assertions.assertArrayEquals(new double[] {0, 0},
        HistogramPlot.createHistogramValues(new double[] {}));
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
  void testGetStatistics() {
    final double[] values = {1, 2, 3, 4, 5, 3, 2, 2, 1};
    HistogramPlot plot = new HistogramPlot("title", StoredData.create(values), "name");
    final StoredDataStatistics s1 = plot.getStatistics(values);
    StoredDataStatistics s2 = plot.getStatistics(values);
    Assertions.assertSame(s1, s2);
    plot = new HistogramPlot("title", s1, "name");
    s2 = plot.getStatistics(values);
    Assertions.assertSame(s1, s2);
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
    Assertions.assertArrayEquals(new double[] {limits[0], 9 + 6 * 1.5}, limits2);

    values[0] = -100;
    values[values.length - 1] = values.length + 1;
    limits = MathUtils.limits(values);
    limits2 = limits.clone();
    plot.updateLimitsToRemoveOutliers(limits2, values);
    // limit is clipped
    Assertions.assertArrayEquals(new double[] {3 - 6 * 1.5, limits[1]}, limits2);

    // Percentile
    builder.setRemoveOutliersOption(2);
    plot = builder.build();

    limits2 = limits.clone();
    plot.updateLimitsToRemoveOutliers(limits2, values);
    Assertions.assertArrayEquals(new double[] {limits[0], new Percentile().evaluate(values, 98)},
        limits2);

    // Create larger data
    values = SimpleArrayUtils.newArray(100, 0, 1.0);
    builder.setData(StoredData.create(values));
    plot = builder.build();
    limits2 = new double[] {0, 99};
    plot.updateLimitsToRemoveOutliers(limits2, values);
    Assertions.assertArrayEquals(new double[] {0, new Percentile().evaluate(values, 98)}, limits2);
  }

  @Test
  void testCreatePlotValues() {
    final StoredData dummy = StoredData.create(new double[] {});
    final HistogramPlot plot = new HistogramPlot("title", dummy, "name");
    final double[] values = {1, 2, 2, 2, 3, 4, 5, 5, 10};
    final double[] limits = MathUtils.limits(values);
    final int bins = 10;
    final double[][] hist = HistogramPlot.calcHistogram(values, limits[0], limits[1], bins);

    // Bar chart
    plot.createPlotValues(limits, values, bins, true);
    Assertions.assertArrayEquals(hist[0], plot.getPlotXValues());
    Assertions.assertArrayEquals(hist[1], plot.getPlotYValues());

    // Line plot of non-zero values
    plot.createPlotValues(limits, values, bins, false);
    Assertions.assertArrayEquals(new double[] {1, 2, 3, 4, 5, 10}, plot.getPlotXValues());
    Assertions.assertArrayEquals(new double[] {1, 3, 1, 1, 2, 1}, plot.getPlotYValues());
  }

  @Test
  void testCreatePlot() {
    final StoredData dummy = StoredData.create(new double[] {});
    final HistogramPlot plot = new HistogramPlot("title", dummy, "name");
    double[] values = {1, 2, 2, 2, 3, 4, 5, 5};
    double[] limits = MathUtils.limits(values);
    final int bins = 0;

    // Bar chart
    plot.createPlotValues(limits, values, bins, true);
    plot.createPlot(true);
    // Test for padding
    double[][] hist = HistogramPlot.calcHistogram(values, limits[0], limits[1], bins);
    double[] x = hist[0];
    double[] y = hist[1];
    Assertions.assertTrue(x[x.length - 1] < plot.getPlotMaxX());
    Assertions.assertTrue(x[0] > plot.getPlotMinX());
    Assertions.assertTrue(MathUtils.max(y) < plot.getPlotMaxY());
    Plot p = plot.getPlot();
    Assertions.assertArrayEquals(
        new double[] {plot.getPlotMinX(), plot.getPlotMaxX(), 0, plot.getPlotMaxY()},
        p.getLimits());

    // Line chart
    plot.createPlotValues(limits, values, bins, false);
    plot.createPlot(false);
    // Test for padding
    Assertions.assertTrue(x[x.length - 1] < plot.getPlotMaxX());
    Assertions.assertTrue(x[0] > plot.getPlotMinX());
    Assertions.assertTrue(MathUtils.max(y) < plot.getPlotMaxY());
    p = plot.getPlot();
    Assertions.assertArrayEquals(
        new double[] {plot.getPlotMinX(), plot.getPlotMaxX(), 0, plot.getPlotMaxY()},
        p.getLimits());

    // Single bar with label
    values = new double[] {values[0]};
    limits = MathUtils.limits(values);

    plot.setPlotLabel("my label");
    plot.createPlotValues(limits, values, bins, true);
    plot.createPlot(true);
    // Test for padding
    hist = HistogramPlot.calcHistogram(values, limits[0], limits[1], bins);
    x = hist[0];
    y = hist[1];
    Assertions.assertTrue(x[x.length - 1] < plot.getPlotMaxX());
    Assertions.assertTrue(x[0] > plot.getPlotMinX());
    Assertions.assertTrue(MathUtils.max(y) < plot.getPlotMaxY());
    p = plot.getPlot();
    Assertions.assertArrayEquals(
        new double[] {plot.getPlotMinX(), plot.getPlotMaxX(), 0, plot.getPlotMaxY()},
        p.getLimits());
  }
}