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

package uk.ac.sussex.gdsc.core.ij.io;

import java.io.IOException;

/**
 * This class uses a byte array to store an entire seekable stream in memory.
 */
public final class ByteArraySeekableStream extends SeekableStream {
  /** The current position in the byte array. */
  int position;

  /** The buffer of bytes. */
  byte[] buffer;

  /** The length of the byte array. */
  final int length;

  /**
   * Instantiates a new byte array seekable stream.
   *
   * @param bytes the bytes
   */
  ByteArraySeekableStream(byte[] bytes) {
    if (bytes == null) {
      throw new NullPointerException();
    }
    this.buffer = bytes;
    length = bytes.length;
  }

  /**
   * Create a new byte array seekable stream wrapping the provided data.
   *
   * @param bytes the bytes
   * @return the byte array seekable stream
   */
  public static ByteArraySeekableStream wrap(byte[] bytes) {
    return new ByteArraySeekableStream(bytes);
  }

  @Override
  public long getFilePointer() {
    return position;
  }

  @Override
  public int read() {
    if (position < length) {
      return buffer[position++] & 0xff;
    }
    return -1;
  }

  @Override
  public int read(byte[] bytes, int off, int len) {
    if (position < length) {
      if (len > 0) {
        final int size = (position + len <= length) ? len : length - position;
        System.arraycopy(buffer, position, bytes, off, size);
        position += size;
        return size;
      }
      return 0;
    }
    return -1;
  }

  @Override
  public void seek(long loc) throws IOException {
    if (loc < 0) {
      throw new IOException("Negative position");
    }
    // Allow seek to the end
    position = (loc > length) ? length : (int) loc;
  }

  @Override
  public void close() {
    // Do nothing
  }

  @Override
  public long skip(long n) {
    if (n <= 0) {
      return 0;
    }
    final int pos = position;
    final long newpos = pos + n;
    if (newpos > length || newpos < 0) {
      position = length;
    } else {
      position = (int) newpos;
    }

    /* return the actual number of bytes skipped */
    return (long) position - pos;
  }

  @Override
  public int available() {
    return length - position;
  }

  @Override
  public boolean canCopy() {
    return true;
  }

  /**
   * Copy the stream reusing the underlying byte buffer.
   *
   * @return the byte array seekable stream
   */
  @Override
  public ByteArraySeekableStream copy() {
    return new ByteArraySeekableStream(buffer);
  }
}
