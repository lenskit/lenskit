package org.grouplens.lenskit;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.LongSet;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import org.grouplens.lenskit.data.ScoredId;
import org.grouplens.lenskit.data.vector.SparseVector;
import org.grouplens.lenskit.util.LongSortedArraySet;

import com.google.inject.Inject;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class PredictorBasedRatingRecommender extends AbstractRatingRecommender {
    protected final DiscoverableRatingPredictor predictor;
    
    /**
     * Construct a new recommender from a predictor.
     * @param predictor The predictor to use.
     */
    @Inject
    public PredictorBasedRatingRecommender(DiscoverableRatingPredictor predictor) {
        this.predictor = predictor;
    }
    
    @Override
    protected List<ScoredId> recommend(long user, SparseVector ratings, int n,
            LongSet candidates, LongSet exclude) {
        if (candidates == null)
            candidates = predictor.getPredictableItems(user, ratings);
        if (!exclude.isEmpty())
            candidates = LongSortedArraySet.setDifference(candidates, exclude);
        
        SparseVector predictions = predictor.predict(user, ratings, candidates);
        PriorityQueue<ScoredId> queue = new PriorityQueue<ScoredId>(predictions.size());
        for (Long2DoubleMap.Entry pred: predictions.fast()) {
            final double v = pred.getDoubleValue();
            if (!Double.isNaN(v)) {
                queue.add(new ScoredId(pred.getLongKey(), v));
            }
        }

        ArrayList<ScoredId> finalPredictions =
            new ArrayList<ScoredId>(n >= 0 ? n : queue.size());
        for (int i = 0; !queue.isEmpty() && (n < 0 || i < n); i++) {
            finalPredictions.add(queue.poll());
        }

        return finalPredictions;
    }

}
