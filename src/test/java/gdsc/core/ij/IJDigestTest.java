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
package gdsc.core.ij;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.Assert;
import org.junit.Test;

import gdsc.core.utils.Digest;
import gdsc.test.TestSettings;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ShortProcessor;

@SuppressWarnings({ "javadoc" })
public class IJDigestTest
{
	int size = 50;

	@Test
	public void canDigestByteProcessor()
	{
		final RandomGenerator r = TestSettings.getRandomGenerator();
		final byte[] data = new byte[size];
		r.nextBytes(data);

		final String o = new IJDigest().digest(new ByteProcessor(size, 1, data));
		final String e = Digest.md5Hex(data);
		Assert.assertEquals(e, o);
	}

	@Test
	public void canDigestShortProcessor() throws IOException
	{
		final RandomGenerator r = TestSettings.getRandomGenerator();
		final short[] data = new short[size];
		for (int i = 0; i < size; i++)
			data[i] = (short) ((r.nextDouble() - 0.5) * 2 * Short.MAX_VALUE);

		final String o = new IJDigest().digest(new ShortProcessor(size, 1, data, null));
		final ByteArrayOutputStream bos = new ByteArrayOutputStream(size);
		final DataOutputStream out = new DataOutputStream(bos);
		for (int i = 0; i < size; i++)
			out.writeShort(data[i]);
		final String e = Digest.md5Hex(bos.toByteArray());
		Assert.assertEquals(e, o);
	}

	@Test
	public void canDigestFloatProcessor() throws IOException
	{
		final RandomGenerator r = TestSettings.getRandomGenerator();
		final float[] data = new float[size];
		for (int i = 0; i < size; i++)
			data[i] = (r.nextFloat() - 0.5f) * 2f;

		final String o = new IJDigest().digest(new FloatProcessor(size, 1, data, null));
		final ByteArrayOutputStream bos = new ByteArrayOutputStream(size);
		final DataOutputStream out = new DataOutputStream(bos);
		for (int i = 0; i < size; i++)
			out.writeFloat(data[i]);
		final String e = Digest.md5Hex(bos.toByteArray());
		Assert.assertEquals(e, o);
	}

	@Test
	public void canDigestColorProcessor() throws IOException
	{
		final RandomGenerator r = TestSettings.getRandomGenerator();
		final int[] data = new int[size];
		for (int i = 0; i < size; i++)
			data[i] = r.nextInt();

		final String o = new IJDigest().digest(new ColorProcessor(size, 1, data));
		final ByteArrayOutputStream bos = new ByteArrayOutputStream(size);
		final DataOutputStream out = new DataOutputStream(bos);
		for (int i = 0; i < size; i++)
			out.writeInt(data[i]);
		final String e = Digest.md5Hex(bos.toByteArray());
		Assert.assertEquals(e, o);
	}
}
