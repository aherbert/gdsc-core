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

package uk.ac.sussex.gdsc.core.utils.rng;

import java.util.Spliterator;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.StreamSupport;

/**
 * Support for using random number generators in streams.
 */
public final class RngStreams {
  /** Error message when the range is incorrect. */
  private static final String BOUND_MUST_BE_ABOVE_ORIGIN = "bound must be greater than origin";
  /** Error message when the stream size is negative. */
  private static final String SIZE_MUST_BE_POSITIVE = "size must be positive";

  /** Spliterator characteristics. */
  private static final int CHARACTERISTICS =
      Spliterator.SIZED | Spliterator.SUBSIZED | Spliterator.NONNULL | Spliterator.IMMUTABLE;

  // Spliterators have been adapted from the JDK 8 API which contains examples
  // for splitting different types of RNG to make a second thread-safe instance:
  //
  // - SplittableRandom::split
  // - ThreadLocalRandom::current
  // - Random (same instance as it is synchronized)
  //
  // This class requires that the input generator is a SplittableUniformRandomProvider.
  // Thread-safe specialisations of RNGs are expected to be slower without the custom
  // support within the Thread class as used by ThreadLocalRandom.
  //
  // Here the range methods for integers delegate to the UniformRandomProvider implementation
  // if possible and provide a separate large range algorithm.

  // @formatter:off
  /**
   * Spliterator for int streams. Supports 4 versions:
   *
   * <ul>
   * <li>Fixed stream length; unbounded sample output
   * <li>Fixed stream length; bounded sample output
   * <li>Infinite stream length; unbounded sample output
   * <li>Infinite stream length; bounded sample output
   * </ul>
   *
   * <p>An infinite stream is created using a end of Long.MAX_VALUE. Splitting is done
   * by dividing in half until this is not possible.
   *
   * <p>The long and double versions of this class are identical except for types.
   */
  // @formatter:on
  static final class RngIntSpliterator implements Spliterator.OfInt {
    /** The random generator. */
    final SplittableUniformRandomProvider rng;
    /** The current position in the range. */
    long position;
    /** The upper limit of the range. */
    final long end;
    /** The lower upper (inclusive) of the random number range to produce. */
    final int lower;
    /** The upper upper (exclusive) of the random number range to produce. */
    final int upper;

    /**
     * Create a new instance.
     *
     * @param rng the random generator
     * @param start the start position of the stream (inclusive)
     * @param end the upper limit of the stream (exclusive)
     * @param lower the lower bound (inclusive) of the random number range to produce.
     * @param upper the upper bound (exclusive) of the random number range to produce.
     */
    RngIntSpliterator(SplittableUniformRandomProvider rng, long start, long end, int lower,
        int upper) {
      this.rng = rng;
      this.position = start;
      this.end = end;
      this.lower = lower;
      this.upper = upper;
    }

    @Override
    public RngIntSpliterator trySplit() {
      final long start = position;
      final long middle = (start + end) >>> 1;
      if (middle <= start) {
        return null;
      }
      position = middle;
      return new RngIntSpliterator(rng.split(), start, middle, lower, upper);
    }

    @Override
    public long estimateSize() {
      return end - position;
    }

    @Override
    public int characteristics() {
      return CHARACTERISTICS;
    }

    @Override
    public boolean tryAdvance(IntConsumer consumer) {
      if (consumer == null) {
        throw new NullPointerException();
      }
      final long pos = position;
      if (pos < end) {
        consumer.accept(nextInt(rng, lower, upper));
        position = pos + 1;
        return true;
      }
      return false;
    }

    @Override
    public void forEachRemaining(IntConsumer consumer) {
      if (consumer == null) {
        throw new NullPointerException();
      }
      long pos = position;
      final long last = end;
      if (pos < last) {
        position = last;
        final SplittableUniformRandomProvider g = rng;
        final int o = lower;
        final int b = upper;
        if (o < b) {
          do {
            consumer.accept(nextInt(g, o, b));
          } while (++pos < last);
        } else {
          do {
            consumer.accept(g.nextInt());
          } while (++pos < last);
        }
      }
    }

