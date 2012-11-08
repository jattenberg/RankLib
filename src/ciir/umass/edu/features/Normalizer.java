/*===============================================================================
 * Copyright (c) 2010-2012 University of Massachusetts.  All Rights Reserved.
 *
 * Use of the RankLib package is subject to the terms of the software license set 
 * forth in the LICENSE file included with this software, and also available at
 * http://people.cs.umass.edu/~vdang/ranklib_license.html
 *===============================================================================
 */

package ciir.umass.edu.features;

import ciir.umass.edu.learning.RankList;

/**
 * @author vdang
 *
 * Abstract class for feature normalization
 */
public interface Normalizer {
	public void normalize(RankList rl, int[] fids);
	public String name();
}
