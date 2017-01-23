package ij.process;

import org.junit.Assert;
import org.junit.Test;

import ij.process.LUTHelper.DefaultLUTMapper;
import ij.process.LUTHelper.LUTMapper;
import ij.process.LUTHelper.NonZeroLUTMapper;

public class LUTHelperTest
{
	@Test
	public void canMapTo0to255()
	{
		mapTo0to255(0, 0);
		mapTo0to255(0, 1);
		mapTo0to255(0, 255);
		mapTo0to255(0, 1000);
		
		mapTo0to255(4.3f, 32.5f);
		mapTo0to255(-4.3f, 0f);
		mapTo0to255(-4.3f, 32.5f);
		mapTo0to255(0f, 32.5f);
	}

	@Test
	public void canMapTo1to255()
	{
		mapTo1to255(1, 1);
		mapTo1to255(1, 2);
		mapTo1to255(1, 255);
		mapTo1to255(1, 1000);
		
		mapTo1to255(4.3f, 32.5f);
		mapTo1to255(-4.3f, 0f);
		mapTo1to255(-4.3f, 32.5f);
		mapTo1to255(0f, 32.5f);
	}

	private void mapTo0to255(float min, float max)
	{
		LUTMapper map = new DefaultLUTMapper(min, max);
		Assert.assertEquals(0, map.map(min));
		if (max != min)
			Assert.assertEquals(255, map.map(max));
	}

	private void mapTo1to255(float min, float max)
	{
		LUTMapper map = new NonZeroLUTMapper(min, max);
		Assert.assertEquals(1, map.map(min));
		if (max != min)
			Assert.assertEquals(255, map.map(max));
	}
}
