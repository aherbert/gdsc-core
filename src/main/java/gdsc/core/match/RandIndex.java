package gdsc.core.match;

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

/**
 * Compute the Rand index for two classifications of a set of data.
 * <p>
 * The Rand index has a value between 0 and 1, with 0 indicating that the two data clusters do not agree on any pair
 * of points and 1 indicating that the data clusters are exactly the same.
 * <p>
 * A problem with the Rand index is that the expected value between random partitions is not constant. The adjusted Rand
 * index assumes the generalized hyper-geometric distribution as the model of randomness. It has the maximum value 1,
 * and its expected value is 0 in the case of random clusters.
 * <p>
 * W. M. Rand (1971). "Objective criteria for the evaluation of clustering methods". Journal of the American Statistical
 * Association. American Statistical Association. 66 (336): 846–850. doi:10.2307/2284239. JSTOR 2284239. 
 * 
 * @see https://en.wikipedia.org/wiki/Rand_index
 * @author Alex Herbert
 */
public class RandIndex
{
	/**
	 * Returns an exact representation of the the number of
	 * 2-element subsets that can be selected from an
	 * {@code n}-element set.
	 * <p>
	 * If n==0 or n==1 it will return 0.
	 * </p>
	 *
	 * @param n
	 *            the size of the set
	 * @return {@code n choose 2}
	 */
	private static long binomialCoefficient2(final long n)
	{
		return (n - 1L) * n / 2L;
	}

	/**
	 * Gets the default Rand index for small datasets.
	 *
	 * @param n
	 *            the n
	 * @return the default rand index
	 */
	private static double getDefaultRandIndex(int n)
	{
		return (n == 1) ? 1 : 0;
	}

	/**
	 * Gets the default adjusted Rand index for small datasets.
	 *
	 * @param n
	 *            the n
	 * @return the default rand index
	 */
	private static double getDefaultAdjustedRandIndex(int n)
	{
		return (n == 1) ? 1 : 0;
	}

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

		final int n = set1.length;

		if (n < 2)
			return getDefaultRandIndex(n);

		// a = the number of pairs of elements in S that are in the same set in X and in the same set in Y
		// b = the number of pairs of elements in S that are in different sets in X and in different sets in Y
		long a_plus_b = 0; // a+b
		for (int i = 0; i < n; i++)
		{
			final int s1 = set1[i];
			final int s2 = set2[i];
			for (int j = i + 1; j < n; j++)
			{
				if (s1 == set1[j])
				{
					if (s2 == set2[j])
						a_plus_b++;
				}
				else
				{
					if (s2 != set2[j])
						a_plus_b++;
				}
			}
		}

		return (double) a_plus_b / binomialCoefficient2(n);
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

		final int n = set1.length;
		if (n < 2)
			return getDefaultRandIndex(n);

		// Compute using a contingency table.
		// Each set should optimally use integers from 0 to n-1 for n clusters.
		final int[] set1a = new int[n];
		final int[] set2b = new int[n];
		final int max1 = compact(set1, set1a);
		final int max2 = compact(set2, set2b);

