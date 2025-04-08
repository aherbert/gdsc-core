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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"javadoc"})
class ConcurrentMonoStackTest {
  @Test
  void putThrowsWithNull() {
    final ConcurrentMonoStack<Integer> queue = new ConcurrentMonoStack<>();
    Assertions.assertThrows(NullPointerException.class, () -> queue.push(null));
  }

  @Test
  void insertThrowsWithNull() {
    final ConcurrentMonoStack<Integer> queue = new ConcurrentMonoStack<>();
    Assertions.assertThrows(NullPointerException.class, () -> queue.insert(null));
  }

  @Test
  void offerThrowsWithNull() {
    final ConcurrentMonoStack<Integer> queue = new ConcurrentMonoStack<>();
    Assertions.assertThrows(NullPointerException.class, () -> queue.offer(null));
  }

  @Test
  void canClear() throws InterruptedException {
    final ConcurrentMonoStack<Integer> queue = new ConcurrentMonoStack<>();
    Assertions.assertEquals(0, queue.size());
    Assertions.assertFalse(queue.isClosed());
    Assertions.assertFalse(queue.isClosedAndEmpty());
    queue.push(1);
    Assertions.assertEquals(1, queue.size());
    queue.clear();
    Assertions.assertEquals(0, queue.size());
    Assertions.assertFalse(queue.isClosed());
    Assertions.assertFalse(queue.isClosedAndEmpty());

    // Clear an empty queue has no effect
    queue.clear();
    Assertions.assertEquals(0, queue.size());
    Assertions.assertFalse(queue.isClosed());
    Assertions.assertFalse(queue.isClosedAndEmpty());
  }

  @Test
  void canClearUnblockWaitingThread()
      throws InterruptedException, ExecutionException, TimeoutException {
    final ConcurrentMonoStack<Integer> queue = new ConcurrentMonoStack<>();
    queue.push(1);

    // Add in a different thread.
    final AtomicBoolean done = new AtomicBoolean();
    final CountDownLatch count = new CountDownLatch(1);
    final ExecutorService es = Executors.newSingleThreadExecutor();
    final Future<?> f = es.submit(() -> {
      count.countDown();
      try {
        queue.push(2);
        done.set(true);
      } catch (final InterruptedException e) {
        throw new RuntimeException(e);
      }
    });

    // Wait for thread to start and it should block
    count.await(10, TimeUnit.SECONDS);
    Assertions.assertFalse(done.get());

    // Clear the queue should unblock the thread
    queue.clear();

    // Wait
    f.get(10, TimeUnit.SECONDS);
    es.shutdown();

    Assertions.assertTrue(done.get());
    Assertions.assertEquals(1, queue.size());
    Assertions.assertEquals(2, queue.pop());
  }

  @Test
  void canClose() throws InterruptedException {
    final ConcurrentMonoStack<Integer> queue = new ConcurrentMonoStack<>();
    Assertions.assertEquals(0, queue.size());
    Assertions.assertFalse(queue.isClosed());
    Assertions.assertFalse(queue.isClosedAndEmpty());
    queue.push(1);
    Assertions.assertEquals(1, queue.size());

    queue.close(false);
    Assertions.assertEquals(1, queue.size());
    Assertions.assertTrue(queue.isClosed());
    Assertions.assertFalse(queue.isClosedAndEmpty());

    // Cannot add to a closed queue
    Assertions.assertFalse(queue.push(99));
    Assertions.assertEquals(1, queue.size());

    // Can still take from a closed queue
    Assertions.assertEquals(1, queue.pop());
    Assertions.assertEquals(0, queue.size());
    Assertions.assertTrue(queue.isClosed());
    Assertions.assertTrue(queue.isClosedAndEmpty());

    // No more items
    Assertions.assertNull(queue.pop());

    // Edge case when closed and empty
    queue.clear();
    queue.close(false);
    Assertions.assertEquals(0, queue.size());
    Assertions.assertTrue(queue.isClosed());
    Assertions.assertTrue(queue.isClosedAndEmpty());
  }

  @Test
  void canCloseAndClear() throws InterruptedException {
    final ConcurrentMonoStack<Integer> queue = new ConcurrentMonoStack<>();
    Assertions.assertEquals(0, queue.size());
    Assertions.assertFalse(queue.isClosed());
    Assertions.assertFalse(queue.isClosedAndEmpty());
    queue.push(1);
    Assertions.assertEquals(1, queue.size());

    queue.close(true);
    Assertions.assertEquals(0, queue.size());
    Assertions.assertTrue(queue.isClosed());
    Assertions.assertTrue(queue.isClosedAndEmpty());

    // No more items
    Assertions.assertNull(queue.pop());
  }

