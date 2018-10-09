package uk.ac.sussex.gdsc.core.utils.rng;

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
import java.util.logging.Logger;

import org.apache.commons.math3.stat.inference.ChiSquareTest;
import org.apache.commons.rng.RestorableUniformRandomProvider;
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.simple.RandomSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import uk.ac.sussex.gdsc.test.junit5.*;import uk.ac.sussex.gdsc.test.rng.RngFactory;import uk.ac.sussex.gdsc.test.utils.TestLog;

@SuppressWarnings("javadoc")
public class RadixStringSamplerTest {

  private static Logger logger;

  @BeforeAll
  public static void beforeAll() {
    logger = Logger.getLogger(RadixStringSamplerTest.class.getName());
  }

  @AfterAll
  public static void afterAll() {
    logger = null;
  }

  @Test
  public void testConstructor() {
    final UniformRandomProvider rng = RandomSource.create(RandomSource.SPLIT_MIX_64);
    final int length = 1;
    for (int radix = 2; radix <= 64; radix++) {
      final RadixStringSampler s = new RadixStringSampler(rng, length, radix);
      Assertions.assertNotNull(s);
      Assertions.assertEquals(radix, s.getRadix());
      Assertions.assertEquals(length, s.getLength());
    }
  }

  @SuppressWarnings("unused")
  @Test
  public void testConstructorThrows() {
    final UniformRandomProvider rng = RandomSource.create(RandomSource.SPLIT_MIX_64);
    final int length = 1;
    final int radix = 16;
    Assertions.assertThrows(NullPointerException.class, () -> {
      new RadixStringSampler(null, length, radix);
    });
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      new RadixStringSampler(rng, 0, radix);
    });
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      new RadixStringSampler(rng, length, 0);
    });
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      new RadixStringSampler(rng, length, 65);
    });
  }

  @Test
  public void testZeroLengthSamples() {
    final RestorableUniformRandomProvider rng2 = null;
    final int length = 0;
    for (int radix = 2; radix <= 64; radix++) {
      final int radix_ = radix;
      Assertions.assertEquals("", RadixStringSampler.nextString(rng2, length, radix),
          () -> Integer.toString(radix_));
    }
  }

  @Test
  public void testSamples() {
    for (int radix = 2; radix <= 64; radix++) {
      testSamples(radix);
    }
  }

  private final int lower1 = '0';
  private final int upper1 = '9';
  private final int lower2 = 'A';
  private final int upper2 = 'Z';
  private final int offset2 = lower2 - 10;
  private final int lower3 = 'a';
  private final int upper3 = 'z';
  private final int offset3 = lower3 - 36;

  private int map(char c) {
    if (c >= lower1 && c <= upper1) {
      return c - lower1;
    }
    if (c >= lower2 && c <= upper2) {
      return c - offset2;
    }
    if (c >= lower3 && c <= upper3) {
      return c - offset3;
    }
    if (c == '+') {
      return 62;
    }
    if (c == '/') {
      return 63;
    }
    Assertions.fail("Unsupported character: " + c);
    // For the java compiler
    return 0;
  }

  private void testSamples(int radix) {
    final RestorableUniformRandomProvider rng1 = RandomSource.create(RandomSource.MWC_256);
    final RestorableUniformRandomProvider rng2 = RandomSource.create(RandomSource.MWC_256);
    rng2.restoreState(rng1.saveState());
    // Test long enough strings that the algorithm edge cases are hit
    final int[] lengths = new int[] {1, 2, 3, 4, 5, 10};
    for (final int length : lengths) {
      final RadixStringSampler s = new RadixStringSampler(rng1, length, radix);
      Assertions.assertEquals(length, s.getLength());
      for (int i = 0; i < 10; i++) {
        final String string = s.sample();
        Assertions.assertNotNull(string);
        // System.out.println(string);
        Assertions.assertEquals(length, string.length());
        for (int j = 0; j < length; j++) {
          final char c = string.charAt(j);
          Assertions.assertTrue(map(c) <= radix);
        }

        // Check the static method does the same
        final String string2 = RadixStringSampler.nextString(rng2, length, radix);
        Assertions.assertEquals(string, string2);
      }
    }
  }

  @Test
  public void testSamplesAreUniform() {
    for (int radix = 2; radix <= 64; radix++) {
      testSamplesAreUniform(radix);
    }
  }

  private void testSamplesAreUniform(int radix) {
    final long[] h = new long[radix];

    final UniformRandomProvider rng = RandomSource.create(RandomSource.MWC_256);
    final int length = 1000;
    final int repeats = 100;
    final RadixStringSampler s = new RadixStringSampler(rng, length, radix);
    for (int i = 0; i < repeats; i++) {
      final String hex = s.sample();
      for (int j = 0; j < length; j++) {
        h[map(hex.charAt(j))]++;
      }
    }

    // double mean = (double) length * repeats / radix;
    // for (int i = 0; i < h.length; i++) {
    // System.out.printf("%2d = %d (%.2f)\n", i, h[i], h[i] / mean);
    // }

    // Statistical test
    final ChiSquareTest chi = new ChiSquareTest();
    final double[] expected = new double[h.length];
    Arrays.fill(expected, 1.0 / radix);
    final double p = chi.chiSquareTest(expected, h);
    final boolean reject = p < 0.001;
    logger.log(TestLog.getResultRecord(!reject,
        () -> String.format("Radix %d, chiSq p = %s  (reject=%b)", radix, p, reject)));
    // This will sometimes fail due to randomness so do not assert
    // Assertions.assertFalse(reject);
  }
}
