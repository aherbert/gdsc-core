package uk.ac.sussex.gdsc.core.ij.io;

import uk.ac.sussex.gdsc.test.junit5.RandomSeed;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.rng.RngUtils;

import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.Assertions;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

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

    final File file = new File("test");
    try (ByteArraySeekableStream in = ByteArraySeekableStream.wrap(bytes)) {
      final FastTiffDecoder decoder = little ? new LittleEndianFastTiffDecoder(in, file)
          : new BigEndianFastTiffDecoder(in, file);
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
