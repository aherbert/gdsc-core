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

import org.junit.Assert;
import org.junit.Test;

public class GeometryTest
{
	@Test
	public void canComputeArea()
	{
		// Area is signed
		canComputeArea(0.5, true, 0, 0, 1, 0, 1, 1);
		canComputeArea(-0.5, true, 0, 0, 1, 1, 1, 0);
		canComputeArea(0.5, false, 0, 0, 1, 1, 1, 0);

		canComputeArea(1, true, 0, 0, 1, 0, 1, 1, 0, 1);
	}

	private void canComputeArea(double e, boolean signed, double... vertices)
	{
		double[] x = new double[vertices.length / 2];
		double[] y = new double[x.length];
		for (int i = 0, j = 0; i < vertices.length; i += 2, j++)
		{
			x[j] = vertices[i];
			y[j] = vertices[i + 1];
		}
		double o = Geometry.getArea(x, y);
		if (!signed)
			o = Math.abs(o);
		Assert.assertEquals(e, o, 1e-10);
	}

	@Test
	public void canComputeIntersection()
	{
		canComputeIntersection(null, 0, 0, 1, 0, 0, 1, 1, 1);
		canComputeIntersection(new double[] { 0.5, 0.5 }, 0, 0, 1, 1, 1, 0, 0, 1);
		canComputeIntersection(new double[] { 0, 0 }, 0, 0, 1, 1, 0, 0, 0, 1);
	}

	private void canComputeIntersection(double[] e, double x1, double y1, double x2, double y2, double x3, double y3,
			double x4, double y4)
	{
		double[] o = new double[2];
		boolean result = Geometry.getIntersection(x1, y1, x2, y2, x3, y3, x4, y4, o);
		if (e == null)
		{
			Assert.assertFalse(result);
		}
		else
		{
			Assert.assertTrue(result);
			Assert.assertArrayEquals(e, o, 1e-10);
		}
	}
}
