package uk.ac.sussex.gdsc.core.utils.rng;

import uk.ac.sussex.gdsc.core.data.NotImplementedException;
import uk.ac.sussex.gdsc.test.junit5.RandomSeed;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;

import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Random;

@SuppressWarnings("javadoc")
public class JdkRandomAdaptorTest {
  @SuppressWarnings("unused")
  @Test
  public void testConstructorThrows() {
    Assertions.assertThrows(NullPointerException.class, () -> {
      new JdkRandomAdaptor(null);
    });
  }

  @Test
  public void testSetSeedThrows() {
    final JdkRandomAdaptor rng = new JdkRandomAdaptor(SplitMix.new64(0));
    Assertions.assertThrows(NotImplementedException.class, () -> {
      rng.setSeed(44);
    });
  }

  @SeededTest
  public void testGeneratedValues(RandomSeed randomSeed) {
    final long seed = randomSeed.getSeedAsLong();
    final Random random1 = new Random(seed);
    final Random random2 = new Random(seed);
    final UniformRandomProvider source = new UniformRandomProvider() {
      @Override
      public void nextBytes(byte[] bytes) {}

      @Override
      public void nextBytes(byte[] bytes, int start, int len) {}

      @Override
      public int nextInt() {
        return random2.nextInt();
      }

      @Override
      public int nextInt(int n) {
        return 0;
      }

      @Override
      public long nextLong() {
        return 0;
      }

      @Override
      public long nextLong(long n) {
        return 0;
      }

      @Override
      public boolean nextBoolean() {
        return false;
      }

      @Override
      public float nextFloat() {
        return 0;
      }

      @Override
      public double nextDouble() {
        return 0;
      }
    };
    final JdkRandomAdaptor rng = new JdkRandomAdaptor(source);

    Assertions.assertEquals(random1.nextInt(), rng.nextInt());
    Assertions.assertEquals(random1.nextInt(567), rng.nextInt(567));
    Assertions.assertEquals(random1.nextFloat(), rng.nextFloat());
    Assertions.assertEquals(random1.nextDouble(), rng.nextDouble());
  }

  @Test
  public void testSerializationThrows() throws IOException {
    final JdkRandomAdaptor rng = new JdkRandomAdaptor(SplitMix.new64(0));
    try (ObjectOutputStream oos = new ObjectOutputStream(new ByteArrayOutputStream())) {
      Assertions.assertThrows(NotImplementedException.class, () -> {
        oos.writeObject(rng);
      });
    }
  }
}
