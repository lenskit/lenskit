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
package org.lenskit.data.entities;

import org.junit.Test;
import org.lenskit.data.ratings.Rating;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.*;

public class EntitiesTest {
    @Test
    public void testNullProject() {
        Entity e = Entities.newBuilder(CommonTypes.RATING)
                           .setId(10)
                           .setAttribute(CommonAttributes.USER_ID, 15L)
                           .setAttribute(CommonAttributes.ITEM_ID, 25L)
                           .setAttribute(CommonAttributes.RATING, 3.5)
                           .setAttribute(CommonAttributes.TIMESTAMP, 2308010L)
                           .build();
        Entity e2 = Entities.project(e, Entity.class);
        assertThat(e2, sameInstance(e));
    }

    @Test
    public void testConvertToRating() {
        Entity e = Entities.newBuilder(CommonTypes.RATING)
                           .setId(10)
                           .setAttribute(CommonAttributes.USER_ID, 15L)
                           .setAttribute(CommonAttributes.ITEM_ID, 25L)
                           .setAttribute(CommonAttributes.RATING, 3.5)
                           .setAttribute(CommonAttributes.TIMESTAMP, 2308010L)
                           .build();
        Rating rating = Entities.project(e, Rating.class);
        assertThat(rating.getId(), equalTo(10L));
        assertThat(rating.getUserId(), equalTo(15L));
        assertThat(rating.getValue(), equalTo(3.5));
        assertThat(rating.getTimestamp(), equalTo(2308010L));
        assertThat(rating.equals(e), equalTo(true));
        assertThat(e.equals(rating), equalTo(true));
    }
}