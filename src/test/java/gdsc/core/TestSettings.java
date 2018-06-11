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
package gdsc.core;

public class TestSettings
{
	/**
	 * Set this to true to run all the speed tests. Tests will run and output the speed
	 * difference between two methods. Set to false to ignore tests.
	 */
	public static final boolean RUN_SPEED_TESTS = false;
	
	/**
	 * Set this to true assert the speed tests, i.e. check if something is faster than another.
	 * Note that in some cases the speed difference between two methods is very close and so the test 
	 * can fail a fraction of the time it is run. Leaving at false will ensure the package can be built. 
	 */
	public static final boolean ASSERT_SPEED_TESTS = false;
}
