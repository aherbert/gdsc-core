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

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.RandomAccess;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import uk.ac.sussex.gdsc.core.data.VisibleForTesting;

/**
 * A {@link List} intended for <strong>single-threaded</strong> or <strong>local</strong> usage. It
 * is <em>not recommended</em> to expose this list to code beyond the scope of the code that created
 * the list. Notable features include:
 *
 * <ul>
 *
 * <li>Allows {@code null} entries.</li>
 *
 * <li>Uses a resizable array for data storage. Performance should be comparable to
 * {@link java.util.ArrayList}.</li>
 *
 * <li>No checks for concurrent modification.</li>
 *
 * <li>Efficient iterator and spliterator implementations using a snapshot of the list.</li>
 *
 * <li>The iterator does not support the optional remove operation for efficiency. This can be
 * achieved through the ListIterator instance.</li>
 *
 * <li>Methods for direct access to the backing storage (warnings below).</li>
 *
 * </ul>
 *
 * <p>In addition to implementing the {@link List} interface methods have been added to manipulate
 * the size of the array as per {@link java.util.ArrayList}. Methods signatures match those in
 * {@code ArrayList} ({@code ensureCapacity}, {@code trimToSize}).
 *
 * <p>Unlike the {@code ArrayList} the list is not {@link java.io.Serializable Serializable} or
 * {@link Cloneable}. The {@link #copy()} and {@link #copyOfRange(int, int)} methods are used to
 * obtain a storage optimised shallow copy of the list.
 *
 * <h1>Warnings</h1>
 *
 * <p>There are methods in this class to supplement the {@link List} API. Some do not have the usual
 * guarded access to the positions in the list based on the current list size. It is possible to
 * access indices beyond the current size of the list that are within the array capacity without
 * raising an exception; these methods can access or create stale data beyond the managed elements
 * of the list. Usage of these methods in intended to be performed in an equivalent manner to
 * manipulating an array {@code E[]} up to a known size:+
 *
 * <pre>
 * {@code LocalList<String> list = ...
 * int nullCount = 0;
 * for (int i = 0; i < list.size(); i++)
 *    if (list.unsafeGet(i) == null) {
 *      nullCount++;
 *    }
 * }
 * </pre>
 *
 * <p>The use of the {@link #ensureCapacity(int)} method can increase the capacity of the list and
 * allow usage of the faster unguarded methods to add or replace data. Use at your own risk.
 *
 * <p>This list has no checks against concurrent modification. It is intended to be used in local
 * thread environments or internally in private methods. It is not recommended to expose this list
 * to code beyond the scope of the code that created the list. For that purpose it is recommended to
 * use {@link java.util.ArrayList}.
 *
 * @param <E> the element type
 */
public final class LocalList<E> implements List<E>, RandomAccess {
  /**
   * The maximum size buffer to safely allocate. Attempts to allocate above this size may fail.
   *
   * <p>This is set to the same size used in the JDK {@code java.util.ArrayList}:
   *
   * <blockquote> Some VMs reserve some header words in an array. Attempts to allocate larger arrays
   * may result in OutOfMemoryError: Requested array size exceeds VM limit. </blockquote>
   */
  private static final int MAX_BUFFER_SIZE = Integer.MAX_VALUE - 8;

  /**
   * The default capacity.
   *
   * <p>Since capacity increases by 50% when serially adding elements the start value is chosen so
   * that it will be close to the maximum value for an array before default capacity increases are
   * clipped. This minimises reallocations of a large memory block. From candidates of 1-20 the
   * following are the best: (11) 47 reallocations = 1,992,174,387; (16) 46 reallocations =
   * 1,992,174,387; (17) 46 reallocations = 2,032,255,081. We choose 11 for a smaller footprint for
   * an empty list than 17.
   *
   * <p>Note: The JDK {@code java.util.ArrayList} uses: (10) 47 reallocations = 1,796,357,452. Using
   * 11 allows storage of an extra 195,816,935 items than an ArrayList with the same number of
   * memory reallocations. However it is extremely unlikely that a list will be serially added to
   * for this many elements from the default capacity. There may be a better choice of size based on
   * other criteria.
   */
  private static final int DEFAULT_CAPACITY = 11;

  /** An empty array. Used as a placeholder for empty lists. */
  private static final Object[] EMPTY_ARRAY = {};

  /** The data. Package scope so it may be directly accessed by inner classes. */
  Object[] data;

  /** The size. Effectively accessed by inner classes via an inlined size() method. */
  private int size;

  /**
   * Create an instance with the default capacity.
   */
  public LocalList() {
    // Note: This will allocate a new array.
    // The alternative is to use a dummy empty array as a signal for the default capacity.
    // This requires an additional check that the data is not the dummy empty array
    // each time the current capacity is computed. Here we put up with memory allocation
    // for an empty list.
    this(DEFAULT_CAPACITY);
  }

  /**
   * Create an instance with the specified capacity.
   *
   * <p>Use a {@code capacity of 0} for an empty list with low memory footprint.
   *
   * @param capacity the capacity
   * @throws IllegalArgumentException if the capacity is negative
   */
  public LocalList(int capacity) {
    ValidationUtils.checkPositive(capacity, "capacity");
    data = capacity == 0 ? EMPTY_ARRAY : new Object[capacity];
  }

  /**
   * Create an instance containing all the elements of the collection.
   *
   * @param c the collection
   * @throws NullPointerException if the collection is null
   */
  public LocalList(Collection<? extends E> c) {
    data = c.toArray();
    size = data.length;
    // Optimise
    if (size == 0) {
      data = EMPTY_ARRAY;
    } else {
      // Avoid runtime errors if the array is not an Object[], e.g. if a '?' array
      // (where '?' extends 'E') then we cannot assign an instance of E to the array.
      if (data.getClass() != Object[].class) {
        data = Arrays.copyOf(data, size, Object[].class);
      }
    }
  }

  /**
   * Copy constructor.
   *
   * @param source the source
   */
  private LocalList(LocalList<E> source) {
    data = source.size == 0 ? EMPTY_ARRAY : source.toArray();
    size = data.length;
  }

