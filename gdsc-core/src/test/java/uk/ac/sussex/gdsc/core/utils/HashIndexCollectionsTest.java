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

import com.koloboke.collect.map.hash.HashIntByteMap;
import com.koloboke.collect.map.hash.HashIntByteMapFactory;
import com.koloboke.collect.map.hash.HashIntByteMaps;
import com.koloboke.collect.map.hash.HashIntCharMap;
import com.koloboke.collect.map.hash.HashIntCharMapFactory;
import com.koloboke.collect.map.hash.HashIntCharMaps;
import com.koloboke.collect.map.hash.HashIntDoubleMap;
import com.koloboke.collect.map.hash.HashIntDoubleMapFactory;
import com.koloboke.collect.map.hash.HashIntDoubleMaps;
import com.koloboke.collect.map.hash.HashIntFloatMap;
import com.koloboke.collect.map.hash.HashIntFloatMapFactory;
import com.koloboke.collect.map.hash.HashIntFloatMaps;
import com.koloboke.collect.map.hash.HashIntIntMap;
import com.koloboke.collect.map.hash.HashIntIntMapFactory;
import com.koloboke.collect.map.hash.HashIntIntMaps;
import com.koloboke.collect.map.hash.HashIntLongMap;
import com.koloboke.collect.map.hash.HashIntLongMapFactory;
import com.koloboke.collect.map.hash.HashIntLongMaps;
import com.koloboke.collect.map.hash.HashIntObjMap;
import com.koloboke.collect.map.hash.HashIntObjMapFactory;
import com.koloboke.collect.map.hash.HashIntObjMaps;
import com.koloboke.collect.map.hash.HashIntShortMap;
import com.koloboke.collect.map.hash.HashIntShortMapFactory;
import com.koloboke.collect.map.hash.HashIntShortMaps;
import com.koloboke.collect.set.hash.HashIntSet;
import com.koloboke.collect.set.hash.HashIntSetFactory;
import com.koloboke.collect.set.hash.HashIntSets;
import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.Assertions;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.rng.RngFactory;
import uk.ac.sussex.gdsc.test.utils.RandomSeed;

@SuppressWarnings({"javadoc"})
class HashIndexCollectionsTest {
  /** Keys in the key domain. */
  private static final int[] KEYS = {0, 42, 999, 2136846, Integer.MAX_VALUE};

  /**
   * Keys not in the key domain.
   *
   * <h2>Note</h2>
   *
   * <p>Koloboke v1.0.0 used the keys domain as a performance hint for table initialisation.
   * Out-of-domain keys should still be allowed. Filtering the key domain is expensive. The tests
   * add out-of-domain keys to the collections to check the keys domain is a performance hint only.
   * If tests fail when upgrading the Koloboke implementation then all use of hash collections with
   * the index domain should be checked for performance against using the default domain (all
   * integer values).
   */
  private static final int[] BAD_KEYS = {-1, -56757, Integer.MIN_VALUE};

  @SeededTest
  void canGetHashIntSetFactory(RandomSeed seed) {
    final UniformRandomProvider r = RngFactory.create(seed.get());
    final HashIntSetFactory f = HashIndexCollections.getHashIntSetFactory();
    final HashIntSet actual = f.newUpdatableSet();
    final HashIntSet expected = HashIntSets.newUpdatableSet();

    for (final int k : KEYS) {
      actual.add(k);
      expected.add(k);
    }
    Assertions.assertEquals(expected, actual);
    for (int i = 0; i < 10; i++) {
      final int k = r.nextInt() >>> 1;
      actual.add(k);
      expected.add(k);
    }
    Assertions.assertEquals(expected, actual);

    for (final int k : BAD_KEYS) {
      actual.add(k);
      expected.add(k);
    }
    Assertions.assertEquals(expected, actual);
    for (int i = 0; i < 10; i++) {
      final int k = r.nextInt() | Integer.MIN_VALUE;
      actual.add(k);
      expected.add(k);
    }
    Assertions.assertEquals(expected, actual);
  }

  @SeededTest
  void canGetHashIntByteMapFactory(RandomSeed seed) {
    final UniformRandomProvider r = RngFactory.create(seed.get());
    final HashIntByteMapFactory f = HashIndexCollections.getHashIntByteMapFactory();
    final HashIntByteMap actual = f.newUpdatableMap();
    final HashIntByteMap expected = HashIntByteMaps.newUpdatableMap();

    for (final int k : KEYS) {
      final byte v = (byte) r.nextInt();
      actual.put(k, v);
      expected.put(k, v);
    }
    Assertions.assertEquals(expected, actual);

    for (final int k : BAD_KEYS) {
      final byte v = (byte) r.nextInt();
      actual.put(k, v);
      expected.put(k, v);
    }
    Assertions.assertEquals(expected, actual);
  }

  @SeededTest
  void canGetHashIntCharMapFactory(RandomSeed seed) {
    final UniformRandomProvider r = RngFactory.create(seed.get());
    final HashIntCharMapFactory f = HashIndexCollections.getHashIntCharMapFactory();
    final HashIntCharMap actual = f.newUpdatableMap();
    final HashIntCharMap expected = HashIntCharMaps.newUpdatableMap();

    for (final int k : KEYS) {
      final char v = (char) r.nextInt();
      actual.put(k, v);
      expected.put(k, v);
    }
    Assertions.assertEquals(expected, actual);

    for (final int k : BAD_KEYS) {
      final char v = (char) r.nextInt();
      actual.put(k, v);
      expected.put(k, v);
    }
    Assertions.assertEquals(expected, actual);
  }

