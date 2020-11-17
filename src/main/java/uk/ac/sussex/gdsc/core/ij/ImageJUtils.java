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
 * Copyright (C) 2011 - 2020 Alex Herbert
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

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.Macro;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.gui.Plot;
import ij.gui.PlotWindow;
import ij.io.DirectoryChooser;
import ij.io.OpenDialog;
import ij.plugin.HyperStackReducer;
import ij.plugin.ZProjector;
import ij.plugin.frame.Recorder;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;
import ij.process.ShortProcessor;
import ij.text.TextPanel;
import ij.text.TextWindow;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.LayoutManager;
import java.awt.Panel;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import uk.ac.sussex.gdsc.core.annotation.NotNull;
import uk.ac.sussex.gdsc.core.annotation.Nullable;
import uk.ac.sussex.gdsc.core.data.VisibleForTesting;
import uk.ac.sussex.gdsc.core.ij.plugin.WindowOrganiser;
import uk.ac.sussex.gdsc.core.logging.Ticker;
import uk.ac.sussex.gdsc.core.utils.SimpleArrayUtils;
import uk.ac.sussex.gdsc.core.utils.TextUtils;
import uk.ac.sussex.gdsc.core.utils.concurrent.ConcurrencyUtils;

/**
 * Contains helper functions.
 */
public final class ImageJUtils {
  // Flags for buildImageList

  /** Single plane (2D image). */
  public static final int SINGLE = 1;

  /** Binary image. */
  public static final int BINARY = 2;

  /** Greyscale image (8, 16, 32 bit). */
  public static final int GREY_SCALE = 4;

  /** Greyscale image (8, 16 bit). */
  public static final int GREY_8_16 = 8;

  /** Add no image option. */
  public static final int NO_IMAGE = 16;

  /** The constant for no image title. */
  public static final String NO_IMAGE_TITLE = "[None]";

  /**
   * Flag used to preserve the x min when reusing the plot window. See
   * {@link #display(String, Plot, int)}.
   */
  public static final int PRESERVE_X_MIN = 0x01;
  /**
   * Flag used to preserve the x max when reusing the plot window. See
   * {@link #display(String, Plot, int)}.
   */
  public static final int PRESERVE_X_MAX = 0x02;
  /**
   * Flag used to preserve the y min when reusing the plot window. See
   * {@link #display(String, Plot, int)}.
   */
  public static final int PRESERVE_Y_MIN = 0x04;
  /**
   * Flag used to preserve the y max when reusing the plot window. See
   * {@link #display(String, Plot, int)}.
   */
  public static final int PRESERVE_Y_MAX = 0x08;
  /**
   * Flag used to preserve all limits when reusing the plot window. See
   * {@link #display(String, Plot, int)}.
   */
  public static final int PRESERVE_ALL = 0x0f;
  /**
   * Flag used to not bring the plot to the front when reusing the plot window. See
   * {@link #display(String, Plot, int)}.
   */
  public static final int NO_TO_FRONT = 0x10;

  /** Used to record extra options in the macro recorder. */
  private static final String EXTRA_OPTIONS = "extraoptions";

  /** The interval (in milliseconds) between changes to update the ImageJ status bar. */
  private static final int STATUS_CHANGE_INTERVAL = 150;

  /** The last time to ImageJ status bar was updated. */
  private static long lastTime;

  /**
   * No public construction.
   */
  private ImageJUtils() {}

  /**
   * Splits a full path into the directory and filename. A null input is treated as an empty string
   * {@code ""}.
   *
   * <p>If there is no directory it is set as null.
   *
   * @param path the path
   * @return directory (may be null) and filename (not null)
   */
  public static String[] decodePath(String path) {
    final String[] result = new String[2];
    final String safePath = (path == null) ? "" : path;
    int index = safePath.lastIndexOf('/');
    if (index == -1) {
      index = safePath.lastIndexOf('\\');
    }
    if (index > 0) {
      result[0] = safePath.substring(0, index + 1);
      result[1] = safePath.substring(index + 1);
    } else {
      // This used to return OpenDialog.getDefaultDirectory()
      result[0] = null;
      result[1] = safePath;
    }
    return result;
  }

  /**
   * Show the image. Replace a currently open image with the specified title or else create a new
   * image.
   *
   * @param title the title
   * @param ip the image processor
   * @return the image
   */
  public static ImagePlus display(String title, ImageProcessor ip) {
    return display(title, ip, 0, null);
  }

  /**
   * Show the image. Replace a currently open image with the specified title or else create a new
   * image.
   *
   * @param title the title
   * @param ip the image processor
   * @param flags the flags
   * @return the image
   */
  public static ImagePlus display(String title, ImageProcessor ip, int flags) {
    return display(title, ip, flags, null);
  }

  /**
   * Show the image. Replace a currently open image with the specified title or else create a new
   * image.
   *
   * @param title the title
   * @param ip the image processor
   * @param windowOrganiser the window organiser. New images are added to this.
   * @return the image
   */
  public static ImagePlus display(String title, ImageProcessor ip,
      WindowOrganiser windowOrganiser) {
    return display(title, ip, 0, windowOrganiser);
  }

