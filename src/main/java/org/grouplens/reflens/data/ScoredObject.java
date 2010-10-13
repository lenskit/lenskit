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

import java.util.AbstractCollection;
import java.util.Collection;
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
	private T object;
	private float score;
	
	/**
	 * Construct a new scored object.
	 * @param object The object to score.
	 * @param score The object's score.
	 */
	public ScoredObject(T object, float score) {
		this.object = object;
		this.score = score;
	}
	
	public ScoredObject(Map.Entry<? extends T, Float> entry) {
		this(entry.getKey(), entry.getValue());
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
	 * @return A collection representing the map entries as scored objects.
	 */
	public static <T> Collection<ScoredObject<T>> wrap(final Collection<Map.Entry<T,Float>> entries) {
		return new AbstractCollection<ScoredObject<T>>() {

			@Override
			public Iterator<ScoredObject<T>> iterator() {
				return new IteratorWrapper<T>(entries.iterator());
			}

			@Override
			public int size() {
				return entries.size();
			}
			
		};
	}
	
	public static <T> Collection<ScoredObject<T>> wrap(final Map<T,Float> entries) {
		return wrap(entries.entrySet());
	}
	
	/**
	 * Convert map entries to scored objects en masse.  Iterators on the resulting
	 * collection are <i>fast</i> - that is, they mutate and return the same
	 * ScoredObject instance.
	 * @param <T> The type of keys.
	 * @param entries A collection of map entries.
	 * @return A collection representing the map entries as scored objects.
	 */
	public static <T> Collection<ScoredObject<T>> fastWrap(final Collection<Map.Entry<T,Float>> entries) {
		return new AbstractCollection<ScoredObject<T>>() {

			@Override
			public Iterator<ScoredObject<T>> iterator() {
				return new FastIteratorWrapper<T>(entries.iterator());
			}

			@Override
			public int size() {
				return entries.size();
			}
			
		};
	}
	
	public static <T> Collection<ScoredObject<T>> fastWrap(final Map<T,Float> entries) {
		return fastWrap(entries.entrySet());
	}
	
	private static class IteratorWrapper<I> implements Iterator<ScoredObject<I>> {
		private final Iterator<Map.Entry<I, Float>> iter;
		public IteratorWrapper(Iterator<Map.Entry<I, Float>> iter) {
			this.iter = iter;
		}
		
		public boolean hasNext() {
			return iter.hasNext();
		}
		public ScoredObject<I> next() {
			Map.Entry<I, Float> next = iter.next();
			return new ScoredObject<I>(next);
		}
		public void remove() {
			iter.remove();
		}
	}
	
	private static class FastIteratorWrapper<I> implements Iterator<ScoredObject<I>> {
		private final Iterator<Map.Entry<I, Float>> iter;
		private ScoredObject<I> obj = new ScoredObject<I>(null, Float.NaN);
		
		public FastIteratorWrapper(Iterator<Map.Entry<I, Float>> iter) {
			this.iter = iter;
		}
		
		public boolean hasNext() {
			return iter.hasNext();
		}
		public ScoredObject<I> next() {
			Map.Entry<I, Float> next = iter.next();
			obj.object = next.getKey();
			obj.score = next.getValue();
			return obj;
		}
		public void remove() {
			iter.remove();
		}
	}
	
	@Override
	public int compareTo(ScoredObject<T> other) {
		return Float.compare(score, other.getScore());
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof ScoredObject<?>) {
			ScoredObject<?> os = (ScoredObject<?>) o;
			return object.equals(os.object) && score == os.score;
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return object != null ? object.hashCode() : 0;
	}
}
