package gdsc.core.clustering.optics;

/*----------------------------------------------------------------------------- 
 * GDSC ImageJ Software
 * 
 * Copyright (C) 2016 Alex Herbert
 * Genome Damage and Stability Centre
 * University of Sussex, UK
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *---------------------------------------------------------------------------*/

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.EnumSet;
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
import de.lmu.ifi.dbs.elki.database.datastore.DataStore;
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
import gdsc.core.clustering.optics.OPTICSManager.Option;
import gdsc.core.logging.ConsoleLogger;
import gdsc.core.logging.NullTrackProgress;
import gdsc.core.logging.TrackProgress;
import gdsc.core.utils.Maths;
import gdsc.core.utils.PartialSort;
import gdsc.core.utils.Random;

public class OPTICSManagerTest
{
	boolean skipSpeedTest = true;
	private gdsc.core.utils.Random rand = new Random(30051977);

	int size = 256;
	float[] radii = new float[] { 2, 4, 8, 16 };
	int[] N = new int[] { 1000, 2000, 4000, 8000 };

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
		@SuppressWarnings("unused")
		double generatingDistance;

		public CheatingRandomProjectedNeighborsAndDensities(double[][] d, int minPts, double generatingDistance)
		{
			super(RandomFactory.get(30051977l));
			this.d = d;
			this.minPts = minPts;
		}

		// Override the methods used by optics

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
				double d;
				double[] data = this.d[asInteger(it)];

				// Simple sort
				//double[] dd = data.clone();
				//Arrays.sort(dd);
				//d = dd[minPts - 1];

				// Partial sort
				d = PartialSort.bottom(PartialSort.OPTION_HEAD_FIRST, data, minPts)[0];

				// This break the code
				//davg.put(it, (d <= generatingDistance) ? d : FastOPTICS.UNDEFINED_DISTANCE);
				// This break the code
				//davg.put(it, (d <= generatingDistance) ? d : Double.POSITIVE_INFINITY);

