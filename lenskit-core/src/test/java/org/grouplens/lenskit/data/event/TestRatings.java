package org.grouplens.lenskit.data.event;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Michael Ekstrand
 */
public class TestRatings {
    @Test
    public void testEmptyURV() {
        List<Rating> ratings = Collections.emptyList();
        MutableSparseVector urv = Ratings.userRatingVector(ratings);
        assertThat(urv.isEmpty(), equalTo(true));
        assertThat(urv.size(), equalTo(0));
    }

    @Test
    public void testURVRatingsInOrder() {
        List<Rating> ratings = new ArrayList<Rating>();
        ratings.add(new SimpleRating(1, 1, 2, 3.0, 3));
        ratings.add(new SimpleRating(2, 1, 3, 4.5, 7));
        ratings.add(new SimpleRating(3, 1, 5, 2.3, 10));
        MutableSparseVector urv = Ratings.userRatingVector(ratings);
        assertThat(urv.isEmpty(), equalTo(false));
        assertThat(urv.size(), equalTo(3));
        assertThat(urv.get(2), closeTo(3.0, 1.0e-6));
        assertThat(urv.get(3), closeTo(4.5, 1.0e-6));
        assertThat(urv.get(5), closeTo(2.3, 1.0e-6));
        assertThat(urv.containsKey(1), equalTo(false));
    }

    @Test
    public void testURVRatingsOutOfOrder() {
        List<Rating> ratings = new ArrayList<Rating>();
        ratings.add(new SimpleRating(1, 1, 2, 3.0, 3));
        ratings.add(new SimpleRating(3, 1, 5, 2.3, 7));
        ratings.add(new SimpleRating(2, 1, 3, 4.5, 10));
        MutableSparseVector urv = Ratings.userRatingVector(ratings);
        assertThat(urv.isEmpty(), equalTo(false));
        assertThat(urv.size(), equalTo(3));
        assertThat(urv.get(2), closeTo(3.0, 1.0e-6));
        assertThat(urv.get(3), closeTo(4.5, 1.0e-6));
        assertThat(urv.get(5), closeTo(2.3, 1.0e-6));
        assertThat(urv.containsKey(1), equalTo(false));
    }

    @Test
    public void testURVRatingsDup() {
        List<Rating> ratings = new ArrayList<Rating>();
        ratings.add(new SimpleRating(1, 1, 2, 3.0, 3));
        ratings.add(new SimpleRating(3, 1, 5, 2.3, 4));
        ratings.add(new SimpleRating(2, 1, 3, 4.5, 5));
        ratings.add(new SimpleRating(4, 1, 5, 3.7, 6));
        MutableSparseVector urv = Ratings.userRatingVector(ratings);
        assertThat(urv.isEmpty(), equalTo(false));
        assertThat(urv.size(), equalTo(3));
        assertThat(urv.get(2), closeTo(3.0, 1.0e-6));
        assertThat(urv.get(3), closeTo(4.5, 1.0e-6));
        assertThat(urv.get(5), closeTo(3.7, 1.0e-6));
        assertThat(urv.containsKey(1), equalTo(false));
    }

    @Test
    public void testURVRatingsRmv() {
        List<Rating> ratings = new ArrayList<Rating>();
        ratings.add(new SimpleRating(1, 1, 2, 3.0, 3));
        ratings.add(new SimpleRating(3, 1, 5, 2.3, 5));
        ratings.add(new SimpleNullRating(5, 1, 2, 7));
        ratings.add(new SimpleRating(2, 1, 3, 4.5, 8));
        MutableSparseVector urv = Ratings.userRatingVector(ratings);
        assertThat(urv.isEmpty(), equalTo(false));
        assertThat(urv.size(), equalTo(2));
        assertThat(urv.get(3), closeTo(4.5, 1.0e-6));
        assertThat(urv.get(5), closeTo(2.3, 1.0e-6));
        assertThat(urv.containsKey(1), equalTo(false));
        assertThat(urv.containsKey(2), equalTo(false));
    }

    @Test
    public void testURVRatingsDupOutOfOrder() {
        List<Rating> ratings = new ArrayList<Rating>();
        ratings.add(new SimpleRating(1, 1, 2, 3.0, 3));
        ratings.add(new SimpleRating(3, 1, 5, 2.3, 7));
        ratings.add(new SimpleRating(2, 1, 3, 4.5, 5));
        ratings.add(new SimpleRating(4, 1, 5, 3.7, 6));
        MutableSparseVector urv = Ratings.userRatingVector(ratings);
        assertThat(urv.isEmpty(), equalTo(false));
        assertThat(urv.size(), equalTo(3));
        assertThat(urv.get(2), closeTo(3.0, 1.0e-6));
        assertThat(urv.get(3), closeTo(4.5, 1.0e-6));
        assertThat(urv.get(5), closeTo(2.3, 1.0e-6));
        assertThat(urv.containsKey(1), equalTo(false));
    }

    @Test
    public void testEmptyIRV() {
        List<Rating> ratings = Collections.emptyList();
        MutableSparseVector urv = Ratings.itemRatingVector(ratings);
        assertThat(urv.isEmpty(), equalTo(true));
        assertThat(urv.size(), equalTo(0));
    }

    @Test
    public void testIRVRatings() {
        List<Rating> ratings = new ArrayList<Rating>();
        ratings.add(new SimpleRating(1, 1, 2, 3.0, 1));
        ratings.add(new SimpleRating(2, 3, 2, 4.5, 2));
        ratings.add(new SimpleRating(3, 2, 2, 2.3, 3));
        ratings.add(new SimpleRating(4, 3, 2, 4.5, 10));
        MutableSparseVector urv = Ratings.itemRatingVector(ratings);
        assertThat(urv.isEmpty(), equalTo(false));
        assertThat(urv.size(), equalTo(3));
        assertThat(urv.get(1), closeTo(3.0, 1.0e-6));
        assertThat(urv.get(3), closeTo(4.5, 1.0e-6));
        assertThat(urv.get(2), closeTo(2.3, 1.0e-6));
        assertThat(urv.containsKey(5), equalTo(false));
    }
}
