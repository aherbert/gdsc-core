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
 * Copyright (C) 2011 - 2018 Alex Herbert
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
 * <p>
 * Currently supports a radix of 2 (binary string), 8 (octal string), 16 (hex
 * string) and 64 (using the MIME Base64 table: A-Z, a-z, 0-9, +, /).
 */
public class RadixStringSampler {

    /**
     * The lookup_table for Binary, Octal and Hex encoding. Hex will be uppercase.
     */
    //@formatter:off
    private static final char[] TABLE16 = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'A', 'B', 'C', 'D', 'E', 'F'
    };
    //@formatter:on

    /**
     * This array is a lookup table that translates 6-bit positive integer index
     * values into their "Base64 Alphabet" equivalents as specified in Table 1 of
     * RFC 2045.
     *
     * Adapted from org.apache.commons.codec.binary.Base64 to directly map to char
     * so avoiding using {@link String#String(byte[], java.nio.charset.Charset)} to
     * encode.
     */
    //@formatter:off
    static final char[] TABLE64 = {
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
            'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
            'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'
    };
    //@formatter:on    

    /** Generator of uniformly distributed random numbers. */
    private final UniformRandomProvider rng;
    /** The length of the output string. */
    private final int length;
    /** The supplier for generating the random string. */
    private final Supplier<String> supplier;

    /**
     * Creates a generator of strings of the given length.
     * <p>
     * Currently supports a radix of 2 (binary string), 8 (octal string), 16 (hex
     * string) and 64 (using the MIME Base64 table: A-Z, a-z, 0-9, +, /).
     *
     * @param rng    Generator of uniformly distributed random numbers.
     * @param length The length.
     * @param radix  The radix for the output.
     * @throws IllegalArgumentException If {@code length <= 0} or if {@code radix}
     *                                  is not supported.
     * @throws NullPointerException     If {@code rng} is null
     */
    public RadixStringSampler(UniformRandomProvider rng, int length, int radix) throws IllegalArgumentException {
        if (length <= 0)
            throw new IllegalArgumentException(length + " <= 0");
        this.rng = Objects.requireNonNull(rng);
        this.supplier = createSupplier(radix);
        this.length = length;
    }

    /**
     * Creates the supplier that generates the random string.
     *
     * @param radix The radix for the output.
     * @return the supplier
     * @throws IllegalArgumentException If {@code radix} is not supported.
     */
    private Supplier<String> createSupplier(int radix) throws IllegalArgumentException {
        switch (radix) {
        case 64:
            return new Supplier<String>() {
                @Override
                public String get() {
                    return nextBase64String(rng, length);
                }
            };
        case 16:
            return new Supplier<String>() {
                @Override
                public String get() {
                    return nextHexString(rng, length);
                }
            };
        case 8:
            return new Supplier<String>() {
                @Override
                public String get() {
                    return nextOctalString(rng, length);
                }
            };
        case 2:
            return new Supplier<String>() {
                @Override
                public String get() {
                    return nextBinaryString(rng, length);
                }
            };
        default:
            throw new IllegalArgumentException("Unsupported radix: " + radix);
        }
    }

    /**
     * @return A random string.
     */
    public String sample() {
        return supplier.get();
    }

    /**
     * Generate a random Base64 string of the given length.
     * <p>
     * The string uses MIME's Base64 table (A-Z, a-z, 0-9, +, /).
     *
     * @param rng    Generator of uniformly distributed random numbers.
     * @param length The length.
     * @return A random Base64 string.
     * @throws NegativeArraySizeException If length is negative
     * @see <a href="http://www.ietf.org/rfc/rfc2045.txt">RFC 2045</a>
     */
    public static String nextBase64String(UniformRandomProvider rng, int length) throws NegativeArraySizeException {
        // Process blocks of 6 bits as an index in the range 0-63
        // for each base64 character.
        // There are 16 samples per 3 ints (16 * 6 = 3 * 32 = 96 bits).
        final char[] out = new char[length];
        // Run the loop without checking index i by producing characters
        // up to the size below the desired length.
        int i = 0;
        for (int loopLimit = length / 16; loopLimit-- > 0;) {
            final int i1 = rng.nextInt();
            final int i2 = rng.nextInt();
            final int i3 = rng.nextInt();
            // 0x3F == 0x01 | 0x02 | 0x04 | 0x08 | 0x10 | 0x20
            // Extract 4 6-bit samples from the first 24 bits of each int
            out[i++] = TABLE64[(i1 >>> 18) & 0x3F];
            out[i++] = TABLE64[(i1 >>> 12) & 0x3F];
            out[i++] = TABLE64[(i1 >>> 6) & 0x3F];
            out[i++] = TABLE64[i1 & 0x3F];
            out[i++] = TABLE64[(i2 >>> 18) & 0x3F];
            out[i++] = TABLE64[(i2 >>> 12) & 0x3F];
            out[i++] = TABLE64[(i2 >>> 6) & 0x3F];
            out[i++] = TABLE64[i2 & 0x3F];
            out[i++] = TABLE64[(i3 >>> 18) & 0x3F];
            out[i++] = TABLE64[(i3 >>> 12) & 0x3F];
            out[i++] = TABLE64[(i3 >>> 6) & 0x3F];
            out[i++] = TABLE64[i3 & 0x3F];
            // Combine the remaining 8-bits from each int
            // to get 4 more samples
            final int i4 = (i1 >>> 24) | ((i2 >>> 24) << 8) | ((i3 >>> 24) << 16);
            out[i++] = TABLE64[(i4 >>> 18) & 0x3F];
            out[i++] = TABLE64[(i4 >>> 12) & 0x3F];
            out[i++] = TABLE64[(i4 >>> 6) & 0x3F];
            out[i++] = TABLE64[i4 & 0x3F];
        }
        // The final characters
        if (i < length) {
            // For simplicity there are 5 samples per int (with two unused bits).
            while (i < length) {
                int b = rng.nextInt();
                out[i++] = TABLE64[b & 0x3F];
                for (int j = 0; j < 4 && i < length; j++) {
                    b >>>= 6;
                    out[i++] = TABLE64[b & 0x3F];
                }
            }
        }
        return new String(out);
    }

    /**
     * Generate a random hex string of the given length.
     *
     * @param rng    Generator of uniformly distributed random numbers.
     * @param length The length.
     * @return A random hex string.
     * @throws NegativeArraySizeException If length is negative
     */
    public static String nextHexString(UniformRandomProvider rng, int length) throws NegativeArraySizeException {
        // Use the upper and lower 4 bits of each byte as an
        // index in the range 0-15 for each hex character.
        // There are 8 samples per int.
        final char[] out = new char[length];
        // Run the loop without checking index i by producing characters
        // up to the size below the desired length.
        int i = 0;
        for (int loopLimit = length / 8; loopLimit-- > 0;) {
            final int b = rng.nextInt();
            // 0x0F == 0x01 | 0x02 | 0x04 | 0x08
            out[i++] = TABLE16[(b >>> 28) & 0x0F];
            out[i++] = TABLE16[(b >>> 24) & 0x0F];
            out[i++] = TABLE16[(b >>> 20) & 0x0F];
            out[i++] = TABLE16[(b >>> 16) & 0x0F];
            out[i++] = TABLE16[(b >>> 12) & 0x0F];
            out[i++] = TABLE16[(b >>> 8) & 0x0F];
            out[i++] = TABLE16[(b >>> 4) & 0x0F];
            out[i++] = TABLE16[b & 0x0F];
        }
        // The final characters
        if (i < length) {
            int b = rng.nextInt();
            out[i++] = TABLE16[b & 0x0F];
            while (i < length) {
                b >>>= 4;
                out[i++] = TABLE16[b & 0x0F];
            }
        }
        return new String(out);
    }

    /**
     * Generate a random octal string of the given length.
     *
     * @param rng    Generator of uniformly distributed random numbers.
     * @param length The length.
     * @return A random octal string.
     * @throws NegativeArraySizeException If length is negative
     */
    public static String nextOctalString(UniformRandomProvider rng, int length) throws NegativeArraySizeException {
        // Process blocks of 3 bits as an index in the range 0-7
        // for each octal character.
        // There are 32 samples per 3 ints (32 * 3 = 3 * 32 = 96 bits).
        // For simplicity this is changed to 10 samples per int (with two unused bits).
        final char[] out = new char[length];
        // Run the loop without checking index i by producing characters
        // up to the size below the desired length.
        int i = 0;
        for (int loopLimit = length / 10; loopLimit-- > 0;) {
            final int b = rng.nextInt();
            // 0x07 == 0x01 | 0x02 | 0x03
            out[i++] = TABLE16[(b >>> 27) & 0x07];
            out[i++] = TABLE16[(b >>> 24) & 0x07];
            out[i++] = TABLE16[(b >>> 21) & 0x07];
            out[i++] = TABLE16[(b >>> 18) & 0x07];
            out[i++] = TABLE16[(b >>> 15) & 0x07];
            out[i++] = TABLE16[(b >>> 12) & 0x07];
            out[i++] = TABLE16[(b >>> 9) & 0x07];
            out[i++] = TABLE16[(b >>> 6) & 0x07];
            out[i++] = TABLE16[(b >>> 3) & 0x07];
            out[i++] = TABLE16[b & 0x07];
        }
        // The final characters
        if (i < length) {
            int b = rng.nextInt();
            out[i++] = TABLE16[b & 0x07];
            while (i < length) {
                b >>>= 3;
                out[i++] = TABLE16[b & 0x07];
            }
        }
        return new String(out);
    }

    /**
     * Generate a random binary string of the given length.
     *
     * @param rng    Generator of uniformly distributed random numbers.
     * @param length The length.
     * @return A random binary string.
     * @throws NegativeArraySizeException If length is negative
     */
    public static String nextBinaryString(UniformRandomProvider rng, int length) throws NegativeArraySizeException {
        // Process each bits as an index in the range 0-1
        // for each binary character.
        // There are 32 samples per int.
        final char[] out = new char[length];
        // Run the loop without checking index i by producing characters
        // up to the size below the desired length.
        int i = 0;
        for (int loopLimit = length / 32; loopLimit-- > 0;) {
            final int b = rng.nextInt();
            out[i++] = TABLE16[(b >>> 31) & 0x01];
            out[i++] = TABLE16[(b >>> 30) & 0x01];
            out[i++] = TABLE16[(b >>> 29) & 0x01];
            out[i++] = TABLE16[(b >>> 28) & 0x01];
            out[i++] = TABLE16[(b >>> 27) & 0x01];
            out[i++] = TABLE16[(b >>> 26) & 0x01];
            out[i++] = TABLE16[(b >>> 25) & 0x01];
            out[i++] = TABLE16[(b >>> 24) & 0x01];
            out[i++] = TABLE16[(b >>> 23) & 0x01];
            out[i++] = TABLE16[(b >>> 22) & 0x01];
            out[i++] = TABLE16[(b >>> 21) & 0x01];
            out[i++] = TABLE16[(b >>> 20) & 0x01];
            out[i++] = TABLE16[(b >>> 19) & 0x01];
            out[i++] = TABLE16[(b >>> 18) & 0x01];
            out[i++] = TABLE16[(b >>> 17) & 0x01];
            out[i++] = TABLE16[(b >>> 16) & 0x01];
            out[i++] = TABLE16[(b >>> 15) & 0x01];
            out[i++] = TABLE16[(b >>> 14) & 0x01];
            out[i++] = TABLE16[(b >>> 13) & 0x01];
            out[i++] = TABLE16[(b >>> 12) & 0x01];
            out[i++] = TABLE16[(b >>> 11) & 0x01];
            out[i++] = TABLE16[(b >>> 10) & 0x01];
            out[i++] = TABLE16[(b >>> 9) & 0x01];
            out[i++] = TABLE16[(b >>> 8) & 0x01];
            out[i++] = TABLE16[(b >>> 7) & 0x01];
            out[i++] = TABLE16[(b >>> 6) & 0x01];
            out[i++] = TABLE16[(b >>> 5) & 0x01];
            out[i++] = TABLE16[(b >>> 4) & 0x01];
            out[i++] = TABLE16[(b >>> 3) & 0x01];
            out[i++] = TABLE16[(b >>> 2) & 0x01];
            out[i++] = TABLE16[(b >>> 1) & 0x01];
            out[i++] = TABLE16[b & 0x01];
        }
        // The final characters
        if (i < length) {
            int b = rng.nextInt();
            out[i++] = TABLE16[b & 0x01];
            while (i < length) {
                b >>>= 1;
                out[i++] = TABLE16[b & 0x01];
            }
        }
        return new String(out);
    }
}
