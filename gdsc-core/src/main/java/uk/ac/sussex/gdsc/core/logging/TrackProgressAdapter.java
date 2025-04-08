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

/**
 * An abstract adapter class for tracking progress. The methods in this class are empty. This class
 * exists as a convenience for creating tracking objects.
 *
 * <p>Extend this class to create a {@code TrackProgress} and override the methods for the progress
 * events of interest. (If you implement the {@code TrackProgress} interface, you have to define all
 * of the methods in it. This abstract class defines null methods for them all, so you only have to
 * define methods for progress events you care about. Note: The {@link TrackProgress#isEnded()}
 * returns {@code false} and the other properties return {@code true}).
 */
public abstract class TrackProgressAdapter implements TrackProgress {
  @Override
  public void progress(double fraction) {
    // Do nothing
  }

  @Override
  public void progress(long position, long total) {
    // Do nothing
  }

  @Override
  public void incrementProgress(double fraction) {
    // Do nothing
  }

  @Override
  public void log(String format, Object... args) {
    // Do nothing
  }

  @Override
  public void status(String format, Object... args) {
    // Do nothing
  }

  @Override
  public boolean isEnded() {
    // Never end
    return false;
  }

  @Override
  public boolean isProgress() {
    // Assume active
    return true;
  }

  @Override
  public boolean isLog() {
    // Assume active
    return true;
  }

  @Override
  public boolean isStatus() {
    // Assume active
    return true;
  }
}
