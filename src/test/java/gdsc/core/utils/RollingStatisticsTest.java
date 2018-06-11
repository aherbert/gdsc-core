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
import org.apache.commons.math3.random.Well19937c;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.util.MathArrays;
import org.junit.Assert;
import org.junit.Test;

public class RollingStatisticsTest
{
	@Test
	public void canComputeStatistics()
	{
		RandomGenerator r = new Well19937c(30051977);
		DescriptiveStatistics e;
		RollingStatistics o;
		for (int i = 0; i < 10; i++)
		{
			e = new DescriptiveStatistics();
			o = new RollingStatistics();
			for (int j = 0; j < 100; j++)
			{
				double d = r.nextDouble();
				e.addValue(d);
				o.add(d);
				check(e, o);
			}
		}

		e = new DescriptiveStatistics();
		o = new RollingStatistics();
		int[] idata = SimpleArrayUtils.newArray(100, 0, 1);
		MathArrays.shuffle(idata, r);
		for (double v : idata)
			e.addValue(v);
		o.add(idata);
		check(e, o);

		e = new DescriptiveStatistics();
		o = new RollingStatistics();
		double[] ddata = new double[idata.length];
		for (int i = 0; i < idata.length; i++)
		{
			ddata[i] = idata[i];
			e.addValue(ddata[i]);
		}
		o.add(ddata);
		check(e, o);

		e = new DescriptiveStatistics();
		o = new RollingStatistics();
		float[] fdata = new float[idata.length];
		for (int i = 0; i < idata.length; i++)
		{
			fdata[i] = idata[i];
			e.addValue(fdata[i]);
		}
		o.add(fdata);
		check(e, o);
	}

	private void check(DescriptiveStatistics e, RollingStatistics o)
	{
		Assert.assertEquals("N", e.getN(), o.getN(), 0);
		Assert.assertEquals("Mean", e.getMean(), o.getMean(), 1e-10);
		Assert.assertEquals("Variance", e.getVariance(), o.getVariance(), 1e-10);
		Assert.assertEquals("SD", e.getStandardDeviation(), o.getStandardDeviation(), 1e-10);
	}

	@Test
	public void canAddStatistics()
	{
		int[] d1 = SimpleArrayUtils.newArray(100, 0, 1);
		int[] d2 = SimpleArrayUtils.newArray(100, 4, 1);
		RollingStatistics o = new RollingStatistics();
		o.add(d1);
		RollingStatistics o2 = new RollingStatistics();
		o2.add(d2);
		RollingStatistics o3 = new RollingStatistics();
		o3.add(o);
		o3.add(o2);
		RollingStatistics o4 = new RollingStatistics();
		o4.add(d1);
		o4.add(d2);

		Assert.assertEquals("Variance", o3.getVariance(), o4.getVariance(), 0);
	}

	@Test
	public void canComputeWithLargeNumbers()
	{
		// https://en.wikipedia.org/wiki/Algorithms_for_calculating_variance#Example
		double[] v = new double[] { 4, 7, 13, 16 };
		RollingStatistics o = new RollingStatistics();
		o.add(v);
		Assert.assertEquals("Mean", o.getMean(), 10, 0);
		Assert.assertEquals("Variance", o.getVariance(), 30, 0);

		double add = Math.pow(10, 9);
		for (int i = 0; i < v.length; i++)
			v[i] += add;
		o = new RollingStatistics();
		o.add(v);
		Assert.assertEquals("Mean", o.getMean(), add + 10, 0);
		Assert.assertEquals("Variance", o.getVariance(), 30, 0);
	}
}
