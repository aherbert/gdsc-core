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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.core.ij.HistogramPlot.BinMethod;
import uk.ac.sussex.gdsc.core.ij.HistogramPlot.HistogramPlotBuilder;
import uk.ac.sussex.gdsc.core.ij.gui.Plot2;
import uk.ac.sussex.gdsc.core.utils.DoubleData;
import uk.ac.sussex.gdsc.core.utils.StoredData;

@SuppressWarnings({"javadoc"})
class HistogramPlotTest {
  @Test
  void testBuilder() {
    String title = "Test";
    HistogramPlotBuilder builder = new HistogramPlotBuilder(title);
    Assertions.assertThrows(NullPointerException.class, () -> builder.setTitle(null));
    Assertions.assertThrows(NullPointerException.class, () -> builder.setName(null));
    Assertions.assertThrows(NullPointerException.class, () -> builder.setData(null));
    StoredData data = StoredData.create(new double[] {1, 2, 3});
    builder.setData(data);
    String name = "Data";
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
    String title = "Test";
    StoredData data = StoredData.create(new double[] {1, 2, 3});
    String name = "Data";
    Assertions.assertThrows(NullPointerException.class, () -> new HistogramPlot(null, data, name));
    Assertions.assertThrows(NullPointerException.class, () -> new HistogramPlot(title, null, name));
    Assertions.assertThrows(NullPointerException.class, () -> new HistogramPlot(title, data, null));
  }

  @Test
  void testShowWithNoValues() {
    String title = "Test";
    String name = "Data";
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
}
