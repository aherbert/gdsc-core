package ags.utils.dataStructures.trees.thirdGenKD;

/**
 *
 */
public class KdTree2D<T> extends KdTree<T>
{
	public KdTree2D()
	{
		this(24);
	}

	public KdTree2D(int bucketCapacity)
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
