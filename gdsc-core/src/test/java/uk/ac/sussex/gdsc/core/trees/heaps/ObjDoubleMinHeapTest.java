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

package uk.ac.sussex.gdsc.core.trees.heaps;

import java.util.Arrays;
import java.util.TreeMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"javadoc"})
class ObjDoubleMinHeapTest {
  @Test
  void testEmpty() {
    final ObjDoubleMinHeap<Integer> heap = new ObjDoubleMinHeap<>(5);
    Assertions.assertEquals(0, heap.getSize());
    Assertions.assertEquals(5, heap.getCapacity());
    Assertions.assertEquals(Double.POSITIVE_INFINITY, heap.getThreshold());
    Assertions.assertThrows(IllegalStateException.class, () -> heap.remove(null));
  }

  @Test
  void testNaN() {
    final ObjDoubleMinHeap<Integer> heap = new ObjDoubleMinHeap<>(5);
    Assertions.assertEquals(0, heap.getSize());
    heap.offer(Double.NaN, 42);
    Assertions.assertEquals(0, heap.getSize());
  }

  @Test
  void testPartial() {
    final ObjDoubleMinHeap<Integer> heap = new ObjDoubleMinHeap<>(5);
    heap.offer(1.1, 100);
    heap.offer(3.1, 200);
    heap.offer(2.1, 300);
    Assertions.assertEquals(3, heap.getSize());
    Assertions.assertEquals(5, heap.getCapacity());
    Assertions.assertEquals(Double.POSITIVE_INFINITY, heap.getThreshold());

    final double[] distances = heap.getValues();
    final Integer[] values = heap.getItems(new Integer[0]);
    final Integer[] values2 = heap.getItems(new Integer[55]);
    Arrays.sort(distances);
    Arrays.sort(values);
    Arrays.sort(values2, 0, 3);
    Assertions.assertArrayEquals(new double[] {1.1, 2.1, 3.1}, distances);
    Assertions.assertArrayEquals(new Integer[] {100, 200, 300}, values);
    Assertions.assertArrayEquals(new Integer[] {100, 200, 300}, Arrays.copyOf(values2, 3));

    double obs = heap.remove((t, d) -> {
      Assertions.assertEquals(3.1, d);
      Assertions.assertEquals(200, t);
    });
    Assertions.assertEquals(2, heap.getSize());
    Assertions.assertEquals(3.1, obs);

    obs = heap.remove((t, d) -> {
      Assertions.assertEquals(2.1, d);
      Assertions.assertEquals(300, t);
    });
    Assertions.assertEquals(1, heap.getSize());
    Assertions.assertEquals(2.1, obs);

    // The final value should be left
    Assertions.assertEquals(1.1, heap.getValue(0));
    Assertions.assertEquals(100, heap.getItem(0));

    Assertions.assertEquals(1.1, heap.remove(null));
    Assertions.assertEquals(0, heap.getSize());
  }

  @Test
  void testFull() {
    final ObjDoubleMinHeap<Integer> heap = new ObjDoubleMinHeap<>(3);
    heap.offer(1.1, 100);
    heap.offer(3.1, 200);
    heap.offer(2.1, 300);
    heap.offer(0.1, 400);
    heap.offer(4.1, 500);
    Assertions.assertEquals(3, heap.getSize());
    Assertions.assertEquals(3, heap.getCapacity());
    Assertions.assertEquals(2.1, heap.getThreshold());

    final double[] distances = heap.getValues();
    final Integer[] values = heap.getItems(new Integer[0]);
    Arrays.sort(distances);
    Arrays.sort(values);
    Assertions.assertArrayEquals(new double[] {0.1, 1.1, 2.1}, distances);
    Assertions.assertArrayEquals(new Integer[] {100, 300, 400}, values);

    final TreeMap<Integer, Double> expected = new TreeMap<>();
    expected.put(100, 1.1);
    expected.put(300, 2.1);
    expected.put(400, 0.1);
    for (int i = 0; i < heap.getSize(); i++) {
      final Integer value = heap.getItem(i);
      final Double distance = heap.getValue(i);
      Assertions.assertEquals(expected.get(value), distance);
    }
  }

  /**
   * This test fills the heap in reverse size order. It hits execution points in the heap methods
   * that maintain the internal structure not hit by the other tests.
   */
  @Test
  void testFullReverseOrder() {
    final ObjDoubleMinHeap<Integer> heap = new ObjDoubleMinHeap<>(3);
    heap.offer(3.1, 100);
    heap.offer(2.1, 200);
    heap.offer(1.1, 300);
    heap.offer(0.1, 400);
    Assertions.assertEquals(3, heap.getSize());
    Assertions.assertEquals(3, heap.getCapacity());
    Assertions.assertEquals(2.1, heap.getThreshold());

    final double[] distances = heap.getValues();
    final Integer[] values = heap.getItems(new Integer[0]);
    Arrays.sort(distances);
    Arrays.sort(values);
    Assertions.assertArrayEquals(new double[] {0.1, 1.1, 2.1}, distances);
    Assertions.assertArrayEquals(new Integer[] {200, 300, 400}, values);

    final TreeMap<Integer, Double> expected = new TreeMap<>();
    expected.put(200, 2.1);
    expected.put(300, 1.1);
    expected.put(400, 0.1);
    for (int i = 0; i < heap.getSize(); i++) {
      final Integer value = heap.getItem(i);
      final Double distance = heap.getValue(i);
      Assertions.assertEquals(expected.get(value), distance);
    }
  }
}
