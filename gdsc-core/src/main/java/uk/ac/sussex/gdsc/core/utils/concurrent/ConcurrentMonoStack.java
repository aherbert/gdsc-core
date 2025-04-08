/*-
 * #%L
 * Genome Damage and Stability Centre Core Package
 *
 * Contains core utilities for image analysis and is used by:
 *
 * GDSC ImageJ Plugins - Microscopy image analysis
 *
 * GDSC SMLM ImageJ Plugins - Single molecule localisation microscopy (SMLM)
 * %%
 * Copyright (C) 2011 - 2025 Alex Herbert
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

import java.util.Objects;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Allow work to be added to a Last In First Out (LIFO) stack of size 1 in a synchronised manner.
 * Offers blocking and non-blocking methods to add and take from the stack.
 *
 * <p>Provides a method to replace the current top of the stack to allow a LIFO functionality when
 * the stack is full.
 *
 * <p>Modifications have been made to allow the stack to be closed to puts. When closed the
 * behaviour is non-blocking. Any blocked threads waiting to put or take are released. No puts to
 * the stack are allowed and any take operations return items or null when the stack is empty. The
 * transition to closed is not reversible.
 *
 * <p>A pipeline to process items can be created using producer and consumer threads. If the stack
 * is closed all threads will unblock. The producer should respond to the boolean flag returned by
 * push indicating if the item was added to the stack. If the false then the producer can accept
 * this as a shutdown signal. The consumer should check taken items are not null. If null then the
 * consumer can accept this as a shutdown signal.
 *
 * <p>The stack does not implement a {@code java.util.Collection} interface and can only contain 1
 * element. However the blocking functionality is more similar to the concurrent collections in
 * {@code java.util.concurrent} than an {@link java.util.concurrent.atomic.AtomicReference
 * AtomicReference}. The class and methods have been named using similar wording to a
 * {@link java.util.Deque Deque} which can be used as a stack for adding (push, offer) and removal
 * (pop, poll, peek).
 *
 * @param <E> the element type
 * @see CloseableBlockingQueue
 */
public class ConcurrentMonoStack<E> {

  /** The single item in the mono stack. */
  private E item;

  /** Lock controlling access. */
  private final ReentrantLock lock;

  /** Condition for waiting additions. */
  private final Condition notFull;

  /** Condition for waiting removals. */
  private final Condition notEmpty;

  /** The closed flag. Should only be modified when holding lock. */
  private boolean closed;

  /**
   * The closed and empty flag. This is used to avoid synchronisation when closed. Should only be
   * modified when holding lock.
   */
  private boolean closedAndEmpty;

  /**
   * Creates an {@code ConcurrentMonoStack} with the default access policy.
   */
  public ConcurrentMonoStack() {
    this(false);
  }

  /**
   * Creates an {@code ConcurrentMonoStack} with the specified access policy.
   *
   * @param fair if {@code true} then stack accesses for threads blocked on addition or removal, are
   *        processed in FIFO order; if {@code false} the access order is unspecified.
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
   * @return true if closed
   */
  public boolean isClosed() {
    return closed;
  }

  /**
   * Checks if is closed and empty. No additions are possible when the stack is closed. Items cannot
   * be removed when the stack is empty.
   *
   * <p>If the stack is closed and empty then synchronisation is avoided on all methods that use the
   * stack since it cannot change.
   *
   * @return true if closed and empty
   */
  public boolean isClosedAndEmpty() {
    return closedAndEmpty;
  }

  /**
   * Close the stack. No additions are possible when the stack is closed. Items can still be
   * removed.
   *
   * <p>The transition to closed is not reversible. Closing a queue is a shutdown signal for any
   * threads waiting to push or pop.
   *
   * @param clear Set to true to clear the stack
   */
  public void close(boolean clear) {
    if (closed) {
      return;
    }

    final ReentrantLock localLock = this.lock;
    localLock.lock();
    try {
      closed = true;
      if (clear) {
        // No need to call clear() as we hold the lock and notify waiters next
        item = null;
        closedAndEmpty = true;
      }

      // Release anything waiting to put items in the stack.
      // Nothing can be added when it is closed.
      while (localLock.hasWaiters(notFull)) {
        notFull.signal();
      }
      // Release anything waiting for the stack to fill.
      // This is because the stack will never fill when closed
      // and prevents stale threads waiting forever.
      while (localLock.hasWaiters(notEmpty)) {
        notEmpty.signal();
      }
    } finally {
      localLock.unlock();
    }
  }

  /**
   * Pushes an element onto the stack and signals not empty.
   */
  private void doPush(E x) {
    item = x;
    notEmpty.signal();
  }

  /**
   * Pops the most recently added element from the stack and signals not full.
   */
  private E doPop() {
    final E x = item;
    item = null;
    // Set the closedAndEmpty flag to avoid synchronisation in a further dequeue methods
    closedAndEmpty = closed;
    notFull.signal();
    return x;
  }

