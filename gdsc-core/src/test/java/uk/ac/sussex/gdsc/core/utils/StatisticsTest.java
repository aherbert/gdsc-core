/*-
 * #%L
 * Genome Damage and Stability Centre Core Package
 *
 * Contains core utilities for image analysis and is used by:
 *
 * GDSC ImageJ Plugins - Microscopy image analysis
 *
 * GDSC SMLM ImageJ Plugins - Single molecule localisation microscopy (SMLM)
 * %%
 * Copyright (C) 2011 - 2023 Alex Herbert
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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.sampling.PermutationSampler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;
import uk.ac.sussex.gdsc.core.utils.concurrent.ConcurrencyUtils;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.rng.RngFactory;
import uk.ac.sussex.gdsc.test.utils.RandomSeed;

@SuppressWarnings({"javadoc"})
class StatisticsTest {
  @Test
  void testEmptyValues() {
    final Statistics observed = new Statistics();
    final DescriptiveStatistics expected = new DescriptiveStatistics();
    check(expected, observed);
  }

  @Test
  void testSingleValues() {
    final Statistics observed = new Statistics();
    final DescriptiveStatistics expected = new DescriptiveStatistics();
    observed.add(Math.PI);
    expected.addValue(Math.PI);
    check(expected, observed);
  }

  @Test
  void canAddNullArray() {
    final Statistics observed = new Statistics();
    final DescriptiveStatistics expected = new DescriptiveStatistics();
    observed.add((double[]) null);
    observed.add((float[]) null);
    observed.add((int[]) null);
    check(expected, observed);
  }

  @Test
  void canAddNullArrayUsingRange() {
    final Statistics observed = new Statistics();
    final DescriptiveStatistics expected = new DescriptiveStatistics();
    final int from = 0;
    final int to = 10;
    observed.add((double[]) null, from, to);
    observed.add((float[]) null, from, to);
    observed.add((int[]) null, from, to);
    check(expected, observed);
  }

  @Test
  void testAddDoubleArrayWithBadRangeThrows() {
    final Statistics observed = new Statistics();
    final int size = 3;
    final double[] data = new double[size];
    Assertions.assertThrows(IllegalArgumentException.class, () -> observed.add(data, 1, 0));
    Assertions.assertThrows(ArrayIndexOutOfBoundsException.class, () -> observed.add(data, -1, 2));
    Assertions.assertThrows(ArrayIndexOutOfBoundsException.class,
        () -> observed.add(data, 1, size + 1));
    observed.add(data, 1, 1);
    Assertions.assertEquals(0, observed.getN());
  }

  @Test
  void testAddFloatArrayWithBadRangeThrows() {
    final Statistics observed = new Statistics();
    final int size = 3;
    final float[] data = new float[size];
    Assertions.assertThrows(IllegalArgumentException.class, () -> observed.add(data, 1, 0));
    Assertions.assertThrows(ArrayIndexOutOfBoundsException.class, () -> observed.add(data, -1, 2));
    Assertions.assertThrows(ArrayIndexOutOfBoundsException.class,
        () -> observed.add(data, 1, size + 1));
    observed.add(data, 1, 1);
    Assertions.assertEquals(0, observed.getN());
  }

  @Test
  void testAddIntArrayWithBadRangeThrows() {
    final Statistics observed = new Statistics();
    final int size = 3;
    final int[] data = new int[size];
    Assertions.assertThrows(IllegalArgumentException.class, () -> observed.add(data, 1, 0));
    Assertions.assertThrows(ArrayIndexOutOfBoundsException.class, () -> observed.add(data, -1, 2));
    Assertions.assertThrows(ArrayIndexOutOfBoundsException.class,
        () -> observed.add(data, 1, size + 1));
    observed.add(data, 1, 1);
    Assertions.assertEquals(0, observed.getN());
  }

  @Test
  void testAddDoubleArrayWithZeroRange() {
    final Statistics observed = new Statistics();
    final double[] data = new double[3];
    observed.add(data, 1, 1);
    Assertions.assertEquals(0, observed.getN());
  }

  @Test
  void testAddFloatArrayWithZeroRange() {
    final Statistics observed = new Statistics();
    final float[] data = new float[3];
    observed.add(data, 1, 1);
    Assertions.assertEquals(0, observed.getN());
  }

  @Test
  void testAddIntArrayWithZeroRange() {
    final Statistics observed = new Statistics();
    final int[] data = new int[3];
    observed.add(data, 1, 1);
    Assertions.assertEquals(0, observed.getN());
  }

  @SeededTest
  void canAddMultipleValues(RandomSeed seed) {
    final UniformRandomProvider r = RngFactory.create(seed.get());
    final Statistics observed = new Statistics();
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
    final UniformRandomProvider rng = RngFactory.create(seed.get());
    DescriptiveStatistics expected;
    Statistics observed;
    final Statistics observed2 = new Statistics();
    for (int i = 0; i < 10; i++) {
      expected = new DescriptiveStatistics();
      observed = new Statistics();
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
    observed = Statistics.create(idata);
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
    observed = Statistics.create(ddata);
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
    observed = Statistics.create(fdata);
    check(expected, observed);
    observed2.reset();
    observed2.add(fdata, 0, fdata.length / 2);
    observed2.add(fdata, fdata.length / 2, fdata.length);
    check(expected, observed2);
  }

  private static void check(DescriptiveStatistics expected, Statistics observed) {
    Assertions.assertEquals(expected.getN(), observed.getN(), "N");
    Assertions.assertEquals(expected.getMean(), observed.getMean(), 1e-10, "Mean");
    Assertions.assertEquals(expected.getVariance(), observed.getVariance(), 1e-10, "Variance");
    Assertions.assertEquals(expected.getStandardDeviation(), observed.getStandardDeviation(), 1e-10,
        "SD");
    Assertions.assertEquals(expected.getSum(), observed.getSum(), 1e-10, "Sum");
    Assertions.assertEquals(expected.getSumsq(), observed.getSumOfSquares(), 1e-10, "SumOfSquare");
    Assertions.assertEquals(expected.getStandardDeviation() / Math.sqrt(expected.getN()),
        observed.getStandardError(), 1e-10, "StandardError");
  }

  @Test
  void canAddStatistics() {
    final int[] d1 = SimpleArrayUtils.natural(100);
    final int[] d2 = SimpleArrayUtils.newArray(75, 4, 1);
    final int[] d3 = SimpleArrayUtils.newArray(33, 4, -1);
    final Statistics o1 = Statistics.create(d1);
    final Statistics o2 = Statistics.create(d2);
    final Statistics o3 = Statistics.create(d3);
    final Statistics expected = new Statistics();
    expected.add(d1);
    expected.add(d2);
    expected.add(d3);
    final Statistics observed = new Statistics();
    observed.add(o1);
    observed.add(o2);
    observed.add(o3);
    observed.add(new Statistics());

    Assertions.assertEquals(expected.getN(), observed.getN(), "N");
    Assertions.assertEquals(expected.getMean(), observed.getMean(), "Mean");
    Assertions.assertEquals(expected.getVariance(), observed.getVariance(), "Variance");
    Assertions.assertEquals(expected.getStandardDeviation(), observed.getStandardDeviation(), "SD");
  }

  @Test
  void cannotComputeWithLargeNumbers() {
    // https://en.wikipedia.org/wiki/Algorithms_for_calculating_variance#Example
    final double[] v = new double[] {4, 7, 13, 16};
    final Statistics o = new Statistics();
    o.add(v);
    Assertions.assertEquals(10, o.getMean(), "Mean");
    Assertions.assertEquals(30, o.getVariance(), "Variance");

    final double add = Math.pow(10, 9);
    for (int i = 0; i < v.length; i++) {
      v[i] += add;
    }

    final Statistics o2 = new Statistics();
    o2.add(v);
    Assertions.assertEquals(10 + add, o2.getMean(), "Mean");

    // Expect this to be totally wrong
    Assertions.assertThrows(AssertionFailedError.class, () -> {
      Assertions.assertEquals(30, o2.getVariance(), 5, "Variance");
    });
  }

  @Test
  void canSafeAdd() throws InterruptedException, ExecutionException {
    final ExecutorService es = Executors.newFixedThreadPool(6);
    final float[][] fdata = {{0, 1, 2, 3}, {4, 5, 6}, {7, 8}};
    final double[][] ddata = {{0, 1, 2, 3}, {4, 5, 6}, {7, 8}};
    final int[][] idata = {{0, 1, 2, 3}, {4, 5, 6}, {7, 8}};
    final double[] data = {0, 1, 2, 3, 4, 5, 6, 7, 8};
    final Statistics[] sdata = new Statistics[3];
    for (int i = 0; i < sdata.length; i++) {
      sdata[i] = Statistics.create(data);
    }
    final Statistics observed = new Statistics();
    final CountDownLatch count = new CountDownLatch(1);
    final LocalList<Future<?>> futures = new LocalList<>();
    futures.add(es.submit(() -> {
      await(count);
      for (final float[] value : fdata) {
        observed.safeAdd(value);
      }
    }));
    futures.add(es.submit(() -> {
      await(count);
      for (final double[] value : ddata) {
        observed.safeAdd(value);
      }
    }));
    futures.add(es.submit(() -> {
      await(count);
      for (final int[] value : idata) {
        observed.safeAdd(value);
      }
    }));
    futures.add(es.submit(() -> {
      await(count);
      for (final double value : data) {
        observed.safeAdd(value);
      }
    }));
    futures.add(es.submit(() -> {
      await(count);
      for (final double value : data) {
        observed.safeAdd(2, value);
      }
    }));
    futures.add(es.submit(() -> {
      await(count);
      for (final Statistics value : sdata) {
        observed.safeAdd(value);
      }
    }));
    // Wait then release all the threads together
    Thread.sleep(1000);
    count.countDown();
    ConcurrencyUtils.waitForCompletion(futures);
    es.shutdown();

    // All the arrays are the same so add multiple
    final DescriptiveStatistics expected = new DescriptiveStatistics();
    for (int i = 0; i < 9; i++) {
      for (final double value : data) {
        expected.addValue(value);
      }
    }
    check(expected, observed);
  }

  private static void await(CountDownLatch count) {
    try {
      count.await(5, TimeUnit.SECONDS);
    } catch (final InterruptedException ex) {
      Assertions.fail(ex);
    }
  }

  @Test
  void testConfidenceInterval() {
    final Statistics stats = new Statistics();
    Assertions.assertEquals(Double.POSITIVE_INFINITY, stats.getConfidenceInterval(0.0));
    Assertions.assertEquals(Double.POSITIVE_INFINITY, stats.getConfidenceInterval(0.01));
    Assertions.assertEquals(Double.POSITIVE_INFINITY, stats.getConfidenceInterval(0.05));
    stats.add(42);
    Assertions.assertEquals(Double.POSITIVE_INFINITY, stats.getConfidenceInterval(0.0));
    Assertions.assertEquals(Double.POSITIVE_INFINITY, stats.getConfidenceInterval(0.01));
    Assertions.assertEquals(Double.POSITIVE_INFINITY, stats.getConfidenceInterval(0.05));

    stats.add(SimpleArrayUtils.newArray(100, 42, 1));

    Assertions.assertThrows(IllegalArgumentException.class,
        () -> stats.getConfidenceInterval(-0.01));
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> stats.getConfidenceInterval(1.01));

    Assertions.assertEquals(0.0, stats.getConfidenceInterval(0.0));
    // Should be larger.
    // Note: No check is if the values are correct.
    final double[] intervals = {stats.getConfidenceInterval(0.001),
        stats.getConfidenceInterval(0.01), stats.getConfidenceInterval(0.05)};
    Assertions.assertTrue(intervals[1] > intervals[0]);
    Assertions.assertTrue(intervals[2] > intervals[1]);
  }

  @Test
  void testStdDevVarianceWithInfiniteValues() {
    final Statistics stats = new Statistics();
    stats.add(Double.POSITIVE_INFINITY);
    Assertions.assertEquals(Double.NaN, stats.getStandardError());
    Assertions.assertEquals(Double.NaN, stats.getStandardDeviation());
    Assertions.assertEquals(Double.NaN, stats.getVariance());
  }
}
