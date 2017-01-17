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
 * <p>
 * The following syntax is valid:
 * 
 * <pre>
    int n;
    TriangleArray a = new TriangleArray(n);
    
    // fast iteration over the data
    for (int i = 0; i<n; i++)
        for (int j = i + 1, index = a.toIndex(i, j); j<n; j++, index++)
        {
        }

	// Iterate over all NxN values 
	for (int i = 0; i<n; i++)
	{
		for (int j = 0, precursor = a.toPrecursorIndex(i); j < i; j++)
		{
			int k = a.toSafeIndex(i, j);
			int index = a.precursorToIndex(precursor, j);
			// k == index
		}
		for (int j = i + 1, index = a.toIndex(i, j); j < n; j++, index++)
		{
			int k = a.toSafeIndex(i, j);
			// k == index
		}
	}

	// Comparing any index j to index i 
	a.setup(i);
	for (int j = 0; j < n; j++)
	{
		if (i == j)
			continue;
		int k = a.toSafeIndex(i, j);
		int index = a.toIndex(j);
		// k == index
	}
 * </pre>
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

	private int j, precursor, rootIndex;

	/**
	 * Setup to generate the linear index for any index i and target index j
	 * 
	 * @param j
	 *            the index j
	 */
	public void setup(int j)
	{
		this.j = j;
		precursor = toPrecursorIndex(j);
		rootIndex = toIndex(j, 0);
	}

	/**
	 * Generate the linear index for any index i and target index j (initialised with {@link #setup(int)})
	 * 
	 * @param i
	 *            the index i
	 * @return the linear index
	 * @throws IllegalArgumentException
	 *             if i==j
	 */
	public int toIndex(int i)
	{
		if (j > i)
		{
			return precursorToIndex(precursor, i);
		}
		if (j < i)
		{
			return rootIndex + i;
		}
		throw new IllegalArgumentException("i cannot equal j");
	}

	/**
	 * Convert from j to a precursor for the linear index. Index j must be greater than target i. Behaviour is
	 * undefined if i==j.
	 *
	 * @param j
	 *            the index j
	 * @return the precursor to the linear index
	 */
	public int toPrecursorIndex(int j)
	{
		return toIndex1 + j;
	}

	/**
	 * Convert from precursor j to linear index. Precursor for j must be computed with index j greater than i. Behaviour
	 * is undefined if i==j.
	 *
	 * @param precusor
	 *            the precursor to the linear index
	 * @param i
	 *            the index i
	 * @return the linear index
	 */
	public int precursorToIndex(int precusor, int i)
	{
		return precusor - (n - i) * ((n - i) - 1) / 2 - i;
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