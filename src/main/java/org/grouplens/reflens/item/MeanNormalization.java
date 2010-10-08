/* RefLens, a reference implementation of recommender algorithms.
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
 */

package org.grouplens.reflens.item;

import java.util.Map;

import org.grouplens.reflens.Normalizer;
import org.grouplens.reflens.data.RatingVector;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class MeanNormalization<S,T> implements Normalizer<RatingVector<S,T>> {

	/* (non-Javadoc)
	 * @see org.grouplens.reflens.Normalization#normalize(java.lang.Object)
	 */
	@Override
	public RatingVector<S,T> normalize(RatingVector<S,T> src) {
		RatingVector<S,T> v2 = src.copy();
		float mean = src.getAverage();
		for (Map.Entry<T, Float> e: src.getRatings().entrySet()) {
			v2.putRating(e.getKey(), e.getValue() - mean);
		}
		return v2;
	}
}
