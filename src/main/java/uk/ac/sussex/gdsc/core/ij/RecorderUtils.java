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

package uk.ac.sussex.gdsc.core.ij;

import ij.plugin.frame.Recorder;
import uk.ac.sussex.gdsc.core.data.VisibleForTesting;
import uk.ac.sussex.gdsc.core.utils.LocalList;

/**
 * Contains helper functions for the recorder.
 */
public final class RecorderUtils {

  /**
   * Define methods used from the ImageJ Recorder.
   */
  interface RecorderCommand {

    /**
     * Returns the name of the command currently being recorded, or null.
     *
     * @return the command
     */
    String getCommand();

    /**
     * Used by GenericDialog to determine if any options have been recorded.
     *
     * @return the command options
     */
    String getCommandOptions();

    /**
     * Starts recording a command. Does nothing if the recorder is not open or the command being
     * recorded has called IJ.run().
     *
     * @param command the new command
     */
    void setCommand(String command);

    /**
     * Writes the current command and options to the Recorder window, then resets the recorder
     * command name and options.
     */
    void saveCommand();

    /**
     * Record option.
     *
     * @param key the key
     */
    void recordOption(String key);

    /**
     * Record option.
     *
     * @param key the key
     * @param value the value
     */
    void recordOption(String key, String value);
  }

  /**
   * Default ImageJ Recorder implementation.
   */
  static final class ImageJRecorderCommand implements RecorderCommand {
    /** The instance. */
    static final ImageJRecorderCommand INSTANCE = new ImageJRecorderCommand();

    private ImageJRecorderCommand() {
      /* do nothing */
    }

    @Override
    public String getCommand() {
      return Recorder.getCommand();
    }

    @Override
    public String getCommandOptions() {
      return Recorder.getCommandOptions();
    }

    @Override
    public void setCommand(String command) {
      Recorder.setCommand(command);
    }

    @Override
    public void saveCommand() {
      Recorder.saveCommand();
    }

    @Override
    public void recordOption(String key) {
      Recorder.recordOption(key);
    }

    @Override
    public void recordOption(String key, String value) {
      Recorder.recordOption(key, value);
    }
  }

  private static RecorderCommand recorder = ImageJRecorderCommand.INSTANCE;

  /** No construction. */
  private RecorderUtils() {}

  /**
   * Reset the recorder for all the named keys.
   *
   * @param keys the keys
   */
  public static void resetRecorder(String[] keys) {
    if (keys == null || keys.length == 0) {
      return;
    }

    // Get the recorder.options, remove all the labels, and update the reduced recorder.options
    final String commandName = recorder.getCommand();
    final String commandOptions = recorder.getCommandOptions();
    if (commandName == null || commandOptions == null) {
      return;
    }

    // We only support labels added with
    // recorder.recordOption(String)
    // recorder.recordOption(String,String)
    // These will create a key in the command options of:
    // " "+key
    // " "+key+"="+value
    // " "+key+"=["+value+"]"
    boolean ignored = false;
    final LocalList<String[]> pairs = new LocalList<>();
    int current = 0;
    final int len = commandOptions.length();
    while (current < len) {
      // Find the next non-space character, this will be the start of a key
      while (current < len && commandOptions.charAt(current) == ' ') {
        current++;
      }
      if (current == len) {
        break;
      }

      // Find the end of the key.
      // This could be a space or an equals.
      final int keyEnd = findKeyEnd(commandOptions, len, current);

      final String key = commandOptions.substring(current, keyEnd);

      current = keyEnd;

      // Find the value if present
      String value = null;
      if (keyEnd < len && commandOptions.charAt(keyEnd) == '=') {
        // There is a value. This may be surrounded by brackets
        int valueStart = keyEnd + 1;
        int valueEnd;
        if (valueStart < len && commandOptions.charAt(valueStart) == '[') {
          valueStart++;
          valueEnd = commandOptions.indexOf(']', valueStart);
        } else {
          valueEnd = commandOptions.indexOf(' ', valueStart);
        }
        if (valueEnd < 0) {
          valueEnd = len;
        }
        value = commandOptions.substring(valueStart, valueEnd);

        current = valueEnd + 1;
      }

      // Check key should be ignored
      if (ignore(key, keys)) {
        ignored = true;
      } else {
        pairs.add(new String[] {key, value});
      }
    }

    if (!ignored) {
      // No keys to remove
      return;
    }

    // Reset
    recorder.setCommand(null);
    recorder.saveCommand();
    // Re-record all the remaining pairs
    recorder.setCommand(commandName);
    for (int i = 0; i < pairs.size(); i++) {
      final String[] pair = pairs.unsafeGet(i);
      final String key = pair[0];
      String value = pair[1];
      if (value == null) {
        recorder.recordOption(key);
      } else {
        // As per the GenericDialog ensure that empty strings are wrapped
        if (value.isEmpty()) {
          value = "[]";
        }
        recorder.recordOption(key, value);
      }
    }
  }

  private static int findKeyEnd(String commandOptions, int len, int start) {
    int index = start;
    while (index < len) {
      final char c = commandOptions.charAt(index);
      if (c == ' ' || c == '=') {
        break;
      }
      index++;
    }
    return index;
  }

  private static boolean ignore(String key, String[] keys) {
    for (final String value : keys) {
      if (value.equalsIgnoreCase(key)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Sets the recorder.
   *
   * @param recorder the new recorder
   */
  @VisibleForTesting
  static void setRecorder(RecorderCommand recorder) {
    RecorderUtils.recorder = recorder;
  }
}
