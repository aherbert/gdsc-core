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
 * Copyright (C) 2011 - 2022 Alex Herbert
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

import ij.io.FileInfo;
import java.io.EOFException;
import java.io.IOException;
import uk.ac.sussex.gdsc.core.data.VisibleForTesting;
import uk.ac.sussex.gdsc.core.utils.FileUtils;

/**
 * Reads raw 8-bit, 16-bit or 32-bit (float or Rgb) images from a stream.
 *
 * <p>This is a re-implementation of the {@link ij.io.ImageReader} to use a {@link SeekableStream}
 * interface. The purpose of this class is to:
 *
 * <ul>
 *
 * <li>Allow pixels from very large image files to be efficiently extracted via the
 * {@link SeekableStream} which allows seeking within the file.
 *
 * <li>Allow smaller images to be read into memory once from file and then converted directly to
 * their pixels with no intermediate buffers.
 *
 * <li>
 *
 * </ul>
 *
 * <p>The 'Fast' name comes from the use of specialised methods with a
 * {@link ByteArraySeekableStream}. When using this input {@link SeekableStream} there is no use of
 * a buffer. The byte buffer backing the {@link ByteArraySeekableStream} is used directly. There is
 * no guarantee than this is faster. It will use less memory.
 *
 * <p>This class has been tested to return the same pixels as those generated by the ImageJ
 * ImageReader for supported image types. However this is not a direct replacement for the ImageJ
 * ImageReader: Removed support for progress tracking. Allow IO Exceptions to be thrown. Removed
 * support for compressed images.
 */
public class FastImageReader {
  private static final int BUFFER_BLOCK_SIZE = 8192;

  private final FileInfo fi;
  private final int width;
  private final int height;
  private long skipCount;
  private int bytesPerPixel;
  private int bufferSize;
  private int numberOfPixels;
  private long byteCount;

  // readRgb48() calculates min/max pixel values
  /** The min value read for Rgb48 images. */
  private double min;
  /** The max value read for Rgb48 images. */
  private double max;

  /**
   * Constructs a new ImageReader using a FileInfo object to describe the file to be read.
   *
   * @param fi the file info
   * @see ij.io.FileInfo
   * @throws IllegalArgumentException If the compression level is set
   */
  public FastImageReader(FileInfo fi) {
    if (fi.compression > FileInfo.COMPRESSION_NONE) {
      throw new IllegalArgumentException("Compressed images are not supported");
    }
    this.fi = fi;
    width = fi.width;
    height = fi.height;
    skipCount = fi.getOffset();
  }

  /**
   * Read 8 bit image.
   *
   * @param in the input seekable stream
   * @return the byte[] image
   * @throws IOException Signals that an I/O exception has occurred.
   */
  byte[] read8bitImage(SeekableStream in) throws IOException {
    if (isCompressedOrStrips(fi)) {
      return readCompressed8bitImage(in);
    }
    final byte[] pixels = new byte[numberOfPixels];
    // assume contiguous strips
    int count;
    int totalRead = 0;
    while (totalRead < byteCount) {
      if (totalRead + bufferSize > byteCount) {
        count = (int) (byteCount - totalRead);
      } else {
        count = bufferSize;
      }
      final int actuallyRead = in.read(pixels, totalRead, count);
      handleEof(actuallyRead);
      totalRead += actuallyRead;
    }
    return pixels;
  }

  /**
   * Read 8 bit image.
   *
   * @param in the input seekable stream
   * @return the byte[] image
   * @throws IOException Signals that an I/O exception has occurred.
   */
  byte[] read8bitImage(ByteArraySeekableStream in) throws IOException {
    if (isCompressedOrStrips(fi)) {
      return readCompressed8bitImage(in);
    }

    final int j = getPositionAndSkipPixelBytes(in, 1);
    final byte[] pixels = new byte[numberOfPixels];
    System.arraycopy(in.buffer, j, pixels, 0, numberOfPixels);
    return pixels;
  }

  /**
   * Gets the position and skips the number of bytes covering all of the pixel data.
   *
   * @param in the input seekable stream
   * @param bytesPerPixel the bytes per pixel
   * @return the position before the skip
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private int getPositionAndSkipPixelBytes(ByteArraySeekableStream in, int bytesPerPixel)
      throws IOException {
    final int position = in.position;
    final long skip = bytesPerPixel * ((long) numberOfPixels);
    FileUtils.skip(in, skip);
    return position;
  }

  /**
   * Read compressed 8 bit image.
   *
   * @param in the input seekable stream
   * @return the byte[] image
   * @throws IOException Signals that an I/O exception has occurred.
   */
  byte[] readCompressed8bitImage(SeekableStream in) throws IOException {
    final byte[] pixels = new byte[numberOfPixels];
    int current = 0;
    for (int i = 0; i < fi.stripOffsets.length; i++) {
      in.seek(fi.stripOffsets[i]);
      final byte[] byteArray = new byte[fi.stripLengths[i]];
      int read = 0;
      int left = byteArray.length;
      while (left > 0) {
        final int r = in.read(byteArray, read, left);
        handleEof(r);
        read += r;
        left -= r;
      }
      int length = byteArray.length;
      length = length - (length % fi.width);
      length = Math.min(length, pixels.length - current);
      System.arraycopy(byteArray, 0, pixels, current, length);
      current += length;
    }
    return pixels;
  }

