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
 * Copyright (C) 2011 - 2021 Alex Herbert
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
class MultiplyAddTypeConverterTest {

  @Test
  void testConstructorThrows() {
    for (final double d : new double[] {Double.NaN, Double.POSITIVE_INFINITY,
        Double.NEGATIVE_INFINITY}) {
      Assertions.assertThrows(ConversionException.class,
          () -> new MultiplyAddTypeConverter<>(TimeUnit.MILLISECONDS, TimeUnit.SECONDS, 0.001, d));
    }
  }

  @Test
  void testConverter() {
    final MultiplyAddTypeConverter<String> c =
        new MultiplyAddTypeConverter<>("Celcius", "Fahrenheit", 1.8, 32);
    Assertions.assertEquals("x * 1.8 + 32.0", c.getFunction());
    Assertions.assertEquals(-40.0, c.convert(-40.0));
    Assertions.assertEquals(32.0, c.convert(0.0));
    Assertions.assertEquals(212.0, c.convert(100.0));
    Assertions.assertEquals(0.0, c.convertBack(32.0));
    Assertions.assertEquals(100.0, c.convertBack(212.0));
    Assertions.assertEquals(-40.0, c.convertBack(-40.0));
  }
}
