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

package uk.ac.sussex.gdsc.core.math.interpolation;

import java.io.Serializable;

/**
 * Contains the 64 data points of a cubic spline stored as a {@code double}.
 *
 * <p>Can be used to represent a cubic spline power table or cubic spline coefficients.
 *
 * <p>This class is immutable.
 */
public class DoubleCubicSplineData implements Serializable {
  private static final long serialVersionUID = 20190326L;

  /** Data for x^0 * y^0 * z^0 (data[0]). */
  public final double x0y0z0;
  /** Data for x^1 * y^0 * z^0 (data[1]). */
  public final double x1y0z0;
  /** Data for x^2 * y^0 * z^0 (data[2]). */
  public final double x2y0z0;
  /** Data for x^3 * y^0 * z^0 (data[3]). */
  public final double x3y0z0;
  /** Data for x^0 * y^1 * z^0 (data[4]). */
  public final double x0y1z0;
  /** Data for x^1 * y^1 * z^0 (data[5]). */
  public final double x1y1z0;
  /** Data for x^2 * y^1 * z^0 (data[6]). */
  public final double x2y1z0;
  /** Data for x^3 * y^1 * z^0 (data[7]). */
  public final double x3y1z0;
  /** Data for x^0 * y^2 * z^0 (data[8]). */
  public final double x0y2z0;
  /** Data for x^1 * y^2 * z^0 (data[9]). */
  public final double x1y2z0;
  /** Data for x^2 * y^2 * z^0 (data[10]). */
  public final double x2y2z0;
  /** Data for x^3 * y^2 * z^0 (data[11]). */
  public final double x3y2z0;
  /** Data for x^0 * y^3 * z^0 (data[12]). */
  public final double x0y3z0;
  /** Data for x^1 * y^3 * z^0 (data[13]). */
  public final double x1y3z0;
  /** Data for x^2 * y^3 * z^0 (data[14]). */
  public final double x2y3z0;
  /** Data for x^3 * y^3 * z^0 (data[15]). */
  public final double x3y3z0;
  /** Data for x^0 * y^0 * z^1 (data[16]). */
  public final double x0y0z1;
  /** Data for x^1 * y^0 * z^1 (data[17]). */
  public final double x1y0z1;
  /** Data for x^2 * y^0 * z^1 (data[18]). */
  public final double x2y0z1;
  /** Data for x^3 * y^0 * z^1 (data[19]). */
  public final double x3y0z1;
  /** Data for x^0 * y^1 * z^1 (data[20]). */
  public final double x0y1z1;
  /** Data for x^1 * y^1 * z^1 (data[21]). */
  public final double x1y1z1;
  /** Data for x^2 * y^1 * z^1 (data[22]). */
  public final double x2y1z1;
  /** Data for x^3 * y^1 * z^1 (data[23]). */
  public final double x3y1z1;
  /** Data for x^0 * y^2 * z^1 (data[24]). */
  public final double x0y2z1;
  /** Data for x^1 * y^2 * z^1 (data[25]). */
  public final double x1y2z1;
  /** Data for x^2 * y^2 * z^1 (data[26]). */
  public final double x2y2z1;
  /** Data for x^3 * y^2 * z^1 (data[27]). */
  public final double x3y2z1;
  /** Data for x^0 * y^3 * z^1 (data[28]). */
  public final double x0y3z1;
  /** Data for x^1 * y^3 * z^1 (data[29]). */
  public final double x1y3z1;
  /** Data for x^2 * y^3 * z^1 (data[30]). */
  public final double x2y3z1;
  /** Data for x^3 * y^3 * z^1 (data[31]). */
  public final double x3y3z1;
  /** Data for x^0 * y^0 * z^2 (data[32]). */
  public final double x0y0z2;
  /** Data for x^1 * y^0 * z^2 (data[33]). */
  public final double x1y0z2;
  /** Data for x^2 * y^0 * z^2 (data[34]). */
  public final double x2y0z2;
  /** Data for x^3 * y^0 * z^2 (data[35]). */
  public final double x3y0z2;
  /** Data for x^0 * y^1 * z^2 (data[36]). */
  public final double x0y1z2;
  /** Data for x^1 * y^1 * z^2 (data[37]). */
  public final double x1y1z2;
  /** Data for x^2 * y^1 * z^2 (data[38]). */
  public final double x2y1z2;
  /** Data for x^3 * y^1 * z^2 (data[39]). */
  public final double x3y1z2;
  /** Data for x^0 * y^2 * z^2 (data[40]). */
  public final double x0y2z2;
  /** Data for x^1 * y^2 * z^2 (data[41]). */
  public final double x1y2z2;
  /** Data for x^2 * y^2 * z^2 (data[42]). */
  public final double x2y2z2;
  /** Data for x^3 * y^2 * z^2 (data[43]). */
  public final double x3y2z2;
  /** Data for x^0 * y^3 * z^2 (data[44]). */
  public final double x0y3z2;
  /** Data for x^1 * y^3 * z^2 (data[45]). */
  public final double x1y3z2;
  /** Data for x^2 * y^3 * z^2 (data[46]). */
  public final double x2y3z2;
  /** Data for x^3 * y^3 * z^2 (data[47]). */
  public final double x3y3z2;
  /** Data for x^0 * y^0 * z^3 (data[48]). */
  public final double x0y0z3;
  /** Data for x^1 * y^0 * z^3 (data[49]). */
  public final double x1y0z3;
  /** Data for x^2 * y^0 * z^3 (data[50]). */
  public final double x2y0z3;
  /** Data for x^3 * y^0 * z^3 (data[51]). */
  public final double x3y0z3;
  /** Data for x^0 * y^1 * z^3 (data[52]). */
  public final double x0y1z3;
  /** Data for x^1 * y^1 * z^3 (data[53]). */
  public final double x1y1z3;
  /** Data for x^2 * y^1 * z^3 (data[54]). */
  public final double x2y1z3;
  /** Data for x^3 * y^1 * z^3 (data[55]). */
  public final double x3y1z3;
  /** Data for x^0 * y^2 * z^3 (data[56]). */
  public final double x0y2z3;
  /** Data for x^1 * y^2 * z^3 (data[57]). */
  public final double x1y2z3;
  /** Data for x^2 * y^2 * z^3 (data[58]). */
  public final double x2y2z3;
  /** Data for x^3 * y^2 * z^3 (data[59]). */
  public final double x3y2z3;
  /** Data for x^0 * y^3 * z^3 (data[60]). */
  public final double x0y3z3;
  /** Data for x^1 * y^3 * z^3 (data[61]). */
  public final double x1y3z3;
  /** Data for x^2 * y^3 * z^3 (data[62]). */
  public final double x2y3z3;
  /** Data for x^3 * y^3 * z^3 (data[63]). */
  public final double x3y3z3;

