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

package uk.ac.sussex.gdsc.core.clustering;

import uk.ac.sussex.gdsc.core.data.VisibleForTesting;
import uk.ac.sussex.gdsc.core.utils.ConcurrencyUtils;
import uk.ac.sussex.gdsc.core.utils.IntFixedList;
import uk.ac.sussex.gdsc.core.utils.TurboList;

import org.apache.commons.lang3.concurrent.ConcurrentRuntimeException;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Calculate the density of classes of molecules around a given position.
 */
public class DensityCounter {

  /** The square root of 2. */
  private static final double ROOT2 = Math.sqrt(2.0);

  /** Synchronised mode. */
  static final byte MODE_SYNC = 0;
  /** Non-synchronised mode. */
  static final byte MODE_NON_SYNC = 1;

  /** The radius. */
  private final float radius;

  /** The radius squared. */
  final float r2;
  private final float xmin;
  private final float ymin;
  private final float binWidth;
  private final int nxbins;
  private final int nybins;
  private final int moleculesCount;
  /**
   * The grid of molecules.
   *
   * <p>Package private to allow working inner classes access.
   */
  final IndexMolecule[][] grid;

  private final int nonEmpty;
  private final int maxCellSize;

  /** The grid priority. */
  int[] gridPriority;

  // Note:
  // Multi-threading is not faster unless the
  // number of molecules is very large and/or the radius (i.e. the total
  // number of comparisons). However at low numbers of comparison the
  // speed slow-down for multi-threading will likely not be noticed so
  // default to multi-threading.
  private int numberOfThreads = -1;

  /** Multi-thread mode. */
  private byte multiThreadMode = MODE_NON_SYNC;

  /**
   * Specify a molecule.
   */
  public interface Molecule {
    /**
     * Gets the x coordinate. This must remain constant for efficient molecule processing.
     *
     * @return the x
     */
    float getX();

    /**
     * Gets the y coordinate. This must remain constant for efficient molecule processing.
     *
     * @return the y
     */
    float getY();

    /**
     * Gets the id. Must be zero or above.
     *
     * @return the id
     */
    int getId();
  }

  /**
   * Provide a simple class that implements the Molecule interface.
   */
  public static class SimpleMolecule implements DensityCounter.Molecule {
    private final float x;
    private final float y;
    private int id;

    /**
     * Instantiates a new simple molecule.
     *
     * @param x the x
     * @param y the y
     */
    public SimpleMolecule(float x, float y) {
      this(x, y, 0);
    }

    /**
     * Instantiates a new simple molecule.
     *
     * @param x the x
     * @param y the y
     * @param id the id
     */
    public SimpleMolecule(float x, float y, int id) {
      this.x = x;
      this.y = y;
      setId(id);
    }

    @Override
    public float getX() {
      return x;
    }

    @Override
    public float getY() {
      return y;
    }

    @Override
    public int getId() {
      return id;
    }

    /**
     * Sets the id.
     *
     * @param id the new id
     */
    public void setId(int id) {
      if (id < 0) {
        throw new IllegalArgumentException("Id must be positive");
      }
      this.id = id;
    }
  }

  /**
   * Wrap input molecules with an index to the original input order.
   */
  private static class IndexMolecule implements Molecule {
    /** The molecule. */
    Molecule molecule;

    /** The index. */
    final int index;

    /**
     * Instantiates a new index molecule.
     *
     * @param molecule the molecule
     * @param index the index
     */
    IndexMolecule(Molecule molecule, int index) {
      this.molecule = molecule;
      this.index = index;
    }

    /**
     * Gets the x.
     *
     * @return the x
     */
    @Override
    public float getX() {
      return molecule.getX();
    }

    /**
     * Gets the y.
     *
     * @return the y
     */
    @Override
    public float getY() {
      return molecule.getY();
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    @Override
    public int getId() {
      return molecule.getId();
    }
  }

  /**
   * A simple list of molecules.
   */
  private static class MoleculeList {
    int size;
    IndexMolecule[] data = new IndexMolecule[1];

    void add(Molecule molecule, int index) {
      if (size == data.length) {
        data = Arrays.copyOf(data, 2 * size);
      }
      data[size++] = new IndexMolecule(molecule, index);
    }

