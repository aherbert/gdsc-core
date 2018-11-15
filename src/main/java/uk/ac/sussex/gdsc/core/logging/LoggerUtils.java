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

package uk.ac.sussex.gdsc.core.logging;

import uk.ac.sussex.gdsc.core.utils.ValidationUtils;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Utilities for working with {@link java.util.logging.Logger}.
 */
public final class LoggerUtils {

  /**
   * Extend Logger and make isLoggable always return false and setLogLevel be ignored. Then use this
   * when the logger is null.
   */
  private static class NoLogger extends Logger {
    private static final NoLogger INSTANCE = new NoLogger();

    NoLogger() {
      super(NoLogger.class.getName(), null);
    }

    @Override
    public boolean isLoggable(Level level) {
      // Nothing is logged
      return false;
    }

    @Override
    public void setLevel(Level newLevel) {
      // Ignore
    }

    @Override
    public Level getLevel() {
      return Level.OFF;
    }
  }

  /** No public construction. */
  private LoggerUtils() {}

  /**
   * Creates an instance if the argument is null, else return the argument.
   *
   * <p>The created instance will ignore logging requests.
   *
   * @param logger the logger (may be null)
   * @return the logger (not null)
   */
  public static Logger createIfNull(Logger logger) {
    return ValidationUtils.defaultIfNull(logger, NoLogger.INSTANCE);
  }

  /**
   * Gets an unconfigured logger. This is an anonymous logger which does not have any handlers.
   *
   * @return the unconfigured logger
   */
  public static Logger getUnconfiguredLogger() {
    final Logger logger = Logger.getAnonymousLogger();
    logger.setUseParentHandlers(false);
    return logger;
  }

  /**
   * Log the formatted message to the logger.
   *
   * @param logger the logger (ignored if null)
   * @param level the level
   * @param format the format
   * @param args the arguments
   * @see String#format(String, Object...)
   */
  public static void log(Logger logger, Level level, String format, Object... args) {
    if (logger == null || !logger.isLoggable(level)) {
      return;
    }
    logger.log(new LogRecord(level, String.format(format, args)));
  }
}
