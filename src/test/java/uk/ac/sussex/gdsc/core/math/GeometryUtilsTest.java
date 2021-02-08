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

package uk.ac.sussex.gdsc.core.math;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.core.utils.SimpleArrayUtils;

/**
 * Test for {@link GeometryUtils}.
 */
@SuppressWarnings({"javadoc"})
class GeometryUtilsTest {
  @Test
  void canComputeTraingleArea() {
    Assertions.assertEquals(0.5, GeometryUtils.getArea(1, 1, 2, 1, 1, 2));
    Assertions.assertEquals(2.0, GeometryUtils.getArea(0, 0, 1, 2, 2, 0));
    Assertions.assertEquals(1.5, GeometryUtils.getArea(0, 0, 1, 2, 2, 1));
  }

  @Test
  void canComputeTraingleAreaWithZero() {
    Assertions.assertEquals(2.0, GeometryUtils.getArea(1, 2, 2, 0));
    Assertions.assertEquals(1.5, GeometryUtils.getArea(1, 2, 2, 1));
  }

  @Test
  void canComputeAreaFloat() {
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> GeometryUtils.getArea(new float[5], new float[4]));

    // Area is signed
    canComputeAreaFloat(0, true, 0, 0, 1, 0);
    canComputeAreaFloat(0.5, true, 0, 0, 1, 0, 1, 1);
    canComputeAreaFloat(-0.5, true, 0, 0, 1, 1, 1, 0);
    canComputeAreaFloat(0.5, false, 0, 0, 1, 1, 1, 0);

