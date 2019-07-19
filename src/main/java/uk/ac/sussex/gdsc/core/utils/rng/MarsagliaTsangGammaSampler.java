/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package uk.ac.sussex.gdsc.core.utils.rng;

import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.sampling.distribution.ContinuousSampler;
import org.apache.commons.rng.sampling.distribution.NormalizedGaussianSampler;
import org.apache.commons.rng.sampling.distribution.ZigguratNormalizedGaussianSampler;

/**
 * Sampling from the <a href="http://mathworld.wolfram.com/GammaDistribution.html">Gamma
 * distribution</a>.
 *
 * <p>This sampler is only valid if the alpha (scale) parameter is {@code >= 1}. Uses the method
 * from <blockquote>Marsaglia and Tsang, <i>A Simple Method for Generating Gamma Variables.</i> ACM
 * Transactions on Mathematical Software, Volume 26 Issue 3, September, 2000. </blockquote></p>
 *
 * <p>Sampling uses:</p>
 *
 * <ul> <li>{@link UniformRandomProvider#nextDouble()}</li>
 * <li>{@link UniformRandomProvider#nextLong()}</li> </ul>
 *
 * <p>This code has been adapted from Apache Commons RNG. Modifications have been made so that: the
 * sampler is only suitable for a scale parameter above 1; the scale and shape parameter can be set
 * after construction.</p>
 *
 * @since 2.0
 * @see <a href="https://en.wikipedia.org/wiki/Gamma_distribution">Wikipedia Gamma distribution</a>
 */
public class MarsagliaTsangGammaSampler implements ContinuousSampler {
  /** 1/3. */
  private static final double ONE_THIRD = 1d / 3;

  /** Underlying source of randomness. */
  private final UniformRandomProvider rng;
  /** The theta parameter. This is a scale parameter. */
  private double theta;

  /** Optimisation (see code). */
  private double dd;
  /** Optimisation (see code). */
  private double cc;
  /** Gaussian sampling. */
  private final NormalizedGaussianSampler gaussian;

  /**
   * Class to sample from the Gamma distribution when the {@code alpha >= 1}.
   *
   * <p>This performs no parameter checks!</p>
   *
   * @param rng Generator of uniformly distributed random numbers.
   * @param alpha Alpha (shape) parameter of the distribution (must be {@code >= 1}).
   * @param theta Theta (scale) parameter of the distribution (must be {@code > 0}).
   */
  public MarsagliaTsangGammaSampler(UniformRandomProvider rng, double alpha, double theta) {
    this.rng = rng;
    setTheta(theta);
    setAlpha(alpha);
    gaussian = new ZigguratNormalizedGaussianSampler(rng);
  }

  /**
   * Sets the alpha (shape) parameter.
   *
   * <p>This performs no parameter checks!</p>
   *
   * @param alpha Alpha (shape) parameter of the distribution (must be {@code >= 1}).
   */
  public final void setAlpha(double alpha) {
    dd = alpha - ONE_THIRD;
    cc = ONE_THIRD / Math.sqrt(dd);
  }

  /**
   * Sets the theta (scale) parameter.
   *
   * <p>This performs no parameter checks!</p>
   *
   * @param theta Theta (scale) parameter of the distribution (must be {@code > 0}).
   */
  public final void setTheta(double theta) {
    this.theta = theta;
  }

  @Override
  public double sample() {
    for (;;) {
      final double x = gaussian.sample();
      final double oPcTx = 1 + cc * x;
      final double v = oPcTx * oPcTx * oPcTx;

      if (v <= 0) {
        continue;
      }

      final double x2 = x * x;
      final double u = rng.nextDouble();

      // Squeeze.
      if (u < 1 - 0.0331 * x2 * x2) {
        return theta * dd * v;
      }

      if (Math.log(u) < 0.5 * x2 + dd * (1 - v + Math.log(v))) {
        return theta * dd * v;
      }
    }
  }
}
