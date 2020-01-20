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
import org.apache.commons.rng.sampling.distribution.SharedStateContinuousSampler;

/**
 * Sampling from the interval {@code [-1, 1)}.
 * 
 * <p>Samples evenly from the 2<sup>54</sup> dyadic rationals in the range.
 * Sampling uses 64-bits per sample and branch conditions are avoided for efficient sampling.
 * This sampler can be used as an alternative to:
 * <pre>
 * // Samples from 2^54 dyadic rationals with a branch condition
 * x = rng.nextDouble() - (rng.nextBoolean() ? 1.0 : 0);
 *
 * // Samples from 2^54 dyadic rationals with extra random bit required
 * x = rng.nextDouble() - rng.nextInt(1);
 *
 * // Samples from 2^53 dyadic rationals with no branch condition 
 * x = 2 * rng.nextDouble() - 1.0;
 * </pre>
 *
 * @since 2.0
 */
public class SignedDoubleSampler implements SharedStateContinuousSampler {
  /** Underlying source of randomness. */
  private final UniformRandomProvider rng;

  /**
   * Class to sample from the range {@code [-1, 1)}.
   *
   * @param rng Generator of uniformly distributed random numbers.
   */
  public SignedDoubleSampler(UniformRandomProvider rng) {
    this.rng = rng;
  }

  @Override
  public double sample() {
    return NumberUtils.makeSignedDouble(rng.nextLong());
  }

  @Override
  public SharedStateContinuousSampler withUniformRandomProvider(UniformRandomProvider rng) {
    return new SignedDoubleSampler(rng);
  }
}
