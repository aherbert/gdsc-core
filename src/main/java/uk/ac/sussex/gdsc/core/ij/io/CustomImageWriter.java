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

import ij.VirtualStack;
import ij.io.FileInfo;
import ij.process.ImageProcessor;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Writes raw 8-bit, 16-bit or 32-bit (float or Rgb) images to a stream.
 *
 * <p>This is a re-implementation of the {@link ij.io.ImageWriter} to remove support for progress
 * tracking.
 *
 * <p>Also removed support for flipping a virtual stack image if the fileName is
 * {@code "FlipTheseImages"}.
 */
public class CustomImageWriter {
  /** The file info. */
  private final FileInfo fi;

  /**
   * Create a new instance.
   *
   * @param fi the file info
   */
  public CustomImageWriter(FileInfo fi) {
    this.fi = fi;
  }

  private void write8BitImage(OutputStream out, byte[] pixels) throws IOException {
    int bytesWritten = 0;
    final int size = fi.width * fi.height;
    int count = 8192;
    while (bytesWritten < size) {
      if ((bytesWritten + count) > size) {
        count = size - bytesWritten;
      }
      out.write(pixels, bytesWritten, count);
      bytesWritten += count;
    }
  }

  private void write8BitStack(OutputStream out, Object[] stack) throws IOException {
    for (int i = 0; i < fi.nImages; i++) {
      write8BitImage(out, (byte[]) stack[i]);
    }
  }

  private void write8BitVirtualStack(OutputStream out, VirtualStack virtualStack)
      throws IOException {
    for (int i = 1; i <= fi.nImages; i++) {
      final ImageProcessor ip = virtualStack.getProcessor(i);
      final byte[] pixels = (byte[]) ip.getPixels();
      write8BitImage(out, pixels);
    }
  }

  private void write16BitImage(OutputStream out, short[] pixels) throws IOException {
    long bytesWritten = 0L;
    final long size = 2L * fi.width * fi.height;
    int count = 8192;
    final byte[] buffer = new byte[count];

    while (bytesWritten < size) {
      if ((bytesWritten + count) > size) {
        count = (int) (size - bytesWritten);
      }
      int index = (int) (bytesWritten / 2L);
      int value;
      if (fi.intelByteOrder) {
        for (int i = 0; i < count; i += 2) {
          value = pixels[index];
          buffer[i] = (byte) value;
          buffer[i + 1] = (byte) (value >>> 8);
          index++;
        }
      } else {
        for (int i = 0; i < count; i += 2) {
          value = pixels[index];
          buffer[i] = (byte) (value >>> 8);
          buffer[i + 1] = (byte) value;
          index++;
        }
      }
      out.write(buffer, 0, count);
      bytesWritten += count;
    }
  }

  private void write16BitStack(OutputStream out, Object[] stack) throws IOException {
    for (int i = 0; i < fi.nImages; i++) {
      write16BitImage(out, (short[]) stack[i]);
    }
  }

  private void write16BitVirtualStack(OutputStream out, VirtualStack virtualStack)
      throws IOException {
    for (int i = 1; i <= fi.nImages; i++) {
      final ImageProcessor ip = virtualStack.getProcessor(i);
      final short[] pixels = (short[]) ip.getPixels();
      write16BitImage(out, pixels);
    }
  }

  private void writeRgb48Image(OutputStream out, Object[] stack) throws IOException {
    final short[] r = (short[]) stack[0];
    final short[] g = (short[]) stack[1];
    final short[] b = (short[]) stack[2];
    final int count = fi.width * 6;
    final byte[] buffer = new byte[count];
    for (int line = 0; line < fi.height; line++) {
      int index2 = 0;
      int index1 = line * fi.width;
      int value;
      if (fi.intelByteOrder) {
        for (int i = 0; i < fi.width; i++) {
          value = r[index1];
          buffer[index2++] = (byte) value;
          buffer[index2++] = (byte) (value >>> 8);
          value = g[index1];
          buffer[index2++] = (byte) value;
          buffer[index2++] = (byte) (value >>> 8);
          value = b[index1];
          buffer[index2++] = (byte) value;
          buffer[index2++] = (byte) (value >>> 8);
          index1++;
        }
      } else {
        for (int i = 0; i < fi.width; i++) {
          value = r[index1];
          buffer[index2++] = (byte) (value >>> 8);
          buffer[index2++] = (byte) value;
          value = g[index1];
          buffer[index2++] = (byte) (value >>> 8);
          buffer[index2++] = (byte) value;
          value = b[index1];
          buffer[index2++] = (byte) (value >>> 8);
          buffer[index2++] = (byte) value;
          index1++;
        }
      }
      out.write(buffer, 0, count);
    }
  }

