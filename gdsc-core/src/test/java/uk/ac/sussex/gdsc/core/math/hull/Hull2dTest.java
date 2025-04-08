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
 * Copyright (C) 2011 - 2025 Alex Herbert
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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.core.utils.SimpleArrayUtils;

@SuppressWarnings({"javadoc"})
class Hull2dTest {
  @Test
  void testCreateThrows() {
    final double[] x = {0, 1, 2};
    final double[] y = {};
    Assertions.assertThrows(NullPointerException.class, () -> Hull2d.create(null, x));
    Assertions.assertThrows(NullPointerException.class, () -> Hull2d.create(x, null));
    Assertions.assertThrows(IllegalArgumentException.class, () -> Hull2d.create(x, new double[1]));
    Assertions.assertThrows(IllegalArgumentException.class, () -> Hull2d.create(y, y));
  }

  @Test
  void canCreateWithOnePoint() {
    final double[] x = {1.2345};
    final Hull2d hull = Hull2d.create(x, x);
    Assertions.assertEquals(2, hull.dimensions());
    Assertions.assertEquals(1, hull.getNumberOfVertices());
    Assertions.assertArrayEquals(new double[][] {{x[0], x[0]}}, hull.getVertices());
    Assertions.assertEquals(0, hull.getLength());
    Assertions.assertEquals(0, hull.getArea());
    Assertions.assertArrayEquals(new double[] {x[0], x[0]}, hull.getCentroid());
  }

  @Test
  void canCreateWithTwoPoints() {
    final double[] x = {1.5, 2.5};
    final Hull2d hull = Hull2d.create(x, x);
    Assertions.assertEquals(2, hull.dimensions());
    Assertions.assertEquals(2, hull.getNumberOfVertices());
    Assertions.assertArrayEquals(new double[][] {{x[0], x[0]}, {x[1], x[1]}}, hull.getVertices());
    Assertions.assertEquals(2 * Math.sqrt(2), hull.getLength(), 1e-10);
    Assertions.assertEquals(0, hull.getArea());
    Assertions.assertArrayEquals(new double[] {2, 2}, hull.getCentroid());
  }

  @Test
  void canCreateWithThreePoints() {
    final double[] x = {1, 2, 2};
    final double[] y = {1, 1, 2};
    final Hull2d hull = Hull2d.create(x, y);
    Assertions.assertEquals(2, hull.dimensions());
    Assertions.assertEquals(3, hull.getNumberOfVertices());
    Assertions.assertArrayEquals(new double[][] {{x[0], y[0]}, {x[1], y[1]}, {x[2], y[2]}},
        hull.getVertices());
    Assertions.assertEquals(2 + Math.sqrt(2), hull.getLength(), 1e-10);
    Assertions.assertEquals(0.5, hull.getArea(), 1e-10);
    Assertions.assertArrayEquals(new double[] {5.0 / 3, 4.0 / 3}, hull.getCentroid());
  }

  @Test
  void canGetCentroid() {
    final double[] x = {1, 2, 2};
    final double[] y = {1, 1, 2};
    final double[] centroid = {5.0 / 3, 4.0 / 3};
    for (int i = 0; i <= 1; i++) {
      final double[] x2 = x.clone();
      SimpleArrayUtils.add(x2, i);
      for (int j = 0; j <= 1; j++) {
        final double[] y2 = y.clone();
        SimpleArrayUtils.add(y2, -j);
        final double[] centroid2 = new double[] {centroid[0] + i, centroid[1] - j};
        final Hull2d hull = Hull2d.create(x2, y2);
        // Test repeat calls
        final double[] c = hull.getCentroid();
        final double[] c2 = hull.getCentroid();
        Assertions.assertArrayEquals(centroid2, c, 1e-10);
        Assertions.assertArrayEquals(c, c2);
        Assertions.assertNotSame(c, c2);
      }
    }
  }

  @Test
  void canGetLength() {
    final double[] x = {1, 2, 2};
    final double[] y = {1, 1, 2};
    final Hull2d hull = Hull2d.create(x, y);
    final double length = 1 + 1 + Math.sqrt(2);
    // Test repeat calls
    Assertions.assertEquals(length, hull.getLength());
    Assertions.assertEquals(length, hull.getLength());
  }

  @Test
  void canGetArea() {
    final double[] x = {1, 2, 2};
    final double[] y = {1, 1, 2};
    final Hull2d hull = Hull2d.create(x, y);
    final double area = 0.5;
    // Test repeat calls
    Assertions.assertEquals(area, hull.getArea());
    Assertions.assertEquals(area, hull.getArea());
  }

