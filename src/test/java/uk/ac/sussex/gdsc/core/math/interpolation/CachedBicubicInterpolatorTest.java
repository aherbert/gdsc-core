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

package uk.ac.sussex.gdsc.core.math.interpolation;

import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.test.api.TestAssertions;
import uk.ac.sussex.gdsc.test.api.TestHelper;
import uk.ac.sussex.gdsc.test.api.function.DoubleDoubleBiPredicate;

/**
 * Test for {@link CachedBicubicInterpolator}.
 */
@SuppressWarnings({"javadoc"})
class CachedBicubicInterpolatorTest {
  @Test
  void testGetValue() {

    // Test data
    final float[] fdata = new float[4 * 4];
    final float[][] ffdata = new float[4][4];
    final double[] ddata = new double[4 * 4];
    final double[][] dddata = new double[4][4];
    for (int y = 0, i = 0; y < 4; y++) {
      for (int x = 0; x < 4; x++, i++) {
        fdata[i] = i;
        ddata[i] = i;
        ffdata[x][y] = i;
        dddata[x][y] = i;
      }
    }
    final FloatProcessor fp = new FloatProcessor(4, 4, fdata);
    fp.setInterpolationMethod(ImageProcessor.BICUBIC);

    final double[] x = new double[] {0.3, 0.5, 0.7};
    final double[] x2 = new double[x.length];
    final double[] x3 = new double[x.length];
    for (int i = 0; i < x.length; i++) {
      x2[i] = x[i] * x[i];
      x3[i] = x[i] * x[i] * x[i];
    }

    final DoubleDoubleBiPredicate equality = TestHelper.doublesAreClose(1e-5, 0);

    for (int i = 0; i < x.length; i++) {
      for (int j = 0; j < x.length; j++) {

        final double e = fp.getInterpolatedPixel(1 + x[i], 1 + x[j]);

        CachedBicubicInterpolator in = new CachedBicubicInterpolator();
        in.updateCoefficients(fdata);


        TestAssertions.assertTest(e, in.getValue(x[i], x[j]), equality);
        TestAssertions.assertTest(e, in.getValue(x[i], x2[i], x3[i], x[j], x2[j], x3[j]), equality);

        in = new CachedBicubicInterpolator();
        in.updateCoefficients(ddata);

        TestAssertions.assertTest(e, in.getValue(x[i], x[j]), equality);
        TestAssertions.assertTest(e, in.getValue(x[i], x2[i], x3[i], x[j], x2[j], x3[j]), equality);

        in = new CachedBicubicInterpolator();
        in.updateCoefficients(ffdata);

        TestAssertions.assertTest(e, in.getValue(x[i], x[j]), equality);
        TestAssertions.assertTest(e, in.getValue(x[i], x2[i], x3[i], x[j], x2[j], x3[j]), equality);

        in = new CachedBicubicInterpolator();
        in.updateCoefficients(dddata);

        TestAssertions.assertTest(e, in.getValue(x[i], x[j]), equality);
        TestAssertions.assertTest(e, in.getValue(x[i], x2[i], x3[i], x[j], x2[j], x3[j]), equality);
      }
    }
  }
}
