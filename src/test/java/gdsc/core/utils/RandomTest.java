package gdsc.core.utils;

import org.junit.Assert;
import org.junit.Test;

public class RandomTest
{
	@Test
	public void canComputeSample()
	{
		int[] set = new int[] { 0, 1, 2, 5, 8, 9, 10 };
		Random r = new Random(30051977);
		for (int n : set)
		{
			for (int k : set)
			{
				canComputeSample(r, k, n);
			}
		}
	}

	@Test
	public void canComputeSampleFromBigData()
	{
		Random r = new Random(30051977);
		int n = 100;
		for (int k : new int[] { 0, 1, 2, n / 2, n - 2, n - 1, n })
		{
			canComputeSample(r, k, n);
		}
	}

	private void canComputeSample(Random r, int k, int n)
	{
		int[] sample = r.sample(k, n);
		//System.out.printf("%d from %d = %s\n", k, n, java.util.Arrays.toString(sample));
		Assert.assertEquals(Math.min(k, n), sample.length);
		for (int i = 0; i < sample.length; i++)
			for (int j = i + 1; j < sample.length; j++)
				Assert.assertNotEquals(sample[i], sample[j]);
	}
}
