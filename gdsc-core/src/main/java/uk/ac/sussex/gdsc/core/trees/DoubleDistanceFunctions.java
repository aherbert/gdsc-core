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
 * Copyright (C) 2011 - 2022 Alex Herbert
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
 * Provide implementations for distance computation.
 */
public enum DoubleDistanceFunctions implements DoubleDistanceFunction {
  /**
   * Compute the squared Euclidean distance in N-dimensions.
   */
  SQUARED_EUCLIDEAN_ND {
    @Override
    public double distance(double[] p1, double[] p2) {
      double distance = 0;
      for (int i = 0; i < p1.length; i++) {
        final double d = p1[i] - p2[i];
        distance += d * d;
      }
      return distance;
    }

    @Override
    public double distanceToRectangle(double[] point, double[] min, double[] max) {
      double distance = 0;
      for (int i = 0; i < point.length; i++) {
        final double d = getDistanceOutsideRange(point[i], min[i], max[i]);
        distance += d * d;
      }
      return distance;
    }
  },
  /**
   * Compute the squared Euclidean distance in 3-dimensions.
   */
  SQUARED_EUCLIDEAN_3D {
    @Override
    public double distance(double[] p1, double[] p2) {
      final double dx = p1[0] - p2[0];
      final double dy = p1[1] - p2[1];
      final double dz = p1[2] - p2[2];
      return dx * dx + dy * dy + dz * dz;
    }

    @Override
    public double distanceToRectangle(double[] point, double[] min, double[] max) {
      final double dx = getDistanceOutsideRange(point[0], min[0], max[0]);
      final double dy = getDistanceOutsideRange(point[1], min[1], max[1]);
      final double dz = getDistanceOutsideRange(point[2], min[2], max[2]);
      return dx * dx + dy * dy + dz * dz;
    }
  },
  /**
   * Compute the squared Euclidean distance in 2-dimensions.
   */
  SQUARED_EUCLIDEAN_2D {
    @Override
    public double distance(double[] p1, double[] p2) {
      final double dx = p1[0] - p2[0];
      final double dy = p1[1] - p2[1];
      return dx * dx + dy * dy;
    }

    @Override
    public double distanceToRectangle(double[] point, double[] min, double[] max) {
      final double dx = getDistanceOutsideRange(point[0], min[0], max[0]);
      final double dy = getDistanceOutsideRange(point[1], min[1], max[1]);
      return dx * dx + dy * dy;
    }
  };

  /**
   * Gets the distance that the value is outside the min - max range. Return 0 if inside the range.
   *
   * <p>This does not work for NaN values.
   *
   * @param value the value
   * @param min the min
   * @param max the max
   * @return the distance
   */
  static double getDistanceOutsideRange(double value, double min, double max) {
    if (value > max) {
      return value - max;
    }
    return (value < min) ? min - value : 0;
  }

  /**
   * Return a squared Euclidean distance function for the specified number of dimensions.
   *
   * @param dimensions the dimensions
   * @return the distance function
   */
  public static DoubleDistanceFunction squaredEuclidean(int dimensions) {
    if (dimensions == 2) {
      return SQUARED_EUCLIDEAN_2D;
    }
    if (dimensions == 3) {
      return SQUARED_EUCLIDEAN_3D;
    }
    return SQUARED_EUCLIDEAN_ND;
  }
}
