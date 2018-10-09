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
package uk.ac.sussex.gdsc.core.data.utils;

import uk.ac.sussex.gdsc.core.data.DataException;

/**
 * Exception to throw if conversion is not possible.
 */
public class ConversionException extends DataException {
  private static final long serialVersionUID = 2470815639465684383L;

  /**
   * Instantiates a new conversion exception.
   */
  public ConversionException() {
    super();
  }

  /**
   * Instantiates a new conversion exception.
   *
   * @param message the message
   */
  public ConversionException(String message) {
    super(message);
  }

  /**
   * Instantiates a new conversion exception.
   *
   * @param message the message
   * @param cause the cause
   */
  public ConversionException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Instantiates a new conversion exception.
   *
   * @param cause the cause
   */
  public ConversionException(Throwable cause) {
    super(cause);
  }
}
