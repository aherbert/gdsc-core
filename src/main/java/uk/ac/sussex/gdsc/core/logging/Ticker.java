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

package uk.ac.sussex.gdsc.core.logging;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Track the progress of processing results in incremental updates to a progress tracker.
 */
public abstract class Ticker {
  /**
   * Increment the current ticks and report the progress. Ideally this should be called after the
   * work has been done as the progress is reported as completed.
   */
  public abstract void tick();

  /**
   * Gets the total amount of ticks.
   *
   * @return the total
   */
  public abstract long getTotal();

  /**
   * Gets the current amount of ticks.
   *
   * @return the position
   */
  public abstract long getCurrent();

  /**
   * Gets the progress as a fraction.
   *
   * @return the progress
   */
  public double getProgress() {
    return (double) getCurrent() / getTotal();
  }

  /**
   * Reset the ticker and report progress as 0.
   */
  public abstract void start();

  /**
   * Report progress as 1. This should be called when ticking has stopped. The current position of
   * the ticker is not changed allowing the ticking to be continued.
   */
  public abstract void stop();

  /**
   * Checks if the {@link #tick()} method is thread safe.
   *
   * @return true, if is thread safe
   */
  public abstract boolean isThreadSafe();

  /**
   * Gets the default instance. This returns a ticker that does nothing.
   *
   * @return the default instance
   */
  public static Ticker getDefaultInstance() {
    return NullTicker.INSTANCE;
  }

  /**
   * Creates a ticker. If the track progress is null, or the total is not positive, then a ticker
   * that does nothing will be returned.
   *
   * <p>The tickers returned will only report progress to the track progress object incrementally.
   * This prevents excessive calls to the track progress object.
   *
   * <p>The default increment is a maximum of 0.5%.
   *
   * @param trackProgress the track progress to report progress to
   * @param total the total amount of ticks
   * @param threadSafe Set to true to create a thread safe ticker
   * @return the ticker
   * @throws IllegalArgumentException If the interval or total are not strictly positive
   */
  public static Ticker create(TrackProgress trackProgress, long total, boolean threadSafe) {
    final long interval = getProgressInterval(total);
    return create(trackProgress, interval, total, threadSafe);
  }

  /**
   * Creates a ticker. If the track progress is null, or the total is not positive, then a ticker
   * that does nothing will be returned.
   *
   * <p>The tickers returned will only report progress to the track progress object incrementally.
   * This prevents excessive calls to the track progress object.
   *
   * @param trackProgress the track progress to report progress to
   * @param interval the interval at which to report progress
   * @param total the total amount of ticks
   * @param threadSafe Set to true to create a thread safe ticker
   * @return the ticker
   * @throws IllegalArgumentException If the interval or total are not strictly positive
   */
  public static Ticker create(TrackProgress trackProgress, long interval, long total,
      boolean threadSafe) {
    if (trackProgress == null || trackProgress instanceof NullTrackProgress || total < 1) {
      return getDefaultInstance();
    }
    if (interval <= 0) {
      throw new IllegalArgumentException("Interval must be strictly positive: " + interval);
    }
    if (total < Integer.MAX_VALUE) {
      return (threadSafe) ? new ConcurrentIntTicker(trackProgress, (int) interval, (int) total)
          : new IntTicker(trackProgress, (int) interval, (int) total);
    }
    return (threadSafe) ? new ConcurrentLongTicker(trackProgress, interval, total)
        : new LongTicker(trackProgress, interval, total);
  }

  /**
   * Creates a starter ticker. If the track progress is null, or the total is not positive, then a
   * ticker that does nothing will be returned.
   *
   * <p>The tickers returned will only report progress to the track progress object incrementally.
   * This prevents excessive calls to the track progress object.
   *
   * <p>The default increment is a maximum of 0.5%.
   *
   * @param trackProgress the track progress to report progress to
   * @param total the total amount of ticks
   * @param threadSafe Set to true to create a thread safe ticker
   * @return the started ticker
   */
  public static Ticker createStarted(TrackProgress trackProgress, long total, boolean threadSafe) {
    final long interval = getProgressInterval(total);
    return createStarted(trackProgress, interval, total, threadSafe);
  }

  /**
   * Creates a starter ticker. If the track progress is null, or the total is not positive, then a
   * ticker that does nothing will be returned.
   *
   * <p>The tickers returned will only report progress to the track progress object incrementally.
   * This prevents excessive calls to the track progress object.
   *
   * @param trackProgress the track progress to report progress to
   * @param interval the interval at which to report progress
   * @param total the total amount of ticks
   * @param threadSafe Set to true to create a thread safe ticker
   * @return the started ticker
   */
  public static Ticker createStarted(TrackProgress trackProgress, long interval, long total,
      boolean threadSafe) {
    final Ticker ticker = create(trackProgress, interval, total, threadSafe);
    ticker.start();
    return ticker;
  }

