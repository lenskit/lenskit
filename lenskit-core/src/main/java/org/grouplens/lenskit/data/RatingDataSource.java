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
package org.grouplens.lenskit.data;

import org.grouplens.common.cursors.Cursor;


/**
 * Represents a data source providing ratings data.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public interface RatingDataSource extends DataSource {
	/**
	 * Get all ratings from the data set.
	 * @return A cursor iterating over all ratings.
	 */
	public Cursor<Rating> getRatings();
	/**
	 * Get all ratings with a sort order.
	 * @param order The sort to apply for the ratings.
	 * @return The ratings in order.
	 * @throws UnsupportedQueryException if the sort order cannot be supported.
	 */
	public Cursor<Rating> getRatings(SortOrder order);

	/**
	 * Get all user rating profiles from the system.
	 * @return A cursor returning the user rating profile for each user in the
	 * data source.
	 */
	public Cursor<UserRatingProfile> getUserRatingProfiles();
	/**
	 * Get all ratings for the specified user.
	 * @param userId The ID of the user whose ratings are requested.
	 * @return An iterator over the user's ratings.
	 */
	public Cursor<Rating> getUserRatings(long userId);
	/**
	 * Get all ratings for the specified user.
	 * @param userId The ID of the user whose ratings are requested.
	 * @param order The sort order for the ratings.
	 * @return An iterator over the user's ratings.
	 * @throws UnsupportedQueryException if the specified sort order is not
	 * supported.
	 */
	public Cursor<Rating> getUserRatings(long userId, SortOrder order);
}