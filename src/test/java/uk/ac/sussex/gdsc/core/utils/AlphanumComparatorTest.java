package uk.ac.sussex.gdsc.core.utils;

import uk.ac.sussex.gdsc.test.junit5.RandomSeed;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.rng.RngUtils;

import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

@SuppressWarnings({"javadoc"})
public class AlphanumComparatorTest {
  @Test
  public void canSortStrings() {
    final String first = "aaa";
    final String second = "bb";
    final String[] data = new String[] {second, first};
    Arrays.sort(data, new AlphanumComparator(true));
    Assertions.assertEquals(first, data[0]);
  }

  @Test
  public void canSortStringsWithNumbers() {
    final String first = "a2.txt";
    final String second = "a10.txt";
    final String[] data = new String[] {second, first};
    Arrays.sort(data, new AlphanumComparator(true));
    Assertions.assertEquals(first, data[0]);
  }

  @Test
  public void canSortStringsWithNull() {
    final String first = "a2.txt";
    final String second = null;
    final String[] data = new String[] {second, first};

    // Repeat sort to hit cases of null cmp not-null and not-null cmp null

    Arrays.sort(data, new AlphanumComparator(true));
    Assertions.assertEquals(null, data[0]);
    Arrays.sort(data, new AlphanumComparator(true));
    Assertions.assertEquals(null, data[0]);

    Arrays.sort(data, new AlphanumComparator(false));
    Assertions.assertEquals(null, data[1]);
    Arrays.sort(data, new AlphanumComparator(false));
    Assertions.assertEquals(null, data[1]);
  }

  @Test
  public void canSortStringsWithLeadingZeros() {
    final String first = "a002.txt";
    final String second = "a200.txt";
    final String[] data = new String[] {second, first};
    Arrays.sort(data, new AlphanumComparator(true));
    Assertions.assertEquals(first, data[0]);
  }

  @SeededTest
  public void canSortStringsWithTextAndNumbers(RandomSeed seed) {
    // This hits all the edge cases in the code.
    // The array order as declared is correct.
    final String zero = "0";
    final String[] data = new String[] {null, zero, zero, "1", "2", "a0", "a00", "a000", "a1", "b1",
        "b2", "b03", "b004", "b0005", "b0005aa", "b10", "b0010"};
    final String[] sorted = data.clone();

    // Shuffle
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    for (int i = data.length - 1; i != 0; i--) {
      final int j = rng.nextInt(i + 1);
      final String tmp = data[i];
      data[i] = data[j];
      data[j] = tmp;
    }

    Arrays.sort(data, new AlphanumComparator(true));
    Assertions.assertArrayEquals(sorted, data);

    Arrays.sort(data, new AlphanumComparator(false));
    Assertions.assertEquals(null, data[data.length - 1]);
  }
}
