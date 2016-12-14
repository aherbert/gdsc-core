package gdsc.core.utils;

/*----------------------------------------------------------------------------- 
 * GDSC Plugins for ImageJ
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Simple wrapper for a list of settings. Allows settings to be compared using equals().
 */
public class Settings extends ArrayList<Object>
{
	private static final long serialVersionUID = -4023821382363386047L;

	/**
	 * Instantiates a new list of settings.
	 *
	 * @param settings
	 *            the settings
	 */
	public Settings(Object... settings)
	{
		addAll(Arrays.asList(settings));
	}

	/**
	 * Creates a sublist from index (inclusive) to the end of the list.
	 *
	 * @param from
	 *            the from index (must be less than size
	 * @return the sublist
	 * @throws IndexOutOfBoundsException
	 *             If the from index is invalid
	 */
	public List<Object> subList(int from)
	{
		return subList(from, size());
	}
}