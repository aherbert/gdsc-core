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
import java.util.EnumSet;

import org.apache.commons.math3.random.RandomDataGenerator;

import ags.utils.dataStructures.trees.secondGenKD.SimpleFloatKdTree2D;
import gdsc.core.clustering.CoordinateStore;
import gdsc.core.ij.Utils;
import gdsc.core.logging.TrackProgress;
import gdsc.core.utils.Maths;

/**
 * Compute clustering using OPTICS.
 * <p>
 * This is an implementation of the OPTICS method. Mihael Ankerst, Markus M Breunig, Hans-Peter Kriegel, and Jorg
 * Sander. Optics: ordering points to identify the clustering structure. In ACM Sigmod Record, volume 28, pages
 * 49–60. ACM, 1999.
 */
public class OPTICSManager extends CoordinateStore
{
	/**
	 * The UNDEFINED distance in the OPTICS algorithm. This is actually arbitrary. Use a simple value so we can spot it
	 * when debugging.
	 */
	static final float UNDEFINED = -1;

	/**
	 * Options for the algorithms
	 */
	public enum Option
	{
		/**
		 * Flag to indicate that memory structures should be cached. Set this when it is intended to call multiple
		 * clustering methods.
		 */
		CACHE,
		/**
		 * Flag to indicate that a circular mask should be used on the 2D grid. This has performance
		 * benefits since some distance computations can be avoided.
		 * <p>
		 * Note: This option is experimental and can cause slow-down
		 */
		CIRCULAR_PROCESSING,
		/**
		 * Flag to indicate that inner-circle processing should be used on the 2D grid. This has performance
		 * benefits for DBSCAN since some distance computations can be assumed.
		 * <p>
		 * Note: This option is experimental and can cause slow-down
		 */
		INNER_PROCESSING,
		/**
		 * Flag to indicate that processing should use a 2D grid.
		 * <p>
		 * Note: If no options are provided then the memory structure will be chosen automatically.
		 */
		GRID_PROCESSING,
		/**
		 * Flag to indicate that OPTICS should sort objects using their input order (id) if the reachability distance is
		 * equal.
		 * <p>
		 * Omitting this flag defaults to sorting objects using their reachability distance. If the reachability
		 * distance is the same then the order will be dependent on the type of priority queue, i.e. is
		 * non-deterministic.
		 */
		OPTICS_STRICT_ID_ORDER,
		/**
		 * Flag to indicate that OPTICS should sort objects using their reverse input order (id) if the reachability
		 * distance is equal.
		 * <p>
		 * Omitting this flag defaults to sorting objects using their reachability distance. If the reachability
		 * distance is the same then the order will be dependent on the type of priority queue, i.e. is
		 * non-deterministic.
		 * <p>
		 * Note: This option matches the implementation in the ELKI framework.
		 */
		OPTICS_STRICT_REVERSE_ID_ORDER,
		/**
		 * Flag to indicate that OPTICS should sort objects using a simple priority queue. The default is a binary heap.
		 */
		OPTICS_SIMPLE_PRIORITY_QUEUE;
	}

	EnumSet<Option> options = EnumSet.noneOf(Option.class);

	/**
	 * Sets the options.
	 *
	 * @param options
	 *            the new options
	 */
	public void setOptions(Option... options)
	{
		if (options == null)
			return;
		for (Option option : options)
			this.options.add(option);
	}

	/**
	 * Sets the options.
	 *
	 * @param options
	 *            the new options
	 */
	public void setOptions(EnumSet<Option> options)
	{
		if (options == null)
			throw new IllegalArgumentException("Options cannot be null");
		this.options = options;
	}

	/**
	 * Gets the options.
	 *
	 * @return the options
	 */
	public EnumSet<Option> getOptions()
	{
		return options;
	}

	/**
	 * Used in the DBSCAN algorithm to store a queue of molecules to process
	 */
	private class MoleculeQueue extends MoleculeArray
	{
		int next = 0;

		MoleculeQueue(int capacity)
		{
			super(capacity);
		}

		void push(Molecule m)
		{
			add(m);
		}

		void clear()
		{
			size = next = 0;
		}

		boolean hasNext()
		{
			return next < size;
		}

		public Molecule next()
		{
			return list[next++];
		}
	}

	/**
	 * Counter used in DBSCAN
	 */
	private class Counter
	{
		int next, progress, total;

		Counter(int total)
		{
			this.total = total;
		}

		int nextClusterId()
		{
			return ++next;
		}

		/**
		 * Increment the counter and check if processing should stop.
		 *
		 * @return true, if processing should stop
		 */
		boolean increment()
		{
			if (tracker != null)
			{
				tracker.progress(++progress, total);
				return tracker.isEnded();
			}
			return false;
		}

		int getTotalClusters()
		{
			return next;
		}
	}

	/**
	 * Interface for the OPTICS priority queue. Molecules should be ordered by their reachability distance.
	 */
	private interface OPTICSPriorityQueue
	{
		/**
		 * Push the molecule to the queue and move up.
		 *
		 * @param m
		 *            the molecule
		 */
		public void push(Molecule m);

		/**
		 * Move the molecule up the queue (since the reachability distance has changed).
		 *
		 * @param m
		 *            the molecule
		 */
		public void moveUp(Molecule m);

		/**
		 * Checks for next.
		 *
		 * @return true, if successful
		 */
		public boolean hasNext();

		/**
		 * Get the next molecule.
		 *
		 * @return the molecule
		 */
		public Molecule next();

		/**
		 * Clear the queue.
		 */
		public void clear();
	}

