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
package org.lenskit.data.history;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.junit.Test;
import org.lenskit.data.events.Event;
import org.lenskit.data.ratings.Rating;
import org.lenskit.data.ratings.Ratings;
import org.lenskit.util.math.Vectors;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class BasicUserHistoryTest {
    @Test
    public void testEmptyList() {
        UserHistory<Event> history = History.forUser(42, ImmutableList.<Event>of());
        assertThat(history.size(), equalTo(0));
        assertThat(history.isEmpty(), equalTo(true));
        assertThat(history.getUserId(), equalTo(42L));
    }

    @Test
    public void testSingletonList() {
        Rating r = Rating.create(42, 39, 2.5);
        UserHistory<Rating> history = History.forUser(42, ImmutableList.of(r));
        assertThat(history.size(), equalTo(1));
        assertThat(history.isEmpty(), equalTo(false));
        assertThat(history.getUserId(), equalTo(42L));
        assertThat(history, contains(r));
    }

    @Test
    public void testMemoize() {
        List<Event> events = ImmutableList.of(
                (Event) Rating.create(42, 39, 2.5),
                Rating.create(42, 62, 3.5),
                Rating.create(42, 22, 3));
        UserHistory<Event> history = History.forUser(42, events);
        assertThat(history, hasSize(3));
        Long2DoubleMap v = history.memoize(Ratings.userRatingVectorFunction());
        assertThat(v.size(), equalTo(3));
        assertThat(Vectors.mean(v), equalTo(3.0));
        assertThat(history.memoize(Ratings.userRatingVectorFunction()),
                   sameInstance(v));
    }

    @Test
    public void testIdSet() {
        List<Event> events = ImmutableList.of(
                (Event) Rating.create(42, 39, 2.5),
                Rating.create(42, 62, 3.5),
                Rating.create(42, 22, 3));
        UserHistory<Event> history = History.forUser(42, events);
        assertThat(history, hasSize(3));
        LongSet ids = history.itemSet();
        assertThat(ids, hasSize(3));
        assertThat(ids, containsInAnyOrder(39L, 62L, 22L));
    }
}
