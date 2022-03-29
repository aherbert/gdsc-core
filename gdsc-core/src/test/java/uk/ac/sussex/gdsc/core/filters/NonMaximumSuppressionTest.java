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

package uk.ac.sussex.gdsc.core.filters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Formatter;
import java.util.logging.Logger;
import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.core.filters.NonMaximumSuppression.FloatScanCandidate;
import uk.ac.sussex.gdsc.core.filters.NonMaximumSuppression.IntScanCandidate;
import uk.ac.sussex.gdsc.core.utils.IntFixedList;
import uk.ac.sussex.gdsc.core.utils.rng.RandomUtils;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.junit5.SpeedTag;
import uk.ac.sussex.gdsc.test.rng.RngFactory;
import uk.ac.sussex.gdsc.test.utils.RandomSeed;
import uk.ac.sussex.gdsc.test.utils.TestComplexity;
import uk.ac.sussex.gdsc.test.utils.TestLogging;
import uk.ac.sussex.gdsc.test.utils.TestLogging.TestLevel;
import uk.ac.sussex.gdsc.test.utils.TestSettings;
import uk.ac.sussex.gdsc.test.utils.functions.FormatSupplier;

@SuppressWarnings({"javadoc"})
class NonMaximumSuppressionTest {
  static final int ITER = 5;

  private static Logger logger;

  @BeforeAll
  public static void beforeAll() {
    logger = Logger.getLogger(NonMaximumSuppressionTest.class.getName());
  }

  @AfterAll
  public static void afterAll() {
    logger = null;
  }

  private final boolean debug = logger.isLoggable(TestLevel.TEST_DEBUG);

  // int[] primes = new int[] { 113, 97, 53, 29, 17, 7 };
  // int[] primes = new int[] { 509, 251 };
  int[] primes = new int[] {113, 29};
  // int[] primes = new int[] { 17 };
  // int[] smallPrimes = new int[] { 113, 97, 53, 29, 17, 7 };
  int[] smallPrimes = new int[] {17};
  int[] boxSizes = new int[] {9, 5, 3, 2, 1};
  // int[] boxSizes = new int[] { 2, 3, 5, 9, 15 };
  // int[] boxSizes = new int[] { 1 };

  int[] borderSizes = new int[] {9, 5, 3, 2, 1, 0};

  final int[] sizes2x2 = {2, 3, 6, 9};
  final int[] modulus2x2 = {3, 5, Integer.MAX_VALUE};

  /**
   * Flatten the data to a single array and sort.
   *
   * @param data the data
   * @return the sorted data
   */
  private static int[] flattenAndSort(int[][] data) {
    return Arrays.stream(data).flatMapToInt(Arrays::stream).sorted().toArray();
  }

  @Test
  void testCopy() {
    final boolean[] flags = {true, false};
    for (final boolean f1 : flags) {
      for (final boolean f2 : flags) {
        final NonMaximumSuppression nms = new NonMaximumSuppression();
        nms.setDataBuffer(f1);
        nms.setNeighbourCheck(f2);
        final NonMaximumSuppression nms2 = nms.copy();
        Assertions.assertEquals(f1, nms2.isBufferData());
        Assertions.assertEquals(f2, nms2.isNeighbourCheck());
      }
    }
  }

  @Test
  void testGetResultBuffer() {
    final NonMaximumSuppression nms = new NonMaximumSuppression();
    Assertions.assertTrue(nms.isBufferData());
    IntFixedList f1 = nms.getResultsBuffer(10);
    Assertions.assertTrue(f1.capacity() >= 10);
    Assertions.assertEquals(0, f1.size());
    IntFixedList f2 = nms.getResultsBuffer(10);
    Assertions.assertSame(f1, f2);
    f1.add(42);
    f2 = nms.getResultsBuffer(10);
    Assertions.assertEquals(0, f2.size());
    f1 = nms.getResultsBuffer(20);
    Assertions.assertTrue(f1.capacity() >= 20);
    Assertions.assertEquals(0, f1.size());
    f2 = nms.getResultsBuffer(10);
    Assertions.assertSame(f1, f2);

    nms.setDataBuffer(false);
    f2 = nms.getResultsBuffer(10);
    Assertions.assertNotSame(f1, f2);
    Assertions.assertTrue(f2.capacity() >= 10);
    Assertions.assertEquals(0, f2.size());
  }

  @Test
  void testGetFlagBuffer() {
    final NonMaximumSuppression nms = new NonMaximumSuppression();
    Assertions.assertTrue(nms.isBufferData());
    boolean[] f1 = nms.getFlagBuffer(10);
    Assertions.assertTrue(f1.length >= 10);
    boolean[] f2 = nms.getFlagBuffer(10);
    Assertions.assertSame(f1, f2);
    f1[3] = true;
    f2 = nms.getFlagBuffer(10);
    Assertions.assertFalse(f2[3]);
    f1 = nms.getFlagBuffer(20);
    Assertions.assertTrue(f1.length >= 20);
    f2 = nms.getFlagBuffer(10);
    Assertions.assertSame(f1, f2);

    nms.setDataBuffer(false);
    f2 = nms.getFlagBuffer(10);
    Assertions.assertNotSame(f1, f2);
    Assertions.assertTrue(f2.length >= 10);
  }

  @Test
  void testTruncateIntArray() {
    final int[] data = {0, 1, 2};
    for (int length = 0; length <= data.length; length++) {
      Assertions.assertArrayEquals(Arrays.copyOf(data, length),
          NonMaximumSuppression.truncate(data, length));
    }
  }

  @Test
  void testTruncateIntIntArray() {
    final int[][] data = {{0, 1, 2}, {3}, {4, 5}};
    for (int length = 0; length <= data.length; length++) {
      Assertions.assertArrayEquals(Arrays.copyOf(data, length),
          NonMaximumSuppression.truncate(data, length));
    }
  }

  // XXX: Copy from here..
  @Test
  void floatTestScanCandidate() {
    final FloatScanCandidate candidates = new FloatScanCandidate();
    final float[] data = {0, 1, 2, 3, 4, 4, 5};
    final int[] scan1 = null;
    final int[] scan2 = null;
    final int[] scan3 = null;
    final int[] scan4 = null;
    candidates.add(data, 1, scan1);
    Assertions.assertEquals(1, candidates.size());
    Assertions.assertEquals(1, candidates.getMaxIndex(0));
    Assertions.assertSame(scan1, candidates.getScan(0));
    candidates.add(data, 4, scan2);
    candidates.add(data, 5, scan3);
    Assertions.assertEquals(2, candidates.size());
    Assertions.assertEquals(4, candidates.getMaxIndex(0));
    Assertions.assertEquals(5, candidates.getMaxIndex(1));
    Assertions.assertSame(scan2, candidates.getScan(0));
    Assertions.assertSame(scan3, candidates.getScan(1));
    candidates.add(data, 6, scan4);
    Assertions.assertEquals(1, candidates.size());
    Assertions.assertEquals(6, candidates.getMaxIndex(0));
    Assertions.assertSame(scan4, candidates.getScan(0));
  }

  @SeededTest
  void floatBlockFindAndMaxFindReturnSameResult(RandomSeed seed) {
    final UniformRandomProvider rg = RngFactory.create(seed.get());
    final NonMaximumSuppression nms = new NonMaximumSuppression();

    for (final int width : primes) {
      for (final int height : primes) {
        for (final int boxSize : boxSizes) {
          floatCompareBlockFindToMaxFind(rg, nms, width, height, boxSize);
        }
      }
    }
  }

  @SeededTest
  void floatBlockFindReturnSameResultWithNeighbourCheck(RandomSeed seed) {
    final UniformRandomProvider rg = RngFactory.create(seed.get());
    final NonMaximumSuppression nms = new NonMaximumSuppression();

    for (final int width : primes) {
      for (final int height : primes) {
        for (final int boxSize : boxSizes) {
          floatCompareBlockFindWithNeighbourCheck(rg, nms, width, height, boxSize);
        }
      }
    }
  }

  private static void floatCompareBlockFindWithNeighbourCheck(UniformRandomProvider rg,
      NonMaximumSuppression nms, int width, int height, int boxSize) {
    // Random data
    final float[] data = floatCreateData(rg, width, height);
    nms.setNeighbourCheck(false);
    final int[] blockIndices1 = nms.blockFindNxN(data, width, height, boxSize);
    nms.setNeighbourCheck(true);
    final int[] blockIndices2 = nms.blockFindNxN(data, width, height, boxSize);

    Assertions.assertArrayEquals(blockIndices1, blockIndices2,
        FormatSupplier.getSupplier("Indices do not match: [%dx%d] @ %d", width, height, boxSize));
  }

  @Test
  void floatBlockFindAndMaxFindReturnSameResultOnPatternDataWithNeighbourCheck() {
    final NonMaximumSuppression nms = new NonMaximumSuppression();
    nms.setNeighbourCheck(true);

    for (final int width : smallPrimes) {
      for (final int height : smallPrimes) {
        for (final int boxSize : boxSizes) {
          floatCompareBlockFindToMaxFindWithPatternData(nms, width, height, boxSize);
        }
      }
    }
  }

  private void floatCompareBlockFindToMaxFindWithPatternData(NonMaximumSuppression nms, int width,
      int height, int boxSize) {
    // This fails when N=2. Pattern data is problematic given the block find algorithm processes the
    // pixels in a different order
    // from a linear run across the yx order data. So when the pattern produces a max pixel within
    // the range of all
    // candidates on the top row of the block, the block algorithm will output a maxima from a
    // subsequent row. Standard
    // processing will just move further along the row (beyond the block boundary) to find the next
    // maxima.
    if (boxSize <= 2) {
      return;
    }

    // Pattern data
    floatCompareBlockFindToMaxFind(nms, width, height, boxSize,
        floatCreatePatternData(width, height, 1, 0, 0, 0), "Pattern1000");
    floatCompareBlockFindToMaxFind(nms, width, height, boxSize,
        floatCreatePatternData(width, height, 1, 0, 1, 0), "Pattern1010");
    floatCompareBlockFindToMaxFind(nms, width, height, boxSize,
        floatCreatePatternData(width, height, 1, 0, 0, 1), "Pattern1001");
    floatCompareBlockFindToMaxFind(nms, width, height, boxSize,
        floatCreatePatternData(width, height, 1, 1, 1, 0), "Pattern1110");
  }

  private void floatCompareBlockFindToMaxFind(UniformRandomProvider rg, NonMaximumSuppression nms,
      int width, int height, int boxSize) {
    floatCompareBlockFindToMaxFind(nms, width, height, boxSize, floatCreateData(rg, width, height),
        "Random");

    // Empty data
    floatCompareBlockFindToMaxFind(nms, width, height, boxSize, new float[width * height], "Empty");
  }

  private void floatCompareBlockFindToMaxFind(NonMaximumSuppression nms, int width, int height,
      int boxSize, float[] data, String name) {
    final int[] blockIndices = nms.blockFindNxN(data, width, height, boxSize);
    final int[] maxIndices = nms.maxFind(data, width, height, boxSize);

    Arrays.sort(blockIndices);
    Arrays.sort(maxIndices);

    if (debug) {
      floatCompareIndices(width, height, data, boxSize, blockIndices, maxIndices);
    }

    Assertions.assertArrayEquals(maxIndices, blockIndices, FormatSupplier
        .getSupplier("%s: Indices do not match: [%dx%d] @ %d", name, width, height, boxSize));
  }

  @SeededTest
  void floatBlockFindInternalAndMaxFindInternalReturnSameResult(RandomSeed seed) {
    final UniformRandomProvider rg = RngFactory.create(seed.get());
    final NonMaximumSuppression nms = new NonMaximumSuppression();

    for (final int width : primes) {
      for (final int height : primes) {
        for (final int boxSize : boxSizes) {
          for (final int border : borderSizes) {
            floatCompareBlockFindInternalToMaxFindInternal(rg, nms, width, height, boxSize, border);
          }
        }
      }
    }
  }

