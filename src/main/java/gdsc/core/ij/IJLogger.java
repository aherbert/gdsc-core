package gdsc.core.ij;

/*----------------------------------------------------------------------------- 
 * GDSC Plugins for ImageJ
 * 
 * Copyright (C) 2016 Alex Herbert
 * Genome Damage and Stability Centre
 * University of Sussex, UK
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *---------------------------------------------------------------------------*/

import ij.IJ;

/**
 * Log to the ImageJ log window
 */
public class IJLogger implements gdsc.core.logging.Logger
{
	public boolean showDebug = false;
	public boolean showError = true;

	public IJLogger()
	{
	}

	public IJLogger(boolean showDebug, boolean showError)
	{
		this.showDebug = showDebug;
		this.showError = showError;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.smlm.fitting.logging.Logger#info(java.lang.String)
	 */
	public void info(String message)
	{
		IJ.log(message);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.smlm.fitting.logging.Logger#info(java.lang.String, java.lang.Object[])
	 */
	public void info(String format, Object... args)
	{
		IJ.log(String.format(format, args));
	}

	public void debug(String message)
	{
		if (showDebug)
			info(message);
	}

	public void debug(String format, Object... args)
	{
		if (showDebug)
			info(format, args);
	}

	public void error(String message)
	{
		if (showError)
			info(message);
	}

	public void error(String format, Object... args)
	{
		if (showError)
			info(format, args);
	}
}