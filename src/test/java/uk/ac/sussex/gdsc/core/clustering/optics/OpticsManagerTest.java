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
 * Copyright (C) 2011 - 2020 Alex Herbert
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

package uk.ac.sussex.gdsc.core.clustering.optics;

import de.lmu.ifi.dbs.elki.algorithm.clustering.optics.AbstractOPTICS;
import de.lmu.ifi.dbs.elki.algorithm.clustering.optics.ClusterOrder;
import de.lmu.ifi.dbs.elki.algorithm.clustering.optics.FastOPTICS;
import de.lmu.ifi.dbs.elki.algorithm.clustering.optics.OPTICSXi;
import de.lmu.ifi.dbs.elki.algorithm.outlier.lof.LoOP;
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
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.sampling.distribution.SharedStateContinuousSampler;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.ac.sussex.gdsc.core.clustering.optics.OpticsManager.Option;
import uk.ac.sussex.gdsc.core.logging.TrackProgress;
import uk.ac.sussex.gdsc.core.match.RandIndex;
import uk.ac.sussex.gdsc.core.utils.MathUtils;
import uk.ac.sussex.gdsc.core.utils.PartialSort;
import uk.ac.sussex.gdsc.core.utils.SimpleArrayUtils;
import uk.ac.sussex.gdsc.core.utils.rng.SamplerUtils;
import uk.ac.sussex.gdsc.test.api.TestAssertions;
import uk.ac.sussex.gdsc.test.api.TestHelper;
import uk.ac.sussex.gdsc.test.api.function.DoubleDoubleBiPredicate;
import uk.ac.sussex.gdsc.test.junit5.RandomSeed;
import uk.ac.sussex.gdsc.test.junit5.SeededTest;
import uk.ac.sussex.gdsc.test.junit5.SpeedTag;
import uk.ac.sussex.gdsc.test.rng.RngUtils;
import uk.ac.sussex.gdsc.test.utils.BaseTimingTask;
import uk.ac.sussex.gdsc.test.utils.TestComplexity;
import uk.ac.sussex.gdsc.test.utils.TestLogUtils;
import uk.ac.sussex.gdsc.test.utils.TestSettings;
import uk.ac.sussex.gdsc.test.utils.TimingResult;
import uk.ac.sussex.gdsc.test.utils.TimingService;
import uk.ac.sussex.gdsc.test.utils.functions.FunctionUtils;

@SuppressWarnings({"javadoc"})
public class OpticsManagerTest {
  private static Logger logger;

  @BeforeAll
  static void beforeAll() {
    // Using ELKI causes the root logger to be changed so
    // initialise ELKI logging then reset
    de.lmu.ifi.dbs.elki.logging.LoggingConfiguration.assertConfigured();
    try {
      final LogManager lm = LogManager.getLogManager();
      lm.readConfiguration();

      // ELKI defaults to warning log level
      lm.getLogger("de.lmu.ifi.dbs.elki").setLevel(Level.WARNING);
    } catch (SecurityException | IOException ex) {
      ex.printStackTrace();
      throw new Error(ex);
    }

    logger = Logger.getLogger(OpticsManagerTest.class.getName());
  }

  @AfterAll
  static void afterAll() {
    logger = null;
  }

  int size = 256;
  float[] radii = new float[] {2, 4, 8, 16};
  int[] ns = new int[] {1000, 2000, 4000, 8000};

  class SimpleMoleculeSpace extends MoleculeSpace {
    OpticsManager opticsManager;
    // All-vs-all distance matrix
    float[][] distances;

    // All-vs-all distance matrix in double - Used for ELKI
    double[][] ddistances;

    SimpleMoleculeSpace(OpticsManager opticsManager, float generatingDistanceE) {
      super(opticsManager.getSize(), generatingDistanceE);
      this.opticsManager = opticsManager;
      generate();
    }

    @Override
    Molecule[] generate() {
      final float[] xcoord = opticsManager.getXData();
      final float[] ycoord = opticsManager.getYData();

      // Compute all-vs-all distance matrix
      final int n = xcoord.length;
      distances = new float[n][n];

      setOfObjects = new Molecule[xcoord.length];
      for (int i = 0; i < xcoord.length; i++) {
        final float x = xcoord[i];
        final float y = ycoord[i];
        // Build a single linked list
        final Molecule m = new Molecule(i, x, y);
        setOfObjects[i] = m;
        for (int j = i; j-- > 0;) {
          distances[i][j] = distances[j][i] = (float) MoleculeDistanceFunctions.SQUARED_EUCLIDEAN_2D
              .applyAsDouble(m, setOfObjects[j]);
        }
      }

      return setOfObjects;
    }

    void createDoubleDistances() {
      final double[][] doubleData = opticsManager.getDoubleData();
      final double[] xcoord = doubleData[0];
      final double[] ycoord = doubleData[1];

      // Compute all-vs-all distance matrix
      final int n = xcoord.length;
      ddistances = new double[n][n];

      for (int i = 0; i < xcoord.length; i++) {
        final double x = xcoord[i];
        final double y = ycoord[i];
        // Build a single linked list
        for (int j = i; j-- > 0;) {
          ddistances[i][j] = ddistances[j][i] = MathUtils.distance(x, y, xcoord[j], ycoord[j]);
        }
      }
    }

    @Override
    void findNeighbours(int minPts, Molecule object, float generatingDistanceE) {
      final float[] fdata = distances[object.id];
      neighbours.clear();
      for (int i = 0; i < fdata.length; i++) {
        if (fdata[i] <= generatingDistanceE) {
          neighbours.add(setOfObjects[i]);
        }
      }
    }

    @Override
    void findNeighboursAndDistances(int minPts, Molecule object, float generatingDistanceE) {
      final float[] fdata = distances[object.id];
      neighbours.clear();
      for (int i = 0; i < fdata.length; i++) {
        if (fdata[i] <= generatingDistanceE) {
          setOfObjects[i].setD(fdata[i]);
          neighbours.add(setOfObjects[i]);
        }
      }
    }
  }

  /**
   * To overcome the 'issue' with the ELKI algorithm using fast-approximations we return the actual
   * values required. We can do this because the dataset is small.
   */
  private class CheatingRandomProjectedNeighborsAndDensities
      extends RandomProjectedNeighborsAndDensities<DoubleVector> {
    // All-vs-all distance matrix
    SimpleMoleculeSpace space;
    Relation<DoubleVector> points;
    int minPts;

    public CheatingRandomProjectedNeighborsAndDensities(SimpleMoleculeSpace space, int minPts) {
      super(RandomFactory.get(30051977L));
      this.space = space;
      this.minPts = minPts;
    }

    // Override the methods used by optics

    @Override
    public void computeSetsBounds(Relation<DoubleVector> points, int minSplitSize, DBIDs ptList) {
      // Store the points
      this.points = points;

      // What is this doing? Just call it anyway.
      super.computeSetsBounds(points, minSplitSize, ptList);
    }

    @Override
    public DoubleDataStore computeAverageDistInSet() {
      // Here we do not use an approximation of the density but actually compute it.
      final WritableDoubleDataStore davg =
          DataStoreUtil.makeDoubleStorage(points.getDBIDs(), DataStoreFactory.HINT_HOT);
      for (final DBIDIter it = points.getDBIDs().iter(); it.valid(); it.advance()) {
        // float[] fdata = space.d[asInteger(it)];
        // double[] data = new StoredDataStatistics(fdata).getValues();

        final double[] data = space.ddistances[asInteger(it)];

        // Simple sort
        // double[] dd = data.clone();
        // Arrays.sort(dd);
        // d = dd[minPts - 1];

        // Partial sort
        // double d = Math.sqrt(PartialSort.bottom(PartialSort.OPTION_HEAD_FIRST, data, minPts)[0]);
        final double d = PartialSort.bottom(PartialSort.OPTION_HEAD_FIRST, data, minPts)[0];

        // This breaks the code
        // davg.put(it, (d <= generatingDistance) ? d : FastOPTICS.UNDEFINED_DISTANCE);
        // This breaks the code
        // davg.put(it, (d <= generatingDistance) ? d : Double.POSITIVE_INFINITY);

        // This is OK. I am not sure how to deal with a smaller generating distance
        davg.put(it, d);
      }
      return davg;
    }

    @Override
    public DataStore<? extends DBIDs> getNeighs() {
      // Not modifying this method appears to work.
      // We could find all Ids below the generating distance fro each point
      return super.getNeighs();
    }
  }

  /**
   * This is injected into the ELKI framework to output the same distances and neighbours.
   */
  private class CopyRandomProjectedNeighborsAndDensities
      extends RandomProjectedNeighborsAndDensities<DoubleVector> {
    // All-vs-all distance matrix
    ProjectedMoleculeSpace space;
    Relation<DoubleVector> points;

    public CopyRandomProjectedNeighborsAndDensities(ProjectedMoleculeSpace space) {
      super(RandomFactory.get(30051977L));
      this.space = space;
    }

    // Override the methods used by optics

    @Override
    public void computeSetsBounds(Relation<DoubleVector> points, int minSplitSize, DBIDs ptList) {
      // Store the points
      this.points = points;
    }

    @Override
    public DoubleDataStore computeAverageDistInSet() {
      // Use the same core distance calculated by the space

      final WritableDoubleDataStore davg =
          DataStoreUtil.makeDoubleStorage(points.getDBIDs(), DataStoreFactory.HINT_HOT);
      for (final DBIDIter it = points.getDBIDs().iter(); it.valid(); it.advance()) {
        final int id = asInteger(it);
        final Molecule m = space.setOfObjects[id];
        final double d = (m.coreDistance != OpticsManager.UNDEFINED) ? m.getCoreDistance()
            : FastOPTICS.UNDEFINED_DISTANCE;
        davg.put(it, d);
      }
      return davg;
    }

    @Override
    public DataStore<? extends DBIDs> getNeighs() {
      // Use the same neighbours calculated by the space

      final DBIDs ids = points.getDBIDs();
      // init lists
      final WritableDataStore<ModifiableDBIDs> neighs =
          DataStoreUtil.makeStorage(ids, DataStoreFactory.HINT_HOT, ModifiableDBIDs.class);
      for (final DBIDIter it = ids.iter(); it.valid(); it.advance()) {
        neighs.put(it, DBIDUtil.newHashSet());
      }

      final DBIDVar v = DBIDUtil.newVar();
      final int[][] allNeighbours = space.getAllNeighbours();
      for (int i = allNeighbours.length; i-- > 0;) {
        final int[] list = allNeighbours[i];
        final ArrayModifiableDBIDs nids = DBIDUtil.newArray(list.length);
        for (final int id : list) {
          nids.add(DBIDFactory.FACTORY.importInteger(id));
        }
        v.assignVar(i, v);
        neighs.get(v).addDBIDs(nids);
      }

      return neighs;
    }
  }

  private static class AssertionTracker implements TrackProgress {
    static final AssertionTracker INSTANCE = new AssertionTracker();

    @Override
    public void progress(double fraction) {
      // Ignore
    }

    @Override
    public void progress(long position, long total) {
      // Ignore
    }

    @Override
    public void incrementProgress(double fraction) {
      // Ignore
    }

    @Override
    public void log(String format, Object... args) {
      Assertions.assertNotNull(String.format(format, args));
    }

    @Override
    public void status(String format, Object... args) {
      Assertions.assertNotNull(String.format(format, args));
    }

    @Override
    public boolean isEnded() {
      return false;
    }

    @Override
    public boolean isProgress() {
      return true;
    }

    @Override
    public boolean isLog() {
      return true;
    }

    @Override
    public boolean isStatus() {
      return true;
    }
  }

  @Test
  public void testBadZCoords() {
    final float[] x = {0};
    final float[] z = {0, 1};
    Assertions.assertThrows(IllegalArgumentException.class, () -> new OpticsManager(x, x, null, 1));
    Assertions.assertThrows(IllegalArgumentException.class, () -> new OpticsManager(x, x, z, 1));
  }

