package ij.plugin;

import java.awt.Dimension;
import java.util.Arrays;

/*----------------------------------------------------------------------------- 
 * GDSC Software
 * 
 * Copyright (C) 2016 Alex Herbert
 * Genome Damage and Stability Centre
 * University of Sussex, UK
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *---------------------------------------------------------------------------*/

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.gui.PlotWindow;

/**
 * Extend the standard ImageJ window organiser plugin and make the methods public
 * <p>
 * Adds stateful methods to the instance so it can layout any windows added to it.
 */
public class WindowOrganiser extends ij.plugin.WindowOrganizer
{
	private static final int XSTART = 4, YSTART = 80, XOFFSET = 8, YOFFSET = 24, MAXSTEP = 200, GAP = 2;
	private int titlebarHeight = IJ.isMacintosh() ? 40 : 20;

	private int[] list = new int[10];
	private int count = 0;

	/** Set to true to ignore any added window. */
	public boolean ignore = false;

	/**
	 * Adds the window ID to the instance
	 *
	 * @param id
	 *            the window id
	 */
	public void add(int id)
	{
		if (ignore)
			return;
		if (list.length == count)
			list = Arrays.copyOf(list, (int) (count * 1.5));
		list[count++] = id;
	}

	/**
	 * Adds the window ID to the instance
	 *
	 * @param imp
	 *            the image
	 */
	public void add(ImagePlus imp)
	{
		if (imp != null)
			add(imp.getID());
	}

	/**
	 * Adds the window ID to the instance
	 *
	 * @param pw
	 *            the plot window
	 */
	public void add(PlotWindow pw)
	{
		if (pw != null)
			add(pw.getImagePlus());
	}

	/**
	 * Tile all the windows added to this instance
	 */
	public void tile()
	{
		if (count == 0)
			return;
		tileWindows(Arrays.copyOf(list, count));
	}

	/**
	 * Cascade all the windows added to this instance
	 */
	public void cascade()
	{
		if (count == 0)
			return;
		cascadeWindows(Arrays.copyOf(list, count));
	}

	@Override
	public void tileWindows(int[] wList)
	{
		// As of ImageJ 1.50 plot windows must be frozen to allow tiling.
		// This is because they are dynamically resized.
		@SuppressWarnings("unused")
		final boolean[] unfreeze = freezePlotWindows(wList);
		try
		{
			super.tileWindows(wList);
		}
		catch (Throwable t)
		{
			// Some versions of ImageJ / Java do not allow this so call the duplicated function
			copyOfTileWindows(wList);
		}
		finally
		{
			// TODO - Determine how to deal with freeze and unfreeze
			// Since you can unfreeze a plot within the plot window (using the More>> menu) 
			// for now it is left to the user to unfreeze plots for dynamic resizing
			//unfreezePlotWindows(wList, unfreeze);
		}
	}

	/**
	 * Freeze any plot windows to allow them to be tiled
	 * 
	 * @param wList
	 * @return The windows that should be unfrozen
	 */
	private boolean[] freezePlotWindows(int[] wList)
	{
		boolean[] unfreeze = new boolean[wList.length];
		for (int i = 0; i < wList.length; i++)
		{
			ImageWindow win = getWindow(wList[i]);
			if (win == null)
				continue;
			if (win instanceof PlotWindow)
			{
				PlotWindow pw = (PlotWindow) win;
				if (!pw.getPlot().isFrozen())
				{
					unfreeze[i] = true;
					pw.getPlot().setFrozen(true);
				}
			}
		}
		return unfreeze;
	}

	/**
	 * Unfreeze any marked plot windows
	 * 
	 * @param wList
	 * @param unfreeze
	 *            The windows that should be unfrozen
	 */
	@SuppressWarnings("unused")
	private void unfreezePlotWindows(int[] wList, boolean[] unfreeze)
	{
		for (int i = 0; i < wList.length; i++)
		{
			if (!unfreeze[i])
				continue;
			ImageWindow win = getWindow(wList[i]);
			if (win == null)
				continue;
			if (win instanceof PlotWindow)
			{
				PlotWindow pw = (PlotWindow) win;
				pw.getPlot().setFrozen(false);
			}
		}
	}

