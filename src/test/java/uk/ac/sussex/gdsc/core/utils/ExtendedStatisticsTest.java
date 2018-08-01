package uk.ac.sussex.gdsc.core.utils;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.sampling.PermutationSampler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import uk.ac.sussex.gdsc.test.TestSettings;
import uk.ac.sussex.gdsc.test.junit5.RandomSeed;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;

@SuppressWarnings({ "javadoc" })
public class ExtendedStatisticsTest
{
	@SeededTest
	public void canComputeStatistics(RandomSeed seed)
	{
		final UniformRandomProvider r = TestSettings.getRandomGenerator(seed.getSeed());
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
		PermutationSampler.shuffle(r, idata);
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
		Assertions.assertEquals(e.getN(), o.getN(), "N");
		Assertions.assertEquals(e.getMean(), o.getMean(), 1e-10, "Mean");
		Assertions.assertEquals(e.getVariance(), o.getVariance(), 1e-10, "Variance");
		Assertions.assertEquals(e.getStandardDeviation(), o.getStandardDeviation(), 1e-10, "SD");
		Assertions.assertEquals(e.getMin(), o.getMin(), "Min");
		Assertions.assertEquals(e.getMax(), o.getMax(), "Max");
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

		Assertions.assertEquals(o3.getN(), o4.getN(), "N");
		Assertions.assertEquals(o3.getMean(), o4.getMean(), "Mean");
		Assertions.assertEquals(o3.getVariance(), o4.getVariance(), "Variance");
		Assertions.assertEquals(o3.getStandardDeviation(), o4.getStandardDeviation(), "SD");
		Assertions.assertEquals(o3.getMin(), o4.getMin(), "Min");
		Assertions.assertEquals(o3.getMax(), o4.getMax(), "Max");
	}
}