  /**
   * Create a new instance of the cubic spline power table. The data represents z^a * y^b * x^c with
   * a,b,c in [0, 3].
   *
   * @param x x-coordinate of the interpolation point.
   * @param y y-coordinate of the interpolation point.
   * @param z z-coordinate of the interpolation point.
   */
  public DoubleCubicSplineData(CubicSplinePosition x, CubicSplinePosition y,
      CubicSplinePosition z) {
    // Table computed as if iterating: z^a * y^b * x^c a,b,c in [0, 3]
    x0y0z0 = 1.0;
    x1y0z0 = x.x1;
    x2y0z0 = x.x2;
    x3y0z0 = x.x3;
    x0y1z0 = y.x1;
    x1y1z0 = y.x1 * x.x1;
    x2y1z0 = y.x1 * x.x2;
    x3y1z0 = y.x1 * x.x3;
    x0y2z0 = y.x2;
    x1y2z0 = y.x2 * x.x1;
    x2y2z0 = y.x2 * x.x2;
    x3y2z0 = y.x2 * x.x3;
    x0y3z0 = y.x3;
    x1y3z0 = y.x3 * x.x1;
    x2y3z0 = y.x3 * x.x2;
    x3y3z0 = y.x3 * x.x3;
    x0y0z1 = z.x1;
    x1y0z1 = z.x1 * x.x1;
    x2y0z1 = z.x1 * x.x2;
    x3y0z1 = z.x1 * x.x3;
    x0y1z1 = z.x1 * y.x1;
    x1y1z1 = x0y1z1 * x.x1;
    x2y1z1 = x0y1z1 * x.x2;
    x3y1z1 = x0y1z1 * x.x3;
    x0y2z1 = z.x1 * y.x2;
    x1y2z1 = x0y2z1 * x.x1;
    x2y2z1 = x0y2z1 * x.x2;
    x3y2z1 = x0y2z1 * x.x3;
    x0y3z1 = z.x1 * y.x3;
    x1y3z1 = x0y3z1 * x.x1;
    x2y3z1 = x0y3z1 * x.x2;
    x3y3z1 = x0y3z1 * x.x3;
    x0y0z2 = z.x2;
    x1y0z2 = z.x2 * x.x1;
    x2y0z2 = z.x2 * x.x2;
    x3y0z2 = z.x2 * x.x3;
    x0y1z2 = z.x2 * y.x1;
    x1y1z2 = x0y1z2 * x.x1;
    x2y1z2 = x0y1z2 * x.x2;
    x3y1z2 = x0y1z2 * x.x3;
    x0y2z2 = z.x2 * y.x2;
    x1y2z2 = x0y2z2 * x.x1;
    x2y2z2 = x0y2z2 * x.x2;
    x3y2z2 = x0y2z2 * x.x3;
    x0y3z2 = z.x2 * y.x3;
    x1y3z2 = x0y3z2 * x.x1;
    x2y3z2 = x0y3z2 * x.x2;
    x3y3z2 = x0y3z2 * x.x3;
    x0y0z3 = z.x3;
    x1y0z3 = z.x3 * x.x1;
    x2y0z3 = z.x3 * x.x2;
    x3y0z3 = z.x3 * x.x3;
    x0y1z3 = z.x3 * y.x1;
    x1y1z3 = x0y1z3 * x.x1;
    x2y1z3 = x0y1z3 * x.x2;
    x3y1z3 = x0y1z3 * x.x3;
    x0y2z3 = z.x3 * y.x2;
    x1y2z3 = x0y2z3 * x.x1;
    x2y2z3 = x0y2z3 * x.x2;
    x3y2z3 = x0y2z3 * x.x3;
    x0y3z3 = z.x3 * y.x3;
    x1y3z3 = x0y3z3 * x.x1;
    x2y3z3 = x0y3z3 * x.x2;
    x3y3z3 = x0y3z3 * x.x3;
  }

