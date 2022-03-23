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
 * Copyright (C) 2011 - 2022 Alex Herbert
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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackInputStream;
import java.io.Reader;

/**
 * Class to automatically detect and open the input stream using the correct file encoding.
 *
 * <p>Extracted from the Google Data API.
 *
 * @see <a
 *      href="http://stackoverflow.com/questions/1835430/byte-order-mark-screws-up-file-reading-in-java">
 *      Stack Overflow: Byte order mark screws up file reading in java</a>
 */
public class UnicodeReader extends Reader {
  /** The size of the byte-order-mark (BOM). */
  private static final int BOM_SIZE = 4;
  /** A byte that is not present in any BOM. */
  private static final byte IGNORE = (byte) 0xAA;

  /** The reader. */
  private final InputStreamReader reader;

  /**
   * Construct UnicodeReader.
   *
   * @param in Input stream.
   * @param defaultEncoding Default encoding to be used if BOM is not found, or {@code null} to
   *        use system default encoding.
   * @throws IOException If an I/O error occurs.
   */
  public UnicodeReader(InputStream in, String defaultEncoding) throws IOException {
    // Avoid zero fill and any byte we are trying to match
    final byte[] bom = {IGNORE, IGNORE, IGNORE, IGNORE};
    String encoding;
    int unread;
    final PushbackInputStream pushbackStream = new PushbackInputStream(in, BOM_SIZE);
    final int read = pushbackStream.read(bom, 0, bom.length);

    // Read ahead four bytes and check for BOM marks.
    if ((bom[0] == (byte) 0xEF) && (bom[1] == (byte) 0xBB) && (bom[2] == (byte) 0xBF)) {
      encoding = "UTF-8";
      unread = read - 3;
    } else if ((bom[0] == (byte) 0xFE) && (bom[1] == (byte) 0xFF)) {
      encoding = "UTF-16BE";
      unread = read - 2;
    } else if ((bom[0] == (byte) 0xFF) && (bom[1] == (byte) 0xFE)) {
      if ((bom[2] == (byte) 0x00) && (bom[3] == (byte) 0x00)) {
        encoding = "UTF-32LE";
        unread = read - 4;
      } else {
        encoding = "UTF-16LE";
        unread = read - 2;
      }
    } else if ((bom[0] == (byte) 0x00) && (bom[1] == (byte) 0x00) && (bom[2] == (byte) 0xFE)
        && (bom[3] == (byte) 0xFF)) {
      encoding = "UTF-32BE";
      unread = read - 4;
    } else {
      encoding = defaultEncoding;
      unread = read;
    }

    // Unread bytes if necessary and skip BOM marks.
    if (unread > 0) {
      pushbackStream.unread(bom, (read - unread), unread);
    }

    // Use given encoding.
    if (encoding == null) {
      reader = new InputStreamReader(pushbackStream);
    } else {
      reader = new InputStreamReader(pushbackStream, encoding);
    }
  }

  /**
   * Gets the encoding.
   *
   * @return the encoding
   */
  public String getEncoding() {
    return reader.getEncoding();
  }

  @Override
  public int read(char[] cbuf, int off, int len) throws IOException {
    return reader.read(cbuf, off, len);
  }

  @Override
  public void close() throws IOException {
    reader.close();
  }
}
