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

package uk.ac.sussex.gdsc.core.utils;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.sampling.PermutationSampler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.core.utils.concurrent.ConcurrencyUtils;
import uk.ac.sussex.gdsc.test.junit5.RandomSeed;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.rng.RngUtils;

@SuppressWarnings({"javadoc"})
class BooleanArrayTest {
  @Test
  void testEmptyValues() {
    final BooleanArray observed = new BooleanArray(10);
    final LocalList<Boolean> expected = new LocalList<>();
    check(expected, observed);
    final BooleanArray observed2 = new BooleanArray(10);
    check(expected, observed2);
  }

  @Test
  void testSingleValues() {
    final BooleanArray observed = new BooleanArray(10);
    final LocalList<Boolean> expected = new LocalList<>();
    observed.add(true);
    expected.add(true);
    check(expected, observed);
  }

  @Test
  void canAddNullArray() {
    final BooleanArray observed = new BooleanArray(10);
    final LocalList<Boolean> expected = new LocalList<>();
    observed.add((boolean[]) null);
    check(expected, observed);
  }

  @SeededTest
  void canAddMultipleValues(RandomSeed seed) {
    final UniformRandomProvider r = RngUtils.create(seed.getSeed());
    final BooleanArray observed = new BooleanArray(10);
    final LocalList<Boolean> expected = new LocalList<>();
    Assertions.assertThrows(IllegalArgumentException.class, () -> observed.add(-1, true));
    observed.add(0, true);
    for (int i = 0; i < 5; i++) {
      final int n = r.nextInt(10) + 1;
      final boolean value = r.nextBoolean();
      observed.add(n, value);
      for (int j = 0; j < n; j++) {
        expected.add(value);
      }
    }
    check(expected, observed);
  }

  @SeededTest
  void canAdd(RandomSeed seed) {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    LocalList<Boolean> expected;
    BooleanArray observed;
    for (int i = 0; i < 10; i++) {
      expected = new LocalList<>();
      observed = new BooleanArray(10);
      for (int j = 0; j < 100; j++) {
        final boolean d = rng.nextBoolean();
        expected.add(d);
        observed.add(d);
        check(expected, observed);
      }
    }

    expected = new LocalList<>();
    final int[] idata = SimpleArrayUtils.natural(100);
    PermutationSampler.shuffle(rng, idata);
    final boolean[] bdata = new boolean[idata.length];
    for (int i = 0; i < idata.length; i++) {
      bdata[i] = (idata[i] & 1) == 0;
      expected.add(bdata[i]);
    }
    observed = BooleanArray.create(bdata);
    check(expected, observed);
  }

  private static void check(LocalList<Boolean> expected, BooleanArray observed) {
    check(expected, observed, false);
  }

  private static void check(LocalList<Boolean> expected, BooleanArray observed, boolean sort) {
    Assertions.assertEquals(expected.size(), observed.size(), "size");
    final boolean[] d2 = observed.toArray();
    Assertions.assertEquals(expected.size(), d2.length, "length");
    for (int i = 0; i < d2.length; i++) {
      Assertions.assertEquals(observed.get(i), d2[i]);
      Assertions.assertEquals(observed.getf(i), d2[i]);
    }
    if (sort) {
      expected.sort(Boolean::compare);
      final LocalList<Boolean> proxy = new LocalList<>();
      for (int i = 0; i < d2.length; i++) {
        proxy.add(d2[i]);
      }
      proxy.sort(Boolean::compare);
      for (int i = 0; i < d2.length; i++) {
        d2[i] = proxy.unsafeGet(i);
      }
    }
    for (int i = 0; i < d2.length; i++) {
      Assertions.assertEquals(expected.unsafeGet(i), d2[i]);
    }
    Assertions.assertThrows(IndexOutOfBoundsException.class, () -> observed.get(observed.size()));
  }

  @Test
  void canAddBooleanArray() {
    final boolean[] d1 = {true, true, false, false};
    final boolean[] d2 = {true, false, false};
    final boolean[] d3 = {false, true};
    final BooleanArray o1 = BooleanArray.create(d1);
    final BooleanArray o2 = BooleanArray.create(d2);
    final BooleanArray o3 = BooleanArray.create(d3);
    final BooleanArray expected = new BooleanArray(10);
    expected.add(d1);
    expected.add(d2);
    expected.add(d3);
    final BooleanArray observed = new BooleanArray(10);
    observed.add(o1);
    observed.add(o2);
    observed.add(o3);
    observed.add(new BooleanArray(10));

    Assertions.assertEquals(expected.size(), observed.size(), "size");
    Assertions.assertArrayEquals(expected.toArray(), observed.toArray(), "values");
  }

  @Test
  void canSafeAdd() throws InterruptedException, ExecutionException {
    final ExecutorService es = Executors.newFixedThreadPool(3);
    final boolean[][] ddata = {{true, true, false, false}, {true, false, false}, {false, true}};
    final boolean[] data = {true, true, false, false, true, false, false, false, true};
    final BooleanArray[] sdata = new BooleanArray[3];
    for (int i = 0; i < sdata.length; i++) {
      sdata[i] = BooleanArray.create(data);
    }
    final BooleanArray observed = new BooleanArray(10);
    final CountDownLatch count = new CountDownLatch(1);
    final LocalList<Future<?>> futures = new LocalList<>();
    futures.add(es.submit(() -> {
      await(count);
      for (final boolean[] value : ddata) {
        observed.safeAdd(value);
      }
    }));
    futures.add(es.submit(() -> {
      await(count);
      for (final boolean value : data) {
        observed.safeAdd(value);
      }
    }));
    futures.add(es.submit(() -> {
      await(count);
      for (final BooleanArray value : sdata) {
        observed.safeAdd(value);
      }
    }));
    // Wait then release all the threads together
    Thread.sleep(1000);
    count.countDown();
    ConcurrencyUtils.waitForCompletion(futures);
    es.shutdown();

    // All the arrays are the same so add multiple
    final LocalList<Boolean> expected = new LocalList<>();
    for (int i = 0; i < 5; i++) {
      for (final boolean value : data) {
        expected.add(value);
      }
    }
    check(expected, observed, true);
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
    BooleanArray data;
    data = BooleanArray.create(new boolean[] {true, false, true});
    data.add(true);
  }

  @Test
  void canClear() {
    final boolean[] array = {true, true, false};
    final BooleanArray data = BooleanArray.create(array);
    Assertions.assertEquals(array.length, data.size());
    data.clear();
    Assertions.assertEquals(0, data.size());
    Assertions.assertArrayEquals(new boolean[0], data.toArray());
    Assertions.assertEquals(3, data.capacity());
  }

  @Test
  void canCompact() {
    final boolean[] array = {true, true, false};
    final BooleanArray data = new BooleanArray(10);
    data.add(array);
    Assertions.assertEquals(array.length, data.size());
    Assertions.assertEquals(10, data.capacity());
    Assertions.assertArrayEquals(array, data.toArray());
    data.compact();
    Assertions.assertEquals(array.length, data.size());
    Assertions.assertEquals(array.length, data.capacity());
    Assertions.assertArrayEquals(array, data.toArray());
  }
}
