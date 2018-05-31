/*
 * LensKit, an open-source toolkit for recommender systems.
 * Copyright 2014-2017 LensKit contributors (see CONTRIBUTORS.md)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.lenskit.eval.traintest;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.lenskit.data.entities.CommonAttributes;
import org.lenskit.data.entities.CommonTypes;
import org.lenskit.data.entities.Entities;
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
    private final List<Entity> trainHistory;
    private final List<Entity> testHistory;
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
    public TestUser(Entity user, List<Entity> train, List<Entity> test) {
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

    public Entity getUser() {
        return user;
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
    public List<Entity> getTrainHistory() {
        return trainHistory;
    }

    public LongSet getTrainItems() {

        LongSet items = trainItems;
        if (items == null) {
            items = new LongOpenHashSet();
            for (Entity e : trainHistory) {
                if(e.hasAttribute(CommonAttributes.ITEM_ID)) {
                    items.add(e.getLong(CommonAttributes.ITEM_ID));
                }
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
    public List<Entity> getTestHistory() {
        return testHistory;
    }

    public LongSet getTestItems() {
        LongSet items = testItems;
        if (items == null) {
            items = new LongOpenHashSet();
            for (Entity e : testHistory) {
                if(e.hasAttribute(CommonAttributes.ITEM_ID)) {
                    items.add(e.getLong(CommonAttributes.ITEM_ID));
                }
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
            Predicate<Entity> predicate = Entities.typePredicate(CommonTypes.RATING);
            Function<Entity, Rating> targetViewClass = Entities.projection(Rating.class);
            ImmutableList<Rating> list = FluentIterable.from(testHistory).filter(predicate).transform(targetViewClass).toList();
            testRatings = Ratings.userRatingVector(list);
        }
        return testRatings;
    }
}
