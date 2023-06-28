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

package uk.ac.sussex.gdsc.core.math.hull;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import org.apache.commons.rng.sampling.shape.UnitBallSampler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.core.math.hull.KnnConcaveHull2d.AngleList;
import uk.ac.sussex.gdsc.test.rng.RngFactory;

@SuppressWarnings({"javadoc"})
class KnnConcaveHull2dTest {

  @Test
  void testClockwiseTurns() {
    Assertions.assertEquals(0, AngleList.clockwiseTurns(Math.PI, Math.PI));
    Assertions.assertEquals(0, AngleList.clockwiseTurns(-Math.PI, Math.PI));
    Assertions.assertEquals(0.0, AngleList.clockwiseTurns(Math.PI, -Math.PI));
    Assertions.assertEquals(0.0, AngleList.clockwiseTurns(0, 2 * Math.PI));
    Assertions.assertEquals(0.25, AngleList.clockwiseTurns(0, 3 * Math.PI / 2));
    Assertions.assertEquals(0.25, AngleList.clockwiseTurns(Math.PI, Math.PI / 2));
    Assertions.assertEquals(0.5, AngleList.clockwiseTurns(0, Math.PI));
    Assertions.assertEquals(0.5, AngleList.clockwiseTurns(0, -Math.PI));
    Assertions.assertEquals(0.75, AngleList.clockwiseTurns(Math.PI / 2, Math.PI));
    Assertions.assertEquals(0.75, AngleList.clockwiseTurns(0, Math.PI / 2));
  }

  @Test
  void testAngleListSortByAngle() {
    final double[][] points = {{0, 0}, {1, 0}, {1, 1}, {0, 1}, {0.5, 0.5}};
    final AngleList knn = new AngleList(4);
    for (int i = 0; i < 4; i++) {
      // Ignore distance
      knn.add(i, 0.0);
      Assertions.assertEquals(i + 1, knn.size());
    }
    Assertions.assertEquals(4, knn.size());
    final int current = 4;
    knn.sortByAngle(points, current, AngleList.angle(points[current], points[0]));
    Assertions.assertArrayEquals(new int[] {1, 2, 3, 0}, toArray(knn));
    knn.sortByAngle(points, current, AngleList.angle(points[current], points[1]));
    Assertions.assertArrayEquals(new int[] {2, 3, 0, 1}, toArray(knn));
    knn.sortByAngle(points, current, AngleList.angle(points[current], points[2]));
    Assertions.assertArrayEquals(new int[] {3, 0, 1, 2}, toArray(knn));
    knn.sortByAngle(points, current, AngleList.angle(points[current], points[3]));
    Assertions.assertArrayEquals(new int[] {0, 1, 2, 3}, toArray(knn));
    knn.clear();
    Assertions.assertEquals(0, knn.size());
  }

  @Test
  void testAngleListSortByDistance() {
    final double[][] points = {{1, 0}, {2, 0}, {3, 0}, {0, 0}};
    final AngleList knn = new AngleList(4);
    for (int i = 0; i < 3; i++) {
      // Add distance
      knn.add(i, points[i][0]);
      Assertions.assertEquals(i + 1, knn.size());
    }
    Assertions.assertEquals(3, knn.size());
    final int current = 3;
    knn.sortByAngle(points, current, AngleList.angle(points[current], points[3]));
    Assertions.assertArrayEquals(new int[] {2, 1, 0}, toArray(knn));
    knn.clear();
    Assertions.assertEquals(0, knn.size());
  }

  private static int[] toArray(AngleList knn) {
    final int[] list = new int[knn.size()];
    for (int i = 0; i < knn.size(); i++) {
      list[i] = knn.getIndex(i);
    }
    return list;
  }

  @Test
  void testNumberOfNeighbours() {
    final KnnConcaveHull2d.Builder builder = KnnConcaveHull2d.newBuilder();
    Assertions.assertEquals(3, builder.getK());
    Assertions.assertSame(builder, builder.setK(7));
    Assertions.assertEquals(7, builder.getK());
    builder.setK(0);
    Assertions.assertEquals(3, builder.getK());
  }

