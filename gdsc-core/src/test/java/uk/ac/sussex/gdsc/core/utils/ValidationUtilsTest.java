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
 * Copyright (C) 2011 - 2022 Alex Herbert
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

package uk.ac.sussex.gdsc.core.utils;

import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.test.utils.TestLogging.TestLevel;

@SuppressWarnings({"javadoc"})
class ValidationUtilsTest {

  private static final String[] series = {"first", "second"};

  private static Logger logger;

  /** The level at which the generated methods are logged. */
  private final Level level = TestLevel.TEST_DEBUG;

  enum Arguments {
    NONE, SUPPLIER, VALUE_OF, FORMAT;
  }

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
  void canGenerateMethods() {
    final String[] name = {"Argument", "State", "NotNull"};
    final String[] ex = {"IllegalArgument", "IllegalState", "NullPointer"};
    final boolean[] generics = {false, false, true};
    final StringBuilder sb = new StringBuilder(1000);
    sb.append('\n');
    for (int i = 0; i < name.length; i++) {
      generateMethods(sb, generics[i], name[i], ex[i]);
    }
    logger.log(level, () -> sb.toString());
  }

  private static void generateMethods(StringBuilder sb, boolean generic, String methodSuffix,
      String exceptionPrefix) {
    final String[] types = {"byte", "int", "long", "float", "double", "Object"};
    doGenerateMethods(sb, generic, methodSuffix, exceptionPrefix, Arguments.NONE);
    doGenerateMethods(sb, generic, methodSuffix, exceptionPrefix, Arguments.SUPPLIER);
    doGenerateMethods(sb, generic, methodSuffix, exceptionPrefix, Arguments.VALUE_OF);
    for (final String type : types) {
      doGenerateMethods(sb, generic, methodSuffix, exceptionPrefix, Arguments.FORMAT, type);
    }
    for (final String type : types) {
      doGenerateMethods(sb, generic, methodSuffix, exceptionPrefix, Arguments.FORMAT, type, type);
    }
  }

