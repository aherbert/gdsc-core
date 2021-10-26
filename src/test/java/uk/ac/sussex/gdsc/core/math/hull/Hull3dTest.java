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
 * Copyright (C) 2011 - 2021 Alex Herbert
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
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.core.math.hull.Hull3d.Edge;
import uk.ac.sussex.gdsc.core.math.hull.Hull3d.MarkedEdge;
import uk.ac.sussex.gdsc.core.utils.LocalList;
import uk.ac.sussex.gdsc.core.utils.SimpleArrayUtils;

@SuppressWarnings({"javadoc"})
class Hull3dTest {
  @Test
  void testEdge() {
    final Edge e1 = Edge.create(0, 1);
    Assertions.assertEquals(0, e1.from);
    Assertions.assertEquals(1, e1.to);
    final Edge e2 = Edge.create(1, 0);
    Assertions.assertEquals(0, e2.from);
    Assertions.assertEquals(1, e2.to);
    final Edge e3 = Edge.create(1, 2);
    Assertions.assertEquals(1, e3.from);
    Assertions.assertEquals(2, e3.to);
    Assertions.assertEquals(0, Edge.compare(e1, e2));
    Assertions.assertEquals(0, Edge.compare(e2, e1));
    Assertions.assertEquals(-1, Edge.compare(e1, e3));
    Assertions.assertEquals(1, Edge.compare(e3, e1));
  }

  @Test
  void testMarkedEdge() {
    final MarkedEdge e1 = MarkedEdge.create(0, 1);
    Assertions.assertEquals(0, e1.from);
    Assertions.assertEquals(1, e1.to);
    final MarkedEdge e2 = MarkedEdge.create(1, 0);
    Assertions.assertEquals(0, e2.from);
    Assertions.assertEquals(1, e2.to);
    final MarkedEdge e3 = MarkedEdge.create(1, 2);
    Assertions.assertEquals(1, e3.from);
    Assertions.assertEquals(2, e3.to);
    Assertions.assertEquals(0, Edge.compare(e1, e2));
    Assertions.assertEquals(0, Edge.compare(e2, e1));
    Assertions.assertEquals(-1, Edge.compare(e1, e3));
    Assertions.assertEquals(1, Edge.compare(e3, e1));
  }

  @Test
  void testPointHashingStrategy() {
    final double[] p0 = {0.0, 0.0, 0.0};
    final double[] p1 = {1, 2, 3};
    final double[] p2 = {6.76, 8.90, 1.11};
    final double[] p3 = {-0.0, -0.0, -0.0};
    for (final double[] p : new double[][] {p0, p1, p2}) {
      Assertions.assertEquals(Arrays.hashCode(p),
          Hull3d.PointHashingStrategy.INSTANCE.computeHashCode(p), () -> Arrays.toString(p));
      Assertions.assertTrue(Hull3d.PointHashingStrategy.INSTANCE.equals(p, p));
    }
    Assertions.assertFalse(Hull3d.PointHashingStrategy.INSTANCE.equals(p0, p1));
    Assertions.assertFalse(Hull3d.PointHashingStrategy.INSTANCE.equals(p1, p2));

    Assertions.assertTrue(Hull3d.PointHashingStrategy.INSTANCE.equals(p0, p3));
    Assertions.assertEquals(Hull3d.PointHashingStrategy.INSTANCE.computeHashCode(p0),
        Hull3d.PointHashingStrategy.INSTANCE.computeHashCode(p3));
  }

  @Test
  void testEdgeHashingStrategy() {
    final Edge p0 = Edge.create(0, 1);
    final Edge p1 = Edge.create(1, 0);
    final Edge p2 = Edge.create(1, 2);
    final Edge p3 = Edge.create(3, 4);
    for (final Edge p : new Edge[] {p0, p1, p2, p3}) {
      Assertions.assertEquals(Arrays.hashCode(new int[] {p.from, p.to}),
          Hull3d.EdgeHashingStrategy.INSTANCE.computeHashCode(p),
          () -> Arrays.toString(new int[] {p.from, p.to}));
      Assertions.assertTrue(Hull3d.EdgeHashingStrategy.INSTANCE.equals(p, p));
    }
    Assertions.assertTrue(Hull3d.EdgeHashingStrategy.INSTANCE.equals(p0, p1));
    Assertions.assertFalse(Hull3d.EdgeHashingStrategy.INSTANCE.equals(p1, p2));
    Assertions.assertFalse(Hull3d.EdgeHashingStrategy.INSTANCE.equals(p2, p3));
  }

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