  @Test
  void cannotComputeKnnConcaveHullFromNoCoords() {
    final double[] x = new double[] {};
    final double[] y = new double[] {};
    final Hull2d hull = KnnConcaveHull2d.create(x, y);
    Assertions.assertNull(hull);
  }

  @Test
  void canComputeKnnConcaveHullFromSquare() {
    final double[] ex = new double[] {0, 10, 10, 0};
    final double[] ey = new double[] {0, 0, 10, 10};
    for (int i = 0; i < ex.length; i++) {
      final double[] x = new double[ex.length];
      final double[] y = new double[ey.length];
      for (int j = 0; j < ex.length; j++) {
        final int n = (i + j) % ex.length;
        x[j] = ex[n];
        y[j] = ey[n];
      }
      final Hull2d hull = KnnConcaveHull2d.create(x, y);
      check(ex, ey, hull);
    }
  }

  @Test
  void canComputeKnnConcaveHullFromSquareWithInternalPoint() {
    final double[] x = new double[] {0, 0, 10, 10, 5};
    final double[] y = new double[] {0, 10, 10, 0, 5};
    final double[] ex = new double[] {0, 10, 10, 0};
    final double[] ey = new double[] {0, 0, 10, 10};
    final Hull2d hull = KnnConcaveHull2d.create(x, y);
    check(ex, ey, hull);
  }

  @Test
  void canComputeKnnConcaveHullFromSquareWithInternalPoint2() {
    final double[] x = new double[] {0, 0, 5, 10, 10};
    final double[] y = new double[] {0, 10, 5, 10, 0};
    final double[] ex = new double[] {0, 10, 10, 0};
    final double[] ey = new double[] {0, 0, 10, 10};
    final Hull2d hull = KnnConcaveHull2d.create(x, y);
    check(ex, ey, hull);
  }

  private static void check(double[] ex, double[] ey, Hull2d hull) {
    if (ex == null) {
      Assertions.assertNull(hull);
      return;
    }
    Assertions.assertNotNull(hull);
    final int n = ex.length;
    Assertions.assertEquals(n, hull.getNumberOfVertices());

    final double[][] points = hull.getVertices();

    for (int i = 0; i < n; i++) {
      Assertions.assertEquals(ex[i], points[i][0]);
      Assertions.assertEquals(ey[i], points[i][1]);
    }
  }

  @Test
  void canBuildWithNoPoints() {
    Assertions.assertNull(KnnConcaveHull2d.newBuilder().build());
  }

  @Test
  void canBuildWithOnePoint() {
    final double[] x = new double[] {1.2345, 6.78};
    final Hull2d hull = KnnConcaveHull2d.newBuilder().add(x).build();
    Assertions.assertEquals(1, hull.getNumberOfVertices());
    Assertions.assertEquals(2, hull.dimensions());
    Assertions.assertEquals(0, hull.getLength());
    Assertions.assertEquals(0, hull.getArea());
    Assertions.assertArrayEquals(new double[] {x[0], x[1]}, hull.getCentroid());
  }

  @Test
  void canClearBuilder() {
    final KnnConcaveHull2d.Builder builder = KnnConcaveHull2d.newBuilder();
    builder.add(1, 2);
    final Hull2d hull1 = builder.build();
    Assertions.assertNotNull(hull1);
    final Hull2d hull2 = builder.build();
    Assertions.assertNotNull(hull2);
    Assertions.assertNotSame(hull1, hull2);
    builder.clear();
    final Hull2d hull3 = builder.build();
    Assertions.assertNull(hull3);
  }

  @Test
  void canCreateWithNoPoints() {
    final double[] x = new double[0];
    Assertions.assertNull(KnnConcaveHull2d.create(x, x));
  }

  @Test
  void canCreateWithOnePoint() {
    final double[] x = new double[] {1.2345f};
    final Hull2d hull = KnnConcaveHull2d.create(x, x);
    Assertions.assertEquals(1, hull.getNumberOfVertices());
    Assertions.assertEquals(0, hull.getLength());
    Assertions.assertEquals(0, hull.getArea());
    Assertions.assertArrayEquals(new double[] {x[0], x[0]}, hull.getCentroid());
  }

