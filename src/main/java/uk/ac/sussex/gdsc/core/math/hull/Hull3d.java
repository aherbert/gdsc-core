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
  /** The faces. */
  private final int[][] faces;

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
   * Create a new hull from the given vertices and faces. The input arrays are copied.
   *
   * <p>No validation is performed to check each face is a plane, or that the faces cover the entire
   * surface of the hull.
   *
   * <p>A simple validation is performed to check that the number of faces ({@code F}) is at most
   * {@code F = 2V - 4} with {@code V} the number of vertices.
   *
   * @param vertices the vertices
   * @param faces the faces
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
}
