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
package gdsc.core.ij;

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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.swing.JLabel;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.util.FastMath;

import gdsc.core.utils.DoubleData;
import gdsc.core.utils.Maths;
import gdsc.core.utils.Statistics;
import gdsc.core.utils.StoredDataStatistics;
import gdsc.core.utils.TextUtils;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.Macro;
import ij.Prefs;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.gui.Plot;
import ij.gui.Plot2;
import ij.gui.PlotWindow;
import ij.gui.ProgressBar;
import ij.io.DirectoryChooser;
import ij.io.OpenDialog;
import ij.plugin.HyperStackReducer;
import ij.plugin.WindowOrganiser;
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

/**
 * Contains helper functions.
 */
public class Utils
{
	// Flags for buildImageList

	/** Single plane (2D image). */
	public final static int SINGLE = 1;

	/** Binary image. */
	public final static int BINARY = 2;

	/** Greyscale image (8, 16, 32 bit). */
	public final static int GREY_SCALE = 4;

	/** Greyscale image (8, 16 bit). */
	public final static int GREY_8_16 = 8;

	/** Add no image option. */
	public final static int NO_IMAGE = 16;

	/** The constant for no image title. */
	public final static String NO_IMAGE_TITLE = "[None]";

	private static boolean newWindow = false;

	/**
	 * Splits a full path into the directory and filename.
	 *
	 * @param path
	 *            the path
	 * @return directory and filename
	 */
	public static String[] decodePath(String path)
	{
		final String[] result = new String[2];
		if (path == null)
			path = "";
		int i = path.lastIndexOf('/');
		if (i == -1)
			i = path.lastIndexOf('\\');
		if (i > 0)
		{
			result[0] = path.substring(0, i + 1);
			result[1] = path.substring(i + 1);
		}
		else
		{
			result[0] = OpenDialog.getDefaultDirectory();
			result[1] = path;
		}
		return result;
	}

	/**
	 * Round the double to the specified significant digits.
	 *
	 * @param d
	 *            the d
	 * @param significantDigits
	 *            the significant digits
	 * @return the string
	 */
	public static String rounded(double d, int significantDigits)
	{
		return Maths.rounded(d, significantDigits);
	}

	/**
	 * Round the double to 4 significant digits.
	 *
	 * @param d
	 *            the d
	 * @return the string
	 */
	public static String rounded(double d)
	{
		return rounded(d, 4);
	}

	/**
	 * Show the image. Replace a currently open image with the specified title or else create a new image.
	 *
	 * @param title
	 *            the title
	 * @param ip
	 *            the image processor
	 * @return the
	 */
	public static ImagePlus display(String title, ImageProcessor ip)
	{
		return display(title, ip, 0);
	}

	/**
	 * Show the image. Replace a currently open image with the specified title or else create a new image.
	 *
	 * @param title
	 *            the title
	 * @param ip
	 *            the image processor
	 * @param flags
	 *            the flags
	 * @return the
	 */
	public static ImagePlus display(String title, ImageProcessor ip, int flags)
	{
		return display(title, ip, flags, null);
	}

	/**
	 * Show the image. Replace a currently open image with the specified title or else create a new image.
	 *
	 * @param title
	 *            the title
	 * @param ip
	 *            the image processor
	 * @param flags
	 *            the flags
	 * @param windowOrganiser
	 *            the window organiser. New images are added to this.
	 * @return the
	 */
	public static ImagePlus display(String title, ImageProcessor ip, int flags, WindowOrganiser windowOrganiser)
	{
		newWindow = false;
		ImagePlus imp = WindowManager.getImage(title);
		if (imp == null)
		{
			imp = new ImagePlus(title, ip);
			imp.show();
			newWindow = true;
			if (windowOrganiser != null)
				windowOrganiser.add(imp);
		}
		else
		{
			final boolean resized = imp.getWidth() != ip.getWidth() || imp.getHeight() != ip.getHeight();
			// This works only if it is not a stack
			if (imp.getStackSize() == 1)
				imp.setProcessor(ip);
			else
			{
				final ImageStack stack = new ImageStack(ip.getWidth(), ip.getHeight());
				stack.addSlice(ip);
				imp.setStack(stack);
			}
			if (resized)
				// Assume overlay is no longer valid
				imp.setOverlay(null);
			if (!imp.getWindow().isVisible())
				imp.getWindow().setVisible(true);
			else if ((flags & NO_TO_FRONT) == 0)
				imp.getWindow().toFront();
		}
		return imp;
	}

	/**
	 * Show the image. Replace a currently open image with the specified title or else create a new image.
	 *
	 * @param title
	 *            the title
	 * @param slices
	 *            the slices
	 * @return the image
	 */
	public static ImagePlus display(String title, ImageStack slices)
	{
		return display(title, slices, 0);
	}

	/**
	 * Show the image. Replace a currently open image with the specified title or else create a new image.
	 *
	 * @param title
	 *            the title
	 * @param slices
	 *            the slices
	 * @param flags
	 *            the flags
	 *            the window organiser. New images are added to this.
	 * @return the image
	 */
	public static ImagePlus display(String title, ImageStack slices, int flags)
	{
		return display(title, slices, flags, null);
	}

	/**
	 * Show the image. Replace a currently open image with the specified title or else create a new image.
	 *
	 * @param title
	 *            the title
	 * @param slices
	 *            the slices
	 * @param flags
	 *            the flags
	 *            the window organiser. New images are added to this.
	 * @param windowOrganiser
	 *            the window organiser
	 * @return the image
	 */
	public static ImagePlus display(String title, ImageStack slices, int flags, WindowOrganiser windowOrganiser)
	{
		newWindow = false;
		ImagePlus imp = WindowManager.getImage(title);
		if (imp == null)
		{
			imp = new ImagePlus(title, slices);
			imp.show();
			newWindow = true;
			if (windowOrganiser != null)
				windowOrganiser.add(imp);
		}
		else
		{
			slices.setColorModel(imp.getProcessor().getColorModel());
			final boolean resized = imp.getWidth() != slices.getWidth() || imp.getHeight() != slices.getHeight();
			imp.setStack(slices);
			if (resized)
				// Assume overlay is no longer valid
				imp.setOverlay(null);
			if (!imp.getWindow().isVisible())
				imp.getWindow().setVisible(true);
			else if ((flags & NO_TO_FRONT) == 0)
				imp.getWindow().toFront();
		}
		return imp;
	}

	/**
	 * Show the image. Replace a currently open image with the specified title or else create a new image.
	 *
	 * @param title
	 *            the title
	 * @param data
	 *            the data
	 * @param w
	 *            the width
	 * @param h
	 *            the height
	 * @return the image
	 */
	public static ImagePlus display(String title, double[] data, int w, int h)
	{
		return display(title, data, w, h, 0);
	}

	/**
	 * Show the image. Replace a currently open image with the specified title or else create a new image.
	 *
	 * @param title
	 *            the title
	 * @param data
	 *            the data
	 * @param w
	 *            the width
	 * @param h
	 *            the height
	 * @param flags
	 *            the flags
	 * @return the image
	 */
	public static ImagePlus display(String title, double[] data, int w, int h, int flags)
	{
		if (data == null || data.length < w * h)
			return null;
		final float[] f = new float[w * h];
		for (int i = 0; i < f.length; i++)
			f[i] = (float) data[i];
		return Utils.display(title, new FloatProcessor(w, h, f), flags);
	}

	/**
	 * Show the image. Replace a currently open image with the specified title or else create a new image.
	 *
	 * @param title
	 *            the title
	 * @param data
	 *            the data
	 * @param w
	 *            the width
	 * @param h
	 *            the height
	 * @return the image
	 */
	public static ImagePlus display(String title, double[][] data, int w, int h)
	{
		return display(title, data, w, h, 0);
	}

