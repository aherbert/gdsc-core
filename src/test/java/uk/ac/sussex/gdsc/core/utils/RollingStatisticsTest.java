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

package uk.ac.sussex.gdsc.core.utils;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.sampling.PermutationSampler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.core.data.NotImplementedException;
import uk.ac.sussex.gdsc.test.api.TestAssertions;
import uk.ac.sussex.gdsc.test.api.TestHelper;
import uk.ac.sussex.gdsc.test.api.function.DoubleDoubleBiPredicate;
import uk.ac.sussex.gdsc.test.junit5.RandomSeed;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.rng.RngUtils;

@SuppressWarnings({"javadoc"})
class RollingStatisticsTest {
  @Test
  void testEmptyValues() {
    final RollingStatistics observed = new RollingStatistics();
    final DescriptiveStatistics expected = new DescriptiveStatistics();
    check(expected, observed);
  }

  @Test
  void testSingleValues() {
    final RollingStatistics observed = new RollingStatistics();
    final DescriptiveStatistics expected = new DescriptiveStatistics();
    observed.add(Math.PI);
    expected.addValue(Math.PI);
    check(expected, observed);
  }

  @Test
  void testSumOfSquaresThrows() {
    final RollingStatistics observed = new RollingStatistics();
    Assertions.assertThrows(NotImplementedException.class, () -> observed.getSumOfSquares());
  }

  @Test
  void testAddDoubleArrayWithZeroRange() {
    final RollingStatistics observed = new RollingStatistics();
    final double[] data = new double[] {1, 2, 3};
    observed.add(data, 1, 1);
    Assertions.assertEquals(0, observed.getN());
    observed.add(data, 2, 3);
    final DescriptiveStatistics expected = new DescriptiveStatistics();
    expected.addValue(data[2]);
    check(expected, observed);
  }

  @Test
  void testAddFloatArrayWithZeroRange() {
    final RollingStatistics observed = new RollingStatistics();
    final float[] data = new float[] {1, 2, 3};
    observed.add(data, 1, 1);
    Assertions.assertEquals(0, observed.getN());
    observed.add(data, 2, 3);
    final DescriptiveStatistics expected = new DescriptiveStatistics();
    expected.addValue(data[2]);
    check(expected, observed);
  }

  @Test
  void testAddIntArrayWithZeroRange() {
    final RollingStatistics observed = new RollingStatistics();
    final int[] data = new int[] {1, 2, 3};
    observed.add(data, 1, 1);
    Assertions.assertEquals(0, observed.getN());
    observed.add(data, 2, 3);
    final DescriptiveStatistics expected = new DescriptiveStatistics();
    expected.addValue(data[2]);
    check(expected, observed);
  }

  @SeededTest
  void canAddMultipleValues(RandomSeed seed) {
    final UniformRandomProvider r = RngUtils.create(seed.getSeed());
    final RollingStatistics observed = new RollingStatistics();
    final DescriptiveStatistics expected = new DescriptiveStatistics();
    Assertions.assertThrows(IllegalArgumentException.class, () -> observed.add(-1, 123));
    observed.add(0, 123);
    for (int i = 0; i < 5; i++) {
      final int n = r.nextInt(10) + 1;
      final double value = r.nextDouble();
      observed.add(n, value);
      for (int j = 0; j < n; j++) {
        expected.addValue(value);
      }
    }
    check(expected, observed);
  }

