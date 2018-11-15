package uk.ac.sussex.gdsc.core.match;

import uk.ac.sussex.gdsc.test.api.TestAssertions;
import uk.ac.sussex.gdsc.test.api.TestHelper;
import uk.ac.sussex.gdsc.test.junit5.RandomSeed;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.rng.RngUtils;
import uk.ac.sussex.gdsc.test.utils.TestComplexity;
import uk.ac.sussex.gdsc.test.utils.TestLogUtils;
import uk.ac.sussex.gdsc.test.utils.TestSettings;

import gnu.trove.map.hash.TIntIntHashMap;

import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.sampling.PermutationSampler;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings({"javadoc"})
public class RandIndexTest {

  private static final int NO_ENTRY = -1;

  private static Logger logger;

  @BeforeAll
  public static void beforeAll() {
    logger = Logger.getLogger(RandIndexTest.class.getName());
  }

  @AfterAll
  public static void afterAll() {
    logger = null;
  }

  @Test
  public void canSetDestructiveModification() {
    for (final boolean flag : new boolean[] {true, false}) {
      final RandIndex ri = new RandIndex(flag);
      Assertions.assertEquals(flag, ri.isAllowDestructiveModification(), "Not set in constructor");
      ri.setAllowDestructiveModification(!flag);
      Assertions.assertEquals(!flag, ri.isAllowDestructiveModification(), "Not set by setter");
    }

    // Test
    final int[] data = {0, 2, 5, 1};

    // No destructive
    final RandIndex ri = new RandIndex();
    Assertions.assertFalse(ri.isAllowDestructiveModification(), "Not false by default");

    final int[] set1 = data.clone();
    final int[] set2 = data.clone();
    final double r1 = ri.getRandIndex(set1, set2);
    Assertions.assertArrayEquals(set1, data, "Set 1 modified in-place");
    Assertions.assertArrayEquals(set2, data, "Set 2 modified in-place");

    ri.setAllowDestructiveModification(true);
    final int[] set1b = data.clone();
    final int[] set2b = data.clone();
    final double r2 = ri.getRandIndex(set1b, set2b);
    Assertions.assertThrows(AssertionError.class, () -> Assertions.assertArrayEquals(set1b, data),
        "Set 1 not modified in-place");
    Assertions.assertThrows(AssertionError.class, () -> Assertions.assertArrayEquals(set2b, data),
        "Set 2 not modified in-place");

    Assertions.assertEquals(r1, r2, "Not same result");
  }

  @Test
  public void canCompact() {
    Assertions.assertEquals(0, RandIndex.compact(null), "null input");
    canCompact(new int[] {});
    canCompact(new int[] {0});
    canCompact(new int[] {0});
    canCompact(new int[] {0, 0});
    canCompact(new int[] {0, 1});
    canCompact(new int[] {0, 0, 0});
    canCompact(new int[] {0, 1, 2});
    canCompact(new int[] {1, 2, 0});
    canCompact(new int[] {1, 0, 2});
    canCompact(new int[] {Integer.MIN_VALUE, 0, Integer.MAX_VALUE});
    canCompact(new int[] {Integer.MAX_VALUE, 0, Integer.MIN_VALUE});
    canCompact(new int[] {0, 0, 0, 0});
    canCompact(new int[] {0, 0, 0, 1});

    // Hit an edge case where the cluster matches the id but has been processed
    canCompact(new int[] {Integer.MIN_VALUE + 1, 0, 1, 2, 3, 0, 1, 2, 3});
  }

  private static void canCompact(int[] inputData) {
    // Try shifts
    for (int i = -1; i <= 1; i++) {
      // Use -1 as the null entry
      final int[] data = inputData.clone();
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
      final int numberOfClusters = RandIndex.compact(observed);
      Assertions.assertEquals(map.size(), numberOfClusters,
          () -> "Number of clusters : " + Arrays.toString(data));
      Assertions.assertArrayEquals(expected, observed,
          () -> "Original data=" + Arrays.toString(data));
    }
  }

