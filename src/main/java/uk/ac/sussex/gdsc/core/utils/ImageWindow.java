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
package uk.ac.sussex.gdsc.core.utils;

/**
 * Apply a window function to reduce edge artifacts
 */
public class ImageWindow
{
	/**
	 * The window function.
	 */
	public enum WindowFunction
	{
		/** The Hanning window function. */
		HANNING,
		/** The cosine window function. */
		COSINE,
		/** The Tukey window function. */
		TUKEY
	}

	// Allow cached window weights
	private double[] wx = null;
	private double[] wy = null;
	private final WindowFunction windowFunction = null;

	/**
	 * Apply a window function to reduce edge artifacts.
	 * <p>
	 * Applied as two 1-dimensional window functions. Faster than the nonseparable form but has direction dependent
	 * corners.
	 * <p>
	 * Instance method allows caching the weight matrices.
	 *
	 * @param image
	 *            the image
	 * @param maxx
	 *            the maxx
	 * @param maxy
	 *            the maxy
	 * @param windowFunction
	 *            the window function
	 * @return the image
	 */
	public float[] applySeperable(float[] image, final int maxx, final int maxy, WindowFunction windowFunction)
	{
		if (this.windowFunction != windowFunction || wx == null || wx.length != maxx || wy == null || wy.length != maxy)
			switch (windowFunction)
			{
				case HANNING:
					wx = hanning(maxx);
					wy = hanning(maxy);
					break;
				case COSINE:
					wx = cosine(maxx);
					wy = cosine(maxy);
					break;
				case TUKEY:
				default:
					wx = tukey(maxx);
					wy = tukey(maxy);
					break;
			}

		final float[] data = new float[image.length];

		for (int y = 0, i = 0; y < maxy; y++)
			for (int x = 0; x < maxx; x++, i++)
				data[i] = (float) (image[i] * wx[x] * wy[y]);

		return data;
	}

	/**
	 * Apply a window function to reduce edge artifacts.
	 * <p>
	 * Applied as two 1-dimensional window functions. Faster than the nonseparable form but has direction dependent
	 * corners.
	 *
	 * @param image
	 *            the image
	 * @param maxx
	 *            the maxx
	 * @param maxy
	 *            the maxy
	 * @param windowFunction
	 *            the window function
	 * @return the image
	 */
	public static float[] applyWindowSeparable(float[] image, final int maxx, final int maxy,
			WindowFunction windowFunction)
	{
		double[] wx = null;
		double[] wy = null;

		switch (windowFunction)
		{
			case HANNING:
				wx = hanning(maxx);
				wy = hanning(maxy);
				break;
			case COSINE:
				wx = cosine(maxx);
				wy = cosine(maxy);
				break;
			case TUKEY:
				wx = tukey(maxx);
				wy = tukey(maxy);
				break;
		}

		if (wx == null || wy == null)
			return image;

		final float[] data = new float[image.length];

		for (int y = 0, i = 0; y < maxy; y++)
			for (int x = 0; x < maxx; x++, i++)
				data[i] = (float) (image[i] * wx[x] * wy[y]);

		return data;
	}

	/**
	 * Apply a window function to reduce edge artifacts.
	 * <p>
	 * Applied as two 1-dimensional window functions. Faster than the nonseparable form but has direction dependent
	 * corners.
	 *
	 * @param image
	 *            the image
	 * @param maxx
	 *            the maxx
	 * @param maxy
	 *            the maxy
	 * @param wx
	 *            the window for x
	 * @param wy
	 *            the window for y
	 * @return the float[]
	 */
	public static float[] applyWindowSeparable(float[] image, final int maxx, final int maxy, double[] wx, double[] wy)
	{
		if (wx == null || wx.length != maxx || wy == null || wy.length != maxy)
			throw new IllegalArgumentException("Window function must match image dimensions");

		final float[] data = new float[image.length];

		for (int y = 0, i = 0; y < maxy; y++)
			for (int x = 0; x < maxx; x++, i++)
				data[i] = (float) (image[i] * wx[x] * wy[y]);

		return data;
	}

	/**
	 * Apply a window function to reduce edge artifacts.
	 * <p>
	 * Applied as two 1-dimensional window functions. Faster than the nonseparable form but has direction dependent
	 * corners.
	 *
	 * @param image
	 *            the image
	 * @param maxx
	 *            the maxx
	 * @param maxy
	 *            the maxy
	 * @param wx
	 *            the window for x
	 * @param wy
	 *            the window for y
	 */
	public static void applyWindowSeparableInPlace(float[] image, int maxx, int maxy, double[] wx, double[] wy)
	{
		if (wx == null || wx.length != maxx || wy == null || wy.length != maxy)
			throw new IllegalArgumentException("Window function must match image dimensions");

		for (int y = 0, i = 0; y < maxy; y++)
			for (int x = 0; x < maxx; x++, i++)
				image[i] *= wx[x] * wy[y];
	}

