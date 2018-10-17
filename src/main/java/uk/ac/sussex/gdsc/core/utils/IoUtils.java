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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Simple Input/Output utility class.
 *
 * <p>Any {@link IOException} is logged but not re-thrown.
 */
public final class IoUtils {

  /** The logger. */
  private static final Logger logger = Logger.getLogger(IoUtils.class.getName());

  /**
   * No public construction.
   */
  private IoUtils() {}

  /**
   * Save an array to file, one record per line.
   *
   * <p>Any {@link IOException} is logged but not re-thrown.
   *
   * @param header The header
   * @param data The data
   * @param filename The filename
   * @return true if all OK, false if an error occurred
   */
  public static boolean save(String header, double[] data, String filename) {
    try (BufferedWriter file = new BufferedWriter(new FileWriter(filename))) {
      if (!TextUtils.isNullOrEmpty(header)) {
        file.write(header);
        file.newLine();
      }
      if (data != null) {
        for (final double d : data) {
          file.write(Double.toString(d));
          file.newLine();
        }
      }
      return true;
    } catch (final IOException ex) {
      logger.log(Level.WARNING, "Unable to save data to file", ex);
    }
    return false;
  }

  /**
   * Save an array to file, one record per line.
   *
   * <p>Any {@link IOException} is logged but not re-thrown.
   *
   * @param data The data
   * @param filename The filename
   * @return true if all OK, false if an error occurred
   */
  public static boolean save(double[] data, String filename) {
    return save(null, data, filename);
  }

  /**
   * Save an array to file, one record per line.
   *
   * <p>Any {@link IOException} is logged but not re-thrown.
   *
   * @param header The header
   * @param data The data
   * @param filename The filename
   * @return true if all OK, false if an error occurred
   */
  public static boolean save(String header, int[] data, String filename) {
    try (BufferedWriter file = new BufferedWriter(new FileWriter(filename))) {
      if (!TextUtils.isNullOrEmpty(header)) {
        file.write(header);
        file.newLine();
      }
      if (data != null) {
        for (final int d : data) {
          file.write(Integer.toString(d));
          file.newLine();
        }
      }
      return true;
    } catch (final IOException ex) {
      logger.log(Level.WARNING, "Unable to save data to file", ex);
    }
    return false;
  }

  /**
   * Save an array to file, one record per line.
   *
   * <p>Any {@link IOException} is logged but not re-thrown.
   *
   * @param data The data
   * @param filename The filename
   * @return true if all OK, false if an error occurred
   */
  public static boolean save(int[] data, String filename) {
    return save(null, data, filename);
  }

  /**
   * Save an array to file, one record per line.
   *
   * <p>Any {@link IOException} is logged but not re-thrown.
   *
   * @param header The header
   * @param data The data
   * @param filename The filename
   * @return true if all OK, false if an error occurred
   */
  public static boolean save(String header, float[] data, String filename) {
    try (BufferedWriter file = new BufferedWriter(new FileWriter(filename))) {
      if (!TextUtils.isNullOrEmpty(header)) {
        file.write(header);
        file.newLine();
      }
      if (data != null) {
        for (final float d : data) {
          file.write(Float.toString(d));
          file.newLine();
        }
      }
      return true;
    } catch (final IOException ex) {
      logger.log(Level.WARNING, "Unable to save data to file", ex);
    }
    return false;
  }

  /**
   * Save an array to file, one record per line.
   *
   * <p>Any {@link IOException} is logged but not re-thrown.
   *
   * @param data The data
   * @param filename The filename
   * @return true if all OK, false if an error occurred
   */
  public static boolean save(float[] data, String filename) {
    return save(null, data, filename);
  }
}
