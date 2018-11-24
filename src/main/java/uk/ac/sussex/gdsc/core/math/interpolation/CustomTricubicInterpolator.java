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
 * Copyright (C) 2011 - 2018 Alex Herbert
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

import uk.ac.sussex.gdsc.core.data.DoubleArrayTrivalueProvider;
import uk.ac.sussex.gdsc.core.data.DoubleArrayValueProvider;
import uk.ac.sussex.gdsc.core.data.TrivalueProvider;
import uk.ac.sussex.gdsc.core.data.ValueProvider;
import uk.ac.sussex.gdsc.core.data.procedures.TrivalueProcedure;
import uk.ac.sussex.gdsc.core.logging.Ticker;
import uk.ac.sussex.gdsc.core.logging.TrackProgress;
import uk.ac.sussex.gdsc.core.utils.ConcurrencyUtils;
import uk.ac.sussex.gdsc.core.utils.SimpleArrayUtils;
import uk.ac.sussex.gdsc.core.utils.TurboList;

import org.apache.commons.lang3.concurrent.ConcurrentRuntimeException;
import org.apache.commons.math3.analysis.interpolation.TrivariateGridInterpolator;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.NonMonotonicSequenceException;
import org.apache.commons.math3.exception.NumberIsTooSmallException;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Generates a tricubic interpolating function.
 */
public class CustomTricubicInterpolator implements TrivariateGridInterpolator {

  /**
   * The constant 2.
   *
   * <p>This is the minimum size required to interpolate a set of function points using a cubic
   * spline. Although the spline requires 4 points this is set to 2 as the outer points can be
   * assumed to be zero.
   */
  private static final int TWO = 2;
  /**
   * The constant 4.
   *
   * <p>This is the number of points required to interpolate a function using a cubic spline.
   */
  private static final int FOUR = 4;

  private TrackProgress progress;
  private ExecutorService executorService;
  private long taskSize = 1000;
  private boolean singlePrecision;

  /**
   * A builder for the CustomTricubicInterpolator.
   */
  public static class Builder {
    private ValueProvider xval;
    private ValueProvider yval;
    private ValueProvider zval;
    private TrivalueProvider fval;
    private TrackProgress progress;
    private ExecutorService executorService;
    private long taskSize;
    private boolean integerAxisValues;
    private boolean singlePrecision;

    /**
     * Sets the X value.
     *
     * @param x the x value
     * @return the builder
     */
    public Builder setXValue(double[] x) {
      return setXValue(new DoubleArrayValueProvider(x));
    }

    /**
     * Sets the X value.
     *
     * @param x the x value
     * @return the builder
     */
    public Builder setXValue(ValueProvider x) {
      xval = x;
      return this;
    }

    /**
     * Sets the Y value.
     *
     * @param y the y value
     * @return the builder
     */
    public Builder setYValue(double[] y) {
      return setYValue(new DoubleArrayValueProvider(y));
    }

    /**
     * Sets the Y value.
     *
     * @param y the y value
     * @return the builder
     */
    public Builder setYValue(ValueProvider y) {
      yval = y;
      return this;
    }

    /**
     * Sets the Z value.
     *
     * @param z the z value
     * @return the builder
     */
    public Builder setZValue(double[] z) {
      return setZValue(new DoubleArrayValueProvider(z));
    }

    /**
     * Sets the Z value.
     *
     * @param z the z value
     * @return the builder
     */
    public Builder setZValue(ValueProvider z) {
      zval = z;
      return this;
    }

    /**
     * Sets the function value.
     *
     * @param functionValue the function value
     * @return the builder
     */
    public Builder setFValue(double[][][] functionValue) {
      return setFValue(new DoubleArrayTrivalueProvider(functionValue));
    }

    /**
     * Sets the function value.
     *
     * @param functionValue the function value
     * @return the builder
     */
    public Builder setFValue(TrivalueProvider functionValue) {
      fval = functionValue;
      return this;
    }

    /**
     * Sets the progress tracker.
     *
     * @param progress the new progress tracker
     * @return the builder
     */
    public Builder setProgress(TrackProgress progress) {
      this.progress = progress;
      return this;
    }

    /**
     * Sets the executor service for interpolating.
     *
     * @param executorService the new executor service
     * @return the builder
     */
    public Builder setExecutorService(ExecutorService executorService) {
      this.executorService = executorService;
      return this;
    }

    /**
     * Set to true to create integer axis values stating from zero.
     *
     * @param integerAxisValues the integer axis values flag
     * @return the builder
     */
    public Builder setIntegerAxisValues(boolean integerAxisValues) {
      this.integerAxisValues = integerAxisValues;
      return this;
    }

    /**
     * Sets the task size for multi-threaded interpolation. If the number of interpolation nodes is
     * less than this then multi-threading is not used.
     *
     * @param taskSize the new task size
     * @return the builder
     */
    public Builder setTaskSize(long taskSize) {
      this.taskSize = taskSize;
      return this;
    }

    /**
     * Sets the single precision.
     *
     * @param singlePrecision the single precision
     * @return the builder
     */
    public Builder setSinglePrecision(boolean singlePrecision) {
      this.singlePrecision = singlePrecision;
      return this;
    }