	/**
	 * Used in the OPTICS algorithm to store the next seed is a priority queue
	 */
	class OPTICSMoleculePriorityQueue extends MoleculeArray implements OPTICSPriorityQueue
	{
		int next = 0;

		OPTICSMoleculePriorityQueue(int capacity)
		{
			super(capacity);
		}

		public void push(Molecule m)
		{
			set(m, size++);
			moveUp(m);
		}

		void set(Molecule m, int index)
		{
			list[index] = m;
			m.setQueueIndex(index);
		}

		public void moveUp(Molecule object)
		{
			if (lower(object, list[next]))
				swap(next, object.getQueueIndex());
		}

		public boolean hasNext()
		{
			return next < size;
		}

		public Molecule next()
		{
			Molecule m = list[next++];
			if (hasNext())
			{
				// Find the next lowest molecule
				int lowest = next;
				for (int i = next + 1; i < size; i++)
				{
					if (lower(list[i], list[lowest]))
						lowest = i;
				}
				swap(next, lowest);
			}
			return m;
		}

		void swap(int i, int j)
		{
			Molecule m = list[i];
			set(list[j], i);
			set(m, j);
		}

		public void clear()
		{
			size = next = 0;
		}

		boolean lower(Molecule m1, Molecule m2)
		{
			return m1.reachabilityDistance < m2.reachabilityDistance;
		}
	}

	/**
	 * Used in the OPTICS algorithm to store the next seed is a priority queue
	 * <p>
	 * If distances are equal then IDs are used to sort the objects in order.
	 */
	class OPTICSMoleculePriorityQueueIdOrdered extends OPTICSMoleculePriorityQueue
	{
		OPTICSMoleculePriorityQueueIdOrdered(int capacity)
		{
			super(capacity);
		}

		@Override
		boolean lower(Molecule m1, Molecule m2)
		{
			if (m1.reachabilityDistance < m2.reachabilityDistance)
				return true;
			if (m1.reachabilityDistance > m2.reachabilityDistance)
				return false;
			return m1.id < m2.id;
		}
	}

	/**
	 * Used in the OPTICS algorithm to store the next seed is a priority queue
	 * <p>
	 * If distances are equal then IDs are used to sort the objects in reverse order.
	 */
	class OPTICSMoleculePriorityQueueReverseIdOrdered extends OPTICSMoleculePriorityQueue
	{
		OPTICSMoleculePriorityQueueReverseIdOrdered(int capacity)
		{
			super(capacity);
		}

		@Override
		boolean lower(Molecule m1, Molecule m2)
		{
			if (m1.reachabilityDistance < m2.reachabilityDistance)
				return true;
			if (m1.reachabilityDistance > m2.reachabilityDistance)
				return false;
			return m1.id > m2.id;
		}
	}

	/**
	 * An implementation of a binary heap respecting minimum order.
	 * <p>
	 * This class is based on ags.utils.dataStructures.BinaryHeap
	 */
	class OPTICSMoleculeBinaryHeap extends MoleculeArray implements OPTICSPriorityQueue
	{
		OPTICSMoleculeBinaryHeap(int capacity)
		{
			super(capacity);
		}

		public void push(Molecule m)
		{
			set(m, size++);
			moveUp(m);
		}

		void set(Molecule m, int index)
		{
			list[index] = m;
			m.setQueueIndex(index);
		}

		public boolean hasNext()
		{
			return size != 0;
		}

		public Molecule next()
		{
			Molecule m = list[0];
			set(list[--size], 0);
			siftDown(0);
			return m;
		}

		public void moveUp(Molecule object)
		{
			siftUp(object.getQueueIndex());
		}

		void siftUp(int c)
		{
			// Original
			//			for (int p = (c - 1) / 2; c != 0 && lower(list[c], list[p]); c = p, p = (c - 1) / 2)
			//			{
			//				swap(p, c);
			//			}
			
			// Remove unnecessary loop set-up statements, i.e. where p is not needed
			while (c > 0)
			{
				final int p = (c - 1) / 2;
				if (lower(list[c], list[p]))
				{
					swap(p, c);
					c = p;
				}
				else
				{
					break;
				}
			}
		}

		void swap(int i, int j)
		{
			Molecule m = list[i];
			set(list[j], i);
			set(m, j);
		}

		void siftDown(int p)
		{
			for (int c = p * 2 + 1; c < size; p = c, c = p * 2 + 1)
			{
				if (c + 1 < size && higher(list[c], list[c + 1]))
				{
					c++;
				}
				if (higher(list[p], list[c]))
				{
					swap(p, c);
				}
				else
				{
					break;
				}
			}
		}

		public void clear()
		{
			super.clear();
		}

		boolean lower(Molecule m1, Molecule m2)
		{
			return m1.reachabilityDistance < m2.reachabilityDistance;
		}

		boolean higher(Molecule m1, Molecule m2)
		{
			return m1.reachabilityDistance > m2.reachabilityDistance;
		}
	}

	/**
	 * An implementation of a binary heap respecting minimum order.
	 * <p>
	 * If distances are equal then IDs are used to sort the objects in order.
	 */
	class OPTICSMoleculeBinaryHeapIdOrdered extends OPTICSMoleculeBinaryHeap
	{
		OPTICSMoleculeBinaryHeapIdOrdered(int capacity)
		{
			super(capacity);
		}

		@Override
		boolean lower(Molecule m1, Molecule m2)
		{
			if (m1.reachabilityDistance < m2.reachabilityDistance)
				return true;
			if (m1.reachabilityDistance > m2.reachabilityDistance)
				return false;
			return m1.id < m2.id;
		}

