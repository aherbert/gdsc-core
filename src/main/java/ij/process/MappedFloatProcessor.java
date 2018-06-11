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
package ij.process;


import java.awt.image.ColorModel;

/**
 * Extends the ImageJ FloatProcess class to map the min-max range to 1-255 in the 8-bit image. The min is set to the
 * first value above zero. All values below min are mapped to 0 in the LUT.
 * <p>
 * Optionally +0.0f can be set as the min value mapped to 1. In this case -0.0f is still mapped to 0. This allows for
 * example display of the results of a probability calculation where 0 is a valid display value. -0.0f can be used when
 * no probability exists.
 * 
 * @author Alex Herbert
 */
public class MappedFloatProcessor extends FloatProcessor
{
	private boolean mapZero = false;

	/**
	 * If set to true positive zero is mapped to 1 in the LUT. The default maps the first value above zero to 1 in the
	 * LUT.
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
	 * @param mapZero
	 *            the new map zero value
	 */
	public void setMapZero(boolean mapZero)
	{
		this.mapZero = mapZero;
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

	private static int NEGATIVE_ZERO = Float.floatToRawIntBits(-0.0f);

	// scale from float to 8-bits
	protected byte[] create8BitImage()
	{
		/*
		 * Map all non zero values to the range 1-255.
		 * 
		 * Optionally map +zero to the range 1-255 as well.
		 * 
		 * Must find the minimum value above zero. This will be mapped to 1.
		 * Or special case is mapping +0f to 1 but -0f to 0.
		 */

		int size = width * height;
		if (pixels8 == null)
			pixels8 = new byte[size];
		float[] pixels = (float[]) getPixels();
		float value;
		int ivalue;

		// Default min/max
		float min2 = (float) getMin(), max2 = (float) getMax();

		// Ensure above zero
		min2 = Math.max(0, min2);
		max2 = Math.max(0, max2);

		// Get minimum above zero
		if (min2 == 0 && max2 > 0 && !isMapZero())
		{
			min2 = max2;
			for (int i = 0; i < size; i++)
			{
				if (pixels[i] > 0)
				{
					if (min2 > pixels[i])
						min2 = pixels[i];
				}
			}
		}

		float scale = 254f / (max2 - min2);

		if (isMapZero() && min2 == 0)
		{
			// We map equal or below -0 to 0.
			// Special case of mapping +0 to 1.
			for (int i = 0; i < size; i++)
			{
				if (pixels[i] < 0)
				{
					// Below zero maps to zero
					pixels8[i] = (byte) 0;
					continue;
				}

				// Special case where we must check for -0 or +0
				if (pixels[i] == 0)
				{
					if (Float.floatToRawIntBits(pixels[i]) == NEGATIVE_ZERO)
					{
						pixels8[i] = (byte) 0;
						continue;
					}
				}

				// +0 or above maps to 1-255
				ivalue = 1 + (int) ((pixels[i] * scale) + 0.5f);
				if (ivalue > 255)
					ivalue = 255;
				pixels8[i] = (byte) ivalue;
			}
		}
		else
		{
			for (int i = 0; i < size; i++)
			{
				if (pixels[i] < min2 || pixels[i] == 0)
				{
					// Below min (or zero) maps to zero
					pixels8[i] = (byte) 0;
				}
				else
				{
					// Map all non zero values to the range 1-255.
					value = pixels[i] - min2;
					ivalue = 1 + (int) ((value * scale) + 0.5f);
					if (ivalue > 255)
						ivalue = 255;
					pixels8[i] = (byte) ivalue;
				}
			}
		}
		return pixels8;
	}
}
