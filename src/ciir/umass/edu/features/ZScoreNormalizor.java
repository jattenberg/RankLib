/*===============================================================================
 * Copyright (c) 2010-2012 University of Massachusetts.  All Rights Reserved.
 *
 * Use of the RankLib package is subject to the terms of the software license set 
 * forth in the LICENSE file included with this software, and also available at
 * http://people.cs.umass.edu/~vdang/ranklib_license.html
 *===============================================================================
 */

package ciir.umass.edu.features;

import java.util.Arrays;

import ciir.umass.edu.learning.DataPoint;
import ciir.umass.edu.learning.RankList;

/**
 * @author vdang
 */
public class ZScoreNormalizor implements Normalizer {

	@Override
	public void normalize(RankList rl, int[] fids) {
		
		float[] mean = new float[fids.length];
		float[] std = new float[fids.length];
		Arrays.fill(mean, 0);
		Arrays.fill(std, 0);
		for(int i=0;i<rl.size();i++)
		{
			DataPoint dp = rl.get(i);
			for(int j=0;j<fids.length;j++)
				mean[j] += dp.getFeatureValue(fids[j]);
		}
		
		for(int j=0;j<fids.length;j++)
		{
			mean[j] = mean[j] / rl.size();
			for(int i=0;i<rl.size();i++)
			{
				DataPoint p = rl.get(i);
				float x = p.getFeatureValue(fids[j]) - mean[j];
				std[j] += x*x;
			}
			std[j] = (float) Math.sqrt(std[j] / (rl.size()-1));
			//normalize
			if(std[j] > 0.0)
			{
				for(int i=0;i<rl.size();i++)
				{
					DataPoint p = rl.get(i);
					float x = (p.getFeatureValue(fids[j]) - mean[j])/std[j];//x ~ standard normal (0, 1)
					p.setFeatureValue(fids[j], x);
				}
			}
		}
	}
	public String name()
	{
		return "zscore";
	}
}
