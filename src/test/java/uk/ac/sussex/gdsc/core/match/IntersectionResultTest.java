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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link IntersectionResult}.
 */
@SuppressWarnings({"javadoc"})
class IntersectionResultTest {
  @Test
  void canCreate() {
    final int tp = 5;
    final int fp = 1;
    final int fn = 3;
    final IntersectionResult match = new IntersectionResult(tp, fp, fn);
    Assertions.assertEquals(tp, match.getIntersection(), "intersection");
    Assertions.assertEquals(fp, match.getSizeAMinusIntersection(), "A - intersection");
    Assertions.assertEquals(fn, match.getSizeBMinusIntersection(), "B - intersection");
    Assertions.assertEquals(tp + fp, match.getSizeA(), "A");
    Assertions.assertEquals(tp + fn, match.getSizeB(), "B");
  }
}