  @Test
  void testComparePoint() {
    final double[] p0 = {-0.0, 0, 0};
    final double[] p1 = {0, 0, 0};
    final double[] p2 = {1, 0, 0};
    final double[] p3 = {0, 1, 0};
    final double[] p4 = {0, 0, 1};
    assertComparePoint(0, p0, p0);
    assertComparePoint(0, p1, p1);
    assertComparePoint(0, p2, p2);
    assertComparePoint(0, p3, p3);
    assertComparePoint(0, p4, p4);

    assertComparePoint(0, p0, p1);
    assertComparePoint(-1, p1, p2);
    assertComparePoint(-1, p1, p3);
    assertComparePoint(-1, p1, p4);
    assertComparePoint(1, p2, p3);
    assertComparePoint(1, p2, p4);
    assertComparePoint(1, p3, p4);
  }

  private static void assertComparePoint(int result, double[] p1, double[] p2) {
    Assertions.assertEquals(result, Hull3d.compare(p1, p2));
    Assertions.assertEquals(-result, Hull3d.compare(p2, p1));
  }

  @Test
  void testCreatePolygon() {
    final double[][] empty = new double[0][];

    final LocalList<double[]> coords = new LocalList<>();
    coords.add(new double[] {0, 0, 0});
    coords.add(new double[] {1, 0, 0});
    coords.add(new double[] {0, 1, 0});
    final LocalList<MarkedEdge> lines = new LocalList<>();
    lines.add(new MarkedEdge(0, 1));
    lines.add(new MarkedEdge(1, 2));

    final double[] plane = {0, 0, 1};

    // Missing edge (2, 0)
    Assertions.assertThrows(IllegalStateException.class,
        () -> assertCreatePolygon(true, 0, lines, coords.toArray(empty), plane, null));

    lines.add(new MarkedEdge(2, 0));
    assertCreatePolygon(true, 0, lines, coords.toArray(empty), plane, new int[] {0, 1, 2, 0});
    assertCreatePolygon(true, 1, lines, coords.toArray(empty), plane, new int[] {1, 2, 0, 1});

    // Add another face in the same plane
    coords.add(new double[] {1, 1, 0});
    lines.add(new MarkedEdge(1, 3));
    lines.add(new MarkedEdge(3, 2));

    // Missing (2, 1)
    assertCreatePolygon(true, 0, lines, coords.toArray(empty), plane, new int[] {0, 1, 2, 0});
    Assertions.assertThrows(IllegalStateException.class,
        () -> assertCreatePolygon(false, 3, lines, coords.toArray(empty), plane, null));

    lines.add(new MarkedEdge(2, 1));
    assertCreatePolygon(true, 0, lines, coords.toArray(empty), plane, new int[] {0, 1, 2, 0});
    assertCreatePolygon(false, 3, lines, coords.toArray(empty), plane, new int[] {1, 3, 2, 1});

    // Another face using a line that has a cross product the other direction
    // at the junction vertex 1. (1,2), (1,3) or (1,4)
    coords.add(new double[] {2, -1, 0});
    lines.add(new MarkedEdge(1, 4));
    lines.add(new MarkedEdge(4, 3));
    lines.add(new MarkedEdge(3, 1));
    assertCreatePolygon(true, 0, lines, coords.toArray(empty), plane, new int[] {0, 1, 2, 0});
    assertCreatePolygon(false, 3, lines, coords.toArray(empty), plane, new int[] {1, 3, 2, 1});
    assertCreatePolygon(false, 6, lines, coords.toArray(empty), plane, new int[] {1, 4, 3, 1});
  }

  private static void assertCreatePolygon(boolean resetLines, int start,
      LocalList<MarkedEdge> lines, double[][] coordinates, double[] plane, int[] expected) {
    if (resetLines) {
      lines.forEach(e -> e.mark = 0);
    }
    final int[] actual = Hull3d.createPolygon(start, lines, coordinates, plane);
    Assertions.assertArrayEquals(expected, actual);
  }