  /**
   * Show the image. Replace a currently open image with the specified title or else create a new
   * image.
   *
   * @param title the title
   * @param ip the image processor
   * @param flags the flags
   * @param windowOrganiser the window organiser. New images are added to this.
   * @return the image
   */
  public static ImagePlus display(String title, ImageProcessor ip, int flags,
      WindowOrganiser windowOrganiser) {
    ImagePlus imp = WindowManager.getImage(title);
    if (imp == null) {
      imp = new ImagePlus(title, ip);
      imp.show();
      addImage(windowOrganiser, imp);
    } else {
      final boolean resized = imp.getWidth() != ip.getWidth() || imp.getHeight() != ip.getHeight();
      // This works only if it is not a stack
      if (imp.getStackSize() == 1) {
        imp.setProcessor(ip);
      } else {
        final ImageStack stack = new ImageStack(ip.getWidth(), ip.getHeight());
        stack.addSlice(ip);
        imp.setStack(stack);
      }
      if (resized) {
        // Assume overlay is no longer valid
        imp.setOverlay(null);
      }
      if (imp.getWindow().isVisible()) {
        // Already visible
        if ((flags & NO_TO_FRONT) == 0) {
          imp.getWindow().toFront();
        }
      } else {
        imp.getWindow().setVisible(true);
      }
    }
    return imp;
  }

  /**
   * Show the image. Replace a currently open image with the specified title or else create a new
   * image.
   *
   * @param title the title
   * @param slices the slices
   * @return the image
   */
  public static ImagePlus display(String title, ImageStack slices) {
    return display(title, slices, 0, null);
  }

  /**
   * Show the image. Replace a currently open image with the specified title or else create a new
   * image.
   *
   * @param title the title
   * @param slices the slices
   * @param flags the flags
   * @return the image
   */
  public static ImagePlus display(String title, ImageStack slices, int flags) {
    return display(title, slices, flags, null);
  }

  /**
   * Show the image. Replace a currently open image with the specified title or else create a new
   * image.
   *
   * @param title the title
   * @param slices the slices
   * @param windowOrganiser the window organiser. New images are added to this.
   * @return the image
   */
  public static ImagePlus display(String title, ImageStack slices,
      WindowOrganiser windowOrganiser) {
    return display(title, slices, 0, windowOrganiser);
  }

  /**
   * Show the image. Replace a currently open image with the specified title or else create a new
   * image.
   *
   * @param title the title
   * @param slices the slices
   * @param flags the flags
   * @param windowOrganiser the window organiser. New images are added to this.
   * @return the image
   */
  public static ImagePlus display(String title, ImageStack slices, int flags,
      WindowOrganiser windowOrganiser) {
    ImagePlus imp = WindowManager.getImage(title);
    if (imp == null) {
      imp = new ImagePlus(title, slices);
      imp.show();
      addImage(windowOrganiser, imp);
    } else {
      slices.setColorModel(imp.getProcessor().getColorModel());
      final boolean resized =
          imp.getWidth() != slices.getWidth() || imp.getHeight() != slices.getHeight();
      imp.setStack(slices);
      if (resized) {
        // Assume overlay is no longer valid
        imp.setOverlay(null);
      }
      if (imp.getWindow().isVisible()) {
        // Already visible
        if ((flags & NO_TO_FRONT) == 0) {
          imp.getWindow().toFront();
        }
      } else {
        imp.getWindow().setVisible(true);
      }
    }
    return imp;
  }

  /**
   * Show the image. Replace a currently open image with the specified title or else create a new
   * image.
   *
   * @param title the title
   * @param data the data
   * @param width the width
   * @param height the height
   * @return the image
   */
  public static ImagePlus display(String title, double[] data, int width, int height) {
    return display(title, data, width, height, 0, null);
  }

  /**
   * Show the image. Replace a currently open image with the specified title or else create a new
   * image.
   *
   * @param title the title
   * @param data the data
   * @param width the width
   * @param height the height
   * @param flags the flags
   * @param windowOrganiser the window organiser (new windows are added to this)
   * @return the image
   */
  public static ImagePlus display(String title, double[] data, int width, int height, int flags,
      WindowOrganiser windowOrganiser) {
    if (data == null || data.length < width * height) {
      return null;
    }
    final float[] f = new float[width * height];
    for (int i = 0; i < f.length; i++) {
      f[i] = (float) data[i];
    }
    return ImageJUtils.display(title, new FloatProcessor(width, height, f), flags, windowOrganiser);
  }

  /**
   * Show the image. Replace a currently open image with the specified title or else create a new
   * image.
   *
   * @param title the title
   * @param data the data
   * @param width the width
   * @param height the height
   * @return the image
   */
  public static ImagePlus display(String title, double[][] data, int width, int height) {
    return display(title, data, width, height, 0, null);
  }

