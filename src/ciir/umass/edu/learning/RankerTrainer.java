/*===============================================================================
 * Copyright (c) 2010-2012 University of Massachusetts.  All Rights Reserved.
 *
 * Use of the RankLib package is subject to the terms of the software license set 
 * forth in the LICENSE file included with this software, and also available at
 * http://people.cs.umass.edu/~vdang/ranklib_license.html
 *===============================================================================
 */

package ciir.umass.edu.learning;

import java.util.List;

import ciir.umass.edu.metric.MetricScorer;

/**
 * @author vdang
 * 
 * This class is for users who want to use this library programmatically. It provides trained rankers of different types with respect to user-specified parameters.
 */
public class RankerTrainer {

	protected RankerFactory rf = new RankerFactory();
	
	public Ranker train(RANKER_TYPE type, List<RankList> samples, int[] features, MetricScorer scorer)
	{
		Ranker ranker = rf.createRanker(type, samples, features);
		ranker.set(scorer);
		ranker.init();
		ranker.learn();
		return ranker;
	}
	
}
