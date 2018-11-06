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
 * Annotation used to indicates that the visibility of a type or member has been relaxed to make the
 * code testable.
 *
 * <p>This idea originates from Google Guava. PMD will not flag package private members when using
 * the DefaultPackage rule if they are annotated with VisibleForTesting.
 *
 * @see <a href="https://github.com/google/guava">Guava: Google Core Libraries for Java</a>
 * @see <a href="https://pmd.github.io/latest/pmd_rules_java_codestyle.html#defaultpackage">PMD Rule
 *      DefaultPackage</a>
 */
public @interface VisibleForTesting {
  // Annotation
}
