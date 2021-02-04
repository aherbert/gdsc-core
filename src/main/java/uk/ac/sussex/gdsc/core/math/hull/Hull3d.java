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

import uk.ac.sussex.gdsc.core.utils.SimpleArrayUtils;
import uk.ac.sussex.gdsc.core.utils.ValidationUtils;

/**
 * Contains a set of coordinates representing the hull of a set of points. This should be a
 * non-self-intersecting (simple) polyhedron, which can be convex or concave.
 *
 * @since 2.0
 */
public final class Hull3d implements Hull {

  /** The vertices. */
  private final double[][] vertices;
  /** The faces (using counter-clockwise ordering). */
  private final int[][] faces;
  /** The centroid. */
  private double[] centroid;
  /** The area. */
  private double area;
  /** The volume. */
  private double volume;

  /**
   * Instantiates a new hull.
   *
   * @param vertices the vertices
   * @param faces the faces
   */
  Hull3d(double[][] vertices, int[][] faces) {
    this.vertices = vertices;
    this.faces = faces;
  }

  /**
   * Create a new hull from the given vertices and faces (counter-clockwise ordering). The input
   * arrays are copied.
   *
   * <p>No validation is performed to check each face is a plane, or that the faces cover the entire
   * surface of the hull.
   *
   * <p>A simple validation is performed to check that the number of faces ({@code F}) is at most
   * {@code F = 2V - 4} with {@code V} the number of vertices.
   *
   * @param vertices the vertices
   * @param faces the faces (counter-clockwise ordering)
   * @return the convex hull
   * @throws NullPointerException if the inputs are null
   * @throws IllegalArgumentException if the array lengths are less than 4, if the coordinate
   *         lengths are not at least 3, if the faces lengths are not at least 3, if the indices of
   *         the faces do not reference a vertex, or if the number of faces is too large.
   */
  public static Hull3d create(double[][] vertices, int[][] faces) {
    ValidationUtils.checkArgument(vertices.length >= 4, "vertices length");
    ValidationUtils.checkArgument(faces.length >= 4, "faces length");
    ValidationUtils.checkArgument(faces.length <= 2 * vertices.length - 4, "F > 2V - 4: F=%d, V=%d",
        faces.length, vertices.length);

    // Note: Potential edge validation.
    // Each edge (E) is a consecutive pair on the face. There should be pairs of corresponding
    // half edges in opposite directions.
    // e.g. face {0, 1, 2} expects a single occurrence of the opposite edges {1, 0}, {2, 1} and {0,
    // 2} to be present in the rest of the faces.

    final int numberOfVertices = vertices.length;
    final double[][] v = new double[numberOfVertices][];
    for (int i = 0; i < numberOfVertices; i++) {
      v[i] = copyVertex(vertices[i]);
    }
    final int[][] f = new int[faces.length][];
    for (int i = 0; i < faces.length; i++) {
      f[i] = copyFace(faces[i], numberOfVertices);
    }
    return new Hull3d(v, f);
  }

  /**
   * Copy the first 3 positions of the input vertex array.
   *
   * @param vertex the vertex
   * @return the copy
   * @throws IllegalArgumentException if the array length is not at least 3
   */
  private static double[] copyVertex(double[] vertex) {
    ValidationUtils.checkArgument(vertex.length >= 3, "vertex length < 3: %d", vertex.length);
    final double[] v = new double[3];
    System.arraycopy(vertex, 0, v, 0, 3);
    return v;
  }

  /**
   * Copy the input face array. Validate the index is valid for the number of vertices.
   *
   * @param face the face
   * @param numberOfVertices the number of vertices
   * @return the copy
   * @throws IllegalArgumentException if the array length is not at least 3
   */
  private static int[] copyFace(int[] face, int numberOfVertices) {
    ValidationUtils.checkArgument(face.length >= 3, "face length < 3: %d", face.length);
    for (final int i : face) {
      ValidationUtils.checkIndex(i, numberOfVertices, "vertex index");
    }
    return face.clone();
  }

  @Override
  public int dimensions() {
    return 3;
  }

  @Override
  public int getNumberOfVertices() {
    return vertices.length;
  }

  /**
   * {@inheritDoc}.
   *
   * <p>The returned result is a copy of the vertices.
   */
  @Override
  public double[][] getVertices() {
    final double[][] v = new double[getNumberOfVertices()][];
    for (int i = 0; i < v.length; i++) {
      v[i] = vertices[i].clone();
    }
    return v;
  }

  /**
   * Gets the number of faces associated with this hull.
   *
   * @return the number of faces
   */
  public int getNumberOfFaces() {
    return faces.length;
  }