	/**
	 * Show the image. Replace a currently open image with the specified title or else create a new image.
	 *
	 * @param title
	 *            the title
	 * @param data
	 *            the data
	 * @param w
	 *            the width
	 * @param h
	 *            the height
	 * @param flags
	 *            the flags
	 * @return the image
	 */
	public static ImagePlus display(String title, double[][] data, int w, int h, int flags)
	{
		if (data == null || data.length < 1)
			return null;
		final int n = w * h;
		for (int s = 0; s < data.length; s++)
			if (data[s] == null || data[s].length < n)
				return null;
		final ImageStack stack = new ImageStack(w, h, data.length);
		for (int s = 0; s < data.length; s++)
		{
			final float[] f = new float[n];
			for (int i = 0; i < n; i++)
				f[i] = (float) data[s][i];
			stack.setPixels(f, s + 1);
		}
		return Utils.display(title, stack, flags);
	}

	/**
	 * Show the plot. Replace a currently open plot with the specified title or else create a new plot window.
	 *
	 * @param title
	 *            the title
	 * @param plot
	 *            the plot
	 * @return the plot window
	 */
	public static PlotWindow display(String title, Plot plot)
	{
		return display(title, plot, 0);
	}

	/**
	 * Show the plot. Replace a currently open plot with the specified title or else create a new plot window.
	 *
	 * @param title
	 *            the title
	 * @param plot
	 *            the plot
	 * @param flags
	 *            Option flags, e.g. to preserve the current limits of an existing plot
	 * @return the plot window
	 */
	public static PlotWindow display(String title, Plot plot, int flags)
	{
		return display(title, plot, flags, null);
	}

	/**
	 * Flag used to preserve the x min when reusing the plot window.
	 * See {@link #display(String, Plot, int)}.
	 */
	public static final int PRESERVE_X_MIN = 0x01;
	/**
	 * Flag used to preserve the x max when reusing the plot window.
	 * See {@link #display(String, Plot, int)}.
	 */
	public static final int PRESERVE_X_MAX = 0x02;
	/**
	 * Flag used to preserve the y min when reusing the plot window.
	 * See {@link #display(String, Plot, int)}.
	 */
	public static final int PRESERVE_Y_MIN = 0x04;
	/**
	 * Flag used to preserve the y max when reusing the plot window.
	 * See {@link #display(String, Plot, int)}.
	 */
	public static final int PRESERVE_Y_MAX = 0x08;
	/**
	 * Flag used to preserve all limits when reusing the plot window.
	 * See {@link #display(String, Plot, int)}.
	 */
	public static final int PRESERVE_ALL = 0x0f;
	/**
	 * Flag used to not bring the plot to the front when reusing the plot window. See
	 * {@link #display(String, Plot, int)}.
	 */
	public static final int NO_TO_FRONT = 0x10;

	/**
	 * Show the plot. Replace a currently open plot with the specified title or else create a new plot window.
	 *
	 * @param title
	 *            the title
	 * @param plot
	 *            the plot
	 * @param flags
	 *            Option flags, e.g. to preserve the current limits of an existing plot
	 * @param windowOrganiser
	 *            the window organiser. New plots are added to this.
	 * @return the plot window
	 */
	public static PlotWindow display(String title, Plot plot, int flags, WindowOrganiser windowOrganiser)
	{
		newWindow = false;
		Frame plotWindow = null;
		final int[] wList = WindowManager.getIDList();
		final int len = wList != null ? wList.length : 0;
		for (int i = 0; i < len; i++)
		{
			final ImagePlus imp = WindowManager.getImage(wList[i]);
			if (imp != null && imp.getWindow() instanceof PlotWindow)
				if (imp.getTitle().equals(title))
				{
					plotWindow = imp.getWindow();
					break;
				}
		}
		PlotWindow p = null;
		if (plotWindow == null)
		{
			p = plot.show();
			if (windowOrganiser != null)
				windowOrganiser.add(p);
			newWindow = true;
		}
		else
			// Since the new IJ 1.50 plot functionality to have scalable plots this can sometimes error
			try
			{
				p = (PlotWindow) plotWindow;
				final Plot oldPlot = p.getPlot();
				final Dimension d = oldPlot.getSize();
				final double[] limits = oldPlot.getLimits();
				plot.setSize(d.width, d.height);
				if ((flags & PRESERVE_ALL) == PRESERVE_ALL)
				{
					// Setting the limits before drawing avoids a double draw
					plot.setLimits(limits[0], limits[1], limits[2], limits[3]);
					flags = 0;
				}
				// If only some of the limits are to be preserved then we must get the current default min/max.
				// This cannot be done for a Plot class but we can do it for Plot2 (which makes public extra
				// functionality).
				if ((flags & PRESERVE_ALL) != 0 && plot instanceof Plot2)
				{
					final Plot2 p2 = (Plot2) plot;
					final double[] currentLimits = p2.getDefaultMinAndMax();
					if (currentLimits != null)
					{
						preserveLimits(plot, flags, limits, currentLimits);
						flags = 0;
					}
				}
				p.drawPlot(plot);
				preserveLimits(plot, flags, limits);
				if (!plotWindow.isVisible())
					plotWindow.setVisible(true);
				else if ((flags & NO_TO_FRONT) == 0)
					p.toFront();
			}
			catch (final Throwable t)
			{
				// Allow debugging
				t.printStackTrace();

				// Get the location and close the error window
				Point location = null;
				Dimension d = null;
				double[] limits = null;
				if (p != null)
				{
					location = p.getLocation();
					final Plot oldPlot = p.getPlot();
					d = oldPlot.getSize();
					limits = oldPlot.getLimits();
					try
					{
						p.close();
					}
					catch (final Throwable tt)
					{
						// Ignore
					}
				}

				// Show a new window
				if (d != null)
					plot.setSize(d.width, d.height);
				p = plot.show();
				if (location != null)
					p.setLocation(location);
				if (limits != null)
					preserveLimits(plot, flags, limits);
				if (windowOrganiser != null)
					windowOrganiser.add(p);
				newWindow = true;
			}
		return p;
	}

	/**
	 * Preserve limits the limits from the input array for all the set flags. Otherwise use the current plot limits.
	 *
	 * @param plot
	 *            the plot
	 * @param preserveLimits
	 *            the preserve limits flag
	 * @param limits
	 *            the limits
	 */
	private static void preserveLimits(Plot plot, int preserveLimits, double[] limits)
	{
		// Note: We must have drawn the plot to get the current limits
		preserveLimits(plot, preserveLimits, limits, plot.getLimits());
	}

	/**
	 * Preserve limits the limits from the input array for all the set flags. Otherwise use the current plot limits.
	 *
	 * @param plot
	 *            the plot
	 * @param preserveLimits
	 *            the preserve limits flag
	 * @param limits
	 *            the limits
	 * @param currentLimits
	 *            the current limits
	 */
	private static void preserveLimits(Plot plot, int preserveLimits, double[] limits, double[] currentLimits)
	{
		if (preserveLimits != 0)
		{
			if ((preserveLimits & PRESERVE_X_MIN) == 0)
				limits[0] = currentLimits[0];
			if ((preserveLimits & PRESERVE_X_MAX) == 0)
				limits[1] = currentLimits[1];
			if ((preserveLimits & PRESERVE_Y_MIN) == 0)
				limits[2] = currentLimits[2];
			if ((preserveLimits & PRESERVE_Y_MAX) == 0)
				limits[3] = currentLimits[3];

			plot.setLimits(limits[0], limits[1], limits[2], limits[3]);
		}
	}

	/**
	 * Hide the image window.
	 *
	 * @param title
	 *            the title
	 * @return True if a window with the title was found
	 */
	public static boolean hide(String title)
	{
		final int[] wList = WindowManager.getIDList();
		final int len = wList != null ? wList.length : 0;
		for (int i = 0; i < len; i++)
		{
			final ImagePlus imp = WindowManager.getImage(wList[i]);
			if (imp != null)
				if (imp.getTitle().equals(title))
				{
					imp.getWindow().setVisible(false);
					return true;
				}
		}
		return false;
	}

	/**
	 * Close the named window.
	 *
	 * @param name
	 *            the name
	 */
	public static void close(String name)
	{
		final Window w = WindowManager.getWindow(name);
		if (w != null)
			if (w instanceof ImageWindow)
				((ImageWindow) w).close();
			else if (w instanceof Frame)
			{
				WindowManager.removeWindow(w);
				w.dispose();
			}
	}

