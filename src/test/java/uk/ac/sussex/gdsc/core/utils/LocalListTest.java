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

package uk.ac.sussex.gdsc.core.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.SplittableRandom;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntBiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.test.utils.functions.IndexSupplier;

/**
 * Test the {@link LocalList}. Ensure the implementation of the {@link List} API is correct.
 */
@SuppressWarnings({"javadoc"})
public class LocalListTest {

  /**
   * Creates a list from the data. This is fully working list implementation.
   *
   * @param data the data
   * @return the list
   */
  @SafeVarargs
  private static <T> List<T> createList(T... data) {
    // Just append to an ArrayList.
    final ArrayList<T> list = new ArrayList<>(data.length);
    for (final T t : data) {
      list.add(t);
    }
    return list;
  }

  /**
   * Assert two lists are equal using only {@link List#size()} and {@link List#get(int)}.
   *
   * @param <E> the element type
   * @param l1 List 1
   * @param l2 List 2
   */
  private static <E> void assertListEquals(List<E> l1, List<E> l2) {
    assertListEquals(l1, 0, l1.size(), l2);
  }

  /**
   * Assert two lists are equal using only {@link List#size()} and {@link List#get(int)}. The second
   * list must be the same as the range from the first list
   *
   * @param <E> the element type
   * @param l1 List 1
   * @param from Start index of the range (inclusive)
   * @param to End index of the range (exclusive)
   * @param l2 List 2
   */
  private static <E> void assertListEquals(List<E> l1, int from, int to, List<E> l2) {
    final int size = to - from;
    Assertions.assertEquals(size, l2.size(), "Size differs");
    final IndexSupplier msg = new IndexSupplier(1).setMessagePrefix("Element at ");
    for (int i = from; i < to; i++) {
      Assertions.assertEquals(l1.get(i), l2.get(i - from), msg.set(0, i));
    }
  }

  /**
   * Null-safe compare of two Integers.
   *
   * @param i the i
   * @param j the j
   * @return -1, 0 or 1
   * @see Integer#compare(int, int)
   */
  private static int compare(Integer i, Integer j) {
    // Send null to the end of the sorted list
    if (i == null) {
      return 1;
    }
    if (j == null) {
      return -1;
    }
    return Integer.compare(i, j);
  }

  @Test
  public void testConstructorDefaultCapacity() {
    final LocalList<Integer> list = new LocalList<>();
    Assertions.assertTrue(list.getCapacity() > 0, "Default capacity should not be zero");
    Assertions.assertTrue(list.isEmpty());
    Assertions.assertEquals(0, list.size());
  }

  @Test
  public void testConstructorWithCapacity() {
    for (final int c : new int[] {0, 1, 7, 45}) {
      final LocalList<Integer> list = new LocalList<>(c);
      Assertions.assertEquals(c, list.getCapacity());
      Assertions.assertTrue(list.isEmpty());
      Assertions.assertEquals(0, list.size());
    }
  }

  @Test
  public void testConstructorWithCollection() {
    final List<Integer> c = createList(2, 5, 13);
    final LocalList<Integer> list = new LocalList<>(c);
    Assertions.assertFalse(list.isEmpty());
    Assertions.assertEquals(c.size(), list.size());
    for (int i = 0; i < c.size(); i++) {
      Assertions.assertEquals(c.get(i), list.get(i));
    }
  }

  @Test
  public void testConstructorWithEmptyCollection() {
    final LocalList<Integer> list = new LocalList<>(Collections.emptyList());
    Assertions.assertTrue(list.isEmpty());
  }

  @Test
  public void testConstructorWithCollectionWithBadToArray() {
    final Integer[] contents = {13, 42};
    final ArrayList<Integer> badList = new ArrayList<Integer>() {
      private static final long serialVersionUID = 1L;

      @Override
      public Object[] toArray() {
        // Not an Object[] array
        return contents;
      }
    };
    final LocalList<Integer> list = new LocalList<>(badList);
    Assertions.assertEquals(2, list.size());
    for (int i = 0; i < contents.length; i++) {
      Assertions.assertEquals(contents[i], list.get(i));
    }
    // We can add more
    list.add(99);
    Assertions.assertEquals(3, list.size());
    Assertions.assertEquals(99, list.get(2));
    // We can set an existing value
    list.set(1, 67);
    Assertions.assertEquals(67, list.get(1));
  }

  @Test
  public void testCopy() {
    // Empty
    final LocalList<Integer> list0 = new LocalList<>();
    LocalList<Integer> list2 = list0.copy();
    Assertions.assertNotSame(list0, list2);
    Assertions.assertEquals(list0.size(), list2.size());

    // With elements
    final List<Integer> c = createList(2, 5, 13, 42, 99);
    final LocalList<Integer> list1 = new LocalList<>(c);
    list2 = list1.copy();
    Assertions.assertNotSame(list1, list2);
    Assertions.assertEquals(list1.size(), list2.size());
    for (int i = 0; i < list1.size(); i++) {
      Assertions.assertEquals(list1.get(i), list2.get(i));
    }
  }

  @Test
  public void testCopyOfRange() {
    final List<Integer> c = createList(2, 5, 13, 42, 99);
    final LocalList<Integer> list1 = new LocalList<>(c);

    final int from = 2;
    final int to = 4;
    LocalList<Integer> list2 = list1.copyOfRange(from, to);
    Assertions.assertEquals(to - from, list2.size());
    for (int i = from; i < to; i++) {
      Assertions.assertEquals(list1.get(i), list2.get(i - from));
    }

    // Copy empty range
    list2 = list1.copyOfRange(from, from);
    Assertions.assertEquals(0, list2.size());

    // Bad range
    Assertions.assertThrows(IndexOutOfBoundsException.class, () -> list1.copyOfRange(-1, to));
    Assertions.assertThrows(IndexOutOfBoundsException.class,
        () -> list1.copyOfRange(from, list1.size() + 1));
    Assertions.assertThrows(IllegalArgumentException.class, () -> list1.copyOfRange(to, from));
  }

  @Test
  public void testTrimToSize() {
    LocalList<Integer> list = new LocalList<>();
    Assertions.assertTrue(list.getCapacity() > 1);
    list.trimToSize();
    Assertions.assertEquals(0, list.getCapacity());

    list = new LocalList<>(5);
    Assertions.assertEquals(5, list.getCapacity());
    list.add(1);
    list.add(42);
    Assertions.assertEquals(5, list.getCapacity());
    list.trimToSize();
    Assertions.assertEquals(2, list.getCapacity());
    // Second call should be ignored
    list.trimToSize();
    Assertions.assertEquals(2, list.getCapacity());
  }

