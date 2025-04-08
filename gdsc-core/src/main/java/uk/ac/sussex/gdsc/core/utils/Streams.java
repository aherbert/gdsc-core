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

import java.util.Objects;
import java.util.Spliterator;
import java.util.function.DoubleConsumer;
import java.util.stream.DoubleStream;
import java.util.stream.StreamSupport;

/**
 * Provides {@code Stream} utilities.
 *
 * @since 2.1
 * @see DoubleStream
 */
public final class Streams {

  /** Spliterator characteristics. */
  private static final int CHARACTERISTICS =
      Spliterator.SIZED | Spliterator.SUBSIZED | Spliterator.NONNULL | Spliterator.IMMUTABLE;

  /**
   * A spliterator for a range of a float array. Values are exposed as doubles.
   */
  private static class FloatSpliterator implements Spliterator.OfDouble {
    /** The data. */
    private final float[] data;
    /** The current position in the range. */
    private int position;
    /** The upper limit of the range. */
    private final int end;

    /**
     * Create a new instance.
     *
     * @param data the data
     * @param start the start position of the stream (inclusive)
     * @param end the upper limit of the stream (exclusive)
     */
    FloatSpliterator(float[] data, int start, int end) {
      this.data = data;
      this.position = start;
      this.end = end;
    }

    @Override
    public FloatSpliterator trySplit() {
      final int start = position;
      final int middle = (start + end) >>> 1;
      if (middle <= start) {
        return null;
      }
      position = middle;
      return new FloatSpliterator(data, start, middle);
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
      final int pos = position;
      if (pos < end) {
        consumer.accept(data[pos]);
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
      int pos = position;
      final int last = end;
      if (pos < last) {
        position = last;
        final float[] d = data;
        do {
          consumer.accept(d[pos]);
        } while (++pos < last);
      }
    }
  }

  /** No public construction. */
  private Streams() {}

  /**
   * Returns a sequential {@link DoubleStream} with the specified array as its source. The stream
   * {@code float} values are converted to {@code double} on demand using primitive type conversion.
   *
   * @param array The array, assumed to be unmodified during use
   * @return a {@code DoubleStream} for the array
   */
  public static DoubleStream stream(float[] array) {
    return StreamSupport
        .doubleStream(new FloatSpliterator(Objects.requireNonNull(array), 0, array.length), false);
  }

  /**
   * Returns a sequential {@link DoubleStream} with the specified array as its source. The stream
   * {@code float} values are converted to {@code double} on demand using primitive type conversion.
   *
   * @param array The array, assumed to be unmodified during use
   * @param from the first index to cover, inclusive
   * @param to the last index, exclusive
   * @return a {@code DoubleStream} for the array
   * @throws IllegalArgumentException if {@code from} is greater than {@code to}.
   * @throws ArrayIndexOutOfBoundsException if {@code from} or {@code to} are greater than
   *         {@code arrayLength} or negative.
   */
  public static DoubleStream stream(float[] array, int from, int to) {
    it.unimi.dsi.fastutil.Arrays.ensureFromTo(Objects.requireNonNull(array).length, from, to);
    return StreamSupport.doubleStream(new FloatSpliterator(array, from, to), false);
  }
}
