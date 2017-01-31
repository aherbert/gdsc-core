package ags.utils.dataStructures.trees.secondGenKD;

/**
 * Class for tracking up to 'size' closest values
 */
public class FloatHeap
{
	final float[] distance;
	private final int size;
	int values;
	public float removedDist;

	public FloatHeap(int size)
	{
		this.distance = new float[size];
		this.size = size;
		this.values = 0;
	}

	public void addValue(float dist)
	{
		// If there is still room in the heap
		if (values < size)
		{
			// Insert new value at the end
			distance[values] = dist;
			upHeapify(values);
			values++;
		}
		// If there is no room left in the heap, and the new entry is lower
		// than the max entry
		else if (dist < distance[0])
		{
			// Replace the max entry with the new entry
			distance[0] = dist;
			downHeapify(0);
		}
	}

	public void removeLargest()
	{
		if (values == 0)
		{
			throw new IllegalStateException();
		}

		removedDist = distance[0];
		values--;
		distance[0] = distance[values];
		downHeapify(0);
	}

	private void upHeapify(int c)
	{
		while (c > 0)
		{
			final int p = (c - 1) >>> 1;
			if (distance[c] > distance[p])
			{
				float pDist = distance[p];
				distance[p] = distance[c];
				distance[c] = pDist;
				c = p;
			}
			else
			{
				break;
			}
		}
	}

	private void downHeapify(int p)
	{
		for (int c = p * 2 + 1; c < values; p = c, c = p * 2 + 1)
		{
			if (c + 1 < values && distance[c] < distance[c + 1])
			{
				c++;
			}
			if (distance[p] < distance[c])
			{
				// Swap the points
				float pDist = distance[p];
				distance[p] = distance[c];
				distance[c] = pDist;
			}
			else
			{
				break;
			}
		}
	}

	public float getMaxDist()
	{
		if (values < size)
		{
			return Float.POSITIVE_INFINITY;
		}
		return distance[0];
	}
	
	public float[] values()
	{
		return distance;
	}
}