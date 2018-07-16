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
package gdsc.core.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.commons.math3.random.RandomGenerator;
import org.junit.Assert;
import org.junit.Test;

import gdsc.test.TestSettings;

@SuppressWarnings({ "javadoc" })
public class DigestTest
{
	@Test
	public void canComputeMD5Hex() throws IOException
	{
		final RandomGenerator r = TestSettings.getRandomGenerator();
		final RandomDataGenerator rdg = new RandomDataGenerator(r);

		final byte[] testBytes = new byte[50];

		for (int i = 0; i < 10; i++)
		{
			final String testString = rdg.nextHexString(50);
			Assert.assertEquals(DigestUtils.md5Hex(testString), Digest.md5Hex(testString));
			r.nextBytes(testBytes);
			Assert.assertEquals(DigestUtils.md5Hex(testBytes), Digest.md5Hex(testBytes));
			Assert.assertEquals(DigestUtils.md5Hex(new ByteArrayInputStream(testBytes)),
					Digest.md5Hex(new ByteArrayInputStream(testBytes)));
		}
	}
}
