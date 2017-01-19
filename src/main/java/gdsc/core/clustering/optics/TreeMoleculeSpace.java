package gdsc.core.clustering.optics;

import ags.utils.dataStructures.trees.secondGenKD.KdTree2D;
import ags.utils.dataStructures.trees.secondGenKD.NeighbourStore;

/**
 * Store molecules in a 2D tree
 */
class TreeMoleculeSpace extends MoleculeSpace
{
	private class MoleculeStore implements NeighbourStore<Molecule>
	{
		public void add(double distance, Molecule m)
		{
			m.setD((float) distance);
			neighbours.add(m);
		}
	}
	
	/**
	 * Used for access to the raw coordinates
	 */
	protected final OPTICSManager opticsManager;

	private KdTree2D<Molecule> tree;
	private final MoleculeStore store;

	TreeMoleculeSpace(OPTICSManager opticsManager, float generatingDistanceE)
	{
		super(opticsManager.getSize(), generatingDistanceE);

		this.opticsManager = opticsManager;
		this.store = new MoleculeStore();
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
		tree = new KdTree2D.SqrEuclid2D<Molecule>();
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
		neighbours.clear();
		tree.findNeighbor(new double[] { object.x, object.y }, e, store);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.core.clustering.optics.OPTICSManager.MoleculeSpace#findNeighboursAndDistances(int,
	 * gdsc.core.clustering.optics.OPTICSManager.Molecule, float)
	 */
	void findNeighboursAndDistances(int minPts, Molecule object, float e)
	{
		neighbours.clear();
		tree.findNeighbor(new double[] { object.x, object.y }, e, store);
	}
}