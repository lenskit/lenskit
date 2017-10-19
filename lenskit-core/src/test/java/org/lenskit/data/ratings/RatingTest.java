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
package org.lenskit.data.ratings;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import org.junit.Test;
import org.lenskit.data.entities.CommonAttributes;
import org.lenskit.data.entities.EntityFactory;
import org.lenskit.data.entities.NoSuchAttributeException;
import org.lenskit.data.entities.TypedName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class RatingTest {
    @Test
    public void testGetValueOfRating() {
        Rating rating = Rating.create(1, 2, 3.0, 3);
        assertThat(rating.getValue(), equalTo(3.0));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testDeprecatedFactories() {
        Rating rating = Ratings.make(1, 2, 3.0);
        Rating withTS = Ratings.make(1, 2, 3.0, 1030);
        assertThat(rating.getUserId(), equalTo(1L));
        assertThat(rating.getItemId(), equalTo(2L));
        assertThat(rating.getValue(), equalTo(3.0));

        assertThat(withTS.getUserId(), equalTo(1L));
        assertThat(withTS.getItemId(), equalTo(2L));
        assertThat(withTS.getValue(), equalTo(3.0));
        assertThat(withTS.getTimestamp(), equalTo(1030L));
    }

    @Test
    public void testSimpleEquality() {
        Rating r1 = Rating.create(1, 2, 3.0, 0);
        Rating r1a = Rating.create(1, 2, 3.0, 0);
        Rating r2 = Rating.create(1, 3, 2.5, 1);
        assertThat(r1, equalTo(r1));
        assertThat(r1a, equalTo(r1));
        assertThat(r2, not(equalTo(r1)));
        assertThat(r1, not(equalTo(r2)));
    }

    @Test
    public void testEmptyURV() {
        List<Rating> ratings = Collections.emptyList();
        Long2DoubleMap urv = Ratings.userRatingVector(ratings);
        assertThat(urv.isEmpty(), equalTo(true));
        assertThat(urv.size(), equalTo(0));
    }

    @Test
    public void testURVRatingsInOrder() {
        List<Rating> ratings = new ArrayList<>();
        ratings.add(Rating.create(1, 2, 3.0, 3));
        ratings.add(Rating.create(1, 3, 4.5, 7));
        ratings.add(Rating.create(1, 5, 2.3, 10));
        Long2DoubleMap urv = Ratings.userRatingVector(ratings);
        assertThat(urv.isEmpty(), equalTo(false));
        assertThat(urv.size(), equalTo(3));
        assertThat(urv.get(2), closeTo(3.0, 1.0e-6));
        assertThat(urv.get(3), closeTo(4.5, 1.0e-6));
        assertThat(urv.get(5), closeTo(2.3, 1.0e-6));
        assertThat(urv.containsKey(1), equalTo(false));
    }

    @Test
    public void testURVRatingsOutOfOrder() {
        List<Rating> ratings = new ArrayList<>();
        ratings.add(Rating.create(1, 2, 3.0, 3));
        ratings.add(Rating.create(1, 5, 2.3, 7));
        ratings.add(Rating.create(1, 3, 4.5, 10));
        Long2DoubleMap urv = Ratings.userRatingVector(ratings);
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
        Long2DoubleMap urv = Ratings.itemRatingVector(ratings);
        assertThat(urv.isEmpty(), equalTo(true));
        assertThat(urv.size(), equalTo(0));
    }

    @Test
    public void testIRVRatings() {
        List<Rating> ratings = new ArrayList<>();
        ratings.add(Rating.create(1, 2, 3.0, 1));
        ratings.add(Rating.create(2, 2, 2.3, 3));
        ratings.add(Rating.create(3, 2, 4.5, 10));
        Long2DoubleMap urv = Ratings.itemRatingVector(ratings);
        assertThat(urv.isEmpty(), equalTo(false));
        assertThat(urv.size(), equalTo(3));
        assertThat(urv.get(1), closeTo(3.0, 1.0e-6));
        assertThat(urv.get(3), closeTo(4.5, 1.0e-6));
        assertThat(urv.get(2), closeTo(2.3, 1.0e-6));
        assertThat(urv.containsKey(5), equalTo(false));
    }

    @Test
    public void testRatingEntity() {
        EntityFactory fac = new EntityFactory();
        Rating r = fac.rating(42, 37, 3.5, 10);
        assertThat(r.getId(), equalTo(1L));
        assertThat(r.getUserId(), equalTo(42L));
        assertThat(r.getItemId(), equalTo(37L));
        assertThat(r.getValue(), equalTo(3.5));
        assertThat(r.getTimestamp(), equalTo(10L));

        assertThat(r.getLong(CommonAttributes.ENTITY_ID), equalTo(1L));
        assertThat(r.getLong(CommonAttributes.USER_ID), equalTo(42L));
        assertThat(r.getLong(CommonAttributes.ITEM_ID), equalTo(37L));
        assertThat(r.getDouble(CommonAttributes.RATING), equalTo(3.5));
        assertThat(r.getLong(CommonAttributes.TIMESTAMP), equalTo(10L));

        assertThat(r.get(CommonAttributes.ENTITY_ID), equalTo(1L));
        assertThat(r.get(CommonAttributes.USER_ID), equalTo(42L));
        assertThat(r.get(CommonAttributes.ITEM_ID), equalTo(37L));
        assertThat(r.get(CommonAttributes.RATING), equalTo(3.5));
        assertThat(r.get(CommonAttributes.TIMESTAMP), equalTo(10L));

        assertThat(r.get("id"), equalTo(1L));
        assertThat(r.get("user"), equalTo(42L));
        assertThat(r.get("item"), equalTo(37L));
        assertThat(r.get("rating"), equalTo(3.5));
        assertThat(r.get("timestamp"), equalTo(10L));

        assertThat(r.hasAttribute("id"), equalTo(true));
        assertThat(r.hasAttribute("user"), equalTo(true));
        assertThat(r.hasAttribute("item"), equalTo(true));
        assertThat(r.hasAttribute("rating"), equalTo(true));
        assertThat(r.hasAttribute("timestamp"), equalTo(true));
        assertThat(r.hasAttribute("wombat"), equalTo(false));
    }

    @Test
    public void testRatingEntityBadAttr() {
        EntityFactory fac = new EntityFactory();
        Rating r = fac.rating(42, 37, 3.5, 10);

        try {
            r.get(TypedName.create("rating", long.class));
            fail("get with bad attribute should throw");
        } catch (Throwable th) {
            assertThat(th, instanceOf(IllegalArgumentException.class));
        }

        try {
            r.getLong(TypedName.create("rating", long.class));
            fail("get with bad attribute type should throw");
        } catch (Throwable th) {
            assertThat(th, instanceOf(IllegalArgumentException.class));
        }

        try {
            r.get("foobat");
            fail("get with missing attribute should throw");
        } catch (Throwable th) {
            assertThat(th, instanceOf(NoSuchAttributeException.class));
        }
    }
}
