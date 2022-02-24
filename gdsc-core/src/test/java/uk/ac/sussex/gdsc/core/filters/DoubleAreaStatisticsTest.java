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

package uk.ac.sussex.gdsc.core.filters;

import ij.process.FloatProcessor;
import ij.process.ImageStatistics;
import java.awt.Rectangle;
import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.core.utils.Statistics;
import uk.ac.sussex.gdsc.core.utils.rng.RandomUtils;
import uk.ac.sussex.gdsc.test.api.TestAssertions;
import uk.ac.sussex.gdsc.test.api.TestHelper;
import uk.ac.sussex.gdsc.test.api.function.DoubleDoubleBiPredicate;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.rng.RngUtils;
import uk.ac.sussex.gdsc.test.utils.RandomSeed;

@SuppressWarnings({"javadoc"})
class DoubleAreaStatisticsTest {

  boolean[] rolling = new boolean[] {true, false};
  int[] boxSizes = new int[] {15, 9, 5, 3, 2, 1};
  int maxx = 97;
  int maxy = 101;

  DoubleDoubleBiPredicate equality = TestHelper.doublesAreClose(1e-6);

  @Test
  void canComputeNoAreaResult() {
    final double[] data = {0, 1, 2};
    final DoubleAreaStatistics a = DoubleAreaStatistics.wrap(data, 3, 1);
    final double[] expected = {0, Double.NaN, Double.NaN};
    Assertions.assertArrayEquals(expected, a.getStatistics(-1, 0, 0));
    Assertions.assertArrayEquals(expected, a.getStatistics(0, -1, 0));
    Assertions.assertArrayEquals(expected, a.getStatistics(10, 0, 0));
    Assertions.assertArrayEquals(expected, a.getStatistics(0, 10, 0));
    Assertions.assertArrayEquals(expected, a.getStatistics(0, 0, -1));
    Assertions.assertArrayEquals(expected, a.getStatistics(-1, 0, 0, 0));
    Assertions.assertArrayEquals(expected, a.getStatistics(0, -1, 0, 0));
    Assertions.assertArrayEquals(expected, a.getStatistics(10, 0, 0, 0));
    Assertions.assertArrayEquals(expected, a.getStatistics(0, 10, 0, 0));
    Assertions.assertArrayEquals(expected, a.getStatistics(0, 0, -1, 0));
    Assertions.assertArrayEquals(expected, a.getStatistics(0, 0, 0, -1));
    Assertions.assertArrayEquals(expected, a.getStatistics(new Rectangle(0, 0, 0, 1)));
    Assertions.assertArrayEquals(expected, a.getStatistics(new Rectangle(0, 0, 1, 0)));
    Assertions.assertArrayEquals(expected, a.getStatistics(new Rectangle(10, 0, 1, 1)));
    Assertions.assertArrayEquals(expected, a.getStatistics(new Rectangle(0, 10, 1, 1)));
    Assertions.assertArrayEquals(expected, a.getStatistics(new Rectangle(-10, 0, 1, 1)));
    Assertions.assertArrayEquals(expected, a.getStatistics(new Rectangle(0, -10, 1, 1)));
  }

  @Test
  void canComputeSinglePointStatistics() {
    final double[] data = {0, 1, 2};
    final DoubleAreaStatistics a = DoubleAreaStatistics.wrap(data, 3, 1);
    Assertions.assertArrayEquals(new double[] {1, 0, 0}, a.getSingleResult(0, 0));
    Assertions.assertArrayEquals(new double[] {1, 1, 0}, a.getSingleResult(1, 0));
    Assertions.assertArrayEquals(new double[] {1, 2, 0}, a.getSingleResult(2, 0));
    Assertions.assertArrayEquals(new double[] {1, 0, 0}, a.getStatistics(0, 0, 0));
    Assertions.assertArrayEquals(new double[] {1, 1, 0}, a.getStatistics(1, 0, 0));
    Assertions.assertArrayEquals(new double[] {1, 2, 0}, a.getStatistics(2, 0, 0));
    Assertions.assertArrayEquals(new double[] {1, 0, 0}, a.getStatistics(0, 0, 0, 0));
    Assertions.assertArrayEquals(new double[] {1, 1, 0}, a.getStatistics(1, 0, 0, 0));
    Assertions.assertArrayEquals(new double[] {1, 2, 0}, a.getStatistics(2, 0, 0, 0));
    // Hit case of nx or ny not equals to zero
    Assertions.assertArrayEquals(new double[] {3, 3, 1}, a.getStatistics(1, 0, 1, 0));
    Assertions.assertArrayEquals(new double[] {1, 1, 0}, a.getStatistics(1, 0, 0, 1));
  }

