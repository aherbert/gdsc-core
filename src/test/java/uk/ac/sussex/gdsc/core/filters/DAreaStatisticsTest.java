package uk.ac.sussex.gdsc.core.filters;

import uk.ac.sussex.gdsc.core.utils.RandomUtils;
import uk.ac.sussex.gdsc.core.utils.Statistics;
import uk.ac.sussex.gdsc.test.api.TestAssertions;
import uk.ac.sussex.gdsc.test.api.TestHelper;
import uk.ac.sussex.gdsc.test.junit5.RandomSeed;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.junit5.SpeedTag;
import uk.ac.sussex.gdsc.test.rng.RngUtils;
import uk.ac.sussex.gdsc.test.utils.BaseTimingTask;
import uk.ac.sussex.gdsc.test.utils.TestComplexity;
import uk.ac.sussex.gdsc.test.utils.TestLogUtils;
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
public class DAreaStatisticsTest {
  private static Logger logger;

  @BeforeAll
  public static void beforeAll() {
    logger = Logger.getLogger(DAreaStatisticsTest.class.getName());
  }

  @AfterAll
  public static void afterAll() {
    logger = null;
  }

  boolean[] rolling = new boolean[] {true, false};
  int[] boxSizes = new int[] {15, 9, 5, 3, 2, 1};
  int maxx = 97;
  int maxy = 101;

  @SeededTest
  public void canComputeGlobalStatistics(RandomSeed seed) {
    final double[] data = createData(RngUtils.create(seed.getSeedAsLong()));
    final Statistics s = new Statistics(data);
    final DAreaStatistics a = new DAreaStatistics(data, maxx, maxy);
    for (final boolean rng : rolling) {
      a.setRollingSums(rng);
      double[] obs = a.getStatistics(0, 0, maxy);
      Assertions.assertEquals(s.getN(), obs[DAreaSum.INDEX_COUNT]);
      TestAssertions.assertTest(s.getSum(), obs[DAreaSum.INDEX_SUM],
          TestHelper.doublesAreClose(1e-6, 0));
      TestAssertions.assertTest(s.getStandardDeviation(), obs[DAreaStatistics.INDEX_SD],
          TestHelper.doublesAreClose(1e-6, 0));

      obs = a.getStatistics(new Rectangle(maxx, maxy));
      Assertions.assertEquals(s.getN(), obs[DAreaSum.INDEX_COUNT]);
      TestAssertions.assertTest(s.getSum(), obs[DAreaSum.INDEX_SUM],
          TestHelper.doublesAreClose(1e-6, 0));
      TestAssertions.assertTest(s.getStandardDeviation(), obs[DAreaStatistics.INDEX_SD],
          TestHelper.doublesAreClose(1e-6, 0));
    }
  }

  @SeededTest
  public void canComputeNxNRegionStatistics(RandomSeed seed) {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeedAsLong());
    final double[] data = createData(rng);
    final DAreaStatistics a1 = new DAreaStatistics(data, maxx, maxy);
    a1.setRollingSums(true);
    final DAreaStatistics a2 = new DAreaStatistics(data, maxx, maxy);
    a2.setRollingSums(false);

    final FloatProcessor fp = new FloatProcessor(maxx, maxy, data);