    /**
     * Compute the next value in the specified range.
     *
     * @param rng the random generator
     * @param lower the lower bound (inclusive) of the random number range to produce.
     * @param upper the upper bound (exclusive) of the random number range to produce.
     * @return the value
     */
    private static int nextInt(SplittableUniformRandomProvider rng, int lower, int upper) {
      if (lower < upper) {
        final int n = upper - lower;
        if (n > 0) {
          // Small range
          return rng.nextInt(n) + lower;
        }
        // Large range
        int result;
        do {
          result = rng.nextInt();
        } while (result < lower || result >= upper);
        return result;
      }
      return rng.nextInt();
    }
  }

  /**
   * Spliterator for long streams. Supports 4 versions as per {@link RngIntSpliterator}.
   */
  static final class RngLongSpliterator implements Spliterator.OfLong {
    /** The random generator. */
    final SplittableUniformRandomProvider rng;
    /** The current position in the stream range. */
    long position;
    /** The upper limit of the stream range. */
    final long end;
    /** The lower upper (inclusive) of the random number range to produce. */
    final long lower;
    /** The upper upper (exclusive) of the random number range to produce. */
    final long upper;

    /**
     * Create a new instance.
     *
     * @param rng the random generator
     * @param start the start position of the stream
     * @param end the upper limit of the stream
     * @param lower the lower upper (inclusive) of the random number range to produce.
     * @param upper the upper upper (exclusive) of the random number range to produce.
     */
    RngLongSpliterator(SplittableUniformRandomProvider rng, long start, long end, long lower,
        long upper) {
      this.rng = rng;
      this.position = start;
      this.end = end;
      this.lower = lower;
      this.upper = upper;
    }

    @Override
    public RngLongSpliterator trySplit() {
      final long start = position;
      final long middle = (start + end) >>> 1;
      if (middle <= start) {
        return null;
      }
      position = middle;
      return new RngLongSpliterator(rng.split(), start, middle, lower, upper);
    }

    @Override
    public long estimateSize() {
      return end - position;
    }

    @Override
    public int characteristics() {
      return CHARACTERISTICS;
    }

    @Override
    public boolean tryAdvance(LongConsumer consumer) {
      if (consumer == null) {
        throw new NullPointerException();
      }
      final long pos = position;
      if (pos < end) {
        consumer.accept(nextLong(rng, lower, upper));
        position = pos + 1;
        return true;
      }
      return false;
    }

    @Override
    public void forEachRemaining(LongConsumer consumer) {
      if (consumer == null) {
        throw new NullPointerException();
      }
      long pos = position;
      final long last = end;
      if (pos < last) {
        position = last;
        final SplittableUniformRandomProvider g = rng;
        final long o = lower;
        final long b = upper;
        if (o < b) {
          do {
            consumer.accept(nextLong(g, o, b));
          } while (++pos < last);
        } else {
          do {
            consumer.accept(g.nextLong());
          } while (++pos < last);
        }
      }
    }

    /**
     * Compute the next value in the specified range.
     *
     * @param rng the random generator
     * @param lower the lower bound (inclusive) of the random number range to produce.
     * @param upper the upper bound (exclusive) of the random number range to produce.
     * @return the value
     */
    private static long nextLong(SplittableUniformRandomProvider rng, long lower, long upper) {
      if (lower < upper) {
        final long n = upper - lower;
        if (n > 0L) {
          // Small range
          return rng.nextLong(n) + lower;
        }
        // Large range
        long result;
        do {
          result = rng.nextLong();
        } while (result < lower || result >= upper);
        return result;
      }
      return rng.nextLong();
    }
  }

  /**
   * Spliterator for long streams. Supports 4 versions as per {@link RngIntSpliterator}.
   */
  static final class RngDoubleSpliterator implements Spliterator.OfDouble {
    /** The random generator. */
    final SplittableUniformRandomProvider rng;
    /** The current position in the stream range. */
    long position;
    /** The upper limit of the stream range. */
    final long end;
    /** The lower upper (inclusive) of the random number range to produce. */
    final double lower;
    /** The upper upper (exclusive) of the random number range to produce. */
    final double upper;

