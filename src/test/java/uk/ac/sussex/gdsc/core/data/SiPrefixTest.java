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
 * Copyright (C) 2011 - 2020 Alex Herbert
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

package uk.ac.sussex.gdsc.core.data;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.test.utils.TestLogUtils;

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
  public void testSiPrefixDeci() {
    Assertions.assertEquals(1e-1, SiPrefix.DECI.getFactor());
    Assertions.assertEquals("deci", SiPrefix.DECI.getPrefix());
    Assertions.assertEquals("d", SiPrefix.DECI.getSymbol());
  }

  @Test
  public void testSiConvertions() {
    final SiPrefix[] prefixes = SiPrefix.values();

    for (final SiPrefix p1 : prefixes) {
      Assertions.assertEquals(Math.PI / p1.getFactor(), p1.convert(Math.PI));
      for (final SiPrefix p2 : prefixes) {
        final double converted = p1.convert(Math.PI, p2);
        Assertions.assertEquals(Math.PI * (p2.getFactor() / p1.getFactor()), converted);
        Assertions.assertEquals(Math.PI, p2.convert(converted, p1), 1e-8);
      }
    }
  }

  @Test
  public void testForOrdinal() {
    for (final SiPrefix prefix : SiPrefix.values()) {
      Assertions.assertEquals(prefix, SiPrefix.forOrdinal(prefix.ordinal()));
    }
  }

  @Test
  public void testForOrdinalThrows() {
    Assertions.assertThrows(IllegalArgumentException.class, () -> SiPrefix.forOrdinal(-1));
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> SiPrefix.forOrdinal(Integer.MAX_VALUE));
  }

  @Test
  public void testForOrdinalWithDefaultValue() {
    final SiPrefix prefix0 = SiPrefix.forOrdinal(0);
    Assertions.assertEquals(prefix0, SiPrefix.forOrdinal(-1, null));
    Assertions.assertEquals(prefix0, SiPrefix.forOrdinal(Integer.MAX_VALUE, null));
    for (final SiPrefix prefix : new SiPrefix[] {SiPrefix.DECI, SiPrefix.PETA}) {
      Assertions.assertEquals(prefix, SiPrefix.forOrdinal(prefix.ordinal(), SiPrefix.GIGA));
      Assertions.assertEquals(prefix, SiPrefix.forOrdinal(-1, prefix));
      Assertions.assertEquals(prefix, SiPrefix.forOrdinal(Integer.MAX_VALUE, prefix));
    }
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
