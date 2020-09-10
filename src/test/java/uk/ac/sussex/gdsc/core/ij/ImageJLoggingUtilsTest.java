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

import java.util.Arrays;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.MemoryHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"javadoc"})
class ImageJLoggingUtilsTest {
  @Test
  void testRedirectConsoleHandler() {
    final String name = this.getClass().getSimpleName();
    final Logger logger = Logger.getLogger(name);
    logger.setUseParentHandlers(false);
    final ImageJLogHandler imageJLogHandler = new ImageJLogHandler();
    final ConsoleHandler consoleHandler = new ConsoleHandler();
    final Handler memoryHandler = new MemoryHandler(new ConsoleHandler(), 10, Level.WARNING);
    logger.addHandler(imageJLogHandler);
    logger.addHandler(consoleHandler);
    logger.addHandler(memoryHandler);
    List<Handler> handlers = Arrays.asList(logger.getHandlers());
    Assertions.assertTrue(handlers.contains(imageJLogHandler));
    Assertions.assertTrue(handlers.contains(consoleHandler));
    Assertions.assertTrue(handlers.contains(memoryHandler));
    ImageJLoggingUtils.redirectConsoleHandler(name);
    handlers = Arrays.asList(logger.getHandlers());
    Assertions.assertFalse(handlers.contains(imageJLogHandler));
    Assertions.assertFalse(handlers.contains(consoleHandler));
    Assertions.assertTrue(handlers.contains(memoryHandler));
    Assertions.assertFalse(logger.getUseParentHandlers());
    for (Handler h : logger.getHandlers()) {
      if (h instanceof ImageJLogHandler) {
        return;
      }
    }
    Assertions.fail("Should have an ImageJLogHandler");
  }

  @Test
  void testCollectHandlers() {
    final String name = this.getClass().getSimpleName();
    final Logger logger = Logger.getLogger(name);
    final boolean useParentHandlers = logger.getUseParentHandlers();
    try {
      logger.setUseParentHandlers(false);
      Assertions.assertEquals(Arrays.asList(logger.getHandlers()),
          ImageJLoggingUtils.collectHandlers(logger));
      logger.setUseParentHandlers(true);
      final List<Handler> handlers = ImageJLoggingUtils.collectHandlers(logger);
      for (final Handler h : logger.getHandlers()) {
        Assertions.assertTrue(handlers.contains(h));
      }
    } finally {
      logger.setUseParentHandlers(useParentHandlers);
    }
  }
}
