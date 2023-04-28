/*-
 * #%L
 * Genome Damage and Stability Centre Test Utilities
 *
 * Contains utilities for use with test frameworks.
 * %%
 * Copyright (C) 2018 - 2022 Alex Herbert
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package uk.ac.sussex.gdsc.core.utils;

import java.util.Arrays;
import java.util.function.Supplier;

/**
 * Contains utility functions for Hexidecimal (Hex) character encoding and decoding.
 */
public final class Hex {

  /** The empty string. */
  private static final char[] EMPTY_CHARS = new char[0];
  /** The empty byte array. */
  private static final byte[] EMPTY_BYTES = new byte[0];
  /** Output Hex characters. */
  private static final char[] HEX_DIGITS =
      {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
  /**
   * The byte used to indicate the character is unmapped. No ASCII character is above 127 so this
   * can use -1. Bad characters can be identified with a sign test.
   */
  private static final byte UNMAPPED = (byte) -1;

  /**
   * This array is a lookup table that translates hex characters to 4-bit byte numbers.
   */
  private static final byte[] DECODE_TABLE;

  static {
    // Lower case is after upper case
    DECODE_TABLE = new byte['f' + 1];
    Arrays.fill(DECODE_TABLE, UNMAPPED);
    int value = 0;
    for (int c = '0'; c <= '9'; c++) {
      DECODE_TABLE[c] = (byte) value++;
    }
    value = 10;
    for (int c = 'A'; c <= 'F'; c++) {
      DECODE_TABLE[c] = (byte) value++;
    }
    value = 10;
    for (int c = 'a'; c <= 'f'; c++) {
      DECODE_TABLE[c] = (byte) value++;
    }
  }

  /** No instances. */
  private Hex() {}

  /**
   * Encode the bytes as hex characters.
   *
   * <p>Output hex characters are [0-9a-f].
   *
   * <p>If the bytes are null then an empty array is returned.
   *
   * @param bytes the bytes
   * @return the hex characters
   */
  public static char[] encode(byte[] bytes) {
    // Safe for null input
    int len;
    if (bytes == null || (len = bytes.length) == 0) {
      return EMPTY_CHARS;
    }
    // Two hex characters per byte
    final char[] chars = new char[len << 1];
    for (int i = len; i-- != 0;) {
      chars[i << 1] = HEX_DIGITS[(bytes[i] & 0xf0) >>> 4];
      chars[(i << 1) + 1] = HEX_DIGITS[bytes[i] & 0xf];
    }
    return chars;
  }

  /**
   * Decode the hex char sequence into bytes. Hex characters are [0-9A-Fa-f].
   *
   * <p>If the sequence is not a valid hex character then an empty array is returned.
   *
   * <p>If the sequence is an odd length then the final hex character is assumed to be '0'.
   *
   * <p>Note: Decode a {@code char[]} using {@link java.nio.CharBuffer#wrap
   * CharBuffer.wrap(char[])}.
   *
   * @param chars the characters
   * @return the bytes
   */
  public static byte[] decode(CharSequence chars) {
    return decode(chars, () -> EMPTY_BYTES);
  }

  /**
   * Decode the hex char sequence into bytes. Hex characters are [0-9A-Fa-f].
   *
   * <p>If the sequence is not a valid hex character then the default value is returned.
   * This will raise a {@link NullPointerException} if the supplier is null.
   *
   * <p>If the sequence is an odd length then the final hex character is assumed to be '0'.
   *
   * <p>Note: Decode a {@code char[]} using {@link java.nio.CharBuffer#wrap
   * CharBuffer.wrap(char[])}.
   *
   * @param chars the characters
   * @param defaultValue the default value
   * @return the bytes
   */
  public static byte[] decode(CharSequence chars, Supplier<byte[]> defaultValue) {
    // Safe for null input
    int len;
    if (chars == null || (len = chars.length()) == 0) {
      return defaultValue.get();
    }

    // Convert: Two hex characters per byte
    final int length = len >> 1;
    // Allow extra odd characters.
    final byte[] decoded = new byte[length + (len & 0x1)];
    // Handle final odd character
    if ((len & 0x1) == 1) {
      final int ch1 = mapToHexNumber(chars.charAt(len - 1));
      if (ch1 < 0) {
        return defaultValue.get();
      }
      decoded[length] = (byte) (ch1 << 4);
    }

    // Process pairs
    for (int i = length; i-- != 0;) {
      final int ch1 = mapToHexNumber(chars.charAt(i << 1));
      final int ch2 = mapToHexNumber(chars.charAt((i << 1) + 1));
      if ((ch1 | ch2) < 0) {
        // Not valid so return empty
        return defaultValue.get();
      }
      decoded[i] = (byte) ((ch1 << 4) | ch2);
    }

    return decoded;
  }

  /**
   * Checks if the character is a hex number representation {@code [0-9A-Fa-f]}.
   *
   * @param ch the character
   * @return true if a hex number
   */
  public static boolean isHex(char ch) {
    return ch < DECODE_TABLE.length && DECODE_TABLE[ch] != UNMAPPED;
  }

  /**
   * Map the character to a hex number representation (0-f) or {@link #UNMAPPED}.
   *
   * <p>Note: The input integer is expected to be created from a char and thus should not be
   * negative.
   *
   * @param ch the character code
   * @return the hex number
   */
  private static byte mapToHexNumber(int ch) {
    return ch < DECODE_TABLE.length ? DECODE_TABLE[ch] : UNMAPPED;
  }
}
