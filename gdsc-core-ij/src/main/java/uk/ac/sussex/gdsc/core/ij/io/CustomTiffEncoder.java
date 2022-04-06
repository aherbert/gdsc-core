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
import java.io.IOException;
import java.io.OutputStream;

/**
 * Saves an image described by a FileInfo object as an uncompressed, big/little-endian TIFF file.
 *
 * <p>This is a re-implementation of the {@link ij.io.TiffEncoder} to remove support for progress
 * tracking.
 *
 * <p>The encoder has been modified to respect the byte order of the FileInfo object. The ImageJ
 * TiffEncoder reads the IJ preferences and updates the input FileInfo object to match.
 */
public class CustomTiffEncoder {
  private static final int HDR_SIZE = 8;
  private static final int MAP_SIZE = 768; // in 16-bit words
  private static final int BPS_DATA_SIZE = 6;
  private static final int SCALE_DATA_SIZE = 16;

  private final FileInfo fi;
  private final int bitsPerSample;
  private final int photoInterp;
  private final int samplesPerPixel;
  private int entries;
  private final int ifdSize;
  private long imageOffset;
  private final int imageSize;
  private final long stackSize;
  private byte[] description;
  private int metaDataSize;
  private int metaDataTypes;
  private int metaDataEntries;
  private int sliceLabels;
  private int extraMetaDataEntries;
  private final int scaleSize;
  private final boolean littleEndian;
  private final byte[] buffer = new byte[8];
  private int colorMapSize;

  /**
   * Create a new instance.
   *
   * @param fi the file info
   */
  public CustomTiffEncoder(FileInfo fi) {
    this.fi = fi;
    // Respect the byte order of the FileInfo object
    littleEndian = fi.intelByteOrder;
    entries = 9;
    int bytesPerPixel = 1;
    int bpsSize = 0;

    int bps = 8;
    int spp = 1;
    int interp = 0;
    switch (fi.fileType) {
      case FileInfo.GRAY8:
        interp = fi.whiteIsZero ? 0 : 1;
        break;
      case FileInfo.GRAY16_UNSIGNED:
      case FileInfo.GRAY16_SIGNED:
        bps = 16;
        interp = fi.whiteIsZero ? 0 : 1;
        if (fi.lutSize > 0) {
          entries = 10;
          colorMapSize = MAP_SIZE * 2;
        }
        bytesPerPixel = 2;
        break;
      case FileInfo.GRAY32_FLOAT:
        bps = 32;
        interp = fi.whiteIsZero ? 0 : 1;
        if (fi.lutSize > 0) {
          entries = 10;
          colorMapSize = MAP_SIZE * 2;
        }
        bytesPerPixel = 4;
        break;
      case FileInfo.RGB:
        interp = 2;
        spp = 3;
        bytesPerPixel = 3;
        bpsSize = BPS_DATA_SIZE;
        break;
      case FileInfo.RGB48:
        bps = 16;
        interp = 2;
        spp = 3;
        bytesPerPixel = 6;
        fi.nImages /= 3;
        bpsSize = BPS_DATA_SIZE;
        break;
      case FileInfo.COLOR8:
        interp = 3;
        entries = 10;
        colorMapSize = MAP_SIZE * 2;
        break;
      default:
        // Leave at zero
    }
    bitsPerSample = bps;
    samplesPerPixel = spp;
    photoInterp = interp;

    if (fi.unit != null && fi.pixelWidth != 0 && fi.pixelHeight != 0) {
      entries += 3; // XResolution, YResolution and ResolutionUnit
    }
    if (fi.fileType == FileInfo.GRAY32_FLOAT) {
      entries++; // SampleFormat tag
    }
    makeDescriptionString();
    if (description != null) {
      entries++; // ImageDescription tag
    }
    final long size = (long) fi.width * fi.height * bytesPerPixel;
    imageSize = size <= 0xffffffffL ? (int) size : 0;
    stackSize = (long) imageSize * fi.nImages;
    metaDataSize = getMetaDataSize();
    if (metaDataSize > 0) {
      entries += 2; // MetaData & MetaDataCounts
    }
    ifdSize = 2 + entries * 12 + 4;
    final int descriptionSize = description != null ? description.length : 0;
    scaleSize = fi.unit != null && fi.pixelWidth != 0 && fi.pixelHeight != 0 ? SCALE_DATA_SIZE : 0;
    imageOffset = HDR_SIZE + ifdSize + bpsSize + descriptionSize + scaleSize + colorMapSize
        + metaDataEntries * 4 + metaDataSize;
    fi.offset = (int) imageOffset;
  }

