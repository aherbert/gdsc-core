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

import java.util.function.Supplier;

/**
 * Utility class for splitting KD-trees.
 */
enum SplitStrategy {
  /**
   * Split using the middle of the dimension. Uses a simple strategy of the mean of the minimum and
   * maximum.
   *
   * <p>The split value will be finite and will not be equal to the max limit. This allows using
   * {@code value > splitValue} to partition the data.
   */
  MIDDLE {
    @Override
    double splitValue(double min, double max, Supplier<double[]> values) {
      // Weighted mean to avoid overflow in (min + max) * 0.5
      double splitValue = min * 0.5 + max * 0.5;

      // Never split on infinity or NaN
      if (splitValue == Double.POSITIVE_INFINITY) {
        splitValue = Double.MAX_VALUE;
      } else if (splitValue == Double.NEGATIVE_INFINITY) {
        splitValue = -Double.MAX_VALUE;
      } else if (Double.isNaN(splitValue)) {
        splitValue = 0;
      }

      // Don't let the split value be the same as the upper value as
      // can happen due to rounding errors!
      if (splitValue == max) {
        splitValue = min;
      }
      return splitValue;
    }
  };

  /** No public construction. */
  SplitStrategy() {}

  /**
   * Compute the split value.
   *
   * @param min minimum of the dimension
   * @param max maximum of the dimension
   * @param values supplier of the values
   * @return the split value
   */
  abstract double splitValue(double min, double max, Supplier<double[]> values);
}
