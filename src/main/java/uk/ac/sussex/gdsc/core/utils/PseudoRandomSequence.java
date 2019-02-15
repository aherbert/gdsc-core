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

import java.util.Objects;

/**
 * Contains a set of random numbers that are reused in sequence.
 */
public class PseudoRandomSequence {
  /** The sequence. */
  protected final double[] sequence;

  /** The position. */
  private int position;

  /**
   * Copy constructor.
   *
   * @param source the source
   */
  protected PseudoRandomSequence(PseudoRandomSequence source) {
    this.sequence = source.sequence;
    this.position = source.position;
  }

  /**
   * Instantiates a new pseudo random sequence. The input sequence is cloned.
   *
   * @param sequence the sequence
   * @throws IllegalArgumentException if the sequence is not positive in length
   */
  public PseudoRandomSequence(double[] sequence) {
    checkSize(sequence.length);
    this.sequence = sequence.clone();
  }

  /**
   * Instantiates a new pseudo random sequence of the given size.
   *
   * @param size the size
   * @param source the random source
   * @param scale the scale
   * @throws IllegalArgumentException if the size is not positive
   * @throws NullPointerException if the generator is null
   */
  public PseudoRandomSequence(int size, RandomGenerator source, double scale) {
    checkSize(size);
    Objects.requireNonNull(source, "Source generator must not be null");
    sequence = new double[size];
    // Preserve order
    for (int i = 0; i < size; i++) {
      sequence[i] = source.nextDouble() * scale;
    }
  }

  /**
   * Instantiates a new pseudo random sequence of the given size.
   *
   * @param size the size
   * @param source the random source
   * @param scale the scale
   * @throws IllegalArgumentException if the size is not positive
   * @throws NullPointerException if the generator is null
   */
  public PseudoRandomSequence(int size, UniformRandomProvider source, double scale) {
    checkSize(size);
    Objects.requireNonNull(source, "Source generator must not be null");
    sequence = new double[size];
    // Preserve order
    for (int i = 0; i < size; i++) {
      sequence[i] = source.nextDouble() * scale;
    }
  }

  private static void checkSize(int size) {
    if (size < 1) {
      throw new IllegalArgumentException("Sequence must have a positive length: " + size);
    }
  }

  /**
   * Sets the seed for the sequence.
   *
   * @param seed the new seed
   */
  public void setSeed(long seed) {
    position = Math.abs(Long.hashCode(seed)) % sequence.length;
  }

  /**
   * Get the next double in the sequence.
   *
   * @return the double
   */
  public double nextDouble() {
    int current = position;
    // Wrap
    if (current == sequence.length) {
      current = 0;
    }
    // Set the next position in the sequence
    position = current + 1;
    return sequence[current];
  }

  /**
   * Create a copy.
   *
   * @return the copy
   */
  public PseudoRandomSequence copy() {
    return new PseudoRandomSequence(this);
  }

  /**
   * Gets the sequence of random numbers.
   *
   * @return the sequence
   */
  public double[] getSequence() {
    return sequence.clone();
  }
}
