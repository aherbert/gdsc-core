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

package uk.ac.sussex.gdsc.core.match;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Assertions;
import uk.ac.sussex.gdsc.core.utils.rng.RandomUtils;
import uk.ac.sussex.gdsc.test.junit5.RandomSeed;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.rng.RngUtils;

/**
 * Test for {@link AssignmentComparator}.
 */
@SuppressWarnings({"javadoc"})
class AssignmentComparatorTest {
  @SeededTest
  void canSort(RandomSeed seed) {
    final Assignment[] data = IntStream.rangeClosed(1, 10)
        .mapToObj(d -> new ImmutableAssignment(0, 0, d)).toArray(Assignment[]::new);
    RandomUtils.shuffle(data, RngUtils.create(seed.getSeed()));
    final List<Assignment> list = Arrays.asList(data.clone());
    AssignmentComparator.sort(data);
    AssignmentComparator.sort(list);
    for (int i = 0; i < data.length; i++) {
      final double expected = i + 1.0;
      Assertions.assertEquals(expected, data[i].getDistance(), "Array sort");
      Assertions.assertEquals(expected, list.get(i).getDistance(), "List sort");
    }
  }
}