  @Test
  public void testNumberOfThreads() {
    final int expected = Runtime.getRuntime().availableProcessors();
    final float[] x = {0};
    final OpticsManager om = new OpticsManager(x, x, 1);
    Assertions.assertEquals(expected, om.getNumberOfThreads());
    om.setNumberOfThreads(3);
    Assertions.assertEquals(3, om.getNumberOfThreads());
    om.setNumberOfThreads(-3);
    Assertions.assertEquals(1, om.getNumberOfThreads());
  }

  @Test
  public void testRandomSeed() {
    final float[] x = {0};
    final OpticsManager om = new OpticsManager(x, x, 1);
    Assertions.assertEquals(0, om.getRandomSeed());
    om.setRandomSeed(42);
    Assertions.assertEquals(42, om.getRandomSeed());
  }

  @Test
  public void testOptions() {
    final float[] x = {0};
    final OpticsManager om = new OpticsManager(x, x, 1);
    final Set<Option> options = om.getOptions();
    Assertions.assertTrue(options.isEmpty());
    om.addOptions(Option.CACHE);
    Assertions.assertTrue(options.contains(Option.CACHE));
    om.removeOptions(Option.CACHE);
    Assertions.assertTrue(options.isEmpty());
  }

  @Test
  public void testGetExecutorService() {
    final float[] x = {0};
    final OpticsManager om = new OpticsManager(x, x, 1);
    Assertions.assertNull(om.getExecutorService());
    om.setNumberOfThreads(3);
    Assertions.assertNotNull(om.getExecutorService());
    Assertions.assertFalse(om.getExecutorService().isShutdown());
    om.shutdownExecutorService();
    final ExecutorService es = om.getExecutorService();
    Assertions.assertNotNull(es);
    Assertions.assertFalse(om.getExecutorService().isShutdown());
    es.shutdown();
    Assertions.assertNotNull(om.getExecutorService());
    Assertions.assertFalse(om.getExecutorService().isShutdown());
    om.shutdownExecutorService();
  }

  @Test
  public void testNearestNeighbourDistanceEmpty() {
    final float[] x = {0};
    final OpticsManager om = new OpticsManager(x, x, 10);
    Assertions.assertArrayEquals(new float[0], om.nearestNeighbourDistance(5, 5, false));
  }

  @Test
  public void testNearestNeighbourDistance2d() {
    final float[] x = SimpleArrayUtils.newArray(5, 0f, 1f);
    final OpticsManager om = new OpticsManager(x, x, 4 * 4);
    om.setTracker(AssertionTracker.INSTANCE);
    final float end = (float) Math.sqrt(2 * 2 + 2 * 2);
    final float mid = (float) Math.sqrt(1 * 1 + 1 * 1);
    final float[] expected = {end, mid, mid, mid, end};
    Assertions.assertArrayEquals(expected, om.nearestNeighbourDistance(2, 5, true));
    Assertions.assertArrayEquals(expected, om.nearestNeighbourDistance(2, 0, true));
    // Random
    final float[] distances = om.nearestNeighbourDistance(2, 2, false);
    for (final float d : distances) {
      Assertions.assertTrue(d <= end);
      Assertions.assertTrue(d >= mid);
    }
  }

  @Test
  public void testNearestNeighbourDistance3d() {
    final float[] x = SimpleArrayUtils.newArray(5, 0f, 1f);
    final OpticsManager om = new OpticsManager(x, x, x, 4 * 4 * 4);
    final float end = (float) Math.sqrt(2 * 2 + 2 * 2 + 2 * 2);
    final float mid = (float) Math.sqrt(1 * 1 + 1 * 1 + 1 * 1);
    final float[] expected = {end, mid, mid, mid, end};
    Assertions.assertArrayEquals(expected, om.nearestNeighbourDistance(2, 5, true));
    Assertions.assertArrayEquals(expected, om.nearestNeighbourDistance(2, 0, true));
    // Random
    final float[] distances = om.nearestNeighbourDistance(2, 2, false);
    for (final float d : distances) {
      Assertions.assertTrue(d <= end);
      Assertions.assertTrue(d >= mid);
    }
  }

  @Test
  public void testLoopEmpty() {
    final float[] x = {0};
    final OpticsManager om = new OpticsManager(x, x, 10);
    Assertions.assertArrayEquals(new float[1], om.loop(2, 3, false));
  }

  @Test
  public void testLoop2d() {
    final float[] x = {0, 1, 2, 3, 10};
    final OpticsManager om = new OpticsManager(x, x, 1);
    om.setTracker(AssertionTracker.INSTANCE);
    final float[] scores = om.loop(2, 0.5, true);
    Assertions.assertTrue(scores[4] > scores[0]);
    Assertions.assertTrue(scores[0] > scores[1]);
    Assertions.assertEquals(scores[1], scores[2]);
    Assertions.assertEquals(scores[0], scores[3]);
    final float[] scores2 = om.loop(10, 0.5, false);
    Assertions.assertTrue(scores2[4] > scores2[0]);
  }

  @Test
  public void testLoop2dColocated() {
    final float[] x = {0, 0, 0, 0, 1, 10};
    final OpticsManager om = new OpticsManager(x, x, 1);
    om.setTracker(AssertionTracker.INSTANCE);
    // The method should be robust to ignore self for colocated points
    final float[] scores = om.loop(2, 0.5, true);
    Assertions.assertTrue(scores[5] > scores[0]);
  }

  @Test
  public void testLoop3d() {
    final float[] x = {0, 1, 2, 3, 10};
    final OpticsManager om = new OpticsManager(x, x, x, 1);
    om.setTracker(AssertionTracker.INSTANCE);
    final float[] scores = om.loop(2, 0.5, true);
    Assertions.assertTrue(scores[4] > scores[0]);
    Assertions.assertTrue(scores[0] > scores[1]);
    Assertions.assertEquals(scores[1], scores[2]);
    Assertions.assertEquals(scores[0], scores[3]);
    final float[] scores2 = om.loop(10, 0.5, false);
    Assertions.assertTrue(scores2[4] > scores2[0]);
  }

  /**
   * Test the results of Optics using the ELKI framework.
   */
  @SeededTest
  public void canComputeOptics(RandomSeed seed) {
    // This does not fail but logs warnings

    final UniformRandomProvider rg = RngUtils.create(seed.getSeed());
    final TrackProgress tracker = null; // new SimpleTrackProgress();
    final int[] minPoints =
        (TestSettings.allow(TestComplexity.LOW)) ? new int[] {5, 10} : new int[] {10};
    final DoubleDoubleBiPredicate equality = TestHelper.doublesAreClose(1e-5, 0);
    for (final int n : new int[] {100, 500}) {
      final OpticsManager om = createOpticsManager(size, n, rg);
      om.setTracker(tracker);
      // Needed to match the ELKI framework
      om.addOptions(Option.OPTICS_STRICT_REVERSE_ID_ORDER);

      final SimpleMoleculeSpace space = new SimpleMoleculeSpace(om, 0);
      space.createDoubleDistances();

      // Use ELKI to provide the expected results
      final double[][] data = new Array2DRowRealMatrix(om.getDoubleData()).transpose().getData();

      for (final int minPts : minPoints) {
        // Reset starting Id to 1
        final DatabaseConnection dbc = new ArrayAdapterDatabaseConnection(data, null, 0);
        final ListParameterization params = new ListParameterization();
        params.addParameter(AbstractDatabase.Parameterizer.DATABASE_CONNECTION_ID, dbc);
        final Database db =
            ClassGenericsUtil.parameterizeOrAbort(StaticArrayDatabase.class, params);
        db.initialize();
        final Relation<?> rel = db.getRelation(TypeUtil.ANY);
        Assertions.assertEquals(n, rel.size(), "Database size does not match.");

        // Debug: Print the core distance for each point
        // for (int i = 0; i < n; i++)
        // {
        // double[] dd = d[i].clone();
        // Arrays.sort(dd);
        // TestLog.info(logger,"%d Core %f, next %f", i, dd[minPts - 1], dd[minPts]);
        // }

        // Use max range
        final OpticsResult r1 = om.optics(size, minPts);

        // Test verses the ELKI frame work
        final RandomProjectedNeighborsAndDensities<DoubleVector> projections =
            new CheatingRandomProjectedNeighborsAndDensities(space, minPts);
        final FastOPTICS<DoubleVector> fo = new FastOPTICS<>(minPts, projections);
        final ClusterOrder order = fo.run(db);

        // Check
        int index = 0;
        final DBIDVar pre = DBIDUtil.newVar();
        for (final DBIDIter it = order.iter(); it.valid(); it.advance(), index++) {
          if (index == 0) {
            // No predecessor or reachability distance
            continue;
          }

          final int expId = asInteger(it);
          final int obsId = r1.get(index).parent;

          order.getPredecessor(it, pre);
          final int expPre = asInteger(pre);
          final int obsPre = r1.get(index).predecessor;

          final double expR = order.getReachability(it);
          final double obsR = r1.get(index).getReachabilityDistance();

          // TestLog.debug(logger,"[%d] %d %d : %f = %f (%f) : %s = %d", i, expId, obsId, expR,
          // obsR,
          // r1.get(i).coreDistance, expPre, obsPre);

          Assertions.assertEquals(expId, obsId, FunctionUtils.getSupplier("[%d] Id", index));
          Assertions.assertEquals(expPre, obsPre, FunctionUtils.getSupplier("[%d] Pre", index));
          TestAssertions.assertTest(expR, obsR, equality,
              FunctionUtils.getSupplier("[%d] R", index));
        }
      }
    }
  }

  private static int asInteger(DBIDRef id) {
    return DBIDUtil.asInteger(id);
  }

  /**
   * Test the results of Fast Optics using the ELKI framework.
   */
  @SeededTest
  public void canComputeFastOptics(RandomSeed seed) {
    final UniformRandomProvider rg = RngUtils.create(seed.getSeed());
    final TrackProgress tracker = null; // new SimpleTrackProgress();
    final int[] minPoints =
        (TestSettings.allow(TestComplexity.LOW)) ? new int[] {5, 10} : new int[] {10};
    final DoubleDoubleBiPredicate equality = TestHelper.doublesAreClose(1e-5, 0);
    for (final int n : new int[] {100, 500}) {
      final OpticsManager om = createOpticsManager(size, n, rg);
      om.setTracker(tracker);
      // Needed to match the ELKI framework
      om.addOptions(Option.OPTICS_STRICT_REVERSE_ID_ORDER, Option.CACHE);

      final SimpleMoleculeSpace space = new SimpleMoleculeSpace(om, 0);
      space.createDoubleDistances();

      // Use ELKI to provide the expected results
      final double[][] data = new Array2DRowRealMatrix(om.getDoubleData()).transpose().getData();

      for (final int minPts : minPoints) {
        // Reset starting Id to 1
        final DatabaseConnection dbc = new ArrayAdapterDatabaseConnection(data, null, 0);
        final ListParameterization params = new ListParameterization();
        params.addParameter(AbstractDatabase.Parameterizer.DATABASE_CONNECTION_ID, dbc);
        final Database db =
            ClassGenericsUtil.parameterizeOrAbort(StaticArrayDatabase.class, params);
        db.initialize();
        final Relation<?> rel = db.getRelation(TypeUtil.ANY);
        Assertions.assertEquals(n, rel.size(), "Database size does not match.");

        // Debug: Print the core distance for each point
        // for (int i = 0; i < n; i++)
        // {
        // double[] dd = d[i].clone();
        // Arrays.sort(dd);
        // TestLog.info(logger,"%d Core %f, next %f", i, dd[minPts - 1], dd[minPts]);
        // }

        final OpticsResult r1 = om.fastOptics(minPts);

        // Test verses the ELKI frame work
        final RandomProjectedNeighborsAndDensities<DoubleVector> projections =
            new CopyRandomProjectedNeighborsAndDensities((ProjectedMoleculeSpace) om.moleculeSpace);
        final FastOPTICS<DoubleVector> fo = new FastOPTICS<>(minPts, projections);
        final ClusterOrder order = fo.run(db);

        // Check
        int index = 0;
        final DBIDVar pre = DBIDUtil.newVar();
        for (final DBIDIter it = order.iter(); it.valid(); it.advance(), index++) {
          if (index == 0) {
            // No predecessor or reachability distance
            continue;
          }

          final int expId = asInteger(it);
          final int obsId = r1.get(index).parent;

          order.getPredecessor(it, pre);
          final int expPre = asInteger(pre);
          final int obsPre = r1.get(index).predecessor;

          final double expR = order.getReachability(it);
          final double obsR = r1.get(index).getReachabilityDistance();

          // TestLog.debug(logger,"[%d] %d %d : %f = %f (%f) : %s = %d", i, expId, obsId, expR,
          // obsR,
          // r1.get(i).coreDistance, expPre, obsPre);

          Assertions.assertEquals(expId, obsId, FunctionUtils.getSupplier("Id %d", index));
          Assertions.assertEquals(expPre, obsPre, FunctionUtils.getSupplier("Pre %d", index));
          TestAssertions.assertTest(expR, obsR, equality, FunctionUtils.getSupplier("R %d", index));
        }
      }
    }
  }

