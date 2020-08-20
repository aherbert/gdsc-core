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
 * Copyright (C) 2011 - 2020 Alex Herbert
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

import java.math.BigInteger;
import java.util.Formatter;
import org.apache.commons.rng.UniformRandomProvider;
import uk.ac.sussex.gdsc.core.data.VisibleForTesting;

/**
 * Decorate a {@link UniformRandomProvider} to accumulate usage statistics.
 *
 * <p>Supports counting invocations up to 2<sup>64</sup> - 1.
 *
 * @since 2.0
 */
public class UniformRandomProviderUsageDecorator extends UniformRandomProviderDecorator {
  /**
   * The {@link UniformRandomProvider#nextBytes(byte[])} invocation count.
   */
  private long nextBytesCount;

  /**
   * The {@link UniformRandomProvider#nextBytes(byte[])} total bytes size.
   */
  private final SizeCounter nextBytesSize = new SizeCounter();

  /**
   * The {@link UniformRandomProvider#nextBytes(byte[], int, int)} invocation count.
   */
  private long nextBytesRangeCount;

  /**
   * The {@link UniformRandomProvider#nextBytes(byte[], int, int)} total bytes size.
   */
  private final SizeCounter nextBytesRangeSize = new SizeCounter();

  /**
   * The {@link UniformRandomProvider#nextInt()} invocation count.
   */
  private long nextIntCount;

  /**
   * The {@link UniformRandomProvider#nextInt(int)} invocation count.
   */
  private long nextIntRangeCount;

  /**
   * The {@link UniformRandomProvider#nextLong()} invocation count.
   */
  private long nextLongCount;

  /**
   * The {@link UniformRandomProvider#nextLong(long)} invocation count.
   */
  private long nextLongRangeCount;

  /**
   * The {@link UniformRandomProvider#nextBoolean()} invocation count.
   */
  private long nextBooleanCount;

  /**
   * The {@link UniformRandomProvider#nextFloat()} invocation count.
   */
  private long nextFloatCount;

  /**
   * The {@link UniformRandomProvider#nextDouble()} invocation count.
   */
  private long nextDoubleCount;

  /**
   * A counter of unsigned integers up to a maximum of 128-bits.
   *
   * <p>Support beyond 128-bits is redundant as the 64-bit method invocation counter will roll-over
   * before this rolls over 128-bits.
   */
  @VisibleForTesting
  static class SizeCounter {
    /** The lower bits. */
    private long low;
    /** The high bits. */
    private long high;

    /**
     * Reset the count.
     */
    void reset() {
      low = high = 0;
    }

    /**
     * Adds the value as if it were an unsigned 32-bit value.
     *
     * @param value the value
     */
    void addUnsigned(int value) {
      addUnsigned(value & 0xffffffffL);
    }

    /**
     * Adds the value as if it were an unsigned 64-bit value.
     *
     * @param value the value
     */
    void addUnsigned(long value) {
      final long before = low;
      // twos-compliment addition is effectively unsigned
      low += value;
      // Check for roll-over of 64-bit unsigned number.
      // This is equivalent to Long.compareUnsigned(low, before) < 0.
      if (low + Long.MIN_VALUE < before + Long.MIN_VALUE) {
        high++;
      }
    }

    /**
     * Adds the value from the other instance.
     *
     * @param other the other instance.
     */
    void add(SizeCounter other) {
      addUnsigned(other.low);
      // Ignore any roll-over
      high += other.high;
    }

    /**
     * Gets the value. This is limited to a 128-bit unsigned integer.
     *
     * @return the value
     */
    BigInteger value() {
      BigInteger result = toBigInteger(low);
      if (high != 0) {
        // Add the high bits
        result = result.add(toBigInteger(high).shiftLeft(64));
      }
      return result;
    }

    /**
     * Gets the value as a long. This is equivalent to a narrowing primitive conversion of the
     * 128-bit value.
     *
     * @return the count
     * @see BigInteger#longValue()
     */
    long longValue() {
      return low;
    }

