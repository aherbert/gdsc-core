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
package gdsc.core.match;

import java.util.Arrays;
import java.util.Comparator;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;
import org.junit.Test;

import gdsc.core.test.BaseTimingTask;
import gdsc.core.test.TimingService;

public class AssignmentComparatorTest
{
	private static class IntegerSortData implements Comparable<IntegerSortData>
	{
		final int data;

		IntegerSortData(int data)
		{
			this.data = data;
		}

		public int compareTo(IntegerSortData o)
		{
			if (data < o.data)
				return -1;
			if (data > o.data)
				return 1;
			return 0;
		}
	}

	private static class DoubleSortData implements Comparable<DoubleSortData>
	{
		final double data;

		DoubleSortData(double data)
		{
			this.data = data;
		}

		public int compareTo(DoubleSortData o)
		{
			if (data < o.data)
				return -1;
			if (data > o.data)
				return 1;
			return 0;
		}
	}

	static int[][] intData;
	static double[][] doubleData;
	static int[][] intExp;
	static double[][] doubleExp;
	static IntegerSortData[][] intSortData;
	static DoubleSortData[][] doubleSortData;
	static Assignment[][] aData;
	static
	{
		RandomGenerator r = new Well19937c(30051977);
		int size = 100;
		// The assignment data will be concatenated blocks of sorted arrays
		int blocks = 50;
		int blockSize = 10;
		int length = blocks * blockSize;

		intData = new int[size][];
		doubleData = new double[size][];
		intExp = new int[size][];
		doubleExp = new double[size][];
		intSortData = new IntegerSortData[size][];
		doubleSortData = new DoubleSortData[size][];
		aData = new Assignment[size][];
		final int upper = 65536;
		for (int i = size; i-- > 0;)
		{
			int[] idata = new int[length];
			double[] ddata = new double[length];
			IntegerSortData[] sdata = new IntegerSortData[length];
			DoubleSortData[] dsdata = new DoubleSortData[length];
			Assignment[] adata = new Assignment[length];
			intData[i] = idata;
			doubleData[i] = ddata;
			intSortData[i] = sdata;
			doubleSortData[i] = dsdata;
			aData[i] = adata;

			// Build the data of sorted blocks
			for (int b = 0; b < blocks; b++)
			{
				final int[] block = new int[blockSize];
				for (int j = 0; j < block.length; j++)
					block[j] = r.nextInt(upper);
				Arrays.sort(block);
				System.arraycopy(block, 0, idata, b * blockSize, blockSize);
			}

			// Copy
			for (int j = length; j-- > 0;)
			{
				final int k = idata[j];
				final double d = k / upper;
				ddata[j] = d;
				sdata[j] = new IntegerSortData(k);
				dsdata[j] = new DoubleSortData(d);
				adata[j] = new ImmutableAssignment(0, 0, d);
			}

			idata = idata.clone();
			Arrays.sort(idata);
			intExp[i] = idata;
			ddata = ddata.clone();
			Arrays.sort(ddata);
			doubleExp[i] = ddata;
		}
	}

	private abstract class MyTimingTask extends BaseTimingTask
	{
		public MyTimingTask(String name)
		{
			super(name);
		}

		public int getSize()
		{
			return intData.length;
		}
	}

	private abstract class AssignmentTimingTask extends MyTimingTask
	{
		public AssignmentTimingTask(String name)
		{
			super(name);
		}

		@Override
		public void check(int i, Object result)
		{
			double[] exp = doubleExp[i];
			Assignment[] obs = (Assignment[]) result;
			for (int j = 0; j < exp.length; j++)
				if (exp[j] != obs[j].getDistance())
					throw new AssertionError(getName());
		}
	}

