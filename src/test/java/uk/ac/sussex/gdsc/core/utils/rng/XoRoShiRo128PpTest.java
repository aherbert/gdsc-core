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

import java.util.concurrent.ThreadLocalRandom;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings("javadoc")
class XoRoShiRo128PpTest {
  @Test
  void testZeroSeed() {
    final XoRoShiRo128PP rng1 = new XoRoShiRo128PP(0);
    final XoRoShiRo128PP rng2 = new XoRoShiRo128PP(0, 0);
    boolean zeroOutput = true;
    for (int i = 0; i < 200; i++) {
      final long value = rng1.nextLong();
      Assertions.assertEquals(value, rng2.nextLong());
      if (value != 0) {
        zeroOutput = false;
      }
    }
    Assertions.assertFalse(zeroOutput, "Zero seed should not create all zero output");
  }

  /**
   * Hit the edge case where the one seed state is zero but the second is not (i.e. a partial zero
   * seed).
   */
  @Test
  void testPartialZeroSeed() {
    final XoRoShiRo128PP rng1 = new XoRoShiRo128PP(0);
    final XoRoShiRo128PP rng2 = new XoRoShiRo128PP(0, 1);
    Assertions.assertNotEquals(rng1.nextLong(), rng2.nextLong());
    final XoRoShiRo128PP rng3 = new XoRoShiRo128PP(0);
    final XoRoShiRo128PP rng4 = new XoRoShiRo128PP(1, 0);
    Assertions.assertNotEquals(rng3.nextLong(), rng4.nextLong());
  }

  @Test
  void testCopy() {
    final XoRoShiRo128PP rng1 = new XoRoShiRo128PP(ThreadLocalRandom.current().nextLong());
    // Create some state
    rng1.nextInt();
    rng1.nextBoolean();
    final XoRoShiRo128PP rng2 = rng1.copy();
    Assertions.assertNotSame(rng1, rng2);
    for (int i = 0; i < 10; i++) {
      Assertions.assertEquals(rng1.nextLong(), rng2.nextLong());
      Assertions.assertEquals(rng1.nextInt(), rng2.nextInt());
      Assertions.assertEquals(rng1.nextBoolean(), rng2.nextBoolean());
    }
  }

  @Test
  void testSplit() {
    final long seed0 = ThreadLocalRandom.current().nextLong();
    final long seed1 = seed0 + 2637846284L;

    // Create the split
    final XoRoShiRo128PP rng1 = new XoRoShiRo128PP(seed0, seed1);
    final XoRoShiRo128PP rng2a = rng1.split();
    Assertions.assertNotSame(rng1, rng2a, "Split should be a different object");

    // Check the split does a mix of the state
    final XoRoShiRo128PP rng2b =
        new XoRoShiRo128PP(Mixers.stafford13(seed0), Mixers.stafford13(seed1));

    for (int i = 0; i < 10; i++) {
      Assertions.assertEquals(rng2a.nextLong(), rng2b.nextLong());
    }

    // Second split should be the same as from an advanced generator
    final XoRoShiRo128PP rng3a = rng1.split();
    final XoRoShiRo128PP rng1copy = new XoRoShiRo128PP(seed0, seed1);
    rng1copy.nextLong();
    final XoRoShiRo128PP rng3b = rng1copy.split();

    for (int i = 0; i < 10; i++) {
      Assertions.assertEquals(rng3a.nextLong(), rng3b.nextLong());
    }
  }
}
