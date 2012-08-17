/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
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
import org.grouplens.lenskit.RatingPredictor;
import org.grouplens.lenskit.baseline.BaselinePredictor;
import org.grouplens.lenskit.core.LenskitRecommenderEngineFactory;
import org.grouplens.lenskit.data.Event;
import org.grouplens.lenskit.data.UserHistory;
import org.grouplens.lenskit.data.dao.DataAccessObject;
import org.grouplens.lenskit.data.history.UserHistorySummarizer;
import org.grouplens.lenskit.transform.normalize.VectorTransformation;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;

/**
 * Generate predictions with item-item collaborative filtering. This configures
 * {@link ItemItemScorer} to predict ratings, and can supply baseline
 * predictions if so configured. The user's ratings are not supplied; if they have
 * rated an item, the predictor still attempts to predict their rating for that
 * item based on their other ratings.
 *
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 * @see ItemItemModelProvider
 * @see ItemItemScorer
 */
public class ItemItemRatingPredictor extends ItemItemScorer implements RatingPredictor {
    private static final Logger logger = LoggerFactory.getLogger(ItemItemRatingPredictor.class);
    protected
    @Nullable
    BaselinePredictor baseline;

    @Inject
    public ItemItemRatingPredictor(DataAccessObject dao, ItemItemModel model,
                                   UserHistorySummarizer summarizer,
                                   ItemScoreAlgorithm algo) {
        super(dao, model, summarizer, new WeightedAverageNeighborhoodScorer(), algo);
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
     *             {@link BaselinePredictor} component.
     * @see LenskitRecommenderEngineFactory#bind(Class)
     */
    @Inject
    public void setBaseline(@Nullable BaselinePredictor pred) {
        logger.debug("using baseline {}", pred);
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
     * {@link #setNormalizer(org.grouplens.lenskit.transform.normalize.UserVectorNormalizer)}) with a baseline-adding
     * transform if there is a baseline configured.
     */
    @Override
    protected VectorTransformation makeTransform(long user, SparseVector userData) {
        if (baseline == null) {
            return normalizer.makeTransformation(user, userData);
        } else {
            return new BaselineAddingTransform(user, userData);
        }
    }

    /**
     * Vector transformation that wraps the normalizer to supply baseline
     * predictions for missing values in the
     * {@link #unapply(MutableSparseVector)} method.
     *
     * @author Michael Ekstrand <ekstrand@cs.umn.edu>
     */
    protected class BaselineAddingTransform implements VectorTransformation {
        final VectorTransformation norm;
        final long user;
        final SparseVector ratings;

        public BaselineAddingTransform(long uid, SparseVector userData) {
            user = uid;
            ratings = userData;
            norm = normalizer.makeTransformation(user, userData);
        }

        @Override
        public MutableSparseVector unapply(MutableSparseVector vector) {
            norm.unapply(vector);

            assert baseline != null;
            baseline.predict(user, ratings, vector, false);
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