  @Test
  void testGetPolygonsWithCube() {
    final double[][] v = {{1, 1, -1}, {-1, 1, -1}, {-1, -1, -1}, {1, -1, -1}, {1, 1, 1}, {-1, 1, 1},
        {-1, -1, 1}, {1, -1, 1}};
    final int[][] f =
        {{0, 1, 5, 4}, {1, 2, 6, 5}, {2, 3, 7, 6}, {3, 0, 4, 7}, {7, 4, 5, 6}, {2, 1, 0, 3}};
    final Hull3d hull = Hull3d.create(v, f);

    // Not Hessian normalised plane
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> hull.getPolygons(new double[] {1, 0.0001, 0, 0}));

    // Plane does not cut the hull
    Assertions.assertEquals(Collections.emptyList(), hull.getPolygons(new double[] {1, 0, 0, 10}));
    Assertions.assertEquals(Collections.emptyList(), hull.getPolygons(new double[] {1, 0, 0, -10}));

    // Cut through the middle along z-axis
    assertPolygons(hull, new double[] {0, 0, 1, 0}, Arrays
        .asList(Arrays.asList(new double[][] {{-1, -1, 0}, {1, -1, 0}, {1, 1, 0}, {-1, 1, 0}})));
    // Opposite orientation along z-axis
    assertPolygons(hull, new double[] {0, 0, -1, 0}, Arrays
        .asList(Arrays.asList(new double[][] {{-1, -1, 0}, {-1, 1, 0}, {1, 1, 0}, {1, -1, 0}})));
    // Offset cut along z-axis
    assertPolygons(hull, new double[] {0, 0, 1, -0.5}, Arrays.asList(
        Arrays.asList(new double[][] {{-1, -1, 0.5}, {1, -1, 0.5}, {1, 1, 0.5}, {-1, 1, 0.5}})));
    // Offset cut along x-axis
    assertPolygons(hull, new double[] {1, 0, 0, -0.5}, Arrays.asList(
        Arrays.asList(new double[][] {{0.5, -1, -1}, {0.5, 1, -1}, {0.5, 1, 1}, {0.5, -1, 1}})));
    // Offset cut along y-axis
    assertPolygons(hull, new double[] {0, 1, 0, -0.5}, Arrays.asList(
        Arrays.asList(new double[][] {{-1, 0.5, -1}, {1, 0.5, -1}, {1, 0.5, 1}, {-1, 0.5, 1}})));

    // Cut in a face
    assertPolygons(hull, new double[] {1, 0, 0, -1}, Arrays
        .asList(Arrays.asList(new double[][] {{1, -1, -1}, {1, 1, -1}, {1, 1, 1}, {1, -1, 1}})));
    // Cut in the same face, opposite direction for the plane reverses winding
    assertPolygons(hull, new double[] {-1, 0, 0, 1}, Arrays
        .asList(Arrays.asList(new double[][] {{1, -1, -1}, {1, -1, 1}, {1, 1, 1}, {1, 1, -1}})));

    // Cut to touch a face edge but not through the hull
    assertPolygons(hull, new double[] {1, 1, 0, -Math.sqrt(2)}, Collections.emptyList());

    // Cut to touch a vertex but not through the hull
    // ax+by+cz+d = 0
    // for point (1,1,1): d = 3 * 1.0 / sqrt(3) = sqrt(3)
    // Due to floating point error we must use additions for ax+by+cz = -d
    final double a = 1.0 / Math.sqrt(3);
    assertPolygons(hull, new double[] {1, 1, 1, -a - a - a}, Collections.emptyList());

