package gdsc.core.clustering.optics;

/*----------------------------------------------------------------------------- 
 * GDSC ImageJ Software
 * 
 * Copyright (C) 2017 Alex Herbert
 * Genome Damage and Stability Centre
 * University of Sussex, UK
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *---------------------------------------------------------------------------*/

/**
 * The sample mode to sample neighbours in FastOPTICS
 */
public enum SampleMode
{
	//@formatter:on
	/** Randomly sample a maximum of two neighbours from each set */
	RANDOM
	{
		@Override
		public String getName()
		{
			return "Random";
		}
	},
	/** The median of the project set is the neighbour of all points in the set */
	MEDIAN
	{
		@Override
		public String getName()
		{
			return "Median";
		}
	},
	/** Sample all-vs-all from each set */
	ALL
	{
		@Override
		public String getName()
		{
			return "All";
		}
	};
	//@formatter:off
	
	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	abstract public String getName();

	@Override
	public String toString()
	{
		return getName();
	}	
	
	public static SampleMode get(int ordinal)
	{
		if (ordinal < 0|| ordinal >= values().length)
			ordinal = 0;
		return values()[ordinal];
	}
}