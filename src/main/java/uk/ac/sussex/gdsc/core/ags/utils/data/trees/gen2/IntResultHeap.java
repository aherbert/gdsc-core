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

package uk.ac.sussex.gdsc.core.ags.utils.data.trees.gen2;

import java.util.Arrays;

/**
 * Class for tracking up to 'capacity' closest size.
 */
public class IntResultHeap {
  /** The data. */
  final int[] data;

  /** The distance. */
  final double[] distance;

  /** The capacity. */
  private final int capacity;

  /** The size. */
  int size;

  /**
   * The removed data.
   */
  private int removedData;

  /**
   * The removed distance.
   */
  private double removedDistance;

  /**
   * Instantiates a new int result heap.
   *
   * @param capacity the capacity
   */
  public IntResultHeap(int capacity) {
    this.data = new int[capacity];
    this.distance = new double[capacity];
    this.capacity = capacity;
    this.size = 0;
  }

  /**
   * Adds the value.
   *
   * @param dist the dist
   * @param value the value
   */
  public void addValue(double dist, int value) {
    // If there is still room in the heap
    if (size < capacity) {
      // Insert new value at the end
      data[size] = value;
      distance[size] = dist;
      upHeapify(size);
      size++;
    } else if (dist < distance[0]) {
      // If there is no room left in the heap, and the new entry is lower
      // than the max entry replace the max entry with the new entry
      data[0] = value;
      distance[0] = dist;
      downHeapify(0);
    }
  }

  /**
   * Removes the largest.
   */
  public void removeLargest() {
    if (size == 0) {
      throw new IllegalStateException();
    }

    removedData = data[0];
    removedDistance = distance[0];
    size--;
    data[0] = data[size];
    distance[0] = distance[size];
    downHeapify(0);
  }

  /**
   * Up heapify.
   *
   * @param index the index
   */
  private void upHeapify(int index) {
    while (index > 0) {
      final int p = (index - 1) >>> 1;
      if (distance[index] > distance[p]) {
        final int pData = data[p];
        final double pDist = distance[p];
        data[p] = data[index];
        distance[p] = distance[index];
        data[index] = pData;
        distance[index] = pDist;
        index = p;
      } else {
        break;
      }
    }
  }

  /**
   * Down heapify.
   *
   * @param index the index
   */
  private void downHeapify(int index) {
    for (int c = index * 2 + 1; c < size; index = c, c = index * 2 + 1) {
      if (c + 1 < size && distance[c] < distance[c + 1]) {
        c++;
      }
      if (distance[index] < distance[c]) {
        // Swap the points
        final int pData = data[index];
        final double pDist = distance[index];
        data[index] = data[c];
        distance[index] = distance[c];
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
    if (size < capacity) {
      return Float.POSITIVE_INFINITY;
    }
    return distance[0];
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
    return capacity;
  }

  /**
   * Gets the distance.
   *
   * @return the distance
   */
  public double[] getDistance() {
    return Arrays.copyOf(distance, size);
  }

  /**
   * Gets the data.
   *
   * @return the data
   */
  public int[] getData() {
    return Arrays.copyOf(data, size);
  }

  /**
   * Gets the removed data.
   *
   * @return the removed data
   * @see #removeLargest()
   */
  public int getRemovedData() {
    return removedData;
  }

  /**
   * Gets the removed distance.
   *
   * @return the removed distance
   * @see #removeLargest()
   */
  public double getRemovedDistance() {
    return removedDistance;
  }
}
