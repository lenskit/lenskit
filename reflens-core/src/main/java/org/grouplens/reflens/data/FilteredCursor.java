/**
 * 
 */
package org.grouplens.reflens.data;

import java.util.NoSuchElementException;

import com.google.common.base.Predicate;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class FilteredCursor<T> extends AbstractCursor<T> {
	private final Predicate<T> filter;
	private final Cursor<T> cursor;
	private T nval;
	
	public FilteredCursor(Cursor<T> cursor, Predicate<T> filter) {
		this.filter = filter;
		this.cursor = cursor;
		nval = null;
	}
	
	@Override
	public void close() {
		cursor.close();
	}
	
	@Override
	public boolean hasNext() {
		while (nval == null && cursor.hasNext()) {
			nval = cursor.next();
			if (!filter.apply(nval))
				nval = null;
		}
		return nval != null;
	}
	
	@Override
	public T next() {
		if (hasNext()) {
			T v = nval;
			nval = null;
			return v;
		} else {
			throw new NoSuchElementException();
		}
	}
}
