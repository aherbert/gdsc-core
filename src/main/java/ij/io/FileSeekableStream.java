package ij.io;

import java.io.File;
import java.io.FileNotFoundException;

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
import java.io.RandomAccessFile;

/**
 * This class uses a random access file to allow seeking within a stream.
 */
public final class FileSeekableStream extends SeekableStream
{
	private RandomAccessFile ras;

	/**
	 * Instantiates a new file seekable stream. The input file must be opened.
	 *
	 * @param ras
	 *            the random access file
	 */
	public FileSeekableStream(RandomAccessFile ras)
	{
		if (ras == null)
			throw new NullPointerException();
		this.ras = ras;
	}

	/**
	 * Instantiates a new file seekable stream. The input file will be opened.
	 *
	 * @param file
	 *            the file
	 * @throws FileNotFoundException
	 *             if the given file object does not denote an existing regular file
	 * @throws SecurityException
	 *             if a security manager exists and its checkRead method denies read access to the file
	 */
	public FileSeekableStream(File file) throws FileNotFoundException, SecurityException
	{
		if (file == null)
			throw new NullPointerException();
		this.ras = new RandomAccessFile(file, "r");
	}

	/**
	 * Instantiates a new file seekable stream. The input file will be opened.
	 *
	 * @param path
	 *            the path
	 * @throws FileNotFoundException
	 *             if the given file object does not denote an existing regular file
	 * @throws SecurityException
	 *             if a security manager exists and its checkRead method denies read access to the file
	 */
	public FileSeekableStream(String path) throws FileNotFoundException, SecurityException
	{
		this(new File(path));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ij.io.SeekableStream#getFilePointer()
	 */
	public long getFilePointer() throws IOException
	{
		return ras.getFilePointer();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ij.io.SeekableStream#read()
	 */
	public int read() throws IOException
	{
		return ras.read();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ij.io.SeekableStream#read(byte[], int, int)
	 */
	public int read(byte[] bytes, int off, int len) throws IOException
	{
		return ras.read(bytes, off, len);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ij.io.SeekableStream#seek(long)
	 */
	public void seek(long loc) throws IOException
	{
		ras.seek(loc);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ij.io.SeekableStream#close()
	 */
	public void close() throws IOException
	{
		ras.close();
	}

	@Override
	public long skip(long n) throws IOException
	{
		if (n <= 0)
		{
			return 0;
		}
		long pos = getFilePointer();
		long len = ras.length();
		long newpos = pos + n;
		if (newpos > len || newpos < 0) // Check against overflow
		{
			newpos = len;
		}
		seek(newpos);

		/* return the actual number of bytes skipped */
		return newpos - pos;
	}
}
