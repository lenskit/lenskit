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
package org.lenskit.eval.crossfold;

import org.junit.Test;
import org.lenskit.data.events.Event;
import org.lenskit.data.ratings.Rating;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class SortOrderTest {
    @Test
    public void testRandom() {
        List<Event> events = new ArrayList<>();
        events.add(Rating.create(42, 1, 3.5, 100));
        events.add(Rating.create(42, 2, 4.0, 105));
        events.add(Rating.create(42, 3, 2.5, 98));

        List<Event> copy = new ArrayList<>(events);

        SortOrder.RANDOM.apply(copy, new Random());

        assertThat(copy, hasSize(3));
        for (Event e: events) {
            assertThat(copy, hasItem(e));
        }
    }

    @Test
    public void testTimestamp() {
        List<Rating> events = new ArrayList<>();
        events.add(Rating.create(42, 1, 3.5, 100));
        events.add(Rating.create(42, 2, 4.0, 105));
        events.add(Rating.create(42, 3, 2.5, 98));

        List<Rating> copy = new ArrayList<>(events);

        SortOrder.TIMESTAMP.apply(copy, new Random());

        assertThat(copy, hasSize(3));
        assertThat(copy, contains(Rating.create(42, 3, 2.5, 98),
                                  Rating.create(42, 1, 3.5, 100),
                                  Rating.create(42, 2, 4.0, 105)));
    }
}