  /**
   * Create a new instance of the cubic spline coefficients.
   *
   * <p>Coefficients must be computed as if iterating: z^a * y^b * x^c with a,b,c in [0, 3].
   *
   * @param coefficients the coefficients.
   */
  DoubleCubicSplineData(double[] coefficients) {
    x0y0z0 = coefficients[0];
    x1y0z0 = coefficients[1];
    x2y0z0 = coefficients[2];
    x3y0z0 = coefficients[3];
    x0y1z0 = coefficients[4];
    x1y1z0 = coefficients[5];
    x2y1z0 = coefficients[6];
    x3y1z0 = coefficients[7];
    x0y2z0 = coefficients[8];
    x1y2z0 = coefficients[9];
    x2y2z0 = coefficients[10];
    x3y2z0 = coefficients[11];
    x0y3z0 = coefficients[12];
    x1y3z0 = coefficients[13];
    x2y3z0 = coefficients[14];
    x3y3z0 = coefficients[15];
    x0y0z1 = coefficients[16];
    x1y0z1 = coefficients[17];
    x2y0z1 = coefficients[18];
    x3y0z1 = coefficients[19];
    x0y1z1 = coefficients[20];
    x1y1z1 = coefficients[21];
    x2y1z1 = coefficients[22];
    x3y1z1 = coefficients[23];
    x0y2z1 = coefficients[24];
    x1y2z1 = coefficients[25];
    x2y2z1 = coefficients[26];
    x3y2z1 = coefficients[27];
    x0y3z1 = coefficients[28];
    x1y3z1 = coefficients[29];
    x2y3z1 = coefficients[30];
    x3y3z1 = coefficients[31];
    x0y0z2 = coefficients[32];
    x1y0z2 = coefficients[33];
    x2y0z2 = coefficients[34];
    x3y0z2 = coefficients[35];
    x0y1z2 = coefficients[36];
    x1y1z2 = coefficients[37];
    x2y1z2 = coefficients[38];
    x3y1z2 = coefficients[39];
    x0y2z2 = coefficients[40];
    x1y2z2 = coefficients[41];
    x2y2z2 = coefficients[42];
    x3y2z2 = coefficients[43];
    x0y3z2 = coefficients[44];
    x1y3z2 = coefficients[45];
    x2y3z2 = coefficients[46];
    x3y3z2 = coefficients[47];
    x0y0z3 = coefficients[48];
    x1y0z3 = coefficients[49];
    x2y0z3 = coefficients[50];
    x3y0z3 = coefficients[51];
    x0y1z3 = coefficients[52];
    x1y1z3 = coefficients[53];
    x2y1z3 = coefficients[54];
    x3y1z3 = coefficients[55];
    x0y2z3 = coefficients[56];
    x1y2z3 = coefficients[57];
    x2y2z3 = coefficients[58];
    x3y2z3 = coefficients[59];
    x0y3z3 = coefficients[60];
    x1y3z3 = coefficients[61];
    x2y3z3 = coefficients[62];
    x3y3z3 = coefficients[63];
  }

