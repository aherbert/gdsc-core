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
package gdsc.core.logging;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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
	@Override
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
	@Override
	public void info(String format, Object... args)
	{
		messages.add(String.format(format, args));
	}

	@Override
	public void debug(String message)
	{
		info(message);
	}

	@Override
	public void debug(String format, Object... args)
	{
		info(format, args);
	}

	@Override
	public void error(String message)
	{
		info(message);
	}

	@Override
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