  /**
   * Reads a 16-bit image. Signed pixels are converted to unsigned by adding 32768.
   *
   * @param in the input seekable stream
   * @return the short[] image
   * @throws IOException Signals that an I/O exception has occurred.
   */
  short[] read16bitImage(SeekableStream in) throws IOException {
    if (isCompressedOrStripsAndNotRgb48Planar(fi)) {
      return readCompressed16bitImage(in);
    }
    int pixelsRead;
    final byte[] buffer = new byte[bufferSize];
    final short[] pixels = new short[numberOfPixels];
    long totalRead = 0L;
    int base = 0;
    int count;
    int bufferCount;

    while (totalRead < byteCount) {
      if ((totalRead + bufferSize) > byteCount) {
        bufferSize = (int) (byteCount - totalRead);
      }
      bufferCount = 0;
      while (bufferCount < bufferSize) { // fill the buffer
        count = in.read(buffer, bufferCount, bufferSize - bufferCount);
        handleEof(count);
        bufferCount += count;
      }
      totalRead += bufferSize;
      pixelsRead = bufferSize / bytesPerPixel;
      if (fi.intelByteOrder) {
        if (fi.fileType == FileInfo.GRAY16_SIGNED) {
          for (int i = base, j = 0; i < (base + pixelsRead); i++, j += 2) {
            pixels[i] = (short) ((((buffer[j + 1] & 0xff) << 8) | (buffer[j] & 0xff)) + 32768);
          }
        } else {
          for (int i = base, j = 0; i < (base + pixelsRead); i++, j += 2) {
            pixels[i] = (short) (((buffer[j + 1] & 0xff) << 8) | (buffer[j] & 0xff));
          }
        }
      } else if (fi.fileType == FileInfo.GRAY16_SIGNED) {
        for (int i = base, j = 0; i < (base + pixelsRead); i++, j += 2) {
          pixels[i] = (short) ((((buffer[j] & 0xff) << 8) | (buffer[j + 1] & 0xff)) + 32768);
        }
      } else {
        for (int i = base, j = 0; i < (base + pixelsRead); i++, j += 2) {
          pixels[i] = (short) (((buffer[j] & 0xff) << 8) | (buffer[j + 1] & 0xff));
        }
      }
      base += pixelsRead;
    }
    return pixels;
  }

  /**
   * Reads a 16-bit image. Signed pixels are converted to unsigned by adding 32768.
   *
   * @param in the input seekable stream
   * @return the short[] image
   * @throws IOException Signals that an I/O exception has occurred.
   */
  short[] read16bitImage(ByteArraySeekableStream in) throws IOException {
    if (isCompressedOrStripsAndNotRgb48Planar(fi)) {
      return readCompressed16bitImage(in);
    }

    // We use the bytes direct
    int index = getPositionAndSkipPixelBytes(in, 2);
    final byte[] buffer = in.buffer;
    final short[] pixels = new short[numberOfPixels];

    if (fi.intelByteOrder) {
      if (fi.fileType == FileInfo.GRAY16_SIGNED) {
        for (int i = 0; i < numberOfPixels; i++, index += 2) {
          pixels[i] =
              (short) ((((buffer[index + 1] & 0xff) << 8) | (buffer[index] & 0xff)) + 32768);
        }
      } else {
        for (int i = 0; i < numberOfPixels; i++, index += 2) {
          pixels[i] = (short) (((buffer[index + 1] & 0xff) << 8) | (buffer[index] & 0xff));
        }
      }
    } else if (fi.fileType == FileInfo.GRAY16_SIGNED) {
      for (int i = 0; i < numberOfPixels; i++, index += 2) {
        pixels[i] = (short) ((((buffer[index] & 0xff) << 8) | (buffer[index + 1] & 0xff)) + 32768);
      }
    } else {
      for (int i = 0; i < numberOfPixels; i++, index += 2) {
        pixels[i] = (short) (((buffer[index] & 0xff) << 8) | (buffer[index + 1] & 0xff));
      }
    }

    return pixels;
  }

