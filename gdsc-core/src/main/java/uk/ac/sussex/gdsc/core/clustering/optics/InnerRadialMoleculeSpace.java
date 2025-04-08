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

package uk.ac.sussex.gdsc.core.clustering.optics;

/**
 * Store molecules in a 2D grid and perform distance computation using cells within the radius from
 * the centre. Forces the use of the internal region of the circle.
 */
class InnerRadialMoleculeSpace extends RadialMoleculeSpace {
  /**
   * Instantiates a new inner radial molecule space.
   *
   * @param opticsManager the optics manager
   * @param generatingDistanceE the generating distance (E)
   */
  InnerRadialMoleculeSpace(OpticsManager opticsManager, float generatingDistanceE) {
    this(opticsManager, generatingDistanceE, 0);
  }

  /**
   * Instantiates a new inner radial molecule space.
   *
   * @param opticsManager the optics manager
   * @param generatingDistanceE the generating distance (E)
   * @param resolution the resolution
   */
  InnerRadialMoleculeSpace(OpticsManager opticsManager, float generatingDistanceE, int resolution) {
    super(opticsManager, generatingDistanceE, resolution, true);
  }
}
