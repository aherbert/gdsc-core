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
package gdsc.core.ij;

import java.security.MessageDigest;

import gdsc.core.utils.Digest;
import ij.ImageStack;
import ij.process.ImageProcessor;

/**
 * Provide digest functionality for ImageJ images to digest the pixels array
 */
public class IJDigest
{
	private abstract class PixelsDigester
	{
		MessageDigest digest;

		PixelsDigester(MessageDigest digest)
		{
			this.digest = digest;
			digest.reset();
		}

		public abstract void update(Object pixels);
	}

	private class BytePixelsDigester extends PixelsDigester
	{
		BytePixelsDigester(MessageDigest digest)
		{
			super(digest);
		}

		@Override
		public void update(Object pixels)
		{
			digest.update((byte[]) pixels);
		}
	}

	private class ShortPixelsDigester extends PixelsDigester
	{
		byte[] buffer = new byte[2];

		ShortPixelsDigester(MessageDigest digest)
		{
			super(digest);
		}

		@Override
		public void update(Object pixels)
		{
			short[] data = (short[]) pixels;
			for (int i = 0; i < data.length; i++)
			{
				int v = data[i];
				buffer[0] = (byte) (v >>> 8);
				buffer[1] = (byte) (v >>> 0);
				digest.update(buffer);
			}
		}
	}

	private class IntegerPixelsDigester extends PixelsDigester
	{
		byte[] buffer = new byte[4];

		IntegerPixelsDigester(MessageDigest digest)
		{
			super(digest);
		}

		@Override
		public void update(Object pixels)
		{
			int[] data = (int[]) pixels;
			for (int i = 0; i < data.length; i++)
			{
				int v = data[i];
				buffer[0] = (byte) (v >>> 24);
				buffer[1] = (byte) (v >>> 16);
				buffer[2] = (byte) (v >>> 8);
				buffer[3] = (byte) (v >>> 0);
				digest.update(buffer);
			}
		}
	}

	private class FloatPixelsDigester extends PixelsDigester
	{
		byte[] buffer = new byte[4];

		FloatPixelsDigester(MessageDigest digest)
		{
			super(digest);
		}

		@Override
		public void update(Object pixels)
		{
			float[] data = (float[]) pixels;
			for (int i = 0; i < data.length; i++)
			{
				int v = Float.floatToRawIntBits(data[i]);
				buffer[0] = (byte) (v >>> 24);
				buffer[1] = (byte) (v >>> 16);
				buffer[2] = (byte) (v >>> 8);
				buffer[3] = (byte) (v >>> 0);
				digest.update(buffer);
			}
		}
	}

	MessageDigest digest;

	/**
	 * Instantiates a new IJ digest.
	 */
	public IJDigest()
	{
		this(Digest.MD5);
	}

	/**
	 * Instantiates a new IJ digest.
	 *
	 * @param algorithm
	 *            the algorithm
	 */
	public IJDigest(String algorithm)
	{
		digest = Digest.getDigest(algorithm);
	}

	/**
	 * Digest the processor.
	 *
	 * @param ip
	 *            the ip
	 * @return the string
	 */
	public String digest(ImageProcessor ip)
	{
		Object pixels = ip.getPixels();
		PixelsDigester digester = getPixelsDigester(pixels);
		digester.update(pixels);
		return Digest.toHex(digester.digest.digest());
	}

	/**
	 * Digest the stack.
	 *
	 * @param stack
	 *            the stack
	 * @return the string
	 */
	public String digest(ImageStack stack)
	{
		PixelsDigester digester = getPixelsDigester(stack.getPixels(1));
		for (int i = 1; i <= stack.getSize(); i++)
			digester.update(stack.getPixels(i));
		return Digest.toHex(digester.digest.digest());
	}

	/**
	 * Gets the pixels digester.
	 *
	 * @param pixels
	 *            the pixels
	 * @return the pixels digester
	 */
	private PixelsDigester getPixelsDigester(Object pixels)
	{
		if (pixels instanceof byte[])
			return new BytePixelsDigester(digest);
		if (pixels instanceof short[])
			return new ShortPixelsDigester(digest);
		if (pixels instanceof float[])
			return new FloatPixelsDigester(digest);
		if (pixels instanceof int[])
			return new IntegerPixelsDigester(digest);
		throw new IllegalArgumentException("Unrecognised pixels type");
	}
}
