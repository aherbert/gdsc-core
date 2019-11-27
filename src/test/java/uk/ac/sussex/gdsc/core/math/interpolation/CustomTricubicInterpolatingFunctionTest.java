package uk.ac.sussex.gdsc.core.math.interpolation;

import uk.ac.sussex.gdsc.core.data.DoubleArrayTrivalueProvider;
import uk.ac.sussex.gdsc.core.data.DoubleArrayValueProvider;
import uk.ac.sussex.gdsc.core.data.TrivalueProvider;
import uk.ac.sussex.gdsc.core.data.ValueProvider;
import uk.ac.sussex.gdsc.core.logging.TrackProgress;
import uk.ac.sussex.gdsc.core.math.interpolation.CustomTricubicInterpolatingFunction.Size;
import uk.ac.sussex.gdsc.test.api.TestAssertions;
import uk.ac.sussex.gdsc.test.api.TestHelper;
import uk.ac.sussex.gdsc.test.api.function.DoubleDoubleBiPredicate;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.NoDataException;
import org.apache.commons.math3.exception.NonMonotonicSequenceException;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.simple.RandomSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;

/**
 * This class is used to in-line the computation for the
 * {@link CustomTricubicInterpolatingFunction}.
 */
@SuppressWarnings({"javadoc"})
public class CustomTricubicInterpolatingFunctionTest {

