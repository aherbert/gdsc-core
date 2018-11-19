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

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.rng.UniformRandomProvider;

import java.util.Arrays;

/**
 * Random number generator.
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
  public static void shuffle(double[] data, RandomGenerator rng) {
    for (int i = data.length; i-- > 1;) {
      final int j = rng.nextInt(i + 1);
      final double tmp = data[i];
      data[i] = data[j];
      data[j] = tmp;
    }
  }

  /**
   * Perform a Fisher-Yates shuffle on the data.
   *
   * @param data the data
   * @param rng the random generator
   */
  public static void shuffle(float[] data, RandomGenerator rng) {
    for (int i = data.length; i-- > 1;) {
      final int j = rng.nextInt(i + 1);
      final float tmp = data[i];
      data[i] = data[j];
      data[j] = tmp;
    }
  }

  /**
   * Perform a Fisher-Yates shuffle on the data.
   *
   * @param data the data
   * @param rng the random generator
   */
  public static void shuffle(int[] data, RandomGenerator rng) {
    for (int i = data.length; i-- > 1;) {
      final int j = rng.nextInt(i + 1);
      final int tmp = data[i];
      data[i] = data[j];
      data[j] = tmp;
    }
  }

  /**
   * Perform a Fisher-Yates shuffle on the data.
   *
   * @param data the data
   * @param rng the random generator
   */
  public static void shuffle(Object[] data, RandomGenerator rng) {
    for (int i = data.length; i-- > 1;) {
      final int j = rng.nextInt(i + 1);
      final Object tmp = data[i];
      data[i] = data[j];
      data[j] = tmp;
    }
  }

  /**
   * Perform a Fisher-Yates shuffle on the data.
   *
   * @param data the data
   * @param rng the random generator
   */
  public static void shuffle(double[] data, UniformRandomProvider rng) {
    for (int i = data.length; i-- > 1;) {
      final int j = rng.nextInt(i + 1);
      final double tmp = data[i];
      data[i] = data[j];
      data[j] = tmp;
    }
  }

  /**
   * Perform a Fisher-Yates shuffle on the data.
   *
   * @param data the data
   * @param rng the random generator
   */
  public static void shuffle(float[] data, UniformRandomProvider rng) {
    for (int i = data.length; i-- > 1;) {
      final int j = rng.nextInt(i + 1);
      final float tmp = data[i];
      data[i] = data[j];
      data[j] = tmp;
    }
  }

  /**
   * Perform a Fisher-Yates shuffle on the data.
   *
   * @param data the data
   * @param rng the random generator
   */
  public static void shuffle(int[] data, UniformRandomProvider rng) {
    for (int i = data.length; i-- > 1;) {
      final int j = rng.nextInt(i + 1);
      final int tmp = data[i];
      data[i] = data[j];
      data[j] = tmp;
    }
  }

  /**
   * Perform a Fisher-Yates shuffle on the data.
   *
   * @param data the data
   * @param rng the random generator
   */
  public static void shuffle(Object[] data, UniformRandomProvider rng) {
    for (int i = data.length; i-- > 1;) {
      final int j = rng.nextInt(i + 1);
      final Object tmp = data[i];
      data[i] = data[j];
      data[j] = tmp;
    }
  }

  // It is fine to have parameter names n and k for the common nomenclature of nCk (n choose k)
  // CHECKSTYLE.OFF: ParameterName

  /**
   * Sample k objects without replacement from n objects. This is done using an in-line Fisher-Yates
   * shuffle on an array of length n for the first k target indices.
   *
   * <p>Note: Returns an empty array if n or k are less than 1. Returns an ascending array of
   * indices if k is equal or bigger than n.
   *
   * @param k the k
   * @param n the n
   * @param rng the random generator
   * @return the sample
   */
  public static int[] sample(final int k, final int n, RandomGenerator rng) {
    // Avoid stupidity
    if (n < 1 || k < 1) {
      return new int[0];
    }

    // Create a range of data to sample
    final int[] data = new int[n];
    for (int i = 1; i < n; i++) {
      data[i] = i;
    }

    if (k >= n) {
      // No sub-sample needed
      return data;
    }

    // If k>n/2 then we can sample (n-k) and then construct the result
    // by removing the selection from the original range.
    if (k > n / 2) {
      final int[] sample = inlineSelection(data.clone(), n - k, rng);
      // Flag for removal
      for (final int value : sample) {
        data[value] = -1;
      }
      // Remove from original series
      int count = 0;
      for (final int value : data) {
        if (value == -1) {
          continue;
        }
        data[count++] = value;
      }
      return Arrays.copyOf(data, count);
    }

    return inlineSelection(data, k, rng);
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
  public static int[] sample(final int k, final int[] data, RandomGenerator rng) {
    final int[] sample = sample(k, data.length, rng);
    // Convert indices to values
    for (int i = sample.length; i-- > 0;) {
      sample[i] = data[sample[i]];
    }
    return sample;
  }

  /**
   * Sample k objects without replacement from n objects. This is done using an in-line Fisher-Yates
   * shuffle on an array of length n for the first k target indices.
   *
   * <p>Note: Returns an empty array if n or k are less than 1. Returns an ascending array of
   * indices if k is equal or bigger than n.
   *
   * @param k the k
   * @param n the n
   * @param rng the random generator
   * @return the sample
   */
  public static int[] sample(final int k, final int n, UniformRandomProvider rng) {
    // Avoid stupidity
    if (n < 1 || k < 1) {
      return new int[0];
    }

    // Create a range of data to sample
    final int[] data = new int[n];
    for (int i = 1; i < n; i++) {
      data[i] = i;
    }

    if (k >= n) {
      // No sub-sample needed
      return data;
    }

    // If k>n/2 then we can sample (n-k) and then construct the result
    // by removing the selection from the original range.
    if (k > n / 2) {
      final int[] sample = inlineSelection(data.clone(), n - k, rng);
      // Flag for removal
      for (final int value : sample) {
        data[value] = -1;
      }
      // Remove from original series
      int count = 0;
      for (final int value : data) {
        if (value == -1) {
          continue;
        }
        data[count++] = value;
      }
      return Arrays.copyOf(data, count);
    }

    return inlineSelection(data, k, rng);
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

  private static int[] inlineSelection(final int[] data, int k, RandomGenerator rng) {
    // Do an in-line Fisher-Yates shuffle into a result array
    final int[] result = new int[k];
    for (int i = data.length - 1, count = 0; count < k; i--, count++) {
      final int j = rng.nextInt(i + 1);
      // In a standard shuffle we swap i and j:
      // int tmp = data[i]
      // data[i] = data[j]
      // data[j] = tmp
      // i then becomes fixed (with a random sample) as we descend the array.
      // This method is modified to write i into j and write what we would put into i into the
      // result array.
      result[count] = data[j];
      data[j] = data[i];
    }
    return result;
  }

  private static int[] inlineSelection(final int[] data, int k, UniformRandomProvider rng) {
    // Do an in-line Fisher-Yates shuffle into a result array
    final int[] result = new int[k];
    for (int i = data.length - 1, count = 0; count < k; i--, count++) {
      final int j = rng.nextInt(i + 1);
      // In a standard shuffle we swap i and j:
      // int tmp = data[i]
      // data[i] = data[j]
      // data[j] = tmp
      // i then becomes fixed (with a random sample) as we descend the array.
      // This method is modified to write i into j and write what we would put into i into the
      // result array.
      result[count] = data[j];
      data[j] = data[i];
    }
    return result;
  }

  // CHECKSTYLE.ON: ParameterName
}
