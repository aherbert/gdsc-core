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

package uk.ac.sussex.gdsc.core.match;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link Resequencer}.
 */
@SuppressWarnings({"javadoc"})
class ResequencerTest {

  private static final int NO_ENTRY = -1;

  @Test
  void renumberThrowsWithMismatchedArrays() {
    final Resequencer resequencer = new Resequencer();
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      resequencer.renumber(new int[2], new int[3]);
    });
  }

  @Test
  void canResequenceWithDefaultSwitchPoint() {
    canResequence(false);
  }

  @Test
  void canResequenceAboveSwitchPoint() {
    canResequence(true);
  }

  private static void canResequence(boolean dynamic) {
    final Resequencer resequencer = new Resequencer();
    if (dynamic) {
      resequencer.setSwitchPoint(0);
    }
    canResequence(new int[] {}, resequencer);
    canResequence(new int[] {0}, resequencer);
    canResequence(new int[] {1}, resequencer);
    canResequence(new int[] {0, 0}, resequencer);
    canResequence(new int[] {0, 1}, resequencer);
    canResequence(new int[] {0, 0, 0}, resequencer);
    canResequence(new int[] {0, 1, 2}, resequencer);
    canResequence(new int[] {1, 2, 0}, resequencer);
    canResequence(new int[] {1, 0, 2}, resequencer);
    canResequence(new int[] {Integer.MIN_VALUE, 0, Integer.MAX_VALUE}, resequencer);
    canResequence(new int[] {Integer.MAX_VALUE, 0, Integer.MIN_VALUE}, resequencer);
    canResequence(new int[] {0, 0, 0, 0}, resequencer);
    canResequence(new int[] {0, 0, 0, 1}, resequencer);
  }

  private static void canResequence(int[] inputData, Resequencer resequencer) {
    // Try shifts
    for (int i = -1; i <= 1; i++) {
      final int[] data = inputData.clone();
      for (int j = 0; j < data.length; j++) {
        data[j] += i;
      }
      // Use -1 as the null entry
      final Int2IntOpenHashMap map = new Int2IntOpenHashMap(data.length);
      map.defaultReturnValue(NO_ENTRY);
      int value = 0;
      for (final int key : data) {
        if (map.putIfAbsent(key, value) == NO_ENTRY) {
          value++;
        }
      }
      final int[] expected = new int[data.length];
      for (int j = 0; j < data.length; j++) {
        expected[j] = map.get(data[j]);
      }
      final int[] observed = data.clone();
      final int numberOfClusters = resequencer.renumber(observed);
      Assertions.assertEquals(map.size(), numberOfClusters,
          () -> "Number of identifiers : " + Arrays.toString(data));
      Assertions.assertArrayEquals(expected, observed,
          () -> "Original data=" + Arrays.toString(data));
    }
  }

  @Test
  void canCacheRenumberMapWithDefaultSwitchPoint() {
    canCacheRenumberMap(false);
  }

  @Test
  void canCacheRenumberMapAboveSwitchPoint() {
    canCacheRenumberMap(true);
  }

  private static void canCacheRenumberMap(boolean dynamic) {
    final Resequencer resequencer = new Resequencer();
    if (dynamic) {
      resequencer.setSwitchPoint(0);
    }
    Assertions.assertFalse(resequencer.isCacheMap(), "Default setting should not cache the map");
    Assertions.assertNull(resequencer.getRenumberMap(), "Default renumber map");
    Assertions.assertNull(resequencer.getRenumberInverseMap(), "Default renumber inverse map");

    resequencer.renumber(new int[10]);
    Assertions.assertNull(resequencer.getRenumberMap(), "Renumber map should not be cached");
    Assertions.assertNull(resequencer.getRenumberInverseMap(),
        "Renumber inverse map should not be cached");

    resequencer.setCacheMap(true);
    Assertions.assertTrue(resequencer.isCacheMap(), "CacheMap setter failed");

    canCacheRenumberMap(new int[] {}, resequencer);
    canCacheRenumberMap(new int[] {0}, resequencer);
    canCacheRenumberMap(new int[] {1}, resequencer);
    canCacheRenumberMap(new int[] {0, 0}, resequencer);
    canCacheRenumberMap(new int[] {0, 1}, resequencer);
    canCacheRenumberMap(new int[] {0, 0, 0}, resequencer);
    canCacheRenumberMap(new int[] {0, 1, 2}, resequencer);
    canCacheRenumberMap(new int[] {1, 2, 0}, resequencer);
    canCacheRenumberMap(new int[] {1, 0, 2}, resequencer);
    canCacheRenumberMap(new int[] {Integer.MIN_VALUE, 0, Integer.MAX_VALUE}, resequencer);
    canCacheRenumberMap(new int[] {Integer.MAX_VALUE, 0, Integer.MIN_VALUE}, resequencer);
    canCacheRenumberMap(new int[] {0, 0, 0, 0}, resequencer);
    canCacheRenumberMap(new int[] {0, 0, 0, 1}, resequencer);

    // Finally clear the cache
    Assertions.assertNotNull(resequencer.getRenumberMap(), "Renumber map should be cached");
    resequencer.setCacheMap(false);
    Assertions.assertNull(resequencer.getRenumberMap(), "Renumber map cached should be cleared");
  }

  private static void canCacheRenumberMap(int[] inputData, Resequencer resequencer) {

    final int[] outputData = new int[inputData.length];
    // Try shifts
    for (int i = -1; i <= 1; i++) {
      final int[] data = inputData.clone();
      for (int j = 0; j < data.length; j++) {
        data[j] += i;
      }

      final int n = resequencer.renumber(data, outputData);

      final List<int[]> pairs = resequencer.getRenumberMap();
      Assertions.assertNotNull(pairs, "getNumberMap should be cached");

      Assertions.assertEquals(n, pairs.size(),
          () -> "Map has wrong size : " + Arrays.toString(data));

      // Check
      final Int2IntOpenHashMap map = new Int2IntOpenHashMap();
      for (int j = 0; j < data.length; j++) {
        map.put(data[j], outputData[j]);
      }
      for (final int[] pair : pairs) {
        Assertions.assertEquals(map.get(pair[0]), pair[1],
            () -> "Map has key doesn't map to value : " + Arrays.toString(data));
      }

      // Extract keys
      final int[] inverseMap = resequencer.getRenumberInverseMap();
      Assertions.assertEquals(n, inverseMap.length,
          () -> "Inverse map has wrong size : " + Arrays.toString(data));

      for (int j = 0; j < data.length; j++) {
        Assertions.assertEquals(data[j], inverseMap[outputData[j]],
            () -> "Key doesn't map to value : " + Arrays.toString(data));
      }
    }
  }
}
