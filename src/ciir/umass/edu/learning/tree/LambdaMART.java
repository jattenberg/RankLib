/*===============================================================================
 * Copyright (c) 2010-2012 University of Massachusetts.  All Rights Reserved.
 *
 * Use of the RankLib package is subject to the terms of the software license set 
 * forth in the LICENSE file included with this software, and also available at
 * http://people.cs.umass.edu/~vdang/ranklib_license.html
 *===============================================================================
 */

package ciir.umass.edu.learning.tree;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ciir.umass.edu.learning.CoorAscent;
import ciir.umass.edu.learning.DataPoint;
import ciir.umass.edu.learning.Evaluator;
import ciir.umass.edu.learning.RANKER_TYPE;
import ciir.umass.edu.learning.RankList;
import ciir.umass.edu.learning.Ranker;
import ciir.umass.edu.learning.boosting.AdaRank;
import ciir.umass.edu.learning.boosting.RankBoost;
import ciir.umass.edu.learning.neuralnet.LambdaRank;
import ciir.umass.edu.learning.neuralnet.ListNet;
import ciir.umass.edu.learning.neuralnet.Neuron;
import ciir.umass.edu.learning.neuralnet.RankNet;
import ciir.umass.edu.metric.METRIC;
import ciir.umass.edu.utilities.MyThreadPool;
import ciir.umass.edu.utilities.SimpleMath;
import ciir.umass.edu.utilities.MergeSorter;

/**
 * @author vdang
 *
 *  This class implements LambdaMART.
 *  Q. Wu, C.J.C. Burges, K. Svore and J. Gao. Adapting Boosting for Information Retrieval Measures. 
 *  Journal of Information Retrieval, 2007.
 */
public class LambdaMART extends Ranker {
	//Parameters
	public static int nTrees = 1000;//the number of trees
	public static float learningRate = 0.1F;//or shrinkage
	public static int nThreshold = 256;
	public static int nRoundToStopEarly = 100;//If no performance gain on the *VALIDATION* data is observed in #rounds, stop the training process right away. 
	public static int nTreeLeaves = 10;
	public static int minLeafSupport = 1;
	
	//Local variables
	protected float[][] thresholds = null;
	protected Ensemble ensemble = null;
	protected float[] modelScores = null;//on training data
	
	protected float[][] modelScoresOnValidation = null;
	protected int bestModelOnValidation = Integer.MAX_VALUE-2;
	
	//Training instances prepared for MART
	protected DataPoint[] martSamples = null;//Need initializing only once
	protected int[][] sortedIdx = null;//sorted list of samples in @martSamples by each feature -- Need initializing only once 
	protected FeatureHistogram hist = null;
	protected float[] pseudoResponses = null;//different for each iteration
	
