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

package uk.ac.sussex.gdsc.core.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"javadoc"})
class IntFixedListTest {
  @Test
  void testAddGetSet() {
    final int capacity = 5;
    final IntFixedList list = new IntFixedList(capacity);
    Assertions.assertEquals(capacity, list.capacity());
    Assertions.assertEquals(0, list.size());
    for (int i = 0; i < capacity; i++) {
      list.add(i + 1);
      Assertions.assertEquals(i + 1, list.size());
      Assertions.assertEquals(i + 1, list.get(i));
      list.set(i, i + 7);
      Assertions.assertEquals(i + 7, list.get(i));
    }
  }

  @Test
  void testAddArray() {
    final IntFixedList list = new IntFixedList(10);
    final int[] data = {42, 7, 13};
    list.add(data);
    Assertions.assertEquals(3, list.size());
    for (int i = 0; i < list.size(); i++) {
      Assertions.assertEquals(data[i], list.get(i));
    }
    list.addValues(data);
    Assertions.assertEquals(6, list.size());
    for (int i = 0; i < list.size(); i++) {
      Assertions.assertEquals(data[i % 3], list.get(i));
    }
  }

  @Test
  void testAddIntFixedList() {
    final IntFixedList list = new IntFixedList(10);
    final IntFixedList list2 = new IntFixedList(10);
    final int[] data = {42, 7, 13};
    list2.add(data);

    list.add(list2);
    Assertions.assertEquals(3, list.size());
    for (int i = 0; i < list.size(); i++) {
      Assertions.assertEquals(data[i], list.get(i));
    }
    list.add(list2);
    Assertions.assertEquals(6, list.size());
    for (int i = 0; i < list.size(); i++) {
      Assertions.assertEquals(data[i % 3], list.get(i));
    }
  }

  @Test
  void testCopy() {
    final IntFixedList list = new IntFixedList(10);
    final int[] data = {42, 7, 13};
    list.add(data);

    final int[] dest = new int[5];
    list.copy(dest, 2);
    Assertions.assertArrayEquals(new int[] {0, 0, 42, 7, 13}, dest);
  }

  @Test
  void testClear() {
    final IntFixedList list = new IntFixedList(10);
    final int[] data = {42, 7, 13};
    list.add(data);
    Assertions.assertEquals(3, list.size());
    list.clear();
    Assertions.assertEquals(0, list.size());
  }

  @Test
  void testToArray() {
    final IntFixedList list = new IntFixedList(10);
    Assertions.assertArrayEquals(new int[0], list.toArray());
    final int[] data = {42, 7, 13};
    list.add(data);
    Assertions.assertArrayEquals(data, list.toArray());
  }

  @Test
  void testRemove() {
    final IntFixedList list = new IntFixedList(10);
    final int[] data = {42, 7, 13};
    list.add(data);
    list.remove(1);
    Assertions.assertArrayEquals(new int[] {42, 13}, list.toArray());
    Assertions.assertThrows(IndexOutOfBoundsException.class, () -> list.remove(-1));
    Assertions.assertThrows(IndexOutOfBoundsException.class, () -> list.remove(list.size()));
  }

  @Test
  void testRemoveIf() {
    final IntFixedList list = new IntFixedList(10);
    final int[] data = {42, 7, 13};
    list.add(data);
    list.removeIf(i -> i == 99);
    Assertions.assertArrayEquals(data, list.toArray());
    list.removeIf(i -> i == 42);
    Assertions.assertArrayEquals(new int[] {7, 13}, list.toArray());
    list.removeIf(i -> i != 42);
    Assertions.assertArrayEquals(new int[0], list.toArray());

    // Test with a bad predicate
    list.add(data);
    try {
      list.removeIf(i -> {
        if (i == 42) {
          return true;
        }
        throw new RuntimeException();
      });
    } catch (final RuntimeException expected) {
      // ignore
    }
    // The list is not corrupted
    Assertions.assertEquals(2, list.size());
    Assertions.assertArrayEquals(new int[] {7, 13}, list.toArray());
  }
}
