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

import it.unimi.dsi.fastutil.longs.LongSet;

import javax.annotation.Nullable;

import org.grouplens.lenskit.RatingPredictor;
import org.grouplens.lenskit.baseline.BaselinePredictor;
import org.grouplens.lenskit.collections.LongSortedArraySet;
import org.grouplens.lenskit.core.LenskitRecommenderEngineFactory;
import org.grouplens.lenskit.data.Event;
import org.grouplens.lenskit.data.UserHistory;
import org.grouplens.lenskit.data.dao.DataAccessObject;
import org.grouplens.lenskit.data.history.HistorySummarizer;
import org.grouplens.lenskit.data.history.UserVector;
import org.grouplens.lenskit.knn.params.NeighborhoodSize;
import org.grouplens.lenskit.norm.VectorNormalizer;
import org.grouplens.lenskit.norm.VectorTransformation;
import org.grouplens.lenskit.params.UserHistorySummary;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generate predictions with item-item collaborative filtering. This configures
 * {@link ItemItemScorer} to predict ratings, and can supply baseline
 * predictions if so configured.
 *
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 * @see ItemItemModelBuilder
 * @see ItemItemScorer
 */
public class ItemItemRatingPredictor extends ItemItemScorer implements RatingPredictor {
    private static final Logger logger = LoggerFactory.getLogger(ItemItemRatingPredictor.class);
    protected @Nullable BaselinePredictor baseline;

    public ItemItemRatingPredictor(DataAccessObject dao, ItemItemModel model,
                                   @NeighborhoodSize int nnbrs,
                                   @UserHistorySummary HistorySummarizer summarizer) {
        super(dao, model, nnbrs, summarizer, new WeightedAverageNeighborhoodScorer());
        logger.debug("Creating rating scorer with neighborhood size {}", neighborhoodSize);
    }

    @Nullable
    public BaselinePredictor getBaseline() {
        return baseline;
    }

    /**
     * Configure the baseline predictor for unpredicatble items. If an item
     * cannot be have its preference predicted (e.g. no neighborhood is found),
     * the prediction is supplied from this baseline.
     *
     * @param pred The baseline predictor. Configure this by setting the
     *        {@link BaselinePredictor} component.
     * @see LenskitRecommenderEngineFactory#setComponent(Class, Class)
     */
    public void setBaseline(@Nullable BaselinePredictor pred) {
        baseline = pred;
    }

    /**
     * Configure a neighborhood scorer. The default scorer is
     * {@link WeightedAverageNeighborhoodScorer}.
     *
     * @param scorer
     */
    // FIXME: Enable this code when the new config system allows it
//    public void setScorer(NeighborhoodScorer scorer) {
//        this.scorer = scorer;
//    }

    /**
     * Make a transform that wraps the normalizer (
     * {@link #setNormalizer(VectorNormalizer)}) with a baseline-adding
     * transform if there is a baseline configured.
     */
    @Override
    protected VectorTransformation makeTransform(final UserVector userData) {
        if (baseline == null) {
            return normalizer.makeTransformation(userData);
        } else {
            return new BaselineAddingTransform(userData);
        }
    }
    
    /**
     * Vector transformation that wraps the normalizer to supply baseline
     * predictions for missing values in the
     * {@link #unapply(MutableSparseVector)} method.
     * 
     * @author Michael Ekstrand <ekstrand@cs.umn.edu>
     * 
     */
    protected class BaselineAddingTransform implements VectorTransformation {
        final VectorTransformation norm;
        final UserVector ratings;
        
        public BaselineAddingTransform(UserVector userData) {
            ratings = userData;
            norm = normalizer.makeTransformation(userData);
        }

        @Override
        public MutableSparseVector unapply(MutableSparseVector vector) {
            norm.unapply(vector);
            
            assert baseline != null;
            LongSet unpredItems = LongSortedArraySet.setDifference(vector.keyDomain(), vector.keySet());
            if (!unpredItems.isEmpty()) {
                logger.trace("Filling {} items from baseline",
                             unpredItems.size());
                SparseVector basePreds = baseline.predict(ratings, unpredItems);
                vector.set(basePreds);
            }
            
            return vector;
        }

        @Override
        public MutableSparseVector apply(MutableSparseVector vector) {
            return norm.apply(vector);
        }
    }

    @Override
    public LongSet getScoreableItems(UserHistory<? extends Event> user) {
        if (baseline != null) {
            return model.getItemUniverse();
        } else {
            return super.getScoreableItems(user);
        }
    }
}
