/*===============================================================================
 * Copyright (c) 2010-2012 University of Massachusetts.  All Rights Reserved.
 *
 * Use of the RankLib package is subject to the terms of the software license set 
 * forth in the LICENSE file included with this software, and also available at
 * http://people.cs.umass.edu/~vdang/ranklib_license.html
 *===============================================================================
 */

package ciir.umass.edu.utilities;

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * @author vdang
 */
public class LinearComputer {
	Hashtable<String, Float> map = new Hashtable<String, Float>();
	
	public LinearComputer(String name, String description)
	{
		String[] strs = description.split(" ");
		for(int i=0;i<strs.length;i++)
		{
			strs[i] = strs[i].trim();
			if(strs[i].compareTo("")==0)
				continue;
			String key = strs[i].substring(0, strs[i].indexOf(":"));
			float value = Float.parseFloat(strs[i].substring(strs[i].lastIndexOf(":")+1));
			map.put(key, value);
		}
	}
	public float compute(float[] featureList)
	{
		float output = 0.0f;
		for(Enumeration<String> e = map.keys();e.hasMoreElements();)
		{
			String key = e.nextElement().toString();
			float weight = map.get(key).floatValue();
			int d = Integer.parseInt(key);
			float fVal = 0.0f;
			if(d < featureList.length)
				fVal = featureList[d];
			output += weight * fVal;
		}
		return output;
	}
	public String toString()
	{
		String output = "";
		for(Enumeration<String> e = map.keys();e.hasMoreElements();)
		{
			String key = e.nextElement().toString();
			float weight = map.get(key).floatValue();
			output += key + ":" + weight + " ";
		}
		return output.trim();
	}
	public int size()
	{
		return map.size();
	}
}
