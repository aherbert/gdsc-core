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
 * Copyright (C) 2011 - 2020 Alex Herbert
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
import java.util.Arrays;
import java.util.EnumSet;
import java.util.function.Supplier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.core.threshold.AutoThreshold.Method;
import uk.ac.sussex.gdsc.core.utils.MathUtils;
import uk.ac.sussex.gdsc.core.utils.Statistics;

@SuppressWarnings({"javadoc"})
public class AutoThresholdTest {
  private static final Method[] methods = Method.values();

  @Test
  public void canThreshold() {
    assertThreshold(new int[] {1});
    assertThreshold(new int[] {6, 7, 8});
    assertThreshold(new int[] {1, 1, 4, 16, 7, 3, 1, 1, 1, 1, 1});
    assertThreshold(new int[] {1, 1, 4, 16, 7, 3, 1, 0, 1, 0, 1});
  }

  @Test
  public void canGetMethods() {
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
  public void canGetMethodsWithoutNone() {
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
  public void canChangeStdDevMultiplier() {
    final int[] data = new int[] {1, 1, 4, 16, 7, 3, 1, 1, 1, 1, 1};
    final AutoThreshold thresholder = new AutoThreshold();
    final double multiplier = AutoThreshold.getStdDevMultiplier();
    try {
      for (final double m : new double[] {2, 3, 4}) {
        AutoThreshold.setStdDevMultiplier(m);
        Assertions.assertEquals(m, AutoThreshold.getStdDevMultiplier());
        Assertions.assertEquals(thresholder.meanPlusStdDev(data, m),
            AutoThreshold.meanPlusStdDev(data));
      }
    } finally {
      AutoThreshold.setStdDevMultiplier(multiplier);
    }
  }

  private static void assertThreshold(int[] histogram) {
    final int pad = 10;
    final int[] h1 = Arrays.copyOf(histogram, histogram.length + pad);
    final int[] h2 = new int[h1.length];
    System.arraycopy(histogram, 0, h2, pad, histogram.length);
    for (final Method method : methods) {
      final int expected = getThreshold(method, histogram);
      int actual = AutoThreshold.getThreshold(method, histogram);
      assertThresholdValue(expected, 0, actual, () -> method + " " + Arrays.toString(histogram));
      actual = AutoThreshold.getThreshold(method.toString(), histogram);
      assertThresholdValue(expected, 0, actual, () -> method + " " + Arrays.toString(histogram));
      // Zero-pad
      actual = AutoThreshold.getThreshold(method, h1);
      assertThresholdValue(expected, 0, actual, () -> method + " " + Arrays.toString(h1));
      actual = AutoThreshold.getThreshold(method, h2);
      assertThresholdValue(expected, pad, actual, () -> method + " " + Arrays.toString(h2));
    }
  }

  private static int getThreshold(Method method, int[] data) {
    int t = 0;
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
        t = Auto_Threshold.IsoData(data);
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
