/*-
 * #%L
 * Genome Damage and Stability Centre Core Package
 *
 * Contains core utilities for image analysis and is used by:
 *
 * GDSC ImageJ Plugins - Microscopy image analysis
 *
 * GDSC SMLM ImageJ Plugins - Single molecule localisation microscopy (SMLM)
 * %%
 * Copyright (C) 2011 - 2023 Alex Herbert
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
 * Test for {@link MutableAssignment}.
 */
@SuppressWarnings({"javadoc"})
class MutableAssignmentTest {
  @Test
  void canCreate() {
    final int targetId = 1454945;
    final int predictedId = 7686;
    final double distance = 2342.78998;
    final MutableAssignment data = new MutableAssignment(targetId, predictedId, distance);
    Assertions.assertEquals(targetId, data.getTargetId(), "Target Id");
    Assertions.assertEquals(predictedId, data.getPredictedId(), "Predicted Id");
    Assertions.assertEquals(distance, data.getDistance(), "Distance");
  }

  @Test
  void canUpdate() {
    int targetId = 1454945;
    int predictedId = 7686;
    double distance = 2342.78998;
    final MutableAssignment data = new MutableAssignment(targetId, predictedId, distance);

    for (final int add : new int[] {-45, 2, 34}) {
      targetId = add + data.getTargetId();
      predictedId = add + data.getPredictedId();
      distance = add + data.getDistance();
      data.setTargetId(targetId);
      data.setPredictedId(predictedId);
      data.setDistance(distance);
      Assertions.assertEquals(targetId, data.getTargetId(), "Target Id");
      Assertions.assertEquals(predictedId, data.getPredictedId(), "Predicted Id");
      Assertions.assertEquals(distance, data.getDistance(), "Distance");
    }
  }
}
