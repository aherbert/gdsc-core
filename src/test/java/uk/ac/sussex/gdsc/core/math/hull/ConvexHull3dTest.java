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

import gnu.trove.list.array.TDoubleArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.sampling.UnitSphereSampler;
import org.apache.commons.rng.sampling.distribution.ContinuousUniformSampler;
import org.apache.commons.rng.sampling.distribution.SharedStateContinuousSampler;
import org.apache.commons.rng.sampling.shape.UnitBallSampler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.core.utils.LocalCollectors;
import uk.ac.sussex.gdsc.core.utils.LocalList;
import uk.ac.sussex.gdsc.core.utils.MathUtils;
import uk.ac.sussex.gdsc.core.utils.SimpleArrayUtils;
import uk.ac.sussex.gdsc.core.utils.rng.RandomUtils;
import uk.ac.sussex.gdsc.test.junit5.RandomSeed;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.rng.RngUtils;

@SuppressWarnings({"javadoc"})
class ConvexHull3dTest {
  @Test
  void cannotComputeConvexHullFromLessThanFourCoords() {
    final TDoubleArrayList x = new TDoubleArrayList();
    final TDoubleArrayList y = new TDoubleArrayList();
    final TDoubleArrayList z = new TDoubleArrayList();
    Assertions.assertNull(ConvexHull3d.create(x.toArray(), y.toArray(), z.toArray()));
    // @formatter:off
    x.add(0); y.add(0); z.add(0);
    Assertions.assertNull(ConvexHull3d.create(x.toArray(), y.toArray(), z.toArray()));
    x.add(1); y.add(0); z.add(0);
    Assertions.assertNull(ConvexHull3d.create(x.toArray(), y.toArray(), z.toArray()));
    x.add(0); y.add(1); z.add(0);
    Assertions.assertNull(ConvexHull3d.create(x.toArray(), y.toArray(), z.toArray()));
    x.add(0); y.add(0); z.add(1);
    // @formatter:on
    Assertions.assertNotNull(ConvexHull3d.create(x.toArray(), y.toArray(), z.toArray()));
  }

  @SeededTest
  void canComputeConvexHullFromCube(RandomSeed seed) {
    final double[][] v = {{1, 1, -1}, {-1, 1, -1}, {-1, -1, -1}, {1, -1, -1}, {1, 1, 1}, {-1, 1, 1},
        {-1, -1, 1}, {1, -1, 1}};
    final int[][] f =
        {{0, 1, 5, 4}, {1, 2, 6, 5}, {2, 3, 7, 6}, {3, 0, 4, 7}, {7, 4, 5, 6}, {2, 1, 0, 3}};
    final double[] x = new double[v.length];
    final double[] y = new double[x.length];
    final double[] z = new double[x.length];
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    final double[][] v2 = v.clone();
    for (int n = 0; n < 3; n++) {
      RandomUtils.shuffle(v2, rng);
      for (int i = 0; i < v2.length; i++) {
        final double[] p = v2[i];
        x[i] = p[0];
        y[i] = p[1];
        z[i] = p[2];
      }
      final Hull3d hull = ConvexHull3d.create(x, y, z);
      check(v, f, hull);
    }
  }

  @SeededTest
  void canComputeConvexHullFromCubeWithInternalPoints(RandomSeed seed) {
    // Upgrade to BoxSampler from commons RNG 1.4
    final double[][] v = {{1, 1, -1}, {-1, 1, -1}, {-1, -1, -1}, {1, -1, -1}, {1, 1, 1}, {-1, 1, 1},
        {-1, -1, 1}, {1, -1, 1}};
    final int[][] f =
        {{0, 1, 5, 4}, {1, 2, 6, 5}, {2, 3, 7, 6}, {3, 0, 4, 7}, {7, 4, 5, 6}, {2, 1, 0, 3}};
    final int extra = 10;
    final double[] x = new double[v.length + extra];
    final double[] y = new double[x.length];
    final double[] z = new double[x.length];
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    final double[][] v2 = v.clone();
    for (int n = 0; n < 3; n++) {
      RandomUtils.shuffle(v2, rng);
      for (int i = 0; i < v2.length; i++) {
        final double[] p = v2[i];
        x[i] = p[0];
        y[i] = p[1];
        z[i] = p[2];
      }
      for (int i = extra; i < x.length; i++) {
        x[i] = rng.nextDouble() * 2 - 1;
        y[i] = rng.nextDouble() * 2 - 1;
        z[i] = rng.nextDouble() * 2 - 1;
      }
      final Hull3d hull = ConvexHull3d.create(x, y, z);
      check(v, f, hull);
    }
  }

