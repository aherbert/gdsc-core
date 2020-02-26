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

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Class for simple lock acquisition for threaded applications. Allows multiple threads to view the
 * same lock and only execute code if another thread is not currently in possession of the lock so
 * supporting a type of non-blocking synchronised block execution.
 *
 * <pre>
 * SoftLock lock = new SoftLock();
 *
 * if (lock.acquire()) {
 *   // Do something
 *   lock.release();
 * }
 * // else fall through
 * </pre>
 *
 * <p>The lock does no checks on ownership and thus does not actually enforce locking.
 */
public class SoftLock {

  /** The lock. */
  private final AtomicBoolean lock = new AtomicBoolean();

  /**
   * Acquire the lock. The lock should be released when finished.
   *
   * @return true, if the lock was acquired, else false if already locked
   */
  public boolean acquire() {
    // expect the lock to be unlocked = false
    // update the lock to be locked = true
    // This will return true if the value was changed.
    return lock.compareAndSet(false, true);
  }

  /**
   * Checks if is locked.
   *
   * @return true, if is locked
   */
  public boolean isLocked() {
    return lock.get();
  }

  /**
   * Release the lock.
   *
   * <p>This should be called by the owner of the lock, however this is not enforced.
   */
  public void release() {
    lock.set(false);
  }
}
