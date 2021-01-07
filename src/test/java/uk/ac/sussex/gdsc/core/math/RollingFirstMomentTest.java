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
    final RollingFirstMoment m = new RollingFirstMoment();
    Assertions.assertEquals(Double.NaN, m.getFirstMoment());
    Assertions.assertEquals(0, m.getN());
  }

  @Test
  void testConstructorWithMean() {
    final double moment1 = 3.45;
    final long size = 13;
    final RollingFirstMoment m = new RollingFirstMoment(size, moment1);
    Assertions.assertEquals(moment1, m.getFirstMoment());
    Assertions.assertEquals(size, m.getN());
  }

  @Test
  void testConstructorWithZeroSize() {
    final double moment1 = 3.45;
    final long size = 0;
    final RollingFirstMoment m = new RollingFirstMoment(size, moment1);
    Assertions.assertEquals(Double.NaN, m.getFirstMoment());
    Assertions.assertEquals(0, m.getN());
    final double value = 6.5;
    m.add(value);
    Assertions.assertEquals(value, m.getFirstMoment());
    Assertions.assertEquals(1, m.getN());
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
      final RollingFirstMoment m = new RollingFirstMoment();
      final Mean m1 = new Mean();
      Assertions.assertEquals(m1.getResult(), m.getFirstMoment());
      Assertions.assertEquals(m1.getN(), m.getN());
      for (int j = 0; j < 10; j++) {
        final double value = rng.nextDouble();
        m.add(value);
        m1.increment(value);
        Assertions.assertEquals(m1.getResult(), m.getFirstMoment());
        Assertions.assertEquals(m1.getN(), m.getN());
      }
    }
  }

  @Test
  void canAddRollingMoment() {
    final double moment11 = 3.45;
    final long size1 = 13;
    final RollingFirstMoment m1 = new RollingFirstMoment(size1, moment11);
    final RollingFirstMoment m2 = new RollingFirstMoment();
    m1.add(m2);
    Assertions.assertEquals(moment11, m1.getFirstMoment());
    Assertions.assertEquals(size1, m1.getN());
    m2.add(m1);
    Assertions.assertEquals(moment11, m2.getFirstMoment());
    Assertions.assertEquals(size1, m2.getN());
    final double moment13 = 32.98;
    final long size3 = 42;
    final RollingFirstMoment m3 = new RollingFirstMoment(size3, moment13);
    final long expectedN = size1 + size3;
    final double expectedMean = (moment11 * size1 + moment13 * size3) / expectedN;
    m3.add(m1);
    Assertions.assertEquals(expectedMean, m3.getFirstMoment(), 1e-10);
    Assertions.assertEquals(expectedN, m3.getN());
    m1.add(new RollingFirstMoment(size3, moment13));
    Assertions.assertEquals(expectedMean, m1.getFirstMoment(), 1e-10);
    Assertions.assertEquals(expectedN, m1.getN());
  }

  @Test
  void testCopy() {
    final double moment1 = 3.45;
    final long size = 13;
    final RollingFirstMoment m = new RollingFirstMoment(size, moment1);
    Assertions.assertEquals(moment1, m.getFirstMoment());
    Assertions.assertEquals(size, m.getN());
    final RollingFirstMoment mb = m.copy();
    Assertions.assertNotSame(m, mb);
    Assertions.assertEquals(moment1, mb.getFirstMoment());
    Assertions.assertEquals(size, mb.getN());
  }
}
