/*-
 * %%Ignore-License
 *
 * GDSC Software
 *
 * This is an extension of the
 * org.apache.commons.math3.analysis.interpolation.TricubicInterpolatingFunction
 *
 * Modifications have been made to allow computation of gradients and abstraction
 * of the input data into value providers.
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

import uk.ac.sussex.gdsc.core.data.DoubleArrayValueProvider;
import uk.ac.sussex.gdsc.core.data.TrivalueProvider;
import uk.ac.sussex.gdsc.core.data.ValueProvider;
import uk.ac.sussex.gdsc.core.data.VisibleForTesting;
import uk.ac.sussex.gdsc.core.data.procedures.TrivalueProcedure;
import uk.ac.sussex.gdsc.core.logging.Ticker;
import uk.ac.sussex.gdsc.core.logging.TrackProgress;
import uk.ac.sussex.gdsc.core.utils.ConcurrencyUtils;
import uk.ac.sussex.gdsc.core.utils.SimpleArrayUtils;
import uk.ac.sussex.gdsc.core.utils.TurboList;

import org.apache.commons.lang3.concurrent.ConcurrentRuntimeException;
import org.apache.commons.math3.analysis.TrivariateFunction;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.NoDataException;
import org.apache.commons.math3.exception.NonMonotonicSequenceException;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.util.MathArrays.OrderDirection;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.DoublePredicate;

/**
 * Function that implements the <a href="http://en.wikipedia.org/wiki/Tricubic_interpolation">
 * tricubic spline interpolation</a>, as proposed in <blockquote> Tricubic interpolation in three
 * dimensions, F. Lekien and J. Marsden, <em>Int. J. Numer. Meth. Eng</em> 2005; <b>63</b>:455-471
 * </blockquote>.
 */
public class CustomTricubicInterpolatingFunction implements TrivariateFunction {

  /** The tolerance for checking the spline points are uniform. */
  public static final double UNIFORM_TOLERANCE = 1e-6;

  /** The tolerance for checking the spline point spacing is integer. */
  public static final double INTEGER_TOLERANCE = 1e-6;

  /** The upper tolerance for checking the spline point spacing is integer. */
  private static final double ONE_UPPER = 1.0 + INTEGER_TOLERANCE;

  /** The lower tolerance for checking the spline point spacing is integer. */
  private static final double ONE_LOWER = 1.0 - INTEGER_TOLERANCE;

  /**
   * Set to true if the x,y,z spline points are uniformly spaced.
   *
   * <p>This allows the function to be efficiently sampled using precomputed spline coefficients
   * (see {@link #value(int, int, int, double[])})
   */
  private final boolean isUniformFlag;

  /**
   * Set to true if the x,y,z spline points have a grid spacing of 1. Note that the spline points
   * may not be integer values, only the spacing between them.
   *
   * <p>This allows faster computation with no scaling.
   */
  private final boolean isIntegerFlag;

  /**
   * The size of the cubic spline coefficient table.
   */
  private static final int COEFFICIENT_SIZE = 64;

  /**
   * Matrix to compute the spline coefficients from the function values and function derivatives
   * values.
   */
  static final double[][] AINV = {
    //@formatter:off
    { 1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 },
    { 0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 },
    { -3,3,0,0,0,0,0,0,-2,-1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 },
    { 2,-2,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 },
    { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 },
    { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 },
    { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,-3,3,0,0,0,0,0,0,0,0,0,0,0,0,0,0,-2,-1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 },
    { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,2,-2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 },
    { -3,0,3,0,0,0,0,0,0,0,0,0,0,0,0,0,-2,0,-1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 },
    { 0,0,0,0,0,0,0,0,-3,0,3,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,-2,0,-1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 },
    { 9,-9,-9,9,0,0,0,0,6,3,-6,-3,0,0,0,0,6,-6,3,-3,0,0,0,0,0,0,0,0,0,0,0,0,4,2,2,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 },
    { -6,6,6,-6,0,0,0,0,-3,-3,3,3,0,0,0,0,-4,4,-2,2,0,0,0,0,0,0,0,0,0,0,0,0,-2,-2,-1,-1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 },
    { 2,0,-2,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 },
    { 0,0,0,0,0,0,0,0,2,0,-2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 },
    { -6,6,6,-6,0,0,0,0,-4,-2,4,2,0,0,0,0,-3,3,-3,3,0,0,0,0,0,0,0,0,0,0,0,0,-2,-1,-2,-1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 },
    { 4,-4,-4,4,0,0,0,0,2,2,-2,-2,0,0,0,0,2,-2,2,-2,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 },
    { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 },
    { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 },
    { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,-3,3,0,0,0,0,0,0,0,0,0,0,0,0,0,0,-2,-1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 },
    { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,2,-2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 },
    { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 },
    { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0 },
    { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,-3,3,0,0,0,0,0,0,-2,-1,0,0,0,0,0,0 },
    { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,2,-2,0,0,0,0,0,0,1,1,0,0,0,0,0,0 },
    { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,-3,0,3,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,-2,0,-1,0,0,0,0,0,0,0,0,0,0,0,0,0 },
    { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,-3,0,3,0,0,0,0,0,0,0,0,0,0,0,0,0,-2,0,-1,0,0,0,0,0 },
    { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,9,-9,-9,9,0,0,0,0,0,0,0,0,0,0,0,0,6,3,-6,-3,0,0,0,0,6,-6,3,-3,0,0,0,0,4,2,2,1,0,0,0,0 },
    { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,-6,6,6,-6,0,0,0,0,0,0,0,0,0,0,0,0,-3,-3,3,3,0,0,0,0,-4,4,-2,2,0,0,0,0,-2,-2,-1,-1,0,0,0,0 },
    { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,2,0,-2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0 },
    { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,2,0,-2,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,1,0,0,0,0,0 },
    { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,-6,6,6,-6,0,0,0,0,0,0,0,0,0,0,0,0,-4,-2,4,2,0,0,0,0,-3,3,-3,3,0,0,0,0,-2,-1,-2,-1,0,0,0,0 },
    { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,-4,-4,4,0,0,0,0,0,0,0,0,0,0,0,0,2,2,-2,-2,0,0,0,0,2,-2,2,-2,0,0,0,0,1,1,1,1,0,0,0,0 },
    {-3,0,0,0,3,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,-2,0,0,0,-1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 },
    { 0,0,0,0,0,0,0,0,-3,0,0,0,3,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,-2,0,0,0,-1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 },
    { 9,-9,0,0,-9,9,0,0,6,3,0,0,-6,-3,0,0,0,0,0,0,0,0,0,0,6,-6,0,0,3,-3,0,0,0,0,0,0,0,0,0,0,4,2,0,0,2,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 },
    { -6,6,0,0,6,-6,0,0,-3,-3,0,0,3,3,0,0,0,0,0,0,0,0,0,0,-4,4,0,0,-2,2,0,0,0,0,0,0,0,0,0,0,-2,-2,0,0,-1,-1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 },
    { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,-3,0,0,0,3,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,-2,0,0,0,-1,0,0,0,0,0,0,0,0,0,0,0 },
    { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,-3,0,0,0,3,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,-2,0,0,0,-1,0,0,0 },
    { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,9,-9,0,0,-9,9,0,0,0,0,0,0,0,0,0,0,6,3,0,0,-6,-3,0,0,0,0,0,0,0,0,0,0,6,-6,0,0,3,-3,0,0,4,2,0,0,2,1,0,0 },
    { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,-6,6,0,0,6,-6,0,0,0,0,0,0,0,0,0,0,-3,-3,0,0,3,3,0,0,0,0,0,0,0,0,0,0,-4,4,0,0,-2,2,0,0,-2,-2,0,0,-1,-1,0,0 },
    { 9,0,-9,0,-9,0,9,0,0,0,0,0,0,0,0,0,6,0,3,0,-6,0,-3,0,6,0,-6,0,3,0,-3,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,0,2,0,2,0,1,0,0,0,0,0,0,0,0,0 },
    { 0,0,0,0,0,0,0,0,9,0,-9,0,-9,0,9,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,6,0,3,0,-6,0,-3,0,6,0,-6,0,3,0,-3,0,0,0,0,0,0,0,0,0,4,0,2,0,2,0,1,0 },
    { -27,27,27,-27,27,-27,-27,27,-18,-9,18,9,18,9,-18,-9,-18,18,-9,9,18,-18,9,-9,-18,18,18,-18,-9,9,9,-9,-12,-6,-6,-3,12,6,6,3,-12,-6,12,6,-6,-3,6,3,-12,12,-6,6,-6,6,-3,3,-8,-4,-4,-2,-4,-2,-2,-1 },
    { 18,-18,-18,18,-18,18,18,-18,9,9,-9,-9,-9,-9,9,9,12,-12,6,-6,-12,12,-6,6,12,-12,-12,12,6,-6,-6,6,6,6,3,3,-6,-6,-3,-3,6,6,-6,-6,3,3,-3,-3,8,-8,4,-4,4,-4,2,-2,4,4,2,2,2,2,1,1 },
    { -6,0,6,0,6,0,-6,0,0,0,0,0,0,0,0,0,-3,0,-3,0,3,0,3,0,-4,0,4,0,-2,0,2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,-2,0,-2,0,-1,0,-1,0,0,0,0,0,0,0,0,0 },
    { 0,0,0,0,0,0,0,0,-6,0,6,0,6,0,-6,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,-3,0,-3,0,3,0,3,0,-4,0,4,0,-2,0,2,0,0,0,0,0,0,0,0,0,-2,0,-2,0,-1,0,-1,0 },
    { 18,-18,-18,18,-18,18,18,-18,12,6,-12,-6,-12,-6,12,6,9,-9,9,-9,-9,9,-9,9,12,-12,-12,12,6,-6,-6,6,6,3,6,3,-6,-3,-6,-3,8,4,-8,-4,4,2,-4,-2,6,-6,6,-6,3,-3,3,-3,4,2,4,2,2,1,2,1 },
    { -12,12,12,-12,12,-12,-12,12,-6,-6,6,6,6,6,-6,-6,-6,6,-6,6,6,-6,6,-6,-8,8,8,-8,-4,4,4,-4,-3,-3,-3,-3,3,3,3,3,-4,-4,4,4,-2,-2,2,2,-4,4,-4,4,-2,2,-2,2,-2,-2,-2,-2,-1,-1,-1,-1 },
    { 2,0,0,0,-2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 },
    { 0,0,0,0,0,0,0,0,2,0,0,0,-2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 },
    { -6,6,0,0,6,-6,0,0,-4,-2,0,0,4,2,0,0,0,0,0,0,0,0,0,0,-3,3,0,0,-3,3,0,0,0,0,0,0,0,0,0,0,-2,-1,0,0,-2,-1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 },
    { 4,-4,0,0,-4,4,0,0,2,2,0,0,-2,-2,0,0,0,0,0,0,0,0,0,0,2,-2,0,0,2,-2,0,0,0,0,0,0,0,0,0,0,1,1,0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 },
    { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,2,0,0,0,-2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0 },
    { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,2,0,0,0,-2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,1,0,0,0 },
    { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,-6,6,0,0,6,-6,0,0,0,0,0,0,0,0,0,0,-4,-2,0,0,4,2,0,0,0,0,0,0,0,0,0,0,-3,3,0,0,-3,3,0,0,-2,-1,0,0,-2,-1,0,0 },
    { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,-4,0,0,-4,4,0,0,0,0,0,0,0,0,0,0,2,2,0,0,-2,-2,0,0,0,0,0,0,0,0,0,0,2,-2,0,0,2,-2,0,0,1,1,0,0,1,1,0,0 },
    { -6,0,6,0,6,0,-6,0,0,0,0,0,0,0,0,0,-4,0,-2,0,4,0,2,0,-3,0,3,0,-3,0,3,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,-2,0,-1,0,-2,0,-1,0,0,0,0,0,0,0,0,0 },
    { 0,0,0,0,0,0,0,0,-6,0,6,0,6,0,-6,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,-4,0,-2,0,4,0,2,0,-3,0,3,0,-3,0,3,0,0,0,0,0,0,0,0,0,-2,0,-1,0,-2,0,-1,0 },
    { 18,-18,-18,18,-18,18,18,-18,12,6,-12,-6,-12,-6,12,6,12,-12,6,-6,-12,12,-6,6,9,-9,-9,9,9,-9,-9,9,8,4,4,2,-8,-4,-4,-2,6,3,-6,-3,6,3,-6,-3,6,-6,3,-3,6,-6,3,-3,4,2,2,1,4,2,2,1 },
    { -12,12,12,-12,12,-12,-12,12,-6,-6,6,6,6,6,-6,-6,-8,8,-4,4,8,-8,4,-4,-6,6,6,-6,-6,6,6,-6,-4,-4,-2,-2,4,4,2,2,-3,-3,3,3,-3,-3,3,3,-4,4,-2,2,-4,4,-2,2,-2,-2,-1,-1,-2,-2,-1,-1 },
    { 4,0,-4,0,-4,0,4,0,0,0,0,0,0,0,0,0,2,0,2,0,-2,0,-2,0,2,0,-2,0,2,0,-2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,1,0,1,0,1,0,0,0,0,0,0,0,0,0 },
    { 0,0,0,0,0,0,0,0,4,0,-4,0,-4,0,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,2,0,2,0,-2,0,-2,0,2,0,-2,0,2,0,-2,0,0,0,0,0,0,0,0,0,1,0,1,0,1,0,1,0 },
    { -12,12,12,-12,12,-12,-12,12,-8,-4,8,4,8,4,-8,-4,-6,6,-6,6,6,-6,6,-6,-6,6,6,-6,-6,6,6,-6,-4,-2,-4,-2,4,2,4,2,-4,-2,4,2,-4,-2,4,2,-3,3,-3,3,-3,3,-3,3,-2,-1,-2,-1,-2,-1,-2,-1 },
    { 8,-8,-8,8,-8,8,8,-8,4,4,-4,-4,-4,-4,4,4,4,-4,4,-4,-4,4,-4,4,4,-4,-4,4,4,-4,-4,4,2,2,2,2,-2,-2,-2,-2,2,2,-2,-2,2,2,-2,-2,2,-2,2,-2,2,-2,2,-2,1,1,1,1,1,1,1,1 }
    //@formatter:on
  };

  private double[] scale;

  /** Samples x-coordinates. */
  private final double[] xval;
  /** Samples y-coordinates. */
  private final double[] yval;
  /** Samples z-coordinates. */
  private final double[] zval;
  /** Set of cubic splines patching the whole data grid. */
  private final CustomTricubicFunction[][][] splines;

  /** The scale for x. This is the interval between x[i+1] and x[i]. */
  private final double[] xscale;
  /** The scale for y. This is the interval between y[i+1] and y[i]. */
  private final double[] yscale;
  /** The scale for z. This is the interval between z[i+1] and z[i]. */
  private final double[] zscale;

