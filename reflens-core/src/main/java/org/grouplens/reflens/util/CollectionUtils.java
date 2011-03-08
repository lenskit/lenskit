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
/**
 * 
 */
package org.grouplens.reflens.util;

import it.unimi.dsi.fastutil.longs.AbstractLongCollection;
import it.unimi.dsi.fastutil.longs.AbstractLongIterator;
import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongIterator;

import java.util.Collection;
import java.util.Iterator;

/**
 * Various helper methods for working with collections (particularly Fastutil
 * collections).
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class CollectionUtils {
	/**
	 * Get a Fastutil {@link LongCollection} from a {@link Collection} of longs.
	 * This method simply casts the collection, if possible, and returns a
	 * wrapper otherwise.
	 * @param longs A collection of longs.
	 * @return The collection as a {@link LongCollection}.
	 */
	public static LongCollection fastCollection(final Collection<Long> longs) {
		if (longs instanceof LongCollection) return (LongCollection) longs;
		
		return new AbstractLongCollection() {
			private Collection<Long> base = longs;
			@Override
			public int size() {
				return base.size();
			}
			
			@Override
			public boolean contains(long key) {
				return base.contains(key);
			}
			
			@Override
			public LongIterator iterator() {
				return fastIterator(base.iterator());
			}
		};
	}
	
	/**
	 * Get a {@link LongIterator} which to iterate over a collection.
	 * This facilitiates iteration without boxing if the underlying collection
	 * is a Fastutil {@link LongCollection}.
	 * @see #fastIterator(Iterator)
	 * @param col The collection of longs.
	 * @return A Fastutil iterator for the collection.
	 */
	public static LongIterator fastIterator(final Collection<Long> col) {
		return fastIterator(col.iterator());
	}
	
	/**
	 * Cast or wrap an iterator to a Fastutil {@link LongIterator}.
	 * @param iter An iterator of longs.
	 * @return A Fastutil iterator wrapping <var>iter</var>.  If <var>iter</var>
	 * is already a Fastutil iterator (an instance of {@link LongIterator}), this
	 * is simply <var>iter</var> cast to {@link LongIterator}.  Otherwise, it is
	 * a wrapper object.
	 */
	public static LongIterator fastIterator(final Iterator<Long> iter) {
		if (iter instanceof LongIterator) return (LongIterator) iter;
		
		return new AbstractLongIterator() {
			Iterator<Long> biter = iter;
			@Override
			public boolean hasNext() {
				return biter.hasNext(); 
			}
			@Override
			public Long next() {
				return biter.next();
			}
		};
	}
}
