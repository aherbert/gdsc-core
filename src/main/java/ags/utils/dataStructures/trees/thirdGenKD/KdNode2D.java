package ags.utils.dataStructures.trees.thirdGenKD;

class KdNode2D<T> extends KdNode<T>
{
	protected KdNode2D(int bucketCapacity)
	{
		super(bucketCapacity);
	}

	@Override
	public int getDimensions()
	{
		return 2;
	}

	@Override
	protected KdNode<T> newInstance()
	{
		return new KdNode2D<T>(bucketCapacity);
	}
}
