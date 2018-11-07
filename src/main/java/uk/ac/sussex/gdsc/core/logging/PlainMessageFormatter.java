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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * Format a {@link LogRecord} using only the level and the message. These are output as:
 *
 * <pre>
 * level + ":" + message
 * </pre>
 *
 * <p>By default the level is not included if it is {@link Level#INFO}.
 *
 * <p>If the record contains a thrown exception then the stack trace is added to the message.
 *
 * <p>The formatted message is created using the log record parameters. No timestamp is added and no
 * use of the record resource bundle occurs.
 */
public class PlainMessageFormatter extends Formatter {

  /** The include info flag. */
  private boolean includeInfo;

  @Override
  public String format(LogRecord record) {
    if (record.getThrown() != null) {
      final StringWriter sw = new StringWriter();
      try (PrintWriter pw = new PrintWriter(sw)) {
        pw.println(getPlainMessage(record));
        record.getThrown().printStackTrace(pw);
        return sw.toString();
      }
    }
    return getPlainMessage(record);
  }

  private String getPlainMessage(LogRecord record) {
    if (record.getLevel() == Level.INFO && !isIncludeInfo()) {
      return formatLogRecord(record);
    }
    return record.getLevel() + ":" + formatLogRecord(record);
  }

  /**
   * Format the log record. Adapted from {@link Formatter#formatMessage(LogRecord)} but removed the
   * use of resource bundle.
   *
   * @param record the record
   * @return the string
   */
  private static String formatLogRecord(LogRecord record) {
    String format = record.getMessage();
    Object[] parameters = record.getParameters();
    if (parameters == null || parameters.length == 0) {
      // No parameters. Just return format string.
      return format;
    }

    // Is it a java.text style format?
    // Ideally we could match with
    // Pattern.compile("\\{\\d").matcher(format).find())
    // However the cost is 14% higher, so we cheaply check for
    // 1 of the first 4 parameters
    if (format.indexOf("{0") >= 0 || format.indexOf("{1") >= 0 || format.indexOf("{2") >= 0
        || format.indexOf("{3") >= 0) {
      // Do the formatting.
      try {
        return java.text.MessageFormat.format(format, parameters);
      } catch (Exception ex) {
        // Formatting failed. Fall through to plain format string
      }
    }
    return format;
  }

  /**
   * Checks if {@link Level#INFO} will be included in the message. By default this is {@code false}.
   *
   * @return true, if is including INFO
   */
  public boolean isIncludeInfo() {
    return includeInfo;
  }

  /**
   * Sets if {@link Level#INFO} will be included in the message.
   *
   * @param includeInfo the new include info flag
   */
  public void setIncludeInfo(boolean includeInfo) {
    this.includeInfo = includeInfo;
  }
}
