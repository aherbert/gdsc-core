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

import uk.ac.sussex.gdsc.core.ags.utils.data.trees.gen2.KdTree2D;
import uk.ac.sussex.gdsc.core.ags.utils.data.trees.gen2.NeighbourStore;

/**
 * Store molecules in a 2D tree (using the 2nd generation implementation).
 */
class TreeMoleculeSpace2ndGen extends MoleculeSpace {

  /**
   * Used for access to the raw coordinates.
   */
  protected final OpticsManager opticsManager;

  /** The 2nd generation tree implementation. */
  private KdTree2D<Molecule> tree;
  /** The store. */
  private final MoleculeStore store;

  /**
   * A store for the nearest neighbour molecules.
   */
  private class MoleculeStore implements NeighbourStore<Molecule> {
    @Override
    public void add(double distance, Molecule molecule) {
      molecule.setD((float) distance);
      neighbours.add(molecule);
    }
  }

  /**
   * Instantiates a new tree molecule space.
   *
   * @param opticsManager the optics manager
   * @param generatingDistanceE the generating distance (E)
   */
  TreeMoleculeSpace2ndGen(OpticsManager opticsManager, float generatingDistanceE) {
    super(opticsManager.getSize(), generatingDistanceE);

    this.opticsManager = opticsManager;
    this.store = new MoleculeStore();
  }

  @Override
  Molecule[] generate() {
    final float[] xcoord = opticsManager.getXData();
    final float[] ycoord = opticsManager.getYData();

    setOfObjects = new Molecule[xcoord.length];
    tree = new KdTree2D.SqrEuclid2D<>();
    for (int i = 0; i < xcoord.length; i++) {
      final float x = xcoord[i];
      final float y = ycoord[i];
      final Molecule m = new DistanceMolecule(i, x, y);
      setOfObjects[i] = m;
      tree.addPoint(new double[] {x, y}, m);
    }

    return setOfObjects;
  }

  @Override
  void findNeighbours(int minPts, Molecule object, float generatingDistanceE) {
    neighbours.clear();
    tree.findNeighbor(new double[] {object.x, object.y}, generatingDistanceE, store);
  }

  @Override
  void findNeighboursAndDistances(int minPts, Molecule object, float generatingDistanceE) {
    findNeighbours(minPts, object, generatingDistanceE);
  }
}