  private void writeFloatImage(OutputStream out, float[] pixels) throws IOException {
    long bytesWritten = 0L;
    final long size = 4L * fi.width * fi.height;
    int count = 8192;
    final byte[] buffer = new byte[count];
    int tmp;

    while (bytesWritten < size) {
      if ((bytesWritten + count) > size) {
        count = (int) (size - bytesWritten);
      }
      int index = (int) (bytesWritten / 4L);
      if (fi.intelByteOrder) {
        for (int i = 0; i < count; i += 4) {
          tmp = Float.floatToRawIntBits(pixels[index]);
          buffer[i] = (byte) tmp;
          buffer[i + 1] = (byte) (tmp >> 8);
          buffer[i + 2] = (byte) (tmp >> 16);
          buffer[i + 3] = (byte) (tmp >> 24);
          index++;
        }
      } else {
        for (int i = 0; i < count; i += 4) {
          tmp = Float.floatToRawIntBits(pixels[index]);
          buffer[i] = (byte) (tmp >> 24);
          buffer[i + 1] = (byte) (tmp >> 16);
          buffer[i + 2] = (byte) (tmp >> 8);
          buffer[i + 3] = (byte) tmp;
          index++;
        }
      }
      out.write(buffer, 0, count);
      bytesWritten += count;
    }
  }

  private void writeFloatStack(OutputStream out, Object[] stack) throws IOException {
    for (int i = 0; i < fi.nImages; i++) {
      writeFloatImage(out, (float[]) stack[i]);
    }
  }

  private void writeFloatVirtualStack(OutputStream out, VirtualStack virtualStack)
      throws IOException {
    for (int i = 1; i <= fi.nImages; i++) {
      final ImageProcessor ip = virtualStack.getProcessor(i);
      final float[] pixels = (float[]) ip.getPixels();
      writeFloatImage(out, pixels);
    }
  }

  private void writeRgbImage(OutputStream out, int[] pixels) throws IOException {
    long bytesWritten = 0L;
    final long size = 3L * fi.width * fi.height;
    int count = fi.width * 24;
    final byte[] buffer = new byte[count];
    while (bytesWritten < size) {
      if ((bytesWritten + count) > size) {
        count = (int) (size - bytesWritten);
      }
      int index = (int) (bytesWritten / 3L);
      for (int i = 0; i < count; i += 3) {
        buffer[i] = (byte) (pixels[index] >> 16); // red
        buffer[i + 1] = (byte) (pixels[index] >> 8); // green
        buffer[i + 2] = (byte) pixels[index]; // blue
        index++;
      }
      out.write(buffer, 0, count);
      bytesWritten += count;
    }
  }

  private void writeRgbStack(OutputStream out, Object[] stack) throws IOException {
    for (int i = 0; i < fi.nImages; i++) {
      writeRgbImage(out, (int[]) stack[i]);
    }
  }

  private void writeRgbVirtualStack(OutputStream out, VirtualStack virtualStack)
      throws IOException {
    for (int i = 1; i <= fi.nImages; i++) {
      final ImageProcessor ip = virtualStack.getProcessor(i);
      final int[] pixels = (int[]) ip.getPixels();
      writeRgbImage(out, pixels);
    }
  }

  /**
   * Writes the image to the specified OutputStream. The OutputStream is not closed.
   *
   * <p>The fi.pixels field must contain the image data. If fi.nImages &gt; 1 then fi.pixels must be
   * a 2D array, for example an array of images returned by ImageStack.getImageArray()).
   *
   * <p>The fi.offset field is ignored.
   *
   * @param out the output stream
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public void write(OutputStream out) throws IOException {
    if (fi.pixels == null && fi.virtualStack == null) {
      throw new IOException("CustomImageWriter: fi.pixels==null");
    }
    if (isStack() && fi.virtualStack == null && !(fi.pixels instanceof Object[])) {
      throw new IOException("CustomImageWriter: fi.pixels not a stack");
    }
    switch (fi.fileType) {
      case FileInfo.GRAY8:
      case FileInfo.COLOR8:
        if (isVirtualStack()) {
          write8BitVirtualStack(out, fi.virtualStack);
        } else if (isStack()) {
          write8BitStack(out, (Object[]) fi.pixels);
        } else {
          write8BitImage(out, (byte[]) fi.pixels);
        }
        break;
      case FileInfo.GRAY16_SIGNED:
      case FileInfo.GRAY16_UNSIGNED:
        if (isVirtualStack()) {
          write16BitVirtualStack(out, fi.virtualStack);
        } else if (isStack()) {
          write16BitStack(out, (Object[]) fi.pixels);
        } else {
          write16BitImage(out, (short[]) fi.pixels);
        }
        break;
      case FileInfo.RGB48:
        writeRgb48Image(out, (Object[]) fi.pixels);
        break;
      case FileInfo.GRAY32_FLOAT:
        if (isVirtualStack()) {
          writeFloatVirtualStack(out, fi.virtualStack);
        } else if (isStack()) {
          writeFloatStack(out, (Object[]) fi.pixels);
        } else {
          writeFloatImage(out, (float[]) fi.pixels);
        }
        break;
      case FileInfo.RGB:
        if (isVirtualStack()) {
          writeRgbVirtualStack(out, fi.virtualStack);
        } else if (isStack()) {
          writeRgbStack(out, (Object[]) fi.pixels);
        } else {
          writeRgbImage(out, (int[]) fi.pixels);
        }
        break;
      default:
        throw new IOException("CustomImageWriter: unknown file type " + fi.fileType);
    }
  }

  private boolean isVirtualStack() {
    return isStack() && fi.virtualStack != null;
  }

  private boolean isStack() {
    return fi.nImages > 1;
  }
}
