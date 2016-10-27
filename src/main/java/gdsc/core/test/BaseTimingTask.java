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
public abstract class BaseTimingTask implements TimingTask
{
	private String name;

	/**
	 * Instantiates a new base timing task.
	 *
	 * @param name
	 *            the name
	 */
	public BaseTimingTask(String name)
	{
		this.name = name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.core.test.TimingTask#getName()
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * This base class does nothing so that extending classes can override if necessary
	 * 
	 * @see gdsc.core.test.TimingTask#check(int, java.lang.Object)
	 */
	public void check(int i, Object result)
	{

	}
}
