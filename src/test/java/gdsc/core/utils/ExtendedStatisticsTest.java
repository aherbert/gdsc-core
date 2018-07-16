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

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.util.MathArrays;
import org.junit.Assert;
import org.junit.Test;

import gdsc.test.TestSettings;

@SuppressWarnings({ "javadoc" })
public class ExtendedStatisticsTest
{
	@Test
	public void canComputeStatistics()
	{
		final RandomGenerator r = TestSettings.getRandomGenerator();
		DescriptiveStatistics e;
		ExtendedStatistics o;
		for (int i = 0; i < 10; i++)
		{
			e = new DescriptiveStatistics();
			o = new ExtendedStatistics();
			for (int j = 0; j < 100; j++)
			{
				final double d = r.nextDouble();
				e.addValue(d);
				o.add(d);
				check(e, o);
			}
		}

		e = new DescriptiveStatistics();
		o = new ExtendedStatistics();
		final int[] idata = SimpleArrayUtils.newArray(100, 0, 1);
		MathArrays.shuffle(idata, r);
		for (final double v : idata)
			e.addValue(v);
		o.add(idata);
		check(e, o);

		e = new DescriptiveStatistics();
		o = new ExtendedStatistics();
		final double[] ddata = new double[idata.length];
		for (int i = 0; i < idata.length; i++)
		{
			ddata[i] = idata[i];
			e.addValue(ddata[i]);
		}
		o.add(ddata);
		check(e, o);

		e = new DescriptiveStatistics();
		o = new ExtendedStatistics();
		final float[] fdata = new float[idata.length];
		for (int i = 0; i < idata.length; i++)
		{
			fdata[i] = idata[i];
			e.addValue(fdata[i]);
		}
		o.add(fdata);
		check(e, o);
	}

	private static void check(DescriptiveStatistics e, ExtendedStatistics o)
	{
		Assert.assertEquals("N", e.getN(), o.getN(), 0);
		Assert.assertEquals("Mean", e.getMean(), o.getMean(), 1e-10);
		Assert.assertEquals("Variance", e.getVariance(), o.getVariance(), 1e-10);
		Assert.assertEquals("SD", e.getStandardDeviation(), o.getStandardDeviation(), 1e-10);
		Assert.assertEquals("Min", e.getMin(), o.getMin(), 0);
		Assert.assertEquals("Max", e.getMax(), o.getMax(), 0);
	}

	@Test
	public void canAddStatistics()
	{
		final int[] d1 = SimpleArrayUtils.newArray(100, 0, 1);
		final int[] d2 = SimpleArrayUtils.newArray(100, 4, 1);
		final ExtendedStatistics o = new ExtendedStatistics();
		o.add(d1);
		final ExtendedStatistics o2 = new ExtendedStatistics();
		o2.add(d2);
		final ExtendedStatistics o3 = new ExtendedStatistics();
		o3.add(o);
		o3.add(o2);
		final ExtendedStatistics o4 = new ExtendedStatistics();
		o4.add(d1);
		o4.add(d2);

		Assert.assertEquals("N", o3.getN(), o4.getN(), 0);
		Assert.assertEquals("Mean", o3.getMean(), o4.getMean(), 0);
		Assert.assertEquals("Variance", o3.getVariance(), o4.getVariance(), 0);
		Assert.assertEquals("SD", o3.getStandardDeviation(), o4.getStandardDeviation(), 0);
		Assert.assertEquals("Min", o3.getMin(), o4.getMin(), 0);
		Assert.assertEquals("Max", o3.getMax(), o4.getMax(), 0);
	}
}
