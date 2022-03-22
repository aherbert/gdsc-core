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
 * Copyright (C) 2011 - 2022 Alex Herbert
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

package uk.ac.sussex.gdsc.core.math.interpolation;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import java.util.Formatter;
import java.util.function.UnaryOperator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.core.utils.SortUtils;
import uk.ac.sussex.gdsc.test.api.Predicates;
import uk.ac.sussex.gdsc.test.api.TestAssertions;
import uk.ac.sussex.gdsc.test.api.function.DoubleDoubleBiPredicate;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.rng.RngFactory;
import uk.ac.sussex.gdsc.test.utils.RandomSeed;
import uk.ac.sussex.gdsc.test.utils.TestComplexity;
import uk.ac.sussex.gdsc.test.utils.TestLogging.TestLevel;
import uk.ac.sussex.gdsc.test.utils.TestSettings;

/**
 * This class is used to in-line the computation for the
 * {@link CustomTricubicInterpolatingFunction}.
 */
@SuppressWarnings({"javadoc"})
class CustomTricubicInterpolatingFunctionInlineTest {
  private static Logger logger;

  @BeforeAll
  public static void beforeAll() {
    logger = Logger.getLogger(CustomTricubicInterpolatingFunctionInlineTest.class.getName());
  }

  @AfterAll
  public static void afterAll() {
    logger = null;
  }

  static String inlineComputeCoefficients() {
    final StringBuilder sb = new StringBuilder();
    try (Formatter formatter = new Formatter(sb)) {
      final int sz = 64;

      formatter.format("final double[] a = new double[%d];\n", sz);

      for (int i = 0; i < sz; i++) {
        formatter.format("a[%d]=", i);

        final double[] row = CustomTricubicInterpolatingFunction.AINV[i];
        for (int j = 0; j < sz; j++) {
          final double d = row[j];
          if (d != 0) {
            if (d > 0) {
              sb.append('+');
            }
            final int di = (int) Math.floor(d);
            if (di == d) {
              formatter.format("%d*beta[%d]", di, j);
            } else {
              formatter.format("%f*beta[%d]", d, j);
            }
          }
        }
        formatter.format(";\n", i);
      }
      sb.append("return a;\n");
    }

    return finialise(sb);
  }

  static String inlineComputeCoefficientsCollectTerms() {
    final StringBuilder sb = new StringBuilder();
    try (Formatter formatter = new Formatter(sb)) {

      final int sz = 64;

      // Require integer coefficients
      int max = 0;
      for (int i = 0; i < sz; i++) {
        final double[] row = CustomTricubicInterpolatingFunction.AINV[i];
        for (int j = 0; j < sz; j++) {
          final double d = row[j];
          if (d != 0) {
            final int di = (int) Math.floor(d);
            if (di != d) {
              return null;
            }
            if (max < Math.abs(di)) {
              max = Math.abs(di);
            }
          }
        }
      }

      final Int2ObjectArrayMap<IntArrayList> map = new Int2ObjectArrayMap<>(max + 1);

      formatter.format("final double[] a = new double[%d];\n", sz);

      for (int i = 0; i < sz; i++) {
        map.clear();
        final double[] row = CustomTricubicInterpolatingFunction.AINV[i];
        for (int j = 0; j < sz; j++) {
          final double d = row[j];
          if (d != 0) {
            final int di = (int) Math.floor(d);
            final int key = Math.abs(di);
            // Check if contains either positive or negative key
            IntArrayList value = map.get(key);
            if (value == null) {
              value = new IntArrayList();
              map.put(key, value);
            }
            // Store the index and the sign.
            // We use 1-based index so we can store -0
            value.add(((di < 0) ? -1 : 1) * (j + 1));
          }
        }

        formatter.format("a[%d]=", i);

        // Collect terms
        map.int2ObjectEntrySet().forEach(e -> {
          final int key = e.getIntKey();
          final IntArrayList value = e.getValue();
          final int[] js = value.toIntArray(); // Signed j
          final int[] j = js.clone(); // Unsigned j
          for (int k = 0; k < j.length; k++) {
            j[k] = Math.abs(j[k]);
          }

          SortUtils.sortData(js, j, true, false);

          // Check if starting with negative
          char add = '+';
          char sub = '-';

          if (js[0] < 0) {
            // Subtract the set
            sb.append('-');
            if (key > 1) {
              sb.append(key).append('*');
            }
            // Swap signs
            add = sub;
            sub = '+';
          } else {
            // Some positive so add the set
            sb.append('+');
            if (key > 1) {
              sb.append(key).append('*');
            }
          }

          if (js.length != 1) {
            sb.append('(');
          }
          for (int k = 0; k < js.length; k++) {
            if (k != 0) {
              if (js[k] < 0) {
                sb.append(sub);
              } else {
                sb.append(add);
              }
            }
            // Convert 1-based index back to 0-based
            sb.append("beta[").append(Math.abs(js[k]) - 1).append(']');
          }
          if (js.length != 1) {
            sb.append(')');
          }
        });

        formatter.format(";\n", i);
      }
      sb.append("return a;\n");
    }

    return finialise(sb);
  }

  private static String finialise(final StringBuilder sb) {
    String result = sb.toString();
    result = result.replaceAll("\\+1\\*", "+");
    result = result.replaceAll("-1\\*", "-");
    result = result.replaceAll("=\\+", "=");
    result = result.replaceAll("=\\-", "=-");
    return result;
  }

  private final Level level = TestLevel.TEST_DEBUG;

  @Test
  void canConstructInlineComputeCoefficients() {
    Assumptions.assumeTrue(logger.isLoggable(level));
    logger.log(level, inlineComputeCoefficients());
  }

  @Test
  void canConstructInlineComputeCoefficientsCollectTerms() {
    Assumptions.assumeTrue(logger.isLoggable(level));
    logger.log(level, inlineComputeCoefficientsCollectTerms());
  }

  @SeededTest
  void canComputeCoefficientsInline(RandomSeed seed) {
    canComputeCoefficients(seed, CustomTricubicInterpolatingFunction::computeCoefficientsInline);
  }

  @SeededTest
  void canComputeCoefficientsInlineCollectTerms(RandomSeed seed) {
    canComputeCoefficients(seed,
        CustomTricubicInterpolatingFunction::computeCoefficientsInlineCollectTerms);
  }

  private static void canComputeCoefficients(RandomSeed seed, UnaryOperator<double[]> fun) {
    Assumptions.assumeTrue(TestSettings.allow(TestComplexity.MEDIUM));

    final UniformRandomProvider r = RngFactory.create(seed.get());
    final DoubleDoubleBiPredicate equality = Predicates.doublesAreClose(1e-6, 0);

    final int N = 3000;
    for (int i = 0; i < N; i++) {
      final double[] table = new double[64];
      for (int j = 0; j < 64; j++) {
        table[j] = r.nextDouble();
      }
      double[] a = CustomTricubicInterpolatingFunction.computeCoefficients(table);
      double[] b = fun.apply(table);
      TestAssertions.assertArrayTest(a, b, equality);
    }
  }
}
