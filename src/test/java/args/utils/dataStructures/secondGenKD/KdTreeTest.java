package args.utils.dataStructures.secondGenKD;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;

/*----------------------------------------------------------------------------- 
 * GDSC ImageJ Software
 * 
 * Copyright (C) 2017 Alex Herbert
 * Genome Damage and Stability Centre
 * University of Sussex, UK
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *---------------------------------------------------------------------------*/

import org.junit.Test;

import ags.utils.dataStructures.MaxHeap;
import ags.utils.dataStructures.trees.secondGenKD.KdTree.Entry;
import ags.utils.dataStructures.trees.thirdGenKD.DistanceFunction;
import ags.utils.dataStructures.trees.thirdGenKD.SquareEuclideanDistanceFunction2D;
import gdsc.core.test.BaseTimingTask;
import gdsc.core.test.TimingService;
import gdsc.core.utils.Maths;
import gdsc.core.utils.PartialSort;
import gdsc.core.utils.Random;
import gdsc.core.utils.SimpleArrayUtils;

public class KdTreeTest
{
	private gdsc.core.utils.Random rand = new Random(30051977);

	int size = 256;
	int[] N = new int[] { 100, 200, 400, 2000 };
	int[] K = new int[] { 2, 4, 8, 16 };

	@Test
	public void canComputeKNNSecondGen()
	{
		for (int n : N)
		{
			double[][] data = createData(size, n, false);

			// Create the KDtree
			ags.utils.dataStructures.trees.secondGenKD.KdTree<Object> tree = new ags.utils.dataStructures.trees.secondGenKD.KdTree.SqrEuclid2D<Object>(
					null);
			for (double[] location : data)
				tree.addPoint(location, null);

			// Compute all-vs-all distances
			double[][] d = new double[n][n];
			for (int i = 0; i < n; i++)
				for (int j = i + 1; j < n; j++)
				{
					d[i][j] = d[j][i] = Maths.distance2(data[i][0], data[i][1], data[j][0], data[j][1]);
				}

			// For each point
			for (int i = 0; i < n; i++)
			{
				// Get the sorted distances to neighbours
				double[] d2 = PartialSort.bottom(d[i], K[K.length - 1]);

				// Get the KNN
				for (int k : K)
				{
					List<Entry<Object>> neighbours = tree.nearestNeighbor(data[i], k, true);
					double[] observed = new double[k];
					// Neighbours will be in reverse order
					int j = k;
					for (Entry<Object> e : neighbours)
					{
						observed[--j] = e.distance;
					}

					double[] expected = Arrays.copyOf(d2, k);
					//System.out.printf("[%d] k=%d  E=%s, O=%s\n", i, k, Arrays.toString(expected),
					//		Arrays.toString(observed));

					Assert.assertArrayEquals(expected, observed, 0);
				}
			}
		}
	}

	@Test
	public void canComputeKNNSecondGenWithDuplicates()
	{
		for (int n : N)
		{
			double[][] data = createData(size, n, true);

			// Create the KDtree
			ags.utils.dataStructures.trees.secondGenKD.KdTree<Object> tree = new ags.utils.dataStructures.trees.secondGenKD.KdTree.SqrEuclid2D<Object>(
					null);
			for (double[] location : data)
				tree.addPoint(location, null);

			// Compute all-vs-all distances
			double[][] d = new double[n][n];
			for (int i = 0; i < n; i++)
				for (int j = i + 1; j < n; j++)
				{
					d[i][j] = d[j][i] = Maths.distance2(data[i][0], data[i][1], data[j][0], data[j][1]);
				}

			// For each point
			for (int i = 0; i < n; i++)
			{
				// Get the sorted distances to neighbours
				double[] d2 = PartialSort.bottom(d[i], K[K.length - 1]);

				// Get the KNN
				for (int k : K)
				{
					List<Entry<Object>> neighbours = tree.nearestNeighbor(data[i], k, true);
					double[] observed = new double[k];
					// Neighbours will be in reverse order
					int j = k;
					for (Entry<Object> e : neighbours)
					{
						observed[--j] = e.distance;
					}

					double[] expected = Arrays.copyOf(d2, k);
					//System.out.printf("[%d] k=%d  E=%s, O=%s\n", i, k, Arrays.toString(expected),
					//		Arrays.toString(observed));

					Assert.assertArrayEquals(expected, observed, 0);
				}
			}
		}
	}

