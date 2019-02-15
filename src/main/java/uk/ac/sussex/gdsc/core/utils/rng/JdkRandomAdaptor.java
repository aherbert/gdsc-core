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

package uk.ac.sussex.gdsc.core.utils.rng;

import uk.ac.sussex.gdsc.core.data.NotImplementedException;

import org.apache.commons.rng.UniformRandomProvider;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Objects;
import java.util.Random;

/**
 * Extension of <code>java.util.Random</code> wrapping a {@link UniformRandomProvider}.
 *
 * <p>The seed cannot be set and the adaptor is not serializable.
 */
public class JdkRandomAdaptor extends Random {
  private static final long serialVersionUID = 20190123L;
  /** The generator of uniformly distributed random numbers. */
  private final UniformRandomProvider rng;

  /**
   * Creates a generator of strings.
   *
   * <p>The sampling works using the {@code UniformRandomProvider#nextInt(int)} method so a native
   * generator of ints is best.
   *
   * @param rng Generator of uniformly distributed random numbers.
   * @throws NullPointerException If {@code rng} is null.
   */
  public JdkRandomAdaptor(UniformRandomProvider rng) {
    super(0L);
    this.rng = Objects.requireNonNull(rng, "Random generator must not be null");
  }

  /**
   * This method is not supported.
   *
   * @throws NotImplementedException The seed cannot be set on the underlying source of randomness.
   */
  @Override
  public synchronized void setSeed(long seed) {
    if (rng != null) {
      // This method is called by the super constructor.
      // Only error when initialised.
      throw new NotImplementedException();
    }
  }

  /**
   * Delegates the generation of 32 random bits to the {@code UniformRandomProvider} argument
   * provided at {@link #JdkRandomAdaptor(UniformRandomProvider) construction}. The returned value
   * is such that if the source of randomness is a {@link Random}, all the generated values will be
   * identical to those produced by the same sequence of calls on the same {@link Random} instance.
   *
   * @param n Number of random bits which the requested value must contain.
   * @return the value represented by the {@code n} high-order bits of a pseudo-random 32-bits
   *         integer.
   */
  @Override
  protected synchronized int next(int n) {
    return rng.nextInt() >>> (32 - n);
  }

  /**
   * This method is not supported.
   *
   * @param out the out
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws NotImplementedException The seed cannot be set on the underlying source of randomness.
   */
  @SuppressWarnings({"static-method", "unused"})
  private synchronized void writeObject(ObjectOutputStream out) throws IOException {
    throw new NotImplementedException();
  }

  /**
   * This method is not supported.
   *
   * @param in Input stream.
   * @throws IOException if an error occurs.
   * @throws ClassNotFoundException if an error occurs.
   * @throws NotImplementedException The seed cannot be set on the underlying source of randomness.
   */
  @SuppressWarnings({"static-method", "unused"})
  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    throw new NotImplementedException();
  }
}
