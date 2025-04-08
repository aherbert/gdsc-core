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

package uk.ac.sussex.gdsc.core.utils;

import java.util.Arrays;
import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.rng.RngFactory;
import uk.ac.sussex.gdsc.test.utils.RandomSeed;

@SuppressWarnings({"javadoc"})
class AlphaNumericComparatorTest {
  @Test
  void canSortStrings() {
    final String first = "aaa";
    final String second = "bb";
    final String[] data = new String[] {second, first};
    Arrays.sort(data, AlphaNumericComparator.NULL_IS_LESS_INSTANCE);
    Assertions.assertEquals(first, data[0]);
  }

  @Test
  void canSortStringsWithNumbers() {
    final String first = "a2.txt";
    final String second = "a10.txt";
    final String[] data = new String[] {second, first};
    Arrays.sort(data, AlphaNumericComparator.NULL_IS_LESS_INSTANCE);
    Assertions.assertEquals(first, data[0]);
  }

  @Test
  void canSortStringsWithNull() {
    final String first = "a2.txt";
    final String second = null;
    final String[] data = new String[] {second, first};

    // Repeat sort to hit cases of null cmp not-null and not-null cmp null

    Arrays.sort(data, AlphaNumericComparator.NULL_IS_LESS_INSTANCE);
    Assertions.assertEquals(null, data[0]);
    Arrays.sort(data, AlphaNumericComparator.NULL_IS_LESS_INSTANCE);
    Assertions.assertEquals(null, data[0]);

    Arrays.sort(data, AlphaNumericComparator.NULL_IS_MORE_INSTANCE);
    Assertions.assertEquals(null, data[1]);
    Arrays.sort(data, AlphaNumericComparator.NULL_IS_MORE_INSTANCE);
    Assertions.assertEquals(null, data[1]);
  }

  @Test
  void canSortStringsWithLeadingZeros() {
    final String first = "a002.txt";
    final String second = "a20.txt";
    final String[] data = new String[] {second, first};
    Arrays.sort(data, AlphaNumericComparator.NULL_IS_LESS_INSTANCE);
    Assertions.assertEquals(first, data[0]);
  }

  @SeededTest
  void canSortStringsWithTextAndNumbers(RandomSeed seed) {
    // This hits all the edge cases in the code.
    // The array order as declared is correct.
    final String zero = "0";
    final String[] data = new String[] {null, zero, zero, "1", "2", "a0", "a00", "a000", "a1", "b1",
        "b2", "b03", "b004", "b0005", "b0005aa", "b10", "b0010"};
    final String[] sorted = data.clone();

    // Shuffle
    final UniformRandomProvider rng = RngFactory.create(seed.get());
    for (int i = data.length - 1; i != 0; i--) {
      final int j = rng.nextInt(i + 1);
      final String tmp = data[i];
      data[i] = data[j];
      data[j] = tmp;
    }

    Arrays.sort(data, AlphaNumericComparator.NULL_IS_LESS_INSTANCE);
    Assertions.assertArrayEquals(sorted, data);

    Arrays.sort(data, AlphaNumericComparator.NULL_IS_MORE_INSTANCE);
    Assertions.assertEquals(null, data[data.length - 1]);
  }
}