  /**
   * Gets the progress interval.
   *
   * @param total the total
   * @return the progress interval
   */
  private static long getProgressInterval(long total) {
    // Return a maximum of 0.5% (i.e. 200 / 100)
    return (total > 200L) ? total / 100L : 1L;
  }

  /** A class that ignores all calls to the Ticker interface. */
  private static class NullTicker extends Ticker {
    static final NullTicker INSTANCE = new NullTicker();

    @Override
    public void tick() {
      // Do nothing
    }

    @Override
    public long getTotal() {
      return 0;
    }

    @Override
    public long getCurrent() {
      return 0;
    }

    @Override
    public void start() {
      // Do nothing
    }

    @Override
    public void stop() {
      // Do nothing
    }

    @Override
    public boolean isThreadSafe() {
      return true;
    }
  }

  private abstract static class BaseTicker extends Ticker {
    final TrackProgress trackProgress;

    BaseTicker(TrackProgress trackProgress) {
      this.trackProgress = trackProgress;
    }

    @Override
    public void stop() {
      trackProgress.progress(1);
    }
  }

  private static class IntTicker extends BaseTicker {
    final int total;
    final int interval;
    int current;
    int next;

    IntTicker(TrackProgress trackProgress, int interval, int total) {
      super(trackProgress);
      this.total = total;
      this.interval = interval;
    }

    @Override
    public void tick() {
      final int now = ++current;
      if (now >= next) {
        next = now + interval;
        trackProgress.progress((double) now / total);
      }
    }

    @Override
    public long getTotal() {
      return total;
    }

    @Override
    public long getCurrent() {
      return current;
    }

    @Override
    public void start() {
      current = 0;
      next = interval;
      trackProgress.progress(0.0);
    }

    @Override
    public boolean isThreadSafe() {
      return false;
    }
  }

  private static class ConcurrentIntTicker extends BaseTicker {
    final int total;
    final int interval;
    final AtomicInteger current = new AtomicInteger();
    AtomicInteger next = new AtomicInteger();

    ConcurrentIntTicker(TrackProgress trackProgress, int interval, int total) {
      super(trackProgress);
      this.total = total;
      this.interval = interval;
    }

    @Override
    public void tick() {
      final int now = current.incrementAndGet();
      // This is used as an alternative to
      // next.compareAndSet(now, now + interval)
      // since multiple threads could increment 'current' past 'next'.
      // It also matches the non-threaded implementation.
      if (now >= next.get()) {
        next.set(now + interval);
        trackProgress.progress((double) now / total);
      }
    }

    @Override
    public long getTotal() {
      return total;
    }

    @Override
    public long getCurrent() {
      return current.get();
    }

    @Override
    public void start() {
      current.set(0);
      next.set(interval);
      trackProgress.progress(0.0);
    }

    @Override
    public boolean isThreadSafe() {
      return true;
    }
  }

  private static class LongTicker extends BaseTicker {
    final long total;
    final long interval;
    long current;
    long next;

    LongTicker(TrackProgress trackProgress, long interval, long total) {
      super(trackProgress);
      this.total = total;
      this.interval = interval;
    }

    @Override
    public void tick() {
      final long now = ++current;
      if (now >= next) {
        next = now + interval;
        trackProgress.progress((double) now / total);
      }
    }

    @Override
    public long getTotal() {
      return total;
    }

    @Override
    public long getCurrent() {
      return current;
    }

    @Override
    public void start() {
      current = 0L;
      next = interval;
      trackProgress.progress(0.0);
    }

    @Override
    public boolean isThreadSafe() {
      return false;
    }
  }

  private static class ConcurrentLongTicker extends BaseTicker {
    final long total;
    final long interval;
    final AtomicLong current = new AtomicLong();
    AtomicLong next = new AtomicLong();

    ConcurrentLongTicker(TrackProgress trackProgress, long interval, long total) {
      super(trackProgress);
      this.total = total;
      this.interval = interval;
    }

    @Override
    public void tick() {
      final long now = current.incrementAndGet();
      // This is used as an alternative to
      // next.compareAndSet(now, now + interval)
      // since multiple threads could increment 'current' past 'next'.
      // It also matches the non-threaded implementation.
      if (now >= next.get()) {
        next.set(now + interval);
        trackProgress.progress((double) now / total);
      }
    }

    @Override
    public long getTotal() {
      return total;
    }

    @Override
    public long getCurrent() {
      return current.get();
    }

    @Override
    public void start() {
      current.set(0L);
      next.set(interval);
      trackProgress.progress(0.0);
    }

    @Override
    public boolean isThreadSafe() {
      return true;
    }
  }
}
