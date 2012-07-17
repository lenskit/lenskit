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
import it.unimi.dsi.fastutil.longs.LongCollection;

import java.util.Iterator;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.commons.lang3.time.StopWatch;
import org.grouplens.grapht.annotation.Transient;
import org.grouplens.lenskit.baseline.BaselinePredictor;
import org.grouplens.lenskit.collections.FastCollection;
import org.grouplens.lenskit.data.pref.IndexedPreference;
import org.grouplens.lenskit.data.snapshot.PreferenceSnapshot;
import org.grouplens.lenskit.svd.params.FeatureCount;
import org.grouplens.lenskit.transform.clamp.ClampingFunction;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;
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
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class FunkSVDModelProvider implements Provider<FunkSVDModel> {
    private static Logger logger = LoggerFactory.getLogger(FunkSVDModelProvider.class);

    // The default value for feature values - isn't supposed to matter much
    private static final double DEFAULT_FEATURE_VALUE = 0.1;
    
    private final int featureCount;
    private final BaselinePredictor baseline;
    private final PreferenceSnapshot snapshot;

    private double[][] userFeatures;
    private double[][] itemFeatures;
    private UpdateRule trainer;
    
    @Inject
    public FunkSVDModelProvider(@Transient PreferenceSnapshot snapshot, @FeatureCount int featureCount,
                               BaselinePredictor baseline, UpdateRule trainer) {
        this.featureCount = featureCount;
        this.baseline = baseline;
        this.snapshot = snapshot;
        
        userFeatures = new double[featureCount][snapshot.getUserIds().size()];
        itemFeatures = new double[featureCount][snapshot.getItemIds().size()];
        this.trainer = trainer;
    }

    /* (non-Javadoc)
     * @see org.grouplens.lenskit.RecommenderComponentBuilder#build(org.grouplens.lenskit.data.snapshot.RatingBuildContext)
     */
    @Override
    public FunkSVDModel get() {
    	
        logger.debug("Setting up to build SVD recommender with {} features", featureCount);
        logger.debug("Learning rate is {}", trainer.getLearningRate());
        logger.debug("Regularization term is {}", trainer.getTrainingRegularization());
        
        if (trainer.getIterationCount() > 0) {
            logger.debug("Training each epoch for {} iterations", trainer.getIterationCount());
        } else {
            logger.debug("Error epsilon is {}", trainer.getTrainingThreshold());
        }

        double[] estimates = initializeEstimates(snapshot, baseline);
        FastCollection<IndexedPreference> ratings = snapshot.getRatings();

        logger.debug("Building SVD with {} features for {} ratings",
                						featureCount, ratings.size());

        ClampingFunction clamp = trainer.getClampingFunction();
        // We'll be using this one object throughout the whole building time
        // 		by reseting its internal values in the loop over featureCount
        for (int i = 0; i < featureCount; i++) {
        	trainer.reset();
            trainFeature(estimates, ratings, i, trainer, clamp);
        }

        return new FunkSVDModel(featureCount, itemFeatures, userFeatures, trainer.getClampingFunction(),
        								snapshot.itemIndex(),snapshot.userIndex(), baseline);
    }
    
    private void trainFeature(double[] estimates, FastCollection<IndexedPreference> ratings
								, int feature, UpdateRule trainer, ClampingFunction clamp) {
    	
        logger.trace("Training feature {}", feature);

        // Fetch and initialize the arrays for this feature
        DoubleArrays.fill(userFeatures[feature], DEFAULT_FEATURE_VALUE);
        DoubleArrays.fill(itemFeatures[feature], DEFAULT_FEATURE_VALUE);

        // Initialize our counters and error tracking
        StopWatch timer = new StopWatch();
        timer.start();
        
        // We assume that all subsequent features have DEFAULT_FEATURE_VALUE
        // We can therefore precompute the "trailing" prediction value, as it
        // will be the same for all ratings for this feature.
        final double trailingValue = (featureCount - feature - 1) 
					* DEFAULT_FEATURE_VALUE * DEFAULT_FEATURE_VALUE;
        
        while (trainer.nextEpoch()) {
            logger.trace("Running epoch {} of feature {}", trainer.getEpoch(), feature);
            
            Iterator<IndexedPreference> ratingIter = ratings.fastIterator();
        	while (ratingIter.hasNext()) {
        		IndexedPreference r = ratingIter.next();
            	final int uidx = r.getUserIndex();
                final int iidx = r.getItemIndex();
                
                // Step 1: Save the old feature values before computing the new ones 
                final double ouf = userFeatures[feature][uidx];
                final double oif = itemFeatures[feature][iidx];
                
                // Step 2: Compute the error
                trainer.compute(r.getUserId(), r.getItemId(), trailingValue 
                					, estimates[r.getIndex()], r.getValue(), ouf, oif);

                // Step 3: Update feature values
                userFeatures[feature][uidx] = trainer.getUserUpdate(ouf, oif);
                itemFeatures[feature][iidx] = trainer.getItemUpdate(ouf, oif);
            }
            
            logger.trace("Epoch {} had RMSE of {}", trainer.getEpoch(), trainer.getLastRMSE());
        }

        timer.stop();
        logger.debug("Finished feature {} in {} epochs (took {}), rmse={}",
                new Object[]{feature, trainer.getEpoch(), timer, trainer.getLastRMSE()});

        // After training this feature, we need to update each rating's cached
        // value to accommodate it.
        double[] ufvs = userFeatures[feature];
        double[] ifvs = itemFeatures[feature];
        Iterator<IndexedPreference> ratingIter = ratings.fastIterator();
    	while (ratingIter.hasNext()) {
    		IndexedPreference r = ratingIter.next();
            double est = estimates[r.getIndex()];
            estimates[r.getIndex()] = clamp.apply(r.getUserId(), r.getItemId()
            		, est + ufvs[r.getUserIndex()] * ifvs[r.getItemIndex()]);
        }
    }
    
    private double[] initializeEstimates(PreferenceSnapshot snapshot,
            BaselinePredictor baseline) {
    	final LongCollection userIds = snapshot.getUserIds();
    	final int nprefs = snapshot.getRatings().size();
    	double[] estimates = new double[nprefs];

    	for (long uid : userIds) {
    		SparseVector rvector = snapshot.userRatingVector(uid);
    		MutableSparseVector blpreds = baseline.predict(uid, rvector, rvector.keySet());

    		Iterator<IndexedPreference> ratingIter = snapshot.getUserRatings(uid).fastIterator();
        	while (ratingIter.hasNext()) {
        		IndexedPreference r = ratingIter.next();
    			estimates[r.getIndex()] = blpreds.get(r.getItemId());
    		}
    	}

    	return estimates;
    }
}