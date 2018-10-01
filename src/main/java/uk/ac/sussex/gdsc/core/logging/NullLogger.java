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
package uk.ac.sussex.gdsc.core.logging;

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

    /** {@inheritDoc} */
    @Override
    public void info(String message)
    {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void info(String format, Object... args)
    {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void debug(String message)
    {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void debug(String format, Object... args)
    {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void error(String message)
    {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void error(String format, Object... args)
    {
        // Do nothing
    }
}
