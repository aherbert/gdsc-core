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

import java.util.Objects;
import java.util.function.Supplier;
import org.apache.commons.rng.UniformRandomProvider;

/**
 * Class for generating random strings using a given radix, e.g. hex strings.
 *
 * <p>Currently supports a radix of 2 to 64 inclusive using the MIME Base64 table rearranged to
 * [0-9, A-Z, a-z, +, /]. E.g. hex strings (radix 16) will be upper-case; radix 36 will return a
 * valid string for character conversion as defined by {@link Character#MAX_RADIX}.
 *
 * <p>Specialised fast algorithms are provided for a radix of 2 (binary string), 8 (octal string),
 * 16 (hex string) and 64.
 */
public class RadixStringSampler {

  /**
   * The lookup table for sampling [0-9, A-Z, a-z, +, /].
   *
   * <p>This table is based on the "Base64 Alphabet" as specified in Table 1 of RFC 2045.
   *
   * <p>The table has been rearranged from the MIME specification for encoding [A-Z, a-z, 0-9, +, /]
   * to [0-9, A-Z, a-z, +, /] so that it can be used for random samples of base 2 upwards and will
   * produce a valid string up to {@link Character#MAX_RADIX} and then beyond to base 64.
   *
   * <p>Rearranging the table is allowed since it is not encoding (where a strict alphabet has a
   * defined byte interpretation for each character) but random sampling.
   *
   * @see <a href="http://www.ietf.org/rfc/rfc2045.txt">RFC 2045</a>
   */
  //@formatter:off
  static final char[] TABLE64 = {
          '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
          'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
          'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
          'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
          'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
          '+', '/'
  };
  //@formatter:on

  /** Generator of uniformly distributed random numbers. */
  private final UniformRandomProvider rng;
  /** The length of the output string. */
  private final int length;
  /** The radix for the output. */
  private final int radix;
  /** The supplier for generating the random string. */
  private final Supplier<String> supplier;

  /**
   * Creates a generator of strings of the given length.
   *
   * <p>Currently supports a radix of 2 to 64 inclusive.
   *
   * <p>Fast algorithms are provided for a radix of 2 (binary string), 8 (octal string), 16 (hex
   * string) and 64.
   *
   * <p>Uses a character order of [0-9, A-Z, a-z, +, /].
   *
   * @param rng Generator of uniformly distributed random numbers.
   * @param length The length.
   * @param radix The radix for the output.
   * @throws IllegalArgumentException If {@code length <= 0} or if {@code radix} is not supported.
   * @throws NullPointerException If {@code rng} is null
   */
  public RadixStringSampler(UniformRandomProvider rng, int length, int radix) {
    if (length <= 0) {
      throw new IllegalArgumentException(length + " <= 0");
    }
    checkRadix(radix);
    this.rng = Objects.requireNonNull(rng, "Random generator must not be null");
    this.length = length;
    this.radix = radix;
    this.supplier = createSupplier(radix, length);
  }

  /**
   * Check the radix is supported.
   *
   * @param radix The radix for the output.
   * @throws IllegalArgumentException If {@code radix} is not supported.
   */
  private static void checkRadix(int radix) {
    if (radix < 2 || radix > 64) {
      throw new IllegalArgumentException("Unsupported radix: " + radix);
    }
  }

  /**
   * Creates the supplier that generates the random string.
   *
   * @param radix The radix for the output.
   * @param length The length.
   * @return the supplier
   * @throws IllegalArgumentException If {@code radix} is not supported.
   */
  private Supplier<String> createSupplier(int radix, int length) {
    final char[] out = new char[length];
    switch (radix) {
      case 64:
        return () -> nextBase64String(rng, out);
      case 16:
        return () -> nextHexString(rng, out);
      case 8:
        return () -> nextOctalString(rng, out);
      case 2:
        return () -> nextBinaryString(rng, out);
      default:
        return () -> nextString(rng, out, radix);
    }
  }

  /**
   * Gets the length of the sample string.
   *
   * @return the length
   */
  public int getLength() {
    return length;
  }

  /**
   * Gets the radix of the sample string.
   *
   * @return the radix
   */
  public int getRadix() {
    return radix;
  }

  /**
   * Generate a random sample string.
   *
   * @return A random string.
   */
  public String sample() {
    return supplier.get();
  }

