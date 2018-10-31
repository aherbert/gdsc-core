package uk.ac.sussex.gdsc.core.filters;

import uk.ac.sussex.gdsc.core.utils.Random;
import uk.ac.sussex.gdsc.core.utils.Statistics;
import uk.ac.sussex.gdsc.test.api.TestAssertions;
import uk.ac.sussex.gdsc.test.api.TestHelper;
import uk.ac.sussex.gdsc.test.junit5.RandomSeed;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.junit5.SpeedTag;
import uk.ac.sussex.gdsc.test.rng.RngUtils;
import uk.ac.sussex.gdsc.test.utils.BaseTimingTask;
import uk.ac.sussex.gdsc.test.utils.TestComplexity;
import uk.ac.sussex.gdsc.test.utils.TestLog;
import uk.ac.sussex.gdsc.test.utils.TestSettings;
import uk.ac.sussex.gdsc.test.utils.TimingService;

import ij.process.FloatProcessor;
import ij.process.ImageStatistics;

import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.awt.Rectangle;
import java.util.logging.Logger;

@SuppressWarnings({"javadoc"})
public class AreaSumTest {
  private static Logger logger;

  @BeforeAll
  public static void beforeAll() {
    logger = Logger.getLogger(AreaSumTest.class.getName());
  }

  @AfterAll
  public static void afterAll() {
    logger = null;
  }

  boolean[] rolling = new boolean[] {true, false};
  int[] boxSizes = new int[] {15, 9, 5, 3, 2, 1};
  int maxx = 97, maxy = 101;

  @SeededTest
  public void canComputeGlobalStatistics(RandomSeed seed) {
    final float[] data = createData(RngUtils.create(seed.getSeedAsLong()));
    final Statistics s = new Statistics(data);
    final AreaSum a = new AreaSum(data, maxx, maxy);
    for (final boolean r : rolling) {
      a.setRollingSums(r);
      double[] o = a.getStatistics(0, 0, maxy);
      Assertions.assertEquals(s.getN(), o[AreaSum.INDEX_COUNT]);
      TestAssertions.assertTest(s.getSum(), o[AreaSum.INDEX_SUM],
          TestHelper.doublesAreClose(1e-6, 0));

      o = a.getStatistics(new Rectangle(maxx, maxy));
      Assertions.assertEquals(s.getN(), o[AreaSum.INDEX_COUNT]);
      TestAssertions.assertTest(s.getSum(), o[AreaSum.INDEX_SUM],
          TestHelper.doublesAreClose(1e-6, 0));
    }
  }

  @SeededTest
  public void canComputeNxNRegionStatistics(RandomSeed seed) {
    final UniformRandomProvider r = RngUtils.create(seed.getSeedAsLong());
    final float[] data = createData(r);
    final AreaSum a1 = new AreaSum(data, maxx, maxy);
    a1.setRollingSums(true);
    final AreaSum a2 = new AreaSum(data, maxx, maxy);
    a2.setRollingSums(false);

    final FloatProcessor fp = new FloatProcessor(maxx, maxy, data);

    for (final int x : Random.sample(5, maxx, r)) {
      for (final int y : Random.sample(5, maxy, r)) {
        for (final int n : boxSizes) {
          final double[] e = a1.getStatistics(x, y, n);
          final double[] o = a2.getStatistics(x, y, n);
          TestAssertions.assertArrayTest(e, o, TestHelper.doublesAreClose(1e-6, 0));
          // TestLog.debug(logger,"%s vs %s", toString(e), toString(o));

          // Check with ImageJ
          fp.setRoi(new Rectangle(x - n, y - n, 2 * n + 1, 2 * n + 1));
          final ImageStatistics s = fp.getStatistics();

          Assertions.assertEquals(s.area, o[AreaSum.INDEX_COUNT]);
          final double sum = s.mean * s.area;
          TestAssertions.assertTest(sum, o[AreaSum.INDEX_SUM], TestHelper.doublesAreClose(1e-6, 0));
        }
      }
    }
  }

