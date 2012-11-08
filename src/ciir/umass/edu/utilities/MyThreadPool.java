/*===============================================================================
 * Copyright (c) 2010-2012 University of Massachusetts.  All Rights Reserved.
 *
 * Use of the RankLib package is subject to the terms of the software license set 
 * forth in the LICENSE file included with this software, and also available at
 * http://people.cs.umass.edu/~vdang/ranklib_license.html
 *===============================================================================
 */

package ciir.umass.edu.utilities;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 
 * @author vdang
 *
 */
public class MyThreadPool extends ThreadPoolExecutor {

	private final Semaphore semaphore;
	private int size = 0;
	
	private MyThreadPool(int size)
	{
		super(size, size, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
		semaphore = new Semaphore(size, true);
		this.size = size;
	}
	
	private static MyThreadPool singleton = null;
	public static MyThreadPool getInstance()
	{
		return singleton;
	}
	
	public static void init(int poolSize)
	{
		singleton = new MyThreadPool(poolSize);
	}
	public int size()
	{
		return size;
	}
	public void await()
	{
		for(int i=0;i<size;i++)
		{
			try {
				semaphore.acquire();				
			}
			catch(Exception ex)
			{
				System.out.println("Error in MyThreadPool.await(): " + ex.toString());
				System.exit(1);
			}
		}
		for(int i=0;i<size;i++)
			semaphore.release();
	}
	public int[] partition(int listSize)
	{
		int chunk = (listSize-1)/size + 1;
		int[] partition = new int[size+1];
		partition[0] = 0;
		for(int i=0;i<size;i++)
		{
			int end = (i+1)*chunk;
			if(end > listSize)
				end = listSize;
			partition[i+1] = end;
		}
		return partition;
	}
	
	public void execute(Runnable task) 
	{
		try {
			semaphore.acquire();
			super.execute(task);
		}
		catch(Exception ex)
		{
			System.out.println("Error in MyThreadPool.execute(): " + ex.toString());
			System.exit(1);
		}
	}
	protected void afterExecute(Runnable r, Throwable t)
	{
		super.afterExecute(r, t);
		semaphore.release();
	}
}