    IndexMolecule[] toArray() {
      return Arrays.copyOf(data, size);
    }
  }

  /**
   * Instantiates a new density counter.
   *
   * @param molecules the molecules
   * @param radius the radius at which to count the density
   * @param zeroOrigin Set to true if the molecules have a zero origin
   * @throws IllegalArgumentException if results are null or empty
   */
  public DensityCounter(Molecule[] molecules, float radius, boolean zeroOrigin) {
    if (molecules == null || molecules.length == 0) {
      throw new IllegalArgumentException("Molecules must not be empty");
    }
    if (Float.isInfinite(radius) || Float.isNaN(radius) || radius <= 0) {
      throw new IllegalArgumentException("Radius must be a positive real number");
    }
    this.radius = radius;
    r2 = radius * radius;
    moleculesCount = molecules.length;

    if (zeroOrigin) {
      xmin = ymin = 0;
    } else {
      float minx = Float.POSITIVE_INFINITY;
      float miny = Float.POSITIVE_INFINITY;
      for (final Molecule m : molecules) {
        if (minx > m.getX()) {
          minx = m.getX();
        }
        if (miny > m.getY()) {
          miny = m.getY();
        }
      }
      xmin = minx;
      ymin = miny;
    }

    float xmax = Float.NEGATIVE_INFINITY;
    float ymax = Float.NEGATIVE_INFINITY;
    for (final Molecule m : molecules) {
      if (xmax < m.getX()) {
        xmax = m.getX();
      }
      if (ymax < m.getY()) {
        ymax = m.getY();
      }
    }

    // Create grid
    final float xrange = xmax - xmin;
    final float yrange = ymax - ymin;
    binWidth = determineBinWidth(xrange, yrange, radius);

    nxbins = (int) (1 + Math.floor(xrange / binWidth));
    nybins = (int) (1 + Math.floor(yrange / binWidth));

    // Assign to grid
    final MoleculeList[] tmp = new MoleculeList[nxbins * nybins];
    for (int i = 0; i < tmp.length; i++) {
      tmp[i] = new MoleculeList();
    }
    for (int i = 0; i < molecules.length; i++) {
      final Molecule m = molecules[i];
      tmp[getBin(m.getX(), m.getY())].add(m, i);
    }

    // Convert for efficiency
    grid = new IndexMolecule[tmp.length][];
    int count = 0;
    int max = 0;
    for (int i = 0; i < tmp.length; i++) {
      if (tmp[i].size != 0) {
        if (max < tmp[i].size) {
          max = tmp[i].size;
        }
        count++;
        grid[i] = tmp[i].toArray();
      }
      tmp[i] = null;
    }
    nonEmpty = count;
    maxCellSize = max;
  }

  /**
   * Gets the x bin.
   *
   * @param x the x
   * @return the x bin
   */
  private int getXBin(float x) {
    return (int) ((x - xmin) / binWidth);
  }

  /**
   * Gets the y bin.
   *
   * @param y the y
   * @return the y bin
   */
  private int getYBin(float y) {
    return (int) ((y - ymin) / binWidth);
  }

  /**
   * Gets the bin.
   *
   * @param x the x
   * @param y the y
   * @return the bin
   */
  private int getBin(float x, float y) {
    return getBin(getXBin(x), getYBin(y));
  }

  /**
   * Gets the bin.
   *
   * @param xbin the xbin
   * @param ybin the ybin
   * @return the bin
   */
  private int getBin(int xbin, int ybin) {
    return ybin * nxbins + xbin;
  }

  /**
   * Gets the bin.
   *
   * @param x the x
   * @param y the y
   * @return the bin
   */
  private int getBinSafe(float x, float y) {
    final int xBin = clip(nxbins, getXBin(x));
    final int yBin = clip(nybins, getYBin(y));
    return getBin(xBin, yBin);
  }

  private static int clip(int upper, int value) {
    if (value < 0) {
      return 0;
    }
    if (value >= upper) {
      return upper - 1;
    }
    return value;
  }

