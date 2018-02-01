package gdsc.core.match;

/*----------------------------------------------------------------------------- 
 * GDSC Software
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
 * <p>
 * Stores a 2D/3D point with a start and end time. Allows scoring the match between two fluorophore pulses.
 */
public class Pulse extends BasePoint implements Comparable<Pulse>
{
	private int start, end;

	public Pulse(float x, float y, float z, int start, int end)
	{
		super(x, y, z);
		this.start = start;
		this.end = end;
	}

	public Pulse(float x, float y, int start, int end)
	{
		super(x, y);
		this.start = start;
		this.end = end;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object aThat)
	{
		if (this == aThat)
			return true;
		if (!(aThat instanceof Pulse))
			return false;

		//cast to native object is now safe
		Pulse that = (Pulse) aThat;

		return x == that.x && y == that.y && z == that.z && start == that.start && end == that.end;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		// Note: floatToRawIntBits does not unify all possible NaN values
		// However since the equals() will fail for NaN values we are not
		// breaking the java contract.
		return (41 * (41 * (41 * (41 * (41 + Float.floatToRawIntBits(x)) + Float.floatToRawIntBits(y)) +
				Float.floatToRawIntBits(z)) + start) + end);
	}

	/**
	 * Calculate the score for the match to the other pulse. The score computes the number of matching on frames (the
	 * overlap) and multiplies it by the distance weighted score (from 1 to zero). The threshold distance is the
	 * squared distance at which the score will be 0.5;
	 * 
	 * @param other
	 * @param d2
	 *            The squared distance between the two coordinates
	 * @param dt
	 *            The squared distance threshold
	 * @return
	 */
	public double score(final Pulse other, final double d2, final double dt)
	{
		final int overlap = calculateOverlap(other);
		return overlap * (1 / (1 + d2 / dt));
	}

	/**
	 * Calculate the score for the match to the other pulse. The score computes the number of matching on frames (the
	 * overlap) and multiplies it by the distance weighted score (from 1 to zero). The threshold distance is the
	 * squared distance at which the score will be 0.5;
	 * 
	 * @param other
	 * @param dt
	 *            The squared distance threshold
	 * @return
	 */
	public double score(final Pulse other, final double dt)
	{
		final double d2 = distanceXYZ2(other);
		return score(other, d2, dt);
	}

	/**
	 * Calculate the number of overlapping frames using the start and end times
	 * 
	 * @param that
	 *            The other pulse
	 * @return the number of frames
	 */
	public int calculateOverlap(final Pulse that)
	{
		// --------------
		//                ===========
		// or
		// ============
		//               ------------
		if (this.end < that.start || that.end < this.start)
			return 0;

		// ---------------------
		//         ==================
		// or
		// --------------------------------
		//         ==================
		// or
		//         ------------------      
		// =================================
		// or
		//         ---------------------
		// ==================
		final int s = (this.start < that.start) ? that.start : this.start;
		final int e = (this.end < that.end) ? this.end : that.end;
		// TODO - remove this
		if (e - s < 0)
			throw new RuntimeException("overlap error");
		return e - s + 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Pulse o)
	{
		if (start == o.start)
		{
			return end - o.end;
		}
		return start - o.start;
	}

	/**
	 * @return the start frame
	 */
	public int getStart()
	{
		return start;
	}

	/**
	 * @return the end frame
	 */
	public int getEnd()
	{
		return end;
	}
}