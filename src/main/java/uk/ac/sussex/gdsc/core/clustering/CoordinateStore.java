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
package uk.ac.sussex.gdsc.core.clustering;

import java.awt.Rectangle;

import uk.ac.sussex.gdsc.core.logging.TrackProgress;

/**
 * Store 2D coordinates shifted to the origin for efficient grid processing
 */
public class CoordinateStore implements Cloneable
{
    /** The tracker. */
    protected TrackProgress tracker = null;

    /** The xcoords. */
    protected final float[] xcoord;

    /** The ycoords. */
    protected final float[] ycoord;

    /** The origin for the x coordinate. Add this to x to get the original coordinates. */
    public final float originx;
    /** The origin for the y coordinate. Add this to y to get the original coordinates. */
    public final float originy;

    /** The min X coord. */
    public final float minXCoord;
    /** The min Y coord. */
    public final float minYCoord;
    /** The max X coord. */
    public final float maxXCoord;
    /** The max Y coord. */
    public final float maxYCoord;

    /** The area. This is the product of the constructor bounds width multiplied by the height. */
    public final int area;

    /**
     * Input arrays are modified.
     *
     * @param xcoord
     *            the xcoord
     * @param ycoord
     *            the ycoord
     * @param bounds
     *            the bounds
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
        originx = (float) Math.floor(minXCoord);
        originy = (float) Math.floor(minYCoord);

        // Get max bounds
        minXCoord -= originx;
        minYCoord -= originy;
        float maxXCoord = 0;
        float maxYCoord = 0;
        for (int i = 0; i < xcoord.length; i++)
        {
            xcoord[i] -= originx;
            ycoord[i] -= originy;
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
     * Gets the number of points in the data store.
     *
     * @return the size
     */
    public int getSize()
    {
        return xcoord.length;
    }

    /**
     * Gets the minimum X.
     *
     * @return the minimum X
     */
    public float getMinimumX()
    {
        return minXCoord;
    }

    /**
     * Gets the maximum X.
     *
     * @return the maximum X
     */
    public float getMaximumX()
    {
        return maxXCoord;
    }

    /**
     * Gets the minimum Y.
     *
     * @return the minimum Y
     */
    public float getMinimumY()
    {
        return minYCoord;
    }

    /**
     * Gets the maximum Y.
     *
     * @return the maximum Y
     */
    public float getMaximumY()
    {
        return maxYCoord;
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
        final double[] x = new double[xcoord.length];
        final double[] y = new double[xcoord.length];
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

    /** {@inheritDoc} */
    @Override
    public CoordinateStore clone()
    {
        try
        {
            return (CoordinateStore) super.clone();
        }
        catch (final CloneNotSupportedException e)
        {
            return null;
        }
    }
}
