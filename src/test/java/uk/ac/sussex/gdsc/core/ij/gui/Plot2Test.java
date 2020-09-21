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

package uk.ac.sussex.gdsc.core.ij.gui;

import ij.gui.Plot;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"javadoc"})
class Plot2Test {
  @Test
  void testFloatArrayConstructor() {
    final String title = "title";
    final String xLabel = "x";
    final String yLabel = "y";
    final float[] x = {0, 1, 2};
    final float[] y = {6, 5, 4};
    final Plot2 p1 = new Plot2(title, xLabel, yLabel);
    p1.addPoints(x, y, Plot.LINE);
    final Plot2 p2 = new Plot2(title, xLabel, yLabel, x, y);
    Assertions.assertArrayEquals(p1.getDataObjectArrays(0), p2.getDataObjectArrays(0));
  }

  @Test
  void testDoubleArrayConstructor() {
    final String title = "title";
    final String xLabel = "x";
    final String yLabel = "y";
    final double[] x = {0, 1, 2};
    final double[] y = {6, 5, 4};
    final Plot2 p1 = new Plot2(title, xLabel, yLabel);
    p1.addPoints(x, y, Plot.LINE);
    final Plot2 p2 = new Plot2(title, xLabel, yLabel, x, y);
    Assertions.assertArrayEquals(p1.getDataObjectArrays(0), p2.getDataObjectArrays(0));
  }

  @Test
  void testAddPointsWithNull() {
    final String title = "title";
    final String xLabel = "x";
    final String yLabel = "y";
    final float[] x = {0, 1, 2};
    final float[] y = {6, 5, 4};
    final Plot2 p1 = new Plot2(title, xLabel, yLabel);
    final Plot2 p2 = new Plot2(title, xLabel, yLabel);
    final Plot2 p3 = new Plot2(title, xLabel, yLabel);
    p1.addPoints(x, y, Plot2.BAR);
    // Using null or empty should match a natural order
    p2.addPoints(null, y, Plot2.BAR);
    p3.addPoints(new float[0], y, Plot2.BAR);
    Assertions.assertArrayEquals(p1.getDataObjectArrays(0), p2.getDataObjectArrays(0));
    Assertions.assertArrayEquals(p1.getDataObjectArrays(0), p3.getDataObjectArrays(0));
  }
}
