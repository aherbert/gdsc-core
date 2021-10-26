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

package uk.ac.sussex.gdsc.core.ij;

import ij.IJ;
import java.awt.event.KeyEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"javadoc"})
class ImageJTrackProgressTest {
  @Test
  void testProperties() {
    final ImageJTrackProgress progress = new ImageJTrackProgress();
    Assertions.assertTrue(progress.isLog());
    Assertions.assertTrue(progress.isStatus());
    Assertions.assertTrue(progress.isProgress());

    progress.setLogActive(false);
    Assertions.assertFalse(progress.isLog());
    progress.setLogActive(true);
    Assertions.assertTrue(progress.isLog());

    progress.setStatusActive(false);
    Assertions.assertFalse(progress.isStatus());
    progress.setStatusActive(true);
    Assertions.assertTrue(progress.isStatus());

    progress.setProgressActive(false);
    Assertions.assertFalse(progress.isProgress());
    progress.setProgressActive(true);
    Assertions.assertTrue(progress.isProgress());

    Assertions.assertFalse(progress.isEnded());
    try {
      IJ.setKeyDown(KeyEvent.VK_ESCAPE);
      Assertions.assertTrue(progress.isEnded());
    } finally {
      IJ.resetEscape();
    }
  }

  @Test
  void testProgressDouble() {
    for (final boolean slow : new boolean[] {true, false}) {
      final ImageJTrackProgress progress = new ImageJTrackProgress(slow);
      progress.progress(0);
      progress.progress(0.1);
      progress.progress(1.0);
      progress.setProgressActive(false);
      progress.progress(0.5);
    }
  }

  @Test
  void testProgressLong() {
    for (final boolean slow : new boolean[] {true, false}) {
      final ImageJTrackProgress progress = new ImageJTrackProgress(slow);
      progress.progress(0, 10);
      progress.progress(1, 10);
      progress.progress(10, 10);
      progress.setProgressActive(false);
      progress.progress(5, 10);
    }
  }

  @Test
  void testIncrementProgress() {
    for (final boolean slow : new boolean[] {true, false}) {
      final ImageJTrackProgress progress = new ImageJTrackProgress(slow);
      progress.incrementProgress(0.25);
      progress.incrementProgress(0.25);
      progress.incrementProgress(0.25);
      progress.setProgressActive(false);
      progress.incrementProgress(0.25);
    }
  }

  @Test
  void testStatus() {
    final ImageJTrackProgress progress = new ImageJTrackProgress();
    progress.status("hello %s", "world");
    progress.setStatusActive(false);
    progress.status("goodbye %s", "world");
  }

  @Test
  void testLog() {
    final ImageJTrackProgress progress = new ImageJTrackProgress();
    progress.log("hello %s", "world");
    progress.setLogActive(false);
    progress.log("goodbye %s", "world");
  }
}
