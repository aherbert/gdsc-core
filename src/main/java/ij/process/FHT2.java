package ij.process;

import ij.ImageStack;

/**
 * Extends the ImageJ FHT class to increase the speed where possible.
 */
public class FHT2 extends FHT
{
	public FHT2(ImageProcessor ip)
	{
		super(ip);
	}

	public FHT2()
	{
		super();
	}

	public FHT2(ImageProcessor floatProcessor, boolean isFrequencyDomain)
	{
		super(floatProcessor, isFrequencyDomain);
	}

	public FHT2(float[] data, int maxN, boolean isFrequencyDomain)
	{
		super(new FloatProcessor(maxN, maxN, data, null), isFrequencyDomain);
	}

	@Override
	void transposeR(float[] x, int maxN)
	{
		//		int r, c;
		//		float rTemp;
		//
		//		for (r = 0; r < maxN; r++)
		//		{
		//			for (c = r; c < maxN; c++)
		//			{
		//				if (r != c)
		//				{
		//					rTemp = x[r * maxN + c];
		//					x[r * maxN + c] = x[c * maxN + r];
		//					x[c * maxN + r] = rTemp;
		//				}
		//			}
		//		}

		//		for (int r = 0; r < maxN; r++)
		//		{
		//			for (int c = r + 1; c < maxN; c++)
		//			{
		//				final float rTemp = x[r * maxN + c];
		//				x[r * maxN + c] = x[c * maxN + r];
		//				x[c * maxN + r] = rTemp;
		//			}
		//		}

		for (int r = 0; r < maxN; r++)
		{
			for (int c = r + 1, i = r * maxN + r + 1, ii = (r + 1) * maxN + r; c < maxN; c++, i++, ii += maxN)
			{
				final float rTemp = x[i];
				x[i] = x[ii];
				x[ii] = rTemp;
			}
		}
	}

	/**
	 * Returns the image resulting from the point by point Hartley multiplication
	 * of this image and the specified image. Both images are assumed to be in
	 * the frequency domain. Multiplication in the frequency domain is equivalent
	 * to convolution in the space domain.
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
	 *            the tmp buffer for the result (can be null)
	 * @return the fht2
	 */
	public FHT2 multiply(FHT2 fht, float[] tmp)
	{
		int rowMod, colMod;
		double h2e, h2o;
		float[] h1 = (float[]) getPixels();
		float[] h2 = (float[]) fht.getPixels();
		final int maxN = getWidth();
		if (tmp == null || tmp.length != maxN * maxN)
			tmp = new float[maxN * maxN];
		for (int r = 0; r < maxN; r++)
		{
			rowMod = (maxN - r) % maxN;
			for (int c = 0; c < maxN; c++)
			{
				colMod = (maxN - c) % maxN;
				h2e = (h2[r * maxN + c] + h2[rowMod * maxN + colMod]) / 2;
				h2o = (h2[r * maxN + c] - h2[rowMod * maxN + colMod]) / 2;
				tmp[r * maxN + c] = (float) (h1[r * maxN + c] * h2e + h1[rowMod * maxN + colMod] * h2o);
			}
		}
		return new FHT2(new FloatProcessor(maxN, maxN, tmp, null), true);
	}

	/**
	 * Returns the image resulting from the point by point Hartley conjugate
	 * multiplication of this image and the specified image. Both images are
	 * assumed to be in the frequency domain. Conjugate multiplication in
	 * the frequency domain is equivalent to correlation in the space domain.
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
	 *            the tmp buffer for the result (can be null)
	 * @return the fht2
	 */
	public FHT2 conjugateMultiply(FHT2 fht, float[] tmp)
	{
		int rowMod, colMod;
		double h2e, h2o;
		float[] h1 = (float[]) getPixels();
		float[] h2 = (float[]) fht.getPixels();
		final int maxN = getWidth();
		if (tmp == null || tmp.length != maxN * maxN)
			tmp = new float[maxN * maxN];
		for (int r = 0; r < maxN; r++)
		{
			rowMod = (maxN - r) % maxN;
			for (int c = 0; c < maxN; c++)
			{
				colMod = (maxN - c) % maxN;
				h2e = (h2[r * maxN + c] + h2[rowMod * maxN + colMod]) / 2;
				h2o = (h2[r * maxN + c] - h2[rowMod * maxN + colMod]) / 2;
				tmp[r * maxN + c] = (float) (h1[r * maxN + c] * h2e - h1[rowMod * maxN + colMod] * h2o);
			}
		}
		return new FHT2(new FloatProcessor(maxN, maxN, tmp, null), true);
	}

