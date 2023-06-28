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

package uk.ac.sussex.gdsc.core.math.interpolation;

import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.simple.RandomSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link CustomTricubicFunction}.
 */
@SuppressWarnings({"javadoc"})
class CustomTricubicFunctionTest {
  @Test
  void canGetCoefficients() {
    final UniformRandomProvider rng = RandomSource.SPLIT_MIX_64.create();
    final float[] fa = new float[64];
    final double[] da = new double[64];
    for (int i = 0; i < fa.length; i++) {
      da[i] = fa[i] = rng.nextFloat();
    }
    final FloatCustomTricubicFunction ff =
        (FloatCustomTricubicFunction) CustomTricubicFunctionUtils.create(fa.clone());
    final DoubleCustomTricubicFunction df =
        (DoubleCustomTricubicFunction) CustomTricubicFunctionUtils.create(da.clone());

    final double[] da2 = new double[64];

    ff.getCoefficients(da2);
    Assertions.assertArrayEquals(da, da2);
    df.getCoefficients(da2);
    Assertions.assertArrayEquals(da, da2);

    final float[] fa2 = new float[64];

    ff.getCoefficients(fa2);
    Assertions.assertArrayEquals(fa, fa2);
    df.getCoefficients(fa2);
    Assertions.assertArrayEquals(fa, fa2);

    // test scaling
    ff.scale(2).getCoefficients(fa2);
    df.scale(2).getCoefficients(da2);
    for (int i = 0; i < fa.length; i++) {
      Assertions.assertEquals(da[i] * 2, fa2[i]);
      Assertions.assertEquals(da[i] * 2, da2[i]);
    }
  }

  @Test
  void canConvert() {
    final UniformRandomProvider rng = RandomSource.SPLIT_MIX_64.create();
    final float[] fa = new float[64];
    final double[] da = new double[64];
    for (int i = 0; i < fa.length; i++) {
      da[i] = fa[i] = rng.nextFloat();
    }
    final FloatCustomTricubicFunction ff =
        (FloatCustomTricubicFunction) CustomTricubicFunctionUtils.create(fa.clone());
    final DoubleCustomTricubicFunction df =
        (DoubleCustomTricubicFunction) CustomTricubicFunctionUtils.create(da.clone());

    Assertions.assertSame(ff, ff.toSinglePrecision());
    Assertions.assertSame(df, df.toDoublePrecision());

    // Convert
    final DoubleCustomTricubicFunction df2 = (DoubleCustomTricubicFunction) ff.toDoublePrecision();
    final FloatCustomTricubicFunction ff2 = (FloatCustomTricubicFunction) df.toSinglePrecision();

    final float[] fa2 = new float[64];
    final double[] da2 = new double[64];
    ff2.getCoefficients(fa2);
    df2.getCoefficients(da2);

    Assertions.assertArrayEquals(fa, fa2);
    Assertions.assertArrayEquals(da, da2);
  }

