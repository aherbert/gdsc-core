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
 * Copyright (C) 2011 - 2023 Alex Herbert
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
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Simple File utility class.
 *
 * <p>Any {@link IOException} is logged but not re-thrown.
 */
public final class FileUtils {
  /** The logger. */
  private static final Logger logger = Logger.getLogger(FileUtils.class.getName());
  /** The dot character '.'. */
  private static final char DOT = '.';

  /**
   * No public construction.
   */
  private FileUtils() {}

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
    try (BufferedWriter file = openFile(filename)) {
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
      logIoException(ex);
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
    try (BufferedWriter file = openFile(filename)) {
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
      logIoException(ex);
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
    try (BufferedWriter file = openFile(filename)) {
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
      logIoException(ex);
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

  /**
   * Save the text to file.
   *
   * <p>Any {@link IOException} is logged but not re-thrown.
   *
   * @param filename the filename
   * @param text the text
   * @return true, if successful
   */
  public static boolean save(String filename, String text) {
    try (BufferedWriter file = openFile(filename)) {
      file.write(text);
      return true;
    } catch (final IOException ex) {
      logIoException(ex);
    }
    return false;
  }

  /**
   * Open the file.
   *
   * @param filename the filename
   * @return the buffered writer
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private static BufferedWriter openFile(String filename) throws IOException {
    return Files.newBufferedWriter(Paths.get(filename));
  }

  /**
   * Log the IO exception.
   *
   * @param exception the exception
   */
  private static void logIoException(final IOException exception) {
    logger.log(Level.WARNING, "Unable to save data to file", exception);
  }

  /**
   * Add the platform specific file separator character to the directory (if missing).
   *
   * @param directory the directory
   * @return The directory
   * @see File#separatorChar
   */
  public static String addFileSeparator(String directory) {
    return (directory.endsWith("/") || directory.endsWith("\\")) ? directory
        : directory + File.separatorChar;
  }

  /**
   * Creates the parent of the specified path.
   *
   * <p>Does nothing if the parent is null, or already exists.
   *
   * <p>Note: Any {@link SecurityException} is wrapped with an {@link IOException}.
   *
   * @param path the path
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static void createParent(Path path) throws IOException {
    final Path parent = path.getParent();
    if (parent != null) {
      try {
        Files.createDirectories(parent);
      } catch (final SecurityException ex) {
        throw new IOException("Failed to create parent directory: " + path.getParent(), ex);
      }
    }
  }

  /**
   * Remove the filename extension. This is the text following the last dot ('.') character in the
   * filename. Any dot characters before the last file separator character are ignored.
   *
   * <p>A null input will return the empty string.
   *
   * @param filename the filename
   * @return the new filename
   * @see File#separatorChar
   */
  public static String removeExtension(String filename) {
    if (TextUtils.isNullOrEmpty(filename)) {
      return "";
    }
    final int index = filename.lastIndexOf(DOT);
    final int index2 = filename.lastIndexOf(File.separatorChar);
    if (index > index2) {
      return filename.substring(0, index);
    }
    return filename;
  }

  /**
   * Get the filename extension. This is the text following the last dot ('.') character in the
   * filename. Any dot characters before the last file separator character are ignored.
   *
   * <p>A null input will return the empty string.
   *
   * @param filename the filename
   * @return the filename extension (or the empty string)
   * @see File#separatorChar
   */
  public static String getExtension(String filename) {
    if (filename != null) {
      final int index = filename.lastIndexOf(DOT);
      final int index2 = filename.lastIndexOf(File.separatorChar);
      if (index > index2) {
        return filename.substring(index + 1);
      }
    }
    return "";
  }

  /**
   * Get the length of the filename extension. This is the text following the last dot ('.')
   * character in the filename. Any dot characters before the last file separator character are
   * ignored.
   *
   * <p>A null input will return 0.
   *
   * @param filename the filename
   * @return the length of the filename extension
   * @see File#separatorChar
   */
  public static int getExtensionLength(String filename) {
    if (filename != null) {
      final int index = filename.lastIndexOf(DOT);
      final int index2 = filename.lastIndexOf(File.separatorChar);
      if (index > index2) {
        return filename.length() - (index + 1);
      }
    }
    return 0;
  }

  /**
   * Replace the filename extension with the specified extension. This is the text following the
   * last dot ('.') character in the filename. Any dot characters before the last file separator
   * character are ignored.
   *
   * <p>If the extension is empty then the effect is the removal of any current extension.
   *
   * <p>A null input for the filename or extension will be treated as the empty string.
   *
   * <p>If the extension is missing the dot character at the start it is prefixed. The method does
   * not check that the extension is only dot characters.
   *
   * @param filename the filename
   * @param extension the extension
   * @return the new filename
   * @see File#separatorChar
   */
  public static String replaceExtension(String filename, String extension) {
    final int filenameLength = TextUtils.getLength(filename);
    final int extensionLength = TextUtils.getLength(extension);
    final StringBuilder sb = new StringBuilder(filenameLength + extensionLength + 1);

    if (filenameLength != 0) {
      addFilenameWithoutExtension(sb, filename);
    }
    if (extensionLength != 0) {
      addExtension(sb, extension);
    }
    return sb.toString();
  }

  private static void addFilenameWithoutExtension(StringBuilder sb, String filename) {
    final int index = filename.lastIndexOf(DOT);
    final int index2 = filename.lastIndexOf(File.separatorChar);
    if (index > index2) {
      sb.append(filename, 0, index);
    } else {
      sb.append(filename);
    }
  }

  private static void addExtension(StringBuilder sb, String extension) {
    if (extension.charAt(0) != DOT) {
      sb.append(DOT);
    }
    sb.append(extension);
  }

  /**
   * Adds the filename extension if currently absent. This is the text following the last dot ('.')
   * character in the filename. Any dot characters before the last file separator character are
   * ignored.
   *
   * <p>A null input for the filename or extension will be treated as the empty string.
   *
   * <p>If the extension is missing the dot character at the start it is prefixed. The method does
   * not check that the extension is only dot characters.
   *
   * @param filename the filename
   * @param extension the extension (must not be empty)
   * @return the string
   * @see File#separatorChar
   */
  public static String addExtensionIfAbsent(String filename, String extension) {
    if (getExtensionLength(filename) != 0) {
      return filename;
    }
    return replaceExtension(filename, extension);
  }

  /**
   * Returns the name of the file or directory. This is the text after the occurrence of the file
   * separator character.
   *
   * <p>Note: Any trailing file separator characters are trimmed from the end of the path.
   *
   * <p>A null input for the path will be treated as the empty string.
   *
   * <pre>
   * null         ==  ""
   * ""           ==  ""
   * "//"         ==  ""
   * "file"       ==  "file"
   * "dir/file"   ==  "file"
   * "dir/dir2/"  ==  "dir2"
   * "dir/dir2//" ==  "dir2"
   * </pre>
   *
   * @param path the path
   * @return The name of the file or directory denoted by this path, or the empty string
   * @see File#separatorChar
   */
  public static String getName(String path) {
    if (path == null) {
      return "";
    }
    // Trim final separator characters
    int length = path.length();
    while (length > 0 && path.charAt(length - 1) == File.separatorChar) {
      length--;
    }
    if (length == 0) {
      return "";
    }
    // Find the next separator character
    final int index = path.lastIndexOf(File.separatorChar, length - 1);
    return path.substring(index + 1, length);
  }

  /**
   * Skip the input by the given number of bytes.
   *
   * <p>Throws an {@link EOFException} if the number of bytes skipped was incorrect.
   *
   * @param in the input stream
   * @param numberOfBytes the number of bytes
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static void skip(InputStream in, long numberOfBytes) throws IOException {
    // Check the correct number of bytes were skipped
    if (numberOfBytes > 0 && in.skip(numberOfBytes) != numberOfBytes) {
      throw new EOFException();
    }
  }
}
