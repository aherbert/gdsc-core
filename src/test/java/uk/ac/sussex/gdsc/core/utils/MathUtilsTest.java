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

package uk.ac.sussex.gdsc.core.utils;

import java.math.BigDecimal;
import org.apache.commons.math3.special.Erf;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.test.junit5.RandomSeed;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.rng.RngUtils;

@SuppressWarnings({"javadoc"})
public class MathUtilsTest {
  @Test
  public void testLimitsDouble() {
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
  public void testLimitsFloat() {
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
  public void testLimitsInt() {
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
  public void testLimitsLong() {
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
  public void testLimitsShort() {
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
  public void testMaxDouble() {
    final double noMax = Double.NaN;
    Assertions.assertEquals(noMax, MathUtils.max((double[]) null));
    Assertions.assertEquals(noMax, MathUtils.max(new double[0]));
    for (double value : new double[] {Double.NaN, Double.NEGATIVE_INFINITY, -42}) {
      Assertions.assertEquals(value, MathUtils.maxDefault(value, new double[0]));
      Assertions.assertEquals(value, MathUtils.maxDefault(value, (double[]) null));
    }
    Assertions.assertEquals(7, MathUtils.max(new double[] {7, 6, 4, 3, 5}));
    Assertions.assertEquals(7, MathUtils.max(new double[] {3, 7, 6, 4, 5}));
  }

  @Test
  public void testMaxFloat() {
    final float noMax = Float.NaN;
    Assertions.assertEquals(noMax, MathUtils.max((float[]) null));
    Assertions.assertEquals(noMax, MathUtils.max(new float[0]));
    for (float value : new float[] {Float.NaN, Float.NEGATIVE_INFINITY, -42}) {
      Assertions.assertEquals(value, MathUtils.maxDefault(value, new float[0]));
      Assertions.assertEquals(value, MathUtils.maxDefault(value, (float[]) null));
    }
    Assertions.assertEquals(7, MathUtils.max(new float[] {7, 6, 4, 3, 5}));
    Assertions.assertEquals(7, MathUtils.max(new float[] {3, 7, 6, 4, 5}));
  }

  @Test
  public void testMaxInt() {
    final int noMax = Integer.MIN_VALUE;
    Assertions.assertEquals(noMax, MathUtils.max((int[]) null));
    Assertions.assertEquals(noMax, MathUtils.max(new int[0]));
    for (int value : new int[] {Integer.MIN_VALUE, -42, 13}) {
      Assertions.assertEquals(value, MathUtils.maxDefault(value, new int[0]));
      Assertions.assertEquals(value, MathUtils.maxDefault(value, (int[]) null));
    }
    Assertions.assertEquals(7, MathUtils.max(new int[] {7, 6, 4, 3, 5}));
    Assertions.assertEquals(7, MathUtils.max(new int[] {3, 7, 6, 4, 5}));
  }

  @Test
  public void testMaxLong() {
    final long noMax = Long.MIN_VALUE;
    Assertions.assertEquals(noMax, MathUtils.max((long[]) null));
    Assertions.assertEquals(noMax, MathUtils.max(new long[0]));
    for (long value : new long[] {Long.MIN_VALUE, -42, 13}) {
      Assertions.assertEquals(value, MathUtils.maxDefault(value, new long[0]));
      Assertions.assertEquals(value, MathUtils.maxDefault(value, (long[]) null));
    }
    Assertions.assertEquals(7, MathUtils.max(new long[] {7, 6, 4, 3, 5}));
    Assertions.assertEquals(7, MathUtils.max(new long[] {3, 7, 6, 4, 5}));
  }

  @Test
  public void testMaxShort() {
    final short noMax = Short.MIN_VALUE;
    Assertions.assertEquals(noMax, MathUtils.max((short[]) null));
    Assertions.assertEquals(noMax, MathUtils.max(new short[0]));
    for (short value : new short[] {Short.MIN_VALUE, -42, 13}) {
      Assertions.assertEquals(value, MathUtils.maxDefault(value, new short[0]));
      Assertions.assertEquals(value, MathUtils.maxDefault(value, (short[]) null));
    }
    Assertions.assertEquals(7, MathUtils.max(new short[] {7, 6, 4, 3, 5}));
    Assertions.assertEquals(7, MathUtils.max(new short[] {3, 7, 6, 4, 5}));
  }

  @Test
  public void testMinDouble() {
    final double noMin = Double.NaN;
    Assertions.assertEquals(noMin, MathUtils.min((double[]) null));
    Assertions.assertEquals(noMin, MathUtils.min(new double[0]));
    for (double value : new double[] {Double.NaN, Double.NEGATIVE_INFINITY, -42}) {
      Assertions.assertEquals(value, MathUtils.minDefault(value, new double[0]));
      Assertions.assertEquals(value, MathUtils.minDefault(value, (double[]) null));
    }
    Assertions.assertEquals(3, MathUtils.min(new double[] {7, 6, 4, 3, 5}));
    Assertions.assertEquals(3, MathUtils.min(new double[] {3, 7, 6, 4, 5}));
  }

  @Test
  public void testMinFloat() {
    final float noMin = Float.NaN;
    Assertions.assertEquals(noMin, MathUtils.min((float[]) null));
    Assertions.assertEquals(noMin, MathUtils.min(new float[0]));
    for (float value : new float[] {Float.NaN, Float.NEGATIVE_INFINITY, -42}) {
      Assertions.assertEquals(value, MathUtils.minDefault(value, new float[0]));
      Assertions.assertEquals(value, MathUtils.minDefault(value, (float[]) null));
    }
    Assertions.assertEquals(3, MathUtils.min(new float[] {7, 6, 4, 3, 5}));
    Assertions.assertEquals(3, MathUtils.min(new float[] {3, 7, 6, 4, 5}));
  }

  @Test
  public void testMinInt() {
    final int noMin = Integer.MAX_VALUE;
    Assertions.assertEquals(noMin, MathUtils.min((int[]) null));
    Assertions.assertEquals(noMin, MathUtils.min(new int[0]));
    for (int value : new int[] {Integer.MAX_VALUE, -42, 13}) {
      Assertions.assertEquals(value, MathUtils.minDefault(value, new int[0]));
      Assertions.assertEquals(value, MathUtils.minDefault(value, (int[]) null));
    }
    Assertions.assertEquals(3, MathUtils.min(new int[] {7, 6, 4, 3, 5}));
    Assertions.assertEquals(3, MathUtils.min(new int[] {3, 7, 6, 4, 5}));
  }

  @Test
  public void testMinLong() {
    final long noMin = Long.MAX_VALUE;
    Assertions.assertEquals(noMin, MathUtils.min((long[]) null));
    Assertions.assertEquals(noMin, MathUtils.min(new long[0]));
    for (long value : new long[] {Long.MAX_VALUE, -42, 13}) {
      Assertions.assertEquals(value, MathUtils.minDefault(value, new long[0]));
      Assertions.assertEquals(value, MathUtils.minDefault(value, (long[]) null));
    }
    Assertions.assertEquals(3, MathUtils.min(new long[] {7, 6, 4, 3, 5}));
    Assertions.assertEquals(3, MathUtils.min(new long[] {3, 7, 6, 4, 5}));
  }

  @Test
  public void testMinShort() {
    final short noMin = Short.MAX_VALUE;
    Assertions.assertEquals(noMin, MathUtils.min((short[]) null));
    Assertions.assertEquals(noMin, MathUtils.min(new short[0]));
    for (short value : new short[] {Short.MAX_VALUE, -42, 13}) {
      Assertions.assertEquals(value, MathUtils.minDefault(value, new short[0]));
      Assertions.assertEquals(value, MathUtils.minDefault(value, (short[]) null));
    }
    Assertions.assertEquals(3, MathUtils.min(new short[] {7, 6, 4, 3, 5}));
    Assertions.assertEquals(3, MathUtils.min(new short[] {3, 7, 6, 4, 5}));
  }

  @Test
  public void testCumulativeHistogram() {
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
  public void canGetLogLikelihoodFromResidualSumOfSquares() {
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
  public void canComputeAic() {
    for (final int k : new int[] {3, 6}) {
      for (final double ll : new double[] {-456.78, 98.123}) {
        final double expected = 2 * k - 2 * ll;
        Assertions.assertEquals(expected, MathUtils.getAkaikeInformationCriterion(ll, k));
      }
    }
  }

  @Test
  public void canComputeAicc() {
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
  public void canComputeBic() {
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
  public void canComputeAdjustedR2() {
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
  public void canGetTotalSumOfSquares(RandomSeed seed) {
    final UniformRandomProvider r = RngUtils.create(seed.getSeed());
    final double[] values = new double[7];
    for (int i = 1; i <= 10; i++) {
      SummaryStatistics stats = new SummaryStatistics();
      for (int j = 0; j < values.length; j++) {
        values[j] = r.nextDouble() * i;
        stats.addValue(values[j]);
      }
      final double expected = stats.getSecondMoment();
      Assertions.assertEquals(expected, MathUtils.getTotalSumOfSquares(values), expected * 1e-8);
    }
  }

  @Test
  public void testSumDouble() {
    Assertions.assertEquals(0, MathUtils.sum((double[]) null));
    Assertions.assertEquals(0, MathUtils.sum(new double[0]));
    Assertions.assertEquals(0, MathUtils.sum(new double[2]));
    Assertions.assertEquals(3, MathUtils.sum(new double[] {1, 2, 3, -2, -1}));
  }

  @Test
  public void testSumFloat() {
    Assertions.assertEquals(0, MathUtils.sum((float[]) null));
    Assertions.assertEquals(0, MathUtils.sum(new float[0]));
    Assertions.assertEquals(0, MathUtils.sum(new float[2]));
    Assertions.assertEquals(3, MathUtils.sum(new float[] {1, 2, 3, -2, -1}));
  }

  @Test
  public void testSumLong() {
    Assertions.assertEquals(0, MathUtils.sum((long[]) null));
    Assertions.assertEquals(0, MathUtils.sum(new long[0]));
    Assertions.assertEquals(0, MathUtils.sum(new long[2]));
    Assertions.assertEquals(3, MathUtils.sum(new long[] {1, 2, 3, -2, -1}));
  }

  @Test
  public void testSumInt() {
    Assertions.assertEquals(0, MathUtils.sum((int[]) null));
    Assertions.assertEquals(0, MathUtils.sum(new int[0]));
    Assertions.assertEquals(0, MathUtils.sum(new int[2]));
    Assertions.assertEquals(3, MathUtils.sum(new int[] {1, 2, 3, -2, -1}));
  }

  @Test
  public void testRoundedToSignificantDigits() {
    for (double value : new double[] {Double.NaN, Double.NEGATIVE_INFINITY,
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
  }

  @Test
  public void testRoundToSignificantDigits() {
    for (double value : new double[] {Double.NaN, Double.NEGATIVE_INFINITY,
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
  public void testRoundToFactor() {
    for (double value : new double[] {Double.NaN, Double.NEGATIVE_INFINITY,
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
  public void testRoundToBigDecimalSignificantDigits() {
    for (double value : new double[] {Double.NaN, Double.NEGATIVE_INFINITY,
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
  public void canRoundUsingDecimalPlaces() {
    for (double value : new double[] {Double.NaN, Double.NEGATIVE_INFINITY,
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
  public void canRoundUsingNegativeDecimalPlaces() {
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
  public void canRoundToBigDecimalUsingDecimalPlaces(RandomSeed seed) {
    // 0.1 cannot be an exact double (see constructor documentation for BigDecimal)
    double value = 0.1;
    BigDecimal bd = new BigDecimal(value);
    Assertions.assertNotEquals("0.1", bd.toPlainString());
    Assertions.assertEquals("0.1",
        MathUtils.roundUsingDecimalPlacesToBigDecimal(value, 1).toPlainString());

    // Random test that rounding does the same as String.format
    final UniformRandomProvider r = RngUtils.create(seed.getSeed());
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
  public void canRoundToBigDecimalUsingNegativeDecimalPlaces() {
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
  public void testFloorToFactor() {
    for (double value : new double[] {Double.NaN, Double.NEGATIVE_INFINITY,
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
  public void testCeilToFactor() {
    for (double value : new double[] {Double.NaN, Double.NEGATIVE_INFINITY,
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
  public void canInterpolateY() {
    Assertions.assertEquals(3.5, MathUtils.interpolateY(1, 2, 3, 5, 2));
    Assertions.assertEquals(6.5, MathUtils.interpolateY(1, 2, 3, 5, 4));
    Assertions.assertEquals(-1, MathUtils.interpolateY(1, 2, 3, 5, -1));
    Assertions.assertEquals(-2.5, MathUtils.interpolateY(1, 2, 3, 5, -2));
  }

  @Test
  public void canInterpolateX() {
    Assertions.assertEquals(5.0 / 3, MathUtils.interpolateX(1, 2, 3, 5, 3));
    Assertions.assertEquals(11.0 / 3, MathUtils.interpolateX(1, 2, 3, 5, 6));
    Assertions.assertEquals(-1, MathUtils.interpolateX(1, 2, 3, 5, -1));
    Assertions.assertEquals(-5.0 / 3, MathUtils.interpolateX(1, 2, 3, 5, -2));
  }

  @Test
  public void testDistanceDouble() {
    Assertions.assertEquals(5, MathUtils.distance(1.0, 2, 4, 6));
  }

  @Test
  public void testDistanceThreeDDouble() {
    Assertions.assertEquals(Math.sqrt(2 * 5 * 5), MathUtils.distance(1.0, 2, 3, 4, 6, 8));
  }

  @Test
  public void testDistanceFloat() {
    Assertions.assertEquals(5, MathUtils.distance(1f, 2, 4, 6));
  }

  @Test
  public void testDistance2Double() {
    Assertions.assertEquals(5 * 5, MathUtils.distance2(1.0, 2, 4, 6));
  }

  @Test
  public void testDistance2ThreeDDouble() {
    Assertions.assertEquals(2 * 5 * 5, MathUtils.distance2(1.0, 2, 3, 4, 6, 8));
  }

  @Test
  public void testDistance2Float() {
    Assertions.assertEquals(5 * 5, MathUtils.distance2(1f, 2, 4, 6));
  }

  @Test
  public void testClipDouble() {
    Assertions.assertEquals(5, MathUtils.clip(5.0, 10.0, 3));
    Assertions.assertEquals(7, MathUtils.clip(5.0, 10.0, 7));
    Assertions.assertEquals(10, MathUtils.clip(5.0, 10.0, 11));
  }

  @Test
  public void testClipFloat() {
    Assertions.assertEquals(5f, MathUtils.clip(5.0f, 10.0f, 3f));
    Assertions.assertEquals(7f, MathUtils.clip(5.0f, 10.0f, 7f));
    Assertions.assertEquals(10f, MathUtils.clip(5.0f, 10.0f, 11f));
  }

  @Test
  public void testClipInt() {
    Assertions.assertEquals(5, MathUtils.clip(5, 10, 3));
    Assertions.assertEquals(7, MathUtils.clip(5, 10, 7));
    Assertions.assertEquals(10, MathUtils.clip(5, 10, 11));
  }

  @Test
  public void testPow2Double() {
    Assertions.assertEquals(25, MathUtils.pow2(5.0));
    Assertions.assertEquals(25, MathUtils.pow2(-5.0));
  }

  @Test
  public void testPow2Int() {
    Assertions.assertEquals(25, MathUtils.pow2(5));
    Assertions.assertEquals(25, MathUtils.pow2(-5));
  }

  @Test
  public void testPow3Double() {
    Assertions.assertEquals(125, MathUtils.pow3(5.0));
    Assertions.assertEquals(-125, MathUtils.pow3(-5.0));
  }

  @Test
  public void testPow3Int() {
    Assertions.assertEquals(125, MathUtils.pow3(5));
    Assertions.assertEquals(-125, MathUtils.pow3(-5));
  }

  @Test
  public void testPow4Double() {
    Assertions.assertEquals(625, MathUtils.pow4(5.0));
    Assertions.assertEquals(625, MathUtils.pow4(-5.0));
  }

  @Test
  public void testPow4Int() {
    Assertions.assertEquals(625, MathUtils.pow4(5));
    Assertions.assertEquals(625, MathUtils.pow4(-5));
  }

  @Test
  public void testIsPow2() {
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
  public void testNextPow2() {
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
  public void canComputeApproximateLog2() {
    Assertions.assertEquals(Integer.MIN_VALUE, MathUtils.log2(0));
    Assertions.assertEquals(0, MathUtils.log2(1));
    Assertions.assertEquals(1, MathUtils.log2(2));
    Assertions.assertEquals(2, MathUtils.log2(4));
    Assertions.assertEquals(8, MathUtils.log2(256));
  }

  @Test
  public void canComputeLog2() {
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
  public void testDiv0Double() {
    Assertions.assertEquals(0, MathUtils.div0(0.0, 0));
    Assertions.assertEquals(0, MathUtils.div0(0.0, 10));
    Assertions.assertEquals(1.1, MathUtils.div0(11.0, 10));
    Assertions.assertEquals(10, MathUtils.div0(100.0, 10));
  }

  @Test
  public void testDiv0Float() {
    Assertions.assertEquals(0f, MathUtils.div0(0f, 0));
    Assertions.assertEquals(0f, MathUtils.div0(0f, 10));
    Assertions.assertEquals(1.1f, MathUtils.div0(11f, 10));
    Assertions.assertEquals(10f, MathUtils.div0(100f, 10));
  }

  @Test
  public void testDiv0Int() {
    Assertions.assertEquals(0, MathUtils.div0(0, 0));
    Assertions.assertEquals(0, MathUtils.div0(0, 10));
    Assertions.assertEquals(1.1, MathUtils.div0(11, 10));
    Assertions.assertEquals(10, MathUtils.div0(100, 10));
  }

  @Test
  public void testDiv0Long() {
    Assertions.assertEquals(0, MathUtils.div0(0L, 0));
    Assertions.assertEquals(0, MathUtils.div0(0L, 10));
    Assertions.assertEquals(1.1, MathUtils.div0(11L, 10));
    Assertions.assertEquals(10, MathUtils.div0(100L, 10));
  }

  @Test
  public void testIsMathematicalInteger() {
    for (double value : new double[] {Double.NaN, Double.NEGATIVE_INFINITY,
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
  public void testIsIntegerDouble() {
    for (double value : new double[] {Double.NaN, Double.NEGATIVE_INFINITY,
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
  public void testIsIntegerFloat() {
    for (float value : new float[] {Float.NaN, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY}) {
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
  public void testErf(RandomSeed seed) {
    Assertions.assertEquals(-1, MathUtils.erf(-7));
    Assertions.assertEquals(1, MathUtils.erf(7));
    final UniformRandomProvider r = RngUtils.create(seed.getSeed());
    for (int i = 0; i < 10; i++) {
      final double x = r.nextDouble();
      for (int j = 1; j <= 5; j++) {
        Assertions.assertEquals(Erf.erf(x * j), MathUtils.erf(x * j), 1e-6);
      }
    }
  }

  @Test
  public void canComputeAverageIndex() {
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
}
