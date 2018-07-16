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
package ij.io;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import javax.imageio.ImageIO;

import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

/**
 * Reads raw 8-bit, 16-bit or 32-bit (float or RGB) images from a stream.
 * <p>
 * This is a re-implementation of the ij.io.TiffDecoder to use a SeekableStream interface. If you want to use an input
 * stream then you can use the original TiffDecoder.
 * <p>
 * Removed support for progress tracking. Allow IO Exceptions to be thrown.
 */
public class FastImageReader
{
	private static final int CLEAR_CODE = 256;
	private static final int EOI_CODE = 257;

	private final FileInfo fi;
	private final int width, height;
	private long skipCount;
	private int bytesPerPixel, bufferSize, nPixels;
	private long byteCount;

	// readRGB48() calculates min/max pixel values
	/** The min value read for RGB48 images. */
	public double min;
	/** The max value read for RGB48 images. */
	public double max;

	/**
	 * Constructs a new ImageReader using a FileInfo object to describe the file to be read.
	 *
	 * @param fi
	 *            the file info
	 * @see ij.io.FileInfo
	 */
	public FastImageReader(FileInfo fi)
	{
		this.fi = fi;
		width = fi.width;
		height = fi.height;
		skipCount = fi.getOffset();
	}

	/**
	 * Read 8 bit image.
	 *
	 * @param in
	 *            the input seekable stream
	 * @return the byte[] image
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	byte[] read8bitImage(SeekableStream in) throws IOException
	{
		if (fi.compression > FileInfo.COMPRESSION_NONE)
			return readCompressed8bitImage(in);
		final byte[] pixels = new byte[nPixels];
		// assume contiguous strips
		int count, actuallyRead;
		int totalRead = 0;
		while (totalRead < byteCount)
		{
			if (totalRead + bufferSize > byteCount)
				count = (int) (byteCount - totalRead);
			else
				count = bufferSize;
			actuallyRead = in.read(pixels, totalRead, count);
			if (actuallyRead < 0)
				throw new EOFException();
			totalRead += actuallyRead;
			//showProgress(totalRead, byteCount);
		}
		return pixels;
	}

	/**
	 * Read compressed 8 bit image.
	 *
	 * @param in
	 *            the input seekable stream
	 * @return the byte[] image
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	byte[] readCompressed8bitImage(SeekableStream in) throws IOException
	{
		final byte[] pixels = new byte[nPixels];
		int current = 0;
		byte last = 0;
		for (int i = 0; i < fi.stripOffsets.length; i++)
		{
			in.seek(fi.stripOffsets[i]);
			byte[] byteArray = new byte[fi.stripLengths[i]];
			int read = 0, left = byteArray.length;
			while (left > 0)
			{
				final int r = in.read(byteArray, read, left);
				if (r < 0)
					throw new EOFException();
				read += r;
				left -= r;
			}
			byteArray = uncompress(byteArray);
			int length = byteArray.length;
			length = length - (length % fi.width);
			if (fi.compression == FileInfo.LZW_WITH_DIFFERENCING)
				for (int b = 0; b < length; b++)
				{
					byteArray[b] += last;
					last = b % fi.width == fi.width - 1 ? 0 : byteArray[b];
				}
			if (current + length > pixels.length)
				length = pixels.length - current;
			System.arraycopy(byteArray, 0, pixels, current, length);
			current += length;
			//showProgress(i + 1, fi.stripOffsets.length);
		}
		return pixels;
	}

	/**
	 * Reads a 16-bit image. Signed pixels are converted to unsigned by adding 32768.
	 *
	 * @param in
	 *            the input seekable stream
	 * @return the short[] image
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	short[] read16bitImage(SeekableStream in) throws IOException
	{
		if (fi.compression > FileInfo.COMPRESSION_NONE ||
				(fi.stripOffsets != null && fi.stripOffsets.length > 1) && fi.fileType != FileInfo.RGB48_PLANAR)
			return readCompressed16bitImage(in);
		int pixelsRead;
		final byte[] buffer = new byte[bufferSize];
		final short[] pixels = new short[nPixels];
		long totalRead = 0L;
		int base = 0;
		int count;
		int bufferCount;

		// Debug what takes the time when reading
		//		long readTime = 0;
		//		long decodeTime = 0;
		//		long stamp1, stamp2;

		while (totalRead < byteCount)
		{
			//stamp1 = System.nanoTime();
			if ((totalRead + bufferSize) > byteCount)
				bufferSize = (int) (byteCount - totalRead);
			bufferCount = 0;
			while (bufferCount < bufferSize)
			{ // fill the buffer
				count = in.read(buffer, bufferCount, bufferSize - bufferCount);
				if (count < 0)
					throw new EOFException();
				bufferCount += count;
			}
			totalRead += bufferSize;
			//showProgress(totalRead, byteCount);
			pixelsRead = bufferSize / bytesPerPixel;
			//stamp2 = System.nanoTime();
			//readTime += stamp2 - stamp1;
			if (fi.intelByteOrder)
			{
				if (fi.fileType == FileInfo.GRAY16_SIGNED)
					for (int i = base, j = 0; i < (base + pixelsRead); i++, j += 2)
						pixels[i] = (short) ((((buffer[j + 1] & 0xff) << 8) | (buffer[j] & 0xff)) + 32768);
				else
					for (int i = base, j = 0; i < (base + pixelsRead); i++, j += 2)
						pixels[i] = (short) (((buffer[j + 1] & 0xff) << 8) | (buffer[j] & 0xff));
			}
			else if (fi.fileType == FileInfo.GRAY16_SIGNED)
				for (int i = base, j = 0; i < (base + pixelsRead); i++, j += 2)
					pixels[i] = (short) ((((buffer[j] & 0xff) << 8) | (buffer[j + 1] & 0xff)) + 32768);
			else
				for (int i = base, j = 0; i < (base + pixelsRead); i++, j += 2)
					pixels[i] = (short) (((buffer[j] & 0xff) << 8) | (buffer[j + 1] & 0xff));
			base += pixelsRead;
			//stamp1 = System.nanoTime();
			//decodeTime += stamp1 - stamp2;
		}
		//System.out.printf("Read = %d, Decode = %d  (%.2f)\n", readTime, decodeTime,
		//		(double) decodeTime / (readTime + decodeTime));
		return pixels;
	}

	/**
	 * Read compressed 16 bit image.
	 *
	 * @param in
	 *            the input seekable stream
	 * @return the short[] image
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	short[] readCompressed16bitImage(SeekableStream in) throws IOException
	{
		final short[] pixels = new short[nPixels];
		int base = 0;
		short last = 0;
		for (int k = 0; k < fi.stripOffsets.length; k++)
		{
			in.seek(fi.stripOffsets[k]);
			byte[] byteArray = new byte[fi.stripLengths[k]];
			int read = 0, left = byteArray.length;
			while (left > 0)
			{
				final int r = in.read(byteArray, read, left);
				if (r < 0)
					throw new EOFException();
				read += r;
				left -= r;
			}
			byteArray = uncompress(byteArray);
			int pixelsRead = byteArray.length / bytesPerPixel;
			pixelsRead = pixelsRead - (pixelsRead % fi.width);
			int pmax = base + pixelsRead;
			if (pmax > nPixels)
				pmax = nPixels;
			if (fi.intelByteOrder)
				for (int i = base, j = 0; i < pmax; i++, j += 2)
					pixels[i] = (short) (((byteArray[j + 1] & 0xff) << 8) | (byteArray[j] & 0xff));
			else
				for (int i = base, j = 0; i < pmax; i++, j += 2)
					pixels[i] = (short) (((byteArray[j] & 0xff) << 8) | (byteArray[j + 1] & 0xff));
			if (fi.compression == FileInfo.LZW_WITH_DIFFERENCING)
				for (int b = base; b < pmax; b++)
				{
					pixels[b] += last;
					last = b % fi.width == fi.width - 1 ? 0 : pixels[b];
				}
			base += pixelsRead;
			//showProgress(k + 1, fi.stripOffsets.length);
		}
		if (fi.fileType == FileInfo.GRAY16_SIGNED)
			// convert to unsigned
			for (int i = 0; i < nPixels; i++)
				pixels[i] = (short) (pixels[i] + 32768);
		return pixels;
	}

	/**
	 * Read 32 bit image.
	 *
	 * @param in
	 *            the input seekable stream
	 * @return the float[] image
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	float[] read32bitImage(SeekableStream in) throws IOException
	{
		if (fi.compression > FileInfo.COMPRESSION_NONE || (fi.stripOffsets != null && fi.stripOffsets.length > 1))
			return readCompressed32bitImage(in);
		int pixelsRead;
		final byte[] buffer = new byte[bufferSize];
		final float[] pixels = new float[nPixels];
		long totalRead = 0L;
		int base = 0;
		int count;
		int bufferCount;
		int tmp;

		while (totalRead < byteCount)
		{
			if ((totalRead + bufferSize) > byteCount)
				bufferSize = (int) (byteCount - totalRead);
			bufferCount = 0;
			while (bufferCount < bufferSize)
			{ // fill the buffer
				count = in.read(buffer, bufferCount, bufferSize - bufferCount);
				if (count < 0)
					throw new EOFException();
				bufferCount += count;
			}
			totalRead += bufferSize;
			//showProgress(totalRead, byteCount);
			pixelsRead = bufferSize / bytesPerPixel;
			int pmax = base + pixelsRead;
			if (pmax > nPixels)
				pmax = nPixels;
			int j = 0;
			if (fi.intelByteOrder)
				for (int i = base; i < pmax; i++)
				{
					tmp = ((buffer[j + 3] & 0xff) << 24) | ((buffer[j + 2] & 0xff) << 16) |
							((buffer[j + 1] & 0xff) << 8) | (buffer[j] & 0xff);
					if (fi.fileType == FileInfo.GRAY32_FLOAT)
						pixels[i] = Float.intBitsToFloat(tmp);
					else if (fi.fileType == FileInfo.GRAY32_UNSIGNED)
						pixels[i] = tmp & 0xffffffffL;
					else
						pixels[i] = tmp;
					j += 4;
				}
			else
				for (int i = base; i < pmax; i++)
				{
					tmp = ((buffer[j] & 0xff) << 24) | ((buffer[j + 1] & 0xff) << 16) | ((buffer[j + 2] & 0xff) << 8) |
							(buffer[j + 3] & 0xff);
					if (fi.fileType == FileInfo.GRAY32_FLOAT)
						pixels[i] = Float.intBitsToFloat(tmp);
					else if (fi.fileType == FileInfo.GRAY32_UNSIGNED)
						pixels[i] = tmp & 0xffffffffL;
					else
						pixels[i] = tmp;
					j += 4;
				}
			base += pixelsRead;
		}
		return pixels;
	}

	/**
	 * Read compressed 32 bit image.
	 *
	 * @param in
	 *            the input seekable stream
	 * @return the float[] image
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	float[] readCompressed32bitImage(SeekableStream in) throws IOException
	{
		final float[] pixels = new float[nPixels];
		int base = 0;
		float last = 0;
		for (int k = 0; k < fi.stripOffsets.length; k++)
		{
			in.seek(fi.stripOffsets[k]);
			byte[] byteArray = new byte[fi.stripLengths[k]];
			int read = 0, left = byteArray.length;
			while (left > 0)
			{
				final int r = in.read(byteArray, read, left);
				if (r < 0)
					throw new EOFException();
				read += r;
				left -= r;
			}
			byteArray = uncompress(byteArray);
			int pixelsRead = byteArray.length / bytesPerPixel;
			pixelsRead = pixelsRead - (pixelsRead % fi.width);
			int pmax = base + pixelsRead;
			if (pmax > nPixels)
				pmax = nPixels;
			int tmp;
			if (fi.intelByteOrder)
				for (int i = base, j = 0; i < pmax; i++, j += 4)
				{
					tmp = ((byteArray[j + 3] & 0xff) << 24) | ((byteArray[j + 2] & 0xff) << 16) |
							((byteArray[j + 1] & 0xff) << 8) | (byteArray[j] & 0xff);
					if (fi.fileType == FileInfo.GRAY32_FLOAT)
						pixels[i] = Float.intBitsToFloat(tmp);
					else if (fi.fileType == FileInfo.GRAY32_UNSIGNED)
						pixels[i] = tmp & 0xffffffffL;
					else
						pixels[i] = tmp;
				}
			else
				for (int i = base, j = 0; i < pmax; i++, j += 4)
				{
					tmp = ((byteArray[j] & 0xff) << 24) | ((byteArray[j + 1] & 0xff) << 16) |
							((byteArray[j + 2] & 0xff) << 8) | (byteArray[j + 3] & 0xff);
					if (fi.fileType == FileInfo.GRAY32_FLOAT)
						pixels[i] = Float.intBitsToFloat(tmp);
					else if (fi.fileType == FileInfo.GRAY32_UNSIGNED)
						pixels[i] = tmp & 0xffffffffL;
					else
						pixels[i] = tmp;
				}
			if (fi.compression == FileInfo.LZW_WITH_DIFFERENCING)
				for (int b = base; b < pmax; b++)
				{
					pixels[b] += last;
					last = b % fi.width == fi.width - 1 ? 0 : pixels[b];
				}
			base += pixelsRead;
			//showProgress(k + 1, fi.stripOffsets.length);
		}
		return pixels;
	}

	/**
	 * Read 64 bit image.
	 *
	 * @param in
	 *            the input seekable stream
	 * @return the float[] image
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	float[] read64bitImage(SeekableStream in) throws IOException
	{
		int pixelsRead;
		final byte[] buffer = new byte[bufferSize];
		final float[] pixels = new float[nPixels];
		long totalRead = 0L;
		int base = 0;
		int count;
		int bufferCount;
		long tmp;
		long b1, b2, b3, b4, b5, b6, b7, b8;

		while (totalRead < byteCount)
		{
			if ((totalRead + bufferSize) > byteCount)
				bufferSize = (int) (byteCount - totalRead);
			bufferCount = 0;
			while (bufferCount < bufferSize)
			{ // fill the buffer
				count = in.read(buffer, bufferCount, bufferSize - bufferCount);
				if (count < 0)
					throw new EOFException();
				bufferCount += count;
			}
			totalRead += bufferSize;
			//showProgress(totalRead, byteCount);
			pixelsRead = bufferSize / bytesPerPixel;
			int j = 0;
			for (int i = base; i < (base + pixelsRead); i++)
			{
				b1 = buffer[j + 7] & 0xff;
				b2 = buffer[j + 6] & 0xff;
				b3 = buffer[j + 5] & 0xff;
				b4 = buffer[j + 4] & 0xff;
				b5 = buffer[j + 3] & 0xff;
				b6 = buffer[j + 2] & 0xff;
				b7 = buffer[j + 1] & 0xff;
				b8 = buffer[j] & 0xff;
				if (fi.intelByteOrder)
					tmp = (b1 << 56) | (b2 << 48) | (b3 << 40) | (b4 << 32) | (b5 << 24) | (b6 << 16) | (b7 << 8) | b8;
				else
					tmp = (b8 << 56) | (b7 << 48) | (b6 << 40) | (b5 << 32) | (b4 << 24) | (b3 << 16) | (b2 << 8) | b1;
				pixels[i] = (float) Double.longBitsToDouble(tmp);
				j += 8;
			}
			base += pixelsRead;
		}
		return pixels;
	}

	/**
	 * Read chunky RGB.
	 *
	 * @param in
	 *            the input seekable stream
	 * @return the int[] image
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	int[] readChunkyRGB(SeekableStream in) throws IOException
	{
		if (fi.compression == FileInfo.JPEG)
			return readJPEG(in);
		else if (fi.compression > FileInfo.COMPRESSION_NONE)
			return readCompressedChunkyRGB(in);
		int pixelsRead;
		bufferSize = 24 * width;
		final byte[] buffer = new byte[bufferSize];
		final int[] pixels = new int[nPixels];
		long totalRead = 0L;
		int base = 0;
		int count;
		int bufferCount;
		int r, g, b, a;

		while (totalRead < byteCount)
		{
			if ((totalRead + bufferSize) > byteCount)
				bufferSize = (int) (byteCount - totalRead);
			bufferCount = 0;
			while (bufferCount < bufferSize)
			{ // fill the buffer
				count = in.read(buffer, bufferCount, bufferSize - bufferCount);
				if (count < 0)
					throw new EOFException();
				bufferCount += count;
			}
			totalRead += bufferSize;
			//showProgress(totalRead, byteCount);
			pixelsRead = bufferSize / bytesPerPixel;
			final boolean bgr = fi.fileType == FileInfo.BGR;
			int j = 0;
			for (int i = base; i < (base + pixelsRead); i++)
			{
				if (bytesPerPixel == 4)
				{
					if (fi.fileType == FileInfo.BARG)
					{ // MCID
						b = buffer[j++] & 0xff;
						j++; // ignore alfa byte
						r = buffer[j++] & 0xff;
						g = buffer[j++] & 0xff;
					}
					else if (fi.fileType == FileInfo.ABGR)
					{
						b = buffer[j++] & 0xff;
						g = buffer[j++] & 0xff;
						r = buffer[j++] & 0xff;
						j++; // ignore alfa byte
					}
					else if (fi.fileType == FileInfo.CMYK)
					{
						r = buffer[j++] & 0xff; // c
						g = buffer[j++] & 0xff; // m
						b = buffer[j++] & 0xff; // y
						a = buffer[j++] & 0xff; // k
						if (a > 0)
						{ // if k>0 then  c=c*(1-k)+k
							r = ((r * (256 - a)) >> 8) + a;
							g = ((g * (256 - a)) >> 8) + a;
							b = ((b * (256 - a)) >> 8) + a;
						} // else  r=1-c, g=1-m and b=1-y, which IJ does by inverting image
					}
					else
					{ // ARGB
						r = buffer[j++] & 0xff;
						g = buffer[j++] & 0xff;
						b = buffer[j++] & 0xff;
						j++; // ignore alfa byte
					}
				}
				else
				{
					r = buffer[j++] & 0xff;
					g = buffer[j++] & 0xff;
					b = buffer[j++] & 0xff;
				}
				if (bgr)
					pixels[i] = 0xff000000 | (b << 16) | (g << 8) | r;
				else
					pixels[i] = 0xff000000 | (r << 16) | (g << 8) | b;
			}
			base += pixelsRead;
		}
		return pixels;
	}

	/**
	 * Read compressed chunky RGB.
	 *
	 * @param in
	 *            the input seekable stream
	 * @return the int[] image
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	int[] readCompressedChunkyRGB(SeekableStream in) throws IOException
	{
		final int[] pixels = new int[nPixels];
		int base = 0;
		int red = 0, green = 0, blue = 0, alpha = 0;
		final boolean bgr = fi.fileType == FileInfo.BGR;
		final boolean cmyk = fi.fileType == FileInfo.CMYK;
		final boolean differencing = fi.compression == FileInfo.LZW_WITH_DIFFERENCING;
		for (int i = 0; i < fi.stripOffsets.length; i++)
		{
			in.seek(fi.stripOffsets[i]);
			byte[] byteArray = new byte[fi.stripLengths[i]];
			int read = 0, left = byteArray.length;
			while (left > 0)
			{
				final int r = in.read(byteArray, read, left);
				if (r < 0)
					throw new EOFException();
				read += r;
				left -= r;
			}
			byteArray = uncompress(byteArray);
			if (differencing)
				for (int b = 0; b < byteArray.length; b++)
				{
					if (b / bytesPerPixel % fi.width == 0)
						continue;
					byteArray[b] += byteArray[b - bytesPerPixel];
				}
			int k = 0;
			int pixelsRead = byteArray.length / bytesPerPixel;
			pixelsRead = pixelsRead - (pixelsRead % fi.width);
			int pmax = base + pixelsRead;
			if (pmax > nPixels)
				pmax = nPixels;
			for (int j = base; j < pmax; j++)
			{
				if (bytesPerPixel == 4)
				{
					red = byteArray[k++] & 0xff;
					green = byteArray[k++] & 0xff;
					blue = byteArray[k++] & 0xff;
					alpha = byteArray[k++] & 0xff;
					if (cmyk && alpha > 0)
					{
						red = ((red * (256 - alpha)) >> 8) + alpha;
						green = ((green * (256 - alpha)) >> 8) + alpha;
						blue = ((blue * (256 - alpha)) >> 8) + alpha;
					}
				}
				else
				{
					red = byteArray[k++] & 0xff;
					green = byteArray[k++] & 0xff;
					blue = byteArray[k++] & 0xff;
				}
				if (bgr)
					pixels[j] = 0xff000000 | (blue << 16) | (green << 8) | red;
				else
					pixels[j] = 0xff000000 | (red << 16) | (green << 8) | blue;
			}
			base += pixelsRead;
			//showProgress(i + 1, fi.stripOffsets.length);
		}
		return pixels;
	}

	/**
	 * Read JPEG.
	 *
	 * @param in
	 *            the input seekable stream
	 * @return the int[] image
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	int[] readJPEG(SeekableStream in) throws IOException
	{
		final BufferedImage bi = ImageIO.read(in);
		final ImageProcessor ip = new ColorProcessor(bi);
		return (int[]) ip.getPixels();
	}

	/**
	 * Read planar RGB.
	 *
	 * @param in
	 *            the input seekable stream
	 * @return the int[] image
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	int[] readPlanarRGB(SeekableStream in) throws IOException
	{
		if (fi.compression > FileInfo.COMPRESSION_NONE)
			return readCompressedPlanarRGBImage(in);
		final int planeSize = nPixels; // 1/3 image size
		final byte[] buffer = new byte[planeSize];
		final int[] pixels = new int[nPixels];
		int r, g, b;

		//showProgress(10, 100);
		in.readFully(buffer);
		for (int i = 0; i < planeSize; i++)
		{
			r = buffer[i] & 0xff;
			pixels[i] = 0xff000000 | (r << 16);
		}

		//showProgress(40, 100);
		in.readFully(buffer);
		for (int i = 0; i < planeSize; i++)
		{
			g = buffer[i] & 0xff;
			pixels[i] |= g << 8;
		}

		//showProgress(70, 100);
		in.readFully(buffer);
		for (int i = 0; i < planeSize; i++)
		{
			b = buffer[i] & 0xff;
			pixels[i] |= b;
		}

		//showProgress(90, 100);
		return pixels;
	}

	/**
	 * Read compressed planar RGB image.
	 *
	 * @param in
	 *            the input seekable stream
	 * @return the int[] image
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	int[] readCompressedPlanarRGBImage(SeekableStream in) throws IOException
	{
		final int[] pixels = new int[nPixels];
		int r, g, b;
		nPixels *= 3; // read all 3 planes
		final byte[] buffer = readCompressed8bitImage(in);
		nPixels /= 3;
		for (int i = 0; i < nPixels; i++)
		{
			r = buffer[i] & 0xff;
			pixels[i] = 0xff000000 | (r << 16);
		}
		for (int i = 0; i < nPixels; i++)
		{
			g = buffer[nPixels + i] & 0xff;
			pixels[i] |= g << 8;
		}
		for (int i = 0; i < nPixels; i++)
		{
			b = buffer[nPixels * 2 + i] & 0xff;
			pixels[i] |= b;
		}
		return pixels;
	}

	/**
	 * Read RGB 48.
	 *
	 * @param in
	 *            the input seekable stream
	 * @return the object
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	Object readRGB48(SeekableStream in) throws IOException
	{
		if (fi.compression > FileInfo.COMPRESSION_NONE)
			return readCompressedRGB48(in);
		final int channels = fi.samplesPerPixel;
		final short[][] stack = new short[channels][nPixels];
		int pixel = 0;
		int min = 65535, max = 0;
		if (fi.stripLengths == null)
		{
			fi.stripLengths = new int[fi.stripOffsets.length];
			fi.stripLengths[0] = width * height * bytesPerPixel;
		}
		for (int i = 0; i < fi.stripOffsets.length; i++)
		{
			if (i > 0)
			{
				final long skip = (fi.stripOffsets[i] & 0xffffffffL) - (fi.stripOffsets[i - 1] & 0xffffffffL) -
						fi.stripLengths[i - 1];
				if (skip > 0L)
					in.skip(skip);
			}
			int len = fi.stripLengths[i];
			final int bytesToGo = (nPixels - pixel) * channels * 2;
			if (len > bytesToGo)
				len = bytesToGo;
			final byte[] buffer = new byte[len];
			in.readFully(buffer);
			int value;
			int channel = 0;
			final boolean intel = fi.intelByteOrder;
			for (int base = 0; base < len; base += 2)
			{
				if (intel)
					value = ((buffer[base + 1] & 0xff) << 8) | (buffer[base] & 0xff);
				else
					value = ((buffer[base] & 0xff) << 8) | (buffer[base + 1] & 0xff);
				if (value < min)
					min = value;
				if (value > max)
					max = value;
				stack[channel][pixel] = (short) (value);
				channel++;
				if (channel == channels)
				{
					channel = 0;
					pixel++;
				}
			}
			//showProgress(i + 1, fi.stripOffsets.length);
		}
		this.min = min;
		this.max = max;
		return stack;
	}

	/**
	 * Read compressed RGB 48.
	 *
	 * @param in
	 *            the input seekable stream
	 * @return the object
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	Object readCompressedRGB48(SeekableStream in) throws IOException
	{
		if (fi.compression == FileInfo.LZW_WITH_DIFFERENCING)
			throw new IOException("ImageJ cannot open 48-bit LZW compressed TIFFs with predictor");
		final int channels = 3;
		final short[][] stack = new short[channels][nPixels];
		int pixel = 0;
		int min = 65535, max = 0;
		for (int i = 0; i < fi.stripOffsets.length; i++)
		{
			if (i > 0)
			{
				final long skip = (fi.stripOffsets[i] & 0xffffffffL) - (fi.stripOffsets[i - 1] & 0xffffffffL) -
						fi.stripLengths[i - 1];
				if (skip > 0L)
					in.skip(skip);
			}
			int len = fi.stripLengths[i];
			byte[] buffer = new byte[len];
			in.readFully(buffer);
			buffer = uncompress(buffer);
			len = buffer.length;
			if (len % 2 != 0)
				len--;
			int value;
			int channel = 0;
			final boolean intel = fi.intelByteOrder;
			for (int base = 0; base < len && pixel < nPixels; base += 2)
			{
				if (intel)
					value = ((buffer[base + 1] & 0xff) << 8) | (buffer[base] & 0xff);
				else
					value = ((buffer[base] & 0xff) << 8) | (buffer[base + 1] & 0xff);
				if (value < min)
					min = value;
				if (value > max)
					max = value;
				stack[channel][pixel] = (short) (value);
				channel++;
				if (channel == channels)
				{
					channel = 0;
					pixel++;
				}
			}
			//showProgress(i + 1, fi.stripOffsets.length);
		}
		this.min = min;
		this.max = max;
		return stack;
	}

	/**
	 * Read RGB 48 planar.
	 *
	 * @param in
	 *            the input seekable stream
	 * @return the object
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	Object readRGB48Planar(SeekableStream in) throws IOException
	{
		final int channels = fi.samplesPerPixel;
		final Object[] stack = new Object[channels];
		for (int i = 0; i < channels; i++)
			stack[i] = read16bitImage(in);
		return stack;
	}

	/**
	 * Read 12 bit image.
	 *
	 * @param in
	 *            the input seekable stream
	 * @return the short[] image
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	short[] read12bitImage(SeekableStream in) throws IOException
	{
		int bytesPerLine = (int) (width * 1.5);
		if ((width & 1) == 1)
			bytesPerLine++; // add 1 if odd
		final byte[] buffer = new byte[bytesPerLine * height];
		final short[] pixels = new short[nPixels];
		in.readFully(buffer);
		for (int y = 0; y < height; y++)
		{
			int index1 = y * bytesPerLine;
			final int index2 = y * width;
			int count = 0;
			while (count < width)
			{
				pixels[index2 + count] = (short) (((buffer[index1] & 0xff) * 16) + ((buffer[index1 + 1] >> 4) & 0xf));
				count++;
				if (count == width)
					break;
				pixels[index2 + count] = (short) (((buffer[index1 + 1] & 0xf) * 256) + (buffer[index1 + 2] & 0xff));
				count++;
				index1 += 3;
			}
		}
		return pixels;
	}

	/**
	 * Read 24 bit image.
	 *
	 * @param in
	 *            the input seekable stream
	 * @return the float[] image
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	float[] read24bitImage(SeekableStream in) throws IOException
	{
		final byte[] buffer = new byte[width * 3];
		final float[] pixels = new float[nPixels];
		int b1, b2, b3;
		for (int y = 0; y < height; y++)
		{
			in.readFully(buffer);
			int b = 0;
			for (int x = 0; x < width; x++)
			{
				b1 = buffer[b++] & 0xff;
				b2 = buffer[b++] & 0xff;
				b3 = buffer[b++] & 0xff;
				pixels[x + y * width] = (b3 << 16) | (b2 << 8) | b1;
			}
		}
		return pixels;
	}

	/**
	 * Read 1 bit image.
	 *
	 * @param in
	 *            the input seekable stream
	 * @return the byte[] image
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	byte[] read1bitImage(SeekableStream in) throws IOException
	{
		if (fi.compression == FileInfo.LZW)
			throw new IOException("ImageJ cannot open 1-bit LZW compressed TIFFs");
		final int scan = (int) Math.ceil(width / 8.0);
		final int len = scan * height;
		final byte[] buffer = new byte[len];
		final byte[] pixels = new byte[nPixels];
		in.readFully(buffer);
		int value1, value2, offset, index;
		for (int y = 0; y < height; y++)
		{
			offset = y * scan;
			index = y * width;
			for (int x = 0; x < scan; x++)
			{
				value1 = buffer[offset + x] & 0xff;
				for (int i = 7; i >= 0; i--)
				{
					value2 = (value1 & (1 << i)) != 0 ? 255 : 0;
					if (index < pixels.length)
						pixels[index++] = (byte) value2;
				}
			}
		}
		return pixels;
	}

	/**
	 * Skip the input by the current value of skip count and
	 * then initialise the buffer for reading.
	 *
	 * @param in
	 *            the input seekable streamput
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	void skip(SeekableStream in) throws IOException
	{
		if (skipCount > 0)
			in.skip(skipCount);
		byteCount = ((long) width) * height * bytesPerPixel;
		if (fi.fileType == FileInfo.BITMAP)
		{
			int scan = width / 8;
			final int pad = width % 8;
			if (pad > 0)
				scan++;
			byteCount = scan * height;
		}
		nPixels = width * height;
		bufferSize = (int) (byteCount / 25L);
		if (bufferSize < 8192)
			bufferSize = 8192;
		else
			bufferSize = (bufferSize / 8192) * 8192;
	}

	/**
	 * Reads the image from the SeekableStream and returns the pixel
	 * array (byte, short, int or float).
	 * Does not close the SeekableStream.
	 *
	 * @param in
	 *            the input seekable stream
	 * @return the object
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public Object readPixels(SeekableStream in) throws IOException
	{
		Object pixels;
		switch (fi.fileType)
		{
			case FileInfo.GRAY8:
			case FileInfo.COLOR8:
				bytesPerPixel = 1;
				skip(in);
				pixels = read8bitImage(in);
				break;
			case FileInfo.GRAY16_SIGNED:
			case FileInfo.GRAY16_UNSIGNED:
				bytesPerPixel = 2;
				skip(in);
				pixels = read16bitImage(in);
				break;
			case FileInfo.GRAY32_INT:
			case FileInfo.GRAY32_UNSIGNED:
			case FileInfo.GRAY32_FLOAT:
				bytesPerPixel = 4;
				skip(in);
				pixels = read32bitImage(in);
				break;
			case FileInfo.GRAY64_FLOAT:
				bytesPerPixel = 8;
				skip(in);
				pixels = read64bitImage(in);
				break;
			case FileInfo.RGB:
			case FileInfo.BGR:
			case FileInfo.ARGB:
			case FileInfo.ABGR:
			case FileInfo.BARG:
			case FileInfo.CMYK:
				bytesPerPixel = fi.getBytesPerPixel();
				skip(in);
				pixels = readChunkyRGB(in);
				break;
			case FileInfo.RGB_PLANAR:
				bytesPerPixel = 3;
				skip(in);
				pixels = readPlanarRGB(in);
				break;
			case FileInfo.BITMAP:
				bytesPerPixel = 1;
				skip(in);
				pixels = read1bitImage(in);
				break;
			case FileInfo.RGB48:
				bytesPerPixel = 6;
				skip(in);
				pixels = readRGB48(in);
				break;
			case FileInfo.RGB48_PLANAR:
				bytesPerPixel = 2;
				skip(in);
				pixels = readRGB48Planar(in);
				break;
			case FileInfo.GRAY12_UNSIGNED:
				skip(in);
				final short[] data = read12bitImage(in);
				pixels = data;
				break;
			case FileInfo.GRAY24_UNSIGNED:
				skip(in);
				pixels = read24bitImage(in);
				break;
			default:
				pixels = null;
		}
		//showProgress(1, 1);
		return pixels;
	}

	/**
	 * Skips the specified number of bytes, then reads an image and
	 * returns the pixel array (byte, short, int or float).
	 * Does not close the SeekableStream.
	 *
	 * @param in
	 *            the input seekable stream
	 * @param skipCount
	 *            the skip count
	 * @return the object
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public Object readPixels(SeekableStream in, long skipCount) throws IOException
	{
		this.skipCount = skipCount;
		return readPixels(in);
	}

	/**
	 * Uncompress.
	 *
	 * @param input
	 *            the input seekable streamput
	 * @return the byte[] image
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	byte[] uncompress(byte[] input) throws IOException
	{
		if (fi.compression == FileInfo.PACK_BITS)
			return packBitsUncompress(input, fi.rowsPerStrip * fi.width * fi.getBytesPerPixel());
		else if (fi.compression == FileInfo.LZW || fi.compression == FileInfo.LZW_WITH_DIFFERENCING)
			return lzwUncompress(input);
		else if (fi.compression == FileInfo.ZIP)
			return zipUncompress(input);
		else
			return input;
	}

	/**
	 * TIFF Adobe ZIP support contributed by Jason Newton.
	 *
	 * @param input
	 *            the input seekable streamput bytes
	 * @return the byte[] image
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public byte[] zipUncompress(byte[] input) throws IOException
	{
		final ByteArrayOutputStream imageBuffer = new ByteArrayOutputStream();
		final byte[] buffer = new byte[1024];
		final Inflater decompressor = new Inflater();
		decompressor.setInput(input);
		try
		{
			while (!decompressor.finished())
			{
				final int rlen = decompressor.inflate(buffer);
				imageBuffer.write(buffer, 0, rlen);
			}
			decompressor.end();
			return imageBuffer.toByteArray();
		}
		catch (final DataFormatException e)
		{
			throw new IOException(e);
		}
	}

	/**
	 * Utility method for decoding an LZW-compressed image strip.
	 * Adapted from the TIFF 6.0 Specification:
	 * http://partners.adobe.com/asn/developer/pdfs/tn/TIFF6.pdf (page 61).
	 * Author: Curtis Rueden (ctrueden at wisc.edu).
	 *
	 * @param input
	 *            the input seekable streamput bytes
	 * @return the byte[] image
	 */
	public byte[] lzwUncompress(byte[] input)
	{
		if (input == null || input.length == 0)
			return input;
		final byte[][] symbolTable = new byte[4096][1];
		int bitsToRead = 9;
		int nextSymbol = 258;
		int code;
		int oldCode = -1;
		final ByteVector out = new ByteVector(8192);
		final BitBuffer bb = new BitBuffer(input);
		final byte[] byteBuffer1 = new byte[16];
		final byte[] byteBuffer2 = new byte[16];

		while (out.size() < byteCount)
		{
			code = bb.getBits(bitsToRead);
			if (code == EOI_CODE || code == -1)
				break;
			if (code == CLEAR_CODE)
			{
				// initialize symbol table
				for (int i = 0; i < 256; i++)
					symbolTable[i][0] = (byte) i;
				nextSymbol = 258;
				bitsToRead = 9;
				code = bb.getBits(bitsToRead);
				if (code == EOI_CODE || code == -1)
					break;
				out.add(symbolTable[code]);
				oldCode = code;
			}
			else
			{
				if (code < nextSymbol)
				{
					// code is in table
					out.add(symbolTable[code]);
					// add string to table
					final ByteVector symbol = new ByteVector(byteBuffer1);
					symbol.add(symbolTable[oldCode]);
					symbol.add(symbolTable[code][0]);
					symbolTable[nextSymbol] = symbol.toByteArray(); //**
					oldCode = code;
					nextSymbol++;
				}
				else
				{
					// out of table
					final ByteVector symbol = new ByteVector(byteBuffer2);
					symbol.add(symbolTable[oldCode]);
					symbol.add(symbolTable[oldCode][0]);
					final byte[] outString = symbol.toByteArray();
					out.add(outString);
					symbolTable[nextSymbol] = outString; //**
					oldCode = code;
					nextSymbol++;
				}
				if (nextSymbol == 511)
					bitsToRead = 10;
				if (nextSymbol == 1023)
					bitsToRead = 11;
				if (nextSymbol == 2047)
					bitsToRead = 12;
			}
		}
		return out.toByteArray();
	}

