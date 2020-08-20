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
   * Perform an inversion of a xor right-shift.
   *
   * <pre>
   * {@code
   * value = x ^ (x >>> shift)
   * }
   * </pre>
   *
   * <p>The shift value is not checked that it lies in the interval {@code [1, 63]}. If outside this
   * interval the results are undefined.
   *
   * @param value the value
   * @param shift the shift (must be in the interval {@code [1, 63]})
   * @return the inverted value (x)
   */
  public static long reverseXorRightShift(long value, int shift) {
    // @formatter:off
    // Note: a xor operation can be used to recover itself:
    // x ^ y = z
    // z ^ y = x
    // z ^ x = y
    //
    // During a xor right-shift of size n the top n-bits are unchanged.
    // Thus x ^ (x >>> shift):
    //
    //   |  a  |  b  |  c  |  d  |  e  |
    // ^ |     |  a  |  b  |  c  |  d  |
    // = |  a  | a^b | b^c | c^d | d^e |
    //
    // These known values can be used to recover the next set of n-bits.
    // This can be done recursively in doubling blocks until all are recovered.
    //
    // Reverse step 1:
    //   |  a  | a^b | b^c | c^d | d^e |
    // ^ |     |  a  | a^b | b^c | c^d |
    // = |  a  |  b  | a^c | b^d | c^e |
    //
    // Reverse step 2:
    //   |  a  |  b  | a^c | b^d | c^e |
    // ^ |     |     |  a  |  b  | a^c |
    // = |  a  |  b  |  c  |  d  | a^e |
    //
    // Reverse step 3:
    //   |  a  |  b  |  c  |  d  | a^e |
    // ^ |     |     |     |     |  a  |
    // = |  a  |  b  |  c  |  d  |  e  |
    //
    // @formatter:on

    // Initialise the recovered value. This will have the correct top 2n-bits set.
    long recovered = value ^ (value >>> shift);
    // Use an algorithm that requires the recovered bits to be xor'd in doubling steps.
    if (shift < 32) {
      recovered ^= (recovered >>> (shift << 1));
      if (shift < 16) {
        recovered ^= (recovered >>> (shift << 2));
        if (shift < 8) {
          recovered ^= (recovered >>> (shift << 3));
          if (shift < 4) {
            recovered ^= (recovered >>> (shift << 4));
            if (shift < 2) {
              recovered ^= (recovered >>> (shift << 5));
            }
          }
        }
      }
    }
    return recovered;
  }

  /**
   * Perform an inversion of a xor left-shift.
   *
   * <pre>
   * {@code
   * value = x ^ (x << shift)
     * }
   * </pre>
   *
   * <p>The shift value is not checked that it lies in the interval {@code [1, 63]}. If outside this
   * interval the results are undefined.
   *
   * @param value the value
   * @param shift the shift (must be in the interval {@code [1, 63]})
   * @return the inverted value (x)
   */
  public static long reverseXorLeftShift(long value, int shift) {
    // @formatter:off
    // Note: a xor operation can be used to recover itself:
    // x ^ y = z
    // z ^ y = x
    // z ^ x = y
    //
    // During a xor left-shift of size n the bottom n-bits are unchanged.
    // Thus x ^ (x << shift):
    //
    //   |  a  |  b  |  c  |  d  |  e  |
    // ^ |  b  |  c  |  d  |  e  |     |
    // = | a^b | b^c | c^d | d^e |  e  |
    //
    // These known values can be used to recover the next set of n-bits.
    // This can be done recursively in doubling blocks until all are recovered.
    //
    // Reverse step 1:
    //   | a^b | b^c | c^d | d^e |  e  |
    // ^ | b^c | c^d | d^e |  e  |     |
    // = | a^c | b^d | c^e |  d  |  e  |
    //
    // Reverse step 2:
    //   | a^c | b^d | c^e |  d  |  e  |
    //   | c^e |  d  |  e  |     |     |
    // = | a^e |  b  |  c  |  d  |  e  |
    //
    // Reverse step 3:
    //   | a^e |  b  |  c  |  d  |  e  |
    // ^ |  e  |     |     |     |     |
    // = |  a  |  b  |  c  |  d  |  e  |
    //
    // @formatter:on

    // Initialise the recovered value. This will have the correct bottom 2n-bits set.
    long recovered = value ^ (value << shift);
    // Use an algorithm that requires the recovered bits to be xor'd in doubling steps.
    if (shift < 32) {
      recovered ^= (recovered << (shift << 1));
      if (shift < 16) {
        recovered ^= (recovered << (shift << 2));
        if (shift < 8) {
          recovered ^= (recovered << (shift << 3));
          if (shift < 4) {
            recovered ^= (recovered << (shift << 4));
            if (shift < 2) {
              recovered ^= (recovered << (shift << 5));
            }
          }
        }
      }
    }
    return recovered;
  }

  /**
   * Perform the 64-bit RXS-M-XS (Random Xor Shift; Multiply; Xor Shift) mix function of the
   * Permuted Congruential Generator (PCG) family.
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
   * Reverse the 64-bit RXS-M-XS (Random Xor Shift; Multiply; Xor Shift) mix function of the
   * Permuted Congruential Generator (PCG) family.
   *
   * @param x the input value
   * @return the output value
   * @see #rxsmxs(long)
   */
  public static long rxsmxsUnmix(long x) {
    final long word = ((x >>> 43) ^ x) * RXSMXS_UNMULTIPLIER;
    return reverseXorRightShift(word, ((int) (word >>> 59)) + 5);
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
    long result = unxorshift28(value) * 0x2ab9c720d1024adL;
    result = unxorshift28(result) * 0x2ab9c720d1024adL;
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
    for (final int r : rots) {
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
   * Perform the finalising 32-bit mix function of Austin Appleby's MurmurHash3.
   *
   * @param x the input value
   * @return the output value
   * @see <a href="https://github.com/aappleby/smhasher">SMHasher</a>
   */
  public static int murmur3(int x) {
    x = (x ^ (x >>> 16)) * 0x85ebca6b;
    x = (x ^ (x >>> 13)) * 0xc2b2ae35;
    return x ^ (x >>> 16);
  }

  /**
   * Perform the finalising 64-bit mix function of Austin Appleby's MurmurHash3.
   *
   * @param x the input value
   * @return the output value
   * @see <a href="https://github.com/aappleby/smhasher">SMHasher</a>
   */
  public static long murmur3(long x) {
    return mix64(x, 33, 0xff51afd7ed558ccdL, 33, 0xc4ceb9fe1a85ec53L, 33);
  }

  /**
   * Perform variant 1 of David Stafford's 64-bit mix function.
   *
   * <p>This is ranked second of the top 14 Stafford mixers.
   *
   * @param x the input value
   * @return the output value
   * @see <a href="http://zimbry.blogspot.com/2011/09/better-bit-mixing-improving-on.html">Better
   *      Bit Mixing - Improving on MurmurHash3&#39;s 64-bit Finalizer.</a>
   */
  public static long stafford1(long x) {
    return mix64(x, 31, 0x7fb5d329728ea185L, 27, 0x81dadef4bc2dd44dL, 33);
  }

  /**
   * Perform variant 13 of David Stafford's 64-bit mix function.
   *
   * <p>This is ranked first of the top 14 Stafford mixers.
   *
   * @param x the input value
   * @return the output value
   * @see <a href="http://zimbry.blogspot.com/2011/09/better-bit-mixing-improving-on.html">Better
   *      Bit Mixing - Improving on MurmurHash3&#39;s 64-bit Finalizer.</a>
   */
  public static long stafford13(long x) {
    return mix64(x, 30, 0xbf58476d1ce4e5b9L, 27, 0x94d049bb133111ebL, 31);
  }

  /**
   * Perform a 64-bit mixing function consisting of alternating xor operations with a right-shifted
   * state and multiplications. This is based on the original 64-bit mix function of Austin
   * Appleby's MurmurHash3.
   *
   * @param x the state
   * @param shift1 the first shift
   * @param multiplier1 the first multiplier
   * @param shift2 the second shift
   * @param multiplier2 the second multiplier
   * @param shift3 the third shift
   * @return the long
   */
  private static long mix64(long x, int shift1, long multiplier1, int shift2, long multiplier2,
      int shift3) {
    x = (x ^ (x >>> shift1)) * multiplier1;
    x = (x ^ (x >>> shift2)) * multiplier2;
    return x ^ (x >>> shift3);
  }
}
