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

import uk.ac.sussex.gdsc.test.junit5.*;
import uk.ac.sussex.gdsc.test.rng.RngFactory;
import org.junit.jupiter.api.*;

import uk.ac.sussex.gdsc.test.junit5.*;
import uk.ac.sussex.gdsc.test.rng.RngFactory;


/**
 * Used in the DBSCAN/OPTICS algorithms to represent 2D molecules.
 */
class GridMolecule extends DistanceMolecule {
  // Used to construct a single linked list of molecules
  private GridMolecule next = null;

  private final int xBin;
  private final int yBin;

  /**
   * Instantiates a new grid molecule.
   *
   * @param id the id
   * @param x the x
   * @param y the y
   * @param xBin the x bin
   * @param yBin the y bin
   * @param next the next
   */
  GridMolecule(int id, float x, float y, int xBin, int yBin, GridMolecule next) {
    super(id, x, y);
    this.next = next;
    this.xBin = xBin;
    this.yBin = yBin;
  }

  @Override
  public GridMolecule getNext() {
    return next;
  }

  /**
   * Sets the next molecule (used to create a linked-list).
   *
   * @param next the new next
   */
  public void setNext(GridMolecule next) {
    this.next = next;
  }

  @Override
  int getXBin() {
    return xBin;
  }

  @Override
  int getYBin() {
    return yBin;
  }
}