  @SeededTest
  void floatBlockFindInternalReturnSameResultWithNeighbourCheck(RandomSeed seed) {
    final UniformRandomProvider rg = RngFactory.create(seed.get());
    final NonMaximumSuppression nms = new NonMaximumSuppression();

    for (final int width : primes) {
      for (final int height : primes) {
        for (final int boxSize : boxSizes) {
          for (final int border : borderSizes) {
            floatCompareBlockFindInternalWithNeighbourCheck(rg, nms, width, height, boxSize,
                border);
          }
        }
      }
    }
  }

  private static void floatCompareBlockFindInternalWithNeighbourCheck(UniformRandomProvider rg,
      NonMaximumSuppression nms, int width, int height, int boxSize, int border) {
    // Random data
    final float[] data = floatCreateData(rg, width, height);
    nms.setNeighbourCheck(false);
    final int[] blockIndices1 = nms.blockFindNxNInternal(data, width, height, boxSize, border);
    nms.setNeighbourCheck(true);
    final int[] blockIndices2 = nms.blockFindNxNInternal(data, width, height, boxSize, border);

    Assertions.assertArrayEquals(blockIndices1, blockIndices2,
        FormatSupplier.getSupplier("Indices do not match: [%dx%d] @ %d", width, height, boxSize));
  }

  @Test
  public void
      floatBlockFindInternalAndMaxFindInternalReturnSameResultOnPatternDataWithNeighbourCheck() {
    final NonMaximumSuppression nms = new NonMaximumSuppression();
    nms.setNeighbourCheck(true);

    for (final int width : smallPrimes) {
      for (final int height : smallPrimes) {
        for (final int boxSize : boxSizes) {
          for (final int border : borderSizes) {
            floatCompareBlockFindInternalToMaxFindInternalWithPatternData(nms, width, height,
                boxSize, border);
          }
        }
      }
    }
  }

  private void floatCompareBlockFindInternalToMaxFindInternalWithPatternData(
      NonMaximumSuppression nms, int width, int height, int boxSize, int border) {
    // This fails when N=2. Pattern data is problematic given the block find algorithm processes the
    // pixels in a different order from a linear run across the yx order data. So when the pattern
    // produces a max pixel within the range of all candidates on the top row of the block, the
    // block algorithm will output a maxima from a subsequent row. Standard processing will just
    // move further along the row (beyond the block boundary) to find the next maxima.
    if (boxSize <= 2) {
      return;
    }

    // Pattern data
    floatCompareBlockFindInternalToMaxFindInternal(nms, width, height, boxSize, border,
        floatCreatePatternData(width, height, 1, 0, 0, 0), "Pattern1000");
    floatCompareBlockFindInternalToMaxFindInternal(nms, width, height, boxSize, border,
        floatCreatePatternData(width, height, 1, 0, 1, 0), "Pattern1010");
    floatCompareBlockFindInternalToMaxFindInternal(nms, width, height, boxSize, border,
        floatCreatePatternData(width, height, 1, 0, 0, 1), "Pattern1001");
    floatCompareBlockFindInternalToMaxFindInternal(nms, width, height, boxSize, border,
        floatCreatePatternData(width, height, 1, 1, 1, 0), "Pattern1110");
  }

  private void floatCompareBlockFindInternalToMaxFindInternal(UniformRandomProvider rg,
      NonMaximumSuppression nms, int width, int height, int boxSize, int border) {
    floatCompareBlockFindInternalToMaxFindInternal(nms, width, height, boxSize, border,
        floatCreateData(rg, width, height), "Random");

    // Empty data
    floatCompareBlockFindInternalToMaxFindInternal(nms, width, height, boxSize, border,
        new float[width * height], "Empty");
  }

  private void floatCompareBlockFindInternalToMaxFindInternal(NonMaximumSuppression nms, int width,
      int height, int boxSize, int border, float[] data, String name) {
    final int[] blockIndices = nms.blockFindNxNInternal(data, width, height, boxSize, border);
    final int[] maxIndices = nms.maxFindInternal(data, width, height, boxSize, border);

    Arrays.sort(blockIndices);
    Arrays.sort(maxIndices);

    if (debug) {
      floatCompareIndices(width, height, data, boxSize, blockIndices, maxIndices);
    }

    Assertions.assertArrayEquals(maxIndices, blockIndices, FormatSupplier.getSupplier(
        "%s: Indices do not match: [%dx%d] @ %d (%d)", name, width, height, boxSize, border));
  }

  private static void floatCompareIndices(int width, int height, float[] data, int boxSize,
      int[] indices1, int[] indices2) {
    if (logger.isLoggable(TestLevel.TEST_INFO)) {
      final StringBuilder sb = new StringBuilder();
      try (Formatter formatter = new Formatter(sb)) {
        formatter.format("float [%dx%d@%d] i1 = %d; int i2 =  %d%n", width, height, boxSize,
            indices1.length, indices2.length);
        int i1 = 0;
        int i2 = 0;
        while (i1 < indices1.length || i2 < indices2.length) {
          final int i = (i1 < indices1.length) ? indices1[i1] : Integer.MAX_VALUE;
          final int j = (i2 < indices2.length) ? indices2[i2] : Integer.MAX_VALUE;

          if (i == j) {
            formatter.format("float   [%d,%d] = [%d,%d]%n", i % width, i / width, j % width,
                j / width);
            i1++;
            i2++;
          } else if (i < j) {
            formatter.format("float   [%d,%d] : -%n", i % width, i / width);
            i1++;
          } else if (i > j) {
            formatter.format("float   - : [%d,%d]%n", j % width, j / width);
            i2++;
          }
        }
      }
      logger.log(TestLevel.TEST_INFO, sb.toString());
      if (!Arrays.equals(indices1, indices2)) {
        logger.log(TestLevel.TEST_INFO, "Arrays are not equal");
      }
    }
  }

  @Test
  void floatFindBlockMaximaNxNInternalWithNoBlocks() {
    final NonMaximumSuppression nms = new NonMaximumSuppression();
    final float[] data = new float[12];
    Assertions.assertArrayEquals(new int[0], nms.findBlockMaximaNxNInternal(data, 12, 1, 2, 10));
    Assertions.assertArrayEquals(new int[0], nms.findBlockMaximaNxNInternal(data, 1, 12, 2, 10));
  }

  @Test
  void floatFindBlockMaximaCandidatesNxNInternalWithNoBlocks() {
    final NonMaximumSuppression nms = new NonMaximumSuppression();
    final float[] data = new float[12];
    Assertions.assertArrayEquals(new int[0][0],
        nms.findBlockMaximaCandidatesNxNInternal(data, 12, 1, 2, 10));
    Assertions.assertArrayEquals(new int[0][0],
        nms.findBlockMaximaCandidatesNxNInternal(data, 1, 12, 2, 10));
  }

  @SeededTest
  void floatFindBlockMaxima(RandomSeed seed) {
    final UniformRandomProvider rg = RngFactory.create(seed.get());
    final NonMaximumSuppression nms = new NonMaximumSuppression();
    final int width = 10;
    final int height = 15;
    final float[] data = floatCreateData(rg, width, height);
    Assertions.assertArrayEquals(nms.findBlockMaxima2x2(data, width, height),
        nms.findBlockMaxima(data, width, height, 1));
    Assertions.assertArrayEquals(nms.findBlockMaximaNxN(data, width, height, 2),
        nms.findBlockMaxima(data, width, height, 2));
  }

  @SeededTest
  void floatFindBlockMaximaCandidates(RandomSeed seed) {
    final UniformRandomProvider rg = RngFactory.create(seed.get());
    final NonMaximumSuppression nms = new NonMaximumSuppression();
    final int width = 10;
    final int height = 15;
    final float[] data = floatCreateData(rg, width, height);
    Assertions.assertArrayEquals(nms.findBlockMaximaCandidates2x2(data, width, height),
        nms.findBlockMaximaCandidates(data, width, height, 1));
    Assertions.assertArrayEquals(nms.findBlockMaximaCandidatesNxN(data, width, height, 2),
        nms.findBlockMaximaCandidates(data, width, height, 2));
  }

  @SeededTest
  void floatBlockFind(RandomSeed seed) {
    final UniformRandomProvider rg = RngFactory.create(seed.get());
    final NonMaximumSuppression nms = new NonMaximumSuppression();
    final int width = 10;
    final int height = 15;
    final float[] data = floatCreateData(rg, width, height);
    Assertions.assertArrayEquals(nms.blockFind3x3(data, width, height),
        nms.blockFind(data, width, height, 1));
    Assertions.assertArrayEquals(nms.blockFindNxN(data, width, height, 2),
        nms.blockFind(data, width, height, 2));
  }

  @SeededTest
  void floatBlockFindInternal(RandomSeed seed) {
    final UniformRandomProvider rg = RngFactory.create(seed.get());
    final NonMaximumSuppression nms = new NonMaximumSuppression();
    final int width = 10;
    final int height = 15;
    final float[] data = floatCreateData(rg, width, height);
    Assertions.assertArrayEquals(nms.blockFind3x3Internal(data, width, height, 1),
        nms.blockFindInternal(data, width, height, 1, 1));
    Assertions.assertArrayEquals(nms.blockFindNxNInternal(data, width, height, 2, 1),
        nms.blockFindInternal(data, width, height, 2, 1));
  }

  @SeededTest
  void floatBlockFind3x3InternalWithNoBorder(RandomSeed seed) {
    final UniformRandomProvider rg = RngFactory.create(seed.get());
    final NonMaximumSuppression nms = new NonMaximumSuppression();
    final int width = 10;
    final int height = 5;
    final float[] data = floatCreateData(rg, width, height);
    Assertions.assertArrayEquals(nms.blockFind3x3(data, width, height),
        nms.blockFind3x3Internal(data, width, height, 0));
  }

  @SeededTest
  void floatBlockFindNxNAndBlockFind3x3ReturnSameResult(RandomSeed seed) {
    final UniformRandomProvider rg = RngFactory.create(seed.get());

    final NonMaximumSuppression nms = new NonMaximumSuppression();

    for (int width : primes) {
      // 3x3 does not process to the edge of odd size images
      width++;

      for (int height : primes) {
        height++;

        final float[] data = floatCreateData(rg, width, height);

        for (final boolean b : new boolean[] {false, true}) {
          nms.setNeighbourCheck(b);
          final int[] blockNxNIndices = nms.blockFindNxN(data, width, height, 1);
          final int[] block3x3Indices = nms.blockFind3x3(data, width, height);
          final int[] blockIndices = nms.blockFind(data, width, height, 1);

          Arrays.sort(blockNxNIndices);
          Arrays.sort(block3x3Indices);
          Arrays.sort(blockIndices);

          if (debug) {
            floatCompareIndices(width, height, data, 1, blockNxNIndices, block3x3Indices);
          }

          Assertions.assertArrayEquals(blockNxNIndices, block3x3Indices,
              FormatSupplier.getSupplier("Indices do not match: [%dx%d] %b", width, height, b));
          Assertions.assertArrayEquals(blockNxNIndices, blockIndices,
              FormatSupplier.getSupplier("Indices do not match: [%dx%d] %b", width, height, b));
        }
      }
    }
  }

  @SeededTest
  void floatBlockFindNxNInternalAndBlockFind3x3InternalReturnSameResult(RandomSeed seed) {
    final UniformRandomProvider rg = RngFactory.create(seed.get());

    final NonMaximumSuppression nms = new NonMaximumSuppression();

    for (int width : primes) {
      // 3x3 does not process to the edge of odd size images
      width++;

      for (int height : primes) {
        height++;

        final float[] data = floatCreateData(rg, width, height);

        for (final boolean b : new boolean[] {false, true}) {
          nms.setNeighbourCheck(b);
          final int[] blockNxNIndices = nms.blockFindNxNInternal(data, width, height, 1, 1);
          final int[] block3x3Indices = nms.blockFind3x3Internal(data, width, height, 1);
          final int[] blockIndices = nms.blockFindInternal(data, width, height, 1, 1);

          Arrays.sort(blockNxNIndices);
          Arrays.sort(block3x3Indices);
          Arrays.sort(blockIndices);

          if (debug) {
            floatCompareIndices(width, height, data, 1, blockNxNIndices, block3x3Indices);
          }

          Assertions.assertArrayEquals(blockNxNIndices, block3x3Indices,
              FormatSupplier.getSupplier("Indices do not match: [%dx%d] %b", width, height, b));
          Assertions.assertArrayEquals(blockNxNIndices, blockIndices,
              FormatSupplier.getSupplier("Indices do not match: [%dx%d] %b", width, height, b));
        }
      }
    }
  }

