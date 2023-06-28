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

package uk.ac.sussex.gdsc.core.utils.rng;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;
import org.apache.commons.rng.sampling.distribution.ContinuousUniformSampler;
import org.apache.commons.rng.sampling.distribution.DiscreteUniformSampler;
import org.apache.commons.rng.sampling.distribution.UniformLongSampler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.core.utils.rng.RngStreams.RangeSplittableDoubleSupplier;
import uk.ac.sussex.gdsc.core.utils.rng.RngStreams.RngSplittableDoubleSupplier;
import uk.ac.sussex.gdsc.core.utils.rng.RngStreams.RngSplittableIntSupplier;
import uk.ac.sussex.gdsc.core.utils.rng.RngStreams.RngSplittableLongSupplier;
import uk.ac.sussex.gdsc.core.utils.rng.RngStreams.SupplierDoubleSpliterator;
import uk.ac.sussex.gdsc.core.utils.rng.RngStreams.SupplierIntSpliterator;
import uk.ac.sussex.gdsc.core.utils.rng.RngStreams.SupplierLongSpliterator;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.utils.RandomSeed;

@SuppressWarnings("javadoc")
class RngStreamsTest {
  @Test
  void testRandomIntsThrowsWithBadSize() {
    final SplittableUniformRandomProvider rng = UniformRandomProviders.createSplittable(123);
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      RngStreams.ints(rng, -1);
    }, "Should throw with negative size");
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      RngStreams.ints(rng, -1, 0, 100);
    }, "Should throw with negative size for bounded output");
  }

  @Test
  void testRandomIntsThrowsWithBadRange() {
    final SplittableUniformRandomProvider rng = UniformRandomProviders.createSplittable(123);
    final int lower = 10;
    final int upper = 9;
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      RngStreams.ints(rng, lower, upper);
    }, "Should throw with bad range");
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      RngStreams.ints(rng, 10, lower, upper);
    }, "Should throw with bad range for fixed size stream");
  }

  @SeededTest
  void testRandomInts(RandomSeed randomSeed) {
    final SplittableUniformRandomProvider rng = Pcg32.xshrs(randomSeed.getAsLong());
    final int size = 7;
    final int[] values = RngStreams.ints(rng).limit(size).toArray();
    Assertions.assertEquals(size, values.length, "Incorrect stream length");
  }

  @SeededTest
  void testRandomIntsWithSize(RandomSeed randomSeed) {
    final SplittableUniformRandomProvider rng = Pcg32.xshrs(randomSeed.getAsLong());
    final int size = 7;
    final int[] values = RngStreams.ints(rng, size).toArray();
    Assertions.assertEquals(size, values.length, "Incorrect stream length");
  }

  @SeededTest
  void testRandomIntsWithSmallRange(RandomSeed randomSeed) {
    final SplittableUniformRandomProvider rng = Pcg32.xshrs(randomSeed.getAsLong());
    final int size = 7;
    final int lower = 44;
    final int upper = 99;
    final int[] values = RngStreams.ints(rng, lower, upper).limit(size).toArray();
    Assertions.assertEquals(size, values.length, "Incorrect stream length");
    for (final int value : values) {
      Assertions.assertTrue(value >= lower && value < upper, "Output is not within the bounds");
    }
  }

  @SeededTest
  void testRandomIntsWithLargeRange(RandomSeed randomSeed) {
    final SplittableUniformRandomProvider rng = Pcg32.xshrs(randomSeed.getAsLong());
    // Set the range in the middle so equal chance of below/above rejection path
    final int size = 50;
    final int lower = Integer.MIN_VALUE / 2 - 1;
    final int upper = Integer.MAX_VALUE / 2 + 1;
    final int[] values = RngStreams.ints(rng, lower, upper).limit(size).toArray();
    Assertions.assertEquals(size, values.length, "Incorrect stream length");
    for (final int value : values) {
      Assertions.assertTrue(value >= lower && value < upper, "Output is not within the bounds");
    }
  }

  @SeededTest
  void testRandomIntsWithSmallRangeWithSize(RandomSeed randomSeed) {
    final SplittableUniformRandomProvider rng = Pcg32.xshrs(randomSeed.getAsLong());
    final int size = 7;
    final int lower = 44;
    final int upper = 99;
    final int[] values = RngStreams.ints(rng, size, lower, upper).toArray();
    Assertions.assertEquals(size, values.length, "Incorrect stream length");
    for (final int value : values) {
      Assertions.assertTrue(value >= lower && value < upper, "Output is not within the bounds");
    }
  }

  @Test
  void testRandomLongsThrowsWithBadSize() {
    final SplittableUniformRandomProvider rng = UniformRandomProviders.createSplittable(123);
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      RngStreams.longs(rng, -1);
    }, "Should throw with negative size");
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      RngStreams.longs(rng, -1, 0, 100);
    }, "Should throw with negative size for bounded output");
  }

  @Test
  void testRandomLongsThrowsWithBadRange() {
    final SplittableUniformRandomProvider rng = UniformRandomProviders.createSplittable(123);
    final long lower = 10;
    final long upper = 9;
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      RngStreams.longs(rng, lower, upper);
    }, "Should throw with bad range");
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      RngStreams.longs(rng, 10, lower, upper);
    }, "Should throw with bad range for fixed size stream");
  }

  @SeededTest
  void testRandomLongs(RandomSeed randomSeed) {
    final SplittableUniformRandomProvider rng = Pcg32.xshrs(randomSeed.getAsLong());
    final int size = 7;
    final long[] values = RngStreams.longs(rng).limit(size).toArray();
    Assertions.assertEquals(size, values.length, "Incorrect stream length");
  }

  @SeededTest
  void testRandomLongsWithSize(RandomSeed randomSeed) {
    final SplittableUniformRandomProvider rng = Pcg32.xshrs(randomSeed.getAsLong());
    final int size = 7;
    final long[] values = RngStreams.longs(rng, size).toArray();
    Assertions.assertEquals(size, values.length, "Incorrect stream length");
  }

  @SeededTest
  void testRandomLongsWithSmallRange(RandomSeed randomSeed) {
    final SplittableUniformRandomProvider rng = Pcg32.xshrs(randomSeed.getAsLong());
    final int size = 7;
    final int lower = 44;
    final int upper = 99;
    final long[] values = RngStreams.longs(rng, lower, upper).limit(size).toArray();
    Assertions.assertEquals(size, values.length, "Incorrect stream length");
    for (final long value : values) {
      Assertions.assertTrue(value >= lower && value < upper, "Output is not within the bounds");
    }
  }

  @SeededTest
  void testRandomLongsWithLargeRange(RandomSeed randomSeed) {
    final SplittableUniformRandomProvider rng = Pcg32.xshrs(randomSeed.getAsLong());
    // Set the range in the middle so equal chance of below/above rejection path
    final int size = 50;
    final long lower = Long.MIN_VALUE / 2 - 1;
    final long upper = Long.MAX_VALUE / 2 + 1;
    final long[] values = RngStreams.longs(rng, lower, upper).limit(size).toArray();
    Assertions.assertEquals(size, values.length, "Incorrect stream length");
    for (final long value : values) {
      Assertions.assertTrue(value >= lower && value < upper, "Output is not within the bounds");
    }
  }

  @SeededTest
  void testRandomLongsWithSmallRangeWithSize(RandomSeed randomSeed) {
    final SplittableUniformRandomProvider rng = Pcg32.xshrs(randomSeed.getAsLong());
    final int size = 7;
    final int lower = 44;
    final int upper = 99;
    final long[] values = RngStreams.longs(rng, size, lower, upper).toArray();
    Assertions.assertEquals(size, values.length, "Incorrect stream length");
    for (final long value : values) {
      Assertions.assertTrue(value >= lower && value < upper, "Output is not within the bounds");
    }
  }

  @Test
  void testRandomDoublesThrowsWithBadSize() {
    final SplittableUniformRandomProvider rng = UniformRandomProviders.createSplittable(123);
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      RngStreams.doubles(rng, -1);
    }, "Should throw with negative size");
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      RngStreams.doubles(rng, -1, 0, 100);
    }, "Should throw with negative size for bounded output");
  }

  @Test
  void testRandomDoublesThrowsWithBadRange() {
    final SplittableUniformRandomProvider rng = UniformRandomProviders.createSplittable(123);
    final double lower = 10;
    final double upper = Double.NaN;
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      RngStreams.doubles(rng, lower, upper);
    }, "Should throw with bad range");
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      RngStreams.doubles(rng, 10, lower, upper);
    }, "Should throw with bad range for fixed size stream");
  }

  @SeededTest
  void testRandomDoubles(RandomSeed randomSeed) {
    final SplittableUniformRandomProvider rng = Pcg32.xshrs(randomSeed.getAsLong());
    final int size = 7;
    final double[] values = RngStreams.doubles(rng).limit(size).toArray();
    Assertions.assertEquals(size, values.length, "Incorrect stream length");
  }

  @SeededTest
  void testRandomDoublesWithSize(RandomSeed randomSeed) {
    final SplittableUniformRandomProvider rng = Pcg32.xshrs(randomSeed.getAsLong());
    final int size = 7;
    final double[] values = RngStreams.doubles(rng, size).toArray();
    Assertions.assertEquals(size, values.length, "Incorrect stream length");
  }

  @SeededTest
  void testRandomDoublesWithRange(RandomSeed randomSeed) {
    final SplittableUniformRandomProvider rng = Pcg32.xshrs(randomSeed.getAsLong());
    final int size = 7;
    final int lower = 44;
    final int upper = 99;
    final double[] values = RngStreams.doubles(rng, lower, upper).limit(size).toArray();
    Assertions.assertEquals(size, values.length, "Incorrect stream length");
    for (final double value : values) {
      Assertions.assertTrue(value >= lower && value < upper, "Output is not within the bounds");
    }
  }

  @SeededTest
  void testRandomDoublesWithRangeWithSize(RandomSeed randomSeed) {
    final SplittableUniformRandomProvider rng = Pcg32.xshrs(randomSeed.getAsLong());
    final int size = 7;
    final int lower = 44;
    final int upper = 99;
    final double[] values = RngStreams.doubles(rng, size, lower, upper).toArray();
    Assertions.assertEquals(size, values.length, "Incorrect stream length");
    for (final double value : values) {
      Assertions.assertTrue(value >= lower && value < upper, "Output is not within the bounds");
    }
  }

  /**
   * Test the doubles spliterator edge-case where the result from the generator is 1. This results
   * in the sampler value being set to the upper bound and then adjusted because the upper bound is
   * exclusive.
   */
  @Test
  void testRandomDoublesWithSampleAtUpperBound() {
    final SplittableUniformRandomProvider rng = new SplittableUniformRandomProvider() {
      @Override
      public void nextBytes(byte[] bytes) {
        // Ignore
      }

      @Override
      public void nextBytes(byte[] bytes, int start, int len) {
        // Ignore
      }

      @Override
      public int nextInt() {
        return 0;
      }

      @Override
      public int nextInt(int n) {
        return 0;
      }

      @Override
      public long nextLong() {
        return 0;
      }

      @Override
      public long nextLong(long n) {
        return 0;
      }

      @Override
      public boolean nextBoolean() {
        return false;
      }

      @Override
      public float nextFloat() {
        return 0;
      }

      @Override
      public double nextDouble() {
        return 1.0;
      }

      @Override
      public SplittableUniformRandomProvider split() {
        return this;
      }
    };

    final int size = 2;
    final double lower = 33.5;
    final double upper = 46.78;
    final double[] values = RngStreams.doubles(rng, size, lower, upper).toArray();

    for (int i = 0; i < values.length; i++) {
      Assertions.assertEquals(Math.nextDown(upper), values[i]);
    }
  }

  @Test
  void testSplittableIntsThrowsWithBadSize() {
    final SplittableIntSupplier generator =
        Splittables.ofInt(UniformRandomProviders.createSplittable(123),
            rng -> DiscreteUniformSampler.of(rng, 0, 2048));
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      RngStreams.ints(generator, -1);
    }, "Should throw with negative size");
  }

  @Test
  void testSplittableInts() {
    final SplittableIntSupplier generator =
        Splittables.ofInt(UniformRandomProviders.createSplittable(123),
            rng -> DiscreteUniformSampler.of(rng, 0, 2048));
    final int size = 7;
    final int[] values = RngStreams.ints(generator).limit(size).toArray();
    Assertions.assertEquals(size, values.length, "Incorrect stream length");
  }

  @Test
  void testSplittableIntsWithSize() {
    final SplittableIntSupplier generator =
        Splittables.ofInt(UniformRandomProviders.createSplittable(123),
            rng -> DiscreteUniformSampler.of(rng, 0, 2048));
    final int size = 7;
    final int[] values = RngStreams.ints(generator, size).toArray();
    Assertions.assertEquals(size, values.length, "Incorrect stream length");
  }

  @Test
  void testIntSpliteratorCanSplit() {
    final SplittableIntSupplier generator =
        Splittables.ofInt(UniformRandomProviders.createSplittable(123),
            rng -> DiscreteUniformSampler.of(rng, 0, 2048));

    final SupplierIntSpliterator spliterator1 = new SupplierIntSpliterator(generator, 0, 2);
    Assertions.assertEquals(2, spliterator1.getExactSizeIfKnown(), "Incorrect pre-split size");

    // First split should work
    final SupplierIntSpliterator spliterator2 = spliterator1.trySplit();
    Assertions.assertNotNull(spliterator2, "Should be able to split");

    // Split in half
    Assertions.assertEquals(1, spliterator1.getExactSizeIfKnown(), "Incorrect post-split size");
    Assertions.assertEquals(1, spliterator2.getExactSizeIfKnown(), "Incorrect post-split size");

    // Second split should not work
    final SupplierIntSpliterator spliterator1b = spliterator1.trySplit();
    Assertions.assertNull(spliterator1b, "Should not be able to split");
    final SupplierIntSpliterator spliterator2b = spliterator2.trySplit();
    Assertions.assertNull(spliterator2b, "Should not be able to split");
  }

  @Test
  void testIntSpliteratorTryAdvance() {
    final SplittableIntSupplier generator =
        Splittables.ofInt(UniformRandomProviders.createSplittable(123),
            rng -> DiscreteUniformSampler.of(rng, 0, 2048));

    final int size = 2;
    final SupplierIntSpliterator spliterator = new SupplierIntSpliterator(generator, 0, size);

    Assertions.assertThrows(NullPointerException.class, () -> {
      spliterator.tryAdvance((IntConsumer) null);
    }, "null IntConsumer");

    Assertions.assertEquals(size, spliterator.getExactSizeIfKnown(), "Incorrect pre-advance size");

    // Should be able to advance to end
    final IntConsumer action = i -> {
      // Not used
    };
    for (int i = 0; i < size; i++) {
      Assertions.assertTrue(spliterator.tryAdvance(action),
          "Incorrect advance result when not at the end");
      Assertions.assertEquals(size - i - 1, spliterator.getExactSizeIfKnown(),
          "Incorrect size estimate after advance");
    }
    Assertions.assertFalse(spliterator.tryAdvance(action), "Should not advance at the end");
  }

  @Test
  void testIntSpliteratorForEachRemaining() {
    final SplittableIntSupplier generator =
        Splittables.ofInt(UniformRandomProviders.createSplittable(123),
            rng -> DiscreteUniformSampler.of(rng, 0, 2048));

    final int size = 2;
    final SupplierIntSpliterator spliterator = new SupplierIntSpliterator(generator, 0, size);

    Assertions.assertThrows(NullPointerException.class, () -> {
      spliterator.forEachRemaining((IntConsumer) null);
    }, "null IntConsumer");

    // Should be able to advance to end
    final AtomicInteger count = new AtomicInteger();
    final IntConsumer action = i -> count.getAndIncrement();
    spliterator.forEachRemaining(action);

    Assertions.assertEquals(size, count.get(), "Incorrect invocations of consumer");
    Assertions.assertEquals(0, spliterator.getExactSizeIfKnown(),
        "Incorrect size after remaining have been consumed");

    // Try again
    spliterator.forEachRemaining(action);
    Assertions.assertEquals(size, count.get(),
        "Should not invoke consumer after remaining have been consumed");
  }

  @Test
  void testSplittableLongsThrowsWithBadSize() {
    final SplittableLongSupplier generator = Splittables.ofLong(
        UniformRandomProviders.createSplittable(123), rng -> UniformLongSampler.of(rng, 0, 2048));
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      RngStreams.longs(generator, -1);
    }, "Should throw with negative size");
  }

  @Test
  void testSplittableLongs() {
    final SplittableLongSupplier generator = Splittables.ofLong(
        UniformRandomProviders.createSplittable(123), rng -> UniformLongSampler.of(rng, 0, 2048));
    final long size = 7;
    final long[] values = RngStreams.longs(generator).limit(size).toArray();
    Assertions.assertEquals(size, values.length, "Incorrect stream length");
  }

  @Test
  void testSplittableLongsWithSize() {
    final SplittableLongSupplier generator = Splittables.ofLong(
        UniformRandomProviders.createSplittable(123), rng -> UniformLongSampler.of(rng, 0, 2048));
    final long size = 7;
    final long[] values = RngStreams.longs(generator, size).toArray();
    Assertions.assertEquals(size, values.length, "Incorrect stream length");
  }

  @Test
  void testLongSpliteratorCanSplit() {
    final SplittableLongSupplier generator = Splittables.ofLong(
        UniformRandomProviders.createSplittable(123), rng -> UniformLongSampler.of(rng, 0, 2048));

    final SupplierLongSpliterator spliterator1 = new SupplierLongSpliterator(generator, 0, 2);
    Assertions.assertEquals(2, spliterator1.getExactSizeIfKnown(), "Incorrect pre-split size");

    // First split should work
    final SupplierLongSpliterator spliterator2 = spliterator1.trySplit();
    Assertions.assertNotNull(spliterator2, "Should be able to split");

    // Split in half
    Assertions.assertEquals(1, spliterator1.getExactSizeIfKnown(), "Incorrect post-split size");
    Assertions.assertEquals(1, spliterator2.getExactSizeIfKnown(), "Incorrect post-split size");

    // Second split should not work
    final SupplierLongSpliterator spliterator1b = spliterator1.trySplit();
    Assertions.assertNull(spliterator1b, "Should not be able to split");
    final SupplierLongSpliterator spliterator2b = spliterator2.trySplit();
    Assertions.assertNull(spliterator2b, "Should not be able to split");
  }

  @Test
  void testLongSpliteratorTryAdvance() {
    final SplittableLongSupplier generator = Splittables.ofLong(
        UniformRandomProviders.createSplittable(123), rng -> UniformLongSampler.of(rng, 0, 2048));

    final long size = 2;
    final SupplierLongSpliterator spliterator = new SupplierLongSpliterator(generator, 0, size);

    Assertions.assertThrows(NullPointerException.class, () -> {
      spliterator.tryAdvance((LongConsumer) null);
    }, "null LongConsumer");

    Assertions.assertEquals(size, spliterator.getExactSizeIfKnown(), "Incorrect pre-advance size");

    // Should be able to advance to end
    final LongConsumer action = i -> {
      // Not used
    };
    for (long i = 0; i < size; i++) {
      Assertions.assertTrue(spliterator.tryAdvance(action),
          "Incorrect advance result when not at the end");
      Assertions.assertEquals(size - i - 1, spliterator.getExactSizeIfKnown(),
          "Incorrect size estimate after advance");
    }
    Assertions.assertFalse(spliterator.tryAdvance(action), "Should not advance at the end");
  }

  @Test
  void testLongSpliteratorForEachRemaining() {
    final SplittableLongSupplier generator = Splittables.ofLong(
        UniformRandomProviders.createSplittable(123), rng -> UniformLongSampler.of(rng, 0, 2048));

    final long size = 2;
    final SupplierLongSpliterator spliterator = new SupplierLongSpliterator(generator, 0, size);

    Assertions.assertThrows(NullPointerException.class, () -> {
      spliterator.forEachRemaining((LongConsumer) null);
    }, "null LongConsumer");

    // Should be able to advance to end
    final AtomicLong count = new AtomicLong();
    final LongConsumer action = i -> count.getAndIncrement();
    spliterator.forEachRemaining(action);

    Assertions.assertEquals(size, count.get(), "Incorrect invocations of consumer");
    Assertions.assertEquals(0, spliterator.getExactSizeIfKnown(),
        "Incorrect size after remaining have been consumed");

    // Try again
    spliterator.forEachRemaining(action);
    Assertions.assertEquals(size, count.get(),
        "Should not invoke consumer after remaining have been consumed");
  }

  @Test
  void testSplittableDoublesThrowsWithBadSize() {
    final SplittableDoubleSupplier generator =
        Splittables.ofDouble(UniformRandomProviders.createSplittable(123),
            rng -> ContinuousUniformSampler.of(rng, 0, 2048));
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      RngStreams.doubles(generator, -1);
    }, "Should throw with negative size");
  }

  @Test
  void testSplittableDoubles() {
    final SplittableDoubleSupplier generator =
        Splittables.ofDouble(UniformRandomProviders.createSplittable(123),
            rng -> ContinuousUniformSampler.of(rng, 0, 2048));
    final int size = 7;
    final double[] values = RngStreams.doubles(generator).limit(size).toArray();
    Assertions.assertEquals(size, values.length, "Incorrect stream length");
  }

  @Test
  void testSplittableDoublesWithSize() {
    final SplittableDoubleSupplier generator =
        Splittables.ofDouble(UniformRandomProviders.createSplittable(123),
            rng -> ContinuousUniformSampler.of(rng, 0, 2048));
    final int size = 7;
    final double[] values = RngStreams.doubles(generator, size).toArray();
    Assertions.assertEquals(size, values.length, "Incorrect stream length");
  }

  @Test
  void testDoubleSpliteratorCanSplit() {
    final SplittableDoubleSupplier generator =
        Splittables.ofDouble(UniformRandomProviders.createSplittable(123),
            rng -> ContinuousUniformSampler.of(rng, 0, 2048));

    final SupplierDoubleSpliterator spliterator1 = new SupplierDoubleSpliterator(generator, 0, 2);
    Assertions.assertEquals(2, spliterator1.getExactSizeIfKnown(), "Incorrect pre-split size");

    // First split should work
    final SupplierDoubleSpliterator spliterator2 = spliterator1.trySplit();
    Assertions.assertNotNull(spliterator2, "Should be able to split");

    // Split in half
    Assertions.assertEquals(1, spliterator1.getExactSizeIfKnown(), "Incorrect post-split size");
    Assertions.assertEquals(1, spliterator2.getExactSizeIfKnown(), "Incorrect post-split size");

    // Second split should not work
    final SupplierDoubleSpliterator spliterator1b = spliterator1.trySplit();
    Assertions.assertNull(spliterator1b, "Should not be able to split");
    final SupplierDoubleSpliterator spliterator2b = spliterator2.trySplit();
    Assertions.assertNull(spliterator2b, "Should not be able to split");
  }

  @Test
  void testDoubleSpliteratorTryAdvance() {
    final SplittableDoubleSupplier generator =
        Splittables.ofDouble(UniformRandomProviders.createSplittable(123),
            rng -> ContinuousUniformSampler.of(rng, 0, 2048));

    final int size = 2;
    final SupplierDoubleSpliterator spliterator = new SupplierDoubleSpliterator(generator, 0, size);

    Assertions.assertThrows(NullPointerException.class, () -> {
      spliterator.tryAdvance((DoubleConsumer) null);
    }, "null DoubleConsumer");

    Assertions.assertEquals(size, spliterator.getExactSizeIfKnown(), "Incorrect pre-advance size");

    // Should be able to advance to end
    final DoubleConsumer action = i -> {
      // Not used
    };
    for (int i = 0; i < size; i++) {
      Assertions.assertTrue(spliterator.tryAdvance(action),
          "Incorrect advance result when not at the end");
      Assertions.assertEquals(size - i - 1, spliterator.getExactSizeIfKnown(),
          "Incorrect size estimate after advance");
    }
    Assertions.assertFalse(spliterator.tryAdvance(action), "Should not advance at the end");
  }

  @Test
  void testDoubleSpliteratorForEachRemaining() {
    final SplittableDoubleSupplier generator =
        Splittables.ofDouble(UniformRandomProviders.createSplittable(123),
            rng -> ContinuousUniformSampler.of(rng, 0, 2048));

    final int size = 2;
    final SupplierDoubleSpliterator spliterator = new SupplierDoubleSpliterator(generator, 0, size);

    Assertions.assertThrows(NullPointerException.class, () -> {
      spliterator.forEachRemaining((DoubleConsumer) null);
    }, "null DoubleConsumer");

    // Should be able to advance to end
    final AtomicInteger count = new AtomicInteger();
    final DoubleConsumer action = i -> count.getAndIncrement();
    spliterator.forEachRemaining(action);

    Assertions.assertEquals(size, count.get(), "Incorrect invocations of consumer");
    Assertions.assertEquals(0, spliterator.getExactSizeIfKnown(),
        "Incorrect size after remaining have been consumed");

    // Try again
    spliterator.forEachRemaining(action);
    Assertions.assertEquals(size, count.get(),
        "Should not invoke consumer after remaining have been consumed");
  }

  @Test
  void testRngSplittableIntSupplierCanSplit() {
    final FixedSplit rng = new FixedSplit(0);
    final SplittableIntSupplier s1 = new RngSplittableIntSupplier(rng);
    SplittableIntSupplier s2 = s1.split();

    for (int i = 0; i < 3; i++) {
      Assertions.assertEquals(0, s1.getAsInt());
      Assertions.assertEquals(i + 1, s2.getAsInt());
      s2 = s2.split();
    }
  }

  @Test
  void testRngSplittableLongSupplierCanSplit() {
    final FixedSplit rng = new FixedSplit(0);
    final SplittableLongSupplier s1 = new RngSplittableLongSupplier(rng);
    SplittableLongSupplier s2 = s1.split();

    for (int i = 0; i < 3; i++) {
      Assertions.assertEquals(0, s1.getAsLong());
      Assertions.assertEquals(i + 1, s2.getAsLong());
      s2 = s2.split();
    }
  }

  @Test
  void testRngSplittableDoubleSupplierCanSplit() {
    final FixedSplit rng = new FixedSplit(0);
    final SplittableDoubleSupplier s1 = new RngSplittableDoubleSupplier(rng);
    SplittableDoubleSupplier s2 = s1.split();

    for (int i = 0; i < 3; i++) {
      Assertions.assertEquals(0 * 0x1.0p-53, s1.getAsDouble());
      Assertions.assertEquals((i + 1) * 0x1.0p-53, s2.getAsDouble());
      s2 = s2.split();
    }
  }

  @Test
  void testRangeRngSplittableDoubleSupplierCanSplit() {
    final FixedSplit rng = new FixedSplit(0);
    final double lo = 13;
    final double hi = 25;
    final double range = hi - lo;
    final SplittableDoubleSupplier s1 = new RangeSplittableDoubleSupplier(rng, lo, hi);
    SplittableDoubleSupplier s2 = s1.split();

    for (int i = 0; i < 3; i++) {
      Assertions.assertEquals(0 * 0x1.0p-53 * range + lo, s1.getAsDouble());
      Assertions.assertEquals((i + 1) * 0x1.0p-53 * range + lo, s2.getAsDouble());
      s2 = s2.split();
    }
  }

  /**
   * Simple class providing a fixed valued output.
   */
  private static class FixedSplit implements SplittableUniformRandomProvider {
    /** The value. */
    private final int value;

    /**
     * Create an instance.
     *
     * @param value the value
     */
    FixedSplit(int value) {
      this.value = value;
    }

    @Override
    public void nextBytes(byte[] bytes) {}

    @Override
    public void nextBytes(byte[] bytes, int start, int len) {}

    @Override
    public int nextInt() {
      return value;
    }

    @Override
    public int nextInt(int n) {
      return 0;
    }

    @Override
    public long nextLong() {
      return value;
    }

    @Override
    public long nextLong(long n) {
      return 0;
    }

    @Override
    public boolean nextBoolean() {
      return false;
    }

    @Override
    public float nextFloat() {
      return 0;
    }

    @Override
    public double nextDouble() {
      return value * 0x1.0p-53;
    }

    @Override
    public SplittableUniformRandomProvider split() {
      return new FixedSplit(value + 1);
    }
  }
}
