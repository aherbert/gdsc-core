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
 * Class for tracking up to 'size' closest values.
 */
public class FloatHeap {
  /** The distance. */
  final float[] distance;

  /** The size. */
  private final int size;

  /** The values. */
  int values;

  /** The distance of the last removed item. */
  public float removedDist;

  /**
   * Instantiates a new float heap.
   *
   * @param size the size
   */
  public FloatHeap(int size) {
    this.distance = new float[size];
    this.size = size;
    this.values = 0;
  }

  /**
   * Adds the value.
   *
   * @param dist the dist
   */
  public void addValue(float dist) {
    // If there is still room in the heap
    if (values < size) {
      // Insert new value at the end
      distance[values] = dist;
      upHeapify(values);
      values++;
    } else if (dist < distance[0]) {
      // If there is no room left in the heap, and the new entry is lower
      // than the max entry replace the max entry with the new entry
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

    removedDist = distance[0];
    values--;
    distance[0] = distance[values];
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
        final float pDist = distance[p];
        distance[p] = distance[index];
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
    for (int c = index * 2 + 1; c < values; index = c, c = index * 2 + 1) {
      if (c + 1 < values && distance[c] < distance[c + 1]) {
        c++;
      }
      if (distance[index] < distance[c]) {
        // Swap the points
        final float pDist = distance[index];
        distance[index] = distance[c];
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
  public float getMaxDist() {
    if (values < size) {
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
  public float[] getDistance() {
    return Arrays.copyOf(distance, values);
  }
}
