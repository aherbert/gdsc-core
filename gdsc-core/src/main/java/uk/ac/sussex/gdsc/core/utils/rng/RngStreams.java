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

import java.util.Objects;
import java.util.Spliterator;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.StreamSupport;
import org.apache.commons.rng.sampling.distribution.DiscreteUniformSampler;
import org.apache.commons.rng.sampling.distribution.UniformLongSampler;

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

  /**
   * Spliterator for int streams. Supports 2 versions:
   *
   * <ul>
   *
   * <li>Fixed stream length; unbounded sample output
   *
   * <li>Infinite stream length; unbounded sample output
   *
   * </ul>
   *
   * <p>An infinite stream is created using a end of Long.MAX_VALUE. Splitting is done by dividing
   * in half until this is not possible.
   *
   * <p>The long and double versions of this class are identical except for types.
   */
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

  /**
   * Wrap a splittable RNG to implement the SplittableIntSupplier.
   */
  static final class RngSplittableIntSupplier implements SplittableIntSupplier {
    /** The generator. */
    private final SplittableUniformRandomProvider rng;

    /**
     * Create an instance.
     *
     * @param rng the generator
     */
    RngSplittableIntSupplier(SplittableUniformRandomProvider rng) {
      this.rng = rng;
    }

    @Override
    public int getAsInt() {
      return rng.nextInt();
    }

    @Override
    public SplittableIntSupplier split() {
      return new RngSplittableIntSupplier(rng.split());
    }
  }

  /**
   * Wrap a splittable RNG to implement the SplittableLongSupplier.
   */
  static final class RngSplittableLongSupplier implements SplittableLongSupplier {
    /** The generator. */
    private final SplittableUniformRandomProvider rng;

    /**
     * Create an instance.
     *
     * @param rng the generator
     */
    RngSplittableLongSupplier(SplittableUniformRandomProvider rng) {
      this.rng = rng;
    }

    @Override
    public long getAsLong() {
      return rng.nextLong();
    }

    @Override
    public SplittableLongSupplier split() {
      return new RngSplittableLongSupplier(rng.split());
    }
  }

  /**
   * Wrap a splittable RNG to implement the SplittableDoubleSupplier.
   */
  static final class RngSplittableDoubleSupplier implements SplittableDoubleSupplier {
    /** The generator. */
    private final SplittableUniformRandomProvider rng;

    /**
     * Create an instance.
     *
     * @param rng the generator
     */
    RngSplittableDoubleSupplier(SplittableUniformRandomProvider rng) {
      this.rng = rng;
    }

    @Override
    public double getAsDouble() {
      return rng.nextDouble();
    }

    @Override
    public SplittableDoubleSupplier split() {
      return new RngSplittableDoubleSupplier(rng.split());
    }
  }

  /**
   * Wrap a splittable RNG to implement the SplittableDoubleSupplier with a range. Note: This does
   * not use {@link org.apache.commons.rng.sampling.distribution.ContinuousUniformSampler
   * ContinuousUniformSampler} to support the exclusive upper bound.
   */
  static final class RangeSplittableDoubleSupplier implements SplittableDoubleSupplier {
    /** The generator. */
    private final SplittableUniformRandomProvider rng;
    /** The low bound (includive). */
    private final double lo;
    /** The high bound (exclusive). */
    private final double hi;
    /** The range. */
    private final double range;
    /** The upper limit (inclusive). */
    private final double upper;

    /**
     * Create an instance.
     *
     * @param rng the generator
     * @param lo the low bound (inclusive)
     * @param hi the high bound (exclusive)
     */
    RangeSplittableDoubleSupplier(SplittableUniformRandomProvider rng, double lo, double hi) {
      this.rng = rng;
      this.lo = lo;
      this.hi = hi;
      range = hi - lo;
      upper = Math.nextDown(hi);
    }

    /**
     * Create a copy instance.
     *
     * @param source the source
     */
    private RangeSplittableDoubleSupplier(RangeSplittableDoubleSupplier source) {
      this.rng = source.rng.split();
      lo = source.lo;
      hi = source.hi;
      range = source.range;
      upper = source.upper;
    }

    @Override
    public double getAsDouble() {
      final double u = rng.nextDouble() * range + lo;
      // Correct for rounding.
      return u >= hi ? upper : u;
    }

    @Override
    public SplittableDoubleSupplier split() {
      return new RangeSplittableDoubleSupplier(this);
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
    Objects.requireNonNull(rng, "rng");
    return ints(new RngSplittableIntSupplier(rng), streamSize);
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
    Objects.requireNonNull(rng, "rng");
    return ints(new RngSplittableIntSupplier(rng));
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
    Objects.requireNonNull(rng, "rng");
    return ints(Splittables.ofInt(rng,
        DiscreteUniformSampler.of(rng, randomNumberOrigin, randomNumberBound - 1)), streamSize);
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
    Objects.requireNonNull(rng, "rng");
    return ints(Splittables.ofInt(rng,
        DiscreteUniformSampler.of(rng, randomNumberOrigin, randomNumberBound - 1)));
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
    Objects.requireNonNull(rng, "rng");
    return longs(new RngSplittableLongSupplier(rng), streamSize);
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
    Objects.requireNonNull(rng, "rng");
    return longs(new RngSplittableLongSupplier(rng));
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
    Objects.requireNonNull(rng, "rng");
    return longs(Splittables.ofLong(rng,
        UniformLongSampler.of(rng, randomNumberOrigin, randomNumberBound - 1)), streamSize);
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
    Objects.requireNonNull(rng, "rng");
    return longs(Splittables.ofLong(rng,
        UniformLongSampler.of(rng, randomNumberOrigin, randomNumberBound - 1)));
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
    Objects.requireNonNull(rng, "rng");
    return doubles(new RngSplittableDoubleSupplier(rng), streamSize);
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
    Objects.requireNonNull(rng, "rng");
    return doubles(new RngSplittableDoubleSupplier(rng));
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
    Objects.requireNonNull(rng, "rng");
    return doubles(new RangeSplittableDoubleSupplier(rng, randomNumberOrigin, randomNumberBound),
        streamSize);
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
    Objects.requireNonNull(rng, "rng");
    return doubles(new RangeSplittableDoubleSupplier(rng, randomNumberOrigin, randomNumberBound));
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
