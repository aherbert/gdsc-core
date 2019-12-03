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
 * Copyright (C) 2011 - 2019 Alex Herbert
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

import uk.ac.sussex.gdsc.core.utils.TurboList;
import uk.ac.sussex.gdsc.core.utils.ValidationUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * This class uses a memory cache to allow seeking within an InputStream.
 *
 * <p>Entirely based on {@link ij.io.RandomAccessStream} which is itself based on the JAI
 * MemoryCacheSeekableStream class.
 */
public final class MemoryCacheSeekableStream extends SeekableStream {
  private static final int BLOCK_SIZE = 1024;
  private static final int BLOCK_MASK = 1023;
  private static final int BLOCK_SHIFT = 10;
  private final InputStream src;
  private long pointer;
  private final TurboList<byte[]> data;
  private long length;
  private boolean endOfStream;

  /**
   * Constructs a MemoryCacheSeekableStream from an InputStream. Seeking backwards is supported
   * using a memory cache.
   *
   * @param inputstream the inputstream
   */
  public MemoryCacheSeekableStream(InputStream inputstream) {
    ValidationUtils.checkNotNull(inputstream, "input stream must not be null");
    data = new TurboList<>();
    src = inputstream;
  }

  @Override
  public long getFilePointer() {
    return pointer;
  }

  @Override
  public int read() throws IOException {
    final long l = pointer + 1L;
    final long l1 = readUntil(l);
    if (l1 >= l) {
      final byte[] abyte0 = data.get((int) (pointer >>> BLOCK_SHIFT));
      return abyte0[(int) (pointer++ & BLOCK_MASK)] & 0xff;
    }
    // Set the pointer to the end of the stream
    pointer = length;
    return -1;
  }

  @Override
  public int read(byte[] bytes, int off, int len) throws IOException {
    ValidationUtils.checkNotNull(bytes, "bytes must not be null");
    if (off < 0 || len < 0
    // Overflow safe
        || off + len - bytes.length > 0) {
      throw new IndexOutOfBoundsException();
    }
    if (len == 0) {
      return 0;
    }
    final long l = readUntil(pointer + len);
    if (l <= pointer) {
      // Set the pointer to the end of the stream
      pointer = length;
      return -1;
    }
    final byte[] abyte1 = data.get((int) (pointer >>> BLOCK_SHIFT));
    // The final block may be smaller than the block size
    final int k = Math.min(len, abyte1.length - (int) (pointer & BLOCK_MASK));
    System.arraycopy(abyte1, (int) (pointer & BLOCK_MASK), bytes, off, k);
    pointer += k;
    return k;
  }

  /**
   * Read until the given location.
   *
   * <p>The returned location may be less than desired if end-of-stream was found.
   *
   * @param location the location
   * @return the location
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private long readUntil(long location) throws IOException {
    if (location < length) {
      return location;
    }
    if (endOfStream) {
      return length;
    }
    final int i = (int) (location >>> BLOCK_SHIFT);
    final int j = (int) (length >>> BLOCK_SHIFT);
    for (int k = j; k <= i; k++) {
      final byte[] abyte0 = new byte[BLOCK_SIZE];
      int i1 = BLOCK_SIZE;
      int j1 = 0;
      while (i1 > 0) {
        final int k1 = src.read(abyte0, j1, i1);
        if (k1 == -1) {
          // Truncate the final block
          data.add(Arrays.copyOf(abyte0, BLOCK_SIZE - i1));
          endOfStream = true;
          return length;
        }
        j1 += k1;
        i1 -= k1;
        length += k1;
      }
      data.add(abyte0);
    }
    return length;
  }

  @Override
  public void seek(long loc) throws IOException {
    if (loc < 0) {
      throw new IOException("Location is negative");
    }
    pointer = loc;
  }

  @Override
  public void close() throws IOException {
    data.clear();
    src.close();
  }
}
