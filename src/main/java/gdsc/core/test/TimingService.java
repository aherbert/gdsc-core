package gdsc.core.test;

import java.io.PrintStream;
import java.util.ArrayList;

import gdsc.core.utils.Maths;

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

/**
 * Class to run timing tasks.
 */
public class TimingService
{
	private int runs;

	private ArrayList<TimingResult> results = new ArrayList<TimingResult>();

	/**
	 * @return The number of timing results
	 */
	public int getSize()
	{
		return results.size();
	}
	
	/**
	 * Clear the results.
	 */
	public void clearResults()
	{
		results.clear();
	}

	/**
	 * Get the timing result
	 * 
	 * @param index
	 *            The index
	 * @return the timing result
	 */
	public TimingResult get(int index)
	{
		return results.get(index);
	}

	/**
	 * Instantiates a new timing service.
	 */
	public TimingService()
	{
		this(5);
	}

	/**
	 * Instantiates a new timing service.
	 *
	 * @param runs
	 *            the number of timing runs
	 */
	public TimingService(int runs)
	{
		setRuns(runs);
	}

	/**
	 * Sets the runs.
	 *
	 * @param runs
	 *            the number of timing runs
	 */
	public void setRuns(int runs)
	{
		this.runs = runs;
	}

	/**
	 * Gets the number of timing runs.
	 *
	 * @return the runs
	 */
	public int getRuns()
	{
		return runs;
	}

	/**
	 * Execute the timing task.
	 *
	 * @param task
	 *            the task
	 * @param check
	 *            Set to true to validate result with the check method
	 * @return the timing result
	 */
	public TimingResult execute(TimingTask task)
	{
		return execute(task, false);
	}

	/**
	 * Execute the timing task.
	 *
	 * @param task
	 *            the task
	 * @param check
	 *            Set to true to validate result with the check method
	 * @return the timing result
	 */
	public TimingResult execute(TimingTask task, boolean check)
	{
		final int size = task.getSize();
		final long[] times = new long[runs];

		// First run store the result
		int run = 0;
		Object[] result = new Object[size];
		for (; run < runs; run++)
		{
			final Object[] data = new Object[size];
			for (int i = 0; i < size; i++)
			{
				data[i] = task.getData(i);
			}
			final long start = System.nanoTime();
			for (int i = 0; i < size; i++)
				result[i] = task.run(data[i]);
			times[run] = System.nanoTime() - start;
		}
		// Remaining runs
		for (; run < runs; run++)
		{
			final Object[] data = new Object[size];
			for (int i = 0; i < size; i++)
			{
				data[i] = task.getData(i);
			}
			final long start = System.nanoTime();
			for (int i = 0; i < size; i++)
				task.run(data[i]);
			times[run] = System.nanoTime() - start;
		}
		if (check)
		{
			for (int i = 0; i < size; i++)
				task.check(i, result[i]);
		}
		TimingResult r = new TimingResult(task, times);
		results.add(r);
		return r;
	}

	/**
	 * Report the timimg results to the standard output.
	 */
	public void report()
	{
		report(System.out);
	}

	/**
	 * Report the timimg results to the output.
	 *
	 * @param out
	 *            the output
	 */
	public void report(PrintStream out)
	{
		report(out, results.toArray(new TimingResult[results.size()]));
	}

	/**
	 * Report the timimg results to the output.
	 *
	 * @param out
	 *            the output
	 * @param results
	 *            the results
	 */
	public static void report(PrintStream out, TimingResult... results)
	{
		if (results == null || results.length == 0)
			return;

		double[] avs = new double[results.length];
		long[] mins = new long[results.length];

		int width = 0;
		for (int i = 0; i < results.length; i++)
		{
			int l = results[i].getTask().getName().length();
			if (width < l)
				width = l;

			mins[i] = results[i].getMin();
			avs[i] = results[i].getMean();
		}
		String format = String.format("%%-%ds : %%15d (%%8.3f)%%c: %%15f (%%8.3f)%%c\n", width);

		// Find the fastest
		long min = Maths.min(mins);
		double av = Maths.min(avs);

		for (int i = 0; i < results.length; i++)
		{
			// Results relative to the first result
			// Mark the fastest
			char mc = (mins[i] == min) ? '*' : ' ';
			char ac = (avs[i] == av) ? '*' : ' ';
			out.printf(format, results[i].getTask().getName(), mins[i], (double) mins[i] / mins[0], mc, avs[i],
					avs[i] / avs[0], ac);
		}
	}

	/**
	 * Repeat all the tasks.
	 *
	 * @return the number repeated
	 */
	public int repeat()
	{
		return repeat(results.size());
	}

	/**
	 * Repeat all the tasks up to the given size.
	 *
	 * @param size
	 *            the size
	 * @return the number repeated
	 */
	public int repeat(int size)
	{
		for (int i = 0; i < size; i++)
			execute(results.get(i).getTask());
		return size;
	}

	/**
	 * Run each task and call the check method
	 */
	public void check()
	{
		int size = results.size();
		for (int i = 0; i < size; i++)
		{
			check(results.get(i).getTask());
		}
	}

	/**
	 * Run the task and call the check method on the results
	 *
	 * @param task
	 *            the task
	 */
	public static void check(TimingTask task)
	{
		final int size = task.getSize();
		for (int i = 0; i < size; i++)
		{
			Object result = task.run(task.getData(i));
			task.check(i, result);
		}
	}
}
