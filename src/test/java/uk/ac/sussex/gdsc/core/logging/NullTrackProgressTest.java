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

package uk.ac.sussex.gdsc.core.logging;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"javadoc"})
class NullTrackProgressTest {

  @Test
  void canCreateIfNull() {
    final TrackProgress progress = new TrackProgress() {
      @Override
      public void progress(double fraction) {}

      @Override
      public void progress(long position, long total) {}

      @Override
      public void incrementProgress(double fraction) {}

      @Override
      public void log(String format, Object... args) {}

      @Override
      public void status(String format, Object... args) {}

      @Override
      public boolean isEnded() {
        return false;
      }

      @Override
      public boolean isProgress() {
        return false;
      }

      @Override
      public boolean isLog() {
        return false;
      }

      @Override
      public boolean isStatus() {
        return false;
      }
    };

    Assertions.assertSame(progress, NullTrackProgress.createIfNull(progress),
        "Failed to return same track progress");

    final TrackProgress newProgress = NullTrackProgress.createIfNull(null);
    Assertions.assertNotNull(newProgress, "Failed to create if null");
    Assertions.assertSame(NullTrackProgress.getInstance(), newProgress,
        "Failed to return the default instance if null");

    Assertions.assertFalse(newProgress.isEnded());
    Assertions.assertFalse(newProgress.isProgress());
    Assertions.assertFalse(newProgress.isLog());
    Assertions.assertFalse(newProgress.isStatus());

    // Exercise the methods for coverage
    newProgress.progress(0.5);
    newProgress.progress(1, 2);
    newProgress.incrementProgress(0.1);
    newProgress.status("ignored");
  }
}
