package uk.ac.sussex.gdsc.core.data.detection;

import uk.ac.sussex.gdsc.core.utils.SimpleArrayUtils;
import uk.ac.sussex.gdsc.test.junit5.RandomSeed;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.rng.RngUtils;
import uk.ac.sussex.gdsc.test.utils.BaseTimingTask;
import uk.ac.sussex.gdsc.test.utils.TestComplexity;
import uk.ac.sussex.gdsc.test.utils.TestLogUtils;
import uk.ac.sussex.gdsc.test.utils.TestSettings;
import uk.ac.sussex.gdsc.test.utils.TimingResult;
import uk.ac.sussex.gdsc.test.utils.TimingService;

import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings({"javadoc"})
public class DetectionGridTest {
  private static Logger logger;

  @BeforeAll
  public static void beforeAll() {
    logger = Logger.getLogger(DetectionGridTest.class.getName());
  }

  @AfterAll
  public static void afterAll() {
    logger = null;
  }

  @Test
  public void canDetectCollisionsUsingSimpleGrid() {
    final Rectangle[] bounds = new Rectangle[3];
    bounds[0] = new Rectangle(0, 0, 10, 10);
    bounds[1] = new Rectangle(0, 5, 10, 5);
    bounds[2] = new Rectangle(5, 5, 5, 5);
    final SimpleDetectionGrid g = new SimpleDetectionGrid(bounds);
    Assertions.assertArrayEquals(new int[] {0}, g.find(0, 0));
    Assertions.assertArrayEquals(new int[] {0, 1, 2}, g.find(5, 5));
    Assertions.assertArrayEquals(new int[0], g.find(-5, 5));

    // Definition of insideness
    Assertions.assertArrayEquals(new int[0], g.find(10, 10));
    g.setIncludeOuterEdge(true);
    Assertions.assertArrayEquals(new int[] {0, 1, 2}, g.find(10, 10));
  }

  @Test
  public void canFindIndicesUsingBinaryTreeGrid() {
    final double[] data = SimpleArrayUtils.newArray(10, 0, 1.0);
    int i1;
    int i2;
    for (int i = 0; i < data.length; i++) {
      i1 = BinarySearchDetectionGrid.findIndexUpToAndIncluding(data, data[i]);
      Assertions.assertEquals(i, i1);
      i1 = BinarySearchDetectionGrid.findIndexUpToAndIncluding(data, data[i] + 0.1);
      Assertions.assertEquals(i, i1);
      i1 = BinarySearchDetectionGrid.findIndexUpToAndIncluding(data, data[i] - 0.1);
      Assertions.assertEquals(i - 1, i1);

      i2 = BinarySearchDetectionGrid.findIndexIncludingAndAfter(data, data[i]);
      Assertions.assertEquals(i, i2);
      i2 = BinarySearchDetectionGrid.findIndexIncludingAndAfter(data, data[i] - 0.1);
      Assertions.assertEquals(i, i2);
      i2 = BinarySearchDetectionGrid.findIndexIncludingAndAfter(data, data[i] + 0.1);
      Assertions.assertEquals(i + 1, i2);

      i2 = BinarySearchDetectionGrid.findIndexAfter(data, data[i]);
      Assertions.assertEquals(i + 1, i2);
      i2 = BinarySearchDetectionGrid.findIndexAfter(data, data[i] - 0.1);
      Assertions.assertEquals(i, i2);
      i2 = BinarySearchDetectionGrid.findIndexAfter(data, data[i] + 0.1);
      Assertions.assertEquals(i + 1, i2);
    }

    // Handle identity by testing with duplicates
    for (int i = 0; i < data.length; i++) {
      data[i] = i / 2;
    }

    for (int i = 0; i < data.length; i++) {
      i1 = BinarySearchDetectionGrid.findIndexUpToAndIncluding(data, data[i]);
      Assertions.assertEquals(i + (i + 1) % 2, i1);
      i1 = BinarySearchDetectionGrid.findIndexUpToAndIncluding(data, data[i] + 0.1);
      Assertions.assertEquals(i + (i + 1) % 2, i1);
      i1 = BinarySearchDetectionGrid.findIndexUpToAndIncluding(data, data[i] - 0.1);
      Assertions.assertEquals(i - i % 2 - 1, i1);

      i2 = BinarySearchDetectionGrid.findIndexIncludingAndAfter(data, data[i]);
      Assertions.assertEquals(i - i % 2, i2);
      i2 = BinarySearchDetectionGrid.findIndexIncludingAndAfter(data, data[i] - 0.1);
      Assertions.assertEquals(i - i % 2, i2);
      i2 = BinarySearchDetectionGrid.findIndexIncludingAndAfter(data, data[i] + 0.1);
      Assertions.assertEquals(i - i % 2 + 2, i2);

      i2 = BinarySearchDetectionGrid.findIndexAfter(data, data[i]);
      Assertions.assertEquals(i - i % 2 + 2, i2);
      i2 = BinarySearchDetectionGrid.findIndexAfter(data, data[i] - 0.1);
      Assertions.assertEquals(i - i % 2, i2);
      i2 = BinarySearchDetectionGrid.findIndexAfter(data, data[i] + 0.1);
      Assertions.assertEquals(i - i % 2 + 2, i2);
    }
  }

