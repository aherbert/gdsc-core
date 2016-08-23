package gdsc.core.utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import gdsc.core.ij.Utils;

/*----------------------------------------------------------------------------- 
 * GDSC SMLM Software
 * 
 * Copyright (C) 2013 Alex Herbert
 * Genome Damage and Stability Centre
 * University of Sussex, UK
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *---------------------------------------------------------------------------*/

/**
 * Simple Input/Output class
 */
public class IO
{
	/**
	 * Save an array to file
	 * 
	 * @param header
	 *            The header
	 * @param data
	 *            The data
	 * @param filename
	 *            The filename
	 * @return true if all OK, false if an error occurred
	 */
	public static boolean save(String header, double[] data, String filename)
	{
		boolean ok = true;
		BufferedWriter file = null;
		try
		{
			file = new BufferedWriter(new FileWriter(filename));
			if (!Utils.isNullOrEmpty(header))
			{
				file.write(header);
				file.newLine();
			}
			if (data != null)
			{
				for (double d : data)
				{
					file.write(Double.toString(d));
					file.newLine();
				}
			}
		}
		catch (IOException e)
		{
			ok = false;
		}
		finally
		{
			if (file != null)
			{
				try
				{
					file.close();
				}
				catch (IOException e)
				{
				}
			}
		}
		return ok;
	}

	/**
	 * Save an array to file
	 * 
	 * @param data
	 *            The data
	 * @param filename
	 *            The filename
	 * @return true if all OK, false if an error occurred
	 */
	public static boolean save(double[] data, String filename)
	{
		return save(null, data, filename);
	}
}