  @SeededTest
  void floatFindBlockMaximaCandidatesNxNAndFindBlockMaximaCandidates2x2ReturnSameResult(
      RandomSeed seed) {
    final UniformRandomProvider rg = RngFactory.create(seed.get());

    final NonMaximumSuppression nms = new NonMaximumSuppression();

    for (final int width : sizes2x2) {
      for (final int height : sizes2x2) {
        for (final int mod : modulus2x2) {
          final float[] data = floatCreateRepeatData(rg, width, height, mod);

          final int[][] blockNxNIndices = nms.findBlockMaximaCandidatesNxN(data, width, height, 1);
          final int[][] block2x2Indices = nms.findBlockMaximaCandidates2x2(data, width, height);
          final int[][] blockIndices = nms.findBlockMaximaCandidates(data, width, height, 1);

          flattenAndSort(blockNxNIndices);
          flattenAndSort(block2x2Indices);
          flattenAndSort(blockIndices);

          Assertions.assertArrayEquals(blockNxNIndices, block2x2Indices,
              FormatSupplier.getSupplier("Indices do not match: [%dx%d]", width, height));
          Assertions.assertArrayEquals(blockNxNIndices, blockIndices,
              FormatSupplier.getSupplier("Indices do not match: [%dx%d]", width, height));
        }
      }
    }
  }

  @SpeedTag
  @SeededTest
  void floatBlockFindIsFasterThanMaxFind(RandomSeed seed) {
    Assumptions.assumeTrue(TestSettings.allow(TestComplexity.MEDIUM));

    final UniformRandomProvider rg = RngFactory.create(seed.get());

    final NonMaximumSuppression nms = new NonMaximumSuppression();

    final ArrayList<float[]> dataSet = floatCreateSpeedData(rg);
    final ArrayList<Long> blockTimes = new ArrayList<>();

    // Initialise
    nms.blockFind(dataSet.get(0), primes[0], primes[0], boxSizes[0]);
    nms.maxFind(dataSet.get(0), primes[0], primes[0], boxSizes[0]);

    for (final int boxSize : boxSizes) {
      for (final int width : primes) {
        for (final int height : primes) {
          long time = System.nanoTime();
          for (final float[] data : dataSet) {
            nms.blockFind(data, width, height, boxSize);
          }
          time = System.nanoTime() - time;
          blockTimes.add(time);
        }
      }
    }

    long total = 0;
    long blockTotal = 0;
    int index = 0;
    for (final int boxSize : boxSizes) {
      long boxTotal = 0;
      long blockBoxTotal = 0;
      for (final int width : primes) {
        for (final int height : primes) {
          long time = System.nanoTime();
          for (final float[] data : dataSet) {
            nms.maxFind(data, width, height, boxSize);
          }
          time = System.nanoTime() - time;

          final long blockTime = blockTimes.get(index++);
          total += time;
          blockTotal += blockTime;
          boxTotal += time;
          blockBoxTotal += blockTime;
          if (debug) {
            logger.log(TestLevel.TEST_DEBUG,
                FormatSupplier.getSupplier(
                    "float maxFind [%dx%d] @ %d : %d => blockFind %d = %.2fx", width, height,
                    boxSize, time, blockTime, (1.0 * time) / blockTime));
            // Assertions.assertTrue(String.format("Not faster: [%dx%d] @ %d : %d > %d", width,
            // height, boxSize,
            // blockTime, time), blockTime < time);
          }
        }
      }
      // if (debug)
      logger.log(TestLogging.getTimingRecord("float maxFind" + boxSize, boxTotal, "float blockFind",
          blockBoxTotal));
      // if (boxSize > 1) // Sometimes this fails at small sizes
      // Assertions.assertTrue(String.format("Not faster: Block %d : %d > %d", boxSize,
      // blockBoxTotal, boxTotal),
      // blockBoxTotal < boxTotal);
    }
    logger.log(TestLogging.getTimingRecord("float maxFind", total, "float blockFind", blockTotal));
  }

  @SpeedTag
  @SeededTest
  void floatBlockFindWithNeighbourCheckIsFasterThanMaxFind(RandomSeed seed) {
    Assumptions.assumeTrue(TestSettings.allow(TestComplexity.MEDIUM));

    final UniformRandomProvider rg = RngFactory.create(seed.get());

    final NonMaximumSuppression nms = new NonMaximumSuppression();
    nms.setNeighbourCheck(true);

    final ArrayList<float[]> dataSet = floatCreateSpeedData(rg);
    final ArrayList<Long> blockTimes = new ArrayList<>();

    // Initialise
    nms.blockFind(dataSet.get(0), primes[0], primes[0], boxSizes[0]);
    nms.maxFind(dataSet.get(0), primes[0], primes[0], boxSizes[0]);

    for (final int boxSize : boxSizes) {
      for (final int width : primes) {
        for (final int height : primes) {
          long time = System.nanoTime();
          for (final float[] data : dataSet) {
            nms.blockFind(data, width, height, boxSize);
          }
          time = System.nanoTime() - time;
          blockTimes.add(time);
        }
      }
    }

    long total = 0;
    long blockTotal = 0;
    int index = 0;
    for (final int boxSize : boxSizes) {
      long boxTotal = 0;
      long blockBoxTotal = 0;
      for (final int width : primes) {
        for (final int height : primes) {
          long time = System.nanoTime();
          for (final float[] data : dataSet) {
            nms.maxFind(data, width, height, boxSize);
          }
          time = System.nanoTime() - time;

          final long blockTime = blockTimes.get(index++);
          total += time;
          blockTotal += blockTime;
          boxTotal += time;
          blockBoxTotal += blockTime;
          if (debug) {
            logger.log(TestLevel.TEST_DEBUG,
                FormatSupplier.getSupplier(
                    "float maxFind [%dx%d] @ %d : %d => blockFindWithCheck %d = %.2fx", width,
                    height, boxSize, time, blockTime, (1.0 * time) / blockTime));
            // Assertions.assertTrue(String.format("Not faster: [%dx%d] @ %d : %d > %d", width,
            // height, boxSize,
            // blockTime, time), blockTime < time);
          }
        }
      }
      // if (debug)
      logger.log(TestLogging.getTimingRecord("float maxFind" + boxSize, boxTotal,
          "float blockFindWithCheck", blockBoxTotal));
      // if (boxSize > 1) // Sometimes this fails at small sizes
      // Assertions.assertTrue(String.format("Not faster: Block %d : %d > %d", boxSize,
      // blockBoxTotal, boxTotal),
      // blockBoxTotal < boxTotal);
    }
    logger.log(TestLogging.getTimingRecord("float maxFind", total, "float blockFindWithCheck",
        blockTotal));
  }

  private ArrayList<float[]> floatCreateSpeedData(UniformRandomProvider rg) {
    final int iter = ITER;

    final ArrayList<float[]> dataSet = new ArrayList<>(iter);
    for (int i = iter; i-- > 0;) {
      dataSet.add(floatCreateData(rg, primes[0], primes[0]));
    }
    return dataSet;
  }

  @SpeedTag
  @SeededTest
  void floatBlockFindNxNInternalIsFasterThanBlockFindNxNForBigBorders(RandomSeed seed) {
    // Note: This test is currently failing. The primes used to be:
    // int[] primes = new int[] { 997, 503, 251 };
    // Now with smaller primes (to increase the speed of running these tests)
    // this test fails. The time for the JVM to optimise the internal method
    // is high.
    // If all the tests are run then the similar test
    // floatBlockFindInternalIsFasterWithoutNeighbourCheck shows much faster
    // times for the internal method.
    // This test should be changed to repeat until the times converge.

    Assumptions.assumeTrue(TestSettings.allow(TestComplexity.MEDIUM));

    final UniformRandomProvider rg = RngFactory.create(seed.get());

    final NonMaximumSuppression nms = new NonMaximumSuppression();

    final ArrayList<float[]> dataSet = floatCreateSpeedData(rg);
    final ArrayList<Long> internalTimes = new ArrayList<>();

    for (final int boxSize : boxSizes) {
      for (final int width : primes) {
        for (final int height : primes) {
          // Initialise
          nms.blockFindNxNInternal(dataSet.get(0), width, height, boxSize, boxSize);
          long time = System.nanoTime();
          for (final float[] data : dataSet) {
            nms.blockFindNxNInternal(data, width, height, boxSize, boxSize);
          }
          time = System.nanoTime() - time;
          internalTimes.add(time);
        }
      }
    }

    long total = 0;
    long internalTotal = 0;
    long bigTotal = 0;
    long bigInternalTotal = 0;
    int index = 0;
    for (final int boxSize : boxSizes) {
      long boxTotal = 0;
      long internalBoxTotal = 0;
      for (final int width : primes) {
        for (final int height : primes) {
          // Initialise
          nms.blockFindNxN(dataSet.get(0), primes[0], primes[0], boxSizes[0]);
          long time = System.nanoTime();
          for (final float[] data : dataSet) {
            nms.blockFindNxN(data, width, height, boxSize);
          }
          time = System.nanoTime() - time;

          final long internalTime = internalTimes.get(index++);
          total += time;
          internalTotal += internalTime;
          if (boxSize >= 5) {
            bigTotal += time;
            bigInternalTotal += internalTime;
          }
          boxTotal += time;
          internalBoxTotal += internalTime;
          if (debug) {
            logger.log(TestLevel.TEST_DEBUG,
                FormatSupplier.getSupplier(
                    "float blockFind[%dx%d] @ %d : %d => blockFindInternal %d = %.2fx", width,
                    height, boxSize, time, internalTime, (1.0 * time) / internalTime));
            // Assertions.assertTrue(String.format("Not faster: [%dx%d] @ %d : %d > %d", width,
            // height, boxSize,
            // blockTime, time), blockTime < time);
          }
        }
      }
      // if (debug)
      logger.log(TestLogging.getTimingRecord("float blockFind" + boxSize, boxTotal,
          "float blockFindInternal", internalBoxTotal));
      // This is not always faster for the 15-size block so leave commented out.
      // Assertions.assertTrue(String.format("Internal not faster: Block %d : %d > %d", boxSize,
      // blockBoxTotal, boxTotal), blockBoxTotal < boxTotal);
    }
    logger.log(TestLevel.TEST_INFO,
        FormatSupplier.getSupplier("float blockFind %d => blockFindInternal %d = %.2fx", total,
            internalTotal, (1.0 * total) / internalTotal));
    logger.log(TestLogging.getTimingRecord("float blockFind (border >= 5)", bigTotal,
        "float blockFindInternal (border >= 5)", bigInternalTotal));
  }

