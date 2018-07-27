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
package uk.ac.sussex.gdsc.core.data;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;import uk.ac.sussex.gdsc.test.junit5.SeededTest;import uk.ac.sussex.gdsc.test.junit5.RandomSeed;import uk.ac.sussex.gdsc.test.junit5.SpeedTag;

@SuppressWarnings({ "javadoc" })
public class DoubleArrayTrivalueProviderTest
{
	@Test
	public void canProvideData()
	{
		final int maxx = 5, maxy = 4, maxz = 3;
		final double[][][] data = new double[maxx][maxy][maxz];
		for (int x = 0, i = 0; x < maxx; x++)
			for (int y = 0; y < maxy; y++)
				for (int z = 0; z < maxz; z++)
					data[x][y][z] = i++;

		final DoubleArrayTrivalueProvider f = new DoubleArrayTrivalueProvider(data);

		final double[][][] values = new double[3][3][3];

		final int[] test = { -1, 0, 1 };

		for (int x = 0; x < maxx; x++)
			for (int y = 0; y < maxy; y++)
				for (int z = 0; z < maxz; z++)
				{
					Assertions.assertEquals(data[x][y][z], f.get(x, y, z));

					if (x > 0 && x < x - 1 && y > 0 && y < y - 1 && z > 0 && z < maxz - 1)
					{
						f.get(x, y, z, values);

						for (final int i : test)
							for (final int j : test)
								for (final int k : test)
									Assertions.assertEquals(data[x + i][y + j][z + k], values[i + 1][j + 1][k + 1]);
					}
				}
	}
}
