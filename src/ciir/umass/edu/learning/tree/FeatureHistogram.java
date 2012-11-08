/*===============================================================================
 * Copyright (c) 2010-2012 University of Massachusetts.  All Rights Reserved.
 *
 * Use of the RankLib package is subject to the terms of the software license set 
 * forth in the LICENSE file included with this software, and also available at
 * http://people.cs.umass.edu/~vdang/ranklib_license.html
 *===============================================================================
 */

package ciir.umass.edu.learning.tree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import ciir.umass.edu.learning.DataPoint;

/**
 * @author vdang
 */
public class FeatureHistogram {
	//Parameter
	public static float samplingRate = 1;
	
	//Variables
	public int[] features = null;
	public float[][] thresholds = null;
	public double[][] sum = null;
	public double[][] sqSum = null;
	public int[][] count = null;
	public int[][] sampleToThresholdMap = null;
	
	public FeatureHistogram()
	{
		
	}
	public void construct(DataPoint[] samples, float[] labels, int[][] sampleSortedIdx, int[] features, float[][] thresholds)
	{
		this.features = features;
		this.thresholds = thresholds;
		
		sum = new double[features.length][];
		sqSum = new double[features.length][];
		count = new int[features.length][];
		sampleToThresholdMap = new int[features.length][];
		for(int i=0;i<features.length;i++)
		{			
			int fid = features[i];			
			//get the list of samples associated with this node (sorted in ascending order with respect to the current feature)
			int[] idx = sampleSortedIdx[i];
			
			double sumLeft = 0;
			double sqSumLeft = 0;
			float[] threshold = thresholds[i];
			double[] sumLabel = new double[threshold.length];
			double[] sqSumLabel = new double[threshold.length];
			int[] c = new int[threshold.length];
			int[] stMap = new int[samples.length];
			
			int last = -1;
			for(int t=0;t<threshold.length;t++)
			{
				int j=last+1;
				//find the first sample that exceeds the current threshold
				for(;j<idx.length;j++)
				{
					int k = idx[j];
					if(samples[k].getFeatureValue(fid) >  threshold[t])
						break;
					sumLeft += labels[k];
					sqSumLeft += labels[k] * labels[k];
					stMap[k] =  t;
				}
				last = j-1;	
				sumLabel[t] = sumLeft;
				sqSumLabel[t] = sqSumLeft;
				c[t] = last+1;
			}
			sampleToThresholdMap[i] = stMap;
			sum[i] = sumLabel;
			sqSum[i] = sqSumLabel;
			count[i] = c;
		}
	}
	public void update(float[] labels)
	{
		for(int f=0;f<features.length;f++)
		{
			Arrays.fill(sum[f], 0);
			Arrays.fill(sqSum[f], 0);			
		}
		for(int k=0;k<labels.length;k++)
		{
			for(int f=0;f<features.length;f++)
			{
				int t = sampleToThresholdMap[f][k];
				sum[f][t] += labels[k];
				sqSum[f][t] += labels[k]*labels[k];
				//count doesn't change, so no need to re-compute
			}
		}
		for(int f=0;f<features.length;f++)
		{			
			for(int t=1;t<thresholds[f].length;t++)
			{
				sum[f][t] += sum[f][t-1];
				sqSum[f][t] += sqSum[f][t-1];
			}
		}
	}
	public void construct(FeatureHistogram parent, int[] soi, float[] labels)
	{
		this.features = parent.features;
		this.thresholds = parent.thresholds;
		
		sum = new double[features.length][];
		sqSum = new double[features.length][];
		count = new int[features.length][];
		sampleToThresholdMap = parent.sampleToThresholdMap;
		
		//init
		for(int i=0;i<features.length;i++)
		{			
			float[] threshold = thresholds[i];
			sum[i] = new double[threshold.length];
			sqSum[i] = new double[threshold.length];
			count[i] = new int[threshold.length];
			Arrays.fill(sum[i], 0);
			Arrays.fill(sqSum[i], 0);		
			Arrays.fill(count[i], 0);
		}
		
		//update
		for(int i=0;i<soi.length;i++)
		{
			int k = soi[i];
			for(int f=0;f<features.length;f++)
			{
				int t = sampleToThresholdMap[f][k];
				sum[f][t] += labels[k];
				sqSum[f][t] += labels[k]*labels[k];
				count[f][t] ++;
			}
		}
		
		for(int f=0;f<features.length;f++)
		{			
			for(int t=1;t<thresholds[f].length;t++)
			{
				sum[f][t] += sum[f][t-1];
				sqSum[f][t] += sqSum[f][t-1];
				count[f][t] += count[f][t-1];
			}
		}
	}
	
