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

import org.apache.commons.rng.UniformRandomProvider;

/**
 * Class for generating random strings.
 */
public class StringSampler {
    /**
     * Represents a supplier of results.
     * <p>
     * Included for backwards compatibility with older Java versions since
     * {@link java.util.function.Supplier} was introduced in 1.8.
     */
    private interface Supplier<T> {
        /**
         * Gets a result.
         *
         * @return a result
         */
        T get();
    }

    /**
     * Used to build output. Hex will be lower-case.
     */
    private static final char[] DIGITS_LOWER = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd',
            'e', 'f' };

    /** Generator of uniformly distributed random numbers. */
    private final UniformRandomProvider rng;
    /** The byte buffer for random samples. */
    private final byte[] bytes;
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
     */
    public StringSampler(UniformRandomProvider rng, int length, int radix) throws IllegalArgumentException {
        this.rng = rng;
        this.supplier = createSupplier(radix);
        this.bytes = createByteBuffer(length, radix);
        this.length = length;
    }

    /**
     * Creates the byte buffer used for the random sample.
     *
     * @param length The length.
     * @param radix  The radix for the output.
     * @return The byte buffer.
     * @throws IllegalArgumentException If {@code length <= 0} or if {@code radix}
     *                                  is not supported.
     */
    private static byte[] createByteBuffer(int length, int radix) throws IllegalArgumentException {
        if (length <= 0)
            throw new IllegalArgumentException(length + " <= 0");
        // Check the radix
        switch (radix) {
        case 16:
            // 16 = 2^4 => 4 bits per sample, or 2 samples per byte.
            // Get enough bytes to cover the length,
            return new byte[(int) Math.ceil(length / 2.0)];
        case 8:
            // 8 = 2^3 => 3 bits per sample, or 8/3 samples per byte.
            // This is not a rational number so they are combined using
            // 3 bytes for 8 samples.
            return new byte[(int) (3 * Math.ceil(length / 8.0))];
        case 2:
            // 2 = 2^1 -> 1 bit per sample, or 8 samples per byte.
            return new byte[(int) Math.ceil(length / 8.0)];
        default:
            throw new IllegalArgumentException("Unsupported radix: " + radix);
        }
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
                    return nextHexString(rng, bytes, length);
                }
            };
        case 8:
            return new Supplier<String>() {
                @Override
                public String get() {
                    return nextOctalString(rng, bytes, length);
                }
            };
        case 2:
            return new Supplier<String>() {
                @Override
                public String get() {
                    return nextBinaryString(rng, bytes, length);
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
     * @throws IllegalArgumentException If {@code length <= 0}
     */
    public static String nextHexString(UniformRandomProvider rng, int length) throws IllegalArgumentException {
        return nextHexString(rng, createByteBuffer(length, 16), length);
    }

    /**
     * Generate a random hex string of the given length.
     * <p>
     * No checks are made that the byte buffer is the appropriate size.
     *
     * @param rng    Generator of uniformly distributed random numbers.
     * @param bytes  The byte buffer.
     * @param length The length.
     * @return A random hex string.
     */
private static String nextHexString(UniformRandomProvider rng, byte[] bytes, int length) {
    rng.nextBytes(bytes);
    // Use the upper and lower 4 bits of each byte as an
    // index in the range 0-15 for each hex character.
    final char[] out = new char[length];
    // Run the loop without checking index j by producing characters
    // up to the size below the desired length.
    final int loopLimit = length / 2;
    int i = 0, j = 0;
    while (i < loopLimit) {
        final byte b = bytes[i];
        // 0x0F == 0x01 | 0x02 | 0x04 | 0x08
        out[j++] = DIGITS_LOWER[(b >>> 4) & 0x0F];
        out[j++] = DIGITS_LOWER[b & 0x0F];
        i++;
    }
    // The final character
    if (j < length)
        out[j++] = DIGITS_LOWER[(bytes[i] >>> 4) & 0x0F];
    return new String(out);
}

    /**
     * Generate a random octal string of the given length.
     *
     * @param rng    Generator of uniformly distributed random numbers.
     * @param length The length.
     * @return A random octal string.
     * @throws IllegalArgumentException If {@code length <= 0}
     */
    public static String nextOctalString(UniformRandomProvider rng, int length) throws IllegalArgumentException {
        return nextOctalString(rng, createByteBuffer(length, 8), length);
    }

    /**
     * Generate a random octal string of the given length.
     * <p>
     * No checks are made that the byte buffer is the appropriate size.
     *
     * @param rng    Generator of uniformly distributed random numbers.
     * @param bytes  The byte buffer.
     * @param length The length.
     * @return A random octal string.
     */
    private static String nextOctalString(UniformRandomProvider rng, byte[] bytes, int length) {
        rng.nextBytes(bytes);
        // Process blocks of 3 bytes, using 3 bits as an
        // index in the range 0-7 for each octal character.
        final char[] out = new char[length];
        // Run the loop without checking index j by
        // producing octal characters pairs up to the size
        // below the desired length.
        final int loopLimit = 3 * (length / 8);
        int i = 0, j = 0;
        while (i < loopLimit) {
            // Pack 3 bytes as an integer
            int value = (bytes[i] & 0xff) | ((bytes[i + 1] & 0xff) << 8) | ((bytes[i + 2] & 0xff) << 16);

            // 0x07 == 0x01 | 0x02 | 0x03
            out[j++] = DIGITS_LOWER[(value >>> 21) & 0x07];
            out[j++] = DIGITS_LOWER[(value >>> 18) & 0x07];
            out[j++] = DIGITS_LOWER[(value >>> 15) & 0x07];
            out[j++] = DIGITS_LOWER[(value >>> 12) & 0x07];
            out[j++] = DIGITS_LOWER[(value >>> 9) & 0x07];
            out[j++] = DIGITS_LOWER[(value >>> 6) & 0x07];
            out[j++] = DIGITS_LOWER[(value >>> 3) & 0x07];
            out[j++] = DIGITS_LOWER[value & 0x07];
            i += 3;
        }
        // The final characters
        if (j < length) {
            // Pack final 3 bytes as an integer
            int value = (bytes[i] & 0xff) | ((bytes[i + 1] & 0xff) << 8) | ((bytes[i + 2] & 0xff) << 16);

            out[j++] = DIGITS_LOWER[value & 0x07];
            while (j < length) {
                value >>>= 3;
                out[j++] = DIGITS_LOWER[value & 0x07];
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
     * @throws IllegalArgumentException If {@code length <= 0}
     */
    public static String nextBinaryString(UniformRandomProvider rng, int length) throws IllegalArgumentException {
        return nextBinaryString(rng, createByteBuffer(length, 2), length);
    }

    /**
     * Generate a random binary string of the given length.
     * <p>
     * No checks are made that the byte buffer is the appropriate size.
     *
     * @param rng    Generator of uniformly distributed random numbers.
     * @param bytes  The byte buffer.
     * @param length The length.
     * @return A random binary string.
     */
    private static String nextBinaryString(UniformRandomProvider rng, byte[] bytes, int length) {
        rng.nextBytes(bytes);
        // Use each bit for each binary character.
        final char[] out = new char[length];
        // Run the loop without checking index j by producing characters
        // up to the size below the desired length.
        final int loopLimit = length / 8;
        int i = 0, j = 0;
        while (i < loopLimit) {
            final byte b = bytes[i];
            out[j++] = DIGITS_LOWER[(b >>> 7) & 0x01];
            out[j++] = DIGITS_LOWER[(b >>> 6) & 0x01];
            out[j++] = DIGITS_LOWER[(b >>> 5) & 0x01];
            out[j++] = DIGITS_LOWER[(b >>> 4) & 0x01];
            out[j++] = DIGITS_LOWER[(b >>> 3) & 0x01];
            out[j++] = DIGITS_LOWER[(b >>> 2) & 0x01];
            out[j++] = DIGITS_LOWER[(b >>> 1) & 0x01];
            out[j++] = DIGITS_LOWER[b & 0x01];
            i++;
        }
        // The final characters
        if (j < length) {
            // This switches to use little endian first.
            // Bytes are random so this is fine.
            byte b = bytes[i];
            out[j++] = DIGITS_LOWER[b & 0x01];
            while (j < length) {
                b >>>= 1;
                out[j++] = DIGITS_LOWER[b & 0x01];
            }
        }
        return new String(out);
    }
}
