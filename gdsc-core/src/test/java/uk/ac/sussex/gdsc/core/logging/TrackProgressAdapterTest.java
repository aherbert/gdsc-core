/*-
 * #%L
 * Genome Damage and Stability Centre Core Package
 *
 * Contains core utilities for image analysis and is used by:
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

package uk.ac.sussex.gdsc.core.logging;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"javadoc"})
class TrackProgressAdapterTest {

  @Test
  void testAdaptor() {
    final TrackProgressAdapter progress = new TrackProgressAdapter() {};

    // Exercise the methods for coverage
    progress.progress(0.5);
    progress.progress(1L, 10);
    progress.incrementProgress(0.1);
    progress.log("hello %", "world");
    progress.status("hello %", "world");

    Assertions.assertFalse(progress.isEnded());
    Assertions.assertTrue(progress.isProgress());
    Assertions.assertTrue(progress.isLog());
    Assertions.assertTrue(progress.isStatus());
  }
}
