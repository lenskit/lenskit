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

package org.grouplens.lenskit;

import it.unimi.dsi.fastutil.longs.LongSet;

import org.grouplens.lenskit.data.Cursors2;
import org.grouplens.lenskit.data.dao.RatingDataAccessObject;

/**
 * A basic implementation of an {@link AbstractPredictItemRecommender}.
 * Uses a simple, but inefficient, implementation of 
 * {@link AbstractPredictItemRecommender#getPredictableItems(long)}.
 */
public class SimplePredictItemRecommender extends AbstractPredictItemRecommender {

	public SimplePredictItemRecommender(RatingDataAccessObject dao, RatingPredictor predictor) {
		super(dao, predictor);
	}

	@Override
	protected LongSet getPredictableItems(long user) {
		return Cursors2.makeSet(dao.getItems());
	}

}