  /**
   * Saves the image as a TIFF file. The OutputStream is not closed.
   *
   * <p>The fi.pixels field must contain the image data. If fi.nImages &gt; 1 then fi.pixels must be
   * a 2D array.
   *
   * <p>The fi.offset field is ignored.
   *
   * @param out the output stream
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public void write(OutputStream out) throws IOException {
    writeHeader(out);
    long nextIfd = 0L;
    if (fi.nImages > 1) {
      nextIfd = imageOffset + stackSize;
    }
    final boolean bigTiff = nextIfd + fi.nImages * ifdSize >= 0xffffffffL;
    if (bigTiff) {
      nextIfd = 0L;
    }
    writeIfd(out, (int) imageOffset, (int) nextIfd);
    if (fi.fileType == FileInfo.RGB || fi.fileType == FileInfo.RGB48) {
      writeBitsPerPixel(out);
    }
    if (description != null) {
      writeDescription(out);
    }
    if (scaleSize > 0) {
      writeScale(out);
    }
    if (colorMapSize > 0) {
      writeColorMap(out);
    }
    if (metaDataSize > 0) {
      writeMetaData(out);
    }
    new CustomImageWriter(fi).write(out);
    if (nextIfd > 0L) {
      int ifdSize2 = ifdSize;
      if (metaDataSize > 0) {
        metaDataSize = 0;
        entries -= 2;
        ifdSize2 -= 2 * 12;
      }
      for (int i = 2; i <= fi.nImages; i++) {
        if (i == fi.nImages) {
          nextIfd = 0;
        } else {
          nextIfd += ifdSize2;
        }
        imageOffset += imageSize;
        writeIfd(out, (int) imageOffset, (int) nextIfd);
      }
    }
  }

  private int getMetaDataSize() {
    sliceLabels = 0;
    metaDataEntries = 0;
    int size = 0;
    int types = 0;
    if (fi.info != null && fi.info.length() > 0) {
      metaDataEntries = 1;
      size = fi.info.length() * 2;
      types++;
    }
    if (fi.sliceLabels != null) {
      final int max = Math.min(fi.sliceLabels.length, fi.nImages);
      boolean isNonNullLabel = false;
      for (int i = 0; i < max; i++) {
        if (fi.sliceLabels[i] != null && fi.sliceLabels[i].length() > 0) {
          isNonNullLabel = true;
          break;
        }
      }
      if (isNonNullLabel) {
        for (int i = 0; i < max; i++) {
          sliceLabels++;
          if (fi.sliceLabels[i] != null) {
            size += fi.sliceLabels[i].length() * 2;
          }
        }
        if (sliceLabels > 0) {
          types++;
        }
        metaDataEntries += sliceLabels;
      }
    }

    if (fi.displayRanges != null) {
      metaDataEntries++;
      size += fi.displayRanges.length * 8;
      types++;
    }

    if (fi.channelLuts != null) {
      for (int i = 0; i < fi.channelLuts.length; i++) {
        if (fi.channelLuts[i] != null) {
          size += fi.channelLuts[i].length;
        }
      }
      types++;
      metaDataEntries += fi.channelLuts.length;
    }

    if (fi.plot != null) {
      metaDataEntries++;
      size += fi.plot.length;
      types++;
    }

    if (fi.roi != null) {
      metaDataEntries++;
      size += fi.roi.length;
      types++;
    }

    if (fi.overlay != null) {
      for (int i = 0; i < fi.overlay.length; i++) {
        if (fi.overlay[i] != null) {
          size += fi.overlay[i].length;
        }
      }
      types++;
      metaDataEntries += fi.overlay.length;
    }

    if (fi.metaDataTypes != null && fi.metaData != null && fi.metaData[0] != null
        && fi.metaDataTypes.length == fi.metaData.length) {
      extraMetaDataEntries = fi.metaData.length;
      types += extraMetaDataEntries;
      metaDataEntries += extraMetaDataEntries;
      for (int i = 0; i < extraMetaDataEntries; i++) {
        if (fi.metaData[i] != null) {
          size += fi.metaData[i].length;
        }
      }
    }
    if (metaDataEntries > 0) {
      metaDataEntries++; // add entry for header
    }
    final int hdrSize = 4 + types * 8;
    if (size > 0) {
      size += hdrSize;
    }
    metaDataTypes = types;
    return size;
  }

  /** Writes the 8-byte image file header. */
  private void writeHeader(OutputStream out) throws IOException {
    final byte[] hdr = new byte[8];
    if (littleEndian) {
      hdr[0] = 73; // "II" (Intel byte order)
      hdr[1] = 73;
      hdr[2] = 42; // 42 (magic number)
      hdr[4] = 8; // 8 (offset to first IFD)
    } else {
      hdr[0] = 77; // "MM" (Motorola byte order)
      hdr[1] = 77;
      hdr[3] = 42; // 42 (magic number)
      hdr[7] = 8; // 8 (offset to first IFD)
    }
    out.write(hdr);
  }

