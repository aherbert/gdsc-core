package uk.ac.sussex.gdsc.core.utils;

import uk.ac.sussex.gdsc.core.utils.EnumList;
import uk.ac.sussex.gdsc.test.utils.functions.IndexSupplier;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.NoSuchElementException;

@SuppressWarnings({"javadoc"})
public class EnumListTest {

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
  public void canCreateWithEmptyEnum() {
    final EnumList<EmptyEnum> list = EnumList.forEnum(EmptyEnum.class);
    check(list, EmptyEnum.values());
  }

  @Test
  public void canCreateWithSize1Enum() {
    final EnumList<Size1Enum> list = EnumList.forEnum(Size1Enum.class);
    check(list, Size1Enum.values());
  }

  @Test
  public void canCreateWithSize2Enum() {
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
        "Should get default with get(-1)");
    Assertions.assertEquals(defaultValue, list.getOrDefault(values.length, defaultValue),
        "Should get default with get(values.length)");

    // getOrFirst
    defaultValue = (values.length == 0) ? null : values[0];
    Assertions.assertEquals(defaultValue, list.getOrFirst(-1), "Should get first with get(-1)");
    Assertions.assertEquals(defaultValue, list.getOrFirst(values.length),
        "Should get first with get(values.length)");

    if (defaultValue != null) {
      final EnumList<E> list2 = EnumList.forEnum(defaultValue);
      Assertions.assertArrayEquals(list.toArray(), list2.toArray(),
          "Incorrect list from an enum value");
    }
  }

  @Test
  public void canIterate() {
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
