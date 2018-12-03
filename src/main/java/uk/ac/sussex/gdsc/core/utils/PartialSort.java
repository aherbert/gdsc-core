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
 * Copyright (C) 2011 - 2018 Alex Herbert
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

import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;

/**
 * Provides functionality to partially sort an array.
 */
public final class PartialSort {
  // This class uses n,k as legitimate method parameter names.
  // CHECKSTYLE.OFF: ParameterName
  /**
   * Sort the final returned data. This takes precedence over {@link #OPTION_HEAD_FIRST}.
   *
   * <p>Note that if the number of points to return (n) is above half the total size of the input
   * list (m, e.g. n/m &gt; 0.5) then it is probably faster to sort the input list and take the top
   * n.
   */
  public static final int OPTION_SORT = 0x01;

  /**
   * Remove NaN values from the returned data.
   */
  public static final int OPTION_REMOVE_NAN = 0x02;

  /**
   * Return the head position at the first point in the returned data (i.e. if choosing the bottom N
   * then array[0] will contain the Nth point. This is not compatible with {@link #OPTION_SORT}.
   */
  public static final int OPTION_HEAD_FIRST = 0x04;

  /**
   * Return a sorted array with no invalid data.
   */
  public static final int OPTION_CLEAN = OPTION_SORT | OPTION_REMOVE_NAN;

  /**
   * Internal top direction flag.
   */
  static final int OPTION_TOP = 0x08;

  /**
   * Internal no clone flag.
   */
  private static final int OPTION_NO_CLONE = 0x10;

  /**
   * The size where the selector is faster than the heap.
   */
  private static final int SIZE_TO_USE_SELECTOR = 5;

  /** No public construction. */
  private PartialSort() {}

  /**
   * Provide partial sort of double arrays.
   *
   * <p>This class is based on ags.utils.dataStructures.trees.secondGenKD.ResultHeap. Ideas have
   * been taken from: http://sites.fas.harvard.edu/~libs111/code/heaps/Heap.java
   */
  public static class DoubleHeap {
    /**
     * The number N to select.
     */
    final int selectN;
    /**
     * Working storage.
     */
    private final double[] queue;

    /**
     * Instantiates a new max double heap.
     *
     * @param selectN the number to select
     */
    public DoubleHeap(int selectN) {
      checkStrictlyPositive(selectN);
      this.queue = new double[selectN];
      this.selectN = selectN;
    }

    /**
     * Pick the bottom N from the data using ascending order, i.e. find the bottom selectN smallest
     * values.
     *
     * <p>If the input data size is smaller than N then an {@link ArrayIndexOutOfBoundsException}
     * will occur.
     *
     * @param list the data list
     * @return The bottom N (passed as a reference to internal data structure)
     */
    public double[] bottom(double[] list) {
      return bottom(OPTION_CLEAN, list, list.length);
    }

    /**
     * Pick the bottom N from the data using ascending order, i.e. find the bottom selectN smallest
     * values.
     *
     * <p>If the input data size is smaller than N then an {@link ArrayIndexOutOfBoundsException}
     * will occur.
     *
     * @param list the data list
     * @param options the options
     * @return The bottom N (passed as a reference to internal data structure)
     */
    public double[] bottom(int options, double[] list) {
      return bottom(options, list, list.length);
    }

    /**
     * Pick the bottom N from the data using ascending order, i.e. find the bottom selectN smallest
     * values.
     *
     * <p>If the input data size is smaller than N then an {@link ArrayIndexOutOfBoundsException}
     * will occur.
     *
     * @param list the data list
     * @param size The size of the list (must be equal or above N)
     * @param options the options
     * @return The bottom N (passed as a reference to internal data structure)
     */
    public double[] bottom(int options, double[] list, int size) {
      queue[0] = list[0];

      // Fill
      int index = 1;
      while (index < selectN) {
        // Insert new value at the end
        queue[index] = list[index];
        bottomUpHeapify(index);
        index++;
      }

      // Scan
      while (index < size) {
        // Replace if lower
        if (queue[0] > list[index]) {
          queue[0] = list[index];
          bottomDownHeapify(0);
        }
        index++;
      }

      return finish(queue, options);
    }

    private void bottomUpHeapify(int index) {
      // Reference to the sifted element
      final double toSift = queue[index];
      int child = index;
      while (child > 0) {
        final int parent = (child - 1) >>> 1;
        if (toSift > queue[parent]) {
          // Move parent down and move up one level in the tree.
          queue[child] = queue[parent];
          child = parent;
        } else {
          break;
        }
      }
      queue[child] = toSift;
    }

    private void bottomDownHeapify(int index) {
      // Reference to the sifted element
      final double toSift = queue[index];
      int parent = index;
      int child = parent * 2 + 1;
      while (child < selectN) {
        // If the right child is bigger, compare with it.
        if (child + 1 < selectN && queue[child] < queue[child + 1]) {
          child++;
        }
        if (toSift < queue[child]) {
          // Move child up and move down one level in the tree.
          queue[parent] = queue[child];
          parent = child;
          child = parent * 2 + 1;
        } else {
          // Done
          break;
        }
      }
      queue[parent] = toSift;
    }