  /**
   * Instantiates a new custom tricubic interpolating function.
   *
   * @param x Sample values of the x-coordinate, in increasing order.
   * @param y Sample values of the y-coordinate, in increasing order.
   * @param z Sample values of the z-coordinate, in increasing order.
   * @param f Values of the function on every grid point.
   * @param dFdX Values of the partial derivative of function with respect to x on every grid point.
   * @param dFdY Values of the partial derivative of function with respect to y on every grid point.
   * @param dFdZ Values of the partial derivative of function with respect to z on every grid point.
   * @param d2FdXdY Values of the cross partial derivative of function on every grid point.
   * @param d2FdXdZ Values of the cross partial derivative of function on every grid point.
   * @param d2FdYdZ Values of the cross partial derivative of function on every grid point.
   * @param d3FdXdYdZ Values of the cross partial derivative of function on every grid point.
   * @param progress the progress
   * @param executorService the executor service
   * @param taskSize the task size (If the number of interpolation nodes is less than this then
   *        multi-threading is not used)
   * @param singlePrecision the single precision flag
   * @throws NoDataException if any of the arrays has zero length.
   * @throws DimensionMismatchException if the various arrays do not contain the expected number of
   *         elements.
   * @throws NonMonotonicSequenceException if {@code x}, {@code y} or {@code z} are not strictly
   *         increasing.
   * @throws ConcurrentRuntimeException if interrupted when using an executor service
   */
  @SuppressWarnings("null")
  //@formatter:off
  public CustomTricubicInterpolatingFunction(ValueProvider x,
                                             ValueProvider y,
                                             ValueProvider z,
                                             final TrivalueProvider f,
                                             final TrivalueProvider dFdX,
                                             final TrivalueProvider dFdY,
                                             final TrivalueProvider dFdZ,
                                             final TrivalueProvider d2FdXdY,
                                             final TrivalueProvider d2FdXdZ,
                                             final TrivalueProvider d2FdYdZ,
                                             final TrivalueProvider d3FdXdYdZ,
                                             TrackProgress progress,
                                             ExecutorService executorService,
                                             long taskSize,
                                             final boolean singlePrecision) {
    //@formatter:on
    final int xLen = x.getLength();
    final int yLen = y.getLength();
    final int zLen = z.getLength();

    // The original only failed if the length was zero. However
    // this function requires two points to interpolate between so
    // check the length is at least 2.
    if (xLen <= 1 || yLen <= 1 || zLen <= 1) {
      throw new NoDataException();
    }
    checkDimensions(xLen, yLen, zLen, f);
    checkDimensions(xLen, yLen, zLen, dFdX);
    checkDimensions(xLen, yLen, zLen, dFdY);
    checkDimensions(xLen, yLen, zLen, dFdZ);
    checkDimensions(xLen, yLen, zLen, d2FdXdY);
    checkDimensions(xLen, yLen, zLen, d2FdXdZ);
    checkDimensions(xLen, yLen, zLen, d2FdYdZ);
    checkDimensions(xLen, yLen, zLen, d3FdXdYdZ);

    checkOrder(x);
    checkOrder(y);
    checkOrder(z);

    xval = x.toArray();
    yval = y.toArray();
    zval = z.toArray();

    //@formatter:off
    isUniformFlag =
        SimpleArrayUtils.isUniform(xval, (xval[1]-xval[0])*UNIFORM_TOLERANCE) &&
        SimpleArrayUtils.isUniform(yval, (yval[1]-yval[0])*UNIFORM_TOLERANCE) &&
        SimpleArrayUtils.isUniform(zval, (zval[1]-zval[0])*UNIFORM_TOLERANCE);
    isIntegerFlag = isUniformFlag &&
        (isIntegerRange(xval)) &&
        (isIntegerRange(yval)) &&
        (isIntegerRange(zval));
    //@formatter:on

    xscale = createScale(xval);
    yscale = createScale(yval);
    zscale = createScale(zval);

    final int lastI = xLen - 1;
    final int lastJ = yLen - 1;
    final int lastK = zLen - 1;
    splines = new CustomTricubicFunction[lastI][lastJ][lastK];

    final long total = (long) lastI * lastJ * lastK;
    long safeTaskSize = Math.max(1, taskSize);

    final boolean threaded = executorService != null && safeTaskSize < total;

    final Ticker ticker = Ticker.create(progress, total, threaded);
    ticker.start();

    if (threaded) {
      final long lastIlastJ = (long) lastI * lastJ;

      // Break this up into reasonable tasks, ensuring we can hold all the futures
      final long[] tmp =
          CustomTricubicInterpolator.getTaskSizeAndNumberOfTasks(total, safeTaskSize);
      safeTaskSize = tmp[0];
      final long nTasks = tmp[1];
      final TurboList<Future<?>> futures = new TurboList<>((int) nTasks);
      for (long i = 0; i < total;) {
        final long from = i;
        final long to = Math.min(i + safeTaskSize, total);
        futures.add(executorService.submit(() -> {
          if (isIntegerFlag) {
            for (long index = from; index < to; index++) {
              //@formatter:off
              buildInteger(f, dFdX, dFdY, dFdZ,
                    d2FdXdY, d2FdXdZ, d2FdYdZ, d3FdXdYdZ,
                    lastI, lastIlastJ, index, ticker, singlePrecision);
              //@formatter:on
            }
          } else {
            for (long index = from; index < to; index++) {
              //@formatter:off
              build(f, dFdX, dFdY, dFdZ,
                    d2FdXdY, d2FdXdZ, d2FdYdZ, d3FdXdYdZ,
                    lastI, lastIlastJ, index, ticker, singlePrecision);
              //@formatter:on
            }
          }
        }));
        i = to;
      }

      ConcurrencyUtils.waitForCompletionUnchecked(futures);
    } else {
      final double[] beta = new double[64];
      if (isIntegerFlag) {
        // We can shortcut if a true integer grid by ignoring the scale
        for (int i = 0; i < lastI; i++) {

          final int ip1 = i + 1;
          for (int j = 0; j < lastJ; j++) {

            final int jp1 = j + 1;
            for (int k = 0; k < lastK; k++) {
              final int kp1 = k + 1;

              beta[0] = f.get(i, j, k);
              beta[1] = f.get(ip1, j, k);
              beta[2] = f.get(i, jp1, k);
              beta[3] = f.get(ip1, jp1, k);
              beta[4] = f.get(i, j, kp1);
              beta[5] = f.get(ip1, j, kp1);
              beta[6] = f.get(i, jp1, kp1);
              beta[7] = f.get(ip1, jp1, kp1);
              beta[8] = dFdX.get(i, j, k);
              beta[9] = dFdX.get(ip1, j, k);
              beta[10] = dFdX.get(i, jp1, k);
              beta[11] = dFdX.get(ip1, jp1, k);
              beta[12] = dFdX.get(i, j, kp1);
              beta[13] = dFdX.get(ip1, j, kp1);
              beta[14] = dFdX.get(i, jp1, kp1);
              beta[15] = dFdX.get(ip1, jp1, kp1);
              beta[16] = dFdY.get(i, j, k);
              beta[17] = dFdY.get(ip1, j, k);
              beta[18] = dFdY.get(i, jp1, k);
              beta[19] = dFdY.get(ip1, jp1, k);
              beta[20] = dFdY.get(i, j, kp1);
              beta[21] = dFdY.get(ip1, j, kp1);
              beta[22] = dFdY.get(i, jp1, kp1);
              beta[23] = dFdY.get(ip1, jp1, kp1);
              beta[24] = dFdZ.get(i, j, k);
              beta[25] = dFdZ.get(ip1, j, k);
              beta[26] = dFdZ.get(i, jp1, k);
              beta[27] = dFdZ.get(ip1, jp1, k);
              beta[28] = dFdZ.get(i, j, kp1);
              beta[29] = dFdZ.get(ip1, j, kp1);
              beta[30] = dFdZ.get(i, jp1, kp1);
              beta[31] = dFdZ.get(ip1, jp1, kp1);
              beta[32] = d2FdXdY.get(i, j, k);
              beta[33] = d2FdXdY.get(ip1, j, k);
              beta[34] = d2FdXdY.get(i, jp1, k);
              beta[35] = d2FdXdY.get(ip1, jp1, k);
              beta[36] = d2FdXdY.get(i, j, kp1);
              beta[37] = d2FdXdY.get(ip1, j, kp1);
              beta[38] = d2FdXdY.get(i, jp1, kp1);
              beta[39] = d2FdXdY.get(ip1, jp1, kp1);
              beta[40] = d2FdXdZ.get(i, j, k);
              beta[41] = d2FdXdZ.get(ip1, j, k);
              beta[42] = d2FdXdZ.get(i, jp1, k);
              beta[43] = d2FdXdZ.get(ip1, jp1, k);
              beta[44] = d2FdXdZ.get(i, j, kp1);
              beta[45] = d2FdXdZ.get(ip1, j, kp1);
              beta[46] = d2FdXdZ.get(i, jp1, kp1);
              beta[47] = d2FdXdZ.get(ip1, jp1, kp1);
              beta[48] = d2FdYdZ.get(i, j, k);
              beta[49] = d2FdYdZ.get(ip1, j, k);
              beta[50] = d2FdYdZ.get(i, jp1, k);
              beta[51] = d2FdYdZ.get(ip1, jp1, k);
              beta[52] = d2FdYdZ.get(i, j, kp1);
              beta[53] = d2FdYdZ.get(ip1, j, kp1);
              beta[54] = d2FdYdZ.get(i, jp1, kp1);
              beta[55] = d2FdYdZ.get(ip1, jp1, kp1);
              beta[56] = d3FdXdYdZ.get(i, j, k);
              beta[57] = d3FdXdYdZ.get(ip1, j, k);
              beta[58] = d3FdXdYdZ.get(i, jp1, k);
              beta[59] = d3FdXdYdZ.get(ip1, jp1, k);
              beta[60] = d3FdXdYdZ.get(i, j, kp1);
              beta[61] = d3FdXdYdZ.get(ip1, j, kp1);
              beta[62] = d3FdXdYdZ.get(i, jp1, kp1);
              beta[63] = d3FdXdYdZ.get(ip1, jp1, kp1);

              setSpline(i, j, k, beta, singlePrecision);
              ticker.tick();
            }
          }
        }
      } else {
        // Not an integer grid. Use the scale.

        for (int i = 0; i < lastI; i++) {

          final int ip1 = i + 1;
          final double xR = xscale[i];
          for (int j = 0; j < lastJ; j++) {

            final int jp1 = j + 1;
            final double yR = yscale[j];
            final double xRyR = xR * yR;
            for (int k = 0; k < lastK; k++) {
              final int kp1 = k + 1;
              final double zR = zscale[k];
              final double xRzR = xR * zR;
              final double yRzR = yR * zR;
              final double xRyRzR = xR * yRzR;

              beta[0] = f.get(i, j, k);
              beta[1] = f.get(ip1, j, k);
              beta[2] = f.get(i, jp1, k);
              beta[3] = f.get(ip1, jp1, k);
              beta[4] = f.get(i, j, kp1);
              beta[5] = f.get(ip1, j, kp1);
              beta[6] = f.get(i, jp1, kp1);
              beta[7] = f.get(ip1, jp1, kp1);
              beta[8] = dFdX.get(i, j, k) * xR;
              beta[9] = dFdX.get(ip1, j, k) * xR;
              beta[10] = dFdX.get(i, jp1, k) * xR;
              beta[11] = dFdX.get(ip1, jp1, k) * xR;
              beta[12] = dFdX.get(i, j, kp1) * xR;
              beta[13] = dFdX.get(ip1, j, kp1) * xR;
              beta[14] = dFdX.get(i, jp1, kp1) * xR;
              beta[15] = dFdX.get(ip1, jp1, kp1) * xR;
              beta[16] = dFdY.get(i, j, k) * yR;
              beta[17] = dFdY.get(ip1, j, k) * yR;
              beta[18] = dFdY.get(i, jp1, k) * yR;
              beta[19] = dFdY.get(ip1, jp1, k) * yR;
              beta[20] = dFdY.get(i, j, kp1) * yR;
              beta[21] = dFdY.get(ip1, j, kp1) * yR;
              beta[22] = dFdY.get(i, jp1, kp1) * yR;
              beta[23] = dFdY.get(ip1, jp1, kp1) * yR;
              beta[24] = dFdZ.get(i, j, k) * zR;
              beta[25] = dFdZ.get(ip1, j, k) * zR;
              beta[26] = dFdZ.get(i, jp1, k) * zR;
              beta[27] = dFdZ.get(ip1, jp1, k) * zR;
              beta[28] = dFdZ.get(i, j, kp1) * zR;
              beta[29] = dFdZ.get(ip1, j, kp1) * zR;
              beta[30] = dFdZ.get(i, jp1, kp1) * zR;
              beta[31] = dFdZ.get(ip1, jp1, kp1) * zR;
              beta[32] = d2FdXdY.get(i, j, k) * xRyR;
              beta[33] = d2FdXdY.get(ip1, j, k) * xRyR;
              beta[34] = d2FdXdY.get(i, jp1, k) * xRyR;
              beta[35] = d2FdXdY.get(ip1, jp1, k) * xRyR;
              beta[36] = d2FdXdY.get(i, j, kp1) * xRyR;
              beta[37] = d2FdXdY.get(ip1, j, kp1) * xRyR;
              beta[38] = d2FdXdY.get(i, jp1, kp1) * xRyR;
              beta[39] = d2FdXdY.get(ip1, jp1, kp1) * xRyR;
              beta[40] = d2FdXdZ.get(i, j, k) * xRzR;
              beta[41] = d2FdXdZ.get(ip1, j, k) * xRzR;
              beta[42] = d2FdXdZ.get(i, jp1, k) * xRzR;
              beta[43] = d2FdXdZ.get(ip1, jp1, k) * xRzR;
              beta[44] = d2FdXdZ.get(i, j, kp1) * xRzR;
              beta[45] = d2FdXdZ.get(ip1, j, kp1) * xRzR;
              beta[46] = d2FdXdZ.get(i, jp1, kp1) * xRzR;
              beta[47] = d2FdXdZ.get(ip1, jp1, kp1) * xRzR;
              beta[48] = d2FdYdZ.get(i, j, k) * yRzR;
              beta[49] = d2FdYdZ.get(ip1, j, k) * yRzR;
              beta[50] = d2FdYdZ.get(i, jp1, k) * yRzR;
              beta[51] = d2FdYdZ.get(ip1, jp1, k) * yRzR;
              beta[52] = d2FdYdZ.get(i, j, kp1) * yRzR;
              beta[53] = d2FdYdZ.get(ip1, j, kp1) * yRzR;
              beta[54] = d2FdYdZ.get(i, jp1, kp1) * yRzR;
              beta[55] = d2FdYdZ.get(ip1, jp1, kp1) * yRzR;
              beta[56] = d3FdXdYdZ.get(i, j, k) * xRyRzR;
              beta[57] = d3FdXdYdZ.get(ip1, j, k) * xRyRzR;
              beta[58] = d3FdXdYdZ.get(i, jp1, k) * xRyRzR;
              beta[59] = d3FdXdYdZ.get(ip1, jp1, k) * xRyRzR;
              beta[60] = d3FdXdYdZ.get(i, j, kp1) * xRyRzR;
              beta[61] = d3FdXdYdZ.get(ip1, j, kp1) * xRyRzR;
              beta[62] = d3FdXdYdZ.get(i, jp1, kp1) * xRyRzR;
              beta[63] = d3FdXdYdZ.get(ip1, jp1, kp1) * xRyRzR;

              setSpline(i, j, k, beta, singlePrecision);
              ticker.tick();
            }
          }
        }
      }
    }
    ticker.stop();
  }

  private static void checkDimensions(int xLen, int yLen, int zLen, TrivalueProvider function) {
    if (xLen != function.getLengthX()) {
      throw new DimensionMismatchException(xLen, function.getLengthX());
    }
    if (yLen != function.getLengthY()) {
      throw new DimensionMismatchException(yLen, function.getLengthY());
    }
    if (zLen != function.getLengthZ()) {
      throw new DimensionMismatchException(zLen, function.getLengthZ());
    }
  }

  /**
   * Check that the given array is sorted.
   *
   * <p>Adapted from org.apache.commons.math3.util.MathArrays.checkOrder(...)
   *
   * @param val Values.
   * @throws NonMonotonicSequenceException if the array is not sorted
   */
  static void checkOrder(ValueProvider val) {
    double previous = val.get(0);
    final int max = val.getLength();

    for (int index = 1; index < max; index++) {
      final double current = val.get(index);
      if (current <= previous) {
        throw new NonMonotonicSequenceException(current, previous, index, OrderDirection.INCREASING,
            true);
      }
      previous = current;
    }
  }

  private static boolean isIntegerRange(double[] x) {
    // Test the full range can be divided into integer steps
    final int upper = x.length - 1;
    return isOne(Math.abs(x[0] - x[upper]) / upper);
  }

  private static boolean isOne(double abs) {
    return abs > ONE_LOWER && abs < ONE_UPPER;
  }

  private static double[] createScale(double[] x) {
    final int n = x.length - 1;
    final double[] scale = new double[n];
    for (int i = 0; i < n; i++) {
      scale[i] = x[i + 1] - x[i];
    }
    return scale;
  }

  // @CHECKSTYLE.OFF: ParameterName

  private void buildInteger(final TrivalueProvider f, final TrivalueProvider dFdX,
      final TrivalueProvider dFdY, final TrivalueProvider dFdZ, final TrivalueProvider d2FdXdY,
      final TrivalueProvider d2FdXdZ, final TrivalueProvider d2FdYdZ,
      final TrivalueProvider d3FdXdYdZ, final int lastI, final long lastIlastJ, long index,
      final Ticker ticker, boolean singlePrecision) {
    final int k = (int) (index / lastIlastJ);
    final long mod = index % lastIlastJ;
    final int j = (int) (mod / lastI);
    final int i = (int) (mod % lastI);
    final int ip1 = i + 1;
    final int jp1 = j + 1;
    final int kp1 = k + 1;

    final double[] beta = new double[64];
    beta[0] = f.get(i, j, k);
    beta[1] = f.get(ip1, j, k);
    beta[2] = f.get(i, jp1, k);
    beta[3] = f.get(ip1, jp1, k);
    beta[4] = f.get(i, j, kp1);
    beta[5] = f.get(ip1, j, kp1);
    beta[6] = f.get(i, jp1, kp1);
    beta[7] = f.get(ip1, jp1, kp1);
    beta[8] = dFdX.get(i, j, k);
    beta[9] = dFdX.get(ip1, j, k);
    beta[10] = dFdX.get(i, jp1, k);
    beta[11] = dFdX.get(ip1, jp1, k);
    beta[12] = dFdX.get(i, j, kp1);
    beta[13] = dFdX.get(ip1, j, kp1);
    beta[14] = dFdX.get(i, jp1, kp1);
    beta[15] = dFdX.get(ip1, jp1, kp1);
    beta[16] = dFdY.get(i, j, k);
    beta[17] = dFdY.get(ip1, j, k);
    beta[18] = dFdY.get(i, jp1, k);
    beta[19] = dFdY.get(ip1, jp1, k);
    beta[20] = dFdY.get(i, j, kp1);
    beta[21] = dFdY.get(ip1, j, kp1);
    beta[22] = dFdY.get(i, jp1, kp1);
    beta[23] = dFdY.get(ip1, jp1, kp1);
    beta[24] = dFdZ.get(i, j, k);
    beta[25] = dFdZ.get(ip1, j, k);
    beta[26] = dFdZ.get(i, jp1, k);
    beta[27] = dFdZ.get(ip1, jp1, k);
    beta[28] = dFdZ.get(i, j, kp1);
    beta[29] = dFdZ.get(ip1, j, kp1);
    beta[30] = dFdZ.get(i, jp1, kp1);
    beta[31] = dFdZ.get(ip1, jp1, kp1);
    beta[32] = d2FdXdY.get(i, j, k);
    beta[33] = d2FdXdY.get(ip1, j, k);
    beta[34] = d2FdXdY.get(i, jp1, k);
    beta[35] = d2FdXdY.get(ip1, jp1, k);
    beta[36] = d2FdXdY.get(i, j, kp1);
    beta[37] = d2FdXdY.get(ip1, j, kp1);
    beta[38] = d2FdXdY.get(i, jp1, kp1);
    beta[39] = d2FdXdY.get(ip1, jp1, kp1);
    beta[40] = d2FdXdZ.get(i, j, k);
    beta[41] = d2FdXdZ.get(ip1, j, k);
    beta[42] = d2FdXdZ.get(i, jp1, k);
    beta[43] = d2FdXdZ.get(ip1, jp1, k);
    beta[44] = d2FdXdZ.get(i, j, kp1);
    beta[45] = d2FdXdZ.get(ip1, j, kp1);
    beta[46] = d2FdXdZ.get(i, jp1, kp1);
    beta[47] = d2FdXdZ.get(ip1, jp1, kp1);
    beta[48] = d2FdYdZ.get(i, j, k);
    beta[49] = d2FdYdZ.get(ip1, j, k);
    beta[50] = d2FdYdZ.get(i, jp1, k);
    beta[51] = d2FdYdZ.get(ip1, jp1, k);
    beta[52] = d2FdYdZ.get(i, j, kp1);
    beta[53] = d2FdYdZ.get(ip1, j, kp1);
    beta[54] = d2FdYdZ.get(i, jp1, kp1);
    beta[55] = d2FdYdZ.get(ip1, jp1, kp1);
    beta[56] = d3FdXdYdZ.get(i, j, k);
    beta[57] = d3FdXdYdZ.get(ip1, j, k);
    beta[58] = d3FdXdYdZ.get(i, jp1, k);
    beta[59] = d3FdXdYdZ.get(ip1, jp1, k);
    beta[60] = d3FdXdYdZ.get(i, j, kp1);
    beta[61] = d3FdXdYdZ.get(ip1, j, kp1);
    beta[62] = d3FdXdYdZ.get(i, jp1, kp1);
    beta[63] = d3FdXdYdZ.get(ip1, jp1, kp1);

    setSpline(i, j, k, beta, singlePrecision);
    ticker.tick();
  }

