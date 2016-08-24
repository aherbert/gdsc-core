package ij.gui;

import java.awt.Color;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.WindowManager;
import ij.macro.Interpreter;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

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
			Method m = Plot.class.getDeclaredMethod("draw");
			m.setAccessible(true);
			m.invoke(this);

			ImageProcessor ip = getProcessor();
			if (Prefs.useInvertingLut && (ip instanceof ByteProcessor) && !Interpreter.isBatchMode() &&
					IJ.getInstance() != null)
			{
				ip.invertLut();
				ip.invert();
			}
			if ((IJ.macroRunning() && IJ.getInstance() == null) || Interpreter.isBatchMode())
			{
				String title = "";
				try
				{
					Field f = Plot.class.getDeclaredField("title");
					f.setAccessible(true);
					title = f.get(this).toString();
				}
				catch (Throwable e)
				{
					// Ignore
				}
				ImagePlus imp = new ImagePlus(title, ip);
				WindowManager.setTempCurrentImage(imp);
				imp.setProperty("XValues", getXValues()); //Allows values to be retrieved by 
				imp.setProperty("YValues", getYValues()); // by Plot.getValues() macro function
				Interpreter.addBatchModeImage(imp);
				return null;
			}
			ImageWindow.centerNextImage();
			// This may throw an IllegalAccessError on some platforms
			PlotWindow2 pw = new PlotWindow2(this);
			ImagePlus imp = pw.getImagePlus();
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see ij.gui.Plot#setColor(java.awt.Color)
	 */
	@Override
	public void setColor(Color c)
	{
		// If the user sets a custom colour we must make sure the image processor is initialised and the plot
		// drawn. This is getProcessor() sets the colour to black and we use getProcessor() to get 
		// the processor in the addPoints(...) method.
		getProcessor();
		super.setColor(c);
	}
}