  @SeededTest
  void canComputeConvexHullFromTetrahedronWithInternalPoints(RandomSeed seed) {
    // Upgrade to TetrahedronSampler from commons RNG 1.4
    final double[][] v = {{0, 0, 0}, {1, 0, 0}, {0, 1, 0}, {0, 0, 1}};
    final int[][] f = {{0, 2, 1}, {2, 3, 1}, {0, 3, 2}, {0, 1, 3}};
    final int extra = 10;
    final double[] x = new double[v.length + extra];
    final double[] y = new double[x.length];
    final double[] z = new double[x.length];
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    final double[][] v2 = v.clone();
    // Get the plane equation for points 1, 2, 3
    final double[] plane = getPlane(v[1], v[2], v[3]);

    for (int n = 0; n < 3; n++) {
      RandomUtils.shuffle(v2, rng);
      for (int i = 0; i < v2.length; i++) {
        final double[] p = v2[i];
        x[i] = p[0];
        y[i] = p[1];
        z[i] = p[2];
      }
      for (int i = extra; i < x.length;) {
        final double xx = rng.nextDouble();
        final double yy = rng.nextDouble();
        final double zz = rng.nextDouble();
        // Ensure internal. Measure distance from the plane is negative.
        if (xx * plane[0] + yy * plane[1] + zz * plane[2] >= -plane[3]) {
          continue;
        }
        x[i] = xx;
        y[i] = yy;
        z[i] = zz;
        i++;
      }
      final Hull3d hull = ConvexHull3d.create(x, y, z);
      check(v, f, hull);
    }
  }

  private static void check(double[][] v, int[][] f, Hull3d hull) {
    Assertions.assertEquals(v.length, hull.getNumberOfVertices());
    Assertions.assertEquals(f.length, hull.getNumberOfFaces());

    // Check all the vertices are present
    final LocalList<double[]> vertices = new LocalList<>(Arrays.asList(hull.getVertices()));
    // Vertices may be renumbered so we map them back to the original index
    final int[] map = SimpleArrayUtils.newIntArray(v.length, -1);
    for (int i = 0; i < v.length; i++) {
      final double[] p1 = v[i];
      final int index = vertices.findIndex(p2 -> Arrays.equals(p1, p2));
      Assertions.assertTrue(index != -1, () -> "Missing vertex: " + Arrays.toString(p1));
      Assertions.assertTrue(map[index] == -1, "Vertex already found");
      map[index] = i;
    }

    // Check all the faces are present.
    // Faces refer to the new vertex indices so map them to the original.
    // Faces may be permutations so we rotate them to the lowest index first.
    final LocalList<int[]> faces = Arrays.stream(hull.getFaces()).map(face -> {
      final int[] face2 = new int[face.length];
      for (int i = 0; i < face.length; i++) {
        face2[i] = map[face[i]];
      }
      return rotateFace(face2);
    }).collect(LocalCollectors.toLocalList());

    for (final int[] face : f) {
      final int[] p1 = rotateFace(face);
      Assertions.assertTrue(faces.findIndex(p2 -> Arrays.equals(p1, p2)) != -1,
          () -> "Missing face: " + Arrays.toString(p1));
    }
  }

  private static int[] rotateFace(int[] face) {
    final int start = SimpleArrayUtils.findMinIndex(face);
    final int[] face2 = new int[face.length];
    for (int i = 0; i < face.length; i++) {
      face2[i] = face[(start + i) % face.length];
    }
    return face2;
  }

