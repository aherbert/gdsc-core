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

package uk.ac.sussex.gdsc.core.ij;

import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.core.ij.process.MappedFloatProcessor;

@SuppressWarnings({"javadoc"})
class MappedImageStackTest {
  @Test
  void testDefaultConstructor() {
    final MappedImageStack stack = new MappedImageStack();
    Assertions.assertFalse(stack.isMapZero());
  }

  @Test
  void testImageStack8Bit() {
    final int width = 3;
    final int height = 4;
    final MappedImageStack stack = new MappedImageStack(width, height);
    stack.addSlice(new ByteProcessor(width, height));
    final ImageProcessor ip = stack.getProcessor(1);
    Assertions.assertTrue(ip instanceof ByteProcessor);
  }

  @Test
  void testImageStack32Bit() {
    final int width = 3;
    final int height = 4;
    final MappedImageStack stack = new MappedImageStack(width, height, 1);
    stack.setPixels(new float[width * height], 1);
    ImageProcessor ip = stack.getProcessor(1);
    Assertions.assertTrue(ip instanceof MappedFloatProcessor);
    MappedFloatProcessor fp = (MappedFloatProcessor) ip;
    Assertions.assertFalse(fp.isMapZero());

    stack.setMapZero(true);
    Assertions.assertTrue(stack.isMapZero());
    ip = stack.getProcessor(1);
    fp = (MappedFloatProcessor) ip;
    Assertions.assertTrue(fp.isMapZero());
  }
}
