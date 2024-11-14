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
 * Copyright (C) 2011 - 2023 Alex Herbert
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

package uk.ac.sussex.gdsc.core.utils;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.function.DoubleUnaryOperator;
import org.apache.commons.numbers.core.Sum;
import org.apache.commons.numbers.gamma.Erf;
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.statistics.descriptive.Mean;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.rng.RngFactory;
import uk.ac.sussex.gdsc.test.utils.RandomSeed;

@SuppressWarnings({"javadoc"})
class MathUtilsTest {
  @Test
  void testLimitsDouble() {
    final double[] noLimits = {Double.NaN, Double.NaN};
    Assertions.assertArrayEquals(noLimits, MathUtils.limits((double[]) null));
    Assertions.assertArrayEquals(noLimits, MathUtils.limits(new double[0]));
    Assertions.assertArrayEquals(noLimits, MathUtils.limits(null, new double[0]));
    Assertions.assertArrayEquals(noLimits, MathUtils.limits(null, (double[]) null));
    Assertions.assertArrayEquals(noLimits, MathUtils.limits(new double[0], (double[]) null));
    Assertions.assertArrayEquals(new double[] {4, 8},
        MathUtils.limits(new double[] {4, 8}, (double[]) null));
    Assertions.assertArrayEquals(new double[] {3, 7},
        MathUtils.limits(new double[] {7, 6, 4, 3, 5}));
    Assertions.assertArrayEquals(new double[] {3, 7},
        MathUtils.limits(new double[] {3, 7, 6, 4, 5}));
    Assertions.assertArrayEquals(new double[] {3, 7},
        MathUtils.limits(new double[] {4, 4}, new double[] {3, 7, 6, 4, 5}));
    Assertions.assertArrayEquals(new double[] {3, 7},
        MathUtils.limits(new double[0], new double[] {3, 7, 6, 4, 5}));
    Assertions.assertArrayEquals(new double[] {3, 7},
        MathUtils.limits((double[]) null, new double[] {3, 7, 6, 4, 5}));
    Assertions.assertArrayEquals(new double[] {2, 10},
        MathUtils.limits(new double[] {2, 10}, new double[] {3, 7, 6, 4, 5}));
    Assertions.assertArrayEquals(new double[] {2, 10},
        MathUtils.limits(new double[] {10, 2}, new double[] {3, 7, 6, 4, 5}));
  }

  @Test
  void testLimitsFloat() {
    final float[] noLimits = {Float.NaN, Float.NaN};
    Assertions.assertArrayEquals(noLimits, MathUtils.limits((float[]) null));
    Assertions.assertArrayEquals(noLimits, MathUtils.limits(new float[0]));
    Assertions.assertArrayEquals(noLimits, MathUtils.limits(null, new float[0]));
    Assertions.assertArrayEquals(noLimits, MathUtils.limits(null, (float[]) null));
    Assertions.assertArrayEquals(noLimits, MathUtils.limits(new float[0], (float[]) null));
    Assertions.assertArrayEquals(new float[] {4, 8},
        MathUtils.limits(new float[] {4, 8}, (float[]) null));
    Assertions.assertArrayEquals(new float[] {3, 7}, MathUtils.limits(new float[] {7, 6, 4, 3, 5}));
    Assertions.assertArrayEquals(new float[] {3, 7}, MathUtils.limits(new float[] {3, 7, 6, 4, 5}));
    Assertions.assertArrayEquals(new float[] {3, 7},
        MathUtils.limits(new float[] {4, 4}, new float[] {3, 7, 6, 4, 5}));
    Assertions.assertArrayEquals(new float[] {3, 7},
        MathUtils.limits(new float[0], new float[] {3, 7, 6, 4, 5}));
    Assertions.assertArrayEquals(new float[] {3, 7},
        MathUtils.limits((float[]) null, new float[] {3, 7, 6, 4, 5}));
    Assertions.assertArrayEquals(new float[] {2, 10},
        MathUtils.limits(new float[] {2, 10}, new float[] {3, 7, 6, 4, 5}));
    Assertions.assertArrayEquals(new float[] {2, 10},
        MathUtils.limits(new float[] {10, 2}, new float[] {3, 7, 6, 4, 5}));
  }

  @Test
  void testLimitsInt() {
    final int[] noLimits = {0, 0};
    Assertions.assertArrayEquals(noLimits, MathUtils.limits((int[]) null));
    Assertions.assertArrayEquals(noLimits, MathUtils.limits(new int[0]));
    Assertions.assertArrayEquals(noLimits, MathUtils.limits(null, new int[0]));
    Assertions.assertArrayEquals(noLimits, MathUtils.limits(null, (int[]) null));
    Assertions.assertArrayEquals(noLimits, MathUtils.limits(new int[0], (int[]) null));
    Assertions.assertArrayEquals(new int[] {4, 8},
        MathUtils.limits(new int[] {4, 8}, (int[]) null));
    Assertions.assertArrayEquals(new int[] {3, 7}, MathUtils.limits(new int[] {7, 6, 4, 3, 5}));
    Assertions.assertArrayEquals(new int[] {3, 7}, MathUtils.limits(new int[] {3, 7, 6, 4, 5}));
    Assertions.assertArrayEquals(new int[] {3, 7},
        MathUtils.limits(new int[] {4, 4}, new int[] {3, 7, 6, 4, 5}));
    Assertions.assertArrayEquals(new int[] {3, 7},
        MathUtils.limits(new int[0], new int[] {3, 7, 6, 4, 5}));
    Assertions.assertArrayEquals(new int[] {3, 7},
        MathUtils.limits((int[]) null, new int[] {3, 7, 6, 4, 5}));
    Assertions.assertArrayEquals(new int[] {2, 10},
        MathUtils.limits(new int[] {2, 10}, new int[] {3, 7, 6, 4, 5}));
    Assertions.assertArrayEquals(new int[] {2, 10},
        MathUtils.limits(new int[] {10, 2}, new int[] {3, 7, 6, 4, 5}));
  }

  @Test
  void testLimitsLong() {
    final long[] noLimits = {0, 0};
    Assertions.assertArrayEquals(noLimits, MathUtils.limits((long[]) null));
    Assertions.assertArrayEquals(noLimits, MathUtils.limits(new long[0]));
    Assertions.assertArrayEquals(noLimits, MathUtils.limits(null, new long[0]));
    Assertions.assertArrayEquals(noLimits, MathUtils.limits(null, (long[]) null));
    Assertions.assertArrayEquals(noLimits, MathUtils.limits(new long[0], (long[]) null));
    Assertions.assertArrayEquals(new long[] {4, 8},
        MathUtils.limits(new long[] {4, 8}, (long[]) null));
    Assertions.assertArrayEquals(new long[] {3, 7}, MathUtils.limits(new long[] {7, 6, 4, 3, 5}));
    Assertions.assertArrayEquals(new long[] {3, 7}, MathUtils.limits(new long[] {3, 7, 6, 4, 5}));
    Assertions.assertArrayEquals(new long[] {3, 7},
        MathUtils.limits(new long[] {4, 4}, new long[] {3, 7, 6, 4, 5}));
    Assertions.assertArrayEquals(new long[] {3, 7},
        MathUtils.limits(new long[0], new long[] {3, 7, 6, 4, 5}));
    Assertions.assertArrayEquals(new long[] {3, 7},
        MathUtils.limits((long[]) null, new long[] {3, 7, 6, 4, 5}));
    Assertions.assertArrayEquals(new long[] {2, 10},
        MathUtils.limits(new long[] {2, 10}, new long[] {3, 7, 6, 4, 5}));
    Assertions.assertArrayEquals(new long[] {2, 10},
        MathUtils.limits(new long[] {10, 2}, new long[] {3, 7, 6, 4, 5}));
  }