	/**
	 * Calculate a histogram given the provided data.
	 *
	 * @param data
	 *            the data
	 * @param numBins
	 *            The number of histogram bins between min and max
	 * @return The histogram as a pair of arrays: { value[], frequency[] }
	 */
	public static float[][] calcHistogram(float[] data, int numBins)
	{
		float min = Float.POSITIVE_INFINITY;
		float max = Float.NEGATIVE_INFINITY;
		for (final float f : data)
		{
			if (min > f)
				min = f;
			if (max < f)
				max = f;
		}
		return calcHistogram(data, min, max, numBins);
	}

	/**
	 * Calculate a histogram given the provided data.
	 * <p>
	 * The histogram will create the specified number of bins to accommodate all data between the minimum and maximum
	 * inclusive. The number of bins must be above one so that min and max are in different bins. If min and max are the
	 * same then the number of bins is set to 1.
	 *
	 * @param data
	 *            the data
	 * @param min
	 *            The minimum value to include (inclusive)
	 * @param max
	 *            The maximum value to include (inclusive)
	 * @param numBins
	 *            The number of histogram bins between min and max (must be above one)
	 * @return The histogram as a pair of arrays: { value[], frequency[] }
	 */
	public static float[][] calcHistogram(float[] data, double min, double max, int numBins)
	{
		// Parameter check
		if (numBins < 2)
			numBins = 2;
		if (max < min)
		{
			final double tmp = max;
			max = min;
			min = tmp;
		}
		final double binSize;
		if (max == min)
		{
			numBins = 1;
			binSize = 1;
		}
		else
			binSize = (max - min) / (numBins - 1);

		final float[] value = new float[numBins];
		final float[] frequency = new float[numBins];

		for (int i = 0; i < numBins; i++)
			value[i] = (float) (min + i * binSize);

		for (final double d : data)
		{
			final int bin = (int) ((d - min) / binSize);
			if (bin < 0)
			{ /* this data is smaller than min */
			}
			else if (bin >= numBins)
			{ /* this data point is bigger than max */
			}
			else
				frequency[bin]++;
		}

		return new float[][] { value, frequency };
	}

	/**
	 * Calculate a histogram given the provided data.
	 *
	 * @param data
	 *            the data
	 * @param numBins
	 *            The number of histogram bins between min and max
	 * @return The histogram as a pair of arrays: { value[], frequency[] }
	 */
	public static double[][] calcHistogram(double[] data, int numBins)
	{
		double min = Double.POSITIVE_INFINITY;
		double max = Double.NEGATIVE_INFINITY;
		for (final double f : data)
		{
			if (min > f)
				min = f;
			if (max < f)
				max = f;
		}
		return calcHistogram(data, min, max, numBins);
	}

	/**
	 * Calculate a histogram given the provided data.
	 * <p>
	 * The histogram will create the specified number of bins to accommodate all data between the minimum and maximum
	 * inclusive. The number of bins must be above one so that min and max are in different bins. If min and max are the
	 * same then the number of bins is set to 1.
	 *
	 * @param data
	 *            the data
	 * @param min
	 *            The minimum value to include (inclusive)
	 * @param max
	 *            The maximum value to include (inclusive)
	 * @param numBins
	 *            The number of histogram bins between min and max (must be above one)
	 * @return The histogram as a pair of arrays: { value[], frequency[] }
	 */
	public static double[][] calcHistogram(double[] data, double min, double max, int numBins)
	{
		// Parameter check
		if (numBins < 2)
			numBins = 2;
		if (max < min)
		{
			final double tmp = max;
			max = min;
			min = tmp;
		}
		final double binSize;
		if (max == min)
		{
			numBins = 1;
			binSize = 1;
		}
		else
			binSize = (max - min) / (numBins - 1);

		final double[] value = new double[numBins];
		final double[] frequency = new double[numBins];

		for (int i = 0; i < numBins; i++)
			value[i] = (min + i * binSize);

		for (final double d : data)
		{
			final int bin = (int) ((d - min) / binSize);
			if (bin < 0)
			{ /* this data is smaller than min */
			}
			else if (bin >= numBins)
			{ /* this data point is bigger than max */
			}
			else
				frequency[bin]++;
		}

		return new double[][] { value, frequency };
	}

	/**
	 * For the provided histogram x-axis bins, produce an x-axis for plotting. This functions doubles up the histogram
	 * x-positions to allow plotting a square line profile using the ImageJ plot command.
	 *
	 * @param histogramX
	 *            the histogram X
	 * @return the x-axis values
	 */
	public static float[] createHistogramAxis(float[] histogramX)
	{
		final float[] axis = new float[histogramX.length * 2 + 2];
		int index = 0;
		for (int i = 0; i < histogramX.length; ++i)
		{
			axis[index++] = histogramX[i];
			axis[index++] = histogramX[i];
		}
		if (histogramX.length > 0)
		{
			final float dx = (histogramX.length == 1) ? 1 : (histogramX[1] - histogramX[0]);
			axis[index++] = histogramX[histogramX.length - 1] + dx;
			axis[index++] = histogramX[histogramX.length - 1] + dx;
		}
		return axis;
	}

	/**
	 * For the provided histogram y-axis values, produce a y-axis for plotting. This functions doubles up the histogram
	 * values to allow plotting a square line profile using the ImageJ plot command.
	 *
	 * @param histogramY
	 *            the histogram Y
	 * @return the y-axis values
	 */
	public static float[] createHistogramValues(float[] histogramY)
	{
		final float[] axis = new float[histogramY.length * 2 + 2];

		int index = 0;
		axis[index++] = 0;
		for (int i = 0; i < histogramY.length; ++i)
		{
			axis[index++] = histogramY[i];
			axis[index++] = histogramY[i];
		}
		return axis;
	}

	/**
	 * For the provided histogram x-axis bins, produce an x-axis for plotting. This functions doubles up the histogram
	 * x-positions to allow plotting a square line profile using the ImageJ plot command.
	 *
	 * @param histogramX
	 *            the histogram X
	 * @return the x-axis values
	 */
	public static double[] createHistogramAxis(double[] histogramX)
	{
		final double[] axis = new double[histogramX.length * 2 + 2];
		int index = 0;
		for (int i = 0; i < histogramX.length; ++i)
		{
			axis[index++] = histogramX[i];
			axis[index++] = histogramX[i];
		}
		if (histogramX.length > 0)
		{
			final double dx = (histogramX.length == 1) ? 1 : (histogramX[1] - histogramX[0]);
			axis[index++] = histogramX[histogramX.length - 1] + dx;
			axis[index++] = histogramX[histogramX.length - 1] + dx;
		}
		return axis;
	}

	/**
	 * For the provided histogram y-axis values, produce a y-axis for plotting. This functions doubles up the histogram
	 * values to allow plotting a square line profile using the ImageJ plot command.
	 *
	 * @param histogramY
	 *            the histogram Y
	 * @return the y-axis values
	 */
	public static double[] createHistogramValues(double[] histogramY)
	{
		final double[] axis = new double[histogramY.length * 2 + 2];

		int index = 0;
		axis[index++] = 0;
		for (int i = 0; i < histogramY.length; ++i)
		{
			axis[index++] = histogramY[i];
			axis[index++] = histogramY[i];
		}
		return axis;
	}

	/**
	 * Return the histogram statistics.
	 *
	 * @param x
	 *            Histogram values
	 * @param y
	 *            Histogram counts
	 * @return Array containing: { mean, standard deviation }
	 */
	public static double[] getHistogramStatistics(float[] x, float[] y)
	{
		// Get the average
		double n = 0;
		double sum = 0.0;
		double sum2 = 0.0;
		for (int i = 0; i < x.length; i++)
			if (y[i] > 0)
			{
				final float count = y[i];
				final float value = x[i];
				n += count;
				sum += value * count;
				sum2 += (value * value) * count;
			}
		final double av = sum / n;

		// Get the Std.Dev
		double stdDev;
		if (n > 0)
		{
			final double d = n;
			stdDev = (d * sum2 - sum * sum) / d;
			if (stdDev > 0.0)
				stdDev = Math.sqrt(stdDev / (d - 1.0));
			else
				stdDev = 0.0;
		}
		else
			stdDev = 0.0;

		return new double[] { av, stdDev };
	}

