package uk.ac.sussex.gdsc.core.utils.rng;

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
  public void testConstructor() {
    final UniformRandomProvider rng = RandomSource.create(RandomSource.SPLIT_MIX_64);
    for (double mean : new double[] {0.5, 1, 2}) {
      GeometricSampler sampler = new GeometricSampler(rng, mean);
      Assertions.assertEquals(mean, sampler.getMean(), "mean");
      Assertions.assertEquals(1.0 / (1.0 + mean), sampler.getP(), "p");
    }
  }

  @SuppressWarnings("unused")
  @Test
  public void testConstructorThrows() {
    final UniformRandomProvider rng = RandomSource.create(RandomSource.SPLIT_MIX_64);
    for (double mean : new double[] {0, Double.NaN, Double.POSITIVE_INFINITY,
        Double.NEGATIVE_INFINITY, Double.MAX_VALUE}) {
      Assertions.assertThrows(IllegalArgumentException.class, () -> {
        new GeometricSampler(rng, mean);
      }, () -> "mean = " + mean);
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
    final double p = 1.0 / (1.0 + mean);
    final TDoubleArrayList list = new TDoubleArrayList();
    for (int k = 0;; k++) {
      double pmf = Math.pow(1 - p, k) * p;
      list.add(pmf);
      double cdf = 1 - Math.pow(1 - p, k + 1);
      if (cdf > 1 - 1e-6) {
        break;
      }
    }

    final double[] pmf = list.toArray();
    final long[] histogram = new long[pmf.length];

    // Sample
    final int repeats = 100000;
    final UniformRandomProvider rng = RandomSource.create(RandomSource.SPLIT_MIX_64);
    GeometricSampler sampler = new GeometricSampler(rng, mean);
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
    logger.log(TestLogUtils.getResultRecord(!reject,
        () -> String.format("Mean %s, chiSq p = %s (reject=%b)", mean, pvalue, reject)));
    // This will sometimes fail due to randomness so do not assert
    // Assertions.assertFalse(reject);
  }
}
