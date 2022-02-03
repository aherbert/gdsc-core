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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
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
   * Remove all instances of {@link ConsoleHandler} or {@link ImageJLogHandler} and replace with a
   * new {@link ImageJLogHandler}. This works on a named subsystem.
   *
   * <p>Note: If configuration is to be permanent then a strong reference to the returned logger
   * should be held as the newly created logger may be retained by the
   * {@link java.util.logging.LogManager LogManager} using a weak reference.
   *
   * @param name the name
   * @return the logger
   * @see ImageJLoggingUtils#redirectConsoleHandler(String, ImageJLogHandler)
   * @see Logger#getLogger(String)
   */
  public static Logger redirectConsoleHandler(String name) {
    return redirectConsoleHandler(name, new ImageJLogHandler());
  }

  /**
   * Remove all instances of {@link ConsoleHandler} or {@link ImageJLogHandler} and replace with the
   * provided {@link ImageJLogHandler}. This works on a named subsystem.
   *
   * <p>This is a helper method to allow code to send logging output to the ImageJ log window. It
   * can be called by ImageJ plugins to ensure the packages that are used will send output to the
   * ImageJ log.
   *
   * <p>The handlers for the logger and its parents will be collected. {@link ConsoleHandler} or
   * {@link ImageJLogHandler} will be replaced and the updated handlers added directly to this
   * logger. The logger will be set to not use its parent handlers.
   *
   * <p>Warning: This effectively detaches the named subsystem from the parent so use with caution.
   *
   * <p>Note: If configuration is to be permanent then a strong reference to the returned logger
   * should be held as the newly created logger may be retained by the
   * {@link java.util.logging.LogManager LogManager} using a weak reference.
   *
   * @param name the name
   * @param imageJLogHandler the ImageJ log handler
   * @return the logger
   */
  public static Logger redirectConsoleHandler(String name, ImageJLogHandler imageJLogHandler) {
    final Logger logger = Logger.getLogger(name);
    final List<Handler> handlers = collectHandlers(logger);
    // Remove specific instances
    final Iterator<Handler> iter = handlers.iterator();
    while (iter.hasNext()) {
      final Handler handler = iter.next();
      if (handler.getClass().equals(ConsoleHandler.class) || handler instanceof ImageJLogHandler) {
        iter.remove();
      }
    }
    // Clear current handlers from the logger
    for (final Handler handler : logger.getHandlers()) {
      logger.removeHandler(handler);
    }
    // Add the specific handler
    handlers.add(imageJLogHandler);
    for (final Handler handler : handlers) {
      logger.addHandler(handler);
    }
    logger.setUseParentHandlers(false);
    return logger;
  }

  /**
   * Collect all the handlers recursively up the tree if the logger allows use of its parent
   * handlers.
   *
   * @param logger the logger
   * @return the list
   */
  public static List<Handler> collectHandlers(Logger logger) {
    final ArrayList<Handler> handlers = new ArrayList<>();
    while (logger != null) {
      handlers.addAll(Arrays.asList(logger.getHandlers()));
      if (!logger.getUseParentHandlers()) {
        break;
      }
      logger = logger.getParent();
    }
    return handlers;
  }
}
