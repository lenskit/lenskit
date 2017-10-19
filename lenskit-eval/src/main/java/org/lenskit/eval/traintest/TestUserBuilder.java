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
