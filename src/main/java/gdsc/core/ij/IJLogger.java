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

import ij.IJ;

/**
 * Log to the ImageJ log window
 */
public class IJLogger implements gdsc.core.logging.Logger
{
	/** Set to true to show debug log messages. */
	public boolean showDebug = false;
	/** Set to true to show error log messages. */
	public boolean showError = true;

	/**
	 * Instantiates a new IJ logger.
	 */
	public IJLogger()
	{
	}

	/**
	 * Instantiates a new IJ logger.
	 *
	 * @param showDebug
	 *            Set to true to show debug log messages
	 * @param showError
	 *            Set to true to show error log messages
	 */
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
	@Override
	public void info(String message)
	{
		IJ.log(message);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gdsc.smlm.fitting.logging.Logger#info(java.lang.String, java.lang.Object[])
	 */
	@Override
	public void info(String format, Object... args)
	{
		IJ.log(String.format(format, args));
	}

	@Override
	public void debug(String message)
	{
		if (showDebug)
			info(message);
	}

	@Override
	public void debug(String format, Object... args)
	{
		if (showDebug)
			info(format, args);
	}

	@Override
	public void error(String message)
	{
		if (showError)
			info(message);
	}

	@Override
	public void error(String format, Object... args)
	{
		if (showError)
			info(format, args);
	}
}
