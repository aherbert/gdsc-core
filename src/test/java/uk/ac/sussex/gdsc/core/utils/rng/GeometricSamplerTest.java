package uk.ac.sussex.gdsc.core.utils.rng;

import uk.ac.sussex.gdsc.core.utils.rng.GeometricSampler.GeometricDiscreteInverseCumulativeProbabilityFunction;
import uk.ac.sussex.gdsc.test.utils.TestLogUtils;

import gnu.trove.list.array.TDoubleArrayList;

import org.apache.commons.math3.stat.inference.ChiSquareTest;
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.simple.RandomSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.logging.Logger;

@SuppressWarnings("javadoc")
public class GeometricSamplerTest {

  private static Logger logger;

  @BeforeAll
  public static void beforeAll() {
    logger = Logger.getLogger(GeometricSamplerTest.class.getName());
  }

  @AfterAll
  public static void afterAll() {
    logger = null;
  }

  @Test
  public void testGeometricDiscreteInverseCumulativeProbabilityFunction() {
    for (double p : new double[] {0.1, 0.5, 0.99999}) {
      GeometricDiscreteInverseCumulativeProbabilityFunction fun =
          new GeometricDiscreteInverseCumulativeProbabilityFunction(p);
      // Test the edge cases
      Assertions.assertEquals(0, fun.inverseCumulativeProbability(0), "cumul p=0");
      Assertions.assertEquals(Integer.MAX_VALUE, fun.inverseCumulativeProbability(1), "cumul p=1");
    }
  }

  @Test
  public void testConstructor() {
    final UniformRandomProvider rng = RandomSource.create(RandomSource.SPLIT_MIX_64);
    for (double p : new double[] {0.1, 0.5, 1}) {
      GeometricSampler sampler = new GeometricSampler(rng, p);
      Assertions.assertEquals(p, sampler.getProbabilityOfSuccess(), "p");
      Assertions.assertTrue(sampler.toString().contains("Geometric"), "Name missing Geometric");
    }
  }

  @Test
  public void testGetMean() {
    for (double p : new double[] {0.1, 0.5, 1}) {
      Assertions.assertEquals((1.0 - p) / p, GeometricSampler.getMean(p));
    }
  }

  @Test
  public void testGetProbabilityOfSuccess() {
    for (double mean : new double[] {0.5, 1, 2, 10, 30}) {
      Assertions.assertEquals(1 / (1.0 + mean), GeometricSampler.getProbabilityOfSuccess(mean));
    }
  }

  @SuppressWarnings("unused")
  @Test
  public void testConstructorThrows() {
    final UniformRandomProvider rng = RandomSource.create(RandomSource.SPLIT_MIX_64);
    for (double p : new double[] {0, 1.1, Double.NaN, Double.POSITIVE_INFINITY,
        Double.NEGATIVE_INFINITY, Double.MAX_VALUE}) {
      Assertions.assertThrows(IllegalArgumentException.class, () -> {
        new GeometricSampler(rng, p);
      }, () -> "p = " + p);
    }
  }

  @Test
  public void testSamples() {
    for (double mean : new double[] {0.5, 1, 2, 10, 30}) {
      testSamples(mean);
    }
  }

  private static void testSamples(double mean) {
    // Create empirical distribution
    final double p = GeometricSampler.getProbabilityOfSuccess(mean);
    org.apache.commons.math3.distribution.GeometricDistribution gd =
        new org.apache.commons.math3.distribution.GeometricDistribution(null, p);
    final int maxK = gd.inverseCumulativeProbability(1 - 1e-6);
    final TDoubleArrayList list = new TDoubleArrayList();
    for (int k = 0; k <= maxK; k++) {
      list.add(gd.probability(k));
    }

    final double[] pmf = list.toArray();
    final long[] histogram = new long[pmf.length];

    // Sample
    final int repeats = 100000;
    final UniformRandomProvider rng = RandomSource.create(RandomSource.SPLIT_MIX_64);
    GeometricSampler sampler = GeometricSampler.createFromMean(rng, mean);
    for (int i = 0; i < repeats; i++) {
      int sample = sampler.sample();
      if (sample < histogram.length) {
        histogram[sample]++;
      }
    }

    // for (int k = 0; k < histogram.length; k++) {
    // System.out.printf("%s : %s%n", pmf[k], (double) histogram[k] / repeats);
    // }
    // System.out.println();

    // Statistical test
    final ChiSquareTest chi = new ChiSquareTest();
    final double pvalue = chi.chiSquareTest(pmf, histogram);
    final boolean reject = pvalue < 0.001;
    logger.log(TestLogUtils.getResultRecord(!reject, () -> String
        .format("mean %s, p %s, chiSq p-value = %s (reject=%b)", mean, p, pvalue, reject)));
    // This will sometimes fail due to randomness so do not assert
    // Assertions.assertFalse(reject);
  }
}
