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

package uk.ac.sussex.gdsc.core.ij.io;

import java.io.IOException;
import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.test.junit5.RandomSeed;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.rng.RngUtils;

@SuppressWarnings({"javadoc"})
class ByteArraySeekableStreamTest {

  @Test
  void testThrowsWithNullBytes() {
    Assertions.assertThrows(NullPointerException.class, () -> ByteArraySeekableStream.wrap(null));
  }

  @SeededTest
  void canReadSingleByte(RandomSeed seed) {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    final byte[] bytes = randomBytes(rng, 2);
    try (ByteArraySeekableStream ss = ByteArraySeekableStream.wrap(bytes)) {
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
    try (ByteArraySeekableStream ss = ByteArraySeekableStream.wrap(bytes)) {
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
    try (ByteArraySeekableStream ss = ByteArraySeekableStream.wrap(bytes)) {
      Assertions.assertThrows(IOException.class, () -> ss.seek(-1L));

      for (final int pos : new int[] {3, 13, 4}) {
        ss.seek(pos);
        Assertions.assertEquals(pos, ss.getFilePointer());
        Assertions.assertEquals(bytes.length - pos, ss.available());
        Assertions.assertEquals(bytes[pos] & 0xff, ss.read());
      }
      ss.seek(100);
      Assertions.assertEquals(bytes.length, ss.getFilePointer());
    }
  }

  @SeededTest
  void canSkip(RandomSeed seed) {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    final byte[] bytes = randomBytes(rng, 20);
    try (ByteArraySeekableStream ss = ByteArraySeekableStream.wrap(bytes)) {

      Assertions.assertEquals(0, ss.skip(-1));
      Assertions.assertEquals(1, ss.skip(1));
      Assertions.assertEquals(bytes[1] & 0xff, ss.read());

      // Check overflow
      final int avail = ss.available();
      Assertions.assertEquals(avail, ss.skip(Long.MAX_VALUE));
      Assertions.assertEquals(bytes.length, ss.getFilePointer());
    }
  }

  @SeededTest
  void canCopy(RandomSeed seed) {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    final byte[] bytes = randomBytes(rng, 20);
    try (ByteArraySeekableStream ss = ByteArraySeekableStream.wrap(bytes)) {
      Assertions.assertTrue(ss.canCopy());
      try (ByteArraySeekableStream ss2 = ss.copy()) {
        for (int i = 0; i < bytes.length; i++) {
          Assertions.assertEquals(ss.read(), ss2.read());
        }
      }
    }
  }

  private static byte[] randomBytes(UniformRandomProvider rng, int length) {
    final byte[] bytes = new byte[length];
    rng.nextBytes(bytes);
    return bytes;
  }
}
