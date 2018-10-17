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
 * Copyright (C) 2011 - 2018 Alex Herbert
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

import org.apache.commons.math3.random.AbstractRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.rng.UniformRandomProvider;

/**
 * Contains a set of random numbers that are reused in sequence.
 */
public class PseudoRandomGenerator extends AbstractRandomGenerator implements Cloneable {
  /** The sequence. */
  protected final double[] sequence;

  /** The length. */
  protected final int length;

  private int position = 0;

  /**
   * Instantiates a new pseudo random generator.
   *
   * @param sequence the sequence (must contains numbers in the interval 0 to 1)
   * @throws IllegalArgumentException if the sequence is not positive in length and contains numbers
   *         outside the interval 0 to 1.
   */
  public PseudoRandomGenerator(double[] sequence) throws IllegalArgumentException {
    this(sequence, sequence.length);
  }

  /**
   * Instantiates a new pseudo random generator.
   *
   * @param sequence the sequence (must contains numbers in the interval 0 to 1)
   * @param length the length (if greater than sequence.length it is set to length)
   * @throws IllegalArgumentException if the sequence is not positive in length and contains numbers
   *         outside the interval 0 to 1.
   */
  public PseudoRandomGenerator(double[] sequence, int length) throws IllegalArgumentException {
    if (sequence == null || length < 1) {
      throw new IllegalArgumentException("Sequence must have a positive length");
    }
    if (length > sequence.length) {
      length = sequence.length;
    }
    for (int i = length; i-- > 0;) {
      if (sequence[i] < 0 || sequence[i] > 1) {
        throw new IllegalArgumentException(
            "Sequence must contain numbers between 0 and 1 inclusive");
      }
    }
    this.sequence = sequence;
    this.length = length;
  }

  /**
   * Instantiates a new pseudo random generator with no checks on the numbers.
   *
   * @param sequence the sequence (must contains numbers in the interval 0 to 1)
   * @param length the length (if greater than sequence.length it is set to length)
   * @param dummy the dummy parameter flag
   * @throws IllegalArgumentException if the size is not positive
   */
  PseudoRandomGenerator(double[] sequence, int length, boolean dummy)
      throws IllegalArgumentException {
    if (sequence == null || length < 1) {
      throw new IllegalArgumentException("Sequence must have a positive length");
    }
    this.sequence = sequence;
    this.length = length;
  }

  /**
   * Instantiates a new pseudo random generator of the given size.
   *
   * @param size the size
   * @param source the random source
   * @throws IllegalArgumentException if the size is not positive
   * @throws NullPointerException if the generator is null
   */
  public PseudoRandomGenerator(int size, RandomGenerator source)
      throws IllegalArgumentException, NullPointerException {
    if (size < 1) {
      throw new IllegalArgumentException("Sequence must have a positive length");
    }
    if (source == null) {
      throw new NullPointerException("Source generator must not be null");
    }
    sequence = new double[size];
    length = size;
    // Preserve order
    for (int i = 0; i < size; i++) {
      sequence[i] = source.nextDouble();
    }
  }

  /**
   * Instantiates a new pseudo random generator of the given size.
   *
   * @param size the size
   * @param source the random source
   * @throws IllegalArgumentException if the size is not positive
   * @throws NullPointerException if the generator is null
   */
  public PseudoRandomGenerator(int size, UniformRandomProvider source)
      throws IllegalArgumentException, NullPointerException {
    if (size < 1) {
      throw new IllegalArgumentException("Sequence must have a positive length");
    }
    if (source == null) {
      throw new NullPointerException("Source generator must not be null");
    }
    sequence = new double[size];
    length = size;
    // Preserve order
    for (int i = 0; i < size; i++) {
      sequence[i] = source.nextDouble();
    }
  }

  @Override
  public void setSeed(int seed) {
    clear();
    position = Math.abs(seed) % length;
  }

  @Override
  public void setSeed(long seed) {
    clear();
    position = Math.abs(Long.hashCode(seed)) % length;
  }

  @Override
  public double nextDouble() {
    final double d = sequence[position++];
    if (position == length) {
      position = 0;
    }
    return d;
  }

  @Override
  public PseudoRandomGenerator clone() {
    try {
      final PseudoRandomGenerator r = (PseudoRandomGenerator) super.clone();
      // In case cloning when being used. This is probably not necessary
      // as the class is not thread safe so cloning should not happen when
      // another thread is using the generator
      if (r.position >= length) {
        r.position = 0;
      }
      return r;
    } catch (final CloneNotSupportedException ex) {
      // This should not happen
      return new PseudoRandomGenerator(sequence, length);
    }
  }

  /**
   * Gets a copy of the sequence of random numbers.
   *
   * @return the sequence
   */
  public double[] getSequence() {
    return sequence.clone();
  }

  /**
   * Gets the length of the sequence of random numbers.
   *
   * @return the length
   */
  public int getLength() {
    return length;
  }

  /**
   * Returns a pseudorandom, uniformly distributed {@code int} value between 0 (inclusive) and the
   * specified value (exclusive), drawn from this random number generator's sequence.
   *
   * <p>The default implementation returns:
   *
   * <pre>
   * <code>(int) (nextDouble() * n</code>
   * </pre>
   *
   * <p>Warning: No check is made that n is positive so use with caution.
   *
   * @param n the bound on the random number to be returned. Must be positive.
   * @return a pseudorandom, uniformly distributed {@code int} value between 0 (inclusive) and n
   *         (exclusive).
   */
  public int nextIntFast(int n) {
    final int result = (int) (nextDouble() * n);
    return result < n ? result : n - 1;
  }

  /**
   * Perform a Fischer-Yates shuffle on the data.
   *
   * @param data the data
   */
  public void shuffle(int[] data) {
    for (int i = data.length; i-- > 1;) {
      final int j = nextIntFast(i + 1);
      final int tmp = data[i];
      data[i] = data[j];
      data[j] = tmp;
    }
  }
}
