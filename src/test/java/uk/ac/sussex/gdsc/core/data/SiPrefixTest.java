package uk.ac.sussex.gdsc.core.data;

import uk.ac.sussex.gdsc.test.utils.TestLogUtils;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings({"javadoc"})
public class SiPrefixTest {
  private static Logger logger;

  @BeforeAll
  public static void beforeAll() {
    logger = Logger.getLogger(SiPrefixTest.class.getName());
  }

  @AfterAll
  public static void afterAll() {
    logger = null;
  }

  @Test
  public void canGenerateSiPrefix() {
    final Level level = Level.FINEST;
    // This is not a test. It generates the Enum.
    // It is left to ensure the code will run.

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
    logger.log(level, () -> sb.toString());
  }

  private static void add(StringBuilder sb, String pow, String name, String symbol) {
    final String name2 = (name.length() == 0) ? "none" : name;
    sb.append("    /** ").append(name2.substring(0, 1).toUpperCase()).append(name2.substring(1))
        .append(". */\n");
    sb.append("    ").append(name2.toUpperCase());
    sb.append("(1e").append(pow).append(", ");
    sb.append('"').append(name).append("\", ");
    sb.append('"').append(symbol).append("\"),\n");
  }

  @Test
  public void canGetPrefix() {
    // Edge cases
    canGetPrefix(0, SiPrefix.NONE);
    canGetPrefix(Double.POSITIVE_INFINITY, SiPrefix.NONE);
    canGetPrefix(Double.NEGATIVE_INFINITY, SiPrefix.NONE);
    canGetPrefix(Double.NaN, SiPrefix.NONE);

    for (final int sign : new int[] {-1, 1}) {
      // Edge case high
      canGetPrefix(sign, SiPrefix.YOTTA.getFactor() * 10, SiPrefix.YOTTA);

      // Above 1
      final SiPrefix[] values = SiPrefix.values();
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
      canGetPrefix(sign, 0.5, SiPrefix.DECI);
      canGetPrefix(sign, 1, SiPrefix.NONE);
      canGetPrefix(sign, 2, SiPrefix.NONE);

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
      canGetPrefix(sign, SiPrefix.YOCTO.getFactor() / 10, SiPrefix.YOCTO);
    }
  }

  private static void canGetPrefix(double value, SiPrefix expectedPrefix) {
    canGetPrefix(1, value, expectedPrefix);
  }

  private static void canGetPrefix(int sign, double value, SiPrefix expectedPrefix) {
    value *= sign;
    final SiPrefix o = SiPrefix.getSiPrefix(value);
    logger.log(TestLogUtils.getRecord(Level.FINE, "Value %s = %s %s (%s)", value, o.convert(value),
        o.getPrefix(), o.getSymbol()));
    Assertions.assertEquals(expectedPrefix, o);
  }
}
