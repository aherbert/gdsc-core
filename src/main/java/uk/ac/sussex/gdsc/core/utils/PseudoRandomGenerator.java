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

import java.util.Objects;

/**
 * Contains a set of random numbers that are reused in sequence.
 */
public class PseudoRandomGenerator extends AbstractRandomGenerator {
  /** The sequence. */
  protected final double[] sequence;

  /** The length. */
  protected final int length;

  /** The position. */
  private int position;

  /**
   * Copy constructor.
   *
   * @param source the source
   */
  protected PseudoRandomGenerator(PseudoRandomGenerator source) {
    this.sequence = source.sequence;
    this.length = source.length;
    this.position = source.position;
  }

  /**
   * Instantiates a new pseudo random generator.
   *
   * <p>The input array is wrapped.
   *
   * @param sequence the sequence (must contains numbers in the interval 0 to 1)
   * @throws IllegalArgumentException if the sequence is not positive in length and contains numbers
   *         outside the interval 0 to 1.
   */
  PseudoRandomGenerator(double[] sequence) {
    this(sequence, sequence.length);
  }

  /**
   * Instantiates a new pseudo random generator.
   *
   * <p>The input array is wrapped.
   *
   * @param sequence the sequence (must contains numbers in the interval 0 to 1)
   * @param length the length (if greater than sequence.length it is set to sequence.length)
   * @throws IllegalArgumentException if the sequence is not positive in length and contains numbers
   *         outside the interval 0 to 1.
   */
  PseudoRandomGenerator(double[] sequence, int length) {
    final int size = Math.min(sequence.length, length);
    checkSize(size);
    for (int i = size; i-- > 0;) {
      if (sequence[i] < 0 || sequence[i] > 1) {
        throw new IllegalArgumentException(
            "Sequence must contain numbers between 0 and 1 inclusive");
      }
    }
    this.sequence = sequence;
    this.length = size;
  }

  /**
   * Instantiates a new pseudo random generator with no checks on the numbers.
   *
   * @param sequence the sequence (must contains numbers in the interval 0 to 1)
   * @param length the length (if greater than sequence.length it is set to length)
   * @param dummy the dummy parameter flag
   * @throws IllegalArgumentException if the size is not positive
   */
  PseudoRandomGenerator(double[] sequence, int length, boolean dummy) {
    checkSize(length);
    this.sequence = Objects.requireNonNull(sequence, "Sequence must not be null");
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
  public PseudoRandomGenerator(int size, RandomGenerator source) {
    checkSize(size);
    Objects.requireNonNull(source, "Source generator must not be null");
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
  public PseudoRandomGenerator(int size, UniformRandomProvider source) {
    checkSize(size);
    Objects.requireNonNull(source, "Source generator must not be null");
    sequence = new double[size];
    length = size;
    // Preserve order
    for (int i = 0; i < size; i++) {
      sequence[i] = source.nextDouble();
    }
  }

  private static void checkSize(int size) {
    if (size < 1) {
      throw new IllegalArgumentException("Sequence must have a positive length: " + size);
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
    int current = position;
    // Wrap
    if (current == length) {
      current = 0;
    }
    // Set the next position in the sequence
    position = current + 1;
    return sequence[current];
  }

  /**
   * Create a copy.
   *
   * <p>The copy reuses the sequence and will begin at the current position. To reset the sequence
   * use {@link #setSeed(int)} using zero.
   *
   * <p>The state of {@link #nextGaussian()} is reset.
   *
   * @return the copy
   */
  public PseudoRandomGenerator copy() {
    return new PseudoRandomGenerator(this);
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
    for (int i = data.length; i > 1; i--) {
      SimpleArrayUtils.swap(data, i - 1, nextIntFast(i));
    }
  }
}