  @Test
  public void testTruncate() {
    final List<Integer> c = createList(2, 5, 13);
    final LocalList<Integer> list = new LocalList<>(c);
    Assertions.assertEquals(3, list.size());
    for (int i = 0; i < c.size(); i++) {
      Assertions.assertEquals(c.get(i), list.get(i));
    }

    // Ignore argument too big
    list.truncate(20);
    Assertions.assertEquals(3, list.size());

    final int capacity = list.getCapacity();
    list.truncate(2);
    Assertions.assertEquals(capacity, list.getCapacity(), "Truncate should not alter capacity");
    Assertions.assertEquals(2, list.size());

    // Check we clear the rest of the list
    Assertions.assertNull(list.unsafeGet(2));

    list.truncate(0);
    Assertions.assertEquals(capacity, list.getCapacity(), "Truncate should not alter capacity");
    Assertions.assertEquals(0, list.size());

    // Check we clear the rest of the list references
    Assertions.assertNull(list.unsafeGet(0));
    Assertions.assertNull(list.unsafeGet(1));
  }

  @Test
  public void testEnsureCapacity() {
    final LocalList<Integer> list = new LocalList<>();
    final int capacity = list.getCapacity();
    list.ensureCapacity(capacity * 2);
    Assertions.assertEquals(capacity * 2, list.getCapacity());
    list.ensureCapacity(capacity * 10);
    Assertions.assertEquals(capacity * 10, list.getCapacity());
    list.ensureCapacity(capacity * 5);
    Assertions.assertEquals(capacity * 10, list.getCapacity(), "Capacity should not decrease");
  }

  /**
   * Test the method to create a new capacity. Explicitly testing by adding items requires a lot of
   * RAM so we instead expose the method that computes the new capacity and test that.
   *
   * <p>This is a rigid test that enforces the default memory capacity behaviour to start at 11 and
   * increase by 50% each reallocation. The test may be modified in the future for different memory
   * allocation scheme.
   */
  @Test
  public void testCreateNewCapacity() {
    // Start from the default
    int capacity = 11;
    for (;;) {
      // Capacity to increase by 50%
      final long newCapacity = (capacity * 3L) / 2;
      if (newCapacity > Integer.MAX_VALUE) {
        break;
      }
      Assertions.assertEquals(newCapacity, LocalList.createNewCapacity(capacity + 1, capacity));
      capacity = (int) newCapacity;
    }

    // Now at the point where we cannot increase by 50%
    Assertions.assertEquals(1_992_174_387, capacity);

    final int safeMaxCapacity = Integer.MAX_VALUE - 8;
    Assertions.assertEquals(safeMaxCapacity, LocalList.createNewCapacity(capacity + 1, capacity));
    // Approach max value in single step increments
    for (int i = 1; i <= 8; i++) {
      Assertions.assertEquals(safeMaxCapacity + i,
          LocalList.createNewCapacity(safeMaxCapacity + i, safeMaxCapacity));
      Assertions.assertEquals(safeMaxCapacity + i,
          LocalList.createNewCapacity(safeMaxCapacity + i, safeMaxCapacity + i - 1));
    }

    Assertions.assertThrows(OutOfMemoryError.class,
        () -> LocalList.createNewCapacity(1 + Integer.MAX_VALUE, 10));
    Assertions.assertThrows(OutOfMemoryError.class,
        () -> LocalList.createNewCapacity(1 + Integer.MAX_VALUE, safeMaxCapacity));
    Assertions.assertThrows(OutOfMemoryError.class,
        () -> LocalList.createNewCapacity(1 + Integer.MAX_VALUE, Integer.MAX_VALUE));
  }

  @Test
  public void testAddTooLarge() {
    Object[] big = null;
    try {
      big = new Object[Integer.MAX_VALUE - 8];
    } catch (final OutOfMemoryError allowed) {
      Assumptions.assumeTrue(false, "Not enough memory for the test");
    }

    // Fill this so the big collection will not fit.
    final LocalList<Object> list = new LocalList<>();
    for (int i = 0; i < 9; i++) {
      list.add(i);
    }

    // Create a dummy collection that will return the big array directly.
    // This avoids duplicating the array and double the memory cost.
    final Object[] data = big;
    final ArrayList<Object> bigList = new ArrayList<Object>() {
      private static final long serialVersionUID = 1L;

      @SuppressWarnings("null")
      @Override
      public int size() {
        return data.length;
      }

      @Override
      public Object[] toArray() {
        return data;
      }
    };

    Assertions.assertThrows(OutOfMemoryError.class, () -> list.addAll(bigList));
  }

  @Test
  public void testUnsafeAccess() {
    final LocalList<Integer> list = new LocalList<>(10);
    final Integer element = 42;
    final int index = 5;
    list.unsafeSet(index, element);
    Assertions.assertEquals(0, list.size(), "Out-of-range element should be ignored by the list");
    Assertions.assertEquals(element, list.unsafeGet(index));
    list.unsafeSet(index, null);
    Assertions.assertNull(list.unsafeGet(index));
  }

  @Test
  public void testPushPop() {
    final LocalList<Integer> list = new LocalList<>(2);
    final Integer element1 = 42;
    final Integer element2 = 99;
    list.push(element1);
    list.push(element2);
    Assertions.assertEquals(2, list.size());

    // No more capacity
    Assertions.assertThrows(IndexOutOfBoundsException.class, () -> list.push(456));

    Assertions.assertEquals(element2, list.pop());
    Assertions.assertNull(list.unsafeGet(1), "Pop should clear references");
    Assertions.assertEquals(element1, list.pop());
    Assertions.assertNull(list.unsafeGet(0), "Pop should clear references");
    Assertions.assertEquals(0, list.size());

    // Empty
    Assertions.assertThrows(IndexOutOfBoundsException.class, () -> list.pop());
  }

  @Test
  public void testToArray() {
    final List<Integer> c = createList(2, 5, 13, 42, 99);
    final LocalList<Integer> list = new LocalList<>(c);
    Assertions.assertArrayEquals(c.toArray(), list.toArray());
    // Too small
    final Integer[] small = new Integer[0];
    Assertions.assertArrayEquals(c.toArray(small), list.toArray(small));
    // Too big
    final Integer[] big = new Integer[10];
    Assertions.assertArrayEquals(c.toArray(big), list.toArray(big));
  }