  @SeededTest
  public void canComputeNxMRegionStatistics(RandomSeed seed) {
    final UniformRandomProvider r = RngUtils.create(seed.getSeedAsLong());
    final float[] data = createData(r);
    final AreaSum a1 = new AreaSum(data, maxx, maxy);
    a1.setRollingSums(true);
    final AreaSum a2 = new AreaSum(data, maxx, maxy);
    a2.setRollingSums(false);

    final FloatProcessor fp = new FloatProcessor(maxx, maxy, data);

    for (final int x : Random.sample(5, maxx, r)) {
      for (final int y : Random.sample(5, maxy, r)) {
        for (final int nx : boxSizes) {
          for (final int ny : boxSizes) {
            final double[] e = a1.getStatistics(x, y, nx, ny);
            final double[] o = a2.getStatistics(x, y, nx, ny);
            TestAssertions.assertArrayTest(e, o, TestHelper.doublesAreClose(1e-6, 0));
            // TestLog.debug(logger,"%s vs %s", toString(e), toString(o));

            // Check with ImageJ
            fp.setRoi(new Rectangle(x - nx, y - ny, 2 * nx + 1, 2 * ny + 1));
            final ImageStatistics s = fp.getStatistics();

            Assertions.assertEquals(s.area, o[AreaSum.INDEX_COUNT]);
            TestAssertions.assertTest(s.mean * s.area, o[AreaSum.INDEX_SUM],
                TestHelper.doublesAreClose(1e-6, 0));
          }
        }
      }
    }
  }

  @SeededTest
  public void canComputeRectangleRegionStatistics(RandomSeed seed) {
    final UniformRandomProvider r = RngUtils.create(seed.getSeedAsLong());
    final float[] data = createData(r);
    final AreaSum a1 = new AreaSum(data, maxx, maxy);
    a1.setRollingSums(true);
    final AreaSum a2 = new AreaSum(data, maxx, maxy);
    a2.setRollingSums(false);

    final int width = 10, height = 12;
    final Rectangle roi = new Rectangle(width, height);

    final FloatProcessor fp = new FloatProcessor(maxx, maxy, data);

    for (final int x : Random.sample(5, maxx - width, r)) {
      for (final int y : Random.sample(5, maxy - height, r)) {
        roi.x = x;
        roi.y = y;
        final double[] e = a1.getStatistics(roi);
        final double[] o = a2.getStatistics(roi);
        TestAssertions.assertArrayTest(e, o, TestHelper.doublesAreClose(1e-6, 0));
        // TestLog.debug(logger,"%s vs %s", toString(e), toString(o));

        // Check with ImageJ
        fp.setRoi(roi);
        final ImageStatistics s = fp.getStatistics();

        Assertions.assertEquals(s.area, o[AreaSum.INDEX_COUNT]);
        TestAssertions.assertTest(s.mean * s.area, o[AreaSum.INDEX_SUM],
            TestHelper.doublesAreClose(1e-6, 0));
      }
    }
  }

  @Test
  public void canComputeStatisticsWithinClippedBounds() {
    final float[] data = new float[] {1, 2, 3, 4};
    final AreaSum a = new AreaSum(data, 2, 2);
    final Statistics stats = new Statistics(data);
    final int c = stats.getN();
    final double u = stats.getSum();
    for (final boolean r : rolling) {
      a.setRollingSums(r);
      for (final int n : boxSizes) {
        double[] o = a.getStatistics(0, 0, n);
        Assertions.assertEquals(c, o[AreaSum.INDEX_COUNT]);
        TestAssertions.assertTest(u, o[AreaSum.INDEX_SUM], TestHelper.doublesAreClose(1e-6, 0));

        final Rectangle bounds = new Rectangle(2 * n + 1, 2 * n + 1);
        o = a.getStatistics(bounds);
        Assertions.assertEquals(c, o[AreaSum.INDEX_COUNT]);
        TestAssertions.assertTest(u, o[AreaSum.INDEX_SUM], TestHelper.doublesAreClose(1e-6, 0));

        bounds.x--;
        bounds.y--;
        o = a.getStatistics(bounds);
        Assertions.assertEquals(c, o[AreaSum.INDEX_COUNT]);
        TestAssertions.assertTest(u, o[AreaSum.INDEX_SUM], TestHelper.doublesAreClose(1e-6, 0));
      }
    }
  }

