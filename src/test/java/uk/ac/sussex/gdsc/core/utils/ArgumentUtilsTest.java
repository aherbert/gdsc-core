package uk.ac.sussex.gdsc.core.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"javadoc"})
public class ArgumentUtilsTest {
  @Test
  public void canCheckCondition() {
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      ArgumentUtils.checkCondition(false);
    });
  }

  @Test
  public void canCheckConditionWithObjectMessage() {
    final String message = "failure message";
    final IllegalArgumentException ex =
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
          ArgumentUtils.checkCondition(false, message);
        });
    Assertions.assertEquals(message, ex.getMessage(), "Does not format the message");
  }

  @Test
  public void canCheckConditionWithFormattedMessage() {
    final String format = "failure %d message";
    final Object[] args = new Object[] {44};
    final IllegalArgumentException ex =
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
          ArgumentUtils.checkCondition(false, format, args);
        });
    final String message = String.format(format, args);
    Assertions.assertEquals(message, ex.getMessage(), "Does not format the message");
  }

  @Test
  public void canCheckState() {
    Assertions.assertThrows(IllegalStateException.class, () -> {
      ArgumentUtils.checkState(false);
    });
  }

  @Test
  public void canCheckStateWithObjectMessage() {
    final String message = "failure message";
    final IllegalStateException ex = Assertions.assertThrows(IllegalStateException.class, () -> {
      ArgumentUtils.checkState(false, message);
    });
    Assertions.assertEquals(message, ex.getMessage(), "Does not format the message");
  }

  @Test
  public void canCheckStateWithFormattedMessage() {
    final String format = "failure %d message";
    final Object[] args = new Object[] {44};
    final IllegalStateException ex = Assertions.assertThrows(IllegalStateException.class, () -> {
      ArgumentUtils.checkState(false, format, args);
    });
    final String message = String.format(format, args);
    Assertions.assertEquals(message, ex.getMessage(), "Does not format the message");
  }

  @Test
  public void canCheckNotNull() {
    final Object object = new Object();
    Assertions.assertSame(object, ArgumentUtils.checkNotNull(object),
        "Does not return the same object");
    Assertions.assertThrows(NullPointerException.class, () -> {
      ArgumentUtils.checkNotNull(null);
    });
  }

  @Test
  public void canCheckNotNullWithObjectMessage() {
    final String message = "failure message";
    final Object object = new Object();
    Assertions.assertSame(object, ArgumentUtils.checkNotNull(object, message),
        "Does not return the same object");
    final NullPointerException ex = Assertions.assertThrows(NullPointerException.class, () -> {
      ArgumentUtils.checkNotNull(null, message);
    });
    Assertions.assertEquals(message, ex.getMessage(), "Does not format the message");
  }

  @Test
  public void canCheckNotNullWithFormattedMessage() {
    final String format = "failure %d message";
    final Object[] args = new Object[] {44};
    final Object object = new Object();
    Assertions.assertSame(object, ArgumentUtils.checkNotNull(object, format, args),
        "Does not return the same object");
    final NullPointerException ex = Assertions.assertThrows(NullPointerException.class, () -> {
      ArgumentUtils.checkNotNull(null, format, args);
    });
    final String message = String.format(format, args);
    Assertions.assertEquals(message, ex.getMessage(), "Does not format the message");
  }
}