	/**
	 * Apply a window function to reduce edge artifacts
	 * <p>
	 * Applied as a nonseparable form.
	 *
	 * @param image
	 *            the image
	 * @param maxx
	 *            the maxx
	 * @param maxy
	 *            the maxy
	 * @param windowFunction
	 *            the window function
	 * @return the image
	 */
	public static float[] applyWindow(float[] image, final int maxx, final int maxy, WindowFunction windowFunction)
	{
		WindowMethod wf = null;
		switch (windowFunction)
		{
			case HANNING:
				wf = new Hanning();
				break;
			case COSINE:
				wf = new Cosine();
				break;
			case TUKEY:
				wf = new Tukey(ALPHA);
		}

		if (wf == null)
			return image;

		final float[] data = new float[image.length];

		final double cx = maxx * 0.5;
		final double cy = maxy * 0.5;
		final double maxDistance = Math.sqrt(maxx * maxx + maxy * maxy);

		// Precompute
		final double[] dx2 = new double[maxx];
		for (int x = 0; x < maxx; x++)
			dx2[x] = (x - cx) * (x - cx);

		for (int y = 0, i = 0; y < maxy; y++)
		{
			final double dy2 = (y - cy) * (y - cy);
			for (int x = 0; x < maxx; x++, i++)
			{
				final double distance = Math.sqrt(dx2[x] + dy2);
				final double w = wf.weight(0.5 - (distance / maxDistance));
				data[i] = (float) (image[i] * w);
			}
		}

		return data;
	}

	private static double ALPHA = 0.5;

	private interface WindowMethod
	{
		/**
		 * Return the weight for the window at a fraction of the distance from the edge of the
		 * window.
		 *
		 * @param fractionDistance
		 *            (range 0-1)
		 * @return the weight
		 */
		double weight(double fractionDistance);
	}

	private static class Hanning implements WindowMethod
	{
		@Override
		public double weight(double fractionDistance)
		{
			return 0.5 * (1 - Math.cos(Math.PI * 2 * fractionDistance));
		}
	}

	private static class Cosine implements WindowMethod
	{
		@Override
		public double weight(double fractionDistance)
		{
			return Math.sin(Math.PI * fractionDistance);
		}
	}

	private static class Tukey implements WindowMethod
	{
		final double alpha;
		final double a1, a2;

		public Tukey(double alpha)
		{
			this.alpha = alpha;
			a1 = alpha / 2;
			a2 = 1 - alpha / 2;
		}

		@Override
		public double weight(double fractionDistance)
		{
			if (fractionDistance < a1)
				return 0.5 * (1 + Math.cos(Math.PI * (2 * fractionDistance / alpha - 1)));
			if (fractionDistance > a2)
				return 0.5 * (1 + Math.cos(Math.PI * (2 * fractionDistance / alpha - 2 / alpha + 1)));
			return 1;
		}
	}

	private static double[] createWindow(WindowMethod wf, int N)
	{
		final double N_1 = N - 1;
		final double[] w = new double[N];
		// Assume symmetry
		final int middle = N / 2;
		for (int i = 0, j = N - 1; i <= middle; i++, j--)
			w[i] = w[j] = wf.weight(i / N_1);
		return w;
	}

	/**
	 * Create a Hanning window.
	 *
	 * @param N
	 *            the size of the window
	 * @return the window weighting
	 */
	public static double[] hanning(int N)
	{
		return createWindow(new Hanning(), N);
	}

	/**
	 * Create a Cosine window.
	 *
	 * @param N
	 *            the size of the window
	 * @return the window weighting
	 */
	public static double[] cosine(int N)
	{
		return createWindow(new Cosine(), N);
	}

	/**
	 * Create a Tukey (Tapered Cosine) window.
	 * <p>
	 * Alpha controls the distance from the edge of the window to the centre to apply the weight.
	 * A value of 1 will return a Hanning window, 0 will return a rectangular window.
	 *
	 * @param N
	 *            the size of the window
	 * @param alpha
	 *            the alpha parameter
	 * @return the window weighting
	 * @throws IllegalArgumentException
	 *             If alpha is not in the range 0-1
	 */
	public static double[] tukey(int N, double alpha)
	{
		if (alpha < 0 || alpha > 1)
			throw new IllegalArgumentException("Alpha must be in the range 0-1");
		return createWindow(new Tukey(alpha), N);
	}

	/**
	 * Create a Tukey (Tapered Cosine) window using the default alpha of 0.5.
	 *
	 * @param N
	 *            the size of the window
	 * @return the window weighting
	 */
	public static double[] tukey(int N)
	{
		return createWindow(new Tukey(ALPHA), N);
	}

	/**
	 * Create a Tukey (Tapered Cosine) window using the specified edge.
	 * <p>
	 * Edge controls the distance from the edge of the window to the centre to apply the weight.
	 * A 0 will return a rectangular window.
	 *
	 * @param N
	 *            the size of the window
	 * @param edge
	 *            the size of the window edge
	 * @return the window weighting
	 */
	public static double[] tukeyEdge(int N, int edge)
	{
		return tukey(N, tukeyAlpha(N, edge));
	}

	/**
	 * Create the alpha for the Tukey window using the desired size of the edge window.
	 *
	 * @param edge
	 *            the size of the window edge
	 * @param N
	 *            the size of the window
	 * @return the alpha (range 0-1)
	 */
	public static double tukeyAlpha(int N, int edge)
	{
		if (edge <= 0 || N < 1)
			return 0;
		return Math.min((2.0 * edge) / (N - 1), 1.0);
	}

	/**
	 * Create a window function.
	 *
	 * @param windowFunction
	 *            the window function
	 * @param N
	 *            the size of the window
	 * @return the window weighting
	 */
	public static double[] createWindow(WindowFunction windowFunction, int N)
	{
		switch (windowFunction)
		{
			case HANNING:
				return hanning(N);
			case COSINE:
				return cosine(N);
			case TUKEY:
			default:
				return tukey(N);
		}
	}
}
