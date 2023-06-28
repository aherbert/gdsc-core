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
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.sampling.CompositeSamplers;
import org.apache.commons.rng.sampling.ObjectSampler;
import org.apache.commons.rng.sampling.shape.TriangleSampler;
import org.apache.commons.rng.sampling.shape.UnitBallSampler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.test.rng.RngFactory;

@SuppressWarnings({"javadoc"})
class ConvexHull2dTest {
  @Test
  void cannotComputeConvexHullFromNoCoords() {
    final double[] x = new double[] {};
    final double[] y = new double[] {};
    final Hull2d hull = ConvexHull2d.create(x, y);
    Assertions.assertNull(hull);
  }

  @Test
  void canComputeConvexHullFromSquare() {
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
      final Hull2d hull = ConvexHull2d.create(x, y);
      check(ex, ey, hull);
    }
  }

  @Test
  void canComputeConvexHullFromSquareWithInternalPoint() {
    final double[] x = new double[] {0, 0, 10, 10, 5};
    final double[] y = new double[] {0, 10, 10, 0, 5};
    final double[] ex = new double[] {0, 10, 10, 0};
    final double[] ey = new double[] {0, 0, 10, 10};
    final Hull2d hull = ConvexHull2d.create(x, y);
    check(ex, ey, hull);
  }

  @Test
  void canComputeConvexHullFromSquareWithInternalPoint2() {
    final double[] x = new double[] {0, 0, 5, 10, 10};
    final double[] y = new double[] {0, 10, 5, 10, 0};
    final double[] ex = new double[] {0, 10, 10, 0};
    final double[] ey = new double[] {0, 0, 10, 10};
    final Hull2d hull = ConvexHull2d.create(x, y);
    check(ex, ey, hull);
  }

  private static void check(double[] ex, double[] ey, Hull2d hull) {
    if (ex == null) {
      Assertions.assertTrue(hull == null);
      return;
    }
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
    Assertions.assertNull(ConvexHull2d.newBuilder().build());
  }

  @Test
  void canBuildWithOnePoint() {
    final double[] x = new double[] {1.2345, 6.78};
    final Hull2d hull = ConvexHull2d.newBuilder().add(x).build();
    Assertions.assertEquals(1, hull.getNumberOfVertices());
    Assertions.assertEquals(2, hull.dimensions());
    Assertions.assertEquals(0, hull.getLength());
    Assertions.assertEquals(0, hull.getArea());
    Assertions.assertArrayEquals(new double[] {x[0], x[1]}, hull.getCentroid());
  }

  @Test
  void canClearBuilder() {
    final ConvexHull2d.Builder builder = ConvexHull2d.newBuilder();
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
    Assertions.assertNull(ConvexHull2d.create(x, x));
  }

  @Test
  void canCreateWithOnePoint() {
    final double[] x = new double[] {1.2345f};
    final Hull2d hull = ConvexHull2d.create(x, x);
    Assertions.assertEquals(1, hull.getNumberOfVertices());
    Assertions.assertEquals(0, hull.getLength());
    Assertions.assertEquals(0, hull.getArea());
    Assertions.assertArrayEquals(new double[] {x[0], x[0]}, hull.getCentroid());
  }

  @Test
  void canCreateWithTwoPoints() {
    final double[] x = new double[] {1.5f, 2.5f};
    final Hull2d hull = ConvexHull2d.create(x, x);
    Assertions.assertEquals(2, hull.getNumberOfVertices());
    Assertions.assertEquals(2 * Math.sqrt(2), hull.getLength(), 1e-10);
    Assertions.assertEquals(0, hull.getArea());
    Assertions.assertArrayEquals(new double[] {2, 2}, hull.getCentroid());
  }

  @Test
  void canCreateWithThreePoints() {
    final double[] x = new double[] {1, 2, 2};
    final double[] y = new double[] {1, 1, 2};
    final Hull2d hull = ConvexHull2d.create(x, y);
    Assertions.assertEquals(3, hull.getNumberOfVertices());
    Assertions.assertEquals(2 + Math.sqrt(2), hull.getLength(), 1e-10);
    Assertions.assertEquals(0.5, hull.getArea(), 1e-10);
    Assertions.assertArrayEquals(new double[] {5.0 / 3, 4.0 / 3}, hull.getCentroid());
  }

  @Test
  void canCreateWithManyPoints() {
    // This test depends on the output from the UnitDiskSampler and is not robust to
    // using any seed.
    final UnitBallSampler sampler = UnitBallSampler.of(RngFactory.create(12345L), 2);
    final int n = 500;
    final DoubleArrayList xx = new DoubleArrayList(n);
    final DoubleArrayList yy = new DoubleArrayList(n);
    final double[] centroid = {1, -2};
    for (int i = 0; i < n; i++) {
      final double[] p = sampler.sample();
      xx.add(p[0] + centroid[0]);
      yy.add(p[1] + centroid[1]);
    }
    final Hull2d hull = ConvexHull2d.create(xx.toDoubleArray(), yy.toDoubleArray());
    Assertions.assertEquals(2 * Math.PI, hull.getLength(), 0.2);
    Assertions.assertEquals(Math.PI, hull.getArea(), 0.2);
    Assertions.assertArrayEquals(centroid, hull.getCentroid(), 0.01);
  }

  @Test
  void canCreateWithManyPointsHexagon() {
    // This test depends on the output from the sampler and is not robust to
    // using any seed.
    final UniformRandomProvider rng = RngFactory.create(12345L);
    // Height and width of half an equilateral triangle with edge length 1
    final double h = Math.sqrt(0.75);
    final double w = 0.5;
    final double[] a = {-1, 0};
    final double[] b = {-w, h};
    final double[] c = {w, h};
    final double[] d = {1, 0};
    final double[] e = {w, -h};
    final double[] f = {-w, -h};
    final double[] o = {0, 0};

    final ObjectSampler<double[]> sampler = CompositeSamplers.<double[]>newObjectSamplerBuilder()
        .add(TriangleSampler.of(rng, a, b, o), 1).add(TriangleSampler.of(rng, b, c, o), 1)
        .add(TriangleSampler.of(rng, c, d, o), 1).add(TriangleSampler.of(rng, d, e, o), 1)
        .add(TriangleSampler.of(rng, e, f, o), 1).add(TriangleSampler.of(rng, f, a, o), 1)
        .build(rng);
    final int n = 5000;
    final DoubleArrayList xx = new DoubleArrayList(n);
    final DoubleArrayList yy = new DoubleArrayList(n);
    final double[] centroid = {1, -2};
    for (int i = 0; i < n; i++) {
      final double[] p = sampler.sample();
      xx.add(p[0] + centroid[0]);
      yy.add(p[1] + centroid[1]);
    }
    final Hull2d hull = ConvexHull2d.create(xx.toDoubleArray(), yy.toDoubleArray());
    Assertions.assertEquals(6, hull.getLength(), 0.2);
    Assertions.assertEquals(6 * h * w, hull.getArea(), 0.2);
    Assertions.assertArrayEquals(centroid, hull.getCentroid(), 0.01);
  }
}
