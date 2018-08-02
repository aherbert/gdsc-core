package uk.ac.sussex.gdsc.core.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings({ "javadoc" })
public class RandomTest
{
	@Test
	public void canComputeSample()
	{
		final int[] set = new int[] { 0, 1, 2, 5, 8, 9, 10 };
		final Random r = new Random(30051977);
		for (final int n : set)
			for (final int k : set)
				canComputeSample(r, k, n);
	}

	@Test
	public void canComputeSampleFromBigData()
	{
		final Random r = new Random(30051977);
		final int n = 100;
		for (final int k : new int[] { 0, 1, 2, n / 2, n - 2, n - 1, n })
			canComputeSample(r, k, n);
	}

	private static void canComputeSample(Random r, int k, int n)
	{
		final int[] sample = r.sample(k, n);
		//TestLog.debug(logger,"%d from %d = %s", k, n, java.util.Arrays.toString(sample));
		Assertions.assertEquals(Math.min(k, n), sample.length);
		for (int i = 0; i < sample.length; i++)
			for (int j = i + 1; j < sample.length; j++)
				Assertions.assertNotEquals(sample[i], sample[j]);
	}
}
