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
 * Copyright (C) 2011 - 2023 Alex Herbert
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

package uk.ac.sussex.gdsc.core.data.detection;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.core.utils.SimpleArrayUtils;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.rng.RngFactory;
import uk.ac.sussex.gdsc.test.utils.RandomSeed;

@SuppressWarnings({"javadoc"})
class DetectionGridTest {

  @Test
  void testSimpleDetectionGridConstrutor() {
    final Rectangle[] bounds = new Rectangle[3];
    bounds[0] = new Rectangle(0, 0, 10, 10);
    bounds[1] = new Rectangle(0, 5, 10, 5);
    bounds[2] = new Rectangle(5, 5, 5, 5);
    final SimpleDetectionGrid g = SimpleDetectionGrid.wrap(bounds);
    Assertions.assertEquals(bounds.length, g.size());
    Assertions.assertThrows(IllegalArgumentException.class, () -> new SimpleDetectionGrid(null));
  }

  @Test
  void testBinarySearchDetectionGridConstrutor() {
    final Rectangle[] bounds = new Rectangle[3];
    bounds[0] = new Rectangle(0, 0, 10, 10);
    bounds[1] = new Rectangle(0, 5, 10, 5);
    bounds[2] = new Rectangle(5, 5, 5, 5);
    final BinarySearchDetectionGrid g = new BinarySearchDetectionGrid(bounds);
    Assertions.assertEquals(bounds.length, g.size());
    Assertions.assertThrows(NullPointerException.class,
        () -> new BinarySearchDetectionGrid(null));
  }

  @Test
  void canDetectCollisionsUsingSimpleGrid() {
    final Rectangle[] bounds = new Rectangle[3];
    bounds[0] = new Rectangle(0, 0, 10, 10);
    bounds[1] = new Rectangle(0, 5, 10, 5);
    bounds[2] = new Rectangle(5, 5, 5, 5);
    final SimpleDetectionGrid g = new SimpleDetectionGrid(bounds);
    Assertions.assertFalse(g.isIncludeOuterEdge());
    Assertions.assertArrayEquals(new int[] {0}, g.find(0, 0));
    Assertions.assertArrayEquals(new int[] {0, 1, 2}, g.find(5, 5));
    Assertions.assertArrayEquals(ArrayUtils.EMPTY_INT_ARRAY, g.find(-5, 5));

    // Definition of insideness
    Assertions.assertArrayEquals(ArrayUtils.EMPTY_INT_ARRAY, g.find(10, 10));

    g.setIncludeOuterEdge(true);
    Assertions.assertTrue(g.isIncludeOuterEdge());
    Assertions.assertArrayEquals(new int[] {0}, g.find(0, 0));
    Assertions.assertArrayEquals(new int[] {0, 1, 2}, g.find(5, 5));
    Assertions.assertArrayEquals(ArrayUtils.EMPTY_INT_ARRAY, g.find(-5, 5));
    Assertions.assertArrayEquals(new int[] {0, 1, 2}, g.find(10, 10));
    Assertions.assertArrayEquals(ArrayUtils.EMPTY_INT_ARRAY, g.find(12, 12));
    Assertions.assertArrayEquals(ArrayUtils.EMPTY_INT_ARRAY, g.find(10, 12));
  }

