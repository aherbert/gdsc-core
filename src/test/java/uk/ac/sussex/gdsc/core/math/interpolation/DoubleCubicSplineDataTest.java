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

package uk.ac.sussex.gdsc.core.math.interpolation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.core.utils.SimpleArrayUtils;

/**
 * Test for {@link DoubleCubicSplineData}.
 */
@SuppressWarnings({"javadoc"})
public class DoubleCubicSplineDataTest {
  @Test
  public void testToArray() {
    final double[] exp = SimpleArrayUtils.newArray(64, 1.0, 1.0);
    DoubleCubicSplineData data = new DoubleCubicSplineData(exp);
    final double[] obs = new double[64];
    data.toArray(obs);
    Assertions.assertArrayEquals(exp, obs);
  }

  @Test
  public void testScale() {
    final double[] exp = SimpleArrayUtils.newArray(64, 1.0, 1.0);
    final int scale = 3;
    DoubleCubicSplineData data = new DoubleCubicSplineData(exp).scale(scale);
    final double[] obs = new double[64];
    data.toArray(obs);
    SimpleArrayUtils.multiply(exp, scale);
    Assertions.assertArrayEquals(exp, obs);
  }

  @Test
  public void testCubicSplineConstructor() {
    final CubicSplinePosition x = new CubicSplinePosition(0.25);
    final CubicSplinePosition y = new CubicSplinePosition(0.5);
    final CubicSplinePosition z = new CubicSplinePosition(0.125);
    final DoubleCubicSplineData data = new DoubleCubicSplineData(x, y, z);
    final double x0 = 1;
    final double x1 = x.x1;
    final double x2 = x.x2;
    final double x3 = x.x3;
    final double y0 = 1;
    final double y1 = y.x1;
    final double y2 = y.x2;
    final double y3 = y.x3;
    final double z0 = 1;
    final double z1 = z.x1;
    final double z2 = z.x2;
    final double z3 = z.x3;
    Assertions.assertEquals(x0 * y0 * z0, data.x0y0z0);
    Assertions.assertEquals(x1 * y0 * z0, data.x1y0z0);
    Assertions.assertEquals(x2 * y0 * z0, data.x2y0z0);
    Assertions.assertEquals(x3 * y0 * z0, data.x3y0z0);
    Assertions.assertEquals(x0 * y1 * z0, data.x0y1z0);
    Assertions.assertEquals(x1 * y1 * z0, data.x1y1z0);
    Assertions.assertEquals(x2 * y1 * z0, data.x2y1z0);
    Assertions.assertEquals(x3 * y1 * z0, data.x3y1z0);
    Assertions.assertEquals(x0 * y2 * z0, data.x0y2z0);
    Assertions.assertEquals(x1 * y2 * z0, data.x1y2z0);
    Assertions.assertEquals(x2 * y2 * z0, data.x2y2z0);
    Assertions.assertEquals(x3 * y2 * z0, data.x3y2z0);
    Assertions.assertEquals(x0 * y3 * z0, data.x0y3z0);
    Assertions.assertEquals(x1 * y3 * z0, data.x1y3z0);
    Assertions.assertEquals(x2 * y3 * z0, data.x2y3z0);
    Assertions.assertEquals(x3 * y3 * z0, data.x3y3z0);
    Assertions.assertEquals(x0 * y0 * z1, data.x0y0z1);
    Assertions.assertEquals(x1 * y0 * z1, data.x1y0z1);
    Assertions.assertEquals(x2 * y0 * z1, data.x2y0z1);
    Assertions.assertEquals(x3 * y0 * z1, data.x3y0z1);
    Assertions.assertEquals(x0 * y1 * z1, data.x0y1z1);
    Assertions.assertEquals(x1 * y1 * z1, data.x1y1z1);
    Assertions.assertEquals(x2 * y1 * z1, data.x2y1z1);
    Assertions.assertEquals(x3 * y1 * z1, data.x3y1z1);
    Assertions.assertEquals(x0 * y2 * z1, data.x0y2z1);
    Assertions.assertEquals(x1 * y2 * z1, data.x1y2z1);
    Assertions.assertEquals(x2 * y2 * z1, data.x2y2z1);
    Assertions.assertEquals(x3 * y2 * z1, data.x3y2z1);
    Assertions.assertEquals(x0 * y3 * z1, data.x0y3z1);
    Assertions.assertEquals(x1 * y3 * z1, data.x1y3z1);
    Assertions.assertEquals(x2 * y3 * z1, data.x2y3z1);
    Assertions.assertEquals(x3 * y3 * z1, data.x3y3z1);
    Assertions.assertEquals(x0 * y0 * z2, data.x0y0z2);
    Assertions.assertEquals(x1 * y0 * z2, data.x1y0z2);
    Assertions.assertEquals(x2 * y0 * z2, data.x2y0z2);
    Assertions.assertEquals(x3 * y0 * z2, data.x3y0z2);
    Assertions.assertEquals(x0 * y1 * z2, data.x0y1z2);
    Assertions.assertEquals(x1 * y1 * z2, data.x1y1z2);
    Assertions.assertEquals(x2 * y1 * z2, data.x2y1z2);
    Assertions.assertEquals(x3 * y1 * z2, data.x3y1z2);
    Assertions.assertEquals(x0 * y2 * z2, data.x0y2z2);
    Assertions.assertEquals(x1 * y2 * z2, data.x1y2z2);
    Assertions.assertEquals(x2 * y2 * z2, data.x2y2z2);
    Assertions.assertEquals(x3 * y2 * z2, data.x3y2z2);
    Assertions.assertEquals(x0 * y3 * z2, data.x0y3z2);
    Assertions.assertEquals(x1 * y3 * z2, data.x1y3z2);
    Assertions.assertEquals(x2 * y3 * z2, data.x2y3z2);
    Assertions.assertEquals(x3 * y3 * z2, data.x3y3z2);
    Assertions.assertEquals(x0 * y0 * z3, data.x0y0z3);
    Assertions.assertEquals(x1 * y0 * z3, data.x1y0z3);
    Assertions.assertEquals(x2 * y0 * z3, data.x2y0z3);
    Assertions.assertEquals(x3 * y0 * z3, data.x3y0z3);
    Assertions.assertEquals(x0 * y1 * z3, data.x0y1z3);
    Assertions.assertEquals(x1 * y1 * z3, data.x1y1z3);
    Assertions.assertEquals(x2 * y1 * z3, data.x2y1z3);
    Assertions.assertEquals(x3 * y1 * z3, data.x3y1z3);
    Assertions.assertEquals(x0 * y2 * z3, data.x0y2z3);
    Assertions.assertEquals(x1 * y2 * z3, data.x1y2z3);
    Assertions.assertEquals(x2 * y2 * z3, data.x2y2z3);
    Assertions.assertEquals(x3 * y2 * z3, data.x3y2z3);
    Assertions.assertEquals(x0 * y3 * z3, data.x0y3z3);
    Assertions.assertEquals(x1 * y3 * z3, data.x1y3z3);
    Assertions.assertEquals(x2 * y3 * z3, data.x2y3z3);
    Assertions.assertEquals(x3 * y3 * z3, data.x3y3z3);
  }
}
