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

import org.apache.commons.rng.RandomProviderState;
import org.apache.commons.rng.RestorableUniformRandomProvider;

/**
 * Implement a Permuted Congruential Generator (PCG) using a 64-bit Linear Congruential Generator
 * (LCG) and an output transform function to produce 32-bits per cycle.
 *
 * <p>This generator has 128-bits of state and a period of 2<sup>64</sup>.
 *
 * <p>Due to the use of an underlying linear congruential generator (LCG) alterations to the 128 bit
 * seed have the following effect: the first 64-bits alter the generator state; the second 64 bits,
 * with the exception of the most significant bit, which is discarded, choose between one of two
 * alternative LCGs where the output of the chosen LCG is the same sequence except for an additive
 * constant determined by the seed bits. The result is that seeds that differ only in the last
 * 64-bits will have a 50% chance of producing highly correlated output sequences.
 *
 * <p>Consider using the fixed increment variant where the 64-bit seed sets the generator state.
 *
 * @see <a href="http://www.pcg-random.org/">PCG, A Family of Better Random Number Generators</a>
 * @see <a href="https://ieeexplore.ieee.org/document/718715">Using Linear Congruential Generators
 *      For Parallel Random Number Generation. S 3.1: Different additive constants in a maximum
 *      potency congruential generator</a>
 */
