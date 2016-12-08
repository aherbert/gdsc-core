package gdsc.core.clustering;

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

import java.awt.Rectangle;

import gdsc.core.logging.TrackProgress;

/**
 * Store 2D coordinates shifted to the origin for efficient grid processing
 */
public class CoordinateStore
{
	protected TrackProgress tracker = null;
	protected final float[] xcoord, ycoord;
	protected final float minXCoord, minYCoord, maxXCoord, maxYCoord;
	protected final int area;

	/**
	 * Input arrays are modified
	 * 
	 * @param xcoord
	 * @param ycoord
	 * @param bounds
	 * @throws IllegalArgumentException
	 *             if results are null or empty
	 */
	public CoordinateStore(float[] xcoord, float[] ycoord, Rectangle bounds)
	{
		if (xcoord == null || ycoord == null || xcoord.length == 0 || xcoord.length != ycoord.length)
			throw new IllegalArgumentException("Results are null or empty or mismatched in length");

		this.xcoord = xcoord;
		this.ycoord = ycoord;

		// Assign localisations & get min bounds
		float minXCoord = Float.POSITIVE_INFINITY;
		float minYCoord = Float.POSITIVE_INFINITY;
		for (int i = 0; i < xcoord.length; i++)
		{
			if (minXCoord > xcoord[i])
				minXCoord = xcoord[i];
			if (minYCoord > ycoord[i])
				minYCoord = ycoord[i];
		}

		// Round down and shift to origin (so all coords are >=0 for efficient grid allocation)
		final float shiftx = (float) Math.floor(minXCoord);
		final float shifty = (float) Math.floor(minYCoord);

		// Get max bounds
		minXCoord -= shiftx;
		minYCoord -= shifty;
		float maxXCoord = 0;
		float maxYCoord = 0;
		for (int i = 0; i < xcoord.length; i++)
		{
			xcoord[i] -= shiftx;
			ycoord[i] -= shifty;
			if (maxXCoord < xcoord[i])
				maxXCoord = xcoord[i];
			if (maxYCoord < ycoord[i])
				maxYCoord = ycoord[i];
		}

		this.minXCoord = minXCoord;
		this.minYCoord = minYCoord;
		this.maxXCoord = maxXCoord;
		this.maxYCoord = maxYCoord;
		// Store the area of the input results
		area = bounds.width * bounds.height;
	}

	/**
	 * Gets the data in float format.
	 *
	 * @return the data
	 */
	public float[][] getData()
	{
		return new float[][] { xcoord.clone(), ycoord.clone() };
	}

	/**
	 * Gets the data in double format.
	 *
	 * @return the data
	 */
	public double[][] getDoubleData()
	{
		double[] x = new double[xcoord.length];
		double[] y = new double[xcoord.length];
		for (int i = x.length; i-- > 0;)
		{
			x[i] = xcoord[i];
			y[i] = ycoord[i];
		}
		return new double[][] { x, y };
	}

	/**
	 * @return the tracker
	 */
	public TrackProgress getTracker()
	{
		return tracker;
	}

	/**
	 * @param tracker
	 *            the tracker to set
	 */
	public void setTracker(TrackProgress tracker)
	{
		this.tracker = tracker;
	}
}
