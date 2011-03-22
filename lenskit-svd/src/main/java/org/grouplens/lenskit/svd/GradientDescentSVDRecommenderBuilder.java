/*
 * RefLens, a reference implementation of recommender algorithms.
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
package org.grouplens.lenskit.svd;

import it.unimi.dsi.fastutil.doubles.DoubleArrays;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;

import java.util.ArrayList;
import java.util.List;

import org.grouplens.common.cursors.Cursor;
import org.grouplens.lenskit.RatingPredictor;
import org.grouplens.lenskit.RecommenderBuilder;
import org.grouplens.lenskit.RecommenderService;
import org.grouplens.lenskit.data.Indexer;
import org.grouplens.lenskit.data.Rating;
import org.grouplens.lenskit.data.RatingDataSource;
import org.grouplens.lenskit.data.vector.MutableSparseVector;
import org.grouplens.lenskit.data.vector.SparseVector;
import org.grouplens.lenskit.svd.params.ClampingFunction;
import org.grouplens.lenskit.svd.params.FeatureCount;
import org.grouplens.lenskit.svd.params.FeatureTrainingThreshold;
import org.grouplens.lenskit.svd.params.GradientDescentRegularization;
import org.grouplens.lenskit.svd.params.IterationCount;
import org.grouplens.lenskit.svd.params.LearningRate;
import org.grouplens.lenskit.util.DoubleFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

/**
 * SVD recommender builder using gradient descent (Funk SVD).
 *
 * This recommender builder constructs an SVD-based recommender using gradient
 * descent, as pioneered by Simon Funk.  It also incorporates the regularizations
 * Funk did.  These are documented in
 * <a href="http://sifter.org/~simon/journal/20061211.html">Netflix Update: Try
 * This at Home</a>.
 *
 * This implementation is based in part on
 * <a href="http://www.timelydevelopment.com/demos/NetflixPrize.aspx">Timely
 * Development's sample code</a>.
 *
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class GradientDescentSVDRecommenderBuilder implements RecommenderBuilder {
    private static Logger logger = LoggerFactory.getLogger(GradientDescentSVDRecommenderBuilder.class);

    // The default value for feature values - isn't supposed to matter much
    private static final double DEFAULT_FEATURE_VALUE = 0.1;
    // Minimum number of epochs to run to train a feature
    private static final double MIN_EPOCHS = 50;
    // Internal epsilon to avoid division by 0
    private static final double MIN_FEAT_NORM = 0.0000000001;

    final int featureCount;
    final double learningRate;
    final double trainingThreshold;
    final double trainingRegularization;
    final DoubleFunction clampingFunction;
    final int iterationCount;

    @Inject
    public GradientDescentSVDRecommenderBuilder(
            @FeatureCount int features,
            @LearningRate double lrate,
            @FeatureTrainingThreshold double trainingThreshold,
            @IterationCount int icount,
            @GradientDescentRegularization double reg,
            @ClampingFunction DoubleFunction clamp) {
        featureCount = features;
        learningRate = lrate;
        this.trainingThreshold = trainingThreshold;
        trainingRegularization = reg;
        clampingFunction = clamp;
        iterationCount = icount;
    }

    @Override
    public RecommenderService build(RatingDataSource data, RatingPredictor baseline) {
        logger.debug("Setting up to build SVD recommender with {} features", featureCount);
        logger.debug("Learning rate is {}", learningRate);
        logger.debug("Regularization term is {}", trainingRegularization);
        if (iterationCount > 0) {
            logger.debug("Training each epoch for {} iterations", iterationCount);
        } else {
            logger.debug("Error epsilon is {}", trainingThreshold);
        }

        Model model = new Model();
        Indexer userIndex = new Indexer();
        model.userIndex = userIndex;
        Indexer itemIndex = new Indexer();
        model.itemIndex = itemIndex;
        List<SVDRating> ratings = indexData(data, baseline, userIndex, itemIndex, model);

        // update each rating to start at the baseline
        for (SVDRating r: ratings) {
            r.cachedValue = model.userBaselines.get(r.user).get(r.iid);
        }

        logger.debug("Building SVD with {} features for {} ratings",
                featureCount, ratings.size());
        model.userFeatures = new double[featureCount][userIndex.getObjectCount()];
        model.itemFeatures = new double[featureCount][itemIndex.getObjectCount()];
        for (int i = 0; i < featureCount; i++) {
            trainFeature(model, ratings, i);
        }

        logger.debug("Extracting singular values");
        model.singularValues = new double[featureCount];
        for (int feature = 0; feature < featureCount; feature++) {
            double[] ufv = model.userFeatures[feature];
            double ussq = 0;
            int numUsers = model.userIndex.getObjectCount();
            for (int i = 0; i < numUsers; i++) {
                double uf = ufv[i];
                ussq += uf * uf;
            }
            double unrm = (double) Math.sqrt(ussq);
            if (unrm > MIN_FEAT_NORM) {
                for (int i = 0; i < numUsers; i++) {
                    ufv[i] /= unrm;
                }
            }
            double[] ifv = model.itemFeatures[feature];
            double issq = 0;
            int numItems = model.itemIndex.getObjectCount();
            for (int i = 0; i < numItems; i++) {
                double fv = ifv[i];
                issq += fv * fv;
            }
            double inrm = (double) Math.sqrt(issq);
            if (inrm > MIN_FEAT_NORM) {
                for (int i = 0; i < numItems; i++) {
                    ifv[i] /= inrm;
                }
            }
            model.singularValues[feature] = unrm * inrm;
        }

        return new SVDRecommenderService(featureCount, itemIndex, baseline, model.itemFeatures, model.singularValues,
                clampingFunction);
    }

    private final void trainFeature(Model model, List<SVDRating> ratings, int feature) {
        logger.trace("Training feature {}", feature);

        // Fetch and initialize the arrays for this feature
        double ufv[] = model.userFeatures[feature];
        double ifv[] = model.itemFeatures[feature];
        DoubleArrays.fill(ufv, DEFAULT_FEATURE_VALUE);
        DoubleArrays.fill(ifv, DEFAULT_FEATURE_VALUE);

        // We assume that all subsequent features have DEFAULT_FEATURE_VALUE
        // We can therefore precompute the "trailing" prediction value, as it
        // will be the same for all ratings for this feature.
        final int rFeatCount = featureCount - feature - 1;
        final double trailingValue = rFeatCount * DEFAULT_FEATURE_VALUE * DEFAULT_FEATURE_VALUE;

        // Initialize our counters and error tracking
        double rmse = Double.MAX_VALUE, oldRmse = 0.0;
        int epoch;
        // We have two potential terminating conditions: if iterationCount is
        // specified, we run for that many iterations irregardless of error.
        // Otherwise, we run until the change in error is less than the training
        // threshold.
        for (epoch = 0; (iterationCount > 0) ? (epoch < iterationCount) : (epoch < MIN_EPOCHS || rmse < oldRmse - trainingThreshold); epoch++) {
            logger.trace("Running epoch {} of feature {}", epoch, feature);
            // Save the old RMSE so that we can measure change in error
            oldRmse = rmse;
            // Run the iteration and save the error
            rmse = trainFeatureIteration(ratings, ufv, ifv, trailingValue);
            logger.trace("Epoch {} had RMSE of {}", epoch, rmse);
        }

        logger.debug("Finished feature {} in {} epochs", feature, epoch);
        logger.debug("Final RMSE for feature {} was {}", feature, rmse);

        // After training this feature, we need to update each rating's cached
        // value to accommodate it.
        for (SVDRating r: ratings) {
            r.updateCachedValue(ufv, ifv);
        }
    }

    private final double trainFeatureIteration(List<SVDRating> ratings,
            double[] ufv, double[] ifv, final double trailingValue) {
        // We'll need to keep track of our sum of squares
        double ssq = 0;
        for (SVDRating r: ratings) {
            ssq += r.trainStep(ufv, ifv, trailingValue);
        }
        // We're done with this feature.  Compute the total error (RMSE)
        // and head off to the next iteration.
        return Math.sqrt(ssq / ratings.size());
    }

    private List<SVDRating> indexData(RatingDataSource data, RatingPredictor baseline, Indexer userIndex, Indexer itemIndex, Model model) {
        ArrayList<Long2DoubleMap> ratingData = new ArrayList<Long2DoubleMap>();

        Cursor<Rating> ratings = data.getRatings();
        try {
            int nratings = ratings.getRowCount();
            logger.debug("pre-processing {} ratings", nratings);
            ArrayList<SVDRating> svr = new ArrayList<SVDRating>(nratings >= 0 ? nratings : 100);
            for (Rating r: ratings) {
                SVDRating svdr = new SVDRating(model, r);
                svr.add(svdr);
                while (svdr.user >= ratingData.size()) {
                    ratingData.add(new Long2DoubleOpenHashMap());
                }
                ratingData.get(svdr.user).put(svdr.iid, svdr.value);
            }
            model.userBaselines = new ArrayList<SparseVector>(ratingData.size());
            for (int i = 0, sz = ratingData.size(); i < sz; i++) {
                SparseVector rv = new MutableSparseVector(ratingData.get(i));
                long uid = userIndex.getId(i);
                model.userBaselines.add(baseline.predict(uid, rv, rv.keySet()));
            }
            svr.trimToSize();
            return svr;
        } finally {
            ratings.close();
        }
    }

    private static final class Model {
        ArrayList<SparseVector> userBaselines;
        double userFeatures[][];
        double itemFeatures[][];
        double singularValues[];
        Indexer userIndex;
        Indexer itemIndex;
    }

    private final class SVDRating {
        public final long uid, iid;
        public final int user;
        public final int item;
        public final double value;
        public double cachedValue;

        public SVDRating(Model model, Rating r) {
            uid = r.getUserId();
            iid = r.getItemId();
            user = model.userIndex.internId(uid);
            item = model.itemIndex.internId(iid);
            this.value = r.getRating();
        }

        /**
         * Train one step on the user and item feature vectors against this rating.
         * The relevant entries in <var>ufv</var> and <var>ifv</var> are updated.
         * @param ufv The current user-feature preference vector.
         * @param ifv The current item-feature relevance vector.
         * @param trailingValue The trailing feature value to add to the prediction
         * @return The squared error in the prediction.
         */
        public double trainStep(double[] ufv, double[] ifv, double trailingValue) {
            // Step 1: get the predicted value (based on preceding features
            // and the current feature values)
            double pred = cachedValue + ufv[user] * ifv[item];
            pred = clampingFunction.apply(pred);

            // Step 1b: add the estimate from remaining trailing values
            // and clamp the result.
            pred = clampingFunction.apply(pred + trailingValue);

            // Step 2: compute the prediction error. We will follow this for
            // the gradient descent.
            final double err = value - pred;

            // Step 3: update the feature values.  We'll save the old values first.
            final double ouf = ufv[user];
            final double oif = ifv[item];
            // Then we'll update user feature preference
            final double udelta = err * oif - trainingRegularization * ouf;
            ufv[user] += udelta * learningRate;
            // And finally the item feature relevance.
            final double idelta = err * ouf - trainingRegularization * oif;
            ifv[item] += idelta * learningRate;

            // Finally, return the squared error to the caller
            return err * err;
        }

        public void updateCachedValue(double[] ufv, double[] ifv) {
            cachedValue = clampingFunction.apply(cachedValue + ufv[user] * ifv[item]);
        }
    }
}
