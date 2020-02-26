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

package uk.ac.sussex.gdsc.core.utils.concurrent;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.apache.commons.lang3.concurrent.ConcurrentRuntimeException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"javadoc"})
public class ConcurrencyUtilsTest {

  @Test
  public void canRefresh() {
    AtomicReference<Integer> reference = new AtomicReference<>();
    Predicate<Integer> test = v -> v.equals(1);
    Supplier<Integer> supplier = () -> Integer.valueOf(1);

    Integer value = ConcurrencyUtils.refresh(reference, test, supplier);
    Assertions.assertEquals(Integer.valueOf(1), value, "Did not generate the value when null");

    Integer refreshedValue = ConcurrencyUtils.refresh(reference, test, supplier);
    Assertions.assertSame(value, refreshedValue, "Did not return the same value when test pass");

    // Add a bad value
    reference.set(2);

    value = ConcurrencyUtils.refresh(reference, test, supplier);
    Assertions.assertEquals(Integer.valueOf(1), value, "Did not generate the value when test fail");
  }

  @Test
  public void canInterruptAndThrowUncheckedIf() {
    Assumptions.assumeFalse(Thread.currentThread().isInterrupted(),
        "Thread should not be interrupted");

    final InterruptedException exception = new InterruptedException();

    ConcurrencyUtils.interruptAndThrowUncheckedIf(false, exception);
    // This will clear the interrupted status
    Assertions.assertTrue(Thread.interrupted(),
        "Thread should be interrupted when condition is false");

    Assertions.assertThrows(ConcurrentRuntimeException.class,
        () -> ConcurrencyUtils.interruptAndThrowUncheckedIf(true, exception));

    Assertions.assertTrue(Thread.interrupted(),
        "Thread should be interrupted when condition is true");
  }
}
