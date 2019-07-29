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

/**
 * Contains bit-mix functions.
 *
 * @since 2.0
 */
public final class Mixers {
  /** The 64-bit RXS-M-XS multiplier. */
  private static final long RXSMXS_MULTIPLIER = Long.parseUnsignedLong("12605985483714917081");
  /** The 64-bit RXS-M-XS unmultiplier. */
  private static final long RXSMXS_UNMULTIPLIER = Long.parseUnsignedLong("15009553638781119849");

  /** The rotation inversion table for rrmxmx. */
  private static final int[] rots = {4, 8, 9, 11, 15, 16, 18, 20, 24, 25, 26, 29, 30, 32, 40, 41,
      43, 44, 45, 48, 50, 54, 56, 57, 58, 60};

  /**
   * No public constructor.
   */
  private Mixers() {}

  /**
   * Perform an inversion of a right xorshift.
   *
   * <pre>
   * value = (x >>> shift) ^ x
   * </pre>
   *
   * <p>The shift value is not checked that it is positive. If zero an infinite loops occurs. If
   * negative the results are undefined.
   *
   * @param value the value
   * @param shift the shift (must be strictly positive)
   * @return the inverted value (x)
   */
  public static long unxorshift(long value, int shift) {
    // @formatter:off
    // Note: a xor operation can be used to recover itself:
    // x ^ y = z
    // z ^ y = x
    // During a right xor shift of size n the top n-bits are unchanged.
    // These known values can be used to recover the next set of n-bits.
    // Thus (x >>> shift) ^ x:
    //
    //   abcdefgh
    // ^     abcd
    // = abcdwxyz
    //
    // Reverse:
    //
    //   abcdwxyz
    // ^     abcd
    // = abcdefgh
    //
    // This can be done recursively in blocks on n-bits until all are recovered.
    // @formatter:on

    // Initialise the recovered value. This will have the correct top n-bits set.
    long recovered = value;
    for (int bits = shift; bits < 64; bits += shift) {
      recovered = recovered ^ (value >>> bits);
    }
    return recovered;
  }

  /**
   * Perform the 64-bit RXS-M-XS (Rand Xors shift; Multiply; Xor shift) mix function of the
   * Permutated Congruential Generator (PCG) family.
   *
   * @param x the input value
   * @return the output value
   * @see <a href="http://www.pcg-random.org/"> PCG, A Family of Better Random Number Generators</a>
   */
  public static long rxsmxs(long x) {
    final long word = ((x >>> ((x >>> 59) + 5)) ^ x) * RXSMXS_MULTIPLIER;
    return (word >>> 43) ^ word;
  }

  /**
   * Reverse the 64-bit RXS-M-XS (Rand Xors shift; Multiply; Xor shift) mix function of the
   * Permutated Congruential Generator (PCG) family.
   *
   * @param x the input value
   * @return the output value
   * @see #rxsmxs(long)
   */
  public static long rxsmxsUnmix(long x) {
    final long word = ((x >>> 43) ^ x) * RXSMXS_UNMULTIPLIER;
    return unxorshift(word, ((int) (word >>> 59)) + 5);
  }

  /**
   * Perform the 64-bit R-R-M-X-M-X (Rotate: Rotate; Multiply; Xor shift; Multiply; Xor shift) mix
   * function of Pelle Evensen.
   *
   * @param x the input value
   * @return the output value
   * @see <a
   *      href="https://mostlymangling.blogspot.com/2018/07/on-mixing-functions-in-fast-splittable.html">
   *      On the mixing functions in "Fast Splittable Pseudorandom Number Generators", MurmurHash3
   *      and David Stafford&#39;s improved variants on the MurmurHash3 finalizer.</a>
   */
  public static long rrmxmx(long x) {
    x ^= Long.rotateRight(x, 49) ^ Long.rotateRight(x, 24);
    x *= 0x9fb21c651e98df25L;
    x ^= x >>> 28;
    x *= 0x9fb21c651e98df25L;
    return x ^ x >>> 28;
  }

  /**
   * Reverse the 64-bit R-R-M-X-M-X (Rotate: Rotate; Multiply; Xor shift; Multiply; Xor shift) mix
   * function of Pelle Evensen.
   *
   * @param value the input value
   * @return the output value
   * @see #rrmxmx(long)
   */
  public static long rrmxmxUnmix(long value) {
    long result = unxorshift28(value) * 0x2AB9C720D1024ADL;
    result = unxorshift28(result) * 0x2AB9C720D1024ADL;
    return invertRor2449(result);
  }

  /**
   * Invert a 28-bit right xorshift operation.
   *
   * @param value the value
   * @return the the original value
   */
  private static long unxorshift28(long value) {
    return value ^ (value >>> 28) ^ (value >>> 56);
  }

  /**
   * Invert the rotate(24) ^ rotate(49) operation.
   *
   * @param value the value
   * @return the the original value
   */
  private static long invertRor2449(long value) {
    long acc = 0;
    for (int r : rots) {
      acc ^= Long.rotateRight(value, r);
    }
    return acc ^ value;
  }

  /**
   * Perform the 64-bit R-R-X-M-R-R-X-M-S-X (Rotate: Rotate; Xor; Multiply; Rotate: Rotate; Xor;
   * Multiply; Shift; Xor) mix function of Pelle Evensen.
   *
   * <p>This mixer is slower than {@link #rrmxmx(long)} due to an additional pair of rotate
   * operations. It has better mixing properties.
   *
   * @param x the input value
   * @return the output value
   * @see <a
   *      href="http://mostlymangling.blogspot.com/2019/01/better-stronger-mixer-and-test-procedure.html">
   *      Better, stronger mixer and a test procedure.</a>
   */
  public static long rrxmrrxmsx0(long x) {
    x ^= Long.rotateRight(x, 25) ^ Long.rotateRight(x, 50);
    x *= 0xa24baed4963ee407L;
    x ^= Long.rotateRight(x, 24) ^ Long.rotateRight(x, 49);
    x *= 0x9fb21c651e98df25L;
    return x ^ x >>> 28;
  }

  /**
   * Perform the finalising mix function of Austin Appleby's MurmurHash3.
   *
   * @param x the input value
   * @return the output value
   * @see <a href="http://code.google.com/p/smhasher/">SMHasher</a>
   */
  public static long murmur3(long x) {
    x = (x ^ (x >>> 33)) * 0xff51afd7ed558ccdL;
    x = (x ^ (x >>> 33)) * 0xc4ceb9fe1a85ec53L;
    return x ^ (x >>> 33);
  }

  /**
   * Perform variant 13 of David Stafford's 64-bit mix function.
   *
   * @param x the input value
   * @return the output value
   * @see <a href="http://zimbry.blogspot.com/2011/09/better-bit-mixing-improving-on.html">Better
   *      Bit Mixing - Improving on MurmurHash3&#39;s 64-bit Finalizer.</a>
   */
  public static long stafford13(long x) {
    x = (x ^ (x >>> 30)) * 0xbf58476d1ce4e5b9L;
    x = (x ^ (x >>> 27)) * 0x94d049bb133111ebL;
    return x ^ (x >>> 31);
  }
}