  /**
   * Test the results of Optics using the ELKI framework.
   */
  @SeededTest
  public void canComputeOpticsXi(RandomSeed seed) {
    // This does not fail but logs warnings

    final UniformRandomProvider rg = RngUtils.create(seed.getSeed());
    final TrackProgress tracker = null; // new SimpleTrackProgress();
    final int[] minPoints =
        (TestSettings.allow(TestComplexity.LOW)) ? new int[] {5, 10} : new int[] {10};
    for (final int n : new int[] {100, 500}) {
      final OpticsManager om = createOpticsManager(size, n, rg);
      om.setTracker(tracker);
      // Needed to match the ELKI framework
      om.addOptions(Option.OPTICS_STRICT_REVERSE_ID_ORDER);

      // Compute the all-vs-all distance for checking the answer
      final SimpleMoleculeSpace space = new SimpleMoleculeSpace(om, 0);
      space.createDoubleDistances();

      // Use ELKI to provide the expected results
      final double[][] data = new Array2DRowRealMatrix(om.getDoubleData()).transpose().getData();

      for (final int minPts : minPoints) {
        // Reset starting Id to 1
        final DatabaseConnection dbc = new ArrayAdapterDatabaseConnection(data, null, 0);
        final ListParameterization params = new ListParameterization();
        params.addParameter(AbstractDatabase.Parameterizer.DATABASE_CONNECTION_ID, dbc);
        final Database db =
            ClassGenericsUtil.parameterizeOrAbort(StaticArrayDatabase.class, params);
        db.initialize();
        final Relation<?> rel = db.getRelation(TypeUtil.ANY);
        Assertions.assertEquals(n, rel.size(), "Database size does not match.");

        // Debug: Print the core distance for each point
        // for (int i = 0; i < n; i++)
        // {
        // double[] dd = d[i].clone();
        // Arrays.sort(dd);
        // TestLog.info(logger,"%d Core %f, next %f", i, dd[minPts - 1], dd[minPts]);
        // }

        // Use max range
        final OpticsResult r1 = om.optics(size, minPts);

        // Test verses the ELKI frame work
        final RandomProjectedNeighborsAndDensities<DoubleVector> index =
            new CheatingRandomProjectedNeighborsAndDensities(space, minPts);
        final FastOPTICS<DoubleVector> fo = new FastOPTICS<>(minPts, index);

        final double xi = 0.03;

        final OPTICSXi opticsXi = new OPTICSXi(fo, xi, false, false);
        final Clustering<OPTICSModel> clustering = opticsXi.run(db);

        // Check by building the clusters into an array
        final int[] expClusters = new int[n];
        final List<de.lmu.ifi.dbs.elki.data.Cluster<OPTICSModel>> allClusters =
            clustering.getAllClusters();
        int clusterId = 0;
        for (final de.lmu.ifi.dbs.elki.data.Cluster<OPTICSModel> c : allClusters) {
          // TestLog.debug(logger,"%d-%d", c.getModel().getStartIndex(),
          // c.getModel().getEndIndex());

          // Add the cluster Id to the expClusters
          clusterId++;
          for (final DBIDIter it = c.getIDs().iter(); it.valid(); it.advance()) {
            expClusters[asInteger(it)] = clusterId;
          }
        }

        // check the clusters match
        r1.extractClusters(xi);
        final int[] obsClusters = r1.getClusters();

        // for (int i = 0; i < n; i++)
        // TestLog.info(logger,"%d = %d %d", i, expClusters[i], obsClusters[i]);

        Assertions.assertEquals(1, RandIndex.randIndex(expClusters, obsClusters));
      }
    }
  }

  @SeededTest
  public void canComputeOpticsXiWithNoHierarchy(RandomSeed seed) {
    final UniformRandomProvider rg = RngUtils.create(seed.getSeed());
    final int[] minPoints =
        (TestSettings.allow(TestComplexity.LOW)) ? new int[] {5, 10} : new int[] {10};
    for (final int n : new int[] {100, 500}) {
      final OpticsManager om = createOpticsManager(size, n, rg);

      for (final int minPts : minPoints) {
        final OpticsResult r1 = om.optics(0, minPts);

        final double xi = 0.03;

        // check the clusters match
        r1.extractClusters(xi);
        final List<OpticsCluster> o1 = r1.getAllClusters();

        r1.extractClusters(xi, OpticsResult.XI_OPTION_TOP_LEVEL);
        final List<OpticsCluster> o2 = r1.getAllClusters();

        Assertions.assertTrue(o1.size() >= o2.size());

        // TestLog.debug(logger,"%d : %d", n, minPts);
        for (final OpticsCluster cluster : o2) {
          Assertions.assertTrue(cluster.getLevel() == 0);
          // TestLog.debug(logger,cluster);
        }
      }
    }
  }

  /**
   * Test the results of FastOPTICS using the ELKI framework.
   */
  @SeededTest
  public void canComputeSimilarFastOpticsToElki(RandomSeed seed) {
    // This does not fail but logs warnings

    final UniformRandomProvider rg = RngUtils.create(seed.getSeed());
    final int nLoops = (TestSettings.allow(TestComplexity.LOW)) ? 5 : 1;
    final RandIndex ri = new RandIndex();
    final TrackProgress tracker = null; // new SimpleTrackProgress();
    for (final int n : new int[] {500}) {
      final OpticsManager om = createOpticsManager(size, n, rg);
      om.setTracker(tracker);
      // Needed to match the ELKI framework
      om.addOptions(Option.OPTICS_STRICT_REVERSE_ID_ORDER);

      // Use ELKI to provide the expected results
      final double[][] data = new Array2DRowRealMatrix(om.getDoubleData()).transpose().getData();

      for (final int minPts : new int[] {4}) {
        double sum = 0;
        for (int loop = 0; loop < nLoops; loop++) {
          final long loopSeed = seed.getSeedAsLong() + loop + 1;
          // Reset starting Id to 1
          final DatabaseConnection dbc = new ArrayAdapterDatabaseConnection(data, null, 0);
          ListParameterization params = new ListParameterization();
          params.addParameter(AbstractDatabase.Parameterizer.DATABASE_CONNECTION_ID, dbc);
          final Database db =
              ClassGenericsUtil.parameterizeOrAbort(StaticArrayDatabase.class, params);
          db.initialize();
          final Relation<?> rel = db.getRelation(TypeUtil.ANY);
          Assertions.assertEquals(n, rel.size(), "Database size does not match.");

          // Use the same settings as ELKI
          final int logOProjectionConst = 20;
          final int dim = 2;
          final int nSplits = (int) (logOProjectionConst * MathUtil.log2(size * dim + 1));
          final int nProjections = nSplits;
          final boolean useRandomVectors = true;
          final boolean saveApproximateSets = true;
          final SampleMode sampleMode = SampleMode.MEDIAN;
          om.setRandomSeed(loopSeed);
          final OpticsResult r1 = om.fastOptics(minPts, nSplits, nProjections, useRandomVectors,
              saveApproximateSets, sampleMode);

          // Test verses the ELKI frame work
          params = new ListParameterization();
          params.addParameter(AbstractOPTICS.Parameterizer.MINPTS_ID, minPts);
          params.addParameter(RandomProjectedNeighborsAndDensities.Parameterizer.RANDOM_ID,
              loopSeed);
          final Class<FastOPTICS<DoubleVector>> clz =
              ClassGenericsUtil.uglyCastIntoSubclass(FastOPTICS.class);
          final FastOPTICS<DoubleVector> fo = params.tryInstantiate(clz);

          final double xi = 0.03;

          final OPTICSXi opticsXi = new OPTICSXi(fo, xi, false, false);
          final Clustering<OPTICSModel> clustering = opticsXi.run(db);

          // Check by building the clusters into an array
          final int[] expClusters = new int[n];
          final List<de.lmu.ifi.dbs.elki.data.Cluster<OPTICSModel>> allClusters =
              clustering.getAllClusters();
          int clusterId = 0;
          for (final de.lmu.ifi.dbs.elki.data.Cluster<OPTICSModel> c : allClusters) {
            // Add the cluster Id to the expClusters
            clusterId++;
            for (final DBIDIter it = c.getIDs().iter(); it.valid(); it.advance()) {
              expClusters[asInteger(it)] = clusterId;
            }
          }

          // check the clusters match
          r1.extractClusters(xi);
          final int[] obsClusters = r1.getClusters();

          // for (int i = 0; i < n; i++)
          // TestLog.debug(logger,"%d = %d %d", i, expClusters[i], obsClusters[i]);

          // Should be similar
          ri.compute(expClusters, obsClusters);

          final double randIndex = ri.getRandIndex();
          logger.log(TestLogUtils.getResultRecord(ri.getAdjustedRandIndex() > 0,
              "FastOPTICS vs ELKI : %d,%d : [%d] r=%f (%f)", n, minPts, loop, randIndex,
              ri.getAdjustedRandIndex()));
          // Assertions.assertTrue(ri.getAdjustedRandIndex() > 0);
          sum += randIndex;
        }

        sum /= nLoops;

        logger.log(TestLogUtils.getResultRecord(sum > 0.6, "FastOPTICS vs ELKI : %d,%d : r=%f", n,
            minPts, sum));
        // Assertions.assertTrue(sum > 0.6);
      }
    }
  }

  @SpeedTag
  @SeededTest
  public void canComputeFastOpticsFasterThanElki(RandomSeed seed) {
    Assumptions.assumeTrue(TestSettings.allow(TestComplexity.MEDIUM));

    final UniformRandomProvider rg = RngUtils.create(seed.getSeed());
    final TrackProgress tracker = null; // new SimpleTrackProgress();
    for (final int n : new int[] {2000}) {
      final OpticsManager om = createOpticsManager(size, n, rg);
      om.setTracker(tracker);
      // Needed to match the ELKI framework
      om.addOptions(Option.OPTICS_STRICT_REVERSE_ID_ORDER);

      // Use ELKI to provide the expected results
      final double[][] data = new Array2DRowRealMatrix(om.getDoubleData()).transpose().getData();

      for (final int minPts : new int[] {4}) {
        // Reset starting Id to 1
        final DatabaseConnection dbc = new ArrayAdapterDatabaseConnection(data, null, 0);
        ListParameterization params = new ListParameterization();
        params.addParameter(AbstractDatabase.Parameterizer.DATABASE_CONNECTION_ID, dbc);
        final Database db =
            ClassGenericsUtil.parameterizeOrAbort(StaticArrayDatabase.class, params);
        db.initialize();
        final Relation<?> rel = db.getRelation(TypeUtil.ANY);
        Assertions.assertEquals(n, rel.size(), "Database size does not match.");

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
        final Class<FastOPTICS<DoubleVector>> clz =
            ClassGenericsUtil.uglyCastIntoSubclass(FastOPTICS.class);
        final FastOPTICS<DoubleVector> fo = params.tryInstantiate(clz);

        final double xi = 0.03;
        final OPTICSXi opticsXi = new OPTICSXi(fo, xi, false, false);

        final long t1 = System.nanoTime();
        opticsXi.run(db);
        final long t2 = System.nanoTime();
        om.fastOptics(minPts, nSplits, nProjections, useRandomVectors, saveApproximateSets,
            sampleMode);
        final long t3 = System.nanoTime();
        om.fastOptics(minPts);
        final long t4 = System.nanoTime();

        final long elki = t2 - t1;
        final long smlm1 = t3 - t2;
        final long smlm2 = t4 - t3;
        logger.log(TestLogUtils.getResultRecord(smlm1 < elki, "ELKI = %d, SMLM = %d = %f", elki,
            smlm1, elki / (double) smlm1));
        logger.log(TestLogUtils.getResultRecord(smlm2 < elki, "ELKI = %d, SMLM (default) = %d = %f",
            elki, smlm2, elki / (double) smlm2));
        // Assertions.assertTrue(smlm1 < elki);
        // Assertions.assertTrue(smlm2 < elki);
      }
    }
  }

