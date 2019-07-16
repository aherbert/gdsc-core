package uk.ac.sussex.gdsc.core.utils.rng;

import uk.ac.sussex.gdsc.test.junit5.RandomSeed;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;

import org.apache.commons.rng.RandomProviderState;
import org.apache.commons.rng.core.source64.SplitMix64;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.SplittableRandom;

@SuppressWarnings("javadoc")
public class SplitMixTest {
  @SeededTest
  public void testNextInt(RandomSeed randomSeed) {
    final long seed = randomSeed.getSeedAsLong();
    final SplittableRandom sr = new SplittableRandom(seed);
    final SplitMix sm = new SplitMix(seed);
    for (int i = 0; i < 10; i++) {
      Assertions.assertEquals(sr.nextInt(), sm.nextInt());
    }
  }

  @SeededTest
  public void testNextLong(RandomSeed randomSeed) {
    final long seed = randomSeed.getSeedAsLong();
    final SplittableRandom sr = new SplittableRandom(seed);
    final SplitMix sm = new SplitMix(seed);
    for (int i = 0; i < 10; i++) {
      Assertions.assertEquals(sr.nextLong(), sm.nextLong());
    }
  }

  @SeededTest
  public void testNextDouble(RandomSeed randomSeed) {
    final long seed = randomSeed.getSeedAsLong();
    final SplittableRandom sr = new SplittableRandom(seed);
    final SplitMix sm = new SplitMix(seed);
    for (int i = 0; i < 10; i++) {
      Assertions.assertEquals(sr.nextDouble(), sm.nextDouble());
    }
  }

  @SeededTest
  public void testNextBoolean(RandomSeed randomSeed) {
    final long seed = randomSeed.getSeedAsLong();
    final SplittableRandom sr = new SplittableRandom(seed);
    final SplitMix sm = new SplitMix(seed);
    for (int i = 0; i < 10; i++) {
      Assertions.assertEquals(sr.nextBoolean(), sm.nextBoolean());
    }
  }

  @SeededTest
  public void testNextIntInRange(RandomSeed randomSeed) {
    final long seed = randomSeed.getSeedAsLong();
    final SplittableRandom sr = new SplittableRandom(seed);
    final SplitMix sm = new SplitMix(seed);
    // A power of 2 and the worst case scenario for the rejection algorithm.
    // Rejection should occur almost 50% of the time so the test should hit all paths.
    for (final int range : new int[] {256, (1 << 30) + 1}) {
      for (int i = 0; i < 10; i++) {
        Assertions.assertEquals(sr.nextInt(range), sm.nextInt(range));
      }
    }
  }

  @SeededTest
  public void testNextLongInRange(RandomSeed randomSeed) {
    final long seed = randomSeed.getSeedAsLong();
    final SplittableRandom sr = new SplittableRandom(seed);
    final SplitMix sm = new SplitMix(seed);
    // A power of 2 and the worst case scenario for the rejection algorithm.
    // Rejection should occur almost 50% of the time so the test should hit all paths.
    for (final long range : new long[] {256, (1L << 62) + 1}) {
      for (int i = 0; i < 10; i++) {
        Assertions.assertEquals(sr.nextLong(range), sm.nextLong(range));
      }
    }
  }

  @SeededTest
  public void testNextFloat(RandomSeed randomSeed) {
    final long seed = randomSeed.getSeedAsLong();
    final SplitMix sm1 = new SplitMix(seed);
    final SplitMix sm2 = new SplitMix(seed);
    for (int i = 0; i < 10; i++) {
      // NumberFactory will be updated in v1.3
      // final float expected = NumberFactory.makeFloat(sm1.nextInt());
      final float expected = (sm1.nextInt() >>> 8) * 0x1.0p-24f;
      Assertions.assertEquals(expected, sm2.nextFloat());
    }
  }

