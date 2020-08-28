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

package uk.ac.sussex.gdsc.core.utils;

import gnu.trove.list.array.TDoubleArrayList;
import java.util.Arrays;
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
import uk.ac.sussex.gdsc.core.utils.concurrent.ConcurrencyUtils;
import uk.ac.sussex.gdsc.test.junit5.RandomSeed;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.rng.RngUtils;

@SuppressWarnings({"javadoc"})
class StoredDataTest {
  @Test
  void testEmptyValues() {
    final StoredData observed = new StoredData();
    final DescriptiveStatistics expected = new DescriptiveStatistics();
    check(expected, observed);
    final StoredData observed2 = new StoredData(10);
    check(expected, observed2);
  }

  @Test
  void testSingleValues() {
    final StoredData observed = new StoredData();
    final DescriptiveStatistics expected = new DescriptiveStatistics();
    observed.add(Math.PI);
    expected.addValue(Math.PI);
    check(expected, observed);
  }

  @Test
  void canAddNullArray() {
    final StoredData observed = new StoredData();
    final DescriptiveStatistics expected = new DescriptiveStatistics();
    observed.add((double[]) null);
    observed.add((float[]) null);
    observed.add((int[]) null);
    check(expected, observed);
  }

  @SeededTest
  void canAddMultipleValues(RandomSeed seed) {
    final UniformRandomProvider r = RngUtils.create(seed.getSeed());
    final StoredData observed = new StoredData();
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
  void canAdd(RandomSeed seed) {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    DescriptiveStatistics expected;
    StoredData observed;
    for (int i = 0; i < 10; i++) {
      expected = new DescriptiveStatistics();
      observed = new StoredData();
      for (int j = 0; j < 100; j++) {
        // Use negative data to test for old bug in median
        final double d = -rng.nextDouble();
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
    observed = StoredData.create(idata);
    check(expected, observed);

    expected = new DescriptiveStatistics();
    final double[] ddata = new double[idata.length];
    for (int i = 0; i < idata.length; i++) {
      ddata[i] = idata[i];
      expected.addValue(ddata[i]);
    }
    observed = StoredData.create(ddata);
    check(expected, observed);

    expected = new DescriptiveStatistics();
    final float[] fdata = new float[idata.length];
    for (int i = 0; i < idata.length; i++) {
      fdata[i] = idata[i];
      expected.addValue(fdata[i]);
    }
    observed = StoredData.create(fdata);
    check(expected, observed);
  }

  private static void check(DescriptiveStatistics expected, StoredData observed) {
    Assertions.assertEquals(expected.getN(), observed.size(), "size");
    final double[] d1 = expected.getValues();
    final double[] d2 = observed.values();
    final float[] f2 = observed.getFloatValues();
    Assertions.assertEquals(d2.length, f2.length);
    for (int i = 0; i < d2.length; i++) {
      Assertions.assertEquals(d2[i], observed.getValue(i));
      Assertions.assertEquals((float) d2[i], f2[i]);
    }
    final TDoubleArrayList list = new TDoubleArrayList(d2.length);
    observed.forEach(list::add);
    Assertions.assertArrayEquals(d2, list.toArray());
    Arrays.sort(d1);
    Arrays.sort(d2);
    Assertions.assertArrayEquals(d1, d2);
  }

  @Test
  void canAddStoredData() {
    final int[] d1 = SimpleArrayUtils.natural(100);
    final int[] d2 = SimpleArrayUtils.newArray(75, 4, 1);
    final int[] d3 = SimpleArrayUtils.newArray(33, 4, -1);
    final StoredData o1 = StoredData.create(d1);
    final StoredData o2 = StoredData.create(d2);
    final StoredData o3 = StoredData.create(d3);
    final StoredData expected = new StoredData();
    expected.add(d1);
    expected.add(d2);
    expected.add(d3);
    final StoredData observed = new StoredData();
    observed.add(o1);
    observed.add(o2);
    observed.add(o3);
    observed.add(new StoredData());

    Assertions.assertEquals(expected.size(), observed.size(), "size");
    Assertions.assertArrayEquals(expected.values(), observed.values(), "values");
  }

  @Test
  void canSafeAdd() throws InterruptedException, ExecutionException {
    final ExecutorService es = Executors.newFixedThreadPool(6);
    final float[][] fdata = {{0, 1, 2, 3}, {4, 5, 6}, {7, 8}};
    final double[][] ddata = {{0, 1, 2, 3}, {4, 5, 6}, {7, 8}};
    final int[][] idata = {{0, 1, 2, 3}, {4, 5, 6}, {7, 8}};
    final double[] data = {0, 1, 2, 3, 4, 5, 6, 7, 8};
    final StoredData[] sdata = new StoredData[3];
    for (int i = 0; i < sdata.length; i++) {
      sdata[i] = StoredData.create(data);
    }
    final StoredData observed = new StoredData();
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
      for (final StoredData value : sdata) {
        observed.safeAdd(value);
      }
    }));
    // Wait then release all the threads together
    Thread.sleep(1000);
    count.countDown();
    ConcurrencyUtils.waitForCompletion(futures);

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
  void canConstructWithData() {
    // This requires that the constructor correctly initialises the storage
    StoredData data;
    data = StoredData.create(new double[] {1, 2, 3});
    data.add(1d);
    data = StoredData.create(new float[] {1, 2, 3});
    data.add(1f);
    data = StoredData.create(new int[] {1, 2, 3});
    data.add(1);
  }

  @Test
  void canReset() {
    final double[] array = {1, 2, 3};
    final StoredData data = StoredData.create(array);
    Assertions.assertEquals(array.length, data.size());
    final int capacity = data.capacity();
    data.reset();
    Assertions.assertEquals(0, data.size());
    Assertions.assertArrayEquals(new double[0], data.values());
    Assertions.assertEquals(capacity, data.capacity());
  }

  @Test
  void canClear() {
    final double[] array = {1, 2, 3};
    final StoredData data = StoredData.create(array);
    Assertions.assertEquals(array.length, data.size());
    data.clear();
    Assertions.assertEquals(0, data.size());
    Assertions.assertArrayEquals(new double[0], data.values());
    Assertions.assertEquals(0, data.capacity());
  }

  @Test
  void canClearWithCapacity() {
    final double[] array = {1, 2, 3};
    final StoredData data = StoredData.create(array);
    Assertions.assertEquals(array.length, data.size());
    data.clear(2);
    Assertions.assertEquals(0, data.size());
    Assertions.assertArrayEquals(new double[0], data.values());
    Assertions.assertEquals(2, data.capacity());
  }
}