  @SpeedTag
  @SeededTest
  void floatBlockFindInternalIsFasterWithoutNeighbourCheck(RandomSeed seed) {
    Assumptions.assumeTrue(TestSettings.allow(TestComplexity.MEDIUM));

    final UniformRandomProvider rg = RngFactory.create(seed.get());

    final NonMaximumSuppression nms = new NonMaximumSuppression();

    final ArrayList<float[]> dataSet = floatCreateSpeedData(rg);
    final ArrayList<Long> noCheckTimes = new ArrayList<>();

    // Initialise
    nms.setNeighbourCheck(false);
    nms.blockFindNxNInternal(dataSet.get(0), primes[0], primes[0], boxSizes[0], boxSizes[0]);

    for (final int boxSize : boxSizes) {
      for (final int width : primes) {
        for (final int height : primes) {
          long time = System.nanoTime();
          for (final float[] data : dataSet) {
            nms.blockFindNxNInternal(data, width, height, boxSize, boxSize);
          }
          time = System.nanoTime() - time;
          noCheckTimes.add(time);
        }
      }
    }

    nms.setNeighbourCheck(true);
    nms.blockFindNxNInternal(dataSet.get(0), primes[0], primes[0], boxSizes[0], boxSizes[0]);

    long checkTotal = 0;
    long noCheckTotal = 0;
    long bigCheckTotal = 0;
    long bigNoCheckTotal = 0;
    int index = 0;
    for (final int boxSize : boxSizes) {
      long checkBoxTotal = 0;
      long noCheckBoxTotal = 0;
      for (final int width : primes) {
        for (final int height : primes) {
          long time = System.nanoTime();
          for (final float[] data : dataSet) {
            nms.blockFindNxNInternal(data, width, height, boxSize, boxSize);
          }
          time = System.nanoTime() - time;

          final long noCheckTime = noCheckTimes.get(index++);
          checkTotal += time;
          if (boxSize >= 5) {
            bigCheckTotal += time;
            bigNoCheckTotal += noCheckTime;
          }
          noCheckTotal += noCheckTime;
          checkBoxTotal += time;
          noCheckBoxTotal += noCheckTime;
          if (debug) {
            logger.log(TestLevel.TEST_DEBUG, FormatSupplier.getSupplier(
                "float blockFindInternal check [%dx%d] @ %d : %d => blockFindInternal %d = %.2fx",
                width, height, boxSize, time, noCheckTime, (1.0 * time) / noCheckTime));
            // Assertions.assertTrue(String.format("Without neighbour check not faster: [%dx%d] @ %d
            // : %d > %d", width, height, boxSize,
            // blockTime, time), blockTime < time);
          }
        }
      }
      // if (debug)
      logger.log(TestLogging.getTimingRecord("float blockFindInternal check" + boxSize,
          checkBoxTotal, "float blockFindInternal", noCheckBoxTotal));
      // This is not always faster for the 15-size block so leave commented out.
      // Assertions.assertTrue(String.format("Without neighbour check not faster: Block %d : %d >
      // %d", boxSize,
      // blockBoxTotal, boxTotal), blockBoxTotal < boxTotal);
    }
    logger.log(TestLevel.TEST_INFO,
        FormatSupplier.getSupplier(
            "float blockFindInternal check %d => blockFindInternal %d = %.2fx", checkTotal,
            noCheckTotal, (1.0 * checkTotal) / noCheckTotal));
    logger.log(TestLogging.getTimingRecord("float blockFindInternal check (border >= 5)",
        bigCheckTotal, "float blockFindInternal (border >= 5)", bigNoCheckTotal));
  }

  @SpeedTag
  @SeededTest
  void floatBlockFindIsFasterWithoutNeighbourCheck(RandomSeed seed) {
    Assumptions.assumeTrue(TestSettings.allow(TestComplexity.MEDIUM));

    final UniformRandomProvider rg = RngFactory.create(seed.get());

    final NonMaximumSuppression nms = new NonMaximumSuppression();

    final ArrayList<float[]> dataSet = floatCreateSpeedData(rg);
    final ArrayList<Long> noCheckTimes = new ArrayList<>();

    // Initialise
    nms.setNeighbourCheck(false);
    nms.blockFindNxN(dataSet.get(0), primes[0], primes[0], boxSizes[0]);

    for (final int boxSize : boxSizes) {
      for (final int width : primes) {
        for (final int height : primes) {
          long time = System.nanoTime();
          for (final float[] data : dataSet) {
            nms.blockFindNxN(data, width, height, boxSize);
          }
          time = System.nanoTime() - time;
          noCheckTimes.add(time);
        }
      }
    }

    nms.setNeighbourCheck(true);
    nms.blockFindNxN(dataSet.get(0), primes[0], primes[0], boxSizes[0]);

    long checkTotal = 0;
    long noCheckTotal = 0;
    long bigCheckTotal = 0;
    long bigNoCheckTotal = 0;
    int index = 0;
    for (final int boxSize : boxSizes) {
      long checkBoxTotal = 0;
      long noCheckBoxTotal = 0;
      for (final int width : primes) {
        for (final int height : primes) {
          long time = System.nanoTime();
          for (final float[] data : dataSet) {
            nms.blockFindNxN(data, width, height, boxSize);
          }
          time = System.nanoTime() - time;

          final long noCheckTime = noCheckTimes.get(index++);
          checkTotal += time;
          if (boxSize >= 5) {
            bigCheckTotal += time;
            bigNoCheckTotal += noCheckTime;
          }
          noCheckTotal += noCheckTime;
          checkBoxTotal += time;
          noCheckBoxTotal += noCheckTime;
          if (debug) {
            logger.log(TestLevel.TEST_DEBUG,
                FormatSupplier.getSupplier(
                    "float blockFind check [%dx%d] @ %d : %d => blockFind %d = %.2fx", width,
                    height, boxSize, time, noCheckTime, (1.0 * time) / noCheckTime));
            // Assertions.assertTrue(String.format("Without neighbour check not faster: [%dx%d] @ %d
            // : %d > %d", width, height, boxSize,
            // blockTime, time), blockTime < time);
          }
        }
      }
      // if (debug)
      logger.log(TestLogging.getTimingRecord("float blockFind check" + boxSize, checkBoxTotal,
          "float blockFind", noCheckBoxTotal));
      // This is not always faster for the 15-size block so leave commented out.
      // Assertions.assertTrue(String.format("Without neighbour check not faster: Block %d : %d >
      // %d", boxSize,
      // blockBoxTotal, boxTotal), blockBoxTotal < boxTotal);
    }
    logger.log(TestLevel.TEST_INFO,
        FormatSupplier.getSupplier("float blockFind check %d => blockFind %d = %.2fx", checkTotal,
            noCheckTotal, (1.0 * checkTotal) / noCheckTotal));
    logger.log(TestLogging.getResultRecord(bigNoCheckTotal <= bigCheckTotal,
        "float blockFind check %d  (border >= 5) => blockFind %d = %.2fx", bigCheckTotal,
        bigNoCheckTotal, (1.0 * bigCheckTotal) / bigNoCheckTotal));
  }

  @SpeedTag
  @SeededTest
  void floatBlockFind3x3MethodIsFasterThanBlockFindNxN(RandomSeed seed) {
    Assumptions.assumeTrue(TestSettings.allow(TestComplexity.MEDIUM));

    final UniformRandomProvider rg = RngFactory.create(seed.get());

    final NonMaximumSuppression nms = new NonMaximumSuppression();

    final ArrayList<float[]> dataSet = floatCreateSpeedData(rg);
    final ArrayList<Long> blockTimes = new ArrayList<>();

    // Initialise
    nms.blockFind3x3(dataSet.get(0), primes[0], primes[0]);
    nms.blockFindNxN(dataSet.get(0), primes[0], primes[0], 1);

    for (final int width : primes) {
      for (final int height : primes) {
        final long time = System.nanoTime();
        for (final float[] data : dataSet) {
          nms.blockFind3x3(data, width, height);
        }
        blockTimes.add(System.nanoTime() - time);
      }
    }

    long total = 0;
    long blockTotal = 0;
    int index = 0;
    for (final int width : primes) {
      for (final int height : primes) {
        long time = System.nanoTime();
        for (final float[] data : dataSet) {
          nms.blockFindNxN(data, width, height, 1);
        }
        time = System.nanoTime() - time;

        final long blockTime = blockTimes.get(index++);
        total += time;
        blockTotal += blockTime;
        if (debug) {
          logger.log(TestLevel.TEST_DEBUG,
              FormatSupplier.getSupplier(
                  "float blockFindNxN [%dx%d] : %d => blockFind3x3 %d = %.2fx", width, height, time,
                  blockTime, (1.0 * time) / blockTime));
          // This can be close so do not allow fail on single cases
          // Assertions.assertTrue(String.format("Not faster: [%dx%d] : %d > %d", width, height,
          // blockTime, time),
          // blockTime < time);
        }
      }
    }
    logger.log(
        TestLogging.getTimingRecord("float blockFindNxN", total, "float blockFind3x3", blockTotal));
  }

  @SpeedTag
  @SeededTest
  void floatBlockFind3x3WithBufferIsFasterThanBlockFind3x3(RandomSeed seed) {
    Assumptions.assumeTrue(TestSettings.allow(TestComplexity.MEDIUM));

    final UniformRandomProvider rg = RngFactory.create(seed.get());

    final NonMaximumSuppression nms = new NonMaximumSuppression();
    nms.setDataBuffer(true);

    final NonMaximumSuppression nms2 = new NonMaximumSuppression();
    nms2.setDataBuffer(false);

    final ArrayList<float[]> dataSet = floatCreateSpeedData(rg);
    final ArrayList<Long> blockTimes = new ArrayList<>();

    // Initialise
    nms.blockFind3x3(dataSet.get(0), primes[0], primes[0]);
    nms2.blockFind3x3(dataSet.get(0), primes[0], primes[0]);

    for (final int width : primes) {
      for (final int height : primes) {
        long time = System.nanoTime();
        for (final float[] data : dataSet) {
          nms.blockFind3x3(data, width, height);
        }
        time = System.nanoTime() - time;
        blockTimes.add(time);
      }
    }

    long total = 0;
    long blockTotal = 0;
    int index = 0;
    for (final int width : primes) {
      for (final int height : primes) {
        long time = System.nanoTime();
        for (final float[] data : dataSet) {
          nms2.blockFind3x3(data, width, height);
        }
        time = System.nanoTime() - time;

        final long blockTime = blockTimes.get(index++);
        total += time;
        blockTotal += blockTime;
        if (debug) {
          logger.log(TestLevel.TEST_DEBUG,
              FormatSupplier.getSupplier(
                  "float blockFind3x3 [%dx%d] : %d => blockFind3x3 (buffer) %d = %.2fx", width,
                  height, time, blockTime, (1.0 * time) / blockTime));
          // This can be close so do not allow fail on single cases
          // Assertions.assertTrue(String.format("Not faster: [%dx%d] : %d > %d", width, height,
          // blockTime, time),
          // blockTime < time);
        }
      }
    }
    logger.log(TestLogging.getTimingRecord("float blockFind3x3", total,
        "float blockFind3x3 (buffer)", blockTotal));
  }

  @SpeedTag
  @SeededTest
  void floatBlockFind3x3MethodIsFasterThanMaxFind3x3(RandomSeed seed) {
    Assumptions.assumeTrue(TestSettings.allow(TestComplexity.MEDIUM));

    final UniformRandomProvider rg = RngFactory.create(seed.get());

    final NonMaximumSuppression nms = new NonMaximumSuppression();

    final ArrayList<float[]> dataSet = floatCreateSpeedData(rg);
    final ArrayList<Long> blockTimes = new ArrayList<>();

    // Initialise
    nms.blockFind3x3(dataSet.get(0), primes[0], primes[0]);
    nms.maxFind(dataSet.get(0), primes[0], primes[0], 1);

    for (final int width : primes) {
      for (final int height : primes) {
        long time = System.nanoTime();
        for (final float[] data : dataSet) {
          nms.blockFind3x3(data, width, height);
        }
        time = System.nanoTime() - time;
        blockTimes.add(time);
      }
    }

    long total = 0;
    long blockTotal = 0;
    int index = 0;
    for (final int width : primes) {
      for (final int height : primes) {
        long time = System.nanoTime();
        for (final float[] data : dataSet) {
          nms.maxFind(data, width, height, 1);
        }
        time = System.nanoTime() - time;

        final long blockTime = blockTimes.get(index++);
        total += time;
        blockTotal += blockTime;
        if (debug) {
          logger.log(TestLevel.TEST_DEBUG,
              FormatSupplier.getSupplier("float maxFind3x3 [%dx%d] : %d => blockFind3x3 %d = %.2fx",
                  width, height, time, blockTime, (1.0 * time) / blockTime));
          // Assertions.assertTrue(String.format("Not faster: [%dx%d] : %d > %d", width, height,
          // blockTime, time),
          // blockTime < time);
        }
      }
    }
    logger.log(
        TestLogging.getTimingRecord("float maxFind3x3", total, "float blockFind3x3", blockTotal));
  }