    /**
     * Pick the top N from the data using ascending order, i.e. find the top N largest values.
     *
     * <p>If the input data size is smaller than N then an {@link ArrayIndexOutOfBoundsException}
     * will occur.
     *
     * @param list the data list
     * @return The top N (passed as a reference to internal data structure)
     */
    public double[] top(double[] list) {
      return top(OPTION_CLEAN, list, list.length);
    }

    /**
     * Pick the top N from the data using ascending order, i.e. find the top N largest values.
     *
     * <p>If the input data size is smaller than N then an {@link ArrayIndexOutOfBoundsException}
     * will occur.
     *
     * @param options the options
     * @param list the data list
     * @return The top N (passed as a reference to internal data structure)
     */
    public double[] top(int options, double[] list) {
      return top(options, list, list.length);
    }

    /**
     * Pick the top N from the data using ascending order, i.e. find the top N largest values.
     *
     * <p>If the input data size is smaller than N then an {@link ArrayIndexOutOfBoundsException}
     * will occur.
     *
     * @param list the data list
     * @param size The size of the list (must be equal or above N)
     * @param options the options
     * @return The top N (passed as a reference to internal data structure)
     */
    public double[] top(int options, double[] list, int size) {
      queue[0] = list[0];

      // Fill
      int index = 1;
      while (index < selectN) {
        // Insert new value at the end
        queue[index] = list[index];
        topUpHeapify(index);
        index++;
      }

      // Scan
      while (index < size) {
        // Replace if higher
        if (queue[0] < list[index]) {
          queue[0] = list[index];
          topDownHeapify(0);
        }
        index++;
      }

      return finish(queue, options | OPTION_TOP);
    }

    private void topUpHeapify(int index) {
      // Reference to the sifted element
      final double toSift = queue[index];
      int child = index;
      while (child > 0) {
        final int parent = (child - 1) >>> 1;
        if (toSift < queue[parent]) {
          // Move parent down and move up one level in the tree.
          queue[child] = queue[parent];
          child = parent;
        } else {
          break;
        }
      }
      queue[child] = toSift;
    }

    private void topDownHeapify(int index) {
      // Reference to the sifted element
      final double toSift = queue[index];
      int parent = index;
      int child = parent * 2 + 1;
      while (child < selectN) {
        // If the right child is bigger, compare with it.
        if (child + 1 < selectN && queue[child] > queue[child + 1]) {
          child++;
        }
        if (toSift > queue[child]) {
          // Move child up and move down one level in the tree.
          queue[parent] = queue[child];
          parent = child;
          child = parent * 2 + 1;
        } else {
          // Done
          break;
        }
      }
      queue[parent] = toSift;
    }
  }

  /**
   * Provide partial sort of double arrays.
   */
  public static class DoubleSelector {
    /**
     * The number N to select.
     */
    final int selectN;
    /**
     * Working storage.
     */
    private final double[] queue;

    /**
     * Create a new DoubleSelector.
     *
     * @param selectN The number N to select
     */
    public DoubleSelector(int selectN) {
      checkStrictlyPositive(selectN);
      this.selectN = selectN;
      queue = new double[selectN];
    }

    /**
     * Pick the bottom N from the data using ascending order, i.e. find the bottom selectN smallest
     * values.
     *
     * <p>If the input data size is smaller than N then an {@link ArrayIndexOutOfBoundsException}
     * will occur.
     *
     * @param list the data list
     * @return The bottom N (passed as a reference to internal data structure)
     */
    public double[] bottom(double[] list) {
      return bottom(OPTION_CLEAN, list, list.length);
    }

    /**
     * Pick the bottom N from the data using ascending order, i.e. find the bottom selectN smallest
     * values.
     *
     * <p>If the input data size is smaller than N then an {@link ArrayIndexOutOfBoundsException}
     * will occur.
     *
     * @param list the data list
     * @param options the options
     * @return The bottom N (passed as a reference to internal data structure)
     */
    public double[] bottom(int options, double[] list) {
      return bottom(options, list, list.length);
    }

    /**
     * Pick the bottom N from the data using ascending order, i.e. find the bottom selectN smallest
     * values.
     *
     * <p>If the input data size is smaller than N then an {@link ArrayIndexOutOfBoundsException}
     * will occur.
     *
     * @param list the data list
     * @param size The size of the list (must be equal or above N)
     * @param options the options
     * @return The bottom N (passed as a reference to internal data structure)
     */
    public double[] bottom(int options, double[] list, int size) {
      // We retain a pointer to the current highest value in the set.
      int max = 0;
      queue[0] = list[0];

      // Fill
      int index = 1;
      while (index < selectN) {
        queue[index] = list[index];
        if (queue[max] < queue[index]) {
          max = index;
        }
        index++;
      }

      // Scan
      while (index < size) {
        // Replace if lower
        if (queue[max] > list[index]) {
          queue[max] = list[index];
          // Find new max
          max = bottomMax(queue);
        }
        index++;
      }

      if ((options & OPTION_HEAD_FIRST) != 0) {
        swapHead(queue, max);
      }

      return finish(queue, options);
    }

