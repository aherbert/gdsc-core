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
package uk.ac.sussex.gdsc.core.ij.io;

import java.io.IOException;

/**
 * This class uses a byte array to store an entire seekable stream in memory.
 */
public final class ByteArraySeekableStream extends SeekableStream
{
	/** The current position in the byte array. */
	int p = 0;

	/** The buffer of bytes. */
	byte[] buffer;

	/** The length of the byte array. */
	final int length;

	/**
	 * Instantiates a new byte array seekable stream.
	 *
	 * @param bytes
	 *            the bytes
	 */
	public ByteArraySeekableStream(byte[] bytes)
	{
		if (bytes == null)
			throw new NullPointerException();
		this.buffer = bytes;
		length = bytes.length;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see ij.io.SeekableStream#getFilePointer()
	 */
	@Override
	public long getFilePointer() throws IOException
	{
		return p;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see ij.io.SeekableStream#read()
	 */
	@Override
	public int read() throws IOException
	{
		if (p < length)
			return buffer[p++] & 0xff;
		return -1;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see ij.io.SeekableStream#read(byte[], int, int)
	 */
	@Override
	public int read(byte[] bytes, int off, int len) throws IOException
	{
		if (p < length)
		{
			if (len > 0)
			{
				final int size = (p + len <= length) ? len : length - p;
				System.arraycopy(buffer, p, bytes, off, size);
				p += size;
				return size;
			}
			return 0;
		}
		return -1;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see ij.io.SeekableStream#seek(long)
	 */
	@Override
	public void seek(long loc) throws IOException
	{
		if (loc < 0)
			throw new IOException("Negative position");
		// Allow seek to the end
		p = (loc > length) ? length : (int) loc;
	}

	@Override
	public void close() throws IOException
	{
		// Do nothing
	}

	@Override
	public long skip(long n) throws IOException
	{
		if (n <= 0)
			return 0;
		final int pos = p;
		final long newpos = pos + n;
		if (newpos > length || newpos < 0) // Check against overflow
			p = length;
		else
			p = (int) newpos;

		/* return the actual number of bytes skipped */
		return p - pos;
	}

	@Override
	public int available() throws IOException
	{
		return length - p;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see ij.io.SeekableStream#canCopy()
	 */
	@Override
	public boolean canCopy()
	{
		return true;
	}

	/**
	 * Copy the stream reusing the underlying byte buffer.
	 *
	 * @return the byte array seekable stream
	 */
	@Override
	public ByteArraySeekableStream copy() throws IOException
	{
		return new ByteArraySeekableStream(buffer);
	}
}
