package org.grouplens.lenskit.data.history;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collection;

import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.event.Ratings;
import org.grouplens.lenskit.data.event.SimpleRating;
import org.grouplens.lenskit.vector.MutableSparseVector;
import org.grouplens.lenskit.vector.SparseVector;
import org.junit.Test;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class TestUserVector {
    private final static double EPSILON = 1.0e-6; 

    /**
     * Test method for {@link org.grouplens.lenskit.data.event.Ratings#userRatingVector(java.util.Collection)}.
     */
    @SuppressWarnings("deprecation")
    @Test
    public void testUserRatingVector() {
        Collection<Rating> ratings = new ArrayList<Rating>();
        ratings.add(new SimpleRating(1, 5, 7, 3.5));
        ratings.add(new SimpleRating(2, 5, 3, 1.5));
        ratings.add(new SimpleRating(3, 5, 8, 2));
        SparseVector v = Ratings.userRatingVector(ratings);
        assertEquals(3, v.size());
        assertEquals(7, v.sum(), EPSILON);
        
        long[] keys = {3, 7, 8};
        double[] values = {1.5, 3.5, 2};
        SparseVector sv = MutableSparseVector.wrap(keys, values);
        assertEquals(sv, v);
    }

}