  private void build(final TrivalueProvider f, final TrivalueProvider dFdX,
      final TrivalueProvider dFdY, final TrivalueProvider dFdZ, final TrivalueProvider d2FdXdY,
      final TrivalueProvider d2FdXdZ, final TrivalueProvider d2FdYdZ,
      final TrivalueProvider d3FdXdYdZ, final int lastI, final long lastIlastJ, long index,
      final Ticker ticker, boolean singlePrecision) {
    final int k = (int) (index / lastIlastJ);
    final long mod = index % lastIlastJ;
    final int j = (int) (mod / lastI);
    final int i = (int) (mod % lastI);
    final int ip1 = i + 1;
    final int jp1 = j + 1;
    final int kp1 = k + 1;
    final double xR = xscale[i];
    final double yR = yscale[j];
    final double xRyR = xR * yR;
    final double zR = zscale[k];
    final double xRzR = xR * zR;
    final double yRzR = yR * zR;
    final double xRyRzR = xR * yRzR;

    final double[] beta = new double[64];
    beta[0] = f.get(i, j, k);
    beta[1] = f.get(ip1, j, k);
    beta[2] = f.get(i, jp1, k);
    beta[3] = f.get(ip1, jp1, k);
    beta[4] = f.get(i, j, kp1);
    beta[5] = f.get(ip1, j, kp1);
    beta[6] = f.get(i, jp1, kp1);
    beta[7] = f.get(ip1, jp1, kp1);
    beta[8] = dFdX.get(i, j, k) * xR;
    beta[9] = dFdX.get(ip1, j, k) * xR;
    beta[10] = dFdX.get(i, jp1, k) * xR;
    beta[11] = dFdX.get(ip1, jp1, k) * xR;
    beta[12] = dFdX.get(i, j, kp1) * xR;
    beta[13] = dFdX.get(ip1, j, kp1) * xR;
    beta[14] = dFdX.get(i, jp1, kp1) * xR;
    beta[15] = dFdX.get(ip1, jp1, kp1) * xR;
    beta[16] = dFdY.get(i, j, k) * yR;
    beta[17] = dFdY.get(ip1, j, k) * yR;
    beta[18] = dFdY.get(i, jp1, k) * yR;
    beta[19] = dFdY.get(ip1, jp1, k) * yR;
    beta[20] = dFdY.get(i, j, kp1) * yR;
    beta[21] = dFdY.get(ip1, j, kp1) * yR;
    beta[22] = dFdY.get(i, jp1, kp1) * yR;
    beta[23] = dFdY.get(ip1, jp1, kp1) * yR;
    beta[24] = dFdZ.get(i, j, k) * zR;
    beta[25] = dFdZ.get(ip1, j, k) * zR;
    beta[26] = dFdZ.get(i, jp1, k) * zR;
    beta[27] = dFdZ.get(ip1, jp1, k) * zR;
    beta[28] = dFdZ.get(i, j, kp1) * zR;
    beta[29] = dFdZ.get(ip1, j, kp1) * zR;
    beta[30] = dFdZ.get(i, jp1, kp1) * zR;
    beta[31] = dFdZ.get(ip1, jp1, kp1) * zR;
    beta[32] = d2FdXdY.get(i, j, k) * xRyR;
    beta[33] = d2FdXdY.get(ip1, j, k) * xRyR;
    beta[34] = d2FdXdY.get(i, jp1, k) * xRyR;
    beta[35] = d2FdXdY.get(ip1, jp1, k) * xRyR;
    beta[36] = d2FdXdY.get(i, j, kp1) * xRyR;
    beta[37] = d2FdXdY.get(ip1, j, kp1) * xRyR;
    beta[38] = d2FdXdY.get(i, jp1, kp1) * xRyR;
    beta[39] = d2FdXdY.get(ip1, jp1, kp1) * xRyR;
    beta[40] = d2FdXdZ.get(i, j, k) * xRzR;
    beta[41] = d2FdXdZ.get(ip1, j, k) * xRzR;
    beta[42] = d2FdXdZ.get(i, jp1, k) * xRzR;
    beta[43] = d2FdXdZ.get(ip1, jp1, k) * xRzR;
    beta[44] = d2FdXdZ.get(i, j, kp1) * xRzR;
    beta[45] = d2FdXdZ.get(ip1, j, kp1) * xRzR;
    beta[46] = d2FdXdZ.get(i, jp1, kp1) * xRzR;
    beta[47] = d2FdXdZ.get(ip1, jp1, kp1) * xRzR;
    beta[48] = d2FdYdZ.get(i, j, k) * yRzR;
    beta[49] = d2FdYdZ.get(ip1, j, k) * yRzR;
    beta[50] = d2FdYdZ.get(i, jp1, k) * yRzR;
    beta[51] = d2FdYdZ.get(ip1, jp1, k) * yRzR;
    beta[52] = d2FdYdZ.get(i, j, kp1) * yRzR;
    beta[53] = d2FdYdZ.get(ip1, j, kp1) * yRzR;
    beta[54] = d2FdYdZ.get(i, jp1, kp1) * yRzR;
    beta[55] = d2FdYdZ.get(ip1, jp1, kp1) * yRzR;
    beta[56] = d3FdXdYdZ.get(i, j, k) * xRyRzR;
    beta[57] = d3FdXdYdZ.get(ip1, j, k) * xRyRzR;
    beta[58] = d3FdXdYdZ.get(i, jp1, k) * xRyRzR;
    beta[59] = d3FdXdYdZ.get(ip1, jp1, k) * xRyRzR;
    beta[60] = d3FdXdYdZ.get(i, j, kp1) * xRyRzR;
    beta[61] = d3FdXdYdZ.get(ip1, j, kp1) * xRyRzR;
    beta[62] = d3FdXdYdZ.get(i, jp1, kp1) * xRyRzR;
    beta[63] = d3FdXdYdZ.get(ip1, jp1, kp1) * xRyRzR;

    setSpline(i, j, k, beta, singlePrecision);
    ticker.tick();
  }

  private void setSpline(int i, int j, int k, double[] beta, boolean singlePrecision) {
    final double[] a = computeCoefficientsInlineCollectTerms(beta);
    splines[i][j][k] = (singlePrecision) ? new FloatCustomTricubicFunction(a)
        : new DoubleCustomTricubicFunction(a);
  }

  // @CHECKSTYLE.ON: ParameterName

  /**
   * Instantiates a new custom tricubic interpolating function.
   *
   * @param x Sample values of the x-coordinate, in increasing order.
   * @param y Sample values of the y-coordinate, in increasing order.
   * @param z Sample values of the z-coordinate, in increasing order.
   * @param splines the splines
   * @throws NoDataException if any of the arrays has zero length.
   * @throws DimensionMismatchException if the various arrays do not contain the expected number of
   *         elements.
   * @throws NonMonotonicSequenceException if {@code x}, {@code y} or {@code z} are not strictly
   *         increasing.
   */
  //@formatter:off
  CustomTricubicInterpolatingFunction(double[] x,
                                      double[] y,
                                      double[] z,
                                      CustomTricubicFunction[][][] splines) {
    this(new DoubleArrayValueProvider(x),
       new DoubleArrayValueProvider(y),
       new DoubleArrayValueProvider(z),
       splines);
  }
  //@formatter:on

  /**
   * Instantiates a new custom tricubic interpolating function.
   *
   * @param x Sample values of the x-coordinate, in increasing order.
   * @param y Sample values of the y-coordinate, in increasing order.
   * @param z Sample values of the z-coordinate, in increasing order.
   * @param splines the splines
   * @throws NoDataException if any of the arrays has zero length.
   * @throws DimensionMismatchException if the various arrays do not contain the expected number of
   *         elements.
   * @throws NonMonotonicSequenceException if {@code x}, {@code y} or {@code z} are not strictly
   *         increasing.
   */
  //@formatter:off
  CustomTricubicInterpolatingFunction(ValueProvider x,
                                      ValueProvider y,
                                      ValueProvider z,
                                      CustomTricubicFunction[][][] splines) {
    //@formatter:on
    final int xLen = x.getLength();
    final int yLen = y.getLength();
    final int zLen = z.getLength();

    // The original only failed if the length was zero. However
    // this function requires two points to interpolate between so
    // check the length is at least 2.
    if (xLen <= 1 || yLen <= 1 || zLen <= 1) {
      throw new NoDataException();
    }

    final int lastI = xLen - 1;
    if (lastI != splines.length) {
      throw new DimensionMismatchException(lastI, splines.length);
    }
    final int lastJ = yLen - 1;
    final int lastK = zLen - 1;
    for (int i = 0; i < lastI; i++) {
      if (lastJ != splines[i].length) {
        throw new DimensionMismatchException(lastJ, splines[i].length);
      }
      for (int j = 0; j < lastJ; j++) {
        if (lastK != splines[i][j].length) {
          throw new DimensionMismatchException(lastK, splines[i][j].length);
        }
      }
    }

    checkOrder(x);
    checkOrder(y);
    checkOrder(z);

    xval = x.toArray();
    yval = y.toArray();
    zval = z.toArray();

    //@formatter:off
    isUniformFlag =
        SimpleArrayUtils.isUniform(xval, (xval[1]-xval[0])*UNIFORM_TOLERANCE) &&
        SimpleArrayUtils.isUniform(yval, (yval[1]-yval[0])*UNIFORM_TOLERANCE) &&
        SimpleArrayUtils.isUniform(zval, (zval[1]-zval[0])*UNIFORM_TOLERANCE);
    isIntegerFlag = isUniformFlag &&
        (isIntegerRange(xval)) &&
        (isIntegerRange(yval)) &&
        (isIntegerRange(zval));
    //@formatter:on

    xscale = createScale(xval);
    yscale = createScale(yval);
    zscale = createScale(zval);

    this.splines = splines;
  }

  //@formatter:on

  /**
   * Find the search index.
   *
   * @param value the value
   * @param values the values
   * @return the index in {@code values} corresponding to the interval containing {@code value}, or
   *         {@code -1} if {@code value} is out of the range defined by the end values of
   *         {@code values}.
   * @throws OutOfRangeException if any of the variables is outside its interpolation range.
   */
  private int searchIndex(double value, double[] values) {
    if (isIntegerFlag) {
      final int high = values.length - 1;

      if (value < values[0] || value > values[high]) {
        throw new OutOfRangeException(value, values[0], values[high]);
      }
      // Edge case for the final node
      if (value == values[high]) {
        return high - 1;
      }

      return (int) Math.floor(value - values[0]);
    }

    return searchIndexBinarySearch(value, values);
  }

  /**
   * Find the search index using a binary search.
   *
   * @param value the value
   * @param values the values
   * @return the index in {@code values} corresponding to the interval containing {@code value}, or
   *         {@code -1} if {@code value} is out of the range defined by the end values of
   *         {@code values}.
   * @throws OutOfRangeException if any of the variables is outside its interpolation range.
   */
  private static int searchIndexBinarySearch(double value, double[] values) {
    // Use a Binary search.
    // We want to find the index equal to or before the key.
    final int high = values.length - 1;

    if (value < values[0] || value > values[high]) {
      throw new OutOfRangeException(value, values[0], values[high]);
    }

    int index = binarySearch0(values, 0, values.length, value);
    if (index < 0) {
      // Not found. Convert to the insertion point.
      // We have already checked the upper bound and so we know the insertion point is
      // below 'high'.
      index = (-index - 1);
    }
    // Return the index before. This makes index in the range 0 to high-1.
    return (index > 0) ? index - 1 : index;
  }

  /**
   * Binary search. Copied from Arrays.binarySearch0(...) and removed the check for equal values
   * using the double bits (e.g.-0 returns the same as +0 even though they are different). This thus
   * does not support NaN values.
   *
   * <p>Searches a range of the specified array of doubles for the specified value using the binary
   * search algorithm. The range must be sorted (as by the
   * {@link java.util.Arrays#sort(double[], int, int)} method) prior to making this call. If it is
   * not sorted, the results are undefined. If the range contains multiple elements with the
   * specified value, there is no guarantee which one will be found. This method considers all NaN
   * values to be equivalent and equal.
   *
   * @param array the array to be searched
   * @param fromIndex the index of the first element (inclusive) to be searched
   * @param toIndex the index of the last element (exclusive) to be searched
   * @param key the value to be searched for
   * @return index of the search key, if it is contained in the array within the specified range;
   *         otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>. The <i>insertion point</i> is
   *         defined as the point at which the key would be inserted into the array: the index of
   *         the first element in the range greater than the key, or <tt>toIndex</tt> if all
   *         elements in the range are less than the specified key. Note that this guarantees that
   *         the return value will be &gt;= 0 if and only if the key is found.
   */
  private static int binarySearch0(double[] array, int fromIndex, int toIndex, double key) {
    int low = fromIndex;
    int high = toIndex - 1;

    while (low <= high) {
      final int mid = (low + high) >>> 1;
      final double midVal = array[mid];

      if (midVal < key) {
        low = mid + 1; // Neither val is NaN, thisVal is smaller
      } else if (midVal > key) {
        high = mid - 1; // Neither val is NaN, thisVal is larger
      } else {
        // Ignore NaN checks
        return mid; // Key found
      }
    }
    return -(low + 1); // key not found.
  }

  /**
   * Gets the spline position.
   *
   * @param xval the spline values
   * @param xscale the scale (xval[i+1] - xval[i])
   * @param x the value
   * @return the spline position
   * @throws OutOfRangeException if the value is outside its interpolation range.
   */
  private IndexedCubicSplinePosition getSplinePosition(double[] xval, double[] xscale, double x) {
    final int i = searchIndex(x, xval);
    if (isIntegerFlag) {
      return new IndexedCubicSplinePosition(i, x - xval[i], false);
    }
    final double xN = (x - xval[i]) / xscale[i];
    return new ScaledIndexedCubicSplinePosition(i, xN, xscale[i], false);
  }

  /**
   * Gets the x spline position.
   *
   * @param value the x value
   * @return the x spline position
   * @throws OutOfRangeException if the value is outside its interpolation range.
   */
  public IndexedCubicSplinePosition getXSplinePosition(double value) {
    return getSplinePosition(xval, xscale, value);
  }

  /**
   * Gets the y spline position.
   *
   * @param value the y value
   * @return the y spline position
   * @throws OutOfRangeException if the value is outside its interpolation range.
   */
  public IndexedCubicSplinePosition getYSplinePosition(double value) {
    return getSplinePosition(yval, yscale, value);
  }

  /**
   * Gets the z spline position.
   *
   * @param value the z value
   * @return the z spline position
   * @throws OutOfRangeException if the value is outside its interpolation range.
   */
  public IndexedCubicSplinePosition getZSplinePosition(double value) {
    return getSplinePosition(zval, zscale, value);
  }

  /**
   * {@inheritDoc}
   *
   * @throws OutOfRangeException if any of the variables is outside its interpolation range.
   */
  @Override
  public double value(double x, double y, double z) {
    final int i = searchIndex(x, xval);
    final int j = searchIndex(y, yval);
    final int k = searchIndex(z, zval);

    if (isIntegerFlag) {
      return splines[i][j][k].value(x - xval[i], y - yval[j], z - zval[k]);
    }

    final double xN = (x - xval[i]) / (xscale[i]);
    final double yN = (y - yval[j]) / (yscale[j]);
    final double zN = (z - zval[k]) / (zscale[k]);

    return splines[i][j][k].value(xN, yN, zN);
  }

  /**
   * Get the interpolated value using pre-computed spline positions.
   *
   * @param x the x
   * @param y the y
   * @param z the z
   * @return the value
   * @throws ArrayIndexOutOfBoundsException if the spline node does not exist
   */
  public double value(IndexedCubicSplinePosition x, IndexedCubicSplinePosition y,
      IndexedCubicSplinePosition z) {
    return splines[x.index][y.index][z.index].value(x, y, z);
  }

  /**
   * Get the interpolated value using pre-computed spline coefficient power table.
   *
   * @param xindex the x spline position
   * @param yindex the y spline position
   * @param zindex the z spline position
   * @param table the table of 64 precomputed power coefficients
   * @return the value
   * @throws ArrayIndexOutOfBoundsException if the spline node does not exist
   * @see CustomTricubicFunction#computePowerTable(double, double, double)
   */
  public double value(int xindex, int yindex, int zindex, double[] table) {
    return splines[xindex][yindex][zindex].value(table);
  }

  /**
   * Get the interpolated value using pre-computed spline coefficient power table.
   *
   * @param xindex the x spline position
   * @param yindex the y spline position
   * @param zindex the z spline position
   * @param table the table of 64 precomputed power coefficients
   * @return the value
   * @throws ArrayIndexOutOfBoundsException if the spline node does not exist
   * @see CustomTricubicFunction#computePowerTable(double, double, double)
   */
  public double value(int xindex, int yindex, int zindex, float[] table) {
    return splines[xindex][yindex][zindex].value(table);
  }

  /**
   * Get the interpolated value and partial first-order derivatives.
   *
   * @param x the x value
   * @param y the y value
   * @param z the z value
   * @param derivative1 the partial first order derivatives with respect to x,y,z
   * @return the value
   * @throws OutOfRangeException if any of the variables is outside its interpolation range.
   */
  public double value(double x, double y, double z, double[] derivative1) {
    final int i = searchIndex(x, xval);
    final int j = searchIndex(y, yval);
    final int k = searchIndex(z, zval);

    if (isIntegerFlag) {
      return splines[i][j][k].value(x - xval[i], y - yval[j], z - zval[k], derivative1);
    }

    final double xN = (x - xval[i]) / xscale[i];
    final double yN = (y - yval[j]) / yscale[j];
    final double zN = (z - zval[k]) / zscale[k];

    final double value = splines[i][j][k].value(xN, yN, zN, derivative1);
    derivative1[0] /= xscale[i];
    derivative1[1] /= yscale[j];
    derivative1[2] /= zscale[k];
    return value;
  }

  /**
   * Get the interpolated value and partial first-order derivatives using pre-computed spline
   * positions. The positions must be computed by this function to ensure correct scaling.
   *
   * @param x the x value
   * @param y the y value
   * @param z the z value
   * @param derivative1 the partial first order derivatives with respect to x,y,z
   * @return the value
   * @throws ArrayIndexOutOfBoundsException if the spline node does not exist
   */
  public double value(IndexedCubicSplinePosition x, IndexedCubicSplinePosition y,
      IndexedCubicSplinePosition z, double[] derivative1) {
    return splines[x.index][y.index][z.index].value(x, y, z, derivative1);
  }