    /**
     * Convert the 64-bit unsigned number to a BigInteger.
     *
     * @param bits the bits
     * @return the big integer
     */
    static BigInteger toBigInteger(long bits) {
      return (bits >= 0) ? BigInteger.valueOf(bits) : new BigInteger(Long.toUnsignedString(bits));
    }
  }

  /**
   * Create a new instance.
   *
   * @param rng the rng
   */
  public UniformRandomProviderUsageDecorator(UniformRandomProvider rng) {
    super(rng);
  }

  @Override
  public void nextBytes(byte[] bytes) {
    super.nextBytes(bytes);
    nextBytesCount++;
    nextBytesSize.addUnsigned(bytes.length);
  }

  @Override
  public void nextBytes(byte[] bytes, int start, int len) {
    super.nextBytes(bytes, start, len);
    nextBytesRangeCount++;
    // Check positive to avoid invalid accumulation.
    // It is expected the nextBytes method ignored this too.
    if (len > 0) {
      nextBytesRangeSize.addUnsigned(len);
    }
  }

  @Override
  public int nextInt() {
    nextIntCount++;
    return super.nextInt();
  }

  @Override
  public int nextInt(int n) {
    nextIntRangeCount++;
    return super.nextInt(n);
  }

  @Override
  public long nextLong() {
    nextLongCount++;
    return super.nextLong();
  }

  @Override
  public long nextLong(long n) {
    nextLongRangeCount++;
    return super.nextLong(n);
  }

  @Override
  public boolean nextBoolean() {
    nextBooleanCount++;
    return super.nextBoolean();
  }

  @Override
  public float nextFloat() {
    nextFloatCount++;
    return super.nextFloat();
  }

  @Override
  public double nextDouble() {
    nextDoubleCount++;
    return super.nextDouble();
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    try (Formatter formatter = new Formatter(sb)) {
      formatter.format("RNG:            %s%n", super.toString());
      formatter.format("nextBytes:      %s (%s bytes)%n", Long.toUnsignedString(nextBytesCount),
          getNextBytesSize());
      formatter.format("nextBytesRange: %s (%s bytes)%n",
          Long.toUnsignedString(nextBytesRangeCount), getNextBytesRangeSize());
      formatter.format("nextInt:        %s%n", Long.toUnsignedString(nextIntCount));
      formatter.format("nextInt(n):     %s%n", Long.toUnsignedString(nextIntRangeCount));
      formatter.format("nextLong:       %s%n", Long.toUnsignedString(nextLongCount));
      formatter.format("nextLong(n):    %s%n", Long.toUnsignedString(nextLongRangeCount));
      formatter.format("nextBoolean:    %s%n", Long.toUnsignedString(nextBooleanCount));
      formatter.format("nextFloat:      %s%n", Long.toUnsignedString(nextFloatCount));
      formatter.format("nextDouble:     %s%n", Long.toUnsignedString(nextDoubleCount));
    }
    return sb.toString();
  }

  /**
   * Reset all usage count to zero.
   */
  public void reset() {
    nextBytesCount = 0;
    nextBytesSize.reset();
    nextBytesRangeCount = 0;
    nextBytesRangeSize.reset();
    nextIntCount = 0;
    nextIntRangeCount = 0;
    nextLongCount = 0;
    nextLongRangeCount = 0;
    nextBooleanCount = 0;
    nextFloatCount = 0;
    nextDoubleCount = 0;
  }

  /**
   * Adds the usage values from the other instance to this instance.
   *
   * @param other the other instance
   */
  public void add(UniformRandomProviderUsageDecorator other) {
    nextBytesCount += other.nextBytesCount;
    nextBytesSize.add(other.nextBytesSize);
    nextBytesRangeCount += other.nextBytesRangeCount;
    nextBytesRangeSize.add(other.nextBytesRangeSize);
    nextIntCount += other.nextIntCount;
    nextIntRangeCount += other.nextIntRangeCount;
    nextLongCount += other.nextLongCount;
    nextLongRangeCount += other.nextLongRangeCount;
    nextBooleanCount += other.nextBooleanCount;
    nextFloatCount += other.nextFloatCount;
    nextDoubleCount += other.nextDoubleCount;
  }

