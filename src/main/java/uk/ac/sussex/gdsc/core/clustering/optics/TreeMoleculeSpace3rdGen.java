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
 * Copyright (C) 2011 - 2019 Alex Herbert
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

import uk.ac.sussex.gdsc.core.ags.utils.data.trees.gen3.KdTree2D;
import uk.ac.sussex.gdsc.core.ags.utils.data.trees.gen3.NearestNeighborIterator;
import uk.ac.sussex.gdsc.core.ags.utils.data.trees.gen3.SquareEuclideanDistanceFunction2D;

/**
 * Store molecules in a 2D tree (using the 3rd generation implementation).
 */
class TreeMoleculeSpace3rdGen extends MoleculeSpace {

  /**
   * Used for access to the raw coordinates.
   */
  protected final OpticsManager opticsManager;

  /** The 3rd generation tree implementation. */
  private KdTree2D<Molecule> tree;

  /**
   * Instantiates a new tree molecule space 2.
   *
   * @param opticsManager the optics manager
   * @param generatingDistanceE the generating distance (E)
   */
  TreeMoleculeSpace3rdGen(OpticsManager opticsManager, float generatingDistanceE) {
    super(opticsManager.getSize(), generatingDistanceE);

    this.opticsManager = opticsManager;
  }

  @Override
  Molecule[] generate() {
    final float[] xcoord = opticsManager.getXData();
    final float[] ycoord = opticsManager.getYData();

    setOfObjects = new Molecule[xcoord.length];
    tree = new KdTree2D<>();
    for (int i = 0; i < xcoord.length; i++) {
      final float x = xcoord[i];
      final float y = ycoord[i];
      // Build a single linked list
      final Molecule m = new DistanceMolecule(i, x, y);
      setOfObjects[i] = m;
      tree.addPoint(new double[] {x, y}, m);
    }

    return setOfObjects;
  }

  @Override
  void findNeighbours(int minPts, Molecule object, float generatingDistanceE) {
    final NearestNeighborIterator<Molecule> iter = tree.getNearestNeighborIterator(
        new double[] {object.x, object.y}, tree.size(), SquareEuclideanDistanceFunction2D.INSTANCE);
    neighbours.clear();
    final double genDistance = generatingDistanceE;
    while (iter.hasNext()) {
      final Molecule m = iter.next();
      if (iter.distance() <= genDistance) {
        neighbours.add(m);
      } else {
        break;
      }
    }
  }

  @Override
  void findNeighboursAndDistances(int minPts, Molecule object, float generatingDistanceE) {
    final NearestNeighborIterator<Molecule> iter = tree.getNearestNeighborIterator(
        new double[] {object.x, object.y}, tree.size(), SquareEuclideanDistanceFunction2D.INSTANCE);
    neighbours.clear();
    while (iter.hasNext()) {
      final Molecule molecule = iter.next();
      final float distance = (float) iter.distance();
      if (distance <= generatingDistanceE) {
        molecule.setD(distance);
        neighbours.add(molecule);
      } else {
        break;
      }
    }
  }
}
