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
package gdsc.core.data;

import org.junit.Assert;
import org.junit.Test;

import gdsc.core.utils.SimpleArrayUtils;
import ij.process.FloatProcessor;

@SuppressWarnings({ "javadoc" })
public class FloatStackTrivalueProviderTest
{
	@SuppressWarnings("null")
	@Test
	public void canProvideData()
	{
		final int maxx = 5, maxy = 4, maxz = 3;
		final int size = maxx * maxy;
		final float[][] data = new float[maxz][];
		for (int z = 0; z < maxz; z++)
			data[z] = SimpleArrayUtils.toFloat(SimpleArrayUtils.newArray(size, z, 1.0));

		final FloatStackTrivalueProvider f = new FloatStackTrivalueProvider(data, maxx, maxy);

		final double[][][] values = new double[3][3][3];

		final int[] test = { -1, 0, 1 };

		// Test with FloatProcessor as that is the likely source of the stack of data
		for (int z = 0; z < maxz; z++)
		{
			final FloatProcessor fp = new FloatProcessor(maxx, maxy, data[z]);
			FloatProcessor fpp = null, fpn = null;
			if (z > 0 && z < maxz - 1)
			{
				fpp = new FloatProcessor(maxx, maxy, data[z - 1]);
				fpn = new FloatProcessor(maxx, maxy, data[z + 1]);
			}

			for (int y = 0; y < maxy; y++)
				for (int x = 0; x < maxx; x++)
				{
					Assert.assertEquals(fp.getPixelValue(x, y), f.get(x, y, z), 0);

					if (x > 0 && x < maxx - 1 && y > 0 && y < maxy - 1 && fpp != null)
					{
						f.get(x, y, z, values);

						for (final int i : test)
							for (final int j : test)
							{
								Assert.assertEquals(fpp.getPixelValue(x + i, y + j), values[i + 1][j + 1][0], 0);
								Assert.assertEquals(fp.getPixelValue(x + i, y + j), values[i + 1][j + 1][1], 0);
								Assert.assertEquals(fpn.getPixelValue(x + i, y + j), values[i + 1][j + 1][2], 0);
							}
					}
				}
		}
	}

	@Test
	public void canConvertToArray()
	{
		final int maxx = 5, maxy = 4, maxz = 3;
		final int size = maxx * maxy;
		final float[][] data = new float[maxz][];
		for (int z = 0; z < maxz; z++)
			data[z] = SimpleArrayUtils.toFloat(SimpleArrayUtils.newArray(size, z, (z + 1) * 2.0));
		final FloatStackTrivalueProvider f = new FloatStackTrivalueProvider(data, maxx, maxy);
		final double[][][] e = new double[maxx][maxy][maxz];
		for (int x = 0; x < maxx; x++)
			for (int y = 0; y < maxy; y++)
				for (int z = 0; z < maxz; z++)
					e[x][y][z] = f.get(x, y, z);
		final double[][][] o = f.toArray();
		for (int x = 0; x < maxx; x++)
			for (int y = 0; y < maxy; y++)
				for (int z = 0; z < maxz; z++)
					Assert.assertEquals(e[x][y][z], o[x][y][z], 0);
	}
}
