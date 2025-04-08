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

package uk.ac.sussex.gdsc.core.trees;

import org.apache.commons.rng.UniformRandomProvider;
import uk.ac.sussex.gdsc.core.utils.SimpleArrayUtils;
import uk.ac.sussex.gdsc.core.utils.rng.RandomUtils;

@SuppressWarnings({"javadoc"})
public class KdTreeTestUtils {

  // Trust that the static arrays will not be modified by other classes.

  /** The size for each dimension in the test data hypercube. */
  static final int size = 256;

  /** The number of items to add to the test tree. */
  static final int[] ns = new int[] {100, 200, 400, 2000};

  /** The number of nearest neighbours to test. */
  static final int[] ks = new int[] {2, 4, 8, 16};

  /** The distance ranges to test. */
  static final double[] ranges = new double[] {3, 5};

  /**
   * Creates the data. The data is 2D and uniformly distributed in each axis in the range
   * {@code [0, size)}.
   *
   * @param rng the source of randomness
   * @param size the size
   * @param n the number of points
   * @param allowDuplicates set to true to allow duplicates
   * @return the data
   */
  static double[][] createData(UniformRandomProvider rng, int size, int n,
      boolean allowDuplicates) {
    final double[][] data = new double[n][];
    if (allowDuplicates) {
      final int half = n / 2;
      for (int i = half; i < n; i++) {
        data[i] = new double[] {rng.nextDouble() * size, rng.nextDouble() * size};
      }
      for (int i = 0, j = half; i < half; i++, j++) {
        data[i] = data[j];
      }
    } else {
      final double[] x = SimpleArrayUtils.newArray(n, 0, (double) size / n);
      final double[] y = x.clone();
      RandomUtils.shuffle(x, rng);
      RandomUtils.shuffle(y, rng);
      for (int i = 0; i < n; i++) {
        data[i] = new double[] {x[i], y[i]};
      }
    }
    return data;
  }

  /**
   * Creates the data. The data is 2D and uniformly distributed in each axis in the range
   * {@code [0, size)}.
   *
   * @param rng the source of randomness
   * @param size the size
   * @param n the number of points
   * @param allowDuplicates set to true to allow duplicates
   * @return the data
   */
  static float[][] createFloatData(UniformRandomProvider rng, int size, int n,
      boolean allowDuplicates) {
    final float[][] data = new float[n][];
    if (allowDuplicates) {
      final int half = n / 2;
      for (int i = half; i < n; i++) {
        data[i] = new float[] {rng.nextFloat() * size, rng.nextFloat() * size};
      }
      for (int i = 0, j = half; i < half; i++, j++) {
        data[i] = data[j];
      }
    } else {
      final float[] x = SimpleArrayUtils.newArray(n, 0, (float) size / n);
      final float[] y = x.clone();
      RandomUtils.shuffle(x, rng);
      RandomUtils.shuffle(y, rng);
      for (int i = 0; i < n; i++) {
        data[i] = new float[] {x[i], y[i]};
      }
    }
    return data;
  }
}