    // Cut across the cube
    assertPolygons(hull, new double[] {1, 1, 1, -1 / Math.sqrt(3)},
        Arrays.asList(Arrays.asList(new double[][] {{-1, 1, 1}, {1, -1, 1}, {1, 1, -1}})));
  }

  @Test
  void testGetPolygonsWithNonConvexFace() {
    // Pyramid with non convex base
    final double[][] v = {{1, 1, 1}, {-1, -1, 0}, {0, 0, 0}, {1, -1, 0}, {0, 1, 0},};
    final int[][] f = {{0, 1, 2}, {0, 2, 3}, {0, 3, 4}, {0, 4, 1},
        // Base is not convex
        {1, 4, 3, 2},};
    final Hull3d hull = Hull3d.create(v, f);

    Assertions.assertThrows(IllegalStateException.class,
        () -> hull.getPolygons(new double[] {0, 1, 0, 0.5}));
  }

  @Test
  void testGetPolygonsWithNonConvexHull() {
    // Two overlapping pyramids
    //@formatter:off
    final double[][] v = {
        // Lower square
        {1, 1, -1},
        {-1, 1, -1},
        {-1, -1, -1},
        {1, -1, -1},
        // Middle square
        {0.5, 0.5, 0},
        {-0.5, 0.5, 0},
        {-0.5, -0.5, 0},
        {0.5, -0.5, 0},
        // Upper square
        {1, 1, 1},
        {-1, 1, 1},
        {-1, -1, 1},
        {1, -1, 1},
    };
    //@formatter:on

    final int[][] f = {
        // Base
        {0, 3, 2, 1},
        // Lower faces
        {0, 4, 7, 3}, {1, 5, 4, 0}, {2, 6, 5, 1}, {3, 7, 6, 2},
        // Upper faces
        {4, 8, 11, 7}, {5, 9, 8, 4}, {6, 10, 9, 5}, {7, 11, 10, 6},
        // Top
        {8, 9, 10, 11}};
    final Hull3d hull = Hull3d.create(v, f);

    // Through the middle
    assertPolygons(hull, new double[] {0, 0, 1, 0}, Arrays.asList(Arrays
        .asList(new double[][] {{-0.5, -0.5, 0}, {0.5, -0.5, 0}, {0.5, 0.5, 0}, {-0.5, 0.5, 0}})));
    // Opposite winding
    assertPolygons(hull, new double[] {0, 0, -1, 0}, Arrays.asList(Arrays
        .asList(new double[][] {{-0.5, -0.5, 0}, {-0.5, 0.5, 0}, {0.5, 0.5, 0}, {0.5, -0.5, 0}})));

    // Touching top and bottom but no cut
    assertPolygons(hull, new double[] {1, 0, 0, 1}, Collections.emptyList());

    // Through the middle vertically
    assertPolygons(hull, new double[] {1, 0, 0, 0}, Arrays.asList(Arrays.asList(new double[][] {
        {0, -1, -1}, {0, 1, -1}, {0, 0.5, 0}, {0, 1, 1}, {0, -1, 1}, {0, -0.5, 0},})));

    assertPolygons(hull, new double[] {1, 0, 0, -0.75}, Arrays.asList(
        Arrays.asList(
            new double[][] {{0.75, -1, 1}, {0.75, -0.75, 0.5}, {0.75, 0.75, 0.5}, {0.75, 1, 1},}),
        Arrays.asList(
            new double[][] {{0.75, -1, -1}, {0.75, 1, -1}, {0.75, 0.75, 0.5}, {0.75, -0.75, 0.5},})

    ));
  }

  @Test
  void testGetPolygonsWithIndentedCube() {
    // Unit cube with a cube cut off one octant.
    // Removed octant +--
    // https://en.wikipedia.org/wiki/Octant_(solid_geometry)
    final double[][] v = {
        // Bottom
        {-1, -1, -1}, {0, -1, -1}, {1, -1, -1}, {-1, 0, -1}, {0, 0, -1}, {1, 0, -1}, {-1, 1, -1},
        {0, 1, -1}, {1, 1, -1},
        // Middle
        {-1, -1, 0}, {0, -1, 0}, {1, -1, 0}, {-1, 0, 0}, {0, 0, 0}, {1, 0, 0}, {-1, 1, 0},
        {0, 1, 0}, {1, 1, 0},
        // Top
        {-1, -1, 1}, {0, -1, 1}, {1, -1, 1}, {-1, 0, 1}, {0, 0, 1}, {1, 0, 1}, {-1, 1, 1},
        {0, 1, 1}, {1, 1, 1},};
    // Faces must be convex. Here we create smaller square faces in the larger square faces
    // so the edges are all paired.
    final int[][] f = {
        // Projected face 0
        {0, 3, 4, 1}, {1, 4, 5, 2}, {3, 6, 7, 4}, {4, 7, 8, 5},
        // Projected face 1
        {0, 1, 10, 9}, {1, 2, 11, 10}, {9, 10, 19, 18}, {13, 14, 23, 22},
        // Projected face 2
        {0, 9, 12, 3}, {3, 12, 15, 6}, {9, 18, 21, 12}, {12, 21, 24, 15},
        // Projected face 3
        {6, 15, 16, 7}, {7, 16, 17, 8}, {15, 24, 25, 16}, {16, 25, 26, 17},
        // Projected face 4
        {2, 5, 14, 11}, {5, 8, 17, 14}, {10, 13, 22, 19}, {14, 17, 26, 23},
        // Projected face 5
        {18, 19, 22, 21}, {10, 11, 14, 13}, {21, 22, 25, 24}, {22, 23, 26, 25},

    };
    final Hull3d hull = Hull3d.create(v, f);

    // Square cross section
    assertPolygons(hull, new double[] {0, 0, 1, 0.5},
        Arrays.asList(Arrays.asList(new double[][] {{-1, -1, -0.5}, {0, -1, -0.5}, {1, -1, -0.5},
            {1, 0, -0.5}, {1, 1, -0.5}, {0, 1, -0.5}, {-1, 1, -0.5}, {-1, 0, -0.5}})));

    // Square cross section of a face
    assertPolygons(hull, new double[] {0, 0, 1, -1},
        Arrays.asList(Arrays.asList(new double[][] {{-1, -1, 1}, {0, -1, 1}, {1, -1, 1},
            {1, 0, 1}, {1, 1, 1}, {0, 1, 1}, {-1, 1, 1}, {-1, 0, 1}})));

    // Quadrant removed: L-shape
    assertPolygons(hull, new double[] {0, 0, 1, -0.5},
        Arrays.asList(Arrays.asList(new double[][] {{-1, -1, 0.5}, {0, -1, 0.5}, {0, 0, 0.5},
            {1, 0, 0.5}, {1, 1, 0.5}, {0, 1, 0.5}, {-1, 1, 0.5}, {-1, 0, 0.5}})));

    // Through the middle should obtain a square not an L-shape
    assertPolygons(hull, new double[] {0, 0, 1, 0},
        Arrays.asList(Arrays.asList(new double[][] {{-1, -1, 0}, {0, -1, 0}, {1, -1, 0}, {1, 0, 0},
            {1, 1, 0}, {0, 1, 0}, {-1, 1, 0}, {-1, 0, 0}})));
  }

  /**
   * Assert the polygons obtained from cutting the hull with the given plane.
   *
   * @param hull the hull
   * @param plane the plane
   * @param expected the expected (assumed already rotated to min XYZ)
   */
  private static void assertPolygons(Hull3d hull, double[] plane, List<List<double[]>> expected) {
    // Normalise plane vector.
    // Copy the normalisation epsilon from com.github.quickhull3d.Vector3d.normalize()
    double length = plane[0] * plane[0] + plane[1] * plane[1] + plane[2] * plane[2];
    final double err = length - 1;
    if (Math.abs(err) > 2 * 2.2204460492503131e-16) {
      length = Math.sqrt(length);
      for (int i = 0; i < 3; i++) {
        plane[i] /= length;
      }
    }

    final List<List<double[]>> actual = hull.getPolygons(plane);
    Assertions.assertEquals(expected.size(), actual.size());
    // rotate each so the first vertex is the min XYZ
    for (final List<double[]> poly : actual) {
      rotateToMinXyz(poly);
    }
    // Sort by min XYZ
    expected.sort((p1, p2) -> Hull3d.compare(p1.get(0), p2.get(0)));
    actual.sort((p1, p2) -> Hull3d.compare(p1.get(0), p2.get(0)));
    for (int i = 0; i < expected.size(); i++) {
      final List<double[]> p1 = expected.get(i);
      final List<double[]> p2 = actual.get(i);
      Assertions.assertEquals(p1.size(), p2.size());
      for (int j = 0; j < expected.size(); j++) {
        Assertions.assertArrayEquals(p1.get(j), p2.get(j));
      }
    }
  }

  private static void rotateToMinXyz(List<double[]> poly) {
    // Find index of min XYZ
    double[] min = poly.get(0);
    int index = 0;
    for (int i = 1; i < poly.size(); i++) {
      final double[] xyz = poly.get(i);
      if (Hull3d.compare(xyz, min) < 0) {
        min = xyz;
        index = i;
      }
    }
    if (index == 0) {
      return;
    }
    // Rotate
    poly.addAll(poly.subList(0, index));
    poly.subList(0, index).clear();
  }
}
