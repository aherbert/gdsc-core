package gdsc.core.test;

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
 * Defines a task to run
 */
public interface TimingTask
{
	/**
	 * Gets the number of tasks.
	 *
	 * @return the number of tasks
	 */
	int getSize();

	/**
	 * Gets the task data for given task.
	 * <p>
	 * If the data is destructively modified by the {@link #run(Object)} method then this should be new data.
	 *
	 * @param i
	 *            the task index
	 * @return the data
	 */
	Object getData(int i);

	/**
	 * Run the task.
	 *
	 * @param data
	 *            the task data
	 * @return the result
	 */
	Object run(Object data);

	/**
	 * The task name.
	 *
	 * @return the name
	 */
	String getName();

	/**
	 * Check the result produced by the given task.
	 * <p>
	 * It is left to the implementation to decide how to handle incorrect results, e.g. throw an exception, log an
	 * error, etc.
	 *
	 * @param i
	 *            the task index
	 * @param result
	 *            the result
	 */
	void check(int i, Object result);
}
