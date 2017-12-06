package gdsc.core.ij;

/*----------------------------------------------------------------------------- 
 * GDSC Plugins for ImageJ
 * 
 * Copyright (C) 2016 Alex Herbert
 * Genome Damage and Stability Centre
 * University of Sussex, UK
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *---------------------------------------------------------------------------*/

import gdsc.core.logging.TrackProgress;
import ij.IJ;

/**
 * Report the progress of processing results
 */
public class IJTrackProgress implements TrackProgress
{
	double done = 0;

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.utils.fitting.results.TrackProgress#progress(double)
	 */
	public void progress(double fraction)
	{
		if (fraction == 0)
			done = 0;
		IJ.showProgress(fraction);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.utils.fitting.results.TrackProgress#progress(long, long)
	 */
	public void progress(long position, long total)
	{
		if (position == 0)
			done = 0;
		IJ.showProgress((double) position / total);
	}

	/**
	 * This is not thread safe. The total work done is accumulated and can be reset by passing zero progress to the
	 * progress methods.
	 * 
	 * @see gdsc.core.logging.TrackProgress#incrementProgress(double)
	 */
	public void incrementProgress(double fraction)
	{
		done += fraction;
		IJ.showProgress(done);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.core.logging.TrackProgress#log(java.lang.String, java.lang.Object[])
	 */
	public void log(String format, Object... args)
	{
		IJ.log(String.format(format, args));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.core.logging.TrackProgress#status(java.lang.String, java.lang.Object[])
	 */
	public void status(String format, Object... args)
	{
		IJ.showStatus(String.format(format, args));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.core.logging.TrackProgress#isEnded()
	 */
	public boolean isEnded()
	{
		return Utils.isInterrupted();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.core.logging.TrackProgress#isProgress()
	 */
	public boolean isProgress()
	{
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.core.logging.TrackProgress#isLogging()
	 */
	public boolean isLog()
	{
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.core.logging.TrackProgress#isStatus()
	 */
	public boolean isStatus()
	{
		return true;
	}
}
