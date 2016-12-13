package gdsc.core.clustering.optics;

/*----------------------------------------------------------------------------- 
 * GDSC ImageJ Software
 * 
 * Copyright (C) 2016 Alex Herbert
 * Genome Damage and Stability Centre
 * University of Sussex, UK
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *---------------------------------------------------------------------------*/

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