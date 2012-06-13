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
package org.grouplens.lenskit.svd;

import it.unimi.dsi.fastutil.doubles.DoubleArrays;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSortedSet;

import java.util.Collection;

import javax.inject.Inject;

import org.apache.commons.lang3.time.StopWatch;
import org.grouplens.lenskit.RatingPredictor;
import org.grouplens.lenskit.baseline.BaselinePredictor;
import org.grouplens.lenskit.collections.CollectionUtils;
import org.grouplens.lenskit.collections.FastCollection;
import org.grouplens.lenskit.collections.LongSortedArraySet;
import org.grouplens.lenskit.core.AbstractItemScorer;
import org.grouplens.lenskit.data.Event;
import org.grouplens.lenskit.data.UserHistory;
import org.grouplens.lenskit.data.dao.DataAccessObject;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.event.Ratings;
import org.grouplens.lenskit.data.pref.IndexedPreference;
import org.grouplens.lenskit.data.snapshot.PreferenceSnapshot;
import org.grouplens.lenskit.svd.params.IterationCount;
import org.grouplens.lenskit.svd.params.LearningRate;
import org.grouplens.lenskit.svd.params.RegularizationTerm;
import org.grouplens.lenskit.svd.params.TrainingThreshold;
import org.grouplens.lenskit.transform.clamp.ClampingFunction;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Do recommendations and predictions based on SVD matrix factorization.
 *
 * Recommendation is done based on folding-in.  The strategy is do a fold-in
 * operation as described in
 * <a href="http://www.grouplens.org/node/212">Sarwar et al., 2002</a> with the
 * user's ratings.
 *
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class FunkSVDRatingPredictor extends AbstractItemScorer implements RatingPredictor {
    protected final FunkSVDModel model;
    private DataAccessObject dao;
    
    
    //Added
    
    private static Logger logger = LoggerFactory.getLogger(FunkSVDRatingPredictor.class);
    // The default value for feature values - isn't supposed to matter much
    private static final double DEFAULT_FEATURE_VALUE = 0.1;
    // Minimum number of epochs to run to train a feature
    private static final double MIN_EPOCHS = 50;
    private final int iterationCount;
    private final double learningRate;
    private final double trainingThreshold;
    private final double trainingRegularization;    
    //End Adding
    
    @Inject
    public FunkSVDRatingPredictor(DataAccessObject dao, FunkSVDModel m, 
    								   @IterationCount int iterCount,
    	                               @LearningRate double learningRate,
    	                               @TrainingThreshold double threshold,
    	                               @RegularizationTerm double gradientDescent) {
        super(dao);
        this.dao = dao;
        model = m;
        
        //Added
        iterationCount = iterCount;
        this.learningRate = learningRate; 
        trainingThreshold = threshold;
        trainingRegularization = gradientDescent;
        //End Adding
        
    }

    /**
     * Get the number of features used by the underlying factorization.
     * @return the feature count (rank) of the factorization.
     */
    public int getFeatureCount() {
        return model.featureCount;
    }

    //Added
    private double[] initializeEstimates(long userId, SparseVector ratings) {
    	double[] estimates = new double[model.numItem];;
    	MutableSparseVector blpreds = model.baseline.predict(userId, ratings, ratings.keySet());
    	for (long iid : blpreds.keySet()) {
    		estimates[model.getItemIndex(iid)] = blpreds.get(iid);
    	}
    	return estimates;
    }
    
    private void getUserPrefs(long user, SparseVector ratings, double[] uprefs){    	
    	
    	double[] estimates = initializeEstimates(user, ratings);
    	
    	if (ratings.isEmpty()){
    		if (model.getUserIndex(user) < 0){
    			DoubleArrays.fill(uprefs, 0);
    		} 
    		else{
    			for (int feature = 0; feature < getFeatureCount(); feature++){
    				uprefs[feature] = model.getUserFeatureValue(model.getUserIndex(user), feature);
    			}
    		}
    	} 
    	else{
    		if (model.getUserIndex(user) < 0){
    			uprefs = model.averUserFeatures;
    		}
    		else{
    			uprefs = PredTimeTraining(user, ratings, uprefs, estimates);
    		}
    	}
    }
    
    private double[] PredTimeTraining(long user, SparseVector ratings, 
    									double[] uprefs, double[] estimates){
    	int featureCount = getFeatureCount();
    	double[] trainedUprefs = new double[featureCount];
    	for (int f = 0; f < featureCount; f++){
    		predTrainFeature(user, trainedUprefs, uprefs, ratings, estimates, f);
    	}
    	return trainedUprefs;
    }
    
    private void predTrainFeature(long user, double[] trainedUprefs, double[] inputUprefs,
    						SparseVector ratings, double[] estimates, int feature){

        logger.trace("Training feature {}", feature);

        // Fetch and initialize the arrays for this feature
        final double ufv = inputUprefs[feature];
        final double[] ifv = model.itemFeatures[feature];
        final int featureCount = getFeatureCount();

        // We assume that all subsequent features have DEFAULT_FEATURE_VALUE
        // We can therefore pre-compute the "trailing" prediction value, as it
        // will be the same for all ratings for this feature.
        final int rFeatCount = featureCount - feature - 1;
        final double trailingValue = rFeatCount * DEFAULT_FEATURE_VALUE * DEFAULT_FEATURE_VALUE;

        // Initialize our counters and error tracking
        double rmse = Double.MAX_VALUE, oldRmse = 0.0;
        int epoch;
        StopWatch timer = new StopWatch();
        timer.start();

        for (epoch = 0; !isDone(epoch, rmse, oldRmse); epoch++) {
            logger.trace("Running epoch {} of feature {}", epoch, feature);
            // Save the old RMSE so that we can measure change in error
            oldRmse = rmse;
            // Run the iteration and save the error
            rmse = trainFeatureIteration(user, ratings, ufv, ifv, estimates, trailingValue);
            logger.trace("Epoch {} had RMSE of {}", epoch, rmse);
        }

        timer.stop();
        logger.debug("Finished feature {} in {} epochs (took {}), rmse={}",
                new Object[]{feature, epoch, timer, rmse});

        // After training this feature, we need to update each rating's cached
        // value to accommodate it.
        for (long itemId : ratings.keySet()) {
            final int idx = model.getItemIndex(itemId);
            double est = estimates[idx];
            est = model.clampingFunction.apply(user, itemId,
                                         est + ufv * ifv[idx]);
            estimates[idx] = est;
        }
    }
	

    private final double trainFeatureIteration(long user, SparseVector ratings, double ufv, double[] ifvs,
    												double[] estimates, double trailingValue) {
        // We'll need to keep track of our sum of squares
        double ssq = 0;
        for (long itemId : ratings.keySet()) {
            ssq += trainRating(user, ufv, ifvs, estimates, trailingValue, itemId, ratings);
        }
        // We're done with this feature.  Compute the total error (RMSE)
        // and head off to the next iteration.
        return Math.sqrt(ssq / ratings.size());
    }

    private final double trainRating(long userId, double ufv, double[] ifv, double[] estimates,
    									double trailingValue, long itemId, SparseVector ratings) {
    	final int idx = model.getItemIndex(itemId);
    	final double value = ratings.get(itemId);
    	// Step 1: get the predicted value (based on preceding features
    	// and the current feature values)
    	final double estimateSingleEntry = estimates[idx];
    	double pred = estimateSingleEntry + ufv * ifv[idx];
    	pred = model.clampingFunction.apply(userId, itemId, pred);

    	// Step 1b: add the estimate from remaining trailing values
    	// and apply the result.
    	pred = model.clampingFunction.apply(userId, itemId, pred + trailingValue);

    	// Step 2: compute the prediction error. We will follow this for
    	// the gradient descent.
    	final double err = value - pred;

    	// Step 3: update the feature values.  We'll save the old values first.
    	final double ouf = ufv;
    	final double oif = ifv[idx];
    	// Then we'll update user feature preference
    	final double udelta = err * oif - trainingRegularization * ouf;
    	ufv += udelta * learningRate;
    	// The item feature relevance.remains unchanged

    	// Finally, accumulate the squared error
    	return err * err;
    }

    protected final boolean isDone(int epoch, double rmse, double oldRmse) {
        if (iterationCount > 0) {
            return epoch >= iterationCount;
        } else {
            return epoch >= MIN_EPOCHS && (oldRmse - rmse) < trainingThreshold;
        }
    }

    
    //End Adding
    
    /**
     * Predict for a user using their preference array and history vector.
     * 
     * @param user The user's rating vector.
     * @param uprefs The user's preference array from the model.
     * @param items The items to predict for.
     * @return The user's predictions.
     */
    
    private MutableSparseVector predict(long user, SparseVector ratings,
    									double[] uprefs, Collection<Long> items) {
        final int nf = model.featureCount;
        final ClampingFunction clamp = model.clampingFunction;
        
        //Added
        getUserPrefs(user, ratings, uprefs);
        //End Adding
        
        LongSortedSet iset;
        if (items instanceof LongSortedSet) {
            iset = (LongSortedSet) items;
        } else {
            iset = new LongSortedArraySet(items);
        }

        MutableSparseVector preds = model.baseline.predict(user, ratings, items);
        LongIterator iter = iset.iterator();
        while (iter.hasNext()) {
            final long item = iter.nextLong();
            final int idx = model.getItemIndex(item);
            if (idx < 0) {
                continue;
            }

            double score = preds.get(item);
            for (int f = 0; f < nf; f++) {
                score += uprefs[f] * model.getItemFeatureValue(idx, f);
                score = clamp.apply(user, item, score);
            }
            preds.set(item, score);
        }

        return preds;
    }

    /**
     * Predict from a user ID and preference array. Delegates to
     * {@link #predict(long, SparseVector, double[], Collection)}.
     */
    private MutableSparseVector predict(long user, double[] uprefs,
                                        Collection<Long> items) {
        return predict(user,
                       Ratings.userRatingVector(dao.getUserEvents(user, Rating.class)),
                       uprefs, items);
    }

    /**
     * FunkSVD cannot currently user user history.
     */
    @Override
    public boolean canUseHistory() {
        return false;
    }

    @Override
    public SparseVector score(UserHistory<? extends Event> user, Collection<Long> items) {
        return score(user.getUserId(), items);
    }

    @Override
    public MutableSparseVector score(long user, Collection<Long> items) {
        int uidx = model.userIndex.getIndex(user);
        if (uidx >= 0) {
            double[] uprefs = new double[model.featureCount];
            for (int i = 0; i < uprefs.length; i++) {
                uprefs[i] = model.userFeatures[i][uidx];
            }
            return predict(user, uprefs, items);
        } else {
            // The user was not included in the model, so just use the baseline
            SparseVector ratings = Ratings.userRatingVector(dao.getUserEvents(user, Rating.class));
            return model.baseline.predict(user, ratings, items);
        }
    }
}
