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
 * Copyright (C) 2011 - 2022 Alex Herbert
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
import org.apache.commons.math3.exception.NotPositiveException;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.rng.sampling.distribution.DiscreteInverseCumulativeProbabilityFunction;

/**
 * Provides the inverse cumulative probability of the Binomial distribution.
 *
 * <p>Allows the number of trials and the probability of success to be updated.
 *
 * @since 2.0
 */
public class BinomialDiscreteInverseCumulativeProbabilityFunction
    implements DiscreteInverseCumulativeProbabilityFunction {

  /** The binomial distribution. */
  private BinomialDistribution bd;

  /**
   * Create a new instance.
   *
   * @param trials Number of trials.
   * @param probabilityOfSuccess Probability of success.
   * @throws NotPositiveException if {@code trials < 0}.
   * @throws OutOfRangeException if {@code p < 0} or {@code p > 1}.
   */
  public BinomialDiscreteInverseCumulativeProbabilityFunction(int trials,
      double probabilityOfSuccess) {
    update(trials, probabilityOfSuccess);
  }

  /**
   * Sets the trials.
   *
   * @param trials the trials
   * @throws NotPositiveException if {@code trials < 0}.
   */
  public void setTrials(int trials) {
    update(trials, bd.getProbabilityOfSuccess());
  }

  /**
   * Sets the probability of success.
   *
   * @param probabilityOfSuccess the probability of success
   * @throws OutOfRangeException if {@code p < 0} or {@code p > 1}.
   */
  public void setProbabilityOfSuccess(double probabilityOfSuccess) {
    update(bd.getNumberOfTrials(), probabilityOfSuccess);
  }

  /**
   * Update the distribution.
   *
   * @param trials the trials
   * @param probabilityOfSuccess the probability of success
   * @throws NotPositiveException if {@code trials < 0}.
   * @throws OutOfRangeException if {@code p < 0} or {@code p > 1}.
   */
  public void updateDistribution(int trials, double probabilityOfSuccess) {
    update(trials, probabilityOfSuccess);
  }

  private void update(int trials, double probabilityOfSuccess) {
    bd = new BinomialDistribution(null, trials, probabilityOfSuccess);
  }

  @Override
  public int inverseCumulativeProbability(double cumulativeProbability) {
    // Delegate the computation
    return bd.inverseCumulativeProbability(cumulativeProbability);
  }
}