  /**
   * Read compressed 16 bit image.
   *
   * @param in the input seekable stream
   * @return the short[] image
   * @throws IOException Signals that an I/O exception has occurred.
   */
  short[] readCompressed16bitImage(SeekableStream in) throws IOException {
    final short[] pixels = new short[numberOfPixels];
    int base = 0;
    for (int k = 0; k < fi.stripOffsets.length; k++) {
      in.seek(fi.stripOffsets[k]);
      final byte[] byteArray = new byte[fi.stripLengths[k]];
      int read = 0;
      int left = byteArray.length;
      while (left > 0) {
        final int r = in.read(byteArray, read, left);
        handleEof(r);
        read += r;
        left -= r;
      }
      int pixelsRead = byteArray.length / bytesPerPixel;
      pixelsRead = pixelsRead - (pixelsRead % fi.width);
      final int pmax = getMaxPixels(pixelsRead, base);
      if (fi.intelByteOrder) {
        for (int i = base, j = 0; i < pmax; i++, j += 2) {
          pixels[i] = (short) (((byteArray[j + 1] & 0xff) << 8) | (byteArray[j] & 0xff));
        }
      } else {
        for (int i = base, j = 0; i < pmax; i++, j += 2) {
          pixels[i] = (short) (((byteArray[j] & 0xff) << 8) | (byteArray[j + 1] & 0xff));
        }
      }
      base += pixelsRead;
    }
    if (fi.fileType == FileInfo.GRAY16_SIGNED) {
      // convert to unsigned
      for (int i = 0; i < numberOfPixels; i++) {
        pixels[i] = (short) (pixels[i] + 32768);
      }
    }
    return pixels;
  }

  /**
   * Read 32 bit image.
   *
   * @param in the input seekable stream
   * @return the float[] image
   * @throws IOException Signals that an I/O exception has occurred.
   */
  float[] read32bitImage(SeekableStream in) throws IOException {
    if (isCompressedOrStrips(fi)) {
      return readCompressed32bitImage(in);
    }
    int pixelsRead;
    final byte[] buffer = new byte[bufferSize];
    final float[] pixels = new float[numberOfPixels];
    long totalRead = 0L;
    int base = 0;
    int count;
    int bufferCount;
    int tmp;

    while (totalRead < byteCount) {
      if ((totalRead + bufferSize) > byteCount) {
        bufferSize = (int) (byteCount - totalRead);
      }
      bufferCount = 0;
      while (bufferCount < bufferSize) { // fill the buffer
        count = in.read(buffer, bufferCount, bufferSize - bufferCount);
        handleEof(count);
        bufferCount += count;
      }
      totalRead += bufferSize;
      pixelsRead = bufferSize / bytesPerPixel;
      final int pmax = getMaxPixels(pixelsRead, base);
      int index = 0;
      if (fi.intelByteOrder) {
        for (int i = base; i < pmax; i++) {
          tmp = ((buffer[index + 3] & 0xff) << 24) | ((buffer[index + 2] & 0xff) << 16)
              | ((buffer[index + 1] & 0xff) << 8) | (buffer[index] & 0xff);
          if (fi.fileType == FileInfo.GRAY32_FLOAT) {
            pixels[i] = Float.intBitsToFloat(tmp);
          } else if (fi.fileType == FileInfo.GRAY32_UNSIGNED) {
            pixels[i] = tmp & 0xffffffffL;
          } else {
            pixels[i] = tmp;
          }
          index += 4;
        }
      } else {
        for (int i = base; i < pmax; i++) {
          tmp = ((buffer[index] & 0xff) << 24) | ((buffer[index + 1] & 0xff) << 16)
              | ((buffer[index + 2] & 0xff) << 8) | (buffer[index + 3] & 0xff);
          if (fi.fileType == FileInfo.GRAY32_FLOAT) {
            pixels[i] = Float.intBitsToFloat(tmp);
          } else if (fi.fileType == FileInfo.GRAY32_UNSIGNED) {
            pixels[i] = tmp & 0xffffffffL;
          } else {
            pixels[i] = tmp;
          }
          index += 4;
        }
      }
      base += pixelsRead;
    }
    return pixels;
  }

  /**
   * Read 32 bit image.
   *
   * @param in the input seekable stream
   * @return the float[] image
   * @throws IOException Signals that an I/O exception has occurred.
   */
  float[] read32bitImage(ByteArraySeekableStream in) throws IOException {
    if (isCompressedOrStrips(fi)) {
      return readCompressed32bitImage(in);
    }

    // We use the bytes direct
    int index = getPositionAndSkipPixelBytes(in, 4);
    final byte[] buffer = in.buffer;
    final float[] pixels = new float[numberOfPixels];

    if (fi.intelByteOrder) {
      if (fi.fileType == FileInfo.GRAY32_FLOAT) {
        for (int i = 0; i < numberOfPixels; i++, index += 4) {
          pixels[i] = Float.intBitsToFloat(
              ((buffer[index + 3] & 0xff) << 24) | ((buffer[index + 2] & 0xff) << 16)
                  | ((buffer[index + 1] & 0xff) << 8) | (buffer[index] & 0xff));
        }
      } else if (fi.fileType == FileInfo.GRAY32_UNSIGNED) {
        for (int i = 0; i < numberOfPixels; i++, index += 4) {
          final int tmp = ((buffer[index + 3] & 0xff) << 24) | ((buffer[index + 2] & 0xff) << 16)
              | ((buffer[index + 1] & 0xff) << 8) | (buffer[index] & 0xff);
          pixels[i] = tmp & 0xffffffffL;
        }
      } else {
        for (int i = 0; i < numberOfPixels; i++, index += 4) {
          pixels[i] = ((buffer[index + 3] & 0xff) << 24) | ((buffer[index + 2] & 0xff) << 16)
              | ((buffer[index + 1] & 0xff) << 8) | (buffer[index] & 0xff);
        }
      }
    } else if (fi.fileType == FileInfo.GRAY32_FLOAT) {
      for (int i = 0; i < numberOfPixels; i++, index += 4) {
        pixels[i] =
            Float.intBitsToFloat(((buffer[index] & 0xff) << 24) | ((buffer[index + 1] & 0xff) << 16)
                | ((buffer[index + 2] & 0xff) << 8) | (buffer[index + 3] & 0xff));
      }
    } else if (fi.fileType == FileInfo.GRAY32_UNSIGNED) {
      for (int i = 0; i < numberOfPixels; i++, index += 4) {
        final int tmp = ((buffer[index] & 0xff) << 24) | ((buffer[index + 1] & 0xff) << 16)
            | ((buffer[index + 2] & 0xff) << 8) | (buffer[index + 3] & 0xff);
        pixels[i] = tmp & 0xffffffffL;
      }
    } else {
      for (int i = 0; i < numberOfPixels; i++, index += 4) {
        pixels[i] = ((buffer[index] & 0xff) << 24) | ((buffer[index + 1] & 0xff) << 16)
            | ((buffer[index + 2] & 0xff) << 8) | (buffer[index + 3] & 0xff);
      }
    }

    return pixels;
  }

