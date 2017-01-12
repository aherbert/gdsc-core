package ags.utils.dataStructures.trees.secondGenKD;

/**
 * Interface to allow storing neighbours of a location within the KD-tree
 */
public interface NeighbourStore<T>
{
	/**
	 * Adds the neighbour to the store.
	 *
	 * @param distance
	 *            the distance to the location
	 * @param neighbour
	 *            the neighbour
	 */
	void add(double distance, T neighbour);
}