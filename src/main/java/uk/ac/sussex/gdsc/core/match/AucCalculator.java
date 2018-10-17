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

package uk.ac.sussex.gdsc.core.match;

/**
 * Calculates the precision and recall for a set of assignments.
 */
public final class AucCalculator {

  private AucCalculator() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Calculates an estimate of the area under the precision-recall curve.
   *
   * <p>The estimate is computed using integration above the recall limit. Below the limit a simple
   * linear interpolation is used from the given point to precision 1 at recall 0. This avoids noise
   * in the lower recall section of the curve.
   *
   * <p>If no recall values are above the limit then the full integration is performed.
   *
   * @param precision the precision
   * @param recall the recall
   * @param recallLimit Set to 0 to compute the full area.
   * @return Area under the PR curve
   */
  public static double auc(double[] precision, double[] recall, double recallLimit) {
    if (precision == null || recall == null) {
      return 0;
    }

    double area = 0.0;
    int index;

    if (recallLimit > 0) {
      // Move from high to low recall and find the first point below the limit
      index = recall.length - 1;
      while (index > 0 && recall[index] > recallLimit) {
        index--;
      }

      if (index > 0) {
        // Find the first point where precision was not 1
        int kk = 0;
        while (precision[kk + 1] == 1) {
          kk++;
        }

        // Full precision of 1 up to point kk
        area += (recall[kk] - recall[0]);

        // Interpolate from precision at kk to k
        area += (precision[index] + precision[kk]) * 0.5 * (recall[index] - recall[kk]);

        // Increment to start the remaining integral
        index++;
      }
    } else {
      // Complete integration from start
      index = 0;
    }

    // Integrate the rest
    double prevR = 0;
    double prevP = 1;
    if (recall[0] == 0) {
      index++;
    }

    for (; index < precision.length; index++) {
      final double delta = recall[index] - prevR;
      if (precision[index] == prevP) {
        area += prevP * delta;
      } else {
        // Interpolate
        area += (precision[index] + prevP) * 0.5 * delta;
      }
      prevR = recall[index];
      prevP = precision[index];
    }
    return area;
  }

  /**
   * Calculates an estimate of the area under the precision-recall curve.
   *
   * <p>Assumes the first values in the two arrays are precision 1 at recall 0.
   *
   * @param precision the precision
   * @param recall the recall
   * @return Area under the PR curve
   */
  public static double auc(double[] precision, double[] recall) {
    double area = 0.0;

    double prevR = 0;
    double prevP = 1;

    for (int k = 1; k < precision.length; k++) {
      final double delta = recall[k] - prevR;
      if (precision[k] == prevP) {
        area += prevP * delta;
      } else {
        // Interpolate
        area += (precision[k] + prevP) * 0.5 * delta;
      }
      prevR = recall[k];
      prevP = precision[k];
    }
    return area;
  }
}
