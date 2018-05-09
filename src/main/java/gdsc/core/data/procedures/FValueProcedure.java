package gdsc.core.data.procedures;

/*----------------------------------------------------------------------------- 
 * GDSC Plugins for ImageJ
 * 
 * Copyright (C) 2018 Alex Herbert
 * Genome Damage and Stability Centre
 * University of Sussex, UK
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *---------------------------------------------------------------------------*/

/**
 * Interface for accessing float values
 */
public interface FValueProcedure
{
	/**
	 * Executes this procedure.
	 *
	 * @param value
	 *            the pixel value
	 */
	void execute(float value);
}