  /**
   * Determine bin width. This must be equal to or greater than the radius. The iwdth is increased
   * if the total number of bins is prohibitively large.
   *
   * @param xrange the xrange
   * @param yrange the yrange
   * @param radius the radius
   * @return the bin width
   */
  private static float determineBinWidth(float xrange, float yrange, float radius) {
    float binWidth = radius;
    while (getBins(xrange, yrange, binWidth) > 100000) {
      // Dumb implementation that increase the bin width so that each cell doubles in size.
      // A better solution would be to conduct a search for the value with a number of bins close
      // to the target.
      binWidth *= ROOT2;
    }
    return binWidth;
  }

  /**
   * Gets the bins.
   *
   * @param xrange the xrange
   * @param yrange the yrange
   * @param binWidth the bin width
   * @return the bins
   */
  private static double getBins(float xrange, float yrange, float binWidth) {
    // Use a double in case the numbers are very high. This occurs when the bin width is too small.
    final double x = xrange / binWidth;
    final double y = yrange / binWidth;
    if (x * y > Integer.MAX_VALUE) {
      return x * y;
    }

    final double nXBins = 1 + Math.floor(x);
    final double nYBins = 1 + Math.floor(y);
    return nXBins * nYBins;
  }

  /**
   * Gets the molecules. This returns a copy of the molecule extracted from their storage format
   * into a new array.
   *
   * @return the molecules
   */
  public Molecule[] getMolecules() {
    // Extract the molecules
    final Molecule[] molecules = new Molecule[moleculesCount];
    for (final IndexMolecule[] cell1 : grid) {
      if (cell1 == null) {
        continue;
      }
      for (int j = cell1.length; j-- > 0;) {
        molecules[cell1[j].index] = cell1[j].molecule;
      }
    }
    return molecules;
  }

  /**
   * Count the density of each class of molecule around each molecule. Counts are returned using the
   * original input order of molecules.
   *
   * <p>This method allows the molecule ID to be changed following creation of the counter but
   * coordinates must be the same. The maximum ID must be input to allow efficient counting.
   *
   * <p>This method is optimised for use when the number of IDs is small. If the number of IDs is
   * large then the routine may run out of memory.
   *
   * @param maxId the max ID of molecules
   * @return the counts
   */
  @VisibleForTesting
  int[][] countAllSimple(int maxId) {
    return countAllSimple(getMolecules(), r2, maxId);
  }

  /**
   * Count the density of each class of molecule around each molecule. Counts are returned using the
   * original input order of molecules.
   *
   * <p>This method is optimised for use when the number of IDs is small. If the number of IDs is
   * large then the routine may run out of memory.
   *
   * @param molecules the molecules
   * @param r2 the squared radius distance
   * @param maxId the max ID of molecules
   * @return the counts
   */
  private static int[][] countAllSimple(Molecule[] molecules, float r2, int maxId) {
    final int moleculesLength = molecules.length;
    final int[][] results = new int[moleculesLength][maxId + 1];

    // All-vs-all
    for (int i = 0; i < moleculesLength; i++) {
      final Molecule m1 = molecules[i];
      final int[] count1 = results[i];
      final float x = m1.getX();
      final float y = m1.getY();
      final int id = m1.getId();

      // Self count
      count1[id]++;

      for (int j = i + 1; j < moleculesLength; j++) {
        final Molecule m2 = molecules[j];
        if (distance2(x, y, m2) < r2) {
          final int[] count2 = results[j];
          count1[m2.getId()]++;
          count2[id]++;
        }
      }
    }

    return results;
  }

  /**
   * Count the density of each class of molecule around each input molecule. Counts are returned
   * using the original input order of molecules.
   *
   * <p>This method is optimised for use when the number of IDs is small. If the number of IDs is
   * large then the routine may run out of memory. .
   *
   * @param searchMolecules the molecules to around which to search
   * @param maxId the max ID of molecules
   * @return the counts
   */
  @VisibleForTesting
  int[][] countAllSimple(Molecule[] searchMolecules, int maxId) {
    return countAllSimple(getMolecules(), searchMolecules, r2, maxId);
  }