  /**
   * Gets the {@link UniformRandomProvider#nextBytes(byte[])} invocation count.
   *
   * @return the count
   */
  public long getNextBytesCount() {
    return nextBytesCount;
  }

  /**
   * Gets the {@link UniformRandomProvider#nextBytes(byte[])} total bytes count.
   *
   * <p>Note: The size is limited to a maximum of a 128-bit unsigned integer. This supports the
   * maximum value possible with up to 2<sup>64</sup> invocations of the
   * {@link UniformRandomProvider#nextBytes(byte[])} method, irrespective of the {@code byte[]}
   * array length filled by the method.
   *
   * @return the size
   */
  public BigInteger getNextBytesSize() {
    return nextBytesSize.value();
  }

  /**
   * Gets the {@link UniformRandomProvider#nextBytes(byte[])} total bytes count as an unsigned long
   * value.
   *
   * <p>This is equivalent to a narrowing primitive conversion of the 128-bit unsigned value; the
   * returned result may lose information about the magnitude of the value and may be negative.
   *
   * @return the size
   */
  public long getNextBytesSizeAsLong() {
    return nextBytesSize.longValue();
  }

  /**
   * Gets the {@link UniformRandomProvider#nextBytes(byte[], int, int)} invocation count.
   *
   * @return the count
   */
  public long getNextBytesRangeCount() {
    return nextBytesRangeCount;
  }

  /**
   * Gets the {@link UniformRandomProvider#nextBytes(byte[], int, int)} total bytes count.
   *
   * <p>Note: The size is limited to a maximum of a 128-bit unsigned integer. This supports the
   * maximum value possible with up to 2<sup>64</sup> invocations of the
   * {@link UniformRandomProvider#nextBytes(byte[],int,int)} method, irrespective of the
   * {@code byte[]} array length filled by the method.
   *
   * @return the size
   */
  public BigInteger getNextBytesRangeSize() {
    return nextBytesRangeSize.value();
  }

  /**
   * Gets the {@link UniformRandomProvider#nextBytes(byte[], int, int)} total bytes count as an
   * unsigned long value.
   *
   * <p>This is equivalent to a narrowing primitive conversion of the 128-bit unsigned value; the
   * returned result may lose information about the magnitude of the value and may be negative.
   *
   * @return the size
   */
  public long getNextBytesRangeSizeAsLong() {
    return nextBytesRangeSize.longValue();
  }

  /**
   * Gets the {@link UniformRandomProvider#nextInt} invocation count.
   *
   * @return the count
   */
  public long getNextIntCount() {
    return nextIntCount;
  }

  /**
   * Gets the {@link UniformRandomProvider#nextInt(int)} invocation count.
   *
   * @return the count
   */
  public long getNextIntRangeCount() {
    return nextIntRangeCount;
  }

  /**
   * Gets the {@link UniformRandomProvider#nextLong()} invocation count.
   *
   * @return the count
   */
  public long getNextLongCount() {
    return nextLongCount;
  }

  /**
   * Gets the {@link UniformRandomProvider#nextLong(long)} invocation count.
   *
   * @return the count
   */
  public long getNextLongRangeCount() {
    return nextLongRangeCount;
  }

  /**
   * Gets the {@link UniformRandomProvider#nextBoolean()} invocation count.
   *
   * @return the count
   */
  public long getNextBooleanCount() {
    return nextBooleanCount;
  }

  /**
   * Gets the {@link UniformRandomProvider#nextFloat()} invocation count.
   *
   * @return the count
   */
  public long getNextFloatCount() {
    return nextFloatCount;
  }

  /**
   * Gets the {@link UniformRandomProvider#nextDouble()} invocation count.
   *
   * @return the count
   */
  public long getNextDoubleCount() {
    return nextDoubleCount;
  }
}
