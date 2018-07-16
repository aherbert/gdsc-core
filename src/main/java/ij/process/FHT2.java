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
package ij.process;

import gdsc.core.utils.Maths;
import ij.ImageStack;

/**
 * Copy implementation of ij.process.FHT to increase the speed where possible.
 */
public class FHT2 extends FloatProcessor
{
	private boolean isFrequencyDomain;
	private float[] C;
	private float[] S;
	private int[] bitrev;
	private float[] tempArr;
	// Used for fast multiply operations
	private double[] h2e, h2o, mag;
	private int[] jj;

	/**
	 * Constructs a FHT object from an ImageProcessor. Byte, short and RGB images
	 * are converted to float. Float images are duplicated.
	 *
	 * @param ip
	 *            the image processor
	 * @throws IllegalArgumentException
	 *             If the processor is not square and a power of 2
	 */
	public FHT2(ImageProcessor ip) throws IllegalArgumentException
	{
		this(ip, false);
	}

	/**
	 * Constructs a FHT object from an ImageProcessor. Byte, short and RGB images
	 * are converted to float. Float images are duplicated.
	 *
	 * @param ip
	 *            the image processor
	 * @param isFrequencyDomain
	 *            True if in the frequency domain
	 * @throws IllegalArgumentException
	 *             If the processor is not square and a power of 2
	 */
	public FHT2(ImageProcessor ip, boolean isFrequencyDomain) throws IllegalArgumentException
	{
		super(ip.getWidth(), ip.getHeight(), (float[]) ((ip instanceof FloatProcessor) ? ip.duplicate().getPixels()
				: ip.convertToFloat().getPixels()), null);
		if (!powerOf2Size())
			throw new IllegalArgumentException("Image not power of 2 size or not square: " + width + "x" + height);
		this.isFrequencyDomain = isFrequencyDomain;
		resetRoi();
	}

	/**
	 * Constructs a FHT object.
	 *
	 * @param pixels
	 *            the pixels (this is not duplicated)
	 * @param maxN
	 *            the max N
	 * @param isFrequencyDomain
	 *            True if in the frequency domain
	 * @throws IllegalArgumentException
	 *             If the processor is not square and a power of 2
	 */
	public FHT2(float[] pixels, int maxN, boolean isFrequencyDomain) throws IllegalArgumentException
	{
		super(maxN, maxN, pixels);
		if (!powerOf2Size())
			throw new IllegalArgumentException("Image not power of 2 size or not square: " + width + "x" + height);
		this.isFrequencyDomain = isFrequencyDomain;
	}

	/**
	 * Gets the data.
	 *
	 * @return the data
	 */
	public float[] getData()
	{
		return (float[]) getPixels();
	}

	/**
	 * Returns true of this FHT contains a square image with a width that is a power of two.
	 *
	 * @return true, if successful
	 */
	public boolean powerOf2Size()
	{
		return width == height && isPowerOf2(width);
	}

	/**
	 * Performs a forward transform, converting this image into the frequency domain.
	 * The image contained in this FHT must be square and its width must be a power of 2.
	 */
	public void transform()
	{
		transform(false);
	}

	/**
	 * Performs an inverse transform, converting this image into the space domain.
	 * The image contained in this FHT must be square and its width must be a power of 2.
	 */
	public void inverseTransform()
	{
		transform(true);
	}

	private void transform(boolean inverse)
	{
		if (S == null)
			initializeTables(width);
		final float[] fht = (float[]) getPixels();
		rc2DFHT(fht, inverse, width);
		isFrequencyDomain = !inverse;
		resetFastOperations();
	}

	private void initializeTables(int maxN)
	{
		if (maxN > 0x40000000)
			throw new IllegalArgumentException("Too large for FHT:  " + maxN + " >2^30");
		makeSinCosTables(maxN);
		makeBitReverseTable(maxN);
		tempArr = new float[maxN];
	}

	/**
	 * Copy the computation tables from the initialised FHT.
	 *
	 * @param fht
	 *            the fht
	 */
	public void copyTables(FHT2 fht)
	{
		if (fht.S != null && fht.width == width)
		{
			// No need to clone as the tables are only read
			S = fht.S;
			C = fht.C;
			bitrev = fht.bitrev;
			// Initialise the temp array
			tempArr = new float[width];
		}
	}

	private void makeSinCosTables(int maxN)
	{
		final int n = maxN / 4;
		C = new float[n];
		S = new float[n];
		double theta = 0.0;
		final double dTheta = 2.0 * Math.PI / maxN;
		for (int i = 0; i < n; i++)
		{
			C[i] = (float) Math.cos(theta);
			S[i] = (float) Math.sin(theta);
			theta += dTheta;
		}
	}

	private void makeBitReverseTable(int maxN)
	{
		bitrev = new int[maxN];
		final int nLog2 = log2(maxN);
		for (int i = 0; i < maxN; i++)
			bitrev[i] = bitRevX(i, nLog2);
	}

