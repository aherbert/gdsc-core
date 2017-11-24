package gdsc.core.ij;

import java.security.MessageDigest;

import gdsc.core.utils.Digest;
import ij.process.ImageProcessor;

/*----------------------------------------------------------------------------- 
 * GDSC Plugins for ImageJ
 * 
 * Copyright (C) 2016 Alex Herbert
 * Genome Damage and Stability Centre
 * University of Sussex, UK
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *---------------------------------------------------------------------------*/

/**
 * Provide digest functionality for ImageJ images
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
				buffer[0] = (byte) ((v >>> 8) & 0xFF);
				buffer[1] = (byte) ((v >>> 0) & 0xFF);
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
				buffer[0] = (byte) ((v >>> 24) & 0xFF);
				buffer[1] = (byte) ((v >>> 16) & 0xFF);
				buffer[2] = (byte) ((v >>> 8) & 0xFF);
				buffer[3] = (byte) ((v >>> 0) & 0xFF);
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
				int v = Float.floatToIntBits(data[i]);
				buffer[0] = (byte) ((v >>> 24) & 0xFF);
				buffer[1] = (byte) ((v >>> 16) & 0xFF);
				buffer[2] = (byte) ((v >>> 8) & 0xFF);
				buffer[3] = (byte) ((v >>> 0) & 0xFF);
				digest.update(buffer);
			}
		}
	}

	MessageDigest digest;

	public IJDigest()
	{
		this(Digest.MD5);
	}

	public IJDigest(String algorithm)
	{
		digest = Digest.getDigest(algorithm);
	}

	public String digest(ImageProcessor ip)
	{
		Object pixels = ip.getPixels();
		PixelsDigester digester = getPixelsDigester(pixels);
		digester.update(pixels);
		return Digest.toHex(digester.digest.digest());
	}

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