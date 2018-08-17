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
 */
public class RadixStringSampler {
    /**
     * Used to build output. Hex will be lower-case.
     */
    private static final char[] DIGITS_LOWER = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd',
            'e', 'f' };

    /** Generator of uniformly distributed random numbers. */
    private final UniformRandomProvider rng;
    /** The length of the output string. */
    private final int length;
    /** The supplier for generating the random string. */
    private final Supplier<String> supplier;

    /**
     * Creates a generator of strings of the given length.
     * <p>
     * Currently supports a radix of 2 (binary string), 8 (octal string) and 16 (hex
     * string).
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
            out[i++] = DIGITS_LOWER[(b >>> 28) & 0x0F];
            out[i++] = DIGITS_LOWER[(b >>> 24) & 0x0F];
            out[i++] = DIGITS_LOWER[(b >>> 20) & 0x0F];
            out[i++] = DIGITS_LOWER[(b >>> 16) & 0x0F];
            out[i++] = DIGITS_LOWER[(b >>> 12) & 0x0F];
            out[i++] = DIGITS_LOWER[(b >>> 8) & 0x0F];
            out[i++] = DIGITS_LOWER[(b >>> 4) & 0x0F];
            out[i++] = DIGITS_LOWER[b & 0x0F];
        }
        // The final characters
        if (i < length) {
            int b = rng.nextInt();
            out[i++] = DIGITS_LOWER[b & 0x0F];
            while (i < length) {
                b >>>= 4;
                out[i++] = DIGITS_LOWER[b & 0x0F];
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
        // There are 10 samples per int (with two unused bits).
        final char[] out = new char[length];
        // Run the loop without checking index i by producing characters
        // up to the size below the desired length.
        int i = 0;
        for (int loopLimit = length / 10; loopLimit-- > 0;) {
            final int b = rng.nextInt();
            // 0x07 == 0x01 | 0x02 | 0x03
            out[i++] = DIGITS_LOWER[(b >>> 27) & 0x07];
            out[i++] = DIGITS_LOWER[(b >>> 24) & 0x07];
            out[i++] = DIGITS_LOWER[(b >>> 21) & 0x07];
            out[i++] = DIGITS_LOWER[(b >>> 18) & 0x07];
            out[i++] = DIGITS_LOWER[(b >>> 15) & 0x07];
            out[i++] = DIGITS_LOWER[(b >>> 12) & 0x07];
            out[i++] = DIGITS_LOWER[(b >>> 9) & 0x07];
            out[i++] = DIGITS_LOWER[(b >>> 6) & 0x07];
            out[i++] = DIGITS_LOWER[(b >>> 3) & 0x07];
            out[i++] = DIGITS_LOWER[b & 0x07];
        }
        // The final characters
        if (i < length) {
            int b = rng.nextInt();
            out[i++] = DIGITS_LOWER[b & 0x07];
            while (i < length) {
                b >>>= 3;
                out[i++] = DIGITS_LOWER[b & 0x07];
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
            out[i++] = DIGITS_LOWER[(b >>> 31) & 0x01];
            out[i++] = DIGITS_LOWER[(b >>> 30) & 0x01];
            out[i++] = DIGITS_LOWER[(b >>> 29) & 0x01];
            out[i++] = DIGITS_LOWER[(b >>> 28) & 0x01];
            out[i++] = DIGITS_LOWER[(b >>> 27) & 0x01];
            out[i++] = DIGITS_LOWER[(b >>> 26) & 0x01];
            out[i++] = DIGITS_LOWER[(b >>> 25) & 0x01];
            out[i++] = DIGITS_LOWER[(b >>> 24) & 0x01];
            out[i++] = DIGITS_LOWER[(b >>> 23) & 0x01];
            out[i++] = DIGITS_LOWER[(b >>> 22) & 0x01];
            out[i++] = DIGITS_LOWER[(b >>> 21) & 0x01];
            out[i++] = DIGITS_LOWER[(b >>> 20) & 0x01];
            out[i++] = DIGITS_LOWER[(b >>> 19) & 0x01];
            out[i++] = DIGITS_LOWER[(b >>> 18) & 0x01];
            out[i++] = DIGITS_LOWER[(b >>> 17) & 0x01];
            out[i++] = DIGITS_LOWER[(b >>> 16) & 0x01];
            out[i++] = DIGITS_LOWER[(b >>> 15) & 0x01];
            out[i++] = DIGITS_LOWER[(b >>> 14) & 0x01];
            out[i++] = DIGITS_LOWER[(b >>> 13) & 0x01];
            out[i++] = DIGITS_LOWER[(b >>> 12) & 0x01];
            out[i++] = DIGITS_LOWER[(b >>> 11) & 0x01];
            out[i++] = DIGITS_LOWER[(b >>> 10) & 0x01];
            out[i++] = DIGITS_LOWER[(b >>> 9) & 0x01];
            out[i++] = DIGITS_LOWER[(b >>> 8) & 0x01];
            out[i++] = DIGITS_LOWER[(b >>> 7) & 0x01];
            out[i++] = DIGITS_LOWER[(b >>> 6) & 0x01];
            out[i++] = DIGITS_LOWER[(b >>> 5) & 0x01];
            out[i++] = DIGITS_LOWER[(b >>> 4) & 0x01];
            out[i++] = DIGITS_LOWER[(b >>> 3) & 0x01];
            out[i++] = DIGITS_LOWER[(b >>> 2) & 0x01];
            out[i++] = DIGITS_LOWER[(b >>> 1) & 0x01];
            out[i++] = DIGITS_LOWER[b & 0x01];
        }
        // The final characters
        if (i < length) {
            int b = rng.nextInt();
            out[i++] = DIGITS_LOWER[b & 0x01];
            while (i < length) {
                b >>>= 1;
                out[i++] = DIGITS_LOWER[b & 0x01];
            }
        }
        return new String(out);
    }
}
