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

import it.unimi.dsi.fastutil.doubles.DoubleArrays;
import org.apache.commons.lang3.time.StopWatch;
import org.grouplens.lenskit.baseline.BaselinePredictor;
import org.grouplens.lenskit.collections.CollectionUtils;
import org.grouplens.lenskit.collections.FastCollection;
import org.grouplens.lenskit.core.Transient;
import org.grouplens.lenskit.data.pref.IndexedPreference;
import org.grouplens.lenskit.data.snapshot.PreferenceSnapshot;
import org.grouplens.lenskit.iterative.TrainingLoopController;
import org.grouplens.lenskit.vectors.MutableVec;
import org.grouplens.lenskit.vectors.Vec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Provider;
import java.util.ArrayList;
import java.util.List;

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
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class FunkSVDModelBuilder implements Provider<FunkSVDModel> {
    private static Logger logger = LoggerFactory.getLogger(FunkSVDModelBuilder.class);

    protected final int featureCount;
    protected final BaselinePredictor baseline;
    protected final PreferenceSnapshot snapshot;
    protected final double initialValue;

    protected final FunkSVDUpdateRule rule;

    @Inject
    public FunkSVDModelBuilder(@Transient @Nonnull PreferenceSnapshot snapshot,
                               @Transient @Nonnull FunkSVDUpdateRule rule,
                               @Nonnull BaselinePredictor baseline,
                               @FeatureCount int featureCount,
                               @InitialFeatureValue double initVal) {
        this.featureCount = featureCount;
        this.initialValue = initVal;
        this.baseline = baseline;
        this.snapshot = snapshot;
        this.rule = rule;
    }


    @Override
    public FunkSVDModel get() {
        double[][] userFeatures = new double[featureCount][snapshot.getUserIds().size()];
        double[][] itemFeatures = new double[featureCount][snapshot.getItemIds().size()];

        logger.debug("Setting up to build SVD recommender with {} features", featureCount);
        logger.debug("Learning rate is {}", rule.getLearningRate());
        logger.debug("Regularization term is {}", rule.getTrainingRegularization());

        logger.debug("Building SVD with {} features for {} ratings",
                     featureCount, snapshot.getRatings().size());

        TrainingEstimator estimates = rule.makeEstimator(snapshot);

        List<FeatureInfo> featureInfo = new ArrayList<FeatureInfo>(featureCount);

        for (int f = 0; f < featureCount; f++) {
            logger.trace("Training feature {}", f);
            StopWatch timer = new StopWatch();
            timer.start();

            // Fetch and initialize the arrays for this feature
            DoubleArrays.fill(userFeatures[f], initialValue);
            DoubleArrays.fill(itemFeatures[f], initialValue);

            FeatureInfo.Builder fib = new FeatureInfo.Builder(f);
            trainFeature(f, estimates, userFeatures[f], itemFeatures[f], fib);
            summarizeFeature(userFeatures[f], itemFeatures[f], fib);
            featureInfo.add(fib.build());

            // Update each rating's cached value to accommodate the feature values.
            estimates.update(userFeatures[f], itemFeatures[f]);

            timer.stop();
            logger.debug("Finished feature {} in {}", f, timer);
        }

        return new FunkSVDModel(featureCount, itemFeatures, userFeatures,
                                rule.getClampingFunction(),
                                snapshot.itemIndex(), snapshot.userIndex(),
                                baseline, featureInfo);
    }

    /**
     * Train a feature using a collection of ratings.  This method iteratively calls {@link
     * #doFeatureIteration(TrainingEstimator, FastCollection, double[], double[], double)} to train
     * the feature.  It can be overridden to customize the feature training strategy.
     *
     * @param feature   The feature number.
     * @param estimates The current estimator.  This method is <b>not</b> expected to update the
     *                  estimator.
     * @param ufvs      The user feature values.  This has been initialized to the initial value,
     *                  and may be reused between features.
     * @param ifvs      The item feature values.  This has been initialized to the initial value,
     *                  and may be reused between features.
     * @param fib       The feature info builder. This method is only expected to add information
     *                  about its training rounds to the builder; the caller takes care of feature
     *                  number and summary data.
     * @see {@link #doFeatureIteration(TrainingEstimator, FastCollection, double[], double[],
     *      double)}
     * @see {@link #summarizeFeature(double[], double[], FeatureInfo.Builder)}
     */
    protected void trainFeature(int feature, TrainingEstimator estimates,
                                double[] ufvs, double[] ifvs,
                                FeatureInfo.Builder fib) {
        // We assume that all subsequent features have initialValue
        // We can therefore pre-compute the "trailing" prediction value, as it
        // will be the same for all ratings for this feature.
        final double trail = (featureCount - feature - 1) * initialValue * initialValue;

        double rmse = Double.MAX_VALUE;
        TrainingLoopController controller = rule.getTrainingLoopController();
        FastCollection<IndexedPreference> ratings = snapshot.getRatings();
        while (controller.keepTraining(rmse)) {
            rmse = doFeatureIteration(estimates, ratings, ufvs, ifvs, trail);
            fib.addTrainingRound(rmse);
            logger.trace("iteration {} finished with RMSE {}", controller.getIterationCount(), rmse);
        }
    }

    /**
     * Do a single feature iteration.
     *
     * @param estimates The estimates.
     * @param ratings   The ratings to train on.
     * @param ufvs      The user feature values.
     * @param ifvs      The item feature values.
     * @param trail     The trailing values.
     * @return The RMSE of the feature iteration.
     */
    protected double doFeatureIteration(TrainingEstimator estimates,
                                        FastCollection<IndexedPreference> ratings,
                                        double[] ufvs, double[] ifvs, double trail) {
        double sse = 0;
        int n = 0;

        for (IndexedPreference r : CollectionUtils.fast(ratings)) {
            final int uidx = r.getUserIndex();
            final int iidx = r.getItemIndex();

            // Step 1: Save the old feature values before computing the new ones
            final double ouf = ufvs[uidx];
            final double oif = ifvs[iidx];

            // Step 2: Compute the error
            final double err = rule.computeError(r.getUserId(), r.getItemId(),
                                                 trail, estimates.get(r),
                                                 r.getValue(), ouf, oif);

            // Step 3: Update feature values
            ufvs[uidx] += rule.userUpdate(err, ouf, oif);
            ifvs[iidx] += rule.itemUpdate(err, ouf, oif);

            sse += err * err;
            n += 1;
        }

        return Math.sqrt(sse / n);
    }

    /**
     * Add a feature's summary to the feature info builder.
     *
     * @param ufvs The user values.
     * @param ifvs The item values.
     * @param fib  The feature info builder.
     */
    protected void summarizeFeature(double[] ufvs, double[] ifvs, FeatureInfo.Builder fib) {
        Vec ufv = MutableVec.wrap(ufvs);
        Vec ifv = MutableVec.wrap(ifvs);
        fib.setUserAverage(ufv.mean())
           .setItemAverage(ifv.mean())
           .setSingularValue(ufv.norm() * ifv.norm());
    }
}
