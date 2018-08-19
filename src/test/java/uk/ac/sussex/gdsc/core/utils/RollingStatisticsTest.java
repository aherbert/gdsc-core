package uk.ac.sussex.gdsc.core.utils;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.sampling.PermutationSampler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import uk.ac.sussex.gdsc.test.junit5.ExtraAssertions;
import uk.ac.sussex.gdsc.test.junit5.RandomSeed;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.rng.RNGFactory;

@SuppressWarnings({ "javadoc" })
public class RollingStatisticsTest
{
    @SeededTest
    public void canComputeStatistics(RandomSeed seed)
    {
        final UniformRandomProvider r = RNGFactory.create(seed.getSeed());
        DescriptiveStatistics e;
        RollingStatistics o;
        for (int i = 0; i < 10; i++)
        {
            e = new DescriptiveStatistics();
            o = new RollingStatistics();
            for (int j = 0; j < 100; j++)
            {
                final double d = r.nextDouble();
                e.addValue(d);
                o.add(d);
                check(e, o);
            }
        }

        e = new DescriptiveStatistics();
        o = new RollingStatistics();
        final int[] idata = SimpleArrayUtils.newArray(100, 0, 1);
        PermutationSampler.shuffle(r, idata);
        for (final double v : idata)
            e.addValue(v);
        o.add(idata);
        check(e, o);

        e = new DescriptiveStatistics();
        o = new RollingStatistics();
        final double[] ddata = new double[idata.length];
        for (int i = 0; i < idata.length; i++)
        {
            ddata[i] = idata[i];
            e.addValue(ddata[i]);
        }
        o.add(ddata);
        check(e, o);

        e = new DescriptiveStatistics();
        o = new RollingStatistics();
        final float[] fdata = new float[idata.length];
        for (int i = 0; i < idata.length; i++)
        {
            fdata[i] = idata[i];
            e.addValue(fdata[i]);
        }
        o.add(fdata);
        check(e, o);
    }

    private static void check(DescriptiveStatistics e, RollingStatistics o)
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
        final RollingStatistics o = new RollingStatistics();
        o.add(d1);
        final RollingStatistics o2 = new RollingStatistics();
        o2.add(d2);
        final RollingStatistics o3 = new RollingStatistics();
        o3.add(o);
        o3.add(o2);
        final RollingStatistics o4 = new RollingStatistics();
        o4.add(d1);
        o4.add(d2);

        Assertions.assertEquals(o3.getN(), o4.getN(), "N");
        ExtraAssertions.assertEqualsRelative(o3.getMean(), o4.getMean(), 1e-10, "Mean");
        ExtraAssertions.assertEqualsRelative(o3.getVariance(), o4.getVariance(), 1e-10, "Variance");
        ExtraAssertions.assertEqualsRelative(o3.getStandardDeviation(), o4.getStandardDeviation(), 1e-10, "SD");
    }

    @Test
    public void canComputeWithLargeNumbers()
    {
        // https://en.wikipedia.org/wiki/Algorithms_for_calculating_variance#Example
        final double[] v = new double[] { 4, 7, 13, 16 };
        final RollingStatistics o = new RollingStatistics();
        o.add(v);
        Assertions.assertEquals(10, o.getMean(), "Mean");
        Assertions.assertEquals(30, o.getVariance(), "Variance");

        final double add = Math.pow(10, 9);
        for (int i = 0; i < v.length; i++)
            v[i] += add;
        final Statistics o2 = new RollingStatistics();
        o2.add(v);
        Assertions.assertEquals(10 + add, o2.getMean(), "Mean");
        Assertions.assertEquals(30, o2.getVariance(), "Variance");
    }
}
