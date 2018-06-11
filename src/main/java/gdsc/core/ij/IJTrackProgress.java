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
package gdsc.core.ij;


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
