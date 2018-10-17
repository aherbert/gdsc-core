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

/**
 * Exception to throw if a method is not implemented.
 */
public class NotImplementedException extends UnsupportedOperationException {
  private static final long serialVersionUID = 7226451080179546585L;

  /**
   * Instantiates a new not implemented exception.
   */
  public NotImplementedException() {
    super();
  }

  /**
   * Instantiates a new not implemented exception.
   *
   * @param message the message
   */
  public NotImplementedException(String message) {
    super(message);
  }

  /**
   * Instantiates a new not implemented exception.
   *
   * @param message the message
   * @param cause the cause
   */
  public NotImplementedException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Instantiates a new not implemented exception.
   *
   * @param cause the cause
   */
  public NotImplementedException(Throwable cause) {
    super(cause);
  }
}
