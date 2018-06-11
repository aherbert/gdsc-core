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
package ags.utils.dataStructures.trees.thirdGenKD;

/**
 * Optimised distance function for 2D data
 * 
 * @author Alex Herbert
 */
public class SquareEuclideanDistanceFunction2D implements DistanceFunction
{
	@Override
	public double distance(double[] p1, double[] p2)
	{
		double dx = p1[0] - p2[0];
		double dy = p1[1] - p2[1];
		return dx * dx + dy * dy;
	}

	@Override
	public double distanceToRect(double[] point, double[] min, double[] max)
	{
		double dx = (point[0] > max[0]) ? point[0] - max[0] : (point[0] < min[0]) ? point[0] - min[0] : 0;
		double dy = (point[1] > max[1]) ? point[1] - max[1] : (point[1] < min[1]) ? point[1] - min[1] : 0;
		return dx * dx + dy * dy;
	}
}
