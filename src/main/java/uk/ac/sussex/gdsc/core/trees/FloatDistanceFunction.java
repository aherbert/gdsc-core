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
 * Interface for a distance between a point and either a point or a hyperrectangle.
 *
 * @see <A href="https://en.wikipedia.org/wiki/Hyperrectangle">Hyperrectangle</a>
 * @since 2.0
 */
public interface FloatDistanceFunction {
  /**
   * Get the distance between the points.
   *
   * @param p1 point 1
   * @param p2 point 2
   * @return the distance
   */
  double distance(double[] p1, float[] p2);

  /**
   * Gets the distance between the point and the n-dimension hyperrectangle.
   *
   * @param point the point
   * @param min the min of the hyperrectangle
   * @param max the max of the hyperrectangle
   * @return the distance
   */
  double distanceToRectangle(double[] point, float[] min, float[] max);
}