  /**
   * Test the maximum finding algorithms for the same result.
   */
  @SeededTest
  void floatAllFindBlockMethodsReturnSameResultForSize1(RandomSeed seed) {
    final UniformRandomProvider rg = RngFactory.create(seed.get());

    final NonMaximumSuppression nms = new NonMaximumSuppression();
    for (final int width : primes) {
      for (final int height : primes) {
        floatCompareBlockMethodsForSize1(rg, nms, width, height);
      }
    }
  }

  private static void floatCompareBlockMethodsForSize1(UniformRandomProvider rg,
      NonMaximumSuppression nms, int width, int height) {
    final float[] data = floatCreateData(rg, width, height);

    final int[] blockNxNIndices = nms.findBlockMaximaNxN(data, width, height, 1);
    final int[] block2x2Indices = nms.findBlockMaxima2x2(data, width, height);
    final int[] blockIndices = nms.findBlockMaxima(data, width, height, 1);

    Arrays.sort(blockNxNIndices);
    Arrays.sort(block2x2Indices);
    Arrays.sort(blockIndices);

    Assertions.assertArrayEquals(blockNxNIndices, block2x2Indices,
        FormatSupplier.getSupplier("Block vs 2x2 do not match: [%dx%d]", width, height));
    Assertions.assertArrayEquals(blockNxNIndices, blockIndices,
        FormatSupplier.getSupplier("Block vs default do not match: [%dx%d]", width, height));
  }

  static float[] floatCreateData(UniformRandomProvider rg, int width, int height) {
    final float[] data = new float[width * height];
    for (int i = data.length; i-- > 0;) {
      data[i] = i;
    }

    RandomUtils.shuffle(data, rg);

    return data;
  }

  private static float[] floatCreateRepeatData(UniformRandomProvider rg, int width, int height,
      int modulus) {
    final float[] data = new float[width * height];
    for (int i = data.length; i-- > 0;) {
      data[i] = i % modulus;
    }

    RandomUtils.shuffle(data, rg);

    return data;
  }

  private static float[] floatCreatePatternData(int width, int height, float v00, float v01,
      float v10, float v11) {
    final float[] row1 = new float[width + 2];
    final float[] row2 = new float[width + 2];
    for (int x = 0; x < width; x += 2) {
      row1[x] = v00;
      row1[x + 1] = v01;
      row2[x] = v10;
      row2[x + 1] = v11;
    }

    final float[] data = new float[width * height];
    for (int y = 0; y < height; y++) {
      final float[] row = (y % 2 == 0) ? row1 : row2;
      System.arraycopy(row, 0, data, y * width, width);
    }

    return data;
  }

  // XXX: Copy methods up to here for 'int' versions
  @Test
  void intTestScanCandidate() {
    final IntScanCandidate candidates = new IntScanCandidate();
    final int[] data = {0, 1, 2, 3, 4, 4, 5};
    final int[] scan1 = null;
    final int[] scan2 = null;
    final int[] scan3 = null;
    final int[] scan4 = null;
    candidates.add(data, 1, scan1);
    Assertions.assertEquals(1, candidates.size());
    Assertions.assertEquals(1, candidates.getMaxIndex(0));
    Assertions.assertSame(scan1, candidates.getScan(0));
    candidates.add(data, 4, scan2);
    candidates.add(data, 5, scan3);
    Assertions.assertEquals(2, candidates.size());
    Assertions.assertEquals(4, candidates.getMaxIndex(0));
    Assertions.assertEquals(5, candidates.getMaxIndex(1));
    Assertions.assertSame(scan2, candidates.getScan(0));
    Assertions.assertSame(scan3, candidates.getScan(1));
    candidates.add(data, 6, scan4);
    Assertions.assertEquals(1, candidates.size());
    Assertions.assertEquals(6, candidates.getMaxIndex(0));
    Assertions.assertSame(scan4, candidates.getScan(0));
  }

  @SeededTest
  void intBlockFindAndMaxFindReturnSameResult(RandomSeed seed) {
    final UniformRandomProvider rg = RngFactory.create(seed.get());
    final NonMaximumSuppression nms = new NonMaximumSuppression();

    for (final int width : primes) {
      for (final int height : primes) {
        for (final int boxSize : boxSizes) {
          intCompareBlockFindToMaxFind(rg, nms, width, height, boxSize);
        }
      }
    }
  }

  @SeededTest
  void intBlockFindReturnSameResultWithNeighbourCheck(RandomSeed seed) {
    final UniformRandomProvider rg = RngFactory.create(seed.get());
    final NonMaximumSuppression nms = new NonMaximumSuppression();

    for (final int width : primes) {
      for (final int height : primes) {
        for (final int boxSize : boxSizes) {
          intCompareBlockFindWithNeighbourCheck(rg, nms, width, height, boxSize);
        }
      }
    }
  }

  private static void intCompareBlockFindWithNeighbourCheck(UniformRandomProvider rg,
      NonMaximumSuppression nms, int width, int height, int boxSize) {
    // Random data
    final int[] data = intCreateData(rg, width, height);
    nms.setNeighbourCheck(false);
    final int[] blockIndices1 = nms.blockFindNxN(data, width, height, boxSize);
    nms.setNeighbourCheck(true);
    final int[] blockIndices2 = nms.blockFindNxN(data, width, height, boxSize);

    Assertions.assertArrayEquals(blockIndices1, blockIndices2,
        FormatSupplier.getSupplier("Indices do not match: [%dx%d] @ %d", width, height, boxSize));
  }

  @Test
  void intBlockFindAndMaxFindReturnSameResultOnPatternDataWithNeighbourCheck() {
    final NonMaximumSuppression nms = new NonMaximumSuppression();
    nms.setNeighbourCheck(true);

    for (final int width : smallPrimes) {
      for (final int height : smallPrimes) {
        for (final int boxSize : boxSizes) {
          intCompareBlockFindToMaxFindWithPatternData(nms, width, height, boxSize);
        }
      }
    }
  }

  private void intCompareBlockFindToMaxFindWithPatternData(NonMaximumSuppression nms, int width,
      int height, int boxSize) {
    // This fails when N=2. Pattern data is problematic given the block find algorithm processes the
    // pixels in a different order
    // from a linear run across the yx order data. So when the pattern produces a max pixel within
    // the range of all
    // candidates on the top row of the block, the block algorithm will output a maxima from a
    // subsequent row. Standard
    // processing will just move further along the row (beyond the block boundary) to find the next
    // maxima.
    if (boxSize <= 2) {
      return;
    }

    // Pattern data
    intCompareBlockFindToMaxFind(nms, width, height, boxSize,
        intCreatePatternData(width, height, 1, 0, 0, 0), "Pattern1000");
    intCompareBlockFindToMaxFind(nms, width, height, boxSize,
        intCreatePatternData(width, height, 1, 0, 1, 0), "Pattern1010");
    intCompareBlockFindToMaxFind(nms, width, height, boxSize,
        intCreatePatternData(width, height, 1, 0, 0, 1), "Pattern1001");
    intCompareBlockFindToMaxFind(nms, width, height, boxSize,
        intCreatePatternData(width, height, 1, 1, 1, 0), "Pattern1110");
  }

  private void intCompareBlockFindToMaxFind(UniformRandomProvider rg, NonMaximumSuppression nms,
      int width, int height, int boxSize) {
    intCompareBlockFindToMaxFind(nms, width, height, boxSize, intCreateData(rg, width, height),
        "Random");

    // Empty data
    intCompareBlockFindToMaxFind(nms, width, height, boxSize, new int[width * height], "Empty");
  }

  private void intCompareBlockFindToMaxFind(NonMaximumSuppression nms, int width, int height,
      int boxSize, int[] data, String name) {
    final int[] blockIndices = nms.blockFindNxN(data, width, height, boxSize);
    final int[] maxIndices = nms.maxFind(data, width, height, boxSize);

    Arrays.sort(blockIndices);
    Arrays.sort(maxIndices);

    if (debug) {
      intCompareIndices(width, height, data, boxSize, blockIndices, maxIndices);
    }

    Assertions.assertArrayEquals(maxIndices, blockIndices, FormatSupplier
        .getSupplier("%s: Indices do not match: [%dx%d] @ %d", name, width, height, boxSize));
  }

  @SeededTest
  void intBlockFindInternalAndMaxFindInternalReturnSameResult(RandomSeed seed) {
    final UniformRandomProvider rg = RngFactory.create(seed.get());
    final NonMaximumSuppression nms = new NonMaximumSuppression();

    for (final int width : primes) {
      for (final int height : primes) {
        for (final int boxSize : boxSizes) {
          for (final int border : borderSizes) {
            intCompareBlockFindInternalToMaxFindInternal(rg, nms, width, height, boxSize, border);
          }
        }
      }
    }
  }

  @SeededTest
  void intBlockFindInternalReturnSameResultWithNeighbourCheck(RandomSeed seed) {
    final UniformRandomProvider rg = RngFactory.create(seed.get());
    final NonMaximumSuppression nms = new NonMaximumSuppression();

    for (final int width : primes) {
      for (final int height : primes) {
        for (final int boxSize : boxSizes) {
          for (final int border : borderSizes) {
            intCompareBlockFindInternalWithNeighbourCheck(rg, nms, width, height, boxSize, border);
          }
        }
      }
    }
  }

  private static void intCompareBlockFindInternalWithNeighbourCheck(UniformRandomProvider rg,
      NonMaximumSuppression nms, int width, int height, int boxSize, int border) {
    // Random data
    final int[] data = intCreateData(rg, width, height);
    nms.setNeighbourCheck(false);
    final int[] blockIndices1 = nms.blockFindNxNInternal(data, width, height, boxSize, border);
    nms.setNeighbourCheck(true);
    final int[] blockIndices2 = nms.blockFindNxNInternal(data, width, height, boxSize, border);

    Assertions.assertArrayEquals(blockIndices1, blockIndices2,
        FormatSupplier.getSupplier("Indices do not match: [%dx%d] @ %d", width, height, boxSize));
  }

  @Test
  public void
      intBlockFindInternalAndMaxFindInternalReturnSameResultOnPatternDataWithNeighbourCheck() {
    final NonMaximumSuppression nms = new NonMaximumSuppression();
    nms.setNeighbourCheck(true);

    for (final int width : smallPrimes) {
      for (final int height : smallPrimes) {
        for (final int boxSize : boxSizes) {
          for (final int border : borderSizes) {
            intCompareBlockFindInternalToMaxFindInternalWithPatternData(nms, width, height, boxSize,
                border);
          }
        }
      }
    }
  }

  private void intCompareBlockFindInternalToMaxFindInternalWithPatternData(
      NonMaximumSuppression nms, int width, int height, int boxSize, int border) {
    // This fails when N=2. Pattern data is problematic given the block find algorithm processes the
    // pixels in a different order from a linear run across the yx order data. So when the pattern
    // produces a max pixel within the range of all candidates on the top row of the block, the
    // block algorithm will output a maxima from a subsequent row. Standard processing will just
    // move further along the row (beyond the block boundary) to find the next maxima.
    if (boxSize <= 2) {
      return;
    }

    // Pattern data
    intCompareBlockFindInternalToMaxFindInternal(nms, width, height, boxSize, border,
        intCreatePatternData(width, height, 1, 0, 0, 0), "Pattern1000");
    intCompareBlockFindInternalToMaxFindInternal(nms, width, height, boxSize, border,
        intCreatePatternData(width, height, 1, 0, 1, 0), "Pattern1010");
    intCompareBlockFindInternalToMaxFindInternal(nms, width, height, boxSize, border,
        intCreatePatternData(width, height, 1, 0, 0, 1), "Pattern1001");
    intCompareBlockFindInternalToMaxFindInternal(nms, width, height, boxSize, border,
        intCreatePatternData(width, height, 1, 1, 1, 0), "Pattern1110");
  }