  /**
   * Adds the specified element onto the stack waiting if necessary for space to become available.
   *
   * <p>If closed then ignores the element.
   *
   * <p>If the stack is closed while waiting then this method will unblock and ignore the element.
   * Callers should check the return value to check if further items that will be ignored by a
   * closed stack.
   *
   * @param element the element to add
   * @return true, if successfully added to the stack
   * @throws NullPointerException if the specified element is null
   * @throws InterruptedException If interrupted while waiting
   */
  public boolean push(E element) throws InterruptedException {
    // Don't lock if closed
    if (closed) {
      return false;
    }

    Objects.requireNonNull(element);
    final ReentrantLock localLock = this.lock;
    localLock.lockInterruptibly();
    try {
      if (closed) {
        return false;
      }

      while (item != null) {
        notFull.await();
        if (closed) {
          // If nothing more can be queued we should return.
          return false;
        }
      }

      // Only here if not closed and not full

      doPush(element);
      return true;
    } finally {
      localLock.unlock();
    }
  }

  /**
   * Adds the specified element onto the stack if it is possible to do so immediately without
   * violating capacity restrictions, returning {@code true} upon success and {@code false} if no
   * space is currently available.
   *
   * <p>If closed then ignores the element.
   *
   * @param element the element to add
   * @return true, if successfully added to the stack
   * @throws NullPointerException if the specified element is null
   */
  public boolean offer(E element) {
    // Don't lock if closed
    if (closed) {
      return false;
    }

    Objects.requireNonNull(element);
    final ReentrantLock localLock = this.lock;
    localLock.lock();
    try {
      if (closed || item != null) {
        // If nothing more can be queued we should return.
        return false;
      }
      doPush(element);
      return true;
    } finally {
      localLock.unlock();
    }
  }

  /**
   * Inserts the specified element onto the stack replacing the most recently added element if it
   * exists.
   *
   * <p>If closed then ignores the element.
   *
   * @param element the element to add
   * @return true, if successfully added to the stack
   * @throws NullPointerException if the specified element is null
   */
  public boolean insert(E element) {
    // Don't lock if closed
    if (closed) {
      return false;
    }

    Objects.requireNonNull(element);
    final ReentrantLock localLock = this.lock;
    localLock.lock();
    try {
      if (closed) {
        // If nothing more can be queued we should return.
        return false;
      }
      doPush(element);
      return true;
    } finally {
      localLock.unlock();
    }
  }

  /**
   * Retrieves and removes the most recently added element from the stack, waiting if necessary
   * until an element becomes available.
   *
   * <p>If the stack is closed while waiting then this method will unblock and return null.
   *
   * <p>Callers should check if the return value is null and appropriately handle a closed stack,
   * i.e. do not continue to call this method as it will no longer block but will have locking
   * synchronisation overhead.
   *
   * @return the top of this stack or null if this stack is closed
   * @throws InterruptedException if interrupted while waiting
   */
  public E pop() throws InterruptedException {
    // Avoid synchronisation if this is closed and empty (since nothing can be added)
    if (closedAndEmpty) {
      return null;
    }

    final ReentrantLock localLock = this.lock;
    localLock.lockInterruptibly();
    try {
      while (item == null) {
        if (closed) {
          // If nothing more can be queued we should return.
          return null;
        }
        notEmpty.await();
      }

      // Only here if size is not 0 (we allow takes when the queue is closed but not empty)

      return doPop();
    } finally {
      localLock.unlock();
    }
  }

  /**
   * Retrieves and removes the most recently added element from the stack, or returns {@code null}
   * if this stack is empty.
   *
   * <p>Note: In contrast to {@link #pop()} this method will not wait. If the stack is closed then
   * this method will return null.
   *
   * <p>Callers should check if the return value is null and appropriately handle a closed stack,
   * i.e. do not continue to call this method as it will will have locking synchronisation overhead.
   *
   * @return the top of this stack, or {@code null} if this stack is empty or closed
   */
  public E poll() {
    // Avoid synchronisation if this is closed and empty (since nothing can be added)
    if (closedAndEmpty) {
      return null;
    }

    final ReentrantLock localLock = this.lock;
    localLock.lock();
    try {
      // Allow poll if closed for additions
      return (item == null) ? null : doPop();
    } finally {
      localLock.unlock();
    }
  }

  /**
   * Retrieves, but does not remove, the most recently added element from the stack, or returns
   * {@code null} if this stack is empty.
   *
   * @return the top of this stack, or {@code null} if this stack is empty
   */
  public E peek() {
    // Avoid synchronisation if this is closed and empty (since nothing can be added)
    if (closedAndEmpty) {
      return null;
    }

    final ReentrantLock localLock = this.lock;
    localLock.lock();
    try {
      return item; // null when stack is empty
    } finally {
      localLock.unlock();
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

    final ReentrantLock localLock = this.lock;
    localLock.lock();
    try {
      if (item != null) {
        item = null;
        // Set the closedAndEmpty flag to avoid synchronisation in a further dequeue methods
        closedAndEmpty = closed;
        if (localLock.hasWaiters(notFull)) {
          notFull.signal();
        }
      }
    } finally {
      localLock.unlock();
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

    final ReentrantLock localLock = this.lock;
    localLock.lock();
    try {
      return (item == null) ? 0 : 1;
    } finally {
      localLock.unlock();
    }
  }
}
