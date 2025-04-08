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

import java.util.Collection;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import uk.ac.sussex.gdsc.core.utils.ValidationUtils;

/**
 * A blocking queue that can be closed.
 *
 * <p>This is a simplified implementation of a queue backed by array storage. It is not intended as
 * a replacement for {@link java.util.concurrent.ArrayBlockingQueue} and it does not implement the
 * {@link Collection} interface.
 *
 * <p>The queue supports put and take of non-null items. These actions will block until the queue
 * has space to allow put or has items to allow take.
 *
 * <p>Modifications have been made to allow the queue to be closed to puts. When closed the
 * behaviour is non-blocking. Any blocked threads waiting to put or take are released. No puts to
 * the queue are allowed and any take operations return items or null when the queue is empty. The
 * transition to closed is not reversible.
 *
 * <p>A pipeline to process items can be created using producer and consumer threads. If the queue
 * is closed all threads will unblock. The producer should respond to the boolean flag returned by
 * put indicating if the item was added to the queue. If the false then the producer can accept this
 * as a shutdown signal. The consumer should check taken items are not null. If null then the
 * consumer can accept this as a shutdown signal.
 *
 * @param <E> the element type
 * @see ConcurrentMonoStack
 */
public class CloseableBlockingQueue<E> {
  /** The queued items. */
  private final Object[] queuedItems;

  /** The lock to hold when modifying/querying the queue. */
  private final ReentrantLock queueLock;

  /** Condition used to notify a thread waiting to put items. */
  private final Condition notFull;

  /** Condition used to notify a thread waiting to take items. */
  private final Condition notEmpty;

  /** The index of the next item to take. */
  private int takeIndex;

  /** The index of the next item to put. */
  private int putIndex;

  /** The number of items in the queue. */
  private int size;

  /**
   * The closed flag. Should only be modified when holding the lock. Marked volatile to ensure
   * synchronisation among different threads.
   */
  private volatile boolean closed;

  /**
   * The closed and empty flag. This is used to avoid synchronisation when closed. Should only be
   * modified when holding lock. Marked volatile to ensure synchronisation among different threads.
   */
  private volatile boolean closedAndEmpty;

  /**
   * Creates an {@code CloseableBlockingQueue} with the given (fixed) capacity and default access
   * policy.
   *
   * @param capacity the capacity of this queue
   * @throws IllegalArgumentException if {@code capacity < 1}
   */
  public CloseableBlockingQueue(int capacity) {
    this(capacity, false);
  }

  /**
   * Creates an {@code CloseableBlockingQueue} with the given (fixed) capacity and the specified
   * access policy.
   *
   * @param capacity the capacity of this queue
   * @param fair if {@code true} then queue accesses for threads blocked on insertion or removal,
   *        are processed in FIFO order; if {@code false} the access order is unspecified.
   * @throws IllegalArgumentException if {@code capacity < 1}
   */
  public CloseableBlockingQueue(int capacity, boolean fair) {
    ValidationUtils.checkStrictlyPositive(capacity, "capacity");
    this.queuedItems = new Object[capacity];
    queueLock = new ReentrantLock(fair);
    notFull = queueLock.newCondition();
    notEmpty = queueLock.newCondition();
  }

  /**
   * Checks if is closed. No additions are possible when the queue is closed. Items can still be
   * removed.
   *
   * @return true, if is closed
   */
  public boolean isClosed() {
    return closed;
  }

  /**
   * Checks if is closed. No additions are possible when the queue is closed. Items cannot be
   * removed when the queue is empty.
   *
   * <p>If the queue is closed and empty then synchronisation is avoided on all methods that use the
   * queue since it cannot change.
   *
   * @return true, if is closed and empty
   */
  public boolean isClosedAndEmpty() {
    return closedAndEmpty;
  }

  /**
   * Update a closed state to closed and empty if the size is zero.
   *
   * <p>This method should be called when the state is transitioned to closed or the size is
   * reduced.
   */
  private void updateClosedAndEmpty() {
    closedAndEmpty = (closed && size == 0);
  }

  /**
   * Close the queue. No additions are possible when the queue is closed. Items can still be
   * removed.
   *
   * <p>The transition to closed is not reversible. Closing a queue is a shutdown signal for any
   * threads waiting to put or take.
   *
   * @param clear Set to true to clear the queue
   */
  public void close(boolean clear) {
    if (closedAndEmpty) {
      return;
    }

    final ReentrantLock lock = this.queueLock;
    lock.lock();
    try {
      closed = true;
      updateClosedAndEmpty();
      if (clear) {
        clear();
      }

      // Release anything waiting to put items in the queue.
      // Nothing can be added when it is closed.
      while (lock.hasWaiters(notFull)) {
        notFull.signal();
      }
      // Release anything waiting for the queue.
      // This is because the queue will never fill when closed
      // and prevents stale threads waiting forever.
      while (lock.hasWaiters(notEmpty)) {
        notEmpty.signal();
      }
    } finally {
      lock.unlock();
    }
  }

