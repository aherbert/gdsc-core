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
package gdsc.core.clustering.optics;

import ags.utils.dataStructures.trees.thirdGenKD.DistanceFunction;
import ags.utils.dataStructures.trees.thirdGenKD.KdTree2D;
import ags.utils.dataStructures.trees.thirdGenKD.NearestNeighborIterator;
import ags.utils.dataStructures.trees.thirdGenKD.SquareEuclideanDistanceFunction2D;

/**
 * Store molecules in a 2D tree
 */
class TreeMoleculeSpace2 extends MoleculeSpace
{
	private static final DistanceFunction distanceFunction = new SquareEuclideanDistanceFunction2D();

	/**
	 * Used for access to the raw coordinates
	 */
	protected final OPTICSManager opticsManager;

	private KdTree2D<Molecule> tree;

	TreeMoleculeSpace2(OPTICSManager opticsManager, float generatingDistanceE)
	{
		super(opticsManager.getSize(), generatingDistanceE);

		this.opticsManager = opticsManager;
	}

	// Nothing to add to default toString()
	//	@Override
	//	public String toString()
	//	{
	//		return this.getClass().getSimpleName();
	//	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.core.clustering.optics.MoleculeSpace#generate()
	 */
	Molecule[] generate()
	{
		final float[] xcoord = opticsManager.getXData();
		final float[] ycoord = opticsManager.getYData();

		setOfObjects = new Molecule[xcoord.length];
		tree = new KdTree2D<Molecule>();
		for (int i = 0; i < xcoord.length; i++)
		{
			final float x = xcoord[i];
			final float y = ycoord[i];
			// Build a single linked list
			final Molecule m = new DistanceMolecule(i, x, y);
			setOfObjects[i] = m;
			tree.addPoint(new double[] { x, y }, m);
		}

		return setOfObjects;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.core.clustering.optics.OPTICSManager.MoleculeSpace#findNeighbours(int,
	 * gdsc.core.clustering.optics.OPTICSManager.Molecule, float)
	 */
	void findNeighbours(int minPts, Molecule object, float e)
	{
		NearestNeighborIterator<Molecule> iter = tree.getNearestNeighborIterator(new double[] { object.x, object.y },
				tree.size(), distanceFunction);
		neighbours.clear();
		final double e2 = e;
		while (iter.hasNext())
		{
			Molecule m = iter.next();
			if (iter.distance() <= e2)
				neighbours.add(m);
			else
				break;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.core.clustering.optics.OPTICSManager.MoleculeSpace#findNeighboursAndDistances(int,
	 * gdsc.core.clustering.optics.OPTICSManager.Molecule, float)
	 */
	void findNeighboursAndDistances(int minPts, Molecule object, float e)
	{
		NearestNeighborIterator<Molecule> iter = tree.getNearestNeighborIterator(new double[] { object.x, object.y },
				tree.size(), distanceFunction);
		neighbours.clear();
		while (iter.hasNext())
		{
			Molecule m = iter.next();
			float d = (float) iter.distance();
			if (d <= e)
			{
				m.setD(d);
				neighbours.add(m);
			}
			else
				break;
		}
	}
}