  /**
   * Read compressed 32 bit image.
   *
   * @param in the input seekable stream
   * @return the float[] image
   * @throws IOException Signals that an I/O exception has occurred.
   */
  float[] readCompressed32bitImage(SeekableStream in) throws IOException {
    final float[] pixels = new float[numberOfPixels];
    int base = 0;
    for (int k = 0; k < fi.stripOffsets.length; k++) {
      in.seek(fi.stripOffsets[k]);
      final byte[] byteArray = new byte[fi.stripLengths[k]];
      int read = 0;
      int left = byteArray.length;
      while (left > 0) {
        final int r = in.read(byteArray, read, left);
        handleEof(r);
        read += r;
        left -= r;
      }
      int pixelsRead = byteArray.length / bytesPerPixel;
      pixelsRead = pixelsRead - (pixelsRead % fi.width);
      final int pmax = getMaxPixels(pixelsRead, base);
      int tmp;
      if (fi.intelByteOrder) {
        for (int i = base, j = 0; i < pmax; i++, j += 4) {
          tmp = ((byteArray[j + 3] & 0xff) << 24) | ((byteArray[j + 2] & 0xff) << 16)
              | ((byteArray[j + 1] & 0xff) << 8) | (byteArray[j] & 0xff);
          if (fi.fileType == FileInfo.GRAY32_FLOAT) {
            pixels[i] = Float.intBitsToFloat(tmp);
          } else if (fi.fileType == FileInfo.GRAY32_UNSIGNED) {
            pixels[i] = tmp & 0xffffffffL;
          } else {
            pixels[i] = tmp;
          }
        }
      } else {
        for (int i = base, j = 0; i < pmax; i++, j += 4) {
          tmp = ((byteArray[j] & 0xff) << 24) | ((byteArray[j + 1] & 0xff) << 16)
              | ((byteArray[j + 2] & 0xff) << 8) | (byteArray[j + 3] & 0xff);
          if (fi.fileType == FileInfo.GRAY32_FLOAT) {
            pixels[i] = Float.intBitsToFloat(tmp);
          } else if (fi.fileType == FileInfo.GRAY32_UNSIGNED) {
            pixels[i] = tmp & 0xffffffffL;
          } else {
            pixels[i] = tmp;
          }
        }
      }
      base += pixelsRead;
    }
    return pixels;
  }

  /**
   * Read 64 bit image.
   *
   * @param in the input seekable stream
   * @return the float[] image
   * @throws IOException Signals that an I/O exception has occurred.
   */
  float[] read64bitImage(SeekableStream in) throws IOException {
    int pixelsRead;
    final byte[] buffer = new byte[bufferSize];
    final float[] pixels = new float[numberOfPixels];
    long totalRead = 0L;
    int base = 0;
    int count;
    int bufferCount;
    long tmp;

    while (totalRead < byteCount) {
      if ((totalRead + bufferSize) > byteCount) {
        bufferSize = (int) (byteCount - totalRead);
      }
      bufferCount = 0;
      while (bufferCount < bufferSize) { // fill the buffer
        count = in.read(buffer, bufferCount, bufferSize - bufferCount);
        handleEof(count);
        bufferCount += count;
      }
      totalRead += bufferSize;
      pixelsRead = bufferSize / bytesPerPixel;
      int index = 0;
      for (int i = base; i < (base + pixelsRead); i++) {
        final long b1 = buffer[index + 7] & 0xff;
        final long b2 = buffer[index + 6] & 0xff;
        final long b3 = buffer[index + 5] & 0xff;
        final long b4 = buffer[index + 4] & 0xff;
        final long b5 = buffer[index + 3] & 0xff;
        final long b6 = buffer[index + 2] & 0xff;
        final long b7 = buffer[index + 1] & 0xff;
        final long b8 = buffer[index] & 0xff;
        if (fi.intelByteOrder) {
          tmp = (b1 << 56) | (b2 << 48) | (b3 << 40) | (b4 << 32) | (b5 << 24) | (b6 << 16)
              | (b7 << 8) | b8;
        } else {
          tmp = (b8 << 56) | (b7 << 48) | (b6 << 40) | (b5 << 32) | (b4 << 24) | (b3 << 16)
              | (b2 << 8) | b1;
        }
        pixels[i] = (float) Double.longBitsToDouble(tmp);
        index += 8;
      }
      base += pixelsRead;
    }
    return pixels;
  }

