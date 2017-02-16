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
	/** Draw a bar plot */
	public static final int BAR = 999;

	private static boolean failedOverride = false;

	public Plot2(String title, String xLabel, String yLabel, float[] xValues, float[] yValues)
	{
		super(title, xLabel, yLabel, xValues, yValues);
	}

	public Plot2(String title, String xLabel, String yLabel, double[] xValues, double[] yValues)
	{
		super(title, xLabel, yLabel, xValues, yValues);
	}

	public Plot2(String dummy, String title, String xLabel, String yLabel, float[] xValues, float[] yValues)
	{
		super(title, xLabel, yLabel, xValues, yValues);
	}

	public Plot2(String title, String xLabel, String yLabel)
	{
		super(title, xLabel, yLabel, (float[]) null, (float[]) null);
	}

	public Plot2(String title, String xLabel, String yLabel, int flags)
	{
		super(title, xLabel, yLabel, (float[]) null, (float[]) null, flags);
	}

	public Plot2(String title, String xLabel, String yLabel, float[] xValues, float[] yValues, int flags)
	{
		super(title, xLabel, yLabel, xValues, yValues, flags);
	}

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

				float[] x = createHistogramAxis(xValues);
				float[] y = createHistogramValues(yValues);

				// No errors
				xValues = x;
				yValues = y;
			}
		}
		catch (Throwable e)
		{
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
	 * @return
	 */
	public static float[] createHistogramAxis(float[] histogramX)
	{
		float[] axis = new float[histogramX.length * 2 + 2];
		int index = 0;
		for (int i = 0; i < histogramX.length; ++i)
		{
			axis[index++] = histogramX[i];
			axis[index++] = histogramX[i];
		}
		if (histogramX.length > 0)
		{
			float dx = (histogramX.length == 1) ? 1 : (histogramX[1] - histogramX[0]);
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
	 * @return
	 */
	public static float[] createHistogramValues(float[] histogramY)
	{
		float[] axis = new float[histogramY.length * 2 + 2];

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
				float[] x = getXValues();
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
				Window win = imp.getWindow();
				if (win instanceof PlotWindow && win.isVisible())
				{
					updateImage(); // show in existing window
					return (PlotWindow) win;
				}
			}
			PlotWindow2 pw = new PlotWindow2(this);
			//if (imp == null)
			imp.setProperty(PROPERTY_KEY, null);
			imp = pw.getImagePlus();
			imp.setProperty(PROPERTY_KEY, this);
			if (IJ.isMacro() && imp != null) // wait for plot to be displayed
				IJ.selectWindow(imp.getID());
			return pw;
		}
		catch (Throwable e)
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
		catch (Throwable e)
		{
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
