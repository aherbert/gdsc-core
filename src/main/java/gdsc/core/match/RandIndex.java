package gdsc.core.match;

import org.apache.commons.math3.exception.MathArithmeticException;

/*----------------------------------------------------------------------------- 
 * GDSC SMLM Software
 * 
 * Copyright (C) 2013 Alex Herbert
 * Genome Damage and Stability Centre
 * University of Sussex, UK
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *---------------------------------------------------------------------------*/

import org.apache.commons.math3.util.CombinatoricsUtils;

/**
 * Compute the Rand index for two classifications of a set of data.
 * <p>
 * The Rand index has a value between 0 and 1, with 0 indicating that the two data clusters do not agree on any pair
 * of points and 1 indicating that the data clusters are exactly the same.
 * 
 * @see https://en.wikipedia.org/wiki/Rand_index
 * @author Alex Herbert
 */
public class RandIndex
{
	/**
	 * Compute the Rand index for two classifications of a set of data.
	 * <p>
	 * The Rand index has a value between 0 and 1, with 0 indicating that the two data clusters do not agree on any pair
	 * of points and 1 indicating that the data clusters are exactly the same.
	 * <p>
	 * Uses a simple method of comparing all possible pairs and counting identical classifications.
	 * 
	 * @param set1
	 *            the first set of clusters for the objects
	 * @param set2
	 *            the second set of clusters for the objects
	 * @return the Rand index
	 * @see https://en.wikipedia.org/wiki/Rand_index
	 */
	public static double simpleRandIndex(int[] set1, int[] set2)
	{
		if (set1 == null)
			throw new NullPointerException("set1");
		if (set2 == null)
			throw new NullPointerException("set2");
		if (set1.length != set2.length)
			throw new IllegalArgumentException("Sets must be the same size");

		int n = set1.length;

		if (n < 2)
			return 1;

		// a = the number of pairs of elements in S that are in the same set in X and in the same set in Y
		// b = the number of pairs of elements in S that are in different sets in X and in different sets in Y
		long a_plus_b = 0; // a+b
		for (int i = 0; i < n; i++)
		{
			int s1 = set1[i];
			int s2 = set2[i];
			for (int j = i + 1; j < n; j++)
			{
				boolean same1 = (s1 == set1[j]);
				boolean same2 = (s2 == set2[j]);
				if (same1 == same2)
					a_plus_b++;
			}
		}

		return (double) a_plus_b / CombinatoricsUtils.binomialCoefficient(n, 2);
	}

	/**
	 * Compute the Rand index for two classifications of a set of data.
	 * <p>
	 * The Rand index has a value between 0 and 1, with 0 indicating that the two data clusters do not agree on any pair
	 * of points and 1 indicating that the data clusters are exactly the same.
	 * <p>
	 * Compute using a contingency table.
	 * 
	 * @param set1
	 *            the first set of clusters for the objects
	 * @param set2
	 *            the second set of clusters for the objects
	 * @return the Rand index
	 * @see https://en.wikipedia.org/wiki/Rand_index
	 */
	public static double randIndex(int[] set1, int[] set2)
	{
		if (set1 == null)
			throw new NullPointerException("set1");
		if (set2 == null)
			throw new NullPointerException("set2");
		if (set1.length != set2.length)
			throw new IllegalArgumentException("Sets must be the same size");

		int n = set1.length;
		if (n < 2)
			return 1;

		// Compute using a contingency table.
		// Each set should optimally use integers from 0 to n-1 for n clusters.
		int[] set1a = new int[n];
		int[] set2b = new int[n];
		int max1 = compact(set1, set1a);
		int max2 = compact(set2, set2b);

		return randIndex(set1a, max1, set2b, max2);
	}

	/**
	 * Compact the set so that it contains cluster assignments from 0 to n-1 where n is the number of clusters.
	 *
	 * @param set
	 *            the set (modified in place)
	 * @return the number of clusters (n)
	 */
	public static int compact(int[] set)
	{
		// Edge cases
		if (set == null || set.length == 0)
			return 0;

		if (set.length == 1)
		{
			set[0] = 0;
			return 1;
		}

		int[] newSet = new int[set.length];
		boolean[] skip = new boolean[set.length];

		int n = 0;
		for (int i = 0; i < set.length; i++)
		{
			if (skip[i])
				continue;
			int value = set[i];
			for (int j = i; j < set.length; j++)
			{
				if (value == set[j])
				{
					skip[j] = true;
					newSet[j] = n;
				}
			}
			n++;
		}

		// Copy back
		System.arraycopy(newSet, 0, set, 0, set.length);

		return n;
	}

