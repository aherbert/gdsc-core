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

import java.util.Objects;
import java.util.function.Function;
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.sampling.distribution.SharedStateContinuousSampler;
import org.apache.commons.rng.sampling.distribution.SharedStateDiscreteSampler;

/**
 * Supply splittable instances.
 */
public final class Splittables {

  /**
   * No public constructor.
   */
  private Splittables() {}

  /**
   * A splittable SharedStateDiscreteSampler using a SplittableUniformRandomProvider to provide
   * split functionality.
   */
  private static class SplittableDiscreteSampler implements SplittableIntSupplier {
    /** The random generator. */
    final SplittableUniformRandomProvider rng;
    /** The sampler. */
    final SharedStateDiscreteSampler sampler;

    /**
     * Create an instance.
     *
     * @param rng the random generator
     * @param sampler the sampler
     */
    SplittableDiscreteSampler(SplittableUniformRandomProvider rng,
        SharedStateDiscreteSampler sampler) {
      this.rng = rng;
      this.sampler = sampler;
    }

    @Override
    public int getAsInt() {
      return sampler.sample();
    }

    @Override
    public SplittableIntSupplier split() {
      final SplittableUniformRandomProvider rng2 = rng.split();
      return new SplittableDiscreteSampler(rng2, sampler.withUniformRandomProvider(rng2));
    }
  }

  /**
   * A splittable SharedStateContinuousSampler using a SplittableUniformRandomProvider to provide
   * split functionality.
   */
  private static class SplittableContinuousSampler implements SplittableDoubleSupplier {
    /** The random generator. */
    final SplittableUniformRandomProvider rng;
    /** The sampler. */
    final SharedStateContinuousSampler sampler;

    /**
     * Create an instance.
     *
     * @param rng the random generator
     * @param sampler the sampler
     */
    SplittableContinuousSampler(SplittableUniformRandomProvider rng,
        SharedStateContinuousSampler sampler) {
      this.rng = rng;
      this.sampler = sampler;
    }

    @Override
    public double getAsDouble() {
      return sampler.sample();
    }

    @Override
    public SplittableDoubleSupplier split() {
      final SplittableUniformRandomProvider rng2 = rng.split();
      return new SplittableContinuousSampler(rng2, sampler.withUniformRandomProvider(rng2));
    }
  }

  /**
   * Create a splittable supplier of {@code int} values.
   *
   * @param rng the random generator
   * @param sampler the sampler
   * @return the splittable supplier
   */
  public static SplittableIntSupplier ofInt(SplittableUniformRandomProvider rng,
      SharedStateDiscreteSampler sampler) {
    Objects.requireNonNull(rng, "rng");
    Objects.requireNonNull(sampler, "sampler");
    return new SplittableDiscreteSampler(rng, sampler);
  }

  /**
   * Create a splittable supplier of {@code int} values.
   *
   * @param rng the random generator
   * @param samplerFactory the sampler factory
   * @return the splittable supplier
   */
  public static SplittableIntSupplier ofInt(SplittableUniformRandomProvider rng,
      Function<UniformRandomProvider, SharedStateDiscreteSampler> samplerFactory) {
    Objects.requireNonNull(rng, "rng");
    Objects.requireNonNull(samplerFactory, "samplerFactory");
    return new SplittableDiscreteSampler(rng, samplerFactory.apply(rng));
  }

  /**
   * Create a splittable supplier of {@code double} values.
   *
   * @param rng the random generator
   * @param sampler the sampler
   * @return the splittable supplier
   */
  public static SplittableDoubleSupplier ofDouble(SplittableUniformRandomProvider rng,
      SharedStateContinuousSampler sampler) {
    Objects.requireNonNull(rng, "rng");
    Objects.requireNonNull(sampler, "sampler");
    return new SplittableContinuousSampler(rng, sampler);
  }

  /**
   * Create a splittable supplier of {@code double} values.
   *
   * @param rng the random generator
   * @param samplerFactory the sampler factory
   * @return the splittable supplier
   */
  public static SplittableDoubleSupplier ofDouble(SplittableUniformRandomProvider rng,
      Function<UniformRandomProvider, SharedStateContinuousSampler> samplerFactory) {
    Objects.requireNonNull(rng, "rng");
    Objects.requireNonNull(samplerFactory, "samplerFactory");
    return new SplittableContinuousSampler(rng, samplerFactory.apply(rng));
  }
}
