/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
 * Work on LensKit has been funded by the National Science Foundation under
 * grants IIS 05-34939, 08-08692, 08-12148, and 10-17697.
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
package org.grouplens.lenskit.mf.funksvd;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.grouplens.lenskit.basic.AbstractItemScorer;
import org.grouplens.lenskit.data.Event;
import org.grouplens.lenskit.data.UserHistory;
import org.grouplens.lenskit.data.dao.DataAccessObject;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.event.Ratings;
import org.grouplens.lenskit.iterative.TrainingLoopController;
import org.grouplens.lenskit.transform.clamp.ClampingFunction;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

/**
 * Do recommendations and predictions based on SVD matrix factorization.
 *
 * Recommendation is done based on folding-in.  The strategy is do a fold-in
 * operation as described in
 * <a href="http://www.grouplens.org/node/212">Sarwar et al., 2002</a> with the
 * user's ratings.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class FunkSVDItemScorer extends AbstractItemScorer {

    protected final FunkSVDModel model;
    private DataAccessObject dao;
    private final int featureCount;
    private final ClampingFunction clamp;

    @Nullable
    private final FunkSVDUpdateRule rule;

    @Inject
    public FunkSVDItemScorer(DataAccessObject dao, FunkSVDModel model,
                             @Nullable FunkSVDUpdateRule rule) {
        super(dao);
        this.dao = dao;
        this.model = model;
        this.rule = rule;

        featureCount = model.featureCount;
        clamp = model.clampingFunction;
    }

    @Nullable
    public FunkSVDUpdateRule getUpdateRule() {
        return rule;
    }

    /**
     * Predict for a user using their preference array and history vector.
     *
     * @param user   The user's ID
     * @param uprefs The user's preference array from the model.
     * @param output The output vector, whose key domain is the items to predict for. It must
     *               be initialized to the user's baseline predictions.
     */
    private void predict(long user, double[] uprefs, MutableSparseVector output) {
        for (VectorEntry e : output.fast()) {
            final long item = e.getKey();
            final int iidx = model.itemIndex.getIndex(item);

            if (iidx < 0) {
                continue;
            }

            double score = e.getValue();
            for (int f = 0; f < featureCount; f++) {
                score += uprefs[f] * model.itemFeatures[f][iidx];
                score = clamp.apply(user, item, score);
            }
            output.set(e, score);
        }
    }

    /**
     * Get estimates for all a user's ratings and the target items.
     *
     * @param user    The user ID.
     * @param ratings The user's ratings.
     * @param items   The target items.
     * @return Baseline predictions for all items either in the target set or the set of
     *         rated items.
     */
    private MutableSparseVector initialEstimates(long user, SparseVector ratings, LongSet items) {
        LongSet allItems = new LongOpenHashSet(items);
        allItems.addAll(ratings.keySet());
        MutableSparseVector estimates = new MutableSparseVector(allItems);
        model.baseline.predict(user, ratings, estimates);
        return estimates;
    }

    @Override
    public void score(@Nonnull UserHistory<? extends Event> userHistory,
                      @Nonnull MutableSparseVector scores) {
        long user = userHistory.getUserId();
        int uidx = model.userIndex.getIndex(user);
        SparseVector ratings = Ratings.userRatingVector(dao.getUserEvents(user, Rating.class));

        MutableSparseVector estimates = initialEstimates(user, ratings, scores.keyDomain());
        // propagate estimates to the output scores
        scores.set(estimates);
        if (uidx < 0 && ratings.isEmpty()) {
            // no real work to do, stop with baseline predictions
            return;
        }

        double[] uprefs;
        if (uidx < 0) {
            uprefs = new double[model.featureCount];
            for (int i = 0; i < model.featureCount; i++) {
                uprefs[i] = model.getFeatureInfo(i).getUserAverage();
            }
        } else {
            uprefs = new double[featureCount];
            for (int i = 0; i < featureCount; i++) {
                uprefs[i] = model.userFeatures[i][uidx];
            }
        }

        if (!ratings.isEmpty() && rule != null) {
            for (int f = 0; f < featureCount; f++) {
                trainUserFeature(user, uprefs, ratings, estimates, f);
            }
        }

        // scores are the estimates, uprefs are trained up.
        predict(user, uprefs, scores);
    }

    private void trainUserFeature(long user, double[] uprefs, SparseVector ratings,
                                  MutableSparseVector estimates, int feature) {
        assert rule != null;

        double rmse = Double.MAX_VALUE;
        TrainingLoopController controller = rule.getTrainingLoopController();
        while (controller.keepTraining(rmse)) {
            rmse = doFeatureIteration(user, uprefs, ratings, estimates, feature);
        }

        // After training this feature, we need to update each rating's cached
        // value to accommodate it.
        double[] ifvs = model.itemFeatures[feature];
        for (VectorEntry itemId : ratings.fast()) {
            final long iid = itemId.getKey();
            double est = estimates.get(iid);
            double offset = uprefs[feature] * ifvs[model.itemIndex.getIndex(iid)];
            est = clamp.apply(user, iid, est + offset);
            estimates.set(iid, est);
        }
    }

    private double doFeatureIteration(long user, double[] uprefs,
                                      SparseVector ratings, MutableSparseVector estimates,
                                      int feature) {
        assert rule != null;
        double sse = 0;
        int n = 0;
        for (VectorEntry e: ratings.fast()) {
            final long iid = e.getKey();
            final int iidx = model.itemIndex.getIndex(iid);

            // Step 1: Compute the trailing value for this item-feature pair
            double trailingValue = 0.0;
            for (int f = feature + 1; f < featureCount; f++) {
                trailingValue += uprefs[f] * model.itemFeatures[f][iidx];
            }

            // Step 2: Save the old feature values before computing the new ones
            final double ouf = uprefs[feature];
            final double oif = model.itemFeatures[feature][iidx];

            // Step 3: Compute the error
            final double err = rule.computeError(user, iid, trailingValue,
                                                 estimates.get(iid), e.getValue(),
                                                 ouf, oif);

            // Step 4: update user preferences
            uprefs[feature] += rule.userUpdate(err, ouf, oif);

            sse += err * err;
            n += 1;
        }
        return Math.sqrt(sse / n);
    }
}
