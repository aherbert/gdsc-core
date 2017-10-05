package gdsc.core.data;

import org.junit.Assert;
import org.junit.Test;

import gdsc.core.utils.SimpleArrayUtils;
import ij.process.FloatProcessor;

public class FloatStackTrivalueProviderTest
{
	@Test
	public void canProvideData()
	{
		int maxx = 5, maxy = 4, maxz = 3;
		int size = maxx * maxy;
		float[][] data = new float[maxz][];
		for (int z = 0; z < maxz; z++)
			data[z] = SimpleArrayUtils.toFloat(SimpleArrayUtils.newArray(size, z, 1.0));

		FloatStackTrivalueProvider f = new FloatStackTrivalueProvider(data, maxx, maxy);

		double[][][] values = new double[3][3][3];

		int[] test = { -1, 0, 1 };

		// Test with FloatProcessor as that is the likely source of the stack of data
		for (int z = 0; z < maxz; z++)
		{
			FloatProcessor fp = new FloatProcessor(maxx, maxy, data[z]);
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

						for (int i : test)
							for (int j : test)
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
		int maxx = 5, maxy = 4, maxz = 3;
		int size = maxx * maxy;
		float[][] data = new float[maxz][];
		for (int z = 0; z < maxz; z++)
			data[z] = SimpleArrayUtils.toFloat(SimpleArrayUtils.newArray(size, z, (z + 1) * 2.0));
		FloatStackTrivalueProvider f = new FloatStackTrivalueProvider(data, maxx, maxy);
		double[][][] e = new double[maxx][maxy][maxz];
		for (int x = 0; x < maxx; x++)
			for (int y = 0; y < maxy; y++)
				for (int z = 0; z < maxz; z++)
					e[x][y][z] = f.get(x, y, z);
		double[][][] o = f.toArray();
		for (int x = 0; x < maxx; x++)
			for (int y = 0; y < maxy; y++)
				for (int z = 0; z < maxz; z++)
					Assert.assertEquals(e[x][y][z], o[x][y][z], 0);
	}
}
