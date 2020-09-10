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
 * Copyright (C) 2011 - 2020 Alex Herbert
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

import java.util.logging.ErrorManager;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"javadoc"})
class ImageJLogHandlerTest {
  @Test
  void testHandler() {
    final ImageJLogHandler handler = new ImageJLogHandler();
    final LogRecord record =
        new LogRecord(Level.WARNING, "test " + ImageJLogHandlerTest.class.getSimpleName());
    handler.setLevel(Level.SEVERE);
    handler.publish(record);
    handler.setLevel(Level.INFO);
    handler.publish(record);
    handler.flush();
    handler.close();
  }

  @Test
  void testPublishWithBadFormat() {
    final RuntimeException exception = new RuntimeException();
    final Formatter formatter = new Formatter() {
      @Override
      public String format(LogRecord record) {
        throw exception;
      }
    };
    final ImageJLogHandler handler = new ImageJLogHandler(formatter);
    final LogRecord record = new LogRecord(Level.WARNING, "test");
    handler.setLevel(Level.INFO);

    final boolean[] done = {false};
    handler.setErrorManager(new ErrorManager() {
      @Override
      public synchronized void error(String msg, Exception ex, int code) {
        Assertions.assertSame(exception, ex);
        done[0] = true;
      }
    });
    // Should be handled by the reportError function
    handler.publish(record);
    Assertions.assertTrue(done[0]);
  }
}
