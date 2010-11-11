/**
 * 
 */
package org.grouplens.reflens.data;

import it.unimi.dsi.fastutil.longs.LongIterator;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public abstract class AbstractLongCursor extends AbstractCursor<Long> implements
		LongCursor {

	/**
	 * Implement {@link Cursor#next()} by delegating to {@link #nextLong()}.
	 */
	@Override
	public Long next() {
		return nextLong();
	}
	
	public LongIterator iterator() {
		return new LongCursorIterator(this);
	}

}
