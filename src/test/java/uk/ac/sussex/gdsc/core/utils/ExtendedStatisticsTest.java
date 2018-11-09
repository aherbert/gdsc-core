package uk.ac.sussex.gdsc.core.utils;

import uk.ac.sussex.gdsc.test.junit5.RandomSeed;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.rng.RngUtils;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.sampling.PermutationSampler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"javadoc"})
public class ExtendedStatisticsTest {
  @SeededTest
  public void canComputeStatistics(RandomSeed seed) {
    final UniformRandomProvider r = RngUtils.create(seed.getSeedAsLong());
    DescriptiveStatistics expected;
    ExtendedStatistics observed;
    for (int i = 0; i < 10; i++) {
      expected = new DescriptiveStatistics();
      observed = new ExtendedStatistics();
      for (int j = 0; j < 100; j++) {
        final double d = r.nextDouble();
        expected.addValue(d);
        observed.add(d);
        check(expected, observed);
      }
    }

    expected = new DescriptiveStatistics();
    observed = new ExtendedStatistics();
    final int[] idata = SimpleArrayUtils.newArray(100, 0, 1);
    PermutationSampler.shuffle(r, idata);
    for (final double v : idata) {
      expected.addValue(v);
    }
    observed.add(idata);
    check(expected, observed);

    expected = new DescriptiveStatistics();
    observed = new ExtendedStatistics();
    final double[] ddata = new double[idata.length];
    for (int i = 0; i < idata.length; i++) {
      ddata[i] = idata[i];
      expected.addValue(ddata[i]);
    }
    observed.add(ddata);
    check(expected, observed);

    expected = new DescriptiveStatistics();
    observed = new ExtendedStatistics();
    final float[] fdata = new float[idata.length];
    for (int i = 0; i < idata.length; i++) {
      fdata[i] = idata[i];
      expected.addValue(fdata[i]);
    }
    observed.add(fdata);
    check(expected, observed);
  }

  private static void check(DescriptiveStatistics expected, ExtendedStatistics observed) {
    Assertions.assertEquals(expected.getN(), observed.getN(), "N");
    Assertions.assertEquals(expected.getMean(), observed.getMean(), 1e-10, "Mean");
    Assertions.assertEquals(expected.getVariance(), observed.getVariance(), 1e-10, "Variance");
    Assertions.assertEquals(expected.getStandardDeviation(), observed.getStandardDeviation(), 1e-10,
        "SD");
    Assertions.assertEquals(expected.getMin(), observed.getMin(), "Min");
    Assertions.assertEquals(expected.getMax(), observed.getMax(), "Max");
  }

  @Test
  public void canAddStatistics() {
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
