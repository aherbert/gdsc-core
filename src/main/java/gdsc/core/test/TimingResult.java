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
package gdsc.core.test;


/**
 * Class to store results of running a timing task
 */
public class TimingResult
{
	private final TimingTask task;
	private final long[] times;

	/**
	 * Instantiates a new timing result.
	 *
	 * @param task
	 *            the task
	 * @param times
	 *            the times
	 * @param data
	 *            the data
	 */
	public TimingResult(TimingTask task, long[] times)
	{
		this.task = task;
		this.times = (times == null) ? new long[0] : times;
	}

	/**
	 * @return the task
	 */
	public TimingTask getTask()
	{
		return task;
	}

	/**
	 * @return the times for running the timing task
	 */
	public long[] getTimes()
	{
		return times.clone();
	}

	/**
	 * Gets the number of times
	 *
	 * @return the size
	 */
	public int getSize()
	{
		return times.length;
	}

	/**
	 * Gets the min time.
	 *
	 * @return the min
	 */
	public long getMin()
	{
		if (times.length == 0)
			return 0;
		long min = Long.MAX_VALUE;
		for (int i = times.length; i-- > 0;)
			if (min > times[i])
				min = times[i];
		return min;
	}

	/**
	 * Gets the mean time.
	 *
	 * @return the mean
	 */
	public double getMean()
	{
		if (times.length == 0)
			return 0;
		long sum = 0;
		for (int i = times.length; i-- > 0;)
			sum += times[i];
		return (double) sum / times.length;
	}
}
