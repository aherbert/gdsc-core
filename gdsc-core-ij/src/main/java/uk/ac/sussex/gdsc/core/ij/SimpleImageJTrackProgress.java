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

package uk.ac.sussex.gdsc.core.ij;

import ij.IJ;
import uk.ac.sussex.gdsc.core.data.NotImplementedException;
import uk.ac.sussex.gdsc.core.logging.TrackProgress;

/**
 * Report the progress of processing results.
 */
public final class SimpleImageJTrackProgress implements TrackProgress {

  /** The single instance. */
  private static final SimpleImageJTrackProgress INSTANCE = new SimpleImageJTrackProgress();

  /**
   * No public construction.
   */
  private SimpleImageJTrackProgress() {
    // Do nothing
  }

  /**
   * Gets the single instance of SimpleImageJTrackProgress.
   *
   * @return single instance of SimpleImageJTrackProgress
   */
  public static SimpleImageJTrackProgress getInstance() {
    return INSTANCE;
  }

  @Override
  public void progress(double fraction) {
    IJ.showProgress(fraction);
  }

  @Override
  public void progress(long position, long total) {
    IJ.showProgress((double) position / total);
  }

  /**
   * This is not implemented.
   *
   * @see uk.ac.sussex.gdsc.core.logging.TrackProgress#incrementProgress(double)
   * @throws NotImplementedException This method is not implemented
   */
  @Override
  public void incrementProgress(double fraction) {
    throw new NotImplementedException();
  }

  @Override
  public void log(String format, Object... args) {
    ImageJUtils.log(format, args);
  }

  @Override
  public void status(String format, Object... args) {
    ImageJUtils.showStatus(() -> String.format(format, args));
  }

  @Override
  public boolean isEnded() {
    return ImageJUtils.isInterrupted();
  }

  @Override
  public boolean isProgress() {
    return true;
  }

  @Override
  public boolean isLog() {
    return true;
  }

  @Override
  public boolean isStatus() {
    return true;
  }
}