  /**
   * Show the image. Replace a currently open image with the specified title or else create a new
   * image.
   *
   * @param title the title
   * @param data the data
   * @param width the width
   * @param height the height
   * @param flags the flags
   * @param windowOrganiser the window organiser (new windows are added to this)
   * @return the image
   */
  public static ImagePlus display(String title, double[][] data, int width, int height, int flags,
      WindowOrganiser windowOrganiser) {
    if (data == null || data.length < 1) {
      return null;
    }
    final int n = width * height;
    for (final double[] dataSlice : data) {
      if (dataSlice == null || dataSlice.length < n) {
        return null;
      }
    }
    final ImageStack stack = new ImageStack(width, height, data.length);
    for (int s = 0; s < data.length; s++) {
      final float[] f = new float[n];
      for (int i = 0; i < n; i++) {
        f[i] = (float) data[s][i];
      }
      stack.setPixels(f, s + 1);
    }
    return ImageJUtils.display(title, stack, flags, windowOrganiser);
  }

  /**
   * Show the plot. Replace a currently open plot with the specified title or else create a new plot
   * window.
   *
   * @param title the title
   * @param plot the plot
   * @return the plot window
   */
  public static PlotWindow display(String title, Plot plot) {
    return display(title, plot, 0);
  }

  /**
   * Show the plot. Replace a currently open plot with the specified title or else create a new plot
   * window.
   *
   * @param title the title
   * @param plot the plot
   * @param flags Option flags, e.g. to preserve the current limits of an existing plot
   * @return the plot window
   */
  public static PlotWindow display(String title, Plot plot, int flags) {
    return display(title, plot, flags, null);
  }

  /**
   * Show the plot. Replace a currently open plot with the specified title or else create a new plot
   * window.
   *
   * @param title the title
   * @param plot the plot
   * @param windowOrganiser the window organiser (new windows are added to this)
   * @return the plot window
   */
  public static PlotWindow display(String title, Plot plot, WindowOrganiser windowOrganiser) {
    return display(title, plot, 0, windowOrganiser);
  }

  /**
   * Show the plot. Replace a currently open plot with the specified title or else create a new plot
   * window.
   *
   * @param title the title
   * @param plot the plot
   * @param flags Option flags, e.g. to preserve the current limits of an existing plot
   * @param windowOrganiser the window organiser (new windows are added to this)
   * @return the plot window
   */
  public static PlotWindow display(String title, Plot plot, int flags,
      WindowOrganiser windowOrganiser) {
    Frame plotWindowFrame = null;
    for (final int i : getIdList()) {
      final ImagePlus imp = WindowManager.getImage(i);
      if (imp != null && imp.getWindow() instanceof PlotWindow && imp.getTitle().equals(title)) {
        plotWindowFrame = imp.getWindow();
        break;
      }
    }
    PlotWindow plotWindow = null;
    if (plotWindowFrame == null) {
      plotWindow = plot.show();
      addPlot(windowOrganiser, plotWindow);
    } else {
      // Since the new IJ 1.50 plot functionality to have scalable plots this can sometimes error
      int displayFlags = flags;
      try {
        plotWindow = (PlotWindow) plotWindowFrame;
        final Plot oldPlot = plotWindow.getPlot();
        final Dimension d = oldPlot.getSize();
        final double[] limits = oldPlot.getLimits();
        plot.setSize(d.width, d.height);
        if ((displayFlags & PRESERVE_ALL) == PRESERVE_ALL) {
          // Setting the limits before drawing avoids a double draw
          plot.setLimits(limits[0], limits[1], limits[2], limits[3]);
          displayFlags = 0;
        } else if ((displayFlags & PRESERVE_ALL) != 0) {
          // If only some of the limits are to be preserved then we use the default
          // auto-range using NaN.
          final double[] currentLimits = SimpleArrayUtils.newDoubleArray(limits.length, Double.NaN);
          preserveLimits(plot, displayFlags, limits, currentLimits);
          displayFlags = 0;
        }
        plotWindow.drawPlot(plot);
        preserveLimits(plot, displayFlags, limits);
        if (plotWindowFrame.isVisible()) {
          // Already visible
          if ((displayFlags & NO_TO_FRONT) == 0) {
            plotWindow.toFront();
          }
        } else {
          plotWindowFrame.setVisible(true);
        }
      } catch (final Throwable thrown) {
        // Allow debugging by logging the error
        IJ.handleException(thrown);

        // Get the location and close the error plot window
        Point location = null;
        Dimension dimension = null;
        double[] limits = null;
        if (plotWindow != null) {
          location = plotWindow.getLocation();
          final Plot oldPlot = plotWindow.getPlot();
          dimension = oldPlot.getSize();
          limits = oldPlot.getLimits();
          try {
            plotWindow.close();
          } catch (final Throwable innerThrown) {
            // Ignore
          }
        }

        // Show a new window
        if (dimension != null) {
          plot.setSize(dimension.width, dimension.height);
        }
        plotWindow = plot.show();
        if (location != null) {
          plotWindow.setLocation(location);
        }
        if (limits != null) {
          preserveLimits(plot, displayFlags, limits);
        }
        addPlot(windowOrganiser, plotWindow);
      }
    }
    return plotWindow;
  }