  @Test
  void testLimitsShort() {
    final short[] noLimits = {0, 0};
    Assertions.assertArrayEquals(noLimits, MathUtils.limits((short[]) null));
    Assertions.assertArrayEquals(noLimits, MathUtils.limits(new short[0]));
    Assertions.assertArrayEquals(noLimits, MathUtils.limits(null, new short[0]));
    Assertions.assertArrayEquals(noLimits, MathUtils.limits(null, (short[]) null));
    Assertions.assertArrayEquals(noLimits, MathUtils.limits(new short[0], (short[]) null));
    Assertions.assertArrayEquals(new short[] {4, 8},
        MathUtils.limits(new short[] {4, 8}, (short[]) null));
    Assertions.assertArrayEquals(new short[] {3, 7}, MathUtils.limits(new short[] {7, 6, 4, 3, 5}));
    Assertions.assertArrayEquals(new short[] {3, 7}, MathUtils.limits(new short[] {3, 7, 6, 4, 5}));
    Assertions.assertArrayEquals(new short[] {3, 7},
        MathUtils.limits(new short[] {4, 4}, new short[] {3, 7, 6, 4, 5}));
    Assertions.assertArrayEquals(new short[] {3, 7},
        MathUtils.limits(new short[0], new short[] {3, 7, 6, 4, 5}));
    Assertions.assertArrayEquals(new short[] {3, 7},
        MathUtils.limits((short[]) null, new short[] {3, 7, 6, 4, 5}));
    Assertions.assertArrayEquals(new short[] {2, 10},
        MathUtils.limits(new short[] {2, 10}, new short[] {3, 7, 6, 4, 5}));
    Assertions.assertArrayEquals(new short[] {2, 10},
        MathUtils.limits(new short[] {10, 2}, new short[] {3, 7, 6, 4, 5}));
  }

  @Test
  void testMaxDouble() {
    final double noMax = Double.NaN;
    Assertions.assertEquals(noMax, MathUtils.max((double[]) null));
    Assertions.assertEquals(noMax, MathUtils.max(new double[0]));
    for (final double value : new double[] {Double.NaN, Double.NEGATIVE_INFINITY, -42}) {
      Assertions.assertEquals(value, MathUtils.maxDefault(value, new double[0]));
      Assertions.assertEquals(value, MathUtils.maxDefault(value, (double[]) null));
    }
    Assertions.assertEquals(7, MathUtils.max(new double[] {7, 6, 4, 3, 5}));
    Assertions.assertEquals(7, MathUtils.max(new double[] {3, 7, 6, 4, 5}));
  }

  @Test
  void testMaxFloat() {
    final float noMax = Float.NaN;
    Assertions.assertEquals(noMax, MathUtils.max((float[]) null));
    Assertions.assertEquals(noMax, MathUtils.max(new float[0]));
    for (final float value : new float[] {Float.NaN, Float.NEGATIVE_INFINITY, -42}) {
      Assertions.assertEquals(value, MathUtils.maxDefault(value, new float[0]));
      Assertions.assertEquals(value, MathUtils.maxDefault(value, (float[]) null));
    }
    Assertions.assertEquals(7, MathUtils.max(new float[] {7, 6, 4, 3, 5}));
    Assertions.assertEquals(7, MathUtils.max(new float[] {3, 7, 6, 4, 5}));
  }

  @Test
  void testMaxInt() {
    final int noMax = Integer.MIN_VALUE;
    Assertions.assertEquals(noMax, MathUtils.max((int[]) null));
    Assertions.assertEquals(noMax, MathUtils.max(new int[0]));
    for (final int value : new int[] {Integer.MIN_VALUE, -42, 13}) {
      Assertions.assertEquals(value, MathUtils.maxDefault(value, new int[0]));
      Assertions.assertEquals(value, MathUtils.maxDefault(value, (int[]) null));
    }
    Assertions.assertEquals(7, MathUtils.max(new int[] {7, 6, 4, 3, 5}));
    Assertions.assertEquals(7, MathUtils.max(new int[] {3, 7, 6, 4, 5}));
  }

  @Test
  void testMaxLong() {
    final long noMax = Long.MIN_VALUE;
    Assertions.assertEquals(noMax, MathUtils.max((long[]) null));
    Assertions.assertEquals(noMax, MathUtils.max(new long[0]));
    for (final long value : new long[] {Long.MIN_VALUE, -42, 13}) {
      Assertions.assertEquals(value, MathUtils.maxDefault(value, new long[0]));
      Assertions.assertEquals(value, MathUtils.maxDefault(value, (long[]) null));
    }
    Assertions.assertEquals(7, MathUtils.max(new long[] {7, 6, 4, 3, 5}));
    Assertions.assertEquals(7, MathUtils.max(new long[] {3, 7, 6, 4, 5}));
  }

  @Test
  void testMaxShort() {
    final short noMax = Short.MIN_VALUE;
    Assertions.assertEquals(noMax, MathUtils.max((short[]) null));
    Assertions.assertEquals(noMax, MathUtils.max(new short[0]));
    for (final short value : new short[] {Short.MIN_VALUE, -42, 13}) {
      Assertions.assertEquals(value, MathUtils.maxDefault(value, new short[0]));
      Assertions.assertEquals(value, MathUtils.maxDefault(value, (short[]) null));
    }
    Assertions.assertEquals(7, MathUtils.max(new short[] {7, 6, 4, 3, 5}));
    Assertions.assertEquals(7, MathUtils.max(new short[] {3, 7, 6, 4, 5}));
  }

  @Test
  void testMinDouble() {
    final double noMin = Double.NaN;
    Assertions.assertEquals(noMin, MathUtils.min((double[]) null));
    Assertions.assertEquals(noMin, MathUtils.min(new double[0]));
    for (final double value : new double[] {Double.NaN, Double.NEGATIVE_INFINITY, -42}) {
      Assertions.assertEquals(value, MathUtils.minDefault(value, new double[0]));
      Assertions.assertEquals(value, MathUtils.minDefault(value, (double[]) null));
    }
    Assertions.assertEquals(3, MathUtils.min(new double[] {7, 6, 4, 3, 5}));
    Assertions.assertEquals(3, MathUtils.min(new double[] {3, 7, 6, 4, 5}));
  }

  @Test
  void testMinFloat() {
    final float noMin = Float.NaN;
    Assertions.assertEquals(noMin, MathUtils.min((float[]) null));
    Assertions.assertEquals(noMin, MathUtils.min(new float[0]));
    for (final float value : new float[] {Float.NaN, Float.NEGATIVE_INFINITY, -42}) {
      Assertions.assertEquals(value, MathUtils.minDefault(value, new float[0]));
      Assertions.assertEquals(value, MathUtils.minDefault(value, (float[]) null));
    }
    Assertions.assertEquals(3, MathUtils.min(new float[] {7, 6, 4, 3, 5}));
    Assertions.assertEquals(3, MathUtils.min(new float[] {3, 7, 6, 4, 5}));
  }

  @Test
  void testMinInt() {
    final int noMin = Integer.MAX_VALUE;
    Assertions.assertEquals(noMin, MathUtils.min((int[]) null));
    Assertions.assertEquals(noMin, MathUtils.min(new int[0]));
    for (final int value : new int[] {Integer.MAX_VALUE, -42, 13}) {
      Assertions.assertEquals(value, MathUtils.minDefault(value, new int[0]));
      Assertions.assertEquals(value, MathUtils.minDefault(value, (int[]) null));
    }
    Assertions.assertEquals(3, MathUtils.min(new int[] {7, 6, 4, 3, 5}));
    Assertions.assertEquals(3, MathUtils.min(new int[] {3, 7, 6, 4, 5}));
  }