	private static int bitRevX(int x, int bitlen)
	{
		int temp = 0;
		for (int i = 0; i <= bitlen; i++)
			if ((x & (1 << i)) != 0)
				temp |= (1 << (bitlen - i - 1));
		return temp;
	}

	/** Performs a 2D FHT (Fast Hartley Transform). */
	private void rc2DFHT(float[] x, boolean inverse, int maxN)
	{
		final float[] tmp = new float[maxN];
		for (int row = 0; row < maxN; row++)
			//dfht3(x, row * maxN, inverse, maxN);
			dfht3(x, row * maxN, inverse, maxN, tmp);
		transposeR(x, maxN);
		for (int row = 0; row < maxN; row++)
			//dfht3(x, row * maxN, inverse, maxN);
			dfht3(x, row * maxN, inverse, maxN, tmp);
		transposeR(x, maxN);

		int mRow, mCol;
		float A, B, C, D, E;
		for (int row = 0; row <= maxN / 2; row++)
			for (int col = 0; col <= maxN / 2; col++)
			{
				mRow = (maxN - row) % maxN;
				mCol = (maxN - col) % maxN;
				A = x[row * maxN + col]; //  see Bracewell, 'Fast 2D Hartley Transf.' IEEE Procs. 9/86
				B = x[mRow * maxN + col];
				C = x[row * maxN + mCol];
				D = x[mRow * maxN + mCol];
				E = ((A + D) - (B + C)) / 2;
				x[row * maxN + col] = A - E;
				x[mRow * maxN + col] = B + E;
				x[row * maxN + mCol] = C + E;
				x[mRow * maxN + mCol] = D - E;
			}
	}

	/**
	 * Performs an optimized 1D FHT of an array or part of an array.
	 *
	 * @param x
	 *            Input array; will be overwritten by the output in the range given by base and maxN.
	 * @param base
	 *            First index from where data of the input array should be read.
	 * @param inverse
	 *            True for inverse transform.
	 * @param maxN
	 *            Length of data that should be transformed; this must be always
	 *            the same for a given FHT object.
	 *            Note that all amplitudes in the output 'x' are multiplied by maxN.
	 */
	@SuppressWarnings("unused")
	private void dfht3(float[] x, int base, boolean inverse, int maxN)
	{
		int stage, gpNum, gpSize, numGps, Nlog2;
		int bfNum, numBfs;
		int Ad0, Ad1, Ad2, Ad3, Ad4, CSAd;
		float rt1, rt2, rt3, rt4;

		//if (S == null)
		//	initializeTables(maxN);
		Nlog2 = log2(maxN);
		bitRevRArr(x, base, maxN); //bitReverse the input array
		gpSize = 2; //first & second stages - do radix 4 butterflies once thru
		numGps = maxN / 4;
		for (gpNum = 0; gpNum < numGps; gpNum++)
		{
			Ad1 = gpNum * 4;
			Ad2 = Ad1 + 1;
			Ad3 = Ad1 + gpSize;
			Ad4 = Ad2 + gpSize;
			rt1 = x[base + Ad1] + x[base + Ad2]; // a + b
			rt2 = x[base + Ad1] - x[base + Ad2]; // a - b
			rt3 = x[base + Ad3] + x[base + Ad4]; // c + d
			rt4 = x[base + Ad3] - x[base + Ad4]; // c - d
			x[base + Ad1] = rt1 + rt3; // a + b + (c + d)
			x[base + Ad2] = rt2 + rt4; // a - b + (c - d)
			x[base + Ad3] = rt1 - rt3; // a + b - (c + d)
			x[base + Ad4] = rt2 - rt4; // a - b - (c - d)
		}

		if (Nlog2 > 2)
		{
			// third + stages computed here
			gpSize = 4;
			numBfs = 2;
			numGps = numGps / 2;
			//IJ.write("FFT: dfht3 "+Nlog2+" "+numGps+" "+numBfs);
			for (stage = 2; stage < Nlog2; stage++)
			{
				for (gpNum = 0; gpNum < numGps; gpNum++)
				{
					Ad0 = gpNum * gpSize * 2;
					Ad1 = Ad0; // 1st butterfly is different from others - no mults needed
					Ad2 = Ad1 + gpSize;
					Ad3 = Ad1 + gpSize / 2;
					Ad4 = Ad3 + gpSize;
					rt1 = x[base + Ad1];
					x[base + Ad1] = x[base + Ad1] + x[base + Ad2];
					x[base + Ad2] = rt1 - x[base + Ad2];
					rt1 = x[base + Ad3];
					x[base + Ad3] = x[base + Ad3] + x[base + Ad4];
					x[base + Ad4] = rt1 - x[base + Ad4];
					for (bfNum = 1; bfNum < numBfs; bfNum++)
					{
						// subsequent BF's dealt with together
						Ad1 = bfNum + Ad0;
						Ad2 = Ad1 + gpSize;
						Ad3 = gpSize - bfNum + Ad0;
						Ad4 = Ad3 + gpSize;

						CSAd = bfNum * numGps;
						rt1 = x[base + Ad2] * C[CSAd] + x[base + Ad4] * S[CSAd];
						rt2 = x[base + Ad4] * C[CSAd] - x[base + Ad2] * S[CSAd];

						x[base + Ad2] = x[base + Ad1] - rt1;
						x[base + Ad1] = x[base + Ad1] + rt1;
						x[base + Ad4] = x[base + Ad3] + rt2;
						x[base + Ad3] = x[base + Ad3] - rt2;

					} /* end bfNum loop */
				} /* end gpNum loop */
				gpSize *= 2;
				numBfs *= 2;
				numGps = numGps / 2;
			} /* end for all stages */
		} /* end if Nlog2 > 2 */

		if (inverse)
			for (int i = 0; i < maxN; i++)
				x[base + i] = x[base + i] / maxN;
	}