  private void intCompareBlockFindInternalToMaxFindInternal(UniformRandomProvider rg,
      NonMaximumSuppression nms, int width, int height, int boxSize, int border) {
    intCompareBlockFindInternalToMaxFindInternal(nms, width, height, boxSize, border,
        intCreateData(rg, width, height), "Random");

    // Empty data
    intCompareBlockFindInternalToMaxFindInternal(nms, width, height, boxSize, border,
        new int[width * height], "Empty");
  }

  private void intCompareBlockFindInternalToMaxFindInternal(NonMaximumSuppression nms, int width,
      int height, int boxSize, int border, int[] data, String name) {
    final int[] blockIndices = nms.blockFindNxNInternal(data, width, height, boxSize, border);
    final int[] maxIndices = nms.maxFindInternal(data, width, height, boxSize, border);

    Arrays.sort(blockIndices);
    Arrays.sort(maxIndices);

    if (debug) {
      intCompareIndices(width, height, data, boxSize, blockIndices, maxIndices);
    }

    Assertions.assertArrayEquals(maxIndices, blockIndices, FormatSupplier.getSupplier(
        "%s: Indices do not match: [%dx%d] @ %d (%d)", name, width, height, boxSize, border));
  }

  private static void intCompareIndices(int width, int height, int[] data, int boxSize,
      int[] indices1, int[] indices2) {
    if (logger.isLoggable(TestLevel.TEST_INFO)) {
      final StringBuilder sb = new StringBuilder();
      try (Formatter formatter = new Formatter(sb)) {
        formatter.format("int [%dx%d@%d] i1 = %d; int i2 =  %d%n", width, height, boxSize,
            indices1.length, indices2.length);
        int i1 = 0;
        int i2 = 0;
        while (i1 < indices1.length || i2 < indices2.length) {
          final int i = (i1 < indices1.length) ? indices1[i1] : Integer.MAX_VALUE;
          final int j = (i2 < indices2.length) ? indices2[i2] : Integer.MAX_VALUE;

          if (i == j) {
            formatter.format("int   [%d,%d] = [%d,%d]%n", i % width, i / width, j % width,
                j / width);
            i1++;
            i2++;
          } else if (i < j) {
            formatter.format("int   [%d,%d] : -%n", i % width, i / width);
            i1++;
          } else if (i > j) {
            formatter.format("int   - : [%d,%d]%n", j % width, j / width);
            i2++;
          }
        }
      }
      logger.log(TestLevel.TEST_INFO, sb.toString());
      if (!Arrays.equals(indices1, indices2)) {
        logger.log(TestLevel.TEST_INFO, "Arrays are not equal");
      }
    }
  }

  @Test
  void intFindBlockMaximaNxNInternalWithNoBlocks() {
    final NonMaximumSuppression nms = new NonMaximumSuppression();
    final int[] data = new int[12];
    Assertions.assertArrayEquals(new int[0], nms.findBlockMaximaNxNInternal(data, 12, 1, 2, 10));
    Assertions.assertArrayEquals(new int[0], nms.findBlockMaximaNxNInternal(data, 1, 12, 2, 10));
  }

  @Test
  void intFindBlockMaximaCandidatesNxNInternalWithNoBlocks() {
    final NonMaximumSuppression nms = new NonMaximumSuppression();
    final int[] data = new int[12];
    Assertions.assertArrayEquals(new int[0][0],
        nms.findBlockMaximaCandidatesNxNInternal(data, 12, 1, 2, 10));
    Assertions.assertArrayEquals(new int[0][0],
        nms.findBlockMaximaCandidatesNxNInternal(data, 1, 12, 2, 10));
  }

  @SeededTest
  void intFindBlockMaxima(RandomSeed seed) {
    final UniformRandomProvider rg = RngFactory.create(seed.get());
    final NonMaximumSuppression nms = new NonMaximumSuppression();
    final int width = 10;
    final int height = 15;
    final int[] data = intCreateData(rg, width, height);
    Assertions.assertArrayEquals(nms.findBlockMaxima2x2(data, width, height),
        nms.findBlockMaxima(data, width, height, 1));
    Assertions.assertArrayEquals(nms.findBlockMaximaNxN(data, width, height, 2),
        nms.findBlockMaxima(data, width, height, 2));
  }

  @SeededTest
  void intFindBlockMaximaCandidates(RandomSeed seed) {
    final UniformRandomProvider rg = RngFactory.create(seed.get());
    final NonMaximumSuppression nms = new NonMaximumSuppression();
    final int width = 10;
    final int height = 15;
    final int[] data = intCreateData(rg, width, height);
    Assertions.assertArrayEquals(nms.findBlockMaximaCandidates2x2(data, width, height),
        nms.findBlockMaximaCandidates(data, width, height, 1));
    Assertions.assertArrayEquals(nms.findBlockMaximaCandidatesNxN(data, width, height, 2),
        nms.findBlockMaximaCandidates(data, width, height, 2));
  }

  @SeededTest
  void intBlockFind(RandomSeed seed) {
    final UniformRandomProvider rg = RngFactory.create(seed.get());
    final NonMaximumSuppression nms = new NonMaximumSuppression();
    final int width = 10;
    final int height = 15;
    final int[] data = intCreateData(rg, width, height);
    Assertions.assertArrayEquals(nms.blockFind3x3(data, width, height),
        nms.blockFind(data, width, height, 1));
    Assertions.assertArrayEquals(nms.blockFindNxN(data, width, height, 2),
        nms.blockFind(data, width, height, 2));
  }

  @SeededTest
  void intBlockFindInternal(RandomSeed seed) {
    final UniformRandomProvider rg = RngFactory.create(seed.get());
    final NonMaximumSuppression nms = new NonMaximumSuppression();
    final int width = 10;
    final int height = 15;
    final int[] data = intCreateData(rg, width, height);
    Assertions.assertArrayEquals(nms.blockFind3x3Internal(data, width, height, 1),
        nms.blockFindInternal(data, width, height, 1, 1));
    Assertions.assertArrayEquals(nms.blockFindNxNInternal(data, width, height, 2, 1),
        nms.blockFindInternal(data, width, height, 2, 1));
  }

  @SeededTest
  void intBlockFind3x3InternalWithNoBorder(RandomSeed seed) {
    final UniformRandomProvider rg = RngFactory.create(seed.get());
    final NonMaximumSuppression nms = new NonMaximumSuppression();
    final int width = 10;
    final int height = 5;
    final int[] data = intCreateData(rg, width, height);
    Assertions.assertArrayEquals(nms.blockFind3x3(data, width, height),
        nms.blockFind3x3Internal(data, width, height, 0));
  }

  @SeededTest
  void intBlockFindNxNAndBlockFind3x3ReturnSameResult(RandomSeed seed) {
    final UniformRandomProvider rg = RngFactory.create(seed.get());

    final NonMaximumSuppression nms = new NonMaximumSuppression();

    for (int width : primes) {
      // 3x3 does not process to the edge of odd size images
      width++;

      for (int height : primes) {
        height++;

        final int[] data = intCreateData(rg, width, height);

        for (final boolean b : new boolean[] {false, true}) {
          nms.setNeighbourCheck(b);
          final int[] blockNxNIndices = nms.blockFindNxN(data, width, height, 1);
          final int[] block3x3Indices = nms.blockFind3x3(data, width, height);
          final int[] blockIndices = nms.blockFind(data, width, height, 1);

          Arrays.sort(blockNxNIndices);
          Arrays.sort(block3x3Indices);
          Arrays.sort(blockIndices);

          if (debug) {
            intCompareIndices(width, height, data, 1, blockNxNIndices, block3x3Indices);
          }

          Assertions.assertArrayEquals(blockNxNIndices, block3x3Indices,
              FormatSupplier.getSupplier("Indices do not match: [%dx%d] %b", width, height, b));
          Assertions.assertArrayEquals(blockNxNIndices, blockIndices,
              FormatSupplier.getSupplier("Indices do not match: [%dx%d] %b", width, height, b));
        }
      }
    }
  }

  @SeededTest
  void intBlockFindNxNInternalAndBlockFind3x3InternalReturnSameResult(RandomSeed seed) {
    final UniformRandomProvider rg = RngFactory.create(seed.get());

    final NonMaximumSuppression nms = new NonMaximumSuppression();

    for (int width : primes) {
      // 3x3 does not process to the edge of odd size images
      width++;

      for (int height : primes) {
        height++;

        final int[] data = intCreateData(rg, width, height);

        for (final boolean b : new boolean[] {false, true}) {
          nms.setNeighbourCheck(b);
          final int[] blockNxNIndices = nms.blockFindNxNInternal(data, width, height, 1, 1);
          final int[] block3x3Indices = nms.blockFind3x3Internal(data, width, height, 1);
          final int[] blockIndices = nms.blockFindInternal(data, width, height, 1, 1);

          Arrays.sort(blockNxNIndices);
          Arrays.sort(block3x3Indices);
          Arrays.sort(blockIndices);

          if (debug) {
            intCompareIndices(width, height, data, 1, blockNxNIndices, block3x3Indices);
          }

          Assertions.assertArrayEquals(blockNxNIndices, block3x3Indices,
              FormatSupplier.getSupplier("Indices do not match: [%dx%d] %b", width, height, b));
          Assertions.assertArrayEquals(blockNxNIndices, blockIndices,
              FormatSupplier.getSupplier("Indices do not match: [%dx%d] %b", width, height, b));
        }
      }
    }
  }

  @SeededTest
  void intFindBlockMaximaCandidatesNxNAndFindBlockMaximaCandidates2x2ReturnSameResult(
      RandomSeed seed) {
    final UniformRandomProvider rg = RngFactory.create(seed.get());

    final NonMaximumSuppression nms = new NonMaximumSuppression();

    for (final int width : sizes2x2) {
      for (final int height : sizes2x2) {
        for (final int mod : modulus2x2) {
          final int[] data = intCreateRepeatData(rg, width, height, mod);

          final int[][] blockNxNIndices = nms.findBlockMaximaCandidatesNxN(data, width, height, 1);
          final int[][] block2x2Indices = nms.findBlockMaximaCandidates2x2(data, width, height);
          final int[][] blockIndices = nms.findBlockMaximaCandidates(data, width, height, 1);

          flattenAndSort(blockNxNIndices);
          flattenAndSort(block2x2Indices);
          flattenAndSort(blockIndices);

          Assertions.assertArrayEquals(blockNxNIndices, block2x2Indices,
              FormatSupplier.getSupplier("Indices do not match: [%dx%d]", width, height));
          Assertions.assertArrayEquals(blockNxNIndices, blockIndices,
              FormatSupplier.getSupplier("Indices do not match: [%dx%d]", width, height));
        }
      }
    }
  }

