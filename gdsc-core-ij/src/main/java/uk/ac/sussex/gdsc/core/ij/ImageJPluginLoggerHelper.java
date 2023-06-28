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

import java.util.logging.ConsoleHandler;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Contains helper functions for plugins that use {@link java.util.logging.Logger}.
 *
 * <p>When created this class will redirect all loggers below the package {@code uk.ac.sussex.gdsc}
 * using {@link ConsoleHandler} to use {@link ImageJLogHandler}.
 *
 * <p>This is a helper that allows top-level GDSC plugins to create loggers that will appropriately
 * redirect output to ImageJ.
 *
 * @see ImageJLoggingUtils#redirectConsoleHandler(String, ImageJLogHandler)
 */
public final class ImageJPluginLoggerHelper {

  /**
   * A reference to the redirected logger for the GDSC package.
   *
   * <p>This is not used but a reference is held to allow the configuration to be maintained since
   * the LogManager only holds a weak reference to the configured logger.
   */
  private static final Logger logger;

  static {
    // Ensure redirection of the top-level GDSC package
    logger = ImageJLoggingUtils.redirectConsoleHandler("uk.ac.sussex.gdsc");
  }

  /**
   * No public construction.
   */
  private ImageJPluginLoggerHelper() {}

  /**
   * Gets the logger for the named subsystem.
   *
   * <p>Note: The initialisation for this helper class redirects all loggers below the package
   * {@code uk.ac.sussex.gdsc} using {@link ConsoleHandler} to use {@link ImageJLogHandler}. This
   * occurs only once and logging can be further configured using the {@link LogManager} or
   * {@link ImageJLoggingUtils}.
   *
   * @param name the name
   * @return the logger
   * @see Logger#getLogger(String)
   */
  public static Logger getLogger(String name) {
    return Logger.getLogger(name);
  }

  /**
   * Gets the logger using the class name.
   *
   * <p>Note: The initialisation for this helper class redirects all loggers below the package
   * {@code uk.ac.sussex.gdsc} using {@link ConsoleHandler} to use {@link ImageJLogHandler}. This
   * occurs only once and logging can be further configured using the {@link LogManager} or
   * {@link ImageJLoggingUtils}.
   *
   * @param clazz the class
   * @return the logger
   * @see Class#getName()
   * @see Logger#getLogger(String)
   */
  public static Logger getLogger(Class<?> clazz) {
    return Logger.getLogger(clazz.getName());
  }

  /**
   * Gets the default logger for the {@code uk.ac.sussex.gdsc} package.
   *
   * @return the logger
   */
  public static Logger getDefaultLogger() {
    return logger;
  }
}
