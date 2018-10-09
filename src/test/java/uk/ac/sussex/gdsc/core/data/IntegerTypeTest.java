package uk.ac.sussex.gdsc.core.data;

import uk.ac.sussex.gdsc.test.junit5.*;
import uk.ac.sussex.gdsc.test.rng.RngFactory;
import org.junit.jupiter.api.*;
import uk.ac.sussex.gdsc.test.api.*;
import uk.ac.sussex.gdsc.test.utils.*;

import uk.ac.sussex.gdsc.test.junit5.*;
import uk.ac.sussex.gdsc.test.rng.RngFactory;
import org.junit.jupiter.api.*;
import uk.ac.sussex.gdsc.test.api.*;

import uk.ac.sussex.gdsc.test.junit5.*;
import uk.ac.sussex.gdsc.test.rng.RngFactory;
import org.junit.jupiter.api.*;

import uk.ac.sussex.gdsc.test.junit5.*;
import uk.ac.sussex.gdsc.test.rng.RngFactory;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"javadoc"})
public class IntegerTypeTest {
  @Test
  public void canGenerateIntegerType() {
    Assumptions.assumeTrue(false);
    final StringBuilder sb = new StringBuilder();
    for (int bitDepth = 1; bitDepth <= 64; bitDepth++) {
      add(sb, true, bitDepth);
    }
    for (int bitDepth = 1; bitDepth <= 63; bitDepth++) {
      add(sb, false, bitDepth);
    }
    System.out.println(sb.toString());
  }

  private static void add(StringBuilder sb, boolean signed, int bitDepth) {
    if (signed) {
      sb.append("    /** A signed ").append(bitDepth).append("-bit integer */\n");
      sb.append("    SIGNED_").append(bitDepth).append(" {\n");
      sb.append("    @Override public String getName() { return \"Signed ").append(bitDepth)
          .append("-bit integer\"; }\n");
      sb.append("    @Override public long getMin() { return ").append(minSigned(bitDepth))
          .append("L; }\n");
      sb.append("    @Override public long getMax() { return ").append(maxSigned(bitDepth))
          .append("L; }\n");
      sb.append("    @Override public boolean isSigned() { return true; }\n");
    } else {
      sb.append("    /** An unsigned ").append(bitDepth).append("-bit integer */\n");
      sb.append("    UNSIGNED_").append(bitDepth).append(" {\n");
      sb.append("    @Override public String getName() { return \"Unsigned ").append(bitDepth)
          .append("-bit integer\"; }\n");
      sb.append("    @Override public long getMin() { return 0L; }\n");
      sb.append("    @Override public long getMax() { return ").append(maxUnsigned(bitDepth))
          .append("L; }\n");
      sb.append("    @Override public boolean isSigned() { return false; }\n");
    }
    sb.append("    @Override public int getBitDepth() { return ").append(bitDepth).append("; }\n");
    sb.append("    },\n");
  }

  @Test
  public void canProvideIntegerTypeData() {
    for (final IntegerType type : IntegerType.values()) {
      final int bd = type.getBitDepth();
      Assertions.assertTrue(type.getName().contains(Integer.toString(bd) + "-bit"));
      Assertions.assertEquals(type, IntegerType.forOrdinal(type.ordinal()));

      if (type.isSigned()) {
        // Signed
        Assertions.assertTrue(type.getName().contains("Signed"));
        Assertions.assertEquals(minSigned(bd), type.getMin(), type.getName());
        Assertions.assertEquals(maxSigned(bd), type.getMax(), type.getName());
        Assertions.assertEquals(-minSigned(bd), type.getAbsoluteMax(), type.getName());
      } else {
        // Unsigned
        Assertions.assertTrue(type.getName().contains("Unsigned"));
        Assertions.assertEquals(0l, type.getMin(), type.getName());
        Assertions.assertEquals(maxUnsigned(bd), type.getMax(), type.getName());
        Assertions.assertEquals(maxUnsigned(bd), type.getAbsoluteMax(), type.getName());
      }
    }
  }

  private static long maxUnsigned(int bd) {
    long max = 1;
    while (bd-- > 0) {
      max *= 2l;
    }
    return max - 1;
  }

  private static long maxSigned(int bd) {
    long max = 1;
    while (bd-- > 1) {
      max *= 2l;
    }
    return max - 1;
  }

  private static long minSigned(int bd) {
    long max = 1;
    while (bd-- > 1) {
      max *= 2l;
    }
    return -max;
  }
}
