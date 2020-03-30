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

package uk.ac.sussex.gdsc.core.trees.heaps;

import java.util.Arrays;
import java.util.TreeSet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.core.trees.heaps.DoubleMinHeap;

@SuppressWarnings({"javadoc"})
public class DoubleHeapMinTest {
  @Test
  public void testEmpty() {
    final DoubleMinHeap heap = new DoubleMinHeap(5);
    Assertions.assertEquals(0, heap.getSize());
    Assertions.assertEquals(5, heap.getCapacity());
    Assertions.assertEquals(Double.POSITIVE_INFINITY, heap.getThreshold());
    Assertions.assertThrows(IllegalStateException.class, () -> heap.remove());
  }

  @Test
  public void testPartial() {
    final DoubleMinHeap heap = new DoubleMinHeap(5);
    heap.offer(1.1);
    heap.offer(3.1);
    heap.offer(2.1);
    Assertions.assertEquals(3, heap.getSize());
    Assertions.assertEquals(5, heap.getCapacity());
    Assertions.assertEquals(Double.POSITIVE_INFINITY, heap.getThreshold());

    final double[] distances = heap.getValues();
    Arrays.sort(distances);
    Assertions.assertArrayEquals(new double[] {1.1, 2.1, 3.1}, distances);

    double obs = heap.remove();
    Assertions.assertEquals(2, heap.getSize());
    Assertions.assertEquals(3.1, obs);

    obs = heap.remove();
    Assertions.assertEquals(1, heap.getSize());
    Assertions.assertEquals(2.1, obs);

    // The final value should be left
    Assertions.assertEquals(1.1, heap.getValue(0));
  }

  @Test
  public void testFull() {
    final DoubleMinHeap heap = new DoubleMinHeap(3);
    heap.offer(1.1);
    heap.offer(3.1);
    heap.offer(2.1);
    heap.offer(0.1);
    heap.offer(4.1);
    Assertions.assertEquals(3, heap.getSize());
    Assertions.assertEquals(3, heap.getCapacity());
    Assertions.assertEquals(2.1, heap.getThreshold());

    final double[] distances = heap.getValues();
    Arrays.sort(distances);
    Assertions.assertArrayEquals(new double[] {0.1, 1.1, 2.1}, distances);

    final TreeSet<Double> expected = new TreeSet<>();
    expected.add(1.1);
    expected.add(2.1);
    expected.add(0.1);
    for (int i = 0; i < heap.getSize(); i++) {
      final Double distance = heap.getValue(i);
      Assertions.assertTrue(expected.contains(distance));
    }
  }

  /**
   * This test fills the heap in reverse size order. It hits execution points in the heap methods
   * that maintain the internal structure not hit by the other tests.
   */
  @Test
  public void testFullReverseOrder() {
    final DoubleMinHeap heap = new DoubleMinHeap(3);
    heap.offer(3.1);
    heap.offer(2.1);
    heap.offer(1.1);
    heap.offer(0.1);
    Assertions.assertEquals(3, heap.getSize());
    Assertions.assertEquals(3, heap.getCapacity());
    Assertions.assertEquals(2.1, heap.getThreshold());

    final double[] distances = heap.getValues();
    Arrays.sort(distances);
    Assertions.assertArrayEquals(new double[] {0.1, 1.1, 2.1}, distances);

    final TreeSet<Double> expected = new TreeSet<>();
    expected.add(2.1);
    expected.add(1.1);
    expected.add(0.1);
    for (int i = 0; i < heap.getSize(); i++) {
      final Double distance = heap.getValue(i);
      Assertions.assertTrue(expected.contains(distance));
    }
  }
}
