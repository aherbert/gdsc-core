package uk.ac.sussex.gdsc.core.data;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