  /**
   * Count the density of each class of molecule around each input molecule. Counts are returned
   * using the original input order of molecules.
   *
   * <p>This method is optimised for use when the number of IDs is small. If the number of IDs is
   * large then the routine may run out of memory.
   *
   * @param molecules the molecules to create the density space
   * @param searchMolecules the molecules to around which to search
   * @param r2 the squared radius distance
   * @param maxId the max ID of molecules
   * @return the counts
   */
  private static int[][] countAllSimple(Molecule[] molecules, Molecule[] searchMolecules, float r2,
      int maxId) {
    final int moleculesLength = searchMolecules.length;
    final int[][] results = new int[moleculesLength][maxId + 1];

    // All-vs-all
    for (int i = 0; i < moleculesLength; i++) {
      final Molecule m1 = searchMolecules[i];
      final int[] count1 = results[i];
      final float x = m1.getX();
      final float y = m1.getY();

      for (final Molecule m2 : molecules) {
        if (distance2(x, y, m2) < r2) {
          count1[m2.getId()]++;
        }
      }
    }

    return results;
  }

  /**
   * Count the density of each class of molecule around each molecule. Counts are returned using the
   * original input order of molecules.
   *
   * <p>This method is optimised for use when the number of IDs is small. If the number of IDs is
   * large then the routine may run out of memory.
   *
   * @param molecules the molecules
   * @param radius the search radius distance
   * @param maxId the max ID of molecules
   * @return the counts
   */
  public static int[][] countAll(Molecule[] molecules, float radius, int maxId) {
    return countAllSimple(molecules, radius * radius, maxId);
  }

  /**
   * Count the density of each class of molecule around each input molecule. Counts are returned
   * using the original input order of molecules.
   *
   * <p>This method is optimised for use when the number of IDs is small. If the number of IDs is
   * large then the routine may run out of memory.
   *
   * @param molecules the molecules to create the density space
   * @param searchMolecules the molecules to around which to search
   * @param radius the search radius distance
   * @param maxId the max ID of molecules
   * @return the counts
   */
  public static int[][] countAll(Molecule[] molecules, Molecule[] searchMolecules, float radius,
      int maxId) {
    return countAllSimple(molecules, searchMolecules, radius * radius, maxId);
  }

  /**
   * Count the density of each class of molecule around each molecule. Counts are returned using the
   * original input order of molecules.
   *
   * <p>This method allows the molecule ID to be changed following creation of the counter but
   * coordinates must be the same. The maximum ID must be input to allow efficient counting.
   *
   * <p>This method is optimised for use when the number of IDs is small. If the number of IDs is
   * large then the routine may run out of memory.
   *
   * @param maxId the max ID of molecules
   * @return the counts
   * @throws ConcurrentRuntimeException If interrupted while computing using multi-thread mode
   */
  public int[][] countAll(int maxId) {
    final int[][] results = new int[moleculesCount][maxId + 1];

    // Single threaded
    if (getNumberOfThreads() == 1) {
      final int[] neighbours = new int[4];
      for (int i = 0; i < grid.length; i++) {
        final IndexMolecule[] cell1 = grid[i];
        if (cell1 == null) {
          continue;
        }

        final int neighbourCount = getNeighbours4(neighbours, i);

        for (int j = cell1.length; j-- > 0;) {
          final IndexMolecule m1 = cell1[j];
          final int[] count1 = results[m1.index];
          final float x = m1.getX();
          final float y = m1.getY();
          final int id = m1.getId();

          // Self count
          count1[id]++;

          // Compare all inside the bin
          for (int k = j; k-- > 0;) {
            final IndexMolecule m2 = cell1[k];
            if (distance2(x, y, m2) < r2) {
              final int[] count2 = results[m2.index];
              count1[m2.getId()]++;
              count2[id]++;
            }
          }

          // Compare to neighbours
          for (int c = neighbourCount; c-- > 0;) {
            final IndexMolecule[] cell2 = grid[neighbours[c]];
            for (int k = cell2.length; k-- > 0;) {
              final IndexMolecule m2 = cell2[k];
              if (distance2(x, y, m2) < r2) {
                final int[] count2 = results[m2.index];
                count1[m2.getId()]++;
                count2[id]++;
              }
            }
          }
        }
      }
    } else {
      // Multi-threaded

      createGridPriority();

      final int threadCount = Math.min(this.numberOfThreads, gridPriority.length);

      // Split the entries evenly over each thread
      // This should fairly allocate the density to all processing threads
      final int[] process = new int[nonEmpty];
      for (int i = 0, j = 0, k = 0; i < gridPriority.length; i++) {
        process[i] = gridPriority[j];
        j += threadCount;
        if (j >= gridPriority.length) {
          j = ++k;
        }
      }

      // Use an executor service so that we know when complete
      final ExecutorService executor = Executors.newFixedThreadPool(threadCount);
      final TurboList<Future<?>> futures = new TurboList<>(threadCount);

      final int nPerThread = (int) Math.ceil((double) process.length / threadCount);
      for (int from = 0; from < process.length;) {
        final int to = Math.min(from + nPerThread, process.length);
        if (multiThreadMode == MODE_NON_SYNC) {
          futures.add(executor.submit(new CountWorker2(results, process, from, to)));
        } else {
          futures.add(executor.submit(new CountWorker(results, process, from, to)));
        }
        from = to;
      }

      ConcurrencyUtils.waitForCompletionUnchecked(futures, DensityCounter::logException);

      executor.shutdown();
    }

    return results;
  }