		return randIndex(set1a, max1, set2b, max2);
	}

	/**
	 * Compute the adjusted Rand index for two classifications of a set of data.
	 * <p>
	 * The adjusted Rand index is the corrected-for-chance version of the Rand index. Though the Rand Index may only
	 * yield a value between 0 and +1, the adjusted Rand index can yield negative values if the index is less than the
	 * expected index.
	 * 
	 * @param set1
	 *            the first set of clusters for the objects
	 * @param set2
	 *            the second set of clusters for the objects
	 * @return the Rand index
	 * @see https://en.wikipedia.org/wiki/Rand_index
	 */
	public static double adjustedRandIndex(int[] set1, int[] set2)
	{
		if (set1 == null)
			throw new NullPointerException("set1");
		if (set2 == null)
			throw new NullPointerException("set2");
		if (set1.length != set2.length)
			throw new IllegalArgumentException("Sets must be the same size");

		final int n = set1.length;
		if (n < 2)
			return getDefaultAdjustedRandIndex(n);

		// Compute using a contingency table.
		// Each set should optimally use integers from 0 to n-1 for n clusters.
		final int[] set1a = new int[n];
		final int[] set2b = new int[n];
		final int max1 = compact(set1, set1a);
		final int max2 = compact(set2, set2b);

		return adjustedRandIndex(set1a, max1, set2b, max2);
	}

	/**
	 * Compact the set so that it contains cluster assignments from 0 to n-1 where n is the number of clusters (max
	 * cluster number + 1).
	 *
	 * @param set
	 *            the set (modified in place)
	 * @return the number of clusters (max cluster number + 1) (n)
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

		final int[] newSet = new int[set.length];
		final boolean[] skip = new boolean[set.length];

		int n = 0;
		for (int i = 0; i < set.length; i++)
		{
			if (skip[i])
				continue;
			final int value = set[i];
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
	 * Compact the set so that it contains cluster assignments from 0 to n-1 where n is the number of clusters (max
	 * cluster number + 1).
	 *
	 * @param set
	 *            the set
	 * @param newSet
	 *            the new set
	 * @return the number of clusters (max cluster number + 1) (n)
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

		final boolean[] skip = new boolean[set.length];

		int n = 0;
		for (int i = 0; i < set.length; i++)
		{
			if (skip[i])
				continue;
			final int value = set[i];
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
	 * Compute using a contingency table. Each set should use integers from 0 to n-1 for n clusters. If clusters numbers
	 * above n clusters are used for either set then an {@link ArrayIndexOutOfBoundsException} will occur.
	 * <p>
	 * Warning: No checks are made on the input!
	 *
	 * @param set1
	 *            the first set of clusters for the objects
	 * @param n1
	 *            the number of clusters (max cluster number + 1) in set 1
	 * @param set2
	 *            the second set of clusters for the objects
	 * @param n2
	 *            the number of clusters (max cluster number + 1) in set 2
	 * @return the Rand index
	 * @see https://en.wikipedia.org/wiki/Rand_index
	 * @throws RuntimeException
	 *             if the sum of the contingency table is greater than the max value of an integer
	 */
	public static double randIndex(int[] set1, int n1, int[] set2, int n2)
	{
		final int n = set1.length;
		if (n < 2)
			return getDefaultRandIndex(n);

		final int size = n1 * n2;
		final int[] table = new int[size];

		for (int i = 0; i < n; i++)
		{
			table[set1[i] * n2 + set2[i]]++;
		}

		long total = 0;
		long tp_fp = 0;
		long tp = 0;
		for (int i = 0, index = 0; i < n1; i++)
		{
			long sum = 0;
			for (final int stop = index + n2; index < stop; index++)
			{
				final int v = table[index];
				sum += v;
				tp += binomialCoefficient2(v);
			}
			if (sum > Integer.MAX_VALUE)
				throw new RuntimeException();
			total += sum;
			tp_fp += binomialCoefficient2(sum);
		}

		if (total > Integer.MAX_VALUE)
			throw new RuntimeException();

		long tp_fn = 0;
		for (int j = 0; j < n2; j++)
		{
			long sum = 0;
			for (int index = j; index < size; index += n2)
				sum += table[index];
			if (sum > Integer.MAX_VALUE)
				throw new RuntimeException();
			tp_fn += binomialCoefficient2(sum);
		}

		final long fp = tp_fp - tp;
		final long fn = tp_fn - tp;
		final long tn = binomialCoefficient2(total) - tp - fp - fn;

		//System.out.printf("%d %d %d %d\n", tp, fp, tn, fn);

		// Note:
		// Use the definitions here:
		// https://en.wikipedia.org/wiki/Rand_index
		// a = tp
		// b = tn
		// c = fp
		// d = fn
		// R = (a+b) / (a+b+c+d)
		// 
		// Adjusted Rand Index
		// sum(nij C 2) = tp 
		// sum(ai C 2)  = tp_fp 
		// sum(bj C 2)  = tp_fn 
		// ARI = (sum(nij C 2) - (sum(ai C 2) * sum(bj C 2))/ nC2) / ((0.5*(sum(ai C 2)+sum(bj C 2))) - (sum(ai C 2)*sum(bj C 2)) / nC2))

		return (double) (tp + tn) / (tp + fp + tn + fn);
	}

	/**
	 * Compute the adjusted Rand index for two classifications of a set of data.
	 * <p>
	 * The adjusted Rand index is the corrected-for-chance version of the Rand index. Though the Rand Index may only
	 * yield a value between 0 and +1, the adjusted Rand index can yield negative values if the index is less than the
	 * expected index.
	 * <p>
	 * Each set should use integers from 0 to n-1 for n clusters. If clusters numbers above n clusters are used for
	 * either set then an {@link ArrayIndexOutOfBoundsException} will occur.
	 * <p>
	 * Warning: No checks are made on the input!
	 *
	 * @param set1
	 *            the first set of clusters for the objects
	 * @param n1
	 *            the number of clusters (max cluster number + 1) in set 1
	 * @param set2
	 *            the second set of clusters for the objects
	 * @param n2
	 *            the number of clusters (max cluster number + 1) in set 2
	 * @return the Rand index
	 * @see https://en.wikipedia.org/wiki/Rand_index
	 * @throws RuntimeException
	 *             if the sum of the contingency table is greater than the max value of an integer
	 */
	public static double adjustedRandIndex(int[] set1, int n1, int[] set2, int n2)
	{
		final int n = set1.length;
		if (n < 2)
			return getDefaultAdjustedRandIndex(n);

		final int size = n1 * n2;
		final int[] table = new int[size];

		for (int i = 0; i < n; i++)
		{
			table[set1[i] * n2 + set2[i]]++;
		}

		long total = 0;
		long tp_fp = 0;
		long tp = 0;
		for (int i = 0, index = 0; i < n1; i++)
		{
			long sum = 0;
			for (final int stop = index + n2; index < stop; index++)
			{
				final int v = table[index];
				sum += v;
				tp += binomialCoefficient2(v);
			}
			if (sum > Integer.MAX_VALUE)
				throw new RuntimeException();
			total += sum;
			tp_fp += binomialCoefficient2(sum);
		}

		if (total > Integer.MAX_VALUE)
			throw new RuntimeException();

		long tp_fn = 0;
		for (int j = 0; j < n2; j++)
		{
			long sum = 0;
			for (int index = j; index < size; index += n2)
				sum += table[index];
			if (sum > Integer.MAX_VALUE)
				throw new RuntimeException();
			tp_fn += binomialCoefficient2(sum);
		}

		// Note:
		// Use the definitions here:
		// https://en.wikipedia.org/wiki/Rand_index
		// 
		// Adjusted Rand Index
		// sum(nij C 2) = tp 
		// sum(ai C 2)  = tp_fp 
		// sum(bj C 2)  = tp_fn 
		// ARI = (sum(nij C 2) - (sum(ai C 2) * sum(bj C 2))/ nC2) / ((0.5*(sum(ai C 2)+sum(bj C 2))) - (sum(ai C 2)*sum(bj C 2)) / nC2))
		//     = (Index - ExpectedIndex) / (MaxIndex - ExpectedIndex)

		long index = tp;
		double expectedIndex = tp_fp * (double) tp_fn / binomialCoefficient2(n);
		double maxIndex = 0.5 * (tp_fp + tp_fn);

		return getAdjustedRandIndex(index, expectedIndex, maxIndex);
	}

	private static double getAdjustedRandIndex(long index, double expectedIndex, double maxIndex)
	{
		// This therefore returns 1 when the sample size is small (n<2).
		// It also returns 1 if a sample of n=2 is used with only 1 cluster. 
		// Q. Is this correct? Perhaps return 0 in that case (i.e. we are no better than random).
		if (index == maxIndex)
			return 1;

		return (index - expectedIndex) / (maxIndex - expectedIndex);
	}

	/**
	 * Compute the contingency table for two classifications of a set of data and generate the values required to
	 * produce the Rand index.
	 * 
	 * @param set1
	 *            the first set of clusters for the objects
	 * @param set2
	 *            the second set of clusters for the objects
	 * @see https://en.wikipedia.org/wiki/Rand_index
	 * @throws RuntimeException
	 *             if the sum of the contingency table is greater than the max value of an integer
	 */
	public void compute(int[] set1, int[] set2)
	{
		tp = tp_fp = tp_fn = 0;

		if (set1 == null)
			throw new NullPointerException("set1");
		if (set2 == null)
			throw new NullPointerException("set2");
		if (set1.length != set2.length)
			throw new IllegalArgumentException("Sets must be the same size");

		final int n = set1.length;
		if (n < 2)
		{
			this.n = n;
			return;
		}

		// Compute using a contingency table.
		// Each set should optimally use integers from 0 to n-1 for n clusters.
		final int[] set1a = new int[n];
		final int[] set2b = new int[n];
		final int max1 = compact(set1, set1a);
		final int max2 = compact(set2, set2b);

		compute(set1a, max1, set2b, max2);
	}

	private int n;
	private long total, tp, tp_fp, tp_fn;

	/**
	 * Compute the contingency table for two classifications of a set of data and generate the values required to
	 * produce the Rand index.
	 * <p>
	 * Each set should use integers from 0 to n-1 for n clusters. If clusters numbers above n clusters are used for
	 * either set then an {@link ArrayIndexOutOfBoundsException} will occur.
	 * <p>
	 * Warning: No checks are made on the input!
	 *
	 * @param set1
	 *            the first set of clusters for the objects
	 * @param n1
	 *            the number of clusters (max cluster number + 1) in set 1
	 * @param set2
	 *            the second set of clusters for the objects
	 * @param n2
	 *            the number of clusters (max cluster number + 1) in set 2
	 * @see https://en.wikipedia.org/wiki/Rand_index
	 * @throws RuntimeException
	 *             if the sum of the contingency table is greater than the max value of an integer
	 */
	public void compute(int[] set1, int n1, int[] set2, int n2)
	{
		n = 0;
		total = tp = tp_fp = tp_fn = 0;

		final int n = set1.length;
		if (n < 2)
		{
			this.n = n;
			return;
		}

		final int size = n1 * n2;
		final int[] table = new int[size];

		for (int i = 0; i < n; i++)
		{
			table[set1[i] * n2 + set2[i]]++;
		}

		long total = 0;
		long tp_fp = 0;
		long tp = 0;
		for (int i = 0, index = 0; i < n1; i++)
		{
			long sum = 0;
			for (final int stop = index + n2; index < stop; index++)
			{
				final int v = table[index];
				sum += v;
				tp += binomialCoefficient2(v);
			}
			if (sum > Integer.MAX_VALUE)
				throw new RuntimeException();
			total += sum;
			tp_fp += binomialCoefficient2(sum);
		}

		if (total > Integer.MAX_VALUE)
			throw new RuntimeException();

		long tp_fn = 0;
		for (int j = 0; j < n2; j++)
		{
			long sum = 0;
			for (int index = j; index < size; index += n2)
				sum += table[index];
			if (sum > Integer.MAX_VALUE)
				throw new RuntimeException();
			tp_fn += binomialCoefficient2(sum);
		}

		// Store after no exceptions are raised
		this.n = n;
		this.total = total;
		this.tp = tp;
		this.tp_fp = tp_fp;
		this.tp_fn = tp_fn;
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
		compute(set1, set2);
		return getRandIndex();
	}

	/**
	 * Compute the Rand index for two classifications of a set of data.
	 * <p>
	 * The Rand index has a value between 0 and 1, with 0 indicating that the two data clusters do not agree on any pair
	 * of points and 1 indicating that the data clusters are exactly the same.
	 * <p>
	 * Compute using a contingency table. Each set should use integers from 0 to n-1 for n clusters. If clusters numbers
	 * above n clusters are used for either set then an {@link ArrayIndexOutOfBoundsException} will occur.
	 * <p>
	 * Warning: No checks are made on the input!
	 *
	 * @param set1
	 *            the first set of clusters for the objects
	 * @param n1
	 *            the number of clusters (max cluster number + 1) in set 1
	 * @param set2
	 *            the second set of clusters for the objects
	 * @param n2
	 *            the number of clusters (max cluster number + 1) in set 2
	 * @return the Rand index
	 * @see https://en.wikipedia.org/wiki/Rand_index
	 * @throws RuntimeException
	 *             if the sum of the contingency table is greater than the max value of an integer
	 */
	public double getRandIndex(int[] set1, int n1, int[] set2, int n2)
	{
		compute(set1, n1, set2, n2);
		return getRandIndex();
	}

	/**
	 * Compute the Rand index for two classifications of a set of data.
	 * <p>
	 * The Rand index has a value between 0 and 1, with 0 indicating that the two data clusters do not agree on any pair
	 * of points and 1 indicating that the data clusters are exactly the same.
	 * <p>
	 * Compute using a contingency table.
	 *
	 * @return the Rand index
	 * @see https://en.wikipedia.org/wiki/Rand_index
	 */
	public double getRandIndex()
	{
		if (n < 2)
			return getDefaultRandIndex(n);

		long fp = getFalsePositives();
		long fn = getFalseNegatives();
		long tn = getTrueNegatives();

		return (double) (tp + tn) / (tp + fp + tn + fn);
	}

	/**
	 * Compute the adjusted Rand index for two classifications of a set of data.
	 * <p>
	 * The adjusted Rand index is the corrected-for-chance version of the Rand index. Though the Rand Index may only
	 * yield a value between 0 and +1, the adjusted Rand index can yield negative values if the index is less than the
	 * expected index.
	 * 
	 * @param set1
	 *            the first set of clusters for the objects
	 * @param set2
	 *            the second set of clusters for the objects
	 * @return the Rand index
	 * @see https://en.wikipedia.org/wiki/Rand_index
	 */
	public double getAdjustedRandIndex(int[] set1, int[] set2)
	{
		compute(set1, set2);
		return getAdjustedRandIndex();
	}

	/**
	 * Compute the adjusted Rand index for two classifications of a set of data.
	 * <p>
	 * The adjusted Rand index is the corrected-for-chance version of the Rand index. Though the Rand Index may only
	 * yield a value between 0 and +1, the adjusted Rand index can yield negative values if the index is less than the
	 * expected index.
	 * <p>
	 * Each set should use integers from 0 to n-1 for n clusters. If clusters numbers above n clusters are used for
	 * either set then an {@link ArrayIndexOutOfBoundsException} will occur.
	 * <p>
	 * Warning: No checks are made on the input!
	 *
	 * @param set1
	 *            the first set of clusters for the objects
	 * @param n1
	 *            the number of clusters (max cluster number + 1) in set 1
	 * @param set2
	 *            the second set of clusters for the objects
	 * @param n2
	 *            the number of clusters (max cluster number + 1) in set 2
	 * @return the Rand index
	 * @see https://en.wikipedia.org/wiki/Rand_index
	 * @throws RuntimeException
	 *             if the sum of the contingency table is greater than the max value of an integer
	 */
	public double getAdjustedRandIndex(int[] set1, int n1, int[] set2, int n2)
	{
		compute(set1, n1, set2, n2);
		return getAdjustedRandIndex();
	}

	/**
	 * Compute the adjusted Rand index for two classifications of a set of data.
	 * <p>
	 * The adjusted Rand index is the corrected-for-chance version of the Rand index. Though the Rand Index may only
	 * yield a value between 0 and +1, the adjusted Rand index can yield negative values if the index is less than the
	 * expected index.
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
	public double getAdjustedRandIndex()
	{
		if (n < 2)
			return getDefaultAdjustedRandIndex(n);

		// Note:
		// Use the definitions here:
		// https://en.wikipedia.org/wiki/Rand_index
		// 
		// Adjusted Rand Index
		// sum(nij C 2) = tp 
		// sum(ai C 2)  = tp_fp 
		// sum(bj C 2)  = tp_fn 
		// ARI = (sum(nij C 2) - (sum(ai C 2) * sum(bj C 2))/ nC2) / ((0.5*(sum(ai C 2)+sum(bj C 2))) - (sum(ai C 2)*sum(bj C 2)) / nC2))
		//     = (Index - ExpectedIndex) / (MaxIndex - ExpectedIndex)

		long index = tp;
		double expectedIndex = tp_fp * (double) tp_fn / binomialCoefficient2(n);
		double maxIndex = 0.5 * (tp_fp + tp_fn);

		return getAdjustedRandIndex(index, expectedIndex, maxIndex);
	}

	/**
	 * Gets the number of elements in the set of data.
	 *
	 * @return the number of elements
	 */
	public int getN()
	{
		return n;
	}

	/**
	 * Gets the true positives from the last call to compute().
	 *
	 * @return the true positives
	 */
	public long getTruePositives()
	{
		return tp;
	}

	/**
	 * Gets the true negatives from the last call to compute().
	 *
	 * @return the true negatives
	 */
	public long getTrueNegatives()
	{
		//long fp = getFalsePositives();
		//long fn = getFalseNegatives();
		//return binomialCoefficient2(total) - tp - fp - fn;

		//return binomialCoefficient2(total) - tp - (tp_fp - tp) - (tp_fn - tp);

		return binomialCoefficient2(total) + tp - tp_fp - tp_fn;
	}

	/**
	 * Gets the false positives from the last call to compute().
	 *
	 * @return the false positives
	 */
	public long getFalsePositives()
	{
		return tp_fp - tp;
	}

	/**
	 * Gets the false negatives from the last call to compute().
	 *
	 * @return the false negatives
	 */
	public long getFalseNegatives()
	{
		return tp_fn - tp;
	}
}
