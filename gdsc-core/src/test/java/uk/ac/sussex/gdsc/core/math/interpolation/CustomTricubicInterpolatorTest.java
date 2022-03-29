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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.util.Precision;
import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.core.data.DoubleArrayTrivalueProvider;
import uk.ac.sussex.gdsc.core.data.DoubleArrayValueProvider;
import uk.ac.sussex.gdsc.core.data.TrivalueProvider;
import uk.ac.sussex.gdsc.core.data.ValueProvider;
import uk.ac.sussex.gdsc.core.data.procedures.StandardTrivalueProcedure;
import uk.ac.sussex.gdsc.core.data.procedures.TrivalueProcedure;
import uk.ac.sussex.gdsc.core.logging.NullTrackProgress;
import uk.ac.sussex.gdsc.core.utils.DoubleEquality;
import uk.ac.sussex.gdsc.core.utils.MathUtils;
import uk.ac.sussex.gdsc.core.utils.SimpleArrayUtils;
import uk.ac.sussex.gdsc.core.utils.Statistics;
import uk.ac.sussex.gdsc.test.api.Predicates;
import uk.ac.sussex.gdsc.test.api.TestAssertions;
import uk.ac.sussex.gdsc.test.api.function.DoubleDoubleBiPredicate;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.rng.RngFactory;
import uk.ac.sussex.gdsc.test.utils.AssertionErrorCounter;
import uk.ac.sussex.gdsc.test.utils.RandomSeed;
import uk.ac.sussex.gdsc.test.utils.TestLogging.TestLevel;
import uk.ac.sussex.gdsc.test.utils.functions.FormatSupplier;

/**
 * Test for {@link CustomTricubicInterpolator}.
 */
@SuppressWarnings({"javadoc"})
class CustomTricubicInterpolatorTest {
  private static Logger logger;

  @BeforeAll
  public static void beforeAll() {
    logger = Logger.getLogger(CustomTricubicInterpolatorTest.class.getName());
  }

  @AfterAll
  public static void afterAll() {
    logger = null;
  }

  // Delta for numerical gradients
  private final double gradientDelta = 0.00001;

  @SeededTest
  void canConstructInterpolatingFunction(RandomSeed seed) {
    final UniformRandomProvider r = RngFactory.create(seed.get());

    final int x = 4;
    final int y = 5;
    final int z = 6;
    final double[][][] fval = createData(x, y, z, null);
    for (int i = 0; i < 3; i++) {
      final double[] xval = SimpleArrayUtils.newArray(x, r.nextDouble(), r.nextDouble());
      final double[] yval = SimpleArrayUtils.newArray(y, r.nextDouble(), r.nextDouble());
      final double[] zval = SimpleArrayUtils.newArray(z, r.nextDouble(), r.nextDouble());

      final CustomTricubicInterpolatingFunction f1 =
          new CustomTricubicInterpolator().interpolate(xval, yval, zval, fval);

      // Check the function knows its bounds
      Assertions.assertEquals(xval[0], f1.getMinX());
      Assertions.assertEquals(yval[0], f1.getMinY());
      Assertions.assertEquals(zval[0], f1.getMinZ());
      Assertions.assertEquals(xval[x - 1], f1.getMaxX());
      Assertions.assertEquals(yval[y - 1], f1.getMaxY());
      Assertions.assertEquals(zval[z - 1], f1.getMaxZ());
      Assertions.assertEquals(x - 2, f1.getMaxXSplinePosition());
      Assertions.assertEquals(y - 2, f1.getMaxYSplinePosition());
      Assertions.assertEquals(z - 2, f1.getMaxZSplinePosition());

      for (int j = 0; j < xval.length; j++) {
        Assertions.assertEquals(xval[j], f1.getXSplineValue(j));
      }
      for (int j = 0; j < yval.length; j++) {
        Assertions.assertEquals(yval[j], f1.getYSplineValue(j));
      }
      for (int j = 0; j < zval.length; j++) {
        Assertions.assertEquals(zval[j], f1.getZSplineValue(j));
      }

      Assertions.assertTrue(f1.isUniform());

      f1.toSinglePrecision();
    }
  }

  @Test
  void constructWithXArrayOfLength1Throws() {
    final int x = 1;
    final int y = 2;
    final int z = 2;
    final double[] xval = SimpleArrayUtils.newArray(x, 0, 1.0);
    final double[] yval = SimpleArrayUtils.newArray(y, 0, 1.0);
    final double[] zval = SimpleArrayUtils.newArray(z, 0, 1.0);
    final double[][][] fval = new double[x][y][z];
    Assertions.assertThrows(NumberIsTooSmallException.class, () -> {
      new CustomTricubicInterpolator().interpolate(xval, yval, zval, fval);
    });
  }

  @Test
  void constructWithYArrayOfLength1Throws() {
    final int x = 2;
    final int y = 1;
    final int z = 2;
    final double[] xval = SimpleArrayUtils.newArray(x, 0, 1.0);
    final double[] yval = SimpleArrayUtils.newArray(y, 0, 1.0);
    final double[] zval = SimpleArrayUtils.newArray(z, 0, 1.0);
    final double[][][] fval = new double[x][y][z];
    Assertions.assertThrows(NumberIsTooSmallException.class, () -> {
      new CustomTricubicInterpolator().interpolate(xval, yval, zval, fval);
    });
  }

  @Test
  void constructWithZArrayOfLength1Throws() {
    final int x = 2;
    final int y = 2;
    final int z = 1;
    final double[] xval = SimpleArrayUtils.newArray(x, 0, 1.0);
    final double[] yval = SimpleArrayUtils.newArray(y, 0, 1.0);
    final double[] zval = SimpleArrayUtils.newArray(z, 0, 1.0);
    final double[][][] fval = new double[x][y][z];
    Assertions.assertThrows(NumberIsTooSmallException.class, () -> {
      new CustomTricubicInterpolator().interpolate(xval, yval, zval, fval);
    });
  }

  @Test
  void canDetectIfUniform() {
    final int x = 3;
    final int y = 3;
    final int z = 3;
    final double xscale = 1;
    final double yscale = 0.5;
    final double zscale = 2.0;
    final double[] xval = SimpleArrayUtils.newArray(x, 0, xscale);
    final double[] yval = SimpleArrayUtils.newArray(y, 0, yscale);
    final double[] zval = SimpleArrayUtils.newArray(z, 0, zscale);
    final double[][][] fval = new double[x][y][z];
    final CustomTricubicInterpolatingFunction f1 =
        new CustomTricubicInterpolator().interpolate(xval, yval, zval, fval);
    Assertions.assertTrue(f1.isUniform());
    final double[] bad = xval.clone();
    bad[1] *= 1.001;
    Assertions.assertFalse(
        new CustomTricubicInterpolator().interpolate(bad, yval, zval, fval).isUniform());
    Assertions.assertFalse(
        new CustomTricubicInterpolator().interpolate(xval, bad, zval, fval).isUniform());
    Assertions.assertFalse(
        new CustomTricubicInterpolator().interpolate(xval, yval, bad, fval).isUniform());
    final double[] good = xval.clone();
    // The tolerance is relative but we have steps of size 1 so use as an absolute
    good[1] += CustomTricubicInterpolatingFunction.UNIFORM_TOLERANCE / 2;
    Assertions.assertTrue(
        new CustomTricubicInterpolator().interpolate(good, yval, zval, fval).isUniform());
    Assertions.assertTrue(
        new CustomTricubicInterpolator().interpolate(xval, good, zval, fval).isUniform());
    Assertions.assertTrue(
        new CustomTricubicInterpolator().interpolate(xval, yval, good, fval).isUniform());

    // Check scale. This can be used to map an interpolation point x to the
    // range 0-1 for power tables
    final double[] scale = f1.getScale();
    Assertions.assertEquals(xscale, scale[0]);
    Assertions.assertEquals(yscale, scale[1]);
    Assertions.assertEquals(zscale, scale[2]);
  }

  @Test
  void canDetectIfInteger() {
    final int x = 3;
    final int y = 3;
    final int z = 3;
    final double[] xval = SimpleArrayUtils.newArray(x, 0, 1.0);
    final double[] yval = SimpleArrayUtils.newArray(y, 4.2345, 1.0);
    final double[] zval = SimpleArrayUtils.newArray(z, 17.5, 1.0);
    final double[][][] fval = new double[x][y][z];
    final CustomTricubicInterpolatingFunction f1 =
        new CustomTricubicInterpolator().interpolate(xval, yval, zval, fval);
    Assertions.assertTrue(f1.isUniform());
    Assertions.assertTrue(f1.isInteger());
    final double[] bad = SimpleArrayUtils.newArray(x, 0,
        1.0 + CustomTricubicInterpolatingFunction.INTEGER_TOLERANCE);
    Assertions.assertTrue(
        new CustomTricubicInterpolator().interpolate(bad, yval, zval, fval).isUniform());
    Assertions.assertTrue(
        new CustomTricubicInterpolator().interpolate(xval, bad, zval, fval).isUniform());
    Assertions.assertTrue(
        new CustomTricubicInterpolator().interpolate(xval, yval, bad, fval).isUniform());
    Assertions.assertFalse(
        new CustomTricubicInterpolator().interpolate(bad, yval, zval, fval).isInteger());
    Assertions.assertFalse(
        new CustomTricubicInterpolator().interpolate(xval, bad, zval, fval).isInteger());
    Assertions.assertFalse(
        new CustomTricubicInterpolator().interpolate(xval, yval, bad, fval).isInteger());

    // Check scale. This can be used to map an interpolation point x to the
    // range 0-1 for power tables
    final double[] scale = f1.getScale();
    Assertions.assertEquals(1, scale[0]);
    Assertions.assertEquals(1, scale[1]);
    Assertions.assertEquals(1, scale[2]);
  }