  /**
   * Copy constructor.
   *
   * @param fromIndex index of first element to be copied
   * @param toIndex index after last element to be copied
   * @param source the source
   */
  private LocalList(LocalList<E> source, int fromIndex, int toIndex) {
    data = fromIndex == toIndex ? EMPTY_ARRAY : Arrays.copyOfRange(source.data, fromIndex, toIndex);
    size = data.length;
  }

  /**
   * Create a shallow-copy of the list (stored elements are copied by reference).
   *
   * <p>The copy will be created with storage optimised for the current size.
   *
   * @return the copy
   */
  public LocalList<E> copy() {
    return new LocalList<>(this);
  }

  /**
   * Create a shallow-copy of the list (stored elements are copied by reference).
   *
   * <p>The copy will be created with storage optimised for the specified range.
   *
   * @param fromIndex index of first element to be copied
   * @param toIndex index after last element to be copied
   * @return the copy
   * @throws IndexOutOfBoundsException if {@code fromIndex < 0} or {@code toIndex > size}
   * @throws IllegalArgumentException if {@code toIndex < fromIndex}
   */
  public LocalList<E> copyOfRange(int fromIndex, int toIndex) {
    checkRangeForSubList(fromIndex, toIndex, size);
    return new LocalList<>(this, fromIndex, toIndex);
  }

  /**
   * Gets the capacity.
   *
   * @return the capacity
   */
  public int getCapacity() {
    return data.length;
  }

  /**
   * Trims the capacity to be the list's current size.
   *
   * @see java.util.ArrayList#trimToSize()
   */
  public void trimToSize() {
    if (size < data.length) {
      data = (size == 0) ? EMPTY_ARRAY : Arrays.copyOf(data, size);
    }
  }

  /**
   * Truncate the list to the new size. This does not reduce the list capacity.
   *
   * <p>This method has no effect if the new size is larger than the current size. In contrast a
   * call to {@link #clearRange(int, int) clearRange(0, newSize)} will throw an exception if the new
   * size is larger than the list.
   *
   * @param newSize the new size
   * @throws IllegalArgumentException if the new size is negative
   * @see #ensureCapacity(int)
   * @see #trimToSize()
   * @see #clearRange(int, int)
   */
  public void truncate(int newSize) {
    ValidationUtils.checkPositive(newSize, "size");
    if (newSize < size) {
      setToNull(newSize, size);
      size = newSize;
    }
  }

  /**
   * Sets the element data to null in the given range. This is used to clear stale references for
   * garbage collection.
   *
   * @param start the start (inclusive)
   * @param end the end (exclusive)
   */
  private void setToNull(int start, int end) {
    for (int i = start; i < end; i++) {
      data[i] = null;
    }
  }

  /**
   * Increases the capacity to hold at least the minimum required capacity.
   *
   * <p>Note: This will silently ignore a negative capacity.
   *
   * @param minCapacity the minimum required capacity
   * @see java.util.ArrayList#ensureCapacity(int)
   */
  public void ensureCapacity(int minCapacity) {
    if (minCapacity > data.length) {
      increaseCapacity(minCapacity);
    }
  }

  /**
   * Increase the capacity to hold at least one more than the current capacity. This will reallocate
   * a new array and should only be called when the capacity has been checked and is known to be too
   * small.
   *
   * @return the new data array
   */
  private Object[] increaseCapacity() {
    return data = Arrays.copyOf(data, createNewCapacity(data.length + 1, data.length));
  }

  /**
   * Increase the capacity to hold at least the minimum required capacity. This will reallocate a
   * new array and should only be called when the capacity has been checked and is known to be too
   * small.
   *
   * @param minCapacity the minimum required capacity
   * @return the new data array
   */
  private Object[] increaseCapacity(final int minCapacity) {
    return data = Arrays.copyOf(data, createNewCapacity(minCapacity, data.length));
  }

  /**
   * Create a new capacity at least as large the minimum required capacity. The old capacity is used
   * to set an initial increase based on the current capacity. If the minimum capacity is negative
   * then this throws an OutOfMemoryError as no array can be allocated.
   *
   * <p>It is assumed the old capacity is positive.
   *
   * @param minCapacity the minimum capacity
   * @param oldCapacity the old capacity (must be positive)
   * @return the capacity
   */
  @VisibleForTesting
  static int createNewCapacity(final int minCapacity, final int oldCapacity) {
    // Overflow-conscious code treats the min and new capacity as unsigned.

    // Increase by 50%
    int newCapacity = oldCapacity + oldCapacity / 2;

    // Check if large enough for the min capacity.
    // Equivalent to Integer.compareUnsigned(newCapacity, minCapacity) < 0.
    if (newCapacity + Integer.MIN_VALUE < minCapacity + Integer.MIN_VALUE) {
      newCapacity = minCapacity;
    }
    // Check if too large.
    // Equivalent to Integer.compareUnsigned(newCapacity, MAX_BUFFER_SIZE) > 0.
    if (newCapacity + Integer.MIN_VALUE > MAX_BUFFER_SIZE + Integer.MIN_VALUE) {
      newCapacity = createPositiveCapacity(minCapacity);
    }

    return newCapacity;
  }

  /**
   * Create a positive capacity at least as large the minimum required capacity. If the minimum
   * capacity is negative then this throws an OutOfMemoryError as no array can be allocated.
   *
   * @param minCapacity the minimum capacity
   * @return the capacity
   */
  private static int createPositiveCapacity(final int minCapacity) {
    if (minCapacity < 0) {
      // overflow
      throw new OutOfMemoryError(
          "Unable to allocate array size: " + Integer.toUnsignedString(minCapacity));
    }
    // This is called when we require buffer expansion to a very big array.
    // Use the conservative maximum buffer size if possible, otherwise the biggest required.
    //
    // Note: In this situation JDK 1.8 java.util.ArrayList returns Integer.MAX_VALUE.
    // This excludes some VMs that can exceed MAX_BUFFER_SIZE but not allocate a full
    // Integer.MAX_VALUE length array.
    // The result is that we may have to allocate an array of this size more than once if
    // the capacity must be expanded again. But it allows maxing out the potential of the
    // current VM.
    return (minCapacity > MAX_BUFFER_SIZE) ? minCapacity : MAX_BUFFER_SIZE;
  }

