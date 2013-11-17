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
package org.grouplens.lenskit.mf.svdplusplus;

import it.unimi.dsi.fastutil.doubles.DoubleArrays;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongListIterator;
import org.apache.commons.lang3.time.StopWatch;
import org.grouplens.lenskit.collections.CollectionUtils;
import org.grouplens.lenskit.collections.FastCollection;
import org.grouplens.lenskit.core.Transient;
import org.grouplens.lenskit.data.pref.IndexedPreference;
import org.grouplens.lenskit.data.snapshot.PreferenceSnapshot;
import org.grouplens.lenskit.iterative.TrainingLoopController;
import org.grouplens.lenskit.mf.funksvd.FeatureCount;
import org.grouplens.lenskit.mf.funksvd.FeatureInfo;
import org.grouplens.lenskit.mf.funksvd.InitialFeatureValue;
import org.grouplens.lenskit.mf.funksvd.TrainingEstimator;
import org.grouplens.lenskit.util.Index;
import org.grouplens.lenskit.vectors.MutableVec;
import org.grouplens.lenskit.vectors.Vec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Provider;
import java.util.ArrayList;
import java.util.List;
import java.lang.Math;

/**
 * SVD++ recommender builder using gradient descent (SVD++).
 *
 * This recommender builder constructs an SVD++ recommender using gradient
 * descent, as pioneered by Yehuda Koren.  It also incorporates the regularizations
 * Koren did. These are documented in
 * <a href="http://dl.acm.org/citation.cfm?id=1401944">Factorization meets the neighborhood:
 * a multifaceted collaborative filtering model</a>.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class SVDPlusPlusModelBuilder implements Provider<SVDPlusPlusModel> {
    private static Logger logger = LoggerFactory.getLogger(SVDPlusPlusModelBuilder.class);

    protected final int featureCount;
    protected final PreferenceSnapshot snapshot;
    protected final double initialValue;

    protected final SVDPlusPlusUpdateRule rule;

    @Inject
    public SVDPlusPlusModelBuilder(@Transient PreferenceSnapshot snapshot,
                               @Transient SVDPlusPlusUpdateRule rule,
                               @FeatureCount int featureCount,
                               @InitialFeatureValue double initVal) {
        this.featureCount = featureCount;
        this.initialValue = initVal;
        this.snapshot = snapshot;
        this.rule = rule;
    }


    @Override
    public SVDPlusPlusModel get() {
        double[][] userFeatures = new double[featureCount][snapshot.getUserIds().size()];
        double[][] itemFeatures = new double[featureCount][snapshot.getItemIds().size()];
        double[][] itemImpFeatures = new double[featureCount][snapshot.getItemIds().size()];

        logger.debug("Setting up to build SVD++ recommender with {} features", featureCount);
        logger.debug("Learning rate is {}", rule.getLearningRate());
        logger.debug("Regularization term is {}", rule.getTrainingRegularization());

        logger.debug("Building SVD++ with {} features for {} ratings",
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
            DoubleArrays.fill(itemImpFeatures[f], initialValue);


            FeatureInfo.Builder fib = new FeatureInfo.Builder(f);
            trainFeature(f, estimates, userFeatures[f], itemFeatures[f], itemImpFeatures[f], fib);
            summarizeFeature(userFeatures[f], itemFeatures[f], fib);
            featureInfo.add(fib.build());

            // Update each rating's cached value to accommodate the feature values.
            estimates.update(userFeatures[f], itemFeatures[f]);

            timer.stop();
            logger.debug("Finished feature {} in {}", f, timer);
        }

        return new SVDPlusPlusModel(featureCount, itemFeatures, userFeatures, itemImpFeatures,
                                rule.getClampingFunction(),
                                snapshot.itemIndex(), snapshot.userIndex(),
                                featureInfo);
    }

    /**
     * Train a feature using a collection of ratings.  This method iteratively calls {@link
     * #doFeatureIteration(TrainingEstimator, FastCollection, double[], double[], double[])} to train
     * the feature.  It can be overridden to customize the feature training strategy.
     *
     * @param feature   The number of the current feature.
     * @param estimates The current estimator.  This method is <b>not</b> expected to update the
     *                  estimator.
     * @param ufvs      The user feature values.  This has been initialized to the initial value,
     *                  and may be reused between features.
     * @param ifvs      The item feature values.  This has been initialized to the initial value,
     *                  and may be reused between features.
     * @param iifvs     The item implicit feature values.  This has been initialized to the initial value,
     *                  and may be reused between features.
     * @param fib       The feature info builder. This method is only expected to add information
     *                  about its training rounds to the builder; the caller takes care of feature
     *                  number and summary data.
     * @see {@link #doFeatureIteration(TrainingEstimator, FastCollection, double[], double[], double[]
     *      )}
     * @see {@link #summarizeFeature(double[], double[], FeatureInfo.Builder)}
     */
    protected void trainFeature(int feature, TrainingEstimator estimates,
                                double[] ufvs, double[] ifvs, double[] iifvs,
                                FeatureInfo.Builder fib) {
        double rmse = Double.MAX_VALUE;
        TrainingLoopController controller = rule.getTrainingLoopController();
        FastCollection<IndexedPreference> ratings = snapshot.getRatings();
        while (controller.keepTraining(rmse)) {
            rmse = doFeatureIteration(estimates, ratings, ufvs, ifvs, iifvs);
            fib.addTrainingRound(rmse);
            logger.trace("iteration {} finished with RMSE {}", controller.getIterationCount(), rmse);
        }
        Index uinds = snapshot.userIndex();
        LongList uids = uinds.getIds();
        LongListIterator it = uids.iterator();
        while (it.hasNext()) {
            long uid = it.next(); 
            final int uidx = uinds.getIndex(uid);
            FastCollection<IndexedPreference> userRatings = snapshot.getUserRatings(uid);
            int ratnum = userRatings.size();
            for (IndexedPreference ir : CollectionUtils.fast(userRatings)) {
                int ratedidx = ir.getItemIndex();
                ufvs[uidx] += iifvs[ratedidx] / Math.sqrt((double)ratnum);
            }
        }
    }

    /**
     * Do a single feature iteration.
     *
     * @param estimates The estimates.
     * @param ratings   The ratings to train on.
     * @param ufvs      The user feature values.
     * @param ifvs      The item feature values.
     * @param iifvs     The item implicit feature values.
     * @return The RMSE of the feature iteration.
     */
    protected double doFeatureIteration(TrainingEstimator estimates,
                                        FastCollection<IndexedPreference> ratings,
                                        double[] ufvs, double[] ifvs, double[] iifvs) {
        double sse = 0;
        int n = 0;

        for (IndexedPreference r : CollectionUtils.fast(ratings)) {
            final int uidx = r.getUserIndex();
            final int iidx = r.getItemIndex();

            // Step 1: Save the old feature values before computing the new ones
            final double ouf = ufvs[uidx];
            final double oif = ifvs[iidx];
            final double oiif = iifvs[iidx];

            // Step 2: Compute the error
            double uside = 0;
            FastCollection<IndexedPreference> userRatings = snapshot.getUserRatings(r.getUserId());
            final double err = rule.computeError(uidx, iidx, r.getUserId(), r.getItemId(), uside,
                    ufvs, ifvs, iifvs, r.getValue(), estimates.get(r), userRatings);

            // Step 3: Update feature values
            ufvs[uidx] += rule.userUpdate(err, ouf, oif);
            ifvs[iidx] += rule.itemUpdate(err, ouf, oif, uside);
            iifvs[iidx] += rule.itemImpUpdate(err, ouf, oif, oiif, userRatings.size());

            sse += err * err;
            n += 1;
        }

        return Math.sqrt(sse / n);
    }

    /**
     * Add a feature's summary to the feature info builder.
     *
     * @param ufvs The user values added up with item implicit values.
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
