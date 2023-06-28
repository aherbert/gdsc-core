/*-
 * #%L
 * Genome Damage and Stability Centre Core Package
 *
 * Contains core utilities for image analysis and is used by:
 *
 * GDSC ImageJ Plugins - Microscopy image analysis
 *
 * GDSC SMLM ImageJ Plugins - Single molecule localisation microscopy (SMLM)
 * %%
 * Copyright (C) 2011 - 2023 Alex Herbert
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
import org.apache.commons.math3.random.AbstractRandomGenerator;
import org.apache.commons.rng.UniformRandomProvider;
import uk.ac.sussex.gdsc.core.data.NotImplementedException;

/**
 * Adapts {@link org.apache.commons.rng.UniformRandomProvider} interface for the
 * {@link org.apache.commons.math3.random.RandomGenerator} interface.
 *
 * <p>Warning: It is not possible to set the seed.
 */
public class RandomGeneratorAdapter extends AbstractRandomGenerator {
  private final UniformRandomProvider rng;

  /**
   * Instantiates a new random generator adapter.
   *
   * @param uniformRandomProvider the uniform random provider
   */
  public RandomGeneratorAdapter(UniformRandomProvider uniformRandomProvider) {
    this.rng =
        Objects.requireNonNull(uniformRandomProvider, "Uniform random provider must not be null");
  }

  /**
   * Warning: It is not possible to set the seed.
   *
   * @param seed the new seed (ignored)
   * @throws NotImplementedException the not implemented exception
   */
  @Override
  public void setSeed(int seed) {
    throw new NotImplementedException();
  }

  /**
   * Warning: It is not possible to set the seed.
   *
   * @param seed the new seed (ignored)
   * @throws NotImplementedException the not implemented exception
   */
  @Override
  public void setSeed(int[] seed) {
    throw new NotImplementedException();
  }

  /**
   * Warning: It is not possible to set the seed.
   *
   * @param seed the new seed (ignored)
   * @throws NotImplementedException the not implemented exception
   */
  @Override
  public void setSeed(long seed) {
    throw new NotImplementedException();
  }

  @Override
  public void nextBytes(byte[] bytes) {
    rng.nextBytes(bytes);
  }

  @Override
  public int nextInt() {
    return rng.nextInt();
  }

  @Override
  public int nextInt(int n) {
    return rng.nextInt(n);
  }

  @Override
  public long nextLong() {
    return rng.nextLong();
  }

  @Override
  public boolean nextBoolean() {
    return rng.nextBoolean();
  }

  @Override
  public float nextFloat() {
    return rng.nextFloat();
  }

  @Override
  public double nextDouble() {
    return rng.nextDouble();
  }
}

