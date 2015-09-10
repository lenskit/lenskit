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
package org.lenskit.data.ratings;

import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class RatingBuilderTest {
    @Test
    public void testInitialState() {
        RatingBuilder rb = new RatingBuilder();
        assertThat(rb.hasRating(), equalTo(false));
        try {
            rb.build();
            fail("building a rating should fail");
        } catch (IllegalStateException e) {
            /* expected */
        }
    }

    @Test
    public void testSetUserId() {
        RatingBuilder rb = new RatingBuilder();
        rb.setUserId(42);
        assertThat(rb.getUserId(), equalTo(42L));
    }

    @Test
    public void testSetItemId() {
        RatingBuilder rb = new RatingBuilder();
        rb.setItemId(42);
        assertThat(rb.getItemId(), equalTo(42L));
    }

    @Test
    public void testSetRating() {
        RatingBuilder rb = new RatingBuilder();
        rb.setRating(3.5);
        assertThat(rb.hasRating(), equalTo(true));
        assertThat(rb.getRating(), equalTo(3.5));
    }

    @Test
    public void testClearRating() {
        RatingBuilder rb = new RatingBuilder();
        rb.setRating(3.5);
        rb.clearRating();
        assertThat(rb.hasRating(), equalTo(false));
    }

    @Test
    public void testSetTimestamp() {
        RatingBuilder rb = new RatingBuilder();
        rb.setTimestamp(235909);
        assertThat(rb.getTimestamp(), equalTo(235909L));
    }

    @Test
    public void testBuildRating() {
        Rating r = new RatingBuilder()
                .setUserId(692)
                .setItemId(483)
                .setRating(3.5)
                .setTimestamp(349702)
                .build();
        assertThat(r, notNullValue());
        assertThat(r.getUserId(), equalTo(692L));
        assertThat(r.getItemId(), equalTo(483L));
        assertThat(r.hasValue(), equalTo(true));
        assertThat(r.getValue(), equalTo(3.5));
        assertThat(r.getTimestamp(), equalTo(349702L));
    }

    @Test
    public void testBuildUnrate() {
        Rating r = new RatingBuilder()
                .setUserId(692)
                .setItemId(483)
                .setTimestamp(349702)
                .build();
        assertThat(r, notNullValue());
        assertThat(r.getUserId(), equalTo(692L));
        assertThat(r.getItemId(), equalTo(483L));
        assertThat(r.hasValue(), equalTo(false));
        assertThat(r.getTimestamp(), equalTo(349702L));
    }
}
