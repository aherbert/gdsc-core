package ij.io;

import java.io.File;
import java.io.IOException;

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

/**
 * A little-endian FastTiffDecoder
 */
public class FastTiffDecoderLE extends FastTiffDecoder
{
	protected FastTiffDecoderLE(SeekableStream in, File file)
	{
		super(in, file);
	}

	@Override
	public boolean isLittleEndian()
	{
		return true;
	}

	@Override
	protected int getInt(int b1, int b2, int b3, int b4)
	{
		return ((b4 << 24) + (b3 << 16) + (b2 << 8) + (b1 << 0));
	}

	@Override
	protected int getShort(int b1, int b2)
	{
		return ((b2 << 8) + b1);
	}

	@Override
	protected long readLong() throws IOException
	{
		return ((long) readInt() & 0xffffffffL) + ((long) readInt() << 32);
	}

	/**
	 * Convert the bytes (range 0-255) to an int.
	 *
	 * @param b1
	 *            byte 1
	 * @param b2
	 *            byte 2
	 * @param b3
	 *            byte 3
	 * @param b4
	 *            byte 4
	 * @return the int
	 */
	public static int toInt(int b1, int b2, int b3, int b4)
	{
		return ((b4 << 24) + (b3 << 16) + (b2 << 8) + (b1 << 0));
	}

	/**
	 * Convert the bytes (range 0-255) to a short.
	 *
	 * @param b1
	 *            byte 1
	 * @param b2
	 *            byte 2
	 * @return the short
	 */
	public static int toShort(int b1, int b2)
	{
		return ((b2 << 8) + b1);
	}
}
