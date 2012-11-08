/*===============================================================================
 * Copyright (c) 2010-2012 University of Massachusetts.  All Rights Reserved.
 *
 * Use of the RankLib package is subject to the terms of the software license set 
 * forth in the LICENSE file included with this software, and also available at
 * http://people.cs.umass.edu/~vdang/ranklib_license.html
 *===============================================================================
 */

package ciir.umass.edu.learning;

import java.util.ArrayList;
import java.util.List;

import ciir.umass.edu.utilities.Sorter;

/**
 * @author vdang
 * 
 * This class implement the list of objects (each of which is a DataPoint) to be ranked. 
 */
public class RankList {

	protected List<DataPoint> rl = null;
	
	public RankList()
	{
		rl = new ArrayList<DataPoint>();
	}
	public RankList(RankList rl)
	{
		this.rl = new ArrayList<DataPoint>();
		for(int i=0;i<rl.size();i++)
			this.rl.add(rl.get(i));
	}
	public RankList(RankList rl, int[] idx)
	{
		this.rl = new ArrayList<DataPoint>();
		for(int i=0;i<idx.length;i++)
			this.rl.add(rl.get(idx[i]));
	}
	
	public String getID()
	{
		return get(0).getID();
	}
	public int size()
	{
		return rl.size();
	}
	public DataPoint get(int k)
	{
		return rl.get(k);
	}
	
	public void add(DataPoint p)
	{
		rl.add(p);
	}
	public void remove(int k)
	{
		rl.remove(k);
	}
	
	public RankList getRanking(int fid)
	{
		double[] score = new double[rl.size()];
		for(int i=0;i<rl.size();i++)
			score[i] = rl.get(i).getFeatureValue(fid);
		int[] idx = Sorter.sort(score, false);
		return new RankList(this, idx);
	}
	public RankList getCorrectRanking()
	{
		double[] score = new double[rl.size()];
		for(int i=0;i<rl.size();i++)
			score[i] = rl.get(i).getLabel();
		int[] idx = Sorter.sort(score, false); 
		return new RankList(this, idx);
	}
	public RankList getWorstRanking()
	{
		double[] score = new double[rl.size()];
		for(int i=0;i<rl.size();i++)
			score[i] = rl.get(i).getLabel();
		int[] idx = Sorter.sort(score, true); 
		return new RankList(this, idx);
	}
}
