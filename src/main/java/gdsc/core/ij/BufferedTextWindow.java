package gdsc.core.ij;

/*----------------------------------------------------------------------------- 
 * GDSC Plugins for ImageJ
 * 
 * Copyright (C) 2016 Alex Herbert
 * Genome Damage and Stability Centre
 * University of Sussex, UK
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *---------------------------------------------------------------------------*/
import java.awt.Frame;

/*----------------------------------------------------------------------------- 
 * GDSC Plugins for ImageJ
 * 
 * Copyright (C) 2016 Alex Herbert
 * Genome Damage and Stability Centre
 * University of Sussex, UK
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *---------------------------------------------------------------------------*/

import ij.text.TextPanel;
import ij.text.TextWindow;
import ij.text.TextWindow2;

/**
 * Buffer to the ImageJ text window. Updates the display when 10 lines have been reached (to auto-layout columns)_and
 * then at the specified increments.
 */
public class BufferedTextWindow
{
	public final Frame textWindow;
	private final TextPanel textPanel;
	private int count = 0;
	private int nextFlush = 10;
	private int increment = 10;

	public BufferedTextWindow(TextWindow textWindow)
	{
		this.textWindow = textWindow;
		textPanel = textWindow.getTextPanel();
	}

	public BufferedTextWindow(TextWindow2 textWindow)
	{
		this.textWindow = textWindow;
		textPanel = textWindow.getTextPanel();
	}

	/**
	 * Append the text, updating the display at the configured interval
	 * 
	 * @param text
	 */
	public void append(String text)
	{
		textPanel.appendWithoutUpdate(text);
		if (++count == nextFlush)
		{
			flush();
		}
	}

	/**
	 * Flush the data to update the display and reset the next flush interval
	 */
	public void flush()
	{
		if (textPanel.isShowing())
			textPanel.updateDisplay();
		nextFlush = count + increment;
	}

	/**
	 * @return the increment
	 */
	public int getIncrement()
	{
		return increment;
	}

	/**
	 * @param increment
	 *            the increment to set
	 */
	public void setIncrement(int increment)
	{
		this.increment = increment;
	}

	public boolean isVisible()
	{
		return textWindow.isVisible();
	}
}