  /**
   * Read 64 bit image.
   *
   * @param in the input seekable stream
   * @return the float[] image
   * @throws IOException Signals that an I/O exception has occurred.
   */
  float[] read64bitImage(ByteArraySeekableStream in) throws IOException {
    // We use the bytes direct
    int index = getPositionAndSkipPixelBytes(in, 8);
    final byte[] buffer = in.buffer;
    final float[] pixels = new float[numberOfPixels];

    for (int i = 0; i < numberOfPixels; i++) {
      final long b1 = buffer[index + 7] & 0xff;
      final long b2 = buffer[index + 6] & 0xff;
      final long b3 = buffer[index + 5] & 0xff;
      final long b4 = buffer[index + 4] & 0xff;
      final long b5 = buffer[index + 3] & 0xff;
      final long b6 = buffer[index + 2] & 0xff;
      final long b7 = buffer[index + 1] & 0xff;
      final long b8 = buffer[index] & 0xff;
      long tmp;
      if (fi.intelByteOrder) {
        tmp = (b1 << 56) | (b2 << 48) | (b3 << 40) | (b4 << 32) | (b5 << 24) | (b6 << 16)
            | (b7 << 8) | b8;
      } else {
        tmp = (b8 << 56) | (b7 << 48) | (b6 << 40) | (b5 << 32) | (b4 << 24) | (b3 << 16)
            | (b2 << 8) | b1;
      }
      pixels[i] = (float) Double.longBitsToDouble(tmp);
      index += 8;
    }
    return pixels;
  }

  /**
   * Read chunky Rgb.
   *
   * @param in the input seekable stream
   * @return the int[] image
   * @throws IOException Signals that an I/O exception has occurred.
   */
  int[] readChunkyRgb(SeekableStream in) throws IOException {
    int pixelsRead;
    bufferSize = 24 * width;
    final byte[] buffer = new byte[bufferSize];
    final int[] pixels = new int[numberOfPixels];
    long totalRead = 0L;
    int base = 0;
    int count;
    int bufferCount;

    while (totalRead < byteCount) {
      if ((totalRead + bufferSize) > byteCount) {
        bufferSize = (int) (byteCount - totalRead);
      }
      bufferCount = 0;
      while (bufferCount < bufferSize) { // fill the buffer
        count = in.read(buffer, bufferCount, bufferSize - bufferCount);
        handleEof(count);
        bufferCount += count;
      }
      totalRead += bufferSize;
      pixelsRead = bufferSize / bytesPerPixel;
      final boolean bgr = fi.fileType == FileInfo.BGR;
      int index = 0;
      for (int i = base; i < (base + pixelsRead); i++) {
        int red;
        int green;
        int blue;
        if (bytesPerPixel == 4) {
          if (fi.fileType == FileInfo.BARG) { // MCID
            blue = buffer[index++] & 0xff;
            index++; // ignore alpha byte
            red = buffer[index++] & 0xff;
            green = buffer[index++] & 0xff;
          } else if (fi.fileType == FileInfo.ABGR) {
            blue = buffer[index++] & 0xff;
            green = buffer[index++] & 0xff;
            red = buffer[index++] & 0xff;
            index++; // ignore alpha byte
          } else if (fi.fileType == FileInfo.CMYK) {
            red = buffer[index++] & 0xff; // c
            green = buffer[index++] & 0xff; // m
            blue = buffer[index++] & 0xff; // y
            final int black = buffer[index++] & 0xff; // k
            if (black > 0) { // if k>0 then c=c*(1-k)+k
              red = ((red * (256 - black)) >> 8) + black;
              green = ((green * (256 - black)) >> 8) + black;
              blue = ((blue * (256 - black)) >> 8) + black;
            } // else r=1-c, g=1-m and b=1-y, which IJ does by inverting image
          } else { // ARGB
            red = buffer[index++] & 0xff;
            green = buffer[index++] & 0xff;
            blue = buffer[index++] & 0xff;
            index++; // ignore alpha byte
          }
        } else {
          red = buffer[index++] & 0xff;
          green = buffer[index++] & 0xff;
          blue = buffer[index++] & 0xff;
        }
        if (bgr) {
          pixels[i] = 0xff000000 | (blue << 16) | (green << 8) | red;
        } else {
          pixels[i] = 0xff000000 | (red << 16) | (green << 8) | blue;
        }
      }
      base += pixelsRead;
    }
    return pixels;
  }

