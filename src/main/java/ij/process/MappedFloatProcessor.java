package ij.process;

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

/**
 * Extends the ImageJ FloatProcess class to map the min-max range to 1-255 in the 8-bit image. Zero in the data is
 * mapped to 0 unless the zero value is specifically set (e.g. to -0.0f).
 * 
 * @author Alex Herbert
 */
public class MappedFloatProcessor extends FloatProcessor
{
	private int mapToZero = Float.floatToRawIntBits(0.0f);

	public void setZero(float f)
	{
		mapToZero = Float.floatToRawIntBits(f);
	}

	/** Creates a new MappedFloatProcessor using the specified pixel array. */
	public MappedFloatProcessor(int width, int height, float[] pixels)
	{
		this(width, height, pixels, null);
	}

	/** Creates a new MappedFloatProcessor using the specified pixel array and ColorModel. */
	public MappedFloatProcessor(int width, int height, float[] pixels, ColorModel cm)
	{
		super(width, height, pixels, cm);
	}

	/**
	 * Creates a blank MappedFloatProcessor using the default grayscale LUT that
	 * displays zero as black. Call invertLut() to display zero as white.
	 */
	public MappedFloatProcessor(int width, int height)
	{
		super(width, height, new float[width * height], null);
	}

	/** Creates a MappedFloatProcessor from an int array using the default grayscale LUT. */
	public MappedFloatProcessor(int width, int height, int[] pixels)
	{
		super(width, height, pixels);
	}

	/** Creates a MappedFloatProcessor from a double array using the default grayscale LUT. */
	public MappedFloatProcessor(int width, int height, double[] pixels)
	{
		super(width, height, pixels);
	}

	/** Creates a MappedFloatProcessor from a 2D float array using the default LUT. */
	public MappedFloatProcessor(float[][] array)
	{
		super(array);
	}

	/** Creates a MappedFloatProcessor from a 2D int array. */
	public MappedFloatProcessor(int[][] array)
	{
		super(array);
	}

	// scale from float to 8-bits
	protected byte[] create8BitImage()
	{
		int size = width * height;
		if (pixels8 == null)
			pixels8 = new byte[size];
		float[] pixels = (float[]) getPixels();
		float value;
		int ivalue;
		float min2 = (float) getMin(), max2 = (float) getMax();
		float scale = 254f / (max2 - min2);
		for (int i = 0; i < size; i++)
		{
			if (Float.floatToRawIntBits(pixels[i]) == mapToZero)
			{
				pixels8[i] = (byte) 0;
				continue;
			}

			value = pixels[i] - min2;
			if (value < 0f)
				value = 0f;
			ivalue = 1 + (int) ((value * scale) + 0.5f);
			if (ivalue > 255)
				ivalue = 255;
			pixels8[i] = (byte) ivalue;
		}
		return pixels8;
	}
}