	/**
	 * Based on the Bio-Formats PackbitsCodec written by Melissa Linkert.
	 *
	 * @param input
	 *            the input seekable streamput
	 * @param expected
	 *            the expected
	 * @return the byte[] image
	 */
	public byte[] packBitsUncompress(byte[] input, int expected)
	{
		if (expected == 0)
			expected = Integer.MAX_VALUE;
		final ByteVector output = new ByteVector(1024);
		int index = 0;
		while (output.size() < expected && index < input.length)
		{
			final byte n = input[index++];
			if (n >= 0)
			{ // 0 <= n <= 127
				byte[] b = new byte[n + 1];
				for (int i = 0; i < n + 1; i++)
					b[i] = input[index++];
				output.add(b);
				b = null;
			}
			else if (n != -128)
			{ // -127 <= n <= -1
				final int len = -n + 1;
				final byte inp = input[index++];
				for (int i = 0; i < len; i++)
					output.add(inp);
			}
		}
		return output.toByteArray();
	}

	// Specialised version to read using an in-memory byte buffer

	/**
	 * Read 8 bit image.
	 *
	 * @param in
	 *            the input seekable stream
	 * @return the byte[] image
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	byte[] read8bitImage(ByteArraySeekableStream in) throws IOException
	{
		if (fi.compression > FileInfo.COMPRESSION_NONE)
			return readCompressed8bitImage(in);

		final int j = getPositionAndSkipPixelBytes(in, 1);
		final byte[] pixels = new byte[nPixels];
		System.arraycopy(in.buffer, j, pixels, 0, nPixels);
		return pixels;
	}

	/**
	 * Reads a 16-bit image. Signed pixels are converted to unsigned by adding 32768.
	 *
	 * @param in
	 *            the input seekable stream
	 * @return the short[] image
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	short[] read16bitImage(ByteArraySeekableStream in) throws IOException
	{
		if (fi.compression > FileInfo.COMPRESSION_NONE ||
				(fi.stripOffsets != null && fi.stripOffsets.length > 1) && fi.fileType != FileInfo.RGB48_PLANAR)
			return readCompressed16bitImage(in);

		// We use the bytes direct
		int j = getPositionAndSkipPixelBytes(in, 2);
		final byte[] buffer = in.buffer;
		final short[] pixels = new short[nPixels];

		if (fi.intelByteOrder)
		{
			if (fi.fileType == FileInfo.GRAY16_SIGNED)
				for (int i = 0; i < nPixels; i++, j += 2)
					pixels[i] = (short) ((((buffer[j + 1] & 0xff) << 8) | (buffer[j] & 0xff)) + 32768);
			else
				for (int i = 0; i < nPixels; i++, j += 2)
					pixels[i] = (short) (((buffer[j + 1] & 0xff) << 8) | (buffer[j] & 0xff));
		}
		else if (fi.fileType == FileInfo.GRAY16_SIGNED)
			for (int i = 0; i < nPixels; i++, j += 2)
				pixels[i] = (short) ((((buffer[j] & 0xff) << 8) | (buffer[j + 1] & 0xff)) + 32768);
		else
			for (int i = 0; i < nPixels; i++, j += 2)
				pixels[i] = (short) (((buffer[j] & 0xff) << 8) | (buffer[j + 1] & 0xff));

		return pixels;
	}

	/**
	 * Read 32 bit image.
	 *
	 * @param in
	 *            the input seekable stream
	 * @return the float[] image
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	float[] read32bitImage(ByteArraySeekableStream in) throws IOException
	{
		if (fi.compression > FileInfo.COMPRESSION_NONE || (fi.stripOffsets != null && fi.stripOffsets.length > 1))
			return readCompressed32bitImage(in);

		// We use the bytes direct
		int j = getPositionAndSkipPixelBytes(in, 4);
		final byte[] buffer = in.buffer;
		final float[] pixels = new float[nPixels];

		if (fi.intelByteOrder)
		{
			//for (int i = 0; i < nPixels; i++, j += 4)
			//{
			//	int tmp = (int) (((buffer[j + 3] & 0xff) << 24) | ((buffer[j + 2] & 0xff) << 16) |
			//			((buffer[j + 1] & 0xff) << 8) | (buffer[j] & 0xff));
			//	if (fi.fileType == FileInfo.GRAY32_FLOAT)
			//		pixels[i] = Float.intBitsToFloat(tmp);
			//	else if (fi.fileType == FileInfo.GRAY32_UNSIGNED)
			//		pixels[i] = (float) (tmp & 0xffffffffL);
			//	else
			//		pixels[i] = tmp;
			//}
			if (fi.fileType == FileInfo.GRAY32_FLOAT)
				for (int i = 0; i < nPixels; i++, j += 4)
					pixels[i] = Float.intBitsToFloat(((buffer[j + 3] & 0xff) << 24) | ((buffer[j + 2] & 0xff) << 16) |
							((buffer[j + 1] & 0xff) << 8) | (buffer[j] & 0xff));
			else if (fi.fileType == FileInfo.GRAY32_UNSIGNED)
			{
				final int tmp = ((buffer[j + 3] & 0xff) << 24) | ((buffer[j + 2] & 0xff) << 16) |
						((buffer[j + 1] & 0xff) << 8) | (buffer[j] & 0xff);
				for (int i = 0; i < nPixels; i++, j += 4)
					pixels[i] = tmp & 0xffffffffL;
			}
			else
				for (int i = 0; i < nPixels; i++, j += 4)
					pixels[i] = ((buffer[j + 3] & 0xff) << 24) | ((buffer[j + 2] & 0xff) << 16) |
							((buffer[j + 1] & 0xff) << 8) | (buffer[j] & 0xff);
		}
		else //for (int i = 0; i < nPixels; i++, j += 4)
		//{
		//	int tmp = (int) (((buffer[j] & 0xff) << 24) | ((buffer[j + 1] & 0xff) << 16) |
		//			((buffer[j + 2] & 0xff) << 8) | (buffer[j + 3] & 0xff));
		//	if (fi.fileType == FileInfo.GRAY32_FLOAT)
		//		pixels[i] = Float.intBitsToFloat(tmp);
		//	else if (fi.fileType == FileInfo.GRAY32_UNSIGNED)
		//		pixels[i] = (float) (tmp & 0xffffffffL);
		//	else
		//		pixels[i] = tmp;
		//}
		if (fi.fileType == FileInfo.GRAY32_FLOAT)
			for (int i = 0; i < nPixels; i++, j += 4)
				pixels[i] = Float.intBitsToFloat(((buffer[j] & 0xff) << 24) | ((buffer[j + 1] & 0xff) << 16) |
						((buffer[j + 2] & 0xff) << 8) | (buffer[j + 3] & 0xff));
		else if (fi.fileType == FileInfo.GRAY32_UNSIGNED)
		{
			final int tmp = ((buffer[j] & 0xff) << 24) | ((buffer[j + 1] & 0xff) << 16) |
					((buffer[j + 2] & 0xff) << 8) | (buffer[j + 3] & 0xff);
			for (int i = 0; i < nPixels; i++, j += 4)
				pixels[i] = tmp & 0xffffffffL;
		}
		else
			for (int i = 0; i < nPixels; i++, j += 4)
				pixels[i] = ((buffer[j] & 0xff) << 24) | ((buffer[j + 1] & 0xff) << 16) |
						((buffer[j + 2] & 0xff) << 8) | (buffer[j + 3] & 0xff);

		return pixels;
	}

	/**
	 * Read 64 bit image.
	 *
	 * @param in
	 *            the input seekable stream
	 * @return the float[] image
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	float[] read64bitImage(ByteArraySeekableStream in) throws IOException
	{
		// We use the bytes direct
		int j = getPositionAndSkipPixelBytes(in, 8);
		final byte[] buffer = in.buffer;
		final float[] pixels = new float[nPixels];
		long tmp;
		long b1, b2, b3, b4, b5, b6, b7, b8;

		for (int i = 0; i < nPixels; i++)
		{
			b1 = buffer[j + 7] & 0xff;
			b2 = buffer[j + 6] & 0xff;
			b3 = buffer[j + 5] & 0xff;
			b4 = buffer[j + 4] & 0xff;
			b5 = buffer[j + 3] & 0xff;
			b6 = buffer[j + 2] & 0xff;
			b7 = buffer[j + 1] & 0xff;
			b8 = buffer[j] & 0xff;
			if (fi.intelByteOrder)
				tmp = (b1 << 56) | (b2 << 48) | (b3 << 40) | (b4 << 32) | (b5 << 24) | (b6 << 16) | (b7 << 8) | b8;
			else
				tmp = (b8 << 56) | (b7 << 48) | (b6 << 40) | (b5 << 32) | (b4 << 24) | (b3 << 16) | (b2 << 8) | b1;
			pixels[i] = (float) Double.longBitsToDouble(tmp);
			j += 8;
		}
		return pixels;
	}

	/**
	 * Gets the position and skips the number of bytes covering all of the pixel data.
	 *
	 * @param in
	 *            the input seekable streamput stream
	 * @param bytesPerPixel
	 *            the bytes per pixel
	 * @return the position before the skip
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private int getPositionAndSkipPixelBytes(ByteArraySeekableStream in, int bytesPerPixel) throws IOException
	{
		final int position = in.p;
		final long skip = bytesPerPixel * ((long) nPixels);
		if (in.skip(skip) != skip)
			throw new EOFException();
		return position;
	}

	/**
	 * Reads the image from the SeekableStream and returns the pixel
	 * array (byte, short, int or float).
	 * Does not close the SeekableStream.
	 *
	 * @param in
	 *            the input seekable stream
	 * @return the object
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public Object readPixels(ByteArraySeekableStream in) throws IOException
	{
		Object pixels;
		switch (fi.fileType)
		{
			case FileInfo.GRAY8:
			case FileInfo.COLOR8:
				bytesPerPixel = 1;
				skip(in);
				pixels = read8bitImage(in);
				break;
			case FileInfo.GRAY16_SIGNED:
			case FileInfo.GRAY16_UNSIGNED:
				bytesPerPixel = 2;
				skip(in);
				pixels = read16bitImage(in);
				break;
			case FileInfo.GRAY32_INT:
			case FileInfo.GRAY32_UNSIGNED:
			case FileInfo.GRAY32_FLOAT:
				bytesPerPixel = 4;
				skip(in);
				pixels = read32bitImage(in);
				break;
			case FileInfo.GRAY64_FLOAT:
				bytesPerPixel = 8;
				skip(in);
				pixels = read64bitImage(in);
				break;
			case FileInfo.RGB:
			case FileInfo.BGR:
			case FileInfo.ARGB:
			case FileInfo.ABGR:
			case FileInfo.BARG:
			case FileInfo.CMYK:
				bytesPerPixel = fi.getBytesPerPixel();
				skip(in);
				pixels = readChunkyRGB(in);
				break;
			case FileInfo.RGB_PLANAR:
				bytesPerPixel = 3;
				skip(in);
				pixels = readPlanarRGB(in);
				break;
			case FileInfo.BITMAP:
				bytesPerPixel = 1;
				skip(in);
				pixels = read1bitImage(in);
				break;
			case FileInfo.RGB48:
				bytesPerPixel = 6;
				skip(in);
				pixels = readRGB48(in);
				break;
			case FileInfo.RGB48_PLANAR:
				bytesPerPixel = 2;
				skip(in);
				pixels = readRGB48Planar(in);
				break;
			case FileInfo.GRAY12_UNSIGNED:
				skip(in);
				final short[] data = read12bitImage(in);
				pixels = data;
				break;
			case FileInfo.GRAY24_UNSIGNED:
				skip(in);
				pixels = read24bitImage(in);
				break;
			default:
				pixels = null;
		}
		//showProgress(1, 1);
		return pixels;
	}

	/**
	 * Skips the specified number of bytes, then reads an image and
	 * returns the pixel array (byte, short, int or float).
	 * Does not close the SeekableStream.
	 *
	 * @param in
	 *            the input seekable stream
	 * @param skipCount
	 *            the skip count
	 * @return the object
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public Object readPixels(ByteArraySeekableStream in, long skipCount) throws IOException
	{
		this.skipCount = skipCount;
		return readPixels(in);
	}

	/** A growable array of bytes. */
	private class ByteVector
	{
		private byte[] data;
		private int size;

		public ByteVector(int initialSize)
		{
			data = new byte[initialSize];
			size = 0;
		}

		public ByteVector(byte[] byteBuffer)
		{
			data = byteBuffer;
			size = 0;
		}

		public void add(byte x)
		{
			if (size >= data.length)
			{
				doubleCapacity();
				add(x);
			}
			else
				data[size++] = x;
		}

		public int size()
		{
			return size;
		}

		public void add(byte[] array)
		{
			final int length = array.length;
			while (data.length - size < length)
				doubleCapacity();
			System.arraycopy(array, 0, data, size, length);
			size += length;
		}

		void doubleCapacity()
		{
			final byte[] tmp = new byte[data.length * 2 + 1];
			System.arraycopy(data, 0, tmp, 0, data.length);
			data = tmp;
		}

		public byte[] toByteArray()
		{
			final byte[] bytes = new byte[size];
			System.arraycopy(data, 0, bytes, 0, size);
			return bytes;
		}
	}
}
