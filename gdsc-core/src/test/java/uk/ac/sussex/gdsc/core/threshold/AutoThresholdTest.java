/*-
 * #%L
 * Genome Damage and Stability Centre Core Package
 *
 * Contains core utilities for image analysis and is used by:
 *
 * GDSC ImageJ Plugins - Microscopy image analysis
 *
 * GDSC SMLM ImageJ Plugins - Single molecule localisation microscopy (SMLM)
 * %%
 * Copyright (C) 2011 - 2025 Alex Herbert
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

package uk.ac.sussex.gdsc.core.threshold;

import fiji.threshold.Auto_Threshold;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.core.threshold.AutoThreshold.Method;
import uk.ac.sussex.gdsc.core.utils.MathUtils;
import uk.ac.sussex.gdsc.core.utils.SimpleArrayUtils;
import uk.ac.sussex.gdsc.core.utils.Statistics;

@SuppressWarnings({"javadoc"})
class AutoThresholdTest {
  private static final Method[] methods = Method.values();

  @Test
  void canThresholdWithBadData() {
    Assertions.assertEquals(0, AutoThreshold.getThreshold(Method.NONE, null));
    Assertions.assertEquals(0, AutoThreshold.getThreshold(Method.NONE, new int[0]));
    Assertions.assertEquals(0, AutoThreshold.getThreshold(Method.DEFAULT, null));
    Assertions.assertEquals(0, AutoThreshold.getThreshold(Method.DEFAULT, new int[0]));
    Assertions.assertEquals(0, AutoThreshold.getThreshold(Method.DEFAULT, new int[10]));
  }

  @Test
  void canThreshold() {
    assertThreshold(new int[] {1});
    assertThreshold(new int[] {6, 7, 8});
    assertThreshold(new int[] {1, 1, 4, 16, 7, 3, 1, 1, 1, 1, 1});
    assertThreshold(new int[] {1, 1, 4, 16, 7, 3, 1, 0, 1, 0, 2, 1});
  }

  @Test
  void canGetMethods() {
    final String[] methodNames = AutoThreshold.getMethods();
    Assertions.assertNotSame(methodNames, AutoThreshold.getMethods());
    Assertions.assertArrayEquals(methodNames, AutoThreshold.getMethods(false));
    Assertions.assertEquals(Method.NONE, AutoThreshold.getMethod("something else"));
    Assertions.assertEquals(Method.NONE, AutoThreshold.getMethod(-1, false));
    Assertions.assertEquals(Method.NONE, AutoThreshold.getMethod(Integer.MAX_VALUE, false));
    final EnumSet<Method> set = EnumSet.noneOf(Method.class);
    for (int i = 0; i < methodNames.length; i++) {
      final Method m = AutoThreshold.getMethod(i, false);
      Assertions.assertEquals(methodNames[i], m.toString());
      Assertions.assertEquals(m, AutoThreshold.getMethod(m.toString()));
      set.add(m);
    }
    Assertions.assertEquals(methodNames.length, set.size());
    Assertions.assertTrue(set.contains(Method.NONE));
  }

  @Test
  void canGetMethodsWithoutNone() {
    final String[] methodNames = AutoThreshold.getMethods(true);
    Assertions.assertNotSame(methodNames, AutoThreshold.getMethods());
    Assertions.assertEquals(Method.NONE, AutoThreshold.getMethod("something else"));
    Assertions.assertEquals(Method.NONE, AutoThreshold.getMethod(-1, true));
    Assertions.assertEquals(Method.NONE, AutoThreshold.getMethod(Integer.MAX_VALUE, true));
    final EnumSet<Method> set = EnumSet.noneOf(Method.class);
    for (int i = 0; i < methodNames.length; i++) {
      final Method m = AutoThreshold.getMethod(i, true);
      Assertions.assertEquals(methodNames[i], m.toString());
      set.add(m);
    }
    Assertions.assertEquals(methodNames.length, set.size());
    Assertions.assertFalse(set.contains(Method.NONE));
  }

  @Test
  void canChangeStdDevMultiplier() {
    final int[] data = new int[] {1, 1, 4, 16, 7, 3, 1, 1, 1, 1, 1};
    final AutoThreshold thresholder = new AutoThreshold();
    final double multiplier = AutoThreshold.getStdDevMultiplier();
    try {
      for (final double m : new double[] {-4, -3, -2, 2, 3, 4}) {
        AutoThreshold.setStdDevMultiplier(m);
        Assertions.assertEquals(m, AutoThreshold.getStdDevMultiplier());
        Assertions.assertEquals(thresholder.meanPlusStdDev(data, m),
            AutoThreshold.meanPlusStdDev(data));
      }
    } finally {
      AutoThreshold.setStdDevMultiplier(multiplier);
    }
  }

  @Test
  void testEmptyHistogram() {
    final int[] data = new int[1];
    // These results match what Auto_Threshold returns
    Assertions.assertEquals(-1, AutoThreshold.huang(data));
    Assertions.assertEquals(0, AutoThreshold.ijDefault(data));
    Assertions.assertEquals(-1, AutoThreshold.intermodes(data));
    Assertions.assertEquals(-1, AutoThreshold.isoData(data));
    Assertions.assertEquals(0, AutoThreshold.li(data));
    Assertions.assertEquals(-1, AutoThreshold.maxEntropy(data));
    Assertions.assertEquals(0, AutoThreshold.mean(data));
    Assertions.assertEquals(0, AutoThreshold.meanPlusStdDev(data));
    Assertions.assertEquals(0, AutoThreshold.minErrorI(data));
    Assertions.assertEquals(-1, AutoThreshold.minimum(data));
    Assertions.assertEquals(0, AutoThreshold.moments(data));
    Assertions.assertEquals(-1, AutoThreshold.otsu(data));
    Assertions.assertEquals(0, AutoThreshold.percentile(data));
    Assertions.assertEquals(0, AutoThreshold.renyiEntropy(data));
    Assertions.assertEquals(0, AutoThreshold.shanbhag(data));
    Assertions.assertEquals(0, AutoThreshold.triangle(data));
    Assertions.assertEquals(-1, AutoThreshold.yen(data));
  }

  @Test
  void testHuang() {
    Assertions.assertEquals(-1, AutoThreshold.huang(new int[10]));
    final int threshold = AutoThreshold.huang(new int[] {5, 4, 3, 2, 1});
    Assertions.assertEquals(threshold + 1, AutoThreshold.huang(new int[] {0, 5, 4, 3, 2, 1, 0}));
  }

  @Test
  void testIjDefault() {
    Assertions.assertEquals(10 / 2, AutoThreshold.ijDefault(new int[10]));
    final int threshold = AutoThreshold.ijDefault(new int[] {5, 4, 3, 2, 1});
    Assertions.assertEquals(threshold + 1,
        AutoThreshold.ijDefault(new int[] {0, 5, 4, 3, 2, 1, 0}));
  }

  @Test
  void testIsoData() {
    final int threshold = AutoThreshold.isoData(new int[] {5, 4, 3, 2, 1});
    Assertions.assertEquals(threshold + 1, AutoThreshold.isoData(new int[] {0, 5, 4, 3, 2, 1, 0}));
  }

  @Test
  void testOtsu() {
    // Test no denominator for BCV
    final int threshold = AutoThreshold.otsu(new int[] {5, 4, 3, 2, 1});
    Assertions.assertEquals(threshold + 2, AutoThreshold.otsu(new int[] {0, 0, 5, 4, 3, 2, 1, 0}));
    // Test multiple thresholds for maximum BCV returns the average
    Assertions.assertEquals(6,
        AutoThreshold.otsu(new int[] {10, 20, 10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 10, 20, 10}));
    // Test the debugging
    final Logger logger = Logger.getLogger(AutoThreshold.class.getName());
    final Level level = logger.getLevel();
    logger.setLevel(Level.FINER);
    Assertions.assertEquals(1, AutoThreshold.otsu(new int[] {5, 4, 3, 2, 1}));
    logger.setLevel(level);
  }

  @Test
  void testPercentile() {
    Assertions.assertEquals(0, AutoThreshold.percentile(new int[] {1, 1}));
    final int threshold = AutoThreshold.percentile(new int[] {4, 3, 2, 1});
    Assertions.assertEquals(threshold + 1, AutoThreshold.percentile(new int[] {0, 4, 3, 2, 1, 0}));
    final int threshold2 = AutoThreshold.percentile(new int[] {1, 2, 3, 4});
    Assertions.assertEquals(threshold2 + 1, AutoThreshold.percentile(new int[] {0, 1, 2, 3, 4, 0}));
  }

  @Test
  void testRenyiEntropy() {
    final int threshold =
        AutoThreshold.renyiEntropy(new int[] {1, 1, 4, 16, 7, 3, 1, 0, 1, 0, 2, 1});
    Assertions.assertEquals(threshold + 1,
        AutoThreshold.renyiEntropy(new int[] {0, 1, 1, 4, 16, 7, 3, 1, 0, 1, 0, 2, 1}));
  }

  @Test
  void testShanbhag() {
    final int threshold = AutoThreshold.shanbhag(new int[] {5, 4, 3, 2, 1});
    Assertions.assertEquals(threshold + 1, AutoThreshold.shanbhag(new int[] {0, 5, 4, 3, 2, 1, 0}));
  }

  @Test
  void testTriangle() {
    final int threshold = AutoThreshold.triangle(new int[] {5, 3, 2, 1});
    Assertions.assertEquals(threshold + 1, AutoThreshold.triangle(new int[] {0, 5, 3, 2, 1, 0}));
  }

  private static void assertThreshold(int[] histogram) {
    final int pad = 10;
    final int[] data = histogram.clone();
    for (int i = 0; i < 2; i++) {
      final int[] h1 = Arrays.copyOf(data, data.length + pad);
      final int[] h2 = new int[h1.length];
      System.arraycopy(data, 0, h2, pad, data.length);
      for (final Method method : methods) {
        final int expected = getThreshold(method, data);
        int actual = AutoThreshold.getThreshold(method, data);
        assertThresholdValue(expected, 0, actual, () -> method + " " + Arrays.toString(data));
        actual = AutoThreshold.getThreshold(method.toString(), data);
        assertThresholdValue(expected, 0, actual, () -> method + " " + Arrays.toString(data));
        // Zero-pad
        actual = AutoThreshold.getThreshold(method, h1);
        assertThresholdValue(expected, 0, actual, () -> method + " " + Arrays.toString(h1));
        actual = AutoThreshold.getThreshold(method, h2);
        assertThresholdValue(expected, pad, actual, () -> method + " " + Arrays.toString(h2));
      }
      SimpleArrayUtils.reverse(data);
    }
  }

  private static int getThreshold(Method method, int[] data) {
    // Stop Auto_Threshold from writing to System.out when it fails.
    // Applies to Intermodes, IsoData, MinError, Minimum.
    // Note this will have side effects if the test is concurrently executed with other
    // resources that write to System.out. In practice this should not happen.
    final PrintStream orig = System.out;
    int t = 0;
    try (PrintStream ps = new PrintStream(new OutputStream() {
      @Override
      public void write(int b) {
        // Ignore the data
      }
    })) {
      System.setOut(ps);
      switch (method) {
        case DEFAULT:
          t = Auto_Threshold.IJDefault(data);
          break;
        case HUANG:
          t = Auto_Threshold.Huang2(data);
          // Work around bug when there is no data
          if (t == 0 && data.length == 1) {
            t = -1;
          }
          break;
        case INTERMODES:
          t = Auto_Threshold.Intermodes(data);
          break;
        case ISO_DATA:
          // Work around bug in isodata for small histograms
          if (data.length < 4) {
            final int[] newData = new int[7];
            System.arraycopy(data, 0, newData, 4, data.length);
            t = Auto_Threshold.IsoData(newData) - 4;
          } else {
            t = Auto_Threshold.IsoData(data);
          }
          break;
        case LI:
          t = Auto_Threshold.Li(data);
          break;
        case MAX_ENTROPY:
          t = Auto_Threshold.MaxEntropy(data);
          break;
        case MEAN:
          t = Auto_Threshold.Mean(data);
          break;
        case MEAN_PLUS_STD_DEV:
          final double m = AutoThreshold.getStdDevMultiplier();
          final Statistics stats = new Statistics();
          for (int i = 0; i < data.length; i++) {
            stats.add(data[i], i);
          }
          t = MathUtils.clip(0, data.length - 1,
              (int) Math.floor(stats.getMean() + m * stats.getStandardDeviation()));
          break;
        case MINIMUM:
          // Work around bug in minimum
          if (data.length < 3) {
            t = -1;
          } else {
            t = Auto_Threshold.Minimum(data);
          }
          break;
        case MIN_ERROR_I:
          t = Auto_Threshold.MinErrorI(data);
          break;
        case MOMENTS:
          t = Auto_Threshold.Moments(data);
          break;
        case NONE:
          t = -1;
          break;
        case OTSU:
          t = Auto_Threshold.Otsu(data);
          break;
        case PERCENTILE:
          t = Auto_Threshold.Percentile(data);
          break;
        case RENYI_ENTROPY:
          t = Auto_Threshold.RenyiEntropy(data);
          break;
        case SHANBHAG:
          t = Auto_Threshold.Shanbhag(data);
          break;
        case TRIANGLE:
          t = Auto_Threshold.Triangle(data);
          break;
        case YEN:
          t = Auto_Threshold.Yen(data);
          break;
        default:
          throw new RuntimeException("unknown method: " + method);
      }
    } finally {
      System.setOut(orig);
    }
    return t;
  }

  private static void assertThresholdValue(int expected, int offset, int actual,
      Supplier<String> msg) {
    if (expected < 0) {
      Assertions.assertEquals(0, actual, msg);
    } else {
      Assertions.assertEquals(expected + offset, actual, msg);
    }
  }
}
