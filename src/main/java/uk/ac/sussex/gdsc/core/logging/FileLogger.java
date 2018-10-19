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

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Logs messages to a file.
 */
public class FileLogger implements Logger, Closeable {
  /** The new line string. */
  private static final String NEW_LINE = System.lineSeparator();

  /** The writer for the output file. */
  private BufferedWriter output = null;

  /** Main lock guarding all access to the output stream. */
  private final ReentrantLock lock = new ReentrantLock(false);

  /**
   * Instantiates a new file logger using the UTF-8 charset.
   *
   * @param filename the filename
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public FileLogger(String filename) throws IOException {
    this(filename, StandardCharsets.UTF_8);
  }

  /**
   * Instantiates a new file logger.
   *
   * @param filename the filename
   * @param charset the charset
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public FileLogger(String filename, Charset charset) throws IOException {
    output = Files.newBufferedWriter(new File(filename).toPath(), charset);
  }

  /**
   * Log the message to the file.
   *
   * @param message the message
   */
  @Override
  public void info(String message) {
    if (isClosed()) {
      return;
    }
    lock.lock();
    try {
      if (isClosed()) {
        return;
      }
      output.write(message);
      if (!message.endsWith(NEW_LINE)) {
        output.write(NEW_LINE);
      }
    } catch (final IOException ex) {
      close();
    } finally {
      lock.unlock();
    }
  }

  /**
   * Log the arguments using the given format to the Java console.
   *
   * <p>Appends a new line to the message.
   *
   * @param format the format
   * @param args the args
   */
  @Override
  public void info(String format, Object... args) {
    info(String.format(format, args));
  }

  @Override
  public void debug(String message) {
    info(message);
  }

  @Override
  public void debug(String format, Object... args) {
    info(format, args);
  }

  @Override
  public void error(String message) {
    info(message);
  }

  @Override
  public void error(String format, Object... args) {
    info(format, args);
  }

  @Override
  public void close() {
    if (isClosed()) {
      return;
    }
    lock.lock();
    try {
      if (isClosed()) {
        return;
      }
      output.close();
    } catch (final IOException ex) {
      // Ignore
    } finally {
      output = null;
      lock.unlock();
    }
  }

  /**
   * Checks if is closed.
   *
   * @return true, if is closed
   */
  public boolean isClosed() {
    return output == null;
  }
}
