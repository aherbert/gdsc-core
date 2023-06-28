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
import java.util.logging.ErrorManager;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import uk.ac.sussex.gdsc.core.logging.PlainMessageFormatter;

/**
 * Publish log records to the ImageJ log window.
 *
 * @see IJ#log(String)
 */
public class ImageJLogHandler extends Handler {

  /**
   * Instantiates a new ImageJ log handler using a {@link PlainMessageFormatter}.
   */
  public ImageJLogHandler() {
    this(new PlainMessageFormatter());
  }

  /**
   * Instantiates a new ImageJ log handler.
   *
   * @param formatter the formatter
   */
  public ImageJLogHandler(Formatter formatter) {
    setFormatter(formatter);
  }

  @Override
  public void publish(LogRecord record) {
    if (isLoggable(record)) {
      // We don't want to throw an exception here, but we
      // report the exception to any registered ErrorManager.
      String msg;
      try {
        msg = getFormatter().format(record);
      } catch (final Exception ex) {
        reportError(null, ex, ErrorManager.FORMAT_FAILURE);
        return;
      }
      try {
        IJ.log(msg);
      } catch (final Exception ex) {
        reportError(null, ex, ErrorManager.WRITE_FAILURE);
      }
    }
  }

  @Override
  public void flush() {
    // Do nothing
  }

  @Override
  public void close() {
    // Do nothing
  }
}