	/**
	 * Performs an optimized 1D FHT of an array or part of an array.
	 *
	 * @param x
	 *            Input array; will be overwritten by the output in the range given by base and maxN.
	 * @param base
	 *            First index from where data of the input array should be read.
	 * @param inverse
	 *            True for inverse transform.
	 * @param maxN
	 *            Length of data that should be transformed; this must be always
	 *            the same for a given FHT object.
	 *            Note that all amplitudes in the output 'x' are multiplied by maxN.
	 * @param x2
	 *            the working data buffer
	 */
	private void dfht3(float[] x, int base, boolean inverse, int maxN, float[] x2)
	{
		int stage, gpNum, gpSize, numGps, Nlog2;
		int bfNum, numBfs;
		int Ad0, Ad1, Ad2, Ad3, Ad4, CSAd;
		float rt1, rt2, rt3, rt4;

		// Extract data
		System.arraycopy(x, base, x2, 0, maxN);

		//if (S == null)
		//	initializeTables(maxN);
		Nlog2 = log2(maxN);
		bitRevRArr(x2, maxN); //bitReverse the input array
		gpSize = 2; //first & second stages - do radix 4 butterflies once thru
		numGps = maxN / 4;
		for (gpNum = 0; gpNum < numGps; gpNum++)
		{
			Ad1 = gpNum * 4;
			Ad2 = Ad1 + 1;
			Ad3 = Ad1 + gpSize;
			Ad4 = Ad2 + gpSize;
			rt1 = x2[Ad1] + x2[Ad2]; // a + b
			rt2 = x2[Ad1] - x2[Ad2]; // a - b
			rt3 = x2[Ad3] + x2[Ad4]; // c + d
			rt4 = x2[Ad3] - x2[Ad4]; // c - d
			x2[Ad1] = rt1 + rt3; // a + b + (c + d)
			x2[Ad2] = rt2 + rt4; // a - b + (c - d)
			x2[Ad3] = rt1 - rt3; // a + b - (c + d)
			x2[Ad4] = rt2 - rt4; // a - b - (c - d)
		}

		if (Nlog2 > 2)
		{
			// third + stages computed here
			gpSize = 4;
			numBfs = 2;
			numGps = numGps / 2;
			//IJ.write("FFT: dfht3 "+Nlog2+" "+numGps+" "+numBfs);
			for (stage = 2; stage < Nlog2; stage++)
			{
				for (gpNum = 0; gpNum < numGps; gpNum++)
				{
					Ad0 = gpNum * gpSize * 2;
					Ad1 = Ad0; // 1st butterfly is different from others - no mults needed
					Ad2 = Ad1 + gpSize;
					Ad3 = Ad1 + gpSize / 2;
					Ad4 = Ad3 + gpSize;
					rt1 = x2[Ad1];
					x2[Ad1] = x2[Ad1] + x2[Ad2];
					x2[Ad2] = rt1 - x2[Ad2];
					rt1 = x2[Ad3];
					x2[Ad3] = x2[Ad3] + x2[Ad4];
					x2[Ad4] = rt1 - x2[Ad4];
					for (bfNum = 1; bfNum < numBfs; bfNum++)
					{
						// subsequent BF's dealt with together
						Ad1 = bfNum + Ad0;
						Ad2 = Ad1 + gpSize;
						Ad3 = gpSize - bfNum + Ad0;
						Ad4 = Ad3 + gpSize;

						CSAd = bfNum * numGps;
						rt1 = x2[Ad2] * C[CSAd] + x2[Ad4] * S[CSAd];
						rt2 = x2[Ad4] * C[CSAd] - x2[Ad2] * S[CSAd];

						x2[Ad2] = x2[Ad1] - rt1;
						x2[Ad1] = x2[Ad1] + rt1;
						x2[Ad4] = x2[Ad3] + rt2;
						x2[Ad3] = x2[Ad3] - rt2;

					} /* end bfNum loop */
				} /* end gpNum loop */
				gpSize *= 2;
				numBfs *= 2;
				numGps = numGps / 2;
			} /* end for all stages */
		} /* end if Nlog2 > 2 */

		if (inverse)
			for (int i = 0; i < maxN; i++)
				x2[i] = x2[i] / maxN;

		// Copy back
		System.arraycopy(x2, 0, x, base, maxN);
	}

