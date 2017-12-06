package ij.io;

import java.awt.Rectangle;
import java.io.File;
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
public class FastTiffDecoder
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
	protected SeekableStream in;
	protected boolean debugMode;
	private boolean littleEndian;
	private String dInfo;
	private int ifdCount;
	private int[] metaDataCounts;
	private String tiffMetadata;
	private int photoInterp;
	private int nEntries;
	private byte[] buffer;

	private TrackProgress trackProgress = NullTrackProgress.INSTANCE;

	/**
	 * This is a count of the number of IFD for which the micro manager metadata will be read. This metadata can be
	 * large and so by default the count is 0 to prevent reading it when it is not wanted.
	 */
	public int ifdCountForMicroManagerMetadata = 0;

	public FastTiffDecoder(String directory, String name)
	{
		this.directory = directory;
		this.name = name;
	}

	public FastTiffDecoder(InputStream in, String name)
	{
		if (in == null)
			throw new NullPointerException();
		directory = "";
		this.name = name;
		this.in = new MemoryCacheSeekableStream(in);
	}

	public FastTiffDecoder(SeekableStream in, String name)
	{
		if (in == null)
			throw new NullPointerException();
		directory = "";
		this.name = name;
		this.in = in;
	}

	public FastTiffDecoder(File file) throws IOException
	{
		if (file == null)
			throw new NullPointerException();
		this.name = file.getCanonicalPath();
	}

	final int getInt() throws IOException
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

	final long getUnsignedInt() throws IOException
	{
		return (long) getInt() & 0xffffffffL;
	}

	final int getShort() throws IOException
	{
		int b1 = in.read();
		int b2 = in.read();
		if (littleEndian)
			return ((b2 << 8) + b1);
		else
			return ((b1 << 8) + b2);
	}

	private final long readLong() throws IOException
	{
		if (littleEndian)
			return ((long) getInt() & 0xffffffffL) + ((long) getInt() << 32);
		else
			return ((long) getInt() << 32) + ((long) getInt() & 0xffffffffL);
		//return in.read()+(in.read()<<8)+(in.read()<<16)+(in.read()<<24)+(in.read()<<32)+(in.read()<<40)+(in.read()<<48)+(in.read()<<56);
	}

	private final double readDouble() throws IOException
	{
		return Double.longBitsToDouble(readLong());
	}

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
			in.close();
			return -1;
		}
		// Magic number
		if (getShort() != 42)
			return -1; // Not a TIFF
		return getUnsignedInt();
	}

	void getColorMap(long offset, ExtendedFileInfo fi) throws IOException
	{
		byte[] colorTable16 = new byte[768 * 2];
		long saveLoc = in.getFilePointer();
		in.seek(offset);
		in.readFully(colorTable16);
		in.seek(saveLoc);
		fi.lutSize = 256;
		fi.reds = new byte[256];
		fi.greens = new byte[256];
		fi.blues = new byte[256];
		int j = 0;
		if (littleEndian)
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
		long saveLoc = in.getFilePointer();
		in.seek(offset);
		in.readFully(bytes);
		in.seek(saveLoc);
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
		long saveLoc = in.getFilePointer();

		in.seek(offset + 12);
		int version = in.readShort();

		in.seek(offset + 160);
		double scale = in.readDouble();
		if (version > 106 && scale != 0.0)
		{
			fi.pixelWidth = 1.0 / scale;
			fi.pixelHeight = fi.pixelWidth;
		}

		// spatial calibration
		in.seek(offset + 172);
		int units = in.readShort();
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
		in.seek(offset + 182);
		int fitType = in.read();
		//int unused = 
		in.read();
		int nCoefficients = in.readShort();
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
				fi.coefficients[i] = in.readDouble();
			}
			in.seek(offset + 234);
			int size = in.read();
			StringBuffer sb = new StringBuffer();
			if (size >= 1 && size <= 16)
			{
				for (int i = 0; i < size; i++)
					sb.append((char) (in.read()));
				fi.valueUnit = new String(sb);
			}
			else
				fi.valueUnit = " ";
		}

		in.seek(offset + 260);
		int nImages = in.readShort();
		if (nImages >= 2 && (fi.fileType == ExtendedFileInfo.GRAY8 || fi.fileType == ExtendedFileInfo.COLOR8))
		{
			fi.nImages = nImages;
			fi.pixelDepth = in.readFloat(); //SliceSpacing
			//int skip = 
			in.readShort(); //CurrentSlice
			fi.frameInterval = in.readFloat();
			//ij.IJ.write("fi.pixelDepth: "+fi.pixelDepth);
		}

		in.seek(offset + 272);
		float aspectRatio = in.readFloat();
		if (version > 140 && aspectRatio != 0.0)
			fi.pixelHeight = fi.pixelWidth / aspectRatio;

		in.seek(saveLoc);
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
		long saveLoc = in.getFilePointer();
		in.seek(loc);
		double numerator = getUnsignedInt();
		double denominator = getUnsignedInt();
		in.seek(saveLoc);
		if (denominator != 0.0)
			return numerator / denominator;
		else
			return 0.0;
	}

	private ExtendedFileInfo openIFD() throws IOException
	{
		// Get Image File Directory data
		nEntries = getShort();
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
		int read = in.readFully(buffer, size);
		if (read != size)
			return null;

		for (int i = 0, j = 0; i < nEntries; i++, j += INDEX_SIZE)
		{
			int tag = getShort(buffer[j] & 0xff, buffer[j + 1] & 0xff);
			int fieldType = getShort(buffer[j + 2] & 0xff, buffer[j + 3] & 0xff);
			int count = getInt(buffer[j + 4] & 0xff, buffer[j + 5] & 0xff, buffer[j + 6] & 0xff, buffer[j + 7] & 0xff);
			int value = getValue(fieldType, count, buffer[j + 8] & 0xff, buffer[j + 9] & 0xff, buffer[j + 10] & 0xff,
					buffer[j + 11] & 0xff);
			long lvalue = ((long) value) & 0xffffffffL;
			if (debugMode && ifdCount < 10)
				dumpTag(tag, count, value, fi);
			//ij.IJ.write(i+"/"+nEntries+" "+tag + ", count=" + count + ", value=" + value);
			//if (tag==0) return null;
			switch (tag)
			{
				case IMAGE_WIDTH:
					fi.width = value;
					fi.intelByteOrder = littleEndian;
					break;
				case IMAGE_LENGTH:
					fi.height = value;
					break;
				case STRIP_OFFSETS:
					if (count == 1)
						fi.stripOffsets = new int[] { value };
					else
					{
						long saveLoc = in.getFilePointer();
						in.seek(lvalue);
						fi.stripOffsets = new int[count];
						for (int c = 0; c < count; c++)
							fi.stripOffsets[c] = getInt();
						in.seek(saveLoc);
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
						long saveLoc = in.getFilePointer();
						in.seek(lvalue);
						fi.stripLengths = new int[count];
						for (int c = 0; c < count; c++)
						{
							if (fieldType == SHORT)
								fi.stripLengths[c] = getShort();
							else
								fi.stripLengths[c] = getInt();
						}
						in.seek(saveLoc);
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
						long saveLoc = in.getFilePointer();
						in.seek(lvalue);
						int bitDepth = getShort();
						if (bitDepth == 8)
							fi.fileType = ExtendedFileInfo.GRAY8;
						else if (bitDepth == 16)
							fi.fileType = ExtendedFileInfo.GRAY16_UNSIGNED;
						else
							error("ImageJ can only open 8 and 16 bit/channel images (" + bitDepth + ")");
						in.seek(saveLoc);
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
					long saveLoc = in.getFilePointer();
					in.seek(lvalue);
					metaDataCounts = new int[count];
					for (int c = 0; c < count; c++)
						metaDataCounts[c] = getInt();
					in.seek(saveLoc);
					break;
				case META_DATA:
					getMetaData(value, fi);
					break;
				case MICRO_MANAGER_META_DATA:
					if (ifdCount < ifdCountForMicroManagerMetadata)
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
		long saveLoc = in.getFilePointer();
		in.seek(loc);
		int hdrSize = metaDataCounts[0];
		if (hdrSize < 12 || hdrSize > 804)
		{
			in.seek(saveLoc);
			return;
		}
		int magicNumber = getInt();
		if (magicNumber != MAGIC_NUMBER) // "IJIJ"
		{
			in.seek(saveLoc);
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
			types[i] = getInt();
			counts[i] = getInt();
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
					in.readFully(fi.metaData[eMDindex], len);
					fi.metaDataTypes[eMDindex] = types[i];
					eMDindex++;
				}
			}
			else
				skipUnknownType(start, start + counts[i] - 1);
			start += counts[i];
		}
		in.seek(saveLoc);
	}

	void getInfoProperty(int first, ExtendedFileInfo fi) throws IOException
	{
		int len = metaDataCounts[first];
		byte[] buffer = new byte[len];
		in.readFully(buffer, len);
		len /= 2;
		char[] chars = new char[len];
		if (littleEndian)
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
				in.readFully(buffer, len);
				len /= 2;
				char[] chars = new char[len];
				if (littleEndian)
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
			in.readFully(fi.channelLuts[index], len);
			index++;
		}
	}

	void getRoi(int first, ExtendedFileInfo fi) throws IOException
	{
		int len = metaDataCounts[first];
		fi.roi = new byte[len];
		in.readFully(fi.roi, len);
	}

	void getOverlay(int first, int last, ExtendedFileInfo fi) throws IOException
	{
		fi.overlay = new byte[last - first + 1][];
		int index = 0;
		for (int i = first; i <= last; i++)
		{
			int len = metaDataCounts[i];
			fi.overlay[index] = new byte[len];
			in.readFully(fi.overlay[index], len);
			index++;
		}
	}

	void error(String message) throws IOException
	{
		if (in != null)
			in.close();
		throw new IOException(message);
	}

	void skipUnknownType(int first, int last) throws IOException
	{
		byte[] buffer = new byte[metaDataCounts[first]];
		for (int i = first; i <= last; i++)
		{
			int len = metaDataCounts[i];
			if (len > buffer.length)
				buffer = new byte[len];
			in.readFully(buffer, len);
		}
	}

	public void enableDebugging()
	{
		debugMode = true;
	}

	public ExtendedFileInfo[] getTiffInfo() throws IOException
	{
		long ifdOffset;
		TurboList<ExtendedFileInfo> list = new TurboList<ExtendedFileInfo>();
		if (in == null)
			in = new FileSeekableStream(new File(directory, name));
		ifdOffset = openImageFileHeader();
		if (ifdOffset < 0L)
		{
			in.close();
			return null;
		}
		if (debugMode)
			dInfo = "\n  " + name + ": opening\n";
		while (ifdOffset > 0L)
		{
			in.seek(ifdOffset);
			ExtendedFileInfo fi = openIFD();
			if (fi != null)
			{
				list.add(fi);
				ifdOffset = getUnsignedInt();
			}
			else
				ifdOffset = 0L;
			if (debugMode && ifdCount < 10)
				dInfo += "  nextIFD=" + ifdOffset + "\n";
			if (fi != null && fi.nImages > 1)
				ifdOffset = 0L; // ignore extra IFDs in ImageJ and NIH Image stacks
		}
		if (list.size() == 0)
		{
			in.close();
			return null;
		}
		else
		{
			ExtendedFileInfo[] info = list.toArray(new ExtendedFileInfo[list.size()]);

			// Attempt to read the Micro-Manager summary metadata
			readMicroManagerSummaryMetadata(info[0]);

			in.close();

			if (debugMode)
				info[0].debugInfo = dInfo;
			if (info[0].info == null)
				info[0].info = tiffMetadata;
			ExtendedFileInfo fi = info[0];
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
			return info;
		}
	}

	private void readMicroManagerSummaryMetadata(ExtendedFileInfo fi) throws IOException
	{
		in.seek(32);
		if (getInt() == 2355492)
		{
			int count = getInt();
			byte[] bytes = new byte[count];
			in.readFully(bytes);
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
	 * Gets the number of images in the TIFF file. The class must have been created using a stream (not a file directory
	 * and name) since the stream is not opened or closed by calling this method. The stream will need
	 * to be reset to position 0 to use for reading IFDs.
	 *
	 * @param estimate
	 *            Flag to indicate that an estimate using file sizes is OK. The default is to read all the IFDs.
	 * @return the number of images
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public NumberOfImages getNumberOfImages(boolean estimate) throws IOException
	{
		if (in == null)
			throw new NullPointerException("No random access stream");

		in.seek(0);

		// Find the first IFD
		long ifdOffset = openImageFileHeader();
		if (ifdOffset < 0L)
			return NO_IMAGES;

		// Try and read the Index map offset header.
		// See https://micro-manager.org/wiki/Micro-Manager_File_Formats
		int nImages = readIndexMapNumberOfEntries();
		if (nImages > 0)
			return new NumberOfImages(nImages);

		// So the index map is not present. We must read the IFDs.
		// We do not care about the actual IFD contents. Just the count.

		// Open the first IFD looking for information about the number of images

		in.seek(ifdOffset);
		int ifdCount = scanFirstIFD();

		if (ifdCount < 0)
			return NO_IMAGES;

		// If an ImageJ image then the nImages is written to the description
		if (ifdCount > 1)
			return new NumberOfImages(ifdCount);

		ifdOffset = getUnsignedInt();

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
			in.seek(ifdOffset);
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
				in.seek(ifdOffset);

				if (!scanIFD())
				{
					//System.out.println("No more IFDs");
					break;
				}

				ifdCount++;
				ifdOffset = getUnsignedInt();
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
		int indexOffsetHeader = getInt();
		if (indexOffsetHeader == 54773648)
		{
			long indexOffset = getUnsignedInt();
			try
			{
				in.seek(indexOffset);
				// Check the header
				if (getInt() == 3453623)
				{
					// This is the index map
					int n = getInt();
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
		nEntries = getShort();
		if (nEntries < 1 || nEntries > 1000)
			return -1;

		// Read the index data in one operation. 
		// Any tag data is read by using a seek operation and then reset to the current position.
		int size = nEntries * INDEX_SIZE;
		byte[] buffer = allocateBuffer(size);
		int read = in.readFully(buffer, size);
		if (read != size)
			return -1;

		for (int i = 0, j = 0; i < nEntries; i++, j += INDEX_SIZE)
		{
			// We are only interested in any fields that specify the nImages
			int tag = getShort(buffer[j] & 0xff, buffer[j + 1] & 0xff);

			// Note: 
			// NIH_IMAGE_HDR does contain nImages for GRAY8 or COLOR8.
			// We don't read those tags so don't support this.

			// METAMORPH2 contains the nImages if compression is ExtendedFileInfo.COMPRESSION_NONE
			// but we don't bother reading that tag

			// IPLAB contains the nImages. We will not encounter those.

			// Just support extracting the nImages from the description
			if (tag == IMAGE_DESCRIPTION)
			{
				int fieldType = getShort(buffer[j + 2] & 0xff, buffer[j + 3] & 0xff);
				int count = getInt(buffer[j + 4] & 0xff, buffer[j + 5] & 0xff, buffer[j + 6] & 0xff,
						buffer[j + 7] & 0xff);
				int value = getValue(fieldType, count, buffer[j + 8] & 0xff, buffer[j + 9] & 0xff,
						buffer[j + 10] & 0xff, buffer[j + 11] & 0xff);
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

	private int getShort(int b1, int b2)
	{
		if (littleEndian)
			return ((b2 << 8) + b1);
		else
			return ((b1 << 8) + b2);
	}

	private int getInt(int b1, int b2, int b3, int b4)
	{
		if (littleEndian)
			return ((b4 << 24) + (b3 << 16) + (b2 << 8) + (b1 << 0));
		else
			return ((b1 << 24) + (b2 << 16) + (b3 << 8) + b4);
	}

	private int getValue(int fieldType, int count, int b1, int b2, int b3, int b4)
	{
		int value = 0;
		if (fieldType == SHORT && count == 1)
			value = getShort(b1, b2);
		else
			value = getInt(b1, b2, b3, b4);
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
		int nEntries = getShort();
		//System.out.println("nEntries = " + nEntries);
		if (nEntries < 1 || nEntries > 1000)
			return false;
		// Skip all the index data: tag, fieldType, count, value
		//in.skip(nEntries * INDEX_SIZE);
		in.seek(in.getFilePointer() + nEntries * INDEX_SIZE);
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
	 * http://www.fileformat.info/format/tiff/corion.htm
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
			nEntries = getShort();
			if (nEntries < 1 || nEntries > 1000)
				return 0;

			// Read the index data in one operation. 
			// Any tag data is read by using a seek operation and then reset to the current position.
			int size = nEntries * INDEX_SIZE;
			byte[] buffer = allocateBuffer(size);
			int read = in.readFully(buffer, size);
			if (read != size)
				return 0;
		}

		// Includes (number of entries) + (next offset)
		long total = 2 + nEntries * INDEX_SIZE + 4;
		for (int i = 0, j = 0; i < nEntries; i++, j += INDEX_SIZE)
		{
			int tag = getShort(buffer[j] & 0xff, buffer[j + 1] & 0xff);
			int fieldType = getShort(buffer[j + 2] & 0xff, buffer[j + 3] & 0xff);
			int count = getInt(buffer[j + 4] & 0xff, buffer[j + 5] & 0xff, buffer[j + 6] & 0xff, buffer[j + 7] & 0xff);

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
				long saveLoc = in.getFilePointer();
				int value = getValue(fieldType, count, buffer[j + 8] & 0xff, buffer[j + 9] & 0xff,
						buffer[j + 10] & 0xff, buffer[j + 11] & 0xff);
				long lvalue = ((long) value) & 0xffffffffL;
				in.seek(lvalue);
				for (int c = 0; c < count; c++)
					total += getInt();
				in.seek(saveLoc);
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
			int tag = getShort(buffer[j] & 0xff, buffer[j + 1] & 0xff);
			int fieldType = getShort(buffer[j + 2] & 0xff, buffer[j + 3] & 0xff);
			int count = getInt(buffer[j + 4] & 0xff, buffer[j + 5] & 0xff, buffer[j + 6] & 0xff, buffer[j + 7] & 0xff);
			int value = getValue(fieldType, count, buffer[j + 8] & 0xff, buffer[j + 9] & 0xff, buffer[j + 10] & 0xff,
					buffer[j + 11] & 0xff);
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
						long saveLoc = in.getFilePointer();
						in.seek(lvalue);
						int bitDepth = getShort();
						if (bitDepth == 8)
							fileType = ExtendedFileInfo.GRAY8;
						else if (bitDepth == 16)
							fileType = ExtendedFileInfo.GRAY16_UNSIGNED;
						else
							error("ImageJ can only open 8 and 16 bit/channel images (" + bitDepth + ")");
						in.seek(saveLoc);
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

	private static final String ROI_TAG = "ROI\":[";

	/**
	 * Gets the origin from the FileInfo. This is read using the summary meta-data looking for a JSON tag of
	 * "ROI":"[x,y,w,h]".
	 *
	 * @param fi
	 *            the file info
	 * @return the origin
	 */
	public static Rectangle getOrigin(ExtendedFileInfo fi)
	{
		if (fi.summaryMetaData == null)
			return null;

		int tagLength = ROI_TAG.length();
		if (fi.summaryMetaData.length() < tagLength)
			return null;

		int i = fi.summaryMetaData.indexOf(ROI_TAG);
		if (i < 0)
			return null;
		int j = fi.summaryMetaData.indexOf(']', i);
		if (j < 0)
			return null;

		// Extract the x,y,w,h from with the [] square brackets 
		i += tagLength;
		char[] chars = new char[j - i];
		fi.summaryMetaData.getChars(i, j, chars, 0);

		// This should contain only digits and 3 commas
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
			if (chars[k] == ',')
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
