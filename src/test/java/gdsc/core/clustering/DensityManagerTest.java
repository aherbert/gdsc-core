package gdsc.core.clustering;

import java.awt.Rectangle;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.junit.Assert;
import org.junit.Test;

import de.lmu.ifi.dbs.elki.algorithm.clustering.optics.ClusterOrder;
import de.lmu.ifi.dbs.elki.algorithm.clustering.optics.FastOPTICS;
import de.lmu.ifi.dbs.elki.algorithm.clustering.optics.OPTICSXi;
import de.lmu.ifi.dbs.elki.data.Clustering;
import de.lmu.ifi.dbs.elki.data.DoubleVector;
import de.lmu.ifi.dbs.elki.data.model.OPTICSModel;
import de.lmu.ifi.dbs.elki.data.type.TypeUtil;
import de.lmu.ifi.dbs.elki.database.AbstractDatabase;
import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.database.StaticArrayDatabase;
import de.lmu.ifi.dbs.elki.database.datastore.DataStoreFactory;
import de.lmu.ifi.dbs.elki.database.datastore.DataStoreUtil;
import de.lmu.ifi.dbs.elki.database.datastore.DoubleDataStore;
import de.lmu.ifi.dbs.elki.database.datastore.WritableDoubleDataStore;
import de.lmu.ifi.dbs.elki.database.ids.DBIDIter;
import de.lmu.ifi.dbs.elki.database.ids.DBIDRef;
import de.lmu.ifi.dbs.elki.database.ids.DBIDUtil;
import de.lmu.ifi.dbs.elki.database.ids.DBIDVar;
import de.lmu.ifi.dbs.elki.database.ids.DBIDs;
import de.lmu.ifi.dbs.elki.database.relation.Relation;
import de.lmu.ifi.dbs.elki.datasource.ArrayAdapterDatabaseConnection;
import de.lmu.ifi.dbs.elki.datasource.DatabaseConnection;
import de.lmu.ifi.dbs.elki.index.preprocessed.fastoptics.RandomProjectedNeighborsAndDensities;
import de.lmu.ifi.dbs.elki.math.random.RandomFactory;
import de.lmu.ifi.dbs.elki.utilities.ClassGenericsUtil;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameterization.ListParameterization;
import gdsc.core.clustering.DensityManager.Optics;
import gdsc.core.logging.ConsoleLogger;
import gdsc.core.logging.NullTrackProgress;
import gdsc.core.logging.TrackProgress;
import gdsc.core.utils.Maths;
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

	/**
	 * To overcome the 'issue' with the ELKI algorithm using fast-approximations we return the actual values required.
	 * We can do this because the dataset is small.
	 */
	private class CheatingRandomProjectedNeighborsAndDensities
			extends RandomProjectedNeighborsAndDensities<DoubleVector>
	{
		// All-vs-all distance matrix
		double[][] d;
		Relation<DoubleVector> points;
		int minPts;

		public CheatingRandomProjectedNeighborsAndDensities(double[][] d, int minPts)
		{
			super(RandomFactory.get(30051977l));
			this.d = d;
			this.minPts = minPts;
		}

		// TODO - override the methods used by optics 
		@Override
		public void computeSetsBounds(Relation<DoubleVector> points, int minSplitSize, DBIDs ptList)
		{
			// Store the points
			this.points = points;

			// What is this doing? Just call it anyway. 
			super.computeSetsBounds(points, minSplitSize, ptList);
		}

		@Override
		public DoubleDataStore computeAverageDistInSet()
		{
			// Here we do not use an approximation of the density but actually compute it.
			WritableDoubleDataStore davg = DataStoreUtil.makeDoubleStorage(points.getDBIDs(),
					DataStoreFactory.HINT_HOT);
			for (DBIDIter it = points.getDBIDs().iter(); it.valid(); it.advance())
			{
				double[] dd = d[asInteger(it)].clone();
				Arrays.sort(dd);
				davg.put(it, dd[minPts - 1]);
			}
			return davg;
		}
	}

	/**
	 * Test the results of OPTICS using the ELKI framework
	 */
	@Test
	public void canComputeOPTICS()
	{
		TrackProgress tracker = null; //new SimpleTrackProgress();
		for (int n : new int[] { 100, 500 })
		{
			DensityManager dm = createDensityManager(size, n);
			dm.setTracker(tracker);

			// Compute the all-vs-all distance for checking the answer
			double[][] data = dm.getDoubleData();
			double[][] d = new double[n][n];
			for (int i = 0; i < n; i++)
				for (int j = i + 1; j < n; j++)
					d[i][j] = d[j][i] = Maths.distance(data[0][i], data[1][i], data[0][j], data[1][j]);

			// Use ELKI to provide the expected results
			data = new Array2DRowRealMatrix(data).transpose().getData();

			for (int minPts : new int[] { 5, 10 })
			{
				// Reset starting Id to 1
				DatabaseConnection dbc = new ArrayAdapterDatabaseConnection(data, null, 0);
				ListParameterization params = new ListParameterization();
				params.addParameter(AbstractDatabase.Parameterizer.DATABASE_CONNECTION_ID, dbc);
				Database db = ClassGenericsUtil.parameterizeOrAbort(StaticArrayDatabase.class, params);
				db.initialize();
				Relation<?> rel = db.getRelation(TypeUtil.ANY);
				Assert.assertEquals("Database size does not match.", n, rel.size());

				// Debug: Print the core distance for each point
				//for (int i = 0; i < n; i++)
				//{
				//	double[] dd = d[i].clone();
				//	Arrays.sort(dd);
				//	System.out.printf("%d Core %f, next %f\n", i, dd[minPts - 1], dd[minPts]);
				//}

				// Use max range
				Optics r1 = dm.optics(size, minPts);

				// Test verses the ELKI frame work
				RandomProjectedNeighborsAndDensities<DoubleVector> index = new CheatingRandomProjectedNeighborsAndDensities(
						d, minPts);
				FastOPTICS<DoubleVector> fo = new FastOPTICS<DoubleVector>(minPts, index);
				ClusterOrder order = fo.run(db);

				// Check 
				int i = 0;
				DBIDVar pre = DBIDUtil.newVar();
				for (DBIDIter it = order.iter(); it.valid(); it.advance(), i++)
				{
					if (i == 0)
						// No predecessor or reachability distance
						continue;

					int expId = asInteger(it);
					int obsId = r1.get(i).parent;

					order.getPredecessor(it, pre);
					int expPre = asInteger(pre);
					int obsPre = r1.get(i).predecessor;

					double expR = order.getReachability(it);
					double obsR = r1.get(i).reachabilityDistance;

					//System.out.printf("[%d] %d %d : %f = %f (%f) : %s = %d\n", i, expId, obsId, expR, obsR,
					//		r1.get(i).coreDistance, expPre, obsPre);

					Assert.assertEquals(expId, obsId);
					Assert.assertEquals(expPre, obsPre);
					Assert.assertEquals(expR, obsR, expR * 1e-5);
				}
			}
		}
	}

	private static int asInteger(DBIDRef id)
	{
		return DBIDUtil.asInteger(id);
	}

	/**
	 * Test the results of OPTICS using the ELKI framework
	 */
	@Test
	public void canComputeOPTICSXi()
	{
		TrackProgress tracker = null; //new SimpleTrackProgress();
		for (int n : new int[] { 100, 500 })
		{
			DensityManager dm = createDensityManager(size, n);
			dm.setTracker(tracker);

			// Compute the all-vs-all distance for checking the answer
			double[][] data = dm.getDoubleData();
			double[][] d = new double[n][n];
			for (int i = 0; i < n; i++)
				for (int j = i + 1; j < n; j++)
					d[i][j] = d[j][i] = Maths.distance(data[0][i], data[1][i], data[0][j], data[1][j]);

			// Use ELKI to provide the expected results
			RealMatrix rm = new Array2DRowRealMatrix(data).transpose();

			for (int minPts : new int[] { 5, 10 })
			{
				// Reset starting Id to 1
				DatabaseConnection dbc = new ArrayAdapterDatabaseConnection(rm.getData(), null, 0);
				ListParameterization params = new ListParameterization();
				params.addParameter(AbstractDatabase.Parameterizer.DATABASE_CONNECTION_ID, dbc);
				Database db = ClassGenericsUtil.parameterizeOrAbort(StaticArrayDatabase.class, params);
				db.initialize();
				Relation<?> rel = db.getRelation(TypeUtil.ANY);
				Assert.assertEquals("Database size does not match.", n, rel.size());

				// Debug: Print the core distance for each point
				//for (int i = 0; i < n; i++)
				//{
				//	double[] dd = d[i].clone();
				//	Arrays.sort(dd);
				//	System.out.printf("%d Core %f, next %f\n", i, dd[minPts - 1], dd[minPts]);
				//}

				// Use max range
				Optics r1 = dm.optics(size, minPts);

				// Test verses the ELKI frame work
				RandomProjectedNeighborsAndDensities<DoubleVector> index = new CheatingRandomProjectedNeighborsAndDensities(
						d, minPts);
				FastOPTICS<DoubleVector> fo = new FastOPTICS<DoubleVector>(minPts, index);

				double xi = 0.03;

				OPTICSXi opticsXi = new OPTICSXi(fo, xi, false, false);
				Clustering<OPTICSModel> clustering = opticsXi.run(db);

				// Check by building the clusters into an array 
				int[] expClusters = new int[n];
				List<de.lmu.ifi.dbs.elki.data.Cluster<OPTICSModel>> allClusters = clustering.getAllClusters();
				int clusterId = 0;
				for (de.lmu.ifi.dbs.elki.data.Cluster<OPTICSModel> c : allClusters)
				{
					System.out.printf("%d-%d\n", c.getModel().getStartIndex(), c.getModel().getEndIndex());

					// Add the cluster Id to the expClusters
					clusterId++;
					for (DBIDIter it = c.getIDs().iter(); it.valid(); it.advance())
					{
						expClusters[asInteger(it)] = clusterId;
					}
				}

				// TODO - check the clusters match
				dm.extractClusters(r1, xi, false, false);
				int[] obsClusters = r1.getClusters();
				for (int i = 0; i < n; i++)
				{
					System.out.printf("%d = %d %d\n", i, expClusters[i], obsClusters[i]);
				}

				return;
				
				// I will have to allow the Ids to be different. So change the Ids using first occurrence mapping...

				// TODO - try at a lower distance threshold

				//System.out.printf("[%d] %d %d : %f = %f (%f) : %s = %d\n", i, expId, obsId, expR, obsR,
				//		r1.get(i).coreDistance, expPre, obsPre);

				//Assert.assertEquals(expId, obsId);
				//Assert.assertEquals(expPre, obsPre);
				//Assert.assertEquals(expR, obsR, expR * 1e-5);

			}
		}
	}

	//@Test
	public void canComputeOPTICSFaster()
	{
		// TODO - Check our implementation is faster than ELKI. This should be true given that it is 2D grid data.
		// If not then hope it is not much slower.		
	}

	@Test
	public void canPerformOPTICSWithLargeData()
	{
		TrackProgress tracker = null; //new SimpleTrackProgress();
		for (int n : N)
		{
			DensityManager dm = createDensityManager(size, n);
			dm.setTracker(tracker);

			for (int minPts : new int[] { 10, 20 })
			{
				dm.optics(0, minPts);
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