	/**
	 * Logs a message to the ImageJ log.
	 *
	 * @param format
	 *            the format
	 * @param args
	 *            the args
	 */
	public static void log(String format, Object... args)
	{
		IJ.log(String.format(format, args));
	}

	/**
	 * Check if the escape key has been pressed. Show a status aborted message if true.
	 *
	 * @return True if aborted
	 */
	public static boolean isInterrupted()
	{
		if (IJ.escapePressed())
		{
			IJ.beep();
			IJ.showStatus("Aborted");
			return true;
		}
		return false;
	}

	/**
	 * Show a histogram of the data.
	 *
	 * @param title
	 *            The title to prepend to the plot name
	 * @param data
	 *            the data
	 * @param name
	 *            The name of plotted statistic
	 * @param minWidth
	 *            The minimum bin width to use (e.g. set to 1 for integer values)
	 * @param removeOutliers
	 *            Remove outliers. 1 - 1.5x IQR. 2 - remove top 2%.
	 * @param bins
	 *            The number of bins to use
	 * @return The histogram window ID
	 */
	public static int showHistogram(String title, DoubleData data, String name, double minWidth, int removeOutliers,
			int bins)
	{
		return showHistogram(title, data, name, minWidth, removeOutliers, bins, true, null);
	}

	/**
	 * Show a histogram of the data.
	 *
	 * @param title
	 *            The title to prepend to the plot name
	 * @param data
	 *            the data
	 * @param name
	 *            The name of plotted statistic
	 * @param minWidth
	 *            The minimum bin width to use (e.g. set to 1 for integer values)
	 * @param removeOutliers
	 *            Remove outliers. 1 - 1.5x IQR. 2 - remove top 2%.
	 * @param bins
	 *            The number of bins to use
	 * @param label
	 *            The label to add
	 * @return The histogram window ID
	 */
	public static int showHistogram(String title, DoubleData data, String name, double minWidth, int removeOutliers,
			int bins, String label)
	{
		return showHistogram(title, data, name, minWidth, removeOutliers, bins, true, label);
	}

	/**
	 * Show a histogram of the data.
	 *
	 * @param title
	 *            The title to prepend to the plot name
	 * @param data
	 *            the data
	 * @param name
	 *            The name of plotted statistic
	 * @param minWidth
	 *            The minimum bin width to use (e.g. set to 1 for integer values)
	 * @param removeOutliers
	 *            Remove outliers. 1 - 1.5x IQR. 2 - remove top 2%.
	 * @param bins
	 *            The number of bins to use
	 * @param barChart
	 *            Use a bar chart, else plot non-zero bin counts as a line plot
	 * @param label
	 *            The label to add
	 * @return The histogram window ID
	 */
	public static int showHistogram(String title, DoubleData data, String name, double minWidth, int removeOutliers,
			int bins, boolean barChart, String label)
	{
		return showHistogram(title, data, name, minWidth, removeOutliers, bins, (barChart) ? Plot2.BAR : Plot.LINE,
				label);
	}

	/**
	 * Show a histogram of the data.
	 *
	 * @param title
	 *            The title to prepend to the plot name
	 * @param data
	 *            the data
	 * @param name
	 *            The name of plotted statistic
	 * @param minWidth
	 *            The minimum bin width to use (e.g. set to 1 for integer values)
	 * @param removeOutliers
	 *            Remove outliers. 1 - 1.5x IQR. 2 - remove top 2%.
	 * @param bins
	 *            The number of bins to use
	 * @param shape
	 *            the shape
	 * @param label
	 *            The label to add
	 * @return The histogram window ID
	 */
	public static int showHistogram(String title, DoubleData data, String name, double minWidth, int removeOutliers,
			int bins, int shape, String label)
	{
		final double[] values = data.values();
		// If we have +/- Infinity in here it will break
		if (values == null || values.length < 2)
			return 0;
		final double[] limits = Maths.limits(values);
		double yMin = limits[0];
		double yMax = limits[1];
		double width;
		double lower = Double.NaN;
		double upper = Double.NaN;
		StoredDataStatistics stats = null;

		if (bins <= 0)
		{
			// Auto
			switch (defaultMethod)
			{
				case SCOTT:
					stats = (data instanceof StoredDataStatistics) ? (StoredDataStatistics) data
							: new StoredDataStatistics(data.values());
					width = getBinWidthScottsRule(stats.getStandardDeviation(), stats.size());
					bins = (int) Math.ceil((limits[1] - limits[0]) / width);
					break;

				case FD:
					stats = (data instanceof StoredDataStatistics) ? (StoredDataStatistics) data
							: new StoredDataStatistics(data.values());
					lower = stats.getStatistics().getPercentile(25);
					upper = stats.getStatistics().getPercentile(75);
					width = getBinWidthFreedmanDiaconisRule(upper, lower, stats.size());
					bins = (int) Math.ceil((limits[1] - limits[0]) / width);
					break;

				case STURGES:
					bins = getBinsSturgesRule(data.size());
					break;

				case SQRT:
				default:
					bins = getBinsSqrtRule(data.size());
			}
			// In case of error (N=0, Infinity in the data range)
			if (bins == Integer.MAX_VALUE)
				bins = getBinsSqrtRule(data.size());
		}
		//System.out.printf("Bins = %d\n", bins);

		switch (removeOutliers)
		{
			case 1:
				// Get the inter quartile range
				if (Double.isNaN(lower))
				{
					stats = (data instanceof StoredDataStatistics) ? (StoredDataStatistics) data
							: new StoredDataStatistics(data.values());
					lower = stats.getStatistics().getPercentile(25);
					upper = stats.getStatistics().getPercentile(75);
				}
				final double iqr = 1.5 * (upper - lower);
				yMin = FastMath.max(lower - iqr, yMin);
				yMax = FastMath.min(upper + iqr, yMax);
				break;

			case 2:
				// Remove top 2%
				if (stats == null)
					stats = (data instanceof StoredDataStatistics) ? (StoredDataStatistics) data
							: new StoredDataStatistics(data.values());
				yMax = stats.getStatistics().getPercentile(98);
				break;

		}

		if (minWidth > 0)
		{
			final double binSize = (yMax - yMin) / ((bins < 2) ? 1 : bins - 1);
			if (binSize < minWidth)
				bins = (int) ((yMax - yMin) / minWidth) + 1;
			//yMax = bins * minWidth + yMin;
		}
		//		else
		//		{
		//			// Calculate the resolution, i.e. the smallest gap between data points
		//			double resolution = Double.POSITIVE_INFINITY;
		//			for (int i=1; i<values.length; i++)
		//			{
		//				if (values[i-1] != values[i])
		//				{
		//					if (resolution > values[i] - values[i-1])
		//						resolution = values[i] - values[i-1];
		//				}
		//			}
		//
		//			// Set the number of bins as the most needed to separate the data points.
		//			// This prevents gaps in the histogram
		//			if (resolution != Double.POSITIVE_INFINITY)
		//			{
		//				int numBins = 1 + (int)((yMax - yMin) / resolution);
		//				if (bins > numBins)
		//					bins = numBins;
		//			}
		//		}

		title += " " + name;

		final double[][] hist = Utils.calcHistogram(values, yMin, yMax, bins);

		final boolean barChart = (shape & Plot2.BAR) == Plot2.BAR;
		if (barChart)
		{
			// Standard histogram
			xValues = hist[0]; //Utils.createHistogramAxis(hist[0]);
			yValues = hist[1]; //Utils.createHistogramValues(hist[1]);
		}
		else
		{
			// Line plot of non-zero values
			int c = 0;
			xValues = new double[hist[0].length];
			yValues = new double[xValues.length];
			for (int i = 0; i < xValues.length; i++)
				if (hist[1][i] != 0)
				{
					xValues[c] = hist[0][i];
					yValues[c] = hist[1][i];
					c++;
				}
			xValues = Arrays.copyOf(xValues, c);
			yValues = Arrays.copyOf(yValues, c);
		}

		plot = new Plot2(title, name, "Frequency");
		Utils.xMin = Utils.xMax = Utils.yMin = Utils.yMax = 0;
		if (xValues.length > 0)
		{
			double dx = 0;
			if (barChart)
				dx = (xValues.length == 1) ? 1 : (xValues[1] - xValues[0]);
			final double xMax = xValues[xValues.length - 1] + dx;
			final double xPadding = 0.05 * (xMax - xValues[0]);
			Utils.xMin = xValues[0] - xPadding;
			Utils.xMax = xMax + xPadding;
			Utils.yMax = Maths.max(yValues) * 1.05;
			plot.setLimits(xMin, xMax, Utils.yMin, Utils.yMax);
		}
		plot.addPoints(xValues, yValues, shape);
		if (label != null)
			plot.addLabel(0, 0, label);
		final PlotWindow window = Utils.display(title, plot);
		return window.getImagePlus().getID();
	}

