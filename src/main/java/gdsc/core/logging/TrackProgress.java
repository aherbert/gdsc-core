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
package gdsc.core.logging;

/**
 * Track the progress of processing results
 */
public interface TrackProgress
{
	/**
	 * Specify progress as a fraction.
	 *
	 * @param fraction
	 *            the fraction
	 */
	public void progress(double fraction);

	/**
	 * Specify progress as the position relative to the total.
	 *
	 * @param position
	 *            the position
	 * @param total
	 *            the total
	 */
	public void progress(long position, long total);

	/**
	 * Specify an increment to the progress as a fraction.
	 *
	 * @param fraction
	 *            the fraction
	 */
	public void incrementProgress(double fraction);

	/**
	 * Logs a message on the progress.
	 *
	 * @param format
	 *            the format
	 * @param args
	 *            the args
	 */
	public void log(String format, Object... args);

	/**
	 * Sets the status on the progress.
	 *
	 * @param format
	 *            the format
	 * @param args
	 *            the args
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
