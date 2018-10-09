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

import uk.ac.sussex.gdsc.test.junit5.*;
import uk.ac.sussex.gdsc.test.rng.RngFactory;
import org.junit.jupiter.api.*;

import uk.ac.sussex.gdsc.test.junit5.*;
import uk.ac.sussex.gdsc.test.rng.RngFactory;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Simple Input/Output class
 */
public class IO {
  /**
   * Save an array to file
   *
   * @param header The header
   * @param data The data
   * @param filename The filename
   * @return true if all OK, false if an error occurred
   */
  public static boolean save(String header, double[] data, String filename) {
    boolean ok = true;
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
    } catch (final IOException e) {
      ok = false;
    }
    return ok;
  }

  /**
   * Save an array to file
   *
   * @param data The data
   * @param filename The filename
   * @return true if all OK, false if an error occurred
   */
  public static boolean save(double[] data, String filename) {
    return save(null, data, filename);
  }

  /**
   * Save an array to file
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
    } catch (final IOException e) { // Ignore
    }
    return false;
  }

  /**
   * Save an array to file
   *
   * @param data The data
   * @param filename The filename
   * @return true if all OK, false if an error occurred
   */
  public static boolean save(int[] data, String filename) {
    return save(null, data, filename);
  }

  /**
   * Save an array to file
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
    } catch (final IOException e) { // Ignore
    }
    return false;
  }

  /**
   * Save an array to file
   *
   * @param data The data
   * @param filename The filename
   * @return true if all OK, false if an error occurred
   */
  public static boolean save(float[] data, String filename) {
    return save(null, data, filename);
  }
}
