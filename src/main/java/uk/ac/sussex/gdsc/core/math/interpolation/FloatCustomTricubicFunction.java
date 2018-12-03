/*-
 * %%Ignore-License
 *
 * GDSC Software
 *
 * This is an extension of the
 * org.apache.commons.math3.analysis.interpolation.TricubicFunction
 *
 * Modifications have been made to allow computation of gradients and computation
 * with pre-computed x,y,z powers.
 *
 * The code is released under the original Apache licence:
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain coeff copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.sussex.gdsc.core.math.interpolation;

/**
 * 3D-spline function using single precision float values to store the coefficients. This reduces
 * the memory required to store the function.
 *
 * <p>Not all computations use exclusively float precision. The computations using the power table
 * use float computation and should show the largest speed benefit over the double precision counter
 * part.
 */
public class FloatCustomTricubicFunction extends CustomTricubicFunction {
  /** The 64 coefficients (a) for the tri-cubic function. */
  private final float[] coeff;

  @Override
  public double[] getCoefficients() {
    return toDouble(coeff);
  }

  @Override
  public double get(int index) {
    return coeff[index];
  }

  @Override
  public float getf(int index) {
    return coeff[index];
  }

  @Override
  public void scale(double scale) {
    for (int i = 0; i < 64; i++) {
      coeff[i] *= scale;
    }
  }

  /**
   * Instantiates coeff new float custom tricubic function.
   *
   * @param coefficients List of spline coefficients.
   */
  FloatCustomTricubicFunction(double[] coefficients) {
    this.coeff = toFloat(coefficients);
  }

  /**
   * Instantiates coeff new float custom tricubic function.
   *
   * @param coefficients List of spline coefficients.
   */
  FloatCustomTricubicFunction(float[] coefficients) {
    this.coeff = coefficients;
  }

  @Override
  public boolean isSinglePrecision() {
    return true;
  }

  @Override
  public CustomTricubicFunction toSinglePrecision() {
    return this;
  }

  @Override
  public CustomTricubicFunction toDoublePrecision() {
    return new DoubleCustomTricubicFunction(coeff);
  }

  @Override
  public CustomTricubicFunction copy() {
    return new FloatCustomTricubicFunction(coeff.clone());
  }

  // XXX - Copy from DoubleCustomTricubicFunction after here

  @Override
  public double value000() {
    return coeff[0];
  }

  @Override
  public double value000(double[] derivative1) {
    derivative1[0] = coeff[1];
    derivative1[1] = coeff[4];
    derivative1[2] = coeff[16];
    return coeff[0];
  }

  @Override
  public double value000(double[] derivative1, double[] derivative2) {
    derivative1[0] = coeff[1];
    derivative1[1] = coeff[4];
    derivative1[2] = coeff[16];
    derivative2[0] = 2 * coeff[2];
    derivative2[1] = 2 * coeff[8];
    derivative2[2] = 2 * coeff[32];
    return coeff[0];
  }

  // Allow the working variables for the power computation
  // to be declared at the top of the method
  // CHECKSTYLE.OFF: VariableDeclarationUsageDistance

  @Override
  protected double value0(final double[] powerX, final double[] powerY, final double[] powerZ) {
    double powerZpowerY;
    double result = 0;

    result += coeff[0];
    result += powerX[0] * coeff[1];
    result += powerX[1] * coeff[2];
    result += powerX[2] * coeff[3];
    result += powerY[0] * coeff[4];
    result += powerY[0] * powerX[0] * coeff[5];
    result += powerY[0] * powerX[1] * coeff[6];
    result += powerY[0] * powerX[2] * coeff[7];
    result += powerY[1] * coeff[8];
    result += powerY[1] * powerX[0] * coeff[9];
    result += powerY[1] * powerX[1] * coeff[10];
    result += powerY[1] * powerX[2] * coeff[11];
    result += powerY[2] * coeff[12];
    result += powerY[2] * powerX[0] * coeff[13];
    result += powerY[2] * powerX[1] * coeff[14];
    result += powerY[2] * powerX[2] * coeff[15];
    result += powerZ[0] * coeff[16];
    result += powerZ[0] * powerX[0] * coeff[17];
    result += powerZ[0] * powerX[1] * coeff[18];
    result += powerZ[0] * powerX[2] * coeff[19];
    powerZpowerY = powerZ[0] * powerY[0];
    result += powerZpowerY * coeff[20];
    result += powerZpowerY * powerX[0] * coeff[21];
    result += powerZpowerY * powerX[1] * coeff[22];
    result += powerZpowerY * powerX[2] * coeff[23];
    powerZpowerY = powerZ[0] * powerY[1];
    result += powerZpowerY * coeff[24];
    result += powerZpowerY * powerX[0] * coeff[25];
    result += powerZpowerY * powerX[1] * coeff[26];
    result += powerZpowerY * powerX[2] * coeff[27];
    powerZpowerY = powerZ[0] * powerY[2];
    result += powerZpowerY * coeff[28];
    result += powerZpowerY * powerX[0] * coeff[29];
    result += powerZpowerY * powerX[1] * coeff[30];
    result += powerZpowerY * powerX[2] * coeff[31];
    result += powerZ[1] * coeff[32];
    result += powerZ[1] * powerX[0] * coeff[33];
    result += powerZ[1] * powerX[1] * coeff[34];
    result += powerZ[1] * powerX[2] * coeff[35];
    powerZpowerY = powerZ[1] * powerY[0];
    result += powerZpowerY * coeff[36];
    result += powerZpowerY * powerX[0] * coeff[37];
    result += powerZpowerY * powerX[1] * coeff[38];
    result += powerZpowerY * powerX[2] * coeff[39];
    powerZpowerY = powerZ[1] * powerY[1];
    result += powerZpowerY * coeff[40];
    result += powerZpowerY * powerX[0] * coeff[41];
    result += powerZpowerY * powerX[1] * coeff[42];
    result += powerZpowerY * powerX[2] * coeff[43];
    powerZpowerY = powerZ[1] * powerY[2];
    result += powerZpowerY * coeff[44];
    result += powerZpowerY * powerX[0] * coeff[45];
    result += powerZpowerY * powerX[1] * coeff[46];
    result += powerZpowerY * powerX[2] * coeff[47];
    result += powerZ[2] * coeff[48];
    result += powerZ[2] * powerX[0] * coeff[49];
    result += powerZ[2] * powerX[1] * coeff[50];
    result += powerZ[2] * powerX[2] * coeff[51];
    powerZpowerY = powerZ[2] * powerY[0];
    result += powerZpowerY * coeff[52];
    result += powerZpowerY * powerX[0] * coeff[53];
    result += powerZpowerY * powerX[1] * coeff[54];
    result += powerZpowerY * powerX[2] * coeff[55];
    powerZpowerY = powerZ[2] * powerY[1];
    result += powerZpowerY * coeff[56];
    result += powerZpowerY * powerX[0] * coeff[57];
    result += powerZpowerY * powerX[1] * coeff[58];
    result += powerZpowerY * powerX[2] * coeff[59];
    powerZpowerY = powerZ[2] * powerY[2];
    result += powerZpowerY * coeff[60];
    result += powerZpowerY * powerX[0] * coeff[61];
    result += powerZpowerY * powerX[1] * coeff[62];
    result += powerZpowerY * powerX[2] * coeff[63];

    return result;
  }

  @Override
  protected double value1(final double[] powerX, final double[] powerY, final double[] powerZ,
      final double[] derivative1) {
    double powerZpowerY;
    double powerZpowerYpowerX;
    double result = 0;
    derivative1[0] = 0;
    derivative1[1] = 0;
    derivative1[2] = 0;

    result += coeff[0];
    derivative1[0] += coeff[1];
    derivative1[1] += coeff[4];
    derivative1[2] += coeff[16];
    result += powerX[0] * coeff[1];
    derivative1[0] += 2 * powerX[0] * coeff[2];
    derivative1[1] += powerX[0] * coeff[5];
    derivative1[2] += powerX[0] * coeff[17];
    result += powerX[1] * coeff[2];
    derivative1[0] += 3 * powerX[1] * coeff[3];
    derivative1[1] += powerX[1] * coeff[6];
    derivative1[2] += powerX[1] * coeff[18];
    result += powerX[2] * coeff[3];
    derivative1[1] += powerX[2] * coeff[7];
    derivative1[2] += powerX[2] * coeff[19];
    result += powerY[0] * coeff[4];
    derivative1[0] += powerY[0] * coeff[5];
    derivative1[1] += 2 * powerY[0] * coeff[8];
    derivative1[2] += powerY[0] * coeff[20];
    powerZpowerYpowerX = powerY[0] * powerX[0];
    result += powerZpowerYpowerX * coeff[5];
    derivative1[0] += 2 * powerZpowerYpowerX * coeff[6];
    derivative1[1] += 2 * powerZpowerYpowerX * coeff[9];
    derivative1[2] += powerZpowerYpowerX * coeff[21];
    powerZpowerYpowerX = powerY[0] * powerX[1];
    result += powerZpowerYpowerX * coeff[6];
    derivative1[0] += 3 * powerZpowerYpowerX * coeff[7];
    derivative1[1] += 2 * powerZpowerYpowerX * coeff[10];
    derivative1[2] += powerZpowerYpowerX * coeff[22];
    powerZpowerYpowerX = powerY[0] * powerX[2];
    result += powerZpowerYpowerX * coeff[7];
    derivative1[1] += 2 * powerZpowerYpowerX * coeff[11];
    derivative1[2] += powerZpowerYpowerX * coeff[23];
    result += powerY[1] * coeff[8];
    derivative1[0] += powerY[1] * coeff[9];
    derivative1[1] += 3 * powerY[1] * coeff[12];
    derivative1[2] += powerY[1] * coeff[24];
    powerZpowerYpowerX = powerY[1] * powerX[0];
    result += powerZpowerYpowerX * coeff[9];
    derivative1[0] += 2 * powerZpowerYpowerX * coeff[10];
    derivative1[1] += 3 * powerZpowerYpowerX * coeff[13];
    derivative1[2] += powerZpowerYpowerX * coeff[25];
    powerZpowerYpowerX = powerY[1] * powerX[1];
    result += powerZpowerYpowerX * coeff[10];
    derivative1[0] += 3 * powerZpowerYpowerX * coeff[11];
    derivative1[1] += 3 * powerZpowerYpowerX * coeff[14];
    derivative1[2] += powerZpowerYpowerX * coeff[26];
    powerZpowerYpowerX = powerY[1] * powerX[2];
    result += powerZpowerYpowerX * coeff[11];
    derivative1[1] += 3 * powerZpowerYpowerX * coeff[15];
    derivative1[2] += powerZpowerYpowerX * coeff[27];
    result += powerY[2] * coeff[12];
    derivative1[0] += powerY[2] * coeff[13];
    derivative1[2] += powerY[2] * coeff[28];
    powerZpowerYpowerX = powerY[2] * powerX[0];
    result += powerZpowerYpowerX * coeff[13];
    derivative1[0] += 2 * powerZpowerYpowerX * coeff[14];
    derivative1[2] += powerZpowerYpowerX * coeff[29];
    powerZpowerYpowerX = powerY[2] * powerX[1];
    result += powerZpowerYpowerX * coeff[14];
    derivative1[0] += 3 * powerZpowerYpowerX * coeff[15];
    derivative1[2] += powerZpowerYpowerX * coeff[30];
    powerZpowerYpowerX = powerY[2] * powerX[2];
    result += powerZpowerYpowerX * coeff[15];
    derivative1[2] += powerZpowerYpowerX * coeff[31];
    result += powerZ[0] * coeff[16];
    derivative1[0] += powerZ[0] * coeff[17];
    derivative1[1] += powerZ[0] * coeff[20];
    derivative1[2] += 2 * powerZ[0] * coeff[32];
    powerZpowerYpowerX = powerZ[0] * powerX[0];
    result += powerZpowerYpowerX * coeff[17];
    derivative1[0] += 2 * powerZpowerYpowerX * coeff[18];
    derivative1[1] += powerZpowerYpowerX * coeff[21];
    derivative1[2] += 2 * powerZpowerYpowerX * coeff[33];
    powerZpowerYpowerX = powerZ[0] * powerX[1];
    result += powerZpowerYpowerX * coeff[18];
    derivative1[0] += 3 * powerZpowerYpowerX * coeff[19];
    derivative1[1] += powerZpowerYpowerX * coeff[22];
    derivative1[2] += 2 * powerZpowerYpowerX * coeff[34];
    powerZpowerYpowerX = powerZ[0] * powerX[2];
    result += powerZpowerYpowerX * coeff[19];
    derivative1[1] += powerZpowerYpowerX * coeff[23];
    derivative1[2] += 2 * powerZpowerYpowerX * coeff[35];
    powerZpowerY = powerZ[0] * powerY[0];
    result += powerZpowerY * coeff[20];
    derivative1[0] += powerZpowerY * coeff[21];
    derivative1[1] += 2 * powerZpowerY * coeff[24];
    derivative1[2] += 2 * powerZpowerY * coeff[36];
    powerZpowerYpowerX = powerZpowerY * powerX[0];
    result += powerZpowerYpowerX * coeff[21];
    derivative1[0] += 2 * powerZpowerYpowerX * coeff[22];
    derivative1[1] += 2 * powerZpowerYpowerX * coeff[25];
    derivative1[2] += 2 * powerZpowerYpowerX * coeff[37];
    powerZpowerYpowerX = powerZpowerY * powerX[1];
    result += powerZpowerYpowerX * coeff[22];
    derivative1[0] += 3 * powerZpowerYpowerX * coeff[23];
    derivative1[1] += 2 * powerZpowerYpowerX * coeff[26];
    derivative1[2] += 2 * powerZpowerYpowerX * coeff[38];
    powerZpowerYpowerX = powerZpowerY * powerX[2];
    result += powerZpowerYpowerX * coeff[23];
    derivative1[1] += 2 * powerZpowerYpowerX * coeff[27];
    derivative1[2] += 2 * powerZpowerYpowerX * coeff[39];
    powerZpowerY = powerZ[0] * powerY[1];
    result += powerZpowerY * coeff[24];
    derivative1[0] += powerZpowerY * coeff[25];
    derivative1[1] += 3 * powerZpowerY * coeff[28];
    derivative1[2] += 2 * powerZpowerY * coeff[40];
    powerZpowerYpowerX = powerZpowerY * powerX[0];
    result += powerZpowerYpowerX * coeff[25];
    derivative1[0] += 2 * powerZpowerYpowerX * coeff[26];
    derivative1[1] += 3 * powerZpowerYpowerX * coeff[29];
    derivative1[2] += 2 * powerZpowerYpowerX * coeff[41];
    powerZpowerYpowerX = powerZpowerY * powerX[1];
    result += powerZpowerYpowerX * coeff[26];
    derivative1[0] += 3 * powerZpowerYpowerX * coeff[27];
    derivative1[1] += 3 * powerZpowerYpowerX * coeff[30];
    derivative1[2] += 2 * powerZpowerYpowerX * coeff[42];
    powerZpowerYpowerX = powerZpowerY * powerX[2];
    result += powerZpowerYpowerX * coeff[27];
    derivative1[1] += 3 * powerZpowerYpowerX * coeff[31];
    derivative1[2] += 2 * powerZpowerYpowerX * coeff[43];
    powerZpowerY = powerZ[0] * powerY[2];
    result += powerZpowerY * coeff[28];
    derivative1[0] += powerZpowerY * coeff[29];
    derivative1[2] += 2 * powerZpowerY * coeff[44];
    powerZpowerYpowerX = powerZpowerY * powerX[0];
    result += powerZpowerYpowerX * coeff[29];
    derivative1[0] += 2 * powerZpowerYpowerX * coeff[30];
    derivative1[2] += 2 * powerZpowerYpowerX * coeff[45];
    powerZpowerYpowerX = powerZpowerY * powerX[1];
    result += powerZpowerYpowerX * coeff[30];
    derivative1[0] += 3 * powerZpowerYpowerX * coeff[31];
    derivative1[2] += 2 * powerZpowerYpowerX * coeff[46];
    powerZpowerYpowerX = powerZpowerY * powerX[2];
    result += powerZpowerYpowerX * coeff[31];
    derivative1[2] += 2 * powerZpowerYpowerX * coeff[47];
    result += powerZ[1] * coeff[32];
    derivative1[0] += powerZ[1] * coeff[33];
    derivative1[1] += powerZ[1] * coeff[36];
    derivative1[2] += 3 * powerZ[1] * coeff[48];
    powerZpowerYpowerX = powerZ[1] * powerX[0];
    result += powerZpowerYpowerX * coeff[33];
    derivative1[0] += 2 * powerZpowerYpowerX * coeff[34];
    derivative1[1] += powerZpowerYpowerX * coeff[37];
    derivative1[2] += 3 * powerZpowerYpowerX * coeff[49];
    powerZpowerYpowerX = powerZ[1] * powerX[1];
    result += powerZpowerYpowerX * coeff[34];
    derivative1[0] += 3 * powerZpowerYpowerX * coeff[35];
    derivative1[1] += powerZpowerYpowerX * coeff[38];
    derivative1[2] += 3 * powerZpowerYpowerX * coeff[50];
    powerZpowerYpowerX = powerZ[1] * powerX[2];
    result += powerZpowerYpowerX * coeff[35];
    derivative1[1] += powerZpowerYpowerX * coeff[39];
    derivative1[2] += 3 * powerZpowerYpowerX * coeff[51];
    powerZpowerY = powerZ[1] * powerY[0];
    result += powerZpowerY * coeff[36];
    derivative1[0] += powerZpowerY * coeff[37];
    derivative1[1] += 2 * powerZpowerY * coeff[40];
    derivative1[2] += 3 * powerZpowerY * coeff[52];
    powerZpowerYpowerX = powerZpowerY * powerX[0];
    result += powerZpowerYpowerX * coeff[37];
    derivative1[0] += 2 * powerZpowerYpowerX * coeff[38];
    derivative1[1] += 2 * powerZpowerYpowerX * coeff[41];
    derivative1[2] += 3 * powerZpowerYpowerX * coeff[53];
    powerZpowerYpowerX = powerZpowerY * powerX[1];
    result += powerZpowerYpowerX * coeff[38];
    derivative1[0] += 3 * powerZpowerYpowerX * coeff[39];
    derivative1[1] += 2 * powerZpowerYpowerX * coeff[42];
    derivative1[2] += 3 * powerZpowerYpowerX * coeff[54];
    powerZpowerYpowerX = powerZpowerY * powerX[2];
    result += powerZpowerYpowerX * coeff[39];
    derivative1[1] += 2 * powerZpowerYpowerX * coeff[43];
    derivative1[2] += 3 * powerZpowerYpowerX * coeff[55];
    powerZpowerY = powerZ[1] * powerY[1];
    result += powerZpowerY * coeff[40];
    derivative1[0] += powerZpowerY * coeff[41];
    derivative1[1] += 3 * powerZpowerY * coeff[44];
    derivative1[2] += 3 * powerZpowerY * coeff[56];
    powerZpowerYpowerX = powerZpowerY * powerX[0];
    result += powerZpowerYpowerX * coeff[41];
    derivative1[0] += 2 * powerZpowerYpowerX * coeff[42];
    derivative1[1] += 3 * powerZpowerYpowerX * coeff[45];
    derivative1[2] += 3 * powerZpowerYpowerX * coeff[57];
    powerZpowerYpowerX = powerZpowerY * powerX[1];
    result += powerZpowerYpowerX * coeff[42];
    derivative1[0] += 3 * powerZpowerYpowerX * coeff[43];
    derivative1[1] += 3 * powerZpowerYpowerX * coeff[46];
    derivative1[2] += 3 * powerZpowerYpowerX * coeff[58];
    powerZpowerYpowerX = powerZpowerY * powerX[2];
    result += powerZpowerYpowerX * coeff[43];
    derivative1[1] += 3 * powerZpowerYpowerX * coeff[47];
    derivative1[2] += 3 * powerZpowerYpowerX * coeff[59];
    powerZpowerY = powerZ[1] * powerY[2];
    result += powerZpowerY * coeff[44];
    derivative1[0] += powerZpowerY * coeff[45];
    derivative1[2] += 3 * powerZpowerY * coeff[60];
    powerZpowerYpowerX = powerZpowerY * powerX[0];
    result += powerZpowerYpowerX * coeff[45];
    derivative1[0] += 2 * powerZpowerYpowerX * coeff[46];
    derivative1[2] += 3 * powerZpowerYpowerX * coeff[61];
    powerZpowerYpowerX = powerZpowerY * powerX[1];
    result += powerZpowerYpowerX * coeff[46];
    derivative1[0] += 3 * powerZpowerYpowerX * coeff[47];
    derivative1[2] += 3 * powerZpowerYpowerX * coeff[62];
    powerZpowerYpowerX = powerZpowerY * powerX[2];
    result += powerZpowerYpowerX * coeff[47];
    derivative1[2] += 3 * powerZpowerYpowerX * coeff[63];
    result += powerZ[2] * coeff[48];
    derivative1[0] += powerZ[2] * coeff[49];
    derivative1[1] += powerZ[2] * coeff[52];
    powerZpowerYpowerX = powerZ[2] * powerX[0];
    result += powerZpowerYpowerX * coeff[49];
    derivative1[0] += 2 * powerZpowerYpowerX * coeff[50];
    derivative1[1] += powerZpowerYpowerX * coeff[53];
    powerZpowerYpowerX = powerZ[2] * powerX[1];
    result += powerZpowerYpowerX * coeff[50];
    derivative1[0] += 3 * powerZpowerYpowerX * coeff[51];
    derivative1[1] += powerZpowerYpowerX * coeff[54];
    powerZpowerYpowerX = powerZ[2] * powerX[2];
    result += powerZpowerYpowerX * coeff[51];
    derivative1[1] += powerZpowerYpowerX * coeff[55];
    powerZpowerY = powerZ[2] * powerY[0];
    result += powerZpowerY * coeff[52];
    derivative1[0] += powerZpowerY * coeff[53];
    derivative1[1] += 2 * powerZpowerY * coeff[56];
    powerZpowerYpowerX = powerZpowerY * powerX[0];
    result += powerZpowerYpowerX * coeff[53];
    derivative1[0] += 2 * powerZpowerYpowerX * coeff[54];
    derivative1[1] += 2 * powerZpowerYpowerX * coeff[57];
    powerZpowerYpowerX = powerZpowerY * powerX[1];
    result += powerZpowerYpowerX * coeff[54];
    derivative1[0] += 3 * powerZpowerYpowerX * coeff[55];
    derivative1[1] += 2 * powerZpowerYpowerX * coeff[58];
    powerZpowerYpowerX = powerZpowerY * powerX[2];
    result += powerZpowerYpowerX * coeff[55];
    derivative1[1] += 2 * powerZpowerYpowerX * coeff[59];
    powerZpowerY = powerZ[2] * powerY[1];
    result += powerZpowerY * coeff[56];
    derivative1[0] += powerZpowerY * coeff[57];
    derivative1[1] += 3 * powerZpowerY * coeff[60];
    powerZpowerYpowerX = powerZpowerY * powerX[0];
    result += powerZpowerYpowerX * coeff[57];
    derivative1[0] += 2 * powerZpowerYpowerX * coeff[58];
    derivative1[1] += 3 * powerZpowerYpowerX * coeff[61];
    powerZpowerYpowerX = powerZpowerY * powerX[1];
    result += powerZpowerYpowerX * coeff[58];
    derivative1[0] += 3 * powerZpowerYpowerX * coeff[59];
    derivative1[1] += 3 * powerZpowerYpowerX * coeff[62];
    powerZpowerYpowerX = powerZpowerY * powerX[2];
    result += powerZpowerYpowerX * coeff[59];
    derivative1[1] += 3 * powerZpowerYpowerX * coeff[63];
    powerZpowerY = powerZ[2] * powerY[2];
    result += powerZpowerY * coeff[60];
    derivative1[0] += powerZpowerY * coeff[61];
    powerZpowerYpowerX = powerZpowerY * powerX[0];
    result += powerZpowerYpowerX * coeff[61];
    derivative1[0] += 2 * powerZpowerYpowerX * coeff[62];
    powerZpowerYpowerX = powerZpowerY * powerX[1];
    result += powerZpowerYpowerX * coeff[62];
    derivative1[0] += 3 * powerZpowerYpowerX * coeff[63];
    powerZpowerYpowerX = powerZpowerY * powerX[2];
    result += powerZpowerYpowerX * coeff[63];

    return result;
  }

