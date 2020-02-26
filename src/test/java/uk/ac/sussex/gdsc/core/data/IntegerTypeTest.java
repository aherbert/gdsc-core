package uk.ac.sussex.gdsc.core.data;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"javadoc"})
public class IntegerTypeTest {
  @Test
  public void canGenerateIntegerType() {
    final Level level = Level.FINEST;
    // This is not a test. It generates the Enum.
    // It is left to ensure the code will run.

    final StringBuilder sb = new StringBuilder();
    for (int bitDepth = 1; bitDepth <= 64; bitDepth++) {
      add(sb, true, bitDepth);
    }
    for (int bitDepth = 1; bitDepth <= 63; bitDepth++) {
      add(sb, false, bitDepth);
    }
    Logger.getLogger(getClass().getName()).log(level, () -> sb.toString());
  }

  private static void add(StringBuilder sb, boolean signed, int bitDepth) {
    if (signed) {
      sb.append("    /** A signed ").append(bitDepth).append("-bit integer. */\n");
      sb.append("    SIGNED_").append(bitDepth);
      sb.append("(\"Signed ").append(bitDepth).append("-bit integer\", ");
      sb.append(minSigned(bitDepth)).append("L, ");
      sb.append(maxSigned(bitDepth)).append("L, true, ");
    } else {
      sb.append("    /** An unsigned ").append(bitDepth).append("-bit integer */\n");
      sb.append("    UNSIGNED_").append(bitDepth);
      sb.append("(\"Unsigned ").append(bitDepth).append("-bit integer\", 0L, ");
      sb.append(maxUnsigned(bitDepth)).append("L, false, ");
    }
    sb.append(bitDepth).append("),\n");
  }

  @Test
  public void canProvideIntegerTypeData() {

    // This is a problem for 64-bit signed integers.
    Assertions.assertEquals(Long.MIN_VALUE, Math.abs(Long.MIN_VALUE),
        "abs(Long.MIN_VALUE) should should be Long.MIN_VALUE");
    System.out.println(Long.MIN_VALUE * Long.MIN_VALUE);
    System.out.println(Long.MIN_VALUE * Long.MAX_VALUE);
    System.out.println(Long.MAX_VALUE * Long.MAX_VALUE);

    for (final IntegerType type : IntegerType.values()) {
      final int bd = type.getBitDepth();
      Assertions.assertTrue(type.getTypeName().contains(Integer.toString(bd) + "-bit"));
      Assertions.assertEquals(type, IntegerType.forOrdinal(type.ordinal()));

      if (type.isSigned()) {
        // Signed
        Assertions.assertTrue(type.getTypeName().contains("Signed"));
        Assertions.assertEquals(minSigned(bd), type.getMin(), type.getTypeName());
        Assertions.assertEquals(maxSigned(bd), type.getMax(), type.getTypeName());
      } else {
        // Unsigned
        Assertions.assertTrue(type.getTypeName().contains("Unsigned"));
        Assertions.assertEquals(0L, type.getMin(), type.getTypeName());
        Assertions.assertEquals(maxUnsigned(bd), type.getMax(), type.getTypeName());
      }
    }
  }

  private static long maxUnsigned(int bd) {
    long max = 1;
    while (bd-- > 0) {
      max *= 2L;
    }
    return max - 1;
  }

  private static long maxSigned(int bd) {
    long max = 1;
    while (bd-- > 1) {
      max *= 2L;
    }
    return max - 1;
  }

  private static long minSigned(int bd) {
    long max = 1;
    while (bd-- > 1) {
      max *= 2L;
    }
    return -max;
  }
}
