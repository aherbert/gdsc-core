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

package uk.ac.sussex.gdsc.core.data.utils;

import java.util.concurrent.TimeUnit;
import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.Assertions;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.rng.RngFactory;
import uk.ac.sussex.gdsc.test.utils.RandomSeed;

@SuppressWarnings({"javadoc"})
class IdentityTypeConverterTest {

  @SeededTest
  void testConverter(RandomSeed seed) {
    final UniformRandomProvider rng = RngFactory.create(seed.get());
    final IdentityTypeConverter<TimeUnit> c = new IdentityTypeConverter<>(TimeUnit.MILLISECONDS);
    Assertions.assertEquals("x", c.getFunction());
    for (int i = 0; i < 10; i++) {
      final double d = rng.nextDouble();
      Assertions.assertEquals(d, c.convert(d));
      Assertions.assertEquals(d, c.convertBack(d));
      final float f = rng.nextFloat();
      Assertions.assertEquals(f, c.convert(f));
      Assertions.assertEquals(f, c.convertBack(f));
    }
  }
}
