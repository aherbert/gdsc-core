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
import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.test.junit5.RandomSeed;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.rng.RngUtils;

@SuppressWarnings({"javadoc"})
public class MathsUtilsTest {
  @SeededTest
  public void canRoundToDecimalPlaces(RandomSeed seed) {
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
  public void canRoundToNegativeDecimalPlaces() {
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
  public void canGetLogLikelihoodFromResidualSumOfSquares() {
    for (int n : new int[] {34, 67}) {
      for (double rss : new double[] {456.78, 98.123}) {
        final double expected =
            -n * Math.log(2 * Math.PI) / 2 - n * Math.log(rss / n) / 2 - n / 2.0;
        Assertions.assertEquals(expected, MathUtils.getLogLikelihood(rss, n),
            Math.abs(expected) * 1e-8);
      }
    }
  }

  @Test
  public void canComputeAic() {
    for (int k : new int[] {3, 6}) {
      for (double ll : new double[] {-456.78, 98.123}) {
        final double expected = 2 * k - 2 * ll;
        Assertions.assertEquals(expected, MathUtils.getAkaikeInformationCriterion(ll, k));
      }
    }
  }

  @Test
  public void canComputeAicc() {
    for (int n : new int[] {13, 42}) {
      for (int k : new int[] {3, 6}) {
        for (double ll : new double[] {-456.78, 98.123}) {
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
    for (int n : new int[] {13, 42}) {
      for (int k : new int[] {3, 6}) {
        for (double ll : new double[] {-456.78, 98.123}) {
          final double expected = k * Math.log(n) - 2 * ll;
          Assertions.assertEquals(expected, MathUtils.getBayesianInformationCriterion(ll, n, k));
        }
      }
    }
  }

  @Test
  public void canComputeAdjustedR2() {
    for (int n : new int[] {13, 42}) {
      for (int k : new int[] {3, 6}) {
        for (double rss : new double[] {-456.78, 98.123}) {
          for (double tss : new double[] {-456.78, 98.123}) {
            final double expected = 1 - (rss / tss) * ((double) (n - 1) / (n - k - 1));
            Assertions.assertEquals(expected,
                MathUtils.getAdjustedCoefficientOfDetermination(rss, tss, n, k));
          }
        }
      }
    }
  }
}
