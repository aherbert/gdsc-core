package ij.io;

import java.awt.Rectangle;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import gdsc.core.logging.NullTrackProgress;
import gdsc.core.logging.TrackProgress;
import gdsc.core.utils.DoubleEquality;
import gdsc.core.utils.TurboList;
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
 * Re-implement the TiffDecoder to allow it to use a SeekableStream interface.
 * <p>
 * Added support for MicroManager TIFF format which uses the OME-TIFF specification.
 */
public abstract class FastTiffDecoder
{
	private static final Charset UTF8 = Charset.forName("UTF-8");

	// tags
	public static final int NEW_SUBFILE_TYPE = 254;
	public static final int IMAGE_WIDTH = 256;
	public static final int IMAGE_LENGTH = 257;
	public static final int BITS_PER_SAMPLE = 258;
	public static final int COMPRESSION = 259;
	public static final int PHOTO_INTERP = 262;
	public static final int IMAGE_DESCRIPTION = 270;
	public static final int STRIP_OFFSETS = 273;
	public static final int ORIENTATION = 274;
	public static final int SAMPLES_PER_PIXEL = 277;
	public static final int ROWS_PER_STRIP = 278;
	public static final int STRIP_BYTE_COUNT = 279;
	public static final int X_RESOLUTION = 282;
	public static final int Y_RESOLUTION = 283;
	public static final int PLANAR_CONFIGURATION = 284;
	public static final int RESOLUTION_UNIT = 296;
	public static final int SOFTWARE = 305;
	public static final int DATE_TIME = 306;
	public static final int ARTEST = 315;
	public static final int HOST_COMPUTER = 316;
	public static final int PREDICTOR = 317;
	public static final int COLOR_MAP = 320;
	public static final int TILE_WIDTH = 322;
	public static final int SAMPLE_FORMAT = 339;
	public static final int JPEG_TABLES = 347;
	public static final int METAMORPH1 = 33628;
	public static final int METAMORPH2 = 33629;
	public static final int IPLAB = 34122;
	public static final int NIH_IMAGE_HDR = 43314;
	public static final int META_DATA_BYTE_COUNTS = 50838; // private ImageJ tag registered with Adobe
	public static final int META_DATA = 50839; // private ImageJ tag registered with Adobe
	public static final int MICRO_MANAGER_META_DATA = 51123; // MicroManager metadata

	//constants
	static final int UNSIGNED = 1;
	static final int SIGNED = 2;
	static final int FLOATING_POINT = 3;

	//field types
	static final int SHORT = 3;
	static final int LONG = 4;

	public static final int BYTE = 1;
	public static final int ASCII_STRING = 2;
	public static final int WORD = 3;
	public static final int DWORD = 4;
	public static final int RATIONAL = 5;

	// metadata types
	static final int MAGIC_NUMBER = 0x494a494a; // "IJIJ"
	static final int INFO = 0x696e666f; // "info" (Info image property)
	static final int LABELS = 0x6c61626c; // "labl" (slice labels)
	static final int RANGES = 0x72616e67; // "rang" (display ranges)
	static final int LUTS = 0x6c757473; // "luts" (channel LUTs)
	static final int ROI = 0x726f6920; // "roi " (ROI)
	static final int OVERLAY = 0x6f766572; // "over" (overlay)

	private String directory;
	private String name;
	protected SeekableStream ss;
	protected boolean debugMode;
	private String dInfo;
	private int ifdCount;
	private int[] metaDataCounts;
	private String tiffMetadata;
	private int photoInterp;
	private int nEntries;
	private byte[] buffer;

	private TrackProgress trackProgress = NullTrackProgress.INSTANCE;

	/**
	 * This is a count of the number of IFDs for which the micro manager metadata will be read. This metadata can be
	 * large and so by default the count is 1 to only do this for the first IFD. Set to 0 if not wanted.
	 */
	public int ifdCountForMicroManagerMetadata = 1;

	/**
	 * This is a count of the number of IFDs for which the debug info is recorded.
	 */
	public int ifdCountForDebugData = 10;

	/**
	 * Instantiates a new fast tiff decoder.
	 *
	 * @param in
	 *            the seekable stream, ready to read the first IFD (at 4 bytes into the stream)
	 * @param file
	 *            the file (or the name of the stream)
	 */
	protected FastTiffDecoder(SeekableStream in, File file)
	{
		directory = file.getParent();
		if (directory == null)
			directory = "";
		this.name = file.getName();
		this.ss = in;
	}

	/**
	 * Checks if is little endian.
	 *
	 * @return true, if is little endian
	 */
	public abstract boolean isLittleEndian();

	/**
	 * Creates the Tiff Decoder with an opened seekable stream set in the position to read the first IFD offset (i.e.
	 * the first 4 bytes of the TIFF file have been read to identify the file type).
	 *
	 * @param directory
	 *            the directory
	 * @param name
	 *            the name
	 * @return the fast tiff decoder
	 * @throws IOException
	 *             If an I/O exception has occurred, or this is not a TIFF file.
	 * @throws NullPointerException
	 *             If name is null
	 */
	public static FastTiffDecoder create(String directory, String name) throws IOException, NullPointerException
	{
		File file = new File(directory, name);
		return create(file);
	}

	/**
	 * Creates the Tiff Decoder with an opened seekable stream set in the position to read the first IFD offset (i.e.
	 * the first 4 bytes of the TIFF file have been read to identify the file type).
	 *
	 * @param in
	 *            the inpout stream
	 * @param name
	 *            the name
	 * @return the fast tiff decoder
	 * @throws IOException
	 *             If an I/O exception has occurred, or this is not a TIFF file.
	 * @throws NullPointerException
	 *             If either argument is null
	 */
	public static FastTiffDecoder create(InputStream in, String name) throws IOException, NullPointerException
	{
		if (in == null)
			throw new NullPointerException();
		return createTiffDecoder(new MemoryCacheSeekableStream(in), new File(name));
	}

	/**
	 * Creates the Tiff Decoder with an opened seekable stream set in the position to read the first IFD offset (i.e.
	 * the first 4 bytes of the TIFF file have been read to identify the file type).
	 *
	 * @param ss
	 *            the ss
	 * @param name
	 *            the name
	 * @return the fast tiff decoder
	 * @throws IOException
	 *             If an I/O exception has occurred, or this is not a TIFF file.
	 * @throws NullPointerException
	 *             If either argument is null
	 */
	public static FastTiffDecoder create(SeekableStream ss, String name) throws IOException, NullPointerException
	{
		if (ss == null)
			throw new NullPointerException();
		ss.seek(0);
		return createTiffDecoder(ss, new File(name));
	}

	/**
	 * Creates the Tiff Decoder with an opened seekable stream set in the position to read the first IFD offset (i.e.
	 * the first 4 bytes of the TIFF file have been read to identify the file type).
	 *
	 * @param file
	 *            the file
	 * @return the fast tiff decoder
	 * @throws IOException
	 *             If an I/O exception has occurred, or this is not a TIFF file.
	 * @throws NullPointerException
	 *             If the file argument is null
	 * @throws FileNotFoundException
	 *             the file not found exception
	 * @throws SecurityException
	 *             the security exception
	 */
	public static FastTiffDecoder create(File file)
			throws IOException, NullPointerException, FileNotFoundException, SecurityException
	{
		return createTiffDecoder(new FileSeekableStream(file), file);
	}

	/**
	 * Creates the tiff decoder. Read the TIFF header and create a little/big-endian decoder.
	 *
	 * @param ss
	 *            the seekable stream at position 0
	 * @param file
	 *            the file (or the name of the stream)
	 * @return the fast tiff decoder
	 * @throws IOException
	 *             If an I/O exception has occurred, or this is not a TIFF file.
	 */
	private static FastTiffDecoder createTiffDecoder(SeekableStream ss, File file) throws IOException
	{
		// Read the 4-byte TIFF header
		byte[] hdr = new byte[4];
		ss.read(hdr);
		// "II" (Intel byte order)
		if (hdr[0] == 73 && hdr[1] == 73)
		{
			// Magic number is 42
			if (hdr[2] == 42 && hdr[3] == 0)
				return new FastTiffDecoderLE(ss, file);
			throw new IOException("Incorrect magic number for little-endian (Intel) byte order");
		}
		// "MM" (Motorola byte order)
		if (hdr[0] == 77 && hdr[1] == 77)
		{
			// Magic number is 42
			if (hdr[2] == 0 && hdr[3] == 42)
				return new FastTiffDecoderBE(ss, file);
			throw new IOException("Incorrect magic number of big-endian (Motorola) byte order");
		}
		throw new IOException("Not a TIFF file");
	}

