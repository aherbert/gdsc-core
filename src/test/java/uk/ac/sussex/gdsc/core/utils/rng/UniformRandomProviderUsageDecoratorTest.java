package uk.ac.sussex.gdsc.core.utils.rng;

import uk.ac.sussex.gdsc.core.utils.rng.UniformRandomProviderUsageDecorator.SizeCounter;

import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.ToLongFunction;

@SuppressWarnings("javadoc")
public class UniformRandomProviderUsageDecoratorTest {
  @Test
  public void testSizeCounterToBigInteger() {
    Assertions.assertEquals(BigInteger.ZERO, SizeCounter.toBigInteger(0));
    Assertions.assertEquals(BigInteger.ONE, SizeCounter.toBigInteger(1));
    Assertions.assertEquals(new BigInteger(Long.toUnsignedString(-1)),
        SizeCounter.toBigInteger(-1));
  }

  @Test
  public void testSizeCounterAddUnsigned() {
    SizeCounter count = new SizeCounter();
    Assertions.assertEquals(BigInteger.ZERO, count.value());

    // Maximum number of bits less 2
    count.addUnsigned(-2L);
    Assertions.assertEquals(new BigInteger(Long.toUnsignedString(-2)), count.value());
    // OK
    count.addUnsigned(1);
    // Roll-over
    count.addUnsigned(1);
    Assertions.assertEquals(BigInteger.valueOf(1).shiftLeft(64), count.value());

    count.reset();
    Assertions.assertEquals(BigInteger.ZERO, count.value());
    // Maximum number of bits
    count.addUnsigned(-1L);
    // Maximum number of bits
    count.addUnsigned(-1L);
    Assertions.assertEquals(BigInteger.valueOf(1).shiftLeft(64).subtract(BigInteger.ONE)
        .multiply(BigInteger.valueOf(2)), count.value());
  }

  @Test
  public void testNextBytes() {
    assertBytesInvocations(UniformRandomProviderUsageDecorator::getNextBytesCount,
        UniformRandomProviderUsageDecorator::getNextBytesSize,
        UniformRandomProviderUsageDecorator::getNextBytesSizeAsLong,
        UniformRandomProvider::nextBytes);
  }

  @Test
  public void testNextBytesRange() {
    assertBytesInvocations(UniformRandomProviderUsageDecorator::getNextBytesRangeCount,
        UniformRandomProviderUsageDecorator::getNextBytesRangeSize,
        UniformRandomProviderUsageDecorator::getNextBytesRangeSizeAsLong,
        (rng, bytes) -> rng.nextBytes(bytes, 0, bytes.length));
  }

  private static void assertBytesInvocations(
      ToLongFunction<UniformRandomProviderUsageDecorator> countMethod,
      Function<UniformRandomProviderUsageDecorator, BigInteger> sizeMethod,
      ToLongFunction<UniformRandomProviderUsageDecorator> longSizeMethod,
      BiConsumer<UniformRandomProvider, byte[]> rngMethod) {
    final long seed = 688812301;
    final SplitMix rng1 = SplitMix.new64(seed);
    final UniformRandomProviderUsageDecorator rng2 =
        new UniformRandomProviderUsageDecorator(SplitMix.new64(seed));
    Assertions.assertEquals(0, countMethod.applyAsLong(rng2));
    long size = 0;
    final int loops = 13;
    for (int i = 0; i < loops; i++) {
      final byte[] expected = new byte[i];
      final byte[] actual = new byte[i];
      size += expected.length;
      rngMethod.accept(rng1, expected);
      rngMethod.accept(rng2, actual);
      Assertions.assertArrayEquals(expected, actual);
      Assertions.assertEquals(i + 1, countMethod.applyAsLong(rng2));
      Assertions.assertEquals(BigInteger.valueOf(size), sizeMethod.apply(rng2));
      Assertions.assertEquals(size, longSizeMethod.applyAsLong(rng2));
    }
    final String description = rng2.toString();
    Assertions.assertTrue(description.contains(String.valueOf(loops)));

    rng2.reset();
    Assertions.assertEquals(0, countMethod.applyAsLong(rng2));
    Assertions.assertEquals(BigInteger.ZERO, sizeMethod.apply(rng2));
    Assertions.assertEquals(0, longSizeMethod.applyAsLong(rng2));
  }

  @Test
  public void testNextInt() {
    assertInvocations(UniformRandomProviderUsageDecorator::getNextIntCount,
        UniformRandomProvider::nextInt);
  }

  @Test
  public void testNextIntRange() {
    assertInvocations(UniformRandomProviderUsageDecorator::getNextIntRangeCount,
        rng -> rng.nextInt(678));
  }

  @Test
  public void testNextLong() {
    assertInvocations(UniformRandomProviderUsageDecorator::getNextLongCount,
        UniformRandomProvider::nextLong);
  }

  @Test
  public void testNextLongRange() {
    assertInvocations(UniformRandomProviderUsageDecorator::getNextLongRangeCount,
        rng -> rng.nextLong(133L));
  }


  @Test
  public void testNextBoolean() {
    assertInvocations(UniformRandomProviderUsageDecorator::getNextBooleanCount,
        UniformRandomProvider::nextBoolean);
  }

  @Test
  public void testNextFloat() {
    assertInvocations(UniformRandomProviderUsageDecorator::getNextFloatCount,
        UniformRandomProvider::nextFloat);
  }

  @Test
  public void testNextDouble() {
    assertInvocations(UniformRandomProviderUsageDecorator::getNextDoubleCount,
        UniformRandomProvider::nextDouble);
  }

  private static void assertInvocations(
      ToLongFunction<UniformRandomProviderUsageDecorator> countMethod,
      Function<UniformRandomProvider, Object> rngMethod) {
    final long seed = 688812301;
    final SplitMix rng1 = SplitMix.new64(seed);
    final UniformRandomProviderUsageDecorator rng2 =
        new UniformRandomProviderUsageDecorator(SplitMix.new64(seed));
    Assertions.assertEquals(0, countMethod.applyAsLong(rng2));
    final int loops = 13;
    for (int i = 0; i < loops; i++) {
      final Object expected = rngMethod.apply(rng1);
      final Object actual = rngMethod.apply(rng2);
      Assertions.assertEquals(expected, actual);
      Assertions.assertEquals(i + 1, countMethod.applyAsLong(rng2));
    }
    final String description = rng2.toString();
    Assertions.assertTrue(description.contains(String.valueOf(loops)));

    rng2.reset();
    Assertions.assertEquals(0, countMethod.applyAsLong(rng2));
  }
}