	/**
	 * Converts this FHT to a complex Fourier transform and returns it as a two slice stack.
	 * Assumes this is in the frequency domain since that cannot be checked as the super-class isFrequencyDomain flag is
	 * hidden. This has been adapted from the routine {@link #getComplexTransform()} to compute the real and imaginary
	 * parts of the transform at the same time.
	 * 
	 * Author: Joachim Wesner, Alex Herbert
	 */
	public ImageStack getComplexTransform2()
	{
		//if (!isFrequencyDomain)
		//	throw new  IllegalArgumentException("Frequency domain image required");
		int maxN = getWidth();
		float[] fht = (float[]) getPixels();
		float[] re = new float[maxN * maxN];
		float[] im = new float[maxN * maxN];
		for (int i = 0; i < maxN; i++)
		{
			FHTboth(i, maxN, fht, re, im);
		}
		swapQuadrants(new FloatProcessor(maxN, maxN, re, null));
		swapQuadrants(new FloatProcessor(maxN, maxN, im, null));
		ImageStack stack = new ImageStack(maxN, maxN);
		stack.addSlice("Real", re);
		stack.addSlice("Imaginary", im);
		return stack;
	}

	/**
	 * FFT real & imaginary value of one row from 2D Hartley Transform.
	 * Author: Joachim Wesner
	 * Adapted by Alex Herbert to compute both together
	 */
	void FHTboth(int row, int maxN, float[] fht, float[] real, float[] imag)
	{
		int base = row * maxN;
		int offs = ((maxN - row) % maxN) * maxN;
		for (int c = 0; c < maxN; c++)
		{
			final float a = fht[base + c];
			final float b = fht[offs + ((maxN - c) % maxN)];
			real[base + c] = (a + b) * 0.5f;
			imag[base + c] = (-a + b) * 0.5f;
		}
	}

	@Override
	public void swapQuadrants()
	{
		swapQuadrants(this);
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
	 */
	public void swapQuadrants(FloatProcessor ip)
	{
		// This is a specialised version to allow reusing the float buffers and 
		// optimised for square images

		int width = ip.getWidth();
		float[] pixels = (float[]) ip.getPixels();
		int size = width / 2;
		float[] a = new float[size * size];
		//float[] b = new float[size * size];
		crop(pixels, width, a, size, 0, size);
		//crop(pixels, width, b, 0, size, size);
		//insert(pixels, width, b, size, 0, size);
		copy(pixels, width, 0, size, size, size, 0);
		insert(pixels, width, a, 0, size, size);
		crop(pixels, width, a, 0, 0, size);
		//crop(pixels, width, b, size, size, size);
		//insert(pixels, width, b, 0, 0, size);
		copy(pixels, width, size, size, size, 0, 0);
		insert(pixels, width, a, size, size, size);
	}

	private static void crop(float[] pixels, int width, float[] pixels2, int x, int y, int size)
	{
		for (int ys = y + size; ys-- > y;)
		{
			int offset = ys * width + x;
			int offset2 = (ys - y) * size;
			System.arraycopy(pixels, offset, pixels2, offset2, size);
			//for (int xs = 0; xs < size; xs++)
			//	pixels2[offset2++] = pixels[offset++];
		}
	}

	private static void insert(float[] pixels, int width, float[] pixels2, int x, int y, int size)
	{
		for (int ys = y + size; ys-- > y;)
		{
			int offset = ys * width + x;
			int offset2 = (ys - y) * size;
			System.arraycopy(pixels2, offset2, pixels, offset, size);
			//for (int xs = 0; xs < size; xs++)
			//	pixels[offset++] = pixels2[offset2++];
		}
	}

	private static void copy(float[] pixels, int width, int x, int y, int size, int x2, int y2)
	{
		for (int ys = y + size, ys2 = y2 + size - 1; ys-- > y; ys2--)
		//for (int ys = y, ys2 = y2; ys < y + size; ys++, ys2++)
		{
			int offset = ys * width + x;
			int offset2 = ys2 * width + x2;
			System.arraycopy(pixels, offset, pixels, offset2, size);
			//for (int xs = 0; xs < size; xs++)
			//	pixels[offset2++] = pixels[offset++];
		}
	}
}