    /**
     * Create a new instance.
     *
     * @param rng the random generator
     * @param start the start position of the stream
     * @param end the upper limit of the stream
     * @param lower the lower bound (inclusive) of the random number range to produce.
     * @param upper the upper bound (exclusive) of the random number range to produce.
     */
    RngDoubleSpliterator(SplittableUniformRandomProvider rng, long start, long end, double lower,
        double upper) {
      this.rng = rng;
      this.position = start;
      this.end = end;
      this.lower = lower;
      this.upper = upper;
    }

    @Override
    public RngDoubleSpliterator trySplit() {
      final long start = position;
      final long middle = (start + end) >>> 1;
      if (middle <= start) {
        return null;
      }
      position = middle;
      return new RngDoubleSpliterator(rng.split(), start, middle, lower, upper);
    }

    @Override
    public long estimateSize() {
      return end - position;
    }

    @Override
    public int characteristics() {
      return CHARACTERISTICS;
    }

    @Override
    public boolean tryAdvance(DoubleConsumer consumer) {
      if (consumer == null) {
        throw new NullPointerException();
      }
      final long pos = position;
      if (pos < end) {
        consumer.accept(nextDouble(rng, lower, upper));
        position = pos + 1;
        return true;
      }
      return false;
    }

    @Override
    public void forEachRemaining(DoubleConsumer consumer) {
      if (consumer == null) {
        throw new NullPointerException();
      }
      long pos = position;
      final long last = end;
      if (pos < last) {
        position = last;
        final SplittableUniformRandomProvider g = rng;
        final double o = lower;
        final double b = upper;
        if (o < b) {
          do {
            consumer.accept(nextDouble(g, o, b));
          } while (++pos < last);
        } else {
          do {
            consumer.accept(g.nextDouble());
          } while (++pos < last);
        }
      }
    }

    /**
     * Compute the next value in the specified range.
     *
     * @param rng the random generator
     * @param lower the lower bound (inclusive) of the random number range to produce.
     * @param upper the upper bound (exclusive) of the random number range to produce.
     * @return the value
     */
    private static double nextDouble(SplittableUniformRandomProvider rng, double lower,
        double upper) {
      double result = rng.nextDouble();
      if (lower < upper) {
        // 2 multiplies; 2 additions
        // result = lower * result + upper * (1 - result)

        // JDK method: 1 multiply; 2 additions; bounds check
        result = result * (upper - lower) + lower;
        if (result >= upper) {
          // Correct for rounding.
          // Equivalent to Math.nextDown(upper) without the edge-case checks.
          result = Double.longBitsToDouble(Double.doubleToLongBits(upper) - 1);
        }
      }
      return result;
    }
  }

  // @formatter:off
  /**
   * Spliterator for int streams. Supports 2 versions:
   *
   * <ul>
   * <li>Fixed stream length; unbounded sample output
   * <li>Infinite stream length; unbounded sample output
   * </ul>
   *
   * <p>An infinite stream is created using a end of Long.MAX_VALUE. Splitting is done
   * by dividing in half until this is not possible.
   *
   * <p>The double version of this class is identical except for types.
   */
  // @formatter:on
  static final class SupplierIntSpliterator implements Spliterator.OfInt {
    /** The generator. */
    final SplittableIntSupplier generator;
    /** The current position in the range. */
    long position;
    /** The upper limit of the range. */
    final long end;

    /**
     * Create a new instance.
     *
     * @param generator the generator
     * @param start the start position of the stream (inclusive)
     * @param end the upper limit of the stream (exclusive)
     */
    SupplierIntSpliterator(SplittableIntSupplier generator, long start, long end) {
      this.generator = generator;
      this.position = start;
      this.end = end;
    }

    @Override
    public SupplierIntSpliterator trySplit() {
      final long start = position;
      final long middle = (start + end) >>> 1;
      if (middle <= start) {
        return null;
      }
      position = middle;
      return new SupplierIntSpliterator(generator.split(), start, middle);
    }

    @Override
    public long estimateSize() {
      return end - position;
    }