  @SeededTest
  void canComputeStatistics(RandomSeed seed) {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    DescriptiveStatistics expected;
    RollingStatistics observed;
    final RollingStatistics observed2 = new RollingStatistics();
    for (int i = 0; i < 10; i++) {
      expected = new DescriptiveStatistics();
      observed = new RollingStatistics();
      for (int j = 0; j < 100; j++) {
        final double d = rng.nextDouble();
        expected.addValue(d);
        observed.add(d);
        check(expected, observed);
      }
    }

    expected = new DescriptiveStatistics();
    final int[] idata = SimpleArrayUtils.natural(100);
    PermutationSampler.shuffle(rng, idata);
    for (final double v : idata) {
      expected.addValue(v);
    }
    observed = RollingStatistics.create(idata);
    check(expected, observed);
    observed2.reset();
    observed2.add(idata, 0, idata.length / 2);
    observed2.add(idata, idata.length / 2, idata.length);
    check(expected, observed2);

    expected = new DescriptiveStatistics();
    final double[] ddata = new double[idata.length];
    for (int i = 0; i < idata.length; i++) {
      ddata[i] = idata[i];
      expected.addValue(ddata[i]);
    }
    observed = RollingStatistics.create(ddata);
    check(expected, observed);
    observed2.reset();
    observed2.add(ddata, 0, ddata.length / 2);
    observed2.add(ddata, ddata.length / 2, ddata.length);
    check(expected, observed2);

    expected = new DescriptiveStatistics();
    final float[] fdata = new float[idata.length];
    for (int i = 0; i < idata.length; i++) {
      fdata[i] = idata[i];
      expected.addValue(fdata[i]);
    }
    observed = RollingStatistics.create(fdata);
    check(expected, observed);
    observed2.reset();
    observed2.add(fdata, 0, fdata.length / 2);
    observed2.add(fdata, fdata.length / 2, fdata.length);
    check(expected, observed2);
  }

  private static void check(DescriptiveStatistics expected, RollingStatistics observed) {
    Assertions.assertEquals(expected.getN(), observed.getN(), "N");
    Assertions.assertEquals(expected.getMean(), observed.getMean(), 1e-10, "Mean");
    Assertions.assertEquals(expected.getVariance(), observed.getVariance(), 1e-10, "Variance");
    Assertions.assertEquals(expected.getStandardDeviation(), observed.getStandardDeviation(), 1e-10,
        "SD");
    Assertions.assertEquals(expected.getSum(), observed.getSum(), 1e-10, "Sum");
  }

  @Test
  void canAddStatistics() {
    final int[] d1 = SimpleArrayUtils.natural(100);
    final int[] d2 = SimpleArrayUtils.newArray(75, 4, 1);
    final int[] d3 = SimpleArrayUtils.newArray(33, 4, -1);
    final RollingStatistics o1 = RollingStatistics.create(d1);
    final RollingStatistics o2 = RollingStatistics.create(d2);
    final RollingStatistics o3 = RollingStatistics.create(d3);
    final RollingStatistics expected = new RollingStatistics();
    expected.add(d1);
    expected.add(d2);
    expected.add(d3);
    final RollingStatistics observed = new RollingStatistics();
    observed.add(o1);
    observed.add(o2);
    observed.add(o3);
    observed.add(new RollingStatistics());

    final DoubleDoubleBiPredicate equality = TestHelper.doublesAreClose(1e-10, 0);
    Assertions.assertEquals(expected.getN(), observed.getN(), "N");
    TestAssertions.assertTest(expected.getMean(), observed.getMean(), equality, "Mean");
    TestAssertions.assertTest(expected.getVariance(), observed.getVariance(), equality, "Variance");
    TestAssertions.assertTest(expected.getStandardDeviation(), observed.getStandardDeviation(),
        equality, "SD");

    Assertions.assertThrows(IllegalArgumentException.class, () -> observed.add(new Statistics()));
  }

  @Test
  void canComputeWithLargeNumbers() {
    // https://en.wikipedia.org/wiki/Algorithms_for_calculating_variance#Example
    final double[] v = new double[] {4, 7, 13, 16};
    final RollingStatistics o = new RollingStatistics();
    o.add(v);
    Assertions.assertEquals(10, o.getMean(), "Mean");
    Assertions.assertEquals(30, o.getVariance(), "Variance");

    final double add = Math.pow(10, 9);
    for (int i = 0; i < v.length; i++) {
      v[i] += add;
    }
    final Statistics o2 = new RollingStatistics();
    o2.add(v);
    Assertions.assertEquals(10 + add, o2.getMean(), "Mean");
    Assertions.assertEquals(30, o2.getVariance(), "Variance");
  }
}