  @Override
  protected double value2(final double[] powerX, final double[] powerY, final double[] powerZ,
      final double[] derivative1, double[] derivative2) {
    double powerZpowerY;
    double powerZpowerYpowerX;
    double result = 0;
    derivative1[0] = 0;
    derivative1[1] = 0;
    derivative1[2] = 0;
    derivative2[0] = 0;
    derivative2[1] = 0;
    derivative2[2] = 0;

    result += coeff[0];
    derivative1[0] += coeff[1];
    derivative2[0] += 2 * coeff[2];
    derivative1[1] += coeff[4];
    derivative2[1] += 2 * coeff[8];
    derivative1[2] += coeff[16];
    derivative2[2] += 2 * coeff[32];
    result += powerX[0] * coeff[1];
    derivative1[0] += 2 * powerX[0] * coeff[2];
    derivative2[0] += 6 * powerX[0] * coeff[3];
    derivative1[1] += powerX[0] * coeff[5];
    derivative2[1] += 2 * powerX[0] * coeff[9];
    derivative1[2] += powerX[0] * coeff[17];
    derivative2[2] += 2 * powerX[0] * coeff[33];
    result += powerX[1] * coeff[2];
    derivative1[0] += 3 * powerX[1] * coeff[3];
    derivative1[1] += powerX[1] * coeff[6];
    derivative2[1] += 2 * powerX[1] * coeff[10];
    derivative1[2] += powerX[1] * coeff[18];
    derivative2[2] += 2 * powerX[1] * coeff[34];
    result += powerX[2] * coeff[3];
    derivative1[1] += powerX[2] * coeff[7];
    derivative2[1] += 2 * powerX[2] * coeff[11];
    derivative1[2] += powerX[2] * coeff[19];
    derivative2[2] += 2 * powerX[2] * coeff[35];
    result += powerY[0] * coeff[4];
    derivative1[0] += powerY[0] * coeff[5];
    derivative2[0] += 2 * powerY[0] * coeff[6];
    derivative1[1] += 2 * powerY[0] * coeff[8];
    derivative2[1] += 6 * powerY[0] * coeff[12];
    derivative1[2] += powerY[0] * coeff[20];
    derivative2[2] += 2 * powerY[0] * coeff[36];
    powerZpowerYpowerX = powerY[0] * powerX[0];
    result += powerZpowerYpowerX * coeff[5];
    derivative1[0] += 2 * powerZpowerYpowerX * coeff[6];
    derivative2[0] += 6 * powerZpowerYpowerX * coeff[7];
    derivative1[1] += 2 * powerZpowerYpowerX * coeff[9];
    derivative2[1] += 6 * powerZpowerYpowerX * coeff[13];
    derivative1[2] += powerZpowerYpowerX * coeff[21];
    derivative2[2] += 2 * powerZpowerYpowerX * coeff[37];
    powerZpowerYpowerX = powerY[0] * powerX[1];
    result += powerZpowerYpowerX * coeff[6];
    derivative1[0] += 3 * powerZpowerYpowerX * coeff[7];
    derivative1[1] += 2 * powerZpowerYpowerX * coeff[10];
    derivative2[1] += 6 * powerZpowerYpowerX * coeff[14];
    derivative1[2] += powerZpowerYpowerX * coeff[22];
    derivative2[2] += 2 * powerZpowerYpowerX * coeff[38];
    powerZpowerYpowerX = powerY[0] * powerX[2];
    result += powerZpowerYpowerX * coeff[7];
    derivative1[1] += 2 * powerZpowerYpowerX * coeff[11];
    derivative2[1] += 6 * powerZpowerYpowerX * coeff[15];
    derivative1[2] += powerZpowerYpowerX * coeff[23];
    derivative2[2] += 2 * powerZpowerYpowerX * coeff[39];
    result += powerY[1] * coeff[8];
    derivative1[0] += powerY[1] * coeff[9];
    derivative2[0] += 2 * powerY[1] * coeff[10];
    derivative1[1] += 3 * powerY[1] * coeff[12];
    derivative1[2] += powerY[1] * coeff[24];
    derivative2[2] += 2 * powerY[1] * coeff[40];
    powerZpowerYpowerX = powerY[1] * powerX[0];
    result += powerZpowerYpowerX * coeff[9];
    derivative1[0] += 2 * powerZpowerYpowerX * coeff[10];
    derivative2[0] += 6 * powerZpowerYpowerX * coeff[11];
    derivative1[1] += 3 * powerZpowerYpowerX * coeff[13];
    derivative1[2] += powerZpowerYpowerX * coeff[25];
    derivative2[2] += 2 * powerZpowerYpowerX * coeff[41];
    powerZpowerYpowerX = powerY[1] * powerX[1];
    result += powerZpowerYpowerX * coeff[10];
    derivative1[0] += 3 * powerZpowerYpowerX * coeff[11];
    derivative1[1] += 3 * powerZpowerYpowerX * coeff[14];
    derivative1[2] += powerZpowerYpowerX * coeff[26];
    derivative2[2] += 2 * powerZpowerYpowerX * coeff[42];
    powerZpowerYpowerX = powerY[1] * powerX[2];
    result += powerZpowerYpowerX * coeff[11];
    derivative1[1] += 3 * powerZpowerYpowerX * coeff[15];
    derivative1[2] += powerZpowerYpowerX * coeff[27];
    derivative2[2] += 2 * powerZpowerYpowerX * coeff[43];
    result += powerY[2] * coeff[12];
    derivative1[0] += powerY[2] * coeff[13];
    derivative2[0] += 2 * powerY[2] * coeff[14];
    derivative1[2] += powerY[2] * coeff[28];
    derivative2[2] += 2 * powerY[2] * coeff[44];
    powerZpowerYpowerX = powerY[2] * powerX[0];
    result += powerZpowerYpowerX * coeff[13];
    derivative1[0] += 2 * powerZpowerYpowerX * coeff[14];
    derivative2[0] += 6 * powerZpowerYpowerX * coeff[15];
    derivative1[2] += powerZpowerYpowerX * coeff[29];
    derivative2[2] += 2 * powerZpowerYpowerX * coeff[45];
    powerZpowerYpowerX = powerY[2] * powerX[1];
    result += powerZpowerYpowerX * coeff[14];
    derivative1[0] += 3 * powerZpowerYpowerX * coeff[15];
    derivative1[2] += powerZpowerYpowerX * coeff[30];
    derivative2[2] += 2 * powerZpowerYpowerX * coeff[46];
    powerZpowerYpowerX = powerY[2] * powerX[2];
    result += powerZpowerYpowerX * coeff[15];
    derivative1[2] += powerZpowerYpowerX * coeff[31];
    derivative2[2] += 2 * powerZpowerYpowerX * coeff[47];
    result += powerZ[0] * coeff[16];
    derivative1[0] += powerZ[0] * coeff[17];
    derivative2[0] += 2 * powerZ[0] * coeff[18];
    derivative1[1] += powerZ[0] * coeff[20];
    derivative2[1] += 2 * powerZ[0] * coeff[24];
    derivative1[2] += 2 * powerZ[0] * coeff[32];
    derivative2[2] += 6 * powerZ[0] * coeff[48];
    powerZpowerYpowerX = powerZ[0] * powerX[0];
    result += powerZpowerYpowerX * coeff[17];
    derivative1[0] += 2 * powerZpowerYpowerX * coeff[18];
    derivative2[0] += 6 * powerZpowerYpowerX * coeff[19];
    derivative1[1] += powerZpowerYpowerX * coeff[21];
    derivative2[1] += 2 * powerZpowerYpowerX * coeff[25];
    derivative1[2] += 2 * powerZpowerYpowerX * coeff[33];
    derivative2[2] += 6 * powerZpowerYpowerX * coeff[49];
    powerZpowerYpowerX = powerZ[0] * powerX[1];
    result += powerZpowerYpowerX * coeff[18];
    derivative1[0] += 3 * powerZpowerYpowerX * coeff[19];
    derivative1[1] += powerZpowerYpowerX * coeff[22];
    derivative2[1] += 2 * powerZpowerYpowerX * coeff[26];
    derivative1[2] += 2 * powerZpowerYpowerX * coeff[34];
    derivative2[2] += 6 * powerZpowerYpowerX * coeff[50];
    powerZpowerYpowerX = powerZ[0] * powerX[2];
    result += powerZpowerYpowerX * coeff[19];
    derivative1[1] += powerZpowerYpowerX * coeff[23];
    derivative2[1] += 2 * powerZpowerYpowerX * coeff[27];
    derivative1[2] += 2 * powerZpowerYpowerX * coeff[35];
    derivative2[2] += 6 * powerZpowerYpowerX * coeff[51];
    powerZpowerY = powerZ[0] * powerY[0];
    result += powerZpowerY * coeff[20];
    derivative1[0] += powerZpowerY * coeff[21];
    derivative2[0] += 2 * powerZpowerY * coeff[22];
    derivative1[1] += 2 * powerZpowerY * coeff[24];
    derivative2[1] += 6 * powerZpowerY * coeff[28];
    derivative1[2] += 2 * powerZpowerY * coeff[36];
    derivative2[2] += 6 * powerZpowerY * coeff[52];
    powerZpowerYpowerX = powerZpowerY * powerX[0];
    result += powerZpowerYpowerX * coeff[21];
    derivative1[0] += 2 * powerZpowerYpowerX * coeff[22];
    derivative2[0] += 6 * powerZpowerYpowerX * coeff[23];
    derivative1[1] += 2 * powerZpowerYpowerX * coeff[25];
    derivative2[1] += 6 * powerZpowerYpowerX * coeff[29];
    derivative1[2] += 2 * powerZpowerYpowerX * coeff[37];
    derivative2[2] += 6 * powerZpowerYpowerX * coeff[53];
    powerZpowerYpowerX = powerZpowerY * powerX[1];
    result += powerZpowerYpowerX * coeff[22];
    derivative1[0] += 3 * powerZpowerYpowerX * coeff[23];
    derivative1[1] += 2 * powerZpowerYpowerX * coeff[26];
    derivative2[1] += 6 * powerZpowerYpowerX * coeff[30];
    derivative1[2] += 2 * powerZpowerYpowerX * coeff[38];
    derivative2[2] += 6 * powerZpowerYpowerX * coeff[54];
    powerZpowerYpowerX = powerZpowerY * powerX[2];
    result += powerZpowerYpowerX * coeff[23];
    derivative1[1] += 2 * powerZpowerYpowerX * coeff[27];
    derivative2[1] += 6 * powerZpowerYpowerX * coeff[31];
    derivative1[2] += 2 * powerZpowerYpowerX * coeff[39];
    derivative2[2] += 6 * powerZpowerYpowerX * coeff[55];
    powerZpowerY = powerZ[0] * powerY[1];
    result += powerZpowerY * coeff[24];
    derivative1[0] += powerZpowerY * coeff[25];
    derivative2[0] += 2 * powerZpowerY * coeff[26];
    derivative1[1] += 3 * powerZpowerY * coeff[28];
    derivative1[2] += 2 * powerZpowerY * coeff[40];
    derivative2[2] += 6 * powerZpowerY * coeff[56];
    powerZpowerYpowerX = powerZpowerY * powerX[0];
    result += powerZpowerYpowerX * coeff[25];
    derivative1[0] += 2 * powerZpowerYpowerX * coeff[26];
    derivative2[0] += 6 * powerZpowerYpowerX * coeff[27];
    derivative1[1] += 3 * powerZpowerYpowerX * coeff[29];
    derivative1[2] += 2 * powerZpowerYpowerX * coeff[41];
    derivative2[2] += 6 * powerZpowerYpowerX * coeff[57];
    powerZpowerYpowerX = powerZpowerY * powerX[1];
    result += powerZpowerYpowerX * coeff[26];
    derivative1[0] += 3 * powerZpowerYpowerX * coeff[27];
    derivative1[1] += 3 * powerZpowerYpowerX * coeff[30];
    derivative1[2] += 2 * powerZpowerYpowerX * coeff[42];
    derivative2[2] += 6 * powerZpowerYpowerX * coeff[58];
    powerZpowerYpowerX = powerZpowerY * powerX[2];
    result += powerZpowerYpowerX * coeff[27];
    derivative1[1] += 3 * powerZpowerYpowerX * coeff[31];
    derivative1[2] += 2 * powerZpowerYpowerX * coeff[43];
    derivative2[2] += 6 * powerZpowerYpowerX * coeff[59];
    powerZpowerY = powerZ[0] * powerY[2];
    result += powerZpowerY * coeff[28];
    derivative1[0] += powerZpowerY * coeff[29];
    derivative2[0] += 2 * powerZpowerY * coeff[30];
    derivative1[2] += 2 * powerZpowerY * coeff[44];
    derivative2[2] += 6 * powerZpowerY * coeff[60];
    powerZpowerYpowerX = powerZpowerY * powerX[0];
    result += powerZpowerYpowerX * coeff[29];
    derivative1[0] += 2 * powerZpowerYpowerX * coeff[30];
    derivative2[0] += 6 * powerZpowerYpowerX * coeff[31];
    derivative1[2] += 2 * powerZpowerYpowerX * coeff[45];
    derivative2[2] += 6 * powerZpowerYpowerX * coeff[61];
    powerZpowerYpowerX = powerZpowerY * powerX[1];
    result += powerZpowerYpowerX * coeff[30];
    derivative1[0] += 3 * powerZpowerYpowerX * coeff[31];
    derivative1[2] += 2 * powerZpowerYpowerX * coeff[46];
    derivative2[2] += 6 * powerZpowerYpowerX * coeff[62];
    powerZpowerYpowerX = powerZpowerY * powerX[2];
    result += powerZpowerYpowerX * coeff[31];
    derivative1[2] += 2 * powerZpowerYpowerX * coeff[47];
    derivative2[2] += 6 * powerZpowerYpowerX * coeff[63];
    result += powerZ[1] * coeff[32];
    derivative1[0] += powerZ[1] * coeff[33];
    derivative2[0] += 2 * powerZ[1] * coeff[34];
    derivative1[1] += powerZ[1] * coeff[36];
    derivative2[1] += 2 * powerZ[1] * coeff[40];
    derivative1[2] += 3 * powerZ[1] * coeff[48];
    powerZpowerYpowerX = powerZ[1] * powerX[0];
    result += powerZpowerYpowerX * coeff[33];
    derivative1[0] += 2 * powerZpowerYpowerX * coeff[34];
    derivative2[0] += 6 * powerZpowerYpowerX * coeff[35];
    derivative1[1] += powerZpowerYpowerX * coeff[37];
    derivative2[1] += 2 * powerZpowerYpowerX * coeff[41];
    derivative1[2] += 3 * powerZpowerYpowerX * coeff[49];
    powerZpowerYpowerX = powerZ[1] * powerX[1];
    result += powerZpowerYpowerX * coeff[34];
    derivative1[0] += 3 * powerZpowerYpowerX * coeff[35];
    derivative1[1] += powerZpowerYpowerX * coeff[38];
    derivative2[1] += 2 * powerZpowerYpowerX * coeff[42];
    derivative1[2] += 3 * powerZpowerYpowerX * coeff[50];
    powerZpowerYpowerX = powerZ[1] * powerX[2];
    result += powerZpowerYpowerX * coeff[35];
    derivative1[1] += powerZpowerYpowerX * coeff[39];
    derivative2[1] += 2 * powerZpowerYpowerX * coeff[43];
    derivative1[2] += 3 * powerZpowerYpowerX * coeff[51];
    powerZpowerY = powerZ[1] * powerY[0];
    result += powerZpowerY * coeff[36];
    derivative1[0] += powerZpowerY * coeff[37];
    derivative2[0] += 2 * powerZpowerY * coeff[38];
    derivative1[1] += 2 * powerZpowerY * coeff[40];
    derivative2[1] += 6 * powerZpowerY * coeff[44];
    derivative1[2] += 3 * powerZpowerY * coeff[52];
    powerZpowerYpowerX = powerZpowerY * powerX[0];
    result += powerZpowerYpowerX * coeff[37];
    derivative1[0] += 2 * powerZpowerYpowerX * coeff[38];
    derivative2[0] += 6 * powerZpowerYpowerX * coeff[39];
    derivative1[1] += 2 * powerZpowerYpowerX * coeff[41];
    derivative2[1] += 6 * powerZpowerYpowerX * coeff[45];
    derivative1[2] += 3 * powerZpowerYpowerX * coeff[53];
    powerZpowerYpowerX = powerZpowerY * powerX[1];
    result += powerZpowerYpowerX * coeff[38];
    derivative1[0] += 3 * powerZpowerYpowerX * coeff[39];
    derivative1[1] += 2 * powerZpowerYpowerX * coeff[42];
    derivative2[1] += 6 * powerZpowerYpowerX * coeff[46];
    derivative1[2] += 3 * powerZpowerYpowerX * coeff[54];
    powerZpowerYpowerX = powerZpowerY * powerX[2];
    result += powerZpowerYpowerX * coeff[39];
    derivative1[1] += 2 * powerZpowerYpowerX * coeff[43];
    derivative2[1] += 6 * powerZpowerYpowerX * coeff[47];
    derivative1[2] += 3 * powerZpowerYpowerX * coeff[55];
    powerZpowerY = powerZ[1] * powerY[1];
    result += powerZpowerY * coeff[40];
    derivative1[0] += powerZpowerY * coeff[41];
    derivative2[0] += 2 * powerZpowerY * coeff[42];
    derivative1[1] += 3 * powerZpowerY * coeff[44];
    derivative1[2] += 3 * powerZpowerY * coeff[56];
    powerZpowerYpowerX = powerZpowerY * powerX[0];
    result += powerZpowerYpowerX * coeff[41];
    derivative1[0] += 2 * powerZpowerYpowerX * coeff[42];
    derivative2[0] += 6 * powerZpowerYpowerX * coeff[43];
    derivative1[1] += 3 * powerZpowerYpowerX * coeff[45];
    derivative1[2] += 3 * powerZpowerYpowerX * coeff[57];
    powerZpowerYpowerX = powerZpowerY * powerX[1];
    result += powerZpowerYpowerX * coeff[42];
    derivative1[0] += 3 * powerZpowerYpowerX * coeff[43];
    derivative1[1] += 3 * powerZpowerYpowerX * coeff[46];
    derivative1[2] += 3 * powerZpowerYpowerX * coeff[58];
    powerZpowerYpowerX = powerZpowerY * powerX[2];
    result += powerZpowerYpowerX * coeff[43];
    derivative1[1] += 3 * powerZpowerYpowerX * coeff[47];
    derivative1[2] += 3 * powerZpowerYpowerX * coeff[59];
    powerZpowerY = powerZ[1] * powerY[2];
    result += powerZpowerY * coeff[44];
    derivative1[0] += powerZpowerY * coeff[45];
    derivative2[0] += 2 * powerZpowerY * coeff[46];
    derivative1[2] += 3 * powerZpowerY * coeff[60];
    powerZpowerYpowerX = powerZpowerY * powerX[0];
    result += powerZpowerYpowerX * coeff[45];
    derivative1[0] += 2 * powerZpowerYpowerX * coeff[46];
    derivative2[0] += 6 * powerZpowerYpowerX * coeff[47];
    derivative1[2] += 3 * powerZpowerYpowerX * coeff[61];
    powerZpowerYpowerX = powerZpowerY * powerX[1];
    result += powerZpowerYpowerX * coeff[46];
    derivative1[0] += 3 * powerZpowerYpowerX * coeff[47];
    derivative1[2] += 3 * powerZpowerYpowerX * coeff[62];
    powerZpowerYpowerX = powerZpowerY * powerX[2];
    result += powerZpowerYpowerX * coeff[47];
    derivative1[2] += 3 * powerZpowerYpowerX * coeff[63];
    result += powerZ[2] * coeff[48];
    derivative1[0] += powerZ[2] * coeff[49];
    derivative2[0] += 2 * powerZ[2] * coeff[50];
    derivative1[1] += powerZ[2] * coeff[52];
    derivative2[1] += 2 * powerZ[2] * coeff[56];
    powerZpowerYpowerX = powerZ[2] * powerX[0];
    result += powerZpowerYpowerX * coeff[49];
    derivative1[0] += 2 * powerZpowerYpowerX * coeff[50];
    derivative2[0] += 6 * powerZpowerYpowerX * coeff[51];
    derivative1[1] += powerZpowerYpowerX * coeff[53];
    derivative2[1] += 2 * powerZpowerYpowerX * coeff[57];
    powerZpowerYpowerX = powerZ[2] * powerX[1];
    result += powerZpowerYpowerX * coeff[50];
    derivative1[0] += 3 * powerZpowerYpowerX * coeff[51];
    derivative1[1] += powerZpowerYpowerX * coeff[54];
    derivative2[1] += 2 * powerZpowerYpowerX * coeff[58];
    powerZpowerYpowerX = powerZ[2] * powerX[2];
    result += powerZpowerYpowerX * coeff[51];
    derivative1[1] += powerZpowerYpowerX * coeff[55];
    derivative2[1] += 2 * powerZpowerYpowerX * coeff[59];
    powerZpowerY = powerZ[2] * powerY[0];
    result += powerZpowerY * coeff[52];
    derivative1[0] += powerZpowerY * coeff[53];
    derivative2[0] += 2 * powerZpowerY * coeff[54];
    derivative1[1] += 2 * powerZpowerY * coeff[56];
    derivative2[1] += 6 * powerZpowerY * coeff[60];
    powerZpowerYpowerX = powerZpowerY * powerX[0];
    result += powerZpowerYpowerX * coeff[53];
    derivative1[0] += 2 * powerZpowerYpowerX * coeff[54];
    derivative2[0] += 6 * powerZpowerYpowerX * coeff[55];
    derivative1[1] += 2 * powerZpowerYpowerX * coeff[57];
    derivative2[1] += 6 * powerZpowerYpowerX * coeff[61];
    powerZpowerYpowerX = powerZpowerY * powerX[1];
    result += powerZpowerYpowerX * coeff[54];
    derivative1[0] += 3 * powerZpowerYpowerX * coeff[55];
    derivative1[1] += 2 * powerZpowerYpowerX * coeff[58];
    derivative2[1] += 6 * powerZpowerYpowerX * coeff[62];
    powerZpowerYpowerX = powerZpowerY * powerX[2];
    result += powerZpowerYpowerX * coeff[55];
    derivative1[1] += 2 * powerZpowerYpowerX * coeff[59];
    derivative2[1] += 6 * powerZpowerYpowerX * coeff[63];
    powerZpowerY = powerZ[2] * powerY[1];
    result += powerZpowerY * coeff[56];
    derivative1[0] += powerZpowerY * coeff[57];
    derivative2[0] += 2 * powerZpowerY * coeff[58];
    derivative1[1] += 3 * powerZpowerY * coeff[60];
    powerZpowerYpowerX = powerZpowerY * powerX[0];
    result += powerZpowerYpowerX * coeff[57];
    derivative1[0] += 2 * powerZpowerYpowerX * coeff[58];
    derivative2[0] += 6 * powerZpowerYpowerX * coeff[59];
    derivative1[1] += 3 * powerZpowerYpowerX * coeff[61];
    powerZpowerYpowerX = powerZpowerY * powerX[1];
    result += powerZpowerYpowerX * coeff[58];
    derivative1[0] += 3 * powerZpowerYpowerX * coeff[59];
    derivative1[1] += 3 * powerZpowerYpowerX * coeff[62];
    powerZpowerYpowerX = powerZpowerY * powerX[2];
    result += powerZpowerYpowerX * coeff[59];
    derivative1[1] += 3 * powerZpowerYpowerX * coeff[63];
    powerZpowerY = powerZ[2] * powerY[2];
    result += powerZpowerY * coeff[60];
    derivative1[0] += powerZpowerY * coeff[61];
    derivative2[0] += 2 * powerZpowerY * coeff[62];
    powerZpowerYpowerX = powerZpowerY * powerX[0];
    result += powerZpowerYpowerX * coeff[61];
    derivative1[0] += 2 * powerZpowerYpowerX * coeff[62];
    derivative2[0] += 6 * powerZpowerYpowerX * coeff[63];
    powerZpowerYpowerX = powerZpowerY * powerX[1];
    result += powerZpowerYpowerX * coeff[62];
    derivative1[0] += 3 * powerZpowerYpowerX * coeff[63];
    powerZpowerYpowerX = powerZpowerY * powerX[2];
    result += powerZpowerYpowerX * coeff[63];

    return result;
  }

