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

package uk.ac.sussex.gdsc.core.trees;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"javadoc"})
class SplitStrategiesTest {
  @Test
  void testComputeSplitValue() {
    Assertions.assertEquals(3.5, SplitStrategies.computeSplitValue(2, 5));
    Assertions.assertEquals(0.5, SplitStrategies.computeSplitValue(Double.MIN_VALUE, 1));

    // Do not split on infinity
    Assertions.assertEquals(Double.MAX_VALUE,
        SplitStrategies.computeSplitValue(Double.MAX_VALUE / 2, Double.POSITIVE_INFINITY));
    Assertions.assertEquals(-Double.MAX_VALUE,
        SplitStrategies.computeSplitValue(Double.NEGATIVE_INFINITY, -Double.MAX_VALUE / 2));

    // Do not split on NaN
    Assertions.assertEquals(0, SplitStrategies.computeSplitValue(Double.NaN, 1));

    // Do not split on max value in the event of rounding errors
    Assertions.assertEquals(Math.nextDown(2.0),
        SplitStrategies.computeSplitValue(Math.nextDown(2.0), 2.0));
  }
}