  /**
   * Generate a random Base64 string of the given length.
   *
   * <p>The string uses MIME's Base64 table (A-Z, a-z, 0-9, +, /).
   *
   * <p>This is a fast method that uses 3 calls to {@link UniformRandomProvider#nextInt()} for 16
   * characters.
   *
   * @param rng Generator of uniformly distributed random numbers.
   * @param length The length.
   * @return A random Base64 string.
   * @throws NegativeArraySizeException If length is negative
   * @see <a href="http://www.ietf.org/rfc/rfc2045.txt">RFC 2045</a>
   */
  public static String nextBase64String(UniformRandomProvider rng, int length) {
    return nextBase64String(rng, new char[length]);
  }

  /**
   * Generate a random Base64 string of the given length.
   *
   * <p>The string uses MIME's Base64 table (A-Z, a-z, 0-9, +, /).
   *
   * @param rng Generator of uniformly distributed random numbers.
   * @param out The output buffer.
   * @return A random Base64 string.
   * @see <a href="http://www.ietf.org/rfc/rfc2045.txt">RFC 2045</a>
   */
  private static String nextBase64String(UniformRandomProvider rng, char[] out) {
    // Process blocks of 6 bits as an index in the range 0-63
    // for each base64 character.
    // There are 16 samples per 3 ints (16 * 6 = 3 * 32 = 96 bits).
    final int length = out.length;
    // Run the loop without checking index i by producing characters
    // up to the size below the desired length.
    int index = 0;
    for (int loopLimit = length / 16; loopLimit-- > 0;) {
      final int i1 = rng.nextInt();
      final int i2 = rng.nextInt();
      final int i3 = rng.nextInt();
      // 0x3F == 0x01 | 0x02 | 0x04 | 0x08 | 0x10 | 0x20
      // Extract 4 6-bit samples from the first 24 bits of each int
      out[index++] = TABLE64[(i1 >>> 18) & 0x3F];
      out[index++] = TABLE64[(i1 >>> 12) & 0x3F];
      out[index++] = TABLE64[(i1 >>> 6) & 0x3F];
      out[index++] = TABLE64[i1 & 0x3F];
      out[index++] = TABLE64[(i2 >>> 18) & 0x3F];
      out[index++] = TABLE64[(i2 >>> 12) & 0x3F];
      out[index++] = TABLE64[(i2 >>> 6) & 0x3F];
      out[index++] = TABLE64[i2 & 0x3F];
      out[index++] = TABLE64[(i3 >>> 18) & 0x3F];
      out[index++] = TABLE64[(i3 >>> 12) & 0x3F];
      out[index++] = TABLE64[(i3 >>> 6) & 0x3F];
      out[index++] = TABLE64[i3 & 0x3F];
      // Combine the remaining 8-bits from each int
      // to get 4 more samples
      final int i4 = (i1 >>> 24) | ((i2 >>> 24) << 8) | ((i3 >>> 24) << 16);
      out[index++] = TABLE64[(i4 >>> 18) & 0x3F];
      out[index++] = TABLE64[(i4 >>> 12) & 0x3F];
      out[index++] = TABLE64[(i4 >>> 6) & 0x3F];
      out[index++] = TABLE64[i4 & 0x3F];
    }
    // The final characters
    // For simplicity there are 5 samples per int (with two unused bits).
    while (index < length) {
      int i1 = rng.nextInt();
      out[index++] = TABLE64[i1 & 0x3F];
      for (int j = 0; j < 4 && index < length; j++) {
        i1 >>>= 6;
        out[index++] = TABLE64[i1 & 0x3F];
      }
    }
    return new String(out);
  }

  /**
   * Generate a random hex string of the given length.
   *
   * <p>The string uses a Base16 table (0-9, A-F), i.e. upper-case hex.
   *
   * <p>This is a fast method that uses 1 call to {@link UniformRandomProvider#nextInt()} for 8
   * characters.
   *
   * @param rng Generator of uniformly distributed random numbers.
   * @param length The length.
   * @return A random hex string.
   * @throws NegativeArraySizeException If length is negative
   */
  public static String nextHexString(UniformRandomProvider rng, int length) {
    return nextHexString(rng, new char[length]);
  }