  /**
   * Get the interpolated value and partial first-order derivatives using pre-computed spline
   * coefficient power table.
   *
   * @param xindex the x spline position
   * @param yindex the y spline position
   * @param zindex the z spline position
   * @param table the power table
   * @param derivative1 the partial first order derivatives with respect to x,y,z
   * @return the value
   * @throws ArrayIndexOutOfBoundsException if the spline node does not exist
   * @see CustomTricubicFunction#computePowerTable(double, double, double)
   */
  public double value(int xindex, int yindex, int zindex, double[] table, double[] derivative1) {
    if (isIntegerFlag) {
      return splines[xindex][yindex][zindex].value(table, derivative1);
    }
    final double value = splines[xindex][yindex][zindex].value(table, derivative1);
    derivative1[0] /= xscale[xindex];
    derivative1[1] /= yscale[yindex];
    derivative1[2] /= zscale[zindex];
    return value;
  }

  /**
   * Get the interpolated value and partial first-order derivatives using pre-computed spline
   * coefficient power table.
   *
   * @param xindex the x spline position
   * @param yindex the y spline position
   * @param zindex the z spline position
   * @param table the power table
   * @param derivative1 the partial first order derivatives with respect to x,y,z
   * @return the value
   * @throws ArrayIndexOutOfBoundsException if the spline node does not exist
   * @see CustomTricubicFunction#computePowerTable(double, double, double)
   */
  public double value(int xindex, int yindex, int zindex, float[] table, double[] derivative1) {
    if (isIntegerFlag) {
      return splines[xindex][yindex][zindex].value(table, derivative1);
    }
    final double value = splines[xindex][yindex][zindex].value(table, derivative1);
    derivative1[0] /= xscale[xindex];
    derivative1[1] /= yscale[yindex];
    derivative1[2] /= zscale[zindex];
    return value;
  }

  /**
   * Get the interpolated value and partial first-order derivatives using pre-computed spline
   * coefficient power table.
   *
   * @param xindex the x spline position
   * @param yindex the y spline position
   * @param zindex the z spline position
   * @param table the power table
   * @param table2 the power table scaled by 2
   * @param table3 the power table scaled by 3
   * @param derivative1 the partial first order derivatives with respect to x,y,z
   * @return the value
   * @throws ArrayIndexOutOfBoundsException if the spline node does not exist
   * @see CustomTricubicFunction#computePowerTable(double, double, double)
   */
  public double value(int xindex, int yindex, int zindex, double[] table, double[] table2,
      double[] table3, double[] derivative1) {
    if (isIntegerFlag) {
      return splines[xindex][yindex][zindex].value(table, table2, table3, derivative1);
    }
    final double value = splines[xindex][yindex][zindex].value(table, table2, table3, derivative1);
    derivative1[0] /= xscale[xindex];
    derivative1[1] /= yscale[yindex];
    derivative1[2] /= zscale[zindex];
    return value;
  }

  /**
   * Get the interpolated value and partial first-order derivatives using pre-computed spline
   * coefficient power table.
   *
   * @param xindex the x spline position
   * @param yindex the y spline position
   * @param zindex the z spline position
   * @param table the power table
   * @param table2 the power table scaled by 2
   * @param table3 the power table scaled by 3
   * @param derivative1 the partial first order derivatives with respect to x,y,z
   * @return the value
   * @throws ArrayIndexOutOfBoundsException if the spline node does not exist
   * @see CustomTricubicFunction#computePowerTable(double, double, double)
   */
  public double value(int xindex, int yindex, int zindex, float[] table, float[] table2,
      float[] table3, double[] derivative1) {
    if (isIntegerFlag) {
      return splines[xindex][yindex][zindex].value(table, table2, table3, derivative1);
    }
    final double value = splines[xindex][yindex][zindex].value(table, table2, table3, derivative1);
    derivative1[0] /= xscale[xindex];
    derivative1[1] /= yscale[yindex];
    derivative1[2] /= zscale[zindex];
    return value;
  }

  /**
   * Get the interpolated value and partial first-order and second-order derivatives.
   *
   * @param x the x value
   * @param y the y value
   * @param z the z value
   * @param derivative1 the partial first order derivatives with respect to x,y,z
   * @param derivative2 the partial second order derivatives with respect to x,y,z
   * @return the value
   * @throws OutOfRangeException if any of the variables is outside its interpolation range.
   */
  public double value(double x, double y, double z, double[] derivative1, double[] derivative2) {
    final int i = searchIndex(x, xval);
    final int j = searchIndex(y, yval);
    final int k = searchIndex(z, zval);

    if (isIntegerFlag) {
      return splines[i][j][k].value(x - xval[i], y - yval[j], z - zval[k], derivative1,
          derivative2);
    }

    final double xN = (x - xval[i]) / xscale[i];
    final double yN = (y - yval[j]) / yscale[j];
    final double zN = (z - zval[k]) / zscale[k];

    final double value = splines[i][j][k].value(xN, yN, zN, derivative1, derivative2);
    derivative1[0] /= xscale[i];
    derivative1[1] /= yscale[j];
    derivative1[2] /= zscale[k];
    derivative2[0] /= xscale[i] * xscale[i];
    derivative2[1] /= yscale[j] * yscale[j];
    derivative2[2] /= zscale[k] * zscale[k];
    return value;
  }

  /**
   * Get the interpolated value and partial first-order and second-order derivatives using
   * pre-computed spline positions.
   *
   * @param x the x value
   * @param y the y value
   * @param z the z value
   * @param derivative1 the partial first order derivatives with respect to x,y,z
   * @param derivative2 the partial second order derivatives with respect to x,y,z
   * @return the value
   * @throws ArrayIndexOutOfBoundsException if the spline node does not exist
   */
  public double value(IndexedCubicSplinePosition x, IndexedCubicSplinePosition y,
      IndexedCubicSplinePosition z, double[] derivative1, double[] derivative2) {
    return splines[x.index][y.index][z.index].value(x, y, z, derivative1, derivative2);
  }

  /**
   * Get the interpolated value and partial first-order and second-order derivatives using
   * pre-computed spline coefficient power table.
   *
   * @param xindex the x spline position
   * @param yindex the y spline position
   * @param zindex the z spline position
   * @param table the power table
   * @param derivative1 the partial first order derivatives with respect to x,y,z
   * @param derivative2 the partial second order derivatives with respect to x,y,z
   * @return the value
   * @throws ArrayIndexOutOfBoundsException if the spline node does not exist
   * @see CustomTricubicFunction#computePowerTable(double, double, double)
   */
  public double value(int xindex, int yindex, int zindex, double[] table, double[] derivative1,
      double[] derivative2) {
    if (isIntegerFlag) {
      return splines[xindex][yindex][zindex].value(table, derivative1, derivative2);
    }
    final double value = splines[xindex][yindex][zindex].value(table, derivative1, derivative2);
    derivative1[0] /= xscale[xindex];
    derivative1[1] /= yscale[yindex];
    derivative1[2] /= zscale[zindex];
    derivative2[0] /= xscale[xindex] * xscale[xindex];
    derivative2[1] /= yscale[yindex] * yscale[yindex];
    derivative2[2] /= zscale[zindex] * zscale[zindex];
    return value;
  }

  /**
   * Get the interpolated value and partial first-order and second-order derivatives using
   * pre-computed spline coefficient power table.
   *
   * @param xindex the x spline position
   * @param yindex the y spline position
   * @param zindex the z spline position
   * @param table the power table
   * @param derivative1 the partial first order derivatives with respect to x,y,z
   * @param derivative2 the partial second order derivatives with respect to x,y,z
   * @return the value
   * @throws ArrayIndexOutOfBoundsException if the spline node does not exist
   * @see CustomTricubicFunction#computePowerTable(double, double, double)
   */
  public double value(int xindex, int yindex, int zindex, float[] table, double[] derivative1,
      double[] derivative2) {
    if (isIntegerFlag) {
      return splines[xindex][yindex][zindex].value(table, derivative1, derivative2);
    }
    final double value = splines[xindex][yindex][zindex].value(table, derivative1, derivative2);
    derivative1[0] /= xscale[xindex];
    derivative1[1] /= yscale[yindex];
    derivative1[2] /= zscale[zindex];
    derivative2[0] /= xscale[xindex] * xscale[xindex];
    derivative2[1] /= yscale[yindex] * yscale[yindex];
    derivative2[2] /= zscale[zindex] * zscale[zindex];
    return value;
  }

  /**
   * Get the interpolated value and partial first-order and second-order derivatives using
   * pre-computed spline coefficient power table.
   *
   * @param xindex the x spline position
   * @param yindex the y spline position
   * @param zindex the z spline position
   * @param table the power table
   * @param table2 the power table scaled by 2
   * @param table3 the power table scaled by 3
   * @param table6 the power table scaled by 6
   * @param derivative1 the partial first order derivatives with respect to x,y,z
   * @param derivative2 the partial second order derivatives with respect to x,y,z
   * @return the value
   * @throws ArrayIndexOutOfBoundsException if the spline node does not exist
   * @see CustomTricubicFunction#computePowerTable(double, double, double)
   */
  public double value(int xindex, int yindex, int zindex, double[] table, double[] table2,
      double[] table3, double[] table6, double[] derivative1, double[] derivative2) {
    if (isIntegerFlag) {
      return splines[xindex][yindex][zindex].value(table, table2, table3, table6, derivative1,
          derivative2);
    }
    final double value = splines[xindex][yindex][zindex].value(table, table2, table3, table6,
        derivative1, derivative2);
    derivative1[0] /= xscale[xindex];
    derivative1[1] /= yscale[yindex];
    derivative1[2] /= zscale[zindex];
    derivative2[0] /= xscale[xindex] * xscale[xindex];
    derivative2[1] /= yscale[yindex] * yscale[yindex];
    derivative2[2] /= zscale[zindex] * zscale[zindex];
    return value;
  }

  /**
   * Get the interpolated value and partial first-order and second-order derivatives using
   * pre-computed spline coefficient power table.
   *
   * @param xindex the x spline position
   * @param yindex the y spline position
   * @param zindex the z spline position
   * @param table the power table
   * @param table2 the power table scaled by 2
   * @param table3 the power table scaled by 3
   * @param table6 the power table scaled by 6
   * @param derivative1 the partial first order derivatives with respect to x,y,z
   * @param derivative2 the partial second order derivatives with respect to x,y,z
   * @return the value
   * @throws ArrayIndexOutOfBoundsException if the spline node does not exist
   * @see CustomTricubicFunction#computePowerTable(double, double, double)
   */
  public double value(int xindex, int yindex, int zindex, float[] table, float[] table2,
      float[] table3, float[] table6, double[] derivative1, double[] derivative2) {
    if (isIntegerFlag) {
      return splines[xindex][yindex][zindex].value(table, table2, table3, table6, derivative1,
          derivative2);
    }
    final double value = splines[xindex][yindex][zindex].value(table, table2, table3, table6,
        derivative1, derivative2);
    derivative1[0] /= xscale[xindex];
    derivative1[1] /= yscale[yindex];
    derivative1[2] /= zscale[zindex];
    derivative2[0] /= xscale[xindex] * xscale[xindex];
    derivative2[1] /= yscale[yindex] * yscale[yindex];
    derivative2[2] /= zscale[zindex] * zscale[zindex];
    return value;
  }

  /**
   * Gets the scale to convert the cube interval 0-1 back to correctly scaled values.
   *
   * <p>This is only valid if the function is uniform on each input axis (see {@link #isUniform()}).
   *
   * @return the scale
   * @throws IllegalStateException the illegal state exception
   */
  public double[] getScale() {
    if (isUniformFlag) {
      if (scale == null) {
        // We know that the values scale is uniform. Compute the scale using the
        // range of values
        scale = new double[3];
        scale[0] = getScale(xval);
        scale[1] = getScale(yval);
        scale[2] = getScale(zval);
      }
      return scale.clone();
    }
    throw new IllegalStateException("The function is not uniform");
  }

  private static double getScale(double[] xval) {
    final int n = xval.length - 1;
    return (xval[n] - xval[0]) / n;
  }

  private static double getMax(double[] xval) {
    return xval[xval.length - 1];
  }

  private static int getMaxSplinePosition(double[] xval) {
    return xval.length - 2;
  }

  /**
   * Gets the max X value for interpolation.
   *
   * @return the max X value
   */
  public double getMaxX() {
    return getMax(xval);
  }

  /**
   * Gets the max X spline position for interpolation.
   *
   * @return the max X spline position
   */
  public int getMaxXSplinePosition() {
    return getMaxSplinePosition(xval);
  }

  /**
   * Gets the min X value for interpolation.
   *
   * @return the min X value
   */
  public double getMinX() {
    return xval[0];
  }

  /**
   * Gets the x spline value for the spline position. Equivalent to the x-value used when
   * constructing the spline
   *
   * @param position the position
   * @return the x spline value
   */
  public double getXSplineValue(int position) {
    return xval[position];
  }

  /**
   * Gets the max Y value for interpolation.
   *
   * @return the max Y value
   */
  public double getMaxY() {
    return getMax(yval);
  }

  /**
   * Gets the max Y spline position for interpolation.
   *
   * @return the max Y spline position
   */
  public int getMaxYSplinePosition() {
    return getMaxSplinePosition(yval);
  }

  /**
   * Gets the min Y value for interpolation.
   *
   * @return the min Y value
   */
  public double getMinY() {
    return yval[0];
  }

  /**
   * Gets the y spline value for the spline position. Equivalent to the y-value used when
   * constructing the spline
   *
   * @param position the position
   * @return the x spline value
   */
  public double getYSplineValue(int position) {
    return yval[position];
  }

  /**
   * Gets the max Z value for interpolation.
   *
   * @return the max Z value
   */
  public double getMaxZ() {
    return getMax(zval);
  }

  /**
   * Gets the max Z spline position for interpolation.
   *
   * @return the max Z spline position
   */
  public int getMaxZSplinePosition() {
    return getMaxSplinePosition(zval);
  }

  /**
   * Gets the min Z value for interpolation.
   *
   * @return the min Z value
   */
  public double getMinZ() {
    return zval[0];
  }

  /**
   * Gets the z spline value for the spline position. Equivalent to the z-value used when
   * constructing the spline
   *
   * @param position the position
   * @return the z spline value
   */
  public double getZSplineValue(int position) {
    return zval[position];
  }

  /**
   * Indicates whether a point is within the interpolation range.
   *
   * @param x First coordinate.
   * @param y Second coordinate.
   * @param z Third coordinate.
   * @return {@code true} if (x, y, z) is a valid point.
   */
  public boolean isValidPoint(double x, double y, double z) {
    return (!(x < xval[0] || x > xval[xval.length - 1] || y < yval[0] || y > yval[yval.length - 1]
        || z < zval[0] || z > zval[zval.length - 1]));
  }

  /**
   * Gets the spline node. This is package scope and used for testing.
   *
   * @param indexX the index for dimension X
   * @param indexY the index for dimension Y
   * @param indexZ the index for dimension Z
   * @return the spline node
   */
  CustomTricubicFunction getSplineNodeReference(int indexX, int indexY, int indexZ) {
    return splines[indexX][indexY][indexZ];
  }

  /**
   * Gets a copy of the spline node.
   *
   * @param indexX the index for dimension X
   * @param indexY the index for dimension Y
   * @param indexZ the index for dimension Z
   * @return the spline node
   */
  public CustomTricubicFunction getSplineNode(int indexX, int indexY, int indexZ) {
    return splines[indexX][indexY][indexZ].copy();
  }

  /**
   * Checks if is single precision.
   *
   * @return true, if is single precision
   */
  public boolean isSinglePrecision() {
    return splines[0][0][0].isSinglePrecision();
  }

  /**
   * Convert to single precision.
   */
  public void toSinglePrecision() {
    if (isSinglePrecision()) {
      return;
    }
    final int maxj = getMaxYSplinePosition() + 1;
    final int maxk = getMaxZSplinePosition() + 1;
    for (int i = getMaxXSplinePosition() + 1; i-- > 0;) {
      for (int j = maxj; j-- > 0;) {
        for (int k = maxk; k-- > 0;) {
          splines[i][j][k] = splines[i][j][k].toSinglePrecision();
        }
      }
    }
  }

  /**
   * Convert to double precision.
   */
  public void toDoublePrecision() {
    if (!isSinglePrecision()) {
      return;
    }
    final int maxj = getMaxYSplinePosition() + 1;
    final int maxk = getMaxZSplinePosition() + 1;
    for (int i = getMaxXSplinePosition() + 1; i-- > 0;) {
      for (int j = maxj; j-- > 0;) {
        for (int k = maxk; k-- > 0;) {
          splines[i][j][k] = splines[i][j][k].toDoublePrecision();
        }
      }
    }
  }

  /**
   * Sample the function.
   *
   * <p>n samples will be taken per node in each dimension. A final sample is taken at the end of
   * the sample range thus the final range for each axis will be the current axis range.
   *
   * <p>The procedure setValue(int,int,int,double) method will be executed in ZYX order.
   *
   * @param n the number of samples per spline node
   * @param procedure the procedure @ If the number of sample is not positive
   */
  public void sample(int n, TrivalueProcedure procedure) {
    sample(n, procedure, null);
  }

  /**
   * Sample the function.
   *
   * <p>n samples will be taken per node in each dimension. A final sample is taken at the end of
   * the sample range thus the final range for each axis will be the current axis range.
   *
   * <p>The procedure setValue(int,int,int,double) method will be executed in ZYX order.
   *
   * @param n the number of samples per spline node
   * @param procedure the procedure
   * @param progress the progress @ If the number of sample is not positive
   */
  public void sample(int n, TrivalueProcedure procedure, TrackProgress progress) {
    sample(n, n, n, procedure, progress);
  }

