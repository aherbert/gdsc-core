package ags.utils.dataStructures.trees.secondGenKD;

/**
 * Class for tracking up to 'size' closest values
 */
public class FloatResultHeap
{
	final Object[] data;
	final float[] distance;
	private final int size;
	int values;
	public Object removedData;
	public float removedDist;

	public FloatResultHeap(int size)
	{
		this.data = new Object[size];
		this.distance = new float[size];
		this.size = size;
		this.values = 0;
	}

	public void addValue(float dist, Object value)
	{
		// If there is still room in the heap
		if (values < size)
		{
			// Insert new value at the end
			data[values] = value;
			distance[values] = dist;
			upHeapify(values);
			values++;
		}
		// If there is no room left in the heap, and the new entry is lower
		// than the max entry
		else if (dist < distance[0])
		{
			// Replace the max entry with the new entry
			data[0] = value;
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

		removedData = data[0];
		removedDist = distance[0];
		values--;
		data[0] = data[values];
		distance[0] = distance[values];
		downHeapify(0);
	}

	private void upHeapify(int c)
	{
		for (int p = (c - 1) / 2; c != 0 && distance[c] > distance[p]; c = p, p = (c - 1) / 2)
		{
			Object pData = data[p];
			float pDist = distance[p];
			data[p] = data[c];
			distance[p] = distance[c];
			data[c] = pData;
			distance[c] = pDist;
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
				Object pData = data[p];
				float pDist = distance[p];
				data[p] = data[c];
				distance[p] = distance[c];
				data[c] = pData;
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
}