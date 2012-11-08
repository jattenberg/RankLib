/*===============================================================================
 * Copyright (c) 2010-2012 University of Massachusetts.  All Rights Reserved.
 *
 * Use of the RankLib package is subject to the terms of the software license set 
 * forth in the LICENSE file included with this software, and also available at
 * http://people.cs.umass.edu/~vdang/ranklib_license.html
 *===============================================================================
 */

package ciir.umass.edu.learning;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.List;

import ciir.umass.edu.learning.boosting.AdaRank;
import ciir.umass.edu.learning.boosting.RankBoost;
import ciir.umass.edu.learning.neuralnet.LambdaRank;
import ciir.umass.edu.learning.neuralnet.ListNet;
import ciir.umass.edu.learning.neuralnet.RankNet;
import ciir.umass.edu.learning.tree.MART;
import ciir.umass.edu.learning.tree.LambdaMART;
import ciir.umass.edu.learning.tree.RFRanker;
import ciir.umass.edu.utilities.FileUtils;

/**
 * @author vdang
 * 
 * This class implements the Ranker factory. All ranking algorithms implemented have to be recognized in this class. 
 */
public class RankerFactory {

	protected Ranker[] rFactory = new Ranker[]{new MART(), new RankBoost(), new RankNet(), new AdaRank(), new CoorAscent(), new LambdaRank(), new LambdaMART(), new ListNet(), new RFRanker()};
	protected static Hashtable<String, RANKER_TYPE> map = new Hashtable<String, RANKER_TYPE>();
	
	public RankerFactory()
	{
		map.put(createRanker(RANKER_TYPE.MART).name().toUpperCase(), RANKER_TYPE.MART);
		map.put(createRanker(RANKER_TYPE.RANKNET).name().toUpperCase(), RANKER_TYPE.RANKNET);
		map.put(createRanker(RANKER_TYPE.RANKBOOST).name().toUpperCase(), RANKER_TYPE.RANKBOOST);
		map.put(createRanker(RANKER_TYPE.ADARANK).name().toUpperCase(), RANKER_TYPE.ADARANK);
		map.put(createRanker(RANKER_TYPE.COOR_ASCENT).name().toUpperCase(), RANKER_TYPE.COOR_ASCENT);
		map.put(createRanker(RANKER_TYPE.LAMBDARANK).name().toUpperCase(), RANKER_TYPE.LAMBDARANK);
		map.put(createRanker(RANKER_TYPE.LAMBDAMART).name().toUpperCase(), RANKER_TYPE.LAMBDAMART);
		map.put(createRanker(RANKER_TYPE.LISTNET).name().toUpperCase(), RANKER_TYPE.LISTNET);
		map.put(createRanker(RANKER_TYPE.RANDOM_FOREST).name().toUpperCase(), RANKER_TYPE.RANDOM_FOREST);
	}
	
	public Ranker createRanker(RANKER_TYPE type)
	{
		Ranker r = rFactory[type.ordinal() - RANKER_TYPE.MART.ordinal()].clone();
		return r;
	}
	public Ranker createRanker(RANKER_TYPE type, List<RankList> samples, int[] features)
	{
		Ranker r = createRanker(type);
		r.set(samples, features);
		return r;
	}
	public Ranker loadRanker(String modelFile)
	{
		Ranker r = null;
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(modelFile), "ASCII"));
			String content = in.readLine();//read the first line to get the name of the ranking algorithm
			in.close();
			content = content.replace("## ", "").trim();
			System.out.println("Model:\t\t" + content);
			r = createRanker(map.get(content.toUpperCase()));
			r.load(modelFile);
		}
		catch(Exception ex)
		{
			System.out.println("Error in RankerFactory.load(): " + ex.toString());
		}
		return r;
	}
}