  // Fast methods with no checks.
  // These are methods that allow the object to be used as a plain E[] array with a size.

  /**
   * Get the element at the specified index without checking the current list size.
   *
   * <h1>Warning</h1>
   *
   * <p>This method directly reads from the underlying storage and allows access to indices outside
   * the list size but within the current capacity. It is possible to obtain stale data from the
   * underlying storage.
   *
   * @param index the index
   * @return the element
   * @throws ArrayIndexOutOfBoundsException If the index is outside the current capacity
   * @see #get(int)
   * @see #size()
   * @see #getCapacity()
   */
  public E unsafeGet(int index) {
    return elementAt(index);
  }

  /**
   * Set the element at the specified index without checking the current list size.
   *
   * <h1>Warning</h1>
   *
   * <p>This method directly writes to the underlying storage and allows access to indices outside
   * the list size but within the current capacity. It is possible to create stale references in the
   * underlying storage that are not memory-managed by the list (i.e. set to {@code null} when
   * removed).
   *
   * <p>This method will not change the size of the list.
   *
   * @param index the index
   * @param element the element
   * @throws ArrayIndexOutOfBoundsException If the index is outside the current capacity
   * @see #set(int, Object)
   * @see #size()
   * @see #getCapacity()
   */
  public void unsafeSet(int index, E element) {
    data[index] = element;
  }

  // Push/Pop for a LIFO stack structure.

  /**
   * Pushes the element onto the end of the list <strong>without</strong> checking the current
   * capacity.
   *
   * <p>In combination with {@link #pop()} this method creates a last-in-first-out (LIFO) stack data
   * structure of the current capacity.
   *
   * <h1>Warning</h1>
   *
   * <p>This method will throw an exception when {@link #add(Object)} would increase the capacity.
   * Use {@link #add(Object)} in-place of {@link #push(Object)} for an expandable LIFO stack.
   *
   * @param element the element
   * @throws IndexOutOfBoundsException If the current size equals the capacity
   * @see #pop()
   * @see #getCapacity()
   * @see #ensureCapacity(int)
   */
  public void push(E element) {
    // Implicit index-out-of-bounds exception
    data[size] = element;
    size++;
  }

  /**
   * Pops an element from the end of the list.
   *
   * <p>In combination with {@link #push(Object)} or {@link #add(Object)} this method creates a
   * last-in-first-out (LIFO) stack data structure.
   *
   * @return the element
   * @throws IndexOutOfBoundsException If the current size is zero
   * @see #push(Object)
   * @see #getCapacity()
   */
  public E pop() {
    final int index = size - 1;
    // Implicit index-out-of-bounds exception
    @SuppressWarnings("unchecked")
    final E element = (E) data[index];
    data[index] = null;
    size = index;
    return element;
  }

  // List API (and complementary methods) below here.

  @Override
  public int size() {
    return size;
  }

  @Override
  public boolean isEmpty() {
    return size == 0;
  }

  @Override
  public Object[] toArray() {
    return Arrays.copyOf(data, size);
  }

  /**
   * Returns an array containing all of the elements in this list in proper sequence (from first to
   * last element); the runtime type of the returned array is that of the specified array. If the
   * list fits in the specified array, it is returned therein. Otherwise, a new array is allocated
   * with the runtime type of the specified array and the size of this list.
   *
   * <p>Note: This deviates from the documented behaviour in {@link List#toArray(Object[])} by not
   * setting any elements to {@code null} in the specified array if it has room to spare.
   */
  @SuppressWarnings("unchecked")
  @Override
  public <T> T[] toArray(T[] a) {
    if (a.length < size) {
      // Not big enough so create a new array of the correct class
      return (T[]) Arrays.copyOf(data, size, a.getClass());
    }
    // Copy into the storage
    System.arraycopy(data, 0, a, 0, size);
    return a;
  }

  /**
   * Returns an array containing all of the elements from a range of the list in proper sequence;
   * the runtime type of the returned array is that of the specified array. If the list fits in the
   * specified array, it is returned therein. Otherwise, a new array is allocated with the runtime
   * type of the specified array and the size of this list.
   *
   * <p>This is a specialisation of the {@link #toArray(Object[])} method to avoid the object
   * creation involved in creating a sub-list:
   *
   * <pre>
   * {@code
   * LocalList<String> list = ...
   * String[] data = list.subList(from, to).toArray(new String[0]);
   * // preferred method
   * String[] data = list.toArrayOfRange(new String[0], from, to);
   * }
   * </pre>
   *
   * @param <T> the runtime type of the array to contain the collection
   * @param a the array into which the elements of this list are to be stored, if it is big enough;
   *        otherwise, a new array of the same runtime type is allocated for this purpose.
   * @param fromIndex index of first element to be copied
   * @param toIndex index after last element to be copied
   * @return an array containing the specified range of elements of this list
   * @throws ArrayStoreException if the runtime type of the specified array is not a supertype of
   *         the runtime type of every element in this list
   * @throws NullPointerException if the specified array is null
   * @see #toArray(Object[])
   */
  @SuppressWarnings("unchecked")
  public <T> T[] toArrayOfRange(T[] a, int fromIndex, int toIndex) {
    checkRangeForSubList(fromIndex, toIndex, size);
    if (a.length < size) {
      // Not big enough so create a new array of the correct class
      return (T[]) Arrays.copyOfRange(data, fromIndex, toIndex, a.getClass());
    }
    // Copy into the storage
    System.arraycopy(data, fromIndex, a, 0, toIndex - fromIndex);
    return a;
  }

  /**
   * Get the object array as if an array of the correct element type. The resulting object should
   * not be leaked outside of the scope of code known to safely handle the array as read-only
   * avoiding a runtime ClassCastException.
   *
   * <p>Package level access for inner classes.
   *
   * @return the elements
   */
  @SuppressWarnings("unchecked")
  E[] elements() {
    return (E[]) data;
  }

