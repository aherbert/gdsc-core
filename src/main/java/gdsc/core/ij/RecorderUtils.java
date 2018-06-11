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
package gdsc.core.ij;

import gdsc.core.utils.TurboList;
import ij.plugin.frame.Recorder;

/**
 * Contains helper functions for the recorder
 */
public class RecorderUtils
{
	/**
	 * Reset the recorder for all the named keys.
	 *
	 * @param keys
	 *            the keys
	 */
	public static void resetRecorder(String[] keys)
	{
		if (keys == null || keys.length == 0)
			return;

		// Get the Recorder options, remove all the labels, and update the reduced Recorder options
		String commandName = Recorder.getCommand();
		String commandOptions = Recorder.getCommandOptions();
		if (commandName == null || commandOptions == null)
			return;

		//System.out.printf("%s - %s\n", commandOptions, java.util.Arrays.toString(keys));

		// We only support labels added with 
		//Recorder.recordOption(String);
		//Recorder.recordOption(String,String);
		// These will create a key in the command options of:
		// " "+key
		// " "+key+"="+value
		// " "+key+"=["+value+"]"
		boolean ignored = false;
		TurboList<String[]> pairs = new TurboList<String[]>();
		for (int current = 0, len = commandOptions.length(); current < len;)
		{
			// Find the next non-space character, this will be the start of a key
			while (current < len && commandOptions.charAt(current) == ' ')
				current++;
			if (current == len)
				break;

			// Find the end of the key.
			// This could be a space or an equals.
			int keyEnd = findKeyEnd(commandOptions, len, current);

			String key = commandOptions.substring(current, keyEnd);

			current = keyEnd;

			// Find the value if present
			String value = null;
			if (keyEnd < len && commandOptions.charAt(keyEnd) == '=')
			{
				// There is a value. This may be surrounded by brackets
				int valueStart = keyEnd + 1;
				int valueEnd;
				if (valueStart < len && commandOptions.charAt(valueStart) == '[')
				{
					valueStart++;
					valueEnd = commandOptions.indexOf(']', valueStart);
				}
				else
				{
					valueEnd = commandOptions.indexOf(' ', valueStart);
				}
				if (valueEnd < 0)
					valueEnd = len;
				value = commandOptions.substring(valueStart, valueEnd);

				current = valueEnd + 1;
			}

			// Check key should be ignored
			if (ignore(key, keys))
			{
				//System.out.printf("Ignoring %s %s\n", key, value);
				ignored = true;
				continue;
			}

			//System.out.printf("Keeping %s %s\n", key, value);
			pairs.add(new String[] { key, value });
		}

		if (!ignored)
			// No keys to remove
			return;

		// Reset
		Recorder.setCommand(null);
		Recorder.saveCommand();
		// Re-record all the remaining pairs
		Recorder.setCommand(commandName);
		for (String[] pair : pairs)
		{
			if (pair[1] == null)
				Recorder.recordOption(pair[0]);
			else
				Recorder.recordOption(pair[0], pair[1]);
		}
		//System.out.printf("Now %s\n", Recorder.getCommandOptions());
	}

	private static int findKeyEnd(String commandOptions, int len, int index)
	{
		while (index < len)
		{
			char c = commandOptions.charAt(index);
			if (c == ' ' || c == '=')
				break;
			index++;
		}
		return index;
	}

	private static boolean ignore(String key, String[] keys)
	{
		for (String k : keys)
			if (k.equalsIgnoreCase(key))
				return true;
		return false;
	}
}
