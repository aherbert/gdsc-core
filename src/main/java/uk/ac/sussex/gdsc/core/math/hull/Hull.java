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
 * Copyright (C) 2011 - 2020 Alex Herbert
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

package uk.ac.sussex.gdsc.core.math.hull;

/**
 * Defines the points of a hull. A hull represents the outer boundary of a collection of points and
 * can be constructed by passing points to a hull builder.
 *
 * <p>Note: Implementing classes for dimensions higher than 2 should add additional methods to
 * define the hull.
 */
public interface Hull {

  /**
   * Define a builder to create a hull.
   */
  interface Builder {
    /**
     * Adds the point.
     *
     * @param point the point
     * @return the builder
     */
    Builder add(double... point);

    /**
     * Clear the points from the builder.
     *
     * @return the builder
     */
    Builder clear();

    /**
     * Builds the hull. The result may be null if the hull cannot be created.
     *
     * @return the hull
     */
    Hull build();
  }

  /**
   * The number of dimensions.
   *
   * @return the dimensions
   */
  int dimensions();

  /**
   * Gets the number of vertices.
   *
   * @return the number of vertices
   */
  int getNumberOfVertices();

  /**
   * Gets the vertices associated with this hull.
   *
   * @return the vertices
   */
  double[][] getVertices();
}
