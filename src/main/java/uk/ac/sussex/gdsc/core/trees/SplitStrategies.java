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
 * Copyright (C) 2011 - 2021 Alex Herbert
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

/**
 * Utility class for splitting KD-trees.
 *
 * @since 2.0
 */
final class SplitStrategies {

  // This may be modified in the future to an enum implementing an interface.
  // The interface should have arguments for the min, max and some means to obtain the
  // values (e.g. a Supplier<Spliterator.OfDouble>).
  // Currently this class is used by all KdTrees to do the same split.

  /** No public construction. */
  private SplitStrategies() {}

  /**
   * Compute the split value given the range of the dimension. Uses a simple strategy of the mean of
   * the minimum and maximum.
   *
   * <p>The split value will be finite and will not be equal to the max limit. This allows using
   * {@code value > splitValue} to partition the data.
   *
   * @param minLimit the minimum limit
   * @param maxLimit the maximum limit
   * @return the split value
   */
  static double computeSplitValue(double minLimit, double maxLimit) {
    // Weighted mean to avoid overflow in (min + max) * 0.5
    double splitValue = minLimit * 0.5 + maxLimit * 0.5;

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
    if (splitValue == maxLimit) {
      splitValue = minLimit;
    }
    return splitValue;
  }
}
