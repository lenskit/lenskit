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
import org.grouplens.lenskit.data.event.Rating;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class PrefetchingItemEventDAOTest {
    @Test
    public void testGetEvents() {
        List<Rating> ratings = Lists.newArrayList(
                Rating.create(1, 2, 3.5),
                Rating.create(1, 3, 4),
                Rating.create(2, 2, 3)
        );
        EventDAO dao = EventCollectionDAO.create(ratings);
        PrefetchingItemEventDAO iedao = new PrefetchingItemEventDAO(dao);
        assertThat(iedao.getEventsForItem(2), hasSize(2));
        assertThat(iedao.getEventsForItem(3), hasSize(1));
        assertThat(iedao.getEventsForItem(4), nullValue());
    }

    @Test
    public void testGetUsers() {
        List<Rating> ratings = Lists.newArrayList(
                Rating.create(1, 2, 3.5),
                Rating.create(1, 3, 4),
                Rating.create(2, 2, 3)
        );
        EventDAO dao = EventCollectionDAO.create(ratings);
        PrefetchingItemEventDAO iedao = new PrefetchingItemEventDAO(dao);
        assertThat(iedao.getUsersForItem(2), hasSize(2));
        assertThat(iedao.getUsersForItem(3), hasSize(1));
        assertThat(iedao.getUsersForItem(4), nullValue());
    }
}
