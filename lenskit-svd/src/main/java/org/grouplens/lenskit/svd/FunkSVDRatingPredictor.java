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
	
	protected final FunkSVDModel model;
    private DataAccessObject dao;
    private final int featureCount;
    private final ClampingFunction clamp;
    
    private UpdateRule trainer;
    
    
    @Inject
    public FunkSVDRatingPredictor(DataAccessObject dao, FunkSVDModel model, UpdateRule trainer) {
        super(dao);
        this.dao = dao;
        this.model = model;
        this.trainer = trainer;
        
        featureCount = model.featureCount;
        clamp = model.clampingFunction;
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
            final int iidx = model.itemIndex.getIndex(item);
            
            if (iidx < 0) {
                continue;
            }

            double score = preds.get(item);
            for (int f = 0; f < featureCount; f++) {
                score += uprefs[f] * model.itemFeatures[f][iidx];
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
    private MutableSparseVector predict(long user, double[] uprefs, Collection<Long> items) {
        return predict(user,
                       Ratings.userRatingVector(dao.getUserEvents(user, Rating.class)),
                       uprefs, items);
    }

    
    /**
     * FunkSVD can currently use user history.
     */
    @Override
    public boolean canUseHistory() {
        return true;
    }

    
    @Override
    public MutableSparseVector score(UserHistory<? extends Event> userHistory, Collection<Long> items) {
        long user = userHistory.getUserId();
        int uidx = model.userIndex.getIndex(user);
        SparseVector ratings = Ratings.userRatingVector(dao.getUserEvents(user, Rating.class));
        
        MutableSparseVector estimates = model.baseline.predict(user, ratings, ratings.keySet());
        if (ratings.isEmpty() && uidx < 0) {
        	return estimates;
        }
        
        double[] uprefs = new double[featureCount];
        if (uidx < 0){
    		uprefs = model.averUserFeatures;
    	} else{
    		for (int i = 0; i < featureCount; i++) {
        		uprefs[i] = model.userFeatures[i][uidx];
        	}
    	}
        
        if (!ratings.isEmpty()){
        	for (int f = 0; f < featureCount; f++){
        		// Reset and reuse the same UpdateRule object trainer in every iteration
	    		trainer.reset();
	    		trainUserFeature(user, uprefs, ratings, estimates, f, trainer);
	    	}
		}
        
        return predict(user, uprefs, items);
    }

    
    @Override
    public MutableSparseVector score(long user, Collection<Long> items) {
        return score(dao.getUserHistory(user), items);
    }
    
    
    private final void trainUserFeature(long user, double[] uprefs, SparseVector ratings,
    									MutableSparseVector estimates, int feature, UpdateRule trainer){
    	while (trainer.nextEpoch()) {
        	for (Entry itemId : ratings.fast()) {
        		final long item = itemId.getLongKey();
        		final int iidx = model.itemIndex.getIndex(item);
        		
        		// Step 1: Compute the trailing value for this item-feature pair
            	double trailingValue = 0.0;
            	for (int f = feature + 1; f < featureCount; f++) {
            		trailingValue += uprefs[f] * model.itemFeatures[f][iidx];
            	}
        	
        		// Step 2: Save the old feature values before computing the new ones 
            	final double ouf = uprefs[feature];
            	final double oif = model.itemFeatures[feature][iidx];
            
            	// Step 3: Compute the err
            	// Notice the trainer.compute method should always be called before
                // 	updating the feature values in step 4, since this method
                //  renew the internal feature values that will be used in step 4
            	final double ratingValue = itemId.getValue();
            	final double estimate = estimates.get(item);
            	trainer.compute(user, item, trailingValue, estimate, ratingValue, ouf, oif);

            	// Step 4: Return updated user feature value
            	uprefs[feature] += trainer.getUserUpdate(); 
        	}
        }

        // After training this feature, we need to update each rating's cached
        // value to accommodate it.
        double[] ifvs = model.itemFeatures[feature];
        for (Entry itemId : ratings.fast()) {
            final long iid = itemId.getLongKey();
            double est = estimates.get(iid);
            double offset = uprefs[feature] * ifvs[model.itemIndex.getIndex(iid)];
            est = clamp.apply(user, iid, est + offset);
            estimates.set(iid, est);
        }
    }
}