  /**
   * Sample the function.
   *
   * <p>n samples will be taken per node in each dimension. A final sample is taken at the end of
   * the sample range thus the final range for each axis will be the current axis range.
   *
   * <p>The procedure setValue(int,int,int,double) method will be executed in ZYX order.
   *
   * @param nx the number of samples per spline node in the x dimension
   * @param ny the number of samples per spline node in the y dimension
   * @param nz the number of samples per spline node in the z dimension
   * @param procedure the procedure
   * @param progress the progress @ If the number of sample is not positive
   */
  public void sample(int nx, int ny, int nz, TrivalueProcedure procedure, TrackProgress progress) {
    if (nx < 1 || ny < 1 || nz < 1) {
      throw new IllegalArgumentException("Samples must be positive");
    }

    // We can interpolate all nodes n-times plus a final point at the last node
    final int maxx = (getMaxXSplinePosition() + 1) * nx;
    final int maxy = (getMaxYSplinePosition() + 1) * ny;
    final int maxz = (getMaxZSplinePosition() + 1) * nz;
    if (!procedure.setDimensions(maxx + 1, maxy + 1, maxz + 1)) {
      return;
    }

    final Ticker ticker =
        Ticker.create(progress, (long) (maxx + 1) * (maxy + 1) * (maxz + 1), false);
    ticker.start();

    // Pre-compute interpolation tables
    final CubicSplinePosition[] sx = createCubicSplinePosition(nx);
    final CubicSplinePosition[] sy = createCubicSplinePosition(ny);
    final CubicSplinePosition[] sz = createCubicSplinePosition(nz);
    final int nx1 = nx + 1;
    final int ny1 = ny + 1;
    final int nz1 = nz + 1;

    final double[][] tables = new double[nx1 * ny1 * nz1][];
    for (int z = 0, i = 0; z < nz1; z++) {
      final CubicSplinePosition szz = sz[z];
      for (int y = 0; y < ny1; y++) {
        final CubicSplinePosition syy = sy[y];
        for (int x = 0; x < nx1; x++, i++) {
          tables[i] = CustomTricubicFunction.computePowerTable(sx[x], syy, szz);
        }
      }
    }

    // Write axis values
    // Cache the table and the spline position to use for each interpolation point
    final int[] xt = new int[maxx + 1];
    final int[] xp = new int[maxx + 1];
    for (int x = 0; x <= maxx; x++) {
      int xposition = x / nx;
      int xtable = x % nx;
      if (x == maxx) {
        // Final interpolation point
        xposition--;
        xtable = nx;
      }
      xt[x] = xtable;
      xp[x] = xposition;
      procedure.setX(x, xval[xposition] + xtable * xscale[xposition] / nx);
    }
    final int[] yt = new int[maxy + 1];
    final int[] yp = new int[maxy + 1];
    for (int y = 0; y <= maxy; y++) {
      int yposition = y / ny;
      int ytable = y % ny;
      if (y == maxy) {
        // Final interpolation point
        yposition--;
        ytable = ny;
      }
      yt[y] = ytable;
      yp[y] = yposition;
      procedure.setY(y, yval[yposition] + ytable * yscale[yposition] / ny);
    }
    final int[] zt = new int[maxz + 1];
    final int[] zp = new int[maxz + 1];
    for (int z = 0; z <= maxz; z++) {
      int zposition = z / nz;
      int ztable = z % nz;
      if (z == maxz) {
        // Final interpolation point
        zposition--;
        ztable = nz;
      }
      zt[z] = ztable;
      zp[z] = zposition;
      procedure.setZ(z, zval[zposition] + ztable * zscale[zposition] / nz);
    }

    // Write interpolated values
    for (int z = 0; z <= maxz; z++) {
      final int zposition = zp[z];
      for (int y = 0; y <= maxy; y++) {
        final int yposition = yp[y];
        final int j = nx1 * (yt[y] + ny1 * zt[z]);
        for (int x = 0; x <= maxx; x++) {
          procedure.setValue(x, y, z, value(xp[x], yposition, zposition, tables[j + xt[x]]));
          ticker.tick();
        }
      }
    }

    ticker.stop();
  }

  private static CubicSplinePosition[] createCubicSplinePosition(int n) {
    // Use an extra one to have the final x=1 interpolation point.
    final double step = 1.0 / n;
    final CubicSplinePosition[] s = new CubicSplinePosition[n + 1];
    for (int x = 0; x < n; x++) {
      s[x] = new CubicSplinePosition(x * step);
    }
    // Final interpolation point must be exactly 1
    s[n] = new CubicSplinePosition(1);
    return s;
  }

  /**
   * Class to hold information about the size of an interpolating function.
   */
  public static class Size {
    private final int[] dimensions;

    /**
     * Instantiates a new size.
     *
     * @param maxx the maxx
     * @param maxy the maxy
     * @param maxz the maxz
     */
    Size(int maxx, int maxy, int maxz) {
      if (maxx < 2 || maxy < 2 || maxz < 2) {
        throw new IllegalArgumentException(
            "Interpolating function requires minimum length 2 on each axis");
      }
      this.dimensions = new int[] {maxx, maxy, maxz};
    }

    /**
     * Gets the function points in a given dimension.
     *
     * @param dimension the dimension
     * @return the function points
     */
    public int getFunctionPoints(int dimension) {
      return dimensions[dimension];
    }

    /**
     * Gets the spline points in a given dimension.
     *
     * @param dimension the dimension
     * @return the spline points
     */
    public int getSplinePoints(int dimension) {
      return dimensions[dimension] - 1;
    }

    /**
     * Gets the total function points.
     *
     * @return the total function points
     */
    public long getTotalFunctionPoints() {
      return (long) dimensions[0] * dimensions[1] * dimensions[2];
    }

    /**
     * Gets the total spline points.
     *
     * @return the total spline points
     */
    public long getTotalSplinePoints() {
      return (long) (dimensions[0] - 1) * (dimensions[1] - 1) * (dimensions[2] - 1);
    }

    /**
     * Gets the memory footprint.
     *
     * @param singlePrecision the single precision flag
     * @return the memory footprint
     */
    public long getMemoryFootprint(boolean singlePrecision) {
      // Each table is 64 long
      long total = getTotalSplinePoints() * 64;
      // Convert spline point tables to their correct size
      total *= ((singlePrecision) ? Float.BYTES : Double.BYTES);
      // Add the size of each axis and scale in double precision
      for (int i = 0; i < 3; i++) {
        total += 8 * ((long) dimensions[i] + dimensions[i] - 1);
      }
      return total;
    }

    /**
     * Enlarge by an integer factor.
     *
     * @param n the factor
     * @return the size
     */
    public Size enlarge(int n) {
      final int maxx = 1 + getSplinePoints(0) * n;
      final int maxy = 1 + getSplinePoints(1) * n;
      final int maxz = 1 + getSplinePoints(2) * n;
      return new Size(maxx, maxy, maxz);
    }
  }

  /**
   * Estimate the size of an interpolating function.
   *
   * @param maxx the maxx
   * @param maxy the maxy
   * @param maxz the maxz
   * @return the size
   */
  public static Size estimateSize(int maxx, int maxy, int maxz) {
    return new Size(maxx, maxy, maxz);
  }

  /**
   * Estimate the size of an interpolating function.
   *
   * @param dimensions the dimensions
   * @return the size
   */
  public static Size estimateSize(int[] dimensions) {
    if (dimensions == null || dimensions.length != 3) {
      throw new IllegalArgumentException("Dimension must be length 3");
    }
    return new Size(dimensions[0], dimensions[1], dimensions[2]);
  }

  /**
   * Gets the coefficients for the given spline node.
   *
   * @param xindex the x spline position
   * @param yindex the y spline position
   * @param zindex the z spline position
   * @return the coefficients
   */
  public double[] getCoefficients(int xindex, int yindex, int zindex) {
    return splines[xindex][yindex][zindex].getCoefficients().clone();
  }

  //@formatter:off

  /**
   * Compute the spline coefficients from the list of function values and
   * function partial derivatives values at the four corners of a grid
   * element. They must be specified in the following order:
   * <ul>
   *  <li>f(0,0,0)</li>
   *  <li>f(1,0,0)</li>
   *  <li>f(0,1,0)</li>
   *  <li>f(1,1,0)</li>
   *  <li>f(0,0,1)</li>
   *  <li>f(1,0,1)</li>
   *  <li>f(0,1,1)</li>
   *  <li>f(1,1,1)</li>
   *
   *  <li>f<sub>x</sub>(0,0,0)</li>
   *  <li>... <em>(same order as above)</em></li>
   *  <li>f<sub>x</sub>(1,1,1)</li>
   *
   *  <li>f<sub>y</sub>(0,0,0)</li>
   *  <li>... <em>(same order as above)</em></li>
   *  <li>f<sub>y</sub>(1,1,1)</li>
   *
   *  <li>f<sub>z</sub>(0,0,0)</li>
   *  <li>... <em>(same order as above)</em></li>
   *  <li>f<sub>z</sub>(1,1,1)</li>
   *
   *  <li>f<sub>xy</sub>(0,0,0)</li>
   *  <li>... <em>(same order as above)</em></li>
   *  <li>f<sub>xy</sub>(1,1,1)</li>
   *
   *  <li>f<sub>xz</sub>(0,0,0)</li>
   *  <li>... <em>(same order as above)</em></li>
   *  <li>f<sub>xz</sub>(1,1,1)</li>
   *
   *  <li>f<sub>yz</sub>(0,0,0)</li>
   *  <li>... <em>(same order as above)</em></li>
   *  <li>f<sub>yz</sub>(1,1,1)</li>
   *
   *  <li>f<sub>xyz</sub>(0,0,0)</li>
   *  <li>... <em>(same order as above)</em></li>
   *  <li>f<sub>xyz</sub>(1,1,1)</li>
   * </ul>
   * where the subscripts indicate the partial derivative with respect to
   * the corresponding variable(s).
   *
   * @param beta List of function values and function partial derivatives values.
   * @return the spline coefficients.
   */
  @VisibleForTesting
  static double[] computeCoefficients(double[] beta) {
    final double[] a = new double[COEFFICIENT_SIZE];

    for (int i = 0; i < COEFFICIENT_SIZE; i++) {
      double result = 0;
      final double[] row = AINV[i];
      for (int j = 0; j < COEFFICIENT_SIZE; j++) {
        result += row[j] * beta[j];
      }
      a[i] = result;
    }

    return a;
  }