  @Test
  public void computeRandIndexThrowsWithBadData() {
    Assertions.assertThrows(NullPointerException.class, () -> RandIndex.randIndex(null, new int[2]),
        "Set1 null mismatch");
    Assertions.assertThrows(NullPointerException.class, () -> RandIndex.randIndex(new int[2], null),
        "Set2 null mismatch");
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> RandIndex.randIndex(new int[1], new int[2]), "Length mismatch");
  }

  @Test
  public void canComputeSimpleRandIndexWithNoData() {
    for (final int size : new int[] {0, 1, 2}) {
      final double e = (size == 0) ? 0 : 1;
      final int[] clusters = new int[size];
      final double r = RandIndex.simpleRandIndex(clusters, clusters);
      Assertions.assertEquals(e, r);
    }
  }

  @Test
  public void canComputeRandIndexWithNoData() {
    for (final int size : new int[] {0, 1, 2}) {
      final double e = (size == 0) ? 0 : 1;
      final int[] clusters = new int[size];
      final double r = RandIndex.randIndex(clusters, clusters);
      Assertions.assertEquals(e, r);
    }
  }

  @Test
  public void canComputeRandIndex2WithNoData() {
    for (final int size : new int[] {0, 1, 2}) {
      final double e = (size == 0) ? 0 : 1;
      final int[] clusters = new int[size];
      final double r = RandIndex.randIndex(clusters, 1, clusters, 1);
      Assertions.assertEquals(e, r);
    }
  }

  @Test
  public void canComputeAdjustedRandIndexWithNoData() {
    for (final int size : new int[] {0, 1, 2}) {
      final double e = (size == 0) ? 0 : 1;
      final int[] clusters = new int[size];
      final double r = RandIndex.adjustedRandIndex(clusters, 1, clusters, 1);
      Assertions.assertEquals(e, r);
    }
  }

  @Test
  public void canComputeAdjustedRandIndexWhenNoRandomness() {
    // Q. should this be zero?
    final double e = 1;

    final int[] clusters = new int[2];
    clusters[1] = 1;
    final double r = RandIndex.adjustedRandIndex(clusters, 2, clusters, 2);
    Assertions.assertEquals(e, r);
  }

  // The example data and answer are from:
  // http://stats.stackexchange.com/questions/89030/rand-index-calculation
  // Since the test will not hit the large matrix variant each test also tests
  // the matrix variant of the algorithm

  @Test
  public void canComputeSimpleRandIndex() {
    final int[] clusters = {0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2};
    final int[] classes = {0, 0, 1, 0, 0, 0, 0, 1, 1, 1, 2, 1, 0, 2, 2, 2, 0};
    final double r = RandIndex.simpleRandIndex(clusters, classes);
    Assertions.assertEquals(0.67647058823529416, r, 1e-10);
  }

  @Test
  public void canComputeRandIndex() {
    final int[] clusters = {0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2};
    final int[] classes = {0, 0, 1, 0, 0, 0, 0, 1, 1, 1, 2, 1, 0, 2, 2, 2, 0};
    final double r = RandIndex.randIndex(clusters, classes);
    Assertions.assertEquals(0.67647058823529416, r, 1e-10);

    // Instance should compute the matches too
    final RandIndex ri = new RandIndex();
    final double r2 = ri.getRandIndex(clusters, classes);
    Assertions.assertEquals(r, r2, "Instance rand index");
    final int truePositives = 20;
    final int trueNegatives = 72;
    final int falsePositives = 20;
    final int falseNegatives = 24;
    Assertions.assertEquals(clusters.length, ri.getN(), "N");
    Assertions.assertEquals(truePositives, ri.getTruePositives(), "True positives");
    Assertions.assertEquals(trueNegatives, ri.getTrueNegatives(), "True negatives");
    Assertions.assertEquals(falsePositives, ri.getFalsePositives(), "False positives");
    Assertions.assertEquals(falseNegatives, ri.getFalseNegatives(), "False negatives");
  }

  @Test
  public void canComputeRandIndexWithArbitraryClusterNumbers() {
    final int[] clusters = {0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2};
    final int[] classes = {0, 0, 1, 0, 0, 0, 0, 1, 1, 1, 2, 1, 0, 2, 2, 2, 0};
    final double r = RandIndex.simpleRandIndex(clusters, classes);

    final int[][] maps =
        new int[][] {{0, 1, 2}, {0, 2, 1}, {1, 0, 2}, {1, 2, 0}, {2, 0, 1}, {2, 1, 0},};
    final RandIndex ri = new RandIndex();
    for (final int[] map : maps) {
      final int[] c2 = new int[classes.length];
      for (int i = 0; i < c2.length; i++) {
        c2[i] = map[classes[i]];
      }
      Assertions.assertEquals(r, ri.getRandIndex(clusters, 3, c2, 3));
      Assertions.assertEquals(r, computeUsingMatrix(clusters, 3, c2, 3),
          "Matrix version not the same");
    }
  }

  @Test
  public void canComputeRandIndexWithMaxClusters() {
    final int[] clusters = {0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2};
    final int[] classes = {0, 0, 1, 0, 0, 0, 0, 1, 1, 1, 2, 1, 0, 2, 2, 2, 0};
    final double r = RandIndex.randIndex(clusters, 3, classes, 3);
    Assertions.assertEquals(0.67647058823529416, r, 1e-10);
    Assertions.assertEquals(r, computeUsingMatrix(clusters, 3, classes, 3),
        "Matrix version not the same");
  }

  @Test
  public void canComputeSimpleRandIndexWithNegativeData() {
    final int[] clusters = {0, 0, 0, 0, 0, 0, -1, -1, -1, -1, -1, -1, 2, 2, 2, 2, 2};
    final int[] classes = {0, 0, 1, 0, 0, 0, 0, 1, 1, 1, -2, 1, 0, -2, -2, -2, 0};
    final double r = RandIndex.simpleRandIndex(clusters, classes);
    Assertions.assertEquals(0.67647058823529416, r, 1e-10);
  }

  @Test
  public void canComputeRandIndexWithNegativeData() {
    final int[] clusters = {0, 0, 0, 0, 0, 0, -1, -1, -1, -1, -1, -1, 2, 2, 2, 2, 2};
    final int[] classes = {0, 0, 1, 0, 0, 0, 0, 1, 1, 1, -2, 1, 0, -2, -2, -2, 0};
    final double r = RandIndex.randIndex(clusters, classes);
    Assertions.assertEquals(0.67647058823529416, r, 1e-10);
  }

  @Test
  public void canComputeRandIndexWithMaxClustersWithNegativeData() {
    final int[] clusters = {0, 0, 0, 0, 0, 0, -1, -1, -1, -1, -1, -1, 2, 2, 2, 2, 2};
    final int[] classes = {0, 0, 1, 0, 0, 0, 0, 1, 1, 1, -2, 1, 0, -2, -2, -2, 0};
    final double r = RandIndex.randIndex(clusters, 3, classes, 3);
    Assertions.assertEquals(0.67647058823529416, r, 1e-10);
    Assertions.assertEquals(r, computeUsingMatrix(clusters, 3, classes, 3),
        "Matrix version not the same");
  }

  @Test
  public void canComputeRandIndexWhenInvalidNClusters() {
    final int[] clusters = {0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2};
    final int[] classes = {0, 0, 1, 0, 0, 0, 0, 1, 1, 1, 2, 1, 0, 2, 2, 2, 0};
    final double r = RandIndex.randIndex(clusters, 2, classes, 3);
    Assertions.assertEquals(0.67647058823529416, r, 1e-10);
  }

  @Test
  public void canComputeAdjustedRandIndexWhenInvalidNClusters() {
    final int[] clusters = {0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2};
    final int[] classes = {0, 0, 1, 0, 0, 0, 0, 1, 1, 1, 2, 1, 0, 2, 2, 2, 0};
    final double e = RandIndex.adjustedRandIndex(clusters, 3, classes, 3);
    final double o = RandIndex.adjustedRandIndex(clusters, 2, classes, 3);
    Assertions.assertEquals(e, o);
  }

  @Test
  public void canComputeSimpleRandIndexWithSparseData() {
    final int[] clusters = {4, 4, 4, 4, 4, 4, 5, 5, 5, 5, 5, 5, 6, 6, 6, 6, 6};
    final int[] classes = {0, 0, 1, 0, 0, 0, 0, 1, 1, 1, 9, 1, 0, 9, 9, 9, 0};
    final double r = RandIndex.simpleRandIndex(clusters, classes);
    Assertions.assertEquals(0.67647058823529416, r, 1e-10);
  }

  @Test
  public void canComputeRandIndexWithSparseData() {
    final int[] clusters = {4, 4, 4, 4, 4, 4, 5, 5, 5, 5, 5, 5, 6, 6, 6, 6, 6};
    final int[] classes = {0, 0, 1, 0, 0, 0, 0, 1, 1, 1, 9, 1, 0, 9, 9, 9, 0};
    final double r = RandIndex.randIndex(clusters, classes);
    Assertions.assertEquals(0.67647058823529416, r, 1e-10);
  }

  @Test
  public void canComputeRandIndexWithMaxClustersWithSparseData() {
    final int[] clusters = {4, 4, 4, 4, 4, 4, 5, 5, 5, 5, 5, 5, 6, 6, 6, 6, 6};
    final int[] classes = {0, 0, 1, 0, 0, 0, 0, 1, 1, 1, 9, 1, 0, 9, 9, 9, 0};
    final double r = RandIndex.randIndex(clusters, 7, classes, 10);
    Assertions.assertEquals(0.67647058823529416, r, 1e-10);
    Assertions.assertEquals(r, computeUsingMatrix(clusters, 3, classes, 3),
        "Matrix version not the same");
  }

  @Test
  public void getRandIndexThrowsWhenNotComputed() {
    Assertions.assertThrows(IllegalStateException.class, () -> {
      final RandIndex ri = new RandIndex();
      ri.getRandIndex();
    });
  }

  @Test
  public void getAdjustedRandIndexThrowsWhenNotComputed() {
    Assertions.assertThrows(IllegalStateException.class, () -> {
      final RandIndex ri = new RandIndex();
      ri.getAdjustedRandIndex();
    });
  }

  @SeededTest
  public void canComputeRandIndexWithSimpleData(RandomSeed seed) {
    final UniformRandomProvider rg = RngUtils.create(seed.getSeedAsLong());
    final int size = 100;
    for (final int n1 : new int[] {1, 2, 3, 4, 5}) {
      for (final int n2 : new int[] {1, 2, 3, 4, 5}) {
        canComputeRandIndexWithData(rg, size, n1, n2);
      }
    }
  }

  @SeededTest
  public void canComputeRandIndexWithBigData(RandomSeed seed) {
    Assumptions.assumeTrue(TestSettings.allow(TestComplexity.LOW));
    final UniformRandomProvider rg = RngUtils.create(seed.getSeedAsLong());
    final int size = 10000;
    for (final int i : new int[] {3, 5, 10}) {
      final int n1 = size / i;
      final int n2 = size / i;
      canComputeRandIndexWithData(rg, size, n1, n2);
    }
    for (final int i : new int[] {3, 5, 10}) {
      final int n1 = size / i;
      final int n2 = i;
      canComputeRandIndexWithData(rg, size, n1, n2);
    }
    for (final int i : new int[] {3, 5, 10}) {
      final int n1 = i;
      final int n2 = i;
      canComputeRandIndexWithData(rg, size, n1, n2);
    }
  }

  private static void canComputeRandIndexWithData(UniformRandomProvider rg, int size, int n1,
      int n2) {
    final int n = size;
    final int[] c1 = new int[size];
    final int[] c2 = new int[size];
    while (size-- > 0) {
      c1[size] = size % n1;
      c2[size] = size % n2;
    }
    PermutationSampler.shuffle(rg, c1);

    final long t1 = System.nanoTime();
    final double e = RandIndex.simpleRandIndex(c1, c2);
    final long t2 = System.nanoTime();
    final double o1 = RandIndex.randIndex(c1, c2);
    final long t3 = System.nanoTime();
    final double o2 = RandIndex.randIndex(c1, n1, c2, n2);
    final long t4 = System.nanoTime();
    final double o3 = computeUsingMatrix(c1, n1, c2, n2);
    final long t5 = System.nanoTime();

    final long simple = t2 - t1;
    final long table1 = t3 - t2;
    final long table2 = t4 - t3;
    final long matrix = t5 - t4;

    logger.log(TestLogUtils.getRecord(Level.FINE, "[%d,%d,%d] simple=%d (%f), table1=%d (%f), %f",
        n, n1, n2, simple, e, table1, o1, simple / (double) table1));
    logger.log(TestLogUtils.getRecord(Level.FINE, "[%d,%d,%d] simple=%d (%f), table2=%d (%f), %f",
        n, n1, n2, simple, e, table2, o2, simple / (double) table2));
    logger.log(TestLogUtils.getRecord(Level.FINE, "[%d,%d,%d] simple=%d (%f), matrix=%d (%f), %f",
        n, n1, n2, simple, e, matrix, o2, simple / (double) matrix));

    TestAssertions.assertTest(e, o1, TestHelper.doublesAreClose(1e-10, 0),
        "simpleRandIndex and randIndex");
    Assertions.assertEquals(o1, o2, "randIndex and randIndex with limits");
    Assertions.assertEquals(o1, o3, "randIndex and randIndex using matrix");
  }

  @SeededTest
  public void adjustedRandIndexIsZeroForRandomData(RandomSeed seed) {
    final UniformRandomProvider rg = RngUtils.create(seed.getSeedAsLong());
    final int size = 100;
    for (final int n1 : new int[] {2, 5, 10}) {
      for (final int n2 : new int[] {2, 5}) {
        adjustedRandIndexIsZeroForRandomData(rg, size, n1, n2, 10);
      }
    }
  }

  private static void adjustedRandIndexIsZeroForRandomData(UniformRandomProvider rg, int size,
      int n1, int n2, int loops) {
    final int n = size;
    final int[] c1 = new int[size];
    final int[] c2 = new int[size];
    while (size-- > 0) {
      c1[size] = size % n1;
      c2[size] = size % n2;
    }
    final RandIndex ri = new RandIndex();

    double sum = 0;
    for (int i = loops; i-- > 0;) {
      PermutationSampler.shuffle(rg, c1);
      sum += ri.getAdjustedRandIndex(c1, n1, c2, n2);
    }

    sum /= loops;
    logger.log(TestLogUtils.getRecord(Level.FINE, "[%d,%d,%d,%d] %f", n, n1, n2, loops, sum));

    final double delta = 0.1;
    Assertions.assertTrue(sum < delta && sum > -delta);
  }

  @SeededTest
  public void canComputeAdjustedRandIndexWithSimpleData(RandomSeed seed) {
    final int size = 100;
    for (final int n1 : new int[] {1, 2, 3, 4, 5}) {
      for (final int n2 : new int[] {1, 2, 3, 4, 5}) {
        canComputeAdjustedRandIndexWithData(seed, size, n1, n2);
      }
    }
  }

  // Speed test on large data
  @SeededTest
  public void canComputeAdjustedRandIndexWithBigData(RandomSeed seed) {
    final int size = 10000;
    for (final int i : new int[] {3, 5, 10}) {
      final int n1 = size / i;
      final int n2 = size / i;
      canComputeAdjustedRandIndexWithData(seed, size, n1, n2);
    }
    for (final int i : new int[] {3, 5, 10}) {
      final int n1 = size / i;
      final int n2 = i;
      canComputeAdjustedRandIndexWithData(seed, size, n1, n2);
    }
    for (final int i : new int[] {3, 5, 10}) {
      final int n1 = i;
      final int n2 = i;
      canComputeAdjustedRandIndexWithData(seed, size, n1, n2);
    }
  }

  private static void canComputeAdjustedRandIndexWithData(RandomSeed seed, int size, int n1,
      int n2) {
    final int n = size;
    final int[] c1 = new int[size];
    final int[] c2 = new int[size];
    while (size-- > 0) {
      c1[size] = size % n1;
      c2[size] = size % n2;
    }
    final UniformRandomProvider rand = RngUtils.create(seed.getSeedAsLong());
    PermutationSampler.shuffle(rand, c1);

    // For debugging check the compact function works with this data
    // canCompact(c1);
    // canCompact(c2);

    final long t1 = System.nanoTime();
    final double o1 = RandIndex.adjustedRandIndex(c1, c2);
    final long t2 = System.nanoTime();
    final double o2 = RandIndex.adjustedRandIndex(c1, n1, c2, n2);
    final long t3 = System.nanoTime();

    final long table1 = t2 - t1;
    final long table2 = t3 - t2;

    logger.log(TestLogUtils.getRecord(Level.FINE,
        () -> String.format("[%d,%d,%d] table1=%d (%f [%f]), table2=%d (%f), %f", n, n1, n2, table1,
            o1, RandIndex.randIndex(c1, c2), table2, o2, table1 / (double) table2)));

    Assertions.assertEquals(o2, o1, "adjustedRandIndex and adjustedRandIndex with limits");
  }

  private static double computeUsingMatrix(int[] set1, int n1, int[] set2, int n2) {
    final RandIndex ri = new RandIndex();
    ri.computeUsingMatrix(set1, n1, set2, n2);
    return ri.getRandIndex();
  }
}
