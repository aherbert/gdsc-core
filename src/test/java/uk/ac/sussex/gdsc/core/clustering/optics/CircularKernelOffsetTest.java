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

package uk.ac.sussex.gdsc.core.clustering.optics;

import java.util.logging.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.core.utils.MathUtils;
import uk.ac.sussex.gdsc.test.utils.TestComplexity;
import uk.ac.sussex.gdsc.test.utils.TestSettings;
import uk.ac.sussex.gdsc.test.utils.functions.FunctionUtils;

@SuppressWarnings({"javadoc"})
class CircularKernelOffsetTest {
  private static Logger logger;

  @BeforeAll
  public static void beforeAll() {
    logger = Logger.getLogger(CircularKernelOffsetTest.class.getName());
  }

  @AfterAll
  public static void afterAll() {
    logger = null;
  }

  @Test
  void canBuildCircularKernelAtDifferentResolutions() {
    // Note: The radius of the default circle is 1 =>
    // Circle Area = pi
    // Square Area = 4

    final int max = (TestSettings.allow(TestComplexity.LOW)) ? 100 : 10;

    for (int r = 1; r <= max; r++) {
      final CircularKernelOffset[] offset = CircularKernelOffset.create(r);
      final int size = offset.length * offset.length;
      final double pixelArea = 4.0 / (size);
      // Count pixels for the outer/inner circles
      int outer = 0;
      int inner = 0;
      for (final CircularKernelOffset o : offset) {
        outer += Math.max(0, o.end - o.start);
        if (o.internal) {
          inner += o.endInternal - o.startInternal;
        }
      }
      final double outerArea = outer * pixelArea;
      final double innerArea = inner * pixelArea;
      final int skip = size - outer;
      logger.info(
          FunctionUtils.getSupplier("R=%d, outer=%d  %f (%f), Skip=%d  (%f), inner=%d  %f (%f)", r,
              outer, outerArea, outerArea / Math.PI, skip, (double) skip / size, inner, innerArea,
              innerArea / outerArea));

      // Test for symmetry
      final int w = offset.length;
      final boolean[] outerMask = new boolean[w * w];
      final boolean[] innerMask = new boolean[outerMask.length];
      for (int i = 0, k = 0; i < offset.length; i++) {
        for (int j = -r; j <= r; j++, k++) {
          if (j >= offset[i].start && j < offset[i].end) {
            outerMask[k] = true;
          }
          if (j >= offset[i].startInternal && j < offset[i].endInternal) {
            innerMask[k] = true;
          }
        }
      }
      for (int y = 0, k = 0; y < w; y++) {
        for (int x = 0; x < w; x++, k++) {
          Assertions.assertTrue(outerMask[k] == outerMask[x * w + y], "No outer symmetry");
        }
      }
      final double e = r * r;
      for (int y = 0, k = 0; y < w; y++) {
        for (int x = 0; x < w; x++, k++) {
          Assertions.assertTrue(innerMask[k] == innerMask[x * w + y], "No inner symmetry");
          // Test distance to centre (r,r)
          if (innerMask[k]) {
            Assertions.assertTrue(MathUtils.distance2(x, y, r, r) <= e, "Bad inner");
          }
        }
      }
    }
  }
}