  /**
   * Inserts the specified element at the tail of this queue, waiting for space to become available
   * if the queue is full.
   *
   * <p>If closed then ignores the element.
   *
   * <p>If the queue is closed while waiting then this method will unblock and ignore the element.
   * Callers should check the return value to check if further items that will be ignored by a
   * closed queue.
   *
   * @param e the element to add
   * @return true, if successfully added to the queue
   * @throws InterruptedException If interrupted while waiting
   * @throws NullPointerException If the specified element is null
   */
  public boolean put(E e) throws InterruptedException {
    // Don't lock if closed
    if (closed) {
      return false;
    }

    ValidationUtils.checkNotNull(e, "element");
    final ReentrantLock lock = this.queueLock;
    lock.lockInterruptibly();
    try {
      if (closed) {
        return false;
      }

      while (size == queuedItems.length) {
        notFull.await();
        if (closed) {
          // If nothing more can be queued we should return.
          return false;
        }
      }

      // Only here if not closed and not full

      final Object[] items = this.queuedItems;
      items[putIndex] = e;
      // Wrap the circular storage
      putIndex++;
      if (putIndex == items.length) {
        putIndex = 0;
      }
      size++;
      // Notify waiting threads we are not empty
      notEmpty.signal();
      return true;
    } finally {
      lock.unlock();
    }
  }

  /**
   * Retrieves and removes the head of this queue, waiting if necessary until an element becomes
   * available.
   *
   * <p>If the queue is closed while waiting then this method will unblock and return null.
   *
   * <p>Callers should check if the return value is null and appropriately handle a closed empty
   * queue, i.e. do not continue to call this method as it will no longer block but will have
   * locking synchronisation overhead.
   *
   * @return the head of the queue
   * @throws InterruptedException the interrupted exception
   */
  public E take() throws InterruptedException {
    // Avoid synchronisation if this is closed and empty (since nothing can be added)
    if (closedAndEmpty) {
      return null;
    }

    final ReentrantLock lock = this.queueLock;
    lock.lockInterruptibly();
    try {
      while (size == 0) {
        if (closed) {
          // If the size is 0 and nothing more can be queued we should return.
          return null;
        }
        notEmpty.await();
      }

      // Only here if size is not 0 (we allow takes when the queue is closed but not empty)

      final Object[] items = this.queuedItems;
      @SuppressWarnings("unchecked")
      final E x = (E) items[takeIndex];
      // Clear memory
      items[takeIndex] = null;
      // Wrap the circular storage
      takeIndex++;
      if (takeIndex == items.length) {
        takeIndex = 0;
      }
      size--;
      updateClosedAndEmpty();
      // Notify a waiting thread we are not full
      notFull.signal();
      return x;
    } finally {
      lock.unlock();
    }
  }

  /**
   * Returns the number of elements in this queue.
   *
   * @return the number of elements in this queue
   */
  public int size() {
    // Avoid synchronisation if this is closed and empty (since nothing can be added)
    if (closedAndEmpty) {
      return 0;
    }

    final ReentrantLock lock = this.queueLock;
    lock.lock();
    try {
      return size;
    } finally {
      lock.unlock();
    }
  }

  /**
   * Atomically removes all of the elements from this queue. The queue will be empty after this call
   * returns.
   */
  public void clear() {
    // Avoid synchronisation if this is closed and empty (since nothing can be added)
    if (closedAndEmpty) {
      return;
    }

    final ReentrantLock lock = this.queueLock;
    lock.lock();
    try {
      int numberCleared = size;
      if (numberCleared != 0) {
        // This requires the following:
        // for (i=takeIndex; i<putIndex; i++)
        // But modified for a circular wrap, so use i != putIndex
        // Note: The put index can be equal to the take index if the queue is full
        // so clear the first item and then loop
        final Object[] items = this.queuedItems;
        int index = takeIndex;
        do {
          items[index] = null;
          index++;
          if (index == items.length) {
            index = 0;
          }
        } while (index != putIndex);
        takeIndex = putIndex;
        size = 0;
        updateClosedAndEmpty();
        // Notify waiting threads we are not full
        while (numberCleared > 0 && lock.hasWaiters(notFull)) {
          notFull.signal();
          numberCleared--;
        }
      }
    } finally {
      lock.unlock();
    }
  }
}