  /**
   * Read planar Rgb.
   *
   * @param in the input seekable stream
   * @return the int[] image
   * @throws IOException Signals that an I/O exception has occurred.
   */
  int[] readPlanarRgb(SeekableStream in) throws IOException {
    final int planeSize = numberOfPixels; // 1/3 image size
    final byte[] buffer = new byte[planeSize];
    final int[] pixels = new int[numberOfPixels];

    in.readFully(buffer);
    for (int i = 0; i < planeSize; i++) {
      final int red = buffer[i] & 0xff;
      pixels[i] = 0xff000000 | (red << 16);
    }

    in.readFully(buffer);
    for (int i = 0; i < planeSize; i++) {
      final int green = buffer[i] & 0xff;
      pixels[i] |= green << 8;
    }

    in.readFully(buffer);
    for (int i = 0; i < planeSize; i++) {
      final int blue = buffer[i] & 0xff;
      pixels[i] |= blue;
    }

    return pixels;
  }

  /**
   * Read Rgb 48.
   *
   * @param in the input seekable stream
   * @return the object
   * @throws IOException Signals that an I/O exception has occurred.
   */
  Object readRgb48(SeekableStream in) throws IOException {
    if (fi.stripLengths == null) {
      fi.stripLengths = new int[fi.stripOffsets.length];
      fi.stripLengths[0] = width * height * bytesPerPixel;
    }
    final int channels = getRbg48Channels(fi);
    final short[][] stack = new short[channels][numberOfPixels];
    int pixel = 0;
    int localMin = 65535;
    int localMax = 0;
    for (int i = 0; i < fi.stripOffsets.length; i++) {
      if (i > 0) {
        final long skip = (fi.stripOffsets[i] & 0xffffffffL)
            - (fi.stripOffsets[i - 1] & 0xffffffffL) - fi.stripLengths[i - 1];
        FileUtils.skip(in, skip);
      }
      int len = fi.stripLengths[i];
      final int bytesToGo = (numberOfPixels - pixel) * channels * 2;
      if (len > bytesToGo) {
        len = bytesToGo;
      }
      final byte[] buffer = new byte[len];
      in.readFully(buffer);
      int value;
      int channel = 0;
      final boolean intel = fi.intelByteOrder;
      for (int base = 0; base < len; base += 2) {
        if (intel) {
          value = ((buffer[base + 1] & 0xff) << 8) | (buffer[base] & 0xff);
        } else {
          value = ((buffer[base] & 0xff) << 8) | (buffer[base + 1] & 0xff);
        }
        if (value < localMin) {
          localMin = value;
        }
        if (value > localMax) {
          localMax = value;
        }
        stack[channel][pixel] = (short) (value);
        channel++;
        if (channel == channels) {
          channel = 0;
          pixel++;
        }
      }
    }
    this.min = localMin;
    this.max = localMax;
    return stack;
  }

  /**
   * Gets the the number of channels for the RGB 48 image.
   *
   * @param fi the file info
   * @return the channels
   */
  @VisibleForTesting
  static int getRbg48Channels(FileInfo fi) {
    final int channels = fi.samplesPerPixel;
    return channels == 1 ? 3 : channels;
  }

  /**
   * Gets min value read for Rgb48 images.
   *
   * @return the min
   */
  public double getMinRgb48Value() {
    return min;
  }

  /**
   * Gets max value read for Rgb48 images.
   *
   * @return the max
   */
  public double getMaxRgb48Value() {
    return max;
  }

  /**
   * Read Rgb 48 planar.
   *
   * @param in the input seekable stream
   * @return the object
   * @throws IOException Signals that an I/O exception has occurred.
   */
  Object readRgb48Planar(SeekableStream in) throws IOException {
    final int channels = getRbg48Channels(fi);
    final Object[] stack = new Object[channels];
    for (int i = 0; i < channels; i++) {
      stack[i] = read16bitImage(in);
    }
    return stack;
  }

  /**
   * Read 12 bit image.
   *
   * @param in the input seekable stream
   * @return the short[] image
   * @throws IOException Signals that an I/O exception has occurred.
   */
  short[] read12bitImage(SeekableStream in) throws IOException {
    int bytesPerLine = (int) (width * 1.5);
    if ((width & 1) == 1) {
      bytesPerLine++; // add 1 if odd
    }
    final byte[] buffer = new byte[bytesPerLine * height];
    final short[] pixels = new short[numberOfPixels];
    in.readFully(buffer);
    for (int y = 0; y < height; y++) {
      int index1 = y * bytesPerLine;
      final int index2 = y * width;
      int count = 0;
      while (count < width) {
        pixels[index2 + count] =
            (short) (((buffer[index1] & 0xff) * 16) + ((buffer[index1 + 1] >> 4) & 0xf));
        count++;
        if (count == width) {
          break;
        }
        pixels[index2 + count] =
            (short) (((buffer[index1 + 1] & 0xf) * 256) + (buffer[index1 + 2] & 0xff));
        count++;
        index1 += 3;
      }
    }
    return pixels;
  }

