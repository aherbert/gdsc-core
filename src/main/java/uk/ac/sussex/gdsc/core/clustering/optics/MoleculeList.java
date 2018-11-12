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

package uk.ac.sussex.gdsc.core.clustering.optics;

/**
 * Used in the algorithms to store molecules in a fixed capacity list.
 */
class MoleculeList {
  /** The list. */
  final Molecule[] list;

  /** The size. */
  int size;

  /**
   * Instantiates a new molecule array.
   *
   * @param capacity the capacity
   */
  MoleculeList(int capacity) {
    list = new Molecule[capacity];
  }

  /**
   * Adds the molecule.
   *
   * @param molecule the molecule
   */
  void add(Molecule molecule) {
    list[size++] = molecule;
  }

  /**
   * Adds the molecules.
   *
   * @param molecules the molecules
   */
  void add(Molecule[] molecules) {
    System.arraycopy(molecules, 0, list, size, molecules.length);
    size += molecules.length;
  }

  /**
   * Gets the molecule at the index.
   *
   * @param index the index
   * @return the molecule
   */
  Molecule get(int index) {
    return list[index];
  }

  /**
   * Clear.
   */
  void clear() {
    size = 0;
  }
}
