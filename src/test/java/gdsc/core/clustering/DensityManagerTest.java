package gdsc.core.clustering;

import java.awt.Rectangle;

import org.junit.Assert;
import org.junit.Test;

import gdsc.core.clustering.DensityManager.Optics;
import gdsc.core.logging.ConsoleLogger;
import gdsc.core.logging.NullTrackProgress;
import gdsc.core.logging.TrackProgress;
import gdsc.core.utils.Random;

public class DensityManagerTest
{
	boolean skipSpeedTest = true;
	private gdsc.core.utils.Random rand = new Random(30051977);

	int size = 256;
	float[] radii = new float[] { 2, 4, 8, 16 };
	int[] N = new int[] { 1000, 2000, 4000, 8000 };

	@Test
	public void densityWithTriangleMatchesDensity()
	{
		for (int n : N)
		{
			DensityManager dm = createDensityManager(size, n);

			for (float radius : radii)
			{
				int[] d1 = dm.calculateDensity(radius);
				int[] d2 = dm.calculateDensityTriangle(radius);

				Assert.assertArrayEquals(String.format("N=%d, R=%f", n, radius), d1, d2);
			}
		}
	}

	@Test
	public void densityWithGridMatchesDensity()
	{
		for (int n : N)
		{
			DensityManager dm = createDensityManager(size, n);

			for (float radius : radii)
			{
				int[] d1 = dm.calculateDensity(radius);
				int[] d2 = dm.calculateDensityGrid(radius);

				Assert.assertArrayEquals(String.format("N=%d, R=%f", n, radius), d1, d2);
			}
		}
	}

	@Test
	public void densityWithGridFasterThanDensityTriangle()
	{
		if (skipSpeedTest)
			return;
		for (int n : N)
		{
			DensityManager dm = createDensityManager(size, n);

			for (float radius : radii)
			{
				long start = System.nanoTime();
				for (int i = 10; i-- > 0;)
					dm.calculateDensityTriangle(radius);
				long t1 = System.nanoTime() - start;
				start = System.nanoTime();
				for (int i = 10; i-- > 0;)
					dm.calculateDensityGrid(radius);
				long t2 = System.nanoTime() - start;

				String msg = String.format("Grid vs Triangle. N=%d, R=%f : %fx faster", n, radius, (double) t1 / t2);
				System.out.println(msg);
				Assert.assertTrue(msg, t2 < t1);
			}
		}
	}

	@Test
	public void densityWithGridFasterThanDensity()
	{
		if (skipSpeedTest)
			return;
		for (int n : N)
		{
			DensityManager dm = createDensityManager(size, n);

			for (float radius : radii)
			{
				long start = System.nanoTime();
				for (int i = 10; i-- > 0;)
					dm.calculateDensity(radius);
				long t1 = System.nanoTime() - start;
				start = System.nanoTime();
				for (int i = 10; i-- > 0;)
					dm.calculateDensityGrid(radius);
				long t2 = System.nanoTime() - start;

				String msg = String.format("Grid vs Standard. N=%d, R=%f : %fx faster", n, radius, (double) t1 / t2);
				System.out.println(msg);
				Assert.assertTrue(msg, t2 < t1);
			}
		}
	}

	@Test
	public void sumWithGridMatchesSum()
	{
		for (int n : N)
		{
			DensityManager dm = createDensityManager(size, n);

			for (float radius : radii)
			{
				int s1 = dm.calculateSum(radius);
				int s2 = dm.calculateSumGrid(radius);

				Assert.assertEquals(String.format("N=%d, R=%f", n, radius), s1, s2);
			}
		}
	}

	@Test
	public void sumWithGridFasterThanSum()
	{
		if (skipSpeedTest)
			return;
		for (int n : N)
		{
			DensityManager dm = createDensityManager(size, n);

			for (float radius : radii)
			{
				long start = System.nanoTime();
				for (int i = 10; i-- > 0;)
					dm.calculateSum(radius);
				long t1 = System.nanoTime() - start;
				start = System.nanoTime();
				for (int i = 10; i-- > 0;)
					dm.calculateSumGrid(radius);
				long t2 = System.nanoTime() - start;

				String msg = String.format("Sum Grid vs Standard. N=%d, R=%f : %fx faster", n, radius,
						(double) t1 / t2);
				System.out.println(msg);
				Assert.assertTrue(msg, t2 < t1);
			}
		}
	}

