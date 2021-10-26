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

package uk.ac.sussex.gdsc.core.clustering.optics;

import java.util.function.ToDoubleBiFunction;

/**
 * Provide implementations for distance computation.
 */
enum MoleculeDistanceFunctions implements ToDoubleBiFunction<Molecule, Molecule> {
  /**
   * Compute the squared Euclidean distance in 2-dimensions (XY).
   */
  SQUARED_EUCLIDEAN_2D {
    @Override
    public double applyAsDouble(Molecule t, Molecule u) {
      final double dx = t.x - u.x;
      final double dy = t.y - u.y;
      return dx * dx + dy * dy;
    }
  },
  /**
   * Compute the squared Euclidean distance in 3-dimensions (XYZ).
   */
  SQUARED_EUCLIDEAN_3D {
    @Override
    public double applyAsDouble(Molecule t, Molecule u) {
      final double dx = t.x - u.x;
      final double dy = t.y - u.y;
      final double dz = t.getZ() - u.getZ();
      return dx * dx + dy * dy + dz * dz;
    }
  },
}