	/**
	 * The method to select the number of histogram bins.
	 */
	public enum BinMethod
	{
		/** The Scott's rule bin method. See {@link Utils#getBinWidthScottsRule(double, int)}. */
		SCOTT,
		/**
		 * The Freedman-Diaconis rule bin method. See
		 * {@link Utils#getBinWidthFreedmanDiaconisRule(double, double, int)}.
		 */
		FD,
		/** The Sturges' rule bin method. See {@link Utils#getBinsSturgesRule(int)}. */
		STURGES,
		/** The square root rule bin method. See {@link Utils#getBinsSqrtRule(int)}. */
		SQRT
	}

	/** The default method to select the histogram bins. Used if the input number of bins is zero. */
	public static BinMethod defaultMethod = BinMethod.SCOTT;

	/**
	 * Gets the bins.
	 * <p>
	 * Based on the MatLab methods.
	 *
	 * @param data
	 *            the data
	 * @param method
	 *            the method
	 * @return the bins
	 * @see "http://uk.mathworks.com/help/matlab/ref/histogram.html"
	 */
	public static int getBins(DoubleData data, BinMethod method)
	{
		double width;
		double[] limits;
		switch (method)
		{
			case SCOTT:
				final Statistics stats = (data instanceof Statistics) ? (Statistics) data
						: new Statistics(data.values());
				width = getBinWidthScottsRule(stats.getStandardDeviation(), data.size());
				limits = Maths.limits(data.values());
				return (int) Math.ceil((limits[1] - limits[0]) / width);

			case FD:
				final DescriptiveStatistics descriptiveStats = (data instanceof StoredDataStatistics)
						? ((StoredDataStatistics) data).getStatistics()
						: new DescriptiveStatistics(data.values());
				final double lower = descriptiveStats.getPercentile(25);
				final double upper = descriptiveStats.getPercentile(75);
				width = getBinWidthFreedmanDiaconisRule(upper, lower, data.size());
				limits = Maths.limits(data.values());
				return (int) Math.ceil((limits[1] - limits[0]) / width);

			case STURGES:
				return getBinsSturgesRule(data.size());

			case SQRT:
			default:
				return getBinsSqrtRule(data.size());
		}
	}

	/**
	 * Gets the bin width using Scott's rule:
	 *
	 * <pre>
	 * 3.5 * sd / cubeRoot(n)
	 * </pre>
	 *
	 * @param sd
	 *            the sd
	 * @param n
	 *            the n
	 * @return the bin width using Scott's rule
	 */
	public static double getBinWidthScottsRule(double sd, int n)
	{
		return 3.5 * sd / FastMath.cbrt(n);
	}

	/**
	 * Gets the bin width using the Freedman-Diaconis rule.
	 *
	 * <pre>
	 * 2 * IQR / cubeRoot(n)
	 * </pre>
	 *
	 * @param upper
	 *            the upper of the Inter-Quartile Range (IQR)
	 * @param lower
	 *            the lower of the Inter-Quartile Range (IQR)
	 * @param n
	 *            the n
	 * @return the bin width freedman diaconis rule
	 */
	public static double getBinWidthFreedmanDiaconisRule(double upper, double lower, int n)
	{
		final double iqr = upper - lower;
		return 2 * iqr / FastMath.cbrt(n);
	}

	/**
	 * Gets the bins using the Sturges' rule.
	 *
	 * <pre>
	 * ceil(1 + log2(n)
	 * </pre>
	 *
	 * @param n
	 *            the n
	 * @return the number of bins
	 */
	public static int getBinsSturgesRule(int n)
	{
		return (int) Math.ceil(1 + Math.log(n) / 0.69314718);
	}

	/**
	 * Gets the bins using the square root rule.
	 *
	 * <pre>
	 * ceil(squareRoot(n))
	 * </pre>
	 *
	 * @param n
	 *            the n
	 * @return the number of bins
	 */
	public static int getBinsSqrtRule(int n)
	{
		return (int) Math.ceil(Math.sqrt(n));
	}

	/** The x values from the last histogram plotted. */
	public static double[] xValues;
	/** The y values from the last histogram plotted. */
	public static double[] yValues;
	/** The x min from the last histogram plotted. */
	public static double xMin;
	/** The x max from the last histogram plotted. */
	public static double xMax;
	/** The y min from the last histogram plotted. */
	public static double yMin;
	/** The y max from the last histogram plotted. */
	public static double yMax;
	/** The last histogram plotted. */
	public static Plot2 plot;

	/**
	 * Checks if is new window.
	 *
	 * @return True is the last call to display created a new window
	 */
	public static boolean isNewWindow()
	{
		return newWindow;
	}

	private static int PROGRESS_BAR_STATUS = 0;
	private static int IS_SHOW_PROGRESS = 0;
	private static ProgressBar progressBar = null;

	/**
	 * Gets the ImageJ GUI progress bar.
	 *
	 * @return the progress bar (or null if there is no ImageJ instance)
	 */
	public static ProgressBar getProgressBar()
	{
		if (PROGRESS_BAR_STATUS == 0)
		{
			if (IJ.getInstance() != null)
				progressBar = IJ.getInstance().getProgressBar();
			PROGRESS_BAR_STATUS = (progressBar == null) ? -1 : 1;
		}
		return progressBar;
	}

	/**
	 * Checks if the ImageJ status bar is not null.
	 *
	 * @return true, if is show progress
	 */
	public static boolean isShowProgress()
	{
		return IS_SHOW_PROGRESS != -1;
	}

	/**
	 * Use reflection to replace the progress bar with null.
	 *
	 * @param showProgress
	 *            Set to true to disable the progress bar
	 */
	public static void setShowProgress(boolean showProgress)
	{
		getProgressBar();

		if (PROGRESS_BAR_STATUS == -1)
			return;

		ProgressBar newProgressBar;
		if (showProgress)
			newProgressBar = progressBar;
		else
			newProgressBar = null;

		try
		{
			final Field f = IJ.class.getDeclaredField("progressBar");
			f.setAccessible(true);
			f.set(IJ.class, newProgressBar);
			IS_SHOW_PROGRESS = (showProgress) ? 1 : -1;
		}
		catch (final Exception e)
		{
			IS_SHOW_PROGRESS = 0;
			PROGRESS_BAR_STATUS = -1;
		}
	}

	private static int STATUS_LINE_STATUS = 0;
	private static int IS_SHOW_STATUS_LINE = 0;
	private static JLabel statusLine = null;

	/**
	 * Gets the ImageJ GUI status bar label.
	 *
	 * @return the status bar label
	 */
	public static JLabel getStatusLine()
	{
		if (STATUS_LINE_STATUS == 0)
		{
			if (IJ.getInstance() != null)
			{
				final Panel statusBar = IJ.getInstance().getStatusBar();
				for (final Component c : statusBar.getComponents())
					if (c instanceof JLabel)
					{
						statusLine = (JLabel) statusBar.getComponent(0);
						break;
					}
			}
			STATUS_LINE_STATUS = (statusLine == null) ? -1 : 1;
		}
		return statusLine;
	}

	/**
	 * Checks if the ImageJ status bar label is not null.
	 *
	 * @return true, if is show status
	 */
	public static boolean isShowStatus()
	{
		return IS_SHOW_STATUS_LINE != -1;
	}

