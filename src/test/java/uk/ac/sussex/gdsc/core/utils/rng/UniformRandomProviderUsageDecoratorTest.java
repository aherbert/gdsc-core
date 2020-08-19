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

import java.math.BigInteger;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.ToLongFunction;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.sampling.distribution.DiscreteSampler;
import org.apache.commons.rng.sampling.distribution.LargeMeanPoissonSampler;
import org.apache.commons.rng.sampling.distribution.NormalizedGaussianSampler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.core.utils.rng.UniformRandomProviderUsageDecorator.SizeCounter;

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
    final SizeCounter count = new SizeCounter();
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
  public void testSizeCounterAdd() {
    final SizeCounter count1 = new SizeCounter();
    final SizeCounter count2 = new SizeCounter();

    count1.addUnsigned(-2L);
    count2.addUnsigned(-1L);

    count1.add(count2);

    Assertions.assertEquals(BigInteger.valueOf(1).shiftLeft(64).subtract(BigInteger.ONE)
        .multiply(BigInteger.valueOf(2)).subtract(BigInteger.ONE), count1.value());
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

  @Test
  public void testAdd() {
    final UniformRandomProviderUsageDecorator rng1 =
        new UniformRandomProviderUsageDecorator(SplitMix.new64(0));
    final UniformRandomProviderUsageDecorator rng2 =
        new UniformRandomProviderUsageDecorator(SplitMix.new64(0));
    final byte[] bytes = new byte[4];
    IntStream.range(0, 1).forEach(i -> rng1.nextBytes(bytes));
    IntStream.range(0, 2).forEach(i -> rng1.nextBytes(bytes, 0, bytes.length));
    IntStream.range(0, 3).forEach(i -> rng1.nextInt());
    IntStream.range(0, 4).forEach(i -> rng1.nextInt(23));
    IntStream.range(0, 5).forEach(i -> rng1.nextLong());
    IntStream.range(0, 6).forEach(i -> rng1.nextLong(23));
    IntStream.range(0, 7).forEach(i -> rng1.nextBoolean());
    IntStream.range(0, 8).forEach(i -> rng1.nextFloat());
    IntStream.range(0, 9).forEach(i -> rng1.nextDouble());
    IntStream.range(0, 9).forEach(i -> rng2.nextBytes(bytes));
    IntStream.range(0, 8).forEach(i -> rng2.nextBytes(bytes, 0, bytes.length));
    IntStream.range(0, 7).forEach(i -> rng2.nextInt());
    IntStream.range(0, 6).forEach(i -> rng2.nextInt(23));
    IntStream.range(0, 5).forEach(i -> rng2.nextLong());
    IntStream.range(0, 4).forEach(i -> rng2.nextLong(23));
    IntStream.range(0, 3).forEach(i -> rng2.nextBoolean());
    IntStream.range(0, 2).forEach(i -> rng2.nextFloat());
    IntStream.range(0, 1).forEach(i -> rng2.nextDouble());
    rng1.add(rng2);
    Assertions.assertEquals(10, rng1.getNextBytesCount());
    Assertions.assertEquals(10, rng1.getNextBytesRangeCount());
    Assertions.assertEquals(10, rng1.getNextIntCount());
    Assertions.assertEquals(10, rng1.getNextIntRangeCount());
    Assertions.assertEquals(10, rng1.getNextLongCount());
    Assertions.assertEquals(10, rng1.getNextLongRangeCount());
    Assertions.assertEquals(10, rng1.getNextBooleanCount());
    Assertions.assertEquals(10, rng1.getNextFloatCount());
    Assertions.assertEquals(10, rng1.getNextDoubleCount());
    Assertions.assertEquals(bytes.length * 10, rng1.getNextBytesSizeAsLong());
    Assertions.assertEquals(bytes.length * 10, rng1.getNextBytesRangeSizeAsLong());
  }

  @Test
  @Disabled("This is for testing and contains no assertions")
  public void testSampling() {
    final UniformRandomProviderUsageDecorator rng =
        new UniformRandomProviderUsageDecorator(SplitMix.new64(0));
    final Logger logger = Logger.getLogger(getClass().getName());

    final NormalizedGaussianSampler sampler = SamplerUtils.createNormalizedGaussianSampler(rng);
    final int size = 1 << 20;
    for (int l = size; l-- > 0;) {
      sampler.sample();
    }
    logger.info(rng.toString());
    final long total = rng.getNextDoubleCount() + rng.getNextLongCount();
    logger.info(() -> String.format("%d / %d = %s", rng.getNextDoubleCount(), total,
        rng.getNextDoubleCount() / (double) total));

    for (final int mean : new int[] {50, 100, 1000}) {
      rng.reset();
      final DiscreteSampler sampler2 = new LargeMeanPoissonSampler(rng, mean);
      for (int l = size; l-- > 0;) {
        sampler2.sample();
      }
      logger.info(rng.toString());
      final long total2 = rng.getNextDoubleCount() + rng.getNextLongCount();
      logger.info(() -> String.format("%d / %d = %s", rng.getNextDoubleCount(), total2,
          rng.getNextDoubleCount() / (double) total2));
    }
  }
}
