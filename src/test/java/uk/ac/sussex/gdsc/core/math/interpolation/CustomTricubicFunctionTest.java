package uk.ac.sussex.gdsc.core.math.interpolation;

import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.simple.RandomSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"javadoc"})
public class CustomTricubicFunctionTest {
//  @Test
//  public void canAccessCoefficients() {
//    final UniformRandomProvider rng = RandomSource.create(RandomSource.MWC_256);
//    final float[] fa = new float[64];
//    final double[] da = new double[64];
//    for (int i = 0; i < fa.length; i++) {
//      da[i] = fa[i] = rng.nextFloat();
//    }
//    final FloatCustomTricubicFunction ff =
//        (FloatCustomTricubicFunction) CustomTricubicFunction.create(fa.clone());
//    final DoubleCustomTricubicFunction df =
//        (DoubleCustomTricubicFunction) CustomTricubicFunction.create(da.clone());
//    for (int i = 0; i < fa.length; i++) {
//      Assertions.assertEquals(da[i], ff.get(i));
//      Assertions.assertEquals(da[i], df.get(i));
//      Assertions.assertEquals(fa[i], ff.getf(i));
//      Assertions.assertEquals(fa[i], df.getf(i));
//    }
//
//    Assertions.assertArrayEquals(da, ff.getCoefficients());
//    Assertions.assertArrayEquals(da, df.getCoefficients());
//
//    // test scaling
//    ff.scale(2);
//    df.scale(2);
//    for (int i = 0; i < fa.length; i++) {
//      Assertions.assertEquals(da[i] * 2, ff.get(i));
//      Assertions.assertEquals(da[i] * 2, df.get(i));
//      Assertions.assertEquals(fa[i] * 2, ff.getf(i));
//      Assertions.assertEquals(fa[i] * 2, df.getf(i));
//    }
//  }
//
//  @Test
//  public void canConvert() {
//    final UniformRandomProvider rng = RandomSource.create(RandomSource.MWC_256);
//    final float[] fa = new float[64];
//    final double[] da = new double[64];
//    for (int i = 0; i < fa.length; i++) {
//      da[i] = fa[i] = rng.nextFloat();
//    }
//    final FloatCustomTricubicFunction ff =
//        (FloatCustomTricubicFunction) CustomTricubicFunction.create(fa.clone());
//    final DoubleCustomTricubicFunction df =
//        (DoubleCustomTricubicFunction) CustomTricubicFunction.create(da.clone());
//
//    Assertions.assertSame(ff, ff.toSinglePrecision());
//    Assertions.assertSame(df, df.toDoublePrecision());
//
//    // Convert
//    final DoubleCustomTricubicFunction df2 = (DoubleCustomTricubicFunction) ff.toDoublePrecision();
//    final FloatCustomTricubicFunction ff2 = (FloatCustomTricubicFunction) df.toSinglePrecision();
//
//    Assertions.assertArrayEquals(da, ff2.getCoefficients());
//    Assertions.assertArrayEquals(da, df2.getCoefficients());
//  }
//
//  @Test
//  public void canGetIndex() {
//    final int N = 4;
//    for (int k = 0, ai = 0; k < N; k++) {
//      for (int j = 0; j < N; j++) {
//        for (int i = 0; i < N; i++, ai++) {
//          Assertions.assertEquals(ai, CustomTricubicFunction.getIndex(i, j, k));
//        }
//      }
//    }
//  }
//
//  @Test
//  public void testComputePowerTableThrows() {
//    CustomTricubicFunction.computePowerTable(0, 0, 0);
//    CustomTricubicFunction.computePowerTable(1, 1, 1);
//    final double under = -0.000001;
//    final double over = 1.000001;
//    Assertions.assertThrows(OutOfRangeException.class, () -> {
//      CustomTricubicFunction.computePowerTable(under, 0, 0);
//    });
//    Assertions.assertThrows(OutOfRangeException.class, () -> {
//      CustomTricubicFunction.computePowerTable(0, under, 0);
//    });
//    Assertions.assertThrows(OutOfRangeException.class, () -> {
//      CustomTricubicFunction.computePowerTable(0, 0, under);
//    });
//    Assertions.assertThrows(OutOfRangeException.class, () -> {
//      CustomTricubicFunction.computePowerTable(over, 0, 0);
//    });
//    Assertions.assertThrows(OutOfRangeException.class, () -> {
//      CustomTricubicFunction.computePowerTable(0, over, 0);
//    });
//    Assertions.assertThrows(OutOfRangeException.class, () -> {
//      CustomTricubicFunction.computePowerTable(0, 0, over);
//    });
//  }
//
//  @Test
//  public void testValueThrows() {
//
//    final double[] df_da = new double[3];
//    final double[] d2f_da2 = new double[3];
//    final double under = -0.000001;
//    final double over = 1.000001;
//    for (int i = 0; i < 2; i++) {
//      final CustomTricubicFunction ff = (i == 0) ? CustomTricubicFunction.create(new float[64])
//          : CustomTricubicFunction.create(new double[64]);
//
//      ff.value(0, 0, 0);
//      ff.value(1, 1, 1);
//      Assertions.assertThrows(OutOfRangeException.class, () -> {
//        ff.value(under, 0, 0);
//      });
//      Assertions.assertThrows(OutOfRangeException.class, () -> {
//        ff.value(0, under, 0);
//      });
//      Assertions.assertThrows(OutOfRangeException.class, () -> {
//        ff.value(0, 0, under);
//      });
//      Assertions.assertThrows(OutOfRangeException.class, () -> {
//        ff.value(over, 0, 0);
//      });
//      Assertions.assertThrows(OutOfRangeException.class, () -> {
//        ff.value(0, over, 0);
//      });
//      Assertions.assertThrows(OutOfRangeException.class, () -> {
//        ff.value(0, 0, over);
//      });
//
//      ff.value(0, 0, 0, df_da);
//      ff.value(1, 1, 1);
//      Assertions.assertThrows(OutOfRangeException.class, () -> {
//        ff.value(under, 0, 0, df_da);
//      });
//      Assertions.assertThrows(OutOfRangeException.class, () -> {
//        ff.value(0, under, 0, df_da);
//      });
//      Assertions.assertThrows(OutOfRangeException.class, () -> {
//        ff.value(0, 0, under, df_da);
//      });
//      Assertions.assertThrows(OutOfRangeException.class, () -> {
//        ff.value(over, 0, 0, df_da);
//      });
//      Assertions.assertThrows(OutOfRangeException.class, () -> {
//        ff.value(0, over, 0, df_da);
//      });
//      Assertions.assertThrows(OutOfRangeException.class, () -> {
//        ff.value(0, 0, over, df_da);
//      });
//
//      ff.value(0, 0, 0, df_da, d2f_da2);
//      ff.value(1, 1, 1);
//      Assertions.assertThrows(OutOfRangeException.class, () -> {
//        ff.value(under, 0, 0, df_da, d2f_da2);
//      });
//      Assertions.assertThrows(OutOfRangeException.class, () -> {
//        ff.value(0, under, 0, df_da, d2f_da2);
//      });
//      Assertions.assertThrows(OutOfRangeException.class, () -> {
//        ff.value(0, 0, under, df_da, d2f_da2);
//      });
//      Assertions.assertThrows(OutOfRangeException.class, () -> {
//        ff.value(over, 0, 0, df_da, d2f_da2);
//      });
//      Assertions.assertThrows(OutOfRangeException.class, () -> {
//        ff.value(0, over, 0, df_da, d2f_da2);
//      });
//      Assertions.assertThrows(OutOfRangeException.class, () -> {
//        ff.value(0, 0, over, df_da, d2f_da2);
//      });
//    }
//  }
//
//  @Test
//  public void testCreateThrows() {
//    Assertions.assertThrows(IllegalArgumentException.class, () -> {
//      CustomTricubicFunction.create((double[]) null);
//    });
//    Assertions.assertThrows(IllegalArgumentException.class, () -> {
//      CustomTricubicFunction.create(new double[1]);
//    });
//    Assertions.assertThrows(IllegalArgumentException.class, () -> {
//      CustomTricubicFunction.create((float[]) null);
//    });
//    Assertions.assertThrows(IllegalArgumentException.class, () -> {
//      CustomTricubicFunction.create(new float[1]);
//    });
//  }
//
//  @Test
//  public void testAreEqual() {
//    Assertions.assertTrue(CustomTricubicFunction.areEqual(1, 2, 0, 1));
//    Assertions.assertTrue(CustomTricubicFunction.areEqual(2, 1, 0, 1));
//    Assertions.assertFalse(CustomTricubicFunction.areEqual(1, 2, 0, 0.5));
//    Assertions.assertFalse(CustomTricubicFunction.areEqual(2, 1, 0, 0.5));
//    Assertions.assertTrue(CustomTricubicFunction.areEqual(1, 2, 0.5, 0));
//    Assertions.assertTrue(CustomTricubicFunction.areEqual(2, 1, 0.5, 0));
//    Assertions.assertFalse(CustomTricubicFunction.areEqual(1, 2, 0.1, 0));
//    Assertions.assertFalse(CustomTricubicFunction.areEqual(2, 1, 0.1, 0));
//  }
//
//  @Test
//  public void testComputePowerTable() {
//    final double x = 0.1;
//    final double y = 0.3;
//    final double z = 0.7;
//    final CubicSplinePosition px = new CubicSplinePosition(x);
//    final CubicSplinePosition py = new CubicSplinePosition(y);
//    final CubicSplinePosition pz = new CubicSplinePosition(z);
//    final double[] e = CustomTricubicFunction.computePowerTable(x, y, z);
//    final double[] o = CustomTricubicFunction.computePowerTable(px, py, pz);
//    Assertions.assertArrayEquals(e, o);
//    final float[] e2 = CustomTricubicFunction.computeFloatPowerTable(x, y, z);
//    final float[] o2 = CustomTricubicFunction.computeFloatPowerTable(px, py, pz);
//    Assertions.assertArrayEquals(e2, o2);
//  }
//
//  @Test
//  public void testIsBoundary() {
//
//    final double x = 0.5;
//    final double[] xyz = {x, x, x};
//    float[] fa = CustomTricubicFunction.computeFloatPowerTable(xyz[0], xyz[1], xyz[2]);
//    double[] da = CustomTricubicFunction.computePowerTable(xyz[0], xyz[1], xyz[2]);
//    // Go out of range for the index
//    for (int i = -1; i < 4; i++) {
//      Assertions.assertFalse(CustomTricubicFunction.isBoundary(i, fa));
//      Assertions.assertFalse(CustomTricubicFunction.isBoundary(i, da));
//    }
//    for (int i = 0; i < 3; i++) {
//      xyz[i] = 0;
//      fa = CustomTricubicFunction.computeFloatPowerTable(xyz[0], xyz[1], xyz[2]);
//      da = CustomTricubicFunction.computePowerTable(xyz[0], xyz[1], xyz[2]);
//      for (int j = 0; j < 3; j++) {
//        Assertions.assertEquals(i == j, CustomTricubicFunction.isBoundary(j, fa));
//        Assertions.assertEquals(i == j, CustomTricubicFunction.isBoundary(j, da));
//      }
//      xyz[i] = 1;
//      fa = CustomTricubicFunction.computeFloatPowerTable(xyz[0], xyz[1], xyz[2]);
//      da = CustomTricubicFunction.computePowerTable(xyz[0], xyz[1], xyz[2]);
//      for (int j = 0; j < 3; j++) {
//        Assertions.assertEquals(i == j, CustomTricubicFunction.isBoundary(j, fa));
//        Assertions.assertEquals(i == j, CustomTricubicFunction.isBoundary(j, da));
//      }
//      xyz[i] = x;
//    }
//  }
//
//  @Test
//  public void testSearch() {
//
//    // An ascending table of coefficients should have the max in the upper corner
//    // (1,1,1).
//    // The search starts in the lower corner (0,0,0).
//    final double[] da = new double[64];
//    for (int i = 0; i < da.length; i++) {
//      da[i] = i;
//    }
//    final DoubleCustomTricubicFunction df =
//        (DoubleCustomTricubicFunction) CustomTricubicFunction.create(da.clone());
//    final double[] e = new double[] {1, 1, 1, df.value(1, 1, 1)};
//    double[] result;
//    // Try different parameters: refinements, relativeError, absoluteError
//    final boolean maximum = true;
//    result = df.search(maximum, 0, 0, 0);
//    Assertions.assertArrayEquals(e, result);
//    result = df.search(maximum, 2, 0, 0);
//    Assertions.assertArrayEquals(e, result);
//    result = df.search(maximum, 2, 0.1, 0);
//    Assertions.assertArrayEquals(e, result);
//    result = df.search(maximum, 2, 0, e[3] * 1e-3);
//    Assertions.assertArrayEquals(e, result);
//  }
}
