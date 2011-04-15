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
package org.grouplens.lenskit.svd;

import it.unimi.dsi.fastutil.doubles.DoubleArrays;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.grouplens.lenskit.AbstractRecommenderComponentBuilder;
import org.grouplens.lenskit.RecommenderComponentBuilder;
import org.grouplens.lenskit.baseline.BaselinePredictor;
import org.grouplens.lenskit.baseline.ItemUserMeanPredictor;
import org.grouplens.lenskit.data.IndexedRating;
import org.grouplens.lenskit.data.context.RatingBuildContext;
import org.grouplens.lenskit.norm.BaselineSubtractingNormalizer;
import org.grouplens.lenskit.norm.NormalizedRatingBuildContext;
import org.grouplens.lenskit.norm.UserRatingVectorNormalizer;
import org.grouplens.lenskit.util.DoubleFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * <p>The builder users a {@link NormalizedRatingBuildContext}, so normalization
 * is handled separately from the SVD build. This has the downside that we cannot
 * easily use Funk's clamping functions to further optimize the recommender.
 *
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class GradientDescentSVDModelBuilder extends AbstractRecommenderComponentBuilder<SVDModel> {
    private static Logger logger = LoggerFactory.getLogger(GradientDescentSVDModelBuilder.class);

    // The default value for feature values - isn't supposed to matter much
    private static final double DEFAULT_FEATURE_VALUE = 0.1;
    // Minimum number of epochs to run to train a feature
    private static final double MIN_EPOCHS = 50;
    // Internal epsilon to avoid division by 0
    private static final double MIN_FEAT_NORM = 0.0000000001;

    private int featureCount;
    private double learningRate;
    private double trainingThreshold;
    private double trainingRegularization;
    private DoubleFunction clampingFunction;
    private int iterationCount;
    
    private RecommenderComponentBuilder<? extends BaselinePredictor> baselineBuilder;

    public GradientDescentSVDModelBuilder() {
        featureCount = 100;
        learningRate = 0.001;
        trainingThreshold = 1.0e-5;
        trainingRegularization = 0.015;
        clampingFunction = new DoubleFunction.Identity();
        iterationCount = 0;
        baselineBuilder = new ItemUserMeanPredictor.Builder();
    }
    
    public int getFeatureCount() {
        return featureCount;
    }
    
    public void setFeatureCount(int count) {
        featureCount = count;
    }
    
    public double getLearningRate() {
        return learningRate;
    }
    
    public void setLearningRate(double rate) {
        learningRate = rate;
    }
    
    public double getTrainingThreshold() {
        return trainingThreshold;
    }
    
    public void setTrainingThreshold(double threshold) {
        trainingThreshold = threshold;
    }
    
    public double getGradientDescentRegularization() {
        return trainingRegularization;
    }
    
    public void setGradientDescentRegularization(double regularization) {
        trainingRegularization = regularization;
    }
    
    public DoubleFunction getClampingFunction() {
        return clampingFunction;
    }
    
    public void setClampingFunction(DoubleFunction function) {
        clampingFunction = function;
    }
    
    public int getIterationCount() {
        return iterationCount;
    }
    
    public void setIterationCount(int count) {
        iterationCount = count;
    }
    
    public RecommenderComponentBuilder<? extends BaselinePredictor> getBaseline() {
        return baselineBuilder;
    }
    
    public void setBaseline(RecommenderComponentBuilder<? extends BaselinePredictor> baseline) {
        baselineBuilder = baseline;
    }

    /* (non-Javadoc)
     * @see org.grouplens.lenskit.RecommenderComponentBuilder#build(org.grouplens.lenskit.data.context.RatingBuildContext)
     */
    @Override
    protected SVDModel buildNew(RatingBuildContext context) {
        logger.debug("Setting up to build SVD recommender with {} features", featureCount);
        logger.debug("Learning rate is {}", learningRate);
        logger.debug("Regularization term is {}", trainingRegularization);
        if (iterationCount > 0) {
            logger.debug("Training each epoch for {} iterations", iterationCount);
        } else {
            logger.debug("Error epsilon is {}", trainingThreshold);
        }

        // FIXME Use the baseline predictor directly.
        UserRatingVectorNormalizer blnorm = new BaselineSubtractingNormalizer.Builder()
            .setBaselinePredictor(baselineBuilder)
            .build(context);
        NormalizedRatingBuildContext data = context.normalize(blnorm);
        
        Model model = new Model();
        List<SVDRating> ratings = indexData(data, model);

        logger.debug("Building SVD with {} features for {} ratings",
                featureCount, ratings.size());
        
        final int numUsers = data.getUserIds().size();
        final int numItems = data.getItemIds().size();
        model.userFeatures = new double[featureCount][numUsers];
        model.itemFeatures = new double[featureCount][numItems];
        for (int i = 0; i < featureCount; i++) {
            trainFeature(model, ratings, i);
        }

        logger.debug("Extracting singular values");
        model.singularValues = new double[featureCount];
        for (int feature = 0; feature < featureCount; feature++) {
            double[] ufv = model.userFeatures[feature];
            double ussq = 0;
            
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
        
        return new SVDModel(featureCount, model.itemFeatures, model.singularValues,
                            clampingFunction, data.itemIndex(), data.getNormalizer());
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

    private List<SVDRating> indexData(RatingBuildContext data, Model model) {
        Collection<IndexedRating> ratings = data.getRatings();

        int nratings = ratings.size();
        logger.debug("pre-processing {} ratings", nratings);
        ArrayList<SVDRating> svr = new ArrayList<SVDRating>(nratings);
        for (IndexedRating r: ratings) {
        	SVDRating svdr = new SVDRating(model, r);
        	svr.add(svdr);
        }
        svr.trimToSize();
        return svr;
    }

    private static final class Model {
        double userFeatures[][];
        double itemFeatures[][];
        double singularValues[];
    }

    // FIXME SVDRating shouldn't duplicate as much information
    private final class SVDRating {
        public final int user;
        public final int item;
        public final double value;
        public double cachedValue;

        public SVDRating(Model model, IndexedRating r) {
            user = r.getUserIndex();
            item = r.getItemIndex();
            value = r.getRating();
            cachedValue = 0;
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
