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
package ij.gui;

import java.awt.Window;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.macro.Interpreter;

/**
 * Extension of the ij.gui.Plot class to add functionality
 */
public class Plot2 extends Plot
{
	/** Draw a bar plot. */
	public static final int BAR = 999;

	private static boolean failedOverride = false;

	/**
	 * Instantiates a new plot 2.
	 *
	 * @param title
	 *            the title
	 * @param xLabel
	 *            the x label
	 * @param yLabel
	 *            the y label
	 * @param xValues
	 *            the x values
	 * @param yValues
	 *            the y values
	 */
	public Plot2(String title, String xLabel, String yLabel, float[] xValues, float[] yValues)
	{
		super(title, xLabel, yLabel, xValues, yValues);
	}

	/**
	 * Instantiates a new plot 2.
	 *
	 * @param title
	 *            the title
	 * @param xLabel
	 *            the x label
	 * @param yLabel
	 *            the y label
	 * @param xValues
	 *            the x values
	 * @param yValues
	 *            the y values
	 */
	public Plot2(String title, String xLabel, String yLabel, double[] xValues, double[] yValues)
	{
		super(title, xLabel, yLabel, xValues, yValues);
	}

	/**
	 * Instantiates a new plot 2.
	 *
	 * @param dummy
	 *            the dummy
	 * @param title
	 *            the title
	 * @param xLabel
	 *            the x label
	 * @param yLabel
	 *            the y label
	 * @param xValues
	 *            the x values
	 * @param yValues
	 *            the y values
	 */
	public Plot2(String dummy, String title, String xLabel, String yLabel, float[] xValues, float[] yValues)
	{
		super(title, xLabel, yLabel, xValues, yValues);
	}

	/**
	 * Instantiates a new plot 2.
	 *
	 * @param title
	 *            the title
	 * @param xLabel
	 *            the x label
	 * @param yLabel
	 *            the y label
	 */
	public Plot2(String title, String xLabel, String yLabel)
	{
		super(title, xLabel, yLabel, (float[]) null, (float[]) null);
	}

	/**
	 * Instantiates a new plot 2.
	 *
	 * @param title
	 *            the title
	 * @param xLabel
	 *            the x label
	 * @param yLabel
	 *            the y label
	 * @param flags
	 *            the flags
	 */
	public Plot2(String title, String xLabel, String yLabel, int flags)
	{
		super(title, xLabel, yLabel, (float[]) null, (float[]) null, flags);
	}

	/**
	 * Instantiates a new plot 2.
	 *
	 * @param title
	 *            the title
	 * @param xLabel
	 *            the x label
	 * @param yLabel
	 *            the y label
	 * @param xValues
	 *            the x values
	 * @param yValues
	 *            the y values
	 * @param flags
	 *            the flags
	 */
	public Plot2(String title, String xLabel, String yLabel, float[] xValues, float[] yValues, int flags)
	{
		super(title, xLabel, yLabel, xValues, yValues, flags);
	}

	/**
	 * Instantiates a new plot 2.
	 *
	 * @param title
	 *            the title
	 * @param xLabel
	 *            the x label
	 * @param yLabel
	 *            the y label
	 * @param xValues
	 *            the x values
	 * @param yValues
	 *            the y values
	 * @param flags
	 *            the flags
	 */
	public Plot2(String title, String xLabel, String yLabel, double[] xValues, double[] yValues, int flags)
	{
		super(title, xLabel, yLabel, xValues, yValues, flags);
	}

	/**
	 * Adds a set of points to the plot or adds a curve if shape is set to LINE.
	 * <p>
	 * Support Bar plots by adding an extra point to draw a horizontal line and vertical line between points
	 *
	 * @param xValues
	 *            the x coordinates, or null. If null, integers starting at 0 will be used for x.
	 * @param yValues
	 *            the y coordinates (must not be null)
	 * @param yErrorBars
	 *            error bars in y, may be null
	 * @param shape
	 *            CIRCLE, X, BOX, TRIANGLE, CROSS, DOT, LINE, CONNECTED_CIRCLES, or BAR
	 * @param label
	 *            Label for this curve or set of points, used for a legend and for listing the plots
	 */
	@Override
	public void addPoints(float[] xValues, float[] yValues, float[] yErrorBars, int shape, String label)
	{
		// This only works if the addPoints super method ignores the BAR option but still store the values
		try
		{
			if (shape == BAR)
			{
				shape = Plot.LINE;

				if (xValues == null || xValues.length == 0)
				{
					xValues = new float[yValues.length];
					for (int i = 0; i < yValues.length; i++)
						xValues[i] = i;
				}

				final float[] x = createHistogramAxis(xValues);
				final float[] y = createHistogramValues(yValues);

				// No errors
				xValues = x;
				yValues = y;
			}
		}
		catch (final Throwable e)
		{ // Ignore
		}
		finally
		{
			super.addPoints(xValues, yValues, yErrorBars, shape, label);
		}
	}

	/**
	 * For the provided histogram x-axis bins, produce an x-axis for plotting. This functions doubles up the histogram
	 * x-positions to allow plotting a square line profile using the ImageJ plot command.
	 *
	 * @param histogramX
	 *            the histogram X
	 * @return the x-axis
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
	 * @return the y-axis
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

	/*
	 * (non-Javadoc)
	 *
	 * @see ij.gui.Plot#show()
	 */
	@Override
	public PlotWindow show()
	{
		// Override to show a PlotWindow2 object
		if (failedOverride)
			return super.show();

		try
		{
			ImagePlus imp = getImagePlus();
			if ((IJ.macroRunning() && IJ.getInstance() == null) || Interpreter.isBatchMode())
			{
				WindowManager.setTempCurrentImage(imp);
				final float[] x = getXValues();
				if (x != null)
				{
					imp.setProperty("XValues", x); // Allows values to be retrieved by
					imp.setProperty("YValues", getYValues()); // by Plot.getValues() macro function
				}
				Interpreter.addBatchModeImage(imp);
				return null;
			}
			if (imp != null)
			{
				final Window win = imp.getWindow();
				if (win instanceof PlotWindow && win.isVisible())
				{
					updateImage(); // show in existing window
					return (PlotWindow) win;
				}
			}
			final PlotWindow2 pw = new PlotWindow2(this);
			//if (imp == null)
			imp.setProperty(PROPERTY_KEY, null);
			imp = pw.getImagePlus();
			imp.setProperty(PROPERTY_KEY, this);
			if (IJ.isMacro()) // wait for plot to be displayed
				IJ.selectWindow(imp.getID());
			return pw;
		}
		catch (final Throwable e)
		{
			// Ignore
			failedOverride = true;
		}

		return super.show();
	}

	/**
	 * Gets the default min and max. This will be the full range of data unless the
	 * {@link #setLimits(double, double, double, double)} method has been called.
	 *
	 * @return the default min and max
	 */
	public double[] getDefaultMinAndMax()
	{
		try
		{
			super.getInitialMinAndMax();
			return defaultMinMax.clone();
		}
		catch (final Throwable e)
		{ // Ignore
		}
		return null;
	}

	/**
	 * Gets the current min and max.
	 *
	 * @return the current min and max
	 */
	public double[] getCurrentMinAndMax()
	{
		return currentMinMax;
	}
}
