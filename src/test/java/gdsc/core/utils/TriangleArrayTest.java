package gdsc.core.utils;

import org.junit.Assert;
import org.junit.Test;

public class TriangleArrayTest
{
	int[] testN = new int[] { 0, 1, 2, 5 };

	@Test
	public void canComputeIndex()
	{
		for (int n : testN)
		{
			TriangleArray a = new TriangleArray(n);

			int[] count = new int[a.getLength()];
			int[] ij = new int[2];

			for (int i = 0; i < n; i++)
				for (int j = i + 1; j < n; j++)
				{
					int k = a.toIndex(i, j);
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
		int n = 10;
		TriangleArray a = new TriangleArray(n);

		for (int i = 0; i < n; i++)
			for (int j = i + 1; j < n; j++)
			{
				int k = a.toIndex(i, j);
				int k2 = a.toIndex(j, i);
				if (k == k2)
					continue;
				return;
			}

		Assert.fail();
	}

	@Test
	public void safeIndexIsReversible()
	{
		int n = 10;
		TriangleArray a = new TriangleArray(n);

		for (int i = 0; i < n; i++)
			for (int j = i + 1; j < n; j++)
			{
				int k = a.toIndex(i, j);
				int k2 = a.toSafeIndex(j, i);
				Assert.assertEquals(k, k2);
			}
	}

	@Test
	public void canFastComputePostIndex()
	{
		for (int n : testN)
		{
			TriangleArray a = new TriangleArray(n);

			for (int i = 0; i < n; i++)
				for (int j = i + 1, index = a.toIndex(i, j); j < n; j++, index++)
				{
					int k = a.toIndex(i, j);
					Assert.assertEquals("i", k, index);
				}
		}
	}

	@Test
	public void canFastComputePreIndex()
	{
		for (int n : testN)
		{
			TriangleArray a = new TriangleArray(n);

			for (int j = n; j-- > 0;)
				for (int i = j, index = a.toPrecursorIndex(j); i-- > 0;)
				{
					int k = a.toIndex(i, j);
					int k2 = a.precursorToIndex(index, i);
					Assert.assertEquals("i", k, k2);
				}
		}
	}

	@Test
	public void canFastIterateNxN()
	{
		for (int n : testN)
		{
			TriangleArray a = new TriangleArray(n);

			for (int i = 0; i < n; i++)
			{
				for (int j = 0, precursor = a.toPrecursorIndex(i); j < i; j++)
				{
					int k = a.toSafeIndex(i, j);
					int k2 = a.precursorToIndex(precursor, j);
					Assert.assertEquals(k, k2);
				}
				for (int j = i + 1, index = a.toIndex(i, j); j < n; j++, index++)
				{
					int k = a.toSafeIndex(i, j);
					Assert.assertEquals(k, index);
				}
			}
		}
	}

	@Test
	public void canCompareItoAnyJ()
	{
		for (int n : testN)
		{
			TriangleArray a = new TriangleArray(n);

			for (int i = 0; i < n; i++)
			{
				a.setup(i);
				for (int j = 0; j < n; j++)
				{
					if (i == j)
						continue;
					int k = a.toSafeIndex(i, j);
					int k2 = a.toIndex(j);
					Assert.assertEquals(k, k2);
				}
			}

		}
	}
}
