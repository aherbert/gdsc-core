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
public class Hull2dTest {
  @Test
  public void testCreateThrows() {
    final double[] x = {0, 1, 2};
    final double[] y = {};
    Assertions.assertThrows(NullPointerException.class, () -> Hull2d.create(null, x));
    Assertions.assertThrows(NullPointerException.class, () -> Hull2d.create(x, null));
    Assertions.assertThrows(IllegalArgumentException.class, () -> Hull2d.create(x, new double[1]));
    Assertions.assertThrows(IllegalArgumentException.class, () -> Hull2d.create(y, y));
  }

  @Test
  public void canCreateWithOnePoint() {
    final double[] x = new double[] {1.2345f};
    final Hull2d hull = Hull2d.create(x, x);
    Assertions.assertEquals(1, hull.getNumberOfVertices());
    Assertions.assertEquals(0, hull.getLength());
    Assertions.assertEquals(0, hull.getArea());
  }

  @Test
  public void canCreateWithTwoPoints() {
    final double[] x = new double[] {1.5f, 2.5f};
    final Hull2d hull = Hull2d.create(x, x);
    Assertions.assertEquals(2, hull.getNumberOfVertices());
    Assertions.assertEquals(2 * Math.sqrt(2), hull.getLength(), 1e-10);
    Assertions.assertEquals(0, hull.getArea());
  }

  @Test
  public void canCreateWithThreePoints() {
    final double[] x = new double[] {1, 2, 2};
    final double[] y = new double[] {1, 1, 2};
    final Hull2d hull = Hull2d.create(x, y);
    Assertions.assertEquals(3, hull.getNumberOfVertices());
    Assertions.assertEquals(2 + Math.sqrt(2), hull.getLength(), 1e-10);
    Assertions.assertEquals(0.5, hull.getArea(), 1e-10);
  }

  @Test
  public void canComputeLengthAndArea() {
    // Parallelogram
    double[] xvalues = new double[] {0, 10, 11, 1};
    double[] yvalues = new double[] {0, 0, 10, 10};
    Hull2d hull = Hull2d.create(xvalues, yvalues);
    Assertions.assertEquals(2 * 10 + 2 * Math.sqrt(1 * 1 + 10 * 10), hull.getLength(), 1e-6);
    Assertions.assertEquals(100, hull.getArea(), 1e-6);

    // Rotated square
    xvalues = new double[] {0, 10, 9, -1};
    yvalues = new double[] {0, 1, 11, 10};
    hull = Hull2d.create(xvalues, yvalues);
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
    hull = Hull2d.create(xvalues, yvalues);
    Assertions.assertEquals(2 * Math.PI * radius, hull.getLength(), 1e-2);
    Assertions.assertEquals(Math.PI * radius * radius, hull.getArea(), 1e-2);
  }

  @Test
  public void conComputeContains() {
    final double[] x = new double[] {0, 10, 11, 1};
    final double[] y = new double[] {0, 0, 10, 10};
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