  @Test
  void testMinLong() {
    final long noMin = Long.MAX_VALUE;
    Assertions.assertEquals(noMin, MathUtils.min((long[]) null));
    Assertions.assertEquals(noMin, MathUtils.min(new long[0]));
    for (final long value : new long[] {Long.MAX_VALUE, -42, 13}) {
      Assertions.assertEquals(value, MathUtils.minDefault(value, new long[0]));
      Assertions.assertEquals(value, MathUtils.minDefault(value, (long[]) null));
    }
    Assertions.assertEquals(3, MathUtils.min(new long[] {7, 6, 4, 3, 5}));
    Assertions.assertEquals(3, MathUtils.min(new long[] {3, 7, 6, 4, 5}));
  }

  @Test
  void testMinShort() {
    final short noMin = Short.MAX_VALUE;
    Assertions.assertEquals(noMin, MathUtils.min((short[]) null));
    Assertions.assertEquals(noMin, MathUtils.min(new short[0]));
    for (final short value : new short[] {Short.MAX_VALUE, -42, 13}) {
      Assertions.assertEquals(value, MathUtils.minDefault(value, new short[0]));
      Assertions.assertEquals(value, MathUtils.minDefault(value, (short[]) null));
    }
    Assertions.assertEquals(3, MathUtils.min(new short[] {7, 6, 4, 3, 5}));
    Assertions.assertEquals(3, MathUtils.min(new short[] {3, 7, 6, 4, 5}));
  }

  @Test
  void testCumulativeHistogram() {
    Assertions.assertArrayEquals(new double[2][0], MathUtils.cumulativeHistogram(null, false));
    Assertions.assertArrayEquals(new double[2][0],
        MathUtils.cumulativeHistogram(new double[0], false));
    Assertions.assertArrayEquals(new double[2][0],
        MathUtils.cumulativeHistogram(new double[] {Double.NaN}, false));
    Assertions.assertArrayEquals(new double[][] {{1, 3, 4, 6}, {1, 2, 3, 4}},
        MathUtils.cumulativeHistogram(new double[] {1, 3, 4, 6}, false));
    Assertions.assertArrayEquals(new double[][] {{1, 2, 3, 4, 5, 6}, {1, 3, 4, 7, 8, 10}},
        MathUtils.cumulativeHistogram(new double[] {1, 2, 2, 3, 4, 4, 4, 5, 6, 6}, false));
    Assertions.assertArrayEquals(new double[][] {{1, 2, 3, 4, 5, 6}, {1, 3, 4, 7, 8, 10}},
        MathUtils.cumulativeHistogram(
            new double[] {1, 2, 2, 3, Double.NaN, 4, 4, 4, 5, 6, 6, Double.NaN}, false));
    Assertions.assertArrayEquals(
        new double[][] {{1, 2, 3, 4, 5, 6}, {0.1, 0.3, 0.4, 0.7, 0.8, 1.0}}, MathUtils
            .cumulativeHistogram(new double[] {1, 2, Double.NaN, 2, 3, 4, 4, 4, 5, 6, 6}, true));
  }

  @Test
  void canGetLogLikelihoodFromResidualSumOfSquares() {
    for (final int n : new int[] {34, 67}) {
      for (final double rss : new double[] {456.78, 98.123}) {
        final double expected =
            -n * Math.log(2 * Math.PI) / 2 - n * Math.log(rss / n) / 2 - n / 2.0;
        Assertions.assertEquals(expected, MathUtils.getLogLikelihood(rss, n),
            Math.abs(expected) * 1e-8);
      }
    }
  }

  @Test
  void canComputeAic() {
    for (final int k : new int[] {3, 6}) {
      for (final double ll : new double[] {-456.78, 98.123}) {
        final double expected = 2 * k - 2 * ll;
        Assertions.assertEquals(expected, MathUtils.getAkaikeInformationCriterion(ll, k));
      }
    }
  }

  @Test
  void canComputeAicc() {
    for (final int n : new int[] {13, 42}) {
      for (final int k : new int[] {3, 6}) {
        for (final double ll : new double[] {-456.78, 98.123}) {
          double expected = 2 * k - 2 * ll;
          // adjust
          expected += (2.0 * k * k + 2 * k) / (n - k - 1);
          Assertions.assertEquals(expected, MathUtils.getAkaikeInformationCriterion(ll, n, k));
        }
      }
    }
  }

  @Test
  void canComputeBic() {
    for (final int n : new int[] {13, 42}) {
      for (final int k : new int[] {3, 6}) {
        for (final double ll : new double[] {-456.78, 98.123}) {
          final double expected = k * Math.log(n) - 2 * ll;
          Assertions.assertEquals(expected, MathUtils.getBayesianInformationCriterion(ll, n, k));
        }
      }
    }
  }

  @Test
  void canComputeAdjustedR2() {
    for (final int n : new int[] {13, 42}) {
      for (final int k : new int[] {3, 6}) {
        for (final double rss : new double[] {-456.78, 98.123}) {
          for (final double tss : new double[] {-456.78, 98.123}) {
            final double expected = 1 - (rss / tss) * ((double) (n - 1) / (n - k - 1));
            Assertions.assertEquals(expected,
                MathUtils.getAdjustedCoefficientOfDetermination(rss, tss, n, k));
          }
        }
      }
    }
  }

  @SeededTest
  void canGetTotalSumOfSquares(RandomSeed seed) {
    final UniformRandomProvider r = RngFactory.create(seed.get());
    final double[] values = new double[7];
    for (int i = 1; i <= 10; i++) {
      for (int j = 0; j < values.length; j++) {
        values[j] = r.nextDouble() * i;
      }
      final double expected = secondMoment(values);
      Assertions.assertEquals(expected, MathUtils.getTotalSumOfSquares(values), expected * 1e-8);
    }
  }

  private static double secondMoment(double[] data) {
    final double m = Mean.of(data).getAsDouble();
    final Sum s = Sum.create();
    for (final double d : data) {
      final double dx = d - m;
      s.add(dx * dx);
    }
    return s.getAsDouble();
  }

  @Test
  void testSumDouble() {
    Assertions.assertEquals(0, MathUtils.sum((double[]) null));
    Assertions.assertEquals(0, MathUtils.sum(new double[0]));
    Assertions.assertEquals(0, MathUtils.sum(new double[2]));
    Assertions.assertEquals(3, MathUtils.sum(new double[] {1, 2, 3, -2, -1}));
  }

  @Test
  void testSumFloat() {
    Assertions.assertEquals(0, MathUtils.sum((float[]) null));
    Assertions.assertEquals(0, MathUtils.sum(new float[0]));
    Assertions.assertEquals(0, MathUtils.sum(new float[2]));
    Assertions.assertEquals(3, MathUtils.sum(new float[] {1, 2, 3, -2, -1}));
  }

  @Test
  void testSumLong() {
    Assertions.assertEquals(0, MathUtils.sum((long[]) null));
    Assertions.assertEquals(0, MathUtils.sum(new long[0]));
    Assertions.assertEquals(0, MathUtils.sum(new long[2]));
    Assertions.assertEquals(3, MathUtils.sum(new long[] {1, 2, 3, -2, -1}));
  }

  @Test
  void testSumInt() {
    Assertions.assertEquals(0, MathUtils.sum((int[]) null));
    Assertions.assertEquals(0, MathUtils.sum(new int[0]));
    Assertions.assertEquals(0, MathUtils.sum(new int[2]));
    Assertions.assertEquals(3, MathUtils.sum(new int[] {1, 2, 3, -2, -1}));
  }

