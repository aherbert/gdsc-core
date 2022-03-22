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

import java.util.logging.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.test.utils.TestLogging;

@SuppressWarnings({"javadoc"})
class MemoryUtilsTest {

  @Test
  void canRunGarbageCollector() {
    // Just test this does not error
    MemoryUtils.runGarbageCollector();
  }

  @Test
  void measureSizeThrows() {
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> MemoryUtils.measureSize(0, Object::new));
  }

  @Test
  void canMeasureSize() {
    final int arraySize = 100;
    final long size = MemoryUtils.measureSize(1000, () -> new int[arraySize]);
    // This is the expected raw byte size of the array. It does not include
    // extra size for memory references etc.
    final double expected = Integer.BYTES * arraySize;
    // Allow a margin for error
    final double error = DoubleEquality.relativeError(size, expected);
    Logger.getLogger(getClass().getName()).log(TestLogging.getResultRecord(error < 0.2,
        "Memory expected=%s : measured=%d : error=%f", expected, size, error));
    // This is flaky so do not assert the test
    // Assertions.assertEquals(expected, size, expected * 0.1);
  }

  /**
   * Test the method to create a new capacity.
   */
  @Test
  void testCreateNewCapacity() {
    // Start from the default
    int capacity = 11;
    for (;;) {
      final int newCapacity = MemoryUtils.createNewCapacity(capacity + 1, capacity);
      Assertions.assertTrue(newCapacity >= capacity + 1);
      capacity = newCapacity;
      if (capacity == Integer.MAX_VALUE) {
        break;
      }
    }

    // Stop increasing in jumps at the safe max capacity
    final int safeMaxCapacity = Integer.MAX_VALUE - 8;
    Assertions.assertEquals(safeMaxCapacity,
        MemoryUtils.createNewCapacity(safeMaxCapacity - 5, safeMaxCapacity - 10));
    // Approach max value in single step increments
    for (int i = 1; i <= 8; i++) {
      Assertions.assertEquals(safeMaxCapacity + i,
          MemoryUtils.createNewCapacity(safeMaxCapacity + i, safeMaxCapacity));
      Assertions.assertEquals(safeMaxCapacity + i,
          MemoryUtils.createNewCapacity(safeMaxCapacity + i, safeMaxCapacity + i - 1));
    }

    Assertions.assertThrows(OutOfMemoryError.class,
        () -> MemoryUtils.createNewCapacity(1 + Integer.MAX_VALUE, 10));
    Assertions.assertThrows(OutOfMemoryError.class,
        () -> MemoryUtils.createNewCapacity(1 + Integer.MAX_VALUE, safeMaxCapacity));
    Assertions.assertThrows(OutOfMemoryError.class,
        () -> MemoryUtils.createNewCapacity(1 + Integer.MAX_VALUE, Integer.MAX_VALUE));
  }
}
