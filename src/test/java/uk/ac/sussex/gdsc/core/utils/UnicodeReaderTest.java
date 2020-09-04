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

package uk.ac.sussex.gdsc.core.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"javadoc"})
class UnicodeReaderTest {

  private static final String TEST_STRING = "The quick brown fox jumped over the lazy dog";
  private static final Charset DEFAULT_CHARSET = Charset.defaultCharset();

  @Test
  void canCreateWithDefaultEncoding() throws IOException {
    final ByteArrayInputStream in = new ByteArrayInputStream(new byte[10]);
    try (UnicodeReader reader = new UnicodeReader(in, "ASCII")) {
      Assertions.assertEquals("ASCII", reader.getEncoding());
      final char[] cbuf = new char[13];
      Assertions.assertEquals(10, reader.read(cbuf, 0, cbuf.length));
      Assertions.assertEquals(-1, reader.read(cbuf, 0, cbuf.length));
    }
  }

  @Test
  void canCreateWithDefaultNullEncoding() throws IOException {
    final ByteArrayInputStream in = new ByteArrayInputStream(new byte[10]);
    try (UnicodeReader reader = new UnicodeReader(in, null)) {
      Assertions.assertEquals(DEFAULT_CHARSET, Charset.forName(reader.getEncoding()));
      final char[] cbuf = new char[13];
      Assertions.assertEquals(10, reader.read(cbuf, 0, cbuf.length));
      Assertions.assertEquals(-1, reader.read(cbuf, 0, cbuf.length));
    }
  }

  @Test
  void canCreateWithUtf8() throws IOException {
    assertReaderUsingBom(StandardCharsets.UTF_8, 3, 0xEF, 0xBB, 0xBF);
    assertReaderUsingBom(null, 0, 0xEF, 0xBB);
    assertReaderUsingBom(null, 0, 0xEF);
  }

  @Test
  void canCreateWithUtf16Be() throws IOException {
    assertReaderUsingBom(StandardCharsets.UTF_16BE, 2, 0xFE, 0xFF);
    assertReaderUsingBom(null, 0, 0xFE);
  }

  @Test
  void canCreateWithUtf16Le() throws IOException {
    assertReaderUsingBom(StandardCharsets.UTF_16LE, 2, 0xFF, 0xFE);
    assertReaderUsingBom(null, 0, 0xFF);
  }

  @Test
  void canCreateWithUtf32Le() throws IOException {
    assertReaderUsingBom(Charset.forName("UTF-32LE"), 4, 0xFF, 0xFE, 0x00, 0x00);
    assertReaderUsingBom(StandardCharsets.UTF_16LE, 2, 0xFF, 0xFE, 0x00, 0x01);
  }

  @Test
  void canCreateWithUtf32Be() throws IOException {
    assertReaderUsingBom(Charset.forName("UTF-32BE"), 4, 0x00, 0x00, 0xFE, 0xFF);
    assertReaderUsingBom(null, 0, 0x00, 0x00, 0xFE);
    assertReaderUsingBom(null, 0, 0x00, 0x00);
    assertReaderUsingBom(null, 0, 0x00);
  }

  private static void assertReaderUsingBom(Charset charset, int discard, int... bom)
      throws IOException {
    @SuppressWarnings("resource")
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    for (final int b : bom) {
      out.write(b);
    }
    if (charset == null) {
      out.write(TEST_STRING.getBytes());
    } else {
      out.write(TEST_STRING.getBytes(charset));
    }
    out.close();
    byte[] bytes = out.toByteArray();
    final ByteArrayInputStream in = new ByteArrayInputStream(bytes);
    try (UnicodeReader reader = new UnicodeReader(in, null)) {
      String expected;
      if (discard != 0) {
        bytes = Arrays.copyOfRange(bytes, discard, bytes.length);
      }
      if (charset == null) {
        Assertions.assertEquals(DEFAULT_CHARSET, Charset.forName(reader.getEncoding()));
        expected = new String(bytes);
      } else {
        Assertions.assertEquals(charset, Charset.forName(reader.getEncoding()));
        expected = new String(bytes, charset);
      }
      final char[] cbuf = new char[1 + out.size()];
      final int count = reader.read(cbuf, 0, cbuf.length);
      Assertions.assertEquals(expected, new String(cbuf, 0, count));
    }
  }

  @Test
  void canReadIncompleteBom() throws IOException {
    // This is missing the final 0x00 for UTF-32LE
    final byte[] bytes = {(byte) 0xFF, (byte) 0xFE, (byte) 0x00};
    final ByteArrayInputStream in = new ByteArrayInputStream(bytes);
    try (UnicodeReader reader = new UnicodeReader(in, null)) {
      Assertions.assertEquals(StandardCharsets.UTF_16LE, Charset.forName(reader.getEncoding()));
      final String expected = new String(new byte[1], StandardCharsets.UTF_16LE);
      final char[] cbuf = new char[1 + bytes.length];
      final int count = reader.read(cbuf, 0, cbuf.length);
      Assertions.assertEquals(expected, new String(cbuf, 0, count));
    }
  }
}
