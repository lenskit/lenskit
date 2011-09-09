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

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongSet;

import javax.annotation.Nullable;

import org.grouplens.lenskit.LenskitRecommenderEngineFactory;
import org.grouplens.lenskit.baseline.BaselinePredictor;
import org.grouplens.lenskit.data.dao.DataAccessObject;
import org.grouplens.lenskit.data.event.Event;
import org.grouplens.lenskit.data.history.HistorySummarizer;
import org.grouplens.lenskit.data.history.UserHistory;
import org.grouplens.lenskit.data.history.UserVector;
import org.grouplens.lenskit.knn.params.NeighborhoodSize;
import org.grouplens.lenskit.norm.VectorNormalizer;
import org.grouplens.lenskit.norm.VectorTransformation;
import org.grouplens.lenskit.params.UserHistorySummary;
import org.grouplens.lenskit.vector.MutableSparseVector;
import org.grouplens.lenskit.vector.SparseVector;
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
public class ItemItemRatingPredictor extends ItemItemScorer {
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
     * Normalize data and supply baselines. The resulting transformation uses
     * the normalizer ({@link #setNormalizer(VectorNormalizer)}) to normalize
     * the user data and denormalize the predictions. It then supplies missing
     * predictions from the baseline predictor (
     * {@link #setBaseline(BaselinePredictor)}) if one has been configured.
     */
    @Override
    protected VectorTransformation makeTransform(final UserVector userData) {
        return new VectorTransformation() {
            final VectorTransformation norm =
                    normalizer.makeTransformation(userData);
            final UserVector ratings = userData;

            @Override
            public MutableSparseVector unapply(MutableSparseVector vector) {
                norm.unapply(vector);
                // apply the baseline if applicable
                if (baseline != null) {
                    LongList unpredItems = new LongArrayList();
                    for (Long2DoubleMap.Entry pred: vector.fast()) {
                        double p = pred.getDoubleValue();
                        if (Double.isNaN(p)) {
                            unpredItems.add(pred.getLongKey());
                        }
                    }
                    if (!unpredItems.isEmpty()) {
                        logger.trace("Filling {} items from baseline", unpredItems.size());
                        SparseVector basePreds = baseline.predict(ratings, unpredItems);
                        vector.set(basePreds);
                    }
                }
                return vector;
            }

            @Override
            public MutableSparseVector apply(MutableSparseVector vector) {
                return norm.apply(vector);
            }
        };
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
