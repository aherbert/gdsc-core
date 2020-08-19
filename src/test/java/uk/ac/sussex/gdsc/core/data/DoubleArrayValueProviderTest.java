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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"javadoc"})
public class DoubleArrayValueProviderTest {
  @Test
  public void testConstructorThrows() {
    Assertions.assertThrows(DataException.class, () -> new DoubleArrayValueProvider(new double[0]));
  }

  @Test
  public void canProvideData() {
    final int maxx = 5;
    final double[] data = new double[maxx];
    for (int x = 0, i = 0; x < maxx; x++) {
      data[x] = i++;
    }

    final DoubleArrayValueProvider f = new DoubleArrayValueProvider(data);
    Assertions.assertEquals(maxx, f.getLength());
    Assertions.assertSame(data, f.toArray());

    final double[] values = new double[3];

    final int[] test = {-1, 0, 1};

    for (int x = 0; x < maxx; x++) {
      Assertions.assertEquals(data[x], f.get(x));

      if (x > 0 && x < maxx - 1) {
        f.get(x, values);

        for (final int i : test) {
          Assertions.assertEquals(data[x + i], values[i + 1]);
        }
      }
    }
  }
}