	/**
	 * Transpose R.
	 *
	 * @param x
	 *            the x
	 * @param maxN
	 *            the max N
	 */
	private static void transposeR(float[] x, int maxN)
	{
		for (int r = 0; r < maxN; r++)
			for (int c = r + 1, i = r * maxN + r + 1, ii = (r + 1) * maxN + r; c < maxN; c++, i++, ii += maxN)
			{
				final float rTemp = x[i];
				x[i] = x[ii];
				x[ii] = rTemp;
			}
	}

	private static int log2(int x)
	{
		int count = 31;
		while (!btst(x, count))
			count--;
		return count;
	}

	private static boolean btst(int x, int bit)
	{
		return ((x & (1 << bit)) != 0);
	}

	private void bitRevRArr(float[] x, int base, int maxN)
	{
		for (int i = 0; i < maxN; i++)
			tempArr[i] = x[base + bitrev[i]];
		for (int i = 0; i < maxN; i++)
			x[base + i] = tempArr[i];
	}

	private void bitRevRArr(float[] x, int maxN)
	{
		for (int i = 0; i < maxN; i++)
			tempArr[i] = x[bitrev[i]];
		for (int i = 0; i < maxN; i++)
			x[i] = tempArr[i];
	}

	/**
	 * Converts this FHT to a complex Fourier transform and returns it as a two slice stack.
	 * This has been adapted from the routine {@link #getComplexTransform()} to compute the real and imaginary
	 * parts of the transform at the same time.
	 *
	 * Author: Joachim Wesner, Alex Herbert
	 *
	 * @return the complex transform
	 */
	public ImageStack getComplexTransform()
	{
		if (!isFrequencyDomain)
			throw new IllegalArgumentException("Frequency domain image required");
		final int maxN = getWidth();
		final float[] fht = getData();
		final float[] re = new float[maxN * maxN];
		final float[] im = new float[maxN * maxN];
		for (int i = 0; i < maxN; i++)
			FHTboth(i, maxN, fht, re, im);
		swapQuadrants(new FloatProcessor(maxN, maxN, re, null));
		swapQuadrants(new FloatProcessor(maxN, maxN, im, null));
		final ImageStack stack = new ImageStack(maxN, maxN);
		stack.addSlice("Real", re);
		stack.addSlice("Imaginary", im);
		return stack;
	}

	/**
	 * Converts this FHT to a complex Fourier transform and returns it as a two slice stack.
	 * This has been adapted from the routine {@link #getComplexTransform()} to compute the real and imaginary
	 * parts of the transform at the same time.
	 *
	 * Author: Joachim Wesner, Alex Herbert
	 *
	 * @return the complex transform real and imaginary processors
	 */
	public FloatProcessor[] getComplexTransformProcessors()
	{
		if (!isFrequencyDomain)
			throw new IllegalArgumentException("Frequency domain image required");
		final int maxN = getWidth();
		final float[] fht = getData();
		final float[] re = new float[maxN * maxN];
		final float[] im = new float[maxN * maxN];
		for (int i = 0; i < maxN; i++)
			FHTboth(i, maxN, fht, re, im);
		final FloatProcessor[] out = new FloatProcessor[] { new FloatProcessor(maxN, maxN, re, null),
				new FloatProcessor(maxN, maxN, im, null) };
		swapQuadrants(out[0]);
		swapQuadrants(out[1]);
		return out;
	}

	/**
	 * FFT real & imaginary value of one row from 2D Hartley Transform.
	 * Author: Joachim Wesner
	 * Adapted by Alex Herbert to compute both together
	 *
	 * @param row
	 *            the row
	 * @param maxN
	 *            the max N
	 * @param fht
	 *            the fht
	 * @param real
	 *            the real
	 * @param imag
	 *            the imag
	 */
	private static void FHTboth(int row, int maxN, float[] fht, float[] real, float[] imag)
	{
		final int base = row * maxN;
		final int offs = ((maxN - row) % maxN) * maxN;
		for (int c = 0; c < maxN; c++)
		{
			final float a = fht[base + c];
			final float b = fht[offs + ((maxN - c) % maxN)];
			real[base + c] = (a + b) * 0.5f;
			imag[base + c] = (-a + b) * 0.5f;
		}
	}

