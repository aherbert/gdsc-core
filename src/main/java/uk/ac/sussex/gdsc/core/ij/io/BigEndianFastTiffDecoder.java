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
 * Copyright (C) 2011 - 2020 Alex Herbert
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
 * A big-endian {@link FastTiffDecoder}.
 */
class BigEndianFastTiffDecoder extends FastTiffDecoder {

  /**
   * Instantiates a new big endian fast tiff decoder.
   *
   * @param in the in
   * @param name the name
   */
  protected BigEndianFastTiffDecoder(SeekableStream in, String name) {
    super(in, name);
  }

  @Override
  public boolean isLittleEndian() {
    return false;
  }

  @Override
  protected int getInt(int b1, int b2, int b3, int b4) {
    return ((b1 << 24) | (b2 << 16) | (b3 << 8) | b4);
  }

  @Override
  protected int getShort(int b1, int b2) {
    return ((b1 << 8) | b2);
  }

  @Override
  protected long readLong() throws IOException {
    return ((long) readInt() << 32) | (readInt() & 0xffffffffL);
  }
}