  @Test
  public void testSizeConstructor() {
    final int maxx = 2;
    final int maxy = 2;
    final int maxz = 2;
    Assertions.assertNotNull(new Size(maxx, maxy, maxz));

    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      @SuppressWarnings("unused")
      final Size p = new Size(maxx - 1, maxy, maxz);
    });
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      @SuppressWarnings("unused")
      final Size p = new Size(maxx, maxy - 1, maxz);
    });
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      @SuppressWarnings("unused")
      final Size p = new Size(maxx, maxy, maxz - 1);
    });
  }

  @Test
  public void testProperties() {
    final int[] data = new int[] {2, 3, 4};
    for (int i = 0; i < 3; i++) {
      final int maxx = data[(0 + i) % 3];
      final int maxy = data[(1 + i) % 3];
      final int maxz = data[(2 + i) % 3];
      final Size p = new Size(maxx, maxy, maxz);
      Assertions.assertEquals(maxx, p.getFunctionPoints(0));
      Assertions.assertEquals(maxy, p.getFunctionPoints(1));
      Assertions.assertEquals(maxz, p.getFunctionPoints(2));
      Assertions.assertEquals(maxx - 1, p.getSplinePoints(0));
      Assertions.assertEquals(maxy - 1, p.getSplinePoints(1));
      Assertions.assertEquals(maxz - 1, p.getSplinePoints(2));
      Assertions.assertEquals(maxx * maxy * maxz, p.getTotalFunctionPoints());
      Assertions.assertEquals((maxx - 1) * (maxy - 1) * (maxz - 1), p.getTotalSplinePoints());

      for (final boolean singlePrecision : new boolean[] {true, false}) {
        // Single precision float[64] arrays for all spline points
        long expected = p.getTotalSplinePoints() * 64 * 4;
        if (!singlePrecision) {
          // Double precision
          expected *= 2;
        }
        // The function store XYZ values in double[]
        expected += (maxx + maxy + maxz) * 8;
        // The function store scales in double[] for each spline point
        expected += (maxx + maxy + maxz - 3) * 8;
        Assertions.assertEquals(expected, p.getMemoryFootprint(singlePrecision));
      }

      for (int n = 2; n <= 4; n++) {
        final Size p2 = p.enlarge(2);
        Assertions.assertEquals((maxx - 1) * 2 + 1, p2.getFunctionPoints(0));
        Assertions.assertEquals((maxy - 1) * 2 + 1, p2.getFunctionPoints(1));
        Assertions.assertEquals((maxz - 1) * 2 + 1, p2.getFunctionPoints(2));
      }
    }
  }

  @Test
  public void testComputeCoefficients() {
    final UniformRandomProvider rng = RandomSource.create(RandomSource.SPLIT_MIX_64);
    final double[] beta = new double[64];
    final DoubleDoubleBiPredicate equality = TestHelper.doublesAreClose(1e-10, 0);
    for (int i = 0; i < 10; i++) {
      for (int j = 0; j < beta.length; j++) {
        beta[j] = rng.nextDouble();
      }
      final double[] e = CustomTricubicInterpolatingFunction.computeCoefficients(beta);
      final double[] o = CustomTricubicInterpolatingFunction.computeCoefficientsInline(beta);
      // Exactly the same
      Assertions.assertArrayEquals(e, o);
      final double[] o2 =
          CustomTricubicInterpolatingFunction.computeCoefficientsInlineCollectTerms(beta);
      // Almost the same
      TestAssertions.assertArrayTest(e, o2, equality);
    }
  }

  @Test
  public void testCreate() {
    final UniformRandomProvider rng = RandomSource.create(RandomSource.SPLIT_MIX_64);
    // This is a test that checks the create method works with partial derivatives.
    // It is a reverse of the creation with the beta table directly.
    final double[] beta = new double[64];
    final double[][][] _f = new double[2][2][2];
    final double[][][] _dFdX = new double[2][2][2];
    final double[][][] _dFdY = new double[2][2][2];
    final double[][][] _dFdZ = new double[2][2][2];
    final double[][][] _d2FdXdY = new double[2][2][2];
    final double[][][] _d2FdXdZ = new double[2][2][2];
    final double[][][] _d2FdYdZ = new double[2][2][2];
    final double[][][] _d3FdXdYdZ = new double[2][2][2];
    final TrivalueProvider f = new DoubleArrayTrivalueProvider(_f);
    final TrivalueProvider dFdX = new DoubleArrayTrivalueProvider(_dFdX);
    final TrivalueProvider dFdY = new DoubleArrayTrivalueProvider(_dFdY);
    final TrivalueProvider dFdZ = new DoubleArrayTrivalueProvider(_dFdZ);
    final TrivalueProvider d2FdXdY = new DoubleArrayTrivalueProvider(_d2FdXdY);
    final TrivalueProvider d2FdXdZ = new DoubleArrayTrivalueProvider(_d2FdXdZ);
    final TrivalueProvider d2FdYdZ = new DoubleArrayTrivalueProvider(_d2FdYdZ);
    final TrivalueProvider d3FdXdYdZ = new DoubleArrayTrivalueProvider(_d3FdXdYdZ);
    final double xscale = 2;
    final double yscale = 3;
    final double zscale = 4;
    for (int i = 0; i < 3; i++) {
      for (int z = 0, j = 0; z < 2; z++) {
        for (int y = 0; y < 2; y++) {
          for (int x = 0; x < 2; x++, j++) {
            _f[x][y][z] = beta[j] = rng.nextDouble();
            _dFdX[x][y][z] = beta[j + 8] = rng.nextDouble();
            _dFdY[x][y][z] = beta[j + 16] = rng.nextDouble();
            _dFdZ[x][y][z] = beta[j + 24] = rng.nextDouble();
            _d2FdXdY[x][y][z] = beta[j + 32] = rng.nextDouble();
            _d2FdXdZ[x][y][z] = beta[j + 40] = rng.nextDouble();
            _d2FdYdZ[x][y][z] = beta[j + 48] = rng.nextDouble();
            _d3FdXdYdZ[x][y][z] = beta[j + 56] = rng.nextDouble();
          }
        }
      }
      final CustomTricubicFunction fun1 = CustomTricubicInterpolatingFunction.create(f, dFdX, dFdY,
          dFdZ, d2FdXdY, d2FdXdZ, d2FdYdZ, d3FdXdYdZ);
      final double[] e =
          CustomTricubicInterpolatingFunction.computeCoefficientsInlineCollectTerms(beta);
      final double[] obs = new double[64];
      fun1.getCoefficients(obs);
      Assertions.assertArrayEquals(e, obs);

      // Check with scale
      for (int z = 0; z < 2; z++) {
        for (int y = 0; y < 2; y++) {
          for (int x = 0; x < 2; x++) {
            _dFdX[x][y][z] /= xscale;
            _dFdY[x][y][z] /= yscale;
            _dFdZ[x][y][z] /= zscale;
            _d2FdXdY[x][y][z] /= (xscale * yscale);
            _d2FdXdZ[x][y][z] /= (xscale * zscale);
            _d2FdYdZ[x][y][z] /= (yscale * zscale);
            _d3FdXdYdZ[x][y][z] /= (xscale * yscale * zscale);
          }
        }
      }

      final CustomTricubicFunction fun2 = CustomTricubicInterpolatingFunction.create(xscale, yscale,
          zscale, f, dFdX, dFdY, dFdZ, d2FdXdY, d2FdXdZ, d2FdYdZ, d3FdXdYdZ);
      fun2.getCoefficients(obs);
      Assertions.assertArrayEquals(e, obs, 1e-12);
    }
  }

  @Test
  public void testConstructorThrows() {
    final double[] _x = new double[] {0, 1};
    final ValueProvider x = new DoubleArrayValueProvider(_x);
    final ValueProvider y = new DoubleArrayValueProvider(_x);
    final ValueProvider z = new DoubleArrayValueProvider(_x);
    final TrivalueProvider f = new DoubleArrayTrivalueProvider(new double[2][2][2]);
    final TrivalueProvider dFdX = new DoubleArrayTrivalueProvider(new double[2][2][2]);
    final TrivalueProvider dFdY = new DoubleArrayTrivalueProvider(new double[2][2][2]);
    final TrivalueProvider dFdZ = new DoubleArrayTrivalueProvider(new double[2][2][2]);
    final TrivalueProvider d2FdXdY = new DoubleArrayTrivalueProvider(new double[2][2][2]);
    final TrivalueProvider d2FdXdZ = new DoubleArrayTrivalueProvider(new double[2][2][2]);
    final TrivalueProvider d2FdYdZ = new DoubleArrayTrivalueProvider(new double[2][2][2]);
    final TrivalueProvider d3FdXdYdZ = new DoubleArrayTrivalueProvider(new double[2][2][2]);

    final TrackProgress progress = null;
    final ExecutorService executorService = null;
    final long taskSize = 0;
    final boolean singlePrecision = false;
    Assertions.assertNotNull(
        new CustomTricubicInterpolatingFunction(x, y, z, f, dFdX, dFdY, dFdZ, d2FdXdY, d2FdXdZ,
            d2FdYdZ, d3FdXdYdZ, progress, executorService, taskSize, singlePrecision));

    final ValueProvider x1 = new DoubleArrayValueProvider(new double[1]);
    final ValueProvider x3 = new DoubleArrayValueProvider(new double[] {0, 1, 2});
    final ValueProvider x_2 = new DoubleArrayValueProvider(new double[] {0, -1});

    Assertions.assertThrows(NoDataException.class, () -> {
      @SuppressWarnings("unused")
      final CustomTricubicInterpolatingFunction f1 =
          new CustomTricubicInterpolatingFunction(x1, y, z, f, dFdX, dFdY, dFdZ, d2FdXdY, d2FdXdZ,
              d2FdYdZ, d3FdXdYdZ, progress, executorService, taskSize, singlePrecision);
    });
    Assertions.assertThrows(NoDataException.class, () -> {
      @SuppressWarnings("unused")
      final CustomTricubicInterpolatingFunction f1 =
          new CustomTricubicInterpolatingFunction(x, x1, z, f, dFdX, dFdY, dFdZ, d2FdXdY, d2FdXdZ,
              d2FdYdZ, d3FdXdYdZ, progress, executorService, taskSize, singlePrecision);
    });
    Assertions.assertThrows(NoDataException.class, () -> {
      @SuppressWarnings("unused")
      final CustomTricubicInterpolatingFunction f1 =
          new CustomTricubicInterpolatingFunction(x, y, x1, f, dFdX, dFdY, dFdZ, d2FdXdY, d2FdXdZ,
              d2FdYdZ, d3FdXdYdZ, progress, executorService, taskSize, singlePrecision);
    });
    Assertions.assertThrows(DimensionMismatchException.class, () -> {
      @SuppressWarnings("unused")
      final CustomTricubicInterpolatingFunction f1 =
          new CustomTricubicInterpolatingFunction(x3, y, z, f, dFdX, dFdY, dFdZ, d2FdXdY, d2FdXdZ,
              d2FdYdZ, d3FdXdYdZ, progress, executorService, taskSize, singlePrecision);
    });
    Assertions.assertThrows(DimensionMismatchException.class, () -> {
      @SuppressWarnings("unused")
      final CustomTricubicInterpolatingFunction f1 =
          new CustomTricubicInterpolatingFunction(x, x3, z, f, dFdX, dFdY, dFdZ, d2FdXdY, d2FdXdZ,
              d2FdYdZ, d3FdXdYdZ, progress, executorService, taskSize, singlePrecision);
    });
    Assertions.assertThrows(DimensionMismatchException.class, () -> {
      @SuppressWarnings("unused")
      final CustomTricubicInterpolatingFunction f1 =
          new CustomTricubicInterpolatingFunction(x, y, x3, f, dFdX, dFdY, dFdZ, d2FdXdY, d2FdXdZ,
              d2FdYdZ, d3FdXdYdZ, progress, executorService, taskSize, singlePrecision);
    });
    Assertions.assertThrows(NonMonotonicSequenceException.class, () -> {
      @SuppressWarnings("unused")
      final CustomTricubicInterpolatingFunction f1 =
          new CustomTricubicInterpolatingFunction(x_2, y, z, f, dFdX, dFdY, dFdZ, d2FdXdY, d2FdXdZ,
              d2FdYdZ, d3FdXdYdZ, progress, executorService, taskSize, singlePrecision);
    });
    Assertions.assertThrows(NonMonotonicSequenceException.class, () -> {
      @SuppressWarnings("unused")
      final CustomTricubicInterpolatingFunction f1 =
          new CustomTricubicInterpolatingFunction(x, x_2, z, f, dFdX, dFdY, dFdZ, d2FdXdY, d2FdXdZ,
              d2FdYdZ, d3FdXdYdZ, progress, executorService, taskSize, singlePrecision);
    });
    Assertions.assertThrows(NonMonotonicSequenceException.class, () -> {
      @SuppressWarnings("unused")
      final CustomTricubicInterpolatingFunction f1 =
          new CustomTricubicInterpolatingFunction(x, y, x_2, f, dFdX, dFdY, dFdZ, d2FdXdY, d2FdXdZ,
              d2FdYdZ, d3FdXdYdZ, progress, executorService, taskSize, singlePrecision);
    });

    final CustomTricubicFunction[][][] splines = new CustomTricubicFunction[1][1][1];
    final double[] a = new double[64];
    splines[0][0][0] = createCustomTricubicFunction(a);

    Assertions.assertNotNull(new CustomTricubicInterpolatingFunction(_x, _x, _x, splines));

    Assertions.assertThrows(NoDataException.class, () -> {
      @SuppressWarnings("unused")
      final CustomTricubicInterpolatingFunction f1 =
          new CustomTricubicInterpolatingFunction(x1, y, z, splines);
    });
    Assertions.assertThrows(NoDataException.class, () -> {
      @SuppressWarnings("unused")
      final CustomTricubicInterpolatingFunction f1 =
          new CustomTricubicInterpolatingFunction(x, x1, z, splines);
    });
    Assertions.assertThrows(NoDataException.class, () -> {
      @SuppressWarnings("unused")
      final CustomTricubicInterpolatingFunction f1 =
          new CustomTricubicInterpolatingFunction(x, y, x1, splines);
    });
    Assertions.assertThrows(DimensionMismatchException.class, () -> {
      @SuppressWarnings("unused")
      final CustomTricubicInterpolatingFunction f1 =
          new CustomTricubicInterpolatingFunction(x3, y, z, splines);
    });
    Assertions.assertThrows(DimensionMismatchException.class, () -> {
      @SuppressWarnings("unused")
      final CustomTricubicInterpolatingFunction f1 =
          new CustomTricubicInterpolatingFunction(x, x3, z, splines);
    });
    Assertions.assertThrows(DimensionMismatchException.class, () -> {
      @SuppressWarnings("unused")
      final CustomTricubicInterpolatingFunction f1 =
          new CustomTricubicInterpolatingFunction(x, y, x3, splines);
    });
    Assertions.assertThrows(NonMonotonicSequenceException.class, () -> {
      @SuppressWarnings("unused")
      final CustomTricubicInterpolatingFunction f1 =
          new CustomTricubicInterpolatingFunction(x_2, y, z, splines);
    });
    Assertions.assertThrows(NonMonotonicSequenceException.class, () -> {
      @SuppressWarnings("unused")
      final CustomTricubicInterpolatingFunction f1 =
          new CustomTricubicInterpolatingFunction(x, x_2, z, splines);
    });
    Assertions.assertThrows(NonMonotonicSequenceException.class, () -> {
      @SuppressWarnings("unused")
      final CustomTricubicInterpolatingFunction f1 =
          new CustomTricubicInterpolatingFunction(x, y, x_2, splines);
    });
  }

  @Test
  public void testIsUniformIsInteger() {
    final ValueProvider tt = new DoubleArrayValueProvider(new double[] {0.5, 1.5, 2.5});
    final ValueProvider tf = new DoubleArrayValueProvider(new double[] {0.5, 2, 3.5});
    final ValueProvider ff = new DoubleArrayValueProvider(new double[] {0.5, 1.8, 3.5});

    final CustomTricubicFunction[][][] splines = new CustomTricubicFunction[2][2][2];
    final double[] a = new double[64];
    for (int z = 0; z < 2; z++) {
      for (int y = 0; y < 2; y++) {
        for (int x = 0; x < 2; x++) {
          splines[x][y][z] = createCustomTricubicFunction(a);
        }
      }
    }

    testIsUniformIsInteger(new CustomTricubicInterpolatingFunction(tt, tt, tt, splines), true,
        true);
    testIsUniformIsInteger(new CustomTricubicInterpolatingFunction(tf, tt, tt, splines), true,
        false);
    testIsUniformIsInteger(new CustomTricubicInterpolatingFunction(tt, tf, tt, splines), true,
        false);
    testIsUniformIsInteger(new CustomTricubicInterpolatingFunction(tt, tt, tf, splines), true,
        false);
    testIsUniformIsInteger(new CustomTricubicInterpolatingFunction(ff, tt, tt, splines), false,
        false);
    testIsUniformIsInteger(new CustomTricubicInterpolatingFunction(tt, ff, tt, splines), false,
        false);
    testIsUniformIsInteger(new CustomTricubicInterpolatingFunction(tt, tt, ff, splines), false,
        false);

    final CustomTricubicInterpolatingFunction f =
        new CustomTricubicInterpolatingFunction(tt, tt, tt, splines);
    final double[] e = f.getScale();
    final double[] o = f.getScale(); // This is a clone
    Assertions.assertNotSame(e, o);
    Assertions.assertArrayEquals(e, o);
    Assertions.assertThrows(IllegalStateException.class, () -> {
      new CustomTricubicInterpolatingFunction(ff, tt, tt, splines).getScale();
    });
  }

  private static void testIsUniformIsInteger(CustomTricubicInterpolatingFunction func,
      boolean isUniform, boolean isInteger) {
    Assertions.assertEquals(isUniform, func.isUniform(), "Uniform");
    Assertions.assertEquals(isInteger, func.isInteger(), "Integer");
  }

  @Test
  public void testCanConvertPrecision() {
    final ValueProvider x = new DoubleArrayValueProvider(new double[] {0, 1});
    final CustomTricubicFunction[][][] splines = new CustomTricubicFunction[1][1][1];
    final double[] a = new double[64];
    splines[0][0][0] = createCustomTricubicFunction(a);
    final CustomTricubicInterpolatingFunction f =
        new CustomTricubicInterpolatingFunction(x, x, x, splines);
    Assertions.assertFalse(f.isSinglePrecision());
    Assertions.assertTrue(f.getSplineNode(0, 0, 0) instanceof DoubleCustomTricubicFunction);
    f.toSinglePrecision();
    Assertions.assertTrue(f.isSinglePrecision());
    Assertions.assertTrue(f.getSplineNode(0, 0, 0) instanceof FloatCustomTricubicFunction);
    f.toSinglePrecision();
    Assertions.assertTrue(f.isSinglePrecision());
    f.toDoublePrecision();
    Assertions.assertFalse(f.isSinglePrecision());
    Assertions.assertTrue(f.getSplineNode(0, 0, 0) instanceof DoubleCustomTricubicFunction);
    f.toDoublePrecision();
    Assertions.assertFalse(f.isSinglePrecision());
  }

  @Test
  public void testIsValidPoint() {
    final ValueProvider x = new DoubleArrayValueProvider(new double[] {0, 1});
    final CustomTricubicFunction[][][] splines = new CustomTricubicFunction[1][1][1];
    final double[] a = new double[64];
    splines[0][0][0] = createCustomTricubicFunction(a);
    final CustomTricubicInterpolatingFunction f =
        new CustomTricubicInterpolatingFunction(x, x, x, splines);
    Assertions.assertTrue(f.isValidPoint(0, 0, 0));
    Assertions.assertTrue(f.isValidPoint(1, 1, 1));
    final double under = -0.000001;
    final double over = 1.000001;
    Assertions.assertFalse(f.isValidPoint(under, 0, 0));
    Assertions.assertFalse(f.isValidPoint(0, under, 0));
    Assertions.assertFalse(f.isValidPoint(0, 0, under));
    Assertions.assertFalse(f.isValidPoint(over, 0, 0));
    Assertions.assertFalse(f.isValidPoint(0, over, 0));
    Assertions.assertFalse(f.isValidPoint(0, 0, over));
  }

  @Test
  public void testValueThrows() {
    final double[] a = new double[64];
    final CustomTricubicFunction[][][] splines = new CustomTricubicFunction[1][1][1];
    splines[0][0][0] = createCustomTricubicFunction(a);
    double scale = 1;
    for (int i = 0; i < 2; i++) {
      final ValueProvider x = new DoubleArrayValueProvider(new double[] {0, scale});
      final CustomTricubicInterpolatingFunction f =
          new CustomTricubicInterpolatingFunction(x, x, x, splines);
      f.value(0, 0, 0);
      f.value(scale, scale, scale);
      final double under = -0.000001;
      final double over = scale + 0.000001;
      Assertions.assertThrows(OutOfRangeException.class, () -> {
        f.value(under, 0, 0);
      });
      Assertions.assertThrows(OutOfRangeException.class, () -> {
        f.value(0, under, 0);
      });
      Assertions.assertThrows(OutOfRangeException.class, () -> {
        f.value(0, 0, under);
      });
      Assertions.assertThrows(OutOfRangeException.class, () -> {
        f.value(over, 0, 0);
      });
      Assertions.assertThrows(OutOfRangeException.class, () -> {
        f.value(0, over, 0);
      });
      Assertions.assertThrows(OutOfRangeException.class, () -> {
        f.value(0, 0, over);
      });
      scale *= 0.5;
    }
  }

  private static CustomTricubicFunction createCustomTricubicFunction(double[] coefficients) {
    return CustomTricubicFunctionUtils.create(coefficients);
  }
}
