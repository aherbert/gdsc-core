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

package uk.ac.sussex.gdsc.core.utils.rng;

import java.util.BitSet;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.core.utils.SimpleArrayUtils;

@SuppressWarnings("javadoc")
class RandomUtilsTest {
  @Test
  void testShuffleDouble() {
    final SplitMix rng = SplitMix.new64(0);
    final int length = 15;
    final double[] data = SimpleArrayUtils.newArray(length, 0.0, 1.0);
    RandomUtils.shuffle(data, rng);
    final boolean[] seen = new boolean[length];
    boolean moved = false;
    for (int i = 0; i < length; i++) {
      final int value = (int) data[i];
      Assertions.assertFalse(seen[value]);
      seen[value] = true;
      moved = moved || value != i;
    }
    Assertions.assertTrue(moved, "Nothing moved");
  }

  @Test
  void testShuffleFloat() {
    final SplitMix rng = SplitMix.new64(0);
    final int length = 15;
    final float[] data = SimpleArrayUtils.newArray(length, 0.0f, 1.0f);
    RandomUtils.shuffle(data, rng);
    final boolean[] seen = new boolean[length];
    boolean moved = false;
    for (int i = 0; i < length; i++) {
      final int value = (int) data[i];
      Assertions.assertFalse(seen[value]);
      seen[value] = true;
      moved = moved || value != i;
    }
    Assertions.assertTrue(moved, "Nothing moved");
  }

  @Test
  void testShuffleInt() {
    final SplitMix rng = SplitMix.new64(0);
    final int length = 15;
    final int[] data = SimpleArrayUtils.natural(length);
    RandomUtils.shuffle(data, rng);
    final boolean[] seen = new boolean[length];
    boolean moved = false;
    for (int i = 0; i < length; i++) {
      final int value = data[i];
      Assertions.assertFalse(seen[value]);
      seen[value] = true;
      moved = moved || value != i;
    }
    Assertions.assertTrue(moved, "Nothing moved");
  }

  @Test
  void testShuffleObject() {
    final SplitMix rng = SplitMix.new64(0);
    final int length = 15;
    final Integer[] data = IntStream.range(0, length).boxed().toArray(Integer[]::new);
    RandomUtils.shuffle(data, rng);
    final boolean[] seen = new boolean[length];
    boolean moved = false;
    for (int i = 0; i < length; i++) {
      final int value = data[i];
      Assertions.assertFalse(seen[value]);
      seen[value] = true;
      moved = moved || value != i;
    }
    Assertions.assertTrue(moved, "Nothing moved");
  }

  @Test
  void testSample() {
    final SplitMix rng = SplitMix.new64(0);
    final int length = 15;
    final int lower = 10;
    final int upper = lower + length;
    final int[] data = SimpleArrayUtils.newArray(length, 10, 1);
    final int k = 3;
    for (int i = 0; i < 10; i++) {
      final int[] sample = RandomUtils.sample(k, data, rng);
      final BitSet set = new BitSet(upper);
      for (final int value : sample) {
        Assertions.assertTrue(value >= lower && value < upper);
        set.set(value);
      }
      Assertions.assertEquals(k, set.cardinality());
    }
  }
}