  /**
   * Compute coefficients inline. This has been created using the same code as
   * {@link #computeCoefficients(double[])} but ignoring any entry in AINV that is zero.
   *
   * @param beta List of function values and function partial derivatives values.
   * @return the spline coefficients.
   */
  @VisibleForTesting
  static double[] computeCoefficientsInline(double[] beta) {
    final double[] a = new double[64];
    a[0]=beta[0];
    a[1]=beta[8];
    a[2]=-3*beta[0]+3*beta[1]-2*beta[8]-beta[9];
    a[3]=2*beta[0]-2*beta[1]+beta[8]+beta[9];
    a[4]=beta[16];
    a[5]=beta[32];
    a[6]=-3*beta[16]+3*beta[17]-2*beta[32]-beta[33];
    a[7]=2*beta[16]-2*beta[17]+beta[32]+beta[33];
    a[8]=-3*beta[0]+3*beta[2]-2*beta[16]-beta[18];
    a[9]=-3*beta[8]+3*beta[10]-2*beta[32]-beta[34];
    a[10]=9*beta[0]-9*beta[1]-9*beta[2]+9*beta[3]+6*beta[8]+3*beta[9]-6*beta[10]-3*beta[11]+6*beta[16]-6*beta[17]+3*beta[18]-3*beta[19]+4*beta[32]+2*beta[33]+2*beta[34]+beta[35];
    a[11]=-6*beta[0]+6*beta[1]+6*beta[2]-6*beta[3]-3*beta[8]-3*beta[9]+3*beta[10]+3*beta[11]-4*beta[16]+4*beta[17]-2*beta[18]+2*beta[19]-2*beta[32]-2*beta[33]-beta[34]-beta[35];
    a[12]=2*beta[0]-2*beta[2]+beta[16]+beta[18];
    a[13]=2*beta[8]-2*beta[10]+beta[32]+beta[34];
    a[14]=-6*beta[0]+6*beta[1]+6*beta[2]-6*beta[3]-4*beta[8]-2*beta[9]+4*beta[10]+2*beta[11]-3*beta[16]+3*beta[17]-3*beta[18]+3*beta[19]-2*beta[32]-beta[33]-2*beta[34]-beta[35];
    a[15]=4*beta[0]-4*beta[1]-4*beta[2]+4*beta[3]+2*beta[8]+2*beta[9]-2*beta[10]-2*beta[11]+2*beta[16]-2*beta[17]+2*beta[18]-2*beta[19]+beta[32]+beta[33]+beta[34]+beta[35];
    a[16]=beta[24];
    a[17]=beta[40];
    a[18]=-3*beta[24]+3*beta[25]-2*beta[40]-beta[41];
    a[19]=2*beta[24]-2*beta[25]+beta[40]+beta[41];
    a[20]=beta[48];
    a[21]=beta[56];
    a[22]=-3*beta[48]+3*beta[49]-2*beta[56]-beta[57];
    a[23]=2*beta[48]-2*beta[49]+beta[56]+beta[57];
    a[24]=-3*beta[24]+3*beta[26]-2*beta[48]-beta[50];
    a[25]=-3*beta[40]+3*beta[42]-2*beta[56]-beta[58];
    a[26]=9*beta[24]-9*beta[25]-9*beta[26]+9*beta[27]+6*beta[40]+3*beta[41]-6*beta[42]-3*beta[43]+6*beta[48]-6*beta[49]+3*beta[50]-3*beta[51]+4*beta[56]+2*beta[57]+2*beta[58]+beta[59];
    a[27]=-6*beta[24]+6*beta[25]+6*beta[26]-6*beta[27]-3*beta[40]-3*beta[41]+3*beta[42]+3*beta[43]-4*beta[48]+4*beta[49]-2*beta[50]+2*beta[51]-2*beta[56]-2*beta[57]-beta[58]-beta[59];
    a[28]=2*beta[24]-2*beta[26]+beta[48]+beta[50];
    a[29]=2*beta[40]-2*beta[42]+beta[56]+beta[58];
    a[30]=-6*beta[24]+6*beta[25]+6*beta[26]-6*beta[27]-4*beta[40]-2*beta[41]+4*beta[42]+2*beta[43]-3*beta[48]+3*beta[49]-3*beta[50]+3*beta[51]-2*beta[56]-beta[57]-2*beta[58]-beta[59];
    a[31]=4*beta[24]-4*beta[25]-4*beta[26]+4*beta[27]+2*beta[40]+2*beta[41]-2*beta[42]-2*beta[43]+2*beta[48]-2*beta[49]+2*beta[50]-2*beta[51]+beta[56]+beta[57]+beta[58]+beta[59];
    a[32]=-3*beta[0]+3*beta[4]-2*beta[24]-beta[28];
    a[33]=-3*beta[8]+3*beta[12]-2*beta[40]-beta[44];
    a[34]=9*beta[0]-9*beta[1]-9*beta[4]+9*beta[5]+6*beta[8]+3*beta[9]-6*beta[12]-3*beta[13]+6*beta[24]-6*beta[25]+3*beta[28]-3*beta[29]+4*beta[40]+2*beta[41]+2*beta[44]+beta[45];
    a[35]=-6*beta[0]+6*beta[1]+6*beta[4]-6*beta[5]-3*beta[8]-3*beta[9]+3*beta[12]+3*beta[13]-4*beta[24]+4*beta[25]-2*beta[28]+2*beta[29]-2*beta[40]-2*beta[41]-beta[44]-beta[45];
    a[36]=-3*beta[16]+3*beta[20]-2*beta[48]-beta[52];
    a[37]=-3*beta[32]+3*beta[36]-2*beta[56]-beta[60];
    a[38]=9*beta[16]-9*beta[17]-9*beta[20]+9*beta[21]+6*beta[32]+3*beta[33]-6*beta[36]-3*beta[37]+6*beta[48]-6*beta[49]+3*beta[52]-3*beta[53]+4*beta[56]+2*beta[57]+2*beta[60]+beta[61];
    a[39]=-6*beta[16]+6*beta[17]+6*beta[20]-6*beta[21]-3*beta[32]-3*beta[33]+3*beta[36]+3*beta[37]-4*beta[48]+4*beta[49]-2*beta[52]+2*beta[53]-2*beta[56]-2*beta[57]-beta[60]-beta[61];
    a[40]=9*beta[0]-9*beta[2]-9*beta[4]+9*beta[6]+6*beta[16]+3*beta[18]-6*beta[20]-3*beta[22]+6*beta[24]-6*beta[26]+3*beta[28]-3*beta[30]+4*beta[48]+2*beta[50]+2*beta[52]+beta[54];
    a[41]=9*beta[8]-9*beta[10]-9*beta[12]+9*beta[14]+6*beta[32]+3*beta[34]-6*beta[36]-3*beta[38]+6*beta[40]-6*beta[42]+3*beta[44]-3*beta[46]+4*beta[56]+2*beta[58]+2*beta[60]+beta[62];
    a[42]=-27*beta[0]+27*beta[1]+27*beta[2]-27*beta[3]+27*beta[4]-27*beta[5]-27*beta[6]+27*beta[7]-18*beta[8]-9*beta[9]+18*beta[10]+9*beta[11]+18*beta[12]+9*beta[13]-18*beta[14]-9*beta[15]-18*beta[16]+18*beta[17]-9*beta[18]+9*beta[19]+18*beta[20]-18*beta[21]+9*beta[22]-9*beta[23]-18*beta[24]+18*beta[25]+18*beta[26]-18*beta[27]-9*beta[28]+9*beta[29]+9*beta[30]-9*beta[31]-12*beta[32]-6*beta[33]-6*beta[34]-3*beta[35]+12*beta[36]+6*beta[37]+6*beta[38]+3*beta[39]-12*beta[40]-6*beta[41]+12*beta[42]+6*beta[43]-6*beta[44]-3*beta[45]+6*beta[46]+3*beta[47]-12*beta[48]+12*beta[49]-6*beta[50]+6*beta[51]-6*beta[52]+6*beta[53]-3*beta[54]+3*beta[55]-8*beta[56]-4*beta[57]-4*beta[58]-2*beta[59]-4*beta[60]-2*beta[61]-2*beta[62]-beta[63];
    a[43]=18*beta[0]-18*beta[1]-18*beta[2]+18*beta[3]-18*beta[4]+18*beta[5]+18*beta[6]-18*beta[7]+9*beta[8]+9*beta[9]-9*beta[10]-9*beta[11]-9*beta[12]-9*beta[13]+9*beta[14]+9*beta[15]+12*beta[16]-12*beta[17]+6*beta[18]-6*beta[19]-12*beta[20]+12*beta[21]-6*beta[22]+6*beta[23]+12*beta[24]-12*beta[25]-12*beta[26]+12*beta[27]+6*beta[28]-6*beta[29]-6*beta[30]+6*beta[31]+6*beta[32]+6*beta[33]+3*beta[34]+3*beta[35]-6*beta[36]-6*beta[37]-3*beta[38]-3*beta[39]+6*beta[40]+6*beta[41]-6*beta[42]-6*beta[43]+3*beta[44]+3*beta[45]-3*beta[46]-3*beta[47]+8*beta[48]-8*beta[49]+4*beta[50]-4*beta[51]+4*beta[52]-4*beta[53]+2*beta[54]-2*beta[55]+4*beta[56]+4*beta[57]+2*beta[58]+2*beta[59]+2*beta[60]+2*beta[61]+beta[62]+beta[63];
    a[44]=-6*beta[0]+6*beta[2]+6*beta[4]-6*beta[6]-3*beta[16]-3*beta[18]+3*beta[20]+3*beta[22]-4*beta[24]+4*beta[26]-2*beta[28]+2*beta[30]-2*beta[48]-2*beta[50]-beta[52]-beta[54];
    a[45]=-6*beta[8]+6*beta[10]+6*beta[12]-6*beta[14]-3*beta[32]-3*beta[34]+3*beta[36]+3*beta[38]-4*beta[40]+4*beta[42]-2*beta[44]+2*beta[46]-2*beta[56]-2*beta[58]-beta[60]-beta[62];
    a[46]=18*beta[0]-18*beta[1]-18*beta[2]+18*beta[3]-18*beta[4]+18*beta[5]+18*beta[6]-18*beta[7]+12*beta[8]+6*beta[9]-12*beta[10]-6*beta[11]-12*beta[12]-6*beta[13]+12*beta[14]+6*beta[15]+9*beta[16]-9*beta[17]+9*beta[18]-9*beta[19]-9*beta[20]+9*beta[21]-9*beta[22]+9*beta[23]+12*beta[24]-12*beta[25]-12*beta[26]+12*beta[27]+6*beta[28]-6*beta[29]-6*beta[30]+6*beta[31]+6*beta[32]+3*beta[33]+6*beta[34]+3*beta[35]-6*beta[36]-3*beta[37]-6*beta[38]-3*beta[39]+8*beta[40]+4*beta[41]-8*beta[42]-4*beta[43]+4*beta[44]+2*beta[45]-4*beta[46]-2*beta[47]+6*beta[48]-6*beta[49]+6*beta[50]-6*beta[51]+3*beta[52]-3*beta[53]+3*beta[54]-3*beta[55]+4*beta[56]+2*beta[57]+4*beta[58]+2*beta[59]+2*beta[60]+beta[61]+2*beta[62]+beta[63];
    a[47]=-12*beta[0]+12*beta[1]+12*beta[2]-12*beta[3]+12*beta[4]-12*beta[5]-12*beta[6]+12*beta[7]-6*beta[8]-6*beta[9]+6*beta[10]+6*beta[11]+6*beta[12]+6*beta[13]-6*beta[14]-6*beta[15]-6*beta[16]+6*beta[17]-6*beta[18]+6*beta[19]+6*beta[20]-6*beta[21]+6*beta[22]-6*beta[23]-8*beta[24]+8*beta[25]+8*beta[26]-8*beta[27]-4*beta[28]+4*beta[29]+4*beta[30]-4*beta[31]-3*beta[32]-3*beta[33]-3*beta[34]-3*beta[35]+3*beta[36]+3*beta[37]+3*beta[38]+3*beta[39]-4*beta[40]-4*beta[41]+4*beta[42]+4*beta[43]-2*beta[44]-2*beta[45]+2*beta[46]+2*beta[47]-4*beta[48]+4*beta[49]-4*beta[50]+4*beta[51]-2*beta[52]+2*beta[53]-2*beta[54]+2*beta[55]-2*beta[56]-2*beta[57]-2*beta[58]-2*beta[59]-beta[60]-beta[61]-beta[62]-beta[63];
    a[48]=2*beta[0]-2*beta[4]+beta[24]+beta[28];
    a[49]=2*beta[8]-2*beta[12]+beta[40]+beta[44];
    a[50]=-6*beta[0]+6*beta[1]+6*beta[4]-6*beta[5]-4*beta[8]-2*beta[9]+4*beta[12]+2*beta[13]-3*beta[24]+3*beta[25]-3*beta[28]+3*beta[29]-2*beta[40]-beta[41]-2*beta[44]-beta[45];
    a[51]=4*beta[0]-4*beta[1]-4*beta[4]+4*beta[5]+2*beta[8]+2*beta[9]-2*beta[12]-2*beta[13]+2*beta[24]-2*beta[25]+2*beta[28]-2*beta[29]+beta[40]+beta[41]+beta[44]+beta[45];
    a[52]=2*beta[16]-2*beta[20]+beta[48]+beta[52];
    a[53]=2*beta[32]-2*beta[36]+beta[56]+beta[60];
    a[54]=-6*beta[16]+6*beta[17]+6*beta[20]-6*beta[21]-4*beta[32]-2*beta[33]+4*beta[36]+2*beta[37]-3*beta[48]+3*beta[49]-3*beta[52]+3*beta[53]-2*beta[56]-beta[57]-2*beta[60]-beta[61];
    a[55]=4*beta[16]-4*beta[17]-4*beta[20]+4*beta[21]+2*beta[32]+2*beta[33]-2*beta[36]-2*beta[37]+2*beta[48]-2*beta[49]+2*beta[52]-2*beta[53]+beta[56]+beta[57]+beta[60]+beta[61];
    a[56]=-6*beta[0]+6*beta[2]+6*beta[4]-6*beta[6]-4*beta[16]-2*beta[18]+4*beta[20]+2*beta[22]-3*beta[24]+3*beta[26]-3*beta[28]+3*beta[30]-2*beta[48]-beta[50]-2*beta[52]-beta[54];
    a[57]=-6*beta[8]+6*beta[10]+6*beta[12]-6*beta[14]-4*beta[32]-2*beta[34]+4*beta[36]+2*beta[38]-3*beta[40]+3*beta[42]-3*beta[44]+3*beta[46]-2*beta[56]-beta[58]-2*beta[60]-beta[62];
    a[58]=18*beta[0]-18*beta[1]-18*beta[2]+18*beta[3]-18*beta[4]+18*beta[5]+18*beta[6]-18*beta[7]+12*beta[8]+6*beta[9]-12*beta[10]-6*beta[11]-12*beta[12]-6*beta[13]+12*beta[14]+6*beta[15]+12*beta[16]-12*beta[17]+6*beta[18]-6*beta[19]-12*beta[20]+12*beta[21]-6*beta[22]+6*beta[23]+9*beta[24]-9*beta[25]-9*beta[26]+9*beta[27]+9*beta[28]-9*beta[29]-9*beta[30]+9*beta[31]+8*beta[32]+4*beta[33]+4*beta[34]+2*beta[35]-8*beta[36]-4*beta[37]-4*beta[38]-2*beta[39]+6*beta[40]+3*beta[41]-6*beta[42]-3*beta[43]+6*beta[44]+3*beta[45]-6*beta[46]-3*beta[47]+6*beta[48]-6*beta[49]+3*beta[50]-3*beta[51]+6*beta[52]-6*beta[53]+3*beta[54]-3*beta[55]+4*beta[56]+2*beta[57]+2*beta[58]+beta[59]+4*beta[60]+2*beta[61]+2*beta[62]+beta[63];
    a[59]=-12*beta[0]+12*beta[1]+12*beta[2]-12*beta[3]+12*beta[4]-12*beta[5]-12*beta[6]+12*beta[7]-6*beta[8]-6*beta[9]+6*beta[10]+6*beta[11]+6*beta[12]+6*beta[13]-6*beta[14]-6*beta[15]-8*beta[16]+8*beta[17]-4*beta[18]+4*beta[19]+8*beta[20]-8*beta[21]+4*beta[22]-4*beta[23]-6*beta[24]+6*beta[25]+6*beta[26]-6*beta[27]-6*beta[28]+6*beta[29]+6*beta[30]-6*beta[31]-4*beta[32]-4*beta[33]-2*beta[34]-2*beta[35]+4*beta[36]+4*beta[37]+2*beta[38]+2*beta[39]-3*beta[40]-3*beta[41]+3*beta[42]+3*beta[43]-3*beta[44]-3*beta[45]+3*beta[46]+3*beta[47]-4*beta[48]+4*beta[49]-2*beta[50]+2*beta[51]-4*beta[52]+4*beta[53]-2*beta[54]+2*beta[55]-2*beta[56]-2*beta[57]-beta[58]-beta[59]-2*beta[60]-2*beta[61]-beta[62]-beta[63];
    a[60]=4*beta[0]-4*beta[2]-4*beta[4]+4*beta[6]+2*beta[16]+2*beta[18]-2*beta[20]-2*beta[22]+2*beta[24]-2*beta[26]+2*beta[28]-2*beta[30]+beta[48]+beta[50]+beta[52]+beta[54];
    a[61]=4*beta[8]-4*beta[10]-4*beta[12]+4*beta[14]+2*beta[32]+2*beta[34]-2*beta[36]-2*beta[38]+2*beta[40]-2*beta[42]+2*beta[44]-2*beta[46]+beta[56]+beta[58]+beta[60]+beta[62];
    a[62]=-12*beta[0]+12*beta[1]+12*beta[2]-12*beta[3]+12*beta[4]-12*beta[5]-12*beta[6]+12*beta[7]-8*beta[8]-4*beta[9]+8*beta[10]+4*beta[11]+8*beta[12]+4*beta[13]-8*beta[14]-4*beta[15]-6*beta[16]+6*beta[17]-6*beta[18]+6*beta[19]+6*beta[20]-6*beta[21]+6*beta[22]-6*beta[23]-6*beta[24]+6*beta[25]+6*beta[26]-6*beta[27]-6*beta[28]+6*beta[29]+6*beta[30]-6*beta[31]-4*beta[32]-2*beta[33]-4*beta[34]-2*beta[35]+4*beta[36]+2*beta[37]+4*beta[38]+2*beta[39]-4*beta[40]-2*beta[41]+4*beta[42]+2*beta[43]-4*beta[44]-2*beta[45]+4*beta[46]+2*beta[47]-3*beta[48]+3*beta[49]-3*beta[50]+3*beta[51]-3*beta[52]+3*beta[53]-3*beta[54]+3*beta[55]-2*beta[56]-beta[57]-2*beta[58]-beta[59]-2*beta[60]-beta[61]-2*beta[62]-beta[63];
    a[63]=8*beta[0]-8*beta[1]-8*beta[2]+8*beta[3]-8*beta[4]+8*beta[5]+8*beta[6]-8*beta[7]+4*beta[8]+4*beta[9]-4*beta[10]-4*beta[11]-4*beta[12]-4*beta[13]+4*beta[14]+4*beta[15]+4*beta[16]-4*beta[17]+4*beta[18]-4*beta[19]-4*beta[20]+4*beta[21]-4*beta[22]+4*beta[23]+4*beta[24]-4*beta[25]-4*beta[26]+4*beta[27]+4*beta[28]-4*beta[29]-4*beta[30]+4*beta[31]+2*beta[32]+2*beta[33]+2*beta[34]+2*beta[35]-2*beta[36]-2*beta[37]-2*beta[38]-2*beta[39]+2*beta[40]+2*beta[41]-2*beta[42]-2*beta[43]+2*beta[44]+2*beta[45]-2*beta[46]-2*beta[47]+2*beta[48]-2*beta[49]+2*beta[50]-2*beta[51]+2*beta[52]-2*beta[53]+2*beta[54]-2*beta[55]+beta[56]+beta[57]+beta[58]+beta[59]+beta[60]+beta[61]+beta[62]+beta[63];
    return a;
  }

