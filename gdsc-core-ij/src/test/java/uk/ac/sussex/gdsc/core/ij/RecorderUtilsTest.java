/*-
 * #%L
 * Genome Damage and Stability Centre Core ImageJ Package
 *
 * Contains core utilities for image analysis in ImageJ and is used by:
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

package uk.ac.sussex.gdsc.core.ij;

import ij.plugin.frame.Recorder;
import java.awt.GraphicsEnvironment;
import java.util.Locale;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.core.ij.RecorderUtils.ImageJRecorderCommand;
import uk.ac.sussex.gdsc.core.ij.RecorderUtils.RecorderCommand;

@SuppressWarnings({"javadoc"})
class RecorderUtilsTest {

  private static final class DummyRecorderCommand implements RecorderCommand {
    /** The instance. */
    static final DummyRecorderCommand INSTANCE = new DummyRecorderCommand();

    String commandName;
    String commandOptions;

    private DummyRecorderCommand() {
      /* do nothing */
    }

    @Override
    public String getCommand() {
      return commandName;
    }

    @Override
    public String getCommandOptions() {
      return commandOptions;
    }

    @Override
    public void setCommand(String command) {
      this.commandName = command;
      commandOptions = null;
    }

    @Override
    public void saveCommand() {
      commandName = commandOptions = null;
    }

    @Override
    public void recordOption(String key) {
      if (key == null) {
        return;
      }
      if (commandOptions == null && key.equals(" ")) {
        commandOptions = " ";
      } else {
        key = trimKey(key);
        checkForDuplicate(" " + key, "");
        if (commandOptions == null || commandOptions == " ") {
          commandOptions = key;
        } else {
          commandOptions += " " + key;
        }
      }
    }

    @Override
    public void recordOption(String key, String value) {
      if (key == null) {
        return;
      }
      key = Recorder.fixString(key);
      key = trimKey(key);
      value = addQuotes(value);
      checkForDuplicate(key + "=", value);
      if (commandOptions == null) {
        commandOptions = key + "=" + value;
      } else {
        commandOptions += " " + key + "=" + value;
      }
    }

    static String addQuotes(String value) {
      if (value == null) {
        value = "";
      }
      final int index = value.indexOf(' ');
      if (index > -1) {
        value = "[" + value + "]";
      }
      return value;
    }

    void checkForDuplicate(String key, String value) {
      if (commandOptions != null && commandName != null && commandOptions.indexOf(key) != -1
          && (value.equals("") || commandOptions.indexOf(value) == -1)) {
        if (key.endsWith("=")) {
          key = key.substring(0, key.length() - 1);
        }
        Assertions.fail("Duplicate keyword:\n \n" + "    Command: " + "\"" + commandName + "\"\n"
            + "    Keyword: " + "\"" + key + "\"\n" + "    Value: " + value + "\n \n"
            + "Add an underscore to the corresponding label\n"
            + "in the dialog to make the first word unique.");
      }
    }

    static String trimKey(String key) {
      int index = key.indexOf(" ");
      if (index > -1) {
        key = key.substring(0, index);
      }
      index = key.indexOf(":");
      if (index > -1) {
        key = key.substring(0, index);
      }
      key = key.toLowerCase(Locale.US);
      return key;
    }
  }

  /** The recorder instance. */
  static RecorderCommand recorder = null;

  @BeforeAll
  public static void beforeAll() {
    if (GraphicsEnvironment.isHeadless()) {
      // Exercise the default
      ImageJRecorderCommand.INSTANCE.setCommand(null);
      ImageJRecorderCommand.INSTANCE.saveCommand();
      ImageJRecorderCommand.INSTANCE.getCommand();
      ImageJRecorderCommand.INSTANCE.getCommandOptions();
      ImageJRecorderCommand.INSTANCE.recordOption("key");
      ImageJRecorderCommand.INSTANCE.recordOption("key", "value");
      recorder = DummyRecorderCommand.INSTANCE;
      RecorderUtils.setRecorder(recorder);
    } else {
      // We need an instance otherwise the static method calls to
      // Recorder.setCommand() are ignored as there is no text area
      Assertions.assertNotNull(new Recorder(false));
      recorder = ImageJRecorderCommand.INSTANCE;
    }
  }

  @AfterAll
  public static void afterAll() {
    // Allow GC to do its work
    recorder = null;
  }

  /**
   * No assertions. This just hits code coverage when the command name or options are null.
   */
  @Test
  void canResetRecorderWithNoCommand() {
    recorder.saveCommand();
    recorder.setCommand(null);
    final String[] keys = {"1", "2"};
    RecorderUtils.resetRecorder(keys);
    recorder.setCommand("test");
    RecorderUtils.resetRecorder(keys);
  }

  @Test
  void testWrapEmptyStrings() {
    recorder.saveCommand();
    recorder.setCommand("test");
    recorder.recordOption("a", "1");
    recorder.recordOption("b", "");
    Assertions.assertEquals("a=1 b=", recorder.getCommandOptions());
    RecorderUtils.resetRecorder(new String[] {"tmp"});
    Assertions.assertEquals("a=1 b=", recorder.getCommandOptions());
    RecorderUtils.resetRecorder(new String[] {"a"});
    Assertions.assertEquals("b=[]", recorder.getCommandOptions(),
        "Expected empty string to be wrapped in square brackets");
  }

  @Test
  void canResetRecorderWithPlainValues() {
    canResetRecorder(toArray("a", "b"), toArray("1", "2"), toArray("c", "d"), toArray("3", "4"),
        null);
    canResetRecorder(toArray("a", "b"), toArray("1", "2"), toArray("c", "d"), toArray("3", "4"),
        null);
    canResetRecorder(toArray("a", "b"), toArray("1", "2"), toArray("c", "d"), toArray("3", ""),
        null);
    canResetRecorder(toArray("a", "b"), toArray("1", "2"), toArray("c", "d"), toArray("3", "4"),
        null);
    canResetRecorder(toArray("a", "b"), toArray("1", "2"), toArray("c", "d"), toArray("", "4"),
        null);
    canResetRecorder(toArray("a", "b"), toArray("1", "2"), toArray("c", "d"), toArray("", ""),
        null);
  }

  @Test
  void canResetRecorderWithQuotedValues() {
    canResetRecorder(toArray("a", "b"), toArray("1 1", "2 2"), toArray("c", "d"),
        toArray("3 3", "4 4"), null);
    canResetRecorder(toArray("a", "b"), toArray("1 1", "2 2"), toArray("c", "d"),
        toArray("3 3", "4"), null);
    canResetRecorder(toArray("a", "b"), toArray("1 1", "2 2"), toArray("c", "d"),
        toArray("3 3", ""), null);
    canResetRecorder(toArray("a", "b"), toArray("1 1", "2 2"), toArray("c", "d"),
        toArray("3", "4 4"), null);
    canResetRecorder(toArray("a", "b"), toArray("1 1", "2 2"), toArray("c", "d"),
        toArray("", "4 4"), null);
    canResetRecorder(toArray("a", "b"), toArray("1 1", "2 2"), toArray("c", "d"), toArray("", ""),
        null);

    canResetRecorder(toArray("a", "b"), toArray("1 1", "2"), toArray("c", "d"),
        toArray("3 3", "4 4"), null);
    canResetRecorder(toArray("a", "b"), toArray("1 1", "2"), toArray("c", "d"), toArray("3 3", "4"),
        null);
    canResetRecorder(toArray("a", "b"), toArray("1 1", "2"), toArray("c", "d"), toArray("3 3", ""),
        null);
    canResetRecorder(toArray("a", "b"), toArray("1 1", "2"), toArray("c", "d"), toArray("3", "4 4"),
        null);
    canResetRecorder(toArray("a", "b"), toArray("1 1", "2"), toArray("c", "d"), toArray("", "4 4"),
        null);
    canResetRecorder(toArray("a", "b"), toArray("1 1", "2"), toArray("c", "d"), toArray("", ""),
        null);

    canResetRecorder(toArray("a", "b"), toArray("1 1", ""), toArray("c", "d"),
        toArray("3 3", "4 4"), null);
    canResetRecorder(toArray("a", "b"), toArray("1 1", ""), toArray("c", "d"), toArray("3 3", "4"),
        null);
    canResetRecorder(toArray("a", "b"), toArray("1 1", ""), toArray("c", "d"), toArray("3 3", ""),
        null);
    canResetRecorder(toArray("a", "b"), toArray("1 1", ""), toArray("c", "d"), toArray("3", "4 4"),
        null);
    canResetRecorder(toArray("a", "b"), toArray("1 1", ""), toArray("c", "d"), toArray("", "4 4"),
        null);
    canResetRecorder(toArray("a", "b"), toArray("1 1", ""), toArray("c", "d"), toArray("", ""),
        null);

    canResetRecorder(toArray("a", "b"), toArray("1", "2 2"), toArray("c", "d"),
        toArray("3 3", "4 4"), null);
    canResetRecorder(toArray("a", "b"), toArray("1", "2 2"), toArray("c", "d"), toArray("3 3", "4"),
        null);
    canResetRecorder(toArray("a", "b"), toArray("1", "2 2"), toArray("c", "d"), toArray("3 3", ""),
        null);
    canResetRecorder(toArray("a", "b"), toArray("1", "2 2"), toArray("c", "d"), toArray("3", "4 4"),
        null);
    canResetRecorder(toArray("a", "b"), toArray("1", "2 2"), toArray("c", "d"), toArray("", "4 4"),
        null);
    canResetRecorder(toArray("a", "b"), toArray("1", "2 2"), toArray("c", "d"), toArray("", ""),
        null);

    canResetRecorder(toArray("a", "b"), toArray("", "2 2"), toArray("c", "d"),
        toArray("3 3", "4 4"), null);
    canResetRecorder(toArray("a", "b"), toArray("", "2 2"), toArray("c", "d"), toArray("3 3", "4"),
        null);
    canResetRecorder(toArray("a", "b"), toArray("", "2 2"), toArray("c", "d"), toArray("3 3", ""),
        null);
    canResetRecorder(toArray("a", "b"), toArray("", "2 2"), toArray("c", "d"), toArray("3", "4 4"),
        null);
    canResetRecorder(toArray("a", "b"), toArray("", "2 2"), toArray("c", "d"), toArray("", "4 4"),
        null);
    canResetRecorder(toArray("a", "b"), toArray("", "2 2"), toArray("c", "d"), toArray("", ""),
        null);

    canResetRecorder(toArray("a", "b"), toArray("", ""), toArray("c", "d"), toArray("3 3", "4 4"),
        null);
    canResetRecorder(toArray("a", "b"), toArray("", ""), toArray("c", "d"), toArray("3 3", "4"),
        null);
    canResetRecorder(toArray("a", "b"), toArray("", ""), toArray("c", "d"), toArray("3 3", ""),
        null);
    canResetRecorder(toArray("a", "b"), toArray("", ""), toArray("c", "d"), toArray("3", "4 4"),
        null);
    canResetRecorder(toArray("a", "b"), toArray("", ""), toArray("c", "d"), toArray("", "4 4"),
        null);
    canResetRecorder(toArray("a", "b"), toArray("", ""), toArray("c", "d"), toArray("", ""), null);
  }

  @Test
  void resetRecorderIgnoresInvalidKeys() {
    canResetRecorder(toArray("a", "b"), toArray("1", "2"), toArray("c", "d"), toArray("3", "4"),
        null);
    canResetRecorder(toArray("a", "b"), toArray("1", "2"), toArray("c", "d"), toArray("3", "4"),
        new String[0]);
    canResetRecorder(toArray("a", "b"), toArray("1", "2"), toArray("c", "d"), toArray("3", "4"),
        toArray("e", "f"));
    canResetRecorder(toArray("a", "b"), toArray("1", "2"), toArray("c", "d"), toArray("3", "4"),
        toArray("e", "f"));
    canResetRecorder(toArray("a", "b"), toArray("1", "2"), toArray("c", "d"), toArray("3", ""),
        toArray("e", "f"));
    canResetRecorder(toArray("a", "b"), toArray("1", "2"), toArray("c", "d"), toArray("3", "4"),
        toArray("e", "f"));
    canResetRecorder(toArray("a", "b"), toArray("1", "2"), toArray("c", "d"), toArray("", "4"),
        toArray("e", "f"));
    canResetRecorder(toArray("a", "b"), toArray("1", "2"), toArray("c", "d"), toArray("", ""),
        toArray("e", "f"));
  }

  private static String[] toArray(String... values) {
    return values;
  }

  private static void canResetRecorder(String[] keys1, String[] values1, String[] keys2,
      String[] values2, String[] badKeys) {
    clearRecorder();
    record(keys1, values1);
    final String e1 = recorder.getCommandOptions();
    clearRecorder();
    record(keys2, values2);
    final String e2 = recorder.getCommandOptions();
    clearRecorder();
    record(keys1, values1);
    record(keys2, values2);
    final String e3 = recorder.getCommandOptions();
    RecorderUtils.resetRecorder(keys2);
    final String o1 = recorder.getCommandOptions();
    Assertions.assertNotEquals(e3, o1, "-keys2 did not change");
    Assertions.assertEquals(e1, o1, "-keys2");
    RecorderUtils.resetRecorder(badKeys);
    final String o1b = recorder.getCommandOptions();
    Assertions.assertEquals(o1, o1b, "-badkeys2");
    clearRecorder();
    record(keys1, values1);
    record(keys2, values2);
    RecorderUtils.resetRecorder(keys1);
    final String o2 = recorder.getCommandOptions();
    Assertions.assertNotEquals(e3, o2, "-keys1 did not change");
    Assertions.assertEquals(e2, o2, "-keys1");
    RecorderUtils.resetRecorder(badKeys);
    final String o2b = recorder.getCommandOptions();
    Assertions.assertEquals(o2, o2b, "-badkeys1");
  }

  private static void clearRecorder() {
    recorder.saveCommand();
    recorder.setCommand("Test");
  }

  private static void record(String[] keys1, String[] values1) {
    for (int i = 0; i < keys1.length; i++) {
      if (values1[i].isEmpty()) {
        recorder.recordOption(keys1[i]);
      } else {
        recorder.recordOption(keys1[i], values1[i]);
      }
    }
  }
}