  /**
   * Gets the faces associated with this hull.
   *
   * <p>Each face is represented by an integer array which gives the indices of the respective hull
   * vertices arranged counter-clockwise.
   *
   * <p>The returned result is a copy of the faces.
   *
   * @return the vertex indices for each face
   */
  public int[][] getFaces() {
    final int[][] v = new int[getNumberOfFaces()][];
    for (int i = 0; i < v.length; i++) {
      v[i] = faces[i].clone();
    }
    return v;
  }

  // Additional 3D methods

  /**
   * Gets the plane for the specified face. Returns a vector of length 4 representing the plane
   * equation:
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
   * @param index the face index
   * @return the plane
   * @see <a href="https://mathworld.wolfram.com/HessianNormalForm.html">Hessian normal form
   *      (Wolfram)</a>
   */
  public double[] getPlane(int index) {
    final int[] face = faces[index];
    // Use Newell's method to compute the normal N of the best fit plane of all the vertices
    // and point P is a point on the plane using the centre of gravity.
    double a = 0;
    double b = 0;
    double c = 0;
    double px = 0;
    double py = 0;
    double pz = 0;
    for (int i = face.length, i1 = 0; i-- > 0; i1 = i) {
      final int v1 = face[i];
      final int v2 = face[i1];
      final double x = vertices[v1][0];
      final double y = vertices[v1][1];
      final double z = vertices[v1][2];
      final double x1 = vertices[v2][0];
      final double y1 = vertices[v2][1];
      final double z1 = vertices[v2][2];
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
    result[3] = (-px * result[0] - py * result[1] - pz * result[2]) / face.length;
    return result;
  }

  /**
   * Gets the plane normal and a point for the specified face into the given result arrays.
   *
   * <p>The plane normal is raw and should be normalised to unit length if required.
   *
   * @param face the face
   * @param normal the result
   * @param point the point
   */
  private void getPlane(int[] face, double[] normal, double[] point) {
    // Use Newell's method to compute the normal N of the best fit plane of all the vertices.
    double a = 0;
    double b = 0;
    double c = 0;
    double px = 0;
    double py = 0;
    double pz = 0;
    for (int i = face.length, i1 = 0; i-- > 0; i1 = i) {
      final int v1 = face[i];
      final int v2 = face[i1];
      final double x = vertices[v1][0];
      final double y = vertices[v1][1];
      final double z = vertices[v1][2];
      final double x1 = vertices[v2][0];
      final double y1 = vertices[v2][1];
      final double z1 = vertices[v2][2];
      a += (y - y1) * (z + z1);
      b += (z - z1) * (x + x1);
      c += (x - x1) * (y + y1);
      px += x;
      py += y;
      pz += z;
    }
    normal[0] = a;
    normal[1] = b;
    normal[2] = c;
    point[0] = px / face.length;
    point[1] = py / face.length;
    point[2] = pz / face.length;
  }

  /**
   * Gets a point on the specified face.
   *
   * <p>The point is the centre of gravity of the face.
   *
   * @param index the face index
   * @return the point
   */
  public double[] getPoint(int index) {
    final int[] face = faces[index];
    // Use Newell's method with the point as the centre of gravity of the face.
    double a = 0;
    double b = 0;
    double c = 0;
    for (final int v1 : face) {
      final double x = vertices[v1][0];
      final double y = vertices[v1][1];
      final double z = vertices[v1][2];
      a += x;
      b += y;
      c += z;
    }
    final double norm = 1.0 / face.length;
    return new double[] {a * norm, b * norm, c * norm};
  }

  /**
   * Gets the centroid.
   *
   * @return the centroid
   */
  public double[] getCentroid() {
    double[] c = centroid;
    if (centroid == null) {
      computeProperties();
      c = centroid;
    }
    return c.clone();
  }

  /**
   * Gets the surface area.
   *
   * @return the area
   */
  public double getArea() {
    double a = area;
    if (a == 0) {
      computeProperties();
      a = area;
    }
    return a;
  }

  /**
   * Gets the volume.
   *
   * @return the volume
   */
  public double getVolume() {
    double v = volume;
    if (v == 0) {
      computeProperties();
      v = volume;
    }
    return v;
  }

  private synchronized void computeProperties() {
    if (centroid != null) {
      return;
    }
    // Assume that we have at least 4 vertices and the hull has a non-zero volume.

    // https://www.cs.uaf.edu/2015/spring/cs482/lecture/02_20_boundary/centroid_2013_nurnberg.pdf
    // Assuming A triangles ordered counter clockwise Ai = (ai,bi,ci)
    // Outer unit normal ui = ni / |ni|, ni = (bi-ai) x (ci-ai)
    // Area Ai = 1/2 |ni|
    // Volume = 1/6 sum ( ai . ni )
    // (The volume is the projection of each triangle as a pyramid to the origin, some are added
    // and some will be subtracted. The centroid is the integral of all points x with the volume
    // divided by the volume.)
    final double[] normal = new double[3];
    final double[] point = new double[3];
    final double[] sumni = new double[3];
    double[] v1 = new double[3];
    double[] v2 = new double[3];
    final double[] ni = new double[3];
    final double[] c = new double[3];
    double a = 0;
    double v = 0;
    for (final int[] face : faces) {
      // Compute the plane normal and point on a plane so the properties correspond to the correct
      // plane orientation. This normal is used to orient each triangle of the face. The point
      // is projected to the origin by each triangle normal:
      // sum(dot(point, ni)) == dot(point, sum(ni))
      getPlane(face, normal, point);

      // The face may not be a triangle. We can split each into triangles using a fan approach
      // with the first point fixed.
      final double[] ai = vertices[face[0]];
      final double[] bi = vertices[face[1]];
      double[] ci = vertices[face[2]];
      vector(ai, bi, v1);
      vector(ai, ci, v2);
      cross(v1, v2, ni);
      copySign(ni, normal);
      System.arraycopy(ni, 0, sumni, 0, 3);
      a += norm(ni);
      for (int d = 0; d < 3; d++) {
        c[d] += ni[d] * (pow2(ai[d] + bi[d]) + pow2(bi[d] + ci[d]) + pow2(ci[d] + ai[d]));
      }
      // Process remaining triangles.
      // TODO - Fix this as it will not work for polygons which are not convex,
      // e.g. a partial/full doughnut.
      for (int k = 3; k < face.length; k++) {
        // Previous ci becomes bi so rotate the vectors to avoid recomputing v1 as (bi - ai)
        final double[] tmp = v1;
        v1 = v2;
        v2 = tmp;
        // Compute next vector
        ci = vertices[face[k]];
        vector(ai, ci, v2);
        cross(v1, v2, ni);
        copySign(ni, normal);
        add(ni, sumni, sumni);
        a += norm(ni);
        for (int d = 0; d < 3; d++) {
          c[d] += ni[d] * (pow2(ai[d] + bi[d]) + pow2(bi[d] + ci[d]) + pow2(ci[d] + ai[d]));
        }
      }
      v += dot(point, sumni);
    }
    volume = v / 6;
    area = a / 2;
    SimpleArrayUtils.multiply(c, 1.0 / (48.0 * volume));
    centroid = c;
  }

  /**
   * Compute the vector from point 1 to point 2 ({@code p2 - p1}) and store the result in {@code v}.
   *
   * @param p1 the first point
   * @param p2 the second point
   * @param v the result
   */
  private static void vector(double[] p1, double[] p2, double[] v) {
    v[0] = p2[0] - p1[0];
    v[1] = p2[1] - p1[1];
    v[2] = p2[2] - p1[2];
  }

  /**
   * Compute the addition of point 1 and point 2 ({@code p1 + p2}) and store the result in
   * {@code v}.
   *
   * @param p1 the first point
   * @param p2 the second point
   * @param v the result
   */
  private static void add(double[] p1, double[] p2, double[] v) {
    v[0] = p2[0] + p1[0];
    v[1] = p2[1] + p1[1];
    v[2] = p2[2] + p1[2];
  }

  /**
   * Compute the cross product of vector 1 and 2 and store the result in {@code n}.
   *
   * @param v1 the first vector
   * @param v2 the second vector
   * @param n the result
   */
  private static void cross(double[] v1, double[] v2, double[] n) {
    final double x = v1[1] * v2[2] - v1[2] * v2[1];
    final double y = v1[2] * v2[0] - v1[0] * v2[2];
    final double z = v1[0] * v2[1] - v1[1] * v2[0];
    n[0] = x;
    n[1] = y;
    n[2] = z;
  }

  /**
   * Compute the dot product of vector 1 and 2.
   *
   * @param v1 the first vector
   * @param v2 the second vector
   * @return the dot product
   */
  private static double dot(double[] v1, double[] v2) {
    return v1[0] * v2[0] + v1[1] * v2[1] + v1[2] * v2[2];
  }

  /**
   * Copy the sign of vector 2 to vector 1.
   *
   * @param v1 the first vector
   * @param v2 the second vector
   */
  private static void copySign(double[] v1, double[] v2) {
    v1[0] = Math.copySign(v1[0], v2[0]);
    v1[1] = Math.copySign(v1[1], v2[1]);
    v1[2] = Math.copySign(v1[2], v2[2]);
  }

  /**
   * Compute the L2 norm of the vector.
   *
   * @param v the vector
   * @return the L2 norm
   */
  private static double norm(double[] v) {
    return Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
  }

  /**
   * Get the argument to the power 2.
   *
   * @param value the value
   * @return value^2
   */
  private static double pow2(double value) {
    return value * value;
  }
}
