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

import java.util.Arrays;
import java.util.function.Supplier;
import java.util.logging.Logger;
import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.test.junit5.RandomSeed;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.junit5.SpeedTag;
import uk.ac.sussex.gdsc.test.rng.RngUtils;
import uk.ac.sussex.gdsc.test.utils.TestComplexity;
import uk.ac.sussex.gdsc.test.utils.TestLogUtils;
import uk.ac.sussex.gdsc.test.utils.TestSettings;
import uk.ac.sussex.gdsc.test.utils.functions.FunctionUtils;

@SuppressWarnings({"javadoc"})
class DoubleLinkedMedianWindowTest {
  private static Logger logger;

  @BeforeAll
  public static void beforeAll() {
    logger = Logger.getLogger(DoubleLinkedMedianWindowTest.class.getName());
  }

  @AfterAll
  public static void afterAll() {
    logger = null;
  }

  private static class UpdateableSupplier implements Supplier<String> {
    int pos;
    int radius;

    @Override
    public String get() {
      return String.format("Position %d, Radius %d", pos, radius);
    }

    Supplier<String> update(int pos, int radius) {
      this.pos = pos;
      this.radius = radius;
      return this;
    }
  }

  int dataSize = 2000;
  int[] radii = new int[] {0, 1, 2, 4, 8, 16};
  double[] values = new double[] {0, -1.1, 2.2};
  int[] speedRadii = new int[] {16, 32, 64};
  int[] speedIncrement = new int[] {1, 2, 4, 6, 8, 12, 16, 24, 32, 48};

