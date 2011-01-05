/*
 * RefLens, a reference implementation of recommender algorithms.
 * Copyright 2010 Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules to produce an
 * executable, regardless of the license terms of these independent modules, and
 * to copy and distribute the resulting executable under terms of your choice,
 * provided that you also meet, for each linked independent module, the terms
 * and conditions of the license of that module. An independent module is a
 * module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but
 * you are not obligated to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */

package org.grouplens.reflens.svd;

import it.unimi.dsi.fastutil.doubles.DoubleArrays;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;

import java.util.Collection;

import org.grouplens.reflens.BasketRecommender;
import org.grouplens.reflens.RatingPredictor;
import org.grouplens.reflens.RatingRecommender;
import org.grouplens.reflens.RecommenderEngine;
import org.grouplens.reflens.data.Index;
import org.grouplens.reflens.data.RatingVector;
import org.grouplens.reflens.data.ScoredId;

/**
 * Do recommendations and predictions based on SVD matrix factorization.
 * 
 * The implementation of SVDRecommender is based on
 * <a href="http://www.timelydevelopment.com/demos/NetflixPrize.aspx">Timely
 * Development's sample code</a>.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class SVDRecommender implements RecommenderEngine, RatingPredictor {
	
	private final Index itemIndex;
	private final RatingPredictor baseline;

	private final int numFeatures;
	private final double itemFeatures[][];
	private final double singularValues[];
	
	SVDRecommender(int nfeatures, Index itemIndexer,
			RatingPredictor baseline, double itemFeatures[][],
			double singularValues[]) { 		
		numFeatures = nfeatures;
		this.itemIndex = itemIndexer;
		this.baseline = baseline;
		this.itemFeatures = itemFeatures;
		this.singularValues = singularValues;
		assert itemFeatures.length == numFeatures;
		assert singularValues.length == numFeatures;
	}
	
		
	protected double[] foldIn(long user, RatingVector ratings, RatingVector base) {
		double featurePrefs[] = new double[numFeatures];
		DoubleArrays.fill(featurePrefs, 0.0f);
		
		for (Long2DoubleMap.Entry rating: ratings.fast()) {
			long iid = rating.getLongKey();
			int idx = itemIndex.getIndex(iid);
			if (idx < 0) continue;
			double r = rating.getValue() - base.get(iid);
			for (int f = 0; f < numFeatures; f++) {
				featurePrefs[f] += r * itemFeatures[f][idx] / singularValues[f];
			}
		}
		
		return featurePrefs;
	}

	/* (non-Javadoc)
	 * @see org.grouplens.reflens.RecommenderEngine#predict(org.grouplens.reflens.data.UserRatingProfile, java.lang.Object)
	 */
	@Override
	public ScoredId predict(long user, RatingVector ratings, long item) {
		LongArrayList items = new LongArrayList(1);
		items.add(item);
		RatingVector scores = predict(user, ratings, items);
		
		if (scores.containsId(item))
			return new ScoredId(item, scores.get(item));
		else
			return null;
	}
	
	@Override
	public RatingVector predict(long user, RatingVector ratings, Collection<Long> items) {
		LongSet tgtids = new LongOpenHashSet(ratings.idSet());
		tgtids.addAll(items);
		RatingVector base = baseline.predict(user, ratings, tgtids);
		double uprefs[] = foldIn(user, ratings, base);
		
		RatingVector preds = new RatingVector();
		for (long item: items) {
			int idx = itemIndex.getIndex(item);
			if (idx < 0)
				continue;

			double score = base.get(item);
			for (int f = 0; f < numFeatures; f++) {
				score += uprefs[f] * singularValues[f] * itemFeatures[f][idx];
			}
			preds.put(item, score);
		}
		return preds;
	}

	@Override
	public BasketRecommender getBasketRecommender() {
		return null;
	}

	@Override
	public RatingPredictor getRatingPredictor() {
		return this;
	}

	@Override
	public RatingRecommender getRatingRecommender() {
		return null;
	}
}
