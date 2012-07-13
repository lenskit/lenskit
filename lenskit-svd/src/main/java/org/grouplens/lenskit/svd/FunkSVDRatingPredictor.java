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
import org.grouplens.lenskit.svd.params.IterationCount;
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
    public FunkSVDRatingPredictor(DataAccessObject dao, FunkSVDModel m,
    					@IterationCount int iterCount, UpdateRule trainer) {
        super(dao);
        this.dao = dao;
        model = m;
        
        featureCount = model.featureCount;
        clamp = model.clampingFunction;
        
        this.trainer = trainer;
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
            final int idx = model.itemIndex.getIndex(item);
            
            if (idx < 0) {
                continue;
            }

            final double[] ifvs = model.getItemFeatureVector(idx);
            double score = preds.get(item);
            for (int f = 0; f < featureCount; f++) {
                score += uprefs[f] * ifvs[f];
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
     * FunkSVD cannot currently use user history.
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
        SparseVector ratings = Ratings.userRatingVector(dao.getUserEvents(user, Rating.class));
        MutableSparseVector estimates = model.baseline.predict(user, ratings, ratings.keySet());
        
        if (ratings.isEmpty() && uidx < 0) {
        	return estimates;
        }
        
        double[] uprefs = new double[featureCount];
        if (uidx < 0){
    		uprefs = model.averUserFeatures;
    	} else{
    		uprefs = model.getUserFeatureVector(uidx);
    	}
        
        if (!ratings.isEmpty()){
        	// We'll be using this one object throughout the whole building time
            // 		by reseting its internal values in the loop over featureCount
	    	for (int f = 0; f < featureCount; f++){
	    		trainer.reset();
	    		trainUserFeature(user, uprefs, ratings, estimates, f, trainer);
	    	}
		}
        
        return predict(user, uprefs, items);
    }
    
    private final void trainUserFeature(long user, double[] uprefs, SparseVector ratings,
    									MutableSparseVector estimates, int feature, UpdateRule trainer){
    	
        // Fetch and initialize the arrays for this feature
        final double[] ifvs = model.itemFeatures[feature];
        
        // Compute the trailing value
    	double trailingValue = 0.0;
    	for (int f = feature + 1; f < feature; f++) {
    		trailingValue += uprefs[f] * ifvs[f];
    	}
        
        while (trainer.nextEpoch()) {
        	for (Entry itemId : ratings.fast()) {
        		final long iid = itemId.getLongKey();
        	
        		// Step 1: Save the old feature values before computing the new ones 
            	final double ouf = uprefs[feature];
            	final double oif = ifvs[feature];
            
            	// Step 2: Compute the err
            	trainer.compute(user, itemId.getLongKey(), trailingValue, estimates.get(iid),
            													itemId.getValue(), ouf, oif);

            	// Step 3: Return updated user feature value
            	uprefs[feature] = trainer.getUserUpdate(ouf, oif); 
        	}
        }

        // After training this feature, we need to update each rating's cached
        // value to accommodate it.
        for (Entry itemId : ratings.fast()) {
            final long iid = itemId.getLongKey();
            double est = estimates.get(itemId.getLongKey());
            est = clamp.apply(user, iid
            		, est + uprefs[feature] * ifvs[model.itemIndex.getIndex(iid)]);
            estimates.set(iid, est);
        }
    }
}
