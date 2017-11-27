package gdsc.core.ij;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;
import org.junit.Assert;
import org.junit.Test;

import gdsc.core.utils.Digest;
import ij.process.*;

public class IJDigestTest
{
	int size = 50;

	@Test
	public void canDigestByteProcessor()
	{
		RandomGenerator r = new Well19937c(30051977);
		byte[] data = new byte[size];
		r.nextBytes(data);

		String o = new IJDigest().digest(new ByteProcessor(size, 1, data));
		String e = Digest.md5Hex(data);
		Assert.assertEquals(e, o);
	}

	@Test
	public void canDigestShortProcessor() throws IOException
	{
		RandomGenerator r = new Well19937c(30051977);
		short[] data = new short[size];
		for (int i = 0; i < size; i++)
			data[i] = (short) ((r.nextDouble() - 0.5) * 2 * Short.MAX_VALUE);

		String o = new IJDigest().digest(new ShortProcessor(size, 1, data, null));
		ByteArrayOutputStream bos = new ByteArrayOutputStream(size);
		DataOutputStream out = new DataOutputStream(bos);
		for (int i = 0; i < size; i++)
			out.writeShort(data[i]);
		String e = Digest.md5Hex(bos.toByteArray());
		Assert.assertEquals(e, o);
	}

	@Test
	public void canDigestFloatProcessor() throws IOException
	{
		RandomGenerator r = new Well19937c(30051977);
		float[] data = new float[size];
		for (int i = 0; i < size; i++)
			data[i] = (r.nextFloat() - 0.5f) * 2f;

		String o = new IJDigest().digest(new FloatProcessor(size, 1, data, null));
		ByteArrayOutputStream bos = new ByteArrayOutputStream(size);
		DataOutputStream out = new DataOutputStream(bos);
		for (int i = 0; i < size; i++)
			out.writeFloat(data[i]);
		String e = Digest.md5Hex(bos.toByteArray());
		Assert.assertEquals(e, o);
	}

	@Test
	public void canDigestColorProcessor() throws IOException
	{
		RandomGenerator r = new Well19937c(30051977);
		int[] data = new int[size];
		for (int i = 0; i < size; i++)
			data[i] = r.nextInt();

		String o = new IJDigest().digest(new ColorProcessor(size, 1, data));
		ByteArrayOutputStream bos = new ByteArrayOutputStream(size);
		DataOutputStream out = new DataOutputStream(bos);
		for (int i = 0; i < size; i++)
			out.writeInt(data[i]);
		String e = Digest.md5Hex(bos.toByteArray());
		Assert.assertEquals(e, o);
	}
}
