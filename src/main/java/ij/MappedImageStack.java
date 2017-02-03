package ij;

/*----------------------------------------------------------------------------- 
 * GDSC Software
 * 
 * Copyright (C) 2017 Alex Herbert
 * Genome Damage and Stability Centre
 * University of Sussex, UK
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *---------------------------------------------------------------------------*/

import java.awt.image.ColorModel;

import ij.ImageStack;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.MappedFloatProcessor;

/**
 * Extends the ImageJ ImageStack class to support a mapped float processor for float data.
 * 
 * @author Alex Herbert
 */
public class MappedImageStack extends ImageStack
{
	private boolean mapZero = false;

	/**
	 * If set to true positive zero is mapped to 1 in the LUT. The default maps the first value above zero to 1 in the LUT.
	 *
	 * @return true, if is map zero
	 */
	public boolean isMapZero()
	{
		return mapZero;
	}

	/**
	 * Set to true to map positive zero to 1 in the LUT. The default maps the first value above zero to 1 in the LUT.
	 *
	 * @param mapZero the new map zero value
	 */
	public void setMapZero(boolean mapZero)
	{
		this.mapZero = mapZero;
	}

	/** Default constructor. */
	public MappedImageStack() { }

	/** Creates a new, empty image stack. */
	public MappedImageStack(int width, int height)
	{
		this(width, height, null);
	}

	/**
	 * Creates a new, empty image stack with a capacity of 'size'. All
	 * 'size' slices and labels of this image stack are initially null.
	 */
	public MappedImageStack(int width, int height, int size)
	{
		super(width, height, size);
	}

	/** Creates a new, empty image stack using the specified color model. */
	public MappedImageStack(int width, int height, ColorModel cm)
	{
		super(width, height, cm);
	}

	@Override
	public ImageProcessor getProcessor(int n)
	{
		ImageProcessor ip = super.getProcessor(n);
		if (ip instanceof FloatProcessor)
		{
			MappedFloatProcessor fp = new MappedFloatProcessor(getWidth(), getHeight(), (float[]) ip.getPixels(),
					ip.getColorModel());
			fp.setMapZero(mapZero);
			fp.setMinAndMax(ip.getMin(), ip.getMax());
			if (ip.getCalibrationTable() != null)
				fp.setCalibrationTable(ip.getCalibrationTable());
			ip = fp;
		}
		return ip;
	}

}
