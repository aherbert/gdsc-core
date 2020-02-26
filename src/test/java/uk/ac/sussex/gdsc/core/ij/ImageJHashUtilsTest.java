package uk.ac.sussex.gdsc.core.ij;

import ij.ImageStack;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ShortProcessor;
import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.Assertions;
import uk.ac.sussex.gdsc.core.utils.SimpleArrayUtils;
import uk.ac.sussex.gdsc.test.junit5.RandomSeed;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.rng.RngUtils;

@SuppressWarnings({"javadoc"})
public class ImageJHashUtilsTest {

  int size = 50;

  @SeededTest
  public void canDigestByteProcessor(RandomSeed seed) {
    final UniformRandomProvider r = RngUtils.create(seed.getSeed());
    final byte[] data = new byte[size];
    r.nextBytes(data);

    final String d1 = ImageJHashUtils.digest(new ByteProcessor(size, 1, data));
    SimpleArrayUtils.reverse(data);
    final String d2 = ImageJHashUtils.digest(new ByteProcessor(size, 1, data));
    Assertions.assertNotEquals(d1, d2);

    digestStack(data, d2);
  }

  @SeededTest
  public void canDigestShortProcessor(RandomSeed seed) {
    final UniformRandomProvider r = RngUtils.create(seed.getSeed());
    final short[] data = new short[size];
    for (int i = 0; i < size; i++) {
      data[i] = (short) ((r.nextDouble() - 0.5) * 2 * Short.MAX_VALUE);
    }

    final String d1 = ImageJHashUtils.digest(new ShortProcessor(size, 1, data, null));
    SimpleArrayUtils.reverse(data);
    final String d2 = ImageJHashUtils.digest(new ShortProcessor(size, 1, data, null));
    Assertions.assertNotEquals(d1, d2);

    digestStack(data, d2);
  }

  @SeededTest
  public void canDigestFloatProcessor(RandomSeed seed) {
    final UniformRandomProvider r = RngUtils.create(seed.getSeed());
    final float[] data = new float[size];
    for (int i = 0; i < size; i++) {
      data[i] = (r.nextFloat() - 0.5f) * 2f;
    }

    final String d1 = ImageJHashUtils.digest(new FloatProcessor(size, 1, data));
    SimpleArrayUtils.reverse(data);
    final String d2 = ImageJHashUtils.digest(new FloatProcessor(size, 1, data));
    Assertions.assertNotEquals(d1, d2);

    digestStack(data, d2);
  }

  @SeededTest
  public void canDigestColorProcessor(RandomSeed seed) {
    final UniformRandomProvider r = RngUtils.create(seed.getSeed());
    final int[] data = new int[size];
    for (int i = 0; i < size; i++) {
      data[i] = r.nextInt();
    }

    final String d1 = ImageJHashUtils.digest(new ColorProcessor(size, 1, data));
    SimpleArrayUtils.reverse(data);
    final String d2 = ImageJHashUtils.digest(new ColorProcessor(size, 1, data));
    Assertions.assertNotEquals(d1, d2);

    digestStack(data, d2);
  }

  private void digestStack(final Object pixels, final String expected) {
    final ImageStack stack = new ImageStack(size, 1);
    stack.addSlice(null, pixels);
    final String observed = ImageJHashUtils.digest(stack);
    Assertions.assertEquals(expected, observed, "Stack digest");
  }
}
