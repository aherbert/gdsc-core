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

import java.util.function.BiPredicate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"javadoc"})
public class DoubleArrayPredicatesTest {
  @Test
  public void testEquals() {
    final BiPredicate<double[], double[]> twod = DoubleArrayPredicates.EQUALS_2D;
    final BiPredicate<double[], double[]> threed = DoubleArrayPredicates.EQUALS_3D;
    final BiPredicate<double[], double[]> nd = DoubleArrayPredicates.EQUALS_ND;
    final double x = -100;
    final double[] p1 = {0, 1, 2};
    final double[] p2 = {x, 1, 2};
    final double[] p3 = {0, x, 2};
    final double[] p4 = {0, 1, x};
    final double[] p5 = {-0f, 1, 2};
    Assertions.assertTrue(twod.test(p1, p1));
    Assertions.assertFalse(twod.test(p1, p2));
    Assertions.assertFalse(twod.test(p1, p3));
    Assertions.assertTrue(twod.test(p1, p4));
    Assertions.assertTrue(twod.test(p1, p5));
    Assertions.assertTrue(twod.test(p1, p1));
    Assertions.assertFalse(twod.test(p2, p1));
    Assertions.assertFalse(twod.test(p3, p1));
    Assertions.assertTrue(twod.test(p4, p1));
    Assertions.assertTrue(twod.test(p5, p1));

    Assertions.assertTrue(threed.test(p1, p1));
    Assertions.assertFalse(threed.test(p1, p2));
    Assertions.assertFalse(threed.test(p1, p3));
    Assertions.assertFalse(threed.test(p1, p4));
    Assertions.assertTrue(threed.test(p1, p5));
    Assertions.assertTrue(threed.test(p1, p1));
    Assertions.assertFalse(threed.test(p2, p1));
    Assertions.assertFalse(threed.test(p3, p1));
    Assertions.assertFalse(threed.test(p4, p1));
    Assertions.assertTrue(threed.test(p5, p1));

    Assertions.assertTrue(nd.test(p1, p1));
    Assertions.assertFalse(nd.test(p1, p2));
    Assertions.assertFalse(nd.test(p1, p3));
    Assertions.assertFalse(nd.test(p1, p4));
    Assertions.assertTrue(nd.test(p1, p5));
    Assertions.assertTrue(nd.test(p1, p1));
    Assertions.assertFalse(nd.test(p2, p1));
    Assertions.assertFalse(nd.test(p3, p1));
    Assertions.assertFalse(nd.test(p4, p1));
    Assertions.assertTrue(nd.test(p5, p1));
  }

  @Test
  public void testCreateSquareEuclideanDistanceFunction() {
    Assertions.assertSame(DoubleArrayPredicates.EQUALS_ND, DoubleArrayPredicates.equals(1));
    Assertions.assertSame(DoubleArrayPredicates.EQUALS_2D, DoubleArrayPredicates.equals(2));
    Assertions.assertSame(DoubleArrayPredicates.EQUALS_3D, DoubleArrayPredicates.equals(3));
    Assertions.assertSame(DoubleArrayPredicates.EQUALS_ND, DoubleArrayPredicates.equals(4));
  }
}