    /**
     * Pick the top N from the data using ascending order, i.e. find the top N largest values.
     *
     * <p>If the input data size is smaller than N then an {@link ArrayIndexOutOfBoundsException}
     * will occur.
     *
     * @param list the data list
     * @return The top N (passed as a reference to internal data structure)
     */
    public double[] top(double[] list) {
      return top(OPTION_CLEAN, list, list.length);
    }

    /**
     * Pick the top N from the data using ascending order, i.e. find the top N largest values.
     *
     * <p>If the input data size is smaller than N then an {@link ArrayIndexOutOfBoundsException}
     * will occur.
     *
     * @param options the options
     * @param list the data list
     * @return The top N (passed as a reference to internal data structure)
     */
    public double[] top(int options, double[] list) {
      return top(options, list, list.length);
    }

    /**
     * Pick the top N from the data using ascending order, i.e. find the top N largest values.
     *
     * <p>If the input data size is smaller than N then an {@link ArrayIndexOutOfBoundsException}
     * will occur.
     *
     * @param list the data list
     * @param size The size of the list (must be equal or above N)
     * @param options the options
     * @return The top N (passed as a reference to internal data structure)
     */
    public double[] top(int options, double[] list, int size) {
      // We retain a pointer to the current highest value in the set.
      int max = 0;
      queue[0] = list[0];

      // Fill
      int index = 1;
      while (index < selectN) {
        queue[index] = list[index];
        if (queue[max] > queue[index]) {
          max = index;
        }
        index++;
      }

      // Scan
      while (index < size) {
        // Replace if higher
        if (queue[max] < list[index]) {
          queue[max] = list[index];
          // Find new max
          max = topMax(queue);
        }
        index++;
      }

      if ((options & OPTION_HEAD_FIRST) != 0) {
        swapHead(queue, max);
      }

      return finish(queue, options | OPTION_TOP);
    }
  }

  /**
   * Provide partial sort of float arrays.
   *
   * <p>This class is based on ags.utils.dataStructures.trees.secondGenKD.ResultHeap. Ideas have
   * been taken from: http://sites.fas.harvard.edu/~libs111/code/heaps/Heap.java
   */
  public static class FloatHeap {
    /**
     * The number N to select.
     */
    final int selectN;
    /**
     * Working storage.
     */
    private final float[] queue;

    /**
     * Instantiates a new max float heap.
     *
     * @param selectN the number to select
     */
    public FloatHeap(int selectN) {
      checkStrictlyPositive(selectN);
      this.queue = new float[selectN];
      this.selectN = selectN;
    }

    /**
     * Pick the bottom N from the data using ascending order, i.e. find the bottom selectN smallest
     * values.
     *
     * <p>If the input data size is smaller than N then an {@link ArrayIndexOutOfBoundsException}
     * will occur.
     *
     * @param list the data list
     * @return The bottom N (passed as a reference to internal data structure)
     */
    public float[] bottom(float[] list) {
      return bottom(OPTION_CLEAN, list, list.length);
    }

    /**
     * Pick the bottom N from the data using ascending order, i.e. find the bottom selectN smallest
     * values.
     *
     * <p>If the input data size is smaller than N then an {@link ArrayIndexOutOfBoundsException}
     * will occur.
     *
     * @param list the data list
     * @param options the options
     * @return The bottom N (passed as a reference to internal data structure)
     */
    public float[] bottom(int options, float[] list) {
      return bottom(options, list, list.length);
    }

    /**
     * Pick the bottom N from the data using ascending order, i.e. find the bottom selectN smallest
     * values.
     *
     * <p>If the input data size is smaller than N then an {@link ArrayIndexOutOfBoundsException}
     * will occur.
     *
     * @param list the data list
     * @param size The size of the list (must be equal or above N)
     * @param options the options
     * @return The bottom N (passed as a reference to internal data structure)
     */
    public float[] bottom(int options, float[] list, int size) {
      queue[0] = list[0];

      // Fill
      int index = 1;
      while (index < selectN) {
        // Insert new value at the end
        queue[index] = list[index];
        bottomUpHeapify(index);
        index++;
      }

      // Scan
      while (index < size) {
        // Replace if lower
        if (queue[0] > list[index]) {
          queue[0] = list[index];
          bottomDownHeapify(0);
        }
        index++;
      }

      return finish(queue, options);
    }

    private void bottomUpHeapify(int index) {
      // Reference to the sifted element
      final float toSift = queue[index];
      int child = index;
      while (child > 0) {
        final int parent = (child - 1) >>> 1;
        if (toSift > queue[parent]) {
          // Move parent down and move up one level in the tree.
          queue[child] = queue[parent];
          child = parent;
        } else {
          break;
        }
      }
      queue[child] = toSift;
    }

    private void bottomDownHeapify(int index) {
      // Reference to the sifted element
      final float toSift = queue[index];
      int parent = index;
      int child = parent * 2 + 1;
      while (child < selectN) {
        // If the right child is bigger, compare with it.
        if (child + 1 < selectN && queue[child] < queue[child + 1]) {
          child++;
        }
        if (toSift < queue[child]) {
          // Move child up and move down one level in the tree.
          queue[parent] = queue[child];
          parent = child;
          child = parent * 2 + 1;
        } else {
          // Done
          break;
        }
      }
      queue[parent] = toSift;
    }

