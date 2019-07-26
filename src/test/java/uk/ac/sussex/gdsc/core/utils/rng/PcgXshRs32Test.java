package uk.ac.sussex.gdsc.core.utils.rng;

import uk.ac.sussex.gdsc.test.junit5.RandomSeed;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;

import org.apache.commons.rng.RandomProviderState;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings("javadoc")
public class PcgXshRs32Test {
  @Test
  public void testNextInt() {
    /*
     * Tested with respect to pcg_engines::setseq_xsh_rs_64_32 from the C++ implementation. See :
     * http://www.pcg-random.org/download.html#cpp-implementation
     */
    final int[] expectedSequence = {0xba4138b8, 0xd329a393, 0x75d68d3f, 0xbb7572ca, 0x7a48d2f2,
        0xcb3c1e37, 0xc1374a97, 0x7c2c5bfa, 0x8a1c8695, 0x30db4fea, 0x95f9a901, 0x72ebfa48,
        0x6a284dbf, 0x0ef11286, 0x37330e11, 0xfeb53893, 0x77e3adda, 0x64dc86bd, 0xc8d762d7,
        0xbf3fb80c, 0x732dfd12, 0x6088e86d, 0xbc4e79e5, 0x56ece5b1, 0xe706ac72, 0xee798018,
        0xef73de74, 0x3de1f966, 0x7a36db53, 0x1e921eb2, 0x55e35484, 0x2577c6f2, 0x0a006e21,
        0x8cb811b7, 0x5f26c916, 0x3990837f, 0x15f2983d, 0x546ccb4a, 0x4eda8716, 0xb8666a25,};
    final PcgXshRs32 rng = new PcgXshRs32(0x012de1babb3c4104L, 0xc8161b4202294965L);
    for (int i = 0; i < expectedSequence.length; i++) {
      Assertions.assertEquals(expectedSequence[i], rng.nextInt());
    }
  }

  // All basic RNG methods based on the monobit test

  @SeededTest
  public void testNextInt(RandomSeed randomSeed) {
    final long seed = randomSeed.getSeedAsLong();
    final PcgXshRs32 rng = new PcgXshRs32(seed);
    int bitCount = 0;
    final int n = 100;
    for (int i = 0; i < n; i++) {
      bitCount += Integer.bitCount(rng.nextInt());
    }
    final int numberOfBits = n * Integer.SIZE;
    assertMonobit(bitCount, numberOfBits);
  }

  @SeededTest
  public void testNextLong(RandomSeed randomSeed) {
    final long seed = randomSeed.getSeedAsLong();
    final PcgXshRs32 rng = new PcgXshRs32(seed);
    int bitCount = 0;
    final int n = 100;
    for (int i = 0; i < n; i++) {
      bitCount += Long.bitCount(rng.nextLong());
    }
    final int numberOfBits = n * Long.SIZE;
    assertMonobit(bitCount, numberOfBits);
  }

  @SeededTest
  public void testNextDouble(RandomSeed randomSeed) {
    final long seed = randomSeed.getSeedAsLong();
    final PcgXshRs32 rng = new PcgXshRs32(seed);
    int bitCount = 0;
    final int n = 100;
    for (int i = 0; i < n; i++) {
      bitCount += Long.bitCount((long) (rng.nextDouble() * (1L << 53)));
    }
    final int numberOfBits = n * 53;
    assertMonobit(bitCount, numberOfBits);
  }

  @SeededTest
  public void testNextBoolean(RandomSeed randomSeed) {
    final long seed = randomSeed.getSeedAsLong();
    final PcgXshRs32 rng = new PcgXshRs32(seed);
    int bitCount = 0;
    final int n = 1000;
    for (int i = 0; i < n; i++) {
      if (rng.nextBoolean()) {
        bitCount++;
      }
    }
    final int numberOfBits = n;
    assertMonobit(bitCount, numberOfBits);
  }

