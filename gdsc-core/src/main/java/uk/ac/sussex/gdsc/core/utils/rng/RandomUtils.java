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
 * Copyright (C) 2011 - 2025 Alex Herbert
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

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.sampling.CombinationSampler;
import uk.ac.sussex.gdsc.core.utils.SimpleArrayUtils;

/**
 * Random utilities.
 */
public final class RandomUtils {

  /** No public construction. */
  private RandomUtils() {}

  /**
   * Perform a Fisher-Yates shuffle on the data.
   *
   * @param data the data
   * @param rng the random generator
   */
  public static void shuffle(double[] data, UniformRandomProvider rng) {
    for (int i = data.length; i > 1; i--) {
      SimpleArrayUtils.swap(data, i - 1, rng.nextInt(i));
    }
  }

  /**
   * Perform a Fisher-Yates shuffle on the data.
   *
   * @param data the data
   * @param rng the random generator
   */
  public static void shuffle(float[] data, UniformRandomProvider rng) {
    for (int i = data.length; i > 1; i--) {
      SimpleArrayUtils.swap(data, i - 1, rng.nextInt(i));
    }
  }

  /**
   * Perform a Fisher-Yates shuffle on the data.
   *
   * @param data the data
   * @param rng the random generator
   */
  public static void shuffle(int[] data, UniformRandomProvider rng) {
    for (int i = data.length; i > 1; i--) {
      SimpleArrayUtils.swap(data, i - 1, rng.nextInt(i));
    }
  }

  /**
   * Perform a Fisher-Yates shuffle on the data.
   *
   * @param <T> the element type
   * @param data the data
   * @param rng the random generator
   */
  public static <T> void shuffle(T[] data, UniformRandomProvider rng) {
    for (int i = data.length; i > 1; i--) {
      SimpleArrayUtils.swap(data, i - 1, rng.nextInt(i));
    }
  }

  // It is fine to have parameter names n and k for the common nomenclature of nCk (n choose k)
  // CHECKSTYLE.OFF: ParameterName

  /**
   * Sample k objects without replacement from n objects. This is done using an in-line Fisher-Yates
   * shuffle on an array of length n for the first k target indices.
   *
   * <p>Note: Returns an empty array if n or k are less than 1. Allows {@code k >= n} where an
   * ascending array of size {@code n} is returned.
   *
   * @param k the k
   * @param n the n
   * @param rng the random generator
   * @return the sample
   */
  public static int[] sample(final int k, final int n, UniformRandomProvider rng) {
    // Avoid stupidity
    if (n < 1 || k < 1) {
      return ArrayUtils.EMPTY_INT_ARRAY;
    }
    if (k >= n) {
      return SimpleArrayUtils.natural(n);
    }
    return new CombinationSampler(rng, n, k).sample();
  }

  /**
   * Sample k values without replacement from the data.
   *
   * <p>Note: Returns an empty array if k is less than 1. Returns a copy of the data if k is greater
   * than data.length.
   *
   * @param k the k
   * @param data the data
   * @param rng the random generator
   * @return the sample
   */
  public static int[] sample(final int k, final int[] data, UniformRandomProvider rng) {
    final int[] sample = sample(k, data.length, rng);
    // Convert indices to values
    for (int i = sample.length; i-- > 0;) {
      sample[i] = data[sample[i]];
    }
    return sample;
  }

  // CHECKSTYLE.ON: ParameterName
}
