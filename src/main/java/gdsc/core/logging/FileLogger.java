package gdsc.core.logging;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

/*----------------------------------------------------------------------------- 
 * GDSC Software
 * 
 * Copyright (C) 2016 Alex Herbert
 * Genome Damage and Stability Centre
 * University of Sussex, UK
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *---------------------------------------------------------------------------*/

/**
 * Logs messages to a file.
 */
public class FileLogger implements Logger
{
	private OutputStreamWriter os = null;

	public FileLogger(String filename) throws FileNotFoundException
	{
		this(new FileOutputStream(filename));
	}

	public FileLogger(FileOutputStream fos)
	{
		os = new OutputStreamWriter(new BufferedOutputStream(fos));
	}

	/**
	 * Log the message to the file.
	 * 
	 * @param message
	 */
	public void info(String message)
	{
		if (os == null)
			return;
		synchronized (os)
		{
			try
			{
				os.write(message);
				if (!message.endsWith("\n"))
					os.write('\n');
			}
			catch (IOException e)
			{
				close();
			}
		}
	}

	/**
	 * Log the arguments using the given format to the Java console. Appends a new line to the message.
	 * 
	 * @param format
	 * @param args
	 */
	public void info(String format, Object... args)
	{
		info(String.format(format, args));
	}

	public void debug(String message)
	{
		info(message);
	}

	public void debug(String format, Object... args)
	{
		info(format, args);
	}

	public void error(String message)
	{
		info(message);
	}

	public void error(String format, Object... args)
	{
		info(format, args);
	}

	/**
	 * Close the file.
	 */
	public void close()
	{
		if (os == null)
			return;
		synchronized (os)
		{
			try
			{
				os.close();
			}
			catch (IOException e)
			{
			}
			finally
			{
				os = null;
			}
		}
	}
}
