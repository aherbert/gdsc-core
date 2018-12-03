package uk.ac.sussex.gdsc.core.utils;

import uk.ac.sussex.gdsc.test.junit5.RandomSeed;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.rng.RngUtils;
import uk.ac.sussex.gdsc.test.utils.functions.FunctionUtils;

import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings({"javadoc"})
public class ConvexHullTest {
  private static Logger logger;

  @BeforeAll
  public static void beforeAll() {
    logger = Logger.getLogger(ConvexHullTest.class.getName());
  }

  @AfterAll
  public static void afterAll() {
    logger = null;
  }

  @Test
  public void cannotComputeConvexHullFromNoCoords() {
    final float[] x = new float[] {};
    final float[] y = new float[] {};
    final ConvexHull hull = ConvexHull.create(x, y);
    Assertions.assertNull(hull);
  }

  @Test
  public void canComputeConvexHullFromSquare() {
    final float[] ex = new float[] {0, 10, 10, 0};
    final float[] ey = new float[] {0, 0, 10, 10};
    for (int i = 0; i < ex.length; i++) {
      final float[] x = new float[ex.length];
      final float[] y = new float[ey.length];
      for (int j = 0; j < ex.length; j++) {
        final int n = (i + j) % ex.length;
        x[j] = ex[n];
        y[j] = ey[n];
      }
      final ConvexHull hull = ConvexHull.create(x, y);
      check(ex, ey, hull);
    }
  }

  @Test
  public void canComputeConvexHullFromSquareWithInternalPoint() {
    final float[] x = new float[] {0, 0, 10, 10, 5};
    final float[] y = new float[] {0, 10, 10, 0, 5};
    final float[] ex = new float[] {0, 10, 10, 0};
    final float[] ey = new float[] {0, 0, 10, 10};
    final ConvexHull hull = ConvexHull.create(x, y);
    check(ex, ey, hull);
  }

  @Test
  public void canComputeConvexHullFromSquareWithInternalPoint2() {
    final float[] x = new float[] {0, 0, 5, 10, 10};
    final float[] y = new float[] {0, 10, 5, 10, 0};
    final float[] ex = new float[] {0, 10, 10, 0};
    final float[] ey = new float[] {0, 0, 10, 10};
    final ConvexHull hull = ConvexHull.create(x, y);
    check(ex, ey, hull);
  }

  private static void check(float[] ex, float[] ey, ConvexHull hull) {
    if (ex == null) {
      Assertions.assertTrue(hull == null);
      return;
    }
    final int n = ex.length;

    Assertions.assertEquals(n, hull.x.length);

    // for (int i = 0; i < ex.length; i++)
    // {
    // TestLog.info(logger,"[%d] %f==%f (%f), %f==%f (%f)", i, ex[i], hull.x[i],
    // hull.x[i] - ex[i], ey[i], hull.y[i], hull.y[i] - ey[i]);
    // }

    for (int i = 0; i < n; i++) {
      Assertions.assertEquals(ex[i], hull.x[i]);
      Assertions.assertEquals(ey[i], hull.y[i]);
    }

    final float ox = MathUtils.min(ex);
    final float oy = MathUtils.min(ey);
    final float maxx = MathUtils.max(ex);
    final float maxy = MathUtils.max(ey);

    final Rectangle bounds = hull.getBounds();
    Assertions.assertEquals(ox, bounds.getX(), "minx");
    Assertions.assertEquals(oy, bounds.getY(), "miny");
    Assertions.assertEquals(maxx, bounds.getMaxX(), "maxx");
    Assertions.assertEquals(maxy, bounds.getMaxY(), "maxy");

    final Rectangle bounds2 = hull.getBounds();
    Assertions.assertNotSame(bounds, bounds2, "Bounds should be a new object");
    Assertions.assertEquals(bounds, bounds2);
  }

