/*-
 * #%L
 * Genome Damage and Stability Centre ImageJ Core Package
 *
 * Contains code used by:
 *
 * GDSC ImageJ Plugins - Microscopy image analysis
 *
 * GDSC SMLM ImageJ Plugins - Single molecule localisation microscopy (SMLM)
 * %%
 * Copyright (C) 2011 - 2021 Alex Herbert
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

package uk.ac.sussex.gdsc.core.utils.rng;

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
import uk.ac.sussex.gdsc.test.utils.TestLogUtils;

@SuppressWarnings("javadoc")
class RadixStringSamplerTest {

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
  void testConstructor() {
    final UniformRandomProvider rng = RandomSource.SPLIT_MIX_64.create();
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
  void testConstructorThrows() {
    final UniformRandomProvider rng = RandomSource.SPLIT_MIX_64.create();
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
  void testZeroLengthSamples() {
    final RestorableUniformRandomProvider rng2 = null;
    final int length = 0;
    for (int radix = 2; radix <= 64; radix++) {
      final int radix_ = radix;
      Assertions.assertEquals("", RadixStringSampler.nextString(rng2, length, radix),
          () -> Integer.toString(radix_));
    }
  }

  @Test
  void testSamples() {
    for (int radix = 2; radix <= 64; radix++) {
      testSamples(radix);
    }
  }

  private void testSamples(int radix) {
    final RestorableUniformRandomProvider rng1 = RandomSource.SPLIT_MIX_64.create();
    final RestorableUniformRandomProvider rng2 = RandomSource.SPLIT_MIX_64.create();
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

  private final int lower1 = '0';
  private final int upper1 = '9';
  private final int lower2 = 'A';
  private final int upper2 = 'Z';
  private final int offset2 = lower2 - 10;
  private final int lower3 = 'a';
  private final int upper3 = 'z';
  private final int offset3 = lower3 - 36;

  private int map(char ch) {
    if (ch >= lower1 && ch <= upper1) {
      return ch - lower1;
    }
    if (ch >= lower2 && ch <= upper2) {
      return ch - offset2;
    }
    if (ch >= lower3 && ch <= upper3) {
      return ch - offset3;
    }
    if (ch == '+') {
      return 62;
    }
    if (ch == '/') {
      return 63;
    }
    Assertions.fail("Unsupported character: " + ch);
    // For the java compiler
    return 0;
  }

  @Test
  void testSamplesAreUniform() {
    for (int radix = 2; radix <= 64; radix++) {
      testSamplesAreUniform(radix);
    }
  }

  private void testSamplesAreUniform(int radix) {
    final long[] h = new long[radix];

    final UniformRandomProvider rng = RandomSource.SPLIT_MIX_64.create();
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
    logger.log(TestLogUtils.getResultRecord(!reject,
        () -> String.format("Radix %d, chiSq p = %s  (reject=%b)", radix, p, reject)));
    // This will sometimes fail due to randomness so do not assert
    // Assertions.assertFalse(reject);
  }

  @Test
  void testStaticSampleMethodsMatchInstanceSampler() {
    final UniformRandomProvider rng1 = SplitMix.new64(0);
    final UniformRandomProvider rng2 = SplitMix.new64(0);
    final int length = 16;
    Assertions.assertEquals(RadixStringSampler.nextBase64String(rng1, length),
        new RadixStringSampler(rng2, length, 64).sample(), "Base64");
    Assertions.assertEquals(RadixStringSampler.nextHexString(rng1, length),
        new RadixStringSampler(rng2, length, 16).sample(), "Hex");
    Assertions.assertEquals(RadixStringSampler.nextOctalString(rng1, length),
        new RadixStringSampler(rng2, length, 8).sample(), "Octal");
    Assertions.assertEquals(RadixStringSampler.nextBinaryString(rng1, length),
        new RadixStringSampler(rng2, length, 2).sample(), "Binary");
  }
}