  /**
   * Compute coefficients inline. This has been created using the same code as
   * {@link #computeCoefficients(double[])} but ignoring any entry in AINV that is zero.
   * Terms have then been collected to remove multiplications.
   *
   * @param beta List of function values and function partial derivatives values.
   * @return the spline coefficients.
   */
  @VisibleForTesting
  static double[] computeCoefficientsInlineCollectTerms(double[] beta) {
    final double[] a = new double[64];
    a[0]=beta[0];
    a[1]=beta[8];
    a[2]=-3*(beta[0]-beta[1])-2*beta[8]-beta[9];
    a[3]=2*(beta[0]-beta[1])+(beta[8]+beta[9]);
    a[4]=beta[16];
    a[5]=beta[32];
    a[6]=-3*(beta[16]-beta[17])-2*beta[32]-beta[33];
    a[7]=2*(beta[16]-beta[17])+(beta[32]+beta[33]);
    a[8]=-3*(beta[0]-beta[2])-2*beta[16]-beta[18];
    a[9]=-3*(beta[8]-beta[10])-2*beta[32]-beta[34];
    a[10]=9*(beta[0]-beta[1]-beta[2]+beta[3])+6*(beta[8]-beta[10]+beta[16]-beta[17])+4*beta[32]+3*(beta[9]-beta[11]+beta[18]-beta[19])+2*(beta[33]+beta[34])+beta[35];
    a[11]=-6*(beta[0]-beta[1]-beta[2]+beta[3])-4*(beta[16]-beta[17])-3*(beta[8]+beta[9]-beta[10]-beta[11])-2*(beta[18]-beta[19]+beta[32]+beta[33])-(beta[34]+beta[35]);
    a[12]=2*(beta[0]-beta[2])+(beta[16]+beta[18]);
    a[13]=2*(beta[8]-beta[10])+(beta[32]+beta[34]);
    a[14]=-6*(beta[0]-beta[1]-beta[2]+beta[3])-4*(beta[8]-beta[10])-3*(beta[16]-beta[17]+beta[18]-beta[19])-2*(beta[9]-beta[11]+beta[32]+beta[34])-(beta[33]+beta[35]);
    a[15]=4*(beta[0]-beta[1]-beta[2]+beta[3])+2*(beta[8]+beta[9]-beta[10]-beta[11]+beta[16]-beta[17]+beta[18]-beta[19])+(beta[32]+beta[33]+beta[34]+beta[35]);
    a[16]=beta[24];
    a[17]=beta[40];
    a[18]=-3*(beta[24]-beta[25])-2*beta[40]-beta[41];
    a[19]=2*(beta[24]-beta[25])+(beta[40]+beta[41]);
    a[20]=beta[48];
    a[21]=beta[56];
    a[22]=-3*(beta[48]-beta[49])-2*beta[56]-beta[57];
    a[23]=2*(beta[48]-beta[49])+(beta[56]+beta[57]);
    a[24]=-3*(beta[24]-beta[26])-2*beta[48]-beta[50];
    a[25]=-3*(beta[40]-beta[42])-2*beta[56]-beta[58];
    a[26]=9*(beta[24]-beta[25]-beta[26]+beta[27])+6*(beta[40]-beta[42]+beta[48]-beta[49])+4*beta[56]+3*(beta[41]-beta[43]+beta[50]-beta[51])+2*(beta[57]+beta[58])+beta[59];
    a[27]=-6*(beta[24]-beta[25]-beta[26]+beta[27])-4*(beta[48]-beta[49])-3*(beta[40]+beta[41]-beta[42]-beta[43])-2*(beta[50]-beta[51]+beta[56]+beta[57])-(beta[58]+beta[59]);
    a[28]=2*(beta[24]-beta[26])+(beta[48]+beta[50]);
    a[29]=2*(beta[40]-beta[42])+(beta[56]+beta[58]);
    a[30]=-6*(beta[24]-beta[25]-beta[26]+beta[27])-4*(beta[40]-beta[42])-3*(beta[48]-beta[49]+beta[50]-beta[51])-2*(beta[41]-beta[43]+beta[56]+beta[58])-(beta[57]+beta[59]);
    a[31]=4*(beta[24]-beta[25]-beta[26]+beta[27])+2*(beta[40]+beta[41]-beta[42]-beta[43]+beta[48]-beta[49]+beta[50]-beta[51])+(beta[56]+beta[57]+beta[58]+beta[59]);
    a[32]=-3*(beta[0]-beta[4])-2*beta[24]-beta[28];
    a[33]=-3*(beta[8]-beta[12])-2*beta[40]-beta[44];
    a[34]=9*(beta[0]-beta[1]-beta[4]+beta[5])+6*(beta[8]-beta[12]+beta[24]-beta[25])+4*beta[40]+3*(beta[9]-beta[13]+beta[28]-beta[29])+2*(beta[41]+beta[44])+beta[45];
    a[35]=-6*(beta[0]-beta[1]-beta[4]+beta[5])-4*(beta[24]-beta[25])-3*(beta[8]+beta[9]-beta[12]-beta[13])-2*(beta[28]-beta[29]+beta[40]+beta[41])-(beta[44]+beta[45]);
    a[36]=-3*(beta[16]-beta[20])-2*beta[48]-beta[52];
    a[37]=-3*(beta[32]-beta[36])-2*beta[56]-beta[60];
    a[38]=9*(beta[16]-beta[17]-beta[20]+beta[21])+6*(beta[32]-beta[36]+beta[48]-beta[49])+4*beta[56]+3*(beta[33]-beta[37]+beta[52]-beta[53])+2*(beta[57]+beta[60])+beta[61];
    a[39]=-6*(beta[16]-beta[17]-beta[20]+beta[21])-4*(beta[48]-beta[49])-3*(beta[32]+beta[33]-beta[36]-beta[37])-2*(beta[52]-beta[53]+beta[56]+beta[57])-(beta[60]+beta[61]);
    a[40]=9*(beta[0]-beta[2]-beta[4]+beta[6])+6*(beta[16]-beta[20]+beta[24]-beta[26])+4*beta[48]+3*(beta[18]-beta[22]+beta[28]-beta[30])+2*(beta[50]+beta[52])+beta[54];
    a[41]=9*(beta[8]-beta[10]-beta[12]+beta[14])+6*(beta[32]-beta[36]+beta[40]-beta[42])+4*beta[56]+3*(beta[34]-beta[38]+beta[44]-beta[46])+2*(beta[58]+beta[60])+beta[62];
    a[42]=-27*(beta[0]-beta[1]-beta[2]+beta[3]-beta[4]+beta[5]+beta[6]-beta[7])-18*(beta[8]-beta[10]-beta[12]+beta[14]+beta[16]-beta[17]-beta[20]+beta[21]+beta[24]-beta[25]-beta[26]+beta[27])-12*(beta[32]-beta[36]+beta[40]-beta[42]+beta[48]-beta[49])-9*(beta[9]-beta[11]-beta[13]+beta[15]+beta[18]-beta[19]-beta[22]+beta[23]+beta[28]-beta[29]-beta[30]+beta[31])-8*beta[56]-6*(beta[33]+beta[34]-beta[37]-beta[38]+beta[41]-beta[43]+beta[44]-beta[46]+beta[50]-beta[51]+beta[52]-beta[53])-4*(beta[57]+beta[58]+beta[60])-3*(beta[35]-beta[39]+beta[45]-beta[47]+beta[54]-beta[55])-2*(beta[59]+beta[61]+beta[62])-beta[63];
    a[43]=18*(beta[0]-beta[1]-beta[2]+beta[3]-beta[4]+beta[5]+beta[6]-beta[7])+12*(beta[16]-beta[17]-beta[20]+beta[21]+beta[24]-beta[25]-beta[26]+beta[27])+9*(beta[8]+beta[9]-beta[10]-beta[11]-beta[12]-beta[13]+beta[14]+beta[15])+8*(beta[48]-beta[49])+6*(beta[18]-beta[19]-beta[22]+beta[23]+beta[28]-beta[29]-beta[30]+beta[31]+beta[32]+beta[33]-beta[36]-beta[37]+beta[40]+beta[41]-beta[42]-beta[43])+4*(beta[50]-beta[51]+beta[52]-beta[53]+beta[56]+beta[57])+3*(beta[34]+beta[35]-beta[38]-beta[39]+beta[44]+beta[45]-beta[46]-beta[47])+2*(beta[54]-beta[55]+beta[58]+beta[59]+beta[60]+beta[61])+(beta[62]+beta[63]);
    a[44]=-6*(beta[0]-beta[2]-beta[4]+beta[6])-4*(beta[24]-beta[26])-3*(beta[16]+beta[18]-beta[20]-beta[22])-2*(beta[28]-beta[30]+beta[48]+beta[50])-(beta[52]+beta[54]);
    a[45]=-6*(beta[8]-beta[10]-beta[12]+beta[14])-4*(beta[40]-beta[42])-3*(beta[32]+beta[34]-beta[36]-beta[38])-2*(beta[44]-beta[46]+beta[56]+beta[58])-(beta[60]+beta[62]);
    a[46]=18*(beta[0]-beta[1]-beta[2]+beta[3]-beta[4]+beta[5]+beta[6]-beta[7])+12*(beta[8]-beta[10]-beta[12]+beta[14]+beta[24]-beta[25]-beta[26]+beta[27])+9*(beta[16]-beta[17]+beta[18]-beta[19]-beta[20]+beta[21]-beta[22]+beta[23])+8*(beta[40]-beta[42])+6*(beta[9]-beta[11]-beta[13]+beta[15]+beta[28]-beta[29]-beta[30]+beta[31]+beta[32]+beta[34]-beta[36]-beta[38]+beta[48]-beta[49]+beta[50]-beta[51])+4*(beta[41]-beta[43]+beta[44]-beta[46]+beta[56]+beta[58])+3*(beta[33]+beta[35]-beta[37]-beta[39]+beta[52]-beta[53]+beta[54]-beta[55])+2*(beta[45]-beta[47]+beta[57]+beta[59]+beta[60]+beta[62])+(beta[61]+beta[63]);
    a[47]=-12*(beta[0]-beta[1]-beta[2]+beta[3]-beta[4]+beta[5]+beta[6]-beta[7])-8*(beta[24]-beta[25]-beta[26]+beta[27])-6*(beta[8]+beta[9]-beta[10]-beta[11]-beta[12]-beta[13]+beta[14]+beta[15]+beta[16]-beta[17]+beta[18]-beta[19]-beta[20]+beta[21]-beta[22]+beta[23])-4*(beta[28]-beta[29]-beta[30]+beta[31]+beta[40]+beta[41]-beta[42]-beta[43]+beta[48]-beta[49]+beta[50]-beta[51])-3*(beta[32]+beta[33]+beta[34]+beta[35]-beta[36]-beta[37]-beta[38]-beta[39])-2*(beta[44]+beta[45]-beta[46]-beta[47]+beta[52]-beta[53]+beta[54]-beta[55]+beta[56]+beta[57]+beta[58]+beta[59])-(beta[60]+beta[61]+beta[62]+beta[63]);
    a[48]=2*(beta[0]-beta[4])+(beta[24]+beta[28]);
    a[49]=2*(beta[8]-beta[12])+(beta[40]+beta[44]);
    a[50]=-6*(beta[0]-beta[1]-beta[4]+beta[5])-4*(beta[8]-beta[12])-3*(beta[24]-beta[25]+beta[28]-beta[29])-2*(beta[9]-beta[13]+beta[40]+beta[44])-(beta[41]+beta[45]);
    a[51]=4*(beta[0]-beta[1]-beta[4]+beta[5])+2*(beta[8]+beta[9]-beta[12]-beta[13]+beta[24]-beta[25]+beta[28]-beta[29])+(beta[40]+beta[41]+beta[44]+beta[45]);
    a[52]=2*(beta[16]-beta[20])+(beta[48]+beta[52]);
    a[53]=2*(beta[32]-beta[36])+(beta[56]+beta[60]);
    a[54]=-6*(beta[16]-beta[17]-beta[20]+beta[21])-4*(beta[32]-beta[36])-3*(beta[48]-beta[49]+beta[52]-beta[53])-2*(beta[33]-beta[37]+beta[56]+beta[60])-(beta[57]+beta[61]);
    a[55]=4*(beta[16]-beta[17]-beta[20]+beta[21])+2*(beta[32]+beta[33]-beta[36]-beta[37]+beta[48]-beta[49]+beta[52]-beta[53])+(beta[56]+beta[57]+beta[60]+beta[61]);
    a[56]=-6*(beta[0]-beta[2]-beta[4]+beta[6])-4*(beta[16]-beta[20])-3*(beta[24]-beta[26]+beta[28]-beta[30])-2*(beta[18]-beta[22]+beta[48]+beta[52])-(beta[50]+beta[54]);
    a[57]=-6*(beta[8]-beta[10]-beta[12]+beta[14])-4*(beta[32]-beta[36])-3*(beta[40]-beta[42]+beta[44]-beta[46])-2*(beta[34]-beta[38]+beta[56]+beta[60])-(beta[58]+beta[62]);
    a[58]=18*(beta[0]-beta[1]-beta[2]+beta[3]-beta[4]+beta[5]+beta[6]-beta[7])+12*(beta[8]-beta[10]-beta[12]+beta[14]+beta[16]-beta[17]-beta[20]+beta[21])+9*(beta[24]-beta[25]-beta[26]+beta[27]+beta[28]-beta[29]-beta[30]+beta[31])+8*(beta[32]-beta[36])+6*(beta[9]-beta[11]-beta[13]+beta[15]+beta[18]-beta[19]-beta[22]+beta[23]+beta[40]-beta[42]+beta[44]-beta[46]+beta[48]-beta[49]+beta[52]-beta[53])+4*(beta[33]+beta[34]-beta[37]-beta[38]+beta[56]+beta[60])+3*(beta[41]-beta[43]+beta[45]-beta[47]+beta[50]-beta[51]+beta[54]-beta[55])+2*(beta[35]-beta[39]+beta[57]+beta[58]+beta[61]+beta[62])+(beta[59]+beta[63]);
    a[59]=-12*(beta[0]-beta[1]-beta[2]+beta[3]-beta[4]+beta[5]+beta[6]-beta[7])-8*(beta[16]-beta[17]-beta[20]+beta[21])-6*(beta[8]+beta[9]-beta[10]-beta[11]-beta[12]-beta[13]+beta[14]+beta[15]+beta[24]-beta[25]-beta[26]+beta[27]+beta[28]-beta[29]-beta[30]+beta[31])-4*(beta[18]-beta[19]-beta[22]+beta[23]+beta[32]+beta[33]-beta[36]-beta[37]+beta[48]-beta[49]+beta[52]-beta[53])-3*(beta[40]+beta[41]-beta[42]-beta[43]+beta[44]+beta[45]-beta[46]-beta[47])-2*(beta[34]+beta[35]-beta[38]-beta[39]+beta[50]-beta[51]+beta[54]-beta[55]+beta[56]+beta[57]+beta[60]+beta[61])-(beta[58]+beta[59]+beta[62]+beta[63]);
    a[60]=4*(beta[0]-beta[2]-beta[4]+beta[6])+2*(beta[16]+beta[18]-beta[20]-beta[22]+beta[24]-beta[26]+beta[28]-beta[30])+(beta[48]+beta[50]+beta[52]+beta[54]);
    a[61]=4*(beta[8]-beta[10]-beta[12]+beta[14])+2*(beta[32]+beta[34]-beta[36]-beta[38]+beta[40]-beta[42]+beta[44]-beta[46])+(beta[56]+beta[58]+beta[60]+beta[62]);
    a[62]=-12*(beta[0]-beta[1]-beta[2]+beta[3]-beta[4]+beta[5]+beta[6]-beta[7])-8*(beta[8]-beta[10]-beta[12]+beta[14])-6*(beta[16]-beta[17]+beta[18]-beta[19]-beta[20]+beta[21]-beta[22]+beta[23]+beta[24]-beta[25]-beta[26]+beta[27]+beta[28]-beta[29]-beta[30]+beta[31])-4*(beta[9]-beta[11]-beta[13]+beta[15]+beta[32]+beta[34]-beta[36]-beta[38]+beta[40]-beta[42]+beta[44]-beta[46])-3*(beta[48]-beta[49]+beta[50]-beta[51]+beta[52]-beta[53]+beta[54]-beta[55])-2*(beta[33]+beta[35]-beta[37]-beta[39]+beta[41]-beta[43]+beta[45]-beta[47]+beta[56]+beta[58]+beta[60]+beta[62])-(beta[57]+beta[59]+beta[61]+beta[63]);
    a[63]=8*(beta[0]-beta[1]-beta[2]+beta[3]-beta[4]+beta[5]+beta[6]-beta[7])+4*(beta[8]+beta[9]-beta[10]-beta[11]-beta[12]-beta[13]+beta[14]+beta[15]+beta[16]-beta[17]+beta[18]-beta[19]-beta[20]+beta[21]-beta[22]+beta[23]+beta[24]-beta[25]-beta[26]+beta[27]+beta[28]-beta[29]-beta[30]+beta[31])+2*(beta[32]+beta[33]+beta[34]+beta[35]-beta[36]-beta[37]-beta[38]-beta[39]+beta[40]+beta[41]-beta[42]-beta[43]+beta[44]+beta[45]-beta[46]-beta[47]+beta[48]-beta[49]+beta[50]-beta[51]+beta[52]-beta[53]+beta[54]-beta[55])+(beta[56]+beta[57]+beta[58]+beta[59]+beta[60]+beta[61]+beta[62]+beta[63]);
    return a;
  }

  //@formatter:on

  /**
   * Interface for writing the cubic spline data.
   */
  private interface SplineWriter {
    /**
     * Write the spline function data.
     *
     * @param out the data output
     * @param function the function
     * @throws IOException Signals that an I/O exception has occurred.
     */
    void write(DataOutput out, CustomTricubicFunction function) throws IOException;
  }

  /**
   * Write float cubic spline data.
   */
  private static class FloatSplineWriter implements SplineWriter {
    @Override
    public void write(DataOutput out, CustomTricubicFunction function) throws IOException {
      for (int i = 0; i < 64; i++) {
        out.writeFloat(function.getf(i));
      }
    }
  }

  /**
   * Write double cubic spline data.
   */
  private static class DoubleSplineWriter implements SplineWriter {
    @Override
    public void write(DataOutput out, CustomTricubicFunction function) throws IOException {
      for (int i = 0; i < 64; i++) {
        out.writeDouble(function.get(i));
      }
    }
  }

  /**
   * Interface for reading the cubic spline data.
   */
  private interface SplineReader {

    /**
     * Read the spline function data.
     *
     * @param in the data input
     * @return the custom tricubic function
     * @throws IOException Signals that an I/O exception has occurred.
     */
    CustomTricubicFunction read(DataInput in) throws IOException;
  }

  /**
   * Read float cubic spline data.
   */
  private static class FloatSplineReader implements SplineReader {
    float[] data = new float[64];

    @Override
    public CustomTricubicFunction read(DataInput in) throws IOException {
      for (int i = 0; i < 64; i++) {
        data[i] = in.readFloat();
      }
      return new FloatCustomTricubicFunction(data.clone());
    }
  }

  /**
   * Read float cubic spline data.
   */
  private static class DoubleSplineReader implements SplineReader {
    double[] data = new double[64];

    @Override
    public CustomTricubicFunction read(DataInput in) throws IOException {
      for (int i = 0; i < 64; i++) {
        data[i] = in.readDouble();
      }
      return new DoubleCustomTricubicFunction(data.clone());
    }
  }

  /**
   * Write a tricubic function to the output stream. The output will be buffered for performance.
   *
   * @param outputStream the output stream
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public void write(OutputStream outputStream) throws IOException {
    write(outputStream, null);
  }

  /**
   * Write a tricubic function to the output stream. The output will be buffered for performance.
   *
   * @param outputStream the output stream
   * @param progress the progress
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public void write(OutputStream outputStream, TrackProgress progress) throws IOException {
    // Write dimensions
    final int lastI = xval.length - 1;
    final int lastJ = yval.length - 1;
    final int lastK = zval.length - 1;
    final Ticker ticker = Ticker.create(progress, (long) lastI * lastJ * lastK, false);
    ticker.start();
    final BufferedOutputStream buffer = new BufferedOutputStream(outputStream);
    final DataOutput out = new DataOutputStream(buffer);
    out.writeInt(xval.length);
    out.writeInt(yval.length);
    out.writeInt(zval.length);
    // Write axis values
    write(out, xval);
    write(out, yval);
    write(out, zval);
    // Write precision
    final boolean singlePrecision = isSinglePrecision();
    out.writeBoolean(singlePrecision);
    final SplineWriter writer =
        (singlePrecision) ? new FloatSplineWriter() : new DoubleSplineWriter();
    for (int i = 0; i < lastI; i++) {
      for (int j = 0; j < lastJ; j++) {
        for (int k = 0; k < lastK; k++) {
          writer.write(out, splines[i][j][k]);
          ticker.tick();
        }
      }
    }
    ticker.stop();
    buffer.flush();
  }

  private static void write(DataOutput out, double[] x) throws IOException {
    for (final double value : x) {
      out.writeDouble(value);
    }
  }

  /**
   * Read a tricubic function from the input stream.
   *
   * <p>Note: For best performance a buffered input stream should be used.
   *
   * @param inputStream the input stream
   * @return the custom tricubic interpolating function
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static CustomTricubicInterpolatingFunction read(InputStream inputStream)
      throws IOException {
    return read(inputStream, null);
  }

  /**
   * Read a tricubic function from the input stream.
   *
   * <p>Note: For best performance a buffered input stream should be used.
   *
   * @param inputStream the input stream
   * @param progress the progress
   * @return the custom tricubic interpolating function
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static CustomTricubicInterpolatingFunction read(InputStream inputStream,
      TrackProgress progress) throws IOException {
    // Read dimensions
    final BufferedInputStream buffer = new BufferedInputStream(inputStream);
    final DataInput in = new DataInputStream(buffer);
    final int maxx = in.readInt();
    final int maxy = in.readInt();
    final int maxz = in.readInt();
    final int lastI = maxx - 1;
    final int lastJ = maxy - 1;
    final int lastK = maxz - 1;
    final Ticker ticker = Ticker.create(progress, (long) lastI * lastJ * lastK, false);
    ticker.start();
    // Read axis values
    final double[] xval = read(in, maxx);
    final double[] yval = read(in, maxy);
    final double[] zval = read(in, maxz);
    // Read precision
    final boolean singlePrecision = in.readBoolean();
    final SplineReader reader =
        (singlePrecision) ? new FloatSplineReader() : new DoubleSplineReader();
    final CustomTricubicFunction[][][] splines = new CustomTricubicFunction[lastI][lastJ][lastK];
    for (int i = 0; i < lastI; i++) {
      for (int j = 0; j < lastJ; j++) {
        for (int k = 0; k < lastK; k++) {
          splines[i][j][k] = reader.read(in);
          ticker.tick();
        }
      }
    }
    ticker.stop();
    // Pass the data to a constructor for validation
    return new CustomTricubicInterpolatingFunction(new DoubleArrayValueProvider(xval),
        new DoubleArrayValueProvider(yval), new DoubleArrayValueProvider(zval), splines);
  }

  private static double[] read(DataInput in, int max) throws IOException {
    final double[] x = new double[max];
    for (int i = 0; i < x.length; i++) {
      x[i] = in.readDouble();
    }
    return x;
  }

  /**
   * Set to true if the x,y,z spline points are uniformly spaced.
   *
   * <p>This allows the function to be efficiently sampled using precomputed spline coefficients
   * (see {@link #value(int, int, int, double[])})
   *
   * @return true, if is uniform
   */
  public boolean isUniform() {
    return isUniformFlag;
  }

  /**
   * Set to true if the x,y,z spline points have a grid spacing of 1. Note that the spline points
   * may not be integer values, only the spacing between them.
   *
   * <p>This allows faster computation with no scaling.
   *
   * @return true, if is integer
   */
  public boolean isInteger() {
    return isIntegerFlag;
  }

  /**
   * Simple predicate to test double values.
   */
  @FunctionalInterface
  private interface DoubleBiPredicate {
    /**
     * Test the two values.
     *
     * @param d1 the first {@code double}
     * @param d2 the second {@code double}
     * @return the result
     */
    boolean test(double d1, double d2);
  }

