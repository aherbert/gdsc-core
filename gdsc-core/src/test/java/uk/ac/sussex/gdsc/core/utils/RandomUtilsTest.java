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

package uk.ac.sussex.gdsc.core.utils;

import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.Assertions;
import uk.ac.sussex.gdsc.core.utils.rng.RandomUtils;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.rng.RngUtils;
import uk.ac.sussex.gdsc.test.utils.RandomSeed;

@SuppressWarnings({"javadoc"})
class RandomUtilsTest {
  @SeededTest
  void canComputeSample(RandomSeed seed) {
    final int[] set = new int[] {0, 1, 2, 5, 8, 9, 10};
    final UniformRandomProvider rng = RngUtils.create(seed.get());
    for (final int total : set) {
      for (final int size : set) {
        canComputeSample(rng, size, total);
      }
    }
  }

  private static void canComputeSample(UniformRandomProvider rng, int size, int total) {
    final int[] sample = RandomUtils.sample(size, total, rng);
    // TestLog.debug(logger,"%d from %d = %s", k, n, java.util.Arrays.toString(sample));
    Assertions.assertEquals(Math.min(size, total), sample.length);
    for (int i = 0; i < sample.length; i++) {
      for (int j = i + 1; j < sample.length; j++) {
        Assertions.assertNotEquals(sample[i], sample[j]);
      }
    }
  }

  @SeededTest
  void canComputeSampleFromBigData(RandomSeed seed) {
    final UniformRandomProvider rng = RngUtils.create(seed.get());
    final int total = 100;
    for (final int size : new int[] {0, 1, 2, total / 2, total - 2, total - 1, total}) {
      canComputeSample(rng, size, total);
    }
  }
}