  // CHECKSTYLE.ON: VariableDeclarationUsageDistance

  @Override
  public double value(double[] table) {
    return table[0] * coeff[0] + table[1] * coeff[1] + table[2] * coeff[2] + table[3] * coeff[3]
        + table[4] * coeff[4] + table[5] * coeff[5] + table[6] * coeff[6] + table[7] * coeff[7]
        + table[8] * coeff[8] + table[9] * coeff[9] + table[10] * coeff[10] + table[11] * coeff[11]
        + table[12] * coeff[12] + table[13] * coeff[13] + table[14] * coeff[14]
        + table[15] * coeff[15] + table[16] * coeff[16] + table[17] * coeff[17]
        + table[18] * coeff[18] + table[19] * coeff[19] + table[20] * coeff[20]
        + table[21] * coeff[21] + table[22] * coeff[22] + table[23] * coeff[23]
        + table[24] * coeff[24] + table[25] * coeff[25] + table[26] * coeff[26]
        + table[27] * coeff[27] + table[28] * coeff[28] + table[29] * coeff[29]
        + table[30] * coeff[30] + table[31] * coeff[31] + table[32] * coeff[32]
        + table[33] * coeff[33] + table[34] * coeff[34] + table[35] * coeff[35]
        + table[36] * coeff[36] + table[37] * coeff[37] + table[38] * coeff[38]
        + table[39] * coeff[39] + table[40] * coeff[40] + table[41] * coeff[41]
        + table[42] * coeff[42] + table[43] * coeff[43] + table[44] * coeff[44]
        + table[45] * coeff[45] + table[46] * coeff[46] + table[47] * coeff[47]
        + table[48] * coeff[48] + table[49] * coeff[49] + table[50] * coeff[50]
        + table[51] * coeff[51] + table[52] * coeff[52] + table[53] * coeff[53]
        + table[54] * coeff[54] + table[55] * coeff[55] + table[56] * coeff[56]
        + table[57] * coeff[57] + table[58] * coeff[58] + table[59] * coeff[59]
        + table[60] * coeff[60] + table[61] * coeff[61] + table[62] * coeff[62]
        + table[63] * coeff[63];
  }

