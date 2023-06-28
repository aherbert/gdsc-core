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

/**
 * Used in the DBSCAN/OPTICS algorithms to represent 2D molecules on a grid.
 */
class GridMolecule extends Molecule {
  /** The next molecule in a single linked list of molecules. */
  private final GridMolecule next;
  /** The x-bin. */
  private final int xbin;
  /** The y-bin. */
  private final int ybin;

  /**
   * Instantiates a new grid molecule.
   *
   * @param id the id
   * @param x the x
   * @param y the y
   * @param xbin the x bin
   * @param ybin the y bin
   * @param next the next
   */
  GridMolecule(int id, float x, float y, int xbin, int ybin, GridMolecule next) {
    super(id, x, y);
    this.next = next;
    this.xbin = xbin;
    this.ybin = ybin;
  }

  /**
   * Gets the next molecule.
   *
   * @return the next molecule
   */
  GridMolecule getNext() {
    return next;
  }

  /**
   * Gets the x bin.
   *
   * @return the x bin
   */
  int getXBin() {
    return xbin;
  }

  /**
   * Gets the y bin.
   *
   * @return the y bin
   */
  int getYBin() {
    return ybin;
  }
}