	@Override
	public void cascadeWindows(int[] wList)
	{
		try
		{
			super.cascadeWindows(wList);
		}
		catch (Throwable t)
		{
			// Some versions of ImageJ / Java do not allow this so call the duplicated function
			copyOfCascadeWindows(wList);
		}
	}

	void copyOfTileWindows(int[] wList)
	{
		Dimension screen = IJ.getScreenSize();
		int minWidth = Integer.MAX_VALUE;
		int minHeight = Integer.MAX_VALUE;
		double totalWidth = 0;
		double totalHeight = 0;
		for (int i = 0; i < wList.length; i++)
		{
			ImageWindow win = getWindow(wList[i]);
			if (win == null)
				continue;
			if (win instanceof PlotWindow && !((PlotWindow) win).getPlot().isFrozen())
			{
				IJ.error("Tile", "Unfrozen plot windows cannot be tiled.");
				return;
			}
			Dimension d = win.getSize();
			int w = d.width;
			int h = d.height + titlebarHeight;
			if (w < minWidth)
				minWidth = w;
			if (h < minHeight)
				minHeight = h;
			totalWidth += w;
			totalHeight += h;
		}
		int nPics = wList.length;
		double averageWidth = totalWidth / nPics;
		double averageHeight = totalHeight / nPics;
		int tileWidth = (int) averageWidth;
		int tileHeight = (int) averageHeight;
		//IJ.write("tileWidth, tileHeight: "+tileWidth+" "+tileHeight);
		int hspace = screen.width - 2 * GAP;
		if (tileWidth > hspace)
			tileWidth = hspace;
		int vspace = screen.height - YSTART;
		if (tileHeight > vspace)
			tileHeight = vspace;
		int hloc, vloc;
		boolean theyFit;
		do
		{
			hloc = XSTART;
			vloc = YSTART;
			theyFit = true;
			int i = 0;
			do
			{
				i++;
				if (hloc + tileWidth > screen.width)
				{
					hloc = XSTART;
					vloc = vloc + tileHeight;
					if (vloc + tileHeight > screen.height)
						theyFit = false;
				}
				hloc = hloc + tileWidth + GAP;
			} while (theyFit && (i < nPics));
			if (!theyFit)
			{
				tileWidth = (int) (tileWidth * 0.98 + 0.5);
				tileHeight = (int) (tileHeight * 0.98 + 0.5);
			}
		} while (!theyFit);
		hloc = XSTART;
		vloc = YSTART;

		for (int i = 0; i < nPics; i++)
		{
			if (hloc + tileWidth > screen.width)
			{
				hloc = XSTART;
				vloc = vloc + tileHeight;
			}
			ImageWindow win = getWindow(wList[i]);
			if (win != null)
			{
				win.setLocation(hloc, vloc);
				//IJ.write(i+" "+w+" "+tileWidth+" "+mag+" "+IJ.d2s(zoomFactor,2)+" "+zoomCount);
				ImageCanvas canvas = win.getCanvas();
				while (win.getSize().width * 0.85 >= tileWidth && canvas.getMagnification() > 0.03125)
					canvas.zoomOut(0, 0);
				win.toFront();
			}
			hloc += tileWidth + GAP;
		}
	}

	ImageWindow getWindow(int id)
	{
		ImageWindow win = null;
		ImagePlus imp = WindowManager.getImage(id);
		if (imp != null)
			win = imp.getWindow();
		return win;
	}

	void copyOfCascadeWindows(int[] wList)
	{
		Dimension screen = IJ.getScreenSize();
		int x = XSTART;
		int y = YSTART;
		int xstep = 0;
		int xstart = XSTART;
		for (int i = 0; i < wList.length; i++)
		{
			ImageWindow win = getWindow(wList[i]);
			if (win == null)
				continue;
			Dimension d = win.getSize();
			if (i == 0)
			{
				xstep = (int) (d.width * 0.8);
				if (xstep > MAXSTEP)
					xstep = MAXSTEP;
			}
			if (y + d.height * 0.67 > screen.height)
			{
				xstart += xstep;
				if (xstart + d.width * 0.67 > screen.width)
					xstart = XSTART + XOFFSET;
				x = xstart;
				y = YSTART;
			}
			win.setLocation(x, y);
			win.toFront();
			x += XOFFSET;
			y += YOFFSET;
		}
	}
}
