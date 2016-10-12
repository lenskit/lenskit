/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2016 LensKit Contributors.  See CONTRIBUTORS.md.
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
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.lenskit.data.entities.Entity;
import org.lenskit.data.ratings.Rating;
import org.lenskit.data.ratings.Ratings;

import java.util.List;

/**
 * A test user's data.
 *
 * @see TestUserBuilder
 */
public class TestUser {
    private final Entity user;
    private final List<Rating> trainHistory;
    private final List<Rating> testHistory;
    private transient volatile LongSet trainItems;
    private transient volatile LongSet testItems;
    private transient volatile Long2DoubleMap testRatings;
    private transient volatile LongSet seenItems;

    /**
     * Construct a new test user object.
     *
     * @param train The training history.
     * @param test  The test history.
     */
    public TestUser(Entity user, List<Rating> train, List<Rating> test) {
        Preconditions.checkNotNull(train, "training history");
        Preconditions.checkNotNull(test, "test history");
        this.user = user;
        trainHistory = train;
        testHistory = test;
    }

    /**
     * Make a builder for test users.
     *
     * @return A new builder for a test user object.
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
        return user.getId();
    }

    /**
     * Return this user's training history.
     *
     * @return The history of the user from the training/query set.
     */
    public List<Rating> getTrainHistory() {
        return trainHistory;
    }

    public LongSet getTrainItems() {
        LongSet items = trainItems;
        if (items == null) {
            items = new LongOpenHashSet();
            for (Rating r : trainHistory) {
                items.add(r.getItemId());
            }
            trainItems = items;
        }
        return items;
    }

    /**
     * Return this user's test history.
     *
     * @return The history of the user from the test set.
     */
    public List<Rating> getTestHistory() {
        return testHistory;
    }

    public LongSet getTestItems() {
        LongSet items = testItems;
        if (items == null) {
            items = new LongOpenHashSet();
            for (Rating r : testHistory) {
                items.add(r.getItemId());
            }
            testItems = items;
        }
        return items;
    }

    /**
     * The set of items this user has *seen* in either training or test.
     * @return The set of all seen items (training and test).
     */
    public LongSet getSeenItems() {
        if (seenItems == null) {
            LongSet items = new LongOpenHashSet(getTrainItems());
            items.addAll(getTestItems());
            seenItems = items;
        }
        return seenItems;
    }

    /**
     * Get the user's test ratings.
     * Summarizes the user's ratings from the history.
     *
     * @return The user's ratings for the test items.
     */
    public Long2DoubleMap getTestRatings() {
        if (testRatings == null) {
            testRatings = Ratings.userRatingVector(testHistory);
        }
        return testRatings;
    }
}
