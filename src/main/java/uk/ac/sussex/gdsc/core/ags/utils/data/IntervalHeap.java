/*
 * Copyright 2009 Rednaxela
 *
 * Modifications to the code have been made by Alex Herbert for a smaller memory footprint and
 * optimised 2D processing for use with image data as part of the Genome Damage and Stability Centre
 * ImageJ Core Package.
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

package uk.ac.sussex.gdsc.core.ags.utils.data;

import java.text.DecimalFormat;
import java.util.Arrays;

/**
 * An implementation of an implicit binary interval heap.
 *
 * @param <T> the generic type
 */
public class IntervalHeap<T> implements MinHeap<T>, MaxHeap<T> {

  /** The default capacity. */
  private static final int DEFAULT_CAPACITY = 64;

  /** The data. */
  private Object[] data;

  /** The keys. */
  private double[] keys;

  /** The capacity. */
  private int capacity;

  /** The size. */
  private int size;

  /**
   * Instantiates a new interval heap.
   */
  public IntervalHeap() {
    this(DEFAULT_CAPACITY);
  }

  /**
   * Instantiates a new interval heap.
   *
   * @param capacity the capacity
   */
  public IntervalHeap(int capacity) {
    this.data = new Object[capacity];
    this.keys = new double[capacity];
    this.capacity = capacity;
    this.size = 0;
  }

  @Override
  public void offer(double key, T value) {
    // If move room is needed, double array size
    if (size >= capacity) {
      capacity *= 2;
      data = Arrays.copyOf(data, capacity);
      keys = Arrays.copyOf(keys, capacity);
    }

    // Insert new value at the end
    size++;
    data[size - 1] = value;
    keys[size - 1] = key;
    siftInsertedValueUp();
  }

  @Override
  public void removeMin() {
    if (size == 0) {
      throw new IllegalStateException();
    }

    size--;
    data[0] = data[size];
    keys[0] = keys[size];
    data[size] = null;
    siftDownMin(0);
  }

  @Override
  public void replaceMin(double key, T value) {
    if (size == 0) {
      throw new IllegalStateException();
    }

    data[0] = value;
    keys[0] = key;
    if (size > 1) {
      // Swap with pair if necessary
      if (keys[1] < key) {
        swap(0, 1);
      }
      siftDownMin(0);
    }
  }

  @Override
  public void removeMax() {
    if (size == 0) {
      throw new IllegalStateException();
    } else if (size == 1) {
      removeMin();
      return;
    }

    size--;
    data[1] = data[size];
    keys[1] = keys[size];
    data[size] = null;
    siftDownMax(1);
  }

  @Override
  public void replaceMax(double key, T value) {
    if (size == 0) {
      throw new IllegalStateException();
    } else if (size == 1) {
      replaceMin(key, value);
      return;
    }

    data[1] = value;
    keys[1] = key;
    // Swap with pair if necessary
    if (key < keys[0]) {
      swap(0, 1);
    }
    siftDownMax(1);
  }

  @Override
  @SuppressWarnings("unchecked")
  public T getMin() {
    if (size == 0) {
      throw new IllegalStateException();
    }

    return (T) data[0];
  }

  @Override
  @SuppressWarnings("unchecked")
  public T getMax() {
    if (size == 0) {
      throw new IllegalStateException();
    } else if (size == 1) {
      return (T) data[0];
    }

    return (T) data[1];
  }

  @Override
  public double getMinKey() {
    if (size == 0) {
      throw new IllegalStateException();
    }

    return keys[0];
  }

  @Override
  public double getMaxKey() {
    if (size == 0) {
      throw new IllegalStateException();
    } else if (size == 1) {
      return keys[0];
    }

    return keys[1];
  }

  private int swap(int i1, int i2) {
    final Object yData = data[i2];
    final double yDist = keys[i2];
    data[i2] = data[i1];
    keys[i2] = keys[i1];
    data[i1] = yData;
    keys[i1] = yDist;
    return i2;
  }

