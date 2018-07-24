/*
 * Copyright 2009 Rednaxela
 *
 * Modifications to the code have been made by Alex Herbert for a smaller 
 * memory footprint and optimised 2D processing for use with image data
 * as part of the Genome Damage and Stability Centre ImageJ Core Package.
 * 
 * This software is provided 'as-is', without any express or implied
 * warranty. In no event will the authors be held liable for any damages
 * arising from the use of this software.
 *
 * Permission is granted to anyone to use this software for any purpose,
 * including commercial applications, and to alter it and redistribute it
 * freely, subject to the following restrictions:
 *
 *    1. The origin of this software must not be misrepresented; you must not
 *    claim that you wrote the original software. If you use this software
 *    in a product, an acknowledgment in the product documentation would be
 *    appreciated but is not required.
 *
 *    2. This notice may not be removed or altered from any source
 *    distribution.
 */
package uk.ac.sussex.gdsc.core.ags.utils.dataStructures.trees.thirdGenKD;

/**
 * The Class KdTree2D.
 *
 * @param <T> the generic type
 */
public class KdTree2D<T> extends KdTree<T>
{
	/**
	 * Instantiates a new kd tree 2 D.
	 */
	public KdTree2D()
	{
		this(24);
	}

	/**
	 * Instantiates a new kd tree 2 D.
	 *
	 * @param bucketCapacity the bucket capacity
	 */
	public KdTree2D(int bucketCapacity)
	{
		super(bucketCapacity);
	}

	/* (non-Javadoc)
	 * @see ags.utils.dataStructures.trees.thirdGenKD.KdNode#getDimensions()
	 */
	@Override
	public int getDimensions()
	{
		return 2;
	}

	/* (non-Javadoc)
	 * @see ags.utils.dataStructures.trees.thirdGenKD.KdNode#newInstance()
	 */
	@Override
	protected KdNode<T> newInstance()
	{
		return new KdNode2D<>(bucketCapacity);
	}
}
