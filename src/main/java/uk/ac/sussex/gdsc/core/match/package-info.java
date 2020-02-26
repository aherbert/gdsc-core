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
 * Copyright (C) 2011 - 2020 Alex Herbert
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
 * Provides classes to perform <a href="https://en.wikipedia.org/wiki/Binary_classification">binary
 * classification</a> match analysis on 2D/3D coordinate data.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Precision_and_recall">Precision and Recall</a>
 * @see <a href="https://en.wikipedia.org/wiki/Jaccard_index">Jaccard</a>
 * @see <a href="https://en.wikipedia.org/wiki/F1_score">F1-score</a>
 * @see <a
 *      href="https://en.wikipedia.org/wiki/Receiver_operating_characteristic#Area_under_the_curve">Area
 *      under the curve (AUC)</a>
 *
 * @since 1.2.0
 */
package uk.ac.sussex.gdsc.core.match;
