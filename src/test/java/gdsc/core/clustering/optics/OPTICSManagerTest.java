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
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
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
import gdsc.core.test.BaseTimingTask;
import gdsc.core.test.TimingResult;
import gdsc.core.test.TimingService;
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

	class SimpleMoleculeSpace extends MoleculeSpace
	{
		OPTICSManager opticsManager;
		// All-vs-all distance matrix
		float[][] d;

		// All-vs-all distance matrix in double - Used for ELKI
		double[][] dd;

		SimpleMoleculeSpace(OPTICSManager opticsManager, float generatingDistanceE)
		{
			super(opticsManager.getSize(), generatingDistanceE);
			this.opticsManager = opticsManager;
			generate();
		}

		@Override
		Molecule[] generate()
		{
			final float[] xcoord = opticsManager.getXData();
			final float[] ycoord = opticsManager.getYData();

			// Compute all-vs-all distance matrix
			final int n = xcoord.length;
			d = new float[n][n];

			setOfObjects = new Molecule[xcoord.length];
			for (int i = 0; i < xcoord.length; i++)
			{
				final float x = xcoord[i];
				final float y = ycoord[i];
				// Build a single linked list
				final Molecule m = new Molecule(i, x, y, 0, 0, null);
				setOfObjects[i] = m;
				for (int j = i; j-- > 0;)
					d[i][j] = d[j][i] = m.distance2(setOfObjects[j]);
			}

			return setOfObjects;
		}

		void createDD()
		{
			double[][] doubleData = opticsManager.getDoubleData();
			final double[] xcoord = doubleData[0];
			final double[] ycoord = doubleData[1];

			// Compute all-vs-all distance matrix
			final int n = xcoord.length;
			dd = new double[n][n];

			for (int i = 0; i < xcoord.length; i++)
			{
				final double x = xcoord[i];
				final double y = ycoord[i];
				// Build a single linked list
				for (int j = i; j-- > 0;)
					dd[i][j] = dd[j][i] = Maths.distance(x, y, xcoord[j], ycoord[j]);
			}
		}

		@Override
		void findNeighbours(int minPts, Molecule object, float e)
		{
			float[] fdata = d[object.id];
			neighbours.clear();
			for (int i = 0; i < fdata.length; i++)
				//if (object.distance2(setOfObjects[i]) <= e)
				if (fdata[i] <= e)
					neighbours.add(setOfObjects[i]);
			//if (neighbours.size < minPts)
			//	neighbours.clear();
		}

		@Override
		void findNeighboursAndDistances(int minPts, Molecule object, float e)
		{
			float[] fdata = d[object.id];
			neighbours.clear();
			for (int i = 0; i < fdata.length; i++)
				if (fdata[i] < e)
				{
					setOfObjects[i].d = fdata[i];
					neighbours.add(setOfObjects[i]);
				}
			if (neighbours.size < minPts)
				neighbours.clear();
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
		SimpleMoleculeSpace space;
		Relation<DoubleVector> points;
		int minPts;

		public CheatingRandomProjectedNeighborsAndDensities(SimpleMoleculeSpace space, int minPts)
		{
			super(RandomFactory.get(30051977l));
			this.space = space;
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
				//float[] fdata = space.d[asInteger(it)];
				//double[] data = new StoredDataStatistics(fdata).getValues();

				double[] data = space.dd[asInteger(it)];

				// Simple sort
				//double[] dd = data.clone();
				//Arrays.sort(dd);
				//d = dd[minPts - 1];

				// Partial sort
				//double d = Math.sqrt(PartialSort.bottom(PartialSort.OPTION_HEAD_FIRST, data, minPts)[0]);
				double d = PartialSort.bottom(PartialSort.OPTION_HEAD_FIRST, data, minPts)[0];

				// This breaks the code
				//davg.put(it, (d <= generatingDistance) ? d : FastOPTICS.UNDEFINED_DISTANCE);
				// This breaks the code
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

			SimpleMoleculeSpace space = new SimpleMoleculeSpace(om, 0);
			space.createDD();

			// Use ELKI to provide the expected results
			double[][] data = new Array2DRowRealMatrix(om.getDoubleData()).transpose().getData();

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
						space, minPts);
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
			SimpleMoleculeSpace space = new SimpleMoleculeSpace(om, 0);
			space.createDD();

			// Use ELKI to provide the expected results
			double[][] data = new Array2DRowRealMatrix(om.getDoubleData()).transpose().getData();

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
						space, minPts);
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
	public void canComputeOPTICSWithInnerProcessing()
	{
		canComputeOPTICSWithOptions(Option.INNER_PROCESSING);
	}

	@Test
	public void canComputeOPTICSWithCircularProcessing()
	{
		canComputeOPTICSWithOptions(Option.CIRCULAR_PROCESSING);
	}

	@Test
	public void canComputeOPTICSWithInnerCircularProcessing()
	{
		canComputeOPTICSWithOptions(Option.INNER_PROCESSING, Option.CIRCULAR_PROCESSING);
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
	public void canComputeDBSCANWithGridProcessing()
	{
		canComputeDBSCANWithOptions(Option.GRID_PROCESSING);
	}

	@Test
	public void canComputeDBSCANWithCircularProcessing()
	{
		canComputeDBSCANWithOptions(Option.CIRCULAR_PROCESSING);
	}

	@Test
	public void canComputeDBSCANWithInnerProcessingCircular()
	{
		canComputeDBSCANWithOptions(Option.INNER_PROCESSING, Option.CIRCULAR_PROCESSING);
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

		//for (int i = 0; i < c1.length; i++)
		//{
		//	System.out.printf("[%d] %d == %d\n", i, c1[i], c2[i]);
		//}

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
		// This test shows a different value due to:
		// - unrealistic data
		// - The optimised DBSCAN implementation not computing distances if not needed.

		System.out.printf("dBSCANIsFasterThanOPTICS %d < %d (%.2f)\n", t3, t2, (double) t2 / t3);
	}

	@Test
	public void dBSCANInnerCircularIsFasterWhenDensityIsHigh()
	{
		int molecules = 10000;
		OPTICSManager om1 = createOPTICSManager(size, molecules);
		OPTICSManager om2 = om1.clone();
		om1.setOptions(Option.GRID_PROCESSING);
		om2.setOptions(Option.CIRCULAR_PROCESSING, Option.INNER_PROCESSING);

		float generatingDistanceE = 0;
		double nMoleculesInPixel = (double) molecules / (size * size);
		double nMoleculesInCircle;
		int limit = RadialMoleculeSpace.N_MOLECULES_FOR_NEXT_RESOLUTION_INNER * 2;
		do
		{
			generatingDistanceE += 0.1f;
			nMoleculesInCircle = Math.PI * generatingDistanceE * generatingDistanceE * nMoleculesInPixel;
		} while (nMoleculesInCircle < limit && generatingDistanceE < size);

		int minPts = 20;

		DBSCANResult r1, r2;

		// Warm-up
		//r1 = om1.dbscan(generatingDistanceE, minPts);
		//r2 = om2.dbscan(generatingDistanceE, minPts);

		long t1 = System.nanoTime();
		r1 = om1.dbscan(generatingDistanceE, minPts);
		long t2 = System.nanoTime();
		r2 = om2.dbscan(generatingDistanceE, minPts);
		long t3 = System.nanoTime();

		areEqual("new", r1, r2, minPts);

		t3 = t3 - t2;
		t2 = t2 - t1;

		System.out.printf("dBSCANInnerCircularIsFasterWhenComparisonsIsHigh %d < %d (%.2f)\n", t3, t2,
				(double) t2 / t3);

		// This sometimes fails due to JVM warm-up so add a factor.
		Assert.assertTrue(t3 < t2 * 2);
	}

	@Test
	public void oPTICSCircularIsFasterWhenDensityIsHigh()
	{
		int molecules = 10000;
		OPTICSManager om1 = createOPTICSManager(size, molecules);
		OPTICSManager om2 = om1.clone();
		om1.setOptions(Option.GRID_PROCESSING);
		om2.setOptions(Option.CIRCULAR_PROCESSING);

		float generatingDistanceE = 0;
		double nMoleculesInPixel = (double) molecules / (size * size);
		double nMoleculesInCircle;
		int limit = RadialMoleculeSpace.N_MOLECULES_FOR_NEXT_RESOLUTION_OUTER;
		do
		{
			generatingDistanceE += 0.1f;
			nMoleculesInCircle = Math.PI * generatingDistanceE * generatingDistanceE * nMoleculesInPixel;
		} while (nMoleculesInCircle < limit && generatingDistanceE < size);

		int minPts = 20;

		OPTICSResult r1, r2;

		// Warm-up
		//r1 = om1.optics(generatingDistanceE, minPts);
		//r2 = om2.optics(generatingDistanceE, minPts);

		long t1 = System.nanoTime();
		r1 = om1.optics(generatingDistanceE, minPts);
		long t2 = System.nanoTime();
		r2 = om2.optics(generatingDistanceE, minPts);
		long t3 = System.nanoTime();

		areEqual("new", r1, r2);

		t3 = t3 - t2;
		t2 = t2 - t1;

		System.out.printf("oPTICSCircularIsFasterWhenDensityIsHigh %d < %d (%.2f)\n", t3, t2, (double) t2 / t3);

		// This sometimes fails due to JVM warm-up so add a factor.
		Assert.assertTrue(t3 < t2 * 2);
	}

	private enum MS
	{
		SIMPLE, GRID, RADIAL, INNER_RADIAL
	}

	private abstract class MyTimingTask extends BaseTimingTask
	{
		MS ms;
		OPTICSManager[] om;
		int minPts;
		float generatingDistanceE, e;
		int resolution;
		Option[] options;
		String name;

		public MyTimingTask(MS ms, OPTICSManager[] om, int minPts, float generatingDistanceE, int resolution,
				Option... options)
		{
			super(ms.toString());
			this.ms = ms;
			this.om = om;
			this.generatingDistanceE = generatingDistanceE;
			this.resolution = resolution;
			this.minPts = minPts;
			e = generatingDistanceE * generatingDistanceE;
			this.options = options;
		}

		public int getSize()
		{
			return om.length;
		}

		public Object getData(int i)
		{
			// Create the molecule space
			EnumSet<Option> o = om[i].getOptions();
			if (options != null)
			{
				o = o.clone();
				om[i].setOptions(options);
			}
			MoleculeSpace space = null;
			switch (ms)
			{
				case SIMPLE:
					space = new SimpleMoleculeSpace(om[i], generatingDistanceE);
					break;
				case GRID:
					space = new GridMoleculeSpace(om[i], generatingDistanceE, resolution);
					break;
				case RADIAL:
					space = new RadialMoleculeSpace(om[i], generatingDistanceE, resolution);
					break;
				case INNER_RADIAL:
					space = new InnerRadialMoleculeSpace(om[i], generatingDistanceE, resolution);
					break;
			}
			om[i].setOptions(o);
			space.generate();
			name = space.toString();
			return space;
		}

		@Override
		public String getName()
		{
			return name;
		}

		public Object run(Object data)
		{
			MoleculeSpace ms = (MoleculeSpace) data;
			int[][] n = new int[ms.size][];
			for (int i = ms.size; i-- > 0;)
			{
				ms.findNeighbours(minPts, ms.setOfObjects[i], e);
				int[] nn = new int[ms.neighbours.size];
				for (int j = ms.neighbours.size; j-- > 0;)
					nn[j] = ms.neighbours.get(j).id;
				n[i] = nn;
			}
			return n;
		}
	}

	private abstract class FindNeighboursTimingTask extends MyTimingTask
	{
		public FindNeighboursTimingTask(MS ms, OPTICSManager[] om, int minPts, float generatingDistanceE,
				int resolution, Option... options)
		{
			super(ms, om, minPts, generatingDistanceE, resolution, options);
		}

		public Object run(Object data)
		{
			MoleculeSpace ms = (MoleculeSpace) data;
			int[][] n = new int[ms.size][];
			for (int i = ms.size; i-- > 0;)
			{
				ms.findNeighbours(minPts, ms.setOfObjects[i], e);
				int[] nn = new int[ms.neighbours.size];
				for (int j = ms.neighbours.size; j-- > 0;)
					nn[j] = ms.neighbours.get(j).id;
				n[i] = nn;
			}
			return n;
		}
	}

	@Test
	public void canTestMoleculeSpaceFindNeighbours()
	{
		OPTICSManager[] om = new OPTICSManager[5];
		for (int i = 0; i < om.length; i++)
			om[i] = createOPTICSManager(size, 500);

		float generatingDistanceE = 10;
		final int minPts = 20;

		// Results
		final int[][][] n = new int[om.length][][];

		TimingService ts = new TimingService(2);
		boolean check = true;

		ts.execute(new FindNeighboursTimingTask(MS.SIMPLE, om, minPts, generatingDistanceE, 0)
		{
			public void check(int i, Object result)
			{
				// Store these as the correct results
				n[i] = format(result);
			}
		}, check);
		ts.execute(new FindNeighboursTimingTask(MS.GRID, om, minPts, generatingDistanceE, 0)
		{
			public void check(int i, Object result)
			{
				String name = getName() + ":" + i + ":";
				int[][] e = n[i];
				int[][] o = format(result);
				for (int j = 0; j < e.length; j++)
					Assert.assertArrayEquals(name + j, e[j], o[j]);
			}
		}, check);
		ts.execute(new FindNeighboursTimingTask(MS.GRID, om, minPts, generatingDistanceE, 10)
		{
			public void check(int i, Object result)
			{
				String name = getName() + ":" + i + ":";
				int[][] e = n[i];
				int[][] o = format(result);
				for (int j = 0; j < e.length; j++)
					Assert.assertArrayEquals(name + j, e[j], o[j]);
			}
		}, check);
		ts.execute(new FindNeighboursTimingTask(MS.RADIAL, om, minPts, generatingDistanceE, 0)
		{
			public void check(int i, Object result)
			{
				String name = getName() + ":" + i + ":";
				int[][] e = n[i];
				int[][] o = format(result);
				for (int j = 0; j < e.length; j++)
					Assert.assertArrayEquals(name, e[j], o[j]);
			}
		}, check);
		ts.execute(new FindNeighboursTimingTask(MS.INNER_RADIAL, om, minPts, generatingDistanceE, 0)
		{
			public void check(int i, Object result)
			{
				String name = getName() + ":" + i + ":";
				int[][] e = n[i];
				int[][] o = format(result);
				for (int j = 0; j < e.length; j++)
					Assert.assertArrayEquals(name, e[j], o[j]);
			}
		}, check);
		ts.execute(new FindNeighboursTimingTask(MS.RADIAL, om, minPts, generatingDistanceE, 10)
		{
			public void check(int i, Object result)
			{
				String name = getName() + ":" + i + ":";
				int[][] e = n[i];
				int[][] o = format(result);
				for (int j = 0; j < e.length; j++)
					Assert.assertArrayEquals(name, e[j], o[j]);
			}
		}, check);
		ts.execute(new FindNeighboursTimingTask(MS.INNER_RADIAL, om, minPts, generatingDistanceE, 10)
		{
			public void check(int i, Object result)
			{
				String name = getName() + ":" + i + ":";
				int[][] e = n[i];
				int[][] o = format(result);
				for (int j = 0; j < e.length; j++)
					Assert.assertArrayEquals(name, e[j], o[j]);
			}
		}, check);

		ts.report();
	}

	/**
	 * This test uses the auto-resolution. It is mainly used to determine when to switch inner circle processing on.
	 */
	//@Test
	public void canTestMoleculeSpaceFindNeighboursWithAutoResolution()
	{
		int molecules = 20000;
		float generatingDistanceE = 0;
		final int minPts = 20;

		double nMoleculesInPixel = (double) molecules / (size * size);
		int[] moleculesInArea = new int[] { 64, 128, 256, 512, 1024 };

		boolean check = false; // This is slow as the number of sorts in the check method is very large

		for (int m : moleculesInArea)
		{
			// Increase generatingDistance until we achieve the molecules
			double nMoleculesInCircle;
			do
			{
				generatingDistanceE += 0.1f;
				nMoleculesInCircle = Math.PI * generatingDistanceE * generatingDistanceE * nMoleculesInPixel;
			} while (nMoleculesInCircle < m && generatingDistanceE < size);

			double nMoleculesInSquare = 4 * generatingDistanceE * generatingDistanceE * nMoleculesInPixel;
			int maxResolution = (int) Math.ceil(nMoleculesInSquare);

			System.out.printf("Square=%.2f, Circle=%.2f, e=%.1f, r <= %d\n", nMoleculesInSquare, nMoleculesInCircle,
					generatingDistanceE, maxResolution);

			OPTICSManager[] om = new OPTICSManager[3];
			for (int i = 0; i < om.length; i++)
				om[i] = createOPTICSManager(size, molecules);

			// Results
			final int[][][] n = new int[om.length][][];

			TimingService ts = new TimingService(1);

			int resolution = 0;
			ts.execute(new FindNeighboursTimingTask(MS.GRID, om, minPts, generatingDistanceE, resolution)
			{
				public void check(int i, Object result)
				{
					n[i] = format(result);
				}
			}, check);
			ts.execute(new FindNeighboursTimingTask(MS.RADIAL, om, minPts, generatingDistanceE, resolution)
			{
				public void check(int i, Object result)
				{
					String name = getName() + ":" + i + ":";
					int[][] e = n[i];
					int[][] o = format(result);
					for (int j = 0; j < e.length; j++)
						Assert.assertArrayEquals(name, e[j], o[j]);
				}
			}, check);
			ts.execute(new FindNeighboursTimingTask(MS.INNER_RADIAL, om, minPts, generatingDistanceE, resolution)
			{
				public void check(int i, Object result)
				{
					String name = getName() + ":" + i + ":";
					int[][] e = n[i];
					int[][] o = format(result);
					for (int j = 0; j < e.length; j++)
						Assert.assertArrayEquals(name, e[j], o[j]);
				}
			}, check);

			ts.report();
		}
	}

	/**
	 * This tests what resolution to use for a GridMoleculeSpace
	 */
	//@Test
	public void canTestGridMoleculeSpaceFindNeighboursWithResolution()
	{
		int molecules = 50000;
		float generatingDistanceE = 0;
		final int minPts = 20;

		double nMoleculesInPixel = (double) molecules / (size * size);
		int[] moleculesInArea = new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 15, 20, 25, 30, 35, 40, 45, 50, 60, 70, 80,
				90, 100, 120, 140, 160, 200, 300 };

		for (int m : moleculesInArea)
		{
			// Increase generatingDistance until we achieve the molecules
			double nMoleculesInSquare;
			do
			{
				generatingDistanceE += 0.1f;
				nMoleculesInSquare = 4 * generatingDistanceE * generatingDistanceE * nMoleculesInPixel;
			} while (nMoleculesInSquare < m && generatingDistanceE < size);

			double nMoleculesInCircle = Math.PI * generatingDistanceE * generatingDistanceE * nMoleculesInPixel;
			int maxResolution = (int) Math.ceil(nMoleculesInSquare);

			System.out.printf("Square=%.2f, Circle=%.2f, e=%.1f, r <= %d\n", nMoleculesInSquare, nMoleculesInCircle,
					generatingDistanceE, maxResolution);

			OPTICSManager[] om = new OPTICSManager[3];
			for (int i = 0; i < om.length; i++)
				om[i] = createOPTICSManager(size, molecules);

			// Results
			final int[][][] n = new int[om.length][][];

			TimingService ts = new TimingService(1);

			double[] best = new double[] { Double.MAX_VALUE };
			TimingResult r;
			int noChange = 0;

			for (int resolution = 1; resolution <= maxResolution; resolution++)
			{
				double last = best[0];
				r = ts.execute(new FindNeighboursTimingTask(MS.GRID, om, minPts, generatingDistanceE, resolution)
				{
					public void check(int i, Object result)
					{
						n[i] = format(result);
					}
				});
				update(r, best);
				if (last == best[0])
					noChange++;
				else
					noChange = 0;
				if (noChange == 2)
					break;
			}

			//ts.check();

			ts.report();
		}
	}

	/**
	 * This tests what resolution to use for a RadialMoleculeSpace
	 */
	//@Test
	public void canTestRadialMoleculeSpaceFindNeighboursWithResolution()
	{
		int molecules = 20000;
		float generatingDistanceE = 0;
		final int minPts = 20;

		double nMoleculesInPixel = (double) molecules / (size * size);
		int[] moleculesInArea = new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 15, 20, 25, 30, 35, 40, 45, 50, 60, 70, 80,
				90, 100, 120, 140, 160, 200, 300 };

		for (int m : moleculesInArea)
		{
			// Increase generatingDistance until we achieve the molecules
			double nMoleculesInCircle;
			do
			{
				generatingDistanceE += 0.1f;
				nMoleculesInCircle = Math.PI * generatingDistanceE * generatingDistanceE * nMoleculesInPixel;
			} while (nMoleculesInCircle < m && generatingDistanceE < size);

			double nMoleculesInSquare = 4 * generatingDistanceE * generatingDistanceE * nMoleculesInPixel;
			int maxResolution = (int) Math.ceil(nMoleculesInCircle);

			System.out.printf("Square=%.2f, Circle=%.2f, e=%.1f, r <= %d\n", nMoleculesInSquare, nMoleculesInCircle,
					generatingDistanceE, maxResolution);

			OPTICSManager[] om = new OPTICSManager[3];
			for (int i = 0; i < om.length; i++)
				om[i] = createOPTICSManager(size, molecules);

			// Results
			final int[][][] n = new int[om.length][][];

			TimingService ts = new TimingService(1);

			double[] best = new double[] { Double.MAX_VALUE };
			TimingResult r;
			int noChange = 0;

			for (int resolution = 1; resolution <= maxResolution; resolution++)
			{
				double last = best[0];
				r = ts.execute(new FindNeighboursTimingTask(MS.RADIAL, om, minPts, generatingDistanceE, resolution)
				{
					public void check(int i, Object result)
					{
						n[i] = format(result);
					}
				});
				update(r, best);
				if (last == best[0])
					noChange++;
				else
					noChange = 0;
				if (noChange == 2)
					break;
			}

			//ts.check();

			ts.report();
		}
	}

	/**
	 * This tests what resolution to use for a InnerRadialMoleculeSpace
	 */
	//@Test
	public void canTestInnerRadialMoleculeSpaceFindNeighboursWithResolution()
	{
		int molecules = 20000;
		float generatingDistanceE = 0;
		final int minPts = 20;

		double nMoleculesInPixel = (double) molecules / (size * size);
		int[] moleculesInArea = new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 15, 20, 25, 30, 35, 40, 45, 50, 60, 70, 80,
				90, 100, 150, 200, 300, 500, 1000 };

		int lastMax = 0;
		for (int m : moleculesInArea)
		{
			// Increase generatingDistance until we achieve the molecules
			double nMoleculesInCircle;
			do
			{
				generatingDistanceE += 0.1f;
				nMoleculesInCircle = Math.PI * generatingDistanceE * generatingDistanceE * nMoleculesInPixel;
			} while (nMoleculesInCircle < m && generatingDistanceE < size);

			double nMoleculesInSquare = 4 * generatingDistanceE * generatingDistanceE * nMoleculesInPixel;
			int maxResolution = (int) Math.ceil(nMoleculesInCircle);

			System.out.printf("Square=%.2f, Circle=%.2f, e=%.1f, r <= %d\n", nMoleculesInSquare, nMoleculesInCircle,
					generatingDistanceE, maxResolution);

			OPTICSManager[] om = new OPTICSManager[3];
			for (int i = 0; i < om.length; i++)
				om[i] = createOPTICSManager(size, molecules);

			// Results
			final int[][][] n = new int[om.length][][];

			TimingService ts = new TimingService(1);

			double[] best = new double[] { Double.MAX_VALUE };
			TimingResult r;
			int noChange = 0;

			for (int resolution = Math.max(1, lastMax - 3); resolution <= maxResolution; resolution++)
			{
				double last = best[0];
				r = ts.execute(
						new FindNeighboursTimingTask(MS.INNER_RADIAL, om, minPts, generatingDistanceE, resolution)
						{
							public void check(int i, Object result)
							{
								n[i] = format(result);
							}
						});
				update(r, best);
				if (last == best[0])
					noChange++;
				else
				{
					noChange = 0;
					lastMax = resolution;
				}
				if (noChange == 2)
					break;
			}

			//ts.check();

			ts.report();
		}
	}

	//@Test
	public void canBuildCircularKernelAtDifferentResolutions()
	{
		// Note: The radius of the default circle is 1 => 
		// Circle Area = pi
		// Square Area = 4

		for (int r = 1; r <= 100; r++)
		{
			CircularKernelOffset[] offset = CircularKernelOffset.create(r);
			int size = offset.length * offset.length;
			double pixelArea = 4.0 / (size);
			// Count pixels for the outer/inner circles
			int outer = 0, inner = 0;
			for (CircularKernelOffset o : offset)
			{
				outer += Math.max(0, o.end - o.start);
				if (o.internal)
					inner += o.endInternal - o.startInternal;
			}
			double outerArea = outer * pixelArea;
			double innerArea = inner * pixelArea;
			int skip = size - outer;
			System.out.printf("R=%d, outer=%d  %f (%f), Skip=%d  (%f), inner=%d  %f (%f)\n", r, outer, outerArea,
					outerArea / Math.PI, skip, (double) skip / size, inner, innerArea, innerArea / outerArea);

			// Test for symmetry
			int w = offset.length;
			boolean[] outerMask = new boolean[w * w];
			boolean[] innerMask = new boolean[outerMask.length];
			for (int i = 0, k = 0; i < offset.length; i++)
			{
				for (int j = -r; j <= r; j++, k++)
				{
					if (j >= offset[i].start && j < offset[i].end)
						outerMask[k] = true;
					if (j >= offset[i].startInternal && j < offset[i].endInternal)
						innerMask[k] = true;
				}
			}
			for (int y = 0, k = 0; y < w; y++)
			{
				for (int x = 0; x < w; x++, k++)
				{
					Assert.assertTrue("No outer symmetry", outerMask[k] == outerMask[x * w + y]);
				}
			}
			double e = r * r;
			for (int y = 0, k = 0; y < w; y++)
			{
				for (int x = 0; x < w; x++, k++)
				{
					Assert.assertTrue("No inner symmetry", innerMask[k] == innerMask[x * w + y]);
					// Test distance to centre (r,r)
					if (innerMask[k])
					{
						Assert.assertTrue("Bad inner", Maths.distance2(x, y, r, r) <= e);
					}
				}
			}
		}
	}

	private void update(TimingResult r, double[] best)
	{
		double time = r.getMean();
		if (best[0] > time)
			best[0] = time;
	}

	private int[][] format(Object result)
	{
		int[][] n = (int[][]) result;
		for (int i = 0; i < n.length; i++)
			Arrays.sort(n[i]);
		return n;
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
