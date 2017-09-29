package gdsc.core.math.interpolation;

//@formatter:off

/*----------------------------------------------------------------------------- 
 * GDSC Software
 * 
 * This is an extension of the 
 * org.apache.commons.math3.analysis.interpolation.TricubicInterpolator
 * 
 * Modifications have been made to return a CustomTricubicInterpolatingFunction 
 * with additional constraints that the gradients at the bounds are zero. This allow
 * interpolation up to the bounds of the input data. 
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
import org.apache.commons.math3.analysis.interpolation.TrivariateGridInterpolator;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.NoDataException;
import org.apache.commons.math3.exception.NonMonotonicSequenceException;
import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.util.MathArrays;

import gdsc.core.logging.NullTrackProgress;
import gdsc.core.logging.TrackProgress;

/**
 * Generates a tricubic interpolating function.
 */
public class CustomTricubicInterpolator
    implements TrivariateGridInterpolator {
	
    /**
     * {@inheritDoc}
     */
    public CustomTricubicInterpolatingFunction interpolate(final double[] xval,
            final double[] yval,
            final double[] zval,
            final double[][][] fval)
    {
    	return interpolate(xval, yval, zval, fval, NullTrackProgress.INSTANCE);
    }
    
    /**
     * Compute an interpolating function for the dataset.
     *
     * @param xval All the x-coordinates of the interpolation points, sorted
     * in increasing order.
     * @param yval All the y-coordinates of the interpolation points, sorted
     * in increasing order.
     * @param zval All the z-coordinates of the interpolation points, sorted
     * in increasing order.
     * @param fval the values of the interpolation points on all the grid knots:
     * {@code fval[i][j][k] = f(xval[i], yval[j], zval[k])}.
     * @return a function that interpolates the data set.
     * @throws NoDataException if any of the arrays has zero length.
     * @throws DimensionMismatchException if the array lengths are inconsistent.
     * @throws NonMonotonicSequenceException if arrays are not sorted
     * @throws NumberIsTooSmallException if the number of points is too small for
     * the order of the interpolation
     * 
     * @see org.apache.commons.math3.analysis.interpolation.TrivariateGridInterpolator#interpolate(double[], double[], double[], double[][][])
     */
    public CustomTricubicInterpolatingFunction interpolate(final double[] xval,
                                                           final double[] yval,
                                                           final double[] zval,
                                                           final double[][][] fval,
                                                           TrackProgress progress)
        throws NoDataException, NumberIsTooSmallException,
               DimensionMismatchException, NonMonotonicSequenceException {
        if (fval.length == 0) {
            throw new NoDataException();
        }
        if (xval.length < 2) {
            throw new NumberIsTooSmallException(xval.length, 2, true);
        }
        if (yval.length < 2) {
            throw new NumberIsTooSmallException(yval.length, 2, true);
        }
        if (zval.length < 2) {
            throw new NumberIsTooSmallException(zval.length, 2, true);
        }
        if (xval.length != fval.length) {
            throw new DimensionMismatchException(xval.length, fval.length);
        }
        
        MathArrays.checkOrder(xval);
        MathArrays.checkOrder(yval);
        MathArrays.checkOrder(zval);

        final int xLen = xval.length;
        final int yLen = yval.length;
        final int zLen = zval.length;

        // Check dimensions here
        for (int i = 0; i < xLen; i++) {
            if (yLen != fval[i].length) {
                throw new DimensionMismatchException(yval.length, fval[i].length);
            }

            for (int j = 0;j < yLen; j++) {
                if (zLen != fval[i][j].length) {
                    throw new DimensionMismatchException(zval.length, fval[i][j].length);
                }
            }
        }
        
        // Approximation to the partial derivatives using finite differences.
        final double[][][] dFdX = new double[xLen][yLen][zLen];
        final double[][][] dFdY = new double[xLen][yLen][zLen];
        final double[][][] dFdZ = new double[xLen][yLen][zLen];
        final double[][][] d2FdXdY = new double[xLen][yLen][zLen];
        final double[][][] d2FdXdZ = new double[xLen][yLen][zLen];
        final double[][][] d2FdYdZ = new double[xLen][yLen][zLen];
        final double[][][] d3FdXdYdZ = new double[xLen][yLen][zLen];

        final long total = (xLen-2) * (yLen-2) * (zLen-2);
        long current = 0;
        
        for (int i = 1; i < xLen - 1; i++) {
        	
            final int nI = i + 1;
            final int pI = i - 1;

            final double nX = xval[nI];
            final double pX = xval[pI];

            final double deltaX = nX - pX;

            for (int j = 1; j < yLen - 1; j++) {
            	
                final int nJ = j + 1;
                final int pJ = j - 1;

                final double nY = yval[nJ];
                final double pY = yval[pJ];

                final double deltaY = nY - pY;
                final double deltaXY = deltaX * deltaY;

                for (int k = 1; k < zLen - 1; k++) {
                	progress.progress(current++, total);
                    final int nK = k + 1;
                    final int pK = k - 1;

                    final double nZ = zval[nK];
                    final double pZ = zval[pK];

                    final double deltaZ = nZ - pZ;

                    dFdX[i][j][k] = (fval[nI][j][k] - fval[pI][j][k]) / deltaX;
                    dFdY[i][j][k] = (fval[i][nJ][k] - fval[i][pJ][k]) / deltaY;
                    dFdZ[i][j][k] = (fval[i][j][nK] - fval[i][j][pK]) / deltaZ;

                    final double deltaXZ = deltaX * deltaZ;
                    final double deltaYZ = deltaY * deltaZ;

                    d2FdXdY[i][j][k] = (fval[nI][nJ][k] - fval[nI][pJ][k] - fval[pI][nJ][k] + fval[pI][pJ][k]) / deltaXY;
                    d2FdXdZ[i][j][k] = (fval[nI][j][nK] - fval[nI][j][pK] - fval[pI][j][nK] + fval[pI][j][pK]) / deltaXZ;
                    d2FdYdZ[i][j][k] = (fval[i][nJ][nK] - fval[i][nJ][pK] - fval[i][pJ][nK] + fval[i][pJ][pK]) / deltaYZ;

                    final double deltaXYZ = deltaXY * deltaZ;

                    d3FdXdYdZ[i][j][k] = (fval[nI][nJ][nK] - fval[nI][pJ][nK] -
                                          fval[pI][nJ][nK] + fval[pI][pJ][nK] -
                                          fval[nI][nJ][pK] + fval[nI][pJ][pK] +
                                          fval[pI][nJ][pK] - fval[pI][pJ][pK]) / deltaXYZ;
                }
            }
        }
        
        progress.progress(1);

        // Create the interpolating function.
        return new CustomTricubicInterpolatingFunction(xval, yval, zval, fval,
                                                       dFdX, dFdY, dFdZ,
                                                       d2FdXdY, d2FdXdZ, d2FdYdZ,
                                                       d3FdXdYdZ, progress);
    }
}
