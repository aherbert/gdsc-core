package gdsc.core.math.interpolation;

/**
 * Tricubic Interpolator using the Catmull-Rom spline.
 * <p>
 * Taken from http://www.paulinternet.nl/?page=bicubic.
 */
public class TricubicInterpolator extends BicubicInterpolator
{
	private double[] arr = new double[4];

	/**
	 * Gets the interpolated value.
	 *
	 * @param p
	 *            the value of the function at x=-1 to x=2; y=-1 to y=2; and z=-1 to z=2
	 * @param x
	 *            the x (between 0 and 1)
	 * @param y
	 *            the y (between 0 and 1)
	 * @param z
	 *            the z (between 0 and 1)
	 * @return the value
	 */
	public double getValue(double[][][] p, double x, double y, double z)
	{
		arr[0] = getValue(p[0], y, z);
		arr[1] = getValue(p[1], y, z);
		arr[2] = getValue(p[2], y, z);
		arr[3] = getValue(p[3], y, z);
		return getValue(arr, x);
	}
}
