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

import org.junit.Test;

import gdsc.core.test.TestSettings.LogLevel;

public class TestSettingsTest
{
	@Test
	public void canAssertEqualsDoubleArraysUsingRelativeError()
	{
		double[] e = new double[] { Math.PI };
		for (double error : new double[] { 0, 1e-8, 1e-6 })
		{
			double[] o = e.clone();
			if (error == 0)
			{
				TestSettings.assertArrayEquals(e, o, 0);
				continue;
			}

			for (int i = 0; i < o.length; i++)
				o[i] = Math.nextUp(e[i] - e[i] * error);
			TestSettings.assertArrayEquals(e, o, error);

			for (int i = 0; i < o.length; i++)
				o[i] = Math.nextDown(e[i] + e[i] * error);
			TestSettings.assertArrayEquals(e, o, error);
		}
	}

	@Test(expected = AssertionError.class)
	public void canAssertNotEqualsDownDoubleArraysUsingRelativeError()
	{
		double[] e = new double[] { Math.PI };
		double error = 1e-8;
		double[] o = e.clone();

		for (int i = 0; i < o.length; i++)
			o[i] = Math.nextDown(e[i] - e[i] * error);
		TestSettings.assertArrayEquals(e, o, error);
	}

	@Test(expected = AssertionError.class)
	public void canAssertNotEqualsUpDoubleArraysUsingRelativeError()
	{
		double[] e = new double[] { Math.PI };
		double error = 1e-8;
		double[] o = e.clone();

		for (int i = 0; i < o.length; i++)
			o[i] = Math.nextUp(e[i] + e[i] * error);
		TestSettings.assertArrayEquals(e, o, error);
	}

	@Test
	public void canAssertEqualsFloatArraysUsingRelativeError()
	{
		float[] e = new float[] { (float) Math.PI };
		for (double error : new double[] { 0, 1e-8, 1e-6 })
		{
			float[] o = e.clone();
			if (error == 0)
			{
				TestSettings.assertArrayEquals(e, o, 0);
				continue;
			}

			for (int i = 0; i < o.length; i++)
				o[i] = (float) Math.nextUp(e[i] - e[i] * error);
			TestSettings.assertArrayEquals(e, o, error);

			for (int i = 0; i < o.length; i++)
				o[i] = (float) Math.nextDown(e[i] + e[i] * error);
			TestSettings.assertArrayEquals(e, o, error);
		}
	}

	@Test(expected = AssertionError.class)
	public void canAssertNotEqualsDownFloatArraysUsingRelativeError()
	{
		float[] e = new float[] { (float) Math.PI };
		double error = 1e-8;
		float[] o = e.clone();

		for (int i = 0; i < o.length; i++)
			o[i] = Math.nextDown((float) (e[i] - e[i] * error));
		TestSettings.assertArrayEquals(e, o, error);
	}

	@Test(expected = AssertionError.class)
	public void canAssertNotEqualsUpFloatArraysUsingRelativeError()
	{
		float[] e = new float[] { (float) Math.PI };
		double error = 1e-8;
		float[] o = e.clone();

		for (int i = 0; i < o.length; i++)
			o[i] = Math.nextUp((float) (e[i] + e[i] * error));
		TestSettings.assertArrayEquals(e, o, error);
	}
	
	@Test
	public void printSettings()
	{
		TestSettings.assume(LogLevel.WARN);
		TestSettings.warn("TestSettings Log Level = %d\n", TestSettings.getLogLevel());
		TestSettings.warn("TestSettings Test Complexity = %d\n", TestSettings.getTestComplexity());
		TestSettings.warn("TestSettings Seed = %d\n", TestSettings.getSeed());
	}
}
