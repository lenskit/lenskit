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
/**
 * 
 */
package org.grouplens.lenskit.data.snapshot;

import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;

import java.util.AbstractCollection;
import java.util.Iterator;

import org.grouplens.lenskit.data.IndexedRating;
import org.grouplens.lenskit.util.FastCollection;
import org.grouplens.lenskit.util.IntIntervalList;

/**
 * Collection for packed rating data.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
class PackedRatingCollection extends AbstractCollection<IndexedRating>
		implements FastCollection<IndexedRating> {
	final private PackedRatingData data;
	final private IntList indices;
	
	PackedRatingCollection(PackedRatingData data) {
	    this.data = data;
	    this.indices = new IntIntervalList(data.values.length);
	}
	
	PackedRatingCollection(PackedRatingData data, IntList indices) {
		this.data = data;
		this.indices = indices;
	}

	@Override
	public Iterator<IndexedRating> iterator() {
		return new IteratorImpl();
	}

	@Override
	public int size() {
		return indices.size();
	}

	@Override
	public Iterable<IndexedRating> fast() {
		return new Iterable<IndexedRating>() {
			@Override
			public Iterator<IndexedRating> iterator() {
				return fastIterator();
			}
		};
	}

	@Override
	public Iterator<IndexedRating> fastIterator() {
		return new FastIteratorImpl();
	}

	private final class IteratorImpl implements Iterator<IndexedRating> {
		private final IntIterator iter;
		
		IteratorImpl() {
			iter = indices.iterator();
		}
		
		@Override
        public boolean hasNext() {
			return iter.hasNext();
		}
		
		@Override
        public IndexedRating next() {
			final int index = iter.next();
			return data.makeRating(index);
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
	
	private final class FastIteratorImpl implements Iterator<IndexedRating> {
		private final IntIterator iter;
		private PackedRatingData.IndirectRating rating;
		
		FastIteratorImpl() {
			iter = indices.iterator();
		}
		
		@Override
        public boolean hasNext() {
			return iter.hasNext();
		}
		
		@Override
        public IndexedRating next() {
			final int index = iter.next();
			if (rating == null)
				rating = data.makeIndirectRating(index);
			else
				rating.index = index;
			return rating;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
}