	public void construct(FeatureHistogram parent, FeatureHistogram leftSibling)
	{
		this.features = parent.features;
		this.thresholds = parent.thresholds;
		
		sum = new double[features.length][];
		sqSum = new double[features.length][];
		count = new int[features.length][];
		sampleToThresholdMap = parent.sampleToThresholdMap;
		
		for(int i=0;i<features.length;i++)
		{			
			float[] threshold = thresholds[i];
			sum[i] = new double[threshold.length];
			sqSum[i] = new double[threshold.length];
			count[i] = new int[threshold.length];
		}
		
		for(int f=0;f<features.length;f++)
		{
			float[] threshold = thresholds[f];
			sum[f] = new double[threshold.length];
			sqSum[f] = new double[threshold.length];
			count[f] = new int[threshold.length];
			for(int t=0;t<threshold.length;t++)
			{
				sum[f][t] = parent.sum[f][t] - leftSibling.sum[f][t];
				sqSum[f][t] += parent.sqSum[f][t] - leftSibling.sqSum[f][t];
				count[f][t] += parent.count[f][t] - leftSibling.count[f][t];
			}
		}
	}
	
	public Split findBestSplit(Split sp, DataPoint[] samples, float[] labels, int minLeafSupport)
	{
		if(sp.getDeviance() >= 0.0 && sp.getDeviance() <= 0.0)//equals 0
			return null;//no need to split
		
		int bestFeatureIdx = -1;
		int bestThresholdIdx = -1;
		double bestVarLeft = -1;
		double bestVarRight = -1;
		double minS = Double.MAX_VALUE;
		
		int[] usedFeatures = null;//index of the features to be used for tree splitting
		if(samplingRate < 1)//need to do sub sampling (feature sampling)
		{
			int size = (int)(samplingRate * features.length);
			usedFeatures = new int[size];
			//put all features into a pool
			List<Integer> fpool = new ArrayList<Integer>();
			for(int i=0;i<features.length;i++)
				fpool.add(i);
			//do sampling, without replacement
			Random r = new Random();
			for(int i=0;i<size;i++)
			{
				int sel = r.nextInt(fpool.size());
				usedFeatures[i] = fpool.get(sel);
				fpool.remove(sel);
			}
		}
		else//no sub-sampling, all features will be used
		{
			usedFeatures = new int[features.length];
			for(int i=0;i<features.length;i++)
				usedFeatures[i] = i;
		}
		
		for(int f=0;f<usedFeatures.length;f++)
		{
			int i = usedFeatures[f];
			float[] threshold = thresholds[i];
			
			double[] sumLabel = sum[i];
			double[] sqSumLabel = sqSum[i];
			int[] sampleCount = count[i];
			
			double s = sumLabel[sumLabel.length-1];
			double sq = sqSumLabel[sumLabel.length-1];
			int c = sampleCount[sumLabel.length-1];
			
			for(int t=0;t<threshold.length;t++)
			{
				int countLeft = sampleCount[t];
				int countRight = c - countLeft;
				if(countLeft < minLeafSupport || countRight < minLeafSupport)
					continue;
				
				double sumLeft = sumLabel[t];
				double sqSumLeft = sqSumLabel[t];
				
				double sumRight = s - sumLeft;
				double sqSumRight = sq - sqSumLeft;
				
				double varLeft = sqSumLeft - sumLeft * sumLeft / countLeft;
				double varRight = sqSumRight - sumRight * sumRight / countRight;
				double S = varLeft + varRight;
				
				if(minS > S)
				{
					minS = S;
					bestFeatureIdx = i;
					bestThresholdIdx = t;
					bestVarLeft = varLeft;
					bestVarRight = varRight;
				}
			}
		}
		
		if(minS >= Double.MAX_VALUE)//unsplitable, for some reason...
			return null;
		
		//if(minS >= sp.getDeviance())
			//return null;
		
		float[] threshold = thresholds[bestFeatureIdx];
		double[] sumLabel = sum[bestFeatureIdx];
		int[] sampleCount = count[bestFeatureIdx];
		
		double s = sumLabel[sumLabel.length-1];
		int c = sampleCount[sumLabel.length-1];
		
		double sumLeft = sumLabel[bestThresholdIdx];
		int countLeft = sampleCount[bestThresholdIdx];
		
		double sumRight = s - sumLeft;
		int countRight = c - countLeft;
		
		//if(countLeft == 0 || countRight == 0)
			//return null;
		
		int[] left = new int[countLeft];
		int[] right = new int[countRight];
		int l = 0;
		int r = 0;
		int[] idx = sp.getSamples();
		for(int j=0;j<idx.length;j++)
		{
			int k = idx[j];
			if(samples[k].getFeatureValue(features[bestFeatureIdx]) <= threshold[bestThresholdIdx])//go to the left
				left[l++] = k;
			else//go to the right
				right[r++] = k;
		}
		
		FeatureHistogram lh = new FeatureHistogram();
		lh.construct(sp.hist, left, labels);
		FeatureHistogram lr = new FeatureHistogram();
		lr.construct(sp.hist, lh);

		sp.set(features[bestFeatureIdx], thresholds[bestFeatureIdx][bestThresholdIdx], (float)minS);
		sp.setLeft(new Split(left, lh, (float)bestVarLeft, sumLeft));
		sp.setRight(new Split(right, lr, (float)bestVarRight, sumRight));
		
		sp.clearSamples();
		
		return sp;
	}	
}