	/**
	 * Use reflection to replace the status bar label with null.
	 *
	 * @param showStatus
	 *            Set to true to disable the status bar
	 */
	public static void setShowStatus(boolean showStatus)
	{
		getStatusLine();

		if (STATUS_LINE_STATUS == -1)
			return;

		JLabel newStatusLine;
		if (showStatus)
			newStatusLine = statusLine;
		else
			// Provide a label that will swallow method calls to setText()
			newStatusLine = new JLabel();

		try
		{
			final ImageJ ij = IJ.getInstance();
			final Field f = ij.getClass().getDeclaredField("statusLine");
			f.setAccessible(true);
			f.set(ij, newStatusLine);
			IS_SHOW_STATUS_LINE = (showStatus) ? 1 : -1;
		}
		catch (final Exception e)
		{
			IS_SHOW_STATUS_LINE = 0;
			STATUS_LINE_STATUS = -1;
		}
	}

	/**
	 * Convert time in milliseconds into a nice string.
	 *
	 * @param time
	 *            the time
	 * @return The string
	 */
	public static String timeToString(double time)
	{
		String units = " ms";
		if (time > 1000) // 1 second
		{
			time /= 1000;
			units = " s";

			if (time > 180) // 3 minutes
			{
				time /= 60;
				units = " min";
			}
		}
		return Utils.rounded(time, 4) + units;
	}

	/**
	 * Replace the filename extension with the specified extension.
	 *
	 * @param filename
	 *            the filename
	 * @param extension
	 *            the extension
	 * @return the new filename
	 */
	public static String replaceExtension(String filename, String extension)
	{
		if (filename != null)
		{
			final int index = filename.lastIndexOf('.');
			final int index2 = filename.lastIndexOf(File.separatorChar);
			if (index > index2)
				filename = filename.substring(0, index);
			filename += (extension.startsWith(".")) ? extension : "." + extension;
		}
		return filename;
	}

	/**
	 * Remove the filename extension.
	 *
	 * @param filename
	 *            the filename
	 * @return the new filename
	 */
	public static String removeExtension(String filename)
	{
		if (filename != null)
		{
			final int index = filename.lastIndexOf('.');
			final int index2 = filename.lastIndexOf(File.separatorChar);
			if (index > index2)
				filename = filename.substring(0, index);
		}
		return filename;
	}

	/**
	 * Check if the current window has the given headings, refreshing the headings if necessary.
	 * Only works if the window is showing.
	 *
	 * @param textWindow
	 *            the text window
	 * @param headings
	 *            the headings
	 * @param preserve
	 *            Preserve the current data (note that is may not match the new headings)
	 * @return True if the window headings were changed
	 */
	public static boolean refreshHeadings(TextWindow textWindow, String headings, boolean preserve)
	{
		if (textWindow != null && textWindow.isShowing())
			if (!textWindow.getTextPanel().getColumnHeadings().equals(headings))
			{
				final TextPanel tp = textWindow.getTextPanel();
				String text = null;
				if (preserve)
				{
					tp.setColumnHeadings("");
					text = tp.getText();
				}

				tp.setColumnHeadings(headings);

				if (preserve)
					tp.append(text);

				return true;
			}
		return false;
	}

	/**
	 * Create and fill an array.
	 *
	 * @param length
	 *            The length of the array
	 * @param start
	 *            The start
	 * @param increment
	 *            The increment
	 * @return The new array
	 * @deprecated This method has been moved to gdsc.core.utils.SimpleArrayUtils
	 */
	@Deprecated
	public static double[] newArray(int length, double start, double increment)
	{
		final double[] data = new double[length];
		for (int i = 0; i < length; i++, start += increment)
			data[i] = start;
		return data;
	}

	/**
	 * Create and fill an array.
	 *
	 * @param length
	 *            The length of the array
	 * @param start
	 *            The start
	 * @param increment
	 *            The increment
	 * @return The new array
	 * @deprecated This method has been moved to gdsc.core.utils.SimpleArrayUtils
	 */
	@Deprecated
	public static int[] newArray(int length, int start, int increment)
	{
		final int[] data = new int[length];
		for (int i = 0; i < length; i++, start += increment)
			data[i] = start;
		return data;
	}

	/**
	 * Waits for all threads to complete computation.
	 * <p>
	 * Catches ExecutionException and InterruptedException and re-throws them as a RuntimeException. This is a
	 * convenience method to allow a simple wait for futures without explicit try/catch blocks.
	 *
	 * @param futures
	 *            the futures
	 * @throws RuntimeException
	 *             a runtime exception that is the cause or a new exception wrapping the cause of the the error
	 */
	public static void waitForCompletion(List<Future<?>> futures) throws RuntimeException
	{
		waitForCompletion(futures, false);
	}

	/**
	 * Waits for all threads to complete computation.
	 * <p>
	 * Catches {@link ExecutionException } and {@link InterruptedException} and re-throws them as a RuntimeException.
	 * This is a convenience method to allow a simple wait for futures without explicit try/catch blocks.
	 *
	 * @param futures
	 *            the futures
	 * @param print
	 *            flag to indicate that the stack trace should be printed
	 * @throws RuntimeException
	 *             a runtime exception that is the {@link ExecutionException } or a new exception wrapping the cause of
	 *             the the error
	 * @throws OutOfMemoryError
	 *             an out of memory error if this is the {@link ExecutionException }
	 */
	public static void waitForCompletion(List<Future<?>> futures, boolean print)
			throws RuntimeException, OutOfMemoryError
	{
		try
		{
			for (final Future<?> f : futures)
				f.get();
		}
		catch (final ExecutionException ex)
		{
			if (print)
				ex.printStackTrace();
			final Throwable t = ex.getCause();
			if (t != null)
			{
				if (t instanceof RuntimeException)
					throw (RuntimeException) t;
				if (t instanceof OutOfMemoryError)
					throw (OutOfMemoryError) t;
			}
			throw new RuntimeException((t != null) ? t : ex);
		}
		catch (final InterruptedException ex)
		{
			if (print)
				ex.printStackTrace();
			final Throwable t = ex.getCause();
			if (t != null)
			{
				if (t instanceof RuntimeException)
					throw (RuntimeException) t;
				if (t instanceof OutOfMemoryError)
					throw (OutOfMemoryError) t;
			}
			throw new RuntimeException((t != null) ? t : ex);
		}
	}

	/**
	 * Open a directory selection dialog using the given title (and optionally the default directory).
	 *
	 * @param title
	 *            The dialog title
	 * @param directory
	 *            The default directory to start in
	 * @return The directory (or null if the dialog is cancelled)
	 */
	public static String getDirectory(String title, String directory)
	{
		final String defaultDir = OpenDialog.getDefaultDirectory();
		if (!TextUtils.isNullOrEmpty(directory))
			OpenDialog.setDefaultDirectory(directory);
		final DirectoryChooser chooser = new DirectoryChooser(title);
		directory = chooser.getDirectory();
		if (!TextUtils.isNullOrEmpty(defaultDir))
			OpenDialog.setDefaultDirectory(defaultDir);
		return directory;
	}

	/**
	 * Open a file selection dialog using the given title (and optionally the default path).
	 *
	 * @param title
	 *            The dialog title
	 * @param filename
	 *            The default path to start with
	 * @return The path (or null if the dialog is cancelled)
	 */
	public static String getFilename(String title, String filename)
	{
		final String[] path = Utils.decodePath(filename);
		final OpenDialog chooser = new OpenDialog(title, path[0], path[1]);
		if (chooser.getFileName() != null)
			return chooser.getDirectory() + chooser.getFileName();
		return null;
	}

	/**
	 * Determine if the plugin is running with extra options. Checks for the ImageJ shift or alt key down properties. If
	 * running in a macro then searches the options string for the 'extraoptions' flag.
	 * <p>
	 * If the extra options are required then adds the 'extraoptions' flag to the macro recorder options.
	 *
	 * @return True if extra options are required
	 */
	public static boolean isExtraOptions()
	{
		final String EXTRA = "extraoptions";
		boolean extraOptions = IJ.altKeyDown() || IJ.shiftKeyDown();
		if (!extraOptions && IJ.isMacro())
			extraOptions = (Macro.getOptions() != null && Macro.getOptions().contains(EXTRA));
		if (extraOptions)
			Recorder.recordOption(EXTRA);
		return extraOptions;
	}

