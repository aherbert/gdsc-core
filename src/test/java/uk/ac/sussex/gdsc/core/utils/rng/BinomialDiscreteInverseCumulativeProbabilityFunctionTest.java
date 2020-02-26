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
 * Copyright (C) 2011 - 2020 Alex Herbert
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
