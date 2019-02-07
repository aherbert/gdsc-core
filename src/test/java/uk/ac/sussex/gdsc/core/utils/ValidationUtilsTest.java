package uk.ac.sussex.gdsc.core.utils;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings({"javadoc"})
public class ValidationUtilsTest {

  private static final String[] series = {"first", "second"};

  private static Logger logger;

  /** The level at which the generated methods are logged. */
  private Level level = Level.ALL;

  @BeforeAll
  public static void beforeAll() {
    logger = Logger.getLogger(ValidationUtilsTest.class.getName());
  }

  @AfterAll
  public static void afterAll() {
    logger = null;
  }

  /**
   * This is not a test but is inline generation of the class methods.
   */
  @Test
  public void canGenerateMethods() {
    String[] name = {"Argument", "State", "NotNull"};
    String[] ex = {"IllegalArgument", "IllegalState", "NullPointer"};
    boolean[] generics = {false, false, true};
    StringBuilder sb = new StringBuilder(1000);
    sb.append('\n');
    for (int i = 0; i < name.length; i++) {
      generateMethods(sb, generics[i], name[i], ex[i]);
    }
    logger.log(level, () -> sb.toString());
  }

  private static void generateMethods(StringBuilder sb, boolean generic, String methodSuffix,
      String exceptionPrefix) {
    String[] types = {"byte", "int", "long", "float", "double", "Object"};
    doGenerateMethods(sb, generic, methodSuffix, exceptionPrefix, false);
    doGenerateMethods(sb, generic, methodSuffix, exceptionPrefix, true);
    for (String type : types) {
      doGenerateMethods(sb, generic, methodSuffix, exceptionPrefix, true, type);
    }
    for (String type : types) {
      doGenerateMethods(sb, generic, methodSuffix, exceptionPrefix, true, type, type);
    }
  }

  private static void doGenerateMethods(StringBuilder sb, boolean generic, String methodSuffix,
      String exceptionPrefix, boolean args, String... params) {
    sb.append("/**\n");
    if (generic) {
      sb.append(" * Checks that the specified object reference is not {@code null}.\n");
    } else {
      sb.append(" * Check the {@code result} is {@code true}.\n");
    }
    sb.append(" *\n");
    if (args) {
      if (params.length > 0) {
        sb.append(" * <p>If not {@code true} the exception message is formed using\n");
        sb.append(" * {@link String#format(String, Object...)}.\n");
        sb.append(" *\n");
      } else if (params.length == 0) {
        sb.append(" * <p>If not {@code true} the exception message is formed using\n");
        sb.append(" * {@link String#valueOf(Object)}.\n");
        sb.append(" *\n");
      }
    }
    if (generic) {
      sb.append(" * @param <T> the type of the reference\n");
      sb.append(" * @param object the object reference to check for nullity\n");
    } else {
      sb.append(" * @param result the result\n");
    }
    if (args) {
      if (params.length > 0) {
        sb.append(" * @param format the format of the exception message\n");
      } else if (params.length == 0) {
        sb.append(" * @param message the object used to form the exception message\n");
      }
    }
    for (int i = 0; i < params.length; i++) {
      sb.append(" * @param p").append(i + 1).append(" the ").append(series[i])
          .append(" argument of the exception message\n");
    }
    if (generic) {
      sb.append(" * @return {@code object} if not {@code null}\n");
      sb.append(" * @throws ").append(exceptionPrefix)
          .append("Exception if {@code object} is {@code null}\n");
    } else {
      sb.append(" * @throws ").append(exceptionPrefix).append("Exception if not {@code true}\n");
    }
    sb.append(" */\n");
    if (generic) {
      sb.append("public static <T> T check").append(methodSuffix);
      sb.append("(T object");
    } else {
      sb.append("public static void check").append(methodSuffix);
      sb.append("(boolean result");
    }
    if (args) {
      if (params.length > 0) {
        sb.append(", String format");
      } else if (params.length == 0) {
        sb.append(", Object message");
      }
    }
    for (int i = 0; i < params.length; i++) {
      sb.append(", ").append(params[i]).append(" p").append(i + 1);
    }
    sb.append(") {\n");
    if (generic) {
      sb.append("  if (object == null) {\n");
    } else {
      sb.append("  if (!result) {\n");
    }
    sb.append("throw new ").append(exceptionPrefix).append("Exception(");
    if (args) {
      if (params.length > 0) {
        sb.append("String.format(format");
        for (int i = 0; i < params.length; i++) {
          sb.append(", p").append(i + 1);
        }
        sb.append(")");
      } else if (params.length == 0) {
        sb.append("String.valueOf(message)");
      }
    }
    sb.append(");\n");
    sb.append("  }\n");
    if (generic) {
      sb.append("  return object;\n");
    }
    sb.append("}\n");
  }

