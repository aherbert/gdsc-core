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
package uk.ac.sussex.gdsc.core.generics;

import uk.ac.sussex.gdsc.test.junit5.*;
import uk.ac.sussex.gdsc.test.rng.RngFactory;
import org.junit.jupiter.api.*;

import uk.ac.sussex.gdsc.test.junit5.*;
import uk.ac.sussex.gdsc.test.rng.RngFactory;


import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Allow work to be added to a LIFO stack of size 1 in a synchronised manner. <p> Provides a method
 * to replace the current top of the stack to allow a LIFO functionality when the stack is full. <p>
 * The stack is closeable to prevent further additions. Items can still be removed from a closed
 * stack.
 *
 * @param <E> the element type
 * @since 1.2.0
 */
public class ConcurrentMonoStack<E> {
  // We only support a stack size of 1
  private E item = null;

  /** Main lock guarding all access */
  final ReentrantLock lock;

  /** Condition for waiting takes */
  private final Condition notEmpty;

  /** Condition for waiting puts */
  private final Condition notFull;

  /** The closed flag. Should only be modified when holding lock. */
  private boolean closed = false;

  /**
   * The closed and empty flag. This is used to avoid synchronisation when closed. Should only be
   * modified when holding lock.
   */
  private boolean closedAndEmpty = false;

  /** Flag specifying the behaviour if closed. */
  private boolean throwIfClosed = false;

  /**
   * Creates an {@code ConcurrentMonoStack} with the default access policy.
   */
  public ConcurrentMonoStack() {
    this(false);
  }

  /**
   * Creates an {@code ConcurrentMonoStack} with the specified access policy.
   *
   * @param fair if {@code true} then stack accesses for threads blocked on insertion or removal,
   *        are processed in FIFO order; if {@code false} the access order is unspecified.
   */
  public ConcurrentMonoStack(boolean fair) {
    lock = new ReentrantLock(fair);
    notEmpty = lock.newCondition();
    notFull = lock.newCondition();
  }

  /**
   * Checks if is closed. No additions are possible when the stack is closed. Items can still be
   * removed.
   *
   * @return true, if is closed
   */
  public boolean isClosed() {
    return closed;
  }

  /**
   * Checks if is closed. No additions are possible when the stack is closed. Items cannot be
   * removed when the stack is empty. <p> If the stack is closed and empty then synchronisation is
   * avoided on all methods that use the stack since it cannot change.
   *
   * @return true, if is closed and empty
   */
  public boolean isClosedAndEmpty() {
    return closedAndEmpty;
  }

  /**
   * Close the stack. No additions are possible when the stack is closed. Items can still be
   * removed.
   *
   * @param clear Set to true to clear the stack
   */
  public void close(boolean clear) {
    if (closed) {
      return;
    }

    final ReentrantLock lock = this.lock;
    lock.lock();
    try {
      closed = true;
      if (clear) {
        clear();
        closedAndEmpty = true;
      }

      // Release anything waiting to put items in the stack.
      // Nothing can be added when it is closed.
      while (lock.hasWaiters(notFull)) {
        notFull.signal();
      }
      // Release anything waiting for the stack.
      // This is because the stack will never fill when closed
      // and prevents stale threads waiting forever.
      while (lock.hasWaiters(notEmpty)) {
        notEmpty.signal();
      }
    } finally {
      lock.unlock();
    }
  }

  /**
   * Checks if is throw if closed flag.
   *
   * @return true, if IllegalStateException is thrown by additions/removals when closed
   */
  public boolean isThrowIfClosed() {
    return throwIfClosed;
  }

  /**
   * Sets the throw if closed flag. <p> If true then additions to the closed stack that normally
   * block when full will throw an exception. The default is to ignore them. <p> If true then
   * removals from the stack that normally block will throw an exception. The default is to return
   * null. <p> The exception will be an IllegalStateException.
   *
   * @param throwIfClosed the new throw if closed
   */
  public void setThrowIfClosed(boolean throwIfClosed) {
    final ReentrantLock lock = this.lock;
    lock.lock();
    try {
      this.throwIfClosed = throwIfClosed;
    } finally {
      lock.unlock();
    }
  }

  /**
   * Throws NullPointerException if argument is null.
   *
   * @param v the element
   */
  private static void checkNotNull(Object v) {
    if (v == null) {
      throw new NullPointerException();
    }
  }

  /**
   * Inserts element and signals. Call only when holding lock.
   */
  private void enqueue(E x) {
    // assert lock.getHoldCount() == 1;
    // assert items[putIndex] == null;
    item = x;
    notEmpty.signal();
  }

  /**
   * Extracts element and signals. Call only when holding lock.
   */
  private E dequeue() {
    // assert lock.getHoldCount() == 1;
    // assert items[takeIndex] != null;
    final E x = item;
    item = null;
    // Set the closedAndEmpty flag to avoid synchronisation in a further dequeue methods
    if (closed) {
      closedAndEmpty = true;
    }
    notFull.signal();
    return x;
  }

