package uk.ac.sussex.gdsc.core.match;

import gnu.trove.map.hash.TIntIntHashMap;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

@SuppressWarnings({"javadoc"})
public class ResequencerTest {

  private static final int NO_ENTRY = -1;

  @Test
  public void renumberThrowsWithMismatchedArrays() {
    Resequencer resequencer = new Resequencer();
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      resequencer.renumber(new int[2], new int[3]);
    });
  }

  @Test
  public void canResequence() {
    canResequence(false);
  }

  @Test
  public void canResequenceAboveSwitchPoint() {
    canResequence(true);
  }

  private static void canResequence(boolean dynamic) {
    Resequencer resequencer = new Resequencer();
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
      final TIntIntHashMap map = new TIntIntHashMap(data.length, 0.5f, 0, NO_ENTRY);
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
  public void canCacheRenumberMap() {
    canCacheRenumberMap(false);
  }

  @Test
  public void canCacheRenumberMapAboveSwitchPoint() {
    canCacheRenumberMap(true);
  }

  private static void canCacheRenumberMap(boolean dynamic) {
    Resequencer resequencer = new Resequencer();
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

    int[] outputData = new int[inputData.length];
    // Try shifts
    for (int i = -1; i <= 1; i++) {
      final int[] data = inputData.clone();
      for (int j = 0; j < data.length; j++) {
        data[j] += i;
      }

      final int n = resequencer.renumber(data, outputData);

      List<int[]> pairs = resequencer.getRenumberMap();
      Assertions.assertNotNull(pairs, "getNumberMap should be cached");

      Assertions.assertEquals(n, pairs.size(),
          () -> "Map has wrong size : " + Arrays.toString(data));

      // Check
      TIntIntHashMap map = new TIntIntHashMap();
      for (int j = 0; j < data.length; j++) {
        map.put(data[j], outputData[j]);
      }
      for (int[] pair : pairs) {
        Assertions.assertEquals(map.get(pair[0]), pair[1],
            () -> "Map has key doesn't map to value : " + Arrays.toString(data));
      }

      // Extract keys
      int[] inverseMap = resequencer.getRenumberInverseMap();
      Assertions.assertEquals(n, inverseMap.length,
          () -> "Inverse map has wrong size : " + Arrays.toString(data));

      for (int j = 0; j < data.length; j++) {
        Assertions.assertEquals(data[j], inverseMap[outputData[j]],
            () -> "Key doesn't map to value : " + Arrays.toString(data));
      }
    }
  }
}
