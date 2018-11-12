package uk.ac.sussex.gdsc.core.utils;

import uk.ac.sussex.gdsc.test.api.TestAssertions;
import uk.ac.sussex.gdsc.test.api.TestHelper;
import uk.ac.sussex.gdsc.test.api.function.DoubleDoubleBiPredicate;
import uk.ac.sussex.gdsc.test.junit5.RandomSeed;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.rng.RngUtils;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.sampling.PermutationSampler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"javadoc"})
public class RollingStatisticsTest {
  @SeededTest
  public void canComputeStatistics(RandomSeed seed) {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeedAsLong());
    DescriptiveStatistics expected;
    RollingStatistics observed;
    for (int i = 0; i < 10; i++) {
      expected = new DescriptiveStatistics();
      observed = new RollingStatistics();
      for (int j = 0; j < 100; j++) {
        final double d = rng.nextDouble();
        expected.addValue(d);
        observed.add(d);
        check(expected, observed);
      }
    }

    expected = new DescriptiveStatistics();
    observed = new RollingStatistics();
    final int[] idata = SimpleArrayUtils.natural(100);
    PermutationSampler.shuffle(rng, idata);
    for (final double v : idata) {
      expected.addValue(v);
    }
    observed.add(idata);
    check(expected, observed);

    expected = new DescriptiveStatistics();
    observed = new RollingStatistics();
    final double[] ddata = new double[idata.length];
    for (int i = 0; i < idata.length; i++) {
      ddata[i] = idata[i];
      expected.addValue(ddata[i]);
    }
    observed.add(ddata);
    check(expected, observed);

    expected = new DescriptiveStatistics();
    observed = new RollingStatistics();
    final float[] fdata = new float[idata.length];
    for (int i = 0; i < idata.length; i++) {
      fdata[i] = idata[i];
      expected.addValue(fdata[i]);
    }
    observed.add(fdata);
    check(expected, observed);
  }

  private static void check(DescriptiveStatistics expected, RollingStatistics observed) {
    Assertions.assertEquals(expected.getN(), observed.getN(), "N");
    Assertions.assertEquals(expected.getMean(), observed.getMean(), 1e-10, "Mean");
    Assertions.assertEquals(expected.getVariance(), observed.getVariance(), 1e-10, "Variance");
    Assertions.assertEquals(expected.getStandardDeviation(), observed.getStandardDeviation(), 1e-10,
        "SD");
  }

  @Test
  public void canAddStatistics() {
    final int[] d1 = SimpleArrayUtils.natural(100);
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

    final DoubleDoubleBiPredicate equality = TestHelper.doublesAreClose(1e-10, 0);
    Assertions.assertEquals(o3.getN(), o4.getN(), "N");
    TestAssertions.assertTest(o3.getMean(), o4.getMean(), equality, "Mean");
    TestAssertions.assertTest(o3.getVariance(), o4.getVariance(), equality, "Variance");
    TestAssertions.assertTest(o3.getStandardDeviation(), o4.getStandardDeviation(), equality, "SD");
  }

  @Test
  public void canComputeWithLargeNumbers() {
    // https://en.wikipedia.org/wiki/Algorithms_for_calculating_variance#Example
    final double[] v = new double[] {4, 7, 13, 16};
    final RollingStatistics o = new RollingStatistics();
    o.add(v);
    Assertions.assertEquals(10, o.getMean(), "Mean");
    Assertions.assertEquals(30, o.getVariance(), "Variance");

    final double add = Math.pow(10, 9);
    for (int i = 0; i < v.length; i++) {
      v[i] += add;
    }
    final Statistics o2 = new RollingStatistics();
    o2.add(v);
    Assertions.assertEquals(10 + add, o2.getMean(), "Mean");
    Assertions.assertEquals(30, o2.getVariance(), "Variance");
  }
}
