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
import java.util.Arrays;
import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.test.junit5.RandomSeed;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.rng.RngUtils;

@SuppressWarnings({"javadoc"})
class SeekableStreamTest {

  private static class DumbSeekableStream extends SeekableStream {
    private long pos;

    @Override
    public long getFilePointer() {
      return pos;
    }

    @Override
    public int read() {
      return 0;
    }

    @Override
    public int read(byte[] bytes, int off, int len) {
      return 0;
    }

    @Override
    public void seek(long loc) {
      pos = loc;
    }

    @Override
    public void close() {}
  }

  @Test
  void testReadThrowsNegativeLength() throws IOException {
    try (SeekableStream ss = new DumbSeekableStream()) {
      final byte[] b = new byte[10];
      Assertions.assertThrows(IndexOutOfBoundsException.class, () -> ss.readFully(b, -1));
      Assertions.assertThrows(IndexOutOfBoundsException.class, () -> ss.readFully(b, 0, -1));
      Assertions.assertThrows(IndexOutOfBoundsException.class, () -> ss.readBytes(b, -1));
      Assertions.assertThrows(IndexOutOfBoundsException.class, () -> ss.readBytes(b, 0, -1));
    }
  }

  @Test
  void testCopy() throws IOException {
    try (SeekableStream ss = new DumbSeekableStream()) {
      Assertions.assertFalse(ss.canCopy());
      Assertions.assertThrows(IOException.class, () -> ss.copy());
    }
  }

  @Test
  void testSeekWithInt() throws IOException {
    try (SeekableStream ss = new DumbSeekableStream()) {
      for (final int pos : new int[] {Integer.MAX_VALUE, Integer.MIN_VALUE, -1, 0, 1}) {
        ss.seek(pos);
        Assertions.assertEquals(Integer.toUnsignedLong(pos), ss.getFilePointer());
      }
    }
  }

  @Test
  void testReadFullyThrowsWithUnderflow() throws IOException {
    final byte[] bytes1 = new byte[5];
    final byte[] bytes2 = new byte[10];
    try (SeekableStream ss = create(bytes1)) {
      Assertions.assertThrows(EOFException.class, () -> ss.readFully(bytes2));
    }
  }

  @SeededTest
  void testReadBytesWithUnderflow(RandomSeed seed) throws IOException {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    final byte[] bytes1 = randomBytes(rng, 5);
    final byte[] bytes2 = new byte[10];
    try (SeekableStream ss = create(bytes1)) {
      Assertions.assertEquals(bytes1.length, ss.readBytes(bytes2));
      Assertions.assertArrayEquals(bytes1, Arrays.copyOf(bytes2, bytes1.length));
    }
  }

  @SeededTest
  void testReadFully(RandomSeed seed) throws IOException {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    final byte[] bytes1 = randomBytes(rng, 5);
    final byte[] bytes2 = new byte[5];
    try (SeekableStream ss = create(bytes1)) {
      ss.readFully(bytes2);
      Assertions.assertArrayEquals(bytes1, bytes2);
    }
    try (SeekableStream ss = create(bytes1)) {
      ss.readFully(bytes2, bytes2.length);
      Assertions.assertArrayEquals(bytes1, bytes2);
    }
    try (SeekableStream ss = create(bytes1)) {
      ss.readFully(bytes2, 0, bytes2.length);
      Assertions.assertArrayEquals(bytes1, bytes2);
    }
  }

  @SeededTest
  void testReadBytes(RandomSeed seed) throws IOException {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    final byte[] bytes1 = randomBytes(rng, 5);
    final byte[] bytes2 = new byte[5];
    try (SeekableStream ss = create(bytes1)) {
      Assertions.assertEquals(bytes1.length, ss.readBytes(bytes2));
      Assertions.assertArrayEquals(bytes1, bytes2);
    }
    try (SeekableStream ss = create(bytes1)) {
      Assertions.assertEquals(bytes1.length, ss.readBytes(bytes2, bytes2.length));
      Assertions.assertArrayEquals(bytes1, bytes2);
    }
    try (SeekableStream ss = create(bytes1)) {
      Assertions.assertEquals(bytes1.length, ss.readBytes(bytes2, 0, bytes2.length));
      Assertions.assertArrayEquals(bytes1, bytes2);
    }
  }

  private static SeekableStream create(byte[] bytes) {
    return ByteArraySeekableStream.wrap(bytes);
  }

  private static byte[] randomBytes(UniformRandomProvider rng, int length) {
    final byte[] bytes = new byte[length];
    rng.nextBytes(bytes);
    return bytes;
  }
}