    /**
     * Compute an interpolating function for the dataset.
     *
     * @return the custom tricubic interpolating function
     */
    public CustomTricubicInterpolatingFunction interpolate() {
      final CustomTricubicInterpolator i = build();
      if (integerAxisValues) {
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
    public CustomTricubicInterpolator build() {
      final CustomTricubicInterpolator i = new CustomTricubicInterpolator();
      i.setProgress(progress);
      if (taskSize > 0) {
        i.setTaskSize(taskSize);
      }
      i.setExecutorService(executorService);
      i.setSinglePrecision(singlePrecision);
      return i;
    }
  }

  @Override
  //@formatter:off
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
  //@formatter:on

  // CHECKSTYLE.OFF: LocalVariableName

  /**
   * Compute an interpolating function for the dataset.
   *
   * @param xval All the x-coordinates of the interpolation points, sorted in increasing order.
   * @param yval All the y-coordinates of the interpolation points, sorted in increasing order.
   * @param zval All the z-coordinates of the interpolation points, sorted in increasing order.
   * @param fval the values of the interpolation points on all the grid knots:
   *        {@code fval.get[i][j][k] = f(xval.get(i], yval.get(j], zval.get(k])}.
   * @return a function that interpolates the data set.
   * @throws DimensionMismatchException if the array lengths are inconsistent.
   * @throws NonMonotonicSequenceException if arrays are not sorted
   * @throws NumberIsTooSmallException if the number of points is too small for the order of the
   *         interpolation
   * @throws ConcurrentRuntimeException if interrupted when using an executor service
   *
   * @see org.apache.commons.math3.analysis.interpolation.TrivariateGridInterpolator#interpolate(
   *      double[], double[], double[], double[][][])
   */
  @SuppressWarnings("null")
  //@formatter:off
  public CustomTricubicInterpolatingFunction interpolate(final ValueProvider xval,
                                                         final ValueProvider yval,
                                                         final ValueProvider zval,
                                                         final TrivalueProvider fval) {
    //@formatter:on
    if (xval.getLength() < TWO) {
      throw new NumberIsTooSmallException(xval.getLength(), 2, true);
    }
    if (yval.getLength() < TWO) {
      throw new NumberIsTooSmallException(yval.getLength(), 2, true);
    }
    if (zval.getLength() < TWO) {
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
    final int xLenM1 = xLen - 1;
    final int yLenM1 = yLen - 1;
    final int zLenM1 = zLen - 1;

    // Approximation to the partial derivatives using finite differences.
    final double[][][] dFdX = new double[xLen][yLen][zLen];
    final double[][][] dFdY = new double[xLen][yLen][zLen];
    final double[][][] dFdZ = new double[xLen][yLen][zLen];
    final double[][][] d2FdXdY = new double[xLen][yLen][zLen];
    final double[][][] d2FdXdZ = new double[xLen][yLen][zLen];
    final double[][][] d2FdYdZ = new double[xLen][yLen][zLen];
    final double[][][] d3FdXdYdZ = new double[xLen][yLen][zLen];

    final long total = (long) xLen * yLen * zLen;

    final ExecutorService localExecutorService = this.executorService;
    long localTaskSize = Math.max(1, this.taskSize);
    final boolean threaded = localExecutorService != null && localTaskSize < total;

    final Ticker ticker = Ticker.create(progress, total, threaded);
    ticker.start();

    if (threaded) {
      final long xLenMyLen = (long) xLen * yLen;

      // Break this up into reasonable tasks, ensuring we can hold all the futures
      final long[] tmp = getTaskSizeAndNumberOfTasks(total, localTaskSize);
      localTaskSize = tmp[0];
      final long numberOfTasks = tmp[1];
      final TurboList<Future<?>> futures = new TurboList<>((int) numberOfTasks);
      for (long i = 0; i < total;) {
        final long from = i;
        final long to = Math.min(i + localTaskSize, total);
        futures.add(localExecutorService.submit(() -> {
          final double[][][] values = new double[3][3][3];
          for (long index = from; index < to; index++) {
            buildForInterpolating(index, xLen, xLenMyLen, xLenM1, yLenM1, zLenM1, xval, yval, zval,
                fval, dFdX, dFdY, dFdZ, d2FdXdY, d2FdXdZ, d2FdYdZ, d3FdXdYdZ, values, ticker);
          }
        }));
        i = to;
      }

      ConcurrencyUtils.waitForCompletionUnchecked(futures);
    } else {
      final double[][][] values = new double[3][3][3];

      // Updated to handle edges
      int nI = 0;
      int pI = 0;
      double deltaX = 0;
      for (int i = 0; i < xLen; i++) {

        final boolean edgeX = i == 0 || i == xLenM1;
        if (!edgeX) {
          // Not ignored so compute next and previous
          nI = i + 1;
          pI = i - 1;
          deltaX = xval.get(nI) - xval.get(pI);
        }

        int nJ = 0;
        int pJ = 0;
        double deltaY = 0;
        for (int j = 0; j < yLen; j++) {

          final boolean edgeY = j == 0 || j == yLenM1;
          if (!edgeY) {
            // Not ignored so compute next and previous
            nJ = j + 1;
            pJ = j - 1;
            deltaY = yval.get(nJ) - yval.get(pJ);
          }

          int nK = 0;
          int pK = 0;
          double deltaZ = 0;
          for (int k = 0; k < zLen; k++) {

            final boolean edgeZ = k == 0 || k == zLenM1;
            if (!edgeZ) {
              // Not ignored so compute next and previous
              nK = k + 1;
              pK = k - 1;
              deltaZ = zval.get(nK) - zval.get(pK);
            }

            buildForInterpolating(fval, dFdX, dFdY, dFdZ, d2FdXdY, d2FdXdZ, d2FdYdZ, d3FdXdYdZ,
                values, k, j, i, edgeX, nI, pI, deltaX, edgeY, nJ, pJ, deltaY, edgeZ, nK, pK,
                deltaZ);

            ticker.tick();
          }
        }
      }
    }

    ticker.stop();

    // Create the interpolating function.
    //@formatter:off
    return new CustomTricubicInterpolatingFunction(xval, yval, zval, fval,
        new DoubleArrayTrivalueProvider(dFdX),
        new DoubleArrayTrivalueProvider(dFdY),
        new DoubleArrayTrivalueProvider(dFdZ),
        new DoubleArrayTrivalueProvider(d2FdXdY),
        new DoubleArrayTrivalueProvider(d2FdXdZ),
        new DoubleArrayTrivalueProvider(d2FdYdZ),
        new DoubleArrayTrivalueProvider(d3FdXdYdZ),
        progress, localExecutorService, localTaskSize, singlePrecision);
  }

  private static void buildForInterpolating(long index,
      final int xLen,
      final long xLenMyLen,
      final int xLenM1,
      final int yLenM1,
      final int zLenM1,
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
      final Ticker ticker) {
    //@formatter:on

    // Convert position to the indices
    final int k = (int) (index / xLenMyLen);
    final long mod = index % xLenMyLen;
    final int j = (int) (mod / xLen);
    final int i = (int) (mod % xLen);

    final boolean edgeX = i == 0 || i == xLenM1;
    int nI;
    int pI;
    double deltaX;
    if (edgeX) {
      // Ignored
      nI = pI = 0;
      deltaX = 0;
    } else {
      nI = i + 1;
      pI = i - 1;
      deltaX = xval.get(nI) - xval.get(pI);
    }

    final boolean edgeY = j == 0 || j == yLenM1;
    int nJ;
    int pJ;
    double deltaY;
    if (edgeY) {
      // Ignored
      nJ = pJ = 0;
      deltaY = 0;
    } else {
      nJ = j + 1;
      pJ = j - 1;
      deltaY = yval.get(nJ) - yval.get(pJ);
    }

    final boolean edgeZ = k == 0 || k == zLenM1;
    int nK;
    int pK;
    double deltaZ;
    if (edgeZ) {
      // Ignored
      nK = pK = 0;
      deltaZ = 0;
    } else {
      nK = k + 1;
      pK = k - 1;
      deltaZ = zval.get(nK) - zval.get(pK);
    }

    buildForInterpolating(fval, dFdX, dFdY, dFdZ, d2FdXdY, d2FdXdZ, d2FdYdZ, d3FdXdYdZ, values, k,
        j, i, edgeX, nI, pI, deltaX, edgeY, nJ, pJ, deltaY, edgeZ, nK, pK, deltaZ);

    ticker.tick();
  }

  // CHECKSTYLE.ON: LocalVariableName
  // CHECKSTYLE.OFF: ParameterName

  private static void buildForInterpolating(final TrivalueProvider fval, final double[][][] dFdX,
      final double[][][] dFdY, final double[][][] dFdZ, final double[][][] d2FdXdY,
      final double[][][] d2FdXdZ, final double[][][] d2FdYdZ, final double[][][] d3FdXdYdZ,
      final double[][][] values, final int k, final int j, final int i, final boolean edgeX,
      final int nI, final int pI, final double deltaX, final boolean edgeY, final int nJ,
      final int pJ, final double deltaY, final boolean edgeZ, final int nK, final int pK,
      final double deltaZ) {
    if (edgeX || edgeY || edgeZ) {
      //@formatter:off
      // No gradients at the edge
      dFdX[i][j][k] = (edgeX) ? 0 : (fval.get(nI,j,k) - fval.get(pI,j,k)) / deltaX;
      dFdY[i][j][k] = (edgeY) ? 0 : (fval.get(i,nJ,k) - fval.get(i,pJ,k)) / deltaY;
      dFdZ[i][j][k] = (edgeZ) ? 0 : (fval.get(i,j,nK) - fval.get(i,j,pK)) / deltaZ;

      d2FdXdY[i][j][k] = (edgeX||edgeY) ? 0 : (fval.get(nI,nJ,k) - fval.get(nI,pJ,k) - fval.get(pI,nJ,k) + fval.get(pI,pJ,k)) / (deltaX * deltaY);
      d2FdXdZ[i][j][k] = (edgeX||edgeZ) ? 0 : (fval.get(nI,j,nK) - fval.get(nI,j,pK) - fval.get(pI,j,nK) + fval.get(pI,j,pK)) / (deltaX * deltaZ);
      d2FdYdZ[i][j][k] = (edgeY||edgeZ) ? 0 : (fval.get(i,nJ,nK) - fval.get(i,nJ,pK) - fval.get(i,pJ,nK) + fval.get(i,pJ,pK)) / (deltaY * deltaZ);
      //@formatter:on
    } else {
      // Use the 3x3x3 values around the indexes
      fval.get(i, j, k, values);

      //@formatter:off
      dFdX[i][j][k] = (values[2][1][1] - values[0][1][1]) / deltaX;
      dFdY[i][j][k] = (values[1][2][1] - values[1][0][1]) / deltaY;
      dFdZ[i][j][k] = (values[1][1][2] - values[1][1][0]) / deltaZ;

      final double deltaXy = deltaX * deltaY;

      d2FdXdY[i][j][k] = (values[2][2][1] - values[2][0][1] - values[0][2][1] + values[0][0][1]) / deltaXy;
      d2FdXdZ[i][j][k] = (values[2][1][2] - values[2][1][0] - values[0][1][2] + values[0][1][0]) / (deltaX * deltaZ);
      d2FdYdZ[i][j][k] = (values[1][2][2] - values[1][2][0] - values[1][0][2] + values[1][0][0]) / (deltaY * deltaZ);

      d3FdXdYdZ[i][j][k] = (values[2][2][2] - values[2][0][2] -
                            values[0][2][2] + values[0][0][2] -
                            values[2][2][0] + values[2][0][0] +
                            values[0][2][0] - values[0][0][0]) / (deltaXy * deltaZ);
      //@formatter:on
    }
  }

  // CHECKSTYLE.ON: ParameterName

  /**
   * Sets the progress tracker.
   *
   * @param progress the new progress tracker
   */
  public void setProgress(TrackProgress progress) {
    this.progress = progress;
  }

  /**
   * Sets the executor service for interpolating.
   *
   * @param executorService the new executor service
   */
  public void setExecutorService(ExecutorService executorService) {
    this.executorService = executorService;
  }

  /**
   * Gets the task size for multi-threaded interpolation.
   *
   * @return the task size
   */
  public long getTaskSize() {
    return taskSize;
  }

  /**
   * Sets the task size for multi-threaded interpolation. If the number of interpolation nodes is
   * less than this then multi-threading is not used.
   *
   * @param taskSize the new task size
   */
  public void setTaskSize(long taskSize) {
    this.taskSize = taskSize;
  }

  /**
   * Checks if is single precision.
   *
   * @return true, if is single precision
   */
  public boolean isSinglePrecision() {
    return singlePrecision;
  }

  /**
   * Sets the single precision flag. A single precision tricubic function will require half the
   * memory.
   *
   * @param singlePrecision the new single precision flag
   */
  public void setSinglePrecision(boolean singlePrecision) {
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
  public static CustomTricubicFunction create(final TrivalueProvider fval) {
    if (FOUR != fval.getLengthX()) {
      throw new DimensionMismatchException(4, fval.getLengthX());
    }
    if (FOUR != fval.getLengthY()) {
      throw new DimensionMismatchException(4, fval.getLengthY());
    }
    if (FOUR != fval.getLengthZ()) {
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

    for (int i = 0; i < TWO; i++) {
      for (int j = 0; j < TWO; j++) {
        for (int k = 0; k < TWO; k++) {

          fval.get(i + 1, j + 1, k + 1, values);

          f[i][j][k] = values[1][1][1];

          //@formatter:off
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
          //@formatter:on
        }
      }
    }

    // Create the interpolating function.
    //@formatter:off
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
    //@formatter:on
  }


  /**
   * Compute an interpolating function for the data. Creates a single tricubic function for
   * interpolation between [1] and [2] assuming that the input value is a 4x4x4 cube of values
   * representing the value at [0,1,2,3] for each axis.
   *
   * <p>To use the function to create an interpolated value in the range [1] and [2]:
   *
   * <pre>
   * {@code
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
   * }
   * </pre>
   *
   * @param xval All the x-coordinates of the interpolation points, sorted in increasing order.
   * @param yval All the y-coordinates of the interpolation points, sorted in increasing order.
   * @param zval All the z-coordinates of the interpolation points, sorted in increasing order.
   * @param fval the values of the interpolation points on all the grid knots
   * @return a tricubic function that interpolates the data.
   * @throws DimensionMismatchException if the array lengths are inconsistent.
   */
  //@formatter:off
  public static CustomTricubicFunction create(final ValueProvider xval,
                                              final ValueProvider yval,
                                              final ValueProvider zval,
                                              final TrivalueProvider fval) {
    //@formatter:on
    if (FOUR != fval.getLengthX()) {
      throw new DimensionMismatchException(4, fval.getLengthX());
    }
    if (FOUR != fval.getLengthY()) {
      throw new DimensionMismatchException(4, fval.getLengthY());
    }
    if (FOUR != fval.getLengthZ()) {
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

    for (int i = 0; i < TWO; i++) {

      final double nX = xval.get(i + 2);
      final double pX = xval.get(i);

      final double deltaX = nX - pX;

      for (int j = 0; j < TWO; j++) {

        final double nY = yval.get(j + 2);
        final double pY = yval.get(j);

        final double deltaY = nY - pY;
        final double deltaXy = deltaX * deltaY;

        for (int k = 0; k < TWO; k++) {

          final double nZ = zval.get(k + 2);
          final double pZ = zval.get(k);

          final double deltaZ = nZ - pZ;

          fval.get(i + 1, j + 1, k + 1, values);

          f[i][j][k] = values[1][1][1];

          //@formatter:off
          dFdX[i][j][k] = (values[2][1][1] - values[0][1][1]) / deltaX;
          dFdY[i][j][k] = (values[1][2][1] - values[1][0][1]) / deltaY;
          dFdZ[i][j][k] = (values[1][1][2] - values[1][1][0]) / deltaZ;

          final double deltaXZ = deltaX * deltaZ;
          final double deltaYZ = deltaY * deltaZ;

          d2FdXdY[i][j][k] = (values[2][2][1] - values[2][0][1] - values[0][2][1] + values[0][0][1]) / deltaXy;
          d2FdXdZ[i][j][k] = (values[2][1][2] - values[2][1][0] - values[0][1][2] + values[0][1][0]) / deltaXZ;
          d2FdYdZ[i][j][k] = (values[1][2][2] - values[1][2][0] - values[1][0][2] + values[1][0][0]) / deltaYZ;

          final double deltaXyZ = deltaXy * deltaZ;

          d3FdXdYdZ[i][j][k] = (values[2][2][2] - values[2][0][2] -
                                values[0][2][2] + values[0][0][2] -
                                values[2][2][0] + values[2][0][0] +
                                values[0][2][0] - values[0][0][0]) / deltaXyZ;
          //@formatter:on
        }
      }
    }

    // Create the interpolating function.
    //@formatter:off
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
    //@formatter:on
  }

  // CHECKSTYLE.OFF: LocalVariableName

  /**
   * Compute an interpolating function for the data. Creates a single tricubic function for
   * interpolation between 0 and 1 assuming that the input value contains a 4x4x4 cube of values
   * representing the value at [-1,0,1,2] for each axis. If no value is available at the ends of the
   * range then the gradients are set to zero. This allows interpolation of data with a minimum
   * length of 2 in each dimension.
   *
   * <p>The 4x4x4 cube is extracted from the input value at the given offset for [0,0,0].
   *
   * @param fval the values of the interpolation points on all the grid knots
   * @param x the x-offset in the values
   * @param y the y-offset in the values
   * @param z the z-offset in the values
   * @return a tricubic function that interpolates the data.
   * @throws NumberIsTooSmallException if the number of points is too small for the order of the
   *         interpolation
   * @throws IllegalArgumentException If the offset is not positive
   */
  public static CustomTricubicFunction create(final TrivalueProvider fval, int x, int y, int z) {
    if (x < 0 || y < 0 || z < 0) {
      throw new IllegalArgumentException("Offset must be positive");
    }
    if (fval.getLengthX() - x < TWO) {
      throw new NumberIsTooSmallException(fval.getLengthX(), x + 2, true);
    }
    if (fval.getLengthY() - y < TWO) {
      throw new NumberIsTooSmallException(fval.getLengthY(), y + 2, true);
    }
    if (fval.getLengthZ() - z < TWO) {
      throw new NumberIsTooSmallException(fval.getLengthZ(), z + 2, true);
    }

    final int xLenM1 = fval.getLengthX() - 1;
    final int yLenM1 = fval.getLengthY() - 1;
    final int zLenM1 = fval.getLengthZ() - 1;

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

    int nI = 0;
    int pI = 0;
    for (int ii = 0; ii < TWO; ii++) {

      final int i = x + ii;
      final boolean edgeX = i == 0 || i == xLenM1;
      if (!edgeX) {
        // Not ignored so compute next and previous
        nI = i + 1;
        pI = i - 1;
      }

      int nJ = 0;
      int pJ = 0;
      for (int jj = 0; jj < TWO; jj++) {

        final int j = y + jj;
        final boolean edgeY = j == 0 || j == yLenM1;
        if (!edgeY) {
          // Not ignored so compute next and previous
          nJ = j + 1;
          pJ = j - 1;
        }

        int nK = 0;
        int pK = 0;
        for (int kk = 0; kk < TWO; kk++) {

          final int k = z + kk;
          final boolean edgeZ = k == 0 || k == zLenM1;
          if (!edgeZ) {
            // Not ignored so compute next and previous
            nK = k + 1;
            pK = k - 1;
          }

          if (edgeX || edgeY || edgeZ) {
            f[ii][jj][kk] = fval.get(i, j, k);

            // No gradients at the edge
            //@formatter:off
            dFdX[ii][jj][kk] = (edgeX) ? 0 : (fval.get(nI,j,k) - fval.get(pI,j,k)) / 2;
            dFdY[ii][jj][kk] = (edgeY) ? 0 : (fval.get(i,nJ,k) - fval.get(i,pJ,k)) / 2;
            dFdZ[ii][jj][kk] = (edgeZ) ? 0 : (fval.get(i,j,nK) - fval.get(i,j,pK)) / 2;

            d2FdXdY[ii][jj][kk] = (edgeX||edgeY) ? 0 : (fval.get(nI,nJ,k) - fval.get(nI,pJ,k) - fval.get(pI,nJ,k) + fval.get(pI,pJ,k)) / 4;
            d2FdXdZ[ii][jj][kk] = (edgeX||edgeZ) ? 0 : (fval.get(nI,j,nK) - fval.get(nI,j,pK) - fval.get(pI,j,nK) + fval.get(pI,j,pK)) / 4;
            d2FdYdZ[ii][jj][kk] = (edgeY||edgeZ) ? 0 : (fval.get(i,nJ,nK) - fval.get(i,nJ,pK) - fval.get(i,pJ,nK) + fval.get(i,pJ,pK)) / 4;
            //@formatter:on
          } else {
            fval.get(i, j, k, values);

            f[ii][jj][kk] = values[1][1][1];

            //@formatter:off
            dFdX[ii][jj][kk] = (values[2][1][1] - values[0][1][1]) / 2;
            dFdY[ii][jj][kk] = (values[1][2][1] - values[1][0][1]) / 2;
            dFdZ[ii][jj][kk] = (values[1][1][2] - values[1][1][0]) / 2;

            d2FdXdY[ii][jj][kk] = (values[2][2][1] - values[2][0][1] - values[0][2][1] + values[0][0][1]) / 4;
            d2FdXdZ[ii][jj][kk] = (values[2][1][2] - values[2][1][0] - values[0][1][2] + values[0][1][0]) / 4;
            d2FdYdZ[ii][jj][kk] = (values[1][2][2] - values[1][2][0] - values[1][0][2] + values[1][0][0]) / 4;

            d3FdXdYdZ[ii][jj][kk] = (values[2][2][2] - values[2][0][2] -
                                     values[0][2][2] + values[0][0][2] -
                                     values[2][2][0] + values[2][0][0] +
                                     values[0][2][0] - values[0][0][0]) / 8;
            //@formatter:on
          }
        }
      }
    }

    // Create the interpolating function.
    //@formatter:off
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
    //@formatter:on
  }

  /**
   * Compute an interpolating function for the data. Creates a single tricubic function for
   * interpolation between [0] and [1] assuming that the input value contains a 4x4x4 cube of values
   * representing the value at [-1,0,1,2] for each axis. If no value is available at the ends of the
   * range then the gradients are set to zero. This allows interpolation of data with a minimum
   * length of 2 in each dimension.
   *
   * <p>The 4x4x4 cube is extracted from the input value at the given offset for [0,0,0].
   *
   * <p>To use the function to create an interpolated value in the range [0] and [1]:
   *
   * <pre>
   * {@code
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
   * }
   * </pre>
   *
   * @param xval All the x-coordinates of the interpolation points, sorted in increasing order.
   * @param yval All the y-coordinates of the interpolation points, sorted in increasing order.
   * @param zval All the z-coordinates of the interpolation points, sorted in increasing order.
   * @param fval the values of the interpolation points on all the grid knots
   * @param x the x-offset in the values
   * @param y the y-offset in the values
   * @param z the z-offset in the values
   * @return a tricubic function that interpolates the data.
   * @throws NumberIsTooSmallException if the number of points is too small for the order of the
   *         interpolation
   * @throws IllegalArgumentException If the offset is not positive
   * @throws DimensionMismatchException if the array lengths are inconsistent.
   * @throws NonMonotonicSequenceException if arrays are not sorted
   */
  public static CustomTricubicFunction create(final ValueProvider xval, final ValueProvider yval,
      final ValueProvider zval, final TrivalueProvider fval, int x, int y, int z) {
    if (x < 0 || y < 0 || z < 0) {
      throw new IllegalArgumentException("Offset must be positive");
    }
    if (xval.getLength() - x < TWO) {
      throw new NumberIsTooSmallException(xval.getLength(), x + 2, true);
    }
    if (yval.getLength() - y < TWO) {
      throw new NumberIsTooSmallException(yval.getLength(), y + 2, true);
    }
    if (zval.getLength() - z < TWO) {
      throw new NumberIsTooSmallException(zval.getLength(), z + 2, true);
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

    final int xLenM1 = fval.getLengthX() - 1;
    final int yLenM1 = fval.getLengthY() - 1;
    final int zLenM1 = fval.getLengthZ() - 1;

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

    int nI = 0;
    int pI = 0;
    double deltaX = 0;
    for (int ii = 0; ii < TWO; ii++) {

      final int i = x + ii;
      final boolean edgeX = i == 0 || i == xLenM1;
      if (!edgeX) {
        // Not ignored so compute next and previous
        nI = i + 1;
        pI = i - 1;
        deltaX = xval.get(nI) - xval.get(pI);
      }

      int nJ = 0;
      int pJ = 0;
      double deltaY = 0;
      for (int jj = 0; jj < TWO; jj++) {

        final int j = y + jj;
        final boolean edgeY = j == 0 || j == yLenM1;
        if (!edgeY) {
          // Not ignored so compute next and previous
          nJ = j + 1;
          pJ = j - 1;
          deltaY = yval.get(nJ) - yval.get(pJ);
        }

        final double deltaXy = deltaX * deltaY;

        int nK = 0;
        int pK = 0;
        double deltaZ = 0;
        for (int kk = 0; kk < TWO; kk++) {

          final int k = z + kk;
          final boolean edgeZ = k == 0 || k == zLenM1;
          if (!edgeZ) {
            // Not ignored so compute next and previous
            nK = k + 1;
            pK = k - 1;
            deltaZ = zval.get(nK) - zval.get(pK);
          }

          if (edgeX || edgeY || edgeZ) {
            // No gradients at the edge
            f[ii][jj][kk] = fval.get(i, j, k);

            // No gradients at the edge
            //@formatter:off
            dFdX[ii][jj][kk] = (edgeX) ? 0 : (fval.get(nI,j,k) - fval.get(pI,j,k)) / deltaX;
            dFdY[ii][jj][kk] = (edgeY) ? 0 : (fval.get(i,nJ,k) - fval.get(i,pJ,k)) / deltaY;
            dFdZ[ii][jj][kk] = (edgeZ) ? 0 : (fval.get(i,j,nK) - fval.get(i,j,pK)) / deltaZ;

            d2FdXdY[ii][jj][kk] = (edgeX||edgeY) ? 0 : (fval.get(nI,nJ,k) - fval.get(nI,pJ,k) - fval.get(pI,nJ,k) + fval.get(pI,pJ,k)) / deltaXy;
            d2FdXdZ[ii][jj][kk] = (edgeX||edgeZ) ? 0 : (fval.get(nI,j,nK) - fval.get(nI,j,pK) - fval.get(pI,j,nK) + fval.get(pI,j,pK)) / (deltaX * deltaZ);
            d2FdYdZ[ii][jj][kk] = (edgeY||edgeZ) ? 0 : (fval.get(i,nJ,nK) - fval.get(i,nJ,pK) - fval.get(i,pJ,nK) + fval.get(i,pJ,pK)) / (deltaY * deltaZ);
            //@formatter:on
          } else {
            fval.get(i, j, k, values);

            f[ii][jj][kk] = values[1][1][1];

            //@formatter:off
            dFdX[ii][jj][kk] = (values[2][1][1] - values[0][1][1]) / deltaX;
            dFdY[ii][jj][kk] = (values[1][2][1] - values[1][0][1]) / deltaY;
            dFdZ[ii][jj][kk] = (values[1][1][2] - values[1][1][0]) / deltaZ;

            d2FdXdY[ii][jj][kk] = (values[2][2][1] - values[2][0][1] - values[0][2][1] + values[0][0][1]) / deltaXy;
            d2FdXdZ[ii][jj][kk] = (values[2][1][2] - values[2][1][0] - values[0][1][2] + values[0][1][0]) / (deltaX * deltaZ);
            d2FdYdZ[ii][jj][kk] = (values[1][2][2] - values[1][2][0] - values[1][0][2] + values[1][0][0]) / (deltaY * deltaZ);

            d3FdXdYdZ[ii][jj][kk] = (values[2][2][2] - values[2][0][2] -
                                     values[0][2][2] + values[0][0][2] -
                                     values[2][2][0] + values[2][0][0] +
                                     values[0][2][0] - values[0][0][0]) / (deltaXy * deltaZ);
            //@formatter:on
          }
        }
      }
    }

    // Create the interpolating function.
    //@formatter:off
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
    //@formatter:on
  }

  /**
   * Sample the function.
   *
   * <p>n samples will be taken per node in each dimension. A final sample is taken at then end of
   * the sample range thus the final range for each axis will be the current axis range.
   *
   * @param fval the function value
   * @param n the number of samples per spline node
   * @param procedure the procedure
   * @throws IllegalArgumentException If the number of samples is not at least 2
   * @throws NumberIsTooSmallException if the number of points in any dimension is less than 2
   */
  public void sample(final TrivalueProvider fval, int n, TrivalueProcedure procedure) {
    if (n < TWO) {
      throw new IllegalArgumentException("Samples must be at least 2");
    }
    sample(fval, n, n, n, procedure);
  }

  /**
   * Sample the function.
   *
   * <p>n samples will be taken per node in each dimension. A final sample is taken at the end of
   * the sample range thus the final range for each axis will be the current axis range.
   *
   * <p>Uses the instance track progress and executor service if set.
   *
   * @param fval the function value
   * @param nx the number of samples per spline node in the x dimension
   * @param ny the number of samples per spline node in the y dimension
   * @param nz the number of samples per spline node in the z dimension
   * @param procedure the procedure
   * @throws IllegalArgumentException If the number of samples is not positive and at least 2 in one
   *         dimension
   * @throws NumberIsTooSmallException if the number of points in any dimension is less than 2
   */
  @SuppressWarnings("null")
  public void sample(final TrivalueProvider fval, final int nx, final int ny, final int nz,
      final TrivalueProcedure procedure) {
    if (nx < 1 || ny < 1 || nz < 1) {
      throw new IllegalArgumentException("Samples must be positive");
    }
    if (nx == 1 && ny == 1 && nz == 1) {
      throw new IllegalArgumentException("Samples must be at least 2 in one dimension");
    }

    final int xLen = fval.getLengthX();
    if (xLen < TWO) {
      throw new NumberIsTooSmallException(xLen, 2, true);
    }
    final int yLen = fval.getLengthY();
    if (yLen < TWO) {
      throw new NumberIsTooSmallException(yLen, 2, true);
    }
    final int zLen = fval.getLengthZ();
    if (zLen < TWO) {
      throw new NumberIsTooSmallException(zLen, 2, true);
    }

    // M is for minus
    final int xLenM1 = xLen - 1;
    final int yLenM1 = yLen - 1;
    final int zLenM1 = zLen - 1;

    // We can interpolate all nodes n-times plus a final point at the last node
    final int maxx = (xLenM1) * nx;
    final int maxy = (yLenM1) * ny;
    final int maxz = (zLenM1) * nz;
    if (!procedure.setDimensions(maxx + 1, maxy + 1, maxz + 1)) {
      return;
    }

    final int xLenM2 = xLen - 2;
    final int yLenM2 = yLen - 2;
    final int zLenM2 = zLen - 2;

    // Allow threading
    final long xLenM1yLenM1 = (long) xLenM1 * (yLenM1);
    final long nNodes = xLenM1yLenM1 * zLenM1;
    final long total = (long) (maxx + 1) * (maxy + 1) * (maxz + 1);

    final ExecutorService localExecutorService = this.executorService;
    long localTaskSize = Math.max(1, this.taskSize);
    final boolean threaded = localExecutorService != null && localTaskSize < nNodes;

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
    for (int x = 0; x <= maxx; x++) {
      procedure.setX(x, (double) x / nx);
    }
    for (int y = 0; y <= maxy; y++) {
      procedure.setY(y, (double) y / ny);
    }
    for (int z = 0; z <= maxz; z++) {
      procedure.setZ(z, (double) z / nz);
    }

    if (threaded) {
      // Break this up into reasonable tasks, ensuring we can hold all the futures.
      final long[] tmp = getTaskSizeAndNumberOfTasks(nNodes, localTaskSize);
      localTaskSize = tmp[0];
      final long numberOfTasks = tmp[1];
      final TurboList<Future<?>> futures = new TurboList<>((int) numberOfTasks);
      for (long n = 0; n < nNodes;) {
        final long from = n;
        final long to = Math.min(n + localTaskSize, nNodes);
        futures.add(localExecutorService.submit(() -> {
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

          // Wrap the arrays for CustomTricubicInterpolatingFunction.createFunction
          final DoubleArrayTrivalueProvider pf = new DoubleArrayTrivalueProvider(f);
          final DoubleArrayTrivalueProvider pdFdX = new DoubleArrayTrivalueProvider(dFdX);
          final DoubleArrayTrivalueProvider pdFdY = new DoubleArrayTrivalueProvider(dFdY);
          final DoubleArrayTrivalueProvider pdFdZ = new DoubleArrayTrivalueProvider(dFdZ);
          final DoubleArrayTrivalueProvider pd2FdXdY = new DoubleArrayTrivalueProvider(d2FdXdY);
          final DoubleArrayTrivalueProvider pd2FdXdZ = new DoubleArrayTrivalueProvider(d2FdXdZ);
          final DoubleArrayTrivalueProvider pd2FdYdZ = new DoubleArrayTrivalueProvider(d2FdYdZ);
          final DoubleArrayTrivalueProvider pd3FdXdYdZ = new DoubleArrayTrivalueProvider(d3FdXdYdZ);

          for (long index = from; index < to; index++) {
            // Convert position to the indices for the node
            final int z = (int) (index / xLenM1yLenM1);
            final long mod = index % xLenM1yLenM1;
            final int y = (int) (mod / xLenM1);
            final int x = (int) (mod % xLenM1);

            int nI = 0;
            int pI = 0;
            for (int ii = 0; ii < TWO; ii++) {
              final int i = x + ii;
              final boolean edgeX = i == 0 || i == xLenM1;
              if (!edgeX) {
                // Not ignored so compute next and previous
                nI = i + 1;
                pI = i - 1;
              }
              int nJ = 0;
              int pJ = 0;
              for (int jj = 0; jj < TWO; jj++) {
                final int j = y + jj;
                final boolean edgeY = j == 0 || j == yLenM1;
                if (!edgeY) {
                  // Not ignored so compute next and previous
                  nJ = j + 1;
                  pJ = j - 1;
                }
                int nK = 0;
                int pK = 0;
                for (int kk = 0; kk < TWO; kk++) {
                  final int k = z + kk;
                  final boolean edgeZ = k == 0 || k == zLenM1;
                  if (!edgeZ) {
                    // Not ignored so compute next and previous
                    nK = k + 1;
                    pK = k - 1;
                  }
                  buildForSampling(fval, f, dFdX, dFdY, dFdZ, d2FdXdY, d2FdXdZ, d2FdYdZ, d3FdXdYdZ,
                      values, nI, pI, ii, i, edgeX, nJ, pJ, jj, j, edgeY, nK, pK, kk, k, edgeZ);
                }
              }
            }

            // Create the interpolating function.
            final CustomTricubicFunction cf = CustomTricubicInterpolatingFunction.createFunction(
                beta, pf, pdFdX, pdFdY, pdFdZ, pd2FdXdY, pd2FdXdZ, pd2FdYdZ, pd3FdXdYdZ);

            // Write interpolated values. For the final position we use the extra table to
            // get the value at x=1 in the range [0-1].
            final int maxk = (z == zLenM2) ? nz1 : nz;
            for (int k = 0, zz = z * nz; k < maxk; k++, zz++) {
              final int maxj = (y == yLenM2) ? ny1 : ny;
              for (int j = 0, yy = y * ny; j < maxj; j++, yy++) {
                // Position in the interpolation tables
                int pos = nx1 * (j + ny1 * k);
                final int maxi = (x == xLenM2) ? nx1 : nx;
                for (int i = 0, xx = x * nx; i < maxi; i++, xx++) {
                  procedure.setValue(xx, yy, zz, cf.value(tables[pos++]));
                  ticker.tick();
                }
              }
            }
          }
        }));
        n = to;
      }

      ConcurrencyUtils.waitForCompletionUnchecked(futures);
    } else {
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

      // Wrap the arrays for CustomTricubicInterpolatingFunction.createFunction
      final DoubleArrayTrivalueProvider pf = new DoubleArrayTrivalueProvider(f);
      final DoubleArrayTrivalueProvider pdFdX = new DoubleArrayTrivalueProvider(dFdX);
      final DoubleArrayTrivalueProvider pdFdY = new DoubleArrayTrivalueProvider(dFdY);
      final DoubleArrayTrivalueProvider pdFdZ = new DoubleArrayTrivalueProvider(dFdZ);
      final DoubleArrayTrivalueProvider pd2FdXdY = new DoubleArrayTrivalueProvider(d2FdXdY);
      final DoubleArrayTrivalueProvider pd2FdXdZ = new DoubleArrayTrivalueProvider(d2FdXdZ);
      final DoubleArrayTrivalueProvider pd2FdYdZ = new DoubleArrayTrivalueProvider(d2FdYdZ);
      final DoubleArrayTrivalueProvider pd3FdXdYdZ = new DoubleArrayTrivalueProvider(d3FdXdYdZ);

      // Dynamically interpolate each node
      for (int x = 0; x < xLenM1; x++) {
        for (int y = 0; y < yLenM1; y++) {
          for (int z = 0; z < zLenM1; z++) {
            int nI = 0;
            int pI = 0;
            for (int ii = 0; ii < TWO; ii++) {
              final int i = x + ii;
              final boolean edgeX = i == 0 || i == xLenM1;
              if (!edgeX) {
                // Not ignored so compute next and previous
                nI = i + 1;
                pI = i - 1;
              }
              int nJ = 0;
              int pJ = 0;
              for (int jj = 0; jj < TWO; jj++) {
                final int j = y + jj;
                final boolean edgeY = j == 0 || j == yLenM1;
                if (!edgeY) {
                  // Not ignored so compute next and previous
                  nJ = j + 1;
                  pJ = j - 1;
                }
                int nK = 0;
                int pK = 0;
                for (int kk = 0; kk < TWO; kk++) {
                  final int k = z + kk;
                  final boolean edgeZ = k == 0 || k == zLenM1;
                  if (!edgeZ) {
                    // Not ignored so compute next and previous
                    nK = k + 1;
                    pK = k - 1;
                  }
                  buildForSampling(fval, f, dFdX, dFdY, dFdZ, d2FdXdY, d2FdXdZ, d2FdYdZ, d3FdXdYdZ,
                      values, nI, pI, ii, i, edgeX, nJ, pJ, jj, j, edgeY, nK, pK, kk, k, edgeZ);
                }
              }
            }

            // Create the interpolating function.
            final CustomTricubicFunction cf = CustomTricubicInterpolatingFunction.createFunction(
                beta, pf, pdFdX, pdFdY, pdFdZ, pd2FdXdY, pd2FdXdZ, pd2FdYdZ, pd3FdXdYdZ);

            // Write interpolated values. For the final position we use the extra table to
            // get the value at x=1 in the range [0-1].
            final int maxk = (z == zLenM2) ? nz1 : nz;
            for (int k = 0, zz = z * nz; k < maxk; k++, zz++) {
              final int maxj = (y == yLenM2) ? ny1 : ny;
              for (int j = 0, yy = y * ny; j < maxj; j++, yy++) {
                // Position in the interpolation tables
                int pos = nx1 * (j + ny1 * k);
                final int maxi = (x == xLenM2) ? nx1 : nx;
                for (int i = 0, xx = x * nx; i < maxi; i++, xx++) {
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

  // CHECKSTYLE.OFF: LocalVariableName
  // CHECKSTYLE.OFF: ParameterName

  private static void buildForSampling(final TrivalueProvider fval, final double[][][] f,
      final double[][][] dFdX, final double[][][] dFdY, final double[][][] dFdZ,
      final double[][][] d2FdXdY, final double[][][] d2FdXdZ, final double[][][] d2FdYdZ,
      final double[][][] d3FdXdYdZ, final double[][][] values, int nI, int pI, int ii, final int i,
      final boolean edgeX, int nJ, int pJ, int jj, final int j, final boolean edgeY, int nK, int pK,
      int kk, final int k, final boolean edgeZ) {
    if (edgeX || edgeY || edgeZ) {
      f[ii][jj][kk] = fval.get(i, j, k);

      // No gradients at the edge
      //@formatter:off
      dFdX[ii][jj][kk] = (edgeX) ? 0 : (fval.get(nI,j,k) - fval.get(pI,j,k)) / 2;
      dFdY[ii][jj][kk] = (edgeY) ? 0 : (fval.get(i,nJ,k) - fval.get(i,pJ,k)) / 2;
      dFdZ[ii][jj][kk] = (edgeZ) ? 0 : (fval.get(i,j,nK) - fval.get(i,j,pK)) / 2;

      d2FdXdY[ii][jj][kk] = (edgeX||edgeY) ? 0 : (fval.get(nI,nJ,k) - fval.get(nI,pJ,k) - fval.get(pI,nJ,k) + fval.get(pI,pJ,k)) / 4;
      d2FdXdZ[ii][jj][kk] = (edgeX||edgeZ) ? 0 : (fval.get(nI,j,nK) - fval.get(nI,j,pK) - fval.get(pI,j,nK) + fval.get(pI,j,pK)) / 4;
      d2FdYdZ[ii][jj][kk] = (edgeY||edgeZ) ? 0 : (fval.get(i,nJ,nK) - fval.get(i,nJ,pK) - fval.get(i,pJ,nK) + fval.get(i,pJ,pK)) / 4;

      d3FdXdYdZ[ii][jj][kk] = 0;
      //@formatter:on
    } else {
      fval.get(i, j, k, values);

      f[ii][jj][kk] = values[1][1][1];

      //@formatter:off
      dFdX[ii][jj][kk] = (values[2][1][1] - values[0][1][1]) / 2;
      dFdY[ii][jj][kk] = (values[1][2][1] - values[1][0][1]) / 2;
      dFdZ[ii][jj][kk] = (values[1][1][2] - values[1][1][0]) / 2;

      d2FdXdY[ii][jj][kk] = (values[2][2][1] - values[2][0][1] - values[0][2][1] + values[0][0][1]) / 4;
      d2FdXdZ[ii][jj][kk] = (values[2][1][2] - values[2][1][0] - values[0][1][2] + values[0][1][0]) / 4;
      d2FdYdZ[ii][jj][kk] = (values[1][2][2] - values[1][2][0] - values[1][0][2] + values[1][0][0]) / 4;

      d3FdXdYdZ[ii][jj][kk] = (values[2][2][2] - values[2][0][2] -
                               values[0][2][2] + values[0][0][2] -
                               values[2][2][0] + values[2][0][0] +
                               values[0][2][0] - values[0][0][0]) / 8;
      //@formatter:on
    }
  }

  // CHECKSTYLE.ON: ParameterName

  /**
   * Gets the task size and number of tasks.
   *
   * <p>The task size is increased to reduce the number of tasks until the number of tasks can fit
   * in memory.
   *
   * @param total the total number of tasks
   * @param taskSize the initial task size
   * @return [taskSize, numberOfTasks]
   */
  static long[] getTaskSizeAndNumberOfTasks(final long total, long taskSize) {
    long numberOfTasks = (long) Math.ceil((double) total / taskSize);
    long newTaskSize = taskSize;
    while (numberOfTasks >= Integer.MAX_VALUE) {
      newTaskSize *= 2;
      numberOfTasks = (long) Math.ceil((double) total / newTaskSize);
    }
    return new long[] {newTaskSize, numberOfTasks};
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
}
