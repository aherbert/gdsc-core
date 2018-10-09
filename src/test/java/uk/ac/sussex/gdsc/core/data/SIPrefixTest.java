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


import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import uk.ac.sussex.gdsc.test.junit5.*;import uk.ac.sussex.gdsc.test.rng.RngFactory;import uk.ac.sussex.gdsc.test.utils.TestLog;

@SuppressWarnings({"javadoc"})
public class SIPrefixTest {
  private static Logger logger;

  @BeforeAll
  public static void beforeAll() {
    logger = Logger.getLogger(SIPrefixTest.class.getName());
  }

  @AfterAll
  public static void afterAll() {
    logger = null;
  }

  @Test
  public void canGenerateSIPrefix() {
    // This is not a test. It generates the Enum.
    Assumptions.assumeTrue(false);

    //@formatter:off
		final String[] data = {
            "24","yotta","Y",
            "21","zetta","Z",
            "18","exa","E",
            "15","peta","P",
            "12","tera","T",
            "9","giga","G",
            "6","mega","M",
            "3","kilo","k",
            "2","hecto","h",
            "1","deka","da",
            "0","","",
            "-1","deci","d",
            "-2","centi","c",
            "-3","milli","m",
            "-6","micro","µ",
            "-9","nano","n",
            "-12","pico","p",
            "-15","femto","f",
            "-18","atto","a",
            "-21","zepto","z",
            "-24","yocto","y"
		};
		//@formatter:on
    final StringBuilder sb = new StringBuilder();
    for (int i = 0; i < data.length; i += 3) {
      add(sb, data[i], data[i + 1], data[i + 2]);
    }
    System.out.println(sb.toString());
  }

  private static void add(StringBuilder sb, String pow, String name, String symbol) {
    final String name2 = (name.length() == 0) ? "none" : name;
    sb.append("    /** ").append(name2.substring(0, 1).toUpperCase()).append(name2.substring(1))
        .append(" */\n");
    sb.append("    ").append(name2.toUpperCase()).append(" {\n");
    sb.append("    @Override public double getFactor() { return 1e").append(pow).append("; }\n");
    sb.append("    @Override public String getName() { return \"").append(name).append("\"; }\n");
    sb.append("    @Override public String getSymbol() { return \"").append(symbol)
        .append("\"; }\n");
    sb.append("    },\n");
  }

  @Test
  public void canGetPrefix() {
    // Edge cases
    canGetPrefix(0, SIPrefix.NONE);
    canGetPrefix(Double.POSITIVE_INFINITY, SIPrefix.NONE);
    canGetPrefix(Double.NEGATIVE_INFINITY, SIPrefix.NONE);
    canGetPrefix(Double.NaN, SIPrefix.NONE);

    for (final int sign : new int[] {-1, 1}) {
      // Edge case high
      canGetPrefix(sign, SIPrefix.YOTTA.getFactor() * 10, SIPrefix.YOTTA);

      // Above 1
      final SIPrefix[] values = SIPrefix.values();
      for (int i = 0; i < values.length; i++) {
        if (values[i].getFactor() > 1) {
          if (i > 0) {
            canGetPrefix(sign, (values[i].getFactor() + values[i - 1].getFactor()) / 2, values[i]);
          }
          canGetPrefix(sign, values[i].getFactor(), values[i]);
          if (i + 1 < values.length) {
            canGetPrefix(sign, (values[i].getFactor() + values[i + 1].getFactor()) / 2,
                values[i + 1]);
          }
        }
      }

      // 1
      canGetPrefix(sign, 0.5, SIPrefix.DECI);
      canGetPrefix(sign, 1, SIPrefix.NONE);
      canGetPrefix(sign, 2, SIPrefix.NONE);

      // Below 1
      for (int i = 0; i < values.length; i++) {
        if (values[i].getFactor() < 1) {
          if (i > 0) {
            canGetPrefix(sign, (values[i].getFactor() + values[i - 1].getFactor()) / 2, values[i]);
          }
          canGetPrefix(sign, values[i].getFactor(), values[i]);
          if (i + 1 < values.length) {
            canGetPrefix(sign, (values[i].getFactor() + values[i + 1].getFactor()) / 2,
                values[i + 1]);
          }
        }
      }

      // Edge case low
      canGetPrefix(sign, SIPrefix.YOCTO.getFactor() / 10, SIPrefix.YOCTO);
    }
  }

  private static void canGetPrefix(double value, SIPrefix e) {
    canGetPrefix(1, value, e);
  }

  private static void canGetPrefix(int sign, double value, SIPrefix e) {
    value *= sign;
    final SIPrefix o = SIPrefix.getPrefix(value);
    logger.log(TestLog.getRecord(Level.FINE, "Value %s = %s %s (%s)", value, o.convert(value),
        o.getName(), o.getSymbol()));
    Assertions.assertEquals(e, o);
  }
}
