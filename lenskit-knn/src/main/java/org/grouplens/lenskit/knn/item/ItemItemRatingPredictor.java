package org.grouplens.lenskit.knn.item;

import static java.lang.Math.abs;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSortedSet;

import java.util.Collection;

import org.grouplens.lenskit.DiscoverableRatingPredictor;
import org.grouplens.lenskit.data.ScoredId;
import org.grouplens.lenskit.data.vector.MutableSparseVector;
import org.grouplens.lenskit.data.vector.SparseVector;
import org.grouplens.lenskit.util.IndexedItemScore;
import org.grouplens.lenskit.util.LongSortedArraySet;

import com.google.inject.Inject;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class ItemItemRatingPredictor implements DiscoverableRatingPredictor {
    protected final ItemItemModel model;
    
    @Inject
    ItemItemRatingPredictor(ItemItemModel model) {
        this.model = model;
    }
    
    public ItemItemModel getModel() {
        return model;
    }
    
    @Override
    public LongSet getPredictableItems(long user, SparseVector ratings) {
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
    
    @Override
    public ScoredId predict(long user, SparseVector ratings, long item) {
        MutableSparseVector normed = MutableSparseVector.copy(ratings);
        model.subtractBaseline(user, ratings, normed);
        double sum = 0;
        double totalWeight = 0;
        for (IndexedItemScore score: model.getNeighbors(item)) {
            long other = model.getItem(score.getIndex());
            double s = score.getScore();
            if (normed.containsId(other)) {
                // FIXME this goes wacky with negative similarities
                double rating = normed.get(other);
                sum += rating * s;
                totalWeight += abs(s);
            }
        }
        double pred = 0;
        if (totalWeight > 0)
            pred = sum / totalWeight;
        // FIXME Should return NULL if there is no baseline
        return new ScoredId(item, model.addBaseline(user, ratings, item, pred));
    }

    @Override
    public SparseVector predict(long user, SparseVector ratings, Collection<Long> items) {
        MutableSparseVector normed = MutableSparseVector.copy(ratings);
        model.subtractBaseline(user, ratings, normed);

        LongSortedSet iset;
        if (items instanceof LongSortedSet)
            iset = (LongSortedSet) items;
        else
            iset = new LongSortedArraySet(items);

        MutableSparseVector sums = new MutableSparseVector(iset);
        MutableSparseVector weights = new MutableSparseVector(iset);
        for (Long2DoubleMap.Entry rating: normed.fast()) {
            final double r = rating.getDoubleValue();
            for (IndexedItemScore score: model.getNeighbors(rating.getLongKey())) {
                final double s = score.getScore();
                final int idx = score.getIndex();
                final long iid = model.getItem(idx);
                weights.add(iid, abs(s));
                sums.add(iid, s*r);
            }
        }

        final boolean hasBaseline = model.hasBaseline();
        LongIterator iter = sums.keySet().iterator();
        while (iter.hasNext()) {
            final long iid = iter.next();
            final double w = weights.get(iid);
            if (w > 0)
                sums.set(iid, sums.get(iid) / w);
            else
                sums.set(iid, hasBaseline ? 0 : Double.NaN);
        }

        model.addBaseline(user, ratings, sums);
        return sums;
    }

}