  /** Writes one 12-byte IFD entry. */
  private void writeEntry(OutputStream out, int tag, int fieldType, int count, int value)
      throws IOException {
    writeShort(out, tag);
    writeShort(out, fieldType);
    writeInt(out, count);
    if (count == 1 && fieldType == FastTiffDecoder.SHORT) {
      writeShort(out, value);
      writeShort(out, 0);
    } else {
      writeInt(out, value); // may be an offset
    }
  }

  /** Writes one IFD (Image File Directory). */
  private void writeIfd(OutputStream out, int imageOffset, int nextIfd) throws IOException {
    int tagDataOffset = HDR_SIZE + ifdSize;
    writeShort(out, entries);
    writeEntry(out, FastTiffDecoder.NEW_SUBFILE_TYPE, 4, 1, 0);
    writeEntry(out, FastTiffDecoder.IMAGE_WIDTH, 4, 1, fi.width);
    writeEntry(out, FastTiffDecoder.IMAGE_LENGTH, 4, 1, fi.height);
    if (fi.fileType == FileInfo.RGB || fi.fileType == FileInfo.RGB48) {
      writeEntry(out, FastTiffDecoder.BITS_PER_SAMPLE, 3, 3, tagDataOffset);
      tagDataOffset += BPS_DATA_SIZE;
    } else {
      writeEntry(out, FastTiffDecoder.BITS_PER_SAMPLE, 3, 1, bitsPerSample);
    }
    writeEntry(out, FastTiffDecoder.PHOTO_INTERP, 3, 1, photoInterp);
    if (description != null) {
      writeEntry(out, FastTiffDecoder.IMAGE_DESCRIPTION, 2, description.length, tagDataOffset);
      tagDataOffset += description.length;
    }
    writeEntry(out, FastTiffDecoder.STRIP_OFFSETS, 4, 1, imageOffset);
    writeEntry(out, FastTiffDecoder.SAMPLES_PER_PIXEL, 3, 1, samplesPerPixel);
    writeEntry(out, FastTiffDecoder.ROWS_PER_STRIP, 3, 1, fi.height);
    writeEntry(out, FastTiffDecoder.STRIP_BYTE_COUNT, 4, 1, imageSize);
    if (fi.unit != null && fi.pixelWidth != 0 && fi.pixelHeight != 0) {
      writeEntry(out, FastTiffDecoder.X_RESOLUTION, 5, 1, tagDataOffset);
      writeEntry(out, FastTiffDecoder.Y_RESOLUTION, 5, 1, tagDataOffset + 8);
      tagDataOffset += SCALE_DATA_SIZE;
      int unit = 1;
      if (fi.unit.equals("inch")) {
        unit = 2;
      } else if (fi.unit.equals("cm")) {
        unit = 3;
      }
      writeEntry(out, FastTiffDecoder.RESOLUTION_UNIT, 3, 1, unit);
    }
    if (fi.fileType == FileInfo.GRAY32_FLOAT) {
      final int format = FastTiffDecoder.FLOATING_POINT;
      writeEntry(out, FastTiffDecoder.SAMPLE_FORMAT, 3, 1, format);
    }
    if (colorMapSize > 0) {
      writeEntry(out, FastTiffDecoder.COLOR_MAP, 3, MAP_SIZE, tagDataOffset);
      tagDataOffset += MAP_SIZE * 2;
    }
    if (metaDataSize > 0) {
      writeEntry(out, FastTiffDecoder.META_DATA_BYTE_COUNTS, 4, metaDataEntries, tagDataOffset);
      writeEntry(out, FastTiffDecoder.META_DATA, 1, metaDataSize,
          tagDataOffset + 4 * metaDataEntries);
    }
    writeInt(out, nextIfd);
  }

