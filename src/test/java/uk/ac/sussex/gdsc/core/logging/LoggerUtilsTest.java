package uk.ac.sussex.gdsc.core.logging;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

@SuppressWarnings({"javadoc"})
public class LoggerUtilsTest {

  @Test
  public void canCreateIfNull() {
    final Logger logger = Logger.getAnonymousLogger();
    Assertions.assertSame(logger, LoggerUtils.createIfNull(logger), "Failed to return same logger");

    final Logger newLogger = LoggerUtils.createIfNull(null);
    Assertions.assertNotNull(newLogger, "Failed to create if null");
    for (final Level level : new Level[] {Level.OFF, Level.ALL, Level.SEVERE, Level.INFO}) {
      newLogger.setLevel(level);
      Assertions.assertFalse(newLogger.isLoggable(level), "Should not be loggable");
      Assertions.assertEquals(Level.OFF, newLogger.getLevel(),
          "Should ignore changes to the level");
    }
  }

  @Test
  public void canGetUnconfiguredLogger() {
    final Logger logger = LoggerUtils.getUnconfiguredLogger();
    final Handler[] handlers = logger.getHandlers();
    Assertions.assertEquals(0, handlers.length, "Should have no handlers");
    Assertions.assertFalse(logger.getUseParentHandlers(), "Should not log to parent");
    logger.log(Level.SEVERE, "This should be ignored");
  }

  @Test
  public void canLogFormattedMessage() {
    final Logger logger = LoggerUtils.getUnconfiguredLogger();
    final ArrayList<LogRecord> records = new ArrayList<>();
    final Handler handler = new Handler() {
      @Override
      public void publish(LogRecord record) {
        records.add(record);
      }

      @Override
      public void flush() {
        // Ignore
      }

      @Override
      public void close() throws SecurityException {
        // Ignore
      }
    };
    logger.addHandler(handler);

    final String format = "Test %d";
    final Object[] args = {1};
    final String expected = String.format(format, args);
    LoggerUtils.log(null, Level.INFO, format, args);

    Assertions.assertEquals(0, records.size(), "Nothing should be logged");

    logger.setLevel(Level.INFO);
    LoggerUtils.log(logger, Level.FINE, format, args);
    Assertions.assertEquals(0, records.size(), "FINE should not be logged");

    LoggerUtils.log(logger, Level.INFO, format, args);
    Assertions.assertEquals(1, records.size(), "INFO should be logged");

    Assertions.assertEquals(expected, records.get(0).getMessage(), "Message was not formatted");
  }
}
