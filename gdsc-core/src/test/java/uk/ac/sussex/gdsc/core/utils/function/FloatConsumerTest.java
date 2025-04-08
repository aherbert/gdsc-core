/*-
 * #%L
 * Genome Damage and Stability Centre Core Package
 *
 * Contains core utilities for image analysis and is used by:
 *
 * GDSC ImageJ Plugins - Microscopy image analysis
 *
 * GDSC SMLM ImageJ Plugins - Single molecule localisation microscopy (SMLM)
 * %%
 * Copyright (C) 2011 - 2025 Alex Herbert
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

package uk.ac.sussex.gdsc.core.utils.function;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"javadoc"})
class FloatConsumerTest {

  @Test
  void canAccept() {
    final float[] values = {1, 2, 3};
    final FloatConsumer fun = new FloatConsumer() {
      int index;

      @Override
      public void accept(float value) {
        Assertions.assertEquals(values[index++], value);
      }
    };
    for (final float value : values) {
      fun.accept(value);
    }
  }

  @Test
  void testAndThen() {
    final float[] values = {1, 2, 3};
    final DoubleArrayList l1 = new DoubleArrayList();
    final FloatConsumer f1 = v -> l1.add(v);

    Assertions.assertThrows(NullPointerException.class, () -> f1.andThen(null));

    final DoubleArrayList l2 = new DoubleArrayList();
    final FloatConsumer f2 = v -> l2.add(-v);

    final FloatConsumer fun = f1.andThen(f2);
    for (final float value : values) {
      fun.accept(value);
    }

    for (int i = 0; i < values.length; i++) {
      final float value = values[i];
      Assertions.assertEquals(value, l1.getDouble(i));
      Assertions.assertEquals(-value, l2.getDouble(i));
    }
  }
}