  @Test
  void testRoundedToSignificantDigits() {
    for (final double value : new double[] {Double.NaN, Double.NEGATIVE_INFINITY,
        Double.POSITIVE_INFINITY}) {
      Assertions.assertEquals(String.valueOf(value), MathUtils.rounded(value, 3));
    }
    Assertions.assertEquals("1.0", MathUtils.rounded(1.1234567, 1));
    Assertions.assertEquals("1.1", MathUtils.rounded(1.1234567, 2));
    Assertions.assertEquals("1.12", MathUtils.rounded(1.1234567, 3));
    Assertions.assertEquals("1.123", MathUtils.rounded(1.1234567, 4));
    Assertions.assertEquals("1.123", MathUtils.rounded(1.1234567));
    Assertions.assertEquals("1.1235", MathUtils.rounded(1.1234567, 5));
    Assertions.assertEquals("1.12346", MathUtils.rounded(1.1234567, 6));

    Assertions.assertEquals("1.1E100", MathUtils.rounded(1.1234567e100, 2));
    Assertions.assertEquals("1.12E100", MathUtils.rounded(1.1234567e100, 3));
    Assertions.assertEquals("0.1", MathUtils.rounded(0.1, 1));
    Assertions.assertEquals("0.1", MathUtils.rounded(0.1, 5));
  }

  @Test
  void testRoundToSignificantDigits() {
    for (final double value : new double[] {Double.NaN, Double.NEGATIVE_INFINITY,
        Double.POSITIVE_INFINITY}) {
      Assertions.assertEquals(value, MathUtils.round(value, 3));
    }
    Assertions.assertEquals(1.0, MathUtils.round(1.1234567, 1));
    Assertions.assertEquals(1.1, MathUtils.round(1.1234567, 2));
    Assertions.assertEquals(1.12, MathUtils.round(1.1234567, 3));
    Assertions.assertEquals(1.123, MathUtils.round(1.1234567, 4));
    Assertions.assertEquals(1.123, MathUtils.round(1.1234567));
    Assertions.assertEquals(1.1235, MathUtils.round(1.1234567, 5));
    Assertions.assertEquals(1.12346, MathUtils.round(1.1234567, 6));
  }

  @Test
  void testRoundToFactor() {
    for (final double value : new double[] {Double.NaN, Double.NEGATIVE_INFINITY,
        Double.POSITIVE_INFINITY}) {
      Assertions.assertEquals(value, MathUtils.round(value, 3.0));
    }
    Assertions.assertEquals(1.0, MathUtils.round(1.1234567, 1.0));
    Assertions.assertEquals(2.0, MathUtils.round(1.1234567, 2.0));
    Assertions.assertEquals(1.1, MathUtils.round(1.1234567, 0.1));
    Assertions.assertEquals(1.1, MathUtils.round(1.1234567, 0.05));
    Assertions.assertEquals(1.12, MathUtils.round(1.1234567, 0.01));
    Assertions.assertEquals(1.123, MathUtils.round(1.1234567, 0.001));
  }

  @Test
  void testRoundToBigDecimalSignificantDigits() {
    for (final double value : new double[] {Double.NaN, Double.NEGATIVE_INFINITY,
        Double.POSITIVE_INFINITY}) {
      Assertions.assertThrows(NumberFormatException.class,
          () -> MathUtils.roundToBigDecimal(value, 3));
    }
    Assertions.assertEquals(new BigDecimal("1"), MathUtils.roundToBigDecimal(1.1234567, 1));
    Assertions.assertEquals(new BigDecimal("1.1"), MathUtils.roundToBigDecimal(1.1234567, 2));
    Assertions.assertEquals(new BigDecimal("1.12"), MathUtils.roundToBigDecimal(1.1234567, 3));
    Assertions.assertEquals(new BigDecimal("1.123"), MathUtils.roundToBigDecimal(1.1234567, 4));
    Assertions.assertEquals(new BigDecimal("1.1235"), MathUtils.roundToBigDecimal(1.1234567, 5));
    Assertions.assertEquals(new BigDecimal("1.12346"), MathUtils.roundToBigDecimal(1.1234567, 6));
  }

  @Test
  void canRoundUsingDecimalPlaces() {
    for (final double value : new double[] {Double.NaN, Double.NEGATIVE_INFINITY,
        Double.POSITIVE_INFINITY}) {
      Assertions.assertEquals(value, MathUtils.roundUsingDecimalPlaces(value, 3));
    }
    Assertions.assertEquals(1.0, MathUtils.roundUsingDecimalPlaces(1.1234567, 0));
    Assertions.assertEquals(1.1, MathUtils.roundUsingDecimalPlaces(1.1234567, 1));
    Assertions.assertEquals(1.12, MathUtils.roundUsingDecimalPlaces(1.1234567, 2));
    Assertions.assertEquals(1.123, MathUtils.roundUsingDecimalPlaces(1.1234567, 3));
    Assertions.assertEquals(1.1235, MathUtils.roundUsingDecimalPlaces(1.1234567, 4));
    Assertions.assertEquals(1.12346, MathUtils.roundUsingDecimalPlaces(1.1234567, 5));
  }

  @Test
  void canRoundUsingNegativeDecimalPlaces() {
    Assertions.assertEquals(123.0, MathUtils.roundUsingDecimalPlaces(123, 2));
    Assertions.assertEquals(123.0, MathUtils.roundUsingDecimalPlaces(123, 1));
    Assertions.assertEquals(123, MathUtils.roundUsingDecimalPlaces(123, 0));
    Assertions.assertEquals(120, MathUtils.roundUsingDecimalPlaces(123, -1));
    Assertions.assertEquals(100, MathUtils.roundUsingDecimalPlaces(123, -2));
    Assertions.assertEquals(0, MathUtils.roundUsingDecimalPlaces(123, -3));
    Assertions.assertEquals(0, MathUtils.roundUsingDecimalPlaces(123, -4));
    Assertions.assertEquals(1000, MathUtils.roundUsingDecimalPlaces(523, -3));
  }

  @SeededTest
  void canRoundToBigDecimalUsingDecimalPlaces(RandomSeed seed) {
    // 0.1 cannot be an exact double (see constructor documentation for BigDecimal)
    double value = 0.1;
    BigDecimal bd = new BigDecimal(value);
    Assertions.assertNotEquals("0.1", bd.toPlainString());
    Assertions.assertEquals("0.1",
        MathUtils.roundUsingDecimalPlacesToBigDecimal(value, 1).toPlainString());

    // Random test that rounding does the same as String.format
    final UniformRandomProvider r = RngFactory.create(seed.get());
    for (int i = 0; i < 10; i++) {
      final String format = "%." + i + "f";
      for (int j = 0; j < 10; j++) {
        value = r.nextDouble();
        final String e = String.format(format, value);
        bd = MathUtils.roundUsingDecimalPlacesToBigDecimal(value, i);
        Assertions.assertEquals(e, bd.toPlainString());
      }
    }
  }

  @Test
  void canRoundToBigDecimalUsingNegativeDecimalPlaces() {
    Assertions.assertEquals("123.0",
        MathUtils.roundUsingDecimalPlacesToBigDecimal(123, 2).toPlainString());
    Assertions.assertEquals("123.0",
        MathUtils.roundUsingDecimalPlacesToBigDecimal(123, 1).toPlainString());
    Assertions.assertEquals("123",
        MathUtils.roundUsingDecimalPlacesToBigDecimal(123, 0).toPlainString());
    Assertions.assertEquals("120",
        MathUtils.roundUsingDecimalPlacesToBigDecimal(123, -1).toPlainString());
    Assertions.assertEquals("100",
        MathUtils.roundUsingDecimalPlacesToBigDecimal(123, -2).toPlainString());
    Assertions.assertEquals("0",
        MathUtils.roundUsingDecimalPlacesToBigDecimal(123, -3).toPlainString());
    Assertions.assertEquals("0",
        MathUtils.roundUsingDecimalPlacesToBigDecimal(123, -4).toPlainString());
    Assertions.assertEquals("1000",
        MathUtils.roundUsingDecimalPlacesToBigDecimal(523, -3).toPlainString());
  }

