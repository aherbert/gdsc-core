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

package uk.ac.sussex.gdsc.core.match;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link AucCalculator}.
 */
@SuppressWarnings({"javadoc"})
class AucCalculatorTest {
  @Test
  void testBadArguments() {
    // Length mismatch
    final double[] precision = new double[1];
    final double[] recall = new double[2];
    Assertions.assertThrows(NullPointerException.class, () -> AucCalculator.auc(null, recall));
    Assertions.assertThrows(NullPointerException.class, () -> AucCalculator.auc(precision, null));
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> AucCalculator.auc(precision, recall));
  }

  @Test
  void testAuc() {
    final double[] precision = {1, 0.75, 0.8, 0.8, 0.6};
    final double[] recall = {0, 0.1, 0.2, 0.35, 0.4};
    // @formatter:off
    final double expected =
        (recall[1] - recall[0]) * (precision[1] + precision[0]) / 2 +
        (recall[2] - recall[1]) * (precision[2] + precision[1]) / 2 +
        (recall[3] - recall[2]) * (precision[3] + precision[2]) / 2 +
        (recall[4] - recall[3]) * (precision[4] + precision[3]) / 2;
    // @formatter:on
    Assertions.assertEquals(expected, AucCalculator.auc(precision, recall));
  }

  @Test
  void testAucWithoutRecallZero() {
    final double[] precision = {0.75, 0.8, 0.8, 0.6};
    final double[] recall = {0.1, 0.2, 0.35, 0.4};
    // @formatter:off
    final double expected =
        (recall[0] -       0.0) * (precision[0] +          1.0) / 2 +
        (recall[1] - recall[0]) * (precision[1] + precision[0]) / 2 +
        (recall[2] - recall[1]) * (precision[2] + precision[1]) / 2 +
        (recall[3] - recall[2]) * (precision[3] + precision[2]) / 2;
    // @formatter:on
    Assertions.assertEquals(expected, AucCalculator.auc(precision, recall));
  }
}
