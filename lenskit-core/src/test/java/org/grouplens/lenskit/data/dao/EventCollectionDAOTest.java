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
package org.grouplens.lenskit.data.dao;

import com.google.common.collect.Lists;
import org.grouplens.lenskit.cursors.Cursors;
import org.lenskit.data.events.Event;
import org.lenskit.data.ratings.Rating;
import org.hamcrest.Matcher;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class EventCollectionDAOTest {
    // in this file, we don't bother to close cursors, as they're list-backed.

    @Test
    public void testEmptyStream() {
        List<Event> events = Collections.emptyList();
        EventDAO dao = EventCollectionDAO.create(events);
        assertThat(dao.streamEvents().hasNext(), equalTo(false));
        assertThat(dao.streamEvents(Rating.class).hasNext(),
                   equalTo(false));
        assertThat(dao.streamEvents(Rating.class, SortOrder.ITEM).hasNext(),
                   equalTo(false));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testRatingStream() {
        List<Rating> ratings = Lists.newArrayList(
                Rating.create(1, 2, 3.5),
                Rating.create(1, 3, 4),
                Rating.create(2, 4, 3)
        );
        EventDAO dao = EventCollectionDAO.create(ratings);
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
                Rating.create(1, 2, 3.5),
                Rating.create(1, 3, 4),
                Rating.create(2, 4, 3)
        );
        EventDAO dao = EventCollectionDAO.create(ratings);
        assertThat(Cursors.makeList(dao.streamEvents(Purchase.class)),
                   emptyIterable());
        assertThat(Cursors.makeList(dao.streamEvents(Purchase.class, SortOrder.ITEM)),
                   emptyIterable());
    }

    @Test
    public void testFilterMixed() {
        List<Event> ratings = Lists.newArrayList(
                Rating.create(1, 2, 3.5),
                new Purchase(1, 4),
                Rating.create(1, 3, 4),
                Rating.create(2, 4, 3)
        );
        EventDAO dao = EventCollectionDAO.create(ratings);
        assertThat(Cursors.makeList(dao.streamEvents(Purchase.class)),
                   hasSize(1));
        assertThat(Cursors.makeList(dao.streamEvents(Rating.class)),
                   hasSize(3));
        assertThat(Cursors.makeList(dao.streamEvents(Purchase.class, SortOrder.ITEM)),
                   hasSize(1));
        assertThat(Cursors.makeList(dao.streamEvents(Rating.class, SortOrder.ITEM)),
                   hasSize(3));
    }

    static class Purchase implements Event {
        private final long user;
        private final long item;

        Purchase(long user, long item) {
            this.user = user;
            this.item = item;
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
