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

import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.sampling.distribution.SharedStateContinuousSampler;
import org.junit.jupiter.api.Assertions;
import uk.ac.sussex.gdsc.test.junit5.RandomSeed;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.rng.RngUtils;

@SuppressWarnings("javadoc")
public class SignedDoubleSamplerTest {
  @SeededTest
  public void testSampler(RandomSeed seed) {
    final UniformRandomProvider rng = RngUtils.create(seed.getSeed());
    final UniformRandomProvider rng1 = RngUtils.create(seed.getSeed());
    final UniformRandomProvider rng2 = RngUtils.create(seed.getSeed());
    final SharedStateContinuousSampler sampler1 = new SignedDoubleSampler(rng1);
    final SharedStateContinuousSampler sampler2 = sampler1.withUniformRandomProvider(rng2);
    Assertions.assertNotSame(sampler1, sampler2);
    for (int i = 0; i < 10; i++) {
      final double expected = NumberUtils.makeSignedDouble(rng.nextLong());
      Assertions.assertEquals(expected, sampler1.sample());
      Assertions.assertEquals(expected, sampler2.sample());
    }
  }
}
