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
 * Copyright (C) 2011 - 2022 Alex Herbert
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

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.custom_hash.TObjectIntCustomHashMap;
import gnu.trove.set.hash.TIntHashSet;
import gnu.trove.strategy.HashingStrategy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;
import uk.ac.sussex.gdsc.core.data.VisibleForTesting;
import uk.ac.sussex.gdsc.core.math.GeometryUtils;
import uk.ac.sussex.gdsc.core.utils.BitFlagUtils;
import uk.ac.sussex.gdsc.core.utils.LocalList;
import uk.ac.sussex.gdsc.core.utils.MathUtils;
import uk.ac.sussex.gdsc.core.utils.SimpleArrayUtils;
import uk.ac.sussex.gdsc.core.utils.SortUtils;
import uk.ac.sussex.gdsc.core.utils.ValidationUtils;

/**
 * Contains a set of coordinates representing the hull of a set of points. This should be a
 * non-self-intersecting (simple) polyhedron, which can be convex or concave.
 *
 * <p>Computation of the volume and centroid of the hull requires the faces to be convex polygons
 * (which are split using a simple fan method to triangles).
 */
public final class Hull3d implements Hull {

  /** The precision epsilon of a double number (gap between 1.0 and the next higher number). */
  private static final double EPSILON = Math.ulp(1.0);

  /** Non-existing intersection of line and the plane. */
  private static final Integer NO_INDEX = -1;
  /** Forward direction. */
  private static final Integer FORWARD = 1;
  /** Backward (reverse) direction. */
  private static final Integer BACKWARD = -1;

  /** A line that has not been processed. */
  private static final int UNPROCESSED = 0x0;
  /** A line that has been visited. */
  private static final int VISITED = 0x1;
  /** A line that is part of a polygon. */
  private static final int POLYGON = 0x2;
  /** Marks a line as visited and a member of a polygon. */
  private static final int VISITED_POLYGON = VISITED | POLYGON;

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
   * A class to allow validation of face edges.
   */
  @VisibleForTesting
  static class Edge {
    /** The edge start. */
    final int from;
    /** The edge end. */
    final int to;

    /**
     * Create an instance.
     *
     * @param from the from
     * @param to the to
     */
    Edge(int from, int to) {
      this.from = from;
      this.to = to;
    }

    /**
     * Create an instance. The from and to vertices are placed in their natural order
     * {@code from < to}.
     *
     * @param from the from
     * @param to the to
     * @return the edge
     */
    static Edge create(int from, int to) {
      return (from < to) ? new Edge(from, to) : new Edge(to, from);
    }

    /**
     * Compare two edges.
     *
     * @param e1 the first edge
     * @param e2 the second edge
     * @return [-1, 0, 1]
     */
    static int compare(Edge e1, Edge e2) {
      final int result = Integer.compare(e1.from, e2.from);
      return result == 0 ? Integer.compare(e1.to, e2.to) : result;
    }

    // Note: We do not override equals as it is not used in the TreeMap. Only the comparator is used
    // to test equivalence.
  }

  /**
   * A class to allow traversal of edges using a mark to store processeing status.
   */
  @VisibleForTesting
  static class MarkedEdge extends Edge {
    /** The mark. */
    int mark;

    /**
     * Create an instance.
     *
     * @param from the from
     * @param to the to
     */
    MarkedEdge(int from, int to) {
      super(from, to);
    }

    /**
     * Create an instance. The from and to vertices are placed in their natural order
     * {@code from < to}.
     *
     * @param from the from
     * @param to the to
     * @return the edge
     */
    static MarkedEdge create(int from, int to) {
      return (from < to) ? new MarkedEdge(from, to) : new MarkedEdge(to, from);
    }
  }

  /**
   * A hashing strategy for numerically equivalent 3D points.
   */
  @VisibleForTesting
  static class PointHashingStrategy implements HashingStrategy<double[]> {
    /** An instance. */
    static final PointHashingStrategy INSTANCE = new PointHashingStrategy();

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    @Override
    public int computeHashCode(double[] object) {
      return 31 * (31 * (31 + hash(object[0])) + hash(object[1])) + hash(object[2]);
    }

    /**
     * Hash.
     *
     * @param d the d
     * @return the int
     */
    private static int hash(double d) {
      // Unsigned hash so that -0.0 == 0.0
      final long bits = Double.doubleToRawLongBits(d) & Long.MAX_VALUE;
      return (int) (bits ^ (bits >>> 32));
    }