	/**
	 * Swap quadrants 1 and 3 and 2 and 4 of the specified ImageProcessor
	 * so the power spectrum origin is at the center of the image.
	 *
	 * <pre>
	 * 	    2 1
	 * 	    3 4
	 * </pre>
	 *
	 * @param ip
	 *            the image
	 */
	public void swapQuadrants(ImageProcessor ip)
	{
		//IJ.log("swap");
		ImageProcessor t1, t2;
		final int size = ip.getWidth() / 2;
		ip.setRoi(size, 0, size, size);
		t1 = ip.crop();
		ip.setRoi(0, size, size, size);
		t2 = ip.crop();
		ip.insert(t1, 0, size);
		ip.insert(t2, size, 0);
		ip.setRoi(0, 0, size, size);
		t1 = ip.crop();
		ip.setRoi(size, size, size, size);
		t2 = ip.crop();
		ip.insert(t1, size, size);
		ip.insert(t2, 0, 0);
		ip.resetRoi();
	}

	/**
	 * Swap quadrants 1 and 3 and 2 and 4 of image
	 * so the power spectrum origin is at the center of the image.
	 *
	 * <pre>
	    2 1
	    3 4
	 * </pre>
	 */
	public void swapQuadrants()
	{
		swapQuadrants(this);
		resetFastOperations();
	}

	/**
	 * Swap quadrants 1 and 3 and 2 and 4 of the specified ImageProcessor
	 * so the power spectrum origin is at the center of the image.
	 *
	 * <pre>
	    2 1
	    3 4
	 * </pre>
	 *
	 * @param ip
	 *            The processor (must be an even square, i.e. width==height and width is even)
	 * @throws IllegalArgumentException
	 *             If not even dimensions
	 */
	public static void swapQuadrants(FloatProcessor ip) throws IllegalArgumentException
	{
		// This is a specialised version to allow using a float buffer and
		// optimised for even sized images

		final int ny = ip.getHeight();
		final int nx = ip.getWidth();
		if ((ny & 1) == 1 || (nx & 1) == 1)
			throw new IllegalArgumentException("Require even dimensions");

		final int ny_2 = ny / 2;
		final int nx_2 = nx / 2;

		final float[] tmp = new float[nx];
		final float[] a = (float[]) ip.getPixels();

		//@formatter:off
		// We swap: 0 <=> nx_2, 0 <=> ny_2
		// 1 <=> 3
		FHT2.swap(a, a, nx, nx_2,    0,    0, ny_2, nx_2, ny_2, tmp);
		// 2 <=> 4
		FHT2.swap(a, a, nx,    0,    0, nx_2, ny_2, nx_2, ny_2, tmp);
		//@formatter:on
	}

	/**
	 * Swap the rectangle pixel values from a with b.
	 * <p>
	 * No bounds checks are performed so use with care!
	 *
	 * @param a
	 *            the a pixels
	 * @param b
	 *            the b pixels (must match a.length)
	 * @param width
	 *            the width of each set of pixels
	 * @param ax
	 *            the x origin from a
	 * @param ay
	 *            the y origin from a
	 * @param bx
	 *            the x origin from b
	 * @param by
	 *            the b origin from b
	 * @param w
	 *            the width of the rectangle to swap
	 * @param h
	 *            the height of the rectangle to swap
	 * @param tmp
	 *            the tmp buffer (must be at least width in length)
	 */
	public static void swap(float[] a, float[] b, int width, int ax, int ay, int bx, int by, int w, int h, float[] tmp)
	{
		for (int ayy = ay + h, byy = by + h - 1; ayy-- > ay; byy--)
		{
			final int ai = ayy * width + ax;
			final int bi = byy * width + bx;
			System.arraycopy(a, ai, tmp, 0, w);
			System.arraycopy(b, bi, a, ai, w);
			System.arraycopy(tmp, 0, b, bi, w);
		}
	}

	/**
	 * Initialise fast operations for {@link #multiply(FHT2)} and {@link #conjugateMultiply(FHT2)}. This pre-computes
	 * the values needed for the operations.
	 * <p>
	 * Note: This initialises the FHT object for use as the argument to the operation, for example if a convolution
	 * kernel is to be applied to many FHT objects.
	 */
	public void initialiseFastMultiply()
	{
		if (h2e == null)
		{
			// Do this on new arrays for thread safety (i.e. concurrent initialisation)
			final float[] h2 = getData();
			final int maxN = getWidth();
			final double[] h2e = new double[h2.length];
			final double[] h2o = new double[h2e.length];
			final int[] jj = new int[h2e.length];
			for (int r = 0, rowMod = 0, i = 0; r < maxN; r++, rowMod = maxN - r)
				for (int c = 0, colMod = 0; c < maxN; c++, colMod = maxN - c, i++)
				{
					final int j = rowMod * maxN + colMod;
					h2e[i] = (h2[i] + h2[j]) / 2;
					h2o[i] = (h2[i] - h2[j]) / 2;
					jj[i] = j;
				}
			this.h2o = h2o;
			this.jj = jj;
			// Assign at the end for thread safety (i.e. concurrent initialisation)
			this.h2e = h2e;
		}
	}

