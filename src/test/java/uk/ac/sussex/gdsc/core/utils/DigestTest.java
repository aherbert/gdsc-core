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

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.Assertions;

import uk.ac.sussex.gdsc.test.TestSettings;
import uk.ac.sussex.gdsc.test.junit5.RandomSeed;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;

@SuppressWarnings({ "javadoc" })
public class DigestTest
{
	@SeededTest
	public void canComputeMD5Hex(RandomSeed seed) throws IOException
	{
		final UniformRandomProvider r = TestSettings.getRandomGenerator(seed.getSeed());

		final byte[] testBytes = new byte[50];

		for (int i = 0; i < 10; i++)
		{
			final String testString = nextHexString(r, 50);
			Assertions.assertEquals(DigestUtils.md5Hex(testString), Digest.md5Hex(testString));
			r.nextBytes(testBytes);
			Assertions.assertEquals(DigestUtils.md5Hex(testBytes), Digest.md5Hex(testBytes));
			Assertions.assertEquals(DigestUtils.md5Hex(new ByteArrayInputStream(testBytes)),
					Digest.md5Hex(new ByteArrayInputStream(testBytes)));
		}
	}

	/**
	 * Generates a random string of hex characters of length len.
	 * <p>
	 * Copied from org.apache.commons.math3.random.RandomDataGenerator.
	 *
	 * @param ran
	 *            the random provider
	 * @param len
	 *            the len
	 * @return the string
	 * @throws NotStrictlyPositiveException
	 *             the not strictly positive exception
	 */
	private static String nextHexString(UniformRandomProvider ran, int len) throws NotStrictlyPositiveException
	{
		if (len <= 0)
		{
			throw new NotStrictlyPositiveException(LocalizedFormats.LENGTH, len);
		}

		// Initialize output buffer
		StringBuilder outBuffer = new StringBuilder();

		// Get int(len/2)+1 random bytes
		byte[] randomBytes = new byte[(len / 2) + 1];
		ran.nextBytes(randomBytes);

		// Convert each byte to 2 hex digits
		for (int i = 0; i < randomBytes.length; i++)
		{
			Integer c = Integer.valueOf(randomBytes[i]);

			/*
			 * Add 128 to byte value to make interval 0-255 before doing hex
			 * conversion. This guarantees <= 2 hex digits from toHexString()
			 * toHexString would otherwise add 2^32 to negative arguments.
			 */
			String hex = Integer.toHexString(c.intValue() + 128);

			// Make sure we add 2 hex digits for each byte
			if (hex.length() == 1)
			{
				outBuffer.append("0");
			}
			outBuffer.append(hex);
		}
		return outBuffer.toString().substring(0, len);
	}
}
