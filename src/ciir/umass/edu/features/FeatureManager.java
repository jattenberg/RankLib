/*===============================================================================
 * Copyright (c) 2010-2012 University of Massachusetts.  All Rights Reserved.
 *
 * Use of the RankLib package is subject to the terms of the software license set 
 * forth in the LICENSE file included with this software, and also available at
 * http://people.cs.umass.edu/~vdang/ranklib_license.html
 *===============================================================================
 */

package ciir.umass.edu.features;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import ciir.umass.edu.learning.DataPoint;
import ciir.umass.edu.learning.RankList;

/**
 * @author vdang
 * 
 * Managing training samples and features.
 */
public class FeatureManager {

	private Hashtable<String, Integer> featureMap = new Hashtable<String, Integer>();
	private String[] fnames = null;
	
	public FeatureManager()
	{
		featureMap.put("IDF-SUM", 1);
		featureMap.put("IDF-STD", 2);
		featureMap.put("IDF-MMRATIO", 3);
		featureMap.put("IDF-MAX", 4);
		featureMap.put("IDF-MEAN", 5);
		featureMap.put("IDF-GEOMEAN", 6);
		featureMap.put("IDF-HARMEAN", 7);
		featureMap.put("IDF-STDMEANRATIO", 8);
		
		featureMap.put("SCQ-SUM", 9);
		featureMap.put("SCQ-STD", 10);
		featureMap.put("SCQ-MMRATIO", 11);
		featureMap.put("SCQ-MAX", 12);
		featureMap.put("SCQ-MEAN", 13);
		featureMap.put("SCQ-GEOMEAN", 14);
		featureMap.put("SCQ-HARMEAN", 15);
		featureMap.put("SCQ-STDMEANRATIO", 16);
		
		featureMap.put("ICTF-SUM", 17);
		featureMap.put("ICTF-STD", 18);
		featureMap.put("ICTF-MMRATIO", 19);
		featureMap.put("ICTF-MAX", 20);
		featureMap.put("ICTF-MEAN", 21);
		featureMap.put("ICTF-GEOMEAN", 22);
		featureMap.put("ICTF-HARMEAN", 23);
		featureMap.put("ICTF-STDMEANRATIO", 24);
		
		featureMap.put("SIM-CLARITY", 25);
		
		featureMap.put("QSCOPE", 26);
		featureMap.put("MI", 27);
		
		featureMap.put("CLARITY-5", 28);
		featureMap.put("CLARITY-10", 29);
		featureMap.put("CLARITY-50", 30);
		featureMap.put("CLARITY-100", 31);
		featureMap.put("CLARITY-500", 32);
		
		featureMap.put("QF-5", 33);
		featureMap.put("QF-10", 34);
		featureMap.put("QF-50", 35);
		featureMap.put("QF-100", 36);
		
		featureMap.put("WIG-5", 37);
		featureMap.put("WIG-10", 38);
		featureMap.put("WIG-50", 39);
		featureMap.put("WIG-100", 40);
		featureMap.put("WIG-500", 41);
	}
	
