package uk.ac.sussex.gdsc.core.clustering.optics;

import uk.ac.sussex.gdsc.test.junit5.*;
import uk.ac.sussex.gdsc.test.rng.RngFactory;
import org.junit.jupiter.api.*;
import uk.ac.sussex.gdsc.test.api.*;
import uk.ac.sussex.gdsc.test.utils.*;

import uk.ac.sussex.gdsc.test.junit5.*;
import uk.ac.sussex.gdsc.test.rng.RngFactory;
import org.junit.jupiter.api.*;
import uk.ac.sussex.gdsc.test.api.*;

import uk.ac.sussex.gdsc.test.junit5.*;
import uk.ac.sussex.gdsc.test.rng.RngFactory;
import org.junit.jupiter.api.*;

import uk.ac.sussex.gdsc.test.junit5.*;
import uk.ac.sussex.gdsc.test.rng.RngFactory;


import java.util.logging.Logger;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import uk.ac.sussex.gdsc.test.junit5.*;import uk.ac.sussex.gdsc.test.rng.RngFactory;import uk.ac.sussex.gdsc.core.utils.Maths;
import uk.ac.sussex.gdsc.test.utils.TestComplexity;
import uk.ac.sussex.gdsc.test.utils.TestSettings;
import uk.ac.sussex.gdsc.test.utils.functions.FunctionUtils;

@SuppressWarnings({"javadoc"})
public class CircularKernelOffsetTest {
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
  public void canBuildCircularKernelAtDifferentResolutions() {
    // Note: The radius of the default circle is 1 =>
    // Circle Area = pi
    // Square Area = 4

    final int max = (TestSettings.allow(TestComplexity.LOW)) ? 100 : 10;

    for (int r = 1; r <= max; r++) {
      final CircularKernelOffset[] offset = CircularKernelOffset.create(r);
      final int size = offset.length * offset.length;
      final double pixelArea = 4.0 / (size);
      // Count pixels for the outer/inner circles
      int outer = 0, inner = 0;
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
            Assertions.assertTrue(Maths.distance2(x, y, r, r) <= e, "Bad inner");
          }
        }
      }
    }
  }
}
