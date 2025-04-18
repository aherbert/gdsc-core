/*-
 * #%L
 * Genome Damage and Stability Centre Core Package
 *
 * Contains core utilities for image analysis and is used by:
 *
 * GDSC ImageJ Plugins - Microscopy image analysis
 *
 * GDSC SMLM ImageJ Plugins - Single molecule localisation microscopy (SMLM)
 * %%
 * Copyright (C) 2011 - 2025 Alex Herbert
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
 * Provides classes for constructing and searching tree-like data structures such as a <a
 * href="https://en.wikipedia.org/wiki/K-d_tree">KD-Tree</a>.
 *
 * <p>The tree functionality is defined using {@code <type>KdTree} interfaces. Trees should be
 * constructed using the {@link uk.ac.sussex.gdsc.core.trees.KdTrees KdTrees} utility class.
 *
 * <p>The underlying KD-Tree implementation has been adapted from <a
 * href="http://robowiki.net/wiki/User:Rednaxela/kD-Tree">Rednaxela's KD-tree</a>. Modifications to
 * the original code have been made to support the required tree interfaces including different
 * primitive-type specialisations and to allow thread-safe concurrent search.
 *
 * @since 2.0
 */
package uk.ac.sussex.gdsc.core.trees;
