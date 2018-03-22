package ij.gui;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.ScrollPane;

import javax.swing.JScrollPane;
import javax.swing.JViewport;

import ij.IJ;

/*----------------------------------------------------------------------------- 
 * GDSC Plugins for ImageJ
 * 
 * Copyright (C) 2018 Alex Herbert
 * Genome Damage and Stability Centre
 * University of Sussex, UK
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *---------------------------------------------------------------------------*/

/**
 * Allows resizing of window components for the current screen dimensions
 */
public class ScreenDimensionHelper
{
	// Store the screen dimension
	private static Dimension screenDimension;
	static
	{
		screenDimension = IJ.getScreenSize();
	}
	
	/**
	 * Gets the screen size.
	 *
	 * @return the screen size
	 */
	public static Dimension getScreenSize()
	{
		return new Dimension(screenDimension);
	}

	// Max width - Set to a reasonable value for current screen resolution.
	private int maxWidth = screenDimension.width - 100;
	// Max height - Set to a reasonable value for current screen resolution.
	private int maxHeight = screenDimension.height - 150;

	private int minWidth = Math.min(600, screenDimension.width);
	private int minHeight = Math.min(400, screenDimension.height);

	/**
	 * Setup the scroll pane using the preferred size. For example this can be the preferred size of the first component
	 * of the scroll pane.
	 *
	 * @param scroll
	 *            the scroll
	 * @param preferredSize
	 *            the preferred size
	 */
	public void setup(ScrollPane scroll, Dimension preferredSize)
	{
		clipDimensions(preferredSize);

		Insets insets = scroll.getInsets();
		preferredSize.width += insets.left + insets.right;
		preferredSize.height += insets.top + insets.bottom;

		if (IJ.isMacintosh())
		{
			// This is needed as the OSX scroll pane adds scrollbars when the panel 
			// is close in size to the scroll pane
			int padding = 15;
			preferredSize.width += padding;
			preferredSize.height += padding;
		}

		scroll.setPreferredSize(preferredSize);
		scroll.setSize(preferredSize);
	}

	private void clipDimensions(Dimension preferredSize)
	{
		if (maxWidth > 0)
			preferredSize.width = Math.min(preferredSize.width, maxWidth);
		if (maxHeight > 0)
			preferredSize.height = Math.min(preferredSize.height, maxHeight);
		if (minWidth > 0)
			preferredSize.width = Math.max(preferredSize.width, minWidth);
		if (minHeight > 0)
			preferredSize.height = Math.max(preferredSize.height, minHeight);
	}

	/**
	 * Setup the scroll pane using the preferred size.
	 *
	 * @param scroll
	 *            the scroll
	 */
	public void setup(JScrollPane scroll)
	{
		JViewport viewport = scroll.getViewport();
		Dimension preferredSize = viewport.getViewSize();
		clipDimensions(preferredSize);
		viewport.setPreferredSize(preferredSize);
	}

	/**
	 * Gets the max width.
	 *
	 * @return the max width
	 */
	public int getMaxWidth()
	{
		return maxWidth;
	}

	/**
	 * Sets the max width.
	 *
	 * @param maxWidth
	 *            the new max width
	 */
	public void setMaxWidth(int maxWidth)
	{
		this.maxWidth = maxWidth;
	}

	/**
	 * Gets the max height.
	 *
	 * @return the max height
	 */
	public int getMaxHeight()
	{
		return maxHeight;
	}

	/**
	 * Sets the max height.
	 *
	 * @param maxHeight
	 *            the new max height
	 */
	public void setMaxHeight(int maxHeight)
	{
		this.maxHeight = maxHeight;
	}

	/**
	 * Sets the max size.
	 *
	 * @param maxWidth
	 *            the max width
	 * @param maxHeight
	 *            the max height
	 */
	public void setMaxSize(int maxWidth, int maxHeight)
	{
		setMaxWidth(maxWidth);
		setMaxHeight(maxHeight);
	}

	/**
	 * Gets the min width.
	 *
	 * @return the min width
	 */
	public int getMinWidth()
	{
		return minWidth;
	}

	/**
	 * Sets the min width.
	 *
	 * @param minWidth
	 *            the new min width
	 */
	public void setMinWidth(int minWidth)
	{
		this.minWidth = minWidth;
	}

	/**
	 * Gets the min height.
	 *
	 * @return the min height
	 */
	public int getMinHeight()
	{
		return minHeight;
	}

	/**
	 * Sets the min height.
	 *
	 * @param minHeight
	 *            the new min height
	 */
	public void setMinHeight(int minHeight)
	{
		this.minHeight = minHeight;
	}

	/**
	 * Sets the min size.
	 *
	 * @param minWidth
	 *            the min width
	 * @param minHeight
	 *            the min height
	 */
	public void setMinSize(int minWidth, int minHeight)
	{
		setMinWidth(minWidth);
		setMinHeight(minHeight);
	}
}
