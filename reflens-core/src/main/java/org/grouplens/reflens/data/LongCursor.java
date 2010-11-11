/**
 * 
 */
package org.grouplens.reflens.data;

import it.unimi.dsi.fastutil.longs.LongIterable;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public interface LongCursor extends Cursor<Long>, LongIterable {
	public long nextLong();
}