  @Override
  public double value(float[] table) {
    return table[0] * coeff[0] + table[1] * coeff[1] + table[2] * coeff[2] + table[3] * coeff[3]
        + table[4] * coeff[4] + table[5] * coeff[5] + table[6] * coeff[6] + table[7] * coeff[7]
        + table[8] * coeff[8] + table[9] * coeff[9] + table[10] * coeff[10] + table[11] * coeff[11]
        + table[12] * coeff[12] + table[13] * coeff[13] + table[14] * coeff[14]
        + table[15] * coeff[15] + table[16] * coeff[16] + table[17] * coeff[17]
        + table[18] * coeff[18] + table[19] * coeff[19] + table[20] * coeff[20]
        + table[21] * coeff[21] + table[22] * coeff[22] + table[23] * coeff[23]
        + table[24] * coeff[24] + table[25] * coeff[25] + table[26] * coeff[26]
        + table[27] * coeff[27] + table[28] * coeff[28] + table[29] * coeff[29]
        + table[30] * coeff[30] + table[31] * coeff[31] + table[32] * coeff[32]
        + table[33] * coeff[33] + table[34] * coeff[34] + table[35] * coeff[35]
        + table[36] * coeff[36] + table[37] * coeff[37] + table[38] * coeff[38]
        + table[39] * coeff[39] + table[40] * coeff[40] + table[41] * coeff[41]
        + table[42] * coeff[42] + table[43] * coeff[43] + table[44] * coeff[44]
        + table[45] * coeff[45] + table[46] * coeff[46] + table[47] * coeff[47]
        + table[48] * coeff[48] + table[49] * coeff[49] + table[50] * coeff[50]
        + table[51] * coeff[51] + table[52] * coeff[52] + table[53] * coeff[53]
        + table[54] * coeff[54] + table[55] * coeff[55] + table[56] * coeff[56]
        + table[57] * coeff[57] + table[58] * coeff[58] + table[59] * coeff[59]
        + table[60] * coeff[60] + table[61] * coeff[61] + table[62] * coeff[62]
        + table[63] * coeff[63];
  }

  @Override
  public double value(double[] table, double[] derivative1) {
    derivative1[0] = table[0] * coeff[1] + 2 * table[1] * coeff[2] + 3 * table[2] * coeff[3]
        + table[4] * coeff[5] + 2 * table[5] * coeff[6] + 3 * table[6] * coeff[7]
        + table[8] * coeff[9] + 2 * table[9] * coeff[10] + 3 * table[10] * coeff[11]
        + table[12] * coeff[13] + 2 * table[13] * coeff[14] + 3 * table[14] * coeff[15]
        + table[16] * coeff[17] + 2 * table[17] * coeff[18] + 3 * table[18] * coeff[19]
        + table[20] * coeff[21] + 2 * table[21] * coeff[22] + 3 * table[22] * coeff[23]
        + table[24] * coeff[25] + 2 * table[25] * coeff[26] + 3 * table[26] * coeff[27]
        + table[28] * coeff[29] + 2 * table[29] * coeff[30] + 3 * table[30] * coeff[31]
        + table[32] * coeff[33] + 2 * table[33] * coeff[34] + 3 * table[34] * coeff[35]
        + table[36] * coeff[37] + 2 * table[37] * coeff[38] + 3 * table[38] * coeff[39]
        + table[40] * coeff[41] + 2 * table[41] * coeff[42] + 3 * table[42] * coeff[43]
        + table[44] * coeff[45] + 2 * table[45] * coeff[46] + 3 * table[46] * coeff[47]
        + table[48] * coeff[49] + 2 * table[49] * coeff[50] + 3 * table[50] * coeff[51]
        + table[52] * coeff[53] + 2 * table[53] * coeff[54] + 3 * table[54] * coeff[55]
        + table[56] * coeff[57] + 2 * table[57] * coeff[58] + 3 * table[58] * coeff[59]
        + table[60] * coeff[61] + 2 * table[61] * coeff[62] + 3 * table[62] * coeff[63];
    derivative1[1] = table[0] * coeff[4] + table[1] * coeff[5] + table[2] * coeff[6]
        + table[3] * coeff[7] + 2 * table[4] * coeff[8] + 2 * table[5] * coeff[9]
        + 2 * table[6] * coeff[10] + 2 * table[7] * coeff[11] + 3 * table[8] * coeff[12]
        + 3 * table[9] * coeff[13] + 3 * table[10] * coeff[14] + 3 * table[11] * coeff[15]
        + table[16] * coeff[20] + table[17] * coeff[21] + table[18] * coeff[22]
        + table[19] * coeff[23] + 2 * table[20] * coeff[24] + 2 * table[21] * coeff[25]
        + 2 * table[22] * coeff[26] + 2 * table[23] * coeff[27] + 3 * table[24] * coeff[28]
        + 3 * table[25] * coeff[29] + 3 * table[26] * coeff[30] + 3 * table[27] * coeff[31]
        + table[32] * coeff[36] + table[33] * coeff[37] + table[34] * coeff[38]
        + table[35] * coeff[39] + 2 * table[36] * coeff[40] + 2 * table[37] * coeff[41]
        + 2 * table[38] * coeff[42] + 2 * table[39] * coeff[43] + 3 * table[40] * coeff[44]
        + 3 * table[41] * coeff[45] + 3 * table[42] * coeff[46] + 3 * table[43] * coeff[47]
        + table[48] * coeff[52] + table[49] * coeff[53] + table[50] * coeff[54]
        + table[51] * coeff[55] + 2 * table[52] * coeff[56] + 2 * table[53] * coeff[57]
        + 2 * table[54] * coeff[58] + 2 * table[55] * coeff[59] + 3 * table[56] * coeff[60]
        + 3 * table[57] * coeff[61] + 3 * table[58] * coeff[62] + 3 * table[59] * coeff[63];
    derivative1[2] = table[0] * coeff[16] + table[1] * coeff[17] + table[2] * coeff[18]
        + table[3] * coeff[19] + table[4] * coeff[20] + table[5] * coeff[21] + table[6] * coeff[22]
        + table[7] * coeff[23] + table[8] * coeff[24] + table[9] * coeff[25] + table[10] * coeff[26]
        + table[11] * coeff[27] + table[12] * coeff[28] + table[13] * coeff[29]
        + table[14] * coeff[30] + table[15] * coeff[31] + 2 * table[16] * coeff[32]
        + 2 * table[17] * coeff[33] + 2 * table[18] * coeff[34] + 2 * table[19] * coeff[35]
        + 2 * table[20] * coeff[36] + 2 * table[21] * coeff[37] + 2 * table[22] * coeff[38]
        + 2 * table[23] * coeff[39] + 2 * table[24] * coeff[40] + 2 * table[25] * coeff[41]
        + 2 * table[26] * coeff[42] + 2 * table[27] * coeff[43] + 2 * table[28] * coeff[44]
        + 2 * table[29] * coeff[45] + 2 * table[30] * coeff[46] + 2 * table[31] * coeff[47]
        + 3 * table[32] * coeff[48] + 3 * table[33] * coeff[49] + 3 * table[34] * coeff[50]
        + 3 * table[35] * coeff[51] + 3 * table[36] * coeff[52] + 3 * table[37] * coeff[53]
        + 3 * table[38] * coeff[54] + 3 * table[39] * coeff[55] + 3 * table[40] * coeff[56]
        + 3 * table[41] * coeff[57] + 3 * table[42] * coeff[58] + 3 * table[43] * coeff[59]
        + 3 * table[44] * coeff[60] + 3 * table[45] * coeff[61] + 3 * table[46] * coeff[62]
        + 3 * table[47] * coeff[63];
    return table[0] * coeff[0] + table[1] * coeff[1] + table[2] * coeff[2] + table[3] * coeff[3]
        + table[4] * coeff[4] + table[5] * coeff[5] + table[6] * coeff[6] + table[7] * coeff[7]
        + table[8] * coeff[8] + table[9] * coeff[9] + table[10] * coeff[10] + table[11] * coeff[11]
        + table[12] * coeff[12] + table[13] * coeff[13] + table[14] * coeff[14]
        + table[15] * coeff[15] + table[16] * coeff[16] + table[17] * coeff[17]
        + table[18] * coeff[18] + table[19] * coeff[19] + table[20] * coeff[20]
        + table[21] * coeff[21] + table[22] * coeff[22] + table[23] * coeff[23]
        + table[24] * coeff[24] + table[25] * coeff[25] + table[26] * coeff[26]
        + table[27] * coeff[27] + table[28] * coeff[28] + table[29] * coeff[29]
        + table[30] * coeff[30] + table[31] * coeff[31] + table[32] * coeff[32]
        + table[33] * coeff[33] + table[34] * coeff[34] + table[35] * coeff[35]
        + table[36] * coeff[36] + table[37] * coeff[37] + table[38] * coeff[38]
        + table[39] * coeff[39] + table[40] * coeff[40] + table[41] * coeff[41]
        + table[42] * coeff[42] + table[43] * coeff[43] + table[44] * coeff[44]
        + table[45] * coeff[45] + table[46] * coeff[46] + table[47] * coeff[47]
        + table[48] * coeff[48] + table[49] * coeff[49] + table[50] * coeff[50]
        + table[51] * coeff[51] + table[52] * coeff[52] + table[53] * coeff[53]
        + table[54] * coeff[54] + table[55] * coeff[55] + table[56] * coeff[56]
        + table[57] * coeff[57] + table[58] * coeff[58] + table[59] * coeff[59]
        + table[60] * coeff[60] + table[61] * coeff[61] + table[62] * coeff[62]
        + table[63] * coeff[63];
  }

  @Override
  public double value(float[] table, double[] derivative1) {
    derivative1[0] = table[0] * coeff[1] + 2 * table[1] * coeff[2] + 3 * table[2] * coeff[3]
        + table[4] * coeff[5] + 2 * table[5] * coeff[6] + 3 * table[6] * coeff[7]
        + table[8] * coeff[9] + 2 * table[9] * coeff[10] + 3 * table[10] * coeff[11]
        + table[12] * coeff[13] + 2 * table[13] * coeff[14] + 3 * table[14] * coeff[15]
        + table[16] * coeff[17] + 2 * table[17] * coeff[18] + 3 * table[18] * coeff[19]
        + table[20] * coeff[21] + 2 * table[21] * coeff[22] + 3 * table[22] * coeff[23]
        + table[24] * coeff[25] + 2 * table[25] * coeff[26] + 3 * table[26] * coeff[27]
        + table[28] * coeff[29] + 2 * table[29] * coeff[30] + 3 * table[30] * coeff[31]
        + table[32] * coeff[33] + 2 * table[33] * coeff[34] + 3 * table[34] * coeff[35]
        + table[36] * coeff[37] + 2 * table[37] * coeff[38] + 3 * table[38] * coeff[39]
        + table[40] * coeff[41] + 2 * table[41] * coeff[42] + 3 * table[42] * coeff[43]
        + table[44] * coeff[45] + 2 * table[45] * coeff[46] + 3 * table[46] * coeff[47]
        + table[48] * coeff[49] + 2 * table[49] * coeff[50] + 3 * table[50] * coeff[51]
        + table[52] * coeff[53] + 2 * table[53] * coeff[54] + 3 * table[54] * coeff[55]
        + table[56] * coeff[57] + 2 * table[57] * coeff[58] + 3 * table[58] * coeff[59]
        + table[60] * coeff[61] + 2 * table[61] * coeff[62] + 3 * table[62] * coeff[63];
    derivative1[1] = table[0] * coeff[4] + table[1] * coeff[5] + table[2] * coeff[6]
        + table[3] * coeff[7] + 2 * table[4] * coeff[8] + 2 * table[5] * coeff[9]
        + 2 * table[6] * coeff[10] + 2 * table[7] * coeff[11] + 3 * table[8] * coeff[12]
        + 3 * table[9] * coeff[13] + 3 * table[10] * coeff[14] + 3 * table[11] * coeff[15]
        + table[16] * coeff[20] + table[17] * coeff[21] + table[18] * coeff[22]
        + table[19] * coeff[23] + 2 * table[20] * coeff[24] + 2 * table[21] * coeff[25]
        + 2 * table[22] * coeff[26] + 2 * table[23] * coeff[27] + 3 * table[24] * coeff[28]
        + 3 * table[25] * coeff[29] + 3 * table[26] * coeff[30] + 3 * table[27] * coeff[31]
        + table[32] * coeff[36] + table[33] * coeff[37] + table[34] * coeff[38]
        + table[35] * coeff[39] + 2 * table[36] * coeff[40] + 2 * table[37] * coeff[41]
        + 2 * table[38] * coeff[42] + 2 * table[39] * coeff[43] + 3 * table[40] * coeff[44]
        + 3 * table[41] * coeff[45] + 3 * table[42] * coeff[46] + 3 * table[43] * coeff[47]
        + table[48] * coeff[52] + table[49] * coeff[53] + table[50] * coeff[54]
        + table[51] * coeff[55] + 2 * table[52] * coeff[56] + 2 * table[53] * coeff[57]
        + 2 * table[54] * coeff[58] + 2 * table[55] * coeff[59] + 3 * table[56] * coeff[60]
        + 3 * table[57] * coeff[61] + 3 * table[58] * coeff[62] + 3 * table[59] * coeff[63];
    derivative1[2] = table[0] * coeff[16] + table[1] * coeff[17] + table[2] * coeff[18]
        + table[3] * coeff[19] + table[4] * coeff[20] + table[5] * coeff[21] + table[6] * coeff[22]
        + table[7] * coeff[23] + table[8] * coeff[24] + table[9] * coeff[25] + table[10] * coeff[26]
        + table[11] * coeff[27] + table[12] * coeff[28] + table[13] * coeff[29]
        + table[14] * coeff[30] + table[15] * coeff[31] + 2 * table[16] * coeff[32]
        + 2 * table[17] * coeff[33] + 2 * table[18] * coeff[34] + 2 * table[19] * coeff[35]
        + 2 * table[20] * coeff[36] + 2 * table[21] * coeff[37] + 2 * table[22] * coeff[38]
        + 2 * table[23] * coeff[39] + 2 * table[24] * coeff[40] + 2 * table[25] * coeff[41]
        + 2 * table[26] * coeff[42] + 2 * table[27] * coeff[43] + 2 * table[28] * coeff[44]
        + 2 * table[29] * coeff[45] + 2 * table[30] * coeff[46] + 2 * table[31] * coeff[47]
        + 3 * table[32] * coeff[48] + 3 * table[33] * coeff[49] + 3 * table[34] * coeff[50]
        + 3 * table[35] * coeff[51] + 3 * table[36] * coeff[52] + 3 * table[37] * coeff[53]
        + 3 * table[38] * coeff[54] + 3 * table[39] * coeff[55] + 3 * table[40] * coeff[56]
        + 3 * table[41] * coeff[57] + 3 * table[42] * coeff[58] + 3 * table[43] * coeff[59]
        + 3 * table[44] * coeff[60] + 3 * table[45] * coeff[61] + 3 * table[46] * coeff[62]
        + 3 * table[47] * coeff[63];
    return table[0] * coeff[0] + table[1] * coeff[1] + table[2] * coeff[2] + table[3] * coeff[3]
        + table[4] * coeff[4] + table[5] * coeff[5] + table[6] * coeff[6] + table[7] * coeff[7]
        + table[8] * coeff[8] + table[9] * coeff[9] + table[10] * coeff[10] + table[11] * coeff[11]
        + table[12] * coeff[12] + table[13] * coeff[13] + table[14] * coeff[14]
        + table[15] * coeff[15] + table[16] * coeff[16] + table[17] * coeff[17]
        + table[18] * coeff[18] + table[19] * coeff[19] + table[20] * coeff[20]
        + table[21] * coeff[21] + table[22] * coeff[22] + table[23] * coeff[23]
        + table[24] * coeff[24] + table[25] * coeff[25] + table[26] * coeff[26]
        + table[27] * coeff[27] + table[28] * coeff[28] + table[29] * coeff[29]
        + table[30] * coeff[30] + table[31] * coeff[31] + table[32] * coeff[32]
        + table[33] * coeff[33] + table[34] * coeff[34] + table[35] * coeff[35]
        + table[36] * coeff[36] + table[37] * coeff[37] + table[38] * coeff[38]
        + table[39] * coeff[39] + table[40] * coeff[40] + table[41] * coeff[41]
        + table[42] * coeff[42] + table[43] * coeff[43] + table[44] * coeff[44]
        + table[45] * coeff[45] + table[46] * coeff[46] + table[47] * coeff[47]
        + table[48] * coeff[48] + table[49] * coeff[49] + table[50] * coeff[50]
        + table[51] * coeff[51] + table[52] * coeff[52] + table[53] * coeff[53]
        + table[54] * coeff[54] + table[55] * coeff[55] + table[56] * coeff[56]
        + table[57] * coeff[57] + table[58] * coeff[58] + table[59] * coeff[59]
        + table[60] * coeff[60] + table[61] * coeff[61] + table[62] * coeff[62]
        + table[63] * coeff[63];
  }