  @SpeedTag
  @SeededTest
  void intBlockFindIsFasterThanMaxFind(RandomSeed seed) {
    Assumptions.assumeTrue(TestSettings.allow(TestComplexity.MEDIUM));

    final UniformRandomProvider rg = RngFactory.create(seed.get());

    final NonMaximumSuppression nms = new NonMaximumSuppression();

    final ArrayList<int[]> dataSet = intCreateSpeedData(rg);
    final ArrayList<Long> blockTimes = new ArrayList<>();

    // Initialise
    nms.blockFind(dataSet.get(0), primes[0], primes[0], boxSizes[0]);
    nms.maxFind(dataSet.get(0), primes[0], primes[0], boxSizes[0]);

    for (final int boxSize : boxSizes) {
      for (final int width : primes) {
        for (final int height : primes) {
          long time = System.nanoTime();
          for (final int[] data : dataSet) {
            nms.blockFind(data, width, height, boxSize);
          }
          time = System.nanoTime() - time;
          blockTimes.add(time);
        }
      }
    }

    long total = 0;
    long blockTotal = 0;
    int index = 0;
    for (final int boxSize : boxSizes) {
      long boxTotal = 0;
      long blockBoxTotal = 0;
      for (final int width : primes) {
        for (final int height : primes) {
          long time = System.nanoTime();
          for (final int[] data : dataSet) {
            nms.maxFind(data, width, height, boxSize);
          }
          time = System.nanoTime() - time;

          final long blockTime = blockTimes.get(index++);
          total += time;
          blockTotal += blockTime;
          boxTotal += time;
          blockBoxTotal += blockTime;
          if (debug) {
            logger.log(TestLevel.TEST_DEBUG,
                FormatSupplier.getSupplier("int maxFind [%dx%d] @ %d : %d => blockFind %d = %.2fx",
                    width, height, boxSize, time, blockTime, (1.0 * time) / blockTime));
            // Assertions.assertTrue(String.format("Not faster: [%dx%d] @ %d : %d > %d", width,
            // height, boxSize,
            // blockTime, time), blockTime < time);
          }
        }
      }
      // if (debug)
      logger.log(TestLogging.getTimingRecord("int maxFind" + boxSize, boxTotal, "int blockFind",
          blockBoxTotal));
      // if (boxSize > 1) // Sometimes this fails at small sizes
      // Assertions.assertTrue(String.format("Not faster: Block %d : %d > %d", boxSize,
      // blockBoxTotal, boxTotal),
      // blockBoxTotal < boxTotal);
    }
    logger.log(TestLogging.getTimingRecord("int maxFind", total, "int blockFind", blockTotal));
  }

  @SpeedTag
  @SeededTest
  void intBlockFindWithNeighbourCheckIsFasterThanMaxFind(RandomSeed seed) {
    Assumptions.assumeTrue(TestSettings.allow(TestComplexity.MEDIUM));

    final UniformRandomProvider rg = RngFactory.create(seed.get());

    final NonMaximumSuppression nms = new NonMaximumSuppression();
    nms.setNeighbourCheck(true);

    final ArrayList<int[]> dataSet = intCreateSpeedData(rg);
    final ArrayList<Long> blockTimes = new ArrayList<>();

    // Initialise
    nms.blockFind(dataSet.get(0), primes[0], primes[0], boxSizes[0]);
    nms.maxFind(dataSet.get(0), primes[0], primes[0], boxSizes[0]);

    for (final int boxSize : boxSizes) {
      for (final int width : primes) {
        for (final int height : primes) {
          long time = System.nanoTime();
          for (final int[] data : dataSet) {
            nms.blockFind(data, width, height, boxSize);
          }
          time = System.nanoTime() - time;
          blockTimes.add(time);
        }
      }
    }

    long total = 0;
    long blockTotal = 0;
    int index = 0;
    for (final int boxSize : boxSizes) {
      long boxTotal = 0;
      long blockBoxTotal = 0;
      for (final int width : primes) {
        for (final int height : primes) {
          long time = System.nanoTime();
          for (final int[] data : dataSet) {
            nms.maxFind(data, width, height, boxSize);
          }
          time = System.nanoTime() - time;

          final long blockTime = blockTimes.get(index++);
          total += time;
          blockTotal += blockTime;
          boxTotal += time;
          blockBoxTotal += blockTime;
          if (debug) {
            logger.log(TestLevel.TEST_DEBUG,
                FormatSupplier.getSupplier(
                    "int maxFind [%dx%d] @ %d : %d => blockFindWithCheck %d = %.2fx", width, height,
                    boxSize, time, blockTime, (1.0 * time) / blockTime));
            // Assertions.assertTrue(String.format("Not faster: [%dx%d] @ %d : %d > %d", width,
            // height, boxSize,
            // blockTime, time), blockTime < time);
          }
        }
      }
      // if (debug)
      logger.log(TestLogging.getTimingRecord("int maxFind" + boxSize, boxTotal,
          "int blockFindWithCheck", blockBoxTotal));
      // if (boxSize > 1) // Sometimes this fails at small sizes
      // Assertions.assertTrue(String.format("Not faster: Block %d : %d > %d", boxSize,
      // blockBoxTotal, boxTotal),
      // blockBoxTotal < boxTotal);
    }
    logger.log(
        TestLogging.getTimingRecord("int maxFind", total, "int blockFindWithCheck", blockTotal));
  }

  private ArrayList<int[]> intCreateSpeedData(UniformRandomProvider rg) {
    final int iter = ITER;

    final ArrayList<int[]> dataSet = new ArrayList<>(iter);
    for (int i = iter; i-- > 0;) {
      dataSet.add(intCreateData(rg, primes[0], primes[0]));
    }
    return dataSet;
  }

  @SpeedTag
  @SeededTest
  void intBlockFindNxNInternalIsFasterThanBlockFindNxNForBigBorders(RandomSeed seed) {
    // Note: This test is currently failing. The primes used to be:
    // int[] primes = new int[] { 997, 503, 251 };
    // Now with smaller primes (to increase the speed of running these tests)
    // this test fails. The time for the JVM to optimise the internal method
    // is high.
    // If all the tests are run then the similar test
    // intBlockFindInternalIsFasterWithoutNeighbourCheck shows much faster
    // times for the internal method.
    // This test should be changed to repeat until the times converge.

    Assumptions.assumeTrue(TestSettings.allow(TestComplexity.MEDIUM));

    final UniformRandomProvider rg = RngFactory.create(seed.get());

    final NonMaximumSuppression nms = new NonMaximumSuppression();

    final ArrayList<int[]> dataSet = intCreateSpeedData(rg);
    final ArrayList<Long> internalTimes = new ArrayList<>();

    for (final int boxSize : boxSizes) {
      for (final int width : primes) {
        for (final int height : primes) {
          // Initialise
          nms.blockFindNxNInternal(dataSet.get(0), width, height, boxSize, boxSize);
          long time = System.nanoTime();
          for (final int[] data : dataSet) {
            nms.blockFindNxNInternal(data, width, height, boxSize, boxSize);
          }
          time = System.nanoTime() - time;
          internalTimes.add(time);
        }
      }
    }

    long total = 0;
    long internalTotal = 0;
    long bigTotal = 0;
    long bigInternalTotal = 0;
    int index = 0;
    for (final int boxSize : boxSizes) {
      long boxTotal = 0;
      long internalBoxTotal = 0;
      for (final int width : primes) {
        for (final int height : primes) {
          // Initialise
          nms.blockFindNxN(dataSet.get(0), primes[0], primes[0], boxSizes[0]);
          long time = System.nanoTime();
          for (final int[] data : dataSet) {
            nms.blockFindNxN(data, width, height, boxSize);
          }
          time = System.nanoTime() - time;

          final long internalTime = internalTimes.get(index++);
          total += time;
          internalTotal += internalTime;
          if (boxSize >= 5) {
            bigTotal += time;
            bigInternalTotal += internalTime;
          }
          boxTotal += time;
          internalBoxTotal += internalTime;
          if (debug) {
            logger.log(TestLevel.TEST_DEBUG,
                FormatSupplier.getSupplier(
                    "int blockFind[%dx%d] @ %d : %d => blockFindInternal %d = %.2fx", width, height,
                    boxSize, time, internalTime, (1.0 * time) / internalTime));
            // Assertions.assertTrue(String.format("Not faster: [%dx%d] @ %d : %d > %d", width,
            // height, boxSize,
            // blockTime, time), blockTime < time);
          }
        }
      }
      // if (debug)
      logger.log(TestLogging.getTimingRecord("int blockFind" + boxSize, boxTotal,
          "int blockFindInternal", internalBoxTotal));
      // This is not always faster for the 15-size block so leave commented out.
      // Assertions.assertTrue(String.format("Internal not faster: Block %d : %d > %d", boxSize,
      // blockBoxTotal, boxTotal), blockBoxTotal < boxTotal);
    }
    logger.log(TestLevel.TEST_INFO,
        FormatSupplier.getSupplier("int blockFind %d => blockFindInternal %d = %.2fx", total,
            internalTotal, (1.0 * total) / internalTotal));
    logger.log(TestLogging.getTimingRecord("int blockFind (border >= 5)", bigTotal,
        "int blockFindInternal (border >= 5)", bigInternalTotal));
  }

  @SpeedTag
  @SeededTest
  void intBlockFindInternalIsFasterWithoutNeighbourCheck(RandomSeed seed) {
    Assumptions.assumeTrue(TestSettings.allow(TestComplexity.MEDIUM));

    final UniformRandomProvider rg = RngFactory.create(seed.get());

    final NonMaximumSuppression nms = new NonMaximumSuppression();

    final ArrayList<int[]> dataSet = intCreateSpeedData(rg);
    final ArrayList<Long> noCheckTimes = new ArrayList<>();

    // Initialise
    nms.setNeighbourCheck(false);
    nms.blockFindNxNInternal(dataSet.get(0), primes[0], primes[0], boxSizes[0], boxSizes[0]);

    for (final int boxSize : boxSizes) {
      for (final int width : primes) {
        for (final int height : primes) {
          long time = System.nanoTime();
          for (final int[] data : dataSet) {
            nms.blockFindNxNInternal(data, width, height, boxSize, boxSize);
          }
          time = System.nanoTime() - time;
          noCheckTimes.add(time);
        }
      }
    }

    nms.setNeighbourCheck(true);
    nms.blockFindNxNInternal(dataSet.get(0), primes[0], primes[0], boxSizes[0], boxSizes[0]);

    long checkTotal = 0;
    long noCheckTotal = 0;
    long bigCheckTotal = 0;
    long bigNoCheckTotal = 0;
    int index = 0;
    for (final int boxSize : boxSizes) {
      long checkBoxTotal = 0;
      long noCheckBoxTotal = 0;
      for (final int width : primes) {
        for (final int height : primes) {
          long time = System.nanoTime();
          for (final int[] data : dataSet) {
            nms.blockFindNxNInternal(data, width, height, boxSize, boxSize);
          }
          time = System.nanoTime() - time;

          final long noCheckTime = noCheckTimes.get(index++);
          checkTotal += time;
          if (boxSize >= 5) {
            bigCheckTotal += time;
            bigNoCheckTotal += noCheckTime;
          }
          noCheckTotal += noCheckTime;
          checkBoxTotal += time;
          noCheckBoxTotal += noCheckTime;
          if (debug) {
            logger.log(TestLevel.TEST_DEBUG,
                FormatSupplier.getSupplier(
                    "int blockFindInternal check [%dx%d] @ %d : %d => blockFindInternal %d = %.2fx",
                    width, height, boxSize, time, noCheckTime, (1.0 * time) / noCheckTime));
            // Assertions.assertTrue(String.format("Without neighbour check not faster: [%dx%d] @ %d
            // : %d > %d", width, height, boxSize,
            // blockTime, time), blockTime < time);
          }
        }
      }
      // if (debug)
      logger.log(TestLogging.getTimingRecord("int blockFindInternal check" + boxSize, checkBoxTotal,
          "int blockFindInternal", noCheckBoxTotal));
      // This is not always faster for the 15-size block so leave commented out.
      // Assertions.assertTrue(String.format("Without neighbour check not faster: Block %d : %d >
      // %d", boxSize,
      // blockBoxTotal, boxTotal), blockBoxTotal < boxTotal);
    }
    logger.log(TestLevel.TEST_INFO,
        FormatSupplier.getSupplier("int blockFindInternal check %d => blockFindInternal %d = %.2fx",
            checkTotal, noCheckTotal, (1.0 * checkTotal) / noCheckTotal));
    logger.log(TestLogging.getTimingRecord("int blockFindInternal check (border >= 5)",
        bigCheckTotal, "int blockFindInternal (border >= 5)", bigNoCheckTotal));
  }

