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

import java.awt.Dimension;
import java.util.Arrays;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.gui.PlotWindow;

/**
 * Extend the standard ImageJ window organiser plugin and make the methods public <p> Adds stateful
 * methods to the instance so it can layout any windows added to it.
 */
public class WindowOrganiser extends ij.plugin.WindowOrganizer {
  private static final int XSTART = 4, YSTART = 80, XOFFSET = 8, YOFFSET = 24, MAXSTEP = 200,
      GAP = 2;

  /** The titlebar height. */
  private final static int titlebarHeight = IJ.isMacintosh() ? 40 : 20;

  /** The list. */
  private int[] list = new int[10];

  /** The count. */
  private int count = 0;

  /** Set to true to ignore any added window. */
  private boolean ignore = false;

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
    if (list.length == count) {
      list = Arrays.copyOf(list, (int) (count * 1.5));
    }
    list[count++] = id;
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
   * Set to true to unfreeze plots after layout
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
    return count;
  }

  /**
   * Tile all the windows added to this instance.
   */
  public void tile() {
    if (count <= 1) {
      return;
    }
    tileWindows(Arrays.copyOf(list, count), isUnfreeze());
  }

  /**
   * Cascade all the windows added to this instance.
   */
  public void cascade() {
    if (count <= 1) {
      return;
    }
    cascadeWindows(Arrays.copyOf(list, count));
  }

  /**
   * Tile windows.
   *
   * @param wList the window list
   */
  public static void tileWindows(int[] wList) {
    tileWindows(wList, true);
  }

  /**
   * Tile windows.
   *
   * @param wList the window list
   * @param isUnfreeze Set to true if unfreezing plots after layout
   */
  public static void tileWindows(int[] wList, boolean isUnfreeze) {
    // As of ImageJ 1.50 plot windows must be frozen to allow tiling.
    // This is because they are dynamically resized.
    final boolean[] unfreeze = freezePlotWindows(wList);
    try {
      // This is not visible so call a copy
      // super.tileWindows(wList);
      copyOfTileWindows(wList);
    } finally {
      // TODO - Determine how to deal with freeze and unfreeze
      // Since you can unfreeze a plot within the plot window (using the More>> menu)
      // for now it is left to the user to unfreeze plots for dynamic resizing
      if (isUnfreeze) {
        unfreezePlotWindows(wList, unfreeze);
      }
    }
  }

  /**
   * Freeze any plot windows to allow them to be tiled.
   *
   * @param wList the w list
   * @return The windows that should be unfrozen
   */
  private static boolean[] freezePlotWindows(int[] wList) {
    final boolean[] unfreeze = new boolean[wList.length];
    for (int i = 0; i < wList.length; i++) {
      final ImageWindow win = getWindow(wList[i]);
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
   * @param wList the w list
   * @param unfreeze The windows that should be unfrozen
   */
  private static void unfreezePlotWindows(int[] wList, boolean[] unfreeze) {
    for (int i = 0; i < wList.length; i++) {
      if (!unfreeze[i]) {
        continue;
      }
      final ImageWindow win = getWindow(wList[i]);
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
   * @param wList the window list
   */
  public static void cascadeWindows(int[] wList) {
    // This is not visible so call a copy
    // super.cascadeWindows(wList);
    copyOfCascadeWindows(wList);
  }

  /**
   * Copy of tile windows.
   *
   * @param wList the w list
   */
  private static void copyOfTileWindows(int[] wList) {
    final Dimension screen = IJ.getScreenSize();
    int minWidth = Integer.MAX_VALUE;
    int minHeight = Integer.MAX_VALUE;
    double totalWidth = 0;
    double totalHeight = 0;
    for (int i = 0; i < wList.length; i++) {
      final ImageWindow win = getWindow(wList[i]);
      if (win == null) {
        continue;
      }
      if (win instanceof PlotWindow && !((PlotWindow) win).getPlot().isFrozen()) {
        IJ.error("Tile", "Unfrozen plot windows cannot be tiled.");
        return;
      }
      final Dimension d = win.getSize();
      final int w = d.width;
      final int h = d.height + titlebarHeight;
      if (w < minWidth) {
        minWidth = w;
      }
      if (h < minHeight) {
        minHeight = h;
      }
      totalWidth += w;
      totalHeight += h;
    }
    final int nPics = wList.length;
    final double averageWidth = totalWidth / nPics;
    final double averageHeight = totalHeight / nPics;
    int tileWidth = (int) averageWidth;
    int tileHeight = (int) averageHeight;
    // IJ.write("tileWidth, tileHeight: "+tileWidth+" "+tileHeight);
    final int hspace = screen.width - 2 * GAP;
    if (tileWidth > hspace) {
      tileWidth = hspace;
    }
    final int vspace = screen.height - YSTART;
    if (tileHeight > vspace) {
      tileHeight = vspace;
    }
    int hloc, vloc;
    boolean theyFit;
    do {
      hloc = XSTART;
      vloc = YSTART;
      theyFit = true;
      int i = 0;
      do {
        i++;
        if (hloc + tileWidth > screen.width) {
          hloc = XSTART;
          vloc = vloc + tileHeight;
          if (vloc + tileHeight > screen.height) {
            theyFit = false;
          }
        }
        hloc = hloc + tileWidth + GAP;
      } while (theyFit && (i < nPics));
      if (!theyFit) {
        tileWidth = (int) (tileWidth * 0.98 + 0.5);
        tileHeight = (int) (tileHeight * 0.98 + 0.5);
      }
    } while (!theyFit);
    hloc = XSTART;
    vloc = YSTART;

    for (int i = 0; i < nPics; i++) {
      if (hloc + tileWidth > screen.width) {
        hloc = XSTART;
        vloc = vloc + tileHeight;
      }
      final ImageWindow win = getWindow(wList[i]);
      if (win != null) {
        win.setLocation(hloc, vloc);
        // IJ.write(i+" "+w+" "+tileWidth+" "+mag+" "+IJ.d2s(zoomFactor,2)+" "+zoomCount);
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
   * @param wList the w list
   */
  private static void copyOfCascadeWindows(int[] wList) {
    final Dimension screen = IJ.getScreenSize();
    int x = XSTART;
    int y = YSTART;
    int xstep = 0;
    int xstart = XSTART;
    for (int i = 0; i < wList.length; i++) {
      final ImageWindow win = getWindow(wList[i]);
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
      if (y + d.height * 0.67 > screen.height) {
        xstart += xstep;
        if (xstart + d.width * 0.67 > screen.width) {
          xstart = XSTART + XOFFSET;
        }
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
