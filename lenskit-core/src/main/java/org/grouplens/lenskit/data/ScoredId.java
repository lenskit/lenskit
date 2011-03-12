/*
 * RefLens, a reference implementation of recommender algorithms.
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
package org.grouplens.lenskit.data;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * Associate a double score with an object.
 * 
 * This class implements {@link Comparable}.  Instances are sorted by score.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 */
public class ScoredId implements Comparable<ScoredId> {
	private long id;
	private double score;
	
	/**
	 * Construct a new scored object.
	 * @param object The object to score.
	 * @param score The object's score.
	 */
	public ScoredId(long object, double score) {
		this.id = object;
		this.score = score;
	}
	
	public ScoredId(Map.Entry<Long, Double> entry) {
		this(entry.getKey(), entry.getValue());
	}
	
	/**
	 * Get the scored object.
	 * @return
	 */
	public long getId() {
		return id;
	}
	
	/**
	 * Get the object's score.
	 * @return
	 */
	public double getScore() {
		return score;
	}
	
	/**
	 * Convert map entries to scored objects en masse.
	 * @param <T> The type of keys.
	 * @param entries A collection of map entries.
	 * @return A collection representing the map entries as scored objects.
	 */
	public static Collection<ScoredId> wrap(final Collection<Map.Entry<Long,Double>> entries) {
		return new AbstractCollection<ScoredId>() {

			@Override
			public Iterator<ScoredId> iterator() {
				return new IteratorWrapper(entries.iterator());
			}

			@Override
			public int size() {
				return entries.size();
			}
			
		};
	}
	
	public static Collection<ScoredId> wrap(final Map<Long,Double> entries) {
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
	public static Collection<ScoredId> fastWrap(final Collection<Map.Entry<Long,Double>> entries) {
		return new AbstractCollection<ScoredId>() {

			@Override
			public Iterator<ScoredId> iterator() {
				return new FastIteratorWrapper(entries.iterator());
			}

			@Override
			public int size() {
				return entries.size();
			}
			
		};
	}
	
	public static Collection<ScoredId> fastWrap(final Map<Long,Double> entries) {
		return fastWrap(entries.entrySet());
	}
	
	private static class IteratorWrapper implements Iterator<ScoredId> {
		private final Iterator<Map.Entry<Long, Double>> iter;
		public IteratorWrapper(Iterator<Map.Entry<Long, Double>> iter) {
			this.iter = iter;
		}
		
		public boolean hasNext() {
			return iter.hasNext();
		}
		public ScoredId next() {
			Map.Entry<Long, Double> next = iter.next();
			return new ScoredId(next);
		}
		public void remove() {
			iter.remove();
		}
	}
	
	private static class FastIteratorWrapper implements Iterator<ScoredId> {
		private final Iterator<Map.Entry<Long, Double>> iter;
		private ScoredId obj = new ScoredId(0, Double.NaN);
		
		public FastIteratorWrapper(Iterator<Map.Entry<Long, Double>> iter) {
			this.iter = iter;
		}
		
		public boolean hasNext() {
			return iter.hasNext();
		}
		public ScoredId next() {
			Map.Entry<Long, Double> next = iter.next();
			obj.id = next.getKey();
			obj.score = next.getValue();
			return obj;
		}
		public void remove() {
			iter.remove();
		}
	}
	
	@Override
	public int compareTo(ScoredId other) {
		return Double.compare(score, other.getScore());
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof ScoredId) {
			ScoredId os = (ScoredId) o;
			return id == os.id && score == os.score;
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return (int) (id ^ (id >> 32));
	}
}