		@Override
		boolean higher(Molecule m1, Molecule m2)
		{
			if (m1.reachabilityDistance > m2.reachabilityDistance)
				return true;
			if (m1.reachabilityDistance < m2.reachabilityDistance)
				return false;
			return m1.id > m2.id;
		}
	}

	/**
	 * An implementation of a binary heap respecting minimum order.
	 * <p>
	 * If distances are equal then IDs are used to sort the objects in reverse order.
	 */
	class OPTICSMoleculeBinaryHeapReverseIdOrdered extends OPTICSMoleculeBinaryHeap
	{
		OPTICSMoleculeBinaryHeapReverseIdOrdered(int capacity)
		{
			super(capacity);
		}

		@Override
		boolean lower(Molecule m1, Molecule m2)
		{
			if (m1.reachabilityDistance < m2.reachabilityDistance)
				return true;
			if (m1.reachabilityDistance > m2.reachabilityDistance)
				return false;
			return m1.id > m2.id;
		}

		@Override
		boolean higher(Molecule m1, Molecule m2)
		{
			if (m1.reachabilityDistance > m2.reachabilityDistance)
				return true;
			if (m1.reachabilityDistance < m2.reachabilityDistance)
				return false;
			return m1.id < m2.id;
		}
	}

	/**
	 * Used in the OPTICS algorithm to store the output results
	 */
	private class OPTICSResultList
	{
		final OPTICSOrder[] list;
		int size = 0;

		OPTICSResultList(int capacity)
		{
			list = new OPTICSOrder[capacity];
		}

		/**
		 * Adds the molecule to the results. Send progress to the tracker and checks for a shutdown signal.
		 *
		 * @param m
		 *            the m
		 * @param clusterId
		 *            the cluster id
		 * @return true, if a shutdown signal has been received
		 */
		boolean add(Molecule m)
		{
			list[size++] = m.toOPTICSResult();
			if (tracker != null)
			{
				tracker.progress(size, list.length);
				return tracker.isEnded();
			}
			return false;
		}
	}

	/**
	 * An optimised heap structure for selecting the top n values.
	 */
	private static class FloatHeap
	{
		/**
		 * The number N to select
		 */
		final int n;
		/**
		 * Working storage
		 */
		private final float[] queue;

		/**
		 * Instantiates a new heap.
		 *
		 * @param n
		 *            the number to select
		 */
		private FloatHeap(int n)
		{
			if (n < 1)
				throw new IllegalArgumentException("N must be strictly positive");
			this.queue = new float[n];
			this.n = n;
		}

		/**
		 * Add the first value to the heap.
		 *
		 * @param value
		 *            the value
		 */
		private void start(float value)
		{
			queue[0] = value;
		}

		/**
		 * Put the next value into the heap. This method is used to fill the heap from i=1 to i<n.
		 *
		 * @param i
		 *            the index
		 * @param value
		 *            the value
		 */
		private void put(int i, float value)
		{
			queue[i] = value;
			upHeapify(i);
		}

		/**
		 * Push a value onto a full heap. This method is used to add more values to a full heap.
		 *
		 * @param value
		 *            the value
		 */
		private void push(float value)
		{
			if (queue[0] > value)
			{
				queue[0] = value;
				downHeapify(0);
			}
		}

		private float getMaxValue()
		{
			return queue[0];
		}

		private void upHeapify(int c)
		{
			for (int p = (c - 1) / 2; c != 0 && queue[c] > queue[p]; c = p, p = (c - 1) / 2)
			{
				float pDist = queue[p];
				queue[p] = queue[c];
				queue[c] = pDist;
			}
		}

		private void downHeapify(int p)
		{
			for (int c = p * 2 + 1; c < n; p = c, c = p * 2 + 1)
			{
				if (c + 1 < n && queue[c] < queue[c + 1])
				{
					c++;
				}
				if (queue[p] < queue[c])
				{
					// Swap the points
					float pDist = queue[p];
					queue[p] = queue[c];
					queue[c] = pDist;
				}
				else
				{
					break;
				}
			}
		}
	}

	/**
	 * Input arrays are modified
	 * 
	 * @param xcoord
	 * @param ycoord
	 * @param bounds
	 * @throws IllegalArgumentException
	 *             if results are null or empty
	 */
	public OPTICSManager(float[] xcoord, float[] ycoord, Rectangle bounds)
	{
		super(xcoord, ycoord, bounds);
	}