  /**
   * Create a scaled copy instance.
   *
   * @param source the source.
   * @param scale the scale.
   */
  DoubleCubicSplineData(DoubleCubicSplineData source, double scale) {
    x0y0z0 = source.x0y0z0 * scale;
    x1y0z0 = source.x1y0z0 * scale;
    x2y0z0 = source.x2y0z0 * scale;
    x3y0z0 = source.x3y0z0 * scale;
    x0y1z0 = source.x0y1z0 * scale;
    x1y1z0 = source.x1y1z0 * scale;
    x2y1z0 = source.x2y1z0 * scale;
    x3y1z0 = source.x3y1z0 * scale;
    x0y2z0 = source.x0y2z0 * scale;
    x1y2z0 = source.x1y2z0 * scale;
    x2y2z0 = source.x2y2z0 * scale;
    x3y2z0 = source.x3y2z0 * scale;
    x0y3z0 = source.x0y3z0 * scale;
    x1y3z0 = source.x1y3z0 * scale;
    x2y3z0 = source.x2y3z0 * scale;
    x3y3z0 = source.x3y3z0 * scale;
    x0y0z1 = source.x0y0z1 * scale;
    x1y0z1 = source.x1y0z1 * scale;
    x2y0z1 = source.x2y0z1 * scale;
    x3y0z1 = source.x3y0z1 * scale;
    x0y1z1 = source.x0y1z1 * scale;
    x1y1z1 = source.x1y1z1 * scale;
    x2y1z1 = source.x2y1z1 * scale;
    x3y1z1 = source.x3y1z1 * scale;
    x0y2z1 = source.x0y2z1 * scale;
    x1y2z1 = source.x1y2z1 * scale;
    x2y2z1 = source.x2y2z1 * scale;
    x3y2z1 = source.x3y2z1 * scale;
    x0y3z1 = source.x0y3z1 * scale;
    x1y3z1 = source.x1y3z1 * scale;
    x2y3z1 = source.x2y3z1 * scale;
    x3y3z1 = source.x3y3z1 * scale;
    x0y0z2 = source.x0y0z2 * scale;
    x1y0z2 = source.x1y0z2 * scale;
    x2y0z2 = source.x2y0z2 * scale;
    x3y0z2 = source.x3y0z2 * scale;
    x0y1z2 = source.x0y1z2 * scale;
    x1y1z2 = source.x1y1z2 * scale;
    x2y1z2 = source.x2y1z2 * scale;
    x3y1z2 = source.x3y1z2 * scale;
    x0y2z2 = source.x0y2z2 * scale;
    x1y2z2 = source.x1y2z2 * scale;
    x2y2z2 = source.x2y2z2 * scale;
    x3y2z2 = source.x3y2z2 * scale;
    x0y3z2 = source.x0y3z2 * scale;
    x1y3z2 = source.x1y3z2 * scale;
    x2y3z2 = source.x2y3z2 * scale;
    x3y3z2 = source.x3y3z2 * scale;
    x0y0z3 = source.x0y0z3 * scale;
    x1y0z3 = source.x1y0z3 * scale;
    x2y0z3 = source.x2y0z3 * scale;
    x3y0z3 = source.x3y0z3 * scale;
    x0y1z3 = source.x0y1z3 * scale;
    x1y1z3 = source.x1y1z3 * scale;
    x2y1z3 = source.x2y1z3 * scale;
    x3y1z3 = source.x3y1z3 * scale;
    x0y2z3 = source.x0y2z3 * scale;
    x1y2z3 = source.x1y2z3 * scale;
    x2y2z3 = source.x2y2z3 * scale;
    x3y2z3 = source.x3y2z3 * scale;
    x0y3z3 = source.x0y3z3 * scale;
    x1y3z3 = source.x1y3z3 * scale;
    x2y3z3 = source.x2y3z3 * scale;
    x3y3z3 = source.x3y3z3 * scale;
  }

