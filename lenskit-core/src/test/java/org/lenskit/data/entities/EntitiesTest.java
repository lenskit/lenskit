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