  @Test
  void testFloorToFactor() {
    for (final double value : new double[] {Double.NaN, Double.NEGATIVE_INFINITY,
        Double.POSITIVE_INFINITY}) {
      Assertions.assertEquals(value, MathUtils.floor(value, 3.0));
    }
    Assertions.assertEquals(1.0, MathUtils.floor(1.1234567, 1.0));
    Assertions.assertEquals(0.0, MathUtils.floor(1.1234567, 2.0));
    Assertions.assertEquals(1.1, MathUtils.floor(1.1234567, 0.1));
    Assertions.assertEquals(1.1, MathUtils.floor(1.1234567, 0.05));
    Assertions.assertEquals(1.12, MathUtils.floor(1.1234567, 0.01));
    Assertions.assertEquals(1.123, MathUtils.floor(1.1234567, 0.001));
  }

  @Test
  void testCeilToFactor() {
    for (final double value : new double[] {Double.NaN, Double.NEGATIVE_INFINITY,
        Double.POSITIVE_INFINITY}) {
      Assertions.assertEquals(value, MathUtils.ceil(value, 3.0));
    }
    Assertions.assertEquals(2.0, MathUtils.ceil(1.1234567, 1.0));
    Assertions.assertEquals(2.0, MathUtils.ceil(1.1234567, 2.0));
    Assertions.assertEquals(1.2, MathUtils.ceil(1.1234567, 0.1), 1e-6);
    Assertions.assertEquals(1.15, MathUtils.ceil(1.1234567, 0.05), 1e-6);
    Assertions.assertEquals(1.13, MathUtils.ceil(1.1234567, 0.01), 1e-6);
    Assertions.assertEquals(1.124, MathUtils.ceil(1.1234567, 0.001), 1e-6);
  }

  @Test
  void canInterpolateY() {
    Assertions.assertEquals(3.5, MathUtils.interpolateY(1, 2, 3, 5, 2));
    Assertions.assertEquals(6.5, MathUtils.interpolateY(1, 2, 3, 5, 4));
    Assertions.assertEquals(-1, MathUtils.interpolateY(1, 2, 3, 5, -1));
    Assertions.assertEquals(-2.5, MathUtils.interpolateY(1, 2, 3, 5, -2));
  }

  @Test
  void canInterpolateX() {
    Assertions.assertEquals(5.0 / 3, MathUtils.interpolateX(1, 2, 3, 5, 3));
    Assertions.assertEquals(11.0 / 3, MathUtils.interpolateX(1, 2, 3, 5, 6));
    Assertions.assertEquals(-1, MathUtils.interpolateX(1, 2, 3, 5, -1));
    Assertions.assertEquals(-5.0 / 3, MathUtils.interpolateX(1, 2, 3, 5, -2));
  }

  @Test
  void testDistanceDouble() {
    Assertions.assertEquals(5, MathUtils.distance(1.0, 2, 4, 6));
  }

  @Test
  void testDistanceThreeDDouble() {
    Assertions.assertEquals(Math.sqrt(2 * 5 * 5), MathUtils.distance(1.0, 2, 3, 4, 6, 8));
  }

  @Test
  void testDistanceFloat() {
    Assertions.assertEquals(5, MathUtils.distance(1f, 2, 4, 6));
  }

  @Test
  void testDistance2Double() {
    Assertions.assertEquals(5 * 5, MathUtils.distance2(1.0, 2, 4, 6));
  }

  @Test
  void testDistance2ThreeDDouble() {
    Assertions.assertEquals(2 * 5 * 5, MathUtils.distance2(1.0, 2, 3, 4, 6, 8));
  }

  @Test
  void testDistance2Float() {
    Assertions.assertEquals(5 * 5, MathUtils.distance2(1f, 2, 4, 6));
  }

  @Test
  void testClipDouble() {
    Assertions.assertEquals(5, MathUtils.clip(5.0, 10.0, 3));
    Assertions.assertEquals(7, MathUtils.clip(5.0, 10.0, 7));
    Assertions.assertEquals(10, MathUtils.clip(5.0, 10.0, 11));
  }

  @Test
  void testClipFloat() {
    Assertions.assertEquals(5f, MathUtils.clip(5.0f, 10.0f, 3f));
    Assertions.assertEquals(7f, MathUtils.clip(5.0f, 10.0f, 7f));
    Assertions.assertEquals(10f, MathUtils.clip(5.0f, 10.0f, 11f));
  }

  @Test
  void testClipInt() {
    Assertions.assertEquals(5, MathUtils.clip(5, 10, 3));
    Assertions.assertEquals(7, MathUtils.clip(5, 10, 7));
    Assertions.assertEquals(10, MathUtils.clip(5, 10, 11));
  }

  @Test
  void testPow2Double() {
    Assertions.assertEquals(25, MathUtils.pow2(5.0));
    Assertions.assertEquals(25, MathUtils.pow2(-5.0));
  }

  @Test
  void testPow2Int() {
    Assertions.assertEquals(25, MathUtils.pow2(5));
    Assertions.assertEquals(25, MathUtils.pow2(-5));
  }

  @Test
  void testPow3Double() {
    Assertions.assertEquals(125, MathUtils.pow3(5.0));
    Assertions.assertEquals(-125, MathUtils.pow3(-5.0));
  }

  @Test
  void testPow3Int() {
    Assertions.assertEquals(125, MathUtils.pow3(5));
    Assertions.assertEquals(-125, MathUtils.pow3(-5));
  }

  @Test
  void testPow4Double() {
    Assertions.assertEquals(625, MathUtils.pow4(5.0));
    Assertions.assertEquals(625, MathUtils.pow4(-5.0));
  }

  @Test
  void testPow4Int() {
    Assertions.assertEquals(625, MathUtils.pow4(5));
    Assertions.assertEquals(625, MathUtils.pow4(-5));
  }

  @Test
  void testIsPow2() {
    // This is technically incorrect
    Assertions.assertTrue(MathUtils.isPow2(0));

    for (int i = 0; i < 31; i++) {
      final int value = 1 << i;
      Assertions.assertTrue(MathUtils.isPow2(value), () -> String.valueOf(value));
      Assertions.assertFalse(MathUtils.isPow2(-value), () -> String.valueOf(-value));
      if (i >= 2) {
        Assertions.assertFalse(MathUtils.isPow2(value - 1), () -> String.valueOf(value - 1));
        Assertions.assertFalse(MathUtils.isPow2(value + 1), () -> String.valueOf(value + 1));
      }
    }

    Assertions.assertTrue(MathUtils.isPow2(1 << 31));
    // This is technically incorrect
    Assertions.assertTrue(MathUtils.isPow2(-(1 << 31)));
  }

  @Test
  void testNextPow2() {
    Assertions.assertThrows(IllegalArgumentException.class, () -> MathUtils.nextPow2(0));
    Assertions.assertThrows(IllegalArgumentException.class, () -> MathUtils.nextPow2(-1));
    for (int i = 0; i < 31; i++) {
      final int value = 1 << i;
      Assertions.assertEquals(value, MathUtils.nextPow2(value), () -> String.valueOf(value));
      if (i >= 2) {
        Assertions.assertEquals(value, MathUtils.nextPow2(value - 1),
            () -> String.valueOf(value - 1));
        Assertions.assertEquals(value << 1, MathUtils.nextPow2(value + 1),
            () -> String.valueOf(value + 1));
      }
    }
    Assertions.assertEquals(1 << 31, MathUtils.nextPow2((1 << 30) + 1));
  }