	/**
	 * Initialise fast operations for {@link #multiply(FHT2)}, {@link #conjugateMultiply(FHT2)} and
	 * {@link #divide(FHT2)}. This pre-computes the values needed for the operations.
	 * <p>
	 * Note: This initialises the FHT object for use as the argument to the operation, for example if a deconvolution
	 * kernel is to be applied to many FHT objects.
	 */
	public void initialiseFastOperations()
	{
		initialiseFastMultiply();
		if (mag == null)
		{
			// Do this on new arrays for thread safety (i.e. concurrent initialisation)
			final double[] mag = new double[h2e.length];
			final float[] h2 = getData();
			for (int i = 0; i < h2.length; i++)
				// Note that pre-computed h2e and h2o are divided by 2 so we also
				// divide the magnitude by 2 to allow reuse of the pre-computed values
				// in the divide operation (which does not require h2e/2 and h2o/2)
				mag[i] = Math.max(1e-20, h2[i] * h2[i] + h2[jj[i]] * h2[jj[i]]) / 2;
			this.mag = mag;
		}
	}

	/**
	 * Checks if is initialised for fast multiply.
	 *
	 * @return true, if is fast multiply
	 */
	public boolean isFastMultiply()
	{
		return h2e != null;
	}

	/**
	 * Checks if is initialised for fast operations.
	 *
	 * @return true, if is fast operations
	 */
	public boolean isFastOperations()
	{
		return mag != null;
	}

	private void resetFastOperations()
	{
		h2e = null;
		h2o = null;
		jj = null;
		mag = null;
	}

	private FHT2 createFHTResult(float[] tmp, final int maxN)
	{
		final FHT2 result = new FHT2(tmp, maxN, true);
		// For faster inverse transform copy the tables
		result.copyTables(this);
		return result;
	}

	/**
	 * Returns the image resulting from the point by point Hartley multiplication
	 * of this image and the specified image. Both images are assumed to be in
	 * the frequency domain. Multiplication in the frequency domain is equivalent
	 * to convolution in the space domain.
	 *
	 * @param fht
	 *            the fht
	 * @return the fht2
	 */
	public FHT2 multiply(FHT2 fht)
	{
		return multiply(fht, null);
	}

	/**
	 * Returns the image resulting from the point by point Hartley multiplication
	 * of this image and the specified image. Both images are assumed to be in
	 * the frequency domain. Multiplication in the frequency domain is equivalent
	 * to convolution in the space domain.
	 *
	 * @param fht
	 *            the fht
	 * @param tmp
	 *            the buffer for the result (can be null)
	 * @return the fht2
	 */
	public FHT2 multiply(FHT2 fht, float[] tmp)
	{
		return (fht.isFastMultiply()) ? multiply(fht.h2e, fht.h2o, fht.jj, tmp) : multiply(fht.getData(), tmp);
	}

	/**
	 * Returns the image resulting from the point by point Hartley multiplication
	 * of this image and the specified image. Both images are assumed to be in
	 * the frequency domain. Multiplication in the frequency domain is equivalent
	 * to convolution in the space domain.
	 *
	 * @param h2
	 *            the second FHT
	 * @param tmp
	 *            the buffer for the result (can be null)
	 * @return the fht2
	 */
	private FHT2 multiply(float[] h2, float[] tmp)
	{
		final float[] h1 = getData();
		final int maxN = getWidth();
		if (tmp == null || tmp.length != h1.length)
			tmp = new float[h1.length];
		for (int r = 0, rowMod = 0, i = 0; r < maxN; r++, rowMod = maxN - r)
			//rowMod = (maxN - r) % maxN;
			for (int c = 0, colMod = 0; c < maxN; c++, colMod = maxN - c, i++)
			{
				//colMod = (maxN - c) % maxN;
				//h2e = (h2[r * maxN + c] + h2[rowMod * maxN + colMod]) / 2;
				//h2o = (h2[r * maxN + c] - h2[rowMod * maxN + colMod]) / 2;
				//tmp[r * maxN + c] = (float) (h1[r * maxN + c] * h2e + h1[rowMod * maxN + colMod] * h2o);

				// This is actually doing for 2D data stored as x[rows][columns]
				// x==column, y==row (this is row-major order as per JTransforms notation)
				// https://en.wikipedia.org/wiki/Discrete_Hartley_transform
				//h2e = (h2[r][c] + h2[N-r][N-c]) / 2;
				//h2o = (h2[r][c] - h2[N-r][N-c]) / 2;
				//tmp[r][c] = (float) (h1[r][c] * h2e + h1[N-r][N-c] * h2o);

				final int j = rowMod * maxN + colMod;
				final double h2e = (h2[i] + h2[j]) / 2;
				final double h2o = (h2[i] - h2[j]) / 2;
				tmp[i] = (float) (h1[i] * h2e + h1[j] * h2o);
			}
		return createFHTResult(tmp, maxN);
	}