  @Test
  void testCreateSingleArrayThrows() {
    Assertions.assertThrows(NullPointerException.class, () -> Hull2d.create(null));
    Assertions.assertThrows(IllegalArgumentException.class, () -> Hull2d.create(new double[0][2]));
    Assertions.assertThrows(IndexOutOfBoundsException.class, () -> Hull2d.create(new double[1][1]));
  }

  @Test
  void canCreateSingleArrayWithOnePoint() {
    final double[][] c = {{1.2345, 6.78}};
    final Hull2d hull = Hull2d.create(c);
    Assertions.assertEquals(2, hull.dimensions());
    Assertions.assertEquals(1, hull.getNumberOfVertices());
    Assertions.assertArrayEquals(c, hull.getVertices());
    Assertions.assertEquals(0, hull.getLength());
    Assertions.assertEquals(0, hull.getArea());
    Assertions.assertArrayEquals(c[0], hull.getCentroid());
  }

  @Test
  void canCreateSingleArrayWithTwoPoints() {
    final double[][] c = {{1.5, 1.5}, {2.5, 2.5}};
    final Hull2d hull = Hull2d.create(c);
    Assertions.assertEquals(2, hull.dimensions());
    Assertions.assertEquals(2, hull.getNumberOfVertices());
    Assertions.assertArrayEquals(c, hull.getVertices());
    Assertions.assertEquals(2 * Math.sqrt(2), hull.getLength(), 1e-10);
    Assertions.assertEquals(0, hull.getArea());
    Assertions.assertArrayEquals(new double[] {2, 2}, hull.getCentroid());
  }

  @Test
  void canCreateSingleArrayWithThreePoints() {
    final double[][] c = {{1, 1}, {2, 1}, {2, 2}};
    final Hull2d hull = Hull2d.create(c);
    Assertions.assertEquals(2, hull.dimensions());
    Assertions.assertEquals(3, hull.getNumberOfVertices());
    Assertions.assertArrayEquals(c, hull.getVertices());
    Assertions.assertEquals(2 + Math.sqrt(2), hull.getLength(), 1e-10);
    Assertions.assertEquals(0.5, hull.getArea(), 1e-10);
    Assertions.assertArrayEquals(new double[] {5.0 / 3, 4.0 / 3}, hull.getCentroid());
  }

  @Test
  void canComputeCentroidLengthAndArea() {
    // Parallelogram
    double[] xvalues = {0, 10, 11, 1};
    double[] yvalues = {0, 0, 10, 10};
    Hull2d hull = Hull2d.create(xvalues, yvalues);
    Assertions.assertEquals(2 * 10 + 2 * Math.sqrt(1 * 1 + 10 * 10), hull.getLength(), 1e-6);
    Assertions.assertEquals(100, hull.getArea(), 1e-6);
    Assertions.assertArrayEquals(new double[] {5.5, 5}, hull.getCentroid());

    // Rotated square
    xvalues = new double[] {0, 10, 9, -1};
    yvalues = new double[] {0, 1, 11, 10};
    hull = Hull2d.create(xvalues, yvalues);
    final double edgeLengthSquared = 1 * 1 + 10 * 10;
    Assertions.assertEquals(4 * Math.sqrt(edgeLengthSquared), hull.getLength(), 1e-6);
    Assertions.assertEquals(edgeLengthSquared, hull.getArea(), 1e-6);
    Assertions.assertArrayEquals(new double[] {9.0 / 2, 11.0 / 2}, hull.getCentroid());

    // Polygon circle
    final int n = 1000;
    final double radius = 4;
    xvalues = new double[n];
    yvalues = new double[n];
    for (int i = 0; i < 1000; i++) {
      final double a = i * 2 * Math.PI / n;
      xvalues[i] = Math.sin(a) * radius;
      yvalues[i] = Math.cos(a) * radius;
    }
    hull = Hull2d.create(xvalues, yvalues);
    Assertions.assertEquals(2 * Math.PI * radius, hull.getLength(), 1e-2);
    Assertions.assertEquals(Math.PI * radius * radius, hull.getArea(), 1e-2);
    Assertions.assertArrayEquals(new double[] {0, 0}, hull.getCentroid(), 1e-2);
  }

  @Test
  void conComputeContains() {
    final double[] x = {0, 10, 11, 1};
    final double[] y = {0, 0, 10, 10};
    final Hull2d hull = Hull2d.create(x, y);
    // Contains does not match outer bounds on right or bottom
    Assertions.assertTrue(hull.contains(new double[] {x[0], y[0]}));
    for (int i = 1; i < x.length; i++) {
      Assertions.assertFalse(hull.contains(new double[] {x[i], y[i]}));
    }
    Assertions.assertTrue(hull.contains(new double[] {5, 5}));
    Assertions.assertFalse(hull.contains(new double[] {-5, 5}));
    Assertions.assertFalse(hull.contains(new double[] {5, -5}));
  }
}
