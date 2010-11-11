package org.grouplens.reflens.data;

import it.unimi.dsi.fastutil.longs.LongCollection;

public class LongCollectionCursor extends LongIteratorCursor {
	private final int size;
	
	public LongCollectionCursor(LongCollection collection) {
		super(collection.iterator());
		size = collection.size(); 
	}

	@Override
	public int getRowCount() {
		return size;
	}
}
