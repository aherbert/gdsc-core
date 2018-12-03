/*
 * Copyright 2009 Rednaxela
 *
 * Modifications to the code have been made by Alex Herbert for a smaller memory footprint and
 * optimised 2D processing for use with image data as part of the Genome Damage and Stability Centre
 * ImageJ Core Package.
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

package uk.ac.sussex.gdsc.core.ags.utils.data;

/**
 * Utility class for distance computation.
 *
 * @author Alex Herbert
 */
public final class DistanceUtils {

  /** No public construction. */
  private DistanceUtils() {}

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
  public static float getDistanceOutsideRange(float value, float min, float max) {
    if (value > max) {
      return value - max;
    }
    return (value < min) ? min - value : 0;
  }

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
  public static double getDistanceOutsideRange(double value, double min, double max) {
    if (value > max) {
      return value - max;
    }
    return (value < min) ? min - value : 0;
  }
}
