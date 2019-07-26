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
import org.apache.commons.rng.UniformRandomProvider;

/**
 * Implement the SplitMix algorithm from {@link java.util.SplittableRandom SplittableRandom} and the
 * {@link UniformRandomProvider} interface. Adds functions to allow the state to be advanced and
 * copied.
 *
 * <p>This generator has 64-bits of state, outputs 64-bits per cycle and a period of
 * 2<sup>64</sup>.
 */
public final class SplitMix implements RestorableUniformRandomProvider {
  /**
   * The golden ratio, phi, scaled to 64-bits and rounded to odd.
   *
   * <pre>
   * phi = (sqrt(5) - 1) / 2) * 2^64
   *     ~ 0.61803 * 2^64
   *     = 11400714819323198485 (unsigned 64-bit integer)
   * </pre>
   */
  private static final long GOLDEN_RATIO = 0x9e3779b97f4a7c15L;

  /** The state. */
  private long state;

  /**
   * Create a new instance.
   *
   * @param seed the seed
   */
  public SplitMix(long seed) {
    this.state = seed;
  }

  /**
   * Gets the state of the generator.
   *
   * @return the state
   */
  public long getState() {
    return state;
  }

  @Override
  public void nextBytes(byte[] bytes) {
    nextBytes(bytes, 0, bytes.length);
  }

  @Override
  public void nextBytes(byte[] bytes, int start, int len) {
    int index = start; // Index of first insertion.

    // Index of first insertion plus multiple of 8 part of length
    // (i.e. length with 3 least significant bits unset).
    final int indexLoopLimit = index + (len & 0x7ffffff8);

    // Start filling in the byte array, 8 bytes at a time.
    while (index < indexLoopLimit) {
      final long random = nextLong();
      bytes[index++] = (byte) random;
      bytes[index++] = (byte) (random >>> 8);
      bytes[index++] = (byte) (random >>> 16);
      bytes[index++] = (byte) (random >>> 24);
      bytes[index++] = (byte) (random >>> 32);
      bytes[index++] = (byte) (random >>> 40);
      bytes[index++] = (byte) (random >>> 48);
      bytes[index++] = (byte) (random >>> 56);
    }

    final int indexLimit = start + len; // Index of last insertion + 1.

    // Fill in the remaining bytes.
    if (index < indexLimit) {
      long random = nextLong();
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
    long key = state += GOLDEN_RATIO;
    // 32 high bits of Stafford variant 4 mix64 function as int:
    // http://zimbry.blogspot.com/2011/09/better-bit-mixing-improving-on.html
    key = (key ^ (key >>> 33)) * 0x62a9d9ed799705f5L;
    return (int) (((key ^ (key >>> 28)) * 0xcb24d0a5c88c35b3L) >>> 32);
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
    long key = state += GOLDEN_RATIO;
    // Stafford variant 13 of 64-bit mix function:
    // http://zimbry.blogspot.com/2011/09/better-bit-mixing-improving-on.html
    key = (key ^ (key >>> 30)) * 0xbf58476d1ce4e5b9L;
    key = (key ^ (key >>> 27)) * 0x94d049bb133111ebL;
    return key ^ (key >>> 31);
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
    state += GOLDEN_RATIO * steps;
  }

  /**
   * Create a copy.
   *
   * @return the copy
   */
  public SplitMix copy() {
    return new SplitMix(state);
  }

  /**
   * Create a copy and advance the generator 2<sup>48</sup> steps in the output sequence. The copy
   * is returned.
   *
   * @return the copy of the previous state
   * @see #copy()
   * @see #advance(long)
   */
  public SplitMix copyAndJump() {
    final SplitMix copy = copy();
    advance(1L << 48);
    return copy;
  }

  @Override
  public RandomProviderState saveState() {
    return new SplitMixState(state);
  }

  @Override
  public void restoreState(RandomProviderState state) {
    if (state instanceof SplitMixState) {
      this.state = ((SplitMixState) state).state;
    } else {
      throw new IllegalArgumentException("Incompatible state");
    }
  }

  /**
   * The state of the SplitMix generator.
   */
  private static class SplitMixState implements RandomProviderState {
    /** The state. */
    final long state;

    /**
     * Create a new instance.
     *
     * @param state the state
     */
    SplitMixState(long state) {
      this.state = state;
    }
  }
}