  @Test
  void canComputeApproximateLog2() {
    Assertions.assertEquals(Integer.MIN_VALUE, MathUtils.log2(0));
    Assertions.assertEquals(0, MathUtils.log2(1));
    Assertions.assertEquals(1, MathUtils.log2(2));
    Assertions.assertEquals(2, MathUtils.log2(4));
    Assertions.assertEquals(8, MathUtils.log2(256));
  }

  @Test
  void canComputeLog2() {
    Assertions.assertEquals(Double.NaN, MathUtils.log2(Double.NaN));
    Assertions.assertEquals(Double.NEGATIVE_INFINITY, MathUtils.log2(-0.0));
    Assertions.assertEquals(Double.NEGATIVE_INFINITY, MathUtils.log2(0.0));
    Assertions.assertEquals(Double.POSITIVE_INFINITY, MathUtils.log2(Double.POSITIVE_INFINITY));
    Assertions.assertEquals(0.0, MathUtils.log2(1.0));
    Assertions.assertEquals(1.0, MathUtils.log2(2.0));
    Assertions.assertEquals(2.0, MathUtils.log2(4.0));
    Assertions.assertEquals(8.0, MathUtils.log2(256.0));
    for (final double d : new double[] {1.23, 4.56, 99}) {
      Assertions.assertEquals(d, MathUtils.log2(Math.pow(2.0, d)));
    }
  }

  @Test
  void testDiv0Double() {
    Assertions.assertEquals(0, MathUtils.div0(0.0, 0));
    Assertions.assertEquals(0, MathUtils.div0(0.0, 10));
    Assertions.assertEquals(1.1, MathUtils.div0(11.0, 10));
    Assertions.assertEquals(10, MathUtils.div0(100.0, 10));
  }

  @Test
  void testDiv0Float() {
    Assertions.assertEquals(0f, MathUtils.div0(0f, 0));
    Assertions.assertEquals(0f, MathUtils.div0(0f, 10));
    Assertions.assertEquals(1.1f, MathUtils.div0(11f, 10));
    Assertions.assertEquals(10f, MathUtils.div0(100f, 10));
  }

  @Test
  void testDiv0Int() {
    Assertions.assertEquals(0, MathUtils.div0(0, 0));
    Assertions.assertEquals(0, MathUtils.div0(0, 10));
    Assertions.assertEquals(1.1, MathUtils.div0(11, 10));
    Assertions.assertEquals(10, MathUtils.div0(100, 10));
  }

  @Test
  void testDiv0Long() {
    Assertions.assertEquals(0, MathUtils.div0(0L, 0));
    Assertions.assertEquals(0, MathUtils.div0(0L, 10));
    Assertions.assertEquals(1.1, MathUtils.div0(11L, 10));
    Assertions.assertEquals(10, MathUtils.div0(100L, 10));
  }

  @Test
  void testIsMathematicalInteger() {
    for (final double value : new double[] {Double.NaN, Double.NEGATIVE_INFINITY,
        Double.POSITIVE_INFINITY}) {
      Assertions.assertFalse(MathUtils.isMathematicalInteger(value));
    }
    for (int i = -1; i <= 1; i++) {
      Assertions.assertTrue(MathUtils.isMathematicalInteger(i));
      Assertions.assertFalse(MathUtils.isMathematicalInteger(i + 0.1));
    }
    // Too big for an int
    Assertions.assertTrue(MathUtils.isMathematicalInteger(1L << 52));
    Assertions.assertTrue(MathUtils.isMathematicalInteger(0x1.0p678));
    Assertions.assertFalse(MathUtils.isMathematicalInteger(0x1.0000001p4));
  }

  @Test
  void testIsIntegerDouble() {
    for (final double value : new double[] {Double.NaN, Double.NEGATIVE_INFINITY,
        Double.POSITIVE_INFINITY}) {
      Assertions.assertFalse(MathUtils.isInteger(value));
    }
    for (int i = -1; i <= 1; i++) {
      Assertions.assertTrue(MathUtils.isInteger((double) i));
      Assertions.assertFalse(MathUtils.isInteger(i + 0.1));
    }
    // Too big for an int
    Assertions.assertFalse(MathUtils.isInteger((double) (1L << 52)));
    Assertions.assertFalse(MathUtils.isInteger(0x1.0p678));
    Assertions.assertFalse(MathUtils.isInteger(0x1.0000001p4));
  }

  @Test
  void testIsIntegerFloat() {
    for (final float value : new float[] {Float.NaN, Float.NEGATIVE_INFINITY,
        Float.POSITIVE_INFINITY}) {
      Assertions.assertFalse(MathUtils.isInteger(value));
    }
    for (int i = -1; i <= 1; i++) {
      Assertions.assertTrue(MathUtils.isInteger(i));
      Assertions.assertFalse(MathUtils.isInteger(i + 0.1f));
    }
    // Too big for an int
    Assertions.assertFalse(MathUtils.isInteger(1L << 52));
    Assertions.assertFalse(MathUtils.isInteger(0x1.0p89f));
    Assertions.assertFalse(MathUtils.isInteger(0x1.00001p4f));
  }

  @SeededTest
  void testErf(RandomSeed seed) {
    Assertions.assertEquals(-1, MathUtils.erf(-7));
    Assertions.assertEquals(1, MathUtils.erf(7));
    final UniformRandomProvider r = RngFactory.create(seed.get());
    for (int i = 0; i < 10; i++) {
      final double x = r.nextDouble();
      for (int j = 1; j <= 5; j++) {
        Assertions.assertEquals(Erf.value(x * j), MathUtils.erf(x * j), 1e-6);
      }
    }
  }

  @Test
  void canComputeAverageIndex() {
    canComputeAverageIndex(0, 0);
    canComputeAverageIndex(0, 1);
    canComputeAverageIndex(0, 2);
    canComputeAverageIndex(1, 2);
    canComputeAverageIndex(1, 3);
    canComputeAverageIndex(Integer.MAX_VALUE, 1);
    canComputeAverageIndex(Integer.MAX_VALUE - 10, Integer.MAX_VALUE);
    canComputeAverageIndex(Integer.MAX_VALUE, Integer.MAX_VALUE);
  }

  private static void canComputeAverageIndex(int index1, int index2) {
    final long expected = ((long) index1 + (long) index2) / 2L;
    Assertions.assertEquals(expected, MathUtils.averageIndex(index1, index2),
        () -> "Index1=" + index1 + ", Index2=" + index2);
  }

