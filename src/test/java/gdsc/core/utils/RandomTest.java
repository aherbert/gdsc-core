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
package gdsc.core.utils;

import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings({"javadoc"})
public class RandomTest
{
	@Test
	public void canComputeSample()
	{
		int[] set = new int[] { 0, 1, 2, 5, 8, 9, 10 };
		Random r = new Random(30051977);
		for (int n : set)
		{
			for (int k : set)
			{
				canComputeSample(r, k, n);
			}
		}
	}

	@Test
	public void canComputeSampleFromBigData()
	{
		Random r = new Random(30051977);
		int n = 100;
		for (int k : new int[] { 0, 1, 2, n / 2, n - 2, n - 1, n })
		{
			canComputeSample(r, k, n);
		}
	}

	private void canComputeSample(Random r, int k, int n)
	{
		int[] sample = r.sample(k, n);
		//TestSettings.debug("%d from %d = %s\n", k, n, java.util.Arrays.toString(sample));
		Assert.assertEquals(Math.min(k, n), sample.length);
		for (int i = 0; i < sample.length; i++)
			for (int j = i + 1; j < sample.length; j++)
				Assert.assertNotEquals(sample[i], sample[j]);
	}
}
