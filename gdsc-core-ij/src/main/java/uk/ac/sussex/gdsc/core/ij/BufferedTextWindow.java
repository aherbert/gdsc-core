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

import ij.text.TextPanel;
import ij.text.TextWindow;

/**
 * Buffer to the ImageJ text window. Updates the display when 10 lines have been reached (to
 * auto-layout columns) and then at the specified increments.
 */
public class BufferedTextWindow implements AutoCloseable {
  private final Output output;
  private int count;

  /**
   * The count for the next flush operation.
   *
   * <p>This is initialised to the maximum count of rows where ImageJ will force recalculation of
   * the column headers.
   */
  private int nextFlush = 10;

  /** The number of rows that must be appended before an update of the display. */
  private int increment = 10;

  /**
   * An output that accepts text and can be flushed.
   */
  interface Output {
    /**
     * Write the text to the output.
     *
     * @param text the text
     */
    void write(String text);

    /**
     * Flushes the output by writing any buffered output to the underlying stream.
     */
    void flush();
  }

  /**
   * Instantiates a new buffered text window.
   *
   * @param textWindow the text window
   */
  public BufferedTextWindow(TextWindow textWindow) {
    final TextPanel textPanel = textWindow.getTextPanel();
    output = new Output() {
      @Override
      public void write(String text) {
        textPanel.appendWithoutUpdate(text);
      }

      @Override
      public void flush() {
        if (textPanel.isShowing()) {
          textPanel.updateDisplay();
        }
      }
    };
  }

  /**
   * Instantiates a new buffered text window.
   *
   * @param output the output
   */
  BufferedTextWindow(Output output) {
    this.output = output;
  }

  /**
   * Append the text, updating the display at the configured interval.
   *
   * @param text the text
   */
  public void append(String text) {
    output.write(text);
    if (++count == nextFlush) {
      flush();
    }
  }

  /**
   * Flush the data to update the display and reset the next flush interval.
   *
   * <p>This method should be called if no more data will be sent to the table.
   */
  public void flush() {
    output.flush();
    nextFlush = count + increment;
  }

  /**
   * Gets the increment. This is the number of calls to {@link #append(String)} before an update of
   * the displayed text panel using {@link #flush()}.
   *
   * @return the increment.
   */
  public int getIncrement() {
    return increment;
  }

  /**
   * Sets the increment. This is the number of calls to {@link #append(String)} before an update of
   * the displayed text panel using {@link #flush()}.
   *
   * <p>If set to zero then the {@link #flush()} will only be called once when the count reaches the
   * largest row count used by ImageJ to auto-layout the columns.
   *
   * @param increment the increment to set.
   */
  public void setIncrement(int increment) {
    this.increment = increment;
  }

  /**
   * Flushes the data to the display.
   *
   * @see #flush()
   */
  @Override
  public void close() {
    flush();
  }
}
