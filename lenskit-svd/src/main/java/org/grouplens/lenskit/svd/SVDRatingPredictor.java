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
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSortedSet;

import java.util.Collection;

import org.grouplens.lenskit.RatingPredictor;
import org.grouplens.lenskit.data.ScoredId;
import org.grouplens.lenskit.data.vector.MutableSparseVector;
import org.grouplens.lenskit.data.vector.SparseVector;
import org.grouplens.lenskit.util.DoubleFunction;
import org.grouplens.lenskit.util.LongSortedArraySet;

/**
 * Do recommendations and predictions based on SVD matrix factorization.
 *
 * Recommendation is done based on folding-in.  The strategy is do a fold-in
 * operation as described in
 * <a href="http://www.grouplens.org/node/212">Sarwar et al., 2002</a> with the
 * user's ratings.
 *
 * @todo Look at using the user's feature preferences in some cases.
 * @todo Revise this class's relationship with {@link FunkSVDRecommender}.
 *
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class SVDRatingPredictor implements RatingPredictor {
	protected final FunkSVDRecommender model;

    protected SVDRatingPredictor(FunkSVDRecommender m) {
        model = m;
    }

    /**
     * Get the number of features used by the underlying factorization.
     * @return the feature count (rank) of the factorization.
     */
    public int getFeatureCount() {
        return model.featureCount;
    }

    /**
     * Fold in a user's ratings vector to produce a feature preference vector.
     * A baseline vector is also provided; its values are subtracted from the
     * rating vector prior to folding in.
     * @param user The user ID.
     * @param ratings The user's rating vector.
     * @param base The user's baseline vector (e.g. baseline predictions).
     * @return An array of feature preference values.  The length of this array
     * will be the number of features.
     * @see #getFeatureCount()
     */
    protected double[] foldIn(long user, SparseVector ratings) {
    	final int nf = model.featureCount;
    	final double[][] ifeats = model.itemFeatures;
    	final double[] svals = model.singularValues;
        double featurePrefs[] = new double[nf];
        DoubleArrays.fill(featurePrefs, 0.0);

        for (Long2DoubleMap.Entry rating: ratings.fast()) {
            long iid = rating.getLongKey();
            int idx = model.itemIndex.getIndex(iid);
            if (idx < 0) continue;
            double r = rating.getValue();
            for (int f = 0; f < nf; f++) {
                featurePrefs[f] += r * ifeats[f][idx] / svals[f];
            }
        }

        return featurePrefs;
    }

    /* (non-Javadoc)
     * @see org.grouplens.lenskit.Recommender#predict(org.grouplens.lenskit.data.dao.UserRatingProfile, java.lang.Object)
     */
    @Override
    public ScoredId predict(long user, SparseVector ratings, long item) {
        LongArrayList items = new LongArrayList(1);
        items.add(item);
        SparseVector scores = predict(user, ratings, items);

        if (scores.containsKey(item))
            return new ScoredId(item, scores.get(item));
        else
            return null;
    }

    @Override
    public MutableSparseVector predict(long user, SparseVector ratings, Collection<Long> items) {
        MutableSparseVector rtmp = ratings.mutableCopy();
        LongSet baseTargets = new LongOpenHashSet(ratings.size() + items.size());
        baseTargets.addAll(ratings.keySet());
        baseTargets.addAll(items);
        MutableSparseVector bl = model.baseline.predict(user, ratings, ratings.keySet());
        rtmp.subtract(bl);
        double uprefs[] = foldIn(user, rtmp);

        final int nf = model.featureCount;
        final double[] svals = model.singularValues;
        final DoubleFunction clamp = model.clampingFunction;
        
        LongSortedSet iset;
        if (items instanceof LongSortedSet)
        	iset = (LongSortedSet) items;
        else
        	iset = new LongSortedArraySet(items);
        MutableSparseVector preds = new MutableSparseVector(iset);
        LongIterator iter = iset.iterator();
        while (iter.hasNext()) {
            final long item = iter.nextLong();
            final int idx = model.getItemIndex(item);
            if (idx < 0)
                continue;

            double score = 0;
            for (int f = 0; f < nf; f++) {
                score += uprefs[f] * svals[f] * model.getItemFeatureValue(idx, f);
                score = clamp.apply(score);
            }
            preds.set(item, score);
        }
        bl = model.baseline.predict(user, ratings, items);
        preds.add(bl);
        return preds;
    }
}
