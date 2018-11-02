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

package uk.ac.sussex.gdsc.core.ij.plugin;

import gnu.trove.list.array.TIntArrayList;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.gui.PlotWindow;

import java.awt.Dimension;

/**
 * Extend the standard ImageJ window organiser plugin and make the methods public.
 *
 * <p>Adds stateful methods to the instance so it can layout any windows added to it.
 */
public class WindowOrganiser extends ij.plugin.WindowOrganizer {
  private static final int XSTART = 4;
  private static final int YSTART = 80;
  private static final int XOFFSET = 8;
  private static final int YOFFSET = 24;
  private static final int MAXSTEP = 200;
  private static final int GAP = 2;

  /** The titlebar height. */
  private static final int TITLE_BAR_HEIGHT = IJ.isMacintosh() ? 40 : 20;

  /** The list. */
  private TIntArrayList list = new TIntArrayList(10);

  /** Set to true to ignore any added window. */
  private boolean ignore;

  /** Set to true to unfreeze plots after layout. */
  private boolean unfreeze = true;

  /**
   * Adds the window ID to the instance.
   *
   * @param id the window id
   */
  public void add(int id) {
    if (ignore) {
      return;
    }
    list.add(id);
  }

  /**
   * Adds the window ID to the instance.
   *
   * @param imp the image
   */
  public void add(ImagePlus imp) {
    if (imp != null) {
      add(imp.getID());
    }
  }

  /**
   * Adds the window ID to the instance.
   *
   * @param pw the plot window
   */
  public void add(PlotWindow pw) {
    if (pw != null) {
      add(pw.getImagePlus());
    }
  }

  /**
   * Adds the window IDs from the window organiser to the instance.
   *
   * @param windowOrganiser the window organiser
   */
  public void add(WindowOrganiser windowOrganiser) {
    if (windowOrganiser != null) {
      windowOrganiser.list.forEach(id -> {
        add(id);
        return true;
      });
    }
  }

  /**
   * Checks if ignoring any added window.
   *
   * @return true, if ignoring
   */
  public boolean isIgnore() {
    return ignore;
  }

  /**
   * Sets the ignore flag.
   *
   * @param ignore Set to true to ignore any added window.
   */
  public void setIgnore(boolean ignore) {
    this.ignore = ignore;
  }

  /**
   * Checks if unfreezing plots after layout.
   *
   * @return true, if unfreezing
   */
  public boolean isUnfreeze() {
    return unfreeze;
  }

  /**
   * Set to true to unfreeze plots after layout.
   *
   * @param unfreeze the new unfreeze flag
   */
  public void setUnfreeze(boolean unfreeze) {
    this.unfreeze = unfreeze;
  }

  /**
   * The number of windows that have been added.
   *
   * @return the size
   */
  public int size() {
    return list.size();
  }

  /**
   * Checks if no windows have been added.
   *
   * @return true, if is empty
   */
  public boolean isEmpty() {
    return list.isEmpty();
  }

  /**
   * Checks if windows have been added.
   *
   * @return true, if is not empty
   */
  public boolean isNotEmpty() {
    return !isEmpty();
  }

  /**
   * Gets the window IDs.
   *
   * @return the window IDs
   */
  public int[] getWindowIds() {
    return list.toArray();
  }

  /**
   * Tile all the windows added to this instance.
   */
  public void tile() {
    if (list.isEmpty()) {
      return;
    }
    tileWindows(getWindowIds(), isUnfreeze());
  }

  /**
   * Cascade all the windows added to this instance.
   */
  public void cascade() {
    if (list.isEmpty()) {
      return;
    }
    cascadeWindows(getWindowIds());
  }

  /**
   * Tile windows.
   *
   * @param windowList the window list
   */
  public static void tileWindows(int[] windowList) {
    tileWindows(windowList, true);
  }

  /**
   * Tile windows.
   *
   * @param windowList the window list
   * @param isUnfreeze Set to true if unfreezing plots after layout
   */
  public static void tileWindows(int[] windowList, boolean isUnfreeze) {
    // As of ImageJ 1.50 plot windows must be frozen to allow tiling.
    // This is because they are dynamically resized.
    final boolean[] unfreeze = freezePlotWindows(windowList);
    try {
      // This is not visible so call a copy
      // super.tileWindows(windowList)
      copyOfTileWindows(windowList);
    } finally {
      // Since you can unfreeze a plot within the plot window (using the More>> menu)
      // for now it is left to the user to unfreeze plots for dynamic resizing
      if (isUnfreeze) {
        unfreezePlotWindows(windowList, unfreeze);
      }
    }
  }

  /**
   * Freeze any plot windows to allow them to be tiled.
   *
   * @param windowList the window list
   * @return The windows that should be unfrozen
   */
  private static boolean[] freezePlotWindows(int[] windowList) {
    final boolean[] unfreeze = new boolean[windowList.length];
    for (int i = 0; i < windowList.length; i++) {
      final ImageWindow win = getWindow(windowList[i]);
      if (win == null) {
        continue;
      }
      if (win instanceof PlotWindow) {
        final PlotWindow pw = (PlotWindow) win;
        if (!pw.getPlot().isFrozen()) {
          unfreeze[i] = true;
          pw.getPlot().setFrozen(true);
        }
      }
    }
    return unfreeze;
  }