	@Test
	public void blockDensityMatchesBlockDensity2()
	{
		for (int n : N)
		{
			DensityManager dm = createDensityManager(size, n);

			for (float radius : radii)
			{
				int[] d1 = dm.calculateBlockDensity(radius);
				int[] d2 = dm.calculateBlockDensity2(radius);

				Assert.assertArrayEquals(String.format("N=%d, R=%f", n, radius), d1, d2);
			}
		}
	}

	@Test
	public void blockDensity2MatchesBlockDensity3()
	{
		for (int n : N)
		{
			DensityManager dm = createDensityManager(size, n);

			for (float radius : radii)
			{
				int[] d1 = dm.calculateBlockDensity2(radius);
				int[] d2 = dm.calculateBlockDensity3(radius);

				Assert.assertArrayEquals(String.format("N=%d, R=%f", n, radius), d1, d2);
			}
		}
	}

	// This is not always true. The two are comparable in speed.
	//@Test
	public void blockDensityFasterThanBlockDensity2()
	{
		if (skipSpeedTest)
			return;
		for (int n : N)
		{
			DensityManager dm = createDensityManager(size, n);

			for (float radius : radii)
			{
				long start = System.nanoTime();
				for (int i = 10; i-- > 0;)
					dm.calculateBlockDensity(radius);
				long t1 = System.nanoTime() - start;
				start = System.nanoTime();
				for (int i = 10; i-- > 0;)
					dm.calculateBlockDensity2(radius);
				long t2 = System.nanoTime() - start;

				String msg = String.format("calculateBlockDensity2 vs calculateBlockDensity. N=%d, R=%f : %fx faster",
						n, radius, (double) t1 / t2);
				System.out.println(msg);
				Assert.assertTrue(msg, t2 < t1);
			}
		}
	}

	@Test
	public void blockDensity2FasterThanBlockDensity3()
	{
		if (skipSpeedTest)
			return;
		for (int n : N)
		{
			DensityManager dm = createDensityManager(size, n);

			for (float radius : radii)
			{
				long start = System.nanoTime();
				for (int i = 10; i-- > 0;)
					dm.calculateBlockDensity3(radius);
				long t1 = System.nanoTime() - start;
				start = System.nanoTime();
				for (int i = 10; i-- > 0;)
					dm.calculateBlockDensity2(radius);
				long t2 = System.nanoTime() - start;

				String msg = String.format("calculateBlockDensity2 vs calculateBlockDensity3. N=%d, R=%f : %fx faster",
						n, radius, (double) t1 / t2);
				System.out.println(msg);
				Assert.assertTrue(msg, t2 < t1);
			}
		}
	}

	class SimpleTrackProgress extends NullTrackProgress
	{
		ConsoleLogger l = new ConsoleLogger();

		@Override
		public void log(String format, Object... args)
		{
			l.info(format, args);
		}
	}

	@Test
	public void canPerformOPTICS()
	{
		TrackProgress tracker = null; //new SimpleTrackProgress();
		for (int n : N)
		{
			DensityManager dm = createDensityManager(size, n);
			dm.setTracker(tracker);

			for (int minPts : new int[] { 10, 20 })
			{
				//Optics r1 = 
				dm.optics(0, minPts);
				//// Histogram the results
				//double[] x = new double[r1.size()];
				//double[] y = new double[r1.size()];
				//for (int i = 0; i < r1.size(); i++)
				//{
				//	x[i] = i + 1;
				//	y[i] = r1.get(i).reachabilityDistance;
				//}
				//Plot plot = new Plot("OPTICS", "Order", "R_dist");
				//plot.setLimits(1, r1.size() + 1, 0, radius);
				//plot.addPoints(x, y, Plot.LINE);
				//Utils.display("OPTICS", plot);
			}
		}
	}

	@Test
	public void canComputeGeneratingDistance()
	{
		int[] points = new int[] { 1, 2, 3, 5, 10, 20, 50, 100 };
		double area = size * size;
		for (int n : N)
		{
			for (int minPts : points)
			{
				//float d = 
				DensityManager.computeGeneratingDistance(minPts, area, n);
				//System.out.printf("k=%d, volumeDS=%.1f, N=%d, d=%f\n", minPts, area, n, d);
			}
		}
	}

