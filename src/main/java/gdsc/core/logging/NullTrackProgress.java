package gdsc.core.logging;

/*----------------------------------------------------------------------------- 
 * GDSC Software
 * 
 * Copyright (C) 2013 Alex Herbert
 * Genome Damage and Stability Centre
 * University of Sussex, UK
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *---------------------------------------------------------------------------*/

/**
 * Ignore all method calls from the {@link TrackProgress} interface
 */
public class NullTrackProgress implements TrackProgress
{
	/** An instance to ignore progress reporting */
	public static final NullTrackProgress INSTANCE = new NullTrackProgress();

	/**
	 * Creates an instance if the argument is null, else return the argument.
	 *
	 * @param trackProgress
	 *            the track progress (may be null)
	 * @return the track progress (not null)
	 */
	public static TrackProgress createIfNull(TrackProgress trackProgress)
	{
		return (trackProgress == null) ? INSTANCE : trackProgress;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.utils.fitting.results.TrackProgress#progress(double)
	 */
	public void progress(double fraction)
	{
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.utils.fitting.results.TrackProgress#progress(long, long)
	 */
	public void progress(long position, long total)
	{
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.core.logging.TrackProgress#incrementProgress(double)
	 */
	public void incrementProgress(double fraction)
	{
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.core.logging.TrackProgress#log(java.lang.String, java.lang.Object[])
	 */
	public void log(String format, Object... args)
	{
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.core.logging.TrackProgress#status(java.lang.String, java.lang.Object[])
	 */
	public void status(String format, Object... args)
	{
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.core.logging.TrackProgress#isEnded()
	 */
	public boolean isEnded()
	{
		return false;
	}
}
