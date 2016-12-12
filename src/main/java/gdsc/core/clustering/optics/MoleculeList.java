package gdsc.core.clustering.optics;

/**
 * Used in the algorithms to store molecules in an indexable list
 */
class MoleculeList extends MoleculeArray
{
	MoleculeList(int capacity)
	{
		super(capacity);
	}

	Molecule get(int i)
	{
		return list[i];
	}

	void add(Molecule[] molecules)
	{
		System.arraycopy(molecules, 0, list, size, molecules.length);
		size += molecules.length;
	}
}