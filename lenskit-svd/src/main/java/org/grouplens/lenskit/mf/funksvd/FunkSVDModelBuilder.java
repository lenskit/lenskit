/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 LensKit Contributors.  See CONTRIBUTORS.md.
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

import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.grouplens.lenskit.core.Transient;
import org.grouplens.lenskit.data.pref.IndexedPreference;
import org.grouplens.lenskit.data.snapshot.PreferenceSnapshot;
import org.grouplens.lenskit.iterative.TrainingLoopController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Provider;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * SVD recommender builder using gradient descent (Funk SVD).
 *
 * <p>
 * This recommender builder constructs an SVD-based recommender using gradient
 * descent, as pioneered by Simon Funk.  It also incorporates the regularizations
 * Funk did. These are documented in
 * <a href="http://sifter.org/~simon/journal/20061211.html">Netflix Update: Try
 * This at Home</a>. This implementation is based in part on
 * <a href="http://www.timelydevelopment.com/demos/NetflixPrize.aspx">Timely
 * Development's sample code</a>.</p>
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class FunkSVDModelBuilder implements Provider<FunkSVDModel> {
    private static Logger logger = LoggerFactory.getLogger(FunkSVDModelBuilder.class);

    protected final int featureCount;
    protected final PreferenceSnapshot snapshot;
    protected final double initialValue;

    protected final FunkSVDUpdateRule rule;

    @Inject
    public FunkSVDModelBuilder(@Transient @Nonnull PreferenceSnapshot snapshot,
                               @Transient @Nonnull FunkSVDUpdateRule rule,
                               @FeatureCount int featureCount,
                               @InitialFeatureValue double initVal) {
        this.featureCount = featureCount;
        this.initialValue = initVal;
        this.snapshot = snapshot;
        this.rule = rule;
    }


    @Override
    public FunkSVDModel get() {
        int userCount = snapshot.getUserIds().size();
        RealMatrix userFeatures = MatrixUtils.createRealMatrix(userCount, featureCount);

        int itemCount = snapshot.getItemIds().size();
        RealMatrix itemFeatures = MatrixUtils.createRealMatrix(itemCount, featureCount);

        logger.debug("Learning rate is {}", rule.getLearningRate());
        logger.debug("Regularization term is {}", rule.getTrainingRegularization());

        logger.info("Building SVD with {} features for {} ratings",
                    featureCount, snapshot.getRatings().size());

        TrainingEstimator estimates = rule.makeEstimator(snapshot);

        List<FeatureInfo> featureInfo = new ArrayList<FeatureInfo>(featureCount);

        // Use scratch vectors for each feature for better cache locality
        // Per-feature vectors are strided in the output matrices
        RealVector uvec = MatrixUtils.createRealVector(new double [userCount]);
        RealVector ivec = MatrixUtils.createRealVector(new double[itemCount]);

        for (int f = 0; f < featureCount; f++) {
            logger.debug("Training feature {}", f);
            StopWatch timer = new StopWatch();
            timer.start();

            uvec.set(initialValue);
            ivec.set(initialValue);

            FeatureInfo.Builder fib = new FeatureInfo.Builder(f);
            trainFeature(f, estimates, uvec, ivec, fib);
            summarizeFeature(uvec, ivec, fib);
            featureInfo.add(fib.build());

            // Update each rating's cached value to accommodate the feature values.
            estimates.update(uvec, ivec);

            // And store the data into the matrix
            userFeatures.setColumnVector(f, uvec);
            assert Math.abs(userFeatures.getColumnVector(f).getL1Norm() - uvec.getL1Norm()) < 1.0e-4 : "user column sum matches";
            itemFeatures.setColumnVector(f, ivec);
            assert Math.abs(itemFeatures.getColumnVector(f).getL1Norm() - ivec.getL1Norm()) < 1.0e-4 : "item column sum matches";

            timer.stop();
            logger.info("Finished feature {} in {}", f, timer);
        }

        // Wrap the user/item matrices because we won't use or modify them again
        return new FunkSVDModel(userFeatures,
                                itemFeatures,
                                snapshot.userIndex(), snapshot.itemIndex(),
                                featureInfo);
    }

    /**
     * Train a feature using a collection of ratings.  This method iteratively calls {@link
     * #doFeatureIteration(TrainingEstimator, Collection, RealVector, RealVector, double)}  to train
     * the feature.  It can be overridden to customize the feature training strategy.
     *
     * <p>We use the estimator to maintain the estimate up through a particular feature value,
     * rather than recomputing the entire kernel value every time.  This hopefully speeds up training.
     * It means that we always tell the updater we are training feature 0, but use a subvector that
     * starts with the current feature.</p>
     *
     *
     * @param feature   The number of the current feature.
     * @param estimates The current estimator.  This method is <b>not</b> expected to update the
     *                  estimator.
     * @param userFeatureVector      The user feature values.  This has been initialized to the initial value,
     *                  and may be reused between features.
     * @param itemFeatureVector      The item feature values.  This has been initialized to the initial value,
     *                  and may be reused between features.
     * @param fib       The feature info builder. This method is only expected to add information
     *                  about its training rounds to the builder; the caller takes care of feature
     *                  number and summary data.
     * @see #doFeatureIteration(TrainingEstimator, Collection, RealVector, RealVector, double)
     * @see #summarizeFeature(RealVector, RealVector, FeatureInfo.Builder)
     */
    protected void trainFeature(int feature, TrainingEstimator estimates,
                                RealVector userFeatureVector, RealVector itemFeatureVector,
                                FeatureInfo.Builder fib) {
        double rmse = Double.MAX_VALUE;
        double trail = initialValue * initialValue * (featureCount - feature - 1);
        TrainingLoopController controller = rule.getTrainingLoopController();
        Collection<IndexedPreference> ratings = snapshot.getRatings();
        while (controller.keepTraining(rmse)) {
            rmse = doFeatureIteration(estimates, ratings, userFeatureVector, itemFeatureVector, trail);
            fib.addTrainingRound(rmse);
            logger.trace("iteration {} finished with RMSE {}", controller.getIterationCount(), rmse);
        }
    }

    /**
     * Do a single feature iteration.
     *
     *
     *
     * @param estimates The estimates.
     * @param ratings   The ratings to train on.
     * @param userFeatureVector The user column vector for the current feature.
     * @param itemFeatureVector The item column vector for the current feature.
     * @param trail The sum of the remaining user-item-feature values.
     * @return The RMSE of the feature iteration.
     */
    protected double doFeatureIteration(TrainingEstimator estimates,
                                        Collection<IndexedPreference> ratings,
                                        RealVector userFeatureVector, RealVector itemFeatureVector,
                                        double trail) {
        // We'll create a fresh updater for each feature iteration
        // Not much overhead, and prevents needing another parameter
        FunkSVDUpdater updater = rule.createUpdater();

        for (IndexedPreference r : ratings) {
            final int uidx = r.getUserIndex();
            final int iidx = r.getItemIndex();

            updater.prepare(0, r.getValue(), estimates.get(r),
                            userFeatureVector.getEntry(uidx), itemFeatureVector.getEntry(iidx), trail);

            // Step 3: Update feature values
            userFeatureVector.addToEntry(uidx, updater.getUserFeatureUpdate());
            itemFeatureVector.addToEntry(iidx, updater.getItemFeatureUpdate());
        }

        return updater.getRMSE();
    }

    /**
     * Add a feature's summary to the feature info builder.
     *
     * @param ufv The user values.
     * @param ifv The item values.
     * @param fib  The feature info builder.
     */
    protected void summarizeFeature(RealVector ufv, RealVector ifv, FeatureInfo.Builder fib) {
        fib.setUserAverage(realVectorSum(ufv) / ufv.getDimension())
           .setItemAverage(realVectorSum(ifv) / ifv.getDimension())
           .setSingularValue(ufv.getNorm() * ifv.getNorm());
    }

    // TODO Find a better solution than this
    private double realVectorSum (RealVector rv){
        double total = 0;
        for (double i : rv.toArray()){
            total += i;
        }
        return total;
    }
}
