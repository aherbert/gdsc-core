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

package uk.ac.sussex.gdsc.core.trees;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"javadoc"})
public class FloatDistanceFunctionsTest {
  @Test
  public void testSquareEuclideanDistance() {
    FloatDistanceFunction twod = FloatDistanceFunctions.SQUARED_EUCLIDEAN_2D;
    FloatDistanceFunction threed = FloatDistanceFunctions.SQUARED_EUCLIDEAN_3D;
    FloatDistanceFunction nd = FloatDistanceFunctions.SQUARED_EUCLIDEAN_ND;
    double[] p1 = {1, 2, 3};
    float[] p2 = {4, 6, 8};
    Assertions.assertEquals(25.0, twod.distance(p1, p2));
    Assertions.assertEquals(50.0, threed.distance(p1, p2));
    Assertions.assertEquals(50.0, nd.distance(p1, p2));
  }

  @Test
  public void testSquareEuclideanDistanceToRectangle() {
    FloatDistanceFunction twod = FloatDistanceFunctions.SQUARED_EUCLIDEAN_2D;
    FloatDistanceFunction threed = FloatDistanceFunctions.SQUARED_EUCLIDEAN_3D;
    FloatDistanceFunction nd = FloatDistanceFunctions.SQUARED_EUCLIDEAN_ND;
    float[] p1 = {1, 2, 3};
    float[] p2 = {4, 6, 8};

    // Inside
    double[] point = {3, 4, 5};
    Assertions.assertEquals(0.0, twod.distanceToRectangle(point, p1, p2));
    Assertions.assertEquals(0.0, threed.distanceToRectangle(point, p1, p2));
    Assertions.assertEquals(0.0, nd.distanceToRectangle(point, p1, p2));

    // Below
    point[0] = 0;
    Assertions.assertEquals(1.0, twod.distanceToRectangle(point, p1, p2));
    Assertions.assertEquals(1.0, threed.distanceToRectangle(point, p1, p2));
    Assertions.assertEquals(1.0, nd.distanceToRectangle(point, p1, p2));

    // Above
    point[1] = 10;
    Assertions.assertEquals(17.0, twod.distanceToRectangle(point, p1, p2));
    Assertions.assertEquals(17.0, threed.distanceToRectangle(point, p1, p2));
    Assertions.assertEquals(17.0, nd.distanceToRectangle(point, p1, p2));
  }
}
