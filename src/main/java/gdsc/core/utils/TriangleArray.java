package gdsc.core.utils;

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
 * Specify data in a 2D upper triangular array of size n in 1 dimension.
 * <p>
 * The amount of data will be n*(n-1)/2. The data can be iterated for i in 0:n-1 and for j in i+1:n-1. No index is
 * computed for i==j.
 * 
 * @author Alex Herbert
 */
public class TriangleArray
{
	/**
	 * The length of the array data (n * (n - 1) / 2).
	 *
	 * @param n
	 *            the size of the array in 1 dimension
	 * @return the length
	 */
	public static int getLength(int n)
	{
		return (n * (n - 1) / 2);
	}

	/**
	 * Convert from ij to linear index. Behaviour is undefined if i==j.
	 *
	 * @param n
	 *            the size of the array in 1 dimension
	 * @param i
	 *            the index i
	 * @param j
	 *            the index j
	 * @return the linear index
	 */
	public static int toIndex(int n, int i, int j)
	{
		return (n * (n - 1) / 2) - (n - i) * ((n - i) - 1) / 2 + j - i - 1;
	}

	/**
	 * Convert from ij to linear index. If j is less than i then the pair are reversed. Behaviour is undefined if i==j.
	 *
	 * @param n
	 *            the size of the array in 1 dimension
	 * @param i
	 *            the index i
	 * @param j
	 *            the index j
	 * @return the linear index
	 */
	public static int toSafeIndex(int n, int i, int j)
	{
		if (j > i)
			return toIndex(n, i, j);
		return toIndex(n, j, i);
	}

	/**
	 * Convert from linear index to ij
	 *
	 * @param n
	 *            the size of the array in 1 dimension
	 * @param k
	 *            the linear index (Must be with the bound 0:k-1)
	 * @return the ij data
	 */
	public static int[] fromIndex(int n, int k)
	{
		int i = n - 2 - (int) Math.floor(Math.sqrt(-8 * k + 4 * n * (n - 1) - 7) / 2.0 - 0.5);
		int j = k + i + 1 - (n * (n - 1) / 2) + (n - i) * ((n - i) - 1) / 2;
		return new int[] { i, j };
	}

	/**
	 * Convert from linear index to ij
	 *
	 * @param n
	 *            the size of the array in 1 dimension
	 * @param k
	 *            the linear index (Must be with the bound 0:k-1)
	 * @param ij
	 *            the ij data (Must be size 2 or greater)
	 */
	public static void fromIndex(int n, int k, int[] ij)
	{
		final int i = n - 2 - (int) Math.floor(Math.sqrt(-8 * k + 4 * n * (n - 1) - 7) / 2.0 - 0.5);
		ij[0] = i;
		ij[1] = k + i + 1 - (n * (n - 1) / 2) + (n - i) * ((n - i) - 1) / 2;
	}

	/**
	 * The size of the array in 1 dimension
	 */
	final int n;

	private final int toIndex1, fromIndex1, fromIndex2;

	/**
	 * Instantiates a new 2D upper triangle array.
	 *
	 * @param n
	 *            the size of the array in 1 dimension
	 */
	public TriangleArray(int n)
	{
		if (n < 0)
			throw new IllegalArgumentException("n must be positive");

		this.n = n;

		// Pre-compute conversion constants 
		toIndex1 = getLength() - 1;
		fromIndex1 = 4 * n * (n - 1) - 7;
		fromIndex2 = n - 2;
	}

	/**
	 * The length of the array data (n * (n - 1) / 2).
	 *
	 * @return the length
	 */
	public int getLength()
	{
		return (n * (n - 1) / 2);
	}

	/**
	 * Convert from ij to linear index. Index j must be greater than i. Behaviour is undefined if i==j.
	 *
	 * @param i
	 *            the index i
	 * @param j
	 *            the index j
	 * @return the linear index
	 */
	public int toIndex(int i, int j)
	{
		return toIndex1 - (n - i) * ((n - i) - 1) / 2 + j - i;
	}

	/**
	 * Convert from ij to linear index. If j is less than i then the pair are reversed. Behaviour is undefined if i==j.
	 *
	 * @param i
	 *            the index i
	 * @param j
	 *            the index j
	 * @return the linear index
	 */
	public int toSafeIndex(int i, int j)
	{
		if (j > i)
			return toIndex(i, j);
		return toIndex(j, i);
	}

	/**
	 * Convert from linear index to ij.
	 *
	 * @param k
	 *            the linear index (Must be with the bound 0:length-1)
	 * @return the ij data
	 */
	public int[] fromIndex(int k)
	{
		final int i = fromIndex2 - (int) Math.floor(Math.sqrt(-8 * k + fromIndex1) / 2.0 - 0.5);
		final int j = k + i - toIndex1 + (n - i) * ((n - i) - 1) / 2;
		return new int[] { i, j };
	}

	/**
	 * Convert from linear index to ij.
	 *
	 * @param k
	 *            the linear index (Must be with the bound 0:length-1)
	 * @param ij
	 *            the ij data (Must be size 2 or greater)
	 */
	public void fromIndex(int k, int[] ij)
	{
		final int i = fromIndex2 - (int) Math.floor(Math.sqrt(-8 * k + fromIndex1) / 2.0 - 0.5);
		ij[0] = i;
		ij[1] = k + i - toIndex1 + (n - i) * ((n - i) - 1) / 2;
	}
}