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
package ij.process;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import ij.plugin.filter.EDM;
import uk.ac.sussex.gdsc.core.ij.process.FHT2;
import uk.ac.sussex.gdsc.core.utils.SimpleArrayUtils;
import uk.ac.sussex.gdsc.test.junit5.ExtraAssertions;

@SuppressWarnings({ "javadoc" })
public class FHT2Test
{
	@Test
	public void canCheckPowerOf2()
	{
		Assertions.assertFalse(FHT2.isPowerOf2(1), "1");
		Assertions.assertFalse(FHT2.isPowerOf2(Integer.MAX_VALUE), "" + Integer.MAX_VALUE);
		int i = 2;
		while (i > 0) // Until overflow
		{
			ExtraAssertions.assertTrue(FHT2.isPowerOf2(i), "%d", i);
			ExtraAssertions.assertFalse(FHT2.isPowerOf2(i - 1), "%d", (i - 1));
			ExtraAssertions.assertFalse(FHT2.isPowerOf2(i + 1), "%d", (i + 1));
			i *= 2;
		}
	}

	@Test
	public void canConjugateMultiply()
	{
		canCompute(0, false);
	}

	@Test
	public void canMultiply()
	{
		canCompute(1, false);
	}

	@Test
	public void canDivide()
	{
		canCompute(2, false);
	}

	@Test
	public void canFastConjugateMultiply()
	{
		canCompute(0, true);
	}

	@Test
	public void canFastMultiply()
	{
		canCompute(1, true);
	}

	@Test
	public void canFastDivide()
	{
		canCompute(2, true);
	}

	private static void canCompute(int mode, boolean fast)
	{
		final int size = 16;
		final int ex = 5, ey = 7;
		final int ox = 1, oy = 2;
		final FloatProcessor fp1 = createProcessor(size, ex, ey, 4, 4);
		final FloatProcessor fp2 = createProcessor(size, size / 2 + ox, size / 2 + oy, 4, 4);

		// These will duplicate in the constructor
		final FHT fhtA = new FHT(fp1);
		final FHT fhtB = new FHT(fp2);
		fhtA.transform();
		fhtB.transform();

		final FHT2 fht1 = new FHT2(fp1);
		final FHT2 fht2 = new FHT2(fp2);
		fht1.transform();
		fht2.transform();

		FHT fhtE;
		FHT2 fhtO;
		switch (mode)
		{
			case 2:
				fhtE = fhtA.divide(fhtB);
				if (fast)
					fht2.initialiseFastOperations();
				fhtO = fht1.divide(fht2);
				break;
			case 1:
				fhtE = fhtA.multiply(fhtB);
				if (fast)
					fht2.initialiseFastMultiply();
				fhtO = fht1.multiply(fht2);
				break;
			default:
				fhtE = fhtA.conjugateMultiply(fhtB);
				if (fast)
					fht2.initialiseFastMultiply();
				fhtO = fht1.conjugateMultiply(fht2);
				break;
		}
		fhtE.inverseTransform();
		fhtO.inverseTransform();

		final float[] e = (float[]) fhtE.getPixels();
		final float[] o = (float[]) fhtO.getPixels();

		// This is not exact for the divide since the FHT2 magnitude is computed
		// using double*double + double*double rather than float*float + float*float,
		// i.e. the float are converted to double before multiplication.
		if (mode == 2)
			ExtraAssertions.assertArrayEqualsRelative(e, o, 1e-5);
		else
			Assertions.assertArrayEquals(e, o);
	}

	private static FloatProcessor createProcessor(int size, int x, int y, int w, int h)
	{
		final ByteProcessor bp = new ByteProcessor(size, size);
		bp.setColor(255);
		bp.fillOval(x, y, w, h);
		final EDM e = new EDM();
		return e.makeFloatEDM(bp, 0, true);
	}

	@Test
	public void canSwapQuadrants()
	{
		final int size = 16;
		final FloatProcessor fp1 = new FloatProcessor(size, size, SimpleArrayUtils.newArray(size * size, 0, 1f));
		final FloatProcessor fp2 = (FloatProcessor) fp1.duplicate();

		final FHT fht1 = new FHT(fp1);
		final FHT2 fht2 = new FHT2(fp2);

		fht1.swapQuadrants();
		fht2.swapQuadrants();

		Assertions.assertArrayEquals((float[]) fp1.getPixels(), (float[]) fp2.getPixels());
	}
}