  /**
   * Adds the image to the window organiser.
   *
   * @param windowOrganiser the window organiser
   * @param imp the imp
   */
  @VisibleForTesting
  static void addImage(WindowOrganiser windowOrganiser, ImagePlus imp) {
    if (windowOrganiser != null) {
      windowOrganiser.add(imp);
    }
  }

  /**
   * Adds the plot to the window organiser.
   *
   * @param windowOrganiser the window organiser
   * @param plotWindow the plot window
   */
  @VisibleForTesting
  static void addPlot(WindowOrganiser windowOrganiser, PlotWindow plotWindow) {
    if (windowOrganiser != null) {
      windowOrganiser.add(plotWindow);
    }
  }

  /**
   * Preserve the limits from the input array for all the set flags. Otherwise use the current plot
   * limits.
   *
   * @param plot the plot
   * @param preserveLimits the preserve limits flag
   * @param limits the limits
   */
  @VisibleForTesting
  static void preserveLimits(Plot plot, int preserveLimits, double[] limits) {
    // Note: We must have drawn the plot to get the current limits
    preserveLimits(plot, preserveLimits, limits, plot.getLimits());
  }

  /**
   * Preserve the limits from the input array for all the set flags. Otherwise use the current plot
   * limits.
   *
   * @param plot the plot
   * @param preserveLimits the preserve limits flag
   * @param limits the limits
   * @param currentLimits the current limits
   */
  private static void preserveLimits(Plot plot, int preserveLimits, double[] limits,
      double[] currentLimits) {
    if (preserveLimits != 0) {
      if ((preserveLimits & PRESERVE_X_MIN) == 0) {
        limits[0] = currentLimits[0];
      }
      if ((preserveLimits & PRESERVE_X_MAX) == 0) {
        limits[1] = currentLimits[1];
      }
      if ((preserveLimits & PRESERVE_Y_MIN) == 0) {
        limits[2] = currentLimits[2];
      }
      if ((preserveLimits & PRESERVE_Y_MAX) == 0) {
        limits[3] = currentLimits[3];
      }

      plot.setLimits(limits[0], limits[1], limits[2], limits[3]);
    }
  }

  /**
   * Hide the image window.
   *
   * @param title the title
   * @return True if a window with the title was found
   */
  public static boolean hide(String title) {
    for (final int i : getIdList()) {
      final ImagePlus imp = WindowManager.getImage(i);
      if (imp != null && imp.getTitle().equals(title)) {
        imp.getWindow().setVisible(false);
        return true;
      }
    }
    return false;
  }

  /**
   * Close the named window.
   *
   * @param name the name
   */
  public static void close(String name) {
    final Window w = WindowManager.getWindow(name);
    if (w != null) {
      if (w instanceof ImageWindow) {
        ((ImageWindow) w).close();
      } else if (w instanceof Frame) {
        WindowManager.removeWindow(w);
        w.dispose();
      }
    }
  }

  /**
   * Logs a message to the ImageJ log.
   *
   * @param format the format
   * @param args the args
   */
  public static void log(String format, Object... args) {
    IJ.log(String.format(format, args));
  }

  /**
   * Adds the message to the ImageJ dialog.
   *
   * @param gd the generic dialog
   * @param format the format
   * @param args the args
   */
  public static void addMessage(GenericDialog gd, String format, Object... args) {
    gd.addMessage(String.format(format, args));
  }

  /**
   * Check if the escape key has been pressed. Show a status aborted message if true.
   *
   * @return True if aborted
   */
  public static boolean isInterrupted() {
    if (IJ.escapePressed()) {
      IJ.beep();
      IJ.showStatus("Aborted");
      return true;
    }
    return false;
  }

  /**
   * Show the slow part of the dual progress.
   *
   * <p>The dual progress bar works if the progress is negative and between 0 exclusive and -1
   * exclusive. So the progress is set using:
   *
   * <pre>
   * -(currentIndex + 0.5) / finalIndex
   * </pre>
   *
   * <p>The progress will show a dual progress as long as currentIndex is below finalIndex, e.g. in
   * a loop:
   *
   * <pre>
   * <code>
   * int size = 10;
   * for (int i = 0; i &lt; size; i++) {
   *   showSlowProgress(i, size);
   *   // .. do something that updates the progress bar from 0 to 1.
   *   // This will be shown as the fast progress.
   * }
   * </code>
   * </pre>
   *
   * <p>The dual progress bar can be cleared using {@link IJ#showProgress(double)} with an argument
   * of -1.
   *
   * @param currentIndex the current index
   * @param finalIndex the final index
   */
  public static void showSlowProgress(int currentIndex, int finalIndex) {
    IJ.showProgress(-(currentIndex + 0.5) / finalIndex);
  }

