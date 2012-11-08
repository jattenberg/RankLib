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
import java.util.List;

import ciir.umass.edu.learning.DataPoint;

/**
 * 
 * @author vdang
 *
 */
public class Split {
	//Key attributes of a split (tree node)
	private int featureID = -1;
	private float threshold = 0F;
	private float avgLabel = 0.0F;
	
	//Intermediate variables (ONLY used during learning)
	//*DO NOT* attempt to access them once the training is done
	private double sumLabel = 0.0;
	private double sqSumLabel = 0.0;
	private Split left = null;
	private Split right = null;
	private float deviance = 0F;//mean squared error "S"
	private int[][] sortedSampleIDs = null;
	public int[] samples = null;
	public FeatureHistogram hist = null;
	
	public Split()
	{
		
	}
	public Split(int featureID, float threshold, float deviance)
	{
		this.featureID = featureID;
		this.threshold = threshold;
		this.deviance = deviance;
	}
	public Split(int[][] sortedSampleIDs, float deviance, double sumLabel, double sqSumLabel)
	{
		this.sortedSampleIDs = sortedSampleIDs;
		this.deviance = deviance;
		this.sumLabel = sumLabel;
		this.sqSumLabel = sqSumLabel;
		avgLabel = (float)(sumLabel/sortedSampleIDs[0].length);
	}
	
	public Split(int[] samples, FeatureHistogram hist, float deviance, double sumLabel)
	{
		this.samples = samples;
		this.hist = hist;
		this.deviance = deviance;
		this.sumLabel = sumLabel;
		avgLabel = (float)(sumLabel/samples.length);
	}
	
	public void set(int featureID, float threshold, float deviance)
	{
		this.featureID = featureID;
		this.threshold = threshold;
		this.deviance = deviance;
	}
	public void setLeft(Split s)
	{
		left = s;
	}
	public void setRight(Split s)
	{
		right = s;
	}
	public void setOutput(float output)
	{
		avgLabel = output;
	}
	
	public Split getLeft()
	{
		return left;
	}
	public Split getRight()
	{
		return right;
	}
	public float getDeviance()
	{
		return deviance;
	}
	public float getOutput()
	{
		return avgLabel;
	}
	
	public List<Split> leaves()
	{
		List<Split> list = new ArrayList<Split>();
		leaves(list);
		return list;
	}
	private void leaves(List<Split> leaves)
	{
		if(featureID == -1)
			leaves.add(this);
		else
		{
			left.leaves(leaves);
			right.leaves(leaves);
		}
	}
	
	public float eval(DataPoint dp)
	{
		if(featureID == -1)
			return avgLabel;
		if(dp.getFeatureValue(featureID) <= threshold)
			return left.eval(dp);
		return right.eval(dp);
	}
	
	public String toString()
	{
		return toString("");
	}
	public String toString(String indent)
	{
		String strOutput = indent + "<split>" + "\n";
		strOutput += getString(indent + "\t");
		strOutput += indent + "</split>" + "\n";
		return strOutput;
	}
	public String getString(String indent)
	{
		String strOutput = "";
		if(featureID == -1)
		{
			strOutput += indent + "<output> " + avgLabel + " </output>" + "\n";
		}
		else
		{
			strOutput += indent + "<feature> " + featureID + " </feature>" + "\n";
			strOutput += indent + "<threshold> " + threshold + " </threshold>" + "\n";
			strOutput += indent + "<split pos=\"left\">" + "\n";
			strOutput += left.getString(indent + "\t");
			strOutput += indent + "</split>" + "\n";
			strOutput += indent + "<split pos=\"right\">" + "\n";
			strOutput += right.getString(indent + "\t");
			strOutput += indent + "</split>" + "\n";
		}
		return strOutput;
	}
	
	//Internal functions(ONLY used during learning)
	//*DO NOT* attempt to call them once the training is done
	public int[] getSamples()
	{
		if(sortedSampleIDs != null)
			return sortedSampleIDs[0];
		return samples;
	}
	public int[][] getSampleSortedIndex()
	{
		return sortedSampleIDs;
	}
	public double getSumLabel()
	{
		return sumLabel;
	}
	public double getSqSumLabel()
	{
		return sqSumLabel;
	}
	public void clearSamples()
	{
		sortedSampleIDs = null;
		samples = null;
		hist = null;
	}
}