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
 * Copyright (C) 2011 - 2021 Alex Herbert
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

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well1024a;
import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"javadoc"})
class UniformRandomProviderAdapterTest {

  @Test
  void testConstructorThrows() {
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> new UniformRandomProviderAdapter(null));
  }

  @Test
  void testGenerator() {
    final int seed = 123;
    final RandomGenerator r1 = new Well1024a(seed);
    final UniformRandomProvider r2 = new UniformRandomProviderAdapter(new Well1024a(seed));
    final byte[] b1 = new byte[17];
    final byte[] b2 = new byte[17];
    for (int i = 0; i < 10; i++) {
      r1.nextBytes(b1);
      r2.nextBytes(b2);
      Assertions.assertArrayEquals(b1, b2);
      Assertions.assertEquals(r1.nextInt(), r2.nextInt());
      Assertions.assertEquals(r1.nextInt(42), r2.nextInt(42));
      Assertions.assertEquals(r1.nextLong(), r2.nextLong());
      Assertions.assertEquals(r1.nextBoolean(), r2.nextBoolean());
      Assertions.assertEquals(r1.nextFloat(), r2.nextFloat());
      Assertions.assertEquals(r1.nextDouble(), r2.nextDouble());
    }
  }

  @Test
  void testNextLong() {
    final UniformRandomProvider rng = new UniformRandomProviderAdapter(new Well1024a(567));
    Assertions.assertThrows(IllegalArgumentException.class, () -> rng.nextLong(-1));
    // 1/4 of the time the sample is rejected
    final long limit = (1L << 61) * 3;
    for (int i = 0; i < 100; i++) {
      final long value = rng.nextLong(limit);
      Assertions.assertTrue(value >= 0);
      Assertions.assertTrue(value < limit);
    }
  }

  @Test
  void testNextBytesFullRange() {
    assertNextBytes(0, 17);
  }

  @Test
  void testNextBytesPartialRange() {
    assertNextBytes(0, 12);
    assertNextBytes(3, 12);
    assertNextBytes(3, 14);
  }

  private static void assertNextBytes(int start, int len) {
    final UniformRandomProvider rng = new UniformRandomProviderAdapter(new Well1024a(567));
    final byte[] b1 = new byte[17];
    final byte[] all = new byte[17];
    for (int i = 0; i < 100; i++) {
      rng.nextBytes(b1, start, len);
      for (int j = start; j < start + len; j++) {
        all[j] |= b1[j];
      }
    }
    for (int j = 0; j < start; j++) {
      Assertions.assertEquals(0, all[j]);
    }
    for (int j = start; j < start + len; j++) {
      Assertions.assertTrue(all[j] != 0);
    }
    for (int j = start + len; j < all.length; j++) {
      Assertions.assertEquals(0, all[j]);
    }
  }
}
