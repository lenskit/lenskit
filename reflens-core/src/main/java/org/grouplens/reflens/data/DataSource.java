/* RefLens, a reference implementation of recommender algorithms.
 * Copyright 2010 <AUTHOR> (TODO: insert author name)
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 * 
 * Linking this library statically or dynamically with other modules is making a
 * combined work based on this library. Thus, the terms and conditions of the
 * GNU General Public License cover the whole combination.
 * 
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules to produce an
 * executable, regardless of the license terms of these independent modules, and
 * to copy and distribute the resulting executable under terms of your choice,
 * provided that you also meet, for each linked independent module, the terms
 * and conditions of the license of that module. An independent module is a
 * module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but
 * you are not obligated to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
package org.grouplens.reflens.data;

import java.util.Collection;


/**
 * Data sources for sequences of items.
 * 
 * <tt>DataSource</tt> is effectively an immutable, closeable variant of
 * {@link Collection}.  Objects can be counted and iterated, and it can be
 * closed (for instance, to close an underlying database connection).
 * 
 * Implementers are responsible to make sure that the view presented of the
 * underlying data does not change from when a data source is opened (or created)
 * to when it is closed, as recommender builders may take multiple passes over
 * the data source to do the job.  This can be accomplished with in-memory
 * caching, using the transactional semantics of the underlying storage system,
 * or by some other backend-dependent means.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 * @param T The type of data returned.
 */
public interface DataSource<T> {
	/**
	 * Get the number of items in this data source.
	 * @return The number of items in the data source.  This should be exactly
	 * the number of items accessed by iterating over the return value of {@link #cursor()}.
	 */
	public int getRowCount();
	
	/**
	 * Create a new cursor over the data source.
	 * @return A new cursor that iterates over the data source from the
	 * beginning.
	 */
	public Cursor<T> cursor();
	
	/**
	 * Close the data source.  All open cursors will be invalidated.
	 */
	public void close();
}