  @Override
  public double value(double[] table, double[] table2, double[] table3, double[] derivative1) {
    derivative1[0] = table[0] * coeff[1] + table2[1] * coeff[2] + table3[2] * coeff[3]
        + table[4] * coeff[5] + table2[5] * coeff[6] + table3[6] * coeff[7] + table[8] * coeff[9]
        + table2[9] * coeff[10] + table3[10] * coeff[11] + table[12] * coeff[13]
        + table2[13] * coeff[14] + table3[14] * coeff[15] + table[16] * coeff[17]
        + table2[17] * coeff[18] + table3[18] * coeff[19] + table[20] * coeff[21]
        + table2[21] * coeff[22] + table3[22] * coeff[23] + table[24] * coeff[25]
        + table2[25] * coeff[26] + table3[26] * coeff[27] + table[28] * coeff[29]
        + table2[29] * coeff[30] + table3[30] * coeff[31] + table[32] * coeff[33]
        + table2[33] * coeff[34] + table3[34] * coeff[35] + table[36] * coeff[37]
        + table2[37] * coeff[38] + table3[38] * coeff[39] + table[40] * coeff[41]
        + table2[41] * coeff[42] + table3[42] * coeff[43] + table[44] * coeff[45]
        + table2[45] * coeff[46] + table3[46] * coeff[47] + table[48] * coeff[49]
        + table2[49] * coeff[50] + table3[50] * coeff[51] + table[52] * coeff[53]
        + table2[53] * coeff[54] + table3[54] * coeff[55] + table[56] * coeff[57]
        + table2[57] * coeff[58] + table3[58] * coeff[59] + table[60] * coeff[61]
        + table2[61] * coeff[62] + table3[62] * coeff[63];
    derivative1[1] = table[0] * coeff[4] + table[1] * coeff[5] + table[2] * coeff[6]
        + table[3] * coeff[7] + table2[4] * coeff[8] + table2[5] * coeff[9] + table2[6] * coeff[10]
        + table2[7] * coeff[11] + table3[8] * coeff[12] + table3[9] * coeff[13]
        + table3[10] * coeff[14] + table3[11] * coeff[15] + table[16] * coeff[20]
        + table[17] * coeff[21] + table[18] * coeff[22] + table[19] * coeff[23]
        + table2[20] * coeff[24] + table2[21] * coeff[25] + table2[22] * coeff[26]
        + table2[23] * coeff[27] + table3[24] * coeff[28] + table3[25] * coeff[29]
        + table3[26] * coeff[30] + table3[27] * coeff[31] + table[32] * coeff[36]
        + table[33] * coeff[37] + table[34] * coeff[38] + table[35] * coeff[39]
        + table2[36] * coeff[40] + table2[37] * coeff[41] + table2[38] * coeff[42]
        + table2[39] * coeff[43] + table3[40] * coeff[44] + table3[41] * coeff[45]
        + table3[42] * coeff[46] + table3[43] * coeff[47] + table[48] * coeff[52]
        + table[49] * coeff[53] + table[50] * coeff[54] + table[51] * coeff[55]
        + table2[52] * coeff[56] + table2[53] * coeff[57] + table2[54] * coeff[58]
        + table2[55] * coeff[59] + table3[56] * coeff[60] + table3[57] * coeff[61]
        + table3[58] * coeff[62] + table3[59] * coeff[63];
    derivative1[2] = table[0] * coeff[16] + table[1] * coeff[17] + table[2] * coeff[18]
        + table[3] * coeff[19] + table[4] * coeff[20] + table[5] * coeff[21] + table[6] * coeff[22]
        + table[7] * coeff[23] + table[8] * coeff[24] + table[9] * coeff[25] + table[10] * coeff[26]
        + table[11] * coeff[27] + table[12] * coeff[28] + table[13] * coeff[29]
        + table[14] * coeff[30] + table[15] * coeff[31] + table2[16] * coeff[32]
        + table2[17] * coeff[33] + table2[18] * coeff[34] + table2[19] * coeff[35]
        + table2[20] * coeff[36] + table2[21] * coeff[37] + table2[22] * coeff[38]
        + table2[23] * coeff[39] + table2[24] * coeff[40] + table2[25] * coeff[41]
        + table2[26] * coeff[42] + table2[27] * coeff[43] + table2[28] * coeff[44]
        + table2[29] * coeff[45] + table2[30] * coeff[46] + table2[31] * coeff[47]
        + table3[32] * coeff[48] + table3[33] * coeff[49] + table3[34] * coeff[50]
        + table3[35] * coeff[51] + table3[36] * coeff[52] + table3[37] * coeff[53]
        + table3[38] * coeff[54] + table3[39] * coeff[55] + table3[40] * coeff[56]
        + table3[41] * coeff[57] + table3[42] * coeff[58] + table3[43] * coeff[59]
        + table3[44] * coeff[60] + table3[45] * coeff[61] + table3[46] * coeff[62]
        + table3[47] * coeff[63];
    return table[0] * coeff[0] + table[1] * coeff[1] + table[2] * coeff[2] + table[3] * coeff[3]
        + table[4] * coeff[4] + table[5] * coeff[5] + table[6] * coeff[6] + table[7] * coeff[7]
        + table[8] * coeff[8] + table[9] * coeff[9] + table[10] * coeff[10] + table[11] * coeff[11]
        + table[12] * coeff[12] + table[13] * coeff[13] + table[14] * coeff[14]
        + table[15] * coeff[15] + table[16] * coeff[16] + table[17] * coeff[17]
        + table[18] * coeff[18] + table[19] * coeff[19] + table[20] * coeff[20]
        + table[21] * coeff[21] + table[22] * coeff[22] + table[23] * coeff[23]
        + table[24] * coeff[24] + table[25] * coeff[25] + table[26] * coeff[26]
        + table[27] * coeff[27] + table[28] * coeff[28] + table[29] * coeff[29]
        + table[30] * coeff[30] + table[31] * coeff[31] + table[32] * coeff[32]
        + table[33] * coeff[33] + table[34] * coeff[34] + table[35] * coeff[35]
        + table[36] * coeff[36] + table[37] * coeff[37] + table[38] * coeff[38]
        + table[39] * coeff[39] + table[40] * coeff[40] + table[41] * coeff[41]
        + table[42] * coeff[42] + table[43] * coeff[43] + table[44] * coeff[44]
        + table[45] * coeff[45] + table[46] * coeff[46] + table[47] * coeff[47]
        + table[48] * coeff[48] + table[49] * coeff[49] + table[50] * coeff[50]
        + table[51] * coeff[51] + table[52] * coeff[52] + table[53] * coeff[53]
        + table[54] * coeff[54] + table[55] * coeff[55] + table[56] * coeff[56]
        + table[57] * coeff[57] + table[58] * coeff[58] + table[59] * coeff[59]
        + table[60] * coeff[60] + table[61] * coeff[61] + table[62] * coeff[62]
        + table[63] * coeff[63];

  }

  @Override
  public double value(float[] table, float[] table2, float[] table3, double[] derivative1) {
    derivative1[0] = table[0] * coeff[1] + table2[1] * coeff[2] + table3[2] * coeff[3]
        + table[4] * coeff[5] + table2[5] * coeff[6] + table3[6] * coeff[7] + table[8] * coeff[9]
        + table2[9] * coeff[10] + table3[10] * coeff[11] + table[12] * coeff[13]
        + table2[13] * coeff[14] + table3[14] * coeff[15] + table[16] * coeff[17]
        + table2[17] * coeff[18] + table3[18] * coeff[19] + table[20] * coeff[21]
        + table2[21] * coeff[22] + table3[22] * coeff[23] + table[24] * coeff[25]
        + table2[25] * coeff[26] + table3[26] * coeff[27] + table[28] * coeff[29]
        + table2[29] * coeff[30] + table3[30] * coeff[31] + table[32] * coeff[33]
        + table2[33] * coeff[34] + table3[34] * coeff[35] + table[36] * coeff[37]
        + table2[37] * coeff[38] + table3[38] * coeff[39] + table[40] * coeff[41]
        + table2[41] * coeff[42] + table3[42] * coeff[43] + table[44] * coeff[45]
        + table2[45] * coeff[46] + table3[46] * coeff[47] + table[48] * coeff[49]
        + table2[49] * coeff[50] + table3[50] * coeff[51] + table[52] * coeff[53]
        + table2[53] * coeff[54] + table3[54] * coeff[55] + table[56] * coeff[57]
        + table2[57] * coeff[58] + table3[58] * coeff[59] + table[60] * coeff[61]
        + table2[61] * coeff[62] + table3[62] * coeff[63];
    derivative1[1] = table[0] * coeff[4] + table[1] * coeff[5] + table[2] * coeff[6]
        + table[3] * coeff[7] + table2[4] * coeff[8] + table2[5] * coeff[9] + table2[6] * coeff[10]
        + table2[7] * coeff[11] + table3[8] * coeff[12] + table3[9] * coeff[13]
        + table3[10] * coeff[14] + table3[11] * coeff[15] + table[16] * coeff[20]
        + table[17] * coeff[21] + table[18] * coeff[22] + table[19] * coeff[23]
        + table2[20] * coeff[24] + table2[21] * coeff[25] + table2[22] * coeff[26]
        + table2[23] * coeff[27] + table3[24] * coeff[28] + table3[25] * coeff[29]
        + table3[26] * coeff[30] + table3[27] * coeff[31] + table[32] * coeff[36]
        + table[33] * coeff[37] + table[34] * coeff[38] + table[35] * coeff[39]
        + table2[36] * coeff[40] + table2[37] * coeff[41] + table2[38] * coeff[42]
        + table2[39] * coeff[43] + table3[40] * coeff[44] + table3[41] * coeff[45]
        + table3[42] * coeff[46] + table3[43] * coeff[47] + table[48] * coeff[52]
        + table[49] * coeff[53] + table[50] * coeff[54] + table[51] * coeff[55]
        + table2[52] * coeff[56] + table2[53] * coeff[57] + table2[54] * coeff[58]
        + table2[55] * coeff[59] + table3[56] * coeff[60] + table3[57] * coeff[61]
        + table3[58] * coeff[62] + table3[59] * coeff[63];
    derivative1[2] = table[0] * coeff[16] + table[1] * coeff[17] + table[2] * coeff[18]
        + table[3] * coeff[19] + table[4] * coeff[20] + table[5] * coeff[21] + table[6] * coeff[22]
        + table[7] * coeff[23] + table[8] * coeff[24] + table[9] * coeff[25] + table[10] * coeff[26]
        + table[11] * coeff[27] + table[12] * coeff[28] + table[13] * coeff[29]
        + table[14] * coeff[30] + table[15] * coeff[31] + table2[16] * coeff[32]
        + table2[17] * coeff[33] + table2[18] * coeff[34] + table2[19] * coeff[35]
        + table2[20] * coeff[36] + table2[21] * coeff[37] + table2[22] * coeff[38]
        + table2[23] * coeff[39] + table2[24] * coeff[40] + table2[25] * coeff[41]
        + table2[26] * coeff[42] + table2[27] * coeff[43] + table2[28] * coeff[44]
        + table2[29] * coeff[45] + table2[30] * coeff[46] + table2[31] * coeff[47]
        + table3[32] * coeff[48] + table3[33] * coeff[49] + table3[34] * coeff[50]
        + table3[35] * coeff[51] + table3[36] * coeff[52] + table3[37] * coeff[53]
        + table3[38] * coeff[54] + table3[39] * coeff[55] + table3[40] * coeff[56]
        + table3[41] * coeff[57] + table3[42] * coeff[58] + table3[43] * coeff[59]
        + table3[44] * coeff[60] + table3[45] * coeff[61] + table3[46] * coeff[62]
        + table3[47] * coeff[63];
    return table[0] * coeff[0] + table[1] * coeff[1] + table[2] * coeff[2] + table[3] * coeff[3]
        + table[4] * coeff[4] + table[5] * coeff[5] + table[6] * coeff[6] + table[7] * coeff[7]
        + table[8] * coeff[8] + table[9] * coeff[9] + table[10] * coeff[10] + table[11] * coeff[11]
        + table[12] * coeff[12] + table[13] * coeff[13] + table[14] * coeff[14]
        + table[15] * coeff[15] + table[16] * coeff[16] + table[17] * coeff[17]
        + table[18] * coeff[18] + table[19] * coeff[19] + table[20] * coeff[20]
        + table[21] * coeff[21] + table[22] * coeff[22] + table[23] * coeff[23]
        + table[24] * coeff[24] + table[25] * coeff[25] + table[26] * coeff[26]
        + table[27] * coeff[27] + table[28] * coeff[28] + table[29] * coeff[29]
        + table[30] * coeff[30] + table[31] * coeff[31] + table[32] * coeff[32]
        + table[33] * coeff[33] + table[34] * coeff[34] + table[35] * coeff[35]
        + table[36] * coeff[36] + table[37] * coeff[37] + table[38] * coeff[38]
        + table[39] * coeff[39] + table[40] * coeff[40] + table[41] * coeff[41]
        + table[42] * coeff[42] + table[43] * coeff[43] + table[44] * coeff[44]
        + table[45] * coeff[45] + table[46] * coeff[46] + table[47] * coeff[47]
        + table[48] * coeff[48] + table[49] * coeff[49] + table[50] * coeff[50]
        + table[51] * coeff[51] + table[52] * coeff[52] + table[53] * coeff[53]
        + table[54] * coeff[54] + table[55] * coeff[55] + table[56] * coeff[56]
        + table[57] * coeff[57] + table[58] * coeff[58] + table[59] * coeff[59]
        + table[60] * coeff[60] + table[61] * coeff[61] + table[62] * coeff[62]
        + table[63] * coeff[63];

  }

