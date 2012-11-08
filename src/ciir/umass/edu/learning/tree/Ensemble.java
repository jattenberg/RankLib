/*===============================================================================
 * Copyright (c) 2010-2012 University of Massachusetts.  All Rights Reserved.
 *
 * Use of the RankLib package is subject to the terms of the software license set 
 * forth in the LICENSE file included with this software, and also available at
 * http://people.cs.umass.edu/~vdang/ranklib_license.html
 *===============================================================================
 */

package ciir.umass.edu.learning.tree;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ciir.umass.edu.learning.DataPoint;

/**
 * @author vdang
 */
public class Ensemble {
	protected List<RegressionTree> trees = null;
	protected List<Float> weights = null;
	
	public Ensemble()
	{
		trees = new ArrayList<RegressionTree>();
		weights = new ArrayList<Float>();
	}
	public Ensemble(Ensemble e)
	{
		trees = new ArrayList<RegressionTree>();
		weights = new ArrayList<Float>();
		trees.addAll(e.trees);
		weights.addAll(e.weights);
	}
	public Ensemble(String xmlRep)
	{
		try {
			trees = new ArrayList<RegressionTree>();
			weights = new ArrayList<Float>();
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			byte[] xmlDATA = xmlRep.getBytes();
			ByteArrayInputStream in = new ByteArrayInputStream(xmlDATA);
			Document doc = dBuilder.parse(in);
			NodeList nl = doc.getElementsByTagName("tree");
			for(int i=0;i<nl.getLength();i++)
			{
				Node n = nl.item(i);//each node corresponds to a "tree" (tag)
				//create a regression tree from this node
				Split root = create(n.getFirstChild());
				//get the weight for this tree
				float weight = Float.parseFloat(n.getAttributes().getNamedItem("weight").getNodeValue().toString());
				//add it to the ensemble
				trees.add(new RegressionTree(root));
				weights.add(weight);
			}
		}
		catch(Exception ex)
		{
			System.out.println("Error in Emsemble(xmlRepresentation): " + ex.toString());
		}

	}
	
	public void add(RegressionTree tree, float weight)
	{
		trees.add(tree);
		weights.add(weight);
	}
	public RegressionTree getTree(int k)
	{
		return trees.get(k);
	}
	public float getWeight(int k)
	{
		return weights.get(k);
	}
	public double variance()
	{
		double var = 0;
		for(int i=0;i<trees.size();i++)
			var += trees.get(i).variance();
		return var;
	}
	public void remove(int k)
	{
		trees.remove(k);
		weights.remove(k);
	}
	public int treeCount()
	{
		return trees.size();
	}
	public int leafCount()
	{
		int count = 0;
		for(int i=0;i<trees.size();i++)
			count += trees.get(i).leaves().size();
		return count;
	}
	public float eval(DataPoint dp)
	{
		float s = 0;
		for(int i=0;i<trees.size();i++)
			s += trees.get(i).eval(dp) * weights.get(i);
		return s;
	}
	public String toString()
	{
		String strRep = "<ensemble>" + "\n";
		for(int i=0;i<trees.size();i++)
		{
			strRep += "\t<tree id=\"" + (i+1) + "\" weight=\"" + weights.get(i) + "\">" + "\n";
			strRep += trees.get(i).toString("\t\t");
			strRep += "\t</tree>" + "\n";
		}
		strRep += "</ensemble>" + "\n";
		return strRep;
	}
	
	/**
	 * Each input node @n corersponds to a <split> tag in the model file.
	 * @param n
	 * @return
	 */
	private Split create(Node n)
	{
		Split s = null;
		if(n.getFirstChild().getNodeName().compareToIgnoreCase("feature") == 0)//this is a split
		{
			NodeList nl = n.getChildNodes();
			int fid = Integer.parseInt(nl.item(0).getFirstChild().getNodeValue().toString().trim());//<feature>
			float threshold = Float.parseFloat(nl.item(1).getFirstChild().getNodeValue().toString().trim());//<threshold>
			s = new Split(fid, threshold, 0);
			s.setLeft(create(nl.item(2)));
			s.setRight(create(nl.item(3)));
		}
		else//this is a stump
		{
			float output = Float.parseFloat(n.getFirstChild().getFirstChild().getNodeValue().toString().trim());
			s = new Split();
			s.setOutput(output);
		}
		return s;
	}
}
