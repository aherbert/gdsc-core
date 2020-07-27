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
public class MultiplyTypeConverterTest {

  @Test
  public void testConstructorThrows() {
    for (final double d : new double[] {Double.NaN, Double.POSITIVE_INFINITY,
        Double.NEGATIVE_INFINITY}) {
      Assertions.assertThrows(ConversionException.class,
          () -> new MultiplyTypeConverter<>(TimeUnit.MILLISECONDS, TimeUnit.SECONDS, d));
    }
  }

  @Test
  public void testConverter() {
    final MultiplyTypeConverter<TimeUnit> c =
        new MultiplyTypeConverter<>(TimeUnit.MILLISECONDS, TimeUnit.SECONDS, 0.001);
    Assertions.assertEquals("x * 0.001", c.getFunction());
    Assertions.assertEquals(1.0, c.convert(1000.0));
    Assertions.assertEquals(1000.0, c.convertBack(1.0));
    Assertions.assertEquals(1f, c.convert(1000f));
    Assertions.assertEquals(1000f, c.convertBack(1f));
  }
}