	/**
	 * Compute the core radius for each point to have n closest neighbours and the minimum reachability distance of a
	 * point from another core point.
	 * <p>
	 * This is an implementation of the OPTICS method. Mihael Ankerst, Markus M Breunig, Hans-Peter Kriegel, and Jorg
	 * Sander. Optics: ordering points to identify the clustering structure. In ACM Sigmod Record, volume 28, pages
	 * 49–60. ACM, 1999.
	 * <p>
	 * The returned results are the output of {@link #extractDBSCANClustering(OPTICSOrder[], float)} with the
	 * configured generating distance. Note that the generating distance may have been modified if invalid. If it is
	 * not strictly positive or not finite then it is set using {@link #computeGeneratingDistance(int)}. If it is larger
	 * than the data range allows it is set to the maximum distance that can be computed for the data range. If the data
	 * are colocated the distance is set to 1. The distance is stored in the results.
	 * <p>
	 * This creates a large memory structure. It can be held in memory for re-use when using a different number of min
	 * points. The tracker can be used to follow progress (see {@link #setTracker(TrackProgress)}).
	 *
	 * @param generatingDistanceE
	 *            the generating distance E (set to zero to auto calibrate)
	 * @param minPts
	 *            the min points for a core object (recommended range 10-20)
	 * @return the results (or null if the algorithm was stopped using the tracker)
	 */
	public OPTICSResult optics(float generatingDistanceE, int minPts)
	{
		if (minPts < 1)
			minPts = 1;

		long time = System.currentTimeMillis();
		initialiseOPTICS(generatingDistanceE, minPts);

		// The distance may be updated
		generatingDistanceE = grid.generatingDistanceE;

		if (tracker != null)
		{
			tracker.log("Running OPTICS ... Distance=%g, minPts=%d", generatingDistanceE, minPts);
			tracker.progress(0, xcoord.length);
		}

		// Note: The method and variable names used in this function are designed to match 
		// the pseudocode implementation from the 1999 OPTICS paper.
		// The generating distance (E) used in the paper is the maximum distance at which cluster
		// centres will be formed. This implementation uses the squared distance to avoid sqrt() 
		// function calls.
		final float e = generatingDistanceE * generatingDistanceE;

		final int size = xcoord.length;
		Molecule[] setOfObjects = grid.setOfObjects;

		// Allow different queue implementations.
		// Note:
		// The ELKI code de.lmu.ifi.dbs.elki.algorithm.clustering.optics.OPTICSHeapEntry
		// Returns the opposite of an id comparison:
		// return -DBIDUtil.compare(objectID, o.objectID);
		// I do not know why this is but I have added it so the functionality 
		// is identical in order to pass the JUnit tests		
		OPTICSPriorityQueue orderSeeds;
		if (options.contains(Option.OPTICS_SIMPLE_PRIORITY_QUEUE))
		{
			if (options.contains(Option.OPTICS_STRICT_ID_ORDER))
				orderSeeds = new OPTICSMoleculePriorityQueueIdOrdered(size);
			else if (options.contains(Option.OPTICS_STRICT_REVERSE_ID_ORDER))
				orderSeeds = new OPTICSMoleculePriorityQueueReverseIdOrdered(size);
			else
				orderSeeds = new OPTICSMoleculePriorityQueue(size);
		}
		else
		{
			if (options.contains(Option.OPTICS_STRICT_ID_ORDER))
				orderSeeds = new OPTICSMoleculeBinaryHeapIdOrdered(size);
			else if (options.contains(Option.OPTICS_STRICT_REVERSE_ID_ORDER))
				orderSeeds = new OPTICSMoleculeBinaryHeapReverseIdOrdered(size);
			else
				orderSeeds = new OPTICSMoleculeBinaryHeap(size);
		}

		// Testing ... Should we use the ID order by default? Nothing in the OPTICS paper mentions it.
		// Perhaps the reachability distance profile is more consistent when changing the generating distance?
		//orderSeeds = new OPTICSMoleculeBinaryHeap(size);
		//orderSeeds = new OPTICSMoleculePriorityQueue(size);
		//orderSeeds = new OPTICSMoleculeBinaryHeapIdOrdered(size);
		//orderSeeds = new OPTICSMoleculePriorityQueueIdOrdered(size);
		//orderSeeds = new OPTICSMoleculeBinaryHeapReverseIdOrdered(size);

		OPTICSResultList results = new OPTICSResultList(size);

		for (int i = 0; i < size; i++)
		{
			final Molecule object = setOfObjects[i];
			if (object.isNotProcessed())
			{
				if (expandClusterOrder(object, e, minPts, results, orderSeeds))
					break;
			}
		}

		boolean stopped = false;
		if (tracker != null)
		{
			stopped = tracker.isEnded();
			tracker.progress(1.0);

			if (stopped)
				tracker.log("Aborted OPTICS");
		}

		OPTICSResult optics = null;
		if (!stopped)
		{
			optics = new OPTICSResult(this, minPts, generatingDistanceE, results.list);
			final int nClusters = optics.extractDBSCANClustering(generatingDistanceE);
			if (tracker != null)
			{
				time = System.currentTimeMillis() - time;
				tracker.log("Finished OPTICS: %s (Time = %s)", Utils.pleural(nClusters, "Cluster"),
						Utils.timeToString(time));
			}
		}

		finish();

		return optics;
	}

	private MoleculeSpace grid;
	//private float[] floatArray;
	private FloatHeap heap;

	/**
	 * Initialise the memory structure for the OPTICS algorithm. This can be cached if the generatingDistanceE does not
	 * change.
	 *
	 * @param generatingDistanceE
	 *            the generating distance E
	 * @param minPts
	 *            the min points for a core object
	 */
	private void initialiseOPTICS(float generatingDistanceE, int minPts)
	{
		// TODO - See if OPTICS can be made faster with a specialised MoleculeSpace.
		// For now this is disabled.
		//Class<?> clazz = getPreferredMoleculeSpace(true);

		Class<?> clazz = getPreferredMoleculeSpace(false);
		if (clazz != null)
		{
			// Ensure the distance is valid
			generatingDistanceE = getWorkingGeneratingDistance(generatingDistanceE, minPts);

			// OPTICS will benefit from circular processing if the density is high.
			// This is because we can skip distance computation to molecules outside the circle.
			// This is roughly pi/4 
			// Compute the expected number of molecules in the area.

			final float xrange = maxXCoord - minXCoord;
			final float yrange = maxYCoord - minYCoord;
			double area;
			if (xrange == 0 && yrange == 0)
			{
				// Occurs when only 1 point or colocated data. A distance of zero is invalid so set to 1.
				area = 1;
			}
			else
			{
				area = xrange * yrange;
			}

			double nMoleculesInPixel = (double) getSize() / area;
			double nMoleculesInCircle = Math.PI * generatingDistanceE * generatingDistanceE * nMoleculesInPixel;

			// TODO - JUnit test to show when to use a circle to avoid distance comparisons. 
			// We can miss 1 - pi/4 = 21% of the area.  
			if (nMoleculesInCircle > RadialMoleculeSpace.N_MOLECULES_FOR_NEXT_RESOLUTION_OUTER)
				clazz = RadialMoleculeSpace.class;
		}
		initialise(generatingDistanceE, minPts, clazz);
	}