  @Override
  public double value(double[] table, double[] derivative1, double[] derivative2) {
    derivative1[0] = table[0] * coeff[1] + 2 * table[1] * coeff[2] + 3 * table[2] * coeff[3]
        + table[4] * coeff[5] + 2 * table[5] * coeff[6] + 3 * table[6] * coeff[7]
        + table[8] * coeff[9] + 2 * table[9] * coeff[10] + 3 * table[10] * coeff[11]
        + table[12] * coeff[13] + 2 * table[13] * coeff[14] + 3 * table[14] * coeff[15]
        + table[16] * coeff[17] + 2 * table[17] * coeff[18] + 3 * table[18] * coeff[19]
        + table[20] * coeff[21] + 2 * table[21] * coeff[22] + 3 * table[22] * coeff[23]
        + table[24] * coeff[25] + 2 * table[25] * coeff[26] + 3 * table[26] * coeff[27]
        + table[28] * coeff[29] + 2 * table[29] * coeff[30] + 3 * table[30] * coeff[31]
        + table[32] * coeff[33] + 2 * table[33] * coeff[34] + 3 * table[34] * coeff[35]
        + table[36] * coeff[37] + 2 * table[37] * coeff[38] + 3 * table[38] * coeff[39]
        + table[40] * coeff[41] + 2 * table[41] * coeff[42] + 3 * table[42] * coeff[43]
        + table[44] * coeff[45] + 2 * table[45] * coeff[46] + 3 * table[46] * coeff[47]
        + table[48] * coeff[49] + 2 * table[49] * coeff[50] + 3 * table[50] * coeff[51]
        + table[52] * coeff[53] + 2 * table[53] * coeff[54] + 3 * table[54] * coeff[55]
        + table[56] * coeff[57] + 2 * table[57] * coeff[58] + 3 * table[58] * coeff[59]
        + table[60] * coeff[61] + 2 * table[61] * coeff[62] + 3 * table[62] * coeff[63];
    derivative1[1] = table[0] * coeff[4] + table[1] * coeff[5] + table[2] * coeff[6]
        + table[3] * coeff[7] + 2 * table[4] * coeff[8] + 2 * table[5] * coeff[9]
        + 2 * table[6] * coeff[10] + 2 * table[7] * coeff[11] + 3 * table[8] * coeff[12]
        + 3 * table[9] * coeff[13] + 3 * table[10] * coeff[14] + 3 * table[11] * coeff[15]
        + table[16] * coeff[20] + table[17] * coeff[21] + table[18] * coeff[22]
        + table[19] * coeff[23] + 2 * table[20] * coeff[24] + 2 * table[21] * coeff[25]
        + 2 * table[22] * coeff[26] + 2 * table[23] * coeff[27] + 3 * table[24] * coeff[28]
        + 3 * table[25] * coeff[29] + 3 * table[26] * coeff[30] + 3 * table[27] * coeff[31]
        + table[32] * coeff[36] + table[33] * coeff[37] + table[34] * coeff[38]
        + table[35] * coeff[39] + 2 * table[36] * coeff[40] + 2 * table[37] * coeff[41]
        + 2 * table[38] * coeff[42] + 2 * table[39] * coeff[43] + 3 * table[40] * coeff[44]
        + 3 * table[41] * coeff[45] + 3 * table[42] * coeff[46] + 3 * table[43] * coeff[47]
        + table[48] * coeff[52] + table[49] * coeff[53] + table[50] * coeff[54]
        + table[51] * coeff[55] + 2 * table[52] * coeff[56] + 2 * table[53] * coeff[57]
        + 2 * table[54] * coeff[58] + 2 * table[55] * coeff[59] + 3 * table[56] * coeff[60]
        + 3 * table[57] * coeff[61] + 3 * table[58] * coeff[62] + 3 * table[59] * coeff[63];
    derivative1[2] = table[0] * coeff[16] + table[1] * coeff[17] + table[2] * coeff[18]
        + table[3] * coeff[19] + table[4] * coeff[20] + table[5] * coeff[21] + table[6] * coeff[22]
        + table[7] * coeff[23] + table[8] * coeff[24] + table[9] * coeff[25] + table[10] * coeff[26]
        + table[11] * coeff[27] + table[12] * coeff[28] + table[13] * coeff[29]
        + table[14] * coeff[30] + table[15] * coeff[31] + 2 * table[16] * coeff[32]
        + 2 * table[17] * coeff[33] + 2 * table[18] * coeff[34] + 2 * table[19] * coeff[35]
        + 2 * table[20] * coeff[36] + 2 * table[21] * coeff[37] + 2 * table[22] * coeff[38]
        + 2 * table[23] * coeff[39] + 2 * table[24] * coeff[40] + 2 * table[25] * coeff[41]
        + 2 * table[26] * coeff[42] + 2 * table[27] * coeff[43] + 2 * table[28] * coeff[44]
        + 2 * table[29] * coeff[45] + 2 * table[30] * coeff[46] + 2 * table[31] * coeff[47]
        + 3 * table[32] * coeff[48] + 3 * table[33] * coeff[49] + 3 * table[34] * coeff[50]
        + 3 * table[35] * coeff[51] + 3 * table[36] * coeff[52] + 3 * table[37] * coeff[53]
        + 3 * table[38] * coeff[54] + 3 * table[39] * coeff[55] + 3 * table[40] * coeff[56]
        + 3 * table[41] * coeff[57] + 3 * table[42] * coeff[58] + 3 * table[43] * coeff[59]
        + 3 * table[44] * coeff[60] + 3 * table[45] * coeff[61] + 3 * table[46] * coeff[62]
        + 3 * table[47] * coeff[63];
    derivative2[0] = 2 * table[0] * coeff[2] + 6 * table[1] * coeff[3] + 2 * table[4] * coeff[6]
        + 6 * table[5] * coeff[7] + 2 * table[8] * coeff[10] + 6 * table[9] * coeff[11]
        + 2 * table[12] * coeff[14] + 6 * table[13] * coeff[15] + 2 * table[16] * coeff[18]
        + 6 * table[17] * coeff[19] + 2 * table[20] * coeff[22] + 6 * table[21] * coeff[23]
        + 2 * table[24] * coeff[26] + 6 * table[25] * coeff[27] + 2 * table[28] * coeff[30]
        + 6 * table[29] * coeff[31] + 2 * table[32] * coeff[34] + 6 * table[33] * coeff[35]
        + 2 * table[36] * coeff[38] + 6 * table[37] * coeff[39] + 2 * table[40] * coeff[42]
        + 6 * table[41] * coeff[43] + 2 * table[44] * coeff[46] + 6 * table[45] * coeff[47]
        + 2 * table[48] * coeff[50] + 6 * table[49] * coeff[51] + 2 * table[52] * coeff[54]
        + 6 * table[53] * coeff[55] + 2 * table[56] * coeff[58] + 6 * table[57] * coeff[59]
        + 2 * table[60] * coeff[62] + 6 * table[61] * coeff[63];
    derivative2[1] = 2 * table[0] * coeff[8] + 2 * table[1] * coeff[9] + 2 * table[2] * coeff[10]
        + 2 * table[3] * coeff[11] + 6 * table[4] * coeff[12] + 6 * table[5] * coeff[13]
        + 6 * table[6] * coeff[14] + 6 * table[7] * coeff[15] + 2 * table[16] * coeff[24]
        + 2 * table[17] * coeff[25] + 2 * table[18] * coeff[26] + 2 * table[19] * coeff[27]
        + 6 * table[20] * coeff[28] + 6 * table[21] * coeff[29] + 6 * table[22] * coeff[30]
        + 6 * table[23] * coeff[31] + 2 * table[32] * coeff[40] + 2 * table[33] * coeff[41]
        + 2 * table[34] * coeff[42] + 2 * table[35] * coeff[43] + 6 * table[36] * coeff[44]
        + 6 * table[37] * coeff[45] + 6 * table[38] * coeff[46] + 6 * table[39] * coeff[47]
        + 2 * table[48] * coeff[56] + 2 * table[49] * coeff[57] + 2 * table[50] * coeff[58]
        + 2 * table[51] * coeff[59] + 6 * table[52] * coeff[60] + 6 * table[53] * coeff[61]
        + 6 * table[54] * coeff[62] + 6 * table[55] * coeff[63];
    derivative2[2] = 2 * table[0] * coeff[32] + 2 * table[1] * coeff[33] + 2 * table[2] * coeff[34]
        + 2 * table[3] * coeff[35] + 2 * table[4] * coeff[36] + 2 * table[5] * coeff[37]
        + 2 * table[6] * coeff[38] + 2 * table[7] * coeff[39] + 2 * table[8] * coeff[40]
        + 2 * table[9] * coeff[41] + 2 * table[10] * coeff[42] + 2 * table[11] * coeff[43]
        + 2 * table[12] * coeff[44] + 2 * table[13] * coeff[45] + 2 * table[14] * coeff[46]
        + 2 * table[15] * coeff[47] + 6 * table[16] * coeff[48] + 6 * table[17] * coeff[49]
        + 6 * table[18] * coeff[50] + 6 * table[19] * coeff[51] + 6 * table[20] * coeff[52]
        + 6 * table[21] * coeff[53] + 6 * table[22] * coeff[54] + 6 * table[23] * coeff[55]
        + 6 * table[24] * coeff[56] + 6 * table[25] * coeff[57] + 6 * table[26] * coeff[58]
        + 6 * table[27] * coeff[59] + 6 * table[28] * coeff[60] + 6 * table[29] * coeff[61]
        + 6 * table[30] * coeff[62] + 6 * table[31] * coeff[63];
    return table[0] * coeff[0] + table[1] * coeff[1] + table[2] * coeff[2] + table[3] * coeff[3]
        + table[4] * coeff[4] + table[5] * coeff[5] + table[6] * coeff[6] + table[7] * coeff[7]
        + table[8] * coeff[8] + table[9] * coeff[9] + table[10] * coeff[10] + table[11] * coeff[11]
        + table[12] * coeff[12] + table[13] * coeff[13] + table[14] * coeff[14]
        + table[15] * coeff[15] + table[16] * coeff[16] + table[17] * coeff[17]
        + table[18] * coeff[18] + table[19] * coeff[19] + table[20] * coeff[20]
        + table[21] * coeff[21] + table[22] * coeff[22] + table[23] * coeff[23]
        + table[24] * coeff[24] + table[25] * coeff[25] + table[26] * coeff[26]
        + table[27] * coeff[27] + table[28] * coeff[28] + table[29] * coeff[29]
        + table[30] * coeff[30] + table[31] * coeff[31] + table[32] * coeff[32]
        + table[33] * coeff[33] + table[34] * coeff[34] + table[35] * coeff[35]
        + table[36] * coeff[36] + table[37] * coeff[37] + table[38] * coeff[38]
        + table[39] * coeff[39] + table[40] * coeff[40] + table[41] * coeff[41]
        + table[42] * coeff[42] + table[43] * coeff[43] + table[44] * coeff[44]
        + table[45] * coeff[45] + table[46] * coeff[46] + table[47] * coeff[47]
        + table[48] * coeff[48] + table[49] * coeff[49] + table[50] * coeff[50]
        + table[51] * coeff[51] + table[52] * coeff[52] + table[53] * coeff[53]
        + table[54] * coeff[54] + table[55] * coeff[55] + table[56] * coeff[56]
        + table[57] * coeff[57] + table[58] * coeff[58] + table[59] * coeff[59]
        + table[60] * coeff[60] + table[61] * coeff[61] + table[62] * coeff[62]
        + table[63] * coeff[63];

  }

  @Override
  public double value(float[] table, double[] derivative1, double[] derivative2) {
    derivative1[0] = table[0] * coeff[1] + 2 * table[1] * coeff[2] + 3 * table[2] * coeff[3]
        + table[4] * coeff[5] + 2 * table[5] * coeff[6] + 3 * table[6] * coeff[7]
        + table[8] * coeff[9] + 2 * table[9] * coeff[10] + 3 * table[10] * coeff[11]
        + table[12] * coeff[13] + 2 * table[13] * coeff[14] + 3 * table[14] * coeff[15]
        + table[16] * coeff[17] + 2 * table[17] * coeff[18] + 3 * table[18] * coeff[19]
        + table[20] * coeff[21] + 2 * table[21] * coeff[22] + 3 * table[22] * coeff[23]
        + table[24] * coeff[25] + 2 * table[25] * coeff[26] + 3 * table[26] * coeff[27]
        + table[28] * coeff[29] + 2 * table[29] * coeff[30] + 3 * table[30] * coeff[31]
        + table[32] * coeff[33] + 2 * table[33] * coeff[34] + 3 * table[34] * coeff[35]
        + table[36] * coeff[37] + 2 * table[37] * coeff[38] + 3 * table[38] * coeff[39]
        + table[40] * coeff[41] + 2 * table[41] * coeff[42] + 3 * table[42] * coeff[43]
        + table[44] * coeff[45] + 2 * table[45] * coeff[46] + 3 * table[46] * coeff[47]
        + table[48] * coeff[49] + 2 * table[49] * coeff[50] + 3 * table[50] * coeff[51]
        + table[52] * coeff[53] + 2 * table[53] * coeff[54] + 3 * table[54] * coeff[55]
        + table[56] * coeff[57] + 2 * table[57] * coeff[58] + 3 * table[58] * coeff[59]
        + table[60] * coeff[61] + 2 * table[61] * coeff[62] + 3 * table[62] * coeff[63];
    derivative1[1] = table[0] * coeff[4] + table[1] * coeff[5] + table[2] * coeff[6]
        + table[3] * coeff[7] + 2 * table[4] * coeff[8] + 2 * table[5] * coeff[9]
        + 2 * table[6] * coeff[10] + 2 * table[7] * coeff[11] + 3 * table[8] * coeff[12]
        + 3 * table[9] * coeff[13] + 3 * table[10] * coeff[14] + 3 * table[11] * coeff[15]
        + table[16] * coeff[20] + table[17] * coeff[21] + table[18] * coeff[22]
        + table[19] * coeff[23] + 2 * table[20] * coeff[24] + 2 * table[21] * coeff[25]
        + 2 * table[22] * coeff[26] + 2 * table[23] * coeff[27] + 3 * table[24] * coeff[28]
        + 3 * table[25] * coeff[29] + 3 * table[26] * coeff[30] + 3 * table[27] * coeff[31]
        + table[32] * coeff[36] + table[33] * coeff[37] + table[34] * coeff[38]
        + table[35] * coeff[39] + 2 * table[36] * coeff[40] + 2 * table[37] * coeff[41]
        + 2 * table[38] * coeff[42] + 2 * table[39] * coeff[43] + 3 * table[40] * coeff[44]
        + 3 * table[41] * coeff[45] + 3 * table[42] * coeff[46] + 3 * table[43] * coeff[47]
        + table[48] * coeff[52] + table[49] * coeff[53] + table[50] * coeff[54]
        + table[51] * coeff[55] + 2 * table[52] * coeff[56] + 2 * table[53] * coeff[57]
        + 2 * table[54] * coeff[58] + 2 * table[55] * coeff[59] + 3 * table[56] * coeff[60]
        + 3 * table[57] * coeff[61] + 3 * table[58] * coeff[62] + 3 * table[59] * coeff[63];
    derivative1[2] = table[0] * coeff[16] + table[1] * coeff[17] + table[2] * coeff[18]
        + table[3] * coeff[19] + table[4] * coeff[20] + table[5] * coeff[21] + table[6] * coeff[22]
        + table[7] * coeff[23] + table[8] * coeff[24] + table[9] * coeff[25] + table[10] * coeff[26]
        + table[11] * coeff[27] + table[12] * coeff[28] + table[13] * coeff[29]
        + table[14] * coeff[30] + table[15] * coeff[31] + 2 * table[16] * coeff[32]
        + 2 * table[17] * coeff[33] + 2 * table[18] * coeff[34] + 2 * table[19] * coeff[35]
        + 2 * table[20] * coeff[36] + 2 * table[21] * coeff[37] + 2 * table[22] * coeff[38]
        + 2 * table[23] * coeff[39] + 2 * table[24] * coeff[40] + 2 * table[25] * coeff[41]
        + 2 * table[26] * coeff[42] + 2 * table[27] * coeff[43] + 2 * table[28] * coeff[44]
        + 2 * table[29] * coeff[45] + 2 * table[30] * coeff[46] + 2 * table[31] * coeff[47]
        + 3 * table[32] * coeff[48] + 3 * table[33] * coeff[49] + 3 * table[34] * coeff[50]
        + 3 * table[35] * coeff[51] + 3 * table[36] * coeff[52] + 3 * table[37] * coeff[53]
        + 3 * table[38] * coeff[54] + 3 * table[39] * coeff[55] + 3 * table[40] * coeff[56]
        + 3 * table[41] * coeff[57] + 3 * table[42] * coeff[58] + 3 * table[43] * coeff[59]
        + 3 * table[44] * coeff[60] + 3 * table[45] * coeff[61] + 3 * table[46] * coeff[62]
        + 3 * table[47] * coeff[63];
    derivative2[0] = 2 * table[0] * coeff[2] + 6 * table[1] * coeff[3] + 2 * table[4] * coeff[6]
        + 6 * table[5] * coeff[7] + 2 * table[8] * coeff[10] + 6 * table[9] * coeff[11]
        + 2 * table[12] * coeff[14] + 6 * table[13] * coeff[15] + 2 * table[16] * coeff[18]
        + 6 * table[17] * coeff[19] + 2 * table[20] * coeff[22] + 6 * table[21] * coeff[23]
        + 2 * table[24] * coeff[26] + 6 * table[25] * coeff[27] + 2 * table[28] * coeff[30]
        + 6 * table[29] * coeff[31] + 2 * table[32] * coeff[34] + 6 * table[33] * coeff[35]
        + 2 * table[36] * coeff[38] + 6 * table[37] * coeff[39] + 2 * table[40] * coeff[42]
        + 6 * table[41] * coeff[43] + 2 * table[44] * coeff[46] + 6 * table[45] * coeff[47]
        + 2 * table[48] * coeff[50] + 6 * table[49] * coeff[51] + 2 * table[52] * coeff[54]
        + 6 * table[53] * coeff[55] + 2 * table[56] * coeff[58] + 6 * table[57] * coeff[59]
        + 2 * table[60] * coeff[62] + 6 * table[61] * coeff[63];
    derivative2[1] = 2 * table[0] * coeff[8] + 2 * table[1] * coeff[9] + 2 * table[2] * coeff[10]
        + 2 * table[3] * coeff[11] + 6 * table[4] * coeff[12] + 6 * table[5] * coeff[13]
        + 6 * table[6] * coeff[14] + 6 * table[7] * coeff[15] + 2 * table[16] * coeff[24]
        + 2 * table[17] * coeff[25] + 2 * table[18] * coeff[26] + 2 * table[19] * coeff[27]
        + 6 * table[20] * coeff[28] + 6 * table[21] * coeff[29] + 6 * table[22] * coeff[30]
        + 6 * table[23] * coeff[31] + 2 * table[32] * coeff[40] + 2 * table[33] * coeff[41]
        + 2 * table[34] * coeff[42] + 2 * table[35] * coeff[43] + 6 * table[36] * coeff[44]
        + 6 * table[37] * coeff[45] + 6 * table[38] * coeff[46] + 6 * table[39] * coeff[47]
        + 2 * table[48] * coeff[56] + 2 * table[49] * coeff[57] + 2 * table[50] * coeff[58]
        + 2 * table[51] * coeff[59] + 6 * table[52] * coeff[60] + 6 * table[53] * coeff[61]
        + 6 * table[54] * coeff[62] + 6 * table[55] * coeff[63];
    derivative2[2] = 2 * table[0] * coeff[32] + 2 * table[1] * coeff[33] + 2 * table[2] * coeff[34]
        + 2 * table[3] * coeff[35] + 2 * table[4] * coeff[36] + 2 * table[5] * coeff[37]
        + 2 * table[6] * coeff[38] + 2 * table[7] * coeff[39] + 2 * table[8] * coeff[40]
        + 2 * table[9] * coeff[41] + 2 * table[10] * coeff[42] + 2 * table[11] * coeff[43]
        + 2 * table[12] * coeff[44] + 2 * table[13] * coeff[45] + 2 * table[14] * coeff[46]
        + 2 * table[15] * coeff[47] + 6 * table[16] * coeff[48] + 6 * table[17] * coeff[49]
        + 6 * table[18] * coeff[50] + 6 * table[19] * coeff[51] + 6 * table[20] * coeff[52]
        + 6 * table[21] * coeff[53] + 6 * table[22] * coeff[54] + 6 * table[23] * coeff[55]
        + 6 * table[24] * coeff[56] + 6 * table[25] * coeff[57] + 6 * table[26] * coeff[58]
        + 6 * table[27] * coeff[59] + 6 * table[28] * coeff[60] + 6 * table[29] * coeff[61]
        + 6 * table[30] * coeff[62] + 6 * table[31] * coeff[63];
    return table[0] * coeff[0] + table[1] * coeff[1] + table[2] * coeff[2] + table[3] * coeff[3]
        + table[4] * coeff[4] + table[5] * coeff[5] + table[6] * coeff[6] + table[7] * coeff[7]
        + table[8] * coeff[8] + table[9] * coeff[9] + table[10] * coeff[10] + table[11] * coeff[11]
        + table[12] * coeff[12] + table[13] * coeff[13] + table[14] * coeff[14]
        + table[15] * coeff[15] + table[16] * coeff[16] + table[17] * coeff[17]
        + table[18] * coeff[18] + table[19] * coeff[19] + table[20] * coeff[20]
        + table[21] * coeff[21] + table[22] * coeff[22] + table[23] * coeff[23]
        + table[24] * coeff[24] + table[25] * coeff[25] + table[26] * coeff[26]
        + table[27] * coeff[27] + table[28] * coeff[28] + table[29] * coeff[29]
        + table[30] * coeff[30] + table[31] * coeff[31] + table[32] * coeff[32]
        + table[33] * coeff[33] + table[34] * coeff[34] + table[35] * coeff[35]
        + table[36] * coeff[36] + table[37] * coeff[37] + table[38] * coeff[38]
        + table[39] * coeff[39] + table[40] * coeff[40] + table[41] * coeff[41]
        + table[42] * coeff[42] + table[43] * coeff[43] + table[44] * coeff[44]
        + table[45] * coeff[45] + table[46] * coeff[46] + table[47] * coeff[47]
        + table[48] * coeff[48] + table[49] * coeff[49] + table[50] * coeff[50]
        + table[51] * coeff[51] + table[52] * coeff[52] + table[53] * coeff[53]
        + table[54] * coeff[54] + table[55] * coeff[55] + table[56] * coeff[56]
        + table[57] * coeff[57] + table[58] * coeff[58] + table[59] * coeff[59]
        + table[60] * coeff[60] + table[61] * coeff[61] + table[62] * coeff[62]
        + table[63] * coeff[63];

  }

