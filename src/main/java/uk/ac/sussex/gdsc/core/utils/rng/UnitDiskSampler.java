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

import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.sampling.SharedStateSampler;

/**
 * Class for generating a random point within a unit disk.
 *
 * @since 2.0
 * @see <a href="https://mathworld.wolfram.com/DiskPointPicking.html">Disk point picking</a>
 */
public class UnitDiskSampler implements SharedStateSampler<UnitDiskSampler> {

  /** The generator of uniformly distributed random numbers. */
  private final UniformRandomProvider rng;

  /**
   * Creates a new instance.
   *
   * @param rng Generator of uniformly distributed random numbers.
   */
  private UnitDiskSampler(UniformRandomProvider rng) {
    this.rng = rng;
  }

  /**
   * Create a new instance.
   *
   * @param rng Generator of uniformly distributed random numbers.
   * @return the sampler
   */
  public static UnitDiskSampler of(UniformRandomProvider rng) {
    return new UnitDiskSampler(rng);
  }

  /**
   * Sample from the unit circle.
   *
   * @return the sample
   */
  public double[] sample() {
    // Generate via rejection method of a circle inside a square of edge length 2.
    // This should compute approximately 2^2 / pi = 1.27 square positions per sample.
    double x;
    double y;
    do {
      x = next();
      y = next();
    } while (x * x + y * y >= 1.0);
    return new double[] {x, y};
  }

  /**
   * Compute the next double in the interval {@code [-1, 1)}.
   *
   * @return the double
   */
  private double next() {
    return NumberUtils.makeSignedDouble(rng.nextLong());
  }

  @Override
  public UnitDiskSampler withUniformRandomProvider(UniformRandomProvider rng) {
    return of(rng);
  }
}