    for (final int x : RandomUtils.sample(5, maxx, rng)) {
      for (final int y : RandomUtils.sample(5, maxy, rng)) {
        for (final int size : boxSizes) {
          final double[] exp = a1.getStatistics(x, y, size);
          final double[] obs = a2.getStatistics(x, y, size);
          TestAssertions.assertArrayTest(exp, obs, TestHelper.doublesAreClose(1e-6, 0));
          // TestLog.debug(logger,"%s vs %s", toString(exp), toString(obs));

          // Check with ImageJ
          fp.setRoi(new Rectangle(x - size, y - size, 2 * size + 1, 2 * size + 1));
          final ImageStatistics s = fp.getStatistics();

          Assertions.assertEquals(s.area, obs[DAreaSum.INDEX_COUNT]);
          final double sum = s.mean * s.area;
          TestAssertions.assertTest(sum, obs[DAreaSum.INDEX_SUM],
              TestHelper.doublesAreClose(1e-6, 0));
          TestAssertions.assertTest(s.stdDev, obs[DAreaStatistics.INDEX_SD],
              TestHelper.doublesAreClose(1e-6, 0));
        }
      }
    }
  }

  @SeededTest
  public void canComputeNxMRegionStatistics(RandomSeed seed) {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeedAsLong());
    final double[] data = createData(rng);
    final DAreaStatistics a1 = new DAreaStatistics(data, maxx, maxy);
    a1.setRollingSums(true);
    final DAreaStatistics a2 = new DAreaStatistics(data, maxx, maxy);
    a2.setRollingSums(false);

    final FloatProcessor fp = new FloatProcessor(maxx, maxy, data);

    for (final int x : RandomUtils.sample(5, maxx, rng)) {
      for (final int y : RandomUtils.sample(5, maxy, rng)) {
        for (final int nx : boxSizes) {
          for (final int ny : boxSizes) {
            final double[] exp = a1.getStatistics(x, y, nx, ny);
            final double[] obs = a2.getStatistics(x, y, nx, ny);
            TestAssertions.assertArrayTest(exp, obs, TestHelper.doublesAreClose(1e-6, 0));
            // TestLog.debug(logger,"%s vs %s", toString(exp), toString(obs));

            // Check with ImageJ
            fp.setRoi(new Rectangle(x - nx, y - ny, 2 * nx + 1, 2 * ny + 1));
            final ImageStatistics s = fp.getStatistics();

            Assertions.assertEquals(s.area, obs[DAreaSum.INDEX_COUNT]);
            final double sum = s.mean * s.area;
            TestAssertions.assertTest(sum, obs[DAreaSum.INDEX_SUM],
                TestHelper.doublesAreClose(1e-6, 0));
            TestAssertions.assertTest(s.stdDev, obs[DAreaStatistics.INDEX_SD],
                TestHelper.doublesAreClose(1e-6, 0));
          }
        }
      }
    }
  }

  @SeededTest
  public void canComputeRectangleRegionStatistics(RandomSeed seed) {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeedAsLong());
    final double[] data = createData(rng);
    final DAreaStatistics a1 = new DAreaStatistics(data, maxx, maxy);
    a1.setRollingSums(true);
    final DAreaStatistics a2 = new DAreaStatistics(data, maxx, maxy);
    a2.setRollingSums(false);

    final int width = 10;
    final int height = 12;
    final Rectangle roi = new Rectangle(width, height);

    final FloatProcessor fp = new FloatProcessor(maxx, maxy, data);

    for (final int x : RandomUtils.sample(5, maxx - width, rng)) {
      for (final int y : RandomUtils.sample(5, maxy - height, rng)) {
        roi.x = x;
        roi.y = y;
        final double[] exp = a1.getStatistics(roi);
        final double[] obs = a2.getStatistics(roi);
        TestAssertions.assertArrayTest(exp, obs, TestHelper.doublesAreClose(1e-6, 0));
        // TestLog.debug(logger,"%s vs %s", toString(exp), toString(obs));

        // Check with ImageJ
        fp.setRoi(roi);
        final ImageStatistics s = fp.getStatistics();

        Assertions.assertEquals(s.area, obs[DAreaSum.INDEX_COUNT]);
        TestAssertions.assertTest(s.mean * s.area, obs[DAreaSum.INDEX_SUM],
            TestHelper.doublesAreClose(1e-6, 0));
        TestAssertions.assertTest(s.stdDev, obs[DAreaStatistics.INDEX_SD],
            TestHelper.doublesAreClose(1e-6, 0));
      }
    }
  }

  @Test
  public void canComputeStatisticsWithinClippedBounds() {
    final double[] data = new double[] {1, 2, 3, 4};
    final DAreaStatistics a = new DAreaStatistics(data, 2, 2);
    final Statistics stats = new Statistics(data);
    final int c = stats.getN();
    final double u = stats.getSum();
    final double s = stats.getStandardDeviation();
    for (final boolean rng : rolling) {
      a.setRollingSums(rng);
      for (final int size : boxSizes) {
        double[] obs = a.getStatistics(0, 0, size);
        Assertions.assertEquals(c, obs[DAreaSum.INDEX_COUNT]);
        TestAssertions.assertTest(u, obs[DAreaSum.INDEX_SUM], TestHelper.doublesAreClose(1e-6, 0));
        TestAssertions.assertTest(s, obs[DAreaStatistics.INDEX_SD],
            TestHelper.doublesAreClose(1e-6, 0));

        final Rectangle bounds = new Rectangle(2 * size + 1, 2 * size + 1);
        obs = a.getStatistics(bounds);
        Assertions.assertEquals(c, obs[DAreaSum.INDEX_COUNT]);
        TestAssertions.assertTest(u, obs[DAreaSum.INDEX_SUM], TestHelper.doublesAreClose(1e-6, 0));
        TestAssertions.assertTest(s, obs[DAreaStatistics.INDEX_SD],
            TestHelper.doublesAreClose(1e-6, 0));

        bounds.x--;
        bounds.y--;
        obs = a.getStatistics(bounds);
        Assertions.assertEquals(c, obs[DAreaSum.INDEX_COUNT]);
        TestAssertions.assertTest(u, obs[DAreaSum.INDEX_SUM], TestHelper.doublesAreClose(1e-6, 0));
        TestAssertions.assertTest(s, obs[DAreaStatistics.INDEX_SD],
            TestHelper.doublesAreClose(1e-6, 0));
      }
    }
  }

  private class MyTimingtask extends BaseTimingTask {
    boolean rolling;
    int size;
    double[][] data;
    int[] sample;

    public MyTimingtask(boolean rolling, int size, double[][] data, int[] sample) {
      super(((rolling) ? "Rolling" : "Simple") + size);
      this.rolling = rolling;
      this.size = size;
      this.data = data;
      this.sample = sample;
    }

    @Override
    public int getSize() {
      return data.length;
    }

    @Override
    public Object getData(int index) {
      return data[index];
    }

    @Override
    public Object run(Object data) {
      final double[] d = (double[]) data;
      final DAreaStatistics a = new DAreaStatistics(d, maxx, maxy);
      a.setRollingSums(rolling);
      for (int i = 0; i < sample.length; i += 2) {
        a.getStatistics(sample[i], sample[i + 1], size);
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
  public void simpleIsfasterAtMediumDensityAndNLessThan5(RandomSeed seed) {
    // Test the speed for computing the noise around each 3x3 box
    // using a region of 3x3 (size=1) to 9x9 (size=4)
    speedTest(seed, 1.0 / 9, false, 1, 4);
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
    final UniformRandomProvider rng = RngUtils.create(seed.getSeedAsLong());

    final int k = (int) Math.round(maxx * maxy * density);
    final int[] x = RandomUtils.sample(k, maxx, rng);
    final int[] y = RandomUtils.sample(k, maxy, rng);
    final int[] sample = new int[k * 2];
    for (int i = 0, j = 0; i < x.length; i++) {
      sample[j++] = x[i];
      sample[j++] = y[i];
    }

    final double[][] data = new double[10][];
    for (int i = 0; i < data.length; i++) {
      data[i] = createData(rng);
    }

    final TimingService ts = new TimingService();
    for (final int size : boxSizes) {
      if (size < minN || size > maxN) {
        continue;
      }
      ts.execute(new MyTimingtask(true, size, data, sample));
      ts.execute(new MyTimingtask(false, size, data, sample));
    }
    final int size = ts.getSize();
    ts.repeat();
    logger.info(ts.getReport(size));
    // Do not let this fail the test suite
    // Assertions.assertEquals(ts.get(-2).getMean() < ts.get(-1).getMean(), rollingIsFaster);
    logger.log(
        TestLogUtils.getResultRecord(ts.get(-2).getMean() < ts.get(-1).getMean() == rollingIsFaster,
            "DAreaStatistics Density=%g RollingIsFaster=%b N=%d:%d: rolling %s vs simple %s",
            density, rollingIsFaster, minN, maxN, ts.get(-2).getMean(), ts.get(-1).getMean()));
  }

  private double[] createData(UniformRandomProvider rng) {
    final double[] d = new double[maxx * maxy];
    for (int i = 0; i < d.length; i++) {
      d[i] = rng.nextDouble();
    }
    return d;
  }
}
