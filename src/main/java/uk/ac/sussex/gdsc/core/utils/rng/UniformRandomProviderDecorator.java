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
import org.apache.commons.rng.UniformRandomProvider;

/**
 * Decorate a {@link UniformRandomProvider}. By default all methods in the
 * {@code UniformRandomProvider} interface are implemented using an underlying instance of the
 * interface. These may be selectively overridden in child classes for example to monitor call
 * frequency or alter the output.
 *
 * @since 2.0
 */
public abstract class UniformRandomProviderDecorator implements UniformRandomProvider {
  /** The rng. */
  private final UniformRandomProvider rng;

  /**
   * Create a new instance.
   *
   * @param rng the rng
   */
  public UniformRandomProviderDecorator(UniformRandomProvider rng) {
    this.rng = Objects.requireNonNull(rng, "RNG must not be null");
  }

  @Override
  public void nextBytes(byte[] bytes) {
    rng.nextBytes(bytes);
  }

  @Override
  public void nextBytes(byte[] bytes, int start, int len) {
    rng.nextBytes(bytes, start, len);
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
  public long nextLong(long n) {
    return rng.nextLong(n);
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

  @Override
  public String toString() {
    return rng.toString();
  }
}