    /**
     * Pick the top N from the data using ascending order, i.e. find the top N largest values.
     *
     * <p>If the input data size is smaller than N then an {@link ArrayIndexOutOfBoundsException}
     * will occur.
     *
     * @param list the data list
     * @return The top N (passed as a reference to internal data structure)
     */
    public float[] top(float[] list) {
      return top(OPTION_CLEAN, list, list.length);
    }

    /**
     * Pick the top N from the data using ascending order, i.e. find the top N largest values.
     *
     * <p>If the input data size is smaller than N then an {@link ArrayIndexOutOfBoundsException}
     * will occur.
     *
     * @param options the options
     * @param list the data list
     * @return The top N (passed as a reference to internal data structure)
     */
    public float[] top(int options, float[] list) {
      return top(options, list, list.length);
    }

    /**
     * Pick the top N from the data using ascending order, i.e. find the top N largest values.
     *
     * <p>If the input data size is smaller than N then an {@link ArrayIndexOutOfBoundsException}
     * will occur.
     *
     * @param list the data list
     * @param size The size of the list (must be equal or above N)
     * @param options the options
     * @return The top N (passed as a reference to internal data structure)
     */
    public float[] top(int options, float[] list, int size) {
      queue[0] = list[0];

      // Fill
      int index = 1;
      while (index < selectN) {
        // Insert new value at the end
        queue[index] = list[index];
        topUpHeapify(index);
        index++;
      }

      // Scan
      while (index < size) {
        // Replace if lower
        if (queue[0] > list[index]) {
          queue[0] = list[index];
          topDownHeapify(0);
        }
        index++;
      }

      return finish(queue, options | OPTION_TOP);
    }

    private void topUpHeapify(int index) {
      // Reference to the sifted element
      final float toSift = queue[index];
      int child = index;
      while (child > 0) {
        final int parent = (child - 1) >>> 1;
        if (toSift < queue[parent]) {
          // Move parent down and move up one level in the tree.
          queue[child] = queue[parent];
          child = parent;
        } else {
          break;
        }
      }
      queue[child] = toSift;
    }

    private void topDownHeapify(int index) {
      // Reference to the sifted element
      final float toSift = queue[index];
      int parent = index;
      int child = parent * 2 + 1;
      while (child < selectN) {
        // If the right child is bigger, compare with it.
        if (child + 1 < selectN && queue[child] > queue[child + 1]) {
          child++;
        }
        if (toSift > queue[child]) {
          // Move child up and move down one level in the tree.
          queue[parent] = queue[child];
          parent = child;
          child = parent * 2 + 1;
        } else {
          // Done
          break;
        }
      }
      queue[parent] = toSift;
    }
  }

  /**
   * Provide partial sort of float arrays.
   */
  public static class FloatSelector {
    /**
     * The number N to select.
     */
    final int selectN;
    /**
     * Working storage.
     */
    private final float[] queue;

    /**
     * Create a new FloatSelector.
     *
     * @param selectN The number N to select
     */
    public FloatSelector(int selectN) {
      checkStrictlyPositive(selectN);
      this.selectN = selectN;
      queue = new float[selectN];
    }

    /**
     * Pick the bottom N from the data using ascending order, i.e. find the bottom selectN smallest
     * values.
     *
     * <p>If the input data size is smaller than N then an {@link ArrayIndexOutOfBoundsException}
     * will occur.
     *
     * @param list the data list
     * @return The bottom N (passed as a reference to internal data structure)
     */
    public float[] bottom(float[] list) {
      return bottom(OPTION_CLEAN, list, list.length);
    }

    /**
     * Pick the bottom N from the data using ascending order, i.e. find the bottom selectN smallest
     * values.
     *
     * <p>If the input data size is smaller than N then an {@link ArrayIndexOutOfBoundsException}
     * will occur.
     *
     * @param list the data list
     * @param options the options
     * @return The bottom N (passed as a reference to internal data structure)
     */
    public float[] bottom(int options, float[] list) {
      return bottom(options, list, list.length);
    }

    /**
     * Pick the bottom N from the data using ascending order, i.e. find the bottom selectN smallest
     * values.
     *
     * <p>If the input data size is smaller than N then an {@link ArrayIndexOutOfBoundsException}
     * will occur.
     *
     * @param list the data list
     * @param size The size of the list (must be equal or above N)
     * @param options the options
     * @return The bottom N (passed as a reference to internal data structure)
     */
    public float[] bottom(int options, float[] list, int size) {
      // We retain a pointer to the current highest value in the set.
      int max = 0;
      queue[0] = list[0];

      // Fill
      int index = 1;
      while (index < selectN) {
        queue[index] = list[index];
        if (queue[max] < queue[index]) {
          max = index;
        }
        index++;
      }

      // Scan
      while (index < size) {
        // Replace if lower
        if (queue[max] > list[index]) {
          queue[max] = list[index];
          // Find new max
          max = bottomMax(queue);
        }
        index++;
      }

      if ((options & OPTION_HEAD_FIRST) != 0) {
        swapHead(queue, max);
      }

      return finish(queue, options);
    }

