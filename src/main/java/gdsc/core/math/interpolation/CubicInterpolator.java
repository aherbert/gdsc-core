package gdsc.core.math.interpolation;

/**
 * Cubic Interpolator using the Catmull-Rom spline.
 * <p>
 * Taken from http://www.paulinternet.nl/?page=bicubic.
 */
public class CubicInterpolator
{
	/**
	 * Gets the interpolated value.
	 *
	 * @param p
	 *            the value of the function at x=-1, x=0, x=1, and x=2
	 * @param x
	 *            the x (between 0 and 1)
	 * @return the value
	 */
	public static double getValue(double[] p, double x)
	{
		return p[1] + 0.5 * x * (p[2] - p[0] +
				x * (2.0 * p[0] - 5.0 * p[1] + 4.0 * p[2] - p[3] + x * (3.0 * (p[1] - p[2]) + p[3] - p[0])));
	}
}
