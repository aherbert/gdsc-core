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
package uk.ac.sussex.gdsc.core.data;

/**
 * Provide data on 1-axis from an array of doubles.
 */
public class DoubleArrayValueProvider implements ValueProvider
{
    private final double[] val;

    /**
     * Instantiates a new double array trivalue provider.
     *
     * @param val
     *            the val
     * @throws DataException
     *             If the array is missing data
     */
    public DoubleArrayValueProvider(double[] val) throws DataException
    {
        if (val.length == 0)
            throw new DataException("No data");
        this.val = val;
    }

    /** {@inheritDoc} */
    @Override
    public int getLength()
    {
        return val.length;
    }

    /** {@inheritDoc} */
    @Override
    public double get(int x)
    {
        return val[x];
    }

    /** {@inheritDoc} */
    @Override
    public void get(int x, double[] values)
    {
        values[0] = val[x - 1];
        values[1] = val[x];
        values[2] = val[x + 1];
    }

    /** {@inheritDoc} */
    @Override
    public double[] toArray()
    {
        return val;
    }
}