  @Test
  @Disabled("This outputs the mean and max error for two log1pmx implementations")
  void testLog1pmxVersions() {
    final UniformRandomProvider rng = RngFactory.createWithFixedSeed();
    final int trials = 1000;
    final double n = trials;
    final DoubleUnaryOperator fa = MathUtilsTest::log1pmxa;
    final DoubleUnaryOperator fb = MathUtilsTest::log1pmxb;
    // final DoubleUnaryOperator fb = MathUtils::log1pmx;
    final DoubleUnaryOperator fc = MathUtilsTest::log1pmxc;
    for (final int exp : new int[] {
        // No error
        // -150, -140, -130, -120, -110, -100, -90, -80, -70, -60,
        -54, -53, -52, -51, -50, -49, -48, -47, -46, -45, -44, -43, -42, -41, -40, -39, -38, -37,
        -36, -35, -34, -33, -32, -31, -30, -29, -28, -27, -26, -25, -24, -23, -22, -21, -20, -19,
        -18, -17, -16, -15, -14, -13, -12, -11, -10, -9, -8, -7, -6, -5, -4, -3, -2,
        // Note: No methods are good when x -> -0.84.. as the result -> 1
        // and there is loss of precision
        -1}) {
      final double xx = Math.scalb(1.0, exp);
      final long bits = Double.doubleToRawLongBits(xx);
      long ea1 = 0;
      long eb1 = 0;
      long ea2 = 0;
      long eb2 = 0;
      long ma1 = 0;
      long mb1 = 0;
      long ma2 = 0;
      long mb2 = 0;
      for (int i = 0; i < trials; i++) {
        final double x = Double.longBitsToDouble(bits | (rng.nextLong() >>> 12));
        final double a1 = fa.applyAsDouble(x);
        final double b1 = fb.applyAsDouble(x);
        final double c1 = fc.applyAsDouble(x);
        final long ba1 = Double.doubleToLongBits(a1);
        final long bb1 = Double.doubleToLongBits(b1);
        final long bc1 = Double.doubleToLongBits(c1);
        long da = Math.abs(bc1 - ba1);
        long db = Math.abs(bc1 - bb1);
        ea1 += da;
        eb1 += db;
        ma1 = Math.max(ma1, da);
        mb1 = Math.max(mb1, db);
        final double a2 = fa.applyAsDouble(-x);
        final double b2 = fb.applyAsDouble(-x);
        final double c2 = fc.applyAsDouble(-x);
        final long ba2 = Double.doubleToLongBits(a2);
        final long bb2 = Double.doubleToLongBits(b2);
        final long bc2 = Double.doubleToLongBits(c2);
        da = Math.abs(bc2 - ba2);
        db = Math.abs(bc2 - bb2);
        // When x -> -1 then the reference implementation using BigDecimal is very different.
        // if (db > 1000) {
        // System.out.printf("x=%s %s : %s %d %s %d%n", x, c2, a2, da, b2, db);
        // }
        ea2 += da;
        eb2 += db;
        ma2 = Math.max(ma2, da);
        mb2 = Math.max(mb2, db);
      }
      System.out.printf("exp=%3d x=%25s %25s : %10.6g %2d %10.6g %2d : %10.6g %2d %10.6g %2d%n",
          exp, xx, log1pmxc(xx), ea1 / n, ma1, eb1 / n, mb1, ea2 / n, ma2, eb2 / n, mb2);
    }
  }

  /**
   * Returns {@code log(1 + x) - x}. This function is accurate when {@code x -> 0}.
   *
   * <p>This function uses a Taylor series expansion when x is small:
   *
   * <pre>
   * ln(1 + x) = x - x^2/2 + x^3/3 - x^4/4 + ...
   * </pre>
   *
   * <p>See <a href="https://en.wikipedia.org/wiki/Abramowitz_and_Stegun">Abramowitz, M. and Stegun,
   * I. A.</a> (1972) Handbook of Mathematical Functions. New York: Dover. Formula (4.1.24), p.68.
   *
   * @param x the x
   * @return {@code log(1 + x) - x}
   */
  private static double log1pmxa(double x) {
    final double a = Math.abs(x);
    // log1p = 0.40546510810816438486
    // log1p(0.25) = 0.22314355131420976
    // log1p(-0.25) = -0.2876820724517809
    // log1p(-0.5) = -0.69314718055994528623
    // Note: Boost uses 0.95
    if (a > 0.5) {
      return Math.log1p(x) - x;
    }

    // Sum in reverse order (small + big) for less round-off error.
    // This computes more accurately than the traditional big -> small order.

    final double xx = x * x;
    double xp = xx * xx;
    double sum = -xp / 4 + x * xx / 3 - xx / 2;

    // To stop the Taylor series the next term must be less than 1 ulp from the answer.
    // x^n/n < |log(1+x)-x| * eps
    // eps = machine epsilon = 2^-53
    // x^n < |log(1+x)-x| * eps
    // n >= (log(|log(1+x)-x|) + log(eps)) / log(x)

    // +/-9.5367431640625e-07: log1pmx = -4.547470617660916e-13 : -4.5474764000725028e-13
    // n = 4.69
    if (a < 0x1.0p-20) {
      // No point in the series expansion as additional terms are too small to be added
      return sum;
    }
    // +/-2.44140625E-4: log1pmx = -2.9797472637290841e-08 : -2.9807173914456693e-08
    // n = 6.49
    // @formatter:off
    if (a < 0x1.0p-12) {
      // 7 iterations
      return x * xx * xp / 7 -
                 xx * xp / 6 +
                  x * xp / 5 -
                      xp / 4 +
                  x * xx / 3 -
                      xx / 2;
    }
    // +/-0.00390625: log1pmx = -7.6095843426769861e-06 : -7.6493211363290911e-06
    // n = 8.75
    if (a < 0x1.0p-8) {
      // 9 iterations
      return x * xp * xp / 9 -
                 xp * xp / 8 +
             x * xx * xp / 7 -
                 xx * xp / 6 +
                  x * xp / 5 -
                      xp / 4 +
                  x * xx / 3 -
                      xx / 2;
    }
    // +/-0.015625: log1pmx = -0.00012081346403474586 : -0.00012335696813916864
    // n = 10.9974
    if (a < 0x1.0p-6) {
      // 11 iterations
      return x * xx * xp * xp / 11 -
                 xx * xp * xp / 10 +
                  x * xp * xp /  9 -
                      xp * xp /  8 +
                  x * xx * xp /  7 -
                      xx * xp /  6 +
                       x * xp /  5 -
                           xp /  4 +
                       x * xx /  3 -
                           xx /  2;
    }
    // @formatter:on

    // ln(1 + x) -x = x^2/2 + x^3/3 - x^4/4 + ...
    // Add 2 terms at a time, smallest first
    for (int n = 5;; n += 2) {
      final double xp2 = xp * xx;
      final double sum2 = -xp2 / (n + 1) + x * xp / n + sum;
      // Since x < 1 the additional terms will reduce in magnitude.
      // Iterate until convergence.
      if (sum2 == sum) {
        break;
      }
      xp = xp2;
      sum = sum2;
    }

    return sum;
  }

  /**
   * Returns {@code log(1 + x) - x}. This function is accurate when {@code x -> 0}.
   *
   * <p>This function uses a Taylor series expansion when x is small:
   *
   * <pre>
   * ln(1 + x) = x - x^2/2 + x^3/3 - x^4/4 + ...
   * </pre>
   *
   * <p>See <a href="https://en.wikipedia.org/wiki/Abramowitz_and_Stegun">Abramowitz, M. and Stegun,
   * I. A.</a> (1972) Handbook of Mathematical Functions. New York: Dover. Formula (4.1.24), p.68.
   *
   * @param x the x
   * @return {@code log(1 + x) - x}
   */
  private static double log1pmxa2(double x) {
    final double a = Math.abs(x);
    if (a > 0.5) {
      return Math.log1p(x) - x;
    }

    final double xx = x * x;
    double xp = -xx * xx;
    double sum = -xx / 2 + x * xx / 3 + xp / 4;
    if (a < 0x1.0p-20) {
      // No point in the series expansion as additional terms are too small to be added
      return sum;
    }

    // Continue the series
    // ln(1 + x) -x = x^2/2 + x^3/3 - x^4/4 + ...
    for (int n = 5;; n++) {
      xp *= -x;
      final double sum2 = sum + xp / n;
      // Since x < 1 the additional terms will reduce in magnitude.
      // Iterate until convergence. Worst case scenario is ~48 iterations
      // when x=0.5
      if (sum2 == sum) {
        break;
      }
      sum = sum2;
    }

    return sum;
  }

