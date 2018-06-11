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