  @SeededTest
  void canInterpolateWithNonIntegerAxis(RandomSeed seed) {
    canInterpolate(seed, false);
  }

  @SeededTest
  void canInterpolateWithIntegerAxis(RandomSeed seed) {
    canInterpolate(seed, true);
  }

  private void canInterpolate(RandomSeed seed, boolean isInteger) {
    final UniformRandomProvider r = RngFactory.create(seed.get());
    final int x = 4;
    final int y = 4;
    final int z = 4;
    final double[] xval = SimpleArrayUtils.newArray(x, 0, (isInteger) ? 1.0 : 1.5);
    final double[] yval = SimpleArrayUtils.newArray(y, 0, (isInteger) ? 1.0 : 0.5);
    final double[] zval = SimpleArrayUtils.newArray(z, 0, (isInteger) ? 1.0 : 2.0);
    final double[] testx = SimpleArrayUtils.newArray(6, xval[1], (xval[2] - xval[1]) / 5);
    final double[] testy = SimpleArrayUtils.newArray(6, yval[1], (yval[2] - yval[1]) / 5);
    final double[] testz = SimpleArrayUtils.newArray(6, zval[1], (zval[2] - zval[1]) / 5);
    final TricubicInterpolator f3 = new TricubicInterpolator();
    final BicubicInterpolator bi = new BicubicInterpolator();
    double[] face;
    double[] face2;
    double obs;
    double exp;
    final DoubleDoubleBiPredicate equality = Predicates.doublesAreClose(1e-8, 0);
    for (int i = 0; i < 3; i++) {
      final double[][][] fval = createData(x, y, z, (i == 0) ? null : r);

      final CustomTricubicInterpolatingFunction f1 =
          new CustomTricubicInterpolator().interpolate(xval, yval, zval, fval);
      // No longer possible to test verses the original TricubicInterpolatingFunction
      // as we handle edges differently
      for (final double zz : testz) {
        final IndexedCubicSplinePosition sz = f1.getZSplinePosition(zz);
        for (final double yy : testy) {
          final IndexedCubicSplinePosition sy = f1.getYSplinePosition(yy);

          for (final double xx : testx) {
            obs = f1.value(xx, yy, zz);
            final IndexedCubicSplinePosition sx = f1.getXSplinePosition(xx);
            final double obs2 = f1.value(sx, sy, sz);
            Assertions.assertEquals(obs, obs2);

            // Test against simple tricubic spline
            // which requires x,y,z in the range 0-1 for function values
            // x=-1 to x=2; y=-1 to y=2; and z=-1 to z=2
            // @formatter:off
            final double e2 = f3.getValue(fval,
                    (xx - xval[1]) / (xval[2] - xval[1]),
                    (yy - yval[1]) / (yval[2] - yval[1]),
                    (zz - zval[1]) / (zval[2] - zval[1]));
            // @formatter:on
            TestAssertions.assertTest(e2, obs, equality);
          }
        }
      }

      // Each face of the cube should interpolate as a Bicubic function
      face = extractXyFace(fval, 0);
      face2 = extractXyFace(fval, z - 1);
      for (final double xx : testx) {
        for (final double yy : testy) {
          obs = f1.value(xx, yy, 0);
          exp = bi.getValue(face, (xx - xval[1]) / (xval[2] - xval[1]),
              (yy - yval[1]) / (yval[2] - yval[1]));
          TestAssertions.assertTest(exp, obs, equality);
          obs = f1.value(xx, yy, zval[z - 1]);
          exp = bi.getValue(face2, (xx - xval[1]) / (xval[2] - xval[1]),
              (yy - yval[1]) / (yval[2] - yval[1]));
          TestAssertions.assertTest(exp, obs, equality);
        }
      }

      face = extractXzFace(fval, 0);
      face2 = extractXzFace(fval, y - 1);
      for (final double xx : testx) {
        for (final double zz : testz) {
          obs = f1.value(xx, 0, zz);
          exp = bi.getValue(face, (xx - xval[1]) / (xval[2] - xval[1]),
              (zz - zval[1]) / (zval[2] - zval[1]));
          TestAssertions.assertTest(exp, obs, equality);
          obs = f1.value(xx, yval[y - 1], zz);
          exp = bi.getValue(face2, (xx - xval[1]) / (xval[2] - xval[1]),
              (zz - zval[1]) / (zval[2] - zval[1]));
          TestAssertions.assertTest(exp, obs, equality);
        }
      }

      face = extractYzFace(fval, 0);
      face2 = extractYzFace(fval, z - 1);
      for (final double yy : testy) {
        for (final double zz : testz) {
          obs = f1.value(0, yy, zz);
          exp = bi.getValue(face, (yy - yval[1]) / (yval[2] - yval[1]),
              (zz - zval[1]) / (zval[2] - zval[1]));
          TestAssertions.assertTest(exp, obs, equality);
          obs = f1.value(xval[x - 1], yy, zz);
          exp = bi.getValue(face2, (yy - yval[1]) / (yval[2] - yval[1]),
              (zz - zval[1]) / (zval[2] - zval[1]));
          TestAssertions.assertTest(exp, obs, equality);
        }
      }
    }
  }

  private static double[] extractXyFace(double[][][] fval, int z) {
    final int maxx = fval.length;
    final int maxy = fval[0].length;

    final double[] f = new double[maxx * maxy];
    int index = 0;
    for (int y = 0; y < maxy; y++) {
      for (int x = 0; x < maxx; x++) {
        f[index++] = fval[x][y][z];
      }
    }
    return f;
  }

  private static double[] extractXzFace(double[][][] fval, int y) {
    final int maxx = fval.length;
    final int maxz = fval[0][0].length;

    final double[] f = new double[maxx * maxz];
    int index = 0;
    for (int z = 0; z < maxz; z++) {
      for (int x = 0; x < maxx; x++) {
        f[index++] = fval[x][y][z];
      }
    }
    return f;
  }

  private static double[] extractYzFace(double[][][] fval, int x) {
    final int maxy = fval[0].length;
    final int maxz = fval[0][0].length;

    final double[] f = new double[maxy * maxz];
    int index = 0;
    for (int z = 0; z < maxz; z++) {
      for (int y = 0; y < maxy; y++) {
        f[index++] = fval[x][y][z];
      }
    }
    return f;
  }

  @SeededTest
  void canInterpolateUsingPrecomputedPoints(RandomSeed seed) {
    final UniformRandomProvider r = RngFactory.create(seed.get());
    final int x = 4;
    final int y = 4;
    final int z = 4;
    final double xscale = 1;
    final double yscale = 0.5;
    final double zscale = 2.0;
    final double[] xval = SimpleArrayUtils.newArray(x, 0, xscale);
    final double[] yval = SimpleArrayUtils.newArray(y, 0, yscale);
    final double[] zval = SimpleArrayUtils.newArray(z, 0, zscale);
    final double[][][] fval = createData(x, y, z, null);
    final CustomTricubicInterpolatingFunction f1 =
        new CustomTricubicInterpolator().interpolate(xval, yval, zval, fval);
    final DoubleDoubleBiPredicate equality = Predicates.doublesAreClose(1e-8, 0);
    for (int i = 0; i < 3; i++) {
      double xx = r.nextDouble();
      double yy = r.nextDouble();
      double zz = r.nextDouble();

      // This is done unscaled
      final CubicSplinePosition px = new CubicSplinePosition(xx);
      final CubicSplinePosition py = new CubicSplinePosition(yy);
      final CubicSplinePosition pz = new CubicSplinePosition(zz);

      xx *= xscale;
      yy *= yscale;
      zz *= zscale;

      for (int zi = 1; zi < 3; zi++) {
        for (int yi = 1; yi < 3; yi++) {
          for (int xi = 1; xi < 3; xi++) {
            final double obs = f1.value(xval[xi] + xx, yval[yi] + yy, zval[zi] + zz);
            final double exp = f1.value(xi, yi, zi, px, py, pz);
            TestAssertions.assertTest(exp, obs, equality);
          }
        }
      }
    }
  }

  @Test
  void canInterpolateSingleNodeWithScale() {
    canInterpolateSingleNode(0.5, 1, 2);
  }

  @Test
  void canInterpolateSingleNodeWithNoScale() {
    canInterpolateSingleNode(1, 1, 1);
  }