  /** Writes the 6 bytes of data required by RGB BitsPerSample tag. */
  private void writeBitsPerPixel(OutputStream out) throws IOException {
    final int bitsPerPixel = fi.fileType == FileInfo.RGB48 ? 16 : 8;
    writeShort(out, bitsPerPixel);
    writeShort(out, bitsPerPixel);
    writeShort(out, bitsPerPixel);
  }

  /** Writes the 16 bytes of data required by the XResolution and YResolution tags. */
  private void writeScale(OutputStream out) throws IOException {
    final double xscale = 1.0 / fi.pixelWidth;
    final double yscale = 1.0 / fi.pixelHeight;
    double scale = 1000000.0;
    if (xscale * scale > Integer.MAX_VALUE || yscale * scale > Integer.MAX_VALUE) {
      scale = (int) (Integer.MAX_VALUE / Math.max(xscale, yscale));
    }
    writeInt(out, (int) (xscale * scale));
    writeInt(out, (int) scale);
    writeInt(out, (int) (yscale * scale));
    writeInt(out, (int) scale);
  }

  /** Writes the variable length ImageDescription string. */
  private void writeDescription(OutputStream out) throws IOException {
    out.write(description, 0, description.length);
  }

  /** Writes color palette following the image. */
  private void writeColorMap(OutputStream out) throws IOException {
    final byte[] colorTable16 = new byte[MAP_SIZE * 2];
    int index = littleEndian ? 1 : 0;
    for (int i = 0; i < fi.lutSize; i++) {
      colorTable16[index] = fi.reds[i];
      colorTable16[512 + index] = fi.greens[i];
      colorTable16[1024 + index] = fi.blues[i];
      index += 2;
    }
    out.write(colorTable16);
  }

  /**
   * Writes image metadata ("info" image property, stack slice labels, channel display ranges, luts,
   * ROIs, overlays and extra metadata).
   */
  private void writeMetaData(OutputStream out) throws IOException {

    // write byte counts (META_DATA_BYTE_COUNTS tag)
    writeInt(out, 4 + metaDataTypes * 8); // header size
    if (fi.info != null && fi.info.length() > 0) {
      writeInt(out, fi.info.length() * 2);
    }
    for (int i = 0; i < sliceLabels; i++) {
      if (fi.sliceLabels[i] == null) {
        writeInt(out, 0);
      } else {
        writeInt(out, fi.sliceLabels[i].length() * 2);
      }
    }
    if (fi.displayRanges != null) {
      writeInt(out, fi.displayRanges.length * 8);
    }
    if (fi.channelLuts != null) {
      for (int i = 0; i < fi.channelLuts.length; i++) {
        writeInt(out, fi.channelLuts[i].length);
      }
    }
    if (fi.plot != null) {
      writeInt(out, fi.plot.length);
    }
    if (fi.roi != null) {
      writeInt(out, fi.roi.length);
    }
    if (fi.overlay != null) {
      for (int i = 0; i < fi.overlay.length; i++) {
        writeInt(out, fi.overlay[i].length);
      }
    }
    for (int i = 0; i < extraMetaDataEntries; i++) {
      writeInt(out, fi.metaData[i].length);
    }

    // write header (META_DATA tag header)
    writeInt(out, FastTiffDecoder.MAGIC_NUMBER); // "IJIJ"
    if (fi.info != null) {
      writeInt(out, FastTiffDecoder.INFO); // type="info"
      writeInt(out, 1); // count
    }
    if (sliceLabels > 0) {
      writeInt(out, FastTiffDecoder.LABELS); // type="labl"
      writeInt(out, sliceLabels); // count
    }
    if (fi.displayRanges != null) {
      writeInt(out, FastTiffDecoder.RANGES); // type="rang"
      writeInt(out, 1); // count
    }
    if (fi.channelLuts != null) {
      writeInt(out, FastTiffDecoder.LUTS); // type="luts"
      writeInt(out, fi.channelLuts.length); // count
    }
    if (fi.plot != null) {
      writeInt(out, FastTiffDecoder.PLOT); // type="plot"
      writeInt(out, 1); // count
    }
    if (fi.roi != null) {
      writeInt(out, FastTiffDecoder.ROI); // type="roi "
      writeInt(out, 1); // count
    }
    if (fi.overlay != null) {
      writeInt(out, FastTiffDecoder.OVERLAY); // type="over"
      writeInt(out, fi.overlay.length); // count
    }
    for (int i = 0; i < extraMetaDataEntries; i++) {
      writeInt(out, fi.metaDataTypes[i]);
      writeInt(out, 1); // count
    }

    // write data (META_DATA tag body)
    if (fi.info != null) {
      writeChars(out, fi.info);
    }
    for (int i = 0; i < sliceLabels; i++) {
      if (fi.sliceLabels[i] != null) {
        writeChars(out, fi.sliceLabels[i]);
      }
    }
    if (fi.displayRanges != null) {
      for (int i = 0; i < fi.displayRanges.length; i++) {
        writeDouble(out, fi.displayRanges[i]);
      }
    }
    if (fi.channelLuts != null) {
      for (int i = 0; i < fi.channelLuts.length; i++) {
        out.write(fi.channelLuts[i]);
      }
    }
    if (fi.plot != null) {
      out.write(fi.plot);
    }
    if (fi.roi != null) {
      out.write(fi.roi);
    }
    if (fi.overlay != null) {
      for (int i = 0; i < fi.overlay.length; i++) {
        out.write(fi.overlay[i]);
      }
    }
    for (int i = 0; i < extraMetaDataEntries; i++) {
      out.write(fi.metaData[i]);
    }

  }

