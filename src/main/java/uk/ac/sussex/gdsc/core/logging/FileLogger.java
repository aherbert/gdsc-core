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
package uk.ac.sussex.gdsc.core.logging;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * Logs messages to a file.
 */
public class FileLogger implements Logger {
  private OutputStreamWriter os = null;

  /**
   * Instantiates a new file logger.
   *
   * @param filename the filename
   * @throws FileNotFoundException If the file cannot be created
   */
  @SuppressWarnings("resource")
  public FileLogger(String filename) throws FileNotFoundException {
    this(new FileOutputStream(filename));
  }

  /**
   * Instantiates a new file logger.
   *
   * @param fos the file output stream
   */
  public FileLogger(FileOutputStream fos) {
    os = new OutputStreamWriter(new BufferedOutputStream(fos));
  }

  /**
   * Log the message to the file.
   *
   * @param message the message
   */
  @Override
  public void info(String message) {
    if (os == null) {
      return;
    }
    synchronized (os) {
      try {
        os.write(message);
        if (!message.endsWith("\n")) {
          os.write('\n');
        }
      } catch (final IOException e) {
        close();
      }
    }
  }

  /**
   * Log the arguments using the given format to the Java console. Appends a new line to the
   * message.
   *
   * @param format the format
   * @param args the args
   */
  @Override
  public void info(String format, Object... args) {
    info(String.format(format, args));
  }

  /** {@inheritDoc} */
  @Override
  public void debug(String message) {
    info(message);
  }

  /** {@inheritDoc} */
  @Override
  public void debug(String format, Object... args) {
    info(format, args);
  }

  /** {@inheritDoc} */
  @Override
  public void error(String message) {
    info(message);
  }

  /** {@inheritDoc} */
  @Override
  public void error(String format, Object... args) {
    info(format, args);
  }

  /**
   * Close the file.
   */
  public void close() {
    if (os == null) {
      return;
    }
    synchronized (os) {
      try {
        os.close();
      } catch (final IOException e) { // Ignore
      } finally {
        os = null;
      }
    }
  }
}
