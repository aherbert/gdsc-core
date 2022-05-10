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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Random;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.core.data.NotImplementedException;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.utils.RandomSeed;

@SuppressWarnings("javadoc")
class JdkRandomAdapterTest {
  @SuppressWarnings("unused")
  @Test
  void testConstructorThrows() {
    Assertions.assertThrows(NullPointerException.class, () -> {
      new JdkRandomAdapter(null);
    });
  }

  @Test
  void testSetSeedThrows() {
    final JdkRandomAdapter rng = new JdkRandomAdapter(SplitMix.new32(0)::nextInt);
    Assertions.assertThrows(NotImplementedException.class, () -> {
      rng.setSeed(44);
    });
  }

  @SeededTest
  void testGeneratedValues(RandomSeed randomSeed) {
    final long seed = randomSeed.getAsLong();
    final Random random1 = new Random(seed);
    final Random random2 = new Random(seed);
    final JdkRandomAdapter rng = new JdkRandomAdapter(random2::nextInt);

    Assertions.assertEquals(random1.nextInt(), rng.nextInt());
    Assertions.assertEquals(random1.nextInt(567), rng.nextInt(567));
    Assertions.assertEquals(random1.nextFloat(), rng.nextFloat());
    Assertions.assertEquals(random1.nextDouble(), rng.nextDouble());
  }

  @Test
  void testSerializationThrows() throws IOException {
    final JdkRandomAdapter rng = new JdkRandomAdapter(SplitMix.new32(0)::nextInt);
    try (ObjectOutputStream oos = new ObjectOutputStream(new ByteArrayOutputStream())) {
      Assertions.assertThrows(NotImplementedException.class, () -> {
        oos.writeObject(rng);
      });
    }
  }
}
