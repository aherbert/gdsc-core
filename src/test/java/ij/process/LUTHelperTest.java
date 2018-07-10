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
package ij.process;

import org.junit.Assert;
import org.junit.Test;

import ij.process.LUTHelper.DefaultLUTMapper;
import ij.process.LUTHelper.LUTMapper;
import ij.process.LUTHelper.NonZeroLUTMapper;

@SuppressWarnings({ "javadoc" })
public class LUTHelperTest
{
	@Test
	public void canMapTo0to255()
	{
		mapTo0to255(0, 0);
		mapTo0to255(0, 1);
		mapTo0to255(0, 255);
		mapTo0to255(0, 1000);

		mapTo0to255(4.3f, 32.5f);
		mapTo0to255(-4.3f, 0f);
		mapTo0to255(-4.3f, 32.5f);
		mapTo0to255(0f, 32.5f);
	}

	@Test
	public void canMapTo1to255()
	{
		mapTo1to255(1, 1);
		mapTo1to255(1, 2);
		mapTo1to255(1, 255);
		mapTo1to255(1, 1000);

		mapTo1to255(4.3f, 32.5f);
		mapTo1to255(-4.3f, 0f);
		mapTo1to255(-4.3f, 32.5f);
		mapTo1to255(0f, 32.5f);
	}

	private static void mapTo0to255(float min, float max)
	{
		final LUTMapper map = new DefaultLUTMapper(min, max);
		Assert.assertEquals(0, map.map(min));
		if (max != min)
			Assert.assertEquals(255, map.map(max));
	}

	private static void mapTo1to255(float min, float max)
	{
		final LUTMapper map = new NonZeroLUTMapper(min, max);
		Assert.assertEquals(1, map.map(min));
		if (max != min)
			Assert.assertEquals(255, map.map(max));
	}
}