  /**
   * Creates an optional image description string for saving calibration data. For stacks, also
   * saves the stack size so ImageJ can open the stack without decoding an IFD for each slice.
   */
  private void makeDescriptionString() {
    if (fi.description != null) {
      if (fi.description.charAt(fi.description.length() - 1) != (char) 0) {
        fi.description += " ";
      }
      description = fi.description.getBytes();
      description[description.length - 1] = (byte) 0;
    } else {
      description = null;
    }
  }

  private void writeShort(OutputStream out, int value) throws IOException {
    if (littleEndian) {
      out.write(value & 255);
      out.write((value >>> 8) & 255);
    } else {
      out.write((value >>> 8) & 255);
      out.write(value & 255);
    }
  }

  private void writeInt(OutputStream out, int value) throws IOException {
    if (littleEndian) {
      out.write(value & 255);
      out.write((value >>> 8) & 255);
      out.write((value >>> 16) & 255);
      out.write((value >>> 24) & 255);
    } else {
      out.write((value >>> 24) & 255);
      out.write((value >>> 16) & 255);
      out.write((value >>> 8) & 255);
      out.write(value & 255);
    }
  }

  private void writeLong(OutputStream out, long value) throws IOException {
    if (littleEndian) {
      buffer[7] = (byte) (value >>> 56);
      buffer[6] = (byte) (value >>> 48);
      buffer[5] = (byte) (value >>> 40);
      buffer[4] = (byte) (value >>> 32);
      buffer[3] = (byte) (value >>> 24);
      buffer[2] = (byte) (value >>> 16);
      buffer[1] = (byte) (value >>> 8);
      buffer[0] = (byte) value;
      out.write(buffer, 0, 8);
    } else {
      buffer[0] = (byte) (value >>> 56);
      buffer[1] = (byte) (value >>> 48);
      buffer[2] = (byte) (value >>> 40);
      buffer[3] = (byte) (value >>> 32);
      buffer[4] = (byte) (value >>> 24);
      buffer[5] = (byte) (value >>> 16);
      buffer[6] = (byte) (value >>> 8);
      buffer[7] = (byte) value;
      out.write(buffer, 0, 8);
    }
  }

  private void writeDouble(OutputStream out, double value) throws IOException {
    writeLong(out, Double.doubleToLongBits(value));
  }

  private void writeChars(OutputStream out, String string) throws IOException {
    final int len = string.length();
    if (littleEndian) {
      for (int i = 0; i < len; i++) {
        final int v = string.charAt(i);
        out.write(v & 255);
        out.write((v >>> 8) & 255);
      }
    } else {
      for (int i = 0; i < len; i++) {
        final int v = string.charAt(i);
        out.write((v >>> 8) & 255);
        out.write(v & 255);
      }
    }
  }
}