				// This is OK. I am not sure how to deal with a smaller generating distance
				davg.put(it, d);
			}
			return davg;
		}

		@Override
		public DataStore<? extends DBIDs> getNeighs()
		{
			// Not modifying this method appears to work. 
			// We could find all Ids below the generating distance fro each point
			return super.getNeighs();
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
			OPTICSManager om = createOPTICSManager(size, n);
			om.setTracker(tracker);

			// Compute the all-vs-all distance for checking the answer
			double[][] data = om.getDoubleData();
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
				OPTICSResult r1 = om.optics(size, minPts);

				// Test verses the ELKI frame work
				RandomProjectedNeighborsAndDensities<DoubleVector> index = new CheatingRandomProjectedNeighborsAndDensities(
						d, minPts, size);
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
			OPTICSManager om = createOPTICSManager(size, n);
			om.setTracker(tracker);

			// Compute the all-vs-all distance for checking the answer
			double[][] data = om.getDoubleData();
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
				OPTICSResult r1 = om.optics(size, minPts);

				// Test verses the ELKI frame work
				RandomProjectedNeighborsAndDensities<DoubleVector> index = new CheatingRandomProjectedNeighborsAndDensities(
						d, minPts, size);
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
					//System.out.printf("%d-%d\n", c.getModel().getStartIndex(), c.getModel().getEndIndex());

					// Add the cluster Id to the expClusters
					clusterId++;
					for (DBIDIter it = c.getIDs().iter(); it.valid(); it.advance())
					{
						expClusters[asInteger(it)] = clusterId;
					}
				}

				// check the clusters match
				r1.extractClusters(xi);
				int[] obsClusters = r1.getClusters();

				// I will have to allow the Ids to be different. So change the Ids using first occurrence mapping...
				remap(expClusters);
				remap(obsClusters);

				//for (int i = 0; i < n; i++)
				//	System.out.printf("%d = %d %d\n", i, expClusters[i], obsClusters[i]);

				Assert.assertArrayEquals(expClusters, obsClusters);
			}
		}
	}

	private void remap(int[] clusters)
	{
		int n = clusters.length;
		int[] map = new int[n];
		int clusterId = 0;
		for (int i = 0; i < n; i++)
		{
			if (map[clusters[i]] == 0)
				map[clusters[i]] = ++clusterId;
		}
		for (int i = 0; i < n; i++)
			clusters[i] = map[clusters[i]];
	}

	/**
	 * Test the results of OPTICS using the ELKI framework
	 */
	@Test
	public void canComputeOPTICSXiWithNoHierarchy()
	{
		for (int n : new int[] { 100, 500 })
		{
			OPTICSManager om = createOPTICSManager(size, n);

			for (int minPts : new int[] { 5, 10 })
			{
				OPTICSResult r1 = om.optics(0, minPts);

				final double xi = 0.03;

				// check the clusters match
				r1.extractClusters(xi);
				ArrayList<OPTICSCluster> o1 = r1.getAllClusters();

				r1.extractClusters(xi, OPTICSResult.XI_OPTION_TOP_LEVEL);
				ArrayList<OPTICSCluster> o2 = r1.getAllClusters();

				Assert.assertTrue(o1.size() >= o2.size());

				//System.out.printf("%d : %d\n", n, minPts);
				for (OPTICSCluster cluster : o2)
				{
					Assert.assertTrue(cluster.getLevel() == 0);
					//System.out.println(cluster);
				}
			}
		}
	}

	@Test
	public void canComputeOPTICSWithHighResolution()
	{
		canComputeOPTICSWithOptions(Option.HIGH_RESOLUTION);
	}

	@Test
	public void canComputeOPTICSWithHighResolutionRadial()
	{
		canComputeOPTICSWithOptions(Option.HIGH_RESOLUTION, Option.RADIAL_PROCESSING);
	}

	public void canComputeOPTICSWithOptions(Option... options)
	{
		for (int n : new int[] { 100, 500 })
		{
			OPTICSManager om1 = createOPTICSManager(size, n);
			OPTICSManager om2 = om1.clone();
			om2.setOptions(options);

			for (int minPts : new int[] { 5, 10 })
			{
				// Use max range
				OPTICSResult r1 = om1.optics(0, minPts);
				OPTICSResult r1b = om1.optics(0, minPts);
				OPTICSResult r2 = om2.optics(0, minPts);

				areEqual("repeat", r1, r1b);
				areEqual("new", r1, r2);
			}
		}
	}

	private void areEqual(String title, OPTICSResult r1, OPTICSResult r2)
	{
		for (int i = 0; i < r1.size(); i++)
		{
			// Edge-points are random so ignore them. Only do core points.
			if (!r1.get(i).isCorePoint() || !r1.get(i).isCorePoint())
				continue;

			double expC = r1.get(i).coreDistance;
			double obsC = r2.get(i).coreDistance;
			Assert.assertEquals(title + " C " + i, expC, obsC, expC * 1e-5);
			
			int expId = r1.get(i).parent;
			int obsId = r2.get(i).parent;

			int expPre = r1.get(i).predecessor;
			int obsPre = r2.get(i).predecessor;

			double expR = r1.get(i).reachabilityDistance;
			double obsR = r2.get(i).reachabilityDistance;

			//System.out.printf("[%d] %d %d : %f = %f (%f) : %s = %d\n", i, expId, obsId, expR, obsR,
			//		r1.get(i).coreDistance, expPre, obsPre);

			Assert.assertEquals(title + " Id " + i, expId, obsId);
			Assert.assertEquals(title + " Pre " + i, expPre, obsPre);
			Assert.assertEquals(title + " R " + i, expR, obsR, expR * 1e-5);
		}
	}

	@Test
	public void canComputeDBSCANWithRadial()
	{
		canComputeDBSCANWithOptions(Option.RADIAL_PROCESSING);
	}

	@Test
	public void canComputeDBSCANWithHighResolution()
	{
		canComputeDBSCANWithOptions(Option.HIGH_RESOLUTION);
	}

	@Test
	public void canComputeDBSCANWithHighResolutionRadial()
	{
		canComputeDBSCANWithOptions(Option.HIGH_RESOLUTION, Option.RADIAL_PROCESSING);
	}

	private void canComputeDBSCANWithOptions(Option... options)
	{
		for (int n : new int[] { 100, 500, 5000 })
		{
			OPTICSManager om1 = createOPTICSManager(size, n);
			OPTICSManager om2 = om1.clone();
			om2.setOptions(options);

			for (int minPts : new int[] { 5, 10 })
			{
				DBSCANResult r1 = om1.dbscan(0, minPts);
				DBSCANResult r1b = om1.dbscan(0, minPts);
				DBSCANResult r2 = om2.dbscan(0, minPts);

				areEqual("repeat", r1, r1b, minPts);
				areEqual("new", r1, r2, minPts);
			}
		}
	}

	private void areEqual(String title, DBSCANResult r1, DBSCANResult r2, int minPts)
	{
		for (int i = 0; i < r1.size(); i++)
		{
			int expPts = r1.get(i).nPts;
			int obsPts = r2.get(i).nPts;

			// Edge-points are random so ignore them. Only do core points.
			if (expPts < minPts || obsPts < minPts)
				continue;

			Assert.assertEquals(title + " Pts " + i, expPts, obsPts);

			int expId = r1.get(i).parent;
			int obsId = r2.get(i).parent;

			int expCId = r1.get(i).clusterId;
			int obsCId = r2.get(i).clusterId;

			Assert.assertEquals(title + " Id " + i, expId, obsId);
			Assert.assertEquals(title + " CId " + i, expCId, obsCId);
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
			OPTICSManager om = createOPTICSManager(size, n);
			om.setTracker(tracker);

			for (int minPts : new int[] { 10, 20 })
			{
				om.optics(0, minPts);
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
				OPTICSManager.computeGeneratingDistance(minPts, area, n);
				//System.out.printf("k=%d, volumeDS=%.1f, N=%d, d=%f\n", minPts, area, n, d);
			}
		}
	}

	@Test
	public void canRepeatOPTICS()
	{
		int n = N[0];
		OPTICSManager om = createOPTICSManager(size, n);

		float radius = 0;

		int minPts = 10;
		Assert.assertFalse(om.hasMemory());

		EnumSet<Option> opt = om.getOptions();

		opt.add(OPTICSManager.Option.CACHE);
		OPTICSResult r1 = om.optics(radius, minPts);
		Assert.assertTrue(om.hasMemory());

		opt.remove(OPTICSManager.Option.CACHE);
		OPTICSResult r2 = om.optics(radius, minPts);
		Assert.assertFalse(om.hasMemory());

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
			OPTICSManager om = createOPTICSManager(size, n);

			for (float radius : new float[] { 0.01f })
			{
				om.optics(radius, minPts);
				//System.out.printf("OPTICS %d @ %.1f,%d\n", n, radius, minPts);
			}
		}
	}

	@Test
	public void canPerformOPTICSWith1Point()
	{
		OPTICSManager om = createOPTICSManager(size, 1);

		for (float radius : new float[] { -1, 0, 0.01f, 1f })
			for (int minPts : new int[] { -1, 0, 1 })
			{
				OPTICSResult r1 = om.optics(radius, minPts);
				// Should be 1 cluster
				Assert.assertEquals(1, r1.get(0).clusterId);
			}

		OPTICSResult r1 = om.optics(1, 2);
		// Should be 0 clusters as the min size is too high
		Assert.assertEquals(0, r1.get(0).clusterId);
	}

	@Test
	public void canPerformOPTICSWithColocatedData()
	{
		OPTICSManager om = new OPTICSManager(new float[10], new float[10], new Rectangle(size, size));

		for (float radius : new float[] { -1, 0, 0.01f, 1f })
			for (int minPts : new int[] { -1, 0, 1, 10 })
			{
				OPTICSResult r1 = om.optics(radius, minPts);
				// All should be in the same cluster
				Assert.assertEquals(1, r1.get(0).clusterId);
			}
	}

	@Test
	public void canConvertOPTICSToDBSCAN()
	{
		int n = N[0];
		OPTICSManager om = createOPTICSManager(size, n);

		float radius = radii[radii.length - 1];

		int minPts = 10;
		OPTICSResult r1 = om.optics(radius, minPts);
		// Store for later and reset
		int[] clusterId = new int[r1.size()];
		for (int i = r1.size(); i-- > 0;)
		{
			clusterId[i] = r1.get(i).clusterId;
			r1.get(i).clusterId = -1;
		}
		// Smaller distance
		int nClusters = r1.extractDBSCANClustering(radius * 0.8f);
		int max = 0;
		for (int i = r1.size(); i-- > 0;)
		{
			if (max < r1.get(i).clusterId)
				max = r1.get(i).clusterId;
			Assert.assertNotEquals(r1.get(i).clusterId, -1);
		}
		Assert.assertEquals(nClusters, max);
		// Same distance
		nClusters = r1.extractDBSCANClustering(radius);
		for (int i = r1.size(); i-- > 0;)
		{
			Assert.assertEquals(r1.get(i).clusterId, clusterId[i]);
		}
	}

	/**
	 * Test the results of DBSCAN using OPTICS
	 */
	@Test
	public void canComputeDBSCAN()
	{
		for (int n : new int[] { 100, 500 })
		{
			OPTICSManager om = createOPTICSManager(size, n);
			// Keep items in memory for speed during the test
			om.setOptions(OPTICSManager.Option.CACHE);

			for (int minPts : new int[] { 5, 10 })
			{
				// Use default range
				OPTICSResult r1 = om.optics(0, minPts);
				DBSCANResult r2 = om.dbscan(0, minPts);

				areSameClusters(r1, r2);
			}
		}
	}

	private void areSameClusters(OPTICSResult r1, DBSCANResult r2)
	{
		// Check. Remove non-core points as OPTICS and DBSCAN differ in the 
		// processing order within a cluster.
		int[] c1 = r1.getClusters(true);
		int[] c2 = r2.getClusters(true);

		Assert.assertArrayEquals(c1, c2);
	}

	@Test
	public void dBSCANIsFasterThanOPTICS()
	{
		OPTICSManager om1 = createOPTICSManager(size, 5000);
		OPTICSManager om2 = om1.clone();

		long t1 = System.nanoTime();
		OPTICSResult r1 = om1.optics(0, 10);
		long t2 = System.nanoTime();
		DBSCANResult r2 = om2.dbscan(0, 10);
		long t3 = System.nanoTime();

		areSameClusters(r1, r2);

		t3 = t3 - t2;
		t2 = t2 - t1;

		Assert.assertTrue(t3 < t2);

		// Note: The OPTICS paper reports that it should be about 1.6x slower than DBSCAN 
		// This test shows a smaller difference probably due to unrealistic data. 

		//System.out.printf("%d < %d (%.2f)\n", t3, t2, (double) t2 / t3);
	}

	@Test
	public void dBSCANHighResolutionRadialIsFaster()
	{
		OPTICSManager om1 = createOPTICSManager(size, 5000);
		OPTICSManager om2 = om1.clone();
		om2.setOptions(Option.HIGH_RESOLUTION, Option.RADIAL_PROCESSING);

		int minPts = 20;

		long t1 = System.nanoTime();
		DBSCANResult r1 = om1.dbscan(0, minPts);
		long t2 = System.nanoTime();
		DBSCANResult r2 = om2.dbscan(0, minPts);
		long t3 = System.nanoTime();

		DBSCANResult r1b = om1.dbscan(0, minPts);

		areEqual("repeat", r1, r1b, minPts);
		areEqual("new", r1, r2, minPts);

		t3 = t3 - t2;
		t2 = t2 - t1;

		System.out.printf("dBSCANHighResolutionRadialIsFaster %d < %d (%.2f)\n", t3, t2, (double) t2 / t3);

		//Assert.assertTrue(t3 < t2);
	}

	private OPTICSManager createOPTICSManager(int size, int n)
	{
		float[] xcoord = new float[n];
		float[] ycoord = new float[xcoord.length];
		for (int i = 0; i < xcoord.length; i++)
		{
			xcoord[i] = rand.next() * size;
			ycoord[i] = rand.next() * size;
		}
		OPTICSManager om = new OPTICSManager(xcoord, ycoord, new Rectangle(size, size));
		return om;
	}
}
