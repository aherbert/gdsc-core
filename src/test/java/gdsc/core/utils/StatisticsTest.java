package gdsc.core.utils;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.util.MathArrays;
import org.junit.Assert;
import org.junit.Test;

public class StatisticsTest
{
	@Test
	public void canComputeStatistics()
	{
		RandomGenerator r = new Well19937c(30051977);
		DescriptiveStatistics e;
		Statistics o;
		for (int i = 0; i < 10; i++)
		{
			e = new DescriptiveStatistics();
			o = new Statistics();
			for (int j = 0; j < 100; j++)
			{
				double d = r.nextDouble();
				e.addValue(d);
				o.add(d);
				check(e, o);
			}
		}

		e = new DescriptiveStatistics();
		o = new Statistics();
		int[] idata = SimpleArrayUtils.newArray(100, 0, 1);
		MathArrays.shuffle(idata, r);
		for (double v : idata)
			e.addValue(v);
		o.add(idata);
		check(e, o);

		e = new DescriptiveStatistics();
		o = new Statistics();
		double[] ddata = new double[idata.length];
		for (int i = 0; i < idata.length; i++)
		{
			ddata[i] = idata[i];
			e.addValue(ddata[i]);
		}
		o.add(ddata);
		check(e, o);
		
		e = new DescriptiveStatistics();
		o = new Statistics();
		float[] fdata = new float[idata.length];
		for (int i = 0; i < idata.length; i++)
		{
			fdata[i] = idata[i];
			e.addValue(fdata[i]);
		}
		o.add(fdata);
		check(e, o);
	}

	private void check(DescriptiveStatistics e, Statistics o)
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
		Statistics o = new Statistics();
		o.add(d1);
		Statistics o2 = new Statistics();
		o2.add(d2);
		Statistics o3 = new Statistics();
		o3.add(o);
		o3.add(o2);
		Statistics o4 = new Statistics();
		o4.add(d1);
		o4.add(d2);

		Assert.assertEquals("Variance", o3.getVariance(), o4.getVariance(), 0);
	}
}
