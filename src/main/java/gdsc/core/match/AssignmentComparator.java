package gdsc.core.match;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/*----------------------------------------------------------------------------- 
 * GDSC SMLM Software
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
 * Compares assignments
 */
public class AssignmentComparator implements Comparator<Assignment>
{
	private static final AssignmentComparator instance = new AssignmentComparator();

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Assignment o1, Assignment o2)
	{
		if (o1.getDistance() < o2.getDistance())
			return -1;
		if (o1.getDistance() > o2.getDistance())
			return 1;
		return 0;
	}

	/**
	 * Sort the assignments
	 * 
	 * @param assignments
	 */
	public static void sort(List<? extends Assignment> assignments)
	{
		Collections.sort(assignments, instance);
	}

	/**
	 * Sort the assignments
	 * 
	 * @param assignments
	 */
	public static void sort(Assignment[] assignments)
	{
		Arrays.sort(assignments, instance);
	}
}