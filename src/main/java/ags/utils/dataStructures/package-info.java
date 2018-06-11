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
/**
 * The code contained here has been taken from https://bitbucket.org/rednaxela/knn-benchmark.
 * <p>
 * The KNN benchmark project contains various implementations of Kd-trees for performing efficient K-Nearest Neighbour
 * searches. There are many implementations available, this package contains one of the fastest in the standard
 * benchmark tests.
 * <p>
 * The code is Copyright 2009 Rednaxela
 * <p>
 * Modifications to the code have been made by Alex Herbert for a smaller memory footprint and dedicated 2D processing.
 */
package ags.utils.dataStructures;
