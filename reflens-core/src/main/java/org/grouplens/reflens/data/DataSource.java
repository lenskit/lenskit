/*
 * RefLens, a reference implementation of recommender algorithms.
 * Copyright 2010 Michael Ekstrand <ekstrand@cs.umn.edu>
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

/**
 * 
 */
package org.grouplens.reflens.data;


/**
 * Represents a data source.  More properly, this represents a connection to a
 * data source, expected to exhibit transactional behavior in that the view of
 * the data will not change between when it is constructed and closed.  This
 * allows recommender-building code to take multiple passes over the data.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public interface DataSource {
	/**
	 * Retrieve the users from the data source.
	 * @return a cursor iterating the user IDs.
	 */
	public LongCursor getUsers();
	
	/**
	 * Get the number of users in the system.  This should be the same number
	 * of users that will be returned by iterating {@link #getUsers()}.
	 * @return The number of users in the system.
	 */
	public int getUserCount();
	
	/**
	 * Retrieve the items from the data source.
	 * @return a cursor iterating the item IDs.
	 */
	public LongCursor getItems();
	
	/**
	 * Get the number of items in the system.  This should be the same number
	 * of items that will be returned by iterating {@link #getItems()}.
	 * @return The number of items in the system.
	 */
	public int getItemCount();
	
	/**
	 * Close the data source.  Any subsequent operations are invalid.
	 * 
	 * <p>Implementations are not required to enforce closure, but are permitted
	 * to throw {@link RuntimeException}s from any other method after
	 * <tt>close()</tt> has been called for no other reason that the source
	 * has been closed.
	 */
	public void close();
}
