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
}
