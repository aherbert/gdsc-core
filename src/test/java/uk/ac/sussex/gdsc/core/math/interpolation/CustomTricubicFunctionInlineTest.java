package uk.ac.sussex.gdsc.core.math.interpolation;

import uk.ac.sussex.gdsc.test.utils.functions.FunctionUtils;

import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.procedure.TObjectIntProcedure;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Formatter;
import java.util.logging.Level;
import java.util.logging.Logger;

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
public class CustomTricubicFunctionInlineTest {
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

  private static int getIndex(int powerX, int powerY, int powerZ) {
    return powerX + 4 * (powerY + 4 * powerZ);
  }

  /**
   * Used to create the inline value function.
   *
   * @return the function text.
   */
  String inlineValue0() {
    String zCyB;
    final StringBuilder sb = new StringBuilder(NL);
    try (Formatter formatter = new Formatter(sb)) {
      for (int k = 0, ai = 0; k < N; k++) {
        for (int j = 0; j < N; j++) {
          zCyB = append_zCyB(formatter, k, j);

          for (int i = 0; i < N; i++, ai++) {
            formatter.format("result += %s * x.x%d * coeff.x%dy%dz%d;\n", zCyB, i, i, j, k);
          }
        }
      }
    }

    return finaliseInlineFunction(sb);
  }

  static String append_zCyB(Formatter formatter, int powerZ, int powerY) {
    String zCyB;
    if (powerY == 0) {
      if (powerZ == 0) {
        zCyB = "1";
      } else {
        zCyB = "z.x" + powerZ;
      }
    } else if (powerZ == 0) {
      zCyB = "y.x" + powerY;
    } else {
      formatter.format("zCyB = z.x%d * y.x%d;\n", powerZ, powerY);
      zCyB = "zCyB";
    }
    return zCyB;
  }

