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
 * Used in the DBSCAN/OPTICS algorithms to represent 2D molecules.
 */
class GridMolecule extends DistanceMolecule
{
	// Used to construct a single linked list of molecules
	private GridMolecule next = null;

	private final int xBin;
	private final int yBin;

	GridMolecule(int id, float x, float y, int xBin, int yBin, GridMolecule next)
	{
		super(id, x, y);
		this.next = next;
		this.xBin = xBin;
		this.yBin = yBin;
	}

	public GridMolecule getNext()
	{
		return next;
	}

	public void setNext(GridMolecule next)
	{
		this.next = next;
	}

	int getXBin()
	{
		return xBin;
	}

	int getYBin()
	{
		return yBin;
	}
}