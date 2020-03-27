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
 * Class for tracking up to 'capacity' closest distances.
 */
public class FloatHeap {
  /** The distance. */
  private final float[] distance;

  /** The size. */
  private int size;

  /** The distance of the last removed item. */
  private float removedDistance;

  /**
   * Instantiates a new float heap.
   *
   * @param capacity the capacity
   */
  public FloatHeap(int capacity) {
    this.distance = new float[capacity];
  }

  /**
   * Adds the value.
   *
   * @param dist the distance
   */
  public void addValue(float dist) {
    // If there is still room in the heap
    if (size != distance.length) {
      // Insert new value at the end
      distance[size] = dist;
      upHeapify(size);
      size++;
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
    if (size == 0) {
      throw new IllegalStateException();
    }

    removedDistance = distance[0];
    size--;
    distance[0] = distance[size];
    downHeapify(0);
  }

  /**
   * Up heapify.
   *
   * @param index the index
   */
  private void upHeapify(int index) {
    int child = index;
    while (child > 0) {
      final int p = (child - 1) >>> 1;
      if (distance[child] > distance[p]) {
        final float pDist = distance[p];
        distance[p] = distance[child];
        distance[child] = pDist;
        child = p;
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
    for (int p = index, c = index * 2 + 1; c < size; p = c, c = p * 2 + 1) {
      if (c + 1 < size && distance[c] < distance[c + 1]) {
        c++;
      }
      if (distance[p] < distance[c]) {
        // Swap the points
        final float pDist = distance[p];
        distance[p] = distance[c];
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
    if (size != distance.length) {
      // Not yet full
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
    return distance.length;
  }

  /**
   * Gets the distance.
   *
   * @return the distance
   */
  public float[] getDistance() {
    return Arrays.copyOf(distance, size);
  }

  /**
   * Gets the removed distance.
   *
   * @return the removed distance
   * @see #removeLargest()
   */
  public float getRemovedDistance() {
    return removedDistance;
  }
}
