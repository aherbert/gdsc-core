package gdsc.core.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.commons.math3.random.RandomGenerator;
import org.junit.Assert;
import org.junit.Test;

public class DigestTest
{
	@Test
	public void canComputeMD5Hex() throws IOException
	{
		RandomDataGenerator rdg = new RandomDataGenerator();
		RandomGenerator r = rdg.getRandomGenerator();
		
		byte[] testBytes = new byte[50];

		for (int i = 0; i < 10; i++)
		{
			String testString = rdg.nextHexString(50);
			Assert.assertEquals(DigestUtils.md5Hex(testString), Digest.md5Hex(testString));
			r.nextBytes(testBytes);
			Assert.assertEquals(DigestUtils.md5Hex(testBytes), Digest.md5Hex(testBytes));
			Assert.assertEquals(DigestUtils.md5Hex(new ByteArrayInputStream(testBytes)), 
					Digest.md5Hex(new ByteArrayInputStream(testBytes)));
		}
	}
}