	@Test
	public void canComputeSortSpeed()
	{
		//@formatter:off
		TimingService ts = new TimingService();
		ts.execute(new MyTimingTask("int[]")
		{
			public Object getData(int i) {	return intData[i].clone();	}
			public Object run(Object data) { Arrays.sort((int[]) data); return data; }
			public void check(int i, Object result)	{
				int[] exp = intExp[i];
				int[] obs = (int[]) result;
				for (int j=0; j<exp.length; j++)
					if (exp[j] != obs[j])
						throw new AssertionError(getName());
			}
		});
		ts.execute(new MyTimingTask("double[]")
		{
			public Object getData(int i) {	return doubleData[i].clone();	}
			public Object run(Object data) { Arrays.sort((double[]) data); return data; }
			public void check(int i, Object result)	{
				double[] exp = doubleExp[i];
				double[] obs = (double[]) result;
				for (int j=0; j<exp.length; j++)
					if (exp[j] != obs[j])
						throw new AssertionError(getName());
			}
		});
		ts.execute(new MyTimingTask("long[]")
		{
			public Object getData(int i) {	
				long[] data = new long[intData[i].length];
				for (int j=data.length; j-- > 0; )
					data[j] = intData[i][j];
				return data;
			}
			public Object run(Object data) { Arrays.sort((long[]) data); return data; }
		});
		final Comparator<int[]> c1 = new Comparator<int[]>(){ 
			public int compare(int[] o1, int[] o2) { 
				if (o1[0]<o2[0]) return -1; 
				if (o1[0]>o2[0]) return 1;
				return 0;
			}};
		ts.execute(new MyTimingTask("int[][]")
		{
			public Object getData(int i) {
				int[] d = intData[i];
				int[][] data = new int[d.length][];
				for (int j=d.length; j-- > 0; )
					data[j] = new int[]{ d[j] };
				return data;	
			}
			public Object run(Object data) { 
				Arrays.sort((int[][]) data, c1); 
				return data; 
			}
			public void check(int i, Object result)	{
				int[] exp = intExp[i];
				int[][] obs = (int[][]) result;
				for (int j=0; j<exp.length; j++)
					if (exp[j] != obs[j][0])
						throw new AssertionError(getName());
			}
		});
		final Comparator<double[]> c2 = new Comparator<double[]>(){ 
			public int compare(double[] o1, double[] o2) { 
				if (o1[0]<o2[0]) return -1; 
				if (o1[0]>o2[0]) return 1;
				return 0;
			}};
		ts.execute(new MyTimingTask("double[][]")
		{
			public Object getData(int i) {
				double[] d = doubleData[i];
				double[][] data = new double[d.length][];
				for (int j=d.length; j-- > 0; )
					data[j] = new double[]{ d[j] };
				return data;	
			}
			public Object run(Object data) { 
				Arrays.sort((double[][]) data, c2); 
				return data; 
			}
			public void check(int i, Object result)	{
				double[] exp = doubleExp[i];
				double[][] obs = (double[][]) result;
				for (int j=0; j<exp.length; j++)
					if (exp[j] != obs[j][0])
						throw new AssertionError(getName());
			}
		});
		final Comparator<int[]> c3 = new Comparator<int[]>(){ 
			public int compare(int[] o1, int[] o2) { 
				return o1[0]-o2[0];
			}};
		ts.execute(new MyTimingTask("int[][] subtract")
		{
			public Object getData(int i) {
				int[] d = intData[i];
				int[][] data = new int[d.length][];
				for (int j=d.length; j-- > 0; )
					data[j] = new int[]{ d[j] };
				return data;	
			}
			public Object run(Object data) { 
				Arrays.sort((int[][]) data, c3); 
				return data; 
			}
			public void check(int i, Object result)	{
				int[] exp = intExp[i];
				int[][] obs = (int[][]) result;
				for (int j=0; j<exp.length; j++)
					if (exp[j] != obs[j][0])
						throw new AssertionError(getName());
			}
		});
		final Comparator<long[]> c4 = new Comparator<long[]>(){ 
			public int compare(long[] o1, long[] o2) { 
				if (o1[0]<o2[0]) return -1; 
				if (o1[0]>o2[0]) return 1;
				return 0;
			}};
		ts.execute(new MyTimingTask("long[][]")
		{
			public Object getData(int i) {
				int[] d = intData[i];
				long[][] data = new long[d.length][];
				for (int j=d.length; j-- > 0; )
					data[j] = new long[]{ d[j] };
				return data;	
			}
			public Object run(Object data) { 
				Arrays.sort((long[][]) data, c4); 
				return data; 
			}
		});
		ts.execute(new MyTimingTask("IntegerSortData[]")
		{
			public Object getData(int i) {
				return Arrays.copyOf(intSortData[i], intSortData[i].length);	
			}
			public Object run(Object data) { 
				Arrays.sort((IntegerSortData[]) data); 
				return data; 
			}
			public void check(int i, Object result)	{
				int[] exp = intExp[i];
				IntegerSortData[] obs = (IntegerSortData[]) result;
				for (int j=0; j<exp.length; j++)
					if (exp[j] != obs[j].data)
						throw new AssertionError(getName());
			}
		});
		ts.execute(new MyTimingTask("DoubleSortData[]")
		{
			public Object getData(int i) {
				return Arrays.copyOf(doubleSortData[i], doubleSortData[i].length);	
			}
			public Object run(Object data) { 
				Arrays.sort((DoubleSortData[]) data); 
				return data; 
			}
			public void check(int i, Object result)	{
				double[] exp = doubleExp[i];
				DoubleSortData[] obs = (DoubleSortData[]) result;
				for (int j=0; j<exp.length; j++)
					if (exp[j] != obs[j].data)
						throw new AssertionError(getName());
			}
		});
		ts.execute(new AssignmentTimingTask("Assignment[] sort1")
		{
			public Object getData(int i) {
				return Arrays.copyOf(aData[i], aData[i].length);	
			}
			public Object run(Object data) { 
				AssignmentComparator.sort1((Assignment[]) data); 
				return data; 
			}
		});
		ts.execute(new AssignmentTimingTask("Assignment[] sort2")
		{
			public Object getData(int i) {
				return Arrays.copyOf(aData[i], aData[i].length);	
			}
			public Object run(Object data) { 
				AssignmentComparator.sort2((Assignment[]) data); 
				return data; 
			}
		});
		ts.execute(new AssignmentTimingTask("Assignment[] sort3")
		{
			public Object getData(int i) {
				return Arrays.copyOf(aData[i], aData[i].length);	
			}
			public Object run(Object data) { 
				AssignmentComparator.sort3((Assignment[]) data); 
				return data; 
			}
		});
		ts.execute(new AssignmentTimingTask("Assignment[] sort4")
		{
			public Object getData(int i) {
				return Arrays.copyOf(aData[i], aData[i].length);	
			}
			public Object run(Object data) { 
				AssignmentComparator.sort4((Assignment[]) data); 
				return data; 
			}
		});
		ts.execute(new AssignmentTimingTask("Assignment[] sort")
		{
			public Object getData(int i) {
				return Arrays.copyOf(aData[i], aData[i].length);	
			}
			public Object run(Object data) { 
				AssignmentComparator.sort((Assignment[]) data); 
				return data; 
			}
		});
		//@formatter:on

		ts.check();

		int size = ts.repeat();
		ts.repeat(size);

		ts.report();
	}
}