	@Test
	public void canRepeatOPTICS()
	{
		int n = N[0];
		DensityManager dm = createDensityManager(size, n);

		float radius = 0;

		int minPts = 10;
		Assert.assertFalse(dm.hasOpticsMemory());
		Optics r1 = dm.optics(radius, minPts, false);
		Assert.assertTrue(dm.hasOpticsMemory());
		Optics r2 = dm.optics(radius, minPts, true);
		Assert.assertFalse(dm.hasOpticsMemory());
		Assert.assertEquals(r1.size(), r2.size());
		for (int i = r1.size(); i-- > 0;)
		{
			Assert.assertEquals(r1.get(i).parent, r2.get(i).parent);
			Assert.assertEquals(r1.get(i).clusterId, r2.get(i).clusterId);
			Assert.assertEquals(r1.get(i).coreDistance, r2.get(i).coreDistance, 0);
			Assert.assertEquals(r1.get(i).reachabilityDistance, r2.get(i).reachabilityDistance, 0);
		}
	}

	@Test
	public void canPerformOPTICSWithTinyRadius()
	{
		int minPts = 10;
		for (int n : N)
		{
			DensityManager dm = createDensityManager(size, n);

			for (float radius : new float[] { 0.01f })
			{
				dm.optics(radius, minPts);
				//System.out.printf("OPTICS %d @ %.1f,%d\n", n, radius, minPts);
			}
		}
	}

	@Test
	public void canPerformOPTICSWith1Point()
	{
		DensityManager dm = createDensityManager(size, 1);

		for (float radius : new float[] { -1, 0, 0.01f, 1f })
			for (int minPts : new int[] { -1, 0, 1 })
			{
				Optics r1 = dm.optics(radius, minPts, false);
				// Should be 1 cluster
				Assert.assertEquals(1, r1.get(0).clusterId);
			}

		Optics r1 = dm.optics(1, 2, false);
		// Should be 0 clusters as the min size is too high
		Assert.assertEquals(0, r1.get(0).clusterId);
	}

	@Test
	public void canPerformOPTICSWithColocatedData()
	{
		DensityManager dm = new DensityManager(new float[10], new float[10], new Rectangle(size, size));

		for (float radius : new float[] { -1, 0, 0.01f, 1f })
			for (int minPts : new int[] { -1, 0, 1, 10 })
			{
				Optics r1 = dm.optics(radius, minPts, false);
				// All should be in the same cluster
				Assert.assertEquals(1, r1.get(0).clusterId);
			}
	}

	@Test
	public void canConvertOPTICSToDBSCAN()
	{
		int n = N[0];
		DensityManager dm = createDensityManager(size, n);

		float radius = radii[radii.length - 1];

		int minPts = 10;
		Optics r1 = dm.optics(radius, minPts, false);
		// Store for later and reset
		int[] clusterId = new int[r1.size()];
		for (int i = r1.size(); i-- > 0;)
		{
			clusterId[i] = r1.get(i).clusterId;
			r1.get(i).clusterId = -1;
		}
		// Smaller distance
		int nClusters = dm.extractDBSCANClustering(r1, radius * 0.8f);
		int max = 0;
		for (int i = r1.size(); i-- > 0;)
		{
			if (max < r1.get(i).clusterId)
				max = r1.get(i).clusterId;
			Assert.assertNotEquals(r1.get(i).clusterId, -1);
		}
		Assert.assertEquals(nClusters, max);
		// Same distance
		nClusters = dm.extractDBSCANClustering(r1, radius);
		for (int i = r1.size(); i-- > 0;)
		{
			Assert.assertEquals(r1.get(i).clusterId, clusterId[i]);
		}
	}

	private DensityManager createDensityManager(int size, int n)
	{
		float[] xcoord = new float[n];
		float[] ycoord = new float[xcoord.length];
		for (int i = 0; i < xcoord.length; i++)
		{
			xcoord[i] = rand.next() * size;
			ycoord[i] = rand.next() * size;
		}
		DensityManager dm = new DensityManager(xcoord, ycoord, new Rectangle(size, size));
		return dm;
	}
}
