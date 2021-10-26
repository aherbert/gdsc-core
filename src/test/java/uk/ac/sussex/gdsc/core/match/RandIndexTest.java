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

package uk.ac.sussex.gdsc.core.match;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.sampling.PermutationSampler;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.test.api.TestAssertions;
import uk.ac.sussex.gdsc.test.api.TestHelper;
import uk.ac.sussex.gdsc.test.junit5.RandomSeed;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.rng.RngUtils;
import uk.ac.sussex.gdsc.test.utils.TestComplexity;
import uk.ac.sussex.gdsc.test.utils.TestLogUtils;
import uk.ac.sussex.gdsc.test.utils.TestSettings;

/**
 * Test for {@link RandIndex}.
 */
@SuppressWarnings({"javadoc"})
class RandIndexTest {

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
  void computeRandIndexThrowsWithBadData() {
    Assertions.assertThrows(NullPointerException.class, () -> RandIndex.randIndex(null, new int[2]),
        "Set1 null mismatch");
    Assertions.assertThrows(NullPointerException.class, () -> RandIndex.randIndex(new int[2], null),
        "Set2 null mismatch");
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> RandIndex.randIndex(new int[1], new int[2]), "Length mismatch");

    final int[] ok = new int[10];
    final int[] negative = new int[ok.length];
    negative[0] = -1;
    Assertions.assertThrows(IllegalArgumentException.class, () -> RandIndex.randIndex(ok, negative),
        "Negative Id in set 1 mismatch");
    Assertions.assertThrows(IllegalArgumentException.class, () -> RandIndex.randIndex(negative, ok),
        "Negative Id in set 2 mismatch");
  }

  @Test
  void canComputeSimpleRandIndexWithNoData() {
    for (final int size : new int[] {0, 1, 2}) {
      final double e = (size == 0) ? 0 : 1;
      final int[] clusters = new int[size];
      final double r = RandIndex.simpleRandIndex(clusters, clusters);
      Assertions.assertEquals(e, r);
    }
  }

  @Test
  void canComputeRandIndexWithNoData() {
    for (final int size : new int[] {0, 1, 2}) {
      final double e = (size == 0) ? 0 : 1;
      final int[] clusters = new int[size];
      final double r = RandIndex.randIndex(clusters, clusters);
      Assertions.assertEquals(e, r);
    }
  }

  @Test
  void canComputeAdjustedRandIndexWithNoData() {
    for (final int size : new int[] {0, 1, 2}) {
      final double e = (size == 0) ? 0 : 1;
      final int[] clusters = new int[size];
      final double r = RandIndex.adjustedRandIndex(clusters, clusters);
      Assertions.assertEquals(e, r);
    }
  }

  // The example data and answer are from:
  // http://stats.stackexchange.com/questions/89030/rand-index-calculation
  // Since the test will not hit the large matrix variant each test also tests
  // the matrix variant of the algorithm

  @Test
  void canComputeSimpleRandIndex() {
    final int[] clusters = {0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2};
    final int[] classes = {0, 0, 1, 0, 0, 0, 0, 1, 1, 1, 2, 1, 0, 2, 2, 2, 0};
    final double r = RandIndex.simpleRandIndex(clusters, classes);
    Assertions.assertEquals(0.67647058823529416, r, 1e-10);
  }

  @Test
  void canComputeRandIndex() {
    final int[] clusters = {0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2};
    final int[] classes = {0, 0, 1, 0, 0, 0, 0, 1, 1, 1, 2, 1, 0, 2, 2, 2, 0};
    final double r = RandIndex.randIndex(clusters, classes);
    Assertions.assertEquals(0.67647058823529416, r, 1e-10);

    // Instance should compute the matches too
    final RandIndex ri = new RandIndex();
    final double r2 = ri.compute(clusters, classes).getRandIndex();
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
  void canComputeRandIndexWithBigClusterNumbers() {
    // This test should make the class switch to using the matrix version
    final int[] clusters = {0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2};
    final int[] classes = {0, 0, 1, 0, 0, 0, 0, 1, 1, 1, 2, 1, 0, 2, 2, 2, 0};
    final int offset = 100000;
    for (int i = 0; i < clusters.length; i++) {
      clusters[i] += offset;
      classes[i] += offset;
    }
    final double r = RandIndex.randIndex(clusters, classes);
    Assertions.assertEquals(0.67647058823529416, r, 1e-10);
  }

  @Test
  void canComputeRandIndexWithArbitraryClusterNumbers() {
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
      Assertions.assertEquals(r, ri.compute(clusters, c2).getRandIndex());
      Assertions.assertEquals(r, computeRandIndexUsingMatrix(clusters, 3, c2, 3),
          "Matrix version not the same");
    }
  }

  @Test
  void canComputeSimpleRandIndexWithSparseData() {
    final int[] clusters = {4, 4, 4, 4, 4, 4, 5, 5, 5, 5, 5, 5, 6, 6, 6, 6, 6};
    final int[] classes = {0, 0, 1, 0, 0, 0, 0, 1, 1, 1, 9, 1, 0, 9, 9, 9, 0};
    final double r = RandIndex.simpleRandIndex(clusters, classes);
    Assertions.assertEquals(0.67647058823529416, r, 1e-10);
  }

  @Test
  void canComputeRandIndexWithSparseData() {
    final int[] clusters = {4, 4, 4, 4, 4, 4, 5, 5, 5, 5, 5, 5, 6, 6, 6, 6, 6};
    final int[] classes = {0, 0, 1, 0, 0, 0, 0, 1, 1, 1, 9, 1, 0, 9, 9, 9, 0};
    final double r = RandIndex.randIndex(clusters, classes);
    Assertions.assertEquals(0.67647058823529416, r, 1e-10);
  }

  @Test
  void getRandIndexThrowsWhenNotComputed() {
    Assertions.assertThrows(IllegalStateException.class, () -> {
      final RandIndex ri = new RandIndex();
      ri.getRandIndex();
    });
  }

  @Test
  void getAdjustedRandIndexThrowsWhenNotComputed() {
    Assertions.assertThrows(IllegalStateException.class, () -> {
      final RandIndex ri = new RandIndex();
      ri.getAdjustedRandIndex();
    });
  }

  @SeededTest
  void canComputeRandIndexWithSimpleData(RandomSeed seed) {
    final UniformRandomProvider rg = RngUtils.create(seed.getSeed());
    final int size = 100;
    for (final int n1 : new int[] {1, 2, 3, 4, 5}) {
      for (final int n2 : new int[] {1, 2, 3, 4, 5}) {
        canComputeRandIndexWithData(rg, size, n1, n2);
      }
    }
  }

  @SeededTest
  void canComputeRandIndexWithBigData(RandomSeed seed) {
    Assumptions.assumeTrue(TestSettings.allow(TestComplexity.LOW));
    final UniformRandomProvider rg = RngUtils.create(seed.getSeed());
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
    final double o2 = computeRandIndexUsingMatrix(c1, n1, c2, n2);
    final long t4 = System.nanoTime();

    final long simple = t2 - t1;
    final long table1 = t3 - t2;
    final long matrix = t4 - t3;

    logger.log(TestLogUtils.getRecord(Level.FINE, "[%d,%d,%d] simple=%d (%f), table1=%d (%f), %f",
        n, n1, n2, simple, e, table1, o1, simple / (double) table1));
    logger.log(TestLogUtils.getRecord(Level.FINE, "[%d,%d,%d] simple=%d (%f), matrix=%d (%f), %f",
        n, n1, n2, simple, e, matrix, o2, simple / (double) matrix));

    TestAssertions.assertTest(e, o1, TestHelper.doublesAreClose(1e-10, 0),
        "simpleRandIndex and randIndex");
    Assertions.assertEquals(o1, o2, "randIndex and randIndex using matrix");
  }

  @SeededTest
  void adjustedRandIndexIsZeroForRandomData(RandomSeed seed) {
    final UniformRandomProvider rg = RngUtils.create(seed.getSeed());
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
      sum += ri.compute(c1, c2).getAdjustedRandIndex();
    }

    sum /= loops;
    logger.log(TestLogUtils.getRecord(Level.FINE, "[%d,%d,%d,%d] %f", n, n1, n2, loops, sum));

    final double delta = 0.1;
    Assertions.assertTrue(sum < delta && sum > -delta);
  }

  @SeededTest
  void canComputeAdjustedRandIndexWithSimpleData(RandomSeed seed) {
    final int size = 100;
    for (final int n1 : new int[] {1, 2, 3, 4, 5}) {
      for (final int n2 : new int[] {1, 2, 3, 4, 5}) {
        canComputeAdjustedRandIndexWithData(seed, size, n1, n2);
      }
    }
  }

  // Speed test on large data
  @SeededTest
  void canComputeAdjustedRandIndexWithBigData(RandomSeed seed) {
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
    final UniformRandomProvider rand = RngUtils.create(seed.getSeed());
    PermutationSampler.shuffle(rand, c1);

    final long t1 = System.nanoTime();
    final double o1 = RandIndex.adjustedRandIndex(c1, c2);
    final long t2 = System.nanoTime();
    final double o2 = computeAdjustedRandIndexUsingMatrix(c1, n1, c2, n2);
    final long t3 = System.nanoTime();

    final long table1 = t2 - t1;
    final long table2 = t3 - t2;

    logger.log(TestLogUtils.getRecord(Level.FINE,
        () -> String.format("[%d,%d,%d] table1=%d (%f [%f]), table2=%d (%f), %f", n, n1, n2, table1,
            o1, RandIndex.randIndex(c1, c2), table2, o2, table1 / (double) table2)));

    Assertions.assertEquals(o1, o2, "adjustedRandIndex and adjustedRandIndex using matrix");
  }

  private static double computeRandIndexUsingMatrix(int[] set1, int n1, int[] set2, int n2) {
    final RandIndex ri = new RandIndex();
    ri.computeUsingMatrix(set1, n1, set2, n2);
    return ri.getRandIndex();
  }

  private static double computeAdjustedRandIndexUsingMatrix(int[] set1, int n1, int[] set2,
      int n2) {
    final RandIndex ri = new RandIndex();
    ri.computeUsingMatrix(set1, n1, set2, n2);
    return ri.getAdjustedRandIndex();
  }
}
