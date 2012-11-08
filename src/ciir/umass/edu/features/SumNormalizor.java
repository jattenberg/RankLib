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
public class SumNormalizor implements Normalizer {

	@Override
	public void normalize(RankList rl, int[] fids) {
		float[] norm = new float[fids.length];
		Arrays.fill(norm, 0);
		for(int i=0;i<rl.size();i++)
		{
			DataPoint dp = rl.get(i);
			for(int j=0;j<fids.length;j++)
				norm[j] += Math.abs(dp.getFeatureValue(fids[j]));
		}
		for(int i=0;i<rl.size();i++)
		{
			DataPoint dp = rl.get(i);
			dp.normalize(fids, norm);
		}
	}
	public String name()
	{
		return "sum";
	}
}