    @Override
    public int characteristics() {
      return CHARACTERISTICS;
    }

    @Override
    public boolean tryAdvance(IntConsumer consumer) {
      if (consumer == null) {
        throw new NullPointerException();
      }
      final long pos = position;
      if (pos < end) {
        consumer.accept(generator.getAsInt());
        position = pos + 1;
        return true;
      }
      return false;
    }

    @Override
    public void forEachRemaining(IntConsumer consumer) {
      if (consumer == null) {
        throw new NullPointerException();
      }
      long pos = position;
      final long last = end;
      if (pos < last) {
        position = last;
        final SplittableIntSupplier g = generator;
        do {
          consumer.accept(g.getAsInt());
        } while (++pos < last);
      }
    }
  }

  /**
   * Spliterator for long streams. Supports 2 versions as per {@link SupplierIntSpliterator}.
   */
  static final class SupplierLongSpliterator implements Spliterator.OfLong {
    /** The generator. */
    final SplittableLongSupplier generator;
    /** The current position in the range. */
    long position;
    /** The upper limit of the range. */
    final long end;

    /**
     * Create a new instance.
     *
     * @param generator the generator
     * @param start the start position of the stream (inclusive)
     * @param end the upper limit of the stream (exclusive)
     */
    SupplierLongSpliterator(SplittableLongSupplier generator, long start, long end) {
      this.generator = generator;
      this.position = start;
      this.end = end;
    }

    @Override
    public SupplierLongSpliterator trySplit() {
      final long start = position;
      final long middle = (start + end) >>> 1;
      if (middle <= start) {
        return null;
      }
      position = middle;
      return new SupplierLongSpliterator(generator.split(), start, middle);
    }

    @Override
    public long estimateSize() {
      return end - position;
    }

    @Override
    public int characteristics() {
      return CHARACTERISTICS;
    }

    @Override
    public boolean tryAdvance(LongConsumer consumer) {
      if (consumer == null) {
        throw new NullPointerException();
      }
      final long pos = position;
      if (pos < end) {
        consumer.accept(generator.getAsLong());
        position = pos + 1;
        return true;
      }
      return false;
    }

    @Override
    public void forEachRemaining(LongConsumer consumer) {
      if (consumer == null) {
        throw new NullPointerException();
      }
      long pos = position;
      final long last = end;
      if (pos < last) {
        position = last;
        final SplittableLongSupplier g = generator;
        do {
          consumer.accept(g.getAsLong());
        } while (++pos < last);
      }
    }
  }

  /**
   * Spliterator for double streams. Supports 2 versions as per {@link SupplierIntSpliterator}.
   */
  static final class SupplierDoubleSpliterator implements Spliterator.OfDouble {
    /** The generator. */
    final SplittableDoubleSupplier generator;
    /** The current position in the range. */
    long position;
    /** The upper limit of the range. */
    final long end;

    /**
     * Create a new instance.
     *
     * @param generator the generator
     * @param start the start position of the stream (inclusive)
     * @param end the upper limit of the stream (exclusive)
     */
    SupplierDoubleSpliterator(SplittableDoubleSupplier generator, long start, long end) {
      this.generator = generator;
      this.position = start;
      this.end = end;
    }

    @Override
    public SupplierDoubleSpliterator trySplit() {
      final long start = position;
      final long middle = (start + end) >>> 1;
      if (middle <= start) {
        return null;
      }
      position = middle;
      return new SupplierDoubleSpliterator(generator.split(), start, middle);
    }

    @Override
    public long estimateSize() {
      return end - position;
    }

    @Override
    public int characteristics() {
      return CHARACTERISTICS;
    }

    @Override
    public boolean tryAdvance(DoubleConsumer consumer) {
      if (consumer == null) {
        throw new NullPointerException();
      }
      final long pos = position;
      if (pos < end) {
        consumer.accept(generator.getAsDouble());
        position = pos + 1;
        return true;
      }
      return false;
    }