  private static void doGenerateMethods(StringBuilder sb, boolean generic, String methodSuffix,
      String exceptionPrefix, Arguments args, String... params) {
    sb.append("/**\n");
    if (generic) {
      sb.append(" * Checks that the specified object reference is not {@code null}.\n");
    } else {
      sb.append(" * Check the {@code result} is {@code true}.\n");
    }
    sb.append(" *\n");
    if (args == Arguments.FORMAT) {
      sb.append(" * <p>If not {@code true} the exception message is formed using\n");
      sb.append(" * {@link String#format(String, Object...)}.\n");
      sb.append(" *\n");
    } else if (args == Arguments.VALUE_OF) {
      sb.append(" * <p>If not {@code true} the exception message is formed using\n");
      sb.append(" * {@link String#valueOf(Object)}.\n");
      sb.append(" *\n");
    } else if (args == Arguments.SUPPLIER) {
      sb.append(" * <p>If not {@code true} the exception message is formed using\n");
      sb.append(" * {@link Supplier#get()}.\n");
      sb.append(" *\n");
    }
    if (generic) {
      sb.append(" * @param <T> the type of the reference\n");
      sb.append(" * @param object the object reference to check for nullity\n");
    } else {
      sb.append(" * @param result the result\n");
    }
    if (args == Arguments.FORMAT) {
      sb.append(" * @param format the format of the exception message\n");
    } else if (args == Arguments.VALUE_OF) {
      sb.append(" * @param message the object used to form the exception message\n");
    } else if (args == Arguments.SUPPLIER) {
      sb.append(" * @param message the supplier of the exception message\n");
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
    if (args == Arguments.FORMAT) {
      sb.append(", String format");
    } else if (args == Arguments.VALUE_OF) {
      sb.append(", Object message");
    } else if (args == Arguments.SUPPLIER) {
      sb.append(", Supplier<String> message");
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
    if (args == Arguments.FORMAT) {
      sb.append("String.format(format");
      for (int i = 0; i < params.length; i++) {
        sb.append(", p").append(i + 1);
      }
      sb.append(")");
    } else if (args == Arguments.VALUE_OF) {
      sb.append("String.valueOf(message)");
    } else if (args == Arguments.SUPPLIER) {
      sb.append("message.get()");
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
  void canGenerateTestMethods() {
    final String[] name = {"Argument", "State", "NotNull"};
    final String[] ex = {"IllegalArgument", "IllegalState", "NullPointer"};
    final boolean[] generics = {false, false, true};
    final StringBuilder sb = new StringBuilder(1000);
    sb.append('\n');
    for (int i = 0; i < name.length; i++) {
      generateTestMethods(sb, generics[i], name[i], ex[i]);
    }
    logger.log(level, () -> sb.toString());
  }

  private static void generateTestMethods(StringBuilder sb, boolean generic, String methodSuffix,
      String exceptionPrefix) {
    final String[] types = {"byte", "int", "long", "float", "double", "Object"};
    doGenerateTestMethods(sb, generic, methodSuffix, exceptionPrefix, Arguments.NONE);
    doGenerateTestMethods(sb, generic, methodSuffix, exceptionPrefix, Arguments.SUPPLIER);
    doGenerateTestMethods(sb, generic, methodSuffix, exceptionPrefix, Arguments.VALUE_OF);
    for (final String type : types) {
      doGenerateTestMethods(sb, generic, methodSuffix, exceptionPrefix, Arguments.FORMAT, type);
    }
    for (final String type : types) {
      doGenerateTestMethods(sb, generic, methodSuffix, exceptionPrefix, Arguments.FORMAT, type,
          type);
    }
  }

  private static void doGenerateTestMethods(StringBuilder sb, boolean generic, String methodSuffix,
      String exceptionPrefix, Arguments args, String... params) {
    sb.append("@Test\n");
    sb.append("void canCheck").append(methodSuffix);
    if (args != Arguments.NONE) {
      sb.append("With");
      if (args == Arguments.FORMAT) {
        sb.append("Format");
      } else if (args == Arguments.VALUE_OF) {
        sb.append("ValueOf");
      } else if (args == Arguments.SUPPLIER) {
        sb.append("Supplier");
      }
      for (int i = 0; i < params.length; i++) {
        sb.append(Character.toUpperCase(params[i].charAt(0))).append(params[i], 1,
            params[i].length());
      }
    }
    sb.append("() {\n");
    if (args == Arguments.NONE) {
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

    if (args == Arguments.SUPPLIER) {
      sb.append("final Supplier<String> message = () -> \"failure message\";");
    } else {
      sb.append("final String message = \"failure message");
      for (int i = 0; i < params.length; i++) {
        sb.append(" %s");
      }
      sb.append("\";\n");
      for (int i = 0; i < params.length; i++) {
        sb.append("final ").append(params[i]).append(" p").append(i + 1).append(" = ").append(i + 1)
            .append(";\n");
      }
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
    sb.append("final String expected = ");
    if (args == Arguments.SUPPLIER) {
      sb.append("message.get(");
    } else if (args == Arguments.VALUE_OF) {
      sb.append("String.valueOf(message");
    } else {
      sb.append("String.format(message");
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
  void canGetDefaultIfNull() {
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
  void canCheckArgument() {
    ValidationUtils.checkArgument(true);
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      ValidationUtils.checkArgument(false);
    });
  }

  @Test
  void canCheckArgumentWithSupplier() {
    final Supplier<String> message = () -> "failure message";
    ValidationUtils.checkArgument(true, message);

    final IllegalArgumentException ex =
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
          ValidationUtils.checkArgument(false, message);
        });
    final String expected = message.get();
    Assertions.assertEquals(expected, ex.getMessage(), "Does not format the message");
  }

  @Test
  void canCheckArgumentWithValueOf() {
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
  void canCheckArgumentWithFormatByte() {
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
  void canCheckArgumentWithFormatInt() {
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
  void canCheckArgumentWithFormatLong() {
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
  void canCheckArgumentWithFormatFloat() {
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
  void canCheckArgumentWithFormatDouble() {
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
  void canCheckArgumentWithFormatObject() {
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
  void canCheckArgumentWithFormatByteByte() {
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
  void canCheckArgumentWithFormatIntInt() {
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
  void canCheckArgumentWithFormatLongLong() {
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
  void canCheckArgumentWithFormatFloatFloat() {
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
  void canCheckArgumentWithFormatDoubleDouble() {
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
  void canCheckArgumentWithFormatObjectObject() {
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
  void canCheckState() {
    ValidationUtils.checkState(true);
    Assertions.assertThrows(IllegalStateException.class, () -> {
      ValidationUtils.checkState(false);
    });
  }

  @Test
  void canCheckStateWithSupplier() {
    final Supplier<String> message = () -> "failure message";
    ValidationUtils.checkState(true, message);

    final IllegalStateException ex = Assertions.assertThrows(IllegalStateException.class, () -> {
      ValidationUtils.checkState(false, message);
    });
    final String expected = message.get();
    Assertions.assertEquals(expected, ex.getMessage(), "Does not format the message");
  }

  @Test
  void canCheckStateWithValueOf() {
    final String message = "failure message";

    ValidationUtils.checkState(true, message);

    final IllegalStateException ex = Assertions.assertThrows(IllegalStateException.class, () -> {
      ValidationUtils.checkState(false, message);
    });
    final String expected = String.valueOf(message);
    Assertions.assertEquals(expected, ex.getMessage(), "Does not format the message");
  }

  @Test
  void canCheckStateWithFormatByte() {
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
  void canCheckStateWithFormatInt() {
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
  void canCheckStateWithFormatLong() {
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
  void canCheckStateWithFormatFloat() {
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
  void canCheckStateWithFormatDouble() {
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
  void canCheckStateWithFormatObject() {
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
  void canCheckStateWithFormatByteByte() {
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
  void canCheckStateWithFormatIntInt() {
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
  void canCheckStateWithFormatLongLong() {
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
  void canCheckStateWithFormatFloatFloat() {
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
  void canCheckStateWithFormatDoubleDouble() {
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
  void canCheckStateWithFormatObjectObject() {
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
  void canCheckNotNull() {
    ValidationUtils.checkNotNull(this);
    Assertions.assertThrows(NullPointerException.class, () -> {
      ValidationUtils.checkNotNull(null);
    });
  }

  @Test
  void canCheckNotNullWithSupplier() {
    final Supplier<String> message = () -> "failure message";
    final Object anything = new Object();
    final Object result = ValidationUtils.checkNotNull(anything, message);
    Assertions.assertSame(anything, result, "Did not return the same object");

    final NullPointerException ex = Assertions.assertThrows(NullPointerException.class, () -> {
      ValidationUtils.checkNotNull(null, message);
    });
    final String expected = message.get();
    Assertions.assertEquals(expected, ex.getMessage(), "Does not format the message");
  }

  @Test
  void canCheckNotNullWithValueOf() {
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
  void canCheckNotNullWithFormatByte() {
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
  void canCheckNotNullWithFormatInt() {
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
  void canCheckNotNullWithFormatLong() {
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
  void canCheckNotNullWithFormatFloat() {
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
  void canCheckNotNullWithFormatDouble() {
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
  void canCheckNotNullWithFormatObject() {
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
  void canCheckNotNullWithFormatByteByte() {
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
  void canCheckNotNullWithFormatIntInt() {
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
  void canCheckNotNullWithFormatLongLong() {
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
  void canCheckNotNullWithFormatFloatFloat() {
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
  void canCheckNotNullWithFormatDoubleDouble() {
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
  void canCheckNotNullWithFormatObjectObject() {
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
  void canCheckPositiveWithInt() {
    Assertions.assertEquals(42, ValidationUtils.checkPositive(42));
    final int value = 0;
    ValidationUtils.checkPositive(value);
    for (final int badValue : new int[] {Integer.MIN_VALUE, -1}) {
      final IllegalArgumentException ex =
          Assertions.assertThrows(IllegalArgumentException.class, () -> {
            ValidationUtils.checkPositive(badValue);
          });
      Assertions.assertTrue(ex.getMessage().contains(String.valueOf(badValue)),
          "Does not contain the bad value");
    }
  }

  @Test
  void canCheckPositiveWithLong() {
    Assertions.assertEquals(42, ValidationUtils.checkPositive(42L));
    final long value = 0;
    ValidationUtils.checkPositive(value);
    for (final long badValue : new long[] {Long.MIN_VALUE, -1}) {
      final IllegalArgumentException ex =
          Assertions.assertThrows(IllegalArgumentException.class, () -> {
            ValidationUtils.checkPositive(badValue);
          });
      Assertions.assertTrue(ex.getMessage().contains(String.valueOf(badValue)),
          "Does not contain the bad value");
    }
  }

  @Test
  void canCheckPositiveWithFloat() {
    Assertions.assertEquals(42, ValidationUtils.checkPositive(42.0F));
    final float value = 0;
    ValidationUtils.checkPositive(value);
    for (final float badValue : new float[] {Float.NaN, -1}) {
      final IllegalArgumentException ex =
          Assertions.assertThrows(IllegalArgumentException.class, () -> {
            ValidationUtils.checkPositive(badValue);
          });
      Assertions.assertTrue(ex.getMessage().contains(String.valueOf(badValue)),
          "Does not contain the bad value");
    }
  }

  @Test
  void canCheckPositiveWithDouble() {
    Assertions.assertEquals(42, ValidationUtils.checkPositive(42.0));
    final double value = 0;
    ValidationUtils.checkPositive(value);
    for (final double badValue : new double[] {Double.NaN, -1}) {
      final IllegalArgumentException ex =
          Assertions.assertThrows(IllegalArgumentException.class, () -> {
            ValidationUtils.checkPositive(badValue);
          });
      Assertions.assertTrue(ex.getMessage().contains(String.valueOf(badValue)),
          "Does not contain the bad value");
    }
  }

  @Test
  void canCheckStrictlyPositiveWithInt() {
    Assertions.assertEquals(42, ValidationUtils.checkPositive(42));
    final int value = 1;
    ValidationUtils.checkStrictlyPositive(value);
    for (final int badValue : new int[] {Integer.MIN_VALUE, -1, 0}) {
      final IllegalArgumentException ex =
          Assertions.assertThrows(IllegalArgumentException.class, () -> {
            ValidationUtils.checkStrictlyPositive(badValue);
          });
      Assertions.assertTrue(ex.getMessage().contains(String.valueOf(badValue)),
          "Does not contain the bad value");
    }
  }

  @Test
  void canCheckStrictlyPositiveWithLong() {
    Assertions.assertEquals(42, ValidationUtils.checkPositive(42L));
    final long value = 1;
    ValidationUtils.checkStrictlyPositive(value);
    for (final long badValue : new long[] {Long.MIN_VALUE, -1, 0}) {
      final IllegalArgumentException ex =
          Assertions.assertThrows(IllegalArgumentException.class, () -> {
            ValidationUtils.checkStrictlyPositive(badValue);
          });
      Assertions.assertTrue(ex.getMessage().contains(String.valueOf(badValue)),
          "Does not contain the bad value");
    }
  }

  @Test
  void canCheckStrictlyPositiveWithFloat() {
    Assertions.assertEquals(42, ValidationUtils.checkPositive(42.0F));
    final float value = 1;
    ValidationUtils.checkStrictlyPositive(value);
    for (final float badValue : new float[] {Float.NaN, -1, 0}) {
      final IllegalArgumentException ex =
          Assertions.assertThrows(IllegalArgumentException.class, () -> {
            ValidationUtils.checkStrictlyPositive(badValue);
          });
      Assertions.assertTrue(ex.getMessage().contains(String.valueOf(badValue)),
          "Does not contain the bad value");
    }
  }

  @Test
  void canCheckStrictlyPositiveWithDouble() {
    Assertions.assertEquals(42, ValidationUtils.checkPositive(42.0));
    final double value = 1;
    ValidationUtils.checkStrictlyPositive(value);
    for (final double badValue : new double[] {Double.NaN, -1, 0}) {
      final IllegalArgumentException ex =
          Assertions.assertThrows(IllegalArgumentException.class, () -> {
            ValidationUtils.checkStrictlyPositive(badValue);
          });
      Assertions.assertTrue(ex.getMessage().contains(String.valueOf(badValue)),
          "Does not contain the bad value");
    }
  }

  @Test
  void canCheckArrayLength() {
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
  void canCheckArrayIndex() {
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
  void canCheckSizeIndex() {
    final int size = 1;
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
