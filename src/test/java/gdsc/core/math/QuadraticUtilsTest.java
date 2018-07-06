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
package gdsc.core.math;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.Assert;
import org.junit.Test;

import gdsc.core.data.DataException;
import gdsc.test.TestAssert;
import gdsc.test.TestSettings;

public class QuadraticUtilsTest
{
	@Test
	public void canGetDeterminant3x3()
	{
		RandomGenerator r = TestSettings.getRandomGenerator();
		for (int i = 0; i < 5; i++)
		{
			double[] m = new double[9];
			for (int j = 0; j < 9; j++)
				m[j] = -5 + r.nextDouble() * 10;

			double e = QuadraticUtils.getDeterminant3x3(m, 1);
			for (int j = 0; j < 3; j++)
			{
				double scale = r.nextDouble() * 100;
				double o = QuadraticUtils.getDeterminant3x3(m, scale);
				//System.out.printf("[%d] scale %.2f = %s vs %s\n", i, scale, e, o);
				TestAssert.assertEqualsRelative(e, o, 1e-6);
			}
		}
	}

	@Test
	public void canSolveQuadratic()
	{
		double a = 3;
		double b = -2;
		double c = -4;
		double[] e = new double[] { a, b, c };

		RandomGenerator r = TestSettings.getRandomGenerator();
		for (int i = 0; i < 5; i++)
		{
			// Avoid identical points
			double x1 = -5 + r.nextDouble() * 10;
			double x2 = -5 + r.nextDouble() * 10;
			while (x2 == x1)
				x2 = -5 + r.nextDouble() * 10;
			double x3 = -5 + r.nextDouble() * 10;
			while (x3 == x1 || x3 == x2)
				x3 = -5 + r.nextDouble() * 10;
			
			// Order invariant
			canSolveQuadratic(a, b, c, e, x1, x2, x3);
			canSolveQuadratic(a, b, c, e, x1, x3, x2);
			canSolveQuadratic(a, b, c, e, x2, x1, x3);
			canSolveQuadratic(a, b, c, e, x2, x3, x1);
			canSolveQuadratic(a, b, c, e, x3, x1, x2);
			canSolveQuadratic(a, b, c, e, x3, x2, x1);
		}
	}

	private void canSolveQuadratic(double a, double b, double c, double[] e, double x1, double x2, double x3)
	{
		double[] o = solveQuadratic(a, b, c, x1, x2, x3);
		Assert.assertNotNull(o);
		//System.out.println(java.util.Arrays.toString(o));
		TestAssert.assertDoubleArrayEqualsRelative(e, o, 1e-6);
	}

	private double[] solveQuadratic(double a, double b, double c, double x1, double x2, double x3)
	{
		double y1 = a * x1 * x1 + b * x1 + c;
		double y2 = a * x2 * x2 + b * x2 + c;
		double y3 = a * x3 * x3 + b * x3 + c;
		return QuadraticUtils.solve(x1, y1, x2, y2, x3, y3);
	}
	
	@Test
	public void solveUsingColocatedPointsReturnsNull()
	{
		double a = 3;
		double b = -2;
		double c = -4;
		Assert.assertNull(solveQuadratic(a, b, c, -1, 0, 0));
		Assert.assertNull(solveQuadratic(a, b, c, -1, -1, 0));
		Assert.assertNull(solveQuadratic(a, b, c, 0, -1, 0));
	}

	@Test
	public void canFindMinMaxQuadratic()
	{
		TestAssert.assertEqualsRelative(0, findMinMaxQuadratic(1, 0, 0, -1, 0, 1), 1e-6);
		TestAssert.assertEqualsRelative(0, findMinMaxQuadratic(1, 0, -10, -1, 0, 1), 1e-6);
		TestAssert.assertEqualsRelative(-1, findMinMaxQuadratic(1, 2, 0, -1, 0, 1), 1e-6);
		TestAssert.assertEqualsRelative(-1, findMinMaxQuadratic(1, 2, -10, -1, 0, 1), 1e-6);
	}

	private double findMinMaxQuadratic(double a, double b, double c, double x1, double x2, double x3)
	{
		double y1 = a * x1 * x1 + b * x1 + c;
		double y2 = a * x2 * x2 + b * x2 + c;
		double y3 = a * x3 * x3 + b * x3 + c;
		return QuadraticUtils.findMinMax(x1, y1, x2, y2, x3, y3);
	}
	
	@Test(expected=DataException.class)
	public void findMinMaxUsingColocatedPointsThrows()
	{
		double a = 3;
		double b = -2;
		double c = -4;
		findMinMaxQuadratic(a, b, c, -1, 0, 0);
	}
	
	@Test(expected=DataException.class)
	public void findMinMaxUsingColinearPointsThrows()
	{
		double a = 0;
		double b = 1;
		double c = 0;
		findMinMaxQuadratic(a, b, c, -1, 0, 1);
	}
}