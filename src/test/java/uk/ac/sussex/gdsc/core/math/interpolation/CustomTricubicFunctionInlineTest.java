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

package uk.ac.sussex.gdsc.core.math.interpolation;

import java.util.Formatter;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * This class is used to in-line the computation for the CustomTricubicFunction.
 *
 * <p>The ordering of the computation is set to multiply by the power ZYX and the cubic coefficient
 * last.
 *
 * <p>This allows the power table to be precomputed and the result should match the non-precomputed
 * version. This includes scaling the power table by 2,3,6 for computation of the gradients.
 */
@SuppressWarnings({"javadoc"})
class CustomTricubicFunctionInlineTest {
  private static Logger logger;

  @BeforeAll
  public static void beforeAll() {
    logger = Logger.getLogger(CustomTricubicFunctionInlineTest.class.getName());
  }

  @AfterAll
  public static void afterAll() {
    logger = null;
  }

  /** The new-line separator. */
  private static String NL = System.lineSeparator();
  /** Number of points. */
  private static final short N = 4;
  /** Number of points - 1. */
  private static final short N_1 = 3;
  /** Number of points - 2. */
  private static final short N_2 = 2;

  /**
   * Used to create the inline value function.
   *
   * @return the function text.
   */
  private static String inlineValue0() {
    final StringBuilder sb = new StringBuilder(NL);
    // The formal computation is output. This can be factorised.
    try (Formatter formatter = new Formatter(sb)) {
      for (int k = 0; k < N; k++) {
        for (int j = 0; j < N; j++) {
          for (int i = 0; i < N; i++) {
            //@formatter:off
            formatter.format("result += z.x%d * y.x%d * x.x%d * coeff.x%dy%dz%d;\n",
                k, j, i, i, j, k);
            //@formatter:on
          }
        }
      }
    }

    return finaliseInlineFunction(sb);
  }

  private static String finaliseInlineFunction(StringBuilder sb) {
    String result = sb.toString();
    // Replace the use of 1 in multiplications
    result = result.replace("x.x0", "1");
    result = result.replace("y.x0", "1");
    result = result.replace("z.x0", "1");
    result = result.replace(" * 1", "");
    result = result.replace(" 1 *", "");

    return result;
  }

  /**
   * Used to create the inline value function for first-order gradients.
   *
   * @return the function text.
   */
  private static String inlineValue1() {
    final StringBuilder sb = new StringBuilder(NL);
    try (Formatter formatter = new Formatter(sb)) {
      // Gradients are described in:
      // Babcock & Zhuang (2017)
      // Analyzing Single Molecule Localization Microscopy Data Using Cubic Splines
      // Scientific Reports 7, Article number: 552
      //
      // // Formal computation
      // for (int k = 0; k < N; k++) {
      // for (int j = 0; j < N; j++) {
      // for (int i = 0; i < N; i++) {
      // zCyBxA = powerZ[k] * powerY[j] * powerX[i];
      // result += zCyBxA * a[ai];
      // if (i < N_1)
      // derivative1[0] += (i+1) * zCyBxA * a[getIndex(i+1, j, k)];
      // if (j < N_1)
      // derivative1[1] += (j+1) * zCyBxA * a[getIndex(i, j+1, k)];
      // if (k < N_1)
      // derivative1[2] += (k+1) * zCyBxA * a[getIndex(i, j, k+1)];
      // }
      // }
      // }

      // Note: The value is not included as the function can just call the function below it, e.g.
      // return value0(x, y, z);

      // To make it easier to factorise this each component is done together.
      // The parameter that is being differentiated is the common factor. Put this first.
      for (int i = 0; i < N_1; i++) {
        for (int k = 0; k < N; k++) {
          for (int j = 0; j < N; j++) {
            //@formatter:off
            formatter.format("derivative1[0] += %d * x.x%d * z.x%d * y.x%d * coeff.x%dy%dz%d;\n",
                i+1, i, k, j, i+1, j, k);
            //@formatter:on
          }
        }
      }
      for (int j = 0; j < N_1; j++) {
        for (int k = 0; k < N; k++) {
          for (int i = 0; i < N; i++) {
            //@formatter:off
            formatter.format("derivative1[1] += %d * y.x%d * z.x%d * x.x%d * coeff.x%dy%dz%d;\n",
                j+1, j, k, i, i, j+1, k);
            //@formatter:on
          }
        }
      }
      for (int k = 0; k < N_1; k++) {
        for (int j = 0; j < N; j++) {
          for (int i = 0; i < N; i++) {
            //@formatter:off
            formatter.format("derivative1[2] += %d * z.x%d * y.x%d * x.x%d * coeff.x%dy%dz%d;\n",
                k+1, k, j, i, i, j, k+1);
            //@formatter:on
          }
        }
      }
    }

    return finaliseInlineFunction(sb);
  }

