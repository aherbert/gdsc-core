package ags.utils.dataStructures.trees.thirdGenKD;

class KdNodeND<T> extends KdNode<T>
{
	protected int dimensions;

	protected KdNodeND(int dimensions, int bucketCapacity)
	{
		super(bucketCapacity);
		this.dimensions = dimensions;
	}

	@Override
	public int getDimensions()
	{
		return dimensions;
	}

	@Override
	protected KdNode<T> newInstance()
	{
		return new KdNodeND<T>(dimensions, bucketCapacity);
	}
}