  @SeededTest
  public void testNextFloat(RandomSeed randomSeed) {
    final long seed = randomSeed.getSeedAsLong();
    final PcgXshRs32 rng = new PcgXshRs32(seed);
    int bitCount = 0;
    final int n = 100;
    for (int i = 0; i < n; i++) {
      bitCount += Integer.bitCount((int) (rng.nextFloat() * (1 << 24)));
    }
    final int numberOfBits = n * 24;
    assertMonobit(bitCount, numberOfBits);
  }

  @SeededTest
  public void testNextBytes(RandomSeed randomSeed) {
    final long seed = randomSeed.getSeedAsLong();
    final PcgXshRs32 rng = new PcgXshRs32(seed);
    for (final int range : new int[] {16, 18}) {
      final byte[] bytes = new byte[range];
      int bitCount = 0;
      final int n = 100;
      for (int i = 0; i < n; i++) {
        rng.nextBytes(bytes);
        for (byte b1 : bytes) {
          bitCount += Integer.bitCount(b1 & 0xff);
        }
      }
      final int numberOfBits = n * Byte.SIZE * range;
      assertMonobit(bitCount, numberOfBits);
    }
  }

  /**
   * Assert that the number of 1 bits is approximately 50%. This is based upon a fixed-step "random
   * walk" of +1/-1 from zero.
   *
   * <p>The test is equivalent to the NIST Monobit test with a fixed p-value of 0.0001. The number
   * of bits is recommended to be above 100.</p>
   *
   * @see <A href="https://csrc.nist.gov/publications/detail/sp/800-22/rev-1a/final">Bassham, et al
   *      (2010) NIST SP 800-22: A Statistical Test Suite for Random and Pseudorandom Number
   *      Generators for Cryptographic Applications. Section 2.1.</a>
   *
   * @param bitCount The bit count.
   * @param numberOfBits Number of bits.
   */
  private static void assertMonobit(int bitCount, int numberOfBits) {
    // Convert the bit count into a number of +1/-1 steps.
    final double sum = 2.0 * bitCount - numberOfBits;
    // The reference distribution is Normal with a standard deviation of sqrt(n).
    // Check the absolute position is not too far from the mean of 0 with a fixed
    // p-value of 0.0001 taken from a 2-tailed Normal distribution. Computation of
    // the p-value requires the complimentary error function.
    // The p-value is set to be equal to a 0.01 with 1 allowed re-run.
    // (Re-runs are not configured for this test.)
    final double absSum = Math.abs(sum);
    final double max = Math.sqrt(numberOfBits) * 3.891;
    Assertions.assertTrue(absSum <= max, () -> "Walked too far astray: " + absSum + " > " + max
        + " (test will fail randomly about 1 in 10,000 times)");
  }

  // No statistical tests for range methods

  @SeededTest
  public void testNextIntInRange(RandomSeed randomSeed) {
    final long seed = randomSeed.getSeedAsLong();
    final PcgXshRs32 rng = new PcgXshRs32(seed);
    // A power of 2 and the worst case scenario for the rejection algorithm.
    // Rejection should occur almost 50% of the time so the test should hit all paths.
    for (final int range : new int[] {256, (1 << 30) + 1}) {
      for (int i = 0; i < 10; i++) {
        final int value = rng.nextInt(range);
        Assertions.assertTrue(value >= 0 && value < range);
      }
    }
  }

  @SeededTest
  public void testNextLongInRange(RandomSeed randomSeed) {
    final long seed = randomSeed.getSeedAsLong();
    final PcgXshRs32 rng = new PcgXshRs32(seed);
    // A power of 2 and the worst case scenario for the rejection algorithm.
    // Rejection should occur almost 50% of the time so the test should hit all paths.
    for (final long range : new long[] {256, (1L << 62) + 1}) {
      for (int i = 0; i < 10; i++) {
        final long value = rng.nextLong(range);
        Assertions.assertTrue(value >= 0 && value < range);
      }
    }
  }

