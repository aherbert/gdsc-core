package gdsc.core.math.interpolation;

/*----------------------------------------------------------------------------- 
 * GDSC Software
 * 
 * This is an extension of the 
 * org.apache.commons.math3.analysis.interpolation.TricubicInterpolatingFunction
 * 
 * Modifications have been made to allow computation of gradients.
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

import org.apache.commons.math3.analysis.TrivariateFunction;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.NoDataException;
import org.apache.commons.math3.exception.NonMonotonicSequenceException;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.util.MathArrays;

import gdsc.core.logging.TrackProgress;
import gdsc.core.utils.SimpleArrayUtils;

/**
 * Function that implements the
 * <a href="http://en.wikipedia.org/wiki/Tricubic_interpolation">
 * tricubic spline interpolation</a>, as proposed in
 * <blockquote>
 * Tricubic interpolation in three dimensions,
 * F. Lekien and J. Marsden,
 * <em>Int. J. Numer. Meth. Eng</em> 2005; <b>63</b>:455-471
 * </blockquote>
 */
//@formatter:off
public class CustomTricubicInterpolatingFunction
    implements TrivariateFunction {
	
	/** The tolerance for checking the spline points are uniform */
	public static final double UNIFORM_TOLERANCE = 1e-6;
	
	/** Set to true if the x,y,z spline points have integer values. */
	final boolean isInteger;
	
	/** 
	 * Set to true if the x,y,z spline points are uniformly spaced. 
	 * <p>
	 * This allows the function to be efficiently sampled using precomputed 
	 * spline coefficients (see {@link #value(int, int, int, double[])})
	 */
	final boolean isUniform;
	
	private double[] scale;
	
    /**
     * Matrix to compute the spline coefficients from the function values
     * and function derivatives values
     */
    private static final double[][] AINV = {
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
    };

    /** Samples x-coordinates */
    private final double[] xval;
    /** Samples y-coordinates */
    private final double[] yval;
    /** Samples z-coordinates */
    private final double[] zval;
    /** Set of cubic splines patching the whole data grid */
    private final CustomTricubicFunction[][][] splines;
    
	/** The scale for x. This is the interval between x[i+1] and x[i] */
	private final double[] xscale;
	/** The scale for y. This is the interval between y[i+1] and y[i] */
	private final double[] yscale;
	/** The scale for z. This is the interval between z[i+1] and z[i] */
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
     * @throws NoDataException if any of the arrays has zero length.
     * @throws DimensionMismatchException if the various arrays do not contain the expected number of elements.
     * @throws NonMonotonicSequenceException if {@code x}, {@code y} or {@code z} are not strictly increasing.
     */
    public CustomTricubicInterpolatingFunction(double[] x,
                                         double[] y,
                                         double[] z,
                                         double[][][] f,
                                         double[][][] dFdX,
                                         double[][][] dFdY,
                                         double[][][] dFdZ,
                                         double[][][] d2FdXdY,
                                         double[][][] d2FdXdZ,
                                         double[][][] d2FdYdZ,
                                         double[][][] d3FdXdYdZ,
                                         TrackProgress progress)
        throws NoDataException,
               DimensionMismatchException,
               NonMonotonicSequenceException {
        final int xLen = x.length;
        final int yLen = y.length;
        final int zLen = z.length;

        // The original only failed if the length was zero. However
        // this function requires two points to interpolate between so 
        // check the length is at least 2.
        if (xLen <= 1 || yLen <= 1 || z.length <= 1 || f.length <= 1 || f[0].length <= 1) {
            throw new NoDataException();
        }
        if (xLen != f.length) {
            throw new DimensionMismatchException(xLen, f.length);
        }
        if (xLen != dFdX.length) {
            throw new DimensionMismatchException(xLen, dFdX.length);
        }
        if (xLen != dFdY.length) {
            throw new DimensionMismatchException(xLen, dFdY.length);
        }
        if (xLen != dFdZ.length) {
            throw new DimensionMismatchException(xLen, dFdZ.length);
        }
        if (xLen != d2FdXdY.length) {
            throw new DimensionMismatchException(xLen, d2FdXdY.length);
        }
        if (xLen != d2FdXdZ.length) {
            throw new DimensionMismatchException(xLen, d2FdXdZ.length);
        }
        if (xLen != d2FdYdZ.length) {
            throw new DimensionMismatchException(xLen, d2FdYdZ.length);
        }
        if (xLen != d3FdXdYdZ.length) {
            throw new DimensionMismatchException(xLen, d3FdXdYdZ.length);
        }

        MathArrays.checkOrder(x);
        MathArrays.checkOrder(y);
        MathArrays.checkOrder(z);
        
        
        isInteger = SimpleArrayUtils.isInteger(x) && SimpleArrayUtils.isInteger(y) && SimpleArrayUtils.isInteger(z); 
    	isUniform = 
    			SimpleArrayUtils.isUniform(x, (x[1]-x[0])*UNIFORM_TOLERANCE) && 
    			SimpleArrayUtils.isUniform(y, (y[1]-y[0])*UNIFORM_TOLERANCE) && 
    			SimpleArrayUtils.isUniform(z, (z[1]-z[0])*UNIFORM_TOLERANCE);
 
        xval = x.clone();
        yval = y.clone();
        zval = z.clone();

        xscale = createScale(x);
        yscale = createScale(y);
        zscale = createScale(z);
        
        final int lastI = xLen - 1;
        final int lastJ = yLen - 1;
        final int lastK = zLen - 1;
        splines = new CustomTricubicFunction[lastI][lastJ][lastK];

        final long total = lastI * lastJ * lastK;
        long current = 0;
        
        for (int i = 0; i < lastI; i++) {
            if (f[i].length != yLen) {
                throw new DimensionMismatchException(f[i].length, yLen);
            }
            if (dFdX[i].length != yLen) {
                throw new DimensionMismatchException(dFdX[i].length, yLen);
            }
            if (dFdY[i].length != yLen) {
                throw new DimensionMismatchException(dFdY[i].length, yLen);
            }
            if (dFdZ[i].length != yLen) {
                throw new DimensionMismatchException(dFdZ[i].length, yLen);
            }
            if (d2FdXdY[i].length != yLen) {
                throw new DimensionMismatchException(d2FdXdY[i].length, yLen);
            }
            if (d2FdXdZ[i].length != yLen) {
                throw new DimensionMismatchException(d2FdXdZ[i].length, yLen);
            }
            if (d2FdYdZ[i].length != yLen) {
                throw new DimensionMismatchException(d2FdYdZ[i].length, yLen);
            }
            if (d3FdXdYdZ[i].length != yLen) {
                throw new DimensionMismatchException(d3FdXdYdZ[i].length, yLen);
            }

            final int ip1 = i + 1;
            final double xR = xval[ip1] - xval[i];
            for (int j = 0; j < lastJ; j++) {
                if (f[i][j].length != zLen) {
                    throw new DimensionMismatchException(f[i][j].length, zLen);
                }
                if (dFdX[i][j].length != zLen) {
                    throw new DimensionMismatchException(dFdX[i][j].length, zLen);
                }
                if (dFdY[i][j].length != zLen) {
                    throw new DimensionMismatchException(dFdY[i][j].length, zLen);
                }
                if (dFdZ[i][j].length != zLen) {
                    throw new DimensionMismatchException(dFdZ[i][j].length, zLen);
                }
                if (d2FdXdY[i][j].length != zLen) {
                    throw new DimensionMismatchException(d2FdXdY[i][j].length, zLen);
                }
                if (d2FdXdZ[i][j].length != zLen) {
                    throw new DimensionMismatchException(d2FdXdZ[i][j].length, zLen);
                }
                if (d2FdYdZ[i][j].length != zLen) {
                    throw new DimensionMismatchException(d2FdYdZ[i][j].length, zLen);
                }
                if (d3FdXdYdZ[i][j].length != zLen) {
                    throw new DimensionMismatchException(d3FdXdYdZ[i][j].length, zLen);
                }

                final int jp1 = j + 1;
                final double yR = yval[jp1] - yval[j];
                final double xRyR = xR * yR;
                for (int k = 0; k < lastK; k++) {
                	progress.progress(current++, total);
                    final int kp1 = k + 1;
                    final double zR = zval[kp1] - zval[k];
                    final double xRzR = xR * zR;
                    final double yRzR = yR * zR;
                    final double xRyRzR = xR * yRzR;

                    final double[] beta = new double[] {
                        f[i][j][k], f[ip1][j][k],
                        f[i][jp1][k], f[ip1][jp1][k],
                        f[i][j][kp1], f[ip1][j][kp1],
                        f[i][jp1][kp1], f[ip1][jp1][kp1],

                        dFdX[i][j][k] * xR, dFdX[ip1][j][k] * xR,
                        dFdX[i][jp1][k] * xR, dFdX[ip1][jp1][k] * xR,
                        dFdX[i][j][kp1] * xR, dFdX[ip1][j][kp1] * xR,
                        dFdX[i][jp1][kp1] * xR, dFdX[ip1][jp1][kp1] * xR,

                        dFdY[i][j][k] * yR, dFdY[ip1][j][k] * yR,
                        dFdY[i][jp1][k] * yR, dFdY[ip1][jp1][k] * yR,
                        dFdY[i][j][kp1] * yR, dFdY[ip1][j][kp1] * yR,
                        dFdY[i][jp1][kp1] * yR, dFdY[ip1][jp1][kp1] * yR,

                        dFdZ[i][j][k] * zR, dFdZ[ip1][j][k] * zR,
                        dFdZ[i][jp1][k] * zR, dFdZ[ip1][jp1][k] * zR,
                        dFdZ[i][j][kp1] * zR, dFdZ[ip1][j][kp1] * zR,
                        dFdZ[i][jp1][kp1] * zR, dFdZ[ip1][jp1][kp1] * zR,

                        d2FdXdY[i][j][k] * xRyR, d2FdXdY[ip1][j][k] * xRyR,
                        d2FdXdY[i][jp1][k] * xRyR, d2FdXdY[ip1][jp1][k] * xRyR,
                        d2FdXdY[i][j][kp1] * xRyR, d2FdXdY[ip1][j][kp1] * xRyR,
                        d2FdXdY[i][jp1][kp1] * xRyR, d2FdXdY[ip1][jp1][kp1] * xRyR,

                        d2FdXdZ[i][j][k] * xRzR, d2FdXdZ[ip1][j][k] * xRzR,
                        d2FdXdZ[i][jp1][k] * xRzR, d2FdXdZ[ip1][jp1][k] * xRzR,
                        d2FdXdZ[i][j][kp1] * xRzR, d2FdXdZ[ip1][j][kp1] * xRzR,
                        d2FdXdZ[i][jp1][kp1] * xRzR, d2FdXdZ[ip1][jp1][kp1] * xRzR,

                        d2FdYdZ[i][j][k] * yRzR, d2FdYdZ[ip1][j][k] * yRzR,
                        d2FdYdZ[i][jp1][k] * yRzR, d2FdYdZ[ip1][jp1][k] * yRzR,
                        d2FdYdZ[i][j][kp1] * yRzR, d2FdYdZ[ip1][j][kp1] * yRzR,
                        d2FdYdZ[i][jp1][kp1] * yRzR, d2FdYdZ[ip1][jp1][kp1] * yRzR,

                        d3FdXdYdZ[i][j][k] * xRyRzR, d3FdXdYdZ[ip1][j][k] * xRyRzR,
                        d3FdXdYdZ[i][jp1][k] * xRyRzR, d3FdXdYdZ[ip1][jp1][k] * xRyRzR,
                        d3FdXdYdZ[i][j][kp1] * xRyRzR, d3FdXdYdZ[ip1][j][kp1] * xRyRzR,
                        d3FdXdYdZ[i][jp1][kp1] * xRyRzR, d3FdXdYdZ[ip1][jp1][kp1] * xRyRzR,
                    };

                    // Q. Option to create as single precision?
                    splines[i][j][k] = new DoubleCustomTricubicFunction(computeCoefficients(beta));
                }
            }
        }
        
        progress.progress(1);
    }

    private static double[] createScale(double[] x)
	{
    	int n = x.length-1;
    	double[] scale = new double[n];
    	for (int i=0; i < n; i++)
    		scale[i] = x[i + 1] - x[i];
		return scale;
	}
    
	/**
     * {@inheritDoc}
     *
     * @throws OutOfRangeException if any of the variables is outside its interpolation range.
     */
    public double value(double x, double y, double z)
        throws OutOfRangeException {
        final int i = searchIndex(x, xval);
        final int j = searchIndex(y, yval);
        final int k = searchIndex(z, zval);

        final double xN = (x - xval[i]) / (xval[i + 1] - xval[i]);
        final double yN = (y - yval[j]) / (yval[j + 1] - yval[j]);
        final double zN = (z - zval[k]) / (zval[k + 1] - zval[k]);

        return splines[i][j][k].value(xN, yN, zN);
    }
    
    //@formatter:on

	/**
	 * @param c
	 *            Coordinate.
	 * @param val
	 *            Coordinate samples.
	 * @return the index in {@code val} corresponding to the interval containing {@code c}, or {@code -1}
	 *         if {@code c} is out of the range defined by the end values of {@code val}.
	 * @throws OutOfRangeException
	 *             if any of the variables is outside its interpolation range.
	 */
	private static int searchIndex(double c, double[] val) throws OutOfRangeException
	{
		return searchIndexBinarySearch(c, val);

		//// TODO - remove this after testing the binary search
		//int i = searchIndexOriginal(c, val);
		//int j = searchIndexBinarySearch(c, val);
		//if (i != j)
		//	throw new RuntimeException();
		//return j;
	}

	/**
	 * @param c
	 *            Coordinate.
	 * @param val
	 *            Coordinate samples.
	 * @return the index in {@code val} corresponding to the interval containing {@code c}
	 * @throws OutOfRangeException
	 *             if any of the variables is outside its interpolation range.
	 */
	@SuppressWarnings("unused")
	private static int searchIndexOriginal(double c, double[] val) throws OutOfRangeException
	{
		if (c < val[0])
		{
			throw new OutOfRangeException(c, val[0], val[val.length - 1]);
			//return -1;
		}

		final int max = val.length;
		for (int i = 1; i < max; i++)
		{
			if (c <= val[i])
			{
				return i - 1;
			}
		}

		throw new OutOfRangeException(c, val[0], val[val.length - 1]);
		//return -1;
	}

	/**
	 * @param c
	 *            Coordinate.
	 * @param val
	 *            Coordinate samples.
	 * @return the index in {@code val} corresponding to the interval containing {@code c}, or {@code -1}
	 *         if {@code c} is out of the range defined by the end values of {@code val}.
	 * @throws OutOfRangeException
	 *             if any of the variables is outside its interpolation range.
	 */
	private static int searchIndexBinarySearch(double c, double[] val) throws OutOfRangeException
	{
		// Use a Binary search.
		// We want to find the index equal to or before the key.  
		int high = val.length - 1;

		if (c < val[0] || c > val[high])
			throw new OutOfRangeException(c, val[0], val[high]);

		int i = binarySearch0(val, 0, val.length, c);
		if (i < 0)
		{
			// Not found. Convert to the insertion point.
			// We have already checked the upper bound and so we know the insertion point is 
			// below 'high'.
			i = (-i - 1);
		}
		// Return the index before. This makes index in the range 0 to high-1. 
		return (i > 0) ? i - 1 : i;
		//return Math.max(0, i - 1);
	}

	/**
	 * Binary search. Copied from Arrays.binarySearch0(...).
	 *
	 * Searches a range of
	 * the specified array of doubles for the specified value using
	 * the binary search algorithm.
	 * The range must be sorted
	 * (as by the {@link #sort(double[], int, int)} method)
	 * prior to making this call.
	 * If it is not sorted, the results are undefined. If the range contains
	 * multiple elements with the specified value, there is no guarantee which
	 * one will be found. This method considers all NaN values to be
	 * equivalent and equal.
	 *
	 * @param a
	 *            the array to be searched
	 * @param fromIndex
	 *            the index of the first element (inclusive) to be
	 *            searched
	 * @param toIndex
	 *            the index of the last element (exclusive) to be searched
	 * @param key
	 *            the value to be searched for
	 * @return index of the search key, if it is contained in the array
	 *         within the specified range;
	 *         otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>. The
	 *         <i>insertion point</i> is defined as the point at which the
	 *         key would be inserted into the array: the index of the first
	 *         element in the range greater than the key,
	 *         or <tt>toIndex</tt> if all
	 *         elements in the range are less than the specified key. Note
	 *         that this guarantees that the return value will be &gt;= 0 if
	 *         and only if the key is found.
	 */
	private static int binarySearch0(double[] a, int fromIndex, int toIndex, double key)
	{
		int low = fromIndex;
		int high = toIndex - 1;

		while (low <= high)
		{
			int mid = (low + high) >>> 1;
			double midVal = a[mid];

			if (midVal < key)
				low = mid + 1; // Neither val is NaN, thisVal is smaller
			else if (midVal > key)
				high = mid - 1; // Neither val is NaN, thisVal is larger
			else
			{
				// Remove the comparison using long bits

				//long midBits = Double.doubleToLongBits(midVal);
				//long keyBits = Double.doubleToLongBits(key);
				//if (midBits == keyBits) // Values are equal
				return mid; // Key found
				//else if (midBits < keyBits) // (-0.0, 0.0) or (!NaN, NaN)
				//	low = mid + 1;
				//else // (0.0, -0.0) or (NaN, !NaN)
				//	high = mid - 1;
			}
		}
		return -(low + 1); // key not found.
	}

	/**
	 * Gets the spline position.
	 *
	 * @param xval
	 *            the spline values
	 * @param xscale
	 *            the scale (xval[i+1] - xval[i])
	 * @param x
	 *            the value
	 * @return the spline position
	 * @throws OutOfRangeException
	 *             if the value is outside its interpolation range.
	 */
	private static IndexedCubicSplinePosition getSplinePosition(double[] xval, double[] xscale, double x)
	{
		final int i = searchIndex(x, xval);
		final double xN = (x - xval[i]) / xscale[i];
		return new IndexedCubicSplinePosition(i, xN, xscale[i], false);
	}

	/**
	 * Gets the x spline position.
	 *
	 * @param value
	 *            the x value
	 * @return the x spline position
	 * @throws OutOfRangeException
	 *             if the value is outside its interpolation range.
	 */
	public IndexedCubicSplinePosition getXSplinePosition(double value) throws OutOfRangeException
	{
		return getSplinePosition(xval, xscale, value);
	}

	/**
	 * Gets the y spline position.
	 *
	 * @param value
	 *            the y value
	 * @return the y spline position
	 * @throws OutOfRangeException
	 *             if the value is outside its interpolation range.
	 */
	public IndexedCubicSplinePosition getYSplinePosition(double value)
	{
		return getSplinePosition(yval, yscale, value);
	}

	/**
	 * Gets the z spline position.
	 *
	 * @param value
	 *            the z value
	 * @return the z spline position
	 * @throws OutOfRangeException
	 *             if the value is outside its interpolation range.
	 */
	public IndexedCubicSplinePosition getZSplinePosition(double value)
	{
		return getSplinePosition(zval, zscale, value);
	}

	/**
	 * Get the interpolated value using pre-computed spline positions.
	 *
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 * @param z
	 *            the z
	 * @return the value
	 * @throws ArrayIndexOutOfBoundsException
	 *             if the spline node does not exist
	 */
	public double value(IndexedCubicSplinePosition x, IndexedCubicSplinePosition y, IndexedCubicSplinePosition z)
			throws ArrayIndexOutOfBoundsException
	{
		return splines[x.index][y.index][z.index].value(x, y, z);
	}

	/**
	 * Get the interpolated value using pre-computed spline coefficient power table.
	 *
	 * @param xindex
	 *            the x spline position
	 * @param yindex
	 *            the y spline position
	 * @param zindex
	 *            the z spline position
	 * @param table
	 *            the table of 64 precomputed power coefficients
	 * @return the value
	 * @throws ArrayIndexOutOfBoundsException
	 *             if the spline node does not exist
	 * @see CustomTricubicFunction#computePowerTable(double, double, double)
	 */
	public double value(int xindex, int yindex, int zindex, double[] table)
	{
		return splines[xindex][yindex][zindex].value(table);
	}

	/**
	 * Get the interpolated value using pre-computed spline coefficient power table.
	 *
	 * @param xindex
	 *            the x spline position
	 * @param yindex
	 *            the y spline position
	 * @param zindex
	 *            the z spline position
	 * @param table
	 *            the table of 64 precomputed power coefficients
	 * @return the value
	 * @throws ArrayIndexOutOfBoundsException
	 *             if the spline node does not exist
	 * @see CustomTricubicFunction#computePowerTable(double, double, double)
	 */
	public double value(int xindex, int yindex, int zindex, float[] table)
	{
		return splines[xindex][yindex][zindex].value(table);
	}

	/**
	 * Get the interpolated value and partial first-order derivatives.
	 *
	 * @param x
	 *            the x value
	 * @param y
	 *            the y value
	 * @param z
	 *            the z value
	 * @param df_da
	 *            the partial first order derivatives with respect to x,y,z
	 * @return the value
	 * @throws OutOfRangeException
	 *             if any of the variables is outside its interpolation range.
	 */
	public double value(double x, double y, double z, double[] df_da) throws OutOfRangeException
	{
		final int i = searchIndex(x, xval);
		final int j = searchIndex(y, yval);
		final int k = searchIndex(z, zval);

		final double xN = (x - xval[i]) / xscale[i];
		final double yN = (y - yval[j]) / yscale[j];
		final double zN = (z - zval[k]) / zscale[k];

		double value = splines[i][j][k].value(xN, yN, zN, df_da);
		df_da[0] /= xscale[i];
		df_da[1] /= yscale[j];
		df_da[2] /= zscale[k];
		return value;
	}

	/**
	 * Get the interpolated value and partial first-order derivatives using pre-computed spline positions.
	 *
	 * @param x
	 *            the x value
	 * @param y
	 *            the y value
	 * @param z
	 *            the z value
	 * @param df_da
	 *            the partial first order derivatives with respect to x,y,z
	 * @return the value
	 * @throws ArrayIndexOutOfBoundsException
	 *             if the spline node does not exist
	 */
	public double value(IndexedCubicSplinePosition x, IndexedCubicSplinePosition y, IndexedCubicSplinePosition z,
			double[] df_da) throws ArrayIndexOutOfBoundsException
	{
		return splines[x.index][y.index][z.index].value(x, y, z, df_da);
	}

	/**
	 * Get the interpolated value and partial first-order derivatives using pre-computed spline coefficient power table.
	 *
	 * @param xindex
	 *            the x spline position
	 * @param yindex
	 *            the y spline position
	 * @param zindex
	 *            the z spline position
	 * @param table
	 *            the power table
	 * @param df_da
	 *            the partial first order derivatives with respect to x,y,z
	 * @return the value
	 * @throws ArrayIndexOutOfBoundsException
	 *             if the spline node does not exist
	 * @see CustomTricubicFunction#computePowerTable(double, double, double)
	 */
	public double value(int xindex, int yindex, int zindex, double[] table, double[] df_da)
	{
		double value = splines[xindex][yindex][zindex].value(table, df_da);
		df_da[0] /= xscale[xindex];
		df_da[1] /= yscale[yindex];
		df_da[2] /= zscale[zindex];
		return value;
	}

	/**
	 * Get the interpolated value and partial first-order derivatives using pre-computed spline coefficient power table.
	 *
	 * @param xindex
	 *            the x spline position
	 * @param yindex
	 *            the y spline position
	 * @param zindex
	 *            the z spline position
	 * @param table
	 *            the power table
	 * @param df_da
	 *            the partial first order derivatives with respect to x,y,z
	 * @return the value
	 * @throws ArrayIndexOutOfBoundsException
	 *             if the spline node does not exist
	 * @see CustomTricubicFunction#computePowerTable(double, double, double)
	 */
	public double value(int xindex, int yindex, int zindex, float[] table, double[] df_da)
	{
		double value = splines[xindex][yindex][zindex].value(table, df_da);
		df_da[0] /= xscale[xindex];
		df_da[1] /= yscale[yindex];
		df_da[2] /= zscale[zindex];
		return value;
	}

	/**
	 * Get the interpolated value and partial first-order and second-order derivatives.
	 *
	 * @param x
	 *            the x value
	 * @param y
	 *            the y value
	 * @param z
	 *            the z value
	 * @param df_da
	 *            the partial first order derivatives with respect to x,y,z
	 * @param d2f_da2
	 *            the partial second order derivatives with respect to x,y,z
	 * @return the value
	 * @throws OutOfRangeException
	 *             if any of the variables is outside its interpolation range.
	 */
	public double value(double x, double y, double z, double[] df_da, double[] d2f_da2) throws OutOfRangeException
	{
		final int i = searchIndex(x, xval);
		final int j = searchIndex(y, yval);
		final int k = searchIndex(z, zval);

		final double xN = (x - xval[i]) / xscale[i];
		final double yN = (y - yval[j]) / yscale[j];
		final double zN = (z - zval[k]) / zscale[k];

		double value = splines[i][j][k].value(xN, yN, zN, df_da, d2f_da2);
		df_da[0] /= xscale[i];
		df_da[1] /= yscale[j];
		df_da[2] /= zscale[k];
		d2f_da2[0] /= xscale[i] * xscale[i];
		d2f_da2[1] /= yscale[j] * yscale[j];
		d2f_da2[2] /= zscale[k] * zscale[k];
		return value;
	}

	/**
	 * Get the interpolated value and partial first-order and second-order derivatives using pre-computed spline
	 * positions.
	 *
	 * @param x
	 *            the x value
	 * @param y
	 *            the y value
	 * @param z
	 *            the z value
	 * @param df_da
	 *            the partial first order derivatives with respect to x,y,z
	 * @param d2f_da2
	 *            the partial second order derivatives with respect to x,y,z
	 * @return the value
	 * @throws ArrayIndexOutOfBoundsException
	 *             if the spline node does not exist
	 */
	public double value(IndexedCubicSplinePosition x, IndexedCubicSplinePosition y, IndexedCubicSplinePosition z,
			double[] df_da, double[] d2f_da2) throws ArrayIndexOutOfBoundsException
	{
		return splines[x.index][y.index][z.index].value(x, y, z, df_da, d2f_da2);
	}

	/**
	 * Get the interpolated value and partial first-order and second-order derivatives using pre-computed spline
	 * coefficient power table.
	 *
	 * @param xindex
	 *            the x spline position
	 * @param yindex
	 *            the y spline position
	 * @param zindex
	 *            the z spline position
	 * @param table
	 *            the power table
	 * @param df_da
	 *            the partial first order derivatives with respect to x,y,z
	 * @param d2f_da2
	 *            the partial second order derivatives with respect to x,y,z
	 * @return the value
	 * @throws ArrayIndexOutOfBoundsException
	 *             if the spline node does not exist
	 * @see CustomTricubicFunction#computePowerTable(double, double, double)
	 */
	public double value(int xindex, int yindex, int zindex, double[] table, double[] df_da, double[] d2f_da2)
	{
		double value = splines[xindex][yindex][zindex].value(table, df_da, d2f_da2);
		df_da[0] /= xscale[xindex];
		df_da[1] /= yscale[yindex];
		df_da[2] /= zscale[zindex];
		d2f_da2[0] /= xscale[xindex] * xscale[xindex];
		d2f_da2[1] /= yscale[yindex] * yscale[yindex];
		d2f_da2[2] /= zscale[zindex] * zscale[zindex];
		return value;
	}

	/**
	 * Get the interpolated value and partial first-order and second-order derivatives using pre-computed spline
	 * coefficient power table.
	 *
	 * @param xindex
	 *            the x spline position
	 * @param yindex
	 *            the y spline position
	 * @param zindex
	 *            the z spline position
	 * @param table
	 *            the power table
	 * @param df_da
	 *            the partial first order derivatives with respect to x,y,z
	 * @param d2f_da2
	 *            the partial second order derivatives with respect to x,y,z
	 * @return the value
	 * @throws ArrayIndexOutOfBoundsException
	 *             if the spline node does not exist
	 * @see CustomTricubicFunction#computePowerTable(double, double, double)
	 */
	public double value(int xindex, int yindex, int zindex, float[] table, double[] df_da, double[] d2f_da2)
	{
		double value = splines[xindex][yindex][zindex].value(table, df_da, d2f_da2);
		df_da[0] /= xscale[xindex];
		df_da[1] /= yscale[yindex];
		df_da[2] /= zscale[zindex];
		d2f_da2[0] /= xscale[xindex] * xscale[xindex];
		d2f_da2[1] /= yscale[yindex] * yscale[yindex];
		d2f_da2[2] /= zscale[zindex] * zscale[zindex];
		return value;
	}

	/**
	 * Gets the scale to convert the cube interval 0-1 back to correctly scaled values.
	 * <p>
	 * This is only valid if the function is uniform on each input axis (see {@link #isUniform}).
	 *
	 * @return the scale
	 * @throws IllegalStateException
	 *             the illegal state exception
	 */
	public double[] getScale() throws IllegalStateException
	{
		if (isUniform)
		{
			if (scale == null)
			{
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

	private static double getScale(double[] xval)
	{
		int n = xval.length - 1;
		return (xval[n] - xval[0]) / n;
	}

	private static double getMax(double[] xval)
	{
		return xval[xval.length - 1];
	}

	private static int getMaxSplinePosition(double[] xval)
	{
		return xval.length - 2;
	}

	/**
	 * Gets the max X value for interpolation.
	 *
	 * @return the max X value
	 */
	public double getMaxX()
	{
		return getMax(xval);
	}

	/**
	 * Gets the max X spline position for interpolation.
	 *
	 * @return the max X spline position
	 */
	public int getMaxXSplinePosition()
	{
		return getMaxSplinePosition(xval);
	}

	/**
	 * Gets the min X value for interpolation.
	 *
	 * @return the min X value
	 */
	public double getMinX()
	{
		return xval[0];
	}

	/**
	 * Gets the x spline value for the spline position. Equivalent to the x-value used when constructing the spline
	 *
	 * @param position
	 *            the position
	 * @return the x spline value
	 */
	public double getXSplineValue(int position)
	{
		return xval[position];
	}

	/**
	 * Gets the max Y value for interpolation.
	 *
	 * @return the max Y value
	 */
	public double getMaxY()
	{
		return getMax(yval);
	}

	/**
	 * Gets the max Y spline position for interpolation.
	 *
	 * @return the max Y spline position
	 */
	public int getMaxYSplinePosition()
	{
		return getMaxSplinePosition(yval);
	}

	/**
	 * Gets the min Y value for interpolation.
	 *
	 * @return the min Y value
	 */
	public double getMinY()
	{
		return yval[0];
	}

	/**
	 * Gets the y spline value for the spline position. Equivalent to the y-value used when constructing the spline
	 *
	 * @param position
	 *            the position
	 * @return the x spline value
	 */
	public double getYSplineValue(int position)
	{
		return yval[position];
	}

	/**
	 * Gets the max Z value for interpolation.
	 *
	 * @return the max Z value
	 */
	public double getMaxZ()
	{
		return getMax(zval);
	}

	/**
	 * Gets the max Z spline position for interpolation.
	 *
	 * @return the max Z spline position
	 */
	public int getMaxZSplinePosition()
	{
		return getMaxSplinePosition(zval);
	}

	/**
	 * Gets the min Z value for interpolation.
	 *
	 * @return the min Z value
	 */
	public double getMinZ()
	{
		return zval[0];
	}

	/**
	 * Gets the z spline value for the spline position. Equivalent to the z-value used when constructing the spline
	 *
	 * @param position
	 *            the position
	 * @return the z spline value
	 */
	public double getZSplineValue(int position)
	{
		return zval[position];
	}

	/**
	 * Indicates whether a point is within the interpolation range.
	 *
	 * @param x
	 *            First coordinate.
	 * @param y
	 *            Second coordinate.
	 * @param z
	 *            Third coordinate.
	 * @return {@code true} if (x, y, z) is a valid point.
	 */
	public boolean isValidPoint(double x, double y, double z)
	{
		if (x < xval[0] || x > xval[xval.length - 1] || y < yval[0] || y > yval[yval.length - 1] || z < zval[0] ||
				z > zval[zval.length - 1])
		{
			return false;
		}
		else
		{
			return true;
		}
	}

	/**
	 * Pre-compute gradient coefficients for partial derivatives.
	 *
	 * @param order
	 *            the order (<=2)
	 */
	public void precomputeGradientCoefficients(int order)
	{
		int maxj = getMaxYSplinePosition() + 1;
		int maxk = getMaxZSplinePosition() + 1;
		for (int i = getMaxXSplinePosition() + 1; i-- > 0;)
		{
			for (int j = maxj; j-- > 0;)
			{
				for (int k = maxk; k-- > 0;)
				{
					splines[i][j][k].precomputeGradientCoefficients(order);
				}
			}
		}
	}

	/**
	 * Gets the spline node. This is package scope and used for testing.
	 *
	 * @param i
	 *            the i
	 * @param j
	 *            the j
	 * @param k
	 *            the k
	 * @return the spline node
	 */
	CustomTricubicFunction getSplineNode(int i, int j, int k)
	{
		return splines[i][j][k];
	}

	/**
	 * Checks if is single precision.
	 *
	 * @return true, if is single precision
	 */
	public boolean isSinglePrecision()
	{
		return splines[0][0][0] instanceof FloatCustomTricubicFunction;
	}

	/**
	 * Convert to single precision.
	 */
	public void toSinglePrecision()
	{
		if (isSinglePrecision())
			return;
		int maxj = getMaxYSplinePosition() + 1;
		int maxk = getMaxZSplinePosition() + 1;
		for (int i = getMaxXSplinePosition() + 1; i-- > 0;)
		{
			for (int j = maxj; j-- > 0;)
			{
				for (int k = maxk; k-- > 0;)
				{
					splines[i][j][k] = splines[i][j][k].toSinglePrecision();
				}
			}
		}
	}

	/**
	 * Convert to double precision.
	 */
	public void toDoublePrecision()
	{
		if (!isSinglePrecision())
			return;
		int maxj = getMaxYSplinePosition() + 1;
		int maxk = getMaxZSplinePosition() + 1;
		for (int i = getMaxXSplinePosition() + 1; i-- > 0;)
		{
			for (int j = maxj; j-- > 0;)
			{
				for (int k = maxk; k-- > 0;)
				{
					splines[i][j][k] = splines[i][j][k].toDoublePrecision();
				}
			}
		}
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
    private double[] computeCoefficients(double[] beta) {
        final int sz = 64;
        final double[] a = new double[sz];

        for (int i = 0; i < sz; i++) {
            double result = 0;
            final double[] row = AINV[i];
            for (int j = 0; j < sz; j++) {
                result += row[j] * beta[j];
            }
            a[i] = result;
        }

        return a;
    }
}
//@formatter:on
