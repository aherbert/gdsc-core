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
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.Assertions;
import uk.ac.sussex.gdsc.test.junit5.RandomSeed;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.rng.RngUtils;

@SuppressWarnings({"javadoc"})
public class EndianFastTiffDecoderTest {

  @SeededTest
  public void testLittleEndianFastTiffDecoder(RandomSeed seed) throws IOException {
    testEndianFastTiffDecoder(seed, true);
  }

  @SeededTest
  public void testBigEndianFastTiffDecoder(RandomSeed seed) throws IOException {
    testEndianFastTiffDecoder(seed, false);
  }

  private static void testEndianFastTiffDecoder(RandomSeed seed, boolean little)
      throws IOException {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    final ByteBuffer bb = ByteBuffer.allocate(1000);
    if (little) {
      bb.order(ByteOrder.LITTLE_ENDIAN);
    }
    for (int i = 0; i < 10; i++) {
      bb.putShort((short) rng.nextInt());
      bb.putInt(rng.nextInt());
      bb.putLong(rng.nextInt());
    }

    bb.flip();
    final byte[] bytes = new byte[bb.limit()];
    bb.get(bytes);

    final String name = "test";
    try (ByteArraySeekableStream in = ByteArraySeekableStream.wrap(bytes)) {
      final FastTiffDecoder decoder = little ? new LittleEndianFastTiffDecoder(in, name)
          : new BigEndianFastTiffDecoder(in, name);
      Assertions.assertEquals(little, decoder.isLittleEndian());

      bb.rewind();
      for (int i = 0; i < 10; i++) {
        Assertions.assertEquals(bb.getShort() & 0xffff, decoder.readShort());
        Assertions.assertEquals(bb.getInt(), decoder.readInt());
        Assertions.assertEquals(bb.getLong(), decoder.readLong());
      }
    }
  }
}
