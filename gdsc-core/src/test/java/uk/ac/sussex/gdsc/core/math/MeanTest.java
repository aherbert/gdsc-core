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
 * Copyright (C) 2011 - 2022 Alex Herbert
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
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.rng.RngUtils;
import uk.ac.sussex.gdsc.test.utils.RandomSeed;

/**
 * Test for {@link Mean}.
 */
@SuppressWarnings({"javadoc"})
class MeanTest {

  @Test
  void testConstructor() {
    final Mean m = new Mean();
    Assertions.assertEquals(Double.NaN, m.getMean());
    Assertions.assertEquals(0, m.getN());
  }

  @Test
  void testConstructorWithValues() {
    final double mean = 3.45;
    final long size = 13;
    final Mean m = new Mean(size, mean);
    Assertions.assertEquals(mean, m.getMean());
    Assertions.assertEquals(size, m.getN());
  }

  @Test
  void testConstructorWithZeroSize() {
    final double mean = 3.45;
    final long size = 0;
    final Mean m = new Mean(size, mean);
    Assertions.assertEquals(Double.NaN, m.getMean());
    Assertions.assertEquals(0, m.getN());
    final double value = 6.5;
    m.add(value);
    Assertions.assertEquals(value, m.getMean());
    Assertions.assertEquals(1, m.getN());
  }

  @Test
  void testConstructorThrows() {
    Assertions.assertThrows(IllegalArgumentException.class, () -> new Mean(-1, 0));
  }

  @SeededTest
  void canAddValues(RandomSeed seed) {
    // Test vs Apache Commons Math
    final UniformRandomProvider rng = RngUtils.create(seed.get());
    for (int i = 0; i < 3; i++) {
      final Mean m = new Mean();
      final org.apache.commons.math3.stat.descriptive.moment.Mean m1 =
          new org.apache.commons.math3.stat.descriptive.moment.Mean();
      Assertions.assertEquals(m1.getResult(), m.getMean());
      Assertions.assertEquals(m1.getN(), m.getN());
      for (int j = 0; j < 10; j++) {
        final double value = rng.nextDouble();
        m.add(value);
        m1.increment(value);
        Assertions.assertEquals(m1.getResult(), m.getMean());
        Assertions.assertEquals(m1.getN(), m.getN());
      }
    }
  }

  @Test
  void canAddInstances() {
    final double mean1 = 3.45;
    final long size1 = 13;
    final Mean m1 = new Mean(size1, mean1);
    final Mean m2 = new Mean();
    m1.add(m2);
    Assertions.assertEquals(mean1, m1.getMean());
    Assertions.assertEquals(size1, m1.getN());
    m2.add(m1);
    Assertions.assertEquals(mean1, m2.getMean());
    Assertions.assertEquals(size1, m2.getN());
    final double mean3 = 32.98;
    final long size3 = 42;
    final Mean m3 = new Mean(size3, mean3);
    final long expectedN = size1 + size3;
    final double expectedMean = (mean1 * size1 + mean3 * size3) / expectedN;
    m3.add(m1);
    Assertions.assertEquals(expectedMean, m3.getMean(), 1e-10);
    Assertions.assertEquals(expectedN, m3.getN());
    m1.add(new Mean(size3, mean3));
    Assertions.assertEquals(expectedMean, m1.getMean(), 1e-10);
    Assertions.assertEquals(expectedN, m1.getN());
  }

  @Test
  void testCopy() {
    final double mean = 3.45;
    final long size = 13;
    final Mean m = new Mean(size, mean);
    Assertions.assertEquals(mean, m.getMean());
    Assertions.assertEquals(size, m.getN());
    final Mean mb = m.copy();
    Assertions.assertNotSame(m, mb);
    Assertions.assertEquals(mean, mb.getMean());
    Assertions.assertEquals(size, mb.getN());
  }
}
