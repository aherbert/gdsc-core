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

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Spliterator;
import java.util.function.IntConsumer;
import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import uk.ac.sussex.gdsc.core.utils.IndexSets.BitSetIndexSet;
import uk.ac.sussex.gdsc.core.utils.IndexSets.HashIndexSet;
import uk.ac.sussex.gdsc.test.junit5.RandomSeedSource;
import uk.ac.sussex.gdsc.test.rng.RngUtils;
import uk.ac.sussex.gdsc.test.utils.RandomSeed;

@SuppressWarnings({"javadoc"})
class IndexSetTest {

  static List<Arguments> testIndexSet() {
    // Get the random seeds
    final RandomSeed[] seeds = new RandomSeedSource().provideArguments(null)
        .map(a -> (RandomSeed) a.get()[0]).toArray(RandomSeed[]::new);

    final int[] capacities = {13, 52};
    final int[] ks = {1, 15, 50};
    final int[] ns = {17, 113};

    final ArrayList<Arguments> list = new ArrayList<>();
    for (final RandomSeed seed : seeds) {
      for (final int capacity : capacities) {
        for (final int k : ks) {
          for (final int n : ns) {
            list.add(Arguments.of(seed, new BitSetIndexSet(capacity), k, n));
            list.add(Arguments.of(seed, new HashIndexSet(capacity), k, n));
          }
        }
      }
    }
    return list;
  }

  @ParameterizedTest
  @MethodSource
  void testIndexSet(RandomSeed seed, IndexSet actual, int k, int n) {
    Assertions.assertEquals(0, actual.size());
    Assertions.assertEquals(0, actual.intStream().count());
    actual.forEach(i -> Assertions.fail("Should be empty"));
    Assertions.assertArrayEquals(new int[0], actual.toArray(null));
    final int[] a = {123, 1234, 1234};
    Assertions.assertSame(a, actual.toArray(a));
    Assertions.assertArrayEquals(a, actual.toArray(a), "Should be unchanged");

    Assertions.assertThrows(IndexOutOfBoundsException.class, () -> actual.add(-1));
    Assertions.assertThrows(IndexOutOfBoundsException.class, () -> actual.contains(-1));

    final UniformRandomProvider rng = RngUtils.create(seed.get());
    final IntOpenHashSet expected = new IntOpenHashSet();
    for (int i = k; i-- > 0;) {
      final int value = rng.nextInt(n);
      Assertions.assertEquals(expected.contains(value), actual.contains(value));
      Assertions.assertEquals(expected.add(value), actual.add(value));
      Assertions.assertEquals(expected.size(), actual.size());
      Assertions.assertTrue(actual.contains(value));
    }

    int[] a1 = actual.toArray(null);
    int[] a2 = actual.toArray(new int[actual.size()]);
    int[] a3a = new int[actual.size() + 10];
    int old1 = -42;
    int old2 = -999;
    a3a[actual.size()] = old1;
    a3a[actual.size() + 1] = old2;
    int[] a3 = actual.toArray(a3a);
    int[] count = {0};
    actual.forEach(i -> {
      Assertions.assertTrue(actual.contains(i));
      Assertions.assertEquals(i, a1[count[0]]);
      Assertions.assertEquals(i, a2[count[0]]);
      Assertions.assertEquals(i, a3[count[0]]);
      count[0]++;
    });
    Assertions.assertEquals(old1, a3[actual.size()]);
    Assertions.assertEquals(old2, a3[actual.size() + 1]);

    int[] e = expected.toIntArray();
    Arrays.sort(e);
    Arrays.sort(a1);
    Assertions.assertArrayEquals(e, a1);

    final IntOpenHashSet expected2 = new IntOpenHashSet();
    actual.forEach(expected2::add);
    Assertions.assertEquals(expected, expected2);

    expected2.clear();
    actual.intStream().forEach(expected2::add);
    Assertions.assertEquals(expected, expected2);

    expected2.clear();
    actual.intStream().parallel().forEach(IntSets.synchronize(expected2)::add);
    Assertions.assertEquals(expected, expected2);

    expected2.clear();
    final IntConsumer action = expected2::add;
    final Spliterator.OfInt s = actual.spliterator();
    Assertions.assertEquals(Spliterator.DISTINCT, s.characteristics() & Spliterator.DISTINCT);
    while (s.tryAdvance(action)) {
      // do nothing
    }
    Assertions.assertFalse(s.tryAdvance(action));
    Assertions.assertEquals(expected, expected2);

    actual.clear();
    Assertions.assertEquals(0, actual.size());
    Assertions.assertEquals(0, actual.intStream().count());
    actual.forEach(i -> Assertions.fail("Should be empty"));

    // Test put
    expected.forEach((int i) -> {
      if (!actual.contains(i)) {
        actual.put(i);
      }
    });
    Assertions.assertEquals(expected.size(), actual.size());
  }