    @Override
    public void forEachRemaining(DoubleConsumer consumer) {
      if (consumer == null) {
        throw new NullPointerException();
      }
      long pos = position;
      final long last = end;
      if (pos < last) {
        position = last;
        final SplittableDoubleSupplier g = generator;
        do {
          consumer.accept(g.getAsDouble());
        } while (++pos < last);
      }
    }
  }

  /** No construction. */
  private RngStreams() {}

  /**
   * Returns a stream producing the given {@code streamSize} number of pseudorandom {@code int}
   * values from the generator and/or one split from it.
   *
   * @param rng the random generator
   * @param streamSize the number of values to generate
   * @return a stream of pseudorandom {@code int} values
   * @throws IllegalArgumentException if {@code streamSize} is less than zero
   */
  public static IntStream ints(SplittableUniformRandomProvider rng, long streamSize) {
    if (streamSize < 0L) {
      throw new IllegalArgumentException(SIZE_MUST_BE_POSITIVE);
    }
    return StreamSupport.intStream(new RngIntSpliterator(rng, 0L, streamSize, Integer.MAX_VALUE, 0),
        false);
  }

  /**
   * Returns an effectively unlimited stream of pseudorandom {@code int} values from the generator
   * and/or one split from it.
   *
   * <h2>Note</h2>
   *
   * <p>This method is implemented to be equivalent to
   * {@link #ints(SplittableUniformRandomProvider, long) ints(rng, Long.MAX_VALUE)}.
   *
   * @param rng the random generator
   * @return a stream of pseudorandom {@code int} values
   */
  public static IntStream ints(SplittableUniformRandomProvider rng) {
    return StreamSupport
        .intStream(new RngIntSpliterator(rng, 0L, Long.MAX_VALUE, Integer.MAX_VALUE, 0), false);
  }

  /**
   * Returns a stream producing the given {@code streamSize} number of pseudorandom {@code int}
   * values from the generator and/or one split from it; each value conforms to the given origin
   * (inclusive) and bound (exclusive).
   *
   * @param rng the random generator
   * @param streamSize the number of values to generate
   * @param randomNumberOrigin the origin (inclusive) of each random value
   * @param randomNumberBound the bound (exclusive) of each random value
   * @return a stream of pseudorandom {@code int} values, each with the given origin (inclusive) and
   *         bound (exclusive)
   * @throws IllegalArgumentException if {@code streamSize} is less than zero, or
   *         {@code randomNumberOrigin} is greater than or equal to {@code randomNumberBound}
   */
  public static IntStream ints(SplittableUniformRandomProvider rng, long streamSize,
      int randomNumberOrigin, int randomNumberBound) {
    if (streamSize < 0L) {
      throw new IllegalArgumentException(SIZE_MUST_BE_POSITIVE);
    }
    if (randomNumberOrigin >= randomNumberBound) {
      throw new IllegalArgumentException(BOUND_MUST_BE_ABOVE_ORIGIN);
    }
    return StreamSupport.intStream(
        new RngIntSpliterator(rng, 0L, streamSize, randomNumberOrigin, randomNumberBound), false);
  }

  /**
   * Returns an effectively unlimited stream of pseudorandom {@code
   * int} values from the generator and/or one split from it; each value conforms to the given
   * origin (inclusive) and bound (exclusive).
   *
   * <h2>Note</h2>
   *
   * <p>This method is implemented to be equivalent to
   * {@link #ints(SplittableUniformRandomProvider, long, int, int) ints(rng, Long.MAX_VALUE,
   * randomNumberOrigin, randomNumberBound)}.
   *
   * @param rng the random generator
   * @param randomNumberOrigin the origin (inclusive) of each random value
   * @param randomNumberBound the bound (exclusive) of each random value
   * @return a stream of pseudorandom {@code int} values, each with the given origin (inclusive) and
   *         bound (exclusive)
   * @throws IllegalArgumentException if {@code randomNumberOrigin} is greater than or equal to
   *         {@code randomNumberBound}
   */
  public static IntStream ints(SplittableUniformRandomProvider rng, int randomNumberOrigin,
      int randomNumberBound) {
    if (randomNumberOrigin >= randomNumberBound) {
      throw new IllegalArgumentException(BOUND_MUST_BE_ABOVE_ORIGIN);
    }
    return StreamSupport.intStream(
        new RngIntSpliterator(rng, 0L, Long.MAX_VALUE, randomNumberOrigin, randomNumberBound),
        false);
  }

