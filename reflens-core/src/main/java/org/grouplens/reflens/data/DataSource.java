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
