/*===============================================================================
 * Copyright (c) 2010-2012 University of Massachusetts.  All Rights Reserved.
 *
 * Use of the RankLib package is subject to the terms of the software license set 
 * forth in the LICENSE file included with this software, and also available at
 * http://people.cs.umass.edu/~vdang/ranklib_license.html
 *===============================================================================
 */

package ciir.umass.edu.learning;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SampleGenerator {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		SampleGenerator sg = new SampleGenerator(50);
		
		int[] count = sg.generate(2000, "data/toy/train.txt");
		
		System.out.print("#{");
		for(int i=0;i<count.length;i++)
			System.out.print(count[i] + (i==count.length-1?"":", "));
		System.out.println("}");
		
		System.out.println("#Validation");
		sg.generate(0.2F, count, "data/toy/vali.txt");
		
		System.out.println("#Test");
		sg.generate(1.0F, count, "data/toy/test.txt");
	}

	private int nFeature = 30;
	//private int degree = 3;
	//private float[][] coefficients = null;
	//private float constant = -1;
	int[] permuIndex1 = null;
	int[] permuIndex2 = null;
	int[] permuIndex3 = null;
	float[] constants = null;
	
	private Random rnd = new Random();
	
	public SampleGenerator(int nFeature)
	{
		this.nFeature = nFeature;
		/*coefficients = new float[nFeature][];
		for(int i=0;i<nFeature;i++)
		{
			coefficients[i] = new float[degree];
			for(int j=0;j<degree;j++)
			{
				int sign = (rnd.nextInt(2)==0)?1:-1;
				coefficients[i][j] = rnd.nextInt(10) * sign; 
			}
		}
		constant = rnd.nextInt(10) * ((rnd.nextInt(2)==0)?1:-1);*/
		
		permuIndex1 = getPermutationIndex();
		permuIndex2 = getPermutationIndex();
		permuIndex3 = getPermutationIndex();
		constants = randomVector();
	}
	private int[] getPermutationIndex()
	{
		int[] pi = new int[nFeature];
		List<Integer> l = new ArrayList<Integer>();
		for(int i=0;i<nFeature;i++)
			l.add(i);
		Collections.shuffle(l);
		for(int i=0;i<nFeature;i++)
			pi[i] = l.get(i);
		return pi;
	}
	
	public int[] generate(int nSamples, String outputFile)
	{
		int[] count = null;
		try {
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), "ASCII"));
			
			int[][] dists = new int[5][];
			
			dists[0] = new int[]{10, 2, 1, 2};
			dists[1] = new int[]{12, 1, 1, 1};
			dists[2] = new int[]{13, 0, 0, 2};
			dists[3] = new int[]{12, 0, 1, 2};
			dists[4] = new int[]{13, 2, 0, 0};
			float[] prob = new float[]{0, 0.2F, 0.4F, 0.6F, 0.8F};
			
			count = new int[dists.length];
			for(int i=0;i<dists.length;i++)
				count[i] = 0;
			
			for(int i=0;i<nSamples;i++)
			{
				float v = rnd.nextFloat();
				int[] dist = null;
				for(int j=prob.length-1;j>=0;j--)
				{
					if(v >= prob[j])
					{
						dist = dists[j];
						count[j]++;
						break;
					}
				}
				generate((i+1)+"", dist, out);
			}
			out.close();
		}
		catch(Exception ex)
		{
			
		}
		return count;
	}
	public void generate(float ratio, int[] count, String outputFile)
	{
		try {
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), "ASCII"));
			int[][] dists = new int[5][];
			
			dists[0] = new int[]{10, 2, 1, 2};
			dists[1] = new int[]{12, 1, 1, 1};
			dists[2] = new int[]{13, 0, 0, 2};
			dists[3] = new int[]{12, 0, 1, 2};
			dists[4] = new int[]{13, 2, 0, 0};
			
			int[] taken = new int[count.length];
			for(int i=0;i<taken.length;i++)
			{
				taken[i] = 0;
				count[i] = (int) (((float)count[i])*ratio);
			}
			
			int k=0;
			while(true)
			{
				int j = rnd.nextInt(5);
				if(taken[j] <= count[j])
				{
					generate((k+1)+"", dists[j], out);
					taken[j]++;
					k++;
				}
				
				boolean flag = true;			
				for(int i=0;i<taken.length&&flag;i++)
					if(taken[i] < count[i])
						flag = false;
				if(flag)
					break;
			}
			out.close();
		}
		catch(Exception ex)
		{
			
		}
	}
	
	public void generate(String id, int[] dist, BufferedWriter out) throws Exception
	{
		//float[] t = new float[]{-1000000, 5, 10, 15};
		float[] t = new float[]{-1000000, -1, 1, 2};
		int[] taken = new int[]{0, 0, 0, 0};
		
		while(true)
		{
			float[] f = randomVector();
			double v = eval(f);
			int label = 0;
			for(int j=t.length-1;j>=0;j--)
			{
				if(v > t[j])
				{
					label = j;
					taken[j]++;
					if(taken[j] <= dist[j])
						write(id, f, label, out);
					break;
				}
			}
			boolean flag = true;
			for(int i=0;i<taken.length&&flag;i++)
				if(taken[i] < dist[i])
					flag = false;
			if(flag)
				break;
		}
	}
	public float[] randomVector()
	{
		float[] f = new float[nFeature];
		for(int i=0;i<nFeature;i++)
			f[i] = rnd.nextFloat() * ((rnd.nextInt(2)==0)?1:-1);
		return f;
	}
	public double eval(float[] features)
	{
		double val = 0.0;
		/*for(int i=0;i<nFeature;i++)
		{
			for(int j=0;j<coefficients[i].length;j++)
			{
				val += coefficients[i][j] * Math.pow(features[i], (j+1));				
			}
		}
		val += constant;*/
		
		for(int i=0;i<nFeature;i++)
		{
			double v0 = features[i] * constants[i];
			double v1 = features[i] * features[permuIndex1[i]];
			double v2 = features[i] * features[permuIndex2[i]] * features[permuIndex3[i]];
			val += v0 + v1 + v2;
		}
		val /= 3;
		return val;
	}
	public void write(String id, float[] features, int label, BufferedWriter out) throws Exception
	{
		out.write(label + " " + "qid:" + id + " ");
		for(int i=0;i<features.length;i++)
			out.write((i+1) + ":" + features[i] + (i==features.length-1 ? "" : " "));
		out.newLine();
	}
}