	private void initialiseDBSCAN(float generatingDistanceE, int minPts)
	{
		Class<?> clazz = getPreferredMoleculeSpace(true);
		if (clazz != null)
		{
			// Ensure the distance is valid
			generatingDistanceE = getWorkingGeneratingDistance(generatingDistanceE, minPts);

			// DBSCAN will benefit from inner radial processing if the number of comparisons is high.
			// Compute the expected number of molecules in the area.

			final float xrange = maxXCoord - minXCoord;
			final float yrange = maxYCoord - minYCoord;
			double area;
			if (xrange == 0 && yrange == 0)
			{
				// Occurs when only 1 point or colocated data. A distance of zero is invalid so set to 1.
				area = 1;
			}
			else
			{
				area = xrange * yrange;
			}

			double nMoleculesInPixel = (double) getSize() / area;
			double nMoleculesInCircle = Math.PI * generatingDistanceE * generatingDistanceE * nMoleculesInPixel;

			if (nMoleculesInCircle > RadialMoleculeSpace.N_MOLECULES_FOR_NEXT_RESOLUTION_INNER)
				clazz = InnerRadialMoleculeSpace.class;
		}
		initialise(generatingDistanceE, minPts, clazz);
	}

	/**
	 * Returned the preferred class for the molecule space using the options.
	 * 
	 * @param allowNull
	 *            Set to true to return null if no options are set
	 * @return the preferred class for the molecule space
	 */
	private Class<?> getPreferredMoleculeSpace(boolean allowNull)
	{
		if (options.contains(Option.CIRCULAR_PROCESSING))
		{
			if (options.contains(Option.INNER_PROCESSING))
				return InnerRadialMoleculeSpace.class;
			return RadialMoleculeSpace.class;
		}
		if (options.contains(Option.GRID_PROCESSING))
			return GridMoleculeSpace.class;
		return (allowNull) ? null : GridMoleculeSpace.class;
	}

	/**
	 * Initialise the memory structure for the OPTICS algorithm. This can be cached if the generatingDistanceE does not
	 * change.
	 *
	 * @param generatingDistanceE
	 *            the generating distance E
	 * @param minPts
	 *            the min points for a core object
	 * @param clazz
	 *            the preferred class for the molecule space
	 */
	private void initialise(float generatingDistanceE, int minPts, Class<?> clazz)
	{
		// Ensure the distance is valid
		generatingDistanceE = getWorkingGeneratingDistance(generatingDistanceE, minPts);

		if (clazz == null)
			clazz = getPreferredMoleculeSpace(false);

		// Compare to the existing grid
		if (grid == null || grid.generatingDistanceE != generatingDistanceE || grid.getClass() != clazz)
		{
			if (tracker != null)
				tracker.log("Initialising ...");

			// Control the type of space we use to store the data
			if (clazz == InnerRadialMoleculeSpace.class)
				grid = new InnerRadialMoleculeSpace(this, generatingDistanceE);
			if (clazz == RadialMoleculeSpace.class)
				grid = new RadialMoleculeSpace(this, generatingDistanceE);
			else
				grid = new GridMoleculeSpace(this, generatingDistanceE);

			grid.generate();
		}
		else
		{
			// This is the same distance so the objects can be reused
			grid.reset();
		}

		//if (floatArray == null || floatArray.length < minPts)
		//	floatArray = new float[minPts];

		if (heap == null || heap.n != minPts)
			heap = new FloatHeap(minPts);
	}

	/**
	 * Gets the working generating distance. Ensure the generating distance is not too high for the data range. Also set
	 * it to the max value if the generating distance is not valid.
	 *
	 * @param generatingDistanceE
	 *            the generating distance E
	 * @param minPts
	 *            the min points for a core object
	 * @return the working generating distance
	 */
	private float getWorkingGeneratingDistance(float generatingDistanceE, int minPts)
	{
		final float xrange = maxXCoord - minXCoord;
		final float yrange = maxYCoord - minYCoord;
		if (xrange == 0 && yrange == 0)
			// Occurs when only 1 point or colocated data. A distance of zero is invalid so set to 1.
			return 1;

		// If not set then compute the generating distance
		if (!Maths.isFinite(generatingDistanceE) || generatingDistanceE <= 0)
			return computeGeneratingDistance(minPts);

		// Compute the upper distance we can expect
		double maxDistance = Math.sqrt(xrange * xrange + yrange * yrange);
		if (generatingDistanceE > maxDistance)
			return (float) maxDistance;

		// Stick to the user input
		return generatingDistanceE;
	}

	/**
	 * Checks for search algorithm structures stored in memory.
	 *
	 * @return true, if successful
	 */
	public boolean hasMemory()
	{
		return grid != null;
	}

	/**
	 * Clear memory used by the search algorithm
	 */
	private void finish()
	{
		if (!options.contains(Option.CACHE))
		{
			clearMemory();
		}
		heap = null;
	}