	/**
	 * Convert the input array to a double.
	 *
	 * @param a
	 *            the a
	 * @return The new array
	 * @deprecated This method has been moved to gdsc.core.utils.SimpleArrayUtils
	 */
	@Deprecated
	public static double[] toDouble(float[] a)
	{
		if (a == null)
			return null;
		final double[] b = new double[a.length];
		for (int i = 0; i < a.length; i++)
			b[i] = a[i];
		return b;
	}

	/**
	 * Convert the input array to a float.
	 *
	 * @param a
	 *            the a
	 * @return The new array
	 * @deprecated This method has been moved to gdsc.core.utils.SimpleArrayUtils
	 */
	@Deprecated
	public static float[] toFloat(double[] a)
	{
		if (a == null)
			return null;
		final float[] b = new float[a.length];
		for (int i = 0; i < a.length; i++)
			b[i] = (float) a[i];
		return b;
	}

	/**
	 * Return "s" if the size is not 1 otherwise returns an empty string. This can be used to add an s where necessary
	 * to adjectives:
	 *
	 * <pre>
	 * System.out.printf(&quot;Created %d thing%s\n&quot;, n, TextUtils.pleural(n));
	 * </pre>
	 *
	 * @param n
	 *            The number of things
	 * @return "s" or empty string
	 * @deprecated This method has been moved to gdsc.core.utils.TextUtils
	 */
	@Deprecated
	public static String pleural(int n)
	{
		return (Math.abs(n) == 1) ? "" : "s";
	}

	/**
	 * Return "s" if the size is not 1 otherwise returns an empty string. This can be used to add an s where necessary
	 * to adjectives:
	 *
	 * <pre>
	 * System.out.printf(&quot;Created %s\n&quot;, TextUtils.pleural(n, &quot;thing&quot;));
	 * </pre>
	 *
	 * @param n
	 *            The number of things
	 * @param name
	 *            The name of the thing
	 * @return "s" or empty string
	 * @deprecated This method has been moved to gdsc.core.utils.TextUtils
	 */
	@Deprecated
	public static String pleural(int n, String name)
	{
		return n + " " + name + ((Math.abs(n) == 1) ? "" : "s");
	}

	/**
	 * Check if the string is null or length zero. Does not check for a string of whitespace.
	 *
	 * @param string
	 *            the string
	 * @return true if the string is null or length zero
	 * @deprecated This method has been moved to gdsc.core.utils.TextUtils
	 */
	@Deprecated
	public static boolean isNullOrEmpty(String string)
	{
		return string == null || string.length() == 0;
	}

	private static long lastTime = 0;

	/**
	 * Show a message on the status bar if enough time has passed since the last call.
	 *
	 * @param message
	 *            The message
	 * @return True if shown
	 */
	public static boolean showStatus(String message)
	{
		final long time = System.currentTimeMillis();
		if (time - lastTime > 150)
		{
			lastTime = time;
			IJ.showStatus(message);
			return true;
		}
		return false;
	}

	/**
	 * Set the current source rectangle to centre the view on the given coordinates
	 *
	 * Adapted from ij.gui.ImageCanvas.adjustSourceRect(double newMag, int x, int y)
	 *
	 * @param imp
	 *            The image
	 * @param newMag
	 *            The new magnification (set to zero to use the current magnification)
	 * @param x
	 *            The x coordinate
	 * @param y
	 *            The y coordinate
	 */
	public static void adjustSourceRect(ImagePlus imp, double newMag, int x, int y)
	{
		final ImageCanvas ic = imp.getCanvas();
		if (ic == null)
			return;
		final Dimension d = ic.getPreferredSize();
		final int dstWidth = d.width, dstHeight = d.height;
		final int imageWidth = imp.getWidth(), imageHeight = imp.getHeight();
		if (newMag <= 0)
			newMag = ic.getMagnification();
		int w = (int) Math.round(dstWidth / newMag);
		if (w * newMag < dstWidth)
			w++;
		int h = (int) Math.round(dstHeight / newMag);
		if (h * newMag < dstHeight)
			h++;
		//x = ic.offScreenX(x);
		//y = ic.offScreenY(y);
		final Rectangle r = new Rectangle(x - w / 2, y - h / 2, w, h);
		if (r.x < 0)
			r.x = 0;
		if (r.y < 0)
			r.y = 0;
		if (r.x + w > imageWidth)
			r.x = imageWidth - w;
		if (r.y + h > imageHeight)
			r.y = imageHeight - h;
		ic.setSourceRect(r);
		ic.setMagnification(newMag);
		ic.repaint();
	}

	/**
	 * Returns a list of the IDs of open images. Returns
	 * an empty array if no windows are open.
	 *
	 * @return List of IDs
	 * @see ij.WindowManager#getIDList()
	 */
	public static int[] getIDList()
	{
		final int[] list = WindowManager.getIDList();
		return (list != null) ? list : new int[0];
	}

	/**
	 * Build a list of all the image names.
	 *
	 * @param flags
	 *            Specify the types of image to collate
	 * @return The list of images
	 */
	public static String[] getImageList(final int flags)
	{
		return getImageList(flags, null);
	}

	/**
	 * Build a list of all the image names.
	 *
	 * @param flags
	 *            Specify the types of image to collate
	 * @param ignoreSuffix
	 *            A list of title suffixes to ignore
	 * @return The list of images
	 */
	public static String[] getImageList(final int flags, String[] ignoreSuffix)
	{
		final ArrayList<String> newImageList = new ArrayList<>();

		if ((flags & NO_IMAGE) == NO_IMAGE)
			newImageList.add(NO_IMAGE_TITLE);

		for (final int id : getIDList())
		{
			final ImagePlus imp = WindowManager.getImage(id);
			if (imp == null)
				continue;
			// Check flags
			if ((flags & SINGLE) == SINGLE && imp.getNDimensions() > 2)
				continue;
			if ((flags & BINARY) == BINARY && !imp.getProcessor().isBinary())
				continue;
			if ((flags & GREY_SCALE) == GREY_SCALE && imp.getBitDepth() == 24)
				continue;
			if ((flags & GREY_8_16) == GREY_8_16 && (imp.getBitDepth() == 24 || imp.getBitDepth() == 32))
				continue;
			if (ignoreImage(ignoreSuffix, imp.getTitle()))
				continue;

			newImageList.add(imp.getTitle());
		}

		return newImageList.toArray(new String[0]);
	}

	/**
	 * Return true if the image title ends with any of the specified suffixes.
	 *
	 * @param ignoreSuffix
	 *            A list of title suffixes to ignore
	 * @param title
	 *            The image title
	 * @return true if the image title ends with any of the specified suffixes
	 */
	public static boolean ignoreImage(String[] ignoreSuffix, String title)
	{
		if (ignoreSuffix != null)
			for (final String suffix : ignoreSuffix)
				if (title.endsWith(suffix))
					return true;
		return false;
	}

	/**
	 * Return the interval for reporting progress to the ImageJ progress bar given the total number of steps. Code
	 * should use the following prototype: <br/>
	 *
	 * <pre>
	 * final int interval = Utils.getProgressInterval(total);
	 * for (int i = 0; i &lt; total; i++)
	 * {
	 * 	if (i % interval == 0)
	 * 	{
	 * 		IJ.showProgress(i, total);
	 * 	}
	 * }
	 * </pre>
	 *
	 * @param total
	 *            the total number of steps
	 * @return The interval
	 */
	public static int getProgressInterval(int total)
	{
		return (total > 200) ? total / 100 : 1;
	}

	/**
	 * Return the interval for reporting progress to the ImageJ progress bar given the total number of steps.
	 *
	 * @param total
	 *            the total number of steps
	 * @return The interval
	 */
	public static long getProgressInterval(long total)
	{
		return (total > 200L) ? total / 100L : 1L;
	}