  @SeededTest
  public void canComputeSimilarFastOpticsTopLevelClusters(RandomSeed seed) {
    canComputeSimilarFastOptics(seed, 0, 0.7);
  }

  @SeededTest
  public void canComputeSimilarFastOpticsXi(RandomSeed seed) {
    canComputeSimilarFastOptics(seed, 0.03, 0.5);
  }

  private void canComputeSimilarFastOptics(RandomSeed seed, double xi, double randMin) {
    final UniformRandomProvider rg = RngUtils.create(seed.getSeed());
    final RandIndex ri = new RandIndex();
    final TrackProgress tracker = null; // new SimpleTrackProgress();
    final boolean[] both = new boolean[] {true, false};
    final int[] minPoints =
        (TestSettings.allow(TestComplexity.LOW)) ? new int[] {5, 10} : new int[] {10};

    for (final int n : new int[] {500}) {
      final OpticsManager om = createOpticsManager(size, n, rg);
      om.setTracker(tracker);
      om.addOptions(Option.OPTICS_STRICT_ID_ORDER);
      om.setRandomSeed(seed.getSeedAsLong());

      for (final int minPts : minPoints) {
        // Default using ALL
        final int nSplits = 0;
        final int nProjections = 0;
        final int[] c1 =
            runFastOptics(om, xi, minPts, nSplits, nProjections, false, false, SampleMode.ALL);
        for (final SampleMode sampleMode : SampleMode.values()) {
          double sum = 0;
          int count = 0;
          for (final boolean useRandomVectors : both) {
            for (final boolean saveApproximateSets : both) {
              final int[] c2 = runFastOptics(om, xi, minPts, nSplits, nProjections,
                  useRandomVectors, saveApproximateSets, sampleMode);

              // Should be similar
              ri.compute(c1, c2);
              final double randIndex = ri.getRandIndex();
              sum += randIndex;
              count++;
              final double ari = ri.getAdjustedRandIndex();
              logger.info(FunctionUtils.getSupplier(
                  "xi=%f, n=%d, minPts=%d, splits=%d, projections=%d, randomVectors=%b, "
                      + "approxSets=%b, sampleMode=%s : r=%f (%f)",
                  xi, n, minPts, nSplits, nProjections, useRandomVectors, saveApproximateSets,
                  sampleMode, randIndex, ari));

              if (ari <= 0) {
                System.out.println(Arrays.toString(c1));
                System.out.println(Arrays.toString(c2));
              }
              // This should always be true, i.e. better than chance
              Assertions.assertTrue(0 < ari, () -> {
                return String.format("Adjusted rand index is below zero: %s (ARI=%s)", sampleMode,
                    ari);
              });
            }
          }
          final double averageRandIndex = sum / count;
          // This may fail with certain random seeds
          logger.log(TestLogUtils.getResultRecord(randMin < averageRandIndex,
              "xi=%f, n=%d, minPts=%d, splits=%d, projections=%d, sampleMode=%s : r=%f", xi, n,
              minPts, nSplits, nProjections, sampleMode, averageRandIndex));
        }
      }
    }
  }

  private static int[] runFastOptics(OpticsManager om, double xi, int minPts, int numberOfSplits,
      int numberOfProjections, boolean useRandomVectors, boolean saveApproximateSets,
      SampleMode sampleMode) {
    final OpticsResult r1 = om.fastOptics(minPts, numberOfSplits, numberOfProjections,
        useRandomVectors, saveApproximateSets, sampleMode);
    if (xi > 0) {
      r1.extractClusters(xi);
    }
    return r1.getClusters();
  }

  // The following tests are optional since they test non-default Optics processing

  @SeededTest
  public void canComputeOpticsWithInnerProcessing(RandomSeed seed) {
    Assumptions.assumeTrue(TestSettings.allow(TestComplexity.MEDIUM));
    canComputeOpticsWithOptions(seed, Option.INNER_PROCESSING);
  }

  @SeededTest
  public void canComputeOpticsWithCircularProcessing(RandomSeed seed) {
    Assumptions.assumeTrue(TestSettings.allow(TestComplexity.MEDIUM));
    canComputeOpticsWithOptions(seed, Option.CIRCULAR_PROCESSING);
  }

  @SeededTest
  public void canComputeOpticsWithInnerCircularProcessing(RandomSeed seed) {
    Assumptions.assumeTrue(TestSettings.allow(TestComplexity.MEDIUM));
    canComputeOpticsWithOptions(seed, Option.INNER_PROCESSING, Option.CIRCULAR_PROCESSING);
  }

  @SeededTest
  public void canComputeOpticsWithSimpleQueue(RandomSeed seed) {
    Assumptions.assumeTrue(TestSettings.allow(TestComplexity.MEDIUM));
    // This fails as the order is different when we do not use ID to order the objects when
    // reachability distance is equal
    // canComputeOpticsWithOptions(seed, Option.OPTICS_SIMPLE_PRIORITY_QUEUE);

    // We can do a simple check that the cluster ID and core distance are the same for each object.
    // Since the processing order is different we cannot check the reachability distance or the
    // predecessor.
    canComputeOpticsWithOptions(seed, true, Option.OPTICS_SIMPLE_PRIORITY_QUEUE);
  }

  @SeededTest
  public void canComputeOpticsWithSimpleQueueReverseIdOrderD(RandomSeed seed) {
    Assumptions.assumeTrue(TestSettings.allow(TestComplexity.MEDIUM));
    canComputeOpticsWithOptions(seed, new Option[] {Option.OPTICS_STRICT_REVERSE_ID_ORDER},
        Option.OPTICS_SIMPLE_PRIORITY_QUEUE);
  }

  @SeededTest
  public void canComputeOpticsWithSimpleQueueIdOrder(RandomSeed seed) {
    Assumptions.assumeTrue(TestSettings.allow(TestComplexity.MEDIUM));
    canComputeOpticsWithOptions(seed, new Option[] {Option.OPTICS_STRICT_ID_ORDER},
        Option.OPTICS_SIMPLE_PRIORITY_QUEUE);
  }

  private void canComputeOpticsWithOptions(RandomSeed seed, Option... options) {
    canComputeOpticsWithOptions(seed, false, options);
  }

  private void canComputeOpticsWithOptions(RandomSeed seed, boolean simpleCheck,
      Option... options) {
    final UniformRandomProvider rg = RngUtils.create(seed.getSeed());
    final int[] minPoints =
        (TestSettings.allow(TestComplexity.LOW)) ? new int[] {5, 10} : new int[] {10};
    for (final int n : new int[] {100, 500}) {
      final OpticsManager om1 = createOpticsManager(size, n, rg);
      final OpticsManager om2 = om1.copy(false);
      om2.addOptions(options);

      for (final int minPts : minPoints) {
        // Use max range
        final OpticsResult r1 = om1.optics(0, minPts);
        final OpticsResult r1b = om1.optics(0, minPts);
        final OpticsResult r2 = om2.optics(0, minPts);

        opticsAreEqual("repeat", r1, r1b);
        if (simpleCheck) {
          areEqualClusters(r1, r2);
        } else {
          opticsAreEqual("new", r1, r2);
        }
      }
    }
  }

  private void canComputeOpticsWithOptions(RandomSeed seed, Option[] options1, Option... options) {
    final UniformRandomProvider rg = RngUtils.create(seed.getSeed());
    final int[] minPoints =
        (TestSettings.allow(TestComplexity.LOW)) ? new int[] {5, 10} : new int[] {10};
    for (final int n : new int[] {100, 500}) {
      final OpticsManager om1 = createOpticsManager(size, n, rg);
      om1.addOptions(options1);
      final OpticsManager om2 = om1.copy(false);
      om2.addOptions(options);

      for (final int minPts : minPoints) {
        // Use max range
        final OpticsResult r1 = om1.optics(0, minPts);
        final OpticsResult r1b = om1.optics(0, minPts);
        final OpticsResult r2 = om2.optics(0, minPts);

        opticsAreEqual("repeat", r1, r1b);
        opticsAreEqual("new", r1, r2);
      }
    }
  }

  private static void opticsAreEqual(String title, OpticsResult r1, OpticsResult r2) {
    final DoubleDoubleBiPredicate equality =
        TestHelper.doublesAreClose(1e-5, 0).or(TestHelper.doublesEqual());
    for (int i = 0; i < r1.size(); i++) {
      // Edge-points are random so ignore them. Only do core points.
      if (!r1.get(i).isCorePoint() || !r1.get(i).isCorePoint()) {
        continue;
      }

      final double expC = r1.get(i).getCoreDistance();
      final double obsC = r2.get(i).getCoreDistance();

      final int expId = r1.get(i).parent;
      final int obsId = r2.get(i).parent;

      final int expPre = r1.get(i).predecessor;
      final int obsPre = r2.get(i).predecessor;

      final double expR = r1.get(i).getReachabilityDistance();
      final double obsR = r2.get(i).getReachabilityDistance();

      // TestLog.debug(logger,"[%d] %d %d : %f = %f (%f) : %s = %d", i, expId, obsId, expR, obsR,
      // r1.get(i).coreDistance, expPre, obsPre);

      TestAssertions.assertTest(expC, obsC, equality,
          FunctionUtils.getSupplier("%s C %d", title, i));
      Assertions.assertEquals(expId, obsId, FunctionUtils.getSupplier("%s Id %d", title, i));
      Assertions.assertEquals(expPre, obsPre, FunctionUtils.getSupplier("%s Pre %d", title, i));
      TestAssertions.assertTest(expR, obsR, equality,
          FunctionUtils.getSupplier("%s R %d", title, i));
    }
  }

  private static void areEqualClusters(OpticsResult r1, OpticsResult r2) {
    // We check the core distance and cluster ID are the same for each parent
    final double[] core1 = r1.getCoreDistance(false);
    final double[] core2 = r2.getCoreDistance(false);

    Assertions.assertArrayEquals(core1, core2, "Core");

    final int[] cluster1 = r1.getClusters();
    final int[] cluster2 = r2.getClusters();

    Assertions.assertArrayEquals(cluster1, cluster2, "Cluster");
  }

  @SeededTest
  public void canComputeDbscanWithGridProcessing(RandomSeed seed) {
    Assumptions.assumeTrue(TestSettings.allow(TestComplexity.MEDIUM));
    canComputeDbscanWithOptions(seed, Option.GRID_PROCESSING);
  }

  @SeededTest
  public void canComputeDbscanWithCircularProcessing(RandomSeed seed) {
    Assumptions.assumeTrue(TestSettings.allow(TestComplexity.MEDIUM));
    canComputeDbscanWithOptions(seed, Option.CIRCULAR_PROCESSING);
  }

  @SeededTest
  public void canComputeDbscanWithInnerProcessingCircular(RandomSeed seed) {
    Assumptions.assumeTrue(TestSettings.allow(TestComplexity.MEDIUM));
    canComputeDbscanWithOptions(seed, Option.INNER_PROCESSING, Option.CIRCULAR_PROCESSING);
  }