  /**
   * Show the slow part of the dual progress.
   *
   * <p>The dual progress bar works if the progress is negative and between 0 exclusive and -1
   * exclusive. So the progress is set using:
   *
   * <pre>
   * -(currentIndex + 0.5) / finalIndex
   * </pre>
   *
   * <p>The progress will show a dual progress as long as currentIndex is below finalIndex, e.g. in
   * a loop:
   *
   * <pre>
   * <code>
   * int size = 10;
   * for (int i = 0; i &lt; size; i++) {
   *   showSlowProgress(i, size);
   *   // .. do something that updates the progress bar from 0 to 1.
   *   // This will be shown as the fast progress.
   * }
   * </code>
   * </pre>
   *
   * <p>The dual progress bar can be cleared using {@link IJ#showProgress(double)} with an argument
   * of -1.
   *
   * @param currentIndex the current index
   * @param finalIndex the final index
   */
  public static void showSlowProgress(long currentIndex, long finalIndex) {
    IJ.showProgress(-(currentIndex + 0.5) / finalIndex);
  }

  /**
   * Clear the progress bar including the slow part of the dual progress.
   *
   * <p>The dual progress bar can be cleared using {@link IJ#showProgress(double)} with an argument
   * of -1.
   */
  public static void clearSlowProgress() {
    IJ.showProgress(-1);
  }

  /**
   * Check if the current window has the given headings, refreshing the headings if necessary. Only
   * works if the window is showing.
   *
   * @param textWindow the text window
   * @param headings the headings
   * @param preserve Preserve the current data (note that is may not match the new headings)
   * @return True if the window headings were changed
   */
  public static boolean refreshHeadings(TextWindow textWindow, String headings, boolean preserve) {
    if (isShowing(textWindow) && !textWindow.getTextPanel().getColumnHeadings().equals(headings)) {
      final TextPanel tp = textWindow.getTextPanel();
      String text = null;
      if (preserve) {
        tp.setColumnHeadings("");
        text = tp.getText();
      }

      tp.setColumnHeadings(headings);

      if (preserve) {
        tp.append(text);
      }

      return true;
    }
    return false;
  }

  /**
   * Checks if the window is showing.
   *
   * @param window the window (can be null)
   * @return true if the window is showing
   * @see Window#isShowing()
   */
  public static boolean isShowing(@Nullable Window window) {
    if (window != null) {
      return window.isShowing();
    }
    return false;
  }

  /**
   * Refresh the text window within the provided reference. If the window is showing then it is
   * returned. Otherwise a new window is created using the supplier, stored in the reference and
   * returned.
   *
   * <p>A window listener is added to any newly created window to remove itself from the reference
   * when the window closes. This releases memory.
   *
   * @param reference the reference
   * @param supplier the supplier
   * @return the text window
   */
  @NotNull
  public static <T extends TextWindow> T refresh(AtomicReference<T> reference,
      Supplier<T> supplier) {
    final Supplier<T> wrappedSuppler = () -> {
      final T window = supplier.get();
      // When it closes remove the reference to this window
      window.addWindowListener(new WindowAdapter() {
        @Override
        public void windowClosed(WindowEvent event) {
          reference.compareAndSet(window, null);
          super.windowClosed(event);
        }
      });
      return window;
    };
    return ConcurrencyUtils.refresh(reference, TextWindow::isShowing, wrappedSuppler);
  }

  /**
   * Open a directory selection dialog using the given title (and optionally the default directory).
   *
   * @param title The dialog title
   * @param directory The default directory to start in
   * @return The directory (or null if the dialog is cancelled)
   */
  public static String getDirectory(String title, String directory) {
    final String defaultDir = OpenDialog.getDefaultDirectory();
    if (!TextUtils.isNullOrEmpty(directory)) {
      OpenDialog.setDefaultDirectory(directory);
    }
    final DirectoryChooser chooser = new DirectoryChooser(title);
    final String chosenDirectory = chooser.getDirectory();
    if (!TextUtils.isNullOrEmpty(defaultDir)) {
      OpenDialog.setDefaultDirectory(defaultDir);
    }
    return chosenDirectory;
  }

  /**
   * Open a file selection dialog using the given title (and optionally the default path).
   *
   * @param title The dialog title
   * @param filename The default path to start with
   * @return The path (or null if the dialog is cancelled)
   */
  public static String getFilename(String title, String filename) {
    final String[] path = ImageJUtils.decodePath(filename);
    final OpenDialog chooser = new OpenDialog(title, path[0], path[1]);
    if (chooser.getFileName() != null) {
      return chooser.getDirectory() + chooser.getFileName();
    }
    return null;
  }

  /**
   * Determine if the plugin is running with extra options. Checks for the ImageJ shift or alt key
   * down properties. If running in a macro then searches the options string for the 'extraoptions'
   * flag.
   *
   * <p>If the extra options are required then adds the 'extraoptions' flag to the macro recorder
   * options.
   *
   * @return True if extra options are required
   */
  public static boolean isExtraOptions() {
    boolean extraOptions = IJ.altKeyDown() || IJ.shiftKeyDown();
    if (!extraOptions && IJ.isMacro()) {
      extraOptions = StringUtils.contains(Macro.getOptions(), EXTRA_OPTIONS);
    }
    if (extraOptions) {
      Recorder.recordOption(EXTRA_OPTIONS);
    }
    return extraOptions;
  }

