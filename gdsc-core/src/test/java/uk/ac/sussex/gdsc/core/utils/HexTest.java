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
 * Copyright (C) 2011 - 2025 Alex Herbert
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

package uk.ac.sussex.gdsc.core.utils;

import java.nio.CharBuffer;
import java.util.Arrays;
import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.rng.RngFactory;
import uk.ac.sussex.gdsc.test.utils.RandomSeed;

@SuppressWarnings("javadoc")
class HexTest {

  @Test
  void testEncodeWithBadInput() {
    final char[] empty = {};
    Assertions.assertArrayEquals(empty, Hex.encode(null), "Null input");
    Assertions.assertArrayEquals(empty, Hex.encode(new byte[0]), "Empty input");
  }

  @Test
  void testDecodeWithBadInput() {
    final byte[] empty = new byte[0];
    Assertions.assertArrayEquals(empty, Hex.decode(null), "Null input");
    Assertions.assertArrayEquals(empty, Hex.decode(""), "Empty input");
    Assertions.assertArrayEquals(empty, Hex.decode("j"), "Bad single character");
    Assertions.assertArrayEquals(empty, Hex.decode("/0123456789ABCDEFabcdef"), "Bad char");
    Assertions.assertArrayEquals(empty, Hex.decode("0123456789:ABCDEFabcdef"), "Bar char");
    Assertions.assertArrayEquals(empty, Hex.decode("0123456789@ABCDEFabcdef"), "Bar char");
    Assertions.assertArrayEquals(empty, Hex.decode("0123456789ABCDEFGabcdef"), "Bar char");
    Assertions.assertArrayEquals(empty, Hex.decode("0123456789ABCDEF`abcdef"), "Bar char");
    Assertions.assertArrayEquals(empty, Hex.decode("0123456789ABCDEFabcdefg"), "Bar char");
  }

  @SeededTest
  void testEncode(RandomSeed seed) {
    final UniformRandomProvider rng = RngFactory.create(seed.get());
    final boolean toLowerCase = true;
    for (int i = 1; i < 20; i++) {
      final byte[] bytes = new byte[i];
      for (int j = 0; j < 5; j++) {
        rng.nextBytes(bytes);
        final String expected =
            org.apache.commons.codec.binary.Hex.encodeHexString(bytes, toLowerCase);
        final String actual = new String(Hex.encode(bytes));
        Assertions.assertEquals(expected, actual, "Bad encoding");
      }
    }
  }

  @SeededTest
  void testDecode(RandomSeed seed) {
    final UniformRandomProvider rng = RngFactory.create(seed.get());
    final boolean toLowerCase = true;
    // Output Hex characters.
    final char[] hexDigits =
        {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    for (int i = 1; i < 20; i++) {
      final byte[] bytes = new byte[i];
      for (int j = 0; j < 5; j++) {
        rng.nextBytes(bytes);
        final String hex = org.apache.commons.codec.binary.Hex.encodeHexString(bytes, toLowerCase);
        final byte[] actual = Hex.decode(hex);
        Assertions.assertArrayEquals(bytes, actual, "Bad decoding");

        // Test with odd length string. It should be the same as if it had a '0' on the end.
        final StringBuilder sb = new StringBuilder(hex);
        sb.append(hexDigits[rng.nextInt(16)]);

        final byte[] padded = Hex.decode(sb);
        sb.append('0');
        final byte[] unpadded = Hex.decode(sb);
        Assertions.assertArrayEquals(padded, unpadded, "Bad padding");

        // Check against original
        final byte[] clipped = Arrays.copyOf(padded, bytes.length);
        Assertions.assertArrayEquals(bytes, clipped, "Bad decoding after padding");

        // Test decode CharBuffer
        final CharBuffer buf = CharBuffer.wrap(hex.toCharArray());
        Assertions.assertArrayEquals(bytes, Hex.decode(buf), "Bad decoding CharBuffer");
      }
    }
  }

  @Test
  void testIsHex() {
    for (int c = '0'; c <= '9'; c++) {
      Assertions.assertTrue(Hex.isHex((char) c));
    }
    for (int c = 'A'; c <= 'F'; c++) {
      Assertions.assertTrue(Hex.isHex((char) c));
    }
    for (int c = 'a'; c <= 'f'; c++) {
      Assertions.assertTrue(Hex.isHex((char) c));
    }
    Assertions.assertFalse(Hex.isHex((char) ('0' - 1)));
    Assertions.assertFalse(Hex.isHex((char) ('9' + 1)));
    Assertions.assertFalse(Hex.isHex((char) ('A' - 1)));
    Assertions.assertFalse(Hex.isHex((char) ('F' + 1)));
    Assertions.assertFalse(Hex.isHex((char) ('a' - 1)));
    Assertions.assertFalse(Hex.isHex((char) ('f' + 1)));
  }
}