  @Test
  public void canDetectCollisionsUsingBinaryTreeGrid() {
    final Rectangle[] r = new Rectangle[3];
    r[0] = new Rectangle(0, 0, 10, 10);
    r[1] = new Rectangle(0, 5, 10, 5);
    r[2] = new Rectangle(5, 5, 5, 5);
    final BinarySearchDetectionGrid g = new BinarySearchDetectionGrid(r);
    Assertions.assertArrayEquals(new int[] {0}, g.find(0, 0));
    Assertions.assertArrayEquals(new int[] {0, 1, 2}, g.find(5, 5));
    Assertions.assertArrayEquals(new int[0], g.find(-5, 5));

    // Respect the insideness definition
    Assertions.assertArrayEquals(new int[0], g.find(10, 10));
  }

  @SeededTest
  public void canDetectTheSameCollisions(RandomSeed seed) {
    final int size = 512;
    final UniformRandomProvider rdg = RngUtils.create(seed.getSeedAsLong());
    final Rectangle2D[] r = generateRectangles(rdg, 1000, size);

    final SimpleDetectionGrid g1 = new SimpleDetectionGrid(r);
    final BinarySearchDetectionGrid g2 = new BinarySearchDetectionGrid(r);

    final double[][] points = generatePoints(rdg, 500, size);

    for (final double[] p : points) {
      final int[] e = g1.find(p[0], p[1]);
      final int[] o = g2.find(p[0], p[1]);
      Arrays.sort(e);
      Arrays.sort(o);
      // TestLog.debugln(logger,Arrays.toString(e));
      // TestLog.debugln(logger,Arrays.toString(o));
      Assertions.assertArrayEquals(e, o);
    }
  }

  private static Rectangle2D[] generateRectangles(UniformRandomProvider rdg, int n, int size) {
    final Rectangle2D[] r = new Rectangle2D[n];
    final double[][] p1 = generatePoints(rdg, n, size);
    final double[][] p2 = generatePoints(rdg, n, size);
    for (int i = 0; i < r.length; i++) {
      double x1 = p1[i][0];
      double x2 = p1[i][1];
      double y1 = p2[i][0];
      double y2 = p2[i][1];
      if (x2 < x1) {
        final double tmp = x2;
        x2 = x1;
        x1 = tmp;
      }
      if (y2 < y1) {
        final double tmp = y2;
        y2 = y1;
        y1 = tmp;
      }
      r[i] = new Rectangle2D.Double(x1, y1, x2 - x1, y2 - y1);
    }
    return r;
  }

  private static Rectangle2D[] generateSmallRectangles(UniformRandomProvider rdg, int n, int size,
      int width) {
    final Rectangle2D[] r = new Rectangle2D[n];
    final double[][] p1 = generatePoints(rdg, n, size);
    for (int i = 0; i < r.length; i++) {
      final double x1 = p1[i][0];
      final double y1 = p1[i][1];
      final double w = 1 + rdg.nextInt(width - 1);
      final double h = 1 + rdg.nextInt(width - 1);
      r[i] = new Rectangle2D.Double(x1, y1, w, h);
    }
    return r;
  }

  private static double[][] generatePoints(UniformRandomProvider rdg, int n, int size) {
    final double[][] x = new double[n][];
    while (n-- > 0) {
      x[n] = new double[] {rdg.nextInt(size), rdg.nextInt(size)};
    }
    return x;
  }

  private class MyTimingtask extends BaseTimingTask {
    DetectionGrid grid;
    double[][] points;

    public MyTimingtask(DetectionGrid grid, double[][] points) {
      super(grid.getClass().getSimpleName() + grid.size());
      this.grid = grid;
      this.points = points;
    }

    @Override
    public int getSize() {
      return 1;
    }

    @Override
    public Object getData(int index) {
      return points;
    }

    @Override
    public Object run(Object data) {
      final double[][] points = (double[][]) data;
      for (final double[] p : points) {
        grid.find(p[0], p[1]);
      }
      return null;
    }
  }

  @SeededTest
  public void binaryTreeIsFasterWithBigRectangles(RandomSeed seed) {
    final int size = 512;
    final int width = 200;
    final int n = 10000;
    final int np = 500;
    speedTest(seed, size, width, n, np);
  }

  @SeededTest
  public void binaryTreeIsFasterWithSmallRectangles(RandomSeed seed) {
    final int size = 512;
    final int width = 10;
    final int n = 10000;
    final int np = 500;
    speedTest(seed, size, width, n, np);
  }

  private void speedTest(RandomSeed seed, int size, int width, int n, int np) {
    Assumptions.assumeTrue(logger.isLoggable(Level.INFO));
    Assumptions.assumeTrue(TestSettings.allow(TestComplexity.MEDIUM));

    final UniformRandomProvider rdg = RngUtils.create(seed.getSeedAsLong());

    final TimingService ts = new TimingService();
    while (n > 500) {
      final Rectangle2D[] r = generateSmallRectangles(rdg, n, size, width);

      final SimpleDetectionGrid g1 = new SimpleDetectionGrid(r);
      final BinarySearchDetectionGrid g2 = new BinarySearchDetectionGrid(r);

      final double[][] points = generatePoints(rdg, np, size);
      ts.execute(new MyTimingtask(g1, points));
      ts.execute(new MyTimingtask(g2, points));
      n /= 2;
    }
    int resultsSize = ts.getSize();
    ts.repeat();
    logger.info(ts.getReport());
    for (int i1 = -1, i2 = -2; resultsSize > 0; resultsSize -= 2, i1 -= 2, i2 -= 2) {
      final TimingResult fast = ts.get(i1);
      final TimingResult slow = ts.get(i2);
      logger.log(TestLogUtils.getTimingRecord(slow, fast));
    }
  }
}