    @Override
    public boolean equals(double[] o1, double[] o2) {
      // Numerical equivalence (-0.0 == 0.0)
      return o1[0] == o2[0] && o1[1] == o2[1] && o1[2] == o2[2];
    }
  }

  /**
   * A hashing strategy for edges.
   */
  @VisibleForTesting
  static class EdgeHashingStrategy implements HashingStrategy<Edge> {
    /** An instance. */
    static final EdgeHashingStrategy INSTANCE = new EdgeHashingStrategy();

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    @Override
    public int computeHashCode(Edge e) {
      return 31 * (31 + e.from) + e.to;
    }

    @Override
    public boolean equals(Edge o1, Edge o2) {
      return o1.from == o2.from && o1.to == o2.to;
    }
  }

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
   * <p>The edges are then extracted and a test performed to ensure each forward edge between two
   * vertices has a corresponding reverse edge.
   *
   * @param vertices the vertices
   * @param faces the faces (counter-clockwise ordering)
   * @return the convex hull
   * @throws NullPointerException if the inputs are null
   * @throws IllegalArgumentException if the array lengths are less than 4, if the coordinate
   *         lengths are not at least 3, if the faces lengths are not at least 3, if the indices of
   *         the faces do not reference a vertex, if the number of faces is too large, or if there
   *         are unpaired edges between two vertices.
   */
  public static Hull3d create(double[][] vertices, int[][] faces) {
    ValidationUtils.checkArgument(vertices.length >= 4, "vertices length");
    ValidationUtils.checkArgument(faces.length >= 4, "faces length");
    ValidationUtils.checkArgument(faces.length <= 2 * vertices.length - 4, "F > 2V - 4: F=%d, V=%d",
        faces.length, vertices.length);

    final int numberOfVertices = vertices.length;
    final double[][] v = new double[numberOfVertices][];
    for (int i = 0; i < numberOfVertices; i++) {
      v[i] = copyVertex(vertices[i]);
    }
    final int[][] f = new int[faces.length][];
    for (int i = 0; i < faces.length; i++) {
      f[i] = copyFace(faces[i], numberOfVertices);
    }

    // Note: Edge validation.
    // Each edge (E) is a consecutive pair on the face. There should be pairs of corresponding
    // half edges in opposite directions.
    // e.g. face {0, 1, 2} expects the opposite edges {1, 0}, {2, 1} and {0, 2} to be present
    // in the rest of the faces. So we build a map of the edges and a count of the direction.
    final TreeMap<Edge, int[]> set = new TreeMap<>(Edge::compare);
    for (final int[] face : f) {
      for (int i = face.length, i1 = 0; i-- > 0; i1 = i) {
        final int from = face[i];
        final int to = face[i1];
        // Orient the edge as forward or backward
        Edge e;
        int direction;
        if (from < to) {
          e = new Edge(from, to);
          direction = 1;
        } else {
          e = new Edge(to, from);
          direction = -1;
        }
        set.compute(e, (k, count) -> {
          if (count == null) {
            return new int[] {direction};
          }
          count[0] += direction;
          return count;
        });
      }
    }
    // All edges should be paired so the count is zero
    set.forEach((e, count) -> ValidationUtils.checkArgument(count[0] == 0,
        "Unpaired edges from vertex %d to %d", e.from, e.to));

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
   * {@code d} is the distance of the plane from the origin.
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
   * Gets the plane normal for the specified face into the given result arrays.
   *
   * <p>The plane normal is raw and should be normalised to unit length if required.
   *
   * @param face the face
   * @param normal the result
   * @param point the point
   */
  private void getPlane(int[] face, double[] normal) {
    // Use Newell's method to compute the normal N of the best fit plane of all the vertices.
    double a = 0;
    double b = 0;
    double c = 0;
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
    }
    normal[0] = a;
    normal[1] = b;
    normal[2] = c;
  }