  @Override
  public double value(double[] table, double[] table2, double[] table3, double[] table6,
      double[] derivative1, double[] derivative2) {
    derivative1[0] = table[0] * coeff[1] + table2[1] * coeff[2] + table3[2] * coeff[3]
        + table[4] * coeff[5] + table2[5] * coeff[6] + table3[6] * coeff[7] + table[8] * coeff[9]
        + table2[9] * coeff[10] + table3[10] * coeff[11] + table[12] * coeff[13]
        + table2[13] * coeff[14] + table3[14] * coeff[15] + table[16] * coeff[17]
        + table2[17] * coeff[18] + table3[18] * coeff[19] + table[20] * coeff[21]
        + table2[21] * coeff[22] + table3[22] * coeff[23] + table[24] * coeff[25]
        + table2[25] * coeff[26] + table3[26] * coeff[27] + table[28] * coeff[29]
        + table2[29] * coeff[30] + table3[30] * coeff[31] + table[32] * coeff[33]
        + table2[33] * coeff[34] + table3[34] * coeff[35] + table[36] * coeff[37]
        + table2[37] * coeff[38] + table3[38] * coeff[39] + table[40] * coeff[41]
        + table2[41] * coeff[42] + table3[42] * coeff[43] + table[44] * coeff[45]
        + table2[45] * coeff[46] + table3[46] * coeff[47] + table[48] * coeff[49]
        + table2[49] * coeff[50] + table3[50] * coeff[51] + table[52] * coeff[53]
        + table2[53] * coeff[54] + table3[54] * coeff[55] + table[56] * coeff[57]
        + table2[57] * coeff[58] + table3[58] * coeff[59] + table[60] * coeff[61]
        + table2[61] * coeff[62] + table3[62] * coeff[63];
    derivative1[1] = table[0] * coeff[4] + table[1] * coeff[5] + table[2] * coeff[6]
        + table[3] * coeff[7] + table2[4] * coeff[8] + table2[5] * coeff[9] + table2[6] * coeff[10]
        + table2[7] * coeff[11] + table3[8] * coeff[12] + table3[9] * coeff[13]
        + table3[10] * coeff[14] + table3[11] * coeff[15] + table[16] * coeff[20]
        + table[17] * coeff[21] + table[18] * coeff[22] + table[19] * coeff[23]
        + table2[20] * coeff[24] + table2[21] * coeff[25] + table2[22] * coeff[26]
        + table2[23] * coeff[27] + table3[24] * coeff[28] + table3[25] * coeff[29]
        + table3[26] * coeff[30] + table3[27] * coeff[31] + table[32] * coeff[36]
        + table[33] * coeff[37] + table[34] * coeff[38] + table[35] * coeff[39]
        + table2[36] * coeff[40] + table2[37] * coeff[41] + table2[38] * coeff[42]
        + table2[39] * coeff[43] + table3[40] * coeff[44] + table3[41] * coeff[45]
        + table3[42] * coeff[46] + table3[43] * coeff[47] + table[48] * coeff[52]
        + table[49] * coeff[53] + table[50] * coeff[54] + table[51] * coeff[55]
        + table2[52] * coeff[56] + table2[53] * coeff[57] + table2[54] * coeff[58]
        + table2[55] * coeff[59] + table3[56] * coeff[60] + table3[57] * coeff[61]
        + table3[58] * coeff[62] + table3[59] * coeff[63];
    derivative1[2] = table[0] * coeff[16] + table[1] * coeff[17] + table[2] * coeff[18]
        + table[3] * coeff[19] + table[4] * coeff[20] + table[5] * coeff[21] + table[6] * coeff[22]
        + table[7] * coeff[23] + table[8] * coeff[24] + table[9] * coeff[25] + table[10] * coeff[26]
        + table[11] * coeff[27] + table[12] * coeff[28] + table[13] * coeff[29]
        + table[14] * coeff[30] + table[15] * coeff[31] + table2[16] * coeff[32]
        + table2[17] * coeff[33] + table2[18] * coeff[34] + table2[19] * coeff[35]
        + table2[20] * coeff[36] + table2[21] * coeff[37] + table2[22] * coeff[38]
        + table2[23] * coeff[39] + table2[24] * coeff[40] + table2[25] * coeff[41]
        + table2[26] * coeff[42] + table2[27] * coeff[43] + table2[28] * coeff[44]
        + table2[29] * coeff[45] + table2[30] * coeff[46] + table2[31] * coeff[47]
        + table3[32] * coeff[48] + table3[33] * coeff[49] + table3[34] * coeff[50]
        + table3[35] * coeff[51] + table3[36] * coeff[52] + table3[37] * coeff[53]
        + table3[38] * coeff[54] + table3[39] * coeff[55] + table3[40] * coeff[56]
        + table3[41] * coeff[57] + table3[42] * coeff[58] + table3[43] * coeff[59]
        + table3[44] * coeff[60] + table3[45] * coeff[61] + table3[46] * coeff[62]
        + table3[47] * coeff[63];
    derivative2[0] = table2[0] * coeff[2] + table6[1] * coeff[3] + table2[4] * coeff[6]
        + table6[5] * coeff[7] + table2[8] * coeff[10] + table6[9] * coeff[11]
        + table2[12] * coeff[14] + table6[13] * coeff[15] + table2[16] * coeff[18]
        + table6[17] * coeff[19] + table2[20] * coeff[22] + table6[21] * coeff[23]
        + table2[24] * coeff[26] + table6[25] * coeff[27] + table2[28] * coeff[30]
        + table6[29] * coeff[31] + table2[32] * coeff[34] + table6[33] * coeff[35]
        + table2[36] * coeff[38] + table6[37] * coeff[39] + table2[40] * coeff[42]
        + table6[41] * coeff[43] + table2[44] * coeff[46] + table6[45] * coeff[47]
        + table2[48] * coeff[50] + table6[49] * coeff[51] + table2[52] * coeff[54]
        + table6[53] * coeff[55] + table2[56] * coeff[58] + table6[57] * coeff[59]
        + table2[60] * coeff[62] + table6[61] * coeff[63];
    derivative2[1] = table2[0] * coeff[8] + table2[1] * coeff[9] + table2[2] * coeff[10]
        + table2[3] * coeff[11] + table6[4] * coeff[12] + table6[5] * coeff[13]
        + table6[6] * coeff[14] + table6[7] * coeff[15] + table2[16] * coeff[24]
        + table2[17] * coeff[25] + table2[18] * coeff[26] + table2[19] * coeff[27]
        + table6[20] * coeff[28] + table6[21] * coeff[29] + table6[22] * coeff[30]
        + table6[23] * coeff[31] + table2[32] * coeff[40] + table2[33] * coeff[41]
        + table2[34] * coeff[42] + table2[35] * coeff[43] + table6[36] * coeff[44]
        + table6[37] * coeff[45] + table6[38] * coeff[46] + table6[39] * coeff[47]
        + table2[48] * coeff[56] + table2[49] * coeff[57] + table2[50] * coeff[58]
        + table2[51] * coeff[59] + table6[52] * coeff[60] + table6[53] * coeff[61]
        + table6[54] * coeff[62] + table6[55] * coeff[63];
    derivative2[2] = table2[0] * coeff[32] + table2[1] * coeff[33] + table2[2] * coeff[34]
        + table2[3] * coeff[35] + table2[4] * coeff[36] + table2[5] * coeff[37]
        + table2[6] * coeff[38] + table2[7] * coeff[39] + table2[8] * coeff[40]
        + table2[9] * coeff[41] + table2[10] * coeff[42] + table2[11] * coeff[43]
        + table2[12] * coeff[44] + table2[13] * coeff[45] + table2[14] * coeff[46]
        + table2[15] * coeff[47] + table6[16] * coeff[48] + table6[17] * coeff[49]
        + table6[18] * coeff[50] + table6[19] * coeff[51] + table6[20] * coeff[52]
        + table6[21] * coeff[53] + table6[22] * coeff[54] + table6[23] * coeff[55]
        + table6[24] * coeff[56] + table6[25] * coeff[57] + table6[26] * coeff[58]
        + table6[27] * coeff[59] + table6[28] * coeff[60] + table6[29] * coeff[61]
        + table6[30] * coeff[62] + table6[31] * coeff[63];
    return table[0] * coeff[0] + table[1] * coeff[1] + table[2] * coeff[2] + table[3] * coeff[3]
        + table[4] * coeff[4] + table[5] * coeff[5] + table[6] * coeff[6] + table[7] * coeff[7]
        + table[8] * coeff[8] + table[9] * coeff[9] + table[10] * coeff[10] + table[11] * coeff[11]
        + table[12] * coeff[12] + table[13] * coeff[13] + table[14] * coeff[14]
        + table[15] * coeff[15] + table[16] * coeff[16] + table[17] * coeff[17]
        + table[18] * coeff[18] + table[19] * coeff[19] + table[20] * coeff[20]
        + table[21] * coeff[21] + table[22] * coeff[22] + table[23] * coeff[23]
        + table[24] * coeff[24] + table[25] * coeff[25] + table[26] * coeff[26]
        + table[27] * coeff[27] + table[28] * coeff[28] + table[29] * coeff[29]
        + table[30] * coeff[30] + table[31] * coeff[31] + table[32] * coeff[32]
        + table[33] * coeff[33] + table[34] * coeff[34] + table[35] * coeff[35]
        + table[36] * coeff[36] + table[37] * coeff[37] + table[38] * coeff[38]
        + table[39] * coeff[39] + table[40] * coeff[40] + table[41] * coeff[41]
        + table[42] * coeff[42] + table[43] * coeff[43] + table[44] * coeff[44]
        + table[45] * coeff[45] + table[46] * coeff[46] + table[47] * coeff[47]
        + table[48] * coeff[48] + table[49] * coeff[49] + table[50] * coeff[50]
        + table[51] * coeff[51] + table[52] * coeff[52] + table[53] * coeff[53]
        + table[54] * coeff[54] + table[55] * coeff[55] + table[56] * coeff[56]
        + table[57] * coeff[57] + table[58] * coeff[58] + table[59] * coeff[59]
        + table[60] * coeff[60] + table[61] * coeff[61] + table[62] * coeff[62]
        + table[63] * coeff[63];

  }

