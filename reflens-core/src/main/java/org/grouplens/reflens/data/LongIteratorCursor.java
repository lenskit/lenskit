package org.grouplens.reflens.data;

import it.unimi.dsi.fastutil.longs.LongIterator;

public class LongIteratorCursor extends AbstractLongCursor {
	private LongIterator iterator;

	public LongIteratorCursor(LongIterator iter) {
		iterator = iter;
	}

	@Override
	public boolean hasNext() {
		return iterator.hasNext();
	}

	@Override
	public Long next() {
		return iterator.next();
	}
	
	public long nextLong() {
		return iterator.nextLong();
	}
	
	@Override
	public LongIterator iterator() {
		return iterator;
	}
}
