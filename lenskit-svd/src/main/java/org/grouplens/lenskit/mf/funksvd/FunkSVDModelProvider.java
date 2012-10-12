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
package org.grouplens.lenskit.mf.funksvd;

import it.unimi.dsi.fastutil.doubles.DoubleArrays;
import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongIterator;
import org.apache.commons.lang3.time.StopWatch;
import org.grouplens.lenskit.baseline.BaselinePredictor;
import org.grouplens.lenskit.collections.CollectionUtils;
import org.grouplens.lenskit.collections.FastCollection;
import org.grouplens.lenskit.core.Transient;
import org.grouplens.lenskit.data.pref.IndexedPreference;
import org.grouplens.lenskit.data.snapshot.PreferenceSnapshot;
import org.grouplens.lenskit.mf.funksvd.params.FeatureCount;
import org.grouplens.lenskit.transform.clamp.ClampingFunction;
import org.grouplens.lenskit.util.iterative.StoppingCondition;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Provider;

/**
 * SVD recommender builder using gradient descent (Funk SVD).
 *
 * This recommender builder constructs an SVD-based recommender using gradient
 * descent, as pioneered by Simon Funk.  It also incorporates the regularizations
 * Funk did. These are documented in
 * <a href="http://sifter.org/~simon/journal/20061211.html">Netflix Update: Try
 * This at Home</a>. This implementation is based in part on
 * <a href="http://www.timelydevelopment.com/demos/NetflixPrize.aspx">Timely
 * Development's sample code</a>.
 *
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 */
public class FunkSVDModelProvider implements Provider<FunkSVDModel> {
    private static Logger logger = LoggerFactory.getLogger(FunkSVDModelProvider.class);

    // The default value for feature values - isn't supposed to matter much
    private static final double DEFAULT_FEATURE_VALUE = 0.1;

    private final int featureCount;
    private final BaselinePredictor baseline;
    private final PreferenceSnapshot snapshot;

    private FunkSVDTrainingConfig rule;
    private StoppingCondition stoppingCondition;

    @Inject
    public FunkSVDModelProvider(@Transient @Nonnull PreferenceSnapshot snapshot,
                                @Transient @Nonnull FunkSVDTrainingConfig rule,
                                @Nonnull BaselinePredictor baseline,
                                @Transient StoppingCondition stop,
                                @FeatureCount int featureCount) {
        stoppingCondition = stop;
        this.featureCount = featureCount;
        this.baseline = baseline;
        this.snapshot = snapshot;
        this.rule = rule;
    }


    /* (non-Javadoc)
    * @see org.grouplens.lenskit.RecommenderComponentBuilder#build(org.grouplens.lenskit.data.snapshot.RatingBuildContext)
    */
    @Override
    public FunkSVDModel get() {
        double[][] userFeatures = new double[featureCount][snapshot.getUserIds().size()];
        double[][] itemFeatures = new double[featureCount][snapshot.getItemIds().size()];

        logger.debug("Setting up to build SVD recommender with {} features", featureCount);
        logger.debug("Learning rate is {}", rule.getLearningRate());
        logger.debug("Regularization term is {}", rule.getTrainingRegularization());

        FastCollection<IndexedPreference> ratings = snapshot.getRatings();
        logger.debug("Building SVD with {} features for {} ratings", featureCount, ratings.size());

        double[] estimates = initializeEstimates(snapshot, baseline);
        ClampingFunction clamp = rule.getClampingFunction();

        for (int f = 0; f < featureCount; f++) {
            trainFeature(estimates, ratings, f, userFeatures[f], itemFeatures[f]);

            // Update each rating's cached value to accommodate the feature values.
            updateRatingEstimates(estimates, ratings, userFeatures[f], itemFeatures[f], clamp);
        }

        return new FunkSVDModel(featureCount, itemFeatures, userFeatures,
                                rule.getClampingFunction(), snapshot.itemIndex(), snapshot.userIndex(), baseline);
    }


    private void trainFeature(double[] estimates, FastCollection<IndexedPreference> ratings,
                              int feature, double[] ufvs, double[] ifvs) {
        logger.trace("Training feature {}", feature);

        // Fetch and initialize the arrays for this feature
        DoubleArrays.fill(ufvs, DEFAULT_FEATURE_VALUE);
        DoubleArrays.fill(ifvs, DEFAULT_FEATURE_VALUE);

        // We assume that all subsequent features have DEFAULT_FEATURE_VALUE
        // We can therefore pre-compute the "trailing" prediction value, as it
        // will be the same for all ratings for this feature.
        final double trail = (featureCount - feature - 1)
                * DEFAULT_FEATURE_VALUE * DEFAULT_FEATURE_VALUE;

        // Initialize our counters and error tracking
        StopWatch timer = new StopWatch();
        timer.start();

        double oldRMSE = Double.NaN;
        double rmse = Double.MAX_VALUE;
        int niters = 0;
        while (!stoppingCondition.isFinished(niters, oldRMSE - rmse)) {
            oldRMSE = rmse;
            rmse = doFeatureIteration(estimates, ratings, ufvs, ifvs, trail);

            niters += 1;
            logger.trace("iteration {} finished with RMSE {}", niters, rmse);
        }

        timer.stop();
        logger.debug("Finished feature {} in {} epochs (took {})",
                     new Object[]{feature, niters, timer});
    }

    private double doFeatureIteration(double[] estimates, FastCollection<IndexedPreference> ratings,
                                      double[] ufvs, double[] ifvs, double trail) {
        double sse = 0;
        int n = 0;

        for (IndexedPreference r: CollectionUtils.fast(ratings)) {
            final int uidx = r.getUserIndex();
            final int iidx = r.getItemIndex();

            // Step 1: Save the old feature values before computing the new ones
            final double ouf = ufvs[uidx];
            final double oif = ifvs[iidx];

            // Step 2: Compute the error
            final double err = rule.computeError(r.getUserId(), r.getItemId(),
                                                 trail, estimates[r.getIndex()],
                                                 r.getValue(), ouf, oif);

            // Step 3: Update feature values
            ufvs[uidx] += rule.userUpdate(err, ouf, oif);
            ifvs[iidx] += rule.itemUpdate(err, ouf, oif);

            sse += err * err;
            n += 1;
        }

        return Math.sqrt(sse / n);
    }

    private void updateRatingEstimates(double[] estimates, FastCollection<IndexedPreference> ratings,
                                       double[] ufvs, double[] ifvs, ClampingFunction clamp) {
        for (IndexedPreference r : CollectionUtils.fast(ratings)) {
            double est = estimates[r.getIndex()];
            double offset = ufvs[r.getUserIndex()] * ifvs[r.getItemIndex()];
            estimates[r.getIndex()] = clamp.apply(r.getUserId(), r.getItemId(), est + offset);
        }
    }

    private double[] initializeEstimates(PreferenceSnapshot snapshot, BaselinePredictor baseline) {
        final LongCollection userIds = snapshot.getUserIds();
        final int numItem = snapshot.getRatings().size();
        double[] estimates = new double[numItem];

        LongIterator userIter = userIds.iterator();
        while (userIter.hasNext()) {
            long uid = userIter.nextLong();
            SparseVector rvector = snapshot.userRatingVector(uid);
            MutableSparseVector blpreds = new MutableSparseVector(rvector.keySet());
            baseline.predict(uid, rvector, blpreds);

            for (IndexedPreference r : CollectionUtils.fast(snapshot.getUserRatings(uid))) {
                estimates[r.getIndex()] = blpreds.get(r.getItemId());
            }
        }

        return estimates;
    }
}
