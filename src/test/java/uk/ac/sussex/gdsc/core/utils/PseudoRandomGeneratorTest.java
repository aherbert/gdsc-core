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
package uk.ac.sussex.gdsc.core.utils;

import java.util.Arrays;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.Assert;
import org.junit.Test;

import uk.ac.sussex.gdsc.test.TestSettings;

@SuppressWarnings({ "javadoc" })
public class PseudoRandomGeneratorTest
{
	@Test
	public void canConstructPseudoRandomGeneratorFromSequence()
	{
		final double[] e = new double[] { 0.2, 0.87, 0.45, 0.99 };
		final PseudoRandomGenerator r = new PseudoRandomGenerator(e);
		canConstructPseudoRandomGenerator(r, e);
	}

	@Test
	public void canConstructPseudoRandomGeneratorFromSequenceLength()
	{
		double[] e = new double[] { 0.2, 0.87, 0.45, 0.99 };
		final int length = e.length - 1;
		final PseudoRandomGenerator r = new PseudoRandomGenerator(e, length);
		e = Arrays.copyOf(e, length);
		canConstructPseudoRandomGenerator(r, e);
	}

	@Test
	public void canConstructPseudoRandomGeneratorFromSource()
	{
		final RandomGenerator source = TestSettings.getRandomGenerator();
		final int length = 5;
		final double[] e = new double[length];
		for (int i = 0; i < e.length; i++)
			e[i] = source.nextDouble();
		final PseudoRandomGenerator r = new PseudoRandomGenerator(length, TestSettings.getRandomGenerator());
		canConstructPseudoRandomGenerator(r, e);
	}

	private static void canConstructPseudoRandomGenerator(PseudoRandomGenerator r, double[] e)
	{
		for (int i = 0; i < e.length; i++)
			Assert.assertEquals(e[i], r.nextDouble(), 0);
		// Repeat
		for (int i = 0; i < e.length; i++)
			Assert.assertEquals(e[i], r.nextDouble(), 0);
	}
}
