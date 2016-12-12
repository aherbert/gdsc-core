package gdsc.core.clustering.optics;

/**
 * Used in the algorithms to store molecules
 */
abstract class MoleculeArray
{
	final Molecule[] list;
	int size = 0;

	MoleculeArray(int capacity)
	{
		list = new Molecule[capacity];
	}

	void add(Molecule m)
	{
		list[size++] = m;
	}

	void clear()
	{
		size = 0;
	}
}