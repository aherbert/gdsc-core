package ags.utils.dataStructures.trees.thirdGenKD;

/**
 * Optimised distance function for 2D data
 * 
 * @author Alex Herbert
 */
public class SquareEuclideanDistanceFunction2D implements DistanceFunction
{
	public double distance(double[] p1, double[] p2)
	{
		double dx = p1[0] - p2[0];
		double dy = p1[1] - p2[1];
		return dx * dx + dy * dy;
	}

	public double distanceToRect(double[] point, double[] min, double[] max)
	{
		double dx = (point[0] > max[0]) ? point[0] - max[0] : (point[0] < min[0]) ? point[0] - min[0] : 0;
		double dy = (point[1] > max[1]) ? point[1] - max[1] : (point[1] < min[1]) ? point[1] - min[1] : 0;
		return dx * dx + dy * dy;
	}
}