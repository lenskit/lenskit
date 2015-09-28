/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 LensKit Contributors.  See CONTRIBUTORS.md.
 * Work on LensKit has been funded by the National Science Foundation under
 * grants IIS 05-34939, 08-08692, 08-12148, and 10-17697.
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
package org.lenskit.eval.traintest;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import org.grouplens.lenskit.data.history.UserHistory;
import org.lenskit.data.events.Event;
import org.lenskit.data.ratings.Rating;
import org.lenskit.data.ratings.Ratings;

/**
 * A test user's data.
 *
 * @see TestUserBuilder
 */
public class TestUser {
    private final UserHistory<Event> trainHistory;
    private final UserHistory<Event> testHistory;
    private transient volatile Long2DoubleMap testRatings;

    /**
     * Construct a new test user object.
     * @param train The training history.
     * @param test The test history.
     */
    public TestUser(UserHistory<Event> train, UserHistory<Event> test) {
        Preconditions.checkNotNull(train, "training history");
        Preconditions.checkNotNull(test, "test history");
        Preconditions.checkArgument(train.getUserId() == test.getUserId(),
                                    "user histories have different user IDs");
        trainHistory = train;
        testHistory = test;
    }

    /**
     * Make a builder for test users.
     * @return A new builder for a a test user object.
     */
    public static TestUserBuilder newBuilder() {
        return new TestUserBuilder();
    }

    /**
     * Get the ID of this user.
     *
     * @return The user's ID.
     */
    public long getUserId() {
        return trainHistory.getUserId();
    }

    /**
     * Return this user's training history.
     *
     * @return The history of the user from the training/query set.
     */
    public UserHistory<Event> getTrainHistory() {
        return trainHistory;
    }

    /**
     * Return this user's test history.
     *
     * @return The history of the user from the test set.
     */
    public UserHistory<Event> getTestHistory() {
        return testHistory;
    }

    /**
     * Get the user's test ratings.
     *
     * Summarizes the user's ratings from the history.
     *
     * @return The user's ratings for the test items.
     */
    public Long2DoubleMap getTestRatings() {
        if (testRatings == null) {
            testRatings = Ratings.userRatingVector(testHistory.filter(Rating.class)).asMap();
        }
        return testRatings;
    }
}
