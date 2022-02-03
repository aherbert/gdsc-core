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

package uk.ac.sussex.gdsc.core.trees;

import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.ac.sussex.gdsc.test.rng.RngUtils;

@SuppressWarnings({"javadoc"})
class StatusStackTest {
  @ParameterizedTest
  @ValueSource(ints = {1, 2, 3, 5, 10, 15, 32, 33, 50})
  void testStack(int capacity) {
    final UniformRandomProvider rng = RngUtils.createWithFixedSeed();
    final byte[] choice =
        {Status.RIGHTVISITED, Status.NONE, Status.LEFTVISITED, Status.ALLVISITED,};
    for (int i = 0; i < 10; i++) {
      final StatusStack stack = StatusStack.create(capacity);
      final byte[] expected = new byte[capacity];
      for (int j = 0; j < capacity; j++) {
        final byte next = choice[rng.nextInt(choice.length)];
        expected[j] = next;
        stack.push(next);

        for (int k = j + 1; k < capacity; k++) {
          expected[k] = next;
          stack.push(next);
        }
        for (int k = capacity; k-- > j + 1;) {
          Assertions.assertEquals(expected[k], stack.pop());
        }
      }
      for (int j = capacity; j-- > 0;) {
        Assertions.assertEquals(expected[j], stack.pop());
      }
      // Allows pop to be called once when empty
      Assertions.assertEquals(0, stack.pop());
    }
  }
}
