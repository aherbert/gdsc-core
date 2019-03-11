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
 * Copyright (C) 2011 - 2019 Alex Herbert
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

package uk.ac.sussex.gdsc.core.utils;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.rng.UniformRandomProvider;

/**
 * Adapts {@link org.apache.commons.math3.random.RandomGenerator} for the
 * {@link org.apache.commons.rng.UniformRandomProvider} interface.
 */
public class UniformRandomProviderAdapter implements UniformRandomProvider {
  private final RandomGenerator rg;

  /**
   * Instantiates a new uniform random provider adapter.
   *
   * @param randomGenerator the random generator
   */
  public UniformRandomProviderAdapter(RandomGenerator randomGenerator) {
    if (randomGenerator == null) {
      throw new IllegalArgumentException("Random generator must not be null");
    }
    this.rg = randomGenerator;
  }

  @Override
  public void nextBytes(byte[] bytes) {
    rg.nextBytes(bytes);
  }

  /**
   * {@inheritDoc}
   *
   * <p><strong>Note:</strong> This is a dumb implementation that create a new byte array of the
   * given length, fills that using the underlying {@link RandomGenerator} and then copies it to the
   * input.
   */
  @Override
  public void nextBytes(byte[] bytes, int start, int len) {
    if (start == 0 && len == bytes.length) {
      rg.nextBytes(bytes);
      return;
    }
    // Create a tmp byte array
    final byte[] tmp = new byte[len];
    rg.nextBytes(tmp);
    System.arraycopy(tmp, 0, bytes, start, len);
  }

  @Override
  public int nextInt() {
    return rg.nextInt();
  }

  @Override
  public int nextInt(int n) {
    return rg.nextInt(n);
  }

  @Override
  public long nextLong() {
    return rg.nextLong();
  }

  @Override
  public long nextLong(long n) {
    // Copied from org.apache.commons.rng.core.BaseProvider
    if (n <= 0) {
      throw new IllegalArgumentException("Must be strictly positive: " + n);
    }

    long bits;
    long val;
    do {
      bits = nextLong() >>> 1;
      val = bits % n;
    }
    while (bits - val + (n - 1) < 0);

    return val;
  }

  @Override
  public boolean nextBoolean() {
    return rg.nextBoolean();
  }

  @Override
  public float nextFloat() {
    return rg.nextFloat();
  }

  @Override
  public double nextDouble() {
    return rg.nextDouble();
  }
}