  @Test
  void canComputeNoResidualsStatistics() {
    final double[] data = {2, 2, 2};
    final DoubleAreaStatistics a = DoubleAreaStatistics.wrap(data, 3, 1);
    Assertions.assertArrayEquals(new double[] {1, 2, 0}, a.getStatistics(0, 0, 0));
    Assertions.assertArrayEquals(new double[] {2, 4, 0}, a.getStatistics(0, 0, 1));
    Assertions.assertArrayEquals(new double[] {3, 6, 0}, a.getStatistics(0, 0, 2));
    Assertions.assertArrayEquals(new double[] {3, 6, 0}, a.getStatistics(1, 0, 1));
  }

  @SeededTest
  void canComputeGlobalStatistics(RandomSeed seed) {
    final double[] data = createData(RngUtils.create(seed.get()));
    final Statistics s = Statistics.create(data);
    final DoubleAreaStatistics a = DoubleAreaStatistics.wrap(data, maxx, maxy);
    for (final boolean rng : rolling) {
      a.setRollingSums(rng);
      double[] obs = a.getStatistics(0, 0, maxy);
      Assertions.assertEquals(s.getN(), obs[AreaStatistics.INDEX_COUNT]);
      TestAssertions.assertTest(s.getSum(), obs[AreaStatistics.INDEX_SUM], equality);
      TestAssertions.assertTest(s.getStandardDeviation(), obs[AreaStatistics.INDEX_SD], equality);

      obs = a.getStatistics(new Rectangle(maxx, maxy));
      Assertions.assertEquals(s.getN(), obs[AreaStatistics.INDEX_COUNT]);
      TestAssertions.assertTest(s.getSum(), obs[AreaStatistics.INDEX_SUM], equality);
      TestAssertions.assertTest(s.getStandardDeviation(), obs[AreaStatistics.INDEX_SD], equality);
    }
  }

  @SeededTest
  void canComputeNxNRegionStatistics(RandomSeed seed) {
    final UniformRandomProvider rng = RngUtils.create(seed.get());
    final double[] data = createData(rng);
    final DoubleAreaStatistics a1 = DoubleAreaStatistics.wrap(data, maxx, maxy);
    a1.setRollingSums(true);
    final DoubleAreaStatistics a2 = DoubleAreaStatistics.wrap(data, maxx, maxy);
    a2.setRollingSums(false);

    final FloatProcessor fp = new FloatProcessor(maxx, maxy, data);

    for (final int x : RandomUtils.sample(5, maxx, rng)) {
      for (final int y : RandomUtils.sample(5, maxy, rng)) {
        for (final int size : boxSizes) {
          final double[] exp = a1.getStatistics(x, y, size);
          final double[] obs = a2.getStatistics(x, y, size);
          Assertions.assertEquals(exp[AreaStatistics.INDEX_COUNT], obs[AreaStatistics.INDEX_COUNT]);
          TestAssertions.assertTest(exp[AreaStatistics.INDEX_SUM], obs[AreaStatistics.INDEX_SUM],
              equality);
          TestAssertions.assertTest(exp[AreaStatistics.INDEX_SD], obs[AreaStatistics.INDEX_SD],
              equality);
          // TestLog.debug(logger,"%s vs %s", toString(exp), toString(obs));

          // Check with ImageJ
          fp.setRoi(new Rectangle(x - size, y - size, 2 * size + 1, 2 * size + 1));
          final ImageStatistics s = fp.getStatistics();

          Assertions.assertEquals(s.area, obs[AreaStatistics.INDEX_COUNT]);
          final double sum = s.mean * s.area;
          TestAssertions.assertTest(sum, obs[AreaStatistics.INDEX_SUM], equality);
          TestAssertions.assertTest(s.stdDev, obs[AreaStatistics.INDEX_SD], equality);
        }
      }
    }
  }

  @SeededTest
  void canComputeNxMRegionStatistics(RandomSeed seed) {
    final UniformRandomProvider rng = RngUtils.create(seed.get());
    final double[] data = createData(rng);
    final DoubleAreaStatistics a1 = DoubleAreaStatistics.wrap(data, maxx, maxy);
    a1.setRollingSums(true);
    final DoubleAreaStatistics a2 = DoubleAreaStatistics.wrap(data, maxx, maxy);
    a2.setRollingSums(false);

    final FloatProcessor fp = new FloatProcessor(maxx, maxy, data);

    for (final int x : RandomUtils.sample(5, maxx, rng)) {
      for (final int y : RandomUtils.sample(5, maxy, rng)) {
        for (final int nx : boxSizes) {
          for (final int ny : boxSizes) {
            final double[] exp = a1.getStatistics(x, y, nx, ny);
            final double[] obs = a2.getStatistics(x, y, nx, ny);
            Assertions.assertEquals(exp[AreaStatistics.INDEX_COUNT],
                obs[AreaStatistics.INDEX_COUNT]);
            TestAssertions.assertTest(exp[AreaStatistics.INDEX_SUM], obs[AreaStatistics.INDEX_SUM],
                equality);
            TestAssertions.assertTest(exp[AreaStatistics.INDEX_SD], obs[AreaStatistics.INDEX_SD],
                equality);
            // TestLog.debug(logger,"%s vs %s", toString(exp), toString(obs));

            // Check with ImageJ
            fp.setRoi(new Rectangle(x - nx, y - ny, 2 * nx + 1, 2 * ny + 1));
            final ImageStatistics s = fp.getStatistics();

            Assertions.assertEquals(s.area, obs[AreaStatistics.INDEX_COUNT]);
            final double sum = s.mean * s.area;
            TestAssertions.assertTest(sum, obs[AreaStatistics.INDEX_SUM], equality);
            TestAssertions.assertTest(s.stdDev, obs[AreaStatistics.INDEX_SD], equality);
          }
        }
      }
    }
  }

