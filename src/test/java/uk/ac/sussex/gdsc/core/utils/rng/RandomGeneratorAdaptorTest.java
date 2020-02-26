package uk.ac.sussex.gdsc.core.utils.rng;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.core.data.NotImplementedException;
import uk.ac.sussex.gdsc.test.junit5.RandomSeed;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;

@SuppressWarnings("javadoc")
public class RandomGeneratorAdaptorTest {
  @Test
  public void testConstructorThrows() {
    Assertions.assertThrows(IllegalArgumentException.class, () -> new RandomGeneratorAdapter(null));
  }

  @Test
  public void testSetSeedThrows() {
    final SplitMix rng = SplitMix.new64(0);
    Assertions.assertThrows(NotImplementedException.class,
        () -> new RandomGeneratorAdapter(rng).setSeed(0), "Should throw with int seed");
    Assertions.assertThrows(NotImplementedException.class,
        () -> new RandomGeneratorAdapter(rng).setSeed(new int[2]), "Should throw with int[] seed");
    Assertions.assertThrows(NotImplementedException.class,
        () -> new RandomGeneratorAdapter(rng).setSeed(0L), "Should throw with long seed");
  }

  @SeededTest
  public void testNextMethods(RandomSeed randomSeed) {
    final long seed = randomSeed.getSeedAsLong();
    final SplitMix rng1 = SplitMix.new64(seed);
    final RandomGeneratorAdapter rng2 = new RandomGeneratorAdapter(SplitMix.new64(seed));
    Assertions.assertEquals(rng1.nextDouble(), rng2.nextDouble());
    Assertions.assertEquals(rng1.nextFloat(), rng2.nextFloat());
    Assertions.assertEquals(rng1.nextInt(), rng2.nextInt());
    Assertions.assertEquals(rng1.nextInt(44), rng2.nextInt(44));
    Assertions.assertEquals(rng1.nextLong(), rng2.nextLong());
    Assertions.assertEquals(rng1.nextBoolean(), rng2.nextBoolean());
    final byte[] b1 = new byte[23];
    final byte[] b2 = new byte[23];
    rng1.nextBytes(b1);
    rng2.nextBytes(b2);
    Assertions.assertArrayEquals(b1, b2);
  }
}