    canComputeAreaFloat(1, true, 0, 0, 1, 0, 1, 1, 0, 1);
  }

  private static void canComputeAreaFloat(double exp, boolean signed, float... vertices) {
    final float[] x = new float[vertices.length / 2];
    final float[] y = new float[x.length];
    for (int i = 0, j = 0; i < vertices.length; i += 2, j++) {
      x[j] = vertices[i];
      y[j] = vertices[i + 1];
    }
    double obs = GeometryUtils.getArea(x, y);
    if (!signed) {
      obs = Math.abs(obs);
    }
    Assertions.assertEquals(exp, obs, 1e-10);
  }

  @Test
  void canComputeAreaDouble() {
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> GeometryUtils.getArea(new double[5], new double[4]));

    // Area is signed
    canComputeAreaDouble(0, true, 0, 0, 1, 0);
    canComputeAreaDouble(0.5, true, 0, 0, 1, 0, 1, 1);
    canComputeAreaDouble(-0.5, true, 0, 0, 1, 1, 1, 0);
    canComputeAreaDouble(0.5, false, 0, 0, 1, 1, 1, 0);

    canComputeAreaDouble(1, true, 0, 0, 1, 0, 1, 1, 0, 1);
  }

  private static void canComputeAreaDouble(double exp, boolean signed, double... vertices) {
    final double[] x = new double[vertices.length / 2];
    final double[] y = new double[x.length];
    for (int i = 0, j = 0; i < vertices.length; i += 2, j++) {
      x[j] = vertices[i];
      y[j] = vertices[i + 1];
    }
    double obs = GeometryUtils.getArea(x, y);
    if (!signed) {
      obs = Math.abs(obs);
    }
    Assertions.assertEquals(exp, obs, 1e-10);
  }

  @Test
  void canComputeIntersection() {
    // no intersection
    canComputeIntersection(null, 0, 0, 1, 0, 0, 1, 1, 0.5);
    canComputeIntersection(null, 0, 0, 1, 0, 0.5, 1, 0.5, 0.5);
    canComputeIntersection(null, 0, 0, 1, 0, 0.5, -1, 0.5, -0.5);
    canComputeIntersection(null, 0, 0, 1, 0, -0.5, -1, -0.5, 1);
    canComputeIntersection(null, 0, 0, 1, 0, 1.5, -1, 1.5, 1);
    // parallel
    canComputeIntersection(null, 0, 0, 1, 0, 0, 1, 1, 1);
    canComputeIntersection(null, 0, 0, 0, 1, 1, 0, 1, 1);
    canComputeIntersection(null, 0, 0, 1, 1, 0, 1, 1, 2);
    canComputeIntersection(null, 0, 1, 1, 1, 0, 0, 1, 0);
    // intersection
    canComputeIntersection(new double[] {0.5, 0.5}, 0, 0, 1, 1, 1, 0, 0, 1);
    canComputeIntersection(new double[] {0, 0}, 0, 0, 1, 1, 0, 0, 0, 1);
    // end points touching
    canComputeIntersection(new double[] {1, 1}, 0, 0, 1, 1, 1, 1, 4, 2);
    // coincident
    canComputeIntersection(new double[] {0.5, 0.5}, 0, 0, 1, 1, 0.5, 0.5, 2, 2);
    canComputeIntersection(new double[] {0.25, 0.25}, 0, 0, 1, 1, -0.25, -0.25, 0.25, 0.25);
  }

  private static void canComputeIntersection(double[] exp, double x1, double y1, double x2,
      double y2, double x3, double y3, double x4, double y4) {
    final double[] obs = new double[2];
    final boolean result = GeometryUtils.getIntersection(x1, y1, x2, y2, x3, y3, x4, y4, obs);
    final boolean result2 = GeometryUtils.testIntersect(x1, y1, x2, y2, x3, y3, x4, y4);
    Assertions.assertEquals(result, result2);
    if (exp == null) {
      Assertions.assertFalse(result);
    } else {
      Assertions.assertTrue(result);
      Assertions.assertArrayEquals(exp, obs, 1e-10);
    }
  }

  @Test
  void canComputeIntersection3d() {
    // Simple line through XYZ
    canComputeIntersection3d(0, SimpleArrayUtils.newDoubleArray(3, Math.sqrt(0.25 / 3)),
        new double[] {0, 0, 0}, new double[] {1, 1, 1}, new double[] {1, 1, 1, -0.5});
    final double[] p = SimpleArrayUtils.newDoubleArray(3, Math.sqrt(1.0 / 3));
    canComputeIntersection3d(0, p, new double[] {0, 0, 0}, new double[] {1, 1, 1},
        new double[] {1, 1, 1, -1});
    // Start in the plane
    canComputeIntersection3d(0, p, p, new double[] {1, 1, 1}, new double[] {1, 1, 1, -1});
    // End in the plane
    canComputeIntersection3d(0, p, new double[] {0, 0, 0}, p, new double[] {1, 1, 1, -1});
    // Line below plane
    canComputeIntersection3d(1, null, new double[] {0, 0, 0}, new double[] {0.5, 0.5, 0.5},
        new double[] {1, 1, 1, -1});
    // Line above plane
    canComputeIntersection3d(1, null, new double[] {1, 1, 1}, new double[] {2, 2, 2},
        new double[] {1, 1, 1, -1});
    // Line parallel to plane
    canComputeIntersection3d(2, null, new double[] {0, 0, 0}, new double[] {0, 1, 0},
        new double[] {1, 0, 0, -0.5});
    // Line parallel to plane but inside the plane
    canComputeIntersection3d(2, null, new double[] {0, 0, 0}, new double[] {0, 1, 0},
        new double[] {1, 0, 0, 0});
    canComputeIntersection3d(2, null, new double[] {1, 1, 1}, new double[] {1, 2, 1},
        new double[] {1, 0, 0, -1});
  }

  private static void canComputeIntersection3d(int expResult, double[] exp, double[] p1,
      double[] p2, double[] plane) {
    // Normalise the plane for convenience
    double norm = 1.0 / Math.sqrt(plane[0] * plane[0] + plane[1] * plane[1] + plane[2] * plane[2]);
    for (int i = 0; i < 3; i++) {
      plane[i] *= norm;
    }
    final double[] obs = new double[3];
    final int result = GeometryUtils.getIntersection3d(p1, p2, plane, obs);
    Assertions.assertEquals(expResult, result);
    if (exp != null) {
      Assertions.assertArrayEquals(exp, obs, 1e-10);
    }
  }
}
