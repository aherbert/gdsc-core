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
package uk.ac.sussex.gdsc.core.utils;

/**
 * Class for simple lock acquisition for threaded applications. Allows multiple threads to view the same lock and only
 * execute code if another thread is not currently in possession of the lock so supporting a type of non-blocking
 * synchronised block execution.
 *
 * <pre>
 * SimpleLock lock = new SimpleLock();
 *
 * if (lock.acquire())
 * {
 * 	// Do something
 * 	lock.release();
 * }
 * // else fall through
 * </pre>
 *
 * @author Alex Herbert
 */
public class SimpleLock
{
	private boolean locked;

	/**
	 * Acquire the lock. This method is synchronized. The lock should be released when finished.
	 *
	 * @return true, if the lock was acquired, else false if already locked
	 */
	public synchronized boolean acquire()
	{
		if (locked)
			return false;
		return locked = true;
	}

	/**
	 * Checks if is locked.
	 *
	 * @return true, if is locked
	 */
	public boolean isLocked()
	{
		return locked;
	}

	/**
	 * Release the lock. This is not synchronized and should be called by the owner of the lock.
	 */
	public void release()
	{
		locked = false;
	}
}
