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
package gdsc.core.clustering.optics;

import org.junit.Assert;
import org.junit.Test;

import gdsc.core.utils.Maths;
import gdsc.test.TestSettings;
import gdsc.test.TestSettings.TestComplexity;

@SuppressWarnings({ "javadoc" })
public class CircularKernelOffsetTest
{
	@Test
	public void canBuildCircularKernelAtDifferentResolutions()
	{
		// Note: The radius of the default circle is 1 =>
		// Circle Area = pi
		// Square Area = 4

		final int max = (TestSettings.allow(TestComplexity.LOW)) ? 100 : 10;

		for (int r = 1; r <= max; r++)
		{
			final CircularKernelOffset[] offset = CircularKernelOffset.create(r);
			final int size = offset.length * offset.length;
			final double pixelArea = 4.0 / (size);
			// Count pixels for the outer/inner circles
			int outer = 0, inner = 0;
			for (final CircularKernelOffset o : offset)
			{
				outer += Math.max(0, o.end - o.start);
				if (o.internal)
					inner += o.endInternal - o.startInternal;
			}
			final double outerArea = outer * pixelArea;
			final double innerArea = inner * pixelArea;
			final int skip = size - outer;
			TestSettings.info("R=%d, outer=%d  %f (%f), Skip=%d  (%f), inner=%d  %f (%f)\n", r, outer, outerArea,
					outerArea / Math.PI, skip, (double) skip / size, inner, innerArea, innerArea / outerArea);

			// Test for symmetry
			final int w = offset.length;
			final boolean[] outerMask = new boolean[w * w];
			final boolean[] innerMask = new boolean[outerMask.length];
			for (int i = 0, k = 0; i < offset.length; i++)
				for (int j = -r; j <= r; j++, k++)
				{
					if (j >= offset[i].start && j < offset[i].end)
						outerMask[k] = true;
					if (j >= offset[i].startInternal && j < offset[i].endInternal)
						innerMask[k] = true;
				}
			for (int y = 0, k = 0; y < w; y++)
				for (int x = 0; x < w; x++, k++)
					Assert.assertTrue("No outer symmetry", outerMask[k] == outerMask[x * w + y]);
			final double e = r * r;
			for (int y = 0, k = 0; y < w; y++)
				for (int x = 0; x < w; x++, k++)
				{
					Assert.assertTrue("No inner symmetry", innerMask[k] == innerMask[x * w + y]);
					// Test distance to centre (r,r)
					if (innerMask[k])
						Assert.assertTrue("Bad inner", Maths.distance2(x, y, r, r) <= e);
				}
		}
	}
}
