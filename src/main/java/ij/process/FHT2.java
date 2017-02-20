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
		int rowMod, colMod;
		double h2e, h2o;
		float[] h1 = (float[]) getPixels();
		float[] h2 = (float[]) fht.getPixels();
		final int maxN = getWidth();
		float[] tmp = new float[maxN * maxN];
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
		int rowMod, colMod;
		double h2e, h2o;
		float[] h1 = (float[]) getPixels();
		float[] h2 = (float[]) fht.getPixels();
		final int maxN = getWidth();
		float[] tmp = new float[maxN * maxN];
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
	 * hidden
	 * 
	 * Author: Joachim Wesner
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

	/**	 FFT real & imaginary value of one row from 2D Hartley Transform.
	*	Author: Joachim Wesner
	*   Adapted by Alex Herbert to compute both together 
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
}