  static String finaliseInlineFunction(StringBuilder sb) {
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
   * Used to create the inline value function for first-order gradients with power table.
   *
   * @return the function text.
   */
  String inlineValue0WithPowerTable() {
    final TObjectIntHashMap<String> map = new TObjectIntHashMap<>(64);

    final StringBuilder sb = new StringBuilder(NL);
    sb.append("return ");
    for (int k = 0; k < N; k++) {
      for (int j = 0; j < N; j++) {
        for (int i = 0; i < N; i++) {
          appendPower(map, sb, i, j, k, i, j, k);
        }
      }
    }
    sb.append(";\n");

    // Each entry should be unique indicating that the result is optimal
    map.forEachEntry(new TObjectIntProcedure<String>() {
      @Override
      public boolean execute(String key, int value) {
        if (value > 1) {
          logger.info(FunctionUtils.getSupplier("%s = %d\n", key, value));
          return false;
        }
        return true;
      }
    });

    return finaliseInlinePowerTableFunction(sb);
  }

  /**
   * Used to create the inline value function for first-order gradients.
   *
   * @return the function text.
   */
  String inlineValue1() {
    String zCyB;
    String zCyBxA;
    final StringBuilder sb = new StringBuilder(NL);
    try (Formatter formatter = new Formatter(sb)) {
      // Gradients are described in:
      // Babcock & Zhuang (2017)
      // Analyzing Single Molecule Localization Microscopy Data Using Cubic Splines
      // Scientific Reports 7, Article number: 552
      for (int k = 0; k < N; k++) {
        for (int j = 0; j < N; j++) {
          zCyB = append_zCyB(formatter, k, j);

          for (int i = 0; i < N; i++) {
            zCyBxA = append_zCyBxA(formatter, zCyB, i);

            //@formatter:off
            formatter.format("result += %s * coeff.x%dy%dz%d;\n", zCyBxA, i, j, k);
            if (i < N_1) {
              formatter.format("derivative1[0] += %d * %s * coeff.x%dy%dz%d;\n", i+1, zCyBxA, i+1, j, k);
             }
            if (j < N_1) {
              formatter.format("derivative1[1] += %d * %s * coeff.x%dy%dz%d;\n", j+1, zCyBxA, i, j+1, k);
            }
            if (k < N_1) {
              formatter.format("derivative1[2] += %d * %s * coeff.x%dy%dz%d;\n", k+1, zCyBxA, i, j, k+1);
            }
            //@formatter:on

            // Formal computation
            // zCyBxA = powerZ[k] * powerY[j] * powerX[i];
            // result += zCyBxA * a[ai];
            // if (i < N_1)
            // derivative1[0] += (i+1) * zCyBxA * a[getIndex(i+1, j, k)];
            // if (j < N_1)
            // derivative1[1] += (j+1) * zCyBxA * a[getIndex(i, j+1, k)];
            // if (k < N_1)
            // derivative1[2] += (k+1) * zCyBxA * a[getIndex(i, j, k+1)];
          }
        }
      }
    }

    return finaliseInlineFunction(sb);
  }

  static String append_zCyBxA(Formatter formatter, String zCyB, int power) {
    String zCyBxA;
    if (power == 0) {
      zCyBxA = zCyB;
    } else if (zCyB.equals("1")) {
      zCyBxA = "x.x" + power;
    } else {
      formatter.format("zCyBxA = %s * x.x%d;\n", zCyB, power);
      zCyBxA = "zCyBxA";
    }
    return zCyBxA;
  }

  /**
   * Used to create the inline value function for first-order gradients with power table.
   *
   * @return the function text.
   */
  String inlineValue1WithPowerTable() {
    final TObjectIntHashMap<String> map = new TObjectIntHashMap<>(64);

    final StringBuilder sb = new StringBuilder(NL);
    // Inline each gradient array in order.
    // Maybe it will help the optimiser?
    sb.append("derivative1[0] =");
    for (int k = 0; k < N; k++) {
      for (int j = 0; j < N; j++) {
        for (int i = 0; i < N; i++) {
          if (i < N_1) {
            appendPower(map, sb, i + 1, j, k, i, j, k);
          }
        }
      }
    }
    sb.append(";\n");
    sb.append("derivative1[1] =");
    for (int k = 0; k < N; k++) {
      for (int j = 0; j < N; j++) {
        for (int i = 0; i < N; i++) {
          if (j < N_1) {
            appendPower(map, sb, i, j + 1, k, i, j, k);
          }
        }
      }
    }
    sb.append(";\n");
    sb.append("derivative1[2] =");
    for (int k = 0; k < N; k++) {
      for (int j = 0; j < N; j++) {
        for (int i = 0; i < N; i++) {
          if (k < N_1) {
            appendPower(map, sb, i, j, k + 1, i, j, k);
          }
        }
      }
    }
    sb.append(";\n");
    sb.append("return ");
    for (int k = 0; k < N; k++) {
      for (int j = 0; j < N; j++) {
        for (int i = 0; i < N; i++) {
          appendPower(map, sb, i, j, k, i, j, k);
        }
      }
    }
    sb.append(";\n");

    // Each entry should be unique indicating that the result is optimal
    map.forEachEntry(new TObjectIntProcedure<String>() {
      @Override
      public boolean execute(String key, int value) {
        if (value > 1) {
          logger.info(FunctionUtils.getSupplier("%s = %d\n", key, value));
          return false;
        }
        return true;
      }
    });

    return finaliseInlinePowerTableFunction(sb);
  }

  static void appendPower(TObjectIntHashMap<String> map, StringBuilder sb, int i1, int j1, int k1,
      int i2, int j2, int k2) {
    int nh;
    int nl;
    if (i1 != i2) {
      nh = i1;
      nl = i2;
    } else if (j1 != j2) {
      nh = j1;
      nl = j2;
    } else {
      nh = k1;
      nl = k2;
    }
    int powerN = 1;
    while (nh > nl) {
      powerN *= nh;
      nh--;
    }
    final String sum = String.format("%d * table.x%dy%dz%d * coeff.x%dy%dz%d\n", powerN, i2, j2, k2, i1, j1, k1);
    map.adjustOrPutValue(sum, 1, 1);
    sb.append("+ ").append(sum);
  }

  /**
   * Used to create the inline value function for first-order gradients with power table.
   *
   * @return the function text.
   */
  String inlineValue1WithPowerTableN() {
    final TObjectIntHashMap<String> map = new TObjectIntHashMap<>(64);

    final StringBuilder sb = new StringBuilder(NL);
    // Inline each gradient array in order.
    // Maybe it will help the optimiser?
    sb.append("derivative1[0] =");
    for (int k = 0; k < N; k++) {
      for (int j = 0; j < N; j++) {
        for (int i = 0; i < N; i++) {
          if (i < N_1) {
            appendPowerN(map, sb, i + 1, j, k, i, j, k);
          }
        }
      }
    }
    sb.append(";\n");
    sb.append("derivative1[1] =");
    for (int k = 0; k < N; k++) {
      for (int j = 0; j < N; j++) {
        for (int i = 0; i < N; i++) {
          if (j < N_1) {
            appendPowerN(map, sb, i, j + 1, k, i, j, k);
          }
        }
      }
    }
    sb.append(";\n");
    sb.append("derivative1[2] =");
    for (int k = 0; k < N; k++) {
      for (int j = 0; j < N; j++) {
        for (int i = 0; i < N; i++) {
          if (k < N_1) {
            appendPowerN(map, sb, i, j, k + 1, i, j, k);
          }
        }
      }
    }
    sb.append(";\n");
    sb.append("return ");
    for (int k = 0; k < N; k++) {
      for (int j = 0; j < N; j++) {
        for (int i = 0; i < N; i++) {
          appendPowerN(map, sb, i, j, k, i, j, k);
        }
      }
    }
    sb.append(";\n");

    // Each entry should be unique indicating that the result is optimal
    map.forEachEntry(new TObjectIntProcedure<String>() {
      @Override
      public boolean execute(String key, int value) {
        if (value > 1) {
          logger.info(FunctionUtils.getSupplier("%s = %d", key, value));
          return false;
        }
        return true;
      }
    });

    return finaliseInlinePowerTableFunction(sb);
  }

  static void appendPowerN(TObjectIntHashMap<String> map, StringBuilder sb, int i1, int j1, int k1,
      int i2, int j2, int k2) {
    int nh;
    int nl;
    if (i1 != i2) {
      nh = i1;
      nl = i2;
    } else if (j1 != j2) {
      nh = j1;
      nl = j2;
    } else {
      nh = k1;
      nl = k2;
    }
    int powerN = 1;
    while (nh > nl) {
      powerN *= nh;
      nh--;
    }
    final String sum = String.format("table%d.x%dy%dz%d * coeff.x%dy%dz%d\n", powerN, i2, j2, k2, i1,j1,k1);
    map.adjustOrPutValue(sum, 1, 1);
    sb.append("+ ").append(sum);
  }

  static String finaliseInlinePowerTableFunction(StringBuilder sb) {
    String result = sb.toString();
    result = result.replace("return +", "return ");
    result = result.replace("=+", "=");
    result = result.replace(" 1 * ", "");
    result = result.replace("table1", "table");
    return result;
  }

  /**
   * Used to create the inline value function for second-order gradients.
   *
   * @return the function text.
   */
  String inlineValue2() {
    String zCyB;
    String zCyBxA;
    final StringBuilder sb = new StringBuilder(NL);
    try (Formatter formatter = new Formatter(sb)) {
      // Gradients are described in:
      // Babcock & Zhuang (2017)
      // Analyzing Single Molecule Localization Microscopy Data Using Cubic Splines
      // Scientific Reports 7, Article number: 552
      for (int k = 0; k < N; k++) {
        for (int j = 0; j < N; j++) {
          zCyB = append_zCyB(formatter, k, j);

          for (int i = 0; i < N; i++) {
            zCyBxA = append_zCyBxA(formatter, zCyB, i);

            //@formatter:off
            formatter.format("result += %s * coeff.x%dy%dz%d;\n", zCyBxA, i, j, k);
            if (i < N_1) {
              formatter.format("derivative1[0] += %d * %s * coeff.x%dy%dz%d;\n", i+1, zCyBxA, i+1, j, k);
              if (i < N_2) {
                formatter.format("derivative2[0] += %d * %s * coeff.x%dy%dz%d;\n", (i+1)*(i+2), zCyBxA, i+2, j, k);
              }
            }
            if (j < N_1) {
              formatter.format("derivative1[1] += %d * %s * coeff.x%dy%dz%d;\n", j+1, zCyBxA, i, j+1, k);
              if (j < N_2) {
                formatter.format("derivative2[1] += %d * %s * coeff.x%dy%dz%d;\n", (j+1)*(j+2), zCyBxA, i, j+2, k);
              }
            }
            if (k < N_1)
            {
              formatter.format("derivative1[2] += %d * %s * coeff.x%dy%dz%d;\n", k+1, zCyBxA, i, j, k+1);
              if (k < N_2) {
                formatter.format("derivative2[2] += %d * %s * coeff.x%dy%dz%d;\n", (k+1)*(k+2), zCyBxA, i, j, k+2);
              }
            }
            //@formatter:on

            //// Formal computation
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
          }
        }
      }
    }

    return finaliseInlineFunction(sb);
  }

  /**
   * Used to create the inline value function for second-order gradients with power table.
   *
   * @return the function text.
   */
  String inlineValue2WithPowerTable() {
    final TObjectIntHashMap<String> map = new TObjectIntHashMap<>(64);
    final StringBuilder sb = new StringBuilder(NL);
    // Inline each gradient array in order.
    // Maybe it will help the optimiser?
    sb.append("derivative1[0] =");
    for (int k = 0; k < N; k++) {
      for (int j = 0; j < N; j++) {
        for (int i = 0; i < N; i++) {
          if (i < N_1) {
            appendPower(map, sb, i + 1, j, k, i, j, k);
          }
        }
      }
    }
    sb.append(";\n");
    sb.append("derivative1[1] =");
    for (int k = 0; k < N; k++) {
      for (int j = 0; j < N; j++) {
        for (int i = 0; i < N; i++) {
          if (j < N_1) {
            appendPower(map, sb, i, j + 1, k, i, j, k);
          }
        }
      }
    }
    sb.append(";\n");
    sb.append("derivative1[2] =");
    for (int k = 0; k < N; k++) {
      for (int j = 0; j < N; j++) {
        for (int i = 0; i < N; i++) {
          if (k < N_1) {
            appendPower(map, sb, i, j, k + 1, i, j, k);
          }
        }
      }
    }
    sb.append(";\n");
    sb.append("derivative2[0] =");
    for (int k = 0; k < N; k++) {
      for (int j = 0; j < N; j++) {
        for (int i = 0; i < N; i++) {
          if (i < N_2) {
            appendPower(map, sb, i + 2, j, k, i, j, k);
          }
        }
      }
    }
    sb.append(";\n");
    sb.append("derivative2[1] =");
    for (int k = 0; k < N; k++) {
      for (int j = 0; j < N; j++) {
        for (int i = 0; i < N; i++) {
          if (j < N_2) {
            appendPower(map, sb, i, j + 2, k, i, j, k);
          }
        }
      }
    }
    sb.append(";\n");
    sb.append("derivative2[2] =");
    for (int k = 0; k < N; k++) {
      for (int j = 0; j < N; j++) {
        for (int i = 0; i < N; i++) {
          if (k < N_2) {
            appendPower(map, sb, i, j, k + 2, i, j, k);
          }
        }
      }
    }
    sb.append(";\n");
    sb.append("return ");
    for (int k = 0; k < N; k++) {
      for (int j = 0; j < N; j++) {
        for (int i = 0; i < N; i++) {
          appendPower(map, sb, i, j, k, i, j, k);
        }
      }
    }
    sb.append(";\n");

    // Each entry should be unique indicating that the result is optimal
    map.forEachEntry(new TObjectIntProcedure<String>() {
      @Override
      public boolean execute(String key, int value) {
        if (value > 1) {
          logger.info(FunctionUtils.getSupplier("%s = %d", key, value));
          return false;
        }
        return true;
      }
    });

    return finaliseInlinePowerTableFunction(sb);
  }

  /**
   * Used to create the inline value function for second-order gradients with power table.
   *
   * @return the function text.
   */
  String inlineValue2WithPowerTableN() {
    final TObjectIntHashMap<String> map = new TObjectIntHashMap<>(64);
    final StringBuilder sb = new StringBuilder(NL);
    // Inline each gradient array in order.
    // Maybe it will help the optimiser?
    sb.append("derivative1[0] =");
    for (int k = 0; k < N; k++) {
      for (int j = 0; j < N; j++) {
        for (int i = 0; i < N; i++) {
          if (i < N_1) {
            appendPowerN(map, sb, i + 1, j, k, i, j, k);
          }
        }
      }
    }
    sb.append(";\n");
    sb.append("derivative1[1] =");
    for (int k = 0; k < N; k++) {
      for (int j = 0; j < N; j++) {
        for (int i = 0; i < N; i++) {
          if (j < N_1) {
            appendPowerN(map, sb, i, j + 1, k, i, j, k);
          }
        }
      }
    }
    sb.append(";\n");
    sb.append("derivative1[2] =");
    for (int k = 0; k < N; k++) {
      for (int j = 0; j < N; j++) {
        for (int i = 0; i < N; i++) {
          if (k < N_1) {
            appendPowerN(map, sb, i, j, k + 1, i, j, k);
          }
        }
      }
    }
    sb.append(";\n");
    sb.append("derivative2[0] =");
    for (int k = 0; k < N; k++) {
      for (int j = 0; j < N; j++) {
        for (int i = 0; i < N; i++) {
          if (i < N_2) {
            appendPowerN(map, sb, i + 2, j, k, i, j, k);
          }
        }
      }
    }
    sb.append(";\n");
    sb.append("derivative2[1] =");
    for (int k = 0; k < N; k++) {
      for (int j = 0; j < N; j++) {
        for (int i = 0; i < N; i++) {
          if (j < N_2) {
            appendPowerN(map, sb, i, j + 2, k, i, j, k);
          }
        }
      }
    }
    sb.append(";\n");
    sb.append("derivative2[2] =");
    for (int k = 0; k < N; k++) {
      for (int j = 0; j < N; j++) {
        for (int i = 0; i < N; i++) {
          if (k < N_2) {
            appendPowerN(map, sb, i, j, k + 2, i, j, k);
          }
        }
      }
    }
    sb.append(";\n");
    sb.append("return ");
    for (int k = 0; k < N; k++) {
      for (int j = 0; j < N; j++) {
        for (int i = 0; i < N; i++) {
          appendPowerN(map, sb, i, j, k, i, j, k);
        }
      }
    }
    sb.append(";\n");

    // Each entry should be unique indicating that the result is optimal
    map.forEachEntry(new TObjectIntProcedure<String>() {
      @Override
      public boolean execute(String key, int value) {
        if (value > 1) {
          logger.info(FunctionUtils.getSupplier("%s = %d", key, value));
          return false;
        }
        return true;
      }
    });

    return finaliseInlinePowerTableFunction(sb);
  }

  private final Level level = Level.INFO;

  // @formatter:off

  /**
   * Build data for:
   * {@link DoubleCustomTricubicFunction#value0(CubicSplinePosition, CubicSplinePosition, CubicSplinePosition)}.
   */
  @Test
  public void canConstructInlineValue0() {
    Assumptions.assumeTrue(logger.isLoggable(level));
    logger.log(level, inlineValue0());
  }

  /**
   * Build data for:
   * {@link DoubleCustomTricubicFunction#value1(CubicSplinePosition, CubicSplinePosition, CubicSplinePosition, double[])}.
   */
  @Test
  public void canConstructInlineValue1() {
    Assumptions.assumeTrue(logger.isLoggable(level));
    logger.log(level, inlineValue1());
  }

  /**
   * Build data for:
   * {@link DoubleCustomTricubicFunction#value2(CubicSplinePosition, CubicSplinePosition, CubicSplinePosition, double[], double[])}.
   */
  @Test
  public void canConstructInlineValue2() {
    Assumptions.assumeTrue(logger.isLoggable(level));
    logger.log(level, inlineValue2());
  }

  /**
   * Build data for:
   * {@link DoubleCustomTricubicFunction#value(DoubleCubicSplineData)};
   * {@link DoubleCustomTricubicFunction#value(FloatCubicSplineData)}.
   */
  @Test
  public void canConstructInlineValue0WithPowerTable() {
    Assumptions.assumeTrue(logger.isLoggable(level));
    logger.log(level, inlineValue0WithPowerTable());
  }

  /**
   * Build data for:
   * {@link DoubleCustomTricubicFunction#value(DoubleCubicSplineData, double[])};
   * {@link DoubleCustomTricubicFunction#gradient(DoubleCubicSplineData, double[])};
   * {@link DoubleCustomTricubicFunction#value(FloatCubicSplineData, double[])};
   * {@link DoubleCustomTricubicFunction#gradient(FloatCubicSplineData, double[])}.
   */
  @Test
  public void canConstructInlineValue1WithPowerTable() {
    Assumptions.assumeTrue(logger.isLoggable(level));
    logger.log(level, inlineValue1WithPowerTable());
  }

 /**
  * Build data for:
  * {@link DoubleCustomTricubicFunction#value(DoubleCubicSplineData, DoubleCubicSplineData, DoubleCubicSplineData, double[])};
  * {@link DoubleCustomTricubicFunction#value(FloatCubicSplineData, FloatCubicSplineData, FloatCubicSplineData, double[])}.
  */
  @Test
  public void canConstructInlineValue1WithPowerTableN() {
    Assumptions.assumeTrue(logger.isLoggable(level));
    logger.log(level, inlineValue1WithPowerTableN());
  }

  /**
   * Build data for:
   * {@link DoubleCustomTricubicFunction#value(DoubleCubicSplineData, double[], double[])};
   * {@link DoubleCustomTricubicFunction#value(FloatCubicSplineData, double[], double[])}.
   */
  @Test
  public void canConstructInlineValue2WithPowerTable() {
    Assumptions.assumeTrue(logger.isLoggable(level));
    logger.log(level, inlineValue2WithPowerTable());
  }

  /**
   * Build data for:
   * {@link DoubleCustomTricubicFunction#value(DoubleCubicSplineData, DoubleCubicSplineData, DoubleCubicSplineData, DoubleCubicSplineData, double[], double[])};
   * {@link DoubleCustomTricubicFunction#value(FloatCubicSplineData, FloatCubicSplineData, FloatCubicSplineData, FloatCubicSplineData, double[], double[])}.
   */
  @Test
  public void canConstructInlineValue2WithPowerTableN() {
    Assumptions.assumeTrue(logger.isLoggable(level));
    logger.log(level, inlineValue2WithPowerTableN());
  }
}
