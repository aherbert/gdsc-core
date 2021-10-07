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

import java.util.function.Supplier;

/**
 * Memory utilities.
 */
public final class MemoryUtils {
  /**
   * The maximum size buffer to safely allocate. Attempts to allocate above this size may fail.
   *
   * <p>This is set to the same size used in the JDK {@code java.util.ArrayList}:
   *
   * <blockquote> Some VMs reserve some header words in an array. Attempts to allocate larger arrays
   * may result in OutOfMemoryError: Requested array size exceeds VM limit. </blockquote>
   */
  private static final int MAX_BUFFER_SIZE = Integer.MAX_VALUE - 8;

  /** The runtime for the currrent Java application. */
  private static final Runtime runtime = Runtime.getRuntime();

  /**
   * No public construction.
   */
  private MemoryUtils() {}

  /**
   * Run the garbage collector multiple times to free memory.
   *
   * @see Runtime#gc()
   */
  public static void runGarbageCollector() {
    // It helps to call Runtime.gc() using several method calls:
    for (int r = 0; r < 4; ++r) {
      runGarbageCollectorMulitpleTimes();
    }
  }

  private static void runGarbageCollectorMulitpleTimes() {
    long usedMem1 = getUsedMemory();
    long usedMem2 = Long.MAX_VALUE;
    // Iterate until no memory is freed or a simple limit is reached.
    for (int i = 0; (usedMem1 < usedMem2) && (i < 500); ++i) {
      runGarbageCollectorOnce();
      Thread.currentThread();
      Thread.yield();

      usedMem2 = usedMem1;
      usedMem1 = getUsedMemory();
    }
  }

  /**
   * Run the garbage collector once to free memory.
   *
   * @see Runtime#gc()
   */
  public static void runGarbageCollectorOnce() {
    runtime.runFinalization();
    runtime.gc();
  }

  /**
   * Gets the used memory.
   *
   * <p>Returns (total memory) - (free memory).
   *
   * @return the used memory
   * @see Runtime#totalMemory()
   * @see Runtime#freeMemory()
   */
  public static long getUsedMemory() {
    return getTotalMemory() - getFreeMemory();
  }

  /**
   * Gets the total memory.
   *
   * @return the total memory
   * @see Runtime#totalMemory()
   */
  public static long getTotalMemory() {
    return runtime.totalMemory();
  }

  /**
   * Gets the free memory.
   *
   * @return the free memory
   * @see Runtime#freeMemory()
   */
  public static long getFreeMemory() {
    return runtime.freeMemory();
  }

  /**
   * Measure the memory size of objects produced by the supplier.
   *
   * <p>This methods runs the garbage collector repeatedly before and after creating a number of
   * objects.
   *
   * <p>Note: It will not work in a multi-threaded application as the memory is based on the current
   * runtime.
   *
   * @param count the number of objects
   * @param supplier the supplier of the objects
   * @return the memory size of a single object
   * @throws IllegalArgumentException If the count is not strictly positive
   * @see <a href="http://www.javaworld.com/javaworld/javatips/jw-javatip130.html">Java Tip 130: Do
   *      you know your data size?</a>
   */
  public static long measureSize(int count, Supplier<Object> supplier) {
    if (count <= 0) {
      throw new IllegalArgumentException("Count must be strictly positive");
    }

    // Warm up all classes/methods we will use
    MemoryUtils.runGarbageCollector();
    MemoryUtils.getUsedMemory();

    // Array to keep strong references to allocated objects
    Object[] objects = new Object[count];

    long heap1 = 0;
    // Allocate count+1 objects, discard the first one
    for (int i = -1; i < count; ++i) {
      // Create the data
      Object object = supplier.get();

      if (i == -1) {
        // First object.
        // Discard the object.
        object = null;
        // Take a before heap snapshot, i.e. all required classes are initialised but
        // no instances remain
        MemoryUtils.runGarbageCollector();
        heap1 = MemoryUtils.getUsedMemory();
      } else {
        // Hold in memory
        objects[i] = object;
      }
    }
    MemoryUtils.runGarbageCollector();
    final long heap2 = MemoryUtils.getUsedMemory(); // Take an after heap snapshot:

    final long memorySize = Math.round(((double) (heap2 - heap1)) / count);

    // Free memory
    for (int i = 0; i < count; ++i) {
      objects[i] = null;
    }
    objects = null;

    MemoryUtils.runGarbageCollector();

    return memorySize;
  }

  /**
   * Create a new capacity for an array at least as large the minimum required capacity. The old
   * capacity is used to set an initial increase based on the current capacity. If the minimum
   * capacity is negative then this throws an OutOfMemoryError as no array can be allocated.
   *
   * <p>It is assumed the old capacity is positive. Results are undefined if the old capacity is
   * negative.
   *
   * <p>This method is intended for use when resizing arrays:
   *
   * <pre>
   * // Current data and size
   * int[] data = ...;
   * int size = ...;
   *
   * // Data to add (in full) 
   * int[] newData = ...;
   *
   * int oldCapacity = data.length;
   * int minCapacity = data.length + newData.length;
   *
   * // Overflow safe size check
   * if (minCapacity - oldCapacity > 0) {
   *   // Resize required
   *   int newCapacity = MemoryUtils.createNewCapacity(minCapacity, oldCapacity));
   *   data = Arrays.copyOf(data, newCapacity);
   *   System.arraycopy(newData, newData.length, data, size, newData.length);
   *   size += newData.length;
   * }
   * </pre>
   *
   * @param minCapacity the minimum capacity
   * @param oldCapacity the old capacity (must be positive)
   * @return the capacity
   * @throws OutOfMemoryError if the minimum capacity is negative
   */
  public static int createNewCapacity(final int minCapacity, final int oldCapacity) {
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
}