	public static void main(String args[])
	{
		MyThreadPool.init(Runtime.getRuntime().availableProcessors());
		CoorAscent.nRestart = 1;
		
		Evaluator ev = new Evaluator(RANKER_TYPE.RANDOM_FOREST, METRIC.MAP, 10, METRIC.NDCG, 1);
		AdaRank.trainWithEnqueue = false;
		RankNet.nHiddenLayer = 1;
		RankNet.nIteration = 300;
		RankNet.nHiddenNodePerLayer = 5;
		//RankNet.learningRate = 0.0005;
		RankNet.learningRate = 0.0001;
		
		RankBoost.nIteration = 100;
		RankBoost.nThreshold = 200;
		
		LambdaMART.nThreshold = 2000;
		RFRanker.nBag = 100;
		//ev.modelFile = "RandomForest.model";
		//ev.evaluate("data/toy/train.txt", "data/toy/vali.txt", "data/toy/test.txt", "");
		
		//ev.evaluate("data/Fold1/train.txt", "data/Fold1/test.txt", "");
		//ev.evaluate("data/Fold1/train.txt", "data/Fold1/test.txt", "");
		//ev.evaluate("data/MQ2008/Fold1/train-vali.txt", "", "data/MQ2008/Fold1/test.txt", "");
		//ev.evaluate("data/MQ2008/Fold2/train-vali.txt", "", "data/MQ2008/Fold2/test.txt", "");
		
		//ev.evaluate("data/MQ2008/Fold1/train.txt", "data/MQ2008/Fold1/vali.txt", "data/MQ2008/Fold1/test.txt", "");
		//ev.evaluate("data/MQ2008/Fold2/train.txt", "data/MQ2008/Fold2/vali.txt", "data/MQ2008/Fold2/test.txt", "");
		//ev.evaluate("data/MQ2008/Fold3/train.txt", "data/MQ2008/Fold3/vali.txt", "data/MQ2008/Fold3/test.txt", "");
		//ev.evaluate("data/MQ2008/Fold4/train.txt", "data/MQ2008/Fold4/vali.txt", "data/MQ2008/Fold4/test.txt", "");
		//ev.evaluate("data/MQ2008/Fold5/train.txt", "data/MQ2008/Fold5/vali.txt", "data/MQ2008/Fold5/test.txt", "");
		//ev.evaluate("data/Fold4/train.txt", "data/Fold4/vali.txt", "data/Fold4/test.txt", "");
		//ev.evaluate("data/Fold5/train.txt", "data/Fold5/vali.txt", "data/Fold5/test.txt", "");
		
		//ev.evaluate("data/toy/train.txt", "data/toy/test.txt", "");
		//ev.evaluate("data/MQ2007/Fold1/train.txt", "data/MQ2007/Fold1/test.txt", "");
		//ev.evaluate("data/MQ2007/Fold1/train.txt", 0.8, "data/MQ2007/Fold1/test.txt", "");
		
		//ev.evaluate("data/MQ2007/Fold1/train.txt", "data/MQ2007/Fold1/vali.txt", "data/MQ2007/Fold1/test.txt", "");
		ev.evaluate("data/MQ2007/Fold2/train.txt", "data/MQ2007/Fold2/vali.txt", "data/MQ2007/Fold2/test.txt", "");
		//ev.evaluate("data/MQ2007/Fold3/train.txt", "data/MQ2007/Fold3/vali.txt", "data/MQ2007/Fold3/test.txt", "");
		//ev.evaluate("data/MQ2007/Fold4/train.txt", "data/MQ2007/Fold4/vali.txt", "data/MQ2007/Fold4/test.txt", "");
		//ev.evaluate("data/MQ2007/Fold5/train.txt", "data/MQ2007/Fold5/vali.txt", "data/MQ2007/Fold5/test.txt", "");
		//ev.evaluate("data/MSLR-WEB10K/Fold1/train.txt", "data/MSLR-WEB10K/Fold1/vali.txt", "data/MSLR-WEB10K/Fold1/test.txt", "");
		
		//ev.test("RandomForest.model", "data/MQ2008/Fold1/test.txt");
		//ev.rank("RB.model", "data/MQ2008/Fold1/test.txt");
		MyThreadPool.getInstance().shutdown();
	}
	
	public LambdaMART()
	{		
	}
	public LambdaMART(List<RankList> samples, int[] features)
	{
		super(samples, features);
	}
	
