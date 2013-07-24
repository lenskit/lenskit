/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.data.dao;

import com.google.common.collect.Lists;
import org.grouplens.lenskit.cursors.Cursors;
import org.grouplens.lenskit.data.event.Event;
import org.grouplens.lenskit.data.event.AbstractEvent;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.event.Ratings;
import org.hamcrest.Matcher;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class EventCollectionDAOTest {
    // in this file, we don't bother to close cursors, as they're list-backed.

    @Test
    public void testEmptyStream() {
        List<Event> events = Collections.emptyList();
        EventDAO dao = new EventCollectionDAO(events);
        assertThat(dao.streamEvents().hasNext(), equalTo(false));
        assertThat(dao.streamEvents(Rating.class).hasNext(),
                   equalTo(false));
        assertThat(dao.streamEvents(Rating.class, SortOrder.ITEM).hasNext(),
                   equalTo(false));
    }

    @Test
    public void testRatingStream() {
        List<Rating> ratings = Lists.newArrayList(
                Ratings.make(1, 2, 3.5),
                Ratings.make(1, 3, 4),
                Ratings.make(2, 4, 3)
        );
        EventDAO dao = new EventCollectionDAO(ratings);
        assertThat(Cursors.makeList(dao.streamEvents()),
                   (Matcher) hasSize(3));
        assertThat(Cursors.makeList(dao.streamEvents(Rating.class)),
                   (Matcher) hasSize(3));
        assertThat(Cursors.makeList(dao.streamEvents(Rating.class, SortOrder.ITEM)),
                   hasSize(3));
    }

    @Test
    public void testFilterOutAllRatings() {
        List<Rating> ratings = Lists.newArrayList(
                Ratings.make(1, 2, 3.5),
                Ratings.make(1, 3, 4),
                Ratings.make(2, 4, 3)
        );
        EventDAO dao = new EventCollectionDAO(ratings);
        assertThat(Cursors.makeList(dao.streamEvents(Purchase.class)),
                   emptyIterable());
        assertThat(Cursors.makeList(dao.streamEvents(Purchase.class, SortOrder.ITEM)),
                   emptyIterable());
    }

    @Test
    public void testFilterMixed() {
        List<Event> ratings = Lists.newArrayList(
                Ratings.make(1, 2, 3.5),
                new Purchase(-1, 1, 4),
                Ratings.make(1, 3, 4),
                Ratings.make(2, 4, 3)
        );
        EventDAO dao = new EventCollectionDAO(ratings);
        assertThat(Cursors.makeList(dao.streamEvents(Purchase.class)),
                   hasSize(1));
        assertThat(Cursors.makeList(dao.streamEvents(Rating.class)),
                   hasSize(3));
        assertThat(Cursors.makeList(dao.streamEvents(Purchase.class, SortOrder.ITEM)),
                   hasSize(1));
        assertThat(Cursors.makeList(dao.streamEvents(Rating.class, SortOrder.ITEM)),
                   hasSize(3));
    }

    static class Purchase extends AbstractEvent {
        private final long id;
        private final long user;
        private final long item;

        Purchase(long id, long user, long item) {
            this.id = id;
            this.user = user;
            this.item = item;
        }

        @Override
        public long getId() {
            return id;
        }

        @Override
        public long getUserId() {
            return user;
        }

        @Override
        public long getItemId() {
            return item;
        }

        @Override
        public long getTimestamp() {
            return -1;
        }
    }
}
