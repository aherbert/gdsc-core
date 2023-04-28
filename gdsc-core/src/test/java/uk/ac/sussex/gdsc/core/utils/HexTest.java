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
