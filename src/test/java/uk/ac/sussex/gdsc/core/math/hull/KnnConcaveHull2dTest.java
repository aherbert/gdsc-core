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
 * Copyright (C) 2011 - 2020 Alex Herbert
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

import gnu.trove.list.array.TDoubleArrayList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.core.math.hull.KnnConcaveHull2d.ActiveList;
import uk.ac.sussex.gdsc.core.math.hull.KnnConcaveHull2d.AngleList;
import uk.ac.sussex.gdsc.core.utils.rng.UnitCircleSampler;
import uk.ac.sussex.gdsc.test.rng.RngUtils;

@SuppressWarnings({"javadoc"})
public class KnnConcaveHull2dTest {
  @Test
  public void testActiveList() {
    final int n = 5;
    final ActiveList active = new ActiveList(n);
    Assertions.assertEquals(0, active.size());
    for (int i = 0; i < n; i++) {
      Assertions.assertFalse(active.isEnabled(i));
    }
    active.enableAll();
    for (int i = 0; i < n; i++) {
      Assertions.assertTrue(active.isEnabled(i));
    }
    Assertions.assertEquals(n, active.size());
    int index = 2;
    active.disable(index);
    Assertions.assertEquals(n - 1, active.size());
    Assertions.assertFalse(active.isEnabled(index));
    active.enable(index);
    Assertions.assertEquals(n, active.size());
    Assertions.assertTrue(active.isEnabled(index));
  }

  @Test
  public void testClockwiseTurns() {
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
  public void testAngleList() {
    final double[][] points = {{0, 0}, {1, 0}, {1, 1}, {0, 1}, {0.5, 0.5}};
    AngleList knn = new AngleList(4);
    for (int i = 0; i < 4; i++) {
      knn.add(i);
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

  private static int[] toArray(AngleList knn) {
    final int[] list = new int[knn.size()];
    for (int i = 0; i < knn.size(); i++) {
      list[i] = knn.getIndex(i);
    }
    return list;
  }

  @Test
  public void testNumberOfNeighbours() {
    KnnConcaveHull2d.Builder builder = KnnConcaveHull2d.newBuilder();
    Assertions.assertEquals(3, builder.getK());
    Assertions.assertSame(builder, builder.setK(7));
    Assertions.assertEquals(7, builder.getK());
    builder.setK(0);
    Assertions.assertEquals(3, builder.getK());
  }

  @Test
  public void cannotComputeKnnConcaveHullFromNoCoords() {
    final double[] x = new double[] {};
    final double[] y = new double[] {};
    final Hull2d hull = KnnConcaveHull2d.create(x, y);
    Assertions.assertNull(hull);
  }

  @Test
  public void canComputeKnnConcaveHullFromSquare() {
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
  public void canComputeKnnConcaveHullFromSquareWithInternalPoint() {
    final double[] x = new double[] {0, 0, 10, 10, 5};
    final double[] y = new double[] {0, 10, 10, 0, 5};
    final double[] ex = new double[] {0, 10, 10, 0};
    final double[] ey = new double[] {0, 0, 10, 10};
    final Hull2d hull = KnnConcaveHull2d.create(x, y);
    check(ex, ey, hull);
  }

  @Test
  public void canComputeKnnConcaveHullFromSquareWithInternalPoint2() {
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
  public void canBuildWithNoPoints() {
    Assertions.assertNull(KnnConcaveHull2d.newBuilder().build());
  }

  @Test
  public void canBuildWithOnePoint() {
    final double[] x = new double[] {1.2345, 6.78};
    final Hull2d hull = KnnConcaveHull2d.newBuilder().add(x).build();
    Assertions.assertEquals(1, hull.getNumberOfVertices());
    Assertions.assertEquals(2, hull.dimensions());
    Assertions.assertEquals(0, hull.getLength());
    Assertions.assertEquals(0, hull.getArea());
  }

  @Test
  public void canClearBuilder() {
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
  public void canCreateWithNoPoints() {
    final double[] x = new double[0];
    Assertions.assertNull(KnnConcaveHull2d.create(x, x));
  }

  @Test
  public void canCreateWithOnePoint() {
    final double[] x = new double[] {1.2345f};
    final Hull2d hull = KnnConcaveHull2d.create(x, x);
    Assertions.assertEquals(1, hull.getNumberOfVertices());
    Assertions.assertEquals(0, hull.getLength());
    Assertions.assertEquals(0, hull.getArea());
  }

  @Test
  public void canCreateWithTwoPoints() {
    final double[] x = new double[] {1.5f, 2.5f};
    final Hull2d hull = KnnConcaveHull2d.create(x, x);
    Assertions.assertEquals(2, hull.getNumberOfVertices());
    Assertions.assertEquals(2 * Math.sqrt(2), hull.getLength(), 1e-10);
    Assertions.assertEquals(0, hull.getArea());
  }

  @Test
  public void canCreateWithThreePoints() {
    final double[] x = new double[] {1, 2, 2};
    final double[] y = new double[] {1, 1, 2};
    final Hull2d hull = KnnConcaveHull2d.create(x, y);
    Assertions.assertEquals(3, hull.getNumberOfVertices());
    Assertions.assertEquals(2 + Math.sqrt(2), hull.getLength(), 1e-10);
    Assertions.assertEquals(0.5, hull.getArea(), 1e-10);
  }

  @Test
  public void canCreateWithManyPoints() {
    final UnitCircleSampler sampler = UnitCircleSampler.of(RngUtils.create(126487618L));
    final int n = 500;
    final TDoubleArrayList xx = new TDoubleArrayList(n);
    final TDoubleArrayList yy = new TDoubleArrayList(n);
    for (int i = 0; i < n; i++) {
      final double[] p = sampler.sample();
      xx.add(p[0]);
      yy.add(p[1]);
    }
    final Hull2d hull = KnnConcaveHull2d.create(5, xx.toArray(), yy.toArray(), xx.size());
    Assertions.assertNotNull(hull);
    // Deltas are high as the concave hull may be much smaller than the enclosing circle
    // with a longer perimeter
    Assertions.assertEquals(2 * Math.PI, hull.getLength(), 0.3);
    final double area = hull.getArea();
    Assertions.assertTrue(area <= Math.PI);
    Assertions.assertEquals(Math.PI, area, 0.4);
  }

  /**
   * Test the edge case where the number of neighbours is too low to bridge a large gap to points
   * that should be inside the hull.
   */
  @Test
  public void canCreateWithSeparatedClusters() {
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
  public void canCreateWithColinearPoints() {
    final double[] x = new double[] {0, 0, 1, 1, 3, 3};
    final double[] y = new double[] {0, 1, 1, 0, 0, 1};
    final Hull2d hull = KnnConcaveHull2d.create(x, y);
    Assertions.assertNotNull(hull);
    // Expected to draw a 3x1 square
    Assertions.assertEquals(8, hull.getLength());
    Assertions.assertEquals(3, hull.getArea());
  }
}
