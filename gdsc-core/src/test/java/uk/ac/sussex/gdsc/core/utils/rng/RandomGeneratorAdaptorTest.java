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
 * Copyright (C) 2011 - 2022 Alex Herbert
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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.core.data.NotImplementedException;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.utils.RandomSeed;

@SuppressWarnings("javadoc")
class RandomGeneratorAdaptorTest {
  @Test
  void testConstructorThrows() {
    Assertions.assertThrows(IllegalArgumentException.class, () -> new RandomGeneratorAdapter(null));
  }

  @Test
  void testSetSeedThrows() {
    final SplitMix rng = SplitMix.new64(0);
    Assertions.assertThrows(NotImplementedException.class,
        () -> new RandomGeneratorAdapter(rng).setSeed(0), "Should throw with int seed");
    Assertions.assertThrows(NotImplementedException.class,
        () -> new RandomGeneratorAdapter(rng).setSeed(new int[2]), "Should throw with int[] seed");
    Assertions.assertThrows(NotImplementedException.class,
        () -> new RandomGeneratorAdapter(rng).setSeed(0L), "Should throw with long seed");
  }

  @SeededTest
  void testNextMethods(RandomSeed randomSeed) {
    final long seed = randomSeed.getAsLong();
    final SplitMix rng1 = SplitMix.new64(seed);
    final RandomGeneratorAdapter rng2 = new RandomGeneratorAdapter(SplitMix.new64(seed));
    Assertions.assertEquals(rng1.nextDouble(), rng2.nextDouble());
    Assertions.assertEquals(rng1.nextFloat(), rng2.nextFloat());
    Assertions.assertEquals(rng1.nextInt(), rng2.nextInt());
    Assertions.assertEquals(rng1.nextInt(44), rng2.nextInt(44));
    Assertions.assertEquals(rng1.nextLong(), rng2.nextLong());
    Assertions.assertEquals(rng1.nextBoolean(), rng2.nextBoolean());
    final byte[] b1 = new byte[23];
    final byte[] b2 = new byte[23];
    rng1.nextBytes(b1);
    rng2.nextBytes(b2);
    Assertions.assertArrayEquals(b1, b2);
  }
}
