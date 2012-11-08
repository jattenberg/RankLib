/*===============================================================================
 * Copyright (c) 2010-2012 University of Massachusetts.  All Rights Reserved.
 *
 * Use of the RankLib package is subject to the terms of the software license set 
 * forth in the LICENSE file included with this software, and also available at
 * http://people.cs.umass.edu/~vdang/ranklib_license.html
 *===============================================================================
 */

package ciir.umass.edu.learning;

import java.util.Arrays;

/**
 * @author vdang
 * 
 * This class implements objects to be ranked. In the context of Information retrieval, each instance is a query-url pair represented by a n-dimentional feature vector.
 * It should be general enough for other ranking applications as well (not limited to just IR I hope). 
 */
public class DataPoint {
	public static float INFINITY = -1000000.0f;
	public static int MAX_FEATURE = 51;
	public static int FEATURE_INCREASE = 10;
	
	public static int featureCount = 0;
	
	protected float label = 0.0f;//[ground truth] the real label of the data point (e.g. its degree of relevance according to the relevance judgment)
	protected String id = "";//id of this datapoint (e.g. document-id, query-id, etc)
	protected float[] fVals = null;//fVals[0] is un-used. Feature id MUST start from 1
	protected String description = "";
	
	protected double cached = -1.0;//the latest evaluation score of the learned model on this data point
	
	private String getKey(String pair)
	{
		return pair.substring(0, pair.indexOf(":"));
	}
	private String getValue(String pair)
	{
		return pair.substring(pair.lastIndexOf(":")+1);
	}
	
	/**
	 * The input must have the form: 
	 * @param text
	 */
	public DataPoint(String text)
	{
		fVals = new float[MAX_FEATURE];
		Arrays.fill(fVals, INFINITY);
		int lastFeature = -1;
		try {
			int idx = text.lastIndexOf("#");
			if(idx != -1)
			{
				description = text.substring(idx);
				text = text.substring(0, idx).trim();//remove the comment part at the end of the line
			}
			String[] fs = text.split(" ");
			label = Float.parseFloat(fs[0]);
			id = getValue(fs[1]);
			String key = "";
			String val = "";
			for(int i=2;i<fs.length;i++)
			{
				key = getKey(fs[i]);
				val = getValue(fs[i]);
				int f = Integer.parseInt(key);
				if(f >= MAX_FEATURE)
				{
					while(f >= MAX_FEATURE)
						MAX_FEATURE += FEATURE_INCREASE;
					float[] tmp = new float [MAX_FEATURE];
					System.arraycopy(fVals, 0, tmp, 0, fVals.length);
					Arrays.fill(tmp, fVals.length, MAX_FEATURE, INFINITY);
					fVals = tmp;
				}
				fVals[f] = Float.parseFloat(val);
				if(f > featureCount)//#feature will be the max_id observed
					featureCount = f;
				if(f > lastFeature)//note than lastFeature is the max_id observed for this current data point, whereas featureCount is the max_id observed on the entire dataset
					lastFeature = f;
			}
			//shrink fVals
			float[] tmp = new float[lastFeature+1];
			System.arraycopy(fVals, 0, tmp, 0, lastFeature+1);
			fVals = tmp;
		}
		catch(Exception ex)
		{
			System.out.println("Error in DataPoint(text) constructor");
		}
	}
	
	public String getID()
	{
		return id;
	}
	public void setID(String id)
	{
		this.id = id;
	}
	public float getLabel()
	{
		return label;
	}
	public void setLabel(float label)
	{
		this.label = label;
	}

	public float getFeatureValue(int fid)
	{
		if(fid >= fVals.length)
			return 0.0f;
		if(fVals[fid] < INFINITY+1)//+1 just to be safe
			return 0.0f;
		return fVals[fid];
	}
	public void setFeatureValue(int fid, float fval) 
	{
		fVals[fid] = fval;
	}
	
	public int getFeatureCount()
	{
		return featureCount;
	}
	public float[] getFeatureVector(int[] featureID)
	{
		float[] fvector = new float[featureID.length];
		for(int i=0;i<featureID.length;i++)
			fvector[i] = getFeatureValue(featureID[i]);
		return fvector;
	}
	public float[] getFeatureVector()
	{
		return fVals;
	}
	public float[] getExternalFeatureVector()
	{
		float[] ufVals = new float[fVals.length];
		System.arraycopy(fVals, 0, ufVals, 0, fVals.length);
		for(int i=0;i<ufVals.length;i++)
			if(ufVals[i] > INFINITY + 1)//+1 just to be safe ==> NOT padded features
				ufVals[i] = 0.0f;
		return ufVals;
	}
	public String getDescription()
	{
		return description;
	}
	public void setDescription(String description)
	{
		this.description = description;
	}
	
	public void normalize(int[] fids, float[] norm)
	{
		for(int i=0;i<fids.length;i++)
			if(norm[i] > 0.0)
				fVals[fids[i]] /= norm[i];
	}
	public String toString()
	{
		String output = label + " " + "id:" + id + " ";
		for(int i=1;i<fVals.length;i++)
			if(fVals[i] > INFINITY+1)//+1 just to be safe
				output += i + ":" + fVals[i] + ((i==fVals.length-1)?"":" ");
		output += " " + description;
		return output;
	}

	public void addFeatures(float[] values)
	{
		float[] tmp = new float[(featureCount+1) + values.length];
		System.arraycopy(fVals, 0, tmp, 0, fVals.length);
		Arrays.fill(tmp, fVals.length, featureCount+1, INFINITY);
		System.arraycopy(values, 0, tmp, featureCount+1, values.length);
		fVals = tmp;
	}
	
	public void setCached(double c)
	{
		cached = c;
	}
	public double getCached()
	{
		return cached;

	}
	public void resetCached()
	{
		cached = -100000000.0f;;
	}
}