  private void canComputeDbscanWithOptions(RandomSeed seed, Option... options) {
    final UniformRandomProvider rg = RngUtils.create(seed.getSeed());
    final int[] minPoints =
        (TestSettings.allow(TestComplexity.LOW)) ? new int[] {5, 10} : new int[] {10};
    for (final int n : new int[] {100, 500, 5000}) {
      final OpticsManager om1 = createOpticsManager(size, n, rg);
      final OpticsManager om2 = om1.copy(false);
      om2.addOptions(options);

      for (final int minPts : minPoints) {
        final DbscanResult r1 = om1.dbscan(0, minPts);
        final DbscanResult r1b = om1.dbscan(0, minPts);
        final DbscanResult r2 = om2.dbscan(0, minPts);

        dbscanAreEqual("repeat", r1, r1b, minPts);
        dbscanAreEqual("new", r1, r2, minPts);
      }
    }
  }

  private static void dbscanAreEqual(String title, DbscanResult r1, DbscanResult r2, int minPts) {
    for (int i = 0; i < r1.size(); i++) {
      final int expPts = r1.get(i).numberOfPoints;
      final int obsPts = r2.get(i).numberOfPoints;

      // Edge-points are random so ignore them. Only do core points.
      if (expPts < minPts || obsPts < minPts) {
        continue;
      }

      Assertions.assertEquals(expPts, obsPts, FunctionUtils.getSupplier("%s Pts %d", title, i));

      final int expId = r1.get(i).parent;
      final int obsId = r2.get(i).parent;

      final int expCId = r1.get(i).getClusterId();
      final int obsCId = r2.get(i).getClusterId();

      Assertions.assertEquals(expId, obsId, FunctionUtils.getSupplier("%s Id %d", title, i));
      Assertions.assertEquals(expCId, obsCId, FunctionUtils.getSupplier("%s CId %d", title, i));
    }
  }

  @SeededTest
  public void canPerformOpticsWithLargeData(RandomSeed seed) {
    Assumptions.assumeTrue(TestSettings.allow(TestComplexity.MEDIUM));
    final UniformRandomProvider rg = RngUtils.create(seed.getSeed());
    final TrackProgress tracker = null; // new SimpleTrackProgress();
    for (final int n : ns) {
      final OpticsManager om = createOpticsManager(size, n, rg);
      om.setTracker(tracker);

      for (final int minPts : new int[] {10, 20}) {
        om.optics(0, minPts);
      }
    }

    // Use to speed time the algorithm when making changes
    // long time2 = 0;
    // for (int i = 10; i-- > 0;)
    // {
    // long time = System.nanoTime();
    // for (int n : N)
    // {
    // OpticsManager om = createOpticsManager(size, n, rg);
    //
    // for (int minPts : new int[] { 10, 20 })
    // {
    // om.optics(0, minPts);
    // }
    // }
    // time = System.nanoTime() - time;
    // TestLog.info(logger,"Time = %d", time);
    // if (i < 5)
    // time2 += time;
    // }
    // TestLog.info(logger,"Time = %d", time2);
  }

  @SeededTest
  public void canComputeKnnDistance(RandomSeed seed) {
    final UniformRandomProvider rg = RngUtils.create(seed.getSeed());
    final int n = 100;
    final OpticsManager om = createOpticsManager(size, n, rg);

    // All-vs-all distance matrix
    final float[][] data = om.getData();
    final float[] x = data[0];
    final float[] y = data[1];
    final double[][] d2 = new double[n][n];
    for (int i = 0; i < n; i++) {
      for (int j = i + 1; j < n; j++) {
        d2[i][j] = d2[j][i] = MathUtils.distance2((double) x[i], y[i], x[j], y[j]);
      }
    }

    // Try all including invalid bounds
    for (int k : new int[] {0, 1, 3, 5, n - 1, n}) {
      final float[] o = om.nearestNeighbourDistance(k, -1, true);
      final float[] e = new float[n];
      // Set the correct bounds on k
      if (k >= n) {
        k = n - 1;
      }
      if (k < 1) {
        k = 1;
      }
      for (int i = 0; i < n; i++) {
        e[i] = (float) Math
            .sqrt(PartialSort.bottom(PartialSort.OPTION_HEAD_FIRST, d2[i], n, k + 1)[0]);
      }
      // TestLog.debug(logger,"e=%s, o=%s", Arrays.toString(e), Arrays.toString(o));
      Assertions.assertArrayEquals(e, o);
    }
  }

  @SeededTest
  public void canComputeKnnDistanceWithBigData(RandomSeed seed) {
    Assumptions.assumeTrue(TestSettings.allow(TestComplexity.MEDIUM));
    final UniformRandomProvider rg = RngUtils.create(seed.getSeed());
    for (final int n : ns) {
      final OpticsManager om = createOpticsManager(size, n, rg);

      for (final int k : new int[] {3, 5}) {
        final float[] d = om.nearestNeighbourDistance(k, -1, true);
        Assertions.assertEquals(d.length, n);
      }
    }
  }

  @SeededTest
  public void canComputeKnnDistanceWithSample(RandomSeed seed) {
    final UniformRandomProvider rg = RngUtils.create(seed.getSeed());
    for (final int n : ns) {
      final OpticsManager om = createOpticsManager(size, n, rg);

      final int samples = n / 10;
      for (final int k : new int[] {3, 5}) {
        final float[] d = om.nearestNeighbourDistance(k, samples, true);
        Assertions.assertEquals(d.length, samples);
      }
    }
  }

  @Test
  public void canComputeGeneratingDistance() {
    // 1000 points in 10000 area => 10 area / point
    Assertions.assertEquals(3.56825, OpticsManager.computeGeneratingDistance(4, 10000, false, 1000),
        1e-3);
    Assertions.assertEquals(6.67558,
        OpticsManager.computeGeneratingDistance(14, 10000, false, 1000), 1e-3);
    // 1000 points in 10000 volume => 10 volume / point
    Assertions.assertEquals(2.1215688,
        OpticsManager.computeGeneratingDistance(4, 10000, true, 1000), 1e-3);
    Assertions.assertEquals(3.221166,
        OpticsManager.computeGeneratingDistance(14, 10000, true, 1000), 1e-3);
  }

  @SeededTest
  public void canRepeatOptics(RandomSeed seed) {
    final UniformRandomProvider rg = RngUtils.create(seed.getSeed());
    final int n = ns[0];
    final OpticsManager om = createOpticsManager(size, n, rg);

    final float radius = 0;

    final int minPts = 10;
    Assertions.assertFalse(om.hasMemory());

    final Set<Option> opt = om.getOptions();

    opt.add(OpticsManager.Option.CACHE);
    final OpticsResult r1 = om.optics(radius, minPts);
    Assertions.assertTrue(om.hasMemory());

    opt.remove(OpticsManager.Option.CACHE);
    final OpticsResult r2 = om.optics(radius, minPts);
    Assertions.assertFalse(om.hasMemory());

    Assertions.assertEquals(r1.size(), r2.size());
    for (int i = r1.size(); i-- > 0;) {
      Assertions.assertEquals(r1.get(i).parent, r2.get(i).parent);
      Assertions.assertEquals(r1.get(i).clusterId, r2.get(i).clusterId);
      Assertions.assertEquals(r1.get(i).getCoreDistance(), r2.get(i).getCoreDistance());
      Assertions.assertEquals(r1.get(i).getReachabilityDistance(),
          r2.get(i).getReachabilityDistance());
    }
  }

  @SeededTest
  public void canPerformOpticsWithTinyRadius(RandomSeed seed) {
    final UniformRandomProvider rg = RngUtils.create(seed.getSeed());
    final int minPts = 10;
    for (final int n : ns) {
      final OpticsManager om = createOpticsManager(size, n, rg);

      for (final float radius : new float[] {0.01f}) {
        om.optics(radius, minPts);
        // TestLog.debug(logger,"Optics %d @ %.1f,%d", n, radius, minPts);
      }
    }
  }

  @SeededTest
  public void canPerformOpticsWith1Point(RandomSeed seed) {
    final UniformRandomProvider rg = RngUtils.create(seed.getSeed());
    final OpticsManager om = createOpticsManager(size, 1, rg);

    for (final float radius : new float[] {-1, 0, 0.01f, 1f}) {
      for (final int minPts : new int[] {-1, 0, 1}) {
        final OpticsResult r1 = om.optics(radius, minPts);
        // Should be 1 cluster
        Assertions.assertEquals(1, r1.get(0).clusterId);
      }
    }

    final OpticsResult r1 = om.optics(1, 2);
    // Should be 0 clusters as the min size is too high
    Assertions.assertEquals(0, r1.get(0).clusterId);
  }

  @Test
  public void canPerformOpticsWithColocatedData() {
    final OpticsManager om = new OpticsManager(new float[10], new float[10], size * size);

    for (final float radius : new float[] {-1, 0, 0.01f, 1f}) {
      for (final int minPts : new int[] {-1, 0, 1, 10}) {
        final OpticsResult r1 = om.optics(radius, minPts);
        // All should be in the same cluster
        Assertions.assertEquals(1, r1.get(0).clusterId);
      }
    }
  }

  @SeededTest
  public void canConvertOpticsToDbscan(RandomSeed seed) {
    final UniformRandomProvider rg = RngUtils.create(seed.getSeed());
    final int n = ns[0];
    final OpticsManager om = createOpticsManager(size, n, rg);

    final float radius = radii[radii.length - 1];

    final int minPts = 10;
    final OpticsResult r1 = om.optics(radius, minPts);
    // Store for later and reset
    final int[] clusterId = new int[r1.size()];
    for (int i = r1.size(); i-- > 0;) {
      clusterId[i] = r1.get(i).clusterId;
      r1.get(i).clusterId = -1;
    }
    // Smaller distance
    int totalClusters = r1.extractDbscanClustering(radius * 0.8f);
    int max = 0;
    // int[] clusterId2 = new int[r1.size()];
    for (int i = r1.size(); i-- > 0;) {
      if (max < r1.get(i).clusterId) {
        max = r1.get(i).clusterId;
      }
      // clusterId2[i] = r1.get(i).clusterId;
      Assertions.assertNotEquals(r1.get(i).clusterId, -1);
    }
    Assertions.assertEquals(totalClusters, max);
    // Same distance
    totalClusters = r1.extractDbscanClustering(radius);
    for (int i = r1.size(); i-- > 0;) {
      Assertions.assertEquals(r1.get(i).clusterId, clusterId[i]);
    }
  }

  /**
   * Test the results of Dbscan using Optics.
   */
  @SeededTest
  public void canComputeDbscan(RandomSeed seed) {
    final UniformRandomProvider rg = RngUtils.create(seed.getSeed());
    final int[] minPoints =
        (TestSettings.allow(TestComplexity.LOW)) ? new int[] {5, 10} : new int[] {10};
    for (final int n : new int[] {100, 500}) {
      final OpticsManager om = createOpticsManager(size, n, rg);
      // Keep items in memory for speed during the test
      om.addOptions(OpticsManager.Option.CACHE);

      for (final int minPts : minPoints) {
        // Use default range
        final OpticsResult r1 = om.optics(0, minPts);
        final DbscanResult r2 = om.dbscan(0, minPts);

        areSameClusters(r1, r2);
      }
    }
  }

  private static void areSameClusters(OpticsResult r1, DbscanResult r2) {
    // Check. Remove non-core points as Optics and Dbscan differ in the
    // processing order within a cluster.
    final int[] c1 = r1.getClusters(true);
    final int[] c2 = r2.getClusters(true);

    // for (int i = 0; i < c1.length; i++)
    // {
    // TestLog.info(logger,"[%d] %d == %d", i, c1[i], c2[i]);
    // }

    Assertions.assertArrayEquals(c1, c2);
  }

