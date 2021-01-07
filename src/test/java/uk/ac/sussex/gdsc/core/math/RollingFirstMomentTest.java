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

package uk.ac.sussex.gdsc.core.math;

import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.test.junit5.RandomSeed;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.rng.RngUtils;

/**
 * Test for {@link ArrayMoment}.
 */
@SuppressWarnings({"javadoc"})
class RollingFirstMomentTest {

  @Test
  void testConstructor() {
    final RollingFirstMoment m1 = new RollingFirstMoment();
    Assertions.assertEquals(Double.NaN, m1.getFirstMoment());
    Assertions.assertEquals(0, m1.getN());
  }

  @Test
  void testConstructorWithMean() {
    final double mean = 3.45;
    final long size = 13;
    final RollingFirstMoment m1 = new RollingFirstMoment(size, mean);
    Assertions.assertEquals(mean, m1.getFirstMoment());
    Assertions.assertEquals(size, m1.getN());
  }

  @Test
  void testConstructorWithZeroSize() {
    final double mean = 3.45;
    final long size = 0;
    final RollingFirstMoment m1 = new RollingFirstMoment(size, mean);
    Assertions.assertEquals(Double.NaN, m1.getFirstMoment());
    Assertions.assertEquals(0, m1.getN());
    final double value = 6.5;
    m1.add(value);
    Assertions.assertEquals(value, m1.getFirstMoment());
    Assertions.assertEquals(1, m1.getN());
  }

  @Test
  void testConstructorThrows() {
    Assertions.assertThrows(IllegalArgumentException.class, () -> new RollingFirstMoment(-1, 0));
  }

  @SeededTest
  void canComputeRollingMoment(RandomSeed seed) {
    // Test vs Apache Commons Math
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    for (int i = 0; i < 3; i++) {
      final RollingFirstMoment m1 = new RollingFirstMoment();
      final Mean m2 = new Mean();
      Assertions.assertEquals(m2.getResult(), m1.getFirstMoment());
      Assertions.assertEquals(m2.getN(), m1.getN());
      for (int j = 0; j < 10; j++) {
        final double value = rng.nextDouble();
        m1.add(value);
        m2.increment(value);
        Assertions.assertEquals(m2.getResult(), m1.getFirstMoment());
        Assertions.assertEquals(m2.getN(), m1.getN());
      }
    }
  }

  @Test
  void canAddRollingMoment() {
    final double mean1 = 3.45;
    final long size1 = 13;
    final RollingFirstMoment m1 = new RollingFirstMoment(size1, mean1);
    final RollingFirstMoment m2 = new RollingFirstMoment();
    m1.add(m2);
    Assertions.assertEquals(mean1, m1.getFirstMoment());
    Assertions.assertEquals(size1, m1.getN());
    m2.add(m1);
    Assertions.assertEquals(mean1, m2.getFirstMoment());
    Assertions.assertEquals(size1, m2.getN());
    final double mean3 = 32.98;
    final long size3 = 42;
    final RollingFirstMoment m3 = new RollingFirstMoment(size3, mean3);
    final long expectedN = size1 + size3;
    final double expectedMean = (mean1 * size1 + mean3 * size3) / expectedN;
    m3.add(m1);
    Assertions.assertEquals(expectedMean, m3.getFirstMoment(), 1e-10);
    Assertions.assertEquals(expectedN, m3.getN());
    m1.add(new RollingFirstMoment(size3, mean3));
    Assertions.assertEquals(expectedMean, m1.getFirstMoment(), 1e-10);
    Assertions.assertEquals(expectedN, m1.getN());
  }
}
