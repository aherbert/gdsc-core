package uk.ac.sussex.gdsc.core.utils.rng;

import org.apache.commons.math3.distribution.BinomialDistribution;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings("javadoc")
public class BinomialDiscreteInverseCumulativeProbabilityFunctionTest {
  @Test
  public void testInverseCumulativeProbabiity() {
    final int trials = 13;
    final double probabilityOfSuccess = 0.02;
    final BinomialDistribution bd = new BinomialDistribution(trials, probabilityOfSuccess);
    final BinomialDiscreteInverseCumulativeProbabilityFunction fun =
        new BinomialDiscreteInverseCumulativeProbabilityFunction(trials, probabilityOfSuccess);
    for (int i = 0; i <= 10; i++) {
      final double pvalue = i * 0.1;
      Assertions.assertEquals(bd.inverseCumulativeProbability(pvalue),
          fun.inverseCumulativeProbability(pvalue));
    }
  }

  @Test
  public void testSetTrials() {
    final int trials = 13;
    final double probabilityOfSuccess = 0.02;
    final BinomialDistribution bd = new BinomialDistribution(trials, probabilityOfSuccess);
    final BinomialDiscreteInverseCumulativeProbabilityFunction fun =
        new BinomialDiscreteInverseCumulativeProbabilityFunction(trials - 1, probabilityOfSuccess);

    fun.setTrials(trials);

    for (int i = 0; i <= 10; i++) {
      final double pvalue = i * 0.1;
      Assertions.assertEquals(bd.inverseCumulativeProbability(pvalue),
          fun.inverseCumulativeProbability(pvalue));
    }
  }

  @Test
  public void testSetProbabilityOfSuccess() {
    final int trials = 13;
    final double probabilityOfSuccess = 0.02;
    final BinomialDistribution bd = new BinomialDistribution(trials, probabilityOfSuccess);
    final BinomialDiscreteInverseCumulativeProbabilityFunction fun =
        new BinomialDiscreteInverseCumulativeProbabilityFunction(trials,
            probabilityOfSuccess + 0.1);

    fun.setProbabilityOfSuccess(probabilityOfSuccess);

    for (int i = 0; i <= 10; i++) {
      final double pvalue = i * 0.1;
      Assertions.assertEquals(bd.inverseCumulativeProbability(pvalue),
          fun.inverseCumulativeProbability(pvalue));
    }
  }

  @Test
  public void testUpdateDistribution() {
    final int trials = 13;
    final double probabilityOfSuccess = 0.02;
    final BinomialDistribution bd = new BinomialDistribution(trials, probabilityOfSuccess);
    final BinomialDiscreteInverseCumulativeProbabilityFunction fun =
        new BinomialDiscreteInverseCumulativeProbabilityFunction(trials - 1,
            probabilityOfSuccess + 0.1);

    fun.updateDistribution(trials, probabilityOfSuccess);

    for (int i = 0; i <= 10; i++) {
      final double pvalue = i * 0.1;
      Assertions.assertEquals(bd.inverseCumulativeProbability(pvalue),
          fun.inverseCumulativeProbability(pvalue));
    }
  }
}
