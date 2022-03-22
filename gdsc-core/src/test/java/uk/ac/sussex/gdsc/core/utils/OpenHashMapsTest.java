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
import uk.ac.sussex.gdsc.core.utils.OpenHashMaps.CustomInt2IntOpenHashMap;
import uk.ac.sussex.gdsc.core.utils.OpenHashMaps.CustomInt2ObjectOpenHashMap;
import uk.ac.sussex.gdsc.core.utils.OpenHashMaps.CustomLong2IntOpenHashMap;
import uk.ac.sussex.gdsc.core.utils.OpenHashMaps.CustomObject2IntOpenHashMap;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.rng.RngFactory;
import uk.ac.sussex.gdsc.test.utils.RandomSeed;

@SuppressWarnings({"javadoc"})
class OpenHashMapsTest {
  @SeededTest
  void testLong2IntForEach(RandomSeed seed) {
    final UniformRandomProvider rng = RngFactory.create(seed.get());
    final CustomLong2IntOpenHashMap m = new CustomLong2IntOpenHashMap();
    m.forEach((long k, int v) -> {
      Assertions.fail();
    });
    for (int i = 0; i < 5; i++) {
      m.put(rng.nextLong(), rng.nextInt());
    }
    final CustomLong2IntOpenHashMap m2 = new CustomLong2IntOpenHashMap(16);
    m.forEach((long k, int v) -> m2.put(k, v));
    Assertions.assertEquals(m2, m);
  }

  @SeededTest
  void testInt2IntForEach(RandomSeed seed) {
    final UniformRandomProvider rng = RngFactory.create(seed.get());
    final CustomInt2IntOpenHashMap m = new CustomInt2IntOpenHashMap();
    m.forEach((int k, int v) -> {
      Assertions.fail();
    });
    for (int i = 0; i < 5; i++) {
      m.put(rng.nextInt(), rng.nextInt());
    }
    final CustomInt2IntOpenHashMap m2 = new CustomInt2IntOpenHashMap(16);
    m.forEach((int k, int v) -> m2.put(k, v));
    Assertions.assertEquals(m2, m);
  }

  @SeededTest
  void testObject2IntForEach(RandomSeed seed) {
    final UniformRandomProvider rng = RngFactory.create(seed.get());
    final CustomObject2IntOpenHashMap<Double> m = new CustomObject2IntOpenHashMap<>();
    m.forEach((Double k, int v) -> {
      Assertions.fail();
    });
    for (int i = 0; i < 5; i++) {
      m.put(Double.valueOf(rng.nextDouble()), rng.nextInt());
    }
    final CustomObject2IntOpenHashMap<Double> m2 = new CustomObject2IntOpenHashMap<>(16);
    m.forEach((Double k, int v) -> m2.put(k, v));
    Assertions.assertEquals(m2, m);
  }

  @SeededTest
  void testInt2ObjectForEach(RandomSeed seed) {
    final UniformRandomProvider rng = RngFactory.create(seed.get());
    final CustomInt2ObjectOpenHashMap<Double> m = new CustomInt2ObjectOpenHashMap<>();
    m.forEach((int k, Double v) -> {
      Assertions.fail();
    });
    for (int i = 0; i < 5; i++) {
      m.put(rng.nextInt(), Double.valueOf(rng.nextDouble()));
    }
    final CustomInt2ObjectOpenHashMap<Double> m2 = new CustomInt2ObjectOpenHashMap<>(16);
    m.forEach((int k, Double v) -> m2.put(k, v));
    Assertions.assertEquals(m2, m);
  }
}
