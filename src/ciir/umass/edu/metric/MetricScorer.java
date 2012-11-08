/*===============================================================================
 * Copyright (c) 2010-2012 University of Massachusetts.  All Rights Reserved.
 *
 * Use of the RankLib package is subject to the terms of the software license set 
 * forth in the LICENSE file included with this software, and also available at
 * http://people.cs.umass.edu/~vdang/ranklib_license.html
 *===============================================================================
 */

package ciir.umass.edu.metric;

import java.util.List;

import ciir.umass.edu.learning.RankList;

/**
 * @author vdang
 * A generic retrieval measure computation interface. 
 */
public class MetricScorer {

	protected int k = 10;
	
	public MetricScorer() 
	{
		
	}
	public void setK(int k)
	{
		this.k = k;
	}
	public void loadExternalRelevanceJudgment(String qrelFile)
	{
		
	}
	public double score(List<RankList> rl)
	{
		double score = 0.0;
		for(int i=0;i<rl.size();i++)
			score += score(rl.get(i));
		return score/rl.size();
	}
	
	/**
	 * MUST BE OVER-RIDDEN
	 * @param rl
	 * @return
	 */
	public double score(RankList rl)
	{
		return 0.0;
	}
	public MetricScorer clone()
	{
		return null;
	}
	public String name()
	{
		return "";
	}
	public double[][] swapChange(RankList rl)
	{
		return null;
	}
}