  @SeededTest
  public void testDbscanIsFasterThanOptics(RandomSeed seed) {
    Assumptions.assumeTrue(TestSettings.allow(TestComplexity.MEDIUM));

    final UniformRandomProvider rg = RngUtils.create(seed.getSeed());
    final OpticsManager om1 = createOpticsManager(size, 5000, rg);
    final OpticsManager om2 = om1.copy(false);

    final long t1 = System.nanoTime();
    final OpticsResult r1 = om1.optics(0, 10);
    long t2 = System.nanoTime();
    final DbscanResult r2 = om2.dbscan(0, 10);
    long t3 = System.nanoTime();

    areSameClusters(r1, r2);

    t3 = t3 - t2;
    t2 = t2 - t1;

    Assertions.assertTrue(t3 < t2);

    // Note: The Optics paper reports that it should be about 1.6x slower than Dbscan
    // This test shows a different value due to:
    // - unrealistic data
    // - The optimised Dbscan implementation not computing distances if not needed.

    logger.info(FunctionUtils.getSupplier("dBSCANIsFasterThanOptics %d < %d (%.2f)", t3, t2,
        (double) t2 / t3));
  }

  @SeededTest
  public void canMatchDbscanCorePointsWithOptics(RandomSeed seed) {
    final UniformRandomProvider rg = RngUtils.create(seed.getSeed());
    for (final int n : new int[] {100, 500}) {
      final OpticsManager om = createOpticsManager(size, n, rg);
      // Keep items in memory for speed during the test
      om.addOptions(OpticsManager.Option.CACHE);

      for (final int minPts : new int[] {5, 20}) {
        for (final float e : new float[] {0, 4}) {
          final OpticsResult r1 = om.optics(e, minPts);

          // Try smaller radius for Dbscan
          for (int i = 2; i <= 5; i++) {
            final float d = r1.getGeneratingDistance() / i;
            final DbscanResult r2 = om.dbscan(d, minPts);

            // Now extract core points
            r1.extractDbscanClustering(d, true);
            final int[] c1 = r1.getClusters();
            final int[] c2 = r2.getClusters(true);

            Assertions.assertEquals(1, RandIndex.randIndex(c1, c2));
          }
        }
      }
    }
  }

  @SpeedTag
  @SeededTest
  public void testDbscanInnerCircularIsFasterWhenDensityIsHigh(RandomSeed seed) {
    Assumptions.assumeTrue(TestSettings.allow(TestComplexity.MEDIUM));

    final UniformRandomProvider rg = RngUtils.create(seed.getSeed());
    final int molecules = 10000;
    final OpticsManager om1 = createOpticsManager(size, molecules, rg);
    final OpticsManager om2 = om1.copy(false);
    om1.addOptions(Option.GRID_PROCESSING);
    om2.addOptions(Option.CIRCULAR_PROCESSING, Option.INNER_PROCESSING);

    float generatingDistanceE = 0;
    final double moleculesInPixel = (double) molecules / (size * size);
    double moleculesInCircle;
    final int limit = RadialMoleculeSpace.N_MOLECULES_FOR_NEXT_RESOLUTION_INNER * 2;
    do {
      generatingDistanceE += 0.1f;
      moleculesInCircle = Math.PI * generatingDistanceE * generatingDistanceE * moleculesInPixel;
    } while (moleculesInCircle < limit && generatingDistanceE < size);

    final int minPts = 20;

    DbscanResult r1;
    DbscanResult r2;

    // Warm-up
    // r1 = om1.dbscan(generatingDistanceE, minPts);
    // r2 = om2.dbscan(generatingDistanceE, minPts);

    final long t1 = System.nanoTime();
    r1 = om1.dbscan(generatingDistanceE, minPts);
    long t2 = System.nanoTime();
    r2 = om2.dbscan(generatingDistanceE, minPts);
    long t3 = System.nanoTime();

    dbscanAreEqual("new", r1, r2, minPts);

    t3 = t3 - t2;
    t2 = t2 - t1;

    logger.log(
        TestLogUtils.getTimingRecord("Dbscan Grid High Density", t2, "Dbscan Inner Cicrular", t3));
  }

  @SpeedTag
  @SeededTest
  public void testOpticsCircularIsFasterWhenDensityIsHigh(RandomSeed seed) {
    Assumptions.assumeTrue(TestSettings.allow(TestComplexity.MEDIUM));

    final UniformRandomProvider rg = RngUtils.create(seed.getSeed());
    final int molecules = 10000;
    final OpticsManager om1 = createOpticsManager(size, molecules, rg);
    final OpticsManager om2 = om1.copy(false);
    om1.addOptions(Option.GRID_PROCESSING);
    om2.addOptions(Option.CIRCULAR_PROCESSING);

    float generatingDistanceE = 0;
    final double moleculesInPixel = (double) molecules / (size * size);
    double moleculesInCircle;
    final int limit = RadialMoleculeSpace.N_MOLECULES_FOR_NEXT_RESOLUTION_OUTER;
    do {
      generatingDistanceE += 0.1f;
      moleculesInCircle = Math.PI * generatingDistanceE * generatingDistanceE * moleculesInPixel;
    } while (moleculesInCircle < limit && generatingDistanceE < size);

    final int minPts = 20;

    OpticsResult r1;
    OpticsResult r2;

    // Warm-up
    // r1 = om1.optics(generatingDistanceE, minPts);
    // r2 = om2.optics(generatingDistanceE, minPts);

    final long t1 = System.nanoTime();
    r1 = om1.optics(generatingDistanceE, minPts);
    long t2 = System.nanoTime();
    r2 = om2.optics(generatingDistanceE, minPts);
    long t3 = System.nanoTime();

    opticsAreEqual("new", r1, r2);

    t3 = t3 - t2;
    t2 = t2 - t1;

    logger.log(TestLogUtils.getTimingRecord("Optics Grid High Density", t2, "Optics Circular", t3));
  }

  private enum TestMoleculeSpace {
    SIMPLE, GRID, RADIAL, INNER_RADIAL, TREE
  }

  private abstract class MyTimingTask extends BaseTimingTask {
    TestMoleculeSpace ms;
    boolean generate;
    OpticsManager[] om;
    int minPts;
    float generatingDistanceE;
    /** The generating distance squared. */
    float e2;
    int resolution;
    Option[] options;
    String name;

    public MyTimingTask(TestMoleculeSpace ms, boolean generate, OpticsManager[] om, int minPts,
        float generatingDistanceE, int resolution, Option... options) {
      super(ms.toString());
      this.ms = ms;
      this.generate = generate;
      this.om = om;
      this.generatingDistanceE = generatingDistanceE;
      this.resolution = resolution;
      this.minPts = minPts;
      e2 = generatingDistanceE * generatingDistanceE;
      this.options = options;
    }

    @Override
    public int getSize() {
      return om.length;
    }

    @Override
    public Object getData(int index) {
      // Create the molecule space
      if (options != null) {
        om[index].addOptions(options);
      }
      MoleculeSpace space = null;
      switch (ms) {
        case SIMPLE:
          space = new SimpleMoleculeSpace(om[index], generatingDistanceE);
          break;
        case GRID:
          space = new GridMoleculeSpace(om[index], generatingDistanceE, resolution);
          break;
        case RADIAL:
          space = new RadialMoleculeSpace(om[index], generatingDistanceE, resolution);
          break;
        case INNER_RADIAL:
          space = new InnerRadialMoleculeSpace(om[index], generatingDistanceE, resolution);
          break;
        case TREE:
          space = new FloatTreeMoleculeSpace(om[index], generatingDistanceE);
          break;
        default:
          Assertions.fail("Unknown option: " + ms);
      }
      if (options != null) {
        om[index].removeOptions(options);
      }
      if (generate) {
        generate(space);
      }
      return space;
    }

    void generate(MoleculeSpace space) {
      space.generate();
      if (name == null) {
        name = space.toString();
      }
    }

    @Override
    public String getName() {
      return name;
    }
  }

  private abstract class FindNeighboursTimingTask extends MyTimingTask {
    public FindNeighboursTimingTask(TestMoleculeSpace ms, boolean generate, OpticsManager[] om,
        int minPts, float generatingDistanceE, int resolution, Option... options) {
      super(ms, generate, om, minPts, generatingDistanceE, resolution, options);
    }

    public FindNeighboursTimingTask(TestMoleculeSpace ms, OpticsManager[] om, int minPts,
        float generatingDistanceE, int resolution, Option... options) {
      super(ms, false, om, minPts, generatingDistanceE, resolution, options);
    }

    @Override
    public Object run(Object data) {
      final MoleculeSpace space = (MoleculeSpace) data;
      if (!generate) {
        generate(space);
      }
      final int[][] n = new int[space.size][];
      for (int i = space.size; i-- > 0;) {
        space.findNeighbours(minPts, space.setOfObjects[i], e2);
        final int[] nn = new int[space.neighbours.size];
        for (int j = space.neighbours.size; j-- > 0;) {
          nn[j] = space.neighbours.get(j).id;
        }
        n[i] = nn;
      }
      return n;
    }
  }

  @SeededTest
  public void canTestMoleculeSpaceFindNeighbours(RandomSeed seed) {
    final UniformRandomProvider rg = RngUtils.create(seed.getSeed());
    final OpticsManager[] om = new OpticsManager[5];
    for (int i = 0; i < om.length; i++) {
      om[i] = createOpticsManager(size, 1000, rg);
    }

    final float generatingDistanceE = 10;
    final int minPts = 20;

    // Results
    final int[][][] n = new int[om.length][][];

    final int loops = (logger.isLoggable(Level.INFO)) ? 5 : 1;

    final TimingService ts = new TimingService(loops);
    final boolean check = true;

    ts.execute(
        new FindNeighboursTimingTask(TestMoleculeSpace.SIMPLE, om, minPts, generatingDistanceE, 0) {
          @Override
          public void check(int index, Object result) {
            // Store these as the correct results
            n[index] = format(result);
          }
        }, check);
    ts.execute(
        new FindNeighboursTimingTask(TestMoleculeSpace.GRID, om, minPts, generatingDistanceE, 0) {
          @Override
          public void check(int index, Object result) {
            final int[][] e = n[index];
            final int[][] o = format(result);
            Assertions.assertArrayEquals(e, o, () -> String.format("%s:%d:", getName(), index));
          }
        }, check);
    ts.execute(
        new FindNeighboursTimingTask(TestMoleculeSpace.GRID, om, minPts, generatingDistanceE, 10) {
          @Override
          public void check(int index, Object result) {
            final int[][] e = n[index];
            final int[][] o = format(result);
            Assertions.assertArrayEquals(e, o, () -> String.format("%s:%d:", getName(), index));
          }
        }, check);
    ts.execute(
        new FindNeighboursTimingTask(TestMoleculeSpace.RADIAL, om, minPts, generatingDistanceE, 0) {
          @Override
          public void check(int index, Object result) {
            final String name = getName() + ":" + index + ":";
            final int[][] e = n[index];
            final int[][] o = format(result);
            Assertions.assertArrayEquals(e, o, name);
          }
        }, check);
    ts.execute(new FindNeighboursTimingTask(TestMoleculeSpace.INNER_RADIAL, om, minPts,
        generatingDistanceE, 0) {
      @Override
      public void check(int index, Object result) {
        final int[][] e = n[index];
        final int[][] o = format(result);
        Assertions.assertArrayEquals(e, o, () -> String.format("%s:%d:", getName(), index));
      }
    }, check);
    ts.execute(new FindNeighboursTimingTask(TestMoleculeSpace.RADIAL, om, minPts,
        generatingDistanceE, 10) {
      @Override
      public void check(int index, Object result) {
        final int[][] e = n[index];
        final int[][] o = format(result);
        Assertions.assertArrayEquals(e, o, () -> String.format("%s:%d:", getName(), index));
      }
    }, check);
    ts.execute(new FindNeighboursTimingTask(TestMoleculeSpace.INNER_RADIAL, om, minPts,
        generatingDistanceE, 10) {
      @Override
      public void check(int index, Object result) {
        final int[][] e = n[index];
        final int[][] o = format(result);
        Assertions.assertArrayEquals(e, o, () -> String.format("%s:%d:", getName(), index));
      }
    }, check);
    ts.execute(
        new FindNeighboursTimingTask(TestMoleculeSpace.TREE, om, minPts, generatingDistanceE, 0) {
          @Override
          public void check(int index, Object result) {
            final int[][] e = n[index];
            final int[][] o = format(result);
            Assertions.assertArrayEquals(e, o, () -> String.format("%s:%d:", getName(), index));
          }
        }, check);

    if (loops > 1) {
      logger.info(ts.getReport());
    }
  }