  /**
   * Inserts the specified element into the stack waiting if necessary for space to become
   * available. <p> If closed then either ignores the element or throws an exception. <p> If the
   * stack is closed while waiting then this method will unblock and ignore the element. Callers
   * should check the return value to check if further items that will be ignored by a closed stack.
   *
   * @param e the element to add
   * @return true, if successfully added to the stack
   * @throws NullPointerException if the specified element is null
   * @throws InterruptedException If interrupted while waiting
   * @throws IllegalStateException If closed and {@link #isThrowIfClosed()} is true
   */
  public boolean push(E e) throws InterruptedException {
    // Don't lock if closed
    if (closed) {
      if (throwIfClosed) {
        throw new IllegalStateException("No additions to a closed stack");
      }
      return false;
    }

    checkNotNull(e);
    final ReentrantLock lock = this.lock;
    lock.lockInterruptibly();
    try {
      if (closed) {
        if (throwIfClosed) {
          throw new IllegalStateException("No additions to a closed stack");
        }
        return false;
      }

      while (item != null) {
        notFull.await();
      }

      if (closed) {
        if (throwIfClosed) {
          throw new IllegalStateException("No additions to a closed stack");
        }
        return false;
      }

      enqueue(e);
      return true;
    } finally {
      lock.unlock();
    }
  }

  /**
   * Inserts the specified element into the stack if it is possible to do so immediately without
   * violating capacity restrictions, returning {@code true} upon success and {@code false} if no
   * space is currently available.
   *
   * @param e the element to add
   * @return true, if successfully added to the stack
   * @throws NullPointerException if the specified element is null
   */
  public boolean offer(E e) {
    // Don't lock if closed
    if (closed) {
      return false;
    }

    checkNotNull(e);
    final ReentrantLock lock = this.lock;
    lock.lock();
    try {
      if (closed || item != null) {
        return false;
      }
      enqueue(e);
      return true;
    } finally {
      lock.unlock();
    }
  }

  /**
   * Inserts the specified element into the stack replacing the current head position. <p> If closed
   * then either ignores the element or throws an exception.
   *
   * @param e the element to add
   * @return true, if successfully added to the stack
   * @throws NullPointerException if the specified element is null
   * @throws IllegalStateException If closed and {@link #isThrowIfClosed()} is true
   */
  public boolean insert(E e) {
    // Don't lock if closed
    if (closed) {
      if (throwIfClosed) {
        throw new IllegalStateException("No additions to a closed stack");
      }
      return false;
    }

    checkNotNull(e);
    final ReentrantLock lock = this.lock;
    lock.lock();
    try {
      if (closed) {
        if (throwIfClosed) {
          throw new IllegalStateException("No additions to a closed stack");
        }
        return false;
      }
      enqueue(e);
      return true;
    } finally {
      lock.unlock();
    }
  }

  /**
   * Retrieves and removes the head of the stack, waiting if necessary until an element becomes
   * available. <p> If the stack is closed while waiting then this method will unblock and either
   * return null or throw an exception. <p> Callers should check if the return value is null and
   * appropriately handle a closed empty stack, i.e. do not continue to call this method as it will
   * no longer block but will have locking synchronisation overhead.
   *
   * @return the head of this stack
   * @throws InterruptedException if interrupted while waiting
   * @throws IllegalStateException If closed and {@link #isThrowIfClosed()} is true
   */
  public E pop() throws InterruptedException, IllegalStateException {
    // Avoid synchronisation if this is closed and empty (since nothing can be added)
    if (closedAndEmpty) {
      if (throwIfClosed) {
        throw new IllegalStateException("Empty closed stack");
      }
      return null;
    }

    final ReentrantLock lock = this.lock;
    lock.lockInterruptibly();
    try {
      while (item == null) {
        if (closed) {
          // If the count is 0 and nothing can be queued we should return.
          if (throwIfClosed) {
            throw new IllegalStateException("Empty closed stack");
          }
          return null;
        }

        notEmpty.await();
      }

      return dequeue();
    } finally {
      lock.unlock();
    }
  }

  /**
   * Retrieves and removes the head of the stack, or returns {@code null} if this stack is empty.
   *
   * @return the head of this stack, or {@code null} if this stack is empty
   */
  public E poll() {
    // Avoid synchronisation if this is closed and empty (since nothing can be added)
    if (closedAndEmpty) {
      return null;
    }

    final ReentrantLock lock = this.lock;
    lock.lock();
    try {
      // Allow poll if closed for additions
      return (item == null) ? null : dequeue();
    } finally {
      lock.unlock();
    }

  }

  /**
   * Retrieves, but does not remove, the head of the stack, or returns {@code null} if this stack is
   * empty.
   *
   * @return the head of this stack, or {@code null} if this stack is empty
   */
  public E peek() {
    // Avoid synchronisation if this is closed and empty (since nothing can be added)
    if (closedAndEmpty) {
      return null;
    }

    final ReentrantLock lock = this.lock;
    lock.lock();
    try {
      return item; // null when stack is empty
    } finally {
      lock.unlock();
    }
  }

  /**
   * Atomically removes all of the elements from this stack. The stack will be empty after this call
   * returns.
   */
  public void clear() {
    // Avoid synchronisation if this is closed and empty (since nothing can be added)
    if (closedAndEmpty) {
      return;
    }

    final ReentrantLock lock = this.lock;
    lock.lock();
    try {
      if (item != null) {
        item = null;
        if (lock.hasWaiters(notFull)) {
          notFull.signal();
        }
      }
    } finally {
      lock.unlock();
    }
  }

  /**
   * Returns the number of elements in this stack.
   *
   * @return the number of elements in this stack
   */
  public int size() {
    // Avoid synchronisation if this is closed and empty (since nothing can be added)
    if (closedAndEmpty) {
      return 0;
    }

    final ReentrantLock lock = this.lock;
    lock.lock();
    try {
      return (item == null) ? 1 : 0;
    } finally {
      lock.unlock();
    }
  }
}