  @Test
  void cannotBuildWithLessThanFourPoints() {
    final ConvexHull3d.Builder builder = ConvexHull3d.newBuilder();
    Assertions.assertNull(builder.build());
    builder.add(0, 0, 0);
    Assertions.assertNull(builder.build());
    builder.add(1, 0, 0);
    Assertions.assertNull(builder.build());
    builder.add(0, 1, 0);
    Assertions.assertNull(builder.build());
  }

  @Test
  void canBuildWithFourPoints() {
    final ConvexHull3d.Builder builder = ConvexHull3d.newBuilder();
    builder.add(0, 0, 0);
    builder.add(1, 0, 0);
    builder.add(0, 1, 0);
    builder.add(0, 0, 1);
    final Hull3d hull = builder.build();
    Assertions.assertEquals(4, hull.getNumberOfVertices());
    Assertions.assertEquals(3, hull.dimensions());
    final double[][] v = {{0, 0, 0}, {1, 0, 0}, {0, 1, 0}, {0, 0, 1}};
    final int[][] f = {{0, 2, 1}, {2, 3, 1}, {0, 3, 2}, {0, 1, 3}};
    check(v, f, hull);
  }

  @Test
  void canTriangulate() {
    final ConvexHull3d.Builder builder = ConvexHull3d.newBuilder();
    builder.add(0, 0, 0);
    builder.add(1, 0, 0);
    builder.add(0, 1, 0);
    builder.add(1, 1, 0);
    builder.add(0, 0, 1);
    Assertions.assertFalse(builder.isTriangulate());
    final Hull3d hull = builder.build();
    Assertions.assertEquals(5, hull.getNumberOfVertices());
    Assertions.assertEquals(3, hull.dimensions());
    final double[][] v = {{0, 0, 0}, {1, 0, 0}, {1, 1, 0}, {0, 1, 0}, {0, 0, 1}};
    final int[][] f = {{0, 3, 2, 1}, {0, 1, 4}, {1, 2, 4}, {2, 3, 4}, {3, 0, 4}};
    check(v, f, hull);

    // Should triangulate the square base. There are two possible ways to triangulate
    // a square so this test is not robust to changes in the implementation.
    Assertions.assertSame(builder, builder.setTriangulate(true));
    Assertions.assertTrue(builder.isTriangulate());
    final Hull3d hull2 = builder.build();
    final double[][] v2 = {{0, 0, 0}, {1, 0, 0}, {1, 1, 0}, {0, 1, 0}, {0, 0, 1}};
    final int[][] f2 = {{0, 3, 2}, {0, 2, 1}, {0, 1, 4}, {1, 2, 4}, {2, 3, 4}, {3, 0, 4}};
    check(v2, f2, hull2);
  }

  @Test
  void canClearBuilder() {
    final ConvexHull3d.Builder builder = ConvexHull3d.newBuilder();
    builder.add(0, 0, 0);
    builder.add(1, 0, 0);
    builder.add(0, 1, 0);
    builder.add(0, 0, 1);
    final Hull3d hull1 = builder.build();
    Assertions.assertNotNull(hull1);
    final Hull3d hull2 = builder.build();
    Assertions.assertNotNull(hull2);
    Assertions.assertNotSame(hull1, hull2);
    builder.clear();
    final Hull3d hull3 = builder.build();
    Assertions.assertNull(hull3);
  }

  @Test
  void canBuildWithManyPoints() {
    // Sample from a unit ball.
    final UnitBallSampler sampler = UnitBallSampler.of(RngUtils.create(126487618L), 3);
    final int n = 5000;
    final ConvexHull3d.Builder builder = ConvexHull3d.newBuilder();
    final double[] centroid = {1, -2, 3};
    for (int i = 0; i < n; i++) {
      final double[] sample = sampler.sample();
      for (int j = 0; j < 3; j++) {
        sample[j] += centroid[j];
      }
      builder.add(sample);
    }
    // Test volume and area
    final Hull3d hull = builder.build();
    Assertions.assertEquals(4 * Math.PI, hull.getArea(), 0.5, "Area");
    Assertions.assertEquals(4 * Math.PI / 3, hull.getVolume(), 0.3, "Volume");
    Assertions.assertArrayEquals(centroid, hull.getCentroid(), 0.01, "Centroid");
  }