	@Test
	public void canComputeKNNDistanceSecondGen()
	{
		for (int n : N)
		{
			double[][] data = createData(size, n, true);

			// Create the KDtree
			ags.utils.dataStructures.trees.secondGenKD.KdTree<Object> tree = new ags.utils.dataStructures.trees.secondGenKD.KdTree.SqrEuclid2D<Object>(
					null);
			for (double[] location : data)
				tree.addPoint(location, null);

			// Compute all-vs-all distances
			double[][] d = new double[n][n];
			for (int i = 0; i < n; i++)
				for (int j = i + 1; j < n; j++)
				{
					d[i][j] = d[j][i] = Maths.distance2(data[i][0], data[i][1], data[j][0], data[j][1]);
				}

			// For each point
			for (int i = 0; i < n; i++)
			{
				// Get the sorted distances to neighbours
				double[] d2 = PartialSort.bottom(d[i], K[K.length - 1]);

				// Get the KNN
				for (int k : K)
				{
					List<Entry<Object>> neighbours = tree.nearestNeighbor(data[i], k, false);

					Assert.assertEquals(d2[k - 1], neighbours.get(0).distance, 0);
				}
			}
		}
	}

	@Test
	public void canComputeKNNThirdGen()
	{
		for (int n : N)
		{
			double[][] data = createData(size, n, false);

			// Create the KDtree
			ags.utils.dataStructures.trees.thirdGenKD.KdTree2D<Object> tree = new ags.utils.dataStructures.trees.thirdGenKD.KdTree2D<Object>(
					2);
			for (double[] location : data)
				tree.addPoint(location, null);

			// Compute all-vs-all distances
			double[][] d = new double[n][n];
			for (int i = 0; i < n; i++)
				for (int j = i + 1; j < n; j++)
				{
					d[i][j] = d[j][i] = Maths.distance2(data[i][0], data[i][1], data[j][0], data[j][1]);
				}

			// For each point
			for (int i = 0; i < n; i++)
			{
				// Get the sorted distances to neighbours
				double[] d2 = PartialSort.bottom(d[i], K[K.length - 1]);

				// Get the KNN
				for (int k : K)
				{
					MaxHeap<Object> neighbours = tree.findNearestNeighbors(data[i], k,
							new SquareEuclideanDistanceFunction2D());
					double[] observed = new double[k];
					// Neighbours will be in reverse order
					int j = k;
					while (neighbours.size() > 0)
					{
						observed[--j] = neighbours.getMaxKey();
						neighbours.removeMax();
					}

					double[] expected = Arrays.copyOf(d2, k);
					//System.out.printf("[%d] k=%d  E=%s, O=%s\n", i, k, Arrays.toString(expected),
					//		Arrays.toString(observed));

					Assert.assertArrayEquals(expected, observed, 0);
				}
			}
		}
	}

	@Test
	public void canComputeKNNThirdGenWithDuplicates()
	{
		for (int n : N)
		{
			double[][] data = createData(size, n, true);

			// Create the KDtree
			ags.utils.dataStructures.trees.thirdGenKD.KdTree2D<Object> tree = new ags.utils.dataStructures.trees.thirdGenKD.KdTree2D<Object>(
					2);
			for (double[] location : data)
				tree.addPoint(location, null);

			// Compute all-vs-all distances
			double[][] d = new double[n][n];
			for (int i = 0; i < n; i++)
				for (int j = i + 1; j < n; j++)
				{
					d[i][j] = d[j][i] = Maths.distance2(data[i][0], data[i][1], data[j][0], data[j][1]);
				}

			// For each point
			for (int i = 0; i < n; i++)
			{
				// Get the sorted distances to neighbours
				double[] d2 = PartialSort.bottom(d[i], K[K.length - 1]);

				// Get the KNN
				for (int k : K)
				{
					MaxHeap<Object> neighbours = tree.findNearestNeighbors(data[i], k,
							new SquareEuclideanDistanceFunction2D());
					double[] observed = new double[k];
					// Neighbours will be in reverse order
					int j = k;
					while (neighbours.size() > 0)
					{
						observed[--j] = neighbours.getMaxKey();
						neighbours.removeMax();
					}

					double[] expected = Arrays.copyOf(d2, k);
					//System.out.printf("[%d] k=%d  E=%s, O=%s\n", i, k, Arrays.toString(expected),
					//		Arrays.toString(observed));

					Assert.assertArrayEquals(expected, observed, 0);
				}
			}
		}
	}

