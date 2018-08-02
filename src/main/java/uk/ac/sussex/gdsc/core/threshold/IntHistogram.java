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
package uk.ac.sussex.gdsc.core.threshold;

/**
 * Contains a histogram.
 * <p>
 * The histogram is implemented in this class using integer bin values starting from an offset
 */
public class IntHistogram extends Histogram
{
    /** The offset. */
    final int offset;

    /**
     * Instantiates a new int histogram.
     *
     * @param h
     *            the histogram counts
     * @param minBin
     *            the min bin
     * @param maxBin
     *            the max bin
     * @param offset
     *            the offset
     */
    protected IntHistogram(int[] h, int minBin, int maxBin, int offset)
    {
        super(h, minBin, maxBin);
        this.offset = offset;
    }

    /**
     * Instantiates a new int histogram.
     *
     * @param h
     *            the histogram counts
     * @param offset
     *            the offset
     */
    public IntHistogram(int[] h, int offset)
    {
        super(h);
        this.offset = offset;
    }

    /*
     * (non-Javadoc)
     *
     * @see uk.ac.sussex.gdsc.core.threshold.Histogram#getValue(int)
     */
    @Override
    public float getValue(int i)
    {
        return offset + i;
    }

    /*
     * (non-Javadoc)
     *
     * @see uk.ac.sussex.gdsc.core.threshold.Histogram#clone()
     */
    @Override
    public IntHistogram clone()
    {
        return new IntHistogram(this.h.clone(), minBin, maxBin, offset);
    }
}
