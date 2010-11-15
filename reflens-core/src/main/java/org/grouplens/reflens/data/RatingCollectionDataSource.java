/**
 * 
 */
package org.grouplens.reflens.data;

import java.util.Collection;

/**
 * Simple data source wrapping a collection of ratings.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class RatingCollectionDataSource extends AbstractRatingDataSource {
	private Collection<Rating> ratings;
	
	/**
	 * Construct a new data source from a collection of ratings.
	 * @param ratings The ratings to use.
	 */
	public RatingCollectionDataSource(Collection<Rating> ratings) {
		this.ratings = ratings;
	}

	/* (non-Javadoc)
	 * @see org.grouplens.reflens.data.AbstractRatingDataSource#getRatings()
	 */
	@Override
	public Cursor<Rating> getRatings() {
		return Cursors.wrap(ratings);
	}

}