  /**
   * This is not a test but is inline generation of the test class methods.
   */
  @Test
  public void canGenerateTestMethods() {
    String[] name = {"Argument", "State", "NotNull"};
    String[] ex = {"IllegalArgument", "IllegalState", "NullPointer"};
    boolean[] generics = {false, false, true};
    StringBuilder sb = new StringBuilder(1000);
    sb.append('\n');
    for (int i = 0; i < name.length; i++) {
      generateTestMethods(sb, generics[i], name[i], ex[i]);
    }
    logger.log(level, () -> sb.toString());
  }

  private static void generateTestMethods(StringBuilder sb, boolean generic, String methodSuffix,
      String exceptionPrefix) {
    String[] types = {"byte", "int", "long", "float", "double", "Object"};
    doGenerateTestMethods(sb, generic, methodSuffix, exceptionPrefix, false);
    doGenerateTestMethods(sb, generic, methodSuffix, exceptionPrefix, true);
    for (String type : types) {
      doGenerateTestMethods(sb, generic, methodSuffix, exceptionPrefix, true, type);
    }
    for (String type : types) {
      doGenerateTestMethods(sb, generic, methodSuffix, exceptionPrefix, true, type, type);
    }
  }

  private static void doGenerateTestMethods(StringBuilder sb, boolean generic, String methodSuffix,
      String exceptionPrefix, boolean args, String... params) {
    sb.append("@Test\n");
    sb.append("public void canCheck").append(methodSuffix);
    if (args) {
      sb.append("With");
      for (int i = 0; i < params.length; i++) {
        sb.append(Character.toUpperCase(params[i].charAt(0))).append(params[i], 1,
            params[i].length());
      }
      sb.append("Message");
    }
    sb.append("() {\n");
    if (!args) {
      sb.append("ValidationUtils.check").append(methodSuffix).append('(')
          .append(generic ? "this" : "true").append(");\n");
      sb.append("Assertions.assertThrows(").append(exceptionPrefix)
          .append("Exception.class, () -> {\n");
      sb.append("  ValidationUtils.check").append(methodSuffix).append('(')
          .append(generic ? "null" : "false").append(");\n");
      sb.append("});\n");
      sb.append("}\n");
      return;
    }

    sb.append("final String message = \"failure message");
    for (int i = 0; i < params.length; i++) {
      sb.append(" %s");
    }
    sb.append("\";\n");
    for (int i = 0; i < params.length; i++) {
      sb.append("final ").append(params[i]).append(" p").append(i + 1).append(" = ").append(i + 1)
          .append(";\n");
    }

    // Check can pass
    sb.append('\n');
    if (generic) {
      sb.append("final Object anything = new Object();\nfinal Object result = ");
    }
    sb.append("ValidationUtils.check").append(methodSuffix).append('(')
        .append(generic ? "anything" : "true").append(", message");
    for (int i = 0; i < params.length; i++) {
      sb.append(", p").append(i + 1);
    }
    sb.append(");\n");
    if (generic) {
      sb.append("Assertions.assertSame(anything, result, \"Did not return the same object\");\n");
    }
    sb.append("\n");

    // Check failure
    sb.append("final ").append(exceptionPrefix).append("Exception ex =\n");
    sb.append("Assertions.assertThrows(").append(exceptionPrefix)
        .append("Exception.class, () -> {\n");
    sb.append("  ValidationUtils.check").append(methodSuffix).append('(')
        .append(generic ? "null" : "false").append(", message");
    for (int i = 0; i < params.length; i++) {
      sb.append(", p").append(i + 1);
    }
    sb.append(");\n");
    sb.append("});\n");
    sb.append("final String expected = String.");
    if (params.length == 0) {
      sb.append("valueOf(message");
    } else {
      sb.append("format(message");
      for (int i = 0; i < params.length; i++) {
        sb.append(", p").append(i + 1);
      }
    }
    sb.append(");\n");
    sb.append(
        "Assertions.assertEquals(expected, ex.getMessage(), \"Does not format the message\");\n");
    sb.append("}\n");
  }

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
    ValidationUtils.checkArgument(true);
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      ValidationUtils.checkArgument(false);
    });
  }

  @Test
  public void canCheckArgumentWithMessage() {
    final String message = "failure message";

    ValidationUtils.checkArgument(true, message);

    final IllegalArgumentException ex =
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
          ValidationUtils.checkArgument(false, message);
        });
    final String expected = String.valueOf(message);
    Assertions.assertEquals(expected, ex.getMessage(), "Does not format the message");
  }

  @Test
  public void canCheckArgumentWithByteMessage() {
    final String message = "failure message %s";
    final byte p1 = 1;

    ValidationUtils.checkArgument(true, message, p1);

    final IllegalArgumentException ex =
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
          ValidationUtils.checkArgument(false, message, p1);
        });
    final String expected = String.format(message, p1);
    Assertions.assertEquals(expected, ex.getMessage(), "Does not format the message");
  }

  @Test
  public void canCheckArgumentWithIntMessage() {
    final String message = "failure message %s";
    final int p1 = 1;

    ValidationUtils.checkArgument(true, message, p1);

    final IllegalArgumentException ex =
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
          ValidationUtils.checkArgument(false, message, p1);
        });
    final String expected = String.format(message, p1);
    Assertions.assertEquals(expected, ex.getMessage(), "Does not format the message");
  }

  @Test
  public void canCheckArgumentWithLongMessage() {
    final String message = "failure message %s";
    final long p1 = 1;

    ValidationUtils.checkArgument(true, message, p1);

    final IllegalArgumentException ex =
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
          ValidationUtils.checkArgument(false, message, p1);
        });
    final String expected = String.format(message, p1);
    Assertions.assertEquals(expected, ex.getMessage(), "Does not format the message");
  }

  @Test
  public void canCheckArgumentWithFloatMessage() {
    final String message = "failure message %s";
    final float p1 = 1;

    ValidationUtils.checkArgument(true, message, p1);

    final IllegalArgumentException ex =
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
          ValidationUtils.checkArgument(false, message, p1);
        });
    final String expected = String.format(message, p1);
    Assertions.assertEquals(expected, ex.getMessage(), "Does not format the message");
  }

  @Test
  public void canCheckArgumentWithDoubleMessage() {
    final String message = "failure message %s";
    final double p1 = 1;

    ValidationUtils.checkArgument(true, message, p1);

    final IllegalArgumentException ex =
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
          ValidationUtils.checkArgument(false, message, p1);
        });
    final String expected = String.format(message, p1);
    Assertions.assertEquals(expected, ex.getMessage(), "Does not format the message");
  }

  @Test
  public void canCheckArgumentWithObjectMessage() {
    final String message = "failure message %s";
    final Object p1 = 1;

    ValidationUtils.checkArgument(true, message, p1);

    final IllegalArgumentException ex =
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
          ValidationUtils.checkArgument(false, message, p1);
        });
    final String expected = String.format(message, p1);
    Assertions.assertEquals(expected, ex.getMessage(), "Does not format the message");
  }

  @Test
  public void canCheckArgumentWithByteByteMessage() {
    final String message = "failure message %s %s";
    final byte p1 = 1;
    final byte p2 = 2;

    ValidationUtils.checkArgument(true, message, p1, p2);

    final IllegalArgumentException ex =
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
          ValidationUtils.checkArgument(false, message, p1, p2);
        });
    final String expected = String.format(message, p1, p2);
    Assertions.assertEquals(expected, ex.getMessage(), "Does not format the message");
  }

  @Test
  public void canCheckArgumentWithIntIntMessage() {
    final String message = "failure message %s %s";
    final int p1 = 1;
    final int p2 = 2;

    ValidationUtils.checkArgument(true, message, p1, p2);

    final IllegalArgumentException ex =
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
          ValidationUtils.checkArgument(false, message, p1, p2);
        });
    final String expected = String.format(message, p1, p2);
    Assertions.assertEquals(expected, ex.getMessage(), "Does not format the message");
  }

  @Test
  public void canCheckArgumentWithLongLongMessage() {
    final String message = "failure message %s %s";
    final long p1 = 1;
    final long p2 = 2;

    ValidationUtils.checkArgument(true, message, p1, p2);

    final IllegalArgumentException ex =
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
          ValidationUtils.checkArgument(false, message, p1, p2);
        });
    final String expected = String.format(message, p1, p2);
    Assertions.assertEquals(expected, ex.getMessage(), "Does not format the message");
  }

  @Test
  public void canCheckArgumentWithFloatFloatMessage() {
    final String message = "failure message %s %s";
    final float p1 = 1;
    final float p2 = 2;

    ValidationUtils.checkArgument(true, message, p1, p2);

    final IllegalArgumentException ex =
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
          ValidationUtils.checkArgument(false, message, p1, p2);
        });
    final String expected = String.format(message, p1, p2);
    Assertions.assertEquals(expected, ex.getMessage(), "Does not format the message");
  }

  @Test
  public void canCheckArgumentWithDoubleDoubleMessage() {
    final String message = "failure message %s %s";
    final double p1 = 1;
    final double p2 = 2;

    ValidationUtils.checkArgument(true, message, p1, p2);

    final IllegalArgumentException ex =
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
          ValidationUtils.checkArgument(false, message, p1, p2);
        });
    final String expected = String.format(message, p1, p2);
    Assertions.assertEquals(expected, ex.getMessage(), "Does not format the message");
  }

  @Test
  public void canCheckArgumentWithObjectObjectMessage() {
    final String message = "failure message %s %s";
    final Object p1 = 1;
    final Object p2 = 2;

    ValidationUtils.checkArgument(true, message, p1, p2);

    final IllegalArgumentException ex =
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
          ValidationUtils.checkArgument(false, message, p1, p2);
        });
    final String expected = String.format(message, p1, p2);
    Assertions.assertEquals(expected, ex.getMessage(), "Does not format the message");
  }

  @Test
  public void canCheckState() {
    ValidationUtils.checkState(true);
    Assertions.assertThrows(IllegalStateException.class, () -> {
      ValidationUtils.checkState(false);
    });
  }

  @Test
  public void canCheckStateWithMessage() {
    final String message = "failure message";

    ValidationUtils.checkState(true, message);

    final IllegalStateException ex = Assertions.assertThrows(IllegalStateException.class, () -> {
      ValidationUtils.checkState(false, message);
    });
    final String expected = String.valueOf(message);
    Assertions.assertEquals(expected, ex.getMessage(), "Does not format the message");
  }

  @Test
  public void canCheckStateWithByteMessage() {
    final String message = "failure message %s";
    final byte p1 = 1;

    ValidationUtils.checkState(true, message, p1);

    final IllegalStateException ex = Assertions.assertThrows(IllegalStateException.class, () -> {
      ValidationUtils.checkState(false, message, p1);
    });
    final String expected = String.format(message, p1);
    Assertions.assertEquals(expected, ex.getMessage(), "Does not format the message");
  }

  @Test
  public void canCheckStateWithIntMessage() {
    final String message = "failure message %s";
    final int p1 = 1;

    ValidationUtils.checkState(true, message, p1);

    final IllegalStateException ex = Assertions.assertThrows(IllegalStateException.class, () -> {
      ValidationUtils.checkState(false, message, p1);
    });
    final String expected = String.format(message, p1);
    Assertions.assertEquals(expected, ex.getMessage(), "Does not format the message");
  }

  @Test
  public void canCheckStateWithLongMessage() {
    final String message = "failure message %s";
    final long p1 = 1;

    ValidationUtils.checkState(true, message, p1);

    final IllegalStateException ex = Assertions.assertThrows(IllegalStateException.class, () -> {
      ValidationUtils.checkState(false, message, p1);
    });
    final String expected = String.format(message, p1);
    Assertions.assertEquals(expected, ex.getMessage(), "Does not format the message");
  }

  @Test
  public void canCheckStateWithFloatMessage() {
    final String message = "failure message %s";
    final float p1 = 1;

    ValidationUtils.checkState(true, message, p1);

    final IllegalStateException ex = Assertions.assertThrows(IllegalStateException.class, () -> {
      ValidationUtils.checkState(false, message, p1);
    });
    final String expected = String.format(message, p1);
    Assertions.assertEquals(expected, ex.getMessage(), "Does not format the message");
  }

  @Test
  public void canCheckStateWithDoubleMessage() {
    final String message = "failure message %s";
    final double p1 = 1;

    ValidationUtils.checkState(true, message, p1);

    final IllegalStateException ex = Assertions.assertThrows(IllegalStateException.class, () -> {
      ValidationUtils.checkState(false, message, p1);
    });
    final String expected = String.format(message, p1);
    Assertions.assertEquals(expected, ex.getMessage(), "Does not format the message");
  }

  @Test
  public void canCheckStateWithObjectMessage() {
    final String message = "failure message %s";
    final Object p1 = 1;

    ValidationUtils.checkState(true, message, p1);

    final IllegalStateException ex = Assertions.assertThrows(IllegalStateException.class, () -> {
      ValidationUtils.checkState(false, message, p1);
    });
    final String expected = String.format(message, p1);
    Assertions.assertEquals(expected, ex.getMessage(), "Does not format the message");
  }

  @Test
  public void canCheckStateWithByteByteMessage() {
    final String message = "failure message %s %s";
    final byte p1 = 1;
    final byte p2 = 2;

    ValidationUtils.checkState(true, message, p1, p2);

    final IllegalStateException ex = Assertions.assertThrows(IllegalStateException.class, () -> {
      ValidationUtils.checkState(false, message, p1, p2);
    });
    final String expected = String.format(message, p1, p2);
    Assertions.assertEquals(expected, ex.getMessage(), "Does not format the message");
  }

  @Test
  public void canCheckStateWithIntIntMessage() {
    final String message = "failure message %s %s";
    final int p1 = 1;
    final int p2 = 2;

    ValidationUtils.checkState(true, message, p1, p2);

    final IllegalStateException ex = Assertions.assertThrows(IllegalStateException.class, () -> {
      ValidationUtils.checkState(false, message, p1, p2);
    });
    final String expected = String.format(message, p1, p2);
    Assertions.assertEquals(expected, ex.getMessage(), "Does not format the message");
  }

  @Test
  public void canCheckStateWithLongLongMessage() {
    final String message = "failure message %s %s";
    final long p1 = 1;
    final long p2 = 2;

    ValidationUtils.checkState(true, message, p1, p2);

    final IllegalStateException ex = Assertions.assertThrows(IllegalStateException.class, () -> {
      ValidationUtils.checkState(false, message, p1, p2);
    });
    final String expected = String.format(message, p1, p2);
    Assertions.assertEquals(expected, ex.getMessage(), "Does not format the message");
  }

  @Test
  public void canCheckStateWithFloatFloatMessage() {
    final String message = "failure message %s %s";
    final float p1 = 1;
    final float p2 = 2;

    ValidationUtils.checkState(true, message, p1, p2);

    final IllegalStateException ex = Assertions.assertThrows(IllegalStateException.class, () -> {
      ValidationUtils.checkState(false, message, p1, p2);
    });
    final String expected = String.format(message, p1, p2);
    Assertions.assertEquals(expected, ex.getMessage(), "Does not format the message");
  }

  @Test
  public void canCheckStateWithDoubleDoubleMessage() {
    final String message = "failure message %s %s";
    final double p1 = 1;
    final double p2 = 2;

    ValidationUtils.checkState(true, message, p1, p2);

    final IllegalStateException ex = Assertions.assertThrows(IllegalStateException.class, () -> {
      ValidationUtils.checkState(false, message, p1, p2);
    });
    final String expected = String.format(message, p1, p2);
    Assertions.assertEquals(expected, ex.getMessage(), "Does not format the message");
  }

  @Test
  public void canCheckStateWithObjectObjectMessage() {
    final String message = "failure message %s %s";
    final Object p1 = 1;
    final Object p2 = 2;

    ValidationUtils.checkState(true, message, p1, p2);

    final IllegalStateException ex = Assertions.assertThrows(IllegalStateException.class, () -> {
      ValidationUtils.checkState(false, message, p1, p2);
    });
    final String expected = String.format(message, p1, p2);
    Assertions.assertEquals(expected, ex.getMessage(), "Does not format the message");
  }

  @Test
  public void canCheckNotNull() {
    ValidationUtils.checkNotNull(this);
    Assertions.assertThrows(NullPointerException.class, () -> {
      ValidationUtils.checkNotNull(null);
    });
  }

  @Test
  public void canCheckNotNullWithMessage() {
    final String message = "failure message";

    final Object anything = new Object();
    final Object result = ValidationUtils.checkNotNull(anything, message);
    Assertions.assertSame(anything, result, "Did not return the same object");

    final NullPointerException ex = Assertions.assertThrows(NullPointerException.class, () -> {
      ValidationUtils.checkNotNull(null, message);
    });
    final String expected = String.valueOf(message);
    Assertions.assertEquals(expected, ex.getMessage(), "Does not format the message");
  }

  @Test
  public void canCheckNotNullWithByteMessage() {
    final String message = "failure message %s";
    final byte p1 = 1;

    final Object anything = new Object();
    final Object result = ValidationUtils.checkNotNull(anything, message, p1);
    Assertions.assertSame(anything, result, "Did not return the same object");

    final NullPointerException ex = Assertions.assertThrows(NullPointerException.class, () -> {
      ValidationUtils.checkNotNull(null, message, p1);
    });
    final String expected = String.format(message, p1);
    Assertions.assertEquals(expected, ex.getMessage(), "Does not format the message");
  }

  @Test
  public void canCheckNotNullWithIntMessage() {
    final String message = "failure message %s";
    final int p1 = 1;

    final Object anything = new Object();
    final Object result = ValidationUtils.checkNotNull(anything, message, p1);
    Assertions.assertSame(anything, result, "Did not return the same object");

    final NullPointerException ex = Assertions.assertThrows(NullPointerException.class, () -> {
      ValidationUtils.checkNotNull(null, message, p1);
    });
    final String expected = String.format(message, p1);
    Assertions.assertEquals(expected, ex.getMessage(), "Does not format the message");
  }

  @Test
  public void canCheckNotNullWithLongMessage() {
    final String message = "failure message %s";
    final long p1 = 1;

    final Object anything = new Object();
    final Object result = ValidationUtils.checkNotNull(anything, message, p1);
    Assertions.assertSame(anything, result, "Did not return the same object");

    final NullPointerException ex = Assertions.assertThrows(NullPointerException.class, () -> {
      ValidationUtils.checkNotNull(null, message, p1);
    });
    final String expected = String.format(message, p1);
    Assertions.assertEquals(expected, ex.getMessage(), "Does not format the message");
  }

  @Test
  public void canCheckNotNullWithFloatMessage() {
    final String message = "failure message %s";
    final float p1 = 1;

    final Object anything = new Object();
    final Object result = ValidationUtils.checkNotNull(anything, message, p1);
    Assertions.assertSame(anything, result, "Did not return the same object");

    final NullPointerException ex = Assertions.assertThrows(NullPointerException.class, () -> {
      ValidationUtils.checkNotNull(null, message, p1);
    });
    final String expected = String.format(message, p1);
    Assertions.assertEquals(expected, ex.getMessage(), "Does not format the message");
  }

  @Test
  public void canCheckNotNullWithDoubleMessage() {
    final String message = "failure message %s";
    final double p1 = 1;

    final Object anything = new Object();
    final Object result = ValidationUtils.checkNotNull(anything, message, p1);
    Assertions.assertSame(anything, result, "Did not return the same object");

    final NullPointerException ex = Assertions.assertThrows(NullPointerException.class, () -> {
      ValidationUtils.checkNotNull(null, message, p1);
    });
    final String expected = String.format(message, p1);
    Assertions.assertEquals(expected, ex.getMessage(), "Does not format the message");
  }

  @Test
  public void canCheckNotNullWithObjectMessage() {
    final String message = "failure message %s";
    final Object p1 = 1;

    final Object anything = new Object();
    final Object result = ValidationUtils.checkNotNull(anything, message, p1);
    Assertions.assertSame(anything, result, "Did not return the same object");

    final NullPointerException ex = Assertions.assertThrows(NullPointerException.class, () -> {
      ValidationUtils.checkNotNull(null, message, p1);
    });
    final String expected = String.format(message, p1);
    Assertions.assertEquals(expected, ex.getMessage(), "Does not format the message");
  }

  @Test
  public void canCheckNotNullWithByteByteMessage() {
    final String message = "failure message %s %s";
    final byte p1 = 1;
    final byte p2 = 2;

    final Object anything = new Object();
    final Object result = ValidationUtils.checkNotNull(anything, message, p1, p2);
    Assertions.assertSame(anything, result, "Did not return the same object");

    final NullPointerException ex = Assertions.assertThrows(NullPointerException.class, () -> {
      ValidationUtils.checkNotNull(null, message, p1, p2);
    });
    final String expected = String.format(message, p1, p2);
    Assertions.assertEquals(expected, ex.getMessage(), "Does not format the message");
  }

  @Test
  public void canCheckNotNullWithIntIntMessage() {
    final String message = "failure message %s %s";
    final int p1 = 1;
    final int p2 = 2;

    final Object anything = new Object();
    final Object result = ValidationUtils.checkNotNull(anything, message, p1, p2);
    Assertions.assertSame(anything, result, "Did not return the same object");

    final NullPointerException ex = Assertions.assertThrows(NullPointerException.class, () -> {
      ValidationUtils.checkNotNull(null, message, p1, p2);
    });
    final String expected = String.format(message, p1, p2);
    Assertions.assertEquals(expected, ex.getMessage(), "Does not format the message");
  }

  @Test
  public void canCheckNotNullWithLongLongMessage() {
    final String message = "failure message %s %s";
    final long p1 = 1;
    final long p2 = 2;

    final Object anything = new Object();
    final Object result = ValidationUtils.checkNotNull(anything, message, p1, p2);
    Assertions.assertSame(anything, result, "Did not return the same object");

    final NullPointerException ex = Assertions.assertThrows(NullPointerException.class, () -> {
      ValidationUtils.checkNotNull(null, message, p1, p2);
    });
    final String expected = String.format(message, p1, p2);
    Assertions.assertEquals(expected, ex.getMessage(), "Does not format the message");
  }

  @Test
  public void canCheckNotNullWithFloatFloatMessage() {
    final String message = "failure message %s %s";
    final float p1 = 1;
    final float p2 = 2;

    final Object anything = new Object();
    final Object result = ValidationUtils.checkNotNull(anything, message, p1, p2);
    Assertions.assertSame(anything, result, "Did not return the same object");

    final NullPointerException ex = Assertions.assertThrows(NullPointerException.class, () -> {
      ValidationUtils.checkNotNull(null, message, p1, p2);
    });
    final String expected = String.format(message, p1, p2);
    Assertions.assertEquals(expected, ex.getMessage(), "Does not format the message");
  }

  @Test
  public void canCheckNotNullWithDoubleDoubleMessage() {
    final String message = "failure message %s %s";
    final double p1 = 1;
    final double p2 = 2;

    final Object anything = new Object();
    final Object result = ValidationUtils.checkNotNull(anything, message, p1, p2);
    Assertions.assertSame(anything, result, "Did not return the same object");

    final NullPointerException ex = Assertions.assertThrows(NullPointerException.class, () -> {
      ValidationUtils.checkNotNull(null, message, p1, p2);
    });
    final String expected = String.format(message, p1, p2);
    Assertions.assertEquals(expected, ex.getMessage(), "Does not format the message");
  }

  @Test
  public void canCheckNotNullWithObjectObjectMessage() {
    final String message = "failure message %s %s";
    final Object p1 = 1;
    final Object p2 = 2;

    final Object anything = new Object();
    final Object result = ValidationUtils.checkNotNull(anything, message, p1, p2);
    Assertions.assertSame(anything, result, "Did not return the same object");

    final NullPointerException ex = Assertions.assertThrows(NullPointerException.class, () -> {
      ValidationUtils.checkNotNull(null, message, p1, p2);
    });
    final String expected = String.format(message, p1, p2);
    Assertions.assertEquals(expected, ex.getMessage(), "Does not format the message");
  }

  @Test
  public void canCheckPositiveWithInt() {
    int value = 0;
    ValidationUtils.checkPositive(value);
    for (int badValue : new int[] {Integer.MIN_VALUE, -1}) {
      final IllegalArgumentException ex =
          Assertions.assertThrows(IllegalArgumentException.class, () -> {
            ValidationUtils.checkPositive(badValue);
          });
      Assertions.assertTrue(ex.getMessage().contains(String.valueOf(badValue)),
          "Does not contain the bad value");
    }
  }

  @Test
  public void canCheckPositiveWithLong() {
    long value = 0;
    ValidationUtils.checkPositive(value);
    for (long badValue : new long[] {Long.MIN_VALUE, -1}) {
      final IllegalArgumentException ex =
          Assertions.assertThrows(IllegalArgumentException.class, () -> {
            ValidationUtils.checkPositive(badValue);
          });
      Assertions.assertTrue(ex.getMessage().contains(String.valueOf(badValue)),
          "Does not contain the bad value");
    }
  }

  @Test
  public void canCheckPositiveWithFloat() {
    float value = 0;
    ValidationUtils.checkPositive(value);
    for (float badValue : new float[] {Float.NaN, -1}) {
      final IllegalArgumentException ex =
          Assertions.assertThrows(IllegalArgumentException.class, () -> {
            ValidationUtils.checkPositive(badValue);
          });
      Assertions.assertTrue(ex.getMessage().contains(String.valueOf(badValue)),
          "Does not contain the bad value");
    }
  }

  @Test
  public void canCheckPositiveWithDouble() {
    double value = 0;
    ValidationUtils.checkPositive(value);
    for (double badValue : new double[] {Double.NaN, -1}) {
      final IllegalArgumentException ex =
          Assertions.assertThrows(IllegalArgumentException.class, () -> {
            ValidationUtils.checkPositive(badValue);
          });
      Assertions.assertTrue(ex.getMessage().contains(String.valueOf(badValue)),
          "Does not contain the bad value");
    }
  }

  @Test
  public void canCheckStrictlyPositiveWithInt() {
    int value = 1;
    ValidationUtils.checkStrictlyPositive(value);
    for (int badValue : new int[] {Integer.MIN_VALUE, -1, 0}) {
      final IllegalArgumentException ex =
          Assertions.assertThrows(IllegalArgumentException.class, () -> {
            ValidationUtils.checkStrictlyPositive(badValue);
          });
      Assertions.assertTrue(ex.getMessage().contains(String.valueOf(badValue)),
          "Does not contain the bad value");
    }
  }

  @Test
  public void canCheckStrictlyPositiveWithLong() {
    long value = 1;
    ValidationUtils.checkStrictlyPositive(value);
    for (long badValue : new long[] {Long.MIN_VALUE, -1, 0}) {
      final IllegalArgumentException ex =
          Assertions.assertThrows(IllegalArgumentException.class, () -> {
            ValidationUtils.checkStrictlyPositive(badValue);
          });
      Assertions.assertTrue(ex.getMessage().contains(String.valueOf(badValue)),
          "Does not contain the bad value");
    }
  }

  @Test
  public void canCheckStrictlyPositiveWithFloat() {
    float value = 1;
    ValidationUtils.checkStrictlyPositive(value);
    for (float badValue : new float[] {Float.NaN, -1, 0}) {
      final IllegalArgumentException ex =
          Assertions.assertThrows(IllegalArgumentException.class, () -> {
            ValidationUtils.checkStrictlyPositive(badValue);
          });
      Assertions.assertTrue(ex.getMessage().contains(String.valueOf(badValue)),
          "Does not contain the bad value");
    }
  }

  @Test
  public void canCheckStrictlyPositiveWithDouble() {
    double value = 1;
    ValidationUtils.checkStrictlyPositive(value);
    for (double badValue : new double[] {Double.NaN, -1, 0}) {
      final IllegalArgumentException ex =
          Assertions.assertThrows(IllegalArgumentException.class, () -> {
            ValidationUtils.checkStrictlyPositive(badValue);
          });
      Assertions.assertTrue(ex.getMessage().contains(String.valueOf(badValue)),
          "Does not contain the bad value");
    }
  }

  @Test
  public void canCheckArrayLength() {
    ValidationUtils.checkArrayLength(new boolean[1]);
    ValidationUtils.checkArrayLength(new byte[1]);
    ValidationUtils.checkArrayLength(new char[1]);
    ValidationUtils.checkArrayLength(new double[1]);
    ValidationUtils.checkArrayLength(new float[1]);
    ValidationUtils.checkArrayLength(new int[1]);
    ValidationUtils.checkArrayLength(new long[1]);
    ValidationUtils.checkArrayLength(new short[1]);
    ValidationUtils.checkArrayLength(new Object[1]);

    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      ValidationUtils.checkArrayLength(new boolean[0]);
    });
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      ValidationUtils.checkArrayLength(new byte[0]);
    });
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      ValidationUtils.checkArrayLength(new char[0]);
    });
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      ValidationUtils.checkArrayLength(new double[0]);
    });
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      ValidationUtils.checkArrayLength(new float[0]);
    });
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      ValidationUtils.checkArrayLength(new int[0]);
    });
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      ValidationUtils.checkArrayLength(new long[0]);
    });
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      ValidationUtils.checkArrayLength(new short[0]);
    });
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      ValidationUtils.checkArrayLength(new Object[0]);
    });

    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      ValidationUtils.checkArrayLength(null);
    }, "null input");
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      ValidationUtils.checkArrayLength(new Object());
    }, "non-array input");
  }

  @Test
  public void canCheckArrayIndex() {
    final Object o1 = new Object();
    final Object[] array = {o1};
    ValidationUtils.checkIndex(0, array);
    Assertions.assertThrows(NullPointerException.class, () -> {
      ValidationUtils.checkIndex(0, null);
    }, "null input");
    Assertions.assertThrows(IndexOutOfBoundsException.class, () -> {
      ValidationUtils.checkIndex(-1, array);
    }, "negative index");
    Assertions.assertThrows(IndexOutOfBoundsException.class, () -> {
      ValidationUtils.checkIndex(array.length, array);
    }, "out-of-bounds index");
  }

  @Test
  public void canCheckSizeIndex() {
    int size = 1;
    ValidationUtils.checkIndex(0, size);
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      ValidationUtils.checkIndex(0, -1);
    }, "negative size");
    Assertions.assertThrows(IndexOutOfBoundsException.class, () -> {
      ValidationUtils.checkIndex(-1, size);
    }, "negative index");
    Assertions.assertThrows(IndexOutOfBoundsException.class, () -> {
      ValidationUtils.checkIndex(size, size);
    }, "out-of-bounds index");
  }
}
