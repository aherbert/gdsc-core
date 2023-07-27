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
 * Copyright (C) 2011 - 2023 Alex Herbert
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

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.stream.Stream;
import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.rng.RngFactory;
import uk.ac.sussex.gdsc.test.utils.RandomSeed;

@SuppressWarnings({"javadoc"})
class SeedTest {
  @Test
  void nullArgumentThrows() {
    Assertions.assertThrows(NullPointerException.class, () -> Seed.from((byte[]) null));
    Assertions.assertThrows(NullPointerException.class, () -> Seed.from((String) null));
  }

  @ParameterizedTest
  @ValueSource(longs = {0, 1, -1, 1253717123, 192384179239871L})
  void testLong(long value) {
    final Seed seed = Seed.from(value);
    Assertions.assertEquals(value, seed.toLong());
    Assertions.assertEquals(value, Seed.from(seed.toString()).toLong());
    Assertions.assertEquals(value, Seed.from(seed.toBytes()).toLong());
  }

  @ParameterizedTest
  @MethodSource
  void testBytes(byte[] value) {
    final Seed seed = Seed.from(value);
    Assertions.assertArrayEquals(value, seed.toBytes());
    Assertions.assertArrayEquals(value, Seed.from(seed.toString()).toBytes());
    if (value.length == 8) {
      Assertions.assertArrayEquals(value, Seed.from(seed.toLong()).toBytes());
    }
  }

  static Stream<byte[]> testBytes() {
    final UniformRandomProvider rng = RngFactory.createWithFixedSeed();
    final Stream.Builder<byte[]> builder = Stream.builder();
    for (int i = 0; i < 16; i++) {
      final byte[] bytes = new byte[i];
      rng.nextBytes(bytes);
      builder.add(bytes);
    }
    return builder.build();
  }

  @Test
  void testBytesToLong() {
    final byte[] b0 = {};
    final byte[] b1 = {0};
    final byte[] b2 = {1};
    Assertions.assertEquals(0, Seed.from(b0).toLong());
    Assertions.assertEquals(0, Seed.from(b1).toLong());
    Assertions.assertEquals(ByteBuffer.wrap(Arrays.copyOf(b2, 8)).getLong(),
        Seed.from(b2).toLong());
    final byte[] b3 = {1, 2, 3, 4, 5, 6, 7, 8};
    final byte[] b4 = {1, 2, 3, 4, 5, 6, 7, 8, 9};
    Assertions.assertNotEquals(Seed.from(b3).toLong(), Seed.from(b4).toLong());
  }

  @SeededTest
  void testEqualsAndHashCode(RandomSeed rs) {
    final byte[] bytes = rs.get();
    final int hash = Arrays.hashCode(bytes);
    final Seed seed = Seed.from(bytes);
    Assertions.assertEquals(hash, seed.hashCode());
    Assertions.assertEquals(hash, seed.hashCode(), "hash code is cached");
    Assertions.assertTrue(seed.equals(seed));
    Assertions.assertTrue(seed.equals(Seed.from(bytes)));
    Assertions.assertFalse(seed.equals("hello"));
    bytes[0]++;
    Assertions.assertFalse(seed.equals(Seed.from(bytes)));
  }
}