  @SpeedTag
  @SeededTest
  void intBlockFindIsFasterWithoutNeighbourCheck(RandomSeed seed) {
    Assumptions.assumeTrue(TestSettings.allow(TestComplexity.MEDIUM));

    final UniformRandomProvider rg = RngFactory.create(seed.get());

    final NonMaximumSuppression nms = new NonMaximumSuppression();

    final ArrayList<int[]> dataSet = intCreateSpeedData(rg);
    final ArrayList<Long> noCheckTimes = new ArrayList<>();

    // Initialise
    nms.setNeighbourCheck(false);
    nms.blockFindNxN(dataSet.get(0), primes[0], primes[0], boxSizes[0]);

    for (final int boxSize : boxSizes) {
      for (final int width : primes) {
        for (final int height : primes) {
          long time = System.nanoTime();
          for (final int[] data : dataSet) {
            nms.blockFindNxN(data, width, height, boxSize);
          }
          time = System.nanoTime() - time;
          noCheckTimes.add(time);
        }
      }
    }

    nms.setNeighbourCheck(true);
    nms.blockFindNxN(dataSet.get(0), primes[0], primes[0], boxSizes[0]);

    long checkTotal = 0;
    long noCheckTotal = 0;
    long bigCheckTotal = 0;
    long bigNoCheckTotal = 0;
    int index = 0;
    for (final int boxSize : boxSizes) {
      long checkBoxTotal = 0;
      long noCheckBoxTotal = 0;
      for (final int width : primes) {
        for (final int height : primes) {
          long time = System.nanoTime();
          for (final int[] data : dataSet) {
            nms.blockFindNxN(data, width, height, boxSize);
          }
          time = System.nanoTime() - time;

          final long noCheckTime = noCheckTimes.get(index++);
          checkTotal += time;
          if (boxSize >= 5) {
            bigCheckTotal += time;
            bigNoCheckTotal += noCheckTime;
          }
          noCheckTotal += noCheckTime;
          checkBoxTotal += time;
          noCheckBoxTotal += noCheckTime;
          if (debug) {
            logger.log(TestLevel.TEST_DEBUG,
                FormatSupplier.getSupplier(
                    "int blockFind check [%dx%d] @ %d : %d => blockFind %d = %.2fx", width, height,
                    boxSize, time, noCheckTime, (1.0 * time) / noCheckTime));
            // Assertions.assertTrue(String.format("Without neighbour check not faster: [%dx%d] @ %d
            // : %d > %d", width, height, boxSize,
            // blockTime, time), blockTime < time);
          }
        }
      }
      // if (debug)
      logger.log(TestLogging.getTimingRecord("int blockFind check" + boxSize, checkBoxTotal,
          "int blockFind", noCheckBoxTotal));
      // This is not always faster for the 15-size block so leave commented out.
      // Assertions.assertTrue(String.format("Without neighbour check not faster: Block %d : %d >
      // %d", boxSize,
      // blockBoxTotal, boxTotal), blockBoxTotal < boxTotal);
    }
    logger.log(TestLevel.TEST_INFO,
        FormatSupplier.getSupplier("int blockFind check %d => blockFind %d = %.2fx", checkTotal,
            noCheckTotal, (1.0 * checkTotal) / noCheckTotal));
    logger.log(TestLogging.getResultRecord(bigNoCheckTotal <= bigCheckTotal,
        "int blockFind check %d  (border >= 5) => blockFind %d = %.2fx", bigCheckTotal,
        bigNoCheckTotal, (1.0 * bigCheckTotal) / bigNoCheckTotal));
  }

  @SpeedTag
  @SeededTest
  void intBlockFind3x3MethodIsFasterThanBlockFindNxN(RandomSeed seed) {
    Assumptions.assumeTrue(TestSettings.allow(TestComplexity.MEDIUM));

    final UniformRandomProvider rg = RngFactory.create(seed.get());

    final NonMaximumSuppression nms = new NonMaximumSuppression();

    final ArrayList<int[]> dataSet = intCreateSpeedData(rg);
    final ArrayList<Long> blockTimes = new ArrayList<>();

    // Initialise
    nms.blockFind3x3(dataSet.get(0), primes[0], primes[0]);
    nms.blockFindNxN(dataSet.get(0), primes[0], primes[0], 1);

    for (final int width : primes) {
      for (final int height : primes) {
        final long time = System.nanoTime();
        for (final int[] data : dataSet) {
          nms.blockFind3x3(data, width, height);
        }
        blockTimes.add(System.nanoTime() - time);
      }
    }

    long total = 0;
    long blockTotal = 0;
    int index = 0;
    for (final int width : primes) {
      for (final int height : primes) {
        long time = System.nanoTime();
        for (final int[] data : dataSet) {
          nms.blockFindNxN(data, width, height, 1);
        }
        time = System.nanoTime() - time;

        final long blockTime = blockTimes.get(index++);
        total += time;
        blockTotal += blockTime;
        if (debug) {
          logger.log(TestLevel.TEST_DEBUG,
              FormatSupplier.getSupplier("int blockFindNxN [%dx%d] : %d => blockFind3x3 %d = %.2fx",
                  width, height, time, blockTime, (1.0 * time) / blockTime));
          // This can be close so do not allow fail on single cases
          // Assertions.assertTrue(String.format("Not faster: [%dx%d] : %d > %d", width, height,
          // blockTime, time),
          // blockTime < time);
        }
      }
    }
    logger.log(
        TestLogging.getTimingRecord("int blockFindNxN", total, "int blockFind3x3", blockTotal));
  }

  @SpeedTag
  @SeededTest
  void intBlockFind3x3WithBufferIsFasterThanBlockFind3x3(RandomSeed seed) {
    Assumptions.assumeTrue(TestSettings.allow(TestComplexity.MEDIUM));

    final UniformRandomProvider rg = RngFactory.create(seed.get());

    final NonMaximumSuppression nms = new NonMaximumSuppression();
    nms.setDataBuffer(true);

    final NonMaximumSuppression nms2 = new NonMaximumSuppression();
    nms2.setDataBuffer(false);

    final ArrayList<int[]> dataSet = intCreateSpeedData(rg);
    final ArrayList<Long> blockTimes = new ArrayList<>();

    // Initialise
    nms.blockFind3x3(dataSet.get(0), primes[0], primes[0]);
    nms2.blockFind3x3(dataSet.get(0), primes[0], primes[0]);

    for (final int width : primes) {
      for (final int height : primes) {
        long time = System.nanoTime();
        for (final int[] data : dataSet) {
          nms.blockFind3x3(data, width, height);
        }
        time = System.nanoTime() - time;
        blockTimes.add(time);
      }
    }

    long total = 0;
    long blockTotal = 0;
    int index = 0;
    for (final int width : primes) {
      for (final int height : primes) {
        long time = System.nanoTime();
        for (final int[] data : dataSet) {
          nms2.blockFind3x3(data, width, height);
        }
        time = System.nanoTime() - time;

        final long blockTime = blockTimes.get(index++);
        total += time;
        blockTotal += blockTime;
        if (debug) {
          logger.log(TestLevel.TEST_DEBUG,
              FormatSupplier.getSupplier(
                  "int blockFind3x3 [%dx%d] : %d => blockFind3x3 (buffer) %d = %.2fx", width,
                  height, time, blockTime, (1.0 * time) / blockTime));
          // This can be close so do not allow fail on single cases
          // Assertions.assertTrue(String.format("Not faster: [%dx%d] : %d > %d", width, height,
          // blockTime, time),
          // blockTime < time);
        }
      }
    }
    logger.log(TestLogging.getTimingRecord("int blockFind3x3", total, "int blockFind3x3 (buffer)",
        blockTotal));
  }

  @SpeedTag
  @SeededTest
  void intBlockFind3x3MethodIsFasterThanMaxFind3x3(RandomSeed seed) {
    Assumptions.assumeTrue(TestSettings.allow(TestComplexity.MEDIUM));

    final UniformRandomProvider rg = RngFactory.create(seed.get());

    final NonMaximumSuppression nms = new NonMaximumSuppression();

    final ArrayList<int[]> dataSet = intCreateSpeedData(rg);
    final ArrayList<Long> blockTimes = new ArrayList<>();

    // Initialise
    nms.blockFind3x3(dataSet.get(0), primes[0], primes[0]);
    nms.maxFind(dataSet.get(0), primes[0], primes[0], 1);

    for (final int width : primes) {
      for (final int height : primes) {
        long time = System.nanoTime();
        for (final int[] data : dataSet) {
          nms.blockFind3x3(data, width, height);
        }
        time = System.nanoTime() - time;
        blockTimes.add(time);
      }
    }

    long total = 0;
    long blockTotal = 0;
    int index = 0;
    for (final int width : primes) {
      for (final int height : primes) {
        long time = System.nanoTime();
        for (final int[] data : dataSet) {
          nms.maxFind(data, width, height, 1);
        }
        time = System.nanoTime() - time;

        final long blockTime = blockTimes.get(index++);
        total += time;
        blockTotal += blockTime;
        if (debug) {
          logger.log(TestLevel.TEST_DEBUG,
              FormatSupplier.getSupplier("int maxFind3x3 [%dx%d] : %d => blockFind3x3 %d = %.2fx",
                  width, height, time, blockTime, (1.0 * time) / blockTime));
          // Assertions.assertTrue(String.format("Not faster: [%dx%d] : %d > %d", width, height,
          // blockTime, time),
          // blockTime < time);
        }
      }
    }
    logger
        .log(TestLogging.getTimingRecord("int maxFind3x3", total, "int blockFind3x3", blockTotal));
  }

  /**
   * Test the maximum finding algorithms for the same result.
   */
  @SeededTest
  void intAllFindBlockMethodsReturnSameResultForSize1(RandomSeed seed) {
    final UniformRandomProvider rg = RngFactory.create(seed.get());

    final NonMaximumSuppression nms = new NonMaximumSuppression();
    for (final int width : primes) {
      for (final int height : primes) {
        intCompareBlockMethodsForSize1(rg, nms, width, height);
      }
    }
  }

  private static void intCompareBlockMethodsForSize1(UniformRandomProvider rg,
      NonMaximumSuppression nms, int width, int height) {
    final int[] data = intCreateData(rg, width, height);

    final int[] blockNxNIndices = nms.findBlockMaximaNxN(data, width, height, 1);
    final int[] block2x2Indices = nms.findBlockMaxima2x2(data, width, height);
    final int[] blockIndices = nms.findBlockMaxima(data, width, height, 1);

    Arrays.sort(blockNxNIndices);
    Arrays.sort(block2x2Indices);
    Arrays.sort(blockIndices);

    Assertions.assertArrayEquals(blockNxNIndices, block2x2Indices,
        FormatSupplier.getSupplier("Block vs 2x2 do not match: [%dx%d]", width, height));
    Assertions.assertArrayEquals(blockNxNIndices, blockIndices,
        FormatSupplier.getSupplier("Block vs default do not match: [%dx%d]", width, height));
  }

  static int[] intCreateData(UniformRandomProvider rg, int width, int height) {
    final int[] data = new int[width * height];
    for (int i = data.length; i-- > 0;) {
      data[i] = i;
    }

    RandomUtils.shuffle(data, rg);

    return data;
  }

  private static int[] intCreateRepeatData(UniformRandomProvider rg, int width, int height,
      int modulus) {
    final int[] data = new int[width * height];
    for (int i = data.length; i-- > 0;) {
      data[i] = i % modulus;
    }

    RandomUtils.shuffle(data, rg);

    return data;
  }

  private static int[] intCreatePatternData(int width, int height, int v00, int v01, int v10,
      int v11) {
    final int[] row1 = new int[width + 2];
    final int[] row2 = new int[width + 2];
    for (int x = 0; x < width; x += 2) {
      row1[x] = v00;
      row1[x + 1] = v01;
      row2[x] = v10;
      row2[x + 1] = v11;
    }

    final int[] data = new int[width * height];
    for (int y = 0; y < height; y++) {
      final int[] row = (y % 2 == 0) ? row1 : row2;
      System.arraycopy(row, 0, data, y * width, width);
    }

    return data;
  }
}
