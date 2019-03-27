/*-
 * %%Ignore-License
 *
 * GDSC Software
 *
 * This is an extension of the
 * org.apache.commons.math3.analysis.interpolation.TricubicFunction
 *
 * Modifications have been made to allow computation of gradients and computation
 * with pre-computed x,y,z powers using single/floating precision.
 *
 * The code is released under the original Apache licence:
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import org.apache.commons.lang3.ArrayUtils;

/**
 * Utilities for a 3D-spline function.
 */
public final class CustomTricubicFunctionUtils {
  /**
   * No public construction.
   */
  private CustomTricubicFunctionUtils() {}

  /**
   * Creates the CustomTricubicFunction from the 64 coefficients.
   *
   * <p>Coefficients must be computed as if iterating: z^a * y^b * x^c with a,b,c in [0, 3].
   *
   * @param coefficients the coefficients
   * @return the custom tricubic function
   * @throws IllegalArgumentException if the input array length {@code < 64}
   */
  public static CustomTricubicFunction create(double[] coefficients) {
    checkLength(coefficients);
    return new DoubleCustomTricubicFunction(new DoubleCubicSplineData(coefficients));
  }

  /**
   * Creates the CustomTricubicFunction from the 64 coefficients.
   *
   * <p>Coefficients must be computed as if iterating: z^a * y^b * x^c with a,b,c in [0, 3].
   *
   * @param coefficients the coefficients
   * @return the custom tricubic function
   * @throws IllegalArgumentException if the input array length {@code < 64}
   */
  public static CustomTricubicFunction create(float[] coefficients) {
    checkLength(coefficients);
    return new FloatCustomTricubicFunction(new FloatCubicSplineData(coefficients));
  }

  private static void checkLength(Object coefficients) {
    if (ArrayUtils.getLength(coefficients) < 64) {
      throw new IllegalArgumentException("Require an array of 64 coefficients");
    }
  }
}