  @Test
  void testConstructor() {
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> new DoubleLinkedMedianWindow(null));
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> new DoubleLinkedMedianWindow(new double[0]));
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> new DoubleLinkedMedianWindow(new double[2]), "Length must be odd");
    for (int i = 1; i < 6; i += 2) {
      final DoubleLinkedMedianWindow mw =
          new DoubleLinkedMedianWindow(SimpleArrayUtils.newArray(i, 0.0, 1));
      Assertions.assertEquals(i, mw.getSize());
      Assertions.assertEquals(i / 2, mw.getMedian());
    }
  }

  @Test
  void canComputeMedianForRandomDataUsingDynamicLinkedListIfNewDataIsAboveMedian() {
    final double[] data = new double[] {1, 2, 3, 4, 5};

    final DoubleLinkedMedianWindow mw = new DoubleLinkedMedianWindow(data);
    double median = mw.getMedian();
    double median2 = MedianWindowTest.calculateMedian(data, 2, 2);
    Assertions.assertEquals(median2, median, 1e-6, "Before insert");

    final double[] insert = new double[] {6, 7, 6, 7};
    for (int i = 0; i < insert.length; i++) {
      mw.add(insert[i]);
      median = mw.getMedian();
      data[i] = insert[i];
      median2 = MedianWindowTest.calculateMedian(data, 2, 2);
      Assertions.assertEquals(median2, median, 1e-6, "After insert");
    }
  }

  @Test
  void canComputeMedianForRandomDataUsingDynamicLinkedListIfNewDataIsBelowMedian() {
    final double[] data = new double[] {4, 5, 6, 7, 8};

    final DoubleLinkedMedianWindow mw = new DoubleLinkedMedianWindow(data);
    double median = mw.getMedian();
    double median2 = MedianWindowTest.calculateMedian(data, 2, 2);
    Assertions.assertEquals(median2, median, 1e-6, "Before insert");

    final double[] insert = new double[] {3, 2, 3, 2};
    for (int i = 0; i < insert.length; i++) {
      mw.add(insert[i]);
      median = mw.getMedian();
      data[i] = insert[i];
      median2 = MedianWindowTest.calculateMedian(data, 2, 2);
      Assertions.assertEquals(median2, median, 1e-6, "After insert");
    }
  }

  @Test
  void canComputeMedianForRandomDataUsingDynamicLinkedListIfNewDataIsMedianOrAbove() {
    final double[] data = new double[] {1, 2, 3, 4, 5};

    final DoubleLinkedMedianWindow mw = new DoubleLinkedMedianWindow(data);
    double median = mw.getMedian();
    double median2 = MedianWindowTest.calculateMedian(data, 2, 2);
    Assertions.assertEquals(median2, median, 1e-6, "Before insert");

    final double[] insert = new double[] {3, 6, 3, 6};
    for (int i = 0; i < insert.length; i++) {
      mw.add(insert[i]);
      median = mw.getMedian();
      data[i] = insert[i];
      median2 = MedianWindowTest.calculateMedian(data, 2, 2);
      Assertions.assertEquals(median2, median, 1e-6, "After insert");
    }
  }

  @Test
  void canComputeMedianForRandomDataUsingDynamicLinkedListIfNewDataIsMedianOrBelow() {
    final double[] data = new double[] {1, 2, 3, 4, 5};

    final DoubleLinkedMedianWindow mw = new DoubleLinkedMedianWindow(data);
    double median = mw.getMedian();
    double median2 = MedianWindowTest.calculateMedian(data, 2, 2);
    Assertions.assertEquals(median2, median, 1e-6, "Before insert");

    final double[] insert = new double[] {3, 0, 3, 0};
    for (int i = 0; i < insert.length; i++) {
      mw.add(insert[i]);
      median = mw.getMedian();
      data[i] = insert[i];
      median2 = MedianWindowTest.calculateMedian(data, 2, 2);
      Assertions.assertEquals(median2, median, 1e-6, "After insert");
    }
  }

  @SeededTest
  void canComputeMedianForRandomDataUsingDynamicLinkedList(RandomSeed seed) {
    final UniformRandomProvider rg = RngUtils.create(seed.getSeed());
    final double[] data = MedianWindowTest.createRandomDataDouble(rg, dataSize);
    final UpdateableSupplier msg = new UpdateableSupplier();
    for (final int radius : radii) {
      final double[] startData = Arrays.copyOf(data, 2 * radius + 1);
      final DoubleLinkedMedianWindow mw = new DoubleLinkedMedianWindow(startData);
      int pos = 0;
      for (int i = 0; i < radius; i++, pos++) {
        final double median = mw.getMedianOldest(i + 1 + radius);
        final double median2 = MedianWindowTest.calculateMedian(data, pos, radius);
        // logger.log(TestLog.getRecord(Level.FINE, "Position %d, Radius %d : %g vs %g", p, radius,
        // median2, median));
        Assertions.assertEquals(median2, median, 1e-6, msg.update(pos, radius));
      }
      for (int j = startData.length; j < data.length; j++, pos++) {
        final double median = mw.getMedian();
        mw.add(data[j]);
        final double median2 = MedianWindowTest.calculateMedian(data, pos, radius);
        // logger.log(TestLog.getRecord(Level.FINE, "Position %d, Radius %d : %g vs %g", p, radius,
        // median2, median));
        Assertions.assertEquals(median2, median, 1e-6, msg.update(pos, radius));
      }
      for (int i = 2 * radius + 1; i-- > 0; pos++) {
        final double median = mw.getMedianYoungest(i + 1);
        final double median2 = MedianWindowTest.calculateMedian(data, pos, radius);
        // logger.log(TestLog.getRecord(Level.FINE, "Position %d, Radius %d : %g vs %g", p, radius,
        // median2, median));
        Assertions.assertEquals(median2, median, 1e-6, msg.update(pos, radius));
      }
    }
  }

  @SeededTest
  void canComputeMedianForSparseDataUsingDynamicLinkedList(RandomSeed seed) {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    final UpdateableSupplier msg = new UpdateableSupplier();
    for (final double value : values) {
      final double[] data = MedianWindowTest.createSparseDataDouble(rng, dataSize, value);
      for (final int radius : radii) {
        final double[] startData = Arrays.copyOf(data, 2 * radius + 1);
        final DoubleLinkedMedianWindow mw = new DoubleLinkedMedianWindow(startData);
        int pos = 0;
        for (int i = 0; i < radius; i++, pos++) {
          final double median = mw.getMedianOldest(i + 1 + radius);
          final double median2 = MedianWindowTest.calculateMedian(data, pos, radius);
          // logger.log(TestLog.getRecord(Level.FINE, "Position %d, Radius %d : %g vs %g", p,
          // radius, median2, median));
          Assertions.assertEquals(median2, median, 1e-6, msg.update(pos, radius));
        }
        for (int j = startData.length; j < data.length; j++, pos++) {
          final double median = mw.getMedian();
          mw.add(data[j]);
          final double median2 = MedianWindowTest.calculateMedian(data, pos, radius);
          // logger.log(TestLog.getRecord(Level.FINE, "Position %d, Radius %d : %g vs %g", p,
          // radius, median2, median));
          Assertions.assertEquals(median2, median, 1e-6, msg.update(pos, radius));
        }
        for (int i = 2 * radius + 1; i-- > 0; pos++) {
          final double median = mw.getMedianYoungest(i + 1);
          final double median2 = MedianWindowTest.calculateMedian(data, pos, radius);
          // logger.log(TestLog.getRecord(Level.FINE, "Position %d, Radius %d : %g vs %g", p,
          // radius, median2, median));
          Assertions.assertEquals(median2, median, 1e-6, msg.update(pos, radius));
        }
      }
    }
  }

  @Test
  void canComputeMedianForDuplicateDataUsingDynamicLinkedList() {
    final UpdateableSupplier msg = new UpdateableSupplier();
    for (final double value : values) {
      final double[] data = MedianWindowTest.createDuplicateDataDouble(dataSize, value);
      for (final int radius : radii) {
        final double[] startData = Arrays.copyOf(data, 2 * radius + 1);
        final DoubleLinkedMedianWindow mw = new DoubleLinkedMedianWindow(startData);
        int pos = 0;
        for (int i = 0; i < radius; i++, pos++) {
          final double median = mw.getMedianOldest(i + 1 + radius);
          final double median2 = MedianWindowTest.calculateMedian(data, pos, radius);
          // logger.log(TestLog.getRecord(Level.FINE, "Position %d, Radius %d : %g vs %g", p,
          // radius, median2, median));
          Assertions.assertEquals(median2, median, 1e-6, msg.update(pos, radius));
        }
        for (int j = startData.length; j < data.length; j++, pos++) {
          final double median = mw.getMedian();
          mw.add(data[j]);
          final double median2 = MedianWindowTest.calculateMedian(data, pos, radius);
          // logger.log(TestLog.getRecord(Level.FINE, "Position %d, Radius %d : %g vs %g", p,
          // radius, median2, median));
          Assertions.assertEquals(median2, median, 1e-6, msg.update(pos, radius));
        }
        for (int i = 2 * radius + 1; i-- > 0; pos++) {
          final double median = mw.getMedianYoungest(i + 1);
          final double median2 = MedianWindowTest.calculateMedian(data, pos, radius);
          // logger.log(TestLog.getRecord(Level.FINE, "Position %d, Radius %d : %g vs %g", p,
          // radius, median2, median));
          Assertions.assertEquals(median2, median, 1e-6, msg.update(pos, radius));
        }
      }
    }
  }

  @SeededTest
  void canComputeMedianForRange(RandomSeed seed) {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    double[] data = new double[] {1, 2, 3, 4, 5};

    final DoubleLinkedMedianWindow mw = new DoubleLinkedMedianWindow(data);
    // Extremes
    Assertions.assertEquals(Double.NaN, mw.getMedian(1, 0));
    Assertions.assertEquals(data[0], mw.getMedian(0, Integer.MIN_VALUE));
    Assertions.assertEquals(data[2], mw.getMedian(0, 10));
    Assertions.assertEquals(data[4], mw.getMedian(10, 10));
    for (int i = 0; i < data.length; i++) {
      Assertions.assertEquals(data[i], mw.getMedian(i, i));
    }

    double median = mw.getMedian();
    double median2 = MedianWindowTest.calculateMedian(data, 2, 2);
    Assertions.assertEquals(median2, median, 1e-6, "Before insert");
    median = mw.getMedian(1, 3);
    median2 = MedianWindowTest.calculateMedian(data, 2, 1);
    Assertions.assertEquals(median2, median, 1e-6, "Before insert");
    data = Arrays.copyOf(data, 15);
    for (int i = 5, j = 1; i < data.length; i++, j++) {
      final double value = 1 + rng.nextInt(10);
      mw.add(value);
      data[i] = value;
      median = mw.getMedian(0, 4);
      median2 = MedianWindowTest.calculateMedian(data, j + 2, 2);
      Assertions.assertEquals(median2, median, 1e-6);
      median = mw.getMedian(0, 2);
      median2 = MedianWindowTest.calculateMedian(data, j + 1, 1);
      Assertions.assertEquals(median2, median, 1e-6);
      median = mw.getMedian(1, 3);
      median2 = MedianWindowTest.calculateMedian(data, j + 2, 1);
      Assertions.assertEquals(median2, median, 1e-6);
      median = mw.getMedian(2, 4);
      median2 = MedianWindowTest.calculateMedian(data, j + 3, 1);
      Assertions.assertEquals(median2, median, 1e-6);
    }
  }

  @Test
  void canComputeOldest() {
    final double[] data = new double[] {1, 2, 3, 4, 5};
    final DoubleLinkedMedianWindow mw = new DoubleLinkedMedianWindow(data);
    Assertions.assertThrows(IllegalArgumentException.class, () -> mw.getMedianOldest(0));
    Assertions.assertEquals(data[0], mw.getMedianOldest(1));
    Assertions.assertEquals((data[0] + data[1]) * 0.5, mw.getMedianOldest(2));
    Assertions.assertEquals(data[1], mw.getMedianOldest(3));
    Assertions.assertEquals((data[1] + data[2]) * 0.5, mw.getMedianOldest(4));
    Assertions.assertEquals(data[2], mw.getMedianOldest(5));
    Assertions.assertEquals(data[2], mw.getMedianOldest(6));
  }

  @Test
  void canComputeYoungest() {
    final double[] data = new double[] {1, 2, 3, 4, 5};
    final DoubleLinkedMedianWindow mw = new DoubleLinkedMedianWindow(data);
    Assertions.assertThrows(IllegalArgumentException.class, () -> mw.getMedianYoungest(0));
    Assertions.assertEquals(data[4], mw.getMedianYoungest(1));
    Assertions.assertEquals((data[4] + data[3]) * 0.5, mw.getMedianYoungest(2));
    Assertions.assertEquals(data[3], mw.getMedianYoungest(3));
    Assertions.assertEquals((data[3] + data[2]) * 0.5, mw.getMedianYoungest(4));
    Assertions.assertEquals(data[2], mw.getMedianYoungest(5));
    Assertions.assertEquals(data[2], mw.getMedianYoungest(6));
  }

  @SpeedTag
  @SeededTest
  void isFasterThanMedianWindowUsingSortedCacheDataWhenIncrementIsSmall(RandomSeed seed) {
    Assumptions.assumeTrue(TestSettings.allow(TestComplexity.LOW));
    for (final int radius : speedRadii) {
      for (final int increment : speedIncrement) {
        if (increment > radius) {
          continue;
        }
        isFasterThanMedianWindowUsingSortedCacheDataWhenIncrementIsSmall(seed, radius, increment);
      }
    }
  }

  private void isFasterThanMedianWindowUsingSortedCacheDataWhenIncrementIsSmall(RandomSeed seed,
      int radius, int increment) {
    final UniformRandomProvider rg = RngUtils.create(seed.getSeed());
    final int iterations = 20;
    final double[][] data = new double[iterations][];
    for (int i = 0; i < iterations; i++) {
      data[i] = MedianWindowTest.createRandomDataDouble(rg, dataSize);
    }

    final double[] m1 = new double[dataSize];
    // Initialise class
    final int finalPosition = dataSize - radius;
    DoubleMedianWindow mw = new DoubleMedianWindow(data[0], radius);
    mw.setPosition(radius);
    long t1;
    if (increment == 1) {
      int index = 0;
      do {
        m1[index++] = mw.getMedian();
        mw.increment();
      } while (mw.getPosition() < finalPosition);

      final long s1 = System.nanoTime();
      for (int iter = 0; iter < iterations; iter++) {
        mw = new DoubleMedianWindow(data[iter], radius);
        mw.setPosition(radius);
        do {
          mw.getMedian();
          mw.increment();
        } while (mw.getPosition() < finalPosition);
      }
      t1 = System.nanoTime() - s1;
    } else {
      int index = 0;
      do {
        m1[index++] = mw.getMedian();
        mw.increment(increment);
      } while (mw.getPosition() < finalPosition);

      final long s1 = System.nanoTime();
      for (int iter = 0; iter < iterations; iter++) {
        mw = new DoubleMedianWindow(data[iter], radius);
        mw.setPosition(radius);
        do {
          mw.getMedian();
          mw.increment(increment);
        } while (mw.getPosition() < finalPosition);
      }
      t1 = System.nanoTime() - s1;
    }

    final double[] m2 = new double[dataSize];
    double[] startData = Arrays.copyOf(data[0], 2 * radius + 1);
    DoubleLinkedMedianWindow mw2 = new DoubleLinkedMedianWindow(startData);
    long t2;
    if (increment == 1) {
      int index = 0;
      m2[index++] = mw2.getMedian();
      for (int j = startData.length; j < data[0].length; j++) {
        mw2.add(data[0][j]);
        m2[index++] = mw2.getMedian();
      }
      final long s2 = System.nanoTime();
      for (int iter = 0; iter < iterations; iter++) {
        startData = Arrays.copyOf(data[iter], 2 * radius + 1);
        mw2 = new DoubleLinkedMedianWindow(startData);
        mw2.getMedian();
        for (int j = startData.length; j < data[iter].length; j++) {
          mw2.add(data[iter][j]);
          mw2.getMedian();
        }
      }
      t2 = System.nanoTime() - s2;
    } else {
      final int limit = data[0].length - increment;
      int index = 0;
      m2[index++] = mw2.getMedian();
      for (int j = startData.length; j < limit; j += increment) {
        for (int i = 0; i < increment; i++) {
          mw2.add(data[0][j + i]);
        }
        m2[index++] = mw2.getMedian();
      }
      final long s2 = System.nanoTime();
      for (int iter = 0; iter < iterations; iter++) {
        startData = Arrays.copyOf(data[iter], 2 * radius + 1);
        mw2 = new DoubleLinkedMedianWindow(startData);
        mw2.getMedian();
        for (int j = startData.length; j < limit; j += increment) {
          for (int i = 0; i < increment; i++) {
            mw2.add(data[iter][j + i]);
          }
          mw2.getMedian();
        }
      }
      t2 = System.nanoTime() - s2;
    }

    Assertions.assertArrayEquals(m1, m2, 1e-6,
        FunctionUtils.getSupplier("Radius %d, Increment %d", radius, increment));

    // Only test when the increment is small.
    // When the increment is large then the linked list is doing too many operations
    // verses the full array sort of the cache median window.
    if (increment <= 4) {
      logger.log(TestLogUtils.getResultRecord(t2 < t1,
          "Radius %d, Increment %d : Cached %d : DLL %d = %fx faster", radius, increment, t1, t2,
          (double) t1 / t2));
    } else {
      logger.info(
          FunctionUtils.getSupplier("Radius %d, Increment %d : Cached %d : DLL %d = %fx faster",
              radius, increment, t1, t2, (double) t1 / t2));
    }
  }
}
