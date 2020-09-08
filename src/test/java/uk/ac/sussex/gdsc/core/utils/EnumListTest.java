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

package uk.ac.sussex.gdsc.core.utils;

import java.util.Iterator;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.test.utils.functions.IndexSupplier;

@SuppressWarnings({"javadoc"})
class EnumListTest {

  enum EmptyEnum {
    // Empty
  }

  enum Size1Enum {
    A;
  }

  enum Size2Enum {
    A, B;
  }

  @Test
  void canCreateWithEmptyEnum() {
    final EnumList<EmptyEnum> list = EnumList.forEnum(EmptyEnum.class);
    check(list, EmptyEnum.values());
  }

  @Test
  void canCreateWithSize1Enum() {
    final EnumList<Size1Enum> list = EnumList.forEnum(Size1Enum.class);
    check(list, Size1Enum.values());
  }

  @Test
  void canCreateWithSize2Enum() {
    final EnumList<Size2Enum> list = EnumList.forEnum(Size2Enum.class);
    check(list, Size2Enum.values());
  }

  private static <E extends Enum<E>> void check(EnumList<E> list, E[] values) {
    // Check the size and array of values
    Assertions.assertEquals(values.length, list.size(), "Incorrect size");
    Assertions.assertArrayEquals(values, list.toArray(), "Incorrect array values");
    for (final E element : values) {
      Assertions.assertEquals(element, list.get(element.ordinal()),
          "Not correct element for ordinal");
    }

    // get
    Assertions.assertThrows(IllegalArgumentException.class, () -> list.get(-1),
        "Should throw with get(-1)");
    Assertions.assertThrows(IllegalArgumentException.class, () -> list.get(values.length),
        "Should throw with get(values.length)");

    // getOrDefault
    E defaultValue = (values.length == 0) ? null : values[values.length - 1];
    Assertions.assertEquals(defaultValue, list.getOrDefault(-1, defaultValue),
        "Should get default with getOrDefault(-1)");
    Assertions.assertEquals(defaultValue, list.getOrDefault(values.length, defaultValue),
        "Should get default with getOrDefault(values.length)");
    if (values.length != 0) {
      Assertions.assertEquals(values[0], list.getOrDefault(0, null),
          "Should get first with getOrDefault(0)");
      Assertions.assertEquals(values[values.length - 1], list.getOrDefault(values.length - 1, null),
          "Should get last with getOrDefault(values.length - 1)");
    }

    // getOrFirst
    defaultValue = (values.length == 0) ? null : values[0];
    Assertions.assertEquals(defaultValue, list.getOrFirst(-1),
        "Should get first with getOrFirst(-1)");
    Assertions.assertEquals(defaultValue, list.getOrFirst(values.length),
        "Should get first with getOrFirst(values.length)");
    if (values.length != 0) {
      Assertions.assertEquals(values[0], list.getOrFirst(0), "Should get first with getOrFirst(0)");
      Assertions.assertEquals(values[values.length - 1], list.getOrFirst(values.length - 1),
          "Should get last with getOrFirst(values.length - 1)");
    }

    if (defaultValue != null) {
      final EnumList<E> list2 = EnumList.forEnum(defaultValue);
      Assertions.assertArrayEquals(list.toArray(), list2.toArray(),
          "Incorrect list from an enum value");
    }
  }

  @Test
  void canIterate() {
    final EnumList<Size2Enum> list = EnumList.forEnum(Size2Enum.class);
    final Size2Enum[] values = Size2Enum.values();
    int index = 0;
    final IndexSupplier msg = new IndexSupplier(1, "Incorrect element ", null);
    for (final Size2Enum element : list) {
      Assertions.assertEquals(values[index], element, msg.set(0, index++));
    }

    final Iterator<Size2Enum> itr = list.iterator();
    for (int i = 0; i < list.size(); i++) {
      itr.next();
    }

    Assertions.assertThrows(NoSuchElementException.class, () -> itr.next(),
        "Should not iterate past the total elements");

    Assertions.assertThrows(UnsupportedOperationException.class, () -> itr.remove(),
        "Should not support remove()");
  }
}