  @SeededTest
  public void canTestMoleculeSpaceFindNeighboursPregenerated(RandomSeed seed) {
    final UniformRandomProvider rg = RngUtils.create(seed.getSeed());
    final OpticsManager[] om = new OpticsManager[5];
    for (int i = 0; i < om.length; i++) {
      om[i] = createOpticsManager(size, 1000, rg);
    }

    final float generatingDistanceE = 10;
    final int minPts = 20;

    // Results
    final int[][][] n = new int[om.length][][];

    final int loops = (logger.isLoggable(Level.INFO)) ? 5 : 1;

    final TimingService ts = new TimingService(loops);
    final boolean check = true;

    ts.execute(new FindNeighboursTimingTask(TestMoleculeSpace.SIMPLE, true, om, minPts,
        generatingDistanceE, 0) {
      @Override
      public void check(int index, Object result) {
        // Store these as the correct results
        n[index] = format(result);
      }
    }, check);
    ts.execute(new FindNeighboursTimingTask(TestMoleculeSpace.GRID, true, om, minPts,
        generatingDistanceE, 0) {
      @Override
      public void check(int index, Object result) {
        final int[][] e = n[index];
        final int[][] o = format(result);
        Assertions.assertArrayEquals(e, o, () -> String.format("%s:%d:", getName(), index));
      }
    }, check);
    ts.execute(new FindNeighboursTimingTask(TestMoleculeSpace.GRID, true, om, minPts,
        generatingDistanceE, 10) {
      @Override
      public void check(int index, Object result) {
        final int[][] e = n[index];
        final int[][] o = format(result);
        Assertions.assertArrayEquals(e, o, () -> String.format("%s:%d:", getName(), index));
      }
    }, check);
    ts.execute(new FindNeighboursTimingTask(TestMoleculeSpace.RADIAL, true, om, minPts,
        generatingDistanceE, 0) {
      @Override
      public void check(int index, Object result) {
        final int[][] e = n[index];
        final int[][] o = format(result);
        Assertions.assertArrayEquals(e, o, () -> String.format("%s:%d:", getName(), index));
      }
    }, check);
    ts.execute(new FindNeighboursTimingTask(TestMoleculeSpace.INNER_RADIAL, true, om, minPts,
        generatingDistanceE, 0) {
      @Override
      public void check(int index, Object result) {
        final int[][] e = n[index];
        final int[][] o = format(result);
        Assertions.assertArrayEquals(e, o, () -> String.format("%s:%d:", getName(), index));
      }
    }, check);
    ts.execute(new FindNeighboursTimingTask(TestMoleculeSpace.RADIAL, true, om, minPts,
        generatingDistanceE, 10) {
      @Override
      public void check(int index, Object result) {
        final int[][] e = n[index];
        final int[][] o = format(result);
        Assertions.assertArrayEquals(e, o, () -> String.format("%s:%d:", getName(), index));
      }
    }, check);
    ts.execute(new FindNeighboursTimingTask(TestMoleculeSpace.INNER_RADIAL, true, om, minPts,
        generatingDistanceE, 10) {
      @Override
      public void check(int index, Object result) {
        final int[][] e = n[index];
        final int[][] o = format(result);
        Assertions.assertArrayEquals(e, o, () -> String.format("%s:%d:", getName(), index));
      }
    }, check);
    ts.execute(new FindNeighboursTimingTask(TestMoleculeSpace.TREE, true, om, minPts,
        generatingDistanceE, 0) {
      @Override
      public void check(int index, Object result) {
        final int[][] e = n[index];
        final int[][] o = format(result);
        Assertions.assertArrayEquals(e, o, () -> String.format("%s:%d:", getName(), index));
      }
    }, check);

    if (loops > 1) {
      logger.info(ts.getReport());
    }
  }

  /**
   * This test uses the auto-resolution. It is mainly used to determine when to switch inner circle
   * processing on.
   */
  @SeededTest
  public void canTestMoleculeSpaceFindNeighboursWithAutoResolution(RandomSeed seed) {
    Assumptions.assumeTrue(logger.isLoggable(Level.INFO));
    Assumptions.assumeTrue(TestSettings.allow(TestComplexity.MEDIUM));

    final UniformRandomProvider rg = RngUtils.create(seed.getSeed());
    final int molecules = 20000;
    float generatingDistanceE = 0;
    final int minPts = 20;

    final double moleculesInPixel = (double) molecules / (size * size);
    final int[] moleculesInArea = new int[] {64, 128, 256, 512, 1024};

    // Should this ever be done?
    // This is slow as the number of sorts in the check method is very large
    final boolean check = TestSettings.allow(TestComplexity.HIGH);

    for (final int m : moleculesInArea) {
      // Increase generatingDistance until we achieve the molecules
      double moleculesInCircle;
      do {
        generatingDistanceE += 0.1f;
        moleculesInCircle = Math.PI * generatingDistanceE * generatingDistanceE * moleculesInPixel;
      } while (moleculesInCircle < m && generatingDistanceE < size);

      final double nMoleculesInSquare =
          4 * generatingDistanceE * generatingDistanceE * moleculesInPixel;
      final int maxResolution = (int) Math.ceil(nMoleculesInSquare);

      logger.info(FunctionUtils.getSupplier("Square=%.2f, Circle=%.2f, e=%.1f, r <= %d",
          nMoleculesInSquare, moleculesInCircle, generatingDistanceE, maxResolution));

      final OpticsManager[] om = new OpticsManager[3];
      for (int i = 0; i < om.length; i++) {
        om[i] = createOpticsManager(size, molecules, rg);
      }

      // Results
      final int[][][] n = new int[om.length][][];

      final TimingService ts = new TimingService(1);

      final int resolution = 0;
      ts.execute(new FindNeighboursTimingTask(TestMoleculeSpace.GRID, om, minPts,
          generatingDistanceE, resolution) {
        @Override
        public void check(int index, Object result) {
          n[index] = format(result);
        }
      }, check);
      ts.execute(new FindNeighboursTimingTask(TestMoleculeSpace.RADIAL, om, minPts,
          generatingDistanceE, resolution) {
        @Override
        public void check(int index, Object result) {
          final int[][] e = n[index];
          final int[][] o = format(result);
          Assertions.assertArrayEquals(e, o, () -> String.format("%s:%d:", getName(), index));
        }
      }, check);
      ts.execute(new FindNeighboursTimingTask(TestMoleculeSpace.INNER_RADIAL, om, minPts,
          generatingDistanceE, resolution) {
        @Override
        public void check(int index, Object result) {
          final int[][] e = n[index];
          final int[][] o = format(result);
          Assertions.assertArrayEquals(e, o, () -> String.format("%s:%d:", getName(), index));
        }
      }, check);

      logger.info(ts.getReport());
    }
  }

  /**
   * This tests what resolution to use for a GridMoleculeSpace.
   */
  @SeededTest
  public void canTestGridMoleculeSpaceFindNeighboursWithResolution(RandomSeed seed) {
    Assumptions.assumeTrue(logger.isLoggable(Level.INFO));
    Assumptions.assumeTrue(TestSettings.allow(TestComplexity.HIGH));

    final UniformRandomProvider rg = RngUtils.create(seed.getSeed());
    final int molecules = 50000;
    float generatingDistanceE = 0;
    final int minPts = 20;

    final double moleculesInPixel = (double) molecules / (size * size);
    final int[] moleculesInArea = new int[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 15, 20, 25, 30, 35, 40,
        45, 50, 60, 70, 80, 90, 100, 120, 140, 160, 200, 300};

    for (final int m : moleculesInArea) {
      // Increase generatingDistance until we achieve the molecules
      double moleculesInSquare;
      do {
        generatingDistanceE += 0.1f;
        moleculesInSquare = 4 * generatingDistanceE * generatingDistanceE * moleculesInPixel;
      } while (moleculesInSquare < m && generatingDistanceE < size);

      final double moleculesInCircle =
          Math.PI * generatingDistanceE * generatingDistanceE * moleculesInPixel;
      final int maxResolution = (int) Math.ceil(moleculesInSquare);

      logger.info(FunctionUtils.getSupplier("Square=%.2f, Circle=%.2f, e=%.1f, r <= %d",
          moleculesInSquare, moleculesInCircle, generatingDistanceE, maxResolution));

      final OpticsManager[] om = new OpticsManager[3];
      for (int i = 0; i < om.length; i++) {
        om[i] = createOpticsManager(size, molecules, rg);
      }

      // Results
      final int[][][] n = new int[om.length][][];

      final TimingService ts = new TimingService(1);

      final double[] best = new double[] {Double.MAX_VALUE};
      int noChange = 0;

      for (int resolution = 1; resolution <= maxResolution; resolution++) {
        final double last = best[0];
        final TimingResult result = ts.execute(new FindNeighboursTimingTask(TestMoleculeSpace.GRID,
            om, minPts, generatingDistanceE, resolution) {
          @Override
          public void check(int index, Object result) {
            n[index] = format(result);
          }
        });
        update(result, best);
        if (last == best[0]) {
          noChange++;
        } else {
          noChange = 0;
        }
        if (noChange == 2) {
          break;
        }
      }

      // ts.check();

      logger.info(ts.getReport());
    }
  }

  /**
   * This tests what resolution to use for a RadialMoleculeSpace.
   */
  @SeededTest
  public void canTestRadialMoleculeSpaceFindNeighboursWithResolution(RandomSeed seed) {
    Assumptions.assumeTrue(logger.isLoggable(Level.INFO));
    Assumptions.assumeTrue(TestSettings.allow(TestComplexity.HIGH));

    final UniformRandomProvider rg = RngUtils.create(seed.getSeed());
    final int molecules = 20000;
    float generatingDistanceE = 0;
    final int minPts = 20;

    final double moleculesInPixel = (double) molecules / (size * size);
    final int[] moleculesInArea = new int[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 15, 20, 25, 30, 35, 40,
        45, 50, 60, 70, 80, 90, 100, 120, 140, 160, 200, 300};

    for (final int m : moleculesInArea) {
      // Increase generatingDistance until we achieve the molecules
      double moleculesInCircle;
      do {
        generatingDistanceE += 0.1f;
        moleculesInCircle = Math.PI * generatingDistanceE * generatingDistanceE * moleculesInPixel;
      } while (moleculesInCircle < m && generatingDistanceE < size);

      final double nMoleculesInSquare =
          4 * generatingDistanceE * generatingDistanceE * moleculesInPixel;
      final int maxResolution = (int) Math.ceil(moleculesInCircle);

      logger.info(FunctionUtils.getSupplier("Square=%.2f, Circle=%.2f, e=%.1f, r <= %d",
          nMoleculesInSquare, moleculesInCircle, generatingDistanceE, maxResolution));

      final OpticsManager[] om = new OpticsManager[3];
      for (int i = 0; i < om.length; i++) {
        om[i] = createOpticsManager(size, molecules, rg);
      }

      // Results
      final int[][][] n = new int[om.length][][];

      final TimingService ts = new TimingService(1);

      final double[] best = new double[] {Double.MAX_VALUE};
      int noChange = 0;

      for (int resolution = 1; resolution <= maxResolution; resolution++) {
        final double last = best[0];
        final TimingResult result =
            ts.execute(new FindNeighboursTimingTask(TestMoleculeSpace.RADIAL, om, minPts,
                generatingDistanceE, resolution) {
              @Override
              public void check(int index, Object result) {
                n[index] = format(result);
              }
            });
        update(result, best);
        if (last == best[0]) {
          noChange++;
        } else {
          noChange = 0;
        }
        if (noChange == 2) {
          break;
        }
      }

      // ts.check();

      logger.info(ts.getReport());
    }
  }

