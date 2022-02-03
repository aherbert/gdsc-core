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
 * Copyright (C) 2011 - 2022 Alex Herbert
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
 * Exception to throw if computation failed.
 */
public class ComputationException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  /**
   * Instantiates a new computation exception.
   */
  public ComputationException() {
    super();
  }

  /**
   * Instantiates a new computation exception.
   *
   * @param message the message
   */
  public ComputationException(String message) {
    super(message);
  }

  /**
   * Instantiates a new computation exception.
   *
   * @param message the message
   * @param cause the cause
   */
  public ComputationException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Instantiates a new computation exception.
   *
   * @param cause the cause
   */
  public ComputationException(Throwable cause) {
    super(cause);
  }
}