  @Test
  void canCreateWithTwoPoints() {
    final double[] x = new double[] {1.5f, 2.5f};
    final Hull2d hull = KnnConcaveHull2d.create(x, x);
    Assertions.assertEquals(2, hull.getNumberOfVertices());
    Assertions.assertEquals(2 * Math.sqrt(2), hull.getLength(), 1e-10);
    Assertions.assertEquals(0, hull.getArea());
    Assertions.assertArrayEquals(new double[] {2, 2}, hull.getCentroid());
  }

  @Test
  void canCreateWithThreePoints() {
    final double[] x = new double[] {1, 2, 2};
    final double[] y = new double[] {1, 1, 2};
    final Hull2d hull = KnnConcaveHull2d.create(x, y);
    Assertions.assertEquals(3, hull.getNumberOfVertices());
    Assertions.assertEquals(2 + Math.sqrt(2), hull.getLength(), 1e-10);
    Assertions.assertEquals(0.5, hull.getArea(), 1e-10);
    Assertions.assertArrayEquals(new double[] {5.0 / 3, 4.0 / 3}, hull.getCentroid());
  }

  @Test
  void canCreateWithMultiplePointsCircular() {
    // This test depends on the output from the UnitDiskSampler and is not robust to
    // using any seed.
    final UnitBallSampler sampler = UnitBallSampler.of(RngFactory.create(12345L), 2);
    final int n = 500;
    final DoubleArrayList xx = new DoubleArrayList(n);
    final DoubleArrayList yy = new DoubleArrayList(n);
    for (int i = 0; i < n; i++) {
      final double[] p = sampler.sample();
      xx.add(p[0]);
      yy.add(p[1]);
    }
    final Hull2d hull =
        KnnConcaveHull2d.create(15, xx.toDoubleArray(), yy.toDoubleArray(), xx.size());
    Assertions.assertNotNull(hull);
    // Deltas are high as the concave hull may be much smaller than the enclosing circle
    // with a longer perimeter
    Assertions.assertEquals(2 * Math.PI, hull.getLength(), 0.3);
    final double area = hull.getArea();
    Assertions.assertTrue(area <= Math.PI);
    Assertions.assertEquals(Math.PI, area, 0.4);
    Assertions.assertArrayEquals(new double[] {0, 0}, hull.getCentroid(), 0.01);
  }

  /**
   * Test the edge case where the number of neighbours is too low to bridge a large gap to points
   * that should be inside the hull.
   */
  @Test
  void canCreateWithSeparatedClusters() {
    final double[] x = new double[] {0, 0, 1, 1, 0.5, 0.5, 0.5};
    final double[] y = new double[] {0, 1, 1, 0, 0.5, -0.5, 3};
    final Hull2d hull = KnnConcaveHull2d.create(x, y);
    Assertions.assertNotNull(hull);
    // Expected to draw a 1x1 square + 1x0.5 triangle + 1x2 triangle
    Assertions.assertEquals(2 + 2 * Math.hypot(0.5, 0.5) + 2 * Math.hypot(0.5, 2),
        hull.getLength());
    Assertions.assertEquals(1 + 0.25 + 1, hull.getArea());
  }

  /**
   * Test the edge case where some points are colinear but due to ordering the closer points are
   * ignored in favour of the further points. Thus the final hull has some 'internal' points that
   * that are on the boundary (are colinear with hull points) and test as outside the hull.
   */
  @Test
  void canCreateWithColinearPoints() {
    final double[] x = new double[] {0, 0, 1, 1, 3, 3};
    final double[] y = new double[] {0, 1, 1, 0, 0, 1};
    final Hull2d hull = KnnConcaveHull2d.create(x, y);
    Assertions.assertNotNull(hull);
    // Expected to draw a 3x1 square
    Assertions.assertEquals(8, hull.getLength());
    Assertions.assertEquals(3, hull.getArea());
  }
}