  /**
   * Scale the coefficients.
   *
   * @param scale the scale
   * @return the scaled coefficients
   * @see CustomTricubicFunction
   */
  public DoubleCubicSplineData scale(double scale) {
    return new DoubleCubicSplineData(this, scale);
  }

  /**
   * Write the 64 coefficient to a provided array.
   *
   * <p>The data represents z^a * y^b * x^c with a,b,c in [0, 3].
   *
   * <pre>
   * final int N = 4;
   * int i = 0;
   * for (int a = 0; a &lt; N; a++) {
   *   for (int b = 0; b &lt; N; b++) {
   *     for (int c = 0; c &lt; N; c++) {
   *       coefficient[i++] = z ^ a * y ^ b * x ^ c;
   *     }
   *   }  
   * }
   * </pre>
   *
   * @param coefficients the coefficients (must be allocated to {@code length >= 64})
   */
  public void toArray(double[] coefficients) {
    coefficients[0] = x0y0z0;
    coefficients[1] = x1y0z0;
    coefficients[2] = x2y0z0;
    coefficients[3] = x3y0z0;
    coefficients[4] = x0y1z0;
    coefficients[5] = x1y1z0;
    coefficients[6] = x2y1z0;
    coefficients[7] = x3y1z0;
    coefficients[8] = x0y2z0;
    coefficients[9] = x1y2z0;
    coefficients[10] = x2y2z0;
    coefficients[11] = x3y2z0;
    coefficients[12] = x0y3z0;
    coefficients[13] = x1y3z0;
    coefficients[14] = x2y3z0;
    coefficients[15] = x3y3z0;
    coefficients[16] = x0y0z1;
    coefficients[17] = x1y0z1;
    coefficients[18] = x2y0z1;
    coefficients[19] = x3y0z1;
    coefficients[20] = x0y1z1;
    coefficients[21] = x1y1z1;
    coefficients[22] = x2y1z1;
    coefficients[23] = x3y1z1;
    coefficients[24] = x0y2z1;
    coefficients[25] = x1y2z1;
    coefficients[26] = x2y2z1;
    coefficients[27] = x3y2z1;
    coefficients[28] = x0y3z1;
    coefficients[29] = x1y3z1;
    coefficients[30] = x2y3z1;
    coefficients[31] = x3y3z1;
    coefficients[32] = x0y0z2;
    coefficients[33] = x1y0z2;
    coefficients[34] = x2y0z2;
    coefficients[35] = x3y0z2;
    coefficients[36] = x0y1z2;
    coefficients[37] = x1y1z2;
    coefficients[38] = x2y1z2;
    coefficients[39] = x3y1z2;
    coefficients[40] = x0y2z2;
    coefficients[41] = x1y2z2;
    coefficients[42] = x2y2z2;
    coefficients[43] = x3y2z2;
    coefficients[44] = x0y3z2;
    coefficients[45] = x1y3z2;
    coefficients[46] = x2y3z2;
    coefficients[47] = x3y3z2;
    coefficients[48] = x0y0z3;
    coefficients[49] = x1y0z3;
    coefficients[50] = x2y0z3;
    coefficients[51] = x3y0z3;
    coefficients[52] = x0y1z3;
    coefficients[53] = x1y1z3;
    coefficients[54] = x2y1z3;
    coefficients[55] = x3y1z3;
    coefficients[56] = x0y2z3;
    coefficients[57] = x1y2z3;
    coefficients[58] = x2y2z3;
    coefficients[59] = x3y2z3;
    coefficients[60] = x0y3z3;
    coefficients[61] = x1y3z3;
    coefficients[62] = x2y3z3;
    coefficients[63] = x3y3z3;
  }
}
