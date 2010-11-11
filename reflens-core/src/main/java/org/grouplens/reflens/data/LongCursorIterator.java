package org.grouplens.reflens.data;

import it.unimi.dsi.fastutil.longs.LongIterator;

class LongCursorIterator implements LongIterator {
	private final LongCursor cursor;
	
	public LongCursorIterator(LongCursor cursor) {
		this.cursor = cursor;
	}

	@Override
	public long nextLong() {
		return cursor.nextLong();
	}

	@Override
	public int skip(int n) {
		int i = 0;
		while (i < n && cursor.hasNext()) {
			nextLong();
			i++;
		}
		return i;
	}

	@Override
	public boolean hasNext() {
		return cursor.hasNext();
	}

	@Override
	public Long next() {
		return cursor.next();
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

}