  /**
   * Get the element at the specified index. Internal method to perform an unchecked cast on the raw
   * object to the correct return type.
   *
   * <p>Package level access for inner classes.
   *
   * @param index the index
   * @return the element
   */
  @SuppressWarnings("unchecked")
  E elementAt(int index) {
    return (E) data[index];
  }

  @Override
  public E get(int index) {
    if (index >= size) {
      throw new IndexOutOfBoundsException(indexOutOfBoundsMessage(index));
    }
    // Implicit IndexOutOfBounds for index < 0
    return elementAt(index);
  }

  @Override
  public E set(int index, E element) {
    if (index >= size) {
      throw new IndexOutOfBoundsException(indexOutOfBoundsMessage(index));
    }
    // Implicit IndexOutOfBounds for index < 0
    @SuppressWarnings("unchecked")
    final E previous = (E) data[index];
    data[index] = element;
    return previous;
  }

  @Override
  public boolean add(E element) {
    // Ideally this method byte code size should be below -XX:MaxInlineSize
    // (which defaults to 35 bytes). This compiles to 34 bytes.
    final int s = size;
    Object[] elements = data;
    if (s == elements.length) {
      elements = increaseCapacity();
    }
    size = s + 1;
    elements[s] = element;
    return true;
  }

  @Override
  public void add(int index, E element) {
    checkRangeForInsert(index);
    final int s = size;
    Object[] elements = data;
    if (s == elements.length) {
      // Note this will resize the array copying all the data in the process.
      // In this case it may be more efficient to allocate the new array and
      // then copy from old to new here.
      elements = increaseCapacity();
    }
    // Shift right all elements after the insert index.
    // Does nothing if index == size (insert at the end of the list).
    System.arraycopy(elements, index, elements, index + 1, s - index);
    // Insert
    size = s + 1;
    elements[index] = element;
  }

  @Override
  public boolean addAll(Collection<? extends E> c) {
    // Dump to an array and append.
    final Object[] all = c.toArray();
    final int len = all.length;
    return addAll(all, len);
  }

  /**
   * Appends all of the elements in the specified LocalList to the end of this list, in the order
   * that they are returned by the specified lists's iterator.
   *
   * <p>This is a specialisation of the {@link #addAll(Collection)} method to exploit access to
   * internal data structures.
   *
   * @param c list containing elements to be added to this list
   * @return {@code true} if this list changed as a result of the call
   * @see #addAll(Collection)
   */
  public boolean addAll(LocalList<? extends E> c) {
    // Direct access to the internal array
    return addAll(c.data, c.size);
  }

  /**
   * Adds the specified length of the provided data to the end of this list.
   *
   * @param all the data
   * @param len the length
   * @return {@code true} if this list changed as a result of the call
   */
  private boolean addAll(Object[] all, int len) {
    if (len == 0) {
      return false;
    }
    final int s = size;
    Object[] elements = data;
    // spare = elements.length - size
    if (len > elements.length - s) {
      elements = increaseCapacity(s + len);
    }
    // Append
    System.arraycopy(all, 0, data, s, len);
    size = s + len;
    return true;
  }

  @Override
  public boolean addAll(int index, Collection<? extends E> c) {
    checkRangeForInsert(index);
    // Dump to an array and insert.
    final Object[] all = c.toArray();
    final int len = all.length;
    return addAll(index, all, len);
  }

  /**
   * Inserts all of the elements in the specified LocalList into this list at the specified
   * position. Shifts the element currently at that position (if any) and any subsequent elements to
   * the right (increases their indices). The new elements will appear in this list in the order
   * that they are returned by the specified lists's iterator.
   *
   * <p>This is a specialisation of the {@link #addAll(Collection)} method to exploit access to
   * internal data structures.
   *
   * @param index index at which to insert the first element from the specified collection
   * @param c list containing elements to be added to this list
   * @return {@code true} if this list changed as a result of the call
   * @throws IndexOutOfBoundsException if the index is out of range ({@code index < 0 || index >
   *         size()})
   */
  public boolean addAll(int index, LocalList<? extends E> c) {
    checkRangeForInsert(index);
    // Direct access to the internal array
    return addAll(index, c.data, c.size);
  }

  /**
   * Inserts the specified length of the provided data at the specified position in this list.
   *
   * @param index the index
   * @param all the data
   * @param len the length
   * @return {@code true} if this list changed as a result of the call
   */
  private boolean addAll(int index, Object[] all, int len) {
    if (len == 0) {
      return false;
    }
    final int s = size;
    Object[] elements = data;
    // spare = elements.length - size
    if (len > elements.length - s) {
      elements = increaseCapacity(s + len);
    }
    // Shift right all elements after the insert index.
    System.arraycopy(elements, index, elements, index + len, s - index);
    // Insert
    System.arraycopy(all, 0, elements, index, len);
    size = s + len;
    return true;
  }

  @Override
  public E remove(int index) {
    if (index >= size) {
      throw new IndexOutOfBoundsException(indexOutOfBoundsMessage(index));
    }
    // Implicit IndexOutOfBounds for index < 0
    @SuppressWarnings("unchecked")
    final E previous = (E) data[index];
    removeElement(index);
    return previous;
  }