  /**
   * Sift the inserted value up.
   *
   * <pre>
   * Min-side (u % 2 == 0): - leftchild: 2u + 2 - rightchild: 2u + 4 - parent: (x/2-1)&~1
   *
   * Max-side (u % 2 == 1): - leftchild: 2u + 1 - rightchild: 2u + 3 - parent: (x/2-1)|1
   * </pre>
   */
  private void siftInsertedValueUp() {
    if (size == 1) {
      // Do nothing if it's the only element!
      return;
    }
    int child = size - 1;
    if (child == 1) {
      // If it is the second element, just sort it with it's pair
      if (keys[child] < keys[child - 1]) {
        swap(child, child - 1); // Swap with it's pair
      }
    } else if ((child & 1) == 1) { // Check for oddness
      // Already paired. Ensure pair is ordered right
      // The larger value of the parent pair
      final int parent = (child / 2 - 1) | 1;
      if (keys[child] < keys[child - 1]) {
        // If less than it's pair
        // Swap with it's pair
        child = swap(child, child - 1);
        if (keys[child] < keys[parent - 1]) {
          // If smaller than smaller parent pair
          // Swap into min-heap side
          child = swap(child, parent - 1);
          siftUpMin(child);
        }
      } else if (keys[child] > keys[parent]) {
        // If larger that larger parent pair
        // Swap into max-heap side
        child = swap(child, parent);
        siftUpMax(child);
      }
    } else {
      // Inserted in the lower-value slot without a partner
      // The larger value of the parent pair
      final int p = (child / 2 - 1) | 1;
      if (keys[child] > keys[p]) {
        // If larger that larger parent pair
        // Swap into max-heap side
        child = swap(child, p);
        siftUpMax(child);
      } else if (keys[child] < keys[p - 1]) {
        // If smaller than smaller parent pair
        // Swap into min-heap side
        child = swap(child, p - 1);
        siftUpMin(child);
      }
    }
  }

  private void siftUpMin(int index) {
    // Min-side parent: (x/2-1)&~1
    for (int c = index, p = (index / 2 - 1) & ~1; p >= 0 && keys[c] < keys[p];
        c = p, p = (c / 2 - 1) & ~1) {
      swap(c, p);
    }
  }

  private void siftUpMax(int index) {
    // Max-side parent: (x/2-1)|1
    for (int c = index, p = (index / 2 - 1) | 1; p >= 0 && keys[c] > keys[p];
        c = p, p = (c / 2 - 1) | 1) {
      swap(c, p);
    }
  }

  private void siftDownMin(int index) {
    for (int p = index, c = index * 2 + 2; c < size; p = c, c = p * 2 + 2) {
      if (c + 2 < size && keys[c + 2] < keys[c]) {
        c += 2;
      }
      if (keys[c] < keys[p]) {
        swap(p, c);
        // Swap with pair if necessary
        if (c + 1 < size && keys[c + 1] < keys[c]) {
          swap(c, c + 1);
        }
      } else {
        break;
      }
    }
  }

  private void siftDownMax(int index) {
    for (int p = index, c = index * 2 + 1; c <= size; p = c, c = p * 2 + 1) {
      if (c == size) {
        // If the left child only has half a pair
        if (keys[c - 1] > keys[p]) {
          swap(p, c - 1);
        }
        break;
      } else if (c + 2 == size) {
        // If there is only room for a right child lower pair
        if (keys[c + 1] > keys[c]) {
          if (keys[c + 1] > keys[p]) {
            swap(p, c + 1);
          }
          break;
        }
      } else if (c + 2 < size
          // If there is room for a right child upper pair
          && keys[c + 2] > keys[c]) {
        c += 2;
      }
      if (keys[c] > keys[p]) {
        swap(p, c);
        // Swap with pair if necessary
        if (keys[c - 1] > keys[c]) {
          swap(c, c - 1);
        }
      } else {
        break;
      }
    }
  }

  @Override
  public int size() {
    return size;
  }

  /**
   * Get the capacity.
   *
   * @return the capacity
   */
  public int capacity() {
    return capacity;
  }

  @Override
  public String toString() {
    final DecimalFormat twoPlaces = new DecimalFormat("0.00");
    final StringBuilder str = new StringBuilder(100).append(IntervalHeap.class.getCanonicalName())
        .append(", size: ").append(size()).append(" capacity: ").append(capacity());
    int index = 0;
    int parent = 2;
    while (index < size()) {
      int counter = 0;
      str.append('\t');
      while ((index + counter) < size() && counter < parent) {
        str.append(twoPlaces.format(keys[index + counter])).append(", ");
        counter++;
      }
      str.append('\n');
      index += counter;
      parent *= 2;
    }
    return str.toString();
  }

  /**
   * Validate the heap. This can be used to check the heap construction.
   *
   * @return true, if valid
   */
  boolean validateHeap() {
    // Validate left-right
    for (int i = 0; i < size - 1; i += 2) {
      if (keys[i] > keys[i + 1]) {
        return false;
      }
    }
    // Validate within parent interval
    for (int i = 2; i < size; i++) {
      final double maxParent = keys[(i / 2 - 1) | 1];
      final double minParent = keys[(i / 2 - 1) & ~1];
      if (keys[i] > maxParent || keys[i] < minParent) {
        return false;
      }
    }
    return true;
  }
}
