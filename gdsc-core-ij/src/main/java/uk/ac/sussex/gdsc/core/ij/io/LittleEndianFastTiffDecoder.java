/*-
 * #%L
 * Genome Damage and Stability Centre Core ImageJ Package
 *
 * Contains core utilities for image analysis in ImageJ and is used by:
 *
 * GDSC ImageJ Plugins - Microscopy image analysis
 *
 * GDSC SMLM ImageJ Plugins - Single molecule localisation microscopy (SMLM)
 * %%
 * Copyright (C) 2011 - 2025 Alex Herbert
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

import java.io.IOException;

/**
 * A little-endian {@link FastTiffDecoder}.
 */
class LittleEndianFastTiffDecoder extends FastTiffDecoder {

  /**
   * Instantiates a new little endian fast tiff decoder.
   *
   * @param in the in
   * @param name the name
   */
  protected LittleEndianFastTiffDecoder(SeekableStream in, String name) {
    super(in, name);
  }

  @Override
  public boolean isLittleEndian() {
    return true;
  }

  @Override
  protected int getInt(int b1, int b2, int b3, int b4) {
    return (b4 << 24) | (b3 << 16) | (b2 << 8) | b1;
  }

  @Override
  protected int getShort(int b1, int b2) {
    return ((b2 << 8) | b1);
  }

  @Override
  protected long readLong() throws IOException {
    return (readInt() & 0xffffffffL) + ((long) readInt() << 32);
  }
}
