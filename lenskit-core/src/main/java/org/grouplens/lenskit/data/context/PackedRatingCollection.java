/**
 * 
 */
package org.grouplens.lenskit.data.context;

import java.util.AbstractCollection;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.grouplens.lenskit.data.IndexedRating;
import org.grouplens.lenskit.util.FastCollection;

/**
 * Collection for packed rating data.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
class PackedRatingCollection extends AbstractCollection<IndexedRating>
		implements FastCollection<IndexedRating> {
	final private PackedRatingData data;
	
	PackedRatingCollection(PackedRatingData data) {
		this.data = data;
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
		private final int count;
		private int index;
		
		IteratorImpl() {
			count = data.values.length;
			index = -1;
		}
		
		public boolean hasNext() {
			return index + 1 < count;
		}
		
		public IndexedRating next() {
			if (hasNext()) {
				index += 1;
				return data.makeRating(index);
			} else {
				throw new NoSuchElementException();
			}
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
	
	private final class FastIteratorImpl implements Iterator<IndexedRating> {
		private final int count;
		private PackedRatingData.IndirectRating rating;
		
		FastIteratorImpl() {
			count = data.values.length;
			rating = data.makeIndirectRating(0);
		}
		
		public boolean hasNext() {
			return rating.index < count;
		}
		
		public IndexedRating next() {
			if (rating.index + 1 < count) {
				rating.index += 1;
				return rating;
			} else {
				throw new NoSuchElementException();
			}
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
}
