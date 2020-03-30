/*
 * Copyright 2009 Rednaxela
 *
 * This software is provided 'as-is', without any express or implied warranty. In no event will the
 * authors be held liable for any damages arising from the use of this software.
 *
 * Permission is granted to anyone to use this software for any purpose, including commercial
 * applications, and to alter it and redistribute it freely, subject to the following restrictions:
 *
 * 1. The origin of this software must not be misrepresented; you must not claim that you wrote the
 * original software. If you use this software in a product, an acknowledgment in the product
 * documentation would be appreciated but is not required.
 *
 * 2. This notice may not be removed or altered from any source distribution.
 */

package uk.ac.sussex.gdsc.core.trees;

/**
 * Utility class for splitting KD-trees.
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