	/**
	 * Combine the arguments into a complete file path.
	 *
	 * @param paths
	 *            the paths
	 * @return The file path
	 */
	public static String combinePath(String... paths)
	{
		File file = new File(paths[0]);

		for (int i = 1; i < paths.length; i++)
			file = new File(file, paths[i]);

		return file.getPath();
	}

	/**
	 * Perform an either/or operator.
	 *
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @return true if one or the other is true but not both
	 */
	public static boolean xor(boolean a, boolean b)
	{
		return (a && !b) || (b && !a);
	}

	/**
	 * Extracts a single tile image processor from a hyperstack using the given projection method from the ZProjector.
	 *
	 * @param imp
	 *            Image hyperstack
	 * @param frame
	 *            The frame to extract
	 * @param channel
	 *            The channel to extract
	 * @param projectionMethod
	 *            the projection method
	 * @return A new image processor
	 * @see ij.plugin.ZProjector
	 */
	public static ImageProcessor extractTile(ImagePlus imp, int frame, int channel, int projectionMethod)
	{
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
	 * Add the platform specific file separator character to the directory (if missing).
	 *
	 * @param directory
	 *            the directory
	 * @return The directory
	 */
	public static String addFileSeparator(String directory)
	{
		if (directory.length() > 0 && !(directory.endsWith("/") || directory.endsWith("\\")))
			directory += Prefs.separator;
		return directory;
	}

	/**
	 * True if the generic dialog will be shown. This will return false if headless or if there are macro options for
	 * the dialog (i.e. a macro is running and the dialog will not present to the user)
	 *
	 * @return true, if the GenericDialog can and will be shown
	 */
	public static boolean isShowGenericDialog()
	{
		if (java.awt.GraphicsEnvironment.isHeadless())
			return false;

		return Macro.getOptions() == null;
	}

	/**
	 * True if a macro is running and the generic dialog will not present to the user.
	 *
	 * @return true, if a macro is running
	 */
	public static boolean isMacro()
	{
		return Macro.getOptions() != null;
	}

	/**
	 * Write the text to file.
	 *
	 * @param filename
	 *            the filename
	 * @param text
	 *            the text
	 * @return true, if successful
	 * @deprecated This method has been moved to gdsc.core.utils.TextUtils
	 */
	@Deprecated
	public static boolean write(String filename, String text)
	{
		try (FileOutputStream fs = new FileOutputStream(filename))
		{
			fs.write(text.getBytes());
			return true;
		}
		catch (final FileNotFoundException e)
		{
			//e.printStackTrace();
		}
		catch (final IOException e)
		{
			//e.printStackTrace();
		}
		return false;
	}

	/**
	 * Rearrange the columns in the dialog layout so that each column has the configured number of rows.
	 * <p>
	 * Assumes the generic dialog has been constructed normally/ and consists of a layout with pairs of items in
	 * ascending y in the grid layout. The components are extracted put into a panel for each column. The panels
	 * are then added to the dialog.
	 *
	 * @param gd
	 *            the dialog
	 * @param rowsPerColumn
	 *            the rows per column, for each column in order
	 */
	public static void rearrangeColumns(GenericDialog gd, int... rowsPerColumn)
	{
		if (!isShowGenericDialog())
			return;
		if (rowsPerColumn.length < 1)
			return;

		final LayoutManager manager = gd.getLayout();
		if (manager != null && manager instanceof GridBagLayout)
		{
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
			int yOffset = 0;

			for (final Component comp : gd.getComponents())
			{
				final GridBagConstraints c = grid.getConstraints(comp);

				// Check if this should be a new column
				if (c.gridy >= nextColumnY)
				{
					current = new Panel();
					currentGrid = new GridBagLayout();
					current.setLayout(currentGrid);
					panels.add(current);

					// Used to reset the y to the top of the column
					yOffset = nextColumnY;

					counter++;
					if (counter < rowsPerColumn.length)
						nextColumnY += rowsPerColumn[counter];
					else
						nextColumnY = Integer.MAX_VALUE;
				}

				// Reposition in the current column
				c.gridy = c.gridy - yOffset;
				//c.insets.left = c.insets.left + 10 * xOffset;
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
			for (int i = 0; i < panels.size(); i++)
			{
				final Panel p = panels.get(i);
				c.gridx = i;
				gd.add(p);
				grid.setConstraints(p, c);
				// For the next columns
				c.insets.left = 10;
			}

			if (IJ.isLinux())
				gd.setBackground(new Color(238, 238, 238));
		}
	}

	/**
	 * Gets the column from the text panel.
	 *
	 * @param tp
	 *            the tp
	 * @param heading
	 *            the heading
	 * @return the column
	 */
	public static int getColumn(TextPanel tp, String heading)
	{
		if (tp != null)
		{
			final String[] headings = tp.getColumnHeadings().split("\t");
			for (int i = 0; i < headings.length; i++)
				if (headings[i].equals(heading))
					return i;
		}
		return -1;
	}

	/**
	 * Gets the bit depth.
	 *
	 * @param pixels
	 *            the pixels
	 * @return the bit depth
	 * @throws IllegalArgumentException
	 *             If the pixesl array is an unrecognised type
	 */
	public static int getBitDepth(Object pixels) throws IllegalArgumentException
	{
		if (pixels instanceof float[])
			return 32;
		if (pixels instanceof short[])
			return 16;
		if (pixels instanceof byte[])
			return 8;
		if (pixels instanceof int[])
			return 24;
		throw new IllegalArgumentException("Unrecognised pixels array");
	}

	/**
	 * Creates the processor.
	 *
	 * @param width
	 *            the width
	 * @param height
	 *            the height
	 * @param pixels
	 *            the pixels
	 * @return the image processor
	 * @throws IllegalArgumentException
	 *             If the pixesl array is an unrecognised type
	 */
	public static ImageProcessor createProcessor(int width, int height, Object pixels) throws IllegalArgumentException
	{
		if (pixels instanceof float[])
			return new FloatProcessor(width, height, (float[]) pixels);
		if (pixels instanceof short[])
			return new ShortProcessor(width, height, (short[]) pixels, null);
		if (pixels instanceof byte[])
			return new ByteProcessor(width, height, (byte[]) pixels);
		if (pixels instanceof int[])
			return new ColorProcessor(width, height, (int[]) pixels);
		throw new IllegalArgumentException("Unrecognised pixels array");
	}

	/**
	 * Get the min/max display range from the image's current {@link ImageProcessor} using the ImageJ Auto adjust method
	 * (copied from {@link ij.plugin.frame.ContrastAdjuster }).
	 * <p>
	 * Although the ContrastAdjuster records its actions as 'run("Enhance Contrast", "saturated=0.35");' it actually
	 * does something else which makes the image easier to see than the afore mentioned command.
	 *
	 * @param imp
	 *            the image
	 * @param update
	 *            Set to true to update the image display range
	 * @return [min,max]
	 */
	public static double[] autoAdjust(ImagePlus imp, boolean update)
	{
		ImageStatistics stats = imp.getRawStatistics(); // get uncalibrated stats
		int limit = stats.pixelCount / 10;
		int[] histogram = stats.histogram;
		int autoThreshold = 0;
		if (autoThreshold < 10)
			autoThreshold = 5000;
		else
			autoThreshold /= 2;
		int threshold = stats.pixelCount / autoThreshold;
		int i = -1;
		boolean found = false;
		int count;
		do
		{
			i++;
			count = histogram[i];
			if (count > limit)
				count = 0;
			found = count > threshold;
		} while (!found && i < 255);
		int hmin = i;
		i = 256;
		do
		{
			i--;
			count = histogram[i];
			if (count > limit)
				count = 0;
			found = count > threshold;
		} while (!found && i > 0);
		int hmax = i;
		double min, max;
		if (hmax >= hmin)
		{
			min = stats.histMin + hmin * stats.binSize;
			max = stats.histMin + hmax * stats.binSize;
			if (min == max)
			{
				min = stats.min;
				max = stats.max;
			}
		}
		else
		{
			min = stats.min;
			max = stats.max;
		}
		if (update)
			imp.setDisplayRange(min, max);
		return new double[] { min, max };
	}
}
