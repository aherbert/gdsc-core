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

package uk.ac.sussex.gdsc.core.clustering.optics;

import java.util.function.IntFunction;
import uk.ac.sussex.gdsc.core.trees.FloatDistanceFunction;
import uk.ac.sussex.gdsc.core.trees.FloatDistanceFunctions;
import uk.ac.sussex.gdsc.core.trees.KdTrees;
import uk.ac.sussex.gdsc.core.trees.ObjFloatKdTree;

/**
 * Store molecules in a KD tree using float data.
 */
class FloatTreeMoleculeSpace extends MoleculeSpace {

  /**
   * Used for access to the raw coordinates.
   */
  private final OpticsManager opticsManager;

  /** The KD tree implementation. */
  private ObjFloatKdTree<Molecule> tree;

  /** The distance function. */
  private FloatDistanceFunction distanceFunction;

  /**
   * Instantiates a new tree molecule space.
   *
   * @param opticsManager the optics manager
   * @param generatingDistanceE the generating distance (E)
   */
  FloatTreeMoleculeSpace(OpticsManager opticsManager, float generatingDistanceE) {
    super(opticsManager.getSize(), generatingDistanceE);

    this.opticsManager = opticsManager;
  }

  @Override
  Molecule[] generate() {
    final float[] xcoord = opticsManager.getXData();
    final float[] ycoord = opticsManager.getYData();

    IntFunction<Molecule> factory;
    if (opticsManager.is3d()) {
      final float[] zcoord = opticsManager.getZData();
      tree = KdTrees.newObjFloatKdTree(3);
      distanceFunction = FloatDistanceFunctions.SQUARED_EUCLIDEAN_3D;
      factory = i -> {
        final float x = xcoord[i];
        final float y = ycoord[i];
        final float z = zcoord[i];
        final Molecule m = new Molecule3d(i, x, y, z);
        setOfObjects[i] = m;
        tree.addPoint(new float[] {x, y, z}, m);
        return m;
      };
    } else {
      tree = KdTrees.newObjFloatKdTree(2);
      distanceFunction = FloatDistanceFunctions.SQUARED_EUCLIDEAN_2D;
      factory = i -> {
        final float x = xcoord[i];
        final float y = ycoord[i];
        final Molecule m = new Molecule(i, x, y);
        setOfObjects[i] = m;
        tree.addPoint(new float[] {x, y}, m);
        return m;
      };
    }

    setOfObjects = new Molecule[xcoord.length];
    for (int i = 0; i < xcoord.length; i++) {
      setOfObjects[i] = factory.apply(i);
    }

    return setOfObjects;
  }

  @Override
  void findNeighbours(int minPts, Molecule object, float generatingDistanceE) {
    neighbours.clear();
    // Empty z coordinate will be ignored in 2D
    tree.findNeighbours(new double[] {object.x, object.y, object.getZ()}, generatingDistanceE,
        distanceFunction, (molecule, distance) -> {
          molecule.setD((float) distance);
          neighbours.add(molecule);
        });
  }

  @Override
  void findNeighboursAndDistances(int minPts, Molecule object, float generatingDistanceE) {
    findNeighbours(minPts, object, generatingDistanceE);
  }
}