  /**
   * Returns a stream producing the given {@code streamSize} number of {@code int} values from the
   * generator and/or one split from it.
   *
   * @param generator the generator
   * @param streamSize the number of values to generate
   * @return a stream of {@code int} values
   * @throws IllegalArgumentException if {@code streamSize} is less than zero
   */
  public static IntStream ints(SplittableIntSupplier generator, long streamSize) {
    if (streamSize < 0L) {
      throw new IllegalArgumentException(SIZE_MUST_BE_POSITIVE);
    }
    return StreamSupport.intStream(new SupplierIntSpliterator(generator, 0L, streamSize), false);
  }

  /**
   * Returns an effectively unlimited stream of {@code int} values from the generator and/or one
   * split from it.
   *
   * <h2>Note</h2>
   *
   * <p>This method is implemented to be equivalent to {@link #ints(SplittableIntSupplier, long)
   * ints(generator, Long.MAX_VALUE)}.
   *
   * @param generator the generator
   * @return a stream of {@code int} values
   */
  public static IntStream ints(SplittableIntSupplier generator) {
    return StreamSupport.intStream(new SupplierIntSpliterator(generator, 0L, Long.MAX_VALUE),
        false);
  }

  /**
   * Returns a stream producing the given {@code streamSize} number of pseudorandom {@code long}
   * values from the generator and/or one split from it.
   *
   * @param rng the random generator
   * @param streamSize the number of values to generate
   * @return a stream of pseudorandom {@code long} values
   * @throws IllegalArgumentException if {@code streamSize} is less than zero
   */
  public static LongStream longs(SplittableUniformRandomProvider rng, long streamSize) {
    if (streamSize < 0L) {
      throw new IllegalArgumentException(SIZE_MUST_BE_POSITIVE);
    }
    return StreamSupport.longStream(new RngLongSpliterator(rng, 0L, streamSize, Long.MAX_VALUE, 0L),
        false);
  }

  /**
   * Returns an effectively unlimited stream of pseudorandom {@code
   * long} values from the generator and/or one split from it.
   *
   * <h2>Note</h2>
   *
   * <p>This method is implemented to be equivalent to
   * {@link #longs(SplittableUniformRandomProvider, long) longs(rng, Long.MAX_VALUE)}.
   *
   * @param rng the random generator
   * @return a stream of pseudorandom {@code long} values
   */
  public static LongStream longs(SplittableUniformRandomProvider rng) {
    return StreamSupport
        .longStream(new RngLongSpliterator(rng, 0L, Long.MAX_VALUE, Long.MAX_VALUE, 0L), false);
  }

  /**
   * Returns a stream producing the given {@code streamSize} number of pseudorandom {@code long}
   * values from the generator and/or one split from it; each value conforms to the given origin
   * (inclusive) and bound (exclusive).
   *
   * @param rng the random generator
   * @param streamSize the number of values to generate
   * @param randomNumberOrigin the origin (inclusive) of each random value
   * @param randomNumberBound the bound (exclusive) of each random value
   * @return a stream of pseudorandom {@code long} values, each with the given origin (inclusive)
   *         and bound (exclusive)
   * @throws IllegalArgumentException if {@code streamSize} is less than zero, or
   *         {@code randomNumberOrigin} is greater than or equal to {@code randomNumberBound}
   */
  public static LongStream longs(SplittableUniformRandomProvider rng, long streamSize,
      long randomNumberOrigin, long randomNumberBound) {
    if (streamSize < 0L) {
      throw new IllegalArgumentException(SIZE_MUST_BE_POSITIVE);
    }
    if (randomNumberOrigin >= randomNumberBound) {
      throw new IllegalArgumentException(BOUND_MUST_BE_ABOVE_ORIGIN);
    }
    return StreamSupport.longStream(
        new RngLongSpliterator(rng, 0L, streamSize, randomNumberOrigin, randomNumberBound), false);
  }