  /**
   * Show a message on the status bar if enough time has passed since the last call.
   *
   * @param message The message
   * @return True if shown
   */
  public static boolean showStatus(String message) {
    if (statusExpired()) {
      IJ.showStatus(message);
      return true;
    }
    return false;
  }

  /**
   * Show a message on the status bar if enough time has passed since the last call.
   *
   * @param message The message
   * @return True if shown
   */
  public static boolean showStatus(Supplier<String> message) {
    if (statusExpired()) {
      IJ.showStatus(message.get());
      return true;
    }
    return false;
  }

  /**
   * Return true when a set interval has passed since the ImageJ status was changed.
   *
   * @return true if the status interval has elapsed
   */
  private static boolean statusExpired() {
    final long time = System.currentTimeMillis();
    if (time - lastTime > STATUS_CHANGE_INTERVAL) {
      lastTime = time;
      return true;
    }
    return false;
  }

  /**
   * Set the current source rectangle to centre the view on the given coordinates.
   *
   * <p>Adapted from ij.gui.ImageCanvas.adjustSourceRect(double newMag, int x, int y).
   *
   * @param imp The image
   * @param newMagnification The new magnification (set to zero to use the current magnification)
   * @param x The x coordinate
   * @param y The y coordinate
   */
  public static void adjustSourceRect(ImagePlus imp, double newMagnification, int x, int y) {
    final ImageCanvas ic = imp.getCanvas();
    if (ic == null) {
      return;
    }
    final Dimension dimension = ic.getPreferredSize();
    final int canvasWidth = dimension.width;
    final int canvasHeight = dimension.height;
    final int imageWidth = imp.getWidth();
    final int imageHeight = imp.getHeight();
    final double magnification = (newMagnification <= 0) ? ic.getMagnification() : newMagnification;
    int width = (int) Math.round(canvasWidth / magnification);
    if (width * magnification < canvasWidth) {
      width++;
    }
    int height = (int) Math.round(canvasHeight / magnification);
    if (height * magnification < canvasHeight) {
      height++;
    }
    final Rectangle rectangle = new Rectangle(x - width / 2, y - height / 2, width, height);
    if (rectangle.x < 0) {
      rectangle.x = 0;
    }
    if (rectangle.y < 0) {
      rectangle.y = 0;
    }
    if (rectangle.x + width > imageWidth) {
      rectangle.x = imageWidth - width;
    }
    if (rectangle.y + height > imageHeight) {
      rectangle.y = imageHeight - height;
    }
    ic.setSourceRect(rectangle);
    ic.setMagnification(magnification);
    ic.repaint();
  }

  /**
   * Returns a list of the IDs of open images. Returns an empty array if no windows are open.
   *
   * @return List of IDs
   * @see ij.WindowManager#getIDList()
   */
  public static @NotNull int[] getIdList() {
    final int[] list = WindowManager.getIDList();
    return (list == null) ? ArrayUtils.EMPTY_INT_ARRAY : list;
  }

  /**
   * Build a list of all the image names.
   *
   * @param flags Specify the types of image to collate
   * @return The list of images
   */
  public static @NotNull String[] getImageList(final int flags) {
    return getImageList(flags, null);
  }

  /**
   * Build a list of all the image names.
   *
   * @param flags Specify the types of image to collate
   * @param ignoreSuffix A list of title suffixes to ignore
   * @return The list of images
   */
  public static @NotNull String[] getImageList(final int flags, String[] ignoreSuffix) {
    final ArrayList<String> newImageList = new ArrayList<>();

    if ((flags & NO_IMAGE) == NO_IMAGE) {
      newImageList.add(NO_IMAGE_TITLE);
    }

    for (final int id : getIdList()) {
      final ImagePlus imp = WindowManager.getImage(id);
      if ((imp == null)
          // Single image
          || ((flags & SINGLE) == SINGLE && imp.getNDimensions() > 2)
          // Binary image
          || ((flags & BINARY) == BINARY && !imp.getProcessor().isBinary())
          // Greyscale image
          || ((flags & GREY_SCALE) == GREY_SCALE && imp.getBitDepth() == 24)
          // 8/16-bit only
          || ((flags & GREY_8_16) == GREY_8_16
              && (imp.getBitDepth() != 8 && imp.getBitDepth() != 16))
          // Ignore image suffix
          || (ignoreImage(ignoreSuffix, imp.getTitle()))) {
        continue;
      }

      newImageList.add(imp.getTitle());
    }

    return newImageList.toArray(new String[0]);
  }

