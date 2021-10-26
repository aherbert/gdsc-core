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

package uk.ac.sussex.gdsc.core.math.hull;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"javadoc"})
class ActiveListTest {
  @Test
  void testActiveList() {
    final int n = 5;
    final ActiveList active = new ActiveList(n);
    Assertions.assertEquals(0, active.size());
    for (int i = 0; i < n; i++) {
      Assertions.assertFalse(active.isEnabled(i));
      Assertions.assertTrue(active.isDisabled(i));
    }
    active.enableAll();
    for (int i = 0; i < n; i++) {
      Assertions.assertTrue(active.isEnabled(i));
      Assertions.assertFalse(active.isDisabled(i));
    }
    Assertions.assertEquals(n, active.size());
    final int index = 2;
    active.disable(index);
    Assertions.assertEquals(n - 1, active.size());
    Assertions.assertFalse(active.isEnabled(index));
    Assertions.assertTrue(active.isDisabled(index));
    active.enable(index);
    Assertions.assertEquals(n, active.size());
    Assertions.assertTrue(active.isEnabled(index));
    Assertions.assertFalse(active.isDisabled(index));
  }
}