	/**
	 * Clear memory used by the search algorithm
	 */
	public void clearMemory()
	{
		grid = null;
		//floatArray = null;
	}

	/**
	 * Expand cluster order.
	 *
	 * @param object
	 *            the object
	 * @param e
	 *            the generating distance (squared)
	 * @param minPts
	 *            the min points for a core object
	 * @param orderedFile
	 *            the results
	 * @param orderSeeds
	 *            the order seeds
	 * @return true, if the algorithm has received a shutdown signal
	 */
	private boolean expandClusterOrder(Molecule object, float e, int minPts, OPTICSResultList orderedFile,
			OPTICSPriorityQueue orderSeeds)
	{
		grid.findNeighboursAndDistances(minPts, object, e);
		object.markProcessed();
		setCoreDistance(object, minPts, grid.neighbours);
		if (orderedFile.add(object))
			return true;

		if (object.coreDistance != UNDEFINED)
		{
			// Create seed-list for further expansion.
			fill(orderSeeds, grid.neighbours, object);

			while (orderSeeds.hasNext())
			{
				object = orderSeeds.next();
				grid.findNeighboursAndDistances(minPts, object, e);
				if (!object.isNotProcessed())
					System.out.printf("Choose a processed point ?\n");
				object.markProcessed();
				setCoreDistance(object, minPts, grid.neighbours);
				if (orderedFile.add(object))
					return true;

				if (object.coreDistance != UNDEFINED)
					update(orderSeeds, grid.neighbours, object);
			}
		}
		return false;
	}

	/**
	 * Set the core distance.
	 *
	 * @param object
	 *            the object
	 * @param minPts
	 *            the min points to be a core point
	 * @param neighbours
	 *            the neighbours
	 */
	public void setCoreDistance(Molecule object, int minPts, MoleculeList neighbours)
	{
		final int size = neighbours.size;
		if (size < minPts)
			// Not a core point
			return;

		final Molecule[] list = neighbours.list;

		// Special case where we find the max value
		if (size == minPts)
		{
			float max = list[0].d;
			for (int i = 1; i < size; i++)
			{
				if (max < list[i].d)
					max = list[i].d;
			}
			object.coreDistance = max;
			return;
		}

		// Use a heap structure. This should out perform a pointer to the max value when 
		// minPts is much lower than the number of neighbours. When it is similar then 
		// the speed is fast no matter what method is used since minPts is expected to be low
		// (somewhere around 5 for 2D data).

		heap.start(list[0].d);
		int i = 1;
		while (i < minPts)
		{
			heap.put(i, list[i++].d);
		}
		// Scan
		while (i < size)
		{
			heap.push(list[i++].d);
		}

		object.coreDistance = heap.getMaxValue();

		//		// DEBUGGING : Used for Speed timing
		//		for (int ii = 100; ii-- > 0;)
		//		{
		//			final boolean useHeap = true;
		//			if (useHeap)
		//			{
		//				// Fill 
		//				heap.start(list[0].d);
		//				i = 1;
		//				while (i < minPts)
		//				{
		//					heap.put(i, list[i++].d);
		//				}
		//				// Scan
		//				while (i < size)
		//				{
		//					heap.push(list[i++].d);
		//				}
		//				
		//				object.coreDistance = heap.getMaxValue();
		//			}
		//			else
		//			{
		//				// Avoid a full sort using a priority queue structure.
		//				// We retain a pointer to the current highest value in the set. 
		//				int max = 0;
		//				floatArray[0] = list[0].d;
		//
		//				// Fill 
		//				i = 1;
		//				while (i < minPts)
		//				{
		//					floatArray[i] = list[i].d;
		//					if (floatArray[max] < floatArray[i])
		//						max = i;
		//					i++;
		//				}
		//
		//				// Scan
		//				while (i < size)
		//				{
		//					// Replace if lower
		//					if (floatArray[max] > list[i].d)
		//					{
		//						floatArray[max] = list[i].d;
		//						// Find new max
		//						for (int j = minPts; j-- > 0;)
		//						{
		//							if (floatArray[max] < floatArray[j])
		//								max = j;
		//						}
		//					}
		//					i++;
		//				}
		//
		//				object.coreDistance = floatArray[max];
		//			}
		//		}
	}

	/**
	 * Clear the seeds and fill with the unprocessed neighbours of the current object. Set the reachability distance and
	 * reorder.
	 *
	 * @param orderSeeds
	 *            the order seeds
	 * @param neighbours
	 *            the neighbours
	 * @param centreObject
	 *            the object
	 */
	private void fill(OPTICSPriorityQueue orderSeeds, MoleculeList neighbours, Molecule centreObject)
	{
		orderSeeds.clear();

		final float c_dist = centreObject.coreDistance;
		for (int i = neighbours.size; i-- > 0;)
		{
			final Molecule object = neighbours.get(i);
			if (object.isNotProcessed())
			{
				// This is new so add it to the list
				object.reachabilityDistance = max(c_dist, object.d);
				object.predecessor = centreObject.id;
				orderSeeds.push(object);
			}
		}
	}