    /**
     * Pick the top N from the data using ascending order, i.e. find the top N largest values.
     *
     * <p>If the input data size is smaller than N then an {@link ArrayIndexOutOfBoundsException}
     * will occur.
     *
     * @param list the data list
     * @return The top N (passed as a reference to internal data structure)
     */
    public float[] top(float[] list) {
      return top(OPTION_CLEAN, list, list.length);
    }

    /**
     * Pick the top N from the data using ascending order, i.e. find the top N largest values.
     *
     * <p>If the input data size is smaller than N then an {@link ArrayIndexOutOfBoundsException}
     * will occur.
     *
     * @param options the options
     * @param list the data list
     * @return The top N (passed as a reference to internal data structure)
     */
    public float[] top(int options, float[] list) {
      return top(options, list, list.length);
    }

    /**
     * Pick the top N from the data using ascending order, i.e. find the top N largest values.
     *
     * <p>If the input data size is smaller than N then an {@link ArrayIndexOutOfBoundsException}
     * will occur.
     *
     * @param list the data list
     * @param size The size of the list (must be equal or above N)
     * @param options the options
     * @return The top N (passed as a reference to internal data structure)
     */
    public float[] top(int options, float[] list, int size) {
      // We retain a pointer to the current highest value in the set.
      int max = 0;
      queue[0] = list[0];

      // Fill
      int index = 1;
      while (index < selectN) {
        queue[index] = list[index];
        if (queue[max] > queue[index]) {
          max = index;
        }
        index++;
      }

      // Scan
      while (index < size) {
        // Replace if higher
        if (queue[max] < list[index]) {
          queue[max] = list[index];
          // Find new max
          max = topMax(queue);
        }
        index++;
      }

      if ((options & OPTION_HEAD_FIRST) != 0) {
        swapHead(queue, max);
      }

      return finish(queue, options | OPTION_TOP);
    }
  }

  /**
   * Find the index of the bottom (lowest value) of the data.
   *
   * @param data the data
   * @return the index
   */
  static int bottomMax(double[] data) {
    int max = 0;
    for (int i = 1; i < data.length; i++) {
      if (data[max] < data[i]) {
        max = i;
      }
    }
    return max;
  }

  /**
   * Find the index of the bottom (lowest value) of the data.
   *
   * @param data the data
   * @return the index
   */
  static int bottomMax(float[] data) {
    int max = 0;
    for (int i = 1; i < data.length; i++) {
      if (data[max] < data[i]) {
        max = i;
      }
    }
    return max;
  }

  /**
   * Find the index of the top (highest value) of the data.
   *
   * @param data the data
   * @return the index
   */
  static int topMax(float[] data) {
    int max = 0;
    for (int i = 1; i < data.length; i++) {
      if (data[max] > data[i]) {
        max = i;
      }
    }
    return max;
  }

  /**
   * Find the index of the top (highest value) of the data.
   *
   * @param data the data
   * @return the index
   */
  static int topMax(double[] data) {
    int max = 0;
    for (int i = 1; i < data.length; i++) {
      if (data[max] > data[i]) {
        max = i;
      }
    }
    return max;
  }

  /**
   * Perform the finishing stage of the partial sort result using the specified finishing options.
   *
   * @param data the data
   * @param options the options
   * @return the result
   */
  static double[] finish(double[] data, int options) {
    final double[] result = removeNaN(data, options);
    sort(result, options);
    return cloneResultIfRequired(data, result, options);
  }

  /**
   * Perform the finishing stage of the partial sort to result using the specified finishing
   * options.
   *
   * @param data the data
   * @param options the options
   * @return the result
   */
  static float[] finish(float[] data, int options) {
    final float[] result = removeNaN(data, options);
    sort(result, options);
    return cloneResultIfRequired(data, result, options);
  }

  /**
   * Perform the finishing stage of the partial bottom sort result using the specified finishing
   * options.
   *
   * @param data the data
   * @param options the options
   * @return the result
   */
  static double[] bottomFinish(double[] data, int options) {
    if (options == 0) {
      return data;
    }
    replaceHead(data, options);
    return finish(data, options);
  }

  /**
   * Perform the finishing stage of the partial bottom sort result using the specified finishing
   * options.
   *
   * @param data the data
   * @param options the options
   * @return the result
   */
  static float[] bottomFinish(float[] data, int options) {
    if (options == 0) {
      return data;
    }
    replaceHead(data, options);
    return finish(data, options);
  }

  /**
   * Perform the finishing stage of the partial top sort result using the specified finishing
   * options.
   *
   * @param data the data
   * @param options the options
   * @return the result
   */
  static double[] topFinish(double[] data, int options) {
    if (options == 0) {
      return data;
    }
    final int topOptions = options | OPTION_TOP;
    replaceHead(data, topOptions);
    return finish(data, topOptions);
  }