  @Override
  public double value(float[] table, float[] table2, float[] table3, float[] table6,
      double[] derivative1, double[] derivative2) {
    derivative1[0] = table[0] * coeff[1] + table2[1] * coeff[2] + table3[2] * coeff[3]
        + table[4] * coeff[5] + table2[5] * coeff[6] + table3[6] * coeff[7] + table[8] * coeff[9]
        + table2[9] * coeff[10] + table3[10] * coeff[11] + table[12] * coeff[13]
        + table2[13] * coeff[14] + table3[14] * coeff[15] + table[16] * coeff[17]
        + table2[17] * coeff[18] + table3[18] * coeff[19] + table[20] * coeff[21]
        + table2[21] * coeff[22] + table3[22] * coeff[23] + table[24] * coeff[25]
        + table2[25] * coeff[26] + table3[26] * coeff[27] + table[28] * coeff[29]
        + table2[29] * coeff[30] + table3[30] * coeff[31] + table[32] * coeff[33]
        + table2[33] * coeff[34] + table3[34] * coeff[35] + table[36] * coeff[37]
        + table2[37] * coeff[38] + table3[38] * coeff[39] + table[40] * coeff[41]
        + table2[41] * coeff[42] + table3[42] * coeff[43] + table[44] * coeff[45]
        + table2[45] * coeff[46] + table3[46] * coeff[47] + table[48] * coeff[49]
        + table2[49] * coeff[50] + table3[50] * coeff[51] + table[52] * coeff[53]
        + table2[53] * coeff[54] + table3[54] * coeff[55] + table[56] * coeff[57]
        + table2[57] * coeff[58] + table3[58] * coeff[59] + table[60] * coeff[61]
        + table2[61] * coeff[62] + table3[62] * coeff[63];
    derivative1[1] = table[0] * coeff[4] + table[1] * coeff[5] + table[2] * coeff[6]
        + table[3] * coeff[7] + table2[4] * coeff[8] + table2[5] * coeff[9] + table2[6] * coeff[10]
        + table2[7] * coeff[11] + table3[8] * coeff[12] + table3[9] * coeff[13]
        + table3[10] * coeff[14] + table3[11] * coeff[15] + table[16] * coeff[20]
        + table[17] * coeff[21] + table[18] * coeff[22] + table[19] * coeff[23]
        + table2[20] * coeff[24] + table2[21] * coeff[25] + table2[22] * coeff[26]
        + table2[23] * coeff[27] + table3[24] * coeff[28] + table3[25] * coeff[29]
        + table3[26] * coeff[30] + table3[27] * coeff[31] + table[32] * coeff[36]
        + table[33] * coeff[37] + table[34] * coeff[38] + table[35] * coeff[39]
        + table2[36] * coeff[40] + table2[37] * coeff[41] + table2[38] * coeff[42]
        + table2[39] * coeff[43] + table3[40] * coeff[44] + table3[41] * coeff[45]
        + table3[42] * coeff[46] + table3[43] * coeff[47] + table[48] * coeff[52]
        + table[49] * coeff[53] + table[50] * coeff[54] + table[51] * coeff[55]
        + table2[52] * coeff[56] + table2[53] * coeff[57] + table2[54] * coeff[58]
        + table2[55] * coeff[59] + table3[56] * coeff[60] + table3[57] * coeff[61]
        + table3[58] * coeff[62] + table3[59] * coeff[63];
    derivative1[2] = table[0] * coeff[16] + table[1] * coeff[17] + table[2] * coeff[18]
        + table[3] * coeff[19] + table[4] * coeff[20] + table[5] * coeff[21] + table[6] * coeff[22]
        + table[7] * coeff[23] + table[8] * coeff[24] + table[9] * coeff[25] + table[10] * coeff[26]
        + table[11] * coeff[27] + table[12] * coeff[28] + table[13] * coeff[29]
        + table[14] * coeff[30] + table[15] * coeff[31] + table2[16] * coeff[32]
        + table2[17] * coeff[33] + table2[18] * coeff[34] + table2[19] * coeff[35]
        + table2[20] * coeff[36] + table2[21] * coeff[37] + table2[22] * coeff[38]
        + table2[23] * coeff[39] + table2[24] * coeff[40] + table2[25] * coeff[41]
        + table2[26] * coeff[42] + table2[27] * coeff[43] + table2[28] * coeff[44]
        + table2[29] * coeff[45] + table2[30] * coeff[46] + table2[31] * coeff[47]
        + table3[32] * coeff[48] + table3[33] * coeff[49] + table3[34] * coeff[50]
        + table3[35] * coeff[51] + table3[36] * coeff[52] + table3[37] * coeff[53]
        + table3[38] * coeff[54] + table3[39] * coeff[55] + table3[40] * coeff[56]
        + table3[41] * coeff[57] + table3[42] * coeff[58] + table3[43] * coeff[59]
        + table3[44] * coeff[60] + table3[45] * coeff[61] + table3[46] * coeff[62]
        + table3[47] * coeff[63];
    derivative2[0] = table2[0] * coeff[2] + table6[1] * coeff[3] + table2[4] * coeff[6]
        + table6[5] * coeff[7] + table2[8] * coeff[10] + table6[9] * coeff[11]
        + table2[12] * coeff[14] + table6[13] * coeff[15] + table2[16] * coeff[18]
        + table6[17] * coeff[19] + table2[20] * coeff[22] + table6[21] * coeff[23]
        + table2[24] * coeff[26] + table6[25] * coeff[27] + table2[28] * coeff[30]
        + table6[29] * coeff[31] + table2[32] * coeff[34] + table6[33] * coeff[35]
        + table2[36] * coeff[38] + table6[37] * coeff[39] + table2[40] * coeff[42]
        + table6[41] * coeff[43] + table2[44] * coeff[46] + table6[45] * coeff[47]
        + table2[48] * coeff[50] + table6[49] * coeff[51] + table2[52] * coeff[54]
        + table6[53] * coeff[55] + table2[56] * coeff[58] + table6[57] * coeff[59]
        + table2[60] * coeff[62] + table6[61] * coeff[63];
    derivative2[1] = table2[0] * coeff[8] + table2[1] * coeff[9] + table2[2] * coeff[10]
        + table2[3] * coeff[11] + table6[4] * coeff[12] + table6[5] * coeff[13]
        + table6[6] * coeff[14] + table6[7] * coeff[15] + table2[16] * coeff[24]
        + table2[17] * coeff[25] + table2[18] * coeff[26] + table2[19] * coeff[27]
        + table6[20] * coeff[28] + table6[21] * coeff[29] + table6[22] * coeff[30]
        + table6[23] * coeff[31] + table2[32] * coeff[40] + table2[33] * coeff[41]
        + table2[34] * coeff[42] + table2[35] * coeff[43] + table6[36] * coeff[44]
        + table6[37] * coeff[45] + table6[38] * coeff[46] + table6[39] * coeff[47]
        + table2[48] * coeff[56] + table2[49] * coeff[57] + table2[50] * coeff[58]
        + table2[51] * coeff[59] + table6[52] * coeff[60] + table6[53] * coeff[61]
        + table6[54] * coeff[62] + table6[55] * coeff[63];
    derivative2[2] = table2[0] * coeff[32] + table2[1] * coeff[33] + table2[2] * coeff[34]
        + table2[3] * coeff[35] + table2[4] * coeff[36] + table2[5] * coeff[37]
        + table2[6] * coeff[38] + table2[7] * coeff[39] + table2[8] * coeff[40]
        + table2[9] * coeff[41] + table2[10] * coeff[42] + table2[11] * coeff[43]
        + table2[12] * coeff[44] + table2[13] * coeff[45] + table2[14] * coeff[46]
        + table2[15] * coeff[47] + table6[16] * coeff[48] + table6[17] * coeff[49]
        + table6[18] * coeff[50] + table6[19] * coeff[51] + table6[20] * coeff[52]
        + table6[21] * coeff[53] + table6[22] * coeff[54] + table6[23] * coeff[55]
        + table6[24] * coeff[56] + table6[25] * coeff[57] + table6[26] * coeff[58]
        + table6[27] * coeff[59] + table6[28] * coeff[60] + table6[29] * coeff[61]
        + table6[30] * coeff[62] + table6[31] * coeff[63];
    return table[0] * coeff[0] + table[1] * coeff[1] + table[2] * coeff[2] + table[3] * coeff[3]
        + table[4] * coeff[4] + table[5] * coeff[5] + table[6] * coeff[6] + table[7] * coeff[7]
        + table[8] * coeff[8] + table[9] * coeff[9] + table[10] * coeff[10] + table[11] * coeff[11]
        + table[12] * coeff[12] + table[13] * coeff[13] + table[14] * coeff[14]
        + table[15] * coeff[15] + table[16] * coeff[16] + table[17] * coeff[17]
        + table[18] * coeff[18] + table[19] * coeff[19] + table[20] * coeff[20]
        + table[21] * coeff[21] + table[22] * coeff[22] + table[23] * coeff[23]
        + table[24] * coeff[24] + table[25] * coeff[25] + table[26] * coeff[26]
        + table[27] * coeff[27] + table[28] * coeff[28] + table[29] * coeff[29]
        + table[30] * coeff[30] + table[31] * coeff[31] + table[32] * coeff[32]
        + table[33] * coeff[33] + table[34] * coeff[34] + table[35] * coeff[35]
        + table[36] * coeff[36] + table[37] * coeff[37] + table[38] * coeff[38]
        + table[39] * coeff[39] + table[40] * coeff[40] + table[41] * coeff[41]
        + table[42] * coeff[42] + table[43] * coeff[43] + table[44] * coeff[44]
        + table[45] * coeff[45] + table[46] * coeff[46] + table[47] * coeff[47]
        + table[48] * coeff[48] + table[49] * coeff[49] + table[50] * coeff[50]
        + table[51] * coeff[51] + table[52] * coeff[52] + table[53] * coeff[53]
        + table[54] * coeff[54] + table[55] * coeff[55] + table[56] * coeff[56]
        + table[57] * coeff[57] + table[58] * coeff[58] + table[59] * coeff[59]
        + table[60] * coeff[60] + table[61] * coeff[61] + table[62] * coeff[62]
        + table[63] * coeff[63];
  }

  @Override
  public void gradient(double[] table, double[] derivative1) {
    derivative1[0] = table[0] * coeff[1] + 2 * table[1] * coeff[2] + 3 * table[2] * coeff[3]
        + table[4] * coeff[5] + 2 * table[5] * coeff[6] + 3 * table[6] * coeff[7]
        + table[8] * coeff[9] + 2 * table[9] * coeff[10] + 3 * table[10] * coeff[11]
        + table[12] * coeff[13] + 2 * table[13] * coeff[14] + 3 * table[14] * coeff[15]
        + table[16] * coeff[17] + 2 * table[17] * coeff[18] + 3 * table[18] * coeff[19]
        + table[20] * coeff[21] + 2 * table[21] * coeff[22] + 3 * table[22] * coeff[23]
        + table[24] * coeff[25] + 2 * table[25] * coeff[26] + 3 * table[26] * coeff[27]
        + table[28] * coeff[29] + 2 * table[29] * coeff[30] + 3 * table[30] * coeff[31]
        + table[32] * coeff[33] + 2 * table[33] * coeff[34] + 3 * table[34] * coeff[35]
        + table[36] * coeff[37] + 2 * table[37] * coeff[38] + 3 * table[38] * coeff[39]
        + table[40] * coeff[41] + 2 * table[41] * coeff[42] + 3 * table[42] * coeff[43]
        + table[44] * coeff[45] + 2 * table[45] * coeff[46] + 3 * table[46] * coeff[47]
        + table[48] * coeff[49] + 2 * table[49] * coeff[50] + 3 * table[50] * coeff[51]
        + table[52] * coeff[53] + 2 * table[53] * coeff[54] + 3 * table[54] * coeff[55]
        + table[56] * coeff[57] + 2 * table[57] * coeff[58] + 3 * table[58] * coeff[59]
        + table[60] * coeff[61] + 2 * table[61] * coeff[62] + 3 * table[62] * coeff[63];
    derivative1[1] = table[0] * coeff[4] + table[1] * coeff[5] + table[2] * coeff[6]
        + table[3] * coeff[7] + 2 * table[4] * coeff[8] + 2 * table[5] * coeff[9]
        + 2 * table[6] * coeff[10] + 2 * table[7] * coeff[11] + 3 * table[8] * coeff[12]
        + 3 * table[9] * coeff[13] + 3 * table[10] * coeff[14] + 3 * table[11] * coeff[15]
        + table[16] * coeff[20] + table[17] * coeff[21] + table[18] * coeff[22]
        + table[19] * coeff[23] + 2 * table[20] * coeff[24] + 2 * table[21] * coeff[25]
        + 2 * table[22] * coeff[26] + 2 * table[23] * coeff[27] + 3 * table[24] * coeff[28]
        + 3 * table[25] * coeff[29] + 3 * table[26] * coeff[30] + 3 * table[27] * coeff[31]
        + table[32] * coeff[36] + table[33] * coeff[37] + table[34] * coeff[38]
        + table[35] * coeff[39] + 2 * table[36] * coeff[40] + 2 * table[37] * coeff[41]
        + 2 * table[38] * coeff[42] + 2 * table[39] * coeff[43] + 3 * table[40] * coeff[44]
        + 3 * table[41] * coeff[45] + 3 * table[42] * coeff[46] + 3 * table[43] * coeff[47]
        + table[48] * coeff[52] + table[49] * coeff[53] + table[50] * coeff[54]
        + table[51] * coeff[55] + 2 * table[52] * coeff[56] + 2 * table[53] * coeff[57]
        + 2 * table[54] * coeff[58] + 2 * table[55] * coeff[59] + 3 * table[56] * coeff[60]
        + 3 * table[57] * coeff[61] + 3 * table[58] * coeff[62] + 3 * table[59] * coeff[63];
    derivative1[2] = table[0] * coeff[16] + table[1] * coeff[17] + table[2] * coeff[18]
        + table[3] * coeff[19] + table[4] * coeff[20] + table[5] * coeff[21] + table[6] * coeff[22]
        + table[7] * coeff[23] + table[8] * coeff[24] + table[9] * coeff[25] + table[10] * coeff[26]
        + table[11] * coeff[27] + table[12] * coeff[28] + table[13] * coeff[29]
        + table[14] * coeff[30] + table[15] * coeff[31] + 2 * table[16] * coeff[32]
        + 2 * table[17] * coeff[33] + 2 * table[18] * coeff[34] + 2 * table[19] * coeff[35]
        + 2 * table[20] * coeff[36] + 2 * table[21] * coeff[37] + 2 * table[22] * coeff[38]
        + 2 * table[23] * coeff[39] + 2 * table[24] * coeff[40] + 2 * table[25] * coeff[41]
        + 2 * table[26] * coeff[42] + 2 * table[27] * coeff[43] + 2 * table[28] * coeff[44]
        + 2 * table[29] * coeff[45] + 2 * table[30] * coeff[46] + 2 * table[31] * coeff[47]
        + 3 * table[32] * coeff[48] + 3 * table[33] * coeff[49] + 3 * table[34] * coeff[50]
        + 3 * table[35] * coeff[51] + 3 * table[36] * coeff[52] + 3 * table[37] * coeff[53]
        + 3 * table[38] * coeff[54] + 3 * table[39] * coeff[55] + 3 * table[40] * coeff[56]
        + 3 * table[41] * coeff[57] + 3 * table[42] * coeff[58] + 3 * table[43] * coeff[59]
        + 3 * table[44] * coeff[60] + 3 * table[45] * coeff[61] + 3 * table[46] * coeff[62]
        + 3 * table[47] * coeff[63];
  }

  @Override
  public void gradient(float[] table, double[] derivative1) {
    derivative1[0] = table[0] * coeff[1] + 2 * table[1] * coeff[2] + 3 * table[2] * coeff[3]
        + table[4] * coeff[5] + 2 * table[5] * coeff[6] + 3 * table[6] * coeff[7]
        + table[8] * coeff[9] + 2 * table[9] * coeff[10] + 3 * table[10] * coeff[11]
        + table[12] * coeff[13] + 2 * table[13] * coeff[14] + 3 * table[14] * coeff[15]
        + table[16] * coeff[17] + 2 * table[17] * coeff[18] + 3 * table[18] * coeff[19]
        + table[20] * coeff[21] + 2 * table[21] * coeff[22] + 3 * table[22] * coeff[23]
        + table[24] * coeff[25] + 2 * table[25] * coeff[26] + 3 * table[26] * coeff[27]
        + table[28] * coeff[29] + 2 * table[29] * coeff[30] + 3 * table[30] * coeff[31]
        + table[32] * coeff[33] + 2 * table[33] * coeff[34] + 3 * table[34] * coeff[35]
        + table[36] * coeff[37] + 2 * table[37] * coeff[38] + 3 * table[38] * coeff[39]
        + table[40] * coeff[41] + 2 * table[41] * coeff[42] + 3 * table[42] * coeff[43]
        + table[44] * coeff[45] + 2 * table[45] * coeff[46] + 3 * table[46] * coeff[47]
        + table[48] * coeff[49] + 2 * table[49] * coeff[50] + 3 * table[50] * coeff[51]
        + table[52] * coeff[53] + 2 * table[53] * coeff[54] + 3 * table[54] * coeff[55]
        + table[56] * coeff[57] + 2 * table[57] * coeff[58] + 3 * table[58] * coeff[59]
        + table[60] * coeff[61] + 2 * table[61] * coeff[62] + 3 * table[62] * coeff[63];
    derivative1[1] = table[0] * coeff[4] + table[1] * coeff[5] + table[2] * coeff[6]
        + table[3] * coeff[7] + 2 * table[4] * coeff[8] + 2 * table[5] * coeff[9]
        + 2 * table[6] * coeff[10] + 2 * table[7] * coeff[11] + 3 * table[8] * coeff[12]
        + 3 * table[9] * coeff[13] + 3 * table[10] * coeff[14] + 3 * table[11] * coeff[15]
        + table[16] * coeff[20] + table[17] * coeff[21] + table[18] * coeff[22]
        + table[19] * coeff[23] + 2 * table[20] * coeff[24] + 2 * table[21] * coeff[25]
        + 2 * table[22] * coeff[26] + 2 * table[23] * coeff[27] + 3 * table[24] * coeff[28]
        + 3 * table[25] * coeff[29] + 3 * table[26] * coeff[30] + 3 * table[27] * coeff[31]
        + table[32] * coeff[36] + table[33] * coeff[37] + table[34] * coeff[38]
        + table[35] * coeff[39] + 2 * table[36] * coeff[40] + 2 * table[37] * coeff[41]
        + 2 * table[38] * coeff[42] + 2 * table[39] * coeff[43] + 3 * table[40] * coeff[44]
        + 3 * table[41] * coeff[45] + 3 * table[42] * coeff[46] + 3 * table[43] * coeff[47]
        + table[48] * coeff[52] + table[49] * coeff[53] + table[50] * coeff[54]
        + table[51] * coeff[55] + 2 * table[52] * coeff[56] + 2 * table[53] * coeff[57]
        + 2 * table[54] * coeff[58] + 2 * table[55] * coeff[59] + 3 * table[56] * coeff[60]
        + 3 * table[57] * coeff[61] + 3 * table[58] * coeff[62] + 3 * table[59] * coeff[63];
    derivative1[2] = table[0] * coeff[16] + table[1] * coeff[17] + table[2] * coeff[18]
        + table[3] * coeff[19] + table[4] * coeff[20] + table[5] * coeff[21] + table[6] * coeff[22]
        + table[7] * coeff[23] + table[8] * coeff[24] + table[9] * coeff[25] + table[10] * coeff[26]
        + table[11] * coeff[27] + table[12] * coeff[28] + table[13] * coeff[29]
        + table[14] * coeff[30] + table[15] * coeff[31] + 2 * table[16] * coeff[32]
        + 2 * table[17] * coeff[33] + 2 * table[18] * coeff[34] + 2 * table[19] * coeff[35]
        + 2 * table[20] * coeff[36] + 2 * table[21] * coeff[37] + 2 * table[22] * coeff[38]
        + 2 * table[23] * coeff[39] + 2 * table[24] * coeff[40] + 2 * table[25] * coeff[41]
        + 2 * table[26] * coeff[42] + 2 * table[27] * coeff[43] + 2 * table[28] * coeff[44]
        + 2 * table[29] * coeff[45] + 2 * table[30] * coeff[46] + 2 * table[31] * coeff[47]
        + 3 * table[32] * coeff[48] + 3 * table[33] * coeff[49] + 3 * table[34] * coeff[50]
        + 3 * table[35] * coeff[51] + 3 * table[36] * coeff[52] + 3 * table[37] * coeff[53]
        + 3 * table[38] * coeff[54] + 3 * table[39] * coeff[55] + 3 * table[40] * coeff[56]
        + 3 * table[41] * coeff[57] + 3 * table[42] * coeff[58] + 3 * table[43] * coeff[59]
        + 3 * table[44] * coeff[60] + 3 * table[45] * coeff[61] + 3 * table[46] * coeff[62]
        + 3 * table[47] * coeff[63];
  }
}
