package uk.ac.sussex.gdsc.core.utils;

import uk.ac.sussex.gdsc.test.junit5.RandomSeed;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.rng.RngUtils;

import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

@SuppressWarnings({"javadoc"})
public class PseudoRandomGeneratorTest {
  @Test
  public void canConstructPseudoRandomGeneratorFromSequence() {
    final double[] expected = new double[] {0.2, 0.87, 0.45, 0.99};
    final PseudoRandomGenerator r = new PseudoRandomGenerator(expected);
    canConstructPseudoRandomGenerator(r, expected);
  }

  @Test
  public void canConstructPseudoRandomGeneratorFromSequenceLength() {
    double[] expected = new double[] {0.2, 0.87, 0.45, 0.99};
    final int length = expected.length - 1;
    final PseudoRandomGenerator r = new PseudoRandomGenerator(expected, length);
    expected = Arrays.copyOf(expected, length);
    canConstructPseudoRandomGenerator(r, expected);
  }

  @SeededTest
  public void canConstructPseudoRandomGeneratorFromSource(RandomSeed seed) {
    final UniformRandomProvider source = RngUtils.create(seed.getSeedAsLong());
    final int length = 5;
    final double[] expected = new double[length];
    for (int i = 0; i < expected.length; i++) {
      expected[i] = source.nextDouble();
    }
    final PseudoRandomGenerator r = new PseudoRandomGenerator(length,
        new RandomGeneratorAdapter(RngUtils.create(seed.getSeedAsLong())));
    canConstructPseudoRandomGenerator(r, expected);
  }

  private static void canConstructPseudoRandomGenerator(PseudoRandomGenerator rng,
      double[] expected) {
    for (int i = 0; i < expected.length; i++) {
      Assertions.assertEquals(expected[i], rng.nextDouble());
    }
    // Repeat
    for (int i = 0; i < expected.length; i++) {
      Assertions.assertEquals(expected[i], rng.nextDouble());
    }
  }
}
