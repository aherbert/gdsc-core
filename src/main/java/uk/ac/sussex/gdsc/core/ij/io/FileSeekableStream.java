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

import uk.ac.sussex.gdsc.test.junit5.*;
import uk.ac.sussex.gdsc.test.rng.RngFactory;
import org.junit.jupiter.api.*;

import uk.ac.sussex.gdsc.test.junit5.*;
import uk.ac.sussex.gdsc.test.rng.RngFactory;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

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
    if (ras == null) {
      throw new NullPointerException();
    }
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
  public FileSeekableStream(File file) throws FileNotFoundException, SecurityException {
    if (file == null) {
      throw new NullPointerException();
    }
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
  public FileSeekableStream(String path) throws FileNotFoundException, SecurityException {
    this(new File(path));
  }

  /** {@inheritDoc} */
  @Override
  public long getFilePointer() throws IOException {
    return ras.getFilePointer();
  }

  /** {@inheritDoc} */
  @Override
  public int read() throws IOException {
    return ras.read();
  }

  /** {@inheritDoc} */
  @Override
  public int read(byte[] bytes, int off, int len) throws IOException {
    return ras.read(bytes, off, len);
  }

  /** {@inheritDoc} */
  @Override
  public void seek(long loc) throws IOException {
    ras.seek(loc);
  }

  /** {@inheritDoc} */
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
