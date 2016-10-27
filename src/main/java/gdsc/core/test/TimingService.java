package gdsc.core.test;

import java.io.PrintStream;
import java.util.ArrayList;

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
	 * @return the timing result
	 */
	public TimingResult execute(TimingTask task)
	{
		final int size = task.getSize();
		final long[] times = new long[runs];
		for (int run = 0; run < runs; run++)
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

		// For relative results
		final long min = results[0].getMin();
		final double av = results[0].getMean();

		int width = 0;
		for (int i = 0; i < results.length; i++)
		{
			int l = results[i].getTask().getName().length();
			if (width < l)
				width = l;
		}
		String format = String.format("%%-%ds : %%15d (%%8.3f) : %%15f (%%8.3f)\n", width);

		for (int i = 0; i < results.length; i++)
		{
			long min2 = (i == 0) ? min : results[i].getMin();
			double av2 = (i == 0) ? av : results[i].getMean();
			out.printf(format, results[i].getTask().getName(), min2, (double) min2 / min, av2, av2 / av);
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
