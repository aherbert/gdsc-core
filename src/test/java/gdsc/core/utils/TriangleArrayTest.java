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
package gdsc.core.utils;

import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings({ "javadoc" })
public class TriangleArrayTest
{
	int[] testN = new int[] { 0, 1, 2, 5 };

	@Test
	public void canComputeIndex()
	{
		for (final int n : testN)
		{
			final TriangleArray a = new TriangleArray(n);

			final int[] count = new int[a.getLength()];
			int[] ij = new int[2];

			for (int i = 0; i < n; i++)
				for (int j = i + 1; j < n; j++)
				{
					final int k = a.toIndex(i, j);
					count[k]++;

					ij = a.fromIndex(k);
					Assert.assertEquals("i", i, ij[0]);
					Assert.assertEquals("j", j, ij[1]);

					a.fromIndex(k, ij);
					Assert.assertEquals("i", i, ij[0]);
					Assert.assertEquals("j", j, ij[1]);

					ij = TriangleArray.fromIndex(n, k);
					Assert.assertEquals("i", i, ij[0]);
					Assert.assertEquals("j", j, ij[1]);

					TriangleArray.fromIndex(n, k, ij);
					Assert.assertEquals("i", i, ij[0]);
					Assert.assertEquals("j", j, ij[1]);
				}
			for (int i = count.length; i-- > 0;)
				Assert.assertEquals("count", 1, count[i]);
		}
	}

	@Test
	public void indexNotReversible()
	{
		final int n = 10;
		final TriangleArray a = new TriangleArray(n);

		for (int i = 0; i < n; i++)
			for (int j = i + 1; j < n; j++)
			{
				final int k = a.toIndex(i, j);
				final int k2 = a.toIndex(j, i);
				if (k == k2)
					continue;
				return;
			}

		Assert.fail();
	}

	@Test
	public void safeIndexIsReversible()
	{
		final int n = 10;
		final TriangleArray a = new TriangleArray(n);

		for (int i = 0; i < n; i++)
			for (int j = i + 1; j < n; j++)
			{
				final int k = a.toIndex(i, j);
				final int k2 = a.toSafeIndex(j, i);
				Assert.assertEquals(k, k2);
			}
	}

	@Test
	public void canFastComputePostIndex()
	{
		for (final int n : testN)
		{
			final TriangleArray a = new TriangleArray(n);

			for (int i = 0; i < n; i++)
				for (int j = i + 1, index = a.toIndex(i, j); j < n; j++, index++)
				{
					final int k = a.toIndex(i, j);
					Assert.assertEquals("i", k, index);
				}
		}
	}

	@Test
	public void canFastComputePreIndex()
	{
		for (final int n : testN)
		{
			final TriangleArray a = new TriangleArray(n);

			for (int j = n; j-- > 0;)
				for (int i = j, index = a.toPrecursorIndex(j); i-- > 0;)
				{
					final int k = a.toIndex(i, j);
					final int k2 = a.precursorToIndex(index, i);
					Assert.assertEquals("i", k, k2);
				}
		}
	}

	@Test
	public void canFastIterateNxN()
	{
		for (final int n : testN)
		{
			final TriangleArray a = new TriangleArray(n);

			for (int i = 0; i < n; i++)
			{
				for (int j = 0, precursor = a.toPrecursorIndex(i); j < i; j++)
				{
					final int k = a.toSafeIndex(i, j);
					final int k2 = a.precursorToIndex(precursor, j);
					Assert.assertEquals(k, k2);
				}
				for (int j = i + 1, index = a.toIndex(i, j); j < n; j++, index++)
				{
					final int k = a.toSafeIndex(i, j);
					Assert.assertEquals(k, index);
				}
			}
		}
	}

	@Test
	public void canCompareItoAnyJ()
	{
		for (final int n : testN)
		{
			final TriangleArray a = new TriangleArray(n);

			for (int i = 0; i < n; i++)
			{
				a.setup(i);
				for (int j = 0; j < n; j++)
				{
					if (i == j)
						continue;
					final int k = a.toSafeIndex(i, j);
					final int k2 = a.toIndex(j);
					Assert.assertEquals(k, k2);
				}
			}

		}
	}
}