	public void init()
	{
		PRINT("Initializing... ");		
		//initialize samples for MART
		int dpCount = 0;
		for(int i=0;i<samples.size();i++)
		{
			RankList rl = samples.get(i);
			dpCount += rl.size();
		}
		int current = 0;
		martSamples = new DataPoint[dpCount];
		modelScores = new float[dpCount];
		pseudoResponses = new float[dpCount];
		for(int i=0;i<samples.size();i++)
		{
			RankList rl = samples.get(i);
			for(int j=0;j<rl.size();j++)
			{
				martSamples[current+j] = rl.get(j);
				modelScores[current+j] = 0.0F;
				pseudoResponses[current+j] = 0.0F;
			}
			current += rl.size();
		}			
		
		//sort (MART) samples by each feature so that we can quickly retrieve a sorted list of samples by any feature later on.
		sortedIdx = new int[features.length][];
		MyThreadPool p = MyThreadPool.getInstance();
		if(p.size() == 1)//single-thread
			sortSamplesByFeature(0, features.length-1);
		else//multi-thread
		{
			int[] partition = p.partition(features.length);
			for(int i=0;i<partition.length-1;i++)
				p.execute(new SortWorker(this, partition[i], partition[i+1]-1));
			p.await();
		}
		
		//Create a table of candidate thresholds (for each feature). Later on, we will select the best tree split from these candidates 
		thresholds = new float[features.length][];
		for(int f=0;f<features.length;f++)
		{
			//For this feature, keep track of the list of unique values and the max/min 
			List<Float> values = new ArrayList<Float>();
			float fmax = Float.NEGATIVE_INFINITY;
			float fmin = Float.MAX_VALUE;
			for(int i=0;i<martSamples.length;i++)
			{
				int k = sortedIdx[f][i];//get samples sorted with respect to this feature
				float fv = martSamples[k].getFeatureValue(features[f]);
				values.add(fv);
				if(fmax < fv)
					fmax = fv;
				if(fmin > fv)
					fmin = fv;
				//skip all samples with the same feature value
				int j=i+1;
				while(j < martSamples.length)
				{
					if(martSamples[sortedIdx[f][j]].getFeatureValue(features[f]) > fv)
						break;
					j++;
				}
				i = j-1;//[i, j] gives the range of samples with the same feature value
			}
			if(values.size() <= nThreshold || nThreshold == -1)
			{
				thresholds[f] = new float[values.size()+1];
				for(int i=0;i<values.size();i++)
					thresholds[f][i] = values.get(i);
				thresholds[f][values.size()] = Float.MAX_VALUE;
			}
			else
			{
				float step = (Math.abs(fmax - fmin))/nThreshold;
				thresholds[f] = new float[nThreshold+1];
				thresholds[f][0] = fmin;
				for(int j=1;j<nThreshold;j++)
					thresholds[f][j] = thresholds[f][j-1] + step;
				thresholds[f][nThreshold] = Float.MAX_VALUE;
			}
		}
		
		if(validationSamples != null)
		{
			modelScoresOnValidation = new float[validationSamples.size()][];
			for(int i=0;i<validationSamples.size();i++)
			{
				modelScoresOnValidation[i] = new float[validationSamples.get(i).size()];
				Arrays.fill(modelScoresOnValidation[i], 0);
			}
		}
		
		//compute the feature histogram (this is used to speed up the procedure of finding the best tree split later on)
		hist = new FeatureHistogram();
		hist.construct(martSamples, pseudoResponses, sortedIdx, features, thresholds);
		//we no longer need the sorted indexes of samples
		sortedIdx = null;
		
		System.gc();
		PRINTLN("[Done]");
	}
	public void learn()
	{
		ensemble = new Ensemble();
		
		PRINTLN("---------------------------------");
		PRINTLN("Training starts...");
		PRINTLN("---------------------------------");
		PRINTLN(new int[]{7, 9, 9}, new String[]{"#iter", scorer.name()+"-T", scorer.name()+"-V"});
		PRINTLN("---------------------------------");		
		
		//Start the gradient boosting process
		for(int m=0; m<nTrees; m++)
		{
			PRINT(new int[]{7}, new String[]{(m+1)+""});
			
			//Compute lambdas (which act as the "pseudo responses")
			//Create training instances for MART:
			//  - Each document is a training sample
			//	- The lambda for this document serves as its training label
			computePseudoResponses();
			
			//update the histogram with these training labels (the feature histogram will be used to find the best tree split)
			hist.update(pseudoResponses);
			
			//Fit a regression tree			
			RegressionTree rt = new RegressionTree(nTreeLeaves, martSamples, pseudoResponses, hist, minLeafSupport);
			rt.fit();
			
			//Add this tree to the ensemble (our model)
			ensemble.add(rt, learningRate);
			
			//update the outputs of the tree (with gamma computed using the Newton-Raphson method) 
			updateTreeOutput(rt);
			
			rt.clearSamples();//clear references to data that is longer used
			
			//beg the garbage collector to work...
			System.gc();
			
			//Update the model's outputs on all training samples
			for(int i=0;i<modelScores.length;i++)
				modelScores[i] += learningRate * rt.eval(martSamples[i]);

			//Evaluate the current model
			scoreOnTrainingData = computeModelScoreOnTraining();
			//**** NOTE ****
			//The above function to evaluate the current model on the training data is equivalent to a single call:
			//
			//		scoreOnTrainingData = scorer.score(rank(samples);
			//
			//However, this function is more efficient since it uses the cached outputs of the model (as opposed to re-evaluate the model 
			//on the entire training set).
			
			PRINT(new int[]{9}, new String[]{SimpleMath.round(scoreOnTrainingData, 4) + ""});			
			
			//Evaluate the current model on the validation data (if available)
			if(validationSamples != null)
			{
				//Update the model's scores on all validation samples
				for(int i=0;i<modelScoresOnValidation.length;i++)
					for(int j=0;j<modelScoresOnValidation[i].length;j++)
						modelScoresOnValidation[i][j] += learningRate * rt.eval(validationSamples.get(i).get(j));
				
				//again, equivalent to scoreOnValidation=scorer.score(rank(validationSamples)), but more efficient since we use the cached models' outputs
				double score = computeModelScoreOnValidation();				
				PRINT(new int[]{9}, new String[]{SimpleMath.round(score, 4) + ""});
				if(score > bestScoreOnValidationData)
				{
					bestScoreOnValidationData = score;
					bestModelOnValidation = ensemble.treeCount()-1;
				}
			}
			
			//long end = System.nanoTime();
			//System.out.print(" [" + (double)(end-start)/1e9 + "] ");
			PRINTLN("");
			
			//Should we stop early?
			if(m - bestModelOnValidation > nRoundToStopEarly)
				break;
		}
		
		//Rollback to the best model observed on the validation data
		while(ensemble.treeCount() > bestModelOnValidation+1)
			ensemble.remove(ensemble.treeCount()-1);
		
		//Finishing up
		scoreOnTrainingData = scorer.score(rank(samples));
		PRINTLN("---------------------------------");
		PRINTLN("Finished sucessfully.");
		PRINTLN(scorer.name() + " on training data: " + SimpleMath.round(scoreOnTrainingData, 4));
		if(validationSamples != null)
		{
			bestScoreOnValidationData = scorer.score(rank(validationSamples));
			PRINTLN(scorer.name() + " on validation data: " + SimpleMath.round(bestScoreOnValidationData, 4));
		}
		PRINTLN("---------------------------------");
	}
	public double eval(DataPoint dp)
	{
		return ensemble.eval(dp);
	}	
	public Ranker clone()
	{
		return new LambdaMART();
	}
	public String toString()
	{
		return ensemble.toString();
	}
	public String model()
	{
		String output = "## " + name() + "\n";
		output += "## No. of trees = " + nTrees + "\n";
		output += "## No. of leaves = " + nTreeLeaves + "\n";
		output += "## No. of threshold candidates = " + nThreshold + "\n";
		output += "## Learning rate = " + learningRate + "\n";
		output += "## Stop early = " + nRoundToStopEarly + "\n";
		output += "\n";
		output += toString();
		return output;
	}
	public void load(String fn)
	{
		try {
			String content = "";
			String model = "";
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(fn), "ASCII"));
			while((content = in.readLine()) != null)
			{
				content = content.trim();
				if(content.length() == 0)
					continue;
				if(content.indexOf("##")==0)
					continue;
				//actual model component
				model += content;
			}
			in.close();
			//load the ensemble
			ensemble = new Ensemble(model);
		}
		catch(Exception ex)
		{
			System.out.println("Error in LambdaMART::load(): " + ex.toString());
		}
	}
	public void printParameters()
	{
		PRINTLN("No. of trees: " + nTrees);
		PRINTLN("No. of leaves: " + nTreeLeaves);
		PRINTLN("No. of threshold candidates: " + nThreshold);
		PRINTLN("Learning rate: " + learningRate);
		PRINTLN("Stop early: " + nRoundToStopEarly + " rounds without performance gain on validation data");		
	}	
	public String name()
	{
		return "LambdaMART";
	}
	public Ensemble getEnsemble()
	{
		return ensemble;
	}
	
	protected void computePseudoResponses()
	{
		MyThreadPool p = MyThreadPool.getInstance();
		if(p.size() == 1)//single-thread
			computePseudoResponses(0, samples.size()-1, 0);
		
		//multi-threading
		List<LambdaComputationWorker> workers = new ArrayList<LambdaMART.LambdaComputationWorker>();
		//divide the entire dataset into chunks of equal size for each worker thread
		int chunk = (samples.size()-1)/p.size() + 1;
		int current = 0;
		for(int i=0;i<p.size();i++)
		{
			int start = i*chunk;
			int end = start + chunk - 1;
			if(end >= samples.size())
				end = samples.size()-1;
			
			//execute the worker
			LambdaComputationWorker wk = new LambdaComputationWorker(this, start, end, current); 
			workers.add(wk);//keep it so we can get back results from it later on
			p.execute(wk);
			
			if(i < chunk-1)
				for(int j=start; j<=end;j++)
					current += samples.get(j).size();
		}
		
		//wait for all workers to complete before we move on to the next stage
		p.await();
	}
	protected void computePseudoResponses(int start, int end, int current)
	{
		//compute the lambda for each document (aka "pseudo response")
		for(int i=start;i<=end;i++)
		{
			RankList r = samples.get(i);				
			float[][] changes = computeMetricChange(i, current);			
			double[] lambdas = new double[r.size()];
			double[] weights = new double[r.size()];
			Arrays.fill(lambdas, 0);
			Arrays.fill(weights, 0);
			
			for(int j=0;j<r.size();j++)
			{
				DataPoint p1 = r.get(j);
				for(int k=0;k<r.size();k++)
				{
					if(j == k)
						continue;
					
					DataPoint p2 = r.get(k);
					double deltaNDCG = Math.abs(changes[j][k]);
					
					if(p1.getLabel() > p2.getLabel())
					{
						double rho = 1.0 / (1 + Math.exp(modelScores[current+j] - modelScores[current+k]));
						double lambda = rho * deltaNDCG;
						lambdas[j] += lambda;
						lambdas[k] -= lambda;
						double delta = rho * (1.0 - rho) * deltaNDCG;
						weights[j] += delta;
						weights[k] += delta;
					}
				}
			}
			
			for(int j=0;j<r.size();j++)
			{
				pseudoResponses[current+j] = (float)lambdas[j];
				r.get(j).setCached(weights[j]);
			}
			current += r.size();
		}
	}
	protected void updateTreeOutput(RegressionTree rt)
	{
		List<Split> leaves = rt.leaves();
		for(int i=0;i<leaves.size();i++)
		{
			float s1 = 0.0F;
			float s2 = 0.0F;
			Split s = leaves.get(i);
			int[] idx = s.getSamples();
			for(int j=0;j<idx.length;j++)
			{
				int k = idx[j];
				s1 += pseudoResponses[k];
				s2 += martSamples[k].getCached();
			}
			s.setOutput(s1/s2);
		}
	}
	protected int[] sortSamplesByFeature(DataPoint[] samples, int fid)
	{
		double[] score = new double[samples.length];
		for(int i=0;i<samples.length;i++)
			score[i] = samples[i].getFeatureValue(fid);
		int[] idx = MergeSorter.sort(score, true); 
		return idx;
	}
	/**
	 * This function is equivalent to the inherited function rank(...), but it uses the cached model's outputs instead of computing them from scratch.
	 * @param rankListIndex
	 * @param current
	 * @return
	 */
	protected RankList rank(int rankListIndex, int current)
	{
		RankList orig = samples.get(rankListIndex);	
		float[] scores = new float[orig.size()];
		for(int i=0;i<scores.length;i++)
			scores[i] = modelScores[current+i];
		int[] idx = MergeSorter.sort(scores, false);
		return new RankList(orig, idx);
	}
	protected float computeModelScoreOnTraining() 
	{
		float s = 0;
		int current = 0;		
		for(int i=0;i<samples.size();i++)
		{
			s += scorer.score(rank(i, current));
			current += samples.get(i).size();
		}
		s = s / samples.size();
		return s;
	}
	protected float computeModelScoreOnValidation() 
	{
		float score = 0;
		for(int i=0;i<validationSamples.size();i++)
		{
			int[] idx = MergeSorter.sort(modelScoresOnValidation[i], false);
			score += scorer.score(new RankList(validationSamples.get(i), idx));
		}
		return score/validationSamples.size();
	}
	/**
	 * Compute the change (in whatever specified metric) for swapping each pair of documents in a rank list. 
	 * @param rankListIndex
	 * @param current
	 * @return
	 */
	protected float[][] computeMetricChange(int rankListIndex, int current)
	{
		RankList orig = samples.get(rankListIndex);		
		float[] scores = new float[orig.size()];
		for(int i=0;i<scores.length;i++)
			scores[i] = modelScores[current+i];
		
		int[] idx = MergeSorter.sort(scores, false);
		RankList rl = new RankList(orig, idx);
		float[][] changes = new float[orig.size()][];
		for(int i=0;i<changes.length;i++)
			changes[i] = new float[orig.size()];
		
		double[][] c = scorer.swapChange(rl);
		for(int i=0;i<changes.length;i++)
		{
			for(int j=0;j<changes.length;j++)
				changes[idx[i]][idx[j]] = (float)c[i][j];
		}
		return changes;
	}
	protected void sortSamplesByFeature(int fStart, int fEnd)
	{
		for(int i=fStart;i<=fEnd; i++)
			sortedIdx[i] = sortSamplesByFeature(martSamples, features[i]);
	}

	//For multi-threading processing
	class SortWorker implements Runnable {
		LambdaMART ranker = null;
		int start = -1;
		int end = -1;
		SortWorker(LambdaMART ranker, int start, int end)
		{
			this.ranker = ranker;
			this.start = start;
			this.end = end;
		}		
		public void run()
		{
			//System.out.println("Thread [" + start + "/" + end + "]");
			ranker.sortSamplesByFeature(start, end);
		}
	}
	class LambdaComputationWorker implements Runnable {
		LambdaMART ranker = null;
		int rlStart = -1;
		int rlEnd = -1;
		int martStart = -1;
		LambdaComputationWorker(LambdaMART ranker, int rlStart, int rlEnd, int martStart)
		{
			this.ranker = ranker;
			this.rlStart = rlStart;
			this.rlEnd = rlEnd;
			this.martStart = martStart;
		}		
		public void run()
		{
			ranker.computePseudoResponses(rlStart, rlEnd, martStart);
		}
	}
}
