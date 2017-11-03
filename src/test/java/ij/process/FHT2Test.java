package ij.process;

import org.junit.Assert;
import org.junit.Test;

import gdsc.core.utils.SimpleArrayUtils;
import ij.plugin.filter.EDM;

public class FHT2Test
{
	@Test
	public void canCrossCorrelate()
	{
		int size = 16;
		int ex = 5, ey = 7;
		int ox = 1, oy = 2;
		FloatProcessor fp1 = createProcessor(size, ex, ey, 4, 4);
		// This must be offset from the centre
		FloatProcessor fp2 = createProcessor(size, size / 2 + ox, size / 2 + oy, 4, 4);

		FHT2 fht1 = new FHT2(fp1);
		fht1.transform();
		FHT2 fht2 = new FHT2(fp2);
		fht2.transform();

		FHT2 fhtE = fht1.conjugateMultiply(fht2);
		fhtE.inverseTransform();
		fhtE.swapQuadrants();

		float[] e = (float[]) fhtE.getPixels();
		int max = SimpleArrayUtils.findMaxIndex(e);
		int x = max % 16;
		int y = max / 16;

		Assert.assertEquals(ex, x + ox);
		Assert.assertEquals(ey, y + oy);
	}

	private FloatProcessor createProcessor(int size, int x, int y, int w, int h)
	{
		ByteProcessor bp = new ByteProcessor(size, size);
		bp.setColor(255);
		bp.fillOval(x, y, w, h);
		EDM e = new EDM();
		return e.makeFloatEDM(bp, 0, true);
	}

	@Test
	public void canSwapQuadrants()
	{
		int size = 16;
		FloatProcessor fp1 = new FloatProcessor(size, size, SimpleArrayUtils.newArray(size * size, 0, 1f));
		FloatProcessor fp2 = (FloatProcessor) fp1.duplicate();

		FHT fht1 = new FHT();
		FHT2 fht2 = new FHT2();

		fht1.swapQuadrants(fp1);
		fht2.swapQuadrants(fp2);

		Assert.assertArrayEquals((float[]) fp1.getPixels(), (float[]) fp2.getPixels(), 0);
	}
}
