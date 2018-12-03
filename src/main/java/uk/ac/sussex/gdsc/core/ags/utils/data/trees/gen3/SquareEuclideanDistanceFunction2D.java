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

package uk.ac.sussex.gdsc.core.ags.utils.data.trees.gen3;

import uk.ac.sussex.gdsc.core.ags.utils.data.DistanceUtils;

/**
 * Optimised distance function for 2D data.
 *
 * @author Alex Herbert
 */
public final class SquareEuclideanDistanceFunction2D implements DistanceFunction {

  /** The instance. */
  public static final SquareEuclideanDistanceFunction2D INSTANCE =
      new SquareEuclideanDistanceFunction2D();

  /** No public construction. */
  private SquareEuclideanDistanceFunction2D() {}

  @Override
  public double distance(double[] p1, double[] p2) {
    final double dx = p1[0] - p2[0];
    final double dy = p1[1] - p2[1];
    return dx * dx + dy * dy;
  }

  @Override
  public double distanceToRect(double[] point, double[] min, double[] max) {
    final double dx = DistanceUtils.getDistanceOutsideRange(point[0], min[0], max[0]);
    final double dy = DistanceUtils.getDistanceOutsideRange(point[1], min[1], max[1]);
    return dx * dx + dy * dy;
  }
}
