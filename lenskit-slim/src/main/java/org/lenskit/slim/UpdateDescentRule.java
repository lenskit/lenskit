package org.lenskit.slim;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import java.util.Map;


/**
 * not used. wonder if abstract class is better than interface for different updates??
 */

public interface UpdateDescentRule {
    /**
     * learning process
     * @param labels label vector
     * @param trainingMatrix observations matrix row: user ratings for different items, column: item ratings of different users
     * @return weight vector
     */
    Long2DoubleMap fit(Long2DoubleMap labels, Map<Long, Long2DoubleMap> trainingMatrix);
}