  /**
   * Return true if the image title ends with any of the specified suffixes.
   *
   * @param ignoreSuffix A list of title suffixes to ignore
   * @param title The image title
   * @return true if the image title ends with any of the specified suffixes
   */
  public static boolean ignoreImage(String[] ignoreSuffix, String title) {
    if (ignoreSuffix != null) {
      for (final String suffix : ignoreSuffix) {
        if (title.endsWith(suffix)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Return the interval for reporting progress to the ImageJ progress bar given the total number of
   * steps. Code should use the following prototype: <br>
   *
   * <pre>
   * final int interval = ImageJUtils.getProgressInterval(total);
   * for (int i = 0; i &lt; total; i++) {
   *   if (i % interval == 0) {
   *     IJ.showProgress(i, total);
   *   }
   * }
   * </pre>
   *
   * @param total the total number of steps
   * @return The interval
   */
  public static int getProgressInterval(int total) {
    return (total > 200) ? total / 100 : 1;
  }

  /**
   * Return the interval for reporting progress to the ImageJ progress bar given the total number of
   * steps.
   *
   * @param total the total number of steps
   * @return The interval
   */
  public static long getProgressInterval(long total) {
    return (total > 200L) ? total / 100L : 1L;
  }

  /**
   * Extracts a single tile image processor from a hyperstack using the given projection method from
   * the ZProjector.
   *
   * <p>The SUM and AV methods returns a 32-bit (FloatProcessor) image. Other methods use the same
   * pixel type as the input image.
   *
   * @param imp Image hyperstack
   * @param frame The frame to extract
   * @param channel The channel to extract
   * @param projectionMethod the projection method
   * @return A new image processor
   * @see ij.plugin.ZProjector
   */
  public static ImageProcessor extractTile(ImagePlus imp, int frame, int channel,
      int projectionMethod) {
    final int c = imp.getChannel();
    final int s = imp.getSlice();
    final int f = imp.getFrame();

    imp.setPositionWithoutUpdate(channel, 1, frame);

    // Extract the timepoint/channel z-stack
    final HyperStackReducer reducer = new HyperStackReducer(imp);
    final int slices = imp.getNSlices();
    final ImagePlus imp1 = imp.createHyperStack("", 1, slices, 1, imp.getBitDepth());
    reducer.reduce(imp1);

    // Perform projectionMethod
    final ZProjector projector = new ZProjector(imp1);
    projector.setMethod(projectionMethod);
    projector.doProjection();

    imp.setPositionWithoutUpdate(c, s, f);

    return projector.getProjection().getProcessor();
  }

  /**
   * True if the generic dialog will be shown. This will return false if headless or if there are
   * macro options for the dialog (i.e. a macro is running and the dialog will not present to the
   * user)
   *
   * @return true, if the GenericDialog can and will be shown
   */
  public static boolean isShowGenericDialog() {
    if (java.awt.GraphicsEnvironment.isHeadless()) {
      return false;
    }
    return Macro.getOptions() == null;
  }

  /**
   * True if a macro is running and the generic dialog will not present to the user.
   *
   * @return true, if a macro is running
   */
  public static boolean isMacro() {
    return Macro.getOptions() != null;
  }

  /**
   * Rearrange the columns in the dialog layout so that each column has the configured number of
   * rows.
   *
   * <p>Assumes the generic dialog has been constructed normally/ and consists of a layout with
   * pairs of items in ascending y in the grid layout. The components are extracted put into a panel
   * for each column. The panels are then added to the dialog.
   *
   * @param gd the dialog
   * @param rowsPerColumn the rows per column, for each column in order
   */
  @SuppressWarnings("null")
  public static void rearrangeColumns(GenericDialog gd, int... rowsPerColumn) {
    if (!isShowGenericDialog()) {
      return;
    }
    if (rowsPerColumn.length < 1) {
      return;
    }

    final LayoutManager manager = gd.getLayout();
    if (manager instanceof GridBagLayout) {
      final GridBagLayout grid = (GridBagLayout) gd.getLayout();

      // We assume the generic dialog has been constructed normally
      // and consists of a layout with pairs of items in ascending y
      // in the grid layout. We will extract all of these and put them
      // into columns on a panel. The panels can then be added to the dialog.
      final ArrayList<Panel> panels = new ArrayList<>();

      Panel current = null;
      GridBagLayout currentGrid = null;

      int counter = -1;
      int nextColumnY = 0;
      int offsetY = 0;

      for (final Component comp : gd.getComponents()) {
        final GridBagConstraints c = grid.getConstraints(comp);

        // Check if this should be a new column
        if (c.gridy >= nextColumnY || current == null) {
          current = new Panel();
          currentGrid = new GridBagLayout();
          current.setLayout(currentGrid);
          panels.add(current);

          // Used to reset the y to the top of the column
          offsetY = nextColumnY;

          counter++;
          if (counter < rowsPerColumn.length) {
            nextColumnY += rowsPerColumn[counter];
          } else {
            nextColumnY = Integer.MAX_VALUE;
          }
        }

        // Reposition in the current column
        c.gridy = c.gridy - offsetY;
        // c.insets.left = c.insets.left + 10 * xOffset
        c.insets.top = 0;
        c.insets.bottom = 0;

        // Setting constraints separately clones the constraints
        current.add(comp);
        currentGrid.setConstraints(comp, c);
      }

      // Replace the components with columns
      gd.removeAll();

      final GridBagConstraints c = new GridBagConstraints();
      c.gridy = 0;
      c.anchor = GridBagConstraints.NORTH;
      for (int i = 0; i < panels.size(); i++) {
        final Panel p = panels.get(i);
        c.gridx = i;
        gd.add(p);
        grid.setConstraints(p, c);
        // For the next columns
        c.insets.left = 10;
      }

      if (IJ.isLinux()) {
        gd.setBackground(new Color(238, 238, 238));
      }
    }
  }

  /**
   * Gets the column from the text panel.
   *
   * @param tp the tp
   * @param heading the heading
   * @return the column
   */
  public static int getColumn(TextPanel tp, String heading) {
    if (tp != null) {
      final String[] headings = tp.getColumnHeadings().split("\t");
      for (int i = 0; i < headings.length; i++) {
        if (headings[i].equals(heading)) {
          return i;
        }
      }
    }
    return -1;
  }

  /**
   * Gets the bit depth.
   *
   * @param pixels the pixels
   * @return the bit depth
   * @throws IllegalArgumentException If the pixels array is an unrecognised type
   */
  public static int getBitDepth(Object pixels) {
    if (pixels instanceof float[]) {
      return 32;
    }
    if (pixels instanceof short[]) {
      return 16;
    }
    if (pixels instanceof byte[]) {
      return 8;
    }
    if (pixels instanceof int[]) {
      return 24;
    }
    throw new IllegalArgumentException("Unrecognised pixels array");
  }

  /**
   * Creates the processor.
   *
   * @param width the width
   * @param height the height
   * @param pixels the pixels
   * @return the image processor
   * @throws IllegalArgumentException If the pixesl array is an unrecognised type
   */
  public static ImageProcessor createProcessor(int width, int height, Object pixels) {
    if (pixels instanceof float[]) {
      return new FloatProcessor(width, height, (float[]) pixels);
    }
    if (pixels instanceof short[]) {
      return new ShortProcessor(width, height, (short[]) pixels, null);
    }
    if (pixels instanceof byte[]) {
      return new ByteProcessor(width, height, (byte[]) pixels);
    }
    if (pixels instanceof int[]) {
      return new ColorProcessor(width, height, (int[]) pixels);
    }
    throw new IllegalArgumentException("Unrecognised pixels array");
  }

  /**
   * Get the min/max display range from the image's current {@link ImageProcessor} using the ImageJ
   * Auto adjust method (copied from {@link ij.plugin.frame.ContrastAdjuster }).
   *
   * <p>Although the ContrastAdjuster records its actions as 'run("Enhance Contrast",
   * "saturated=0.35");' it actually does something else which makes the image easier to see than
   * the afore mentioned command.
   *
   * @param imp the image
   * @param update Set to true to update the image display range
   * @return [min,max]
   */
  public static double[] autoAdjust(ImagePlus imp, boolean update) {
    final ImageStatistics stats = imp.getRawStatistics(); // get uncalibrated stats
    final int limit = stats.pixelCount / 10;
    final int[] histogram = stats.histogram;
    final int threshold = stats.pixelCount / 5000;

    int index = -1;
    boolean found = false;
    int count;
    do {
      index++;
      count = histogram[index];
      if (count > limit) {
        count = 0;
      }
      found = count > threshold;
    } while (!found && index < 255);

    final int hmin = index;
    index = 256;
    do {
      index--;
      count = histogram[index];
      if (count > limit) {
        count = 0;
      }
      found = count > threshold;
    } while (!found && index > 0);

    final int hmax = index;
    double min;
    double max;
    if (hmax > hmin) {
      min = stats.histMin + hmin * stats.binSize;
      max = stats.histMin + hmax * stats.binSize;
    } else {
      min = stats.min;
      max = stats.max;
    }
    if (update) {
      imp.setDisplayRange(min, max);
    }
    return new double[] {min, max};
  }


  /**
   * Creates a started ticker for the total number of steps. The ticker will report to the ImageJ
   * progress and status bar.
   *
   * <p>The number of threads is required to determine thread-safety of the ticker. Set to zero for
   * non thread-safe.
   *
   * @param total the total
   * @param numberOfThreads the number of threads
   * @return the ticker
   */
  public static Ticker createTicker(long total, int numberOfThreads) {
    return createTicker(total, numberOfThreads, null);
  }

  /**
   * Creates a started ticker for the total number of steps. The ticker will report to the ImageJ
   * progress and status bar. If the status message is not null then the ImageJ status bar will be
   * updated with the message.
   *
   * <p>The number of threads is required to determine thread-safety of the ticker. Set to zero for
   * non thread-safe.
   *
   * @param total the total
   * @param numberOfThreads the number of threads
   * @param statusMessage the status message
   * @return the ticker
   */
  public static Ticker createTicker(long total, int numberOfThreads, String statusMessage) {
    if (statusMessage != null) {
      IJ.showStatus(statusMessage);
    }
    return Ticker.createStarted(SimpleImageJTrackProgress.getInstance(), total,
        numberOfThreads > 1);
  }

  /**
   * Set the progress to complete and the status message to empty.
   */
  public static void finished() {
    finished("");
  }

  /**
   * Set the progress to complete and the show the provided status message.
   *
   * @param statusMessage the status message
   */
  public static void finished(String statusMessage) {
    IJ.showStatus(statusMessage);
    IJ.showProgress(1.0);
  }
}
