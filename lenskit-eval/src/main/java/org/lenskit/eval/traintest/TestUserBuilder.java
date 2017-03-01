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

import com.google.common.collect.ImmutableList;
import org.lenskit.data.entities.CommonAttributes;
import org.lenskit.data.entities.CommonTypes;
import org.lenskit.data.entities.Entities;
import org.lenskit.data.entities.Entity;
import org.lenskit.data.ratings.Rating;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder for test user objects.
 *
 * @see TestUser
 */
public class TestUserBuilder {
    private long userId;
    private List<Entity> trainEvents = new ArrayList<>();
    private List<Entity> testEvents = new ArrayList<>();

    /**
     * Construct a new test user builder.
     */
    public TestUserBuilder() {}

    public TestUserBuilder setUserId(long uid) {
        userId = uid;
        return this;
    }

    public TestUserBuilder addTestEntity(Entity... events) {
        for (Entity e: events) {
            if (e.get(CommonAttributes.USER_ID) != userId) {
                throw new IllegalArgumentException("invalid user ID: " + e.get(CommonAttributes.USER_ID));
            }
            testEvents.add(e);
        }
        return this;
    }

    public TestUserBuilder addTestRating(long iid, double score) {
        addTestEntity(Rating.create(userId, iid, score));
        return this;
    }

    public TestUserBuilder addTrainEntity(Entity... events) {
        for (Entity e: events) {
            trainEvents.add(e);
        }
        return this;
    }

    public TestUserBuilder setTrainHistory(List<Entity> train) {
        trainEvents = new ArrayList<>(train);
        return this;
    }

    public TestUserBuilder setTestHistory(List<Entity> test) {
        testEvents = new ArrayList<>(test);
        return this;
    }

    public TestUser build() {
        return new TestUser(Entities.create(CommonTypes.USER, userId),
                            ImmutableList.copyOf(trainEvents),
                            ImmutableList.copyOf(testEvents));
    }
}