  /**
   * Read 24 bit image.
   *
   * @param in the input seekable stream
   * @return the float[] image
   * @throws IOException Signals that an I/O exception has occurred.
   */
  float[] read24bitImage(SeekableStream in) throws IOException {
    final byte[] buffer = new byte[width * 3];
    final float[] pixels = new float[numberOfPixels];
    for (int y = 0; y < height; y++) {
      in.readFully(buffer);
      int count = 0;
      for (int x = 0; x < width; x++) {
        final int b1 = buffer[count++] & 0xff;
        final int b2 = buffer[count++] & 0xff;
        final int b3 = buffer[count++] & 0xff;
        pixels[x + y * width] = (b3 << 16) | (b2 << 8) | b1;
      }
    }
    return pixels;
  }

  /**
   * Read 1 bit image.
   *
   * @param in the input seekable stream
   * @return the byte[] image
   * @throws IOException Signals that an I/O exception has occurred.
   */
  byte[] read1bitImage(SeekableStream in) throws IOException {
    final int scan = (int) Math.ceil(width / 8.0);
    final int len = scan * height;
    final byte[] buffer = new byte[len];
    final byte[] pixels = new byte[numberOfPixels];
    in.readFully(buffer);
    for (int y = 0; y < height; y++) {
      final int offset = y * scan;
      int index = y * width;
      for (int x = 0; x < scan; x++) {
        final int value1 = buffer[offset + x] & 0xff;
        for (int i = 7; i >= 0; i--) {
          final int value2 = (value1 & (1 << i)) != 0 ? 255 : 0;
          if (index < pixels.length) {
            pixels[index++] = (byte) value2;
          }
        }
      }
    }
    return pixels;
  }

  /**
   * Checks the compression value is set or if the file info has strip offsets. The image must be
   * read using strips.
   *
   * @param fi the file info
   * @return true if read using (compressed) strips
   */
  @VisibleForTesting
  static boolean isCompressedOrStrips(FileInfo fi) {
    return fi.compression > FileInfo.COMPRESSION_NONE
        || (fi.stripOffsets != null && fi.stripOffsets.length > 1);
  }

  /**
   * Checks the compression value is set or if the file info has strip offsets and is not RBG48
   * Planar. The image must be read using strips.
   *
   * @param fi the file info
   * @return true if read using (compressed) strips
   */
  @VisibleForTesting
  static boolean isCompressedOrStripsAndNotRgb48Planar(FileInfo fi) {
    return fi.compression > FileInfo.COMPRESSION_NONE
        || ((fi.stripOffsets != null && fi.stripOffsets.length > 1)
            && fi.fileType != FileInfo.RGB48_PLANAR);
  }

  /**
   * Handle End-of-file (EOF) by raising an EOF exception.
   *
   * @param count the count of bytes read
   * @throws EOFException the EOF exception
   */
  @VisibleForTesting
  static void handleEof(final int count) throws EOFException {
    if (count < 0) {
      throw new EOFException();
    }
  }

  /**
   * Gets the maximum pixel index.
   *
   * @param pixelsRead the new number of pixels
   * @param base the current index
   * @return the maximum pixel index
   */
  private int getMaxPixels(int pixelsRead, int base) {
    // overflow safe
    // pmax = Math.min(base + pixelsRead, numberOfPixel)
    return Math.min(base + pixelsRead - numberOfPixels, 0) + numberOfPixels;
  }

  /**
   * Skip the input by the current value of skip count and then initialise the buffer for reading.
   *
   * @param in the input seekable stream
   * @throws IOException Signals that an I/O exception has occurred.
   */
  void skip(SeekableStream in) throws IOException {
    FileUtils.skip(in, skipCount);
    byteCount = ((long) width) * height * bytesPerPixel;
    if (fi.fileType == FileInfo.BITMAP) {
      int scan = width / 8;
      final int pad = width % 8;
      if (pad > 0) {
        scan++;
      }
      byteCount = (long) scan * height;
    }
    numberOfPixels = width * height;
    bufferSize = getBufferSize(byteCount);
  }

  /**
   * Gets the buffer size.
   *
   * @param byteCount the byte count
   * @return the buffer size
   */
  @VisibleForTesting
  static int getBufferSize(long byteCount) {
    // Divide by 32
    final long block = byteCount >>> 5;
    if (block < BUFFER_BLOCK_SIZE) {
      return BUFFER_BLOCK_SIZE;
    }
    // Convert to integer
    final int bufferSize = (int) Math.min(block, 1 << 30);
    // Clip to sensible blocks
    return (bufferSize / BUFFER_BLOCK_SIZE) * BUFFER_BLOCK_SIZE;
  }

