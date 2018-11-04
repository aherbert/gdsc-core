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

import java.util.logging.Level;

/**
 * Utilities for logging.
 */
public final class LoggingUtils {

  /**
   * Extend java.util.logging.Logger and make isLoggable always return false and setLogLevel be
   * ignored. Then use this when the logger is null.
   */
  private static class NoLogger extends java.util.logging.Logger {
    private static final NoLogger INSTANCE = new NoLogger();

    public NoLogger() {
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
  private LoggingUtils() {}

  /**
   * Creates an instance if the argument is null, else return the argument.
   * 
   * <p>The created instance will ignore logging requests.
   *
   * @param logger the logger (may be null)
   * @return the logger (not null)
   */
  public static java.util.logging.Logger createIfNull(java.util.logging.Logger logger) {
    return (logger != null) ? logger : NoLogger.INSTANCE;
  }
}
