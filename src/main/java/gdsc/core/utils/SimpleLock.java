package gdsc.core.utils;

/*----------------------------------------------------------------------------- 
 * GDSC ImageJ Plugins Software
 * 
 * Copyright (C) 2017 Alex Herbert
 * Genome Damage and Stability Centre
 * University of Sussex, UK
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *---------------------------------------------------------------------------*/

/**
 * Class for simple lock acquisition for threaded applications. Allows multiple threads to view the same lock and only
 * execute code if another thread is not currently in possession of the lock so supporting a type of non-blocking
 * synchronised block execution.
 * <pre>
 * SimpleLock lock = new SimpleLock();
 * 
 * if (lock.acquire())
 * {
 *     // Do something
 *     lock.release();
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
	 * @return true, if the lock was aquired, else false if already locked
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
	 * Release the lock. This is not synchronized and sbould be called by the owner of the lock.
	 */
	public void release()
	{
		locked = false;
	}
}