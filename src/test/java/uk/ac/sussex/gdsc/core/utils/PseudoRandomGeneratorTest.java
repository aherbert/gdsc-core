package uk.ac.sussex.gdsc.core.utils;

import uk.ac.sussex.gdsc.test.junit5.*;
import uk.ac.sussex.gdsc.test.rng.RngFactory;
import org.junit.jupiter.api.*;
import uk.ac.sussex.gdsc.test.api.*;
import uk.ac.sussex.gdsc.test.utils.*;

import uk.ac.sussex.gdsc.test.junit5.*;
import uk.ac.sussex.gdsc.test.rng.RngFactory;
import org.junit.jupiter.api.*;
import uk.ac.sussex.gdsc.test.api.*;

import uk.ac.sussex.gdsc.test.junit5.*;
import uk.ac.sussex.gdsc.test.rng.RngFactory;
import org.junit.jupiter.api.*;

import uk.ac.sussex.gdsc.test.junit5.*;
import uk.ac.sussex.gdsc.test.rng.RngFactory;


import java.util.Arrays;

import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"javadoc"})
public class PseudoRandomGeneratorTest {
  @Test
  public void canConstructPseudoRandomGeneratorFromSequence() {
    final double[] e = new double[] {0.2, 0.87, 0.45, 0.99};
    final PseudoRandomGenerator r = new PseudoRandomGenerator(e);
    canConstructPseudoRandomGenerator(r, e);
  }

  @Test
  public void canConstructPseudoRandomGeneratorFromSequenceLength() {
    double[] e = new double[] {0.2, 0.87, 0.45, 0.99};
    final int length = e.length - 1;
    final PseudoRandomGenerator r = new PseudoRandomGenerator(e, length);
    e = Arrays.copyOf(e, length);
    canConstructPseudoRandomGenerator(r, e);
  }

  @SeededTest
  public void canConstructPseudoRandomGeneratorFromSource(RandomSeed seed) {
    final UniformRandomProvider source = RngFactory.create(seed.getSeedAsLong());
    final int length = 5;
    final double[] e = new double[length];
    for (int i = 0; i < e.length; i++) {
      e[i] = source.nextDouble();
    }
    final PseudoRandomGenerator r = new PseudoRandomGenerator(length,
        new RandomGeneratorAdapter(RngFactory.create(seed.getSeedAsLong())));
    canConstructPseudoRandomGenerator(r, e);
  }

  private static void canConstructPseudoRandomGenerator(PseudoRandomGenerator r, double[] e) {
    for (int i = 0; i < e.length; i++) {
      Assertions.assertEquals(e[i], r.nextDouble());
    }
    // Repeat
    for (int i = 0; i < e.length; i++) {
      Assertions.assertEquals(e[i], r.nextDouble());
    }
  }
}
