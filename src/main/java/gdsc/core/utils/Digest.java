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

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.MessageDigest;


/**
 * Class for computing digests
 * 
 * @author Alex Herbert
 */
public class Digest
{
    /**
     * The MD2 message digest algorithm defined in RFC 1319.
     */
    public static final String MD2 = "MD2";

    /**
     * The MD5 message digest algorithm defined in RFC 1321.
     */
    public static final String MD5 = "MD5";

    /**
     * The SHA-1 hash algorithm defined in the FIPS PUB 180-2.
     */
    public static final String SHA_1 = "SHA-1";

    /**
     * The SHA-224 hash algorithm defined in the FIPS PUB 180-3.
     * <p>
     * Present in Oracle Java 8.
     * </p>
     *
     * @since 1.11
     */
    public static final String SHA_224 = "SHA-224";

    /**
     * The SHA-256 hash algorithm defined in the FIPS PUB 180-2.
     */
    public static final String SHA_256 = "SHA-256";

    /**
     * The SHA-384 hash algorithm defined in the FIPS PUB 180-2.
     */
    public static final String SHA_384 = "SHA-384";

    /**
     * The SHA-512 hash algorithm defined in the FIPS PUB 180-2.
     */
    public static final String SHA_512 = "SHA-512";
    
	/**
	 * Gets the digest.
	 *
	 * @param algorithm
	 *            the algorithm
	 * @return the digest
	 * @throws IllegalArgumentException
	 *             If the algorithm is not recognised
	 */
	public static MessageDigest getDigest(String algorithm) throws IllegalArgumentException
	{
		try
		{
			MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
			messageDigest.reset();
			return messageDigest;
		}
		catch (java.security.NoSuchAlgorithmException e)
		{
			throw new IllegalArgumentException("Invalid algorithm: " + algorithm);
		}
	}

	/**
	 * Update the digest.
	 *
	 * @param messageDigest
	 *            the message digest
	 * @param data
	 *            the data
	 * @return the message digest
	 */
	public static MessageDigest updateDigest(MessageDigest messageDigest, byte[] data)
	{
		for (int i = 0; i < data.length; i++)
			messageDigest.update(data[i]);
		return messageDigest;
	}

	/**
	 * Update the digest by reading the stream until the end. The stream is not closed.
	 *
	 * @param messageDigest
	 *            the message digest
	 * @param data
	 *            the data
	 * @return the message digest
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static MessageDigest updateDigest(MessageDigest messageDigest, InputStream data) throws IOException
	{
		final byte[] buffer = new byte[1024];
		int read = data.read(buffer, 0, 1024);
		while (read > -1)
		{
			messageDigest.update(buffer, 0, read);
			read = data.read(buffer, 0, 1024);
		}
		return messageDigest;
	}

	/**
	 * Update the digest.
	 *
	 * @param messageDigest
	 *            the message digest
	 * @param data
	 *            the data
	 * @return the message digest
	 */
	public static MessageDigest updateDigest(MessageDigest messageDigest, String data)
	{
		messageDigest.update(data.getBytes(Charset.forName("UTF-8")));
		return messageDigest;
	}

	private static final char[] DIGITS_UPPER = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D',
			'E', 'F' };
	private static final char[] DIGITS_LOWER = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd',
			'e', 'f' };

	/**
	 * Convert the byte data to a hex string. Lower case is used.
	 * <p>
	 * Taken from org.apache.commons.codec.binary.Hex
	 *
	 * @param data
	 *            the data
	 * @return the hex string
	 */
	public static String toHex(byte[] data)
	{
		final int l = data.length;
		final char[] out = new char[l << 1];
		// two characters form the hex value.
		for (int i = 0, j = 0; i < l; i++)
		{
			out[j++] = DIGITS_LOWER[(0xF0 & data[i]) >>> 4];
			out[j++] = DIGITS_LOWER[0x0F & data[i]];
		}
		return new String(out);
	}

	/**
	 * Convert the byte data to a hex string.
	 * <p>
	 * Taken from org.apache.commons.codec.binary.Hex
	 *
	 * @param data
	 *            the data
	 * @param toLowerCase
	 *            true if lower case is required
	 * @return the hex string
	 */
	public static String toHex(byte[] data, boolean toLowerCase)
	{
		final int l = data.length;
		final char[] out = new char[l << 1];
		final char[] digits = (toLowerCase) ? DIGITS_LOWER : DIGITS_UPPER;
		// two characters form the hex value.
		for (int i = 0, j = 0; i < l; i++)
		{
			out[j++] = digits[(0xF0 & data[i]) >>> 4];
			out[j++] = digits[0x0F & data[i]];
		}
		return new String(out);
	}

	/**
	 * Create an MD5 digest from a string.
	 * 
	 * @param data
	 *            the data
	 * @return the digest
	 */
	public static byte[] md5(String data)
	{
		return updateDigest(getDigest("MD5"), data).digest();
	}

	/**
	 * Create an MD5 digest from an input stream.
	 *
	 * @param data
	 *            the data
	 * @return the digest
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static byte[] md5(InputStream data) throws IOException
	{
		return updateDigest(getDigest("MD5"), data).digest();
	}

	/**
	 * Create an MD5 digest from an input stream.
	 *
	 * @param data
	 *            the data
	 * @return the digest
	 */
	public static byte[] md5(byte[] data)
	{
		return updateDigest(getDigest("MD5"), data).digest();
	}

	/**
	 * Convert the MD5 byte hash to a hex string.
	 *
	 * @param hash
	 *            the hash
	 * @return the hex string
	 */
	private static String md5ToHex(byte[] hash)
	{
		return String.format("%032x", new BigInteger(1, hash));
	}

	/**
	 * Create an MD5 hex digest from a string.
	 * 
	 * @param data
	 *            the data
	 * @return the MD5 string
	 */
	public static String md5Hex(String data)
	{
		return md5ToHex(md5(data));
	}

	/**
	 * Create an MD5 hex digest from an input stream.
	 *
	 * @param data
	 *            the data
	 * @return the MD5 string
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static String md5Hex(InputStream data) throws IOException
	{
		return md5ToHex(md5(data));
	}

	/**
	 * Create an MD5 hex digest from an input stream.
	 *
	 * @param data
	 *            the data
	 * @return the MD5 string
	 */
	public static String md5Hex(byte[] data)
	{
		return md5ToHex(md5(data));
	}
}
