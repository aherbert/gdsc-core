package ij.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

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
 * Re-implement the TiffDecoder to allow it to use a SeekableStream interface
 */
public class FastTiffDecoder
{
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
	public static final int META_DATA_BYTE_COUNTS = 50838; // private tag registered with Adobe
	public static final int META_DATA = 50839; // private tag registered with Adobe

	//constants
	static final int UNSIGNED = 1;
	static final int SIGNED = 2;
	static final int FLOATING_POINT = 3;

	//field types
	static final int SHORT = 3;
	static final int LONG = 4;

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
		//int magicNumber = 
		getShort(); // 42
		long offset = ((long) getInt()) & 0xffffffffL;
		return offset;
	}

	void getColorMap(long offset, FileInfo fi) throws IOException
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
		if (sum != 0 && fi.fileType == FileInfo.GRAY8)
			fi.fileType = FileInfo.COLOR8;
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
	 * Save the image description in the specified FileInfo. ImageJ
	 * saves spatial and density calibration data in this string. For
	 * stacks, it also saves the number of images to avoid having to
	 * decode an IFD for each image.
	 */
	public void saveImageDescription(byte[] description, FileInfo fi)
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

	void decodeNIHImageHeader(int offset, FileInfo fi) throws IOException
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
		if (nImages >= 2 && (fi.fileType == FileInfo.GRAY8 || fi.fileType == FileInfo.COLOR8))
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

	void dumpTag(int tag, int count, int value, FileInfo fi)
	{
		long lvalue = ((long) value) & 0xffffffffL;
		String name = getName(tag);
		String cs = (count == 1) ? "" : ", count=" + count;
		dInfo += "    " + tag + ", \"" + name + "\", value=" + lvalue + cs + "\n";
		//ij.IJ.log(tag + ", \"" + name + "\", value=" + value + cs + "\n");
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

	private FileInfo openIFD() throws IOException
	{
		// Get Image File Directory data
		nEntries = getShort();
		if (nEntries < 1 || nEntries > 1000)
			return null;
		ifdCount++;
		if ((ifdCount % 50) == 0 && ifdCount > 0)
			ij.IJ.showStatus("Opening IFDs: " + ifdCount);
		FileInfo fi = new FileInfo();
		fi.fileType = FileInfo.BITMAP; //BitsPerSample defaults to 1

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
							fi.fileType = FileInfo.GRAY8;
						else if (value == 16)
							fi.fileType = FileInfo.GRAY16_UNSIGNED;
						else if (value == 32)
							fi.fileType = FileInfo.GRAY32_INT;
						else if (value == 12)
							fi.fileType = FileInfo.GRAY12_UNSIGNED;
						else if (value == 1)
							fi.fileType = FileInfo.BITMAP;
						else
							error("Unsupported BitsPerSample: " + value);
					}
					else if (count > 1)
					{
						long saveLoc = in.getFilePointer();
						in.seek(lvalue);
						int bitDepth = getShort();
						if (bitDepth == 8)
							fi.fileType = FileInfo.GRAY8;
						else if (bitDepth == 16)
							fi.fileType = FileInfo.GRAY16_UNSIGNED;
						else
							error("ImageJ can only open 8 and 16 bit/channel images (" + bitDepth + ")");
						in.seek(saveLoc);
					}
					break;
				case SAMPLES_PER_PIXEL:
					fi.samplesPerPixel = value;
					if (value == 3 && fi.fileType == FileInfo.GRAY8)
						fi.fileType = FileInfo.RGB;
					else if (value == 3 && fi.fileType == FileInfo.GRAY16_UNSIGNED)
						fi.fileType = FileInfo.RGB48;
					else if (value == 4 && fi.fileType == FileInfo.GRAY8)
						fi.fileType = photoInterp == 5 ? FileInfo.CMYK : FileInfo.ARGB;
					else if (value == 4 && fi.fileType == FileInfo.GRAY16_UNSIGNED)
					{
						fi.fileType = FileInfo.RGB48;
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
					if (value == 2 && fi.fileType == FileInfo.RGB48)
						fi.fileType = FileInfo.RGB48_PLANAR;
					else if (value == 2 && fi.fileType == FileInfo.RGB)
						fi.fileType = FileInfo.RGB_PLANAR;
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
						fi.compression = FileInfo.LZW;
						if (fi.fileType == FileInfo.GRAY12_UNSIGNED)
							error("ImageJ cannot open 12-bit LZW-compressed TIFFs");
					}
					else if (value == 32773) // PackBits compression
						fi.compression = FileInfo.PACK_BITS;
					else if (value == 32946 || value == 8)
						fi.compression = FileInfo.ZIP;
					else if (value != 1 && value != 0 && !(value == 7 && fi.width < 500))
					{
						// don't abort with Spot camera compressed (7) thumbnails
						// otherwise, this is an unknown compression type
						fi.compression = FileInfo.COMPRESSION_UNKNOWN;
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
					if (value == 2 && fi.compression == FileInfo.LZW)
						fi.compression = FileInfo.LZW_WITH_DIFFERENCING;
					break;
				case COLOR_MAP:
					if (count == 768)
						getColorMap(lvalue, fi);
					break;
				case TILE_WIDTH:
					error("ImageJ cannot open tiled TIFFs.\nTry using the Bio-Formats plugin.");
					break;
				case SAMPLE_FORMAT:
					if (fi.fileType == FileInfo.GRAY32_INT && value == FLOATING_POINT)
						fi.fileType = FileInfo.GRAY32_FLOAT;
					if (fi.fileType == FileInfo.GRAY16_UNSIGNED)
					{
						if (value == SIGNED)
							fi.fileType = FileInfo.GRAY16_SIGNED;
						if (value == FLOATING_POINT)
							error("ImageJ cannot open 16-bit float TIFFs");
					}
					break;
				case JPEG_TABLES:
					if (fi.compression == FileInfo.JPEG)
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
							fi.compression == FileInfo.COMPRESSION_NONE)
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
				default:
					if (tag > 10000 && tag < 32768 && ifdCount > 1)
						return null;
			}
		}
		fi.fileFormat = FileInfo.TIFF;
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

	void getMetaData(int loc, FileInfo fi) throws IOException
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

	void getInfoProperty(int first, FileInfo fi) throws IOException
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

	void getSliceLabels(int first, int last, FileInfo fi) throws IOException
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
				//ij.IJ.log(i+"  "+fi.sliceLabels[i-1]+"  "+len);
			}
			else
				fi.sliceLabels[index++] = null;
		}
	}

	void getDisplayRanges(int first, FileInfo fi) throws IOException
	{
		int n = metaDataCounts[first] / 8;
		fi.displayRanges = new double[n];
		for (int i = 0; i < n; i++)
			fi.displayRanges[i] = readDouble();
	}

	void getLuts(int first, int last, FileInfo fi) throws IOException
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

	void getRoi(int first, FileInfo fi) throws IOException
	{
		int len = metaDataCounts[first];
		fi.roi = new byte[len];
		in.readFully(fi.roi, len);
	}

	void getOverlay(int first, int last, FileInfo fi) throws IOException
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

	public FileInfo[] getTiffInfo() throws IOException
	{
		long ifdOffset;
		TurboList<FileInfo> list = new TurboList<FileInfo>();
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
			FileInfo fi = openIFD();
			if (fi != null)
			{
				list.add(fi);
				ifdOffset = ((long) getInt()) & 0xffffffffL;
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
			FileInfo[] info = list.toArray(new FileInfo[list.size()]);
			if (debugMode)
				info[0].debugInfo = dInfo;
			in.close();
			if (info[0].info == null)
				info[0].info = tiffMetadata;
			FileInfo fi = info[0];
			if (fi.fileType == FileInfo.GRAY16_UNSIGNED && fi.description == null)
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

	String getGapInfo(FileInfo[] fi)
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
	 * Gets the number of images in the TIFF file. The stream is not closed by calling this method. The stream will need
	 * to be reset to position 0 to use for reading IFDs.
	 *
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
			return 0;
		}

		// We do not care about the actual IFD contents. Just the count.

		// Open the first IFD looking for information about the number of images

		//System.out.println("first IFD = " + ifdOffset);
		in.seek(ifdOffset);
		int ifdCount = scanFirstIFD();

		//		// This should be the same for nImages
		//		in.seek(0);
		//		OpenImageFileHeader();
		//		in.seek(ifdOffset);
		//		FileInfo fi2 = OpenIFD();

		if (ifdCount < 0)
		{
			//System.out.println("No first IFD");
			return 0;
		}

		// If an ImageJ image then the nImages is written to the description
		if (ifdCount > 1)
		{
			return ifdCount;
		}

		// If not an ImageJ image then we have to read each IFD
		ifdCount = 1;
		ifdOffset = ((long) getInt()) & 0xffffffffL;

		while (ifdOffset > 0L)
		{
			in.seek(ifdOffset);

			if (!scanIFD())
			{
				//System.out.println("No more IFDs");
				break;
			}

			ifdCount++;
			ifdOffset = ((long) getInt()) & 0xffffffffL;
		}

		return ifdCount;
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

			// METAMORPH2 contains the nImages if compression is FileInfo.COMPRESSION_NONE
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
	public static void saveImageJnImages(byte[] description, FileInfo fi)
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
	 * Guess the number of images in the TIFF file. The stream is not closed by calling this method. The stream will
	 * need to be reset to position 0 to use for reading IFDs.
	 * <p>
	 * This assumes that all the IFDs after the first one are the same size and all the images are the same pixel type
	 * and size as the first.
	 *
	 * @param size
	 *            the file size
	 * @return the number of images
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public int guessNumberOfImages() throws IOException
	{
		if (in == null)
			throw new NullPointerException("No random access stream");

		in.seek(0);

		// Find the first IFD
		long ifdOffset = openImageFileHeader();
		if (ifdOffset < 0L)
		{
			//System.out.println("No IFD offset");
			return 0;
		}

		// We do not care about the actual IFD contents. Just the count.

		// Open the first IFD looking for information about the number of images

		//System.out.println("first IFD = " + ifdOffset);
		in.seek(ifdOffset);
		int ifdCount = scanFirstIFD();

		//		// This should be the same for nImages
		//		in.seek(0);
		//		OpenImageFileHeader();
		//		in.seek(ifdOffset);
		//		FileInfo fi2 = OpenIFD();

		if (ifdCount < 0)
		{
			//System.out.println("No first IFD");
			return 0;
		}

		// If an ImageJ image then the nImages is written to the description
		if (ifdCount > 1)
		{
			return ifdCount;
		}

		ifdOffset = ((long) getInt()) & 0xffffffffL;

		if (ifdOffset <= 0L)
			return 1;

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
		// The 8 bytes is for the image file header data.
		return 1 + (int) Math.round((double) (fileSize - imageSize - ifdSize1 - 8) / (imageSize + ifdSize2));
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

			if (count > 1)
				total += getFieldSize(fieldType) * count;

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

	private int getFieldSize(int fieldType)
	{
		switch (fieldType)
		{
			case 1:
				return 1; // byte
			case 2:
				return 1; // ASCII String
			case 3:
				return 2; // word
			case 4:
				return 4; // dword / uword
			case 5:
				return 8; // rational (2 dwords, numerator and denominator)
		}
		if (debugMode)
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
							fileType = FileInfo.GRAY8;
						else if (value == 16)
							fileType = FileInfo.GRAY16_UNSIGNED;
						else if (value == 32)
							fileType = FileInfo.GRAY32_INT;
						else if (value == 12)
							fileType = FileInfo.GRAY12_UNSIGNED;
						else if (value == 1)
							fileType = FileInfo.BITMAP;
						else
							error("Unsupported BitsPerSample: " + value);
					}
					else if (count > 1)
					{
						long saveLoc = in.getFilePointer();
						in.seek(lvalue);
						int bitDepth = getShort();
						if (bitDepth == 8)
							fileType = FileInfo.GRAY8;
						else if (bitDepth == 16)
							fileType = FileInfo.GRAY16_UNSIGNED;
						else
							error("ImageJ can only open 8 and 16 bit/channel images (" + bitDepth + ")");
						in.seek(saveLoc);
					}
					break;
				case SAMPLES_PER_PIXEL:
					samplesPerPixel = value;
					if (value == 3 && fileType == FileInfo.GRAY8)
						fileType = FileInfo.RGB;
					else if (value == 3 && fileType == FileInfo.GRAY16_UNSIGNED)
						fileType = FileInfo.RGB48;
					else if (value == 4 && fileType == FileInfo.GRAY8)
						fileType = photoInterp == 5 ? FileInfo.CMYK : FileInfo.ARGB;
					else if (value == 4 && fileType == FileInfo.GRAY16_UNSIGNED)
					{
						fileType = FileInfo.RGB48;
					}
					break;

				case PLANAR_CONFIGURATION: // 1=chunky, 2=planar
					if (value == 2 && fileType == FileInfo.RGB48)
						fileType = FileInfo.RGB48_PLANAR;
					else if (value == 2 && fileType == FileInfo.RGB)
						fileType = FileInfo.RGB_PLANAR;
					else if (value != 2 && !(samplesPerPixel == 1 || samplesPerPixel == 3 || samplesPerPixel == 4))
					{
						String msg = "Unsupported SamplesPerPixel: " + samplesPerPixel;
						error(msg);
					}
					break;
				case COMPRESSION:
					if (value == 5)
					{// LZW compression
						compression = FileInfo.LZW;
						if (fileType == FileInfo.GRAY12_UNSIGNED)
							error("ImageJ cannot open 12-bit LZW-compressed TIFFs");
					}
					else if (value == 32773) // PackBits compression
						compression = FileInfo.PACK_BITS;
					else if (value == 32946 || value == 8)
						compression = FileInfo.ZIP;
					else if (value != 1 && value != 0 && !(value == 7 && width < 500))
					{
						// don't abort with Spot camera compressed (7) thumbnails
						// otherwise, this is an unknown compression type
						compression = FileInfo.COMPRESSION_UNKNOWN;
						error("ImageJ cannot open TIFF files " + "compressed in this fashion (" + value + ")");
					}
					break;
				case TILE_WIDTH:
					error("ImageJ cannot open tiled TIFFs.\nTry using the Bio-Formats plugin.");
					break;
				case SAMPLE_FORMAT:
					if (fileType == FileInfo.GRAY32_INT && value == FLOATING_POINT)
						fileType = FileInfo.GRAY32_FLOAT;
					if (fileType == FileInfo.GRAY16_UNSIGNED)
					{
						if (value == SIGNED)
							fileType = FileInfo.GRAY16_SIGNED;
						if (value == FLOATING_POINT)
							error("ImageJ cannot open 16-bit float TIFFs");
					}
					break;
				case JPEG_TABLES:
					if (compression == FileInfo.JPEG)
						error("Cannot open JPEG-compressed TIFFs with separate tables");
					break;
			}
		}
		int size = getBytesPerImage(width, height, fileType);
		if (size != 0 && compression <= FileInfo.COMPRESSION_NONE)
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
			case FileInfo.GRAY8:
			case FileInfo.COLOR8:
			case FileInfo.BITMAP:
				return size;
			case FileInfo.GRAY12_UNSIGNED:
				return (int) (1.5 * size);
			case FileInfo.GRAY16_SIGNED:
			case FileInfo.GRAY16_UNSIGNED:
				return 2 * size;
			case FileInfo.GRAY24_UNSIGNED:
				return 3 * size;
			case FileInfo.GRAY32_INT:
			case FileInfo.GRAY32_UNSIGNED:
			case FileInfo.GRAY32_FLOAT:
				return 4 * size;
			case FileInfo.GRAY64_FLOAT:
				return 8 * size;
			case FileInfo.ARGB:
			case FileInfo.BARG:
			case FileInfo.ABGR:
			case FileInfo.CMYK:
				return 4 * size;
			case FileInfo.RGB:
			case FileInfo.RGB_PLANAR:
			case FileInfo.BGR:
				return 3 * size;
			case FileInfo.RGB48:
				return 6 * size;
			case FileInfo.RGB48_PLANAR:
				return 2 * size;
			default:
				return 0;
		}
	}

}