  /**
   * Perform n refinements of a binary search to find the optimum value. The search finds the spline
   * node that has the optimum value. If refinements are performed then 8 vertices of the node cube
   * are evaluated per refinement and the optimum value selected. The bounds of the cube are then
   * reduced by 2.
   *
   * <p>The search starts with the bounds at 0,1 for each dimension. This search works because the
   * function is a cubic polynomial and so the peak at the optimum is closest-in-distance to the
   * closest-in-value bounding point.
   *
   * <p>The optimum will be found within error +/- scale/(2^refinements), e.g. 5 refinements will
   * have an error of +/- scale/32, with scale the distance between the node and the next node point
   * in each axis.
   *
   * <p>An optional tolerance for improvement can be specified. This is applied only if the optimum
   * vertex has changed, otherwise the value would be the same. If it has changed then the maximum
   * error will be greater than if the maximum refinements was achieved.
   *
   * @param maximum Set to true to find the maximum
   * @param refinements the refinements (if below 1 then no interpolation between node points is
   *        performed)
   * @param relativeError relative tolerance threshold (set to negative to ignore)
   * @param absoluteError absolute tolerance threshold (set to negative to ignore)
   * @return [x, y, z, value]
   */
  public double[] search(boolean maximum, int refinements, double relativeError,
      double absoluteError) {
    // Find the optimum node
    final int maxx = getMaxXSplinePosition();
    final int maxy = getMaxYSplinePosition();
    final int maxz = getMaxZSplinePosition();
    double optimumValue = splines[0][0][0].value000();
    int ox = 0;
    int oy = 0;
    int oz = 0;
    // Set the test to determine is the value is an improvement
    final DoubleBiPredicate valueIsBetter = (maximum)
        // Higher
        ? (d1, d2) -> d1 > d2
        // Lower
        : (d1, d2) -> d1 < d2;
    for (int x = 0; x < maxx; x++) {
      for (int y = 0; y < maxy; y++) {
        for (int z = 0; z < maxz; z++) {
          final double value = splines[x][y][z].value000();
          if (valueIsBetter.test(value, optimumValue)) {
            optimumValue = value;
            ox = x;
            oy = y;
            oz = z;
          }
        }
      }
    }

    if (refinements < 1) {
      return new double[] {xval[ox], yval[oy], zval[oz], optimumValue};
    }

    // We want to do refinement within the cube of the optimum node.
    // Evaluate the gradient at the node position to test how the value will change as
    // the optimum point (ox,oy,oz) is increased.
    final double[] derivative1 = new double[3];
    splines[ox][oy][oz].value000(derivative1);

    // Check if moving in the wrong direction.
    // A maximum search requires the gradient to be positive.
    // A minimum search requires the gradient to be negative.
    // If the gradient is zero then keep the same node as we already know this is the
    // optimum point. A cubic polynomial between this and the next node with zero gradient
    // would require a lower neighbour node or a flat line between them making them equal
    // for interpolation.
    final DoublePredicate gradientIsWrongDirection = (maximum)
        // Maximum search: if the gradient is negative this is the wrong direction
        ? d1 -> d1 < 0
        // Minimum search: if the gradient is positive this is the wrong direction
        : d1 -> d1 > 0;
    if (gradientIsWrongDirection.test(derivative1[0])) {
      ox = Math.max(0, ox - 1);
    }
    if (gradientIsWrongDirection.test(derivative1[1])) {
      oy = Math.max(0, oy - 1);
    }
    if (gradientIsWrongDirection.test(derivative1[2])) {
      oz = Math.max(0, oz - 1);
    }

    final double[] optimum =
        splines[ox][oy][oz].search(maximum, refinements, relativeError, absoluteError);

    // Scale the coordinates
    optimum[0] = xval[ox] + xscale[ox] * optimum[0];
    optimum[1] = xval[oy] + xscale[oy] * optimum[1];
    optimum[2] = xval[oz] + xscale[oz] * optimum[2];
    return optimum;
  }

  // @CHECKSTYLE.OFF: ParameterName

  /**
   * Create a tricubic interpolating function for interpolation between 0 and 1. The input must have
   * function values and derivatives for each vertex of the cube [2x2x2].
   *
   * @param f Values of the function on every grid point.
   * @param dFdX Values of the partial derivative of function with respect to x on every grid point.
   * @param dFdY Values of the partial derivative of function with respect to y on every grid point.
   * @param dFdZ Values of the partial derivative of function with respect to z on every grid point.
   * @param d2FdXdY Values of the cross partial derivative of function on every grid point.
   * @param d2FdXdZ Values of the cross partial derivative of function on every grid point.
   * @param d2FdYdZ Values of the cross partial derivative of function on every grid point.
   * @param d3FdXdYdZ Values of the cross partial derivative of function on every grid point.
   * @return tricubic interpolating function
   * @throws DimensionMismatchException if the array lengths are inconsistent.
   */
  public static CustomTricubicFunction create(final TrivalueProvider f, final TrivalueProvider dFdX,
      final TrivalueProvider dFdY, final TrivalueProvider dFdZ, final TrivalueProvider d2FdXdY,
      final TrivalueProvider d2FdXdZ, final TrivalueProvider d2FdYdZ,
      final TrivalueProvider d3FdXdYdZ) {
    checkDimensions(2, 2, 2, f);
    checkDimensions(2, 2, 2, dFdX);
    checkDimensions(2, 2, 2, dFdY);
    checkDimensions(2, 2, 2, dFdZ);
    checkDimensions(2, 2, 2, d2FdXdY);
    checkDimensions(2, 2, 2, d2FdXdZ);
    checkDimensions(2, 2, 2, d2FdYdZ);
    checkDimensions(2, 2, 2, d3FdXdYdZ);
    return createFunction(new double[64], f, dFdX, dFdY, dFdZ, d2FdXdY, d2FdXdZ, d2FdYdZ,
        d3FdXdYdZ);
  }

  /**
   * Create a tricubic interpolating function for interpolation between 0 and 1. The input must have
   * function values and derivatives for each vertex of the cube [2x2x2]. The input gradients are
   * assumed to require normalisation by the scale for each dimension.
   *
   * <p>To use the function to create an interpolated value in the range
   * [0-xscale,0-yscale,0-zscale]:
   *
   * <pre>
   * double value = f.value(x / xscale, y / yscale, z / zscale);
   * </pre>
   *
   * @param xscale the xscale
   * @param yscale the yscale
   * @param zscale the zscale
   * @param f Values of the function on every grid point.
   * @param dFdX Values of the partial derivative of function with respect to x on every grid point.
   * @param dFdY Values of the partial derivative of function with respect to y on every grid point.
   * @param dFdZ Values of the partial derivative of function with respect to z on every grid point.
   * @param d2FdXdY Values of the cross partial derivative of function on every grid point.
   * @param d2FdXdZ Values of the cross partial derivative of function on every grid point.
   * @param d2FdYdZ Values of the cross partial derivative of function on every grid point.
   * @param d3FdXdYdZ Values of the cross partial derivative of function on every grid point.
   * @return tricubic interpolating function
   * @throws DimensionMismatchException if the array lengths are inconsistent.
   */
  public static CustomTricubicFunction create(double xscale, double yscale, double zscale,
      final TrivalueProvider f, final TrivalueProvider dFdX, final TrivalueProvider dFdY,
      final TrivalueProvider dFdZ, final TrivalueProvider d2FdXdY, final TrivalueProvider d2FdXdZ,
      final TrivalueProvider d2FdYdZ, final TrivalueProvider d3FdXdYdZ) {
    checkDimensions(2, 2, 2, f);
    checkDimensions(2, 2, 2, dFdX);
    checkDimensions(2, 2, 2, dFdY);
    checkDimensions(2, 2, 2, dFdZ);
    checkDimensions(2, 2, 2, d2FdXdY);
    checkDimensions(2, 2, 2, d2FdXdZ);
    checkDimensions(2, 2, 2, d2FdYdZ);
    checkDimensions(2, 2, 2, d3FdXdYdZ);
    return createFunction(new double[64], xscale, yscale, zscale, f, dFdX, dFdY, dFdZ, d2FdXdY,
        d2FdXdZ, d2FdYdZ, d3FdXdYdZ);
  }


  /**
   * Create a tricubic interpolating function for interpolation between 0 and 1. The input must have
   * function values and derivatives for each vertex of the cube [2x2x2].
   *
   * @param beta the beta array working space (must be a double[64])
   * @param f Values of the function on every grid point.
   * @param dFdX Values of the partial derivative of function with respect to x on every grid point.
   * @param dFdY Values of the partial derivative of function with respect to y on every grid point.
   * @param dFdZ Values of the partial derivative of function with respect to z on every grid point.
   * @param d2FdXdY Values of the cross partial derivative of function on every grid point.
   * @param d2FdXdZ Values of the cross partial derivative of function on every grid point.
   * @param d2FdYdZ Values of the cross partial derivative of function on every grid point.
   * @param d3FdXdYdZ Values of the cross partial derivative of function on every grid point.
   * @return tricubic interpolating function
   */
  static CustomTricubicFunction createFunction(double[] beta, final TrivalueProvider f,
      final TrivalueProvider dFdX, final TrivalueProvider dFdY, final TrivalueProvider dFdZ,
      final TrivalueProvider d2FdXdY, final TrivalueProvider d2FdXdZ,
      final TrivalueProvider d2FdYdZ, final TrivalueProvider d3FdXdYdZ) {
    beta[0] = f.get(0, 0, 0);
    beta[1] = f.get(1, 0, 0);
    beta[2] = f.get(0, 1, 0);
    beta[3] = f.get(1, 1, 0);
    beta[4] = f.get(0, 0, 1);
    beta[5] = f.get(1, 0, 1);
    beta[6] = f.get(0, 1, 1);
    beta[7] = f.get(1, 1, 1);
    beta[8] = dFdX.get(0, 0, 0);
    beta[9] = dFdX.get(1, 0, 0);
    beta[10] = dFdX.get(0, 1, 0);
    beta[11] = dFdX.get(1, 1, 0);
    beta[12] = dFdX.get(0, 0, 1);
    beta[13] = dFdX.get(1, 0, 1);
    beta[14] = dFdX.get(0, 1, 1);
    beta[15] = dFdX.get(1, 1, 1);
    beta[16] = dFdY.get(0, 0, 0);
    beta[17] = dFdY.get(1, 0, 0);
    beta[18] = dFdY.get(0, 1, 0);
    beta[19] = dFdY.get(1, 1, 0);
    beta[20] = dFdY.get(0, 0, 1);
    beta[21] = dFdY.get(1, 0, 1);
    beta[22] = dFdY.get(0, 1, 1);
    beta[23] = dFdY.get(1, 1, 1);
    beta[24] = dFdZ.get(0, 0, 0);
    beta[25] = dFdZ.get(1, 0, 0);
    beta[26] = dFdZ.get(0, 1, 0);
    beta[27] = dFdZ.get(1, 1, 0);
    beta[28] = dFdZ.get(0, 0, 1);
    beta[29] = dFdZ.get(1, 0, 1);
    beta[30] = dFdZ.get(0, 1, 1);
    beta[31] = dFdZ.get(1, 1, 1);
    beta[32] = d2FdXdY.get(0, 0, 0);
    beta[33] = d2FdXdY.get(1, 0, 0);
    beta[34] = d2FdXdY.get(0, 1, 0);
    beta[35] = d2FdXdY.get(1, 1, 0);
    beta[36] = d2FdXdY.get(0, 0, 1);
    beta[37] = d2FdXdY.get(1, 0, 1);
    beta[38] = d2FdXdY.get(0, 1, 1);
    beta[39] = d2FdXdY.get(1, 1, 1);
    beta[40] = d2FdXdZ.get(0, 0, 0);
    beta[41] = d2FdXdZ.get(1, 0, 0);
    beta[42] = d2FdXdZ.get(0, 1, 0);
    beta[43] = d2FdXdZ.get(1, 1, 0);
    beta[44] = d2FdXdZ.get(0, 0, 1);
    beta[45] = d2FdXdZ.get(1, 0, 1);
    beta[46] = d2FdXdZ.get(0, 1, 1);
    beta[47] = d2FdXdZ.get(1, 1, 1);
    beta[48] = d2FdYdZ.get(0, 0, 0);
    beta[49] = d2FdYdZ.get(1, 0, 0);
    beta[50] = d2FdYdZ.get(0, 1, 0);
    beta[51] = d2FdYdZ.get(1, 1, 0);
    beta[52] = d2FdYdZ.get(0, 0, 1);
    beta[53] = d2FdYdZ.get(1, 0, 1);
    beta[54] = d2FdYdZ.get(0, 1, 1);
    beta[55] = d2FdYdZ.get(1, 1, 1);
    beta[56] = d3FdXdYdZ.get(0, 0, 0);
    beta[57] = d3FdXdYdZ.get(1, 0, 0);
    beta[58] = d3FdXdYdZ.get(0, 1, 0);
    beta[59] = d3FdXdYdZ.get(1, 1, 0);
    beta[60] = d3FdXdYdZ.get(0, 0, 1);
    beta[61] = d3FdXdYdZ.get(1, 0, 1);
    beta[62] = d3FdXdYdZ.get(0, 1, 1);
    beta[63] = d3FdXdYdZ.get(1, 1, 1);
    final double[] a = computeCoefficientsInlineCollectTerms(beta);
    return new DoubleCustomTricubicFunction(a);
  }

  /**
   * Create a tricubic interpolating function for interpolation between 0 and 1. The input must have
   * function values and derivatives for each vertex of the cube [2x2x2]. The input gradients are
   * assumed to require normalisation by the scale for each dimension.
   *
   * <p>To use the function to create an interpolated value in the range
   * [0-xscale,0-yscale,0-zscale]:
   *
   * <pre>
   * double value = f.value(x / xscale, y / yscale, z / zscale);
   * </pre>
   *
   * @param beta the beta array working space (must be a double[64])
   * @param xscale the xscale
   * @param yscale the yscale
   * @param zscale the zscale
   * @param f Values of the function on every grid point.
   * @param dFdX Values of the partial derivative of function with respect to x on every grid point.
   * @param dFdY Values of the partial derivative of function with respect to y on every grid point.
   * @param dFdZ Values of the partial derivative of function with respect to z on every grid point.
   * @param d2FdXdY Values of the cross partial derivative of function on every grid point.
   * @param d2FdXdZ Values of the cross partial derivative of function on every grid point.
   * @param d2FdYdZ Values of the cross partial derivative of function on every grid point.
   * @param d3FdXdYdZ Values of the cross partial derivative of function on every grid point.
   * @return tricubic interpolating function
   */
  static CustomTricubicFunction createFunction(double[] beta, double xscale, double yscale,
      double zscale, final TrivalueProvider f, final TrivalueProvider dFdX,
      final TrivalueProvider dFdY, final TrivalueProvider dFdZ, final TrivalueProvider d2FdXdY,
      final TrivalueProvider d2FdXdZ, final TrivalueProvider d2FdYdZ,
      final TrivalueProvider d3FdXdYdZ) {
    final double xR = xscale;
    final double yR = yscale;
    final double xRyR = xR * yR;
    final double zR = zscale;
    final double xRzR = xR * zR;
    final double yRzR = yR * zR;
    final double xRyRzR = xR * yRzR;
    beta[0] = f.get(0, 0, 0);
    beta[1] = f.get(1, 0, 0);
    beta[2] = f.get(0, 1, 0);
    beta[3] = f.get(1, 1, 0);
    beta[4] = f.get(0, 0, 1);
    beta[5] = f.get(1, 0, 1);
    beta[6] = f.get(0, 1, 1);
    beta[7] = f.get(1, 1, 1);
    beta[8] = dFdX.get(0, 0, 0) * xR;
    beta[9] = dFdX.get(1, 0, 0) * xR;
    beta[10] = dFdX.get(0, 1, 0) * xR;
    beta[11] = dFdX.get(1, 1, 0) * xR;
    beta[12] = dFdX.get(0, 0, 1) * xR;
    beta[13] = dFdX.get(1, 0, 1) * xR;
    beta[14] = dFdX.get(0, 1, 1) * xR;
    beta[15] = dFdX.get(1, 1, 1) * xR;
    beta[16] = dFdY.get(0, 0, 0) * yR;
    beta[17] = dFdY.get(1, 0, 0) * yR;
    beta[18] = dFdY.get(0, 1, 0) * yR;
    beta[19] = dFdY.get(1, 1, 0) * yR;
    beta[20] = dFdY.get(0, 0, 1) * yR;
    beta[21] = dFdY.get(1, 0, 1) * yR;
    beta[22] = dFdY.get(0, 1, 1) * yR;
    beta[23] = dFdY.get(1, 1, 1) * yR;
    beta[24] = dFdZ.get(0, 0, 0) * zR;
    beta[25] = dFdZ.get(1, 0, 0) * zR;
    beta[26] = dFdZ.get(0, 1, 0) * zR;
    beta[27] = dFdZ.get(1, 1, 0) * zR;
    beta[28] = dFdZ.get(0, 0, 1) * zR;
    beta[29] = dFdZ.get(1, 0, 1) * zR;
    beta[30] = dFdZ.get(0, 1, 1) * zR;
    beta[31] = dFdZ.get(1, 1, 1) * zR;
    beta[32] = d2FdXdY.get(0, 0, 0) * xRyR;
    beta[33] = d2FdXdY.get(1, 0, 0) * xRyR;
    beta[34] = d2FdXdY.get(0, 1, 0) * xRyR;
    beta[35] = d2FdXdY.get(1, 1, 0) * xRyR;
    beta[36] = d2FdXdY.get(0, 0, 1) * xRyR;
    beta[37] = d2FdXdY.get(1, 0, 1) * xRyR;
    beta[38] = d2FdXdY.get(0, 1, 1) * xRyR;
    beta[39] = d2FdXdY.get(1, 1, 1) * xRyR;
    beta[40] = d2FdXdZ.get(0, 0, 0) * xRzR;
    beta[41] = d2FdXdZ.get(1, 0, 0) * xRzR;
    beta[42] = d2FdXdZ.get(0, 1, 0) * xRzR;
    beta[43] = d2FdXdZ.get(1, 1, 0) * xRzR;
    beta[44] = d2FdXdZ.get(0, 0, 1) * xRzR;
    beta[45] = d2FdXdZ.get(1, 0, 1) * xRzR;
    beta[46] = d2FdXdZ.get(0, 1, 1) * xRzR;
    beta[47] = d2FdXdZ.get(1, 1, 1) * xRzR;
    beta[48] = d2FdYdZ.get(0, 0, 0) * yRzR;
    beta[49] = d2FdYdZ.get(1, 0, 0) * yRzR;
    beta[50] = d2FdYdZ.get(0, 1, 0) * yRzR;
    beta[51] = d2FdYdZ.get(1, 1, 0) * yRzR;
    beta[52] = d2FdYdZ.get(0, 0, 1) * yRzR;
    beta[53] = d2FdYdZ.get(1, 0, 1) * yRzR;
    beta[54] = d2FdYdZ.get(0, 1, 1) * yRzR;
    beta[55] = d2FdYdZ.get(1, 1, 1) * yRzR;
    beta[56] = d3FdXdYdZ.get(0, 0, 0) * xRyRzR;
    beta[57] = d3FdXdYdZ.get(1, 0, 0) * xRyRzR;
    beta[58] = d3FdXdYdZ.get(0, 1, 0) * xRyRzR;
    beta[59] = d3FdXdYdZ.get(1, 1, 0) * xRyRzR;
    beta[60] = d3FdXdYdZ.get(0, 0, 1) * xRyRzR;
    beta[61] = d3FdXdYdZ.get(1, 0, 1) * xRyRzR;
    beta[62] = d3FdXdYdZ.get(0, 1, 1) * xRyRzR;
    beta[63] = d3FdXdYdZ.get(1, 1, 1) * xRyRzR;
    final double[] a = computeCoefficientsInlineCollectTerms(beta);
    return new DoubleCustomTricubicFunction(a);
  }
}
