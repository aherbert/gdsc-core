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
public class SeekableStreamTest {

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
  public void testReadThrowsNegativeLength() throws IOException {
    try (SeekableStream ss = new DumbSeekableStream()) {
      final byte[] b = new byte[10];
      Assertions.assertThrows(IndexOutOfBoundsException.class, () -> ss.readFully(b, -1));
      Assertions.assertThrows(IndexOutOfBoundsException.class, () -> ss.readFully(b, 0, -1));
      Assertions.assertThrows(IndexOutOfBoundsException.class, () -> ss.readBytes(b, -1));
      Assertions.assertThrows(IndexOutOfBoundsException.class, () -> ss.readBytes(b, 0, -1));
    }
  }

  @Test
  public void testCopy() throws IOException {
    try (SeekableStream ss = new DumbSeekableStream()) {
      Assertions.assertFalse(ss.canCopy());
      Assertions.assertThrows(IOException.class, () -> ss.copy());
    }
  }

  @Test
  public void testSeekWithInt() throws IOException {
    try (SeekableStream ss = new DumbSeekableStream()) {
      for (final int pos : new int[] {Integer.MAX_VALUE, Integer.MIN_VALUE, -1, 0, 1}) {
        ss.seek(pos);
        Assertions.assertEquals(Integer.toUnsignedLong(pos), ss.getFilePointer());
      }
    }
  }

  @Test
  public void testReadFullyThrowsWithUnderflow() throws IOException {
    final byte[] bytes1 = new byte[5];
    final byte[] bytes2 = new byte[10];
    try (SeekableStream ss = create(bytes1)) {
      Assertions.assertThrows(EOFException.class, () -> ss.readFully(bytes2));
    }
  }

  @SeededTest
  public void testReadBytesWithUnderflow(RandomSeed seed) throws IOException {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    final byte[] bytes1 = randomBytes(rng, 5);
    final byte[] bytes2 = new byte[10];
    try (SeekableStream ss = create(bytes1)) {
      Assertions.assertEquals(bytes1.length, ss.readBytes(bytes2));
      Assertions.assertArrayEquals(bytes1, Arrays.copyOf(bytes2, bytes1.length));
    }
  }

  @SeededTest
  public void testReadFully(RandomSeed seed) throws IOException {
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
  public void testReadBytes(RandomSeed seed) throws IOException {
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
