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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Simple wrapper for a list of settings. Allows settings to be compared using equals().
 */
public class Settings extends ArrayList<Object>
{
    private static final long serialVersionUID = -4023821382363386047L;

    /**
     * Instantiates a new list of settings.
     *
     * @param settings
     *            the settings
     */
    public Settings(Object... settings)
    {
        addAll(Arrays.asList(settings));
    }

    /**
     * Creates a sublist from index (inclusive) to the end of the list.
     *
     * @param from
     *            the from index (must be less than size
     * @return the sublist
     * @throws IndexOutOfBoundsException
     *             If the from index is invalid
     */
    public List<Object> subList(int from)
    {
        return subList(from, size());
    }
}
