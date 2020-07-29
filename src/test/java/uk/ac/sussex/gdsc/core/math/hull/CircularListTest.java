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

package uk.ac.sussex.gdsc.core.math.hull;

import gnu.trove.list.array.TIntArrayList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"javadoc"})
public class CircularListTest {
  @Test
  public void testCircularListSize1() {
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
    final TIntArrayList data = new TIntArrayList(1);
    list.forEach(data::add);
    Assertions.assertArrayEquals(new int[] {value}, data.toArray());
    Assertions.assertFalse(list.advanceTo(value + 1));
    Assertions.assertEquals(value, list.current());
    Assertions.assertTrue(list.advanceTo(value));
    Assertions.assertEquals(value, list.current());
  }

  @Test
  public void testCircularListSize2() {
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
    final TIntArrayList data = new TIntArrayList(2);
    list.forEach(data::add);
    Assertions.assertArrayEquals(new int[] {value2, value1}, data.toArray());
    Assertions.assertFalse(list.advanceTo(value1 + 1));
    Assertions.assertEquals(value2, list.current());
    Assertions.assertTrue(list.advanceTo(value1));
    Assertions.assertEquals(value1, list.current());
    data.clear();
    list.forEach(data::add);
    Assertions.assertArrayEquals(new int[] {value1, value2}, data.toArray());
  }

  @Test
  public void testCircularListSize3() {
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
    final TIntArrayList data = new TIntArrayList(3);
    list.forEach(data::add);
    Assertions.assertArrayEquals(new int[] {value3, value1, value2}, data.toArray());
    Assertions.assertFalse(list.advanceTo(value1 + 1));
    Assertions.assertEquals(value3, list.current());
    Assertions.assertTrue(list.advanceTo(value1));
    Assertions.assertEquals(value1, list.current());
    data.clear();
    list.forEach(data::add);
    Assertions.assertArrayEquals(new int[] {value1, value2, value3}, data.toArray());
  }
}