  @SeededTest
  void canComputeRectangleRegionStatistics(RandomSeed seed) {
    final UniformRandomProvider rng = RngUtils.create(seed.get());
    final double[] data = createData(rng);
    final DoubleAreaStatistics a1 = DoubleAreaStatistics.wrap(data, maxx, maxy);
    a1.setRollingSums(true);
    final DoubleAreaStatistics a2 = DoubleAreaStatistics.wrap(data, maxx, maxy);
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
        Assertions.assertEquals(exp[AreaStatistics.INDEX_COUNT], obs[AreaStatistics.INDEX_COUNT]);
        TestAssertions.assertTest(exp[AreaStatistics.INDEX_SUM], obs[AreaStatistics.INDEX_SUM],
            equality);
        TestAssertions.assertTest(exp[AreaStatistics.INDEX_SD], obs[AreaStatistics.INDEX_SD],
            equality);
        // TestLog.debug(logger,"%s vs %s", toString(exp), toString(obs));

        // Check with ImageJ
        fp.setRoi(roi);
        final ImageStatistics s = fp.getStatistics();

        Assertions.assertEquals(s.area, obs[AreaStatistics.INDEX_COUNT]);
        TestAssertions.assertTest(s.mean * s.area, obs[AreaStatistics.INDEX_SUM], equality);
        TestAssertions.assertTest(s.stdDev, obs[AreaStatistics.INDEX_SD], equality);
      }
    }
  }

  @Test
  void canComputeStatisticsWithinClippedBounds() {
    final double[] data = new double[] {1, 2, 3, 4};
    final DoubleAreaStatistics a = DoubleAreaStatistics.wrap(data, 2, 2);
    final Statistics stats = Statistics.create(data);
    final int c = stats.getN();
    final double u = stats.getSum();
    final double s = stats.getStandardDeviation();
    for (final boolean rng : rolling) {
      a.setRollingSums(rng);
      for (final int size : boxSizes) {
        double[] obs = a.getStatistics(0, 0, size);
        Assertions.assertEquals(c, obs[AreaStatistics.INDEX_COUNT]);
        TestAssertions.assertTest(u, obs[AreaStatistics.INDEX_SUM], equality);
        TestAssertions.assertTest(s, obs[AreaStatistics.INDEX_SD], equality);

        final Rectangle bounds = new Rectangle(2 * size + 1, 2 * size + 1);
        obs = a.getStatistics(bounds);
        Assertions.assertEquals(c, obs[AreaStatistics.INDEX_COUNT]);
        TestAssertions.assertTest(u, obs[AreaStatistics.INDEX_SUM], equality);
        TestAssertions.assertTest(s, obs[AreaStatistics.INDEX_SD], equality);

        bounds.x--;
        bounds.y--;
        obs = a.getStatistics(bounds);
        Assertions.assertEquals(c, obs[AreaStatistics.INDEX_COUNT]);
        TestAssertions.assertTest(u, obs[AreaStatistics.INDEX_SUM], equality);
        TestAssertions.assertTest(s, obs[AreaStatistics.INDEX_SD], equality);
      }
    }
  }

  private double[] createData(UniformRandomProvider rng) {
    final double[] d = new double[maxx * maxy];
    for (int i = 0; i < d.length; i++) {
      d[i] = rng.nextDouble();
    }
    return d;
  }

  // Note:
  // Previous speed test have been removed.
  // Names are preserved for the conclusions:
  // - simpleIsfasterAtLowDensityAndNLessThan10: density of roughly 1 / 100 pixels.
  // - simpleIsfasterAtMediumDensityAndNLessThan5: density of 1 / 9 pixels,
  // noise around each 3x3 box using a region of 3x3 (size=1) to 9x9 (size=4)
  // - rollingIsfasterAtHighDensity: density of 0.5
}
