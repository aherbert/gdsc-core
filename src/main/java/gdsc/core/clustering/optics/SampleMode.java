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
package gdsc.core.clustering.optics;

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

	/**
	 * Gets the sample mode.
	 *
	 * @param ordinal the ordinal
	 * @return the sample mode
	 */
	public static SampleMode get(int ordinal)
	{
		if (ordinal < 0|| ordinal >= values().length)
			ordinal = 0;
		return values()[ordinal];
	}
}