	/**
	 * Update the ordered seeds with the neighbours of the current object. Set the reachability distance and reorder.
	 *
	 * @param orderSeeds
	 *            the order seeds
	 * @param neighbours
	 *            the neighbours
	 * @param centreObject
	 *            the object
	 */
	private void update(OPTICSPriorityQueue orderSeeds, MoleculeList neighbours, Molecule centreObject)
	{
		final float c_dist = centreObject.coreDistance;
		for (int i = neighbours.size; i-- > 0;)
		{
			final Molecule object = neighbours.get(i);
			if (object.isNotProcessed())
			{
				final float new_r_dist = max(c_dist, object.d);
				if (object.reachabilityDistance == UNDEFINED)
				{
					// This is new so add it to the list
					object.reachabilityDistance = new_r_dist;
					object.predecessor = centreObject.id;
					orderSeeds.push(object);
				}
				else
				{
					// This is already in the list
					// Here is the difference between OPTICS and DBSCAN.
					// In this case the order of points to process can be changed based on the reachability.
					if (new_r_dist < object.reachabilityDistance)
					{
						object.reachabilityDistance = new_r_dist;
						object.predecessor = centreObject.id;
						orderSeeds.moveUp(object);
					}
				}
			}
		}
	}

	/**
	 * Find the max fast (ignore the possibility of NaN or infinity
	 *
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @return the max
	 */
	public static float max(float a, float b)
	{
		return (a >= b) ? a : b;
	}

	/**
	 * Compute the OPTICS generating distance assuming a uniform distribution in the data space.
	 *
	 * @param minPts
	 *            the min points for a core object
	 * @return the generating distance
	 */
	public float computeGeneratingDistance(int minPts)
	{
		return computeGeneratingDistance(minPts, area, xcoord.length);
	}

	/**
	 * Compute the OPTICS generating distance assuming a uniform distribution in the data space.
	 *
	 * @param minPts
	 *            the min points for a core object
	 * @param area
	 *            the area of the data space
	 * @param N
	 *            the number of points in the data space
	 * @return the generating distance
	 */
	public static float computeGeneratingDistance(int minPts, double area, int N)
	{
		// Taken from section 4.1 of the OPTICS paper.

		// Number of dimensions
		// d = 2;
		// Volume of the data space (DS)
		double volumeDS = area;
		// Expected k-nearest-neighbours
		int k = minPts;

		// Compute the volume of the hypersphere required to contain k neighbours,
		// assuming a uniform spread.
		// VolumeS = (VolumeDS/N) x k

		double volumeS = (volumeDS / N) * k;

		// Note Volume S(r) for a 2D hypersphere = pi * r^2
		return (float) Math.sqrt(volumeS / Math.PI);
	}

	/**
	 * Compute the core points as any that have more than min points within the distance. All points within the radius
	 * are reachable points that are processed in turn. Any new core points expand the search space.
	 * <p>
	 * This is an implementation of the DBSCAN method. Ester, Martin; Kriegel, Hans-Peter; Sander, Jörg; Xu, Xiaowei
	 * (1996). Simoudis, Evangelos; Han, Jiawei; Fayyad, Usama M., eds. A density-based algorithm for discovering
	 * clusters in large spatial databases with noise. Proceedings of the Second International Conference on Knowledge
	 * Discovery and Data Mining (KDD-96). AAAI Press. pp. 226–231.
	 * <p>
	 * Note that the generating distance may have been modified if invalid. If it is not strictly positive or not finite
	 * then it is set using {@link #computeGeneratingDistance(int)}. If it is larger than the data range allows it is
	 * set to the maximum distance that can be computed for the data range. If the data are colocated the distance is
	 * set to 1. The distance is stored in the results.
	 * <p>
	 * This creates a large memory structure. It can be held in memory for re-use when using a different number of min
	 * points. The tracker can be used to follow progress (see {@link #setTracker(TrackProgress)}).
	 *
	 * @param generatingDistanceE
	 *            the generating distance E (set to zero to auto calibrate)
	 * @param minPts
	 *            the min points for a core object
	 * @return the results (or null if the algorithm was stopped using the tracker)
	 */
	public DBSCANResult dbscan(float generatingDistanceE, int minPts)
	{
		if (minPts < 1)
			minPts = 1;

		long time = System.currentTimeMillis();

		initialiseDBSCAN(generatingDistanceE, minPts);

		// The distance may be updated
		generatingDistanceE = grid.generatingDistanceE;

		if (tracker != null)
		{
			tracker.log("Running DBSCAN ... Distance=%g, minPts=%d", generatingDistanceE, minPts);
			tracker.progress(0, xcoord.length);
		}

		// The generating distance (E) used in the paper is the maximum distance at which cluster
		// centres will be formed. This implementation uses the squared distance to avoid sqrt() 
		// function calls.
		final float e = generatingDistanceE * generatingDistanceE;

		final int size = xcoord.length;
		Molecule[] setOfObjects = grid.setOfObjects;

		// Working storage
		MoleculeQueue seeds = new MoleculeQueue(size);
		Counter counter = new Counter(size);

		for (int i = 0; i < size; i++)
		{
			final Molecule object = setOfObjects[i];
			if (object.isNotProcessed())
			{
				if (expandCluster(object, e, minPts, counter, seeds))
					break;
			}
		}

		boolean stopped = false;
		if (tracker != null)
		{
			stopped = tracker.isEnded();
			tracker.progress(1.0);

			if (stopped)
				tracker.log("Aborted DBSCAN");
		}

		DBSCANResult dbscanResult = null;
		if (!stopped)
		{
			// Convert the working data structure to the output
			DBSCANOrder[] dbscanOrder = new DBSCANOrder[size];
			for (int i = 0; i < size; i++)
				dbscanOrder[i] = setOfObjects[i].toDBSCANResult();
			dbscanResult = new DBSCANResult(this, minPts, generatingDistanceE, dbscanOrder);
			if (tracker != null)
			{
				time = System.currentTimeMillis() - time;
				tracker.log("Finished DBSCAN: %s (Time = %s)", Utils.pleural(counter.getTotalClusters(), "Cluster"),
						Utils.timeToString(time));
			}
		}

		finish();

		return dbscanResult;
	}