  @Test
  void canPopIfClosed() throws InterruptedException {
    final ConcurrentMonoStack<Integer> queue = new ConcurrentMonoStack<>();
    queue.push(1);
    Assertions.assertEquals(1, queue.size());

    queue.close(false);
    Assertions.assertEquals(1, queue.pop());
    Assertions.assertTrue(queue.isClosed());
    Assertions.assertTrue(queue.isClosedAndEmpty());

    // No more items
    Assertions.assertNull(queue.pop());
  }

  @Test
  void canPollIfClosed() throws InterruptedException {
    final ConcurrentMonoStack<Integer> queue = new ConcurrentMonoStack<>();
    Assertions.assertNull(queue.poll());
    queue.push(1);
    Assertions.assertEquals(1, queue.size());
    Assertions.assertEquals(1, queue.poll());
    Assertions.assertEquals(0, queue.size());

    queue.push(2);
    queue.close(false);
    Assertions.assertEquals(2, queue.poll());
    Assertions.assertTrue(queue.isClosed());
    Assertions.assertTrue(queue.isClosedAndEmpty());

    // No more items
    Assertions.assertNull(queue.poll());
  }

  @Test
  void canPeekIfClosed() throws InterruptedException {
    final ConcurrentMonoStack<Integer> queue = new ConcurrentMonoStack<>();
    queue.push(1);
    Assertions.assertEquals(1, queue.size());

    queue.close(false);
    Assertions.assertEquals(1, queue.peek());
    Assertions.assertTrue(queue.isClosed());
    Assertions.assertFalse(queue.isClosedAndEmpty());
    queue.clear();
    Assertions.assertTrue(queue.isClosedAndEmpty());

    // No more items
    Assertions.assertNull(queue.peek());
  }

  @Test
  void canCloseUnblockWaitingThread()
      throws InterruptedException, ExecutionException, TimeoutException {
    final ConcurrentMonoStack<Integer> queue = new ConcurrentMonoStack<>();
    queue.push(1);

    // Queue is full.
    // Add in a different thread.
    final AtomicBoolean done = new AtomicBoolean();
    final AtomicBoolean added = new AtomicBoolean();
    final CountDownLatch count = new CountDownLatch(1);
    final ExecutorService es = Executors.newSingleThreadExecutor();
    final Future<?> f = es.submit(() -> {
      count.countDown();
      try {
        added.set(queue.push(2));
        done.set(true);
      } catch (final InterruptedException e) {
        throw new RuntimeException(e);
      }
    });

    // Wait for thread to start and it should block
    count.await(10, TimeUnit.SECONDS);
    Assertions.assertFalse(done.get());

    // Close the queue should unblock the thread
    queue.close(false);

    // Wait
    f.get(10, TimeUnit.SECONDS);
    es.shutdown();

    Assertions.assertTrue(done.get());
    Assertions.assertEquals(1, queue.size());
    // Did not add to the closed queue
    Assertions.assertFalse(added.get());
  }

  @Test
  void canCloseAndClearUnblockWaitingThread()
      throws InterruptedException, ExecutionException, TimeoutException {
    final ConcurrentMonoStack<Integer> queue = new ConcurrentMonoStack<>();
    queue.push(1);

    // Queue is full.
    // Add in a different thread.
    final AtomicBoolean done = new AtomicBoolean();
    final AtomicBoolean added = new AtomicBoolean();
    final CountDownLatch count = new CountDownLatch(1);
    final ExecutorService es = Executors.newSingleThreadExecutor();
    final Future<?> f = es.submit(() -> {
      count.countDown();
      try {
        added.set(queue.push(2));
        done.set(true);
      } catch (final InterruptedException e) {
        throw new RuntimeException(e);
      }
    });

    // Wait for thread to start and it should block
    count.await(10, TimeUnit.SECONDS);
    Assertions.assertFalse(done.get());

    // Close the queue should unblock the thread
    queue.close(true);

    // Wait
    f.get(10, TimeUnit.SECONDS);
    es.shutdown();

    Assertions.assertTrue(done.get());
    Assertions.assertEquals(0, queue.size());
    // Did not add to the closed queue
    Assertions.assertFalse(added.get());
  }

