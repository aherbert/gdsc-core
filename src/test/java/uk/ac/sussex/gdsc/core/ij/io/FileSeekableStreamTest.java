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
 * Copyright (C) 2011 - 2021 Alex Herbert
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
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.test.junit5.RandomSeed;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.rng.RngUtils;

@SuppressWarnings({"javadoc"})
class FileSeekableStreamTest {

  private static Path tmpFile;

  @BeforeAll
  public static void setup() throws IOException {
    tmpFile = Files.createTempFile(FileSeekableStreamTest.class.getSimpleName(), "dat");
  }

  @AfterAll
  public static void teardown() throws IOException {
    Files.deleteIfExists(tmpFile);
  }

  @Test
  void testThrowsWithNullInputFile() {
    Assertions.assertThrows(NullPointerException.class,
        () -> new FileSeekableStream((RandomAccessFile) null));
    Assertions.assertThrows(NullPointerException.class, () -> new FileSeekableStream((File) null));
  }

  @Test
  void testThrowsWithOutOfBounds() throws IOException {
    try (SeekableStream ss = create(new byte[10])) {
      final byte[] bytes = new byte[5];
      Assertions.assertThrows(IndexOutOfBoundsException.class, () -> ss.read(bytes, -1, 1));
      Assertions.assertThrows(IndexOutOfBoundsException.class, () -> ss.read(bytes, 0, -1));
      Assertions.assertThrows(IndexOutOfBoundsException.class, () -> ss.read(bytes, 0, 1000));
    }
  }

  @SeededTest
  void canReadSingleByte(RandomSeed seed) throws IOException {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    final byte[] bytes = randomBytes(rng, 2);
    try (SeekableStream ss = create(bytes)) {
      for (int i = 0; i < bytes.length; i++) {
        Assertions.assertEquals(i, ss.getFilePointer());
        Assertions.assertEquals(bytes[i] & 0xff, ss.read());
      }
      Assertions.assertEquals(bytes.length, ss.getFilePointer());
      Assertions.assertEquals(-1, ss.read());
    }
  }

  @SeededTest
  void canReadMultiByte(RandomSeed seed) throws IOException {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    final byte[] bytes = randomBytes(rng, 2);
    try (SeekableStream ss = create(bytes)) {
      final byte[] buffer = new byte[bytes.length];
      Assertions.assertEquals(0, ss.read(buffer, 0, 0));

      Assertions.assertEquals(buffer.length, ss.read(buffer, 0, buffer.length));
      Assertions.assertEquals(buffer.length, ss.getFilePointer());
      Assertions.assertArrayEquals(bytes, buffer);

      Assertions.assertEquals(-1, ss.read(buffer));
    }
  }

  @SeededTest
  void canSeek(RandomSeed seed) throws IOException {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    final byte[] bytes = randomBytes(rng, 20);
    try (SeekableStream ss = create(bytes)) {
      Assertions.assertThrows(IOException.class, () -> ss.seek(-1L));

      for (final int pos : new int[] {3, 13, 4}) {
        ss.seek(pos);
        Assertions.assertEquals(pos, ss.getFilePointer());
        Assertions.assertEquals(bytes[pos] & 0xff, ss.read());
      }
      // Can seek past the end. It should be truncated.
      ss.seek(100);
      Assertions.assertEquals(bytes.length, ss.getFilePointer());
      // It should handle reading nothing
      Assertions.assertEquals(-1, ss.read());
      Assertions.assertEquals(bytes.length, ss.getFilePointer());
    }
  }

  @SeededTest
  void canSkip(RandomSeed seed) throws IOException {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    final byte[] bytes = randomBytes(rng, 20);
    try (SeekableStream ss = create(bytes, true)) {

      Assertions.assertEquals(0, ss.skip(-1));
      Assertions.assertEquals(1, ss.skip(1));
      Assertions.assertEquals(bytes[1] & 0xff, ss.read());

      // Check overflow
      final int avail = bytes.length - 2;
      Assertions.assertEquals(avail, ss.skip(Long.MAX_VALUE));
      Assertions.assertEquals(bytes.length, ss.getFilePointer());
      ss.seek(2);
      Assertions.assertEquals(avail, ss.skip(bytes.length));
      Assertions.assertEquals(bytes.length, ss.getFilePointer());
    }
  }

  private static SeekableStream create(byte[] bytes) throws IOException {
    return create(bytes, false);
  }

  @SuppressWarnings("resource")
  private static SeekableStream create(byte[] bytes, boolean rasFile) throws IOException {
    try (OutputStream out = Files.newOutputStream(tmpFile)) {
      out.write(bytes);
    }
    if (rasFile) {
      return new FileSeekableStream(new RandomAccessFile(tmpFile.toFile(), "r"));
    }
    return new FileSeekableStream(tmpFile.toString());
  }

  private static byte[] randomBytes(UniformRandomProvider rng, int length) {
    final byte[] bytes = new byte[length];
    rng.nextBytes(bytes);
    return bytes;
  }
}