	private boolean expandCluster(Molecule object, float e, int minPts, Counter counter, MoleculeQueue seeds)
	{
		grid.findNeighbours(minPts, object, e);
		if (counter.increment())
			return true;

		object.markProcessed();
		object.setNumberOfPoints(grid.neighbours.size);
		if (grid.neighbours.size >= minPts)
		{
			// New cluster
			int clusterId = counter.nextClusterId();
			object.setClusterOrigin(clusterId);

			// Expand through the grid.neighbours
			seeds.clear();
			update(seeds, grid.neighbours, clusterId);

			while (seeds.hasNext())
			{
				object = seeds.next();
				grid.findNeighbours(minPts, object, e);
				if (counter.increment())
					return true;

				object.markProcessed();
				object.setNumberOfPoints(grid.neighbours.size);
				if (grid.neighbours.size >= minPts)
					update(seeds, grid.neighbours, clusterId);
			}
		}

		return false;
	}

	/**
	 * Update the set of points to search with any as yet unvisited points from the list of neighbours. Set the cluster
	 * Id of any unassigned points.
	 *
	 * @param pointsToSearch
	 *            the points to search
	 * @param neighbours
	 *            the neighbours
	 * @param clusterId
	 *            the cluster id
	 */
	private void update(MoleculeQueue pointsToSearch, MoleculeList neighbours, int clusterId)
	{
		for (int i = neighbours.size; i-- > 0;)
		{
			final Molecule object = neighbours.get(i);
			if (object.isNotInACluster())
			{
				object.setClusterMember(clusterId);

				// Ensure that the search is not repeated
				if (object.isNotProcessed())
					pointsToSearch.push(object);
			}
		}
	}

	@Override
	public OPTICSManager clone()
	{
		OPTICSManager m = (OPTICSManager) super.clone();
		m.options = options.clone();
		m.clearMemory();
		return m;
	}

	/**
	 * Gets the raw x data.
	 *
	 * @return the raw x data
	 */
	float[] getXData()
	{
		return xcoord;
	}

	/**
	 * Gets the raw y data.
	 *
	 * @return the raw y data
	 */
	float[] getYData()
	{
		return ycoord;
	}

	/**
	 * Gets the original X.
	 *
	 * @param i
	 *            the index
	 * @return the original X
	 */
	float getOriginalX(int i)
	{
		return xcoord[i] + originx;
	}

	/**
	 * Gets the original Y.
	 *
	 * @param i
	 *            the i
	 * @return the original Y
	 */
	float getOriginalY(int i)
	{
		return ycoord[i] + originy;
	}

	private SimpleFloatKdTree2D tree = null;;

	/**
	 * Compute (a sample of) the k-nearest neighbour distance for objects from the data
	 * The plot of the sorted k-distance can be used to pick the generating distance. Or it can be done
	 * automatically using a % noise threshold.
	 * <p>
	 * See:
	 * Jörg Sander, Martin Ester, Hans-Peter Kriegel, Xiaowei Xu
	 * Density-Based Clustering in Spatial Databases: The Algorithm GDBSCAN and Its Applications
	 * Data Mining and Knowledge Discovery, 1998.
	 *
	 * @param k
	 *            the k (automatically bounded between 1 and size-1)
	 * @param samples
	 *            the number of samples (set to negative to compute all samples)
	 * @param cache
	 *            Set to true to cache the KD-tree used for the nearest neighbour search
	 * @return the k-nearest neighbour distances
	 */
	public float[] nearestNeighbourDistance(int k, int samples, boolean cache)
	{
		int size = xcoord.length;
		if (size < 2)
			return new float[0];

		long time = System.currentTimeMillis();

		// Optionally compute all samples
		if (samples < 0)
			samples = size;

		// Bounds check k
		if (k < 1)
			k = 1;
		else if (k >= size)
			k = size - 1;

		final int n = Math.min(samples, size);
		float[] d = new float[n];

		if (tracker != null)
		{
			tracker.log("Computing %d nearest-neighbour distances, samples=%d", k, n);
			tracker.progress(0, n);
		}

		int[] indices;
		if (n <= size)
		{
			// Compute all
			indices = Utils.newArray(n, 0, 1);
		}
		else
		{
			// Random sample
			RandomDataGenerator r = new RandomDataGenerator();
			indices = r.nextPermutation(size, n);
		}

		// Use a KDtree to allow search of the space
		if (tree == null)
		{
			tree = new SimpleFloatKdTree2D.SqrEuclid2D();
			for (int i = 0; i < size; i++)
				tree.addPoint(new float[] { xcoord[i], ycoord[i] });
		}

		// Note: The k-nearest neighbour search will include the actual point so increment by 1
		k++;

		for (int i = 0; i < n; i++)
		{
			if (tracker != null)
				tracker.progress(i, n);
			int index = indices[i];
			float[] location = new float[] { xcoord[index], ycoord[index] };
			// The tree will use the squared distance so compute the root
			d[i] = (float) (Math.sqrt(tree.nearestNeighbor(location, k)[0]));
		}
		if (tracker != null)
		{
			time = System.currentTimeMillis() - time;
			tracker.log("Finished KNN computation (Time = " + Utils.timeToString(time) + ")");
			tracker.progress(1);
		}

		if (!cache)
			tree = null;

		return d;
	}
}