  @SeededTest
  public void canComputeConvexHullFromOrigin00(RandomSeed seed) {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeedAsLong());
    for (final int size : new int[] {10}) {
      for (final float w : new float[] {10, 5}) {
        for (final float h : new float[] {10, 5}) {
          compute(rng, size, 0, 0, w, h);
        }
      }
    }
  }

  @SeededTest
  public void canComputeConvexHullFromOriginXy(RandomSeed seed) {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeedAsLong());
    for (final int size : new int[] {10}) {
      for (final float ox : new float[] {-5, 5}) {
        for (final float oy : new float[] {-5, 5}) {
          for (final float w : new float[] {10, 5}) {
            for (final float h : new float[] {10, 5}) {
              compute(rng, size, ox, oy, w, h);
            }
          }
        }
      }
    }
  }

  private static void compute(UniformRandomProvider rng, int size, float ox, float oy, float width,
      float height) {
    final float[][] data = createData(rng, size, ox, oy, width, height);
    final ConvexHull hull = ConvexHull.create(data[0], data[1]);

    // Simple check of the bounds
    try {
      Assertions.assertNotNull(hull);
      final Rectangle2D.Double bounds = hull.getFloatBounds();
      Assertions.assertTrue(ox <= bounds.getX(),
          () -> String.format("xmin %d <= %d", ox, bounds.getX()));
      Assertions.assertTrue(oy <= bounds.getY(),
          () -> String.format("ymin %d <= %d", oy, bounds.getY()));

      Assertions.assertTrue(ox + width >= bounds.getMaxX(),
          () -> String.format("xmax %d >= %d", ox + width, bounds.getMaxX()));
      Assertions.assertTrue(oy + height >= bounds.getMaxY(),
          () -> String.format("ymax %d >= %d", oy + height, bounds.getMaxY()));

      final Rectangle2D.Double bounds2 = hull.getFloatBounds();
      Assertions.assertNotSame(bounds, bounds2, "Bounds should be a new object");
      Assertions.assertEquals(bounds, bounds2);
    } catch (final AssertionError ex) {
      // Debug
      if (logger.isLoggable(Level.FINE)) {
        for (int i = 0; i < size; i++) {
          logger.fine(FunctionUtils.getSupplier("[%d] %f,%f", i, data[0][i], data[1][i]));
        }
        if (hull != null) {
          for (int i = 0; i < hull.x.length; i++) {
            logger.fine(FunctionUtils.getSupplier("H[%d] %f,%f", i, hull.x[i], hull.y[i]));
          }
        }
      }
      throw ex;
    }
  }

  private static float[][] createData(UniformRandomProvider rng, int size, float ox, float oy,
      float width, float height) {
    final float[][] data = new float[2][size];
    for (int i = 0; i < size; i++) {
      data[0][i] = ox + rng.nextFloat() * width;
      data[1][i] = oy + rng.nextFloat() * height;
    }
    return data;
  }

  @Test
  public void canCreateWithNoPoints() {
    final float[] x = new float[0];
    Assertions.assertNull(ConvexHull.create(x, x));
  }

  @Test
  public void canCreateWithOnePoint() {
    final float[] x = new float[] {1.2345f};
    final ConvexHull hull = ConvexHull.create(x, x);
    Assertions.assertEquals(1, hull.size());
    Assertions.assertTrue(hull.getLength() == 0);
    Assertions.assertTrue(hull.getArea() == 0);
  }

  @Test
  public void canCreateWithTwoPoints() {
    final float[] x = new float[] {1.5f, 2.5f};
    final ConvexHull hull = ConvexHull.create(x, x);
    Assertions.assertEquals(2, hull.size());
    Assertions.assertEquals(2 * Math.sqrt(2), hull.getLength(), 1e-10);
    Assertions.assertTrue(hull.getArea() == 0);
  }

  @Test
  public void canCreateWithThreePoints() {
    final float[] x = new float[] {1, 2, 2};
    final float[] y = new float[] {1, 1, 2};
    final ConvexHull hull = ConvexHull.create(x, y);
    Assertions.assertEquals(3, hull.size());
    Assertions.assertEquals(2 + Math.sqrt(2), hull.getLength(), 1e-10);
    Assertions.assertEquals(hull.getArea(), 0.5, 1e-10);
  }

  @Test
  public void canComputeLengthAndArea() {
    // Parallelogram
    float[] xvalues = new float[] {0, 10, 11, 1};
    float[] yvalues = new float[] {0, 0, 10, 10};
    ConvexHull hull = ConvexHull.create(xvalues, yvalues);
    Assertions.assertEquals(2 * 10 + 2 * Math.sqrt(1 * 1 + 10 * 10), hull.getLength(), 1e-6);
    Assertions.assertEquals(100, hull.getArea(), 1e-6);

    // Rotated square
    xvalues = new float[] {0, 10, 9, -1};
    yvalues = new float[] {0, 1, 11, 10};
    hull = ConvexHull.create(xvalues, yvalues);
    final double edgeLengthSquared = 1 * 1 + 10 * 10;
    Assertions.assertEquals(4 * Math.sqrt(edgeLengthSquared), hull.getLength(), 1e-6);
    Assertions.assertEquals(edgeLengthSquared, hull.getArea(), 1e-6);

    // Polygon circle
    final int n = 1000;
    final double radius = 4;
    xvalues = new float[n];
    yvalues = new float[n];
    for (int i = 0; i < 1000; i++) {
      final double a = i * 2 * Math.PI / n;
      xvalues[i] = (float) (Math.sin(a) * radius);
      yvalues[i] = (float) (Math.cos(a) * radius);
    }
    hull = ConvexHull.create(xvalues, yvalues);
    Assertions.assertEquals(2 * Math.PI * radius, hull.getLength(), 1e-2);
    Assertions.assertEquals(Math.PI * radius * radius, hull.getArea(), 1e-2);
  }

  @Test
  public void conComputeContains() {
    final float[] x = new float[] {0, 10, 11, 1};
    final float[] y = new float[] {0, 0, 10, 10};
    final ConvexHull hull = ConvexHull.create(x, y);
    // Contains does not match outer bounds on right or bottom
    Assertions.assertTrue(hull.contains(x[0], y[0]));
    for (int i = 1; i < x.length; i++) {
      Assertions.assertFalse(hull.contains(x[i], y[i]));
    }
    Assertions.assertTrue(hull.contains(5, 5));
    Assertions.assertFalse(hull.contains(-5, 5));
    Assertions.assertFalse(hull.contains(5, -5));
  }
}
