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
			final Molecule m = new Molecule(i, x, y, 0, 0, null);
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
				m.d = d;
				neighbours.add(m);
			}
			else
				break;
		}
	}
}