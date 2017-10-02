package gdsc.core.logging;

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
 * Logs messages to nowhere
 */
public class NullLogger implements Logger
{
	/** An instance to ignore progress reporting */
	public static final NullLogger INSTANCE = new NullLogger();

	/**
	 * Creates an instance if the argument is null, else return the argument.
	 *
	 * @param logger
	 *            the logger (may be null)
	 * @return the logger (not null)
	 */
	public static Logger createIfNull(Logger logger)
	{
		return (logger == null) ? INSTANCE : logger;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.smlm.utils.logging.Logger#info(java.lang.String)
	 */
	public void info(String message)
	{
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.smlm.utils.logging.Logger#info(java.lang.String, java.lang.Object[])
	 */
	public void info(String format, Object... args)
	{
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.smlm.utils.logging.Logger#debug(java.lang.String)
	 */
	public void debug(String message)
	{
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.smlm.utils.logging.Logger#debug(java.lang.String, java.lang.Object[])
	 */
	public void debug(String format, Object... args)
	{
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.smlm.utils.logging.Logger#error(java.lang.String)
	 */
	public void error(String message)
	{
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.smlm.utils.logging.Logger#error(java.lang.String, java.lang.Object[])
	 */
	public void error(String format, Object... args)
	{
	}
}
