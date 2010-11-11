package org.grouplens.reflens.data;


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