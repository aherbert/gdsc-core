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
}
