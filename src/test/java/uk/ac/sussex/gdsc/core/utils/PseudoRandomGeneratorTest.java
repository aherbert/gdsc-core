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

import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;import uk.ac.sussex.gdsc.test.junit5.SeededTest;import uk.ac.sussex.gdsc.test.junit5.RandomSeed;import uk.ac.sussex.gdsc.test.junit5.SpeedTag;

import uk.ac.sussex.gdsc.test.TestSettings;
import uk.ac.sussex.gdsc.test.junit5.RandomSeed;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;import uk.ac.sussex.gdsc.test.junit5.RandomSeed;

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

	@SeededTest
	public void canConstructPseudoRandomGeneratorFromSource(RandomSeed seed)
	{
		final UniformRandomProvider source = TestSettings.getRandomGenerator(seed.getSeed());
		final int length = 5;
		final double[] e = new double[length];
		for (int i = 0; i < e.length; i++)
			e[i] = source.nextDouble();
		final PseudoRandomGenerator r = new PseudoRandomGenerator(length, 
				new RandomGeneratorAdapter(TestSettings.getRandomGenerator(seed.getSeed())));
		canConstructPseudoRandomGenerator(r, e);
	}

	private static void canConstructPseudoRandomGenerator(PseudoRandomGenerator r, double[] e)
	{
		for (int i = 0; i < e.length; i++)
			Assertions.assertEquals(e[i], r.nextDouble());
		// Repeat
		for (int i = 0; i < e.length; i++)
			Assertions.assertEquals(e[i], r.nextDouble());
	}
}