  /**
   * Returns {@code log(1 + x) - x}. This function is accurate when {@code x -> 0}.
   *
   * <p>This function uses a Taylor series expansion when x is small:
   *
   * <pre>
   * ln(1 + x) = ln(a) + 2 [z + z^3/3 + z^5/5 + z^7/7 + ... ]
   *
   * with z = x / (2a + x), a = 1:
   *
   * ln(x + 1) - x = -x + 2 [z + z^3/3 + z^5/5 + z^7/7 + ... ]
   * ln(x + 1) - x = z * (-x/z + 2 + 2 [ z^2/3 + z^4/5 + z^6/7 + ... ])
   *               = z * (-x/(x/(2+x)) + 2 + 2 [ z^2/3 + z^4/5 + z^6/7 + ... ])
   *               = z * (-(2+x) + 2 + 2 [ z^2/3 + z^4/5 + z^6/7 + ... ])
   *               = z * (-x + 2 [ z^2/3 + z^4/5 + z^6/7 + ... ])
   *               = z * (-x + 2z [ 1/3 + z^2/5 + z^4/7 + ... ])
   * </pre>
   *
   * <p>The code is based on the {@code log1pmx} documentation for the <a
   * href="https://rdrr.io/rforge/DPQ/man/log1pmx.html">R DPQ package</a>.
   *
   * <p>Abramowitz, M. and Stegun, I. A. (1972) Handbook of Mathematical Functions. New York: Dover.
   * <a href="https://en.wikipedia.org/wiki/Abramowitz_and_Stegun">Wikipedia:
   * Abramowitz_and_Stegun</a> provides links to the full text which is in public domain. Formula
   * (4.1.29), p.68.
   *
   * @param x the x
   * @return {@code log(1 + x) - x}
   */
  private static double log1pmxb(double x) {
    if (x < -1) {
      return Double.NaN;
    }
    if (x == -1) {
      return Double.NEGATIVE_INFINITY;
    }
    // Use the threshold documented in the R implementation
    if (x < -0.79149064 || x > 1) {
      return Math.log1p(x) - x;
    }
    final double t = x / (2 + x);
    final double y = t * t;
    if (Math.abs(x) < 0.01) {
      // Optimise result when series expansion is not required
      return t * ((((2.0 / 9 * y + 2.0 / 7) * y + 2.0 / 5) * y + 2.0 / 3) * y - x);
      // This should be optimised in the range 2^-19 to 2^-6 as it has more error than
      // the Taylor series. None of these changes make any difference.

      // Round-off from 2/3 = 3.700743415417188E-17
      // return t * ((((2.0 / 9 * y + 2.0 / 7) * y + 2.0 / 5) * y + 3.700743415417188E-17 + 2.0 / 3)
      // * y - x);
      // return t * (y * 2 * (1.0/3 + y/5 + y*y/7 + y*y*y/9) - x);
      // return t * (2 * (y/3 + y*y/5 + y*y*y/7 + y*y*y*y/9) - x);
      // return t * (-x + 2*y/3 + 2*y*y/5 + 2*y*y*y/7 + 2*y*y*y*y/9);
      // return t * (2*y*y*y*y/9 + 2*y*y*y/7 + 2*y*y/5 + 3.700743415417188E-17*y + 2*y/3 - x);
    }

    // Continued fraction
    // sum(k=0,...,Inf; y^k/(i+k*d)) = 1/3 + y/5 + y^2/7 + y^3/9 + ... )

    double numerator = 1;
    int denominator = 3;
    double sum = 1.0 / 3;
    for (;;) {
      numerator *= y;
      denominator += 2;
      final double sum2 = sum + numerator / denominator;
      // Since x <= 1 the additional terms will reduce in magnitude.
      // Iterate until convergence. Worst case scenario:
      // x iterations
      // -0.79 38
      // -0.5 15
      // -0.1 5
      // 0.1 5
      // 0.5 10
      // 1.0 15
      if (sum2 == sum) {
        break;
      }
      sum = sum2;
    }
    return t * (2 * y * sum - x);
  }

  /**
   * Returns {@code log(1 + x) - x}. This function is accurate when {@code x -> 0}.
   *
   * <p>This function uses a Taylor series expansion:
   *
   * <pre>
   * ln(1 + x) = x - x^2/2 + x^3/3 - x^4/4 + ...
   * </pre>
   *
   * @param x the x
   * @return {@code log(1 + x) - x}
   */
  private static double log1pmxc(double x) {
    // final double a = Math.abs(x);
    // if (a > 0.5) {
    // return Math.log1p(x) - x;
    // }

    // ln(1 + x) -x = x^2/2 + x^3/3 - x^4/4 + ...
    BigDecimal sum = BigDecimal.ZERO;
    final BigDecimal nx = new BigDecimal(-x);
    BigDecimal xp = new BigDecimal(x);
    final MathContext mc = new MathContext(100);
    for (int n = 2;; n++) {
      xp = xp.multiply(nx, mc);
      final BigDecimal sum2 = sum.add(xp.divide(new BigDecimal(n), mc), mc);
      // Since x < 1 the additional terms will reduce in magnitude.
      // Iterate until convergence.
      if (sum2.equals(sum)) {
        return sum.doubleValue();
      }
      sum = sum2;
    }
  }

  @ParameterizedTest
  @ValueSource(doubles = {-1, -1.1, 1, 1.5, 2, 3})
  void testLog1pmxStandard(double x) {
    Assertions.assertEquals(Math.log1p(x) - x, MathUtils.log1pmx(x));
  }

  // @formatter:off
  @ParameterizedTest
  @ValueSource(doubles = {
      1e-100, 1e-50, 1e-20, 1e-10, 1e-6, 1e-4, 0.1, 0.2, 0.3, 0.4,
      -1e-100, -1e-50, -1e-20, -1e-10, -1e-6, -1e-4, -0.1, -0.2, -0.3, -0.4,
  })
  void testLog1pmx(double x) {
    final double expected = log1pmxc(x);
    if (Math.abs(x) > 1e-16) {
      // Verify the reference method is correct
      Assertions.assertEquals(Math.log1p(x) - x, expected, Math.abs(expected) * 1e-6);
    }
    Assertions.assertEquals(expected, MathUtils.log1pmx(x), 2 * Math.ulp(expected));
  }

  @ParameterizedTest
  @CsvSource({
    "0, 0",
    "0, 1",
    "0, -0.1",
    "NaN, 2",
    "2, NaN",
    "2, 2",
    "-2, 3",
    "-2, 4",
    "4.3, 2.23",
    "4.3, -2.23",
    "22, 0.2",
  })
  void testPowm1Standard(double x, double y) {
    Assertions.assertEquals(Math.pow(x, y) - 1, MathUtils.powm1(x, y));
  }

  @ParameterizedTest
  @CsvSource({
    "1.1, 0.1, 0.009576582776887034",
    "0.9, 0.1, -0.010480741793785605",
    "1.1, 1.234, 0.12480845830764926",
    "0.9, 1.234, -0.12191763181244",
    "5.67, 0.1, 0.18948318179396795",
    "5.67, -0.1, -0.15929874814050846",
    "1.000001, 1.234, 1.234000144276446e-06",
    "0.999999, 1.234, -1.2339998556574477e-06",
    "-1.000001, 2, 2.0000009998354665e-06",
  })
  void testPowm1(double x, double y, double expected) {
    Assertions.assertEquals(expected, MathUtils.powm1(x, y), Math.ulp(expected));
  }
}