  private class MyTimingtask extends BaseTimingTask {
    boolean rolling;
    int n;
    float[][] data;
    int[] sample;

    public MyTimingtask(boolean rolling, int n, float[][] data, int[] sample) {
      super(((rolling) ? "Rolling" : "Simple") + n);
      this.rolling = rolling;
      this.n = n;
      this.data = data;
      this.sample = sample;
    }

    @Override
    public int getSize() {
      return data.length;
    }

    @Override
    public Object getData(int i) {
      return data[i];
    }

    @Override
    public Object run(Object data) {
      final float[] d = (float[]) data;
      final AreaSum a = new AreaSum(d, maxx, maxy);
      a.setRollingSums(rolling);
      for (int i = 0; i < sample.length; i += 2) {
        a.getStatistics(sample[i], sample[i + 1], n);
      }
      return null;
    }
  }

  @SpeedTag
  @SeededTest
  public void simpleIsfasterAtLowDensityAndNLessThan10(RandomSeed seed) {
    // Test the speed for computing the noise around spots at a density of roughly 1 / 100 pixels.
    speedTest(seed, 1.0 / 100, false, 1, 10);
  }

  @SpeedTag
  @SeededTest
  public void simpleIsfasterAtMediumDensityAndNLessThan3(RandomSeed seed) {
    // Test the speed for computing the noise around each 3x3 box
    // using a region of 3x3 (n=1) to 5x5 (n=2)
    speedTest(seed, 1.0 / 9, false, 1, 2);
  }

  @SpeedTag
  @SeededTest
  public void rollingIsfasterAtHighDensity(RandomSeed seed) {
    // Since this is a slow test
    Assumptions.assumeTrue(TestSettings.allow(TestComplexity.MEDIUM));

    // Test for sampling half the pixels. Ignore the very small box size
    speedTest(seed, 0.5, true, 2, Integer.MAX_VALUE);
  }

  private void speedTest(RandomSeed seed, double density, boolean rollingIsFaster, int minN,
      int maxN) {
    final UniformRandomProvider r = RngUtils.create(seed.getSeedAsLong());

    final int k = (int) Math.round(maxx * maxy * density);
    final int[] x = Random.sample(k, maxx, r);
    final int[] y = Random.sample(k, maxy, r);
    final int[] sample = new int[k * 2];
    for (int i = 0, j = 0; i < x.length; i++) {
      sample[j++] = x[i];
      sample[j++] = y[i];
    }

    final float[][] data = new float[10][];
    for (int i = 0; i < data.length; i++) {
      data[i] = createData(r);
    }

    final TimingService ts = new TimingService();
    for (final int n : boxSizes) {
      if (n < minN || n > maxN) {
        continue;
      }
      ts.execute(new MyTimingtask(true, n, data, sample));
      ts.execute(new MyTimingtask(false, n, data, sample));
    }
    final int size = ts.getSize();
    ts.repeat();
    logger.info(ts.getReport(size));
    // Do not let this fail the test suite
    // Assertions.assertEquals(ts.get(-2).getMean() < ts.get(-1).getMean(), rollingIsFaster);
    logger
        .log(TestLog.getResultRecord(ts.get(-2).getMean() < ts.get(-1).getMean() == rollingIsFaster,
            "AreaSum Density=%g RollingIsFaster=%b N=%d:%d: rolling %s vs simple %s", density,
            rollingIsFaster, minN, maxN, ts.get(-2).getMean(), ts.get(-1).getMean()));
  }

  private float[] createData(UniformRandomProvider r) {
    final float[] d = new float[maxx * maxy];
    for (int i = 0; i < d.length; i++) {
      d[i] = r.nextFloat();
    }
    return d;
  }

  static String toString(double[] d) {
    return java.util.Arrays.toString(d);
  }
}
