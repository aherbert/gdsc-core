package gdsc.core.utils;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.MessageDigest;

/*----------------------------------------------------------------------------- 
 * GDSC ImageJ Plugins Software
 * 
 * Copyright (C) 2017 Alex Herbert
 * Genome Damage and Stability Centre
 * University of Sussex, UK
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *---------------------------------------------------------------------------*/

/**
 * Class for computing digests
 * 
 * @author Alex Herbert
 */
public class Digest
{
	/**
	 * Gets the digest.
	 *
	 * @param algorithm
	 *            the algorithm
	 * @return the digest
	 * @throws IllegalArgumentException
	 *             If teh algorithm is not recognised
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