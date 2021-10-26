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
 * Copyright (C) 2011 - 2021 Alex Herbert
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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"javadoc"})
class CustomImageWriterTest {
  @Test
  void testBadPixelsThrows() {
    final FileInfo fi = new FileInfo();
    final ByteArrayOutputStream data = new ByteArrayOutputStream();
    final CustomImageWriter writer = new CustomImageWriter(fi);
    fi.pixels = null;
    fi.virtualStack = null;
    Assertions.assertThrows(IOException.class, () -> writer.write(data), "Null pixels");

    fi.pixels = new byte[1];
    fi.nImages = 2;
    Assertions.assertThrows(IOException.class, () -> writer.write(data), "Not a pixel array stack");
  }

  @Test
  void testBadFileTypeThrows() {
    final FileInfo fi = new FileInfo();
    final ByteArrayOutputStream data = new ByteArrayOutputStream();
    final CustomImageWriter writer = new CustomImageWriter(fi);
    fi.pixels = new byte[1];
    fi.fileType = FileInfo.RGB48_PLANAR;
    Assertions.assertThrows(IOException.class, () -> writer.write(data));
  }
}