  @Test
  void canCutHullWithManyPoints() {
    // Sample from a unit sphere.
    final UniformRandomProvider rng = RngUtils.create(126487618L);
    final UnitSphereSampler sampler = UnitSphereSampler.of(rng, 3);
    final int n = 500;
    final ConvexHull3d.Builder builder = ConvexHull3d.newBuilder();
    for (int i = 0; i < n; i++) {
      builder.add(sampler.sample());
    }
    // Test volume and area
    final Hull3d hull = builder.build();
    Assertions.assertEquals(4 * Math.PI, hull.getArea(), 0.5, "Area");
    Assertions.assertEquals(4 * Math.PI / 3, hull.getVolume(), 0.3, "Volume");
    Assertions.assertArrayEquals(new double[3], hull.getCentroid(), 0.01, "Centroid");

    // Now cut the sphere.
    // Each result should be an approximate circle.
    // Offset a random distance (avoid close to the edge)
    final SharedStateContinuousSampler offset = ContinuousUniformSampler.of(rng, -0.9, 0.9);
    for (int i = 0; i < 10; i++) {
      // Random plane through the origin
      final double[] plane = Arrays.copyOf(sampler.sample(), 4);
      // Offset the plane
      final double r = offset.sample();
      plane[3] = r;
      final List<List<double[]>> polygons = hull.getPolygons(plane);
      Assertions.assertEquals(1, polygons.size());
      // Compute the diameter
      double diameter = 0;
      final List<double[]> polygon = polygons.get(0);
      for (int j = polygon.size(), j1 = 0; j-- > 0; j1 = j) {
        final double[] p1 = polygon.get(j);
        final double[] p2 = polygon.get(j1);
        diameter += MathUtils.distance(p1[0], p1[1], p1[2], p2[0], p2[1], p2[2]);
      }
      // Compute the expected diameter
      // https://en.wikipedia.org/wiki/Circle_of_a_sphere
      final double radius = Math.sqrt(1 - r * r);
      Assertions.assertEquals(2 * Math.PI * radius, diameter, 0.1);
    }
  }

  /**
   * Gets the best fit plane for the specified points. The points are assumed to a convex polygon
   * using anti-clockwise ordering. Returns a vector of length 4 representing the plane equation:
   *
   * <pre>
   * ax + by + cz + d = 0
   *
   * (X - P) . N = 0
   * </pre>
   *
   * <p>where {@code X = (x, y, z)}, {@code N = (a, b, c)} is the normal to the plane, and {@code P}
   * is a point on the plane.
   *
   * <p>The plane is specified in Hessian normal form where the normal vector is of unit length and
   * {@code d} is the is the distance of the plane from the origin.
   *
   * <p>Note: This method is extracted from {@link Hull3d}.
   *
   * @param points the points
   * @return the plane
   * @see <a href="https://mathworld.wolfram.com/HessianNormalForm.html">Hessian normal form
   *      (Wolfram)</a>
   */
  private static double[] getPlane(double[]... points) {
    // Use Newell's method to compute the normal N of the best fit plane of all the vertices
    // and point P is a point on the plane using the centre of gravity.
    double a = 0;
    double b = 0;
    double c = 0;
    double px = 0;
    double py = 0;
    double pz = 0;
    for (int i = points.length, i1 = 0; i-- > 0; i1 = i) {
      final double x = points[i][0];
      final double y = points[i][1];
      final double z = points[i][2];
      final double x1 = points[i1][0];
      final double y1 = points[i1][1];
      final double z1 = points[i1][2];
      a += (y - y1) * (z + z1);
      b += (z - z1) * (x + x1);
      c += (x - x1) * (y + y1);
      px += x;
      py += y;
      pz += z;
    }
    // Normalise to unit length
    final double norm = 1.0 / Math.sqrt(a * a + b * b + c * c);
    final double[] result = new double[] {a * norm, b * norm, c * norm, 0};
    // Solve the equation (X - P) . N = 0
    // Compute D = -P . N
    // (Note: P has not been divided by the number of points so we do it here)
    result[3] = (-px * result[0] - py * result[1] - pz * result[2]) / points.length;
    return result;
  }
}