  @Test
  void testValueThrows() {

    final double[] df_da = new double[3];
    final double[] d2f_da2 = new double[3];
    final double under = -0.000001;
    final double over = 1.000001;
    for (int i = 0; i < 2; i++) {
      final CustomTricubicFunction ff = (i == 0) ? CustomTricubicFunctionUtils.create(new float[64])
          : CustomTricubicFunctionUtils.create(new double[64]);

      ff.value(0, 0, 0);
      ff.value(1, 1, 1);
      Assertions.assertThrows(OutOfRangeException.class, () -> {
        ff.value(under, 0, 0);
      });
      Assertions.assertThrows(OutOfRangeException.class, () -> {
        ff.value(0, under, 0);
      });
      Assertions.assertThrows(OutOfRangeException.class, () -> {
        ff.value(0, 0, under);
      });
      Assertions.assertThrows(OutOfRangeException.class, () -> {
        ff.value(over, 0, 0);
      });
      Assertions.assertThrows(OutOfRangeException.class, () -> {
        ff.value(0, over, 0);
      });
      Assertions.assertThrows(OutOfRangeException.class, () -> {
        ff.value(0, 0, over);
      });

      ff.value(0, 0, 0, df_da);
      ff.value(1, 1, 1);
      Assertions.assertThrows(OutOfRangeException.class, () -> {
        ff.value(under, 0, 0, df_da);
      });
      Assertions.assertThrows(OutOfRangeException.class, () -> {
        ff.value(0, under, 0, df_da);
      });
      Assertions.assertThrows(OutOfRangeException.class, () -> {
        ff.value(0, 0, under, df_da);
      });
      Assertions.assertThrows(OutOfRangeException.class, () -> {
        ff.value(over, 0, 0, df_da);
      });
      Assertions.assertThrows(OutOfRangeException.class, () -> {
        ff.value(0, over, 0, df_da);
      });
      Assertions.assertThrows(OutOfRangeException.class, () -> {
        ff.value(0, 0, over, df_da);
      });

      ff.value(0, 0, 0, df_da, d2f_da2);
      ff.value(1, 1, 1);
      Assertions.assertThrows(OutOfRangeException.class, () -> {
        ff.value(under, 0, 0, df_da, d2f_da2);
      });
      Assertions.assertThrows(OutOfRangeException.class, () -> {
        ff.value(0, under, 0, df_da, d2f_da2);
      });
      Assertions.assertThrows(OutOfRangeException.class, () -> {
        ff.value(0, 0, under, df_da, d2f_da2);
      });
      Assertions.assertThrows(OutOfRangeException.class, () -> {
        ff.value(over, 0, 0, df_da, d2f_da2);
      });
      Assertions.assertThrows(OutOfRangeException.class, () -> {
        ff.value(0, over, 0, df_da, d2f_da2);
      });
      Assertions.assertThrows(OutOfRangeException.class, () -> {
        ff.value(0, 0, over, df_da, d2f_da2);
      });
    }
  }

  @Test
  void testCreateThrows() {
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      CustomTricubicFunctionUtils.create((double[]) null);
    });
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      CustomTricubicFunctionUtils.create(new double[1]);
    });
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      CustomTricubicFunctionUtils.create((float[]) null);
    });
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      CustomTricubicFunctionUtils.create(new float[1]);
    });
  }

  @Test
  void testAreEqual() {
    Assertions.assertTrue(CustomTricubicFunction.areEqual(1, 2, 0, 1));
    Assertions.assertTrue(CustomTricubicFunction.areEqual(2, 1, 0, 1));
    Assertions.assertFalse(CustomTricubicFunction.areEqual(1, 2, 0, 0.5));
    Assertions.assertFalse(CustomTricubicFunction.areEqual(2, 1, 0, 0.5));
    Assertions.assertTrue(CustomTricubicFunction.areEqual(1, 2, 0.5, 0));
    Assertions.assertTrue(CustomTricubicFunction.areEqual(2, 1, 0.5, 0));
    Assertions.assertFalse(CustomTricubicFunction.areEqual(1, 2, 0.1, 0));
    Assertions.assertFalse(CustomTricubicFunction.areEqual(2, 1, 0.1, 0));
  }

  @Test
  void testIsBoundary() {
    Assertions.assertTrue(CustomTricubicFunction.isBoundary(new CubicSplinePosition(0)));
    Assertions.assertFalse(CustomTricubicFunction.isBoundary(new CubicSplinePosition(0.5)));
    Assertions.assertTrue(CustomTricubicFunction.isBoundary(new CubicSplinePosition(1)));
  }

  @Test
  void testSearch() {

    // An ascending table of coefficients should have the max in the upper corner
    // (1,1,1).
    // The search starts in the lower corner (0,0,0).
    final double[] da = new double[64];
    for (int i = 0; i < da.length; i++) {
      da[i] = i;
    }
    final DoubleCustomTricubicFunction df =
        (DoubleCustomTricubicFunction) CustomTricubicFunctionUtils.create(da.clone());
    final double[] e = new double[] {1, 1, 1, df.value(1, 1, 1)};
    double[] result;
    // Try different parameters: refinements, relativeError, absoluteError
    final boolean maximum = true;
    result = df.search(maximum, 0, 0, 0);
    Assertions.assertArrayEquals(e, result);
    result = df.search(maximum, 2, 0, 0);
    Assertions.assertArrayEquals(e, result);
    result = df.search(maximum, 2, 0.1, 0);
    Assertions.assertArrayEquals(e, result);
    result = df.search(maximum, 2, 0, e[3] * 1e-3);
    Assertions.assertArrayEquals(e, result);
  }
}
