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

package uk.ac.sussex.gdsc.core.utils;

import java.util.Arrays;
import java.util.Spliterator;
import java.util.function.DoubleConsumer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"javadoc"})
class StreamsTest {

  @Test
  void testFloatStreamThrows() {
    final float[] missing = null;
    Assertions.assertThrows(NullPointerException.class, () -> Streams.stream(missing));
    Assertions.assertThrows(NullPointerException.class, () -> Streams.stream(missing, 0, 1));
    final float[] values = new float[5];
    Assertions.assertThrows(IllegalArgumentException.class, () -> Streams.stream(values, 5, 4));
    Assertions.assertThrows(ArrayIndexOutOfBoundsException.class, () -> Streams.stream(values, 5, 10));
    Assertions.assertThrows(ArrayIndexOutOfBoundsException.class, () -> Streams.stream(values, 0, 10));
    Assertions.assertThrows(ArrayIndexOutOfBoundsException.class, () -> Streams.stream(values, -1, 5));
  }

  @Test
  void testFloatSpliterator() {
    final float[] values = {44, 0, 1};
    final Spliterator.OfDouble s = Streams.stream(values).spliterator();
    Assertions.assertEquals(values.length, s.estimateSize());
    Assertions.assertEquals(Spliterator.SIZED, s.characteristics() & Spliterator.SIZED);
    Assertions.assertEquals(Spliterator.SUBSIZED, s.characteristics() & Spliterator.SUBSIZED);
    Assertions.assertEquals(Spliterator.NONNULL, s.characteristics() & Spliterator.NONNULL);
    Assertions.assertEquals(Spliterator.IMMUTABLE, s.characteristics() & Spliterator.IMMUTABLE);
    final Spliterator.OfDouble s2 = s.trySplit();
    Assertions.assertEquals(Spliterator.SIZED, s2.characteristics() & Spliterator.SIZED);
    Assertions.assertEquals(Spliterator.SUBSIZED, s2.characteristics() & Spliterator.SUBSIZED);
    Assertions.assertEquals(Spliterator.NONNULL, s2.characteristics() & Spliterator.NONNULL);
    Assertions.assertEquals(Spliterator.IMMUTABLE, s2.characteristics() & Spliterator.IMMUTABLE);
    // Should divide s in half.
    // The returned spliterator if the lower half which cannot be further split.
    Assertions.assertNull(s2.trySplit());
    final int[] i = {0};
    Assertions.assertTrue(s2.tryAdvance((double x) -> Assertions.assertEquals(values[i[0]++], x)));
    Assertions.assertFalse(s2.tryAdvance((double x) -> Assertions.fail()));
    Assertions.assertTrue(s.tryAdvance((double x) -> Assertions.assertEquals(values[i[0]++], x)));
    Assertions.assertTrue(s.tryAdvance((double x) -> Assertions.assertEquals(values[i[0]++], x)));
    Assertions.assertFalse(s.tryAdvance((double x) -> Assertions.fail()));
    Assertions.assertThrows(NullPointerException.class, () -> s.tryAdvance((DoubleConsumer) null));
    final Spliterator.OfDouble s3 = Streams.stream(values).spliterator();
    Assertions.assertThrows(NullPointerException.class,
        () -> s3.forEachRemaining((DoubleConsumer) null));
    i[0] = 0;
    s3.forEachRemaining((double x) -> Assertions.assertEquals(values[i[0]++], x));
    s3.forEachRemaining((double x) -> Assertions.fail());
  }

  @Test
  void testFloatStream() {
    final float[] values =
        {1, 2, 2.3f, 5, -1, Float.NaN, Float.POSITIVE_INFINITY, 0, 1, Float.NEGATIVE_INFINITY};
    final double[] expected = SimpleArrayUtils.toDouble(values);
    Assertions.assertArrayEquals(expected, Streams.stream(values).toArray());
    for (int i = 0; i < values.length; i++) {
      for (int j = i + 1; j <= values.length; j++) {
        final double[] a = Streams.stream(values, i, j).toArray();
        Assertions.assertArrayEquals(Arrays.copyOfRange(expected, i, j), a);
      }
    }
  }
}
