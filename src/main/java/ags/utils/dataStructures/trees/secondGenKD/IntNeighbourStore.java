package ags.utils.dataStructures.trees.secondGenKD;

/**
 * Interface to allow storing neighbours of a location within the KD-tree
 */
public interface IntNeighbourStore
{
	/**
	 * Adds the neighbour to the store.
	 *
	 * @param distance
	 *            the distance to the location
	 * @param neighbour
	 *            the neighbour
	 */
	void add(double distance, int neighbour);

	/**
	 * Adds the neighbour to the store.
	 *
	 * @param index
	 *            the index
	 * @param distance
	 *            the distance to the location
	 * @param neighbour
	 *            the neighbour
	 */
	void add(int index, double distance, int neighbour);
}