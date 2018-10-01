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
package uk.ac.sussex.gdsc.core.data.utils;

/**
 * Class to the Rounder interface that does not perform rounding
 */
public class NonRounder implements Rounder
{
    /** {@inheritDoc} */
    @Override
    public double round(double value)
    {
        return value;
    }

    /** {@inheritDoc} */
    @Override
    public String toString(double value)
    {
        return Double.toString(value);
    }

    /** {@inheritDoc} */
    @Override
    public float round(float value)
    {
        return value;
    }

    /** {@inheritDoc} */
    @Override
    public String toString(float value)
    {
        return Float.toString(value);
    }
}
