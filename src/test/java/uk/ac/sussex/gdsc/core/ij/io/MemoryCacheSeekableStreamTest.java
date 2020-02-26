package uk.ac.sussex.gdsc.core.ij.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.test.junit5.RandomSeed;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.rng.RngUtils;

@SuppressWarnings({"javadoc"})
public class MemoryCacheSeekableStreamTest {

  @Test
  public void testThrowsWithNullInputStream() {
    Assertions.assertThrows(NullPointerException.class, () -> new MemoryCacheSeekableStream(null));
  }

  @Test
  public void testThrowsWithOutOfBounds() throws IOException {
    try (SeekableStream ss = create(new byte[10])) {
      final byte[] bytes = new byte[5];
      Assertions.assertThrows(IndexOutOfBoundsException.class, () -> ss.read(bytes, -1, 1));
      Assertions.assertThrows(IndexOutOfBoundsException.class, () -> ss.read(bytes, 0, -1));
      Assertions.assertThrows(IndexOutOfBoundsException.class, () -> ss.read(bytes, 0, 1000));
    }
  }

  @SeededTest
  public void canReadSingleByte(RandomSeed seed) throws IOException {
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
  public void canReadMultiByte(RandomSeed seed) throws IOException {
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
  public void canReadBytesLargerThanBlockSize(RandomSeed seed) throws IOException {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    // Block size is 1024 so create more than that but read less
    final byte[] bytes = randomBytes(rng, 1024 * 3);
    try (SeekableStream ss = create(bytes)) {
      final byte[] buffer = new byte[1536];

      Assertions.assertEquals(buffer.length, ss.readBytes(buffer, 0, buffer.length));
      Assertions.assertEquals(buffer.length, ss.getFilePointer());
      Assertions.assertArrayEquals(Arrays.copyOf(bytes, buffer.length), buffer);

      // Read the rest
      ByteArrayOutputStream tmp = new ByteArrayOutputStream(bytes.length);
      tmp.write(buffer);
      int read = ss.read(buffer);
      while (read >= 0) {
        tmp.write(buffer, 0, read);
        read = ss.read(buffer);
      }

      Assertions.assertEquals(-1, ss.read(buffer));

      Assertions.assertArrayEquals(bytes, tmp.toByteArray());
    }
  }

  @SeededTest
  public void canSeek(RandomSeed seed) throws IOException {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    final byte[] bytes = randomBytes(rng, 20);
    try (SeekableStream ss = create(bytes)) {
      Assertions.assertThrows(IOException.class, () -> ss.seek(-1L));

      for (final int pos : new int[] {3, 13, 4}) {
        ss.seek(pos);
        Assertions.assertEquals(pos, ss.getFilePointer());
        Assertions.assertEquals(bytes[pos] & 0xff, ss.read());
      }
      // Can seek past the end
      ss.seek(100);
      Assertions.assertEquals(100, ss.getFilePointer());
      // It should handle reading nothing
      Assertions.assertEquals(-1, ss.read());
      Assertions.assertEquals(bytes.length, ss.getFilePointer());
    }
  }

  @SeededTest
  public void canSkip(RandomSeed seed) throws IOException {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    final byte[] bytes = randomBytes(rng, 20);
    try (SeekableStream ss = create(bytes)) {

      Assertions.assertEquals(0, ss.skip(-1));
      Assertions.assertEquals(1, ss.skip(1));
      Assertions.assertEquals(bytes[1] & 0xff, ss.read());

      // Check overflow
      final int avail = bytes.length - 2;
      Assertions.assertEquals(avail, ss.skip(Long.MAX_VALUE));
      Assertions.assertEquals(bytes.length, ss.getFilePointer());
    }
  }

  private static SeekableStream create(byte[] bytes) {
    return new MemoryCacheSeekableStream(new ByteArrayInputStream(bytes));
  }

  private static byte[] randomBytes(UniformRandomProvider rng, int length) {
    final byte[] bytes = new byte[length];
    rng.nextBytes(bytes);
    return bytes;
  }
}
