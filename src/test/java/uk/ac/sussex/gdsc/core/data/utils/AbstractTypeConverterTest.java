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

package uk.ac.sussex.gdsc.core.data.utils;

import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"javadoc"})
public class AbstractTypeConverterTest {

  private static class DummyTypeConverter extends AbstractTypeConverter<TimeUnit> {

    DummyTypeConverter(TimeUnit from, TimeUnit to) {
      super(from, to);
    }

    DummyTypeConverter(TimeUnit from, TimeUnit to, boolean suppressExceptions) {
      super(from, to, suppressExceptions);
    }

    @Override
    public double convert(double value) {
      return value * 1.23;
    }

    @Override
    public double convertBack(double value) {
      return value / 1.23;
    }

    @Override
    public String getFunction() {
      return "dummy";
    }
  }

  @Test
  public void testConstructorWithNull() {
    TimeUnit from = TimeUnit.MILLISECONDS;
    TimeUnit to = TimeUnit.SECONDS;
    Assertions.assertThrows(ConversionException.class, () -> new DummyTypeConverter(null, to));
    Assertions.assertThrows(ConversionException.class, () -> new DummyTypeConverter(from, null));
    Assertions.assertThrows(ConversionException.class,
        () -> new DummyTypeConverter(null, to, false));
    Assertions.assertThrows(ConversionException.class,
        () -> new DummyTypeConverter(from, null, false));
    final TypeConverter<TimeUnit> c = new DummyTypeConverter(null, null, true);
    Assertions.assertNull(c.from());
    Assertions.assertNull(c.to());
  }

  @Test
  public void testConverter() {
    final TypeConverter<TimeUnit> c =
        new DummyTypeConverter(TimeUnit.MILLISECONDS, TimeUnit.SECONDS);
    Assertions.assertEquals("SECONDS = f(x=MILLISECONDS) = dummy", c.toString());
    Assertions.assertEquals(TimeUnit.MILLISECONDS, c.from());
    Assertions.assertEquals(TimeUnit.SECONDS, c.to());
    final float value = 234.234234f;
    // float conversion just uses the double conversion by default
    Assertions.assertEquals(c.convert(value), (float) c.convert((double) value));
    Assertions.assertEquals(c.convertBack(value), (float) c.convertBack((double) value));
  }
}
