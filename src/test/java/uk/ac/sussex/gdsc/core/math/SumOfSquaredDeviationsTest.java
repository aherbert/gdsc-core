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
 * Copyright (C) 2011 - 2021 Alex Herbert
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

import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.test.junit5.RandomSeed;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.rng.RngUtils;

/**
 * Test for {@link SumOfSquaredDeviations}.
 */
@SuppressWarnings({"javadoc"})
class SumOfSquaredDeviationsTest {

  @Test
  void testConstructor() {
    final SumOfSquaredDeviations m = new SumOfSquaredDeviations();
    Assertions.assertEquals(Double.NaN, m.getMean());
    Assertions.assertEquals(Double.NaN, m.getSumOfSquaredDeviations());
    Assertions.assertEquals(0, m.getN());
  }

  @Test
  void testConstructorWithValues() {
    final double mean = 3.45;
    final double ss = 0.123;
    final long size = 13;
    final SumOfSquaredDeviations m = new SumOfSquaredDeviations(size, mean, ss);
    Assertions.assertEquals(mean, m.getMean());
    Assertions.assertEquals(size, m.getN());
  }

  @Test
  void testConstructorWithZeroSize() {
    final double mean = 3.45;
    final double ss = 0.123;
    final long size = 0;
    final SumOfSquaredDeviations m = new SumOfSquaredDeviations(size, mean, ss);
    Assertions.assertEquals(Double.NaN, m.getMean());
    Assertions.assertEquals(Double.NaN, m.getSumOfSquaredDeviations());
    Assertions.assertEquals(0, m.getN());
    final double value = 6.5;
    m.add(value);
    Assertions.assertEquals(value, m.getMean());
    Assertions.assertEquals(0, m.getSumOfSquaredDeviations());
    Assertions.assertEquals(1, m.getN());
  }

  @Test
  void testConstructorThrows() {
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> new SumOfSquaredDeviations(-1, 0, 0));
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> new SumOfSquaredDeviations(1, 0, Double.NaN));
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> new SumOfSquaredDeviations(1, 0, -1));
  }

  @SeededTest
  void canAddValues(RandomSeed seed) {
    // Test vs Apache Commons Math
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    for (int i = 0; i < 3; i++) {
      final SumOfSquaredDeviations m = new SumOfSquaredDeviations();
      final org.apache.commons.math3.stat.descriptive.moment.Mean m1 =
          new org.apache.commons.math3.stat.descriptive.moment.Mean();
      final org.apache.commons.math3.stat.descriptive.moment.SecondMoment m2 =
          new org.apache.commons.math3.stat.descriptive.moment.SecondMoment();
      final org.apache.commons.math3.stat.descriptive.moment.Variance v =
          new org.apache.commons.math3.stat.descriptive.moment.Variance();
      Assertions.assertEquals(m1.getResult(), m.getMean());
      Assertions.assertEquals(m2.getResult(), m.getSumOfSquaredDeviations());
      assertVariance(Double.NaN, m);
      Assertions.assertEquals(m1.getN(), m.getN());
      for (int j = 0; j < 10; j++) {
        final double value = rng.nextDouble();
        m.add(value);
        m1.increment(value);
        m2.increment(value);
        v.increment(value);
        Assertions.assertEquals(m1.getResult(), m.getMean());
        Assertions.assertEquals(m2.getResult(), m.getSumOfSquaredDeviations(), 1e-10);
        assertVariance(v.getResult(), m);
        Assertions.assertEquals(m1.getN(), m.getN());
      }
    }
  }

  private static void assertVariance(double expected, SumOfSquaredDeviations m) {
    Assertions.assertEquals(expected, m.getVariance(), 1e-10);
    Assertions.assertEquals(Math.sqrt(expected), m.getStandardDeviation(), 1e-10);
  }

  @SeededTest
  void canAddInstances(RandomSeed seed) {
    // Test vs Apache Commons Math
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    for (int i = 0; i < 3; i++) {
      final SumOfSquaredDeviations m1 = new SumOfSquaredDeviations();
      final SumOfSquaredDeviations m2 = new SumOfSquaredDeviations();
      final SumOfSquaredDeviations m = new SumOfSquaredDeviations();
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
      Assertions.assertEquals(m.getMean(), m1.getMean(), 1e-10);
      Assertions.assertEquals(m.getSumOfSquaredDeviations(), m1.getSumOfSquaredDeviations(), 1e-10);
      Assertions.assertEquals(m.getN(), m1.getN());
      // Add nothing
      SumOfSquaredDeviations m3 = m2.copy();
      m3.add(new SumOfSquaredDeviations());
      Assertions.assertEquals(m2.getMean(), m3.getMean());
      Assertions.assertEquals(m2.getSumOfSquaredDeviations(), m3.getSumOfSquaredDeviations());
      Assertions.assertEquals(m2.getN(), m3.getN());
      m3 = new SumOfSquaredDeviations();
      m3.add(m2);
      Assertions.assertEquals(m2.getMean(), m3.getMean());
      Assertions.assertEquals(m2.getSumOfSquaredDeviations(), m3.getSumOfSquaredDeviations());
      Assertions.assertEquals(m2.getN(), m3.getN());
    }
  }

  @Test
  void testCopy() {
    final double mean = 3.45;
    final double ss = 0.123;
    final long size = 13;
    final SumOfSquaredDeviations m = new SumOfSquaredDeviations(size, mean, ss);
    Assertions.assertEquals(mean, m.getMean());
    Assertions.assertEquals(ss, m.getSumOfSquaredDeviations());
    Assertions.assertEquals(size, m.getN());
    final SumOfSquaredDeviations mb = m.copy();
    Assertions.assertNotSame(m, mb);
    Assertions.assertEquals(mean, mb.getMean());
    Assertions.assertEquals(ss, m.getSumOfSquaredDeviations());
    Assertions.assertEquals(size, mb.getN());
  }
}
