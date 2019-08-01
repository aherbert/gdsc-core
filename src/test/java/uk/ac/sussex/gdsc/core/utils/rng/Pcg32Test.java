package uk.ac.sussex.gdsc.core.utils.rng;

import uk.ac.sussex.gdsc.test.api.TestHelper;
import uk.ac.sussex.gdsc.test.api.function.IntIntBiPredicate;
import uk.ac.sussex.gdsc.test.junit5.RandomSeed;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;

import org.apache.commons.rng.RandomProviderState;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.function.UnaryOperator;

@SuppressWarnings("javadoc")
public class Pcg32Test {
  @Test
  public void testNextIntXshRs() {
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
    final Pcg32 rng = Pcg32.xshrs(0x012de1babb3c4104L, 0xc8161b4202294965L);
    assertNextInt(expectedSequence, rng);
  }

  @Test
  public void testNextIntXshRsDefaultInc() {
    /*
     * Tested with respect to pcg_engines::setseq_xsh_rs_64_32 from the C++ implementation. See :
     * http://www.pcg-random.org/download.html#cpp-implementation
     */
    final int[] expectedSequence = {0x5ab2ddd9, 0x215c476c, 0x83c34b11, 0xe2c5e213, 0x37979624,
        0x303cf5b5, 0xbf2a146e, 0xb0692351, 0x49b00de3, 0xd9ded67c, 0x298e2bb9, 0xa20d2287,
        0xa067cd33, 0x5c10d395, 0x1f8d8bd5, 0x4306b6bc, 0x97a3e50b, 0x992e0604, 0x8a982b33,
        0x4baa6604, 0xefd995eb, 0x0f341c29, 0x080bce32, 0xb22b3de2, 0x5fbf47ff, 0x7fc928bf,
        0x075a5871, 0x174a0c48, 0x72458b67, 0xa869a8c1, 0x64857577, 0xed28377c, 0x3ce86b48,
        0xa855af8b, 0x6a051d88, 0x23b06c33, 0xb3e4afc1, 0xa848c3e4, 0x79f969a6, 0x670e2acb,};
    final Pcg32 rng = Pcg32.xshrs(0x012de1babb3c4104L);
    assertNextInt(expectedSequence, rng);
  }

  @Test
  public void testNextIntXshRr() {
    /*
     * Tested with respect to pcg_engines::mcg_xsh_rr_64_32 of the C++ implementation. See :
     * http://www.pcg-random.org/download.html#cpp-implementation
     */
    final int[] expectedSequence = {0xe860dd24, 0x15d339c0, 0xd9f75c46, 0x00efabb7, 0xa625e97f,
        0xcdeae599, 0x6304e667, 0xbc81be11, 0x2b8ea285, 0x8e186699, 0xac552be9, 0xd1ae72e5,
        0x5b953ad4, 0xa061dc1b, 0x526006e7, 0xf5a6c623, 0xfcefea93, 0x3a1964d2, 0xd6f03237,
        0xf3e493f7, 0x0c733750, 0x34a73582, 0xc4f8807b, 0x92b741ca, 0x0d38bf9c, 0xc39ee6ad,
        0xdc24857b, 0x7ba8f7d8, 0x377a2618, 0x92d83d3f, 0xd22a957a, 0xb6724af4, 0xe116141a,
        0xf465fe45, 0xa95f35bb, 0xf0398d4d, 0xe880af3e, 0xc2951dfd, 0x984ec575, 0x8679addb,};
    final Pcg32 rng = Pcg32.xshrr(0x012de1babb3c4104L, 0xc8161b4202294965L);
    assertNextInt(expectedSequence, rng);
  }

  @Test
  public void testNextIntXshRrDefaultInc() {
    /*
     * Tested with respect to pcg_engines::mcg_xsh_rr_64_32 of the C++ implementation. See :
     * http://www.pcg-random.org/download.html#cpp-implementation
     */
    final int[] expectedSequence = {0x0d2d5291, 0x45df90aa, 0xc60f3fb7, 0x06694f16, 0x29563e6f,
        0x42f46063, 0xf2be5583, 0x30360e91, 0x36385531, 0xddd36cd9, 0x5f4a6535, 0x644d10c0,
        0xaca075d7, 0x33781706, 0x4e1f9f34, 0x0676e286, 0xaca5eeb2, 0x7315cc93, 0xa6dfefe2,
        0xd480e065, 0xda9da26f, 0xda0f27b7, 0x045c0844, 0x22acfa0f, 0xcd7ecd75, 0xb97fd692,
        0xac96dd03, 0xf59c7174, 0x488947fe, 0x64a3d543, 0x90963884, 0x4adee0bb, 0x993cf7c0,
        0x8545b3f2, 0x409b542d, 0x6bf0a247, 0xfd59f9b4, 0x8f50b06e, 0x1bbcf6f5, 0xe1fdd29c,};
    final Pcg32 rng = Pcg32.xshrr(0x012de1babb3c4104L);
    assertNextInt(expectedSequence, rng);
  }

