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
 * Copyright (C) 2011 - 2023 Alex Herbert
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

package uk.ac.sussex.gdsc.core.match;

import uk.ac.sussex.gdsc.core.utils.ValidationUtils;

/**
 * Calculates the precision and recall for a set of assignments.
 */
public final class AucCalculator {

  /** No public construction. */
  private AucCalculator() {}

  /**
   * Calculates an estimate of the area under the precision-recall curve.
   *
   * <p>Precision and Recall are between 0 and 1. Recall is assumed to be monotonic ascending.
   *
   * @param precision the precision
   * @param recall the recall
   * @return Area under the PR curve
   */
  public static double auc(double[] precision, double[] recall) {
    checkArguments(precision, recall);

    double area = 0.0;

    // Precision at recall 0 is 1 (perfect)
    double previousRecall = 0;
    double previousPrecision = 1;

    for (int k = 0; k < precision.length; k++) {
      final double currentRecall = recall[k];
      final double currentPrecision = precision[k];
      final double delta = currentRecall - previousRecall;
      if (currentPrecision == previousPrecision) {
        // Flat line so add the area
        area += currentPrecision * delta;
      } else {
        // Interpolate using a trapezoid rule:
        // https://en.wikipedia.org/wiki/Trapezoidal_rule
        area += (currentPrecision + previousPrecision) * 0.5 * delta;
      }
      previousRecall = currentRecall;
      previousPrecision = currentPrecision;
    }

    return area;
  }

  /**
   * Check the input arguments are not null and the same length.
   *
   * @param precision the precision
   * @param recall the recall
   */
  private static void checkArguments(double[] precision, double[] recall) {
    ValidationUtils.checkNotNull(precision, "Precision must not be null");
    ValidationUtils.checkNotNull(recall, "Precision must not be null");
    ValidationUtils.checkArgument(precision.length == recall.length,
        "Precision and Recall must be the same length");
  }
}