  /**
   * Generate a random hex string to the of length of the output array.
   *
   * <p>The string uses a Base16 table (0-9, A-F), i.e. upper-case hex.
   *
   * @param rng Generator of uniformly distributed random numbers.
   * @param out The output buffer.
   * @return A random hex string.
   */
  private static String nextHexString(UniformRandomProvider rng, char[] out) {
    // Use the upper and lower 4 bits of each byte as an
    // index in the range 0-15 for each hex character.
    // There are 8 samples per int.
    final int length = out.length;
    // Run the loop without checking index i by producing characters
    // up to the size below the desired length.
    int index = 0;
    for (int loopLimit = length / 8; loopLimit-- > 0;) {
      final int i1 = rng.nextInt();
      // 0x0F == 0x01 | 0x02 | 0x04 | 0x08
      out[index++] = TABLE64[(i1 >>> 28) & 0x0F];
      out[index++] = TABLE64[(i1 >>> 24) & 0x0F];
      out[index++] = TABLE64[(i1 >>> 20) & 0x0F];
      out[index++] = TABLE64[(i1 >>> 16) & 0x0F];
      out[index++] = TABLE64[(i1 >>> 12) & 0x0F];
      out[index++] = TABLE64[(i1 >>> 8) & 0x0F];
      out[index++] = TABLE64[(i1 >>> 4) & 0x0F];
      out[index++] = TABLE64[i1 & 0x0F];
    }
    // The final characters
    if (index < length) {
      int i1 = rng.nextInt();
      out[index++] = TABLE64[i1 & 0x0F];
      while (index < length) {
        i1 >>>= 4;
        out[index++] = TABLE64[i1 & 0x0F];
      }
    }
    return new String(out);
  }

  /**
   * Generate a random octal string of the given length.
   *
   * <p>The string uses a Base8 table (0-7).
   *
   * @param rng Generator of uniformly distributed random numbers.
   * @param length The length.
   * @return A random octal string.
   * @throws NegativeArraySizeException If length is negative
   */
  public static String nextOctalString(UniformRandomProvider rng, int length) {
    return nextOctalString(rng, new char[length]);
  }

  /**
   * Generate a random octal string of the given length.
   *
   * <p>The string uses a Base8 table (0-7). .
   *
   * <p>This is a fast method that uses 1 call to {@link UniformRandomProvider#nextInt()} for 10
   * characters. 2 random bits are unused per {@code int} sample.
   *
   * @param rng Generator of uniformly distributed random numbers.
   * @param out The output buffer.
   * @return A random octal string.
   */
  private static String nextOctalString(UniformRandomProvider rng, char[] out) {
    // Process blocks of 3 bits as an index in the range 0-7
    // for each octal character.
    // There are 32 samples per 3 ints (32 * 3 = 3 * 32 = 96 bits).
    // For simplicity this is changed to 10 samples per int (with two unused bits).
    final int length = out.length;
    // Run the loop without checking index index by producing characters
    // up to the size below the desired length.
    int index = 0;
    for (int loopLimit = length / 10; loopLimit-- > 0;) {
      final int i1 = rng.nextInt();
      // 0x07 == 0x01 | 0x02 | 0x03
      out[index++] = TABLE64[(i1 >>> 27) & 0x07];
      out[index++] = TABLE64[(i1 >>> 24) & 0x07];
      out[index++] = TABLE64[(i1 >>> 21) & 0x07];
      out[index++] = TABLE64[(i1 >>> 18) & 0x07];
      out[index++] = TABLE64[(i1 >>> 15) & 0x07];
      out[index++] = TABLE64[(i1 >>> 12) & 0x07];
      out[index++] = TABLE64[(i1 >>> 9) & 0x07];
      out[index++] = TABLE64[(i1 >>> 6) & 0x07];
      out[index++] = TABLE64[(i1 >>> 3) & 0x07];
      out[index++] = TABLE64[i1 & 0x07];
    }
    // The final characters
    if (index < length) {
      int i1 = rng.nextInt();
      out[index++] = TABLE64[i1 & 0x07];
      while (index < length) {
        i1 >>>= 3;
        out[index++] = TABLE64[i1 & 0x07];
      }
    }
    return new String(out);
  }

  /**
   * Generate a random binary string of the given length.
   *
   * <p>The string uses a Base2 table (0-1). .
   *
   * <p>This is a fast method that uses 1 call to {@link UniformRandomProvider#nextInt()} for 32
   * characters.
   *
   * @param rng Generator of uniformly distributed random numbers.
   * @param length The length.
   * @return A random binary string.
   * @throws NegativeArraySizeException If length is negative
   */
  public static String nextBinaryString(UniformRandomProvider rng, int length) {
    return nextBinaryString(rng, new char[length]);
  }

