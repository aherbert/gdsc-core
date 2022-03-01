/*
 * Copyright 2009 Rednaxela
 * Copyright 2019-2022 Alex Herbert
 *
 * This software is provided 'as-is', without any express or implied warranty. In no event will the
 * authors be held liable for any damages arising from the use of this software.
 *
 * Permission is granted to anyone to use this software for any purpose, including commercial
 * applications, and to alter it and redistribute it freely, subject to the following restrictions:
 *
 * 1. The origin of this software must not be misrepresented; you must not claim that you wrote the
 * original software. If you use this software in a product, an acknowledgment in the product
 * documentation would be appreciated but is not required.
 *
 * 2. This notice may not be removed or altered from any source distribution.
 */

package uk.ac.sussex.gdsc.core.trees.heaps;

import java.util.Arrays;
import java.util.function.ObjDoubleConsumer;

/**
 * A minimum heap for tracking up to N smallest values with an associated item.
 *
 * @param <T> the element type
 */
public class ObjDoubleMinHeap<T> {
  /** The values. */
  private final double[] values;

  /** The items. */
  private final Object[] items;

  /** The size. */
  private int size;

  /**
   * Create a new instance.
   *
   * @param capacity the capacity
   */
  public ObjDoubleMinHeap(int capacity) {
    this.values = new double[capacity];
    this.items = new Object[capacity];
  }

  /**
   * Get the element at the specified index. Use to hide the unchecked cast from object to T.
   *
   * @param index the index
   * @return the element
   */
  @SuppressWarnings("unchecked")
  private T elementAt(int index) {
    return (T) items[index];
  }

  /**
   * Adds the value and item if the value is less than the threshold value, or the heap is not at
   * capacity.
   *
   * <p>NaN values are silently ignored.
   *
   * @param value the value
   * @param item the item
   * @see #getThreshold()
   */
  public void offer(double value, T item) {
    // If there is still room in the heap
    if (size != values.length && !Double.isNaN(value)) {
      // Insert new item at the end
      values[size] = value;
      items[size] = item;
      upHeapify(size);
      size++;
    } else if (value < values[0]) {
      // If there is no room left in the heap, and the new entry is lower
      // than the max entry replace the max entry with the new entry
      values[0] = value;
      items[0] = item;
      downHeapify(0);
    }
  }

  /**
   * Removes the top value and the corresponding item.
   *
   * @param action the action for the value and corresponding item (can be null)
   * @return the value
   * @throws IllegalStateException if the size is zero
   */
  public double remove(ObjDoubleConsumer<? super T> action) {
    if (size == 0) {
      throw new IllegalStateException();
    }

    final double removedValue = values[0];
    final T removedItem = elementAt(0);
    size--;
    values[0] = values[size];
    items[0] = items[size];
    // Clean references to allow garbage collection
    items[size] = null;
    downHeapify(0);

    if (action != null) {
      action.accept(removedItem, removedValue);
    }
    return removedValue;
  }

  /**
   * Sift the item at the specified {@code index} up towards the top of the heap until it is not
   * greater than its parent.
   *
   * @param index the index
   */
  private void upHeapify(int index) {
    int child = index;
    while (child > 0) {
      final int p = (child - 1) >>> 1;
      if (values[child] > values[p]) {
        final Object pData = items[p];
        final double pDist = values[p];
        items[p] = items[child];
        values[p] = values[child];
        items[child] = pData;
        values[child] = pDist;
        child = p;
      } else {
        break;
      }
    }
  }

  /**
   * Sift the item at the specified {@code index} down towards the bottom of the heap until it is
   * not less than its largest child.
   *
   * @param index the index
   */
  private void downHeapify(int index) {
    for (int p = index, c = index * 2 + 1; c < size; p = c, c = p * 2 + 1) {
      if (c + 1 < size && values[c] < values[c + 1]) {
        c++;
      }
      if (values[p] < values[c]) {
        // Swap the points
        final Object pData = items[p];
        final double pDist = values[p];
        items[p] = items[c];
        values[p] = values[c];
        items[c] = pData;
        values[c] = pDist;
      } else {
        break;
      }
    }
  }

  /**
   * Gets the threshold value below which a value is ensured to be added to the heap. If at capacity
   * this returns the largest stored value, else positive infinity. Note that if not empty the
   * largest value currently stored can be accessed using {@code getValue(0)}.
   *
   * @return the maximum value
   */
  public double getThreshold() {
    if (size != values.length) {
      // Not yet full
      return Double.POSITIVE_INFINITY;
    }
    return values[0];
  }

  /**
   * Gets the size.
   *
   * @return the size
   */
  public int getSize() {
    return size;
  }

  /**
   * Gets the capacity.
   *
   * @return the capacity
   */
  public int getCapacity() {
    return values.length;
  }

  /**
   * Gets the values.
   *
   * @return the values
   */
  public double[] getValues() {
    return Arrays.copyOf(values, size);
  }

  /**
   * Gets the value at the given index.
   *
   * <p>Warning: No checks are made that the index is smaller than the current size.
   *
   * @param index the index
   * @return the value
   */
  public double getValue(int index) {
    return values[index];
  }

  /**
   * Gets the items.
   *
   * @param array the array
   * @return the items
   */
  @SuppressWarnings("unchecked")
  public T[] getItems(T[] array) {
    if (array.length < size) {
      // Make a new array of a's runtime type, but my contents:
      return (T[]) Arrays.copyOf(items, size, array.getClass());
    }
    System.arraycopy(items, 0, array, 0, size);
    return array;
  }

  /**
   * Gets the item at the given index.
   *
   * <p>Warning: No checks are made that the index is smaller than the current size.
   *
   * @param index the index
   * @return the item
   */
  public T getItem(int index) {
    return elementAt(index);
  }
}