  /**
   * Count the density of each class of molecule around each input molecule. Counts are returned
   * using the input order of molecules. The ID of the input molecules is ignored.
   *
   * <p>This method allows the molecule ID to be changed following creation of the counter but
   * coordinates must be the same. The maximum ID must be input to allow efficient counting.
   *
   * <p>This method is optimised for use when the number of IDs is small. If the number of IDs is
   * large then the routine may run out of memory.
   *
   * @param searchMolecules the molecules to around which to search
   * @param maxId the max ID of molecules
   * @return the counts
   * @throws ConcurrentRuntimeException If interrupted while computing using multi-thread mode
   */
  @SuppressWarnings("null")
  public int[][] countAll(Molecule[] searchMolecules, int maxId) {
    final int moleculesLength = (searchMolecules == null) ? 0 : searchMolecules.length;
    final int[][] results = new int[moleculesLength][];
    if (moleculesLength == 0) {
      return results;
    }

    final int threadCount = Math.min(getNumberOfThreads(), moleculesLength);
    // Single threaded
    if (threadCount == 1) {
      final int[] neighbours = new int[9];
      for (int i = 0; i < moleculesLength; i++) {
        results[i] = count(searchMolecules[i], maxId, neighbours);
      }
    } else {
      // Multi-threaded

      // Use an executor service so that we know when complete
      final ExecutorService executor = Executors.newFixedThreadPool(threadCount);
      final TurboList<Future<?>> futures = new TurboList<>(threadCount);

      final int nPerThread = (int) Math.ceil((double) moleculesLength / threadCount);
      for (int molecule = 0; molecule < moleculesLength;) {
        final int from = molecule;
        final int to = Math.min(from + nPerThread, moleculesLength);
        futures.add(executor.submit(() -> {
          final int[] neighbours = new int[9];
          for (int i = from; i < to; i++) {
            results[i] = count(searchMolecules[i], maxId, neighbours);
          }
        }));
        molecule = to;
      }

      ConcurrencyUtils.waitForCompletionUnchecked(futures, DensityCounter::logException);

      executor.shutdown();
    }

    return results;
  }

  /**
   * Log an exception.
   *
   * @param ex the exception
   */
  private static void logException(Exception ex) {
    Logger.getLogger(DensityCounter.class.getName()).log(Level.WARNING,
        () -> "Failed to perform computation: " + ex.getMessage());
  }

