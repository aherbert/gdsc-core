package gdsc.core.logging;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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
 * Logs messages to memory
 */
public class MemoryLogger implements Logger
{
	private List<String> messages = new LinkedList<String>();

	/**
	 * Stores the message in memory.
	 * 
	 * @param message
	 */
	public void info(String message)
	{
		messages.add(message);
	}

	/**
	 * Stores the message in memory.
	 * 
	 * @param format
	 * @param args
	 */
	public void info(String format, Object... args)
	{
		messages.add(String.format(format, args));
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
	 * Gets the messages.
	 *
	 * @return the messages
	 */
	public List<String> getMessages()
	{
		return getMessages(false);
	}

	/**
	 * Gets the messages.
	 *
	 * @param clear
	 *            Set to true to clear current messages
	 * @return the messages
	 */
	public List<String> getMessages(boolean clear)
	{
		List<String> result = new ArrayList<String>(messages);
		if (clear)
			messages.clear();
		return result;
	}
}
