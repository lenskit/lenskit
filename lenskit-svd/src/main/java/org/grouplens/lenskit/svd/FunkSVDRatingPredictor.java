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
import it.unimi.dsi.fastutil.longs.Long2DoubleMap.Entry;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSortedSet;

import java.util.Collection;

import javax.inject.Inject;

import org.grouplens.lenskit.RatingPredictor;
import org.grouplens.lenskit.collections.LongSortedArraySet;
import org.grouplens.lenskit.core.AbstractItemScorer;
import org.grouplens.lenskit.data.Event;
import org.grouplens.lenskit.data.UserHistory;
import org.grouplens.lenskit.data.dao.DataAccessObject;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.event.Ratings;
import org.grouplens.lenskit.svd.params.IterationCount;
import org.grouplens.lenskit.svd.params.LearningRate;
import org.grouplens.lenskit.svd.params.RegularizationTerm;
import org.grouplens.lenskit.svd.params.TrainingThreshold;
import org.grouplens.lenskit.transform.clamp.ClampingFunction;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;

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
	
    // Minimum number of epochs to run to train a feature
    private static final double MIN_EPOCHS = 50;
    
    protected final FunkSVDModel model;
    private DataAccessObject dao;
    private final int iterationCount;
    private final double learningRate;
    private final double trainingThreshold;
    private final double trainingRegularization;
    
    @Inject
    public FunkSVDRatingPredictor(DataAccessObject dao, FunkSVDModel m, 
    								   @IterationCount int iterCount,
    	                               @LearningRate double learningRate,
    	                               @TrainingThreshold double threshold,
    	                               @RegularizationTerm double gradientDescent) {
        super(dao);
        this.dao = dao;
        model = m;
        
        iterationCount = iterCount;
        this.learningRate = learningRate; 
        trainingThreshold = threshold;
        trainingRegularization = gradientDescent;
        
    }

    /**
     * Get the number of features used by the underlying factorization.
     * @return the feature count (rank) of the factorization.
     */
    public int getFeatureCount() {
        return model.featureCount;
    }

    
    /**
     * Predict for a user using their preference array and history vector.
     * 
     * @param user The user's ID
     * @param ratings The user's rating vector.
     * @param uprefs The user's preference array from the model.
     * @param items The items to predict for.
     * @return The user's predictions.
     */
    
    private MutableSparseVector predict(long user, SparseVector ratings,
    									double[] uprefs, Collection<Long> items) {
        final int nf = model.featureCount;
        final ClampingFunction clamp = model.clampingFunction;
        
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
        int uidx = model.getUserIndex(user);
        
        double[] uprefs = new double[getFeatureCount()];
        SparseVector ratings = Ratings.userRatingVector(dao.getUserEvents(user, Rating.class));
        
        if (uidx < 0){
    		if (ratings.isEmpty()){
    			DoubleArrays.fill(uprefs, 0);
    		} 
    		else{
    			uprefs = model.averUserFeatures;
    			MutableSparseVector estimates = initializeEstimates(user, ratings);
    			uprefs = trainingFeatureLoop(user, ratings, uprefs, estimates);
    		}
    		return model.baseline.predict(user, ratings, items);
    	} 
    	else{
    		if (ratings.isEmpty()){
    			for (int feature = 0; feature < getFeatureCount(); feature++){
    				uprefs[feature] = model.getUserFeatureValue(uidx, feature);
    			}
    		}
    		else{
    			MutableSparseVector estimates = initializeEstimates(user, ratings);
    			uprefs = trainingFeatureLoop(user, ratings, uprefs, estimates);
    		}
    		return predict(user, uprefs, items);
    	}
    }
    
    private final MutableSparseVector initializeEstimates(long userId, SparseVector ratings) {
    	MutableSparseVector blpreds = model.baseline.predict(userId, ratings, ratings.keySet());
    	return blpreds;
    }    
    
    private final double[] trainingFeatureLoop(long user, SparseVector ratings, 
    									double[] uprefs, MutableSparseVector estimates){
    	int featureCount = getFeatureCount();
    	
    	// Declaration and Initialization of the unique trainer object
        // We'll be using this one object throughout the whole building time
        // 		by reseting its internal values in the loop over featureCount
        UpdateRule trainer = new UpdateRule(learningRate, trainingThreshold, trainingRegularization,
											iterationCount, model.clampingFunction, MIN_EPOCHS);
        
    	for (int f = 0; f < featureCount; f++){
    		trainer.reset();
    		uprefs = trainingEachFeature(user, uprefs, ratings, estimates, f, trainer);
    	}
    	return uprefs;
    }
    
    private final double[] trainingEachFeature(long user, double[] uprefs, SparseVector ratings,
    									MutableSparseVector estimates, int feature, UpdateRule trainer){
    	
        // Fetch and initialize the arrays for this feature
        final double[] ufv = uprefs;
        final double[] ifv = model.itemFeatures[feature];
        
        while (trainer.nextEpochs()) {
            uprefs[feature] = trainFeatureIteration(user, ratings, ufv, ifv, estimates, trainer, feature);
        }

        // After training this feature, we need to update each rating's cached
        // value to accommodate it.
        for (Entry itemId : ratings.fast()) {
            final long iid = itemId.getLongKey();
            final int idx = model.getItemIndex(iid);
            double est = estimates.get(itemId.getLongKey());
            est = model.clampingFunction.apply(user, iid, est + uprefs[feature] * ifv[idx]);
            estimates.set(iid, est);
        }
        
        return uprefs;
    }
	

    private final double trainFeatureIteration(long user, SparseVector ratings, double[] ufvs, double[] ifvs,
    											MutableSparseVector estimates, UpdateRule trainer, int feature) {
    	
    	// Start looping over the ratings to train the feature value
    	double trainedUPref = 0.0;
        for (Entry itemId : ratings.fast()) {
        	final long iid = itemId.getLongKey();
        	
        	// Compute the trailing value
        	double trailingValue = 0.0;
        	for (int f = feature + 1; f < getFeatureCount(); f++) {
        		trailingValue += ufvs[f] * ifvs[model.getItemIndex(iid)];
        	}
        	
            trainedUPref = trainRating(user, ufvs[feature], ifvs, estimates.get(iid), trailingValue, 
            							iid, ratings, trainer);
        }
        // We're done with this feature. Return the trained value
        // then move on to the next feature value
        return trainedUPref;
    }

    private final double trainRating(long userId, double ufv, double[] ifv, double estimate,
    									double trailingValue, long itemId, SparseVector ratings, UpdateRule trainer) {
    	final int idx = model.getItemIndex(itemId);
    	final double value = ratings.get(itemId);
    	
    	// Step 2: Save the old feature values before computing the new ones 
        final double ouf = ufv;
        final double oif = ifv[idx];
        
        // Step 3: Compute the err
        trainer.compute(userId, itemId, trailingValue, estimate, value, ouf, oif);

        // Step 4: Return updated user feature value
        return trainer.getUserUpdate(ouf, oif);
    }

}
