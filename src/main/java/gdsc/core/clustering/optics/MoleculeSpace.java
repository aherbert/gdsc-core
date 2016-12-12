package gdsc.core.clustering.optics;

/**
 * Used in the OPTICS/DBSCAN algorithms
 */
abstract class MoleculeSpace
{
	final float generatingDistanceE;
	Molecule[] setOfObjects;
	final int size;
	// Working storage for find neighbours
	final MoleculeList neighbours;

	/**
	 * Instantiates a new molecule space.
	 *
	 * @param size
	 *            the size
	 * @param generatingDistanceE
	 *            the generating distance E
	 */
	MoleculeSpace(int size, float generatingDistanceE)
	{
		this.generatingDistanceE = generatingDistanceE;
		this.size = size;
		neighbours = new MoleculeList(size);
	}

	/**
	 * Generate the molecule space. Return the list of molecules that will be processed.
	 *
	 * @return the molecule list
	 */
	abstract Molecule[] generate();

	/**
	 * Reset all the molecules for fresh processing.
	 */
	void reset()
	{
		for (int i = setOfObjects.length; i-- > 0;)
			setOfObjects[i].reset();
	}

	/**
	 * Find neighbours closer than the generating distance. The neighbours are written to the working memory store.
	 * <p>
	 * If the number of points is definitely below the minimum number of points then no distances are computed (to
	 * save time).
	 * <p>
	 * The neighbours includes the actual point in the list of neighbours (where the distance would be 0).
	 *
	 * @param minPts
	 *            the min points
	 * @param object
	 *            the object
	 * @param e
	 *            the generating distance
	 */
	abstract void findNeighbours(int minPts, Molecule object, float e);

	/**
	 * Find neighbours closer than the generating distance. The neighbours are written to the working memory store.
	 * The distances are stored in the objects encountered.
	 * <p>
	 * If the number of points is definitely below the minimum number of points then no distances are computed (to
	 * save time). Objects are ranked by distance and distances for objects below the min points may not be computed (in
	 * this case they can be set to zero).
	 * <p>
	 * The neighbours includes the actual point in the list of neighbours (where the distance would be 0).
	 *
	 * @param minPts
	 *            the min points
	 * @param object
	 *            the object
	 * @param e
	 *            the generating distance
	 */
	abstract void findNeighboursAndDistances(int minPts, Molecule object, float e);
}