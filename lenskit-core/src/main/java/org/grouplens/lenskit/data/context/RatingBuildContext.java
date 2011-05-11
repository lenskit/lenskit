/*
 * LensKit, a reference implementation of recommender algorithms.
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

import java.io.Closeable;

import javax.annotation.concurrent.ThreadSafe;

import org.grouplens.lenskit.data.dao.RatingDataAccessObject;

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
 * or even disk files.  RecommenderEngine build code does assume, however, that
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
 * @author Stefan Nelson-Lindall <stefan@cs.umn.edu>
 */
@ThreadSafe
public interface RatingBuildContext extends Closeable {
    /**
     * Key is a typed object that has instance identity semantics. It is used to
     * store and retrieve cached values within a RatingBuildContext.
     * 
     * @author Michael Ludwig
     * @param <T>
     */
    public static class Key<T> { }

    /**
     * Return the dao that is backing this RatingBuildContext.
     * @return Return the value last associated with the given key.
     * @param <T> The return type
     * @param key The key instance used to store the value in this context
     * @return The cached value or null if no value was cached
     */
	<T> T get(Key<T> key);

    /**
     * Store a value in this RatingBuildContext so that it can be retrieved
     * later. A common use case for this method is providing memoization support
     * for Builders. This will overwrite any previous value associated with the
     * key.
     * 
     * @param <T> The type of the stored value
     * @param key The key that the value is associated with
     * @param value The new value
     */
	<T> void put(Key<T> key, T value);
	
	/**
	 * Get the DAO for this build.
	 * @review Do we still need this with dependency injection?
	 */
	RatingDataAccessObject getDAO();
	
	/**
	 * Get the full rating snapshot for building a recommender.
	 * @return The RatingSnapshot containing all ratings at build time.
	 */
	RatingSnapshot ratingSnapshot();
	
	/**
	 * Get a RatingSnapshot suitable for training a recommender.
	 * Based on the build context parameters, this snapshot will contain
	 * a portion of the data, with the rest held out in the tuningSnapshot
	 * 
	 * <p>This (and the companion method {@link #tuningSnapshot()}) are only
	 * needed when a recommender's build process requires a train/test phase.
	 * If the recommender is only to be built and does not need a train-test
	 * split to optimize itself, use {@link #ratingSnapshot()}.
	 * 
	 * @return	a RatingSnapshot for viewing the training data
	 */
	RatingSnapshot trainingSnapshot();
	
	/**
	 * Get a RatingSnapshot suitable for testing a recommender.
	 * 
	 * Based on the build context parameters, this this snapshot will contain
	 * the portion of the data that was held out from the training data.
	 * @return	a RatingSnapshot for viewing the testing data
	 */
	RatingSnapshot tuningSnapshot();
	
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
