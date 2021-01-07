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
import org.apache.commons.math3.stat.descriptive.moment.SecondMoment;
import org.apache.commons.math3.stat.descriptive.moment.Variance;
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
class RollingSecondMomentTest {

  @Test
  void testConstructor() {
    final RollingSecondMoment m = new RollingSecondMoment();
    Assertions.assertEquals(Double.NaN, m.getFirstMoment());
    Assertions.assertEquals(Double.NaN, m.getSecondMoment());
    Assertions.assertEquals(0, m.getN());
  }

  @Test
  void testConstructorWithMean() {
    final double moment1 = 3.45;
    final double moment2 = 0.123;
    final long size = 13;
    final RollingSecondMoment m = new RollingSecondMoment(size, moment1, moment2);
    Assertions.assertEquals(moment1, m.getFirstMoment());
    Assertions.assertEquals(size, m.getN());
  }

  @Test
  void testConstructorWithZeroSize() {
    final double moment1 = 3.45;
    final double moment2 = 0.123;
    final long size = 0;
    final RollingSecondMoment m = new RollingSecondMoment(size, moment1, moment2);
    Assertions.assertEquals(Double.NaN, m.getFirstMoment());
    Assertions.assertEquals(Double.NaN, m.getSecondMoment());
    Assertions.assertEquals(0, m.getN());
    final double value = 6.5;
    m.add(value);
    Assertions.assertEquals(value, m.getFirstMoment());
    Assertions.assertEquals(0, m.getSecondMoment());
    Assertions.assertEquals(1, m.getN());
  }

  @Test
  void testConstructorThrows() {
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> new RollingSecondMoment(-1, 0, 0));
  }

  @SeededTest
  void canComputeRollingMoment(RandomSeed seed) {
    // Test vs Apache Commons Math
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    for (int i = 0; i < 3; i++) {
      final RollingSecondMoment m = new RollingSecondMoment();
      final Mean m1 = new Mean();
      final SecondMoment m2 = new SecondMoment();
      final Variance v = new Variance();
      Assertions.assertEquals(m1.getResult(), m.getFirstMoment());
      Assertions.assertEquals(m2.getResult(), m.getSecondMoment());
      assertVariance(Double.NaN, m);
      Assertions.assertEquals(m1.getN(), m.getN());
      for (int j = 0; j < 10; j++) {
        final double value = rng.nextDouble();
        m.add(value);
        m1.increment(value);
        m2.increment(value);
        v.increment(value);
        Assertions.assertEquals(m1.getResult(), m.getFirstMoment());
        Assertions.assertEquals(m2.getResult(), m.getSecondMoment(), 1e-10);
        assertVariance(v.getResult(), m);
        Assertions.assertEquals(m1.getN(), m.getN());
      }
    }
  }

  private static void assertVariance(double expected, RollingSecondMoment m) {
    Assertions.assertEquals(expected, m.getVariance(), 1e-10);
    Assertions.assertEquals(Math.sqrt(expected), m.getStandardDeviation(), 1e-10);
  }

  @SeededTest
  void canAddRollingMoment(RandomSeed seed) {
    // Test vs Apache Commons Math
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    for (int i = 0; i < 3; i++) {
      final RollingSecondMoment m1 = new RollingSecondMoment();
      final RollingSecondMoment m2 = new RollingSecondMoment();
      final RollingSecondMoment m = new RollingSecondMoment();
      int size = rng.nextInt(16) + 5;
      for (int j = 0; j < size; j++) {
        final double value = rng.nextDouble();
        m1.add(value);
        m.add(value);
      }
      size = rng.nextInt(16) + 5;
      for (int j = 0; j < size; j++) {
        final double value = rng.nextDouble();
        m2.add(value);
        m.add(value);
      }
      m1.add(m2);
      Assertions.assertEquals(m.getFirstMoment(), m1.getFirstMoment(), 1e-10);
      Assertions.assertEquals(m.getSecondMoment(), m1.getSecondMoment(), 1e-10);
      Assertions.assertEquals(m.getN(), m1.getN());
      // Add nothing
      RollingSecondMoment m3 = m2.copy();
      m3.add(new RollingSecondMoment());
      Assertions.assertEquals(m2.getFirstMoment(), m3.getFirstMoment());
      Assertions.assertEquals(m2.getSecondMoment(), m3.getSecondMoment());
      Assertions.assertEquals(m2.getN(), m3.getN());
      m3 = new RollingSecondMoment();
      m3.add(m2);
      Assertions.assertEquals(m2.getFirstMoment(), m3.getFirstMoment());
      Assertions.assertEquals(m2.getSecondMoment(), m3.getSecondMoment());
      Assertions.assertEquals(m2.getN(), m3.getN());
    }
  }

  @Test
  void testCopy() {
    final double moment1 = 3.45;
    final double moment2 = 0.123;
    final long size = 13;
    final RollingSecondMoment m = new RollingSecondMoment(size, moment1, moment2);
    Assertions.assertEquals(moment1, m.getFirstMoment());
    Assertions.assertEquals(moment2, m.getSecondMoment());
    Assertions.assertEquals(size, m.getN());
    final RollingSecondMoment mb = m.copy();
    Assertions.assertNotSame(m, mb);
    Assertions.assertEquals(moment1, mb.getFirstMoment());
    Assertions.assertEquals(moment2, m.getSecondMoment());
    Assertions.assertEquals(size, mb.getN());
  }
}