public abstract class Pcg32
    implements RestorableUniformRandomProvider, SplittableUniformRandomProvider {
  /** The LCG multiplier. */
  private static final long MULTIPLIER = 6364136223846793005L;

  /** The default LCG increment. */
  private static final long DEFAULT_INCREMENT = 1442695040888963407L;

  /** 2^32. */
  private static final long POW_32 = 1L << 32;

  /** The state of the LCG. */
  long state;

  /** The increment of the LCG. */
  private long increment;

  /**
   * Implement the output XSH RS (Xor Shift; Random Shift) transform function.
   *
   * <p>This generator has 128-bits of state, outputs 32-bits per cycle and a period of
   * 2<sup>64</sup>.
   *
   * @see <a href="http://www.pcg-random.org/">PCG, A Family of Better Random Number Generators</a>
   */
  private static final class PcgXshRs32 extends Pcg32 {
    /** Mask for the lower 26 bits of a long, left-shifted by 27 bits. */
    private static final long MASK1 = 0x1ffffff8000000L;
    /** Mask for the lower 27 bits of a long. */
    private static final long MASK2 = 0x7ffffffL;

    /**
     * Create a new instance with the default increment.
     *
     * @param seedState the seed for the state
     */
    PcgXshRs32(long seedState) {
      super(seedState);
    }

    /**
     * Create a new instance.
     *
     * @param seedState the seed for the state
     * @param seedIncrement the seed for the increment
     */
    PcgXshRs32(long seedState, long seedIncrement) {
      super(seedState, seedIncrement);
    }

    /**
     * Create a copy.
     *
     * @param source the source
     */
    PcgXshRs32(PcgXshRs32 source) {
      super(source);
    }

    @Override
    int mix(long x) {
      return (int) ((x ^ (x >>> 22)) >>> (22 + (int) (x >>> 61)));
    }

    @Override
    public long nextLong() {
      // Get two values from the LCG
      final long x = state;
      final long y = bump(x);
      state = bump(y);
      // Perform mix function.
      // For a 32-bit output the x bits should be shifted down (22 + (int) (x >>> 61)).
      // Leave in the upper bits by shift up 32 - (22 + (int) (x >>> 61))
      final long upper = ((x ^ (x >>> 22)) << (10 - (int) (x >>> 61)));
      final long lower = ((y ^ (y >>> 22)) >>> (22 + (int) (y >>> 61)));
      return (upper & 0xffffffff00000000L) | (lower & 0xffffffffL);
    }

    /** {@inheritDoc} */
    @Override
    public double nextDouble() {
      // Get two values from the LCG
      final long x = state;
      final long y = bump(x);
      state = bump(y);
      // Perform mix function.
      // For a 32-bit output the x bits should be shifted down (22 + (int) (x >>> 61)).
      // To match nextDouble requires 26-bits from int 1 and 27 bits from int 2.
      // Int 1 is stored in the upper 32-bits as per nextLong() but shifted down 11 and
      // then masked to keep the upper 26-bits. Discard an extra 5 from int 2.
      final long upper = (x ^ (x >>> 22)) >>> (1 + (int) (x >>> 61));
      final long lower = (y ^ (y >>> 22)) >>> (27 + (int) (y >>> 61));
      return ((upper & MASK1) | (lower & MASK2)) * 0x1.0p-53;
    }

    @Override
    public Pcg32 copy() {
      return new PcgXshRs32(this);
    }

    @Override
    Pcg32 newInstance(long seedState, long seedIncrement) {
      return new PcgXshRs32(seedState, seedIncrement);
    }
  }

  /**
   * Implement the output XSH RR (Xor Shift; Random Rotate) transform function.
   *
   * <p>This generator has 128-bits of state, outputs 32-bits per cycle and a period of
   * 2<sup>64</sup>.
   *
   * @see <a href="http://www.pcg-random.org/">PCG, A Family of Better Random Number Generators</a>
   */
  private static final class PcgXshRr32 extends Pcg32 {
    /**
     * Create a new instance with the default increment.
     *
     * @param seedState the seed for the state
     */
    PcgXshRr32(long seedState) {
      super(seedState);
    }

    /**
     * Create a new instance.
     *
     * @param seedState the seed for the state
     * @param seedIncrement the seed for the increment
     */
    PcgXshRr32(long seedState, long seedIncrement) {
      super(seedState, seedIncrement);
    }

    /**
     * Create a copy.
     *
     * @param source the source
     */
    PcgXshRr32(PcgXshRr32 source) {
      super(source);
    }

    @Override
    int mix(long x) {
      return Integer.rotateRight((int) ((x ^ (x >>> 18)) >>> 27), (int) (x >>> 59));
    }

    @Override
    public Pcg32 copy() {
      return new PcgXshRr32(this);
    }

    @Override
    Pcg32 newInstance(long seedState, long seedIncrement) {
      return new PcgXshRr32(seedState, seedIncrement);
    }
  }

  /**
   * Create a new instance with the default increment.
   *
   * @param seedState the seed for the state
   */
  Pcg32(long seedState) {
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
  Pcg32(long seedState, long seedIncrement) {
    this.increment = (seedIncrement << 1) | 1;
    this.state = bump(seedState + increment);
  }

  /**
   * Create a copy.
   *
   * @param source the source
   */
  Pcg32(Pcg32 source) {
    this.increment = source.increment;
    this.state = source.state;
  }

  /**
   * Create a Permuted Congruential Generator (PCG) using a 64-bit Linear Congruential Generator
   * (LCG) and the output XSH RS (Xor Shift; Random Shift) transform function.
   *
   * <p>Uses the default increment.
   *
   * @param seedState the seed for the state
   * @return the generator
   */
  public static Pcg32 xshrs(long seedState) {
    return new PcgXshRs32(seedState);
  }

  /**
   * Create a Permuted Congruential Generator (PCG) using a 64-bit Linear Congruential Generator
   * (LCG) and the output XSH RS (Xor Shift; Random Shift) transform function.
   *
   * <p>The increment for the LCG is created using the upper 63-bits and setting the lowest bit to
   * odd. This ensures a full period generator and support for 2<sup>63</sup> increments.
   *
   * @param seedState the seed for the state
   * @param seedIncrement the seed for the increment
   * @return the generator
   */
  public static Pcg32 xshrs(long seedState, long seedIncrement) {
    return new PcgXshRs32(seedState, seedIncrement);
  }

  /**
   * Create a Permuted Congruential Generator (PCG) using a 64-bit Linear Congruential Generator
   * (LCG) and the output XSH RR (Xor Shift; Random Rotate) transform function.
   *
   * <p>Uses the default increment.
   *
   * @param seedState the seed for the state
   * @return the generator
   */
  public static Pcg32 xshrr(long seedState) {
    return new PcgXshRr32(seedState);
  }

  /**
   * Create a Permuted Congruential Generator (PCG) using a 64-bit Linear Congruential Generator
   * (LCG) and the output XSH RR (Xor Shift; Random Rotate) transform function.
   *
   * <p>The increment for the LCG is created using the upper 63-bits and setting the lowest bit to
   * odd. This ensures a full period generator and support for 2<sup>63</sup> increments.
   *
   * @param seedState the seed for the state
   * @param seedIncrement the seed for the increment
   * @return the generator
   */
  public static Pcg32 xshrr(long seedState, long seedIncrement) {
    return new PcgXshRr32(seedState, seedIncrement);
  }

  /**
   * Advance the state of the LCG.
   *
   * @param x current state
   * @return next state
   */
  final long bump(long x) {
    return x * MULTIPLIER + increment;
  }

  /**
   * Mix the 64-bit state to a 32-bit output. This should be implemented by subclasses for different
   * generators.
   *
   * @param x the state
   * @return the output
   */
  abstract int mix(long x);

  /**
   * Create a new instance.
   *
   * @param seedState the seed for the state
   * @param seedIncrement the seed for the increment
   * @return the generator
   */
  abstract Pcg32 newInstance(long seedState, long seedIncrement);

  /**
   * Create a copy.
   *
   * @return the copy
   */
  public abstract Pcg32 copy();

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
    state = bump(x);
    return mix(x);
  }

  @Override
  public int nextInt(int n) {
    if (n <= 0) {
      throw new IllegalArgumentException("Not positive: " + n);
    }
    // Lemire (2019): Fast Random Integer Generation in an Interval
    // https://arxiv.org/abs/1805.10941
    long mult = (nextInt() & 0xffffffffL) * n;
    long left = mult & 0xffffffffL;
    if (left < n) {
      // 2^32 % n
      final long t = POW_32 % n;
      while (left < t) {
        mult = (nextInt() & 0xffffffffL) * n;
        left = mult & 0xffffffffL;
      }
    }
    return (int) (mult >>> 32);
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
    if ((n & nm1) == 0) {
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
    return NumberUtils.makeDouble(nextInt(), nextInt());
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
  public Pcg32 copyAndJump() {
    final Pcg32 copy = copy();
    state = NumberUtils.lcgAdvancePow2(state, MULTIPLIER, increment, 48);
    return copy;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Creates a new state and increment for the new instance using a bijection mapping of the
   * current 127-bit state (1-bit of the increment is not used) to a new state. After a split the
   * original generator is advanced one cycle of the generator.
   *
   * <p>Note that due to the use of an underlying LCG a change to the increment chooses between 2
   * related families of generators. For the purposes of overlap computation this generator should
   * be considered to have a period of 2<sup>64</sup>.
   */
  @Override
  public Pcg32 split() {
    // Note: In nextInt() the old state is unused so bump after copying.
    final long s0 = state;
    final long s1 = increment >>> 1;
    state = bump(s1);
    return newInstance(Mixers.stafford13(s0), Mixers.stafford13(s1));
  }

  @Override
  public RandomProviderState saveState() {
    // Transform increment when saving
    return new PcgState(state, increment >>> 1);
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
