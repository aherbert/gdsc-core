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
 * Copyright (C) 2011 - 2018 Alex Herbert
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

import uk.ac.sussex.gdsc.core.logging.TrackProgress;

import ij.IJ;

/**
 * Report the progress of processing results.
 */
public class ImageJTrackProgress implements TrackProgress {
  /**
   * The current progress (used in {@link #incrementProgress(double)}).
   */
  private double done;

  /** The slow mode flag. */
  private final boolean slowMode;

  /** The progress flag. */
  private boolean progress = true;

  /** The log flag. */
  private boolean log = true;

  /** The status flag. */
  private boolean status = true;

  /**
   * Instantiates a new image J track progress.
   */
  public ImageJTrackProgress() {
    this(false);
  }

  /**
   * Instantiates a new image J track progress.
   *
   * <p>If using the slow progress functionality the progress will be reported as negative.
   *
   * @param slowMode Set to true to use the slow progress functionality of ImageJ
   */
  public ImageJTrackProgress(boolean slowMode) {
    this.slowMode = slowMode;
  }

  @Override
  public void progress(double fraction) {
    if (fraction == 0) {
      done = 0;
    }
    if (isProgress()) {
      if (slowMode) {
        IJ.showProgress(-fraction);
      } else {
        IJ.showProgress(fraction);
      }
    }
  }

  @Override
  public void progress(long position, long total) {
    if (position == 0) {
      done = 0;
    }
    if (isProgress()) {
      if (slowMode) {
        ImageJUtils.showSlowProgress((int) position, (int) total);
      } else {
        IJ.showProgress((double) position / total);
      }
    }
  }

  /**
   * This is not thread safe. The total work done is accumulated and can be reset by passing zero
   * progress to the progress methods.
   *
   * @see uk.ac.sussex.gdsc.core.logging.TrackProgress#incrementProgress(double)
   */
  @Override
  public void incrementProgress(double fraction) {
    if (slowMode) {
      done -= fraction;
    } else {
      done += fraction;
    }
    if (isProgress()) {
      IJ.showProgress(done);
    }
  }

  @Override
  public void log(String format, Object... args) {
    if (isLog()) {
      IJ.log(String.format(format, args));
    }
  }

  @Override
  public void status(String format, Object... args) {
    if (isStatus()) {
      IJ.showStatus(String.format(format, args));
    }
  }

  @Override
  public boolean isEnded() {
    return ImageJUtils.isInterrupted();
  }

  @Override
  public boolean isProgress() {
    return progress;
  }

  /**
   * Sets if the progress methods are active.
   *
   * @param progress the new progress
   */
  public void setProgress(boolean progress) {
    this.progress = progress;
  }

  @Override
  public boolean isLog() {
    return log;
  }

  /**
   * Sets if the log methods are active.
   *
   * @param log True for active
   */
  public void setLog(boolean log) {
    this.log = log;
  }

  @Override
  public boolean isStatus() {
    return status;
  }

  /**
   * Sets if the status methods are active.
   *
   * @param status the new status
   */
  public void setStatus(boolean status) {
    this.status = status;
  }
}
