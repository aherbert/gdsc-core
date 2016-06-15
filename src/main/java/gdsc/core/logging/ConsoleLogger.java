package gdsc.core.logging;

/*----------------------------------------------------------------------------- 
 * GDSC SMLM Software
 * 
 * Copyright (C) 2013 Alex Herbert
 * Genome Damage and Stability Centre
 * University of Sussex, UK
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *---------------------------------------------------------------------------*/

/**
 * Logs messages to the Java console
 */
public class ConsoleLogger implements Logger
{
	/**
	 * Log the message to the Java console. Appends a new line to the message.
	 * 
	 * @param message
	 */
	public void info(String message)
	{
		System.out.println(message);
	}

	/**
	 * Log the arguments using the given format to the Java console. Appends a new line to the console if the format is
	 * missing the \n character.
	 * 
	 * @param format
	 * @param args
	 */
	public void info(String format, Object... args)
	{
		System.out.printf(format, args);
		if (!format.endsWith("\n"))
			System.out.println();
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
}
