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
 * Copyright (C) 2011 - 2019 Alex Herbert
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

import org.apache.commons.rng.RandomProviderState;
import org.apache.commons.rng.RestorableUniformRandomProvider;

/**
 * Implement a Permuted Congruential Generator (PCG) using a 64-bit Linear Congruential Generator
 * (LCG) and the output XSH RS (Xor Shift; Random Shift) transform function.
 *
 * <p>This generator has 128-bits of state, outputs 32-bits per cycle and a period of
 * 2<sup>64</sup>.
 *
 * @see <a href="http://www.pcg-random.org/">PCG, A Family of Better Random Number Generators</a>
 * @since 2.0
 */
public final class PcgXshRs32
    implements RestorableUniformRandomProvider, SplittableUniformRandomProvider {
  /** The LCG multiplier. */
  private static final long MULTIPLIER = 6364136223846793005L;

  /** The default LCG increment. */
  private static final long DEFAULT_INCREMENT = 1442695040888963407L;

  /** The state of the LCG. */
  private long state;

  /** The increment of the LCG. */
  private long increment;

  /**
   * Create a new instance with the default increment.
   *
   * @param seedState the seed for the state
   */
  public PcgXshRs32(long seedState) {
    this.increment = DEFAULT_INCREMENT;
    this.state = bump(seedState + increment);
  }

  /**
   * Create a new instance.
   *
   * <p>The increment for the LCG is created using the upper 63-bits and setting the lowest bit to
   * odd. This ensures a full period generator and support for 2<sup>63</sup> increments.
   *
   * @param seedState the seed for the state
   * @param seedIncrement the seed for the increment
   */
  public PcgXshRs32(long seedState, long seedIncrement) {
    this.increment = (seedIncrement << 1) | 1;
    this.state = bump(seedState + increment);
  }

  /**
   * Create a copy.
   *
   * @param source the source
   */
  private PcgXshRs32(PcgXshRs32 source) {
    this.increment = source.increment;
    this.state = source.state;
  }

  /**
   * Advance the state of the LCG.
   *
   * @param x current state
   * @return next state
   */
  private long bump(long x) {
    return x * MULTIPLIER + increment;
  }

  @Override
  public void nextBytes(byte[] bytes) {
    nextBytes(bytes, 0, bytes.length);
  }

  @Override
  public void nextBytes(byte[] bytes, int start, int len) {
    int index = start; // Index of first insertion.

    // Index of first insertion plus multiple of 4 part of length
    // (i.e. length with 2 least significant bits unset).
    final int indexLoopLimit = index + (len & 0x7ffffffc);

    // Start filling in the byte array, 4 bytes at a time.
    while (index < indexLoopLimit) {
      final int random = nextInt();
      bytes[index++] = (byte) random;
      bytes[index++] = (byte) (random >>> 8);
      bytes[index++] = (byte) (random >>> 16);
      bytes[index++] = (byte) (random >>> 24);
    }

    final int indexLimit = start + len; // Index of last insertion + 1.

    // Fill in the remaining bytes.
    if (index < indexLimit) {
      long random = nextInt();
      for (;;) {
        bytes[index++] = (byte) random;
        if (index < indexLimit) {
          random >>>= 8;
        } else {
          break;
        }
      }
    }
  }

  @Override
  public int nextInt() {
    final long x = state;
    state = bump(state);
    final int count = (int) (x >>> 61);
    return (int) ((x ^ (x >>> 22)) >>> (22 + count));
  }

  @Override
  public int nextInt(int n) {
    if (n <= 0) {
      throw new IllegalArgumentException("Not positive: " + n);
    }
    final int nm1 = n - 1;
    if ((n & nm1) == 0) {
      // Power of 2
      return nextInt() & nm1;
    }
    int bits;
    int val;
    do {
      bits = nextInt() >>> 1;
      val = bits % n;
    } while (bits - val + nm1 < 0);

    return val;
  }

  @Override
  public long nextLong() {
    return (((long) nextInt()) << 32) | (nextInt() & 0xffffffffL);
  }

  @Override
  public long nextLong(long n) {
    if (n <= 0) {
      throw new IllegalArgumentException("Not positive: " + n);
    }
    final long nm1 = n - 1;
    if ((n & nm1) == 0L) {
      // Power of 2
      return nextLong() & nm1;
    }
    long bits;
    long val;
    do {
      bits = nextLong() >>> 1;
      val = bits % n;
    } while (bits - val + nm1 < 0);

    return val;
  }

  @Override
  public boolean nextBoolean() {
    return nextInt() < 0;
  }

  @Override
  public float nextFloat() {
    return (nextInt() >>> 8) * 0x1.0p-24f;
  }

  @Override
  public double nextDouble() {
    return (nextLong() >>> 11) * 0x1.0p-53;
  }

  /**
   * Advance the generator the given number of steps in the output sequence.
   *
   * @param steps the steps
   */
  public void advance(long steps) {
    state = NumberUtils.lcgAdvance(state, MULTIPLIER, increment, steps);
  }

  /**
   * Create a copy.
   *
   * @return the copy
   */
  public PcgXshRs32 copy() {
    return new PcgXshRs32(this);
  }

  /**
   * Create a copy and advance the generator 2<sup>48</sup> steps in the output sequence. The copy
   * is returned.
   *
   * <p>Note: It is advised to use the {@link #split()} function to create a generator that may be
   * used for the full period with very low probability of sequence collision.
   *
   * @return the copy of the previous state
   * @see #copy()
   * @see #advance(long)
   * @see #split()
   */
  public PcgXshRs32 copyAndJump() {
    final PcgXshRs32 copy = copy();
    state = NumberUtils.lcgAdvancePow2(state, MULTIPLIER, increment, 48);
    return copy;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Creates a new state and increment for the new instance. The probability of increment
   * collision is 2<sup>-63</sup>; the probability of sequence overlap between the two generators is
   * even lower.
   */
  @Override
  public PcgXshRs32 split() {
    return new PcgXshRs32(nextLong(), nextLong());
  }

  @Override
  public RandomProviderState saveState() {
    // Transform increment when saving
    return new PcgState(state, increment >> 1);
  }

  @Override
  public void restoreState(RandomProviderState state) {
    if (state instanceof PcgState) {
      final PcgState pcgState = (PcgState) state;
      this.state = pcgState.state;
      // Reverse increment transform.
      // This ensures the full period is maintained if the state increment is not odd.
      this.increment = (pcgState.increment << 1) | 1;
    } else {
      throw new IllegalArgumentException("Incompatible state");
    }
  }

  /**
   * The state of the generator.
   */
  private static class PcgState implements RandomProviderState {
    /** The state. */
    final long state;
    /** The increment. */
    final long increment;

    /**
     * Create a new instance.
     *
     * @param state the state
     * @param increment the increment
     */
    PcgState(long state, long increment) {
      this.state = state;
      this.increment = increment;
    }
  }
}