  private void canInterpolateSingleNode(double xscale, double yscale, double zscale) {
    final int x = 4;
    final int y = 4;
    final int z = 4;
    final double[] xval = SimpleArrayUtils.newArray(x, 0, xscale);
    final double[] yval = SimpleArrayUtils.newArray(y, 0, yscale);
    final double[] zval = SimpleArrayUtils.newArray(z, 0, zscale);
    // If the scales are uniform then the version with the scale is identical to the
    // version without as it just packs and then unpacks the gradients.
    final boolean noScale = xscale == 1 && yscale == 1 && zscale == 1;
    if (!noScale) {
      // Create non-linear scale
      for (int i = 0, n = 2; i < 4; i++, n *= 2) {
        xval[i] *= n;
        yval[i] *= n;
        zval[i] *= n;
      }
    }
    final double[][][] fval = createData(x, y, z, null);
    final CustomTricubicInterpolatingFunction f1 =
        new CustomTricubicInterpolator().interpolate(xval, yval, zval, fval);

    final double[] exp = new double[64];
    final double[] obs = new double[64];
    f1.getSplineNode(1, 1, 1).getCoefficients(exp);

    if (noScale) {
      CustomTricubicInterpolator.create(new DoubleArrayTrivalueProvider(fval)).getCoefficients(obs);
    } else {
      CustomTricubicInterpolator
          .create(new DoubleArrayValueProvider(xval), new DoubleArrayValueProvider(yval),
              new DoubleArrayValueProvider(zval), new DoubleArrayTrivalueProvider(fval))
          .getCoefficients(obs);
    }

    Assertions.assertArrayEquals(exp, obs);
  }

  @Test
  void canInterpolateSingleNodeWithOffsetWithScale() {
    canInterpolateSingleNodeWithOffset(0.5, 1, 2);
  }

  @Test
  void canInterpolateSingleNodeWithOffsetWithNoScale() {
    canInterpolateSingleNodeWithOffset(1, 1, 1);
  }

  private void canInterpolateSingleNodeWithOffset(double xscale, double yscale, double zscale) {
    final int x = 6;
    final int y = 6;
    final int z = 6;
    final double[] xval = SimpleArrayUtils.newArray(x, 0, xscale);
    final double[] yval = SimpleArrayUtils.newArray(y, 0, yscale);
    final double[] zval = SimpleArrayUtils.newArray(z, 0, zscale);
    // If the scales are uniform then the version with the scale is identical to the
    // version without as it just packs and then unpacks the gradients.
    final boolean noScale = xscale == 1 && yscale == 1 && zscale == 1;
    if (!noScale) {
      // Create non-linear scale
      for (int i = 0, n = 2; i < x; i++, n *= 2) {
        xval[i] *= n;
        yval[i] *= n;
        zval[i] *= n;
      }
    }
    final double[][][] fval = createData(x, y, z, null);
    final CustomTricubicInterpolatingFunction f1 =
        new CustomTricubicInterpolator().interpolate(xval, yval, zval, fval);

    check(f1, xval, yval, zval, fval, noScale, 0, 0, 0);
    check(f1, xval, yval, zval, fval, noScale, 0, 1, 0);
    check(f1, xval, yval, zval, fval, noScale, 1, 1, 1);
    check(f1, xval, yval, zval, fval, noScale, 2, 1, 1);
    check(f1, xval, yval, zval, fval, noScale, 2, 3, 2);
    check(f1, xval, yval, zval, fval, noScale, 4, 4, 4);
  }

  private static void check(CustomTricubicInterpolatingFunction f1, double[] xval, double[] yval,
      double[] zval, double[][][] fval, boolean noScale, int indexX, int indexY, int indexZ) {
    final double[] exp = new double[64];
    final double[] obs = new double[64];
    f1.getSplineNode(indexX, indexY, indexZ).getCoefficients(exp);

    if (noScale) {
      CustomTricubicInterpolator
          .create(new DoubleArrayTrivalueProvider(fval), indexX, indexY, indexZ)
          .getCoefficients(obs);
    } else {
      CustomTricubicInterpolator.create(new DoubleArrayValueProvider(xval),
          new DoubleArrayValueProvider(yval), new DoubleArrayValueProvider(zval),
          new DoubleArrayTrivalueProvider(fval), indexX, indexY, indexZ).getCoefficients(obs);
    }

    Assertions.assertArrayEquals(exp, obs);
  }

  double[][][] createData(int x, int y, int z, UniformRandomProvider rng) {
    // Create a 2D Gaussian
    double sd = 1.0;
    double cx = x / 2.0;
    double cy = y / 2.0;
    double cz = z / 2.0;
    if (rng != null) {
      sd += rng.nextDouble() - 0.5;
      cx += rng.nextDouble() - 0.5;
      cy += rng.nextDouble() - 0.5;
      cz += rng.nextDouble() - 0.5;
    } else {
      // Prevent symmetry which breaks the evaluation of gradients
      cx += 0.01;
      cy += 0.01;
      cz += 0.01;
    }
    return createData(x, y, z, cx, cy, cz, sd);

    // double[][][] fval = new double[x][y][z];
    // double[] otherx = new double[x];
    // for (int zz = 0; zz < z; zz++)
    // {
    // double s2 = 2 * s * s;
    // for (int xx = 0; xx < x; xx++)
    // otherx[xx] = Maths.pow2(xx - cx) / s2;
    // for (int yy = 0; yy < y; yy++)
    // {
    // double othery = Maths.pow2(yy - cy) / s2;
    // for (int xx = 0; xx < x; xx++)
    // {
    // fval[xx][yy][zz] = Math.exp(otherx[xx] + othery);
    // }
    // }
    // // Move Gaussian
    // s += 0.1;
    // cx += 0.1;
    // cy -= 0.05;
    // }
    // return fval;
  }

  double amplitude;

  double[][][] createData(int x, int y, int z, double cx, double cy, double cz, double sd) {
    final double[][][] fval = new double[x][y][z];
    // Create a 2D Gaussian with astigmatism
    final double[] otherx = new double[x];
    final double zDepth = cz / 2;
    final double gamma = 1;

    // Compute the maximum amplitude
    double sx = sd * (1.0 + MathUtils.pow2((gamma) / zDepth) * 0.5);
    double sy = sd * (1.0 + MathUtils.pow2((-gamma) / zDepth) * 0.5);
    amplitude = 1.0 / (2 * Math.PI * sx * sy);

    // ImageStack stack = new ImageStack(x, y);
    for (int zz = 0; zz < z; zz++) {
      // float[] pixels = new float[x * y];
      // int i=0;

      // Astigmatism based on cz.
      // Width will be 1.5 at zDepth.
      final double dz = cz - zz;
      sx = sd * (1.0 + MathUtils.pow2((dz + gamma) / zDepth) * 0.5);
      sy = sd * (1.0 + MathUtils.pow2((dz - gamma) / zDepth) * 0.5);

      // TestLog.debug(logger,"%d = %f,%f", zz, sx, sy);

      final double norm = 1.0 / (2 * Math.PI * sx * sy);

      final double sx2 = 2 * sx * sx;
      final double sy2 = 2 * sy * sy;
      for (int xx = 0; xx < x; xx++) {
        otherx[xx] = -MathUtils.pow2(xx - cx) / sx2;
      }
      for (int yy = 0; yy < y; yy++) {
        final double othery = MathUtils.pow2(yy - cy) / sy2;
        for (int xx = 0; xx < x; xx++) {
          final double value = norm * Math.exp(otherx[xx] - othery);
          fval[xx][yy][zz] = value;
          // pixels[i++] = (float) value;
        }
      }
      // stack.addSlice(null, pixels);
    }
    // ImagePlus imp = Utils.display("Test", stack);
    // for (int i = 9; i-- > 0;)
    // imp.getCanvas().zoomIn(0, 0);
    return fval;
  }

  @SeededTest
  void canInterpolateWithGradientsWithNonIntegerAxis(RandomSeed seed) {
    canInterpolateWithGradients(seed, false);
  }

  @SeededTest
  void canInterpolateWithGradientsWithIntegerAxis(RandomSeed seed) {
    canInterpolateWithGradients(seed, false);
  }

