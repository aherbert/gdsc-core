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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 * Text utilities
 */
public class TextUtils
{
	// Wrapping - Taken from apache.commons.lang.WordUtils 
	//-----------------------------------------------------------------------
	/**
	 * <p>
	 * Wraps a single line of text, identifying words by <code>' '</code>.
	 * </p>
	 * 
	 * <p>
	 * New lines will be separated by the system property line separator. Very long words, such as URLs will <i>not</i>
	 * be wrapped.
	 * </p>
	 * 
	 * <p>
	 * Leading spaces on a new line are stripped. Trailing spaces are not stripped.
	 * </p>
	 * 
	 * <pre>
	 * WordUtils.wrap(null, *) = null
	 * WordUtils.wrap("", *) = ""
	 * </pre>
	 * 
	 * @param str
	 *            the String to be word wrapped, may be null
	 * @param wrapLength
	 *            the column to wrap the words at, less than 1 is treated as 1
	 * @return a line with newlines inserted, <code>null</code> if null input
	 */
	public static String wrap(String str, int wrapLength)
	{
		return wrap(str, wrapLength, null, false);
	}

	/**
	 * <p>
	 * Wraps a single line of text, identifying words by <code>' '</code>.
	 * </p>
	 * 
	 * <p>
	 * Leading spaces on a new line are stripped. Trailing spaces are not stripped.
	 * </p>
	 * 
	 * <pre>
	 * WordUtils.wrap(null, *, *, *) = null
	 * WordUtils.wrap("", *, *, *) = ""
	 * </pre>
	 * 
	 * @param str
	 *            the String to be word wrapped, may be null
	 * @param wrapLength
	 *            the column to wrap the words at, less than 1 is treated as 1
	 * @param newLineStr
	 *            the string to insert for a new line, <code>null</code> uses the system property line separator
	 * @param wrapLongWords
	 *            true if long words (such as URLs) should be wrapped
	 * @return a line with newlines inserted, <code>null</code> if null input
	 */
	public static String wrap(String str, int wrapLength, String newLineStr, boolean wrapLongWords)
	{
		if (str == null)
		{
			return null;
		}
		if (newLineStr == null)
		{
			newLineStr = System.getProperty("line.separator");
		}
		if (wrapLength < 1)
		{
			wrapLength = 1;
		}
		int inputLineLength = str.length();
		int offset = 0;
		StringBuilder wrappedLine = new StringBuilder(inputLineLength + 32);

		while ((inputLineLength - offset) > wrapLength)
		{
			if (str.charAt(offset) == ' ')
			{
				offset++;
				continue;
			}
			int spaceToWrapAt = str.lastIndexOf(' ', wrapLength + offset);

			if (spaceToWrapAt >= offset)
			{
				// normal case
				wrappedLine.append(str.substring(offset, spaceToWrapAt));
				wrappedLine.append(newLineStr);
				offset = spaceToWrapAt + 1;

			}
			else
			{
				// really long word or URL
				if (wrapLongWords)
				{
					// wrap really long word one line at a time
					wrappedLine.append(str.substring(offset, wrapLength + offset));
					wrappedLine.append(newLineStr);
					offset += wrapLength;
				}
				else
				{
					// do not wrap really long word, just extend beyond limit
					spaceToWrapAt = str.indexOf(' ', wrapLength + offset);
					if (spaceToWrapAt >= 0)
					{
						wrappedLine.append(str.substring(offset, spaceToWrapAt));
						wrappedLine.append(newLineStr);
						offset = spaceToWrapAt + 1;
					}
					else
					{
						wrappedLine.append(str.substring(offset));
						offset = inputLineLength;
					}
				}
			}
		}

		// Whatever is left in line is short enough to just pass through
		wrappedLine.append(str.substring(offset));

		return wrappedLine.toString();
	}

	/**
	 * Return "s" if the size is not 1 otherwise returns an empty string. This can be used to add an s where necessary
	 * to adjectives:
	 * 
	 * <pre>
	 * System.out.printf(&quot;Created %d thing%s\n&quot;, n, TextUtils.pleural(n));
	 * </pre>
	 * 
	 * @param n
	 *            The number of things
	 * @return "s" or empty string
	 */
	public static String pleural(int n)
	{
		return (Math.abs(n) == 1) ? "" : "s";
	}

	/**
	 * Return "s" if the size is not 1 otherwise returns an empty string. This can be used to add an s where necessary
	 * to adjectives:
	 * 
	 * <pre>
	 * System.out.printf(&quot;Created %s\n&quot;, TextUtils.pleural(n, &quot;thing&quot;));
	 * </pre>
	 * 
	 * @param n
	 *            The number of things
	 * @param name
	 *            The name of the thing
	 * @return "s" or empty string
	 */
	public static String pleural(int n, String name)
	{
		return n + " " + name + ((Math.abs(n) == 1) ? "" : "s");
	}

	/**
	 * Check if the string is null or length zero. Does not check for a string of whitespace.
	 * 
	 * @param string
	 * @return true if the string is null or length zero
	 */
	public static boolean isNullOrEmpty(String string)
	{
		return string == null || string.length() == 0;
	}

	/**
	 * Write the text to file.
	 *
	 * @param filename
	 *            the filename
	 * @param text
	 *            the text
	 * @return true, if successful
	 */
	public static boolean write(String filename, String text)
	{
		FileOutputStream fs = null;
		try
		{
			fs = new FileOutputStream(filename);
			fs.write(text.getBytes());
			return true;
		}
		catch (FileNotFoundException e)
		{
			//e.printStackTrace();
		}
		catch (IOException e)
		{
			//e.printStackTrace();
		}
		finally
		{
			if (fs != null)
			{
				try
				{
					fs.close();
				}
				catch (IOException e)
				{
					//e.printStackTrace();
				}
			}
		}
		return false;
	}
}
