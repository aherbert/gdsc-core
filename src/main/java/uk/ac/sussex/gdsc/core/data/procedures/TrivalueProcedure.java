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
package uk.ac.sussex.gdsc.core.data.procedures;

/**
 * Interface for accessing a value in three dimensions.
 */
public interface TrivalueProcedure
{
    /**
     * Sets the dimensions. This will be called first to allow the procedure to prepare to process the results. If the
     * dimensions cannot be processed then return false to abort.
     *
     * @param maxx
     *            the maxx
     * @param maxy
     *            the maxy
     * @param maxz
     *            the maxz
     * @return true, if it is OK to continue
     */
    public boolean setDimensions(int maxx, int maxy, int maxz);

    /**
     * Sets the X axis value.
     *
     * @param i
     *            the index
     * @param value
     *            the value
     */
    public void setX(int i, double value);

    /**
     * Sets the Y axis value.
     *
     * @param j
     *            the index
     * @param value
     *            the value
     */
    public void setY(int j, double value);

    /**
     * Sets the Z axis value.
     *
     * @param k
     *            the index
     * @param value
     *            the value
     */
    public void setZ(int k, double value);

    /**
     * Set the value.
     *
     * @param i
     *            the x index
     * @param j
     *            the y index
     * @param k
     *            the z index
     * @param value
     *            the value
     */
    public void setValue(int i, int j, int k, double value);
}
