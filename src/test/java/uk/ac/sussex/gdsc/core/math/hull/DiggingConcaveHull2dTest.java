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
import uk.ac.sussex.gdsc.core.math.GeometryUtils;
import uk.ac.sussex.gdsc.core.utils.rng.UnitCircleSampler;
import uk.ac.sussex.gdsc.test.rng.RngUtils;
import uk.ac.sussex.gdsc.test.utils.functions.IndexSupplier;

@SuppressWarnings({"javadoc"})
class DiggingConcaveHull2dTest {

  @Test
  void testNumberOfNeighbours() {
    final DiggingConcaveHull2d.Builder builder = DiggingConcaveHull2d.newBuilder();
    Assertions.assertTrue(builder.getThreshold() >= 1);
    Assertions.assertSame(builder, builder.setThreshold(7));
    Assertions.assertEquals(7, builder.getThreshold());
    Assertions.assertThrows(IllegalArgumentException.class, () -> builder.setThreshold(0));
  }

  @Test
  void cannotComputeKnnConcaveHullFromNoCoords() {
    final double[] x = new double[] {};
    final double[] y = new double[] {};
    final Hull2d hull = DiggingConcaveHull2d.create(1.0, x, y);
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
      final Hull2d hull = DiggingConcaveHull2d.create(1.0, x, y);
      check(ex, ey, hull);
    }
  }

  @Test
  void canComputeKnnConcaveHullFromSquareWithInternalPoint() {
    final double[] x = new double[] {0, 0, 10, 10, 5};
    final double[] y = new double[] {0, 10, 10, 0, 5};
    final double[] ex = new double[] {0, 10, 10, 0};
    final double[] ey = new double[] {0, 0, 10, 10};
    final Hull2d hull = DiggingConcaveHull2d.create(2.0, x, y);
    check(ex, ey, hull);
  }

  @Test
  void canComputeKnnConcaveHullFromSquareWithInternalPoint2() {
    final double[] x = new double[] {0, 0, 5, 10, 10};
    final double[] y = new double[] {0, 10, 5, 10, 0};
    final double[] ex = new double[] {0, 10, 10, 0};
    final double[] ey = new double[] {0, 0, 10, 10};
    final Hull2d hull = DiggingConcaveHull2d.create(2.0, x, y);
    check(ex, ey, hull);
  }

  @Test
  void canComputeKnnConcaveHullFromSquareWithInternalPoint3() {
    final double[] x = new double[] {0, 0, 4, 10, 10};
    final double[] y = new double[] {0, 10, 1, 10, 0};
    final double[] ex = new double[] {0, 4, 10, 10, 0};
    final double[] ey = new double[] {0, 1, 0, 10, 10};
    final Hull2d hull = DiggingConcaveHull2d.create(0.5, x, y);
    check(ex, ey, hull);
  }

  @Test
  void canComputeKnnConcaveHullFromSquareWithInternalPoint4() {
    final double[] x = new double[] {0, 0, 6, 10, 10};
    final double[] y = new double[] {0, 10, 1, 10, 0};
    final double[] ex = new double[] {0, 6, 10, 10, 0};
    final double[] ey = new double[] {0, 1, 0, 10, 10};
    final Hull2d hull = DiggingConcaveHull2d.create(0.5, x, y);
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

    final IndexSupplier msgX = new IndexSupplier(1).setMessagePrefix("x ");
    final IndexSupplier msgY = new IndexSupplier(1).setMessagePrefix("y ");
    for (int i = 0; i < n; i++) {
      Assertions.assertEquals(ex[i], points[i][0], msgX.set(0, i));
      Assertions.assertEquals(ey[i], points[i][1], msgY.set(0, i));
    }
  }

  @Test
  void canBuildWithNoPoints() {
    Assertions.assertNull(DiggingConcaveHull2d.newBuilder().build());
  }

  @Test
  void canBuildWithOnePoint() {
    final double[] x = new double[] {1.2345, 6.78};
    final Hull2d hull = DiggingConcaveHull2d.newBuilder().add(x).build();
    Assertions.assertEquals(1, hull.getNumberOfVertices());
    Assertions.assertEquals(2, hull.dimensions());
    Assertions.assertEquals(0, hull.getLength());
    Assertions.assertEquals(0, hull.getArea());
    Assertions.assertArrayEquals(new double[] {x[0], x[1]}, hull.getCentroid());
  }

  @Test
  void canClearBuilder() {
    final DiggingConcaveHull2d.Builder builder = DiggingConcaveHull2d.newBuilder();
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
    Assertions.assertNull(DiggingConcaveHull2d.create(1.0, x, x));
  }

  @Test
  void canCreateWithOnePoint() {
    final double[] x = new double[] {1.2345f};
    final Hull2d hull = DiggingConcaveHull2d.create(1.0, x, x);
    Assertions.assertEquals(1, hull.getNumberOfVertices());
    Assertions.assertEquals(0, hull.getLength());
    Assertions.assertEquals(0, hull.getArea());
    Assertions.assertArrayEquals(new double[] {x[0], x[0]}, hull.getCentroid());
  }

  @Test
  void canCreateWithTwoPoints() {
    final double[] x = new double[] {1.5f, 2.5f};
    final Hull2d hull = DiggingConcaveHull2d.create(1.0, x, x);
    Assertions.assertEquals(2, hull.getNumberOfVertices());
    Assertions.assertEquals(2 * Math.sqrt(2), hull.getLength(), 1e-10);
    Assertions.assertEquals(0, hull.getArea());
    Assertions.assertArrayEquals(new double[] {2, 2}, hull.getCentroid());
  }

  @Test
  void canCreateWithThreePoints() {
    final double[] x = new double[] {1, 2, 2};
    final double[] y = new double[] {1, 1, 2};
    final Hull2d hull = DiggingConcaveHull2d.create(1.0, x, y);
    Assertions.assertEquals(3, hull.getNumberOfVertices());
    Assertions.assertEquals(2 + Math.sqrt(2), hull.getLength(), 1e-10);
    Assertions.assertEquals(0.5, hull.getArea(), 1e-10);
    Assertions.assertArrayEquals(new double[] {5.0 / 3, 4.0 / 3}, hull.getCentroid());
  }

  @Test
  void canCreateWithMultiplePointsCircular() {
    final UnitCircleSampler sampler = UnitCircleSampler.of(RngUtils.create(126487618L));
    final int n = 500;
    final TDoubleArrayList xx = new TDoubleArrayList(n);
    final TDoubleArrayList yy = new TDoubleArrayList(n);
    for (int i = 0; i < n; i++) {
      final double[] p = sampler.sample();
      xx.add(p[0]);
      yy.add(p[1]);
    }
    // Require a high threshold to prevent digging in to the circle.
    final Hull2d hull = DiggingConcaveHull2d.create(10.0, xx.toArray(), yy.toArray(), xx.size());
    Assertions.assertNotNull(hull);
    // Deltas are high as the concave hull may be much smaller than the enclosing circle
    // with a longer perimeter
    Assertions.assertEquals(2 * Math.PI, hull.getLength(), 0.3);
    final double area = hull.getArea();
    Assertions.assertTrue(area <= Math.PI);
    Assertions.assertEquals(Math.PI, area, 0.4);
    Assertions.assertArrayEquals(new double[] {0, 0}, hull.getCentroid(), 0.01);
  }

  @Test
  void canCreateWithMultiplePointsCircularMinusWedge() {
    final UnitCircleSampler sampler = UnitCircleSampler.of(RngUtils.create(126487618L));
    final int n = 500;
    final TDoubleArrayList xx = new TDoubleArrayList(n);
    final TDoubleArrayList yy = new TDoubleArrayList(n);
    while (xx.size() < n) {
      final double[] p = sampler.sample();
      // Cut out an entire sector of the circle
      if (p[0] > 0.0 && p[1] > 0.0) {
        continue;
      }
      xx.add(p[0]);
      yy.add(p[1]);
    }
    // Require a high threshold to prevent digging in to the circle.
    final Hull2d hull = DiggingConcaveHull2d.create(2.0, xx.toArray(), yy.toArray(), xx.size());
    Assertions.assertNotNull(hull);
    // Deltas are high as the concave hull may be much smaller than the enclosing circle
    // with a longer perimeter
    Assertions.assertEquals(2 + 1.5 * Math.PI, hull.getLength(), 0.3);
    final double area = hull.getArea();
    Assertions.assertEquals(0.75 * Math.PI, area, 0.3);
  }

  /**
   * Test the edge case where some points are colinear.
   */
  @Test
  void canCreateWithColinearPoints() {
    final double[] x = new double[] {0, 0, 1, 1, 3, 3};
    final double[] y = new double[] {0, 1, 1, 0, 0, 1};
    final Hull2d hull = DiggingConcaveHull2d.create(2.0, x, y);
    Assertions.assertNotNull(hull);
    // Expected to draw a 3x1 square
    Assertions.assertEquals(8, hull.getLength());
    Assertions.assertEquals(3, hull.getArea());
  }

  /**
   * Test that polygons are simple (non-self intersecting).
   */
  @Test
  void canCreateSimplePolygon() {
    // Simple square.
    // 2 internal points.
    // Assuming the hull starts at (0,0) and proceeds counter clockwise
    // - drop (0,0 - 10,0) for (0,0 - 2,1) and (2,1 - 10,0)
    // - drop (0,10 - 0,0) for (0,10 - 4,1) and (4,1 - 0,0)
    // Lines will intersect: (2,1 - 10,0) and (4,1 - 0,0)
    // Requires careful threshold to force correct digging.
    //
    // Ideally the second connection should be detected to cause an intersect.
    // The correct hull is:
    // 0,0 - 2,1 - 4,1 - 10,0 - 10,10 - 0,10
    final double[] x = new double[] {0, 10, 10, 0, 2, 4};
    final double[] y = new double[] {0, 0, 10, 10, 1, 1};
    final Hull2d hull = DiggingConcaveHull2d.create(1.0, x, y);
    Assertions.assertNotNull(hull);
    final double[][] vertices = hull.getVertices();
    for (int i = 0, j = vertices.length - 1; i < vertices.length; j = i, i++) {
      final double x1 = vertices[i][0];
      final double y1 = vertices[i][1];
      final double x2 = vertices[j][0];
      final double y2 = vertices[j][1];
      for (int ii = 0, jj = vertices.length - 1; ii < vertices.length; jj = ii, ii++) {
        // Ignore touching vertices
        if (ii == i || ii == j || jj == i || jj == j) {
          continue;
        }
        final double x3 = vertices[ii][0];
        final double y3 = vertices[ii][1];
        final double x4 = vertices[jj][0];
        final double y4 = vertices[jj][1];
        Assertions.assertFalse(GeometryUtils.testIntersect(x1, y1, x2, y2, x3, y3, x4, y4));
      }
    }
  }
}