  /**
   * Gets the plane normal for the specified face into the given result arrays.
   *
   * <p>The plane normal is raw and should be normalised to unit length if required.
   *
   * @param vertices the vertices
   * @param normal the result
   */
  @SuppressWarnings("unused")
  private static void getPlane(List<double[]> vertices, double[] normal) {
    // Use Newell's method to compute the normal N of the best fit plane of all the vertices.
    double a = 0;
    double b = 0;
    double c = 0;
    for (int i = vertices.size(), i1 = 0; i-- > 0; i1 = i) {
      final double[] p = vertices.get(i);
      final double[] p1 = vertices.get(i1);
      final double x = p[0];
      final double y = p[1];
      final double z = p[2];
      final double x1 = p1[0];
      final double y1 = p1[1];
      final double z1 = p1[2];
      a += (y - y1) * (z + z1);
      b += (z - z1) * (x + x1);
      c += (x - x1) * (y + y1);
    }
    normal[0] = a;
    normal[1] = b;
    normal[2] = c;
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

  /**
   * Compute properties.
   */
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
    // (The volume is the projection of each triangle as a (slanted) pyramid to the origin, some are
    // added and some will be subtracted. The centroid is the integral of all points x within the
    // volume divided by the volume.)
    final double[] normal = new double[3];
    final double[] point = new double[3];
    final double[] sumni = new double[3];
    final double[] v1 = new double[3];
    final double[] v2 = new double[3];
    final double[] ni = new double[3];
    final double[] c = new double[3];
    double a = 0;
    double v = 0;
    final LocalList<int[]> triangles = new LocalList<>();
    for (final int[] fullFace : faces) {
      // Compute the plane normal and point on a plane so the properties correspond to the correct
      // plane orientation. This normal is used to orient each triangle of the face. The point
      // is projected to the origin by each triangle normal.
      getPlane(fullFace, normal, point);

      // The face may not be a triangle.
      if (fullFace.length == 3) {
        final double[] ai = vertices[fullFace[0]];
        final double[] bi = vertices[fullFace[1]];
        final double[] ci = vertices[fullFace[2]];
        vector(ai, bi, v1);
        vector(ai, ci, v2);
        cross(v1, v2, ni);
        copySign(ni, normal);
        a += norm(ni);
        v += dot(point, ni);
        for (int d = 0; d < 3; d++) {
          c[d] += ni[d] * (pow2(ai[d] + bi[d]) + pow2(bi[d] + ci[d]) + pow2(ci[d] + ai[d]));
        }
      } else {
        // Reset the sum of the normals. This allows use of a single dot product.
        // sum(dot(point, ni)) == dot(point, sum(ni))
        sumni[0] = 0;
        sumni[1] = 0;
        sumni[2] = 0;
        for (final int[] face : triangulate(fullFace, triangles)) {
          final double[] ai = vertices[face[0]];
          final double[] bi = vertices[face[1]];
          final double[] ci = vertices[face[2]];
          vector(ai, bi, v1);
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
    }
    volume = v / 6;
    area = a / 2;
    SimpleArrayUtils.multiply(c, 1.0 / (48.0 * volume));
    centroid = c;
  }

  /**
   * Triangulate the face into triangles.
   *
   * @param face the face
   * @param triangles the triangles working list
   * @return the list of triangles
   */
  private static LocalList<int[]> triangulate(int[] face, LocalList<int[]> triangles) {

    // TODO: This should be updated to handle non convex polygons. E.g. using
    // a simple polygon to monotone polygon to triangle algorithm.
    // Fournier, A.; Montuno, D. Y. (1984),
    // "Triangulating simple polygons and equivalent problems",
    // ACM Transactions on Graphics, 3 (2): 153–174,
    // https://doi.org/10.1145%2F357337.357341

    triangles.ensureCapacity(face.length - 2);
    triangles.clear();
    // This works for convex polygons to create a triangle fan.
    // The fan is not optimal as it does not avoid small wedges.
    // The fan can be improved by selecting the common vertex as the vertex with
    // the largest angle. Here we just pick the first vertex.
    for (int k = 2; k < face.length; k++) {
      triangles.add(new int[] {face[0], face[k - 1], face[k]});
    }
    return triangles;
  }

  /**
   * Compute the vector from point {@code a} to point {@code b} and store the result in {@code v}.
   *
   * <pre>
   * v = b - a
   * </pre>
   *
   * @param a the first point
   * @param b the second point
   * @param v the result
   */
  private static void vector(double[] a, double[] b, double[] v) {
    v[0] = b[0] - a[0];
    v[1] = b[1] - a[1];
    v[2] = b[2] - a[2];
  }

  /**
   * Compute the addition of point {@code a} and point {@code b} and store the result in {@code v}.
   *
   * <pre>
   * v = a + b
   * </pre>
   *
   * @param a the first point
   * @param b the second point
   * @param v the result
   */
  private static void add(double[] a, double[] b, double[] v) {
    v[0] = b[0] + a[0];
    v[1] = b[1] + a[1];
    v[2] = b[2] + a[2];
  }

  /**
   * Compute the cross product of vector {@code a} and {@code b} and store the result in {@code n}.
   *
   * <pre>
   * n = a x b
   * n = -b x a
   * </pre>
   *
   * @param a the first vector
   * @param b the second vector
   * @param n the result
   */
  private static void cross(double[] a, double[] b, double[] n) {
    final double x = a[1] * b[2] - a[2] * b[1];
    final double y = a[2] * b[0] - a[0] * b[2];
    final double z = a[0] * b[1] - a[1] * b[0];
    n[0] = x;
    n[1] = y;
    n[2] = z;
  }

  /**
   * Compute the dot product of vector {@code a} and {@code b}.
   *
   * <pre>
   * a.b = a.x * b.x + a.y * b.y + a.z * b.z
   * </pre>
   *
   * @param a the first vector
   * @param b the second vector
   * @return the dot product
   */
  private static double dot(double[] a, double[] b) {
    return a[0] * b[0] + a[1] * b[1] + a[2] * b[2];
  }

  /**
   * Copy the sign of vector {@code b} to {@code a}.
   *
   * <pre>
   * sign(a) = sign(b)
   * </pre>
   *
   * @param a the first vector
   * @param b the second vector
   */
  private static void copySign(double[] a, double[] b) {
    a[0] = Math.copySign(a[0], b[0]);
    a[1] = Math.copySign(a[1], b[1]);
    a[2] = Math.copySign(a[2], b[2]);
  }

  /**
   * Compute the L2 norm of the vector.
   *
   * <pre>
   * {@code
   * sqrt(x^2 + y^2 + z^2)
   * }
   * </pre>
   *
   * @param a the vector
   * @return the L2 norm
   */
  private static double norm(double[] a) {
    return Math.sqrt(dot(a, a));
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

  /**
   * Gets the polygons created by the intersection with an unbounded plane {@code a,b,c,d}.
   *
   * <pre>
   * ax + by + cz + d = 0
   *
   * (X - P) . N = 0
   * </pre>
   *
   * <p>where {@code X = (x, y, z)}, {@code N = (a, b, c)} is the normal to the plane, and {@code P}
   * is a point on the plane. The plane is specified using Hessian normal form where {@code (a,b,c)}
   * is the plane normal and d is the distance offset of the plane from the origin.
   *
   * <p>Returns the vertices of the polygon in counter-clockwise order when viewed down the normal
   * of the cutting plane. The polygons are assumed to be closed by a connecting line between the
   * first and last coordinate in the polygon list.
   *
   * </p>The polygons will contain 3 or more coordinates. Note if the plane touches a vertex of the
   * hull (single coordinate) or an edge of a face (two vertex coordinates) these results are not
   * valid polygons and are ignored.
   *
   * <p>Warning: This method is valid only when the faces of the hull are convex polygons thus a
   * plane will cut each face a maximum of 2 times when the planes are not coincident. A convex hull
   * will be cut into exactly one convex polygon, or no polygons. The hull is not required to be
   * convex. A concave hull should be cut into polygons enclosing a volume of the hull. If the plane
   * cuts across a face composed of many polygons then these will be joined into the enclosing
   * polygon.
   *
   * @param plane the plane
   * @return the polygons (may be an empty list)
   * @throws IllegalArgumentException if the lengths of the plane normal is not 1.0
   * @throws IllegalStateException if the intersection of the plane and the hull cannot be computed
   *         due to an invalid hull (non-convex faces)
   */
  public List<List<double[]>> getPolygons(double[] plane) {
    // Validate Hessian normal form
    ValidationUtils.checkArgument(Math.abs(norm(plane) - 1.0) <= 2 * EPSILON,
        () -> String.format("Plane is not in Hessian normal form. Length = %s", norm(plane)));

    // Compute if any points are above and below the plane.
    // If true then the plane must cut the polyhedron.
    final double d = plane[3];
    // Dot product is the distance from the origin.
    // ax + by + cz + d = 0 => ax + by + cz = -d
    // Compute the negative dot product which will equal d if the vertex is in the plane.
    final double[] distances = Arrays.stream(vertices).mapToDouble(v -> -dot(v, plane)).toArray();
    final double[] limits = MathUtils.limits(distances);
    // Check if the plane cuts the hull
    if (!(limits[0] <= d && limits[1] >= d)) {
      return Collections.emptyList();
    }

    // For each face find the intersections.
    // Intersection is a point p and the edge it lies on (from, to).
    // Only compute one way and this can be mirrored for paired edges.

    // Use a set to store all unique intersection points encountered.
    // The intersections and lines are then defined using only indices to the unique points.
    final TObjectIntCustomHashMap<double[]> points =
        new TObjectIntCustomHashMap<>(PointHashingStrategy.INSTANCE);
    final TreeMap<Edge, Integer> edges = new TreeMap<>(Edge::compare);

    // Note: Convex or concave hulls
    //
    // If the hull is convex then a cut by a plane will be a single convex polygon.
    // If the plane is coincident with a face then the polygon is the face (or faces if
    // each face is composed of smaller polygons).
    //
    // If the hull is concave then a cut may be multiple polygons.
    // If the plane is coincident with a face then the polygons contain the face (or faces).

    // Multi pass algorithm for consistency.
    // Output is a series of closed polygons.
    //
    // 1: Compute all edge intersections with the plane.
    // If we cannot compute the intersection then put both points in the plane.
    //
    // 2: If any face has more than 2 points in the plane, put the entire
    // face in the plane and store the enclosed polygon.
    //
    // 3: Allocate intersection lines through the faces using
    // above (>), within (==), or below (<) the plane:
    //@formatter:off
    //
    //        p1>    p1==    p1<
    // p2>     -      p1      p
    // p2==   p2     p1+p2    p2
    // p2<     p      p1      -
    // where p is the computed intersection
    //
    // 4: Join lines into enclosed polygons.
    //
    // Store all lines as unprocessed. Hold all lines in memory for reuse.
    // Mark any lines in a coincident face as processed and save the polygon.
    // Start at unprocessed line. Mark all lines unvisited.
    // Attempt to wind a polygon back to the start marking lines as processed and visited.
    // Lines should close to polygons or throw an error if no more unvisited lines.
    // Junctions are chosen using a counter-clockwise winding
    // rule (orientated with the face normal) to choose the smallest angle.
    //
    // Speed-up. Identify vertices with a line count of 2 unprocessed lines.
    // This is an ideal start point.
    //
    // 5: Convert polygons to output vertices.
    //
    //@formatter:on

    // 1: compute intersections
    for (final int[] face : faces) {
      for (int i = face.length, i1 = 0; i-- > 0; i1 = i) {
        final Edge e = Edge.create(face[i], face[i1]);
        // Compute the intersection (point or line) of the edge
        edges.compute(e, (k, point) -> {
          if (point == null) {
            final double d1 = distances[k.from];
            final double d2 = distances[k.to];
            if (d1 >= d && d2 <= d || d2 >= d && d1 <= d) {
              // If the distances are opposite signs compute the intersection.
              final double[] intersection = new double[3];
              final int result = GeometryUtils.getIntersection3d(vertices[k.from], vertices[k.to],
                  plane, intersection);
              if (result == 0) {
                // Single intersection
                return addPoint(intersection, points);
              } else if (result == 1) {
                // The line segment does not cross the plane. Assume this is floating-point error
                // and one vertex is in the plane. Pick the closest one.
                final int compare = Double.compare(Math.abs(d1 - d), Math.abs(d2 - d));
                if (compare < 0) {
                  distances[k.from] = d;
                  return addPoint(vertices[k.from], points);
                } else if (compare > 0) {
                  distances[k.to] = d;
                  return addPoint(vertices[k.to], points);
                }
                // This is not expected:
                // Both equal distance from the plane and on either side.
                // Just fall through to add both in the plane.
              }
              // line parallel to the plane so add both vertices in the plane.
              distances[k.from] = d;
              distances[k.to] = d;
              addPoint(vertices[k.from], points);
              addPoint(vertices[k.to], points);
            }
            // No intersection
            return NO_INDEX;
          }
          return point;
        });
      }
    }

    // Lines through faces. These may not be unique. Lines are marked as part of a polygon
    // if a face in the plane adds all its edges.
    final TObjectIntCustomHashMap<MarkedEdge> markedLines =
        new TObjectIntCustomHashMap<>(EdgeHashingStrategy.INSTANCE);

    // If a plane cuts a face it creates a line. The cross product of the normals
    // denotes the direction of the line with respect to the face (inside/outside the hull).
    // Using n1 (plane normal) and n2 (face normal) the cross product (n1 x n2) points along the
    // counter-clockwise winding (CCW) of the line around a polygon. The dot product of the line
    // and the cross product should be positive to create the line direction.
    //
    // We store the line and the count of observed directions. If a line occurs the same number of
    // times in both directions then it cancels and is removed. Otherwise it is an edge of a
    // counter clockwise winding polygon.
    //
    // Faces in the plane are assumed to have CCW. The lines are added with CCW or the opposite
    // if the normal of the plane matches or is opposite to the face normal.
    // All faces in the plane should have paired edges. These can create a convex edge and the
    // line will be in the same direction twice. A concave edge will have two directions and thus
    // be removed. For triangulated faces this should have the effect of cancelling all internal
    // lines and leave the outer face lines to form a polygon.
    final double[] n2 = new double[3];
    final double[] cross = new double[3];
    final double[] v1 = new double[3];

    // 2: assign faces in the plane
    final BitSet bits = new BitSet(getNumberOfVertices());
    final boolean[] inPlane = new boolean[getNumberOfFaces()];
    for (int f = 0; f < faces.length; f++) {
      final int[] face = faces[f];
      bits.clear();
      for (final int v : face) {
        if (distances[v] == d) {
          bits.set(v);
        }
      }
      if (bits.cardinality() > 2) {
        // Face is in the plane
        inPlane[f] = true;
        for (final int v : face) {
          distances[v] = d;
        }

        // Compute direction
        getPlane(face, n2);
        final int direction = dot(plane, n2) < 0 ? BACKWARD : FORWARD;

        int to = points.get(vertices[face[face.length - 1]]);
        for (final int i : face) {
          final int from = to;
          to = points.get(vertices[i]);
          // Store the unique edge direction correctly as the edge may be flipped
          if (from < to) {
            markedLines.adjustOrPutValue(new MarkedEdge(from, to), direction, direction);
          } else {
            markedLines.adjustOrPutValue(new MarkedEdge(to, from), -direction, -direction);
          }
        }
      }
    }

    // 3: allocate lines for each face not in the plane.
    final TIntHashSet facePoints = new TIntHashSet(8);

    // Required to map point index back to coordinates.
    final int[] indices = points.values();
    final double[][] coordinates = points.keys(new double[points.size()][]);
    SortUtils.sortData(coordinates, indices, false, false);

    for (int f = 0; f < faces.length; f++) {
      if (inPlane[f]) {
        // Already processed
        continue;
      }
      // The edges should have 0, 1, or 2 intersection points with the plane.

      facePoints.clear();
      final int[] face = faces[f];
      for (int i = face.length, i1 = 0; i-- > 0; i1 = i) {
        final int from = face[i];
        final int to = face[i1];
        // Find vertices spanning the plane.
        final double d1 = distances[from];
        final double d2 = distances[to];
        if (d1 >= d && d2 <= d || d2 >= d && d1 <= d) {
          final Edge e = Edge.create(from, to);
          final Integer intersection = edges.get(e);
          // If no intersection then both points have been put in the plane.
          if (intersection == NO_INDEX) {
            facePoints.add(points.get(vertices[from]));
            facePoints.add(points.get(vertices[to]));
          } else {
            facePoints.add(intersection);
          }
        }
      }

      // If 1 then a single vertex must touch the plane. This is ignored.
      // If 2 then create a line.
      if (facePoints.size() == 2) {
        final int[] line = facePoints.toArray();
        final MarkedEdge e = MarkedEdge.create(line[0], line[1]);
        // Compute direction
        // Using n1 (plane normal) and n2 (face normal) the cross product (n1 x n2) points
        // along the counter-clockwise winding (CCW) of the line around a polygon. The dot
        // product of the line and the cross product should be positive to create the line
        // direction.
        getPlane(face, n2);
        cross(plane, n2, cross);
        vector(coordinates[e.from], coordinates[e.to], v1);
        final int direction = dot(cross, v1) < 0 ? BACKWARD : FORWARD;
        markedLines.adjustOrPutValue(e, direction, direction);
      } else if (facePoints.size() > 2) {
        // If 3+ then the face is not convex.
        // Currently we do not support concave faces in the hull properties method.
        //
        // For a concave face the points should be on a line. The dot product of the point
        // and the line is the distance along the line from the origin
        // (projecting 3D points to the line).
        // Sort by the distance and connect consecutive pairs.
        // This requires special handling of any vertex in the plane. If incoming and outgoing
        // lines are on the same side this is touching the plane. If incoming and outgoing
        // are on opposite sides then the vertex is cut by the plane and should remain.
        // With many edge cases it is simpler to not support this situation.
        throw new IllegalStateException(
            "Face is not convex. Number of intersections with the cutting plane: "
                + facePoints.size());
      }
    }

    // Extract the lines with a direction. Ignore those that have cancelled.
    final LocalList<MarkedEdge> lines = new LocalList<>(markedLines.size());
    markedLines.forEachEntry((k, v) -> {
      if (v >= FORWARD) {
        lines.push(k);
      } else if (v <= BACKWARD) {
        lines.push(new MarkedEdge(k.to, k.from));
      }
      return true;
    });

    // Output closed polygons. A polygon references stored points by index.
    // Note: Each polygon list duplicates the start and end point to distinguish a closed polygon
    // from an incomplete polygon.
    final List<int[]> polygons = new LocalList<>();

    // Process all lines.
    // At junctions use the smallest angle to create an enclosing polygon that should
    // use CCW around the inside of the hull.
    for (int index = lines.findIndex(e -> e.mark == UNPROCESSED); index != -1;
        index = lines.findIndex(e -> e.mark == UNPROCESSED)) {
      final int[] polygon = createPolygon(index, lines, coordinates, plane);
      polygons.add(polygon);
    }

    if (polygons.isEmpty()) {
      // This occurs when the cutting plane touches an edge but does not cut the hull
      return Collections.emptyList();
    }

    // Convert polygons back to output coordinates.
    final List<List<double[]>> poly = new ArrayList<>(polygons.size());
    for (final int[] polygon : polygons) {
      // Ignore repeated start/end
      final ArrayList<double[]> coords = new ArrayList<>(polygon.length - 1);
      for (int i = 1; i < polygon.length; i++) {
        coords.add(coordinates[polygon[i]]);
      }

      // Orientate polygons to the counter-clockwise order so they share the plane normal.
      // This is already done by the CCW winding when creating and joining lines.

      // final double[] facePlane = new double[3];
      // getPlane(coords, facePlane);
      // // angle = arccos(a.b / (|a| |b|)) => a.b / (|a| |b|) = cos(angle)
      // // Sign of the dot product: <0 => angle above 90; >0 => angle below 90
      // if (dot(plane, facePlane) < 0) {
      // Collections.reverse(coords);
      // }

      poly.add(coords);
    }

    return poly;
  }

  /**
   * Adds the point to the unique set of points and return the id.
   *
   * @param point the point
   * @param points the points
   * @return the id
   */
  private static int addPoint(double[] point, TObjectIntCustomHashMap<double[]> points) {
    return points.adjustOrPutValue(point, 0, points.size());
  }

  /**
   * Compare two 3D points. Uses the {@code <,>} operators for numerical equivalence.
   *
   * @param p1 the first point
   * @param p2 the second point
   * @return [-1, 0, 1]
   */
  static int compare(double[] p1, double[] p2) {
    for (int i = 0; i < 3; i++) {
      final int result = compare(p1[i], p2[i]);
      if (result != 0) {
        return result;
      }
    }
    return 0;
  }

  /**
   * Compare two doubles. Uses the {@code <,>} operators for numerical equivalence.
   *
   * @param p1 the first point
   * @param p2 the second point
   * @return [-1, 0, 1]
   */
  static int compare(double p1, double p2) {
    if (p1 < p2) {
      return -1;
    }
    if (p1 > p2) {
      return 1;
    }
    return 0;
  }

  /**
   * Creates a closed polygon starting at the given edge.
   *
   * <p>Lines are assumed to be directional using a CCW. Any junctions will select the smallest
   * angle oriented using the given plane.
   *
   * @param start the index of the starting edge
   * @param lines the lines
   * @param coordinates the coordinates
   * @param plane the plane
   * @return the polygon
   * @throws IllegalStateException if the edge cannot form a complete polygon
   */
  @VisibleForTesting
  static int[] createPolygon(int start, LocalList<MarkedEdge> lines, double[][] coordinates,
      double[] plane) {
    // Mark all lines unvisited.
    lines.forEach(e -> e.mark = BitFlagUtils.unset(e.mark, VISITED));

    // Start at the unprocessed line.
    MarkedEdge edge = lines.unsafeGet(start);
    edge.mark |= VISITED_POLYGON;

    // Attempt to wind a polygon back to the start marking lines as processed and visited.
    // Lines should close to polygons or throw an error if no more unvisited lines.
    // All lines are orientated using a counter-clockwise winding
    // rule (orientated with the current polygon face normal). Junctions with multiple candidates
    // choose the smallest angle.

    // Working list
    final LocalList<MarkedEdge> candidates = new LocalList<>(lines.size());
    final double[] v1 = new double[3];
    final double[] v2 = new double[3];
    final double[] normal = new double[3];
    // Initialise polygon
    final TIntArrayList polygon = new TIntArrayList();

    polygon.add(edge.from);
    polygon.add(edge.to);
    final int origin = polygon.getQuick(0);

    while (findCandidates(edge.to, lines, candidates)) {
      // If multiple candidates then select the one with the smallest angle
      edge = selectCandidate(candidates, edge.from, edge.to, coordinates, v1, v2, normal, plane);
      edge.mark |= VISITED_POLYGON;
      polygon.add(edge.to);

      // Check if back to the start
      if (edge.to == origin) {
        break;
      }

      // Note: It should not be possible with a correct set of CCW lines picked using the smallest
      // angle for the end to be anywhere in the current polygon. So we do not check this case.

      // final int index = polygon.indexOf(to);
      // polygon.add(to);
      // if (index >= 0) {
      // if (index == 0) {
      // // A completely closed polygon
      // break;
      // }
      //
      // // Close the internal polygon
      // final int len = polygon.size() - index;
      // polygons.add(polygon.toArray(index, len));
      // polygon.remove(index + 1, len - 1);
      // }
    }

    // Should not reach here with an empty list.
    // Check if closed, otherwise this is an open line and not from a correctly cut hull.
    if (polygon.getQuick(0) != polygon.getQuick(polygon.size() - 1)) {
      throw new IllegalStateException("Error connecting face lines to polygons");
    }

    return polygon.toArray();
  }

  /**
   * Find unprocessed candidates to connect the given index to any edge in the lines, using the from
   * index of the edge.
   *
   * @param index the index
   * @param lines the lines
   * @param candidates the candidates
   * @return true if candidates were found
   */
  private static boolean findCandidates(int index, LocalList<MarkedEdge> lines,
      LocalList<MarkedEdge> candidates) {
    candidates.clear();
    lines.forEach(e -> {
      // Must be unprocessed
      if (e.mark == UNPROCESSED && e.from == index) {
        candidates.push(e);
      }
    });
    return !candidates.isEmpty();
  }

  /**
   * Select a candidate. If there are multiple candidates then compute the candidate with the
   * smallest angle using the plane vector to orient the edges.
   *
   * <p>The plane vector should initially be set to NaN in position 0. This plane vector will be set
   * using the normal of the smallest angle when the first junction is encountered. In typical use
   * cases with only one candidate this functionality is not used.
   *
   * @param candidates the candidates
   * @param from the from
   * @param to the to
   * @param coordinates the coordinates
   * @param v1 the first vector
   * @param v2 the second vector
   * @param normal the normal
   * @param plane the plane
   * @return the marked edge
   */
  private static MarkedEdge selectCandidate(LocalList<MarkedEdge> candidates, int from, int to,
      double[][] coordinates, double[] v1, double[] v2, double[] normal, double[] plane) {
    if (candidates.size() == 1) {
      return candidates.unsafeGet(0);
    }

    // Choose smallest turn angle oriented using the plane.
    // The polygon should wind back to itself.

    // Compute the cosine of the angles
    final double[] cosAngle = new double[candidates.size()];
    final double[] ai = coordinates[from];
    final double[] bi = coordinates[to];
    vector(ai, bi, v1);
    SimpleArrayUtils.multiply(v1, 1.0 / norm(v1));

    for (int i = 0; i < cosAngle.length; i++) {
      final MarkedEdge e = candidates.unsafeGet(i);
      final double[] ci = coordinates[e.to];
      vector(ai, ci, v2);
      cross(v1, v2, normal);
      // cos(angle) = v1 . v2 / (|v1| |v2|)
      cosAngle[i] = dot(v1, v2) / norm(v2);

      // @formatter:off
      //      v2
      //   a --- c
      //    \   /
      //  v1 \ /
      //      b
      // @formatter:on
      //
      // if a->b->c turn counter clockwise the angle is 180 - 0 (dot [-1, 1])
      // if a->b->c is parallel the angle is 0 (dot == 1)
      // if a->b->c turn clockwise the angle is 0 - 180 (dot [1, -1])
      // but the cross product normal is opposite from the plane normal
      // We map the later case to [1, 3] to avoid obtuse angles

      // Orient using the plane
      // This maps obtuse angles (dot [1, -1]) to the range [1, 3].
      // The final range for all angles is [-1, 3] where the smallest is the chosen angle.
      if (dot(normal, plane) < 0) {
        cosAngle[i] = -cosAngle[i] + 2;
      }
    }

    return candidates.unsafeGet(SimpleArrayUtils.findMinIndex(cosAngle));
  }
}