  /**
   * Returns an effectively unlimited stream of pseudorandom {@code
   * long} values from the generator and/or one split from it; each value conforms to the given
   * origin (inclusive) and bound (exclusive).
   *
   * <h2>Note</h2>
   *
   * <p>This method is implemented to be equivalent to
   * {@link #longs(SplittableUniformRandomProvider, long, long, long) longs(rng, Long.MAX_VALUE,
   * randomNumberOrigin, randomNumberBound)}.
   *
   * @param rng the random generator
   * @param randomNumberOrigin the origin (inclusive) of each random value
   * @param randomNumberBound the bound (exclusive) of each random value
   * @return a stream of pseudorandom {@code long} values, each with the given origin (inclusive)
   *         and bound (exclusive)
   * @throws IllegalArgumentException if {@code randomNumberOrigin} is greater than or equal to
   *         {@code randomNumberBound}
   */
  public static LongStream longs(SplittableUniformRandomProvider rng, long randomNumberOrigin,
      long randomNumberBound) {
    if (randomNumberOrigin >= randomNumberBound) {
      throw new IllegalArgumentException(BOUND_MUST_BE_ABOVE_ORIGIN);
    }
    return StreamSupport.longStream(
        new RngLongSpliterator(rng, 0L, Long.MAX_VALUE, randomNumberOrigin, randomNumberBound),
        false);
  }

  /**
   * Returns a stream producing the given {@code streamSize} number of {@code long} values from the
   * generator and/or one split from it.
   *
   * @param generator the generator
   * @param streamSize the number of values to generate
   * @return a stream of {@code long} values
   * @throws IllegalArgumentException if {@code streamSize} is less than zero
   */
  public static LongStream longs(SplittableLongSupplier generator, long streamSize) {
    if (streamSize < 0L) {
      throw new IllegalArgumentException(SIZE_MUST_BE_POSITIVE);
    }
    return StreamSupport.longStream(new SupplierLongSpliterator(generator, 0L, streamSize), false);
  }

  /**
   * Returns an effectively unlimited stream of {@code long} values from the generator and/or one
   * split from it.
   *
   * <h2>Note</h2>
   *
   * <p>This method is implemented to be equivalent to {@link #longs(SplittableLongSupplier, long)
   * longs(generator, Long.MAX_VALUE)}.
   *
   * @param generator the generator
   * @return a stream of {@code long} values
   */
  public static LongStream longs(SplittableLongSupplier generator) {
    return StreamSupport.longStream(new SupplierLongSpliterator(generator, 0L, Long.MAX_VALUE),
        false);
  }

  /**
   * Returns a stream producing the given {@code streamSize} number of pseudorandom {@code double}
   * values from the generator and/or one split from it; each value is between zero (inclusive) and
   * one (exclusive).
   *
   * @param rng the random generator
   * @param streamSize the number of values to generate
   * @return a stream of {@code double} values
   * @throws IllegalArgumentException if {@code streamSize} is less than zero
   */
  public static DoubleStream doubles(SplittableUniformRandomProvider rng, long streamSize) {
    if (streamSize < 0L) {
      throw new IllegalArgumentException(SIZE_MUST_BE_POSITIVE);
    }
    return StreamSupport
        .doubleStream(new RngDoubleSpliterator(rng, 0L, streamSize, Double.MAX_VALUE, 0.0), false);
  }

  /**
   * Returns an effectively unlimited stream of pseudorandom {@code
   * double} values from the generator and/or one split from it; each value is between zero
   * (inclusive) and one (exclusive).
   *
   * <h2>Note</h2>
   *
   * <p>This method is implemented to be equivalent to
   * {@link #doubles(SplittableUniformRandomProvider, long) doubles(rng, Long.MAX_VALUE)}.
   *
   * @param rng the random generator
   * @return a stream of pseudorandom {@code double} values
   */
  public static DoubleStream doubles(SplittableUniformRandomProvider rng) {
    return StreamSupport.doubleStream(
        new RngDoubleSpliterator(rng, 0L, Long.MAX_VALUE, Double.MAX_VALUE, 0.0), false);
  }