  @Test
  void canPutAndTake() throws InterruptedException, ExecutionException, TimeoutException {
    final ConcurrentMonoStack<Integer> queue = new ConcurrentMonoStack<>();

    final ExecutorService es = Executors.newCachedThreadPool();

    final int max = 200;
    final AtomicInteger puts = new AtomicInteger();

    // Add consumers
    final ArrayList<Integer> l1 = new ArrayList<>(max);
    final ArrayList<Integer> l2 = new ArrayList<>(max);
    final ArrayList<Future<?>> consumers = new ArrayList<>(2);
    consumers.add(es.submit(() -> {
      try {
        for (;;) {
          final Integer i = queue.pop();
          if (i == null) {
            break;
          }
          l1.add(i);
        }
      } catch (final InterruptedException e) {
        throw new RuntimeException();
      }
    }));
    consumers.add(es.submit(() -> {
      try {
        for (;;) {
          final Integer i = queue.pop();
          if (i == null) {
            break;
          }
          l2.add(i);
        }
      } catch (final InterruptedException e) {
        throw new RuntimeException();
      }
    }));

    // Add producers
    final ArrayList<Future<?>> producers = new ArrayList<>(2);
    producers.add(es.submit(() -> {
      try {
        for (;;) {
          final int i = puts.incrementAndGet();
          if (i <= max) {
            queue.push(i);
          } else {
            break;
          }
        }
      } catch (final InterruptedException e) {
        throw new RuntimeException();
      }
    }));
    producers.add(es.submit(() -> {
      try {
        for (;;) {
          final int i = puts.incrementAndGet();
          if (i <= max) {
            queue.push(i);
          } else {
            break;
          }
        }
      } catch (final InterruptedException e) {
        throw new RuntimeException();
      }
    }));

    // Wait
    for (final Future<?> f : producers) {
      f.get(10, TimeUnit.SECONDS);
    }
    // Closing the queue will halt consumers.
    queue.close(false);
    for (final Future<?> f : consumers) {
      f.get(10, TimeUnit.SECONDS);
    }

    es.shutdown();

    // Check the queue is empty and the two lists have all the numbers
    Assertions.assertEquals(0, queue.size());

    Assertions.assertEquals(max, l1.size() + l2.size());
    final List<Integer> expected =
        IntStream.rangeClosed(1, max).boxed().collect(Collectors.toList());
    l1.addAll(l2);
    l1.sort(Integer::compare);
    Assertions.assertEquals(expected, l1);
  }

  @Test
  void canInsert() throws InterruptedException {
    final ConcurrentMonoStack<Integer> queue = new ConcurrentMonoStack<>();
    Assertions.assertTrue(queue.insert(1));
    Assertions.assertEquals(1, queue.size());
    Assertions.assertTrue(queue.insert(2));
    Assertions.assertEquals(1, queue.size());
    Assertions.assertEquals(2, queue.pop());
    Assertions.assertEquals(0, queue.size());
    queue.close(false);
    Assertions.assertFalse(queue.insert(3));
    Assertions.assertEquals(0, queue.size());
  }

  @Test
  void canOffer() throws InterruptedException {
    final ConcurrentMonoStack<Integer> queue = new ConcurrentMonoStack<>();
    Assertions.assertTrue(queue.offer(1));
    Assertions.assertEquals(1, queue.size());
    Assertions.assertFalse(queue.offer(2));
    Assertions.assertEquals(1, queue.size());
    Assertions.assertEquals(1, queue.pop());
    Assertions.assertEquals(0, queue.size());
    Assertions.assertTrue(queue.offer(3));
    Assertions.assertEquals(1, queue.size());
    queue.close(false);
    Assertions.assertFalse(queue.offer(4));
    Assertions.assertEquals(1, queue.size());
    Assertions.assertEquals(3, queue.pop());
    Assertions.assertEquals(0, queue.size());
    Assertions.assertFalse(queue.offer(5));
    Assertions.assertEquals(0, queue.size());
  }

  @Test
  void canPeak() {
    final ConcurrentMonoStack<Integer> queue = new ConcurrentMonoStack<>();
    for (int i = 1; i < 3; i++) {
      Assertions.assertTrue(queue.insert(i));
      Assertions.assertEquals(i, queue.peek());
      Assertions.assertEquals(1, queue.size());
    }
  }
}
