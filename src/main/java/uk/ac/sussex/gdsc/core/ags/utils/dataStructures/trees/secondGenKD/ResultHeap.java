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
package uk.ac.sussex.gdsc.core.ags.utils.dataStructures.trees.secondGenKD;

import java.util.Arrays;

/**
 * Class for tracking up to 'size' closest values.
 *
 * @param <T> the generic type
 */
public class ResultHeap<T> {
  /** The data. */
  final Object[] data;

  /** The distance. */
  final double[] distance;

  /** The size. */
  private final int size;

  /** The values. */
  int values;
  /** The removed data (see {@link #removeLargest()}). */
  public Object removedData;
  /** The removed distance (see {@link #removeLargest()}). */
  public double removedDist;

  /**
   * Instantiates a new result heap.
   *
   * @param size the size
   */
  public ResultHeap(int size) {
    this.data = new Object[size];
    this.distance = new double[size];
    this.size = size;
    this.values = 0;
  }

  /**
   * Adds the value.
   *
   * @param dist the dist
   * @param value the value
   */
  public void addValue(double dist, T value) {
    // If there is still room in the heap
    if (values < size) {
      // Insert new value at the end
      data[values] = value;
      distance[values] = dist;
      upHeapify(values);
      values++;
    }
    // If there is no room left in the heap, and the new entry is lower
    // than the max entry
    else if (dist < distance[0]) {
      // Replace the max entry with the new entry
      data[0] = value;
      distance[0] = dist;
      downHeapify(0);
    }
  }

  /**
   * Adds the value fast.
   *
   * @param dist the dist
   * @param value the value
   */
  void addValueFast(double dist, Object value) {
    // If there is still room in the heap
    if (values < size) {
      // Insert new value at the end
      data[values] = value;
      distance[values] = dist;
      upHeapify(values);
      values++;
    }
    // If there is no room left in the heap, and the new entry is lower
    // than the max entry
    else if (dist < distance[0]) {
      // Replace the max entry with the new entry
      data[0] = value;
      distance[0] = dist;
      downHeapify(0);
    }
  }

  /**
   * Removes the largest.
   */
  public void removeLargest() {
    if (values == 0) {
      throw new IllegalStateException();
    }

    removedData = data[0];
    removedDist = distance[0];
    values--;
    data[0] = data[values];
    distance[0] = distance[values];
    downHeapify(0);
  }

  /**
   * Up heapify.
   *
   * @param c the c
   */
  private void upHeapify(int c) {
    while (c > 0) {
      final int p = (c - 1) >>> 1;
      if (distance[c] > distance[p]) {
        final Object pData = data[p];
        final double pDist = distance[p];
        data[p] = data[c];
        distance[p] = distance[c];
        data[c] = pData;
        distance[c] = pDist;
        c = p;
      } else {
        break;
      }
    }
  }

  /**
   * Down heapify.
   *
   * @param p the p
   */
  private void downHeapify(int p) {
    for (int c = p * 2 + 1; c < values; p = c, c = p * 2 + 1) {
      if (c + 1 < values && distance[c] < distance[c + 1]) {
        c++;
      }
      if (distance[p] < distance[c]) {
        // Swap the points
        final Object pData = data[p];
        final double pDist = distance[p];
        data[p] = data[c];
        distance[p] = distance[c];
        data[c] = pData;
        distance[c] = pDist;
      } else {
        break;
      }
    }
  }

  /**
   * Gets the max dist.
   *
   * @return the max dist
   */
  public double getMaxDist() {
    if (values < size) {
      return Double.POSITIVE_INFINITY;
    }
    return distance[0];
  }

  /**
   * Gets the size.
   *
   * @return the size
   */
  public int getSize() {
    return values;
  }

  /**
   * Gets the capacity.
   *
   * @return the capacity
   */
  public int getCapacity() {
    return size;
  }

  /**
   * Gets the distance.
   *
   * @return the distance
   */
  public double[] getDistance() {
    return Arrays.copyOf(distance, values);
  }

  /**
   * Gets the data.
   *
   * @return the data
   */
  public Object[] getData() {
    return Arrays.copyOf(data, values);
  }

  /**
   * Gets the data.
   *
   * @param a the a
   * @return the data
   */
  @SuppressWarnings("unchecked")
  public T[] getData(T[] a) {
    if (a.length < values) {
      // Make a new array of a's runtime type, but my contents:
      return (T[]) Arrays.copyOf(data, values, a.getClass());
    }
    System.arraycopy(data, 0, a, 0, values);
    if (a.length > values) {
      a[values] = null;
    }
    return a;
  }
}
