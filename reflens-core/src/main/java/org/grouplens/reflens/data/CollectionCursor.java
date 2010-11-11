/**
 * 
 */
package org.grouplens.reflens.data;

import java.util.Collection;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class CollectionCursor<T> extends IteratorCursor<T> {
	private final int size;
	
	/**
	 * @param iter
	 */
	public CollectionCursor(Collection<T> collection) {
		super(collection.iterator());
		size = collection.size(); 
	}

	@Override
	public int getRowCount() {
		return size;
	}
}