  /**
   * Unfreeze any marked plot windows.
   *
   * @param windowList the window list
   * @param unfreeze The windows that should be unfrozen
   */
  private static void unfreezePlotWindows(int[] windowList, boolean[] unfreeze) {
    for (int i = 0; i < windowList.length; i++) {
      if (!unfreeze[i]) {
        continue;
      }
      final ImageWindow win = getWindow(windowList[i]);
      if (win == null) {
        continue;
      }
      if (win instanceof PlotWindow) {
        final PlotWindow pw = (PlotWindow) win;
        pw.getPlot().setFrozen(false);
      }
    }
  }

  /**
   * Cascade windows.
   *
   * @param windowList the window list
   */
  public static void cascadeWindows(int[] windowList) {
    // This is not visible so call a copy
    // super.cascadeWindows(windowList)
    copyOfCascadeWindows(windowList);
  }

  /**
   * Copy of tile windows.
   *
   * @param windowList the window list
   */
  private static void copyOfTileWindows(int[] windowList) {
    final Dimension screen = IJ.getScreenSize();
    int minWidth = Integer.MAX_VALUE;
    int minHeight = Integer.MAX_VALUE;
    double totalWidth = 0;
    double totalHeight = 0;
    for (int i = 0; i < windowList.length; i++) {
      final ImageWindow win = getWindow(windowList[i]);
      if (win == null) {
        continue;
      }
      if (win instanceof PlotWindow && !((PlotWindow) win).getPlot().isFrozen()) {
        IJ.error("Tile", "Unfrozen plot windows cannot be tiled.");
        return;
      }
      final Dimension d = win.getSize();
      final int w = d.width;
      final int h = d.height + TITLE_BAR_HEIGHT;
      if (w < minWidth) {
        minWidth = w;
      }
      if (h < minHeight) {
        minHeight = h;
      }
      totalWidth += w;
      totalHeight += h;
    }
    final int nPics = windowList.length;
    final double averageWidth = totalWidth / nPics;
    final double averageHeight = totalHeight / nPics;
    int tileWidth = (int) averageWidth;
    int tileHeight = (int) averageHeight;
    final int hspace = screen.width - 2 * GAP;
    if (tileWidth > hspace) {
      tileWidth = hspace;
    }
    final int vspace = screen.height - YSTART;
    if (tileHeight > vspace) {
      tileHeight = vspace;
    }
    int hloc;
    int vloc;
    boolean theyFit;
    do {
      hloc = XSTART;
      vloc = YSTART;
      theyFit = true;
      int index = 0;
      do {
        index++;
        if (hloc + tileWidth > screen.width) {
          hloc = XSTART;
          vloc = vloc + tileHeight;
          if (vloc + tileHeight > screen.height) {
            theyFit = false;
          }
        }
        hloc = hloc + tileWidth + GAP;
      }
      while (theyFit && (index < nPics));
      if (!theyFit) {
        tileWidth = (int) (tileWidth * 0.98 + 0.5);
        tileHeight = (int) (tileHeight * 0.98 + 0.5);
      }
    }
    while (!theyFit);
    hloc = XSTART;
    vloc = YSTART;

    for (int i = 0; i < nPics; i++) {
      if (hloc + tileWidth > screen.width) {
        hloc = XSTART;
        vloc = vloc + tileHeight;
      }
      final ImageWindow win = getWindow(windowList[i]);
      if (win != null) {
        win.setLocation(hloc, vloc);
        final ImageCanvas canvas = win.getCanvas();
        while (win.getSize().width * 0.85 >= tileWidth && canvas.getMagnification() > 0.03125) {
          canvas.zoomOut(0, 0);
        }
        win.toFront();
      }
      hloc += tileWidth + GAP;
    }
  }

  /**
   * Gets the window.
   *
   * @param id the id
   * @return the window
   */
  private static ImageWindow getWindow(int id) {
    ImageWindow win = null;
    final ImagePlus imp = WindowManager.getImage(id);
    if (imp != null) {
      win = imp.getWindow();
    }
    return win;
  }

  /**
   * Copy of cascade windows.
   *
   * @param windowList the window list
   */
  private static void copyOfCascadeWindows(int[] windowList) {
    final Dimension screen = IJ.getScreenSize();
    int xposition = XSTART;
    int yposition = YSTART;
    int xstep = 0;
    int xstart = XSTART;
    for (int i = 0; i < windowList.length; i++) {
      final ImageWindow win = getWindow(windowList[i]);
      if (win == null) {
        continue;
      }
      final Dimension d = win.getSize();
      if (i == 0) {
        xstep = (int) (d.width * 0.8);
        if (xstep > MAXSTEP) {
          xstep = MAXSTEP;
        }
      }
      if (yposition + d.height * 0.67 > screen.height) {
        xstart += xstep;
        if (xstart + d.width * 0.67 > screen.width) {
          xstart = XSTART + XOFFSET;
        }
        xposition = xstart;
        yposition = YSTART;
      }
      win.setLocation(xposition, yposition);
      win.toFront();
      xposition += XOFFSET;
      yposition += YOFFSET;
    }
  }
}
