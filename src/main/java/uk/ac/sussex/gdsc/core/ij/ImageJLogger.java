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

import uk.ac.sussex.gdsc.core.logging.Logger;

import ij.IJ;

/**
 * Log to the ImageJ log window.
 */
public class ImageJLogger implements Logger {
  /** Set to true to show debug log messages. */
  private boolean showDebug;
  /** Set to true to show error log messages. */
  private boolean showError = true;

  /**
   * Instantiates a new IJ logger.
   */
  public ImageJLogger() {}

  /**
   * Instantiates a new IJ logger.
   *
   * @param showDebug Set to true to show debug log messages
   * @param showError Set to true to show error log messages
   */
  public ImageJLogger(boolean showDebug, boolean showError) {
    this.setShowDebug(showDebug);
    this.setShowError(showError);
  }

  @Override
  public void info(String message) {
    IJ.log(message);
  }

  @Override
  public void info(String format, Object... args) {
    IJ.log(String.format(format, args));
  }

  @Override
  public void debug(String message) {
    if (isShowDebug()) {
      info(message);
    }
  }

  @Override
  public void debug(String format, Object... args) {
    if (isShowDebug()) {
      info(format, args);
    }
  }

  @Override
  public void error(String message) {
    if (isShowError()) {
      info(message);
    }
  }

  @Override
  public void error(String format, Object... args) {
    if (isShowError()) {
      info(format, args);
    }
  }

  /**
   * Checks if is showing debug log messages.
   *
   * @return true if is showing debug log messages
   */
  public boolean isShowDebug() {
    return showDebug;
  }

  /**
   * Set to true to show debug log messages.
   *
   * @param showDebug the new show debug flag
   */
  public void setShowDebug(boolean showDebug) {
    this.showDebug = showDebug;
  }

  /**
   * Checks if is showing error log messages.
   *
   * @return true if is showing error log messages
   */
  public boolean isShowError() {
    return showError;
  }

  /**
   * Set to true to show debug log messages.
   *
   * @param showError the new show error flag
   */
  public void setShowError(boolean showError) {
    this.showError = showError;
  }
}
