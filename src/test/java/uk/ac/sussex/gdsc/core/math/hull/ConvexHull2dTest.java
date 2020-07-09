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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"javadoc"})
public class ConvexHull2dTest {
  @Test
  public void cannotComputeConvexHullFromNoCoords() {
    final double[] x = new double[] {};
    final double[] y = new double[] {};
    final ConvexHull2d hull = ConvexHull2d.create(x, y);
    Assertions.assertNull(hull);
  }

  @Test
  public void canComputeConvexHullFromSquare() {
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
      final ConvexHull2d hull = ConvexHull2d.create(x, y);
      check(ex, ey, hull);
    }
  }

  @Test
  public void canComputeConvexHullFromSquareWithInternalPoint() {
    final double[] x = new double[] {0, 0, 10, 10, 5};
    final double[] y = new double[] {0, 10, 10, 0, 5};
    final double[] ex = new double[] {0, 10, 10, 0};
    final double[] ey = new double[] {0, 0, 10, 10};
    final ConvexHull2d hull = ConvexHull2d.create(x, y);
    check(ex, ey, hull);
  }

  @Test
  public void canComputeConvexHullFromSquareWithInternalPoint2() {
    final double[] x = new double[] {0, 0, 5, 10, 10};
    final double[] y = new double[] {0, 10, 5, 10, 0};
    final double[] ex = new double[] {0, 10, 10, 0};
    final double[] ey = new double[] {0, 0, 10, 10};
    final ConvexHull2d hull = ConvexHull2d.create(x, y);
    check(ex, ey, hull);
  }

  private static void check(double[] ex, double[] ey, ConvexHull2d hull) {
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
  public void canBuildWithNoPoints() {
    Assertions.assertNull(ConvexHull2d.newBuilder().build());
  }

  @Test
  public void canBuilderWithOnePoint() {
    final double[] x = new double[] {1.2345, 6.78};
    final ConvexHull2d hull = ConvexHull2d.newBuilder().add(x).build();
    Assertions.assertEquals(1, hull.getNumberOfVertices());
    Assertions.assertEquals(2, hull.dimensions());
    Assertions.assertTrue(hull.getLength() == 0);
    Assertions.assertTrue(hull.getArea() == 0);
  }

  @Test
  public void canClearBuilder() {
    final ConvexHull2d.Builder builder = ConvexHull2d.newBuilder();
    builder.add(1, 2);
    final ConvexHull2d hull1 = builder.build();
    Assertions.assertNotNull(hull1);
    final ConvexHull2d hull2 = builder.build();
    Assertions.assertNotNull(hull2);
    Assertions.assertNotSame(hull1, hull2);
    builder.clear();
    final ConvexHull2d hull3 = builder.build();
    Assertions.assertNull(hull3);
  }

  @Test
  public void canCreateWithNoPoints() {
    final double[] x = new double[0];
    Assertions.assertNull(ConvexHull2d.create(x, x));
  }

  @Test
  public void canCreateWithOnePoint() {
    final double[] x = new double[] {1.2345f};
    final ConvexHull2d hull = ConvexHull2d.create(x, x);
    Assertions.assertEquals(1, hull.getNumberOfVertices());
    Assertions.assertTrue(hull.getLength() == 0);
    Assertions.assertTrue(hull.getArea() == 0);
  }

  @Test
  public void canCreateWithTwoPoints() {
    final double[] x = new double[] {1.5f, 2.5f};
    final ConvexHull2d hull = ConvexHull2d.create(x, x);
    Assertions.assertEquals(2, hull.getNumberOfVertices());
    Assertions.assertEquals(2 * Math.sqrt(2), hull.getLength(), 1e-10);
    Assertions.assertTrue(hull.getArea() == 0);
  }

  @Test
  public void canCreateWithThreePoints() {
    final double[] x = new double[] {1, 2, 2};
    final double[] y = new double[] {1, 1, 2};
    final ConvexHull2d hull = ConvexHull2d.create(x, y);
    Assertions.assertEquals(3, hull.getNumberOfVertices());
    Assertions.assertEquals(2 + Math.sqrt(2), hull.getLength(), 1e-10);
    Assertions.assertEquals(hull.getArea(), 0.5, 1e-10);
  }

  @Test
  public void canComputeLengthAndArea() {
    // Parallelogram
    double[] xvalues = new double[] {0, 10, 11, 1};
    double[] yvalues = new double[] {0, 0, 10, 10};
    ConvexHull2d hull = ConvexHull2d.create(xvalues, yvalues);
    Assertions.assertEquals(2 * 10 + 2 * Math.sqrt(1 * 1 + 10 * 10), hull.getLength(), 1e-6);
    Assertions.assertEquals(100, hull.getArea(), 1e-6);

    // Rotated square
    xvalues = new double[] {0, 10, 9, -1};
    yvalues = new double[] {0, 1, 11, 10};
    hull = ConvexHull2d.create(xvalues, yvalues);
    final double edgeLengthSquared = 1 * 1 + 10 * 10;
    Assertions.assertEquals(4 * Math.sqrt(edgeLengthSquared), hull.getLength(), 1e-6);
    Assertions.assertEquals(edgeLengthSquared, hull.getArea(), 1e-6);

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
    hull = ConvexHull2d.create(xvalues, yvalues);
    Assertions.assertEquals(2 * Math.PI * radius, hull.getLength(), 1e-2);
    Assertions.assertEquals(Math.PI * radius * radius, hull.getArea(), 1e-2);
  }

  @Test
  public void conComputeContains() {
    final double[] x = new double[] {0, 10, 11, 1};
    final double[] y = new double[] {0, 0, 10, 10};
    final ConvexHull2d hull = ConvexHull2d.create(x, y);
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
