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
import gdsc.core.data.procedures.TrivalueProcedure;
import gdsc.core.ij.Utils;
import gdsc.core.logging.Ticker;
import gdsc.core.logging.TrackProgress;
import gdsc.core.utils.SimpleArrayUtils;
import gdsc.core.utils.TurboList;

/**
 * Generates a tricubic interpolating function.
 */
public class CustomTricubicInterpolator
    implements TrivariateGridInterpolator {
	
	private TrackProgress progress;
	private ExecutorService executorService;
	private long taskSize = 1000;
	private boolean singlePrecision;

	/**
	 * A builder for the CustomTricubicInterpolator.
	 */
	public static class Builder
	{
		ValueProvider xval;
        ValueProvider yval;
        ValueProvider zval;
        TrivalueProvider fval;		
    	TrackProgress progress;
    	ExecutorService executorService;
    	long taskSize;
    	boolean integerAxisValues;
    	boolean singlePrecision;
    	
    	/**
	     * Sets the X value.
	     *
	     * @param x the x value
	     * @return the builder
	     */
	    public Builder setXValue(double[] x)
    	{
    		xval = new DoubleArrayValueProvider(x);
    		return this;
    	}
	    
    	/**
	     * Sets the X value.
	     *
	     * @param x the x value
	     * @return the builder
	     */
	    public Builder setXValue(ValueProvider x)
    	{
    		xval = x;
    		return this;
    	}
	    
    	/**
	     * Sets the Y value.
	     *
	     * @param y the y value
	     * @return the builder
	     */
	    public Builder setYValue(double[] y)
    	{
    		yval = new DoubleArrayValueProvider(y);
    		return this;
    	}
	    
    	/**
	     * Sets the Y value.
	     *
	     * @param y the y value
	     * @return the builder
	     */
	    public Builder setYValue(ValueProvider y)
    	{
    		yval = y;
    		return this;
    	}
	    
    	/**
	     * Sets the Z value.
	     *
	     * @param z the z value
	     * @return the builder
	     */
	    public Builder setZValue(double[] z)
    	{
    		zval = new DoubleArrayValueProvider(z);
    		return this;
    	}
	    
    	/**
	     * Sets the Z value.
	     *
	     * @param z the z value
	     * @return the builder
	     */
	    public Builder setZValue(ValueProvider z)
    	{
    		zval = z;
    		return this;
    	}

    	/**
	     * Sets the function value.
	     *
	     * @param z the function value
	     * @return the builder
	     */
	    public Builder setFValue(double[][][] f)
    	{
    		fval = new DoubleArrayTrivalueProvider(f);
    		return this;
    	}
	    
    	/**
	     * Sets the function value.
	     *
	     * @param z the function value
	     * @return the builder
	     */
	    public Builder setFValue(TrivalueProvider f)
    	{
    		fval = f;
    		return this;
    	}
	    
    	/**
    	 * Sets the progress tracker.
    	 *
    	 * @param progress the new progress tracker
	     * @return the builder
    	 */
    	public Builder setProgress(TrackProgress progress)
    	{
    		this.progress = progress;
    		return this;
    	}
    	
    	/**
    	 * Sets the executor service for interpolating.
    	 *
    	 * @param executorService the new executor service
	     * @return the builder
    	 */
    	public Builder setExecutorService(ExecutorService executorService)
    	{
    		this.executorService = executorService;
    		return this;
    	}
    	
    	/**
	     * Set to true to create integer axis values stating from zero.
	     *
	     * @param integerAxisValues the integer axis values flag
	     * @return the builder
	     */
	    public Builder setIntegerAxisValues(boolean integerAxisValues)
    	{
    		this.integerAxisValues = integerAxisValues;
    		return this;
    	}

    	/**
    	 * Sets the task size for multi-threaded interpolation. If the number of interpolation 
    	 * nodes is less than this then multi-threading is not used.
    	 *
    	 * @param taskSize the new task size
	     * @return the builder
    	 */
    	public Builder setTaskSize(long taskSize)
    	{
    		this.taskSize = taskSize;
    		return this;
    	}

		/**
		 * Sets the single precision.
		 *
		 * @param singlePrecision the single precision
		 * @return the builder
		 */
		public Builder setSinglePrecision(boolean singlePrecision)
		{
			this.singlePrecision = singlePrecision;
			return this;
		}  	
    	
        /**
         * Compute an interpolating function for the dataset.
         *
         * @return the custom tricubic interpolating function
         */
        public CustomTricubicInterpolatingFunction interpolate()
        {
        	CustomTricubicInterpolator i = build();
        	if (integerAxisValues)
        	{
        		setXValue(SimpleArrayUtils.newArray(fval.getLengthX(), 0, 1.0));
        		setYValue(SimpleArrayUtils.newArray(fval.getLengthY(), 0, 1.0));
        		setZValue(SimpleArrayUtils.newArray(fval.getLengthZ(), 0, 1.0));
        	}
        	return i.interpolate(xval, yval, zval, fval);
        }

		/**
		 * Builds the custom tricubic interpolator.
		 *
		 * @return the custom tricubic interpolator
		 */
		public CustomTricubicInterpolator build()
		{
        	CustomTricubicInterpolator i = new CustomTricubicInterpolator();
        	i.setProgress(progress);
        	if (taskSize > 0)
        		i.setTaskSize(taskSize);
        	i.setExecutorService(executorService);
        	i.setSinglePrecision(singlePrecision);
			return i;
		}
	}
	
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
        throws NumberIsTooSmallException,
               DimensionMismatchException, NonMonotonicSequenceException {
        if (xval.getLength() < 2) {
            throw new NumberIsTooSmallException(xval.getLength(), 2, true);
        }
        if (yval.getLength() < 2) {
            throw new NumberIsTooSmallException(yval.getLength(), 2, true);
        }
        if (zval.getLength() < 2) {
            throw new NumberIsTooSmallException(zval.getLength(), 2, true);
        }
        if (xval.getLength() != fval.getLengthX()) {
            throw new DimensionMismatchException(xval.getLength(), fval.getLengthX());
        }
        if (yval.getLength() != fval.getLengthY()) {
            throw new DimensionMismatchException(yval.getLength(), fval.getLengthY());
        }
        if (zval.getLength() != fval.getLengthZ()) {
            throw new DimensionMismatchException(zval.getLength(), fval.getLengthZ());
        }
        
        CustomTricubicInterpolatingFunction.checkOrder(xval);
        CustomTricubicInterpolatingFunction.checkOrder(yval);
        CustomTricubicInterpolatingFunction.checkOrder(zval);

        final int xLen = xval.getLength();
        final int yLen = yval.getLength();
        final int zLen = zval.getLength();
		final int xLen_1 = xLen - 1;
		final int yLen_1 = yLen - 1;
		final int zLen_1 = zLen - 1;
        
        // Approximation to the partial derivatives using finite differences.
        final double[][][] dFdX = new double[xLen][yLen][zLen];
        final double[][][] dFdY = new double[xLen][yLen][zLen];
        final double[][][] dFdZ = new double[xLen][yLen][zLen];
        final double[][][] d2FdXdY = new double[xLen][yLen][zLen];
        final double[][][] d2FdXdZ = new double[xLen][yLen][zLen];
        final double[][][] d2FdYdZ = new double[xLen][yLen][zLen];
        final double[][][] d3FdXdYdZ = new double[xLen][yLen][zLen];

        final long total = (long)xLen * yLen * zLen;
        
        ExecutorService executorService = this.executorService;
		long taskSize = Math.max(1, this.taskSize);
        boolean threaded = executorService != null && taskSize < total;
        
        final Ticker ticker = Ticker.create(progress, total, threaded);
        ticker.start();
        
        if (threaded)
        {
    		final long xLen_yLen = (long)xLen * yLen;
    		
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
							build(index, xLen, xLen_yLen, 
								  xLen_1, yLen_1, zLen_1,
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

            // Updated to handle edges
            
            for (int i = 0; i < xLen; i++) {
            	
    			boolean edgeX = i == 0 || i == xLen_1;
                final int nI, pI;
                final double deltaX;
                if (edgeX)
            	{
                	// Ignored
            		nI = pI = 0;
            		deltaX = 0;
            	}
            	else
            	{
            		nI = i + 1;
            		pI = i - 1;
            		deltaX = xval.get(nI) - xval.get(pI);
            	}
    
                for (int j = 0; j < yLen; j++) {
                	
        			boolean edgeY = j == 0 || j == yLen_1;
                    final int nJ, pJ;
                    final double deltaY;
                    if (edgeY)
                	{
                    	// Ignored
                		nJ = pJ = 0;
                    	deltaY = 0;
                	}
                	else
                	{
                		nJ = j + 1;
                		pJ = j - 1;
                		deltaY = yval.get(nJ) - yval.get(pJ);
                	}

                    final boolean edgeXY = edgeX || edgeY;
                    final double deltaXY = deltaX * deltaY;
    
                    for (int k = 0; k < zLen; k++) {

            			boolean edgeZ = k == 0 || k == zLen_1;
                        final int nK, pK;
                        final double deltaZ;
                        if (edgeZ)
                    	{
                        	// Ignored
                    		nK = pK = 0;
                    		deltaZ = 0;
                    	}
                    	else
                    	{
                    		nK = k + 1;
                    		pK = k - 1;
                    		deltaZ = zval.get(nK) - zval.get(pK);
                    	}
                        
                        final double deltaXZ = deltaX * deltaZ;
                        final double deltaYZ = deltaY * deltaZ;
                        final double deltaXYZ = deltaXY * deltaZ;
                        
                        if (edgeXY || edgeZ)
                        {
                            // No gradients at the edge
                            dFdX[i][j][k] = (edgeX) ? 0 : (fval.get(nI,j,k) - fval.get(pI,j,k)) / deltaX;
                            dFdY[i][j][k] = (edgeY) ? 0 : (fval.get(i,nJ,k) - fval.get(i,pJ,k)) / deltaY;
                            dFdZ[i][j][k] = (edgeZ) ? 0 : (fval.get(i,j,nK) - fval.get(i,j,pK)) / deltaZ;
                            
                            d2FdXdY[i][j][k] = (edgeXY) ? 0 : (fval.get(nI,nJ,k) - fval.get(nI,pJ,k) - fval.get(pI,nJ,k) + fval.get(pI,pJ,k)) / deltaXY;
                            d2FdXdZ[i][j][k] = (edgeX||edgeZ) ? 0 : (fval.get(nI,j,nK) - fval.get(nI,j,pK) - fval.get(pI,j,nK) + fval.get(pI,j,pK)) / deltaXZ;
                            d2FdYdZ[i][j][k] = (edgeY||edgeZ) ? 0 : (fval.get(i,nJ,nK) - fval.get(i,nJ,pK) - fval.get(i,pJ,nK) + fval.get(i,pJ,pK)) / deltaYZ;
                        }
                        else
                        {
                            fval.get(i, j, k, values);
        
                            dFdX[i][j][k] = (values[2][1][1] - values[0][1][1]) / deltaX;
                            dFdY[i][j][k] = (values[1][2][1] - values[1][0][1]) / deltaY;
                            dFdZ[i][j][k] = (values[1][1][2] - values[1][1][0]) / deltaZ;
                              
                            d2FdXdY[i][j][k] = (values[2][2][1] - values[2][0][1] - values[0][2][1] + values[0][0][1]) / deltaXY;
                            d2FdXdZ[i][j][k] = (values[2][1][2] - values[2][1][0] - values[0][1][2] + values[0][1][0]) / deltaXZ;
                            d2FdYdZ[i][j][k] = (values[1][2][2] - values[1][2][0] - values[1][0][2] + values[1][0][0]) / deltaYZ;
                              
                            d3FdXdYdZ[i][j][k] = (values[2][2][2] - values[2][0][2] -
                                                  values[0][2][2] + values[0][0][2] -
                                                  values[2][2][0] + values[2][0][0] +
                                                  values[0][2][0] - values[0][0][0]) / deltaXYZ;
                        }
                        
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
        		progress, executorService, taskSize, singlePrecision);
    }
 	
    private static void build(long index,
    		final int xLen,
    		final long xLen_yLen,
    		final int xLen_1,
    		final int yLen_1,
    		final int zLen_1,
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
		int k = (int) (index / xLen_yLen);
		long mod = index % xLen_yLen;
		int j = (int) (mod / xLen);
		int i = (int) (mod % xLen);
		
		//System.out.printf("%d => [%d][%d][%d]\n", index, i, j, k);
		boolean edgeX = i == 0 || i == xLen_1;
        final int nI, pI;
        final double deltaX;
        if (edgeX)
    	{
        	// Ignored
    		nI = pI = 0;
    		deltaX = 0;
    	}
    	else
    	{
    		nI = i + 1;
    		pI = i - 1;
    		deltaX = xval.get(nI) - xval.get(pI);
    	}
    	
		boolean edgeY = j == 0 || j == yLen_1;
        final int nJ, pJ;
        final double deltaY;
        if (edgeY)
    	{
        	// Ignored
    		nJ = pJ = 0;
        	deltaY = 0;
    	}
    	else
    	{
    		nJ = j + 1;
    		pJ = j - 1;
    		deltaY = yval.get(nJ) - yval.get(pJ);
    	}

        final double deltaXY = deltaX * deltaY;
        
		boolean edgeZ = k == 0 || k == zLen_1;
        final int nK, pK;
        final double deltaZ;
        if (edgeZ)
    	{
        	// Ignored
    		nK = pK = 0;
    		deltaZ = 0;
    	}
    	else
    	{
    		nK = k + 1;
    		pK = k - 1;
    		deltaZ = zval.get(nK) - zval.get(pK);
    	}
        
        final double deltaXZ = deltaX * deltaZ;
        final double deltaYZ = deltaY * deltaZ;
        final double deltaXYZ = deltaXY * deltaZ; 
        
        if (edgeX || edgeY || edgeZ)
        {
            // No gradients at the edge
            dFdX[i][j][k] = (edgeX) ? 0 : (fval.get(nI,j,k) - fval.get(pI,j,k)) / deltaX;
            dFdY[i][j][k] = (edgeY) ? 0 : (fval.get(i,nJ,k) - fval.get(i,pJ,k)) / deltaY;
            dFdZ[i][j][k] = (edgeZ) ? 0 : (fval.get(i,j,nK) - fval.get(i,j,pK)) / deltaZ;
            
            d2FdXdY[i][j][k] = (edgeX||edgeY) ? 0 : (fval.get(nI,nJ,k) - fval.get(nI,pJ,k) - fval.get(pI,nJ,k) + fval.get(pI,pJ,k)) / deltaXY;
            d2FdXdZ[i][j][k] = (edgeX||edgeZ) ? 0 : (fval.get(nI,j,nK) - fval.get(nI,j,pK) - fval.get(pI,j,nK) + fval.get(pI,j,pK)) / deltaXZ;
            d2FdYdZ[i][j][k] = (edgeY||edgeZ) ? 0 : (fval.get(i,nJ,nK) - fval.get(i,nJ,pK) - fval.get(i,pJ,nK) + fval.get(i,pJ,pK)) / deltaYZ;
        }
        else
        {
            fval.get(i, j, k, values);

            dFdX[i][j][k] = (values[2][1][1] - values[0][1][1]) / deltaX;
            dFdY[i][j][k] = (values[1][2][1] - values[1][0][1]) / deltaY;
            dFdZ[i][j][k] = (values[1][1][2] - values[1][1][0]) / deltaZ;
              
            d2FdXdY[i][j][k] = (values[2][2][1] - values[2][0][1] - values[0][2][1] + values[0][0][1]) / deltaXY;
            d2FdXdZ[i][j][k] = (values[2][1][2] - values[2][1][0] - values[0][1][2] + values[0][1][0]) / deltaXZ;
            d2FdYdZ[i][j][k] = (values[1][2][2] - values[1][2][0] - values[1][0][2] + values[1][0][0]) / deltaYZ;
              
            d3FdXdYdZ[i][j][k] = (values[2][2][2] - values[2][0][2] -
                                  values[0][2][2] + values[0][0][2] -
                                  values[2][2][0] + values[2][0][0] +
                                  values[0][2][0] - values[0][0][0]) / deltaXYZ;
        }
        
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

	/**
	 * Checks if is single precision.
	 *
	 * @return true, if is single precision
	 */
	public boolean isSinglePrecision()
	{
		return singlePrecision;
	}

	/**
	 * Sets the single precision flag. A single precision tricubic function will require half the memory.
	 *
	 * @param singlePrecision the new single precision flag
	 */
	public void setSinglePrecision(boolean singlePrecision)
	{
		this.singlePrecision = singlePrecision;
	}
	
    /**
     * Compute an interpolating function for the data. Creates a single tricubic function for 
     * interpolation between 0 and 1 assuming that the input value is a 4x4x4 cube of values 
     * representing the value at [-1,0,1,2] for each axis.
     *
     * @param fval the values of the interpolation points on all the grid knots
     * @return a tricubic function that interpolates the data.
     * @throws DimensionMismatchException if the array lengths are inconsistent.
     */
    public static CustomTricubicFunction create(final TrivalueProvider fval)
        throws NoDataException, NumberIsTooSmallException,
               DimensionMismatchException, NonMonotonicSequenceException {
        if (4 != fval.getLengthX()) {
            throw new DimensionMismatchException(4, fval.getLengthX());
        }
        if (4 != fval.getLengthY()) {
            throw new DimensionMismatchException(4, fval.getLengthY());
        }
        if (4 != fval.getLengthZ()) {
            throw new DimensionMismatchException(4, fval.getLengthZ());
        }
        
        // Approximation to the partial derivatives using finite differences.
        final double[][][] f = new double[2][2][2];
        final double[][][] dFdX = new double[2][2][2];
        final double[][][] dFdY = new double[2][2][2];
        final double[][][] dFdZ = new double[2][2][2];
        final double[][][] d2FdXdY = new double[2][2][2];
        final double[][][] d2FdXdZ = new double[2][2][2];
        final double[][][] d2FdYdZ = new double[2][2][2];
        final double[][][] d3FdXdYdZ = new double[2][2][2];

        final double[][][] values = new double[3][3][3];
    	
        for (int i = 0; i < 2; i++) {
        	
            for (int j = 0; j < 2; j++) {

                for (int k = 0; k < 2; k++) {
                    
                    fval.get(i+1, j+1, k+1, values);

                    f[i][j][k] = values[1][1][1];
                    
                    dFdX[i][j][k] = (values[2][1][1] - values[0][1][1]) / 2;
                    dFdY[i][j][k] = (values[1][2][1] - values[1][0][1]) / 2;
                    dFdZ[i][j][k] = (values[1][1][2] - values[1][1][0]) / 2;
                      
                    d2FdXdY[i][j][k] = (values[2][2][1] - values[2][0][1] - values[0][2][1] + values[0][0][1]) / 4;
                    d2FdXdZ[i][j][k] = (values[2][1][2] - values[2][1][0] - values[0][1][2] + values[0][1][0]) / 4;
                    d2FdYdZ[i][j][k] = (values[1][2][2] - values[1][2][0] - values[1][0][2] + values[1][0][0]) / 4;
                      
                    d3FdXdYdZ[i][j][k] = (values[2][2][2] - values[2][0][2] -
                                          values[0][2][2] + values[0][0][2] -
                                          values[2][2][0] + values[2][0][0] +
                                          values[0][2][0] - values[0][0][0]) / 8;
                }
            }
        }

        // Create the interpolating function.
        return CustomTricubicInterpolatingFunction.createFunction(
        		new double[64],
        		new DoubleArrayTrivalueProvider(f),
        		new DoubleArrayTrivalueProvider(dFdX),
        		new DoubleArrayTrivalueProvider(dFdY),
        		new DoubleArrayTrivalueProvider(dFdZ),
        		new DoubleArrayTrivalueProvider(d2FdXdY),
        		new DoubleArrayTrivalueProvider(d2FdXdZ),
        		new DoubleArrayTrivalueProvider(d2FdYdZ),
        		new DoubleArrayTrivalueProvider(d3FdXdYdZ));
    }
    
	
    /**
     * Compute an interpolating function for the data. Creates a single tricubic function for 
     * interpolation between [1] and [2] assuming that the input value is a 4x4x4 cube of values 
     * representing the value at [0,1,2,3] for each axis.
	 * <p>
	 * To use the function to create an interpolated value in the range [1] and [2]:
	 * 
	 * <pre>
	 * double x1 = xval.get(1);
	 * double y1 = yval.get(1);
	 * double z1 = zval.get(1);
	 * double x2 = xval.get(2);
	 * double y2 = yval.get(2);
	 * double z2 = zval.get(2);
	 * double xscale = x2 - x1;
	 * double yscale = y2 - y1
	 * double zscale = z2 - y2
	 * // x>=x1 && x<=x2 && y>=y1 && y<=y2 && z>=z1 && z<=z2 
	 * double value = f.value((x-x1) / xscale, (y-y1) / yscale, (z-z1) / zscale);
	 * </pre>
     *
     * @param xval All the x-coordinates of the interpolation points, sorted
     * in increasing order.
     * @param yval All the y-coordinates of the interpolation points, sorted
     * in increasing order.
     * @param zval All the z-coordinates of the interpolation points, sorted
     * in increasing order.
     * @param fval the values of the interpolation points on all the grid knots
     * @return a tricubic function that interpolates the data.
     * @throws DimensionMismatchException if the array lengths are inconsistent.
     */
    public static CustomTricubicFunction create(final ValueProvider xval,
            final ValueProvider yval,
            final ValueProvider zval,
            final TrivalueProvider fval)
        throws NumberIsTooSmallException,
               DimensionMismatchException, NonMonotonicSequenceException {
        if (4 != fval.getLengthX()) {
            throw new DimensionMismatchException(4, fval.getLengthX());
        }
        if (4 != fval.getLengthY()) {
            throw new DimensionMismatchException(4, fval.getLengthY());
        }
        if (4 != fval.getLengthZ()) {
            throw new DimensionMismatchException(4, fval.getLengthZ());
        }
        if (xval.getLength() != fval.getLengthX()) {
            throw new DimensionMismatchException(xval.getLength(), fval.getLengthX());
        }
        if (yval.getLength() != fval.getLengthY()) {
            throw new DimensionMismatchException(yval.getLength(), fval.getLengthY());
        }
        if (zval.getLength() != fval.getLengthZ()) {
            throw new DimensionMismatchException(zval.getLength(), fval.getLengthZ());
        }
        
        // Approximation to the partial derivatives using finite differences.
        final double[][][] f = new double[2][2][2];
        final double[][][] dFdX = new double[2][2][2];
        final double[][][] dFdY = new double[2][2][2];
        final double[][][] dFdZ = new double[2][2][2];
        final double[][][] d2FdXdY = new double[2][2][2];
        final double[][][] d2FdXdZ = new double[2][2][2];
        final double[][][] d2FdYdZ = new double[2][2][2];
        final double[][][] d3FdXdYdZ = new double[2][2][2];

        final double[][][] values = new double[3][3][3];
    	
        for (int i = 0; i < 2; i++) {
        	
            final double nX = xval.get(i+2);
            final double pX = xval.get(i);

            final double deltaX = nX - pX;
        	
            for (int j = 0; j < 2; j++) {

                final double nY = yval.get(j+2);
                final double pY = yval.get(j);

                final double deltaY = nY - pY;
                final double deltaXY = deltaX * deltaY;
            	
                for (int k = 0; k < 2; k++) {
                    
                    final double nZ = zval.get(k+2);
                    final double pZ = zval.get(k);

                    final double deltaZ = nZ - pZ;
                	
                    fval.get(i+1, j+1, k+1, values);
                    
                    f[i][j][k] = values[1][1][1]; 

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
                }
            }
        }

        // Create the interpolating function.
        return CustomTricubicInterpolatingFunction.createFunction(
        		new double[64],
        		xval.get(2) - xval.get(1),
        		yval.get(2) - yval.get(1),
        		zval.get(2) - zval.get(1),
        		new DoubleArrayTrivalueProvider(f),
        		new DoubleArrayTrivalueProvider(dFdX),
        		new DoubleArrayTrivalueProvider(dFdY),
        		new DoubleArrayTrivalueProvider(dFdZ),
        		new DoubleArrayTrivalueProvider(d2FdXdY),
        		new DoubleArrayTrivalueProvider(d2FdXdZ),
        		new DoubleArrayTrivalueProvider(d2FdYdZ),
        		new DoubleArrayTrivalueProvider(d3FdXdYdZ));
    }	    

	
    /**
     * Compute an interpolating function for the data. Creates a single tricubic function for 
     * interpolation between 0 and 1 assuming that the input value contains a 4x4x4 cube of values 
     * representing the value at [-1,0,1,2] for each axis. If no value is available at the ends of 
     * the range then the gradients are set to zero. This allows interpolation of data with a minimum 
     * length of 2 in each dimension.
     * <p>
     * The 4x4x4 cube is extracted from the input value at the given offset for [0,0,0].
     *
     * @param fval the values of the interpolation points on all the grid knots
     * @param x the x-offset in the values
     * @param y the y-offset in the values
     * @param z the z-offset in the values
     * @return a tricubic function that interpolates the data.
     * @throws NumberIsTooSmallException if the number of points is too small for
     * the order of the interpolation
     * @throws IllegalArgumentException If the offset is not positive
     */
    public static CustomTricubicFunction create(final TrivalueProvider fval, int x, int y, int z)
        throws NumberIsTooSmallException, IllegalArgumentException {
    	if (x<0 || y<0 || z<0)
    		throw new IllegalArgumentException("Offset must be positive");
		if (fval.getLengthX() - x < 2)
			throw new NumberIsTooSmallException(fval.getLengthX(), x + 2, true);
		if (fval.getLengthY() - y < 2)
			throw new NumberIsTooSmallException(fval.getLengthY(), y + 2, true);
		if (fval.getLengthZ() - z < 2)
			throw new NumberIsTooSmallException(fval.getLengthZ(), z + 2, true);
		
		final int xLen_1 = fval.getLengthX() - 1;
		final int yLen_1 = fval.getLengthY() - 1;
		final int zLen_1 = fval.getLengthZ() - 1;
       
        // Approximation to the partial derivatives using finite differences.
        final double[][][] f = new double[2][2][2];
        final double[][][] dFdX = new double[2][2][2];
        final double[][][] dFdY = new double[2][2][2];
        final double[][][] dFdZ = new double[2][2][2];
        final double[][][] d2FdXdY = new double[2][2][2];
        final double[][][] d2FdXdZ = new double[2][2][2];
        final double[][][] d2FdYdZ = new double[2][2][2];
        final double[][][] d3FdXdYdZ = new double[2][2][2];

        final double[][][] values = new double[3][3][3];
    	
        for (int i = 0; i < 2; i++) {
        	
        	int xx = x + i;
			boolean edgeX = xx == 0 || xx == xLen_1;
			
            for (int j = 0; j < 2; j++) {
            	
            	int yy = y + j;
				boolean edgeXY = edgeX || yy == 0 || yy == yLen_1;
				
                for (int k = 0; k < 2; k++) {
                	
                	int zz = z + k;
                	
    				if (edgeXY || zz == 0 || zz == zLen_1)
    				{
    					// No gradients at the edge
                        f[i][j][k] = fval.get(xx, yy, zz);
                        
                        // Already zero
                        //dFdX[i][j][k] = 0;
                        //dFdY[i][j][k] = 0;
                        //dFdZ[i][j][k] = 0;
                        //  
                        //d2FdXdY[i][j][k] = 0;
                        //d2FdXdZ[i][j][k] = 0;
                        //d2FdYdZ[i][j][k] = 0;
                        //  
                        //d3FdXdYdZ[i][j][k] = 0;
    				}
    				else
    				{
                        fval.get(xx, yy, zz, values);
    
                        f[i][j][k] = values[1][1][1];
                        
                        dFdX[i][j][k] = (values[2][1][1] - values[0][1][1]) / 2;
                        dFdY[i][j][k] = (values[1][2][1] - values[1][0][1]) / 2;
                        dFdZ[i][j][k] = (values[1][1][2] - values[1][1][0]) / 2;
                          
                        d2FdXdY[i][j][k] = (values[2][2][1] - values[2][0][1] - values[0][2][1] + values[0][0][1]) / 4;
                        d2FdXdZ[i][j][k] = (values[2][1][2] - values[2][1][0] - values[0][1][2] + values[0][1][0]) / 4;
                        d2FdYdZ[i][j][k] = (values[1][2][2] - values[1][2][0] - values[1][0][2] + values[1][0][0]) / 4;
                          
                        d3FdXdYdZ[i][j][k] = (values[2][2][2] - values[2][0][2] -
                                              values[0][2][2] + values[0][0][2] -
                                              values[2][2][0] + values[2][0][0] +
                                              values[0][2][0] - values[0][0][0]) / 8;
    				}
                }
            }
        }

        // Create the interpolating function.
        return CustomTricubicInterpolatingFunction.createFunction(
        		new double[64],
        		new DoubleArrayTrivalueProvider(f),
        		new DoubleArrayTrivalueProvider(dFdX),
        		new DoubleArrayTrivalueProvider(dFdY),
        		new DoubleArrayTrivalueProvider(dFdZ),
        		new DoubleArrayTrivalueProvider(d2FdXdY),
        		new DoubleArrayTrivalueProvider(d2FdXdZ),
        		new DoubleArrayTrivalueProvider(d2FdYdZ),
        		new DoubleArrayTrivalueProvider(d3FdXdYdZ));
    }
    
	
    /**
     * Compute an interpolating function for the data. Creates a single tricubic function for 
     * interpolation between [0] and [1] assuming that the input value contains a 4x4x4 cube of values 
     * representing the value at [-1,0,1,2] for each axis. If no value is available at the ends of 
     * the range then the gradients are set to zero. This allows interpolation of data with a minimum 
     * length of 2 in each dimension.
     * <p>
     * The 4x4x4 cube is extracted from the input value at the given offset for [0,0,0].
     * <p>
     * To use the function to create an interpolated value in the range [0] and [1]:
     * 
     * <pre>
     * double x1 = xval.get(x);
     * double y1 = yval.get(y);
     * double z1 = zval.get(z);
     * double x2 = xval.get(x+1);
     * double y2 = yval.get(y+1);
     * double z2 = zval.get(z+1);
     * double xscale = x2 - x1;
     * double yscale = y2 - y1
     * double zscale = z2 - y2
     * // x>=x1 && x<=x2 && y>=y1 && y<=y2 && z>=z1 && z<=z2 
     * double value = f.value((x-x1) / xscale, (y-y1) / yscale, (z-z1) / zscale);
     * </pre>
     *
     * @param xval All the x-coordinates of the interpolation points, sorted
     * in increasing order.
     * @param yval All the y-coordinates of the interpolation points, sorted
     * in increasing order.
     * @param zval All the z-coordinates of the interpolation points, sorted
     * in increasing order.
     * @param fval the values of the interpolation points on all the grid knots
     * @param x the x-offset in the values
     * @param y the y-offset in the values
     * @param z the z-offset in the values
     * @return a tricubic function that interpolates the data.
     * @throws NumberIsTooSmallException if the number of points is too small for
     * the order of the interpolation
     * @throws IllegalArgumentException If the offset is not positive
     * @throws DimensionMismatchException if the array lengths are inconsistent.
     * @throws NonMonotonicSequenceException if arrays are not sorted
     */
    public static CustomTricubicFunction create(final ValueProvider xval,
            final ValueProvider yval,
            final ValueProvider zval,
            final TrivalueProvider fval, int x, int y, int z)
        throws NumberIsTooSmallException, IllegalArgumentException,
               DimensionMismatchException, NonMonotonicSequenceException {
    	if (x<0 || y<0 || z<0)
    		throw new IllegalArgumentException("Offset must be positive");
		if (xval.getLength() - x < 2)
			throw new NumberIsTooSmallException(xval.getLength(), x + 2, true);
		if (yval.getLength() - y < 2)
			throw new NumberIsTooSmallException(yval.getLength(), y + 2, true);
		if (zval.getLength() - z < 2)
			throw new NumberIsTooSmallException(zval.getLength(), z + 2, true);
        if (xval.getLength() != fval.getLengthX())
            throw new DimensionMismatchException(xval.getLength(), fval.getLengthX());
        if (yval.getLength() != fval.getLengthY())
            throw new DimensionMismatchException(yval.getLength(), fval.getLengthY());
        if (zval.getLength() != fval.getLengthZ())
            throw new DimensionMismatchException(zval.getLength(), fval.getLengthZ());
        
        CustomTricubicInterpolatingFunction.checkOrder(xval);
        CustomTricubicInterpolatingFunction.checkOrder(yval);
        CustomTricubicInterpolatingFunction.checkOrder(zval);
        
		final int xLen_1 = fval.getLengthX() - 1;
		final int yLen_1 = fval.getLengthY() - 1;
		final int zLen_1 = fval.getLengthZ() - 1;

        // Approximation to the partial derivatives using finite differences.
        final double[][][] f = new double[2][2][2];
        final double[][][] dFdX = new double[2][2][2];
        final double[][][] dFdY = new double[2][2][2];
        final double[][][] dFdZ = new double[2][2][2];
        final double[][][] d2FdXdY = new double[2][2][2];
        final double[][][] d2FdXdZ = new double[2][2][2];
        final double[][][] d2FdYdZ = new double[2][2][2];
        final double[][][] d3FdXdYdZ = new double[2][2][2];

        final double[][][] values = new double[3][3][3];
      	
        for (int i = 0; i < 2; i++) {
        	
        	int xx = x + i;
			boolean edgeX = xx == 0 || xx == xLen_1;
            final double deltaX = (edgeX) ? 0 : xval.get(xx+1) - xval.get(xx-1);
        	
            for (int j = 0; j < 2; j++) {

            	int yy = y + j;
				boolean edgeXY = edgeX || yy == 0 || yy == yLen_1;
	            final double deltaY = (edgeXY) ? 0 : yval.get(yy+1) - yval.get(yy-1);
                final double deltaXY = deltaX * deltaY;
            	
                for (int k = 0; k < 2; k++) {
                    
                	int zz = z + k;
    				
    				if (edgeXY || zz == 0 || zz == zLen_1)
    				{
    					// No gradients at the edge
                        f[i][j][k] = fval.get(xx, yy, zz);
                        
                        // Already zero
                        //dFdX[i][j][k] = 0;
                        //dFdY[i][j][k] = 0;
                        //dFdZ[i][j][k] = 0;
                        //  
                        //d2FdXdY[i][j][k] = 0;
                        //d2FdXdZ[i][j][k] = 0;
                        //d2FdYdZ[i][j][k] = 0;
                        //  
                        //d3FdXdYdZ[i][j][k] = 0;
    				}
    				else
    				{
                        final double deltaZ = zval.get(zz+1) - zval.get(zz-1);
                    	
                        fval.get(xx, yy, zz, values);
                        
                        f[i][j][k] = values[1][1][1]; 
    
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
    				}
                }
            }
        }

        // Create the interpolating function.
        return CustomTricubicInterpolatingFunction.createFunction(
        		new double[64],
        		xval.get(x+1) - xval.get(x),
        		yval.get(y+1) - yval.get(y),
        		zval.get(z+1) - zval.get(z),
        		new DoubleArrayTrivalueProvider(f),
        		new DoubleArrayTrivalueProvider(dFdX),
        		new DoubleArrayTrivalueProvider(dFdY),
        		new DoubleArrayTrivalueProvider(dFdZ),
        		new DoubleArrayTrivalueProvider(d2FdXdY),
        		new DoubleArrayTrivalueProvider(d2FdXdZ),
        		new DoubleArrayTrivalueProvider(d2FdYdZ),
        		new DoubleArrayTrivalueProvider(d3FdXdYdZ));
    }
    
    //@formatter:on

	/**
	 * Sample the function.
	 * <p>
	 * n samples will be taken per node in each dimension. A final sample is taken at then end of the sample range thus
	 * the final range for each axis will be the current axis range.
	 *
	 * @param fval
	 *            the function value
	 * @param n
	 *            the number of samples per spline node
	 * @param procedure
	 *            the procedure
	 * @param progress
	 *            the progress
	 * @throws IllegalArgumentException
	 *             If the number of samples is not at least 2
	 * @throws NumberIsTooSmallException
	 *             if the number of points in any dimension is less than 2
	 */
	public void sample(final TrivalueProvider fval, int n, TrivalueProcedure procedure) throws IllegalArgumentException
	{
		if (n < 2)
			throw new IllegalArgumentException("Samples must be at least 2");
		sample(fval, n, n, n, procedure);
	}

	/**
	 * Sample the function.
	 * <p>
	 * n samples will be taken per node in each dimension. A final sample is taken at the end of the sample range thus
	 * the final range for each axis will be the current axis range.
	 * <p>
	 * Uses the instance track progress and executor service if set.
	 *
	 * @param fval
	 *            the function value
	 * @param nx
	 *            the number of samples per spline node in the x dimension
	 * @param ny
	 *            the number of samples per spline node in the y dimension
	 * @param nz
	 *            the number of samples per spline node in the z dimension
	 * @param procedure
	 *            the procedure
	 * @throws IllegalArgumentException
	 *             If the number of samples is not positive and at least 2 in one dimension
	 * @throws NumberIsTooSmallException
	 *             if the number of points in any dimension is less than 2
	 */
	public void sample(final TrivalueProvider fval, final int nx, final int ny, final int nz,
			final TrivalueProcedure procedure) throws IllegalArgumentException
	{
		if (nx < 1 || ny < 1 || nz < 1)
			throw new IllegalArgumentException("Samples must be positive");
		if (nx == 1 && ny == 1 && nz == 1)
			throw new IllegalArgumentException("Samples must be at least 2 in one dimension");

		final int xLen = fval.getLengthX();
		final int yLen = fval.getLengthY();
		final int zLen = fval.getLengthZ();

		if (xLen < 2)
			throw new NumberIsTooSmallException(xLen, 2, true);
		if (yLen < 2)
			throw new NumberIsTooSmallException(yLen, 2, true);
		if (zLen < 2)
			throw new NumberIsTooSmallException(zLen, 2, true);

		final int xLen_1 = xLen - 1;
		final int yLen_1 = yLen - 1;
		final int zLen_1 = zLen - 1;
		final int xLen_2 = xLen - 2;
		final int yLen_2 = yLen - 2;
		final int zLen_2 = zLen - 2;

		// We can interpolate all nodes n-times plus a final point at the last node
		final int maxx = (xLen_1) * nx;
		final int maxy = (yLen_1) * ny;
		final int maxz = (zLen_1) * nz;
		if (!procedure.setDimensions(maxx + 1, maxy + 1, maxz + 1))
			return;

		// Allow threading
		final long xLen_1_yLen_1 = (long) xLen_1 * (yLen_1);
		final long nNodes = xLen_1_yLen_1 * zLen_1;
		final long total = (long) (maxx + 1) * (maxy + 1) * (maxz + 1);

		ExecutorService executorService = this.executorService;
		long taskSize = Math.max(1, this.taskSize);
		boolean threaded = executorService != null && taskSize < nNodes;

		final Ticker ticker = Ticker.create(progress, total, threaded);
		ticker.start();

		// Pre-compute interpolation tables
		final CubicSplinePosition[] sx = createCubicSplinePosition(nx);
		final CubicSplinePosition[] sy = createCubicSplinePosition(ny);
		final CubicSplinePosition[] sz = createCubicSplinePosition(nz);
		final int nx1 = nx + 1;
		final int ny1 = ny + 1;
		final int nz1 = nz + 1;

		final double[][] tables = new double[nx1 * ny1 * nz1][];
		for (int z = 0, i = 0; z < nz1; z++)
		{
			CubicSplinePosition szz = sz[z];
			for (int y = 0; y < ny1; y++)
			{
				CubicSplinePosition syy = sy[y];
				for (int x = 0; x < nx1; x++, i++)
				{
					tables[i] = CustomTricubicFunction.computePowerTable(sx[x], syy, szz);
				}
			}
		}

		// Write axis values
		for (int x = 0; x <= maxx; x++)
		{
			procedure.setX(x, (double) x / nx);
		}
		for (int y = 0; y <= maxy; y++)
		{
			procedure.setY(y, (double) y / ny);
		}
		for (int z = 0; z <= maxz; z++)
		{
			procedure.setZ(z, (double) z / nz);
		}

		if (threaded)
		{
			// Break this up into reasonable tasks, ensuring we can hold all the futures.
			long nTasks = (long) Math.ceil((double) nNodes / taskSize);
			while (nTasks >= Integer.MAX_VALUE)
			{
				taskSize *= 2;
				nTasks = (long) Math.ceil((double) nNodes / taskSize);
			}
			TurboList<Future<?>> futures = new TurboList<Future<?>>((int) nTasks);
			for (long from = 0; from < nNodes;)
			{
				final long from_ = from;
				final long to = Math.min(from + taskSize, nNodes);
				futures.add(executorService.submit(new Runnable()
				{
					public void run()
					{
						// Approximation to the partial derivatives using finite differences.
						final double[][][] f = new double[2][2][2];
						final double[][][] dFdX = new double[2][2][2];
						final double[][][] dFdY = new double[2][2][2];
						final double[][][] dFdZ = new double[2][2][2];
						final double[][][] d2FdXdY = new double[2][2][2];
						final double[][][] d2FdXdZ = new double[2][2][2];
						final double[][][] d2FdYdZ = new double[2][2][2];
						final double[][][] d3FdXdYdZ = new double[2][2][2];

						final double[][][] values = new double[3][3][3];
						final double[] beta = new double[64];

						for (long index = from_; index < to; index++)
						{
							// Convert position to the indices for the node
							int z = (int) (index / xLen_1_yLen_1);
							long mod = index % xLen_1_yLen_1;
							int y = (int) (mod / xLen_1);
							int x = (int) (mod % xLen_1);

							//@formatter:off
	                        for (int i = 0; i < 2; i++) {
	                        	int xx = x+i;
	                			boolean edgeX = xx == 0 || xx == xLen_1;
	                            for (int j = 0; j < 2; j++) {
	                            	int yy = y+j;
	                				boolean edgeXY = edgeX || yy == 0 || yy == yLen_1;
	                                for (int k = 0; k < 2; k++) {
	                                	int zz = z+k;
	                    				
	                    				if (edgeXY || zz == 0 || zz == zLen_1)
	                    				{
	                    					// No gradients at the edge
	                                        f[i][j][k] = fval.get(xx, yy, zz);
	                                        
	                                        dFdX[i][j][k] = 0;
	                                        dFdY[i][j][k] = 0;
	                                        dFdZ[i][j][k] = 0;
	                                          
	                                        d2FdXdY[i][j][k] = 0;
	                                        d2FdXdZ[i][j][k] = 0;
	                                        d2FdYdZ[i][j][k] = 0;
	                                          
	                                        d3FdXdYdZ[i][j][k] = 0;
	                    				}
	                    				else
	                    				{
	                                        fval.get(xx, yy, zz, values);
	        
	                                        f[i][j][k] = values[1][1][1];
	                                        
	                                        dFdX[i][j][k] = (values[2][1][1] - values[0][1][1]) / 2;
	                                        dFdY[i][j][k] = (values[1][2][1] - values[1][0][1]) / 2;
	                                        dFdZ[i][j][k] = (values[1][1][2] - values[1][1][0]) / 2;
	                                          
	                                        d2FdXdY[i][j][k] = (values[2][2][1] - values[2][0][1] - values[0][2][1] + values[0][0][1]) / 4;
	                                        d2FdXdZ[i][j][k] = (values[2][1][2] - values[2][1][0] - values[0][1][2] + values[0][1][0]) / 4;
	                                        d2FdYdZ[i][j][k] = (values[1][2][2] - values[1][2][0] - values[1][0][2] + values[1][0][0]) / 4;
	                                          
	                                        d3FdXdYdZ[i][j][k] = (values[2][2][2] - values[2][0][2] -
	                                                              values[0][2][2] + values[0][0][2] -
	                                                              values[2][2][0] + values[2][0][0] +
	                                                              values[0][2][0] - values[0][0][0]) / 8;
	                    				}
	                                }
	                            }
	                        }
	            
	                        // Create the interpolating function.
	                        CustomTricubicFunction cf = CustomTricubicInterpolatingFunction.createFunction(
	                        		beta,
	                        		new DoubleArrayTrivalueProvider(f),
	                        		new DoubleArrayTrivalueProvider(dFdX),
	                        		new DoubleArrayTrivalueProvider(dFdY),
	                        		new DoubleArrayTrivalueProvider(dFdZ),
	                        		new DoubleArrayTrivalueProvider(d2FdXdY),
	                        		new DoubleArrayTrivalueProvider(d2FdXdZ),
	                        		new DoubleArrayTrivalueProvider(d2FdYdZ),
	                        		new DoubleArrayTrivalueProvider(d3FdXdYdZ));
	                    	//@formatter:on

							// Write interpolated values. For the final position we use the extra table to 
							// get the value at x=1 in the range [0-1].
							for (int k = 0, maxk = (z == zLen_2) ? nz1 : nz, zz = z * nz; k < maxk; k++, zz++)
							{
								for (int j = 0, maxj = (y == yLen_2) ? ny1 : ny, yy = y * ny; j < maxj; j++, yy++)
								{
									// Position in the interpolation tables
									int pos = nx1 * (j + ny1 * k);
									for (int i = 0, maxi = (x == xLen_2) ? nx1 : nx, xx = x * nx; i < maxi; i++, xx++)
									{
										procedure.setValue(xx, yy, zz, cf.value(tables[pos++]));
										ticker.tick();
									}
								}
							}
						}
					}
				}));
				from = to;
			}

			Utils.waitForCompletion(futures);
		}
		else
		{
			// Approximation to the partial derivatives using finite differences.
			final double[][][] f = new double[2][2][2];
			final double[][][] dFdX = new double[2][2][2];
			final double[][][] dFdY = new double[2][2][2];
			final double[][][] dFdZ = new double[2][2][2];
			final double[][][] d2FdXdY = new double[2][2][2];
			final double[][][] d2FdXdZ = new double[2][2][2];
			final double[][][] d2FdYdZ = new double[2][2][2];
			final double[][][] d3FdXdYdZ = new double[2][2][2];

			final double[][][] values = new double[3][3][3];
			final double[] beta = new double[64];

			// Dynamically interpolate each node
			for (int x = 0; x < xLen_1; x++)
			{
				for (int y = 0; y < yLen_1; y++)
				{
					for (int z = 0; z < zLen_1; z++)
					{
						//@formatter:off
                        for (int i = 0; i < 2; i++) {
                        	int xx = x+i;
                			boolean edgeX = xx == 0 || xx == xLen_1;
                            for (int j = 0; j < 2; j++) {
                            	int yy = y+j;
                				boolean edgeXY = edgeX || yy == 0 || yy == yLen_1;
                                for (int k = 0; k < 2; k++) {
                                	int zz = z+k;
                    				
                    				if (edgeXY || zz == 0 || zz == zLen_1)
                    				{
                    					// No gradients at the edge
                                        f[i][j][k] = fval.get(xx, yy, zz);
                                        
                                        dFdX[i][j][k] = 0;
                                        dFdY[i][j][k] = 0;
                                        dFdZ[i][j][k] = 0;
                                          
                                        d2FdXdY[i][j][k] = 0;
                                        d2FdXdZ[i][j][k] = 0;
                                        d2FdYdZ[i][j][k] = 0;
                                          
                                        d3FdXdYdZ[i][j][k] = 0;
                    				}
                    				else
                    				{
                                        fval.get(xx, yy, zz, values);
        
                                        f[i][j][k] = values[1][1][1];
                                        
                                        dFdX[i][j][k] = (values[2][1][1] - values[0][1][1]) / 2;
                                        dFdY[i][j][k] = (values[1][2][1] - values[1][0][1]) / 2;
                                        dFdZ[i][j][k] = (values[1][1][2] - values[1][1][0]) / 2;
                                          
                                        d2FdXdY[i][j][k] = (values[2][2][1] - values[2][0][1] - values[0][2][1] + values[0][0][1]) / 4;
                                        d2FdXdZ[i][j][k] = (values[2][1][2] - values[2][1][0] - values[0][1][2] + values[0][1][0]) / 4;
                                        d2FdYdZ[i][j][k] = (values[1][2][2] - values[1][2][0] - values[1][0][2] + values[1][0][0]) / 4;
                                          
                                        d3FdXdYdZ[i][j][k] = (values[2][2][2] - values[2][0][2] -
                                                              values[0][2][2] + values[0][0][2] -
                                                              values[2][2][0] + values[2][0][0] +
                                                              values[0][2][0] - values[0][0][0]) / 8;
                    				}
                                }
                            }
                        }
            
                        // Create the interpolating function.
                        CustomTricubicFunction cf = CustomTricubicInterpolatingFunction.createFunction(
                        		beta,
                        		new DoubleArrayTrivalueProvider(f),
                        		new DoubleArrayTrivalueProvider(dFdX),
                        		new DoubleArrayTrivalueProvider(dFdY),
                        		new DoubleArrayTrivalueProvider(dFdZ),
                        		new DoubleArrayTrivalueProvider(d2FdXdY),
                        		new DoubleArrayTrivalueProvider(d2FdXdZ),
                        		new DoubleArrayTrivalueProvider(d2FdYdZ),
                        		new DoubleArrayTrivalueProvider(d3FdXdYdZ));
                    	//@formatter:on

						// Write interpolated values. For the final position we use the extra table to 
						// get the value at x=1 in the range [0-1].
						for (int k = 0, maxk = (z == zLen_2) ? nz1 : nz, zz = z * nz; k < maxk; k++, zz++)
						{
							for (int j = 0, maxj = (y == yLen_2) ? ny1 : ny, yy = y * ny; j < maxj; j++, yy++)
							{
								// Position in the interpolation tables
								int pos = nx1 * (j + ny1 * k);
								for (int i = 0, maxi = (x == xLen_2) ? nx1 : nx, xx = x * nx; i < maxi; i++, xx++)
								{
									procedure.setValue(xx, yy, zz, cf.value(tables[pos++]));
									ticker.tick();
								}
							}
						}
					}
				}
			}
		}

		ticker.stop();
	}

	private static CubicSplinePosition[] createCubicSplinePosition(int n)
	{
		// Use an extra one to have the final x=1 interpolation point.
		final double step = 1.0 / n;
		CubicSplinePosition[] s = new CubicSplinePosition[n + 1];
		for (int x = 0; x < n; x++)
			s[x] = new CubicSplinePosition(x * step);
		// Final interpolation point must be exactly 1
		s[n] = new CubicSplinePosition(1);
		return s;
	}
}
