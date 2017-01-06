package ags.utils.dataStructures.trees.thirdGenKD;

/**
 *
 */
public class KdTreeND<T> extends KdTree<T>
{
	protected int dimensions;

	public KdTreeND(int dimensions)
	{
		this(dimensions, 24);
	}

	public KdTreeND(int dimensions, int bucketCapacity)
	{
		super(bucketCapacity);
		this.dimensions=dimensions;
	}

	@Override
	public int getDimensions()
	{
		return dimensions;
	}

	@Override
	protected KdNode<T> newInstance()
	{
		return new KdTreeND<T>(dimensions, bucketCapacity);
	}
}
