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

import java.util.Arrays;

/**
 * Computation for information entropy.
 *
 * <p>The entropy is computed as the negative sum of the logarithm of the probability mass function
 * for the value.
 *
 * <pre>
 * S = - sum [ Pi log (Pi) ]
 * </pre>
 *
 * <p>When the logarithm base is 2 the units are 'bits'.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Entropy_(information_theory)">Entropy (information
 *      theory)</a>
 */
public final class Entropy {
  /** The log2 of e. */
  private static final double LOG_2_OF_E = 1.0 / Math.log(2);

  /** No public construction. */
  private Entropy() {}

  /**
   * Define a digest to compute entropy of raw bit data.
   */
  public interface EntropyDigest {
    /**
     * Adds the byte.
     *
     * @param value the value.
     */
    void add(byte value);

    /**
     * Adds the integer.
     *
     * @param value the value.
     */
    void add(int value);

    /**
     * Adds the long.
     *
     * @param value the value
     */
    void add(long value);

    /**
     * Compute the entropy in bits.
     *
     * @return the entropy
     */
    double bits();

    /**
     * Reset the digest.
     */
    void reset();
  }

  /**
   * A base class to compute the entropy of raw bit data.
   */
  private abstract static class AbstractEntropyDigest implements EntropyDigest {
    /**
     * Adds the byte.
     *
     * @param value the value.
     */
    @Override
    public void add(byte value) {
      addByte(value & 0xff);
    }

    /**
     * Adds the integer.
     *
     * @param value the value.
     */
    @Override
    public void add(int value) {
      addByte((value >>> 24));
      addByte((value >>> 16) & 0xff);
      addByte((value >>> 8) & 0xff);
      addByte(value & 0xff);
    }

    /**
     * Adds the long.
     *
     * @param value the value
     */
    @Override
    public void add(long value) {
      addByte((int) (value >>> 56));
      addByte((int) (value >>> 48) & 0xff);
      addByte((int) (value >>> 40) & 0xff);
      addByte((int) (value >>> 32) & 0xff);
      add((int) value);
    }

    /**
     * Adds the byte as an unsigned 8-bit integer.
     *
     * @param value the value
     */
    abstract void addByte(int value);
  }

  /**
   * A class to compute the entropy of a stream of bytes assuming 256 states.
   */
  private static class ByteEntropyDigest extends AbstractEntropyDigest {
    /** The total. */
    private long total;
    /** The counts. */
    private final long[] counts = new long[256];

    @Override
    void addByte(int value) {
      total++;
      counts[value]++;
    }

    @Override
    public double bits() {
      return Entropy.computeBits(total, counts);
    }

    @Override
    public void reset() {
      total = 0;
      Arrays.fill(counts, 0);
    }
  }

  /**
   * A class to compute the entropy of a stream of bytes assuming 2 states.
   */
  private static class BinaryEntropyDigest extends AbstractEntropyDigest {
    /** A lookup table containing the number of one-bits for each byte value. */
    private static final int[] ONES;

    static {
      ONES = new int[256];
      // Ignore zero when filling
      for (int i = 1; i < 256; i++) {
        ONES[i] = Integer.bitCount(i);
      }
    }

    /** The total. */
    private long total;
    /** The counts of ones. */
    private long count1;

    @Override
    void addByte(int value) {
      total += 8;
      count1 += ONES[value];
    }

    @Override
    public double bits() {
      return Entropy.computeBits(total, total - count1, count1);
    }

    @Override
    public void reset() {
      count1 = total = 0;
    }
  }

  /**
   * Compute the entropy in bits using the probability of each state. Negative probabilities are
   * ignored. No checks are made that the probabilities are in the interval {@code [0, 1]} and sum
   * to 1.
   *
   * @param probabilities the probabilities
   * @return the entropy (bits)
   */
  public static double bits(double... probabilities) {
    double sum = 0;
    for (final double p : probabilities) {
      if (p > 0) {
        sum -= p * Math.log(p);
      }
    }
    return sum * LOG_2_OF_E;
  }

  /**
   * Compute the entropy in bits using the count of each state. Negative counts are ignored.
   *
   * @param counts the counts
   * @return the entropy (bits)
   */
  public static double bits(long... counts) {
    return computeBits(Arrays.stream(counts).filter(l -> l >= 0).sum(), counts);
  }

  /**
   * Compute the entropy in bits using the count of each state. Negative counts are ignored.
   *
   * @param counts the counts
   * @return the entropy (bits)
   */
  public static double bits(int... counts) {
    final long total = Arrays.stream(counts).filter(l -> l >= 0).sum();
    double sum = 0;
    for (final int c : counts) {
      if (c > 0) {
        final double p = (double) c / total;
        sum -= p * Math.log(p);
      }
    }
    return sum * LOG_2_OF_E;
  }

  /**
   * Compute the entropy in bits using the count of each state. Negative counts are ignored.
   *
   * @param total the total
   * @param counts the counts
   * @return the entropy (bits)
   */
  private static double computeBits(long total, long... counts) {
    double sum = 0;
    for (final long c : counts) {
      if (c > 0) {
        final double p = (double) c / total;
        sum -= p * Math.log(p);
      }
    }
    return sum * LOG_2_OF_E;
  }

  /**
   * Creates the digest to compute entropy of raw bit data.
   *
   * <p>Supports entropy using 2-states (bits) or 256-states (bytes).
   *
   * @param binary Set to true to use a 2-state (binary) entropy.
   * @return the entropy digest
   */
  public static EntropyDigest createDigest(boolean binary) {
    return binary ? new BinaryEntropyDigest() : new ByteEntropyDigest();
  }
}
