package ij.io;

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

import java.io.IOException;

/**
 * This class uses a byte array to store an entire seekable stream in memory.
 */
public final class ByteArraySeekableStream extends SeekableStream
{
	int p = 0;
	byte[] buffer;
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
	public long getFilePointer() throws IOException
	{
		return p;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ij.io.SeekableStream#read()
	 */
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
	public int read(byte[] bytes, int off, int len) throws IOException
	{
		if (p < length)
		{
			if (len > 0)
			{
				int size = (p + len <= length) ? len : length - p;
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
		{
			return 0;
		}
		int pos = p;
		long newpos = pos + n;
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
}