  @SeededTest
  void canGetHashIntDoubleMapFactory(RandomSeed seed) {
    final UniformRandomProvider r = RngFactory.create(seed.get());
    final HashIntDoubleMapFactory f = HashIndexCollections.getHashIntDoubleMapFactory();
    final HashIntDoubleMap actual = f.newUpdatableMap();
    final HashIntDoubleMap expected = HashIntDoubleMaps.newUpdatableMap();

    for (final int k : KEYS) {
      final double v = r.nextDouble();
      actual.put(k, v);
      expected.put(k, v);
    }
    Assertions.assertEquals(expected, actual);

    for (final int k : BAD_KEYS) {
      final double v = r.nextDouble();
      actual.put(k, v);
      expected.put(k, v);
    }
    Assertions.assertEquals(expected, actual);
  }

  @SeededTest
  void canGetHashIntFloatMapFactory(RandomSeed seed) {
    final UniformRandomProvider r = RngFactory.create(seed.get());
    final HashIntFloatMapFactory f = HashIndexCollections.getHashIntFloatMapFactory();
    final HashIntFloatMap actual = f.newUpdatableMap();
    final HashIntFloatMap expected = HashIntFloatMaps.newUpdatableMap();

    for (final int k : KEYS) {
      final float v = r.nextFloat();
      actual.put(k, v);
      expected.put(k, v);
    }
    Assertions.assertEquals(expected, actual);

    for (final int k : BAD_KEYS) {
      final float v = r.nextFloat();
      actual.put(k, v);
      expected.put(k, v);
    }
    Assertions.assertEquals(expected, actual);
  }

  @SeededTest
  void canGetHashIntIntMapFactory(RandomSeed seed) {
    final UniformRandomProvider r = RngFactory.create(seed.get());
    final HashIntIntMapFactory f = HashIndexCollections.getHashIntIntMapFactory();
    final HashIntIntMap actual = f.newUpdatableMap();
    final HashIntIntMap expected = HashIntIntMaps.newUpdatableMap();

    for (final int k : KEYS) {
      final int v = r.nextInt();
      actual.put(k, v);
      expected.put(k, v);
    }
    Assertions.assertEquals(expected, actual);

    for (final int k : BAD_KEYS) {
      final int v = r.nextInt();
      actual.put(k, v);
      expected.put(k, v);
    }
    Assertions.assertEquals(expected, actual);
  }

  @SeededTest
  void canGetHashIntLongMapFactory(RandomSeed seed) {
    final UniformRandomProvider r = RngFactory.create(seed.get());
    final HashIntLongMapFactory f = HashIndexCollections.getHashIntLongMapFactory();
    final HashIntLongMap actual = f.newUpdatableMap();
    final HashIntLongMap expected = HashIntLongMaps.newUpdatableMap();

    for (final int k : KEYS) {
      final long v = r.nextLong();
      actual.put(k, v);
      expected.put(k, v);
    }
    Assertions.assertEquals(expected, actual);

    for (final int k : BAD_KEYS) {
      final int v = r.nextInt();
      actual.put(k, v);
      expected.put(k, v);
    }
    Assertions.assertEquals(expected, actual);
  }

  @SeededTest
  void canGetHashIntObjMapFactory(RandomSeed seed) {
    final UniformRandomProvider r = RngFactory.create(seed.get());
    final HashIntObjMapFactory<Integer> f = HashIndexCollections.getHashIntObjMapFactory();
    final HashIntObjMap<Integer> actual = f.newUpdatableMap();
    final HashIntObjMap<Integer> expected = HashIntObjMaps.newUpdatableMap();

    for (final int k : KEYS) {
      final int v = r.nextInt();
      actual.put(k, new Integer(v));
      expected.put(k, new Integer(v));
    }
    Assertions.assertEquals(expected, actual);

    for (final int k : BAD_KEYS) {
      final int v = r.nextInt();
      actual.put(k, new Integer(v));
      expected.put(k, new Integer(v));
    }
    Assertions.assertEquals(expected, actual);
  }

  @SeededTest
  void canGetHashIntShortMapFactory(RandomSeed seed) {
    final UniformRandomProvider r = RngFactory.create(seed.get());
    final HashIntShortMapFactory f = HashIndexCollections.getHashIntShortMapFactory();
    final HashIntShortMap actual = f.newUpdatableMap();
    final HashIntShortMap expected = HashIntShortMaps.newUpdatableMap();

    for (final int k : KEYS) {
      final short v = (short) r.nextInt();
      actual.put(k, v);
      expected.put(k, v);
    }
    Assertions.assertEquals(expected, actual);

    for (final int k : BAD_KEYS) {
      final short v = (short) r.nextInt();
      actual.put(k, v);
      expected.put(k, v);
    }
    Assertions.assertEquals(expected, actual);
  }
}