  /**
   * Perform the finishing stage of the partial top sort result using the specified finishing
   * options.
   *
   * @param data the data
   * @param options the options
   * @return the result
   */
  static float[] topFinish(float[] data, int options) {
    if (options == 0) {
      return data;
    }
    final int topOptions = options | OPTION_TOP;
    replaceHead(data, topOptions);
    return finish(data, topOptions);
  }

  private static void replaceHead(double[] data, int options) {
    if ((options & OPTION_HEAD_FIRST) != 0) {
      swapHead(data, ((options & OPTION_TOP) == 0) ? bottomMax(data) : topMax(data));
    }
  }

  private static void replaceHead(float[] data, int options) {
    if ((options & OPTION_HEAD_FIRST) != 0) {
      swapHead(data, ((options & OPTION_TOP) == 0) ? bottomMax(data) : topMax(data));
    }
  }

  /**
   * Swap the specified head index with index 0.
   *
   * @param data the data
   * @param headIndex the head index
   */
  static void swapHead(double[] data, int headIndex) {
    swap(data, 0, headIndex);
  }

  /**
   * Swap the specified head index with index 0.
   *
   * @param data the data
   * @param headIndex the head index
   */
  static void swapHead(float[] data, int headIndex) {
    swap(data, 0, headIndex);
  }

  private static double[] removeNaN(double[] data, int options) {
    if ((options & OPTION_REMOVE_NAN) != 0) {
      int size = 0;
      for (final double value : data) {
        if (Double.isNaN(value)) {
          continue;
        }
        data[size++] = value;
      }
      if (size == data.length) {
        return data;
      }
      if (size == 0) {
        return ArrayUtils.EMPTY_DOUBLE_ARRAY;
      }
      return Arrays.copyOf(data, size);
    }
    return data;
  }

  private static float[] removeNaN(float[] data, int options) {
    if ((options & OPTION_REMOVE_NAN) != 0) {
      int size = 0;
      for (final float value : data) {
        if (Float.isNaN(value)) {
          continue;
        }
        data[size++] = value;
      }
      if (size == data.length) {
        return data;
      }
      if (size == 0) {
        return ArrayUtils.EMPTY_FLOAT_ARRAY;
      }
      return Arrays.copyOf(data, size);
    }
    return data;
  }

  private static void sort(double[] data, int options) {
    if ((options & OPTION_SORT) != 0) {
      Arrays.sort(data);
      if ((options & OPTION_TOP) != 0) {
        SimpleArrayUtils.reverse(data);
      }
    }
  }

  private static void sort(float[] data, int options) {
    if ((options & OPTION_SORT) != 0) {
      Arrays.sort(data);
      if ((options & OPTION_TOP) != 0) {
        SimpleArrayUtils.reverse(data);
      }
    }
  }

  private static double[] cloneResultIfRequired(double[] data, double[] result, int options) {
    return (data == result && (options & OPTION_NO_CLONE) == 0)
        // Clone the result
        ? result.clone()
        // The no clone option is specified; or the result is different anyway
        : result;
  }

  private static float[] cloneResultIfRequired(float[] data, float[] result, int options) {
    return (data == result && (options & OPTION_NO_CLONE) == 0)
        // Clone the result
        ? result.clone()
        // The no clone option is specified; or the result is different anyway
        : result;
  }

  /**
   * Pick the bottom N from the data using ascending order, i.e. find the bottom n smallest values.
   *
   * @param list the data list
   * @param n The number N to select
   * @return The bottom N
   */
  public static double[] bottom(double[] list, int n) {
    if (list == null) {
      return ArrayUtils.EMPTY_DOUBLE_ARRAY;
    }
    return bottom(list, list.length, n);
  }

  /**
   * Pick the bottom N from the data using ascending order, i.e. find the bottom n smallest values.
   *
   * @param list the data list
   * @param n The number N to select
   * @return The bottom N
   */
  public static float[] bottom(float[] list, int n) {
    if (list == null) {
      return ArrayUtils.EMPTY_FLOAT_ARRAY;
    }
    return bottom(list, list.length, n);
  }

  /**
   * Pick the bottom N from the data using ascending order, i.e. find the bottom n smallest values.
   *
   * @param list the data list
   * @param size The size of the list
   * @param n The number N to select
   * @return The bottom N
   */
  public static double[] bottom(double[] list, int size, int n) {
    return bottom(OPTION_CLEAN, list, size, n);
  }

  /**
   * Pick the bottom N from the data using ascending order, i.e. find the bottom n smallest values.
   *
   * @param list the data list
   * @param size The size of the list
   * @param n The number N to select
   * @return The bottom N
   */
  public static float[] bottom(float[] list, int size, int n) {
    return bottom(OPTION_CLEAN, list, size, n);
  }

  /**
   * Pick the bottom N from the data using ascending order, i.e. find the bottom n smallest values.
   *
   * @param options the options
   * @param list the data list
   * @param n The number N to select
   * @return The bottom N
   */
  public static double[] bottom(int options, double[] list, int n) {
    if (list == null) {
      return ArrayUtils.EMPTY_DOUBLE_ARRAY;
    }
    return bottom(options, list, list.length, n);
  }

