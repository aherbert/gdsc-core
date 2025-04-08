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
 * Copyright (C) 2011 - 2025 Alex Herbert
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
import org.apache.commons.statistics.descriptive.Median;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
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
class MedianWindowTest {
  private static Logger logger;

  @BeforeAll
  public static void beforeAll() {
    logger = Logger.getLogger(MedianWindowTest.class.getName());
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

  private final int dataSize = 2000;
  private final int[] radii = new int[] {0, 1, 2, 4, 8, 16};
  private final int[] speedRadii = new int[] {16, 32, 64};
  private final int testSpeedRadius = speedRadii[speedRadii.length - 1];
  private final int[] speedIncrement = new int[] {1, 2, 4, 8, 16};

  @SeededTest
  void testClassCanComputeActualMedian(RandomSeed seed) {
    final UniformRandomProvider rg = RngFactory.create(seed.get());
    final UpdateableSupplier msg = new UpdateableSupplier();

    double[] data = createRandomDataDouble(rg, dataSize);
    for (final int radius : radii) {
      for (int i = 0; i < data.length; i++) {
        final double median = calculateMedian(data, i, radius);
        final double median2 = calculateMedian2(data, i, radius);
        Assertions.assertEquals(median2, median, 1e-6, msg.update(i, radius));
      }
    }
    data = createRandomDataDouble(rg, dataSize + 1);
    for (final int radius : radii) {
      for (int i = 0; i < data.length; i++) {
        final double median = calculateMedian(data, i, radius);
        final double median2 = calculateMedian2(data, i, radius);
        Assertions.assertEquals(median2, median, 1e-6, msg.update(i, radius));
      }
    }
  }

  @SpeedTag
  @SeededTest
  void isFasterThanLocalSort(RandomSeed seed) {
    Assumptions.assumeTrue(TestSettings.allow(TestComplexity.LOW));
    final int[] speedRadii2 =
        (logger.isLoggable(TestLevel.TEST_INFO)) ? speedRadii : new int[] {testSpeedRadius};
    for (final int radius : speedRadii2) {
      for (final int increment : speedIncrement) {
        isFasterThanLocalSort(seed, radius, increment);
      }
    }
  }

  private void isFasterThanLocalSort(RandomSeed seed, int radius, int increment) {
    final UniformRandomProvider rg = RngFactory.create(seed.get());
    final int iterations = 20;
    final double[][] data = new double[iterations][];
    for (int i = 0; i < iterations; i++) {
      data[i] = createRandomDataDouble(rg, dataSize);
    }

    final double[] m1 = new double[dataSize];
    // Initialise class
    DoubleMedianWindow mw = new DoubleMedianWindow(data[0], radius);
    long t1;
    if (increment == 1) {
      do {
        mw.getMedian();
      } while (mw.increment());

      final long s1 = System.nanoTime();
      for (int iter = 0; iter < iterations; iter++) {
        mw = new DoubleMedianWindow(data[iter], radius);
        int index = 0;
        do {
          m1[index++] = mw.getMedian();
        } while (mw.increment());
      }
      t1 = System.nanoTime() - s1;
    } else {
      while (mw.isValidPosition()) {
        mw.getMedian();
        mw.increment(increment);
      }

      final long s1 = System.nanoTime();
      for (int iter = 0; iter < iterations; iter++) {
        mw = new DoubleMedianWindow(data[iter], radius);
        int index = 0;
        while (mw.isValidPosition()) {
          m1[index++] = mw.getMedian();
          mw.increment(increment);
        }
      }
      t1 = System.nanoTime() - s1;
    }

    final double[] m2 = new double[dataSize];
    // Initialise
    for (int i = 0; i < dataSize; i += increment) {
      calculateMedian(data[0], i, radius);
    }
    final long s2 = System.nanoTime();
    for (int iter = 0; iter < iterations; iter++) {
      for (int i = 0, j = 0; i < dataSize; i += increment) {
        m2[j++] = calculateMedian(data[iter], i, radius);
      }
    }
    final long t2 = System.nanoTime() - s2;

    Assertions.assertArrayEquals(m1, m2, 1e-6);
    logger.log(TestLevel.TEST_INFO,
        FormatSupplier.getSupplier("Radius %d, Increment %d : window %d : standard %d = %fx faster",
            radius, increment, t1, t2, (double) t2 / t1));

    // Only test the largest radii
    if (radius == testSpeedRadius) {
      Assertions.assertTrue(t1 < t2,
          FormatSupplier.getSupplier("Radius %d, Increment %d", radius, increment));
    }
  }

  @SpeedTag
  @SeededTest
  void floatVersionIsFasterThanDoubleVersion(RandomSeed seed) {
    Assumptions.assumeTrue(TestSettings.allow(TestComplexity.LOW));
    final int[] speedRadii2 =
        (logger.isLoggable(TestLevel.TEST_INFO)) ? speedRadii : new int[] {testSpeedRadius};
    for (final int radius : speedRadii2) {
      for (final int increment : speedIncrement) {
        floatVersionIsFasterThanDoubleVersion(seed, radius, increment);
      }
    }
  }

  private void floatVersionIsFasterThanDoubleVersion(RandomSeed seed, int radius, int increment) {
    final UniformRandomProvider rg = RngFactory.create(seed.get());
    final int iterations = 20;
    final double[][] data = new double[iterations][];
    final float[][] data2 = new float[iterations][];
    for (int i = 0; i < iterations; i++) {
      data[i] = createRandomDataDouble(rg, dataSize);
      data2[i] = copyDataFloat(data[i]);
    }

    final double[] m1 = new double[dataSize];
    // Initialise class
    DoubleMedianWindow mw = new DoubleMedianWindow(data[0], radius);
    long t1;
    if (increment == 1) {
      do {
        mw.getMedian();
      } while (mw.increment());

      final long s1 = System.nanoTime();
      for (int iter = 0; iter < iterations; iter++) {
        mw = new DoubleMedianWindow(data[iter], radius);
        int index = 0;
        do {
          m1[index++] = mw.getMedian();
        } while (mw.increment());
      }
      t1 = System.nanoTime() - s1;
    } else {
      while (mw.isValidPosition()) {
        mw.getMedian();
        mw.increment(increment);
      }

      final long s1 = System.nanoTime();
      for (int iter = 0; iter < iterations; iter++) {
        mw = new DoubleMedianWindow(data[iter], radius);
        int index = 0;
        while (mw.isValidPosition()) {
          m1[index++] = mw.getMedian();
          mw.increment(increment);
        }
      }
      t1 = System.nanoTime() - s1;
    }

    final double[] m2 = new double[dataSize];
    // Initialise
    FloatMedianWindow mw2 = new FloatMedianWindow(data2[0], radius);
    long t2;
    if (increment == 1) {
      do {
        mw2.getMedian();
      } while (mw2.increment());

      final long s2 = System.nanoTime();
      for (int iter = 0; iter < iterations; iter++) {
        mw2 = new FloatMedianWindow(data2[iter], radius);
        int index = 0;
        do {
          m2[index++] = mw2.getMedian();
        } while (mw2.increment());
      }
      t2 = System.nanoTime() - s2;
    } else {
      while (mw2.isValidPosition()) {
        mw2.getMedian();
        mw2.increment(increment);
      }

      final long s2 = System.nanoTime();
      for (int iter = 0; iter < iterations; iter++) {
        mw2 = new FloatMedianWindow(data2[iter], radius);
        int index = 0;
        while (mw2.isValidPosition()) {
          m2[index++] = mw2.getMedian();
          mw2.increment(increment);
        }
      }
      t2 = System.nanoTime() - s2;
    }

    Assertions.assertArrayEquals(m1, m2, 1e-3);

    // Only test the largest radii
    if (radius == testSpeedRadius) {
      // Allow a margin of error
      // Assertions.assertTrue(String.format("Radius %d, Increment %d", radius, increment), t2 < t1
      // * 1.1);
      logger.log(TestLogging.getResultRecord(t2 < t1,
          "Radius %d, Increment %d : double %d : float %d = %fx faster", radius, increment, t1, t2,
          (double) t1 / t2));
    } else {
      logger.log(TestLevel.TEST_INFO,
          FormatSupplier.getSupplier("Radius %d, Increment %d : double %d : float %d = %fx faster",
              radius, increment, t1, t2, (double) t1 / t2));
    }
  }

  static double calculateMedian(double[] data, int position, int radius) {
    final int start = Math.max(0, position - radius);
    final int end = Math.min(position + radius + 1, data.length);
    final double[] cache = new double[end - start];
    for (int i = start, j = 0; i < end; i++, j++) {
      cache[j] = data[i];
    }
    // TestLog.debugln(logger,Arrays.toString(cache));
    Arrays.sort(cache);
    return (cache[(cache.length - 1) / 2] + cache[cache.length / 2]) * 0.5;
  }

  static float calculateMedian(float[] data, int position, int radius) {
    final int start = Math.max(0, position - radius);
    final int end = Math.min(position + radius + 1, data.length);
    final float[] cache = new float[end - start];
    for (int i = start, j = 0; i < end; i++, j++) {
      cache[j] = data[i];
    }
    // TestLog.debugln(logger,Arrays.toString(cache));
    Arrays.sort(cache);
    return (cache[(cache.length - 1) / 2] + cache[cache.length / 2]) * 0.5f;
  }

  static float calculateMedian(int[] data, int position, int radius) {
    final int start = Math.max(0, position - radius);
    final int end = Math.min(position + radius + 1, data.length);
    final int[] cache = new int[end - start];
    for (int i = start, j = 0; i < end; i++, j++) {
      cache[j] = data[i];
    }
    // TestLog.debugln(logger,Arrays.toString(cache));
    Arrays.sort(cache);
    return (cache[(cache.length - 1) / 2] + cache[cache.length / 2]) * 0.5f;
  }

  static double calculateMedian2(double[] data, int position, int radius) {
    // Verify the internal median method using the Apache commons maths library
    final int start = Math.max(0, position - radius);
    final int end = Math.min(position + radius + 1, data.length);
    final double[] cache = new double[end - start];
    for (int i = start, j = 0; i < end; i++, j++) {
      cache[j] = data[i];
    }
    return Median.withDefaults().evaluate(cache);
  }

  static double[] createRandomDataDouble(UniformRandomProvider random, int size) {
    final double[] data = new double[size];
    for (int i = 0; i < data.length; i++) {
      data[i] = random.nextDouble() * size;
    }
    return data;
  }

  static double[] createDuplicateDataDouble(int size, double value) {
    final double[] data = new double[size];
    Arrays.fill(data, value);
    return data;
  }

  static double[] createSparseDataDouble(UniformRandomProvider rng, int size, double value) {
    final double[] data = new double[size];
    for (int i = 0; i < data.length; i++) {
      data[i] = value;
      if (i % 32 == 0) {
        value++;
      }
    }
    RandomUtils.shuffle(data, rng);
    return data;
  }

  static double[] createNaNDataDouble(UniformRandomProvider rng, int size, int start) {
    final double[] data = new double[size];
    for (int i = start; i < data.length; i++) {
      data[i] = i;
    }
    Arrays.fill(data, 0, start, Double.NaN);
    RandomUtils.shuffle(data, rng);
    return data;
  }

  static float[] createRandomDataFloat(UniformRandomProvider random, int size) {
    final float[] data = new float[size];
    for (int i = 0; i < data.length; i++) {
      data[i] = random.nextFloat() * size;
    }
    return data;
  }

  static float[] createDuplicateDataFloat(int size, float value) {
    final float[] data = new float[size];
    Arrays.fill(data, value);
    return data;
  }

  static float[] createSparseDataFloat(UniformRandomProvider rng, int size, float value) {
    final float[] data = new float[size];
    for (int i = 0; i < data.length; i++) {
      data[i] = value;
      if (i % 32 == 0) {
        value++;
      }
    }
    RandomUtils.shuffle(data, rng);
    return data;
  }

  static float[] createNaNDataFloat(UniformRandomProvider rng, int size, int start) {
    final float[] data = new float[size];
    for (int i = start; i < data.length; i++) {
      data[i] = i;
    }
    Arrays.fill(data, 0, start, Float.NaN);
    RandomUtils.shuffle(data, rng);
    return data;
  }

  static int[] createRandomDataInt(UniformRandomProvider random, int size) {
    final int[] data = new int[size];
    for (int i = 0; i < data.length; i++) {
      data[i] = random.nextInt(size);
    }
    return data;
  }

  static int[] createDuplicateDataInt(int size, int value) {
    final int[] data = new int[size];
    Arrays.fill(data, value);
    return data;
  }

  static int[] createSparseDataInt(UniformRandomProvider rng, int size, int value) {
    final int[] data = new int[size];
    for (int i = 0; i < data.length; i++) {
      data[i] = value;
      if (i % 32 == 0) {
        value++;
      }
    }
    RandomUtils.shuffle(data, rng);
    return data;
  }

  private static float[] copyDataFloat(double[] data) {
    return SimpleArrayUtils.toFloat(data);
  }
}
