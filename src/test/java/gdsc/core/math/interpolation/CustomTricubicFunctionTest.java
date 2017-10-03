package gdsc.core.math.interpolation;

import org.junit.Test;

public class CustomTricubicFunctionTest
{
	@Test
	public void canConstructInlineValue()
	{
		System.out.println(CustomTricubicFunction.inlineValue());
	}

	@Test
	public void canConstructInlineComputePowerTable()
	{
		System.out.println(CustomTricubicFunction.inlineComputePowerTable());
	}

	@Test
	public void canConstructInlineValue1()
	{
		System.out.println(CustomTricubicFunction.inlineValue1());
	}

	@Test
	public void canConstructInlineValue1WithPowerTable()
	{
		System.out.println(CustomTricubicFunction.inlineValue1WithPowerTable());
	}
	
	@Test
	public void canConstructInlineValue2()
	{
		System.out.println(CustomTricubicFunction.inlineValue2());
	}

	@Test
	public void canConstructInlineValue2WithPowerTable()
	{
		System.out.println(CustomTricubicFunction.inlineValue2WithPowerTable());
	}
}
