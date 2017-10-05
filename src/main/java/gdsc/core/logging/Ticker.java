package gdsc.core.logging;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import gdsc.core.ij.Utils;

/*----------------------------------------------------------------------------- 
 * GDSC Software
 * 
 * Copyright (C) 2017 Alex Herbert
 * Genome Damage and Stability Centre
 * University of Sussex, UK
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *---------------------------------------------------------------------------*/

/**
 * Track the progress of processing results in incremental updates to a progress tracker
 */
public abstract class Ticker
{
	/**
	 * Increment the current ticks and report the progress.
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
	public double getProgress()
	{
		return (double) getCurrent() / getTotal();
	}

	/**
	 * Reset the ticker and report progress as 0.
	 */
	public abstract void start();

	/**
	 * Report progress as 1. This should be called when ticking has stopped. The current position of the ticker is not
	 * changed allowing the ticking to be continued.
	 */
	public abstract void stop();

	/**
	 * Creates a ticker. If the track progress is null, or the total is not positive, then a ticker that does nothing
	 * will be returned.
	 * <p>
	 * The tickers returned will only report progress to the track progress object incrementally. This prevents
	 * excessive calls to the track progress object.
	 *
	 * @param trackProgress
	 *            the track progress to report progress to
	 * @param total
	 *            the total amount of ticks
	 * @param threadSafe
	 *            Set to true to create a thread safe ticker
	 * @return the ticker
	 */
	public static Ticker create(TrackProgress trackProgress, long total, boolean threadSafe)
	{
		if (trackProgress == null || trackProgress instanceof NullTrackProgress || total < 1)
			return new NullTicker();
		if (total < Integer.MAX_VALUE)
		{
			return (threadSafe) ? new ConcurrentIntTicker(trackProgress, (int) total)
					: new IntTicker(trackProgress, (int) total);
		}
		return (threadSafe) ? new ConcurrentLongTicker(trackProgress, (int) total)
				: new LongTicker(trackProgress, (int) total);
	}

	private static class NullTicker extends Ticker
	{
		@Override
		public void tick()
		{

		}

		@Override
		public long getTotal()
		{
			return 0;
		}

		@Override
		public long getCurrent()
		{
			return 0;
		}

		@Override
		public void start()
		{

		}

		@Override
		public void stop()
		{

		}
	}

	private static abstract class BaseTicker extends Ticker
	{
		final TrackProgress trackProgress;

		BaseTicker(TrackProgress trackProgress)
		{
			this.trackProgress = trackProgress;
		}

		@Override
		public void stop()
		{
			trackProgress.progress(1);
		}
	}

	private static class IntTicker extends BaseTicker
	{
		final int total;
		final int interval;
		int current = 0;
		int next = 0;

		IntTicker(TrackProgress trackProgress, int total)
		{
			super(trackProgress);
			this.total = total;
			interval = Utils.getProgressInterval(total);
		}

		@Override
		public void tick()
		{
			final int now = ++current;
			if (now > next)
			{
				next = now + interval;
				trackProgress.progress((double) now / total);
			}
		}

		@Override
		public long getTotal()
		{
			return total;
		}

		@Override
		public long getCurrent()
		{
			return current;
		}

		@Override
		public void start()
		{
			current = 0;
			next = 0;
			trackProgress.progress(0.0);
		}
	}

	private static class ConcurrentIntTicker extends BaseTicker
	{
		final int total;
		final int interval;
		final AtomicInteger current = new AtomicInteger();
		int next = 0;

		ConcurrentIntTicker(TrackProgress trackProgress, int total)
		{
			super(trackProgress);
			this.total = total;
			interval = Utils.getProgressInterval(total);
		}

		@Override
		public void tick()
		{
			final int now = current.incrementAndGet();
			if (now > next)
			{
				next = now + interval;
				trackProgress.progress((double) now / total);
			}
		}

		@Override
		public long getTotal()
		{
			return total;
		}

		@Override
		public long getCurrent()
		{
			return current.get();
		}

		@Override
		public void start()
		{
			current.set(0);
			next = 0;
			trackProgress.progress(0.0);
		}
	}

	private static class LongTicker extends BaseTicker
	{
		final long total;
		final long interval;
		long current = 0L;
		long next = 0L;

		LongTicker(TrackProgress trackProgress, long total)
		{
			super(trackProgress);
			this.total = total;
			interval = Utils.getProgressInterval(total);
		}

		@Override
		public void tick()
		{
			final long now = ++current;
			if (now > next)
			{
				next = now + interval;
				trackProgress.progress((double) now / total);
			}
		}

		@Override
		public long getTotal()
		{
			return total;
		}

		@Override
		public long getCurrent()
		{
			return current;
		}

		@Override
		public void start()
		{
			current = 0L;
			next = 0L;
			trackProgress.progress(0.0);
		}
	}

	private static class ConcurrentLongTicker extends BaseTicker
	{
		final long total;
		final long interval;
		final AtomicLong current = new AtomicLong();
		long next = 0L;

		ConcurrentLongTicker(TrackProgress trackProgress, long total)
		{
			super(trackProgress);
			this.total = total;
			interval = Utils.getProgressInterval(total);
		}

		@Override
		public void tick()
		{
			final long now = current.incrementAndGet();
			if (now > next)
			{
				next = now + interval;
				trackProgress.progress((double) now / total);
			}
		}

		@Override
		public long getTotal()
		{
			return total;
		}

		@Override
		public long getCurrent()
		{
			return current.get();
		}

		@Override
		public void start()
		{
			current.set(0L);
			next = 0L;
			trackProgress.progress(0.0);
		}
	}
}
