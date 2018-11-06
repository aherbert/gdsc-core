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

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;

/**
 * Contains utility functions for using {@code java.util.logging} within ImageJ.
 */
public final class ImageJLoggingUtils {

  /**
   * No public construction.
   */
  private ImageJLoggingUtils() {}

  /**
   * Remove all instances of {@link ConsoleHandler} and replace with {@link ImageJLogHandler}.
   *
   * <p>This is a helper method to allow code to send logging output to the ImageJ log window. It
   * can be called by plugins to ensure the packages that are used will send output to the ImageJ
   * log.
   *
   * @param packageName the package name
   */
  public static void redirectConsoleHandler(String packageName) {
    redirectConsoleHandler(packageName, new ImageJLogHandler());
  }

  /**
   * Remove all instances of {@link ConsoleHandler} or {@link ImageJLogHandler} and replace with the
   * provided {@link ImageJLogHandler}.
   *
   * <p>This is a helper method to allow code to send logging output to the ImageJ log window. It
   * can be called by plugins to ensure the packages that are used will send output to the ImageJ
   * log.
   *
   * @param packageName the package name
   * @param imageJLogHandler the ImageJ log handler
   */
  public static void redirectConsoleHandler(String packageName, ImageJLogHandler imageJLogHandler) {
    Logger logger = Logger.getLogger(packageName);
    boolean removed = false;
    for (Handler handler : logger.getHandlers()) {
      if (handler.getClass().equals(ConsoleHandler.class) || handler instanceof ImageJLogHandler) {
        removed = true;
        logger.removeHandler(handler);
      }
    }
    if (removed) {
      logger.addHandler(imageJLogHandler);
    }
  }
}
