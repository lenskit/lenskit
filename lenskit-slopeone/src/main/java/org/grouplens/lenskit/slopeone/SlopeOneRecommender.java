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

package org.grouplens.lenskit.slopeone;

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;

import org.grouplens.lenskit.PredictorBasedDRItemRecommender;
import org.grouplens.lenskit.data.dao.RatingDataAccessObject;
import org.grouplens.lenskit.data.vector.UserRatingVector;

/**
 * A <tt>RatingRecommender</tt> that uses the Slope One algorithm.
 */
public class SlopeOneRecommender extends PredictorBasedDRItemRecommender {
	private SlopeOneRatingPredictor predictor; 
	
	/**
     * Construct a new recommender from a predictor.
     * @param predictor The predictor to use.
     */
    public SlopeOneRecommender(RatingDataAccessObject dao, SlopeOneRatingPredictor predictor) {
        super(dao, predictor);
        this.predictor = predictor;
    }
	
	@Override
	protected LongSet getPredictableItems(UserRatingVector user) {
		if (predictor.getModel().getBaselinePredictor() != null) return predictor.getModel().getItemUniverse();
		else {
			LongSet predictable = new LongOpenHashSet();
			for (long id1 : predictor.getModel().getItemUniverse()) {
				LongIterator iter = user.keySet().iterator();
				int nusers = 0;
				while (iter.hasNext() && nusers == 0) {
					nusers += predictor.getModel().getCoratings(id1, iter.next());
				}
				if (nusers > 0) predictable.add(id1);
			}
			return predictable;
		}		
	}
}