  private void canInterpolateWithGradients(RandomSeed seed, boolean isInteger) {
    final UniformRandomProvider r = RngFactory.create(seed.get());
    final int x = 4;
    final int y = 4;
    final int z = 4;
    // Difference scales
    final double[] xval = SimpleArrayUtils.newArray(x, 0, (isInteger) ? 1.0 : 1.5);
    final double[] yval = SimpleArrayUtils.newArray(y, 0, (isInteger) ? 1.0 : 0.5);
    final double[] zval = SimpleArrayUtils.newArray(z, 0, (isInteger) ? 1.0 : 2.0);

    // Gradients on the node points are evaluated using different polynomials
    // as the function switches to a new cubic polynomial.
    // First-order gradients should be OK across nodes.
    // Second-order gradients will be incorrect.

    final double[] testx = SimpleArrayUtils.newArray(9, xval[1], (xval[2] - xval[1]) / 5);
    final double[] testy = SimpleArrayUtils.newArray(9, yval[1], (yval[2] - yval[1]) / 5);
    final double[] testz = SimpleArrayUtils.newArray(9, zval[1], (zval[2] - zval[1]) / 5);
    final double[] df_daH = new double[3];
    final double[] df_daL = new double[3];
    final double[] df_daA = new double[3];
    final double[] df_daB = new double[3];
    final double[] d2f_da2A = new double[3];
    final double[] d2f_da2B = new double[3];
    final DoubleEquality eq = new DoubleEquality(1e-6, 1e-3);

    // For single precision sometimes there are gradient failures
    final int size = testx.length * testy.length * testz.length;
    final int failLimit = AssertionErrorCounter.computeFailureLimit(size, 0.1);
    final DoubleDoubleBiPredicate equality = Predicates.doublesAreClose(1e-8, 0);

    for (int i = 0; i < 3; i++) {
      final double[][][] fval = createData(x, y, z, (i == 0) ? null : r);
      final CustomTricubicInterpolator in = new CustomTricubicInterpolator();
      for (final boolean singlePrecision : new boolean[] {false, true}) {
        in.setSinglePrecision(singlePrecision);

        // Set up the fail limits
        final int testFailLimit = (singlePrecision) ? failLimit : 0;
        final AssertionErrorCounter tc1 = new AssertionErrorCounter(testFailLimit, 3);
        final AssertionErrorCounter tc2 = new AssertionErrorCounter(testFailLimit, 3);
        final AssertionErrorCounter tc3 = new AssertionErrorCounter(testFailLimit, 3);
        final AssertionErrorCounter tc4 = new AssertionErrorCounter(testFailLimit, 3);

        final CustomTricubicInterpolatingFunction f1 = in.interpolate(xval, yval, zval, fval);

        for (final double zz : testz) {
          boolean onNode = Arrays.binarySearch(zval, zz) >= 0;
          final IndexedCubicSplinePosition sz = f1.getZSplinePosition(zz);

          for (final double yy : testy) {
            onNode = onNode || Arrays.binarySearch(yval, yy) >= 0;
            final IndexedCubicSplinePosition sy = f1.getYSplinePosition(yy);

            for (final double xx : testx) {
              onNode = onNode || Arrays.binarySearch(xval, xx) >= 0;

              final double exp = f1.value(xx, yy, zz);
              final double obs = f1.value(xx, yy, zz, df_daA);
              TestAssertions.assertTest(exp, obs, equality);

              double obs2 = f1.value(xx, yy, zz, df_daB, d2f_da2A);
              Assertions.assertEquals(obs, obs2);
              Assertions.assertArrayEquals(df_daA, df_daB);

              final IndexedCubicSplinePosition sx = f1.getXSplinePosition(xx);
              obs2 = f1.value(sx, sy, sz, df_daB);
              Assertions.assertEquals(obs, obs2);
              Assertions.assertArrayEquals(df_daA, df_daB);

              obs2 = f1.value(sx, sy, sz, df_daB, d2f_da2B);
              Assertions.assertEquals(obs, obs2);
              Assertions.assertArrayEquals(df_daA, df_daB);
              Assertions.assertArrayEquals(d2f_da2A, d2f_da2B);

              // Get gradient and check
              // if (singlePrecision) continue;

              final double[] a = new double[] {xx, yy, zz};
              for (int j = 0; j < 3; j++) {
                final int jj = j;
                final double h = Precision.representableDelta(a[j], gradientDelta);
                final double old = a[j];
                a[j] = old + h;
                final double high = f1.value(a[0], a[1], a[2], df_daH);
                a[j] = old - h;
                final double low = f1.value(a[0], a[1], a[2], df_daL);
                a[j] = old;
                // double df_da = (high - exp) / h;
                final double df_da = (high - low) / (2 * h);
                final boolean signOk = (df_da * df_daA[j]) >= 0;
                final boolean ok = eq.almostEqualRelativeOrAbsolute(df_da, df_daA[j]);
                if (!signOk) {
                  tc1.run(j, () -> {
                    Assertions.fail(df_da + " sign != " + df_daA[jj]);
                  });
                }
                // TestLog.debug(logger,"[%.2f,%.2f,%.2f] %f == [%d] %f ok=%b", xx, yy, zz,
                // df_da2, j,
                // df_daA[j], ok);
                // if (!ok)
                // {
                // TestLog.info(logger,"[%.1f,%.1f,%.1f] %f == [%d] %f?", xx, yy, zz, df_da2, j,
                // df_daA[j]);
                // }
                if (!ok) {
                  tc2.run(j, () -> {
                    Assertions.fail(df_da + " != " + df_daA[jj]);
                  });
                }

                final double d2f_da2 = (df_daH[j] - df_daL[j]) / (2 * h);
                if (!onNode) {
                  if (!((d2f_da2 * d2f_da2A[j]) >= 0)) {
                    tc3.run(j, () -> {
                      Assertions.fail(d2f_da2 + " sign != " + d2f_da2A[jj]);
                    });
                  }
                  // boolean ok = eq.almostEqualRelativeOrAbsolute(d2f_da2, d2f_da2A[j]);
                  // TestLog.debug(logger,"%d [%.2f,%.2f,%.2f] %f == [%d] %f ok=%b", j, xx, yy,
                  // zz, d2f_da2,
                  // j, d2f_da2A[j], ok);
                  // if (!ok)
                  // {
                  // TestLog.debug(logger,"%d [%.1f,%.1f,%.1f] %f == [%d] %f?", j, xx, yy, zz,
                  // d2f_da2, j,
                  // d2f_da2A[j]);
                  // }
                  if (!eq.almostEqualRelativeOrAbsolute(d2f_da2, d2f_da2A[j])) {
                    tc4.run(j, () -> {
                      Assertions.fail(d2f_da2 + " != " + d2f_da2A[jj]);
                    });
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  @SeededTest
  void canInterpolateWithGradientsUsingPrecomputedPointsWithNonIntegerAxis(RandomSeed seed) {
    canInterpolateWithGradientsUsingPrecomputedPoints(seed, false);
  }

  @SeededTest
  void canInterpolateWithGradientsUsingPrecomputedPointsWithIntegerAxis(RandomSeed seed) {
    canInterpolateWithGradientsUsingPrecomputedPoints(seed, true);
  }

  private void canInterpolateWithGradientsUsingPrecomputedPoints(RandomSeed seed,
      boolean isInteger) {
    final UniformRandomProvider r = RngFactory.create(seed.get());
    final int x = 4;
    final int y = 4;
    final int z = 4;
    final double xscale = 1;
    final double yscale = isInteger ? 1.0 : 0.5;
    final double zscale = isInteger ? 1.0 : 2.0;
    final double[] scale = {xscale, yscale, zscale};
    final double[] xval = SimpleArrayUtils.newArray(x, 0, xscale);
    final double[] yval = SimpleArrayUtils.newArray(y, 0, yscale);
    final double[] zval = SimpleArrayUtils.newArray(z, 0, zscale);
    final double[] df_daA = new double[3];
    final double[] df_daB = new double[3];
    final double[] d2f_da2A = new double[3];
    final double[] d2f_da2B = new double[3];
    double exp;
    double obs;
    double obs2;
    double[] e1A;
    double[] e1B;
    double[] e2B;

    final double[][][] fval = createData(x, y, z, null);
    final CustomTricubicInterpolator in = new CustomTricubicInterpolator();
    final DoubleDoubleBiPredicate dtableEquality = Predicates.doublesAreClose(1e-8, 0);
    for (final boolean singlePrecision : new boolean[] {false, true}) {
      in.setSinglePrecision(singlePrecision);
      final CustomTricubicInterpolatingFunction f1 = in.interpolate(xval, yval, zval, fval);
      for (int i = 0; i < 3; i++) {
        double xx = r.nextDouble();
        double yy = r.nextDouble();
        double zz = r.nextDouble();

        // This is done unscaled
        final CubicSplinePosition px = new CubicSplinePosition(xx);
        final CubicSplinePosition py = new CubicSplinePosition(yy);
        final CubicSplinePosition pz = new CubicSplinePosition(zz);

        xx *= xscale;
        yy *= yscale;
        zz *= zscale;

        for (int zi = 1; zi < 3; zi++) {
          final double z_ = zval[zi] + zz;
          for (int yi = 1; yi < 3; yi++) {
            final double y_ = yval[yi] + yy;
            for (int xi = 1; xi < 3; xi++) {
              final double x_ = xval[xi] + xx;

              final CustomTricubicFunction node = f1.getSplineNode(xi, yi, zi);

              exp = f1.value(x_, y_, z_);
              obs = f1.value(xi, yi, zi, px, py, pz);
              TestAssertions.assertTest(exp, obs, dtableEquality);

              // 1st order gradient

              exp = f1.value(x_, y_, z_, df_daA);
              obs = f1.value(xi, yi, zi, px, py, pz, df_daB);
              TestAssertions.assertTest(exp, obs, dtableEquality);
              TestAssertions.assertArrayTest(df_daA, df_daB, dtableEquality);

              // Store result
              e1A = df_daA.clone();
              e1B = df_daB.clone();

              // Node should be the same after scaling
              obs = node.value(px, py, pz, df_daA);
              for (int k = 0; k < 3; k++) {
                df_daA[k] /= scale[k];
              }
              TestAssertions.assertTest(exp, obs, dtableEquality);
              TestAssertions.assertArrayTest(e1A, df_daA, dtableEquality);

              // 2nd order gradient

              obs2 = f1.value(x_, y_, z_, df_daA, d2f_da2A);
              TestAssertions.assertTest(exp, obs2, dtableEquality);
              Assertions.assertArrayEquals(e1A, df_daA);

              obs2 = f1.value(xi, yi, zi, px, py, pz, df_daB, d2f_da2B);
              TestAssertions.assertTest(exp, obs2, dtableEquality);
              Assertions.assertArrayEquals(e1B, df_daB);
              TestAssertions.assertArrayTest(d2f_da2A, d2f_da2B, dtableEquality);

              // Store result
              e2B = d2f_da2B.clone();

              // Node should be the same after scaling
              obs2 = node.value(px, py, pz, df_daA, df_daB);
              for (int k = 0; k < 3; k++) {
                df_daA[k] /= scale[k];
                df_daB[k] /= (scale[k] * scale[k]);
              }
              TestAssertions.assertTest(exp, obs2, dtableEquality);
              TestAssertions.assertArrayTest(e1A, df_daA, dtableEquality);
              TestAssertions.assertArrayTest(e2B, df_daB, dtableEquality);
            }
          }
        }
      }
    }
  }

  @SeededTest
  void canInterpolateWithGradientsUsingPrecomputedTableSinglePrecisionWithNonIntegerAxis(
      RandomSeed seed) {
    canInterpolateWithGradientsUsingPrecomputedTableSinglePrecision(seed, false);
  }

  @SeededTest
  void canInterpolateWithGradientsUsingPrecomputedTableSinglePrecisionWithIntegerAxis(
      RandomSeed seed) {
    canInterpolateWithGradientsUsingPrecomputedTableSinglePrecision(seed, true);
  }

  private void canInterpolateWithGradientsUsingPrecomputedTableSinglePrecision(RandomSeed seed,
      boolean isInteger) {
    final UniformRandomProvider r = RngFactory.create(seed.get());
    final int x = 4;
    final int y = 4;
    final int z = 4;
    final double xscale = 1;
    final double yscale = isInteger ? 1.0 : 0.5;
    final double zscale = isInteger ? 1.0 : 2.0;
    final double[] xval = SimpleArrayUtils.newArray(x, 0, xscale);
    final double[] yval = SimpleArrayUtils.newArray(y, 0, yscale);
    final double[] zval = SimpleArrayUtils.newArray(z, 0, zscale);
    final double[] df_daA = new double[3];
    final double[] df_daB = new double[3];
    final double[] d2f_da2A = new double[3];
    final double[] d2f_da2B = new double[3];
    double exp;
    double obs;
    double obs2;
    double[] e1B;
    final double[][][] fval = createData(x, y, z, null);
    final CustomTricubicInterpolatingFunction f1 =
        new CustomTricubicInterpolator().interpolate(xval, yval, zval, fval);

    final DoubleDoubleBiPredicate valueTolerance = Predicates.doublesAreClose(1e-5, 0);
    // The gradients are computed using float and the tolerance is low
    final DoubleDoubleBiPredicate gradientTolerance = Predicates.doublesAreClose(5e-3, 0);
    final DoubleDoubleBiPredicate gradientTolerance2 = Predicates.doublesAreClose(1e-2, 0);

    // Extract nodes for testing
    final CustomTricubicFunction[] nodes = new CustomTricubicFunction[2 * 2 * 2];
    final CustomTricubicFunction[] fnodes = new CustomTricubicFunction[nodes.length];
    for (int zi = 1, i = 0; zi < 3; zi++) {
      for (int yi = 1; yi < 3; yi++) {
        for (int xi = 1; xi < 3; xi++, i++) {
          nodes[i] = f1.getSplineNodeReference(zi, yi, xi);
          fnodes[i] = nodes[i].toSinglePrecision();
        }
      }
    }

    for (int i = 0; i < 3; i++) {
      final double xx = r.nextDouble();
      final double yy = r.nextDouble();
      final double zz = r.nextDouble();

      final CubicSplinePosition px = new CubicSplinePosition(xx);
      final CubicSplinePosition py = new CubicSplinePosition(yy);
      final CubicSplinePosition pz = new CubicSplinePosition(zz);

      for (int ii = 0; ii < nodes.length; ii++) {
        final CustomTricubicFunction n1 = nodes[ii];
        final CustomTricubicFunction n2 = fnodes[ii];

        // Just check relative to the double-table version
        exp = n1.value(px, py, pz);
        obs = n2.value(px, py, pz);
        TestAssertions.assertTest(exp, obs, valueTolerance);

        // 1st order gradient

        exp = n1.value(px, py, pz, df_daA);
        obs2 = n2.value(px, py, pz, df_daB);
        TestAssertions.assertTest(exp, obs, valueTolerance);
        Assertions.assertEquals(obs, obs2);
        TestAssertions.assertArrayTest(df_daA, df_daB, gradientTolerance);

        // Store result
        e1B = df_daB.clone();

        // 2nd order gradient

        exp = n1.value(px, py, pz, df_daA, d2f_da2A);
        obs2 = n2.value(px, py, pz, df_daB, d2f_da2B);
        // Should be the same as the first-order gradient (which has already passed)
        TestAssertions.assertTest(exp, obs, valueTolerance);
        Assertions.assertEquals(obs, obs2);
        Assertions.assertArrayEquals(e1B, df_daB);

        // Check 2nd order gradient
        TestAssertions.assertArrayTest(d2f_da2A, d2f_da2B, gradientTolerance2);
      }
    }
  }

  @Test
  void canComputeNoInterpolation() {
    final int x = 4;
    final int y = 4;
    final int z = 4;
    final double xscale = 1;
    final double yscale = 0.5;
    final double zscale = 2.0;
    final double[] xval = SimpleArrayUtils.newArray(x, 0, xscale);
    final double[] yval = SimpleArrayUtils.newArray(y, 0, yscale);
    final double[] zval = SimpleArrayUtils.newArray(z, 0, zscale);
    final double[] df_daA = new double[3];
    final double[] df_daB = new double[3];
    final double[] d2f_da2A = new double[3];
    final double[] d2f_da2B = new double[3];
    double exp;
    double obs;
    final double[][][] fval = createData(x, y, z, null);
    final CustomTricubicInterpolatingFunction f1 =
        new CustomTricubicInterpolator().interpolate(xval, yval, zval, fval);

    // Extract node for testing
    final CustomTricubicFunction n1 = f1.getSplineNodeReference(1, 1, 1);
    final CustomTricubicFunction n2 = n1.toSinglePrecision();

    final CubicSplinePosition p0 = new CubicSplinePosition(0);

    // Check no interpolation is correct
    exp = n1.value(p0, p0, p0);
    obs = n1.value000();
    Assertions.assertEquals(exp, obs);

    exp = n1.value(p0, p0, p0, df_daA);
    obs = n1.value000(df_daB);
    Assertions.assertEquals(exp, obs);
    Assertions.assertArrayEquals(df_daA, df_daB);

    exp = n1.value(p0, p0, p0, df_daA, d2f_da2A);
    obs = n1.value000(df_daB, d2f_da2B);
    Assertions.assertEquals(exp, obs);
    Assertions.assertArrayEquals(df_daA, df_daB);
    Assertions.assertArrayEquals(d2f_da2A, d2f_da2B);

    // Check no interpolation is correct
    exp = n2.value(p0, p0, p0);
    obs = n2.value000();
    Assertions.assertEquals(exp, obs);

    exp = n2.value(p0, p0, p0, df_daA);
    obs = n2.value000(df_daB);
    Assertions.assertEquals(exp, obs);
    Assertions.assertArrayEquals(df_daA, df_daB);

    exp = n2.value(p0, p0, p0, df_daA, d2f_da2A);
    obs = n2.value000(df_daB, d2f_da2B);
    Assertions.assertEquals(exp, obs);
    Assertions.assertArrayEquals(df_daA, df_daB);
    Assertions.assertArrayEquals(d2f_da2A, d2f_da2B);
  }

  @Test
  void canComputeNonIntegerGridWithExecutorService() {
    canComputeWithExecutorService(1, 0.5, 2.0);
  }

  @Test
  void canComputeIntegerGridWithExecutorService() {
    canComputeWithExecutorService(1, 1, 1);
  }

  private void canComputeWithExecutorService(double xscale, double yscale, double zscale) {
    final int x = 6;
    final int y = 5;
    final int z = 4;
    final double[] xval = SimpleArrayUtils.newArray(x, 0, xscale);
    final double[] yval = SimpleArrayUtils.newArray(y, 0, yscale);
    final double[] zval = SimpleArrayUtils.newArray(z, 0, zscale);
    final double[][][] fval = createData(x, y, z, null);

    final CustomTricubicInterpolator interpolator = new CustomTricubicInterpolator();
    final CustomTricubicInterpolatingFunction f1 = interpolator.interpolate(xval, yval, zval, fval);
    final ExecutorService es = Executors.newFixedThreadPool(4);
    interpolator.setExecutorService(es);
    interpolator.setTaskSize(5);
    final CustomTricubicInterpolatingFunction f2 = interpolator.interpolate(xval, yval, zval, fval);
    es.shutdown();

    final double[] exp = new double[64];
    final double[] obs = new double[64];

    // Compare all nodes
    for (int i = 0; i < f1.getMaxXSplinePosition(); i++) {
      for (int j = 0; j < f1.getMaxYSplinePosition(); j++) {
        for (int k = 0; k < f1.getMaxZSplinePosition(); k++) {
          final DoubleCustomTricubicFunction n1 =
              (DoubleCustomTricubicFunction) f1.getSplineNodeReference(i, j, k);
          final DoubleCustomTricubicFunction n2 =
              (DoubleCustomTricubicFunction) f2.getSplineNodeReference(i, j, k);
          n1.getCoefficients(exp);
          n2.getCoefficients(obs);
          Assertions.assertArrayEquals(exp, obs);
        }
      }
    }
  }

  @Test
  void canSampleInterpolatedFunctionWithN1() {
    canSampleInterpolatedFunction(1);
  }

  @Test
  void canSampleInterpolatedFunctionWithN2() {
    canSampleInterpolatedFunction(2);
  }

  @Test
  void canSampleInterpolatedFunctionWithN3() {
    canSampleInterpolatedFunction(3);
  }

  private void canSampleInterpolatedFunction(int n) {
    final int x = 6;
    final int y = 5;
    final int z = 4;
    // Make it easy to have exact matching
    final double xscale = 2.0;
    final double yscale = 2.0;
    final double zscale = 2.0;
    final double[] xval = SimpleArrayUtils.newArray(x, 0, xscale);
    final double[] yval = SimpleArrayUtils.newArray(y, 0, yscale);
    final double[] zval = SimpleArrayUtils.newArray(z, 0, zscale);
    final double[][][] fval = createData(x, y, z, null);

    final CustomTricubicInterpolator interpolator = new CustomTricubicInterpolator();
    final CustomTricubicInterpolatingFunction f1 = interpolator.interpolate(xval, yval, zval, fval);

    final StandardTrivalueProcedure p = new StandardTrivalueProcedure();
    f1.sample(n, p);

    Assertions.assertArrayEquals(SimpleArrayUtils.newArray((x - 1) * n + 1, 0, xscale / n),
        p.getXAxis(), 1e-6);
    Assertions.assertArrayEquals(SimpleArrayUtils.newArray((y - 1) * n + 1, 0, yscale / n),
        p.getYAxis(), 1e-6);
    Assertions.assertArrayEquals(SimpleArrayUtils.newArray((z - 1) * n + 1, 0, zscale / n),
        p.getZAxis(), 1e-6);

    final DoubleDoubleBiPredicate equality = Predicates.doublesAreClose(1e-8, 0);

    for (int i = 0; i < p.getXAxis().length; i++) {
      for (int j = 0; j < p.getYAxis().length; j++) {
        for (int k = 0; k < p.getZAxis().length; k++) {
          // Test original function interpolated value against the sample
          TestAssertions.assertTest(f1.value(p.getXAxis()[i], p.getYAxis()[j], p.getZAxis()[k]),
              p.getValue()[i][j][k], equality);
        }
      }
    }
  }

  @Test
  void canDynamicallySampleFunctionWithN2() {
    canDynamicallySampleFunction(2, false);
  }

  @Test
  void canDynamicallySampleFunctionWithN3() {
    canDynamicallySampleFunction(3, false);
  }

  @Test
  void canDynamicallySampleFunctionWithN2WithExecutorService() {
    canDynamicallySampleFunction(2, true);
  }

  @Test
  void canDynamicallySampleFunctionWithN3WithExecutorService() {
    canDynamicallySampleFunction(3, true);
  }

  @SuppressWarnings("null")
  private void canDynamicallySampleFunction(int n, boolean threaded) {
    // This assumes that the sample method of the
    // CustomTricubicInterpolatingFunction works!

    final int x = 6;
    final int y = 5;
    final int z = 4;
    // No scale for this test
    final double[] xval = SimpleArrayUtils.newArray(x, 0, 1.0);
    final double[] yval = SimpleArrayUtils.newArray(y, 0, 1.0);
    final double[] zval = SimpleArrayUtils.newArray(z, 0, 1.0);
    final double[][][] fval = createData(x, y, z, null);

    final CustomTricubicInterpolator interpolator = new CustomTricubicInterpolator();
    final DoubleArrayTrivalueProvider f = new DoubleArrayTrivalueProvider(fval);
    final CustomTricubicInterpolatingFunction f1 =
        interpolator.interpolate(new DoubleArrayValueProvider(xval),
            new DoubleArrayValueProvider(yval), new DoubleArrayValueProvider(zval), f);

    final StandardTrivalueProcedure p = new StandardTrivalueProcedure();
    f1.sample(n, p);

    ExecutorService es = null;
    if (threaded) {
      es = Executors.newFixedThreadPool(4);
      interpolator.setExecutorService(es);
      interpolator.setTaskSize(5);
    }

    final StandardTrivalueProcedure p2 = new StandardTrivalueProcedure();
    interpolator.sample(f, n, p2);

    if (threaded) {
      es.shutdown();
    }

    Assertions.assertArrayEquals(p.getXAxis(), p2.getXAxis(), 1e-10);
    Assertions.assertArrayEquals(p.getYAxis(), p2.getYAxis(), 1e-10);
    Assertions.assertArrayEquals(p.getZAxis(), p2.getZAxis(), 1e-10);

    for (int i = 0; i < p.getXAxis().length; i++) {
      for (int j = 0; j < p.getYAxis().length; j++) {
        Assertions.assertArrayEquals(p.getValue()[i][j], p2.getValue()[i][j]);
      }
    }
  }

  @Test
  void canExternaliseDoubleFunction() throws IOException {
    canExternaliseFunction(false);
  }

  @Test
  void canExternaliseFloatFunction() throws IOException {
    canExternaliseFunction(true);
  }

  private void canExternaliseFunction(boolean singlePrecision) throws IOException {
    final int x = 6;
    final int y = 5;
    final int z = 4;
    final double xscale = 1;
    final double yscale = 0.5;
    final double zscale = 2.0;
    final double[] xval = SimpleArrayUtils.newArray(x, 0, xscale);
    final double[] yval = SimpleArrayUtils.newArray(y, 0, yscale);
    final double[] zval = SimpleArrayUtils.newArray(z, 0, zscale);
    final double[][][] fval = createData(x, y, z, null);

    final CustomTricubicInterpolator interpolator = new CustomTricubicInterpolator();
    final CustomTricubicInterpolatingFunction f1 = interpolator.interpolate(xval, yval, zval, fval);

    if (singlePrecision) {
      f1.toSinglePrecision();
    }

    final ByteArrayOutputStream b = new ByteArrayOutputStream();
    f1.write(b);

    final byte[] bytes = b.toByteArray();
    // TestLog.debug(logger,"Single precision = %b, size = %d, memory estimate =
    // %d", singlePrecision, bytes.length,
    // CustomTricubicInterpolatingFunction.estimateSize(new int[] { x, y, z })
    // .getMemoryFootprint(singlePrecision));
    final CustomTricubicInterpolatingFunction f2 =
        CustomTricubicInterpolatingFunction.read(new ByteArrayInputStream(bytes));

    final int n = 2;
    final StandardTrivalueProcedure p1 = new StandardTrivalueProcedure();
    f1.sample(n, p1);
    final StandardTrivalueProcedure p2 = new StandardTrivalueProcedure();
    f2.sample(n, p2);

    Assertions.assertArrayEquals(p1.getXAxis(), p2.getXAxis());
    Assertions.assertArrayEquals(p1.getYAxis(), p2.getYAxis());
    Assertions.assertArrayEquals(p1.getZAxis(), p2.getZAxis());

    for (int i = 0; i < p1.getXAxis().length; i++) {
      for (int j = 0; j < p1.getYAxis().length; j++) {
        for (int k = 0; k < p1.getZAxis().length; k++) {
          Assertions.assertEquals(f1.value(p1.getXAxis()[i], p1.getYAxis()[j], p1.getZAxis()[k]),
              f2.value(p1.getXAxis()[i], p1.getYAxis()[j], p1.getZAxis()[k]));
        }
      }
    }
  }

  @SeededTest
  void canInterpolateAcrossNodesForValueAndGradient1(RandomSeed seed) {
    final UniformRandomProvider r = RngFactory.create(seed.get());
    final int x = 4;
    final int y = 4;
    final int z = 4;
    // Difference scales
    final double[] xval = SimpleArrayUtils.newArray(x, 0, 1.0);
    final double[] yval = SimpleArrayUtils.newArray(y, 0, 1.0);
    final double[] zval = SimpleArrayUtils.newArray(z, 0, 1.0);
    final double[] df_daA = new double[3];
    final double[] df_daB = new double[3];
    final DoubleDoubleBiPredicate equality = Predicates.doublesAreClose(1e-8, 0);
    for (int ii = 0; ii < 3; ii++) {
      final double[][][] fval = createData(x, y, z, (ii == 0) ? null : r);
      final CustomTricubicInterpolatingFunction f1 =
          new CustomTricubicInterpolator().interpolate(xval, yval, zval, fval);
      for (int zz = f1.getMaxZSplinePosition(); zz > 0; zz--) {
        for (int yy = f1.getMaxYSplinePosition(); yy > 0; yy--) {
          for (int xx = f1.getMaxXSplinePosition(); xx > 0; xx--) {
            final CustomTricubicFunction next = f1.getSplineNodeReference(xx, yy, zz);

            // Test that interpolating at x=1 equals x=0 for the next node
            for (int k = 0; k < 2; k++) {
              final int zzz = zz - k;
              for (int j = 0; j < 2; j++) {
                final int yyy = yy - j;
                for (int i = 0; i < 2; i++) {
                  final int xxx = xx - i;
                  if (i + j + k == 0) {
                    continue;
                  }

                  final CustomTricubicFunction previous = f1.getSplineNodeReference(xxx, yyy, zzz);

                  final double exp = next.value(0, 0, 0, df_daA);
                  final double obs = previous.value(i, j, k, df_daB);
                  TestAssertions.assertTest(exp, obs, equality);

                  for (int c = 0; c < 3; c++) {
                    TestAssertions.assertTest(df_daA[c], df_daB[c], equality);
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  @SeededTest
  void cannotInterpolateAcrossNodesForGradient2(RandomSeed seed) {
    final UniformRandomProvider r = RngFactory.create(seed.get());
    final int x = 4;
    final int y = 4;
    final int z = 4;
    // Difference scales
    final double[] xval = SimpleArrayUtils.newArray(x, 0, 1.0);
    final double[] yval = SimpleArrayUtils.newArray(y, 0, 1.0);
    final double[] zval = SimpleArrayUtils.newArray(z, 0, 1.0);
    final double[] df_daA = new double[3];
    final double[] df_daB = new double[3];
    final double[] d2f_da2A = new double[3];
    final double[] d2f_da2B = new double[3];
    for (int ii = 0; ii < 3; ii++) {
      final Statistics[] value = new Statistics[3];
      for (int i = 0; i < value.length; i++) {
        value[i] = new Statistics();
      }

      final double[][][] fval = createData(x, y, z, (ii == 0) ? null : r);
      final CustomTricubicInterpolatingFunction f1 =
          new CustomTricubicInterpolator().interpolate(xval, yval, zval, fval);
      for (int zz = f1.getMaxZSplinePosition(); zz > 0; zz--) {
        for (int yy = f1.getMaxYSplinePosition(); yy > 0; yy--) {
          for (int xx = f1.getMaxXSplinePosition(); xx > 0; xx--) {
            final CustomTricubicFunction next = f1.getSplineNodeReference(xx, yy, zz);

            // Test that interpolating at x=1 equals x=0 for the next node
            for (int k = 0; k < 2; k++) {
              final int zzz = zz - k;
              for (int j = 0; j < 2; j++) {
                final int yyy = yy - j;
                for (int i = 0; i < 2; i++) {
                  final int xxx = xx - i;
                  if (i + j + k == 0) {
                    continue;
                  }

                  final CustomTricubicFunction previous = f1.getSplineNodeReference(xxx, yyy, zzz);

                  next.value(0, 0, 0, df_daA, d2f_da2A);
                  previous.value(i, j, k, df_daB, d2f_da2B);

                  for (int c = 0; c < 3; c++) {
                    // The function may change direction so check the 2nd derivative magnitude is
                    // similar
                    // TestLog.debug(logger,"[%d] %f vs %f", c, d2f_da2A[c], d2f_da2B[c],
                    // DoubleEquality.relativeError(d2f_da2A[c], d2f_da2B[c]));
                    d2f_da2A[c] = Math.abs(d2f_da2A[c]);
                    d2f_da2B[c] = Math.abs(d2f_da2B[c]);
                    value[c].add(DoubleEquality.relativeError(d2f_da2A[c], d2f_da2B[c]));
                  }
                }
              }
            }
          }
        }
      }

      boolean same = true;
      for (int c = 0; c < 3; c++) {
        // The second gradients are so different that this should fail
        same = same && value[c].getMean() < 0.01;
      }
      // TestLog.debug(logger,"d2yda2[%d] Error = %f +/- %f", c, value[c].getMean(),
      // value[c].getStandardDeviation());
      Assertions.assertFalse(same);
    }
  }

  @SeededTest
  void searchSplineImprovesFunctionValue(RandomSeed seed) {
    final UniformRandomProvider r = RngFactory.create(seed.get());
    // Bigger depth of field to capture astigmatism centre
    final int x = 10;
    final int y = 10;
    final int z = 10;
    final double[] xval = SimpleArrayUtils.newArray(x, 0, 1.0);
    final double[] yval = SimpleArrayUtils.newArray(y, 0, 1.0);
    final double[] zval = SimpleArrayUtils.newArray(z, 0, 1.0);
    for (int ii = 0; ii < 3; ii++) {
      final double cx = (x - 1) / 2.0 + r.nextDouble() / 2;
      final double cy = (y - 1) / 2.0 + r.nextDouble() / 2;
      final double cz = (z - 1) / 2.0 + r.nextDouble() / 2;
      final double[][][] fval = createData(x, y, z, cx, cy, cz, 2);

      final CustomTricubicInterpolator interpolator = new CustomTricubicInterpolator();
      final CustomTricubicInterpolatingFunction f1 =
          interpolator.interpolate(xval, yval, zval, fval);

      // Check the search approaches the actual function value
      double[] last = null;
      for (int i = 0; i <= 10; i++) {
        final double[] optimum = f1.search(true, i, 0, 0);
        // double d = Maths.distance(cx, cy, cz, optimum[0], optimum[1], optimum[2]);
        // TestLog.debug(logger,"[%d] %f,%f,%f %d = %s : dist = %f : error = %f", ii,
        // cx, cy, cz, i,
        // Arrays.toString(optimum), d, DoubleEquality.relativeError(amplitude,
        // optimum[3]));

        // Skip 0 to 1 as it moves from an exact node value to interpolation
        // which may use a different node depending on the gradient
        if (i > 1) {
          @SuppressWarnings("null")
          final double d =
              MathUtils.distance(last[0], last[1], last[2], optimum[0], optimum[1], optimum[2]);
          logger.log(TestLevel.TEST_DEBUG,
              FormatSupplier.getSupplier("[%d] %f,%f,%f %d = %s : dist = %f : change = %g", ii, cx,
                  cy, cz, i, Arrays.toString(optimum), d,
                  DoubleEquality.relativeError(last[3], optimum[3])));
          Assertions.assertTrue(optimum[3] >= last[3]);
        }
        last = optimum;
      }
    }
  }

  @SeededTest
  void canFindOptimum(RandomSeed seed) {
    final UniformRandomProvider r = RngFactory.create(seed.get());
    // Bigger depth of field to capture astigmatism centre
    final int x = 10;
    final int y = 10;
    final int z = 10;
    final double[] xval = SimpleArrayUtils.newArray(x, 0, 1.0);
    final double[] yval = SimpleArrayUtils.newArray(y, 0, 1.0);
    final double[] zval = SimpleArrayUtils.newArray(z, 0, 1.0);
    final DoubleDoubleBiPredicate equality = Predicates.doublesAreClose(5e-2, 0);
    for (int ii = 0; ii < 10; ii++) {
      final double cx = (x - 1) / 2.0 + r.nextDouble() / 2;
      final double cy = (y - 1) / 2.0 + r.nextDouble() / 2;
      final double cz = (z - 1) / 2.0 + r.nextDouble() / 2;
      final double[][][] fval = createData(x, y, z, cx, cy, cz, 2);

      // Test max and min search
      final boolean maximum = (ii % 2 == 1);
      if (!maximum) {
        // Invert
        for (int xx = 0; xx < x; xx++) {
          for (int yy = 0; yy < y; yy++) {
            for (int zz = 0; zz < z; zz++) {
              fval[xx][yy][zz] = -fval[xx][yy][zz];
            }
          }
        }
        amplitude = -amplitude;
      }

      final CustomTricubicInterpolator interpolator = new CustomTricubicInterpolator();
      final CustomTricubicInterpolatingFunction f1 =
          interpolator.interpolate(xval, yval, zval, fval);

      final double[] last = f1.search(maximum, 10, 1e-6, 0);

      // Since the cubic function is not the same as the input we cannot be too
      // precise here
      TestAssertions.assertTest(cx, last[0], equality);
      TestAssertions.assertTest(cy, last[1], equality);
      TestAssertions.assertTest(cz, last[2], equality);
      TestAssertions.assertTest(amplitude, last[3], equality);
    }
  }

  @Test
  void testBuilder() {
    final int x = 6;
    final int y = 5;
    final int z = 4;
    // Make it easy to have exact matching
    final double xscale = 1.0;
    final double yscale = 1.0;
    final double zscale = 1.0;
    final double[] xval = SimpleArrayUtils.newArray(x, 0, xscale);
    final double[] yval = SimpleArrayUtils.newArray(y, 0, yscale);
    final double[] zval = SimpleArrayUtils.newArray(z, 0, zscale);
    final double[][][] fval = createData(x, y, z, null);

    final CustomTricubicInterpolator interpolator = new CustomTricubicInterpolator();
    boolean singlePrecision = !interpolator.isSinglePrecision();
    long taskSize = interpolator.getTaskSize();

    interpolator.setTaskSize(taskSize - 1);
    Assertions.assertEquals(taskSize - 1, interpolator.getTaskSize());
    interpolator.setSinglePrecision(singlePrecision);
    Assertions.assertEquals(singlePrecision, interpolator.isSinglePrecision());

    singlePrecision = true;
    taskSize = 10;

    final CustomTricubicInterpolatingFunction f1 = interpolator.interpolate(xval, yval, zval, fval);

    final ExecutorService es = Executors.newFixedThreadPool(2);

    //@formatter:off
    final CustomTricubicInterpolatingFunction f2 = new CustomTricubicInterpolator.Builder()
        .setXValue(xval)
        .setYValue(yval)
        .setZValue(zval)
        .setFValue(fval)
        .setSinglePrecision(singlePrecision)
        .setProgress(NullTrackProgress.getInstance())
        .setTaskSize(taskSize)
        .setExecutorService(es)
        .interpolate();
    //@formatter:on

    final StandardTrivalueProcedure p1 = new StandardTrivalueProcedure();
    f1.sample(2, p1);
    final StandardTrivalueProcedure p2 = new StandardTrivalueProcedure();
    f2.sample(2, p2);

    Assertions.assertArrayEquals(p1.getXAxis(), p2.getXAxis());
    Assertions.assertArrayEquals(p1.getYAxis(), p2.getYAxis());
    Assertions.assertArrayEquals(p1.getZAxis(), p2.getZAxis());
    Assertions.assertArrayEquals(p1.getValue(), p2.getValue());

    //@formatter:off
    // With integer axis
    final CustomTricubicInterpolatingFunction f3 = new CustomTricubicInterpolator.Builder()
        .setFValue(fval)
        .setSinglePrecision(singlePrecision)
        // Executor but with a big task size so no multi-threading
        .setExecutorService(es)
        .setIntegerAxisValues(true)
        .interpolate();
    //@formatter:on

    f3.sample(2, p2);

    Assertions.assertArrayEquals(p1.getXAxis(), p2.getXAxis());
    Assertions.assertArrayEquals(p1.getYAxis(), p2.getYAxis());
    Assertions.assertArrayEquals(p1.getZAxis(), p2.getZAxis());
    Assertions.assertArrayEquals(p1.getValue(), p2.getValue());

    es.shutdown();
  }

  @Test
  void testCreateThrows() {
    final TrivalueProvider fp = new DoubleArrayTrivalueProvider(createData(4, 4, 4, null));
    Assertions.assertNotNull(CustomTricubicInterpolator.create(fp));

    final TrivalueProvider fp544 = new DoubleArrayTrivalueProvider(createData(5, 4, 4, null));
    final TrivalueProvider fp454 = new DoubleArrayTrivalueProvider(createData(4, 5, 4, null));
    final TrivalueProvider fp445 = new DoubleArrayTrivalueProvider(createData(4, 4, 5, null));
    Assertions.assertThrows(DimensionMismatchException.class, () -> {
      CustomTricubicInterpolator.create(fp544);
    });
    Assertions.assertThrows(DimensionMismatchException.class, () -> {
      CustomTricubicInterpolator.create(fp454);
    });
    Assertions.assertThrows(DimensionMismatchException.class, () -> {
      CustomTricubicInterpolator.create(fp445);
    });

    // With axis values
    final ValueProvider x4 = new DoubleArrayValueProvider(SimpleArrayUtils.newArray(4, 0, 1.0));
    final ValueProvider x5 = new DoubleArrayValueProvider(SimpleArrayUtils.newArray(5, 0, 1.0));
    Assertions.assertNotNull(CustomTricubicInterpolator.create(x4, x4, x4, fp));
    Assertions.assertThrows(DimensionMismatchException.class, () -> {
      CustomTricubicInterpolator.create(x5, x4, x4, fp);
    });
    Assertions.assertThrows(DimensionMismatchException.class, () -> {
      CustomTricubicInterpolator.create(x4, x5, x4, fp);
    });
    Assertions.assertThrows(DimensionMismatchException.class, () -> {
      CustomTricubicInterpolator.create(x4, x4, x5, fp);
    });
    Assertions.assertThrows(DimensionMismatchException.class, () -> {
      CustomTricubicInterpolator.create(x4, x4, x4, fp544);
    });
    Assertions.assertThrows(DimensionMismatchException.class, () -> {
      CustomTricubicInterpolator.create(x4, x4, x4, fp454);
    });
    Assertions.assertThrows(DimensionMismatchException.class, () -> {
      CustomTricubicInterpolator.create(x4, x4, x4, fp445);
    });

    Assertions.assertNotNull(CustomTricubicInterpolator.create(fp, 0, 0, 0));
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      CustomTricubicInterpolator.create(fp, -1, 0, 0);
    });
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      CustomTricubicInterpolator.create(fp, 0, -1, 0);
    });
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      CustomTricubicInterpolator.create(fp, 0, 0, -1);
    });
    Assertions.assertThrows(NumberIsTooSmallException.class, () -> {
      CustomTricubicInterpolator.create(fp, 3, 0, 0);
    });
    Assertions.assertThrows(NumberIsTooSmallException.class, () -> {
      CustomTricubicInterpolator.create(fp, 0, 3, 0);
    });
    Assertions.assertThrows(NumberIsTooSmallException.class, () -> {
      CustomTricubicInterpolator.create(fp, 0, 0, 3);
    });

    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      CustomTricubicInterpolator.create(x4, x4, x4, fp, -1, 0, 0);
    });
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      CustomTricubicInterpolator.create(x4, x4, x4, fp, 0, -1, 0);
    });
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      CustomTricubicInterpolator.create(x4, x4, x4, fp, 0, 0, -1);
    });
    Assertions.assertThrows(NumberIsTooSmallException.class, () -> {
      CustomTricubicInterpolator.create(x4, x4, x4, fp, 3, 0, 0);
    });
    Assertions.assertThrows(NumberIsTooSmallException.class, () -> {
      CustomTricubicInterpolator.create(x4, x4, x4, fp, 0, 3, 0);
    });
    Assertions.assertThrows(NumberIsTooSmallException.class, () -> {
      CustomTricubicInterpolator.create(x4, x4, x4, fp, 0, 0, 3);
    });
    Assertions.assertThrows(DimensionMismatchException.class, () -> {
      CustomTricubicInterpolator.create(x4, x4, x4, fp544, 0, 0, 0);
    });
    Assertions.assertThrows(DimensionMismatchException.class, () -> {
      CustomTricubicInterpolator.create(x4, x4, x4, fp454, 0, 0, 0);
    });
    Assertions.assertThrows(DimensionMismatchException.class, () -> {
      CustomTricubicInterpolator.create(x4, x4, x4, fp445, 0, 0, 0);
    });
  }

  @Test
  void testSampleThrows() {

    final double[][][] fval = createData(2, 2, 2, null);
    final DoubleArrayTrivalueProvider f = new DoubleArrayTrivalueProvider(fval);
    final CustomTricubicInterpolator interpolator = new CustomTricubicInterpolator();
    final StandardTrivalueProcedure p = new StandardTrivalueProcedure();
    final int samples = 2;
    interpolator.sample(f, samples, p);

    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      interpolator.sample(f, 1, p);
    });

    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      interpolator.sample(f, 0, samples, samples, p);
    });
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      interpolator.sample(f, samples, 0, samples, p);
    });
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      interpolator.sample(f, samples, samples, 0, p);
    });
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      interpolator.sample(f, 1, 1, 1, p);
    });

    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      interpolator.sample(new DoubleArrayTrivalueProvider(createData(1, 2, 2, null)), 2, p);
    });
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      interpolator.sample(new DoubleArrayTrivalueProvider(createData(2, 1, 2, null)), 2, p);
    });
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      interpolator.sample(new DoubleArrayTrivalueProvider(createData(2, 2, 1, null)), 2, p);
    });

    // Used for early exit
    final TrivalueProcedure p2 = new TrivalueProcedure() {
      @Override
      public boolean setDimensions(int maxx, int maxy, int maxz) {
        // Stop sampling
        return false;
      }

      @Override
      public void setX(int index0, double value) {
        // Do nothing
      }

      @Override
      public void setY(int index1, double value) {
        // Do nothing
      }

      @Override
      public void setZ(int index2, double value) {
        // Do nothing
      }

      @Override
      public void setValue(int index0, int index1, int index2, double value) {
        // Do nothing
      }
    };
    // These are all OK
    interpolator.sample(f, 2, 1, 1, p2);
    interpolator.sample(f, 2, 2, 1, p2);
    interpolator.sample(f, 2, 1, 2, p2);
    interpolator.sample(f, 1, 2, 1, p2);
    interpolator.sample(f, 1, 1, 2, p2);
    interpolator.sample(f, 1, 2, 2, p2);
  }

  @Test
  void testInterpolateThrows() {

    final TrivalueProvider fp = new DoubleArrayTrivalueProvider(createData(2, 2, 2, null));
    final ValueProvider x1 = new DoubleArrayValueProvider(SimpleArrayUtils.newArray(1, 0, 1.0));
    final ValueProvider x2 = new DoubleArrayValueProvider(SimpleArrayUtils.newArray(2, 0, 1.0));
    final ValueProvider x3 = new DoubleArrayValueProvider(SimpleArrayUtils.newArray(3, 0, 1.0));
    final CustomTricubicInterpolator interpolator = new CustomTricubicInterpolator();
    Assertions.assertNotNull(interpolator.interpolate(x2, x2, x2, fp));
    Assertions.assertThrows(NumberIsTooSmallException.class, () -> {
      interpolator.interpolate(x1, x2, x2, fp);
    });
    Assertions.assertThrows(NumberIsTooSmallException.class, () -> {
      interpolator.interpolate(x2, x1, x2, fp);
    });
    Assertions.assertThrows(NumberIsTooSmallException.class, () -> {
      interpolator.interpolate(x2, x2, x1, fp);
    });
    Assertions.assertThrows(DimensionMismatchException.class, () -> {
      interpolator.interpolate(x3, x2, x2, fp);
    });
    Assertions.assertThrows(DimensionMismatchException.class, () -> {
      interpolator.interpolate(x2, x3, x2, fp);
    });
    Assertions.assertThrows(DimensionMismatchException.class, () -> {
      interpolator.interpolate(x2, x2, x3, fp);
    });
  }

  @Test
  void testCanGetNumberOfTasks() {
    final long nNodes = Integer.MAX_VALUE;
    final long taskSize = 1;
    final long[] result = CustomTricubicInterpolator.getTaskSizeAndNumberOfTasks(nNodes, taskSize);
    final long taskSize2 = result[0];
    Assertions.assertTrue(taskSize < taskSize2);
    final long nTasks = result[1];
    Assertions.assertTrue(nTasks < Integer.MAX_VALUE);
  }
}