  /**
   * Used to create the inline value function for second-order gradients.
   *
   * @return the function text.
   */
  private static String inlineValue2() {
    final StringBuilder sb = new StringBuilder(NL);
    try (Formatter formatter = new Formatter(sb)) {
      // Gradients are described in:
      // Babcock & Zhuang (2017)
      // Analyzing Single Molecule Localization Microscopy Data Using Cubic Splines
      // Scientific Reports 7, Article number: 552
      //
      // //// Formal computation
      // for (int k = 0; k < N; k++) {
      // for (int j = 0; j < N; j++) {
      // for (int i = 0; i < N; i++) {
      // zCyBxA = zCyB * powerX[i];
      // result += zCyBxA * a[ai];
      // if (i < N_1)
      // {
      // derivative1[0] += (i+1) * zCyBxA * a[getIndex(i+1, j, k)];
      // if (i < N_2)
      // derivative2[0] += (i+1) * (i + 2) * zCyBxA * a[getIndex(i + 2, j, k)];
      // }
      // if (j < N_1)
      // {
      // derivative1[1] += (j+1) * zCyBxA * a[getIndex(i, j+1, k)];
      // if (j < N_2)
      // derivative2[1] += (j+1) * (j + 2) * zCyBxA * a[getIndex(i, j + 2, k)];
      // }
      // if (k < N_1)
      // {
      // derivative1[2] += (k+1) * zCyBxA * a[getIndex(i, j, k+1)];
      // if (k < N_2)
      // derivative2[2] += (k+1) * (k + 2) * zCyBxA * a[getIndex(i, j, k + 2)];
      // }
      // }
      // }
      // }

      // Note: The value is not included as the function can just call the function below it, e.g.
      // return value0(x, y, z);
      // Or repeat the computation.

      // To make it easier to factorise this each component is done together.
      // The parameter that is being differentiated is the common factor. Put this first.
      for (int i = 0; i < N_2; i++) {
        for (int k = 0; k < N; k++) {
          for (int j = 0; j < N; j++) {
            //@formatter:off
            formatter.format("derivative2[0] += %d * x.x%d * z.x%d * y.x%d * coeff.x%dy%dz%d;\n",
                (i+1)*(i+2), i, k, j, i+2, j, k);
            //@formatter:on
          }
        }
      }
      for (int j = 0; j < N_2; j++) {
        for (int k = 0; k < N; k++) {
          for (int i = 0; i < N; i++) {
            //@formatter:off
            formatter.format("derivative2[1] += %d * y.x%d * z.x%d * x.x%d * coeff.x%dy%dz%d;\n",
                (j+1)*(j+2), j, k, i, i, j+2, k);
            //@formatter:on
          }
        }
      }
      for (int k = 0; k < N_2; k++) {
        for (int j = 0; j < N; j++) {
          for (int i = 0; i < N; i++) {
            //@formatter:off
            formatter.format("derivative2[2] += %d * z.x%d * y.x%d * x.x%d * coeff.x%dy%dz%d;\n",
                (k+1)*(k+2), k, j, i, i, j, k+2);
            //@formatter:on
          }
        }
      }
    }

    return finaliseInlineFunction(sb);
  }

  private final Level level = Level.FINEST;

  // @formatter:off

  /**
   * Build data for:
   * {@link DoubleCustomTricubicFunction#value0(CubicSplinePosition, CubicSplinePosition,
   * CubicSplinePosition)}.
   */
  @Test
  void canConstructInlineValue0() {
    Assumptions.assumeTrue(logger.isLoggable(level));
    logger.log(level, inlineValue0());
  }

  /**
   * Build data for:
   * {@link DoubleCustomTricubicFunction#value1(CubicSplinePosition, CubicSplinePosition,
   * CubicSplinePosition, double[])}.
   */
  @Test
  void canConstructInlineValue1() {
    Assumptions.assumeTrue(logger.isLoggable(level));
    logger.log(level, inlineValue1());
  }

  /**
   * Build data for:
   * {@link DoubleCustomTricubicFunction#value2(CubicSplinePosition, CubicSplinePosition,
   * CubicSplinePosition, double[], double[])}.
   */
  @Test
  void canConstructInlineValue2() {
    Assumptions.assumeTrue(logger.isLoggable(level));
    logger.log(level, inlineValue2());
  }
}
