package gdsc.core.data;

import org.junit.Assert;
import org.junit.Test;

public class DoubleArrayTrivalueProviderTest
{
	@Test
	public void canProvideData()
	{
		int maxx = 5, maxy = 4, maxz = 3;
		double[][][] data = new double[maxx][maxy][maxz];
		for (int x = 0, i = 0; x < maxx; x++)
			for (int y = 0; y < maxy; y++)
				for (int z = 0; z < maxz; z++)
					data[x][y][z] = i++;

		DoubleArrayTrivalueProvider f = new DoubleArrayTrivalueProvider(data);

		double[][][] values = new double[3][3][3];

		int[] test = { -1, 0, 1 };

		for (int x = 0; x < maxx; x++)
			for (int y = 0; y < maxy; y++)
				for (int z = 0; z < maxz; z++)
				{
					Assert.assertEquals(data[x][y][z], f.get(x, y, z), 0);

					if (x > 0 && x < x - 1 && y > 0 && y < y - 1 && z > 0 && z < maxz - 1)
					{
						f.get(x, y, z, values);

						for (int i : test)
							for (int j : test)
								for (int k : test)
								{
									Assert.assertEquals(data[x + i][y + j][z + k], values[i + 1][j + 1][k + 1], 0);
								}
					}
				}
	}
}
