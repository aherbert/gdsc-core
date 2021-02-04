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

import java.util.Arrays;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.core.utils.SimpleArrayUtils;

@SuppressWarnings({"javadoc"})
class Hull3dTest {
  @Test
  void testCreateThrows() {
    // Tetrahedron
    final double[][] v = {{1, 1, 1}, {1, -1, -1}, {-1, 1, -1}, {-1, -1, 1}};
    final int[][] f = {{0, 1, 2}, {0, 3, 1}, {3, 2, 1}, {3, 0, 2}};
    Assertions.assertThrows(NullPointerException.class, () -> Hull3d.create(null, f));
    Assertions.assertThrows(NullPointerException.class, () -> Hull3d.create(v, null));
    // Not enough vertices
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> Hull3d.create(Arrays.copyOf(v, 3), f));
    // Not enough faces
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> Hull3d.create(v, Arrays.copyOf(f, 3)));
    // Too many faces
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> Hull3d.create(v, Arrays.copyOf(f, 5)));
    // Coordinates less than 3
    final double[][] v2 = v.clone();
    v2[0] = Arrays.copyOf(v[0], 2);
    Assertions.assertThrows(IllegalArgumentException.class, () -> Hull3d.create(v2, f));
    // Face less than 3
    final int[][] f2 = f.clone();
    f2[0] = Arrays.copyOf(f[0], 2);
    Assertions.assertThrows(IllegalArgumentException.class, () -> Hull3d.create(v, f2));
  }

  @Test
  void testCreateThrowsWithUnpairedEdges() {
    // Tetrahedron
    final double[][] v = {{1, 1, 1}, {1, -1, -1}, {-1, 1, -1}, {-1, -1, 1}};
    final int[][] f = {{0, 1, 2}, {0, 3, 1}, {3, 2, 1}, {3, 0, 2}};
    Assertions.assertNotNull(Hull3d.create(v, f));
    for (int i = 0; i < f.length; i++) {
      SimpleArrayUtils.swap(f[0], 0, 1);
      Assertions.assertThrows(IllegalArgumentException.class, () -> Hull3d.create(v, f));
      SimpleArrayUtils.swap(f[0], 0, 1);
    }
  }

  @Test
  void canGetCentroid() {
    // Octahedron
    final double[][] v = {{1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}};
    final int[][] f =
        {{0, 4, 3}, {3, 4, 1}, {1, 4, 2}, {2, 4, 0}, {3, 5, 0}, {1, 5, 3}, {2, 5, 1}, {0, 5, 2}};
    final double[] centroid = {0, 0, 0};
    Hull3d hull = Hull3d.create(v, f);
    // Test repeat calls
    final double[] c1 = hull.getCentroid();
    final double[] c2 = hull.getCentroid();
    Assertions.assertArrayEquals(centroid, c1, 1e-10);
    Assertions.assertArrayEquals(c1, c2);
    Assertions.assertNotSame(c1, c2);

    // Shift
    for (final double[] shift : new double[][] {{1, 2, -3}, {-5, -6, 17}}) {
      for (final double[] c : v) {
        c[0] += shift[0];
        c[1] += shift[1];
        c[2] += shift[2];
      }
      centroid[0] += shift[0];
      centroid[1] += shift[1];
      centroid[2] += shift[2];
      hull = Hull3d.create(v, f);
      Assertions.assertArrayEquals(centroid, hull.getCentroid(), 1e-10);
    }
  }

  @Test
  void canGetArea() {
    // Octahedron
    final double[][] v = {{1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}};
    final int[][] f =
        {{0, 4, 3}, {3, 4, 1}, {1, 4, 2}, {2, 4, 0}, {3, 5, 0}, {1, 5, 3}, {2, 5, 1}, {0, 5, 2}};
    final Hull3d hull = Hull3d.create(v, f);
    // Triangle area = height*base/2
    final double base = Math.sqrt(2);
    final double height = Math.sqrt(base * base - (base / 2) * (base / 2));
    final double area = 8 * height * base / 2;
    // Test repeat calls
    Assertions.assertEquals(area, hull.getArea(), 1e-10);
    Assertions.assertEquals(area, hull.getArea(), 1e-10);
  }

  @Test
  void canGetVolume() {
    // Octahedron
    final double[][] v = {{1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}};
    final int[][] f =
        {{0, 4, 3}, {3, 4, 1}, {1, 4, 2}, {2, 4, 0}, {3, 5, 0}, {1, 5, 3}, {2, 5, 1}, {0, 5, 2}};
    Hull3d hull = Hull3d.create(v, f);
    // Pyramid volume = length*width*height/3 = root(2)*root(2)*1/3
    final double volume = 2 * (2.0 / 3);
    // Test repeat calls
    Assertions.assertEquals(volume, hull.getVolume());
    Assertions.assertEquals(volume, hull.getVolume());

    // Shift
    for (final double[] shift : new double[][] {{1, 2, -3}, {-5, -6, 17}}) {
      for (final double[] c : v) {
        c[0] += shift[0];
        c[1] += shift[1];
        c[2] += shift[2];
      }
      hull = Hull3d.create(v, f);
      Assertions.assertEquals(volume, hull.getVolume());
    }
  }

  @Test
  void canCreateWithRegularTetrahedron() {
    // https://en.wikipedia.org/wiki/Tetrahedron
    final double[][] v = {{1, 1, 1}, {1, -1, -1}, {-1, 1, -1}, {-1, -1, 1}};
    final int[][] f = {{0, 1, 2}, {0, 3, 1}, {3, 2, 1}, {3, 0, 2}};
    final Hull3d hull = Hull3d.create(v, f);
    Assertions.assertEquals(3, hull.dimensions());
    Assertions.assertEquals(4, hull.getNumberOfVertices());
    Assertions.assertArrayEquals(v, hull.getVertices());
    Assertions.assertEquals(4, hull.getNumberOfFaces());
    Assertions.assertArrayEquals(f, hull.getFaces());
    // Edge length
    final double edge = Math.sqrt(8);
    Assertions.assertEquals(Math.sqrt(3) * edge * edge, hull.getArea(), 1e-10);
    Assertions.assertEquals(edge * edge * edge / (6 * Math.sqrt(2)), hull.getVolume(), 1e-10);
    Assertions.assertArrayEquals(new double[] {0, 0, 0}, hull.getCentroid());

    // Test the point for each face
    final double t = 1.0 / 3;
    Assertions.assertArrayEquals(new double[] {t, t, -t}, hull.getPoint(0));
    Assertions.assertArrayEquals(new double[] {t, -t, t}, hull.getPoint(1));
    Assertions.assertArrayEquals(new double[] {-t, -t, -t}, hull.getPoint(2));
    Assertions.assertArrayEquals(new double[] {-t, t, t}, hull.getPoint(3));

    // Test the plane equation returns the correct normal and the value d to compute the
    // distance to the plane
    final double d = 1.0 / Math.sqrt(3);
    final double u = Math.sqrt(t) + Math.sqrt(3);
    assertPlane(hull.getPlane(0), new double[] {d, d, -d}, new double[][] {v[0], v[3]},
        new double[] {0, -u});
    assertPlane(hull.getPlane(1), new double[] {d, -d, d}, new double[][] {v[0], v[2]},
        new double[] {0, -u});
    assertPlane(hull.getPlane(2), new double[] {-d, -d, -d}, new double[][] {v[3], v[0]},
        new double[] {0, -u});
    assertPlane(hull.getPlane(3), new double[] {-d, d, d}, new double[][] {v[3], v[1]},
        new double[] {0, -u});
  }

  @Test
  void canCreateWithTetrahedron() {
    final double[][] v = {{0, 0, 0}, {1, 0, 0}, {0, 1, 0}, {0, 0, 1}};
    final int[][] f = {{0, 2, 1}, {2, 3, 1}, {0, 3, 2}, {0, 1, 3}};
    final Hull3d hull = Hull3d.create(v, f);
    Assertions.assertEquals(3, hull.dimensions());
    Assertions.assertEquals(4, hull.getNumberOfVertices());
    Assertions.assertArrayEquals(v, hull.getVertices());
    Assertions.assertEquals(4, hull.getNumberOfFaces());
    Assertions.assertArrayEquals(f, hull.getFaces());
    // Heron's formula for area given 3 sides: sqrt( p(p-a)(p-b)(p-c) )
    // with p the half perimeter
    final double side = Math.sqrt(2);
    final double p = 3 * side / 2;
    Assertions.assertEquals(3.0 / 2 + Math.sqrt(p * Math.pow(p - side, 3)), hull.getArea(), 1e-10);
    // 1/3 base area * height
    Assertions.assertEquals((1.0 / 2) / 3, hull.getVolume(), 1e-10);
    // Pyramid centroid: 3/4 along the line from the vertex to the centre of the opposite side.
    final double[] centre = {1.0 / 3, 1.0 / 3, 0};
    final double[] vector = {-centre[0], -centre[1], 1.0 - centre[2]};
    for (int i = 0; i < 3; i++) {
      centre[i] += vector[i] / 4;
    }
    Assertions.assertArrayEquals(centre, hull.getCentroid(), 1e-10);

    // Test the point for each face
    Assertions.assertArrayEquals(new double[] {1.0 / 3, 1.0 / 3, 0}, hull.getPoint(0));
    Assertions.assertArrayEquals(new double[] {1.0 / 3, 1.0 / 3, 1.0 / 3}, hull.getPoint(1));
    Assertions.assertArrayEquals(new double[] {0, 1.0 / 3, 1.0 / 3}, hull.getPoint(2));
    Assertions.assertArrayEquals(new double[] {1.0 / 3, 0, 1.0 / 3}, hull.getPoint(3));

    // Test the plane equation returns the correct normal and the value d to compute the
    // distance to the plane
    final double[] x = {1.5, 1.5, 1.5};
    final double d = 1.0 / Math.sqrt(3);
    assertPlane(hull.getPlane(0), new double[] {0, 0, -1}, new double[][] {v[0], v[3], x},
        new double[] {0, -1, -1.5});
    assertPlane(hull.getPlane(1), new double[] {d, d, d}, new double[][] {v[2], v[0], x},
        new double[] {0, -d, Math.sqrt(1.5 * 1.5 * 3) - d});
    assertPlane(hull.getPlane(2), new double[] {-1, 0, 0}, new double[][] {v[0], v[1], x},
        new double[] {0, -1, -1.5});
    assertPlane(hull.getPlane(3), new double[] {0, -1, 0}, new double[][] {v[0], v[2], x},
        new double[] {0, -1, -1.5});
  }

  @Test
  void canCreateWithCube() {
    final double[][] v = {{1, 1, -1}, {-1, 1, -1}, {-1, -1, -1}, {1, -1, -1}, {1, 1, 1}, {-1, 1, 1},
        {-1, -1, 1}, {1, -1, 1}};
    final int[][] f =
        {{0, 1, 5, 4}, {1, 2, 6, 5}, {2, 3, 7, 6}, {3, 0, 4, 7}, {7, 4, 5, 6}, {2, 1, 0, 3}};
    final Hull3d hull = Hull3d.create(v, f);
    Assertions.assertEquals(3, hull.dimensions());
    Assertions.assertEquals(8, hull.getNumberOfVertices());
    Assertions.assertArrayEquals(v, hull.getVertices());
    Assertions.assertEquals(6, hull.getNumberOfFaces());
    Assertions.assertArrayEquals(f, hull.getFaces());
    Assertions.assertEquals(2 * 2 * 6, hull.getArea(), 1e-10);
    Assertions.assertEquals(2 * 2 * 2, hull.getVolume(), 1e-10);
    Assertions.assertArrayEquals(new double[] {0, 0, 0}, hull.getCentroid());

    // Test the point for each face
    Assertions.assertArrayEquals(new double[] {0, 1, 0}, hull.getPoint(0));
    Assertions.assertArrayEquals(new double[] {-1, 0, 0}, hull.getPoint(1));
    Assertions.assertArrayEquals(new double[] {0, -1, 0}, hull.getPoint(2));
    Assertions.assertArrayEquals(new double[] {1, 0, 0}, hull.getPoint(3));
    Assertions.assertArrayEquals(new double[] {0, 0, 1}, hull.getPoint(4));
    Assertions.assertArrayEquals(new double[] {0, 0, -1}, hull.getPoint(5));

    // Test the plane equation returns the correct normal and the value d to compute the
    // distance to the plane
    final double[] p = {1.5, 0.5, 0.5};
    assertPlane(hull.getPlane(0), new double[] {0, 1, 0}, new double[][] {v[0], v[2], p},
        new double[] {0, -2, -0.5});
    assertPlane(hull.getPlane(1), new double[] {-1, 0, 0}, new double[][] {v[1], v[0], p},
        new double[] {0, -2, -2.5});
    assertPlane(hull.getPlane(2), new double[] {0, -1, 0}, new double[][] {v[2], v[5], p},
        new double[] {0, -2, -1.5});
    assertPlane(hull.getPlane(3), new double[] {1, 0, 0}, new double[][] {v[3], v[1], p},
        new double[] {0, -2, 0.5});
    assertPlane(hull.getPlane(4), new double[] {0, 0, 1}, new double[][] {v[7], v[3], p},
        new double[] {0, -2, -0.5});
    assertPlane(hull.getPlane(5), new double[] {0, 0, -1}, new double[][] {v[2], v[7], p},
        new double[] {0, -2, -1.5});
  }

  private static void assertPlane(double[] plane, double[] normal, double[][] points,
      double[] distances) {
    for (int i = 0; i < 3; i++) {
      Assertions.assertEquals(normal[i], plane[i]);
    }
    // Compute distance using the dot product plus the plane constant p:
    // d = N . X + p
    for (int i = 0; i < points.length; i++) {
      final double[] p = points[i];
      final double d = plane[0] * p[0] + plane[1] * p[1] + plane[2] * p[2] + plane[3];
      Assertions.assertEquals(distances[i], d, 1e-10);
    }
  }
}