	@Test
	public void canComputeKNNDistanceThirdGen()
	{
		for (int n : N)
		{
			double[][] data = createData(size, n, true);

			// Create the KDtree
			ags.utils.dataStructures.trees.thirdGenKD.KdTree2D<Object> tree = new ags.utils.dataStructures.trees.thirdGenKD.KdTree2D<Object>(
					2);
			for (double[] location : data)
				tree.addPoint(location, null);

			// Compute all-vs-all distances
			double[][] d = new double[n][n];
			for (int i = 0; i < n; i++)
				for (int j = i + 1; j < n; j++)
				{
					d[i][j] = d[j][i] = Maths.distance2(data[i][0], data[i][1], data[j][0], data[j][1]);
				}

			// For each point
			for (int i = 0; i < n; i++)
			{
				// Get the sorted distances to neighbours
				double[] d2 = PartialSort.bottom(d[i], K[K.length - 1]);

				// Get the KNN
				for (int k : K)
				{
					MaxHeap<Object> neighbours = tree.findNearestNeighbors(data[i], k,
							new SquareEuclideanDistanceFunction2D());

					Assert.assertEquals(d2[k - 1], neighbours.getMaxKey(), 0);
				}
			}
		}
	}

	private abstract class NNTimingTask extends BaseTimingTask
	{
		Object data;
		double[] expected;
		double eps;

		public NNTimingTask(String name, double[][] data, double[] expected)
		{
			super(name);
			this.data = data;
			this.expected = expected;
			this.eps = 0;
		}

		public NNTimingTask(String name, double[][] data, double[] expected, double eps)
		{
			super(name);
			// Convert to float
			double[][] d = (double[][]) data;
			int n = d.length;
			float[][] d2 = new float[n][];
			for (int i = 0; i < n; i++)
			{
				d2[i] = new float[] { (float) d[i][0], (float) d[i][1] };
			}
			this.data = d2;
			this.expected = expected;
			this.eps = eps;
		}

		public int getSize()
		{
			return 1;
		}

		public Object getData(int i)
		{
			return data;
		}

		public void check(int i, Object result)
		{
			double[] observed = (double[]) result;
			Assert.assertArrayEquals(expected, observed, eps);
		}
	}

	@Test
	public void secondGenIsFasterThanThirdGen()
	{
		TimingService ts = new TimingService(15);
		int n = 5000;
		double[][] data = createData(size, n, true);
		final int k = 4;

		long time = System.nanoTime();
		double[] expected = new double[n];
		double[][] d = new double[n][n];
		for (int i = 0; i < n; i++)
		{
			for (int j = i + 1; j < n; j++)
			{
				d[i][j] = d[j][i] = Maths.distance2(data[i][0], data[i][1], data[j][0], data[j][1]);
			}
		}
		for (int i = 0; i < n; i++)
			// Get the sorted distances to neighbours
			expected[i] = PartialSort.bottom(PartialSort.OPTION_HEAD_FIRST, d[i], n, k)[0];
		time = System.nanoTime() - time;

		ts.execute(new NNTimingTask("Second", data, expected)
		{
			public Object run(Object oData)
			{
				ags.utils.dataStructures.trees.secondGenKD.KdTree<Object> tree = new ags.utils.dataStructures.trees.secondGenKD.KdTree.SqrEuclid2D<Object>(
						null);
				double[][] data = (double[][]) oData;
				for (double[] location : data)
					tree.addPoint(location, null);
				double[] o = new double[data.length];
				for (int i = 0; i < data.length; i++)
				{
					o[i] = tree.nearestNeighbor(data[i], k, false).get(0).distance;
				}
				return o;
			}
		});

		ts.execute(new NNTimingTask("Second2D", data, expected)
		{
			public Object run(Object oData)
			{
				ags.utils.dataStructures.trees.secondGenKD.KdTree2D<Object> tree = new ags.utils.dataStructures.trees.secondGenKD.KdTree2D.SqrEuclid2D<Object>();
				double[][] data = (double[][]) oData;
				for (double[] location : data)
					tree.addPoint(location, null);
				double[] o = new double[data.length];
				for (int i = 0; i < data.length; i++)
				{
					o[i] = tree.nearestNeighbor(data[i], k, false).get(0).distance;
				}
				return o;
			}
		});

		ts.execute(new NNTimingTask("SecondSimple2D", data, expected)
		{
			public Object run(Object oData)
			{
				ags.utils.dataStructures.trees.secondGenKD.SimpleKdTree2D tree = new ags.utils.dataStructures.trees.secondGenKD.SimpleKdTree2D.SqrEuclid2D();
				double[][] data = (double[][]) oData;
				for (double[] location : data)
					tree.addPoint(location);
				double[] o = new double[data.length];
				for (int i = 0; i < data.length; i++)
				{
					o[i] = tree.nearestNeighbor(data[i], k, false).get(0).distance;
				}
				return o;
			}
		});

		ts.execute(new NNTimingTask("SecondSimpleFloat2D", data, expected, 1e-3)
		{
			public Object run(Object oData)
			{
				ags.utils.dataStructures.trees.secondGenKD.SimpleFloatKdTree2D tree = new ags.utils.dataStructures.trees.secondGenKD.SimpleFloatKdTree2D.SqrEuclid2D();
				float[][] data = (float[][]) oData;
				for (float[] location : data)
					tree.addPoint(location);
				double[] o = new double[data.length];
				for (int i = 0; i < data.length; i++)
				{
					o[i] = tree.nearestNeighbor(data[i], k, false).get(0).distance;
				}
				return o;
			}
		});

		ts.execute(new NNTimingTask("Third", data, expected)
		{
			public Object run(Object oData)
			{
				ags.utils.dataStructures.trees.thirdGenKD.KdTree<Object> tree = new ags.utils.dataStructures.trees.thirdGenKD.KdTreeND<Object>(
						2);
				double[][] data = (double[][]) oData;
				for (double[] location : data)
					tree.addPoint(location, null);
				DistanceFunction distanceFunction = new SquareEuclideanDistanceFunction2D();
				double[] o = new double[data.length];
				for (int i = 0; i < data.length; i++)
				{
					o[i] = tree.findNearestNeighbors(data[i], k, distanceFunction).getMaxKey();
				}
				return o;
			}
		});

		ts.execute(new NNTimingTask("Third2D", data, expected)
		{
			public Object run(Object oData)
			{
				ags.utils.dataStructures.trees.thirdGenKD.KdTree<Object> tree = new ags.utils.dataStructures.trees.thirdGenKD.KdTree2D<Object>();
				double[][] data = (double[][]) oData;
				for (double[] location : data)
					tree.addPoint(location, null);
				DistanceFunction distanceFunction = new SquareEuclideanDistanceFunction2D();
				double[] o = new double[data.length];
				for (int i = 0; i < data.length; i++)
				{
					o[i] = tree.findNearestNeighbors(data[i], k, distanceFunction).getMaxKey();
				}
				return o;
			}
		});

		ts.check();
		int number = ts.getSize();
		ts.repeat(number);
		ts.repeat(number);

		System.out.printf("All-vs-all = %d\n", time);
		ts.report();

		// No assertions are made since the timings are similar
	}

