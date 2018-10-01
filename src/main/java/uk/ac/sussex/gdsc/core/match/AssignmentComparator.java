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
package uk.ac.sussex.gdsc.core.match;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Compares assignments
 */
public class AssignmentComparator implements Comparator<Assignment>
{
    private static final AssignmentComparator instance = new AssignmentComparator();

    /** {@inheritDoc} */
    @Override
    public int compare(Assignment o1, Assignment o2)
    {
        if (o1.getDistance() < o2.getDistance())
            return -1;
        if (o1.getDistance() > o2.getDistance())
            return 1;
        return 0;
    }

    /**
     * Sort the assignments
     *
     * @param assignments
     *            the assignments
     */
    public static void sort(List<? extends Assignment> assignments)
    {
        Collections.sort(assignments, instance);
    }

    /**
     * Sort the assignments using the fastest sort.
     *
     * @param assignments
     *            the assignments
     */
    public static void sort(Assignment[] assignments)
    {
        sort1(assignments);
    }

    /**
     * Sort the assignments using the native object comparator
     *
     * @param assignments
     *            the assignments
     */
    public static void sort1(Assignment[] assignments)
    {
        Arrays.sort(assignments, instance);
    }

    /**
     * Sort the assignments using a custom comparator
     *
     * @param assignments
     *            the assignments
     */
    public static void sort2(Assignment[] assignments)
    {
        final int size = assignments.length;
        if (assignments.length < 2)
            return;

        // Convert data for sorting
        final double[][] data = new double[size][2];
        for (int i = size; i-- > 0;)
        {
            data[i][0] = assignments[i].getDistance();
            data[i][1] = i;
        }

        Arrays.sort(data, new Comparator<double[]>()
        {
            @Override
            public int compare(double[] o1, double[] o2)
            {
                if (o1[0] < o2[0])
                    return -1;
                if (o1[0] > o2[0])
                    return 1;
                return 0;
            }
        });

        // Copy back
        final Assignment[] tmp = Arrays.copyOf(assignments, size);
        for (int i = size; i-- > 0;)
            assignments[i] = tmp[(int) data[i][1]];
    }

    private static class DoubleSortObject implements Comparable<DoubleSortObject>
    {
        final double d;
        final Assignment a;

        DoubleSortObject(double d, Assignment a)
        {
            this.d = d;
            this.a = a;
        }

        @Override
        public int compareTo(DoubleSortObject o2)
        {
            if (d < o2.d)
                return -1;
            if (d > o2.d)
                return 1;
            return 0;
        }
    }

    /**
     * Sort the assignments using a custom sort object with double precision
     *
     * @param assignments
     *            the assignments
     */
    public static void sort3(Assignment[] assignments)
    {
        final int size = assignments.length;
        if (assignments.length < 2)
            return;

        // Convert data for sorting
        final DoubleSortObject[] data = new DoubleSortObject[size];
        for (int i = size; i-- > 0;)
            data[i] = new DoubleSortObject(assignments[i].getDistance(), assignments[i]);

        Arrays.sort(data);

        //		Arrays.sort(data, new Comparator<DoubleSortObject>()
        //		{
        //			public int compare(DoubleSortObject o1, DoubleSortObject o2)
        //			{
        //				if (o1.d < o2.d)
        //					return -1;
        //				if (o1.d > o2.d)
        //					return 1;
        //				return 0;
        //			}
        //		});

        // Copy back
        for (int i = size; i-- > 0;)
            assignments[i] = data[i].a;
    }

    private static class FloatSortObject implements Comparable<FloatSortObject>
    {
        final float d;
        final Assignment a;

        FloatSortObject(float d, Assignment a)
        {
            this.d = d;
            this.a = a;
        }

        @Override
        public int compareTo(FloatSortObject o2)
        {
            if (d < o2.d)
                return -1;
            if (d > o2.d)
                return 1;
            return 0;
        }
    }

    /**
     * Sort the assignments using a custom sort object with float precision
     *
     * @param assignments
     *            the assignments
     */
    public static void sort4(Assignment[] assignments)
    {
        final int size = assignments.length;
        if (assignments.length < 2)
            return;

        // Convert data for sorting
        final FloatSortObject[] data = new FloatSortObject[size];
        for (int i = size; i-- > 0;)
            data[i] = new FloatSortObject((float) assignments[i].getDistance(), assignments[i]);

        Arrays.sort(data);

        //		Arrays.sort(data, new Comparator<FloatSortObject>()
        //		{
        //			public int compare(FloatSortObject o1, FloatSortObject o2)
        //			{
        //				if (o1.d < o2.d)
        //					return -1;
        //				if (o1.d > o2.d)
        //					return 1;
        //				return 0;
        //			}
        //		});

        // Copy back
        for (int i = size; i-- > 0;)
            assignments[i] = data[i].a;
    }
}