  @Test
  public void testToArrayRange() {
    final List<Integer> c = createList(2, 5, 13, 42, 99);
    final LocalList<Integer> list = new LocalList<>(c);

    // With full range it should match toArray(T[])
    // Too small
    final Integer[] small = new Integer[0];
    Assertions.assertArrayEquals(c.toArray(small), list.toArrayOfRange(small, 0, list.size()));
    // Too big
    final Integer[] big = new Integer[10];
    Assertions.assertArrayEquals(c.toArray(big), list.toArrayOfRange(big, 0, list.size()));

    // Specialisation for a range
    final int from = 2;
    final int to = 4;
    // Too small
    Assertions.assertArrayEquals(c.subList(from, to).toArray(small),
        list.toArrayOfRange(small, from, to));
    // Too big
    Assertions.assertArrayEquals(c.subList(from, to).toArray(big),
        list.toArrayOfRange(big, from, to));

    // Bad range
    Assertions.assertThrows(IndexOutOfBoundsException.class,
        () -> list.toArrayOfRange(small, -1, to));
    Assertions.assertThrows(IndexOutOfBoundsException.class,
        () -> list.toArrayOfRange(small, from, list.size() + 1));
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> list.toArrayOfRange(small, to, from));
  }

  @Test
  public void testGetSet() {
    final List<Integer> c = createList(2, 5, 13, 42, 99);
    final LocalList<Integer> list = new LocalList<>(c);

    // Get
    for (int i = 0; i < c.size(); i++) {
      Assertions.assertEquals(c.get(i), list.get(i));
    }
    Assertions.assertThrows(IndexOutOfBoundsException.class, () -> list.get(-1));
    Assertions.assertThrows(IndexOutOfBoundsException.class, () -> list.get(list.size()));

    // Set
    for (int i = 0; i < c.size(); i++) {
      final Integer value = -678 - i;
      final Integer old = list.set(i, value);
      Assertions.assertEquals(c.get(i), old);
      Assertions.assertEquals(value, list.get(i));
    }
    Assertions.assertThrows(IndexOutOfBoundsException.class, () -> list.set(-1, 0));
    Assertions.assertThrows(IndexOutOfBoundsException.class, () -> list.set(list.size(), 0));
  }

  @Test
  public void testAdd() {
    final List<Integer> c = createList(2, 5, 13, 42, 99);

    final LocalList<Integer> list = new LocalList<>();
    Assertions.assertEquals(0, list.size());

    for (int i = 0; i < c.size(); i++) {
      Assertions.assertTrue(list.add(c.get(i)));
      Assertions.assertEquals(i + 1, list.size());
      Assertions.assertEquals(c.get(i), list.get(i));
    }
  }

  @Test
  public void testAddIndex() {
    final List<Integer> c = createList(2, 5, 13, 42, 99);

    final LocalList<Integer> list = new LocalList<>(c);

    // Add in middle
    final int index = 3;
    list.add(index, 789);
    Assertions.assertEquals(c.size() + 1, list.size());
    for (int i = index; i < c.size(); i++) {
      // All shifted right
      Assertions.assertEquals(c.get(i), list.get(i + 1));
    }

    // Add at end
    final Integer value = 44;
    list.add(list.size(), value);
    Assertions.assertEquals(c.size() + 2, list.size());
    Assertions.assertEquals(value, list.get(list.size() - 1));

    Assertions.assertThrows(IndexOutOfBoundsException.class, () -> list.add(-1, value));
    Assertions.assertThrows(IndexOutOfBoundsException.class,
        () -> list.add(list.size() + 1, value));
  }

  @Test
  public void testAddAll() {
    final List<Integer> c = createList(2, 5, 13, 42, 99);
    final List<Integer> c1 = c.subList(0, 2);
    final List<Integer> c2 = c.subList(2, c.size());

    final LocalList<Integer> list = new LocalList<>(3);
    Assertions.assertTrue(list.addAll(c1));
    Assertions.assertTrue(list.addAll(c2));
    for (int i = 0; i < c.size(); i++) {
      Assertions.assertEquals(c.get(i), list.get(i));
    }

    Assertions.assertFalse(list.addAll(Collections.emptyList()));
  }

  @Test
  public void testAddAllLocalList() {
    final List<Integer> c = createList(2, 5, 13, 42, 99);
    final List<Integer> c1 = c.subList(0, 2);
    final LocalList<Integer> c2 = new LocalList<>(c.subList(2, c.size()));

    final LocalList<Integer> list = new LocalList<>(3);
    Assertions.assertTrue(list.addAll(c1));
    Assertions.assertTrue(list.addAll(c2));
    for (int i = 0; i < c.size(); i++) {
      Assertions.assertEquals(c.get(i), list.get(i));
    }

    Assertions.assertFalse(list.addAll(new LocalList<>()));
  }

  @Test
  public void testAddAllIndex() {
    final List<Integer> c = createList(2, 5, 13, 42, 99);
    final List<Integer> c1 = c.subList(0, 2);
    final List<Integer> c2 = c.subList(2, c.size());

    // Add in middle
    final LocalList<Integer> list = new LocalList<>(3);
    Assertions.assertTrue(list.addAll(0, c1));
    Assertions.assertTrue(list.addAll(1, c2));
    Assertions.assertEquals(c.get(0), list.get(0));
    Assertions.assertEquals(c.get(2), list.get(1));
    Assertions.assertEquals(c.get(3), list.get(2));
    Assertions.assertEquals(c.get(4), list.get(3));
    Assertions.assertEquals(c.get(1), list.get(4));

    // Add at end
    final LocalList<Integer> list2 = new LocalList<>(c1);
    Assertions.assertTrue(list2.addAll(list2.size(), c2));
    for (int i = 0; i < c.size(); i++) {
      Assertions.assertEquals(c.get(i), list2.get(i));
    }

    Assertions.assertFalse(list2.addAll(list2.size(), Collections.emptyList()));

    Assertions.assertThrows(IndexOutOfBoundsException.class, () -> list2.addAll(-1, c2));
    Assertions.assertThrows(IndexOutOfBoundsException.class,
        () -> list2.addAll(list2.size() + 1, c2));
  }

  @Test
  public void testAddAllIndexLocalList() {
    final List<Integer> c = createList(2, 5, 13, 42, 99);
    final List<Integer> c1 = c.subList(0, 2);
    final LocalList<Integer> c2 = new LocalList<>(c.subList(2, c.size()));

    // Add in middle
    final LocalList<Integer> list = new LocalList<>(c1);
    Assertions.assertTrue(list.addAll(1, c2));
    Assertions.assertEquals(c.get(0), list.get(0));
    Assertions.assertEquals(c.get(2), list.get(1));
    Assertions.assertEquals(c.get(3), list.get(2));
    Assertions.assertEquals(c.get(4), list.get(3));
    Assertions.assertEquals(c.get(1), list.get(4));

    // Add at end
    final LocalList<Integer> list2 = new LocalList<>(c1);
    Assertions.assertTrue(list2.addAll(list2.size(), c2));
    for (int i = 0; i < c.size(); i++) {
      Assertions.assertEquals(c.get(i), list2.get(i));
    }

    Assertions.assertFalse(list2.addAll(list2.size(), new LocalList<>()));

    Assertions.assertThrows(IndexOutOfBoundsException.class, () -> list2.addAll(-1, c2));
    Assertions.assertThrows(IndexOutOfBoundsException.class,
        () -> list2.addAll(list2.size() + 1, c2));
  }

  @Test
  public void testRemove() {
    final List<Integer> c = createList(2, 5, 13, 42, 99);

    // Remove in middle
    final LocalList<Integer> list = new LocalList<>(c);
    Assertions.assertEquals(c.get(2), list.remove(2));
    Assertions.assertEquals(c.size() - 1, list.size());

    for (int i = 3; i < c.size(); i++) {
      // All shifted left
      Assertions.assertEquals(c.get(i), list.get(i - 1));
    }

    // Remove from end
    for (int i = c.size() - 1; i >= 3; i--) {
      Assertions.assertEquals(c.get(i), list.remove(list.size() - 1));
      Assertions.assertEquals(i - 1, list.size());
    }

    Assertions.assertThrows(IndexOutOfBoundsException.class, () -> list.remove(-1));
    Assertions.assertThrows(IndexOutOfBoundsException.class, () -> list.remove(list.size() + 1));
  }

  @Test
  public void testRemoveObject() {
    final List<Integer> c = createList(2, 5, 13, null, 42, 99);
    final LocalList<Integer> list = new LocalList<>(c);
    Assertions.assertTrue(list.remove(null));
    Assertions.assertEquals(c.size() - 1, list.size());
    Assertions.assertFalse(list.remove(null));
    Assertions.assertEquals(c.size() - 1, list.size());
    Assertions.assertTrue(list.remove(c.get(0)));
    Assertions.assertEquals(c.size() - 2, list.size());
    Assertions.assertFalse(list.remove(c.get(0)));
    Assertions.assertEquals(c.size() - 2, list.size());
  }

  @Test
  public void testContains() {
    List<Integer> c = createList(2, 5, 13, 42, 99);
    LocalList<Integer> list = new LocalList<>(c);
    Assertions.assertFalse(list.contains(10000));
    Assertions.assertFalse(list.contains(null));
    for (final Integer i : c) {
      Assertions.assertTrue(list.contains(i));
    }
    // With a null
    c = createList(2, 5, 13, null, 42, 99);
    list = new LocalList<>(c);
    for (final Integer i : c) {
      Assertions.assertTrue(list.contains(i));
    }
  }

  @Test
  public void testContainsAll() {
    final List<Integer> c = createList(2, 5, 13);
    final LocalList<Integer> list = new LocalList<>(c);
    Assertions.assertTrue(list.containsAll(c));
    c.add(1000);
    Assertions.assertFalse(list.containsAll(c));
  }

  @Test
  public void testRemoveAll() {
    final List<Integer> c = createList(2, 5, 13, 42, 99);
    final LocalList<Integer> list = new LocalList<>(c);
    Assertions.assertThrows(NullPointerException.class, () -> list.removeAll(null));

    Assertions.assertTrue(list.removeAll(c.subList(1, 3)));
    Assertions.assertEquals(c.size() - 2, list.size());
    Assertions.assertEquals(c.get(0), list.get(0));
    Assertions.assertEquals(c.get(3), list.get(1));
    Assertions.assertEquals(c.get(4), list.get(2));
    // Cannot remove the same items again
    Assertions.assertFalse(list.removeAll(c.subList(1, 3)));
  }

  @Test
  public void testRetainAll() {
    final List<Integer> c = createList(2, 5, 13, 42, 99);
    final LocalList<Integer> list = new LocalList<>(c);
    Assertions.assertThrows(NullPointerException.class, () -> list.retainAll(null));

    Assertions.assertTrue(list.retainAll(c.subList(1, 3)));
    Assertions.assertEquals(c.size() - 3, list.size());
    Assertions.assertEquals(c.get(1), list.get(0));
    Assertions.assertEquals(c.get(2), list.get(1));
    // Cannot remove the same items again
    Assertions.assertFalse(list.retainAll(c.subList(1, 3)));
  }

  @Test
  public void testRemoveIf() {
    final List<Integer> c = createList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    // Remove evens
    testRemoveIf(c, i -> i % 2 == 0, createList(1, 3, 5, 7, 9));
    // Remove odds
    testRemoveIf(c, i -> i % 2 == 1, createList(2, 4, 6, 8, 10));
    // Remove non-prime
    testRemoveIf(c, i -> i > 3 && (i % 2 == 0 || i % 3 == 0), createList(1, 2, 3, 5, 7));
  }

  private static void testRemoveIf(List<Integer> c, Predicate<Integer> filter,
      List<Integer> expected) {
    final LocalList<Integer> list = new LocalList<>(c);
    final boolean result = list.removeIf(filter);
    Assertions.assertEquals(c.size() != expected.size(), result);
    Assertions.assertEquals(expected.size(), list.size());
    for (int i = 0; i < expected.size(); i++) {
      Assertions.assertEquals(expected.get(i), list.get(i));
    }
  }

  @Test
  public void testRemoveIfWithBadFilter() {
    final List<Integer> c = createList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    final LocalList<Integer> list = new LocalList<>(c);
    Assertions.assertThrows(NullPointerException.class, () -> list.removeIf(null));

    final Predicate<Integer> filter = new Predicate<Integer>() {
      @Override
      public boolean test(Integer t) {
        if (t == 7) {
          throw new IllegalArgumentException("not 007");
        }
        // Remove evens
        return t % 2 == 0;
      }
    };
    try {
      list.removeIf(filter);
      Assertions.fail("Exception should be relayed");
    } catch (final IllegalArgumentException ex) {
      Assertions.assertEquals("not 007", ex.getMessage());
    }
    // The list should not be corrupted.
    // Evens are removed up to 7.
    final List<Integer> expected = createList(1, 3, 5, 7, 8, 9, 10);
    Assertions.assertEquals(expected.size(), list.size());
    for (int i = 0; i < expected.size(); i++) {
      Assertions.assertEquals(expected.get(i), list.get(i));
    }
  }

  @Test
  public void testReplaceAll() {
    final List<Integer> c = createList(2, 5, 13, 42, 99);
    final LocalList<Integer> list = new LocalList<>(c);
    Assertions.assertThrows(NullPointerException.class, () -> list.replaceAll(null));

    final Integer value = 456;
    list.replaceAll(i -> value);
    Assertions.assertEquals(c.size(), list.size());
    for (int i = 0; i < c.size(); i++) {
      Assertions.assertEquals(value, list.get(i));
    }

    list.replaceAll(i -> null);
    Assertions.assertEquals(c.size(), list.size());
    for (int i = 0; i < c.size(); i++) {
      Assertions.assertNull(list.get(i));
    }
  }

  @Test
  public void testForEach() {
    final List<Integer> c = createList(2, 5, 13, 42, 99);
    final LocalList<Integer> list = new LocalList<>(c);
    Assertions.assertThrows(NullPointerException.class, () -> list.forEach(null));
    final ArrayList<Integer> c2 = new ArrayList<>(c.size());
    list.forEach(c2::add);
    Assertions.assertEquals(c, c2);
  }

  @Test
  public void testClear() {
    final List<Integer> c = createList(2, 5, 13, 42, 99);
    final LocalList<Integer> list = new LocalList<>(c);
    final int capacity = list.getCapacity();
    list.clear();
    Assertions.assertTrue(list.isEmpty());
    Assertions.assertEquals(capacity, list.getCapacity());
    // test references are cleared
    for (int i = 0; i < c.size(); i++) {
      Assertions.assertNull(list.unsafeGet(i));
    }
  }

  @Test
  public void testClearRange() {
    final List<Integer> c = createList(2, 5, 13, 42, 99);
    testClearRange(c, 0, 5, createList());
    testClearRange(c, 0, 4, createList(99));
    testClearRange(c, 2, 4, createList(2, 5, 99));

    final LocalList<Integer> list = new LocalList<>(c);
    // Bad range
    final int from = 2;
    final int to = 4;
    Assertions.assertThrows(IndexOutOfBoundsException.class, () -> list.clearRange(-1, to));
    Assertions.assertThrows(IndexOutOfBoundsException.class,
        () -> list.clearRange(from, list.size() + 1));
    Assertions.assertThrows(IllegalArgumentException.class, () -> list.clearRange(to, from));
  }

  private static void testClearRange(List<Integer> c, int from, int to, List<Integer> expected) {
    final LocalList<Integer> list = new LocalList<>(c);
    list.clearRange(from, to);
    Assertions.assertEquals(expected.size(), list.size());
    for (int i = 0; i < expected.size(); i++) {
      Assertions.assertEquals(expected.get(i), list.get(i));
    }
    // Remaining should be null
    for (int i = expected.size(); i < c.size(); i++) {
      Assertions.assertNull(list.unsafeGet(i));
    }
  }

  @Test
  public void testIndexOf() {
    assertIndexOf(List::indexOf);
  }

  @Test
  public void testLastIndexOf() {
    assertIndexOf(List::lastIndexOf);
  }

  private static void assertIndexOf(ToIntBiFunction<List<Integer>, Integer> indexOf) {
    final List<Integer> c = createList(2, null, 5, 3, 42, null, 3, 76);
    LocalList<Integer> list = new LocalList<>(c);
    Assertions.assertEquals(-1, indexOf.applyAsInt(list, 6568));
    for (final Integer i : c) {
      Assertions.assertEquals(indexOf.applyAsInt(c, i), indexOf.applyAsInt(list, i));
    }
    // Edge case with no nulls in the list
    list = new LocalList<>(createList(2, 6));
    Assertions.assertEquals(-1, indexOf.applyAsInt(list, null));
  }

  @Test
  public void testSpliteratorEmpty() {
    assertSpliterator(new LocalList<>());
  }

  @Test
  public void testSpliterator() {
    assertSpliterator(new LocalList<>(createList(2, 5, 3, 42, 3, 76)));
  }

  @Test
  public void testSpliteratorWithNulls() {
    assertSpliterator(new LocalList<>(createList(2, null, 5, 3, 42, null, 3, 76)));
  }

  private static void assertSpliterator(List<Integer> list) {
    Spliterator<Integer> sp = list.spliterator();
    Assertions.assertEquals(list.size(), sp.estimateSize());
    Assertions.assertTrue(sp.hasCharacteristics(
        Spliterator.SIZED | Spliterator.SUBSIZED | Spliterator.ORDERED | Spliterator.IMMUTABLE));
    // Consume 1-by-1
    final ArrayList<Integer> drain = new ArrayList<>(list.size());
    while (sp.tryAdvance(drain::add)) {
      // Nothing to do
    }
    assertListEquals(list, drain);

    // Consume all
    drain.clear();
    sp = list.spliterator();
    sp.forEachRemaining(drain::add);
    assertListEquals(list, drain);

    // Split and consume each
    drain.clear();
    splitOrDrain(list.spliterator(), drain);
    list.sort(LocalListTest::compare);
    drain.sort(LocalListTest::compare);
    assertListEquals(list, drain);
  }

  private static void splitOrDrain(Spliterator<Integer> sp, List<Integer> drain) {
    // To check the SUBSIZED conditions
    final long before = sp.estimateSize();
    final Spliterator<Integer> other = sp.trySplit();
    if (other == null) {
      sp.forEachRemaining(drain::add);
    } else {
      // Upon non-null return:
      // the value reported for estimateSize() before splitting, must, after splitting,
      // be greater than or equal to estimateSize() for this and the returned Spliterator;
      Assertions.assertTrue(before >= sp.estimateSize());
      Assertions.assertTrue(before >= other.estimateSize());
      // and if this Spliterator is SUBSIZED, then estimateSize() for this spliterator
      // before splitting must be equal to the sum of estimateSize() for this and the
      // returned Spliterator after splitting.
      Assertions.assertEquals(before, sp.estimateSize() + other.estimateSize());
      splitOrDrain(sp, drain);
      splitOrDrain(other, drain);
    }
  }

  @Test
  public void testIteratorEmpty() {
    assertIterator(new LocalList<>());
  }

  @Test
  public void testIterator() {
    assertIterator(new LocalList<>(createList(2, 5, 3, 42, 3, 76)));
  }

  @Test
  public void testIteratorWithNulls() {
    assertIterator(new LocalList<>(createList(2, null, 5, 3, 42, null, 3, 76)));
  }

  private static void assertIterator(List<Integer> list) {
    final Iterator<Integer> it = list.iterator();

    // Consume 1-by-1
    final ArrayList<Integer> drain = new ArrayList<>(list.size());
    while (it.hasNext()) {
      drain.add(it.next());
      // Do not support remove
      Assertions.assertThrows(UnsupportedOperationException.class, () -> it.remove());
    }
    assertListEquals(list, drain);
    Assertions.assertFalse(it.hasNext());
    Assertions.assertThrows(NoSuchElementException.class, () -> it.next());

    // Consume all
    drain.clear();
    final Iterator<Integer> it2 = list.iterator();
    it2.forEachRemaining(drain::add);
    assertListEquals(list, drain);
    Assertions.assertFalse(it2.hasNext());
    Assertions.assertThrows(NoSuchElementException.class, () -> it2.next());
  }

  @Test
  public void testListIteratorThrowsWithBadIndex() {
    final LocalList<Integer> list = new LocalList<>(createList(0, 1, 2));
    Assertions.assertThrows(IndexOutOfBoundsException.class, () -> list.listIterator(-1));
    Assertions.assertThrows(IndexOutOfBoundsException.class,
        () -> list.listIterator(list.size() + 1));
    // OK to create with the current size
    final ListIterator<Integer> it = list.listIterator(list.size());
    Assertions.assertFalse(it.hasNext());
    Assertions.assertTrue(it.hasPrevious());
  }

  @Test
  public void testListIteratorRemove() {
    final LocalList<Integer> list = new LocalList<>(createList(2, 4, 6, 8));
    final ListIterator<Integer> it = list.listIterator(2);
    Assertions.assertThrows(IllegalStateException.class, () -> it.remove(),
        "No last accessed element");

    // Remove following next
    Assertions.assertEquals(6, it.next());
    it.remove();
    assertListEquals(list, createList(2, 4, 8));
    Assertions.assertThrows(IllegalStateException.class, () -> it.remove(),
        "Cannot remove more than once");

    // Remove following previous
    Assertions.assertEquals(4, it.previous());
    it.remove();
    assertListEquals(list, createList(2, 8));
    Assertions.assertThrows(IllegalStateException.class, () -> it.remove(),
        "Cannot remove more than once");
  }

  @Test
  public void testListIteratorSet() {
    final LocalList<Integer> list = new LocalList<>(createList(2, 4, 6, 8));
    final ListIterator<Integer> it = list.listIterator(2);
    Assertions.assertThrows(IllegalStateException.class, () -> it.set(3),
        "No last accessed element");

    // Set following next
    Assertions.assertEquals(6, it.next());
    it.set(3);
    assertListEquals(list, createList(2, 4, 3, 8));
    it.remove();
    assertListEquals(list, createList(2, 4, 8));
    Assertions.assertThrows(IllegalStateException.class, () -> it.set(3), "No set after remove");

    // Set following previous
    Assertions.assertEquals(4, it.previous());
    it.set(99);
    assertListEquals(list, createList(2, 99, 8));
    it.add(7);
    assertListEquals(list, createList(2, 7, 99, 8));
    Assertions.assertThrows(IllegalStateException.class, () -> it.set(3), "No set after add");
  }

  @Test
  public void testListIteratorAdd() {
    final List<Integer> c = createList(2, 4);
    LocalList<Integer> list = new LocalList<>(c);

    // Add at start
    final ListIterator<Integer> it = list.listIterator();
    // Ensure a last accessed element
    it.next();
    it.previous();
    int cursor = it.nextIndex();
    it.add(7);
    assertListEquals(list, createList(7, 2, 4));
    Assertions.assertEquals(cursor + 1, it.nextIndex());
    Assertions.assertThrows(IllegalStateException.class, () -> it.remove(), "No remove after add");
    Assertions.assertThrows(IllegalStateException.class, () -> it.set(999), "No set after add");

    // Add at end
    list = new LocalList<>(c);
    final ListIterator<Integer> it2 = list.listIterator(2);
    // Ensure a last accessed element
    it2.previous();
    it2.next();
    cursor = it2.nextIndex();
    it2.add(7);
    assertListEquals(list, createList(2, 4, 7));
    Assertions.assertEquals(cursor + 1, it2.nextIndex());
    Assertions.assertThrows(IllegalStateException.class, () -> it2.remove(), "No remove after add");
    Assertions.assertThrows(IllegalStateException.class, () -> it2.set(999), "No set after add");

    // Add at end
    list = new LocalList<>(c);
    final ListIterator<Integer> it3 = list.listIterator(1);
    // Ensure a last accessed element
    it3.previous();
    it3.next();
    cursor = it3.nextIndex();
    it3.add(7);
    assertListEquals(list, createList(2, 7, 4));
    Assertions.assertEquals(cursor + 1, it3.nextIndex());
    Assertions.assertThrows(IllegalStateException.class, () -> it3.remove(), "No remove after add");
    Assertions.assertThrows(IllegalStateException.class, () -> it3.set(999), "No set after add");
  }

  @Test
  public void testListIteratorEmpty() {
    assertListIterator(new LocalList<>());
  }

  @Test
  public void testListIterator() {
    assertListIterator(new LocalList<>(createList(2, 5, 3, 42, 3, 76)));
  }

  @Test
  public void testListIteratorWithNulls() {
    assertListIterator(new LocalList<>(createList(2, null, 5, 3, 42, null, 3, 76)));
  }

  private static void assertListIterator(List<Integer> list) {
    assertListIterator(list, 0);
    assertListIterator(list, list.size());
    assertListIterator(list, list.size() / 2);
  }

  private static void assertListIterator(List<Integer> list, int index) {
    final ListIterator<Integer> it = list.listIterator(index);

    // Consume 1-by-1 forwards
    final ArrayList<Integer> drain = new ArrayList<>(list.size());
    int cursor = index;
    while (it.hasNext()) {
      Assertions.assertEquals(cursor, it.nextIndex());
      Assertions.assertEquals(cursor - 1, it.previousIndex());
      drain.add(it.next());
      cursor++;
      Assertions.assertEquals(cursor, it.nextIndex());
      Assertions.assertEquals(cursor - 1, it.previousIndex());
    }
    assertListEquals(list, index, list.size(), drain);
    Assertions.assertFalse(it.hasNext());
    Assertions.assertThrows(NoSuchElementException.class, () -> it.next());

    // Consume all forwards
    drain.clear();
    final ListIterator<Integer> it2 = list.listIterator(index);
    it2.forEachRemaining(drain::add);
    assertListEquals(list, index, list.size(), drain);
    Assertions.assertFalse(it2.hasNext());
    Assertions.assertThrows(NoSuchElementException.class, () -> it2.next());

    // Consume 1-by-1 backwards
    drain.clear();
    final ListIterator<Integer> it3 = list.listIterator(index);
    cursor = index;
    while (it3.hasPrevious()) {
      Assertions.assertEquals(cursor, it3.nextIndex());
      Assertions.assertEquals(cursor - 1, it3.previousIndex());
      drain.add(it3.previous());
      cursor--;
      Assertions.assertEquals(cursor, it3.nextIndex());
      Assertions.assertEquals(cursor - 1, it3.previousIndex());
    }
    // Reverse the drain
    for (int left = 0, mid = drain.size() >> 1, right = drain.size() - 1; left < mid;
        left++, right--) {
      drain.set(left, drain.set(right, drain.get(left)));
    }
    assertListEquals(list, 0, index, drain);
    Assertions.assertFalse(it3.hasPrevious());
    Assertions.assertThrows(NoSuchElementException.class, () -> it3.previous());
  }

  @Test
  public void testSort() {
    final int size = 5;
    final int lower = 23;
    final int upper = 107;
    for (int i = 0; i < 5; i++) {
      final List<Integer> c =
          new SplittableRandom().ints(size, lower, upper).boxed().collect(Collectors.toList());
      final LocalList<Integer> list = new LocalList<>(c);
      c.sort(Integer::compare);
      list.sort(Integer::compare);
      assertListEquals(c, list);
    }
  }

  @Test
  public void testReverse() {
    final int size = 5;
    final int lower = 23;
    final int upper = 107;
    for (int i = 0; i < 5; i++) {
      // Add i to the size to make lists odd and even lengths
      final List<Integer> c =
          new SplittableRandom().ints(size + i, lower, upper).boxed().collect(Collectors.toList());
      final LocalList<Integer> list = new LocalList<>(c);
      list.reverse();
      final ListIterator<Integer> it = c.listIterator(c.size());
      int index = 0;
      while (it.hasPrevious()) {
        Assertions.assertEquals(it.previous(), list.get(index++));
      }
    }
  }

  @Test
  public void testEquals() {
    final int size = 5;
    final int lower = 23;
    final int upper = 107;
    for (int i = 0; i < 5; i++) {
      final List<Integer> c =
          new SplittableRandom().ints(size, lower, upper).boxed().collect(Collectors.toList());
      final LocalList<Integer> list = new LocalList<>(c);
      Assertions.assertTrue(list.equals(c));
      // This will use the listIterator
      // Assertions.assertTrue(c.equals(list));
    }
  }

  @Test
  public void testEqualsEdgeCases() {
    final List<Integer> c = createList(2, null, 5, 3);
    final LocalList<Integer> list = new LocalList<>(c);

    // Edge cases
    Assertions.assertFalse(list.equals(null));
    Assertions.assertFalse(list.equals(new Object()));

    // Matching lists
    Assertions.assertTrue(list.equals(list));
    Assertions.assertTrue(list.equals(c));

    // Different size
    c.add(9);
    Assertions.assertFalse(list.equals(c));

    // Same size again but null compared to an object
    list.add(null);
    Assertions.assertFalse(list.equals(c));

    // As above but swap which list has the null element
    c.set(c.size() - 1, null);
    list.set(c.size() - 1, 9);
    Assertions.assertFalse(list.equals(c));
  }

  @SuppressWarnings("unlikely-arg-type")
  @Test
  public void testEqualsWhenSizeOfIteratorIsDifferent() {
    final List<Integer> c = createList(2, 5, 3);
    final ArrayList<Integer> badList = new ArrayList<Integer>() {
      private static final long serialVersionUID = 1L;

      // Size will be correct but the list iterator will not return this many items
      @Override
      public int size() {
        return c.size();
      }
    };
    final LocalList<Integer> list = new LocalList<>(c);

    badList.addAll(c);
    badList.add(89);
    Assertions.assertFalse(list.equals(badList), "Bad list contains 4 but reports size=3");

    badList.remove(c.size() - 1);
    badList.remove(c.size() - 1);
    Assertions.assertFalse(list.equals(badList), "Bad list contains 2 but reports size=3");
  }

  @Test
  public void testHashCode() {
    // With null
    List<Integer> c = createList(2, 5, null, 3);
    LocalList<Integer> list = new LocalList<>(c);
    Assertions.assertEquals(c.hashCode(), list.hashCode());

    // Random integers
    final int size = 5;
    final int lower = 23;
    final int upper = 107;
    for (int i = 0; i < 5; i++) {
      c = new SplittableRandom().ints(size, lower, upper).boxed().collect(Collectors.toList());
      list = new LocalList<>(c);
      Assertions.assertEquals(c.hashCode(), list.hashCode());
    }
  }

  // Sub-list tests target only the functionality in the sub-list instance that supplements
  // the skeleton implementation provided by AbstractList

  @Test
  public void testSubListThrowsWithBadIndex() {
    final LocalList<Integer> list = new LocalList<>(createList(0, 1, 2));
    final int from = 0;
    final int to = list.size();
    // Range OK
    list.subList(from, to);
    Assertions.assertThrows(IndexOutOfBoundsException.class, () -> list.subList(from - 1, to));
    Assertions.assertThrows(IndexOutOfBoundsException.class, () -> list.subList(from, to + 1));
  }

  @Test
  public void testSubListSizeAndGet() {
    final List<Integer> c = createList(2, null, 5, 3, 42, null, 3, 76);
    final LocalList<Integer> list = new LocalList<>(c);
    // Sub-list
    final int n = list.size();
    assertListEquals(list, 0, n, list.subList(0, n));
    assertListEquals(list, 2, 5, list.subList(2, 5));
    assertListEquals(list, 2, n, list.subList(2, n));
    assertListEquals(list, 0, 2, list.subList(0, 2));
    // Sub-list of sub-list
    assertListEquals(list, 2, 6, list.subList(2, 6).subList(0, 4));
    assertListEquals(list, 3, 6, list.subList(2, 6).subList(1, 4));
    assertListEquals(list, 2, 5, list.subList(2, 6).subList(0, 3));
    assertListEquals(list, 3, 5, list.subList(2, 6).subList(1, 3));
  }

  @Test
  public void testSubListToArray() {
    final int from = 2;
    final int to = 4;
    List<Integer> c = createList(2, 5, 13, 42, 99);
    final List<Integer> list = new LocalList<>(c).subList(from, to);
    c = c.subList(from, to);
    Assertions.assertArrayEquals(c.toArray(), list.toArray());
    // Too small
    final Integer[] small = new Integer[0];
    Assertions.assertArrayEquals(c.toArray(small), list.toArray(small));
    // Too big
    final Integer[] big = new Integer[10];
    Assertions.assertArrayEquals(c.toArray(big), list.toArray(big));
  }

  @Test
  public void testSubListGetSet() {
    final List<Integer> c = createList(2, 5, 13, 42, 99);
    final LocalList<Integer> parent = new LocalList<>(c);

    final int from = 2;
    final int to = 4;
    final List<Integer> list = parent.subList(from, to);

    // Get
    for (int i = from; i < to; i++) {
      Assertions.assertEquals(c.get(i), list.get(i - from));
    }
    Assertions.assertThrows(IndexOutOfBoundsException.class, () -> list.get(-1));
    Assertions.assertThrows(IndexOutOfBoundsException.class, () -> list.get(list.size()));

    // Set
    for (int i = from; i < to; i++) {
      final Integer value = -678 - i;
      final Integer old = list.set(i - from, value);
      Assertions.assertEquals(c.get(i), old);
      Assertions.assertEquals(value, list.get(i - from));
      // Parent has been updated
      Assertions.assertEquals(value, parent.get(i));
    }
    Assertions.assertThrows(IndexOutOfBoundsException.class, () -> list.set(-1, 0));
    Assertions.assertThrows(IndexOutOfBoundsException.class, () -> list.set(list.size(), 0));
  }

  @Test
  public void testSubListAdd() {
    final List<Integer> c = createList(0, 1, 2, 3, 4);
    final LocalList<Integer> list = new LocalList<>(c);

    final List<Integer> sub = list.subList(2, 4);
    assertListEquals(createList(2, 3), sub);

    sub.add(-1);
    assertListEquals(createList(2, 3, -1), sub);
    assertListEquals(createList(0, 1, 2, 3, -1, 4), list);

    final List<Integer> sub2 = sub.subList(1, 2);
    sub2.add(-2);
    assertListEquals(createList(3, -2), sub2);
    assertListEquals(createList(2, 3, -2, -1), sub);
    assertListEquals(createList(0, 1, 2, 3, -2, -1, 4), list);
  }

  @Test
  public void testSubListAddIndex() {
    final List<Integer> c = createList(0, 1, 2, 3, 4);
    final LocalList<Integer> list = new LocalList<>(c);

    final List<Integer> sub = list.subList(2, 4);
    assertListEquals(createList(2, 3), sub);

    // Add at start
    sub.add(0, -1);
    assertListEquals(createList(-1, 2, 3), sub);
    assertListEquals(createList(0, 1, -1, 2, 3, 4), list);

    // Add in middle
    sub.add(1, -2);
    assertListEquals(createList(-1, -2, 2, 3), sub);
    assertListEquals(createList(0, 1, -1, -2, 2, 3, 4), list);

    // Add at end
    sub.add(sub.size(), -3);
    assertListEquals(createList(-1, -2, 2, 3, -3), sub);
    assertListEquals(createList(0, 1, -1, -2, 2, 3, -3, 4), list);

    // Bad index
    Assertions.assertThrows(IndexOutOfBoundsException.class, () -> sub.add(-1, 99));
    Assertions.assertThrows(IndexOutOfBoundsException.class, () -> sub.add(sub.size() + 1, 99));

    // Sub-list of sub-list
    final List<Integer> sub2 = sub.subList(1, 3);
    assertListEquals(createList(-2, 2), sub2);
    sub2.add(1, -4);
    assertListEquals(createList(-2, -4, 2), sub2);
    assertListEquals(createList(-1, -2, -4, 2, 3, -3), sub);
    assertListEquals(createList(0, 1, -1, -2, -4, 2, 3, -3, 4), list);

    // Bad index
    Assertions.assertThrows(IndexOutOfBoundsException.class, () -> sub2.add(-1, 99));
    Assertions.assertThrows(IndexOutOfBoundsException.class, () -> sub2.add(sub2.size() + 1, 99));
  }

  @Test
  public void testSubListAddAll() {
    final List<Integer> c = createList(0, 1, 2, 3, 4);
    final List<Integer> c2 = createList(-1, -2, -3);
    final List<Integer> c3 = createList(10, 20);
    final LocalList<Integer> list = new LocalList<>(c);

    final List<Integer> sub = list.subList(2, 4);

    Assertions.assertFalse(sub.addAll(Collections.emptyList()));
    Assertions.assertEquals(2, sub.size());

    Assertions.assertTrue(sub.addAll(c2));
    assertListEquals(createList(2, 3, -1, -2, -3), sub);
    assertListEquals(createList(0, 1, 2, 3, -1, -2, -3, 4), list);

    final List<Integer> sub2 = sub.subList(1, 2);
    Assertions.assertTrue(sub2.addAll(c3));
    assertListEquals(createList(3, 10, 20), sub2);
    assertListEquals(createList(2, 3, 10, 20, -1, -2, -3), sub);
    assertListEquals(createList(0, 1, 2, 3, 10, 20, -1, -2, -3, 4), list);
  }

  @Test
  public void testSubListRemove() {
    final List<Integer> c = createList(0, 1, 2, 3, 4);
    final LocalList<Integer> list = new LocalList<>(c);

    final List<Integer> sub = list.subList(1, 4);
    Assertions.assertThrows(IndexOutOfBoundsException.class, () -> sub.remove(-1));
    Assertions.assertThrows(IndexOutOfBoundsException.class, () -> sub.remove(sub.size() + 1));

    Assertions.assertEquals(2, sub.remove(1));
    assertListEquals(createList(1, 3), sub);
    assertListEquals(createList(0, 1, 3, 4), list);

    final List<Integer> sub2 = sub.subList(1, 2);
    Assertions.assertEquals(3, sub2.remove(0));
    assertListEquals(createList(), sub2);
    assertListEquals(createList(1), sub);
    assertListEquals(createList(0, 1, 4), list);
  }

  @Test
  public void testSubListSpliteratorEmpty() {
    assertSpliterator(new LocalList<>(createList(1, 23, 234)).subList(0, 0));
  }

  @Test
  public void testSubListSpliterator() {
    assertSpliterator(new LocalList<>(createList(2, 5, 3, 42, 3, 76).subList(1, 5)));
  }

  @Test
  public void testSubListSpliteratorWithNulls() {
    assertSpliterator(new LocalList<>(createList(2, null, 5, 3, 42, null, 3, 76)).subList(1, 6));
  }

  @Test
  public void testSubListSubListSpliterator() {
    assertSpliterator(
        new LocalList<>(createList(2, null, 5, 3, 42, null, 3, 76)).subList(1, 6).subList(1, 3));
  }

  @Test
  public void testSubListIteratorEmpty() {
    assertIterator(new LocalList<>(createList(1, 23, 234)).subList(0, 0));
  }

  @Test
  public void testSubListIterator() {
    assertIterator(new LocalList<>(createList(2, 5, 3, 42, 3, 76).subList(1, 5)));
  }

  @Test
  public void testSubListIteratorWithNulls() {
    assertIterator(new LocalList<>(createList(2, null, 5, 3, 42, null, 3, 76)).subList(1, 6));
  }

  @Test
  public void testSubListSubListIterator() {
    assertIterator(
        new LocalList<>(createList(2, null, 5, 3, 42, null, 3, 76)).subList(1, 6).subList(1, 3));
  }

  @Test
  public void testSubListClear() {
    final List<Integer> c = IntStream.range(0, 20).boxed().collect(Collectors.toList());
    assertClear(c, l -> l.subList(0, 20));
    assertClear(c, l -> l.subList(3, 20));
    assertClear(c, l -> l.subList(0, 3));
    assertClear(c, l -> l.subList(3, 17));
    assertClear(c, l -> l.subList(3, 17).subList(3, 6));
  }

  @Test
  public void testSubListSubListClear() {
    final List<Integer> c = IntStream.range(0, 20).boxed().collect(Collectors.toList());
    assertClear(c, l -> l.subList(0, 17), l -> l.subList(0, 6));
    assertClear(c, l -> l.subList(0, 17), l -> l.subList(2, 6));
    assertClear(c, l -> l.subList(0, 17), l -> l.subList(2, 17));
    assertClear(c, l -> l.subList(3, 17), l -> l.subList(0, 6));
    assertClear(c, l -> l.subList(3, 17), l -> l.subList(2, 6));
    assertClear(c, l -> l.subList(3, 17), l -> l.subList(2, 14));
  }

  private static void assertClear(List<Integer> c,
      Function<List<Integer>, List<Integer>> toSubList) {
    final List<Integer> list1 = new ArrayList<>(c);
    final List<Integer> list2 = new LocalList<>(c);
    final List<Integer> sub1 = toSubList.apply(list1);
    final List<Integer> sub2 = toSubList.apply(list2);
    sub1.clear();
    sub2.clear();
    assertListEquals(sub1, sub2);
    assertListEquals(list1, list2);
  }

  private static void assertClear(List<Integer> c, Function<List<Integer>, List<Integer>> toSubList,
      Function<List<Integer>, List<Integer>> toSubList2) {
    final List<Integer> list1 = new ArrayList<>(c);
    final List<Integer> list2 = new LocalList<>(c);
    final List<Integer> sub1 = toSubList.apply(list1);
    final List<Integer> sub2 = toSubList.apply(list2);
    final List<Integer> sub21 = toSubList2.apply(sub1);
    final List<Integer> sub22 = toSubList2.apply(sub2);
    sub21.clear();
    sub22.clear();
    assertListEquals(sub21, sub22);
    assertListEquals(sub1, sub2);
    assertListEquals(list1, list2);
  }

  @Test
  public void testSubListSort() {
    final List<Integer> c = createList(2, 5, 3, 42, 3, 76, 1, 23, 4, 6, 7, 19, 2, 3, 4, 1);
    final int from = 5;
    final int to = 12;
    final LocalList<Integer> list = new LocalList<>(c);
    c.subList(from, to).sort(Integer::compare);
    list.subList(from, to).sort(Integer::compare);
    assertListEquals(c, list);
  }
}
