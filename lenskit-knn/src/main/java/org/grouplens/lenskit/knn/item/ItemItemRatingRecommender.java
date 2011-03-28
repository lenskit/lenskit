package org.grouplens.lenskit.knn.item;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import org.grouplens.lenskit.AbstractRatingRecommender;
import org.grouplens.lenskit.data.ScoredId;
import org.grouplens.lenskit.data.vector.SparseVector;
import org.grouplens.lenskit.util.IndexedItemScore;
import org.grouplens.lenskit.util.LongSortedArraySet;

import com.google.inject.Inject;

/**
 * Item-item rating recommender.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class ItemItemRatingRecommender extends AbstractRatingRecommender {
    protected final ItemItemRatingPredictor predictor;
    protected final ItemItemModel model;
    
    @Inject
    ItemItemRatingRecommender(ItemItemRatingPredictor predictor) {
        this.predictor = predictor;
        model = predictor.getModel();
    }
    
    LongSet getRecommendableItems(long user, SparseVector ratings) {
        if (model.hasBaseline()) {
            return model.getItemUniverse();
        } else {
            LongSet items = new LongOpenHashSet();
            LongIterator iter = ratings.keySet().iterator();
            while (iter.hasNext()) {
                final long item = iter.nextLong();
                for (IndexedItemScore n: model.getNeighbors(item)) {
                    items.add(model.getItem(n.getIndex()));
                }
            }
            return items;
        }
    }

    /* (non-Javadoc)
     * @see org.grouplens.lenskit.AbstractRatingRecommender#recommend(long, org.grouplens.lenskit.data.vector.SparseVector, int, it.unimi.dsi.fastutil.longs.LongSet, it.unimi.dsi.fastutil.longs.LongSet)
     */
    @Override
    protected List<ScoredId> recommend(long user, SparseVector ratings, int n,
            LongSet candidates, LongSet exclude) {
        if (candidates == null)
            candidates = getRecommendableItems(user, ratings);
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
