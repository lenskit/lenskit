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

package org.grouplens.reflens.data;

import java.util.Map;

/**
 * Representation of rating vectors.  It consists of an <i>owner</i>, which has
 * ratings for a number of subjects.  The owner can be a user with ratings for
 * items, or it could be an item with ratings from users.  The rating vector
 * implies nothing about the direction of the ratings; the client code context
 * is responsible for that.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 * @param <S> The rating owner type.
 * @param <T> The type of rating subjects.
 */
public interface RatingVector<S, T> extends Iterable<ScoredObject<T>> {
	public S getOwner();
	public boolean containsObject(T object);
	public float getRating(T object);
	public Map<T,Float> getRatings();
	public void putRating(T obj, float rating);
	public float getAverage();
	public RatingVector<S, T> copy();
}
