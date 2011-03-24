/*
 * RefLens, a reference implementation of recommender algorithms.
 * Copyright 2010-2011 Regents of the University of Minnesota
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.grouplens.lenskit.data.context;

import it.unimi.dsi.fastutil.longs.LongCollection;

import java.io.Closeable;

import javax.annotation.concurrent.ThreadSafe;

import org.grouplens.lenskit.data.Index;
import org.grouplens.lenskit.data.IndexedRating;
import org.grouplens.lenskit.data.RatingDataAccessObject;
import org.grouplens.lenskit.util.FastCollection;

import com.google.inject.ImplementedBy;

/**
 * Snapshot of the ratings data for building a recommender.
 * 
 * <p>The recommender build process often needs to take multiple passes over
 * the rating data.  In a live system, the data provided by a
 * {@link RatingDataAccessObject} may change between iterations.  Therefore,
 * we introduce <emph>build contexts</emph> &mdash; snapshots of the rating data
 * at a particular point in time that can be iterated as many times as necessary
 * to build the recommender.
 * 
 * <p>Implementers have a variety of options for implementing build contexts.
 * They can be in-memory snapshots, database transactions, database clones,
 * or even disk files.  Recommender build code does assume, however, that
 * multiple iterations is pretty fast.  Therefore, implementations should avoid
 * re-fetching the data over a network connection for each request.
 * 
 * <p>An additional feature provided by build contexts is that of mapping the
 * item and user IDs to consecutive, 0-based indices.  The indices <strong>may 
 * differ</strong> from one build context to another.
 * 
 * <p>The build context will usually be a singleton in a dedicated
 * recommender-building process or an appropriately scoped object in other
 * contexts.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 */
@ThreadSafe
@ImplementedBy(PackedBuildContext.class)
public interface BuildContext extends Closeable {
	/**
	 * Get the set of user IDs in the snapshot.
	 * @return A set of all known user IDs.
	 */
	LongCollection getUserIds();

	/**
	 * Get the set of item IDs in the snapshot.
	 * @return A set of all known item IDs.
	 */
	LongCollection getItemIds();
	
	/**
	 * Get the user ID index.
	 * @return The index mapping between user IDs and user indices.
	 */
	Index userIndex();
	
	/**
	 * Get the item ID index.
	 * @return The index mapping between user IDs and user indices.
	 */
	Index itemIndex();
	
	/**
	 * Get the collection of ratings in the snapshot.  The ratings are returned
	 * in an undetermined order.  It is guaranteed that no duplicate ratings
	 * appear - each <i>(user,item)</i> pair is rated at most once.
	 * @return All ratings in the system.
	 */
	FastCollection<IndexedRating> getRatings();
	
	/**
	 * Get the ratings for a particular user.  It is guaranteed that no duplicate
	 * ratings appear - each <i>(user,item)</i> pair is rated at most once.
	 * @param userId The user's ID.
	 * @return The user's ratings, or an empty collection if the user is unknown.
	 */
	FastCollection<IndexedRating> getUserRatings(long userId);
	
	/**
	 * Close the build context.  This overrides {@link Closeable#close()} to
	 * drop the exception that can be thrown.
	 * 
	 * <p>After the build context has been closed, all methods are allowed
	 * to fail.  Objects returned from those methods, however, should continue
	 * to be valid.
	 */
	void close();
}
