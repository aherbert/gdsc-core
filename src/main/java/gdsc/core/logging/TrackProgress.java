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
 * Track the progress of processing results
 */
public interface TrackProgress
{
	/**
	 * Specify progress as a fraction
	 * 
	 * @param fraction
	 */
	public void progress(double fraction);

	/**
	 * Specify progress as the position relative to the total
	 * 
	 * @param position
	 * @param total
	 */
	public void progress(long position, long total);

	/**
	 * Specify an increment to the progress as a fraction
	 * 
	 * @param fraction
	 */
	public void incrementProgress(double fraction);

	/**
	 * Logs a message on the progress
	 * 
	 * @param format
	 * @param args
	 */
	public void log(String format, Object... args);

	/**
	 * Sets the status on the progress
	 * 
	 * @param format
	 * @param args
	 */
	public void status(String format, Object... args);

	/**
	 * Return true if the tracker is ended and the processing of results should stop.
	 * <p>
	 * This method can be checked by long running algorithms allowing them to be interrupted.
	 * 
	 * @return True if ended
	 */
	public boolean isEnded();

	/**
	 * Checks if the progress methods are active.
	 *
	 * @return true, if progress methods are active.
	 */
	public boolean isProgress();

	/**
	 * Checks if the {@link #log(String, Object...)}. method is active
	 *
	 * @return true, if the {@link #log(String, Object...)}. method is active
	 */
	public boolean isLog();

	/**
	 * Checks if the {@link #status(String, Object...)} method is active.
	 *
	 * @return true, if is {@link #status(String, Object...)} method is active.
	 */
	public boolean isStatus();
}