  /**
   * Pick the bottom N from the data using ascending order, i.e. find the bottom n smallest values.
   *
   * @param options the options
   * @param list the data list
   * @param n The number N to select
   * @return The bottom N
   */
  public static float[] bottom(int options, float[] list, int n) {
    if (list == null) {
      return ArrayUtils.EMPTY_FLOAT_ARRAY;
    }
    return bottom(options, list, list.length, n);
  }

  /**
   * Pick the bottom N from the data using ascending order, i.e. find the bottom n smallest values.
   *
   * @param options the options
   * @param list the data list
   * @param size The size of the list
   * @param n The number N to select
   * @return The bottom N
   */
  public static double[] bottom(int options, double[] list, int size, int n) {
    if (list == null || size <= 0) {
      return ArrayUtils.EMPTY_DOUBLE_ARRAY;
    }
    final int length = Math.min(size, list.length);
    if (length <= n) {
      return bottomFinish(list.clone(), options);
    }
    if (n < SIZE_TO_USE_SELECTOR) {
      return new DoubleSelector(n).bottom(options | OPTION_NO_CLONE, list, length);
    }
    return new DoubleHeap(n).bottom(options | OPTION_NO_CLONE, list, length);
  }

  /**
   * Pick the bottom N from the data using ascending order, i.e. find the bottom n smallest values.
   *
   * @param options the options
   * @param list the data list
   * @param size The size of the list
   * @param n The number N to select
   * @return The bottom N
   */
  public static float[] bottom(int options, float[] list, int size, int n) {
    if (list == null || size <= 0) {
      return ArrayUtils.EMPTY_FLOAT_ARRAY;
    }
    final int length = Math.min(size, list.length);
    if (length <= n) {
      return bottomFinish(list.clone(), options);
    }
    if (n < SIZE_TO_USE_SELECTOR) {
      return new FloatSelector(n).bottom(options | OPTION_NO_CLONE, list, length);
    }
    return new FloatHeap(n).bottom(options | OPTION_NO_CLONE, list, length);
  }

  /**
   * Pick the top N from the data using ascending order, i.e. find the top n smallest values.
   *
   * @param list the data list
   * @param n The number N to select
   * @return The top N
   */
  public static double[] top(double[] list, int n) {
    if (list == null) {
      return ArrayUtils.EMPTY_DOUBLE_ARRAY;
    }
    return top(list, list.length, n);
  }

  /**
   * Pick the top N from the data using ascending order, i.e. find the top n smallest values.
   *
   * @param list the data list
   * @param n The number N to select
   * @return The top N
   */
  public static float[] top(float[] list, int n) {
    if (list == null) {
      return ArrayUtils.EMPTY_FLOAT_ARRAY;
    }
    return top(list, list.length, n);
  }

  /**
   * Pick the top N from the data using ascending order, i.e. find the top n smallest values.
   *
   * @param list the data list
   * @param size The size of the list
   * @param n The number N to select
   * @return The top N
   */
  public static double[] top(double[] list, int size, int n) {
    return top(OPTION_CLEAN, list, size, n);
  }

  /**
   * Pick the top N from the data using ascending order, i.e. find the top n smallest values.
   *
   * @param list the data list
   * @param size The size of the list
   * @param n The number N to select
   * @return The top N
   */
  public static float[] top(float[] list, int size, int n) {
    return top(OPTION_CLEAN, list, size, n);
  }

  /**
   * Pick the top N from the data using ascending order, i.e. find the top n smallest values.
   *
   * @param options the options
   * @param list the data list
   * @param n The number N to select
   * @return The top N
   */
  public static double[] top(int options, double[] list, int n) {
    if (list == null) {
      return ArrayUtils.EMPTY_DOUBLE_ARRAY;
    }
    return top(options, list, list.length, n);
  }

  /**
   * Pick the top N from the data using ascending order, i.e. find the top n smallest values.
   *
   * @param options the options
   * @param list the data list
   * @param n The number N to select
   * @return The top N
   */
  public static float[] top(int options, float[] list, int n) {
    if (list == null) {
      return ArrayUtils.EMPTY_FLOAT_ARRAY;
    }
    return top(options, list, list.length, n);
  }

  /**
   * Pick the top N from the data using ascending order, i.e. find the top n smallest values.
   *
   * @param options the options
   * @param list the data list
   * @param size The size of the list
   * @param n The number N to select
   * @return The top N
   */
  public static double[] top(int options, double[] list, int size, int n) {
    if (list == null || size <= 0) {
      return ArrayUtils.EMPTY_DOUBLE_ARRAY;
    }
    final int length = Math.min(size, list.length);
    if (length <= n) {
      return topFinish(list.clone(), options);
    }
    if (n < SIZE_TO_USE_SELECTOR) {
      return new DoubleSelector(n).top(options | OPTION_NO_CLONE, list, length);
    }
    return new DoubleHeap(n).top(options | OPTION_NO_CLONE, list, length);
  }

