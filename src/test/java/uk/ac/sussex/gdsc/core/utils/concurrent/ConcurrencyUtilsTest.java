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

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
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
  public void canWaitForCompletion() throws InterruptedException, ExecutionException {
    final ExecutorService es = Executors.newSingleThreadExecutor();
    final AtomicInteger count = new AtomicInteger();
    final ArrayList<Future<?>> futures = new ArrayList<>();
    final int loops = 3;
    for (int i = 0; i < loops; i++) {
      futures.add(es.submit(() -> count.getAndIncrement()));
    }
    ConcurrencyUtils.waitForCompletion(futures);
    Assertions.assertEquals(loops, count.get());
  }

  @Test
  public void canWaitForCompletionUnchecked() {
    final ExecutorService es = Executors.newSingleThreadExecutor();
    final AtomicInteger count = new AtomicInteger();
    final ArrayList<Future<?>> futures = new ArrayList<>();
    final int loops = 3;
    for (int i = 0; i < loops; i++) {
      futures.add(es.submit(() -> count.getAndIncrement()));
    }
    ConcurrencyUtils.waitForCompletionUnchecked(futures);
    Assertions.assertEquals(loops, count.get());
  }

  @Test
  public void canWaitForCompletionUncheckedWithInterruptedException() throws InterruptedException {
    // Set-up jobs
    final ExecutorService es = Executors.newSingleThreadExecutor();
    final ArrayList<Future<?>> futures = new ArrayList<>();
    futures.add(es.submit(() -> {
      try {
        // 10 seconds
        Thread.sleep(10000);
      } catch (final InterruptedException e) {
        // Ignore
      }
    }));
    // Do all work in a thread which will be interrupted while waiting
    final ConcurrentRuntimeException[] ex = new ConcurrentRuntimeException[1];
    final Thread t = new Thread(() -> {
      ex[0] = Assertions.assertThrows(ConcurrentRuntimeException.class, () -> {
        // This should wait then be interrupted
        ConcurrencyUtils.waitForCompletionUnchecked(futures);
      });
    });
    t.start();

    // Start a second thread to interrupt the first one
    new Thread(() -> t.interrupt()).start();
    // Wait. This should be interrupted.
    synchronized (t) {
      // 5 seconds
      t.wait(5000);
    }
    Assertions.assertTrue(ex[0].getCause() instanceof InterruptedException);
  }

  @Test
  public void canWaitForCompletionUncheckedWithRuntimeException() {
    final RuntimeException error = new RuntimeException();
    // Set-up jobs
    final ExecutorService es = Executors.newSingleThreadExecutor();
    final ArrayList<Future<?>> futures = new ArrayList<>();
    futures.add(es.submit(() -> {
      throw error;
    }));
    final RuntimeException ex = Assertions.assertThrows(RuntimeException.class,
        () -> ConcurrencyUtils.waitForCompletionUnchecked(futures));
    Assertions.assertSame(error, ex);
  }

  @Test
  public void canWaitForCompletionUncheckedWithConcurrentRuntimeException() {
    final IOException error = new IOException();
    // Set-up jobs
    final ExecutorService es = Executors.newSingleThreadExecutor();
    final ArrayList<Future<?>> futures = new ArrayList<>();
    futures.add(es.submit(() -> {
      throw error;
    }));
    final ConcurrentRuntimeException ex = Assertions.assertThrows(ConcurrentRuntimeException.class,
        () -> ConcurrencyUtils.waitForCompletionUnchecked(futures));
    Assertions.assertSame(error, ex.getCause());
  }

  @Test
  public void canWaitForCompletionUncheckedWithError() {
    final Error error = new Error();
    // Set-up jobs
    final ExecutorService es = Executors.newSingleThreadExecutor();
    final ArrayList<Future<?>> futures = new ArrayList<>();
    futures.add(es.submit(() -> {
      throw error;
    }));
    final Error ex = Assertions.assertThrows(Error.class,
        () -> ConcurrencyUtils.waitForCompletionUnchecked(futures));
    Assertions.assertSame(error, ex);
  }

  @Test
  public void canWaitForCompletionUncheckedWithErrorAndHandler() {
    final Error error = new Error();
    // Set-up jobs
    final ExecutorService es = Executors.newSingleThreadExecutor();
    final ArrayList<Future<?>> futures = new ArrayList<>();
    futures.add(es.submit(() -> {
      throw error;
    }));
    final AtomicInteger count = new AtomicInteger();
    final Error ex = Assertions.assertThrows(Error.class,
        () -> ConcurrencyUtils.waitForCompletionUnchecked(futures, e -> {
          count.getAndIncrement();
          Assertions.assertTrue(e instanceof ExecutionException);
          Assertions.assertSame(error, e.getCause());
        }));
    Assertions.assertSame(error, ex);
    Assertions.assertEquals(1, count.get());
  }

  @Test
  public void canWaitForCompletionT() throws InterruptedException, ExecutionException {
    final ExecutorService es = Executors.newSingleThreadExecutor();
    final AtomicInteger count = new AtomicInteger();
    final ArrayList<Future<Integer>> futures = new ArrayList<>();
    final int loops = 3;
    for (int i = 0; i < loops; i++) {
      futures.add(es.submit(() -> count.getAndIncrement()));
    }
    ConcurrencyUtils.waitForCompletionT(futures);
    Assertions.assertEquals(loops, count.get());
  }

  @Test
  public void canWaitForCompletionTUnchecked() {
    final ExecutorService es = Executors.newSingleThreadExecutor();
    final AtomicInteger count = new AtomicInteger();
    final ArrayList<Future<Integer>> futures = new ArrayList<>();
    final int loops = 3;
    for (int i = 0; i < loops; i++) {
      futures.add(es.submit(() -> count.getAndIncrement()));
    }
    ConcurrencyUtils.waitForCompletionUncheckedT(futures);
    Assertions.assertEquals(loops, count.get());
  }

  @Test
  public void canWaitForCompletionTUncheckedWithInterruptedException() throws InterruptedException {
    // Set-up jobs
    final ExecutorService es = Executors.newSingleThreadExecutor();
    final ArrayList<Future<Integer>> futures = new ArrayList<>();
    futures.add(es.submit(() -> {
      try {
        // 10 seconds
        Thread.sleep(10000);
      } catch (final InterruptedException e) {
        // Ignore
      }
      return 1;
    }));
    // Do all work in a thread which will be interrupted while waiting
    final ConcurrentRuntimeException[] ex = new ConcurrentRuntimeException[1];
    final Thread t = new Thread(() -> {
      ex[0] = Assertions.assertThrows(ConcurrentRuntimeException.class, () -> {
        // This should wait then be interrupted
        ConcurrencyUtils.waitForCompletionUncheckedT(futures);
      });
    });
    t.start();

    // Start a second thread to interrupt the first one
    new Thread(() -> t.interrupt()).start();
    // Wait. This should be interrupted.
    synchronized (t) {
      // 5 seconds
      t.wait(5000);
    }
    Assertions.assertTrue(ex[0].getCause() instanceof InterruptedException);
  }

  @Test
  public void canWaitForCompletionTUncheckedWithRuntimeException() {
    final RuntimeException error = new RuntimeException();
    // Set-up jobs
    final ExecutorService es = Executors.newSingleThreadExecutor();
    final ArrayList<Future<Integer>> futures = new ArrayList<>();
    futures.add(es.submit(() -> {
      throw error;
    }));
    final RuntimeException ex = Assertions.assertThrows(RuntimeException.class,
        () -> ConcurrencyUtils.waitForCompletionUncheckedT(futures));
    Assertions.assertSame(error, ex);
  }

  @Test
  public void canWaitForCompletionTUncheckedWithConcurrentRuntimeException() {
    final IOException error = new IOException();
    // Set-up jobs
    final ExecutorService es = Executors.newSingleThreadExecutor();
    final ArrayList<Future<Integer>> futures = new ArrayList<>();
    futures.add(es.submit(() -> {
      throw error;
    }));
    final ConcurrentRuntimeException ex = Assertions.assertThrows(ConcurrentRuntimeException.class,
        () -> ConcurrencyUtils.waitForCompletionUncheckedT(futures));
    Assertions.assertSame(error, ex.getCause());
  }

  @Test
  public void canWaitForCompletionTUncheckedWithError() {
    final Error error = new Error();
    // Set-up jobs
    final ExecutorService es = Executors.newSingleThreadExecutor();
    final ArrayList<Future<Integer>> futures = new ArrayList<>();
    futures.add(es.submit(() -> {
      throw error;
    }));
    final Error ex = Assertions.assertThrows(Error.class,
        () -> ConcurrencyUtils.waitForCompletionUncheckedT(futures));
    Assertions.assertSame(error, ex);
  }

  @Test
  public void canWaitForCompletionTUncheckedWithErrorAndHandler() {
    final Error error = new Error();
    // Set-up jobs
    final ExecutorService es = Executors.newSingleThreadExecutor();
    final ArrayList<Future<Integer>> futures = new ArrayList<>();
    futures.add(es.submit(() -> {
      throw error;
    }));
    final AtomicInteger count = new AtomicInteger();
    final Error ex = Assertions.assertThrows(Error.class,
        () -> ConcurrencyUtils.waitForCompletionUncheckedT(futures, e -> {
          count.getAndIncrement();
          Assertions.assertTrue(e instanceof ExecutionException);
          Assertions.assertSame(error, e.getCause());
        }));
    Assertions.assertSame(error, ex);
    Assertions.assertEquals(1, count.get());
  }

  @Test
  public void canInvokeAll() {
    final ExecutorService es = Executors.newSingleThreadExecutor();
    final AtomicInteger count = new AtomicInteger();
    final ArrayList<Callable<Integer>> tasks = new ArrayList<>();
    final int loops = 3;
    for (int i = 0; i < loops; i++) {
      tasks.add(() -> count.getAndIncrement());
    }
    ConcurrencyUtils.invokeAllUnchecked(es, tasks);
    Assertions.assertEquals(loops, count.get());
  }

  @Test
  public void canInvokeAllWithInterruptedException() throws InterruptedException {
    final ExecutorService es = Executors.newSingleThreadExecutor();
    final AtomicInteger count = new AtomicInteger();
    final ArrayList<Callable<Integer>> tasks = new ArrayList<>();
    tasks.add(() -> {
      try {
        // 10 seconds
        Thread.sleep(10000);
      } catch (final InterruptedException e) {
        // Ignore
      }
      return count.getAndIncrement();
    });
    // Do all work in a thread which will be interrupted while waiting
    final ConcurrentRuntimeException[] ex = new ConcurrentRuntimeException[1];
    final Thread t = new Thread(() -> {
      ex[0] = Assertions.assertThrows(ConcurrentRuntimeException.class, () -> {
        // This should wait then be interrupted
        ConcurrencyUtils.invokeAllUnchecked(es, tasks);
      });
    });
    t.start();

    // Start a second thread to interrupt the first one
    new Thread(() -> t.interrupt()).start();
    // Wait. This should be interrupted.
    synchronized (t) {
      // 5 seconds
      t.wait(5000);
    }
    Assertions.assertTrue(ex[0].getCause() instanceof InterruptedException);
  }

  @Test
  public void canRefresh() {
    final AtomicReference<Integer> reference = new AtomicReference<>();
    final Predicate<Integer> test = v -> v.equals(1);
    final Supplier<Integer> supplier = () -> Integer.valueOf(1);

    Integer value = ConcurrencyUtils.refresh(reference, test, supplier);
    Assertions.assertEquals(Integer.valueOf(1), value, "Did not generate the value when null");

    final Integer refreshedValue = ConcurrencyUtils.refresh(reference, test, supplier);
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