  @SeededTest
  public void testNextBytes(RandomSeed randomSeed) {
    final long seed = randomSeed.getSeedAsLong();
    final SplitMix64 sm1 = new SplitMix64(seed);
    final SplitMix sm2 = new SplitMix(seed);
    for (final int range : new int[] {16, 18}) {
      final byte[] b1 = new byte[range];
      final byte[] b2 = new byte[range];
      for (int i = 0; i < 10; i++) {
        sm1.nextBytes(b1);
        sm2.nextBytes(b2);
        Assertions.assertArrayEquals(b1, b2);
      }
    }
  }

  @SeededTest
  public void testAdvance(RandomSeed randomSeed) {
    final long seed = randomSeed.getSeedAsLong();
    final SplitMix sm1 = new SplitMix(seed);
    final SplitMix sm2 = new SplitMix(seed);
    for (final int range : new int[] {1, 10, 32}) {
      for (int i = 0; i < range; i++) {
        sm1.nextLong();
      }
      sm2.advance(range);
      for (int i = 0; i < 10; i++) {
        Assertions.assertEquals(sm1.nextLong(), sm2.nextLong());
      }
    }
  }

  @SeededTest
  public void testAdvanceBackwards(RandomSeed randomSeed) {
    final long seed = randomSeed.getSeedAsLong();
    final SplitMix sm1 = new SplitMix(seed);
    for (final int range : new int[] {1, 10, 32}) {
      final long[] seq1 = new long[range];
      fill(sm1, seq1);
      sm1.advance(-range);
      final long[] seq2 = new long[range];
      fill(sm1, seq2);
      Assertions.assertArrayEquals(seq1, seq2);
    }
  }

  private static void fill(SplitMix sm, long[] sequence) {
    for (int i = 0; i < sequence.length; i++) {
      sequence[i] = sm.nextLong();
    }
  }

  @SeededTest
  public void testCopy(RandomSeed randomSeed) {
    final long seed = randomSeed.getSeedAsLong();
    final SplitMix sm1 = new SplitMix(seed);
    final SplitMix sm2 = sm1.copy();
    Assertions.assertNotSame(sm1, sm2);
    for (int i = 0; i < 10; i++) {
      Assertions.assertEquals(sm1.nextLong(), sm2.nextLong());
    }
  }

  @SeededTest
  public void testCopyAndJump(RandomSeed randomSeed) {
    final long seed = randomSeed.getSeedAsLong();
    final SplitMix sm1 = new SplitMix(seed);
    final SplitMix sm2 = sm1.copyAndJump();
    Assertions.assertNotSame(sm1, sm2);
    for (int i = 0; i < 10; i++) {
      Assertions.assertNotEquals(sm1.nextLong(), sm2.nextLong());
    }
  }

  @Test
  public void testNextIntUsingZeroThrows() {
    final SplitMix sm = new SplitMix(0);
    Assertions.assertThrows(IllegalArgumentException.class, () -> sm.nextInt(0));
  }

  @Test
  public void testNextLongUsingZeroThrows() {
    final SplitMix sm = new SplitMix(0);
    Assertions.assertThrows(IllegalArgumentException.class, () -> sm.nextLong(0));
  }

  @SeededTest
  public void testGetState(RandomSeed randomSeed) {
    final long seed = randomSeed.getSeedAsLong();
    final SplitMix sm = new SplitMix(seed);
    Assertions.assertEquals(seed, sm.getState());
  }

  @SeededTest
  public void testSaveAndRestoreState(RandomSeed randomSeed) {
    final long seed = randomSeed.getSeedAsLong();
    final SplitMix sm = new SplitMix(seed);
    final RandomProviderState state = sm.saveState();
    final long[] seq1 = new long[10];
    fill(sm, seq1);
    sm.restoreState(state);
    final long[] seq2 = new long[seq1.length];
    fill(sm, seq2);
    Assertions.assertArrayEquals(seq1, seq2);
  }

  @Test
  public void testRsetoreUsingBadStateThrows() {
    final SplitMix sm = new SplitMix(0);
    final RandomProviderState state = null;
    Assertions.assertThrows(IllegalArgumentException.class, () -> sm.restoreState(state));
  }
}
