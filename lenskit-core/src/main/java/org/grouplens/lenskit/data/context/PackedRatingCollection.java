/**
 * 
 */
package org.grouplens.lenskit.data.context;

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
		return data.values.length;
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
		
		public boolean hasNext() {
			return iter.hasNext();
		}
		
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
		
		public boolean hasNext() {
			return iter.hasNext();
		}
		
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
