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
package uk.ac.sussex.gdsc.core.utils;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.util.MathArrays;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import uk.ac.sussex.gdsc.test.TestSettings;

@SuppressWarnings({ "javadoc" })
public class StatisticsTest
{
	@Test
	public void canComputeStatistics()
	{
		final RandomGenerator r = TestSettings.getRandomGenerator();
		DescriptiveStatistics e;
		Statistics o;
		for (int i = 0; i < 10; i++)
		{
			e = new DescriptiveStatistics();
			o = new Statistics();
			for (int j = 0; j < 100; j++)
			{
				final double d = r.nextDouble();
				e.addValue(d);
				o.add(d);
				check(e, o);
			}
		}

		e = new DescriptiveStatistics();
		o = new Statistics();
		final int[] idata = SimpleArrayUtils.newArray(100, 0, 1);
		MathArrays.shuffle(idata, r);
		for (final double v : idata)
			e.addValue(v);
		o.add(idata);
		check(e, o);

		e = new DescriptiveStatistics();
		o = new Statistics();
		final double[] ddata = new double[idata.length];
		for (int i = 0; i < idata.length; i++)
		{
			ddata[i] = idata[i];
			e.addValue(ddata[i]);
		}
		o.add(ddata);
		check(e, o);

		e = new DescriptiveStatistics();
		o = new Statistics();
		final float[] fdata = new float[idata.length];
		for (int i = 0; i < idata.length; i++)
		{
			fdata[i] = idata[i];
			e.addValue(fdata[i]);
		}
		o.add(fdata);
		check(e, o);
	}

	private static void check(DescriptiveStatistics e, Statistics o)
	{
		Assertions.assertEquals(e.getN(), o.getN(), "N");
		Assertions.assertEquals(e.getMean(), o.getMean(), 1e-10, "Mean");
		Assertions.assertEquals(e.getVariance(), o.getVariance(), 1e-10, "Variance");
		Assertions.assertEquals(e.getStandardDeviation(), o.getStandardDeviation(), 1e-10, "SD");
	}

	@Test
	public void canAddStatistics()
	{
		final int[] d1 = SimpleArrayUtils.newArray(100, 0, 1);
		final int[] d2 = SimpleArrayUtils.newArray(100, 4, 1);
		final Statistics o = new Statistics();
		o.add(d1);
		final Statistics o2 = new Statistics();
		o2.add(d2);
		final Statistics o3 = new Statistics();
		o3.add(o);
		o3.add(o2);
		final Statistics o4 = new Statistics();
		o4.add(d1);
		o4.add(d2);

		Assertions.assertEquals(o3.getN(), o4.getN(), "N");
		Assertions.assertEquals(o3.getMean(), o4.getMean(), "Mean");
		Assertions.assertEquals(o3.getVariance(), o4.getVariance(), "Variance");
		Assertions.assertEquals(o3.getStandardDeviation(), o4.getStandardDeviation(), "SD");
	}

	@Test
	public void cannotComputeWithLargeNumbers()
	{
		// https://en.wikipedia.org/wiki/Algorithms_for_calculating_variance#Example
		final double[] v = new double[] { 4, 7, 13, 16 };
		Statistics o = new Statistics();
		o.add(v);
		Assertions.assertEquals(10, o.getMean(), "Mean");
		Assertions.assertEquals(30, o.getVariance(), "Variance");

		final double add = Math.pow(10, 9);
		for (int i = 0; i < v.length; i++)
			v[i] += add;

		final Statistics o2 = new Statistics();
		o2.add(v);
		Assertions.assertEquals(10 + add, o2.getMean(), "Mean");

		// Expect this to be totally wrong
		Assertions.assertThrows(AssertionFailedError.class, () -> {
			Assertions.assertEquals(30, o2.getVariance(), 5, "Variance");
		});
	}
}
