package uk.ac.sussex.gdsc.core.logging;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.LogRecord;

@SuppressWarnings({"javadoc"})
public class PlainMessageFormatterTest {

  @Test
  public void canFormatPlainString() {
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
  public void canFormatPlainStringWithException() {
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
  public void canFormatParametersString() {
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
