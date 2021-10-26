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

import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test the {@link LocalCollectors}.
 */
@SuppressWarnings({"javadoc"})
class LocalCollectorsTest {

  @Test
  void testToLocalList() {
    final Collector<Integer, ?, LocalList<Integer>> c = LocalCollectors.toLocalList();

    final LocalList<Integer> list = IntStream.range(0, 100).parallel().boxed().collect(c);
    Assertions.assertEquals(100, list.size());
    list.sort(Integer::compare);
    for (int i = 0; i < list.size(); i++) {
      Assertions.assertEquals(i, list.unsafeGet(i));
    }

    // The finisher is not used by the stream as it is an identity function.
    assertFinisherIsIndentityFunction(c);
  }

  private static <A> void
      assertFinisherIsIndentityFunction(Collector<Integer, A, LocalList<Integer>> c) {
    Assertions.assertTrue(c.characteristics().contains(Collector.Characteristics.IDENTITY_FINISH));

    final Function<A, LocalList<Integer>> f = c.finisher();
    Assertions.assertNotNull(f);

    // Test it is an identity function.
    Assertions.assertNull(f.apply(null));

    final A a = c.supplier().get();
    Assertions.assertSame(a, f.apply(a));
  }
}
