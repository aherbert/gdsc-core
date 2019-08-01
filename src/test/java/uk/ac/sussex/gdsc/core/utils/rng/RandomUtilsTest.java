package uk.ac.sussex.gdsc.core.utils.rng;

import uk.ac.sussex.gdsc.core.utils.SimpleArrayUtils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.BitSet;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

@SuppressWarnings("javadoc")
public class RandomUtilsTest {
  @Test
  public void testShuffleDouble() {
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
  public void testShuffleFloat() {
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
  public void testShuffleInt() {
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
  public void testShuffleObject() {
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
  public void testSample() {
    final SplitMix rng = SplitMix.new64(0);
    final int length = 15;
    final int lower = 10;
    final int upper = lower + length;
    final int[] data = SimpleArrayUtils.newArray(length, 10, 1);
    final int k = 3;
    for (int i = 0; i < 10; i++) {
      final int[] sample = RandomUtils.sample(k, data, rng);
      BitSet set = new BitSet(upper);
      for (int value : sample) {
        Assertions.assertTrue(value >= lower && value < upper);
        set.set(value);
      }
      Assertions.assertEquals(k, set.cardinality());
    }
  }
}
