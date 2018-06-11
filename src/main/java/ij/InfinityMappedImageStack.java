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
package ij;


import java.awt.image.ColorModel;

import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.InfinityMappedFloatProcessor;

/**
 * Extends the ImageJ ImageStack class to support an inifnity mapped float processor for float data.
 * 
 * @author Alex Herbert
 */
public class InfinityMappedImageStack extends ImageStack
{
	private boolean mapPositiveInfinity = false;

	/**
	 * Checks if positive infinity is mapped to zero.
	 *
	 * @return true, if positive infinity is mapped to zero
	 */
	public boolean isMapPositiveInfinity()
	{
		return mapPositiveInfinity;
	}

	/**
	 * Set to true to map positive infinity to zero.
	 *
	 * @param mapPositiveInfinity
	 *            the new map positive infinity flag
	 */
	public void setMapPositiveInfinity(boolean mapPositiveInfinity)
	{
		this.mapPositiveInfinity = mapPositiveInfinity;
	}

	/** Default constructor. */
	public InfinityMappedImageStack()
	{
	}

	/** Creates a new, empty image stack. */
	public InfinityMappedImageStack(int width, int height)
	{
		this(width, height, null);
	}

	/**
	 * Creates a new, empty image stack with a capacity of 'size'. All
	 * 'size' slices and labels of this image stack are initially null.
	 */
	public InfinityMappedImageStack(int width, int height, int size)
	{
		super(width, height, size);
	}

	/** Creates a new, empty image stack using the specified color model. */
	public InfinityMappedImageStack(int width, int height, ColorModel cm)
	{
		super(width, height, cm);
	}

	@Override
	public ImageProcessor getProcessor(int n)
	{
		ImageProcessor ip = super.getProcessor(n);
		if (ip instanceof FloatProcessor)
		{
			InfinityMappedFloatProcessor fp = new InfinityMappedFloatProcessor(getWidth(), getHeight(),
					(float[]) ip.getPixels(), ip.getColorModel());
			fp.setMapPositiveInfinity(mapPositiveInfinity);
			fp.setMinAndMax(ip.getMin(), ip.getMax());
			if (ip.getCalibrationTable() != null)
				fp.setCalibrationTable(ip.getCalibrationTable());
			ip = fp;
		}
		return ip;
	}
}
