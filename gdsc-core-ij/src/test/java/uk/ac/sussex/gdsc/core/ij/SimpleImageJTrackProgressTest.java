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
 * Copyright (C) 2011 - 2023 Alex Herbert
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

import ij.IJ;
import java.awt.event.KeyEvent;
import java.io.OutputStream;
import java.io.PrintStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.core.data.NotImplementedException;

@SuppressWarnings({"javadoc"})
class SimpleImageJTrackProgressTest {
  @Test
  void testTrackProgress() {
    final SimpleImageJTrackProgress instance = SimpleImageJTrackProgress.getInstance();
    instance.progress(0.5);
    instance.progress(3, 4);
    Assertions.assertThrows(NotImplementedException.class, () -> instance.incrementProgress(0.1));
    // Prevent System.out logging here
    final PrintStream orig = System.out;
    try (PrintStream ps = new PrintStream(new OutputStream() {
      @Override
      public void write(int b) {
        // Ignore the data
      }
    })) {
      System.setOut(ps);
      instance.log("%s count %d", SimpleImageJTrackProgressTest.class.getSimpleName(), 1);
    } finally {
      System.setOut(orig);
    }
    instance.status("%s count %d", SimpleImageJTrackProgressTest.class.getSimpleName(), 2);
    Assertions.assertTrue(instance.isProgress());
    Assertions.assertTrue(instance.isLog());
    Assertions.assertTrue(instance.isStatus());
    Assertions.assertFalse(instance.isEnded());
    IJ.resetEscape();
    Assertions.assertFalse(instance.isEnded());
    IJ.escapePressed();
    IJ.setKeyDown(KeyEvent.VK_ESCAPE);
    Assertions.assertTrue(instance.isEnded());
    IJ.resetEscape();
  }
}
