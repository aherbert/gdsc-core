package gdsc.core.math.interpolation;

import org.junit.Test;

public class DoubleCustomTricubicFunctionTest
{
	@Test
	public void canConstructInlineValue()
	{
		System.out.println(DoubleCustomTricubicFunction.inlineValue());
	}

	@Test
	public void canConstructInlineComputePowerTable()
	{
		System.out.println(DoubleCustomTricubicFunction.inlineComputePowerTable());
	}

	@Test
	public void canConstructInlineValue1()
	{
		System.out.println(DoubleCustomTricubicFunction.inlineValue1());
	}

	@Test
	public void canConstructInlineValue1WithPowerTable()
	{
		System.out.println(DoubleCustomTricubicFunction.inlineValue1WithPowerTable());
	}
	
	@Test
	public void canConstructInlineValue2()
	{
		System.out.println(DoubleCustomTricubicFunction.inlineValue2());
	}

	@Test
	public void canConstructInlineValue2WithPowerTable()
	{
		System.out.println(DoubleCustomTricubicFunction.inlineValue2WithPowerTable());
	}
}
