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

import java.util.Arrays;

/**
 * Random number generator.
 */
public class Random extends AbstractRandomGenerator {
  private static int IA = 16807;
  private static int IM = 2147483647;
  private static int IQ = 127773;
  private static int IR = 2836;
  private static int NTAB = 32;
  private static int NDIV = (1 + (IM - 1) / NTAB);
  private static float AM = (float) (1.0 / (IM));
  /** The float before 1f. */
  private static float RNMX = Math.nextDown(1f);

  private int idum;
  private int iy = 0;
  private final int[] iv = new int[NTAB];

  /**
   * Default constructor.
   */
  public Random() {
    // Require an integer seed
    final int seed = (int) (System.currentTimeMillis() & 0xffffffff);
    init(seed);
  }

  /**
   * Constructor.
   *
   * @param seed The seed to use for the random number generator
   */
  public Random(int seed) {
    init(seed);
  }

  private void init(int seed) {
    // Clear the Gaussian random deviate cache
    clear();
    idum = (seed > 0) ? -seed : seed;
  }

  /**
   * Returns a random number between 0 (included) and 1 (excluded).
   *
   * @return Random number
   */
  public float next() {
    return next(RNMX);
  }

  /**
   * Returns a random number between 0 (included) and 1 (optionally included).
   *
   * @param includeOne set to true to return a value up to 1 inclusive. The default is 1 exclusive.
   * @return Random number
   */
  public float next(boolean includeOne) {
    return (includeOne) ? next(1f) : next(RNMX);
  }

  /**
   * Returns a random number between 0 (included) and max.
   *
   * @param max the max
   * @return Random number
   */
  private float next(float max) {
    int kk;

    if (idum <= 0 || iy == 0) {
      if (-idum < 1) {
        idum = 1;
      } else {
        idum = -idum;
      }
      for (int j = NTAB + 8; j-- > 0;) {
        kk = idum / IQ;
        idum = IA * (idum - kk * IQ) - IR * kk;
        if (idum < 0) {
          idum += IM;
        }
        if (j < NTAB) {
          iv[j] = idum;
        }
      }
      iy = iv[0];
    }
    kk = idum / IQ;
    idum = IA * (idum - kk * IQ) - IR * kk;
    if (idum < 0) {
      idum += IM;
    }
    int index = iy / NDIV;
    iy = iv[index];
    iv[index] = idum;
    final float temp = AM * iy;
    return (temp > max) ? max : temp;
  }

  /**
   * Perform a Fisher-Yates shuffle on the data.
   *
   * @param data the data
   */
  public void shuffle(double[] data) {
    shuffle(data, this);
  }

  /**
   * Perform a Fisher-Yates shuffle on the data.
   *
   * @param data the data
   */
  public void shuffle(float[] data) {
    shuffle(data, this);
  }

  /**
   * Perform a Fisher-Yates shuffle on the data.
   *
   * @param data the data
   */
  public void shuffle(int[] data) {
    shuffle(data, this);
  }

  /**
   * Perform a Fisher-Yates shuffle on the data.
   *
   * @param data the data
   */
  public void shuffle(Object[] data) {
    shuffle(data, this);
  }

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
   * @return the sample
   */
  public int[] sample(final int k, final int n) {
    return sample(k, n, this);
  }

  /**
   * Sample k values without replacement from the data.
   *
   * <p>Note: Returns an empty array if k is less than 1. Returns a copy of the data if k is greater
   * than data.length.
   *
   * @param k the k
   * @param data the data
   * @return the sample
   */
  public int[] sample(final int k, final int[] data) {
    return sample(k, data.length, this);
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
      for (int i = 0; i < sample.length; i++) {
        data[sample[i]] = -1;
      }
      // Remove from original series
      int count = 0;
      for (int i = 0; i < n; i++) {
        if (data[i] == -1) {
          continue;
        }
        data[count++] = data[i];
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
      for (int i = 0; i < sample.length; i++) {
        data[sample[i]] = -1;
      }
      // Remove from original series
      int count = 0;
      for (int i = 0; i < n; i++) {
        if (data[i] == -1) {
          continue;
        }
        data[count++] = data[i];
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
    for (int i = data.length - 1; k-- > 0; i--) {
      final int j = rng.nextInt(i + 1);
      // In a standard shuffle we swap i and j:
      // int tmp = data[i]
      // data[i] = data[j]
      // data[j] = tmp
      // i then becomes fixed (with a random sample) as we descend the array.
      // This method is modified to write i into j and write what we would put into i into the
      // result array.
      result[k] = data[j];
      data[j] = data[i];
    }
    return result;
  }

  private static int[] inlineSelection(final int[] data, int k, UniformRandomProvider rng) {
    // Do an in-line Fisher-Yates shuffle into a result array
    final int[] result = new int[k];
    for (int i = data.length - 1; k-- > 0; i--) {
      final int j = rng.nextInt(i + 1);
      // In a standard shuffle we swap i and j:
      // int tmp = data[i]
      // data[i] = data[j]
      // data[j] = tmp
      // i then becomes fixed (with a random sample) as we descend the array.
      // This method is modified to write i into j and write what we would put into i into the
      // result array.
      result[k] = data[j];
      data[j] = data[i];
    }
    return result;
  }

  // CHECKSTYLE.ON: ParameterName

  // Apache commons random generator methods

  @Override
  public void setSeed(long seed) {
    init((int) (seed & 0xffffffffL));
  }

  @Override
  public double nextDouble() {
    // 0 to 1 inclusive
    return next(1f);
  }

  @Override
  public float nextFloat() {
    // 0 to 1 inclusive
    return next(1f);
  }

  @Override
  public int nextInt() {
    return (int) ((2d * next(1f) - 1d) * Integer.MAX_VALUE);
  }

  @Override
  public int nextInt(int upper) {
    return (int) (next(RNMX) * upper);
  }

  /**
   * Generate a random integer between lower and upper (end points included).
   *
   * @param lower the lower
   * @param upper the upper
   * @return the integer
   */
  public int nextInt(int lower, int upper) {
    final int max = (upper - lower) + 1;
    if (max <= 0) {
      // The range is too wide to fit in a positive int (larger
      // than 2^31); as it covers more than half the integer range,
      // we use a simple rejection method.
      while (true) {
        final int next = nextInt();
        if (next >= lower && next <= upper) {
          return next;
        }
      }
    }
    // We can shift the range and directly generate a positive int.
    return lower + nextInt(max);
  }
}
