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
 * Copyright (C) 2011 - 2023 Alex Herbert
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

package uk.ac.sussex.gdsc.core.math.hull;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import java.util.function.IntConsumer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"javadoc"})
class CircularListTest {
  @Test
  void testCircularListSize1() {
    final int value = 42;
    final CircularList list = new CircularList(value);
    Assertions.assertEquals(1, list.size());
    Assertions.assertEquals(value, list.current());
    Assertions.assertEquals(value, list.peek(0));
    Assertions.assertEquals(value, list.peek(-1));
    Assertions.assertEquals(value, list.peek(-2));
    Assertions.assertEquals(value, list.peek(1));
    Assertions.assertEquals(value, list.peek(2));
    Assertions.assertEquals(value, list.next());
    Assertions.assertEquals(value, list.previous());
    final IntArrayList data = new IntArrayList(1);
    list.forEach((IntConsumer) data::add);
    Assertions.assertArrayEquals(new int[] {value}, data.toIntArray());
    Assertions.assertFalse(list.advanceTo(value + 1));
    Assertions.assertEquals(value, list.current());
    Assertions.assertTrue(list.advanceTo(value));
    Assertions.assertEquals(value, list.current());
    data.clear();
    final IntArrayList data2 = new IntArrayList(1);
    list.forEach((i, j) -> {
      data.add(i);
      data2.add(j);
    });
    Assertions.assertArrayEquals(new int[] {value}, data.toIntArray());
    Assertions.assertArrayEquals(new int[] {value}, data2.toIntArray());

    list.reset();
    Assertions.assertEquals(value, list.current());
    list.mark();
    list.reset();
    Assertions.assertEquals(value, list.current());
  }

  @Test
  void testCircularListSize2() {
    final int value1 = 42;
    final int value2 = 99;
    final CircularList list = new CircularList(value1);
    list.insertAfter(value2);
    Assertions.assertEquals(2, list.size());
    Assertions.assertEquals(value2, list.current());
    Assertions.assertEquals(value2, list.peek(0));
    Assertions.assertEquals(value1, list.peek(-1));
    Assertions.assertEquals(value2, list.peek(-2));
    Assertions.assertEquals(value1, list.peek(1));
    Assertions.assertEquals(value2, list.peek(2));
    Assertions.assertEquals(value1, list.next());
    Assertions.assertEquals(value2, list.next());
    Assertions.assertEquals(value1, list.previous());
    Assertions.assertEquals(value2, list.previous());
    final IntArrayList data = new IntArrayList(2);
    list.forEach((IntConsumer) data::add);
    Assertions.assertArrayEquals(new int[] {value2, value1}, data.toIntArray());
    Assertions.assertFalse(list.advanceTo(value1 + 1));
    Assertions.assertEquals(value2, list.current());
    Assertions.assertTrue(list.advanceTo(value1));
    Assertions.assertEquals(value1, list.current());
    data.clear();
    list.forEach((IntConsumer) data::add);
    Assertions.assertArrayEquals(new int[] {value1, value2}, data.toIntArray());
    data.clear();
    final IntArrayList data2 = new IntArrayList(2);
    list.forEach((i, j) -> {
      data.add(i);
      data2.add(j);
    });
    Assertions.assertArrayEquals(new int[] {value1, value2}, data.toIntArray());
    Assertions.assertArrayEquals(new int[] {value2, value1}, data2.toIntArray());

    list.reset();
    Assertions.assertEquals(value1, list.current());
    list.mark();
    list.reset();
    Assertions.assertEquals(value1, list.current());
    list.next();
    list.mark();
    list.next();
    Assertions.assertEquals(value1, list.current());
    list.reset();
    Assertions.assertEquals(value2, list.current());
  }

  @Test
  void testCircularListSize3() {
    final int value1 = 42;
    final int value2 = 99;
    final int value3 = 123;
    final CircularList list = new CircularList(value1);
    list.insertAfter(value2);
    list.insertAfter(value3);
    Assertions.assertEquals(3, list.size());
    Assertions.assertEquals(value3, list.current());
    Assertions.assertEquals(value3, list.peek(0));
    Assertions.assertEquals(value2, list.peek(-1));
    Assertions.assertEquals(value1, list.peek(-2));
    Assertions.assertEquals(value3, list.peek(-3));
    Assertions.assertEquals(value1, list.peek(1));
    Assertions.assertEquals(value2, list.peek(2));
    Assertions.assertEquals(value3, list.peek(3));
    Assertions.assertEquals(value1, list.next());
    Assertions.assertEquals(value2, list.next());
    Assertions.assertEquals(value3, list.next());
    Assertions.assertEquals(value2, list.previous());
    Assertions.assertEquals(value1, list.previous());
    Assertions.assertEquals(value3, list.previous());
    final IntArrayList data = new IntArrayList(3);
    list.forEach((IntConsumer) data::add);
    Assertions.assertArrayEquals(new int[] {value3, value1, value2}, data.toIntArray());
    Assertions.assertFalse(list.advanceTo(value1 + 1));
    Assertions.assertEquals(value3, list.current());
    Assertions.assertTrue(list.advanceTo(value1));
    Assertions.assertEquals(value1, list.current());
    data.clear();
    list.forEach((IntConsumer) data::add);
    Assertions.assertArrayEquals(new int[] {value1, value2, value3}, data.toIntArray());
    data.clear();
    final IntArrayList data2 = new IntArrayList(3);
    list.forEach((i, j) -> {
      data.add(i);
      data2.add(j);
    });
    Assertions.assertArrayEquals(new int[] {value1, value2, value3}, data.toIntArray());
    Assertions.assertArrayEquals(new int[] {value2, value3, value1}, data2.toIntArray());

    list.reset();
    Assertions.assertEquals(value1, list.current());
    list.mark();
    list.reset();
    Assertions.assertEquals(value1, list.current());
    list.next();
    list.mark();
    list.next();
    Assertions.assertEquals(value3, list.current());
    list.reset();
    Assertions.assertEquals(value2, list.current());
  }
}