  /**
   * Check the expected sequence is output by the generator.
   *
   * @param expectedSequence the expected sequence
   * @param rng the generator
   */
  private static void assertNextInt(int[] expectedSequence, Pcg32 rng) {
    for (int i = 0; i < expectedSequence.length; i++) {
      Assertions.assertEquals(expectedSequence[i], rng.nextInt());
    }
  }

  // All basic RNG methods based on the monobit; tested using XSH-RR

  @SeededTest
  public void testNextInt(RandomSeed randomSeed) {
    final long seed = randomSeed.getSeedAsLong();
    final Pcg32 rng = Pcg32.xshrr(seed);
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
    final Pcg32 rng = Pcg32.xshrr(seed);
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
    final Pcg32 rng = Pcg32.xshrr(seed);
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
    final Pcg32 rng = Pcg32.xshrr(seed);
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
    final Pcg32 rng = Pcg32.xshrr(seed);
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
    final Pcg32 rng = Pcg32.xshrr(seed);
    for (final int range : new int[] {16, 18}) {
      final byte[] bytes = new byte[range];
      int bitCount = 0;
      final int n = 100;
      for (int i = 0; i < n; i++) {
        rng.nextBytes(bytes);
        for (final byte b1 : bytes) {
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

  // No statistical tests for range methods; tested using XSH-RS

  @SeededTest
  public void testNextIntInRange(RandomSeed randomSeed) {
    final long seed = randomSeed.getSeedAsLong();
    final Pcg32 rng = Pcg32.xshrs(seed);
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
    final Pcg32 rng = Pcg32.xshrs(seed);
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
    final Pcg32 rng1 = Pcg32.xshrs(seed);
    final Pcg32 rng2 = Pcg32.xshrs(seed);
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
    final Pcg32 rng1 = Pcg32.xshrs(seed);
    for (final int range : new int[] {1, 10, 32}) {
      final int[] seq1 = new int[range];
      fill(rng1, seq1);
      rng1.advance(-range);
      final int[] seq2 = new int[range];
      fill(rng1, seq2);
      Assertions.assertArrayEquals(seq1, seq2);
    }
  }

  private static void fill(Pcg32 rng, int[] sequence) {
    for (int i = 0; i < sequence.length; i++) {
      sequence[i] = rng.nextInt();
    }
  }

  // Shared methods in the abstract class tested using only one generator

  @SeededTest
  public void testCopyAndJump(RandomSeed randomSeed) {
    final long seed = randomSeed.getSeedAsLong();
    final Pcg32 rng1 = Pcg32.xshrs(seed);
    final Pcg32 rng2 = rng1.copyAndJump();
    Assertions.assertNotSame(rng1, rng2);
    for (int i = 0; i < 10; i++) {
      Assertions.assertNotEquals(rng1.nextInt(), rng2.nextInt());
    }
  }

  @Test
  public void testNextIntUsingZeroThrows() {
    final Pcg32 rng = Pcg32.xshrs(0);
    Assertions.assertThrows(IllegalArgumentException.class, () -> rng.nextInt(0));
  }

  @Test
  public void testNextLongUsingZeroThrows() {
    final Pcg32 rng = Pcg32.xshrs(0);
    Assertions.assertThrows(IllegalArgumentException.class, () -> rng.nextLong(0));
  }

  @SeededTest
  public void testSaveAndRestoreState(RandomSeed randomSeed) {
    final long seed = randomSeed.getSeedAsLong();
    final Pcg32 rng = Pcg32.xshrs(seed);
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
    final Pcg32 rng = Pcg32.xshrs(0);
    final RandomProviderState state = null;
    Assertions.assertThrows(IllegalArgumentException.class, () -> rng.restoreState(state));
  }

  // Copy and Split methods tested per generator

  @SeededTest
  public void testCopyXshRs(RandomSeed randomSeed) {
    final long seed = randomSeed.getSeedAsLong();
    assertDuplicate(Pcg32.xshrs(seed), Pcg32::copy, TestHelper.intsEqual());
  }

  @SeededTest
  public void testCopyXshRr(RandomSeed randomSeed) {
    final long seed = randomSeed.getSeedAsLong();
    assertDuplicate(Pcg32.xshrr(seed), Pcg32::copy, TestHelper.intsEqual());
  }

  @SeededTest
  public void testSplitXshRs(RandomSeed randomSeed) {
    final long seed = randomSeed.getSeedAsLong();
    assertDuplicate(Pcg32.xshrs(seed), Pcg32::split, TestHelper.intsEqual().negate());
  }

  @SeededTest
  public void testSplitXshRr(RandomSeed randomSeed) {
    final long seed = randomSeed.getSeedAsLong();
    assertDuplicate(Pcg32.xshrr(seed), Pcg32::split, TestHelper.intsEqual().negate());
  }

  private static void assertDuplicate(Pcg32 rng1, UnaryOperator<Pcg32> duplicate,
      IntIntBiPredicate test) {
    final Pcg32 rng2 = duplicate.apply(rng1);
    Assertions.assertNotSame(rng1, rng2);
    for (int i = 0; i < 10; i++) {
      test.test(rng1.nextInt(), rng2.nextInt());
    }
  }
}
