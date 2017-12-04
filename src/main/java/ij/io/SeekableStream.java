package ij.io;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

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
 * Base class implementing functionality to seek within a stream.
 */
public abstract class SeekableStream extends InputStream
{
	public abstract long getFilePointer() throws IOException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#read()
	 */
	public abstract int read() throws IOException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#read(byte[], int, int)
	 */
	public abstract int read(byte[] bytes, int off, int len) throws IOException;

	/**
	 * Read the full length of the buffer into the byte buffer.
	 *
	 * @param bytes
	 *            the bytes
	 * @return the number of bytes read
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public final int readFully(byte[] bytes) throws IOException
	{
		return readFully(bytes, bytes.length);
	}

	/**
	 * Read the set length into the byte buffer.
	 *
	 * @param bytes
	 *            the bytes
	 * @param len
	 *            the length
	 * @return the number of bytes read
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public final int readFully(byte[] bytes, int len) throws IOException
	{
		int read = 0;
		do
		{
			int l = read(bytes, read, len - read);
			if (l < 0)
				break;
			read += l;
		} while (read < len);
		return read;
	}

	/**
	 * Seek to a position in the stream.
	 *
	 * @param loc
	 *            the location
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public abstract void seek(long loc) throws IOException;

	/**
	 * Seek to a position in the stream.
	 *
	 * @param loc
	 *            the location (used as an unsigned int)
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void seek(int loc) throws IOException
	{
		seek(((long) loc) & 0xffffffffL);
	}

	/**
	 * Read an int value from the stream.
	 *
	 * @return the int
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public final int readInt() throws IOException
	{
		int i = read();
		int j = read();
		int k = read();
		int l = read();
		if ((i | j | k | l) < 0)
			throw new EOFException();
		else
			return (i << 24) + (j << 16) + (k << 8) + l;
	}

	/**
	 * Read a long value from the stream.
	 *
	 * @return the long
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public final long readLong() throws IOException
	{
		return ((long) readInt() << 32) + ((long) readInt() & 0xffffffffL);
	}

	/**
	 * Read a double value from the stream.
	 *
	 * @return the double
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public final double readDouble() throws IOException
	{
		return Double.longBitsToDouble(readLong());
	}

	/**
	 * Read a short value from the stream.
	 *
	 * @return the short
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public final short readShort() throws IOException
	{
		int i = read();
		int j = read();
		if ((i | j) < 0)
			throw new EOFException();
		else
			return (short) ((i << 8) + j);
	}

	/**
	 * Read a float value from the stream.
	 *
	 * @return the float
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public final float readFloat() throws IOException
	{
		return Float.intBitsToFloat(readInt());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#close()
	 */
	public abstract void close() throws IOException;
}