	/**
	 * Read feature data.
	 * @param fn Feature data file
	 * @param qids [output] List of objects found in the feature file.
	 * @return
	 */
	public List<RankList> read(String fn)
	{
		return read(fn, false, false);
	}
	public List<RankList> read(String fn, boolean letor, boolean mustHaveRelDoc)
	{
		List<RankList> samples = new ArrayList<RankList>();
		Hashtable<String, Integer> ht = new Hashtable<String, Integer>();
		int countRL = 0;
		int countEntries = 0;
		try {
			String content = "";
			BufferedReader in = new BufferedReader(
					new InputStreamReader(
							new FileInputStream(fn), "ASCII"));
			
			String lastID = "";
			//int countID = 0;
			boolean hasRel = false;
			RankList rl = new RankList();
			while((content = in.readLine()) != null)
			{
				content = content.trim();
				if(content.length() == 0)
					continue;
				if(content.indexOf("#")==0)
					continue;
				
				if(countEntries % 10000 == 0)
					System.out.print("\rReading feature file [" + fn + "]: " + countRL + "... ");
				
				DataPoint qp = new DataPoint(content);

				if(lastID.compareTo("")!=0 && lastID.compareTo(qp.getID())!=0)
				{
					if(!mustHaveRelDoc || hasRel)
						samples.add(rl);
					rl = new RankList();
					hasRel = false;
				}
				
				if(letor)
					if(qp.getLabel()==2.0f)
						qp.setLabel(3.0f);
				if(qp.getLabel() > 0)
					hasRel = true;
				lastID = qp.getID();
				rl.add(qp);
				countEntries++;
			}
			if(rl.size() > 0 && (!mustHaveRelDoc || hasRel))
				samples.add(rl);
			in.close();
			System.out.println("\rReading feature file [" + fn + "]... [Done.]            ");
			System.out.println("(" + samples.size() + " ranked lists, " + countEntries + " entries read)");
		}
		catch(Exception ex)
		{
			System.out.println("Error in FeatureManager::read(): " + ex.toString());
		}
		return samples;
	}
	public List<RankList> read2(String fn, boolean letor)
	{
		List<RankList> samples = new ArrayList<RankList>();
		Hashtable<String, Integer> ht = new Hashtable<String, Integer>();
		int countRL = 0;
		int countEntries = 0;
		try {
			String content = "";
			BufferedReader in = new BufferedReader(
					new InputStreamReader(
							new FileInputStream(fn), "ASCII"));
			
			//String lastID = "";
			//int countID = 0;
			while((content = in.readLine()) != null)
			{
				content = content.trim();
				if(content.length() == 0)
					continue;
				if(content.indexOf("#")==0)
					continue;
				
				if(countEntries % 10000 == 0)
					System.out.print("\rReading feature file [" + fn + "]: " + countRL + "... ");
				
				DataPoint qp = new DataPoint(content);
				RankList rl = null;
				if(ht.get(qp.getID()) == null)
				{
					//if(countRL >= 12000)
					//	break;
					rl = new RankList();
					//rl.setID(qp.getID());
					ht.put(qp.getID(), samples.size());
					samples.add(rl);
					countRL++;
				}
				else
					rl = samples.get(ht.get(qp.getID()).intValue());
				
				if(letor)
					if(qp.getLabel()==2.0f)
						qp.setLabel(3.0f);
				
				rl.add(qp);
				
				countEntries++;
			}
			in.close();
			System.out.println("\rReading feature file [" + fn + "]... [Done.]            ");
			System.out.println("(" + samples.size() + " ranked lists, " + countEntries + " entries read)");
		}
		catch(Exception ex)
		{
			System.out.println("Error in FeatureManager::read(): " + ex.toString());
		}
		return samples;

	}
	/**
	 * Get feature ID from its name. Note: it's 1-based.
	 * @param fname
	 * @return
	 */
	public int getFeatureID(String fname)
	{
		return featureMap.get(fname).intValue();
	}
	/**
	 * Get feature name from its ID. Note: it's 1-based too.
	 * @param fid
	 * @return
	 */
	public String getFeatureName(int fid)
	{
		return fnames[fid];
	}
	/**
	 * Get feature names from a description file
	 * @param fn
	 * @return
	 */
	public List<String> getFeatureNameFromFile(String fn)
	{
		List<String> fName = new ArrayList<String>();
		try {
			String content = "";
			BufferedReader in = new BufferedReader(
					new InputStreamReader(
							new FileInputStream(fn), "ASCII"));
			
			while((content = in.readLine()) != null)
			{
				content = content.trim();
				if(content.length() == 0)
					continue;
				if(content.indexOf("#")==0)
					continue;
				fName.add(content);
			}
			in.close();
		}
		catch(Exception ex)
		{
			System.out.println(ex.toString());
		}
		return fName;
	}
	/**
	 * Get feature id(s) from a description file
	 * @param fn
	 * @return
	 */
	public int[] getFeatureIDFromFile(String fn)
	{
		if(fn.compareTo("")==0)
			return null;
		List<String> l = getFeatureNameFromFile(fn);
		int[] fv = new int[l.size()];
		for(int i=0;i<l.size();i++)
			fv[i] = Integer.parseInt(l.get(i));
		return fv;
	}
}
