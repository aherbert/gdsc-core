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

/**
 * Used in the algorithms to store molecules.
 */
abstract class MoleculeArray
{
	/** The list. */
	final Molecule[] list;
	
	/** The size. */
	int size = 0;

	/**
	 * Instantiates a new molecule array.
	 *
	 * @param capacity
	 *            the capacity
	 */
	MoleculeArray(int capacity)
	{
		list = new Molecule[capacity];
	}

	/**
	 * Adds the molecule.
	 *
	 * @param m
	 *            the molecule
	 */
	void add(Molecule m)
	{
		list[size++] = m;
	}

	/**
	 * Clear.
	 */
	void clear()
	{
		size = 0;
	}
}
