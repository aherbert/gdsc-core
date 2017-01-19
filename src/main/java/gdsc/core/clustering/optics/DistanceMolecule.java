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
class DistanceMolecule extends Molecule
{
	/**
	 * Working distance to current centre object
	 */
	private float d;

	DistanceMolecule(int id, float x, float y)
	{
		super(id, x, y);
	}
	
	float getD()
	{
		return d;
	}

	void setD(float d)
	{
		this.d = d;
	}
}