  @Test
  void testBadCapacityThrows() {
    Assertions.assertThrows(IllegalArgumentException.class, () -> new HashIndexSet(1 << 30));
  }

  @ParameterizedTest
  @ValueSource(strings = {"1,2,3", "1,56,79879,23",
      "213,123,123,213,33212,32,32,11,123,312,31,23413,45234,6546,456,456,456313,4,342"})
  void testTrySplit(String values) {
    HashIndexSet set = new HashIndexSet(16);
    final IntOpenHashSet expected = new IntOpenHashSet();
    Arrays.stream(values.split(",")).mapToInt(Integer::parseInt).forEach(i -> {
      set.put(i);
      expected.add(i);
    });
    final IntOpenHashSet actual = new IntOpenHashSet();
    // set.spliterator().forEachRemaining((IntConsumer)actual::add);
    assertTrySplit(set.spliterator(), IntSets.synchronize(actual)::add);
    Assertions.assertEquals(expected, actual);

    // Do not split if exhausted
    actual.clear();
    Spliterator.OfInt s = set.spliterator();
    s.forEachRemaining((IntConsumer) actual::add);
    Assertions.assertNull(s.trySplit());
  }

  private static void assertTrySplit(Spliterator.OfInt s, IntConsumer action) {
    long size = s.estimateSize();
    Spliterator.OfInt s2 = s.trySplit();
    if (s2 != null) {
      long n1 = s.estimateSize();
      long n2 = s2.estimateSize();
      Assertions.assertTrue(n1 <= size);
      Assertions.assertTrue(n2 <= size);
      Assertions.assertEquals(Spliterator.DISTINCT, s.characteristics() & Spliterator.DISTINCT);
      Assertions.assertEquals(Spliterator.DISTINCT, s2.characteristics() & Spliterator.DISTINCT);
      assertTrySplit(s, action);
      assertTrySplit(s2, action);
    } else {
      Assertions.assertEquals(size, s.estimateSize());
      s.forEachRemaining(action);
      s.forEachRemaining((int i) -> Assertions.fail());
    }
  }

  @Test
  void testCreateWithExpected() {
    Assertions.assertThrows(IllegalArgumentException.class, () -> IndexSets.create(-1));
    Assertions.assertEquals(BitSetIndexSet.class, IndexSets.create(1 << 30).getClass());
    Assertions.assertEquals(BitSetIndexSet.class, IndexSets.create(Integer.MAX_VALUE).getClass());
    Assertions.assertEquals(HashIndexSet.class, IndexSets.create(1).getClass());
  }

  @Test
  void testCreateWithExpectedAndMax() {
    IndexSets.create(Integer.MAX_VALUE, Integer.MAX_VALUE);

    // OK to have expected > max
    IndexSets.create(0, 0);
    IndexSets.create(10, 0);
    Assertions.assertThrows(IllegalArgumentException.class, () -> IndexSets.create(-1, 5));
    Assertions.assertThrows(IllegalArgumentException.class, () -> IndexSets.create(0, -1));

    // Too big for hash set
    Assertions.assertEquals(BitSetIndexSet.class, IndexSets.create(1 << 30, 10).getClass());
    Assertions.assertEquals(BitSetIndexSet.class,
        IndexSets.create(Integer.MAX_VALUE, 10).getClass());

    Assertions.assertEquals(HashIndexSet.class, IndexSets.create(1, 1000).getClass());
    Assertions.assertEquals(HashIndexSet.class, IndexSets.create(1, 127).getClass());
    Assertions.assertEquals(HashIndexSet.class, IndexSets.create(2, 127).getClass());
    Assertions.assertEquals(HashIndexSet.class, IndexSets.create(3, 127).getClass());
    Assertions.assertEquals(BitSetIndexSet.class, IndexSets.create(4, 127).getClass());
  }
}