	/**
	 * Returns the image resulting from the point by point Hartley multiplication
	 * of this image and the specified image. Both images are assumed to be in
	 * the frequency domain. Multiplication in the frequency domain is equivalent
	 * to convolution in the space domain.
	 *
	 * @param h2e
	 *            the pre-initialised h2e value
	 * @param h2o
	 *            the pre-initialised h2o value
	 * @param jj
	 *            the pre-initialised j index
	 * @param tmp
	 *            the buffer for the result (can be null)
	 * @return the fht2
	 */
	private FHT2 multiply(double[] h2e, double[] h2o, int[] jj, float[] tmp)
	{
		final float[] h1 = getData();
		if (tmp == null || tmp.length != h1.length)
			tmp = new float[h1.length];
		for (int i = 0; i < h1.length; i++)
			tmp[i] = (float) (h1[i] * h2e[i] + h1[jj[i]] * h2o[i]);
		return createFHTResult(tmp, width);
	}

	/**
	 * Returns the image resulting from the point by point Hartley conjugate
	 * multiplication of this image and the specified image. Both images are
	 * assumed to be in the frequency domain. Conjugate multiplication in
	 * the frequency domain is equivalent to correlation in the space domain.
	 *
	 * @param fht
	 *            the fht
	 * @return the fht2
	 */
	public FHT2 conjugateMultiply(FHT2 fht)
	{
		return conjugateMultiply(fht, null);
	}

	/**
	 * Returns the image resulting from the point by point Hartley conjugate
	 * multiplication of this image and the specified image. Both images are
	 * assumed to be in the frequency domain. Conjugate multiplication in
	 * the frequency domain is equivalent to correlation in the space domain.
	 *
	 * @param fht
	 *            the fht
	 * @param tmp
	 *            the buffer for the result (can be null)
	 * @return the fht2
	 */
	public FHT2 conjugateMultiply(FHT2 fht, float[] tmp)
	{
		return (fht.isFastMultiply()) ? conjugateMultiply(fht.h2e, fht.h2o, fht.jj, tmp)
				: conjugateMultiply(fht.getData(), tmp);
	}

	/**
	 * Returns the image resulting from the point by point Hartley conjugate
	 * multiplication of this image and the specified image. Both images are
	 * assumed to be in the frequency domain. Conjugate multiplication in
	 * the frequency domain is equivalent to correlation in the space domain.
	 *
	 * @param h2
	 *            the second FHT
	 * @param tmp
	 *            the buffer for the result (can be null)
	 * @return the fht2
	 */
	private FHT2 conjugateMultiply(float[] h2, float[] tmp)
	{
		final float[] h1 = getData();
		final int maxN = getWidth();
		if (tmp == null || tmp.length != h1.length)
			tmp = new float[h1.length];
		for (int r = 0, rowMod = 0, i = 0; r < maxN; r++, rowMod = maxN - r)
			//rowMod = (maxN - r) % maxN;
			for (int c = 0, colMod = 0; c < maxN; c++, colMod = maxN - c, i++)
			{
				//colMod = (maxN - c) % maxN;
				//h2e = (h2[r * maxN + c] + h2[rowMod * maxN + colMod]) / 2;
				//h2o = (h2[r * maxN + c] - h2[rowMod * maxN + colMod]) / 2;
				//tmp[r * maxN + c] = (float) (h1[r * maxN + c] * h2e - h1[rowMod * maxN + colMod] * h2o);
				final int j = rowMod * maxN + colMod;
				final double h2e = (h2[i] + h2[j]) / 2;
				final double h2o = (h2[i] - h2[j]) / 2;
				tmp[i] = (float) (h1[i] * h2e - h1[j] * h2o);
			}
		return createFHTResult(tmp, maxN);
	}

	/**
	 * Returns the image resulting from the point by point Hartley conjugate
	 * multiplication of this image and the specified image. Both images are
	 * assumed to be in the frequency domain. Conjugate multiplication in
	 * the frequency domain is equivalent to correlation in the space domain.
	 *
	 * @param h2e
	 *            the pre-initialised h2e value
	 * @param h2o
	 *            the pre-initialised h2o value
	 * @param jj
	 *            the pre-initialised j index
	 * @param tmp
	 *            the buffer for the result (can be null)
	 * @return the fht2
	 */
	private FHT2 conjugateMultiply(double[] h2e, double[] h2o, int[] jj, float[] tmp)
	{
		final float[] h1 = getData();
		if (tmp == null || tmp.length != h1.length)
			tmp = new float[h1.length];
		for (int i = 0; i < h1.length; i++)
			tmp[i] = (float) (h1[i] * h2e[i] - h1[jj[i]] * h2o[i]);
		return createFHTResult(tmp, width);
	}

