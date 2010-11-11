/**
 * 
 */
package org.grouplens.reflens.data;


import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class TestUserProfileCursor {
	private List<Rating> ratings;
	private Cursor<Rating> ratingCursor;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		ratings = new ArrayList<Rating>();
		ratings.add(new Rating(2, 5, 3.0));
		ratings.add(new Rating(2, 3, 3.0));
		ratings.add(new Rating(2, 39, 2.5));
		ratings.add(new Rating(5, 7, 2.5));
		ratings.add(new Rating(5, 39, 7.2));
		ratingCursor = Cursors.wrap(ratings);
	}

	@Test
	public void testCursor() {
		Cursor<UserRatingProfile> cursor =
			new AbstractRatingDataSource.UserProfileCursor(ratingCursor);
		assertTrue(cursor.hasNext());
		UserRatingProfile profile = cursor.next();
		assertTrue(cursor.hasNext());
		assertEquals(2, profile.getUser());
		assertEquals(3, profile.getRatings().size());
		profile = cursor.next();
		assertFalse(cursor.hasNext());
		assertEquals(5, profile.getUser());
		assertEquals(2, profile.getRatings().size());
	}

}