  @Test
  void canFindIndicesUsingBinaryTreeGrid() {
    final double[] data = SimpleArrayUtils.newArray(10, 0, 1.0);
    int i1;
    int i2;
    for (int i = 0; i < data.length; i++) {
      i1 = BinarySearchDetectionGrid.findIndexUpToAndIncluding(data, data[i]);
      Assertions.assertEquals(i, i1);
      i1 = BinarySearchDetectionGrid.findIndexUpToAndIncluding(data, data[i] + 0.1);
      Assertions.assertEquals(i, i1);
      i1 = BinarySearchDetectionGrid.findIndexUpToAndIncluding(data, data[i] - 0.1);
      Assertions.assertEquals(i - 1, i1);

      i2 = BinarySearchDetectionGrid.findIndexIncludingAndAfter(data, data[i]);
      Assertions.assertEquals(i, i2);
      i2 = BinarySearchDetectionGrid.findIndexIncludingAndAfter(data, data[i] - 0.1);
      Assertions.assertEquals(i, i2);
      i2 = BinarySearchDetectionGrid.findIndexIncludingAndAfter(data, data[i] + 0.1);
      Assertions.assertEquals(i + 1, i2);

      i2 = BinarySearchDetectionGrid.findIndexAfter(data, data[i]);
      Assertions.assertEquals(i + 1, i2);
      i2 = BinarySearchDetectionGrid.findIndexAfter(data, data[i] - 0.1);
      Assertions.assertEquals(i, i2);
      i2 = BinarySearchDetectionGrid.findIndexAfter(data, data[i] + 0.1);
      Assertions.assertEquals(i + 1, i2);
    }

    // Handle identity by testing with duplicates
    for (int i = 0; i < data.length; i++) {
      data[i] = i / 2;
    }

    for (int i = 0; i < data.length; i++) {
      i1 = BinarySearchDetectionGrid.findIndexUpToAndIncluding(data, data[i]);
      Assertions.assertEquals(i + (i + 1) % 2, i1);
      i1 = BinarySearchDetectionGrid.findIndexUpToAndIncluding(data, data[i] + 0.1);
      Assertions.assertEquals(i + (i + 1) % 2, i1);
      i1 = BinarySearchDetectionGrid.findIndexUpToAndIncluding(data, data[i] - 0.1);
      Assertions.assertEquals(i - i % 2 - 1, i1);

      i2 = BinarySearchDetectionGrid.findIndexIncludingAndAfter(data, data[i]);
      Assertions.assertEquals(i - i % 2, i2);
      i2 = BinarySearchDetectionGrid.findIndexIncludingAndAfter(data, data[i] - 0.1);
      Assertions.assertEquals(i - i % 2, i2);
      i2 = BinarySearchDetectionGrid.findIndexIncludingAndAfter(data, data[i] + 0.1);
      Assertions.assertEquals(i - i % 2 + 2, i2);

      i2 = BinarySearchDetectionGrid.findIndexAfter(data, data[i]);
      Assertions.assertEquals(i - i % 2 + 2, i2);
      i2 = BinarySearchDetectionGrid.findIndexAfter(data, data[i] - 0.1);
      Assertions.assertEquals(i - i % 2, i2);
      i2 = BinarySearchDetectionGrid.findIndexAfter(data, data[i] + 0.1);
      Assertions.assertEquals(i - i % 2 + 2, i2);
    }
  }

  @Test
  void canDetectCollisionsUsingBinaryTreeGrid() {
    final Rectangle[] r = new Rectangle[3];
    r[0] = new Rectangle(0, 0, 10, 10);
    r[1] = new Rectangle(0, 5, 10, 5);
    r[2] = new Rectangle(5, 5, 5, 5);
    final BinarySearchDetectionGrid g = new BinarySearchDetectionGrid(r);
    Assertions.assertArrayEquals(new int[] {0}, g.find(0, 0));
    Assertions.assertArrayEquals(new int[] {0, 1, 2}, g.find(5, 5));
    Assertions.assertArrayEquals(ArrayUtils.EMPTY_INT_ARRAY, g.find(-5, 5));

    // Respect the insideness definition
    Assertions.assertArrayEquals(ArrayUtils.EMPTY_INT_ARRAY, g.find(10, 10));
  }

  @SeededTest
  void canDetectTheSameCollisions(RandomSeed seed) {
    final int size = 512;
    final UniformRandomProvider rdg = RngFactory.create(seed.get());
    final Rectangle2D[] r = generateRectangles(rdg, 1000, size);

    final SimpleDetectionGrid g1 = new SimpleDetectionGrid(r);
    final BinarySearchDetectionGrid g2 = new BinarySearchDetectionGrid(r);

    final double[][] points = generatePoints(rdg, 500, size);

    for (final double[] p : points) {
      final int[] e = g1.find(p[0], p[1]);
      final int[] o = g2.find(p[0], p[1]);
      Arrays.sort(e);
      Arrays.sort(o);
      // TestLog.debugln(logger,Arrays.toString(e));
      // TestLog.debugln(logger,Arrays.toString(o));
      Assertions.assertArrayEquals(e, o);
    }
  }

  private static Rectangle2D[] generateRectangles(UniformRandomProvider rdg, int n, int size) {
    final Rectangle2D[] r = new Rectangle2D[n];
    final double[][] p1 = generatePoints(rdg, n, size);
    final double[][] p2 = generatePoints(rdg, n, size);
    for (int i = 0; i < r.length; i++) {
      double x1 = p1[i][0];
      double x2 = p1[i][1];
      double y1 = p2[i][0];
      double y2 = p2[i][1];
      if (x2 < x1) {
        final double tmp = x2;
        x2 = x1;
        x1 = tmp;
      }
      if (y2 < y1) {
        final double tmp = y2;
        y2 = y1;
        y1 = tmp;
      }
      r[i] = new Rectangle2D.Double(x1, y1, x2 - x1, y2 - y1);
    }
    return r;
  }

  private static double[][] generatePoints(UniformRandomProvider rdg, int n, int size) {
    final double[][] x = new double[n][];
    while (n-- > 0) {
      x[n] = new double[] {rdg.nextInt(size), rdg.nextInt(size)};
    }
    return x;
  }
}