  private void createGridPriority() {
    if (gridPriority == null) {
      // Histogram the size of each cell.
      // This should not be a memory problem as no cell will be larger than moleculesLength.
      final int[] h = new int[maxCellSize + 1];
      for (final IndexMolecule[] cell1 : grid) {
        if (cell1 == null) {
          continue;
        }
        h[cell1.length]++;
      }

      // Allocate storage
      final IntFixedList[] indices = new IntFixedList[h.length];
      for (int i = h.length; i-- > 0;) {
        if (h[i] > 0) {
          indices[i] = new IntFixedList(h[i]);
        }
      }

      // Layout
      for (int i = 0; i < grid.length; i++) {
        final IndexMolecule[] cell1 = grid[i];
        if (cell1 == null) {
          continue;
        }
        indices[cell1.length].add(i);
      }

      // Record in reverse order (largest first)
      gridPriority = new int[nonEmpty];
      int target = 0;
      for (int i = indices.length; i-- > 0;) {
        if (indices[i] != null) {
          indices[i].copy(gridPriority, target);
          target += indices[i].size();
        }
      }
    }
  }

  /**
   * Adds the index to the list of neighbours if the grid contains molecules at the index.
   *
   * <p>This is used to build a list of neighbour cells (reference by their index) to process.
   *
   * @param neighbours the list of neighbours
   * @param count the current count of neighbours
   * @param index the index
   * @return the new count
   */
  int addNeighbour(int[] neighbours, int count, int index) {
    if (grid[index] != null) {
      neighbours[count] = index;
      return count + 1;
    }
    return count;
  }

  /**
   * For processing the countAll method using thread-safe writing to the results array with
   * synchronized.
   */
  private class CountWorker implements Runnable {
    final int[][] results;
    final int[] process;
    final int from;
    final int to;

    // For thread safety we create a stash of updates to the results which are then applied
    // in a synchronised method
    int indexCount;
    int[] indexData = new int[2000];
    int[] idData = new int[2000];

    void addSingle(int index, int id) {
      indexData[indexCount] = index;
      idData[indexCount] = id;
      if (++indexCount == indexData.length) {
        flushSingle();
      }
    }

    void flushSingle() {
      synchronized (results) {
        while (indexCount-- > 0) {
          results[indexData[indexCount]][idData[indexCount]]++;
        }
      }
      indexCount = 0;
    }

    CountWorker(int[][] results, int[] process, int from, int to) {
      this.results = results;
      this.process = process;
      this.from = from;
      this.to = to;
    }

    @Override
    public void run() {
      // Temp storage
      final int countSize = results[0].length;
      int totalLength = 0;
      for (int index = from; index < to; index++) {
        totalLength += grid[process[index]].length;
      }
      final int[][] results1 = new int[totalLength][countSize + 1];

      final int[] neighbours = new int[4];
      for (int index = from; index < to; index++) {
        final int processIndex = process[index];
        final IndexMolecule[] cell1 = grid[processIndex];

        final int neighbourCount = getNeighbours4(neighbours, processIndex);

        for (int j = cell1.length; j-- > 0;) {
          final IndexMolecule m1 = cell1[j];
          final int id = m1.getId();

          // Reset
          final int[] count1 = results1[--totalLength];

          // Self count
          count1[countSize] = m1.index;
          count1[id]++;

          // Compare all inside the bin
          final float x1 = m1.getX();
          final float y1 = m1.getY();
          for (int k = j; k-- > 0;) {
            final IndexMolecule m2 = cell1[k];
            if (distance2(x1, y1, m2) < r2) {
              count1[m2.getId()]++;
              addSingle(m2.index, id);
            }
          }

          // Compare to neighbours
          for (int c = neighbourCount; c-- > 0;) {
            final IndexMolecule[] cell2 = grid[neighbours[c]];
            for (int k = cell2.length; k-- > 0;) {
              final IndexMolecule m2 = cell2[k];
              if (distance2(x1, y1, m2) < r2) {
                count1[m2.getId()]++;
                addSingle(m2.index, id);
              }
            }
          }
        }
      }

      synchronized (results) {
        while (indexCount-- > 0) {
          results[indexData[indexCount]][idData[indexCount]]++;
        }

        for (final int[] count1 : results1) {
          // We store the index at the end of the array
          final int[] count = results[count1[countSize]];
          for (int j = 0; j < countSize; j++) {
            count[j] += count1[j];
          }
        }
      }
    }
  }

  /**
   * For processing the countAll method using an all neighbour cell comparison. This doubles the
   * number of distance comparisons but does not require synchronisation.
   */
  private class CountWorker2 implements Runnable {
    final int[][] results;
    final int[] process;
    final int from;
    final int to;

