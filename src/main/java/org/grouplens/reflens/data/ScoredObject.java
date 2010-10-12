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

import java.util.Iterator;
import java.util.Map;

/**
 * Associate a floating-point score with an object.
 * 
 * This class implements {@link Comparable}.  Instances are sorted by score.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 * @param <T> The type of object to score.
 */
public class ScoredObject<T> implements Comparable<ScoredObject<T>> {
	private final T object;
	private final float score;
	
	/**
	 * Construct a new scored object.
	 * @param object The object to score.
	 * @param score The object's score.
	 */
	public ScoredObject(T object, float score) {
		this.object = object;
		this.score = score;
	}
	
	/**
	 * Get the scored object.
	 * @return
	 */
	public T getObject() {
		return object;
	}
	
	/**
	 * Get the object's score.
	 * @return
	 */
	public float getScore() {
		return score;
	}
	
	/**
	 * Convert map entries to scored objects en masse.
	 * @param <T> The type of keys.
	 * @param entries A collection of map entries.
	 * @return An interable allowing iteration of the map entries as scored
	 * objects.
	 */
	public static <T> Iterable<ScoredObject<T>> wrap(final Iterable<Map.Entry<T,Float>> entries) {
		return new Iterable<ScoredObject<T>>() {
			public Iterator<ScoredObject<T>> iterator() {
				return wrap(entries.iterator());
			}
		};
	}
	
	private static <I> Iterator<ScoredObject<I>> wrap(final Iterator<Map.Entry<I, Float>> iter) {
		return new Iterator<ScoredObject<I>>() {
			public boolean hasNext() {
				return iter.hasNext();
			}
			public ScoredObject<I> next() {
				Map.Entry<I, Float> next = iter.next();
				return new ScoredObject<I>(next.getKey(), next.getValue());
			}
			public void remove() {
				iter.remove();
			}
		};
	}
	
	@Override
	public int compareTo(ScoredObject<T> other) {
		return Float.compare(score, other.getScore());
	}
}