	class Float2DNNTimingTask extends NNTimingTask
	{
		int k;
		int buckectSize;

		public Float2DNNTimingTask(double[][] data, int k, int buckectSize)
		{
			super("Bucket" + buckectSize, data, null, 0);
			this.k = k;
			this.buckectSize = buckectSize;
		}

		public Object run(Object oData)
		{
			// The following tests the bucket size is optimal. It requires the bucketSize be set to public non-final.
			// This prevents some code optimisation and so is not the default. The default uses a final bucket size of 24.
			//int b = ags.utils.dataStructures.trees.secondGenKD.SimpleFloatKdTree2D.bucketSize;
			//ags.utils.dataStructures.trees.secondGenKD.SimpleFloatKdTree2D.bucketSize = buckectSize;
			
			ags.utils.dataStructures.trees.secondGenKD.SimpleFloatKdTree2D tree = new ags.utils.dataStructures.trees.secondGenKD.SimpleFloatKdTree2D.SqrEuclid2D();
			float[][] data = (float[][]) oData;
			for (float[] location : data)
				tree.addPoint(location);
			double[] o = new double[data.length];
			for (int i = 0; i < data.length; i++)
			{
				o[i] = tree.nearestNeighbor(data[i], k, false).get(0).distance;
			}
			//ags.utils.dataStructures.trees.secondGenKD.SimpleFloatKdTree2D.bucketSize = b;
			return o;
		}
	}

	// Requires code modification of the SimpleFloatKdTree2D class to make bucketSize size visible and not final ...
	//@Test
	public void secondGenBucket24IsFastest()
	{
		TimingService ts = new TimingService(15);
		int n = 5000;
		double[][] data = createData(size, n, true);
		final int k = 4;

		for (int b : new int[] { 1, 2, 3, 4, 5, 8, 16, 24, 32 })
			ts.execute(new Float2DNNTimingTask(data, k, b));

		int number = ts.getSize();
		ts.repeat(number);
		ts.repeat(number);

		ts.report();
	}

	private double[][] createData(int size, int n, boolean allowDuplicates)
	{
		double[][] data = new double[n][];
		if (allowDuplicates)
		{
			int half = n / 2;
			for (int i = half; i < n; i++)
			{
				data[i] = new double[] { rand.next() * size, rand.next() * size };
			}
			for (int i = 0, j = half; i < half; i++, j++)
			{
				data[i] = data[j];
			}
		}
		else
		{
			double[] x = SimpleArrayUtils.newArray(n, 0, (double) size / n);
			double[] y = x.clone();
			rand.shuffle(x);
			rand.shuffle(y);
			for (int i = 0; i < n; i++)
			{
				data[i] = new double[] { x[i], y[i] };
			}
		}
		return data;
	}
}