  @Override
  public boolean remove(Object o) {
    // Copy the code from indexOf(Object) then remove(int).
    // Cannot use Object.equals if object is null.
    final Object[] objects = data;
    final int length = size;
    if (o == null) {
      for (int i = 0; i < length; i++) {
        if (objects[i] == null) {
          removeElement(i);
          return true;
        }
      }
    } else {
      for (int i = 0; i < length; i++) {
        if (o.equals(objects[i])) {
          removeElement(i);
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Removes the element from the data and sifts left all following elements.
   *
   * @param index the index
   */
  private void removeElement(int index) {
    // Shift left all elements after the remove index
    System.arraycopy(data, index + 1, data, index, size - index - 1);
    // Remove stale reference
    data[--size] = null;
  }

  /**
   * Removes from this list all of the elements whose index is between {@code fromIndex}, inclusive,
   * and {@code toIndex}, exclusive. Shifts any succeeding elements to the left (reduces their
   * index). This call shortens the list by {@code (toIndex - fromIndex)} elements. (If
   * {@code toIndex==fromIndex}, this operation has no effect.)
   *
   * <p>This method is used by the sub-list implementation based on AbstractList. The method
   * signature and javadoc match the protected AbstractList::removeRange method.
   *
   * <p>Warning: No validation is performed on the range. It is assumed {@code fromIndex < toIndex}
   * and the range is within the current list size. These assumptions are valid if the method is
   * invoked by a sub-list bound to a range of this list.
   *
   * @param fromIndex index of first element to be removed
   * @param toIndex index after last element to be removed
   * @throws IndexOutOfBoundsException if copying would cause access of data outside array bounds
   * @see AbstractList
   * @see System#arraycopy(Object, int, Object, int, int)
   */
  void removeRange(int fromIndex, int toIndex) {
    // Shift left all elements after the to index by insertion at the from index
    System.arraycopy(data, toIndex, data, fromIndex, size - toIndex);
    // Reduce the size by (to - from)
    final int newSize = size - (toIndex - fromIndex);
    setToNull(newSize, size);
    size = newSize;
  }

  /**
   * Check the index is within the range [0, size], thus it is a valid insertion index for an
   * element in the list (including at the end of the list).
   *
   * @param index the index
   * @param size the size
   * @throws IndexOutOfBoundsException if {@code index > size} or {@code index < 0}
   */
  private void checkRangeForInsert(int index) {
    // Insert allowed at the end.
    // if (index < 0 || index > size)
    // Performed using an unsigned integer compare.
    if (index + Integer.MIN_VALUE > size + Integer.MIN_VALUE) {
      throw new IndexOutOfBoundsException(indexOutOfBoundsMessage(index));
    }
  }

  /**
   * Create an error message using the index and size assuming an index is out of the allowed bounds
   * defined by the current size.
   *
   * @param index the index
   * @return the error message
   */
  private String indexOutOfBoundsMessage(int index) {
    return "Index " + index + " not valid for size " + size;
  }

  /**
   * Check the interval [fromIndex, toIndex) is within the range [0, size), thus it is a valid range
   * for a sub-list within the list of the specified size.
   *
   * @param fromIndex low point (inclusive) of the sub-list
   * @param toIndex high point (exclusive) of the sub-list
   * @param size the size
   * @throws IndexOutOfBoundsException if {@code fromIndex < 0} or {@code toIndex > size}
   * @throws IllegalArgumentException if {@code toIndex < fromIndex}
   */
  static void checkRangeForSubList(int fromIndex, int toIndex, int size) {
    if (fromIndex < 0) {
      throw new IndexOutOfBoundsException("From index " + fromIndex);
    }
    if (toIndex > size) {
      throw new IndexOutOfBoundsException("To index " + toIndex + " not valid for size " + size);
    }
    if (toIndex < fromIndex) {
      throw new IllegalArgumentException("Invalid range, to " + toIndex + " < from " + fromIndex);
    }
  }

  @Override
  public boolean contains(Object o) {
    return indexOf(o) >= 0;
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    for (final Object e : c) {
      if (!contains(e)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    ValidationUtils.checkNotNull(c, "collection");
    return removeAnyIf(c::contains);
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    ValidationUtils.checkNotNull(c, "collection");
    return removeAnyIf(e -> !c.contains(e));
  }

  @Override
  public boolean removeIf(Predicate<? super E> filter) {
    ValidationUtils.checkNotNull(filter, "filter");
    return removeAnyIf(filter);
  }

  /**
   * Remove any items from the list that are identified by the filter. Functions as if testing all
   * items:
   *
   * <pre>
   * int newSize = 0;
   * for (int i = 0; i < size; i++) {
   *   if (filter.test((E) data[i])) {
   *     // remove
   *     continue;
   *   }
   *   data[newSize++] = data[i];
   * }
   * size = newSize;
   * </pre>
   *
   * @param filter the filter
   * @return true if the size was modified by the filter
   */
  private boolean removeAnyIf(Predicate<? super E> filter) {
    int index = 0;
    final E[] elements = elements();
    final int length = size;

    // Find first item to filter.
    for (; index < length; index++) {
      if (filter.test(elements[index])) {
        break;
      }
    }
    if (index == length) {
      // Nothing to remove
      return false;
    }

    // The list has changed.
    // Do a single sweep across the remaining data copying elements if they are not filtered.
    // Note: The filter may throw and leave the list without a new size.
    // (E.g. is the filter is a collection that does not allow null).
    // So we set the new size in a finally block.
    int newSize = index;
    try {
      // We know the current index is identified by the filter so advance 1
      index++;

      // Scan the rest
      for (; index < length; index++) {
        final E e = elements[index];
        if (filter.test(e)) {
          continue;
        }
        elements[newSize++] = e;
      }
    } finally {
      // Ensure the length is correct
      if (index != length) {
        // Did not get to the end of the list (e.g. the filter may throw) so copy it verbatim.
        final int len = length - index;
        System.arraycopy(data, index, data, newSize, len);
        newSize += len;
      }
      setToNull(newSize, length);
      size = newSize;
    }
    // The list was modified
    return true;
  }

  @Override
  public void replaceAll(UnaryOperator<E> operator) {
    ValidationUtils.checkNotNull(operator, "operator");
    final E[] elements = elements();
    final int length = size;
    for (int i = 0; i < length; i++) {
      elements[i] = operator.apply(elements[i]);
    }
  }

  @Override
  public void forEach(Consumer<? super E> action) {
    ValidationUtils.checkNotNull(action, "action");
    final E[] elements = elements();
    final int length = size;
    for (int i = 0; i < length; i++) {
      action.accept(elements[i]);
    }
  }

  @Override
  public void clear() {
    setToNull(0, size);
    size = 0;
  }

  /**
   * Removes all of the elements from a range of the list.
   *
   * <p>This is a specialisation of the {@link #clear()} method to avoid the object creation
   * involved in creating a sub-list:
   *
   * <pre>
   * {@code
   * LocalList<String> list = ...
   * list.subList(from, to).clear();
   * // Preferred
   * list.clearRange(from, to);
   * }
   * </pre>
   *
   * @param fromIndex index of first element to be cleared
   * @param toIndex index after last element to be cleared
   * @throws IndexOutOfBoundsException if {@code fromIndex < 0} or {@code toIndex > size}
   * @throws IllegalArgumentException if {@code toIndex < fromIndex}
   * @see #clear()
   */
  public void clearRange(int fromIndex, int toIndex) {
    checkRangeForSubList(fromIndex, toIndex, size);
    removeRange(fromIndex, toIndex);
  }

  @Override
  public int indexOf(Object o) {
    // Cannot use Object.equals if object is null
    final int length = size;
    if (o == null) {
      for (int i = 0; i < length; i++) {
        if (data[i] == null) {
          return i;
        }
      }
    } else {
      for (int i = 0; i < length; i++) {
        if (o.equals(data[i])) {
          return i;
        }
      }
    }
    return -1;
  }

  @Override
  public int lastIndexOf(Object o) {
    // Cannot use Object.equals if object is null
    if (o == null) {
      for (int i = size - 1; i >= 0; i--) {
        if (data[i] == null) {
          return i;
        }
      }
    } else {
      for (int i = size - 1; i >= 0; i--) {
        if (o.equals(data[i])) {
          return i;
        }
      }
    }
    return -1;
  }

  /**
   * Returns the index of the first element in this list that matches the given predicate, or -1 if
   * this list does not contain a match. More formally, returns the lowest index {@code i} such that
   * {@code filter.test(get(i)) == true}, or -1 if there is no such index.
   *
   * @param filter a predicate to identify the element
   * @return the index of the first match in this list, or -1 if this list does not contain a
   *         matching element
   */
  public int findIndex(Predicate<? super E> filter) {
    ValidationUtils.checkNotNull(filter, "filter");
    final E[] elements = elements();
    final int length = size;
    for (int i = 0; i < length; i++) {
      if (filter.test(elements[i])) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Returns the index of the last element in this list that matches the given predicate, or -1 if
   * this list does not contain a match. More formally, returns the highest index {@code i} such
   * that {@code filter.test(get(i)) == true}, or -1 if there is no such index.
   *
   * @param filter a predicate to identify the element
   * @return the index of the last match in this list, or -1 if this list does not contain a
   *         matching element
   */
  public int findLastIndex(Predicate<? super E> filter) {
    ValidationUtils.checkNotNull(filter, "filter");
    final E[] elements = elements();
    for (int i = size - 1; i >= 0; i--) {
      if (filter.test(elements[i])) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Creates a Spliterator over the elements in this list. The Splitertor is <strong>not</strong>
   * <em><a href="Spliterator.html#binding">late-binding</a></em> or <em>fail-fast</em> in the event
   * of concurrent modification of the list.
   *
   * <p>The {@code Spliterator} reports {@link Spliterator#SIZED}, {@link Spliterator#SUBSIZED},
   * {@link Spliterator#ORDERED} and {@link Spliterator#IMMUTABLE}.
   *
   * <h1>Warning</h1>
   *
   * <p>The {@link LocalList} explicitly ignores concurrency modification checks. The spliterator is
   * thus a snapshot of the current list data and it is assumed that modifications to the list will
   * not be reflected during use of the {@link Spliterator}.
   */
  @Override
  public Spliterator<E> spliterator() {
    return Arrays.spliterator(elements(), 0, size);
  }

  /**
   * {@inheritDoc}
   *
   * <p>The iterator does not support the {@link Iterator#remove()} operation. Use the
   * {@link #listIterator()} to allow modification of the list via an iterator.
   *
   * <h1>Warning</h1>
   *
   * <p>The {@link LocalList} explicitly ignores concurrency modification checks. The iterator is
   * thus a snapshot of the current list data and it is assumed that modifications to the list will
   * not be performed during use of the {@link Iterator}.
   */
  @Override
  public Iterator<E> iterator() {
    return new LocalListIterator<>(elements(), 0, size);
  }

  /**
   * {@inheritDoc}
   *
   * <p>The list iterator supports modification of the list. Use the {@link #iterator()} or
   * {@link #forEach(Consumer)} for more efficient traversal of the list.
   *
   * <h1>Warning</h1>
   *
   * <p>The {@link LocalList} explicitly ignores concurrency modification checks. The list iterator
   * can be used to modify the list but must be the sole entity modifying the list. Operation in the
   * event of concurrent structural modification of the list of undefined.
   */
  @Override
  public ListIterator<E> listIterator() {
    return new LocalListListIterator(0);
  }

  /**
   * {@inheritDoc}
   *
   * <p>The list iterator supports modification of the list. Use the {@link #iterator()} or
   * {@link #forEach(Consumer)} for more efficient traversal of the list.
   *
   * <h1>Warning</h1>
   *
   * <p>The {@link LocalList} explicitly ignores concurrency modification checks. The list iterator
   * can be used to modify the list but must be the sole entity modifying the list. Operation in the
   * event of concurrent structural modification of the list of undefined.
   *
   * @throws IndexOutOfBoundsException if {@code index > size} or {@code index < 0}
   */
  @Override
  public ListIterator<E> listIterator(int index) {
    checkRangeForInsert(index);
    return new LocalListListIterator(index);
  }

  /**
   * {@inheritDoc}
   *
   * <h1>Implementation note</h1>
   *
   * <p>The {@link List} returned by this method does not support detection of concurrent
   * modification. The sub-list, and any sub-list thereafter, fully supports the {@link List} API
   * with the exception that the iterator of the list does not support the {@link Iterator#remove()}
   * operation. Use the {@link #listIterator()} to allow modification of the sub-list via an
   * iterator.
   *
   * @throws IndexOutOfBoundsException if {@code fromIndex < 0} or {@code toIndex > size}
   * @throws IllegalArgumentException if {@code toIndex < fromIndex}
   */
  @Override
  public List<E> subList(int fromIndex, int toIndex) {
    checkRangeForSubList(fromIndex, toIndex, size);
    return new LocalListSubList(this, 0, fromIndex, toIndex);
  }

  @Override
  public void sort(Comparator<? super E> c) {
    Arrays.sort(elements(), 0, size, c);
  }

  /**
   * Reverse the list contents.
   */
  public void reverse() {
    for (int left = 0, mid = size >> 1, right = size - 1; left < mid; left++, right--) {
      // swap the values at the left and right indices
      final Object temp = data[left];
      data[left] = data[right];
      data[right] = temp;
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>Note: This implementation makes use of {@link List#size()} as a fast comparison of lists. If
   * equal then the iterators are used to check the elements in order.
   */
  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    // As defined in List::equals
    // 1. Must be a list
    if (!(obj instanceof List)) {
      return false;
    }
    // 2. Same size.
    // Note the implementation in java.util.AbstractList does not check
    // the sizes. It relies on comparing the iterator order only.
    // Thus it defines equality of the sequence of elements even if the
    // size is reported wrong by one or both lists.
    // Here we do a size check because we expect the LocalList to be used
    // by code that is privately scoped and the other list is likely a
    // LocalList.
    final List<?> other = (List<?>) obj;
    final int length = size;
    if (length != other.size()) {
      return false;
    }

    // 3. Same elements in the same order.
    // Since we don't check for concurrent modification
    // skip our iterator and directly access the elements.
    // We make a concession that the other list may be modified
    // concurrently and use hasNext()
    final E[] elements = elements();
    final ListIterator<?> otherItr = other.listIterator();
    int i = 0;
    for (; i < length && otherItr.hasNext(); i++) {
      final E o1 = elements[i];
      // Assume hasNext() from the other is OK since we are equal size
      final Object o2 = otherItr.next();
      // Equality defined by List::equals
      if (!(o1 == null ? o2 == null : o1.equals(o2))) {
        return false;
      }
    }
    // Check if anything is left
    return !(i < length || otherItr.hasNext());
  }

  @Override
  public int hashCode() {
    // As defined in List::hashCode
    int hashCode = 1;
    final E[] elements = elements();
    final int length = size;
    for (int i = 0; i < length; i++) {
      final E e = elements[i];
      hashCode = 31 * hashCode + (e == null ? 0 : e.hashCode());
    }
    return hashCode;
  }

  /**
   * Iterator for the LocalList.
   *
   * <p>Makes no checks for concurrent modification. Does not support the remove() operation. Uses a
   * snapshot of the current list data and iteration range.
   */
  private static class LocalListIterator<E> implements Iterator<E> {
    /** The next index. */
    private int next;
    /** Snapshot of the elements at the time of iterator creation. */
    private final E[] elements;
    /** End index of the list at the time of iterator creation. */
    private final int end;

    /**
     * Create an instance using a snapshot of the list elements.
     *
     * @param elements the elements
     * @param fromIndex low point (inclusive) of the list elements
     * @param toIndex high point (exclusive) of the list elements
     */
    LocalListIterator(E[] elements, int fromIndex, int toIndex) {
      this.elements = elements;
      this.next = fromIndex;
      this.end = toIndex;
    }

    @Override
    public boolean hasNext() {
      return next < end;
    }

    @Override
    public E next() {
      // Local index for the next position
      final int i = next;
      if (i < end) {
        next = i + 1;
        return elements[i];
      }
      throw new NoSuchElementException();
    }

    // Unsupported
    // public void remove()

    @Override
    public void forEachRemaining(Consumer<? super E> action) {
      // Note: No check that the action is not null
      for (int i = next; i < end; i++) {
        action.accept(elements[i]);
      }
      // Finished
      next = end;
    }
  }

  /**
   * ListIterator for the LocalList.
   *
   * <p>Makes no checks for concurrent modification. Supports remove operation using a live view of
   * the list.
   */
  private class LocalListListIterator implements ListIterator<E> {
    /**
     * The cursor position. This is equivalent to the index of the next element.
     *
     * <p>From the ListIterator javadoc: "A ListIterator has no current element; its cursor position
     * always lies between the element that would be returned by a call to previous() and the
     * element that would be returned by a call to next(). An iterator for a list of length n has
     * n+1 possible cursor positions."
     */
    private int cursor;
    /** The index for the last element accessed by next/previous, or -1 if no element. */
    private int lastElement = -1;

    /**
     * Create an instance.
     *
     * @param index the index of the next element to return
     */
    LocalListListIterator(int index) {
      // Assume this is in the range [0, size].
      cursor = index;
    }

    @Override
    public boolean hasNext() {
      return cursor < size;
    }

    @Override
    public E next() {
      // Local index for the next position
      final int i = cursor;
      if (i < LocalList.this.size) {
        cursor = i + 1;
        lastElement = i;
        // Assumes the list size and data are synchronized, i.e. no concurrent modification.
        return LocalList.this.elementAt(i);
      }
      throw new NoSuchElementException();
    }

    @Override
    public boolean hasPrevious() {
      return cursor != 0;
    }

    @Override
    public E previous() {
      // Local index for the previous position
      final int i = cursor - 1;
      if (i >= 0) {
        cursor = i;
        lastElement = i;
        // Assumes no concurrent modification of the list size
        return LocalList.this.elementAt(i);
      }
      throw new NoSuchElementException();
    }

    @Override
    public int nextIndex() {
      return cursor;
    }

    @Override
    public int previousIndex() {
      return cursor - 1;
    }

    @Override
    public void remove() {
      // Local index to remove
      final int i = getLastElementIndex();
      // Assume no concurrent modification and ask the list to remove it
      LocalList.this.remove(i);
      lastElement = -1;
      // If before the cursor then we must update the cursor position too
      if (i < cursor) {
        cursor--;
      }
    }

    @Override
    public void set(E e) {
      // Local index to replace
      final int i = getLastElementIndex();
      // Assume no concurrent modification and ask the list to replace it.
      LocalList.this.unsafeSet(i, e);
    }

    /**
     * Gets the last element index that was accessed.
     *
     * @return the last element index
     * @throws IllegalStateException if the last element does not exist
     */
    private int getLastElementIndex() {
      final int i = lastElement;
      if (i < 0) {
        // The JDK iterators throw IllegalStateException not NoSuchElementException
        throw new IllegalStateException();
      }
      return i;
    }

    @Override
    public void add(E e) {
      // Local index for the insert position
      final int i = cursor;
      // Assume no concurrent modification and ask the list to insert it
      LocalList.this.add(i, e);
      // Inserted before the cursor
      cursor = i + 1;
      // Prevent a remove operation.
      // remove: "can be made only if **add** has not been called after the last call
      // to next or previous."
      lastElement = -1;
    }

    // Use default implementation:
    // public void forEachRemaining(Consumer<? super E> action)
  }

  /**
   * A List bounded to a range of a parent List. Extends {@code java.util.AbstractList}
   * to use the skeleton implementation.
   *
   * <p>Makes no checks for concurrent modification.
   *
   * <p>The sub-list uses some methods to access the element data in the LocalList. Most
   * methods will call the parent list, resulting in a call chain back to the original
   * LocalList. Each sub-list maintains an origin for the position of this sub-list
   * relative to the original element data and and offset to its parent:
   * // @formatter:off
   * <pre>
   * |------------------------------|  original
   *        |----------------|         sublist 1
   *               |----|              sublist 2
   *
   *        |------| offset for sublist 2
   * |-------------| origin for sublist 2
   * </pre>
   * // @formatter:on
   *
   */
  private class LocalListSubList extends AbstractList<E> implements RandomAccess {
    /** The parent list of which this instance is a sub-list. */
    private final List<E> parent;
    /** The origin of the sub-list in the original LocalList. */
    private final int origin;
    /** The offset of the sub-list relative to the parent List. */
    private final int offset;
    /** The size of the list. */
    private int size;

    /**
     * Create an instance of the sub-list relative to a parent. Assumes the ranges are valid.
     *
     * @param parent the parent
     * @param parentOrigin the parent origin relative to the original LocalList
     * @param fromIndex low point (inclusive) of the sub-list
     * @param toIndex high point (exclusive) of the sub-list
     */
    private LocalListSubList(List<E> parent, int parentOrigin, int fromIndex, int toIndex) {
      this.parent = parent;
      this.origin = parentOrigin + fromIndex;
      offset = fromIndex;
      size = toIndex - fromIndex;
    }

    @Override
    public int size() {
      return size;
    }

    // Override methods which benefit from the array access

    @Override
    public Object[] toArray() {
      return Arrays.copyOfRange(LocalList.this.data, origin, origin + size);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T[] toArray(T[] a) {
      if (a.length < size) {
        // Not big enough so create a new array of the correct class
        return (T[]) Arrays.copyOfRange(LocalList.this.data, origin, origin + size, a.getClass());
      }
      // Copy into the storage
      System.arraycopy(LocalList.this.data, origin, a, 0, size);
      // Don't bother setting the first trailing element to null
      return a;
    }

    @Override
    public E get(int index) {
      checkRange(index);
      return LocalList.this.elementAt(origin + index);
    }

    @Override
    public E set(int index, E element) {
      checkRange(index);
      final E previous = LocalList.this.elementAt(origin + index);
      LocalList.this.data[origin + index] = element;
      return previous;
    }

    @Override
    public void add(int index, E element) {
      checkRangeForInsert(index);
      parent.add(offset + index, element);
      size++;
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
      // Insert at the end
      return addAll(size, c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
      checkRangeForInsert(index);
      final int before = parent.size();
      parent.addAll(offset + index, c);
      final int after = parent.size();
      size += (after - before);
      return before != after;
    }

    @Override
    public E remove(int index) {
      checkRange(index);
      final E previous = parent.remove(offset + index);
      size--;
      return previous;
    }

    @Override
    protected void removeRange(int fromIndex, int toIndex) {
      // This is called by the AbstractList.clear() method using removeRange(0, size()).
      // However LocalList does not extend AbstractList so we cannot chain up to the top
      // using parent.removeRange(offset + fromIndex, offset + toIndex).
      // Instead we directly call the LocalList and chain up the size change.
      final int from = origin + fromIndex;
      final int to = origin + toIndex;
      LocalList.this.removeRange(from, to);
      reduceSize(toIndex - fromIndex);
    }

    /**
     * Reduce the size of this sub-list and recursively all the sub-list parents. Used to trickle up
     * a size reduction after a bulk removal of a range.
     *
     * @param reduction the reduction
     */
    @SuppressWarnings("rawtypes")
    private void reduceSize(int reduction) {
      size -= reduction;
      // Recursion will stop at the original LocalList
      if (parent instanceof LocalList.LocalListSubList) {
        ((LocalList.LocalListSubList) parent).reduceSize(reduction);
      }
    }

    @Override
    public Spliterator<E> spliterator() {
      // Use the same snapshot strategy as is done in LocalList
      return Arrays.spliterator(LocalList.this.elements(), origin, origin + size);
    }

    @Override
    public Iterator<E> iterator() {
      // Use the same snapshot strategy as is done in LocalList
      return new LocalListIterator<>(LocalList.this.elements(), origin, origin + size);
    }

    // Left to the default implementation
    // public ListIterator<E> listIterator() {

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
      checkRangeForSubList(fromIndex, toIndex, size);
      return new LocalListSubList(this, origin, fromIndex, toIndex);
    }

    @Override
    public void sort(Comparator<? super E> c) {
      Arrays.sort(LocalList.this.elements(), origin, origin + size, c);
    }

    // For a sublist we cannot rely on the implicit IndexOutOfBounds exception for
    // a negative index so also check if index is negative.

    private void checkRange(int index) {
      // if (index < 0 || index >= size)
      // Performed using an unsigned integer compare.
      if (index + Integer.MIN_VALUE >= size + Integer.MIN_VALUE) {
        throw new IndexOutOfBoundsException(indexOutOfBoundsMessage(index));
      }
    }

    private void checkRangeForInsert(int index) {
      // Insert allowed at the end.
      // if (index < 0 || index > size)
      // Performed using an unsigned integer compare.
      if (index + Integer.MIN_VALUE > size + Integer.MIN_VALUE) {
        throw new IndexOutOfBoundsException(indexOutOfBoundsMessage(index));
      }
    }

    private String indexOutOfBoundsMessage(int index) {
      return "Index " + index + " not valid for size " + size;
    }
  }
}
