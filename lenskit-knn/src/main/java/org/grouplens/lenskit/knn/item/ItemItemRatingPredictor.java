/*
 * LensKit, a reference implementation of recommender algorithms.
 * Copyright 2010-2011 Regents of the University of Minnesota
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.grouplens.lenskit.knn.item;

import static java.lang.Math.abs;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSortedSet;

import java.util.Collection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.grouplens.lenskit.AbstractItemScorer;
import org.grouplens.lenskit.baseline.BaselinePredictor;
import org.grouplens.lenskit.data.ScoredLongList;
import org.grouplens.lenskit.data.ScoredLongListIterator;
import org.grouplens.lenskit.data.dao.DataAccessObject;
import org.grouplens.lenskit.data.event.Event;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.history.RatingVectorSummarizer;
import org.grouplens.lenskit.data.history.UserHistory;
import org.grouplens.lenskit.data.vector.MutableSparseVector;
import org.grouplens.lenskit.data.vector.SparseVector;
import org.grouplens.lenskit.data.vector.UserVector;
import org.grouplens.lenskit.knn.params.NeighborhoodSize;
import org.grouplens.lenskit.norm.IdentityVectorNormalizer;
import org.grouplens.lenskit.norm.VectorNormalizer;
import org.grouplens.lenskit.norm.VectorTransformation;
import org.grouplens.lenskit.params.UserRatingVectorNormalizer;
import org.grouplens.lenskit.util.LongSortedArraySet;
import org.grouplens.lenskit.util.ScoredItemAccumulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generate predictions with item-item collaborative filtering.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 * @see ItemItemModelBuilder
 */
public class ItemItemRatingPredictor extends AbstractItemScorer implements ItemItemScorer {
	private static final Logger logger = LoggerFactory.getLogger(ItemItemRatingPredictor.class);
    protected final ItemItemModel model;
    private final int neighborhoodSize;
    protected @Nonnull VectorNormalizer<? super UserVector> normalizer =
            new IdentityVectorNormalizer();
    protected @Nullable BaselinePredictor baseline;
    
    public ItemItemRatingPredictor(DataAccessObject dao, ItemItemModel model, 
                                   @NeighborhoodSize int nnbrs) {
        super(dao);
        this.model = model;
        neighborhoodSize = nnbrs;
        logger.debug("Creating rating scorer with neighborhood size {}", neighborhoodSize);
    }
    
    @Nonnull
    public VectorNormalizer<? super UserVector> getNormalizer() {
        return normalizer;
    }
    
    @UserRatingVectorNormalizer
    public void setNormalizer(VectorNormalizer<? super UserVector> norm) {
        normalizer = norm;
    }
    
    @Nullable
    public BaselinePredictor getBaseline() {
        return baseline;
    }
    
    public void setBaseline(@Nullable BaselinePredictor pred) {
        baseline = pred;
    }
    
    @Override
    public ItemItemModel getModel() {
        return model;
    }

    @Override
    public SparseVector score(UserHistory<? extends Event> history, Collection<Long> items) {
        UserVector ratings = RatingVectorSummarizer.makeRatingVector(history);
        VectorTransformation norm = normalizer.makeTransformation(ratings);
        MutableSparseVector normed = ratings.mutableCopy();
        norm.apply(normed);

        // FIXME Make sure the direction on similarities is right for asym.
        
        LongSortedSet iset;
        if (items instanceof LongSortedSet)
            iset = (LongSortedSet) items;
        else
            iset = new LongSortedArraySet(items);
        
        MutableSparseVector preds = new MutableSparseVector(iset, Double.NaN);
        
        // We ran reuse accumulators
        ScoredItemAccumulator accum = new ScoredItemAccumulator(neighborhoodSize);
        
        // for each item, compute its prediction
        LongIterator iter = iset.iterator();
        LongList unpredItems = new LongArrayList();
        while (iter.hasNext()) {
            final long item = iter.nextLong();
            
            // find all potential neighbors
            // FIXME: Take advantage of the fact that the neighborhood is sorted
            ScoredLongList neighbors = model.getNeighbors(item);
            
            if (neighbors == null) {
                unpredItems.add(item);
                continue;
            }
            ScoredLongListIterator niter = neighbors.iterator();
            while (niter.hasNext()) {
                long oi = niter.nextLong();
                double score = niter.getScore();
                if (normed.containsKey(oi))
                    accum.put(oi, score);
            }
            
            // accumulate prediction
            double sum = 0;
            double weight = 0;
            niter = accum.finish().iterator();
            while (niter.hasNext()) {
                long oi = niter.nextLong();
                double sim = niter.getScore();
                weight += abs(sim);
                sum += sim * normed.get(oi);
            }
            if (weight > 0)
                preds.set(item, sum / weight);
            else
                unpredItems.add(item);
        }

        // denormalize the predictions
        norm.unapply(preds);
        
        // apply the baseline if applicable
        if (baseline != null && !unpredItems.isEmpty()) {
        	logger.trace("Filling {} items from baseline", unpredItems.size());
            SparseVector basePreds = baseline.predict(ratings, unpredItems);
            for (Long2DoubleMap.Entry e: basePreds.fast()) {
                assert Double.isNaN(preds.get(e.getLongKey()));
                preds.set(e.getLongKey(), e.getDoubleValue());
            }
            return preds;
        } else {
        	return preds.copy(true);
        }
    }
    
    @Override
    public LongSet getScoreableItems(UserHistory<? extends Event> user) {
        // FIXME This method incorrectly assumes the model is symmetric
        if (baseline != null) {
            return model.getItemUniverse();
        } else {
            LongSet items = new LongOpenHashSet();
            LongSet userItems = user.filter(Rating.class).itemSet();
            LongIterator iter = userItems.iterator();
            while (iter.hasNext()) {
                final long item = iter.nextLong();
                items.addAll(model.getNeighbors(item));
            }
            return items;
        }
    }
}
