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
 * Copyright (C) 2011 - 2021 Alex Herbert
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

import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"javadoc"})
class PlainMessageFormatterTest {

  @Test
  void canFormatPlainString() {
    final String msg = "Simple message";
    final LogRecord lr = new LogRecord(Level.SEVERE, msg);
    final PlainMessageFormatter formatter = new PlainMessageFormatter();
    Assertions.assertFalse(formatter.isIncludeInfo(), "Default isIncludeInfo");
    Assertions.assertEquals(Level.SEVERE + ":" + msg, formatter.format(lr));

    // Ignore zero length parameters
    lr.setParameters(new Object[0]);
    Assertions.assertEquals(Level.SEVERE + ":" + msg, formatter.format(lr));

    // Special handling of INFO level
    lr.setLevel(Level.INFO);
    Assertions.assertEquals(msg, formatter.format(lr));
    formatter.setIncludeInfo(true);
    Assertions.assertTrue(formatter.isIncludeInfo(), "Updated isIncludeInfo");
    Assertions.assertEquals(Level.INFO + ":" + msg, formatter.format(lr));
  }

  @Test
  void canFormatPlainStringWithException() {
    final String msg = "Simple message";
    final LogRecord lr = new LogRecord(Level.INFO, msg);
    final Exception ex = new Exception("Something bad");
    lr.setThrown(ex);
    final PlainMessageFormatter formatter = new PlainMessageFormatter();
    final String text = formatter.format(lr);

    Assertions.assertTrue(text.startsWith(msg), "Text does not start with message");
    Assertions.assertTrue(text.contains(ex.getMessage()),
        "Text does not contain exception message");
    Assertions.assertTrue(text.contains(getClass().getName()),
        "Text does not contain exception source classname");
    for (final StackTraceElement element : ex.getStackTrace()) {
      Assertions.assertTrue(text.contains(element.toString()),
          () -> "Text does not contain exception stack trace element: " + element.toString());
    }
  }

  @Test
  void canFormatParametersString() {
    final PlainMessageFormatter formatter = new PlainMessageFormatter();
    final Object[] parameters = new Object[] {"Test", "the", "parameters", "string"};
    final LogRecord lr = new LogRecord(Level.INFO, "");
    lr.setParameters(parameters);
    for (int i = 0; i < 4; i++) {
      final String msg = "Param {" + i + "} message";
      lr.setMessage(msg);
      Assertions.assertEquals(MessageFormat.format(msg, parameters), formatter.format(lr));
    }

    // This is not formatted
    String msg = "Param {4} message";
    lr.setMessage(msg);
    Assertions.assertEquals(msg, formatter.format(lr));

    // Test an exception with a bad format string
    msg = "Param {0:fgfh} message";
    lr.setMessage(msg);
    Assertions.assertEquals(msg, formatter.format(lr));
  }
}