  /**
   * Pick the top N from the data using ascending order, i.e. find the top n smallest values.
   *
   * @param options the options
   * @param list the data list
   * @param size The size of the list
   * @param n The number N to select
   * @return The top N
   */
  public static float[] top(int options, float[] list, int size, int n) {
    if (list == null || size <= 0) {
      return ArrayUtils.EMPTY_FLOAT_ARRAY;
    }
    final int length = Math.min(size, list.length);
    if (length <= n) {
      return topFinish(list.clone(), options);
    }
    if (n < SIZE_TO_USE_SELECTOR) {
      return new FloatSelector(n).top(options | OPTION_NO_CLONE, list, length);
    }
    return new FloatHeap(n).top(options | OPTION_NO_CLONE, list, length);
  }

  // The following select routine is copied from:
  // Numerical Recipes in C++, The Art of Scientific Computing, 2nd Edition, Cambridge Press.
  // CHECKSTYLE.OFF: LocalVariableName

  /**
   * Given k in [0..n-1] returns an array value from arr[0..n-1] such that k array values are less
   * than or equal to the one returned. The input array will be rearranged to have this value in
   * arr[k], with all smaller elements moved to arr[0..k-1] (in arbitrary order) and all larger
   * elements in arr[k+1..n-1] (also in arbitrary order).
   *
   * @param k the k
   * @param n the n
   * @param arr the arr
   * @return the value
   */
  public static double select(int k, int n, double[] arr) {
    int i;
    int ir;
    int j;
    int l;
    int mid;
    double a;

    l = 0;
    ir = n - 1;
    for (;;) {
      if (ir <= l + 1) {
        if (ir == l + 1 && arr[ir] < arr[l]) {
          swap(arr, l, ir);
        }
        return arr[k];
      }
      mid = (l + ir) >>> 1;
      swap(arr, mid, l + 1);
      if (arr[l] > arr[ir]) {
        swap(arr, l, ir);
      }
      if (arr[l + 1] > arr[ir]) {
        swap(arr, l + 1, ir);
      }
      if (arr[l] > arr[l + 1]) {
        swap(arr, l, l + 1);
      }
      i = l + 1;
      j = ir;
      a = arr[l + 1];
      boolean running = true;
      while (running) {
        do {
          i++;
        }
        while (arr[i] < a);
        do {
          j--;
        }
        while (arr[j] > a);
        if (j < i) {
          running = false;
        } else {
          swap(arr, i, j);
        }
      }
      arr[l + 1] = arr[j];
      arr[j] = a;
      if (j >= k) {
        ir = j - 1;
      }
      if (j <= k) {
        l = i;
      }
    }
  }

  /**
   * Given k in [0..n-1] returns an array value from arr[0..n-1] such that k array values are less
   * than or equal to the one returned. The input array will be rearranged to have this value in
   * arr[k], with all smaller elements moved to arr[0..k-1] (in arbitrary order) and all larger
   * elements in arr[k+1..n-1] (also in arbitrary order).
   *
   * @param k the k
   * @param n the n
   * @param arr the arr
   * @return the value
   */
  public static float select(int k, int n, float[] arr) {
    int i;
    int ir;
    int j;
    int l;
    int mid;
    float a;

    l = 0;
    ir = n - 1;
    for (;;) {
      if (ir <= l + 1) {
        if (ir == l + 1 && arr[ir] < arr[l]) {
          swap(arr, l, ir);
        }
        return arr[k];
      }
      mid = (l + ir) >>> 1;
      swap(arr, mid, l + 1);
      if (arr[l] > arr[ir]) {
        swap(arr, l, ir);
      }
      if (arr[l + 1] > arr[ir]) {
        swap(arr, l + 1, ir);
      }
      if (arr[l] > arr[l + 1]) {
        swap(arr, l, l + 1);
      }
      i = l + 1;
      j = ir;
      a = arr[l + 1];
      boolean running = true;
      while (running) {
        do {
          i++;
        }
        while (arr[i] < a);
        do {
          j--;
        }
        while (arr[j] > a);
        if (j < i) {
          running = false;
        } else {
          swap(arr, i, j);
        }
      }
      arr[l + 1] = arr[j];
      arr[j] = a;
      if (j >= k) {
        ir = j - 1;
      }
      if (j <= k) {
        l = i;
      }
    }
  }

  // CHECKSTYLE.ON: LocalVariableName

  /**
   * Swap the two indices.
   *
   * @param data the data
   * @param index1 the first index
   * @param index2 the second index
   */
  static void swap(double[] data, int index1, int index2) {
    final double temp = data[index1];
    data[index1] = data[index2];
    data[index2] = temp;
  }

  /**
   * Swap the two indices.
   *
   * @param data the data
   * @param index1 the first index
   * @param index2 the second index
   */
  static void swap(float[] data, int index1, int index2) {
    final float temp = data[index1];
    data[index1] = data[index2];
    data[index2] = temp;
  }

  /**
   * Check the number is strictly positive ({@code > 0}).
   *
   * @param number the number
   */
  static void checkStrictlyPositive(int number) {
    if (number < 1) {
      throw new IllegalArgumentException("N must be strictly positive: " + number);
    }
  }
}
