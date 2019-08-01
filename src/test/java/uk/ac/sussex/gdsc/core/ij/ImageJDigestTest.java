package uk.ac.sussex.gdsc.core.ij;

import uk.ac.sussex.gdsc.core.utils.DigestUtils;
import uk.ac.sussex.gdsc.test.junit5.RandomSeed;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.rng.RngUtils;

import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ShortProcessor;

import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.Assertions;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

@SuppressWarnings({"javadoc"})
public class ImageJDigestTest {
  int size = 50;

  @SeededTest
  public void canDigestByteProcessor(RandomSeed seed) {
    final UniformRandomProvider r = RngUtils.create(seed.getSeed());
    final byte[] data = new byte[size];
    r.nextBytes(data);

    final String o = new ImageJDigest().digest(new ByteProcessor(size, 1, data));
    final String e = DigestUtils.md5Hex(data);
    Assertions.assertEquals(e, o);
  }

  @SeededTest
  public void canDigestShortProcessor(RandomSeed seed) throws IOException {
    final UniformRandomProvider r = RngUtils.create(seed.getSeed());
    final short[] data = new short[size];
    for (int i = 0; i < size; i++) {
      data[i] = (short) ((r.nextDouble() - 0.5) * 2 * Short.MAX_VALUE);
    }

    final String o = new ImageJDigest().digest(new ShortProcessor(size, 1, data, null));
    final ByteArrayOutputStream bos = new ByteArrayOutputStream(size);
    final DataOutputStream out = new DataOutputStream(bos);
    for (int i = 0; i < size; i++) {
      out.writeShort(data[i]);
    }
    final String e = DigestUtils.md5Hex(bos.toByteArray());
    Assertions.assertEquals(e, o);
  }

  @SeededTest
  public void canDigestFloatProcessor(RandomSeed seed) throws IOException {
    final UniformRandomProvider r = RngUtils.create(seed.getSeed());
    final float[] data = new float[size];
    for (int i = 0; i < size; i++) {
      data[i] = (r.nextFloat() - 0.5f) * 2f;
    }

    final String o = new ImageJDigest().digest(new FloatProcessor(size, 1, data, null));
    final ByteArrayOutputStream bos = new ByteArrayOutputStream(size);
    final DataOutputStream out = new DataOutputStream(bos);
    for (int i = 0; i < size; i++) {
      out.writeFloat(data[i]);
    }
    final String e = DigestUtils.md5Hex(bos.toByteArray());
    Assertions.assertEquals(e, o);
  }

  @SeededTest
  public void canDigestColorProcessor(RandomSeed seed) throws IOException {
    final UniformRandomProvider r = RngUtils.create(seed.getSeed());
    final int[] data = new int[size];
    for (int i = 0; i < size; i++) {
      data[i] = r.nextInt();
    }

    final String o = new ImageJDigest().digest(new ColorProcessor(size, 1, data));
    final ByteArrayOutputStream bos = new ByteArrayOutputStream(size);
    final DataOutputStream out = new DataOutputStream(bos);
    for (int i = 0; i < size; i++) {
      out.writeInt(data[i]);
    }
    final String e = DigestUtils.md5Hex(bos.toByteArray());
    Assertions.assertEquals(e, o);
  }
}