	/**
	 * Compact the set so that it contains cluster assignments from 0 to n-1 where n is the number of clusters.
	 *
	 * @param set
	 *            the set
	 * @param newSet
	 *            the new set
	 * @return the number of clusters (n)
	 */
	public static int compact(int[] set, int[] newSet)
	{
		// Edge cases
		if (set == null || set.length == 0)
			return 0;

		if (newSet == null || newSet.length < set.length)
			newSet = new int[set.length];

		if (set.length == 1)
		{
			newSet[0] = 0;
			return 1;
		}

		boolean[] skip = new boolean[set.length];

		int n = 0;
		for (int i = 0; i < set.length; i++)
		{
			if (skip[i])
				continue;
			int value = set[i];
			for (int j = i; j < set.length; j++)
			{
				if (value == set[j])
				{
					skip[j] = true;
					newSet[j] = n;
				}
			}
			n++;
		}

		return n;
	}

	/**
	 * Compute the Rand index for two classifications of a set of data.
	 * <p>
	 * The Rand index has a value between 0 and 1, with 0 indicating that the two data clusters do not agree on any pair
	 * of points and 1 indicating that the data clusters are exactly the same.
	 * <p>
	 * Compute using a contingency table. Each set should optimally use integers from 0 to n-1 for n clusters.
	 * <p>
	 * Warning: No checks are made on the input!
	 *
	 * @param set1
	 *            the first set of clusters for the objects
	 * @param n1
	 *            the number of clusters in set 1
	 * @param set2
	 *            the second set of clusters for the objects
	 * @param n2
	 *            the number of clusters in set 1
	 * @return the Rand index
	 * @see https://en.wikipedia.org/wiki/Rand_index
	 * @throws MathArithmeticException
	 *             if the sum of the contingency table is greater than the max value of an integer
	 */
	public static double randIndex(int[] set1, int n1, int[] set2, int n2)
	{
		int n = set1.length;
		if (n < 2)
			return 1;

		int[][] table = new int[n1][n2];

		for (int i = 0; i < n; i++)
		{
			table[set1[i]][set2[i]]++;
		}

		long total = 0;
		for (int i = 0; i < n1; i++)
		{
			long last = total;
			for (int j = 0; j < n2; j++)
				total += table[i][j];
			if (total < last)
				throw new MathArithmeticException();
		}

		if (total > Integer.MAX_VALUE)
			throw new MathArithmeticException();

		long tp_fp = 0;
		for (int i = 0; i < n1; i++)
		{
			int sum = 0;
			for (int j = 0; j < n2; j++)
				sum += table[i][j];
			tp_fp += CombinatoricsUtils.binomialCoefficient(sum, 2);
		}

		long tp_fn = 0;
		for (int j = 0; j < n2; j++)
		{
			int sum = 0;
			for (int i = 0; i < n1; i++)
				sum += table[i][j];
			tp_fn += CombinatoricsUtils.binomialCoefficient(sum, 2);
		}

		long tp = 0;
		for (int i = 0; i < n1; i++)
		{
			for (int j = 0; j < n2; j++)
				if (table[i][j] > 1)
					tp += CombinatoricsUtils.binomialCoefficient(table[i][j], 2);
		}

		long fp = tp_fp - tp;
		long fn = tp_fn - tp;
		long tn = CombinatoricsUtils.binomialCoefficient((int) total, 2) - tp - fp - fn;

		//System.out.printf("%d %d %d %d\n", tp, fp, tn, fn);

		return (double) (tp + tn) / (tp + fp + tn + fn);
	}

