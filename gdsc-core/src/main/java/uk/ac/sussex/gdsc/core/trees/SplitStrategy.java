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
import org.apache.commons.statistics.descriptive.Mean;
import org.apache.commons.statistics.descriptive.Median;

/**
 * Utility class for splitting KD-trees.
 *
 * <p>Implementations ensure the split value will be finite and will not be equal to the max limit.
 * This allows using {@code value > splitValue} to partition the data.
 */
enum SplitStrategy {
  /**
   * Split using the middle of the dimension. Uses the mean of the minimum and maximum.
   */
  MIDDLE {
    @Override
    double value(double min, double max, Supplier<double[]> values) {
      // Weighted mean to avoid overflow in (min + max) * 0.5
      return min * 0.5 + max * 0.5;
    }
  },
  /**
   * Split using the mean of the dimension.
   */
  MEAN {
    @Override
    double value(double min, double max, Supplier<double[]> values) {
      return Mean.of(values.get()).getAsDouble();
    }
  },
  /**
   * Split using the median of the dimension.
   */
  MEDIAN {
    @Override
    double value(double min, double max, Supplier<double[]> values) {
      return Median.withDefaults().evaluate(values.get());
    }
  };

  /** No public construction. */
  SplitStrategy() {}

  /**
   * Compute the split value. Ensure the value is finite and not equal to the max limit.
   *
   * @param min minimum of the dimension
   * @param max maximum of the dimension
   * @param values supplier of the values
   * @return the split value
   */
  double splitValue(double min, double max, Supplier<double[]> values) {
    double splitValue = value(min, max, values);

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

  /**
   * Compute the split value.
   *
   * @param min minimum of the dimension
   * @param max maximum of the dimension
   * @param values supplier of the values
   * @return the split value
   */
  abstract double value(double min, double max, Supplier<double[]> values);
}