  @SeededTest
  public void testAdvance(RandomSeed randomSeed) {
    final long seed = randomSeed.getSeedAsLong();
    final PcgXshRs32 rng1 = new PcgXshRs32(seed);
    final PcgXshRs32 rng2 = new PcgXshRs32(seed);
    for (final int range : new int[] {1, 10, 32}) {
      for (int i = 0; i < range; i++) {
        rng1.nextInt();
      }
      rng2.advance(range);
      for (int i = 0; i < 10; i++) {
        Assertions.assertEquals(rng1.nextInt(), rng2.nextInt());
      }
    }
  }

  @SeededTest
  public void testAdvanceBackwards(RandomSeed randomSeed) {
    final long seed = randomSeed.getSeedAsLong();
    final PcgXshRs32 rng1 = new PcgXshRs32(seed);
    for (final int range : new int[] {1, 10, 32}) {
      final int[] seq1 = new int[range];
      fill(rng1, seq1);
      rng1.advance(-range);
      final int[] seq2 = new int[range];
      fill(rng1, seq2);
      Assertions.assertArrayEquals(seq1, seq2);
    }
  }

  private static void fill(PcgXshRs32 rng, int[] sequence) {
    for (int i = 0; i < sequence.length; i++) {
      sequence[i] = rng.nextInt();
    }
  }

  @SeededTest
  public void testCopy(RandomSeed randomSeed) {
    final long seed = randomSeed.getSeedAsLong();
    final PcgXshRs32 rng1 = new PcgXshRs32(seed);
    final PcgXshRs32 rng2 = rng1.copy();
    Assertions.assertNotSame(rng1, rng2);
    for (int i = 0; i < 10; i++) {
      Assertions.assertEquals(rng1.nextInt(), rng2.nextInt());
    }
  }

  @SeededTest
  public void testCopyAndJump(RandomSeed randomSeed) {
    final long seed = randomSeed.getSeedAsLong();
    final PcgXshRs32 rng1 = new PcgXshRs32(seed);
    final PcgXshRs32 rng2 = rng1.copyAndJump();
    Assertions.assertNotSame(rng1, rng2);
    for (int i = 0; i < 10; i++) {
      Assertions.assertNotEquals(rng1.nextInt(), rng2.nextInt());
    }
  }

  @SeededTest
  public void testSplit(RandomSeed randomSeed) {
    final long seed = randomSeed.getSeedAsLong();
    final PcgXshRs32 rng1 = new PcgXshRs32(seed);
    final PcgXshRs32 rng2 = rng1.split();
    Assertions.assertNotSame(rng1, rng2);
    for (int i = 0; i < 10; i++) {
      Assertions.assertNotEquals(rng1.nextInt(), rng2.nextInt());
    }
  }

  @Test
  public void testNextIntUsingZeroThrows() {
    final PcgXshRs32 rng = new PcgXshRs32(0);
    Assertions.assertThrows(IllegalArgumentException.class, () -> rng.nextInt(0));
  }

  @Test
  public void testNextLongUsingZeroThrows() {
    final PcgXshRs32 rng = new PcgXshRs32(0);
    Assertions.assertThrows(IllegalArgumentException.class, () -> rng.nextLong(0));
  }

  @SeededTest
  public void testSaveAndRestoreState(RandomSeed randomSeed) {
    final long seed = randomSeed.getSeedAsLong();
    final PcgXshRs32 rng = new PcgXshRs32(seed);
    final RandomProviderState state = rng.saveState();
    final int[] seq1 = new int[10];
    fill(rng, seq1);
    rng.restoreState(state);
    final int[] seq2 = new int[seq1.length];
    fill(rng, seq2);
    Assertions.assertArrayEquals(seq1, seq2);
  }

  @Test
  public void testRestoreUsingBadStateThrows() {
    final PcgXshRs32 rng = new PcgXshRs32(0);
    final RandomProviderState state = null;
    Assertions.assertThrows(IllegalArgumentException.class, () -> rng.restoreState(state));
  }
}