	/**
	 * Compute the Rand index for two classifications of a set of data.
	 * <p>
	 * The Rand index has a value between 0 and 1, with 0 indicating that the two data clusters do not agree on any pair
	 * of points and 1 indicating that the data clusters are exactly the same.
	 * <p>
	 * Compute using a contingency table.
	 * 
	 * @param set1
	 *            the first set of clusters for the objects
	 * @param set2
	 *            the second set of clusters for the objects
	 * @return the Rand index
	 * @see https://en.wikipedia.org/wiki/Rand_index
	 */
	public double getRandIndex(int[] set1, int[] set2)
	{
		tp = fp = tn = fn = 0;

		if (set1 == null)
			throw new NullPointerException("set1");
		if (set2 == null)
			throw new NullPointerException("set2");
		if (set1.length != set2.length)
			throw new IllegalArgumentException("Sets must be the same size");

		int n = set1.length;
		if (n < 2)
		{
			tp = (n == 1) ? 1 : 0;
			return 1;
		}

		// Compute using a contingency table.
		// Each set should optimally use integers from 0 to n-1 for n clusters.
		int[] set1a = new int[n];
		int[] set2b = new int[n];
		int max1 = compact(set1, set1a);
		int max2 = compact(set2, set2b);

		return getRandIndex(set1a, max1, set2b, max2);
	}

	private long tp, fp, tn, fn;

	/**
	 * Compute the Rand index for two classifications of a set of data.
	 * <p>
	 * The Rand index has a value between 0 and 1, with 0 indicating that the two data clusters do not agree on any pair
	 * of points and 1 indicating that the data clusters are exactly the same.
	 * <p>
	 * Compute using a contingency table. Each set should optimally use integers from 0 to n-1 for n clusters.
	 * <p>
	 * Warning: No checks are made on the input!
	 *
	 * @param set1
	 *            the first set of clusters for the objects
	 * @param n1
	 *            the number of clusters in set 1
	 * @param set2
	 *            the second set of clusters for the objects
	 * @param n2
	 *            the number of clusters in set 1
	 * @return the Rand index
	 * @see https://en.wikipedia.org/wiki/Rand_index
	 * @throws MathArithmeticException
	 *             if the sum of the contingency table is greater than the max value of an integer
	 */
	public double getRandIndex(int[] set1, int n1, int[] set2, int n2)
	{
		tp = fp = tn = fn = 0;

		int n = set1.length;
		if (n < 2)
		{
			tp = (n == 1) ? 1 : 0;
			return 1;
		}

		int[][] table = new int[n1][n2];

		for (int i = 0; i < n; i++)
		{
			table[set1[i]][set2[i]]++;
		}

		long total = 0;
		for (int i = 0; i < n1; i++)
		{
			long last = total;
			for (int j = 0; j < n2; j++)
				total += table[i][j];
			if (total < last)
				throw new MathArithmeticException();
		}

		if (total > Integer.MAX_VALUE)
			throw new MathArithmeticException();

		long tp_fp = 0;
		for (int i = 0; i < n1; i++)
		{
			int sum = 0;
			for (int j = 0; j < n2; j++)
				sum += table[i][j];
			tp_fp += CombinatoricsUtils.binomialCoefficient(sum, 2);
		}

		long tp_fn = 0;
		for (int j = 0; j < n2; j++)
		{
			int sum = 0;
			for (int i = 0; i < n1; i++)
				sum += table[i][j];
			tp_fn += CombinatoricsUtils.binomialCoefficient(sum, 2);
		}

		tp = 0;
		for (int i = 0; i < n1; i++)
		{
			for (int j = 0; j < n2; j++)
				if (table[i][j] > 1)
					tp += CombinatoricsUtils.binomialCoefficient(table[i][j], 2);
		}

		fp = tp_fp - tp;
		fn = tp_fn - tp;
		tn = CombinatoricsUtils.binomialCoefficient((int) total, 2) - tp - fp - fn;

		return (double) (tp + tn) / (tp + fp + tn + fn);
	}

	/**
	 * Gets the true positives from the last call to getRandIndex().
	 *
	 * @return the true positives
	 */
	public long getTruePositives()
	{
		return tp;
	}

	/**
	 * Gets the true negatives from the last call to getRandIndex().
	 *
	 * @return the true negatives
	 */
	public long getTrueNegatives()
	{
		return tn;
	}

	/**
	 * Gets the false positives from the last call to getRandIndex().
	 *
	 * @return the false positives
	 */
	public long getFalsePositives()
	{
		return fp;
	}

	/**
	 * Gets the false negatives from the last call to getRandIndex().
	 *
	 * @return the false negatives
	 */
	public long getFalseNegatives()
	{
		return fn;
	}
}