  /**
   * This tests what resolution to use for a InnerRadialMoleculeSpace.
   */
  @SeededTest
  public void canTestInnerRadialMoleculeSpaceFindNeighboursWithResolution(RandomSeed seed) {
    Assumptions.assumeTrue(logger.isLoggable(Level.INFO));
    Assumptions.assumeTrue(TestSettings.allow(TestComplexity.HIGH));

    final UniformRandomProvider rg = RngUtils.create(seed.getSeed());
    final int molecules = 20000;
    float generatingDistanceE = 0;
    final int minPts = 20;

    final double moleculesInPixel = (double) molecules / (size * size);
    final int[] moleculesInArea = new int[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 15, 20, 25, 30, 35, 40,
        45, 50, 60, 70, 80, 90, 100, 150, 200, 300, 500, 1000};

    int lastMax = 0;
    for (final int m : moleculesInArea) {
      // Increase generatingDistance until we achieve the molecules
      double moleculesInCircle;
      do {
        generatingDistanceE += 0.1f;
        moleculesInCircle = Math.PI * generatingDistanceE * generatingDistanceE * moleculesInPixel;
      } while (moleculesInCircle < m && generatingDistanceE < size);

      final double nMoleculesInSquare =
          4 * generatingDistanceE * generatingDistanceE * moleculesInPixel;
      final int maxResolution = (int) Math.ceil(moleculesInCircle);

      logger.info(FunctionUtils.getSupplier("Square=%.2f, Circle=%.2f, e=%.1f, r <= %d",
          nMoleculesInSquare, moleculesInCircle, generatingDistanceE, maxResolution));

      final OpticsManager[] om = new OpticsManager[3];
      for (int i = 0; i < om.length; i++) {
        om[i] = createOpticsManager(size, molecules, rg);
      }

      // Results
      final int[][][] n = new int[om.length][][];

      final TimingService ts = new TimingService(1);

      final double[] best = new double[] {Double.MAX_VALUE};
      int noChange = 0;

      for (int resolution = Math.max(1, lastMax - 3); resolution <= maxResolution; resolution++) {
        final double last = best[0];
        final TimingResult result =
            ts.execute(new FindNeighboursTimingTask(TestMoleculeSpace.INNER_RADIAL, om, minPts,
                generatingDistanceE, resolution) {
              @Override
              public void check(int index, Object result) {
                n[index] = format(result);
              }
            });
        update(result, best);
        if (last == best[0]) {
          noChange++;
        } else {
          noChange = 0;
          lastMax = resolution;
        }
        if (noChange == 2) {
          break;
        }
      }

      // ts.check();

      logger.info(ts.getReport());
    }
  }

  private class OpticsTimingTask extends BaseTimingTask {
    int moleculesInArea;
    OpticsManager[] om;
    int minPts;
    float generatingDistanceE;
    Option[] options;
    String name = null;

    public OpticsTimingTask(int moleculesInArea, OpticsManager[] om, int minPts,
        float generatingDistanceE, Option... options) {
      super(options.toString());
      this.moleculesInArea = moleculesInArea;
      this.om = om;
      this.generatingDistanceE = generatingDistanceE;
      this.minPts = minPts;
      this.options = options;
      setName();
    }

    private void setName() {
      final StringBuilder sb = new StringBuilder();
      sb.append("m=").append(moleculesInArea);
      sb.append(" minPts=").append(minPts).append(" e=").append(generatingDistanceE);
      for (final Option o : options) {
        sb.append(' ').append(o.toString());
      }
      name = sb.toString();
    }

    @Override
    public int getSize() {
      return om.length;
    }

    @Override
    public Object getData(int index) {
      return om[index];
    }

    @Override
    public Object run(Object data) {
      final OpticsManager om = (OpticsManager) data;
      // Set the options
      if (options != null) {
        om.addOptions(options);
      }
      final OpticsResult result = om.optics(generatingDistanceE, minPts);
      // Reset
      if (options != null) {
        om.removeOptions(options);
      }
      return result;
    }

    @Override
    public String getName() {
      return name;
    }
  }

  /**
   * Tests the speed of the different queue structures. The default Heap is faster that the simple
   * priority queue when the number of molecules within the generating distance is high. When at the
   * default level then the speed is similar.
   */
  @SeededTest
  public void canTestOpticsQueue(RandomSeed seed) {
    Assumptions.assumeTrue(logger.isLoggable(Level.INFO));
    Assumptions.assumeTrue(TestSettings.allow(TestComplexity.HIGH));

    final UniformRandomProvider rg = RngUtils.create(seed.getSeed());
    final int molecules = 5000;
    float generatingDistanceE = 0;
    final int minPts = 5;

    final double moleculesInPixel = (double) molecules / (size * size);
    final int[] moleculesInArea = new int[] {0, 5, 10, 20, 50};

    final OpticsManager[] om = new OpticsManager[5];
    for (int i = 0; i < om.length; i++) {
      om[i] = createOpticsManager(size, molecules, rg);
      om[i].addOptions(Option.CACHE);
    }

    for (final int m : moleculesInArea) {
      // Increase generatingDistance until we achieve the molecules
      double moleculesInCircle =
          Math.PI * generatingDistanceE * generatingDistanceE * moleculesInPixel;
      while (moleculesInCircle < m && generatingDistanceE < size) {
        generatingDistanceE += 0.1f;
        moleculesInCircle = Math.PI * generatingDistanceE * generatingDistanceE * moleculesInPixel;
      }

      final TimingService ts = new TimingService(3);

      // Run once without timing so the structure is cached
      final OpticsTimingTask task = new OpticsTimingTask(m, om, minPts, generatingDistanceE);
      ts.execute(task);
      ts.clearResults();

      // Note: It is not actually fair to do a speed test without strict ID ordering as the order
      // may dictate the speed
      // as the list of items to process will be built in a different order (and to a different
      // size).
      // So we can compare ID and reverse ID ordering for different structures.
      // But not no ID ordering for different structures as the order will be dictated by the
      // structure itself.
      ts.execute(task);
      ts.execute(new OpticsTimingTask(m, om, minPts, generatingDistanceE,
          Option.OPTICS_SIMPLE_PRIORITY_QUEUE));

      ts.execute(
          new OpticsTimingTask(m, om, minPts, generatingDistanceE, Option.OPTICS_STRICT_ID_ORDER));
      ts.execute(new OpticsTimingTask(m, om, minPts, generatingDistanceE,
          Option.OPTICS_STRICT_ID_ORDER, Option.OPTICS_SIMPLE_PRIORITY_QUEUE));

      ts.execute(new OpticsTimingTask(m, om, minPts, generatingDistanceE,
          Option.OPTICS_STRICT_REVERSE_ID_ORDER));
      ts.execute(new OpticsTimingTask(m, om, minPts, generatingDistanceE,
          Option.OPTICS_STRICT_REVERSE_ID_ORDER, Option.OPTICS_SIMPLE_PRIORITY_QUEUE));

      logger.info(ts.getReport());
    }
  }

  /**
   * Test the results of LoOP using the ELKI framework.
   */
  @SeededTest
  public void canComputeLoop(RandomSeed seed) {
    final UniformRandomProvider rg = RngUtils.create(seed.getSeed());
    final TrackProgress tracker = null; // new SimpleTrackProgress();
    final int[] minPoints =
        (TestSettings.allow(TestComplexity.LOW)) ? new int[] {5, 10} : new int[] {10};
    final DoubleDoubleBiPredicate equality = TestHelper.doublesAreClose(1e-2, 1e-5);
    for (final int n : new int[] {100, 500}) {
      final OpticsManager om = createOpticsManager(size, n, rg);
      om.setTracker(tracker);
      om.setNumberOfThreads(1);

      // Use ELKI to provide the expected results
      final double[][] data = new Array2DRowRealMatrix(om.getDoubleData()).transpose().getData();

      for (final int minPts : minPoints) {
        // Reset starting Id to 1
        final DatabaseConnection dbc = new ArrayAdapterDatabaseConnection(data, null, 0);
        ListParameterization params = new ListParameterization();
        params.addParameter(AbstractDatabase.Parameterizer.DATABASE_CONNECTION_ID, dbc);
        final Database db =
            ClassGenericsUtil.parameterizeOrAbort(StaticArrayDatabase.class, params);
        db.initialize();
        final Relation<?> rel = db.getRelation(TypeUtil.ANY);
        Assertions.assertEquals(n, rel.size(), "Database size does not match.");

        final double lambda = 1;

        // Use max range
        long t1 = System.nanoTime();
        final float[] r1 = om.loop(minPts, lambda, false);
        t1 = System.nanoTime() - t1;

        // Test verses the ELKI frame work
        params = new ListParameterization();
        params.addParameter(LoOP.Parameterizer.KCOMP_ID, minPts);
        params.addParameter(LoOP.Parameterizer.KREACH_ID, minPts);
        params.addParameter(LoOP.Parameterizer.LAMBDA_ID, lambda);
        params.addParameter(LoOP.Parameterizer.COMPARISON_DISTANCE_FUNCTION_ID,
            EuclideanDistanceFunction.STATIC);
        final Class<LoOP<DoubleVector>> clz = ClassGenericsUtil.uglyCastIntoSubclass(LoOP.class);
        final LoOP<DoubleVector> loop = params.tryInstantiate(clz);
        long t2 = System.nanoTime();
        final OutlierResult or = loop.run(db);
        t2 = System.nanoTime() - t2;

        // Check
        // TestLog.debug(logger,"LoOP %d vs %d (ELKI) %f", t1, t2, (double)t2 / t1);
        int index = 0;
        final DoubleRelation scores = or.getScores();
        for (final DBIDIter it = scores.iterDBIDs(); it.valid(); it.advance(), index++) {
          final int expId = asInteger(it);
          final int obsId = index;

          final double expL = scores.doubleValue(it);
          final double obsL = r1[index];

          // TestLog.debug(logger,"%s %d %d : %f = %f", prefix, expId, obsId, expL, obsL);

          Assertions.assertEquals(expId, obsId, FunctionUtils.getSupplier("[%d] Id", index));
          TestAssertions.assertTest(expL, obsL, equality,
              FunctionUtils.getSupplier("[%d] LoOP", index));
        }
      }
    }
  }

  private static void update(TimingResult result, double[] best) {
    final double time = result.getMean();
    if (best[0] > time) {
      best[0] = time;
    }
  }

  private static int[][] format(Object result) {
    final int[][] n = (int[][]) result;
    for (int i = 0; i < n.length; i++) {
      Arrays.sort(n[i]);
    }
    return n;
  }

  private static OpticsManager createOpticsManager(int size, int totalMolecules,
      UniformRandomProvider rng) {
    final double noiseFraction = 0.1;
    final int clusterMin = 2;
    final int clusterMax = 30;
    final double radius = size / 20.0;
    return createOpticsManager(size, totalMolecules, noiseFraction, clusterMin, clusterMax, radius,
        rng);
  }

  private static OpticsManager createOpticsManager(int size, int totalMolecules,
      double noiseFraction, int clusterMin, int clusterMax, double radius,
      UniformRandomProvider rng) {
    final float[] xcoord = new float[totalMolecules];
    final float[] ycoord = new float[xcoord.length];

    int index = 0;

    // Uniform noise
    final int noise = (int) (noiseFraction * totalMolecules);
    for (; index < noise; index++) {
      xcoord[index] = rng.nextInt(size);
      ycoord[index] = rng.nextInt(size);
    }

    // Clustered
    final int range = clusterMax - clusterMin;
    while (index < totalMolecules) {
      // Create a cluster
      int molecules = clusterMin + rng.nextInt(range);
      final double x = rng.nextDouble() * size;
      final double y = rng.nextDouble() * size;
      final SharedStateContinuousSampler gx = SamplerUtils.createGaussianSampler(rng, x, radius);
      final SharedStateContinuousSampler gy = SamplerUtils.createGaussianSampler(rng, y, radius);

      while (molecules > 0 && index < totalMolecules) {
        // Ensure within the image
        double xx = gx.sample();
        while (xx < 0 || xx > size) {
          xx = gx.sample();
        }
        double yy = gy.sample();
        while (yy < 0 || yy > size) {
          yy = gy.sample();
        }

        xcoord[index] = (float) xx;
        ycoord[index] = (float) yy;
        molecules--;
        index++;
      }
    }

    return new OpticsManager(xcoord, ycoord, size * size);
  }
}