  /**
   * Reads the image from the SeekableStream and returns the pixel array (byte, short, int or
   * float). Does not close the SeekableStream.
   *
   * @param in the input seekable stream
   * @return the object
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public Object readPixels(SeekableStream in) throws IOException {
    switch (fi.fileType) {
      case FileInfo.GRAY8:
      case FileInfo.COLOR8:
        bytesPerPixel = 1;
        skip(in);
        return read8bitImage(in);
      case FileInfo.GRAY16_SIGNED:
      case FileInfo.GRAY16_UNSIGNED:
        bytesPerPixel = 2;
        skip(in);
        return read16bitImage(in);
      case FileInfo.GRAY32_INT:
      case FileInfo.GRAY32_UNSIGNED:
      case FileInfo.GRAY32_FLOAT:
        bytesPerPixel = 4;
        skip(in);
        return read32bitImage(in);
      case FileInfo.GRAY64_FLOAT:
        bytesPerPixel = 8;
        skip(in);
        return read64bitImage(in);
      case FileInfo.RGB:
      case FileInfo.BGR:
      case FileInfo.ARGB:
      case FileInfo.ABGR:
      case FileInfo.BARG:
      case FileInfo.CMYK:
        bytesPerPixel = fi.getBytesPerPixel();
        skip(in);
        return readChunkyRgb(in);
      case FileInfo.RGB_PLANAR:
        bytesPerPixel = 3;
        skip(in);
        return readPlanarRgb(in);
      case FileInfo.BITMAP:
        bytesPerPixel = 1;
        skip(in);
        return read1bitImage(in);
      case FileInfo.RGB48:
        bytesPerPixel = 6;
        skip(in);
        return readRgb48(in);
      case FileInfo.RGB48_PLANAR:
        bytesPerPixel = 2;
        skip(in);
        return readRgb48Planar(in);
      case FileInfo.GRAY12_UNSIGNED:
        skip(in);
        final short[] data = read12bitImage(in);
        return data;
      case FileInfo.GRAY24_UNSIGNED:
        skip(in);
        return read24bitImage(in);
      default:
        throw new IOException("FastImageReader: unknown file type " + fi.fileType);
    }
  }

  /**
   * Skips the specified number of bytes, then reads an image and returns the pixel array (byte,
   * short, int or float). Does not close the SeekableStream.
   *
   * @param in the input seekable stream
   * @param skipCount the skip count
   * @return the object
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public Object readPixels(SeekableStream in, long skipCount) throws IOException {
    this.skipCount = skipCount;
    return readPixels(in);
  }

  /**
   * Reads the image from the ByteArraySeekableStream and returns the pixel array (byte, short, int
   * or float). Does not close the ByteArraySeekableStream.
   *
   * <p>This method has a performance advantage over {@link #readPixels(SeekableStream)} as the
   * backing buffer can be used directly.
   *
   * @param in the input seekable stream
   * @return the object
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public Object readPixels(ByteArraySeekableStream in) throws IOException {
    switch (fi.fileType) {
      case FileInfo.GRAY8:
      case FileInfo.COLOR8:
        bytesPerPixel = 1;
        skip(in);
        return read8bitImage(in);
      case FileInfo.GRAY16_SIGNED:
      case FileInfo.GRAY16_UNSIGNED:
        bytesPerPixel = 2;
        skip(in);
        return read16bitImage(in);
      case FileInfo.GRAY32_INT:
      case FileInfo.GRAY32_UNSIGNED:
      case FileInfo.GRAY32_FLOAT:
        bytesPerPixel = 4;
        skip(in);
        return read32bitImage(in);
      case FileInfo.GRAY64_FLOAT:
        bytesPerPixel = 8;
        skip(in);
        return read64bitImage(in);
      case FileInfo.RGB:
      case FileInfo.BGR:
      case FileInfo.ARGB:
      case FileInfo.ABGR:
      case FileInfo.BARG:
      case FileInfo.CMYK:
        bytesPerPixel = fi.getBytesPerPixel();
        skip(in);
        return readChunkyRgb(in);
      case FileInfo.RGB_PLANAR:
        bytesPerPixel = 3;
        skip(in);
        return readPlanarRgb(in);
      case FileInfo.BITMAP:
        bytesPerPixel = 1;
        skip(in);
        return read1bitImage(in);
      case FileInfo.RGB48:
        bytesPerPixel = 6;
        skip(in);
        return readRgb48(in);
      case FileInfo.RGB48_PLANAR:
        bytesPerPixel = 2;
        skip(in);
        return readRgb48Planar(in);
      case FileInfo.GRAY12_UNSIGNED:
        skip(in);
        final short[] data = read12bitImage(in);
        return data;
      case FileInfo.GRAY24_UNSIGNED:
        skip(in);
        return read24bitImage(in);
      default:
        throw new IOException("FastImageReader: unknown file type " + fi.fileType);
    }
  }

  /**
   * Skips the specified number of bytes, then reads an image and returns the pixel array (byte,
   * short, int or float). Does not close the ByteARraySeekableStream.
   *
   * <p>This method has a performance advantage over {@link #readPixels(SeekableStream)} as the
   * backing buffer can be used directly.
   *
   * @param in the input seekable stream
   * @param skipCount the skip count
   * @return the object
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public Object readPixels(ByteArraySeekableStream in, long skipCount) throws IOException {
    this.skipCount = skipCount;
    return readPixels(in);
  }
}
