/*-
 * #%L
 * Genome Damage and Stability Centre ImageJ Core Package
 * 
 * Contains code used by:
 * 
 * GDSC ImageJ Plugins - Microscopy image analysis
 * 
 * GDSC SMLM ImageJ Plugins - Single molecule localisation microscopy (SMLM)
 * %%
 * Copyright (C) 2011 - 2018 Alex Herbert
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
package gdsc.core.clustering.optics;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.commons.math3.random.RandomGenerator;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import de.lmu.ifi.dbs.elki.algorithm.clustering.optics.AbstractOPTICS;
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
import de.lmu.ifi.dbs.elki.database.datastore.WritableDataStore;
import de.lmu.ifi.dbs.elki.database.datastore.WritableDoubleDataStore;
import de.lmu.ifi.dbs.elki.database.ids.ArrayModifiableDBIDs;
import de.lmu.ifi.dbs.elki.database.ids.DBIDFactory;
import de.lmu.ifi.dbs.elki.database.ids.DBIDIter;
import de.lmu.ifi.dbs.elki.database.ids.DBIDRef;
import de.lmu.ifi.dbs.elki.database.ids.DBIDUtil;
import de.lmu.ifi.dbs.elki.database.ids.DBIDVar;
import de.lmu.ifi.dbs.elki.database.ids.DBIDs;
import de.lmu.ifi.dbs.elki.database.ids.ModifiableDBIDs;
import de.lmu.ifi.dbs.elki.database.relation.DoubleRelation;
import de.lmu.ifi.dbs.elki.database.relation.Relation;
import de.lmu.ifi.dbs.elki.datasource.ArrayAdapterDatabaseConnection;
import de.lmu.ifi.dbs.elki.datasource.DatabaseConnection;
import de.lmu.ifi.dbs.elki.distance.distancefunction.minkowski.EuclideanDistanceFunction;
import de.lmu.ifi.dbs.elki.index.preprocessed.fastoptics.RandomProjectedNeighborsAndDensities;
import de.lmu.ifi.dbs.elki.math.MathUtil;
import de.lmu.ifi.dbs.elki.math.random.RandomFactory;
import de.lmu.ifi.dbs.elki.result.outlier.OutlierResult;
import de.lmu.ifi.dbs.elki.utilities.ClassGenericsUtil;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameterization.ListParameterization;
import gdsc.core.clustering.optics.OPTICSManager.Option;
import gdsc.core.logging.ConsoleLogger;
import gdsc.core.logging.NullTrackProgress;
import gdsc.core.logging.TrackProgress;
import gdsc.core.match.RandIndex;
import gdsc.core.utils.Maths;
import gdsc.core.utils.PartialSort;
import gdsc.test.BaseTimingTask;
import gdsc.test.TestAssert;
import gdsc.test.TestSettings;
import gdsc.test.TestSettings.LogLevel;
import gdsc.test.TestSettings.TestComplexity;
import gdsc.test.TimingResult;
import gdsc.test.TimingService;

@SuppressWarnings({ "javadoc" })
public class OPTICSManagerTest
{
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
				final Molecule m = new DistanceMolecule(i, x, y);
				setOfObjects[i] = m;
				for (int j = i; j-- > 0;)
					d[i][j] = d[j][i] = m.distance2(setOfObjects[j]);
			}

			return setOfObjects;
		}

		void createDD()
		{
			final double[][] doubleData = opticsManager.getDoubleData();
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
			final float[] fdata = d[object.id];
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
			final float[] fdata = d[object.id];
			neighbours.clear();
			for (int i = 0; i < fdata.length; i++)
				if (fdata[i] < e)
				{
					setOfObjects[i].setD(fdata[i]);
					neighbours.add(setOfObjects[i]);
				}
			//if (neighbours.size < minPts)
			//	neighbours.clear();
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
			final WritableDoubleDataStore davg = DataStoreUtil.makeDoubleStorage(points.getDBIDs(),
					DataStoreFactory.HINT_HOT);
			for (final DBIDIter it = points.getDBIDs().iter(); it.valid(); it.advance())
			{
				//float[] fdata = space.d[asInteger(it)];
				//double[] data = new StoredDataStatistics(fdata).getValues();

				final double[] data = space.dd[asInteger(it)];

				// Simple sort
				//double[] dd = data.clone();
				//Arrays.sort(dd);
				//d = dd[minPts - 1];

				// Partial sort
				//double d = Math.sqrt(PartialSort.bottom(PartialSort.OPTION_HEAD_FIRST, data, minPts)[0]);
				final double d = PartialSort.bottom(PartialSort.OPTION_HEAD_FIRST, data, minPts)[0];

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
	 * This is injected into the ELKI framework to output the same distances and neighbours
	 */
	private class CopyRandomProjectedNeighborsAndDensities extends RandomProjectedNeighborsAndDensities<DoubleVector>
	{
		// All-vs-all distance matrix
		ProjectedMoleculeSpace space;
		Relation<DoubleVector> points;

		public CopyRandomProjectedNeighborsAndDensities(ProjectedMoleculeSpace space)
		{
			super(RandomFactory.get(30051977l));
			this.space = space;
		}

		// Override the methods used by optics

		@Override
		public void computeSetsBounds(Relation<DoubleVector> points, int minSplitSize, DBIDs ptList)
		{
			// Store the points
			this.points = points;
		}

		@Override
		public DoubleDataStore computeAverageDistInSet()
		{
			// Use the same core distance calculated by the space

			final WritableDoubleDataStore davg = DataStoreUtil.makeDoubleStorage(points.getDBIDs(),
					DataStoreFactory.HINT_HOT);
			for (final DBIDIter it = points.getDBIDs().iter(); it.valid(); it.advance())
			{
				final int id = asInteger(it);
				final Molecule m = space.setOfObjects[id];
				final double d = (m.coreDistance != OPTICSManager.UNDEFINED) ? m.getCoreDistance()
						: FastOPTICS.UNDEFINED_DISTANCE;
				davg.put(it, d);
			}
			return davg;
		}

		@Override
		public DataStore<? extends DBIDs> getNeighs()
		{
			// Use the same neighbours calculated by the space

			final DBIDs ids = points.getDBIDs();
			// init lists
			final WritableDataStore<ModifiableDBIDs> neighs = DataStoreUtil.makeStorage(ids, DataStoreFactory.HINT_HOT,
					ModifiableDBIDs.class);
			for (final DBIDIter it = ids.iter(); it.valid(); it.advance())
				neighs.put(it, DBIDUtil.newHashSet());

			final DBIDVar v = DBIDUtil.newVar();
			for (int i = space.allNeighbours.length; i-- > 0;)
			{
				final int[] list = space.allNeighbours[i];
				final ArrayModifiableDBIDs nids = DBIDUtil.newArray(list.length);
				for (final int id : list)
					nids.add(DBIDFactory.FACTORY.importInteger(id));
				v.assignVar(i, v);
				neighs.get(v).addDBIDs(nids);
			}

			return neighs;
		}
	}

	/**
	 * Test the results of OPTICS using the ELKI framework
	 */
	@Test
	public void canComputeOPTICS()
	{
		// This does not fail but logs warnings

		final RandomGenerator rg = TestSettings.getRandomGenerator();
		final TrackProgress tracker = null; //new SimpleTrackProgress();
		final int[] minPoints = (TestSettings.allow(TestComplexity.LOW)) ? new int[] { 5, 10 } : new int[] { 10 };
		for (final int n : new int[] { 100, 500 })
		{
			final OPTICSManager om = createOPTICSManager(size, n, rg);
			om.setTracker(tracker);
			// Needed to match the ELKI framework
			om.setOptions(Option.OPTICS_STRICT_REVERSE_ID_ORDER);

			final SimpleMoleculeSpace space = new SimpleMoleculeSpace(om, 0);
			space.createDD();

			// Use ELKI to provide the expected results
			final double[][] data = new Array2DRowRealMatrix(om.getDoubleData()).transpose().getData();

			for (final int minPts : minPoints)
			{
				// Reset starting Id to 1
				final DatabaseConnection dbc = new ArrayAdapterDatabaseConnection(data, null, 0);
				final ListParameterization params = new ListParameterization();
				params.addParameter(AbstractDatabase.Parameterizer.DATABASE_CONNECTION_ID, dbc);
				final Database db = ClassGenericsUtil.parameterizeOrAbort(StaticArrayDatabase.class, params);
				db.initialize();
				final Relation<?> rel = db.getRelation(TypeUtil.ANY);
				Assert.assertEquals("Database size does not match.", n, rel.size());

				// Debug: Print the core distance for each point
				//for (int i = 0; i < n; i++)
				//{
				//	double[] dd = d[i].clone();
				//	Arrays.sort(dd);
				//	TestSettings.info("%d Core %f, next %f\n", i, dd[minPts - 1], dd[minPts]);
				//}

				// Use max range
				final OPTICSResult r1 = om.optics(size, minPts);

				// Test verses the ELKI frame work
				final RandomProjectedNeighborsAndDensities<DoubleVector> index = new CheatingRandomProjectedNeighborsAndDensities(
						space, minPts);
				final FastOPTICS<DoubleVector> fo = new FastOPTICS<>(minPts, index);
				final ClusterOrder order = fo.run(db);

				// Check
				int i = 0;
				final DBIDVar pre = DBIDUtil.newVar();
				for (final DBIDIter it = order.iter(); it.valid(); it.advance(), i++)
				{
					if (i == 0)
						// No predecessor or reachability distance
						continue;

					final int expId = asInteger(it);
					final int obsId = r1.get(i).parent;

					order.getPredecessor(it, pre);
					final int expPre = asInteger(pre);
					final int obsPre = r1.get(i).predecessor;

					final double expR = order.getReachability(it);
					final double obsR = r1.get(i).reachabilityDistance;

					//TestSettings.debug("[%d] %d %d : %f = %f (%f) : %s = %d\n", i, expId, obsId, expR, obsR,
					//		r1.get(i).coreDistance, expPre, obsPre);

					TestAssert.assertEquals(expId, obsId, "[%d] Id", i);
					TestAssert.assertEquals(expPre, obsPre, "[%d] Pre", i);
					TestAssert.assertEqualsRelative(expR, obsR, 1e-5, "[%d] R", i);
				}
			}
		}
	}

	private static int asInteger(DBIDRef id)
	{
		return DBIDUtil.asInteger(id);
	}

	/**
	 * Test the results of Fast OPTICS using the ELKI framework
	 */
	@Test
	public void canComputeFastOPTICS()
	{
		final RandomGenerator rg = TestSettings.getRandomGenerator();
		final TrackProgress tracker = null; //new SimpleTrackProgress();
		final int[] minPoints = (TestSettings.allow(TestComplexity.LOW)) ? new int[] { 5, 10 } : new int[] { 10 };
		for (final int n : new int[] { 100, 500 })
		{
			final OPTICSManager om = createOPTICSManager(size, n, rg);
			om.setTracker(tracker);
			// Needed to match the ELKI framework
			om.setOptions(Option.OPTICS_STRICT_REVERSE_ID_ORDER, Option.CACHE);

			final SimpleMoleculeSpace space = new SimpleMoleculeSpace(om, 0);
			space.createDD();

			// Use ELKI to provide the expected results
			final double[][] data = new Array2DRowRealMatrix(om.getDoubleData()).transpose().getData();

			for (final int minPts : minPoints)
			{
				// Reset starting Id to 1
				final DatabaseConnection dbc = new ArrayAdapterDatabaseConnection(data, null, 0);
				final ListParameterization params = new ListParameterization();
				params.addParameter(AbstractDatabase.Parameterizer.DATABASE_CONNECTION_ID, dbc);
				final Database db = ClassGenericsUtil.parameterizeOrAbort(StaticArrayDatabase.class, params);
				db.initialize();
				final Relation<?> rel = db.getRelation(TypeUtil.ANY);
				Assert.assertEquals("Database size does not match.", n, rel.size());

				// Debug: Print the core distance for each point
				//for (int i = 0; i < n; i++)
				//{
				//	double[] dd = d[i].clone();
				//	Arrays.sort(dd);
				//	TestSettings.info("%d Core %f, next %f\n", i, dd[minPts - 1], dd[minPts]);
				//}

				final OPTICSResult r1 = om.fastOptics(minPts);

				// Test verses the ELKI frame work
				final RandomProjectedNeighborsAndDensities<DoubleVector> index = new CopyRandomProjectedNeighborsAndDensities(
						(ProjectedMoleculeSpace) om.grid);
				final FastOPTICS<DoubleVector> fo = new FastOPTICS<>(minPts, index);
				final ClusterOrder order = fo.run(db);

				// Check
				int i = 0;
				final DBIDVar pre = DBIDUtil.newVar();
				for (final DBIDIter it = order.iter(); it.valid(); it.advance(), i++)
				{
					if (i == 0)
						// No predecessor or reachability distance
						continue;

					final String prefix = "[" + i + "] ";

					final int expId = asInteger(it);
					final int obsId = r1.get(i).parent;

					order.getPredecessor(it, pre);
					final int expPre = asInteger(pre);
					final int obsPre = r1.get(i).predecessor;

					final double expR = order.getReachability(it);
					final double obsR = r1.get(i).reachabilityDistance;

					//TestSettings.debug("[%d] %d %d : %f = %f (%f) : %s = %d\n", i, expId, obsId, expR, obsR,
					//		r1.get(i).coreDistance, expPre, obsPre);

					Assert.assertEquals(prefix + "Id", expId, obsId);
					Assert.assertEquals(prefix + "Pre", expPre, obsPre);
					Assert.assertEquals(prefix + "R", expR, obsR, expR * 1e-5);
				}
			}
		}
	}

	/**
	 * Test the results of OPTICS using the ELKI framework
	 */
	@Test
	public void canComputeOPTICSXi()
	{
		// This does not fail but logs warnings

		final RandomGenerator rg = TestSettings.getRandomGenerator();
		final TrackProgress tracker = null; //new SimpleTrackProgress();
		final int[] minPoints = (TestSettings.allow(TestComplexity.LOW)) ? new int[] { 5, 10 } : new int[] { 10 };
		for (final int n : new int[] { 100, 500 })
		{
			final OPTICSManager om = createOPTICSManager(size, n, rg);
			om.setTracker(tracker);
			// Needed to match the ELKI framework
			om.setOptions(Option.OPTICS_STRICT_REVERSE_ID_ORDER);

			// Compute the all-vs-all distance for checking the answer
			final SimpleMoleculeSpace space = new SimpleMoleculeSpace(om, 0);
			space.createDD();

			// Use ELKI to provide the expected results
			final double[][] data = new Array2DRowRealMatrix(om.getDoubleData()).transpose().getData();

			for (final int minPts : minPoints)
			{
				// Reset starting Id to 1
				final DatabaseConnection dbc = new ArrayAdapterDatabaseConnection(data, null, 0);
				final ListParameterization params = new ListParameterization();
				params.addParameter(AbstractDatabase.Parameterizer.DATABASE_CONNECTION_ID, dbc);
				final Database db = ClassGenericsUtil.parameterizeOrAbort(StaticArrayDatabase.class, params);
				db.initialize();
				final Relation<?> rel = db.getRelation(TypeUtil.ANY);
				Assert.assertEquals("Database size does not match.", n, rel.size());

				// Debug: Print the core distance for each point
				//for (int i = 0; i < n; i++)
				//{
				//	double[] dd = d[i].clone();
				//	Arrays.sort(dd);
				//	TestSettings.info("%d Core %f, next %f\n", i, dd[minPts - 1], dd[minPts]);
				//}

				// Use max range
				final OPTICSResult r1 = om.optics(size, minPts);

				// Test verses the ELKI frame work
				final RandomProjectedNeighborsAndDensities<DoubleVector> index = new CheatingRandomProjectedNeighborsAndDensities(
						space, minPts);
				final FastOPTICS<DoubleVector> fo = new FastOPTICS<>(minPts, index);

				final double xi = 0.03;

				final OPTICSXi opticsXi = new OPTICSXi(fo, xi, false, false);
				final Clustering<OPTICSModel> clustering = opticsXi.run(db);

				// Check by building the clusters into an array
				final int[] expClusters = new int[n];
				final List<de.lmu.ifi.dbs.elki.data.Cluster<OPTICSModel>> allClusters = clustering.getAllClusters();
				int clusterId = 0;
				for (final de.lmu.ifi.dbs.elki.data.Cluster<OPTICSModel> c : allClusters)
				{
					//TestSettings.debug("%d-%d\n", c.getModel().getStartIndex(), c.getModel().getEndIndex());

					// Add the cluster Id to the expClusters
					clusterId++;
					for (final DBIDIter it = c.getIDs().iter(); it.valid(); it.advance())
						expClusters[asInteger(it)] = clusterId;
				}

				// check the clusters match
				r1.extractClusters(xi);
				final int[] obsClusters = r1.getClusters();

				//for (int i = 0; i < n; i++)
				//	TestSettings.info("%d = %d %d\n", i, expClusters[i], obsClusters[i]);

				Assert.assertEquals(1, RandIndex.randIndex(expClusters, obsClusters), 0);
			}
		}
	}

	@Test
	public void canComputeOPTICSXiWithNoHierarchy()
	{
		final RandomGenerator rg = TestSettings.getRandomGenerator();
		final int[] minPoints = (TestSettings.allow(TestComplexity.LOW)) ? new int[] { 5, 10 } : new int[] { 10 };
		for (final int n : new int[] { 100, 500 })
		{
			final OPTICSManager om = createOPTICSManager(size, n, rg);

			for (final int minPts : minPoints)
			{
				final OPTICSResult r1 = om.optics(0, minPts);

				final double xi = 0.03;

				// check the clusters match
				r1.extractClusters(xi);
				final ArrayList<OPTICSCluster> o1 = r1.getAllClusters();

				r1.extractClusters(xi, OPTICSResult.XI_OPTION_TOP_LEVEL);
				final ArrayList<OPTICSCluster> o2 = r1.getAllClusters();

				Assert.assertTrue(o1.size() >= o2.size());

				//TestSettings.debug("%d : %d\n", n, minPts);
				for (final OPTICSCluster cluster : o2)
					Assert.assertTrue(cluster.getLevel() == 0);
				//TestSettings.debug(cluster);
			}
		}
	}

	/**
	 * Test the results of FastOPTICS using the ELKI framework
	 */
	@Test
	public void canComputeSimilarFastOPTICSToELKI()
	{
		// This does not fail but logs warnings

		final RandomGenerator rg = TestSettings.getRandomGenerator();
		final int nLoops = (TestSettings.allow(TestComplexity.LOW)) ? 5 : 1;
		final RandIndex ri = new RandIndex();
		final TrackProgress tracker = null; //new SimpleTrackProgress();
		for (final int n : new int[] { 500 })
		{
			final OPTICSManager om = createOPTICSManager(size, n, rg);
			om.setTracker(tracker);
			// Needed to match the ELKI framework
			om.setOptions(Option.OPTICS_STRICT_REVERSE_ID_ORDER);

			// Use ELKI to provide the expected results
			final double[][] data = new Array2DRowRealMatrix(om.getDoubleData()).transpose().getData();

			for (final int minPts : new int[] { 4 })
			{
				double sum = 0;
				for (int loop = 0; loop < nLoops; loop++)
				{
					final long seed = TestSettings.getSeed() + loop + 1;
					// Reset starting Id to 1
					final DatabaseConnection dbc = new ArrayAdapterDatabaseConnection(data, null, 0);
					ListParameterization params = new ListParameterization();
					params.addParameter(AbstractDatabase.Parameterizer.DATABASE_CONNECTION_ID, dbc);
					final Database db = ClassGenericsUtil.parameterizeOrAbort(StaticArrayDatabase.class, params);
					db.initialize();
					final Relation<?> rel = db.getRelation(TypeUtil.ANY);
					Assert.assertEquals("Database size does not match.", n, rel.size());

					// Use the same settings as ELKI
					final int logOProjectionConst = 20;
					final int dim = 2;
					final int nSplits = (int) (logOProjectionConst * MathUtil.log2(size * dim + 1));
					final int nProjections = nSplits;
					final boolean useRandomVectors = true;
					final boolean saveApproximateSets = true;
					final SampleMode sampleMode = SampleMode.MEDIAN;
					om.setRandomSeed(seed);
					final OPTICSResult r1 = om.fastOptics(minPts, nSplits, nProjections, useRandomVectors,
							saveApproximateSets, sampleMode);

					// Test verses the ELKI frame work
					params = new ListParameterization();
					params.addParameter(AbstractOPTICS.Parameterizer.MINPTS_ID, minPts);
					params.addParameter(RandomProjectedNeighborsAndDensities.Parameterizer.RANDOM_ID, seed);
					final Class<FastOPTICS<DoubleVector>> clz = ClassGenericsUtil
							.uglyCastIntoSubclass(FastOPTICS.class);
					final FastOPTICS<DoubleVector> fo = params.tryInstantiate(clz);

					final double xi = 0.03;

					final OPTICSXi opticsXi = new OPTICSXi(fo, xi, false, false);
					final Clustering<OPTICSModel> clustering = opticsXi.run(db);

					// Check by building the clusters into an array
					final int[] expClusters = new int[n];
					final List<de.lmu.ifi.dbs.elki.data.Cluster<OPTICSModel>> allClusters = clustering.getAllClusters();
					int clusterId = 0;
					for (final de.lmu.ifi.dbs.elki.data.Cluster<OPTICSModel> c : allClusters)
					{
						// Add the cluster Id to the expClusters
						clusterId++;
						for (final DBIDIter it = c.getIDs().iter(); it.valid(); it.advance())
							expClusters[asInteger(it)] = clusterId;
					}

					// check the clusters match
					r1.extractClusters(xi);
					final int[] obsClusters = r1.getClusters();

					//for (int i = 0; i < n; i++)
					//	TestSettings.debug("%d = %d %d\n", i, expClusters[i], obsClusters[i]);

					// Should be similar
					ri.compute(expClusters, obsClusters);

					final double r = ri.getRandIndex();
					if (ri.getAdjustedRandIndex() > 0)
						TestSettings.info("FastOPTICS vs ELKI : %d,%d : [%d] r=%f (%f)\n", n, minPts, loop, r,
								ri.getAdjustedRandIndex());
					else
						TestSettings.log(LogLevel.SILENT, "WARNING : FastOPTICS vs ELKI : %d,%d : [%d] r=%f (%f)\n", n,
								minPts, loop, r, ri.getAdjustedRandIndex());
					//Assert.assertTrue(ri.getAdjustedRandIndex() > 0);
					sum += r;
				}

				sum /= nLoops;
				if (sum > 0.6)
					TestSettings.info("FastOPTICS vs ELKI : %d,%d : r=%f\n", n, minPts, sum);
				else
					TestSettings.log(LogLevel.SILENT, "WARNING : FastOPTICS vs ELKI : %d,%d : r=%f\n", n, minPts, sum);
				//Assert.assertTrue(sum > 0.6);
			}
		}
	}

	@Test
	public void canComputeFastOPTICSFasterThanELKI()
	{
		TestSettings.assumeSpeedTest();

		final RandomGenerator rg = TestSettings.getRandomGenerator();
		final TrackProgress tracker = null; //new SimpleTrackProgress();
		for (final int n : new int[] { 2000 })
		{
			final OPTICSManager om = createOPTICSManager(size, n, rg);
			om.setTracker(tracker);
			// Needed to match the ELKI framework
			om.setOptions(Option.OPTICS_STRICT_REVERSE_ID_ORDER);

			// Use ELKI to provide the expected results
			final double[][] data = new Array2DRowRealMatrix(om.getDoubleData()).transpose().getData();

			for (final int minPts : new int[] { 4 })
			{
				// Reset starting Id to 1
				final DatabaseConnection dbc = new ArrayAdapterDatabaseConnection(data, null, 0);
				ListParameterization params = new ListParameterization();
				params.addParameter(AbstractDatabase.Parameterizer.DATABASE_CONNECTION_ID, dbc);
				final Database db = ClassGenericsUtil.parameterizeOrAbort(StaticArrayDatabase.class, params);
				db.initialize();
				final Relation<?> rel = db.getRelation(TypeUtil.ANY);
				Assert.assertEquals("Database size does not match.", n, rel.size());

				// Use same settings as ELKI
				final int logOProjectionConst = 20;
				final int dim = 2;
				final int nSplits = (int) (logOProjectionConst * MathUtil.log2(size * dim + 1));
				final int nProjections = nSplits;
				final boolean useRandomVectors = true;
				final boolean saveApproximateSets = true;
				final SampleMode sampleMode = SampleMode.MEDIAN;

				// Test verses the ELKI frame work
				params = new ListParameterization();
				params.addParameter(AbstractOPTICS.Parameterizer.MINPTS_ID, minPts);
				params.addParameter(RandomProjectedNeighborsAndDensities.Parameterizer.RANDOM_ID, 1);
				final Class<FastOPTICS<DoubleVector>> clz = ClassGenericsUtil.uglyCastIntoSubclass(FastOPTICS.class);
				final FastOPTICS<DoubleVector> fo = params.tryInstantiate(clz);

				final double xi = 0.03;
				final OPTICSXi opticsXi = new OPTICSXi(fo, xi, false, false);

				final long t1 = System.nanoTime();
				opticsXi.run(db);
				final long t2 = System.nanoTime();
				om.fastOptics(minPts, nSplits, nProjections, useRandomVectors, saveApproximateSets, sampleMode);
				final long t3 = System.nanoTime();
				om.fastOptics(minPts);
				final long t4 = System.nanoTime();

				final long elki = t2 - t1;
				final long smlm1 = t3 - t2;
				final long smlm2 = t4 - t3;
				TestSettings.logSpeedTestResult(smlm1 < elki, "ELKI = %d, SMLM = %d = %f\n", elki, smlm1,
						elki / (double) smlm1);
				TestSettings.logSpeedTestResult(smlm2 < elki, "ELKI = %d, SMLM (default) = %d = %f\n", elki, smlm2,
						elki / (double) smlm2);
				//Assert.assertTrue(smlm1 < elki);
				//Assert.assertTrue(smlm2 < elki);
			}
		}
	}

	@Test
	public void canComputeSimilarFastOPTICSTopLevelClusters()
	{
		canComputeSimilarFastOPTICS(0, 0.7);
	}

	@Test
	public void canComputeSimilarFastOPTICSXi()
	{
		canComputeSimilarFastOPTICS(0.03, 0.5);
	}

	private void canComputeSimilarFastOPTICS(double xi, double randMin)
	{
		final RandomGenerator rg = TestSettings.getRandomGenerator();
		final RandIndex ri = new RandIndex();
		final TrackProgress tracker = null; //new SimpleTrackProgress();
		final boolean[] both = new boolean[] { true, false };
		final int[] minPoints = (TestSettings.allow(TestComplexity.LOW)) ? new int[] { 5, 10 } : new int[] { 10 };

		for (final int n : new int[] { 500 })
		{
			final OPTICSManager om = createOPTICSManager(size, n, rg);
			om.setTracker(tracker);
			om.setOptions(Option.OPTICS_STRICT_ID_ORDER);
			om.setRandomSeed(TestSettings.getSeed());

			for (final int minPts : minPoints)
			{
				// Default using ALL
				final int[] c1 = runFastOPTICS(om, xi, minPts, 0, 0, false, false, SampleMode.ALL);

				final int nSplits = 0;
				final int nProjections = 0;
				// @formatter:off
				for (final SampleMode sampleMode : SampleMode.values())
				{
					double sum = 0;
					int c = 0;
    				for (final boolean useRandomVectors : both)
    				for (final boolean saveApproximateSets : both)
    				{
    					final int[] c2 = runFastOPTICS(om, xi, minPts, nSplits, nProjections, useRandomVectors,
    							saveApproximateSets, sampleMode);

    					// Should be similar
    					final double r = ri.getRandIndex(c1, c2);
    					sum += r;
    					c++;
    					final double ari = ri.getAdjustedRandIndex();
    					TestSettings.info(
    							"xi=%f, n=%d, minPts=%d, splits=%d, projections=%d, randomVectors=%b, approxSets=%b, sampleMode=%s : r=%f (%f)\n",
    							xi, n, minPts, nSplits, nProjections, useRandomVectors, saveApproximateSets, sampleMode, r, ari);
    					// This should always be true, i.e. better than chance
    					TestAssert.assertTrue(0 < ari, "Adjusted rand index is below zero: %s", sampleMode);
    				}
    				final double r = sum / c;
					TestSettings.info(
							"xi=%f, n=%d, minPts=%d, splits=%d, projections=%d, sampleMode=%s : r=%f\n",
							xi, n, minPts, nSplits, nProjections, sampleMode, r);
					// This may fail with certain random seeds
					if (randMin < r)
						TestSettings.info(
								"xi=%f, n=%d, minPts=%d, splits=%d, projections=%d, sampleMode=%s : r=%f\n",
								xi, n, minPts, nSplits, nProjections, sampleMode, r);
					else TestSettings.log(LogLevel.SILENT,
								"WARNING : Not similar fastOPTICS : xi=%f, n=%d, minPts=%d, splits=%d, projections=%d, sampleMode=%s : r=%f\n",
								xi, n, minPts, nSplits, nProjections, sampleMode, r);
				}
				// @formatter:on
			}
		}
	}

	private static int[] runFastOPTICS(OPTICSManager om, double xi, int minPts, int nSplits, int nProjections,
			boolean useRandomVectors, boolean saveApproximateSets, SampleMode sampleMode)
	{
		final OPTICSResult r1 = om.fastOptics(minPts, nSplits, nProjections, useRandomVectors, saveApproximateSets,
				sampleMode);
		if (xi > 0)
			r1.extractClusters(xi);
		return r1.getClusters();
	}

	// The following tests are optional since they test non-default OPTICS processing

	@Test
	public void canComputeOPTICSWithInnerProcessing()
	{
		TestSettings.assumeMediumComplexity();
		canComputeOPTICSWithOptions(Option.INNER_PROCESSING);
	}

	@Test
	public void canComputeOPTICSWithCircularProcessing()
	{
		TestSettings.assumeMediumComplexity();
		canComputeOPTICSWithOptions(Option.CIRCULAR_PROCESSING);
	}

	@Test
	public void canComputeOPTICSWithInnerCircularProcessing()
	{
		TestSettings.assumeMediumComplexity();
		canComputeOPTICSWithOptions(Option.INNER_PROCESSING, Option.CIRCULAR_PROCESSING);
	}

	@Test
	public void canComputeOPTICSWithSimpleQueue()
	{
		TestSettings.assumeMediumComplexity();
		// This fails as the order is different when we do not use ID to order the objects when reachability distance is equal
		//canComputeOPTICSWithOptions(Option.OPTICS_SIMPLE_PRIORITY_QUEUE);

		// We can do a simple check that the cluster ID and core distance are the same for each object.
		// Since the processing order is different we cannot check the reachability distance or the predecessor.
		canComputeOPTICSWithOptions(true, Option.OPTICS_SIMPLE_PRIORITY_QUEUE);
	}

	@Test
	public void canComputeOPTICSWithSimpleQueueReverseIdOrderD()
	{
		TestSettings.assumeMediumComplexity();
		canComputeOPTICSWithOptions(new Option[] { Option.OPTICS_STRICT_REVERSE_ID_ORDER },
				Option.OPTICS_SIMPLE_PRIORITY_QUEUE);
	}

	@Test
	public void canComputeOPTICSWithSimpleQueueIdOrder()
	{
		TestSettings.assumeMediumComplexity();
		canComputeOPTICSWithOptions(new Option[] { Option.OPTICS_STRICT_ID_ORDER },
				Option.OPTICS_SIMPLE_PRIORITY_QUEUE);
	}

	private void canComputeOPTICSWithOptions(Option... options)
	{
		canComputeOPTICSWithOptions(false, options);
	}

	private void canComputeOPTICSWithOptions(boolean simpleCheck, Option... options)
	{
		final RandomGenerator rg = TestSettings.getRandomGenerator();
		final int[] minPoints = (TestSettings.allow(TestComplexity.LOW)) ? new int[] { 5, 10 } : new int[] { 10 };
		for (final int n : new int[] { 100, 500 })
		{
			final OPTICSManager om1 = createOPTICSManager(size, n, rg);
			final OPTICSManager om2 = om1.clone();
			om2.setOptions(options);

			for (final int minPts : minPoints)
			{
				// Use max range
				final OPTICSResult r1 = om1.optics(0, minPts);
				final OPTICSResult r1b = om1.optics(0, minPts);
				final OPTICSResult r2 = om2.optics(0, minPts);

				areEqual("repeat", r1, r1b);
				if (simpleCheck)
					areEqualClusters(r1, r2);
				else
					areEqual("new", r1, r2);
			}
		}
	}

	private void canComputeOPTICSWithOptions(Option[] options1, Option... options)
	{
		final RandomGenerator rg = TestSettings.getRandomGenerator();
		final int[] minPoints = (TestSettings.allow(TestComplexity.LOW)) ? new int[] { 5, 10 } : new int[] { 10 };
		for (final int n : new int[] { 100, 500 })
		{
			final OPTICSManager om1 = createOPTICSManager(size, n, rg);
			om1.setOptions(options1);
			final OPTICSManager om2 = om1.clone();
			om2.setOptions(options);

			for (final int minPts : minPoints)
			{
				// Use max range
				final OPTICSResult r1 = om1.optics(0, minPts);
				final OPTICSResult r1b = om1.optics(0, minPts);
				final OPTICSResult r2 = om2.optics(0, minPts);

				areEqual("repeat", r1, r1b);
				areEqual("new", r1, r2);
			}
		}
	}

	private static void areEqual(String title, OPTICSResult r1, OPTICSResult r2)
	{
		for (int i = 0; i < r1.size(); i++)
		{
			// Edge-points are random so ignore them. Only do core points.
			if (!r1.get(i).isCorePoint() || !r1.get(i).isCorePoint())
				continue;

			final double expC = r1.get(i).coreDistance;
			final double obsC = r2.get(i).coreDistance;

			final int expId = r1.get(i).parent;
			final int obsId = r2.get(i).parent;

			final int expPre = r1.get(i).predecessor;
			final int obsPre = r2.get(i).predecessor;

			final double expR = r1.get(i).reachabilityDistance;
			final double obsR = r2.get(i).reachabilityDistance;

			//TestSettings.debug("[%d] %d %d : %f = %f (%f) : %s = %d\n", i, expId, obsId, expR, obsR,
			//		r1.get(i).coreDistance, expPre, obsPre);

			TestAssert.assertEqualsRelative(expC, obsC, 1e-5, "%s C %d", title, i);
			TestAssert.assertEquals(expId, obsId, "%s Id %d", title, i);
			TestAssert.assertEquals(expPre, obsPre, 1e-5, "%s Pre %d", title, i);
			TestAssert.assertEqualsRelative(expR, obsR, 1e-5, "%s R %d", title, i);
		}
	}

	private static void areEqualClusters(OPTICSResult r1, OPTICSResult r2)
	{
		// We check the core distance and cluster ID are the same for each parent
		final double[] core1 = r1.getCoreDistance(false);
		final double[] core2 = r2.getCoreDistance(false);

		Assert.assertArrayEquals("Core", core1, core2, 0);

		final int[] cluster1 = r1.getClusters();
		final int[] cluster2 = r2.getClusters();

		Assert.assertArrayEquals("Cluster", cluster1, cluster2);
	}

	@Test
	public void canComputeDBSCANWithGridProcessing()
	{
		TestSettings.assumeMediumComplexity();
		canComputeDBSCANWithOptions(Option.GRID_PROCESSING);
	}

	@Test
	public void canComputeDBSCANWithCircularProcessing()
	{
		TestSettings.assumeMediumComplexity();
		canComputeDBSCANWithOptions(Option.CIRCULAR_PROCESSING);
	}

	@Test
	public void canComputeDBSCANWithInnerProcessingCircular()
	{
		TestSettings.assumeMediumComplexity();
		canComputeDBSCANWithOptions(Option.INNER_PROCESSING, Option.CIRCULAR_PROCESSING);
	}

	private void canComputeDBSCANWithOptions(Option... options)
	{
		final RandomGenerator rg = TestSettings.getRandomGenerator();
		final int[] minPoints = (TestSettings.allow(TestComplexity.LOW)) ? new int[] { 5, 10 } : new int[] { 10 };
		for (final int n : new int[] { 100, 500, 5000 })
		{
			final OPTICSManager om1 = createOPTICSManager(size, n, rg);
			final OPTICSManager om2 = om1.clone();
			om2.setOptions(options);

			for (final int minPts : minPoints)
			{
				final DBSCANResult r1 = om1.dbscan(0, minPts);
				final DBSCANResult r1b = om1.dbscan(0, minPts);
				final DBSCANResult r2 = om2.dbscan(0, minPts);

				areEqual("repeat", r1, r1b, minPts);
				areEqual("new", r1, r2, minPts);
			}
		}
	}

	private static void areEqual(String title, DBSCANResult r1, DBSCANResult r2, int minPts)
	{
		for (int i = 0; i < r1.size(); i++)
		{
			final int expPts = r1.get(i).nPts;
			final int obsPts = r2.get(i).nPts;

			// Edge-points are random so ignore them. Only do core points.
			if (expPts < minPts || obsPts < minPts)
				continue;

			Assert.assertEquals(title + " Pts " + i, expPts, obsPts);

			final int expId = r1.get(i).parent;
			final int obsId = r2.get(i).parent;

			final int expCId = r1.get(i).getClusterId();
			final int obsCId = r2.get(i).getClusterId();

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
		TestSettings.assumeMediumComplexity();
		final RandomGenerator rg = TestSettings.getRandomGenerator();
		final TrackProgress tracker = null; //new SimpleTrackProgress();
		for (final int n : N)
		{
			final OPTICSManager om = createOPTICSManager(size, n, rg);
			om.setTracker(tracker);

			for (final int minPts : new int[] { 10, 20 })
				om.optics(0, minPts);
		}

		// Use to speed time the algorithm when making changes
		//		long time2 = 0;
		//		for (int i = 10; i-- > 0;)
		//		{
		//			long time = System.nanoTime();
		//			for (int n : N)
		//			{
		//				OPTICSManager om = createOPTICSManager(size, n, rg);
		//
		//				for (int minPts : new int[] { 10, 20 })
		//				{
		//					om.optics(0, minPts);
		//				}
		//			}
		//			time = System.nanoTime() - time;
		//			TestSettings.info("Time = %d\n", time);
		//			if (i < 5)
		//				time2 += time;
		//		}
		//		TestSettings.info("Time = %d\n", time2);
	}

	@Test
	public void canComputeKNNDistance()
	{
		final RandomGenerator rg = TestSettings.getRandomGenerator();
		final int n = 100;
		final OPTICSManager om = createOPTICSManager(size, n, rg);

		// All-vs-all distance matrix
		final float[][] data = om.getData();
		final float[] x = data[0];
		final float[] y = data[1];
		final float[][] d2 = new float[n][n];
		for (int i = 0; i < n; i++)
			for (int j = i + 1; j < n; j++)
				d2[i][j] = d2[j][i] = Maths.distance2(x[i], y[i], x[j], y[j]);

		// Try all including invalid bounds
		for (int k : new int[] { 0, 1, 3, 5, n - 1, n })
		{
			final float[] o = om.nearestNeighbourDistance(k, -1, true);
			final float[] e = new float[n];
			// Set the correct bounds on k
			if (k >= n)
				k = n - 1;
			if (k < 1)
				k = 1;
			for (int i = 0; i < n; i++)
				e[i] = (float) Math.sqrt(PartialSort.bottom(PartialSort.OPTION_HEAD_FIRST, d2[i], n, k + 1)[0]);
			//TestSettings.debug("e=%s, o=%s\n", Arrays.toString(e), Arrays.toString(o));
			Assert.assertArrayEquals(e, o, 0);
		}
	}

	@Test
	public void canComputeKNNDistanceWithBigData()
	{
		TestSettings.assumeMediumComplexity();
		final RandomGenerator rg = TestSettings.getRandomGenerator();
		for (final int n : N)
		{
			final OPTICSManager om = createOPTICSManager(size, n, rg);

			for (final int k : new int[] { 3, 5 })
			{
				final float[] d = om.nearestNeighbourDistance(k, -1, true);
				Assert.assertEquals(d.length, n);
			}
		}
	}

	@Test
	public void canComputeKNNDistanceWithSample()
	{
		final RandomGenerator rg = TestSettings.getRandomGenerator();
		for (final int n : N)
		{
			final OPTICSManager om = createOPTICSManager(size, n, rg);

			final int samples = n / 10;
			for (final int k : new int[] { 3, 5 })
			{
				final float[] d = om.nearestNeighbourDistance(k, samples, true);
				Assert.assertEquals(d.length, samples);
			}
		}
	}

	@Test
	public void canComputeGeneratingDistance()
	{
		final int[] points = new int[] { 1, 2, 3, 5, 10, 20, 50, 100 };
		final double area = size * size;
		for (final int n : N)
			for (final int minPts : points)
				//float d =
				OPTICSManager.computeGeneratingDistance(minPts, area, n);
		//TestSettings.debug("k=%d, volumeDS=%.1f, N=%d, d=%f\n", minPts, area, n, d);
	}

	@Test
	public void canRepeatOPTICS()
	{
		final RandomGenerator rg = TestSettings.getRandomGenerator();
		final int n = N[0];
		final OPTICSManager om = createOPTICSManager(size, n, rg);

		final float radius = 0;

		final int minPts = 10;
		Assert.assertFalse(om.hasMemory());

		final EnumSet<Option> opt = om.getOptions();

		opt.add(OPTICSManager.Option.CACHE);
		final OPTICSResult r1 = om.optics(radius, minPts);
		Assert.assertTrue(om.hasMemory());

		opt.remove(OPTICSManager.Option.CACHE);
		final OPTICSResult r2 = om.optics(radius, minPts);
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
		final RandomGenerator rg = TestSettings.getRandomGenerator();
		final int minPts = 10;
		for (final int n : N)
		{
			final OPTICSManager om = createOPTICSManager(size, n, rg);

			for (final float radius : new float[] { 0.01f })
				om.optics(radius, minPts);
			//TestSettings.debug("OPTICS %d @ %.1f,%d\n", n, radius, minPts);
		}
	}

	@Test
	public void canPerformOPTICSWith1Point()
	{
		final RandomGenerator rg = TestSettings.getRandomGenerator();
		final OPTICSManager om = createOPTICSManager(size, 1, rg);

		for (final float radius : new float[] { -1, 0, 0.01f, 1f })
			for (final int minPts : new int[] { -1, 0, 1 })
			{
				final OPTICSResult r1 = om.optics(radius, minPts);
				// Should be 1 cluster
				Assert.assertEquals(1, r1.get(0).clusterId);
			}

		final OPTICSResult r1 = om.optics(1, 2);
		// Should be 0 clusters as the min size is too high
		Assert.assertEquals(0, r1.get(0).clusterId);
	}

	@Test
	public void canPerformOPTICSWithColocatedData()
	{
		final OPTICSManager om = new OPTICSManager(new float[10], new float[10], new Rectangle(size, size));

		for (final float radius : new float[] { -1, 0, 0.01f, 1f })
			for (final int minPts : new int[] { -1, 0, 1, 10 })
			{
				final OPTICSResult r1 = om.optics(radius, minPts);
				// All should be in the same cluster
				Assert.assertEquals(1, r1.get(0).clusterId);
			}
	}

	@Test
	public void canConvertOPTICSToDBSCAN()
	{
		final RandomGenerator rg = TestSettings.getRandomGenerator();
		final int n = N[0];
		final OPTICSManager om = createOPTICSManager(size, n, rg);

		final float radius = radii[radii.length - 1];

		final int minPts = 10;
		final OPTICSResult r1 = om.optics(radius, minPts);
		// Store for later and reset
		final int[] clusterId = new int[r1.size()];
		for (int i = r1.size(); i-- > 0;)
		{
			clusterId[i] = r1.get(i).clusterId;
			r1.get(i).clusterId = -1;
		}
		// Smaller distance
		int nClusters = r1.extractDBSCANClustering(radius * 0.8f);
		int max = 0;
		//int[] clusterId2 = new int[r1.size()];
		for (int i = r1.size(); i-- > 0;)
		{
			if (max < r1.get(i).clusterId)
				max = r1.get(i).clusterId;
			//clusterId2[i] = r1.get(i).clusterId;
			Assert.assertNotEquals(r1.get(i).clusterId, -1);
		}
		Assert.assertEquals(nClusters, max);
		// Same distance
		nClusters = r1.extractDBSCANClustering(radius);
		for (int i = r1.size(); i-- > 0;)
			Assert.assertEquals(r1.get(i).clusterId, clusterId[i]);
	}

	/**
	 * Test the results of DBSCAN using OPTICS
	 */
	@Test
	public void canComputeDBSCAN()
	{
		final RandomGenerator rg = TestSettings.getRandomGenerator();
		final int[] minPoints = (TestSettings.allow(TestComplexity.LOW)) ? new int[] { 5, 10 } : new int[] { 10 };
		for (final int n : new int[] { 100, 500 })
		{
			final OPTICSManager om = createOPTICSManager(size, n, rg);
			// Keep items in memory for speed during the test
			om.setOptions(OPTICSManager.Option.CACHE);

			for (final int minPts : minPoints)
			{
				// Use default range
				final OPTICSResult r1 = om.optics(0, minPts);
				final DBSCANResult r2 = om.dbscan(0, minPts);

				areSameClusters(r1, r2);
			}
		}
	}

	private static void areSameClusters(OPTICSResult r1, DBSCANResult r2)
	{
		// Check. Remove non-core points as OPTICS and DBSCAN differ in the
		// processing order within a cluster.
		final int[] c1 = r1.getClusters(true);
		final int[] c2 = r2.getClusters(true);

		//for (int i = 0; i < c1.length; i++)
		//{
		//	TestSettings.info("[%d] %d == %d\n", i, c1[i], c2[i]);
		//}

		Assert.assertArrayEquals(c1, c2);
	}

	@Test
	public void dBSCANIsFasterThanOPTICS()
	{
		TestSettings.assumeSpeedTest();

		final RandomGenerator rg = TestSettings.getRandomGenerator();
		final OPTICSManager om1 = createOPTICSManager(size, 5000, rg);
		final OPTICSManager om2 = om1.clone();

		final long t1 = System.nanoTime();
		final OPTICSResult r1 = om1.optics(0, 10);
		long t2 = System.nanoTime();
		final DBSCANResult r2 = om2.dbscan(0, 10);
		long t3 = System.nanoTime();

		areSameClusters(r1, r2);

		t3 = t3 - t2;
		t2 = t2 - t1;

		Assert.assertTrue(t3 < t2);

		// Note: The OPTICS paper reports that it should be about 1.6x slower than DBSCAN
		// This test shows a different value due to:
		// - unrealistic data
		// - The optimised DBSCAN implementation not computing distances if not needed.

		TestSettings.info("dBSCANIsFasterThanOPTICS %d < %d (%.2f)\n", t3, t2, (double) t2 / t3);
	}

	@Test
	public void canMatchDBSCANCorePointsWithOPTICS()
	{
		final RandomGenerator rg = TestSettings.getRandomGenerator();
		for (final int n : new int[] { 100, 500 })
		{
			final OPTICSManager om = createOPTICSManager(size, n, rg);
			// Keep items in memory for speed during the test
			om.setOptions(OPTICSManager.Option.CACHE);

			for (final int minPts : new int[] { 5, 20 })
				for (final float e : new float[] { 0, 4 })
				{
					final OPTICSResult r1 = om.optics(e, minPts);

					// Try smaller radius for DBSCAN
					for (int i = 2; i <= 5; i++)
					{
						final float d = r1.generatingDistance / i;
						final DBSCANResult r2 = om.dbscan(d, minPts);

						// Now extract core points
						r1.extractDBSCANClustering(d, true);
						final int[] c1 = r1.getClusters();
						final int[] c2 = r2.getClusters(true);

						Assert.assertEquals(1, RandIndex.randIndex(c1, c2), 0);
					}
				}
		}
	}

	@Test
	public void dBSCANInnerCircularIsFasterWhenDensityIsHigh()
	{
		TestSettings.assumeSpeedTest();

		final RandomGenerator rg = TestSettings.getRandomGenerator();
		final int molecules = 10000;
		final OPTICSManager om1 = createOPTICSManager(size, molecules, rg);
		final OPTICSManager om2 = om1.clone();
		om1.setOptions(Option.GRID_PROCESSING);
		om2.setOptions(Option.CIRCULAR_PROCESSING, Option.INNER_PROCESSING);

		float generatingDistanceE = 0;
		final double nMoleculesInPixel = (double) molecules / (size * size);
		double nMoleculesInCircle;
		final int limit = RadialMoleculeSpace.N_MOLECULES_FOR_NEXT_RESOLUTION_INNER * 2;
		do
		{
			generatingDistanceE += 0.1f;
			nMoleculesInCircle = Math.PI * generatingDistanceE * generatingDistanceE * nMoleculesInPixel;
		} while (nMoleculesInCircle < limit && generatingDistanceE < size);

		final int minPts = 20;

		DBSCANResult r1, r2;

		// Warm-up
		//r1 = om1.dbscan(generatingDistanceE, minPts);
		//r2 = om2.dbscan(generatingDistanceE, minPts);

		final long t1 = System.nanoTime();
		r1 = om1.dbscan(generatingDistanceE, minPts);
		long t2 = System.nanoTime();
		r2 = om2.dbscan(generatingDistanceE, minPts);
		long t3 = System.nanoTime();

		areEqual("new", r1, r2, minPts);

		t3 = t3 - t2;
		t2 = t2 - t1;

		TestSettings.logSpeedTestResult(t3 < t2, "dBSCANInnerCircularIsFasterWhenComparisonsIsHigh %d < %d (%.2f)\n",
				t3, t2, (double) t2 / t3);
	}

	@Test
	public void oPTICSCircularIsFasterWhenDensityIsHigh()
	{
		TestSettings.assumeSpeedTest();

		final RandomGenerator rg = TestSettings.getRandomGenerator();
		final int molecules = 10000;
		final OPTICSManager om1 = createOPTICSManager(size, molecules, rg);
		final OPTICSManager om2 = om1.clone();
		om1.setOptions(Option.GRID_PROCESSING);
		om2.setOptions(Option.CIRCULAR_PROCESSING);

		float generatingDistanceE = 0;
		final double nMoleculesInPixel = (double) molecules / (size * size);
		double nMoleculesInCircle;
		final int limit = RadialMoleculeSpace.N_MOLECULES_FOR_NEXT_RESOLUTION_OUTER;
		do
		{
			generatingDistanceE += 0.1f;
			nMoleculesInCircle = Math.PI * generatingDistanceE * generatingDistanceE * nMoleculesInPixel;
		} while (nMoleculesInCircle < limit && generatingDistanceE < size);

		final int minPts = 20;

		OPTICSResult r1, r2;

		// Warm-up
		//r1 = om1.optics(generatingDistanceE, minPts);
		//r2 = om2.optics(generatingDistanceE, minPts);

		final long t1 = System.nanoTime();
		r1 = om1.optics(generatingDistanceE, minPts);
		long t2 = System.nanoTime();
		r2 = om2.optics(generatingDistanceE, minPts);
		long t3 = System.nanoTime();

		areEqual("new", r1, r2);

		t3 = t3 - t2;
		t2 = t2 - t1;

		TestSettings.logSpeedTestResult(t3 < t2, "oPTICSCircularIsFasterWhenDensityIsHigh %d < %d (%.2f)\n", t3, t2,
				(double) t2 / t3);
	}

	private enum MS
	{
		SIMPLE, GRID, RADIAL, INNER_RADIAL, TREE, TREE2
	}

	private abstract class MyTimingTask extends BaseTimingTask
	{
		MS ms;
		boolean generate;
		OPTICSManager[] om;
		int minPts;
		float generatingDistanceE, e;
		int resolution;
		Option[] options;
		String name;

		public MyTimingTask(MS ms, boolean generate, OPTICSManager[] om, int minPts, float generatingDistanceE,
				int resolution, Option... options)
		{
			super(ms.toString());
			this.ms = ms;
			this.generate = generate;
			this.om = om;
			this.generatingDistanceE = generatingDistanceE;
			this.resolution = resolution;
			this.minPts = minPts;
			e = generatingDistanceE * generatingDistanceE;
			this.options = options;
		}

		@Override
		public int getSize()
		{
			return om.length;
		}

		@Override
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
				case TREE:
					space = new TreeMoleculeSpace(om[i], generatingDistanceE);
					break;
				case TREE2:
					space = new TreeMoleculeSpace2(om[i], generatingDistanceE);
					break;
			}
			om[i].setOptions(o);
			if (generate)
				generate(space);
			return space;
		}

		void generate(MoleculeSpace space)
		{
			space.generate();
			if (name == null)
				name = space.toString();
		}

		@Override
		public String getName()
		{
			return name;
		}
	}

	private abstract class FindNeighboursTimingTask extends MyTimingTask
	{
		public FindNeighboursTimingTask(MS ms, boolean generate, OPTICSManager[] om, int minPts,
				float generatingDistanceE, int resolution, Option... options)
		{
			super(ms, generate, om, minPts, generatingDistanceE, resolution, options);
		}

		public FindNeighboursTimingTask(MS ms, OPTICSManager[] om, int minPts, float generatingDistanceE,
				int resolution, Option... options)
		{
			super(ms, false, om, minPts, generatingDistanceE, resolution, options);
		}

		@Override
		public Object run(Object data)
		{
			final MoleculeSpace space = (MoleculeSpace) data;
			if (!generate)
				generate(space);
			final int[][] n = new int[space.size][];
			for (int i = space.size; i-- > 0;)
			{
				space.findNeighbours(minPts, space.setOfObjects[i], e);
				final int[] nn = new int[space.neighbours.size];
				for (int j = space.neighbours.size; j-- > 0;)
					nn[j] = space.neighbours.get(j).id;
				n[i] = nn;
			}
			return n;
		}
	}

	@Test
	public void canTestMoleculeSpaceFindNeighbours()
	{
		final RandomGenerator rg = TestSettings.getRandomGenerator();
		final OPTICSManager[] om = new OPTICSManager[5];
		for (int i = 0; i < om.length; i++)
			om[i] = createOPTICSManager(size, 1000, rg);

		final float generatingDistanceE = 10;
		final int minPts = 20;

		// Results
		final int[][][] n = new int[om.length][][];

		final int loops = (TestSettings.allow(LogLevel.INFO)) ? 5 : 1;

		final TimingService ts = new TimingService(loops);
		final boolean check = true;

		ts.execute(new FindNeighboursTimingTask(MS.SIMPLE, om, minPts, generatingDistanceE, 0)
		{
			@Override
			public void check(int i, Object result)
			{
				// Store these as the correct results
				n[i] = format(result);
			}
		}, check);
		ts.execute(new FindNeighboursTimingTask(MS.GRID, om, minPts, generatingDistanceE, 0)
		{
			@Override
			public void check(int i, Object result)
			{
				final String name = getName() + ":" + i + ":";
				final int[][] e = n[i];
				final int[][] o = format(result);
				for (int j = 0; j < e.length; j++)
					Assert.assertArrayEquals(name + j, e[j], o[j]);
			}
		}, check);
		ts.execute(new FindNeighboursTimingTask(MS.GRID, om, minPts, generatingDistanceE, 10)
		{
			@Override
			public void check(int i, Object result)
			{
				final String name = getName() + ":" + i + ":";
				final int[][] e = n[i];
				final int[][] o = format(result);
				for (int j = 0; j < e.length; j++)
					Assert.assertArrayEquals(name + j, e[j], o[j]);
			}
		}, check);
		ts.execute(new FindNeighboursTimingTask(MS.RADIAL, om, minPts, generatingDistanceE, 0)
		{
			@Override
			public void check(int i, Object result)
			{
				final String name = getName() + ":" + i + ":";
				final int[][] e = n[i];
				final int[][] o = format(result);
				for (int j = 0; j < e.length; j++)
					Assert.assertArrayEquals(name, e[j], o[j]);
			}
		}, check);
		ts.execute(new FindNeighboursTimingTask(MS.INNER_RADIAL, om, minPts, generatingDistanceE, 0)
		{
			@Override
			public void check(int i, Object result)
			{
				final String name = getName() + ":" + i + ":";
				final int[][] e = n[i];
				final int[][] o = format(result);
				for (int j = 0; j < e.length; j++)
					Assert.assertArrayEquals(name, e[j], o[j]);
			}
		}, check);
		ts.execute(new FindNeighboursTimingTask(MS.RADIAL, om, minPts, generatingDistanceE, 10)
		{
			@Override
			public void check(int i, Object result)
			{
				final String name = getName() + ":" + i + ":";
				final int[][] e = n[i];
				final int[][] o = format(result);
				for (int j = 0; j < e.length; j++)
					Assert.assertArrayEquals(name, e[j], o[j]);
			}
		}, check);
		ts.execute(new FindNeighboursTimingTask(MS.INNER_RADIAL, om, minPts, generatingDistanceE, 10)
		{
			@Override
			public void check(int i, Object result)
			{
				final String name = getName() + ":" + i + ":";
				final int[][] e = n[i];
				final int[][] o = format(result);
				for (int j = 0; j < e.length; j++)
					Assert.assertArrayEquals(name, e[j], o[j]);
			}
		}, check);
		ts.execute(new FindNeighboursTimingTask(MS.TREE, om, minPts, generatingDistanceE, 0)
		{
			@Override
			public void check(int i, Object result)
			{
				final String name = getName() + ":" + i + ":";
				final int[][] e = n[i];
				final int[][] o = format(result);
				for (int j = 0; j < e.length; j++)
					Assert.assertArrayEquals(name, e[j], o[j]);
			}
		}, check);
		ts.execute(new FindNeighboursTimingTask(MS.TREE2, om, minPts, generatingDistanceE, 0)
		{
			@Override
			public void check(int i, Object result)
			{
				final String name = getName() + ":" + i + ":";
				final int[][] e = n[i];
				final int[][] o = format(result);
				for (int j = 0; j < e.length; j++)
					Assert.assertArrayEquals(name, e[j], o[j]);
			}
		}, check);

		if (loops > 1)
			ts.report();
	}

	@Test
	public void canTestMoleculeSpaceFindNeighboursPregenerated()
	{
		final RandomGenerator rg = TestSettings.getRandomGenerator();
		final OPTICSManager[] om = new OPTICSManager[5];
		for (int i = 0; i < om.length; i++)
			om[i] = createOPTICSManager(size, 1000, rg);

		final float generatingDistanceE = 10;
		final int minPts = 20;

		// Results
		final int[][][] n = new int[om.length][][];

		final int loops = (TestSettings.allow(LogLevel.INFO)) ? 5 : 1;

		final TimingService ts = new TimingService(loops);
		final boolean check = true;

		ts.execute(new FindNeighboursTimingTask(MS.SIMPLE, true, om, minPts, generatingDistanceE, 0)
		{
			@Override
			public void check(int i, Object result)
			{
				// Store these as the correct results
				n[i] = format(result);
			}
		}, check);
		ts.execute(new FindNeighboursTimingTask(MS.GRID, true, om, minPts, generatingDistanceE, 0)
		{
			@Override
			public void check(int i, Object result)
			{
				final String name = getName() + ":" + i + ":";
				final int[][] e = n[i];
				final int[][] o = format(result);
				for (int j = 0; j < e.length; j++)
					Assert.assertArrayEquals(name + j, e[j], o[j]);
			}
		}, check);
		ts.execute(new FindNeighboursTimingTask(MS.GRID, true, om, minPts, generatingDistanceE, 10)
		{
			@Override
			public void check(int i, Object result)
			{
				final String name = getName() + ":" + i + ":";
				final int[][] e = n[i];
				final int[][] o = format(result);
				for (int j = 0; j < e.length; j++)
					Assert.assertArrayEquals(name + j, e[j], o[j]);
			}
		}, check);
		ts.execute(new FindNeighboursTimingTask(MS.RADIAL, true, om, minPts, generatingDistanceE, 0)
		{
			@Override
			public void check(int i, Object result)
			{
				final String name = getName() + ":" + i + ":";
				final int[][] e = n[i];
				final int[][] o = format(result);
				for (int j = 0; j < e.length; j++)
					Assert.assertArrayEquals(name, e[j], o[j]);
			}
		}, check);
		ts.execute(new FindNeighboursTimingTask(MS.INNER_RADIAL, true, om, minPts, generatingDistanceE, 0)
		{
			@Override
			public void check(int i, Object result)
			{
				final String name = getName() + ":" + i + ":";
				final int[][] e = n[i];
				final int[][] o = format(result);
				for (int j = 0; j < e.length; j++)
					Assert.assertArrayEquals(name, e[j], o[j]);
			}
		}, check);
		ts.execute(new FindNeighboursTimingTask(MS.RADIAL, true, om, minPts, generatingDistanceE, 10)
		{
			@Override
			public void check(int i, Object result)
			{
				final String name = getName() + ":" + i + ":";
				final int[][] e = n[i];
				final int[][] o = format(result);
				for (int j = 0; j < e.length; j++)
					Assert.assertArrayEquals(name, e[j], o[j]);
			}
		}, check);
		ts.execute(new FindNeighboursTimingTask(MS.INNER_RADIAL, true, om, minPts, generatingDistanceE, 10)
		{
			@Override
			public void check(int i, Object result)
			{
				final String name = getName() + ":" + i + ":";
				final int[][] e = n[i];
				final int[][] o = format(result);
				for (int j = 0; j < e.length; j++)
					Assert.assertArrayEquals(name, e[j], o[j]);
			}
		}, check);
		ts.execute(new FindNeighboursTimingTask(MS.TREE, true, om, minPts, generatingDistanceE, 0)
		{
			@Override
			public void check(int i, Object result)
			{
				final String name = getName() + ":" + i + ":";
				final int[][] e = n[i];
				final int[][] o = format(result);
				for (int j = 0; j < e.length; j++)
					Assert.assertArrayEquals(name, e[j], o[j]);
			}
		}, check);
		ts.execute(new FindNeighboursTimingTask(MS.TREE2, true, om, minPts, generatingDistanceE, 0)
		{
			@Override
			public void check(int i, Object result)
			{
				final String name = getName() + ":" + i + ":";
				final int[][] e = n[i];
				final int[][] o = format(result);
				for (int j = 0; j < e.length; j++)
					Assert.assertArrayEquals(name, e[j], o[j]);
			}
		}, check);

		if (loops > 1)
			ts.report();
	}

	/**
	 * This test uses the auto-resolution. It is mainly used to determine when to switch inner circle processing on.
	 */
	@Test
	public void canTestMoleculeSpaceFindNeighboursWithAutoResolution()
	{
		Assume.assumeTrue(TestSettings.allow(LogLevel.INFO, TestComplexity.MEDIUM));

		final RandomGenerator rg = TestSettings.getRandomGenerator();
		final int molecules = 20000;
		float generatingDistanceE = 0;
		final int minPts = 20;

		final double nMoleculesInPixel = (double) molecules / (size * size);
		final int[] moleculesInArea = new int[] { 64, 128, 256, 512, 1024 };

		// Should this ever be done?
		final boolean check = TestSettings.allow(TestComplexity.HIGH); // This is slow as the number of sorts in the check method is very large

		for (final int m : moleculesInArea)
		{
			// Increase generatingDistance until we achieve the molecules
			double nMoleculesInCircle;
			do
			{
				generatingDistanceE += 0.1f;
				nMoleculesInCircle = Math.PI * generatingDistanceE * generatingDistanceE * nMoleculesInPixel;
			} while (nMoleculesInCircle < m && generatingDistanceE < size);

			final double nMoleculesInSquare = 4 * generatingDistanceE * generatingDistanceE * nMoleculesInPixel;
			final int maxResolution = (int) Math.ceil(nMoleculesInSquare);

			TestSettings.info("Square=%.2f, Circle=%.2f, e=%.1f, r <= %d\n", nMoleculesInSquare, nMoleculesInCircle,
					generatingDistanceE, maxResolution);

			final OPTICSManager[] om = new OPTICSManager[3];
			for (int i = 0; i < om.length; i++)
				om[i] = createOPTICSManager(size, molecules, rg);

			// Results
			final int[][][] n = new int[om.length][][];

			final TimingService ts = new TimingService(1);

			final int resolution = 0;
			ts.execute(new FindNeighboursTimingTask(MS.GRID, om, minPts, generatingDistanceE, resolution)
			{
				@Override
				public void check(int i, Object result)
				{
					n[i] = format(result);
				}
			}, check);
			ts.execute(new FindNeighboursTimingTask(MS.RADIAL, om, minPts, generatingDistanceE, resolution)
			{
				@Override
				public void check(int i, Object result)
				{
					final String name = getName() + ":" + i + ":";
					final int[][] e = n[i];
					final int[][] o = format(result);
					for (int j = 0; j < e.length; j++)
						Assert.assertArrayEquals(name, e[j], o[j]);
				}
			}, check);
			ts.execute(new FindNeighboursTimingTask(MS.INNER_RADIAL, om, minPts, generatingDistanceE, resolution)
			{
				@Override
				public void check(int i, Object result)
				{
					final String name = getName() + ":" + i + ":";
					final int[][] e = n[i];
					final int[][] o = format(result);
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
	@Test
	public void canTestGridMoleculeSpaceFindNeighboursWithResolution()
	{
		Assume.assumeTrue(TestSettings.allow(LogLevel.INFO, TestComplexity.HIGH));

		final RandomGenerator rg = TestSettings.getRandomGenerator();
		final int molecules = 50000;
		float generatingDistanceE = 0;
		final int minPts = 20;

		final double nMoleculesInPixel = (double) molecules / (size * size);
		final int[] moleculesInArea = new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 15, 20, 25, 30, 35, 40, 45, 50, 60, 70,
				80, 90, 100, 120, 140, 160, 200, 300 };

		for (final int m : moleculesInArea)
		{
			// Increase generatingDistance until we achieve the molecules
			double nMoleculesInSquare;
			do
			{
				generatingDistanceE += 0.1f;
				nMoleculesInSquare = 4 * generatingDistanceE * generatingDistanceE * nMoleculesInPixel;
			} while (nMoleculesInSquare < m && generatingDistanceE < size);

			final double nMoleculesInCircle = Math.PI * generatingDistanceE * generatingDistanceE * nMoleculesInPixel;
			final int maxResolution = (int) Math.ceil(nMoleculesInSquare);

			TestSettings.info("Square=%.2f, Circle=%.2f, e=%.1f, r <= %d\n", nMoleculesInSquare, nMoleculesInCircle,
					generatingDistanceE, maxResolution);

			final OPTICSManager[] om = new OPTICSManager[3];
			for (int i = 0; i < om.length; i++)
				om[i] = createOPTICSManager(size, molecules, rg);

			// Results
			final int[][][] n = new int[om.length][][];

			final TimingService ts = new TimingService(1);

			final double[] best = new double[] { Double.MAX_VALUE };
			TimingResult r;
			int noChange = 0;

			for (int resolution = 1; resolution <= maxResolution; resolution++)
			{
				final double last = best[0];
				r = ts.execute(new FindNeighboursTimingTask(MS.GRID, om, minPts, generatingDistanceE, resolution)
				{
					@Override
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
	@Test
	public void canTestRadialMoleculeSpaceFindNeighboursWithResolution()
	{
		Assume.assumeTrue(TestSettings.allow(LogLevel.INFO, TestComplexity.HIGH));

		final RandomGenerator rg = TestSettings.getRandomGenerator();
		final int molecules = 20000;
		float generatingDistanceE = 0;
		final int minPts = 20;

		final double nMoleculesInPixel = (double) molecules / (size * size);
		final int[] moleculesInArea = new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 15, 20, 25, 30, 35, 40, 45, 50, 60, 70,
				80, 90, 100, 120, 140, 160, 200, 300 };

		for (final int m : moleculesInArea)
		{
			// Increase generatingDistance until we achieve the molecules
			double nMoleculesInCircle;
			do
			{
				generatingDistanceE += 0.1f;
				nMoleculesInCircle = Math.PI * generatingDistanceE * generatingDistanceE * nMoleculesInPixel;
			} while (nMoleculesInCircle < m && generatingDistanceE < size);

			final double nMoleculesInSquare = 4 * generatingDistanceE * generatingDistanceE * nMoleculesInPixel;
			final int maxResolution = (int) Math.ceil(nMoleculesInCircle);

			TestSettings.info("Square=%.2f, Circle=%.2f, e=%.1f, r <= %d\n", nMoleculesInSquare, nMoleculesInCircle,
					generatingDistanceE, maxResolution);

			final OPTICSManager[] om = new OPTICSManager[3];
			for (int i = 0; i < om.length; i++)
				om[i] = createOPTICSManager(size, molecules, rg);

			// Results
			final int[][][] n = new int[om.length][][];

			final TimingService ts = new TimingService(1);

			final double[] best = new double[] { Double.MAX_VALUE };
			TimingResult r;
			int noChange = 0;

			for (int resolution = 1; resolution <= maxResolution; resolution++)
			{
				final double last = best[0];
				r = ts.execute(new FindNeighboursTimingTask(MS.RADIAL, om, minPts, generatingDistanceE, resolution)
				{
					@Override
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
	@Test
	public void canTestInnerRadialMoleculeSpaceFindNeighboursWithResolution()
	{
		Assume.assumeTrue(TestSettings.allow(LogLevel.INFO, TestComplexity.HIGH));

		final RandomGenerator rg = TestSettings.getRandomGenerator();
		final int molecules = 20000;
		float generatingDistanceE = 0;
		final int minPts = 20;

		final double nMoleculesInPixel = (double) molecules / (size * size);
		final int[] moleculesInArea = new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 15, 20, 25, 30, 35, 40, 45, 50, 60, 70,
				80, 90, 100, 150, 200, 300, 500, 1000 };

		int lastMax = 0;
		for (final int m : moleculesInArea)
		{
			// Increase generatingDistance until we achieve the molecules
			double nMoleculesInCircle;
			do
			{
				generatingDistanceE += 0.1f;
				nMoleculesInCircle = Math.PI * generatingDistanceE * generatingDistanceE * nMoleculesInPixel;
			} while (nMoleculesInCircle < m && generatingDistanceE < size);

			final double nMoleculesInSquare = 4 * generatingDistanceE * generatingDistanceE * nMoleculesInPixel;
			final int maxResolution = (int) Math.ceil(nMoleculesInCircle);

			TestSettings.info("Square=%.2f, Circle=%.2f, e=%.1f, r <= %d\n", nMoleculesInSquare, nMoleculesInCircle,
					generatingDistanceE, maxResolution);

			final OPTICSManager[] om = new OPTICSManager[3];
			for (int i = 0; i < om.length; i++)
				om[i] = createOPTICSManager(size, molecules, rg);

			// Results
			final int[][][] n = new int[om.length][][];

			final TimingService ts = new TimingService(1);

			final double[] best = new double[] { Double.MAX_VALUE };
			TimingResult r;
			int noChange = 0;

			for (int resolution = Math.max(1, lastMax - 3); resolution <= maxResolution; resolution++)
			{
				final double last = best[0];
				r = ts.execute(
						new FindNeighboursTimingTask(MS.INNER_RADIAL, om, minPts, generatingDistanceE, resolution)
						{
							@Override
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

	private class OPTICSTimingTask extends BaseTimingTask
	{
		int moleculesInArea;
		OPTICSManager[] om;
		int minPts;
		float generatingDistanceE;
		Option[] options;
		String name = null;

		public OPTICSTimingTask(int moleculesInArea, OPTICSManager[] om, int minPts, float generatingDistanceE,
				Option... options)
		{
			super(options.toString());
			this.moleculesInArea = moleculesInArea;
			this.om = om;
			this.generatingDistanceE = generatingDistanceE;
			this.minPts = minPts;
			this.options = options;
			setName();
		}

		private void setName()
		{
			final StringBuilder sb = new StringBuilder();
			sb.append("m=").append(moleculesInArea);
			sb.append(" minPts=").append(minPts).append(" e=").append(generatingDistanceE);
			for (final Option o : options)
				sb.append(' ').append(o.toString());
			name = sb.toString();
		}

		@Override
		public int getSize()
		{
			return om.length;
		}

		@Override
		public Object getData(int i)
		{
			return om[i];
		}

		@Override
		public Object run(Object data)
		{
			final OPTICSManager om = (OPTICSManager) data;
			// Set the options
			EnumSet<Option> o = om.getOptions();
			if (options != null)
			{
				o = o.clone();
				om.setOptions(options);
			}
			final OPTICSResult r = om.optics(generatingDistanceE, minPts);
			// Reset
			om.setOptions(o);
			return r;
		}

		@Override
		public String getName()
		{
			return name;
		}
	}

	/**
	 * Tests the speed of the different queue structures. The default Heap is faster that the simple priority queue when
	 * the number of molecules within the generating distance is high. When at the default level then the speed is
	 * similar.
	 */
	@Test
	public void canTestOPTICSQueue()
	{
		Assume.assumeTrue(TestSettings.allow(LogLevel.INFO, TestComplexity.HIGH));

		final RandomGenerator rg = TestSettings.getRandomGenerator();
		final int molecules = 5000;
		float generatingDistanceE = 0;
		final int minPts = 5;

		final double nMoleculesInPixel = (double) molecules / (size * size);
		final int[] moleculesInArea = new int[] { 0, 5, 10, 20, 50 };

		final OPTICSManager[] om = new OPTICSManager[5];
		for (int i = 0; i < om.length; i++)
		{
			om[i] = createOPTICSManager(size, molecules, rg);
			om[i].setOptions(Option.CACHE);
		}

		for (final int m : moleculesInArea)
		{
			// Increase generatingDistance until we achieve the molecules
			double nMoleculesInCircle = Math.PI * generatingDistanceE * generatingDistanceE * nMoleculesInPixel;
			while (nMoleculesInCircle < m && generatingDistanceE < size)
			{
				generatingDistanceE += 0.1f;
				nMoleculesInCircle = Math.PI * generatingDistanceE * generatingDistanceE * nMoleculesInPixel;
			}

			final TimingService ts = new TimingService(3);

			// Run once without timing so the structure is cached
			final OPTICSTimingTask task = new OPTICSTimingTask(m, om, minPts, generatingDistanceE);
			ts.execute(task);
			ts.clearResults();

			// Note: It is not actually fair to do a speed test without strict ID ordering as the order may dictate the speed
			// as the list of items to process will be built in a different order (and to a different size).
			// So we can compare ID and reverse ID ordering for different structures.
			// But not no ID ordering for different structures as the order will be dictated by the structure itself.
			ts.execute(task);
			ts.execute(new OPTICSTimingTask(m, om, minPts, generatingDistanceE, Option.OPTICS_SIMPLE_PRIORITY_QUEUE));

			//@formatter:off
			ts.execute(new OPTICSTimingTask(m, om, minPts, generatingDistanceE, Option.OPTICS_STRICT_ID_ORDER));
			ts.execute(new OPTICSTimingTask(m, om, minPts, generatingDistanceE, Option.OPTICS_STRICT_ID_ORDER, Option.OPTICS_SIMPLE_PRIORITY_QUEUE));

			ts.execute(new OPTICSTimingTask(m, om, minPts, generatingDistanceE, Option.OPTICS_STRICT_REVERSE_ID_ORDER));
			ts.execute(new OPTICSTimingTask(m, om, minPts, generatingDistanceE, Option.OPTICS_STRICT_REVERSE_ID_ORDER, Option.OPTICS_SIMPLE_PRIORITY_QUEUE));
			//@formatter:on

			ts.report();
		}
	}

	/**
	 * Test the results of LoOP using the ELKI framework
	 */
	@Test
	public void canComputeLoOP()
	{
		final RandomGenerator rg = TestSettings.getRandomGenerator();
		final TrackProgress tracker = null; //new SimpleTrackProgress();
		final int[] minPoints = (TestSettings.allow(TestComplexity.LOW)) ? new int[] { 5, 10 } : new int[] { 10 };
		for (final int n : new int[] { 100, 500 })
		{
			final OPTICSManager om = createOPTICSManager(size, n, rg);
			om.setTracker(tracker);
			om.setNumberOfThreads(1);

			// Use ELKI to provide the expected results
			final double[][] data = new Array2DRowRealMatrix(om.getDoubleData()).transpose().getData();

			for (final int minPts : minPoints)
			{
				// Reset starting Id to 1
				final DatabaseConnection dbc = new ArrayAdapterDatabaseConnection(data, null, 0);
				ListParameterization params = new ListParameterization();
				params.addParameter(AbstractDatabase.Parameterizer.DATABASE_CONNECTION_ID, dbc);
				final Database db = ClassGenericsUtil.parameterizeOrAbort(StaticArrayDatabase.class, params);
				db.initialize();
				final Relation<?> rel = db.getRelation(TypeUtil.ANY);
				Assert.assertEquals("Database size does not match.", n, rel.size());

				final double lambda = 1;

				// Use max range
				long t1 = System.nanoTime();
				final float[] r1 = om.loop(minPts, lambda, false);
				t1 = System.nanoTime() - t1;

				// Test verses the ELKI frame work
				params = new ListParameterization();
				params.addParameter(de.lmu.ifi.dbs.elki.algorithm.outlier.lof.LoOP.Parameterizer.KCOMP_ID, minPts);
				params.addParameter(de.lmu.ifi.dbs.elki.algorithm.outlier.lof.LoOP.Parameterizer.KREACH_ID, minPts);
				params.addParameter(de.lmu.ifi.dbs.elki.algorithm.outlier.lof.LoOP.Parameterizer.LAMBDA_ID, lambda);
				params.addParameter(
						de.lmu.ifi.dbs.elki.algorithm.outlier.lof.LoOP.Parameterizer.COMPARISON_DISTANCE_FUNCTION_ID,
						EuclideanDistanceFunction.STATIC);
				final Class<de.lmu.ifi.dbs.elki.algorithm.outlier.lof.LoOP<DoubleVector>> clz = ClassGenericsUtil
						.uglyCastIntoSubclass(de.lmu.ifi.dbs.elki.algorithm.outlier.lof.LoOP.class);
				final de.lmu.ifi.dbs.elki.algorithm.outlier.lof.LoOP<DoubleVector> loop = params.tryInstantiate(clz);
				long t2 = System.nanoTime();
				final OutlierResult or = loop.run(db);
				t2 = System.nanoTime() - t2;

				// Check
				//TestSettings.debug("LoOP %d vs %d (ELKI) %f\n", t1, t2, (double)t2 / t1);
				int i = 0;
				final DoubleRelation scores = or.getScores();
				for (final DBIDIter it = scores.iterDBIDs(); it.valid(); it.advance(), i++)
				{
					final String prefix = "[" + i + "] ";

					final int expId = asInteger(it);
					final int obsId = i;

					final double expL = scores.doubleValue(it);
					final double obsL = r1[i];

					//TestSettings.debug("%s %d %d : %f = %f\n", prefix, expId, obsId, expL, obsL);

					Assert.assertEquals(prefix + "Id", expId, obsId);
					TestAssert.assertEqualsRelative(prefix + "LoOP", expL, obsL, 1e-2);
				}
			}
		}
	}

	private static void update(TimingResult r, double[] best)
	{
		final double time = r.getMean();
		if (best[0] > time)
			best[0] = time;
	}

	private static int[][] format(Object result)
	{
		final int[][] n = (int[][]) result;
		for (int i = 0; i < n.length; i++)
			Arrays.sort(n[i]);
		return n;
	}

	private static OPTICSManager createOPTICSManager(int size, int n, RandomGenerator r)
	{
		final double noiseFraction = 0.1;
		final int clusterMin = 2;
		final int clusterMax = 30;
		final double radius = size / 20.0;
		return createOPTICSManager(size, n, noiseFraction, clusterMin, clusterMax, radius, r);
	}

	private static OPTICSManager createOPTICSManager(int size, int n, double noiseFraction, int clusterMin,
			int clusterMax, double radius, RandomGenerator r)
	{
		final float[] xcoord = new float[n];
		final float[] ycoord = new float[xcoord.length];

		int i = 0;
		final RandomDataGenerator rand = new RandomDataGenerator(r);

		// Uniform noise
		final int noise = (int) (noiseFraction * n);
		for (; i < noise; i++)
		{
			xcoord[i] = (float) rand.nextUniform(0, size);
			ycoord[i] = (float) rand.nextUniform(0, size);
		}

		// Clustered
		while (i < n)
		{
			// Create a cluster
			int m = rand.nextInt(clusterMin, clusterMax);
			final double x = r.nextDouble() * size;
			final double y = r.nextDouble() * size;
			while (m > 0 && i < n)
			{
				// Ensure within the image
				double xx = rand.nextGaussian(x, radius);
				while (xx < 0 || xx > size)
					xx = rand.nextGaussian(x, radius);
				double yy = rand.nextGaussian(y, radius);
				while (yy < 0 || yy > size)
					yy = rand.nextGaussian(y, radius);

				xcoord[i] = (float) xx;
				ycoord[i] = (float) yy;
				m--;
				i++;
			}
		}

		final OPTICSManager om = new OPTICSManager(xcoord, ycoord, new Rectangle(size, size));
		return om;
	}
}