  /**
   * Returns a stream producing the given {@code streamSize} number of pseudorandom {@code double}
   * values from the generator and/or one split from it; each value conforms to the given origin
   * (inclusive) and bound (exclusive).
   *
   * @param rng the random generator
   * @param streamSize the number of values to generate
   * @param randomNumberOrigin the origin (inclusive) of each random value
   * @param randomNumberBound the bound (exclusive) of each random value
   * @return a stream of pseudorandom {@code double} values, each with the given origin (inclusive)
   *         and bound (exclusive)
   * @throws IllegalArgumentException if {@code randomNumberOrigin} is greater than or equal to
   *         {@code randomNumberBound}
   */
  public static DoubleStream doubles(SplittableUniformRandomProvider rng, long streamSize,
      double randomNumberOrigin, double randomNumberBound) {
    if (streamSize < 0L) {
      throw new IllegalArgumentException(SIZE_MUST_BE_POSITIVE);
    }
    // Negative of comparison check will handle NaN
    if (!(randomNumberOrigin < randomNumberBound)) {
      throw new IllegalArgumentException(BOUND_MUST_BE_ABOVE_ORIGIN);
    }
    return StreamSupport.doubleStream(
        new RngDoubleSpliterator(rng, 0L, streamSize, randomNumberOrigin, randomNumberBound),
        false);
  }

  /**
   * Returns an effectively unlimited stream of pseudorandom {@code
   * double} values from the generator and/or one split from it; each value conforms to the given
   * origin (inclusive) and bound (exclusive).
   *
   * <h2>Note</h2>
   *
   * <p>This method is implemented to be equivalent to
   * {@link #doubles(SplittableUniformRandomProvider, long, double, double) doubles(rng,
   * Long.MAX_VALUE, randomNumberOrigin, randomNumberBound)}.
   *
   * @param rng the random generator
   * @param randomNumberOrigin the origin (inclusive) of each random value
   * @param randomNumberBound the bound (exclusive) of each random value
   * @return a stream of pseudorandom {@code double} values, each with the given origin (inclusive)
   *         and bound (exclusive)
   * @throws IllegalArgumentException if {@code randomNumberOrigin} is greater than or equal to
   *         {@code randomNumberBound}
   */
  public static DoubleStream doubles(SplittableUniformRandomProvider rng, double randomNumberOrigin,
      double randomNumberBound) {
    // Negative of comparison check will handle NaN
    if (!(randomNumberOrigin < randomNumberBound)) {
      throw new IllegalArgumentException(BOUND_MUST_BE_ABOVE_ORIGIN);
    }
    return StreamSupport.doubleStream(
        new RngDoubleSpliterator(rng, 0L, Long.MAX_VALUE, randomNumberOrigin, randomNumberBound),
        false);
  }

  /**
   * Returns a stream producing the given {@code streamSize} number of {@code double} values from
   * the generator and/or one split from it.
   *
   * @param generator the generator
   * @param streamSize the number of values to generate
   * @return a stream of {@code double} values
   * @throws IllegalArgumentException if {@code streamSize} is less than zero
   */
  public static DoubleStream doubles(SplittableDoubleSupplier generator, long streamSize) {
    if (streamSize < 0L) {
      throw new IllegalArgumentException(SIZE_MUST_BE_POSITIVE);
    }
    return StreamSupport.doubleStream(new SupplierDoubleSpliterator(generator, 0L, streamSize),
        false);
  }

  /**
   * Returns an effectively unlimited stream of {@code double} values from the generator and/or one
   * split from it.
   *
   * <h2>Note</h2>
   *
   * <p>This method is implemented to be equivalent to
   * {@link #doubles(SplittableDoubleSupplier, long) doubles(generator, Long.MAX_VALUE)}.
   *
   * @param generator the generator
   * @return a stream of {@code double} values
   */
  public static DoubleStream doubles(SplittableDoubleSupplier generator) {
    return StreamSupport.doubleStream(new SupplierDoubleSpliterator(generator, 0L, Long.MAX_VALUE),
        false);
  }
}