  /**
   * Generate a random binary string of the given length.
   *
   * <p>The string uses a Base2 table (0-1).
   *
   * @param rng Generator of uniformly distributed random numbers.
   * @param out The output buffer.
   * @return A random binary string.
   */
  private static String nextBinaryString(UniformRandomProvider rng, char[] out) {
    // Process each bits as an index in the range 0-1
    // for each binary character.
    // There are 32 samples per int.
    final int length = out.length;
    // Run the loop without checking index index by producing characters
    // up to the size below the desired length.
    int index = 0;
    for (int loopLimit = length / 32; loopLimit-- > 0;) {
      final int i1 = rng.nextInt();
      out[index++] = TABLE64[(i1 >>> 31) & 0x01];
      out[index++] = TABLE64[(i1 >>> 30) & 0x01];
      out[index++] = TABLE64[(i1 >>> 29) & 0x01];
      out[index++] = TABLE64[(i1 >>> 28) & 0x01];
      out[index++] = TABLE64[(i1 >>> 27) & 0x01];
      out[index++] = TABLE64[(i1 >>> 26) & 0x01];
      out[index++] = TABLE64[(i1 >>> 25) & 0x01];
      out[index++] = TABLE64[(i1 >>> 24) & 0x01];
      out[index++] = TABLE64[(i1 >>> 23) & 0x01];
      out[index++] = TABLE64[(i1 >>> 22) & 0x01];
      out[index++] = TABLE64[(i1 >>> 21) & 0x01];
      out[index++] = TABLE64[(i1 >>> 20) & 0x01];
      out[index++] = TABLE64[(i1 >>> 19) & 0x01];
      out[index++] = TABLE64[(i1 >>> 18) & 0x01];
      out[index++] = TABLE64[(i1 >>> 17) & 0x01];
      out[index++] = TABLE64[(i1 >>> 16) & 0x01];
      out[index++] = TABLE64[(i1 >>> 15) & 0x01];
      out[index++] = TABLE64[(i1 >>> 14) & 0x01];
      out[index++] = TABLE64[(i1 >>> 13) & 0x01];
      out[index++] = TABLE64[(i1 >>> 12) & 0x01];
      out[index++] = TABLE64[(i1 >>> 11) & 0x01];
      out[index++] = TABLE64[(i1 >>> 10) & 0x01];
      out[index++] = TABLE64[(i1 >>> 9) & 0x01];
      out[index++] = TABLE64[(i1 >>> 8) & 0x01];
      out[index++] = TABLE64[(i1 >>> 7) & 0x01];
      out[index++] = TABLE64[(i1 >>> 6) & 0x01];
      out[index++] = TABLE64[(i1 >>> 5) & 0x01];
      out[index++] = TABLE64[(i1 >>> 4) & 0x01];
      out[index++] = TABLE64[(i1 >>> 3) & 0x01];
      out[index++] = TABLE64[(i1 >>> 2) & 0x01];
      out[index++] = TABLE64[(i1 >>> 1) & 0x01];
      out[index++] = TABLE64[i1 & 0x01];
    }
    // The final characters
    if (index < length) {
      int i1 = rng.nextInt();
      out[index++] = TABLE64[i1 & 0x01];
      while (index < length) {
        i1 >>>= 1;
        out[index++] = TABLE64[i1 & 0x01];
      }
    }
    return new String(out);
  }

  /**
   * Generate a random string of the given length using the supplied radix.
   *
   * <p>Currently supports a radix of 2 to 64 inclusive.
   *
   * <p>Uses a character order of [0-9, A-Z, a-z, +, /].
   *
   * <p>Fast algorithms are used for radix 2, 8, 16 and 64. The default is to use 1 call to
   * {@link UniformRandomProvider#nextInt(int)} per character.
   *
   * @param rng Generator of uniformly distributed random numbers.
   * @param length The length.
   * @param radix The radix.
   * @return A random string.
   * @throws NegativeArraySizeException If length is negative
   * @throws IllegalArgumentException If {@code radix} is not supported.
   */
  public static String nextString(UniformRandomProvider rng, int length, int radix) {
    checkRadix(radix);
    final char[] out = new char[length];
    switch (radix) {
      case 64:
        return nextBase64String(rng, out);
      case 16:
        return nextHexString(rng, out);
      case 8:
        return nextOctalString(rng, out);
      case 2:
        return nextBinaryString(rng, out);
      default:
        return nextString(rng, out, radix);
    }
  }

  /**
   * Generate a random string of the given length using the supplied radix.
   *
   * <p>Uses a character order of [0-9, A-Z, a-z, +, /].
   *
   * @param rng Generator of uniformly distributed random numbers.
   * @param out The output buffer.
   * @param radix The radix.
   * @return A random binary string.
   */
  private static String nextString(UniformRandomProvider rng, char[] out, int radix) {
    for (int i = 0; i < out.length; i++) {
      out[i] = TABLE64[rng.nextInt(radix)];
    }
    return new String(out);
  }
}
