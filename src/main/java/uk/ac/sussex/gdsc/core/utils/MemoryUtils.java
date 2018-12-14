/*-
 * #%L
 * Genome Damage and Stability Centre SMLM ImageJ Plugins
 *
 * Software for single molecule localisation microscopy (SMLM)
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

import java.util.function.Supplier;

/**
 * Memory utilities.
 */
public final class MemoryUtils {

  /** The Constant runtime. */
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
}