    CountWorker2(int[][] results, int[] process, int from, int to) {
      this.results = results;
      this.process = process;
      this.from = from;
      this.to = to;
    }

    @Override
    public void run() {
      final int[] neighbours = new int[8];
      for (int index = from; index < to; index++) {
        final int i = process[index];
        final IndexMolecule[] cell1 = grid[i];

        final int neighbourCount = getNeighbours8(neighbours, i);

        for (int j = cell1.length; j-- > 0;) {
          final IndexMolecule m1 = cell1[j];
          final int[] count1 = results[m1.index];
          final float x = m1.getX();
          final float y = m1.getY();

          // Compare all inside the bin. This will self-count
          for (int k = cell1.length; k-- > 0;) {
            final IndexMolecule m2 = cell1[k];
            if (distance2(x, y, m2) < r2) {
              count1[m2.getId()]++;
            }
          }

          // Compare to neighbours
          for (int c = neighbourCount; c-- > 0;) {
            final IndexMolecule[] cell2 = grid[neighbours[c]];
            for (int k = cell2.length; k-- > 0;) {
              final IndexMolecule m2 = cell2[k];
              if (distance2(x, y, m2) < r2) {
                count1[m2.getId()]++;
              }
            }
          }
        }
      }
    }
  }

  /**
   * Build a list of which cells to compare up to a maximum of 4.
   *
   * @param neighbours the neighbours
   * @param index the grid index
   * @return the number of neighbours
   */
  int getNeighbours4(int[] neighbours, int index) {
    // Build a list of which cells to compare up to a maximum of 4
    // @formatter:off
    //      | 0,0 | 1,0
    // ------------+-----
    // -1,1 | 0,1 | 1,1
    // @formatter:on
    int count = 0;
    final int xBin = index % nxbins;
    final int yBin = index / nxbins;
    if (yBin < nybins - 1) {
      if (xBin > 0) {
        count = addNeighbour(neighbours, count, index + nxbins - 1);
      }
      count = addNeighbour(neighbours, count, index + nxbins);
      if (xBin < nxbins - 1) {
        count = addNeighbour(neighbours, count, index + nxbins + 1);
        count = addNeighbour(neighbours, count, index + 1);
      }
    } else if (xBin < nxbins - 1) {
      count = addNeighbour(neighbours, count, index + 1);
    }
    return count;
  }

  /**
   * Build a list of which cells to compare up to a maximum of 8.
   *
   * @param neighbours the neighbours
   * @param index the grid index
   * @return the number of neighbours
   */
  int getNeighbours8(int[] neighbours, int index) {
    // Build a list of which cells to compare up to a maximum of 8
    // -1,-1 | 0,-1 | 1,-1
    // ------------+--------
    // -1, 0 | 0, 0 | 1, 0
    // ------------+--------
    // -1, 1 | 0, 1 | 1, 1
    int count = 0;
    final int xBin = index % nxbins;
    final int yBin = index / nxbins;
    final boolean lowerY = yBin > 0;
    final boolean upperY = yBin < nybins - 1;
    if (xBin > 0) {
      count = addNeighbour(neighbours, count, index - 1);
      if (lowerY) {
        count = addNeighbour(neighbours, count, index - 1 - nxbins);
      }
      if (upperY) {
        count = addNeighbour(neighbours, count, index - 1 + nxbins);
      }
    }
    if (lowerY) {
      count = addNeighbour(neighbours, count, index - nxbins);
    }
    if (upperY) {
      count = addNeighbour(neighbours, count, index + nxbins);
    }
    if (xBin < nxbins - 1) {
      count = addNeighbour(neighbours, count, index + 1);
      if (lowerY) {
        count = addNeighbour(neighbours, count, index + 1 - nxbins);
      }
      if (upperY) {
        count = addNeighbour(neighbours, count, index + 1 + nxbins);
      }
    }
    return count;
  }

