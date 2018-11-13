package uk.ac.sussex.gdsc.core.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"javadoc"})
public class ValidationUtilsTest {

  @Test
  public void canGetDefaultIfNull() {
    final Object object = new Object();
    final Object defaultValue = new Object();
    Assertions.assertSame(object, ValidationUtils.defaultIfNull(object, defaultValue),
        "Does not return the same object");
    Assertions.assertSame(defaultValue, ValidationUtils.defaultIfNull(null, defaultValue),
        "Does not return the default object");
    Assertions.assertThrows(NullPointerException.class, () -> {
      ValidationUtils.defaultIfNull(null, null);
    });
  }
  
  @Test
  public void canCheckArgument() {
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      ValidationUtils.checkArgument(false);
    });
  }

  @Test
  public void canCheckArgumentWithObjectMessage() {
    final String message = "failure message";
    final IllegalArgumentException ex =
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
          ValidationUtils.checkArgument(false, message);
        });
    Assertions.assertEquals(message, ex.getMessage(), "Does not format the message");
  }

  @Test
  public void canCheckArgumentWithIntArgMessage() {
    final String format = "failure %d message";
    final int p1 = 44;
    final IllegalArgumentException ex =
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
          ValidationUtils.checkArgument(false, format, p1);
        });
    final String message = String.format(format, p1);
    Assertions.assertEquals(message, ex.getMessage(), "Does not format the message");
  }

  @Test
  public void canCheckArgumentWithObjectArgMessage() {
    final String format = "failure %s message";
    final String p1 = "one";
    final IllegalArgumentException ex =
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
          ValidationUtils.checkArgument(false, format, p1);
        });
    final String message = String.format(format, p1);
    Assertions.assertEquals(message, ex.getMessage(), "Does not format the message");
  }

  @Test
  public void canCheckArgumentWithIntIntArgMessage() {
    final String format = "failure %d message %d";
    final int p1 = 44;
    final int p2 = 123;
    final IllegalArgumentException ex =
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
          ValidationUtils.checkArgument(false, format, p1, p2);
        });
    final String message = String.format(format, p1, p2);
    Assertions.assertEquals(message, ex.getMessage(), "Does not format the message");
  }

  @Test
  public void canCheckArgumentWithObjectObjectArgMessage() {
    final String format = "failure %s message %s";
    final String p1 = "one";
    final String p2 = "two";
    final IllegalArgumentException ex =
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
          ValidationUtils.checkArgument(false, format, p1, p2);
        });
    final String message = String.format(format, p1, p2);
    Assertions.assertEquals(message, ex.getMessage(), "Does not format the message");
  }

  @Test
  public void canCheckArgumentWithVarArgsMessage() {
    final String format = "failure %d message";
    final Object[] args = new Object[] {44};
    final IllegalArgumentException ex =
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
          ValidationUtils.checkArgument(false, format, args);
        });
    final String message = String.format(format, args);
    Assertions.assertEquals(message, ex.getMessage(), "Does not format the message");
  }

  @Test
  public void canCheckState() {
    Assertions.assertThrows(IllegalStateException.class, () -> {
      ValidationUtils.checkState(false);
    });
  }

  @Test
  public void canCheckStateWithObjectMessage() {
    final String message = "failure message";
    final IllegalStateException ex = Assertions.assertThrows(IllegalStateException.class, () -> {
      ValidationUtils.checkState(false, message);
    });
    Assertions.assertEquals(message, ex.getMessage(), "Does not format the message");
  }

  @Test
  public void canCheckStateWithIntArgMessage() {
    final String format = "failure %d message";
    final int p1 = 44;
    final IllegalStateException ex = Assertions.assertThrows(IllegalStateException.class, () -> {
      ValidationUtils.checkState(false, format, p1);
    });
    final String message = String.format(format, p1);
    Assertions.assertEquals(message, ex.getMessage(), "Does not format the message");
  }

  @Test
  public void canCheckStateWithObjectArgMessage() {
    final String format = "failure %s message";
    final String p1 = "one";
    final IllegalStateException ex = Assertions.assertThrows(IllegalStateException.class, () -> {
      ValidationUtils.checkState(false, format, p1);
    });
    final String message = String.format(format, p1);
    Assertions.assertEquals(message, ex.getMessage(), "Does not format the message");
  }

  @Test
  public void canCheckStateWithIntIntArgMessage() {
    final String format = "failure %d message %d";
    final int p1 = 44;
    final int p2 = 123;
    final IllegalStateException ex = Assertions.assertThrows(IllegalStateException.class, () -> {
      ValidationUtils.checkState(false, format, p1, p2);
    });
    final String message = String.format(format, p1, p2);
    Assertions.assertEquals(message, ex.getMessage(), "Does not format the message");
  }

  @Test
  public void canCheckStateWithObjectObjectArgMessage() {
    final String format = "failure %s message %s";
    final String p1 = "one";
    final String p2 = "two";
    final IllegalStateException ex = Assertions.assertThrows(IllegalStateException.class, () -> {
      ValidationUtils.checkState(false, format, p1, p2);
    });
    final String message = String.format(format, p1, p2);
    Assertions.assertEquals(message, ex.getMessage(), "Does not format the message");
  }

  @Test
  public void canCheckStateWithVarArgsMessage() {
    final String format = "failure %d message";
    final Object[] args = new Object[] {44};
    final IllegalStateException ex = Assertions.assertThrows(IllegalStateException.class, () -> {
      ValidationUtils.checkState(false, format, args);
    });
    final String message = String.format(format, args);
    Assertions.assertEquals(message, ex.getMessage(), "Does not format the message");
  }

  @Test
  public void canCheckNotNull() {
    final Object object = new Object();
    Assertions.assertSame(object, ValidationUtils.checkNotNull(object),
        "Does not return the same object");
    Assertions.assertThrows(NullPointerException.class, () -> {
      ValidationUtils.checkNotNull(null);
    });
  }

  @Test
  public void canCheckNotNullWithObjectMessage() {
    final String message = "failure message";
    final Object object = new Object();
    Assertions.assertSame(object, ValidationUtils.checkNotNull(object, message),
        "Does not return the same object");
    final NullPointerException ex = Assertions.assertThrows(NullPointerException.class, () -> {
      ValidationUtils.checkNotNull(null, message);
    });
    Assertions.assertEquals(message, ex.getMessage(), "Does not format the message");
  }

  @Test
  public void canCheckNotNullWithIntArgMessage() {
    final String format = "failure %d message";
    final int p1 = 44;
    final NullPointerException ex = Assertions.assertThrows(NullPointerException.class, () -> {
      ValidationUtils.checkNotNull(null, format, p1);
    });
    final String message = String.format(format, p1);
    Assertions.assertEquals(message, ex.getMessage(), "Does not format the message");
  }

  @Test
  public void canCheckNotNullWithObjectArgMessage() {
    final String format = "failure %s message";
    final String p1 = "one";
    final NullPointerException ex = Assertions.assertThrows(NullPointerException.class, () -> {
      ValidationUtils.checkNotNull(null, format, p1);
    });
    final String message = String.format(format, p1);
    Assertions.assertEquals(message, ex.getMessage(), "Does not format the message");
  }

  @Test
  public void canCheckNotNullWithIntIntArgMessage() {
    final String format = "failure %d message %d";
    final int p1 = 44;
    final int p2 = 123;
    final NullPointerException ex = Assertions.assertThrows(NullPointerException.class, () -> {
      ValidationUtils.checkNotNull(null, format, p1, p2);
    });
    final String message = String.format(format, p1, p2);
    Assertions.assertEquals(message, ex.getMessage(), "Does not format the message");
  }

  @Test
  public void canCheckNotNullWithObjectObjectArgMessage() {
    final String format = "failure %s message %s";
    final String p1 = "one";
    final String p2 = "two";
    final NullPointerException ex = Assertions.assertThrows(NullPointerException.class, () -> {
      ValidationUtils.checkNotNull(null, format, p1, p2);
    });
    final String message = String.format(format, p1, p2);
    Assertions.assertEquals(message, ex.getMessage(), "Does not format the message");
  }

  @Test
  public void canCheckNotNullWithVarArgsMessage() {
    final String format = "failure %d message";
    final Object[] args = new Object[] {44};
    final Object object = new Object();
    Assertions.assertSame(object, ValidationUtils.checkNotNull(object, format, args),
        "Does not return the same object");
    final NullPointerException ex = Assertions.assertThrows(NullPointerException.class, () -> {
      ValidationUtils.checkNotNull(null, format, args);
    });
    final String message = String.format(format, args);
    Assertions.assertEquals(message, ex.getMessage(), "Does not format the message");
  }
}