	/**
	 * Close the seekable stream. Use this when the decoder was created with a file or path.
	 *
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void close() throws IOException
	{
		ss.close();
	}

	/**
	 * Reset the stream. This sets the stream to 4-bytes into the TIFF file after the TIFF magic number has been read
	 * from the header. It is in the position to read the location of the first TIF IFD.
	 *
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public final void reset() throws IOException
	{
		ss.seek(4L);
	}

	final int readInt() throws IOException
	{
		int b1 = ss.read();
		int b2 = ss.read();
		int b3 = ss.read();
		int b4 = ss.read();
		return getInt(b1, b2, b3, b4);
	}

	final int getInt(byte[] buffer, int offset)
	{
		return getInt(buffer[offset] & 0xff, buffer[offset + 1] & 0xff, buffer[offset + 2] & 0xff,
				buffer[offset + 3] & 0xff);
	}

	protected abstract int getInt(int b1, int b2, int b3, int b4);

	final long readUnsignedInt() throws IOException
	{
		return (long) readInt() & 0xffffffffL;
	}

	final int readShort() throws IOException
	{
		int b1 = ss.read();
		int b2 = ss.read();
		return getShort(b1, b2);
	}

	final int getShort(byte[] buffer, int offset)
	{
		return getShort(buffer[offset] & 0xff, buffer[offset + 1] & 0xff);
	}

	protected abstract int getShort(int b1, int b2);

	protected abstract long readLong() throws IOException;

	private final double readDouble() throws IOException
	{
		return Double.longBitsToDouble(readLong());
	}

	void getColorMap(long offset, ExtendedFileInfo fi) throws IOException
	{
		byte[] colorTable16 = new byte[768 * 2];
		long saveLoc = ss.getFilePointer();
		ss.seek(offset);
		ss.readFully(colorTable16);
		ss.seek(saveLoc);
		fi.lutSize = 256;
		fi.reds = new byte[256];
		fi.greens = new byte[256];
		fi.blues = new byte[256];
		int j = 0;
		if (isLittleEndian())
			j++;
		int sum = 0;
		for (int i = 0; i < 256; i++)
		{
			fi.reds[i] = colorTable16[j];
			sum += fi.reds[i];
			fi.greens[i] = colorTable16[512 + j];
			sum += fi.greens[i];
			fi.blues[i] = colorTable16[1024 + j];
			sum += fi.blues[i];
			j += 2;
		}
		if (sum != 0 && fi.fileType == ExtendedFileInfo.GRAY8)
			fi.fileType = ExtendedFileInfo.COLOR8;
	}

	byte[] getString(int count, long offset) throws IOException
	{
		count--; // skip null byte at end of string
		if (count <= 3)
			return null;
		byte[] bytes = new byte[count];
		long saveLoc = ss.getFilePointer();
		ss.seek(offset);
		ss.readFully(bytes);
		ss.seek(saveLoc);
		return bytes;
	}

	/**
	 * Save the image description in the specified ExtendedFileInfo. ImageJ
	 * saves spatial and density calibration data in this string. For
	 * stacks, it also saves the number of images to avoid having to
	 * decode an IFD for each image.
	 */
	public void saveImageDescription(byte[] description, ExtendedFileInfo fi)
	{
		String id = new String(description);
		if (!id.startsWith("ImageJ"))
			saveMetadata(getName(IMAGE_DESCRIPTION), id);
		if (id.length() < 7)
			return;
		fi.description = id;
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

	public void saveMetadata(String name, String data)
	{
		if (data == null)
			return;
		String str = name + ": " + data + "\n";
		if (tiffMetadata == null)
			tiffMetadata = str;
		else
			tiffMetadata += str;
	}

	void decodeNIHImageHeader(int offset, ExtendedFileInfo fi) throws IOException
	{
		long saveLoc = ss.getFilePointer();

		ss.seek(offset + 12);
		int version = ss.readShort();

		ss.seek(offset + 160);
		double scale = ss.readDouble();
		if (version > 106 && scale != 0.0)
		{
			fi.pixelWidth = 1.0 / scale;
			fi.pixelHeight = fi.pixelWidth;
		}

		// spatial calibration
		ss.seek(offset + 172);
		int units = ss.readShort();
		if (version <= 153)
			units += 5;
		switch (units)
		{
			case 5:
				fi.unit = "nanometer";
				break;
			case 6:
				fi.unit = "micrometer";
				break;
			case 7:
				fi.unit = "mm";
				break;
			case 8:
				fi.unit = "cm";
				break;
			case 9:
				fi.unit = "meter";
				break;
			case 10:
				fi.unit = "km";
				break;
			case 11:
				fi.unit = "inch";
				break;
			case 12:
				fi.unit = "ft";
				break;
			case 13:
				fi.unit = "mi";
				break;
		}

		// density calibration
		ss.seek(offset + 182);
		int fitType = ss.read();
		//int unused = 
		ss.read();
		int nCoefficients = ss.readShort();
		if (fitType == 11)
		{
			fi.calibrationFunction = 21; //Calibration.UNCALIBRATED_OD
			fi.valueUnit = "U. OD";
		}
		else if (fitType >= 0 && fitType <= 8 && nCoefficients >= 1 && nCoefficients <= 5)
		{
			switch (fitType)
			{
				case 0:
					fi.calibrationFunction = 0;
					break; //Calibration.STRAIGHT_LINE
				case 1:
					fi.calibrationFunction = 1;
					break; //Calibration.POLY2
				case 2:
					fi.calibrationFunction = 2;
					break; //Calibration.POLY3
				case 3:
					fi.calibrationFunction = 3;
					break; //Calibration.POLY4
				case 5:
					fi.calibrationFunction = 4;
					break; //Calibration.EXPONENTIAL
				case 6:
					fi.calibrationFunction = 5;
					break; //Calibration.POWER
				case 7:
					fi.calibrationFunction = 6;
					break; //Calibration.LOG
				case 8:
					fi.calibrationFunction = 10;
					break; //Calibration.RODBARD2 (NIH Image)
			}
			fi.coefficients = new double[nCoefficients];
			for (int i = 0; i < nCoefficients; i++)
			{
				fi.coefficients[i] = ss.readDouble();
			}
			ss.seek(offset + 234);
			int size = ss.read();
			StringBuffer sb = new StringBuffer();
			if (size >= 1 && size <= 16)
			{
				for (int i = 0; i < size; i++)
					sb.append((char) (ss.read()));
				fi.valueUnit = new String(sb);
			}
			else
				fi.valueUnit = " ";
		}

		ss.seek(offset + 260);
		int nImages = ss.readShort();
		if (nImages >= 2 && (fi.fileType == ExtendedFileInfo.GRAY8 || fi.fileType == ExtendedFileInfo.COLOR8))
		{
			fi.nImages = nImages;
			fi.pixelDepth = ss.readFloat(); //SliceSpacing
			//int skip = 
			ss.readShort(); //CurrentSlice
			fi.frameInterval = ss.readFloat();
			//ij.IJ.write("fi.pixelDepth: "+fi.pixelDepth);
		}

		ss.seek(offset + 272);
		float aspectRatio = ss.readFloat();
		if (version > 140 && aspectRatio != 0.0)
			fi.pixelHeight = fi.pixelWidth / aspectRatio;

		ss.seek(saveLoc);
	}

	void dumpTag(int tag, int count, int value, ExtendedFileInfo fi)
	{
		long lvalue = ((long) value) & 0xffffffffL;
		String name = getName(tag);
		String cs = (count == 1) ? "" : ", count=" + count;
		dInfo += "    " + tag + ", \"" + name + "\", value=" + lvalue + cs + "\n";
	}

	String getName(int tag)
	{
		switch (tag)
		{
			//@formatter:off
			case NEW_SUBFILE_TYPE: return "NewSubfileType"; 
			case IMAGE_WIDTH: return "ImageWidth"; 
			case IMAGE_LENGTH: return "ImageLength"; 
			case STRIP_OFFSETS: return "StripOffsets"; 
			case ORIENTATION: return "Orientation"; 
			case PHOTO_INTERP: return "PhotoInterp"; 
			case IMAGE_DESCRIPTION: return "ImageDescription"; 
			case BITS_PER_SAMPLE: return "BitsPerSample"; 
			case SAMPLES_PER_PIXEL: return "SamplesPerPixel"; 
			case ROWS_PER_STRIP: return "RowsPerStrip"; 
			case STRIP_BYTE_COUNT: return "StripByteCount"; 
			case X_RESOLUTION: return "XResolution"; 
			case Y_RESOLUTION: return "YResolution"; 
			case RESOLUTION_UNIT: return "ResolutionUnit"; 
			case SOFTWARE: return "Software"; 
			case DATE_TIME: return "DateTime"; 
			case ARTEST: return "Artest"; 
			case HOST_COMPUTER: return "HostComputer"; 
			case PLANAR_CONFIGURATION: return "PlanarConfiguration"; 
			case COMPRESSION: return "Compression";  
			case PREDICTOR: return "Predictor";  
			case COLOR_MAP: return "ColorMap";  
			case SAMPLE_FORMAT: return "SampleFormat";  
			case JPEG_TABLES: return "JPEGTables";  
			case NIH_IMAGE_HDR: return "NIHImageHeader";  
			case META_DATA_BYTE_COUNTS: return "MetaDataByteCounts";  
			case META_DATA: return "MetaData";  
			case MICRO_MANAGER_META_DATA: return "MicroManagerMetaData";  
			default: return "???"; 
			//@formatter:on
		}
	}

	private double getRational(long loc) throws IOException
	{
		long saveLoc = ss.getFilePointer();
		ss.seek(loc);
		double numerator = readUnsignedInt();
		double denominator = readUnsignedInt();
		ss.seek(saveLoc);
		if (denominator != 0.0)
			return numerator / denominator;
		else
			return 0.0;
	}

	/**
	 * Open IFD.
	 * <p>
	 * Optionally the IFD entry can be read for only the data needed to read the pixels. This means skipping the
	 * X/YResolution, ResolutionUnit, ImageDescription and any meta-data tags for faster reading.
	 *
	 * @param pixelDataOnly
	 *            the pixel data only flag
	 * @return the extended file info
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private ExtendedFileInfo openIFD(boolean pixelDataOnly) throws IOException
	{
		// Get Image File Directory data
		nEntries = readShort();
		if (nEntries < 1 || nEntries > 1000)
			return null;
		ifdCount++;
		if ((ifdCount % 50) == 0)
			trackProgress.status("Opening IFDs: %d", ifdCount);
		ExtendedFileInfo fi = new ExtendedFileInfo();
		fi.fileType = ExtendedFileInfo.BITMAP; //BitsPerSample defaults to 1

		// Read the index data in one operation. 
		// Any tag data is read by using a seek operation and then reset to the current position.
		int size = nEntries * INDEX_SIZE;
		byte[] buffer = allocateBuffer(size);
		if (ss.readBytes(buffer, size) != size)
			return null;

		for (int i = 0, j = 0; i < nEntries; i++, j += INDEX_SIZE)
		{
			int tag = getShort(buffer, j);

			// Allow skipping non-essential tags
			if (pixelDataOnly && tag > JPEG_TABLES)
				break;

			int fieldType = getShort(buffer, j + 2);
			int count = getInt(buffer, j + 4);
			//int value = getValue(fieldType, count, buffer[j + 8] & 0xff, buffer[j + 9] & 0xff, buffer[j + 10] & 0xff,
			//		buffer[j + 11] & 0xff);
			int value = getValue(fieldType, count, buffer, j + 8);
			long lvalue = ((long) value) & 0xffffffffL;
			if (debugMode && ifdCount <= ifdCountForDebugData)
				dumpTag(tag, count, value, fi);
			//System.out.println(i+"/"+nEntries+" "+tag + ", count=" + count + ", value=" + value);
			//if (tag==0) return null;
			switch (tag)
			{
				case IMAGE_WIDTH:
					fi.width = value;
					fi.intelByteOrder = isLittleEndian();
					break;
				case IMAGE_LENGTH:
					fi.height = value;
					break;
				case STRIP_OFFSETS:
					if (count == 1)
						fi.stripOffsets = new int[] { value };
					else
					{
						long saveLoc = ss.getFilePointer();
						ss.seek(lvalue);
						fi.stripOffsets = new int[count];
						for (int c = 0; c < count; c++)
							fi.stripOffsets[c] = readInt();
						ss.seek(saveLoc);
					}
					fi.offset = count > 0 ? fi.stripOffsets[0] : value;
					if (count > 1 && (((long) fi.stripOffsets[count - 1]) &
							0xffffffffL) < (((long) fi.stripOffsets[0]) & 0xffffffffL))
						fi.offset = fi.stripOffsets[count - 1];
					break;
				case STRIP_BYTE_COUNT:
					if (count == 1)
						fi.stripLengths = new int[] { value };
					else
					{
						long saveLoc = ss.getFilePointer();
						ss.seek(lvalue);
						fi.stripLengths = new int[count];
						for (int c = 0; c < count; c++)
						{
							if (fieldType == SHORT)
								fi.stripLengths[c] = readShort();
							else
								fi.stripLengths[c] = readInt();
						}
						ss.seek(saveLoc);
					}
					break;
				case PHOTO_INTERP:
					photoInterp = value;
					fi.whiteIsZero = value == 0;
					break;
				case BITS_PER_SAMPLE:
					if (count == 1)
					{
						if (value == 8)
							fi.fileType = ExtendedFileInfo.GRAY8;
						else if (value == 16)
							fi.fileType = ExtendedFileInfo.GRAY16_UNSIGNED;
						else if (value == 32)
							fi.fileType = ExtendedFileInfo.GRAY32_INT;
						else if (value == 12)
							fi.fileType = ExtendedFileInfo.GRAY12_UNSIGNED;
						else if (value == 1)
							fi.fileType = ExtendedFileInfo.BITMAP;
						else
							error("Unsupported BitsPerSample: " + value);
					}
					else if (count > 1)
					{
						long saveLoc = ss.getFilePointer();
						ss.seek(lvalue);
						int bitDepth = readShort();
						if (bitDepth == 8)
							fi.fileType = ExtendedFileInfo.GRAY8;
						else if (bitDepth == 16)
							fi.fileType = ExtendedFileInfo.GRAY16_UNSIGNED;
						else
							error("ImageJ can only open 8 and 16 bit/channel images (" + bitDepth + ")");
						ss.seek(saveLoc);
					}
					break;
				case SAMPLES_PER_PIXEL:
					fi.samplesPerPixel = value;
					if (value == 3 && fi.fileType == ExtendedFileInfo.GRAY8)
						fi.fileType = ExtendedFileInfo.RGB;
					else if (value == 3 && fi.fileType == ExtendedFileInfo.GRAY16_UNSIGNED)
						fi.fileType = ExtendedFileInfo.RGB48;
					else if (value == 4 && fi.fileType == ExtendedFileInfo.GRAY8)
						fi.fileType = photoInterp == 5 ? ExtendedFileInfo.CMYK : ExtendedFileInfo.ARGB;
					else if (value == 4 && fi.fileType == ExtendedFileInfo.GRAY16_UNSIGNED)
					{
						fi.fileType = ExtendedFileInfo.RGB48;
						if (photoInterp == 5) //assume cmyk
							fi.whiteIsZero = true;
					}
					break;
				case ROWS_PER_STRIP:
					fi.rowsPerStrip = value;
					break;
				case X_RESOLUTION:
					double xScale = getRational(lvalue);
					if (xScale != 0.0)
						fi.pixelWidth = 1.0 / xScale;
					break;
				case Y_RESOLUTION:
					double yScale = getRational(lvalue);
					if (yScale != 0.0)
						fi.pixelHeight = 1.0 / yScale;
					break;
				case RESOLUTION_UNIT:
					if (value == 1 && fi.unit == null)
						fi.unit = " ";
					else if (value == 2)
					{
						if (fi.pixelWidth == 1.0 / 72.0)
						{
							fi.pixelWidth = 1.0;
							fi.pixelHeight = 1.0;
						}
						else
							fi.unit = "inch";
					}
					else if (value == 3)
						fi.unit = "cm";
					break;
				case PLANAR_CONFIGURATION: // 1=chunky, 2=planar
					if (value == 2 && fi.fileType == ExtendedFileInfo.RGB48)
						fi.fileType = ExtendedFileInfo.RGB48_PLANAR;
					else if (value == 2 && fi.fileType == ExtendedFileInfo.RGB)
						fi.fileType = ExtendedFileInfo.RGB_PLANAR;
					else if (value != 2 &&
							!(fi.samplesPerPixel == 1 || fi.samplesPerPixel == 3 || fi.samplesPerPixel == 4))
					{
						String msg = "Unsupported SamplesPerPixel: " + fi.samplesPerPixel;
						error(msg);
					}
					break;
				case COMPRESSION:
					if (value == 5)
					{// LZW compression
						fi.compression = ExtendedFileInfo.LZW;
						if (fi.fileType == ExtendedFileInfo.GRAY12_UNSIGNED)
							error("ImageJ cannot open 12-bit LZW-compressed TIFFs");
					}
					else if (value == 32773) // PackBits compression
						fi.compression = ExtendedFileInfo.PACK_BITS;
					else if (value == 32946 || value == 8)
						fi.compression = ExtendedFileInfo.ZIP;
					else if (value != 1 && value != 0 && !(value == 7 && fi.width < 500))
					{
						// don't abort with Spot camera compressed (7) thumbnails
						// otherwise, this is an unknown compression type
						fi.compression = ExtendedFileInfo.COMPRESSION_UNKNOWN;
						error("ImageJ cannot open TIFF files " + "compressed in this fashion (" + value + ")");
					}
					break;
				case SOFTWARE:
				case DATE_TIME:
				case HOST_COMPUTER:
				case ARTEST:
					if (ifdCount == 1)
					{
						byte[] bytes = getString(count, lvalue);
						String s = bytes != null ? new String(bytes) : null;
						saveMetadata(getName(tag), s);
					}
					break;
				case PREDICTOR:
					if (value == 2 && fi.compression == ExtendedFileInfo.LZW)
						fi.compression = ExtendedFileInfo.LZW_WITH_DIFFERENCING;
					break;
				case COLOR_MAP:
					if (count == 768)
						getColorMap(lvalue, fi);
					break;
				case TILE_WIDTH:
					error("ImageJ cannot open tiled TIFFs.\nTry using the Bio-Formats plugin.");
					break;
				case SAMPLE_FORMAT:
					if (fi.fileType == ExtendedFileInfo.GRAY32_INT && value == FLOATING_POINT)
						fi.fileType = ExtendedFileInfo.GRAY32_FLOAT;
					if (fi.fileType == ExtendedFileInfo.GRAY16_UNSIGNED)
					{
						if (value == SIGNED)
							fi.fileType = ExtendedFileInfo.GRAY16_SIGNED;
						if (value == FLOATING_POINT)
							error("ImageJ cannot open 16-bit float TIFFs");
					}
					break;
				case JPEG_TABLES:
					if (fi.compression == ExtendedFileInfo.JPEG)
						error("Cannot open JPEG-compressed TIFFs with separate tables");
					break;
				case IMAGE_DESCRIPTION:
					if (pixelDataOnly)
						// Skip this
						break;
					if (ifdCount == 1)
					{
						byte[] s = getString(count, lvalue);
						if (s != null)
							saveImageDescription(s, fi);
					}
					break;
				case ORIENTATION:
					fi.nImages = 0; // file not created by ImageJ so look at all the IFDs
					break;
				case METAMORPH1:
				case METAMORPH2:
					if ((name.indexOf(".STK") > 0 || name.indexOf(".stk") > 0) &&
							fi.compression == ExtendedFileInfo.COMPRESSION_NONE)
					{
						if (tag == METAMORPH2)
							fi.nImages = count;
						else
							fi.nImages = 9999;
					}
					break;
				case IPLAB:
					fi.nImages = value;
					break;
				case NIH_IMAGE_HDR:
					if (count == 256)
						decodeNIHImageHeader(value, fi);
					break;
				case META_DATA_BYTE_COUNTS:
					long saveLoc = ss.getFilePointer();
					ss.seek(lvalue);
					metaDataCounts = new int[count];
					for (int c = 0; c < count; c++)
						metaDataCounts[c] = readInt();
					ss.seek(saveLoc);
					break;
				case META_DATA:
					getMetaData(value, fi);
					break;
				case MICRO_MANAGER_META_DATA:
					if (ifdCount <= ifdCountForMicroManagerMetadata)
					{
						// Only read if desired
						byte[] bytes = getString(count, lvalue);
						if (bytes != null)
							fi.extendedMetaData = new String(bytes, UTF8);
					}
					break;
				default:
					if (tag > 10000 && tag < 32768 && ifdCount > 1)
						return null;
			}
		}
		fi.fileFormat = ExtendedFileInfo.TIFF;
		fi.fileName = name;
		fi.directory = directory;
		return fi;
	}

	private byte[] allocateBuffer(int size)
	{
		if (buffer == null || buffer.length < size)
			buffer = new byte[size];
		return buffer;
	}

	void getMetaData(int loc, ExtendedFileInfo fi) throws IOException
	{
		if (metaDataCounts == null || metaDataCounts.length == 0)
			return;
		long saveLoc = ss.getFilePointer();
		ss.seek(loc);
		int hdrSize = metaDataCounts[0];
		if (hdrSize < 12 || hdrSize > 804)
		{
			ss.seek(saveLoc);
			return;
		}
		int magicNumber = readInt();
		if (magicNumber != MAGIC_NUMBER) // "IJIJ"
		{
			ss.seek(saveLoc);
			return;
		}
		int nTypes = (hdrSize - 4) / 8;
		int[] types = new int[nTypes];
		int[] counts = new int[nTypes];

		if (debugMode)
			dInfo += "Metadata:\n";
		int extraMetaDataEntries = 0;
		for (int i = 0; i < nTypes; i++)
		{
			types[i] = readInt();
			counts[i] = readInt();
			if (types[i] < 0xffffff)
				extraMetaDataEntries += counts[i];
			if (debugMode)
			{
				String id = "";
				if (types[i] == INFO)
					id = " (Info property)";
				if (types[i] == LABELS)
					id = " (slice labels)";
				if (types[i] == RANGES)
					id = " (display ranges)";
				if (types[i] == LUTS)
					id = " (luts)";
				if (types[i] == ROI)
					id = " (roi)";
				if (types[i] == OVERLAY)
					id = " (overlay)";
				dInfo += "   " + i + " " + Integer.toHexString(types[i]) + " " + counts[i] + id + "\n";
			}
		}
		fi.metaDataTypes = new int[extraMetaDataEntries];
		fi.metaData = new byte[extraMetaDataEntries][];
		int start = 1;
		int eMDindex = 0;
		for (int i = 0; i < nTypes; i++)
		{
			if (types[i] == INFO)
				getInfoProperty(start, fi);
			else if (types[i] == LABELS)
				getSliceLabels(start, start + counts[i] - 1, fi);
			else if (types[i] == RANGES)
				getDisplayRanges(start, fi);
			else if (types[i] == LUTS)
				getLuts(start, start + counts[i] - 1, fi);
			else if (types[i] == ROI)
				getRoi(start, fi);
			else if (types[i] == OVERLAY)
				getOverlay(start, start + counts[i] - 1, fi);
			else if (types[i] < 0xffffff)
			{
				for (int j = start; j < start + counts[i]; j++)
				{
					int len = metaDataCounts[j];
					fi.metaData[eMDindex] = new byte[len];
					ss.readFully(fi.metaData[eMDindex], len);
					fi.metaDataTypes[eMDindex] = types[i];
					eMDindex++;
				}
			}
			else
				skipUnknownType(start, start + counts[i] - 1);
			start += counts[i];
		}
		ss.seek(saveLoc);
	}

	void getInfoProperty(int first, ExtendedFileInfo fi) throws IOException
	{
		int len = metaDataCounts[first];
		byte[] buffer = new byte[len];
		ss.readFully(buffer, len);
		len /= 2;
		char[] chars = new char[len];
		if (isLittleEndian())
		{
			for (int j = 0, k = 0; j < len; j++)
				chars[j] = (char) (buffer[k++] & 255 + ((buffer[k++] & 255) << 8));
		}
		else
		{
			for (int j = 0, k = 0; j < len; j++)
				chars[j] = (char) (((buffer[k++] & 255) << 8) + buffer[k++] & 255);
		}
		fi.info = new String(chars);
	}

	void getSliceLabels(int first, int last, ExtendedFileInfo fi) throws IOException
	{
		fi.sliceLabels = new String[last - first + 1];
		int index = 0;
		byte[] buffer = new byte[metaDataCounts[first]];
		for (int i = first; i <= last; i++)
		{
			int len = metaDataCounts[i];
			if (len > 0)
			{
				if (len > buffer.length)
					buffer = new byte[len];
				ss.readFully(buffer, len);
				len /= 2;
				char[] chars = new char[len];
				if (isLittleEndian())
				{
					for (int j = 0, k = 0; j < len; j++)
						chars[j] = (char) (buffer[k++] & 255 + ((buffer[k++] & 255) << 8));
				}
				else
				{
					for (int j = 0, k = 0; j < len; j++)
						chars[j] = (char) (((buffer[k++] & 255) << 8) + buffer[k++] & 255);
				}
				fi.sliceLabels[index++] = new String(chars);
			}
			else
				fi.sliceLabels[index++] = null;
		}
	}

	void getDisplayRanges(int first, ExtendedFileInfo fi) throws IOException
	{
		int n = metaDataCounts[first] / 8;
		fi.displayRanges = new double[n];
		for (int i = 0; i < n; i++)
			fi.displayRanges[i] = readDouble();
	}

	void getLuts(int first, int last, ExtendedFileInfo fi) throws IOException
	{
		fi.channelLuts = new byte[last - first + 1][];
		int index = 0;
		for (int i = first; i <= last; i++)
		{
			int len = metaDataCounts[i];
			fi.channelLuts[index] = new byte[len];
			ss.readFully(fi.channelLuts[index], len);
			index++;
		}
	}

	void getRoi(int first, ExtendedFileInfo fi) throws IOException
	{
		int len = metaDataCounts[first];
		fi.roi = new byte[len];
		ss.readFully(fi.roi, len);
	}

	void getOverlay(int first, int last, ExtendedFileInfo fi) throws IOException
	{
		fi.overlay = new byte[last - first + 1][];
		int index = 0;
		for (int i = first; i <= last; i++)
		{
			int len = metaDataCounts[i];
			fi.overlay[index] = new byte[len];
			ss.readFully(fi.overlay[index], len);
			index++;
		}
	}

	void error(String message) throws IOException
	{
		if (ss != null)
			ss.close();
		throw new IOException(message);
	}

	void skipUnknownType(int first, int last) throws IOException
	{
		long skip = 0;
		for (int i = first; i <= last; i++)
		{
			skip += metaDataCounts[i];
		}
		ss.skip(skip);

		//byte[] buffer = new byte[metaDataCounts[first]];
		//for (int i = first; i <= last; i++)
		//{
		//	int len = metaDataCounts[i];
		//	if (len > buffer.length)
		//		buffer = new byte[len];
		//	ss.readFully(buffer, len);
		//}
	}

	public void enableDebugging()
	{
		debugMode = true;
	}

	/**
	 * Gets the tiff info.
	 * <p>
	 * The stream will need to be reset before calling this method if the decoder has not just been created.
	 * <p>
	 * Optionally the IFD entry can be read for only the data needed to read the pixels. This means skipping the
	 * X/YResolution, ResolutionUnit, ImageDescription and any meta-data tags for faster reading. The full infomation is
	 * always read for the first IFD.
	 *
	 * @param pixelDataOnly
	 *            the pixel data only flag
	 * @return the tiff info
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @see #reset()
	 */
	public ExtendedFileInfo[] getTiffInfo(boolean pixelDataOnly) throws IOException
	{
		//debugMode = true;

		ifdCount = 0;
		long ifdOffset = readUnsignedInt();
		if (ifdOffset < 8L)
		{
			ss.close();
			return null;
		}
		TurboList<ExtendedFileInfo> list = new TurboList<ExtendedFileInfo>();
		if (debugMode)
			dInfo = "\n  " + name + ": opening\n";

		// Read the first IFD. This is read entirely.
		ss.seek(ifdOffset);
		ExtendedFileInfo fi = openIFD(false);
		if (fi != null)
		{
			list.add(fi);
			ifdOffset = readUnsignedInt();
			if (debugMode && ifdCount <= ifdCountForDebugData)
				dInfo += "  nextIFD=" + ifdOffset + "\n";
			// ignore extra IFDs in ImageJ and NIH Image stacks
			if (fi.nImages <= 1)
			{
				// Read the remaining IFDs
				while (ifdOffset > 0L)
				{
					ss.seek(ifdOffset);
					fi = openIFD(pixelDataOnly);
					if (fi != null)
					{
						list.add(fi);
						ifdOffset = readUnsignedInt();
					}
					else
						ifdOffset = 0L;
					if (debugMode && ifdCount <= ifdCountForDebugData)
						dInfo += "  nextIFD=" + ifdOffset + "\n";
				}
			}
		}

		if (list.size() == 0)
		{
			ss.close();
			return null;
		}
		else
		{
			ExtendedFileInfo[] info = list.toArray(new ExtendedFileInfo[list.size()]);

			// Attempt to read the Micro-Manager summary metadata
			if (!pixelDataOnly)
			{
				readMicroManagerSummaryMetadata(info[0]);
				if (info[0].info == null)
					info[0].info = tiffMetadata;
			}

			ss.close();

			if (debugMode)
				info[0].debugInfo = dInfo;
			fi = info[0];
			if (fi.fileType == ExtendedFileInfo.GRAY16_UNSIGNED && fi.description == null)
				fi.lutSize = 0; // ignore troublesome non-ImageJ 16-bit LUTs
			if (debugMode)
			{
				int n = info.length;
				fi.debugInfo += "number of IFDs: " + n + "\n";
				fi.debugInfo += "offset to first image: " + fi.getOffset() + "\n";
				fi.debugInfo += "gap between images: " + getGapInfo(info) + "\n";
				fi.debugInfo += "little-endian byte order: " + fi.intelByteOrder + "\n";
			}

			//System.out.println(dInfo);
			//System.out.println(tiffMetadata);
			//System.out.println(fi.summaryMetaData);
			//System.out.println(fi.extendedMetaData);
			//System.out.println(fi.info);

			return info;
		}
	}

	/**
	 * Gets the tiff info for the index map entry. The seekable stream is not closed.
	 * <p>
	 * This method also sets {@link #ifdCountForDebugData} to 1, i.e. debug data is only recorded for the first IFD.
	 * <p>
	 * Optionally the IFD entry can be read for only the data needed to read the pixels. This means skipping the
	 * X/YResolution, ResolutionUnit, ImageDescription and any meta-data tags for faster reading.
	 *
	 * @param indexMap
	 *            the index map
	 * @param i
	 *            the entry index
	 * @param pixelDataOnly
	 *            the pixel data only flag
	 * @return the tiff info
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public ExtendedFileInfo getTiffInfo(IndexMap indexMap, int i, boolean pixelDataOnly) throws IOException
	{
		//debugMode = true;

		// Note: Special processing for the first IFD so that the result of reading all
		// IFDs from the IndexMap should be the same as using getTiffInfo(). This is 
		// with the exception of the debugInfo field.
		ss.seek(indexMap.getOffset(i));
		ifdCount = i;
		ifdCountForDebugData = 1; // Only record debugging info for the first IFD
		if (i == 0)
		{
			if (debugMode)
				dInfo = "\n  " + name + ": opening\n";
			tiffMetadata = null;
		}
		ExtendedFileInfo fi = openIFD(pixelDataOnly);
		if (i == 0)
		{
			if (!pixelDataOnly)
			{
				readMicroManagerSummaryMetadata(fi);
				if (fi.info == null)
					fi.info = tiffMetadata;
			}
			if (debugMode)
				fi.debugInfo = dInfo;
			if (fi.fileType == ExtendedFileInfo.GRAY16_UNSIGNED && fi.description == null)
				fi.lutSize = 0; // ignore troublesome non-ImageJ 16-bit LUTs
		}

		//System.out.println(dInfo);
		//System.out.println(tiffMetadata);
		//System.out.println(fi.summaryMetaData);
		//System.out.println(fi.extendedMetaData);
		//System.out.println(fi.info);

		return fi;
	}

	private void readMicroManagerSummaryMetadata(ExtendedFileInfo fi) throws IOException
	{
		ss.seek(32L);
		if (readInt() == 2355492)
		{
			int count = readInt();
			byte[] bytes = new byte[count];
			ss.readFully(bytes);
			fi.summaryMetaData = new String(bytes, UTF8);
		}
	}

	String getGapInfo(ExtendedFileInfo[] fi)
	{
		if (fi.length < 2)
			return "0";
		long minGap = Long.MAX_VALUE;
		long maxGap = -Long.MAX_VALUE;
		for (int i = 1; i < fi.length; i++)
		{
			long gap = fi[i].getOffset() - fi[i - 1].getOffset();
			if (gap < minGap)
				minGap = gap;
			if (gap > maxGap)
				maxGap = gap;
		}
		long imageSize = fi[0].width * fi[0].height * fi[0].getBytesPerPixel();
		minGap -= imageSize;
		maxGap -= imageSize;
		if (minGap == maxGap)
			return "" + minGap;
		else
			return "varies (" + minGap + " to " + maxGap + ")";
	}

	/**
	 * A class for holding the number of images in a TIFF file.
	 */
	public static class NumberOfImages
	{
		/** The number of images. */
		public final int numberOfImages;

		/** Flag to indicate that the TIFF info had information for an exact image count. */
		public final boolean exact;

		/**
		 * The error. This is the difference in file size between the estimated size using an IFD scan and the actual
		 * file size.
		 */
		public final double error;

		public NumberOfImages(int numberOfImages, double error)
		{
			this.numberOfImages = numberOfImages;
			this.error = error;
			exact = false;
		}

		public NumberOfImages(int numberOfImages)
		{
			this.numberOfImages = numberOfImages;
			this.error = 0;
			exact = true;
		}
	}

	private static final NumberOfImages NO_IMAGES = new NumberOfImages(0);
	private static final NumberOfImages ONE_IMAGE = new NumberOfImages(1);

	/**
	 * Gets the number of images in the TIFF file. The stream is not opened or closed by calling this method.
	 * <p>
	 * The stream will need to be reset before calling this method if the decoder has not just been created.
	 *
	 * @param estimate
	 *            Flag to indicate that an estimate using file sizes is OK. The default is to read all the IFDs.
	 * @return the number of images
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @see #reset()
	 */
	public NumberOfImages getNumberOfImages(boolean estimate) throws IOException
	{
		// Find the first IFD
		long ifdOffset = readUnsignedInt();
		if (ifdOffset < 8L)
			return NO_IMAGES;

		// Try and read the Index map offset header.
		// See https://micro-manager.org/wiki/Micro-Manager_File_Formats
		int nImages = readIndexMapNumberOfEntries();
		if (nImages > 0)
			return new NumberOfImages(nImages);

		// So the index map is not present. We must read the IFDs.
		// We do not care about the actual IFD contents. Just the count.

		// Open the first IFD looking for information about the number of images

		ss.seek(ifdOffset);
		int ifdCount = scanFirstIFD();

		if (ifdCount < 0)
			return NO_IMAGES;

		// If an ImageJ image then the nImages is written to the description
		if (ifdCount > 1)
			return new NumberOfImages(ifdCount);

		ifdOffset = readUnsignedInt();

		if (ifdOffset <= 0L)
			return ONE_IMAGE;

		if (estimate)
		{
			// If not an ImageJ image then we get the first and next IFD size and the 
			// size of the raw pixels

			int imageSize = getPixelSize();

			// The IFD table entries were read into the buffer so don't re-read.
			long ifdSize1 = getIFDSize(false);

			// Read the next IFD size
			ss.seek(ifdOffset);
			long ifdSize2 = getIFDSize(true);

			long fileSize = new File(directory, name).length();

			// Get an estimate of the number of frames after the first frame which has the biggest IFD.
			// This assumes all the remaining IFDs (and images) are the same size.
			// The 8 bytes is for the standard TIFF image file header data.
			// We assume that the OME-TIFF header is not present since we could not read the index map.
			int n = 1 + (int) Math.round((double) (fileSize - imageSize - ifdSize1 - 8) / (imageSize + ifdSize2));

			// Debug check
			long expected = ifdSize1 + (ifdSize2 * (n - 1)) + (long) imageSize * n + 8;
			double e = DoubleEquality.relativeError(fileSize, expected);
			//System.out.printf("Actual = %d, Expected = %d, error = %s (%f)\n", fileSize, expected, expected - fileSize,
			//		e);
			return new NumberOfImages(n, e);
		}
		else
		{
			// If not an ImageJ image then we have to read each IFD
			ifdCount = 1;

			while (ifdOffset > 0L)
			{
				ss.seek(ifdOffset);

				if (!scanIFD())
				{
					//System.out.println("No more IFDs");
					break;
				}

				ifdCount++;
				ifdOffset = readUnsignedInt();
			}

			return new NumberOfImages(ifdCount);
		}
	}

	/**
	 * Read the index map number of entries. This is written to the end of the file. It may be missing if the system did
	 * not finish writing the file.
	 *
	 * @return the number of entries in the index map
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private int readIndexMapNumberOfEntries() throws IOException
	{
		int indexOffsetHeader = readInt();
		if (indexOffsetHeader == 54773648)
		{
			long indexOffset = readUnsignedInt();
			try
			{
				ss.seek(indexOffset);
				// Check the header
				if (readInt() == 3453623)
				{
					// This is the index map
					int n = readInt();
					//System.out.printf("Index map entries = %d\n", n);
					return n;
				}
			}
			catch (IOException e)
			{
				return 0;
			}
		}
		return 0;
	}

	/**
	 * A class for holding the index map for images in an OME-TIFF file.
	 */
	public class IndexMap
	{
		private final int[] map;

		/** The size of the map. */
		public final int size;

		private int[][] limits;

		private IndexMap(int[] map)
		{
			this.map = map;
			size = map.length / 5;
		}

		public int getChannelIndex(int i)
		{
			checkIndex(i);
			return map[i * 5];
		}

		public int getSliceIndex(int i)
		{
			checkIndex(i);
			return map[i * 5 + 1];
		}

		public int getFrameIndex(int i)
		{
			checkIndex(i);
			return map[i * 5 + 2];
		}

		public int getPositionIndex(int i)
		{
			checkIndex(i);
			return map[i * 5 + 3];
		}

		public long getOffset(int i)
		{
			checkIndex(i);
			return (long) map[i * 5 + 4] & 0xffffffffL;
		}

		private void checkIndex(int i)
		{
			if (i < 0 || i >= size)
				throw new ArrayIndexOutOfBoundsException(i);
		}

		public int getMinChannelIndex()
		{
			createLimits();
			return limits[0][0];
		}

		public int getMaxChannelIndex()
		{
			createLimits();
			return limits[0][1];
		}

		public int getNChannels()
		{
			createLimits();
			return limits[0][1] - limits[0][0] + 1;
		}

		public int getMinSliceIndex()
		{
			createLimits();
			return limits[1][0];
		}

		public int getMaxSliceIndex()
		{
			createLimits();
			return limits[1][1];
		}

		public int getNSlices()
		{
			createLimits();
			return limits[1][1] - limits[1][0] + 1;
		}

		public int getMinFrameIndex()
		{
			createLimits();
			return limits[2][0];
		}

		public int getMaxFrameIndex()
		{
			createLimits();
			return limits[2][1];
		}

		public int getNFrames()
		{
			createLimits();
			return limits[2][1] - limits[2][0] + 1;
		}

		public int getMinPositionIndex()
		{
			createLimits();
			return limits[3][0];
		}

		public int getMaxPositionIndex()
		{
			createLimits();
			return limits[3][1];
		}

		public int getNPositions()
		{
			createLimits();
			return limits[3][1] - limits[3][0] + 1;
		}

		private void createLimits()
		{
			if (limits == null)
			{
				// Ignore the offset as finding the min/max and range of this
				// is of little value
				int[][] limits = new int[4][2];
				for (int j = 0; j < 4; j++)
				{
					limits[j][0] = limits[j][1] = map[j];
				}
				for (int i = 0; i < size; i++)
				{
					for (int j = 0, k = i * 5; j < 4; j++, k++)
					{
						// Min
						if (limits[j][0] > map[k])
							limits[j][0] = map[k];
						else
						// Max
						if (limits[j][1] < map[k])
							limits[j][1] = map[k];
					}
				}
				//System.out.printf("Z=%d, C=%d, T=%d, P=%d\n", 
				//		limits[0][1] - limits[0][0] + 1, 
				//		limits[1][1] - limits[1][0] + 1, 
				//		limits[2][1] - limits[2][0] + 1,
				//i		limits[3][1] - limits[3][0] + 1);
				this.limits = limits;
			}
		}

		public boolean isSingleChannel()
		{
			final int test = map[0];
			for (int i = 5; i < map.length; i += 5)
				if (map[i] != test)
					return false;
			return true;
		}

		public boolean isSingleSlice()
		{
			final int test = map[1];
			for (int i = 6; i < map.length; i += 5)
				if (map[i] != test)
					return false;
			return true;
		}

		public boolean isSingleFrame()
		{
			final int test = map[2];
			for (int i = 7; i < map.length; i += 5)
				if (map[i] != test)
					return false;
			return true;
		}

		public boolean isSinglePosition()
		{
			final int test = map[3];
			for (int i = 8; i < map.length; i += 5)
				if (map[i] != test)
					return false;
			return true;
		}
	}

	/**
	 * Gets the index map.
	 *
	 * @return the index map (or null)
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @see https://micro-manager.org/wiki/Micro-Manager_File_Formats
	 */
	public IndexMap getIndexMap() throws IOException
	{
		ss.seek(8L);
		if (readInt() != 54773648)
			// Not the correct index map offset header
			return null;

		long indexOffset = readUnsignedInt();
		ss.seek(indexOffset);
		// Check the header
		if (readInt() != 3453623)
			return null;

		// This is the index map so get the number of entries
		int n = readInt();
		if (n <= 0)
			return null;

		int[] map = new int[n * 5]; // 5 integers per entry

		// Do this in chunks
		byte[] buffer = allocateBuffer(4096);
		int i = 0;
		while (i < map.length)
		{
			int read = Math.min(4096, (map.length - i) * 4);
			if (ss.readBytes(buffer, read) != read)
				return null;
			for (int j = 0; j < read; j += 4)
				map[i++] = getInt(buffer, j);
		}
		return new IndexMap(map);
	}

	/** The size of the IFD index standard data in bytes (short+short+int+int) */
	private final static int INDEX_SIZE = 2 + 2 + 4 + 4; // 

	/**
	 * Scan the first IFD for the number of images written by ImageJ to a tiff description tag
	 *
	 * @return the number of images (-1 on error)
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private int scanFirstIFD() throws IOException
	{
		// Get Image File Directory data
		nEntries = readShort();
		if (nEntries < 1 || nEntries > 1000)
			return -1;

		// Read the index data in one operation. 
		// Any tag data is read by using a seek operation and then reset to the current position.
		int size = nEntries * INDEX_SIZE;
		byte[] buffer = allocateBuffer(size);
		if (ss.readBytes(buffer, size) != size)
			return -1;

		for (int i = 0, j = 0; i < nEntries; i++, j += INDEX_SIZE)
		{
			// We are only interested in any fields that specify the nImages
			int tag = getShort(buffer, j);

			// Note: 
			// NIH_IMAGE_HDR does contain nImages for GRAY8 or COLOR8.
			// We don't read those tags so don't support this.

			// METAMORPH2 contains the nImages if compression is ExtendedFileInfo.COMPRESSION_NONE
			// but we don't bother reading that tag

			// IPLAB contains the nImages. We will not encounter those.

			// Just support extracting the nImages from the description
			if (tag == IMAGE_DESCRIPTION)
			{
				int fieldType = getShort(buffer, j + 2);
				int count = getInt(buffer, j + 4);
				int value = getValue(fieldType, count, buffer, j + 8);
				long lvalue = ((long) value) & 0xffffffffL;
				byte[] s = getString(count, lvalue);

				// It is possible that there are multiple IMAGE_DESCRIPTION tags
				// e.g. MicroManager OME-TIFF format, so we have to read them all
				if (s != null)
				{
					int n = getImageJnImages(new String(s));
					if (n != 0)
						return n;
				}
			}

			// Tags are sorted in order
			if (tag > IMAGE_DESCRIPTION)
				break;
		}
		return 0;
	}

	@SuppressWarnings("unused")
	private int getValue(int fieldType, int count, int b1, int b2, int b3, int b4)
	{
		int value = 0;
		if (fieldType == SHORT && count == 1)
			value = getShort(b1, b2);
		else
			value = getInt(b1, b2, b3, b4);
		return value;
	}

	private int getValue(int fieldType, int count, byte[] buffer, int offset)
	{
		int value = 0;
		if (fieldType == SHORT && count == 1)
			value = getShort(buffer, offset);
		else
			value = getInt(buffer, offset);
		return value;
	}

	/**
	 * Scan the IFD.
	 *
	 * @return true, if a valid IFD
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private boolean scanIFD() throws IOException
	{
		// Get Image File Directory data
		int nEntries = readShort();
		//System.out.println("nEntries = " + nEntries);
		if (nEntries < 1 || nEntries > 1000)
			return false;
		// Skip all the index data: tag, fieldType, count, value
		//in.skip(nEntries * INDEX_SIZE);
		ss.seek(ss.getFilePointer() + nEntries * INDEX_SIZE);
		return true;
	}

	/**
	 * ImageJ saves the number of images for stacks in the TIFF description tag to avoid having to
	 * decode an IFD for each image.
	 */
	public static void saveImageJnImages(byte[] description, ExtendedFileInfo fi)
	{
		String id = new String(description);
		fi.description = id;
		int n = getImageJnImages(id);
		if (n > 1)
			fi.nImages = n;
	}

	/**
	 * ImageJ saves the number of images for stacks in the TIFF description tag to avoid having to
	 * decode an IFD for each image.
	 *
	 * @param id
	 *            the description tage
	 * @return the number of images (if above 1) else 0
	 */
	public static int getImageJnImages(String id)
	{
		if (id.length() > 7)
		{
			int index1 = id.indexOf("images=");
			if (index1 > 0)
			{
				int index2 = id.indexOf("\n", index1);
				if (index2 > 0)
				{
					String images = id.substring(index1 + 7, index2);
					int n = (int) Tools.parseDouble(images, 0.0);
					if (n > 1)
						return n;
				}
			}
		}
		return 0;
	}

	/**
	 * Get the size of the Image File Directory data.
	 *
	 * @param readTable
	 *            the read table
	 * @return the IFD size
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private long getIFDSize(boolean readTable) throws IOException
	{
		if (readTable)
		{
			nEntries = readShort();
			if (nEntries < 1 || nEntries > 1000)
				return 0;

			// Read the index data in one operation. 
			// Any tag data is read by using a seek operation and then reset to the current position.
			int size = nEntries * INDEX_SIZE;
			byte[] buffer = allocateBuffer(size);
			if (ss.readBytes(buffer, size) != size)
				return 0;
		}

		// This makes a rough guess using the IFD data, see here:
		// http://www.fileformat.info/format/tiff/corion.htm	
		// It will be wrong if the IFD field contents contain further pointers to 
		// other parts of the file.

		// Includes (number of entries) + (next offset)
		long total = 2 + nEntries * INDEX_SIZE + 4;
		for (int i = 0, j = 0; i < nEntries; i++, j += INDEX_SIZE)
		{
			int tag = getShort(buffer, j);
			int fieldType = getShort(buffer, j + 2);
			int count = getInt(buffer, j + 4);

			//if (debugMode)
			//{
			//	long lvalue = ((long) getValue(fieldType, count, buffer[j + 8] & 0xff, buffer[j + 9] & 0xff,
			//			buffer[j + 10] & 0xff, buffer[j + 11] & 0xff)) & 0xffffffffL;
			//	String name = getName(tag);
			//	System.out.printf("%d, %d (%s), %d, %d, %s\n", tag, fieldType, getFieldTypeName(fieldType), count,
			//			lvalue, name);
			//	if (fieldType == ASCII_STRING)
			//	{
			//		byte[] bytes = getString(count, lvalue);
			//		if (bytes != null)
			//		{
			//			String s = new String(bytes);
			//			System.out.println(s.substring(0, (Math.min(100, s.length()))));
			//		}
			//	}
			//}

			// If the data size is less or equal to
			// 4 bytes (determined by the field type and
			// length), then this offset is not a offset
			// but instead the data itself, to save space.
			// So check if count is above 1 or the data size
			// is bigger than 4 bytes.
			if (count > 1)
			{
				total += getFieldTypeSize(fieldType) * count;
			}
			else if (fieldType == 5)
			{
				// rational (2 dwords, numerator and denominator)
				total += 8;
			}

			// Special case to count the size of all the metadata
			if (tag == META_DATA_BYTE_COUNTS)
			{
				long saveLoc = ss.getFilePointer();
				int value = getValue(fieldType, count, buffer, j + 8);
				long lvalue = ((long) value) & 0xffffffffL;
				ss.seek(lvalue);
				for (int c = 0; c < count; c++)
					total += readInt();
				ss.seek(saveLoc);
			}
		}

		return total;
	}

	/**
	 * Gets the field type name for the TIFF IFD field type.
	 *
	 * @param fieldType
	 *            the field type
	 * @return the field type name
	 */
	public static String getFieldTypeName(int fieldType)
	{
		switch (fieldType)
		{
			case BYTE:
				return "byte";
			case ASCII_STRING:
				return "ASCII string";
			case WORD:
				return "word";
			case DWORD:
				return "dword";
			case RATIONAL:
				return "rational";
		}
		return "unknown";
	}

	/**
	 * Gets the field type size for the TIFF IFD field type.
	 *
	 * @param fieldType
	 *            the field type
	 * @return the field size (in bytes)
	 */
	public static int getFieldTypeSize(int fieldType)
	{
		switch (fieldType)
		{
			case BYTE:
				return 1;
			case ASCII_STRING:
				return 1;
			case WORD:
				return 2;
			case DWORD: // Also UWORD
				return 4;
			case RATIONAL:
				return 8; // 2 dwords, numerator and denominator
		}
		System.out.printf("unknown IFD field size for field type: %d\n", fieldType);
		return 1; // It has to have some size
	}

	/**
	 * Gets the pixel size. This assumes 1 IFD has been read into the IFD buffer.
	 *
	 * @return the pixel size
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private int getPixelSize() throws IOException
	{
		int width = 0, height = 0, samplesPerPixel = 0, fileType = 0, compression = 0;

		for (int i = 0, j = 0; i < nEntries; i++, j += INDEX_SIZE)
		{
			// We are only interested in any fields that specify the nImages
			int tag = getShort(buffer, j);
			int fieldType = getShort(buffer, j + 2);
			int count = getInt(buffer, j + 4);
			int value = getValue(fieldType, count, buffer, j + 8);
			long lvalue = ((long) value) & 0xffffffffL;

			switch (tag)
			{
				case IMAGE_WIDTH:
					width = value;
					break;
				case IMAGE_LENGTH:
					height = value;
					break;
				case BITS_PER_SAMPLE:
					if (count == 1)
					{
						if (value == 8)
							fileType = ExtendedFileInfo.GRAY8;
						else if (value == 16)
							fileType = ExtendedFileInfo.GRAY16_UNSIGNED;
						else if (value == 32)
							fileType = ExtendedFileInfo.GRAY32_INT;
						else if (value == 12)
							fileType = ExtendedFileInfo.GRAY12_UNSIGNED;
						else if (value == 1)
							fileType = ExtendedFileInfo.BITMAP;
						else
							error("Unsupported BitsPerSample: " + value);
					}
					else if (count > 1)
					{
						long saveLoc = ss.getFilePointer();
						ss.seek(lvalue);
						int bitDepth = readShort();
						if (bitDepth == 8)
							fileType = ExtendedFileInfo.GRAY8;
						else if (bitDepth == 16)
							fileType = ExtendedFileInfo.GRAY16_UNSIGNED;
						else
							error("ImageJ can only open 8 and 16 bit/channel images (" + bitDepth + ")");
						ss.seek(saveLoc);
					}
					break;
				case SAMPLES_PER_PIXEL:
					samplesPerPixel = value;
					if (value == 3 && fileType == ExtendedFileInfo.GRAY8)
						fileType = ExtendedFileInfo.RGB;
					else if (value == 3 && fileType == ExtendedFileInfo.GRAY16_UNSIGNED)
						fileType = ExtendedFileInfo.RGB48;
					else if (value == 4 && fileType == ExtendedFileInfo.GRAY8)
						fileType = photoInterp == 5 ? ExtendedFileInfo.CMYK : ExtendedFileInfo.ARGB;
					else if (value == 4 && fileType == ExtendedFileInfo.GRAY16_UNSIGNED)
					{
						fileType = ExtendedFileInfo.RGB48;
					}
					break;

				case PLANAR_CONFIGURATION: // 1=chunky, 2=planar
					if (value == 2 && fileType == ExtendedFileInfo.RGB48)
						fileType = ExtendedFileInfo.RGB48_PLANAR;
					else if (value == 2 && fileType == ExtendedFileInfo.RGB)
						fileType = ExtendedFileInfo.RGB_PLANAR;
					else if (value != 2 && !(samplesPerPixel == 1 || samplesPerPixel == 3 || samplesPerPixel == 4))
					{
						String msg = "Unsupported SamplesPerPixel: " + samplesPerPixel;
						error(msg);
					}
					break;
				case COMPRESSION:
					if (value == 5)
					{// LZW compression
						compression = ExtendedFileInfo.LZW;
						if (fileType == ExtendedFileInfo.GRAY12_UNSIGNED)
							error("ImageJ cannot open 12-bit LZW-compressed TIFFs");
					}
					else if (value == 32773) // PackBits compression
						compression = ExtendedFileInfo.PACK_BITS;
					else if (value == 32946 || value == 8)
						compression = ExtendedFileInfo.ZIP;
					else if (value != 1 && value != 0 && !(value == 7 && width < 500))
					{
						// don't abort with Spot camera compressed (7) thumbnails
						// otherwise, this is an unknown compression type
						compression = ExtendedFileInfo.COMPRESSION_UNKNOWN;
						error("ImageJ cannot open TIFF files " + "compressed in this fashion (" + value + ")");
					}
					break;
				case TILE_WIDTH:
					error("ImageJ cannot open tiled TIFFs.\nTry using the Bio-Formats plugin.");
					break;
				case SAMPLE_FORMAT:
					if (fileType == ExtendedFileInfo.GRAY32_INT && value == FLOATING_POINT)
						fileType = ExtendedFileInfo.GRAY32_FLOAT;
					if (fileType == ExtendedFileInfo.GRAY16_UNSIGNED)
					{
						if (value == SIGNED)
							fileType = ExtendedFileInfo.GRAY16_SIGNED;
						if (value == FLOATING_POINT)
							error("ImageJ cannot open 16-bit float TIFFs");
					}
					break;
				case JPEG_TABLES:
					if (compression == ExtendedFileInfo.JPEG)
						error("Cannot open JPEG-compressed TIFFs with separate tables");
					break;
			}
		}
		int size = getBytesPerImage(width, height, fileType);
		if (size != 0 && compression <= ExtendedFileInfo.COMPRESSION_NONE)
		{
			return size;
		}
		error("Cannot estimate TIFF image size");
		return 0; // 
	}

	public static int getBytesPerImage(int width, int height, int fileType)
	{
		int size = width * height;
		switch (fileType)
		{
			case ExtendedFileInfo.GRAY8:
			case ExtendedFileInfo.COLOR8:
			case ExtendedFileInfo.BITMAP:
				return size;
			case ExtendedFileInfo.GRAY12_UNSIGNED:
				return (int) (1.5 * size);
			case ExtendedFileInfo.GRAY16_SIGNED:
			case ExtendedFileInfo.GRAY16_UNSIGNED:
				return 2 * size;
			case ExtendedFileInfo.GRAY24_UNSIGNED:
				return 3 * size;
			case ExtendedFileInfo.GRAY32_INT:
			case ExtendedFileInfo.GRAY32_UNSIGNED:
			case ExtendedFileInfo.GRAY32_FLOAT:
				return 4 * size;
			case ExtendedFileInfo.GRAY64_FLOAT:
				return 8 * size;
			case ExtendedFileInfo.ARGB:
			case ExtendedFileInfo.BARG:
			case ExtendedFileInfo.ABGR:
			case ExtendedFileInfo.CMYK:
				return 4 * size;
			case ExtendedFileInfo.RGB:
			case ExtendedFileInfo.RGB_PLANAR:
			case ExtendedFileInfo.BGR:
				return 3 * size;
			case ExtendedFileInfo.RGB48:
				return 6 * size;
			case ExtendedFileInfo.RGB48_PLANAR:
				return 2 * size;
			default:
				return 0;
		}
	}

	/**
	 * Gets the origin from the FileInfo. This is read using the summaryMetaData field looking for a JSON tag of
	 * "ROI": [x,y,w,h] or the info or extendedMetaData fields using a tag of "ROI": "x-y-w-h". These tags are used by
	 * MircoManager and saved to the appropriate fields of the ExtendedFileInfo object by this decoder.
	 *
	 * @param fi
	 *            the file info
	 * @return the origin
	 */
	public static Rectangle getOrigin(ExtendedFileInfo fi)
	{
		Rectangle r = getOrigin(fi.summaryMetaData, '[', ',', ']');
		if (r != null)
			return r;
		r = getOrigin(fi.info, '"', '-', '"');
		if (r != null)
			return r;
		return getOrigin(fi.extendedMetaData, '"', '-', '"');
	}

	/**
	 * Gets the origin from the text. This looks for a tag of "ROI": AxByBwBhC where A is the start character, B is the
	 * delimiter and C is the end character, x=x-origin, y=y-origin, w=width, h=height. Only [ :] are allowed between
	 * the "ROI" tag and the start character.
	 * <p>
	 * This tag is used by MicroManager IFD description metadata. This is saved in to the FileInfo.info field by this
	 * decoder.
	 *
	 * @param text
	 *            the summary meta data
	 * @return the origin
	 */
	public static Rectangle getOrigin(String text, char start, char delimiter, char end)
	{
		if (text == null)
			return null;

		// The tag is length 5
		if (text.length() < 5)
			return null;

		int tagIndex = text.indexOf("\"ROI\"");
		if (tagIndex < 0)
			return null;

		// Skip over the tag
		tagIndex += 5;
		int startIndex = text.indexOf(start, tagIndex);
		if (startIndex < 0)
			return null;

		// Check that between index and i there is only space/colon
		for (int k = tagIndex; k < startIndex; k++)
		{
			// This has index checking which could be avoided by extracting the chars
			// in a single call. However we expect the tag match to be fairly unique to 
			// this situation and so the number of chars to the start character will be 
			// small.
			char c = text.charAt(k);
			if (!(c == ':' || c == ' '))
				return null;
		}

		// Skip the actual start character
		startIndex++;
		int endIndex = text.indexOf(end, startIndex);
		if (endIndex < 0)
			return null;

		// Extract the x,y,w,h from with the start and end characters
		char[] chars = new char[endIndex - startIndex];
		text.getChars(startIndex, endIndex, chars, 0);

		// This should contain only digits and 3 delimiters
		int[] index = new int[3];
		int digits = 0;
		int count = 0;
		for (int k = 0; k < chars.length; k++)
		{
			if (Character.isDigit(chars[k]))
			{
				digits++;
				continue;
			}
			if (chars[k] == delimiter)
			{
				if (digits == 0 || count == 3)
					// No digits before the comma or too many commas
					return null;
				index[count++] = k;
				digits = 0;
				continue;
			}
			// Anything else is not allowed
			return null;
		}
		// Must have 3 commas and a digit after the last comma 
		if (count != 3 || digits == 0)
			return null;

		// We have the indices of the commas to extract the ROI
		int x = Integer.parseInt(new String(chars, 0, index[0]));
		int y = Integer.parseInt(new String(chars, index[0] + 1, index[1] - index[0] - 1));
		int w = Integer.parseInt(new String(chars, index[1] + 1, index[2] - index[1] - 1));
		int h = Integer.parseInt(new String(chars, index[2] + 1, chars.length - index[2] - 1));

		return new Rectangle(x, y, w, h);
	}

	/**
	 * Sets the track progress used for monitoring the progress of method execution.
	 *
	 * @param p
	 *            the new track progress
	 */
	public void setTrackProgress(TrackProgress p)
	{
		this.trackProgress = NullTrackProgress.createIfNull(p);
	}
}
