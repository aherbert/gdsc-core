/*-
 * #%L
 * Genome Damage and Stability Centre Core ImageJ Package
 *
 * Contains core utilities for image analysis in ImageJ and is used by:
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

package uk.ac.sussex.gdsc.core.ij.roi;

import ij.gui.Line;
import ij.gui.OvalRoi;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.gui.ShapeRoi;
import java.util.Arrays;
import java.util.function.Function;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"javadoc"})
class CoordinatePredicateUtilsTest {

  @Test
  void testCoordinatePredicateNotArea() {
    Assertions.assertNull(CoordinatePredicateUtils.createContainsPredicate(null));
    Assertions.assertNull(CoordinatePredicateUtils.createContainsPredicate(new Line(1, 2, 3, 4)));
    Assertions.assertNull(CoordinatePredicateUtils.createContainsPredicate(
        new PolygonRoi(new int[] {0, 0}, new int[] {0, 1}, 2, Roi.POLYGON)));
    Assertions.assertNull(CoordinatePredicateUtils.createContainsPredicate(
        new PolygonRoi(new int[] {0, 0}, new int[] {1, 0}, 2, Roi.POLYGON)));
    Assertions.assertNull(CoordinatePredicateUtils.createContainsPredicate(
        new PolygonRoi(new int[] {0, 0}, new int[] {0, 0}, 2, Roi.POLYGON)));
    final ShapeRoi roi = new ShapeRoi(new Roi(0, 0, 1, 1)).and(new ShapeRoi(new Roi(3, 3, 1, 1)));
    Assertions.assertNull(CoordinatePredicateUtils.createContainsPredicate(roi));
    // Bad ROI type
    @SuppressWarnings("serial")
    final Roi badRoi = new Roi(0, 1, 2, 3) {
      @Override
      public int getType() {
        return -99;
      }
    };
    Assertions.assertNull(CoordinatePredicateUtils.createContainsPredicate(badRoi));
  }

  @Test
  void testBasicRoiContainsPredicateThrows() {
    final Roi roi = new PolygonRoi(new int[] {0, 0, 5}, new int[] {0, 5, 5}, 3, Roi.POLYGON);
    assertThrows(roi, BasicRoiContainsPredicate::new);
  }

  @Test
  void testPolygonRoiContainsPredicateThrows() {
    final Roi roi = new Roi(0, 1, 2, 3);
    assertThrows(roi, PolygonRoiContainsPredicate::new);
  }

  @Test
  void testCompositeRoiContainsPredicateThrows() {
    final Roi roi = new Roi(0, 1, 2, 3);
    assertThrows(roi, CompositeRoiContainsPredicate::new);
  }

  private static void assertThrows(Roi roi, Function<Roi, CoordinatePredicate> constructor) {
    Assertions.assertThrows(IllegalArgumentException.class, () -> constructor.apply(roi));
  }

  @Test
  void testCoordinatePredicateRectangle() {
    assertCoordinatePredicate(new Roi(1, 2, 3, 4), new int[][] {{2, 3}, {5, 6}},
        new boolean[] {true, false});
  }

  @Test
  void testCoordinatePredicateRoundedRectangle() {
    final Roi roi = new Roi(0, 0, 10, 10);
    roi.setRoundRectArcSize(1);
    assertCoordinatePredicate(roi, new int[][] {{2, 3}, {10, 10}}, new boolean[] {true, false});
  }

  @Test
  void testCoordinatePredicateOval() {
    assertCoordinatePredicate(new OvalRoi(1, 2, 3, 4), new int[][] {{2, 3}, {5, 6}},
        new boolean[] {true, false});
  }

  @Test
  void testCoordinatePredicateComposite() {
    final Roi roi1 = new Roi(1, 2, 3, 4);
    final Roi roi2 = new Roi(5, 6, 7, 8);
    final Roi roi = new ShapeRoi(roi1).or(new ShapeRoi(roi2));
    assertCoordinatePredicate(roi, new int[][] {{2, 3}, {5, 6}, {5, 25}},
        new boolean[] {true, true, false});
  }

  @Test
  void testCoordinatePredicatePolygon() {
    final Roi roi = new PolygonRoi(new float[] {0, 0.5f, 1.5f, 2, 1.5f, 0.5f},
        new float[] {0, 1, 1, 0, -1, -1}, 6, Roi.POLYGON);
    assertCoordinatePredicate(roi, new int[][] {{0, 1}, {1, 1}, {2, 1}, {0, -1}, {1, -1}, {2, -1}},
        new boolean[] {false, false, false, false, true, false});
  }

  @Test
  void testCoordinatePredicateFreeRoi() {
    final Roi roi = new PolygonRoi(new int[] {0, 0, 5}, new int[] {0, 5, 5}, 3, Roi.FREEROI);
    assertCoordinatePredicate(roi, new int[][] {{1, 2}, {1, 1}, {5, 0}, {3, 4}},
        new boolean[] {true, false, false, true});
  }

  @Test
  void testCoordinatePredicateTracedRoi() {
    final Roi roi = new PolygonRoi(new int[] {0, 0, 5}, new int[] {0, 5, 5}, 3, Roi.TRACED_ROI);
    assertCoordinatePredicate(roi, new int[][] {{1, 2}, {1, 1}, {5, 0}, {3, 4}},
        new boolean[] {true, false, false, true});
  }

  private static void assertCoordinatePredicate(Roi roi, int[][] coords, boolean[] within) {
    final CoordinatePredicate predicate = CoordinatePredicateUtils.createContainsPredicate(roi);
    Assertions.assertNotNull(predicate);
    for (int i = 0; i < within.length; i++) {
      final int[] point = coords[i];
      Assertions.assertEquals(within[i], predicate.test(point[0], point[1]),
          () -> Arrays.toString(point));
    }
  }
}
