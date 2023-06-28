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

package uk.ac.sussex.gdsc.core.clustering.optics;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"javadoc"})
class MoleculeDistanceFunctionsTest {
  @Test
  void test2d() {
    final Molecule m1 = new Molecule(0, 1, 2);
    final Molecule m2 = new Molecule(0, 4, 3);
    final double expected = 3 * 3 + 1;
    Assertions.assertEquals(expected,
        MoleculeDistanceFunctions.SQUARED_EUCLIDEAN_2D.applyAsDouble(m1, m2));
    Assertions.assertEquals(expected,
        MoleculeDistanceFunctions.SQUARED_EUCLIDEAN_3D.applyAsDouble(m1, m2));
  }

  @Test
  void test3d() {
    final Molecule m1 = new Molecule3d(0, 1, 2, 3);
    final Molecule m2 = new Molecule3d(0, 6, 5, 4);
    Assertions.assertEquals(5 * 5 + 3 * 3,
        MoleculeDistanceFunctions.SQUARED_EUCLIDEAN_2D.applyAsDouble(m1, m2));
    Assertions.assertEquals(5 * 5 + 3 * 3 + 1,
        MoleculeDistanceFunctions.SQUARED_EUCLIDEAN_3D.applyAsDouble(m1, m2));
  }
}
