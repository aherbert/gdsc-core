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

package uk.ac.sussex.gdsc.core.ij.io;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Base class implementing functionality to seek within a stream.
 */
public abstract class SeekableStream extends InputStream {
  /**
   * Gets the offset in this stream.
   *
   * @return the offset from the beginning of the stream (in bytes) at which the next read occurs
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public abstract long getFilePointer() throws IOException;

  @Override
  public abstract int read() throws IOException;

  @Override
  public abstract int read(byte[] bytes, int off, int len) throws IOException;

  /**
   * Read the full length of the buffer into the byte buffer.
   *
   * @param bytes the buffer into which the data is read
   * @throws EOFException if this input stream reaches the end before reading all the bytes.
   * @throws IOException if the stream has been closed and the contained input stream does not
   *         support reading after close, or another I/O error occurs.
   */
  public final void readFully(byte[] bytes) throws IOException {
    readFullyInternal(bytes, 0, bytes.length);
  }

  /**
   * Read the set length into the byte buffer.
   *
   * @param bytes the buffer into which the data is read
   * @param len the number of bytes to read.
   * @throws EOFException if this input stream reaches the end before reading all the bytes.
   * @throws IOException if the stream has been closed and the contained input stream does not
   *         support reading after close, or another I/O error occurs.
   */
  public final void readFully(byte[] bytes, int len) throws IOException {
    if (len < 0) {
      throw new IndexOutOfBoundsException();
    }
    readFullyInternal(bytes, 0, len);
  }

  /**
   * Read the set length into the byte buffer.
   *
   * @param bytes the buffer into which the data is read
   * @param off the start offset of the data.
   * @param len the number of bytes to read.
   * @throws EOFException if this input stream reaches the end before reading all the bytes.
   * @throws IOException if the stream has been closed and the contained input stream does not
   *         support reading after close, or another I/O error occurs.
   */
  public final void readFully(byte[] bytes, int off, int len) throws IOException {
    if (len < 0) {
      throw new IndexOutOfBoundsException();
    }
    readFullyInternal(bytes, off, len);
  }

  /**
   * Read the set length into the byte buffer.
   *
   * @param bytes the buffer into which the data is read
   * @param off the start offset of the data.
   * @param len the number of bytes to read.
   * @throws EOFException if this input stream reaches the end before reading all the bytes.
   * @throws IOException if the stream has been closed and the contained input stream does not
   *         support reading after close, or another I/O error occurs.
   */
  private void readFullyInternal(byte[] bytes, int off, int len) throws IOException {
    int bytesRead = 0;
    while (bytesRead < len) {
      final int count = read(bytes, off + bytesRead, len - bytesRead);
      if (count < 0) {
        throw new EOFException();
      }
      bytesRead += count;
    }
  }

  /**
   * Read the full length of the buffer into the byte buffer.
   *
   * <p>If this input stream reaches the end before reading all the bytes then the number of bytes
   * read is returned.
   *
   * @param bytes the buffer into which the data is read
   * @return the number of bytes read
   * @throws IOException if the stream has been closed and the contained input stream does not
   *         support reading after close, or another I/O error occurs.
   */
  public final int readBytes(byte[] bytes) throws IOException {
    return readBytesInternal(bytes, 0, bytes.length);
  }

  /**
   * Read the set length into the byte buffer.
   *
   * <p>If this input stream reaches the end before reading all the bytes then the number of bytes
   * read is returned.
   *
   * @param bytes the buffer into which the data is read
   * @param len the number of bytes to read.
   * @return the number of bytes read
   * @throws IOException if the stream has been closed and the contained input stream does not
   *         support reading after close, or another I/O error occurs.
   */
  public final int readBytes(byte[] bytes, int len) throws IOException {
    if (len < 0) {
      throw new IndexOutOfBoundsException();
    }
    return readBytesInternal(bytes, 0, len);
  }

  /**
   * Read the set length into the byte buffer.
   *
   * <p>If this input stream reaches the end before reading all the bytes then the number of bytes
   * read is returned.
   *
   * @param bytes the buffer into which the data is read
   * @param off the start offset of the data.
   * @param len the number of bytes to read.
   * @return the number of bytes read
   * @throws IOException if the stream has been closed and the contained input stream does not
   *         support reading after close, or another I/O error occurs.
   */
  public final int readBytes(byte[] bytes, int off, int len) throws IOException {
    if (len < 0) {
      throw new IndexOutOfBoundsException();
    }
    return readBytesInternal(bytes, off, len);
  }

  /**
   * Read the set length into the byte buffer.
   *
   * <p>If this input stream reaches the end before reading all the bytes then the number of bytes
   * read is returned.
   *
   * @param bytes the buffer into which the data is read
   * @param off the start offset of the data.
   * @param len the number of bytes to read.
   * @return the number of bytes read
   * @throws IOException if the stream has been closed and the contained input stream does not
   *         support reading after close, or another I/O error occurs.
   */
  private int readBytesInternal(byte[] bytes, int off, int len) throws IOException {
    int bytesRead = 0;
    while (bytesRead < len) {
      final int count = read(bytes, off + bytesRead, len - bytesRead);
      if (count < 0) {
        break;
      }
      bytesRead += count;
    }
    return bytesRead;
  }

  /**
   * Seek to a position in the stream.
   *
   * @param loc the location
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public abstract void seek(long loc) throws IOException;

  /**
   * Seek to a position in the stream.
   *
   * @param loc the location (used as an unsigned int)
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public void seek(int loc) throws IOException {
    seek((loc) & 0xffffffffL);
  }

  @Override
  public abstract void close() throws IOException;

  /**
   * Check if this resource can be copied.
   *
   * @return true, if this resource can be copied
   */
  public boolean canCopy() {
    return false;
  }

  /**
   * Copy the resource for reading. It will be set to position 0.
   *
   * @return the copy
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public SeekableStream copy() throws IOException {
    throw new IOException("Cannot copy");
  }
}
