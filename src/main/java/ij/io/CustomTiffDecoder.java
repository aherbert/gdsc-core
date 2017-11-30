package ij.io;

import java.io.IOException;

import ij.util.Tools;

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
 * Extend the TiffDecoder to allow it to accept a RandomAccessStream as an argument
 */
public class CustomTiffDecoder extends TiffDecoder
{
	/** The little endian flag. Used when scanning IFDs to count the images. */
	private boolean littleEndian;

	/**
	 * Instantiates a new custom tiff decoder.
	 *
	 * @param in
	 *            the opened input
	 * @param name
	 *            the name of the TIFF image
	 */
	public CustomTiffDecoder(RandomAccessStream in, String name)
	{
		super("", name);
		this.in = in;
	}

	/**
	 * Gets the number of images in the TIFF file. The class must have been created with a RandomAccessStream. The
	 * stream is not closed by calling this method.
	 *
	 * @param progress
	 *            the progress
	 * @return the number of images
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public int getNumberOfImages() throws IOException
	{
		if (in == null)
			throw new NullPointerException("No random access stream");

		in.seek(0);

		// Find the first IFD
		long ifdOffset = openImageFileHeader();
		if (ifdOffset < 0L)
		{
			//System.out.println("No IFD offset");
			in.seek(0);
			return 0;
		}

		// We do not care about the actual IFD contents. Just the count.

		// Open the first IFD looking for information about the number of images

		//System.out.println("first IFD = " + ifdOffset);
		in.seek(ifdOffset);
		FileInfo fi = scanFirstIFD();

		//		// This should be the same for nImages
		//		in.seek(0);
		//		OpenImageFileHeader();
		//		in.seek(ifdOffset);
		//		FileInfo fi2 = OpenIFD();

		if (fi == null)
		{
			//System.out.println("No first IFD");
			in.seek(0);
			return 0;
		}

		// If an ImageJ image then the nImages is written to the description
		if (fi.nImages > 1)
		{
			in.seek(0);
			return fi.nImages;
		}

		// If not an ImageJ image then we have to read each IFD
		int ifdCount = 1;
		ifdOffset = ((long) getInt2()) & 0xffffffffL;

		while (ifdOffset > 0L)
		{
			in.seek(ifdOffset);

			if (!scanIFD())
			{
				//System.out.println("No more IFDs");
				break;
			}

			ifdCount++;
			ifdOffset = ((long) getInt2()) & 0xffffffffL;
		}

		in.seek(0);

		return ifdCount;
	}

	/**
	 * Copied from TiffDecoder just so that we know if it is big/little endian
	 * 
	 * @return The offset to the first IFD
	 * @throws IOException
	 */
	private long openImageFileHeader() throws IOException
	{
		// Open 8-byte Image File Header at start of file.
		// Returns the offset in bytes to the first IFD or -1
		// if this is not a valid tiff file.
		int byteOrder = in.readShort();
		if (byteOrder == 0x4949) // "II"
			littleEndian = true;
		else if (byteOrder == 0x4d4d) // "MM"
			littleEndian = false;
		else
		{
			// Don't close the input
			//in.close();
			return -1;
		}
		//int magicNumber = 
		getShort2(); // 42
		long offset = ((long) getInt2()) & 0xffffffffL;
		return offset;
	}

	private int getInt2() throws IOException
	{
		int b1 = in.read();
		int b2 = in.read();
		int b3 = in.read();
		int b4 = in.read();
		if (littleEndian)
			return ((b4 << 24) + (b3 << 16) + (b2 << 8) + (b1 << 0));
		else
			return ((b1 << 24) + (b2 << 16) + (b3 << 8) + b4);
	}

	private int getShort2() throws IOException
	{
		int b1 = in.read();
		int b2 = in.read();
		if (littleEndian)
			return ((b2 << 8) + b1);
		else
			return ((b1 << 8) + b2);
	}

	private final static int INDEX_SIZE = 2 + 2 + 4 + 4;// short+short+int+int

	private FileInfo scanFirstIFD() throws IOException
	{
		// Get Image File Directory data
		int tag, fieldType, count, value;
		int nEntries = getShort2();
		if (nEntries < 1 || nEntries > 1000)
			return null;
		FileInfo fi = new FileInfo();

		// Read the index data in one operation. 
		// Any tag data is read by using a seek operation and then reset to the current position.
		byte[] buffer = new byte[nEntries * INDEX_SIZE];
		int read = in.read(buffer);
		if (read != buffer.length)
			return null;

		for (int i = 0, j = 0; i < nEntries; i++)
		{
			tag = getShort(buffer[j++] & 0xff, buffer[j++] & 0xff);
			fieldType = getShort(buffer[j++] & 0xff, buffer[j++] & 0xff);
			count = getInt(buffer[j++] & 0xff, buffer[j++] & 0xff, buffer[j++] & 0xff, buffer[j++] & 0xff);
			value = getValue(fieldType, count, buffer[j++] & 0xff, buffer[j++] & 0xff, buffer[j++] & 0xff,
					buffer[j++] & 0xff);

			// We are only interested in any field that specify the nImages
			switch (tag)
			{
				// Note: 
				// NIH_IMAGE_HDR does contain nImages for GRAY8 or COLOR8.
				// We don't read those tags so don't support this

				// METAMORPH2 contains the nImages if compression is FileInfo.COMPRESSION_NONE

				// IPLAB contains the nImages. We will not encounter those.

				// Just support extracting the nImages from the description
				case IMAGE_DESCRIPTION:
					long lvalue = ((long) value) & 0xffffffffL;
					byte[] s = getString(count, lvalue);
					if (s != null)
						saveImageJnImages(s, fi);

					// This is all we need
					return fi;
			}
		}
		return fi;
	}

	private int getShort(int b1, int b2) throws IOException
	{
		if (littleEndian)
			return ((b2 << 8) + b1);
		else
			return ((b1 << 8) + b2);
	}

	private int getInt(int b1, int b2, int b3, int b4) throws IOException
	{
		if (littleEndian)
			return ((b4 << 24) + (b3 << 16) + (b2 << 8) + (b1 << 0));
		else
			return ((b1 << 24) + (b2 << 16) + (b3 << 8) + b4);
	}

	private int getValue(int fieldType, int count, int b1, int b2, int b3, int b4) throws IOException
	{
		int value = 0;
		if (fieldType == SHORT && count == 1)
			value = getShort(b1, b2);
		else
			value = getInt(b1, b2, b3, b4);
		return value;
	}

	private boolean scanIFD() throws IOException
	{
		// Get Image File Directory data
		int nEntries = getShort2();
		//System.out.println("nEntries = " + nEntries);
		if (nEntries < 1 || nEntries > 1000)
			return false;
		// Skip all the index data: tag, fieldType, count, value
		in.skip(nEntries * INDEX_SIZE);
		return true;
	}

	/**
	 * ImageJ saves the number of images for stacks in the TIFF description tag to avoid having to
	 * decode an IFD for each image.
	 */
	public static void saveImageJnImages(byte[] description, FileInfo fi)
	{
		String id = new String(description);
		fi.description = id;
		if (id.length() < 7)
			return;
		int index1 = id.indexOf("images=");
		if (index1 > 0)
		{
			int index2 = id.indexOf("\n", index1);
			if (index2 > 0)
			{
				String images = id.substring(index1 + 7, index2);
				int n = (int) Tools.parseDouble(images, 0.0);
				if (n > 1)
					fi.nImages = n;
			}
		}
	}
	
	// TODO - re-implement getTiffInfo() so that we can use buffering when reading the IFDs
	// It may be easiest to just re-implement the entire class
}
