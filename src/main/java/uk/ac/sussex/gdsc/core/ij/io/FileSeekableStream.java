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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import uk.ac.sussex.gdsc.core.utils.ValidationUtils;

/**
 * This class uses a random access file to allow seeking within a stream.
 */
public final class FileSeekableStream extends SeekableStream {
  private final RandomAccessFile ras;

  /**
   * Instantiates a new file seekable stream. The input file must be opened.
   *
   * @param ras the random access file
   */
  public FileSeekableStream(RandomAccessFile ras) {
    ValidationUtils.checkNotNull(ras, "random access file must not be null");
    this.ras = ras;
  }

  /**
   * Instantiates a new file seekable stream. The input file will be opened.
   *
   * @param file the file
   * @throws FileNotFoundException if the given file object does not denote an existing regular file
   * @throws SecurityException if a security manager exists and its checkRead method denies read
   *         access to the file
   */
  public FileSeekableStream(File file) throws FileNotFoundException {
    ValidationUtils.checkNotNull(file, "file must not be null");
    this.ras = new RandomAccessFile(file, "r");
  }

  /**
   * Instantiates a new file seekable stream. The input file will be opened.
   *
   * @param path the path
   * @throws FileNotFoundException if the given file object does not denote an existing regular file
   * @throws SecurityException if a security manager exists and its checkRead method denies read
   *         access to the file
   */
  public FileSeekableStream(String path) throws FileNotFoundException {
    this(new File(path));
  }

  @Override
  public long getFilePointer() throws IOException {
    return ras.getFilePointer();
  }

  @Override
  public int read() throws IOException {
    return ras.read();
  }

  @Override
  public int read(byte[] bytes, int off, int len) throws IOException {
    return ras.read(bytes, off, len);
  }

  /**
   * {@inheritDoc}
   *
   * <p>A seek past the end of the stream will result in the file pointer being set to the end of
   * the stream.
   *
   * @see #getFilePointer()
   */
  @Override
  public void seek(long loc) throws IOException {
    // Do not seek past the end
    final long len = ras.length();
    ras.seek(loc < len ? loc : len);
  }

  @Override
  public void close() throws IOException {
    ras.close();
  }

  @Override
  public long skip(long n) throws IOException {
    if (n <= 0) {
      return 0;
    }
    final long pos = getFilePointer();
    final long len = ras.length();
    long newpos = pos + n;
    if (newpos > len || newpos < 0) {
      newpos = len;
    }
    seek(newpos);

    /* return the actual number of bytes skipped */
    return newpos - pos;
  }
}
