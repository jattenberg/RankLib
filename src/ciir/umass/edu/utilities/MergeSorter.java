/*===============================================================================
 * Copyright (c) 2010-2012 University of Massachusetts.  All Rights Reserved.
 *
 * Use of the RankLib package is subject to the terms of the software license set 
 * forth in the LICENSE file included with this software, and also available at
 * http://people.cs.umass.edu/~vdang/ranklib_license.html
 *===============================================================================
 */

package ciir.umass.edu.utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 
 * @author vdang
 *
 */
public class MergeSorter {

	public static void main(String[] args)
	{
		List<Float> list = new ArrayList<Float>();
		Random r = new Random();
		for(int i=0;i<10;i++)
		{
			float x = r.nextFloat();
			System.out.print(x + " ");
			list.add(x);
		}
		System.out.println("");
		int[] idx = sort(list, false);
		for(int i=0;i<idx.length;i++)
			System.out.print(list.get(idx[i]) + " ");
	}
	
	public static int[] sort(List<Float> list, boolean asc)
	{
		float[] values = new float[list.size()];
		int[] idx = new int[list.size()];
		for(int i=0;i<list.size();i++)
		{
			idx[i] = i;
			values[i] = list.get(i);
		}
		return sort(values, asc);
	}
	public static int[] sort(float[] list, boolean asc)
	{
		int[] idx = new int[list.length];
		for(int i=0;i<list.length;i++)
			idx[i] = i;
		return sort(list, idx, asc);
	}
	public static int[] sort(float[] list, int[] idx, boolean asc)
	{
		if(idx.length == 1)
			return idx;
		
		int mid = idx.length / 2;		
		int[] left = new int[mid];
		int[] right = new int[idx.length-mid];
		
		for(int i=0;i<mid;i++)
			left[i] = idx[i];
		for(int i=mid;i<idx.length;i++)
			right[i-mid] = idx[i];
		
		left = sort(list, left, asc);
		right = sort(list, right, asc);
		
		return merge(list, left, right, asc);
	}
	private static int[] merge(float[] list, int[] left, int[] right, boolean asc)
	{
		int[] idx = new int[left.length + right.length];
		int i=0;
		int j=0;
		int c=0;
		while(i < left.length && j < right.length)
		{
			if(asc)
			{
				if(list[left[i]] <= list[right[j]])
					idx[c++] = left[i++];
				else
					idx[c++] = right[j++];
			}
			else
			{
				if(list[left[i]] >= list[right[j]])
					idx[c++] = left[i++];
				else
					idx[c++] = right[j++];
			}
		}
		for(;i<left.length;i++)
			idx[c++] = left[i];
		for(;j<right.length;j++)
			idx[c++] = right[j];
		return idx;
	}

	public static int[] sort(double[] list, boolean asc)
	{
		int[] idx = new int[list.length];
		for(int i=0;i<list.length;i++)
			idx[i] = i;
		return sort(list, idx, asc);
	}
	public static int[] sort(double[] list, int[] idx, boolean asc)
	{
		if(idx.length == 1)
			return idx;
		
		int mid = idx.length / 2;		
		int[] left = new int[mid];
		int[] right = new int[idx.length-mid];
		
		for(int i=0;i<mid;i++)
			left[i] = idx[i];
		for(int i=mid;i<idx.length;i++)
			right[i-mid] = idx[i];
		
		left = sort(list, left, asc);
		right = sort(list, right, asc);
		
		return merge(list, left, right, asc);
	}
	private static int[] merge(double[] list, int[] left, int[] right, boolean asc)
	{
		int[] idx = new int[left.length + right.length];
		int i=0;
		int j=0;
		int c=0;
		while(i < left.length && j < right.length)
		{
			if(asc)
			{
				if(list[left[i]] <= list[right[j]])
					idx[c++] = left[i++];
				else
					idx[c++] = right[j++];
			}
			else
			{
				if(list[left[i]] >= list[right[j]])
					idx[c++] = left[i++];
				else
					idx[c++] = right[j++];
			}
		}
		for(;i<left.length;i++)
			idx[c++] = left[i];
		for(;j<right.length;j++)
			idx[c++] = right[j];
		return idx;
	}
}
