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

package org.grouplens.reflens.data.generic;

import java.util.Iterator;
import java.util.Map;

import org.grouplens.reflens.data.ObjectValue;
import org.grouplens.reflens.data.RatingVector;

public class GenericRatingVector<S, T> implements RatingVector<S, T> {
	protected MapFactory<T, Float> factory;
	protected Map<T, Float> ratings;
	protected Float average = null;
	protected final S owner;
	
	public GenericRatingVector() {
		this(null);
	}
	
	public GenericRatingVector(S owner) {
		this(new GenericMapFactory<T>(), owner, null);
	}
	
	public GenericRatingVector(S owner, Map<T,Float> ratings) {
		this(new GenericMapFactory<T>(), owner, ratings);
	}
	
	protected GenericRatingVector(MapFactory<T,Float> factory, S owner, Map<T,Float> ratings) {
		this.owner = owner;
		this.factory = factory;
		if (ratings == null) {
			this.ratings = factory.create();
		} else {
			this.ratings = factory.copy(ratings);
		}
	}

	@Override
	public S getOwner() {
		return owner;
	}
	
	@Override
	public void putRating(T obj, float rating) {
		average = null;
		ratings.put(obj, rating);
	}

	@Override
	public boolean containsObject(T key) {
		return ratings.containsKey(key);
	}

	@Override
	public float getRating(T key) {
		return ratings.get(key);
	}

	@Override
	public Map<T, Float> getRatings() {
		return ratings;
	}

	@Override
	public Iterator<ObjectValue<T>> iterator() {
		return ObjectValue.wrap(ratings.entrySet()).iterator();
	}

	@Override
	public float getAverage() {
		if (average == null) {
			float avg = 0.0f;
			for (Float v: ratings.values()) {
				avg += v;
			}
			average = avg / ratings.size();
		}
		return average;
	}
	
	@Override
	public GenericRatingVector<S, T> copy() {
		GenericRatingVector<S, T> v2 = new GenericRatingVector<S,T>(factory, owner, ratings);
		v2.average = average;
		return v2;
	}
}