  /**
   * Build a list of which cells to compare up to a maximum of 9.
   *
   * @param neighbours the neighbours
   * @param index the grid index
   * @return the number of neighbours
   */
  int getNeighbours9(int[] neighbours, int index) {
    // Build a list of which cells to compare up to a maximum of 9
    // -1,-1 | 0,-1 | 1,-1
    // ------------+--------
    // -1, 0 | 0, 0 | 1, 0
    // ------------+--------
    // -1, 1 | 0, 1 | 1, 1
    int count = addNeighbour(neighbours, 0, index);
    final int xBin = index % nxbins;
    final int yBin = index / nxbins;
    final boolean lowerY = yBin > 0;
    final boolean upperY = yBin < nybins - 1;
    if (xBin > 0) {
      count = addNeighbour(neighbours, count, index - 1);
      if (lowerY) {
        count = addNeighbour(neighbours, count, index - 1 - nxbins);
      }
      if (upperY) {
        count = addNeighbour(neighbours, count, index - 1 + nxbins);
      }
    }
    if (lowerY) {
      count = addNeighbour(neighbours, count, index - nxbins);
    }
    if (upperY) {
      count = addNeighbour(neighbours, count, index + nxbins);
    }
    if (xBin < nxbins - 1) {
      count = addNeighbour(neighbours, count, index + 1);
      if (lowerY) {
        count = addNeighbour(neighbours, count, index + 1 - nxbins);
      }
      if (upperY) {
        count = addNeighbour(neighbours, count, index + 1 + nxbins);
      }
    }
    return count;
  }

  /**
   * Get the squared distance.
   *
   * @param x the x
   * @param y the y
   * @param molecule the molecule
   * @return the squared distance
   */
  static final float distance2(float x, float y, Molecule molecule) {
    final float dx = x - molecule.getX();
    final float dy = y - molecule.getY();
    return dx * dx + dy * dy;
  }

  /**
   * Count the density of each class of molecule around the input molecule. Counts are returned
   * using the input order of molecules. The ID of the input molecules is ignored.
   *
   * <p>This method allows the molecule ID to be changed following creation of the counter but
   * coordinates must be the same. The maximum ID must be input to allow efficient counting.
   *
   * <p>This method is optimised for use when the number of IDs is small. If the number of IDs is
   * large then the routine may run out of memory.
   *
   * @param m1 the molecule
   * @param maxId the max ID of molecules
   * @return the counts
   */
  public int[] count(Molecule m1, int maxId) {
    return count(m1, maxId, new int[9]);
  }

  private int[] count(Molecule m1, int maxId, int[] neighbours) {
    final int[] count1 = new int[maxId + 1];

    final float x = m1.getX();
    final float y = m1.getY();
    int neighbourCount = getNeighbours9(neighbours, getBinSafe(x, y));

    // Compare to neighbours
    while (neighbourCount-- > 0) {
      final IndexMolecule[] cell2 = grid[neighbours[neighbourCount]];
      for (int k = cell2.length; k-- > 0;) {
        final IndexMolecule m2 = cell2[k];
        if (distance2(x, y, m2) < r2) {
          count1[m2.getId()]++;
        }
      }
    }
    return count1;
  }

  /**
   * Gets the number of threads to use for multi-threaded algorithms.
   *
   * @return the number of threads
   */
  public int getNumberOfThreads() {
    if (numberOfThreads == -1) {
      numberOfThreads = Runtime.getRuntime().availableProcessors();
    }
    return numberOfThreads;
  }

  /**
   * Sets the number of threads to use for multi-threaded algorithms.
   *
   * @param numberOfThreads the new number of threads
   */
  public void setNumberOfThreads(int numberOfThreads) {
    if (numberOfThreads > 0) {
      this.numberOfThreads = numberOfThreads;
    } else {
      this.numberOfThreads = 1;
    }
  }

  /**
   * Gets the radius.
   *
   * @return the radius
   */
  public float getRadius() {
    return radius;
  }

  /**
   * Gets the multi thread mode.
   *
   * @return the multi thread mode
   */
  byte getMultiThreadMode() {
    return multiThreadMode;
  }

  /**
   * Sets the multi thread mode.
   *
   * @param multiThreadMode the new multi thread mode
   */
  void setMultiThreadMode(byte multiThreadMode) {
    this.multiThreadMode = multiThreadMode;
  }
}
