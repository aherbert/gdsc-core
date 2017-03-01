package gdsc.core.math;

/*----------------------------------------------------------------------------- 
 * GDSC SMLM Software
 * 
 * Copyright (C) 2017 Alex Herbert
 * Genome Damage and Stability Centre
 * University of Sussex, UK
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *---------------------------------------------------------------------------*/

/**
 * Interface to calculate the mean and variance of arrayed data.
 */
public interface ArrayMoment
{
	/**
	 * Add the data. This is a convenience method for arrays of size one.
	 *
	 * @param data
	 *            the data
	 */
	public void add(double data);

	/**
	 * Add the data. The first set of data defines the number of individual moments to compute. All subsequent data must
	 * be the same size, e.g an array of length n will compute n first and second moments for the range 0<=i<n.
	 * 
	 * @param data
	 *            the data
	 */
	public void add(double[] data);

	/**
	 * Add the data. The first set of data defines the number of individual moments to compute. All subsequent data must
	 * be the same size, e.g an array of length n will compute n first and second moments for the range 0<=i<n.
	 * 
	 * @param data
	 *            the data
	 */
	public void add(float[] data);

	/**
	 * Add the data. The first set of data defines the number of individual moments to compute. All subsequent data must
	 * be the same size, e.g an array of length n will compute n first and second moments for the range 0<=i<n.
	 * 
	 * @param data
	 *            the data
	 */
	public void add(int[] data);

	/**
	 * Gets the first moment (the sample mean).
	 *
	 * @return the first moment (null if no data has been added)
	 */
	public double[] getFirstMoment();

	/**
	 * Gets the second moment (sum of squared deviations from the sample mean).
	 *
	 * @return the second moment (null if no data has been added)
	 */
	public double[] getSecondMoment();

	/**
	 * Gets the number of observations.
	 *
	 * @return the n
	 */
	public long getN();

	/**
	 * Gets the unbiased estimate of the variance.
	 *
	 * @return the variance (null if no data has been added)
	 */
	public double[] getVariance();

	/**
	 * Gets the estimate of the variance.
	 *
	 * @param isBiasCorrected
	 *            Set to true to be bias corrected, i.e. unbiased
	 * @return the variance (null if no data has been added)
	 */
	public double[] getVariance(boolean isBiasCorrected);

	/**
	 * Gets the unbiased estimate of the standard deviation.
	 *
	 * @return the standard deviation (null if no data has been added)
	 */
	public double[] getStandardDeviation();

	/**
	 * Gets the estimate of the standard deviation.
	 *
	 * @param isBiasCorrected
	 *            Set to true to be bias corrected, i.e. unbiased
	 * @return the standard deviation (null if no data has been added)
	 */
	public double[] getStandardDeviation(boolean isBiasCorrected);

	/**
	 * Create a new instance.
	 *
	 * @return the array moment
	 */
	public ArrayMoment newInstance();
}