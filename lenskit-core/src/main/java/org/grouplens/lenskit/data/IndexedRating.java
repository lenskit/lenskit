package org.grouplens.lenskit.data;

import org.grouplens.lenskit.data.context.BuildContext;

/**
 * Rating that also knows the indexes for its user and item.
 * @see BuildContext
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public interface IndexedRating extends Rating {
	/**
	 * Return the index for the user.  Indexes are 0-based and consecutive, so
	 * they can be used for indexing into arrays.
	 * @return The user index.
	 */
	int getUserIndex();
	
	/**
	 * Return the index for the item.  Indexes are 0-based and consecutive, so
	 * they can be used for indexing into arrays.
	 * @return The item index.
	 */
	int getItemIndex();
}
