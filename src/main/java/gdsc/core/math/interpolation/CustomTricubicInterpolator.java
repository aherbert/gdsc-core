package gdsc.core.math.interpolation;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

//@formatter:off

/*----------------------------------------------------------------------------- 
 * GDSC Software
 * 
 * This is an extension of the 
 * org.apache.commons.math3.analysis.interpolation.TricubicInterpolator
 * 
 * Modifications have been made to return a CustomTricubicInterpolatingFunction 
 * with additional constraints that the gradients at the bounds are zero. This allows
 * interpolation up to the bounds of the input data. The input data is wrapped in a 
 * value provider to allow interpolation of different data sources.
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

import gdsc.core.data.DoubleArrayTrivalueProvider;
import gdsc.core.data.DoubleArrayValueProvider;
import gdsc.core.data.TrivalueProvider;
import gdsc.core.data.ValueProvider;
import gdsc.core.ij.Utils;
import gdsc.core.logging.Ticker;
import gdsc.core.logging.TrackProgress;
import gdsc.core.utils.TurboList;

/**
 * Generates a tricubic interpolating function.
 */
public class CustomTricubicInterpolator
    implements TrivariateGridInterpolator {
	
	private TrackProgress progress;
	private ExecutorService executorService;
	private long taskSize = 1000;

    /**
     * {@inheritDoc}
     */
    public CustomTricubicInterpolatingFunction interpolate(final double[] xval,
            final double[] yval,
            final double[] zval,
            final double[][][] fval)
    {
    	return interpolate(
    			new DoubleArrayValueProvider(xval), 
    			new DoubleArrayValueProvider(yval), 
    			new DoubleArrayValueProvider(zval), 
    			new DoubleArrayTrivalueProvider(fval));
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
     * {@code fval.get[i][j][k] = f(xval.get(i], yval.get(j], zval.get(k])}.
     * @return a function that interpolates the data set.
     * @throws NoDataException if any of the arrays has zero length.
     * @throws DimensionMismatchException if the array lengths are inconsistent.
     * @throws NonMonotonicSequenceException if arrays are not sorted
     * @throws NumberIsTooSmallException if the number of points is too small for
     * the order of the interpolation
     * 
     * @see org.apache.commons.math3.analysis.interpolation.TrivariateGridInterpolator#interpolate(double[], double[], double[], double[][][])
     */
    public CustomTricubicInterpolatingFunction interpolate(final ValueProvider xval,
                                                           final ValueProvider yval,
                                                           final ValueProvider zval,
                                                           final TrivalueProvider fval)
        throws NoDataException, NumberIsTooSmallException,
               DimensionMismatchException, NonMonotonicSequenceException {
        if (xval.getMaxX() < 2) {
            throw new NumberIsTooSmallException(xval.getMaxX(), 2, true);
        }
        if (yval.getMaxX() < 2) {
            throw new NumberIsTooSmallException(yval.getMaxX(), 2, true);
        }
        if (zval.getMaxX() < 2) {
            throw new NumberIsTooSmallException(zval.getMaxX(), 2, true);
        }
        if (xval.getMaxX() != fval.getMaxX()) {
            throw new DimensionMismatchException(xval.getMaxX(), fval.getMaxX());
        }
        if (yval.getMaxX() != fval.getMaxY()) {
            throw new DimensionMismatchException(yval.getMaxX(), fval.getMaxY());
        }
        if (zval.getMaxX() != fval.getMaxZ()) {
            throw new DimensionMismatchException(zval.getMaxX(), fval.getMaxZ());
        }
        
        CustomTricubicInterpolatingFunction.checkOrder(xval);
        CustomTricubicInterpolatingFunction.checkOrder(yval);
        CustomTricubicInterpolatingFunction.checkOrder(zval);

        final int xLen = xval.getMaxX();
        final int yLen = yval.getMaxX();
        final int zLen = zval.getMaxX();
        
        // Approximation to the partial derivatives using finite differences.
        final double[][][] dFdX = new double[xLen][yLen][zLen];
        final double[][][] dFdY = new double[xLen][yLen][zLen];
        final double[][][] dFdZ = new double[xLen][yLen][zLen];
        final double[][][] d2FdXdY = new double[xLen][yLen][zLen];
        final double[][][] d2FdXdZ = new double[xLen][yLen][zLen];
        final double[][][] d2FdYdZ = new double[xLen][yLen][zLen];
        final double[][][] d3FdXdYdZ = new double[xLen][yLen][zLen];

        final long total = (long)(xLen-2) * (yLen-2) * (zLen-2);
        
        ExecutorService executorService = this.executorService;
		long taskSize = Math.max(1, this.taskSize);
        boolean threaded = executorService != null && taskSize < total;
        
        final Ticker ticker = Ticker.create(progress, total, threaded);
        ticker.start();
        
        if (threaded)
        {
    		final int xLen_2 = xLen - 2;
    		final long xLen_2_yLen_2 = (long)xLen_2 * (yLen-2);
    		
        	// Break this up into reasonable tasks, ensuring we can hold all the futures
        	long nTasks = (long) Math.ceil((double) total / taskSize);
        	while (nTasks >= Integer.MAX_VALUE)
        	{	
        		taskSize *= 2;
            	nTasks = (long) Math.ceil((double) total / taskSize);
            }
    		TurboList<Future<?>> futures = new TurboList<Future<?>>((int)nTasks);
			for (long from = 0; from < total;)
			{
				final long from_ = from;
				final long to = Math.min(from + taskSize, total);
				futures.add(executorService.submit(new Runnable()
				{
					public void run()
					{
				        final double[][][] values = new double[3][3][3];
						for (long index = from_; index < to; index++)
							build(index, xLen_2, xLen_2_yLen_2, 
									xval, yval, zval, fval, 
									dFdX, dFdY, dFdZ, d2FdXdY, 
									d2FdXdZ, d2FdYdZ, d3FdXdYdZ, 
									values, ticker);
					}
				}));
				from = to;
			}
			
			Utils.waitForCompletion(futures);
        }
        else
        {
            final double[][][] values = new double[3][3][3];
        	
            for (int i = 1; i < xLen - 1; i++) {
            	
                final int nI = i + 1;
                final int pI = i - 1;
    
                final double nX = xval.get(nI);
                final double pX = xval.get(pI);
    
                final double deltaX = nX - pX;
    
                for (int j = 1; j < yLen - 1; j++) {
                	
                    final int nJ = j + 1;
                    final int pJ = j - 1;
    
                    final double nY = yval.get(nJ);
                    final double pY = yval.get(pJ);
    
                    final double deltaY = nY - pY;
                    final double deltaXY = deltaX * deltaY;
    
                    for (int k = 1; k < zLen - 1; k++) {
                        final int nK = k + 1;
                        final int pK = k - 1;
    
                        final double nZ = zval.get(nK);
                        final double pZ = zval.get(pK);
    
                        final double deltaZ = nZ - pZ;
                        
                        fval.get(i, j, k, values);
    
                        dFdX[i][j][k] = (values[2][1][1] - values[0][1][1]) / deltaX;
                        dFdY[i][j][k] = (values[1][2][1] - values[1][0][1]) / deltaY;
                        dFdZ[i][j][k] = (values[1][1][2] - values[1][1][0]) / deltaZ;
                          
                        final double deltaXZ = deltaX * deltaZ;
                        final double deltaYZ = deltaY * deltaZ;
                          
                        d2FdXdY[i][j][k] = (values[2][2][1] - values[2][0][1] - values[0][2][1] + values[0][0][1]) / deltaXY;
                        d2FdXdZ[i][j][k] = (values[2][1][2] - values[2][1][0] - values[0][1][2] + values[0][1][0]) / deltaXZ;
                        d2FdYdZ[i][j][k] = (values[1][2][2] - values[1][2][0] - values[1][0][2] + values[1][0][0]) / deltaYZ;
                          
                        final double deltaXYZ = deltaXY * deltaZ;
                          
                        d3FdXdYdZ[i][j][k] = (values[2][2][2] - values[2][0][2] -
                                              values[0][2][2] + values[0][0][2] -
                                              values[2][2][0] + values[2][0][0] +
                                              values[0][2][0] - values[0][0][0]) / deltaXYZ;
                      
                        //dFdX[i][j][k] = (fval.get[nI][j][k] - fval.get[pI][j][k]) / deltaX;
                        //dFdY[i][j][k] = (fval.get[i][nJ][k] - fval.get[i][pJ][k]) / deltaY;
                        //dFdZ[i][j][k] = (fval.get[i][j][nK] - fval.get[i][j][pK]) / deltaZ;
                        //
                        //final double deltaXZ = deltaX * deltaZ;
                        //final double deltaYZ = deltaY * deltaZ;
                        //
                        //d2FdXdY[i][j][k] = (fval.get[nI][nJ][k] - fval.get[nI][pJ][k] - fval.get[pI][nJ][k] + fval.get[pI][pJ][k]) / deltaXY;
                        //d2FdXdZ[i][j][k] = (fval.get[nI][j][nK] - fval.get[nI][j][pK] - fval.get[pI][j][nK] + fval.get[pI][j][pK]) / deltaXZ;
                        //d2FdYdZ[i][j][k] = (fval.get[i][nJ][nK] - fval.get[i][nJ][pK] - fval.get[i][pJ][nK] + fval.get[i][pJ][pK]) / deltaYZ;
                        //
                        //final double deltaXYZ = deltaXY * deltaZ;
                        //
                        //d3FdXdYdZ[i][j][k] = (fval.get[nI][nJ][nK] - fval.get[nI][pJ][nK] -
                        //                      fval.get[pI][nJ][nK] + fval.get[pI][pJ][nK] -
                        //                      fval.get[nI][nJ][pK] + fval.get[nI][pJ][pK] +
                        //                      fval.get[pI][nJ][pK] - fval.get[pI][pJ][pK]) / deltaXYZ;
                        
                    	ticker.tick();
                    }
                }
            }
        }
        
        ticker.stop();

        // Create the interpolating function.
        return new CustomTricubicInterpolatingFunction(xval, yval, zval, fval,
        		new DoubleArrayTrivalueProvider(dFdX),
        		new DoubleArrayTrivalueProvider(dFdY),
        		new DoubleArrayTrivalueProvider(dFdZ),
        		new DoubleArrayTrivalueProvider(d2FdXdY),
        		new DoubleArrayTrivalueProvider(d2FdXdZ),
        		new DoubleArrayTrivalueProvider(d2FdYdZ),
        		new DoubleArrayTrivalueProvider(d3FdXdYdZ),
        		progress, executorService, taskSize);
    }
 	
    private static void build(long index,
    		final int xLen_2,
    		final long xLen_2_yLen_2,
            final ValueProvider xval,
            final ValueProvider yval,
            final ValueProvider zval,
            final TrivalueProvider fval,            
            final double[][][] dFdX,
            final double[][][] dFdY,
            final double[][][] dFdZ,
            final double[][][] d2FdXdY,
            final double[][][] d2FdXdZ,
            final double[][][] d2FdYdZ,
            final double[][][] d3FdXdYdZ,
            final double[][][] values,
            final Ticker ticker
    		)
    {
    	// Convert position to the indices
    	// Add 1 since the packing into the index is for the (lengths-2)
		int k = 1 + (int) (index / xLen_2_yLen_2);
		long mod = index % xLen_2_yLen_2;
		int j = 1 + (int) (mod / xLen_2);
		int i = 1 + (int) (mod % xLen_2);
		
		//System.out.printf("%d => [%d][%d][%d]\n", index, i, j, k);
    	
        final int nI = i + 1;
        final int pI = i - 1;

        final double nX = xval.get(nI);
        final double pX = xval.get(pI);

        final double deltaX = nX - pX;
    	
        final int nJ = j + 1;
        final int pJ = j - 1;

        final double nY = yval.get(nJ);
        final double pY = yval.get(pJ);

        final double deltaY = nY - pY;
        final double deltaXY = deltaX * deltaY;

        final int nK = k + 1;
        final int pK = k - 1;
        
        final double nZ = zval.get(nK);
        final double pZ = zval.get(pK);

        final double deltaZ = nZ - pZ;
        
        fval.get(i, j, k, values);

        dFdX[i][j][k] = (values[2][1][1] - values[0][1][1]) / deltaX;
        dFdY[i][j][k] = (values[1][2][1] - values[1][0][1]) / deltaY;
        dFdZ[i][j][k] = (values[1][1][2] - values[1][1][0]) / deltaZ;
          
        final double deltaXZ = deltaX * deltaZ;
        final double deltaYZ = deltaY * deltaZ;
          
        d2FdXdY[i][j][k] = (values[2][2][1] - values[2][0][1] - values[0][2][1] + values[0][0][1]) / deltaXY;
        d2FdXdZ[i][j][k] = (values[2][1][2] - values[2][1][0] - values[0][1][2] + values[0][1][0]) / deltaXZ;
        d2FdYdZ[i][j][k] = (values[1][2][2] - values[1][2][0] - values[1][0][2] + values[1][0][0]) / deltaYZ;
          
        final double deltaXYZ = deltaXY * deltaZ;
          
        d3FdXdYdZ[i][j][k] = (values[2][2][2] - values[2][0][2] -
                              values[0][2][2] + values[0][0][2] -
                              values[2][2][0] + values[2][0][0] +
                              values[0][2][0] - values[0][0][0]) / deltaXYZ;
        
    	ticker.tick();    	
    }

	/**
	 * Sets the progress tracker.
	 *
	 * @param progress the new progress tracker
	 */
	public void setProgress(TrackProgress progress)
	{
		this.progress = progress;
	}
	
	/**
	 * Sets the executor service for interpolating.
	 *
	 * @param executorService the new executor service
	 */
	public void setExecutorService(ExecutorService executorService)
	{
		this.executorService = executorService;
	}

	/**
	 * Gets the task size for multi-threaded interpolation.
	 *
	 * @return the task size
	 */
	public long getTaskSize()
	{
		return taskSize;
	}

	/**
	 * Sets the task size for multi-threaded interpolation. If the number of interpolation 
	 * nodes is less than this then multi-threading is not used.
	 *
	 * @param taskSize the new task size
	 */
	public void setTaskSize(long taskSize)
	{
		this.taskSize = taskSize;
	}
}