	/**
	 * Returns the image resulting from the point by point Hartley division
	 * of this image by the specified image. Both images are assumed to be in
	 * the frequency domain. Division in the frequency domain is equivalent
	 * to deconvolution in the space domain.
	 *
	 * @param fht
	 *            the fht
	 * @return the fht2
	 */
	public FHT2 divide(FHT2 fht)
	{
		return divide(fht, null);
	}

	/**
	 * Returns the image resulting from the point by point Hartley division
	 * of this image by the specified image. Both images are assumed to be in
	 * the frequency domain. Division in the frequency domain is equivalent
	 * to deconvolution in the space domain.
	 *
	 * @param fht
	 *            the fht
	 * @param tmp
	 *            the buffer for the result (can be null)
	 * @return the fht2
	 */
	public FHT2 divide(FHT2 fht, float[] tmp)
	{
		return (fht.isFastOperations()) ? divide(fht.h2e, fht.h2o, fht.jj, fht.mag, tmp) : divide(fht.getData(), tmp);
	}

	/**
	 * Returns the image resulting from the point by point Hartley division
	 * of this image by the specified image. Both images are assumed to be in
	 * the frequency domain. Division in the frequency domain is equivalent
	 * to deconvolution in the space domain.
	 *
	 * @param h2
	 *            the second FHT
	 * @param tmp
	 *            the buffer for the result (can be null)
	 */
	private FHT2 divide(float[] h2, float[] tmp)
	{
		final float[] h1 = getData();
		final int maxN = getWidth();
		if (tmp == null || tmp.length != h1.length)
			tmp = new float[h1.length];
		for (int r = 0, rowMod = 0, i = 0; r < maxN; r++, rowMod = maxN - r)
			//rowMod = (maxN - r) % maxN;
			for (int c = 0, colMod = 0; c < maxN; c++, colMod = maxN - c, i++)
			{
				//colMod = (maxN - c) % maxN;
				//mag = h2[r * maxN + c] * h2[r * maxN + c] + h2[rowMod * maxN + colMod] * h2[rowMod * maxN + colMod];
				//if (mag < 1e-20)
				//	mag = 1e-20;
				//h2e = (h2[r * maxN + c] + h2[rowMod * maxN + colMod]);
				//h2o = (h2[r * maxN + c] - h2[rowMod * maxN + colMod]);
				//tmp[r * maxN + c] = (float) ((h1[r * maxN + c] * h2e - h1[rowMod * maxN + colMod] * h2o) / mag);
				final int j = rowMod * maxN + colMod;
				final double h2i = h2[i];
				final double h2j = h2[j];
				double mag = h2i * h2i + h2j * h2j;
				if (mag < 1e-20)
					mag = 1e-20;
				final double h2e = (h2i + h2j);
				final double h2o = (h2i - h2j);
				tmp[i] = (float) ((h1[i] * h2e - h1[j] * h2o) / mag);
			}
		return createFHTResult(tmp, maxN);
	}

	/**
	 * Returns the image resulting from the point by point Hartley division
	 * of this image by the specified image. Both images are assumed to be in
	 * the frequency domain. Division in the frequency domain is equivalent
	 * to deconvolution in the space domain.
	 *
	 * @param h2e
	 *            the pre-initialised h2e value
	 * @param h2o
	 *            the pre-initialised h2o value
	 * @param jj
	 *            the pre-initialised j index
	 * @param mag
	 *            the pre-initialised magnitude value
	 * @param tmp
	 *            the buffer for the result (can be null)
	 */
	private FHT2 divide(double[] h2e, double[] h2o, int[] jj, double[] mag, float[] tmp)
	{
		final float[] h1 = getData();
		if (tmp == null || tmp.length != h1.length)
			tmp = new float[h1.length];
		for (int i = 0; i < h1.length; i++)
			tmp[i] = (float) ((h1[i] * h2e[i] - h1[jj[i]] * h2o[i]) / mag[i]);
		return createFHTResult(tmp, width);
	}

	/**
	 * Returns a clone of this FHT.
	 *
	 * @return the copy
	 */
	public FHT2 getCopy()
	{
		final FHT2 fht = new FHT2(getData().clone(), width, isFrequencyDomain);
		fht.copyTables(this);
		return fht;
	}

	/**
	 * Checks if is power of 2 above zero (i.e. not 2^0 == 1).
	 *
	 * @param n
	 *            the n
	 * @return true, if is power of 2 above zero
	 */
	public static boolean isPowerOf2(int n)
	{
		//int i = 2;
		//while (i < n)
		//	i *= 2;
		//return i == n;
		// Avoid 2^0 returning true
		return n > 1 && Maths.isPow2(n);
	}

	/** Returns a string containing information about this FHT. */
	@Override
	public String toString()
	{
		return "FHT2, " + getWidth() + "x" + getHeight() + ", fd=" + isFrequencyDomain;
	}
}
