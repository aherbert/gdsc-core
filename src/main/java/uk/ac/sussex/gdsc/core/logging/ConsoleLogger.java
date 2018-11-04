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

import java.io.PrintStream;
import java.util.Objects;

/**
 * Logs messages to an output {@link PrintStream}, defaulting to {@link System#out}.
 */
public class ConsoleLogger implements Logger {
  /** The new line format string. */
  private static final String NEW_LINE = "%n";

  /** The output destination. */
  private final PrintStream out;

  /**
   * Instantiates a new console logger using {@link System#out}.
   */
  public ConsoleLogger() {
    this(System.out);
  }

  /**
   * Instantiates a new console logger.
   *
   * @param out the output destination
   */
  public ConsoleLogger(PrintStream out) {
    this.out = Objects.requireNonNull(out, "Output must not be null");
  }

  /**
   * Log the message to the output. Appends a new line to the message.
   *
   * @param message the message
   */
  @Override
  public void info(String message) {
    out.println(message);
  }

  /**
   * Log the arguments using the given format to the output. Appends a new line to the output if the
   * format is missing the newline format string "%n".
   *
   * @param format the format
   * @param args the args
   */
  @Override
  public void info(String format, Object... args) {
    out.printf(format, args);
    if (!format.endsWith(NEW_LINE)) {
      out.println();
    }
  }

  @Override
  public void debug(String message) {
    info(message);
  }

  @Override
  public void debug(String format, Object... args) {
    info(format, args);
  }

  @Override
  public void error(String message) {
    info(message);
  }

  @Override
  public void error(String format, Object... args) {
    info(format, args);
  }
}
